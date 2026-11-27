package autismclient.ai.engine;

import autismclient.ai.AiContainerTransactions;
import autismclient.ai.AiItems;
import autismclient.ai.AiVisualization;
import autismclient.ai.AiServerEvidence;
import autismclient.compat263.Compat263;
import autismclient.util.AutismLiteVariant;
import autismclient.util.BlockPlacementPlanner;
import autismclient.util.InventorySlotHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class AiPlacer {
   private static final int MAX_TICKS = 80;
   private static final int CONFIRM_TICKS = 2;
   private static final int PLAN_RAY_BUDGET = 48;
   static final int MAX_NO_FACE_TICKS = 12;
   private int noFaceTicks;
   private final AiAim aim = new AiAim();
   private BlockPos targetPos;
   private Block targetBlock;
   private Item targetItem;
   private ItemStack exactStack = ItemStack.EMPTY;
   private int swapDestHotbarSlot = -1;
   private AiContainerTransactions.Inner_DataUtil pendingContainerSwap;
   private BlockPlacementPlanner.PlacementPlan currentPlan;
   private int elapsedTicks;
   private int presentTicks;
   private boolean clicked;
   private long initialBlockRevision;
   private String failureReason = "";
   static final int PROBE_RAY_BUDGET = 1024;
   private BlockPlacementPlanner.RejectReason lastRejectReason;

   public AiPlacer() {
      this.lastRejectReason = BlockPlacementPlanner.RejectReason.NONE;
      this.aim.setProfile(AiAim.AimProfile.NORMAL);
   }

   public AiPlacer fast() {
      this.aim.setProfile(AiAim.AimProfile.FAST);
      return this;
   }

   public void start(BlockPos target, Item placeable) {
      this.targetPos = target == null ? null : target.immutable();
      this.targetItem = placeable;
      this.exactStack = ItemStack.EMPTY;
      this.swapDestHotbarSlot = -1;
      this.pendingContainerSwap = null;
      this.targetBlock = placeable instanceof BlockItem blockItem ? blockItem.getBlock() : null;
      this.currentPlan = null;
      this.elapsedTicks = 0;
      this.noFaceTicks = 0;
      this.presentTicks = 0;
      this.clicked = false;
      this.initialBlockRevision = target == null ? 0L : AiServerEvidence.blockRevision(target);
      this.failureReason = "";
      this.lastRejectReason = BlockPlacementPlanner.RejectReason.NONE;
      this.aim.clear();
   }

   public void startWithExactStack(BlockPos target, ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         this.start(target, stack.getItem());
         this.exactStack = stack.copy();
      } else {
         throw new IllegalArgumentException("A placement stack is required");
      }
   }

   public BlockPos targetPos() {
      return this.targetPos;
   }

   public boolean hasClicked() {
      return this.clicked;
   }

   public boolean needsReconciliation() {
      if (this.pendingContainerSwap != null) {
         return true;
      }

      if (!this.clicked) {
         return false;
      }

      Minecraft mc = Minecraft.getInstance();
      return mc == null
         || mc.level == null
         || this.targetPos == null
         || mc.level.getBlockState(this.targetPos).getBlock() != this.targetBlock
         || AiServerEvidence.blockRevision(this.targetPos) <= this.initialBlockRevision;
   }

   public String failureReason() {
      return this.failureReason;
   }

   public AiPlacer.PlaceStatus tick() {
      Minecraft mc = Minecraft.getInstance();
      if (this.targetPos == null || mc.player == null || mc.level == null) {
         return this.fail("No placement target");
      }

      if (this.targetBlock == null) {
         return this.fail("Item has no placeable block form");
      }

      if (this.pendingContainerSwap != null) {
         AiContainerTransactions.Inner_StreamUtil state = this.pendingContainerSwap.pollAndResync(mc.player.containerMenu, System.nanoTime());
         if (state == AiContainerTransactions.Inner_StreamUtil.WAITING) {
            return AiPlacer.PlaceStatus.WORKING;
         }

         if (state == AiContainerTransactions.Inner_StreamUtil.UNCERTAIN) {
            return this.fail("The server did not confirm the portable container swap");
         }

         this.pendingContainerSwap = null;
         if (!ItemStack.matches(mc.player.getInventory().getItem(this.swapDestHotbarSlot), this.exactStack)) {
            return this.fail("The server swapped a different portable container stack");
         }

         InventorySlotHelper.selectHotbarSlot(mc, this.swapDestHotbarSlot);
         return AiPlacer.PlaceStatus.WORKING;
      } else {
         if (!AutismLiteVariant.enabled()) {
            AiVisualization.init(this, this.targetPos, this.targetBlock.defaultBlockState());
         }

         if (mc.level.getBlockState(this.targetPos).getBlock() != this.targetBlock) {
            this.presentTicks = 0;
            if (++this.elapsedTicks > 80) {
               return this.fail(
                  this.clicked
                     ? "Placement did not take effect at " + this.targetPos.toShortString()
                     : "No reachable support face for "
                        + this.targetPos.toShortString()
                        + " ("
                        + this.lastRejectReason
                        + "; "
                        + mc.level.getBlockState(this.targetPos).getBlock().getName().getString()
                        + " on "
                        + mc.level.getBlockState(this.targetPos.below()).getBlock().getName().getString()
                        + ")"
               );
            }

            if (mc.player.containerMenu != mc.player.inventoryMenu) {
               return this.fail("A container screen blocks placement");
            }

            if (!this.exactStack.isEmpty()) {
               if (this.clicked) {
                  return AiPlacer.PlaceStatus.WORKING;
               }

               if (!mc.player.containerMenu.getCarried().isEmpty()) {
                  return this.fail("Clear the cursor before placing the portable container");
               }

               int slot = findMatchingSlot(mc.player.getInventory(), this.exactStack);
               if (slot < 0) {
                  return this.fail("The selected portable container changed or left the inventory");
               }

               if (slot >= 9) {
                  int destination = mc.player.getInventory().getSelectedSlot();

                  for (int i = 0; i < 9; i++) {
                     if (mc.player.getInventory().getItem(i).isEmpty()) {
                        destination = i;
                        break;
                     }
                  }

                  this.swapDestHotbarSlot = destination;
                  this.pendingContainerSwap = AiContainerTransactions.sendVerifiedSwap(mc.player.inventoryMenu, slot == 40 ? 45 : slot, 36 + destination, destination);
                  if (this.pendingContainerSwap == null) {
                     return this.fail("Could not start the portable container swap");
                  }

                  return AiPlacer.PlaceStatus.WORKING;
               }

               if (mc.player.getInventory().getSelectedSlot() != slot) {
                  InventorySlotHelper.selectHotbarSlot(mc, slot);
                  return AiPlacer.PlaceStatus.WORKING;
               }
            } else {
               AiItems.ItemSpec held = AiItems.resolve(BuiltInRegistries.ITEM.getKey(this.targetItem).toString());
               if (AiItems.equipHotbar(held) < 0) {
                  return this.fail("No " + BuiltInRegistries.ITEM.getKey(this.targetItem) + " in inventory");
               }
            }

            if (this.currentPlan == null || !mc.level.getBlockState(this.currentPlan.init_U_TaskQueue().load_BlockPos()).canOcclude()) {
               this.currentPlan = this.planPlacement(mc);
               if (this.currentPlan == null) {
                  return ++this.noFaceTicks >= 12 && !this.clicked
                     ? this.fail("No reachable support face for " + this.targetPos.toShortString() + " (" + this.lastRejectReason + ")")
                     : AiPlacer.PlaceStatus.WORKING;
               }

               this.noFaceTicks = 0;
            }

            this.aim.aimAt(this.currentPlan.init_U_DataUtil().init_Vec3());
            this.aim.tick();
            if (!AiAim.isLookingAtBlock(this.currentPlan.init_BlockHitResult().getBlockPos())) {
               return AiPlacer.PlaceStatus.WORKING;
            }

            BlockHitResult ray = AiAim.raycastLookBlock();
            BlockHitResult click = ray != null
                  && ray.getBlockPos().equals(this.currentPlan.init_BlockHitResult().getBlockPos())
                  && ray.getDirection() == this.currentPlan.init_BlockHitResult().getDirection()
               ? ray
               : this.currentPlan.init_BlockHitResult();
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, click);
            Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
            this.clicked = true;
            this.currentPlan = null;
            return AiPlacer.PlaceStatus.WORKING;
         } else {
            if (!this.exactStack.isEmpty() && !this.clicked) {
               return this.fail("Another block occupied the portable container's cell");
            }

            if (++this.presentTicks < 2 || this.clicked && AiServerEvidence.blockRevision(this.targetPos) <= this.initialBlockRevision) {
               return ++this.elapsedTicks > 80 ? this.fail("Placement was not confirmed by the server") : AiPlacer.PlaceStatus.WORKING;
            }

            if (this.clicked && !this.targetBlock.defaultBlockState().hasBlockEntity() && this.targetBlock != Blocks.CRAFTING_TABLE) {
               AiEditLedger.write_V(this.targetPos);
            }

            return AiPlacer.PlaceStatus.PLACED;
         }
      }
   }

   public void reset() {
      this.aim.clear();
      this.targetPos = null;
   }

   private BlockPlacementPlanner.PlacementPlan planPlacement(Minecraft mc) {
      Vec3 eye = mc.player.getEyePosition();
      double reach = mc.player.blockInteractionRange();
      BlockPlacementPlanner.Query request = new BlockPlacementPlanner.Query(
            this.targetPos,
            eye,
            reach,
            BlockPlacementPlanner.init(this.exactStack.isEmpty() ? new ItemStack(this.targetItem) : this.exactStack.copy(), mc.player, InteractionHand.MAIN_HAND)
         )
         .init_U_StateCache_U_StreamUtil(new BlockPlacementPlanner.RayBudget(48));
      BlockPlacementPlanner.RejectReason[] refusal = new BlockPlacementPlanner.RejectReason[1];
      BlockPlacementPlanner.PlacementPlan best = BlockPlacementPlanner.init(request, refusal);
      if (best == null && refusal[0] != null) {
         this.lastRejectReason = refusal[0];
      }

      return best;
   }

   static int findMatchingSlot(Container inventory, ItemStack wanted) {
      for (int slot = 0; slot < Math.min(36, inventory.getContainerSize()); slot++) {
         if (ItemStack.matches(inventory.getItem(slot), wanted)) {
            return slot;
         }
      }

      return inventory.getContainerSize() > 40 && ItemStack.matches(inventory.getItem(40), wanted) ? 40 : -1;
   }

   public String statusSummary() {
      return (
            this.clicked
               ? "clicked, no block yet"
               : (
                  this.currentPlan == null
                     ? "no face (" + this.lastRejectReason + ")"
                     : "turning to " + this.currentPlan.init_BlockHitResult().getBlockPos().toShortString()
               )
         )
         + " after "
         + this.elapsedTicks
         + " ticks";
   }

   public static boolean canPlaceFromHere(BlockPos target, Item placeable) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null && target != null && placeable instanceof BlockItem) {
         BlockPlacementPlanner.Query request = new BlockPlacementPlanner.Query(
               target, mc.player.getEyePosition(), mc.player.blockInteractionRange(), BlockPlacementPlanner.init(new ItemStack(placeable), mc.player, InteractionHand.MAIN_HAND)
            )
            .init_U_StateCache_U_StreamUtil(new BlockPlacementPlanner.RayBudget(1024));
         return BlockPlacementPlanner.init_U_CachePool(request) != null;
      } else {
         return false;
      }
   }

   private AiPlacer.PlaceStatus fail(String reason) {
      this.failureReason = reason;
      return AiPlacer.PlaceStatus.FAILED;
   }

   public enum PlaceStatus {
      WORKING,
      PLACED,
      FAILED;
   }
}
