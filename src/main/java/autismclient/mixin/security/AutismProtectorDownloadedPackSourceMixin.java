package autismclient.mixin.security;

import autismclient.g.ConnectionPrivacyRegistry;
import autismclient.g.IsolatedPackDownloader;
import autismclient.g.PackFailureGuard;
import autismclient.g.ResourcePackSessionTracker;
import autismclient.g.ResourcePackDownloadScope;
import autismclient.mixin.accessor.AutismServerPackManagerAccessor;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.resources.server.DownloadedPackSource;
import net.minecraft.client.resources.server.PackDownloader;
import net.minecraft.client.resources.server.PackLoadFeedback;
import net.minecraft.client.resources.server.ServerPackManager;
import net.minecraft.client.resources.server.PackLoadFeedback.FinalResult;
import net.minecraft.client.resources.server.PackLoadFeedback.Update;
import net.minecraft.client.resources.server.PackReloadConfig.Callbacks;
import net.minecraft.client.resources.server.PackReloadConfig.IdAndPath;
import net.minecraft.client.resources.server.ServerPackManager.PackPromptStatus;
import net.minecraft.network.Connection;
import net.minecraft.server.packs.FilePackResources.FileResourcesSupplier;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DownloadedPackSource.class)
public abstract class AutismProtectorDownloadedPackSourceMixin {
   @Shadow
   @Final
   private ServerPackManager manager;
   @Shadow
   private PackLoadFeedback packFeedback;
   @Shadow
   private Callbacks pendingReload;
   @Unique
   private PackDownloader autism$originalDownloader;
   @Unique
   private PackDownloader autism$scopedDownloader;
   @Unique
   private ResourcePackSessionTracker.Inner_BufferHelper autism$downloaderSession;
   @Unique
   private final java.util.concurrent.ConcurrentHashMap<UUID, java.util.concurrent.CopyOnWriteArrayList<java.util.function.BiConsumer<UUID, FinalResult>>> autism$waitCallbacks = new java.util.concurrent.ConcurrentHashMap<>();
   @Unique
   private PackLoadFeedback autism$waitRouterBase;
   @Unique
   private PackLoadFeedback autism$waitRouter;

   @Shadow
   public abstract void onRecoveryFailure();

   @Inject(method = "createRepositorySource", at = @At("RETURN"), cancellable = true)
   private void autism$suppressDownloadedPackSourceAfterFailure(CallbackInfoReturnable<RepositorySource> cir) {
      RepositorySource original = cir.getReturnValue();
      cir.setReturnValue(output -> {
         if (!PackFailureGuard.init_Z()) {
            original.loadPacks(output);
         }
      });
   }

   @Inject(method = "configureForServerControl", at = @At("HEAD"))
   private void autism$registerPackSession(Connection connection, PackPromptStatus promptStatus, CallbackInfo ci) {
      ConnectionPrivacyRegistry.Inner_StreamUtil context = ConnectionPrivacyRegistry.init_BufferHelper_Strea(connection);
      if (context != null) {
         ResourcePackSessionTracker.Inner_BufferHelper contextSession = ResourcePackSessionTracker.init_FileCache_BufferHe(context);
         ResourcePackSessionTracker.Inner_BufferHelper sourceSession = ResourcePackSessionTracker.init(this);
         ResourcePackSessionTracker.init_V(contextSession);
         if (sourceSession != null && sourceSession != contextSession) {
            ResourcePackSessionTracker.load_V(sourceSession);
         }

         if (this.autism$downloaderSession != null && this.autism$downloaderSession != contextSession && this.autism$downloaderSession != sourceSession) {
            ResourcePackSessionTracker.load_V(this.autism$downloaderSession);
         }

         this.autism$restoreDownloader();
         ConnectionPrivacyRegistry.init(connection, this.manager);
         ResourcePackSessionTracker.Inner_BufferHelper session = ResourcePackSessionTracker.init(context, this, this.manager, this::autism$recoverAfterDisconnect);
         this.autism$installDownloader(session);
      }
   }

   @Unique
   private void autism$installDownloader(ResourcePackSessionTracker.Inner_BufferHelper session) {
      AutismServerPackManagerAccessor accessor = (AutismServerPackManagerAccessor)(Object)this.manager;
      PackDownloader current = accessor.autism$downloader();
      if (session != this.autism$downloaderSession || current != this.autism$scopedDownloader) {
         PackDownloader original = current == this.autism$scopedDownloader ? this.autism$originalDownloader : current;
         PackDownloader scoped = IsolatedPackDownloader.init((DownloadedPackSource)(Object)this, this.manager, original);
         this.autism$originalDownloader = original;
         this.autism$scopedDownloader = scoped;
         this.autism$downloaderSession = session;
         accessor.autism$downloader(scoped);
      }
   }

   @Unique
   private void autism$restoreDownloader() {
      if (this.autism$scopedDownloader != null) {
         AutismServerPackManagerAccessor accessor = (AutismServerPackManagerAccessor)(Object)this.manager;
         if (accessor.autism$downloader() == this.autism$scopedDownloader) {
            accessor.autism$downloader(this.autism$originalDownloader);
         }
      }

      this.autism$originalDownloader = null;
      this.autism$scopedDownloader = null;
      this.autism$downloaderSession = null;
   }

   @Unique
   private void autism$recoverAfterDisconnect() {
      if (this.pendingReload != null) {
         ResourcePackDownloadScope.init_V(this.manager);
         this.onRecoveryFailure();
      }
   }

