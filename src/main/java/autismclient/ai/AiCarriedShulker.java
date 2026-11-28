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
   private final Minecraft mc = Minecraft.getInstance();
   private final Object startLevel = this.mc.level;
   private final Object startPlayer = this.mc.player;
   private final Object startConnection = this.mc.getConnection();
   private final ItemStack box;
   private final Item item;
   private final ItemStack exactMaterial;
   private final int wanted;
   private final Set<Long> protectedCells;
   private final autismclient.ai.engine.AiPlacer placer = new autismclient.ai.engine.AiPlacer();
   private final AiPreparationReceipt receipt = new AiPreparationReceipt();
   private final long deadlineNanos = System.nanoTime() + 120000000000L;
   private AiCarriedShulker.Inner_StreamUtil stage = AiCarriedShulker.Inner_StreamUtil.PLACE;
   private BlockPos boxPos;
   private BlockState boxState;
   private BlockState supportState;
   private Object boxEntity;
   private AiOperation child;
   private ShulkerBoxMenu menu;
   private CompletableFuture<Boolean> init_CompletableFuture;
   private List<ItemStack> menuSnapshot;
   private List<ItemStack> inventoryBefore;
   private List<ItemStack> read_List;
   private ItemStack remainingBox = ItemStack.EMPTY;
   private AiOperation.Result finalResult;
   private AiOperation.Result transferResult;
   private int countBefore;
   private int update;
   private int sync;
   private int flush;
   private int close = -1;
   private int open;
   private int poll;
   private long inventoryRevision;
   private long blockRevision;
   private long inventoryRevisionAtPlace;
   private AiCarriedShulker.Inner_DataUtil init_BitSetUtil_DataUti;
   private boolean placed;
   private boolean recovered;
   private boolean cancelled;
   private boolean placementStarted;
   private int push = -1;

   static boolean isShulkerBox(ItemStack stack) {
      return !stack.isEmpty() && stack.getCount() == 1 && stack.getItem() instanceof BlockItem block && block.getBlock() instanceof ShulkerBoxBlock;
   }

   static List<ItemStack> contents(ItemStack box) {
      NonNullList<ItemStack> result = NonNullList.withSize(27, ItemStack.EMPTY);
      box.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(result);
      return result;
   }

   static int countInBox(ItemStack box, Item item) {
      if (!isShulkerBox(box)) {
         return 0;
      }

      int count = 0;

      for (ItemStack stack : contents(box)) {
         if (stack.is(item)) {
            count += stack.getCount();
         }
      }

      return count;
   }

   static int countExactInBox(ItemStack box, ItemStack wanted) {
      if (isShulkerBox(box) && !wanted.isEmpty()) {
         int count = 0;

         for (ItemStack stack : contents(box)) {
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
                  int count = countInBox(stack, supplied);
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
      if (!isShulkerBox(box)) {
         return null;
      }

      if (countInBox(box, wanted) > 0 && init(inventory, box, wanted, 1, true) > 0) {
         return wanted;
      }

      if (!Compat263.isHoe(wanted) && !Compat263.isShovel(wanted)) {
         return null;
      }

      for (ItemStack stored : contents(box)) {
         if (autismclient.ai.print.PrintCore.matchesItem(stored, wanted) && init(inventory, box, stored.getItem(), 1, true) > 0) {
            return stored.getItem();
         }
      }

      return null;
   }

   static Map<Item, Integer> countItems(Container inventory, boolean includeBoxes) {
      HashMap<Item, Integer> counts = new HashMap<>();

      for (int slot = 0; slot < Math.min(41, inventory.getContainerSize()); slot++) {
         if (slot < 36 || slot == 40) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty()) {
               counts.merge(stack.getItem(), stack.getCount(), Integer::sum);
               if (includeBoxes && isShulkerBox(stack)) {
                  for (ItemStack stored : contents(stack)) {
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
               int count = countExactInBox(stack, wanted);
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
      if (wanted > 0 && isShulkerBox(box) && inventory.getContainerSize() >= 36) {
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
         List<ItemStack> stored = contents(box);

         for (int sourcePass = 0; sourcePass < 2; sourcePass++) {
            for (ItemStack stack : stored) {
               if (matches.test(stack) && AiItems.isPlainItem(stack) == (sourcePass == 0)) {
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

   static ItemStack withContents(ItemStack original, List<ItemStack> remaining) {
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

   static List<ItemStack> snapshot(Container inventory) {
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
      if (isShulkerBox(returning) && before.size() == after.size()) {
         ArrayList<ItemStack> expected = new ArrayList<>();

         for (int i = 0; i < before.size(); i++) {
            if ((i < 36 || i == 40) && isShulkerBox(before.get(i))) {
               expected.add(before.get(i));
            }
         }

         expected.add(returning);

         for (int i = 0; i < after.size(); i++) {
            if ((i < 36 || i == 40) && isShulkerBox(after.get(i))) {
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
                  && !autismclient.ai.engine.AiPathfinder.LiveGridView.isFloorHazardCompat(floor)
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

   static BlockPos clampDropCell(BlockPos box, Vec3 drop) {
      return box.offset(Math.clamp(Mth.floor(drop.x) - box.getX(), -2, 2), 0, Math.clamp(Mth.floor(drop.z) - box.getZ(), -2, 2));
   }

   static autismclient.ai.engine.AiPathfinder.PathGoal init_ClassCache_JsonUti_BlockPos_BlockPos(final BlockPos box, BlockPos target) {
      final autismclient.ai.engine.AiPathfinder.PathGoal near = autismclient.ai.engine.AiPathfinder.PathGoal.nearCompat(target, 0.6);
      final Set<autismclient.ai.engine.AiPathfinder.MoveType> walks = Set.copyOf(
         EnumSet.complementOf(EnumSet.of(autismclient.ai.engine.AiPathfinder.MoveType.WALK, autismclient.ai.engine.AiPathfinder.MoveType.WALK_DIAGONAL))
      );
      return new autismclient.ai.engine.AiPathfinder.PathGoal() {
         @Override
         public boolean isInGoal(int x, int y, int z) {
            return near.isInGoal(x, y, z);
         }

         @Override
         public double heuristic(int x, int y, int z) {
            return near.heuristic(x, y, z);
         }

         @Override
         public boolean allowsCell(BlockPos cell) {
            return cell.getY() == box.getY() && Math.abs(cell.getX() - box.getX()) <= 2 && Math.abs(cell.getZ() - box.getZ()) <= 2;
         }

         @Override
         public Set<autismclient.ai.engine.AiPathfinder.MoveType> disallowedMoves() {
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
      this.box = box.copy();
      this.item = item;
      this.wanted = Math.max(1, wanted);
      this.protectedCells = protectedCells;
      this.exactMaterial = exactMaterial.copyWithCount(1);
      if (!isShulkerBox(box) || (exactMaterial.isEmpty() ? countInBox(box, item) : countExactInBox(box, exactMaterial)) == 0) {
         throw new IllegalArgumentException("The selected shulker has no requested stock");
      }
   }

   private int init_I_Z(boolean beforePlacement) {
      return this.exactMaterial.isEmpty()
         ? init(this.mc.player.getInventory(), this.box, this.item, this.wanted, beforePlacement)
         : init(this.mc.player.getInventory(), this.box, this.exactMaterial, this.wanted, beforePlacement);
   }

   private int carriedCount() {
      return this.exactMaterial.isEmpty()
         ? AiPrintSupply.init(this.mc.player.getInventory(), this.item)
         : AiInventoryController.countMatching(this.mc.player.getInventory(), this.exactMaterial);
   }

   @Override
   public AiOperation.Result tick() {
      if (this.finalResult != null) {
         return this.finalResult;
      }

      if (this.cancelled) {
         return this.finish(AiOperation.Status.CANCELLED, "Portable stock was cancelled");
      }

      if (this.mc.level != this.startLevel
         || this.mc.player != this.startPlayer
         || this.mc.getConnection() != this.startConnection
         || this.mc.player == null
         || !this.mc.player.isAlive()) {
         return this.finish(AiOperation.Status.UNCERTAIN, "Portable stock context changed");
      }

      if (this.mc.player.hasInfiniteMaterials()) {
         return this.finish(AiOperation.Status.UNCERTAIN, "Game mode changed before the carried shulker was recovered");
      }

      if (System.nanoTime() >= this.deadlineNanos) {
         return this.finish(AiOperation.Status.TIMED_OUT, "Portable stock recovery deadline reached");
      }

      if (this.stage == AiCarriedShulker.Inner_StreamUtil.APPROACH
         || this.stage == AiCarriedShulker.Inner_StreamUtil.PLACE
         || this.stage == AiCarriedShulker.Inner_StreamUtil.CONFIRM_REMOVAL
         || this.stage == AiCarriedShulker.Inner_StreamUtil.PICKUP
         || this.boxStillPlaced()
         || this.stage == AiCarriedShulker.Inner_StreamUtil.BREAK
            && this.child != null
            && this.mc.level.getBlockState(this.boxPos).isAir()) {
         if (this.stage == AiCarriedShulker.Inner_StreamUtil.APPROACH) {
            AiOperation.Result result = this.receipt.tickChild(this.child);
            if (!result.terminal()) {
               return this.running("Moving to a safe existing shulker recovery area");
            }

            this.receipt.cancelChild(this.child);
            this.child = null;
            if (result.status() != AiOperation.Status.SUCCEEDED) {
               return this.finish(result.status(), "Could not reach a safe shulker recovery area");
            }

            this.stage = AiCarriedShulker.Inner_StreamUtil.PLACE;
            return this.running("Rechecking the shulker recovery area after arrival");
         } else {
            if (this.stage == AiCarriedShulker.Inner_StreamUtil.PLACE) {
               return this.tickPlace();
            }

            if (this.stage == AiCarriedShulker.Inner_StreamUtil.OPEN) {
               return this.tickWithdraw();
            }

            if (this.stage == AiCarriedShulker.Inner_StreamUtil.WITHDRAW) {
               return this.read_AiOperation_Result();
            }

            if (this.stage == AiCarriedShulker.Inner_StreamUtil.CLOSE) {
               if (this.mc.player.containerMenu != this.mc.player.inventoryMenu) {
                  return this.finish(AiOperation.Status.UNCERTAIN, "Another menu opened during shulker recovery");
               }

               if (++this.open < 10 || this.boxEntity instanceof ShulkerBoxBlockEntity shulker && !shulker.isClosed()) {
                  return this.running("Waiting for the shulker lid to close");
               }

               if (!this.read_Z()) {
                  return this.finish(
                     AiOperation.Status.UNCERTAIN, "The stock box recovery area or reserved pickup capacity changed; box left placed"
                  );
               }

               this.sync = countExactInInventory(this.mc.player.getInventory(), this.remainingBox);
               this.read_List = snapshot(this.mc.player.getInventory());
               this.inventoryRevision = AiServerEvidence.inventoryRevision();
               this.blockRevision = AiServerEvidence.blockRevision(this.boxPos);
               this.child = autismclient.ai.engine.AiBreakBlock.breakBlock(this.boxPos, 25, true, this.protectedCells, false);
               this.stage = AiCarriedShulker.Inner_StreamUtil.BREAK;
               return this.running("Recovering the owned stock shulker");
            } else if (this.stage == AiCarriedShulker.Inner_StreamUtil.BREAK) {
               if (this.boxStillPlaced() && !this.read_Z()) {
                  return this.finish(
                     AiOperation.Status.UNCERTAIN, "Stock recovery is no longer safe; further breaking stopped"
                  );
               }

               AiOperation.Result result = this.receipt.tickChild(this.child);
               if (!result.terminal()) {
                  return this.running(result.message());
               }

               this.receipt.cancelChild(this.child);
               this.child = null;
               if (result.status() != AiOperation.Status.SUCCEEDED) {
                  return this.finish(
                     AiOperation.Status.UNCERTAIN, "The stock shulker break did not finish: " + result.message()
                  );
               }

               this.init_BitSetUtil_DataUti = new AiCarriedShulker.Inner_DataUtil(this.blockRevision, System.nanoTime());
               this.stage = AiCarriedShulker.Inner_StreamUtil.CONFIRM_REMOVAL;
               return this.running("Waiting for the server to confirm the stock shulker removal");
            } else if (this.stage == AiCarriedShulker.Inner_StreamUtil.CONFIRM_REMOVAL) {
               AiCarriedShulker.Inner_DataUtil.Inner_DataUtil_2 observed = this.init_BitSetUtil_DataUti
                  .init(AiTerrain.isLoaded(this.mc.level, this.boxPos), AiServerEvidence.lastBlockUpdate(this.boxPos), System.nanoTime());
               if (observed == AiCarriedShulker.Inner_DataUtil.Inner_DataUtil_2.WAITING) {
                  return this.running("Waiting for the server to confirm the stock shulker removal");
               }

               if (observed != AiCarriedShulker.Inner_DataUtil.Inner_DataUtil_2.CONFIRMED) {
                  return this.finish(
                     AiOperation.Status.UNCERTAIN,
                     observed == AiCarriedShulker.Inner_DataUtil.Inner_DataUtil_2.REJECTED
                        ? "The server did not confirm an empty stock shulker cell; inspect the placed box"
                        : "The stock shulker removal confirmation did not arrive"
                  );
               }

               this.stage = AiCarriedShulker.Inner_StreamUtil.PICKUP;
               return this.running("Locating the observed stock shulker drop");
            } else {
               if (!init(
                  this.mc.level,
                  p -> AiTerrain.isLoaded(this.mc.level, p),
                  this.boxPos,
                  this.mc.player.getBoundingBox(),
                  this.protectedCells,
                  false
               )) {
                  return this.finish(
                     AiOperation.Status.UNCERTAIN, "The shulker pickup area changed; no unsafe recovery route will be attempted"
                  );
               }

               if (this.child != null) {
                  AiOperation.Result result = this.receipt.tickChild(this.child);
                  if (!result.terminal()) {
                     return this.running(result.message());
                  }

                  this.receipt.cancelChild(this.child);
                  this.child = null;
                  if (result.status() != AiOperation.Status.SUCCEEDED) {
                     return this.finish(AiOperation.Status.UNCERTAIN, "Could not reach the dropped stock shulker");
                  }
               }

               if (AiServerEvidence.inventoryRevision() > this.inventoryRevision
                  && countExactInInventory(this.mc.player.getInventory(), this.remainingBox) == this.sync + 1
                  && this.mc.player.onGround()
                  && this.mc.player.getDeltaMovement().horizontalDistanceSqr() <= 4.0E-4) {
                  if (!init(this.read_List, snapshot(this.mc.player.getInventory()), this.remainingBox)) {
                     return this.finish(
                        AiOperation.Status.UNCERTAIN, "The recovered box or the other carried shulkers changed during pickup"
                     );
                  }

                  this.recovered = true;
                  return this.finish(
                     this.update > 0 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED,
                     this.update > 0 ? "Material withdrawn and stock shulker recovered" : "Stock shulker recovered without a material transfer"
                  );
               } else {
                  ItemEntity dropped = null;

                  for (ItemEntity candidate : this.mc.level.getEntitiesOfClass(ItemEntity.class, pickupArea(this.boxPos))) {
                     if (ItemStack.matches(candidate.getItem(), this.remainingBox)
                        && !candidate.isRemoved()
                        && (this.push < 0 || candidate.getId() == this.push)) {
                        if (dropped != null) {
                           return this.finish(
                              AiOperation.Status.UNCERTAIN, "More than one indistinguishable stock drop appeared; inspect recovery"
                           );
                        }

                        dropped = candidate;
                     }
                  }

                  if (dropped == null) {
                     return this.running("Waiting for the stock shulker entity or its inventory receipt");
                  }

                  this.push = dropped.getId();
                  AABB dropBounds = dropped.getBoundingBox();
                  if (dropBounds.minX < this.boxPos.getX() - 2
                     || dropBounds.maxX > this.boxPos.getX() + 2 + 1
                     || dropBounds.minZ < this.boxPos.getZ() - 2
                     || dropBounds.maxZ > this.boxPos.getZ() + 2 + 1) {
                     return this.finish(
                        AiOperation.Status.UNCERTAIN, "The stock drop moved beyond the proved landing floor; no edge chase will be attempted"
                     );
                  }

                  if (this.mc.player.getBoundingBox().inflate(1.0, 0.5, 1.0).intersects(dropped.getBoundingBox())) {
                     return this.running("Waiting for the nearby stock shulker pickup receipt");
                  }

                  BlockPos target = clampDropCell(this.boxPos, dropped.position());
                  this.child = AiGoto.travelToCompat(
                     target,
                     0.6,
                     false,
                     false,
                     false,
                     25,
                     autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST,
                     false,
                     2.0,
                     init_ClassCache_JsonUti_BlockPos_BlockPos(this.boxPos, target),
                     new AiGoto.GotoOptions(false, false, 0, true, false, false)
                  );
                  return this.running("Walking inside the safe area to the observed stock shulker");
               }
            }
         }
      } else {
         return this.finish(AiOperation.Status.UNCERTAIN, "The placed stock box changed before recovery");
      }
   }

   private AiOperation.Result tickPlace() {
      if (!this.placementStarted) {
         if (!this.mc.player.onGround()) {
            return this.finish(AiOperation.Status.FAILED, "A supported stance is needed for portable stock");
         }

         if (this.mc.player.containerMenu != this.mc.player.inventoryMenu
            || !this.mc.player.inventoryMenu.getCarried().isEmpty()) {
            return this.finish(AiOperation.Status.FAILED, "Close the menu and clear the cursor before portable stock");
         }

         if (this.init_I_Z(true) == 0) {
            return this.finish(
               AiOperation.Status.FAILED, "Free one inventory slot for material while keeping room to recover the shulker"
            );
         }

         BlockPos feet = this.mc.player.blockPosition();
         this.boxPos = init_BlockPos_Minecraft_Set(this.mc, this.protectedCells);
         if (this.boxPos == null) {
            for (int radius = 3; radius <= 8; radius++) {
               for (Direction side : Plane.HORIZONTAL) {
                  BlockPos candidate = feet.relative(side, radius);
                  BlockPos stance = candidate.relative(side.getOpposite());
                  AABB body = this.mc.player.getBoundingBox().move(Vec3.atBottomCenterOf(stance).subtract(this.mc.player.position()));
                  if (init(this.mc.level, p -> AiTerrain.isLoaded(this.mc.level, p), candidate, body, this.protectedCells)
                     && this.noItemsNear(candidate)) {
                     this.boxPos = candidate.immutable();
                     this.child = AiGoto.travelToCompat(
                        stance,
                        0.6,
                        false,
                        false,
                        false,
                        25,
                        autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST,
                        false,
                        2.0,
                        AiGoto.protectingCellsCompat(autismclient.ai.engine.AiPathfinder.PathGoal.nearCompat(stance, 0.6), this.protectedCells),
                        new AiGoto.GotoOptions(false, false, 0, true, false, false)
                     );
                     this.stage = AiCarriedShulker.Inner_StreamUtil.APPROACH;
                     return this.running("Moving to an existing flat area before placing the stock shulker");
                  }
               }
            }
         }

         if (this.boxPos == null) {
            return this.finish(
               AiOperation.Status.FAILED, "No clear flat shulker recovery area nearby; stock box kept in inventory"
            );
         }

         if (!init(
               this.mc.level,
               p -> AiTerrain.isLoaded(this.mc.level, p),
               this.boxPos,
               this.mc.player.getBoundingBox(),
               this.protectedCells
            )
            || !this.noItemsNear(this.boxPos)) {
            return this.finish(AiOperation.Status.FAILED, "The selected shulker recovery area is no longer clear");
         }

         this.supportState = this.mc.level.getBlockState(this.boxPos.below());
         this.flush = countExactInInventory(this.mc.player.getInventory(), this.box);
         this.placer.startWithExactStack(this.boxPos, this.box);
         this.placementStarted = true;
      }

      if (this.placer.hasClicked()
         || init(
               this.mc.level,
               p -> AiTerrain.isLoaded(this.mc.level, p),
               this.boxPos,
               this.mc.player.getBoundingBox(),
               this.protectedCells
            )
            && this.noItemsNear(this.boxPos)) {
         autismclient.ai.engine.AiPlacer.PlaceStatus placed = this.placer.tick();
         this.placed = this.placed | this.placer.hasClicked();
         if (placed == autismclient.ai.engine.AiPlacer.PlaceStatus.FAILED) {
            return this.finish(
               this.placed ? AiOperation.Status.UNCERTAIN : AiOperation.Status.FAILED, this.placer.failureReason()
            );
         } else if (placed != autismclient.ai.engine.AiPlacer.PlaceStatus.PLACED) {
            return this.running("Placing the selected stock shulker");
         } else {
            this.boxState = this.mc.level.getBlockState(this.boxPos);
            this.boxEntity = this.mc.level.getBlockEntity(this.boxPos);
            if (this.boxEntity != null && this.boxState.getValue(ShulkerBoxBlock.FACING) == Direction.UP) {
               this.placer.reset();
               this.inventoryRevisionAtPlace = AiServerEvidence.inventoryRevision();
               this.child = AiContainer.init(
                  AiJson.objectOf(
                     "action",
                     "inspect",
                     "x",
                     this.boxPos.getX(),
                     "y",
                     this.boxPos.getY(),
                     "z",
                     this.boxPos.getZ(),
                     "navigate",
                     false,
                     "timeoutSeconds",
                     20
                  ),
                  this.protectedCells
               );
               this.stage = AiCarriedShulker.Inner_StreamUtil.OPEN;
               return this.running("Opening the placed stock shulker");
            } else {
               return this.finish(
                  AiOperation.Status.UNCERTAIN, "The stock shulker did not retain its verified upward opening"
               );
            }
         }
      } else {
         return this.finish(AiOperation.Status.FAILED, "The selected portable stock site changed");
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
                     if (init(mc.level, p -> AiTerrain.isLoaded(mc.level, p), at, body, protectedCells)
                        && mc.level.getEntitiesOfClass(ItemEntity.class, pickupArea(at).inflate(1.0)).isEmpty()) {
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

   private AiOperation.Result tickWithdraw() {
      if (this.child != null) {
         AiOperation.Result result = this.receipt.tickChild(this.child);
         if (!result.terminal()) {
            return this.running(result.message());
         }

         this.receipt.cancelChild(this.child);
         this.child = null;
         if (result.status() != AiOperation.Status.SUCCEEDED || !(this.mc.player.containerMenu instanceof ShulkerBoxMenu opened)) {
            return this.finish(AiOperation.Status.UNCERTAIN, "The stock shulker menu was not confirmed");
         }

         this.menu = opened;
         IntegratedServer server = this.mc.getSingleplayerServer();
         if (server != null) {
            UUID uuid = this.mc.player.getUUID();
            ResourceKey<Level> dimension = this.mc.level.dimension();
            int id = this.menu.containerId;
            this.init_CompletableFuture = server.submit(
               () -> {
                  ServerPlayer actor = server.getPlayerList().getPlayer(uuid);
                  return actor != null
                     && actor.level().dimension().equals(dimension)
                     && actor.containerMenu.containerId == id
                     && actor.containerMenu instanceof ShulkerBoxMenu
                     && actor.containerMenu.getSlot(0).container instanceof ShulkerBoxBlockEntity shulker
                     && shulker.getBlockPos().equals(this.boxPos);
               }
            );
         }
      }

      if (this.mc.player.containerMenu != this.menu) {
         return this.finish(AiOperation.Status.UNCERTAIN, "The claimed stock shulker menu changed");
      }

      if (AiServerEvidence.containerRevision(this.menu.containerId) <= this.inventoryRevisionAtPlace || this.init_CompletableFuture != null && !this.init_CompletableFuture.isDone()) {
         return this.running("Waiting for the stock shulker server contents");
      }

      if (this.init_CompletableFuture == null
         || !this.init_CompletableFuture.isCompletedExceptionally() && Boolean.TRUE.equals(this.init_CompletableFuture.getNow(false))) {
         this.menuSnapshot = this.menuContents();
         if (init(contents(this.box), this.menuSnapshot, this.item, 0)
            && this.menu.getCarried().isEmpty()
            && countExactInInventory(this.mc.player.getInventory(), this.box) == this.flush - 1) {
            Set<Integer> destinations = new HashSet<>();

            for (Slot slot : this.menu.slots) {
               if (slot.container == this.mc.player.getInventory() && slot.getContainerSlot() < 36) {
                  if (this.close < 0 && slot.getItem().isEmpty()) {
                     this.close = slot.getContainerSlot();
                  } else {
                     destinations.add(slot.index);
                  }
               }
            }

            int amount = this.init_I_Z(false);
            if (this.close >= 0 && amount != 0) {
               this.inventoryBefore = snapshot(this.mc.player.getInventory());
               this.countBefore = this.carriedCount();
               HashSet<Integer> sources = new HashSet<>();

               for (int i = 0; i < 27; i++) {
                  if (this.exactMaterial.isEmpty() || ItemStack.isSameItemSameComponents(this.menuSnapshot.get(i), this.exactMaterial)) {
                     sources.add(i);
                  }
               }

               int inventoryTarget = this.exactMaterial.isEmpty()
                  ? AiGameState.countInventoryItem(BuiltInRegistries.ITEM.getKey(this.item).toString()) + amount
                  : this.countBefore + amount;
               this.child = this.exactMaterial.isEmpty()
                  ? new AiInventoryController(
                     AiJson.objectOf(
                        "item",
                        BuiltInRegistries.ITEM.getKey(this.item).toString(),
                        "count",
                        inventoryTarget,
                        "containerId",
                        this.menu.containerId,
                        "timeoutSeconds",
                        80
                     ),
                     sources,
                     destinations
                  )
                  : new AiInventoryController(this.menu.containerId, this.exactMaterial, inventoryTarget, sources, destinations);
               this.stage = AiCarriedShulker.Inner_StreamUtil.WITHDRAW;
               return this.running("Withdrawing building material from the carried shulker");
            } else {
               return this.write();
            }
         } else {
            return this.finish(
               AiOperation.Status.UNCERTAIN, "The opened stock shulker does not match the selected inventory box"
            );
         }
      } else {
         return this.finish(AiOperation.Status.UNCERTAIN, "The native stock menu belongs to another shulker");
      }
   }

   private AiOperation.Result read_AiOperation_Result() {
      if (this.mc.player.containerMenu == this.menu && this.load_Z()) {
         AiOperation.Result result = this.receipt.tickChild(this.child);
         if (!result.terminal()) {
            return this.running(result.message());
         } else {
            this.transferResult = result;
            this.receipt.cancelChild(this.child);
            this.child = null;
            if (!this.receipt.requiresReconciliation(null) && result.status() != AiOperation.Status.UNCERTAIN && this.menu.getCarried().isEmpty()) {
               this.update = this.carriedCount() - this.countBefore;
               List<ItemStack> remainingStock = this.menuContents();
               List<ItemStack> inventoryAfter = snapshot(this.mc.player.getInventory());
               return (
                     this.exactMaterial.isEmpty()
                        ? !init(this.menuSnapshot, remainingStock, this.inventoryBefore, inventoryAfter, this.item, this.update)
                        : !init(this.menuSnapshot, remainingStock, this.inventoryBefore, inventoryAfter, this.exactMaterial, this.update)
                  )
                  ? this.finish(
                     AiOperation.Status.UNCERTAIN, "The shulker and inventory do not reconcile to the confirmed withdrawal"
                  )
                  : this.write();
            } else {
               return this.finish(
                  AiOperation.Status.UNCERTAIN, "The shulker transfer needs reconciliation before recovery"
               );
            }
         }
      } else {
         return this.finish(AiOperation.Status.UNCERTAIN, "Stock menu or reserved pickup space changed");
      }
   }

   private AiOperation.Result write() {
      this.remainingBox = withContents(this.box, this.menuContents());
      if (!AiMenuClose.closeMenu()) {
         return this.finish(
            AiOperation.Status.UNCERTAIN, "The stock menu could not close with a reconciled empty cursor"
         );
      }

      this.stage = AiCarriedShulker.Inner_StreamUtil.CLOSE;
      return this.running("Closing the stock shulker before recovery");
   }

   private boolean boxStillPlaced() {
      return this.boxPos != null
         && AiTerrain.isLoaded(this.mc.level, this.boxPos)
         && this.mc.level.getBlockState(this.boxPos).equals(this.boxState)
         && this.mc.level.getBlockEntity(this.boxPos) == this.boxEntity;
   }

   private boolean load_Z() {
      return init(this.mc.player.getInventory(), this.close, this.remainingBox.isEmpty() ? this.box : this.remainingBox);
   }

   private static AABB pickupArea(BlockPos at) {
      return new AABB(at).inflate(2.0, 1.0, 2.0);
   }

   private boolean noItemsNear(BlockPos at) {
      return this.mc.level.getEntitiesOfClass(ItemEntity.class, pickupArea(at).inflate(1.0)).isEmpty();
   }

   private boolean read_Z() {
      return this.mc.player.onGround()
         && !this.mc.player.getAbilities().flying
         && this.load_Z()
         && this.mc.player.containerMenu == this.mc.player.inventoryMenu
         && this.mc.player.inventoryMenu.getCarried().isEmpty()
         && this.mc.level.getBlockState(this.boxPos.below()).equals(this.supportState)
         && init(
            this.mc.level,
            p -> AiTerrain.isLoaded(this.mc.level, p),
            this.boxPos,
            this.mc.player.getBoundingBox(),
            this.protectedCells,
            true
         )
         && this.noItemsNear(this.boxPos)
         && autismclient.ai.engine.AiBlockInteraction.findReachableHit(this.mc, this.boxPos) != null;
   }

   private List<ItemStack> menuContents() {
      ArrayList<ItemStack> result = new ArrayList<>(27);

      for (int i = 0; i < 27; i++) {
         result.add(this.menu.getSlot(i).getItem().copy());
      }

      return result;
   }

   static int countExactInInventory(Container inventory, ItemStack wanted) {
      int count = 0;

      for (int i = 0; i < Math.min(41, inventory.getContainerSize()); i++) {
         if ((i < 36 || i == 40) && ItemStack.isSameItemSameComponents(inventory.getItem(i), wanted)) {
            count += inventory.getItem(i).getCount();
         }
      }

      return count;
   }

   private JsonObject evidence() {
      JsonObject data = AiJson.objectOf(
         "carriedShulker",
         true,
         "recovered",
         this.recovered,
         "withdrawn",
         this.update,
         "recoveryRequired",
         this.placed && !this.recovered,
         "stage",
         this.stage.name(),
         "exactComponents",
         !this.exactMaterial.isEmpty()
      );
      if (this.boxPos != null) {
         data.add("stockPosition", AiJson.objectOf("x", this.boxPos.getX(), "y", this.boxPos.getY(), "z", this.boxPos.getZ()));
      }

      if (this.transferResult != null) {
         data.add("transfer", AiJson.parseObject(this.transferResult.dataJson()));
      }

      if (this.push >= 0) {
         data.addProperty("stockDropEntity", this.push);
      }

      return data;
   }

   private AiOperation.Result running(String message) {
      return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.evidence().toString(), List.of());
   }

   private AiOperation.Result finish(AiOperation.Status status, String message) {
      boolean pending = this.requiresReconciliationAfterEmergency();
      this.receipt.cancelChild(this.child);
      this.child = null;
      this.placer.reset();
      this.finalResult = this.receipt.finalizeResult(new AiOperation.Result(status, message, this.evidence().toString(), List.of()), pending);
      return this.finalResult;
   }

   @Override
   public boolean canInterruptNow() {
      return !this.requiresReconciliationAfterEmergency() && (this.child == null || this.child.canInterruptNow());
   }

   @Override
   public boolean requiresReconciliationAfterEmergency() {
      return this.placed && !this.recovered
         || this.stage == AiCarriedShulker.Inner_StreamUtil.PLACE && this.placer.needsReconciliation()
         || this.receipt.requiresReconciliation(this.child);
   }

   @Override
   public void cancel() {
      if (!this.cancelled) {
         this.receipt.markReconciliationPending(this.requiresReconciliationAfterEmergency());
         this.cancelled = true;
         this.receipt.cancelChild(this.child);
         this.child = null;
         this.placer.reset();
      }
   }

   static final class Inner_DataUtil {
      private final long init;
      private final long load;

      Inner_DataUtil(long before, long now) {
         this.init = before;
         this.load = now + 10000000000L;
      }

      AiCarriedShulker.Inner_DataUtil.Inner_DataUtil_2 init(boolean loaded, AiServerEvidence.BlockUpdate update, long now) {
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
