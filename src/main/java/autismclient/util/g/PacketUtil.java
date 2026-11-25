package autismclient.util.g;

import autismclient.AutismClientAddon;
import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismLiteVariant;
import autismclient.util.aL;
import autismclient.util.aM;
import autismclient.util.aN;
import autismclient.util.ProxyEntry;
import autismclient.util.ProxyManager;
import autismclient.util.ProxyType;
import autismclient.util.bi;
import autismclient.util.bw;
import autismclient.util.cf;
import autismclient.util.macro.aA;
import com.mojang.authlib.GameProfile;
import com.mojang.util.UndashedUuid;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;

public final class PacketUtil implements ClassCache.Inner_TokenStore {
   private static volatile PacketUtil.Inner_PacketUtil init_PacketUtil_PacketU;
   private static volatile PacketUtil init_PacketUtil;
   private static final int init_I = 100;
   private static final long init_J = 2000L;
   private static final long load_J = 20000L;
   private static final int load_I = 3;
   private static final int read_I = 2;
   private static final int write_I = 500;
   private static final int update_I = 32;
   private static final long read_J = 45000L;
   private static final long write_J = 120000L;
   private static final long update_J = 30000L;
   private static final int sync_I = 8;
   private static final int flush_I = 4;
   private static final int close_I = 4;
   private static final long sync_J = 100L;
   private static final long flush_J = TimeUnit.SECONDS.toNanos(1L);
   private final ScheduledExecutorService init_ScheduledExecutorS = Executors.newSingleThreadScheduledExecutor(runnable -> {
      Thread thread = new Thread(runnable, "Autism-Multi-Scheduler");
      thread.setDaemon(true);
      return thread;
   });
   private final ThreadPoolExecutor init_ThreadPoolExecutor = new ThreadPoolExecutor(
      1, 1, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(64), runnable -> {
         Thread thread = new Thread(runnable, "Autism-Multi-Reports");
         thread.setDaemon(true);
         return thread;
      }
   );
   private final ExecutorService init_ExecutorService = Executors.newFixedThreadPool(
      Math.max(2, Math.min(8, Runtime.getRuntime().availableProcessors())), runnable -> {
         Thread thread = new Thread(runnable, "Autism-Multi-Worker");
         thread.setDaemon(true);
         return thread;
      }
   );
   private final AtomicLong init_AtomicLong = new AtomicLong();
   private final Map<String, ClassCache> init_Map = new LinkedHashMap<>();
   private volatile List<ClassCache> init_List = List.of();
   private volatile List<ClassCache.Inner_FieldCache> load_List = List.of();
   private volatile long close_J;
   private volatile Map<String, ClassCache> load_Map = Map.of();
   private volatile long open_J;
   private volatile long poll_J;
   private final Map<String, FieldCache.Inner_BufferHelper> read_Map = new HashMap<>();
   private final Map<String, ProxyEntry> write_Map = new HashMap<>();
   private final Set<String> init_Set = ConcurrentHashMap.newKeySet();
   private static final int open_I = 1;
   private final Map<String, PacketUtil.Inner_JsonUtil> update_Map = new HashMap<>();
   private volatile Map<String, String> sync_Map = Map.of();
   private final ArrayDeque<PacketUtil.Inner_SyncManager> init_ArrayDeque = new ArrayDeque<>();
   private final Set<ClassCache> load_Set = new HashSet<>();
   private final Map<UUID, String> flush_Map = new HashMap<>();
   private final Map<String, String> close_Map = new HashMap<>();
   private final Map<String, ClassCache.Inner_PathHelper> open_Map = new HashMap<>();
   private final Map<String, Long> poll_Map = new HashMap<>();
   private final Set<String> read_Set = new HashSet<>();
   private final Map<String, ArrayDeque<PacketUtil.Inner_ConfigStore>> push_Map = new HashMap<>();
   private final ArrayDeque<PacketUtil.Inner_HashUtil> load_ArrayDeque = new ArrayDeque<>();
   private final Map<String, PacketUtil.Inner_HashUtil> pop_Map = new HashMap<>();
   private long push_J;
   private volatile long pop_J;
   private final ArrayDeque<String> read_ArrayDeque = new ArrayDeque<>();
   private String init_Str = "";
   private int poll_I = -1;
   private String load_Str = "";
   private volatile long tick_J;
   private long run;
   private boolean init_Z;
   private FieldCache init_FieldCache;
   private UUID init_UUID;
   private long get;
   private long set;
   private boolean load_Z;
   private StreamGate.Inner_CachePool init_StreamGate_CachePo = new StreamGate.Inner_CachePool(false, null, "Native");
   private ScheduledFuture<?> init_ScheduledFuture;
   private long put;
   private boolean read_Z;
   private String read_Str = "";
   private StreamGate.Inner_CachePool load_StreamGate_CachePo;
   private boolean write_Z;
   private static final int push_I = 50;
   private static final long add = 15000L;
   private final Map<String, long[]> tick_Map = new HashMap<>();

   public static void init_V_PacketUtil_PacketU(PacketUtil.Inner_PacketUtil listener) {
      init_PacketUtil_PacketU = listener;
   }

   private static void write_V() {
      PacketUtil.Inner_PacketUtil listener = init_PacketUtil_PacketU;
      if (listener != null) {
         try {
            listener.init();
         } catch (Throwable var2) {
         }
      }
   }

   private static void update_V_Str(String accountId) {
      PacketUtil.Inner_PacketUtil listener = init_PacketUtil_PacketU;
      if (listener != null) {
         try {
            listener.init(accountId);
         } catch (Throwable var3) {
         }
      }
   }

   private static void sync_V_Str(String accountId) {
      PacketUtil.Inner_PacketUtil listener = init_PacketUtil_PacketU;
      if (listener != null) {
         try {
            listener.load(accountId);
         } catch (Throwable var3) {
         }
      }
   }

   private static String read_Str_Str_Str(String source, String text) {
      return (source == null ? "" : source) + "\u0000" + text;
   }

   private static String flush_Str_Str(String text) {
      return "\u0000sys\u0000" + text;
   }

   private PacketUtil() {
   }

   public static PacketUtil init_PacketUtil() {
      PacketUtil current = init_PacketUtil;
      if (current != null) {
         return current;
      }

      synchronized (PacketUtil.class) {
         if (init_PacketUtil == null) {
            init_PacketUtil = new PacketUtil();
         }

         return init_PacketUtil;
      }
   }

   public static PacketUtil load_PacketUtil() {
      return init_PacketUtil;
   }

   public synchronized PacketUtil.Inner_TextFormat init_PacketUtil_TextFor_FieldCache(FieldCache source) {

      if (source == null) {
         return PacketUtil.Inner_TextFormat.init("Profile is missing");
      }

      if (this.load_Z) {
         return PacketUtil.Inner_TextFormat.init("Disconnect the active Multi batch first");
      }

      FieldCache profile = new FieldCache(source);
      profile.init_V();
      UUID renderedProfileId = init_UUID();
      boolean hadSessions = !profile.init_List.isEmpty();
      profile.init_List.removeIf(spec -> init_Z_Str(spec.init_Str()));
      if (hadSessions && profile.init_List.isEmpty()) {
         return PacketUtil.Inner_TextFormat.init("You're playing on this account. Pick another.");
      }

      PacketUtil.Inner_TextFormat validation = this.init_PacketUtil_TextFor_FieldCache_UUID(profile, renderedProfileId);
      if (!validation.init_Z()) {
         return validation;
      }

      ServerAddress server = ServerAddress.parseString(profile.update_Str);
      StreamGate.Inner_CachePool selectedVia = StreamGate.init();
      if (profile.update_Str.equalsIgnoreCase(this.read_Str)) {
         selectedVia = StreamGate.init(selectedVia, this.load_StreamGate_CachePo);
      }

      String viaError = StreamGate.init_Str(selectedVia);
      if (!viaError.isBlank()) {
         return PacketUtil.Inner_TextFormat.init(viaError);
      }

      this.tick_J = this.init_AtomicLong.incrementAndGet();
      this.init_FieldCache = profile;
      this.init_UUID = renderedProfileId;
      this.init_StreamGate_CachePo = selectedVia;
      this.load_Z = true;
      cf.init_V_J_Z(512L, true);
      this.pop();
      this.get = 0L;
      this.set = 0L;
      this.init_Map.clear();
      this.update_V();
      this.read_Map.clear();
      this.write_Map.clear();
      this.update_Map.clear();
      this.close();
      this.init_ArrayDeque.clear();
      this.load_Set.clear();
      this.flush_Map.clear();
      this.close_Map.clear();
      this.open_Map.clear();
      this.poll_Map.clear();
      this.read_Set.clear();
      this.init_Z = false;
      this.flush();
      long startedGeneration = this.tick_J;
      boolean auto = profile.init_FieldCache_CachePo == FieldCache.Inner_CachePool.load;
      this.close_V_Str(auto ? "Verifying proxies for " + profile.update_Str : "Resolving " + profile.update_Str);
      CompletableFuture.runAsync(() -> {
         try {
            Map var10000;
            switch (profile.init_FieldCache_CachePo) {
               case load:
                  var10000 = this.init_Map_J(startedGeneration, profile, server.getHost(), server.getPort());
                  break;
               case read:
                  var10000 = init_Map_FieldCache_List(profile, ProxyManager.init_bY().init_List());
                  break;
               case init:
                  var10000 = Map.of();
                  break;
               default:
                  throw new MatchException(null, null);
            }

            Map<String, ProxyEntry> assignment = var10000;
            if (!this.init_Z_J(startedGeneration)) {
               return;
            }

            InetSocketAddress resolved = ServerNameResolver.DEFAULT.resolveAddress(server).map(ResolvedServerAddress::asInetSocketAddress).orElse(null);
            Minecraft.getInstance().execute(() -> this.init_V_J(startedGeneration, profile, assignment, resolved, server));
         } catch (Throwable error) {
            String message = "Start failed: " + update(error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage(), 120);
            Minecraft.getInstance().execute(() -> this.init_V_J(startedGeneration, profile, message));
         }
      }, this.init_ExecutorService);
      return PacketUtil.Inner_TextFormat.init_PacketUtil_TextFor();
   }

   private synchronized void init_V_J(long gen, FieldCache profile, Map<String, ProxyEntry> assignment, InetSocketAddress resolved, ServerAddress server) {
      if (this.load_Z && this.tick_J == gen) {
         boolean dnsOk = resolved != null;
         this.close_V_Str(dnsOk ? "Starting " + profile.write_Str + " on " + profile.update_Str : "Unknown server address");

         for (FieldCache.Inner_BufferHelper spec : profile.init_List) {
            ProxyEntry proxy = init_bW_FieldCache(profile, spec, assignment);
            boolean proxyRequired = profile.init_FieldCache_CachePo != FieldCache.Inner_CachePool.init && !spec.init_Z();
            boolean missingProxy = proxyRequired && proxy == null;
            ProxyEntry snapshot = init_bW_bW(proxy);
            String proxyName = snapshot != null ? snapshot.init_Str() : (proxyRequired ? "No proxy" : "Proxy Off");
            ClassCache session = new ClassCache(
               this.tick_J,
               spec,
               snapshot,
               proxyName,
               profile.init_IoWorker,
               profile.init_FieldCache_DataUti,
               profile.write_Str,
               profile.init_Map_Str(spec.init_Str()),
               this,
               this.init_ExecutorService,
               this.init_StreamGate_CachePo
            );
            session.init_V_CachePool(profile.init_CachePool);
            this.write_V_ClassCache(session);
            this.init_Map.put(spec.init_Str(), session);
            this.read_Map.put(spec.init_Str(), init_FieldCache_BufferH_FieldCache(profile, spec, proxy));
            this.write_Map.put(spec.init_Str(), snapshot);
            if (!dnsOk) {
               session.sync_V_Str("Unknown server address");
            } else if (missingProxy) {
               session.sync_V_Str("No working proxy");
            } else {
               this.init_ArrayDeque.addLast(new PacketUtil.Inner_SyncManager(session, resolved, server.getHost(), server.getPort()));
            }

            this.init_V_FieldCache(profile, spec, session);
         }

         this.update_V();
         if (dnsOk) {
            this.sync_V();
         }

         if (profile.init_FieldCache_DataUti == FieldCache.Inner_DataUtil.read) {
            LinkedHashSet<String> names = new LinkedHashSet<>();

            for (FieldCache.Inner_BufferHelper spec : profile.init_List) {
               String name = spec.read().isBlank() ? profile.sync : spec.read();
               if (name != null && !name.isBlank()) {
                  names.add(name);
               }
            }

            for (String name : names) {
               aM macro = aN.init_aN().init_aM_Str(name);
               if (macro != null) {
                  this.init_V_aM(macro);
               }
            }
         }
      }
   }

   private void update_V() {
      if (!this.write_Z) {
         this.init_List = List.copyOf(this.init_Map.values());
         this.load_Map = Map.copyOf(this.init_Map);
         this.init_V_Z(true);
         this.open_J++;
      }
   }

   private synchronized void init_V_J(long gen, FieldCache profile, String reason) {
      if (this.load_Z && this.tick_J == gen && this.init_Map.isEmpty()) {
         this.close_V_Str(reason);

         for (FieldCache.Inner_BufferHelper spec : profile.init_List) {
            ClassCache session = new ClassCache(
               gen,
               spec,
               null,
               "Proxy Off",
               profile.init_IoWorker,
               profile.init_FieldCache_DataUti,
               profile.write_Str,
               profile.init_Map_Str(spec.init_Str()),
               this,
               this.init_ExecutorService,
               this.init_StreamGate_CachePo
            );
            this.init_Map.put(spec.init_Str(), session);
            this.read_Map.put(spec.init_Str(), spec);
            this.write_Map.put(spec.init_Str(), null);
            this.init_V_FieldCache(profile, spec, session);
            session.sync_V_Str(reason);
         }

         this.update_V();
      }
   }

