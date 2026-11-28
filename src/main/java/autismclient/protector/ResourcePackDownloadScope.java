package autismclient.protector;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.Proxy.Type;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.server.ServerPackManager;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.server.packs.DownloadQueue.BatchConfig;

public final class ResourcePackDownloadScope {
   private static final int init_I = 256;
   private static final int load_I = 256;
   private static final ThreadLocal<ResourcePackDownloadScope.Inner_BufferHelper> CURRENT = new ThreadLocal<>();
   private static final ThreadLocal<ResourcePackDownloadScope.Inner_BufferHelper> load_ThreadLocal = new ThreadLocal<>();
   private static final Object LOCK = new Object();
   private static final Map<BatchConfig, ResourcePackDownloadScope.Inner_StreamUtil> tokensByConfig = new IdentityHashMap<>();
   private static final Map<ConnectionPrivacyRegistry.ConnectionSession, UUID> connectionIds = new WeakHashMap<>();
   private static final ReentrantLock[] STRIPED_LOCKS = new ReentrantLock[256];
   private static boolean multiLoopbackFixtureEnabled;
   private static final ThreadLocal<ResourcePackDownloadScope.Inner_ConfigStore> read;

   private ResourcePackDownloadScope() {
   }

   public static ResourcePackDownloadScope.Inner_BufferHelper snapshot(ServerPackManager manager) throws IOException {
      Objects.requireNonNull(manager, "manager");
      ConnectionPrivacyRegistry.ConnectionSession context = ConnectionPrivacyRegistry.getByPackManager(manager);
      if (context != null
         && context.accountIdentity() != null
         && context.accountIdentity().profileId() != null
         && context.peerAddress() != null
         && context.packetListener() != null
         && context.serverType() != null) {
         String peer = PackUrlValidator.peerKey(context.peerAddress());
         Connection connection = context.connection();
         Minecraft minecraft = Minecraft.getInstance();
         boolean integrated = connection.isMemoryConnection()
            && minecraft != null
            && minecraft.hasSingleplayerServer()
            && minecraft.getConnection() == context.packetListener();
         if (!integrated && context.serverEndpoint() == null) {
            throw new IOException("Resource-pack download has no original remote endpoint");
         }

         String origin = init(context, integrated);
         boolean multiLoopbackFixture = context.isMultiConnection() && multiLoopbackFixtureEnabled;
         UUID connectionIdentity;
         synchronized (LOCK) {
            connectionIdentity = connectionIds.computeIfAbsent(context, ignored -> UUID.randomUUID());
         }

         ResourcePackDownloadScope.Inner_BufferHelper snapshot = new ResourcePackDownloadScope.Inner_BufferHelper(
            context, manager, context.accountIdentity().profileId(), origin, peer, connectionIdentity, integrated, multiLoopbackFixture
         );
         snapshot.ensureCurrent();
         return snapshot;
      } else {
         throw new IOException("Resource-pack download has no complete connection scope");
      }
   }

   static String init(ConnectionPrivacyRegistry.ConnectionSession context, boolean integrated) throws IOException {
      return integrated && context.isLocalOrMemory() ? "integrated:" + context.lifecycleId() : PackUrlValidator.endpointKey(context.serverEndpoint());
   }

   public static ResourcePackDownloadScope.Inner_CachePool enter(ResourcePackDownloadScope.Inner_BufferHelper snapshot) throws IOException {
      Objects.requireNonNull(snapshot, "snapshot");
      snapshot.ensureCurrent();
      ResourcePackDownloadScope.Inner_BufferHelper previous = CURRENT.get();
      CURRENT.set(snapshot);
      return () -> init(CURRENT, previous);
   }

   public static ResourcePackDownloadScope.Inner_CachePool load(ResourcePackDownloadScope.Inner_BufferHelper snapshot) throws IOException {
      Objects.requireNonNull(snapshot, "snapshot");
      snapshot.ensureCurrent();
      ResourcePackDownloadScope.Inner_BufferHelper previous = load_ThreadLocal.get();
      if (previous == snapshot) {
         return () -> {};
      }

      load_ThreadLocal.set(snapshot);
      return () -> init(load_ThreadLocal, previous);
   }

