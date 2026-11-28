package autismclient.ai;

import autismclient.api.ai.AiOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.concurrent.Future;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.world.level.GameType;

final class AiPrintRepeat implements AiOperation {
   private static final int MAX_REPORTED = 32;
   final Minecraft mc = Minecraft.getInstance();
   final AiBuilder.Blueprint base;
   final AiBuilder.BuildOptions options;
   final boolean verify;
   final long deadline;
   int copiesBuilt;
   int placedTotal;
   int skippedTotal;
   int failedTotal;
   final JsonArray failures = new JsonArray();
   AiOperation copyOp;
   boolean copyTicked;
   final AiPreparationReceipt receipt = new AiPreparationReceipt();
   AiOperation.Result finalResult;
   boolean cancelled;
   final Object level;
   final Object player;
   List<autismclient.ai.print.PrintTarget> firstTargets;
   AiSchematicMachinePlan.MachineSpec firstActions;
   autismclient.ai.print.PrintCore.ao firstGeometry;
   final Provider registries;
   BlockPos knownStock;
   AiPrintPreparation.Inner_SyncManager prepareWorker;
   Future<AiPrintRepeat.Inner_DataUtil> pendingCopy;
   AiPrintRepeat.Inner_DataUtil preparedCopy;
   AiPrintPreparation.Inner_CachePool materialCheck;
   AiPrintPreparation.Inner_NetHelper stockSearch;
   int containersFilled;
   int containersSkipped;
   int savedSlotsVerified;
   int savedSlotsSkipped;
   int clocksVerified;
   int clockItemsVerified;
   final JsonArray skippedContents = new JsonArray();

   AiPrintRepeat(
      AiBuilder.Blueprint base,
      AiBuilder.BuildOptions power,
      boolean verify,
      int timeoutSeconds,
      List<autismclient.ai.print.PrintTarget> firstTargets,
      BlockPos knownStock,
      AiSchematicMachinePlan.MachineSpec firstActions,
      autismclient.ai.print.PrintCore.ao firstGeometry
   ) {
      this.base = base;
      this.options = power;
      this.verify = verify;
      this.deadline = timeoutSeconds > 0 ? System.nanoTime() + timeoutSeconds * 1000000000L : 9223372036854775807L;
      this.firstTargets = firstTargets;
      this.firstActions = firstActions;
      this.firstGeometry = firstGeometry;
      this.registries = this.mc.level.registryAccess();
      this.knownStock = knownStock;
      this.level = this.mc.level;
      this.player = this.mc.player;
   }

   @Override
   public AiOperation.Result tick() {
      if (this.finalResult != null) {
         return this.finalResult;
      }

      if (!this.cancelled
         && this.mc.player != null
         && this.mc.level != null
         && this.mc.level == this.level
         && this.mc.player == this.player) {
         if (this.mc.player.getHealth() <= 4.0F && !AiGuard.isLowHealthOverrideActive()) {
            return this.finish(
               AiOperation.Status.FAILED, AiGuard.formatLowHealthAbort("building", this.mc.player.getHealth(), this.mc.player.getMaxHealth())
            );
         }

         if (System.nanoTime() > this.deadline) {
            return this.finish(AiOperation.Status.TIMED_OUT, "Build deadline reached");
         }

         if (this.copyOp != null) {
            AiOperation.Result r = this.receipt.tickChild(this.copyOp);
            this.copyTicked = true;
            if (!r.terminal()) {
               return this.running("Building copy " + (this.copiesBuilt + 1) + " of " + this.copyCountLabel());
            }

            this.collectCopyResult();
            if (r.status() != AiOperation.Status.SUCCEEDED) {
               return this.finish(r.status(), r.message());
            }

            this.copiesBuilt++;
         }

         AiBuilder.RepeatSpec repeat = this.options.repeat();
         return repeat.count() >= 0 && this.copiesBuilt > repeat.count()
            ? this.finish(AiOperation.Status.SUCCEEDED, "Built " + this.copiesBuilt + " copies")
            : this.startNextCopy();
      } else {
         return this.finish(AiOperation.Status.CANCELLED, "Build cancelled or world changed");
      }
   }