   @Inject(method = "createPackResponseSender", at = @At("RETURN"), cancellable = true)
   private static void autism$scopePackResponses(Connection connection, CallbackInfoReturnable<PackLoadFeedback> cir) {
      ResourcePackSessionTracker.Inner_BufferHelper session = ResourcePackSessionTracker.init_FileCache_BufferHe(ConnectionPrivacyRegistry.init_BufferHelper_Strea(connection));
      if (session != null) {
         cir.setReturnValue(IsolatedPackDownloader.init(session, cir.getReturnValue()));
      }
   }

   @Inject(method = "waitForPackFeedback", at = @At("HEAD"), cancellable = true)
   private void autism$scopedPackWait(final UUID packId, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
      final ResourcePackSessionTracker.Inner_BufferHelper session = ResourcePackSessionTracker.init(this);
      final long generation = ResourcePackSessionTracker.init_J(session, packId);
      ResourcePackSessionTracker.Inner_DataUtil policy = ResourcePackSessionTracker.init_FileCache_DataUtil(session, packId);
      if (policy != null && policy != ResourcePackSessionTracker.Inner_DataUtil.FULL && ResourcePackSessionTracker.init_Z(session, packId, generation)) {
         final CompletableFuture<Void> future = new CompletableFuture<>();
         @SuppressWarnings("unchecked")
         final java.util.function.BiConsumer<UUID, FinalResult>[] callbackRef = new java.util.function.BiConsumer[1];
         final Runnable restore = () -> {
            synchronized (this.autism$waitCallbacks) {
               if (callbackRef[0] != null) {
                  this.autism$waitCallbacks.computeIfPresent(packId, (key, list) -> {
                     list.remove(callbackRef[0]);
                     return list.isEmpty() ? null : list;
                  });
               }

               if (this.autism$waitCallbacks.isEmpty()
                  && this.packFeedback == this.autism$waitRouter
                  && ResourcePackSessionTracker.init_FileCache_BufferHe(session.init_BufferHelper_Strea()) == session) {
                  this.packFeedback = this.autism$waitRouterBase;
                  this.autism$waitRouterBase = null;
               }
            }
         };
         callbackRef[0] = (id, result) -> {
            ResourcePackSessionTracker.init_V(session, packId, generation);
            restore.run();
            if (result == FinalResult.APPLIED) {
               future.complete(null);
            } else {
               future.completeExceptionally(new IllegalStateException("Failed to apply pack " + id + ", reason: " + result));
            }
         };
         synchronized (this.autism$waitCallbacks) {
            if (this.autism$waitRouter == null) {
               this.autism$waitRouter = new PackLoadFeedback() {
                  @Override
                  public void reportUpdate(UUID id, Update update) {
                     PackLoadFeedback base = AutismProtectorDownloadedPackSourceMixin.this.autism$waitRouterBase;
                     if (base != null) {
                        base.reportUpdate(id, update);
                     }
                  }

                  @Override
                  public void reportFinalResult(UUID id, FinalResult result) {
                     PackLoadFeedback base = AutismProtectorDownloadedPackSourceMixin.this.autism$waitRouterBase;
                     if (base != null) {
                        base.reportFinalResult(id, result);
                     }

                     java.util.List<java.util.function.BiConsumer<UUID, FinalResult>> listeners = AutismProtectorDownloadedPackSourceMixin.this.autism$waitCallbacks.get(id);
                     if (listeners != null) {
                        for (java.util.function.BiConsumer<UUID, FinalResult> listener : listeners) {
                           listener.accept(id, result);
                        }
                     }
                  }
               };
            }

            this.autism$waitCallbacks.compute(packId, (key, list) -> {
               java.util.concurrent.CopyOnWriteArrayList<java.util.function.BiConsumer<UUID, FinalResult>> target =
                  list != null ? list : new java.util.concurrent.CopyOnWriteArrayList<>();
               target.add(callbackRef[0]);
               return target;
            });
            if (this.packFeedback != this.autism$waitRouter) {
               this.autism$waitRouterBase = this.packFeedback;
               this.packFeedback = this.autism$waitRouter;
            }
         }
         future.whenComplete((ignored, failure) -> {
            ResourcePackSessionTracker.init_V(session, packId, generation);
            restore.run();
         });
         ResourcePackSessionTracker.init(session, packId, generation, future, restore);
         cir.setReturnValue(future);
      }
   }

   @WrapOperation(
      method = "loadRequestedPacks",
      at = @At(value = "NEW", target = "(Ljava/nio/file/Path;)Lnet/minecraft/server/packs/FilePackResources$FileResourcesSupplier;")
   )
   private FileResourcesSupplier autism$wrapFilePackSupplier(Path file, Operation<FileResourcesSupplier> original, @Local IdAndPath idAndPath) {
      return IsolatedPackDownloader.init(ResourcePackSessionTracker.init(this), idAndPath.id(), file, () -> original.call(file));
   }

   @Inject(method = "loadRequestedPacks", at = @At("RETURN"))
   private void autism$recordLoadedPacks(List<IdAndPath> packsToLoad, CallbackInfoReturnable<List<Pack>> cir) {
      IsolatedPackDownloader.init(ResourcePackSessionTracker.init(this), packsToLoad, cir.getReturnValue());
   }

   @Inject(method = {"cleanupAfterDisconnect", "configureForLocalWorld", "close"}, at = @At("HEAD"))
   private void autism$cancelDownloads(CallbackInfo ci) {
      ResourcePackSessionTracker.load_V(ResourcePackSessionTracker.init(this));
      ResourcePackDownloadScope.init_V(this.manager);
      synchronized (this.autism$waitCallbacks) {
         this.autism$waitCallbacks.clear();
         if (this.packFeedback == this.autism$waitRouter && this.autism$waitRouterBase != null) {
            this.packFeedback = this.autism$waitRouterBase;
         }
         this.autism$waitRouterBase = null;
      }
      this.autism$restoreDownloader();
   }
}