   public static ResourcePackDownloadScope.Inner_CachePool init(ResourcePackDownloadScope.Inner_BufferHelper snapshot, String host, int port, String username, String password) throws IOException {
      Objects.requireNonNull(snapshot, "snapshot");
      snapshot.ensureCurrent();
      PackUrlValidator.ParsedTarget target = init(host, port);
      byte[] usernameBytes = init(username);

      byte[] passwordBytes;
      try {
         passwordBytes = init(password);
      } catch (IOException | RuntimeException e) {
         Arrays.fill(usernameBytes, (byte)0);
         throw e;
      }

      ResourcePackDownloadScope.Inner_ConfigStore authentication = new ResourcePackDownloadScope.Inner_ConfigStore(snapshot, target.load(), target.port(), usernameBytes, passwordBytes);
      ResourcePackDownloadScope.Inner_ConfigStore previous = read.get();

      try {
         read.set(authentication);
      } catch (RuntimeException e) {
         authentication.wipe();
         throw e;
      }

      return () -> {
         if (authentication.wipe()) {
            init(read, previous);
         }
      };
   }

   static ResourcePackDownloadScope.Inner_ConfigStore init(ResourcePackDownloadScope.Inner_BufferHelper snapshot, PackUrlValidator.PackRoute route) {
      if (snapshot != null && route != null && route.proxyType() == Type.SOCKS && route.init_Z()) {
         ResourcePackDownloadScope.Inner_ConfigStore authentication = read.get();
         return authentication != null
               && authentication.active
               && authentication.snapshot == snapshot
               && authentication.port == route.endpoint().port()
               && authentication.host.equalsIgnoreCase(route.endpoint().load())
            ? authentication
            : null;
      } else {
         return null;
      }
   }

   private static PackUrlValidator.ParsedTarget init(String host, int port) throws IOException {
      if (host != null && !host.isBlank() && port >= 1 && port <= 65535) {
         String literal = host.indexOf(58) >= 0 && !host.startsWith("[") ? "[" + host + "]" : host;

         try {
            return PackUrlValidator.parseUri(new URI("http://" + literal + ":" + port + "/"));
         } catch (URISyntaxException e) {
            throw new IOException("Invalid configured SOCKS5 proxy endpoint", e);
         }
      } else {
         throw new IOException("Invalid configured SOCKS5 proxy endpoint");
      }
   }

   private static byte[] init(String value) throws IOException {
      if (value != null && !value.isEmpty() && StandardCharsets.ISO_8859_1.newEncoder().canEncode(value)) {
         byte[] encoded = value.getBytes(StandardCharsets.ISO_8859_1);
         if (encoded.length > 255) {
            Arrays.fill(encoded, (byte)0);
            throw new IOException("SOCKS5 credentials must be nonempty ISO-8859-1 values of at most 255 bytes");
         } else {
            return encoded;
         }
      } else {
         throw new IOException("SOCKS5 credentials must be nonempty ISO-8859-1 values of at most 255 bytes");
      }
   }

   private static <T> void init(ThreadLocal<T> slot, T previous) {
      if (previous == null) {
         slot.remove();
      } else {
         slot.set(previous);
      }
   }

   public static ResourcePackDownloadScope.Inner_BufferHelper current() {
      return CURRENT.get();
   }

   public static ResourcePackDownloadScope.Inner_BufferHelper load() {
      return load_ThreadLocal.get();
   }

   public static ResourcePackDownloadScope.Inner_BufferHelper init(BatchConfig config) {
      synchronized (LOCK) {
         ResourcePackDownloadScope.Inner_StreamUtil token = tokensByConfig.get(config);
         return token != null && token.active ? token.snapshot : null;
      }
   }

   public static ResourcePackDownloadScope.Inner_BufferHelper read() throws IOException {
      ResourcePackDownloadScope.Inner_BufferHelper snapshot = CURRENT.get();
      if (snapshot == null) {
         throw new IOException("Unscoped resource-pack batch creation is refused");
      }

      snapshot.ensureCurrent();
      return snapshot;
   }

   public static ResourcePackDownloadScope.Inner_BufferHelper write() throws IOException {
      ResourcePackDownloadScope.Inner_BufferHelper snapshot = load_ThreadLocal.get();
      if (snapshot == null) {
         throw new IOException("Unscoped resource-pack network access is refused");
      }

      snapshot.ensureCurrent();
      return snapshot;
   }

   public static ResourcePackDownloadScope.Inner_BufferHelper load(BatchConfig config) throws IOException {
      synchronized (LOCK) {
         ResourcePackDownloadScope.Inner_StreamUtil token = tokensByConfig.get(config);
         if (token != null && token.active) {
            token.snapshot.ensureCurrent();
            return token.snapshot;
         } else {
            throw new IOException("Unregistered resource-pack batch");
         }
      }
   }

