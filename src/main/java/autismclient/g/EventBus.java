package autismclient.g;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.client.resources.server.DownloadedPackSource;
import net.minecraft.client.resources.server.PackDownloader;
import net.minecraft.client.resources.server.PackLoadFeedback;
import net.minecraft.client.resources.server.ServerPackManager;
import net.minecraft.client.resources.server.PackLoadFeedback.FinalResult;
import net.minecraft.client.resources.server.PackLoadFeedback.Update;
import net.minecraft.client.resources.server.PackReloadConfig.IdAndPath;
import net.minecraft.server.packs.OverlayedPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackMetadataResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.DownloadQueue.BatchResult;
import net.minecraft.server.packs.DownloadQueue.DownloadRequest;
import net.minecraft.server.packs.FilePackResources.FileResourcesSupplier;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.Pack.Metadata;

public final class EventBus {
   private static final Object init_Obj = new Object();
   private static final Map<FileCache.Inner_BufferHelper, Map<UUID, EventBus.Inner_DataUtil>> init_Map = new IdentityHashMap<>();

   private EventBus() {
   }

   public static PackDownloader init(final DownloadedPackSource source, final ServerPackManager manager, final PackDownloader original) {
      return FileCache.init(source) == null ? original : new PackDownloader() {
         @Override
         public void download(Map requests, Consumer output) {
            FileCache.Inner_BufferHelper session = FileCache.init(source);
            if (session != null && FileCache.load_Z(session)) {
               EventBus.init(session, manager, original, requests, output);
            } else {
               EventBus.load(session, manager, original, requests, output);
            }
         }
      };
   }

   public static void init(
      FileCache.Inner_BufferHelper session,
      ServerPackManager manager,
      PackDownloader original,
      Map<UUID, DownloadRequest> requests,
      Consumer<BatchResult> output
   ) {
      if (session != null && FileCache.load_Z(session)) {
         Map<UUID, DownloadRequest> vanillaRequests = new LinkedHashMap<>();
         Map<UUID, DownloadRequest> controlledRequests = new LinkedHashMap<>();
         Map<UUID, Long> controlled = new LinkedHashMap<>();
         Set<UUID> omitted = new HashSet<>();
         if (requests != null) {
            for (Entry<UUID, DownloadRequest> entry : requests.entrySet()) {
               UUID packId = entry.getKey();
               long claim = FileCache.load(session, packId);
               if (claim < 0L) {
                  omitted.add(packId);
               } else if (claim > 0L) {
                  controlledRequests.put(packId, entry.getValue());
                  controlled.put(packId, claim);
               } else {
                  vanillaRequests.put(packId, entry.getValue());
               }
            }
         }

         if (controlledRequests.isEmpty()) {
            if (omitted.isEmpty()) {
               load(session, manager, original, requests, output);
            } else if (!vanillaRequests.isEmpty()) {
               load(session, manager, original, vanillaRequests, output);
            }
         } else {
            try {
               UiHelper.Inner_BufferHelper snapshot = UiHelper.init_UiHelper_BufferHel(manager);
               if (vanillaRequests.isEmpty()) {
                  try (UiHelper.Inner_CachePool ignored = UiHelper.init_UiHelper_CachePool(snapshot)) {
                     original.download(controlledRequests, UiHelper.init(snapshot, result -> {
                        if (init(session, result, controlled, omitted)) {
                           output.accept(result);
                        }
                     }));
                  }
               } else {
                  EventBus.Inner_TaskQueue mixed = new EventBus.Inner_TaskQueue(session, controlled, omitted, output);
                  load(session, manager, original, vanillaRequests, mixed::init);

                  try (UiHelper.Inner_CachePool ignored = UiHelper.init_UiHelper_CachePool(snapshot)) {
                     original.download(controlledRequests, mixed::load);
                  }
               }
            } catch (IOException e) {
               throw new IllegalStateException("Refused unscoped resource-pack download", e);
            }
         }
      } else {
         load(session, manager, original, requests, output);
      }
   }