   private AiOperation.Result startNextCopy() {
      AiBuilder.Blueprint shifted = new AiBuilder.Blueprint(
         this.base
            .origin()
            .offset(
               this.options.repeat().dx() * this.copiesBuilt,
               this.options.repeat().dy() * this.copiesBuilt,
               this.options.repeat().dz() * this.copiesBuilt
            ),
         this.base.cells()
      );
      int remaining = this.deadline == 9223372036854775807L ? 0 : (int)Math.max(1L, Math.min(2147483647L, (this.deadline - System.nanoTime()) / 1000000000L));
      if (this.copiesBuilt == 0 && this.firstTargets != null) {
         AiPrintOp operation = new AiPrintOp(shifted, remaining, this.options, this.firstTargets, this.firstActions, this.firstGeometry);
         if (this.knownStock != null) {
            operation.notifyBlockChanged(this.knownStock);
         }

         this.firstTargets = null;
         this.firstActions = null;
         this.firstGeometry = null;
         this.copyOp = operation;
         return this.running("Building copy 1 of " + this.copyCountLabel());
      } else {
         if (this.preparedCopy == null) {
            if (this.pendingCopy == null) {
               this.prepareWorker = new AiPrintPreparation.Inner_SyncManager();
               this.pendingCopy = this.prepareWorker
                  .init(
                     () -> {
                        List<autismclient.ai.print.PrintTarget> targets = List.copyOf(AiPrintRules.init(shifted));
                        return new AiPrintRepeat.Inner_DataUtil(
                           shifted,
                           AiBuilder.stripPoweredLevers(shifted),
                           targets,
                           AiPrintPreparation.Inner_JsonUtil.of(shifted),
                           this.options
                              .machine()
                              .machineSpec(shifted.origin(), this.registries),
                           autismclient.ai.print.PrintCore.prepareGeometry(targets, AiBuilder::checkNotCancelled)
                        );
                     }
                  );
               return this.running("Preparing copy " + (this.copiesBuilt + 1));
            }

            if (!this.pendingCopy.isDone()) {
               return this.running("Preparing copy " + (this.copiesBuilt + 1));
            }

            try {
               this.preparedCopy = this.pendingCopy.get();
            } catch (Exception failure) {
               return this.finish(
                  AiOperation.Status.FAILED, "Could not prepare schematic copy: " + failure.getMessage()
               );
            } finally {
               if (this.prepareWorker != null) {
                  this.prepareWorker.close();
               }

               this.prepareWorker = null;
               this.pendingCopy = null;
            }

            this.materialCheck = new AiPrintPreparation.Inner_CachePool(this.preparedCopy.stockPlan(), this.preparedCopy.source().keys());
         }

         boolean creative = this.mc.gameMode != null
            && this.mc.gameMode.getPlayerMode() == GameType.CREATIVE
            && this.mc.player.getAbilities().instabuild;
         if (!creative) {
            if (!this.materialCheck.isDone()) {
               this.materialCheck.init(4096, System.nanoTime() + 2000000L, this.mc.level);
               return this.running("Checking materials for copy " + (this.copiesBuilt + 1));
            }

            JsonObject missing = AiBuilder.missingMaterialsForContainer(
               this.materialCheck.requirements(), this.mc.player.getInventory(), this.options.shulkerStock()
            );
            if (!missing.isEmpty()) {
               if (this.options.shulkerStock()) {
                  if (this.stockSearch == null) {
                     this.stockSearch = new AiPrintPreparation.Inner_NetHelper(
                        this.mc.player.blockPosition(), shifted.origin(), this.preparedCopy.source()
                     );
                  }

                  this.stockSearch
                     .init(4096, System.nanoTime() + 2000000L, pos -> AiTerrain.isLoaded(this.mc.level, pos), this.mc.level::getBlockState);
                  if (!this.stockSearch.done && !this.stockSearch.isDone()) {
                     return this.running("Checking stock for copy " + (this.copiesBuilt + 1));
                  }

                  if (this.stockSearch.done) {
                     this.knownStock = this.stockSearch.stockPos;
                  }
               }

               if (this.stockSearch == null || !this.stockSearch.done) {
                  JsonObject data = this.progressData();
                  data.add("missing", missing);
                  return this.copiesBuilt > 0 && this.options.repeat().count() < 0
                     ? this.finish(AiOperation.Status.SUCCEEDED, "Materials exhausted after " + this.copiesBuilt + " copies", data)
                     : this.finish(AiOperation.Status.FAILED, "Missing placement materials for copy " + (this.copiesBuilt + 1), data);
               }
            }
         }

         AiPrintOp operation = new AiPrintOp(
            this.preparedCopy.blueprint(),
            remaining,
            this.options,
            this.preparedCopy.targets(),
            this.preparedCopy.actions(),
            this.preparedCopy.geometry()
         );
         if (this.knownStock != null) {
            operation.notifyBlockChanged(this.knownStock);
         }

         this.copyOp = operation;
         this.preparedCopy = null;
         this.materialCheck = null;
         this.stockSearch = null;
         return this.running("Building copy " + (this.copiesBuilt + 1) + " of " + this.copyCountLabel());
      }
   }