   public static ResourcePackDownloadScope.Inner_DataUtil init(BatchConfig config, ResourcePackDownloadScope.Inner_BufferHelper snapshot) throws IOException {
      Objects.requireNonNull(config, "config");
      Objects.requireNonNull(snapshot, "snapshot");
      snapshot.ensureCurrent();
      ResourcePackDownloadScope.Inner_StreamUtil token;
      synchronized (LOCK) {
         if (tokensByConfig.containsKey(config)) {
            throw new IOException("Resource-pack batch is already registered");
         }

         if (tokensByConfig.size() >= 256) {
            throw new IOException("Too many active resource-pack batches");
         }

         token = new ResourcePackDownloadScope.Inner_StreamUtil(config, snapshot);
         tokensByConfig.put(config, token);
      }

      return new ResourcePackDownloadScope.Inner_DataUtil(token);
   }

   public static <T> Consumer<T> init(ResourcePackDownloadScope.Inner_BufferHelper snapshot, Consumer<T> output) {
      Objects.requireNonNull(output, "output");
      return value -> {
         if (snapshot.isCurrent()) {
            output.accept(value);
         }
      };
   }

   public static void retire(ResourcePackDownloadScope.Inner_BufferHelper snapshot) {
      if (snapshot != null) {
         snapshot.retireSockets();
      }
   }

   public static void init_V(ServerPackManager manager) {
      if (manager != null) {
         List<ResourcePackDownloadScope.Inner_BufferHelper> snapshots = new ArrayList<>();
         synchronized (LOCK) {
            for (ResourcePackDownloadScope.Inner_StreamUtil token : tokensByConfig.values()) {
               if (token.active && token.snapshot.manager == manager) {
                  snapshots.add(token.snapshot);
               }
            }
         }

         for (ResourcePackDownloadScope.Inner_BufferHelper snapshot : snapshots) {
            snapshot.retireSockets();
         }

         ConnectionPrivacyRegistry.ConnectionSession context = ConnectionPrivacyRegistry.getByPackManager(manager);
         if (context != null) {
            ConnectionPrivacyRegistry.retireDownloadSockets(context);
         }
      }
   }

   public static void init(ResourcePackDownloadScope.Inner_BufferHelper snapshot, Socket socket) throws IOException {
      snapshot.init(socket);
   }

   public static void load(ResourcePackDownloadScope.Inner_BufferHelper snapshot, Socket socket) {
      snapshot.load(socket);
   }

   public static Path init(Path cacheDir, ResourcePackDownloadScope.Inner_BufferHelper snapshot, UUID packId, HashFunction hashFunction) throws IOException {
      Objects.requireNonNull(cacheDir, "cacheDir");
      Objects.requireNonNull(snapshot, "snapshot");
      Objects.requireNonNull(packId, "packId");
      Objects.requireNonNull(hashFunction, "hashFunction");
      snapshot.ensureCurrent();
      String scopeHash = init(snapshot.origin(), snapshot.canonicalPeer());
      String mode = snapshot.privacyMode() == ConnectionPrivacyRegistry.PrivacyMode.VANILLA ? "vanilla-neutral-v1" : "modded-full-v1";
      ResourcePackSessionTracker.PackSession session = ResourcePackSessionTracker.sessionFor(snapshot.context());
      ResourcePackSessionTracker.PackPolicy policy = ResourcePackSessionTracker.policyFor(session, packId);
      String protection = policy == ResourcePackSessionTracker.PackPolicy.ISOLATED ? "isolated-v1" : "full-v1";
      Path base = cacheDir.normalize();
      Path result = base.resolve("autism-v2")
         .resolve(snapshot.accountId().toString())
         .resolve(scopeHash)
         .resolve(snapshot.connectionId.toString())
         .resolve(mode)
         .resolve(protection)
         .resolve(init(hashFunction))
         .resolve(packId.toString())
         .normalize();
      if (result.startsWith(base) && !result.equals(base)) {
         return result;
      } else {
         throw new IOException("Resource-pack cache path escaped its root");
      }
   }

   public static Path init(Path cacheDir, Path candidate) throws IOException {
      Path base = cacheDir.normalize();
      Path normalized = candidate.normalize();
      if (normalized.startsWith(base) && !normalized.equals(base)) {
         return normalized;
      } else {
         throw new IOException("Resource-pack cache path escaped its root");
      }
   }

