package autismclient.util.g;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.net.InetAddresses;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.client.resources.server.PackDownloader;
import net.minecraft.client.resources.server.PackLoadFeedback;
import net.minecraft.client.resources.server.ServerPackManager;
import net.minecraft.client.resources.server.PackLoadFeedback.FinalResult;
import net.minecraft.client.resources.server.PackLoadFeedback.Update;
import net.minecraft.client.resources.server.PackReloadConfig.Callbacks;
import net.minecraft.client.resources.server.PackReloadConfig.IdAndPath;
import net.minecraft.client.resources.server.ServerPackManager.PackPromptStatus;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket.Action;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.DownloadQueue;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.DownloadQueue.BatchConfig;
import net.minecraft.server.packs.DownloadQueue.BatchResult;
import net.minecraft.server.packs.DownloadQueue.DownloadRequest;
import net.minecraft.server.packs.FilePackResources.FileResourcesSupplier;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.Pack.Metadata;
import net.minecraft.server.packs.repository.Pack.Position;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Unit;
import net.minecraft.util.HttpUtil.DownloadProgressListener;

final class MapStore {
   private static final Pattern init_Pattern = Pattern.compile("^[a-fA-F0-9]{40}$");
   private static final int init_I = 262144000;
   private static final PackSelectionConfig init_PackSelectionConfi = new PackSelectionConfig(true, Position.TOP, true);
   private static final Component init_Component = Component.translatable("resourcePack.server.name");
   private final Supplier<MapStore.Inner_JsonUtil> init_Supplier;
   private final BooleanSupplier init_BooleanSupplier;
   private final MapStore.Inner_NetHelper init_MapStore_NetHelper;
   private final MapStore.Inner_StreamUtil init_MapStore_StreamUti;
   private final Object init_Obj = new Object();
   private MapStore.Inner_DataUtil init_MapStore_DataUtil;
   private boolean init_Z;

   MapStore(Supplier<MapStore.Inner_JsonUtil> currentTarget, BooleanSupplier sessionClosed, MapStore.Inner_NetHelper sender) {
      this(currentTarget, sessionClosed, sender, () -> Path.of(System.getProperty("java.io.tmpdir"), "autism-multi-resource-packs"));
   }

   MapStore(
      Supplier<MapStore.Inner_JsonUtil> currentTarget, BooleanSupplier sessionClosed, MapStore.Inner_NetHelper sender, MapStore.Inner_StreamUtil cacheDirectory
   ) {
      this.init_Supplier = currentTarget;
      this.init_BooleanSupplier = sessionClosed;
      this.init_MapStore_NetHelper = sender;
      this.init_MapStore_StreamUti = cacheDirectory;
   }

   void init_V_Connection_SocketAddress(Connection connection, SocketAddress peer) {
      synchronized (this.init_Obj) {
         if (!this.init_Z && !this.init_BooleanSupplier.getAsBoolean()) {
            this.write();
            MapStore.Inner_JsonUtil target = this.init_Supplier.get();
            if (connection != null && target != null && target.init_Connection() == connection) {
               this.init_MapStore_DataUtil = new MapStore.Inner_DataUtil(connection, peer, target.init_I(), target.init_J());
            }
         }
      }
   }

   void init_V_Connection(Connection connection) {
      this.init_V_Connection_SocketAddress(connection, connection == null ? null : connection.getRemoteAddress());
   }

   void init(MapStore.Inner_JsonUtil target, ClientboundResourcePackPushPacket pack, long now) {
      this.init(target, pack, now, false);
   }

   void init(MapStore.Inner_JsonUtil target, ClientboundResourcePackPushPacket pack, long now, boolean decline) {
      if (pack != null) {
         synchronized (this.init_Obj) {
            if (this.init_Z_MapStore_JsonUtil(target)) {
               MapStore.Inner_DataUtil current = this.init_MapStore_DataUtil;
               if (this.init_Z_MapStore_DataUtil(current)) {
                  long generation = autismclient.g.FileCache.init(
                     current.init_BufferHelper_Strea,
                     pack.id(),
                     decline ? autismclient.g.FileCache.Inner_DataUtil.read : autismclient.g.FileCache.Inner_DataUtil.init
                  );
                  MapStore.Inner_StateCache parsed = init_MapStore_StateCach_Str_Str(pack.url(), pack.hash());
                  MapStore.Inner_TaskQueue record = new MapStore.Inner_TaskQueue(current, pack.id(), generation, target, parsed, decline);
                  current.init_Map.put(pack.id(), record);
                  current.init_MapStore_TaskQueue = null;
                  if (parsed == null) {
                     current.init_MapStore_TaskQueue = record;
                     this.init_V_MapStore_DataUtil_MapStore_TaskQueue(current, record);
                  } else {
                     if (decline) {
                        current.init_ServerPackManager.popPack(pack.id());
                        current.init_ServerPackManager.rejectServerPacks();
                     }

                     current.init_ServerPackManager.pushPack(pack.id(), parsed.init_URL(), parsed.init_HashCode());
                     if (decline) {
                        current.init_ServerPackManager.allowServerPacks();
                     }
                  }
               }
            }
         }
      }
   }

