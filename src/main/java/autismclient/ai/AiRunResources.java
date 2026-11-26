package autismclient.ai;

import com.google.gson.JsonObject;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class AiRunResources {
   static final int init_I = 27;
   static final int load_I = 12;
   static final int read = 16;
   static final int write = 2;
   static final int update = 10;
   static final int sync = 64;
   static final int flush = 40;
   static final int close = 32;
   private static Object init_Obj;
   private static String init_Str;
   private static Object load_Obj;
   private static Map<String, Integer> init_Map = Map.of();
   private static Map<String, Integer> load_Map = Map.of();
   private static final Set<String> init_Set = Set.of(
      "minecraft:rotten_flesh",
      "minecraft:poisonous_potato",
      "minecraft:spider_eye",
      "minecraft:pufferfish",
      "minecraft:suspicious_stew",
      "minecraft:chorus_fruit"
   );

   private AiRunResources() {
   }

   static AiRunResources.Inner_DataUtil init(int target, int activation, int mobilityPearls) {
      int e = init("minecraft:ender_eye", 0);
      int p = Math.max(0, init("minecraft:ender_pearl", 0) - mobilityPearls);
      int b = init("minecraft:blaze_powder", 0);
      int r = init("minecraft:blaze_rod", 0);
      int equivalents = e + Math.min(p, b + 2 * r);
      return new AiRunResources.Inner_DataUtil(
         e, p, b, r, target, equivalents, Math.max(0, target - e - p), Math.max(0, target - e - b - 2 * r), Math.max(0, equivalents - activation)
      );
   }

   static int init_I() {
      return init("minecraft:ender_eye", 0) + init("minecraft:blaze_powder", 0) + 2 * init("minecraft:blaze_rod", 0);
   }

   static int init(String id, int durability) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return 0;
      }

      int count = 0;
      Inventory inventory = mc.player.getInventory();

      for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
         ItemStack stack = inventory.getItem(slot);
         if (id.equals(init_Str(stack)) && init(stack, durability)) {
            count += stack.getCount();
         }
      }

      return count;
   }

   static boolean init(ItemStack stack, int durability) {
      return AiItems.init(stack, durability);
   }

   private static String init_Str(ItemStack stack) {
      return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
   }

   static Map<String, Integer> init_Map() {
      Map<String, Integer> out = new LinkedHashMap<>();
      Map<String, Integer> kit = new LinkedHashMap<>();
      kit.put("minecraft:diamond_pickaxe", 1);
      kit.putAll(AiSpeedrun.init_Map);
      kit.forEach(
         (id, count) -> {
            int have = id.equals("minecraft:bucket")
               ? init(id, 0) + init("minecraft:water_bucket", 0) + init("minecraft:lava_bucket", 0)
               : load(id, id.contains("boots") ? 16 : (id.contains("diamond_") ? 32 : 0));
            if (have < count) {
               out.put(id, id.equals("minecraft:bucket") ? count : AiCraft.init_I(id) + count - have);
            }
         }
      );
      return out;
   }

   static String init_Str(String id) {
      return id != null && id.startsWith("minecraft:diamond_") ? "minecraft:netherite_" + id.substring("minecraft:diamond_".length()) : null;
   }

   static int load(String id, int durability) {
      String better = init_Str(id);
      return init(id, durability) + (better == null ? 0 : init(better, durability));
   }

   static boolean init_Z() {
      Minecraft mc = Minecraft.getInstance();
      return mc.player == null
         ? false
         : init(mc, EquipmentSlot.HEAD, "minecraft:diamond_helmet")
            && init(mc, EquipmentSlot.CHEST, "minecraft:diamond_chestplate")
            && init(mc, EquipmentSlot.LEGS, "minecraft:diamond_leggings")
            && init(mc, EquipmentSlot.FEET, "minecraft:golden_boots");
   }

   private static boolean init(Minecraft mc, EquipmentSlot slot, String id) {
      ItemStack stack = mc.player.getItemBySlot(slot);
      String wornId = init_Str(stack);
      return (id.equals(wornId) || wornId.equals(init_Str(id))) && init(stack, id.startsWith("minecraft:diamond_") ? 32 : 16);
   }

   static boolean init_Z(ItemStack stack) {
      String id = init_Str(stack);
      return stack.has(DataComponents.FOOD) && !init_Set.contains(id);
   }

   static int init_I(ItemStack stack) {
      return init_Z(stack) ? stack.getCount() : 0;
   }

   static int load_I() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return 0;
      }

      int total = 0;

      for (int slot = 0; slot < 36; slot++) {
         total += init_I(mc.player.getInventory().getItem(slot));
      }

      return total;
   }

   static int read() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return 0;
      }

      int total = 0;

      for (int slot = 0; slot < 36; slot++) {
         ItemStack stack = mc.player.getInventory().getItem(slot);
         FoodProperties food = stack.get(DataComponents.FOOD);
         String id = init_Str(stack);
         if (init_Z(stack) && !AiSpeedrun.read_Map.containsKey(id)) {
            total += food.nutrition() * stack.getCount();
         }
      }

      return total;
   }

   static int write() {
      return init("minecraft:arrow", 0) + init("minecraft:spectral_arrow", 0) + init("minecraft:tipped_arrow", 0);
   }

   static int update() {
      Minecraft mc = Minecraft.getInstance();
      Map<Item, Integer> amounts = new LinkedHashMap<>();
      if (mc.player != null) {
         for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = mc.player.getInventory().getItem(slot);
            if (AiTidy.init_Z(stack)) {
               amounts.merge(stack.getItem(), stack.getCount(), Integer::sum);
            }
         }
      }

      return amounts.entrySet().stream().mapToInt(entry -> Math.max(0, entry.getValue() - load(entry.getKey()))).sum();
   }

   static JsonObject init(boolean mandatoryKit) {
      Minecraft mc = Minecraft.getInstance();
      JsonObject out = new JsonObject();
      if (mc.player == null) {
         out.addProperty("player", "unavailable");
         return out;
      }

      if (mandatoryKit) {
         init_Map().forEach(out::addProperty);
         if (!init_Z()) {
            out.addProperty("equipment", "equip the required armor and golden boots");
         }
      }

      int water = init("minecraft:water_bucket", 0);
      if (water < 2) {
         out.addProperty("waterBuckets", 2 - water);
      }

      if (mc.player.getHealth() < 16.0F) {
         out.addProperty("health", 16.0F - mc.player.getHealth());
      }

      if (mc.player.getFoodData().getFoodLevel() < 18) {
         out.addProperty("hunger", 18 - mc.player.getFoodData().getFoodLevel());
      }

      if (read() < 32) {
         out.addProperty("foodNutrition", 32 - read());
      }

      if (update() < 32) {
         out.addProperty("buildingBlocks", 32 - update());
      }

      if (!AiForage.load()) {
         out.addProperty("fightSupplies", "four beds or a usable bow with twelve arrows");
      }

      if (init("minecraft:bow", 24) == 0 || write() < 12) {
         out.addProperty("crystalFallback", "a usable bow and at least twelve arrows, in addition to the perch weapon");
      }

      if (load("minecraft:diamond_axe", 16) == 0 && init("minecraft:iron_sword", 16) == 0 && load("minecraft:diamond_sword", 16) == 0) {
         out.addProperty("fallbackWeapon", "a usable axe or sword for the perch and covered crystal route");
      }

      return out;
   }

   static Object init_Obj() {
      init_Obj = new Object();
      init_Str = AiWorldIdentity.init(Minecraft.getInstance());
      load_Obj = Minecraft.getInstance().getConnection();
      init_V();
      return init_Obj;
   }

   static boolean load_Z() {
      Minecraft mc = Minecraft.getInstance();
      return init_Obj != null && mc.player != null && mc.level != null && load_Obj == mc.getConnection() && init_Str.equals(AiWorldIdentity.init(mc));
   }

   static void init_V() {
      if (load_Z()) {
         Map<String, Integer> next = new LinkedHashMap<>();
         next.put("minecraft:obsidian", 10);
         next.put("minecraft:diamond", init_Map().isEmpty() ? 0 : init("minecraft:diamond", 0));

         for (String id : AiSpeedrun.init_Map.keySet()) {
            next.put(id, id.equals("minecraft:bucket") ? 3 : 1);
            String better = init_Str(id);
            if (better != null) {
               next.put(better, 1);
            }
         }

         next.put("minecraft:diamond_pickaxe", 1);
         next.put("minecraft:netherite_pickaxe", 1);

         for (String id : List.of(
            "minecraft:water_bucket",
            "minecraft:lava_bucket",
            "minecraft:ender_eye",
            "minecraft:ender_pearl",
            "minecraft:blaze_powder",
            "minecraft:blaze_rod",
            "minecraft:fire_charge"
         )) {
            next.put(id, init(id, 0));
         }

         if (!init_Map().isEmpty()) {
            for (String id : List.of(
               "minecraft:iron_ingot",
               "minecraft:raw_iron",
               "minecraft:flint",
               "minecraft:stick",
               "minecraft:gold_ingot",
               "minecraft:raw_gold",
               "minecraft:gold_nugget"
            )) {
               next.put(id, init(id, 0));
            }
         }

         if (init("minecraft:golden_boots", 16) == 0) {
            next.merge("minecraft:gold_ingot", 4, Math::max);
         }

         for (String id : List.of(
            "minecraft:bow", "minecraft:arrow", "minecraft:spectral_arrow", "minecraft:tipped_arrow", "minecraft:string", "minecraft:feather"
         )) {
            next.put(id, init(id, 0));
         }

         Minecraft mc = Minecraft.getInstance();

         for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = mc.player.getInventory().getItem(slot);
            String itemId = init_Str(stack);
            if (init_Z(stack) || itemId.endsWith("_bed") || itemId.endsWith("_wool")) {
               next.put(itemId, init(itemId, 0));
            }
         }

         init_Map = Map.copyOf(next);
         Map<String, Integer> building = new LinkedHashMap<>();

         for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = mc.player.getInventory().getItem(slot);
            if (AiTidy.init_Z(stack)) {
               building.merge(init_Str(stack), stack.getCount(), Integer::sum);
            }
         }

         Map<String, Integer> protectedBlocks = new LinkedHashMap<>();
         int remaining = 64;

         for (Entry<String, Integer> entry : building.entrySet()
            .stream()
            .sorted(
               Comparator.<Entry<String, Integer>>comparingInt(e -> Math.max(0, e.getValue() - next.getOrDefault(e.getKey(), 0)))
                  .reversed()
                  .thenComparing(Entry::getKey)
            )
            .toList()) {
            int keep = Math.min(remaining, Math.max(0, entry.getValue() - next.getOrDefault(entry.getKey(), 0)));
            if (keep > 0) {
               protectedBlocks.put(entry.getKey(), keep);
            }

            remaining -= keep;
            if (remaining == 0) {
               break;
            }
         }

         load_Map = Map.copyOf(protectedBlocks);
      }
   }

   static int init(Item item) {
      String key = BuiltInRegistries.ITEM.getKey(item).toString();
      return load_Z() ? init_Map.getOrDefault(key, 0) + load_Map.getOrDefault(key, 0) : 0;
   }

   public static int load(Item item) {
      return load_Z() ? init_Map.getOrDefault(BuiltInRegistries.ITEM.getKey(item).toString(), 0) : 0;
   }

   static int init_I(String id) {
      return load_Z() ? init_Map.getOrDefault(id, 0) : 0;
   }

   static int load_I(String id) {
      return Math.max(0, init(id, 0) - (load_Z() ? init_Map.getOrDefault(id, 0) : 0));
   }

   static JsonObject init_JsonObject(String json) {
      try {
         return json != null && !json.isBlank() ? AiJson.init_JsonObject_Str(json) : new JsonObject();
      } catch (RuntimeException malformed) {
         return AiJson.init_JsonObject_ObjArr("unparsedEvidence", json == null ? "" : AiSpeedrun.init_Str_Str(json));
      }
   }

   static JsonObject load_JsonObject(String json) {
      JsonObject out = init_JsonObject(json).deepCopy();
      out.remove("steps");
      out.remove("trail");
      out.remove("transfers");
      return out.toString().length() <= 12000 ? out : AiJson.init_JsonObject_ObjArr("detail", AiSpeedrun.init_Str_Str(json), "evidenceTruncated", true);
   }

   static void init(Object token) {
      if (init_Obj == token) {
         init_Obj = null;
         init_Str = null;
         load_Obj = null;
         init_Map = Map.of();
         load_Map = Map.of();
      }
   }

   static JsonObject init_JsonObject() {
      Minecraft mc = Minecraft.getInstance();
      JsonObject inventory = new JsonObject();
      JsonObject held = new JsonObject();
      if (mc.player != null) {
         for (int slot = 0; slot < mc.player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = mc.player.getInventory().getItem(slot);
            if (!stack.isEmpty()) {
               inventory.addProperty(init_Str(stack), AiJson.init_I_JsonObject(inventory, init_Str(stack), 0) + stack.getCount());
            }
         }

         ItemStack cursor = mc.player.containerMenu.getCarried();
         if (!cursor.isEmpty()) {
            held.addProperty(init_Str(cursor), cursor.getCount());
         }
      }

      return AiJson.init_JsonObject_ObjArr(
         "observed",
         inventory,
         "campaignActive",
         load_Z(),
         "reserved",
         AiJson.init.toJsonTree(load_Z() ? init_Map : Map.of()),
         "cleanupRouteBlocks",
         AiJson.init.toJsonTree(load_Z() ? load_Map : Map.of()),
         "inTransit",
         held,
         "missingGear",
         AiJson.init.toJsonTree(init_Map()),
         "armorEquipped",
         init_Z(),
         "convertiblePowder",
         2 * init("minecraft:blaze_rod", 0),
         "activationEyeReserve",
         12,
         "eyes",
         init(16, 12, 0).data(),
         "foodNutrition",
         read(),
         "buildingBlocks",
         update(),
         "inventoryRevision",
         AiServerEvidence.init_J()
      );
   }

   record Inner_DataUtil(int eyes, int pearls, int powder, int rods, int target, int equivalents, int pearlDeficit, int powderDeficit, int navigationSpare) {
      JsonObject data() {
         return AiJson.init_JsonObject_ObjArr(
            "eyes",
            this.eyes,
            "pearls",
            this.pearls,
            "powder",
            this.powder,
            "rods",
            this.rods,
            "target",
            this.target,
            "eyeEquivalents",
            this.equivalents,
            "missingPearls",
            this.pearlDeficit,
            "missingPowder",
            this.powderDeficit,
            "navigationSpare",
            this.navigationSpare
         );
      }
   }
}
