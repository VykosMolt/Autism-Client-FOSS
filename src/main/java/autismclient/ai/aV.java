package autismclient.ai;

import autismclient.api.ai.AiOperation;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class aV {
   private aV() {
   }

   public static String init_Str(String itemId) {
      if (itemId == null) {
         return null;
      }

      int colon = itemId.indexOf(58);
      String namespace = colon < 0 ? "minecraft" : itemId.substring(0, colon);
      String path = colon < 0 ? itemId : itemId.substring(colon + 1);
      if (path.startsWith("stripped_")) {
         path = path.substring("stripped_".length());
      }

      for (String suffix : new String[]{"_log", "_wood", "_stem", "_hyphae", "_block"}) {
         if (path.endsWith(suffix) && path.length() != suffix.length()) {
            String base = path.substring(0, path.length() - suffix.length());
            if (suffix.equals("_block") && !base.equals("bamboo")) {
               return null;
            }

            return namespace + ":" + base + "_planks";
         }
      }

      return null;
   }

   public static int init_I(String itemId) {
      return itemId != null && itemId.endsWith("bamboo_block") ? 2 : 4;
   }

   static String init(ItemStack stack) {
      if (stack.isEmpty()) {
         return null;
      }

      String planks = init_Str(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
      if (planks == null) {
         return null;
      }

      Identifier id = Identifier.tryParse(planks);
      return id != null && BuiltInRegistries.ITEM.getOptional(id).isPresent() ? planks : null;
   }

   public static int init(Inventory inventory) {
      int total = 0;

      for (int slot = 0; slot < 36; slot++) {
         ItemStack stack = inventory.getItem(slot);
         if (init(stack) != null) {
            total += stack.getCount() * init_I(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
         }
      }

      return total;
   }

   public static AiOperation init(Inventory inventory, int wanted) {
      Map<String, Integer> byPlanks = new HashMap<>();
      Map<String, Integer> perLog = new HashMap<>();

      for (int slot = 0; slot < 36; slot++) {
         ItemStack stack = inventory.getItem(slot);
         String planks = init(stack);
         if (planks != null) {
            byPlanks.merge(planks, stack.getCount(), Integer::sum);
            perLog.put(planks, init_I(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()));
         }
      }

      String best = null;

      for (Entry<String, Integer> entry : byPlanks.entrySet()) {
         if (best == null || entry.getValue() > byPlanks.get(best)) {
            best = entry.getKey();
         }
      }

      if (best == null) {
         return null;
      }

      int each = perLog.get(best);
      int logs = Math.min(byPlanks.get(best), Math.max(1, (wanted + each - 1) / each));

      try {
         return e.init(ag.init_JsonObject_ObjArr("item", best, "count", logs * each, "additional", true, "timeoutSeconds", 20));
      } catch (IllegalArgumentException e) {
         return null;
      }
   }
}
