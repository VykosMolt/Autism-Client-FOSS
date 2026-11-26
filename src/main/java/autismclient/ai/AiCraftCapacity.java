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

   static boolean init_Z_J_CachePool_Z(autismclient.util.AutoCraftingEngine.Inner_CachePool result, boolean allowed) {
      return allowed
         && !result.init_Z
         && !result.load
         && result.init_I == 0
         && result.init_J_CachePool_DataUt == autismclient.util.AutoCraftingEngine.Inner_CachePool.Inner_DataUtil.INVENTORY_FULL;
   }

   static JsonObject init_JsonObject_g_CachePool(AiCraftPlanner.Inner_CachePool plan, int firstStep, String target, int seconds, AiCraftPlanner.Inner_CachePool... obligations) {
      Set<String> keep = new LinkedHashSet<>();
      keep.add(target);
      keep.add("minecraft:crafting_table");
      keep.add("minecraft:furnace");
      keep.add("minecraft:stonecutter");

      for (int i = Math.max(0, firstStep); i < plan.steps().size(); i++) {
         AiCraftPlanner.Inner_BufferHelper recipe = plan.steps().get(i).recipe();
         keep.add(recipe.output());
         recipe.inputs().forEach(keep::addAll);
         if (!recipe.station().isEmpty()) {
            keep.add(recipe.station());
         }
      }

      for (AiCraftPlanner.Inner_CachePool obligation : obligations) {
         for (AiCraftPlanner.Inner_TaskQueue step : obligation.steps()) {
            keep.add(step.recipe().output());
            step.recipe().inputs().forEach(keep::addAll);
            if (!step.recipe().station().isEmpty()) {
               keep.add(step.recipe().station());
            }
         }
      }

      JsonArray ids = new JsonArray();
      keep.forEach(ids::add);
      return AiJson.init_JsonObject_ObjArr("action", "drop", "freeSlots", 2, "maxDrops", 2, "timeoutSeconds", Math.max(5, Math.min(15, seconds)), "keep", ids);
   }

   static AiOperation init_AiOperation_AiOperation_Result(
      final AiOperation.Result rejection, final AiOperation tidy, final Supplier<AiOperation> retry, final IntSupplier freeSlots
   ) {
      return new AiOperation() {
         AiOperation init_AiOperation = tidy;
         AiOperation.Result init_AiOperation_Result;
         AiOperation.Result load_AiOperation_Result;
         AiOperation.Result read_AiOperation_Result;
         boolean init_Z;
         boolean load_Z;
         boolean read_Z;

         @Override
         public AiOperation.Result tick() {
            if (this.read_AiOperation_Result != null) {
               return this.read_AiOperation_Result;
            }

            if (this.load_Z) {
               return this.init(new AiOperation.Result(AiOperation.Status.CANCELLED, "Craft capacity recovery cancelled", "{}", List.of()));
            }

            boolean before = this.init_AiOperation.requiresReconciliationAfterEmergency();
            this.init_AiOperation_Result = this.init_AiOperation.tick();
            if (!this.init_AiOperation_Result.terminal()) {
               return this.init_AiOperation_Result;
            }

            JsonObject evidence = AiRunResources.init_JsonObject(this.init_AiOperation_Result.dataJson());
            this.read_Z = this.read_Z
               | (
                  this.init_AiOperation.requiresReconciliationAfterEmergency()
                     || this.init_AiOperation_Result.status() != AiOperation.Status.SUCCEEDED && before
                     || AiJson.init_Z_JsonObject(evidence, "reobserveBeforeResuming", false)
                     || AiJson.init_Z_JsonObject(evidence, "interruptedActionNeedsReconciliation", false)
               );
            this.init_AiOperation_Result = AiGuardLayer.init_AiOperation_Result_Z_AiOperation_Result(this.read_Z, this.init_AiOperation_Result);
            this.init_AiOperation.cancel();
            if (this.init_Z) {
               return this.init(this.init_AiOperation_Result);
            }

            this.load_AiOperation_Result = this.init_AiOperation_Result;
            if (this.init_AiOperation_Result.status() != AiOperation.Status.SUCCEEDED) {
               return this.init(this.init_AiOperation_Result);
            }

            if (freeSlots.getAsInt() < 2) {
               return this.init(AiOperation.Result.failed(AiFailure.put, "Acknowledged cleanup left insufficient room for the craft", "{}"));
            }

            this.init_Z = true;
            this.init_AiOperation = retry.get();
            this.init_AiOperation_Result = null;
            return new AiOperation.Result(AiOperation.Status.RUNNING, "Inventory room confirmed; retrying the craft once", "{}", List.of());
         }

         AiOperation.Result init(AiOperation.Result actual) {
            AiOperation.Result receipt = this.load_Z && this.init_AiOperation_Result != null ? this.init_AiOperation_Result : actual;
            JsonObject data = AiJson.init_JsonObject_ObjArr(
               "capacityFailure",
               AiRunResources.init_JsonObject(rejection.dataJson()),
               "capacityFailureMessage",
               rejection.message(),
               "capacityFailureCode",
               rejection.failure().name(),
               "capacityRetryStarted",
               this.init_Z
            );
            if (this.load_AiOperation_Result != null) {
               data.add("capacityCleanup", AiRunResources.init_JsonObject(this.load_AiOperation_Result.dataJson()));
            }

            if (this.read_Z && this.init_AiOperation_Result != null) {
               data.add("pendingCapacityChild", AiRunResources.init_JsonObject(this.init_AiOperation_Result.dataJson()));
            }

            AiOperation.Result result = AiNetherRun.init_AiOperation_Result_AiOperation_Status(
               actual.status(), actual.message(), data, this.requiresReconciliationAfterEmergency(), receipt
            );
            this.read_AiOperation_Result = new AiOperation.Result(
               result.status(),
               result.message(),
               result.dataJson(),
               receipt.evidence(),
               this.load_Z && this.init_AiOperation_Result != null ? receipt.failure() : actual.failure()
            );
            this.cancel();
            return this.read_AiOperation_Result;
         }

         @Override
         public boolean canInterruptNow() {
            return !this.read_Z && this.init_AiOperation.canInterruptNow();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.read_Z || this.init_AiOperation.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            if (!this.load_Z) {
               this.read_Z = this.read_Z | this.init_AiOperation.requiresReconciliationAfterEmergency();
               this.load_Z = true;
               this.init_AiOperation.cancel();
            }
         }
      };
   }
}
