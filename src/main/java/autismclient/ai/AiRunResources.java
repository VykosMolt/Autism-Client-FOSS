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
   private static Object campaignToken;
   private static String campaignWorldId;
   private static Object campaignConnection;
   private static Map<String, Integer> reservedItems = Map.of();
   private static Map<String, Integer> protectedBuildingBlocks = Map.of();
   private static final Set<String> UNSAFE_FOODS = Set.of(
      "minecraft:rotten_flesh",
      "minecraft:poisonous_potato",
      "minecraft:spider_eye",
      "minecraft:pufferfish",
      "minecraft:suspicious_stew",
      "minecraft:chorus_fruit"
   );

   private AiRunResources() {
   }

   static AiRunResources.EyeBudget eyeBudget(int target, int activation, int mobilityPearls) {
      int e = countUsableItem("minecraft:ender_eye", 0);
      int p = Math.max(0, countUsableItem("minecraft:ender_pearl", 0) - mobilityPearls);
      int b = countUsableItem("minecraft:blaze_powder", 0);
      int r = countUsableItem("minecraft:blaze_rod", 0);
      int equivalents = e + Math.min(p, b + 2 * r);
      return new AiRunResources.EyeBudget(
         e, p, b, r, target, equivalents, Math.max(0, target - e - p), Math.max(0, target - e - b - 2 * r), Math.max(0, equivalents - activation)
      );
   }

   static int blazePowderEquivalents() {
      return countUsableItem("minecraft:ender_eye", 0) + countUsableItem("minecraft:blaze_powder", 0) + 2 * countUsableItem("minecraft:blaze_rod", 0);
   }

   static int countUsableItem(String id, int durability) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return 0;
      }

      int count = 0;
      Inventory inventory = mc.player.getInventory();

      for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
         ItemStack stack = inventory.getItem(slot);
         if (id.equals(itemIdOf(stack)) && hasMinDurability(stack, durability)) {
            count += stack.getCount();
         }
      }

      return count;
   }

   static boolean hasMinDurability(ItemStack stack, int durability) {
      return AiItems.hasEnoughDurability(stack, durability);
   }

   private static String itemIdOf(ItemStack stack) {
      return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
   }

   static Map<String, Integer> missingMandatoryGear() {
      Map<String, Integer> out = new LinkedHashMap<>();
      Map<String, Integer> kit = new LinkedHashMap<>();
      kit.put("minecraft:diamond_pickaxe", 1);
      kit.putAll(AiSpeedrun.KIT);
      kit.forEach(
         (id, count) -> {
            int have = id.equals("minecraft:bucket")
               ? countUsableItem(id, 0) + countUsableItem("minecraft:water_bucket", 0) + countUsableItem("minecraft:lava_bucket", 0)
               : countTierOrNetherite(id, id.contains("boots") ? 16 : (id.contains("diamond_") ? 32 : 0));
            if (have < count) {
               out.put(id, id.equals("minecraft:bucket") ? count : AiCraft.countItem(id) + count - have);
            }
         }
      );
      return out;
   }

   static String netheriteUpgradeId(String id) {
      return id != null && id.startsWith("minecraft:diamond_") ? "minecraft:netherite_" + id.substring("minecraft:diamond_".length()) : null;
   }

   static int countTierOrNetherite(String id, int durability) {
      String better = netheriteUpgradeId(id);
      return countUsableItem(id, durability) + (better == null ? 0 : countUsableItem(better, durability));
   }

   static boolean isMandatoryArmorEquipped() {
      Minecraft mc = Minecraft.getInstance();
      return mc.player == null
         ? false
         : isSlotEquippedWith(mc, EquipmentSlot.HEAD, "minecraft:diamond_helmet")
            && isSlotEquippedWith(mc, EquipmentSlot.CHEST, "minecraft:diamond_chestplate")
            && isSlotEquippedWith(mc, EquipmentSlot.LEGS, "minecraft:diamond_leggings")
            && isSlotEquippedWith(mc, EquipmentSlot.FEET, "minecraft:golden_boots");
   }

   private static boolean isSlotEquippedWith(Minecraft mc, EquipmentSlot slot, String id) {
      ItemStack stack = mc.player.getItemBySlot(slot);
      String wornId = itemIdOf(stack);
      return (id.equals(wornId) || wornId.equals(netheriteUpgradeId(id))) && hasMinDurability(stack, id.startsWith("minecraft:diamond_") ? 32 : 16);
   }

   static boolean isSafeFood(ItemStack stack) {
      String id = itemIdOf(stack);
      return stack.has(DataComponents.FOOD) && !UNSAFE_FOODS.contains(id);
   }

   static int safeFoodCount(ItemStack stack) {
      return isSafeFood(stack) ? stack.getCount() : 0;
   }

   static int totalSafeFoodItems() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return 0;
      }

      int total = 0;

      for (int slot = 0; slot < 36; slot++) {
         total += safeFoodCount(mc.player.getInventory().getItem(slot));
      }

      return total;
   }

   static int totalSafeFoodNutrition() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return 0;
      }

      int total = 0;

      for (int slot = 0; slot < 36; slot++) {
         ItemStack stack = mc.player.getInventory().getItem(slot);
         FoodProperties food = stack.get(DataComponents.FOOD);
         String id = itemIdOf(stack);
         if (isSafeFood(stack) && !AiSpeedrun.COOKED.containsKey(id)) {
            total += food.nutrition() * stack.getCount();
         }
      }

      return total;
   }

   static int totalArrowCount() {
      return countUsableItem("minecraft:arrow", 0) + countUsableItem("minecraft:spectral_arrow", 0) + countUsableItem("minecraft:tipped_arrow", 0);
   }

   static int spareBuildingBlockCount() {
      Minecraft mc = Minecraft.getInstance();
      Map<Item, Integer> amounts = new LinkedHashMap<>();
      if (mc.player != null) {
         for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = mc.player.getInventory().getItem(slot);
            if (AiTidy.isFillerBlock(stack)) {
               amounts.merge(stack.getItem(), stack.getCount(), Integer::sum);
            }
         }
      }

      return amounts.entrySet().stream().mapToInt(entry -> Math.max(0, entry.getValue() - mandatoryReservedCount(entry.getKey()))).sum();
   }

   static JsonObject readinessDeficits(boolean mandatoryKit) {
      Minecraft mc = Minecraft.getInstance();
      JsonObject out = new JsonObject();
      if (mc.player == null) {
         out.addProperty("player", "unavailable");
         return out;
      }

      if (mandatoryKit) {
         missingMandatoryGear().forEach(out::addProperty);
         if (!isMandatoryArmorEquipped()) {
            out.addProperty("equipment", "equip the required armor and golden boots");
         }
      }

      int water = countUsableItem("minecraft:water_bucket", 0);
      if (water < 2) {
         out.addProperty("waterBuckets", 2 - water);
      }

      if (mc.player.getHealth() < 16.0F) {
         out.addProperty("health", 16.0F - mc.player.getHealth());
      }

      if (mc.player.getFoodData().getFoodLevel() < 18) {
         out.addProperty("hunger", 18 - mc.player.getFoodData().getFoodLevel());
      }

      if (totalSafeFoodNutrition() < 32) {
         out.addProperty("foodNutrition", 32 - totalSafeFoodNutrition());
      }

      if (spareBuildingBlockCount() < 32) {
         out.addProperty("buildingBlocks", 32 - spareBuildingBlockCount());
      }

      if (!AiForage.hasFightPlan()) {
         out.addProperty("fightSupplies", "four beds or a usable bow with twelve arrows");
      }

      if (countUsableItem("minecraft:bow", 24) == 0 || totalArrowCount() < 12) {
         out.addProperty("crystalFallback", "a usable bow and at least twelve arrows, in addition to the perch weapon");
      }

      if (countTierOrNetherite("minecraft:diamond_axe", 16) == 0 && countUsableItem("minecraft:iron_sword", 16) == 0 && countTierOrNetherite("minecraft:diamond_sword", 16) == 0) {
         out.addProperty("fallbackWeapon", "a usable axe or sword for the perch and covered crystal route");
      }

      return out;
   }

   static Object beginCampaign() {
      campaignToken = new Object();
      campaignWorldId = AiWorldIdentity.currentWorldKey(Minecraft.getInstance());
      campaignConnection = Minecraft.getInstance().getConnection();
      refreshReservations();
      return campaignToken;
   }

   static boolean isCampaignActive() {
      Minecraft mc = Minecraft.getInstance();
      return campaignToken != null && mc.player != null && mc.level != null && campaignConnection == mc.getConnection() && campaignWorldId.equals(AiWorldIdentity.currentWorldKey(mc));
   }

   static void refreshReservations() {
      if (isCampaignActive()) {
         Map<String, Integer> next = new LinkedHashMap<>();
         next.put("minecraft:obsidian", 10);
         next.put("minecraft:diamond", missingMandatoryGear().isEmpty() ? 0 : countUsableItem("minecraft:diamond", 0));

         for (String id : AiSpeedrun.KIT.keySet()) {
            next.put(id, id.equals("minecraft:bucket") ? 3 : 1);
            String better = netheriteUpgradeId(id);
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
            next.put(id, countUsableItem(id, 0));
         }

         if (!missingMandatoryGear().isEmpty()) {
            for (String id : List.of(
               "minecraft:iron_ingot",
               "minecraft:raw_iron",
               "minecraft:flint",
               "minecraft:stick",
               "minecraft:gold_ingot",
               "minecraft:raw_gold",
               "minecraft:gold_nugget"
            )) {
               next.put(id, countUsableItem(id, 0));
            }
         }

         if (countUsableItem("minecraft:golden_boots", 16) == 0) {
            next.merge("minecraft:gold_ingot", 4, Math::max);
         }

         for (String id : List.of(
            "minecraft:bow", "minecraft:arrow", "minecraft:spectral_arrow", "minecraft:tipped_arrow", "minecraft:string", "minecraft:feather"
         )) {
            next.put(id, countUsableItem(id, 0));
         }

         Minecraft mc = Minecraft.getInstance();

         for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = mc.player.getInventory().getItem(slot);
            String itemId = itemIdOf(stack);
            if (isSafeFood(stack) || itemId.endsWith("_bed") || itemId.endsWith("_wool")) {
               next.put(itemId, countUsableItem(itemId, 0));
            }
         }

         reservedItems = Map.copyOf(next);
         Map<String, Integer> building = new LinkedHashMap<>();

         for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = mc.player.getInventory().getItem(slot);
            if (AiTidy.isFillerBlock(stack)) {
               building.merge(itemIdOf(stack), stack.getCount(), Integer::sum);
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

         protectedBuildingBlocks = Map.copyOf(protectedBlocks);
      }
   }

   static int totalReservedCount(Item item) {
      String key = BuiltInRegistries.ITEM.getKey(item).toString();
      return isCampaignActive() ? reservedItems.getOrDefault(key, 0) + protectedBuildingBlocks.getOrDefault(key, 0) : 0;
   }

   public static int mandatoryReservedCount(Item item) {
      return isCampaignActive() ? reservedItems.getOrDefault(BuiltInRegistries.ITEM.getKey(item).toString(), 0) : 0;
   }

   static int reservedItemCount(String id) {
      return isCampaignActive() ? reservedItems.getOrDefault(id, 0) : 0;
   }

   static int unreservedItemCount(String id) {
      return Math.max(0, countUsableItem(id, 0) - (isCampaignActive() ? reservedItems.getOrDefault(id, 0) : 0));
   }

   static JsonObject parseEvidenceJson(String json) {
      try {
         return json != null && !json.isBlank() ? AiJson.parseObject(json) : new JsonObject();
      } catch (RuntimeException malformed) {
         return AiJson.objectOf("unparsedEvidence", json == null ? "" : AiSpeedrun.clip(json));
      }
   }

   static JsonObject compactEvidenceJson(String json) {
      JsonObject out = parseEvidenceJson(json).deepCopy();
      out.remove("steps");
      out.remove("trail");
      out.remove("transfers");
      return out.toString().length() <= 12000 ? out : AiJson.objectOf("detail", AiSpeedrun.clip(json), "evidenceTruncated", true);
   }

   static void endCampaign(Object token) {
      if (campaignToken == token) {
         campaignToken = null;
         campaignWorldId = null;
         campaignConnection = null;
         reservedItems = Map.of();
         protectedBuildingBlocks = Map.of();
      }
   }

   static JsonObject inventorySnapshotJson() {
      Minecraft mc = Minecraft.getInstance();
      JsonObject inventory = new JsonObject();
      JsonObject held = new JsonObject();
      if (mc.player != null) {
         for (int slot = 0; slot < mc.player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = mc.player.getInventory().getItem(slot);
            if (!stack.isEmpty()) {
               inventory.addProperty(itemIdOf(stack), AiJson.getInt(inventory, itemIdOf(stack), 0) + stack.getCount());
            }
         }

         ItemStack cursor = mc.player.containerMenu.getCarried();
         if (!cursor.isEmpty()) {
            held.addProperty(itemIdOf(cursor), cursor.getCount());
         }
      }

      return AiJson.objectOf(
         "observed",
         inventory,
         "campaignActive",
         isCampaignActive(),
         "reserved",
         AiJson.GSON.toJsonTree(isCampaignActive() ? reservedItems : Map.of()),
         "cleanupRouteBlocks",
         AiJson.GSON.toJsonTree(isCampaignActive() ? protectedBuildingBlocks : Map.of()),
         "inTransit",
         held,
         "missingGear",
         AiJson.GSON.toJsonTree(missingMandatoryGear()),
         "armorEquipped",
         isMandatoryArmorEquipped(),
         "convertiblePowder",
         2 * countUsableItem("minecraft:blaze_rod", 0),
         "activationEyeReserve",
         12,
         "eyes",
         eyeBudget(16, 12, 0).data(),
         "foodNutrition",
         totalSafeFoodNutrition(),
         "buildingBlocks",
         spareBuildingBlockCount(),
         "inventoryRevision",
         AiServerEvidence.inventoryRevision()
      );
   }

   record EyeBudget(int eyes, int pearls, int powder, int rods, int target, int equivalents, int pearlDeficit, int powderDeficit, int navigationSpare) {
      JsonObject data() {
         return AiJson.objectOf(
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
