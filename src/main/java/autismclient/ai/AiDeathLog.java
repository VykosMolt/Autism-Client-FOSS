package autismclient.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

final class AiDeathLog {
   static final long init_J = 300000L;
   private static volatile AiDeathLog.Inner_DataUtil init_j_DataUtil;
   private static volatile boolean init_Z;

   private AiDeathLog() {
   }

   static void init(Minecraft mc) {
      if (mc.player != null && mc.level != null && (init_j_DataUtil == null || init_j_DataUtil.ageMillis() >= 2000L)) {
         Map<String, Integer> carried = new LinkedHashMap<>();

         for (int slot = 0; slot < mc.player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = mc.player.getInventory().getItem(slot);
            if (!stack.isEmpty()) {
               carried.merge(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), stack.getCount(), Integer::sum);
            }
         }

         init_j_DataUtil = new AiDeathLog.Inner_DataUtil(
            System.currentTimeMillis(), mc.level.dimension().identifier().toString(), mc.player.blockPosition().immutable(), mc.player.experienceLevel, carried
         );
         init_Z = false;
      }
   }

   static AiDeathLog.Inner_DataUtil lastDeath() {
      return init_j_DataUtil;
   }

   static boolean init_Z() {
      return init_Z;
   }

   static void init_V() {
      init_Z = true;
   }

   static void load() {
      init_j_DataUtil = null;
      init_Z = false;
   }

   static JsonObject statusJson() {
      AiDeathLog.Inner_DataUtil death = init_j_DataUtil;
      if (death == null) {
         return AiJson.objectOf("recorded", false);
      }

      JsonArray carried = new JsonArray();
      death.inventory().forEach((item, count) -> carried.add(AiJson.objectOf("item", item, "count", count)));
      return AiJson.objectOf(
         "recorded",
         true,
         "dimension",
         death.dimension(),
         "x",
         death.position().getX(),
         "y",
         death.position().getY(),
         "z",
         death.position().getZ(),
         "ageSeconds",
         death.ageMillis() / 1000L,
         "despawnInSeconds",
         death.remainingMillis() / 1000L,
         "expired",
         death.expired(),
         "recovered",
         init_Z,
         "experienceLevel",
         death.experienceLevel(),
         "carried",
         carried
      );
   }

   record Inner_DataUtil(long at, String dimension, BlockPos position, int experienceLevel, Map<String, Integer> inventory) {
      Inner_DataUtil {
         inventory = Map.copyOf(inventory);
      }

      long ageMillis() {
         return System.currentTimeMillis() - this.at;
      }

      long remainingMillis() {
         return Math.max(0L, 300000L - this.ageMillis());
      }

      boolean expired() {
         return this.remainingMillis() <= 0L;
      }
   }
}
