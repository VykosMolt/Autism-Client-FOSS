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

   static boolean init(BlockGetter level, BlockPos target) {
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

   public static AiOperation init(BlockPos target, int seconds) {
      return init(target, seconds, false);
   }

   public static AiOperation init(BlockPos target, int seconds, boolean requireDrops) {
      return init(target, seconds, requireDrops, Set.of());
   }

   public static AiOperation init(BlockPos target, int seconds, boolean requireDrops, Set<Long> protectedCells) {
      return init(target, seconds, requireDrops, protectedCells, protectedCells.isEmpty());
   }

   public static AiOperation init(BlockPos target, int seconds, boolean requireDrops, Set<Long> protectedCells, boolean allowScaffold) {
      return init(target, seconds, requireDrops, protectedCells, allowScaffold, Set.of());
   }

   public static AiOperation init(
      BlockPos target, int seconds, boolean requireDrops, Set<Long> protectedCells, boolean allowScaffold, Set<BlockPos> sealedWater
   ) {
      return init(target, seconds, requireDrops, protectedCells, allowScaffold, sealedWater, (state, stack) -> true);
   }

   public static AiOperation init(
      final BlockPos target,
      final int seconds,
      final boolean requireDrops,
      final Set<Long> protectedCells,
      final boolean allowScaffold,
      final Set<BlockPos> sealedWater,
      final BiPredicate<BlockState, ItemStack> acceptedTool
   ) {
      return new AiOperation() {
         final Minecraft init_Minecraft = Minecraft.getInstance();
         final Object init_Obj = this.init_Minecraft.level;
         final long init_J = System.nanoTime() + Math.max(1, Math.min(3600, seconds)) * 1000000000L;
         final AiAim init_DataUtil = new AiAim();
         final AiBreakBlock.Inner_DataUtil init_ConfigStore_DataUt = new AiBreakBlock.Inner_DataUtil();
         final int init_I = this.init_Minecraft.player == null ? -1 : this.init_Minecraft.player.getInventory().getSelectedSlot();
         final AiBlockDigger init_RefQueue_DataUtil = new AiBlockDigger(
               this.init_Minecraft,
               s -> this.init_Minecraft.player.getInventory().setSelectedSlot(s),
               this.init_DataUtil::aimAt,
               true,
               requireDrops,
               this.init_DataUtil::tick
            )
            .init_RefQueue_DataUtil_Set(sealedWater)
            .init_RefQueue_DataUtil_BiPredicate(acceptedTool);
         AiOperation init_AiOperation;
         int load_I;
         boolean init_Z;
         boolean load_Z;

         @Override
         public AiOperation.Result tick() {
            if (this.init_Z || this.init_Minecraft.player == null || this.init_Minecraft.level == null) {
               return this.init(AiOperation.Status.CANCELLED, AiFailure.CANCELLED, "Break cancelled");
            }

            if (this.init_Minecraft.level != this.init_Obj) {
               return this.init(AiOperation.Status.FAILED, AiFailure.WORLD_CHANGED, "World changed during break");
            }

            if (System.nanoTime() >= this.init_J) {
               return this.init(AiOperation.Status.TIMED_OUT, AiFailure.TIMEOUT, "Break deadline reached");
            }

            if (this.init_AiOperation != null) {
               AiOperation.Result result = this.init_ConfigStore_DataUt.init(this.init_AiOperation);
               if (!result.terminal()) {
                  return result;
               }

               this.init_AiOperation = null;
               if (result.status() != AiOperation.Status.SUCCEEDED || this.init_ConfigStore_DataUt.init) {
                  return this.init(result);
               }
            }

            if (this.init_Minecraft.level.getBlockState(target).getDestroySpeed(this.init_Minecraft.level, target) < 0.0F) {
               return this.init(AiOperation.Status.FAILED, AiFailure.UNBREAKABLE_OBSTACLE, "Target is unbreakable");
            }

            if (requireDrops && MiningCost.load(this.init_Minecraft, this.init_Minecraft.level.getBlockState(target), true) < 0) {
               return this.init(AiOperation.Status.FAILED, AiFailure.TOOL_WORN, "No suitable harvesting tool above the durability floor");
            }

            if (requireDrops && AiBreakBlock.init(this.init_Minecraft.level, target)) {
               return this.init(AiOperation.Status.FAILED, AiFailure.LAVA_DETECTED, "Its drop would fall into lava below it");
            }

            if (!this.init_RefQueue_DataUtil.init_Z_BlockPos(target) && AiBlockInteraction.findClimbSafeFaceHit(this.init_Minecraft, target, null) == null) {
               if (this.init_Minecraft.level.getChunkSource().getChunk(target.getX() >> 4, target.getZ() >> 4, ChunkStatus.FULL, false) != null
                  && this.init_Minecraft.level.getBlockState(target).isAir()) {
                  return this.init(AiOperation.Status.SUCCEEDED, null, "Block already absent");
               }
            } else {
               if (this.init_Minecraft.player.onClimbable() && !this.init_Minecraft.player.onGround()) {
                  this.init_Minecraft.options.keyShift.setDown(true);
                  if (!this.load_Z) {
                     this.load_Z = true;
                     return this.init("Holding the ladder before breaking");
                  }

                  if (Boolean.getBoolean("autism.navigation.debug") && this.init_Minecraft.player.tickCount % 5 == 0) {
                     AiNavDebug.init(
                        "break ladder target="
                           + target
                           + " body="
                           + this.init_Minecraft.player.position()
                           + " motion="
                           + this.init_Minecraft.player.getDeltaMovement()
                           + " pose="
                           + this.init_Minecraft.player.getPose()
                           + " shift="
                           + this.init_Minecraft.player.isShiftKeyDown()
                     );
                  }
               }

               boolean wasDigging = this.init_RefQueue_DataUtil.init_Z_BlockPos(target);
               AiBlockDigger.Inner_DataUtil_2 status = this.init_RefQueue_DataUtil.init_RefQueue_DataUtil__BlockPos(target);
               this.init_DataUtil.tick();
               if (status == AiBlockDigger.Inner_DataUtil_2.load) {
                  return this.init(AiOperation.Status.SUCCEEDED, null, "Observed target removed");
               }

               if (status == AiBlockDigger.Inner_DataUtil_2.init) {
                  return this.init("Breaking target");
               }

               this.init_ConfigStore_DataUt.init(AiOperation.Status.FAILED, wasDigging, this.init_RefQueue_DataUtil.init_Z_BlockPos(target));
               if (this.init_ConfigStore_DataUt.init) {
                  return this.init(
                     AiOperation.Status.FAILED, AiFailure.UNREACHABLE, "Break stopped before its removal was observed: " + this.init_RefQueue_DataUtil.init_Str()
                  );
               }

               if (++this.load_I >= 2) {
                  return this.init(AiOperation.Status.FAILED, AiFailure.UNREACHABLE, "Target could not be broken: " + this.init_RefQueue_DataUtil.init_Str());
               }
            }

            this.init();
            this.init_AiOperation = AiGoto.init_AiOperation_BlockPos_2(
               target,
               (int)Math.max(1L, (this.init_J - System.nanoTime()) / 1000000000L),
               protectedCells,
               allowScaffold,
               this.load_I > 0 ? 1.75 : 0.25,
               !sealedWater.isEmpty()
            );
            return this.init("Approaching target");
         }

         AiOperation.Result init(String message) {
            return new AiOperation.Result(AiOperation.Status.RUNNING, message, "{}", List.of());
         }

         AiOperation.Result init(AiOperation.Status status, AiFailure failure, String message) {
            return this.init(new AiOperation.Result(status, message, "{}", List.of(), failure));
         }

         AiOperation.Result init(AiOperation.Result result) {
            this.init_ConfigStore_DataUt.init(this.requiresReconciliationAfterEmergency());
            result = this.init_ConfigStore_DataUt.init(result);
            this.cancel();
            return result;
         }

         void init() {
            if (this.load_Z) {
               this.load_Z = false;
               this.init_Minecraft.options.keyShift.setDown(KeyMappingControl.of(this.init_Minecraft.options.keyShift).autism$physicalForBody());
            }
         }

         @Override
         public boolean canInterruptNow() {
            return this.init_ConfigStore_DataUt
               .init(this.init_RefQueue_DataUtil.init_Z_BlockPos(target), this.init_AiOperation == null || this.init_AiOperation.canInterruptNow());
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.init_ConfigStore_DataUt.init
               || this.init_RefQueue_DataUtil.init_Z_BlockPos(target)
               || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            if (!this.init_Z) {
               this.init_ConfigStore_DataUt.init(this.requiresReconciliationAfterEmergency());
               this.init_Z = true;
               if (this.init_AiOperation != null) {
                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
               }

               this.init_RefQueue_DataUtil.init_V();
               this.init_DataUtil.clear();
               this.init();
               if (this.init_Minecraft.player != null && this.init_I >= 0) {
                  this.init_Minecraft.player.getInventory().setSelectedSlot(this.init_I);
               }
            }
         }
      };
   }

   static final class Inner_DataUtil {
      boolean init;

      boolean init(boolean digging, boolean approachSafe) {
         return !this.init && !digging && approachSafe;
      }

      void init(boolean currentPending) {
         this.init |= currentPending;
      }

      void init(AiOperation.Status status, boolean before, boolean after) {
         this.init = this.init | (status == AiOperation.Status.UNCERTAIN || after || status != AiOperation.Status.SUCCEEDED && before);
      }

      AiOperation.Result init(AiOperation child) {
         boolean before = child.requiresReconciliationAfterEmergency();
         AiOperation.Result result = child.tick();
         if (result.terminal()) {
            this.init(result.status(), before, child.requiresReconciliationAfterEmergency());
            child.cancel();
         }

         return result;
      }

      AiOperation.Result init(AiOperation.Result result) {
         if (!this.init) {
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
