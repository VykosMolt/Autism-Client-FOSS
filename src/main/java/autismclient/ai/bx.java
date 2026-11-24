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

final class bx {
   static final Map<String, bx.Inner_StreamUtil> init_Map = Map.of(
      "minecraft:raw_iron",
      new bx.Inner_StreamUtil("iron_ore", "minecraft:raw_iron", 16, bx.Inner_CachePool.read, 1),
      "minecraft:diamond",
      new bx.Inner_StreamUtil("diamond_ore", "minecraft:diamond", -58, bx.Inner_CachePool.write, 1),
      "minecraft:coal",
      new bx.Inner_StreamUtil("coal_ore", "minecraft:coal", 48, bx.Inner_CachePool.load, 1),
      "minecraft:raw_gold",
      new bx.Inner_StreamUtil("gold_ore", "minecraft:raw_gold", -16, bx.Inner_CachePool.write, 1),
      "minecraft:redstone",
      new bx.Inner_StreamUtil("redstone_ore", "minecraft:redstone", -58, bx.Inner_CachePool.write, 4),
      "minecraft:lapis_lazuli",
      new bx.Inner_StreamUtil("lapis_ore", "minecraft:lapis_lazuli", 0, bx.Inner_CachePool.read, 4),
      "minecraft:raw_copper",
      new bx.Inner_StreamUtil("copper_ore", "minecraft:raw_copper", 48, bx.Inner_CachePool.read, 2),
      "minecraft:ancient_debris",
      new bx.Inner_StreamUtil("ancient_debris", "minecraft:ancient_debris", 15, bx.Inner_CachePool.update, 1, true),
      "minecraft:obsidian",
      new bx.Inner_StreamUtil("obsidian", "minecraft:obsidian", -54, bx.Inner_CachePool.update, 1)
   );
   static final Map<String, String> load_Map = Map.of(
      "minecraft:iron_ingot",
      "minecraft:raw_iron",
      "minecraft:gold_ingot",
      "minecraft:raw_gold",
      "minecraft:copper_ingot",
      "minecraft:raw_copper",
      "minecraft:netherite_scrap",
      "minecraft:ancient_debris"
   );
   static final Set<String> init_Set;
   private static volatile long read_J = -1L;
   private static volatile long write_J;
   private static volatile long update_J = -1L;
   private static volatile int update_I;
   static final List<String> init_List = List.of(
      "minecraft:wooden_pickaxe", "minecraft:stone_pickaxe", "minecraft:iron_pickaxe", "minecraft:diamond_pickaxe", "minecraft:netherite_pickaxe"
   );
   static final int init_I = 14;
   static final int load_I = 9;
   static final int read_I = 23;
   static final List<String> load_List = List.of("minecraft:cobblestone", "minecraft:cobbled_deepslate", "minecraft:blackstone");
   static final List<String> read_List = List.of(
      "minecraft:stone", "minecraft:cobblestone", "minecraft:deepslate", "minecraft:cobbled_deepslate", "minecraft:blackstone"
   );
   static final int write_I = 20;
   static final long init_J = 10000000000L;
   static final long load_J = 3000000000L;

   private bx() {
   }

   static boolean init_Z(String material) {
      bx.Inner_StreamUtil ore = init_Map.get(material);
      if (ore == null && load_Map.containsKey(material)) {
         ore = init_Map.get(load_Map.get(material));
      }

      return ore != null && ore.nether();
   }

   static List<String> init(Collection<String> materials) {
      List<String> out = new ArrayList<>();

      for (String m : materials) {
         if (!init_Z(m)) {
            out.add(m);
         }
      }

      for (String m : materials) {
         if (init_Z(m)) {
            out.add(m);
         }
      }

      return out;
   }

   static boolean load_Z(String itemId) {
      return itemId.startsWith("minecraft:netherite_")
         && !itemId.equals("minecraft:netherite_ingot")
         && !itemId.equals("minecraft:netherite_scrap")
         && !itemId.equals("minecraft:netherite_block")
         && !itemId.equals("minecraft:netherite_upgrade_smithing_template");
   }

   static bx.Inner_CachePool init_bx_CachePool(String material) {
      bx.Inner_StreamUtil ore = init_Map.get(material);
      if (ore == null && load_Map.containsKey(material)) {
         ore = init_Map.get(load_Map.get(material));
      }

      return ore == null ? bx.Inner_CachePool.init : ore.pick();
   }

   static boolean init(String target, String oreDrop) {
      return !init_Set.contains(target) || !target.equals(oreDrop);
   }

