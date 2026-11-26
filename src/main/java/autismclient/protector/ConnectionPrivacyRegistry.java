package autismclient.protector;

import autismclient.util.ProxyProtocolType;
import io.netty.channel.local.LocalAddress;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.client.multiplayer.ServerData.Type;
import net.minecraft.client.resources.server.ServerPackManager;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.PacketFlow;

public final class ConnectionPrivacyRegistry {
   static final int init_I = 1024;
   private static final Object LOCK = new Object();
   private static final Map<Connection, ConnectionPrivacyRegistry.ConnectionSession> SESSIONS_BY_CONNECTION = new IdentityHashMap<>();
   private static final Map<PacketListener, ConnectionPrivacyRegistry.ConnectionSession> SESSIONS_BY_LISTENER = new IdentityHashMap<>();
   private static final AtomicLong NEXT_LIFECYCLE_ID = new AtomicLong();

   private ConnectionPrivacyRegistry() {
   }

   public static ConnectionPrivacyRegistry.ConnectionSession register(Connection connection, ConnectionPrivacyRegistry.PrivacyMode mode, boolean multi, SocketAddress peerAddress) {
      Objects.requireNonNull(connection, "connection");
      Objects.requireNonNull(mode, "mode");
      if (peerAddress instanceof LocalAddress || connection.isMemoryConnection() || connection.getReceiving() == PacketFlow.SERVERBOUND) {
         mode = ConnectionPrivacyRegistry.PrivacyMode.MODDED;
      }

      List<Socket> retired = List.of();
      ConnectionPrivacyRegistry.ConnectionSession created;
      synchronized (LOCK) {
         ConnectionPrivacyRegistry.ConnectionSession existing = SESSIONS_BY_CONNECTION.get(connection);
         if (existing != null) {
            if (existing.active) {
               if (peerAddress instanceof LocalAddress) {
                  existing.localAddress = true;
               }

               return existing;
            }

            retired = deactivateSessionLocked(existing);
         }

         if (SESSIONS_BY_CONNECTION.size() >= 1024) {
            throw new IllegalStateException("Connection privacy registry is full (1024)");
         }

         created = new ConnectionPrivacyRegistry.ConnectionSession(NEXT_LIFECYCLE_ID.incrementAndGet(), mode, multi, connection, peerAddress);
         if (created.isLocalOrMemory()) {
            created.serverType = Type.OTHER;
         }

         SESSIONS_BY_CONNECTION.put(connection, created);
      }

      closeSocketsQuietly(retired);
      return created;
   }

   public static ConnectionPrivacyRegistry.ConnectionSession getActive(Connection connection) {
      if (connection == null) {
         return null;
      }

      synchronized (LOCK) {
         ConnectionPrivacyRegistry.ConnectionSession context = SESSIONS_BY_CONNECTION.get(connection);
         return context != null && context.active ? context : null;
      }
   }

   public static ConnectionPrivacyRegistry.ConnectionSession getByListener(PacketListener listener) {
      if (listener == null) {
         return null;
      }

      synchronized (LOCK) {
         ConnectionPrivacyRegistry.ConnectionSession context = SESSIONS_BY_LISTENER.get(listener);
         return context != null && context.active ? context : null;
      }
   }

   public static ConnectionPrivacyRegistry.ConnectionSession getByPackManager(ServerPackManager manager) {
      if (manager == null) {
         return null;
      }

      synchronized (LOCK) {
         ConnectionPrivacyRegistry.ConnectionSession found = null;

         for (ConnectionPrivacyRegistry.ConnectionSession context : SESSIONS_BY_CONNECTION.values()) {
            if (context.active && context.serverPackManager == manager) {
               if (found != null) {
                  return null;
               }

               found = context;
            }
         }

         return found;
      }
   }

   public static ConnectionPrivacyRegistry.ConnectionSession requireActive(Connection connection) {
      synchronized (LOCK) {
         return requireActiveLocked(connection);
      }
   }

   public static boolean isActive(ConnectionPrivacyRegistry.ConnectionSession context) {
      if (context != null && context.active) {
         synchronized (LOCK) {
            return context.active && SESSIONS_BY_CONNECTION.get(context.connection) == context;
         }
      } else {
         return false;
      }
   }

   public static boolean isCurrentDownload(
      ConnectionPrivacyRegistry.ConnectionSession context, long lifecycleId, ServerPackManager manager, PacketListener listener, long downloadGeneration
   ) {
      if (context != null && context.active) {
         synchronized (LOCK) {
            return context.active
               && context.lifecycleId == lifecycleId
               && context.downloadGeneration == downloadGeneration
               && context.serverPackManager == manager
               && context.packetListener == listener
               && SESSIONS_BY_CONNECTION.get(context.connection) == context;
         }
      } else {
         return false;
      }
   }

