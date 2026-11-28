package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.api.ai.AiOperation;
import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class AiProgression {
   static final Map<String, AiProgression.Inner_StreamUtil> ORE_SOURCES = Map.of(
      "minecraft:raw_iron",
      new AiProgression.Inner_StreamUtil("iron_ore", "minecraft:raw_iron", 16, AiProgression.Inner_CachePool.STONE, 1),
      "minecraft:diamond",
      new AiProgression.Inner_StreamUtil("diamond_ore", "minecraft:diamond", -58, AiProgression.Inner_CachePool.IRON, 1),
      "minecraft:coal",
      new AiProgression.Inner_StreamUtil("coal_ore", "minecraft:coal", 48, AiProgression.Inner_CachePool.WOOD, 1),
      "minecraft:raw_gold",
      new AiProgression.Inner_StreamUtil("gold_ore", "minecraft:raw_gold", -16, AiProgression.Inner_CachePool.IRON, 1),
      "minecraft:redstone",
      new AiProgression.Inner_StreamUtil("redstone_ore", "minecraft:redstone", -58, AiProgression.Inner_CachePool.IRON, 4),
      "minecraft:lapis_lazuli",
      new AiProgression.Inner_StreamUtil("lapis_ore", "minecraft:lapis_lazuli", 0, AiProgression.Inner_CachePool.STONE, 4),
      "minecraft:raw_copper",
      new AiProgression.Inner_StreamUtil("copper_ore", "minecraft:raw_copper", 48, AiProgression.Inner_CachePool.STONE, 2),
      "minecraft:ancient_debris",
      new AiProgression.Inner_StreamUtil("ancient_debris", "minecraft:ancient_debris", 15, AiProgression.Inner_CachePool.DIAMOND, 1, true),
      "minecraft:obsidian",
      new AiProgression.Inner_StreamUtil("obsidian", "minecraft:obsidian", -54, AiProgression.Inner_CachePool.DIAMOND, 1)
   );
   static final Map<String, String> SMELT_INPUTS = Map.of(
      "minecraft:iron_ingot",
      "minecraft:raw_iron",
      "minecraft:gold_ingot",
      "minecraft:raw_gold",
      "minecraft:copper_ingot",
      "minecraft:raw_copper",
      "minecraft:netherite_scrap",
      "minecraft:ancient_debris"
   );
   static final Set<String> KEEP_ITEMS;
   private static volatile long lastShelterDay = -1L;
   private static volatile long shelterRetryAt;
   private static volatile long shelterAttemptDay = -1L;
   private static volatile int shelterAttempts;
   static final List<String> PICKAXES = List.of(
      "minecraft:wooden_pickaxe", "minecraft:stone_pickaxe", "minecraft:iron_pickaxe", "minecraft:diamond_pickaxe", "minecraft:netherite_pickaxe"
   );
   static final int init_I = 14;
   static final int EXPOSED_STAIR_REACH = 9;
   static final int STAIR_REACH = 23;
   static final List<String> STONE_TOOL_BLOCKS = List.of("minecraft:cobblestone", "minecraft:cobbled_deepslate", "minecraft:blackstone");
   static final List<String> STONE_BLOCKS = List.of(
      "minecraft:stone", "minecraft:cobblestone", "minecraft:deepslate", "minecraft:cobbled_deepslate", "minecraft:blackstone"
   );
   static final int write_I = 20;
   static final long init_J = 10000000000L;
   static final long PREWORK_MIN_SLACK_NANOS = 3000000000L;

   private AiProgression() {
   }

   static boolean isNetherMaterial(String material) {
      AiProgression.Inner_StreamUtil ore = ORE_SOURCES.get(material);
      if (ore == null && SMELT_INPUTS.containsKey(material)) {
         ore = ORE_SOURCES.get(SMELT_INPUTS.get(material));
      }

      return ore != null && ore.nether();
   }

   static List<String> orderOverworldFirst(Collection<String> materials) {
      List<String> out = new ArrayList<>();

      for (String m : materials) {
         if (!isNetherMaterial(m)) {
            out.add(m);
         }
      }

      for (String m : materials) {
         if (isNetherMaterial(m)) {
            out.add(m);
         }
      }

      return out;
   }

   static boolean isNetheriteGear(String itemId) {
      return itemId.startsWith("minecraft:netherite_")
         && !itemId.equals("minecraft:netherite_ingot")
         && !itemId.equals("minecraft:netherite_scrap")
         && !itemId.equals("minecraft:netherite_block")
         && !itemId.equals("minecraft:netherite_upgrade_smithing_template");
   }

   static AiProgression.Inner_CachePool tierToMine(String material) {
      AiProgression.Inner_StreamUtil ore = ORE_SOURCES.get(material);
      if (ore == null && SMELT_INPUTS.containsKey(material)) {
         ore = ORE_SOURCES.get(SMELT_INPUTS.get(material));
      }

      return ore == null ? AiProgression.Inner_CachePool.NONE : ore.pick();
   }

   static boolean needsCrafting(String target, String oreDrop) {
      return !KEEP_ITEMS.contains(target) || !target.equals(oreDrop);
   }

   static String pickaxeFor(AiProgression.Inner_CachePool tier) {
      String var10000;
      switch (tier) {
         case NONE:
         case WOOD:
            var10000 = "minecraft:wooden_pickaxe";
            break;
         case STONE:
            var10000 = "minecraft:stone_pickaxe";
            break;
         case IRON:
            var10000 = "minecraft:iron_pickaxe";
            break;
         case DIAMOND:
            var10000 = "minecraft:diamond_pickaxe";
            break;
         case NETHERITE:
            var10000 = "minecraft:netherite_pickaxe";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   static AiProgression.Inner_CachePool pickaxeTier(String itemId) {
      AiProgression.Inner_CachePool var10000;
      switch (itemId) {
         case "minecraft:wooden_pickaxe":
         case "minecraft:golden_pickaxe":
            var10000 = AiProgression.Inner_CachePool.WOOD;
            break;
         case "minecraft:stone_pickaxe":
            var10000 = AiProgression.Inner_CachePool.STONE;
            break;
         case "minecraft:iron_pickaxe":
            var10000 = AiProgression.Inner_CachePool.IRON;
            break;
         case "minecraft:diamond_pickaxe":
            var10000 = AiProgression.Inner_CachePool.DIAMOND;
            break;
         case "minecraft:netherite_pickaxe":
            var10000 = AiProgression.Inner_CachePool.NETHERITE;
            break;
         default:
            var10000 = AiProgression.Inner_CachePool.NONE;
      }

      return var10000;
   }

   static String nextPickaxeToward(AiProgression.Inner_CachePool have, AiProgression.Inner_CachePool needed) {
      return have.ordinal() >= needed.ordinal() ? null : pickaxeFor(AiProgression.Inner_CachePool.values()[Math.max(AiProgression.Inner_CachePool.WOOD.ordinal(), have.ordinal() + 1)]);
   }

   static int oresForDrops(AiProgression.Inner_StreamUtil ore, int drops) {
      return Math.max(1, (drops + ore.yieldPerOre() - 1) / ore.yieldPerOre());
   }

   static boolean init_Z(String itemId, int goal) {
      if (PICKAXES.contains(itemId) && pickaxeTier(itemId).ordinal() >= AiProgression.Inner_CachePool.IRON.ordinal()) {
         return true;
      }

      if (isNetheriteGear(itemId)) {
         return true;
      }

      try {
         for (String material : keepItemsFor(itemId, goal).keySet()) {
            if (tierToMine(material).ordinal() >= AiProgression.Inner_CachePool.STONE.ordinal()) {
               return true;
            }
         }

         return false;
      } catch (RuntimeException e) {
         return false;
      }
   }

   static Map<String, Integer> keepItemShortfall(Map<String, Integer> goals) {
      return keepItemShortfall(AiCraft.buildRecipeGraph(), goals);
   }

   static Map<String, Integer> keepItemShortfall(final AiCraftPlanner.RecipeGraph live, Map<String, Integer> goals) {
      AiCraftPlanner.RecipeGraph raw = new AiCraftPlanner.RecipeGraph() {
         @Override
         public List<AiCraftPlanner.RecipeSpec> recipesFor(String id) {
            return AiProgression.KEEP_ITEMS.contains(id) ? List.of() : live.recipesFor(id);
         }

         @Override
         public int held(String id) {
            return AiProgression.KEEP_ITEMS.contains(id) ? 0 : live.held(id);
         }

         @Override
         public int load_I(String id) {
            return AiProgression.KEEP_ITEMS.contains(id) ? 0 : live.load_I(id);
         }

         @Override
         public String init(Set<String> variants) {
            return live.init(variants);
         }
      };
      Map<String, Integer> total = new LinkedHashMap<>();
      goals.forEach((id, n) -> {
         if (KEEP_ITEMS.contains(id)) {
            total.merge(id, n, Integer::sum);
         } else {
            AiCraftPlanner.init(raw, id, n).missing().forEach((m, k) -> {
               if (KEEP_ITEMS.contains(m)) {
                  total.merge(m, k, Integer::sum);
               }
            });
         }
      });
      Map<String, Integer> out = new LinkedHashMap<>();
      total.forEach((m, n) -> {
         int left = n - live.held(m);
         if (left > 0) {
            out.put(m, left);
         }
      });
      return out;
   }

   static AiProgression.Inner_DataUtil woodForKit(AiCraftPlanner.RecipeGraph live, Map<String, Integer> kit, String main, int target) {
      Map<String, Integer> goals = new LinkedHashMap<>(kit);
      if (!PICKAXES.contains(main) && !KEEP_ITEMS.contains(main)) {
         goals.merge(main, target, Math::max);
      }

      return woodForGoals(live, goals);
   }

   static AiProgression.Inner_DataUtil woodForGoals(final AiCraftPlanner.RecipeGraph live, Map<String, Integer> goals) {
      AiCraftPlanner.RecipeGraph raw = new AiCraftPlanner.RecipeGraph() {
         boolean treatAsLeaf(String id) {
            return AiProgression.KEEP_ITEMS.contains(id) || AiProgression.isPlanks(id) || id.equals("minecraft:stick");
         }

         @Override
         public List<AiCraftPlanner.RecipeSpec> recipesFor(String id) {
            return this.treatAsLeaf(id) ? List.of() : live.recipesFor(id);
         }

         @Override
         public int held(String id) {
            return this.treatAsLeaf(id) ? 0 : live.held(id);
         }

         @Override
         public int load_I(String id) {
            return this.treatAsLeaf(id) ? 0 : live.load_I(id);
         }

         @Override
         public String init(Set<String> variants) {
            return live.init(variants);
         }
      };
      int planks = 0;
      int sticks = 0;

      for (Entry<String, Integer> goal : goals.entrySet()) {
         if (live.held(goal.getKey()) < goal.getValue()) {
            AiCraftPlanner.CraftPlan plan = AiCraftPlanner.init(raw, goal.getKey(), goal.getValue());
            if (plan.missing().containsKey(goal.getKey())) {
               return null;
            }

            for (Entry<String, Integer> missing : plan.missing().entrySet()) {
               if (isPlanks(missing.getKey())) {
                  planks += missing.getValue();
               } else if (missing.getKey().equals("minecraft:stick")) {
                  sticks += missing.getValue();
               }
            }
         }
      }

      return new AiProgression.Inner_DataUtil(planks, sticks);
   }

   private static boolean isPlanks(String id) {
      return id.equals("planks") || id.equals("minecraft:planks") || id.endsWith("_planks");
   }

   static int earlyDiamondPicks(int missing, int held, double beforeTicks, double afterTicks, double setupTicks, boolean prerequisitesAffordable) {
      int firstPick = Math.max(0, 3 - held);
      return prerequisitesAffordable && AiMiningPreparation.init(missing, beforeTicks, afterTicks, setupTicks, firstPick) ? firstPick : -1;
   }

   static Map<String, Integer> keepItemsFor(String itemId, int goal) {
      if (KEEP_ITEMS.contains(itemId)) {
         int held = AiCraft.countItem(itemId);
         return held >= goal ? Map.of() : Map.of(itemId, goal - held);
      } else {
         final AiCraftPlanner.RecipeGraph live = AiCraft.buildRecipeGraph();
         AiCraftPlanner.RecipeGraph leaves = new AiCraftPlanner.RecipeGraph() {
            @Override
            public List<AiCraftPlanner.RecipeSpec> recipesFor(String id) {
               return AiProgression.KEEP_ITEMS.contains(id) ? List.of() : live.recipesFor(id);
            }

            @Override
            public int held(String id) {
               return live.held(id);
            }

            @Override
            public int load_I(String id) {
               return AiProgression.KEEP_ITEMS.contains(id) ? 0 : live.load_I(id);
            }

            @Override
            public String init(Set<String> variants) {
               return live.init(variants);
            }
         };
         Map<String, Integer> out = new LinkedHashMap<>();
         AiCraftPlanner.init(leaves, itemId, goal).missing().forEach((id, n) -> {
            if (KEEP_ITEMS.contains(id)) {
               out.merge(id, n, Integer::sum);
            }
         });
         return out;
      }
   }

   static AiProgression.Inner_CachePool bestTier(Minecraft mc) {
      AiProgression.Inner_CachePool best = AiProgression.Inner_CachePool.NONE;
      Inventory inventory = mc.player.getInventory();

      for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
         ItemStack stack = inventory.getItem(slot);
         if (!stack.isEmpty()) {
            AiProgression.Inner_CachePool tier = pickaxeTier(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            if (tier != AiProgression.Inner_CachePool.NONE
               && (!stack.isDamageableItem() || stack.getMaxDamage() - stack.getDamageValue() > 3)
               && tier.ordinal() > best.ordinal()) {
               best = tier;
            }
         }
      }

      return best;
   }

   static boolean hasWeapon(Minecraft mc) {
      Inventory inventory = mc.player.getInventory();

      for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
         String id = BuiltInRegistries.ITEM.getKey(inventory.getItem(slot).getItem()).toString();
         if (id.endsWith("_sword") || id.endsWith("_axe")) {
            return true;
         }
      }

      return false;
   }

   static int bestPickDurability(Minecraft mc, Item pick) {
      int best = 0;

      for (int slot = 0; slot < 36; slot++) {
         ItemStack stack = mc.player.getInventory().getItem(slot);
         if (stack.is(pick)) {
            best = Math.max(best, stack.getMaxDamage() - stack.getDamageValue());
         }
      }

      return best;
   }

   static int freeSlots(Minecraft mc) {
      int free = 0;
      Inventory inventory = mc.player.getInventory();

      for (int slot = 0; slot < 36; slot++) {
         if (inventory.getItem(slot).isEmpty()) {
            free++;
         }
      }

      return free;
   }

   static int plankNeed(AiProgression.Inner_CachePool have, AiProgression.Inner_CachePool last, boolean tableHeld, boolean weapon, int sticksHeld, int smelts, boolean coal) {
      return plankNeed(have, last, tableHeld, weapon, sticksHeld, smelts, coal, false);
   }

   static int plankNeed(
      AiProgression.Inner_CachePool have, AiProgression.Inner_CachePool last, boolean tableHeld, boolean weapon, int sticksHeld, int smelts, boolean coal, boolean oneTable
   ) {
      return plankNeed(have, last, tableHeld, weapon, sticksHeld, smelts, coal, oneTable, new AiProgression.Inner_DataUtil(0, 0));
   }

   static int plankNeed(
      AiProgression.Inner_CachePool have,
      AiProgression.Inner_CachePool last,
      boolean tableHeld,
      boolean weapon,
      int sticksHeld,
      int smelts,
      boolean coal,
      boolean oneTable,
      AiProgression.Inner_DataUtil kit
   ) {
      int tables = tableHeld ? 0 : 1;
      if (!oneTable && have.ordinal() < AiProgression.Inner_CachePool.WOOD.ordinal() && last.ordinal() >= AiProgression.Inner_CachePool.STONE.ordinal()) {
         tables++;
      }

      if (!oneTable && have.ordinal() < AiProgression.Inner_CachePool.STONE.ordinal() && last.ordinal() >= AiProgression.Inner_CachePool.IRON.ordinal()) {
         tables++;
      }

      if (!oneTable && have.ordinal() < AiProgression.Inner_CachePool.IRON.ordinal() && last.ordinal() >= AiProgression.Inner_CachePool.DIAMOND.ordinal()) {
         tables++;
      }

      int planks = 4 * tables + kit.planks();
      if (have.ordinal() < AiProgression.Inner_CachePool.WOOD.ordinal() && last.ordinal() >= AiProgression.Inner_CachePool.WOOD.ordinal()) {
         planks += 3;
      }

      int sticks = kit.sticks() + (!weapon && last.ordinal() >= AiProgression.Inner_CachePool.STONE.ordinal() ? 2 : 0);

      for (AiProgression.Inner_CachePool t : new AiProgression.Inner_CachePool[]{
         AiProgression.Inner_CachePool.WOOD, AiProgression.Inner_CachePool.STONE, AiProgression.Inner_CachePool.IRON, AiProgression.Inner_CachePool.DIAMOND
      }) {
         if (t.ordinal() > have.ordinal() && t.ordinal() <= last.ordinal()) {
            sticks += 2;
         }
      }

      sticks = Math.max(0, sticks - sticksHeld);
      planks += (sticks + 3) / 4 * 2;
      if (!coal && smelts > 0) {
         planks += (smelts * 200 + 299) / 300;
      }

      return planks;
   }

   static int stoneBatchFor(AiProgression.Inner_CachePool have, int remaining) {
      return have == AiProgression.Inner_CachePool.WOOD ? Math.min(3, remaining) : remaining;
   }

   static boolean shouldReclaimTable(boolean carryOneTable, int wood, int futureWood) {
      return carryOneTable || wood < 4 + futureWood;
   }

   static int tableCarrySeconds(boolean campaign, boolean jointKit, String target, int heldTables, boolean attempted, int secondsLeft) {
      if (campaign && (jointKit || target.equals("minecraft:diamond_pickaxe")) && heldTables <= 0) {
         return !attempted && secondsLeft > 0 ? Math.min(90, secondsLeft) : -1;
      } else {
         return 0;
      }
   }

   static int countStoneToolBlocks() {
      int total = 0;

      for (String id : STONE_TOOL_BLOCKS) {
         total += AiCraft.countItem(id);
      }

      return total;
   }

   static AiOperation gatherLogs(int held, int timeoutSeconds) {
      return gatherLogs(held, 0, timeoutSeconds);
   }

   static AiOperation gatherLogs(int held, int heldMin, int timeoutSeconds) {
      return autismclient.ai.engine.AiBlockMine.create(
         AiJson.objectOf(
            "block", "log", "held", held, "heldMin", heldMin, "nearRadius", 20, "trees", true, "radius", 64, "timeoutSeconds", Math.max(30, timeoutSeconds)
         )
      );
   }

   static int plankEquivalents() {
      return AiItems.countInInventory(AiItems.resolveItem("planks")) + 4 * AiItems.countInInventory(AiItems.resolveItem("log"));
   }

   static boolean hasAxe(Minecraft mc) {
      Inventory inventory = mc.player.getInventory();

      for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
         if (inventory.getItem(slot).is(ItemTags.AXES)) {
            return true;
         }
      }

      return false;
   }

   static AiOperation obtain(JsonObject args, final String itemId, final int goal) {
      final Minecraft mc = Minecraft.getInstance();
      final int timeoutSeconds = Math.max(60, Math.min(7200, AiJson.getInt(args, "timeoutSeconds", 3600)));
      final Map<String, Integer> kit = new LinkedHashMap<>();
      if (args.has("kit") && args.get("kit").isJsonObject()) {
         for (Entry<String, JsonElement> e : args.getAsJsonObject("kit").entrySet()) {
            kit.put(e.getKey(), Math.max(1, e.getValue().getAsInt()));
         }
      }

      final int kitPlanks = Math.max(0, AiJson.getInt(args, "kitPlanks", 0));
      return new AiOperation() {
         final String title = "Getting " + AiItems.resolveItem(itemId).items().iterator().next().getDefaultInstance().getHoverName().getString();
         final long deadline = System.nanoTime() + timeoutSeconds * 1000000000L;
         Object startLevel = mc.level;
         final Object startConnection = mc.getConnection();
         final String startWorldKey = AiWorldIdentity.currentWorldKey(mc);
         final UUID playerId = mc.player == null ? null : mc.player.getUUID();
         final long startDeathRevision = AiServerEvidence.deathRevision();
         String dimensionId = AiWorldIdentity.currentDimensionId(mc);
         final JsonArray stepLog = new JsonArray();
         JsonObject furnaceEvidence = new JsonObject();
         final Map<String, Integer> failuresByStep = new HashMap<>();
         final AiGuard.GuardPolicy guardPolicy = AiGuard.GuardPolicy.standard().keeping(AiProgression.keepIds(itemId, kit.keySet()));
         AiOperation stepOp;
         AiOperation guardOp;
         String stepKey = "";
         String stepLabel = "";
         String guardReason = "";
         String lastFailure = "";
         boolean singleTable = AiRunResources.isCampaignActive();
         boolean stockWoodTried;
         boolean shieldEquipTried;
         boolean waterEarlyTried;
         boolean tableRecoveryTried;
         boolean completionTableTried;
         final AiPreparationReceipt receipt = new AiPreparationReceipt();
         int armourEquips;
         BlockPos init_BlockPos;
         BlockPos smeltLoadedFrom;
         String close_Str = "";
         String smeltInput = "";
         String smeltFuel = "";
         String smeltStation = "minecraft:furnace";
         int smeltTarget;
         int inputHeldBeforeLoad;
         int smeltBatch;
         long read_J;
         final Map<String, BlockPos> oreTargetByFamily = new HashMap<>();
         boolean returnToFurnace;
         boolean close_Z;
         BlockPos firstStairStep;
         final Set<Long> blockedStairCells = new LinkedHashSet<>();
         long lastTidyNanos;
         long lastHuntNanos;
         boolean guardFighting;
         boolean guardFleeing;
         int landingWaitTicks;
         boolean cancelled;
         boolean nightStepActive;
         int reservedWood;
         int reservedStone;
         int chainDepth;

         boolean betterArmourHeld() {
            Inventory inv = mc.player.getInventory();

            for (int i = 0; i < 36; i++) {
               ItemStack stack = inv.getItem(i);
               Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
               if (!stack.isEmpty() && equippable != null) {
                  EquipmentSlot slot = equippable.slot();
                  if (slot.getType() == Type.HUMANOID_ARMOR) {
                     ItemStack worn = mc.player.getItemBySlot(slot);
                     if (worn.isEmpty()) {
                        return true;
                     }

                     double have = this.armorScore(worn, slot);
                     double next = this.armorScore(stack, slot);
                     if (next > have) {
                        return true;
                     }
                  }
               }
            }

            return false;
         }

         double armorScore(ItemStack stack, EquipmentSlot slot) {
            double[] total = new double[]{0.0};
            stack.forEachModifier(slot, (attribute, modifier) -> {
               if (attribute.is(Attributes.ARMOR)) {
                  total[0] += modifier.amount();
               } else if (attribute.is(Attributes.ARMOR_TOUGHNESS)) {
                  total[0] += modifier.amount() / 100.0;
               }
            });
            return total[0];
         }

         AiProgression.Inner_CachePool effectiveTier() {
            AiProgression.Inner_CachePool have = AiProgression.bestTier(mc);
            AiProgression.Inner_CachePool worn = AiProgression.Inner_CachePool.NONE;
            Inventory inventory = mc.player.getInventory();

            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
               ItemStack stack = inventory.getItem(slot);
               if (!stack.isEmpty()) {
                  AiProgression.Inner_CachePool tier = AiProgression.pickaxeTier(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                  if (tier.ordinal() > worn.ordinal()) {
                     worn = tier;
                  }
               }
            }

            if (worn.ordinal() <= have.ordinal()) {
               return have;
            }

            boolean var10000;
            switch (worn) {
               case WOOD:
                  var10000 = AiProgression.countStoneToolBlocks() >= 3;
                  break;
               case STONE:
                  var10000 = AiCraft.countItem("minecraft:iron_ingot") >= 3 || this.init_BlockPos != null;
                  break;
               case IRON:
                  var10000 = AiCraft.countItem("minecraft:diamond") >= 3;
                  break;
               default:
                  var10000 = false;
            }

            boolean nextOnItsWay = var10000;
            return nextOnItsWay ? worn : have;
         }

         int stoneNeeded() {
            AiProgression.Inner_CachePool have = this.effectiveTier();
            AiProgression.Inner_CachePool last = this.finalTier();
            int need = 0;
            if (have.ordinal() < AiProgression.Inner_CachePool.STONE.ordinal() && last.ordinal() >= AiProgression.Inner_CachePool.STONE.ordinal()) {
               need += 3;
            }

            if (!AiProgression.hasWeapon(mc) && last.ordinal() >= AiProgression.Inner_CachePool.STONE.ordinal()) {
               need += 3;
            }

            if (have.ordinal() < AiProgression.Inner_CachePool.IRON.ordinal()
               && last.ordinal() >= AiProgression.Inner_CachePool.IRON.ordinal()
               && AiCraft.countItem("minecraft:furnace") == 0
               && this.ironStillNeeded()) {
               need += 8;
            }

            return need;
         }

         Map<String, Integer> materialNeeds() {
            if (kit.isEmpty()) {
               return AiProgression.keepItemsFor(itemId, goal);
            }

            Map<String, Integer> goals = new LinkedHashMap<>();
            goals.put(itemId, goal);
            kit.forEach((id, n) -> {
               if (n - this.filledBucketCount(id) > 0) {
                  goals.merge(id, n - this.filledBucketCount(id), Integer::sum);
               }
            });
            return AiProgression.keepItemShortfall(goals);
         }

         Map<String, Integer> addKitNeeds(String pick, Map<String, Integer> pickNeeds) {
            if (kit.isEmpty()) {
               return pickNeeds;
            }

            Map<String, Integer> all = this.materialNeeds();
            Map<String, Integer> out = new LinkedHashMap<>();
            pickNeeds.forEach((m, n) -> out.put(m, pick.equals(itemId) ? Math.max(n, all.getOrDefault(m, 0)) : n + all.getOrDefault(m, 0)));
            return out;
         }

         boolean kitComplete() {
            for (Entry<String, Integer> e : kit.entrySet()) {
               if (this.heldForKit(e.getKey()) < e.getValue()) {
                  return false;
               }
            }

            return true;
         }

         int heldForKit(String id) {
            int n = AiCraft.countItem(id);
            if (id.equals("minecraft:bucket")) {
               n += AiCraft.countItem("minecraft:water_bucket") + AiCraft.countItem("minecraft:lava_bucket");
            }

            return n;
         }

         int filledBucketCount(String id) {
            return this.heldForKit(id) - AiCraft.countItem(id);
         }

         boolean ironStillNeeded() {
            return AiCraft.countItem("minecraft:iron_ingot") < 3 && this.init_BlockPos == null;
         }

         @Override
         public AiOperation.Result tick() {
            AiOperation.Result result = this.advance();
            if (!result.terminal()) {
               AiVisualization.init(this, this.title, AiTaskStatus.init(result.message(), "Preparing materials"), AiCraft.countItem(itemId), goal);
               AiGoal.init(this, 1, this.title, AiGoal.read(this.stepKey), "");
            } else {
               AiVisualization.clearOwner(this);
               AiGoal.init(this);
            }

            return result;
         }

         AiOperation.Result advance() {
            if (this.cancelled
               || mc.player == null
               || mc.level == null
               || mc.getConnection() != this.startConnection
               || !this.startWorldKey.equals(AiWorldIdentity.currentWorldKey(mc))
               || !mc.player.getUUID().equals(this.playerId)) {
               return this.finish(AiOperation.Status.CANCELLED, "Progression world/player changed");
            }

            if (mc.player.isAlive() && this.startDeathRevision == AiServerEvidence.deathRevision()) {
               if (mc.level != this.startLevel) {
                  boolean expected = this.dimensionId.equals("minecraft:overworld")
                     && AiWorldIdentity.currentDimensionId(mc).equals("minecraft:the_nether")
                     && this.stepOp != null
                     && (this.stepKey.equals("nether") || this.stepKey.equals("smith-ingot"));
                  if (!expected) {
                     return this.finish(AiOperation.Status.UNCERTAIN, "Progression dimension changed outside its expected Nether action");
                  }

                  this.startLevel = mc.level;
                  this.dimensionId = AiWorldIdentity.currentDimensionId(mc);
                  this.init_BlockPos = null;
                  this.smeltLoadedFrom = null;
                  this.close_Str = "";
                  this.smeltInput = "";
                  this.smeltFuel = "";
                  this.read_J = 0L;
                  this.oreTargetByFamily.clear();
               }

               autismclient.ai.engine.AiRestock.reserveWoodForJob(this.reservedWood);
               autismclient.ai.engine.AiRestock.reserveStoneForJob(this.reservedStone);
               boolean preservingIron = this.finalTier().ordinal() >= AiProgression.Inner_CachePool.DIAMOND.ordinal()
                  && AiProgression.bestTier(mc) == AiProgression.Inner_CachePool.IRON;
               autismclient.ai.engine.AiRestock.reserveStrikesForJob(preservingIron ? 48 : 0);
               if (System.nanoTime() > this.deadline) {
                  return this.finish(
                     this.canInterruptNow() ? AiOperation.Status.TIMED_OUT : AiOperation.Status.UNCERTAIN, "Ran out of time at: " + this.stepLabel
                  );
               }

               if (AiCraft.countItem(itemId) >= goal && this.kitComplete() && this.stepOp == null && this.guardOp == null) {
                  int tableBudget = AiProgression.tableCarrySeconds(
                     AiRunResources.isCampaignActive(),
                     !kit.isEmpty(),
                     itemId,
                     AiCraft.countItem("minecraft:crafting_table"),
                     this.completionTableTried,
                     (int)Math.max(0L, (this.deadline - System.nanoTime()) / 1000000000L)
                  );
                  if (tableBudget < 0) {
                     return this.finish(
                        AiOperation.Status.SUCCEEDED,
                        "Obtained " + goal + " " + itemId + (kit.isEmpty() ? "" : " and the kit " + kit) + "; the crafting table could not be recovered"
                     );
                  }

                  if (tableBudget > 0) {
                     if (!autismclient.ai.engine.AiMotionSafety.isPlayerSupported()) {
                        return this.running("Settling before carrying the kit's crafting table onward");
                     }

                     this.completionTableTried = true;
                     AiOperation carry = AiProgression.init(tableBudget, true);
                     if (carry != null) {
                        return this.startStep("pack-table-completion", "carry the crafting table onward with the completed kit", carry);
                     }

                     AiOperation replacement = AiCraft.craft(
                        AiJson.objectOf(
                           "item",
                           "minecraft:crafting_table",
                           "count",
                           1,
                           "timeoutSeconds",
                           Math.max(15, Math.min(45, (int)Math.max(0L, (this.deadline - System.nanoTime()) / 1000000000L)))
                        )
                     );
                     if (replacement != null) {
                        return this.startStep("craft-table-completion", "make the crafting table to carry onward instead of fetching the old one", replacement);
                     }
                  }

                  return this.finish(AiOperation.Status.SUCCEEDED, "Obtained " + goal + " " + itemId + (kit.isEmpty() ? "" : " and the kit " + kit));
               } else {
                  AiOperation.Result guarded = this.guardStep();
                  if (guarded != null) {
                     return guarded;
                  }

                  AiOperation.Result night = this.nightStep();
                  if (night != null) {
                     return night;
                  }

                  if (this.stepOp != null) {
                     if (preservingIron
                        && this.stepKey.startsWith("ore:")
                        && AiProgression.bestPickDurability(mc, Items.IRON_PICKAXE) <= 48
                        && AiProgression.bestPickDurability(mc, Items.STONE_PICKAXE) <= 8
                        && mc.player.onGround()
                        && mc.player.getDeltaMovement().horizontalDistanceSqr() <= 4.0E-4
                        && this.stepOp.canInterruptNow()) {
                        this.stepOp.cancel();
                        this.stepOp = null;
                        return this.startStep(
                           "spare-pick",
                           "make a stone pick for the descent; keep iron for diamonds",
                           this.obtainOp("minecraft:stone_pickaxe", AiCraft.countItem("minecraft:stone_pickaxe") + 1)
                        );
                     }

                     boolean allowHurt = mc.player.getHealth() <= 6.0F && this.noEnemiesWithin(24.0);
                     if (allowHurt) {
                        autismclient.ai.engine.AiLowHealth.enterOverride();
                     }

                     AiOperation.Result r;
                     try {
                        r = this.stepKey.equals("pack-table-completion")
                           ? this.receipt.tickChild(this.stepOp)
                           : this.stepOp.tick();
                     } finally {
                        if (allowHurt) {
                           autismclient.ai.engine.AiLowHealth.load();
                        }
                     }

                     boolean furnaceStep = this.stepKey.startsWith("smelt") || this.stepKey.startsWith("collect:") || this.stepKey.equals("prework");
                     if (furnaceStep) {
                        this.furnaceEvidence = AiRunResources.parseEvidenceJson(r.dataJson());
                     }

                     if (!r.terminal()) {
                        return this.running(this.stepLabel + ": " + r.message());
                     }

                     if (this.stepKey.equals("pack-table-completion")) {
                        this.receipt.cancelChild(this.stepOp);
                     } else {
                        this.stepOp.cancel();
                     }

                     this.stepOp = null;
                     JsonObject outcome = AiJson.objectOf("step", this.stepLabel, "status", r.status().name(), "message", AiProgression.clip(r.message()));
                     if (furnaceStep) {
                        outcome.add("receipt", this.furnaceEvidence.deepCopy());
                     }

                     this.stepLog.add(outcome);
                     if (r.status() == AiOperation.Status.UNCERTAIN) {
                        return this.finish(AiOperation.Status.UNCERTAIN, "Unconfirmed " + this.stepLabel + ": " + r.message());
                     }

                     AutismClientAddon.LOG
                        .info(
                           "[AI] ladder step {}: {} - {}{}",
                           r.status(),
                           this.stepLabel,
                           AiProgression.clip(r.message()),
                           r.status() == AiOperation.Status.SUCCEEDED
                              ? ""
                              : " data " + (r.dataJson().length() > 2500 ? r.dataJson().substring(0, 2500) : r.dataJson())
                        );
                     if (this.nightStepActive) {
                        this.nightStepActive = false;
                        if (r.status() != AiOperation.Status.SUCCEEDED && ++AiProgression.shelterAttempts < 3) {
                           AiProgression.lastShelterDay = -1L;
                           AiProgression.shelterRetryAt = System.nanoTime() + 20000000000L;
                           return this.running("No shelter yet (" + AiProgression.clip(r.message()) + "); trying again shortly");
                        } else {
                           return this.running("Morning; back to the ladder");
                        }
                     } else {
                        if (r.status() == AiOperation.Status.CANCELLED && this.cancelled) {
                           return this.finish(AiOperation.Status.CANCELLED, "Cancelled during " + this.stepLabel);
                        }

                        if (!this.stepKey.startsWith("ore:") || r.failure() != AiFailure.MISSING_TOOL && r.failure() != AiFailure.TOOL_WORN) {
                           if (this.stepKey.equals("prework")) {
                              if (r.status() != AiOperation.Status.SUCCEEDED && this.firstStairStep != null) {
                                 this.blockedStairCells.add(this.firstStairStep.asLong());
                              }

                              this.returnToFurnace = true;
                              return this.continueNow(
                                 this.returnToFurnace ? "Back to the furnace after: " + this.stepLabel : "Continue the smelt work after: " + this.stepLabel
                              );
                           } else {
                              if (r.status() == AiOperation.Status.SUCCEEDED) {
                                 this.onStepSucceeded(r);
                              } else if (this.stepKey.startsWith("collect:")
                                 && this.init_BlockPos != null
                                 && !mc.level.getBlockState(this.init_BlockPos).is(Blocks.FURNACE)) {
                                 AutismClientAddon.LOG
                                    .info("[AI] ladder for {}: the background furnace at {} is gone", itemId, this.init_BlockPos.toShortString());
                                 this.init_BlockPos = null;
                              }

                              if (r.status() != AiOperation.Status.SUCCEEDED) {
                                 if (this.stepKey.equals("ore:minecraft:obsidian") && AiCraft.countItem(itemId) < goal) {
                                    return this.finish(r.status(), "Contained obsidian acquisition stopped: " + AiProgression.clip(r.message()));
                                 }

                                 int n = this.failuresByStep.merge(this.stepKey, 1, Integer::sum);
                                 this.lastFailure = this.stepLabel + ": " + AiProgression.clip(r.message());
                                 if (n >= 3) {
                                    return this.finish(
                                       AiOperation.Status.FAILED, "Could not " + this.stepLabel + " after 3 tries (" + AiProgression.clip(r.message()) + ")"
                                    );
                                 }
                              }

                              return this.continueNow("Next step after: " + this.stepLabel);
                           }
                        } else {
                           String drop = this.stepKey.substring("ore:".length());
                           AiProgression.Inner_StreamUtil ore = AiProgression.ORE_SOURCES.get(drop);
                           JsonObject detail = AiJson.parseObject(r.dataJson());
                           if (ore != null && detail.has("targetX") && detail.has("targetY") && detail.has("targetZ")) {
                              this.oreTargetByFamily
                                 .put(
                                    ore.family(),
                                    new BlockPos(detail.get("targetX").getAsInt(), detail.get("targetY").getAsInt(), detail.get("targetZ").getAsInt())
                                 );
                           }

                           return this.continueNow("Replace the worn mining tool before continuing the same ore");
                        }
                     }
                  } else {
                     if (!mc.player.onGround() && !mc.player.isInWater() && !mc.player.onClimbable() && ++this.landingWaitTicks < 60) {
                        return this.running("Waiting to land");
                     }

                     this.landingWaitTicks = 0;
                     AiOperation was = this.stepOp;
                     AiOperation.Result chosen = this.chooseStep();
                     if (this.stepOp != null && this.stepOp != was && !chosen.terminal() && this.chainDepth < 4) {
                        this.chainDepth++;

                        try {
                           return this.advance();
                        } finally {
                           this.chainDepth--;
                        }
                     } else {
                        return chosen;
                     }
                  }
               }
            } else {
               return this.finish(AiOperation.Status.FAILED, "Player died during progression");
            }
         }

         AiOperation.Result continueNow(String message) {
            if (this.chainDepth >= 4) {
               return this.running(message);
            }

            this.chainDepth++;

            try {
               return this.advance();
            } finally {
               this.chainDepth--;
            }
         }

         AiOperation.Result chooseStep() {
            if (AiProgression.freeSlots(mc) < 3 && System.nanoTime() - this.lastTidyNanos > 30000000000L) {
               this.lastTidyNanos = System.nanoTime();
               return this.startStep(
                  "tidy", "make room in the bag", AiTidy.init(AiJson.objectOf("action", "drop", "freeSlots", 3, "keep", this.keepIds()))
               );
            }

            AiDeathLog.Inner_DataUtil death = AiDeathLog.lastDeath();
            if (death != null
               && !AiDeathLog.init_Z()
               && death.remainingMillis() > 20000L
               && death.dimension().equals(mc.level.dimension().identifier().toString())
               && this.failuresByStep.getOrDefault("recover", 0) == 0) {
               return this.startStep("recover", "go back for the items dropped at " + death.position().toShortString(), AiRecover.init(new JsonObject()));
            }

            AiProgression.Inner_CachePool have = this.effectiveTier();
            if (have.ordinal() >= AiProgression.Inner_CachePool.STONE.ordinal()
               && !AiProgression.hasWeapon(mc)
               && (AiProgression.countStoneToolBlocks() >= 3 || have.ordinal() > AiProgression.Inner_CachePool.STONE.ordinal())
               && this.failuresByStep.getOrDefault("axe", 0) == 0) {
               return this.startStep("axe", "make a stone axe for mobs and crafting tables", this.obtainOp("minecraft:stone_axe", 1));
            }

            if (have.ordinal() >= AiProgression.Inner_CachePool.IRON.ordinal() && kit.containsKey("minecraft:shield")) {
               if (AiCraft.countItem("minecraft:shield") == 0 && AiCraft.countItem("minecraft:iron_ingot") >= 1 && this.failuresByStep.getOrDefault("shield-early", 0) == 0) {
                  return this.startStep("shield-early", "make the kit's shield now", this.obtainOp("minecraft:shield", 1));
               }

               if (AiCraft.countItem("minecraft:shield") > 0
                  && !mc.player.getOffhandItem().is(Items.SHIELD)
                  && mc.player.getOffhandItem().isEmpty()
                  && this.failuresByStep.getOrDefault("shield-offhand", 0) == 0
                  && !this.shieldEquipTried) {
                  this.shieldEquipTried = true;
                  return this.startStep(
                     "shield-offhand", "the shield to the offhand", AiEquip.init(AiJson.objectOf("what", "minecraft:shield", "hand", "offhand"))
                  );
               }
            }

            if (this.betterArmourHeld() && this.armourEquips < 3) {
               this.armourEquips++;
               return this.startStep("armour-now", "put the new armour on", AiEquip.init(AiJson.objectOf("what", "armor")));
            }

            int waterWanted = Math.min(2, kit.getOrDefault("minecraft:bucket", 0));
            if (have.ordinal() >= AiProgression.Inner_CachePool.IRON.ordinal()
               && waterWanted > 0
               && !this.waterEarlyTried
               && AiCraft.countItem("minecraft:water_bucket") < waterWanted
               && (AiCraft.countItem("minecraft:bucket") > 0 || AiCraft.countItem("minecraft:iron_ingot") >= 3)
               && !AiTerrain.findFluidSources(mc, Blocks.WATER, 24, 12, false).stream().filter(p -> mc.level.getBlockState(p.above()).isAir()).toList().isEmpty()) {
               this.waterEarlyTried = true;
               return this.startStep("water-early", "fill the water buckets close by before going deep", AiObsidian.init(waterWanted, 90));
            }

            if (have.ordinal() >= AiProgression.Inner_CachePool.STONE.ordinal()
               && have.ordinal() < AiProgression.Inner_CachePool.IRON.ordinal()
               && this.finalTier().ordinal() >= AiProgression.Inner_CachePool.IRON.ordinal()
               && this.ironStillNeeded()
               && AiCraft.countItem("minecraft:furnace") == 0
               && AiProgression.countStoneToolBlocks() >= 8
               && this.findNearbyBlock(Blocks.CRAFTING_TABLE, 5) != null
               && this.failuresByStep.getOrDefault("furnace", 0) == 0) {
               return this.startStep("furnace", "make the furnace while the table is out", this.obtainOp("minecraft:furnace", 1));
            }

            if (mc.player.getFoodData().getFoodLevel() <= 12 && AiSurvival.init(mc.player.getInventory()) < 0 && System.nanoTime() - this.lastHuntNanos > 600000000000L) {
               this.lastHuntNanos = System.nanoTime();
               AiOperation hunt = AiSpeedrun.hunt(48.0, 4, Math.min(120, this.secondsLeft()));
               if (hunt != null) {
                  return this.startStep("food", "hunt for food", hunt);
               }
            }

            Map<String, Integer> needs = this.materialNeeds();
            AiProgression.Inner_CachePool needed = AiProgression.PICKAXES.contains(itemId)
               ? AiProgression.Inner_CachePool.values()[Math.max(AiProgression.Inner_CachePool.NONE.ordinal(), AiProgression.pickaxeTier(itemId).ordinal() - 1)]
               : AiProgression.Inner_CachePool.NONE;

            for (String material : needs.keySet()) {
               if (AiProgression.tierToMine(material).ordinal() > needed.ordinal()) {
                  needed = AiProgression.tierToMine(material);
               }
            }

            String pick = AiProgression.nextPickaxeToward(have, needed);
            int woodNeed = this.plankNeedFor(needed, AiCraft.countItem("minecraft:crafting_table") > 0);
            if (this.stockWoodTried && !this.singleTable && have.ordinal() <= AiProgression.Inner_CachePool.STONE.ordinal() && AiProgression.plankEquivalents() < woodNeed) {
               this.singleTable = true;
               AutismClientAddon.LOG
                  .info(
                     "[AI] ladder for {}: trees are scarce here ({} planks' worth, {} for a table at every site): one crafting table for the whole ladder, picked back up at each site",
                     itemId,
                     AiProgression.plankEquivalents(),
                     woodNeed
                  );
               woodNeed = this.plankNeedFor(needed, AiCraft.countItem("minecraft:crafting_table") > 0);
            }

            if (pick != null
               && have.ordinal() <= AiProgression.Inner_CachePool.STONE.ordinal()
               && AiProgression.plankEquivalents() < woodNeed
               && this.failuresByStep.getOrDefault("stock-wood", 0) == 0
               && !this.stockWoodTried) {
               int logs = AiItems.countInInventory(AiItems.resolveItem("log")) + (woodNeed - AiProgression.plankEquivalents() + 3) / 4;
               boolean priorLeanBudget = this.singleTable;
               this.singleTable = true;
               int lean = this.plankNeedFor(needed, AiCraft.countItem("minecraft:crafting_table") > 0);
               this.singleTable = priorLeanBudget;
               int leanLogs = AiItems.countInInventory(AiItems.resolveItem("log")) + Math.max(0, (lean - AiProgression.plankEquivalents() + 3) / 4);
               this.stockWoodTried = true;
               return this.startStep("stock-wood", "get wood for the tools", AiProgression.gatherLogs(logs, Math.min(logs, leanLogs), Math.min(240, this.secondsLeft())));
            }

            int stoneBatch = AiProgression.stoneBatchFor(have, this.stoneNeeded());
            if (this.singleTable
               && pick != null
               && (have == AiProgression.Inner_CachePool.WOOD || have == AiProgression.Inner_CachePool.STONE)
               && AiProgression.countStoneToolBlocks() < stoneBatch
               && AiCraft.countItem("minecraft:crafting_table") == 0) {
               BlockPos table = this.findNearbyBlock(Blocks.CRAFTING_TABLE, 6);
               if (table != null) {
                  return this.startStep("reclaim-table", "pick up the crafting table", this.reclaimBlock(table, "minecraft:crafting_table"));
               }
            }

            String stoneKey = "stock-stone:" + have;
            if ((have == AiProgression.Inner_CachePool.WOOD || have == AiProgression.Inner_CachePool.STONE) && AiProgression.countStoneToolBlocks() < stoneBatch && this.failuresByStep.getOrDefault(stoneKey, 0) == 0) {
               int seconds = Math.min(240, this.secondsLeft());
               Supplier<AiOperation> gather = () -> autismclient.ai.engine.AiBlockMine.create(
                  AiJson.objectOf(
                     "blocks",
                     autismclient.ai.engine.AiBlockMine.init(AiProgression.STONE_BLOCKS),
                     "drops",
                     autismclient.ai.engine.AiBlockMine.init(AiProgression.STONE_TOOL_BLOCKS),
                     "held",
                     stoneBatch,
                     "radius",
                     48,
                     "timeoutSeconds",
                     seconds
                  )
               );
               AiOperation stairs = this.stoneStaircase(seconds, stoneBatch);
               return this.startStep(
                  stoneKey,
                  have == AiProgression.Inner_CachePool.WOOD ? "dig stone for a faster pickaxe" : "dig stone for the axe and furnace",
                  stairs == null ? gather.get() : this.firstUntilEnough(stairs, () -> AiProgression.countStoneToolBlocks() >= stoneBatch, gather)
               );
            }

            if (pick == null) {
               if (!needs.isEmpty()) {
                  if (AiCraft.countItem("minecraft:diamond") >= 3 && this.diamondMilestone(needs) == 0) {
                     return this.startStep("diamond-pick-early", "make the required diamond pick before the remaining ore", this.obtainOp(itemId, goal));
                  }

                  List<String> unfinished = new ArrayList<>();
                  if (AiCraft.countItem(itemId) < goal) {
                     unfinished.add(AiProgression.displayName(itemId));
                  }

                  kit.forEach((id, count) -> {
                     if (this.heldForKit(id) < count) {
                        unfinished.add(AiProgression.displayName(id));
                     }
                  });
                  return this.obtainMaterial(needs, "for " + (unfinished.isEmpty() ? AiProgression.displayName(itemId) : String.join(", ", unfinished)));
               } else {
                  if (AiProgression.isNetheriteGear(itemId)) {
                     return this.netheriteStep();
                  }

                  if (AiCraft.countItem(itemId) < goal) {
                     return this.startStep("final", "make " + goal + " " + AiProgression.displayName(itemId), this.obtainOp(itemId, goal));
                  }

                  for (Entry<String, Integer> e : kit.entrySet()) {
                     if (this.heldForKit(e.getKey()) < e.getValue()) {
                        return this.startStep(
                           "final:" + e.getKey(),
                           "make " + e.getValue() + " " + AiProgression.displayName(e.getKey()),
                           this.obtainOp(e.getKey(), e.getValue() - this.filledBucketCount(e.getKey()))
                        );
                     }
                  }

                  return this.finish(AiOperation.Status.SUCCEEDED, "Obtained " + goal + " " + itemId + " and the kit " + kit);
               }
            } else {
               Map<String, Integer> pickNeeds = AiProgression.keepItemsFor(pick, 1);
               return !pickNeeds.isEmpty() && AiProgression.pickaxeTier(pick).ordinal() > AiProgression.Inner_CachePool.STONE.ordinal()
                  ? this.obtainMaterial(this.addKitNeeds(pick, pickNeeds), "for a " + AiProgression.displayName(pick))
                  : this.startStep("pick:" + pick, "make a " + AiProgression.displayName(pick), this.obtainOp(pick, AiCraft.countItem(pick) + 1));
            }
         }

         AiProgression.Inner_CachePool finalTier() {
            if (AiProgression.PICKAXES.contains(itemId)) {
               return AiProgression.pickaxeTier(itemId) == AiProgression.Inner_CachePool.NETHERITE ? AiProgression.Inner_CachePool.DIAMOND : AiProgression.pickaxeTier(itemId);
            }

            AiProgression.Inner_CachePool needed = AiProgression.Inner_CachePool.NONE;

            for (String material : this.materialNeeds().keySet()) {
               if (AiProgression.tierToMine(material).ordinal() > needed.ordinal()) {
                  needed = AiProgression.tierToMine(material);
               }
            }

            return needed;
         }

         int plankNeedFor(AiProgression.Inner_CachePool neededTier, boolean tableHeld) {
            return this.plankNeedFor(neededTier, tableHeld, AiCraft.countItem("minecraft:stick"));
         }

         int plankNeedFor(AiProgression.Inner_CachePool neededTier, boolean tableHeld, int sticksHeld) {
            AiProgression.Inner_CachePool have = this.effectiveTier();
            AiProgression.Inner_CachePool last = AiProgression.PICKAXES.contains(itemId) ? this.finalTier() : neededTier;
            int smelts = 0;
            if (last.ordinal() >= AiProgression.Inner_CachePool.IRON.ordinal() && have.ordinal() < AiProgression.Inner_CachePool.IRON.ordinal()) {
               smelts += Math.max(0, (this.obsidianNeedsBucket() ? 6 : 3) - AiCraft.countItem("minecraft:iron_ingot"));
            }

            Map<String, Integer> pending = new LinkedHashMap<>(this.materialNeeds());
            if (last.ordinal() >= AiProgression.Inner_CachePool.IRON.ordinal() && have.ordinal() < AiProgression.Inner_CachePool.IRON.ordinal()) {
               AiProgression.keepItemsFor("minecraft:iron_pickaxe", 1)
                  .forEach((id, n) -> pending.merge(id, n, itemId.equals("minecraft:iron_pickaxe") ? Math::max : Integer::sum));
            }

            int jointSmelts = 0;

            for (Entry<String, Integer> e : pending.entrySet()) {
               if (AiProgression.SMELT_INPUTS.containsKey(e.getKey())) {
                  jointSmelts += e.getValue();
               }
            }

            smelts = Math.max(smelts, jointSmelts);
            boolean coal = AiCraft.countItem("minecraft:coal") + AiCraft.countItem("minecraft:charcoal") > 0;
            AiProgression.Inner_DataUtil remainingWood = AiProgression.woodForKit(AiCraft.buildRecipeGraph(), kit, itemId, goal);
            return remainingWood != null
               ? AiProgression.plankNeed(have, last, tableHeld, AiProgression.hasWeapon(mc), sticksHeld, smelts, coal, this.singleTable, remainingWood)
               : AiProgression.plankNeed(have, last, tableHeld, AiProgression.hasWeapon(mc), sticksHeld, smelts, coal, this.singleTable) + (this.kitComplete() ? 0 : kitPlanks);
         }

         AiOperation.Result netheriteStep() {
            String diamond = itemId.replace("minecraft:netherite_", "minecraft:diamond_");
            if (AiCraft.countItem(diamond) == 0) {
               return this.startStep("smith-base", "make a " + AiProgression.displayName(diamond) + " to upgrade", this.obtainOp(diamond, 1));
            } else if (AiCraft.countItem("minecraft:netherite_ingot") == 0) {
               return this.startStep(
                  "smith-ingot",
                  "make a netherite ingot",
                  AiProgression.obtain(AiJson.objectOf("timeoutSeconds", this.secondsLeft()), "minecraft:netherite_ingot", 1)
               );
            } else if (AiCraft.countItem("minecraft:netherite_upgrade_smithing_template") == 0) {
               return this.finish(
                  AiOperation.Status.FAILED,
                  "Ready to upgrade: "
                     + AiProgression.displayName(diamond)
                     + " and a netherite ingot are in the bag, but the upgrade needs a netherite upgrade smithing template, which only generates in bastion remnant chests - loot one (or ask the AI to raid a bastion) and ask again"
               );
            } else {
               return AiCraft.countItem("minecraft:smithing_table") == 0 && this.findNearbyBlock(Blocks.SMITHING_TABLE, 12) == null
                  ? this.startStep("smith-table", "make a smithing table", this.obtainOp("minecraft:smithing_table", 1))
                  : this.startStep(
                     "smith",
                     "upgrade the " + AiProgression.displayName(diamond) + " to " + AiProgression.displayName(itemId),
                     AiSmith.init(
                        AiJson.objectOf(
                           "base",
                           diamond,
                           "addition",
                           "minecraft:netherite_ingot",
                           "template",
                           "minecraft:netherite_upgrade_smithing_template",
                           "timeoutSeconds",
                           Math.max(60, this.secondsLeft())
                        )
                     )
                  );
            }
         }

         AiOperation.Result obtainMaterial(Map<String, Integer> needs, String why) {
            String material = AiProgression.orderOverworldFirst(needs.keySet()).get(0);
            int missing = needs.get(material);
            if (material.equals("minecraft:diamond")) {
               int milestone = this.diamondMilestone(needs);
               if (milestone > 0) {
                  missing = Math.min(missing, milestone);
               }
            }

            String raw = AiProgression.SMELT_INPUTS.get(material);
            if (raw == null) {
               return this.obtainOreDrop(material, missing, why);
            }

            if (material.equals("minecraft:iron_ingot") && this.obsidianNeedsBucket()) {
               missing += Math.max(0, 3 - Math.max(0, AiCraft.countItem("minecraft:iron_ingot") - needs.get(material)));
            }

            if (this.init_BlockPos != null && material.equals(this.close_Str)) {
               return this.smeltWaitStep();
            } else {
               int rawHeld = AiCraft.countItem(raw);
               int batch = Math.min(3, missing);
               if (rawHeld < batch) {
                  return this.obtainOreDrop(raw, batch - rawHeld, why);
               } else {
                  AiOperation.Result kitx = this.prepareSmelting(batch, material, raw);
                  if (kitx != null) {
                     return kitx;
                  } else {
                     String fuel = this.fuelFor(batch);
                     if ((missing > batch || this.oreWhileSmelting(material) != null) && this.failuresByStep.getOrDefault("smelt-load:" + material, 0) == 0) {
                        this.close_Str = material;
                        this.smeltInput = raw;
                        this.inputHeldBeforeLoad = rawHeld;
                        this.smeltBatch = batch;
                        this.returnToFurnace = false;
                        this.close_Z = false;
                        this.blockedStairCells.clear();
                        return this.startStep(
                           "smelt-load:" + material,
                           "load the furnace with " + AiProgression.displayName(raw) + " " + why + " and work on while it smelts",
                           AiSmelt.load(
                              AiJson.objectOf(
                                 "item",
                                 material,
                                 "count",
                                 AiCraft.countItem(material) + batch,
                                 "input",
                                 raw,
                                 "fuel",
                                 fuel,
                                 "loadOnly",
                                 true,
                                 "timeoutSeconds",
                                 Math.max(60, Math.min(180, this.secondsLeft()))
                              )
                           )
                        );
                     } else {
                        return this.startStep(
                           "smelt:" + material,
                           "smelt " + batch + " " + AiProgression.displayName(material) + " " + why,
                           AiSmelt.init(
                              AiJson.objectOf(
                                 "item",
                                 material,
                                 "count",
                                 AiCraft.countItem(material) + batch,
                                 "input",
                                 raw,
                                 "fuel",
                                 fuel,
                                 "timeoutSeconds",
                                 Math.max(60, this.secondsLeft())
                              ),
                              this.preworkResources(raw, batch)
                           )
                        );
                     }
                  }
               }
            }
         }

         int diamondMilestone(Map<String, Integer> needs) {
            if (itemId.equals("minecraft:diamond_pickaxe")
               && goal == 1
               && !kit.isEmpty()
               && AiCraft.countItem(itemId) < goal
               && this.failuresByStep.getOrDefault("diamond-pick-early", 0) <= 0) {
               int missing = needs.getOrDefault("minecraft:diamond", 0);
               if (missing <= 0) {
                  return -1;
               }

               BlockState ordinaryOre = Blocks.DIAMOND_ORE.defaultBlockState();
               int slot = autismclient.ai.engine.MiningCost.load(mc, ordinaryOre, true);
               if (slot < 0) {
                  return -1;
               }

               ItemStack current = mc.player.getInventory().getItem(slot);
               ItemStack upgraded = new ItemStack(Items.DIAMOND_PICKAXE);
               double before = autismclient.ai.engine.MiningCost.breakTicks(mc, current, ordinaryOre, ordinaryOre.getBlock().defaultDestroyTime());
               double after = autismclient.ai.engine.MiningCost.breakTicks(mc, upgraded, ordinaryOre, ordinaryOre.getBlock().defaultDestroyTime());
               if (after >= before) {
                  return -1;
               }

               boolean table = AiCraft.isCraftingTableInReach() || AiCraft.countItem("minecraft:crafting_table") > 0;
               AiCraftPlanner.CraftPlan plan = AiMiningPreparation.init(AiCraft.buildRecipeGraph(), itemId, table, "", 0);
               int firstPick = Math.max(0, 3 - AiCraft.countItem("minecraft:diamond"));
               boolean affordable = plan.complete()
                  || plan.missing().size() == 1
                     && plan.missing().getOrDefault("minecraft:diamond", 0) <= firstPick
                     && plan.missing().containsKey("minecraft:diamond");
               return AiProgression.earlyDiamondPicks(missing, AiCraft.countItem("minecraft:diamond"), before, after, 40 + (table ? 0 : 60), affordable);
            } else {
               return -1;
            }
         }

         AiProgression.Inner_StreamUtil oreWhileSmelting(String material) {
            if (AiRunResources.isCampaignActive() && AiCraft.countItem("minecraft:water_bucket") < 2) {
               return null;
            }

            if (material.equals("minecraft:iron_ingot")
               && this.finalTier().ordinal() >= AiProgression.Inner_CachePool.DIAMOND.ordinal()
               && AiProgression.bestTier(mc).ordinal() < AiProgression.Inner_CachePool.DIAMOND.ordinal()
               && AiCraft.countItem("minecraft:diamond") < 3) {
               return AiProgression.ORE_SOURCES.get("minecraft:diamond");
            }

            for (String m : AiProgression.orderOverworldFirst(this.materialNeeds().keySet())) {
               if (!m.equals(material)) {
                  AiProgression.Inner_StreamUtil o = AiProgression.ORE_SOURCES.get(m);
                  if (o == null && AiProgression.SMELT_INPUTS.containsKey(m)) {
                     o = AiProgression.ORE_SOURCES.get(AiProgression.SMELT_INPUTS.get(m));
                  }

                  if (o != null && !o.nether()) {
                     return o;
                  }
               }
            }

            return null;
         }

         void onStepSucceeded(AiOperation.Result r) {
            if (this.stepKey.startsWith("smelt-load:")) {
               BlockPos at = null;
               String fuel = "";
               int cookTicks = 200;
               double progress = 0.0;

               try {
                  JsonObject data = JsonParser.parseString(r.dataJson()).getAsJsonObject();
                  at = AiProgression.parseBlockPos(AiJson.getString(data, "stationAt", ""));
                  fuel = AiJson.getString(data, "fuel", "");
                  this.smeltStation = AiJson.getString(data, "station", "minecraft:furnace");
                  cookTicks = Math.max(1, AiJson.getInt(data, "cookTicksPerItem", 200));
                  if (data.has("furnace")) {
                     progress = AiJson.getDouble(data.getAsJsonObject("furnace"), "cookProgress", 0.0);
                  }
               } catch (RuntimeException var9) {
               }

               int loaded = this.inputHeldBeforeLoad - AiCraft.countItem(this.smeltInput);
               if (at == null || loaded <= 0 || fuel.isEmpty()) {
                  this.failuresByStep.merge(this.stepKey, 1, Integer::sum);
                  return;
               }

               this.init_BlockPos = at;
               this.smeltLoadedFrom = mc.player.blockPosition();
               this.smeltFuel = fuel;
               int cooking = Math.min(loaded, this.smeltBatch);
               this.smeltTarget = AiCraft.countItem(this.close_Str) + cooking;
               this.read_J = System.nanoTime() + Math.max(0L, (long)((double)cooking * cookTicks - progress * cookTicks)) * 50000000L + 500000000L;
               AutismClientAddon.LOG
                  .info(
                     "[AI] ladder for {}: {} {} cooking at {}, ready in {} s",
                     itemId,
                     cooking,
                     AiProgression.displayName(this.close_Str),
                     at.toShortString(),
                     (this.read_J - System.nanoTime()) / 1000000000L
                  );
            } else if (this.stepKey.startsWith("collect:") && (r.status() == AiOperation.Status.SUCCEEDED || AiCraft.countItem(this.close_Str) >= this.smeltTarget)) {
               this.init_BlockPos = null;
            }
         }

         AiFurnaceWork.Inner_DataUtil preworkResources(String input, int committed) {
            Map<String, Integer> resources = new LinkedHashMap<>();

            for (Entry<String, Integer> need : this.materialNeeds().entrySet()) {
               String raw = AiProgression.SMELT_INPUTS.getOrDefault(need.getKey(), need.getKey());
               if (AiProgression.ORE_SOURCES.containsKey(raw)) {
                  int held = Math.max(0, AiCraft.countItem(raw) - (raw.equals(input) && this.init_BlockPos == null ? committed : 0));
                  resources.put(raw, Math.max(held, need.getValue() - (raw.equals(input) ? committed : 0)));
               }
            }

            if (this.finalTier().ordinal() >= AiProgression.Inner_CachePool.DIAMOND.ordinal()
               && AiProgression.bestTier(mc).ordinal() < AiProgression.Inner_CachePool.DIAMOND.ordinal()) {
               resources.merge("minecraft:diamond", 3, Math::max);
            }

            resources.put("minecraft:coal", Math.max(2, Math.min(8, this.smeltBatch)));
            Map<String, Integer> crafts = this.finalTier().ordinal() > AiProgression.bestTier(mc).ordinal() ? Map.of("minecraft:stick", 2) : Map.of();
            return new AiFurnaceWork.Inner_DataUtil(resources, crafts);
         }

         AiOperation.Result smeltWaitStep() {
            long now = System.nanoTime();
            if (!this.returnToFurnace && this.read_J - now - this.init_J_BlockPos(this.init_BlockPos) > 3000000000L) {
               int window = (int)Math.max(0L, Math.min(2147483647L, (this.read_J - now) / 50000000L));
               if (this.smeltLoadedFrom != null) {
                  return this.startStep(
                     "prework",
                     "mine more resources along a clear return route while the " + AiProgression.displayName(this.close_Str) + " cooks",
                     new AiFurnaceWork(
                        this.init_BlockPos,
                        this.smeltLoadedFrom,
                        this.smeltInput,
                        window,
                        this.preworkResources(this.smeltInput, Math.max(0, this.inputHeldBeforeLoad - AiCraft.countItem(this.smeltInput)))
                     )
                  );
               }
            }

            this.returnToFurnace = true;
            int seconds = (int)Math.max(60L, Math.min(this.secondsLeft(), (this.read_J - now) / 1000000000L + 90L));
            AiOperation collect = AiSmelt.load(
               AiJson.objectOf(
                  "item",
                  this.close_Str,
                  "count",
                  this.smeltTarget,
                  "input",
                  this.smeltInput,
                  "fuel",
                  this.smeltFuel,
                  "collect",
                  true,
                  "station",
                  this.smeltStation,
                  "x",
                  this.init_BlockPos.getX(),
                  "y",
                  this.init_BlockPos.getY(),
                  "z",
                  this.init_BlockPos.getZ(),
                  "timeoutSeconds",
                  seconds
               )
            );
            AiOperation back = autismclient.ai.engine.AiBlockInteraction.findReachableHit(mc, this.init_BlockPos) != null
               ? null
               : new AiFurnaceReturn(this.init_BlockPos, "Collect the background batch after productive work");
            return this.startStep(
               "collect:" + this.close_Str,
               "collect the " + AiProgression.displayName(this.close_Str) + " from the furnace at " + this.init_BlockPos.toShortString(),
               back == null ? collect : this.sequence(back, collect)
            );
         }

         AiOperation stairsToOre(AiProgression.Inner_StreamUtil next) {
            BlockPos feet = mc.player.blockPosition();
            Predicate<BlockState> matches = autismclient.ai.engine.AiStaircase.init(next.family());
            BlockPos target = this.oreTargetByFamily.get(next.family());
            if (target != null && (!matches.test(mc.level.getBlockState(target)) || !this.safeToMine(target))) {
               target = null;
            }

            if (target == null) {
               target = autismclient.ai.engine.AiStaircase.load(mc.level, feet, 64, 1, matches, this::safeToMine);
            }

            if (target != null) {
               this.oreTargetByFamily.put(next.family(), target);
            }

            if (target == null) {
               Direction facing = mc.player.getDirection();
               target = new BlockPos(feet.getX() + facing.getStepX() * 512, next.bestY(), feet.getZ() + facing.getStepZ() * 512);
            }

            Predicate<BlockPos> protectedCells = autismclient.ai.engine.AiStaircase.init(
               mc.level, state -> matches.test(state) || state.requiresCorrectToolForDrops() && !this.canHarvest(state)
            );
            Predicate<BlockPos> blocked = cell -> this.blockedStairCells.contains(cell.asLong()) || protectedCells.test(cell);
            Predicate<BlockPos> noFloor = autismclient.ai.engine.AiStaircase.init(mc.level);
            List<BlockPos> steps = autismclient.ai.engine.AiStaircase.read(feet, target, 48, blocked, noFloor);
            if (steps.isEmpty()) {
               return null;
            }

            this.firstStairStep = steps.get(0);
            int seconds = (int)Math.max(20L, (this.read_J - System.nanoTime()) / 1000000000L + 10L);
            return autismclient.ai.engine.AiStaircase.init(steps, seconds);
         }

         AiOperation stoneStaircase(int seconds, int stockGoal) {
            BlockPos feet = mc.player.blockPosition();
            AiProgression.Inner_StreamUtil next = this.finalTier().ordinal() >= AiProgression.Inner_CachePool.IRON.ordinal() ? AiProgression.ORE_SOURCES.get("minecraft:raw_iron") : null;
            Predicate<BlockState> ore = next == null ? state -> false : autismclient.ai.engine.AiStaircase.init(next.family());
            BlockPos target = next == null ? null : this.oreTargetByFamily.get(next.family());
            if (target != null && (!ore.test(mc.level.getBlockState(target)) || !this.safeToMine(target))) {
               target = null;
            }

            if (target == null && next != null) {
               target = autismclient.ai.engine.AiStaircase.init(
                  mc.level, feet, 48, 4, ore, this::safeToMine, Math.max(1, 3 - AiCraft.countItem("minecraft:raw_iron") - AiCraft.countItem("minecraft:iron_ingot"))
               );
            }

            boolean toOre = target != null;
            if (toOre) {
               this.oreTargetByFamily.put(next.family(), target);
            }

            if (target == null) {
               Direction facing = mc.player.getDirection();
               target = new BlockPos(
                  feet.getX() + facing.getStepX() * 512, next == null ? feet.getY() - 40 : next.bestY(), feet.getZ() + facing.getStepZ() * 512
               );
            }

            Predicate<BlockState> keep = state -> ore.test(state) || state.requiresCorrectToolForDrops() && !this.canHarvest(state);
            if (toOre) {
               BlockPos top = autismclient.ai.engine.AiStaircase.findStairTop(mc.level, feet, target, autismclient.ai.engine.AiStaircase.init(mc.level, keep), 40);
               if (top != null) {
                  int inShaft = AiProgression.countStoneToolBlocks();

                  for (int y = top.getY() - 1; y >= target.getY(); y--) {
                     if (AiProgression.STONE_BLOCKS
                        .contains(BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(new BlockPos(top.getX(), y, top.getZ())).getBlock()).toString())) {
                        inShaft++;
                     }
                  }

                  if (inShaft >= stockGoal) {
                     AutismClientAddon.LOG
                        .info(
                           "[AI] ladder for {}: stone from a shaft beside the iron at {}, {} deep ({} stone in it)",
                           itemId,
                           top.toShortString(),
                           top.getY() - target.getY(),
                           inShaft - AiProgression.countStoneToolBlocks()
                        );
                     return autismclient.ai.engine.AiStaircase.digDownOperation(top, target.getY(), seconds, () -> AiProgression.countStoneToolBlocks() >= stockGoal);
                  }
               }
            }

            BlockPos start = toOre ? autismclient.ai.engine.AiStaircase.findStairStart(mc.level, feet, target) : null;
            List<BlockPos> steps = autismclient.ai.engine.AiStaircase.planSteps(
               start != null ? start : feet, target, 32, autismclient.ai.engine.AiStaircase.init(mc.level, keep), autismclient.ai.engine.AiStaircase.init(mc.level)
            );
            if (start != null && steps.size() < 2) {
               start = null;
               steps = autismclient.ai.engine.AiStaircase.planSteps(feet, target, 32, autismclient.ai.engine.AiStaircase.init(mc.level, keep), autismclient.ai.engine.AiStaircase.init(mc.level));
            }

            if (start != null) {
               int on = steps.indexOf(feet);
               if (on >= 0) {
                  steps = new ArrayList<>(steps.subList(on + 1, steps.size()));
                  start = null;
               }
            }

            boolean exposedNear = this.exposedStoneNear(feet, 5);
            int reach = exposedNear ? 9 : 23;
            int stone = AiProgression.countStoneToolBlocks();
            int enough = -1;
            int last = Math.min(steps.size(), reach + 1) - 1;

            for (int i = 0; i <= last && enough < 0; i++) {
               for (int h = 0; h < 3; h++) {
                  if (AiProgression.STONE_BLOCKS.contains(BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(steps.get(i).above(h)).getBlock()).toString())) {
                     stone++;
                  }
               }

               if (stone >= stockGoal) {
                  enough = i;
               }
            }

            if (enough < 0 && last >= 0 && stone >= stockGoal - 3) {
               enough = last;
            }

            if (enough < 0 && toOre && !steps.isEmpty() && steps.size() <= reach + 1 && stone > AiProgression.countStoneToolBlocks()) {
               enough = steps.size() - 1;
            }

            if (enough < 0) {
               AutismClientAddon.LOG
                  .info(
                     "[AI] ladder for {}: no stone within {} stair steps of {} ({} steps toward {}, {} stone in them); gathering it instead",
                     itemId,
                     10,
                     feet.toShortString(),
                     steps.size(),
                     target.toShortString(),
                     stone - AiProgression.countStoneToolBlocks()
                  );
               return null;
            }

            boolean toTheIron = this.effectiveTier() != AiProgression.Inner_CachePool.WOOD
               && toOre
               && !steps.isEmpty()
               && steps.size() <= Math.min(24, Math.max(0, enough) + 5)
               && Math.abs(steps.get(steps.size() - 1).getX() - target.getX()) <= 3
               && Math.abs(steps.get(steps.size() - 1).getZ() - target.getZ()) <= 3
               && Math.abs(steps.get(steps.size() - 1).getY() - target.getY()) <= 2;
            int dig = toTheIron ? steps.size() : Math.min(steps.size(), enough + 2);
            AutismClientAddon.LOG
               .info(
                  "[AI] ladder for {}: stone stairs toward {} ({}), {} of {} steps, {} stone counted, {} held",
                  itemId,
                  target.toShortString(),
                  toTheIron ? "to the iron" : (toOre ? "the iron" : "ahead"),
                  dig,
                  steps.size(),
                  stone - AiProgression.countStoneToolBlocks(),
                  AiProgression.countStoneToolBlocks()
               );
            steps = steps.subList(0, dig);
            if (start != null) {
               AutismClientAddon.LOG
                  .info(
                     "[AI] ladder for {}: the stairs start at {}, {} across, so they end beside the iron",
                     itemId,
                     start.toShortString(),
                     Math.abs(start.getX() - feet.getX()) + Math.abs(start.getZ() - feet.getZ())
                  );
            }

            return autismclient.ai.engine.AiStaircase.followStepsOperation(steps, seconds, toTheIron ? () -> false : () -> AiProgression.countStoneToolBlocks() >= stockGoal, start);
         }

         boolean safeToMine(BlockPos pos) {
            for (Direction side : Direction.values()) {
               BlockPos n = pos.relative(side);
               FluidState fluid = mc.level.getFluidState(n);
               if (fluid.is(FluidTags.LAVA)) {
                  return false;
               }

               if (side != Direction.DOWN && fluid.is(FluidTags.WATER)) {
                  return false;
               }

               if (mc.level.getBlockState(n).isAir() && mc.level.getBrightness(LightLayer.SKY, n) == 0 && !autismclient.ai.engine.AiEditLedger.update(n)) {
                  return false;
               }
            }

            return true;
         }

         boolean exposedStoneNear(BlockPos feet, int radius) {
            for (BlockPos p : BlockPos.betweenClosed(feet.offset(-radius, -radius, -radius), feet.offset(radius, radius, radius))) {
               if (AiProgression.STONE_BLOCKS.contains(BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(p).getBlock()).toString())) {
                  for (Direction d : Direction.values()) {
                     if (mc.level.getBlockState(p.relative(d)).isAir()) {
                        return true;
                     }
                  }
               }
            }

            return false;
         }

         boolean canHarvest(BlockState state) {
            Inventory inventory = mc.player.getInventory();

            for (int i = 0; i < inventory.getContainerSize(); i++) {
               if (inventory.getItem(i).isCorrectToolForDrops(state)) {
                  return true;
               }
            }

            return false;
         }

         AiOperation firstUntilEnough(final AiOperation first, final BooleanSupplier enough, final Supplier<AiOperation> second) {
            return new AiOperation() {
               AiOperation fallback;

               @Override
               public AiOperation.Result tick() {
                  if (this.fallback != null) {
                     return this.fallback.tick();
                  }

                  AiOperation.Result r = first.tick();
                  if (!r.terminal()) {
                     return r;
                  }

                  if (enough.getAsBoolean()) {
                     return r;
                  }

                  AutismClientAddon.LOG.info("[AI] ladder for {}: staircase ended short ({} {}); gathering the rest", itemId, r.status(), r.message());
                  this.fallback = second.get();
                  return this.fallback.tick();
               }

               @Override
               public boolean canInterruptNow() {
                  return (this.fallback == null ? first : this.fallback).canInterruptNow();
               }

               @Override
               public boolean requiresReconciliationAfterEmergency() {
                  return (this.fallback == null ? first : this.fallback).requiresReconciliationAfterEmergency();
               }

               @Override
               public void cancel() {
                  first.cancel();
                  if (this.fallback != null) {
                     this.fallback.cancel();
                  }
               }
            };
         }

         boolean noEnemiesWithin(double radius) {
            for (Entity entity : mc.level.entitiesForRendering()) {
               if (entity instanceof Enemy && entity.isAlive() && entity.distanceToSqr(mc.player) <= radius * radius) {
                  return false;
               }
            }

            return true;
         }

         AiOperation sequence(final AiOperation first, final AiOperation second) {
            return new AiOperation() {
               boolean firstDone;

               @Override
               public AiOperation.Result tick() {
                  if (!this.firstDone) {
                     AiOperation.Result r = first.tick();
                     if (!r.terminal()) {
                        return r;
                     }

                     first.cancel();
                     this.firstDone = true;
                  }

                  return second.tick();
               }

               @Override
               public boolean canInterruptNow() {
                  return (this.firstDone ? second : first).canInterruptNow();
               }

               @Override
               public boolean requiresReconciliationAfterEmergency() {
                  return (this.firstDone ? second : first).requiresReconciliationAfterEmergency();
               }

               @Override
               public void cancel() {
                  first.cancel();
                  second.cancel();
               }
            };
         }

         AiOperation untilFurnaceReady(final AiOperation inner) {
            return new AiOperation() {
               int waitTicks;

               @Override
               public AiOperation.Result tick() {
                  if (inner.canInterruptNow() && System.nanoTime() + init_J_BlockPos(init_BlockPos) >= read_J && (!write_Z_7() || this.waitTicks++ >= 20)) {
                     close_Z = true;
                     inner.cancel();
                     AutismClientAddon.LOG
                        .info(
                           "[AI] ladder for {}: time to walk back to the furnace ({} s of cooking left, {} s back)",
                           itemId,
                           Math.max(0L, (read_J - System.nanoTime()) / 1000000000L),
                           init_J_BlockPos(init_BlockPos) / 1000000000L
                        );
                     return AiGameOperations.result(AiOperation.Status.SUCCEEDED, "Time to go back for the " + AiProgression.displayName(close_Str), init_JsonObject_45());
                  } else {
                     return inner.tick();
                  }
               }

               @Override
               public boolean canInterruptNow() {
                  return inner.canInterruptNow();
               }

               @Override
               public boolean requiresReconciliationAfterEmergency() {
                  return inner.requiresReconciliationAfterEmergency();
               }

               @Override
               public void cancel() {
                  inner.cancel();
               }
            };
         }

         boolean write_Z_7() {
            return mc.gameMode instanceof AutismMultiPlayerGameModeAccessor dig
               && dig.autism$getDestroyBlockPos() != null
               && dig.autism$getDestroyProgress() >= 0.5F
               && !mc.level.getBlockState(dig.autism$getDestroyBlockPos()).isAir();
         }

         long init_J_BlockPos(BlockPos home) {
            return (long)(AiProgression.travelSeconds(home.getX() + 0.5 - mc.player.getX(), home.getY() - mc.player.getY(), home.getZ() + 0.5 - mc.player.getZ()) * 1.0E9);
         }

         AiOperation.Result obtainOreDrop(String drop, int count, String why) {
            AiProgression.Inner_StreamUtil ore = AiProgression.ORE_SOURCES.get(drop);
            if (AiRunResources.isCampaignActive() && drop.equals("minecraft:diamond") && !this.inNether() && AiCraft.countItem("minecraft:water_bucket") < 2) {
               if (this.failuresByStep.getOrDefault("expedition-water", 0) >= 3) {
                  return this.finish(AiOperation.Status.FAILED, "Diamond descent blocked: both water buckets could not be filled");
               }

               int emptyNeeded = 2 - AiCraft.countItem("minecraft:water_bucket");
               int ironNeeded = Math.max(0, emptyNeeded - AiCraft.countItem("minecraft:bucket")) * 3;
               return AiCraft.countItem("minecraft:iron_ingot") < ironNeeded
                  ? this.startStep(
                     "expedition-water",
                     "secure iron for both expedition buckets",
                     AiObtain.init(AiJson.objectOf("item", "minecraft:iron_ingot", "count", ironNeeded, "timeoutSeconds", Math.min(240, this.secondsLeft())))
                  )
                  : this.startStep("expedition-water", "fill both water buckets before diamond descent", AiObsidian.init(2, Math.min(180, this.secondsLeft())));
            } else {
               if (itemId.equals("minecraft:obsidian") && drop.equals("minecraft:diamond") && AiCraft.countItem("minecraft:water_bucket") == 0) {
                  return this.startStep("obsidian-water", "fill the obsidian bucket before descending for diamonds", AiObsidian.fetchOne(this.secondsLeft()));
               }

               if (drop.equals("minecraft:obsidian")) {
                  return this.startStep("ore:" + drop, "obtain " + count + " obsidian " + why, AiObsidian.load(AiCraft.countItem(drop) + count, this.secondsLeft()));
               }

               if (!ore.nether() || this.inNether()) {
                  AiProgression.Inner_CachePool last = this.finalTier();
                  if (AiProgression.needsCrafting(itemId, drop) && AiCraft.countItem("minecraft:crafting_table") == 0) {
                     BlockPos table = this.findNearbyBlock(Blocks.CRAFTING_TABLE, 6);
                     if (table != null && AiProgression.shouldReclaimTable(this.singleTable, AiProgression.plankEquivalents(), this.plankNeedFor(last, true))) {
                        return this.startStep("reclaim-table", "pick up the crafting table", this.reclaimBlock(table, "minecraft:crafting_table"));
                     }

                     if (this.singleTable && table == null && !this.tableRecoveryTried) {
                        this.tableRecoveryTried = true;
                        AiOperation recovery = AiProgression.init(Math.min(90, this.secondsLeft()));
                        if (recovery != null) {
                           return this.startStep("reclaim-table", "recover the observed table before acquiring new wood", recovery);
                        }
                     }

                     return this.startStep("table", "make a crafting table to carry", this.obtainOp("minecraft:crafting_table", 1));
                  } else {
                     int woodNeed = this.plankNeedFor(last, true);
                     if (AiProgression.needsCrafting(itemId, drop) && AiProgression.plankEquivalents() < woodNeed) {
                        return this.startStep(
                           "wood",
                           "get wood for sticks ("
                              + AiProgression.plankEquivalents()
                              + " planks' worth: "
                              + AiItems.countInInventory(AiItems.resolveItem("planks"))
                              + " planks, "
                              + AiItems.countInInventory(AiItems.resolveItem("log"))
                              + " logs, "
                              + AiCraft.countItem("minecraft:stick")
                              + " sticks; need "
                              + woodNeed
                              + ")",
                           AiProgression.gatherLogs(AiItems.countInInventory(AiItems.resolveItem("log")) + (woodNeed - AiProgression.plankEquivalents() + 3) / 4, Math.min(180, this.secondsLeft()))
                        );
                     }

                     AiItems.ensureInHotbar(AiItems.resolveItem(AiProgression.pickaxeFor(AiProgression.bestTier(mc))));
                     return this.startStep(
                        "ore:" + drop,
                        "mine " + count + " " + AiProgression.displayName(drop) + " " + why,
                        AiOreRoute.init(
                           ore.family(), drop, AiCraft.countItem(drop) + count, AiProgression.oresForDrops(ore, count), ore.bestY(), this.secondsLeft(), this.oreTargetByFamily.remove(ore.family())
                        )
                     );
                  }
               } else if (AiCraft.countItem("minecraft:furnace") == 0) {
                  return this.startStep("pack-furnace", "make a furnace to take to the Nether", this.obtainOp("minecraft:furnace", 1));
               } else if (AiCraft.countItem("minecraft:coal") < 4 && this.failuresByStep.getOrDefault("pack-coal", 0) == 0) {
                  return this.startStep("pack-coal", "mine coal to take to the Nether", AiOreRoute.init("coal_ore", "minecraft:coal", 4, 4, Math.min(240, this.secondsLeft())));
               } else {
                  return AiCraft.countItem("minecraft:crafting_table") == 0
                     ? this.startStep("pack-table", "make a crafting table to take along", this.obtainOp("minecraft:crafting_table", 1))
                     : this.startStep(
                        "nether",
                        "get to the Nether",
                        AiNetherEntry.init(AiJson.objectOf("skipVillage", true, "timeoutSeconds", Math.max(600, this.secondsLeft())))
                     );
               }
            }
         }

         AiOperation.Result prepareSmelting(int items, String output, String input) {
            if (!AiSmelt.init(mc, output, input)) {
               return this.startStep("furnace", "make a furnace", this.obtainOp("minecraft:furnace", 1));
            }

            if (this.fuelFor(items) != null) {
               return null;
            }

            int largestPlanks = 0;

            for (Item plank : AiItems.resolveItem("planks").items()) {
               largestPlanks = Math.max(largestPlanks, AiCraft.countItem(BuiltInRegistries.ITEM.getKey(plank).toString()));
            }

            int planksShort = (items * 200 + 299) / 300 - largestPlanks;
            if (AiItems.countInInventory(AiItems.resolveItem("log")) > 0 && this.failuresByStep.getOrDefault("fuel-planks", 0) == 0) {
               AiOperation planks = AiPlanks.init(mc.player.getInventory(), planksShort);
               if (planks != null) {
                  return this.startStep("fuel-planks", "turn a log into planks to burn", planks);
               }
            }

            return AiCraft.countItem("minecraft:coal") == 0 && this.failuresByStep.getOrDefault("fuel-coal", 0) == 0
               ? this.startStep("fuel-coal", "mine coal for fuel", AiOreRoute.init("coal_ore", "minecraft:coal", 2, 2, Math.min(120, this.secondsLeft())))
               : this.startStep("fuel-wood", "get wood to burn", AiProgression.gatherLogs(AiItems.countInInventory(AiItems.resolveItem("log")) + 1, Math.min(180, this.secondsLeft())));
         }

         String fuelFor(int items) {
            long needed = items * 200L;

            for (String coal : List.of("minecraft:coal", "minecraft:charcoal")) {
               if (AiCraft.countItem(coal) * 1600L >= needed) {
                  return coal;
               }
            }

            for (Item plank : AiItems.resolveItem("planks").items()) {
               String id = BuiltInRegistries.ITEM.getKey(plank).toString();
               if (AiCraft.countItem(id) * 300L >= needed) {
                  return id;
               }
            }

            return null;
         }

         AiOperation obtainOp(String item, int count) {
            return AiObtain.init(AiJson.objectOf("item", item, "count", count, "staged", true, "timeoutSeconds", Math.max(60, this.secondsLeft())));
         }

         AiOperation reclaimBlock(BlockPos pos, String item) {
            return AiProgression.reclaimStation(pos, item, 30);
         }

         BlockPos findNearbyBlock(Block block, int radius) {
            BlockPos feet = mc.player.blockPosition();
            BlockPos best = null;
            double bestSq = 1.7976931348623157E308;

            for (BlockPos p : BlockPos.betweenClosed(feet.offset(-radius, -3, -radius), feet.offset(radius, 3, radius))) {
               if ((block != Blocks.CRAFTING_TABLE || !p.equals(feet.below())) && mc.level.getBlockState(p).is(block)) {
                  double sq = p.distSqr(feet);
                  if (sq < bestSq) {
                     bestSq = sq;
                     best = p.immutable();
                  }
               }
            }

            return best;
         }

         AiOperation.Result startStep(String key, String label, AiOperation op) {
            this.reservedWood = this.plankNeedFor(this.finalTier(), AiCraft.countItem("minecraft:crafting_table") > 0);
            autismclient.ai.engine.AiRestock.reserveWoodForJob(this.reservedWood);
            this.reservedStone = this.stoneNeeded()
               + (this.finalTier().ordinal() >= AiProgression.Inner_CachePool.DIAMOND.ordinal() && AiProgression.bestTier(mc) == AiProgression.Inner_CachePool.IRON ? 3 : 0);
            autismclient.ai.engine.AiRestock.reserveStoneForJob(this.reservedStone);
            this.stepOp = AiRunDiagnostics.trace("progression", key + ": " + label, op);
            this.stepKey = key;
            this.stepLabel = label;
            AutismClientAddon.LOG
               .info(
                  "[AI] ladder for {}: {} (pickaxe {}, y {}, {} s left; wood {} planks' worth, {} sticks, {} held back)",
                  itemId,
                  label,
                  AiProgression.bestTier(mc),
                  mc.player.getBlockY(),
                  this.secondsLeft(),
                  AiProgression.plankEquivalents(),
                  AiCraft.countItem("minecraft:stick"),
                  this.reservedWood
               );
            return this.running(label);
         }

         boolean inNether() {
            return "minecraft:the_nether".equals(mc.level.dimension().identifier().toString());
         }

         JsonArray keepIds() {
            JsonArray keep = new JsonArray();
            AiProgression.keepIds(itemId, kit.keySet()).forEach(keep::add);
            return keep;
         }

         AiOperation.Result nightStep() {
            if (this.nightStepActive) {
               return null;
            }

            long time = AiSleep.dayTime();
            long day = mc.level.getOverworldClockTime() / 24000L;
            if (time >= 12500L && time <= 22000L && day != AiProgression.lastShelterDay && System.nanoTime() >= AiProgression.shelterRetryAt) {
               if (day != AiProgression.shelterAttemptDay) {
                  AiProgression.shelterAttemptDay = day;
                  AiProgression.shelterAttempts = 0;
               }

               if (!"minecraft:overworld".equals(mc.level.dimension().identifier().toString())) {
                  return null;
               }

               BlockPos feet = mc.player.blockPosition();
               if (mc.level.getHeight(Types.MOTION_BLOCKING, feet.getX(), feet.getZ()) > feet.getY() + 1) {
                  return null;
               }

               if (AiOracle.isActive() && AiItems.countInInventory(AiItems.resolve("bed")) == 0) {
                  AiProgression.lastShelterDay = day;
                  return null;
               }

               AiProgression.lastShelterDay = day;
               if (this.stepOp != null) {
                  this.stepOp.cancel();
                  this.stepOp = null;
               }

               boolean bed = AiItems.countInInventory(AiItems.resolve("bed")) > 0;
               this.nightStepActive = true;
               AutismClientAddon.LOG.info("[AI] ladder for {}: night falls, {}", itemId, bed ? "sleeping" : "burrowing");
               this.stepOp = bed
                  ? AiSleep.init(AiJson.objectOf("timeoutSeconds", 240))
                  : AiBurrow.init(AiJson.objectOf("timeoutSeconds", 600));
               this.stepKey = "night";
               this.stepLabel = bed ? "sleep through the night" : "burrow until dawn";
               return this.running("Night falls; " + this.stepLabel);
            } else {
               return null;
            }
         }

         AiOperation.Result guardStep() {
            AiGuard.suppressBriefly();
            if (this.guardOp != null) {
               AiOperation.Result r = this.guardOp.tick();
               if (r.status() == AiOperation.Status.UNCERTAIN) {
                  return this.finish(AiOperation.Status.UNCERTAIN, r.message());
               }

               if (!r.terminal()) {
                  return this.running("Interrupted: " + this.guardReason + " - " + r.message());
               }

               this.guardOp.cancel();
               this.guardOp = null;
               if (r.status() != AiOperation.Status.SUCCEEDED) {
                  AiGuard.noteBurrowInterrupted();
               }

               if (AiGuard.perchCombatActive && r.status() != AiOperation.Status.SUCCEEDED) {
                  AiGuard.notePerchBackoff();
               } else if (this.guardFighting && r.status() != AiOperation.Status.SUCCEEDED && !AiGuard.burrowActive && !AiGuard.coverActive) {
                  AiGuard.noteCombatBackoff();
               }

               if (this.guardFleeing && (r.status() != AiOperation.Status.SUCCEEDED || AiGuard.sampleStatus().hostilesNear() > 0)) {
                  AiGuard.noteRetreatBlocked();
               }

               AutismClientAddon.LOG.info("[AI] ladder for {}: guard {} after {}: {}", itemId, r.status(), this.guardReason, AiProgression.clip(r.message()));
               return this.running("Back to the ladder after " + this.guardReason);
            } else if (autismclient.ai.engine.AiMotionSafety.isPlayerSupported() && (this.stepOp == null || this.stepOp.canInterruptNow())) {
               AiGuard.StatusSnapshot vitals = AiGuard.sampleStatus();
               AiOperation action = AiGuard.createResponseOperation(this.guardPolicy, vitals, this.secondsLeft());
               if (action == null) {
                  return null;
               }

               this.guardFleeing = AiGuard.fleeActive;
               this.guardFighting = !this.guardFleeing && AiGuard.decide(this.guardPolicy, vitals).action() == AiGuard.GuardAction.FIGHT;
               this.guardReason = AiGuard.decisionReason(this.guardPolicy, vitals);
               AutismClientAddon.LOG.info("[AI] ladder for {}: guard steps in ({}, health {})", itemId, this.guardReason, (int)vitals.health());
               if (this.stepOp != null) {
                  this.stepOp.cancel();
                  this.stepOp = null;
               }

               this.guardOp = action;
               return this.running("Interrupted: " + this.guardReason);
            } else {
               return null;
            }
         }

         int secondsLeft() {
            return (int)Math.max(10L, (this.deadline - System.nanoTime()) / 1000000000L);
         }

         JsonObject init_JsonObject_45() {
            JsonObject data = AiJson.objectOf(
               "item",
               itemId,
               "goal",
               goal,
               "held",
               AiCraft.countItem(itemId),
               "pickaxe",
               AiProgression.bestTier(mc).name(),
               "steps",
               this.stepLog,
               "furnaceStepEvidence",
               this.furnaceEvidence,
               "lastFailure",
               this.lastFailure
            );
            if (this.completionTableTried) {
               data.addProperty("completionTableAttempted", true);
               data.addProperty("carriedTable", AiCraft.countItem("minecraft:crafting_table") > 0);
               if (AiCraft.countItem("minecraft:crafting_table") == 0) {
                  data.add("missing", AiJson.objectOf("minecraft:crafting_table", 1));
               }
            }

            return data;
         }

         AiOperation.Result running(String message) {
            return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.init_JsonObject_45());
         }

         AiOperation.Result finish(AiOperation.Status status, String message) {
            return this.finishWithFailure(status, message, null);
         }

         AiOperation.Result finishWithFailure(AiOperation.Status status, String message, AiFailure failure) {
            AiVisualization.clearOwner(this);
            autismclient.ai.engine.AiRestock.reserveWoodForJob(0);
            autismclient.ai.engine.AiRestock.reserveStoneForJob(0);
            autismclient.ai.engine.AiRestock.reserveStrikesForJob(0);
            AiOperation.Result result = AiGameOperations.result(status, message, this.init_JsonObject_45());
            if (failure != null) {
               result = new AiOperation.Result(result.status(), result.message(), result.dataJson(), result.evidence(), failure);
            }

            result = this.receipt.finalizeResult(result, this.stepKey.equals("pack-table-completion") && this.receipt.requiresReconciliation(this.stepOp));
            if (this.stepOp != null) {
               if (this.stepKey.equals("pack-table-completion")) {
                  this.receipt.cancelChild(this.stepOp);
               } else {
                  this.stepOp.cancel();
               }

               this.stepOp = null;
            }

            if (this.guardOp != null) {
               this.guardOp.cancel();
               this.guardOp = null;
            }

            return result;
         }

         @Override
         public boolean canInterruptNow() {
            return !this.receipt.requiresReconciliation(null)
               && (this.stepOp == null || this.stepOp.canInterruptNow())
               && (this.guardOp == null || this.guardOp.canInterruptNow());
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.receipt.requiresReconciliation(null)
               || this.stepOp != null && this.stepOp.requiresReconciliationAfterEmergency()
               || this.guardOp != null && this.guardOp.requiresReconciliationAfterEmergency();
         }

         boolean obsidianNeedsBucket() {
            return itemId.equals("minecraft:obsidian") && AiCraft.countItem("minecraft:bucket") == 0 && AiCraft.countItem("minecraft:water_bucket") == 0;
         }

         @Override
         public void cancel() {
            this.cancelled = true;
            AiVisualization.clearOwner(this);
            autismclient.ai.engine.AiRestock.reserveWoodForJob(0);
            autismclient.ai.engine.AiRestock.reserveStoneForJob(0);
            autismclient.ai.engine.AiRestock.reserveStrikesForJob(0);
            if (this.stepOp != null) {
               if (this.stepKey.equals("pack-table-completion")) {
                  this.receipt.cancelChild(this.stepOp);
               } else {
                  this.stepOp.cancel();
               }
            }

            if (this.guardOp != null) {
               this.guardOp.cancel();
            }
         }
      };
   }

   static BlockPos bestTableToReclaim(BlockPos feet, List<BlockPos> tables, List<BlockPos> logs, int wood, int seconds) {
      return bestTableToReclaim(feet, tables, logs, wood, seconds, false);
   }

   static BlockPos bestTableToReclaim(BlockPos feet, List<BlockPos> tables, List<BlockPos> logs, int wood, int seconds, boolean carryRequired) {
      double woodSeconds = !carryRequired && wood >= 4 ? 1.0 : 1.0 / 0.0;

      for (BlockPos log : logs) {
         woodSeconds = Math.min(woodSeconds, 3.0 + travelSeconds(log.getX() - feet.getX(), log.getY() - feet.getY(), log.getZ() - feet.getZ()));
      }

      BlockPos best = null;
      double bestSeconds = 1.0 / 0.0;

      for (BlockPos table : tables) {
         double distance = table.distSqr(feet);
         if (!(distance > 4096.0) && !table.equals(feet.below())) {
            double cost = 3.0 + travelSeconds(table.getX() - feet.getX(), table.getY() - feet.getY(), table.getZ() - feet.getZ());
            if (!(cost + 5.0 > seconds)
               && (carryRequired || !(distance > 36.0) || !(cost >= woodSeconds))
               && (cost < bestSeconds || cost == bestSeconds && (best == null || table.asLong() < best.asLong()))) {
               best = table;
               bestSeconds = cost;
            }
         }
      }

      return best;
   }

   static AiOperation init(int seconds) {
      return init(seconds, false);
   }

   static AiOperation init(int seconds, boolean carryRequired) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null && AiCraft.countItem("minecraft:crafting_table") <= 0 && autismclient.ai.engine.AiMotionSafety.isPlayerSupported()) {
         ItemEntity observed = null;
         double dropCost = 1.0 / 0.0;

         for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof ItemEntity drop && drop.isAlive() && drop.getItem().is(Items.CRAFTING_TABLE)) {
               double cost = dropPickupSeconds(mc.player.position(), drop.position(), seconds);
               if (cost < dropCost) {
                  observed = drop;
                  dropCost = cost;
               }
            }
         }

         if (observed != null && dropCost <= 3.5) {
            return AiGameplay.write(AiJson.objectOf("item", "minecraft:crafting_table", "count", 1, "timeoutSeconds", Math.min(90, seconds)));
         } else {
            BlockPos feet = mc.player.blockPosition();
            List<BlockPos> tables = AiWorldScan.findBlocks(s -> s.is(Blocks.CRAFTING_TABLE), 4, 4, 32)
               .stream()
               .filter(pos -> mc.level.hasChunkAt(pos) && mc.level.getBlockState(pos).is(Blocks.CRAFTING_TABLE))
               .toList();
            List<BlockPos> logs = !carryRequired && plankEquivalents() < 4 ? AiWorldScan.findBlocks(s -> s.is(BlockTags.LOGS), 4, 4, 32) : List.of();
            BlockPos best = bestTableToReclaim(feet, tables, logs, plankEquivalents(), seconds, carryRequired);
            if (observed == null
               || best != null && !(dropCost < 3.0 + travelSeconds(best.getX() - feet.getX(), best.getY() - feet.getY(), best.getZ() - feet.getZ()))) {
               return best == null ? null : reclaimStation(best, "minecraft:crafting_table", Math.min(90, seconds));
            } else {
               return AiGameplay.write(AiJson.objectOf("item", "minecraft:crafting_table", "count", 1, "timeoutSeconds", Math.min(90, seconds)));
            }
         }
      } else {
         return null;
      }
   }

   static double dropPickupSeconds(Vec3 body, Vec3 drop, int seconds) {
      if (body.distanceToSqr(drop) > 4096.0) {
         return 1.0 / 0.0;
      }

      double cost = 1.0 + travelSeconds(drop.x - body.x, drop.y - body.y, drop.z - body.z);
      return cost + 5.0 <= seconds ? cost : 1.0 / 0.0;
   }

   static boolean needsReconcile(boolean broken, boolean carried, boolean liveDrop, boolean childPending) {
      return childPending || broken && !carried && !liveDrop;
   }

   private static AiOperation reclaimStation(final BlockPos pos, final String item, final int seconds) {
      final Minecraft mc = Minecraft.getInstance();
      Object stationWorld = mc.level;
      Object stationPlayer = mc.player;
      final long stationDeadline = System.nanoTime() + Math.max(1, seconds) * 1000000000L;
      AiOperation recovery = new AiOperation() {
         final Object startLevel = mc.level;
         final Object startPlayer = mc.player;
         final long deadline = stationDeadline;
         final int heldAtStart = AiCraft.countItem(item);
         final AiPreparationReceipt receipt = new AiPreparationReceipt();
         AiOperation carryCheck = AiGameplay.init(List.of(item), Math.max(1, seconds - 5));
         AiOperation breakOp;
         AiOperation pickupOp;
         int dropWaitTicks;
         boolean broken;
         boolean finished;
         boolean cancelled;

         @Override
         public AiOperation.Result tick() {
            if (this.cancelled) {
               return this.finishReclaim(AiGameOperations.result(AiOperation.Status.CANCELLED, "Station recovery cancelled", new JsonObject()));
            }

            if (mc.player == this.startPlayer && mc.level == this.startLevel && System.nanoTime() <= this.deadline) {
               if (this.pickupOp == null) {
                  if (!this.broken) {
                     if (this.carryCheck != null) {
                        AiOperation.Result ready = this.receipt.tickChild(this.carryCheck);
                        if (!ready.terminal()) {
                           return ready;
                        }

                        this.receipt.cancelChild(this.carryCheck);
                        this.carryCheck = null;
                        if (ready.status() != AiOperation.Status.SUCCEEDED) {
                           return this.finishReclaim(ready);
                        }

                        if (AiCraft.countItem(item) > this.heldAtStart) {
                           return this.finishReclaim(
                              AiGameOperations.result(
                                 AiOperation.Status.SUCCEEDED,
                                 "Observed the carried " + AiProgression.displayName(item),
                                 AiJson.objectOf("item", item, "held", AiCraft.countItem(item))
                              )
                           );
                        }
                     }

                     if (this.breakOp == null) {
                        this.breakOp = autismclient.ai.engine.AiBreakBlock.breakBlock(
                           pos, (int)Math.max(1L, (this.deadline - System.nanoTime()) / 1000000000L - 5L), false, Set.of(), true
                        );
                     }

                     AiOperation.Result r = this.receipt.tickChild(this.breakOp);
                     if (!r.terminal()) {
                        return r;
                     }

                     if (r.status() != AiOperation.Status.SUCCEEDED) {
                        return this.finishReclaim(r);
                     }

                     this.broken = true;
                  }

                  if (AiCraft.countItem(item) > this.heldAtStart) {
                     return this.finishReclaim(
                        AiGameOperations.result(
                           AiOperation.Status.SUCCEEDED,
                           "Picked the " + AiProgression.displayName(item) + " back up",
                           AiJson.objectOf("item", item, "held", AiCraft.countItem(item))
                        )
                     );
                  }

                  boolean drop = false;

                  for (ItemEntity entity : mc.level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(4.0))) {
                     if (BuiltInRegistries.ITEM.getKey(entity.getItem().getItem()).toString().equals(item)) {
                        drop = true;
                        break;
                     }
                  }

                  if (++this.dropWaitTicks < 10 && !drop) {
                     return AiGameOperations.result(AiOperation.Status.RUNNING, "Waiting for the " + AiProgression.displayName(item) + " to drop", new JsonObject());
                  }

                  int remaining = (int)Math.max(1L, (this.deadline - System.nanoTime()) / 1000000000L);
                  this.pickupOp = AiGameplay.write(AiJson.objectOf("item", item, "count", this.heldAtStart + 1, "timeoutSeconds", Math.min(15, remaining)));
               }

               AiOperation.Result result = this.receipt.tickChild(this.pickupOp);
               return result.terminal() ? this.finishReclaim(result) : result;
            } else {
               return this.finishReclaim(AiGameOperations.result(AiOperation.Status.TIMED_OUT, "Station recovery stopped before verified pickup", new JsonObject()));
            }
         }

         AiOperation.Result finishReclaim(AiOperation.Result result) {
            ItemEntity live = this.liveDrop();
            JsonObject data = AiRunResources.parseEvidenceJson(result.dataJson());
            data.addProperty("stationBroken", this.broken);
            data.addProperty("observedPickupIncomplete", this.broken && AiCraft.countItem(item) <= this.heldAtStart);
            data.add(
               "liveDrop",
               live == null
                  ? JsonNull.INSTANCE
                  : AiJson.objectOf("item", item, "count", live.getItem().getCount(), "x", live.getX(), "y", live.getY(), "z", live.getZ())
            );
            result = new AiOperation.Result(result.status(), result.message(), data.toString(), result.evidence(), result.failure());
            result = this.receipt.finalizeResult(result, this.requiresReconciliationAfterEmergency());
            this.finished = true;
            return result;
         }

         ItemEntity liveDrop() {
            if (mc.level != this.startLevel) {
               return null;
            }

            for (ItemEntity drop : mc.level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(4.0))) {
               if (drop.isAlive() && !drop.getItem().isEmpty() && BuiltInRegistries.ITEM.getKey(drop.getItem().getItem()).toString().equals(item)) {
                  return drop;
               }
            }

            return null;
         }

         AiOperation activeChild() {
            return this.carryCheck != null ? this.carryCheck : (this.pickupOp == null ? this.breakOp : this.pickupOp);
         }

         @Override
         public boolean canInterruptNow() {
            return !this.receipt.requiresReconciliation(this.activeChild())
               && (this.finished || !this.broken && (this.activeChild() == null || this.activeChild().canInterruptNow()));
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return AiProgression.needsReconcile(
               !this.finished && this.broken, AiCraft.countItem(item) > this.heldAtStart, this.liveDrop() != null, this.receipt.requiresReconciliation(this.activeChild())
            );
         }

         @Override
         public void cancel() {
            this.receipt.markReconciliationPending(this.requiresReconciliationAfterEmergency());
            this.cancelled = true;
            this.receipt.cancelChild(this.carryCheck);
            this.receipt.cancelChild(this.breakOp);
            this.receipt.cancelChild(this.pickupOp);
         }
      };
      return AiPreparationReceipt.awaitSupportedHandoff(recovery, autismclient.ai.engine.AiMotionSafety::isPlayerSupported, () -> mc.player == stationPlayer && mc.level == stationWorld, stationDeadline);
   }

   static double travelSeconds(double dx, double dy, double dz) {
      return 0.5 + Math.max(Math.hypot(dx, dz) * 0.25, Math.max(0.0, dy) * 0.45) + Math.max(0.0, -dy) * 0.2;
   }

   static BlockPos parseBlockPos(String text) {
      String[] parts = text.split(",");
      if (parts.length != 3) {
         return null;
      }

      try {
         return new BlockPos(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()), Integer.parseInt(parts[2].trim()));
      } catch (NumberFormatException e) {
         return null;
      }
   }

   static Set<String> keepIds(String itemId, Collection<String> more) {
      Set<String> ids = baseKeepIds(itemId);
      ids.addAll(more);
      return ids;
   }

   static Set<String> baseKeepIds(String itemId) {
      Set<String> ids = new LinkedHashSet<>(
         List.of(
            itemId,
            "minecraft:crafting_table",
            "minecraft:furnace",
            "planks",
            "log",
            "minecraft:stick",
            "minecraft:coal",
            "minecraft:netherite_ingot",
            "minecraft:flint_and_steel",
            "minecraft:bucket",
            "minecraft:water_bucket",
            "minecraft:lava_bucket"
         )
      );
      ids.addAll(KEEP_ITEMS);
      return ids;
   }

   static String displayName(String itemId) {
      return itemId.replace("minecraft:", "").replace('_', ' ');
   }

   static String clip(String text) {
      if (text == null) {
         return "";
      } else {
         return text.length() > 200 ? text.substring(0, 200) : text;
      }
   }

   static {
      Set<String> base = new LinkedHashSet<>(ORE_SOURCES.keySet());
      base.addAll(SMELT_INPUTS.keySet());
      KEEP_ITEMS = Set.copyOf(base);
   }

   enum Inner_CachePool {
      NONE,
      WOOD,
      STONE,
      IRON,
      DIAMOND,
      NETHERITE;
   }

   record Inner_DataUtil(int planks, int sticks) {
   }

   record Inner_StreamUtil(String family, String drop, int bestY, AiProgression.Inner_CachePool pick, int yieldPerOre, boolean nether) {
      Inner_StreamUtil(String family, String drop, int bestY, AiProgression.Inner_CachePool pick, int yield) {
         this(family, drop, bestY, pick, yield, false);
      }
   }
}
