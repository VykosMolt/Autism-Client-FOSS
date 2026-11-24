package autismclient.g;

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

public final class UiHelper {
   private static final int init_I = 256;
   private static final int load_I = 256;
   private static final ThreadLocal<UiHelper.Inner_BufferHelper> init_ThreadLocal = new ThreadLocal<>();
   private static final ThreadLocal<UiHelper.Inner_BufferHelper> load_ThreadLocal = new ThreadLocal<>();
   private static final Object init_Obj = new Object();
   private static final Map<BatchConfig, UiHelper.Inner_StreamUtil> init_Map = new IdentityHashMap<>();
   private static final Map<BufferHelper.Inner_StreamUtil, UUID> load_Map = new WeakHashMap<>();
   private static final ReentrantLock[] init_ReentrantLockArr = new ReentrantLock[256];
   private static boolean init_Z;
   private static final ThreadLocal<UiHelper.Inner_ConfigStore> read;

   private UiHelper() {
   }

   public static UiHelper.Inner_BufferHelper init_UiHelper_BufferHel(ServerPackManager manager) throws IOException {
      Objects.requireNonNull(manager, "manager");
      BufferHelper.Inner_StreamUtil context = BufferHelper.init(manager);
      if (context != null
         && context.init_BufferHelper_DataU() != null
         && context.init_BufferHelper_DataU().init_UUID() != null
         && context.init_SocketAddress() != null
         && context.init_PacketListener() != null
         && context.init_ServerData_Type() != null) {
         String peer = TextFormat.init_Str_SocketAddress(context.init_SocketAddress());
         Connection connection = context.init_Connection();
         Minecraft minecraft = Minecraft.getInstance();
         boolean integrated = connection.isMemoryConnection()
            && minecraft != null
            && minecraft.hasSingleplayerServer()
            && minecraft.getConnection() == context.init_PacketListener();
         if (!integrated && context.init_BufferHelper_Buffe() == null) {
            throw new IOException("Resource-pack download has no original remote endpoint");
         }

         String origin = init(context, integrated);
         boolean multiLoopbackFixture = context.init_Z() && init_Z;
         UUID connectionIdentity;
         synchronized (init_Obj) {
            connectionIdentity = load_Map.computeIfAbsent(context, ignored -> UUID.randomUUID());
         }

         UiHelper.Inner_BufferHelper snapshot = new UiHelper.Inner_BufferHelper(
            context, manager, context.init_BufferHelper_DataU().init_UUID(), origin, peer, connectionIdentity, integrated, multiLoopbackFixture
         );
         snapshot.init_V();
         return snapshot;
      } else {
         throw new IOException("Resource-pack download has no complete connection scope");
      }
   }

   static String init(BufferHelper.Inner_StreamUtil context, boolean integrated) throws IOException {
      return integrated && context.read() ? "integrated:" + context.init_J() : TextFormat.init_Str_BufferHelper_Buffe(context.init_BufferHelper_Buffe());
   }

   public static UiHelper.Inner_CachePool init_UiHelper_CachePool(UiHelper.Inner_BufferHelper snapshot) throws IOException {
      Objects.requireNonNull(snapshot, "snapshot");
      snapshot.init_V();
      UiHelper.Inner_BufferHelper previous = init_ThreadLocal.get();
      init_ThreadLocal.set(snapshot);
      return () -> init(init_ThreadLocal, previous);
   }

   public static UiHelper.Inner_CachePool load(UiHelper.Inner_BufferHelper snapshot) throws IOException {
      Objects.requireNonNull(snapshot, "snapshot");
      snapshot.init_V();
      UiHelper.Inner_BufferHelper previous = load_ThreadLocal.get();
      if (previous == snapshot) {
         return () -> {};
      }

      load_ThreadLocal.set(snapshot);
      return () -> init(load_ThreadLocal, previous);
   }

   public static UiHelper.Inner_CachePool init(UiHelper.Inner_BufferHelper snapshot, String host, int port, String username, String password) throws IOException {
      Objects.requireNonNull(snapshot, "snapshot");
      snapshot.init_V();
      TextFormat.Inner_ConfigStore target = init(host, port);
      byte[] usernameBytes = init(username);

      byte[] passwordBytes;
      try {
         passwordBytes = init(password);
      } catch (IOException | RuntimeException e) {
         Arrays.fill(usernameBytes, (byte)0);
         throw e;
      }

      UiHelper.Inner_ConfigStore authentication = new UiHelper.Inner_ConfigStore(snapshot, target.load(), target.init_I(), usernameBytes, passwordBytes);
      UiHelper.Inner_ConfigStore previous = read.get();

      try {
         read.set(authentication);
      } catch (RuntimeException e) {
         authentication.init_Z();
         throw e;
      }

      return () -> {
         if (authentication.init_Z()) {
            init(read, previous);
         }
      };
   }