   public static ConnectionPrivacyRegistry.ConnectionSession bindServerContext(Connection connection, String host, int port, Type serverKind, ServerPackManager manager) {
      ConnectionPrivacyRegistry.ConnectionSession context = bindEndpoint(connection, host, port);
      bindServerKind(connection, serverKind);
      bindPackManager(connection, manager);
      return context;
   }

   public static ConnectionPrivacyRegistry.ConnectionSession bindEndpoint(Connection connection, String host, int port) {
      ConnectionPrivacyRegistry.ServerEndpoint endpoint = new ConnectionPrivacyRegistry.ServerEndpoint(host, port);
      synchronized (LOCK) {
         ConnectionPrivacyRegistry.ConnectionSession context = requireActiveLocked(connection);
         ConnectionPrivacyRegistry.ServerEndpoint existing = context.serverEndpoint;
         if (existing == null) {
            context.serverEndpoint = endpoint;
         } else if (!existing.equals(endpoint)) {
            throw new IllegalStateException("Original endpoint is already bound for this connection lifecycle");
         }

         return context;
      }
   }

   public static ConnectionPrivacyRegistry.ConnectionSession bindServerKind(Connection connection, Type serverKind) {
      Objects.requireNonNull(serverKind, "serverKind");
      List<Socket> retired = List.of();
      boolean conflict = false;
      ConnectionPrivacyRegistry.ConnectionSession context;
      synchronized (LOCK) {
         context = requireActiveLocked(connection);
         Type existing = context.serverType;
         if (existing == null) {
            context.serverType = serverKind;
         } else if (existing != serverKind) {
            retired = bumpDownloadGenerationLocked(context);
            conflict = true;
         }
      }

      if (conflict) {
         closeSocketsQuietly(retired);
         throw new IllegalStateException("Server kind is already bound for this connection lifecycle");
      } else {
         return context;
      }
   }

   public static ConnectionPrivacyRegistry.ConnectionSession bindPackManager(Connection connection, ServerPackManager manager) {
      Objects.requireNonNull(manager, "manager");
      List<Socket> retired = List.of();
      boolean conflict = false;
      ConnectionPrivacyRegistry.ConnectionSession context;
      synchronized (LOCK) {
         context = requireActiveLocked(connection);
         ServerPackManager existing = context.serverPackManager;
         ConnectionPrivacyRegistry.ConnectionSession previousOwner = null;

         for (ConnectionPrivacyRegistry.ConnectionSession candidate : SESSIONS_BY_CONNECTION.values()) {
            if (candidate != context && candidate.active && candidate.serverPackManager == manager) {
               if (previousOwner != null) {
                  throw new IllegalStateException("Pack manager has multiple connection owners");
               }

               previousOwner = candidate;
            }
         }

         if (previousOwner != null && existing == manager) {
            throw new IllegalStateException("Pack manager has multiple connection owners");
         }

         if (existing != null && existing != manager) {
            retired = bumpDownloadGenerationLocked(context);
            conflict = true;
         } else {
            if (previousOwner != null) {
               retired = bumpDownloadGenerationLocked(previousOwner);
               previousOwner.serverPackManager = null;
            }

            context.serverPackManager = manager;
         }
      }

      if (conflict) {
         closeSocketsQuietly(retired);
         throw new IllegalStateException("Pack manager is already bound for this connection lifecycle");
      } else {
         closeSocketsQuietly(retired);
         return context;
      }
   }

   public static ConnectionPrivacyRegistry.ConnectionSession bindAccount(Connection connection, UUID profileId, String username) {
      ConnectionPrivacyRegistry.AccountIdentity account = new ConnectionPrivacyRegistry.AccountIdentity(profileId, username);
      synchronized (LOCK) {
         ConnectionPrivacyRegistry.ConnectionSession context = requireActiveLocked(connection);
         ConnectionPrivacyRegistry.AccountIdentity existing = context.accountIdentity;
         if (existing == null) {
            context.accountIdentity = account;
         } else if (!existing.equals(account)) {
            throw new IllegalStateException("Account is already bound for this connection lifecycle");
         }

         return context;
      }
   }

   public static void bindProxy(Connection connection, ConnectionPrivacyRegistry.ProxyEndpoint route) {
      List<Socket> retired = List.of();
      boolean conflict = false;
      synchronized (LOCK) {
         ConnectionPrivacyRegistry.ConnectionSession context = requireActiveLocked(connection);
         if (!context.proxyBound) {
            context.proxyEndpoint = route;
            context.proxyBound = true;
            retired = bumpDownloadGenerationLocked(context);
         } else if (!Objects.equals(context.proxyEndpoint, route)) {
            retired = bumpDownloadGenerationLocked(context);
            conflict = true;
         }
      }

      closeSocketsQuietly(retired);
      if (conflict) {
         throw new IllegalStateException("Proxy is already bound for this connection lifecycle");
      }
   }