   public static ReentrantLock init(Path directory) {
      String key = directory.toAbsolutePath().normalize().toString();
      int stripe = (key.hashCode() & 2147483647) % 256;
      return STRIPED_LOCKS[stripe];
   }

   private static String init(String origin, String peer) {
      byte[] originBytes = origin.getBytes(StandardCharsets.UTF_8);
      byte[] peerBytes = peer.getBytes(StandardCharsets.UTF_8);
      ByteBuffer input = ByteBuffer.allocate(8 + originBytes.length + peerBytes.length);
      input.putInt(originBytes.length).put(originBytes).putInt(peerBytes.length).put(peerBytes);
      return Hashing.sha256().hashBytes(input.array()).toString();
   }

   private static String init(HashFunction hashFunction) {
      String var10000;
      switch (hashFunction.bits()) {
         case 160:
            var10000 = "sha1";
            break;
         case 256:
            var10000 = "sha256";
            break;
         case 512:
            var10000 = "sha512";
            break;
         default:
            var10000 = "hash-" + hashFunction.bits() + "-" + Hashing.sha256().hashUnencodedChars(hashFunction.toString()).toString().substring(0, 16);
      }

      return var10000;
   }

   static void init(boolean enabled) {
      multiLoopbackFixtureEnabled = enabled;
   }

   static void reset() {
      multiLoopbackFixtureEnabled = false;
      CURRENT.remove();
      load_ThreadLocal.remove();
      ResourcePackDownloadScope.Inner_ConfigStore authentication = read.get();
      if (authentication != null) {
         authentication.wipe();
      }

      read.remove();
      synchronized (LOCK) {
         tokensByConfig.clear();
         connectionIds.clear();
      }
   }

   static {
      for (int i = 0; i < STRIPED_LOCKS.length; i++) {
         STRIPED_LOCKS[i] = new ReentrantLock();
      }

      read = new ThreadLocal<>();
   }

   public static final class Inner_BufferHelper {
      private final ConnectionPrivacyRegistry.ConnectionSession context;
      private final long lifecycleId;
      private final ServerPackManager manager;
      private final PacketListener packetListener;
      private final long downloadGeneration;
      private final UUID accountId;
      private final String origin;
      private final SocketAddress peerAddress;
      private final String canonicalPeer;
      private final UUID connectionId;
      private final ConnectionPrivacyRegistry.PrivacyMode privacyMode;
      private final net.minecraft.client.multiplayer.ServerData.Type serverType;
      private final boolean integrated;
      private final boolean multiLoopbackFixture;
      private final ConnectionPrivacyRegistry.ProxyEndpoint proxyEndpoint;
      private final Set<Socket> sockets = Collections.newSetFromMap(new IdentityHashMap<>());
      private volatile boolean read;

      private Inner_BufferHelper(
         ConnectionPrivacyRegistry.ConnectionSession context,
         ServerPackManager manager,
         UUID accountId,
         String origin,
         String canonicalPeer,
         UUID connectionIdentity,
         boolean integrated,
         boolean multiLoopbackFixture
      ) {
         this.context = context;
         this.lifecycleId = context.lifecycleId();
         this.manager = manager;
         this.packetListener = context.packetListener();
         this.downloadGeneration = context.downloadGeneration();
         this.accountId = accountId;
         this.origin = origin;
         this.peerAddress = context.peerAddress();
         this.canonicalPeer = canonicalPeer;
         this.connectionId = connectionIdentity;
         this.privacyMode = context.privacyMode();
         this.serverType = context.serverType();
         this.integrated = integrated;
         this.multiLoopbackFixture = multiLoopbackFixture;
         this.proxyEndpoint = context.proxyEndpoint();
      }

      public ConnectionPrivacyRegistry.ConnectionSession context() {
         return this.context;
      }

      public long lifecycleId() {
         return this.lifecycleId;
      }

      public ServerPackManager manager() {
         return this.manager;
      }

      public PacketListener packetListener() {
         return this.packetListener;
      }

      public long downloadGeneration() {
         return this.downloadGeneration;
      }

      public UUID accountId() {
         return this.accountId;
      }

      public String origin() {
         return this.origin;
      }

      public SocketAddress peerAddress() {
         return this.peerAddress;
      }

      public String canonicalPeer() {
         return this.canonicalPeer;
      }

      public ConnectionPrivacyRegistry.PrivacyMode privacyMode() {
         return this.privacyMode;
      }

