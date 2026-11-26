package autismclient.util;

import autismclient.AutismClientAddon;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class BackgroundTaskExecutor {
   private static final AtomicInteger init_AtomicInteger = new AtomicInteger();
   private static final ExecutorService init_ExecutorService = Executors.newCachedThreadPool(runnable -> {
      Thread thread = new Thread(runnable);
      thread.setDaemon(true);
      return thread;
   });

   private BackgroundTaskExecutor() {
   }

   public static Future<?> init_Future_Str_Runnable(String name, Runnable work) {
      String label = name != null && !name.isBlank() ? name : "Autism-BG-" + init_AtomicInteger.incrementAndGet();
      return init_ExecutorService.submit(() -> {
         Thread current = Thread.currentThread();
         String original = current.getName();
         current.setName(label);

         try {
            work.run();
         } catch (Throwable t) {
            AutismClientAddon.LOG.error("Background task '" + label + "' failed", t);
         } finally {
            current.setName(original);
         }
      });
   }

   public static <T> CompletableFuture<T> init_CompletableFuture_Str_Supplier(String name, Supplier<T> work) {
      CompletableFuture<T> future = new CompletableFuture<>();
      init_Future_Str_Runnable(name, () -> {
         try {
            future.complete(work.get());
         } catch (Throwable t) {
            future.completeExceptionally(t);
         }
      });
      return future;
   }

   public static Future<?> init(String name, BooleanSupplier done, Runnable onDone, long pollMs) {
      return init_Future_Str_Runnable(name, () -> {
         while (!done.getAsBoolean()) {
            try {
               Thread.sleep(Math.max(1L, pollMs));
            } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
               return;
            }
         }

         onDone.run();
      });
   }
}
