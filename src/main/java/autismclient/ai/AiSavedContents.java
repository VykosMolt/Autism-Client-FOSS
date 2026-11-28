package autismclient.ai;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.IntPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

public final class AiSavedContents {
   private AiSavedContents() {
   }

   static boolean isContainer(BlockState state) {
      Block block = state.getBlock();
      return block instanceof ChestBlock
         || block instanceof BarrelBlock
         || block instanceof ShulkerBoxBlock
         || block instanceof HopperBlock
         || block instanceof DispenserBlock
         || block instanceof CrafterBlock;
   }

   public static boolean matchesSaved(BlockState expected, BlockState actual, boolean typedSource) {
      if (!actual.is(expected.getBlock())) {
         return false;
      }

      if (expected.getBlock() instanceof BarrelBlock) {
         actual = actual.setValue(BarrelBlock.OPEN, expected.getValue(BarrelBlock.OPEN));
      }

      return !typedSource || autismclient.ai.print.PrintRules.matchesLayout(expected, actual);
   }

   static int slotCount(BlockState state) {
      Block block = state.getBlock();
      if (block instanceof HopperBlock) {
         return 5;
      } else if (!(block instanceof DispenserBlock) && !(block instanceof CrafterBlock)) {
         return !(block instanceof ChestBlock) && !(block instanceof BarrelBlock) && !(block instanceof ShulkerBoxBlock) ? 0 : 27;
      } else {
         return 9;
      }
   }

   public static AiSavedContents.SlotMapping slotMapping(BlockState state, AbstractContainerMenu menu) {
      Block block = state.getBlock();
      if (block instanceof ChestBlock && menu instanceof ChestMenu chest) {
         ChestType type = state.getValue(ChestBlock.TYPE);
         int size = type == ChestType.SINGLE ? 27 : 54;
         return chest.getRowCount() * 9 == size ? new AiSavedContents.SlotMapping(type == ChestType.LEFT ? 27 : 0, 27, size) : null;
      } else if (block instanceof BarrelBlock && menu instanceof ChestMenu chest && chest.getRowCount() == 3) {
         return new AiSavedContents.SlotMapping(0, 27, 27);
      } else if (block instanceof ShulkerBoxBlock && menu instanceof ShulkerBoxMenu) {
         return new AiSavedContents.SlotMapping(0, 27, 27);
      } else if (block instanceof HopperBlock && menu instanceof HopperMenu) {
         return new AiSavedContents.SlotMapping(0, 5, 5);
      } else if (block instanceof DispenserBlock && menu instanceof DispenserMenu) {
         return new AiSavedContents.SlotMapping(0, 9, 9);
      } else {
         return block instanceof CrafterBlock && menu instanceof CrafterMenu ? new AiSavedContents.SlotMapping(0, 9, 9) : null;
      }
   }

   public static AiSavedContents.FillPlan fillPlan(
      AbstractContainerMenu menu, Inventory inventory, Player player, Map<Integer, ItemStack> saved, AiSavedContents.SlotMapping layout
   ) {
      return fillPlan(menu, inventory, player, saved, layout, slot -> false);
   }

   static AiSavedContents.FillPlan fillPlan(
      AbstractContainerMenu menu, Inventory inventory, Player player, Map<Integer, ItemStack> saved, AiSavedContents.SlotMapping layout, IntPredicate clearing
   ) {
      LinkedHashMap<Integer, ItemStack> targets = new LinkedHashMap<>();
      ArrayList<ItemStack> available = new ArrayList<>();

      for (Slot slot : menu.slots) {
         if (slot.container == inventory && slot.mayPickup(player) && !slot.getItem().isEmpty()) {
            available.add(slot.getItem().copy());
         }
      }

      ArrayList<AiSavedContents.MissingItem> missing = new ArrayList<>();
      int satisfied = 0;
      if (!menu.getCarried().isEmpty()) {
         return new AiSavedContents.FillPlan(Map.of(), 0, List.of(), "The inventory cursor is occupied");
      }

      label78:
      for (Entry<Integer, ItemStack> entry : saved.entrySet().stream().sorted(Entry.comparingByKey()).toList()) {
         int raw = entry.getKey();
         int destination = raw + layout.offset();
         ItemStack wanted = entry.getValue();
         if (raw >= 0 && raw < layout.savedSlots() && destination < menu.slots.size() && !wanted.isEmpty()) {
            Slot slot = menu.getSlot(destination);
            ItemStack current = clearing.test(destination) ? ItemStack.EMPTY : slot.getItem();
            if (slot.container != inventory
               && (current.isEmpty() || ItemStack.isSameItemSameComponents(current, wanted))
               && current.getCount() <= wanted.getCount()) {
               targets.put(destination, wanted.copy());
               if (ItemStack.matches(current, wanted)) {
                  satisfied++;
                  continue;
               }

               if (slot.mayPlace(wanted) && wanted.getCount() <= slot.getMaxStackSize(wanted)) {
                  int need = wanted.getCount() - current.getCount();
                  Iterator var18 = available.iterator();

                  while (true) {
                     if (var18.hasNext()) {
                        ItemStack stack = (ItemStack)var18.next();
                        if (!ItemStack.isSameItemSameComponents(stack, wanted)) {
                           continue;
                        }

                        int used = Math.min(need, stack.getCount());
                        need -= used;
                        stack.shrink(used);
                        if (need != 0) {
                           continue;
                        }
                     }

                     if (need > 0) {
                        missing.add(new AiSavedContents.MissingItem(raw, wanted, need));
                     }
                     continue label78;
                  }
               }

               return new AiSavedContents.FillPlan(targets, satisfied, missing, "Saved slot " + raw + " does not accept this stack");
            }

            return new AiSavedContents.FillPlan(targets, satisfied, missing, "Saved slot " + raw + " has different or excess contents");
         }

         return new AiSavedContents.FillPlan(targets, satisfied, missing, "Invalid saved storage slot " + raw);
      }

      return new AiSavedContents.FillPlan(targets, satisfied, missing, null);
   }

   public record FillPlan(Map<Integer, ItemStack> targets, int satisfiedSlots, List<AiSavedContents.MissingItem> missing, String conflict) {
      public FillPlan {
         targets = Map.copyOf(targets);
         missing = List.copyOf(missing);
      }

      boolean ready() {
         return this.conflict == null && this.missing.isEmpty();
      }

      int unfilledSlots() {
         return this.targets.size() - this.satisfiedSlots;
      }
   }

   public record SlotMapping(int offset, int savedSlots, int menuSlots) {
   }

   public record MissingItem(int slot, ItemStack wanted, int count) {
      public MissingItem {
         wanted = wanted.copy();
      }

      public ItemStack wanted() {
         return this.wanted.copy();
      }

      String message() {
         return "Slot " + this.slot + " lacks " + this.count + " " + BuiltInRegistries.ITEM.getKey(this.wanted.getItem());
      }
   }
}