   String copyCountLabel() {
      return this.options.repeat().count() < 0 ? "many" : String.valueOf(this.options.repeat().count() + 1);
   }

   JsonObject progressData() {
      AiBuilder.RepeatSpec repeat = this.options.repeat();
      return AiJson.objectOf(
         "copies",
         this.copiesBuilt,
         "placed",
         this.placedTotal,
         "skipped",
         this.skippedTotal,
         "failed",
         this.failures,
         "failedTotal",
         this.failedTotal,
         "cells",
         this.base.cells().size(),
         "containersFilled",
         this.containersFilled,
         "containersSkipped",
         this.containersSkipped,
         "savedSlotsVerified",
         this.savedSlotsVerified,
         "savedSlotsSkipped",
         this.savedSlotsSkipped,
         "machineComplete",
         false,
         "skippedContents",
         this.skippedContents,
         "repeat",
         AiJson.objectOf("dx", repeat.dx(), "dy", repeat.dy(), "dz", repeat.dz(), "count", repeat.count())
      );
   }

   AiOperation.Result running(String message) {
      return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.progressData());
   }

   AiOperation.Result finish(AiOperation.Status status, String message) {
      if (this.finalResult != null) {
         return this.finalResult;
      }

      this.cancel();
      return this.finish(status, message, this.progressData());
   }

   AiOperation.Result finish(AiOperation.Status status, String message, JsonObject data) {
      if (this.finalResult != null) {
         return this.finalResult;
      }

      this.cancel();
      boolean pending = this.receipt.requiresReconciliation(null);
      data.addProperty("clocksVerified", this.clocksVerified);
      data.addProperty("clockItemsVerified", this.clockItemsVerified);
      data.addProperty("machineComplete", status == AiOperation.Status.SUCCEEDED && this.containersSkipped == 0 && !pending);
      this.finalResult = withReceiptEvidence(AiGameOperations.result(status, message, data), this.receipt);
      return this.finalResult;
   }

   static AiOperation.Result withReceiptEvidence(AiOperation.Result result, AiPreparationReceipt receipt) {
      boolean pending = receipt.requiresReconciliation(null);
      JsonObject data = AiRunResources.parseEvidenceJson(result.dataJson());
      if (receipt.lastChildResult() != null) {
         JsonObject childData = AiRunResources.parseEvidenceJson(receipt.lastChildResult().dataJson());
         if (childData.has("machineRecovery")) {
            data.add("machineRecovery", childData.get("machineRecovery").deepCopy());
         }

         if (childData.has("externalWork")) {
            JsonObject external = childData.getAsJsonObject("externalWork").deepCopy();
            if (pending) {
               external.addProperty("recoveryRequired", true);
            }

            data.add("externalWork", external);
         }
      }

      return receipt.finalizeResult(new AiOperation.Result(result.status(), result.message(), data.toString(), result.evidence(), result.failure()), pending);
   }

   static void releaseChild(AiPreparationReceipt receipt, AiOperation child) {
      receipt.cancelChild(child);
      if (child != null) {
         receipt.markReconciliationPending(child.requiresReconciliationAfterEmergency());
      }
   }

   private void collectCopyResult() {
      if (this.copyOp != null) {
         if (this.copyTicked && this.receipt.lastChildResult() != null) {
            JsonObject legData = AiRunResources.parseEvidenceJson(this.receipt.lastChildResult().dataJson());
            this.placedTotal = this.placedTotal + AiJson.getInt(legData, "placed", 0);
            this.skippedTotal = this.skippedTotal + AiJson.getInt(legData, "skipped", 0);
            this.failedTotal = this.failedTotal + AiJson.getInt(legData, "failedTotal", 0);
            this.containersFilled = this.containersFilled + AiJson.getInt(legData, "containersFilled", 0);
            this.containersSkipped = this.containersSkipped + AiJson.getInt(legData, "containersSkipped", 0);
            this.savedSlotsVerified = this.savedSlotsVerified + AiJson.getInt(legData, "savedSlotsVerified", 0);
            this.savedSlotsSkipped = this.savedSlotsSkipped + AiJson.getInt(legData, "savedSlotsSkipped", 0);
            this.clocksVerified = this.clocksVerified + AiJson.getInt(legData, "clocksVerified", 0);
            this.clockItemsVerified = this.clockItemsVerified + AiJson.getInt(legData, "clockItemsVerified", 0);
            if (legData.has("skippedContents")) {
               for (JsonElement omission : legData.getAsJsonArray("skippedContents")) {
                  if (this.skippedContents.size() >= 32) {
                     break;
                  }

                  this.skippedContents.add(omission);
               }
            }

            if (legData.has("failed")) {
               for (JsonElement failure : legData.getAsJsonArray("failed")) {
                  if (this.failures.size() >= 32) {
                     break;
                  }

                  this.failures.add(failure);
               }
            }
         }

         this.copyTicked = false;
         releaseChild(this.receipt, this.copyOp);
         this.copyOp = null;
      }
   }

   @Override
   public boolean canInterruptNow() {
      return !this.receipt.requiresReconciliation(this.copyOp) && (this.copyOp == null || this.copyOp.canInterruptNow());
   }

   @Override
   public boolean requiresReconciliationAfterEmergency() {
      return this.receipt.requiresReconciliation(this.copyOp);
   }

   @Override
   public void cancel() {
      if (!this.cancelled) {
         this.cancelled = true;
         if (this.prepareWorker != null) {
            this.prepareWorker.close();
            this.prepareWorker = null;
         }

         this.pendingCopy = null;
         this.preparedCopy = null;
         this.materialCheck = null;
         this.stockSearch = null;
         this.firstTargets = null;
         this.firstActions = null;
         this.firstGeometry = null;
         this.collectCopyResult();
      }
   }

   private record Inner_DataUtil(
      AiBuilder.Blueprint blueprint,
      AiBuilder.Blueprint stockPlan,
      List<autismclient.ai.print.PrintTarget> targets,
      AiPrintPreparation.Inner_JsonUtil source,
      AiSchematicMachinePlan.MachineSpec actions,
      autismclient.ai.print.PrintCore.ao geometry
   ) {
   }
}
