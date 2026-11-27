package autismclient.ai;

import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Function;

final class AiLatestTransform<I, O> implements AutoCloseable {
   private final Executor init_Executor;
   private final Executor load_Executor;
   private final Function<I, O> init_Function;
   private final BiConsumer<O, Throwable> init_BiConsumer;
   private I init_Obj;
   private long init_J;
   private boolean init_Z;
   private boolean load_Z;

   AiLatestTransform(Executor worker, Executor client, Function<I, O> compute, BiConsumer<O, Throwable> completed) {
      this.init_Executor = worker;
      this.load_Executor = client;
      this.init_Function = compute;
      this.init_BiConsumer = completed;
   }

   void init(I input) {
      if (!this.load_Z) {
         this.init_Obj = input;
         this.init_J++;
         if (!this.init_Z) {
            this.init_V();
         }
      }
   }

   boolean isUpdating() {
      return this.init_Z;
   }

   private void init_V() {
      this.init_Z = true;
      long token = this.init_J;
      I input = this.init_Obj;
      this.init_Executor.execute(() -> {
         O result = null;
         Throwable failure = null;

         try {
            result = this.init_Function.apply(input);
         } catch (Throwable error) {
            failure = error;
         }

         O value = result;
         Throwable error = failure;
         this.load_Executor.execute(() -> {
            this.init_Z = false;
            if (!this.load_Z) {
               if (token != this.init_J) {
                  this.init_V();
               } else {
                  this.init_BiConsumer.accept(value, error);
               }
            }
         });
      });
   }

   @Override
   public void close() {
      this.load_Z = true;
      this.init_J++;
      this.init_Obj = null;
   }
}
