package autismclient.ai.print;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;

final class PrintCreativeStock {
   private final Map<Integer, ItemStack> stockedSlots = new HashMap<>();

   int stockedSlot(IntFunction<ItemStack> inventory) {
      return this.stockedSlot(inventory, stack -> false);
   }

   int stockedSlot(IntFunction<ItemStack> inventory, Predicate<ItemStack> reserved) {
      this.stockedSlots.entrySet().removeIf(e -> !ItemStack.matches(e.getValue(), inventory.apply(e.getKey())));

      for (int i = 0; i < 36; i++) {
         if (inventory.apply(i).isEmpty()) {
            return i;
         }
      }

      return this.stockedSlots.keySet().stream().filter(slot -> slot >= 0 && slot < 36 && !reserved.test(this.stockedSlots.get(slot))).min(Integer::compare).orElse(-1);
   }

   int stockedSlotOrOffhand(IntFunction<ItemStack> inventory, Predicate<ItemStack> reserved) {
      int main = this.stockedSlot(inventory, reserved);
      return main >= 0 ? main : (inventory.apply(40).isEmpty() ? 40 : -1);
   }

   int stockedHotbarSlot(IntFunction<ItemStack> inventory) {
      int main = this.stockedSlot(inventory);
      if (main >= 0 && main < 9) {
         return main;
      } else {
         int owned = this.stockedSlots.keySet().stream().filter(index -> index >= 0 && index < 9).min(Integer::compare).orElse(-1);
         if (owned >= 0) {
            return owned;
         } else {
            return main >= 0 ? main : (inventory.apply(40).isEmpty() ? 40 : -1);
         }
      }
   }

   void recordSlot(int slot, ItemStack stack) {
      if (stack.isEmpty()) {
         this.stockedSlots.remove(slot);
      } else {
         this.stockedSlots.put(slot, stack.copy());
      }
   }

   void swapSlots(int a, int b) {
      ItemStack first = this.stockedSlots.remove(a);
      ItemStack second = this.stockedSlots.remove(b);
      if (first != null) {
         this.stockedSlots.put(b, first);
      }

      if (second != null) {
         this.stockedSlots.put(a, second);
      }
   }
}