   static UiHelper.Inner_ConfigStore init(UiHelper.Inner_BufferHelper snapshot, TextFormat.Inner_BufferHelper route) {
      if (snapshot != null && route != null && route.init_Proxy_Type() == Type.SOCKS && route.init_Z()) {
         UiHelper.Inner_ConfigStore authentication = read.get();
         return authentication != null
               && authentication.init_Z
               && authentication.init_UiHelper_BufferHel == snapshot
               && authentication.init_I == route.init_TextFormat_CachePo().init_I()
               && authentication.init_Str.equalsIgnoreCase(route.init_TextFormat_CachePo().load())
            ? authentication
            : null;
      } else {
         return null;
      }
   }

   private static TextFormat.Inner_ConfigStore init(String host, int port) throws IOException {
      if (host != null && !host.isBlank() && port >= 1 && port <= 65535) {
         String literal = host.indexOf(58) >= 0 && !host.startsWith("[") ? "[" + host + "]" : host;

         try {
            return TextFormat.init_TextFormat_ConfigS_URI(new URI("http://" + literal + ":" + port + "/"));
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

   public static UiHelper.Inner_BufferHelper init_UiHelper_BufferHel() {
      return init_ThreadLocal.get();
   }

   public static UiHelper.Inner_BufferHelper load() {
      return load_ThreadLocal.get();
   }

   public static UiHelper.Inner_BufferHelper init(BatchConfig config) {
      synchronized (init_Obj) {
         UiHelper.Inner_StreamUtil token = init_Map.get(config);
         return token != null && token.init_Z ? token.init_UiHelper_BufferHel : null;
      }
   }

   public static UiHelper.Inner_BufferHelper read() throws IOException {
      UiHelper.Inner_BufferHelper snapshot = init_ThreadLocal.get();
      if (snapshot == null) {
         throw new IOException("Unscoped resource-pack batch creation is refused");
      }

      snapshot.init_V();
      return snapshot;
   }

   public static UiHelper.Inner_BufferHelper write() throws IOException {
      UiHelper.Inner_BufferHelper snapshot = load_ThreadLocal.get();
      if (snapshot == null) {
         throw new IOException("Unscoped resource-pack network access is refused");
      }

      snapshot.init_V();
      return snapshot;
   }

   public static UiHelper.Inner_BufferHelper load(BatchConfig config) throws IOException {
      synchronized (init_Obj) {
         UiHelper.Inner_StreamUtil token = init_Map.get(config);
         if (token != null && token.init_Z) {
            token.init_UiHelper_BufferHel.init_V();
            return token.init_UiHelper_BufferHel;
         } else {
            throw new IOException("Unregistered resource-pack batch");
         }
      }
   }

   public static UiHelper.Inner_DataUtil init(BatchConfig config, UiHelper.Inner_BufferHelper snapshot) throws IOException {
      Objects.requireNonNull(config, "config");
      Objects.requireNonNull(snapshot, "snapshot");
      snapshot.init_V();
      UiHelper.Inner_StreamUtil token;
      synchronized (init_Obj) {
         if (init_Map.containsKey(config)) {
            throw new IOException("Resource-pack batch is already registered");
         }

         if (init_Map.size() >= 256) {
            throw new IOException("Too many active resource-pack batches");
         }

         token = new UiHelper.Inner_StreamUtil(config, snapshot);
         init_Map.put(config, token);
      }

      return new UiHelper.Inner_DataUtil(token);
   }

   public static <T> Consumer<T> init(UiHelper.Inner_BufferHelper snapshot, Consumer<T> output) {
      Objects.requireNonNull(output, "output");
      return value -> {
         if (snapshot.load_Z()) {
            output.accept(value);
         }
      };
   }

   public static void init_V(UiHelper.Inner_BufferHelper snapshot) {
      if (snapshot != null) {
         snapshot.load_V();
      }
   }

   public static void init_V(ServerPackManager manager) {
      if (manager != null) {
         List<UiHelper.Inner_BufferHelper> snapshots = new ArrayList<>();
         synchronized (init_Obj) {
            for (UiHelper.Inner_StreamUtil token : init_Map.values()) {
               if (token.init_Z && token.init_UiHelper_BufferHel.init_ServerPackManager == manager) {
                  snapshots.add(token.init_UiHelper_BufferHel);
               }
            }
         }

         for (UiHelper.Inner_BufferHelper snapshot : snapshots) {
            snapshot.load_V();
         }

         BufferHelper.Inner_StreamUtil context = BufferHelper.init(manager);
         if (context != null) {
            BufferHelper.load_Z(context);
         }
      }
   }

   public static void init(UiHelper.Inner_BufferHelper snapshot, Socket socket) throws IOException {
      snapshot.init(socket);
   }

   public static void load(UiHelper.Inner_BufferHelper snapshot, Socket socket) {
      snapshot.load(socket);
   }

   public static Path init(Path cacheDir, UiHelper.Inner_BufferHelper snapshot, UUID packId, HashFunction hashFunction) throws IOException {
      Objects.requireNonNull(cacheDir, "cacheDir");
      Objects.requireNonNull(snapshot, "snapshot");
      Objects.requireNonNull(packId, "packId");
      Objects.requireNonNull(hashFunction, "hashFunction");
      snapshot.init_V();
      String scopeHash = init(snapshot.init_Str(), snapshot.load_Str());
      String mode = snapshot.init_BufferHelper_Cache() == BufferHelper.Inner_CachePool.init ? "vanilla-neutral-v1" : "modded-full-v1";
      FileCache.Inner_BufferHelper session = FileCache.init_FileCache_BufferHe(snapshot.init_BufferHelper_Strea());
      FileCache.Inner_DataUtil policy = FileCache.init_FileCache_DataUtil(session, packId);
      String protection = policy == FileCache.Inner_DataUtil.init ? "isolated-v1" : "full-v1";
      Path base = cacheDir.normalize();
      Path result = base.resolve("autism-v2")
         .resolve(snapshot.init_UUID().toString())
         .resolve(scopeHash)
         .resolve(snapshot.load_UUID.toString())
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
      return init_ReentrantLockArr[stripe];
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
      init_Z = enabled;
   }

   static void init_V() {
      init_Z = false;
      init_ThreadLocal.remove();
      load_ThreadLocal.remove();
      UiHelper.Inner_ConfigStore authentication = read.get();
      if (authentication != null) {
         authentication.init_Z();
      }

      read.remove();
      synchronized (init_Obj) {
         init_Map.clear();
         load_Map.clear();
      }
   }

   static {
      for (int i = 0; i < init_ReentrantLockArr.length; i++) {
         init_ReentrantLockArr[i] = new ReentrantLock();
      }

      read = new ThreadLocal<>();
   }

   public static final class Inner_BufferHelper {
      private final BufferHelper.Inner_StreamUtil init_BufferHelper_Strea;
      private final long init_J;
      private final ServerPackManager init_ServerPackManager;
      private final PacketListener init_PacketListener;
      private final long load_J;
      private final UUID init_UUID;
      private final String init_Str;
      private final SocketAddress init_SocketAddress;
      private final String load_Str;
      private final UUID load_UUID;
      private final BufferHelper.Inner_CachePool init_BufferHelper_Cache;
      private final net.minecraft.client.multiplayer.ServerData.Type init_ServerData_Type;
      private final boolean init_Z;
      private final boolean load_Z;
      private final BufferHelper.Inner_ConfigStore init_BufferHelper_Confi;
      private final Set<Socket> init_Set = Collections.newSetFromMap(new IdentityHashMap<>());
      private volatile boolean read;

      private Inner_BufferHelper(
         BufferHelper.Inner_StreamUtil context,
         ServerPackManager manager,
         UUID accountId,
         String origin,
         String canonicalPeer,
         UUID connectionIdentity,
         boolean integrated,
         boolean multiLoopbackFixture
      ) {
         this.init_BufferHelper_Strea = context;
         this.init_J = context.init_J();
         this.init_ServerPackManager = manager;
         this.init_PacketListener = context.init_PacketListener();
         this.load_J = context.load_J();
         this.init_UUID = accountId;
         this.init_Str = origin;
         this.init_SocketAddress = context.init_SocketAddress();
         this.load_Str = canonicalPeer;
         this.load_UUID = connectionIdentity;
         this.init_BufferHelper_Cache = context.init_BufferHelper_Cache();
         this.init_ServerData_Type = context.init_ServerData_Type();
         this.init_Z = integrated;
         this.load_Z = multiLoopbackFixture;
         this.init_BufferHelper_Confi = context.init_BufferHelper_Confi();
      }

      public BufferHelper.Inner_StreamUtil init_BufferHelper_Strea() {
         return this.init_BufferHelper_Strea;
      }

      public long init_J() {
         return this.init_J;
      }

      public ServerPackManager init_ServerPackManager() {
         return this.init_ServerPackManager;
      }

      public PacketListener init_PacketListener() {
         return this.init_PacketListener;
      }

      public long load_J() {
         return this.load_J;
      }

      public UUID init_UUID() {
         return this.init_UUID;
      }

      public String init_Str() {
         return this.init_Str;
      }

      public SocketAddress init_SocketAddress() {
         return this.init_SocketAddress;
      }

      public String load_Str() {
         return this.load_Str;
      }

      public BufferHelper.Inner_CachePool init_BufferHelper_Cache() {
         return this.init_BufferHelper_Cache;
      }

      public BufferHelper.Inner_ConfigStore init_BufferHelper_Confi() {
         return this.init_BufferHelper_Confi;
      }

      public net.minecraft.client.multiplayer.ServerData.Type init_ServerData_Type() {
         return this.init_ServerData_Type;
      }

      public boolean init_Z() {
         return this.init_Z || this.load_Z;
      }

      public boolean load_Z() {
         if (this.read || !BufferHelper.init(this.init_BufferHelper_Strea, this.init_J, this.init_ServerPackManager, this.init_PacketListener, this.load_J)) {
            return false;
         }

         if (!this.init_Z) {
            return true;
         }

         Minecraft minecraft = Minecraft.getInstance();
         return minecraft != null
            && minecraft.hasSingleplayerServer()
            && this.init_BufferHelper_Strea.init_Connection().isMemoryConnection()
            && minecraft.getConnection() == this.init_PacketListener;
      }

      public void init_V() throws IOException {
         if (!this.load_Z()) {
            throw new IOException("The resource-pack download is no longer current");
         }
      }

      private void init(Socket socket) throws IOException {
         Objects.requireNonNull(socket, "socket");
         boolean close = false;
         synchronized (this.init_Set) {
            if (!this.read && this.load_Z()) {
               BufferHelper.init(this.init_BufferHelper_Strea, this.load_J, socket);
               if (!this.read && this.load_Z()) {
                  this.init_Set.add(socket);
               } else {
                  BufferHelper.init(this.init_BufferHelper_Strea, socket);
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
            synchronized (this.init_Set) {
               this.init_Set.remove(socket);
            }

            BufferHelper.init(this.init_BufferHelper_Strea, socket);
         }
      }

      private void load_V() {
         List<Socket> retired;
         synchronized (this.init_Set) {
            if (this.read) {
               return;
            }

            this.read = true;
            retired = new ArrayList<>(this.init_Set);
            this.init_Set.clear();
         }

         for (Socket socket : retired) {
            BufferHelper.init(this.init_BufferHelper_Strea, socket);

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
      private final UiHelper.Inner_BufferHelper init_UiHelper_BufferHel;
      private final String init_Str;
      private final int init_I;
      private final byte[] init_BArr;
      private final byte[] load;
      private volatile boolean init_Z = true;

      private Inner_ConfigStore(UiHelper.Inner_BufferHelper snapshot, String host, int port, byte[] username, byte[] password) {
         this.init_UiHelper_BufferHel = snapshot;
         this.init_Str = host;
         this.init_I = port;
         this.init_BArr = username;
         this.load = password;
      }

      byte[] init_BArr() {
         return Arrays.copyOf(this.init_BArr, this.init_BArr.length);
      }

      byte[] load() {
         return Arrays.copyOf(this.load, this.load.length);
      }

      private synchronized boolean init_Z() {
         if (!this.init_Z) {
            return false;
         }

         this.init_Z = false;
         Arrays.fill(this.init_BArr, (byte)0);
         Arrays.fill(this.load, (byte)0);
         return true;
      }
   }

   public static final class Inner_DataUtil implements AutoCloseable {
      private UiHelper.Inner_StreamUtil init;

      private Inner_DataUtil(UiHelper.Inner_StreamUtil token) {
         this.init = token;
      }

      @Override
      public void close() {
         UiHelper.Inner_StreamUtil closing;
         synchronized (this) {
            closing = this.init;
            this.init = null;
         }

         if (closing != null) {
            synchronized (UiHelper.init_Obj) {
               closing.init_Z = false;
               UiHelper.init_Map.remove(closing.init_DownloadQueue_Batc, closing);
            }
         }
      }
   }

   private static final class Inner_StreamUtil {
      private final BatchConfig init_DownloadQueue_Batc;
      private final UiHelper.Inner_BufferHelper init_UiHelper_BufferHel;
      private boolean init_Z = true;

      private Inner_StreamUtil(BatchConfig config, UiHelper.Inner_BufferHelper snapshot) {
         this.init_DownloadQueue_Batc = config;
         this.init_UiHelper_BufferHel = snapshot;
      }
   }
}
