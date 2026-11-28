package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;

final class AiStationPlacer {
   static final int init_I = 60;
   private final Item item;
   private final String name;
   private final autismclient.ai.engine.AiPlacer placer = new autismclient.ai.engine.AiPlacer();
   private List<BlockPos> candidates;
   private int candidateIndex;
   private int placeTicks;
   private boolean placing;
   private boolean relocationTried;
   private AiOperation travel;
   private AiOperation clearOp;
   private boolean digOutTried;
   private BlockPos clearedCell;
   private BlockPos placed;
   private String failureReason = "";
   private String statusText = "";

   AiStationPlacer(Item item, String name) {
      this.item = item;
      this.name = name;
   }

   AiStationPlacer.Inner_DataUtil tick() {
      if (this.placed != null) {
         return AiStationPlacer.Inner_DataUtil.PLACED;
      }

      if (this.clearOp != null) {
         AiOperation.Result r = this.clearOp.tick();
         if (!r.terminal()) {
            this.statusText = "Making room for the " + this.name + ": " + r.message();
            return AiStationPlacer.Inner_DataUtil.WORKING;
         }

         this.clearOp.cancel();
         this.clearOp = null;
         this.candidates = null;
         this.candidateIndex = 0;
      }

      if (this.travel != null) {
         AiOperation.Result r = this.travel.tick();
         if (!r.terminal()) {
            this.statusText = "Moving to open ground for the " + this.name + ": " + r.message();
            return AiStationPlacer.Inner_DataUtil.WORKING;
         }

         this.travel.cancel();
         this.travel = null;
         this.candidates = null;
      }

      if (!this.placing) {
         if (this.candidates == null && !this.relocationTried && AiCraft.isStandingOnLeaves() && this.moveToOpenSpot()) {
            return AiStationPlacer.Inner_DataUtil.WORKING;
         }

         if (this.candidates == null) {
            this.candidates = new ArrayList<>(AiCraft.candidatePlacementSpots());
            this.candidateIndex = 0;
            this.candidates.removeIf(spotx -> !spotx.equals(this.clearedCell) && !autismclient.ai.engine.AiPlacer.canPlaceFromHere(spotx, this.item));
            if (this.clearedCell != null && this.candidates.remove(this.clearedCell)) {
               this.candidates = new ArrayList<>(this.candidates);
               this.candidates.add(0, this.clearedCell);
            }

            boolean allOurs = !this.candidates.isEmpty();

            for (BlockPos spot : this.candidates) {
               if (!autismclient.ai.engine.AiEditLedger.hasBreakRecord(spot)) {
                  allOurs = false;
                  break;
               }
            }

            if (allOurs && this.digOutSpot()) {
               this.candidates = null;
               return AiStationPlacer.Inner_DataUtil.WORKING;
            }
         }

         if (this.candidateIndex >= Math.min(this.candidates.size(), 8)) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null
               && mc.level != null
               && isWalledIn(mc.player.blockPosition(), cell -> !mc.level.getBlockState(cell).getCollisionShape(mc.level, cell).isEmpty())
               && this.digOutSpot()) {
               return AiStationPlacer.Inner_DataUtil.WORKING;
            }

            if (this.moveToOpenSpot()) {
               return AiStationPlacer.Inner_DataUtil.WORKING;
            }

            if (this.digOutSpot()) {
               return AiStationPlacer.Inner_DataUtil.WORKING;
            }

            if (this.failureReason.isEmpty()) {
               this.failureReason = "no free supported spot within two blocks";
            }

            return AiStationPlacer.Inner_DataUtil.FAILED;
         }