   public static ConnectionPrivacyRegistry.ConnectionSession bindListener(Connection connection, PacketListener listener) {
      Objects.requireNonNull(listener, "listener");
      ConnectionPrivacyRegistry.ConnectionSession context;
      List<Socket> retired;
      synchronized (LOCK) {
         context = requireActiveLocked(connection);
         PacketListener previous = context.packetListener;
         if (previous == listener) {
            return context;
         }

         ConnectionPrivacyRegistry.ConnectionSession owner = SESSIONS_BY_LISTENER.get(listener);
         if (owner != null && owner != context) {
            throw new IllegalStateException("Packet listener is already bound to another connection lifecycle");
         }

         retired = bumpDownloadGenerationLocked(context);
         if (previous != null) {
            SESSIONS_BY_LISTENER.remove(previous, context);
         }

         SESSIONS_BY_LISTENER.put(listener, context);
         context.packetListener = listener;
      }

      closeSocketsQuietly(retired);
      return context;
   }

   public static void trackDownloadSocket(ConnectionPrivacyRegistry.ConnectionSession context, long downloadGeneration, Socket socket) throws IOException {
      Objects.requireNonNull(socket, "socket");
      synchronized (LOCK) {
         if (!isCurrentDownload(context, context.lifecycleId(), context.serverPackManager, context.packetListener, downloadGeneration)) {
            throw new IOException("The resource-pack connection is no longer current");
         }

         context.activeSockets.add(socket);
      }
   }

   public static void untrackDownloadSocket(ConnectionPrivacyRegistry.ConnectionSession context, Socket socket) {
      if (context != null && socket != null) {
         synchronized (LOCK) {
            context.activeSockets.remove(socket);
         }
      }
   }

   public static boolean retireDownloadSockets(ConnectionPrivacyRegistry.ConnectionSession context) {
      if (context == null) {
         return false;
      }

      List<Socket> retired;
      synchronized (LOCK) {
         if (!context.active) {
            return false;
         }

         retired = bumpDownloadGenerationLocked(context);
      }

      closeSocketsQuietly(retired);
      return true;
   }

   public static boolean unregister(Connection connection) {
      if (connection == null) {
         return false;
      }

      List<Socket> retired;
      synchronized (LOCK) {
         ConnectionPrivacyRegistry.ConnectionSession context = SESSIONS_BY_CONNECTION.get(connection);
         if (context == null) {
            return false;
         }

         retired = deactivateSessionLocked(context);
      }

      closeSocketsQuietly(retired);
      return true;
   }

   public static boolean unregisterSession(ConnectionPrivacyRegistry.ConnectionSession context) {
      if (context == null) {
         return false;
      }

      List<Socket> retired;
      synchronized (LOCK) {
         if (SESSIONS_BY_CONNECTION.get(context.connection) != context) {
            return false;
         }

         retired = deactivateSessionLocked(context);
      }

      closeSocketsQuietly(retired);
      return true;
   }

   private static ConnectionPrivacyRegistry.ConnectionSession requireActiveLocked(Connection connection) {
      Objects.requireNonNull(connection, "connection");
      ConnectionPrivacyRegistry.ConnectionSession context = SESSIONS_BY_CONNECTION.get(connection);
      if (context != null && context.active) {
         return context;
      } else {
         throw new IllegalStateException("No active privacy context for this connection");
      }
   }

   private static List<Socket> bumpDownloadGenerationLocked(ConnectionPrivacyRegistry.ConnectionSession context) {
      context.downloadGeneration++;
      return drainSocketsLocked(context);
   }

   private static List<Socket> deactivateSessionLocked(ConnectionPrivacyRegistry.ConnectionSession context) {
      SESSIONS_BY_CONNECTION.remove(context.connection, context);
      PacketListener listener = context.packetListener;
      if (listener != null) {
         SESSIONS_BY_LISTENER.remove(listener, context);
         context.packetListener = null;
      }

      context.active = false;
      context.downloadGeneration++;
      return drainSocketsLocked(context);
   }

   private static List<Socket> drainSocketsLocked(ConnectionPrivacyRegistry.ConnectionSession context) {
      if (context.activeSockets.isEmpty()) {
         return List.of();
      }

      List<Socket> sockets = new ArrayList<>(context.activeSockets);
      context.activeSockets.clear();
      return sockets;
   }

