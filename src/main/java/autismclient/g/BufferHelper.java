package autismclient.g;

import autismclient.util.bZ;
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

public final class BufferHelper {
   static final int init_I = 1024;
   private static final Object init_Obj = new Object();
   private static final Map<Connection, BufferHelper.Inner_StreamUtil> init_Map = new IdentityHashMap<>();
   private static final Map<PacketListener, BufferHelper.Inner_StreamUtil> load = new IdentityHashMap<>();
   private static final AtomicLong init_AtomicLong = new AtomicLong();

   private BufferHelper() {
   }

   public static BufferHelper.Inner_StreamUtil init(Connection connection, BufferHelper.Inner_CachePool mode, boolean multi, SocketAddress peerAddress) {
      Objects.requireNonNull(connection, "connection");
      Objects.requireNonNull(mode, "mode");
      if (peerAddress instanceof LocalAddress || connection.isMemoryConnection() || connection.getReceiving() == PacketFlow.SERVERBOUND) {
         mode = BufferHelper.Inner_CachePool.load;
      }

      List<Socket> retired = List.of();
      BufferHelper.Inner_StreamUtil created;
      synchronized (init_Obj) {
         BufferHelper.Inner_StreamUtil existing = init_Map.get(connection);
         if (existing != null) {
            if (existing.read) {
               if (peerAddress instanceof LocalAddress) {
                  existing.write = true;
               }

               return existing;
            }

            retired = load_List(existing);
         }

         if (init_Map.size() >= 1024) {
            throw new IllegalStateException("Connection privacy registry is full (1024)");
         }

         created = new BufferHelper.Inner_StreamUtil(init_AtomicLong.incrementAndGet(), mode, multi, connection, peerAddress);
         if (created.read()) {
            created.init_ServerData_Type = Type.OTHER;
         }

         init_Map.put(connection, created);
      }

      init(retired);
      return created;
   }

   public static BufferHelper.Inner_StreamUtil init_BufferHelper_Strea(Connection connection) {
      if (connection == null) {
         return null;
      }

      synchronized (init_Obj) {
         BufferHelper.Inner_StreamUtil context = init_Map.get(connection);
         return context != null && context.read ? context : null;
      }
   }

   public static BufferHelper.Inner_StreamUtil init(PacketListener listener) {
      if (listener == null) {
         return null;
      }

      synchronized (init_Obj) {
         BufferHelper.Inner_StreamUtil context = load.get(listener);
         return context != null && context.read ? context : null;
      }
   }

   public static BufferHelper.Inner_StreamUtil init(ServerPackManager manager) {
      if (manager == null) {
         return null;
      }

      synchronized (init_Obj) {
         BufferHelper.Inner_StreamUtil found = null;

         for (BufferHelper.Inner_StreamUtil context : init_Map.values()) {
            if (context.read && context.init_ServerPackManager == manager) {
               if (found != null) {
                  return null;
               }

               found = context;
            }
         }

         return found;
      }
   }

   public static BufferHelper.Inner_StreamUtil load(Connection connection) {
      synchronized (init_Obj) {
         return read(connection);
      }
   }

   public static boolean init_Z(BufferHelper.Inner_StreamUtil context) {
      if (context != null && context.read) {
         synchronized (init_Obj) {
            return context.read && init_Map.get(context.init_Connection) == context;
         }
      } else {
         return false;
      }
   }

   public static boolean init(
      BufferHelper.Inner_StreamUtil context, long lifecycleId, ServerPackManager manager, PacketListener listener, long downloadGeneration
   ) {
      if (context != null && context.read) {
         synchronized (init_Obj) {
            return context.read
               && context.init_J == lifecycleId
               && context.load_J == downloadGeneration
               && context.init_ServerPackManager == manager
               && context.init_PacketListener == listener
               && init_Map.get(context.init_Connection) == context;
         }
      } else {
         return false;
      }
   }

   public static BufferHelper.Inner_StreamUtil init(Connection connection, String host, int port, Type serverKind, ServerPackManager manager) {
      BufferHelper.Inner_StreamUtil context = init(connection, host, port);
      init(connection, serverKind);
      init(connection, manager);
      return context;
   }

