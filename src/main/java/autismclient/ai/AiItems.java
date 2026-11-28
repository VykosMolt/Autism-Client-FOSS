package autismclient.ai;

import autismclient.util.BlockBreakSpeedHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class AiItems {
   private static final Map<String, List<String>> FAMILY_ALIASES = new LinkedHashMap<>();
   private static final Set<String> DYNAMIC_FAMILIES = Set.of("food", "fuel");
   private static final int init_I = 32;
   private static final Map<String, AiItems.ItemSpec> SPEC_CACHE = new LinkedHashMap<String, AiItems.ItemSpec>(16, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<String, AiItems.ItemSpec> eldest) {
         return this.size() > 32;
      }
   };
   private static Object cachedLevel;
   private static final Map<String, List<String>> TAG_FALLBACK_SUFFIXES = Map.ofEntries(
      Map.entry("logs", List.of("_log", "_wood", "_stem", "_hyphae")),
      Map.entry("planks", List.of("_planks")),
      Map.entry("saplings", List.of("_sapling")),
      Map.entry("leaves", List.of("_leaves")),
      Map.entry("wool", List.of("_wool")),
      Map.entry("beds", List.of("_bed")),
      Map.entry("boats", List.of("_boat", "_raft")),
      Map.entry("doors", List.of("_door")),
      Map.entry("fences", List.of("_fence")),
      Map.entry("walls", List.of("_wall")),
      Map.entry("slabs", List.of("_slab")),
      Map.entry("stairs", List.of("_stairs")),
      Map.entry("dirt", List.of("=dirt", "=coarse_dirt", "=rooted_dirt", "=grass_block", "=podzol", "=mycelium", "=moss_block", "=mud")),
      Map.entry("sand", List.of("=sand", "=red_sand")),
      Map.entry("pickaxes", List.of("_pickaxe")),
      Map.entry("axes", List.of("_axe")),
      Map.entry("shovels", List.of("_shovel")),
      Map.entry("hoes", List.of("_hoe")),
      Map.entry("swords", List.of("_sword")),
      Map.entry("head_armor", List.of("_helmet", "=turtle_helmet")),
      Map.entry("chest_armor", List.of("_chestplate", "=elytra")),
      Map.entry("leg_armor", List.of("_leggings")),
      Map.entry("foot_armor", List.of("_boots"))
   );
   private static final List<String> FALLBACK_FUELS = List.of(
      "minecraft:coal",
      "minecraft:charcoal",
      "minecraft:coal_block",
      "minecraft:blaze_rod",
      "minecraft:lava_bucket",
      "minecraft:dried_kelp_block",
      "minecraft:stick"
   );
   private static final List<String> FALLBACK_FOODS = List.of(
      "minecraft:bread",
      "minecraft:cooked_beef",
      "minecraft:cooked_porkchop",
      "minecraft:cooked_chicken",
      "minecraft:cooked_mutton",
      "minecraft:cooked_rabbit",
      "minecraft:cooked_cod",
      "minecraft:cooked_salmon",
      "minecraft:baked_potato",
      "minecraft:golden_carrot",
      "minecraft:carrot",
      "minecraft:potato",
      "minecraft:apple",
      "minecraft:golden_apple",
      "minecraft:melon_slice",
      "minecraft:sweet_berries",
      "minecraft:beef",
      "minecraft:porkchop",
      "minecraft:chicken",
      "minecraft:mutton",
      "minecraft:rabbit",
      "minecraft:cod",
      "minecraft:salmon",
      "minecraft:beetroot",
      "minecraft:dried_kelp",
      "minecraft:cookie",
      "minecraft:pumpkin_pie",
      "minecraft:mushroom_stew",
      "minecraft:rabbit_stew",
      "minecraft:beetroot_soup"
   );
   private static final List<String> TOOL_PROGRESSION = List.of(
      "minecraft:wooden_pickaxe",
      "minecraft:wooden_shovel",
      "minecraft:wooden_axe",
      "minecraft:wooden_hoe",
      "minecraft:stone_pickaxe",
      "minecraft:stone_shovel",
      "minecraft:stone_axe",
      "minecraft:stone_hoe",
      "minecraft:iron_pickaxe",
      "minecraft:iron_shovel",
      "minecraft:iron_axe",
      "minecraft:iron_hoe",
      "minecraft:diamond_pickaxe",
      "minecraft:diamond_shovel",
      "minecraft:diamond_axe",
      "minecraft:diamond_hoe",
      "minecraft:netherite_pickaxe",
      "minecraft:netherite_shovel",
      "minecraft:netherite_axe",
      "minecraft:netherite_hoe",
      "minecraft:shears"
   );
   private static Set<String> directMineableItems;
   private static final Set<String> SILK_OR_UNOBTAINABLE_BLOCKS = Set.of(
      "minecraft:ice",
      "minecraft:packed_ice",
      "minecraft:blue_ice",
      "minecraft:budding_amethyst",
      "minecraft:sculk",
      "minecraft:sculk_vein",
      "minecraft:sculk_sensor",
      "minecraft:calibrated_sculk_sensor",
      "minecraft:sculk_shrieker",
      "minecraft:sculk_catalyst",
      "minecraft:turtle_egg",
      "minecraft:spawner",
      "minecraft:reinforced_deepslate",
      "minecraft:suspicious_sand",
      "minecraft:suspicious_gravel"
   );
   private static final Map<String, List<String>> ITEM_SOURCE_BLOCKS = Map.ofEntries(
      Map.entry("minecraft:cobblestone", List.of("minecraft:stone")),
      Map.entry("minecraft:cobbled_deepslate", List.of("minecraft:deepslate")),
      Map.entry("minecraft:raw_iron", List.of("minecraft:iron_ore", "minecraft:deepslate_iron_ore")),
      Map.entry("minecraft:raw_copper", List.of("minecraft:copper_ore", "minecraft:deepslate_copper_ore")),
      Map.entry("minecraft:raw_gold", List.of("minecraft:gold_ore", "minecraft:deepslate_gold_ore")),
      Map.entry("minecraft:gold_nugget", List.of("minecraft:nether_gold_ore")),
      Map.entry("minecraft:coal", List.of("minecraft:coal_ore", "minecraft:deepslate_coal_ore")),
      Map.entry("minecraft:diamond", List.of("minecraft:diamond_ore", "minecraft:deepslate_diamond_ore")),
      Map.entry("minecraft:emerald", List.of("minecraft:emerald_ore", "minecraft:deepslate_emerald_ore")),
      Map.entry("minecraft:redstone", List.of("minecraft:redstone_ore", "minecraft:deepslate_redstone_ore")),
      Map.entry("minecraft:lapis_lazuli", List.of("minecraft:lapis_ore", "minecraft:deepslate_lapis_ore")),
      Map.entry("minecraft:quartz", List.of("minecraft:nether_quartz_ore")),
      Map.entry("minecraft:clay_ball", List.of("minecraft:clay")),
      Map.entry("minecraft:flint", List.of("minecraft:gravel")),
      Map.entry("minecraft:wheat", List.of("minecraft:wheat")),
      Map.entry("minecraft:wheat_seeds", List.of("minecraft:wheat", "minecraft:short_grass", "minecraft:tall_grass", "minecraft:fern", "minecraft:large_fern")),
      Map.entry("minecraft:carrot", List.of("minecraft:carrots")),
      Map.entry("minecraft:potato", List.of("minecraft:potatoes")),
      Map.entry("minecraft:beetroot", List.of("minecraft:beetroots")),
      Map.entry("minecraft:melon_slice", List.of("minecraft:melon")),
      Map.entry("minecraft:sugar_cane", List.of("minecraft:sugar_cane")),
      Map.entry("minecraft:bamboo", List.of("minecraft:bamboo")),
      Map.entry("minecraft:apple", List.of("minecraft:oak_leaves", "minecraft:dark_oak_leaves")),
      Map.entry("minecraft:stick", List.of("minecraft:dead_bush")),
      Map.entry("minecraft:glowstone_dust", List.of("minecraft:glowstone")),
      Map.entry("minecraft:string", List.of("minecraft:cobweb")),
      Map.entry("minecraft:snowball", List.of("minecraft:snow", "minecraft:snow_block"))
   );
   private static final Map<String, List<String>> ITEM_SOURCE_MOBS = Map.ofEntries(
      Map.entry("minecraft:ender_pearl", List.of("minecraft:enderman")),
      Map.entry("minecraft:blaze_rod", List.of("minecraft:blaze")),
      Map.entry("minecraft:string", List.of("minecraft:spider", "minecraft:cave_spider")),
      Map.entry("minecraft:spider_eye", List.of("minecraft:spider", "minecraft:cave_spider")),
      Map.entry("minecraft:bone", List.of("minecraft:skeleton")),
      Map.entry("minecraft:arrow", List.of("minecraft:skeleton")),
      Map.entry("minecraft:gunpowder", List.of("minecraft:creeper", "minecraft:witch", "minecraft:ghast")),
      Map.entry("minecraft:rotten_flesh", List.of("minecraft:zombie", "minecraft:husk", "minecraft:drowned")),
      Map.entry("minecraft:leather", List.of("minecraft:cow", "minecraft:horse", "minecraft:llama")),
      Map.entry("minecraft:beef", List.of("minecraft:cow")),
      Map.entry("minecraft:porkchop", List.of("minecraft:pig")),
      Map.entry("minecraft:chicken", List.of("minecraft:chicken")),
      Map.entry("minecraft:feather", List.of("minecraft:chicken")),
      Map.entry("minecraft:mutton", List.of("minecraft:sheep")),
      Map.entry("minecraft:white_wool", List.of("minecraft:sheep")),
      Map.entry("minecraft:light_gray_wool", List.of("minecraft:sheep")),
      Map.entry("minecraft:gray_wool", List.of("minecraft:sheep")),
      Map.entry("minecraft:black_wool", List.of("minecraft:sheep")),
      Map.entry("minecraft:brown_wool", List.of("minecraft:sheep")),
      Map.entry("minecraft:pink_wool", List.of("minecraft:sheep")),
      Map.entry("minecraft:rabbit", List.of("minecraft:rabbit")),
      Map.entry("minecraft:cod", List.of("minecraft:cod")),
      Map.entry("minecraft:salmon", List.of("minecraft:salmon")),
      Map.entry("minecraft:slime_ball", List.of("minecraft:slime")),
      Map.entry("minecraft:magma_cream", List.of("minecraft:magma_cube")),
      Map.entry("minecraft:ghast_tear", List.of("minecraft:ghast")),
      Map.entry("minecraft:phantom_membrane", List.of("minecraft:phantom")),
      Map.entry("minecraft:ink_sac", List.of("minecraft:squid")),
      Map.entry("minecraft:prismarine_shard", List.of("minecraft:guardian", "minecraft:elder_guardian")),
      Map.entry("minecraft:wither_skeleton_skull", List.of("minecraft:wither_skeleton")),
      Map.entry("minecraft:shulker_shell", List.of("minecraft:shulker"))
   );

   private AiItems() {
   }

   public static AiItems.ItemSpec resolve(String spec) {
      String clean = normalizeSpec(spec);
      if (clean.isEmpty()) {
         throw new IllegalArgumentException("An item or block spec is required");
      }

      Minecraft mc = Minecraft.getInstance();
      Object level = mc == null ? null : mc.level;
      if (level != cachedLevel) {
         SPEC_CACHE.clear();
         cachedLevel = level;
      }

      AiItems.ItemSpec memoized = SPEC_CACHE.get(clean);
      if (memoized != null) {
         return memoized;
      }

      Set<Item> items = new LinkedHashSet<>();
      Set<Block> blocks = new LinkedHashSet<>();
      boolean[] usedFallback = new boolean[1];
      expandSpec(clean, items, blocks, 0, usedFallback);
      if (items.isEmpty() && blocks.isEmpty()) {
         String loose = resolveLooseSpec(clean);
         if (loose != null) {
            expandSpec(loose, items, blocks, 0, usedFallback);
         }
      }

      if (items.isEmpty() && blocks.isEmpty()) {
         throw new IllegalArgumentException(
            "Unknown item or block '"
               + spec
               + "'"
               + suggestNearMatches(clean)
               + ". Use a registry id (minecraft:oak_log), a tag (#minecraft:logs), or a family name ("
               + String.join(", ", FAMILY_ALIASES.keySet())
               + ", food, fuel)."
         );
      }

      AiItems.ItemSpec target = new AiItems.ItemSpec(clean, items, blocks);
      if (!usedFallback[0]) {
         SPEC_CACHE.put(clean, target);
      }

      return target;
   }

   static String normalizeSpec(String spec) {
      return autismclient.commands.RegistrySuggestions.normalize(spec);
   }

   static String resolveLooseSpec(String clean) {
      if (clean.startsWith("#")) {
         return null;
      }

      if (clean.indexOf(58) < 0) {
         for (String form : autismclient.commands.RegistrySuggestions.singularForms(clean)) {
            if (FAMILY_ALIASES.containsKey(form)) {
               return form;
            }
         }
      }

      Identifier id = autismclient.commands.RegistrySuggestions.parseId(clean);
      return id == null ? null : id.toString();
   }

   public static List<String> familyNames() {
      return List.copyOf(FAMILY_ALIASES.keySet());
   }

   public static AiItems.ItemSpec resolveItem(String spec) {
      AiItems.ItemSpec target = resolve(spec);
      if (target.items().isEmpty()) {
         throw new IllegalArgumentException("'" + spec + "' names blocks but no obtainable item");
      } else {
         return target;
      }
   }

   public static AiItems.ItemSpec resolveBlock(String spec) {
      AiItems.ItemSpec target = resolve(spec);
      if (target.blocks().isEmpty()) {
         throw new IllegalArgumentException("'" + spec + "' names items but no placeable or mineable block");
      } else {
         return target;
      }
   }

   private static void expandSpec(String spec, Set<Item> items, Set<Block> blocks, int depth, boolean[] usedFallback) {
      if (depth <= 4) {
         if (spec.indexOf(44) >= 0) {
            for (String part : spec.split(",")) {
               String one = part.replaceAll("^_+|_+$", "");
               if (!one.isEmpty()) {
                  expandSpec(one, items, blocks, depth + 1, usedFallback);
               }
            }
         } else if (spec.startsWith("#")) {
            expandTag(spec.substring(1), items, blocks, usedFallback);
         } else if (DYNAMIC_FAMILIES.contains(spec)) {
            expandDynamicFamily(spec, items, usedFallback);
         } else {
            Identifier id = Identifier.tryParse(spec.contains(":") ? spec : "minecraft:" + spec);
            boolean exact = false;
            if (id != null) {
               Optional<Item> item = BuiltInRegistries.ITEM.getOptional(id);
               Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(id);
               if (item.isPresent()) {
                  items.add(item.get());
                  exact = true;
               }

               if (block.isPresent()) {
                  blocks.add(block.get());
                  exact = true;
               }
            }

            if (!exact) {
               List<String> alias = FAMILY_ALIASES.get(spec);
               if (alias != null) {
                  for (String nested : alias) {
                     expandSpec(nested, items, blocks, depth + 1, usedFallback);
                  }
               }
            }
         }
      }
   }

   private static void expandTag(String tagSpec, Set<Item> items, Set<Block> blocks, boolean[] usedFallback) {
      Identifier id = Identifier.tryParse(tagSpec.contains(":") ? tagSpec : "minecraft:" + tagSpec);
      if (id != null) {
         TagKey<Item> itemTag = TagKey.create(Registries.ITEM, id);
         TagKey<Block> blockTag = TagKey.create(Registries.BLOCK, id);
         int before = items.size() + blocks.size();

         for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(itemTag)) {
            items.add(holder.value());
         }

         for (Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(blockTag)) {
            blocks.add(holder.value());
         }

         if (items.size() + blocks.size() == before) {
            usedFallback[0] = true;
            expandTagFallback(id.getPath(), items, blocks);
         }
      }
   }

   private static void expandTagFallback(String tagPath, Set<Item> items, Set<Block> blocks) {
      List<String> suffixes = TAG_FALLBACK_SUFFIXES.get(tagPath);
      if (suffixes != null) {
         for (Identifier key : BuiltInRegistries.ITEM.keySet()) {
            if (matchesAnySuffix(key.getPath(), suffixes)) {
               BuiltInRegistries.ITEM.getOptional(key).ifPresent(items::add);
            }
         }

         for (Identifier key : BuiltInRegistries.BLOCK.keySet()) {
            if (matchesAnySuffix(key.getPath(), suffixes)) {
               BuiltInRegistries.BLOCK.getOptional(key).ifPresent(blocks::add);
            }
         }
      }
   }

   private static boolean matchesAnySuffix(String path, List<String> suffixes) {
      for (String suffix : suffixes) {
         if (suffix.startsWith("=") ? path.equals(suffix.substring(1)) : path.endsWith(suffix)) {
            return true;
         }
      }

      return false;
   }

   private static void expandDynamicFamily(String spec, Set<Item> items, boolean[] usedFallback) {
      for (Identifier key : BuiltInRegistries.ITEM.keySet()) {
         Item item = BuiltInRegistries.ITEM.getOptional(key).orElse(null);
         if (item != null) {
            try {
               if (spec.equals("food")) {
                  if (item.components().has(DataComponents.FOOD) && item.components().has(DataComponents.CONSUMABLE)) {
                     items.add(item);
                  }
               } else if (spec.equals("fuel") && fuelBurnTime(new ItemStack(item)) > 0) {
                  items.add(item);
               }
            } catch (RuntimeException var7) {
            }
         }
      }

      if (items.isEmpty()) {
         usedFallback[0] = true;

         for (String fallback : spec.equals("fuel") ? FALLBACK_FUELS : FALLBACK_FOODS) {
            BuiltInRegistries.ITEM.getOptional(Identifier.parse(fallback)).ifPresent(items::add);
         }
      }
   }

   private static String suggestNearMatches(String spec) {
      List<String> near = new ArrayList<>();

      for (Identifier key : BuiltInRegistries.ITEM.keySet()) {
         if (key.getPath().contains(spec) || spec.contains(key.getPath())) {
            near.add(key.toString());
         }

         if (near.size() >= 5) {
            break;
         }
      }

      return near.isEmpty() ? "" : " (did you mean " + String.join(", ", near) + "?)";
   }

   public static int countInInventory(AiItems.ItemSpec target) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return 0;
      }

      int total = 0;

      for (int slot = 0; slot < mc.player.getInventory().getContainerSize(); slot++) {
         ItemStack stack = mc.player.getInventory().getItem(slot);
         if (target.matches(stack)) {
            total += stack.getCount();
         }
      }

      return total;
   }

   public static int countFreeSlots() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return 0;
      }

      int free = 0;

      for (int slot = 0; slot < 36; slot++) {
         if (mc.player.getInventory().getItem(slot).isEmpty()) {
            free++;
         }
      }

      return free;
   }

   public static int findSlot(AiItems.ItemSpec target, int limit) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return -1;
      }

      int bound = Math.min(limit, mc.player.getInventory().getContainerSize());

      for (int slot = 0; slot < bound; slot++) {
         if (target.matches(mc.player.getInventory().getItem(slot))) {
            return slot;
         }
      }

      return -1;
   }

   public static boolean isPlainItem(ItemStack stack) {
      return stack != null && !stack.isEmpty() && !stack.has(DataComponents.CUSTOM_NAME) && stack.getEnchantments().isEmpty();
   }

   public static int ensureInHotbar(AiItems.ItemSpec target) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return -1;
      }

      int hotbar = findSlot(target, 9);
      if (hotbar >= 0) {
         return hotbar;
      }

      int inventory = -1;

      for (int slot = 9; slot < 36; slot++) {
         if (target.matches(mc.player.getInventory().getItem(slot))) {
            inventory = slot;
            break;
         }
      }

      if (inventory < 0) {
         return -1;
      } else {
         int destination = pickHotbarDestination();
         if (!autismclient.util.InventorySlotHelper.swapToHotbar(mc, inventory, destination)) {
            return -1;
         } else {
            return target.matches(mc.player.getInventory().getItem(destination)) ? destination : -1;
         }
      }
   }

   public static int equipHotbar(AiItems.ItemSpec target) {
      Minecraft mc = Minecraft.getInstance();
      int slot = ensureInHotbar(target);
      if (slot >= 0 && mc.player != null) {
         autismclient.util.InventorySlotHelper.selectHotbarSlot(mc, slot);
         return slot;
      } else {
         return -1;
      }
   }

   public static boolean equipOffhand(AiItems.ItemSpec target) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return false;
      }

      if (target.matches(mc.player.getOffhandItem())) {
         return true;
      }

      int slot = findSlot(target, 36);
      return slot < 0 ? false : autismclient.util.InventorySlotHelper.swapToOffhand(mc, slot);
   }

   static int pickHotbarDestination() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return 8;
      }

      for (int slot = 0; slot < 9; slot++) {
         if (mc.player.getInventory().getItem(slot).isEmpty()) {
            return slot;
         }
      }

      int worst = 8;
      int worstScore = 2147483647;

      for (int slot = 0; slot < 9; slot++) {
         ItemStack stack = mc.player.getInventory().getItem(slot);
         int score = hotbarKeepScore(stack);
         if (score < worstScore) {
            worstScore = score;
            worst = slot;
         }
      }

      return worst;
   }

   private static int hotbarKeepScore(ItemStack stack) {
      if (stack.isEmpty()) {
         return 0;
      } else if (stack.has(DataComponents.FOOD)) {
         return 40;
      } else if (stack.isDamageableItem()) {
         return 60;
      } else {
         return stack.getItem() instanceof BlockItem ? 20 : 30;
      }
   }

   public static int bestHotbarToolFor(BlockState state) {
      return BlockBreakSpeedHelper.bestToolSlot(Minecraft.getInstance(), state, 9, false);
   }

   public static int bestToolFor(BlockState state, int limit) {
      return BlockBreakSpeedHelper.bestToolSlot(Minecraft.getInstance(), state, limit, false);
   }

   public static boolean hasUsableToolFor(BlockState state) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && state != null) {
         if (!state.requiresCorrectToolForDrops()) {
            return true;
         }

         for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = mc.player.getInventory().getItem(slot);
            if (stack.isCorrectToolForDrops(state) && hasEnoughDurability(stack, 2)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean hasEnoughDurability(ItemStack stack, int durability) {
      return !stack.isEmpty() && (!stack.isDamageableItem() || stack.getMaxDamage() - stack.getDamageValue() > (isDisposableTierTool(stack) ? 0 : durability));
   }

   public static boolean isDisposableTierTool(ItemStack stack) {
      if (stack != null && !stack.isEmpty() && stack.isDamageableItem() && isPlainItem(stack)) {
         String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
         return (path.startsWith("wooden_") || path.startsWith("stone_") || path.startsWith("iron_"))
            && (path.endsWith("_pickaxe") || path.endsWith("_axe") || path.endsWith("_shovel") || path.endsWith("_hoe") || path.endsWith("_sword"));
      } else {
         return false;
      }
   }

   public static Item cheapestRequiredTool(BlockState state) {
      if (state != null && state.requiresCorrectToolForDrops()) {
         for (String candidate : TOOL_PROGRESSION) {
            Item tool = BuiltInRegistries.ITEM.getOptional(Identifier.parse(candidate)).orElse(null);
            if (tool != null) {
               try {
                  if (new ItemStack(tool).isCorrectToolForDrops(state)) {
                     return tool;
                  }
               } catch (RuntimeException var5) {
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public static int bestHotbarWeaponSlot() {
      return bestWeaponSlot(9);
   }

   public static int bestWeaponSlot(int limit) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return -1;
      }

      AiItems.ItemSpec swords = resolve("#minecraft:swords");
      AiItems.ItemSpec axes = resolve("#minecraft:axes");
      int best = -1;
      double bestDamage = -1.0;

      for (int slot = 0; slot < Math.min(limit, 36); slot++) {
         ItemStack stack = mc.player.getInventory().getItem(slot);
         if (!stack.isEmpty()) {
            boolean weapon = swords.matches(stack) || axes.matches(stack);
            double damage = attackDamageBonus(stack) + (weapon ? 0.5 : 0.0);
            if (damage > bestDamage) {
               bestDamage = damage;
               best = slot;
            }
         }
      }

      return bestDamage > 0.0 ? best : -1;
   }

   public static int selectOrMoveToHotbar(int slot) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && slot >= 0 && slot < 36) {
         if (slot >= 9) {
            int destination = pickHotbarDestination();
            if (!autismclient.util.InventorySlotHelper.swapToHotbar(mc, slot, destination)) {
               return -1;
            }

            slot = destination;
         }

         autismclient.util.InventorySlotHelper.selectHotbarSlot(mc, slot);
         return slot;
      } else {
         return -1;
      }
   }

   public static double attackDamageBonus(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
         if (modifiers == null) {
            return 0.0;
         }

         double total = 0.0;

         for (net.minecraft.world.item.component.ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (entry.attribute().value() == Attributes.ATTACK_DAMAGE.value()) {
               total += entry.modifier().amount();
            }
         }

         return total;
      } else {
         return 0.0;
      }
   }

   public static int fuelBurnTime(ItemStack stack) {
      Minecraft mc = Minecraft.getInstance();
      if (stack != null && !stack.isEmpty() && mc.level != null) {
         CookingFuel fuel = stack.get(DataComponents.COOKING_FUEL);
         return fuel == null ? 0 : 200;
      } else {
         return 0;
      }
   }

   public static boolean canDropWantedItem(Minecraft mc, BlockState state, ItemStack tool, Set<Item> wanted) {
      if (!wanted.isEmpty() && mc.player != null && !mc.player.hasInfiniteMaterials()) {
         boolean silk = BlockBreakSpeedHelper.hasSilkTouch(mc, tool);

         for (Item item : wanted) {
            if (sourceBlocksFor(item).contains(state.getBlock())) {
               boolean blockForm = state.getBlock().asItem() == item;
               if (silk && blockForm || !silk && (!blockForm || directMineableItems().contains(BuiltInRegistries.ITEM.getKey(item).toString()))) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public static BiPredicate<BlockState, ItemStack> dropFilterFor(Minecraft mc, Set<Item> wanted) {
      if (!wanted.isEmpty() && mc.player != null && !mc.player.hasInfiniteMaterials()) {
         Set<String> mineable = directMineableItems();
         Map<Block, Integer> allowed = new HashMap<>();

         for (Item item : wanted) {
            boolean direct = mineable.contains(BuiltInRegistries.ITEM.getKey(item).toString());

            for (Block source : sourceBlocksFor(item)) {
               boolean blockForm = source.asItem() == item;
               int mask = (blockForm ? 2 : 0) | (blockForm && !direct ? 0 : 1);
               allowed.merge(source, mask, (a, b) -> a | b);
            }
         }

         Map<Block, Integer> facts = Map.copyOf(allowed);
         Holder<Enchantment> silk = null;

         try {
            silk = mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
         } catch (RuntimeException var11) {
         }

         Holder<Enchantment> silkHolder = silk;
         return (state, tool) -> {
            boolean silkTouch = silkHolder != null && tool != null && !tool.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(silkHolder, tool) > 0;
            return (facts.getOrDefault(state.getBlock(), 0) & (silkTouch ? 2 : 1)) != 0;
         };
      } else {
         return (state, tool) -> true;
      }
   }

   static Set<String> directMineableItems() {
      if (directMineableItems != null) {
         return directMineableItems;
      }

      Map<Block, String> claimedDrop = new HashMap<>();

      for (Item item : BuiltInRegistries.ITEM) {
         Block form = item instanceof BlockItem blockItem ? blockItem.getBlock() : null;

         for (Block block : sourceBlocksFor(item)) {
            if (block != form) {
               claimedDrop.putIfAbsent(block, BuiltInRegistries.ITEM.getKey(item).toString());
            }
         }
      }

      Set<String> mineable = new HashSet<>();

      for (Item item : BuiltInRegistries.ITEM) {
         String id = BuiltInRegistries.ITEM.getKey(item).toString();
         if (!requiresSilkTouchOrUnobtainable(id)) {
            Block form = item instanceof BlockItem blockItem ? blockItem.getBlock() : null;

            for (Block block : sourceBlocksFor(item)) {
               String claimed = claimedDrop.get(block);
               if (block != form || claimed == null || claimed.equals(id)) {
                  mineable.add(id);
                  break;
               }
            }
         }
      }

      directMineableItems = Set.copyOf(mineable);
      return directMineableItems;
   }

   public static boolean requiresSilkTouchOrUnobtainable(String id) {
      return !SILK_OR_UNOBTAINABLE_BLOCKS.contains(id) && !id.startsWith("minecraft:infested_")
         ? (id.endsWith("glass") || id.endsWith("glass_pane")) && !id.equals("minecraft:tinted_glass")
         : true;
   }

   public static List<Block> sourceBlocksFor(Item item) {
      LinkedHashSet<Block> sources = new LinkedHashSet<>();
      if (item instanceof BlockItem blockItem) {
         sources.add(blockItem.getBlock());
      }

      for (String blockId : ITEM_SOURCE_BLOCKS.getOrDefault(BuiltInRegistries.ITEM.getKey(item).toString(), List.of())) {
         BuiltInRegistries.BLOCK.getOptional(Identifier.parse(blockId)).ifPresent(sources::add);
      }

      return List.copyOf(sources);
   }

   public static List<EntityType<?>> sourceEntitiesFor(Item item) {
      List<EntityType<?>> types = new ArrayList<>();

      for (String typeId : ITEM_SOURCE_MOBS.getOrDefault(BuiltInRegistries.ITEM.getKey(item).toString(), List.of())) {
         BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.parse(typeId)).ifPresent(types::add);
      }

      return types;
   }

   static {
      FAMILY_ALIASES.put("wood", List.of("#minecraft:logs"));
      FAMILY_ALIASES.put("log", List.of("#minecraft:logs"));
      FAMILY_ALIASES.put("logs", List.of("#minecraft:logs"));
      FAMILY_ALIASES.put("planks", List.of("#minecraft:planks"));
      FAMILY_ALIASES.put("plank", List.of("#minecraft:planks"));
      FAMILY_ALIASES.put("sapling", List.of("#minecraft:saplings"));
      FAMILY_ALIASES.put("leaves", List.of("#minecraft:leaves"));
      FAMILY_ALIASES.put("wool", List.of("#minecraft:wool"));
      FAMILY_ALIASES.put("bed", List.of("#minecraft:beds"));
      FAMILY_ALIASES.put("boat", List.of("#minecraft:boats"));
      FAMILY_ALIASES.put("door", List.of("#minecraft:doors"));
      FAMILY_ALIASES.put("fence", List.of("#minecraft:fences"));
      FAMILY_ALIASES.put("wall", List.of("#minecraft:walls"));
      FAMILY_ALIASES.put("slab", List.of("#minecraft:slabs"));
      FAMILY_ALIASES.put("stairs", List.of("#minecraft:stairs"));
      FAMILY_ALIASES.put(
         "stone",
         List.of(
            "minecraft:stone",
            "minecraft:cobblestone",
            "minecraft:deepslate",
            "minecraft:cobbled_deepslate",
            "minecraft:andesite",
            "minecraft:diorite",
            "minecraft:granite",
            "minecraft:tuff",
            "minecraft:blackstone",
            "minecraft:basalt"
         )
      );
      FAMILY_ALIASES.put("cobblestone", List.of("minecraft:cobblestone", "minecraft:cobbled_deepslate"));
      FAMILY_ALIASES.put("dirt", List.of("#minecraft:dirt"));
      FAMILY_ALIASES.put("sand", List.of("#minecraft:sand"));
      FAMILY_ALIASES.put("gravel", List.of("minecraft:gravel"));
      FAMILY_ALIASES.put("clay", List.of("minecraft:clay", "minecraft:clay_ball"));
      FAMILY_ALIASES.put("coal", List.of("minecraft:coal", "minecraft:coal_ore", "minecraft:deepslate_coal_ore"));
      FAMILY_ALIASES.put("iron", List.of("minecraft:iron_ingot", "minecraft:raw_iron", "minecraft:iron_ore", "minecraft:deepslate_iron_ore"));
      FAMILY_ALIASES.put("copper", List.of("minecraft:copper_ingot", "minecraft:raw_copper", "minecraft:copper_ore", "minecraft:deepslate_copper_ore"));
      FAMILY_ALIASES.put(
         "gold", List.of("minecraft:gold_ingot", "minecraft:raw_gold", "minecraft:gold_ore", "minecraft:deepslate_gold_ore", "minecraft:nether_gold_ore")
      );
      FAMILY_ALIASES.put("redstone", List.of("minecraft:redstone", "minecraft:redstone_ore", "minecraft:deepslate_redstone_ore"));
      FAMILY_ALIASES.put("lapis", List.of("minecraft:lapis_lazuli", "minecraft:lapis_ore", "minecraft:deepslate_lapis_ore"));
      FAMILY_ALIASES.put("diamond", List.of("minecraft:diamond", "minecraft:diamond_ore", "minecraft:deepslate_diamond_ore"));
      FAMILY_ALIASES.put("emerald", List.of("minecraft:emerald", "minecraft:emerald_ore", "minecraft:deepslate_emerald_ore"));
      FAMILY_ALIASES.put("quartz", List.of("minecraft:quartz", "minecraft:nether_quartz_ore"));
      FAMILY_ALIASES.put("netherite", List.of("minecraft:netherite_ingot", "minecraft:netherite_scrap", "minecraft:ancient_debris"));
      FAMILY_ALIASES.put("ancient_debris", List.of("minecraft:ancient_debris"));
      FAMILY_ALIASES.put("ore", List.of("coal", "iron", "copper", "gold", "redstone", "lapis", "diamond", "emerald", "quartz", "ancient_debris"));
      FAMILY_ALIASES.put("pickaxe", List.of("#minecraft:pickaxes"));
      FAMILY_ALIASES.put("axe", List.of("#minecraft:axes"));
      FAMILY_ALIASES.put("shovel", List.of("#minecraft:shovels"));
      FAMILY_ALIASES.put("hoe", List.of("#minecraft:hoes"));
      FAMILY_ALIASES.put("sword", List.of("#minecraft:swords"));
      FAMILY_ALIASES.put("helmet", List.of("#minecraft:head_armor"));
      FAMILY_ALIASES.put("chestplate", List.of("#minecraft:chest_armor"));
      FAMILY_ALIASES.put("leggings", List.of("#minecraft:leg_armor"));
      FAMILY_ALIASES.put("boots", List.of("#minecraft:foot_armor"));
      FAMILY_ALIASES.put("armor", List.of("helmet", "chestplate", "leggings", "boots"));
      FAMILY_ALIASES.put("tool", List.of("pickaxe", "axe", "shovel", "hoe"));
      FAMILY_ALIASES.put("weapon", List.of("sword", "axe", "minecraft:trident", "minecraft:bow", "minecraft:crossbow"));
      FAMILY_ALIASES.put(
         "crop", List.of("minecraft:wheat", "minecraft:carrot", "minecraft:potato", "minecraft:beetroot", "minecraft:melon_slice", "minecraft:pumpkin")
      );
      FAMILY_ALIASES.put("meat", List.of("minecraft:beef", "minecraft:porkchop", "minecraft:chicken", "minecraft:mutton", "minecraft:rabbit"));
      FAMILY_ALIASES.put("raw_meat", List.of("meat"));
      FAMILY_ALIASES.put(
         "cooked_meat",
         List.of("minecraft:cooked_beef", "minecraft:cooked_porkchop", "minecraft:cooked_chicken", "minecraft:cooked_mutton", "minecraft:cooked_rabbit")
      );
      FAMILY_ALIASES.put("cooked_food", List.of("cooked_meat", "minecraft:cooked_cod", "minecraft:cooked_salmon", "minecraft:baked_potato"));
      FAMILY_ALIASES.put(
         "seed",
         List.of("minecraft:wheat_seeds", "minecraft:beetroot_seeds", "minecraft:melon_seeds", "minecraft:pumpkin_seeds", "minecraft:torchflower_seeds")
      );
   }

   public record ItemSpec(String spec, Set<Item> items, Set<Block> blocks) {
      public ItemSpec {
         items = Set.copyOf(items);
         blocks = Set.copyOf(blocks);
      }

      public boolean matches(ItemStack stack) {
         return stack != null && !stack.isEmpty() && this.items.contains(stack.getItem());
      }

      public boolean matches(BlockState state) {
         return state != null && this.blocks.contains(state.getBlock());
      }

      public boolean empty() {
         return this.items.isEmpty() && this.blocks.isEmpty();
      }

      public List<String> itemIds() {
         List<String> ids = new ArrayList<>(this.items.size());

         for (Item item : this.items) {
            ids.add(BuiltInRegistries.ITEM.getKey(item).toString());
         }

         ids.sort(null);
         return ids;
      }

      public List<String> blockIds() {
         List<String> ids = new ArrayList<>(this.blocks.size());

         for (Block block : this.blocks) {
            ids.add(BuiltInRegistries.BLOCK.getKey(block).toString());
         }

         ids.sort(null);
         return ids;
      }
   }
}