   private static void closeSocketsQuietly(List<Socket> sockets) {
      for (Socket socket : sockets) {
         try {
            socket.close();
         } catch (IOException var4) {
         }
      }
   }

   public static void clearAll() {
      List<Socket> sockets = new ArrayList<>();
      synchronized (LOCK) {
         for (ConnectionPrivacyRegistry.ConnectionSession context : SESSIONS_BY_CONNECTION.values()) {
            context.packetListener = null;
            context.active = false;
            context.downloadGeneration++;
            sockets.addAll(drainSocketsLocked(context));
         }

         SESSIONS_BY_CONNECTION.clear();
         SESSIONS_BY_LISTENER.clear();
      }

      closeSocketsQuietly(sockets);
   }

   public record ServerEndpoint(String host, int port) {
      public ServerEndpoint(String host, int port) {
         Objects.requireNonNull(host, "host");
         if (port >= 0 && port <= 65535) {
            this.host = host;
            this.port = port;
         } else {
            throw new IllegalArgumentException("Invalid server port: " + port);
         }
      }
   }

   public enum PrivacyMode {
      VANILLA,
      MODDED;
   }

   public record ProxyEndpoint(ProxyProtocolType type, String host, int port, String username, String password) {
      public ProxyEndpoint(ProxyProtocolType type, String host, int port, String username, String password) {
         Objects.requireNonNull(type, "type");
         if (host != null && !host.isBlank() && port >= 1 && port <= 65535) {
            username = username == null ? "" : username;
            password = password == null ? "" : password;
            this.type = type;
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
         } else {
            throw new IllegalArgumentException("Invalid connection proxy endpoint");
         }
      }

      @Override
      public String toString() {
         return "Connection proxy route";
      }
   }

   public record AccountIdentity(UUID profileId, String username) {
      public AccountIdentity {
         if (profileId == null && username == null) {
            throw new IllegalArgumentException("An account name or profile id is required");
         }
      }
   }

   public static final class ConnectionSession {
      private final long lifecycleId;
      private final ConnectionPrivacyRegistry.PrivacyMode privacyMode;
      private final boolean multiConnection;
      private final Connection connection;
      private final SocketAddress peerAddress;
      private final Set<Socket> activeSockets = Collections.newSetFromMap(new IdentityHashMap<>());
      private volatile ConnectionPrivacyRegistry.ServerEndpoint serverEndpoint;
      private volatile ConnectionPrivacyRegistry.AccountIdentity accountIdentity;
      private volatile PacketListener packetListener;
      private volatile Type serverType;
      private volatile ServerPackManager serverPackManager;
      private volatile ConnectionPrivacyRegistry.ProxyEndpoint proxyEndpoint;
      private boolean proxyBound;
      private volatile long downloadGeneration;
      private volatile boolean active = true;
      private volatile boolean localAddress;

      private ConnectionSession(long lifecycleId, ConnectionPrivacyRegistry.PrivacyMode mode, boolean multi, Connection connection, SocketAddress peerAddress) {
         this.lifecycleId = lifecycleId;
         this.privacyMode = mode;
         this.multiConnection = multi;
         this.connection = connection;
         this.peerAddress = peerAddress;
         this.localAddress = peerAddress instanceof LocalAddress;
      }

      public long lifecycleId() {
         return this.lifecycleId;
      }

      public ConnectionPrivacyRegistry.PrivacyMode privacyMode() {
         return this.isLocalOrMemory() ? ConnectionPrivacyRegistry.PrivacyMode.MODDED : this.privacyMode;
      }

      public boolean isMultiConnection() {
         return this.multiConnection;
      }

      public Connection connection() {
         return this.connection;
      }

      public SocketAddress peerAddress() {
         return this.peerAddress == null && this.isLocalOrMemory() ? this.connection.getRemoteAddress() : this.peerAddress;
      }

      public ConnectionPrivacyRegistry.ServerEndpoint serverEndpoint() {
         return this.serverEndpoint;
      }

      public ConnectionPrivacyRegistry.AccountIdentity accountIdentity() {
         return this.accountIdentity;
      }

      public PacketListener packetListener() {
         return this.packetListener;
      }

      public Type serverType() {
         return this.serverType == null && this.isLocalOrMemory() ? Type.OTHER : this.serverType;
      }

      public ServerPackManager serverPackManager() {
         return this.serverPackManager;
      }

      public ConnectionPrivacyRegistry.ProxyEndpoint proxyEndpoint() {
         return this.proxyEndpoint;
      }

      public long downloadGeneration() {
         return this.downloadGeneration;
      }

      public boolean isActive() {
         return this.active;
      }

      public boolean isLocalOrMemory() {
         return this.localAddress || this.connection.isMemoryConnection();
      }
   }
}
