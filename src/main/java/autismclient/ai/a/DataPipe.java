package autismclient.ai.a;

import autismclient.ai.PkgCls_b;
import autismclient.ai.af;
import autismclient.ai.cG;
import autismclient.ai.ca;
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

public final class DataPipe {
   private static final int read = 80;
   private static final int write = 2;
   private static final int update = 48;
   static final int init_I = 12;
   private int sync;
   private final DataUtil init_DataUtil = new DataUtil();
   private BlockPos init_BlockPos;
   private Block init_Block;
   private Item init_Item;
   private ItemStack init_ItemStack = ItemStack.EMPTY;
   private int flush = -1;
   private PkgCls_b.Inner_DataUtil init_b_DataUtil;
   private BlockPlacementPlanner.Inner_CachePool init_U_CachePool;
   private int close;
   private int open;
   private boolean init_Z;
   private long init_J;
   private String init_Str = "";
   static final int load = 1024;
   private BlockPlacementPlanner.Inner_JsonUtil init_U_JsonUtil;

   public DataPipe() {
      this.init_U_JsonUtil = BlockPlacementPlanner.Inner_JsonUtil.NONE;
      this.init_DataUtil.init_V_DataUtil_DataUtil(DataUtil.Inner_DataUtil.read_DataUtil_DataUtil);
   }

   public DataPipe init_DataPipe() {
      this.init_DataUtil.init_V_DataUtil_DataUtil(DataUtil.Inner_DataUtil.write);
      return this;
   }

   public void init_V_BlockPos_Item(BlockPos target, Item placeable) {
      this.init_BlockPos = target == null ? null : target.immutable();
      this.init_Item = placeable;
      this.init_ItemStack = ItemStack.EMPTY;
      this.flush = -1;
      this.init_b_DataUtil = null;
      this.init_Block = placeable instanceof BlockItem blockItem ? blockItem.getBlock() : null;
      this.init_U_CachePool = null;
      this.close = 0;
      this.sync = 0;
      this.open = 0;
      this.init_Z = false;
      this.init_J = target == null ? 0L : ca.init_J_BlockPos(target);
      this.init_Str = "";
      this.init_U_JsonUtil = BlockPlacementPlanner.Inner_JsonUtil.NONE;
      this.init_DataUtil.init_V();
   }