   private static void load(
      FileCache.Inner_BufferHelper session,
      ServerPackManager manager,
      PackDownloader original,
      Map<UUID, DownloadRequest> requests,
      Consumer<BatchResult> output
   ) {
      Map<UUID, Long> generations = new LinkedHashMap<>();
      if (session != null && requests != null) {
         for (UUID id : requests.keySet()) {
            if (FileCache.init_FileCache_DataUtil(session, id) == FileCache.Inner_DataUtil.FULL) {
               generations.put(id, FileCache.init_J(session, id));
            }
         }
      }

      if (generations.isEmpty()) {
         original.download(requests, output);
      } else {
         try {
            UiHelper.Inner_BufferHelper snapshot = UiHelper.init_UiHelper_BufferHel(manager);

            try (UiHelper.Inner_CachePool ignored = UiHelper.init_UiHelper_CachePool(snapshot)) {
               original.download(requests, UiHelper.init(snapshot, result -> {
                  if (result != null) {
                     for (Entry<UUID, Long> entry : generations.entrySet()) {
                        if (!FileCache.load(session, entry.getKey(), entry.getValue())) {
                           result.downloaded().remove(entry.getKey());
                           result.failed().remove(entry.getKey());
                        }
                     }

                     output.accept(result);
                  }
               }));
            }
         } catch (IOException error) {
            throw new IllegalStateException("Refused unscoped full resource-pack download", error);
         }
      }
   }

   private static boolean init(FileCache.Inner_BufferHelper session, BatchResult result, Map<UUID, Long> controlled, Set<UUID> omitted) {
      if (result == null) {
         return false;
      }

      for (UUID packId : omitted) {
         result.downloaded().remove(packId);
         result.failed().remove(packId);
      }

      boolean currentControlled = false;

      for (Entry<UUID, Long> entry : controlled.entrySet()) {
         UUID packId = entry.getKey();
         if (FileCache.load(session, packId, entry.getValue())) {
            currentControlled = true;
         } else {
            result.downloaded().remove(packId);
            result.failed().remove(packId);
         }
      }

      return currentControlled;
   }

   public static boolean init(FileCache.Inner_BufferHelper session, UUID packId, Update update, PackLoadFeedback vanilla) {
      if (!init_Z(session, packId)) {
         return false;
      }

      if (FileCache.init_Z(session) && FileCache.init_FileCache_DataUtil(session, packId) != null) {
         FileCache.init(session, packId, update, vanilla);
      }

      return true;
   }

   public static boolean init(FileCache.Inner_BufferHelper session, UUID packId, FinalResult result, PackLoadFeedback vanilla) {
      if (!init_Z(session, packId)) {
         return false;
      }

      if (FileCache.init_Z(session) && FileCache.init_FileCache_DataUtil(session, packId) != null) {
         FileCache.init(session, packId, result, vanilla);
      }

      return true;
   }

   public static PackLoadFeedback init(final FileCache.Inner_BufferHelper session, final PackLoadFeedback vanilla) {
      return new PackLoadFeedback() {
         @Override
         public void reportUpdate(UUID packId, Update update) {
            if (!EventBus.init(session, packId, update, vanilla)) {
               vanilla.reportUpdate(packId, update);
            }
         }

         @Override
         public void reportFinalResult(UUID packId, FinalResult result) {
            if (!EventBus.init(session, packId, result, vanilla)) {
               vanilla.reportFinalResult(packId, result);
            }
         }
      };
   }

   private static boolean init_Z(FileCache.Inner_BufferHelper session, UUID packId) {
      if (session == null) {
         return false;
      }

      FileCache.Inner_DataUtil policy = FileCache.init_FileCache_DataUtil(session, packId);
      return policy == FileCache.Inner_DataUtil.FULL ? false : policy != null || FileCache.init_Z(session, packId);
   }