   public static BufferHelper.Inner_StreamUtil init(Connection connection, String host, int port) {
      BufferHelper.Inner_BufferHelper endpoint = new BufferHelper.Inner_BufferHelper(host, port);
      synchronized (init_Obj) {
         BufferHelper.Inner_StreamUtil context = read(connection);
         BufferHelper.Inner_BufferHelper existing = context.init_BufferHelper_Buffe;
         if (existing == null) {
            context.init_BufferHelper_Buffe = endpoint;
         } else if (!existing.equals(endpoint)) {
            throw new IllegalStateException("Original endpoint is already bound for this connection lifecycle");
         }

         return context;
      }
   }

   public static BufferHelper.Inner_StreamUtil init(Connection connection, Type serverKind) {
      Objects.requireNonNull(serverKind, "serverKind");
      List<Socket> retired = List.of();
      boolean conflict = false;
      BufferHelper.Inner_StreamUtil context;
      synchronized (init_Obj) {
         context = read(connection);
         Type existing = context.init_ServerData_Type;
         if (existing == null) {
            context.init_ServerData_Type = serverKind;
         } else if (existing != serverKind) {
            retired = init_List(context);
            conflict = true;
         }
      }

      if (conflict) {
         init(retired);
         throw new IllegalStateException("Server kind is already bound for this connection lifecycle");
      } else {
         return context;
      }
   }

