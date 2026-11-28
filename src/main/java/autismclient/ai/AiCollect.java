package autismclient.ai;

import autismclient.api.ai.AiOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

final class AiCollect {
   static final Map<String, String> ORE_DROPS = Map.ofEntries(
      Map.entry("minecraft:coal_ore", "minecraft:coal"),
      Map.entry("minecraft:deepslate_coal_ore", "minecraft:coal"),
      Map.entry("minecraft:iron_ore", "minecraft:raw_iron"),
      Map.entry("minecraft:deepslate_iron_ore", "minecraft:raw_iron"),
      Map.entry("minecraft:copper_ore", "minecraft:raw_copper"),
      Map.entry("minecraft:deepslate_copper_ore", "minecraft:raw_copper"),
      Map.entry("minecraft:gold_ore", "minecraft:raw_gold"),
      Map.entry("minecraft:deepslate_gold_ore", "minecraft:raw_gold"),
      Map.entry("minecraft:diamond_ore", "minecraft:diamond"),
      Map.entry("minecraft:deepslate_diamond_ore", "minecraft:diamond"),
      Map.entry("minecraft:lapis_ore", "minecraft:lapis_lazuli"),
      Map.entry("minecraft:deepslate_lapis_ore", "minecraft:lapis_lazuli"),
      Map.entry("minecraft:redstone_ore", "minecraft:redstone"),
      Map.entry("minecraft:deepslate_redstone_ore", "minecraft:redstone"),
      Map.entry("minecraft:wheat", "minecraft:wheat"),
      Map.entry("minecraft:carrots", "minecraft:carrot"),
      Map.entry("minecraft:potatoes", "minecraft:potato"),
      Map.entry("minecraft:beetroots", "minecraft:beetroot")
   );
   private static final int init_I = 32;
   private static final String init_Str = "minecraft:iron_pickaxe";
   static final Map<String, Set<String>> load = Map.of(
      "minecraft:wheat_seeds", Set.of("minecraft:short_grass", "minecraft:tall_grass", "minecraft:fern", "minecraft:large_fern")
   );

   private AiCollect() {
   }

   static List<AiCollect.Inner_DataUtil> init(List<AiCollect.Inner_DataUtil> targets) {
      List<AiCollect.Inner_DataUtil> ordered = new ArrayList<>(targets);
      ordered.sort(
         Comparator.<AiCollect.Inner_DataUtil, Boolean>comparing(t -> t.drop())
            .reversed()
            .thenComparingDouble(AiCollect.Inner_DataUtil::distanceSqr)
            .thenComparingInt(AiCollect.Inner_DataUtil::entityId)
            .thenComparingInt(t -> t.pos().getX())
            .thenComparingInt(t -> t.pos().getY())
            .thenComparingInt(t -> t.pos().getZ())
      );
      return ordered;
   }

   static String init(String blockId, String asItemId) {
      return ORE_DROPS.getOrDefault(blockId, asItemId);
   }

   static boolean init(String blockId, String asItemId, String item) {
      return init(blockId, asItemId).equals(item) || load.getOrDefault(item, Set.of()).contains(blockId);
   }

   static int init_I(int count, int baseline, boolean additional) {
      return count + (additional ? baseline : 0);
   }

   static AiOperation.Status init_AiOperation_Status(int count, int target, boolean exact) {
      return exact && count != target ? AiOperation.Status.UNCERTAIN : AiOperation.Status.SUCCEEDED;
   }

   static String init(String item, int count, int target) {
      return "No more reachable sources for " + item + " (collected " + count + " of " + target + ")";
   }

   static boolean init(List<JsonObject> skipped, AiCollect.Inner_DataUtil target, String reason) {
      if (skipped.size() >= 32) {
         return false;
      }

      skipped.add(
         AiJson.objectOf(
            "kind", target.drop() ? "drop" : "block", "target", target.drop() ? "entity#" + target.entityId() : target.pos().toShortString(), "reason", reason
         )
      );
      return true;
   }

   static boolean init(boolean requiresCorrectTool, boolean anyCarriedToolHarvests) {
      return !requiresCorrectTool || anyCarriedToolHarvests;
   }

   static String init(BlockState state) {
      Item needed = AiItems.cheapestRequiredTool(state);
      return needed == null ? "minecraft:iron_pickaxe" : BuiltInRegistries.ITEM.getKey(needed).toString();
   }

   static JsonObject init(String toolId) {
      JsonObject missing = new JsonObject();
      missing.addProperty(toolId, 1);
      return missing;
   }

   static String load(String blockId, String toolId) {
      return "no carried tool can harvest " + blockId + " (needs " + toolId + ")";
   }

