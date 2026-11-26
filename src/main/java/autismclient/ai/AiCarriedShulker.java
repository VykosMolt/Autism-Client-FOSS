package autismclient.ai;

import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class AiCarriedShulker implements AiOperation {
   static final int init_I = 27;
   static final int load_I = 2;
   private final Minecraft init_Minecraft = Minecraft.getInstance();
   private final Object init_Obj = this.init_Minecraft.level;
   private final Object load_Obj = this.init_Minecraft.player;
   private final Object read_Obj = this.init_Minecraft.getConnection();
   private final ItemStack init_ItemStack;
   private final Item init_Item;
   private final ItemStack load_ItemStack;
   private final int read_I;
   private final Set<Long> init_Set;
   private final autismclient.ai.engine.AiPlacer init_DataPipe = new autismclient.ai.engine.AiPlacer();
   private final AiPreparationReceipt init_bb = new AiPreparationReceipt();
   private final long init_J = System.nanoTime() + 120000000000L;
   private AiCarriedShulker.Inner_StreamUtil init_BitSetUtil_StreamU = AiCarriedShulker.Inner_StreamUtil.PLACE;
   private BlockPos init_BlockPos;
   private BlockState init_BlockState;
   private BlockState load_BlockState;
   private Object write_Obj;
   private AiOperation init_AiOperation;
   private ShulkerBoxMenu init_ShulkerBoxMenu;
   private CompletableFuture<Boolean> init_CompletableFuture;
   private List<ItemStack> init_List;
   private List<ItemStack> load_List;
   private List<ItemStack> read_List;
   private ItemStack read_ItemStack = ItemStack.EMPTY;
   private AiOperation.Result init_AiOperation_Result;
   private AiOperation.Result load_AiOperation_Result;
   private int write_I;
   private int update;
   private int sync;
   private int flush;
   private int close = -1;
   private int open;
   private int poll;
   private long load_J;
   private long read_J;
   private long write_J;
   private AiCarriedShulker.Inner_DataUtil init_BitSetUtil_DataUti;
   private boolean init_Z;
   private boolean load_Z;
   private boolean read_Z;
   private boolean write_Z;
   private int push = -1;

   static boolean init_Z_ItemStack(ItemStack stack) {
      return !stack.isEmpty() && stack.getCount() == 1 && stack.getItem() instanceof BlockItem block && block.getBlock() instanceof ShulkerBoxBlock;
   }

   static List<ItemStack> init_List_ItemStack(ItemStack box) {
      NonNullList<ItemStack> result = NonNullList.withSize(27, ItemStack.EMPTY);
      box.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(result);
      return result;
   }

   static int init_I_ItemStack_Item(ItemStack box, Item item) {
      if (!init_Z_ItemStack(box)) {
         return 0;
      }

      int count = 0;

      for (ItemStack stack : init_List_ItemStack(box)) {
         if (stack.is(item)) {
            count += stack.getCount();
         }
      }

      return count;
   }

   static int init_I_ItemStack_ItemStack(ItemStack box, ItemStack wanted) {
      if (init_Z_ItemStack(box) && !wanted.isEmpty()) {
         int count = 0;

         for (ItemStack stack : init_List_ItemStack(box)) {
            if (ItemStack.isSameItemSameComponents(stack, wanted)) {
               count += stack.getCount();
            }
         }

         return count;
      } else {
         return 0;
      }
   }

   static ItemStack init(Container inventory, Item item, List<ItemStack> tried) {
      return init(inventory, item, tried, false);
   }

   static ItemStack init(Container inventory, Item item, List<ItemStack> tried, boolean requireCapacity) {
      ItemStack best = ItemStack.EMPTY;
      int bestCount = 0;

      for (int slot = 0; slot < Math.min(41, inventory.getContainerSize()); slot++) {
         if (slot < 36 || slot == 40) {
            ItemStack stack = inventory.getItem(slot);
            if (!tried.stream().anyMatch(previous -> ItemStack.matches(previous, stack))) {
               Item supplied = requireCapacity ? init(inventory, stack, item) : item;
               if (supplied != null) {
                  int count = init_I_ItemStack_Item(stack, supplied);
                  if (requireCapacity && count > 0) {
                     count = init(inventory, stack, supplied, count, true);
                  }

                  if (count > bestCount) {
                     best = stack;
                     bestCount = count;
                  }
               }
            }
         }
      }

      return best.copy();
   }

   static Item init(Container inventory, ItemStack box, Item wanted) {
      if (!init_Z_ItemStack(box)) {
         return null;
      }

      if (init_I_ItemStack_Item(box, wanted) > 0 && init(inventory, box, wanted, 1, true) > 0) {
         return wanted;
      }

      if (!Compat263.isHoe(wanted) && !Compat263.isShovel(wanted)) {
         return null;
      }

      for (ItemStack stored : init_List_ItemStack(box)) {
         if (autismclient.ai.print.PrintCore.init_Z_ItemStack_Item(stored, wanted) && init(inventory, box, stored.getItem(), 1, true) > 0) {
            return stored.getItem();
         }
      }

      return null;
   }

   static Map<Item, Integer> init_Map_Container_Z(Container inventory, boolean includeBoxes) {
      HashMap<Item, Integer> counts = new HashMap<>();

      for (int slot = 0; slot < Math.min(41, inventory.getContainerSize()); slot++) {
         if (slot < 36 || slot == 40) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty()) {
               counts.merge(stack.getItem(), stack.getCount(), Integer::sum);
               if (includeBoxes && init_Z_ItemStack(stack)) {
                  for (ItemStack stored : init_List_ItemStack(stack)) {
                     if (!stored.isEmpty()) {
                        counts.merge(stored.getItem(), stored.getCount(), Integer::sum);
                     }
                  }
               }
            }
         }
      }

      return counts;
   }

   static ItemStack init(Container inventory, ItemStack wanted, List<ItemStack> tried) {
      ItemStack best = ItemStack.EMPTY;
      int bestCount = 0;

      for (int slot = 0; slot < Math.min(41, inventory.getContainerSize()); slot++) {
         if (slot < 36 || slot == 40) {
            ItemStack stack = inventory.getItem(slot);
            if (!tried.stream().anyMatch(previous -> ItemStack.matches(previous, stack))) {
               int count = init_I_ItemStack_ItemStack(stack, wanted);
               if (count > bestCount) {
                  best = stack;
                  bestCount = count;
               }
            }
         }
      }

      return best.copy();
   }

   static int init(Container inventory, ItemStack box, Item item, int wanted, boolean beforePlacement) {
      return init(inventory, box, stack -> stack.is(item), wanted, beforePlacement);
   }

   static int init(Container inventory, ItemStack box, ItemStack item, int wanted, boolean beforePlacement) {
      return init(inventory, box, stack -> ItemStack.isSameItemSameComponents(stack, item), wanted, beforePlacement);
   }

   private static int init(Container inventory, ItemStack box, Predicate<ItemStack> matches, int wanted, boolean beforePlacement) {
      if (wanted > 0 && init_Z_ItemStack(box) && inventory.getContainerSize() >= 36) {
         ArrayList<ItemStack> slots = new ArrayList<>(36);

         for (int i = 0; i < 36; i++) {
            slots.add(inventory.getItem(i).copy());
         }

         if (beforePlacement) {
            int source = -1;
            int i = 0;

            while (true) {
               if (i < 36) {
                  if (!ItemStack.matches(slots.get(i), box)) {
                     i++;
                     continue;
                  }

                  source = i;
               }

               if (source >= 0) {
                  slots.set(source, ItemStack.EMPTY);
               } else {
                  if (inventory.getContainerSize() <= 40 || !ItemStack.matches(inventory.getItem(40), box)) {
                     return 0;
                  }

                  i = inventory instanceof Inventory playerInventory ? playerInventory.getSelectedSlot() : 0;

                  for (int ix = 0; ix < 9; ix++) {
                     if (slots.get(ix).isEmpty()) {
                        i = ix;
                        break;
                     }
                  }

                  slots.set(i, ItemStack.EMPTY);
               }
               break;
            }
         }

         int reserved = -1;

         for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i).isEmpty()) {
               reserved = i;
               break;
            }
         }

         if (reserved < 0) {
            return 0;
         }

         int accepted = 0;
         List<ItemStack> stored = init_List_ItemStack(box);

         for (int sourcePass = 0; sourcePass < 2; sourcePass++) {
            for (ItemStack stack : stored) {
               if (matches.test(stack) && AiItems.init_Z(stack) == (sourcePass == 0)) {
                  int left = Math.min(stack.getCount(), wanted - accepted);

                  for (int pass = 0; pass < 2 && left > 0; pass++) {
                     for (int i = 0; i < slots.size() && left > 0; i++) {
                        if (i != reserved) {
                           ItemStack current = slots.get(i);
                           if (pass == 0 ? !current.isEmpty() && ItemStack.isSameItemSameComponents(current, stack) : current.isEmpty()) {
                              int room = Math.min(stack.getMaxStackSize(), inventory.getMaxStackSize()) - current.getCount();
                              int amount = Math.min(left, Math.max(0, room));
                              if (amount != 0) {
                                 slots.set(i, stack.copyWithCount(current.getCount() + amount));
                                 accepted += amount;
                                 left -= amount;
                              }
                           }
                        }
                     }
                  }

                  if (accepted >= wanted || left > 0) {
                     return accepted;
                  }
               }
            }
         }

         return accepted;
      } else {
         return 0;
      }
   }

   static ItemStack init_ItemStack_ItemStack_List(ItemStack original, List<ItemStack> remaining) {
      ItemStack result = original.copyWithCount(1);
      result.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(remaining));
      return result;
   }

   static boolean init(List<ItemStack> before, List<ItemStack> after, Item item, int taken) {
      return init(before, after, stack -> stack.is(item), taken);
   }

   static boolean init(List<ItemStack> before, List<ItemStack> after, ItemStack item, int taken) {
      return init(before, after, stack -> ItemStack.isSameItemSameComponents(stack, item), taken);
   }

   private static boolean init(List<ItemStack> before, List<ItemStack> after, Predicate<ItemStack> matches, int taken) {
      if (before.size() == 27 && after.size() == 27 && taken >= 0) {
         int delta = 0;

         for (int i = 0; i < 27; i++) {
            ItemStack old = before.get(i);
            ItemStack now = after.get(i);
            if (!matches.test(old)) {
               if (!ItemStack.matches(old, now)) {
                  return false;
               }
            } else {
               if (!now.isEmpty() && !ItemStack.isSameItemSameComponents(old, now) || now.getCount() > old.getCount()) {
                  return false;
               }

               delta += old.getCount() - now.getCount();
            }
         }

         return delta == taken;
      } else {
         return false;
      }
   }

   static List<ItemStack> init_List_Container(Container inventory) {
      ArrayList<ItemStack> contents = new ArrayList<>(inventory.getContainerSize());

      for (int i = 0; i < inventory.getContainerSize(); i++) {
         contents.add(inventory.getItem(i).copy());
      }

      return contents;
   }

   static boolean init(List<ItemStack> before, List<ItemStack> after, List<ItemStack> inventoryBefore, List<ItemStack> inventoryAfter, Item item, int taken) {
      return init(before, after, inventoryBefore, inventoryAfter, stack -> stack.is(item), taken);
   }

   static boolean init(
      List<ItemStack> before, List<ItemStack> after, List<ItemStack> inventoryBefore, List<ItemStack> inventoryAfter, ItemStack item, int taken
   ) {
      return init(before, after, inventoryBefore, inventoryAfter, stack -> ItemStack.isSameItemSameComponents(stack, item), taken);
   }

   private static boolean init(
      List<ItemStack> before, List<ItemStack> after, List<ItemStack> inventoryBefore, List<ItemStack> inventoryAfter, Predicate<ItemStack> matches, int taken
   ) {
      if (init(before, after, matches, taken) && inventoryBefore.size() >= 36 && inventoryBefore.size() == inventoryAfter.size()) {
         ArrayList<ItemStack> gains = new ArrayList<>();

         for (int i = 0; i < inventoryBefore.size(); i++) {
            ItemStack old = inventoryBefore.get(i);
            ItemStack now = inventoryAfter.get(i);
            if (!ItemStack.matches(old, now)) {
               if (i >= 36
                  || now.isEmpty()
                  || !matches.test(now)
                  || !old.isEmpty() && !ItemStack.isSameItemSameComponents(old, now)
                  || now.getCount() <= old.getCount()) {
                  return false;
               }

               gains.add(now.copyWithCount(now.getCount() - old.getCount()));
            }
         }

         for (int i = 0; i < 27; i++) {
            ItemStack old = before.get(i);
            int left = old.getCount() - after.get(i).getCount();

            for (ItemStack gain : gains) {
               if (left > 0 && ItemStack.isSameItemSameComponents(old, gain)) {
                  int matched = Math.min(left, gain.getCount());
                  gain.shrink(matched);
                  left -= matched;
               }
            }

            if (left != 0) {
               return false;
            }
         }

         return gains.stream().allMatch(ItemStack::isEmpty);
      } else {
         return false;
      }
   }

   static boolean init(List<ItemStack> before, List<ItemStack> after, ItemStack returning) {
      if (init_Z_ItemStack(returning) && before.size() == after.size()) {
         ArrayList<ItemStack> expected = new ArrayList<>();

         for (int i = 0; i < before.size(); i++) {
            if ((i < 36 || i == 40) && init_Z_ItemStack(before.get(i))) {
               expected.add(before.get(i));
            }
         }

         expected.add(returning);

         for (int i = 0; i < after.size(); i++) {
            if ((i < 36 || i == 40) && init_Z_ItemStack(after.get(i))) {
               ItemStack current = after.get(i);
               int match = -1;

               for (int j = 0; j < expected.size(); j++) {
                  if (ItemStack.matches(expected.get(j), current)) {
                     match = j;
                     break;
                  }
               }

               if (match < 0) {
                  return false;
               }

               expected.remove(match);
            }
         }

         return expected.isEmpty();
      } else {
         return false;
      }
   }

   static boolean init(BlockGetter level, Predicate<BlockPos> loaded, BlockPos at, AABB body, Set<Long> protectedCells) {
      return body.intersects(new AABB(at).expandTowards(0.0, 0.5, 0.0)) ? false : init(level, loaded, at, body, protectedCells, false);
   }

   static boolean init(BlockGetter level, Predicate<BlockPos> loaded, BlockPos at, AABB body, Set<Long> protectedCells, boolean ownedBoxPresent) {
      if (!(Math.abs(body.minY - at.getY()) > 0.05)
         && !(body.minX < at.getX() - 2)
         && !(body.maxX > at.getX() + 2 + 1)
         && !(body.minZ < at.getZ() - 2)
         && !(body.maxZ > at.getZ() + 2 + 1)
         && (!ownedBoxPresent || !body.intersects(new AABB(at).expandTowards(0.0, 0.5, 0.0)))) {
         for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
               if (level instanceof ClientLevel client && autismclient.ai.engine.AiEnvironmentHazards.init(client, at.offset(dx, 0, dz))) {
                  return false;
               }

               for (int dy = 0; dy <= 2; dy++) {
                  BlockPos p = at.offset(dx, dy, dz);
                  if (!loaded.test(p) || protectedCells.contains(p.asLong())) {
                     return false;
                  }

                  BlockState state = level.getBlockState(p);
                  if (ownedBoxPresent && p.equals(at)) {
                     if (!(state.getBlock() instanceof ShulkerBoxBlock) || state.getValue(ShulkerBoxBlock.FACING) != Direction.UP) {
                        return false;
                     }
                  } else if (!state.isAir() || !state.getFluidState().isEmpty()) {
                     return false;
                  }
               }
            }
         }

         return init(level, loaded, at, protectedCells);
      } else {
         return false;
      }
   }

   static boolean init(BlockGetter level, Predicate<BlockPos> loaded, BlockPos at, Set<Long> protectedCells) {
      for (int dx = -2; dx <= 2; dx++) {
         int dz = -2;

         while (dz <= 2) {
            BlockPos below = at.offset(dx, -1, dz);
            if (loaded.test(below) && !protectedCells.contains(below.asLong())) {
               BlockState floor = level.getBlockState(below);
               if (floor.getFluidState().isEmpty()
                  && !floor.hasBlockEntity()
                  && !floor.isSignalSource()
                  && !floor.is(BlockTags.LEAVES)
                  && !autismclient.ai.engine.AiPathfinder.Inner_StreamUtil.read(floor)
                  && floor.getBlock().getBounceRestitution() == 0.0F
                  && floor.getBlock().getFriction() > 0.0F
                  && floor.getBlock().getFriction() <= 0.6F
                  && !(floor.getBlock().getSpeedFactor() > 1.0F)
                  && !(floor.getBlock().getJumpFactor() > 1.0F)
                  && !(floor.getBlock() instanceof FallingBlock)
                  && !(floor.getBlock() instanceof TntBlock)
                  && !(floor.getBlock() instanceof PistonBaseBlock)
                  && !(floor.getBlock() instanceof ObserverBlock)
                  && floor.isCollisionShapeFullBlock(level, below)) {
                  dz++;
                  continue;
               }

               return false;
            }

            return false;
         }
      }

      return true;
   }

   static BlockPos init_BlockPos_BlockPos_Vec3(BlockPos box, Vec3 drop) {
      return box.offset(Math.clamp(Mth.floor(drop.x) - box.getX(), -2, 2), 0, Math.clamp(Mth.floor(drop.z) - box.getZ(), -2, 2));
   }

   static autismclient.ai.engine.AiPathfinder.Inner_JsonUtil init_ClassCache_JsonUti_BlockPos_BlockPos(final BlockPos box, BlockPos target) {
      final autismclient.ai.engine.AiPathfinder.Inner_JsonUtil near = autismclient.ai.engine.AiPathfinder.Inner_JsonUtil.init_ClassCache_JsonUti_BlockPos_D(target, 0.6);
      final Set<autismclient.ai.engine.AiPathfinder.Inner_SyncManager> walks = Set.copyOf(
         EnumSet.complementOf(EnumSet.of(autismclient.ai.engine.AiPathfinder.Inner_SyncManager.WALK, autismclient.ai.engine.AiPathfinder.Inner_SyncManager.WALK_DIAGONAL))
      );
      return new autismclient.ai.engine.AiPathfinder.Inner_JsonUtil() {
         @Override
         public boolean isInGoal(int x, int y, int z) {
            return near.isInGoal(x, y, z);
         }

         @Override
         public double heuristic(int x, int y, int z) {
            return near.heuristic(x, y, z);
         }

         @Override
         public boolean init_Z(BlockPos cell) {
            return cell.getY() == box.getY() && Math.abs(cell.getX() - box.getX()) <= 2 && Math.abs(cell.getZ() - box.getZ()) <= 2;
         }

         @Override
         public Set<autismclient.ai.engine.AiPathfinder.Inner_SyncManager> init_Set() {
            return walks;
         }
      };
   }

   static boolean init(Container inventory, int slot, ItemStack box) {
      return slot >= 0
         && slot < Math.min(36, inventory.getContainerSize())
         && !box.isEmpty()
         && inventory.getItem(slot).isEmpty()
         && inventory.canPlaceItem(slot, box)
         && box.getCount() <= Math.min(box.getMaxStackSize(), inventory.getMaxStackSize());
   }

   AiCarriedShulker(ItemStack box, Item item, int wanted, Set<Long> protectedCells) {
      this(box, item, ItemStack.EMPTY, wanted, protectedCells);
   }

   AiCarriedShulker(ItemStack box, ItemStack item, int wanted, Set<Long> protectedCells) {
      this(box, item.getItem(), item, wanted, protectedCells);
      if (item.isEmpty()) {
         throw new IllegalArgumentException("Portable saved contents require a nonempty item stack");
      }
   }

   private AiCarriedShulker(ItemStack box, Item item, ItemStack exactMaterial, int wanted, Set<Long> protectedCells) {
      this.init_ItemStack = box.copy();
      this.init_Item = item;
      this.read_I = Math.max(1, wanted);
      this.init_Set = protectedCells;
      this.load_ItemStack = exactMaterial.copyWithCount(1);
      if (!init_Z_ItemStack(box) || (exactMaterial.isEmpty() ? init_I_ItemStack_Item(box, item) : init_I_ItemStack_ItemStack(box, exactMaterial)) == 0) {
         throw new IllegalArgumentException("The selected shulker has no requested stock");
      }
   }

   private int init_I_Z(boolean beforePlacement) {
      return this.load_ItemStack.isEmpty()
         ? init(this.init_Minecraft.player.getInventory(), this.init_ItemStack, this.init_Item, this.read_I, beforePlacement)
         : init(this.init_Minecraft.player.getInventory(), this.init_ItemStack, this.load_ItemStack, this.read_I, beforePlacement);
   }

   private int init_I() {
      return this.load_ItemStack.isEmpty()
         ? AiPrintSupply.init(this.init_Minecraft.player.getInventory(), this.init_Item)
         : AiInventoryController.init_I_Container_ItemStack_2(this.init_Minecraft.player.getInventory(), this.load_ItemStack);
   }

   @Override
   public AiOperation.Result tick() {
      if (this.init_AiOperation_Result != null) {
         return this.init_AiOperation_Result;
      }

      if (this.read_Z) {
         return this.init_AiOperation_Result_AiOperation_Status_Str(AiOperation.Status.CANCELLED, "Portable stock was cancelled");
      }

      if (this.init_Minecraft.level != this.init_Obj
         || this.init_Minecraft.player != this.load_Obj
         || this.init_Minecraft.getConnection() != this.read_Obj
         || this.init_Minecraft.player == null
         || !this.init_Minecraft.player.isAlive()) {
         return this.init_AiOperation_Result_AiOperation_Status_Str(AiOperation.Status.UNCERTAIN, "Portable stock context changed");
      }

      if (this.init_Minecraft.player.hasInfiniteMaterials()) {
         return this.init_AiOperation_Result_AiOperation_Status_Str(AiOperation.Status.UNCERTAIN, "Game mode changed before the carried shulker was recovered");
      }

      if (System.nanoTime() >= this.init_J) {
         return this.init_AiOperation_Result_AiOperation_Status_Str(AiOperation.Status.TIMED_OUT, "Portable stock recovery deadline reached");
      }

      if (this.init_BitSetUtil_StreamU == AiCarriedShulker.Inner_StreamUtil.APPROACH
         || this.init_BitSetUtil_StreamU == AiCarriedShulker.Inner_StreamUtil.PLACE
         || this.init_BitSetUtil_StreamU == AiCarriedShulker.Inner_StreamUtil.CONFIRM_REMOVAL
         || this.init_BitSetUtil_StreamU == AiCarriedShulker.Inner_StreamUtil.PICKUP
         || this.init_Z()
         || this.init_BitSetUtil_StreamU == AiCarriedShulker.Inner_StreamUtil.BREAK
            && this.init_AiOperation != null
            && this.init_Minecraft.level.getBlockState(this.init_BlockPos).isAir()) {
         if (this.init_BitSetUtil_StreamU == AiCarriedShulker.Inner_StreamUtil.APPROACH) {
            AiOperation.Result result = this.init_bb.init_AiOperation_Result(this.init_AiOperation);
            if (!result.terminal()) {
               return this.init_AiOperation_Result_Str("Moving to a safe existing shulker recovery area");
            }

            this.init_bb.init_V(this.init_AiOperation);
            this.init_AiOperation = null;
            if (result.status() != AiOperation.Status.SUCCEEDED) {
               return this.init_AiOperation_Result_AiOperation_Status_Str(result.status(), "Could not reach a safe shulker recovery area");
            }

            this.init_BitSetUtil_StreamU = AiCarriedShulker.Inner_StreamUtil.PLACE;
            return this.init_AiOperation_Result_Str("Rechecking the shulker recovery area after arrival");
         } else {
            if (this.init_BitSetUtil_StreamU == AiCarriedShulker.Inner_StreamUtil.PLACE) {
               return this.init_AiOperation_Result_2();
            }

            if (this.init_BitSetUtil_StreamU == AiCarriedShulker.Inner_StreamUtil.OPEN) {
               return this.load_AiOperation_Result_2();
            }

            if (this.init_BitSetUtil_StreamU == AiCarriedShulker.Inner_StreamUtil.WITHDRAW) {
               return this.read_AiOperation_Result();
            }

            if (this.init_BitSetUtil_StreamU == AiCarriedShulker.Inner_StreamUtil.CLOSE) {
               if (this.init_Minecraft.player.containerMenu != this.init_Minecraft.player.inventoryMenu) {
                  return this.init_AiOperation_Result_AiOperation_Status_Str(AiOperation.Status.UNCERTAIN, "Another menu opened during shulker recovery");
               }

               if (++this.open < 10 || this.write_Obj instanceof ShulkerBoxBlockEntity shulker && !shulker.isClosed()) {
                  return this.init_AiOperation_Result_Str("Waiting for the shulker lid to close");
               }

               if (!this.read_Z()) {
                  return this.init_AiOperation_Result_AiOperation_Status_Str(
                     AiOperation.Status.UNCERTAIN, "The stock box recovery area or reserved pickup capacity changed; box left placed"
                  );
               }

               this.sync = init_I_Container_ItemStack(this.init_Minecraft.player.getInventory(), this.read_ItemStack);
               this.read_List = init_List_Container(this.init_Minecraft.player.getInventory());
               this.load_J = AiServerEvidence.init_J();
               this.read_J = AiServerEvidence.init_J_BlockPos(this.init_BlockPos);
               this.init_AiOperation = autismclient.ai.engine.AiBreakBlock.init(this.init_BlockPos, 25, true, this.init_Set, false);
               this.init_BitSetUtil_StreamU = AiCarriedShulker.Inner_StreamUtil.BREAK;
               return this.init_AiOperation_Result_Str("Recovering the owned stock shulker");
            } else if (this.init_BitSetUtil_StreamU == AiCarriedShulker.Inner_StreamUtil.BREAK) {
               if (this.init_Z() && !this.read_Z()) {
                  return this.init_AiOperation_Result_AiOperation_Status_Str(
                     AiOperation.Status.UNCERTAIN, "Stock recovery is no longer safe; further breaking stopped"
                  );
               }

               AiOperation.Result result = this.init_bb.init_AiOperation_Result(this.init_AiOperation);
               if (!result.terminal()) {
                  return this.init_AiOperation_Result_Str(result.message());
               }

               this.init_bb.init_V(this.init_AiOperation);
               this.init_AiOperation = null;
               if (result.status() != AiOperation.Status.SUCCEEDED) {
                  return this.init_AiOperation_Result_AiOperation_Status_Str(
                     AiOperation.Status.UNCERTAIN, "The stock shulker break did not finish: " + result.message()
                  );
               }

               this.init_BitSetUtil_DataUti = new AiCarriedShulker.Inner_DataUtil(this.read_J, System.nanoTime());
               this.init_BitSetUtil_StreamU = AiCarriedShulker.Inner_StreamUtil.CONFIRM_REMOVAL;
               return this.init_AiOperation_Result_Str("Waiting for the server to confirm the stock shulker removal");
            } else if (this.init_BitSetUtil_StreamU == AiCarriedShulker.Inner_StreamUtil.CONFIRM_REMOVAL) {
               AiCarriedShulker.Inner_DataUtil.Inner_DataUtil_2 observed = this.init_BitSetUtil_DataUti
                  .init(AiTerrain.init(this.init_Minecraft.level, this.init_BlockPos), AiServerEvidence.init_ca_DataUtil_BlockPos(this.init_BlockPos), System.nanoTime());
               if (observed == AiCarriedShulker.Inner_DataUtil.Inner_DataUtil_2.WAITING) {
                  return this.init_AiOperation_Result_Str("Waiting for the server to confirm the stock shulker removal");
               }

               if (observed != AiCarriedShulker.Inner_DataUtil.Inner_DataUtil_2.CONFIRMED) {
                  return this.init_AiOperation_Result_AiOperation_Status_Str(
                     AiOperation.Status.UNCERTAIN,
                     observed == AiCarriedShulker.Inner_DataUtil.Inner_DataUtil_2.REJECTED
                        ? "The server did not confirm an empty stock shulker cell; inspect the placed box"
                        : "The stock shulker removal confirmation did not arrive"
                  );
               }

               this.init_BitSetUtil_StreamU = AiCarriedShulker.Inner_StreamUtil.PICKUP;
               return this.init_AiOperation_Result_Str("Locating the observed stock shulker drop");
            } else {
               if (!init(
                  this.init_Minecraft.level,
                  p -> AiTerrain.init(this.init_Minecraft.level, p),
                  this.init_BlockPos,
                  this.init_Minecraft.player.getBoundingBox(),
                  this.init_Set,
                  false
               )) {
                  return this.init_AiOperation_Result_AiOperation_Status_Str(
                     AiOperation.Status.UNCERTAIN, "The shulker pickup area changed; no unsafe recovery route will be attempted"
                  );
               }

               if (this.init_AiOperation != null) {
                  AiOperation.Result result = this.init_bb.init_AiOperation_Result(this.init_AiOperation);
                  if (!result.terminal()) {
                     return this.init_AiOperation_Result_Str(result.message());
                  }

                  this.init_bb.init_V(this.init_AiOperation);
                  this.init_AiOperation = null;
                  if (result.status() != AiOperation.Status.SUCCEEDED) {
                     return this.init_AiOperation_Result_AiOperation_Status_Str(AiOperation.Status.UNCERTAIN, "Could not reach the dropped stock shulker");
                  }
               }

               if (AiServerEvidence.init_J() > this.load_J
                  && init_I_Container_ItemStack(this.init_Minecraft.player.getInventory(), this.read_ItemStack) == this.sync + 1
                  && this.init_Minecraft.player.onGround()
                  && this.init_Minecraft.player.getDeltaMovement().horizontalDistanceSqr() <= 4.0E-4) {
                  if (!init(this.read_List, init_List_Container(this.init_Minecraft.player.getInventory()), this.read_ItemStack)) {
                     return this.init_AiOperation_Result_AiOperation_Status_Str(
                        AiOperation.Status.UNCERTAIN, "The recovered box or the other carried shulkers changed during pickup"
                     );
                  }

                  this.load_Z = true;
                  return this.init_AiOperation_Result_AiOperation_Status_Str(
                     this.update > 0 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED,
                     this.update > 0 ? "Material withdrawn and stock shulker recovered" : "Stock shulker recovered without a material transfer"
                  );
               } else {
                  ItemEntity dropped = null;

                  for (ItemEntity candidate : this.init_Minecraft.level.getEntitiesOfClass(ItemEntity.class, init_AABB_BlockPos(this.init_BlockPos))) {
                     if (ItemStack.matches(candidate.getItem(), this.read_ItemStack)
                        && !candidate.isRemoved()
                        && (this.push < 0 || candidate.getId() == this.push)) {
                        if (dropped != null) {
                           return this.init_AiOperation_Result_AiOperation_Status_Str(
                              AiOperation.Status.UNCERTAIN, "More than one indistinguishable stock drop appeared; inspect recovery"
                           );
                        }

                        dropped = candidate;
                     }
                  }

                  if (dropped == null) {
                     return this.init_AiOperation_Result_Str("Waiting for the stock shulker entity or its inventory receipt");
                  }

                  this.push = dropped.getId();
                  AABB dropBounds = dropped.getBoundingBox();
                  if (dropBounds.minX < this.init_BlockPos.getX() - 2
                     || dropBounds.maxX > this.init_BlockPos.getX() + 2 + 1
                     || dropBounds.minZ < this.init_BlockPos.getZ() - 2
                     || dropBounds.maxZ > this.init_BlockPos.getZ() + 2 + 1) {
                     return this.init_AiOperation_Result_AiOperation_Status_Str(
                        AiOperation.Status.UNCERTAIN, "The stock drop moved beyond the proved landing floor; no edge chase will be attempted"
                     );
                  }

                  if (this.init_Minecraft.player.getBoundingBox().inflate(1.0, 0.5, 1.0).intersects(dropped.getBoundingBox())) {
                     return this.init_AiOperation_Result_Str("Waiting for the nearby stock shulker pickup receipt");
                  }

                  BlockPos target = init_BlockPos_BlockPos_Vec3(this.init_BlockPos, dropped.position());
                  this.init_AiOperation = AiGoto.init(
                     target,
                     0.6,
                     false,
                     false,
                     false,
                     25,
                     autismclient.ai.engine.AiPathfinder.Inner_IoWorker.SAFEST,
                     false,
                     2.0,
                     init_ClassCache_JsonUti_BlockPos_BlockPos(this.init_BlockPos, target),
                     new AiGoto.Inner_BufferHelper(false, false, 0, true, false, false)
                  );
                  return this.init_AiOperation_Result_Str("Walking inside the safe area to the observed stock shulker");
               }
            }
         }
      } else {
         return this.init_AiOperation_Result_AiOperation_Status_Str(AiOperation.Status.UNCERTAIN, "The placed stock box changed before recovery");
      }
   }

   private AiOperation.Result init_AiOperation_Result_2() {
      if (!this.write_Z) {
         if (!this.init_Minecraft.player.onGround()) {
            return this.init_AiOperation_Result_AiOperation_Status_Str(AiOperation.Status.FAILED, "A supported stance is needed for portable stock");
         }

         if (this.init_Minecraft.player.containerMenu != this.init_Minecraft.player.inventoryMenu
            || !this.init_Minecraft.player.inventoryMenu.getCarried().isEmpty()) {
            return this.init_AiOperation_Result_AiOperation_Status_Str(AiOperation.Status.FAILED, "Close the menu and clear the cursor before portable stock");
         }

         if (this.init_I_Z(true) == 0) {
            return this.init_AiOperation_Result_AiOperation_Status_Str(
               AiOperation.Status.FAILED, "Free one inventory slot for material while keeping room to recover the shulker"
            );
         }

         BlockPos feet = this.init_Minecraft.player.blockPosition();
         this.init_BlockPos = init_BlockPos_Minecraft_Set(this.init_Minecraft, this.init_Set);
         if (this.init_BlockPos == null) {
            for (int radius = 3; radius <= 8; radius++) {
               for (Direction side : Plane.HORIZONTAL) {
                  BlockPos candidate = feet.relative(side, radius);
                  BlockPos stance = candidate.relative(side.getOpposite());
                  AABB body = this.init_Minecraft.player.getBoundingBox().move(Vec3.atBottomCenterOf(stance).subtract(this.init_Minecraft.player.position()));
                  if (init(this.init_Minecraft.level, p -> AiTerrain.init(this.init_Minecraft.level, p), candidate, body, this.init_Set)
                     && this.init_Z_BlockPos(candidate)) {
                     this.init_BlockPos = candidate.immutable();
                     this.init_AiOperation = AiGoto.init(
                        stance,
                        0.6,
                        false,
                        false,
                        false,
                        25,
                        autismclient.ai.engine.AiPathfinder.Inner_IoWorker.SAFEST,
                        false,
                        2.0,
                        AiGoto.read(autismclient.ai.engine.AiPathfinder.Inner_JsonUtil.init_ClassCache_JsonUti_BlockPos_D(stance, 0.6), this.init_Set),
                        new AiGoto.Inner_BufferHelper(false, false, 0, true, false, false)
                     );
                     this.init_BitSetUtil_StreamU = AiCarriedShulker.Inner_StreamUtil.APPROACH;
                     return this.init_AiOperation_Result_Str("Moving to an existing flat area before placing the stock shulker");
                  }
               }
            }
         }

         if (this.init_BlockPos == null) {
            return this.init_AiOperation_Result_AiOperation_Status_Str(
               AiOperation.Status.FAILED, "No clear flat shulker recovery area nearby; stock box kept in inventory"
            );
         }

         if (!init(
               this.init_Minecraft.level,
               p -> AiTerrain.init(this.init_Minecraft.level, p),
               this.init_BlockPos,
               this.init_Minecraft.player.getBoundingBox(),
               this.init_Set
            )
            || !this.init_Z_BlockPos(this.init_BlockPos)) {
            return this.init_AiOperation_Result_AiOperation_Status_Str(AiOperation.Status.FAILED, "The selected shulker recovery area is no longer clear");
         }

         this.load_BlockState = this.init_Minecraft.level.getBlockState(this.init_BlockPos.below());
         this.flush = init_I_Container_ItemStack(this.init_Minecraft.player.getInventory(), this.init_ItemStack);
         this.init_DataPipe.init_V_BlockPos_ItemStack(this.init_BlockPos, this.init_ItemStack);
         this.write_Z = true;
      }

      if (this.init_DataPipe.init_Z()
         || init(
               this.init_Minecraft.level,
               p -> AiTerrain.init(this.init_Minecraft.level, p),
               this.init_BlockPos,
               this.init_Minecraft.player.getBoundingBox(),
               this.init_Set
            )
            && this.init_Z_BlockPos(this.init_BlockPos)) {
         autismclient.ai.engine.AiPlacer.Inner_DataUtil placed = this.init_DataPipe.init_DataPipe_DataUtil();
         this.init_Z = this.init_Z | this.init_DataPipe.init_Z();
         if (placed == autismclient.ai.engine.AiPlacer.Inner_DataUtil.FAILED) {
            return this.init_AiOperation_Result_AiOperation_Status_Str(
               this.init_Z ? AiOperation.Status.UNCERTAIN : AiOperation.Status.FAILED, this.init_DataPipe.init_Str()
            );
         } else if (placed != autismclient.ai.engine.AiPlacer.Inner_DataUtil.PLACED) {
            return this.init_AiOperation_Result_Str("Placing the selected stock shulker");
         } else {
            this.init_BlockState = this.init_Minecraft.level.getBlockState(this.init_BlockPos);
            this.write_Obj = this.init_Minecraft.level.getBlockEntity(this.init_BlockPos);
            if (this.write_Obj != null && this.init_BlockState.getValue(ShulkerBoxBlock.FACING) == Direction.UP) {
               this.init_DataPipe.init_V();
               this.write_J = AiServerEvidence.init_J();
               this.init_AiOperation = AiContainer.init(
                  AiJson.init_JsonObject_ObjArr(
                     "action",
                     "inspect",
                     "x",
                     this.init_BlockPos.getX(),
                     "y",
                     this.init_BlockPos.getY(),
                     "z",
                     this.init_BlockPos.getZ(),
                     "navigate",
                     false,
                     "timeoutSeconds",
                     20
                  ),
                  this.init_Set
               );
               this.init_BitSetUtil_StreamU = AiCarriedShulker.Inner_StreamUtil.OPEN;
               return this.init_AiOperation_Result_Str("Opening the placed stock shulker");
            } else {
               return this.init_AiOperation_Result_AiOperation_Status_Str(
                  AiOperation.Status.UNCERTAIN, "The stock shulker did not retain its verified upward opening"
               );
            }
         }
      } else {
         return this.init_AiOperation_Result_AiOperation_Status_Str(AiOperation.Status.FAILED, "The selected portable stock site changed");
      }
   }

   static BlockPos init_BlockPos_Minecraft_Set(Minecraft mc, Set<Long> protectedCells) {
      if (mc.level != null && mc.player != null && mc.player.onGround() && !mc.player.getAbilities().flying) {
         BlockPos feet = mc.player.blockPosition();
         AABB body = mc.player.getBoundingBox();

         for (int radius = 1; radius <= 2; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
               for (int dz = -radius; dz <= radius; dz++) {
                  if (Math.max(Math.abs(dx), Math.abs(dz)) == radius) {
                     BlockPos at = feet.offset(dx, 0, dz);
                     if (init(mc.level, p -> AiTerrain.init(mc.level, p), at, body, protectedCells)
                        && mc.level.getEntitiesOfClass(ItemEntity.class, init_AABB_BlockPos(at).inflate(1.0)).isEmpty()) {
                        return at;
                     }
                  }
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private AiOperation.Result load_AiOperation_Result_2() {
      if (this.init_AiOperation != null) {
         AiOperation.Result result = this.init_bb.init_AiOperation_Result(this.init_AiOperation);
         if (!result.terminal()) {
            return this.init_AiOperation_Result_Str(result.message());
         }

         this.init_bb.init_V(this.init_AiOperation);
         this.init_AiOperation = null;
         if (result.status() != AiOperation.Status.SUCCEEDED || !(this.init_Minecraft.player.containerMenu instanceof ShulkerBoxMenu opened)) {
            return this.init_AiOperation_Result_AiOperation_Status_Str(AiOperation.Status.UNCERTAIN, "The stock shulker menu was not confirmed");
         }

         this.init_ShulkerBoxMenu = opened;
         IntegratedServer server = this.init_Minecraft.getSingleplayerServer();
         if (server != null) {
            UUID uuid = this.init_Minecraft.player.getUUID();
            ResourceKey<Level> dimension = this.init_Minecraft.level.dimension();
            int id = this.init_ShulkerBoxMenu.containerId;
            this.init_CompletableFuture = server.submit(
               () -> {
                  ServerPlayer actor = server.getPlayerList().getPlayer(uuid);
                  return actor != null
                     && actor.level().dimension().equals(dimension)
                     && actor.containerMenu.containerId == id
                     && actor.containerMenu instanceof ShulkerBoxMenu
                     && actor.containerMenu.getSlot(0).container instanceof ShulkerBoxBlockEntity shulker
                     && shulker.getBlockPos().equals(this.init_BlockPos);
               }
            );
         }
      }

      if (this.init_Minecraft.player.containerMenu != this.init_ShulkerBoxMenu) {
         return this.init_AiOperation_Result_AiOperation_Status_Str(AiOperation.Status.UNCERTAIN, "The claimed stock shulker menu changed");
      }

      if (AiServerEvidence.init_J_I(this.init_ShulkerBoxMenu.containerId) <= this.write_J || this.init_CompletableFuture != null && !this.init_CompletableFuture.isDone()) {
         return this.init_AiOperation_Result_Str("Waiting for the stock shulker server contents");
      }

      if (this.init_CompletableFuture == null
         || !this.init_CompletableFuture.isCompletedExceptionally() && Boolean.TRUE.equals(this.init_CompletableFuture.getNow(false))) {
         this.init_List = this.init_List();
         if (init(init_List_ItemStack(this.init_ItemStack), this.init_List, this.init_Item, 0)
            && this.init_ShulkerBoxMenu.getCarried().isEmpty()
            && init_I_Container_ItemStack(this.init_Minecraft.player.getInventory(), this.init_ItemStack) == this.flush - 1) {
            Set<Integer> destinations = new HashSet<>();

            for (Slot slot : this.init_ShulkerBoxMenu.slots) {
               if (slot.container == this.init_Minecraft.player.getInventory() && slot.getContainerSlot() < 36) {
                  if (this.close < 0 && slot.getItem().isEmpty()) {
                     this.close = slot.getContainerSlot();
                  } else {
                     destinations.add(slot.index);
                  }
               }
            }

            int amount = this.init_I_Z(false);
            if (this.close >= 0 && amount != 0) {
               this.load_List = init_List_Container(this.init_Minecraft.player.getInventory());
               this.write_I = this.init_I();
               HashSet<Integer> sources = new HashSet<>();

               for (int i = 0; i < 27; i++) {
                  if (this.load_ItemStack.isEmpty() || ItemStack.isSameItemSameComponents(this.init_List.get(i), this.load_ItemStack)) {
                     sources.add(i);
                  }
               }

               int inventoryTarget = this.load_ItemStack.isEmpty()
                  ? AiGameState.init_I_Str(BuiltInRegistries.ITEM.getKey(this.init_Item).toString()) + amount
                  : this.write_I + amount;
               this.init_AiOperation = this.load_ItemStack.isEmpty()
                  ? new AiInventoryController(
                     AiJson.init_JsonObject_ObjArr(
                        "item",
                        BuiltInRegistries.ITEM.getKey(this.init_Item).toString(),
                        "count",
                        inventoryTarget,
                        "containerId",
                        this.init_ShulkerBoxMenu.containerId,
                        "timeoutSeconds",
                        80
                     ),
                     sources,
                     destinations
                  )
                  : new AiInventoryController(this.init_ShulkerBoxMenu.containerId, this.load_ItemStack, inventoryTarget, sources, destinations);
               this.init_BitSetUtil_StreamU = AiCarriedShulker.Inner_StreamUtil.WITHDRAW;
               return this.init_AiOperation_Result_Str("Withdrawing building material from the carried shulker");
            } else {
               return this.write();
            }
         } else {
            return this.init_AiOperation_Result_AiOperation_Status_Str(
               AiOperation.Status.UNCERTAIN, "The opened stock shulker does not match the selected inventory box"
            );
         }
      } else {
         return this.init_AiOperation_Result_AiOperation_Status_Str(AiOperation.Status.UNCERTAIN, "The native stock menu belongs to another shulker");
      }
   }

   private AiOperation.Result read_AiOperation_Result() {
      if (this.init_Minecraft.player.containerMenu == this.init_ShulkerBoxMenu && this.load_Z()) {
         AiOperation.Result result = this.init_bb.init_AiOperation_Result(this.init_AiOperation);
         if (!result.terminal()) {
            return this.init_AiOperation_Result_Str(result.message());
         } else {
            this.load_AiOperation_Result = result;
            this.init_bb.init_V(this.init_AiOperation);
            this.init_AiOperation = null;
            if (!this.init_bb.init_Z(null) && result.status() != AiOperation.Status.UNCERTAIN && this.init_ShulkerBoxMenu.getCarried().isEmpty()) {
               this.update = this.init_I() - this.write_I;
               List<ItemStack> remainingStock = this.init_List();
               List<ItemStack> inventoryAfter = init_List_Container(this.init_Minecraft.player.getInventory());
               return (
                     this.load_ItemStack.isEmpty()
                        ? !init(this.init_List, remainingStock, this.load_List, inventoryAfter, this.init_Item, this.update)
                        : !init(this.init_List, remainingStock, this.load_List, inventoryAfter, this.load_ItemStack, this.update)
                  )
                  ? this.init_AiOperation_Result_AiOperation_Status_Str(
                     AiOperation.Status.UNCERTAIN, "The shulker and inventory do not reconcile to the confirmed withdrawal"
                  )
                  : this.write();
            } else {
               return this.init_AiOperation_Result_AiOperation_Status_Str(
                  AiOperation.Status.UNCERTAIN, "The shulker transfer needs reconciliation before recovery"
               );
            }
         }
      } else {
         return this.init_AiOperation_Result_AiOperation_Status_Str(AiOperation.Status.UNCERTAIN, "Stock menu or reserved pickup space changed");
      }
   }

   private AiOperation.Result write() {
      this.read_ItemStack = init_ItemStack_ItemStack_List(this.init_ItemStack, this.init_List());
      if (!AiMenuClose.init_Z()) {
         return this.init_AiOperation_Result_AiOperation_Status_Str(
            AiOperation.Status.UNCERTAIN, "The stock menu could not close with a reconciled empty cursor"
         );
      }

      this.init_BitSetUtil_StreamU = AiCarriedShulker.Inner_StreamUtil.CLOSE;
      return this.init_AiOperation_Result_Str("Closing the stock shulker before recovery");
   }

   private boolean init_Z() {
      return this.init_BlockPos != null
         && AiTerrain.init(this.init_Minecraft.level, this.init_BlockPos)
         && this.init_Minecraft.level.getBlockState(this.init_BlockPos).equals(this.init_BlockState)
         && this.init_Minecraft.level.getBlockEntity(this.init_BlockPos) == this.write_Obj;
   }

   private boolean load_Z() {
      return init(this.init_Minecraft.player.getInventory(), this.close, this.read_ItemStack.isEmpty() ? this.init_ItemStack : this.read_ItemStack);
   }

   private static AABB init_AABB_BlockPos(BlockPos at) {
      return new AABB(at).inflate(2.0, 1.0, 2.0);
   }

   private boolean init_Z_BlockPos(BlockPos at) {
      return this.init_Minecraft.level.getEntitiesOfClass(ItemEntity.class, init_AABB_BlockPos(at).inflate(1.0)).isEmpty();
   }

   private boolean read_Z() {
      return this.init_Minecraft.player.onGround()
         && !this.init_Minecraft.player.getAbilities().flying
         && this.load_Z()
         && this.init_Minecraft.player.containerMenu == this.init_Minecraft.player.inventoryMenu
         && this.init_Minecraft.player.inventoryMenu.getCarried().isEmpty()
         && this.init_Minecraft.level.getBlockState(this.init_BlockPos.below()).equals(this.load_BlockState)
         && init(
            this.init_Minecraft.level,
            p -> AiTerrain.init(this.init_Minecraft.level, p),
            this.init_BlockPos,
            this.init_Minecraft.player.getBoundingBox(),
            this.init_Set,
            true
         )
         && this.init_Z_BlockPos(this.init_BlockPos)
         && autismclient.ai.engine.AiBlockInteraction.init(this.init_Minecraft, this.init_BlockPos) != null;
   }

   private List<ItemStack> init_List() {
      ArrayList<ItemStack> result = new ArrayList<>(27);

      for (int i = 0; i < 27; i++) {
         result.add(this.init_ShulkerBoxMenu.getSlot(i).getItem().copy());
      }

      return result;
   }

   static int init_I_Container_ItemStack(Container inventory, ItemStack wanted) {
      int count = 0;

      for (int i = 0; i < Math.min(41, inventory.getContainerSize()); i++) {
         if ((i < 36 || i == 40) && ItemStack.isSameItemSameComponents(inventory.getItem(i), wanted)) {
            count += inventory.getItem(i).getCount();
         }
      }

      return count;
   }

   private JsonObject init_JsonObject_3() {
      JsonObject data = AiJson.init_JsonObject_ObjArr(
         "carriedShulker",
         true,
         "recovered",
         this.load_Z,
         "withdrawn",
         this.update,
         "recoveryRequired",
         this.init_Z && !this.load_Z,
         "stage",
         this.init_BitSetUtil_StreamU.name(),
         "exactComponents",
         !this.load_ItemStack.isEmpty()
      );
      if (this.init_BlockPos != null) {
         data.add("stockPosition", AiJson.init_JsonObject_ObjArr("x", this.init_BlockPos.getX(), "y", this.init_BlockPos.getY(), "z", this.init_BlockPos.getZ()));
      }

      if (this.load_AiOperation_Result != null) {
         data.add("transfer", AiJson.init_JsonObject_Str(this.load_AiOperation_Result.dataJson()));
      }

      if (this.push >= 0) {
         data.addProperty("stockDropEntity", this.push);
      }

      return data;
   }

   private AiOperation.Result init_AiOperation_Result_Str(String message) {
      return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_3().toString(), List.of());
   }

   private AiOperation.Result init_AiOperation_Result_AiOperation_Status_Str(AiOperation.Status status, String message) {
      boolean pending = this.requiresReconciliationAfterEmergency();
      this.init_bb.init_V(this.init_AiOperation);
      this.init_AiOperation = null;
      this.init_DataPipe.init_V();
      this.init_AiOperation_Result = this.init_bb.init(new AiOperation.Result(status, message, this.init_JsonObject_3().toString(), List.of()), pending);
      return this.init_AiOperation_Result;
   }

   @Override
   public boolean canInterruptNow() {
      return !this.requiresReconciliationAfterEmergency() && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow());
   }

   @Override
   public boolean requiresReconciliationAfterEmergency() {
      return this.init_Z && !this.load_Z
         || this.init_BitSetUtil_StreamU == AiCarriedShulker.Inner_StreamUtil.PLACE && this.init_DataPipe.load_Z()
         || this.init_bb.init_Z(this.init_AiOperation);
   }

   @Override
   public void cancel() {
      if (!this.read_Z) {
         this.init_bb.init(this.requiresReconciliationAfterEmergency());
         this.read_Z = true;
         this.init_bb.init_V(this.init_AiOperation);
         this.init_AiOperation = null;
         this.init_DataPipe.init_V();
      }
   }

   static final class Inner_DataUtil {
      private final long init;
      private final long load;

      Inner_DataUtil(long before, long now) {
         this.init = before;
         this.load = now + 10000000000L;
      }

      AiCarriedShulker.Inner_DataUtil.Inner_DataUtil_2 init(boolean loaded, AiServerEvidence.Inner_DataUtil update, long now) {
         if (loaded && update != null && update.revision() > this.init) {
            return update.state().isAir() ? AiCarriedShulker.Inner_DataUtil.Inner_DataUtil_2.CONFIRMED : AiCarriedShulker.Inner_DataUtil.Inner_DataUtil_2.REJECTED;
         } else {
            return now >= this.load ? AiCarriedShulker.Inner_DataUtil.Inner_DataUtil_2.TIMED_OUT : AiCarriedShulker.Inner_DataUtil.Inner_DataUtil_2.WAITING;
         }
      }

      enum Inner_DataUtil_2 {
         WAITING,
         CONFIRMED,
         REJECTED,
         TIMED_OUT;
      }
   }

   private enum Inner_StreamUtil {
      APPROACH,
      PLACE,
      OPEN,
      WITHDRAW,
      CLOSE,
      BREAK,
      CONFIRM_REMOVAL,
      PICKUP;
   }
}
