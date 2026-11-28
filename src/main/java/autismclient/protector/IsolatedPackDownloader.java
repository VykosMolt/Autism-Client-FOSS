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
   private static final Map<ResourcePackSessionTracker.PackSession, Map<UUID, IsolatedPackDownloader.CachedSnapshot>> SNAPSHOTS = new IdentityHashMap<>();

   private IsolatedPackDownloader() {
   }

   public static PackDownloader wrapDownloader(final DownloadedPackSource source, final ServerPackManager manager, final PackDownloader original) {
      return ResourcePackSessionTracker.sessionForSource(source) == null ? original : new PackDownloader() {
         @Override
         public void download(Map requests, Consumer output) {
            ResourcePackSessionTracker.PackSession session = ResourcePackSessionTracker.sessionForSource(source);
            if (session != null && ResourcePackSessionTracker.hasFilteredPack(session)) {
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
      if (session != null && ResourcePackSessionTracker.hasFilteredPack(session)) {
         Map<UUID, DownloadRequest> vanillaRequests = new LinkedHashMap<>();
         Map<UUID, DownloadRequest> controlledRequests = new LinkedHashMap<>();
         Map<UUID, Long> controlled = new LinkedHashMap<>();
         Set<UUID> omitted = new HashSet<>();
         if (requests != null) {
            for (Entry<UUID, DownloadRequest> entry : requests.entrySet()) {
               UUID packId = entry.getKey();
               long claim = ResourcePackSessionTracker.claimDownload(session, packId);
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
                  IsolatedPackDownloader.MixedBatchCollector mixed = new IsolatedPackDownloader.MixedBatchCollector(session, controlled, omitted, output);
                  load(session, manager, original, vanillaRequests, mixed::acceptVanilla);

                  try (ResourcePackDownloadScope.Inner_CachePool ignored = ResourcePackDownloadScope.enter(snapshot)) {
                     original.download(controlledRequests, mixed::acceptControlled);
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
                        if (!ResourcePackSessionTracker.isClaimCurrent(session, entry.getKey(), entry.getValue())) {
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
         if (ResourcePackSessionTracker.isClaimCurrent(session, packId, entry.getValue())) {
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
         ResourcePackSessionTracker.onPackUpdate(session, packId, update, vanilla);
      }

      return true;
   }

   public static boolean init(ResourcePackSessionTracker.PackSession session, UUID packId, FinalResult result, PackLoadFeedback vanilla) {
      if (!init_Z(session, packId)) {
         return false;
      }

      if (ResourcePackSessionTracker.isActive(session) && ResourcePackSessionTracker.policyFor(session, packId) != null) {
         ResourcePackSessionTracker.onPackResult(session, packId, result, vanilla);
      }

      return true;
   }

   public static PackLoadFeedback wrapFeedback(final ResourcePackSessionTracker.PackSession session, final PackLoadFeedback vanilla) {
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
      return policy == ResourcePackSessionTracker.PackPolicy.FULL ? false : policy != null || ResourcePackSessionTracker.hasFilteredPackFor(session, packId);
   }

   public static FileResourcesSupplier isolatedResources(ResourcePackSessionTracker.PackSession session, UUID packId, Path path, Supplier<FileResourcesSupplier> ordinary) {
      if (session == null) {
         return ordinary.get();
      }

      long generation = ResourcePackSessionTracker.generation(session, packId);
      ResourcePackSessionTracker.PackPolicy policy = ResourcePackSessionTracker.policyFor(session, packId);
      IsolatedPackDownloader.PackIsolation isolation = new IsolatedPackDownloader.PackIsolation(session, packId, generation, path);
      if (path != null
         && ResourcePackSessionTracker.isActive(session)
         && policy != ResourcePackSessionTracker.PackPolicy.DECLINE
         && (policy == null || ResourcePackSessionTracker.isCurrent(session, packId, generation))
         && (policy != null || !ResourcePackSessionTracker.hasFilteredPack(session) || ServerPackSelectionRegistry.init(session, packId, path))) {
         if (policy != ResourcePackSessionTracker.PackPolicy.ISOLATED) {
            return ordinary.get();
         }

         synchronized (LOCK) {
            Map<UUID, IsolatedPackDownloader.CachedSnapshot> sessionSnapshots = SNAPSHOTS.get(session);
            IsolatedPackDownloader.CachedSnapshot cached = sessionSnapshots == null ? null : sessionSnapshots.get(packId);
            if (cached != null && cached.generation == generation && cached.path.equals(path)) {
               return cached.supplier;
            }
         }

         IsolatedResourcePack.Inner_DataUtil snapshot = IsolatedResourcePack.Inner_DataUtil.fromPath(path);
         FileResourcesSupplier supplier = new IsolatedPackDownloader.IsolatedResourcesSupplier(path, snapshot, isolation);
         synchronized (LOCK) {
            if (!ResourcePackSessionTracker.isCurrent(session, packId, generation)) {
               return new IsolatedPackDownloader.IsolatedResourcesSupplier(path, IsolatedResourcePack.Inner_DataUtil.empty(), isolation);
            }

            Map<UUID, IsolatedPackDownloader.CachedSnapshot> sessionSnapshots = SNAPSHOTS.computeIfAbsent(session, ignored -> new LinkedHashMap<>());
            IsolatedPackDownloader.CachedSnapshot cached = sessionSnapshots.get(packId);
            if (cached != null && cached.generation == generation && cached.path.equals(path)) {
               return cached.supplier;
            }

            sessionSnapshots.put(packId, new IsolatedPackDownloader.CachedSnapshot(generation, path, supplier));
            return supplier;
         }
      } else {
         return new IsolatedPackDownloader.IsolatedResourcesSupplier(path, IsolatedResourcePack.Inner_DataUtil.empty(), isolation);
      }
   }

   public static void recordLoadedPacks(ResourcePackSessionTracker.PackSession session, List<IdAndPath> requested, List<Pack> loaded) {
      try {
         if (loaded == null) {
            ServerPackSelectionRegistry.init_V(session, null);
         } else {
            ServerPackSelectionRegistry.recordSelection(session, requested, loaded);
         }
      } finally {
         dropSnapshots(session, null);
      }
   }

   static void dropSnapshots(ResourcePackSessionTracker.PackSession session, UUID packId) {
      if (session != null) {
         synchronized (LOCK) {
            if (packId == null) {
               SNAPSHOTS.remove(session);
            } else {
               Map<UUID, IsolatedPackDownloader.CachedSnapshot> sessionSnapshots = SNAPSHOTS.get(session);
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
         Map<UUID, IsolatedPackDownloader.CachedSnapshot> sessionSnapshots = SNAPSHOTS.get(session);
         return sessionSnapshots == null ? 0 : sessionSnapshots.size();
      }
   }

   interface IsolationAware {
      IsolatedPackDownloader.PackIsolation packIsolation();
   }

   private static final class IsolatedResourcesSupplier extends FileResourcesSupplier {
      private final IsolatedResourcePack.Inner_DataUtil snapshot;
      private final IsolatedPackDownloader.PackIsolation isolation;

      private IsolatedResourcesSupplier(Path path, IsolatedResourcePack.Inner_DataUtil snapshot, IsolatedPackDownloader.PackIsolation isolation) {
         super(path);
         this.snapshot = snapshot;
         this.isolation = isolation;
      }

      @Override
      public PackMetadataResources openMetadata(PackLocationInfo location) {
         return this.snapshot.toPack(location, "", true, this.isolation);
      }

      @Override
      public Stream<PackResources> openResources(PackLocationInfo location, Metadata metadata) {
         PackResources primary = this.snapshot.toPack(location, "", false, this.isolation);
         List<PackResources> overlays = new ArrayList<>();

         for (String overlay : metadata.overlays()) {
            if (IsolatedResourcePack.Inner_DataUtil.isValidOverlay(overlay)) {
               overlays.add(this.snapshot.toPack(location, overlay, false, this.isolation));
            }
         }

         return Stream.of(overlays.isEmpty() ? primary : new IsolatedPackDownloader.IsolatedOverlayResources(primary, overlays, this.isolation));
      }
   }

   record PackIsolation(ResourcePackSessionTracker.PackSession session, UUID packId, long generation, Path path) {
   }

   private record CachedSnapshot(long generation, Path path, FileResourcesSupplier supplier) {
   }

   private static final class IsolatedOverlayResources extends OverlayedPackResources implements IsolatedPackDownloader.IsolationAware {
      private final IsolatedPackDownloader.PackIsolation isolation;

      private IsolatedOverlayResources(PackResources primary, List<PackResources> overlays, IsolatedPackDownloader.PackIsolation isolation) {
         super(primary, overlays);
         this.isolation = isolation;
      }

      @Override
      public IsolatedPackDownloader.PackIsolation packIsolation() {
         return this.isolation;
      }
   }

   private static final class MixedBatchCollector {
      private final ResourcePackSessionTracker.PackSession session;
      private final Map<UUID, Long> controlled;
      private final Set<UUID> omitted;
      private final Consumer<BatchResult> output;
      private BatchResult vanillaResult;
      private BatchResult controlledResult;
      private boolean vanillaDone;
      private boolean controlledDone;
      private boolean completed;

      private MixedBatchCollector(ResourcePackSessionTracker.PackSession session, Map<UUID, Long> controlled, Set<UUID> omitted, Consumer<BatchResult> output) {
         this.session = session;
         this.controlled = controlled;
         this.omitted = omitted;
         this.output = output;
      }

      private void acceptVanilla(BatchResult result) {
         this.accept(true, result);
      }

      private void acceptControlled(BatchResult result) {
         this.accept(false, result);
      }

      private void accept(boolean vanilla, BatchResult result) {
         BatchResult merged;
         synchronized (this) {
            if (vanilla) {
               if (this.vanillaDone) {
                  return;
               }

               this.vanillaDone = true;
               this.vanillaResult = result;
            } else {
               if (this.controlledDone) {
                  return;
               }

               this.controlledDone = true;
               this.controlledResult = result;
            }

            if (!this.vanillaDone || !this.controlledDone || this.completed) {
               return;
            }

            this.completed = true;
            merged = new BatchResult();
            if (this.vanillaResult != null) {
               merged.downloaded().putAll(this.vanillaResult.downloaded());
               merged.failed().addAll(this.vanillaResult.failed());
            }

            if (IsolatedPackDownloader.init(this.session, this.controlledResult, this.controlled, this.omitted)) {
               merged.downloaded().putAll(this.controlledResult.downloaded());
               merged.failed().addAll(this.controlledResult.failed());
            }
         }

         this.output.accept(merged);
      }
   }
}