   public static FileResourcesSupplier init(FileCache.Inner_BufferHelper session, UUID packId, Path path, Supplier<FileResourcesSupplier> ordinary) {
      if (session == null) {
         return ordinary.get();
      }

      long generation = FileCache.init_J(session, packId);
      FileCache.Inner_DataUtil policy = FileCache.init_FileCache_DataUtil(session, packId);
      EventBus.Inner_ConfigStore isolation = new EventBus.Inner_ConfigStore(session, packId, generation, path);
      if (path != null
         && FileCache.init_Z(session)
         && policy != FileCache.Inner_DataUtil.DECLINE
         && (policy == null || FileCache.init_Z(session, packId, generation))
         && (policy != null || !FileCache.load_Z(session) || CharEncoder.init(session, packId, path))) {
         if (policy != FileCache.Inner_DataUtil.ISOLATED) {
            return ordinary.get();
         }

         synchronized (init_Obj) {
            Map<UUID, EventBus.Inner_DataUtil> sessionSnapshots = init_Map.get(session);
            EventBus.Inner_DataUtil cached = sessionSnapshots == null ? null : sessionSnapshots.get(packId);
            if (cached != null && cached.init_J == generation && cached.init_Path.equals(path)) {
               return cached.init_FilePackResources_;
            }
         }

         JsonUtil.Inner_DataUtil snapshot = JsonUtil.Inner_DataUtil.init(path);
         FileResourcesSupplier supplier = new EventBus.Inner_CachePool(path, snapshot, isolation);
         synchronized (init_Obj) {
            if (!FileCache.init_Z(session, packId, generation)) {
               return new EventBus.Inner_CachePool(path, JsonUtil.Inner_DataUtil.init(), isolation);
            }

            Map<UUID, EventBus.Inner_DataUtil> sessionSnapshots = init_Map.computeIfAbsent(session, ignored -> new LinkedHashMap<>());
            EventBus.Inner_DataUtil cached = sessionSnapshots.get(packId);
            if (cached != null && cached.init_J == generation && cached.init_Path.equals(path)) {
               return cached.init_FilePackResources_;
            }

            sessionSnapshots.put(packId, new EventBus.Inner_DataUtil(generation, path, supplier));
            return supplier;
         }
      } else {
         return new EventBus.Inner_CachePool(path, JsonUtil.Inner_DataUtil.init(), isolation);
      }
   }

   public static void init(FileCache.Inner_BufferHelper session, List<IdAndPath> requested, List<Pack> loaded) {
      try {
         if (loaded == null) {
            CharEncoder.init_V(session, null);
         } else {
            CharEncoder.init(session, requested, loaded);
         }
      } finally {
         init_V(session, null);
      }
   }

   static void init_V(FileCache.Inner_BufferHelper session, UUID packId) {
      if (session != null) {
         synchronized (init_Obj) {
            if (packId == null) {
               init_Map.remove(session);
            } else {
               Map<UUID, EventBus.Inner_DataUtil> sessionSnapshots = init_Map.get(session);
               if (sessionSnapshots != null) {
                  sessionSnapshots.remove(packId);
                  if (sessionSnapshots.isEmpty()) {
                     init_Map.remove(session);
                  }
               }
            }
         }
      }
   }

   static int init(FileCache.Inner_BufferHelper session) {
      synchronized (init_Obj) {
         Map<UUID, EventBus.Inner_DataUtil> sessionSnapshots = init_Map.get(session);
         return sessionSnapshots == null ? 0 : sessionSnapshots.size();
      }
   }

   interface Inner_BufferHelper {
      EventBus.Inner_ConfigStore init();
   }

   private static final class Inner_CachePool extends FileResourcesSupplier {
      private final JsonUtil.Inner_DataUtil init_JsonUtil_DataUtil;
      private final EventBus.Inner_ConfigStore init_EventBus_ConfigSto;

