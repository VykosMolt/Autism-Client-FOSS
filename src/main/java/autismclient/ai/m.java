package autismclient.ai;

import autismclient.api.ai.AiOperation;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

final class m implements AiOperation {
   private Supplier<AiOperation> init_Supplier;
   private AiOperation init_AiOperation;
   private boolean init_Z;

   m(Supplier<AiOperation> factory) {
      this.init_Supplier = Objects.requireNonNull(factory);
   }

   @Override
   public AiOperation.Result tick() {
      if (this.init_Z) {
         return new AiOperation.Result(AiOperation.Status.CANCELLED, "Operation cancelled before dispatch", "{}", List.of());
      }

      if (this.init_AiOperation == null) {
         Supplier<AiOperation> create = this.init_Supplier;
         this.init_Supplier = null;
         this.init_AiOperation = Objects.requireNonNull(create.get());
      }

      return this.init_AiOperation.tick();
   }

   @Override
   public boolean canInterruptNow() {
      return this.init_AiOperation == null || this.init_AiOperation.canInterruptNow();
   }

   @Override
   public boolean requiresReconciliationAfterEmergency() {
      return this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency();
   }

   @Override
   public void cancel() {
      if (!this.init_Z) {
         this.init_Z = true;
         this.init_Supplier = null;
         if (this.init_AiOperation != null) {
            this.init_AiOperation.cancel();
         }
      }
   }
}
