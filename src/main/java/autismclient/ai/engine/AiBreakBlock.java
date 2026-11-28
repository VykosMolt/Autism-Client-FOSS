package autismclient.ai.engine;

import autismclient.ai.AiFailure;
import autismclient.api.ai.AiOperation;
import autismclient.util.KeyMappingControl;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public final class AiBreakBlock {
   private AiBreakBlock() {
   }

   static boolean hasLavaBelow(BlockGetter level, BlockPos target) {
      for (int down = 1; down <= 16; down++) {
         BlockPos p = target.below(down);
         BlockState state = level.getBlockState(p);
         if (state.getFluidState().is(FluidTags.LAVA)) {
            return true;
         }

         if (!state.getFluidState().isEmpty() || !state.getCollisionShape(level, p).isEmpty()) {
            return false;
         }
      }

      return false;
   }

   public static AiOperation breakBlock(BlockPos target, int seconds) {
      return breakBlock(target, seconds, false);
   }

   public static AiOperation breakBlock(BlockPos target, int seconds, boolean requireDrops) {
      return breakBlock(target, seconds, requireDrops, Set.of());
   }

   public static AiOperation breakBlock(BlockPos target, int seconds, boolean requireDrops, Set<Long> protectedCells) {
      return breakBlock(target, seconds, requireDrops, protectedCells, protectedCells.isEmpty());
   }

   public static AiOperation breakBlock(BlockPos target, int seconds, boolean requireDrops, Set<Long> protectedCells, boolean allowScaffold) {
      return breakBlock(target, seconds, requireDrops, protectedCells, allowScaffold, Set.of());
   }

   public static AiOperation breakBlock(
      BlockPos target, int seconds, boolean requireDrops, Set<Long> protectedCells, boolean allowScaffold, Set<BlockPos> sealedWater
   ) {
      return breakBlock(target, seconds, requireDrops, protectedCells, allowScaffold, sealedWater, (state, stack) -> true);
   }

   public static AiOperation breakBlock(
      final BlockPos target,
      final int seconds,
      final boolean requireDrops,
      final Set<Long> protectedCells,
      final boolean allowScaffold,
      final Set<BlockPos> sealedWater,
      final BiPredicate<BlockState, ItemStack> acceptedTool
   ) {
      return new AiOperation() {
         final Minecraft mc = Minecraft.getInstance();
         final Object level = this.mc.level;
         final long deadline = System.nanoTime() + Math.max(1, Math.min(3600, seconds)) * 1000000000L;
         final AiAim aim = new AiAim();
         final AiBreakBlock.ChildTracker children = new AiBreakBlock.ChildTracker();
         final int originalSlot = this.mc.player == null ? -1 : this.mc.player.getInventory().getSelectedSlot();
         final AiBlockDigger init_RefQueue_DataUtil = new AiBlockDigger(
               this.mc,
               s -> this.mc.player.getInventory().setSelectedSlot(s),
               this.aim::aimAt,
               true,
               requireDrops,
               this.aim::tick
            )
            .withSealedWaterCompat(sealedWater)
            .withAcceptedToolCompat(acceptedTool);
         AiOperation child;
         int load_I;
         boolean init_Z;
         boolean load_Z;

         @Override
         public AiOperation.Result tick() {
            if (this.init_Z || this.mc.player == null || this.mc.level == null) {
               return this.finish(AiOperation.Status.CANCELLED, AiFailure.CANCELLED, "Break cancelled");
            }

            if (this.mc.level != this.level) {
               return this.finish(AiOperation.Status.FAILED, AiFailure.WORLD_CHANGED, "World changed during break");
            }

            if (System.nanoTime() >= this.deadline) {
               return this.finish(AiOperation.Status.TIMED_OUT, AiFailure.TIMEOUT, "Break deadline reached");
            }

            if (this.child != null) {
               AiOperation.Result result = this.children.tickChild(this.child);
               if (!result.terminal()) {
                  return result;
               }

               this.child = null;
               if (result.status() != AiOperation.Status.SUCCEEDED || this.children.needsReconciliation) {
                  return this.finishWith(result);
               }
            }

            if (this.mc.level.getBlockState(target).getDestroySpeed(this.mc.level, target) < 0.0F) {
               return this.finish(AiOperation.Status.FAILED, AiFailure.UNBREAKABLE_OBSTACLE, "Target is unbreakable");
            }

            if (requireDrops && MiningCost.bestToolSlot(this.mc, this.mc.level.getBlockState(target), true) < 0) {
               return this.finish(AiOperation.Status.FAILED, AiFailure.TOOL_WORN, "No suitable harvesting tool above the durability floor");
            }

            if (requireDrops && AiBreakBlock.hasLavaBelow(this.mc.level, target)) {
               return this.finish(AiOperation.Status.FAILED, AiFailure.LAVA_DETECTED, "Its drop would fall into lava below it");
            }

            if (!this.init_RefQueue_DataUtil.isDiggingTargetCompat(target) && AiBlockInteraction.findClimbSafeFaceHit(this.mc, target, null) == null) {
               if (this.mc.level.getChunkSource().getChunk(target.getX() >> 4, target.getZ() >> 4, ChunkStatus.FULL, false) != null
                  && this.mc.level.getBlockState(target).isAir()) {
                  return this.finish(AiOperation.Status.SUCCEEDED, null, "Block already absent");
               }
            } else {
               if (this.mc.player.onClimbable() && !this.mc.player.onGround()) {
                  this.mc.options.keyShift.setDown(true);
                  if (!this.load_Z) {
                     this.load_Z = true;
                     return this.running("Holding the ladder before breaking");
                  }

                  if (Boolean.getBoolean("autism.navigation.debug") && this.mc.player.tickCount % 5 == 0) {
                     AiNavDebug.log(
                        "break ladder target="
                           + target
                           + " body="
                           + this.mc.player.position()
                           + " motion="
                           + this.mc.player.getDeltaMovement()
                           + " pose="
                           + this.mc.player.getPose()
                           + " shift="
                           + this.mc.player.isShiftKeyDown()
                     );
                  }
               }

               boolean wasDigging = this.init_RefQueue_DataUtil.isDiggingTargetCompat(target);
               AiBlockDigger.DigOutcome status = this.init_RefQueue_DataUtil.digCompat(target);
               this.aim.tick();
               if (status == AiBlockDigger.DigOutcome.load) {
                  return this.finish(AiOperation.Status.SUCCEEDED, null, "Observed target removed");
               }

               if (status == AiBlockDigger.DigOutcome.init) {
                  return this.running("Breaking target");
               }

               this.children.recordTransition(AiOperation.Status.FAILED, wasDigging, this.init_RefQueue_DataUtil.isDiggingTargetCompat(target));
               if (this.children.needsReconciliation) {
                  return this.finish(
                     AiOperation.Status.FAILED, AiFailure.UNREACHABLE, "Break stopped before its removal was observed: " + this.init_RefQueue_DataUtil.lastFailureReasonCompat()
                  );
               }

               if (++this.load_I >= 2) {
                  return this.finish(AiOperation.Status.FAILED, AiFailure.UNREACHABLE, "Target could not be broken: " + this.init_RefQueue_DataUtil.lastFailureReasonCompat());
               }
            }

            this.releaseSneak();
            this.child = AiGoto.approachBlockToBreakCompat(
               target,
               (int)Math.max(1L, (this.deadline - System.nanoTime()) / 1000000000L),
               protectedCells,
               allowScaffold,
               this.load_I > 0 ? 1.75 : 0.25,
               !sealedWater.isEmpty()
            );
            return this.running("Approaching target");
         }

         AiOperation.Result running(String message) {
            return new AiOperation.Result(AiOperation.Status.RUNNING, message, "{}", List.of());
         }

         AiOperation.Result finish(AiOperation.Status status, AiFailure failure, String message) {
            return this.finishWith(new AiOperation.Result(status, message, "{}", List.of(), failure));
         }

         AiOperation.Result finishWith(AiOperation.Result result) {
            this.children.markPending(this.requiresReconciliationAfterEmergency());
            result = this.children.wrapResult(result);
            this.cancel();
            return result;
         }

         void releaseSneak() {
            if (this.load_Z) {
               this.load_Z = false;
               this.mc.options.keyShift.setDown(KeyMappingControl.of(this.mc.options.keyShift).autism$physicalForBody());
            }
         }

         @Override
         public boolean canInterruptNow() {
            return this.children
               .canInterrupt(this.init_RefQueue_DataUtil.isDiggingTargetCompat(target), this.child == null || this.child.canInterruptNow());
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.children.needsReconciliation
               || this.init_RefQueue_DataUtil.isDiggingTargetCompat(target)
               || this.child != null && this.child.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            if (!this.init_Z) {
               this.children.markPending(this.requiresReconciliationAfterEmergency());
               this.init_Z = true;
               if (this.child != null) {
                  this.child.cancel();
                  this.child = null;
               }

               this.init_RefQueue_DataUtil.stopCompat();
               this.aim.clear();
               this.releaseSneak();
               if (this.mc.player != null && this.originalSlot >= 0) {
                  this.mc.player.getInventory().setSelectedSlot(this.originalSlot);
               }
            }
         }
      };
   }

   static final class ChildTracker {
      boolean needsReconciliation;

      boolean canInterrupt(boolean digging, boolean approachSafe) {
         return !this.needsReconciliation && !digging && approachSafe;
      }

      void markPending(boolean currentPending) {
         this.needsReconciliation |= currentPending;
      }

      void recordTransition(AiOperation.Status status, boolean before, boolean after) {
         this.needsReconciliation = this.needsReconciliation | (status == AiOperation.Status.UNCERTAIN || after || status != AiOperation.Status.SUCCEEDED && before);
      }

      AiOperation.Result tickChild(AiOperation child) {
         boolean before = child.requiresReconciliationAfterEmergency();
         AiOperation.Result result = child.tick();
         if (result.terminal()) {
            this.recordTransition(result.status(), before, child.requiresReconciliationAfterEmergency());
            child.cancel();
         }

         return result;
      }

      AiOperation.Result wrapResult(AiOperation.Result result) {
         if (!this.needsReconciliation) {
            return result;
         }

         JsonObject data = JsonParser.parseString(result.dataJson()).getAsJsonObject().deepCopy();
         data.addProperty("terminationStatus", result.status().name());
         data.addProperty("interruptedActionNeedsReconciliation", true);
         data.addProperty("reobserveBeforeResuming", true);
         return new AiOperation.Result(
            AiOperation.Status.UNCERTAIN,
            result.message() + "; pending break changes need observation before restarting work",
            data.toString(),
            result.evidence(),
            result.failure()
         );
      }
   }
}
