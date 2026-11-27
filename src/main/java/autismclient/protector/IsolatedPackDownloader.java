package autismclient.protector;

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

public final class IsolatedPackDownloader {
   private static final Object LOCK = new Object();
   private static final Map<ResourcePackSessionTracker.PackSession, Map<UUID, IsolatedPackDownloader.Inner_DataUtil>> SNAPSHOTS = new IdentityHashMap<>();

   private IsolatedPackDownloader() {
   }

   public static PackDownloader init(final DownloadedPackSource source, final ServerPackManager manager, final PackDownloader original) {
      return ResourcePackSessionTracker.init(source) == null ? original : new PackDownloader() {
         @Override
         public void download(Map requests, Consumer output) {
            ResourcePackSessionTracker.PackSession session = ResourcePackSessionTracker.init(source);
            if (session != null && ResourcePackSessionTracker.load_Z(session)) {
               IsolatedPackDownloader.init(session, manager, original, requests, output);
            } else {
               IsolatedPackDownloader.load(session, manager, original, requests, output);
            }
         }
      };
   }

   public static void init(
      ResourcePackSessionTracker.PackSession session,
      ServerPackManager manager,
      PackDownloader original,
      Map<UUID, DownloadRequest> requests,
      Consumer<BatchResult> output
   ) {
      if (session != null && ResourcePackSessionTracker.load_Z(session)) {
         Map<UUID, DownloadRequest> vanillaRequests = new LinkedHashMap<>();
         Map<UUID, DownloadRequest> controlledRequests = new LinkedHashMap<>();
         Map<UUID, Long> controlled = new LinkedHashMap<>();
         Set<UUID> omitted = new HashSet<>();
         if (requests != null) {
            for (Entry<UUID, DownloadRequest> entry : requests.entrySet()) {
               UUID packId = entry.getKey();
               long claim = ResourcePackSessionTracker.load(session, packId);
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
               ResourcePackDownloadScope.Inner_BufferHelper snapshot = ResourcePackDownloadScope.snapshot(manager);
               if (vanillaRequests.isEmpty()) {
                  try (ResourcePackDownloadScope.Inner_CachePool ignored = ResourcePackDownloadScope.enter(snapshot)) {
                     original.download(controlledRequests, ResourcePackDownloadScope.init(snapshot, result -> {
                        if (init(session, result, controlled, omitted)) {
                           output.accept(result);
                        }
                     }));
                  }
               } else {
                  IsolatedPackDownloader.Inner_TaskQueue mixed = new IsolatedPackDownloader.Inner_TaskQueue(session, controlled, omitted, output);
                  load(session, manager, original, vanillaRequests, mixed::init);

                  try (ResourcePackDownloadScope.Inner_CachePool ignored = ResourcePackDownloadScope.enter(snapshot)) {
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
      ResourcePackSessionTracker.PackSession session,
      ServerPackManager manager,
      PackDownloader original,
      Map<UUID, DownloadRequest> requests,
      Consumer<BatchResult> output
   ) {
      Map<UUID, Long> generations = new LinkedHashMap<>();
      if (session != null && requests != null) {
         for (UUID id : requests.keySet()) {
            if (ResourcePackSessionTracker.policyFor(session, id) == ResourcePackSessionTracker.PackPolicy.FULL) {
               generations.put(id, ResourcePackSessionTracker.generation(session, id));
            }
         }
      }

      if (generations.isEmpty()) {
         original.download(requests, output);
      } else {
         try {
            ResourcePackDownloadScope.Inner_BufferHelper snapshot = ResourcePackDownloadScope.snapshot(manager);

            try (ResourcePackDownloadScope.Inner_CachePool ignored = ResourcePackDownloadScope.enter(snapshot)) {
               original.download(requests, ResourcePackDownloadScope.init(snapshot, result -> {
                  if (result != null) {
                     for (Entry<UUID, Long> entry : generations.entrySet()) {
                        if (!ResourcePackSessionTracker.load(session, entry.getKey(), entry.getValue())) {
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

   private static boolean init(ResourcePackSessionTracker.PackSession session, BatchResult result, Map<UUID, Long> controlled, Set<UUID> omitted) {
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
         if (ResourcePackSessionTracker.load(session, packId, entry.getValue())) {
            currentControlled = true;
         } else {
            result.downloaded().remove(packId);
            result.failed().remove(packId);
         }
      }

      return currentControlled;
   }

   public static boolean init(ResourcePackSessionTracker.PackSession session, UUID packId, Update update, PackLoadFeedback vanilla) {
      if (!init_Z(session, packId)) {
         return false;
      }

      if (ResourcePackSessionTracker.isActive(session) && ResourcePackSessionTracker.policyFor(session, packId) != null) {
         ResourcePackSessionTracker.init(session, packId, update, vanilla);
      }

      return true;
   }

   public static boolean init(ResourcePackSessionTracker.PackSession session, UUID packId, FinalResult result, PackLoadFeedback vanilla) {
      if (!init_Z(session, packId)) {
         return false;
      }

      if (ResourcePackSessionTracker.isActive(session) && ResourcePackSessionTracker.policyFor(session, packId) != null) {
         ResourcePackSessionTracker.init(session, packId, result, vanilla);
      }

      return true;
   }

   public static PackLoadFeedback init(final ResourcePackSessionTracker.PackSession session, final PackLoadFeedback vanilla) {
      return new PackLoadFeedback() {
         @Override
         public void reportUpdate(UUID packId, Update update) {
            if (!IsolatedPackDownloader.init(session, packId, update, vanilla)) {
               vanilla.reportUpdate(packId, update);
            }
         }

         @Override
         public void reportFinalResult(UUID packId, FinalResult result) {
            if (!IsolatedPackDownloader.init(session, packId, result, vanilla)) {
               vanilla.reportFinalResult(packId, result);
            }
         }
      };
   }

   private static boolean init_Z(ResourcePackSessionTracker.PackSession session, UUID packId) {
      if (session == null) {
         return false;
      }

      ResourcePackSessionTracker.PackPolicy policy = ResourcePackSessionTracker.policyFor(session, packId);
      return policy == ResourcePackSessionTracker.PackPolicy.FULL ? false : policy != null || ResourcePackSessionTracker.init_Z(session, packId);
   }

   public static FileResourcesSupplier init(ResourcePackSessionTracker.PackSession session, UUID packId, Path path, Supplier<FileResourcesSupplier> ordinary) {
      if (session == null) {
         return ordinary.get();
      }

      long generation = ResourcePackSessionTracker.generation(session, packId);
      ResourcePackSessionTracker.PackPolicy policy = ResourcePackSessionTracker.policyFor(session, packId);
      IsolatedPackDownloader.Inner_ConfigStore isolation = new IsolatedPackDownloader.Inner_ConfigStore(session, packId, generation, path);
      if (path != null
         && ResourcePackSessionTracker.isActive(session)
         && policy != ResourcePackSessionTracker.PackPolicy.DECLINE
         && (policy == null || ResourcePackSessionTracker.isCurrent(session, packId, generation))
         && (policy != null || !ResourcePackSessionTracker.load_Z(session) || ServerPackSelectionRegistry.init(session, packId, path))) {
         if (policy != ResourcePackSessionTracker.PackPolicy.ISOLATED) {
            return ordinary.get();
         }

         synchronized (LOCK) {
            Map<UUID, IsolatedPackDownloader.Inner_DataUtil> sessionSnapshots = SNAPSHOTS.get(session);
            IsolatedPackDownloader.Inner_DataUtil cached = sessionSnapshots == null ? null : sessionSnapshots.get(packId);
            if (cached != null && cached.generation == generation && cached.path.equals(path)) {
               return cached.supplier;
            }
         }

         IsolatedResourcePack.Inner_DataUtil snapshot = IsolatedResourcePack.Inner_DataUtil.init(path);
         FileResourcesSupplier supplier = new IsolatedPackDownloader.Inner_CachePool(path, snapshot, isolation);
         synchronized (LOCK) {
            if (!ResourcePackSessionTracker.isCurrent(session, packId, generation)) {
               return new IsolatedPackDownloader.Inner_CachePool(path, IsolatedResourcePack.Inner_DataUtil.init(), isolation);
            }

            Map<UUID, IsolatedPackDownloader.Inner_DataUtil> sessionSnapshots = SNAPSHOTS.computeIfAbsent(session, ignored -> new LinkedHashMap<>());
            IsolatedPackDownloader.Inner_DataUtil cached = sessionSnapshots.get(packId);
            if (cached != null && cached.generation == generation && cached.path.equals(path)) {
               return cached.supplier;
            }

            sessionSnapshots.put(packId, new IsolatedPackDownloader.Inner_DataUtil(generation, path, supplier));
            return supplier;
         }
      } else {
         return new IsolatedPackDownloader.Inner_CachePool(path, IsolatedResourcePack.Inner_DataUtil.init(), isolation);
      }
   }

   public static void init(ResourcePackSessionTracker.PackSession session, List<IdAndPath> requested, List<Pack> loaded) {
      try {
         if (loaded == null) {
            ServerPackSelectionRegistry.init_V(session, null);
         } else {
            ServerPackSelectionRegistry.init(session, requested, loaded);
         }
      } finally {
         init_V(session, null);
      }
   }

   static void init_V(ResourcePackSessionTracker.PackSession session, UUID packId) {
      if (session != null) {
         synchronized (LOCK) {
            if (packId == null) {
               SNAPSHOTS.remove(session);
            } else {
               Map<UUID, IsolatedPackDownloader.Inner_DataUtil> sessionSnapshots = SNAPSHOTS.get(session);
               if (sessionSnapshots != null) {
                  sessionSnapshots.remove(packId);
                  if (sessionSnapshots.isEmpty()) {
                     SNAPSHOTS.remove(session);
                  }
               }
            }
         }
      }
   }

   static int init(ResourcePackSessionTracker.PackSession session) {
      synchronized (LOCK) {
         Map<UUID, IsolatedPackDownloader.Inner_DataUtil> sessionSnapshots = SNAPSHOTS.get(session);
         return sessionSnapshots == null ? 0 : sessionSnapshots.size();
      }
   }

   interface Inner_BufferHelper {
      IsolatedPackDownloader.Inner_ConfigStore init();
   }

   private static final class Inner_CachePool extends FileResourcesSupplier {
      private final IsolatedResourcePack.Inner_DataUtil snapshot;
      private final IsolatedPackDownloader.Inner_ConfigStore isolation;

      private Inner_CachePool(Path path, IsolatedResourcePack.Inner_DataUtil snapshot, IsolatedPackDownloader.Inner_ConfigStore isolation) {
         super(path);
         this.snapshot = snapshot;
         this.isolation = isolation;
      }

      @Override
      public PackMetadataResources openMetadata(PackLocationInfo location) {
         return this.snapshot.init(location, "", true, this.isolation);
      }

      @Override
      public Stream<PackResources> openResources(PackLocationInfo location, Metadata metadata) {
         PackResources primary = this.snapshot.init(location, "", false, this.isolation);
         List<PackResources> overlays = new ArrayList<>();

         for (String overlay : metadata.overlays()) {
            if (IsolatedResourcePack.Inner_DataUtil.init(overlay)) {
               overlays.add(this.snapshot.init(location, overlay, false, this.isolation));
            }
         }

         return Stream.of(overlays.isEmpty() ? primary : new IsolatedPackDownloader.Inner_StreamUtil(primary, overlays, this.isolation));
      }
   }

   record Inner_ConfigStore(ResourcePackSessionTracker.PackSession session, UUID packId, long generation, Path path) {
   }

   private record Inner_DataUtil(long generation, Path path, FileResourcesSupplier supplier) {
   }

   private static final class Inner_StreamUtil extends OverlayedPackResources implements IsolatedPackDownloader.Inner_BufferHelper {
      private final IsolatedPackDownloader.Inner_ConfigStore init;

      private Inner_StreamUtil(PackResources primary, List<PackResources> overlays, IsolatedPackDownloader.Inner_ConfigStore isolation) {
         super(primary, overlays);
         this.init = isolation;
      }

      @Override
      public IsolatedPackDownloader.Inner_ConfigStore init() {
         return this.init;
      }
   }

   private static final class Inner_TaskQueue {
      private final ResourcePackSessionTracker.PackSession session;
      private final Map<UUID, Long> controlled;
      private final Set<UUID> omitted;
      private final Consumer<BatchResult> output;
      private BatchResult init_DownloadQueue_Batc;
      private BatchResult load_DownloadQueue_Batc;
      private boolean init_Z;
      private boolean load_Z;
      private boolean read;

      private Inner_TaskQueue(ResourcePackSessionTracker.PackSession session, Map<UUID, Long> controlled, Set<UUID> omitted, Consumer<BatchResult> output) {
         this.session = session;
         this.controlled = controlled;
         this.omitted = omitted;
         this.output = output;
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

            if (IsolatedPackDownloader.init(this.session, this.load_DownloadQueue_Batc, this.controlled, this.omitted)) {
               merged.downloaded().putAll(this.load_DownloadQueue_Batc.downloaded());
               merged.failed().addAll(this.load_DownloadQueue_Batc.failed());
            }
         }

         this.output.accept(merged);
      }
   }
}
