package autismclient.ai;

import autismclient.api.ai.AiOperation;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;

final class AiPreparationReceipt {
   private boolean reconciliationPending;
   private AiOperation.Result lastChildResult;

   AiOperation.Result tickChild(AiOperation child) {
      boolean before = child.requiresReconciliationAfterEmergency();
      this.lastChildResult = child.tick();
      if (!this.lastChildResult.terminal()) {
         return this.lastChildResult;
      }

      this.reconciliationPending = this.reconciliationPending
         | (child.requiresReconciliationAfterEmergency() || this.lastChildResult.status() != AiOperation.Status.SUCCEEDED && before);
      return AiGuardLayer.wrapPendingTermination(this.reconciliationPending, this.lastChildResult);
   }

   boolean requiresReconciliation(AiOperation child) {
      return this.reconciliationPending || child != null && child.requiresReconciliationAfterEmergency();
   }

   AiOperation.Result lastChildResult() {
      return this.lastChildResult;
   }

   void markReconciliationPending(boolean currentPending) {
      this.reconciliationPending |= currentPending;
   }

   AiOperation.Result finalizeResult(AiOperation.Result result, boolean currentPending) {
      this.markReconciliationPending(currentPending);
      return AiMiningPreparation.init(result, this.reconciliationPending, this.lastChildResult);
   }

   void cancelChild(AiOperation child) {
      this.reconciliationPending = this.reconciliationPending | this.requiresReconciliation(child);
      if (child != null) {
         child.cancel();
      }
   }

   static AiOperation awaitSupportedHandoff(AiOperation child, BooleanSupplier supported, BooleanSupplier valid, long deadline) {
      return awaitSupportedHandoff(child, supported, valid, deadline, System::nanoTime);
   }

   static AiOperation awaitSupportedHandoff(final AiOperation child, final BooleanSupplier supported, final BooleanSupplier valid, final long deadline, final LongSupplier now) {
      return new AiOperation() {
         final AiPreparationReceipt receipt = new AiPreparationReceipt();
         AiOperation.Result init_AiOperation_Result;
         AiOperation.Result receivedResult;
         boolean init_Z;

         @Override
         public AiOperation.Result tick() {
            if (this.receivedResult != null) {
               return this.receivedResult;
            }

            if (this.init_Z) {
               return this.confirmWith(new AiOperation.Result(AiOperation.Status.CANCELLED, "Station recovery cancelled", "{}", List.of()));
            }

            if (!valid.getAsBoolean()) {
               return this.confirmWith(AiOperation.Result.failed(AiFailure.WORLD_CHANGED, "Station recovery context changed before supported handoff", "{}"));
            }

            if (now.getAsLong() > deadline) {
               return this.confirmWith(AiOperation.Result.timedOut(AiFailure.TIMEOUT, "Station pickup did not reach a supported handoff before the deadline", "{}"));
            }

            if (this.init_AiOperation_Result == null) {
               AiOperation.Result result = this.receipt.tickChild(child);
               if (!result.terminal()) {
                  return result;
               }

               this.receipt.cancelChild(child);
               if (result.status() != AiOperation.Status.SUCCEEDED) {
                  return this.confirmWith(result);
               }

               this.init_AiOperation_Result = result;
            }

            return supported.getAsBoolean() && !this.receipt.requiresReconciliation(child)
               ? this.confirmWith(this.init_AiOperation_Result)
               : new AiOperation.Result(
                  AiOperation.Status.RUNNING,
                  "Waiting for a supported stance after the station pickup",
                  this.init_AiOperation_Result.dataJson(),
                  this.init_AiOperation_Result.evidence()
               );
         }

         AiOperation.Result confirmWith(AiOperation.Result result) {
            this.receivedResult = this.receipt.finalizeResult(result, this.receipt.requiresReconciliation(child));
            this.receipt.cancelChild(child);
            return this.receivedResult;
         }

         @Override
         public boolean canInterruptNow() {
            return !this.receipt.requiresReconciliation(child)
               && (this.receivedResult != null || (this.init_AiOperation_Result == null ? child.canInterruptNow() : supported.getAsBoolean()));
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.receipt.requiresReconciliation(child);
         }

         @Override
         public void cancel() {
            this.receipt.cancelChild(child);
            this.init_Z = true;
         }
      };
   }
}