   static AiOperation init(JsonObject args) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         final String item = AiJson.getString(args, "item", "");
         int amount = AiJson.getInt(args, "count", 0);
         Identifier itemId = Identifier.tryParse(item);
         if (!item.isBlank() && itemId != null && !BuiltInRegistries.ITEM.getOptional(itemId).isEmpty()) {
            if (amount >= 1 && amount <= 2304) {
               int radius = Math.max(16, Math.min(64, AiJson.getInt(args, "radius", 32)));
               final int baseline = AiGameState.countInventoryItem(item);
               final int target = init_I(amount, baseline, AiJson.getBoolean(args, "additional", false));
               final boolean exact = AiJson.getBoolean(args, "exact", false);
               List<String> sources = new ArrayList<>();
               String tierGated = null;

               for (Block block : BuiltInRegistries.BLOCK) {
                  String blockId = BuiltInRegistries.BLOCK.getKey(block).toString();
                  if (init(blockId, BuiltInRegistries.ITEM.getKey(block.asItem()).toString(), item)) {
                     BlockState state = block.defaultBlockState();
                     if (!init(state.requiresCorrectToolForDrops(), init(client, state))) {
                        tierGated = init(state);
                     } else {
                        sources.add(blockId);
                     }
                  }
               }

               final String missingTool = sources.isEmpty() ? tierGated : null;
               final AiOperation gather = autismclient.ai.engine.AiBlockMine.create(
                  AiJson.objectOf(
                     "blocks",
                     autismclient.ai.engine.AiBlockMine.jsonArray(sources.isEmpty() ? List.of(item) : sources),
                     "drops",
                     autismclient.ai.engine.AiBlockMine.jsonArray(List.of(item)),
                     "held",
                     target,
                     "radius",
                     radius,
                     "explore",
                     false,
                     "dropsOnly",
                     sources.isEmpty(),
                     "timeoutSeconds",
                     Math.max(20, Math.min(600, AiJson.getInt(args, "timeoutSeconds", 180)))
                  )
               );
               return new AiOperation() {
                  boolean init;

                  @Override
                  public AiOperation.Result tick() {
                     if (this.init) {
                        return this.init(AiOperation.Status.CANCELLED, "Collection cancelled");
                     }

                     AiOperation.Result r = gather.tick();
                     int count = AiGameState.countInventoryItem(item);
                     if (count < target) {
                        if (!r.terminal()) {
                           return this.init(r.message());
                        } else if (r.status() == AiOperation.Status.CANCELLED) {
                           return this.init(AiOperation.Status.CANCELLED, "Collection cancelled");
                        } else if (r.status() == AiOperation.Status.TIMED_OUT) {
                           return this.init(AiOperation.Status.TIMED_OUT, "Collection deadline reached");
                        } else {
                           return missingTool != null
                              ? this.init(
                                 AiOperation.Status.FAILED,
                                 AiCollect.init(item, count - baseline, target)
                                    + "; no carried tool can harvest the remaining sources; craft "
                                    + missingTool
                                    + " first"
                              )
                              : this.init(AiOperation.Status.FAILED, AiCollect.init(item, count - baseline, target) + " - " + r.message());
                        }
                     } else {
                        gather.cancel();
                        return this.init(
                           AiCollect.init_AiOperation_Status(count, target, exact),
                           exact && count != target
                              ? "Surplus " + item + " retained; exact target requires storage reconciliation"
                              : "Collected item inventory target observed"
                        );
                     }
                  }

                  AiOperation.Result init(String message) {
                     return AiGameOperations.result(
                        AiOperation.Status.RUNNING, message, AiJson.objectOf("item", item, "count", AiGameState.countInventoryItem(item), "target", target)
                     );
                  }

                  AiOperation.Result init(AiOperation.Status status, String message) {
                     int count = AiGameState.countInventoryItem(item);
                     JsonObject data = AiJson.objectOf(
                        "item",
                        item,
                        "collected",
                        Math.max(0, count - baseline),
                        "baseline",
                        baseline,
                        "count",
                        count,
                        "target",
                        target,
                        "skipped",
                        new JsonArray(),
                        "skippedTotal",
                        0
                     );
                     if (status == AiOperation.Status.FAILED && missingTool != null) {
                        data.add("missing", AiCollect.init(missingTool));
                     }

                     return AiGameOperations.result(status, message, data);
                  }

                  @Override
                  public void cancel() {
                     if (!this.init) {
                        this.init = true;
                        gather.cancel();
                     }
                  }
               };
            } else {
               throw new IllegalArgumentException("Expected count 1..2304");
            }
         } else {
            throw new IllegalArgumentException("Expected a known item registry id");
         }
      } else {
         throw new IllegalArgumentException("Collection requires a connected living player");
      }
   }

   static boolean init(Minecraft mc, BlockState state) {
      if (!state.requiresCorrectToolForDrops()) {
         return true;
      }

      for (int slot = 0; slot < 36; slot++) {
         if (mc.player.getInventory().getItem(slot).isCorrectToolForDrops(state)) {
            return true;
         }
      }

      return false;
   }

   record Inner_DataUtil(BlockPos pos, int entityId, double distanceSqr) {
      boolean drop() {
         return this.entityId >= 0;
      }

      static AiCollect.Inner_DataUtil drop(int entityId, BlockPos pos, double distanceSqr) {
         return new AiCollect.Inner_DataUtil(pos, entityId, distanceSqr);
      }

      static AiCollect.Inner_DataUtil block(BlockPos pos, double distanceSqr) {
         return new AiCollect.Inner_DataUtil(pos, -1, distanceSqr);
      }
   }
}
