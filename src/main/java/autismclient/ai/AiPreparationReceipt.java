package autismclient.ai;

import autismclient.api.ai.AiOperation;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;

final class AiPreparationReceipt {
   private boolean init_Z;
   private AiOperation.Result init_AiOperation_Result;

   AiOperation.Result init_AiOperation_Result(AiOperation child) {
      boolean before = child.requiresReconciliationAfterEmergency();
      this.init_AiOperation_Result = child.tick();
      if (!this.init_AiOperation_Result.terminal()) {
         return this.init_AiOperation_Result;
      }

      this.init_Z = this.init_Z
         | (child.requiresReconciliationAfterEmergency() || this.init_AiOperation_Result.status() != AiOperation.Status.SUCCEEDED && before);
      return AiGuardLayer.init_AiOperation_Result_Z_AiOperation_Result(this.init_Z, this.init_AiOperation_Result);
   }

   boolean init_Z(AiOperation child) {
      return this.init_Z || child != null && child.requiresReconciliationAfterEmergency();
   }

   AiOperation.Result init() {
      return this.init_AiOperation_Result;
   }

   void init(boolean currentPending) {
      this.init_Z |= currentPending;
   }

   AiOperation.Result init(AiOperation.Result result, boolean currentPending) {
      this.init(currentPending);
      return AiMiningPreparation.init(result, this.init_Z, this.init_AiOperation_Result);
   }

   void init_V(AiOperation child) {
      this.init_Z = this.init_Z | this.init_Z(child);
      if (child != null) {
         child.cancel();
      }
   }

   static AiOperation init(AiOperation child, BooleanSupplier supported, BooleanSupplier valid, long deadline) {
      return init(child, supported, valid, deadline, System::nanoTime);
   }

   static AiOperation init(final AiOperation child, final BooleanSupplier supported, final BooleanSupplier valid, final long deadline, final LongSupplier now) {
      return new AiOperation() {
         final AiPreparationReceipt init_bb = new AiPreparationReceipt();
         AiOperation.Result init_AiOperation_Result;
         AiOperation.Result load;
         boolean init_Z;

         @Override
         public AiOperation.Result tick() {
            if (this.load != null) {
               return this.load;
            }

            if (this.init_Z) {
               return this.init(new AiOperation.Result(AiOperation.Status.CANCELLED, "Station recovery cancelled", "{}", List.of()));
            }

            if (!valid.getAsBoolean()) {
               return this.init(AiOperation.Result.failed(AiFailure.WORLD_CHANGED, "Station recovery context changed before supported handoff", "{}"));
            }

            if (now.getAsLong() > deadline) {
               return this.init(AiOperation.Result.timedOut(AiFailure.TIMEOUT, "Station pickup did not reach a supported handoff before the deadline", "{}"));
            }

            if (this.init_AiOperation_Result == null) {
               AiOperation.Result result = this.init_bb.init_AiOperation_Result(child);
               if (!result.terminal()) {
                  return result;
               }

               this.init_bb.init_V(child);
               if (result.status() != AiOperation.Status.SUCCEEDED) {
                  return this.init(result);
               }

               this.init_AiOperation_Result = result;
            }

            return supported.getAsBoolean() && !this.init_bb.init_Z(child)
               ? this.init(this.init_AiOperation_Result)
               : new AiOperation.Result(
                  AiOperation.Status.RUNNING,
                  "Waiting for a supported stance after the station pickup",
                  this.init_AiOperation_Result.dataJson(),
                  this.init_AiOperation_Result.evidence()
               );
         }

         AiOperation.Result init(AiOperation.Result result) {
            this.load = this.init_bb.init(result, this.init_bb.init_Z(child));
            this.init_bb.init_V(child);
            return this.load;
         }

         @Override
         public boolean canInterruptNow() {
            return !this.init_bb.init_Z(child)
               && (this.load != null || (this.init_AiOperation_Result == null ? child.canInterruptNow() : supported.getAsBoolean()));
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.init_bb.init_Z(child);
         }

         @Override
         public void cancel() {
            this.init_bb.init_V(child);
            this.init_Z = true;
         }
      };
   }
}