         this.placer.start(this.candidates.get(this.candidateIndex), this.item);
         this.placing = true;
         this.placeTicks = 0;
      }

      autismclient.ai.engine.AiPlacer.PlaceStatus status = this.placer.tick();
      if (status == autismclient.ai.engine.AiPlacer.PlaceStatus.PLACED) {
         this.placing = false;
         this.placed = this.placer.targetPos();
         return AiStationPlacer.Inner_DataUtil.PLACED;
      } else if (status != autismclient.ai.engine.AiPlacer.PlaceStatus.FAILED && ++this.placeTicks <= 60) {
         this.statusText = "Placing a " + this.name;
         return AiStationPlacer.Inner_DataUtil.WORKING;
      } else {
         this.placing = false;
         this.failureReason = status == autismclient.ai.engine.AiPlacer.PlaceStatus.FAILED
            ? this.placer.failureReason()
            : "not placed within 60 ticks: " + this.placer.statusSummary();
         this.placer.reset();
         AutismClientAddon.LOG
            .info("[AI] {} spot {} unusable after {} ticks: {}", this.name, this.candidates.get(this.candidateIndex).toShortString(), this.placeTicks, this.failureReason);
         this.candidateIndex++;
         this.statusText = this.name + " spot unusable (" + this.failureReason + "); trying the next spot";
         return AiStationPlacer.Inner_DataUtil.WORKING;
      }
   }

   static boolean isWalledIn(BlockPos feet, Predicate<BlockPos> collision) {
      for (Direction side : Plane.HORIZONTAL) {
         BlockPos beside = feet.relative(side);
         if (!collision.test(beside) && !collision.test(beside.above())) {
            return false;
         }
      }

      return true;
   }

   private boolean moveToOpenSpot() {
      if (this.relocationTried) {
         return false;
      }

      this.relocationTried = true;
      BlockPos open = AiCraft.findOpenStationSpot(6);
      if (open == null) {
         open = AiCraft.findFlatStationSpot(3);
      }

      if (open == null) {
         return false;
      }

      AutismClientAddon.LOG.info("[AI] no spot for a {} here, moving to {}", this.name, open.toShortString());
      this.init(open);
      this.travel = AiGoto.travelToCompat(open, 0.6, true, false, false, 30);
      this.statusText = "No room for a " + this.name + " here; moving to " + open.toShortString();
      return true;
   }

   private void init(BlockPos open) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         BlockPos feet = mc.player.blockPosition();
         ArrayList<Direction> sides = new ArrayList<>();
         Plane.HORIZONTAL.forEach(sides::add);
         sides.sort(Comparator.comparingDouble(sidex -> feet.relative(sidex).distSqr(open)));

         for (Direction side : sides) {
            BlockPos cell = feet.relative(side);
            BlockPos above = cell.above();
            boolean lowerLeaf = mc.level.getBlockState(cell).is(BlockTags.LEAVES);
            boolean upperLeaf = mc.level.getBlockState(above).is(BlockTags.LEAVES);
            if ((lowerLeaf || upperLeaf)
               && (lowerLeaf || mc.level.getBlockState(cell).getCollisionShape(mc.level, cell).isEmpty())
               && (upperLeaf || mc.level.getBlockState(above).getCollisionShape(mc.level, above).isEmpty())
               && mc.level.getFluidState(cell).isEmpty()
               && mc.level.getFluidState(above).isEmpty()) {
               boolean landing = false;

               for (int drop = 1; drop <= 3; drop++) {
                  BlockPos below = cell.below(drop);
                  if (AiGoto.isStandableCompat(below)) {
                     landing = true;
                     break;
                  }

                  if (!mc.level.getBlockState(below).getCollisionShape(mc.level, below).isEmpty() || !mc.level.getFluidState(below).isEmpty()) {
                     break;
                  }
               }

               if (landing) {
                  AutismClientAddon.LOG.info("[AI] clearing a leaf exit for the {} at {}", this.name, cell.toShortString());
                  this.clearOp = lowerLeaf && upperLeaf ? breakUpperThenLower(above, cell) : autismclient.ai.engine.AiBreakBlock.breakBlock(upperLeaf ? above : cell, 10);
                  return;
               }
            }
         }
      }
   }

   private boolean digOutSpot() {
      if (this.digOutTried) {
         return false;
      }

      this.digOutTried = true;
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         BlockPos feet = mc.player.blockPosition();

         for (Direction side : Plane.HORIZONTAL) {
            BlockPos cell = feet.relative(side);
            BlockState state = mc.level.getBlockState(cell);
            if (isBreakable(mc, cell) && mc.level.getBlockState(cell.below()).isCollisionShapeFullBlock(mc.level, cell.below())) {
               BlockPos above = cell.above();
               boolean clearHead = !mc.level.getBlockState(above).getCollisionShape(mc.level, above).isEmpty();
               if (!clearHead || isBreakable(mc, above)) {
                  AutismClientAddon.LOG.info("[AI] no spot for a {} here, digging one out at {}", this.name, cell.toShortString());
                  this.clearOp = clearHead ? breakUpperThenLower(above, cell) : autismclient.ai.engine.AiBreakBlock.breakBlock(cell, 10);
                  this.clearedCell = cell.immutable();
                  this.statusText = "Digging out a spot for the " + this.name;
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static boolean isBreakable(Minecraft mc, BlockPos cell) {
      BlockState state = mc.level.getBlockState(cell);
      if (!state.isAir() && !state.hasBlockEntity() && !(state.getDestroySpeed(mc.level, cell) < 0.0F) && state.getFluidState().isEmpty()) {
         if (BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().endsWith("_ore")) {
            return false;
         }

         if (autismclient.ai.engine.MiningCost.bestToolSlot(mc, state, false) < 0) {
            return false;
         }

         for (Direction side : Direction.values()) {
            if (!mc.level.getFluidState(cell.relative(side)).isEmpty()) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static AiOperation breakUpperThenLower(final BlockPos above, final BlockPos cell) {
      return new AiOperation() {
         AiOperation child = autismclient.ai.engine.AiBreakBlock.breakBlock(above, 10);
         boolean breakingUpper = true;

         @Override
         public AiOperation.Result tick() {
            AiOperation.Result result = this.child.tick();
            if (result.terminal() && result.status() == AiOperation.Status.SUCCEEDED && this.breakingUpper) {
               this.child.cancel();
               this.breakingUpper = false;
               this.child = autismclient.ai.engine.AiBreakBlock.breakBlock(cell, 10);
               return this.child.tick();
            } else {
               return result;
            }
         }

         @Override
         public void cancel() {
            this.child.cancel();
         }

         @Override
         public boolean canInterruptNow() {
            return this.child.canInterruptNow();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.child.requiresReconciliationAfterEmergency();
         }
      };
   }

   BlockPos getPlaced() {
      return this.placed;
   }

   String getStatus() {
      return this.statusText;
   }

   String getFailureReason() {
      return this.failureReason;
   }

   boolean canInterrupt() {
      return !this.placer.needsReconciliation()
         && (this.travel == null || this.travel.canInterruptNow())
         && (this.clearOp == null || this.clearOp.canInterruptNow());
   }

   boolean needsReconciliation() {
      return this.placer.needsReconciliation()
         || this.travel != null && this.travel.requiresReconciliationAfterEmergency()
         || this.clearOp != null && this.clearOp.requiresReconciliationAfterEmergency();
   }

   void cancel() {
      this.placer.reset();
      if (this.travel != null) {
         this.travel.cancel();
         this.travel = null;
      }

      if (this.clearOp != null) {
         this.clearOp.cancel();
         this.clearOp = null;
      }
   }

   enum Inner_DataUtil {
      WORKING,
      PLACED,
      FAILED;
   }
}