   private static ProxyEntry init_bW_FieldCache(FieldCache profile, FieldCache.Inner_BufferHelper spec, Map<String, ProxyEntry> assignment) {
      ProxyEntry var10000;
      switch (profile.init_FieldCache_CachePo) {
         case load:
            var10000 = assignment.get(spec.init_Str());
            break;
         case read:
            var10000 = spec.init_Z() ? null : assignment.get(spec.init_Str());
            break;
         case init:
            var10000 = null;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static FieldCache.Inner_BufferHelper init_FieldCache_BufferH_FieldCache(FieldCache profile, FieldCache.Inner_BufferHelper spec, ProxyEntry proxy) {
      FieldCache.Inner_BufferHelper var10000;
      switch (profile.init_FieldCache_CachePo) {
         case load:
            var10000 = new FieldCache.Inner_BufferHelper(spec.init_Str(), proxy == null ? "" : proxy.write());
            break;
         case read:
            var10000 = spec;
            break;
         case init:
            var10000 = new FieldCache.Inner_BufferHelper(spec.init_Str(), "");
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private PacketUtil.Inner_TextFormat init_PacketUtil_TextFor_FieldCache_UUID(FieldCache profile, UUID renderedProfileId) {
      if (profile.update_Str.isBlank()) {
         return PacketUtil.Inner_TextFormat.init("Server address is required");
      }

      if (profile.init_List.isEmpty()) {
         return PacketUtil.Inner_TextFormat.init("Select at least one account");
      }

      if (profile.init_List.size() > 500) {
         return PacketUtil.Inner_TextFormat.init("Maximum 500 sessions");
      }

      boolean manual = profile.init_FieldCache_CachePo == FieldCache.Inner_CachePool.read;
      Set<String> accountIds = new HashSet<>();
      Set<String> identities = new HashSet<>();
      int manualBestRows = 0;

      for (FieldCache.Inner_BufferHelper spec : profile.init_List) {
         if (!accountIds.add(spec.init_Str())) {
            return PacketUtil.Inner_TextFormat.init("Duplicate account row");
         }

         String identity;
         if ("default".equals(spec.init_Str())) {
            UUID defaultProfileId = autismclient.util.FieldCache.init_User().getProfileId();
            if (renderedProfileId != null && renderedProfileId.equals(defaultProfileId)) {
               return PacketUtil.Inner_TextFormat.init("You're playing on this account.");
            }

            identity = defaultProfileId.toString();
         } else {
            autismclient.util.BitSetUtil account = autismclient.util.TokenStore.init_TokenStore().init_BitSetUtil(spec.init_Str());
            if (account == null) {
               return PacketUtil.Inner_TextFormat.init("Missing account: " + spec.init_Str());
            }

            UUID knownProfileId = init_UUID_Str(account.sync);
            if (renderedProfileId != null && renderedProfileId.equals(knownProfileId)) {
               return PacketUtil.Inner_TextFormat.init("You're playing on this account.");
            }

            identity = account.sync != null && !account.sync.isBlank()
               ? account.sync.toLowerCase(Locale.ROOT)
               : account.init_MethodUtil.name() + ":" + account.init_Str().toLowerCase(Locale.ROOT);
         }

         if (!identities.add(identity)) {
            return PacketUtil.Inner_TextFormat.init("The same Minecraft identity is selected twice");
         }

         if (manual) {
            if (spec.load_Z()) {
               manualBestRows++;
            } else if (!spec.init_Z()) {
               ProxyEntry proxy = ProxyManager.init_bY().init_bW_Str(spec.load_Str());
               if (proxy == null || !proxy.init_Z()) {
                  return PacketUtil.Inner_TextFormat.init("Missing proxy for " + poll(spec.init_Str()));
               }
            }
         }
      }

      List<ProxyEntry> usableProxies = init_List_List(ProxyManager.init_bY().init_List());
      if (profile.init_FieldCache_CachePo == FieldCache.Inner_CachePool.load && usableProxies.isEmpty()) {
         return PacketUtil.Inner_TextFormat.init("Add proxies or set Proxy: Off");
      } else {
         return manual && manualBestRows > 0 && usableProxies.isEmpty()
            ? PacketUtil.Inner_TextFormat.init("No proxy is available for Manual Best Proxy rows")
            : PacketUtil.Inner_TextFormat.init_PacketUtil_TextFor();
      }
   }

   private static UUID init_UUID() {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft == null) {
         return null;
      }

      ClientPacketListener connection = minecraft.getConnection();
      if (connection == null) {
         return null;
      }

      GameProfile profile = connection.getLocalGameProfile();
      return profile == null ? null : profile.id();
   }

   public static String init_Str() {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft != null && minecraft.getConnection() != null) {
         GameProfile profile = minecraft.getConnection().getLocalGameProfile();
         return profile == null ? "" : profile.name();
      } else {
         return "";
      }
   }

   public synchronized boolean init_Z() {
      if (this.load_Z && this.init_FieldCache != null) {
         Minecraft minecraft = Minecraft.getInstance();
         return minecraft != null && minecraft.getConnection() != null && minecraft.getCurrentServer() != null
            ? init_Z_Str_Str(this.init_FieldCache.update_Str, minecraft.getCurrentServer().ip)
            : false;
      } else {
         return false;
      }
   }

   static boolean init_Z_Str_Str(String a, String b) {
      if (a != null && b != null) {
         try {
            ServerAddress sa = ServerAddress.parseString(a.trim());
            ServerAddress sb = ServerAddress.parseString(b.trim());
            return sa.getHost().equalsIgnoreCase(sb.getHost()) && sa.getPort() == sb.getPort();
         } catch (RuntimeException ignored) {
            return a.trim().equalsIgnoreCase(b.trim());
         }
      } else {
         return false;
      }
   }

   static UUID init_UUID_Str(String value) {
      if (value != null && !value.isBlank()) {
         try {
            return UndashedUuid.fromStringLenient(value.trim());
         } catch (RuntimeException ignored) {
            try {
               return UUID.fromString(value.trim());
            } catch (RuntimeException ignoredAgain) {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   private static UUID read_UUID_Str(String accountId) {
      if (accountId == null) {
         return null;
      }

      if ("default".equals(accountId)) {
         User user = autismclient.util.FieldCache.init_User();
         return user == null ? null : user.getProfileId();
      }

      autismclient.util.BitSetUtil account = autismclient.util.TokenStore.init_TokenStore().init_BitSetUtil(accountId);
      if (account == null) {
         return null;
      }

      UUID known = init_UUID_Str(account.sync);
      if (known != null) {
         return known;
      }

      String name = account.update != null && !account.update.isBlank() ? account.update : account.init_Str();
      return name != null && !name.isBlank() ? UUIDUtil.createOfflinePlayerUUID(name) : null;
   }

   public static boolean init_Z_Str(String accountId) {
      if (accountId == null) {
         return false;
      }

      Minecraft minecraft = Minecraft.getInstance();
      ClientPacketListener connection = minecraft == null ? null : minecraft.getConnection();
      if (connection == null) {
         return false;
      }

      GameProfile rendered = connection.getLocalGameProfile();
      if (rendered == null) {
         return false;
      }

      UUID target = read_UUID_Str(accountId);
      if (target != null && target.equals(rendered.id())) {
         return true;
      }

      String renderedName = rendered.name();
      String accountName = close_Str_Str(accountId);
      return renderedName != null && !renderedName.isBlank() && accountName != null && !accountName.isBlank() && renderedName.equalsIgnoreCase(accountName);
   }

   private static String close_Str_Str(String accountId) {
      if ("default".equals(accountId)) {
         User user = autismclient.util.FieldCache.init_User();
         return user == null ? null : user.getName();
      } else {
         autismclient.util.BitSetUtil account = autismclient.util.TokenStore.init_TokenStore().init_BitSetUtil(accountId);
         if (account == null) {
            return null;
         } else {
            return account.update != null && !account.update.isBlank() ? account.update : account.init_Str();
         }
      }
   }

   static Map<String, ProxyEntry> init_Map_FieldCache_List(FieldCache profile, List<ProxyEntry> available) {
      if (profile == null) {
         return Map.of();
      }

      List<ProxyEntry> usable = init_List_List(available);
      List<ProxyEntry> allValid = available == null ? List.of() : available.stream().filter(proxy -> proxy != null && proxy.init_Z()).toList();
      Map<String, ProxyEntry> assignment = new LinkedHashMap<>();
      Set<String> explicitlyUsed = new HashSet<>();

      for (FieldCache.Inner_BufferHelper spec : profile.init_List) {
         if (!spec.init_Z() && !spec.load_Z()) {
            ProxyEntry selected = init_bW_List_Str(allValid, spec.load_Str());
            if (selected != null) {
               assignment.put(spec.init_Str(), selected);
               explicitlyUsed.add(init_Str_bW(selected));
            }
         }
      }

      List<ProxyEntry> bestOrder = new ArrayList<>(usable.size());

      for (ProxyEntry candidate : usable) {
         if (!explicitlyUsed.contains(init_Str_bW(candidate))) {
            bestOrder.add(candidate);
         }
      }

      for (ProxyEntry candidate : usable) {
         if (explicitlyUsed.contains(init_Str_bW(candidate))) {
            bestOrder.add(candidate);
         }
      }

      int bestIndex = 0;

      for (FieldCache.Inner_BufferHelper spec : profile.init_List) {
         if (spec.load_Z()) {
            ProxyEntry selected = bestOrder.isEmpty() ? null : bestOrder.get(bestIndex++ % bestOrder.size());
            if (selected != null) {
               assignment.put(spec.init_Str(), selected);
            }
         }
      }

      return assignment;
   }

   private static ProxyEntry init_bW_List_Str(List<ProxyEntry> proxies, String id) {
      if (proxies != null && id != null && !id.isBlank()) {
         for (ProxyEntry proxy : proxies) {
            if (id.equals(proxy.write())) {
               return proxy;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private synchronized void sync_V() {
      if (this.load_Z && this.init_FieldCache != null) {
         int limit = this.init_FieldCache.init_I();

         while (this.load_Set.size() < limit && !this.init_ArrayDeque.isEmpty()) {
            PacketUtil.Inner_SyncManager next = this.init_ArrayDeque.removeFirst();
            this.load_Set.add(next.init_ClassCache());
            long now = System.currentTimeMillis();
            long at = Math.max(now, this.get);
            this.get = at + this.init_FieldCache.load_I();
            try {
               this.init_ScheduledExecutorS
                  .schedule(
                     () -> {
                        synchronized (this) {
                           if (!this.load_Z
                              || next.init_ClassCache().close_J() != this.tick_J
                              || this.init_Map.get(next.init_ClassCache().open_Str()) != next.init_ClassCache()) {
                              this.load_Set.remove(next.init_ClassCache());
                              this.sync_V();
                              return;
                           }
                        }

                        next.init_ClassCache().init_V_InetSocketAddress(next.init_InetSocketAddress(), next.init_Str(), next.init_I());
                     },
                     Math.max(0L, at - now),
                     TimeUnit.MILLISECONDS
                  );
            } catch (RejectedExecutionException ignored) {
               this.load_Set.remove(next.init_ClassCache());
               break;
            }
         }
      }
   }

   public synchronized PacketUtil.Inner_EventBus init_PacketUtil_EventBu_Str(String accountId) {
      if (this.load_Z && this.init_FieldCache != null && accountId != null) {
         ClassCache old = this.init_Map.get(accountId);
         if (old != null && old.tick_Z()) {
            return PacketUtil.Inner_EventBus.load("Connected");
         }

         if (old != null && !init_Z_ClassCache_PathHel(old.init_ClassCache_PathHel())) {
            return PacketUtil.Inner_EventBus.load("Still connecting");
         }

         if (!this.read_Set.add(accountId)) {
            return PacketUtil.Inner_EventBus.load("Already retrying");
         }

         long retryToken = ++this.run;
         this.poll_Map.put(accountId, retryToken);
         FieldCache.Inner_BufferHelper spec = this.read_Map.get(accountId);
         if (spec == null) {
            this.read_Set.remove(accountId);
            return PacketUtil.Inner_EventBus.load("No session");
         }

         ServerAddress server = ServerAddress.parseString(this.init_FieldCache.update_Str);
         if (this.init_FieldCache.init_FieldCache_CachePo == FieldCache.Inner_CachePool.load) {
            PacketUtil.Inner_RenderState candidates = this.init_PacketUtil_RenderS_Str(accountId);
            if (candidates.init().isEmpty() && candidates.load().isEmpty()) {
               this.read_Set.remove(accountId);
               this.close_V_Str(poll(accountId) + ": No proxy");
               return PacketUtil.Inner_EventBus.load("No proxy");
            } else {
               long retryGeneration = this.tick_J;
               int target = this.init_FieldCache.update_I;
               CompletableFuture.runAsync(() -> {
                  try {
                     this.init_V_J_2(retryGeneration, retryToken, accountId, candidates, server, target);
                  } catch (Throwable error) {
                     synchronized (this) {
                        if (!this.load_Z || this.tick_J != retryGeneration || this.poll_Map.getOrDefault(accountId, -1L) != retryToken) {
                           return;
                        }

                        this.read_Set.remove(accountId);
                        this.close_V_Str(poll(accountId) + ": Retry failed");
                     }
                  }
               }, this.init_ExecutorService);
               this.close_V_Str(poll(accountId) + ": Verifying proxy");
               return PacketUtil.Inner_EventBus.init("Verifying proxy");
            }
         } else {
            ProxyEntry previous = this.write_Map.get(accountId);
            String currentProxyId = previous == null ? spec.load_Str() : previous.write();
            boolean proxyEnabled = this.init_FieldCache.init_FieldCache_CachePo == FieldCache.Inner_CachePool.read && !spec.init_Z();
            if (!proxyEnabled) {
               currentProxyId = "";
            }

            String lastFailedProxyId = this.close_Map.getOrDefault(accountId, currentProxyId == null ? "" : currentProxyId);
            ProxyEntry selectedProxy = null;
            if (proxyEnabled) {
               if (spec.load_Z()) {
                  selectedProxy = this.init_bW_Str(accountId, currentProxyId, lastFailedProxyId);
               } else {
                  ProxyEntry configured = ProxyManager.init_bY().init_bW_Str(spec.load_Str());
                  selectedProxy = configured != null ? configured : previous;
               }
            }

            if (selectedProxy == null && proxyEnabled) {
               this.read_Set.remove(accountId);
               this.close_V_Str(poll(accountId) + ": No proxy");
               return PacketUtil.Inner_EventBus.load("No proxy");
            }

            FieldCache.Inner_BufferHelper retrySpec = this.init_FieldCache.init_FieldCache_CachePo == FieldCache.Inner_CachePool.read
               ? spec
               : new FieldCache.Inner_BufferHelper(accountId, "");
            ProxyEntry proxy = init_bW_bW(selectedProxy);
            this.read_Map.put(accountId, retrySpec);
            this.write_Map.put(accountId, proxy);
            ClassCache replacement = new ClassCache(
               this.tick_J,
               retrySpec,
               proxy,
               proxy == null ? "Proxy Off" : proxy.init_Str(),
               this.init_FieldCache.init_IoWorker,
               this.init_FieldCache.init_FieldCache_DataUti,
               this.init_FieldCache.write_Str,
               this.init_FieldCache.init_Map_Str(accountId),
               this,
               this.init_ExecutorService,
               this.init_StreamGate_CachePo
            );
            replacement.init_V_CachePool(this.init_FieldCache.init_CachePool);
            this.write_V_ClassCache(replacement);
            this.init_ArrayDeque.removeIf(next -> next.init_ClassCache() == old);
            this.load_Set.remove(old);
            this.init_Map.put(accountId, replacement);
            if (old != null) {
               old.update_V_Str("Retrying");
            }

            this.init_V_Str_ClassCache(accountId, replacement);
            this.update_V();
            long retryGeneration = this.tick_J;
            CompletableFuture.<InetSocketAddress>supplyAsync(
                  () -> ServerNameResolver.DEFAULT.resolveAddress(server).map(ResolvedServerAddress::asInetSocketAddress).orElse(null),
                  this.init_ExecutorService
               )
               .whenComplete(
                  (resolved, error) -> {
                     synchronized (this) {
                        if (this.load_Z
                           && this.tick_J == retryGeneration
                           && this.init_Map.get(accountId) == replacement
                           && this.poll_Map.getOrDefault(accountId, -1L) == retryToken) {
                           this.read_Set.remove(accountId);
                           if (error == null && resolved != null) {
                              this.init_ArrayDeque.addLast(new PacketUtil.Inner_SyncManager(replacement, resolved, server.getHost(), server.getPort()));
                              this.sync_V();
                           } else {
                              replacement.sync_V_Str("Unknown server address");
                           }
                        }
                     }
                  }
               );
            String label = proxy == null ? "Proxy Off" : proxy.init_Str();
            String message = "Retry: " + update(label, 32);
            this.close_V_Str(poll(accountId) + ": " + message);
            return PacketUtil.Inner_EventBus.init(message);
         }
      } else {
         return PacketUtil.Inner_EventBus.load("No batch");
      }
   }

   private synchronized PacketUtil.Inner_RenderState init_PacketUtil_RenderS_Str(String accountId) {
      Set<String> inUseByOthers = new HashSet<>();

      for (Entry<String, ProxyEntry> entry : this.write_Map.entrySet()) {
         if (!entry.getKey().equals(accountId) && entry.getValue() != null) {
            inUseByOthers.add(init_Str_bW(entry.getValue()));
         }
      }

      return init_PacketUtil_RenderS_List(init_List_List(ProxyManager.init_bY().init_List()), inUseByOthers, this.close_Map.getOrDefault(accountId, ""));
   }

   static PacketUtil.Inner_RenderState init_PacketUtil_RenderS_List(List<ProxyEntry> orderedDistinct, Set<String> inUseLeaseKeys, String lastFailedProxyId) {
      List<ProxyEntry> unused = new ArrayList<>();
      List<ProxyEntry> fallback = new ArrayList<>();
      String failed = lastFailedProxyId == null ? "" : lastFailedProxyId;

      for (ProxyEntry proxy : orderedDistinct == null ? List.<ProxyEntry>of() : orderedDistinct) {
         if (proxy != null) {
            boolean shared = !failed.isBlank() && failed.equals(proxy.write());
            if (!shared && inUseLeaseKeys != null) {
               shared = inUseLeaseKeys.contains(init_Str_bW(proxy));
            }

            (shared ? fallback : unused).add(proxy);
         }
      }

      return new PacketUtil.Inner_RenderState(List.copyOf(unused), List.copyOf(fallback));
   }

   private synchronized ProxyEntry init_bW_Str(String accountId, String currentProxyId, String lastFailedProxyId) {
      Set<String> inUseByOthers = new HashSet<>();

      for (Entry<String, ProxyEntry> entry : this.write_Map.entrySet()) {
         if (!entry.getKey().equals(accountId) && entry.getValue() != null) {
            inUseByOthers.add(init_Str_bW(entry.getValue()));
         }
      }

      List<ProxyEntry> preferred = new ArrayList<>();
      List<ProxyEntry> shared = new ArrayList<>();

      for (ProxyEntry proxy : init_List_List(ProxyManager.init_bY().init_List())) {
         if (inUseByOthers.contains(init_Str_bW(proxy))) {
            shared.add(proxy);
         } else {
            preferred.add(proxy);
         }
      }

      preferred.addAll(shared);
      return init_bW_List(preferred, currentProxyId, lastFailedProxyId, false);
   }

   private void init_V_J_2(long gen, long retryToken, String accountId, PacketUtil.Inner_RenderState candidates, ServerAddress server, int target) {
      int timeout = init_I_I(target);
      PacketUtil.Inner_FileCache best = null;
      List<ProxyEntry> failedUnused = new ArrayList<>();
      int tries = 0;

      for (ProxyEntry candidate : candidates.init()) {
         if (!this.init_Z_J(gen, retryToken, accountId)) {
            return;
         }

         if (tries++ >= 8) {
            break;
         }

         PacketUtil.Inner_FileCache sample = this.init_PacketUtil_FileCac_J(gen, candidate, server.getHost(), server.getPort(), timeout);
         if (sample == null) {
            return;
         }

         if (!sample.init_Z()) {
            failedUnused.add(candidate);
         } else {
            if (sample.init_J() <= target) {
               best = sample;
               break;
            }

            if (best == null || sample.init_J() < best.init_J()) {
               best = sample;
            }
         }
      }

      if (best == null && !failedUnused.isEmpty()) {
         best = this.init_PacketUtil_FileCac_J(gen, retryToken, accountId, failedUnused, server, target, load_I_I(target));
      }

      if (best == null) {
         int sharedTries = 0;

         for (ProxyEntry candidate : candidates.load()) {
            if (!this.init_Z_J(gen, retryToken, accountId)) {
               return;
            }

            if (sharedTries++ >= 4) {
               break;
            }

            PacketUtil.Inner_FileCache sample = this.init_PacketUtil_FileCac_J(gen, candidate, server.getHost(), server.getPort(), timeout);
            if (sample == null) {
               return;
            }

            if (sample.init_Z()) {
               if (sample.init_J() <= target) {
                  best = sample;
                  break;
               }

               if (best == null || sample.init_J() < best.init_J()) {
                  best = sample;
               }
            }
         }
      }

      if (this.init_Z_J(gen, retryToken, accountId)) {
         InetSocketAddress resolved = ServerNameResolver.DEFAULT.resolveAddress(server).map(ResolvedServerAddress::asInetSocketAddress).orElse(null);
         ProxyEntry chosenFinal = best == null ? null : best.init_bW();
         Minecraft.getInstance().execute(() -> this.init_V_J(gen, retryToken, accountId, chosenFinal, resolved, server));
      }
   }

   private PacketUtil.Inner_FileCache init_PacketUtil_FileCac_J(
      long gen, long retryToken, String accountId, List<ProxyEntry> failed, ServerAddress server, int target, int timeout
   ) {
      PacketUtil.Inner_FileCache best = null;
      int tries = 0;

      for (ProxyEntry candidate : failed) {
         if (!this.init_Z_J(gen, retryToken, accountId)) {
            return null;
         }

         if (tries++ >= 4) {
            break;
         }

         PacketUtil.Inner_FileCache sample = this.init_PacketUtil_FileCac_J(gen, candidate, server.getHost(), server.getPort(), timeout);
         if (sample == null) {
            return null;
         }

         if (sample.init_Z()) {
            if (sample.init_J() <= target) {
               return sample;
            }

            if (best == null || sample.init_J() < best.init_J()) {
               best = sample;
            }
         }
      }

      return best;
   }

   private synchronized void init_V_J(long gen, long retryToken, String accountId, ProxyEntry chosen, InetSocketAddress resolved, ServerAddress server) {
      if (this.load_Z && this.tick_J == gen && this.init_FieldCache != null && this.poll_Map.getOrDefault(accountId, -1L) == retryToken) {
         this.read_Set.remove(accountId);
         ClassCache old = this.init_Map.get(accountId);
         if (old == null || !old.tick_Z()) {
            if (chosen == null) {
               this.close_V_Str(poll(accountId) + ": No working proxy");
               if (old != null) {
                  old.sync_V_Str("No working proxy");
               }
            } else if (resolved == null) {
               this.close_V_Str("Unknown server address");
               if (old != null) {
                  old.sync_V_Str("Unknown server address");
               }
            } else {
               this.init_ArrayDeque.removeIf(next -> next.init_ClassCache() == old);
               this.load_Set.remove(old);
               ProxyEntry proxy = init_bW_bW(chosen);
               FieldCache.Inner_BufferHelper retrySpec = new FieldCache.Inner_BufferHelper(accountId, chosen.write());
               this.read_Map.put(accountId, retrySpec);
               this.write_Map.put(accountId, proxy);
               ClassCache replacement = new ClassCache(
                  this.tick_J,
                  retrySpec,
                  proxy,
                  proxy.init_Str(),
                  this.init_FieldCache.init_IoWorker,
                  this.init_FieldCache.init_FieldCache_DataUti,
                  this.init_FieldCache.write_Str,
                  this.init_FieldCache.init_Map_Str(accountId),
                  this,
                  this.init_ExecutorService,
                  this.init_StreamGate_CachePo
               );
               replacement.init_V_CachePool(this.init_FieldCache.init_CachePool);
               this.write_V_ClassCache(replacement);
               this.init_Map.put(accountId, replacement);
               if (old != null) {
                  old.update_V_Str("Retrying");
               }

               this.init_V_Str_ClassCache(accountId, replacement);
               this.update_V();
               this.init_ArrayDeque.addLast(new PacketUtil.Inner_SyncManager(replacement, resolved, server.getHost(), server.getPort()));
               this.sync_V();
               boolean shared = this.init_Z_Str_bW(accountId, proxy);
               this.close_V_Str(poll(accountId) + ": Retry " + update(proxy.init_Str(), 32) + (shared ? " (shared)" : ""));
            }
         }
      }
   }

   private boolean init_Z_Str_bW(String accountId, ProxyEntry proxy) {
      String key = init_Str_bW(proxy);
      if (key.isBlank()) {
         return false;
      }

      for (Entry<String, ProxyEntry> entry : this.write_Map.entrySet()) {
         if (!entry.getKey().equals(accountId) && key.equals(init_Str_bW(entry.getValue()))) {
            return true;
         }
      }

      return false;
   }

   public synchronized PacketUtil.Inner_EventBus init_PacketUtil_EventBu() {
      if (this.load_Z && this.init_FieldCache != null) {
         int attempted = 0;
         this.write_Z = true;

         try {
            for (String accountId : new ArrayList<>(this.init_Map.keySet())) {
               ClassCache session = this.init_Map.get(accountId);
               if (session != null && init_Z_ClassCache_PathHel(session.init_ClassCache_PathHel()) && this.init_PacketUtil_EventBu_Str(accountId).init_Z()) {
                  attempted++;
               }
            }
         } finally {
            this.write_Z = false;
         }

         this.update_V();
         return attempted == 0 ? PacketUtil.Inner_EventBus.load("Nothing to retry") : PacketUtil.Inner_EventBus.init("Retrying " + attempted);
      } else {
         return PacketUtil.Inner_EventBus.load("No batch");
      }
   }

   private Map<String, ProxyEntry> init_Map_J(long gen, FieldCache profile, String host, int port) {
      List<ProxyEntry> candidates = new ArrayList<>();
      List<ProxyEntry> deadCandidates = new ArrayList<>();

      for (ProxyEntry proxy : ProxyManager.init_bY().init_List()) {
         if (proxy != null && proxy.init_Z()) {
            if (proxy.init_bW_CachePool == ProxyEntry.Inner_CachePool.read) {
               deadCandidates.add(proxy);
            } else {
               candidates.add(proxy);
            }
         }
      }

      write_V_List(candidates);
      write_V_List(deadCandidates);
      Comparator<ProxyEntry> bestFirst = Comparator.comparingInt(PacketUtil::init_I_bW)
         .thenComparingLong(proxy -> proxy.init_bW_CachePool == ProxyEntry.Inner_CachePool.write && proxy.init_J > 0L ? proxy.init_J : 9223372036854775807L);
      candidates.sort(bestFirst);
      deadCandidates.sort(bestFirst);
      Map<String, ProxyEntry> assignment = new LinkedHashMap<>();
      if (candidates.isEmpty() && deadCandidates.isEmpty()) {
         this.init_V_J_Str(gen, "No proxies to verify");
         return assignment;
      }

      int needed = (int)profile.init_List.stream().filter(spec -> !spec.init_Z()).count();
      int target = profile.update_I;
      List<ProxyEntry> poolList = new ArrayList<>(candidates.subList(0, Math.min(candidates.size(), 500)));
      int timeout = init_I_I(target);
      int parallelism = Math.min(32, Math.max(8, poolList.size()));
      ExecutorService probePool = Executors.newFixedThreadPool(parallelism, runnable -> {
         Thread thread = new Thread(runnable, "Autism-Multi-Probe");
         thread.setDaemon(true);
         return thread;
      });

      try {
         ExecutorCompletionService<PacketUtil.Inner_FileCache> service = new ExecutorCompletionService<>(probePool);
         List<Future<PacketUtil.Inner_FileCache>> probeTasks = new ArrayList<>(poolList.size());

         for (ProxyEntry candidate : poolList) {
            probeTasks.add(service.submit(() -> this.init_PacketUtil_FileCac_J(gen, candidate, host, port, timeout)));
         }

         if (!poolList.isEmpty()) {
            this.init_V_J_Str(gen, "Testing " + poolList.size() + (poolList.size() == 1 ? " proxy" : " proxies") + " (target " + target + "ms)");
         }

         long deadline = System.currentTimeMillis() + init_J_I_I(poolList.size(), parallelism);
         List<PacketUtil.Inner_FileCache> working = new ArrayList<>();
         Set<String> distinctLeases = new HashSet<>();
         int wantDistinct = needed + 2;
         int tested = 0;

         for (int i = 0; i < poolList.size() && this.init_Z_J(gen); i++) {
            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0L) {
               break;
            }

            Future<PacketUtil.Inner_FileCache> done;
            try {
               done = service.poll(remaining, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
               break;
            }

            if (done == null) {
               break;
            }

            PacketUtil.Inner_FileCache sample;
            try {
               sample = done.get();
            } catch (Exception ignored) {
               continue;
            }

            if (sample != null) {
               tested++;
               if (sample.init_Z()) {
                  working.add(sample);
                  distinctLeases.add(init_Str_bW(sample.init_bW()));
                  if (distinctLeases.size() >= wantDistinct) {
                     break;
                  }
               }
            }
         }

         init_V_List(probeTasks);
         working.sort(Comparator.comparingLong(PacketUtil.Inner_FileCache::init_J));
         List<PacketUtil.Inner_FileCache> distinctWorking = load_List_List(working);
         int deficit = needed - distinctWorking.size();
         if (deficit > 0 && this.init_Z_J(gen)) {
            Set<String> heldLeases = new HashSet<>();

            for (PacketUtil.Inner_FileCache sample : distinctWorking) {
               heldLeases.add(init_Str_bW(sample.init_bW()));
            }

            Set<String> workingIds = new HashSet<>();

            for (PacketUtil.Inner_FileCache sample : working) {
               workingIds.add(sample.init_bW().write());
            }

            List<ProxyEntry> suspects = new ArrayList<>();

            for (ProxyEntry candidate : poolList) {
               if (!workingIds.contains(candidate.write()) && heldLeases.add(init_Str_bW(candidate))) {
                  suspects.add(candidate);
               }
            }

            for (ProxyEntry candidate : deadCandidates) {
               if (heldLeases.add(init_Str_bW(candidate))) {
                  suspects.add(candidate);
               }
            }

            int suspectCount = Math.min(suspects.size(), Math.max(deficit * 4, 32));
            if (suspectCount > 0) {
               this.init_V_J_Str(gen, "Rechecking " + suspectCount + (suspectCount == 1 ? " proxy" : " proxies") + " before sharing endpoints");
               List<PacketUtil.Inner_FileCache> recovered = this.init_List_J(
                  gen,
                  suspects.subList(0, suspectCount),
                  host,
                  port,
                  load_I_I(target),
                  deficit,
                  probePool,
                  Math.max(30000L, init_J_I_I(suspectCount, parallelism))
               );
               if (!recovered.isEmpty()) {
                  working.addAll(recovered);
                  working.sort(Comparator.comparingLong(PacketUtil.Inner_FileCache::init_J));
                  distinctWorking = load_List_List(working);
                  this.init_V_J_Str(gen, "Recovered " + recovered.size() + (recovered.size() == 1 ? " proxy" : " proxies"));
               }
            }
         }

         if (distinctWorking.isEmpty()) {
            this.init_V_J_Str(gen, "No working proxy found");
            return assignment;
         }

         int distinct = distinctWorking.size();
         long best = distinctWorking.getFirst().init_J();
         long underTargetCount = distinctWorking.stream().filter(sample -> sample.init_J() <= target).count();
         this.init_V_J_Str(
            gen,
            "Verified "
               + distinct
               + (distinct == 1 ? " working proxy" : " working proxies")
               + " ("
               + tested
               + " of "
               + poolList.size()
               + " tested, best "
               + best
               + "ms)"
         );
         if (underTargetCount < distinct) {
            this.init_V_J_Str(gen, "Only " + underTargetCount + " under " + target + "ms; slower proxies stay unique");
         }

         if (distinct < needed) {
            this.init_V_J_Str(
               gen,
               "Only " + distinct + " distinct working " + (distinct == 1 ? "proxy" : "proxies") + " for " + needed + " accounts; sharing as a last resort"
            );
         }

         assignment.putAll(init_Map_List_List(profile.init_List, distinctWorking.stream().map(PacketUtil.Inner_FileCache::init_bW).toList()));
         return assignment;
      } finally {
         probePool.shutdownNow();
      }
   }

   private static void init_V_List(List<? extends Future<?>> tasks) {
      if (tasks != null) {
         for (Future<?> task : tasks) {
            if (task != null && !task.isDone()) {
               task.cancel(true);
            }
         }
      }
   }

   private static List<PacketUtil.Inner_FileCache> load_List_List(List<PacketUtil.Inner_FileCache> sortedByPing) {
      List<PacketUtil.Inner_FileCache> distinct = new ArrayList<>(sortedByPing.size());
      Set<String> leases = new HashSet<>();

      for (PacketUtil.Inner_FileCache sample : sortedByPing) {
         if (leases.add(init_Str_bW(sample.init_bW()))) {
            distinct.add(sample);
         }
      }

      return distinct;
   }

   private List<PacketUtil.Inner_FileCache> init_List_J(
      long gen, List<ProxyEntry> suspects, String host, int port, int timeout, int quitAfter, ExecutorService probePool, long budgetMs
   ) {
      if (suspects != null && !suspects.isEmpty() && quitAfter > 0) {
         ExecutorCompletionService<PacketUtil.Inner_FileCache> service = new ExecutorCompletionService<>(probePool);
         List<Future<PacketUtil.Inner_FileCache>> tasks = new ArrayList<>(suspects.size());

         for (ProxyEntry suspect : suspects) {
            tasks.add(service.submit(() -> this.init_PacketUtil_FileCac_J(gen, suspect, host, port, timeout)));
         }

         List<PacketUtil.Inner_FileCache> recovered = new ArrayList<>();
         long deadline = System.currentTimeMillis() + budgetMs;

         for (int i = 0; i < tasks.size() && this.init_Z_J(gen); i++) {
            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0L) {
               break;
            }

            Future<PacketUtil.Inner_FileCache> done;
            try {
               done = service.poll(remaining, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
               break;
            }

            if (done == null) {
               break;
            }

            PacketUtil.Inner_FileCache sample;
            try {
               sample = done.get();
            } catch (Exception ignored) {
               continue;
            }

            if (sample != null && sample.init_Z()) {
               recovered.add(sample);
               if (recovered.size() >= quitAfter) {
                  break;
               }
            }
         }

         init_V_List(tasks);
         return List.copyOf(recovered);
      } else {
         return List.of();
      }
   }

   private PacketUtil.Inner_FileCache init_PacketUtil_FileCac_J(long gen, ProxyEntry proxy, String host, int port, int timeout) {
      long[] pings = new long[3];
      int ok = 0;
      ProxyType workingType = proxy == null ? null : proxy.init_bZ;

      for (int i = 0; i < 3; i++) {
         if (!this.init_Z_J(gen)) {
            return null;
         }

         RefQueue.Inner_DataUtil result = RefQueue.init(proxy, host, port, timeout);
         if (result.init_Z()) {
            if (result.init_bZ() != null) {
               workingType = result.init_bZ();
            }

            pings[ok++] = result.init_J();
         }
      }

      if (ok < 2) {
         return new PacketUtil.Inner_FileCache(proxy, false, 0L);
      }

      Arrays.sort(pings, 0, ok);
      ProxyEntry verified = init_bW_bW(proxy);
      if (workingType != null) {
         verified.init_bZ = workingType;
      }

      return new PacketUtil.Inner_FileCache(verified, true, pings[(ok - 1) / 2]);
   }

   private static int init_I_I(int targetPingMs) {
      return Math.max(1200, Math.min(4000, targetPingMs * 2));
   }

   private static int load_I_I(int targetPingMs) {
      return Math.max(init_I_I(targetPingMs), 3000);
   }

   static long init_J_I_I(int poolSize, int parallelism) {
      int threads = Math.max(1, parallelism);
      int waves = Math.max(1, (Math.max(0, poolSize) + threads - 1) / threads);
      return Math.min(120000L, Math.max(45000L, waves * 15000L));
   }

   private synchronized boolean init_Z_J(long gen) {
      return this.load_Z && this.tick_J == gen;
   }

   private synchronized boolean init_Z_J(long gen, long retryToken, String accountId) {
      return this.load_Z && this.tick_J == gen && this.poll_Map.getOrDefault(accountId, -1L) == retryToken;
   }

   private void init_V_J_Str(long gen, String message) {
      synchronized (this) {
         if (this.load_Z && this.tick_J == gen) {
            this.close_V_Str(message);
         }
      }
   }

   public synchronized void init_V_Str(String accountId) {
      ClassCache session = this.init_Map.get(accountId);
      if (session != null) {
         this.poll_Map.put(accountId, ++this.run);
         this.read_Set.remove(accountId);
         this.update_Map.remove(accountId);
         this.init_ArrayDeque.removeIf(next -> next.init_ClassCache() == session);
         this.load_Set.remove(session);
         session.update_V_Str("Disconnected by user");
         this.sync_V();
      }
   }

   public static boolean init_Z_ClassCache_PathHel(ClassCache.Inner_PathHelper status) {
      return status == ClassCache.Inner_PathHelper.open || status == ClassCache.Inner_PathHelper.close;
   }

   public synchronized void load_V_Str(String reason) {
      if (this.load_Z || !this.init_Map.isEmpty()) {
         this.tick_J = this.init_AtomicLong.incrementAndGet();
         this.load_Z = false;
         cf.init_V_J_Z(512L, false);
         if (this.init_ScheduledFuture != null) {
            this.init_ScheduledFuture.cancel(false);
            this.init_ScheduledFuture = null;
         }

         this.init_ArrayDeque.clear();
         this.load_Set.clear();
         this.flush_Map.clear();
         this.close_Map.clear();
         this.open_Map.clear();

         for (ClassCache session : this.init_Map.values()) {
            session.update_V_Str(reason == null ? "Multi stopped" : reason);
         }

         this.init_Map.clear();
         this.update_V();
         this.read_Map.clear();
         this.write_Map.clear();
         this.update_Map.clear();
         this.sync_Map = Map.of();
         this.poll_Map.clear();
         this.read_Set.clear();
         this.init_Set.clear();
         this.init_FieldCache = null;
         this.init_UUID = null;
         this.tick_Map.clear();
         this.flush();
         write_V();
      }
   }

   private void flush() {
      this.push_Map.clear();
      this.load_ArrayDeque.clear();
      this.pop_Map.clear();
      this.push_J = 0L;
      this.pop_J++;
      this.read_ArrayDeque.clear();
      this.init_Str = "";
      this.poll_I = -1;
      this.load_Str = "";
   }

   public synchronized void read_V_Str(String line) {
      String value = line == null ? "" : line.trim();
      if (!value.isEmpty()) {
         this.read_ArrayDeque.remove(value);
         this.read_ArrayDeque.addLast(value);

         while (this.read_ArrayDeque.size() > 50) {
            this.read_ArrayDeque.removeFirst();
         }

         AutismClientMessaging.rememberRecentChat(value);
      }
   }

   public synchronized List<String> init_List() {
      return List.copyOf(this.read_ArrayDeque);
   }

   public synchronized String load_Str() {
      return this.read_ArrayDeque.peekLast();
   }

   public synchronized void init_V() {
      this.read_ArrayDeque.clear();
   }

   public synchronized void init_V_Str_Set(String command, Set<String> scope) {
      if (command != null) {
         ClassCache source = this.init_ClassCache_Set(scope);
         if (source == null) {
            this.init_Str = "";
            this.poll_I = -1;
            this.load_Str = "";
         } else {
            int id = source.write_I_Str(command);
            if (id < 0) {
               this.init_Str = "";
               this.poll_I = -1;
               this.load_Str = "";
            } else {
               this.init_Str = source.open_Str();
               this.poll_I = id;
               this.load_Str = command;
            }
         }
      }
   }

   public synchronized PacketUtil.Inner_ByteReader init_PacketUtil_ByteRea_Str(String command) {
      if (command != null && command.equals(this.load_Str)) {
         ClassCache source = this.init_Map.get(this.init_Str);
         if (source == null) {
            return null;
         }

         ClassCache.Inner_TaskTimer suggest = source.init_ClassCache_TaskTim_I(this.poll_I);
         return suggest == null ? null : new PacketUtil.Inner_ByteReader(suggest.load(), suggest.read(), suggest.init_List());
      } else {
         return null;
      }
   }

   private ClassCache init_ClassCache_Set(Set<String> scope) {
      if (scope != null && !scope.isEmpty()) {
         for (String id : scope) {
            ClassCache session = this.init_Map.get(id);
            if (session != null && session.run_Z()) {
               return session;
            }
         }
      }

      for (ClassCache session : this.init_Map.values()) {
         if (session.run_Z()) {
            return session;
         }
      }

      return null;
   }

   public void load_V() {
      this.load_V_Str("Game closed");
      this.init_ExecutorService.shutdownNow();
      this.init_ThreadPoolExecutor.shutdown();
      this.init_ScheduledExecutorS.shutdownNow();

      try {
         // Let queued Multi reports reach disk before the game exits.
         this.init_ThreadPoolExecutor.awaitTermination(2L, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
      }
   }

   public synchronized boolean load_Z() {
      return this.load_Z;
   }

   public synchronized void init_V_ServerData(ServerData serverData) {
      if (serverData == null) {
         this.read_Str = "";
         this.load_StreamGate_CachePo = null;
      } else {
         this.read_Str = serverData.ip == null ? "" : serverData.ip.trim();
         this.load_StreamGate_CachePo = StreamGate.init(serverData);
      }
   }

   public int init_I() {
      int count = 0;

      for (ClassCache session : this.init_List) {
         if (session.tick_Z()) {
            count++;
         }
      }

      return count;
   }

   public int load_I() {
      int count = 0;

      for (ClassCache session : this.init_List) {
         if (session.run_Z()) {
            count++;
         }
      }

      return count;
   }

   public int read_I() {
      return this.init_List.size();
   }

   public String read_Str() {
      return this.load_I() + "/" + this.read_I();
   }

   public synchronized FieldCache init_FieldCache() {
      return this.init_FieldCache == null ? null : new FieldCache(this.init_FieldCache);
   }

   public synchronized void init_V_IoWorker(IoWorker policy) {
      if (this.load_Z && this.init_FieldCache != null && policy != null) {
         this.init_FieldCache.init_IoWorker = new IoWorker(policy);

         for (ClassCache session : this.init_Map.values()) {
            session.init_V_IoWorker(policy);
         }

         TaskTimer.init_TaskTimer().init_V_FieldCache(this.init_FieldCache);
         this.poll_J++;
      }
   }

   public synchronized void init_V_CachePool(CachePool config) {
      if (this.load_Z && this.init_FieldCache != null && config != null) {
         this.init_FieldCache.init_CachePool = new CachePool(config);

         for (ClassCache session : this.init_Map.values()) {
            session.init_V_CachePool(this.init_FieldCache.init_CachePool);
         }

         TaskTimer.init_TaskTimer().init_V_FieldCache(this.init_FieldCache);
         this.poll_J++;
      }
   }

   public synchronized String init_Str_Str(String accountId) {
      return this.init_Str_Str_3(accountId, this.init_CachePool().init_Str, "tpa");
   }

   public synchronized String load_Str_Str(String accountId) {
      return this.init_Str_Str_3(accountId, this.init_CachePool().load_Str, "tpa");
   }

   public synchronized String read_Str_Str(String accountId) {
      return this.init_Str_Str_3(accountId, this.init_CachePool().read_Str, "trade");
   }

   private CachePool init_CachePool() {
      return this.init_FieldCache == null ? new CachePool() : this.init_FieldCache.init_CachePool;
   }

   private String init_Str_Str_3(String accountId, String template, String kind) {
      if (!this.init_Z()) {
         return "Join the bots' server first";
      }

      ClassCache session = this.init_Map.get(accountId);
      if (session == null) {
         return "No session";
      }

      if (session.init_ClassCache_PathHel() != ClassCache.Inner_PathHelper.flush) {
         return "Bot is not in the world yet";
      }

      String me = init_Str();
      if (me.isBlank()) {
         return "Can't read your name";
      }

      String command = CachePool.init(template, session.stop_Str(), me);
      if (command.isBlank()) {
         return "No command set";
      }

      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.getConnection() != null) {
         if (command.startsWith("/")) {
            mc.getConnection().sendCommand(command.substring(1));
         } else {
            mc.getConnection().sendChat(command);
         }

         session.write_V_Str(kind);
         return "Sent";
      } else {
         return "Not connected";
      }
   }

   public synchronized void init_V_I_ListPool(int index, ListPool action) {
      if (this.load_Z && this.init_FieldCache != null) {
         this.init_FieldCache.init(index, action);
         TaskTimer.init_TaskTimer().init_V_FieldCache(this.init_FieldCache);
         this.poll_J++;
      }
   }

   public synchronized void init_V_I(int index) {
      this.init_V_I_ListPool(index, new ListPool());
   }

   public synchronized void read_V() {
      if (this.load_Z && this.init_FieldCache != null) {
         this.init_FieldCache.load_V();
         TaskTimer.init_TaskTimer().init_V_FieldCache(this.init_FieldCache);
         this.poll_J++;
      }
   }

   public synchronized void write_V_Str(String macroName) {
      if (this.init_FieldCache != null) {
         String name = macroName == null ? "" : macroName.trim();
         if (!name.equals(this.init_FieldCache.sync)) {
            this.init_FieldCache.sync = name;
            TaskTimer.init_TaskTimer().init_V_FieldCache(this.init_FieldCache);
            this.close();
            this.poll_J++;
         }
      }
   }

   public synchronized void init_V_Str_Str(String accountId, String macroName) {
      if (accountId != null) {
         this.init_I_Set_Str(Set.of(accountId), macroName);
      }
   }

   public synchronized int init_I_Set_Str(Set<String> accountIds, String macroName) {
      if (this.init_FieldCache != null && accountIds != null && !accountIds.isEmpty()) {
         String name = macroName == null ? "" : macroName.trim();
         List<FieldCache.Inner_BufferHelper> specs = this.init_FieldCache.init_List;
         int changed = 0;

         for (int i = 0; i < specs.size(); i++) {
            FieldCache.Inner_BufferHelper spec = specs.get(i);
            if (accountIds.contains(spec.init_Str()) && !spec.read().equals(name)) {
               specs.set(i, spec.init_FieldCache_BufferH_Str(name));
               changed++;
            }
         }

         if (changed > 0) {
            TaskTimer.init_TaskTimer().init_V_FieldCache(this.init_FieldCache);
            this.close();
            this.poll_J++;
         }

         return changed;
      } else {
         return 0;
      }
   }

   public synchronized String write_Str() {
      return this.init_FieldCache == null ? "" : this.init_FieldCache.sync;
   }

   public synchronized boolean read_Z() {
      if (this.init_FieldCache == null) {
         return false;
      }

      if (!this.init_FieldCache.sync.isBlank()) {
         return true;
      }

      for (FieldCache.Inner_BufferHelper spec : this.init_FieldCache.init_List) {
         if (!spec.read().isBlank()) {
            return true;
         }
      }

      return false;
   }

   public synchronized boolean init_Z_Set(Set<String> requestedScope) {
      Set<String> scope = this.init_Set_Set(requestedScope);
      if (scope == null) {
         return true;
      }

      if (scope.isEmpty()) {
         return this.read_Z();
      }

      for (String accountId : scope) {
         if (!this.update_Str_Str(accountId).isBlank()) {
            return true;
         }
      }

      return false;
   }

   public synchronized String write_Str_Str(String accountId) {
      if (this.init_FieldCache == null) {
         return "";
      }

      String published = this.sync_Map.get(accountId);
      return published == null ? this.init_FieldCache.sync : published;
   }

   public String update_Str_Str(String accountId) {
      return accountId == null ? "" : this.sync_Map.getOrDefault(accountId, "");
   }

   private void close() {
      FieldCache profile = this.init_FieldCache;
      if (profile != null && !profile.init_List.isEmpty()) {
         Map<String, String> next = new HashMap<>();

         for (FieldCache.Inner_BufferHelper spec : profile.init_List) {
            next.put(spec.init_Str(), spec.read().isBlank() ? profile.sync : spec.read());
         }

         this.sync_Map = Map.copyOf(next);
      } else {
         this.sync_Map = Map.of();
      }
   }

   public synchronized List<String> init_List_Str(String macroName) {
      if (macroName != null && !macroName.isBlank()) {
         aM macro = aN.init_aN().init_aM_Str(macroName);
         return macro == null ? List.of("Macro not found") : List.copyOf(ByteReader.init(macro));
      } else {
         return List.of();
      }
   }

   public synchronized PacketUtil.Inner_CachePool init_PacketUtil_CachePo_Set(Set<String> scope) {
      return this.init_PacketUtil_CachePo_Set_Z(scope, false);
   }

   public synchronized PacketUtil.Inner_CachePool init_PacketUtil_CachePo_Set_Z(Set<String> scope, boolean idleOnly) {
      Map<String, aM> snapshots = new HashMap<>();
      Set<String> analyzed = new HashSet<>();
      PacketUtil.Inner_StateCache stagger = new PacketUtil.Inner_StateCache();
      PacketUtil.Inner_CachePool result = this.init_PacketUtil_CachePo_Str(
         "Run macro",
         scope,
         session -> {
            if (idleOnly && session.set_Z()) {
               return "Macro already running; not restarted";
            }

            String name = this.write_Str_Str(session.open_Str());
            if (name != null && !name.isBlank()) {
               aM copy = snapshots.computeIfAbsent(name, n -> {
                  aM found = aN.init_aN().init_aM_Str(n);
                  return found == null ? null : found.init_aM();
               });
               if (copy == null) {
                  return "Macro not found: " + name;
               }

               if (analyzed.add(name)) {
                  this.init_V_aM(copy);
               }

               this.update_Map.put(session.open_Str(), PacketUtil.Inner_JsonUtil.init_PacketUtil_JsonUti());
               ClassCache.Inner_PathHelper status = session.init_ClassCache_PathHel();
               if ((status == ClassCache.Inner_PathHelper.open || status == ClassCache.Inner_PathHelper.close)
                  && !autismclient.util.macro.ClassCache.init(copy.init_List, 0, copy.init_Z)) {
                  return "Session is not connected; queued for retry";
               }

               session.init_V_aM_J(copy, stagger.init_J());
               return "Sent";
            } else {
               return "No macro assigned";
            }
         }
      );
      stagger.init_V();
      return result;
   }

   private void init_V_aM(aM macro) {
      List<String> warnings = ByteReader.init(macro);
      if (!warnings.isEmpty()) {
         String name = update(macro.init_Str, 40);
         this.close_V_Str("Warning: macro \"" + name + "\" - " + warnings.size() + " item(s) may not run as intended:");

         for (String line : warnings) {
            this.close_V_Str(line);
         }
      }
   }

   public synchronized PacketUtil.Inner_CachePool init_PacketUtil_CachePo_aM(aM macro) {
      return this.init_PacketUtil_CachePo_aM_Set(macro, null);
   }

   public synchronized PacketUtil.Inner_CachePool init_PacketUtil_CachePo_aM_Set(aM macro, Set<String> scope) {
      if (macro != null && !macro.init_List.isEmpty()) {
         aM copy = macro.init_aM();
         if (copy.init_Str == null || copy.init_Str.isBlank()) {
            copy.init_Str = "Editor macro";
         }

         this.init_V_aM(copy);
         PacketUtil.Inner_JsonUtil intent = PacketUtil.Inner_JsonUtil.init(copy);
         PacketUtil.Inner_StateCache stagger = new PacketUtil.Inner_StateCache();
         PacketUtil.Inner_CachePool result = this.init_PacketUtil_CachePo_Str(
            "Run for Multi",
            scope,
            session -> {
               this.update_Map.put(session.open_Str(), intent);
               ClassCache.Inner_PathHelper status = session.init_ClassCache_PathHel();
               if ((status == ClassCache.Inner_PathHelper.open || status == ClassCache.Inner_PathHelper.close)
                  && !autismclient.util.macro.ClassCache.init(copy.init_List, 0, copy.init_Z)) {
                  return "Session is not connected; queued for retry";
               }

               session.init_V_aM_J(copy, stagger.init_J());
               return "Sent";
            }
         );
         stagger.init_V();
         return result;
      } else {
         return new PacketUtil.Inner_CachePool(0, 0, 1, List.of("Macro has no actions"));
      }
   }

   public synchronized boolean load_Z(String accountId, String macroName) {
      if (accountId != null && macroName != null && !macroName.isBlank()) {
         PacketUtil.Inner_JsonUtil intent = this.update_Map.get(accountId);
         if (intent != null && intent.init_PacketUtil_NetHelp() == PacketUtil.Inner_NetHelper.load) {
            aM direct = intent.init_aM();
            if (direct != null && macroName.equals(direct.init_Str)) {
               ClassCache session = this.init_Map.get(accountId);
               return session == null || !session.tick_Z();
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public synchronized PacketUtil.Inner_CachePool load_PacketUtil_CachePo_Set(Set<String> scope) {
      return this.init_PacketUtil_CachePo_Str("Stop macro", scope, session -> {
         this.update_Map.remove(session.open_Str());
         session.sync_V();
         return "Sent";
      });
   }

   public synchronized PacketUtil.Inner_CachePool read_PacketUtil_CachePo_Set(Set<String> requestedScope) {
      Set<String> scope = this.init_Set_Set(requestedScope);
      return scope == null ? load_PacketUtil_CachePo() : this.init_PacketUtil_CachePo_Set(scope);
   }

   public synchronized PacketUtil.Inner_CachePool load(aM macro, Set<String> requestedScope) {
      Set<String> scope = this.init_Set_Set(requestedScope);
      return scope == null ? load_PacketUtil_CachePo() : this.init_PacketUtil_CachePo_aM_Set(macro, scope);
   }

   public synchronized PacketUtil.Inner_CachePool write_PacketUtil_CachePo_Set(Set<String> requestedScope) {
      Set<String> scope = this.init_Set_Set(requestedScope);
      return scope == null ? load_PacketUtil_CachePo() : this.load_PacketUtil_CachePo_Set(scope);
   }

   public synchronized boolean init_Z_Str_Set(String macroName, Set<String> requestedScope) {
      if (macroName != null && !macroName.isBlank()) {
         Set<String> scope = this.init_Set_Set(requestedScope);
         return scope == null ? false : this.load_Z(macroName, scope);
      } else {
         return false;
      }
   }

   public synchronized boolean load_Z(String macroName, Set<String> scope) {
      if (macroName != null && !macroName.isBlank()) {
         boolean scoped = scope != null && !scope.isEmpty();

         for (Entry<String, ClassCache> entry : this.init_Map.entrySet()) {
            if (!scoped || scope.contains(entry.getKey())) {
               ClassCache session = entry.getValue();
               if ((session.tick_Z() || session.add_Z()) && session.set_Z()) {
                  PacketUtil.Inner_JsonUtil intent = this.update_Map.get(entry.getKey());
                  String intended = intent == null
                     ? ""
                     : (
                        intent.init_PacketUtil_NetHelp() == PacketUtil.Inner_NetHelper.load
                           ? (intent.init_aM() == null ? "" : intent.init_aM().init_Str)
                           : this.write_Str_Str(entry.getKey())
                     );
                  if (macroName.equalsIgnoreCase(intended) || macroName.equalsIgnoreCase(session.clear_Str())) {
                     return true;
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private Set<String> init_Set_Set(Set<String> requestedScope) {
      String pov = ValueSink.init_Str();
      return init_Set_Str(pov, pov == null || this.init_Map.containsKey(pov), requestedScope);
   }

   static Set<String> init_Set_Str(String povAccountId, boolean povSessionExists, Set<String> requestedScope) {
      if (povAccountId == null) {
         return requestedScope;
      } else {
         return povSessionExists ? Set.of(povAccountId) : null;
      }
   }

   private static PacketUtil.Inner_CachePool load_PacketUtil_CachePo() {
      return new PacketUtil.Inner_CachePool(0, 0, 1, List.of("The POV bot is no longer available"));
   }

   private void init_V_Str_ClassCache(String accountId, ClassCache session) {
      PacketUtil.Inner_JsonUtil intent = this.update_Map.get(accountId);
      if (intent != null) {
         if (intent.init_PacketUtil_NetHelp() == PacketUtil.Inner_NetHelper.load) {
            aM direct = intent.init_aM();
            if (direct != null && direct.init_List != null && !direct.init_List.isEmpty()) {
               session.init_V_aM(direct);
            }
         } else {
            String name = this.write_Str_Str(accountId);
            if (name != null && !name.isBlank()) {
               aM macro = aN.init_aN().init_aM_Str(name);
               if (macro != null) {
                  if (this.init_FieldCache != null && this.init_FieldCache.init_FieldCache_DataUti == FieldCache.Inner_DataUtil.read) {
                     session.load_V_aM(macro.init_aM());
                  } else {
                     session.init_V_aM(macro.init_aM());
                  }
               }
            }
         }
      }
   }

   private void init_V_FieldCache(FieldCache profile, FieldCache.Inner_BufferHelper spec, ClassCache session) {
      if (profile != null && spec != null && session != null && profile.init_FieldCache_DataUti == FieldCache.Inner_DataUtil.read) {
         String name = spec.read().isBlank() ? profile.sync : spec.read();
         if (name != null && !name.isBlank()) {
            aM macro = aN.init_aN().init_aM_Str(name);
            if (macro != null) {
               this.update_Map.put(spec.init_Str(), PacketUtil.Inner_JsonUtil.init_PacketUtil_JsonUti());
               session.load_V_aM(macro.init_aM());
            }
         }
      }
   }

   public List<ClassCache.Inner_FieldCache> load_List() {
      this.close_J = System.nanoTime() + flush_J;
      return this.load_List;
   }

   public KeyHolder init_KeyHolder() {
      FieldCache profile = this.init_FieldCache();
      Map<String, String> names = new LinkedHashMap<>();

      for (ClassCache.Inner_FieldCache snapshot : this.load_List()) {
         if (snapshot != null && snapshot.init_Str() != null && !snapshot.init_Str().isBlank() && snapshot.load_Str() != null && !snapshot.load_Str().isBlank()
            )
          {
            names.putIfAbsent(snapshot.init_Str(), snapshot.load_Str());
         }
      }

      return profile == null ? new KeyHolder(List.of(), null) : PathHelper.init(profile, names);
   }

   public List<PacketUtil.Inner_StreamUtil> read_List() {
      List<ClassCache> live = this.init_List;
      List<PacketUtil.Inner_StreamUtil> out = new ArrayList<>(live.size());

      for (ClassCache session : live) {
         if (session != null) {
            String name = session.stop_Str();
            out.add(new PacketUtil.Inner_StreamUtil(session.open_Str(), name != null && !name.isBlank() ? name : session.open_Str()));
         }
      }

      return List.copyOf(out);
   }

   public Set<String> init_Set() {
      List<ClassCache> live = this.init_List;
      if (live.isEmpty()) {
         return Set.of();
      }

      Set<String> names = new HashSet<>(live.size() * 2);

      for (ClassCache session : live) {
         if (session != null) {
            String name = session.stop_Str();
            if (name != null && !name.isBlank()) {
               names.add(name.toLowerCase(Locale.ROOT));
            }
         }
      }

      return names;
   }

   public static PacketUtil.Inner_StreamUtil init_PacketUtil_StreamU_Collection_Str(Collection<PacketUtil.Inner_StreamUtil> bots, String botName) {
      if (bots != null && botName != null) {
         String want = botName.trim();
         if (want.isEmpty()) {
            return null;
         }

         for (PacketUtil.Inner_StreamUtil bot : bots) {
            if (bot != null && bot.load() != null && bot.load().equalsIgnoreCase(want)) {
               return bot;
            }
         }

         for (PacketUtil.Inner_StreamUtil bot : bots) {
            if (bot != null && bot.init() != null && bot.init().equalsIgnoreCase(want)) {
               return bot;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public static Set<String> init_Set_Collection_StrArr(Collection<PacketUtil.Inner_StreamUtil> bots, String... accountIdsOrName) {
      if (bots != null && accountIdsOrName != null && accountIdsOrName.length != 0) {
         Set<String> resolved = new LinkedHashSet<>();

         for (String accountIdOrName : accountIdsOrName) {
            PacketUtil.Inner_StreamUtil bot = init_PacketUtil_StreamU_Collection_Str(bots, accountIdOrName);
            if (bot != null && bot.init() != null) {
               String accountId = bot.init().trim();
               if (!accountId.isEmpty()) {
                  resolved.add(accountId);
               }
            }
         }

         return resolved.isEmpty() ? Set.of() : Collections.unmodifiableSet(resolved);
      } else {
         return Set.of();
      }
   }

   public Set<String> init_Set_Str(String botName) {
      if (botName != null && !botName.isBlank()) {
         PacketUtil.Inner_StreamUtil bot = init_PacketUtil_StreamU_Collection_Str(this.read_List(), botName);
         return bot == null ? null : Set.of(bot.init());
      } else {
         return Set.of();
      }
   }

   private void init_V_Z(boolean force) {
      if (force || System.nanoTime() - this.close_J < 0L) {
         List<ClassCache> live = this.init_List;
         if (live.isEmpty()) {
            this.load_List = List.of();
         } else {
            List<ClassCache.Inner_FieldCache> previous = this.load_List;
            ArrayList<ClassCache.Inner_FieldCache> changed = null;

            for (int i = 0; i < live.size(); i++) {
               ClassCache.Inner_FieldCache next = live.get(i).init_ClassCache_FieldCa();
               if (changed != null || i >= previous.size() || previous.get(i) != next) {
                  if (changed == null) {
                     changed = new ArrayList<>(live.size());

                     for (int j = 0; j < i; j++) {
                        changed.add(previous.get(j));
                     }
                  }

                  changed.add(next);
               }
            }

            if (changed != null || previous.size() != live.size()) {
               if (changed == null) {
                  changed = new ArrayList<>(previous.subList(0, live.size()));
               }

               this.load_List = List.copyOf(changed);
            }
         }
      }
   }

   public long init_J() {
      return this.open_J;
   }

   public long load_J() {
      return this.poll_J;
   }

   public long read_J() {
      return this.pop_J;
   }

   public synchronized List<PacketUtil.Inner_BufferHelper> init_List_Set(Set<String> scope) {
      if (scope != null && !scope.isEmpty()) {
         if (scope.size() == 1) {
            String id = scope.iterator().next();
            ArrayDeque<PacketUtil.Inner_ConfigStore> acc = this.push_Map.get(id);
            if (acc == null) {
               return List.of();
            }

            List<PacketUtil.Inner_BufferHelper> out = new ArrayList<>(acc.size());

            for (PacketUtil.Inner_ConfigStore m : acc) {
               out.add(new PacketUtil.Inner_BufferHelper(m.init_J, m.load_J, m.init_Component, Map.of(id, m.init_Component), false, 1, m.load_Str()));
            }

            return out;
         } else {
            record Inner_DataUtil(String init_Str, PacketUtil.Inner_ConfigStore init_PacketUtil_ConfigS) {
            }

            List<Inner_DataUtil> gathered = new ArrayList<>();

            for (String id : scope) {
               ArrayDeque<PacketUtil.Inner_ConfigStore> acc = this.push_Map.get(id);
               if (acc != null) {
                  for (PacketUtil.Inner_ConfigStore m : acc) {
                     gathered.add(new Inner_DataUtil(id, m));
                  }
               }
            }

            gathered.sort(Comparator.comparingLong(o -> o.init_PacketUtil_ConfigS().init_J()));
            Map<String, PacketUtil.Inner_HashUtil> recent = new HashMap<>();
            List<PacketUtil.Inner_HashUtil> ordered = new ArrayList<>();

            for (Inner_DataUtil o : gathered) {
               String text = o.init_PacketUtil_ConfigS().init_Str();
               String src = o.init_PacketUtil_ConfigS().load_Str();
               String key = read_Str_Str_Str(src, text);
               PacketUtil.Inner_HashUtil prev = recent.get(key);
               if (prev != null && !text.isBlank() && init_Z_J_J(prev.load_J, o.init_PacketUtil_ConfigS().load_J())) {
                  prev.init_I++;
                  prev.init_Map.putIfAbsent(o.init_Str(), o.init_PacketUtil_ConfigS().init_Component());
               } else {
                  PacketUtil.Inner_HashUtil m = new PacketUtil.Inner_HashUtil(
                     o.init_PacketUtil_ConfigS().init_J(), o.init_PacketUtil_ConfigS().load_J(), text, src, false, o.init_PacketUtil_ConfigS().init_Component()
                  );
                  m.init_Map.put(o.init_Str(), o.init_PacketUtil_ConfigS().init_Component());
                  ordered.add(m);
                  if (!text.isBlank()) {
                     recent.put(key, m);
                  }
               }
            }

            int startIndex = Math.max(0, ordered.size() - 100);
            List<PacketUtil.Inner_BufferHelper> out = new ArrayList<>(ordered.size() - startIndex);

            for (int i = startIndex; i < ordered.size(); i++) {
               PacketUtil.Inner_HashUtil m = ordered.get(i);
               out.add(
                  new PacketUtil.Inner_BufferHelper(
                     m.init_J, m.load_J, m.init_Component, Collections.unmodifiableMap(new LinkedHashMap<>(m.init_Map)), false, m.init_I, m.load_Str
                  )
               );
            }

            return out;
         }
      } else {
         List<PacketUtil.Inner_BufferHelper> out = new ArrayList<>(this.load_ArrayDeque.size());

         for (PacketUtil.Inner_HashUtil m : this.load_ArrayDeque) {
            out.add(
               new PacketUtil.Inner_BufferHelper(
                  m.init_J, m.load_J, m.init_Component, Collections.unmodifiableMap(new LinkedHashMap<>(m.init_Map)), m.init_Z, m.init_I, m.load_Str
               )
            );
         }

         return out;
      }
   }

   public synchronized String init_Str_Str_Str(String accountId, String command) {
      ClassCache session = this.init_Map.get(accountId);
      if (session == null) {
         return "No session";
      }

      try {
         return session.read_Str_Str(command);
      } catch (RuntimeException error) {
         return error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
      }
   }

   public synchronized PacketUtil.Inner_CachePool init_PacketUtil_CachePo_Str(String line) {
      return this.init_PacketUtil_CachePo_Str_Set(line, null);
   }

   public synchronized PacketUtil.Inner_CachePool init_PacketUtil_CachePo_Str_Set(String line, Set<String> targets) {
      if (line != null && autismclient.commands.DataUtil.init_Z(line.trim())) {
         return this.load_PacketUtil_CachePo(line, targets);
      }

      if (this.init_Map.isEmpty()) {
         return this.load_PacketUtil_CachePo_Str("Send");
      }

      boolean scoped = targets != null && !targets.isEmpty();
      int sent = 0;
      int skipped = 0;
      int failed = 0;
      List<String> details = new ArrayList<>();

      for (Entry<String, ClassCache> entry : this.init_Map.entrySet()) {
         if (!scoped || targets.contains(entry.getKey())) {
            ClassCache session = entry.getValue();

            String result;
            try {
               result = session.read_Str_Str(line);
            } catch (RuntimeException error) {
               result = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
            }

            if ("Sent".equals(result) || result.startsWith("Queued ")) {
               sent++;
            } else if (!result.contains("not ready") && !result.contains("Blocked") && !result.contains("key") && !result.contains("Rate limited")) {
               failed++;
               details.add(session.init_ClassCache_FieldCa().load_Str() + ": " + result);
            } else {
               skipped++;
               details.add(session.init_ClassCache_FieldCa().load_Str() + ": " + result);
            }
         }
      }

      PacketUtil.Inner_CachePool result = new PacketUtil.Inner_CachePool(sent, skipped, failed, List.copyOf(details));
      if (failed > 0) {
         this.close_V_Str(result.init_Str());
         this.load_V_List(details);
      }

      return result;
   }

   public synchronized PacketUtil.Inner_CachePool load_PacketUtil_CachePo(String line, Set<String> targets) {
      String body = autismclient.commands.DataUtil.init_Str(line).trim();
      if (body.isEmpty()) {
         this.close_V_Str("Empty command");
         return new PacketUtil.Inner_CachePool(0, 0, 0, List.of());
      }

      if (this.init_Map.isEmpty()) {
         return this.load_PacketUtil_CachePo_Str("Command");
      }

      int space = body.indexOf(32);
      String name = (space < 0 ? body : body.substring(0, space)).toLowerCase(Locale.ROOT);
      String args = space < 0 ? "" : body.substring(space + 1).trim();
      String deny = BufferHelper.init_Str(name, args);
      if (deny != null) {
         this.close_V_Str(name + ": " + deny);
         return new PacketUtil.Inner_CachePool(0, 0, 0, List.of());
      }

      boolean scoped = targets != null && !targets.isEmpty();
      int sent = 0;
      int skipped = 0;
      int failed = 0;
      List<String> details = new ArrayList<>();

      for (Entry<String, ClassCache> entry : this.init_Map.entrySet()) {
         if (!scoped || targets.contains(entry.getKey())) {
            ClassCache session = entry.getValue();

            String result;
            try {
               result = session.load_Str_Str_Str(name, args);
            } catch (RuntimeException error) {
               result = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
            }

            if ("Sent".equals(result)) {
               sent++;
            } else if (!result.contains("not ready") && !result.contains("Blocked") && !result.contains("Rate limited")) {
               failed++;
               details.add(session.init_ClassCache_FieldCa().load_Str() + ": " + result);
            } else {
               skipped++;
               details.add(session.init_ClassCache_FieldCa().load_Str() + ": " + result);
            }
         }
      }

      PacketUtil.Inner_CachePool result = new PacketUtil.Inner_CachePool(sent, skipped, failed, List.copyOf(details));
      if (failed > 0) {
         this.close_V_Str(name + " -> " + result.init_Str());
         this.load_V_List(details);
      }

      return result;
   }

   public synchronized PacketUtil.Inner_CachePool init_PacketUtil_CachePo() {
      return this.update_PacketUtil_CachePo_Set(null);
   }

   public synchronized PacketUtil.Inner_CachePool update_PacketUtil_CachePo_Set(Set<String> targets) {
      return this.init_PacketUtil_CachePo_Str("Move", targets, ClassCache::add_Str);
   }

   public synchronized PacketUtil.Inner_CachePool init_PacketUtil_CachePo_ListPool(ListPool action) {
      return this.init_PacketUtil_CachePo_ListPool_Set(action, null);
   }

   public synchronized PacketUtil.Inner_CachePool init_PacketUtil_CachePo_ListPool_Set(ListPool action, Set<String> targets) {
      if (action != null && !action.init_Z()) {
         ListPool sendAction = new ListPool(action);
         List<PacketUtil.Inner_UiHelper> prepared = new ArrayList<>();

         for (ListPool.Inner_DataUtil step : sendAction.init_List) {
            Class<? extends Packet<?>> packetClass = init_Class_Str(step.init_Str());
            if (packetClass == null) {
               PacketUtil.Inner_CachePool result = new PacketUtil.Inner_CachePool(0, 0, this.init_Map.size(), List.of("Missing packet"));
               this.close_V_Str("Missing packet");
               return result;
            }

            prepared.add(new PacketUtil.Inner_UiHelper(packetClass, step.load()));
         }

         return this.init_PacketUtil_CachePo_Str(sendAction.init_Str_I(0), targets, session -> {
            for (PacketUtil.Inner_UiHelper stepx : prepared) {
               String resultx = session.init_Str_Class_Str(stepx.init_Class(), stepx.init_Str());
               if (!"Sent".equals(resultx)) {
                  return resultx;
               }
            }

            return "Sent";
         });
      } else {
         int skipped = targets != null && !targets.isEmpty() ? (int)targets.stream().filter(this.init_Map::containsKey).count() : this.init_Map.size();
         PacketUtil.Inner_CachePool result = new PacketUtil.Inner_CachePool(0, skipped, 0, List.of("Empty slot"));
         this.close_V_Str("Empty slot");
         return result;
      }
   }

   public synchronized PacketUtil.Inner_CachePool init_PacketUtil_CachePo_Class_Str(Class<? extends Packet<?>> packetClass, String arguments) {
      return this.init_PacketUtil_CachePo_Str_Function("Packet", session -> session.init_Str_Class_Str(packetClass, arguments));
   }

   private PacketUtil.Inner_CachePool init_PacketUtil_CachePo_Str_Function(String label, Function<ClassCache, String> sender) {
      return this.init_PacketUtil_CachePo_Str(label, null, sender);
   }

   private PacketUtil.Inner_CachePool init_PacketUtil_CachePo_Str(String label, Set<String> targets, Function<ClassCache, String> sender) {
      if (this.init_Map.isEmpty()) {
         return this.load_PacketUtil_CachePo_Str(label);
      }

      boolean scoped = targets != null && !targets.isEmpty();
      int sent = 0;
      int skipped = 0;
      int failed = 0;
      List<String> details = new ArrayList<>();

      for (Entry<String, ClassCache> entry : this.init_Map.entrySet()) {
         if (!scoped || targets.contains(entry.getKey())) {
            ClassCache session = entry.getValue();

            String result;
            try {
               result = sender.apply(session);
            } catch (RuntimeException error) {
               result = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
            }

            if ("Sent".equals(result)) {
               sent++;
            } else if (read_Z_Str(result)) {
               skipped++;
               details.add(session.init_ClassCache_FieldCa().load_Str() + ": " + result);
            } else {
               failed++;
               details.add(session.init_ClassCache_FieldCa().load_Str() + ": " + result);
            }
         }
      }

      PacketUtil.Inner_CachePool result = new PacketUtil.Inner_CachePool(sent, skipped, failed, List.copyOf(details));
      if (failed > 0) {
         this.close_V_Str(update(label, 24) + ": " + result.init_Str());
         this.load_V_List(details);
      }

      return result;
   }

   private PacketUtil.Inner_CachePool load_PacketUtil_CachePo_Str(String label) {
      int waiting = this.init_FieldCache == null ? 0 : this.init_FieldCache.init_List.size();
      return new PacketUtil.Inner_CachePool(0, waiting, 0, List.of("Sessions are still starting"));
   }

   private void load_V_List(List<String> details) {
      if (details != null && !details.isEmpty()) {
         int shown = Math.min(5, details.size());

         for (int i = 0; i < shown; i++) {
            this.close_V_Str(details.get(i));
         }

         if (details.size() > shown) {
            this.close_V_Str(details.size() - shown + " more sessions omitted");
         }
      }
   }

   public synchronized PacketUtil.Inner_CachePool sync_PacketUtil_CachePo_Set(Set<String> scope) {
      return this.init_PacketUtil_CachePo_Str("Use", scope, ClassCache::get_Str);
   }

   public synchronized PacketUtil.Inner_CachePool flush_PacketUtil_CachePo_Set(Set<String> scope) {
      return this.init_PacketUtil_CachePo_Str("Close", scope, ClassCache::run_Str);
   }

   public synchronized PacketUtil.Inner_CachePool close_PacketUtil_CachePo_Set(Set<String> scope) {
      return this.init_PacketUtil_CachePo_Str("Close (silent)", scope, ClassCache::pop_Str);
   }

   public ClassCache.Inner_ByteReader init_ClassCache_ByteRea_Str(String accountId) {
      ClassCache session = this.load_Map.get(accountId);
      return session == null ? null : session.init_ClassCache_ByteRea();
   }

   public ClassCache.Inner_ByteReader load_ClassCache_ByteRea_Str(String accountId) {
      ClassCache session = this.load_Map.get(accountId);
      return session == null ? null : session.load_ClassCache_ByteRea();
   }

   public long init_J_Str(String accountId) {
      ClassCache session = this.load_Map.get(accountId);
      return session == null ? -1L : session.open_J();
   }

   public String init_Str_Str_2(String accountId, int handler, BufferHelper.Inner_DataUtil spec) {
      ClassCache session = this.load_Map.get(accountId);
      if (session != null && spec != null) {
         return init_Z_Str_ClassCache(accountId, session) ? "Macro controls POV bot" : session.init_Str_I(handler, spec.init_I(), spec.init_ContainerInput());
      } else {
         return "No session";
      }
   }

   private static boolean init_Z_Str_ClassCache(String accountId, ClassCache session) {
      return session != null && ValueSink.init_Z(accountId) && session.put_Z();
   }

   public int init_I_Str_I(String accountId, int handler) {
      ClassCache session = this.load_Map.get(accountId);
      return session == null ? -1 : session.init_I_I(handler);
   }

   public int load_I(String accountId, int handler) {
      ClassCache session = this.load_Map.get(accountId);
      return session == null ? -1 : session.read_I_I(handler);
   }

   public synchronized boolean init_Z_Str_Z(String accountId, boolean on) {
      if (accountId == null) {
         return false;
      }

      if (on) {
         if (!this.init_Set.contains(accountId) && this.init_Set.size() >= 1) {
            return false;
         }

         this.init_Set.add(accountId);
      } else {
         this.init_Set.remove(accountId);
      }

      ClassCache session = this.init_Map.get(accountId);
      if (session != null) {
         session.update_V_Z(on);
      }

      return true;
   }

   public boolean load_Z_Str(String accountId) {
      return accountId != null && this.init_Set.contains(accountId);
   }

   ClassCache init_ClassCache_Str(String accountId) {
      return accountId == null ? null : this.load_Map.get(accountId);
   }

   public UUID load_UUID_Str(String accountId) {
      ClassCache session = this.init_ClassCache_Str(accountId);
      return session == null ? null : session.init_UUID();
   }

   public static GameProfile init_GameProfile_UUID(UUID uuid) {
      PacketUtil mgr = load_PacketUtil();
      if (mgr != null && uuid != null && mgr.load_Z()) {
         for (ClassCache session : mgr.load_Map.values()) {
            if (uuid.equals(session.init_UUID())) {
               return session.init_GameProfile();
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public int write_I() {
      return this.init_Set.size();
   }

   public int update_I() {
      return 1;
   }

   private void write_V_ClassCache(ClassCache session) {
      if (session != null && this.init_Set.contains(session.open_Str())) {
         session.update_V_Z(true);
      }
   }

   public String sync_Str_Str(String accountId) {
      ClassCache session = this.load_Map.get(accountId);
      if (session != null) {
         String addr = session.read_Str();
         if (addr != null && !addr.isBlank()) {
            return addr;
         }
      }

      FieldCache profile = this.init_FieldCache();
      return profile != null && profile.update_Str != null && !profile.update_Str.isBlank() ? profile.update_Str : null;
   }

   public int init_I_Str(String accountId) {
      ClassCache session = this.load_Map.get(accountId);
      return session == null ? -1 : session.update_I();
   }

   public String init_Str_Str_I(String accountId, int index) {
      ClassCache session = this.load_Map.get(accountId);
      if (session == null) {
         return "No session";
      } else {
         return init_Z_Str_ClassCache(accountId, session)
            ? "Macro controls POV bot"
            : session.load_Str_Str_Str("change-slot", String.valueOf(Math.max(1, Math.min(9, index + 1))));
      }
   }

   public String load_Str(String accountId, int index) {
      ClassCache session = this.load_Map.get(accountId);
      if (session == null) {
         return "No session";
      }

      if (init_Z_Str_ClassCache(accountId, session)) {
         return "Macro controls POV bot";
      }

      String selected = session.load_Str_Str_Str("change-slot", String.valueOf(Math.max(1, Math.min(9, index + 1))));
      return !"Sent".equals(selected) ? selected : session.load_Str_Str_Str("use", "");
   }

   public PacketUtil.Inner_CachePool init_PacketUtil_CachePo_List(List<String> accountIds, int handler, BufferHelper.Inner_DataUtil spec) {
      int sent = 0;
      int skipped = 0;
      int failed = 0;
      if (accountIds != null && spec != null) {
         for (String accountId : accountIds) {
            ClassCache session = this.load_Map.get(accountId);
            if (session != null && !init_Z_Str_ClassCache(accountId, session) && (handler < 0 || handler < session.sync_I())) {
               if ("Sent".equals(session.init_Str_I(handler, spec.init_I(), spec.init_ContainerInput()))) {
                  sent++;
               } else {
                  failed++;
               }
            } else {
               skipped++;
            }
         }
      }

      return new PacketUtil.Inner_CachePool(sent, skipped, failed, List.of());
   }

   public String read_Str_Str_I(String accountId, int buttonId) {
      ClassCache session = this.load_Map.get(accountId);
      if (session == null) {
         return "No session";
      } else {
         return init_Z_Str_ClassCache(accountId, session) ? "Macro controls POV bot" : session.init_Str_I(buttonId);
      }
   }

   public PacketUtil.Inner_CachePool init_PacketUtil_CachePo_List_2(List<String> accountIds, int buttonId, String typeId) {
      return this.init_PacketUtil_CachePo_List_5(accountIds, typeId, session -> session.init_Str_I(buttonId));
   }

   public String write(String accountId, int index) {
      ClassCache session = this.load_Map.get(accountId);
      if (session == null) {
         return "No session";
      } else {
         return init_Z_Str_ClassCache(accountId, session) ? "Macro controls POV bot" : session.load_Str_I(index);
      }
   }

   public PacketUtil.Inner_CachePool load(List<String> accountIds, int index, String typeId) {
      return this.init_PacketUtil_CachePo_List_5(accountIds, typeId, session -> session.load_Str_I(index));
   }

   public String init_Str_Str(String accountId, int primaryId, int secondaryId) {
      ClassCache session = this.load_Map.get(accountId);
      if (session == null) {
         return "No session";
      } else {
         return init_Z_Str_ClassCache(accountId, session) ? "Macro controls POV bot" : session.init_Str_I_I(primaryId, secondaryId);
      }
   }

   public PacketUtil.Inner_CachePool init_PacketUtil_CachePo_List(List<String> accountIds, int primaryId, int secondaryId, String typeId) {
      return this.init_PacketUtil_CachePo_List_5(accountIds, typeId, session -> session.init_Str_I_I(primaryId, secondaryId));
   }

   public String init_Str_Str_4(String accountId, RecipeDisplayId id, boolean all) {
      ClassCache session = this.load_Map.get(accountId);
      if (session == null) {
         return "No session";
      } else {
         return init_Z_Str_ClassCache(accountId, session) ? "Macro controls POV bot" : session.init_Str_RecipeDisplayId_Z(id, all);
      }
   }

   public PacketUtil.Inner_CachePool init_PacketUtil_CachePo_List_2(List<String> accountIds, RecipeDisplayId id, boolean all, String typeId) {
      return this.init_PacketUtil_CachePo_List_5(accountIds, typeId, session -> session.init_Str_RecipeDisplayId_Z(id, all));
   }

   public String load_Str(String accountId, String name) {
      ClassCache session = this.load_Map.get(accountId);
      if (session == null) {
         return "No session";
      } else {
         return init_Z_Str_ClassCache(accountId, session) ? "Macro controls POV bot" : session.write_Str_Str(name);
      }
   }

   public PacketUtil.Inner_CachePool init_PacketUtil_CachePo_List_4(List<String> accountIds, String name, String typeId) {
      return this.init_PacketUtil_CachePo_List_5(accountIds, typeId, session -> session.write_Str_Str(name));
   }

   private PacketUtil.Inner_CachePool init_PacketUtil_CachePo_List_5(List<String> accountIds, String typeId, Function<ClassCache, String> action) {
      int sent = 0;
      int skipped = 0;
      int failed = 0;
      if (accountIds != null) {
         for (String accountId : accountIds) {
            ClassCache session = this.load_Map.get(accountId);
            if (session != null && !init_Z_Str_ClassCache(accountId, session) && (typeId == null || typeId.isEmpty() || typeId.equals(session.push_Str()))) {
               if ("Sent".equals(action.apply(session))) {
                  sent++;
               } else {
                  failed++;
               }
            } else {
               skipped++;
            }
         }
      }

      return new PacketUtil.Inner_CachePool(sent, skipped, failed, List.of());
   }

   public PacketUtil.Inner_CachePool init_PacketUtil_CachePo_List_I(List<String> accountIds, int index) {
      return this.init_PacketUtil_CachePo_List_3(accountIds, index, false);
   }

   public PacketUtil.Inner_CachePool load(List<String> accountIds, int index) {
      return this.init_PacketUtil_CachePo_List_3(accountIds, index, true);
   }

   private PacketUtil.Inner_CachePool init_PacketUtil_CachePo_List_3(List<String> accountIds, int index, boolean use) {
      int sent = 0;
      int skipped = 0;
      int failed = 0;
      if (accountIds != null) {
         for (String accountId : accountIds) {
            if (this.load_Map.get(accountId) == null) {
               skipped++;
            } else {
               String result = use ? this.load_Str(accountId, index) : this.init_Str_Str_I(accountId, index);
               if ("Sent".equals(result)) {
                  sent++;
               } else {
                  failed++;
               }
            }
         }
      }

      return new PacketUtil.Inner_CachePool(sent, skipped, failed, List.of());
   }

   private static boolean read_Z_Str(String result) {
      return result == null
         ? false
         : result.contains("not ready")
            || result.contains("Blocked")
            || result.contains("headless-safe")
            || result.contains("Rate limited")
            || result.contains("already running")
            || result.contains("Macro controls POV bot")
            || result.contains("Position is not ready")
            || result.contains("Signing key");
   }

   @Override
   public synchronized String init_Str_ClassCache_UUID(ClassCache session, UUID profileId) {
      return session != null && profileId != null && session.close_J() == this.tick_J && this.init_Map.get(session.open_Str()) == session
         ? init_Str_UUID(this.init_UUID, this.flush_Map, session.open_Str(), profileId)
         : "Stale Multi session";
   }

   static String init_Str_UUID(UUID renderedProfileId, Map<UUID, String> resolved, String accountId, UUID profileId) {
      if (profileId == null || accountId == null || resolved == null) {
         return "Stale Multi session";
      }

      if (renderedProfileId != null && renderedProfileId.equals(profileId)) {
         return "Account is already used by the rendered client";
      }

      String existing = resolved.putIfAbsent(profileId, accountId);
      return existing != null && !existing.equals(accountId) ? "Duplicate Minecraft identity in this batch" : "";
   }

   public synchronized void load_V(String oldName, String newName) {
      if (this.init_FieldCache != null && oldName != null && !oldName.isBlank()) {
         String replacement = newName == null ? "" : newName.trim();
         List<String> affected = new ArrayList<>();

         for (String accountId : this.init_Map.keySet()) {
            if (oldName.equals(this.write_Str_Str(accountId))) {
               affected.add(accountId);
            }
         }

         if (this.init_FieldCache.init_Z(oldName, replacement)) {
            TaskTimer.init_TaskTimer().init_V_FieldCache(this.init_FieldCache);
            this.close();
            this.poll_J++;
         }

         if (replacement.isBlank()) {
            for (String accountId : affected) {
               this.update_Map.remove(accountId);
               ClassCache session = this.init_Map.get(accountId);
               if (session != null) {
                  session.sync_V();
               }
            }
         }
      }
   }

   @Override
   public void init_V_ClassCache(ClassCache session) {
      try {
         this.init_ScheduledExecutorS.execute(() -> this.update_V_ClassCache(session));
      } catch (RejectedExecutionException ignored) {
      }
   }

   private synchronized void update_V_ClassCache(ClassCache session) {
      if (session != null && session.close_J() == this.tick_J && this.init_Map.get(session.open_Str()) == session) {
         ClassCache.Inner_PathHelper current = session.init_ClassCache_PathHel();
         String accountId = session.open_Str();
         ClassCache.Inner_PathHelper previous = this.open_Map.put(accountId, current);
         if (previous != current && (current == ClassCache.Inner_PathHelper.open || current == ClassCache.Inner_PathHelper.close)) {
            ProxyEntry proxy = this.write_Map.get(accountId);
            this.close_Map.put(accountId, proxy == null ? "" : proxy.write());
            String reason = TaskQueue.init_Str_Str_I(session.poll_Str(), 512);
            if (!reason.isBlank()) {
               this.init_V_Str(poll(accountId), reason, session.init_TaskQueue_DataUtil());
            }

            update_V_Str(accountId);
         }

         if ((current == ClassCache.Inner_PathHelper.flush || current == ClassCache.Inner_PathHelper.open || current == ClassCache.Inner_PathHelper.close)
            && this.load_Set.remove(session)) {
            this.sync_V();
         }

         if (current == ClassCache.Inner_PathHelper.flush || current == ClassCache.Inner_PathHelper.open || current == ClassCache.Inner_PathHelper.close) {
            this.read_Set.remove(accountId);
         }
      }
   }

   @Override
   public void init_V_ClassCache_TaskQueue_DataUtil(ClassCache session, TaskQueue.Inner_DataUtil report) {
      if (report != null) {
         try {
            this.init_ThreadPoolExecutor.execute(() -> {
               AutismClientAddon.LOG.warn("[Multi report {}]\n{}", report.init(), report.read());

               try {
                  Path directory = FabricLoader.getInstance().getGameDir().resolve("logs").resolve("multi");
                  Path saved = TaskQueue.init_Path_Path_TaskQueue_DataUtil(directory, report);
                  AutismClientAddon.LOG.info("[Multi report {}] Saved {}", report.init(), saved);
               } catch (IOException | RuntimeException error) {
                  this.init_V_ClassCache(session, report, error.getClass().getSimpleName());
               }
            });
         } catch (RejectedExecutionException error) {
            AutismClientAddon.LOG.warn("[Multi report {}]\n{}", report.init(), report.read());
            this.init_V_ClassCache(session, report, "report queue full");
         }
      }
   }

   private void init_V_ClassCache(ClassCache session, TaskQueue.Inner_DataUtil report, String cause) {
      AutismClientAddon.LOG.warn("[Multi report {}] Separate report unavailable ({}); see latest.log", report.init(), cause);
      try {
         this.init_ScheduledExecutorS.execute(() -> {
            String note = "Multi report " + report.init() + ": could not save a separate file; details are in logs/latest.log.";
            if (session == null) {
               this.close_V_Str(note);
            } else {
               this.read_V_ClassCache_Str(session, note);
            }
         });
      } catch (RejectedExecutionException ignored) {
      }
   }

   @Override
   public void init_V_ClassCache_Str(ClassCache session, String text) {
      if (session != null && text != null && !text.isBlank() && session.close_J() == this.tick_J) {
         this.read_V_ClassCache_Str(session, text);
      }
   }

   @Override
   public void load_V_ClassCache(ClassCache session) {
      if (session != null) {
         try {
            this.init_ScheduledExecutorS.execute(() -> {
               synchronized (this) {
                  if (session.close_J() != this.tick_J || this.init_Map.get(session.open_Str()) != session) {
                     return;
                  }
               }

               sync_V_Str(session.open_Str());
            });
         } catch (RejectedExecutionException ignored) {
         }
      }
   }

   private synchronized void read_V_ClassCache_Str(ClassCache session, String text) {
      if (session.close_J() == this.tick_J && this.init_Map.get(session.open_Str()) == session) {
         this.close_V_Str(text);
      }
   }

   @Override
   public void init_V_ClassCache_Component(ClassCache session, Component component) {
      if (session != null && component != null && session.close_J() == this.tick_J) {
         String text = update(component.getString(), 512);
         Component visible = init_Component_Component_I(component, 512);
         String source = update(session.clear_Str(), 32);
         if (this.init_Z_ClassCache(session, text, visible, source)) {
            RingBuffer.init(session, visible);
         }
      }
   }

   private synchronized boolean init_Z_ClassCache(ClassCache session, String text, Component visible, String source) {
      if (session.close_J() == this.tick_J && this.init_Map.get(session.open_Str()) == session) {
         String accountId = session.open_Str();
         long now = System.currentTimeMillis();
         long seq = ++this.push_J;
         ArrayDeque<PacketUtil.Inner_ConfigStore> account = this.push_Map.computeIfAbsent(accountId, keyx -> new ArrayDeque<>());
         account.addLast(new PacketUtil.Inner_ConfigStore(seq, now, visible, text, source));

         while (account.size() > 100) {
            account.removeFirst();
         }

         if (!text.isBlank()) {
            this.init_V_J(now);
            String key = read_Str_Str_Str(source, text);
            PacketUtil.Inner_HashUtil existing = this.pop_Map.get(key);
            if (existing != null && !existing.init_Z && init_Z_J_J(existing.load_J, now)) {
               existing.init_I++;
               existing.init_Map.putIfAbsent(accountId, visible);
            } else {
               PacketUtil.Inner_HashUtil msg = new PacketUtil.Inner_HashUtil(seq, now, text, source, false, visible);
               msg.init_Map.put(accountId, visible);
               this.init_V_PacketUtil_HashUti(msg);
               this.pop_Map.put(key, msg);
            }
         }

         this.pop_J++;
         return true;
      } else {
         return false;
      }
   }

   synchronized List<RingBuffer.Inner_DataUtil> load_List_Str(String accountId) {
      ArrayDeque<PacketUtil.Inner_ConfigStore> messages = this.push_Map.get(accountId);
      if (messages != null && !messages.isEmpty()) {
         List<RingBuffer.Inner_DataUtil> result = new ArrayList<>(messages.size());

         for (PacketUtil.Inner_ConfigStore message : messages) {
            result.add(new RingBuffer.Inner_DataUtil(message.load_J(), message.init_Component()));
         }

         return List.copyOf(result);
      } else {
         return List.of();
      }
   }

   @Override
   public void load(ClassCache session, String title) {
      boolean firstAlert;
      synchronized (this) {
         if (session == null || session.close_J() != this.tick_J || this.init_Map.get(session.open_Str()) != session) {
            return;
         }

         firstAlert = !this.init_Z;
         this.init_Z = true;
         if (firstAlert) {
            this.close_V_Str(
               "Warning: "
                  + poll(session.open_Str())
                  + " got a login screen (\""
                  + update(title, 48)
                  + "\") but no password is stored for this profile. Set one in the popup."
            );
         }
      }

      if (firstAlert) {
         Minecraft.getInstance().execute(() -> {
            bi.write("A bot hit a login screen. Set a password.");
            Minecraft mc = Minecraft.getInstance();
            if (!(mc.gui.screen() instanceof autismclient.gui.d.TokenStore)) {
               mc.gui.setScreen(new autismclient.gui.d.TokenStore(mc.gui.screen()));
            }
         });
      }
   }

   public synchronized int load_I_Str(String password) {
      if (this.init_FieldCache != null && password != null && !password.isBlank()) {
         int updated = 0;

         for (FieldCache.Inner_BufferHelper spec : this.init_FieldCache.init_List) {
            if (this.init_FieldCache.init(spec.init_Str(), "password", password)) {
               updated++;
            }
         }

         this.open();
         return updated;
      } else {
         return 0;
      }
   }

   public synchronized int sync_I() {
      if (this.init_FieldCache == null) {
         return 0;
      }

      int updated = 0;

      for (FieldCache.Inner_BufferHelper spec : this.init_FieldCache.init_List) {
         if (this.init_FieldCache.init(spec.init_Str(), "password", update_Str())) {
            updated++;
         }
      }

      this.open();
      return updated;
   }

   public static String update_Str() {
      return aL.init();
   }

   private void open() {
      if (this.init_FieldCache != null) {
         TaskTimer.init_TaskTimer().init_V_FieldCache(this.init_FieldCache);
         this.poll_J++;

         for (Entry<String, ClassCache> entry : this.init_Map.entrySet()) {
            entry.getValue().init_V_Map(this.init_FieldCache.init_Map_Str(entry.getKey()));
         }
      }
   }

   public synchronized void init_V_FieldCache(FieldCache source) {
      if (this.init_FieldCache != null && source != null) {
         for (FieldCache.Inner_BufferHelper spec : this.init_FieldCache.init_List) {
            String password = source.init_Map_Str(spec.init_Str()).get("password");
            if (password != null) {
               this.init_FieldCache.init(spec.init_Str(), "password", password);
            }
         }

         this.open();
      }
   }

   @Override
   public boolean init_Z_ClassCache_aA(ClassCache requester, aA action) {
      List<ClassCache.Inner_UiHelper> published = new ArrayList<>();

      for (ClassCache session : this.init_List) {
         if (session != requester) {
            published.add(session.init_ClassCache_FieldCa().init_ClassCache_UiHelpe());
         }
      }

      return init_Z_aA_Iterable(action, published);
   }

   @Override
   public synchronized void init_V_ClassCache_aM(ClassCache session, aM macro) {
      if (session != null && macro != null && session.close_J() == this.tick_J && this.init_Map.get(session.open_Str()) == session) {
         this.update_Map.put(session.open_Str(), PacketUtil.Inner_JsonUtil.init(macro));
      }
   }

   @Override
   public synchronized void read_V_ClassCache(ClassCache session) {
      if (session != null && session.close_J() == this.tick_J && this.init_Map.get(session.open_Str()) == session) {
         this.update_Map.remove(session.open_Str());
      }
   }

   static boolean init_Z_aA_Iterable(aA action, Iterable<ClassCache.Inner_UiHelper> published) {
      if (action == null) {
         return true;
      }

      String target = action.macroName == null ? "" : action.macroName.trim();
      if (target.isEmpty()) {
         return true;
      }

      int step = Math.max(1, action.step);
      boolean found = false;

      for (ClassCache.Inner_UiHelper progress : published) {
         if (progress != null && target.equalsIgnoreCase(progress.init_Str())) {
            found = true;
            boolean var10000;
            switch (action.mode == null ? aA.Inner_DataUtil.load : action.mode) {
               case init:
                  var10000 = (progress.init_Z() && progress.load_I() >= step && progress.init_I() >= step - 1)
                     || progress.init_I() >= step
                     || !progress.init_Z() && progress.load_I() >= step;
                  break;
               case load:
                  var10000 = progress.init_I() >= step || !progress.init_Z() && progress.load_I() >= step;
                  break;
               case read:
                  var10000 = !progress.init_Z();
                  break;
               default:
                  throw new MatchException(null, null);
            }

            boolean satisfied = var10000;
            if (satisfied) {
               return true;
            }
         }
      }

      return !found && action.mode == aA.Inner_DataUtil.read;
   }

   private void init_V_PacketUtil_HashUti(PacketUtil.Inner_HashUtil msg) {
      this.load_ArrayDeque.addLast(msg);

      while (this.load_ArrayDeque.size() > 100) {
         PacketUtil.Inner_HashUtil removed = this.load_ArrayDeque.removeFirst();
         this.pop_Map.remove(removed.init_Z ? flush_Str_Str(removed.init_Str) : read_Str_Str_Str(removed.load_Str, removed.init_Str), removed);
      }
   }

   private void init_V_J(long now) {
      if (this.pop_Map.size() >= 256) {
         this.pop_Map.values().removeIf(m -> m.init_Z ? now - m.read > 20000L : !init_Z_J_J(m.load_J, now));
      }
   }

   static boolean init_Z_J_J(long firstCopyAt, long currentCopyAt) {
      long elapsed = currentCopyAt - firstCopyAt;
      return elapsed >= 0L && elapsed <= 2000L;
   }

   private void poll() {
      long now = System.currentTimeMillis();
      List<ClassCache> snapshot;
      long tickGeneration;
      synchronized (this) {
         if (!this.load_Z) {
            return;
         }

         snapshot = this.init_List;
         tickGeneration = this.tick_J;
      }

      for (ClassCache session : snapshot) {
         init_V_ClassCache_J(session, now);
      }

      synchronized (this) {
         if (this.load_Z && this.tick_J == tickGeneration) {
            this.read_V_List(snapshot);
            if (now - this.set >= 100L) {
               this.init_V_Z(false);
               this.set = now;
            }
         }
      }
   }

   private void read_V_List(List<ClassCache> snapshot) {
      LinkedHashMap<String, Integer> groups = null;

      for (ClassCache session : snapshot) {
         String note = session.reset_Str();
         if (note != null) {
            PacketUtil.Inner_TaskQueue finish = init_PacketUtil_TaskQue_Str(note);
            String macroName = finish.init();
            String reason = finish.load();
            if (!"chained".equals(reason)) {
               this.update_Map.remove(session.open_Str());
            }

            String key = open(reason) + "\u0000" + macroName;
            if (groups == null) {
               groups = new LinkedHashMap<>();
            }

            groups.merge(key, 1, Integer::sum);
         }
      }

      if (groups != null) {
         for (Entry<String, Integer> e : groups.entrySet()) {
            int sep = e.getKey().indexOf(0);
            String verb = e.getKey().substring(0, sep);
            String macroName = e.getKey().substring(sep + 1);
            int count = e.getValue();
            this.close_V_Str("Macro \"" + macroName + "\" " + verb + " on " + count + " bot" + (count == 1 ? "" : "s") + ".");
         }
      }
   }

   static PacketUtil.Inner_TaskQueue init_PacketUtil_TaskQue_Str(String note) {
      String value = note == null ? "" : note;
      int separator = value.indexOf(0);
      return separator >= 0
         ? new PacketUtil.Inner_TaskQueue(value.substring(0, separator), value.substring(separator + 1))
         : new PacketUtil.Inner_TaskQueue(value, "done");
   }

   private static String open(String reason) {
      if (!reason.startsWith("Join:") && !reason.startsWith("Join timed out") && !reason.equals("Join cancelled")) {
         String var10000;
         switch (reason) {
            case "chained":
               var10000 = "handed off";
               break;
            case "error":
               var10000 = "stopped on an error";
               break;
            case "stopped":
               var10000 = "finished (stop action)";
               break;
            default:
               var10000 = "finished";
         }

         return var10000;
      } else {
         return "stopped on a join error";
      }
   }

   private static void init_V_ClassCache_J(ClassCache session, long now) {
      try {
         session.init_V_J(now);
      } catch (RuntimeException var7) {
         RuntimeException error = var7;
         String message = var7.getClass().getSimpleName();

         try {
            session.init_V_Str_Throwable("Session tick failed: " + TaskQueue.init_Str_Str_I(message, 120), error);
         } catch (RuntimeException var7x) {
         }
      }
   }

   private void push() {
      try {
         this.poll();
         this.read_Z = false;
      } catch (RuntimeException error) {
         long now = System.nanoTime();
         if (!this.read_Z || now - this.put >= TimeUnit.SECONDS.toNanos(30L)) {
            this.put = now;
            TaskQueue diagnostics = new TaskQueue();
            diagnostics.init_V_Str("COORDINATOR");
            diagnostics.init_V_Throwable(error);
            TaskQueue.Inner_DataUtil report = diagnostics.init(
               "Background session tick failed: " + error.getClass().getSimpleName(), "Multiple sessions", "Per session", 0
            );
            this.init_V_ClassCache_TaskQueue_DataUtil(null, report);
            this.close_V_Str(report.load() + " [logs/multi or latest.log]; the next tick will retry.");
         }

         this.read_Z = true;
      }
   }

   private synchronized void pop() {
      if (this.init_ScheduledFuture == null || this.init_ScheduledFuture.isCancelled() || this.init_ScheduledFuture.isDone()) {
         try {
            this.init_ScheduledFuture = this.init_ScheduledExecutorS.scheduleWithFixedDelay(this::push, 50L, 50L, TimeUnit.MILLISECONDS);
         } catch (RejectedExecutionException ignored) {
         }
      }
   }

   private void init_V_Str(String account, String reason, TaskQueue.Inner_DataUtil report) {
      String key = update(reason == null ? "" : reason.trim(), 160);
      if (!key.isBlank()) {
         long now = System.currentTimeMillis();
         long[] agg = this.tick_Map.get(key);
         if (agg == null) {
            if (this.tick_Map.size() >= 256) {
               this.tick_Map.clear();
            }
            this.tick_Map.put(key, new long[]{1L, 0L});
            String description = report == null
               ? TaskQueue.init_Str_Str_I(reason, 420)
               : TaskQueue.init_Str_Str_I(report.load(), 340) + " [logs/multi or latest.log]";
            this.flush_V_Str(account + ": " + description);
         } else {
            agg[0]++;
            if (agg[0] == 4L || agg[0] > 4L && now - agg[1] >= 15000L) {
               this.flush_V_Str(key + " (x" + agg[0] + " accounts)");
               agg[1] = now;
            }
         }
      }
   }

   private void flush_V_Str(String line) {
      this.close_V_Str(line);
      AutismClientMessaging.sendPrefixed("Multi: " + line);
   }

   private synchronized void close_V_Str(String text) {
      String safe = update(text, 512);
      if (!safe.isBlank()) {
         long now = System.currentTimeMillis();
         this.init_V_J(now);
         String key = flush_Str_Str(safe);
         PacketUtil.Inner_HashUtil existing = this.pop_Map.get(key);
         if (existing != null && existing.init_Z && now - existing.read < 20000L) {
            existing.init_I++;
            existing.read = now;
            this.pop_J++;
         } else {
            Component render = AutismClientMessaging.themedTag("Multi").append(AutismClientMessaging.themedBody(safe));
            PacketUtil.Inner_HashUtil msg = new PacketUtil.Inner_HashUtil(++this.push_J, now, safe, "", true, render);
            this.init_V_PacketUtil_HashUti(msg);
            this.pop_Map.put(key, msg);
            this.pop_J++;
         }
      }
   }

   public static String update(String text, int maxChars) {
      if (text != null && !text.isBlank()) {
         StringBuilder out = new StringBuilder(Math.min(text.length(), Math.max(16, maxChars)));
         boolean spaced = false;

         for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            boolean space = Character.isISOControl(ch) || Character.isWhitespace(ch);
            if (space) {
               if (!spaced && out.length() > 0) {
                  out.append(' ');
                  spaced = true;
               }
            } else {
               out.append(ch);
               spaced = false;
               if (maxChars > 3 && out.length() >= maxChars) {
                  break;
               }
            }
         }

         String safe = out.toString().trim();
         if (maxChars > 3 && safe.length() > maxChars - 3) {
            safe = safe.substring(0, maxChars - 3).trim() + "...";
         }

         return safe;
      } else {
         return "";
      }
   }

   static Component init_Component_Component_I(Component component, int maxChars) {
      if (component != null && maxChars > 0) {
         MutableComponent safe = Component.empty();
         int[] length = new int[]{0};
         boolean[] spaced = new boolean[]{true};
         component.visit((style, part) -> {
            if (part != null && !part.isEmpty() && length[0] < maxChars) {
               StringBuilder run = new StringBuilder(Math.min(part.length(), maxChars - length[0]));

               for (int i = 0; i < part.length() && length[0] < maxChars; i++) {
                  char ch = part.charAt(i);
                  if (!Character.isISOControl(ch) && !Character.isWhitespace(ch)) {
                     run.append(ch);
                     length[0]++;
                     spaced[0] = false;
                  } else if (!spaced[0] && length[0] > 0) {
                     run.append(' ');
                     length[0]++;
                     spaced[0] = true;
                  }
               }

               if (!run.isEmpty()) {
                  safe.append(Component.literal(run.toString()).withStyle(style == null ? Style.EMPTY : style));
               }

               return Optional.empty();
            } else {
               return Optional.empty();
            }
         }, Style.EMPTY);
         return safe;
      } else {
         return Component.empty();
      }
   }

   private static String poll(String accountId) {
      if ("default".equals(accountId)) {
         return autismclient.util.FieldCache.init_User().getName();
      }

      autismclient.util.BitSetUtil account = autismclient.util.TokenStore.init_TokenStore().init_BitSetUtil(accountId);
      return account == null ? accountId : account.init_Str();
   }

   private static ProxyEntry init_bW_bW(ProxyEntry source) {
      if (source == null) {
         return null;
      }

      ProxyEntry copy = new ProxyEntry();
      copy.init_Str = source.write();
      copy.load_Str = source.load_Str;
      copy.init_bZ = source.init_bZ;
      copy.read = source.read;
      copy.init_I = source.init_I;
      copy.write = source.write;
      copy.update = source.update;
      return copy;
   }

   static String init_Str_bW(ProxyEntry proxy) {
      return proxy != null && proxy.init_Z() ? (proxy.read == null ? "" : proxy.read.trim().toLowerCase(Locale.ROOT)) + ":" + proxy.init_I : "";
   }

   static List<ProxyEntry> init_List_List(List<ProxyEntry> proxies) {
      if (proxies != null && !proxies.isEmpty()) {
         List<ProxyEntry> ordered = new ArrayList<>();

         for (ProxyEntry proxy : proxies) {
            if (init_Z_bW(proxy)) {
               ordered.add(proxy);
            }
         }

         ordered.sort(
            Comparator.comparingInt(PacketUtil::init_I_bW)
               .thenComparingLong(proxyx -> proxyx.init_bW_CachePool == ProxyEntry.Inner_CachePool.write && proxyx.init_J > 0L ? proxyx.init_J : 9223372036854775807L)
               .thenComparing(proxyx -> proxyx.init_Str().toLowerCase(Locale.ROOT))
         );
         write_V_List(ordered);
         Map<String, ProxyEntry> distinct = new LinkedHashMap<>();

         for (ProxyEntry proxy : ordered) {
            distinct.putIfAbsent(init_Str_bW(proxy), proxy);
         }

         return List.copyOf(distinct.values());
      } else {
         return List.of();
      }
   }

   private static void write_V_List(List<ProxyEntry> candidates) {
      if (candidates != null && candidates.size() >= 2 && Minecraft.getInstance() != null) {
         ProxyEntry mainProxy = ProxyManager.init_bY().init_bW();
         if (mainProxy != null) {
            String reserved = init_Str_bW(mainProxy);
            if (!reserved.isBlank()) {
               long distinctEndpoints = candidates.stream().map(PacketUtil::init_Str_bW).distinct().count();
               if (distinctEndpoints > 1L) {
                  candidates.removeIf(proxy -> reserved.equals(init_Str_bW(proxy)));
               }
            }
         }
      }
   }

   static Map<String, ProxyEntry> init_Map_List_List(List<FieldCache.Inner_BufferHelper> sessions, List<ProxyEntry> candidates) {
      if (sessions != null && !sessions.isEmpty()) {
         if (candidates != null && !candidates.isEmpty()) {
            List<ProxyEntry> distinct = new ArrayList<>();
            Set<String> leases = new HashSet<>();

            for (ProxyEntry candidate : candidates) {
               String key = init_Str_bW(candidate);
               if (!key.isBlank() && leases.add(key)) {
                  distinct.add(candidate);
               }
            }

            if (distinct.isEmpty()) {
               return Map.of();
            }

            Map<String, ProxyEntry> assignment = new LinkedHashMap<>();
            int index = 0;

            for (FieldCache.Inner_BufferHelper spec : sessions) {
               if (!spec.init_Z()) {
                  assignment.put(spec.init_Str(), distinct.get(index++ % distinct.size()));
               }
            }

            return assignment;
         } else {
            return Map.of();
         }
      } else {
         return Map.of();
      }
   }

   static ProxyEntry init_bW_List(List<ProxyEntry> proxies, String currentProxyId, String lastFailedProxyId, boolean currentDirect) {
      if (currentDirect) {
         return null;
      }

      if (proxies != null && !proxies.isEmpty()) {
         String current = currentProxyId == null ? "" : currentProxyId;
         String failed = lastFailedProxyId == null ? "" : lastFailedProxyId;
         ProxyEntry candidate = init_bW_List(proxies, current, failed);
         if (candidate != null) {
            return candidate;
         }

         candidate = init_bW_List(proxies, "", failed);
         return candidate != null ? candidate : init_bW_List(proxies, "", "");
      } else {
         return null;
      }
   }

   private static ProxyEntry init_bW_List(List<ProxyEntry> proxies, String avoidCurrent, String avoidFailed) {
      if (proxies != null && !proxies.isEmpty()) {
         String current = avoidCurrent == null ? "" : avoidCurrent;
         String failed = avoidFailed == null ? "" : avoidFailed;
         return proxies.stream()
            .filter(PacketUtil::init_Z_bW)
            .filter(proxy -> {
               String id = proxy.write();
               return (current.isBlank() || !current.equals(id)) && (failed.isBlank() || !failed.equals(id));
            })
            .min(
               Comparator.comparingInt(PacketUtil::init_I_bW)
                  .thenComparingLong(proxy -> proxy.init_bW_CachePool == ProxyEntry.Inner_CachePool.write && proxy.init_J > 0L ? proxy.init_J : 9223372036854775807L)
                  .thenComparing(proxy -> proxy.init_Str().toLowerCase(Locale.ROOT))
            )
            .orElse(null);
      } else {
         return null;
      }
   }

   private static boolean init_Z_bW(ProxyEntry proxy) {
      return proxy != null && proxy.init_Z() && proxy.init_bW_CachePool != ProxyEntry.Inner_CachePool.read;
   }

   private static int init_I_bW(ProxyEntry proxy) {
      if (proxy != null && proxy.init_bW_CachePool != null) {
         byte var10000;
         switch (proxy.init_bW_CachePool) {
            case write:
               var10000 = 0;
               break;
            case init:
               var10000 = 1;
               break;
            case load:
               var10000 = 2;
               break;
            case read:
               var10000 = 3;
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      } else {
         return 3;
      }
   }

   public static Class<? extends Packet<?>> init_Class_Str(String name) {
      return bw.init_Class_Str(name);
   }

   public record Inner_BufferHelper(
      long init_J, long load, Component init_Component, Map<String, Component> init_Map, boolean init_Z, int init_I, String init_Str
   ) {
   }

   public record Inner_ByteReader(int init_I, int load, List<String> init_List) {
   }

   public record Inner_CachePool(int init_I, int load, int read, List<String> init_List) {
      public String init_Str() {
         return "Sent " + this.init_I + ", skipped " + this.load + ", failed " + this.read;
      }
   }

   private record Inner_ConfigStore(long init_J, long load_J, Component init_Component, String init_Str, String load_Str) {
   }

   public record Inner_EventBus(boolean init_Z, String init_Str) {
      static PacketUtil.Inner_EventBus init(String message) {
         return new PacketUtil.Inner_EventBus(true, message);
      }

      static PacketUtil.Inner_EventBus load(String message) {
         return new PacketUtil.Inner_EventBus(false, message);
      }
   }

   private record Inner_FileCache(ProxyEntry init_bW, boolean init_Z, long init_J) {
   }

   private static final class Inner_HashUtil {
      final long init_J;
      final long load_J;
      long read;
      int init_I = 1;
      final String init_Str;
      final String load_Str;
      final boolean init_Z;
      final Component init_Component;
      final Map<String, Component> init_Map = new LinkedHashMap<>();

      Inner_HashUtil(long seq, long time, String text, String source, boolean system, Component representative) {
         this.init_J = seq;
         this.load_J = time;
         this.read = time;
         this.init_Str = text;
         this.load_Str = source == null ? "" : source;
         this.init_Z = system;
         this.init_Component = representative;
      }
   }

   private record Inner_JsonUtil(PacketUtil.Inner_NetHelper init_PacketUtil_NetHelp, aM init_aM) {
      static PacketUtil.Inner_JsonUtil init_PacketUtil_JsonUti() {
         return new PacketUtil.Inner_JsonUtil(PacketUtil.Inner_NetHelper.init, null);
      }

      static PacketUtil.Inner_JsonUtil init(aM macro) {
         return new PacketUtil.Inner_JsonUtil(PacketUtil.Inner_NetHelper.load, macro);
      }
   }

   private enum Inner_NetHelper {
      init,
      load;
   }

   public interface Inner_PacketUtil {
      void init();

      void init(String var1);

      default void load(String accountId) {
      }
   }

   record Inner_RenderState(List<ProxyEntry> init, List<ProxyEntry> load) {
   }

   private final class Inner_StateCache {
      private final long init_J = System.currentTimeMillis();
      private final int init_I = RenderState.init_I();
      private int load;

      long init_J() {
         return RenderState.init_J_J(this.init_J, this.load++, this.init_I);
      }

      void init_V() {
         if (this.init_I > 0 && this.load > 1) {
            PacketUtil.this.close_V_Str("Starting " + this.load + " bots " + RenderState.load_Str(this.init_I) + " apart.");
         }
      }
   }

   public record Inner_StreamUtil(String init, String load) {
   }

   private record Inner_SyncManager(ClassCache init_ClassCache, InetSocketAddress init_InetSocketAddress, String init_Str, int init_I) {
   }

   record Inner_TaskQueue(String init, String load) {
   }

   public record Inner_TextFormat(boolean init_Z, String init_Str) {
      static PacketUtil.Inner_TextFormat init_PacketUtil_TextFor() {
         return new PacketUtil.Inner_TextFormat(true, "");
      }

      static PacketUtil.Inner_TextFormat init(String message) {
         return new PacketUtil.Inner_TextFormat(false, message);
      }
   }

   private record Inner_UiHelper(Class<? extends Packet<?>> init_Class, String init_Str) {
   }
}