   public static BufferHelper.Inner_StreamUtil init(Connection connection, ServerPackManager manager) {
      Objects.requireNonNull(manager, "manager");
      List<Socket> retired = List.of();
      boolean conflict = false;
      BufferHelper.Inner_StreamUtil context;
      synchronized (init_Obj) {
         context = read(connection);
         ServerPackManager existing = context.init_ServerPackManager;
         BufferHelper.Inner_StreamUtil previousOwner = null;

         for (BufferHelper.Inner_StreamUtil candidate : init_Map.values()) {
            if (candidate != context && candidate.read && candidate.init_ServerPackManager == manager) {
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
            retired = init_List(context);
            conflict = true;
         } else {
            if (previousOwner != null) {
               retired = init_List(previousOwner);
               previousOwner.init_ServerPackManager = null;
            }

            context.init_ServerPackManager = manager;
         }
      }

      if (conflict) {
         init(retired);
         throw new IllegalStateException("Pack manager is already bound for this connection lifecycle");
      } else {
         init(retired);
         return context;
      }
   }

   public static BufferHelper.Inner_StreamUtil init(Connection connection, UUID profileId, String username) {
      BufferHelper.Inner_DataUtil account = new BufferHelper.Inner_DataUtil(profileId, username);
      synchronized (init_Obj) {
         BufferHelper.Inner_StreamUtil context = read(connection);
         BufferHelper.Inner_DataUtil existing = context.init_BufferHelper_DataU;
         if (existing == null) {
            context.init_BufferHelper_DataU = account;
         } else if (!existing.equals(account)) {
            throw new IllegalStateException("Account is already bound for this connection lifecycle");
         }

         return context;
      }
   }

   public static void init(Connection connection, BufferHelper.Inner_ConfigStore route) {
      List<Socket> retired = List.of();
      boolean conflict = false;
      synchronized (init_Obj) {
         BufferHelper.Inner_StreamUtil context = read(connection);
         if (!context.load_Z) {
            context.init_BufferHelper_Confi = route;
            context.load_Z = true;
            retired = init_List(context);
         } else if (!Objects.equals(context.init_BufferHelper_Confi, route)) {
            retired = init_List(context);
            conflict = true;
         }
      }

      init(retired);
      if (conflict) {
         throw new IllegalStateException("Proxy is already bound for this connection lifecycle");
      }
   }

   public static BufferHelper.Inner_StreamUtil init(Connection connection, PacketListener listener) {
      Objects.requireNonNull(listener, "listener");
      BufferHelper.Inner_StreamUtil context;
      List<Socket> retired;
      synchronized (init_Obj) {
         context = read(connection);
         PacketListener previous = context.init_PacketListener;
         if (previous == listener) {
            return context;
         }

         BufferHelper.Inner_StreamUtil owner = load.get(listener);
         if (owner != null && owner != context) {
            throw new IllegalStateException("Packet listener is already bound to another connection lifecycle");
         }

         retired = init_List(context);
         if (previous != null) {
            load.remove(previous, context);
         }

         load.put(listener, context);
         context.init_PacketListener = listener;
      }

      init(retired);
      return context;
   }

   public static void init(BufferHelper.Inner_StreamUtil context, long downloadGeneration, Socket socket) throws IOException {
      Objects.requireNonNull(socket, "socket");
      synchronized (init_Obj) {
         if (!init(context, context.init_J(), context.init_ServerPackManager, context.init_PacketListener, downloadGeneration)) {
            throw new IOException("The resource-pack connection is no longer current");
         }

         context.init_Set.add(socket);
      }
   }

   public static void init(BufferHelper.Inner_StreamUtil context, Socket socket) {
      if (context != null && socket != null) {
         synchronized (init_Obj) {
            context.init_Set.remove(socket);
         }
      }
   }

   public static boolean load_Z(BufferHelper.Inner_StreamUtil context) {
      if (context == null) {
         return false;
      }

      List<Socket> retired;
      synchronized (init_Obj) {
         if (!context.read) {
            return false;
         }

         retired = init_List(context);
      }

      init(retired);
      return true;
   }

   public static boolean init_Z(Connection connection) {
      if (connection == null) {
         return false;
      }

      List<Socket> retired;
      synchronized (init_Obj) {
         BufferHelper.Inner_StreamUtil context = init_Map.get(connection);
         if (context == null) {
            return false;
         }

         retired = load_List(context);
      }

      init(retired);
      return true;
   }

   public static boolean read_Z(BufferHelper.Inner_StreamUtil context) {
      if (context == null) {
         return false;
      }

      List<Socket> retired;
      synchronized (init_Obj) {
         if (init_Map.get(context.init_Connection) != context) {
            return false;
         }

         retired = load_List(context);
      }

      init(retired);
      return true;
   }

   private static BufferHelper.Inner_StreamUtil read(Connection connection) {
      Objects.requireNonNull(connection, "connection");
      BufferHelper.Inner_StreamUtil context = init_Map.get(connection);
      if (context != null && context.read) {
         return context;
      } else {
         throw new IllegalStateException("No active privacy context for this connection");
      }
   }

   private static List<Socket> init_List(BufferHelper.Inner_StreamUtil context) {
      context.load_J++;
      return read_List(context);
   }

   private static List<Socket> load_List(BufferHelper.Inner_StreamUtil context) {
      init_Map.remove(context.init_Connection, context);
      PacketListener listener = context.init_PacketListener;
      if (listener != null) {
         load.remove(listener, context);
         context.init_PacketListener = null;
      }

      context.read = false;
      context.load_J++;
      return read_List(context);
   }

   private static List<Socket> read_List(BufferHelper.Inner_StreamUtil context) {
      if (context.init_Set.isEmpty()) {
         return List.of();
      }

      List<Socket> sockets = new ArrayList<>(context.init_Set);
      context.init_Set.clear();
      return sockets;
   }

   private static void init(List<Socket> sockets) {
      for (Socket socket : sockets) {
         try {
            socket.close();
         } catch (IOException var4) {
         }
      }
   }

   public static void init() {
      List<Socket> sockets = new ArrayList<>();
      synchronized (init_Obj) {
         for (BufferHelper.Inner_StreamUtil context : init_Map.values()) {
            context.init_PacketListener = null;
            context.read = false;
            context.load_J++;
            sockets.addAll(read_List(context));
         }

         init_Map.clear();
         load.clear();
      }

      init(sockets);
   }

   public record Inner_BufferHelper(String init_Str, int init_I) {
      public Inner_BufferHelper(String init_Str, int init_I) {
         Objects.requireNonNull(init_Str, "host");
         if (init_I >= 0 && init_I <= 65535) {
            this.init_Str = init_Str;
            this.init_I = init_I;
         } else {
            throw new IllegalArgumentException("Invalid server port: " + init_I);
         }
      }
   }

   public enum Inner_CachePool {
      init,
      load;
   }

   public record Inner_ConfigStore(bZ init_bZ, String init_Str, int init_I, String load, String read) {
      public Inner_ConfigStore(bZ init_bZ, String init_Str, int init_I, String load, String read) {
         Objects.requireNonNull(init_bZ, "type");
         if (init_Str != null && !init_Str.isBlank() && init_I >= 1 && init_I <= 65535) {
            load = load == null ? "" : load;
            read = read == null ? "" : read;
            this.init_bZ = init_bZ;
            this.init_Str = init_Str;
            this.init_I = init_I;
            this.load = load;
            this.read = read;
         } else {
            throw new IllegalArgumentException("Invalid connection proxy endpoint");
         }
      }

      @Override
      public String toString() {
         return "Connection proxy route";
      }
   }

   public record Inner_DataUtil(UUID init_UUID, String init_Str) {
      public Inner_DataUtil {
         if (init_UUID == null && init_Str == null) {
            throw new IllegalArgumentException("An account name or profile id is required");
         }
      }
   }

   public static final class Inner_StreamUtil {
      private final long init_J;
      private final BufferHelper.Inner_CachePool init_BufferHelper_Cache;
      private final boolean init_Z;
      private final Connection init_Connection;
      private final SocketAddress init_SocketAddress;
      private final Set<Socket> init_Set = Collections.newSetFromMap(new IdentityHashMap<>());
      private volatile BufferHelper.Inner_BufferHelper init_BufferHelper_Buffe;
      private volatile BufferHelper.Inner_DataUtil init_BufferHelper_DataU;
      private volatile PacketListener init_PacketListener;
      private volatile Type init_ServerData_Type;
      private volatile ServerPackManager init_ServerPackManager;
      private volatile BufferHelper.Inner_ConfigStore init_BufferHelper_Confi;
      private boolean load_Z;
      private volatile long load_J;
      private volatile boolean read = true;
      private volatile boolean write;

      private Inner_StreamUtil(long lifecycleId, BufferHelper.Inner_CachePool mode, boolean multi, Connection connection, SocketAddress peerAddress) {
         this.init_J = lifecycleId;
         this.init_BufferHelper_Cache = mode;
         this.init_Z = multi;
         this.init_Connection = connection;
         this.init_SocketAddress = peerAddress;
         this.write = peerAddress instanceof LocalAddress;
      }

      public long init_J() {
         return this.init_J;
      }

      public BufferHelper.Inner_CachePool init_BufferHelper_Cache() {
         return this.read() ? BufferHelper.Inner_CachePool.load : this.init_BufferHelper_Cache;
      }

      public boolean init_Z() {
         return this.init_Z;
      }

      public Connection init_Connection() {
         return this.init_Connection;
      }

      public SocketAddress init_SocketAddress() {
         return this.init_SocketAddress == null && this.read() ? this.init_Connection.getRemoteAddress() : this.init_SocketAddress;
      }

      public BufferHelper.Inner_BufferHelper init_BufferHelper_Buffe() {
         return this.init_BufferHelper_Buffe;
      }

      public BufferHelper.Inner_DataUtil init_BufferHelper_DataU() {
         return this.init_BufferHelper_DataU;
      }

      public PacketListener init_PacketListener() {
         return this.init_PacketListener;
      }

      public Type init_ServerData_Type() {
         return this.init_ServerData_Type == null && this.read() ? Type.OTHER : this.init_ServerData_Type;
      }

      public ServerPackManager init_ServerPackManager() {
         return this.init_ServerPackManager;
      }

      public BufferHelper.Inner_ConfigStore init_BufferHelper_Confi() {
         return this.init_BufferHelper_Confi;
      }

      public long load_J() {
         return this.load_J;
      }

      public boolean load_Z() {
         return this.read;
      }

      public boolean read() {
         return this.write || this.init_Connection.isMemoryConnection();
      }
   }
}