   void init_V_MapStore_JsonUtil_Optional(MapStore.Inner_JsonUtil target, Optional<UUID> id) {
      synchronized (this.init_Obj) {
         if (this.init_Z_MapStore_JsonUtil(target)) {
            MapStore.Inner_DataUtil current = this.init_MapStore_DataUtil;
            UUID single = id == null ? null : id.orElse(null);
            if (single == null) {
               current.init_Map.clear();
               current.init_MapStore_TaskQueue = null;
               autismclient.g.FileCache.init_V(current.init_FileCache_BufferHe, null);
               current.init_ServerPackManager.popAll();
            } else {
               MapStore.Inner_TaskQueue removed = current.init_Map.remove(single);
               if (current.init_MapStore_TaskQueue == removed) {
                  current.init_MapStore_TaskQueue = null;
               }

               autismclient.g.FileCache.init_V(current.init_FileCache_BufferHe, single);
               current.init_ServerPackManager.popPack(single);
            }
         }
      }
   }

   void init_V_J(long now) {
      synchronized (this.init_Obj) {
         if (!this.init_Z && !this.init_BooleanSupplier.getAsBoolean()) {
            MapStore.Inner_DataUtil current = this.init_MapStore_DataUtil;
            if (current != null) {
               MapStore.Inner_JsonUtil target = this.init_Supplier.get();
               if (target == null
                  || target.init_Connection() != current.init_Connection
                  || target.init_I() != current.init_I
                  || target.init_J() != current.init_J) {
                  this.write();
               } else if (this.init_Z_MapStore_DataUtil(current)) {
                  this.init_V_MapStore_DataUtil_MapStore_JsonUtil(current, target);
                  this.init_V_MapStore_DataUtil_MapStore_TaskQueue(current, current.init_MapStore_TaskQueue);
                  current.load();
                  autismclient.g.FileCache.load();

                  while (!current.load_Z && current.init_Z) {
                     current.init_Z = false;
                     current.init_ServerPackManager.tick();
                  }

                  current.read();

                  while (!current.load_Z && current.init_Z) {
                     current.init_Z = false;
                     current.init_ServerPackManager.tick();
                  }

                  autismclient.g.FileCache.load();
               }
            }
         } else {
            this.write();
         }
      }
   }

   void init_V() {
      synchronized (this.init_Obj) {
         this.write();
      }
   }

   void load() {
      synchronized (this.init_Obj) {
         if (!this.init_BooleanSupplier.getAsBoolean()) {
            this.write();
            this.init_Z = false;
         }
      }
   }

   void read() {
      synchronized (this.init_Obj) {
         this.init_Z = true;
         this.write();
      }
   }

   Optional<Resource> init_Optional_Identifier(Identifier identifier) {
      synchronized (this.init_Obj) {
         return this.init_MapStore_DataUtil != null && this.init_MapStore_DataUtil.init_ReloadableResource != null
            ? this.init_MapStore_DataUtil.init_ReloadableResource.getResource(identifier)
            : Optional.empty();
      }
   }

   int init_I() {
      synchronized (this.init_Obj) {
         return this.init_MapStore_DataUtil == null ? 0 : this.init_MapStore_DataUtil.read_I;
      }
   }

   private boolean init_Z_MapStore_JsonUtil(MapStore.Inner_JsonUtil target) {
      if (!this.init_Z
         && !this.init_BooleanSupplier.getAsBoolean()
         && target != null
         && this.init_MapStore_DataUtil != null
         && !this.init_MapStore_DataUtil.load_Z) {
         MapStore.Inner_JsonUtil current = this.init_Supplier.get();
         return current != null
            && this.init_MapStore_DataUtil.init_Connection == target.init_Connection()
            && this.init_MapStore_DataUtil.init_I == target.init_I()
            && this.init_MapStore_DataUtil.init_J == target.init_J()
            && target.init_Connection() == current.init_Connection()
            && target.init_I() == current.init_I()
            && target.init_MapStore_CachePool() == current.init_MapStore_CachePool()
            && target.init_J() == current.init_J();
      } else {
         return false;
      }
   }

   private boolean init_Z_MapStore_DataUtil(MapStore.Inner_DataUtil current) {
      if (current != null && !current.load_Z) {
         if (!autismclient.g.BufferHelper.init_Z(current.init_BufferHelper_Strea)) {
            this.write();
            return false;
         }

         current.init_V();
         if (autismclient.g.FileCache.init_Z(current.init_FileCache_BufferHe)) {
            return true;
         }

         autismclient.g.FileCache.Inner_BufferHelper replacement = autismclient.g.FileCache.init(
            current.init_BufferHelper_Strea, current, current.init_ServerPackManager, null
         );

         for (MapStore.Inner_TaskQueue record : new ArrayList<>(current.init_Map.values())) {
            record.init_FileCache_BufferHe = replacement;
            record.init_J = autismclient.g.FileCache.init(
               current.init_BufferHelper_Strea,
               record.init_UUID,
               record.init_Z ? autismclient.g.FileCache.Inner_DataUtil.read : autismclient.g.FileCache.Inner_DataUtil.init
            );
            record.init();
            if (!record.init_Z) {
               for (MapStore.Inner_DataUtil.Inner_DataUtil_2 download : current.init_List) {
                  Long claim = download.init_Map.remove(record);
                  if (claim != null) {
                     download.init_Map.put(record, record.init_J);
                  }
               }

               if (record.load_Z) {
                  autismclient.g.FileCache.load(replacement, record.init_UUID);
               }

               if (record.read) {
                  autismclient.g.FileCache.init(replacement, record.init_UUID, Update.ACCEPTED, record.init_PackLoadFeedback);
               }

               if (record.write) {
                  autismclient.g.FileCache.init(replacement, record.init_UUID, Update.DOWNLOADED, record.init_PackLoadFeedback);
               }

               if (record.init_PackLoadFeedback_F != null) {
                  autismclient.g.FileCache.init(replacement, record.init_UUID, record.init_PackLoadFeedback_F, record.init_PackLoadFeedback);
               }
            } else if (record.init_PackLoadFeedback_F == FinalResult.DECLINED) {
               autismclient.g.FileCache.init(replacement, record.init_UUID, FinalResult.DECLINED, record.init_PackLoadFeedback);
            }
         }

         current.init_FileCache_BufferHe = replacement;
         return true;
      } else {
         return false;
      }
   }