      private Inner_CachePool(Path path, JsonUtil.Inner_DataUtil snapshot, EventBus.Inner_ConfigStore isolation) {
         super(path);
         this.init_JsonUtil_DataUtil = snapshot;
         this.init_EventBus_ConfigSto = isolation;
      }

      @Override
      public PackMetadataResources openMetadata(PackLocationInfo location) {
         return this.init_JsonUtil_DataUtil.init(location, "", true, this.init_EventBus_ConfigSto);
      }

      @Override
      public Stream<PackResources> openResources(PackLocationInfo location, Metadata metadata) {
         PackResources primary = this.init_JsonUtil_DataUtil.init(location, "", false, this.init_EventBus_ConfigSto);
         List<PackResources> overlays = new ArrayList<>();

         for (String overlay : metadata.overlays()) {
            if (JsonUtil.Inner_DataUtil.init(overlay)) {
               overlays.add(this.init_JsonUtil_DataUtil.init(location, overlay, false, this.init_EventBus_ConfigSto));
            }
         }

         return Stream.of(overlays.isEmpty() ? primary : new EventBus.Inner_StreamUtil(primary, overlays, this.init_EventBus_ConfigSto));
      }
   }

   record Inner_ConfigStore(FileCache.Inner_BufferHelper init_FileCache_BufferHe, UUID init_UUID, long init_J, Path init_Path) {
   }

   private record Inner_DataUtil(long init_J, Path init_Path, FileResourcesSupplier init_FilePackResources_) {
   }

   private static final class Inner_StreamUtil extends OverlayedPackResources implements EventBus.Inner_BufferHelper {
      private final EventBus.Inner_ConfigStore init;

      private Inner_StreamUtil(PackResources primary, List<PackResources> overlays, EventBus.Inner_ConfigStore isolation) {
         super(primary, overlays);
         this.init = isolation;
      }

      @Override
      public EventBus.Inner_ConfigStore init() {
         return this.init;
      }
   }

   private static final class Inner_TaskQueue {
      private final FileCache.Inner_BufferHelper init_FileCache_BufferHe;
      private final Map<UUID, Long> init_Map;
      private final Set<UUID> init_Set;
      private final Consumer<BatchResult> init_Consumer;
      private BatchResult init_DownloadQueue_Batc;
      private BatchResult load_DownloadQueue_Batc;
      private boolean init_Z;
      private boolean load_Z;
      private boolean read;

      private Inner_TaskQueue(FileCache.Inner_BufferHelper session, Map<UUID, Long> controlled, Set<UUID> omitted, Consumer<BatchResult> output) {
         this.init_FileCache_BufferHe = session;
         this.init_Map = controlled;
         this.init_Set = omitted;
         this.init_Consumer = output;
      }

      private void init(BatchResult result) {
         this.init(true, result);
      }

      private void load(BatchResult result) {
         this.init(false, result);
      }

      private void init(boolean vanilla, BatchResult result) {
         BatchResult merged;
         synchronized (this) {
            if (vanilla) {
               if (this.init_Z) {
                  return;
               }

               this.init_Z = true;
               this.init_DownloadQueue_Batc = result;
            } else {
               if (this.load_Z) {
                  return;
               }

               this.load_Z = true;
               this.load_DownloadQueue_Batc = result;
            }

            if (!this.init_Z || !this.load_Z || this.read) {
               return;
            }

            this.read = true;
            merged = new BatchResult();
            if (this.init_DownloadQueue_Batc != null) {
               merged.downloaded().putAll(this.init_DownloadQueue_Batc.downloaded());
               merged.failed().addAll(this.init_DownloadQueue_Batc.failed());
            }

            if (EventBus.init(this.init_FileCache_BufferHe, this.load_DownloadQueue_Batc, this.init_Map, this.init_Set)) {
               merged.downloaded().putAll(this.load_DownloadQueue_Batc.downloaded());
               merged.failed().addAll(this.load_DownloadQueue_Batc.failed());
            }
         }

         this.init_Consumer.accept(merged);
      }
   }
}
