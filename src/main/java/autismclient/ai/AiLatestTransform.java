package autismclient.ai;

import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Function;

final class AiLatestTransform<I, O> implements AutoCloseable {
   private final Executor worker;
   private final Executor client;
   private final Function<I, O> compute;
   private final BiConsumer<O, Throwable> completed;
   private I pendingInput;
   private long generation;
   private boolean running;
   private boolean closed;

   AiLatestTransform(Executor worker, Executor client, Function<I, O> compute, BiConsumer<O, Throwable> completed) {
      this.worker = worker;
      this.client = client;
      this.compute = compute;
      this.completed = completed;
   }

   void submit(I input) {
      if (!this.closed) {
         this.pendingInput = input;
         this.generation++;
         if (!this.running) {
            this.startCompute();
         }
      }
   }

   boolean isUpdating() {
      return this.running;
   }

   private void startCompute() {
      this.running = true;
      long token = this.generation;
      I input = this.pendingInput;
      this.worker.execute(() -> {
         O result = null;
         Throwable failure = null;

         try {
            result = this.compute.apply(input);
         } catch (Throwable error) {
            failure = error;
         }

         O value = result;
         Throwable error = failure;
         this.client.execute(() -> {
            this.running = false;
            if (!this.closed) {
               if (token != this.generation) {
                  this.startCompute();
               } else {
                  this.completed.accept(value, error);
               }
            }
         });
      });
   }

   @Override
   public void close() {
      this.closed = true;
      this.generation++;
      this.pendingInput = null;
   }
}