   private void init_V_MapStore_DataUtil_MapStore_JsonUtil(MapStore.Inner_DataUtil current, MapStore.Inner_JsonUtil target) {
      for (MapStore.Inner_TaskQueue record : new ArrayList<>(current.init_Map.values())) {
         if (!init_Z_MapStore_JsonUtil_MapStore_JsonUtil(record.init_MapStore_JsonUtil, target)) {
            current.init_Map.remove(record.init_UUID, record);
            if (current.init_MapStore_TaskQueue == record) {
               current.init_MapStore_TaskQueue = null;
            }

            autismclient.g.FileCache.init_V(record.init_FileCache_BufferHe, record.init_UUID);
            current.init_ServerPackManager.popPack(record.init_UUID);
         }
      }
   }

   private void init_V_MapStore_DataUtil_MapStore_TaskQueue(MapStore.Inner_DataUtil current, MapStore.Inner_TaskQueue record) {
      if (record != null
         && current.init_MapStore_TaskQueue == record
         && current.init_Map.get(record.init_UUID) == record
         && init_Z_MapStore_JsonUtil_MapStore_JsonUtil(record.init_MapStore_JsonUtil, this.init_Supplier.get())
         && autismclient.g.FileCache.init_Z(record.init_FileCache_BufferHe, record.init_UUID, record.init_J)) {
         boolean sent;
         try {
            sent = this.init_MapStore_NetHelper.send(record.init_MapStore_JsonUtil, record.init_UUID, record.init_J, Action.INVALID_URL);
         } catch (RuntimeException ignored) {
            sent = false;
         }

         if (sent) {
            current.init_MapStore_TaskQueue = null;
            current.init_Map.remove(record.init_UUID, record);
            autismclient.g.FileCache.init_V(record.init_FileCache_BufferHe, record.init_UUID);
            current.init_ServerPackManager.popPack(record.init_UUID);
         }
      }
   }

   private void init_V_MapStore_TaskQueue_ServerboundResourc(MapStore.Inner_TaskQueue record, Action action) {
      synchronized (this.init_Obj) {
         MapStore.Inner_DataUtil current = record.init_MapStore_DataUtil;
         if (!this.init_Z
            && !this.init_BooleanSupplier.getAsBoolean()
            && !current.load_Z
            && this.init_MapStore_DataUtil == current
            && current.init_Map.get(record.init_UUID) == record
            && (!record.init_Z || action == Action.DECLINED)
            && init_Z_MapStore_JsonUtil_MapStore_JsonUtil(record.init_MapStore_JsonUtil, this.init_Supplier.get())
            && autismclient.g.FileCache.init_Z(record.init_FileCache_BufferHe, record.init_UUID, record.init_J)) {
            if (!record.init_Set.contains(action)) {
               if (!this.init_MapStore_NetHelper.send(record.init_MapStore_JsonUtil, record.init_UUID, record.init_J, action)) {
                  throw new IllegalStateException("Resource-pack response was refused");
               }

               record.init_Set.add(action);
            }
         } else {
            throw new IllegalStateException("Stale resource-pack response");
         }
      }
   }

   private void write() {
      if (this.init_MapStore_DataUtil != null) {
         this.init_MapStore_DataUtil.write();
         this.init_MapStore_DataUtil = null;
      }
   }

   private static boolean init_Z_MapStore_JsonUtil_MapStore_JsonUtil(MapStore.Inner_JsonUtil captured, MapStore.Inner_JsonUtil current) {
      return captured != null
            && current != null
            && captured.init_Connection() == current.init_Connection()
            && captured.init_I() == current.init_I()
            && captured.init_J() == current.init_J()
         ? captured.init_MapStore_CachePool() == current.init_MapStore_CachePool()
            || captured.init_MapStore_CachePool() == MapStore.Inner_CachePool.load && current.init_MapStore_CachePool() == MapStore.Inner_CachePool.read
         : false;
   }

   private static MapStore.Inner_StateCache init_MapStore_StateCach_Str_Str(String rawUrl, String rawHash) {
      try {
         URL url = new URL(rawUrl);
         autismclient.g.TextFormat.Inner_ConfigStore target = autismclient.g.TextFormat.init_TextFormat_ConfigS_URL(url);
         HashCode hash = null;
         if (rawHash != null && !rawHash.isBlank() && init_Pattern.matcher(rawHash).matches()) {
            hash = HashCode.fromString(rawHash.toLowerCase(Locale.ROOT));
         }

         return new MapStore.Inner_StateCache(url, hash, target);
      } catch (IOException | RuntimeException invalid) {
         return null;
      }
   }

