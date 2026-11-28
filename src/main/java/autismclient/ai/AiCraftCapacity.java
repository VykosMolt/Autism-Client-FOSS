package autismclient.ai;

import autismclient.api.ai.AiOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

final class AiCraftCapacity {
   static final int init = 2;
   static final int load = 2;

   private AiCraftCapacity() {
   }

   static boolean isCapacityFailure(autismclient.util.AutoCraftingEngine.CraftResult result, boolean allowed) {
      return allowed
         && !result.success
         && !result.uncertain
         && result.craftedAmount == 0
         && result.failureReason == autismclient.util.AutoCraftingEngine.CraftResult.FailureReason.INVENTORY_FULL;
   }

   static JsonObject tidyPlan(AiCraftPlanner.CraftPlan plan, int firstStep, String target, int seconds, AiCraftPlanner.CraftPlan... obligations) {
      Set<String> keep = new LinkedHashSet<>();
      keep.add(target);
      keep.add("minecraft:crafting_table");
      keep.add("minecraft:furnace");
      keep.add("minecraft:stonecutter");

      for (int i = Math.max(0, firstStep); i < plan.steps().size(); i++) {
         AiCraftPlanner.RecipeSpec recipe = plan.steps().get(i).recipe();
         keep.add(recipe.output());
         recipe.inputs().forEach(keep::addAll);
         if (!recipe.station().isEmpty()) {
            keep.add(recipe.station());
         }
      }

      for (AiCraftPlanner.CraftPlan obligation : obligations) {
         for (AiCraftPlanner.CraftStep step : obligation.steps()) {
            keep.add(step.recipe().output());
            step.recipe().inputs().forEach(keep::addAll);
            if (!step.recipe().station().isEmpty()) {
               keep.add(step.recipe().station());
            }
         }
      }

      JsonArray ids = new JsonArray();
      keep.forEach(ids::add);
      return AiJson.objectOf("action", "drop", "freeSlots", 2, "maxDrops", 2, "timeoutSeconds", Math.max(5, Math.min(15, seconds)), "keep", ids);
   }

   static AiOperation tidyThenRetry(
      final AiOperation.Result rejection, final AiOperation tidy, final Supplier<AiOperation> retry, final IntSupplier freeSlots
   ) {
      return new AiOperation() {
         AiOperation activeOp = tidy;
         AiOperation.Result lastResult;
         AiOperation.Result cleanupResult;
         AiOperation.Result finalResult;
         boolean retried;
         boolean cancelled;
         boolean reconcileAfterEmergency;

         @Override
         public AiOperation.Result tick() {
            if (this.finalResult != null) {
               return this.finalResult;
            }

            if (this.cancelled) {
               return this.finishWithReceipt(new AiOperation.Result(AiOperation.Status.CANCELLED, "Craft capacity recovery cancelled", "{}", List.of()));
            }

            boolean before = this.activeOp.requiresReconciliationAfterEmergency();
            this.lastResult = this.activeOp.tick();
            if (!this.lastResult.terminal()) {
               return this.lastResult;
            }

            JsonObject evidence = AiRunResources.parseEvidenceJson(this.lastResult.dataJson());
            this.reconcileAfterEmergency = this.reconcileAfterEmergency
               | (
                  this.activeOp.requiresReconciliationAfterEmergency()
                     || this.lastResult.status() != AiOperation.Status.SUCCEEDED && before
                     || AiJson.getBoolean(evidence, "reobserveBeforeResuming", false)
                     || AiJson.getBoolean(evidence, "interruptedActionNeedsReconciliation", false)
               );
            this.lastResult = AiGuardLayer.wrapPendingTermination(this.reconcileAfterEmergency, this.lastResult);
            this.activeOp.cancel();
            if (this.retried) {
               return this.finishWithReceipt(this.lastResult);
            }

            this.cleanupResult = this.lastResult;
            if (this.lastResult.status() != AiOperation.Status.SUCCEEDED) {
               return this.finishWithReceipt(this.lastResult);
            }

            if (freeSlots.getAsInt() < 2) {
               return this.finishWithReceipt(AiOperation.Result.failed(AiFailure.INVENTORY_FULL, "Acknowledged cleanup left insufficient room for the craft", "{}"));
            }

            this.retried = true;
            this.activeOp = retry.get();
            this.lastResult = null;
            return new AiOperation.Result(AiOperation.Status.RUNNING, "Inventory room confirmed; retrying the craft once", "{}", List.of());
         }

         AiOperation.Result finishWithReceipt(AiOperation.Result actual) {
            AiOperation.Result receipt = this.cancelled && this.lastResult != null ? this.lastResult : actual;
            JsonObject data = AiJson.objectOf(
               "capacityFailure",
               AiRunResources.parseEvidenceJson(rejection.dataJson()),
               "capacityFailureMessage",
               rejection.message(),
               "capacityFailureCode",
               rejection.failure().name(),
               "capacityRetryStarted",
               this.retried
            );
            if (this.cleanupResult != null) {
               data.add("capacityCleanup", AiRunResources.parseEvidenceJson(this.cleanupResult.dataJson()));
            }

            if (this.reconcileAfterEmergency && this.lastResult != null) {
               data.add("pendingCapacityChild", AiRunResources.parseEvidenceJson(this.lastResult.dataJson()));
            }

            AiOperation.Result result = AiNetherRun.init_AiOperation_Result_AiOperation_Status(
               actual.status(), actual.message(), data, this.requiresReconciliationAfterEmergency(), receipt
            );
            this.finalResult = new AiOperation.Result(
               result.status(),
               result.message(),
               result.dataJson(),
               receipt.evidence(),
               this.cancelled && this.lastResult != null ? receipt.failure() : actual.failure()
            );
            this.cancel();
            return this.finalResult;
         }

         @Override
         public boolean canInterruptNow() {
            return !this.reconcileAfterEmergency && this.activeOp.canInterruptNow();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.reconcileAfterEmergency || this.activeOp.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            if (!this.cancelled) {
               this.reconcileAfterEmergency = this.reconcileAfterEmergency | this.activeOp.requiresReconciliationAfterEmergency();
               this.cancelled = true;
               this.activeOp.cancel();
            }
         }
      };
   }
}