   static String init(bx.Inner_CachePool tier) {
      String var10000;
      switch (tier) {
         case init:
         case load:
            var10000 = "minecraft:wooden_pickaxe";
            break;
         case read:
            var10000 = "minecraft:stone_pickaxe";
            break;
         case write:
            var10000 = "minecraft:iron_pickaxe";
            break;
         case update:
            var10000 = "minecraft:diamond_pickaxe";
            break;
         case sync:
            var10000 = "minecraft:netherite_pickaxe";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   static bx.Inner_CachePool load_bx_CachePool(String itemId) {
      bx.Inner_CachePool var10000;
      switch (itemId) {
         case "minecraft:wooden_pickaxe":
         case "minecraft:golden_pickaxe":
            var10000 = bx.Inner_CachePool.load;
            break;
         case "minecraft:stone_pickaxe":
            var10000 = bx.Inner_CachePool.read;
            break;
         case "minecraft:iron_pickaxe":
            var10000 = bx.Inner_CachePool.write;
            break;
         case "minecraft:diamond_pickaxe":
            var10000 = bx.Inner_CachePool.update;
            break;
         case "minecraft:netherite_pickaxe":
            var10000 = bx.Inner_CachePool.sync;
            break;
         default:
            var10000 = bx.Inner_CachePool.init;
      }

      return var10000;
   }

   static String init(bx.Inner_CachePool have, bx.Inner_CachePool needed) {
      return have.ordinal() >= needed.ordinal() ? null : init(bx.Inner_CachePool.values()[Math.max(bx.Inner_CachePool.load.ordinal(), have.ordinal() + 1)]);
   }

   static int init(bx.Inner_StreamUtil ore, int drops) {
      return Math.max(1, (drops + ore.kw_yield_I() - 1) / ore.kw_yield_I());
   }

   static boolean init_Z(String itemId, int goal) {
      if (init_List.contains(itemId) && load_bx_CachePool(itemId).ordinal() >= bx.Inner_CachePool.write.ordinal()) {
         return true;
      }

      if (load_Z(itemId)) {
         return true;
      }

      try {
         for (String material : init_Map(itemId, goal).keySet()) {
            if (init_bx_CachePool(material).ordinal() >= bx.Inner_CachePool.read.ordinal()) {
               return true;
            }
         }

         return false;
      } catch (RuntimeException e) {
         return false;
      }
   }

   static Map<String, Integer> init(Map<String, Integer> goals) {
      return init_Map(e.init_g_StreamUtil(), goals);
   }

   static Map<String, Integer> init_Map(final g.Inner_StreamUtil live, Map<String, Integer> goals) {
      g.Inner_StreamUtil raw = new g.Inner_StreamUtil() {
         @Override
         public List<g.Inner_BufferHelper> init_List(String id) {
            return bx.init_Set.contains(id) ? List.of() : live.init_List(id);
         }

         @Override
         public int init_I(String id) {
            return bx.init_Set.contains(id) ? 0 : live.init_I(id);
         }

         @Override
         public int load_I(String id) {
            return bx.init_Set.contains(id) ? 0 : live.load_I(id);
         }

         @Override
         public String init(Set<String> variants) {
            return live.init(variants);
         }
      };
      Map<String, Integer> total = new LinkedHashMap<>();
      goals.forEach((id, n) -> {
         if (init_Set.contains(id)) {
            total.merge(id, n, Integer::sum);
         } else {
            g.init(raw, id, n).missing().forEach((m, k) -> {
               if (init_Set.contains(m)) {
                  total.merge(m, k, Integer::sum);
               }
            });
         }
      });
      Map<String, Integer> out = new LinkedHashMap<>();
      total.forEach((m, n) -> {
         int left = n - live.init_I(m);
         if (left > 0) {
            out.put(m, left);
         }
      });
      return out;
   }

   static bx.Inner_DataUtil init(g.Inner_StreamUtil live, Map<String, Integer> kit, String main, int target) {
      Map<String, Integer> goals = new LinkedHashMap<>(kit);
      if (!init_List.contains(main) && !init_Set.contains(main)) {
         goals.merge(main, target, Math::max);
      }

      return init_bx_DataUtil(live, goals);
   }

   static bx.Inner_DataUtil init_bx_DataUtil(final g.Inner_StreamUtil live, Map<String, Integer> goals) {
      g.Inner_StreamUtil raw = new g.Inner_StreamUtil() {
         boolean load_Z(String id) {
            return bx.init_Set.contains(id) || bx.read(id) || id.equals("minecraft:stick");
         }

         @Override
         public List<g.Inner_BufferHelper> init_List(String id) {
            return this.load_Z(id) ? List.of() : live.init_List(id);
         }

         @Override
         public int init_I(String id) {
            return this.load_Z(id) ? 0 : live.init_I(id);
         }

         @Override
         public int load_I(String id) {
            return this.load_Z(id) ? 0 : live.load_I(id);
         }

         @Override
         public String init(Set<String> variants) {
            return live.init(variants);
         }
      };
      int planks = 0;
      int sticks = 0;

      for (Entry<String, Integer> goal : goals.entrySet()) {
         if (live.init_I(goal.getKey()) < goal.getValue()) {
            g.Inner_CachePool plan = g.init(raw, goal.getKey(), goal.getValue());
            if (plan.missing().containsKey(goal.getKey())) {
               return null;
            }

            for (Entry<String, Integer> missing : plan.missing().entrySet()) {
               if (read(missing.getKey())) {
                  planks += missing.getValue();
               } else if (missing.getKey().equals("minecraft:stick")) {
                  sticks += missing.getValue();
               }
            }
         }
      }

      return new bx.Inner_DataUtil(planks, sticks);
   }

   private static boolean read(String id) {
      return id.equals("planks") || id.equals("minecraft:planks") || id.endsWith("_planks");
   }

   static int init_I_I(int missing, int held, double beforeTicks, double afterTicks, double setupTicks, boolean prerequisitesAffordable) {
      int firstPick = Math.max(0, 3 - held);
      return prerequisitesAffordable && ar.init(missing, beforeTicks, afterTicks, setupTicks, firstPick) ? firstPick : -1;
   }

   static Map<String, Integer> init_Map(String itemId, int goal) {
      if (init_Set.contains(itemId)) {
         int held = e.init_I(itemId);
         return held >= goal ? Map.of() : Map.of(itemId, goal - held);
      } else {
         final g.Inner_StreamUtil live = e.init_g_StreamUtil();
         g.Inner_StreamUtil leaves = new g.Inner_StreamUtil() {
            @Override
            public List<g.Inner_BufferHelper> init_List(String id) {
               return bx.init_Set.contains(id) ? List.of() : live.init_List(id);
            }

            @Override
            public int init_I(String id) {
               return live.init_I(id);
            }

            @Override
            public int load_I(String id) {
               return bx.init_Set.contains(id) ? 0 : live.load_I(id);
            }

            @Override
            public String init(Set<String> variants) {
               return live.init(variants);
            }
         };
         Map<String, Integer> out = new LinkedHashMap<>();
         g.init(leaves, itemId, goal).missing().forEach((id, n) -> {
            if (init_Set.contains(id)) {
               out.merge(id, n, Integer::sum);
            }
         });
         return out;
      }
   }

   static bx.Inner_CachePool init_bx_CachePool(Minecraft mc) {
      bx.Inner_CachePool best = bx.Inner_CachePool.init;
      Inventory inventory = mc.player.getInventory();

      for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
         ItemStack stack = inventory.getItem(slot);
         if (!stack.isEmpty()) {
            bx.Inner_CachePool tier = load_bx_CachePool(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            if (tier != bx.Inner_CachePool.init
               && (!stack.isDamageableItem() || stack.getMaxDamage() - stack.getDamageValue() > 3)
               && tier.ordinal() > best.ordinal()) {
               best = tier;
            }
         }
      }

      return best;
   }

   static boolean init_Z(Minecraft mc) {
      Inventory inventory = mc.player.getInventory();

      for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
         String id = BuiltInRegistries.ITEM.getKey(inventory.getItem(slot).getItem()).toString();
         if (id.endsWith("_sword") || id.endsWith("_axe")) {
            return true;
         }
      }

      return false;
   }

   static int init(Minecraft mc, Item pick) {
      int best = 0;

      for (int slot = 0; slot < 36; slot++) {
         ItemStack stack = mc.player.getInventory().getItem(slot);
         if (stack.is(pick)) {
            best = Math.max(best, stack.getMaxDamage() - stack.getDamageValue());
         }
      }

      return best;
   }

   static int init_I(Minecraft mc) {
      int free = 0;
      Inventory inventory = mc.player.getInventory();

      for (int slot = 0; slot < 36; slot++) {
         if (inventory.getItem(slot).isEmpty()) {
            free++;
         }
      }

      return free;
   }

   static int init(bx.Inner_CachePool have, bx.Inner_CachePool last, boolean tableHeld, boolean weapon, int sticksHeld, int smelts, boolean coal) {
      return init(have, last, tableHeld, weapon, sticksHeld, smelts, coal, false);
   }

   static int init(
      bx.Inner_CachePool have, bx.Inner_CachePool last, boolean tableHeld, boolean weapon, int sticksHeld, int smelts, boolean coal, boolean oneTable
   ) {
      return init(have, last, tableHeld, weapon, sticksHeld, smelts, coal, oneTable, new bx.Inner_DataUtil(0, 0));
   }

   static int init(
      bx.Inner_CachePool have,
      bx.Inner_CachePool last,
      boolean tableHeld,
      boolean weapon,
      int sticksHeld,
      int smelts,
      boolean coal,
      boolean oneTable,
      bx.Inner_DataUtil kit
   ) {
      int tables = tableHeld ? 0 : 1;
      if (!oneTable && have.ordinal() < bx.Inner_CachePool.load.ordinal() && last.ordinal() >= bx.Inner_CachePool.read.ordinal()) {
         tables++;
      }

      if (!oneTable && have.ordinal() < bx.Inner_CachePool.read.ordinal() && last.ordinal() >= bx.Inner_CachePool.write.ordinal()) {
         tables++;
      }

      if (!oneTable && have.ordinal() < bx.Inner_CachePool.write.ordinal() && last.ordinal() >= bx.Inner_CachePool.update.ordinal()) {
         tables++;
      }

      int planks = 4 * tables + kit.planks();
      if (have.ordinal() < bx.Inner_CachePool.load.ordinal() && last.ordinal() >= bx.Inner_CachePool.load.ordinal()) {
         planks += 3;
      }

      int sticks = kit.sticks() + (!weapon && last.ordinal() >= bx.Inner_CachePool.read.ordinal() ? 2 : 0);

      for (bx.Inner_CachePool t : new bx.Inner_CachePool[]{
         bx.Inner_CachePool.load, bx.Inner_CachePool.read, bx.Inner_CachePool.write, bx.Inner_CachePool.update
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

   static int init(bx.Inner_CachePool have, int remaining) {
      return have == bx.Inner_CachePool.load ? Math.min(3, remaining) : remaining;
   }

   static boolean init_Z_Z(boolean carryOneTable, int wood, int futureWood) {
      return carryOneTable || wood < 4 + futureWood;
   }

   static int init_I_Z(boolean campaign, boolean jointKit, String target, int heldTables, boolean attempted, int secondsLeft) {
      if (campaign && (jointKit || target.equals("minecraft:diamond_pickaxe")) && heldTables <= 0) {
         return !attempted && secondsLeft > 0 ? Math.min(90, secondsLeft) : -1;
      } else {
         return 0;
      }
   }

   static int init() {
      int total = 0;

      for (String id : load_List) {
         total += e.init_I(id);
      }

      return total;
   }

   static AiOperation init(int held, int timeoutSeconds) {
      return init_AiOperation_I(held, 0, timeoutSeconds);
   }

   static AiOperation init_AiOperation_I(int held, int heldMin, int timeoutSeconds) {
      return autismclient.ai.a.BufferHelper.init_AiOperation(
         ag.init_JsonObject_ObjArr(
            "block", "log", "held", held, "heldMin", heldMin, "nearRadius", 20, "trees", true, "radius", 64, "timeoutSeconds", Math.max(30, timeoutSeconds)
         )
      );
   }

   static int load() {
      return af.init_I(af.load_af_DataUtil("planks")) + 4 * af.init_I(af.load_af_DataUtil("log"));
   }

   static boolean load(Minecraft mc) {
      Inventory inventory = mc.player.getInventory();

      for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
         if (inventory.getItem(slot).is(ItemTags.AXES)) {
            return true;
         }
      }

      return false;
   }

   static AiOperation init_AiOperation_JsonObject(JsonObject args, final String itemId, final int goal) {
      final Minecraft mc = Minecraft.getInstance();
      final int timeoutSeconds = Math.max(60, Math.min(7200, ag.init_I_JsonObject(args, "timeoutSeconds", 3600)));
      final Map<String, Integer> kit = new LinkedHashMap<>();
      if (args.has("kit") && args.get("kit").isJsonObject()) {
         for (Entry<String, JsonElement> e : args.getAsJsonObject("kit").entrySet()) {
            kit.put(e.getKey(), Math.max(1, e.getValue().getAsInt()));
         }
      }

      final int kitPlanks = Math.max(0, ag.init_I_JsonObject(args, "kitPlanks", 0));
      return new AiOperation() {
         final String init_Str = "Getting " + af.load_af_DataUtil(itemId).items().iterator().next().getDefaultInstance().getHoverName().getString();
         final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
         Object init_Obj = mc.level;
         final Object load_Obj = mc.getConnection();
         final String load_Str = cL.init(mc);
         final UUID init_UUID = mc.player == null ? null : mc.player.getUUID();
         final long load_J = ca.load_J();
         String read_Str = cL.load(mc);
         final JsonArray init_JsonArray = new JsonArray();
         JsonObject init_JsonObject = new JsonObject();
         final Map<String, Integer> init_Map = new HashMap<>();
         final X.Inner_CachePool init_X_CachePool = X.Inner_CachePool.standard().keeping(bx.init(itemId, kit.keySet()));
         AiOperation init_AiOperation;
         AiOperation load_AiOperation;
         String write_Str = "";
         String update_Str = "";
         String sync_Str = "";
         String flush_Str = "";
         boolean init_Z = bK.load_Z();
         boolean load_Z;
         boolean read_Z;
         boolean write_Z;
         boolean update_Z;
         boolean sync_Z;
         final bb init_bb = new bb();
         int init_I;
         BlockPos init_BlockPos;
         BlockPos load_BlockPos;
         String close_Str = "";
         String open_Str = "";
         String poll_Str = "";
         String push_Str = "minecraft:furnace";
         int load_I;
         int read_I;
         int write_I;
         long read_J;
         final Map<String, BlockPos> load_Map = new HashMap<>();
         boolean flush_Z;
         boolean close_Z;
         BlockPos read_BlockPos;
         final Set<Long> init_Set = new LinkedHashSet<>();
         long write_J;
         long update_J;
         boolean open_Z;
         boolean poll_Z;
         int update_I;
         boolean push_Z;
         boolean pop;
         int sync_I;
         int flush_I;
         int close_I;

         boolean init_Z_23() {
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

                     double have = this.init(worn, slot);
                     double next = this.init(stack, slot);
                     if (next > have) {
                        return true;
                     }
                  }
               }
            }

            return false;
         }

         double init(ItemStack stack, EquipmentSlot slot) {
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

         bx.Inner_CachePool init_bx_CachePool() {
            bx.Inner_CachePool have = bx.init_bx_CachePool(mc);
            bx.Inner_CachePool worn = bx.Inner_CachePool.init;
            Inventory inventory = mc.player.getInventory();

            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
               ItemStack stack = inventory.getItem(slot);
               if (!stack.isEmpty()) {
                  bx.Inner_CachePool tier = bx.load_bx_CachePool(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
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
               case load:
                  var10000 = bx.init() >= 3;
                  break;
               case read:
                  var10000 = e.init_I("minecraft:iron_ingot") >= 3 || this.init_BlockPos != null;
                  break;
               case write:
                  var10000 = e.init_I("minecraft:diamond") >= 3;
                  break;
               default:
                  var10000 = false;
            }

            boolean nextOnItsWay = var10000;
            return nextOnItsWay ? worn : have;
         }

         int init_I_29() {
            bx.Inner_CachePool have = this.init_bx_CachePool();
            bx.Inner_CachePool last = this.load_bx_CachePool();
            int need = 0;
            if (have.ordinal() < bx.Inner_CachePool.read.ordinal() && last.ordinal() >= bx.Inner_CachePool.read.ordinal()) {
               need += 3;
            }

            if (!bx.init_Z(mc) && last.ordinal() >= bx.Inner_CachePool.read.ordinal()) {
               need += 3;
            }

            if (have.ordinal() < bx.Inner_CachePool.write.ordinal()
               && last.ordinal() >= bx.Inner_CachePool.write.ordinal()
               && e.init_I("minecraft:furnace") == 0
               && this.read_Z_9()) {
               need += 8;
            }

            return need;
         }

         Map<String, Integer> init_Map_4() {
            if (kit.isEmpty()) {
               return bx.init_Map(itemId, goal);
            }

            Map<String, Integer> goals = new LinkedHashMap<>();
            goals.put(itemId, goal);
            kit.forEach((id, n) -> {
               if (n - this.load_I(id) > 0) {
                  goals.merge(id, n - this.load_I(id), Integer::sum);
               }
            });
            return bx.init(goals);
         }

         Map<String, Integer> init(String pick, Map<String, Integer> pickNeeds) {
            if (kit.isEmpty()) {
               return pickNeeds;
            }

            Map<String, Integer> all = this.init_Map_4();
            Map<String, Integer> out = new LinkedHashMap<>();
            pickNeeds.forEach((m, n) -> out.put(m, pick.equals(itemId) ? Math.max(n, all.getOrDefault(m, 0)) : n + all.getOrDefault(m, 0)));
            return out;
         }

         boolean load_Z_13() {
            for (Entry<String, Integer> e : kit.entrySet()) {
               if (this.init_I_Str_2(e.getKey()) < e.getValue()) {
                  return false;
               }
            }

            return true;
         }

         int init_I_Str_2(String id) {
            int n = e.init_I(id);
            if (id.equals("minecraft:bucket")) {
               n += e.init_I("minecraft:water_bucket") + e.init_I("minecraft:lava_bucket");
            }

            return n;
         }

         int load_I(String id) {
            return this.init_I_Str_2(id) - e.init_I(id);
         }

         boolean read_Z_9() {
            return e.init_I("minecraft:iron_ingot") < 3 && this.init_BlockPos == null;
         }

         @Override
         public AiOperation.Result tick() {
            AiOperation.Result result = this.init_AiOperation_Result_28();
            if (!result.terminal()) {
               cG.init(this, this.init_Str, cs.init(result.message(), "Preparing materials"), e.init_I(itemId), goal);
               W.init(this, 1, this.init_Str, W.read(this.write_Str), "");
            } else {
               cG.init_V(this);
               W.init(this);
            }

            return result;
         }

         AiOperation.Result init_AiOperation_Result_28() {
            if (this.push_Z
               || mc.player == null
               || mc.level == null
               || mc.getConnection() != this.load_Obj
               || !this.load_Str.equals(cL.init(mc))
               || !mc.player.getUUID().equals(this.init_UUID)) {
               return this.init(AiOperation.Status.CANCELLED, "Progression world/player changed");
            }

            if (mc.player.isAlive() && this.load_J == ca.load_J()) {
               if (mc.level != this.init_Obj) {
                  boolean expected = this.read_Str.equals("minecraft:overworld")
                     && cL.load(mc).equals("minecraft:the_nether")
                     && this.init_AiOperation != null
                     && (this.write_Str.equals("nether") || this.write_Str.equals("smith-ingot"));
                  if (!expected) {
                     return this.init(AiOperation.Status.UNCERTAIN, "Progression dimension changed outside its expected Nether action");
                  }

                  this.init_Obj = mc.level;
                  this.read_Str = cL.load(mc);
                  this.init_BlockPos = null;
                  this.load_BlockPos = null;
                  this.close_Str = "";
                  this.open_Str = "";
                  this.poll_Str = "";
                  this.read_J = 0L;
                  this.load_Map.clear();
               }

               autismclient.ai.a.UtilCore.init(this.sync_I);
               autismclient.ai.a.UtilCore.load(this.flush_I);
               boolean preservingIron = this.load_bx_CachePool().ordinal() >= bx.Inner_CachePool.update.ordinal()
                  && bx.init_bx_CachePool(mc) == bx.Inner_CachePool.write;
               autismclient.ai.a.UtilCore.read(preservingIron ? 48 : 0);
               if (System.nanoTime() > this.init_J) {
                  return this.init(
                     this.canInterruptNow() ? AiOperation.Status.TIMED_OUT : AiOperation.Status.UNCERTAIN, "Ran out of time at: " + this.update_Str
                  );
               }

               if (e.init_I(itemId) >= goal && this.load_Z_13() && this.init_AiOperation == null && this.load_AiOperation == null) {
                  int tableBudget = bx.init_I_Z(
                     bK.load_Z(),
                     !kit.isEmpty(),
                     itemId,
                     e.init_I("minecraft:crafting_table"),
                     this.sync_Z,
                     (int)Math.max(0L, (this.init_J - System.nanoTime()) / 1000000000L)
                  );
                  if (tableBudget < 0) {
                     return this.init(
                        AiOperation.Status.SUCCEEDED,
                        "Obtained " + goal + " " + itemId + (kit.isEmpty() ? "" : " and the kit " + kit) + "; the crafting table could not be recovered"
                     );
                  }

                  if (tableBudget > 0) {
                     if (!autismclient.ai.a.AiMotionSafety.fill_Z()) {
                        return this.load_AiOperation_Result_2("Settling before carrying the kit's crafting table onward");
                     }

                     this.sync_Z = true;
                     AiOperation carry = bx.init(tableBudget, true);
                     if (carry != null) {
                        return this.init("pack-table-completion", "carry the crafting table onward with the completed kit", carry);
                     }

                     AiOperation replacement = e.init(
                        ag.init_JsonObject_ObjArr(
                           "item",
                           "minecraft:crafting_table",
                           "count",
                           1,
                           "timeoutSeconds",
                           Math.max(15, Math.min(45, (int)Math.max(0L, (this.init_J - System.nanoTime()) / 1000000000L)))
                        )
                     );
                     if (replacement != null) {
                        return this.init("craft-table-completion", "make the crafting table to carry onward instead of fetching the old one", replacement);
                     }
                  }

                  return this.init(AiOperation.Status.SUCCEEDED, "Obtained " + goal + " " + itemId + (kit.isEmpty() ? "" : " and the kit " + kit));
               } else {
                  AiOperation.Result guarded = this.sync_AiOperation_Result_4();
                  if (guarded != null) {
                     return guarded;
                  }

                  AiOperation.Result night = this.update_AiOperation_Result_4();
                  if (night != null) {
                     return night;
                  }

                  if (this.init_AiOperation != null) {
                     if (preservingIron
                        && this.write_Str.startsWith("ore:")
                        && bx.init(mc, Items.IRON_PICKAXE) <= 48
                        && bx.init(mc, Items.STONE_PICKAXE) <= 8
                        && mc.player.onGround()
                        && mc.player.getDeltaMovement().horizontalDistanceSqr() <= 4.0E-4
                        && this.init_AiOperation.canInterruptNow()) {
                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                        return this.init(
                           "spare-pick",
                           "make a stone pick for the descent; keep iron for diamonds",
                           this.init_AiOperation("minecraft:stone_pickaxe", e.init_I("minecraft:stone_pickaxe") + 1)
                        );
                     }

                     boolean allowHurt = mc.player.getHealth() <= 6.0F && this.init_Z_D_2(24.0);
                     if (allowHurt) {
                        autismclient.ai.a.CharEncoder.init_V();
                     }

                     AiOperation.Result r;
                     try {
                        r = this.write_Str.equals("pack-table-completion")
                           ? this.init_bb.init_AiOperation_Result(this.init_AiOperation)
                           : this.init_AiOperation.tick();
                     } finally {
                        if (allowHurt) {
                           autismclient.ai.a.CharEncoder.load();
                        }
                     }

                     boolean furnaceStep = this.write_Str.startsWith("smelt") || this.write_Str.startsWith("collect:") || this.write_Str.equals("prework");
                     if (furnaceStep) {
                        this.init_JsonObject = bK.init_JsonObject(r.dataJson());
                     }

                     if (!r.terminal()) {
                        return this.load_AiOperation_Result_2(this.update_Str + ": " + r.message());
                     }

                     if (this.write_Str.equals("pack-table-completion")) {
                        this.init_bb.init_V(this.init_AiOperation);
                     } else {
                        this.init_AiOperation.cancel();
                     }

                     this.init_AiOperation = null;
                     JsonObject outcome = ag.init_JsonObject_ObjArr("step", this.update_Str, "status", r.status().name(), "message", bx.load_Str(r.message()));
                     if (furnaceStep) {
                        outcome.add("receipt", this.init_JsonObject.deepCopy());
                     }

                     this.init_JsonArray.add(outcome);
                     if (r.status() == AiOperation.Status.UNCERTAIN) {
                        return this.init(AiOperation.Status.UNCERTAIN, "Unconfirmed " + this.update_Str + ": " + r.message());
                     }

                     AutismClientAddon.LOG
                        .info(
                           "[AI] ladder step {}: {} - {}{}",
                           r.status(),
                           this.update_Str,
                           bx.load_Str(r.message()),
                           r.status() == AiOperation.Status.SUCCEEDED
                              ? ""
                              : " data " + (r.dataJson().length() > 2500 ? r.dataJson().substring(0, 2500) : r.dataJson())
                        );
                     if (this.pop) {
                        this.pop = false;
                        if (r.status() != AiOperation.Status.SUCCEEDED && ++bx.update_I < 3) {
                           bx.read_J = -1L;
                           bx.write_J = System.nanoTime() + 20000000000L;
                           return this.load_AiOperation_Result_2("No shelter yet (" + bx.load_Str(r.message()) + "); trying again shortly");
                        } else {
                           return this.load_AiOperation_Result_2("Morning; back to the ladder");
                        }
                     } else {
                        if (r.status() == AiOperation.Status.CANCELLED && this.push_Z) {
                           return this.init(AiOperation.Status.CANCELLED, "Cancelled during " + this.update_Str);
                        }

                        if (!this.write_Str.startsWith("ore:") || r.failure() != A.tick && r.failure() != A.get) {
                           if (this.write_Str.equals("prework")) {
                              if (r.status() != AiOperation.Status.SUCCEEDED && this.read_BlockPos != null) {
                                 this.init_Set.add(this.read_BlockPos.asLong());
                              }

                              this.flush_Z = true;
                              return this.init_AiOperation_Result_Str_11(
                                 this.flush_Z ? "Back to the furnace after: " + this.update_Str : "Continue the smelt work after: " + this.update_Str
                              );
                           } else {
                              if (r.status() == AiOperation.Status.SUCCEEDED) {
                                 this.init_V_AiOperation_Result_3(r);
                              } else if (this.write_Str.startsWith("collect:")
                                 && this.init_BlockPos != null
                                 && !mc.level.getBlockState(this.init_BlockPos).is(Blocks.FURNACE)) {
                                 AutismClientAddon.LOG
                                    .info("[AI] ladder for {}: the background furnace at {} is gone", itemId, this.init_BlockPos.toShortString());
                                 this.init_BlockPos = null;
                              }

                              if (r.status() != AiOperation.Status.SUCCEEDED) {
                                 if (this.write_Str.equals("ore:minecraft:obsidian") && e.init_I(itemId) < goal) {
                                    return this.init(r.status(), "Contained obsidian acquisition stopped: " + bx.load_Str(r.message()));
                                 }

                                 int n = this.init_Map.merge(this.write_Str, 1, Integer::sum);
                                 this.flush_Str = this.update_Str + ": " + bx.load_Str(r.message());
                                 if (n >= 3) {
                                    return this.init(
                                       AiOperation.Status.FAILED, "Could not " + this.update_Str + " after 3 tries (" + bx.load_Str(r.message()) + ")"
                                    );
                                 }
                              }

                              return this.init_AiOperation_Result_Str_11("Next step after: " + this.update_Str);
                           }
                        } else {
                           String drop = this.write_Str.substring("ore:".length());
                           bx.Inner_StreamUtil ore = bx.init_Map.get(drop);
                           JsonObject detail = ag.init_JsonObject_Str(r.dataJson());
                           if (ore != null && detail.has("targetX") && detail.has("targetY") && detail.has("targetZ")) {
                              this.load_Map
                                 .put(
                                    ore.family(),
                                    new BlockPos(detail.get("targetX").getAsInt(), detail.get("targetY").getAsInt(), detail.get("targetZ").getAsInt())
                                 );
                           }

                           return this.init_AiOperation_Result_Str_11("Replace the worn mining tool before continuing the same ore");
                        }
                     }
                  } else {
                     if (!mc.player.onGround() && !mc.player.isInWater() && !mc.player.onClimbable() && ++this.update_I < 60) {
                        return this.load_AiOperation_Result_2("Waiting to land");
                     }

                     this.update_I = 0;
                     AiOperation was = this.init_AiOperation;
                     AiOperation.Result chosen = this.load_AiOperation_Result_15();
                     if (this.init_AiOperation != null && this.init_AiOperation != was && !chosen.terminal() && this.close_I < 4) {
                        this.close_I++;

                        try {
                           return this.init_AiOperation_Result_28();
                        } finally {
                           this.close_I--;
                        }
                     } else {
                        return chosen;
                     }
                  }
               }
            } else {
               return this.init(AiOperation.Status.FAILED, "Player died during progression");
            }
         }

         AiOperation.Result init_AiOperation_Result_Str_11(String message) {
            if (this.close_I >= 4) {
               return this.load_AiOperation_Result_2(message);
            }

            this.close_I++;

            try {
               return this.init_AiOperation_Result_28();
            } finally {
               this.close_I--;
            }
         }

         AiOperation.Result load_AiOperation_Result_15() {
            if (bx.init_I(mc) < 3 && System.nanoTime() - this.write_J > 30000000000L) {
               this.write_J = System.nanoTime();
               return this.init(
                  "tidy", "make room in the bag", cx.init(ag.init_JsonObject_ObjArr("action", "drop", "freeSlots", 3, "keep", this.init_JsonArray_2()))
               );
            }

            j.Inner_DataUtil death = j.init_j_DataUtil();
            if (death != null
               && !j.init_Z()
               && death.remainingMillis() > 20000L
               && death.dimension().equals(mc.level.dimension().identifier().toString())
               && this.init_Map.getOrDefault("recover", 0) == 0) {
               return this.init("recover", "go back for the items dropped at " + death.position().toShortString(), bE.init(new JsonObject()));
            }

            bx.Inner_CachePool have = this.init_bx_CachePool();
            if (have.ordinal() >= bx.Inner_CachePool.read.ordinal()
               && !bx.init_Z(mc)
               && (bx.init() >= 3 || have.ordinal() > bx.Inner_CachePool.read.ordinal())
               && this.init_Map.getOrDefault("axe", 0) == 0) {
               return this.init("axe", "make a stone axe for mobs and crafting tables", this.init_AiOperation("minecraft:stone_axe", 1));
            }

            if (have.ordinal() >= bx.Inner_CachePool.write.ordinal() && kit.containsKey("minecraft:shield")) {
               if (e.init_I("minecraft:shield") == 0 && e.init_I("minecraft:iron_ingot") >= 1 && this.init_Map.getOrDefault("shield-early", 0) == 0) {
                  return this.init("shield-early", "make the kit's shield now", this.init_AiOperation("minecraft:shield", 1));
               }

               if (e.init_I("minecraft:shield") > 0
                  && !mc.player.getOffhandItem().is(Items.SHIELD)
                  && mc.player.getOffhandItem().isEmpty()
                  && this.init_Map.getOrDefault("shield-offhand", 0) == 0
                  && !this.read_Z) {
                  this.read_Z = true;
                  return this.init(
                     "shield-offhand", "the shield to the offhand", w.init(ag.init_JsonObject_ObjArr("what", "minecraft:shield", "hand", "offhand"))
                  );
               }
            }

            if (this.init_Z_23() && this.init_I < 3) {
               this.init_I++;
               return this.init("armour-now", "put the new armour on", w.init(ag.init_JsonObject_ObjArr("what", "armor")));
            }

            int waterWanted = Math.min(2, kit.getOrDefault("minecraft:bucket", 0));
            if (have.ordinal() >= bx.Inner_CachePool.write.ordinal()
               && waterWanted > 0
               && !this.write_Z
               && e.init_I("minecraft:water_bucket") < waterWanted
               && (e.init_I("minecraft:bucket") > 0 || e.init_I("minecraft:iron_ingot") >= 3)
               && !cu.init(mc, Blocks.WATER, 24, 12, false).stream().filter(p -> mc.level.getBlockState(p.above()).isAir()).toList().isEmpty()) {
               this.write_Z = true;
               return this.init("water-early", "fill the water buckets close by before going deep", aD.init(waterWanted, 90));
            }

            if (have.ordinal() >= bx.Inner_CachePool.read.ordinal()
               && have.ordinal() < bx.Inner_CachePool.write.ordinal()
               && this.load_bx_CachePool().ordinal() >= bx.Inner_CachePool.write.ordinal()
               && this.read_Z_9()
               && e.init_I("minecraft:furnace") == 0
               && bx.init() >= 8
               && this.init(Blocks.CRAFTING_TABLE, 5) != null
               && this.init_Map.getOrDefault("furnace", 0) == 0) {
               return this.init("furnace", "make the furnace while the table is out", this.init_AiOperation("minecraft:furnace", 1));
            }

            if (mc.player.getFoodData().getFoodLevel() <= 12 && cq.init(mc.player.getInventory()) < 0 && System.nanoTime() - this.update_J > 600000000000L) {
               this.update_J = System.nanoTime();
               AiOperation hunt = cl.init_AiOperation_D(48.0, 4, Math.min(120, this.load_I_12()));
               if (hunt != null) {
                  return this.init("food", "hunt for food", hunt);
               }
            }

            Map<String, Integer> needs = this.init_Map_4();
            bx.Inner_CachePool needed = bx.init_List.contains(itemId)
               ? bx.Inner_CachePool.values()[Math.max(bx.Inner_CachePool.init.ordinal(), bx.load_bx_CachePool(itemId).ordinal() - 1)]
               : bx.Inner_CachePool.init;

            for (String material : needs.keySet()) {
               if (bx.init_bx_CachePool(material).ordinal() > needed.ordinal()) {
                  needed = bx.init_bx_CachePool(material);
               }
            }

            String pick = bx.init(have, needed);
            int woodNeed = this.init(needed, e.init_I("minecraft:crafting_table") > 0);
            if (this.load_Z && !this.init_Z && have.ordinal() <= bx.Inner_CachePool.read.ordinal() && bx.load() < woodNeed) {
               this.init_Z = true;
               AutismClientAddon.LOG
                  .info(
                     "[AI] ladder for {}: trees are scarce here ({} planks' worth, {} for a table at every site): one crafting table for the whole ladder, picked back up at each site",
                     itemId,
                     bx.load(),
                     woodNeed
                  );
               woodNeed = this.init(needed, e.init_I("minecraft:crafting_table") > 0);
            }

            if (pick != null
               && have.ordinal() <= bx.Inner_CachePool.read.ordinal()
               && bx.load() < woodNeed
               && this.init_Map.getOrDefault("stock-wood", 0) == 0
               && !this.load_Z) {
               int logs = af.init_I(af.load_af_DataUtil("log")) + (woodNeed - bx.load() + 3) / 4;
               boolean priorLeanBudget = this.init_Z;
               this.init_Z = true;
               int lean = this.init(needed, e.init_I("minecraft:crafting_table") > 0);
               this.init_Z = priorLeanBudget;
               int leanLogs = af.init_I(af.load_af_DataUtil("log")) + Math.max(0, (lean - bx.load() + 3) / 4);
               this.load_Z = true;
               return this.init("stock-wood", "get wood for the tools", bx.init_AiOperation_I(logs, Math.min(logs, leanLogs), Math.min(240, this.load_I_12())));
            }

            int stoneBatch = bx.init(have, this.init_I_29());
            if (this.init_Z
               && pick != null
               && (have == bx.Inner_CachePool.load || have == bx.Inner_CachePool.read)
               && bx.init() < stoneBatch
               && e.init_I("minecraft:crafting_table") == 0) {
               BlockPos table = this.init(Blocks.CRAFTING_TABLE, 6);
               if (table != null) {
                  return this.init("reclaim-table", "pick up the crafting table", this.init(table, "minecraft:crafting_table"));
               }
            }

            String stoneKey = "stock-stone:" + have;
            if ((have == bx.Inner_CachePool.load || have == bx.Inner_CachePool.read) && bx.init() < stoneBatch && this.init_Map.getOrDefault(stoneKey, 0) == 0) {
               int seconds = Math.min(240, this.load_I_12());
               Supplier<AiOperation> gather = () -> autismclient.ai.a.BufferHelper.init_AiOperation(
                  ag.init_JsonObject_ObjArr(
                     "blocks",
                     autismclient.ai.a.BufferHelper.init(bx.read_List),
                     "drops",
                     autismclient.ai.a.BufferHelper.init(bx.load_List),
                     "held",
                     stoneBatch,
                     "radius",
                     48,
                     "timeoutSeconds",
                     seconds
                  )
               );
               AiOperation stairs = this.init(seconds, stoneBatch);
               return this.init(
                  stoneKey,
                  have == bx.Inner_CachePool.load ? "dig stone for a faster pickaxe" : "dig stone for the axe and furnace",
                  stairs == null ? gather.get() : this.init(stairs, () -> bx.init() >= stoneBatch, gather)
               );
            }

            if (pick == null) {
               if (!needs.isEmpty()) {
                  if (e.init_I("minecraft:diamond") >= 3 && this.init_I_Map(needs) == 0) {
                     return this.init("diamond-pick-early", "make the required diamond pick before the remaining ore", this.init_AiOperation(itemId, goal));
                  }

                  List<String> unfinished = new ArrayList<>();
                  if (e.init_I(itemId) < goal) {
                     unfinished.add(bx.init_Str(itemId));
                  }

                  kit.forEach((id, count) -> {
                     if (this.init_I_Str_2(id) < count) {
                        unfinished.add(bx.init_Str(id));
                     }
                  });
                  return this.init(needs, "for " + (unfinished.isEmpty() ? bx.init_Str(itemId) : String.join(", ", unfinished)));
               } else {
                  if (bx.load_Z(itemId)) {
                     return this.read_AiOperation_Result_10();
                  }

                  if (e.init_I(itemId) < goal) {
                     return this.init("final", "make " + goal + " " + bx.init_Str(itemId), this.init_AiOperation(itemId, goal));
                  }

                  for (Entry<String, Integer> e : kit.entrySet()) {
                     if (this.init_I_Str_2(e.getKey()) < e.getValue()) {
                        return this.init(
                           "final:" + e.getKey(),
                           "make " + e.getValue() + " " + bx.init_Str(e.getKey()),
                           this.init_AiOperation(e.getKey(), e.getValue() - this.load_I(e.getKey()))
                        );
                     }
                  }

                  return this.init(AiOperation.Status.SUCCEEDED, "Obtained " + goal + " " + itemId + " and the kit " + kit);
               }
            } else {
               Map<String, Integer> pickNeeds = bx.init_Map(pick, 1);
               return !pickNeeds.isEmpty() && bx.load_bx_CachePool(pick).ordinal() > bx.Inner_CachePool.read.ordinal()
                  ? this.init(this.init(pick, pickNeeds), "for a " + bx.init_Str(pick))
                  : this.init("pick:" + pick, "make a " + bx.init_Str(pick), this.init_AiOperation(pick, e.init_I(pick) + 1));
            }
         }

         bx.Inner_CachePool load_bx_CachePool() {
            if (bx.init_List.contains(itemId)) {
               return bx.load_bx_CachePool(itemId) == bx.Inner_CachePool.sync ? bx.Inner_CachePool.update : bx.load_bx_CachePool(itemId);
            }

            bx.Inner_CachePool needed = bx.Inner_CachePool.init;

            for (String material : this.init_Map_4().keySet()) {
               if (bx.init_bx_CachePool(material).ordinal() > needed.ordinal()) {
                  needed = bx.init_bx_CachePool(material);
               }
            }

            return needed;
         }

         int init(bx.Inner_CachePool neededTier, boolean tableHeld) {
            return this.init(neededTier, tableHeld, e.init_I("minecraft:stick"));
         }

         int init(bx.Inner_CachePool neededTier, boolean tableHeld, int sticksHeld) {
            bx.Inner_CachePool have = this.init_bx_CachePool();
            bx.Inner_CachePool last = bx.init_List.contains(itemId) ? this.load_bx_CachePool() : neededTier;
            int smelts = 0;
            if (last.ordinal() >= bx.Inner_CachePool.write.ordinal() && have.ordinal() < bx.Inner_CachePool.write.ordinal()) {
               smelts += Math.max(0, (this.sync_Z_5() ? 6 : 3) - e.init_I("minecraft:iron_ingot"));
            }

            Map<String, Integer> pending = new LinkedHashMap<>(this.init_Map_4());
            if (last.ordinal() >= bx.Inner_CachePool.write.ordinal() && have.ordinal() < bx.Inner_CachePool.write.ordinal()) {
               bx.init_Map("minecraft:iron_pickaxe", 1)
                  .forEach((id, n) -> pending.merge(id, n, itemId.equals("minecraft:iron_pickaxe") ? Math::max : Integer::sum));
            }

            int jointSmelts = 0;

            for (Entry<String, Integer> e : pending.entrySet()) {
               if (bx.load_Map.containsKey(e.getKey())) {
                  jointSmelts += e.getValue();
               }
            }

            smelts = Math.max(smelts, jointSmelts);
            boolean coal = e.init_I("minecraft:coal") + e.init_I("minecraft:charcoal") > 0;
            bx.Inner_DataUtil remainingWood = bx.init(e.init_g_StreamUtil(), kit, itemId, goal);
            return remainingWood != null
               ? bx.init(have, last, tableHeld, bx.init_Z(mc), sticksHeld, smelts, coal, this.init_Z, remainingWood)
               : bx.init(have, last, tableHeld, bx.init_Z(mc), sticksHeld, smelts, coal, this.init_Z) + (this.load_Z_13() ? 0 : kitPlanks);
         }

         AiOperation.Result read_AiOperation_Result_10() {
            String diamond = itemId.replace("minecraft:netherite_", "minecraft:diamond_");
            if (e.init_I(diamond) == 0) {
               return this.init("smith-base", "make a " + bx.init_Str(diamond) + " to upgrade", this.init_AiOperation(diamond, 1));
            } else if (e.init_I("minecraft:netherite_ingot") == 0) {
               return this.init(
                  "smith-ingot",
                  "make a netherite ingot",
                  bx.init_AiOperation_JsonObject(ag.init_JsonObject_ObjArr("timeoutSeconds", this.load_I_12()), "minecraft:netherite_ingot", 1)
               );
            } else if (e.init_I("minecraft:netherite_upgrade_smithing_template") == 0) {
               return this.init(
                  AiOperation.Status.FAILED,
                  "Ready to upgrade: "
                     + bx.init_Str(diamond)
                     + " and a netherite ingot are in the bag, but the upgrade needs a netherite upgrade smithing template, which only generates in bastion remnant chests - loot one (or ask the AI to raid a bastion) and ask again"
               );
            } else {
               return e.init_I("minecraft:smithing_table") == 0 && this.init(Blocks.SMITHING_TABLE, 12) == null
                  ? this.init("smith-table", "make a smithing table", this.init_AiOperation("minecraft:smithing_table", 1))
                  : this.init(
                     "smith",
                     "upgrade the " + bx.init_Str(diamond) + " to " + bx.init_Str(itemId),
                     cj.init(
                        ag.init_JsonObject_ObjArr(
                           "base",
                           diamond,
                           "addition",
                           "minecraft:netherite_ingot",
                           "template",
                           "minecraft:netherite_upgrade_smithing_template",
                           "timeoutSeconds",
                           Math.max(60, this.load_I_12())
                        )
                     )
                  );
            }
         }

         AiOperation.Result init(Map<String, Integer> needs, String why) {
            String material = bx.init(needs.keySet()).get(0);
            int missing = needs.get(material);
            if (material.equals("minecraft:diamond")) {
               int milestone = this.init_I_Map(needs);
               if (milestone > 0) {
                  missing = Math.min(missing, milestone);
               }
            }

            String raw = bx.load_Map.get(material);
            if (raw == null) {
               return this.init(material, missing, why);
            }

            if (material.equals("minecraft:iron_ingot") && this.sync_Z_5()) {
               missing += Math.max(0, 3 - Math.max(0, e.init_I("minecraft:iron_ingot") - needs.get(material)));
            }

            if (this.init_BlockPos != null && material.equals(this.close_Str)) {
               return this.write_AiOperation_Result_6();
            } else {
               int rawHeld = e.init_I(raw);
               int batch = Math.min(3, missing);
               if (rawHeld < batch) {
                  return this.init(raw, batch - rawHeld, why);
               } else {
                  AiOperation.Result kitx = this.init(batch, material, raw);
                  if (kitx != null) {
                     return kitx;
                  } else {
                     String fuel = this.init_Str_I(batch);
                     if ((missing > batch || this.init_bx_StreamUtil_Str(material) != null) && this.init_Map.getOrDefault("smelt-load:" + material, 0) == 0) {
                        this.close_Str = material;
                        this.open_Str = raw;
                        this.read_I = rawHeld;
                        this.write_I = batch;
                        this.flush_Z = false;
                        this.close_Z = false;
                        this.init_Set.clear();
                        return this.init(
                           "smelt-load:" + material,
                           "load the furnace with " + bx.init_Str(raw) + " " + why + " and work on while it smelts",
                           ci.load(
                              ag.init_JsonObject_ObjArr(
                                 "item",
                                 material,
                                 "count",
                                 e.init_I(material) + batch,
                                 "input",
                                 raw,
                                 "fuel",
                                 fuel,
                                 "loadOnly",
                                 true,
                                 "timeoutSeconds",
                                 Math.max(60, Math.min(180, this.load_I_12()))
                              )
                           )
                        );
                     } else {
                        return this.init(
                           "smelt:" + material,
                           "smelt " + batch + " " + bx.init_Str(material) + " " + why,
                           ci.init(
                              ag.init_JsonObject_ObjArr(
                                 "item",
                                 material,
                                 "count",
                                 e.init_I(material) + batch,
                                 "input",
                                 raw,
                                 "fuel",
                                 fuel,
                                 "timeoutSeconds",
                                 Math.max(60, this.load_I_12())
                              ),
                              this.init_Q_DataUtil(raw, batch)
                           )
                        );
                     }
                  }
               }
            }
         }

         int init_I_Map(Map<String, Integer> needs) {
            if (itemId.equals("minecraft:diamond_pickaxe")
               && goal == 1
               && !kit.isEmpty()
               && e.init_I(itemId) < goal
               && this.init_Map.getOrDefault("diamond-pick-early", 0) <= 0) {
               int missing = needs.getOrDefault("minecraft:diamond", 0);
               if (missing <= 0) {
                  return -1;
               }

               BlockState ordinaryOre = Blocks.DIAMOND_ORE.defaultBlockState();
               int slot = autismclient.ai.a.r.load(mc, ordinaryOre, true);
               if (slot < 0) {
                  return -1;
               }

               ItemStack current = mc.player.getInventory().getItem(slot);
               ItemStack upgraded = new ItemStack(Items.DIAMOND_PICKAXE);
               double before = autismclient.ai.a.r.init_D_Minecraft(mc, current, ordinaryOre, ordinaryOre.getBlock().defaultDestroyTime());
               double after = autismclient.ai.a.r.init_D_Minecraft(mc, upgraded, ordinaryOre, ordinaryOre.getBlock().defaultDestroyTime());
               if (after >= before) {
                  return -1;
               }

               boolean table = e.load() || e.init_I("minecraft:crafting_table") > 0;
               g.Inner_CachePool plan = ar.init(e.init_g_StreamUtil(), itemId, table, "", 0);
               int firstPick = Math.max(0, 3 - e.init_I("minecraft:diamond"));
               boolean affordable = plan.complete()
                  || plan.missing().size() == 1
                     && plan.missing().getOrDefault("minecraft:diamond", 0) <= firstPick
                     && plan.missing().containsKey("minecraft:diamond");
               return bx.init_I_I(missing, e.init_I("minecraft:diamond"), before, after, 40 + (table ? 0 : 60), affordable);
            } else {
               return -1;
            }
         }

         bx.Inner_StreamUtil init_bx_StreamUtil_Str(String material) {
            if (bK.load_Z() && e.init_I("minecraft:water_bucket") < 2) {
               return null;
            }

            if (material.equals("minecraft:iron_ingot")
               && this.load_bx_CachePool().ordinal() >= bx.Inner_CachePool.update.ordinal()
               && bx.init_bx_CachePool(mc).ordinal() < bx.Inner_CachePool.update.ordinal()
               && e.init_I("minecraft:diamond") < 3) {
               return bx.init_Map.get("minecraft:diamond");
            }

            for (String m : bx.init(this.init_Map_4().keySet())) {
               if (!m.equals(material)) {
                  bx.Inner_StreamUtil o = bx.init_Map.get(m);
                  if (o == null && bx.load_Map.containsKey(m)) {
                     o = bx.init_Map.get(bx.load_Map.get(m));
                  }

                  if (o != null && !o.nether()) {
                     return o;
                  }
               }
            }

            return null;
         }

         void init_V_AiOperation_Result_3(AiOperation.Result r) {
            if (this.write_Str.startsWith("smelt-load:")) {
               BlockPos at = null;
               String fuel = "";
               int cookTicks = 200;
               double progress = 0.0;

               try {
                  JsonObject data = JsonParser.parseString(r.dataJson()).getAsJsonObject();
                  at = bx.init_BlockPos(ag.init_Str_JsonObject(data, "stationAt", ""));
                  fuel = ag.init_Str_JsonObject(data, "fuel", "");
                  this.push_Str = ag.init_Str_JsonObject(data, "station", "minecraft:furnace");
                  cookTicks = Math.max(1, ag.init_I_JsonObject(data, "cookTicksPerItem", 200));
                  if (data.has("furnace")) {
                     progress = ag.init_D_JsonObject(data.getAsJsonObject("furnace"), "cookProgress", 0.0);
                  }
               } catch (RuntimeException var9) {
               }

               int loaded = this.read_I - e.init_I(this.open_Str);
               if (at == null || loaded <= 0 || fuel.isEmpty()) {
                  this.init_Map.merge(this.write_Str, 1, Integer::sum);
                  return;
               }

               this.init_BlockPos = at;
               this.load_BlockPos = mc.player.blockPosition();
               this.poll_Str = fuel;
               int cooking = Math.min(loaded, this.write_I);
               this.load_I = e.init_I(this.close_Str) + cooking;
               this.read_J = System.nanoTime() + Math.max(0L, (long)((double)cooking * cookTicks - progress * cookTicks)) * 50000000L + 500000000L;
               AutismClientAddon.LOG
                  .info(
                     "[AI] ladder for {}: {} {} cooking at {}, ready in {} s",
                     itemId,
                     cooking,
                     bx.init_Str(this.close_Str),
                     at.toShortString(),
                     (this.read_J - System.nanoTime()) / 1000000000L
                  );
            } else if (this.write_Str.startsWith("collect:") && (r.status() == AiOperation.Status.SUCCEEDED || e.init_I(this.close_Str) >= this.load_I)) {
               this.init_BlockPos = null;
            }
         }

         Q.Inner_DataUtil init_Q_DataUtil(String input, int committed) {
            Map<String, Integer> resources = new LinkedHashMap<>();

            for (Entry<String, Integer> need : this.init_Map_4().entrySet()) {
               String raw = bx.load_Map.getOrDefault(need.getKey(), need.getKey());
               if (bx.init_Map.containsKey(raw)) {
                  int held = Math.max(0, e.init_I(raw) - (raw.equals(input) && this.init_BlockPos == null ? committed : 0));
                  resources.put(raw, Math.max(held, need.getValue() - (raw.equals(input) ? committed : 0)));
               }
            }

            if (this.load_bx_CachePool().ordinal() >= bx.Inner_CachePool.update.ordinal()
               && bx.init_bx_CachePool(mc).ordinal() < bx.Inner_CachePool.update.ordinal()) {
               resources.merge("minecraft:diamond", 3, Math::max);
            }

            resources.put("minecraft:coal", Math.max(2, Math.min(8, this.write_I)));
            Map<String, Integer> crafts = this.load_bx_CachePool().ordinal() > bx.init_bx_CachePool(mc).ordinal() ? Map.of("minecraft:stick", 2) : Map.of();
            return new Q.Inner_DataUtil(resources, crafts);
         }

         AiOperation.Result write_AiOperation_Result_6() {
            long now = System.nanoTime();
            if (!this.flush_Z && this.read_J - now - this.init_J_BlockPos(this.init_BlockPos) > 3000000000L) {
               int window = (int)Math.max(0L, Math.min(2147483647L, (this.read_J - now) / 50000000L));
               if (this.load_BlockPos != null) {
                  return this.init(
                     "prework",
                     "mine more resources along a clear return route while the " + bx.init_Str(this.close_Str) + " cooks",
                     new Q(
                        this.init_BlockPos,
                        this.load_BlockPos,
                        this.open_Str,
                        window,
                        this.init_Q_DataUtil(this.open_Str, Math.max(0, this.read_I - e.init_I(this.open_Str)))
                     )
                  );
               }
            }

            this.flush_Z = true;
            int seconds = (int)Math.max(60L, Math.min(this.load_I_12(), (this.read_J - now) / 1000000000L + 90L));
            AiOperation collect = ci.load(
               ag.init_JsonObject_ObjArr(
                  "item",
                  this.close_Str,
                  "count",
                  this.load_I,
                  "input",
                  this.open_Str,
                  "fuel",
                  this.poll_Str,
                  "collect",
                  true,
                  "station",
                  this.push_Str,
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
            AiOperation back = autismclient.ai.a.CachePool.init(mc, this.init_BlockPos) != null
               ? null
               : new P(this.init_BlockPos, "Collect the background batch after productive work");
            return this.init(
               "collect:" + this.close_Str,
               "collect the " + bx.init_Str(this.close_Str) + " from the furnace at " + this.init_BlockPos.toShortString(),
               back == null ? collect : this.init(back, collect)
            );
         }

         AiOperation init_AiOperation_bx_StreamUtil(bx.Inner_StreamUtil next) {
            BlockPos feet = mc.player.blockPosition();
            Predicate<BlockState> matches = autismclient.ai.a.h.init(next.family());
            BlockPos target = this.load_Map.get(next.family());
            if (target != null && (!matches.test(mc.level.getBlockState(target)) || !this.init_Z_BlockPos_6(target))) {
               target = null;
            }

            if (target == null) {
               target = autismclient.ai.a.h.load(mc.level, feet, 64, 1, matches, this::init_Z_BlockPos_6);
            }

            if (target != null) {
               this.load_Map.put(next.family(), target);
            }

            if (target == null) {
               Direction facing = mc.player.getDirection();
               target = new BlockPos(feet.getX() + facing.getStepX() * 512, next.bestY(), feet.getZ() + facing.getStepZ() * 512);
            }

            Predicate<BlockPos> protectedCells = autismclient.ai.a.h.init(
               mc.level, state -> matches.test(state) || state.requiresCorrectToolForDrops() && !this.init_Z_BlockState(state)
            );
            Predicate<BlockPos> blocked = cell -> this.init_Set.contains(cell.asLong()) || protectedCells.test(cell);
            Predicate<BlockPos> noFloor = autismclient.ai.a.h.init(mc.level);
            List<BlockPos> steps = autismclient.ai.a.h.read(feet, target, 48, blocked, noFloor);
            if (steps.isEmpty()) {
               return null;
            }

            this.read_BlockPos = steps.get(0);
            int seconds = (int)Math.max(20L, (this.read_J - System.nanoTime()) / 1000000000L + 10L);
            return autismclient.ai.a.h.init(steps, seconds);
         }

         AiOperation init(int seconds, int stockGoal) {
            BlockPos feet = mc.player.blockPosition();
            bx.Inner_StreamUtil next = this.load_bx_CachePool().ordinal() >= bx.Inner_CachePool.write.ordinal() ? bx.init_Map.get("minecraft:raw_iron") : null;
            Predicate<BlockState> ore = next == null ? state -> false : autismclient.ai.a.h.init(next.family());
            BlockPos target = next == null ? null : this.load_Map.get(next.family());
            if (target != null && (!ore.test(mc.level.getBlockState(target)) || !this.init_Z_BlockPos_6(target))) {
               target = null;
            }

            if (target == null && next != null) {
               target = autismclient.ai.a.h.init(
                  mc.level, feet, 48, 4, ore, this::init_Z_BlockPos_6, Math.max(1, 3 - e.init_I("minecraft:raw_iron") - e.init_I("minecraft:iron_ingot"))
               );
            }

            boolean toOre = target != null;
            if (toOre) {
               this.load_Map.put(next.family(), target);
            }

            if (target == null) {
               Direction facing = mc.player.getDirection();
               target = new BlockPos(
                  feet.getX() + facing.getStepX() * 512, next == null ? feet.getY() - 40 : next.bestY(), feet.getZ() + facing.getStepZ() * 512
               );
            }

            Predicate<BlockState> keep = state -> ore.test(state) || state.requiresCorrectToolForDrops() && !this.init_Z_BlockState(state);
            if (toOre) {
               BlockPos top = autismclient.ai.a.h.init_BlockPos_Level_3(mc.level, feet, target, autismclient.ai.a.h.init(mc.level, keep), 40);
               if (top != null) {
                  int inShaft = bx.init();

                  for (int y = top.getY() - 1; y >= target.getY(); y--) {
                     if (bx.read_List
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
                           inShaft - bx.init()
                        );
                     return autismclient.ai.a.h.init_AiOperation_BlockPos(top, target.getY(), seconds, () -> bx.init() >= stockGoal);
                  }
               }
            }

            BlockPos start = toOre ? autismclient.ai.a.h.init_BlockPos_Level(mc.level, feet, target) : null;
            List<BlockPos> steps = autismclient.ai.a.h.init_List_BlockPos(
               start != null ? start : feet, target, 32, autismclient.ai.a.h.init(mc.level, keep), autismclient.ai.a.h.init(mc.level)
            );
            if (start != null && steps.size() < 2) {
               start = null;
               steps = autismclient.ai.a.h.init_List_BlockPos(feet, target, 32, autismclient.ai.a.h.init(mc.level, keep), autismclient.ai.a.h.init(mc.level));
            }

            if (start != null) {
               int on = steps.indexOf(feet);
               if (on >= 0) {
                  steps = new ArrayList<>(steps.subList(on + 1, steps.size()));
                  start = null;
               }
            }

            boolean exposedNear = this.init(feet, 5);
            int reach = exposedNear ? 9 : 23;
            int stone = bx.init();
            int enough = -1;
            int last = Math.min(steps.size(), reach + 1) - 1;

            for (int i = 0; i <= last && enough < 0; i++) {
               for (int h = 0; h < 3; h++) {
                  if (bx.read_List.contains(BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(steps.get(i).above(h)).getBlock()).toString())) {
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

            if (enough < 0 && toOre && !steps.isEmpty() && steps.size() <= reach + 1 && stone > bx.init()) {
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
                     stone - bx.init()
                  );
               return null;
            }

            boolean toTheIron = this.init_bx_CachePool() != bx.Inner_CachePool.load
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
                  stone - bx.init(),
                  bx.init()
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

            return autismclient.ai.a.h.init_AiOperation_List(steps, seconds, toTheIron ? () -> false : () -> bx.init() >= stockGoal, start);
         }

         boolean init_Z_BlockPos_6(BlockPos pos) {
            for (Direction side : Direction.values()) {
               BlockPos n = pos.relative(side);
               FluidState fluid = mc.level.getFluidState(n);
               if (fluid.is(FluidTags.LAVA)) {
                  return false;
               }

               if (side != Direction.DOWN && fluid.is(FluidTags.WATER)) {
                  return false;
               }

               if (mc.level.getBlockState(n).isAir() && mc.level.getBrightness(LightLayer.SKY, n) == 0 && !autismclient.ai.a.FileCache.update(n)) {
                  return false;
               }
            }

            return true;
         }

         boolean init(BlockPos feet, int radius) {
            for (BlockPos p : BlockPos.betweenClosed(feet.offset(-radius, -radius, -radius), feet.offset(radius, radius, radius))) {
               if (bx.read_List.contains(BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(p).getBlock()).toString())) {
                  for (Direction d : Direction.values()) {
                     if (mc.level.getBlockState(p.relative(d)).isAir()) {
                        return true;
                     }
                  }
               }
            }

            return false;
         }

         boolean init_Z_BlockState(BlockState state) {
            Inventory inventory = mc.player.getInventory();

            for (int i = 0; i < inventory.getContainerSize(); i++) {
               if (inventory.getItem(i).isCorrectToolForDrops(state)) {
                  return true;
               }
            }

            return false;
         }

         AiOperation init(final AiOperation first, final BooleanSupplier enough, final Supplier<AiOperation> second) {
            return new AiOperation() {
               AiOperation init;

               @Override
               public AiOperation.Result tick() {
                  if (this.init != null) {
                     return this.init.tick();
                  }

                  AiOperation.Result r = first.tick();
                  if (!r.terminal()) {
                     return r;
                  }

                  if (enough.getAsBoolean()) {
                     return r;
                  }

                  AutismClientAddon.LOG.info("[AI] ladder for {}: staircase ended short ({} {}); gathering the rest", itemId, r.status(), r.message());
                  this.init = second.get();
                  return this.init.tick();
               }

               @Override
               public boolean canInterruptNow() {
                  return (this.init == null ? first : this.init).canInterruptNow();
               }

               @Override
               public boolean requiresReconciliationAfterEmergency() {
                  return (this.init == null ? first : this.init).requiresReconciliationAfterEmergency();
               }

               @Override
               public void cancel() {
                  first.cancel();
                  if (this.init != null) {
                     this.init.cancel();
                  }
               }
            };
         }

         boolean init_Z_D_2(double radius) {
            for (Entity entity : mc.level.entitiesForRendering()) {
               if (entity instanceof Enemy && entity.isAlive() && entity.distanceToSqr(mc.player) <= radius * radius) {
                  return false;
               }
            }

            return true;
         }

         AiOperation init(final AiOperation first, final AiOperation second) {
            return new AiOperation() {
               boolean init;

               @Override
               public AiOperation.Result tick() {
                  if (!this.init) {
                     AiOperation.Result r = first.tick();
                     if (!r.terminal()) {
                        return r;
                     }

                     first.cancel();
                     this.init = true;
                  }

                  return second.tick();
               }

               @Override
               public boolean canInterruptNow() {
                  return (this.init ? second : first).canInterruptNow();
               }

               @Override
               public boolean requiresReconciliationAfterEmergency() {
                  return (this.init ? second : first).requiresReconciliationAfterEmergency();
               }

               @Override
               public void cancel() {
                  first.cancel();
                  second.cancel();
               }
            };
         }

         AiOperation init_AiOperation_AiOperation(final AiOperation inner) {
            return new AiOperation() {
               int init;

               @Override
               public AiOperation.Result tick() {
                  if (inner.canInterruptNow() && System.nanoTime() + init_J_BlockPos(init_BlockPos) >= read_J && (!write_Z_7() || this.init++ >= 20)) {
                     close_Z = true;
                     inner.cancel();
                     AutismClientAddon.LOG
                        .info(
                           "[AI] ladder for {}: time to walk back to the furnace ({} s of cooking left, {} s back)",
                           itemId,
                           Math.max(0L, (read_J - System.nanoTime()) / 1000000000L),
                           init_J_BlockPos(init_BlockPos) / 1000000000L
                        );
                     return R.init_AiOperation_Result(AiOperation.Status.SUCCEEDED, "Time to go back for the " + bx.init_Str(close_Str), init_JsonObject_45());
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
            return (long)(bx.init_D_D(home.getX() + 0.5 - mc.player.getX(), home.getY() - mc.player.getY(), home.getZ() + 0.5 - mc.player.getZ()) * 1.0E9);
         }

         AiOperation.Result init(String drop, int count, String why) {
            bx.Inner_StreamUtil ore = bx.init_Map.get(drop);
            if (bK.load_Z() && drop.equals("minecraft:diamond") && !this.update_Z_6() && e.init_I("minecraft:water_bucket") < 2) {
               if (this.init_Map.getOrDefault("expedition-water", 0) >= 3) {
                  return this.init(AiOperation.Status.FAILED, "Diamond descent blocked: both water buckets could not be filled");
               }

               int emptyNeeded = 2 - e.init_I("minecraft:water_bucket");
               int ironNeeded = Math.max(0, emptyNeeded - e.init_I("minecraft:bucket")) * 3;
               return e.init_I("minecraft:iron_ingot") < ironNeeded
                  ? this.init(
                     "expedition-water",
                     "secure iron for both expedition buckets",
                     aE.init(ag.init_JsonObject_ObjArr("item", "minecraft:iron_ingot", "count", ironNeeded, "timeoutSeconds", Math.min(240, this.load_I_12())))
                  )
                  : this.init("expedition-water", "fill both water buckets before diamond descent", aD.init(2, Math.min(180, this.load_I_12())));
            } else {
               if (itemId.equals("minecraft:obsidian") && drop.equals("minecraft:diamond") && e.init_I("minecraft:water_bucket") == 0) {
                  return this.init("obsidian-water", "fill the obsidian bucket before descending for diamonds", aD.init_AiOperation_I(this.load_I_12()));
               }

               if (drop.equals("minecraft:obsidian")) {
                  return this.init("ore:" + drop, "obtain " + count + " obsidian " + why, aD.load(e.init_I(drop) + count, this.load_I_12()));
               }

               if (!ore.nether() || this.update_Z_6()) {
                  bx.Inner_CachePool last = this.load_bx_CachePool();
                  if (bx.init(itemId, drop) && e.init_I("minecraft:crafting_table") == 0) {
                     BlockPos table = this.init(Blocks.CRAFTING_TABLE, 6);
                     if (table != null && bx.init_Z_Z(this.init_Z, bx.load(), this.init(last, true))) {
                        return this.init("reclaim-table", "pick up the crafting table", this.init(table, "minecraft:crafting_table"));
                     }

                     if (this.init_Z && table == null && !this.update_Z) {
                        this.update_Z = true;
                        AiOperation recovery = bx.init(Math.min(90, this.load_I_12()));
                        if (recovery != null) {
                           return this.init("reclaim-table", "recover the observed table before acquiring new wood", recovery);
                        }
                     }

                     return this.init("table", "make a crafting table to carry", this.init_AiOperation("minecraft:crafting_table", 1));
                  } else {
                     int woodNeed = this.init(last, true);
                     if (bx.init(itemId, drop) && bx.load() < woodNeed) {
                        return this.init(
                           "wood",
                           "get wood for sticks ("
                              + bx.load()
                              + " planks' worth: "
                              + af.init_I(af.load_af_DataUtil("planks"))
                              + " planks, "
                              + af.init_I(af.load_af_DataUtil("log"))
                              + " logs, "
                              + e.init_I("minecraft:stick")
                              + " sticks; need "
                              + woodNeed
                              + ")",
                           bx.init(af.init_I(af.load_af_DataUtil("log")) + (woodNeed - bx.load() + 3) / 4, Math.min(180, this.load_I_12()))
                        );
                     }

                     af.load(af.load_af_DataUtil(bx.init(bx.init_bx_CachePool(mc))));
                     return this.init(
                        "ore:" + drop,
                        "mine " + count + " " + bx.init_Str(drop) + " " + why,
                        aH.init(
                           ore.family(), drop, e.init_I(drop) + count, bx.init(ore, count), ore.bestY(), this.load_I_12(), this.load_Map.remove(ore.family())
                        )
                     );
                  }
               } else if (e.init_I("minecraft:furnace") == 0) {
                  return this.init("pack-furnace", "make a furnace to take to the Nether", this.init_AiOperation("minecraft:furnace", 1));
               } else if (e.init_I("minecraft:coal") < 4 && this.init_Map.getOrDefault("pack-coal", 0) == 0) {
                  return this.init("pack-coal", "mine coal to take to the Nether", aH.init("coal_ore", "minecraft:coal", 4, 4, Math.min(240, this.load_I_12())));
               } else {
                  return e.init_I("minecraft:crafting_table") == 0
                     ? this.init("pack-table", "make a crafting table to take along", this.init_AiOperation("minecraft:crafting_table", 1))
                     : this.init(
                        "nether",
                        "get to the Nether",
                        aA.init(ag.init_JsonObject_ObjArr("skipVillage", true, "timeoutSeconds", Math.max(600, this.load_I_12())))
                     );
               }
            }
         }

         AiOperation.Result init(int items, String output, String input) {
            if (!ci.init(mc, output, input)) {
               return this.init("furnace", "make a furnace", this.init_AiOperation("minecraft:furnace", 1));
            }

            if (this.init_Str_I(items) != null) {
               return null;
            }

            int largestPlanks = 0;

            for (Item plank : af.load_af_DataUtil("planks").items()) {
               largestPlanks = Math.max(largestPlanks, e.init_I(BuiltInRegistries.ITEM.getKey(plank).toString()));
            }

            int planksShort = (items * 200 + 299) / 300 - largestPlanks;
            if (af.init_I(af.load_af_DataUtil("log")) > 0 && this.init_Map.getOrDefault("fuel-planks", 0) == 0) {
               AiOperation planks = aV.init(mc.player.getInventory(), planksShort);
               if (planks != null) {
                  return this.init("fuel-planks", "turn a log into planks to burn", planks);
               }
            }

            return e.init_I("minecraft:coal") == 0 && this.init_Map.getOrDefault("fuel-coal", 0) == 0
               ? this.init("fuel-coal", "mine coal for fuel", aH.init("coal_ore", "minecraft:coal", 2, 2, Math.min(120, this.load_I_12())))
               : this.init("fuel-wood", "get wood to burn", bx.init(af.init_I(af.load_af_DataUtil("log")) + 1, Math.min(180, this.load_I_12())));
         }

         String init_Str_I(int items) {
            long needed = items * 200L;

            for (String coal : List.of("minecraft:coal", "minecraft:charcoal")) {
               if (e.init_I(coal) * 1600L >= needed) {
                  return coal;
               }
            }

            for (Item plank : af.load_af_DataUtil("planks").items()) {
               String id = BuiltInRegistries.ITEM.getKey(plank).toString();
               if (e.init_I(id) * 300L >= needed) {
                  return id;
               }
            }

            return null;
         }

         AiOperation init_AiOperation(String item, int count) {
            return aE.init(ag.init_JsonObject_ObjArr("item", item, "count", count, "staged", true, "timeoutSeconds", Math.max(60, this.load_I_12())));
         }

         AiOperation init(BlockPos pos, String item) {
            return bx.init_AiOperation_BlockPos(pos, item, 30);
         }

         BlockPos init(Block block, int radius) {
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

         AiOperation.Result init(String key, String label, AiOperation op) {
            this.sync_I = this.init(this.load_bx_CachePool(), e.init_I("minecraft:crafting_table") > 0);
            autismclient.ai.a.UtilCore.init(this.sync_I);
            this.flush_I = this.init_I_29()
               + (this.load_bx_CachePool().ordinal() >= bx.Inner_CachePool.update.ordinal() && bx.init_bx_CachePool(mc) == bx.Inner_CachePool.write ? 3 : 0);
            autismclient.ai.a.UtilCore.load(this.flush_I);
            this.init_AiOperation = bJ.init_AiOperation_Str("progression", key + ": " + label, op);
            this.write_Str = key;
            this.update_Str = label;
            AutismClientAddon.LOG
               .info(
                  "[AI] ladder for {}: {} (pickaxe {}, y {}, {} s left; wood {} planks' worth, {} sticks, {} held back)",
                  itemId,
                  label,
                  bx.init_bx_CachePool(mc),
                  mc.player.getBlockY(),
                  this.load_I_12(),
                  bx.load(),
                  e.init_I("minecraft:stick"),
                  this.sync_I
               );
            return this.load_AiOperation_Result_2(label);
         }

         boolean update_Z_6() {
            return "minecraft:the_nether".equals(mc.level.dimension().identifier().toString());
         }

         JsonArray init_JsonArray_2() {
            JsonArray keep = new JsonArray();
            bx.init(itemId, kit.keySet()).forEach(keep::add);
            return keep;
         }

         AiOperation.Result update_AiOperation_Result_4() {
            if (this.pop) {
               return null;
            }

            long time = ch.init_J();
            long day = mc.level.getOverworldClockTime() / 24000L;
            if (time >= 12500L && time <= 22000L && day != bx.read_J && System.nanoTime() >= bx.write_J) {
               if (day != bx.update_J) {
                  bx.update_J = day;
                  bx.update_I = 0;
               }

               if (!"minecraft:overworld".equals(mc.level.dimension().identifier().toString())) {
                  return null;
               }

               BlockPos feet = mc.player.blockPosition();
               if (mc.level.getHeight(Types.MOTION_BLOCKING, feet.getX(), feet.getZ()) > feet.getY() + 1) {
                  return null;
               }

               if (aG.init_Z() && af.init_I(af.init_af_DataUtil("bed")) == 0) {
                  bx.read_J = day;
                  return null;
               }

               bx.read_J = day;
               if (this.init_AiOperation != null) {
                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
               }

               boolean bed = af.init_I(af.init_af_DataUtil("bed")) > 0;
               this.pop = true;
               AutismClientAddon.LOG.info("[AI] ladder for {}: night falls, {}", itemId, bed ? "sleeping" : "burrowing");
               this.init_AiOperation = bed
                  ? ch.init(ag.init_JsonObject_ObjArr("timeoutSeconds", 240))
                  : RingBuffer.init(ag.init_JsonObject_ObjArr("timeoutSeconds", 600));
               this.write_Str = "night";
               this.update_Str = bed ? "sleep through the night" : "burrow until dawn";
               return this.load_AiOperation_Result_2("Night falls; " + this.update_Str);
            } else {
               return null;
            }
         }

         AiOperation.Result sync_AiOperation_Result_4() {
            X.init_V();
            if (this.load_AiOperation != null) {
               AiOperation.Result r = this.load_AiOperation.tick();
               if (r.status() == AiOperation.Status.UNCERTAIN) {
                  return this.init(AiOperation.Status.UNCERTAIN, r.message());
               }

               if (!r.terminal()) {
                  return this.load_AiOperation_Result_2("Interrupted: " + this.sync_Str + " - " + r.message());
               }

               this.load_AiOperation.cancel();
               this.load_AiOperation = null;
               if (r.status() != AiOperation.Status.SUCCEEDED) {
                  X.read_V();
               }

               if (X.write_Z && r.status() != AiOperation.Status.SUCCEEDED) {
                  X.sync_V();
               } else if (this.open_Z && r.status() != AiOperation.Status.SUCCEEDED && !X.init_Z && !X.load_Z) {
                  X.write_V();
               }

               if (this.poll_Z && (r.status() != AiOperation.Status.SUCCEEDED || X.init_X_BufferHelper().hostilesNear() > 0)) {
                  X.load_V();
               }

               AutismClientAddon.LOG.info("[AI] ladder for {}: guard {} after {}: {}", itemId, r.status(), this.sync_Str, bx.load_Str(r.message()));
               return this.load_AiOperation_Result_2("Back to the ladder after " + this.sync_Str);
            } else if (autismclient.ai.a.AiMotionSafety.fill_Z() && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow())) {
               X.Inner_BufferHelper vitals = X.init_X_BufferHelper();
               AiOperation action = X.init_AiOperation_X_CachePool(this.init_X_CachePool, vitals, this.load_I_12());
               if (action == null) {
                  return null;
               }

               this.poll_Z = X.read_Z;
               this.open_Z = !this.poll_Z && X.init_X_StreamUtil(this.init_X_CachePool, vitals).action() == X.Inner_DataUtil.load;
               this.sync_Str = X.init_Str(this.init_X_CachePool, vitals);
               AutismClientAddon.LOG.info("[AI] ladder for {}: guard steps in ({}, health {})", itemId, this.sync_Str, (int)vitals.health());
               if (this.init_AiOperation != null) {
                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
               }

               this.load_AiOperation = action;
               return this.load_AiOperation_Result_2("Interrupted: " + this.sync_Str);
            } else {
               return null;
            }
         }

         int load_I_12() {
            return (int)Math.max(10L, (this.init_J - System.nanoTime()) / 1000000000L);
         }

         JsonObject init_JsonObject_45() {
            JsonObject data = ag.init_JsonObject_ObjArr(
               "item",
               itemId,
               "goal",
               goal,
               "held",
               e.init_I(itemId),
               "pickaxe",
               bx.init_bx_CachePool(mc).name(),
               "steps",
               this.init_JsonArray,
               "furnaceStepEvidence",
               this.init_JsonObject,
               "lastFailure",
               this.flush_Str
            );
            if (this.sync_Z) {
               data.addProperty("completionTableAttempted", true);
               data.addProperty("carriedTable", e.init_I("minecraft:crafting_table") > 0);
               if (e.init_I("minecraft:crafting_table") == 0) {
                  data.add("missing", ag.init_JsonObject_ObjArr("minecraft:crafting_table", 1));
               }
            }

            return data;
         }

         AiOperation.Result load_AiOperation_Result_2(String message) {
            return R.init_AiOperation_Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_45());
         }

         AiOperation.Result init(AiOperation.Status status, String message) {
            return this.init(status, message, null);
         }

         AiOperation.Result init(AiOperation.Status status, String message, A failure) {
            cG.init_V(this);
            autismclient.ai.a.UtilCore.init(0);
            autismclient.ai.a.UtilCore.load(0);
            autismclient.ai.a.UtilCore.read(0);
            AiOperation.Result result = R.init_AiOperation_Result(status, message, this.init_JsonObject_45());
            if (failure != null) {
               result = new AiOperation.Result(result.status(), result.message(), result.dataJson(), result.evidence(), failure);
            }

            result = this.init_bb.init(result, this.write_Str.equals("pack-table-completion") && this.init_bb.init_Z(this.init_AiOperation));
            if (this.init_AiOperation != null) {
               if (this.write_Str.equals("pack-table-completion")) {
                  this.init_bb.init_V(this.init_AiOperation);
               } else {
                  this.init_AiOperation.cancel();
               }

               this.init_AiOperation = null;
            }

            if (this.load_AiOperation != null) {
               this.load_AiOperation.cancel();
               this.load_AiOperation = null;
            }

            return result;
         }

         @Override
         public boolean canInterruptNow() {
            return !this.init_bb.init_Z(null)
               && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow())
               && (this.load_AiOperation == null || this.load_AiOperation.canInterruptNow());
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.init_bb.init_Z(null)
               || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency()
               || this.load_AiOperation != null && this.load_AiOperation.requiresReconciliationAfterEmergency();
         }

         boolean sync_Z_5() {
            return itemId.equals("minecraft:obsidian") && e.init_I("minecraft:bucket") == 0 && e.init_I("minecraft:water_bucket") == 0;
         }

         @Override
         public void cancel() {
            this.push_Z = true;
            cG.init_V(this);
            autismclient.ai.a.UtilCore.init(0);
            autismclient.ai.a.UtilCore.load(0);
            autismclient.ai.a.UtilCore.read(0);
            if (this.init_AiOperation != null) {
               if (this.write_Str.equals("pack-table-completion")) {
                  this.init_bb.init_V(this.init_AiOperation);
               } else {
                  this.init_AiOperation.cancel();
               }
            }

            if (this.load_AiOperation != null) {
               this.load_AiOperation.cancel();
            }
         }
      };
   }

   static BlockPos init(BlockPos feet, List<BlockPos> tables, List<BlockPos> logs, int wood, int seconds) {
      return init_BlockPos_BlockPos(feet, tables, logs, wood, seconds, false);
   }

   static BlockPos init_BlockPos_BlockPos(BlockPos feet, List<BlockPos> tables, List<BlockPos> logs, int wood, int seconds, boolean carryRequired) {
      double woodSeconds = !carryRequired && wood >= 4 ? 1.0 : 1.0 / 0.0;

      for (BlockPos log : logs) {
         woodSeconds = Math.min(woodSeconds, 3.0 + init_D_D(log.getX() - feet.getX(), log.getY() - feet.getY(), log.getZ() - feet.getZ()));
      }

      BlockPos best = null;
      double bestSeconds = 1.0 / 0.0;

      for (BlockPos table : tables) {
         double distance = table.distSqr(feet);
         if (!(distance > 4096.0) && !table.equals(feet.below())) {
            double cost = 3.0 + init_D_D(table.getX() - feet.getX(), table.getY() - feet.getY(), table.getZ() - feet.getZ());
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
      if (mc.player != null && mc.level != null && e.init_I("minecraft:crafting_table") <= 0 && autismclient.ai.a.AiMotionSafety.fill_Z()) {
         ItemEntity observed = null;
         double dropCost = 1.0 / 0.0;

         for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof ItemEntity drop && drop.isAlive() && drop.getItem().is(Items.CRAFTING_TABLE)) {
               double cost = init_D_Vec3(mc.player.position(), drop.position(), seconds);
               if (cost < dropCost) {
                  observed = drop;
                  dropCost = cost;
               }
            }
         }

         if (observed != null && dropCost <= 3.5) {
            return T.write(ag.init_JsonObject_ObjArr("item", "minecraft:crafting_table", "count", 1, "timeoutSeconds", Math.min(90, seconds)));
         } else {
            BlockPos feet = mc.player.blockPosition();
            List<BlockPos> tables = cO.init(s -> s.is(Blocks.CRAFTING_TABLE), 4, 4, 32)
               .stream()
               .filter(pos -> mc.level.hasChunkAt(pos) && mc.level.getBlockState(pos).is(Blocks.CRAFTING_TABLE))
               .toList();
            List<BlockPos> logs = !carryRequired && load() < 4 ? cO.init(s -> s.is(BlockTags.LOGS), 4, 4, 32) : List.of();
            BlockPos best = init_BlockPos_BlockPos(feet, tables, logs, load(), seconds, carryRequired);
            if (observed == null
               || best != null && !(dropCost < 3.0 + init_D_D(best.getX() - feet.getX(), best.getY() - feet.getY(), best.getZ() - feet.getZ()))) {
               return best == null ? null : init_AiOperation_BlockPos(best, "minecraft:crafting_table", Math.min(90, seconds));
            } else {
               return T.write(ag.init_JsonObject_ObjArr("item", "minecraft:crafting_table", "count", 1, "timeoutSeconds", Math.min(90, seconds)));
            }
         }
      } else {
         return null;
      }
   }

   static double init_D_Vec3(Vec3 body, Vec3 drop, int seconds) {
      if (body.distanceToSqr(drop) > 4096.0) {
         return 1.0 / 0.0;
      }

      double cost = 1.0 + init_D_D(drop.x - body.x, drop.y - body.y, drop.z - body.z);
      return cost + 5.0 <= seconds ? cost : 1.0 / 0.0;
   }

   static boolean init(boolean broken, boolean carried, boolean liveDrop, boolean childPending) {
      return childPending || broken && !carried && !liveDrop;
   }

   private static AiOperation init_AiOperation_BlockPos(final BlockPos pos, final String item, final int seconds) {
      final Minecraft mc = Minecraft.getInstance();
      Object stationWorld = mc.level;
      Object stationPlayer = mc.player;
      final long stationDeadline = System.nanoTime() + Math.max(1, seconds) * 1000000000L;
      AiOperation recovery = new AiOperation() {
         final Object init_Obj = mc.level;
         final Object load_Obj = mc.player;
         final long init_J = stationDeadline;
         final int init_I = e.init_I(item);
         final bb init_bb = new bb();
         AiOperation init_AiOperation = T.init(List.of(item), Math.max(1, seconds - 5));
         AiOperation load_AiOperation;
         AiOperation read_AiOperation;
         int load_I;
         boolean init_Z;
         boolean load_Z;
         boolean read_Z;

         @Override
         public AiOperation.Result tick() {
            if (this.read_Z) {
               return this.init(R.init_AiOperation_Result(AiOperation.Status.CANCELLED, "Station recovery cancelled", new JsonObject()));
            }

            if (mc.player == this.load_Obj && mc.level == this.init_Obj && System.nanoTime() <= this.init_J) {
               if (this.read_AiOperation == null) {
                  if (!this.init_Z) {
                     if (this.init_AiOperation != null) {
                        AiOperation.Result ready = this.init_bb.init_AiOperation_Result(this.init_AiOperation);
                        if (!ready.terminal()) {
                           return ready;
                        }

                        this.init_bb.init_V(this.init_AiOperation);
                        this.init_AiOperation = null;
                        if (ready.status() != AiOperation.Status.SUCCEEDED) {
                           return this.init(ready);
                        }

                        if (e.init_I(item) > this.init_I) {
                           return this.init(
                              R.init_AiOperation_Result(
                                 AiOperation.Status.SUCCEEDED,
                                 "Observed the carried " + bx.init_Str(item),
                                 ag.init_JsonObject_ObjArr("item", item, "held", e.init_I(item))
                              )
                           );
                        }
                     }

                     if (this.load_AiOperation == null) {
                        this.load_AiOperation = autismclient.ai.a.ConfigStore.init(
                           pos, (int)Math.max(1L, (this.init_J - System.nanoTime()) / 1000000000L - 5L), false, Set.of(), true
                        );
                     }

                     AiOperation.Result r = this.init_bb.init_AiOperation_Result(this.load_AiOperation);
                     if (!r.terminal()) {
                        return r;
                     }

                     if (r.status() != AiOperation.Status.SUCCEEDED) {
                        return this.init(r);
                     }

                     this.init_Z = true;
                  }

                  if (e.init_I(item) > this.init_I) {
                     return this.init(
                        R.init_AiOperation_Result(
                           AiOperation.Status.SUCCEEDED,
                           "Picked the " + bx.init_Str(item) + " back up",
                           ag.init_JsonObject_ObjArr("item", item, "held", e.init_I(item))
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

                  if (++this.load_I < 10 && !drop) {
                     return R.init_AiOperation_Result(AiOperation.Status.RUNNING, "Waiting for the " + bx.init_Str(item) + " to drop", new JsonObject());
                  }

                  int remaining = (int)Math.max(1L, (this.init_J - System.nanoTime()) / 1000000000L);
                  this.read_AiOperation = T.write(ag.init_JsonObject_ObjArr("item", item, "count", this.init_I + 1, "timeoutSeconds", Math.min(15, remaining)));
               }

               AiOperation.Result result = this.init_bb.init_AiOperation_Result(this.read_AiOperation);
               return result.terminal() ? this.init(result) : result;
            } else {
               return this.init(R.init_AiOperation_Result(AiOperation.Status.TIMED_OUT, "Station recovery stopped before verified pickup", new JsonObject()));
            }
         }

         AiOperation.Result init(AiOperation.Result result) {
            ItemEntity live = this.init_ItemEntity_2();
            JsonObject data = bK.init_JsonObject(result.dataJson());
            data.addProperty("stationBroken", this.init_Z);
            data.addProperty("observedPickupIncomplete", this.init_Z && e.init_I(item) <= this.init_I);
            data.add(
               "liveDrop",
               live == null
                  ? JsonNull.INSTANCE
                  : ag.init_JsonObject_ObjArr("item", item, "count", live.getItem().getCount(), "x", live.getX(), "y", live.getY(), "z", live.getZ())
            );
            result = new AiOperation.Result(result.status(), result.message(), data.toString(), result.evidence(), result.failure());
            result = this.init_bb.init(result, this.requiresReconciliationAfterEmergency());
            this.load_Z = true;
            return result;
         }

         ItemEntity init_ItemEntity_2() {
            if (mc.level != this.init_Obj) {
               return null;
            }

            for (ItemEntity drop : mc.level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(4.0))) {
               if (drop.isAlive() && !drop.getItem().isEmpty() && BuiltInRegistries.ITEM.getKey(drop.getItem().getItem()).toString().equals(item)) {
                  return drop;
               }
            }

            return null;
         }

         AiOperation init_AiOperation_3() {
            return this.init_AiOperation != null ? this.init_AiOperation : (this.read_AiOperation == null ? this.load_AiOperation : this.read_AiOperation);
         }

         @Override
         public boolean canInterruptNow() {
            return !this.init_bb.init_Z(this.init_AiOperation_3())
               && (this.load_Z || !this.init_Z && (this.init_AiOperation_3() == null || this.init_AiOperation_3().canInterruptNow()));
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return bx.init(
               !this.load_Z && this.init_Z, e.init_I(item) > this.init_I, this.init_ItemEntity_2() != null, this.init_bb.init_Z(this.init_AiOperation_3())
            );
         }

         @Override
         public void cancel() {
            this.init_bb.init(this.requiresReconciliationAfterEmergency());
            this.read_Z = true;
            this.init_bb.init_V(this.init_AiOperation);
            this.init_bb.init_V(this.load_AiOperation);
            this.init_bb.init_V(this.read_AiOperation);
         }
      };
      return bb.init(recovery, autismclient.ai.a.AiMotionSafety::fill_Z, () -> mc.player == stationPlayer && mc.level == stationWorld, stationDeadline);
   }

   static double init_D_D(double dx, double dy, double dz) {
      return 0.5 + Math.max(Math.hypot(dx, dz) * 0.25, Math.max(0.0, dy) * 0.45) + Math.max(0.0, -dy) * 0.2;
   }

   static BlockPos init_BlockPos(String text) {
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

   static Set<String> init(String itemId, Collection<String> more) {
      Set<String> ids = init_Set(itemId);
      ids.addAll(more);
      return ids;
   }

   static Set<String> init_Set(String itemId) {
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
      ids.addAll(init_Set);
      return ids;
   }

   static String init_Str(String itemId) {
      return itemId.replace("minecraft:", "").replace('_', ' ');
   }

   static String load_Str(String text) {
      if (text == null) {
         return "";
      } else {
         return text.length() > 200 ? text.substring(0, 200) : text;
      }
   }

   static {
      Set<String> base = new LinkedHashSet<>(init_Map.keySet());
      base.addAll(load_Map.keySet());
      init_Set = Set.copyOf(base);
   }

   enum Inner_CachePool {
      init,
      load,
      read,
      write,
      update,
      sync;
   }

   record Inner_DataUtil(int planks, int sticks) {
   }

   record Inner_StreamUtil(String family, String drop, int bestY, bx.Inner_CachePool pick, int kw_yield_I, boolean nether) {
      Inner_StreamUtil(String family, String drop, int bestY, bx.Inner_CachePool pick, int yield) {
         this(family, drop, bestY, pick, yield, false);
      }
   }
}