   public void init_V_BlockPos_ItemStack(BlockPos target, ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         this.init_V_BlockPos_Item(target, stack.getItem());
         this.init_ItemStack = stack.copy();
      } else {
         throw new IllegalArgumentException("A placement stack is required");
      }
   }

   public BlockPos init_BlockPos() {
      return this.init_BlockPos;
   }

   public boolean init_Z() {
      return this.init_Z;
   }

   public boolean load_Z() {
      if (this.init_b_DataUtil != null) {
         return true;
      }

      if (!this.init_Z) {
         return false;
      }

      Minecraft mc = Minecraft.getInstance();
      return mc == null
         || mc.level == null
         || this.init_BlockPos == null
         || mc.level.getBlockState(this.init_BlockPos).getBlock() != this.init_Block
         || ca.init_J_BlockPos(this.init_BlockPos) <= this.init_J;
   }

   public String init_Str() {
      return this.init_Str;
   }

   public DataPipe.Inner_DataUtil init_DataPipe_DataUtil() {
      Minecraft mc = Minecraft.getInstance();
      if (this.init_BlockPos == null || mc.player == null || mc.level == null) {
         return this.init("No placement target");
      }

      if (this.init_Block == null) {
         return this.init("Item has no placeable block form");
      }

      if (this.init_b_DataUtil != null) {
         PkgCls_b.Inner_StreamUtil state = this.init_b_DataUtil.load(mc.player.containerMenu, System.nanoTime());
         if (state == PkgCls_b.Inner_StreamUtil.init) {
            return DataPipe.Inner_DataUtil.init;
         }

         if (state == PkgCls_b.Inner_StreamUtil.write) {
            return this.init("The server did not confirm the portable container swap");
         }

         this.init_b_DataUtil = null;
         if (!ItemStack.matches(mc.player.getInventory().getItem(this.flush), this.init_ItemStack)) {
            return this.init("The server swapped a different portable container stack");
         }

         InventorySlotHelper.init_V(mc, this.flush);
         return DataPipe.Inner_DataUtil.init;
      } else {
         if (!AutismLiteVariant.enabled()) {
            cG.init(this, this.init_BlockPos, this.init_Block.defaultBlockState());
         }

         if (mc.level.getBlockState(this.init_BlockPos).getBlock() != this.init_Block) {
            this.open = 0;
            if (++this.close > 80) {
               return this.init(
                  this.init_Z
                     ? "Placement did not take effect at " + this.init_BlockPos.toShortString()
                     : "No reachable support face for "
                        + this.init_BlockPos.toShortString()
                        + " ("
                        + this.init_U_JsonUtil
                        + "; "
                        + mc.level.getBlockState(this.init_BlockPos).getBlock().getName().getString()
                        + " on "
                        + mc.level.getBlockState(this.init_BlockPos.below()).getBlock().getName().getString()
                        + ")"
               );
            }

            if (mc.player.containerMenu != mc.player.inventoryMenu) {
               return this.init("A container screen blocks placement");
            }

            if (!this.init_ItemStack.isEmpty()) {
               if (this.init_Z) {
                  return DataPipe.Inner_DataUtil.init;
               }

               if (!mc.player.containerMenu.getCarried().isEmpty()) {
                  return this.init("Clear the cursor before placing the portable container");
               }

               int slot = init_I_Container_ItemStack(mc.player.getInventory(), this.init_ItemStack);
               if (slot < 0) {
                  return this.init("The selected portable container changed or left the inventory");
               }

               if (slot >= 9) {
                  int destination = mc.player.getInventory().getSelectedSlot();

                  for (int i = 0; i < 9; i++) {
                     if (mc.player.getInventory().getItem(i).isEmpty()) {
                        destination = i;
                        break;
                     }
                  }

                  this.flush = destination;
                  this.init_b_DataUtil = PkgCls_b.init(mc.player.inventoryMenu, slot == 40 ? 45 : slot, 36 + destination, destination);
                  if (this.init_b_DataUtil == null) {
                     return this.init("Could not start the portable container swap");
                  }

                  return DataPipe.Inner_DataUtil.init;
               }

               if (mc.player.getInventory().getSelectedSlot() != slot) {
                  InventorySlotHelper.init_V(mc, slot);
                  return DataPipe.Inner_DataUtil.init;
               }
            } else {
               af.Inner_DataUtil held = af.init_af_DataUtil(BuiltInRegistries.ITEM.getKey(this.init_Item).toString());
               if (af.read(held) < 0) {
                  return this.init("No " + BuiltInRegistries.ITEM.getKey(this.init_Item) + " in inventory");
               }
            }

            if (this.init_U_CachePool == null || !mc.level.getBlockState(this.init_U_CachePool.init_U_TaskQueue().load_BlockPos()).canOcclude()) {
               this.init_U_CachePool = this.init(mc);
               if (this.init_U_CachePool == null) {
                  return ++this.sync >= 12 && !this.init_Z
                     ? this.init("No reachable support face for " + this.init_BlockPos.toShortString() + " (" + this.init_U_JsonUtil + ")")
                     : DataPipe.Inner_DataUtil.init;
               }

               this.sync = 0;
            }

            this.init_DataUtil.init_V_Vec3(this.init_U_CachePool.init_U_DataUtil().init_Vec3());
            this.init_DataUtil.load_V();
            if (!DataUtil.init_Z_BlockPos(this.init_U_CachePool.init_BlockHitResult().getBlockPos())) {
               return DataPipe.Inner_DataUtil.init;
            }

            BlockHitResult ray = DataUtil.init_BlockHitResult();
            BlockHitResult click = ray != null
                  && ray.getBlockPos().equals(this.init_U_CachePool.init_BlockHitResult().getBlockPos())
                  && ray.getDirection() == this.init_U_CachePool.init_BlockHitResult().getDirection()
               ? ray
               : this.init_U_CachePool.init_BlockHitResult();
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, click);
            Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
            this.init_Z = true;
            this.init_U_CachePool = null;
            return DataPipe.Inner_DataUtil.init;
         } else {
            if (!this.init_ItemStack.isEmpty() && !this.init_Z) {
               return this.init("Another block occupied the portable container's cell");
            }

            if (++this.open < 2 || this.init_Z && ca.init_J_BlockPos(this.init_BlockPos) <= this.init_J) {
               return ++this.close > 80 ? this.init("Placement was not confirmed by the server") : DataPipe.Inner_DataUtil.init;
            }

            if (this.init_Z && !this.init_Block.defaultBlockState().hasBlockEntity() && this.init_Block != Blocks.CRAFTING_TABLE) {
               FileCache.write_V(this.init_BlockPos);
            }

            return DataPipe.Inner_DataUtil.load;
         }
      }
   }

   public void init_V() {
      this.init_DataUtil.init_V();
      this.init_BlockPos = null;
   }

   private BlockPlacementPlanner.Inner_CachePool init(Minecraft mc) {
      Vec3 eye = mc.player.getEyePosition();
      double reach = mc.player.blockInteractionRange();
      BlockPlacementPlanner.Inner_StateCache request = new BlockPlacementPlanner.Inner_StateCache(
            this.init_BlockPos,
            eye,
            reach,
            BlockPlacementPlanner.init(this.init_ItemStack.isEmpty() ? new ItemStack(this.init_Item) : this.init_ItemStack.copy(), mc.player, InteractionHand.MAIN_HAND)
         )
         .init_U_StateCache_U_StreamUtil(new BlockPlacementPlanner.Inner_StreamUtil(48));
      BlockPlacementPlanner.Inner_JsonUtil[] refusal = new BlockPlacementPlanner.Inner_JsonUtil[1];
      BlockPlacementPlanner.Inner_CachePool best = BlockPlacementPlanner.init(request, refusal);
      if (best == null && refusal[0] != null) {
         this.init_U_JsonUtil = refusal[0];
      }

      return best;
   }

   static int init_I_Container_ItemStack(Container inventory, ItemStack wanted) {
      for (int slot = 0; slot < Math.min(36, inventory.getContainerSize()); slot++) {
         if (ItemStack.matches(inventory.getItem(slot), wanted)) {
            return slot;
         }
      }

      return inventory.getContainerSize() > 40 && ItemStack.matches(inventory.getItem(40), wanted) ? 40 : -1;
   }

   public String load_Str() {
      return (
            this.init_Z
               ? "clicked, no block yet"
               : (
                  this.init_U_CachePool == null
                     ? "no face (" + this.init_U_JsonUtil + ")"
                     : "turning to " + this.init_U_CachePool.init_BlockHitResult().getBlockPos().toShortString()
               )
         )
         + " after "
         + this.close
         + " ticks";
   }

   public static boolean init_Z_BlockPos_Item(BlockPos target, Item placeable) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null && target != null && placeable instanceof BlockItem) {
         BlockPlacementPlanner.Inner_StateCache request = new BlockPlacementPlanner.Inner_StateCache(
               target, mc.player.getEyePosition(), mc.player.blockInteractionRange(), BlockPlacementPlanner.init(new ItemStack(placeable), mc.player, InteractionHand.MAIN_HAND)
            )
            .init_U_StateCache_U_StreamUtil(new BlockPlacementPlanner.Inner_StreamUtil(1024));
         return BlockPlacementPlanner.init_U_CachePool(request) != null;
      } else {
         return false;
      }
   }

   private DataPipe.Inner_DataUtil init(String reason) {
      this.init_Str = reason;
      return DataPipe.Inner_DataUtil.read;
   }

   public enum Inner_DataUtil {
      init,
      load,
      read;
   }
}
