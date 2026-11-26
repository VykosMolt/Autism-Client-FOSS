package autismclient.ai.b;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;

final class PrintCreativeStock {
   private final Map<Integer, ItemStack> init = new HashMap<>();

   int init(IntFunction<ItemStack> inventory) {
      return this.init(inventory, stack -> false);
   }

   int init(IntFunction<ItemStack> inventory, Predicate<ItemStack> reserved) {
      this.init.entrySet().removeIf(e -> !ItemStack.matches(e.getValue(), inventory.apply(e.getKey())));

      for (int i = 0; i < 36; i++) {
         if (inventory.apply(i).isEmpty()) {
            return i;
         }
      }

      return this.init.keySet().stream().filter(slot -> slot >= 0 && slot < 36 && !reserved.test(this.init.get(slot))).min(Integer::compare).orElse(-1);
   }

   int load(IntFunction<ItemStack> inventory, Predicate<ItemStack> reserved) {
      int main = this.init(inventory, reserved);
      return main >= 0 ? main : (inventory.apply(40).isEmpty() ? 40 : -1);
   }

   int load(IntFunction<ItemStack> inventory) {
      int main = this.init(inventory);
      if (main >= 0 && main < 9) {
         return main;
      } else {
         int owned = this.init.keySet().stream().filter(index -> index >= 0 && index < 9).min(Integer::compare).orElse(-1);
         if (owned >= 0) {
            return owned;
         } else {
            return main >= 0 ? main : (inventory.apply(40).isEmpty() ? 40 : -1);
         }
      }
   }

   void init(int slot, ItemStack stack) {
      if (stack.isEmpty()) {
         this.init.remove(slot);
      } else {
         this.init.put(slot, stack.copy());
      }
   }

   void init(int a, int b) {
      ItemStack first = this.init.remove(a);
      ItemStack second = this.init.remove(b);
      if (first != null) {
         this.init.put(b, first);
      }

      if (second != null) {
         this.init.put(a, second);
      }
   }
}
