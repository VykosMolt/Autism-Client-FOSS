package autismclient.ai;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class AiTickQueue {
   private final ConcurrentLinkedQueue<Runnable> pending = new ConcurrentLinkedQueue<>();
   private volatile Thread drainThread;

   boolean init() {
      return this.drainThread == Thread.currentThread();
   }

   void execute(Runnable action) {
      if (this.init()) {
         action.run();
      } else {
         this.pending.add(action);
      }
   }

   <T> CompletableFuture<T> supply(Supplier<T> action) {
      CompletableFuture<T> result = new CompletableFuture<>();
      this.execute(() -> {
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
      if (this.drainThread != null) {
         throw new IllegalStateException("AI tick phase is already running");
      }

      this.drainThread = Thread.currentThread();

      try {
         int remaining = this.pending.size();

         while (remaining-- > 0) {
            Runnable action = this.pending.poll();
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
         this.drainThread = null;
      }
   }
}