      public ConnectionPrivacyRegistry.ProxyEndpoint proxyEndpoint() {
         return this.proxyEndpoint;
      }

      public net.minecraft.client.multiplayer.ServerData.Type serverType() {
         return this.serverType;
      }

      public boolean init_Z() {
         return this.integrated || this.multiLoopbackFixture;
      }

      public boolean isCurrent() {
         if (this.read || !ConnectionPrivacyRegistry.isCurrentDownload(this.context, this.lifecycleId, this.manager, this.packetListener, this.downloadGeneration)) {
            return false;
         }

         if (!this.integrated) {
            return true;
         }

         Minecraft minecraft = Minecraft.getInstance();
         return minecraft != null
            && minecraft.hasSingleplayerServer()
            && this.context.connection().isMemoryConnection()
            && minecraft.getConnection() == this.packetListener;
      }

      public void ensureCurrent() throws IOException {
         if (!this.isCurrent()) {
            throw new IOException("The resource-pack download is no longer current");
         }
      }

      private void init(Socket socket) throws IOException {
         Objects.requireNonNull(socket, "socket");
         boolean close = false;
         synchronized (this.sockets) {
            if (!this.read && this.isCurrent()) {
               ConnectionPrivacyRegistry.trackDownloadSocket(this.context, this.downloadGeneration, socket);
               if (!this.read && this.isCurrent()) {
                  this.sockets.add(socket);
               } else {
                  ConnectionPrivacyRegistry.untrackDownloadSocket(this.context, socket);
                  close = true;
               }
            } else {
               close = true;
            }
         }

         if (close) {
            try {
               socket.close();
            } finally {
               throw new IOException("The resource-pack connection is no longer current");
            }
         }
      }

      private void load(Socket socket) {
         if (socket != null) {
            synchronized (this.sockets) {
               this.sockets.remove(socket);
            }

            ConnectionPrivacyRegistry.untrackDownloadSocket(this.context, socket);
         }
      }

      private void retireSockets() {
         List<Socket> retired;
         synchronized (this.sockets) {
            if (this.read) {
               return;
            }

            this.read = true;
            retired = new ArrayList<>(this.sockets);
            this.sockets.clear();
         }

         for (Socket socket : retired) {
            ConnectionPrivacyRegistry.untrackDownloadSocket(this.context, socket);

            try {
               socket.close();
            } catch (IOException var5) {
            }
         }
      }
   }

   public interface Inner_CachePool extends AutoCloseable {
      @Override
      void close();
   }

   static final class Inner_ConfigStore {
      private final ResourcePackDownloadScope.Inner_BufferHelper snapshot;
      private final String host;
      private final int port;
      private final byte[] username;
      private final byte[] load;
      private volatile boolean active = true;

      private Inner_ConfigStore(ResourcePackDownloadScope.Inner_BufferHelper snapshot, String host, int port, byte[] username, byte[] password) {
         this.snapshot = snapshot;
         this.host = host;
         this.port = port;
         this.username = username;
         this.load = password;
      }

      byte[] usernameBytes() {
         return Arrays.copyOf(this.username, this.username.length);
      }

      byte[] load() {
         return Arrays.copyOf(this.load, this.load.length);
      }

      private synchronized boolean wipe() {
         if (!this.active) {
            return false;
         }

         this.active = false;
         Arrays.fill(this.username, (byte)0);
         Arrays.fill(this.load, (byte)0);
         return true;
      }
   }

   public static final class Inner_DataUtil implements AutoCloseable {
      private ResourcePackDownloadScope.Inner_StreamUtil init;

      private Inner_DataUtil(ResourcePackDownloadScope.Inner_StreamUtil token) {
         this.init = token;
      }

      @Override
      public void close() {
         ResourcePackDownloadScope.Inner_StreamUtil closing;
         synchronized (this) {
            closing = this.init;
            this.init = null;
         }

         if (closing != null) {
            synchronized (ResourcePackDownloadScope.LOCK) {
               closing.active = false;
               ResourcePackDownloadScope.tokensByConfig.remove(closing.config, closing);
            }
         }
      }
   }

   private static final class Inner_StreamUtil {
      private final BatchConfig config;
      private final ResourcePackDownloadScope.Inner_BufferHelper snapshot;
      private boolean active = true;

      private Inner_StreamUtil(BatchConfig config, ResourcePackDownloadScope.Inner_BufferHelper snapshot) {
         this.config = config;
         this.snapshot = snapshot;
      }
   }
}
