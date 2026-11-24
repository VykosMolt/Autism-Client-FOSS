package autismclient.ai;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class cw {
   private final ConcurrentLinkedQueue<Runnable> init_ConcurrentLinkedQu = new ConcurrentLinkedQueue<>();
   private volatile Thread init_Thread;

   boolean init() {
      return this.init_Thread == Thread.currentThread();
   }

   void init_V_Runnable(Runnable action) {
      if (this.init()) {
         action.run();
      } else {
         this.init_ConcurrentLinkedQu.add(action);
      }
   }

   <T> CompletableFuture<T> init_CompletableFuture_Supplier(Supplier<T> action) {
      CompletableFuture<T> result = new CompletableFuture<>();
      this.init_V_Runnable(() -> {
         if (!result.isDone()) {
            try {
               result.complete(action.get());
            } catch (Throwable error) {
               result.completeExceptionally(error);
            }
         }
      });
      return result;
   }

   void init(Runnable tick, Consumer<Throwable> failure) {
      if (this.init_Thread != null) {
         throw new IllegalStateException("AI tick phase is already running");
      }

      this.init_Thread = Thread.currentThread();

      try {
         int remaining = this.init_ConcurrentLinkedQu.size();

         while (remaining-- > 0) {
            Runnable action = this.init_ConcurrentLinkedQu.poll();
            if (action == null) {
               break;
            }

            try {
               action.run();
            } catch (Throwable error) {
               failure.accept(error);
            }
         }

         try {
            tick.run();
         } catch (Throwable error) {
            failure.accept(error);
         }
      } finally {
         this.init_Thread = null;
      }
   }
}
