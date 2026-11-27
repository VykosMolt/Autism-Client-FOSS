package autismclient.mixin.security;

import autismclient.protector.ResourcePackDownloadScope;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.server.packs.DownloadQueue;
import net.minecraft.server.packs.DownloadQueue.BatchConfig;
import net.minecraft.util.HttpUtil.DownloadProgressListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DownloadQueue.class)
public class AutismProtectorDownloadQueueMixin {
   @WrapOperation(
      method = "downloadBatch",
      at = @At(
         value = "INVOKE",
         target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
      )
   )
   private CompletableFuture autism$scopedBatch(
      Supplier supplier, Executor executor, Operation<CompletableFuture> original, @Local(argsOnly = true) BatchConfig config
   ) throws IOException {
      ResourcePackDownloadScope.Inner_BufferHelper snapshot = ResourcePackDownloadScope.current();
      if (snapshot == null) {
         return original.call(supplier, executor);
      }

      snapshot.ensureCurrent();
      ResourcePackDownloadScope.Inner_DataUtil registration = ResourcePackDownloadScope.init(config, snapshot);

      CompletableFuture future;
      try {
         Supplier scopedSupplier = () -> {
            try (ResourcePackDownloadScope.Inner_CachePool ignored = ResourcePackDownloadScope.load(snapshot)) {
               return supplier.get();
            } catch (IOException e) {
               throw new CompletionException(e);
            }
         };
         future = original.call(scopedSupplier, executor);
      } catch (Throwable error) {
         registration.close();
         throw error;
      }

      future.whenComplete((result, errorx) -> registration.close());
      return future;
   }

   @WrapOperation(method = "runDownload", at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"))
   private void autism$workerScope(Map requests, BiConsumer action, Operation<Void> original, @Local(argsOnly = true) BatchConfig config) throws IOException {
      ResourcePackDownloadScope.Inner_BufferHelper registered = ResourcePackDownloadScope.init(config);
      ResourcePackDownloadScope.Inner_BufferHelper worker = ResourcePackDownloadScope.load();
      if (registered == null && worker == null) {
         original.call(requests, action);
      } else {
         if (registered == null) {
            throw new IOException("Controlled resource-pack worker has no registered batch");
         }

         if (worker != null && worker != registered) {
            throw new IOException("Resource-pack worker and batch scopes do not match");
         }

         registered.ensureCurrent();

         try (ResourcePackDownloadScope.Inner_CachePool ignored = ResourcePackDownloadScope.load(registered)) {
            original.call(requests, action);
         }
      }
   }

   @WrapOperation(
      method = {"lambda$runDownload$0", "method_55485"},
      at = @At(value = "INVOKE", target = "Ljava/nio/file/Path;resolve(Ljava/lang/String;)Ljava/nio/file/Path;"),
      require = 0
   )
   private Path autism$scopedCacheDirectory(
      Path cacheDir, String name, Operation<Path> original, @Local(argsOnly = true) UUID packId, @Local BatchConfig config
   ) throws IOException {
      ResourcePackDownloadScope.Inner_BufferHelper snapshot = ResourcePackDownloadScope.load();
      if (snapshot == null) {
         return original.call(cacheDir, name);
      } else {
         snapshot.ensureCurrent();
         if (config != null && packId != null && packId.toString().equals(name)) {
            return ResourcePackDownloadScope.init(cacheDir, snapshot, packId, config.hashFunction());
         } else {
            throw new IOException("Ambiguous resource-pack cache identity");
         }
      }
   }

   @WrapOperation(
      method = {"lambda$runDownload$0", "method_55485"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/util/HttpUtil;downloadFile(Ljava/nio/file/Path;Ljava/net/URL;Ljava/util/Map;Lcom/google/common/hash/HashFunction;Lcom/google/common/hash/HashCode;ILjava/net/Proxy;Lnet/minecraft/util/HttpUtil$DownloadProgressListener;)Ljava/nio/file/Path;"
      ),
      require = 0
   )
   private Path autism$lockedValidatedDownload(
      Path targetDir,
      URL url,
      Map headers,
      HashFunction hashFunction,
      HashCode requestedHash,
      int maxSize,
      Proxy proxy,
      DownloadProgressListener listener,
      Operation<Path> original
   ) throws InterruptedException, IOException {
      ResourcePackDownloadScope.Inner_BufferHelper snapshot = ResourcePackDownloadScope.load();
      if (snapshot == null) {
         return original.call(targetDir, url, headers, hashFunction, requestedHash, maxSize, proxy, listener);
      }

      ReentrantLock lock = ResourcePackDownloadScope.init(targetDir);
      lock.lockInterruptibly();

      try {
         snapshot.ensureCurrent();
         Path result = original.call(targetDir, url, headers, hashFunction, requestedHash, maxSize, proxy, autism$guardedProgress(snapshot, listener));
         snapshot.ensureCurrent();
         return ResourcePackDownloadScope.init(targetDir.getParent(), result);
      } finally {
         lock.unlock();
      }
   }

   private static DownloadProgressListener autism$guardedProgress(final ResourcePackDownloadScope.Inner_BufferHelper snapshot, final DownloadProgressListener listener) {
      return new DownloadProgressListener() {
         @Override
         public void requestStart() {
            if (snapshot.isCurrent()) {
               listener.requestStart();
            }
         }

         @Override
         public void downloadStart(OptionalLong sizeBytes) {
            if (snapshot.isCurrent()) {
               listener.downloadStart(sizeBytes);
            }
         }

         @Override
         public void downloadedBytes(long bytesSoFar) {
            if (snapshot.isCurrent()) {
               listener.downloadedBytes(bytesSoFar);
            }
         }

         @Override
         public void requestFinished(boolean success) {
            if (snapshot.isCurrent()) {
               listener.requestFinished(success);
            }
         }
      };
   }
}