   private static void init_V_AutoCloseable(AutoCloseable closeable) {
      if (closeable != null) {
         try {
            closeable.close();
         } catch (Exception var2) {
         }
      }
   }

   private static void init_V_Path(Path directory) {
      CompletableFuture.runAsync(() -> {
         if (Files.exists(directory)) {
            try (Stream<Path> paths = Files.walk(directory)) {
               for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                  try {
                     Files.deleteIfExists(path);
                  } catch (IOException var6) {
                  }
               }
            } catch (IOException var8) {
            }
         }
      });
   }

   private static final class Inner_BufferHelper {
      private final Callbacks init_PackReloadConfig_C;
      private ReloadableResourceManager init_ReloadableResource;

      private Inner_BufferHelper(Callbacks callbacks) {
         this.init_PackReloadConfig_C = callbacks;
      }
   }

   enum Inner_CachePool {
      init,
      load,
      read;
   }

   private static final class Inner_ConfigStore {
      private final MapStore.Inner_TaskQueue init_MapStore_TaskQueue;
      private final Path init_Path;

      private Inner_ConfigStore(MapStore.Inner_TaskQueue record, Path path) {
         this.init_MapStore_TaskQueue = record;
         this.init_Path = path;
      }
   }

   private final class Inner_DataUtil {
      private final MapStore load_MapStore = MapStore.this;
      private final Connection init_Connection;
      private final int init_I;
      private final long init_J;
      private final Path init_Path;
      private final autismclient.g.BufferHelper.Inner_StreamUtil init_BufferHelper_Strea;
      private final ServerPackManager init_ServerPackManager;
      private final Map<UUID, MapStore.Inner_TaskQueue> init_Map = new LinkedHashMap<>();
      private final ArrayDeque<Runnable> init_ArrayDeque = new ArrayDeque<>();
      private final ArrayDeque<Runnable> load_ArrayDeque = new ArrayDeque<>();
      private final List<MapStore.Inner_DataUtil.Inner_DataUtil_2> init_List = new ArrayList<>();
      private autismclient.g.FileCache.Inner_BufferHelper init_FileCache_BufferHe;
      private MapStore.Inner_TaskQueue init_MapStore_TaskQueue;
      private boolean init_Z;
      private boolean load_Z;
      private ReloadableResourceManager init_ReloadableResource;
      private MapStore.Inner_BufferHelper init_MapStore_BufferHel;
      private int load_I;
      private int read_I;
      private ConfigStore.Inner_DataUtil init_ConfigStore_DataUt;
      private Proxy init_Proxy;
      private boolean read_Z;

      private Inner_DataUtil(Connection connection, SocketAddress peer, int epoch, long generation) {
         this.init_Connection = connection;
         this.init_I = epoch;
         this.init_J = generation;
         this.init_Path = MapStore.this.init_MapStore_StreamUti.cacheDirectory().resolve(UUID.randomUUID().toString());
         autismclient.g.BufferHelper.Inner_StreamUtil createdContext = null;

         try {
            this.init_ServerPackManager = new ServerPackManager(
               new MapStore.Inner_DataUtil.Inner_StreamUtil(),
               new MapStore.Inner_DataUtil.Inner_CachePool(),
               this::init,
               () -> this.init_Z = true,
               PackPromptStatus.ALLOWED
            );
            createdContext = autismclient.g.BufferHelper.init(connection, autismclient.g.BufferHelper.Inner_CachePool.init, true, peer);
            this.init_BufferHelper_Strea = createdContext;
            this.init_V();
            autismclient.g.BufferHelper.init(connection, this.init_ServerPackManager);
            this.init_FileCache_BufferHe = autismclient.g.FileCache.init(this.init_BufferHelper_Strea, this, this.init_ServerPackManager, null);
         } catch (Throwable error) {
            if (createdContext != null) {
               autismclient.g.ConfigStore.init_V(createdContext);
            }

            MapStore.init_V_Path(this.init_Path);
            if (error instanceof RuntimeException runtime) {
               throw runtime;
            } else if (error instanceof IOException io) {
               throw new UncheckedIOException(io);
            } else {
               throw new IllegalStateException(error);
            }
         }
      }

      private void init_V() {
         if (!this.read_Z && ConfigStore.init_Z(this.init_Connection)) {
            ConfigStore.Inner_DataUtil captured = ConfigStore.init_ConfigStore_DataUt(this.init_Connection);
            autismclient.g.BufferHelper.init(
               this.init_Connection,
               captured == null
                  ? null
                  : new autismclient.g.BufferHelper.Inner_ConfigStore(
                     captured.init_bZ(), captured.init_Str(), captured.init_I(), captured.load(), captured.read()
                  )
            );
            this.init_ConfigStore_DataUt = captured;
            this.read_Z = true;
         }
      }

      private Proxy init_Proxy() throws IOException {
         this.init_V();
         if (!this.read_Z) {
            throw new IOException("Multi connection proxy is not registered");
         }

         if (this.init_Proxy == null) {
            this.init_Proxy = ArrayUtil.init_Proxy(this.init_ConfigStore_DataUt);
         }

         return this.init_Proxy;
      }

      private int auditLoadRetries;
      private int auditReadRetries;

      private void load() {
         while (!this.load_Z) {
            Runnable callback = this.init_ArrayDeque.poll();
            if (callback == null) {
               this.auditLoadRetries = 0;
               return;
            }

            try {
               callback.run();
               this.auditLoadRetries = 0;
            } catch (RuntimeException failure) {
               if (failure instanceof IllegalStateException && "Stale resource-pack response".equals(failure.getMessage())) {
                  this.auditLoadRetries = 0;
                  continue;
               }
               if (++this.auditLoadRetries < 3) {
                  this.init_ArrayDeque.addFirst(callback);
                  return;
               }
               this.auditLoadRetries = 0;
            }
         }
      }

      private void read() {
         while (!this.load_Z) {
            Runnable callback = this.load_ArrayDeque.poll();
            if (callback == null) {
               this.auditReadRetries = 0;
               return;
            }

            try {
               callback.run();
               this.auditReadRetries = 0;
            } catch (RuntimeException failure) {
               if (failure instanceof IllegalStateException && "Stale resource-pack response".equals(failure.getMessage())) {
                  this.auditReadRetries = 0;
                  continue;
               }
               if (++this.auditReadRetries < 3) {
                  this.load_ArrayDeque.addFirst(callback);
                  return;
               }
               this.auditReadRetries = 0;
            }
         }
      }

      private void write() {
         if (!this.load_Z) {
            this.load_Z = true;
            autismclient.g.ConfigStore.init_V(this.init_BufferHelper_Strea);
            autismclient.g.FileCache.load_V(this.init_FileCache_BufferHe);
            autismclient.g.UiHelper.init_V(this.init_ServerPackManager);
            List<MapStore.Inner_DataUtil.Inner_DataUtil_2> pending = new ArrayList<>(this.init_List);
            CompletableFuture<?>[] work = pending.stream().map(downloadx -> downloadx.load).toArray(CompletableFuture[]::new);

            for (MapStore.Inner_DataUtil.Inner_DataUtil_2 download : pending) {
               autismclient.g.UiHelper.init_V(download.init_UiHelper_BufferHel);
            }

            this.init_Map.clear();
            this.init_MapStore_TaskQueue = null;
            this.init_ArrayDeque.clear();
            this.load_ArrayDeque.clear();
            if (this.init_MapStore_BufferHel != null) {
               this.init_MapStore_BufferHel.init_PackReloadConfig_C.onFailure(true);
               MapStore.init_V_AutoCloseable(this.init_MapStore_BufferHel.init_ReloadableResource);
               this.init_MapStore_BufferHel = null;
            }

            MapStore.init_V_AutoCloseable(this.init_ReloadableResource);
            this.init_ReloadableResource = null;
            CompletableFuture.allOf(work).whenComplete((ignored, failure) -> MapStore.init_V_Path(this.init_Path));
         }
      }

      private void init(Callbacks callbacks) {
         if (this.load_Z) {
            callbacks.onFailure(true);
         } else if (this.init_ReloadableResource == null && callbacks.packsToLoad().isEmpty()) {
            callbacks.onSuccess();
         } else {
            MapStore.Inner_BufferHelper work = new MapStore.Inner_BufferHelper(callbacks);
            this.init_MapStore_BufferHel = work;
            this.init(work, false);
         }
      }

      private void init(MapStore.Inner_BufferHelper work, boolean recovery) {
         if (!this.load_Z) {
            List<MapStore.Inner_ConfigStore> requested = this.load(work.init_PackReloadConfig_C.packsToLoad());
            if (!this.init_Z(requested)) {
               work.init_PackReloadConfig_C.onFailure(true);
               MapStore.init_V_AutoCloseable(work.init_ReloadableResource);
               this.init_MapStore_BufferHel = null;
            } else {
               ReloadableResourceManager candidate = new ReloadableResourceManager(PackType.CLIENT_RESOURCES);
               work.init_ReloadableResource = candidate;
               List<PackResources> opened = new ArrayList<>();

               try {
                  for (Pack pack : this.init_List(requested)) {
                     opened.addAll(pack.open().toList());
                  }

                  ReloadInstance reload = candidate.createReload(Runnable::run, Runnable::run, CompletableFuture.completedFuture(Unit.INSTANCE), opened);
                  reload.done().join();
               } catch (Throwable failure) {
                  for (PackResources resources : opened) {
                     MapStore.init_V_AutoCloseable(resources);
                  }

                  MapStore.init_V_AutoCloseable(candidate);
                  work.init_ReloadableResource = null;
                  if (!recovery) {
                     work.init_PackReloadConfig_C.onFailure(false);
                     if (!this.load_Z && this.init_MapStore_BufferHel == work) {
                        this.init(work, true);
                     }
                  } else {
                     work.init_PackReloadConfig_C.onFailure(true);
                     this.init_MapStore_BufferHel = null;
                  }

                  return;
               }

               if (!this.load_Z && this.init_MapStore_BufferHel == work && this.init_Z(requested)) {
                  MapStore.init_V_AutoCloseable(this.init_ReloadableResource);
                  this.init_ReloadableResource = candidate;
                  work.init_ReloadableResource = null;
                  this.init_MapStore_BufferHel = null;
                  this.read_I++;
                  work.init_PackReloadConfig_C.onSuccess();
               } else {
                  MapStore.init_V_AutoCloseable(candidate);
                  work.init_ReloadableResource = null;
                  if (!this.load_Z && this.init_MapStore_BufferHel == work) {
                     work.init_PackReloadConfig_C.onFailure(true);
                     this.init_MapStore_BufferHel = null;
                  }
               }
            }
         }
      }

      private List<Pack> init_List(List<MapStore.Inner_ConfigStore> requested) throws IOException {
         List<Pack> packs = new ArrayList<>(requested.size());
         PackFormat current = SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES);

         for (MapStore.Inner_ConfigStore pack : requested) {
            String name = String.format(Locale.ROOT, "server/%08X/%s", this.load_I++, pack.init_MapStore_TaskQueue.init_UUID);
            PackLocationInfo location = new PackLocationInfo(name, MapStore.init_Component, PackSource.SERVER, Optional.empty());
            FileResourcesSupplier resources = new FileResourcesSupplier(pack.init_Path);
            Metadata metadata = Pack.readPackMetadata(location, resources, current, PackType.CLIENT_RESOURCES);
            if (metadata == null) {
               throw new IOException("Invalid resource-pack metadata in " + pack.init_Path);
            }

            packs.add(new Pack(location, resources, metadata, MapStore.init_PackSelectionConfi));
         }

         return packs;
      }

      private List<MapStore.Inner_ConfigStore> load(List rawPacks) {
         List<IdAndPath> reversed = new ArrayList<>();

         for (Object object : rawPacks) {
            reversed.add((IdAndPath)object);
         }

         Collections.reverse(reversed);
         List<MapStore.Inner_ConfigStore> requested = new ArrayList<>(reversed.size());

         for (IdAndPath pack : reversed) {
            MapStore.Inner_TaskQueue record = this.init_Map.get(pack.id());
            requested.add(new MapStore.Inner_ConfigStore(record, pack.path()));
         }

         return requested;
      }

      private boolean init_Z(List<MapStore.Inner_ConfigStore> requested) {
         for (MapStore.Inner_ConfigStore pack : requested) {
            if (pack.init_MapStore_TaskQueue == null
               || pack.init_MapStore_TaskQueue.init_Z
               || this.init_Map.get(pack.init_MapStore_TaskQueue.init_UUID) != pack.init_MapStore_TaskQueue
               || pack.init_MapStore_TaskQueue.init_UiHelper_BufferHel == null
               || !pack.init_MapStore_TaskQueue.init_UiHelper_BufferHel.load_Z()
               || !MapStore.init_Z_MapStore_JsonUtil_MapStore_JsonUtil(pack.init_MapStore_TaskQueue.init_MapStore_JsonUtil, MapStore.this.init_Supplier.get())
               || !autismclient.g.FileCache.init_Z(
                  pack.init_MapStore_TaskQueue.init_FileCache_BufferHe, pack.init_MapStore_TaskQueue.init_UUID, pack.init_MapStore_TaskQueue.init_J
               )) {
               return false;
            }
         }

         return true;
      }

      private final class Inner_CachePool implements PackLoadFeedback {
         @Override
         public void reportUpdate(UUID id, Update update) {
            if (!Inner_DataUtil.this.load_Z) {
               MapStore.Inner_TaskQueue record = Inner_DataUtil.this.init_Map.get(id);
               if (record != null && !record.init_Z) {
                  if (update == Update.ACCEPTED) {
                     record.read = true;
                  }

                  if (update == Update.DOWNLOADED) {
                     record.write = true;
                  }

                  record.load_PackLoadFeedback.reportUpdate(id, update);
               }
            }
         }

         @Override
         public void reportFinalResult(UUID id, FinalResult result) {
            if (!Inner_DataUtil.this.load_Z) {
               MapStore.Inner_TaskQueue record = Inner_DataUtil.this.init_Map.get(id);
               if (record != null && (!record.init_Z || result == FinalResult.DECLINED)) {
                  record.init_PackLoadFeedback_F = result;
                  record.load_PackLoadFeedback.reportFinalResult(id, result);
               }
            }
         }
      }

      private final class Inner_DataUtil_2 implements DownloadProgressListener {
         private final Map<MapStore.Inner_TaskQueue, Long> init_Map;
         private final autismclient.g.UiHelper.Inner_BufferHelper init_UiHelper_BufferHel;
         private final Set<UUID> init_Set;
         private final Consumer<BatchResult> init_Consumer;
         private CompletableFuture<BatchResult> init_CompletableFuture;
         private CompletableFuture<BatchResult> load;
         private DownloadQueue init_DownloadQueue;
         private ArrayUtil.Inner_DataUtil init_ArrayUtil_DataUtil;

         private Inner_DataUtil_2(
            Map<MapStore.Inner_TaskQueue, Long> claims, autismclient.g.UiHelper.Inner_BufferHelper snapshot, Set<UUID> preDenied, Consumer<BatchResult> output
         ) {
            this.init_Map = claims;
            this.init_UiHelper_BufferHel = snapshot;
            this.init_Set = preDenied;
            this.init_Consumer = output;
         }

         @Override
         public synchronized void requestStart() {
            try {
               this.init_ArrayUtil_DataUtil = ArrayUtil.init_ArrayUtil_DataUtil(Inner_DataUtil.this.init_ConfigStore_DataUt);
            } catch (IOException failure) {
               throw new UncheckedIOException(failure);
            }
         }

         @Override
         public void downloadStart(OptionalLong sizeBytes) {
         }

         @Override
         public void downloadedBytes(long bytesSoFar) {
         }

         @Override
         public void requestFinished(boolean success) {
            this.init();
         }

         private void init() {
            ArrayUtil.Inner_DataUtil closing;
            synchronized (this) {
               closing = this.init_ArrayUtil_DataUtil;
               this.init_ArrayUtil_DataUtil = null;
            }

            if (closing != null) {
               closing.close();
            }
         }

         private void load() {
            DownloadQueue closing;
            synchronized (this) {
               closing = this.init_DownloadQueue;
               this.init_DownloadQueue = null;
            }

            MapStore.init_V_AutoCloseable(closing);
         }

         private void init(BatchResult result, Throwable failure) {
            this.init();
            this.load();
            synchronized (MapStore.this.init_Obj) {
               if (!Inner_DataUtil.this.load_Z) {
                  MapStore.this.init_Z_MapStore_DataUtil(Inner_DataUtil.this);
               }

               Inner_DataUtil.this.init_List.remove(this);
               if (!Inner_DataUtil.this.load_Z) {
                  Inner_DataUtil.this.init_ArrayDeque.add(() -> this.load(result, failure));
               }
            }
         }

         private void load(BatchResult result, Throwable failure) {
            if (!Inner_DataUtil.this.load_Z) {
               BatchResult successful = new BatchResult();
               BatchResult failed = new BatchResult();
               this.init(result, failure, successful, failed);
               if (!successful.downloaded().isEmpty() || !failed.failed().isEmpty()) {
                  if (successful.downloaded().isEmpty()) {
                     this.init_Consumer.accept(failed);
                  } else {
                     if (!failed.failed().isEmpty()) {
                        successful.failed().addAll(failed.failed());
                     }

                     this.init_Consumer.accept(successful);
                  }
               }
            }
         }

         private void init(BatchResult result, Throwable failure, BatchResult successful, BatchResult failed) {
            for (MapStore.Inner_TaskQueue record : this.init_Map.keySet()) {
               Long claim = this.init_Map.get(record);
               if (Inner_DataUtil.this.init_Map.get(record.init_UUID) == record
                  && !record.init_Z
                  && claim != null
                  && autismclient.g.FileCache.load(record.init_FileCache_BufferHe, record.init_UUID, claim)) {
                  if (this.init_UiHelper_BufferHel != null
                     && this.init_UiHelper_BufferHel.load_Z()
                     && failure == null
                     && result != null
                     && !this.init_Set.contains(record.init_UUID)
                     && !result.failed().contains(record.init_UUID)) {
                     Path path = result.downloaded().get(record.init_UUID);
                     if (path != null && successful != null) {
                        successful.downloaded().put(record.init_UUID, path);
                        record.init_UiHelper_BufferHel = this.init_UiHelper_BufferHel;
                     }
                  } else {
                     failed.failed().add(record.init_UUID);
                  }
               }
            }
         }
      }

      private final class Inner_StreamUtil implements PackDownloader {
         @Override
         public void download(Map requests, Consumer output) {
            if (!Inner_DataUtil.this.load_Z) {
               Map<UUID, DownloadRequest> filtered = new LinkedHashMap<>();
               Map<MapStore.Inner_TaskQueue, Long> claims = new LinkedHashMap<>();

               for (Object object : requests.entrySet()) {
                  Entry<UUID, DownloadRequest> entry = (Entry<UUID, DownloadRequest>)object;
                  MapStore.Inner_TaskQueue record = Inner_DataUtil.this.init_Map.get(entry.getKey());
                  if (record != null && !record.init_Z) {
                     long claim = autismclient.g.FileCache.load(record.init_FileCache_BufferHe, record.init_UUID);
                     if (claim > 0L) {
                        record.load_Z = true;
                        claims.put(record, claim);
                        filtered.put(record.init_UUID, entry.getValue());
                     }
                  }
               }

               if (!claims.isEmpty()) {
                  autismclient.g.UiHelper.Inner_BufferHelper snapshot = null;
                  Set<UUID> preDenied = new HashSet<>();
                  Throwable setupFailure = null;

                  try {
                     snapshot = autismclient.g.UiHelper.init_UiHelper_BufferHel(Inner_DataUtil.this.init_ServerPackManager);

                     for (MapStore.Inner_TaskQueue record : claims.keySet()) {
                        String host = record.init_MapStore_StateCach.init_TextFormat_ConfigS().load();
                        if (InetAddresses.isInetAddress(host)) {
                           try {
                              autismclient.g.TextFormat.init_TextFormat_CachePo_TextFormat_ConfigS(
                                 record.init_MapStore_StateCach.init_TextFormat_ConfigS(), snapshot, ignored -> List.of(InetAddresses.forString(host))
                              );
                           } catch (IOException | RuntimeException denied) {
                              preDenied.add(record.init_UUID);
                              filtered.remove(record.init_UUID);
                           }
                        }
                     }
                  } catch (Throwable failure) {
                     setupFailure = failure;
                  }

                  MapStore.Inner_DataUtil.Inner_DataUtil_2 work = Inner_DataUtil.this.new Inner_DataUtil_2(claims, snapshot, preDenied, output);
                  if (setupFailure != null) {
                     work.init_CompletableFuture = new CompletableFuture<>();
                     work.init_CompletableFuture.completeExceptionally(setupFailure);
                  } else if (filtered.isEmpty()) {
                     work.init_CompletableFuture = CompletableFuture.completedFuture(new BatchResult());
                  } else {
                     Map<String, String> headers = Map.of("User-Agent", "Minecraft Java/" + SharedConstants.getCurrentVersion().name());
                     DownloadQueue queue = null;

                     try (autismclient.g.UiHelper.Inner_CachePool ignored = autismclient.g.UiHelper.init_UiHelper_CachePool(snapshot)) {
                        Proxy proxy = Inner_DataUtil.this.init_Proxy();
                        queue = new DownloadQueue(Inner_DataUtil.this.init_Path.resolve(UUID.randomUUID().toString()));
                        work.init_DownloadQueue = queue;
                        work.init_CompletableFuture = queue.downloadBatch(new BatchConfig(Hashing.sha1(), 262144000, headers, proxy, work), filtered);
                     } catch (Throwable failure) {
                        MapStore.init_V_AutoCloseable(queue);
                        work.init_DownloadQueue = null;
                        work.init_CompletableFuture = new CompletableFuture<>();
                        work.init_CompletableFuture.completeExceptionally(failure);
                     }
                  }

                  Inner_DataUtil.this.init_List.add(work);
                  work.load = work.init_CompletableFuture.whenComplete(work::init);
               }
            }
         }
      }
   }

   record Inner_JsonUtil(Connection init_Connection, int init_I, MapStore.Inner_CachePool init_MapStore_CachePool, long init_J) {
   }

   @FunctionalInterface
   interface Inner_NetHelper {
      boolean send(MapStore.Inner_JsonUtil var1, UUID var2, long var3, Action var5);
   }

   private record Inner_StateCache(URL init_URL, HashCode init_HashCode, autismclient.g.TextFormat.Inner_ConfigStore init_TextFormat_ConfigS) {
   }

   @FunctionalInterface
   interface Inner_StreamUtil {
      Path cacheDirectory();
   }

   private static final class Inner_TaskQueue {
      private final MapStore.Inner_DataUtil init_MapStore_DataUtil;
      private final UUID init_UUID;
      private final MapStore.Inner_JsonUtil init_MapStore_JsonUtil;
      private final MapStore.Inner_StateCache init_MapStore_StateCach;
      private final boolean init_Z;
      private final Set<Action> init_Set = EnumSet.noneOf(Action.class);
      private autismclient.g.FileCache.Inner_BufferHelper init_FileCache_BufferHe;
      private long init_J;
      private PackLoadFeedback init_PackLoadFeedback;
      private PackLoadFeedback load_PackLoadFeedback;
      private boolean load_Z;
      private boolean read;
      private boolean write;
      private FinalResult init_PackLoadFeedback_F;
      private autismclient.g.UiHelper.Inner_BufferHelper init_UiHelper_BufferHel;

      private Inner_TaskQueue(
         MapStore.Inner_DataUtil bound, UUID id, long generation, MapStore.Inner_JsonUtil target, MapStore.Inner_StateCache parsed, boolean terminalDeclined
      ) {
         this.init_MapStore_DataUtil = bound;
         this.init_UUID = id;
         this.init_FileCache_BufferHe = bound.init_FileCache_BufferHe;
         this.init_J = generation;
         this.init_MapStore_JsonUtil = target;
         this.init_MapStore_StateCach = parsed;
         this.init_Z = terminalDeclined;
         this.init();
      }

      private void init() {
         this.init_PackLoadFeedback = new PackLoadFeedback() {
            @Override
            public void reportUpdate(UUID id, Update update) {
               Action action = update == Update.ACCEPTED ? Action.ACCEPTED : Action.DOWNLOADED;
               Inner_TaskQueue.this.init_MapStore_DataUtil.load_MapStore.init_V_MapStore_TaskQueue_ServerboundResourc(Inner_TaskQueue.this, action);
            }

            @Override
            public void reportFinalResult(UUID id, FinalResult result) {
               Action var10000;
               switch (result) {
                  case APPLIED:
                     var10000 = Action.SUCCESSFULLY_LOADED;
                     break;
                  case DOWNLOAD_FAILED:
                     var10000 = Action.FAILED_DOWNLOAD;
                     break;
                  case DECLINED:
                     var10000 = Action.DECLINED;
                     break;
                  case DISCARDED:
                     var10000 = Action.DISCARDED;
                     break;
                  case ACTIVATION_FAILED:
                     var10000 = Action.FAILED_RELOAD;
                     break;
                  default:
                     throw new MatchException(null, null);
               }

               Action action = var10000;
               Inner_TaskQueue.this.init_MapStore_DataUtil.load_MapStore.init_V_MapStore_TaskQueue_ServerboundResourc(Inner_TaskQueue.this, action);
            }
         };
         this.load_PackLoadFeedback = autismclient.g.EventBus.init(this.init_FileCache_BufferHe, this.init_PackLoadFeedback);
      }
   }
}
