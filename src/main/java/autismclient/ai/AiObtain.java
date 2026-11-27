package autismclient.ai;

import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import autismclient.util.BlockBreakSpeedHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

final class AiObtain {
   private static final int sync = 5;
   static final int init_I = 8;
   private static final int flush = 64;
   static final double init_D = 160.0;
   static final double load_D = 40.0;
   static final int load_I = 8;
   private static final Set<String> init_Set = Set.of(
      "minecraft:stone",
      "minecraft:dirt",
      "minecraft:gravel",
      "minecraft:sand",
      "minecraft:deepslate",
      "minecraft:netherrack",
      "minecraft:tuff",
      "minecraft:andesite",
      "minecraft:diorite",
      "minecraft:granite"
   );
   static final int read = 8;
   static final int write = 32;
   static final int update = 32;

   private AiObtain() {
   }

   static AiOperation init(JsonObject args) {
      return init(args, 0, new LinkedHashSet<>());
   }

   static AiOperation init(JsonObject args, final int depth, final Set<String> visiting) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final String spec = AiJson.getString(args, "item", "");
         final AiItems.ItemSpec target = AiItems.resolveItem(spec);
         if (depth == 0 && !AiJson.getBoolean(args, "staged", false) && target.items().size() == 1) {
            String only = BuiltInRegistries.ITEM.getKey(target.items().iterator().next()).toString();
            int want = AiJson.getBoolean(args, "additional", false)
               ? AiItems.countInInventory(target) + AiJson.getInt(args, "count", 1)
               : AiJson.getInt(args, "count", 1);
            boolean equippedObsidian = only.equals("minecraft:obsidian") && AiProgression.bestTier(mc).ordinal() >= AiProgression.Inner_CachePool.DIAMOND.ordinal();
            if (equippedObsidian && want >= 1 && want <= 2304 && AiItems.countInInventory(target) < want) {
               return AiObsidian.load(want, AiJson.getInt(args, "timeoutSeconds", 600));
            }

            if (!equippedObsidian && AiProgression.init_Z(only, want)) {
               return AiProgression.obtain(args, only, want);
            }

            if (only.equals("minecraft:water_bucket") && want >= 1 && want <= 16 && AiItems.countInInventory(target) < want) {
               return AiObsidian.init(want, AiJson.getInt(args, "timeoutSeconds", 300));
            }
         }

         int requested = AiJson.getInt(args, "count", 1);
         if (requested >= 1 && requested <= 2304) {
            boolean additional = AiJson.getBoolean(args, "additional", false);
            final boolean allowCombat = AiJson.getBoolean(args, "allowCombat", true);
            final boolean allowMining = AiJson.getBoolean(args, "allowMining", true);
            final int radius = Math.max(8, Math.min(128, AiJson.getInt(args, "radius", 64)));
            final int timeoutSeconds = Math.max(10, Math.min(3600, AiJson.getInt(args, "timeoutSeconds", 600)));
            final int held = AiItems.countInInventory(target);
            final int goal = additional ? held + requested : requested;
            return new AiOperation() {
               final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
               final Object init_Obj = mc.level;
               final Object load_Obj = mc.getConnection();
               final LocalPlayer init_LocalPlayer = mc.player;
               final List<AiObtain.Inner_DataUtil> init_List = new ArrayList<>();
               final JsonArray init_JsonArray = new JsonArray();
               final Set<String> init_Set = new LinkedHashSet<>(target.itemIds());
               final AiGuard.GuardPolicy init_X_CachePool = AiGuard.GuardPolicy.standard().keeping(this.init_Set);
               AiOperation init_AiOperation;
               AiOperation load_AiOperation;
               String init_Str = "";
               int init_I;
               AiObtain.Inner_DataUtil init_aE_DataUtil;
               String load_Str = "";
               Set<Item> load_Set;
               Set<Item> read_Set;
               List<Block> load_List;
               List<EntityType<?>> read_List;
               boolean init_Z;
               boolean load_Z;
               int load_I;
               boolean read_Z;
               boolean write_Z;
               Boolean init_Boolean;
               AiItems.ItemSpec init_af_DataUtil = target;
               AiItems.ItemSpec load_af_DataUtil;
               String read_Str;
               boolean update;
               String write_Str;
               boolean sync;
               int read_I;
               final EnumMap<AiObtain.Inner_DataUtil, Integer> init_EnumMap = new EnumMap<>(AiObtain.Inner_DataUtil.class);

               @Override
               public AiOperation.Result tick() {
                  if (this.sync || mc.player == null || mc.level != this.init_Obj || mc.getConnection() != this.load_Obj || mc.player != this.init_LocalPlayer) {
                     return this.init(AiOperation.Status.CANCELLED, "Obtain cancelled or world/player changed");
                  }

                  if (!mc.player.isAlive()) {
                     return this.init(AiOperation.Status.FAILED, "Player died while obtaining supplies");
                  }

                  if (System.nanoTime() > this.init_J) {
                     return this.init(
                        this.canInterruptNow() ? AiOperation.Status.TIMED_OUT : AiOperation.Status.UNCERTAIN,
                        "Obtain deadline reached with " + AiItems.countInInventory(this.init_af_DataUtil) + "/" + goal
                     );
                  }

                  if (AiItems.countInInventory(this.init_af_DataUtil) >= goal && this.init_AiOperation == null) {
                     if (this.init_AiOperation != null) {
                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                        this.init_aE_DataUtil = null;
                     }

                     return this.init(AiOperation.Status.SUCCEEDED, "Obtained " + goal + " " + this.write_Str_2());
                  } else {
                     AiOperation.Result guarded = this.init_AiOperation_Result_17();
                     if (guarded != null) {
                        return guarded;
                     }

                     if (this.init_AiOperation == null) {
                        AiObtain.Inner_DataUtil next = this.init_aE_DataUtil();
                        if (next == null) {
                           String why = this.init_List.isEmpty()
                              ? AiObtain.init(
                                 !this.init_Set_6().isEmpty(),
                                 !this.load_Set_4().isEmpty(),
                                 !this.init_List_6().isEmpty(),
                                 AiProspect.canProspect(target),
                                 !this.load_List_3().isEmpty()
                              )
                              : "every strategy was tried";
                           return this.init(
                              AiOperation.Status.FAILED, "No remaining route to " + spec + " (" + AiItems.countInInventory(this.init_af_DataUtil) + "/" + goal + "); " + why
                           );
                        } else {
                           this.init_AiOperation = AiRunDiagnostics.trace(
                              "obtain", next.name() + " " + spec + " " + AiItems.countInInventory(this.init_af_DataUtil) + "/" + goal, this.init_AiOperation(next)
                           );
                           if (this.init_AiOperation == null) {
                              this.init_List.add(next);
                              return this.init_AiOperation_Result_9("Strategy " + next + " unavailable; trying the next");
                           } else {
                              this.init_aE_DataUtil = next;
                              this.read_I = AiItems.countInInventory(this.init_af_DataUtil);
                              return this.init_AiOperation_Result_9("Starting " + next.name().toLowerCase(Locale.ROOT) + " for " + spec);
                           }
                        }
                     } else {
                        AiOperation.Result result = this.init_AiOperation.tick();
                        if (!result.terminal()) {
                           return this.init_AiOperation_Result_9(result.message());
                        }

                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                        this.init_JsonArray.add(AiObtain.init(this.init_aE_DataUtil.name(), this.load_Str, result));
                        if (result.status() == AiOperation.Status.CANCELLED) {
                           return this.init(AiOperation.Status.CANCELLED, "Obtain cancelled");
                        }

                        if (result.status() == AiOperation.Status.UNCERTAIN) {
                           return this.init(AiOperation.Status.UNCERTAIN, result.message());
                        }

                        if (AiItems.countInInventory(this.init_af_DataUtil) >= goal) {
                           return this.init(AiOperation.Status.SUCCEEDED, "Obtained " + goal + " " + this.write_Str_2());
                        }

                        if (result.status() != AiOperation.Status.SUCCEEDED) {
                           AiOperation detour = this.init(result);
                           if (detour != null) {
                              this.init_AiOperation = AiRunDiagnostics.trace("obtain", "station detour for " + spec, detour);
                              return this.init_AiOperation_Result_9(
                                 this.load_Z && this.init_aE_DataUtil == AiObtain.Inner_DataUtil.SMELT
                                    ? "Smelting needs a furnace; making one first"
                                    : "Craft needs a crafting table; obtaining one first"
                              );
                           }

                           if ((this.init_aE_DataUtil == AiObtain.Inner_DataUtil.MINE || this.init_aE_DataUtil == AiObtain.Inner_DataUtil.GATHER)
                              && !this.read_Z
                              && AiObtain.init_Str(result.dataJson()) != null) {
                              this.read_Z = true;
                              this.init_aE_DataUtil = null;
                              return this.init_AiOperation_Result_9("Mining tool broke; re-tooling before one more attempt");
                           }

                           if (this.init_aE_DataUtil == AiObtain.Inner_DataUtil.GATHER && !this.update && AiObtain.init(result)) {
                              String family = this.read_Str_3();
                              if (family != null && !this.write_Z_6()) {
                                 this.update = true;
                                 this.write_Str = family;
                                 this.init_af_DataUtil = AiItems.resolveItem(family);
                                 this.load_Set = null;
                                 this.read_Set = null;
                                 this.load_List = null;
                                 this.read_List = null;
                                 this.init_AiOperation = AiRunDiagnostics.trace(
                                    "obtain", "family fallback " + family, this.init(Math.max(15, this.init_I_19()))
                                 );
                                 if (this.init_AiOperation == null) {
                                    return this.init_AiOperation_Result_9("No harvestable source for the " + family + " family");
                                 }

                                 this.load_Str = "gather " + family + " (family fallback from " + spec + ")";
                                 return this.init_AiOperation_Result_9("No " + spec + " in range; gathering the " + family + " family instead");
                              }
                           }

                           if (this.init_aE_DataUtil == AiObtain.Inner_DataUtil.CRAFT && this.load_I < 2 && result.dataJson().contains("\"missing\"")) {
                              this.load_I++;
                              this.init_aE_DataUtil = null;
                              return this.init_AiOperation_Result_9("Craft still short on materials; fetching the rest");
                           }

                           if (AiItems.countInInventory(this.init_af_DataUtil) > this.read_I && this.init_EnumMap.merge(this.init_aE_DataUtil, 1, Integer::sum) <= 8) {
                              this.init_aE_DataUtil = null;
                              return this.init_AiOperation_Result_9("Strategy " + result.status() + " after progress; another pass");
                           }

                           this.init_List.add(this.init_aE_DataUtil);
                        }

                        this.init_aE_DataUtil = null;
                        return this.init_AiOperation_Result_9("Strategy " + result.status() + "; re-evaluating");
                     }
                  }
               }

               AiOperation.Result init_AiOperation_Result_17() {
                  if (this.load_AiOperation != null) {
                     AiOperation.Result result = this.load_AiOperation.tick();
                     if (result.status() == AiOperation.Status.UNCERTAIN) {
                        return this.init(AiOperation.Status.UNCERTAIN, result.message());
                     }

                     if (!result.terminal()) {
                        return this.init_AiOperation_Result_9("Interrupted: " + this.init_Str + " - " + result.message());
                     }

                     this.load_AiOperation.cancel();
                     this.load_AiOperation = null;
                     return this.init_AiOperation_Result_9("Resuming after " + this.init_Str);
                  } else if (autismclient.ai.engine.AiMotionSafety.isPlayerSupported() && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow())) {
                     AiGuard.StatusSnapshot vitals = AiGuard.sampleStatus();
                     AiOperation action = AiGuard.createResponseOperation(this.init_X_CachePool, vitals, this.init_I_19());
                     if (action == null) {
                        return null;
                     }

                     this.init_Str = AiGuard.decisionReason(this.init_X_CachePool, vitals);
                     this.init_I++;
                     if (this.init_AiOperation != null) {
                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                        this.init_aE_DataUtil = null;
                     }

                     this.load_AiOperation = AiRunDiagnostics.trace("survival", "obtain: " + this.init_Str, action);
                     return this.init_AiOperation_Result_9("Interrupted: " + this.init_Str);
                  } else {
                     return null;
                  }
               }

               AiObtain.Inner_DataUtil init_aE_DataUtil() {
                  if (!this.init_List.contains(AiObtain.Inner_DataUtil.CRAFT) && !this.init_Set_6().isEmpty()) {
                     AiCraftPlanner.RecipeGraph graph = AiCraft.buildRecipeGraph();
                     if (this.init_Set_6().stream().anyMatch(item -> {
                        AiCraftPlanner.CraftPlan plan = AiCraftPlanner.init(graph, BuiltInRegistries.ITEM.getKey(item).toString(), goal);
                        return plan.complete() && !plan.steps().isEmpty();
                     })) {
                        return AiObtain.Inner_DataUtil.CRAFT;
                     }
                  }

                  if (!this.init_List.contains(AiObtain.Inner_DataUtil.SMELT)
                     && this.load_Set_4()
                        .stream()
                        .anyMatch(
                           output -> autismclient.util.AutoCraftingEngine.getSmeltRecipesFor(mc, output)
                              .stream()
                              .anyMatch(
                                 p -> p.inputs().stream().mapToInt(i -> AiGameState.countInventoryItem(BuiltInRegistries.ITEM.getKey(i).toString())).sum()
                                    >= goal - AiItems.countInInventory(this.init_af_DataUtil)
                              )
                        )) {
                     return AiObtain.Inner_DataUtil.SMELT;
                  }

                  if (allowMining
                     && !this.init_List.contains(AiObtain.Inner_DataUtil.GATHER)
                     && this.init_af_DataUtil
                        .items()
                        .stream()
                        .anyMatch(
                           item -> item instanceof BlockItem form
                              && !AiObtain.init().contains(BuiltInRegistries.ITEM.getKey(item).toString())
                              && AiObtain.init_Z_Item_Block(item, form.getBlock())
                              && this.init(form.getBlock().defaultBlockState())
                              && AiTerrain.nearestBlockDistSqr(mc, List.of(form.getBlock()), 8) <= 64.0
                        )) {
                     return AiObtain.Inner_DataUtil.GATHER;
                  }

                  if (!allowCombat
                     || this.init_List.contains(AiObtain.Inner_DataUtil.HUNT)
                     || this.load_List_3().isEmpty()
                     || this.write_Z_6()
                     || this.init(this.load_List_3(), 160.0) == null && !AiObtain.init_Z(this.load_List_3())) {
                     for (AiObtain.Inner_DataUtil strategy : AiObtain.Inner_DataUtil.values()) {
                        if (!this.init_List.contains(strategy)
                           && (strategy != AiObtain.Inner_DataUtil.HUNT || allowCombat)
                           && (strategy != AiObtain.Inner_DataUtil.GATHER && strategy != AiObtain.Inner_DataUtil.MINE || allowMining)
                           && this.init_Z(strategy)) {
                           return strategy;
                        }
                     }

                     return null;
                  } else {
                     return AiObtain.Inner_DataUtil.HUNT;
                  }
               }

               boolean init_Z(AiObtain.Inner_DataUtil strategy) {
                  boolean var10000;
                  switch (strategy) {
                     case INVENTORY:
                        var10000 = false;
                        break;
                     case TOOL:
                        var10000 = depth < 5 && this.init_Str_11() != null;
                        break;
                     case CRAFT:
                        var10000 = !this.init_Set_6().isEmpty();
                        break;
                     case SMELT:
                        var10000 = this.load_Z_12();
                        break;
                     case GATHER:
                        var10000 = !this.init_List_6().isEmpty() && !this.read_Z_8();
                        break;
                     case MINE:
                        var10000 = this.load_Str_3() != null;
                        break;
                     case PROSPECT:
                        var10000 = AiProspect.canProspect(target) && !AiOracle.isActive();
                        break;
                     case HUNT:
                        var10000 = !this.load_List_3().isEmpty();
                        break;
                     default:
                        throw new MatchException(null, null);
                  }

                  return var10000;
               }

               String init_Str_11() {
                  for (Block block : this.init_List_6()) {
                     BlockState state = block.defaultBlockState();
                     if (!this.init(state)) {
                        Item tool = AiItems.cheapestRequiredTool(state);
                        if (tool != null) {
                           String toolId = BuiltInRegistries.ITEM.getKey(tool).toString();
                           if (!visiting.contains(toolId) && !this.init_af_DataUtil.items().contains(tool)) {
                              return toolId;
                           }
                        }
                     }
                  }

                  return null;
               }

               boolean init(BlockState state) {
                  return autismclient.ai.engine.MiningCost.bestToolSlot(mc, state, true, (block, tool) -> AiItems.canDropWantedItem(mc, block, tool, this.init_af_DataUtil.items())) >= 0;
               }

               AiOperation init_AiOperation(AiObtain.Inner_DataUtil strategy) {
                  int missing = goal - AiItems.countInInventory(this.init_af_DataUtil);
                  int legSeconds = Math.max(15, this.init_I_19());
                  switch (strategy) {
                     case TOOL:
                        String toolId = this.init_Str_11();
                        if (toolId == null) {
                           return null;
                        }

                        this.load_Str = "obtain " + toolId + " to harvest " + spec;
                        Set<String> nested = new LinkedHashSet<>(visiting);

                        for (Item item : this.init_af_DataUtil.items()) {
                           nested.add(BuiltInRegistries.ITEM.getKey(item).toString());
                        }

                        return AiObtain.init(
                           AiJson.objectOf(
                              "item",
                              toolId,
                              "count",
                              AiCraft.countItem(toolId) + 1,
                              "radius",
                              radius,
                              "allowCombat",
                              allowCombat,
                              "allowMining",
                              allowMining,
                              "timeoutSeconds",
                              legSeconds
                           ),
                           depth + 1,
                           nested
                        );
                     case CRAFT:
                        AiCraftPlanner.RecipeGraph graph = AiCraft.buildRecipeGraph();
                        Item output = AiObtain.init(this.init_Set_6(), goal, graph, AiObtain::nearestSourceDistance);
                        if (output == null) {
                           return null;
                        } else {
                           String outputId = BuiltInRegistries.ITEM.getKey(output).toString();
                           AiCraftPlanner.CraftPlan plan = AiCraftPlanner.init(graph, outputId, goal);
                           if (!plan.complete() && depth < 5) {
                              Entry<String, Integer> shortfall = this.init(plan);
                              if (shortfall != null) {
                                 String want = shortfall.getKey();
                                 if (want.endsWith("_log") && AiObtain.nearestSourceDistance(want) == 1.7976931348623157E308 && !visiting.contains("log")) {
                                    want = "log";
                                 }

                                 this.load_Str = "obtain " + want + " for " + outputId;
                                 Set<String> nestedCraft = new LinkedHashSet<>(visiting);
                                 nestedCraft.add(outputId);
                                 int count = shortfall.getValue() + AiObtain.init_I_Z_Str(AiObtain.init(graph, outputId, plan), shortfall.getKey());
                                 return AiObtain.init(
                                    AiJson.objectOf(
                                       "item",
                                       want,
                                       "count",
                                       count,
                                       "additional",
                                       true,
                                       "radius",
                                       radius,
                                       "allowCombat",
                                       allowCombat,
                                       "allowMining",
                                       allowMining,
                                       "timeoutSeconds",
                                       legSeconds
                                    ),
                                    depth + 1,
                                    nestedCraft
                                 );
                              }
                           }

                           if (plan.steps().isEmpty()) {
                              return null;
                           }

                           this.load_Str = "craft " + outputId;
                           return AiCraft.craft(AiJson.objectOf("item", outputId, "count", goal, "timeoutSeconds", legSeconds));
                        }
                     case SMELT:
                        Item outputx = this.load_Set_4()
                           .stream()
                           .filter(this::init_Z)
                           .max(
                              Comparator.comparingInt(
                                 item -> autismclient.util.AutoCraftingEngine.getSmeltRecipesFor(mc, item)
                                    .stream()
                                    .flatMap(p -> p.inputs().stream())
                                    .mapToInt(i -> AiGameState.countMainInventoryItem(BuiltInRegistries.ITEM.getKey(i).toString()))
                                    .max()
                                    .orElse(0)
                              )
                           )
                           .orElse(null);
                        if (outputx == null) {
                           return null;
                        } else {
                           String outputId = BuiltInRegistries.ITEM.getKey(outputx).toString();
                           Entry<String, Integer> inputNeed = this.init_Map_Entry(outputx);
                           Set<String> nestedSmelt = new LinkedHashSet<>(visiting);
                           nestedSmelt.add(outputId);
                           if (inputNeed != null && depth < 5) {
                              String inputId = inputNeed.getKey();
                              this.load_Str = "obtain " + inputId + " to smelt into " + outputId;
                              return AiObtain.init(
                                 AiJson.objectOf(
                                    "item",
                                    inputId,
                                    "count",
                                    inputNeed.getValue(),
                                    "additional",
                                    false,
                                    "radius",
                                    radius,
                                    "allowCombat",
                                    allowCombat,
                                    "allowMining",
                                    allowMining,
                                    "timeoutSeconds",
                                    legSeconds
                                 ),
                                 depth + 1,
                                 nestedSmelt
                              );
                           } else {
                              autismclient.util.AutoCraftingEngine.SmeltRecipe cooking = autismclient.util.AutoCraftingEngine.getSmeltRecipesFor(mc, outputx)
                                 .stream()
                                 .filter(p -> p.inputs().stream().anyMatch(i -> AiGameState.countInventoryItem(BuiltInRegistries.ITEM.getKey(i).toString()) > 0))
                                 .min(Comparator.comparingInt(p -> AiSmelt.hasStationNearby(mc, p.station()) ? 0 : (AiGameState.countInventoryItem(p.station()) > 0 ? 1 : 2)))
                                 .orElse(null);
                              if (cooking != null && depth < 5) {
                                 if (!AiSmelt.hasStationNearby(mc, cooking.station()) && AiGameState.countInventoryItem(cooking.station()) == 0 && !visiting.contains(cooking.station())) {
                                    this.load_Str = "obtain " + cooking.station() + " for " + outputId;
                                    return AiObtain.init(
                                       AiJson.objectOf(
                                          "item",
                                          cooking.station(),
                                          "count",
                                          1,
                                          "staged",
                                          true,
                                          "radius",
                                          radius,
                                          "allowMining",
                                          allowMining,
                                          "allowCombat",
                                          allowCombat,
                                          "timeoutSeconds",
                                          legSeconds
                                       ),
                                       depth + 1,
                                       nestedSmelt
                                    );
                                 }

                                 Item source = cooking.inputs()
                                    .stream()
                                    .filter(i -> AiGameState.countInventoryItem(BuiltInRegistries.ITEM.getKey(i).toString()) > 0)
                                    .findFirst()
                                    .orElseThrow();
                                 Item fuel = AiSmelt.init("", source, missing);
                                 int burnDivisor = AiSmelt.burnDivisor(cooking.station());
                                 int burn = fuel == null ? 0 : AiItems.fuelBurnTime(new ItemStack(fuel)) / burnDivisor;
                                 int fuelCount = fuel == null ? 0 : AiSmelt.init(fuel);
                                 int furnaceCount = AiParallelSmelt.furnaceCountFor(missing);
                                 if (fuelCount < AiParallelSmelt.fuelNeeded(missing, furnaceCount, cooking.cookTimeTicks(), burn)) {
                                    ItemStack fuelStack = fuel == null ? ItemStack.EMPTY : new ItemStack(fuel);
                                    boolean coalNearby = AiObtain.nearestSourceDistance("minecraft:coal") != 1.7976931348623157E308
                                       && AiItems.hasUsableToolFor(Blocks.COAL_ORE.defaultBlockState());
                                    String fuelId;
                                    int needed;
                                    if (fuel == null || AiSmelt.init_I(fuelStack) != 1 || fuel != Items.COAL && fuel != Items.CHARCOAL) {
                                       if (coalNearby && allowMining && !visiting.contains("minecraft:coal")) {
                                          fuelId = "minecraft:coal";
                                          needed = AiParallelSmelt.fuelNeeded(missing, furnaceCount, cooking.cookTimeTicks(), 1600 / burnDivisor) + AiRunResources.mandatoryReservedCount(Items.COAL);
                                       } else if (AiItems.countInInventory(AiItems.resolveItem("log")) <= 0
                                          && AiItems.countInInventory(AiItems.resolveItem("planks")) <= 0
                                          && AiObtain.nearestSourceDistance("minecraft:coal") != 1.7976931348623157E308) {
                                          fuelId = "minecraft:coal";
                                          needed = AiParallelSmelt.fuelNeeded(missing, furnaceCount, cooking.cookTimeTicks(), 1600 / burnDivisor);
                                       } else {
                                          fuelId = "planks";
                                          needed = AiParallelSmelt.fuelNeeded(missing, furnaceCount, cooking.cookTimeTicks(), 300 / burnDivisor) + autismclient.ai.engine.AiRestock.reservedWood();
                                       }
                                    } else {
                                       fuelId = BuiltInRegistries.ITEM.getKey(fuel).toString();
                                       needed = AiParallelSmelt.fuelNeeded(missing, furnaceCount, cooking.cookTimeTicks(), burn) + AiRunResources.mandatoryReservedCount(fuel);
                                    }

                                    if (!visiting.contains(fuelId)) {
                                       this.load_Str = "obtain fuel " + fuelId + " for " + outputId;
                                       return AiObtain.init(
                                          AiJson.objectOf(
                                             "item",
                                             fuelId,
                                             "count",
                                             needed,
                                             "radius",
                                             radius,
                                             "allowMining",
                                             allowMining,
                                             "allowCombat",
                                             allowCombat,
                                             "timeoutSeconds",
                                             legSeconds
                                          ),
                                          depth + 1,
                                          nestedSmelt
                                       );
                                    }
                                 }
                              }

                              this.load_Str = "smelt " + outputId;

                              try {
                                 int largestInput = autismclient.util.AutoCraftingEngine.getSmeltRecipesFor(mc, outputx)
                                    .stream()
                                    .flatMap(p -> p.inputs().stream())
                                    .mapToInt(i -> AiGameState.countInventoryItem(BuiltInRegistries.ITEM.getKey(i).toString()))
                                    .max()
                                    .orElse(0);
                                 int batchGoal = Math.min(goal, AiItems.countInInventory(this.init_af_DataUtil) + largestInput);
                                 return AiSmelt.load(AiJson.objectOf("item", outputId, "count", batchGoal, "timeoutSeconds", legSeconds));
                              } catch (IllegalArgumentException e) {
                                 this.load_Str = "smelt " + outputId + " (" + e.getMessage() + ")";
                                 return null;
                              }
                           }
                        }
                     case GATHER:
                        this.load_Str = "gather " + spec;
                        return this.init(legSeconds);
                     case MINE:
                        String mineId = this.load_Str_3();
                        if (mineId == null) {
                           return null;
                        }

                        this.load_Str = "mine " + mineId + " below ground for " + spec;
                        return this.init(mineId, missing, legSeconds);
                     case PROSPECT:
                        this.load_Str = "prospect " + spec;
                        return AiProspect.init(AiJson.objectOf("item", spec, "count", goal, "timeoutSeconds", legSeconds));
                     case HUNT:
                        List<EntityType<?>> mobs = this.load_List_3();
                        if (mobs.isEmpty()) {
                           return null;
                        }

                        List<String> names = new ArrayList<>();

                        for (EntityType<?> type : mobs) {
                           names.add(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
                        }

                        this.load_Str = "hunt " + String.join(",", names);
                        JsonObject fightArgs = AiJson.objectOf(
                           "target", String.join(",", names), "count", Math.max(1, Math.min(32, missing)), "radius", radius, "timeoutSeconds", legSeconds
                        );
                        String colors = AiObtain.init(this.init_af_DataUtil.items());
                        if (!colors.isEmpty()) {
                           fightArgs.addProperty("sheepColors", colors);
                        }

                        if (AiObtain.init_Z(mobs)) {
                           fightArgs.addProperty("retreatHealth", 0);
                        }

                        if (AiObtain.init_Z(mobs)) {
                           fightArgs.addProperty("radius", Math.max(radius, 64));
                        }

                        return this.init(mobs, fightArgs, names, legSeconds);
                     default:
                        return null;
                  }
               }

               AiOperation init(List<EntityType<?>> mobs, JsonObject fightArgs, List<String> names, int legSeconds) {
                  if (this.init(mobs, 160.0) == null && AiObtain.init_Z(mobs)) {
                     this.load_Str = "look for " + String.join(",", names);
                     List<Item> items = List.copyOf(this.init_af_DataUtil.items());
                     return AiNetherRun.load(
                        AiObtain.init(e -> mobs.contains(e.getType()) && AiObtain.init_Z_Entity_Collection(e, items), String.join(",", names), legSeconds),
                        () -> this.init(mobs, fightArgs, this.init_I_19())
                     );
                  } else {
                     return this.init(mobs, fightArgs, legSeconds);
                  }
               }

               boolean init_Z_18() {
                  return AiItems.countInInventory(AiItems.resolveItem("log")) > 0 || AiItems.countInInventory(AiItems.resolveItem("planks")) >= 2 || AiObtain.nearestSourceDistance("log") <= 1024.0;
               }

               AiOperation init(List<EntityType<?>> mobs, JsonObject fightArgs, int legSeconds) {
                  Entity far = this.init(mobs, 160.0);
                  if (far != null && mc.player.distanceTo(far) > 40.0) {
                     this.load_Str = "walk to the " + BuiltInRegistries.ENTITY_TYPE.getKey(far.getType()).getPath() + " and hunt";
                     AiOperation walk = AiGoto.travelToCompat(far.blockPosition(), 8.0, true, true, true, legSeconds);
                     return this.init(AiNetherRun.sequence(walk, () -> this.init(mobs, fightArgs)));
                  } else {
                     return this.init(this.init(mobs, fightArgs));
                  }
               }

               AiOperation init(List<EntityType<?>> mobs, JsonObject fightArgs) {
                  if (AiObtain.init_Z(mobs) && AiItems.bestWeaponSlot(36) < 0 && depth < 5 && !visiting.contains("minecraft:wooden_sword") && this.init_Z_18()) {
                     this.load_Str = "a wooden sword before the hunt";
                     Set<String> nested = new LinkedHashSet<>(visiting);
                     nested.add("minecraft:wooden_sword");
                     AiOperation sword = AiObtain.init(
                        AiJson.objectOf(
                           "item",
                           "minecraft:wooden_sword",
                           "count",
                           1,
                           "radius",
                           radius,
                           "allowCombat",
                           allowCombat,
                           "allowMining",
                           allowMining,
                           "timeoutSeconds",
                           Math.min(120, this.init_I_19())
                        ),
                        depth + 1,
                        nested
                     );
                     return AiNetherRun.sequence(sword, () -> AiCombat.init(fightArgs));
                  } else {
                     return AiCombat.init(fightArgs);
                  }
               }

               Entry<String, Integer> init(AiCraftPlanner.CraftPlan plan) {
                  return AiObtain.init(plan.missing(), visiting, this.init_Set);
               }

               Entity init(List<EntityType<?>> types, double range) {
                  Entity best = null;
                  double bestSq = range * range;

                  for (Entity e : mc.level.entitiesForRendering()) {
                     if (e instanceof LivingEntity living
                        && living.isAlive()
                        && types.contains(e.getType())
                        && AiObtain.init_Z_Entity_Collection(e, this.init_af_DataUtil.items())) {
                        double d = mc.player.distanceToSqr(e);
                        if (d < bestSq) {
                           bestSq = d;
                           best = e;
                        }
                     }
                  }

                  return best;
               }

               AiOperation init(final AiOperation fight) {
                  return new AiOperation() {
                     final List<Item> init_List = new ArrayList<>(init_af_DataUtil.items());
                     AiOperation init_AiOperation = fight;
                     AiOperation.Result init_AiOperation_Result;
                     int init_I = -1;
                     boolean init_Z;

                     @Override
                     public AiOperation.Result tick() {
                        if (!this.init_Z && mc.player != null && mc.level != null) {
                           if (this.init_AiOperation != null) {
                              AiOperation.Result result = this.init_AiOperation.tick();
                              if (!result.terminal()) {
                                 return result;
                              }

                              this.init_AiOperation.cancel();
                              this.init_AiOperation = null;
                              if (this.init_I < 0) {
                                 this.init_AiOperation_Result = result;
                                 if (result.status() == AiOperation.Status.CANCELLED) {
                                    return result;
                                 }

                                 this.init_I = 0;
                              } else if (result.status() == AiOperation.Status.CANCELLED) {
                                 return result;
                              }
                           }

                           if (AiItems.countInInventory(init_af_DataUtil) >= goal) {
                              return AiGameOperations.result(
                                 AiOperation.Status.SUCCEEDED, "Hunt and collect brought " + spec + " to target", new JsonObject()
                              );
                           }

                           int missing = goal - AiItems.countInInventory(init_af_DataUtil);
                           if (this.init_I == 0 && !this.init_List.isEmpty()) {
                              this.init_I++;
                              List<String> ids = new ArrayList<>();

                              for (Item variant : this.init_List) {
                                 ids.add(BuiltInRegistries.ITEM.getKey(variant).toString());
                              }

                              String dropIds = String.join(",", ids);
                              this.init_AiOperation = AiGameplay.write(
                                 AiJson.objectOf(
                                    "item", dropIds, "count", missing, "additional", true, "timeoutSeconds", Math.max(1, Math.min(60, init_I_19()))
                                 )
                              );
                              return AiGameOperations.result(AiOperation.Status.RUNNING, "Collecting the dropped " + spec, new JsonObject());
                           } else {
                              return AiGameOperations.result(this.init_AiOperation_Result.status(), this.init_AiOperation_Result.message(), new JsonObject());
                           }
                        } else {
                           return AiGameOperations.result(AiOperation.Status.CANCELLED, "Hunt cancelled", new JsonObject());
                        }
                     }

                     @Override
                     public boolean canInterruptNow() {
                        return this.init_AiOperation == null || this.init_AiOperation.canInterruptNow();
                     }

                     @Override
                     public boolean requiresReconciliationAfterEmergency() {
                        return this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency();
                     }

                     @Override
                     public void cancel() {
                        this.init_Z = true;
                        if (this.init_AiOperation != null) {
                           this.init_AiOperation.cancel();
                           this.init_AiOperation = null;
                        }
                     }
                  };
               }

               AiOperation init(AiOperation.Result result) {
                  if (this.init_aE_DataUtil == AiObtain.Inner_DataUtil.SMELT
                     && !this.load_Z
                     && depth < 5
                     && result.dataJson().contains("\"minecraft:furnace\"")
                     && !visiting.contains("minecraft:furnace")
                     && !this.init_Set.contains("minecraft:furnace")) {
                     this.load_Z = true;
                     this.load_Str = "obtain minecraft:furnace to smelt";
                     Set<String> nested = new LinkedHashSet<>(visiting);
                     nested.addAll(this.init_Set);
                     return AiObtain.init(
                        AiJson.objectOf(
                           "item",
                           "minecraft:furnace",
                           "count",
                           1,
                           "radius",
                           radius,
                           "allowCombat",
                           allowCombat,
                           "allowMining",
                           allowMining,
                           "staged",
                           true,
                           "timeoutSeconds",
                           Math.max(15, this.init_I_19())
                        ),
                        depth + 1,
                        nested
                     );
                  }

                  if (this.init_aE_DataUtil != AiObtain.Inner_DataUtil.CRAFT || this.init_Z || depth >= 5) {
                     return null;
                  }

                  if (!AiObtain.load(result.dataJson())) {
                     return null;
                  }

                  if (!visiting.contains("minecraft:crafting_table") && !this.init_Set.contains("minecraft:crafting_table")) {
                     this.init_Z = true;
                     if (AiItems.countInInventory(AiItems.resolveItem("planks")) < 4 && AiItems.countInInventory(AiItems.resolveItem("log")) <= 0) {
                        this.load_Str = "obtain minecraft:crafting_table for the 3x3 grid";
                        Set<String> nested = new LinkedHashSet<>(visiting);
                        nested.addAll(this.init_Set);
                        return AiObtain.init(
                           AiJson.objectOf(
                              "item",
                              "minecraft:crafting_table",
                              "count",
                              1,
                              "radius",
                              radius,
                              "allowCombat",
                              allowCombat,
                              "allowMining",
                              allowMining,
                              "timeoutSeconds",
                              Math.max(15, this.init_I_19())
                           ),
                           depth + 1,
                           nested
                        );
                     } else {
                        this.load_Str = "craft a crafting table for the 3x3 grid";
                        return AiCraft.craft(
                           AiJson.objectOf(
                              "item",
                              "minecraft:crafting_table",
                              "count",
                              AiCraft.countItem("minecraft:crafting_table") + 1,
                              "timeoutSeconds",
                              Math.max(15, Math.min(30, this.init_I_19()))
                           )
                        );
                     }
                  } else {
                     return null;
                  }
               }

               String load_Str_3() {
                  List<String> ids = new ArrayList<>(this.init_List_6().size());

                  for (Block block : this.init_List_6()) {
                     ids.add(BuiltInRegistries.BLOCK.getKey(block).toString());
                  }

                  return AiObtain.init_Str(ids);
               }

               AiOperation init(int timeoutSecondsx) {
                  List<String> blocks = new ArrayList<>();

                  for (Block block : this.init_List_6()) {
                     if (this.init(block.defaultBlockState())) {
                        blocks.add(BuiltInRegistries.BLOCK.getKey(block).toString());
                     }
                  }

                  if (blocks.isEmpty()) {
                     return null;
                  }

                  boolean trees = blocks.stream().anyMatch(id -> id.endsWith("_log"));
                  return autismclient.ai.engine.AiBlockMine.create(
                     AiJson.objectOf(
                        "blocks",
                        autismclient.ai.engine.AiBlockMine.init(blocks),
                        "drops",
                        autismclient.ai.engine.AiBlockMine.init(this.init_af_DataUtil.itemIds()),
                        "held",
                        goal,
                        "trees",
                        trees,
                        "radius",
                        Math.max(48, radius),
                        "timeoutSeconds",
                        timeoutSeconds
                     )
                  );
               }

               AiOperation init(final String blockId, final int count, final int timeoutSecondsx) {
                  return !AiOreRoute.LEGIT_MINING
                     ? this.init(timeoutSeconds)
                     : new AiOperation() {
                        final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
                        final BlockPos init_BlockPos = mc.player.blockPosition();
                        final int init_I = this.init_BlockPos.getY();
                        final Block init_Block = AiCraft.requireBlock(blockId);
                        AiOperation init_AiOperation;
                        boolean init_Z;
                        int load_I;
                        boolean load_Z;

                        @Override
                        public AiOperation.Result tick() {
                           if (!this.load_Z && mc.player != null && mc.level != null) {
                              if (System.nanoTime() > this.init_J) {
                                 return AiGameOperations.result(AiOperation.Status.TIMED_OUT, "Mine deadline reached", new JsonObject());
                              }

                              if (AiItems.countInInventory(init_af_DataUtil) >= goal) {
                                 return AiGameOperations.result(AiOperation.Status.SUCCEEDED, "Mined " + blockId + " to target", new JsonObject());
                              }

                              if (this.init_AiOperation != null) {
                                 AiOperation.Result result = this.init_AiOperation.tick();
                                 if (!result.terminal()) {
                                    return result;
                                 }

                                 this.init_AiOperation.cancel();
                                 this.init_AiOperation = null;
                                 if (result.status() == AiOperation.Status.CANCELLED) {
                                    return result;
                                 }

                                 if (this.init_Z) {
                                    return result;
                                 }

                                 if (result.status() != AiOperation.Status.SUCCEEDED && ++this.load_I >= 2) {
                                    this.init_Z = true;
                                 }
                              }

                              int currentY = mc.player.blockPosition().getY();
                              boolean found = this.init_Z || this.init();
                              Integer nextY = AiObtain.init(this.init_I, currentY, mc.level.getMinY(), found);
                              if (found) {
                                 this.init_Z = true;
                                 this.init_AiOperation = autismclient.ai.engine.AiMine.init(
                                    AiJson.objectOf("block", blockId, "count", count, "radius", radius, "timeoutSeconds", init_I_19())
                                 );
                                 return AiGameOperations.result(AiOperation.Status.RUNNING, "Mining " + blockId, new JsonObject());
                              }

                              if (nextY == null) {
                                 return AiGameOperations.result(
                                    AiOperation.Status.FAILED,
                                    "No " + blockId + " in the scan band after descending " + (this.init_I - currentY) + " to Y=" + currentY,
                                    new JsonObject()
                                 );
                              }

                              this.init_AiOperation = AiGoto.travelToCompat(
                                 new BlockPos(this.init_BlockPos.getX(), nextY, this.init_BlockPos.getZ()), 2.5, true, true, true, AiObtain.init(init_I_19())
                              );
                              return AiGameOperations.result(AiOperation.Status.RUNNING, "Descending to Y=" + nextY + " toward " + blockId, new JsonObject());
                           } else {
                              return AiGameOperations.result(AiOperation.Status.CANCELLED, "Mine cancelled", new JsonObject());
                           }
                        }

                        boolean init() {
                           return AiTerrain.nearestBlockDistSqr(mc, List.of(this.init_Block), Math.min(radius, 96)) != 1.7976931348623157E308;
                        }

                        @Override
                        public boolean canInterruptNow() {
                           return this.init_AiOperation == null || this.init_AiOperation.canInterruptNow();
                        }

                        @Override
                        public boolean requiresReconciliationAfterEmergency() {
                           return this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency();
                        }

                        @Override
                        public void cancel() {
                           this.load_Z = true;
                           if (this.init_AiOperation != null) {
                              this.init_AiOperation.cancel();
                              this.init_AiOperation = null;
                           }
                        }
                     };
               }

               Set<Item> init_Set_6() {
                  if (this.load_Set == null) {
                     this.load_Set = this.init(true);
                  }

                  return this.load_Set;
               }

               Set<Item> load_Set_4() {
                  if (this.read_Set == null) {
                     this.read_Set = this.init(false);
                  }

                  return this.read_Set;
               }

               Set<Item> init(boolean crafting) {
                  Set<Item> out = new LinkedHashSet<>();

                  for (Item item : this.init_af_DataUtil.items()) {
                     if (!visiting.contains(BuiltInRegistries.ITEM.getKey(item).toString())) {
                        boolean known = crafting ? !autismclient.util.AutoCraftingEngine.getCraftingRecipesFor(mc, item).isEmpty() : !autismclient.util.AutoCraftingEngine.getSmeltRecipesFor(mc, item).isEmpty();
                        if (known) {
                           out.add(item);
                        }
                     }
                  }

                  return out;
               }

               boolean init_Z_4(String itemId) {
                  return itemId.startsWith("minecraft:raw_") || !itemId.endsWith("_ore");
               }

               boolean load_Z_12() {
                  return this.load_Set_4().stream().anyMatch(this::init_Z);
               }

               boolean init_Z(Item output) {
                  Entry<String, Integer> need = this.init_Map_Entry(output);
                  return need == null || this.init_Z_4(need.getKey());
               }

               Entry<String, Integer> init_Map_Entry(Item output) {
                  String best = null;
                  int bestCount = -1;

                  for (autismclient.util.AutoCraftingEngine.SmeltRecipe plan : autismclient.util.AutoCraftingEngine.getSmeltRecipesFor(mc, output)) {
                     if (plan.inputs().stream().mapToInt(i -> AiGameState.countInventoryItem(BuiltInRegistries.ITEM.getKey(i).toString())).sum()
                        >= goal - AiItems.countInInventory(this.init_af_DataUtil)) {
                        return null;
                     }

                     for (Item input : plan.inputs()) {
                        String inputId = BuiltInRegistries.ITEM.getKey(input).toString();
                        int count = AiGameState.countInventoryItem(inputId);
                        if (count >= goal - AiItems.countInInventory(this.init_af_DataUtil)) {
                           return null;
                        }

                        if (!visiting.contains(inputId)
                           && (count > bestCount || count == bestCount && best != null && !this.init_Z_4(best) && this.init_Z_4(inputId))) {
                           bestCount = count;
                           best = inputId;
                        }
                     }
                  }

                  return best == null ? null : Map.entry(best, goal - AiItems.countInInventory(this.init_af_DataUtil));
               }

               boolean read_Z_8() {
                  if (!AiOreRoute.LEGIT_MINING) {
                     return false;
                  }

                  if (this.write_Z || this.init_List.contains(AiObtain.Inner_DataUtil.MINE) || this.load_Str_3() == null) {
                     return false;
                  }

                  if (!AiObtain.init(false, false, true, this.write_Z_6())) {
                     return false;
                  }

                  this.write_Z = true;
                  this.init_JsonArray
                     .add(
                        AiJson.objectOf(
                           "strategy",
                           "gather",
                           "status",
                           "SKIPPED",
                           "detail",
                           "gather " + spec,
                           "message",
                           "no source blocks within 32 in loaded terrain; trying buried mining first"
                        )
                     );
                  return true;
               }

               boolean write_Z_6() {
                  if (this.init_Boolean == null) {
                     this.init_Boolean = AiTerrain.nearestBlockDistSqr(mc, this.init_List_6(), 32) != 1.7976931348623157E308;
                  }

                  return this.init_Boolean;
               }

               String read_Str_3() {
                  return target.items().size() != 1
                     ? null
                     : AiObtain.init_Str_Str_Function(BuiltInRegistries.ITEM.getKey(target.items().iterator().next()).toString(), AiObtain::init_Set);
               }

               String write_Str_2() {
                  if (this.write_Str == null) {
                     return spec;
                  }

                  String bestId = spec;
                  int bestCount = -1;

                  for (Item item : this.init_af_DataUtil.items()) {
                     String id = BuiltInRegistries.ITEM.getKey(item).toString();
                     int count = AiGameState.countInventoryItem(id);
                     if (count > bestCount) {
                        bestCount = count;
                        bestId = id;
                     }
                  }

                  return AiObtain.init(spec, this.write_Str, bestId);
               }

               List<Block> init_List_6() {
                  if (this.load_List != null) {
                     return this.load_List;
                  }

                  List<Block> blocks = new ArrayList<>(this.init_af_DataUtil.blocks());

                  for (Item item : this.init_af_DataUtil.items()) {
                     for (Block block : AiItems.sourceBlocksFor(item)) {
                        if (!blocks.contains(block)) {
                           blocks.add(block);
                        }
                     }
                  }

                  blocks.removeIf(
                     blockx -> this.init_af_DataUtil
                        .items()
                        .stream()
                        .noneMatch(itemx -> AiItems.sourceBlocksFor(itemx).contains(blockx) && AiObtain.init_Z_Item_Block(itemx, blockx))
                  );
                  blocks.removeIf(AiObtain::init);
                  this.load_List = List.copyOf(blocks);
                  return this.load_List;
               }

               List<EntityType<?>> load_List_3() {
                  if (this.read_List != null) {
                     return this.read_List;
                  }

                  List<EntityType<?>> mobs = new ArrayList<>();

                  for (Item item : this.init_af_DataUtil.items()) {
                     for (EntityType<?> type : AiItems.sourceEntitiesFor(item)) {
                        if (!mobs.contains(type)) {
                           mobs.add(type);
                        }
                     }
                  }

                  this.read_List = List.copyOf(mobs);
                  return this.read_List;
               }

               int init_I_19() {
                  return (int)Math.max(1L, (this.init_J - System.nanoTime()) / 1000000000L);
               }

               JsonObject init_JsonObject_32() {
                  JsonObject out = AiJson.objectOf(
                     "item", spec, "goal", goal, "held", AiItems.countInInventory(this.init_af_DataUtil), "startedWith", held, "depth", depth
                  );
                  out.add("matches", AiJson.GSON.toJsonTree(this.init_af_DataUtil.itemIds()));
                  if (this.write_Str != null) {
                     out.addProperty("familyFallbackFrom", spec);
                  }

                  out.add("attempts", this.init_JsonArray);
                  if (this.init_aE_DataUtil != null) {
                     out.addProperty("strategy", this.init_aE_DataUtil.name().toLowerCase(Locale.ROOT));
                     out.addProperty("strategyDetail", this.load_Str);
                  }

                  JsonArray spent = new JsonArray();

                  for (AiObtain.Inner_DataUtil strategy : this.init_List) {
                     spent.add(strategy.name().toLowerCase(Locale.ROOT));
                  }

                  out.add("exhausted", spent);
                  out.add("survival", AiGuard.statusToJson(AiGuard.sampleStatus(), this.init_I, this.init_Str));
                  return out;
               }

               AiOperation.Result init_AiOperation_Result_9(String message) {
                  if (this.load_af_DataUtil != this.init_af_DataUtil) {
                     this.load_af_DataUtil = this.init_af_DataUtil;
                     String label = this.init_af_DataUtil.items().size() == 1
                        ? this.init_af_DataUtil.items().iterator().next().getDefaultInstance().getHoverName().getString()
                        : this.write_Str_2().replace("minecraft:", "").replace('_', ' ');
                     this.read_Str = "Getting " + label;
                  }

                  AiVisualization.init(this, this.read_Str, AiTaskStatus.init(message, "Preparing materials"), AiItems.countInInventory(this.init_af_DataUtil), goal);
                  return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_32().toString(), List.of());
               }

               AiOperation.Result init(AiOperation.Status status, String message) {
                  JsonObject data = this.init_JsonObject_32();
                  this.cancel();
                  return new AiOperation.Result(status, message, data.toString(), List.of());
               }

               @Override
               public boolean canInterruptNow() {
                  return (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow())
                     && (this.load_AiOperation == null || this.load_AiOperation.canInterruptNow());
               }

               @Override
               public boolean requiresReconciliationAfterEmergency() {
                  return this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency()
                     || this.load_AiOperation != null && this.load_AiOperation.requiresReconciliationAfterEmergency();
               }

               @Override
               public void cancel() {
                  if (!this.sync) {
                     this.sync = true;
                     AiVisualization.clearOwner(this);
                     if (this.load_AiOperation != null) {
                        this.load_AiOperation.cancel();
                        this.load_AiOperation = null;
                     }

                     if (this.init_AiOperation != null) {
                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                     }
                  }
               }
            };
         } else {
            throw new IllegalArgumentException("count must be between 1 and 2304");
         }
      } else {
         throw new IllegalArgumentException("Obtaining items requires a connected living player");
      }
   }

   static boolean init(Block block) {
      BlockState state = block.defaultBlockState();
      String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
      return !path.endsWith("_wool") && (!path.endsWith("_carpet") || path.equals("moss_carpet") || path.equals("pale_moss_carpet")) && !path.endsWith("_bed")
         ? state.is(BlockTags.PLANKS)
            || state.is(BlockTags.WOODEN_SLABS)
            || state.is(BlockTags.WOODEN_STAIRS)
            || state.is(BlockTags.WOODEN_FENCES)
            || state.is(BlockTags.WOODEN_DOORS)
            || state.is(BlockTags.WOODEN_TRAPDOORS)
         : true;
   }

   static boolean init_Z_Item_Block(Item item, Block block) {
      if (item instanceof BlockItem form && form.getBlock() == block && !init().contains(BuiltInRegistries.ITEM.getKey(item).toString())) {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.player != null) {
            for (int slot = 0; slot < 36; slot++) {
               ItemStack stack = mc.player.getInventory().getItem(slot);
               if (stack.isCorrectToolForDrops(block.defaultBlockState()) && AiItems.hasEnoughDurability(stack, 2) && BlockBreakSpeedHelper.hasSilkTouch(mc, stack)) {
                  return true;
               }
            }

            return false;
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   static Item init(Set<Item> craftable, int goal, AiCraftPlanner.RecipeGraph graph, ToDoubleFunction<String> missingSourceDistance) {
      Item best = null;
      String bestId = null;
      int bestMissing = 2147483647;
      int bestHeldInputs = -1;
      double bestDistance = -1.0;
      AiCraftPlanner.CraftPlan bestPlan = null;

      for (Item item : craftable) {
         if (item != null) {
            String id = BuiltInRegistries.ITEM.getKey(item).toString();
            AiCraftPlanner.CraftPlan plan = AiCraftPlanner.init(graph, id, goal);
            int missing = 0;

            for (int amount : plan.missing().values()) {
               missing += amount;
            }

            int heldInputs = init(plan, id, graph);
            double distance = -1.0;
            boolean replace;
            if (best == null) {
               replace = true;
            } else if (missing != bestMissing) {
               replace = missing < bestMissing;
            } else if (heldInputs != bestHeldInputs) {
               replace = heldInputs > bestHeldInputs;
            } else {
               distance = init_D_g_CachePool_ToDoubleFunction(plan, missingSourceDistance);
               if (bestDistance < 0.0) {
                  bestDistance = init_D_g_CachePool_ToDoubleFunction(bestPlan, missingSourceDistance);
               }

               replace = distance < bestDistance || distance == bestDistance && (init_Z(id) != init_Z(bestId) ? init_Z(id) : id.compareTo(bestId) < 0);
            }

            if (replace) {
               best = item;
               bestId = id;
               bestMissing = missing;
               bestHeldInputs = heldInputs;
               bestPlan = plan;
               bestDistance = distance;
            }
         }
      }

      return best;
   }

   static boolean init_Z(String id) {
      String path = id.contains(":") ? id.substring(id.indexOf(58) + 1) : id;
      return path.startsWith("white_") || path.startsWith("oak_");
   }

   static String init(Collection<Item> items) {
      List<String> colors = new ArrayList<>();

      for (Item item : items) {
         String path = BuiltInRegistries.ITEM.getKey(item).getPath();
         if (path.endsWith("_wool")) {
            colors.add(path.substring(0, path.length() - "_wool".length()));
         }
      }

      return String.join(",", colors);
   }

   static boolean init_Z_Entity_Collection(Entity entity, Collection<Item> items) {
      if (!(entity instanceof Sheep sheep)) {
         return true;
      } else {
         String colors = init(items);
         return colors.isEmpty() ? true : !sheep.isSheared() && List.of(colors.split(",")).contains(sheep.getColor().getSerializedName());
      }
   }

   static boolean init_Z(List<EntityType<?>> mobs) {
      if (mobs.isEmpty()) {
         return false;
      }

      for (EntityType<?> type : mobs) {
         if (type.getCategory() != MobCategory.CREATURE) {
            return false;
         }
      }

      return true;
   }

   static AiOperation init(final Predicate<Entity> wanted, final String what, final int seconds) {
      final Minecraft mc = Minecraft.getInstance();
      return new AiOperation() {
         final long init_J = System.nanoTime() + Math.max(15, seconds) * 1000000000L;
         final BlockPos init_BlockPos = mc.player.blockPosition();
         final Object init_Obj = mc.level;
         AiOperation init_AiOperation;
         int init_I;
         int load;
         boolean init_Z;

         boolean init() {
            for (Entity e : mc.level.entitiesForRendering()) {
               if (e instanceof LivingEntity living && living.isAlive() && wanted.test(e) && mc.player.distanceToSqr(e) <= 25600.0) {
                  return true;
               }
            }

            return false;
         }

         AiOperation.Result init(AiOperation.Status status, String message) {
            return AiGameOperations.result(status, message, AiJson.objectOf("looking", what, "legs", this.init_I));
         }

         @Override
         public AiOperation.Result tick() {
            if (this.init_Z || mc.player == null || mc.level != this.init_Obj) {
               return this.init(AiOperation.Status.CANCELLED, "Search cancelled or world changed");
            }

            if (!mc.player.isAlive()) {
               return this.init(AiOperation.Status.FAILED, "Player died while looking for " + what);
            }

            if (this.init_AiOperation != null) {
               if (this.init_AiOperation.canInterruptNow() && !this.init_AiOperation.requiresReconciliationAfterEmergency() && this.init()) {
                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
                  return this.init(AiOperation.Status.SUCCEEDED, "Found " + what);
               }

               AiOperation.Result r = this.init_AiOperation.tick();
               if (!r.terminal()) {
                  return this.init(AiOperation.Status.RUNNING, "Looking for " + what + ": " + r.message());
               }

               this.init_AiOperation.cancel();
               this.init_AiOperation = null;
            }

            if (this.init()) {
               return this.init(AiOperation.Status.SUCCEEDED, "Found " + what);
            }

            long left = (this.init_J - System.nanoTime()) / 1000000000L;
            if (left <= 0L || this.init_I >= 8) {
               return this.init(AiOperation.Status.FAILED, "No " + what + " found in " + this.init_I + " legs");
            }

            if (!AiSpeedrun.init_Z_Minecraft_I(mc, 5) && this.load < 2) {
               this.load++;
               this.init_AiOperation = autismclient.ai.engine.AiSeek.init(AiJson.objectOf("timeoutSeconds", (int)Math.min(120L, left)));
               return this.init(AiOperation.Status.RUNNING, "Up to open sky to look for " + what);
            }

            double a = this.init_I++ * 2.399;
            int x = this.init_BlockPos.getX() + (int)Math.round(Math.cos(a) * 64.0);
            int z = this.init_BlockPos.getZ() + (int)Math.round(Math.sin(a) * 64.0);
            BlockPos surface = AiSpeedrun.findSurface(mc.player.blockPosition(), x, z, column -> AiSpeedrun.observedSurface(mc, column));
            if (surface == null) {
               return this.init(AiOperation.Status.RUNNING, "No safe ground that way; trying another bearing");
            }

            this.init_AiOperation = AiGoto.travelToCompat(
               surface,
               2.0,
               false,
               false,
               false,
               (int)Math.min(60L, left),
               autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST,
               true,
               2.0,
               AiSpeedrun.surfaceGoal(surface),
               AiGoto.GotoOptions.DEFAULT
            );
            return this.init(AiOperation.Status.RUNNING, "Looking for " + what);
         }

         @Override
         public boolean canInterruptNow() {
            return this.init_AiOperation == null || this.init_AiOperation.canInterruptNow();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            this.init_Z = true;
            if (this.init_AiOperation != null) {
               this.init_AiOperation.cancel();
            }
         }
      };
   }

   private static double init_D_g_CachePool_ToDoubleFunction(AiCraftPlanner.CraftPlan plan, ToDoubleFunction<String> missingSourceDistance) {
      double best = 1.7976931348623157E308;

      for (String missingId : plan.missing().keySet()) {
         best = Math.min(best, missingSourceDistance.applyAsDouble(missingId));
      }

      return best;
   }

   static double nearestSourceDistance(String itemId) {
      Minecraft mc = Minecraft.getInstance();
      AiItems.ItemSpec target = AiItems.resolveItem(itemId);
      List<Block> sources = new ArrayList<>(target.blocks());

      for (Item item : target.items()) {
         for (Block block : AiItems.sourceBlocksFor(item)) {
            if (!sources.contains(block)) {
               sources.add(block);
            }
         }
      }

      sources.removeIf(AiObtain::init);
      double best = sources.isEmpty() ? 1.7976931348623157E308 : AiTerrain.nearestBlockDistSqr(mc, sources, 32);
      List<EntityType<?>> mobs = new ArrayList<>();

      for (Item item : target.items()) {
         for (EntityType<?> type : AiItems.sourceEntitiesFor(item)) {
            if (!mobs.contains(type)) {
               mobs.add(type);
            }
         }
      }

      if (!mobs.isEmpty() && mc.player != null && mc.level != null) {
         for (Entity e : mc.level.entitiesForRendering()) {
            if (e instanceof LivingEntity living && living.isAlive() && mobs.contains(e.getType()) && init_Z_Entity_Collection(e, target.items())) {
               double d = mc.player.distanceToSqr(e);
               if (d <= 25600.0) {
                  best = Math.min(best, d);
               }
            }
         }
      }

      return best;
   }

   private static int init(AiCraftPlanner.CraftPlan plan, String outputId, AiCraftPlanner.RecipeGraph graph) {
      int held = 0;

      for (AiCraftPlanner.CraftStep step : plan.steps()) {
         if (step.recipe().output().equals(outputId)) {
            for (List<String> slot : step.recipe().inputs()) {
               int slotHeld = 0;

               for (String accepted : slot) {
                  slotHeld = Math.max(slotHeld, Math.max(0, graph.held(accepted)));
               }

               held += Math.min(slotHeld, step.crafts());
            }
         }
      }

      return held;
   }

   static Entry<String, Integer> init(Map<String, Integer> missing, Set<String> visiting, Set<String> ownItemIds) {
      for (Entry<String, Integer> entry : missing.entrySet()) {
         if (!visiting.contains(entry.getKey()) && !ownItemIds.contains(entry.getKey())) {
            return entry;
         }
      }

      return null;
   }

   static String init(boolean craftKnown, boolean smeltKnown, boolean hasSourceBlocks, boolean prospectable, boolean hasMobDrops) {
      List<String> reasons = new ArrayList<>();
      if (!craftKnown) {
         reasons.add("no known recipe");
      }

      if (!smeltKnown) {
         reasons.add("no known smelting recipe");
      }

      if (!hasSourceBlocks) {
         reasons.add("no source blocks");
      }

      if (!prospectable) {
         reasons.add("not prospectable");
      }

      if (!hasMobDrops) {
         reasons.add("no mob drops");
      }

      return "no strategy applies (" + String.join("; ", reasons) + ")";
   }

   static String init_Str(List<String> sourceBlockIds) {
      for (String id : sourceBlockIds) {
         if (init_Set.contains(id)) {
            return id;
         }
      }

      return null;
   }

   static Integer init(int startY, int currentY, int minY, boolean sourceInBand) {
      if (sourceInBand) {
         return null;
      }

      if (startY - currentY >= 32) {
         return null;
      }

      int next = Math.max(minY + 4, Math.max(startY - 32, currentY - 8));
      return next < currentY ? next : null;
   }

   static int init(int remainingSeconds) {
      return Math.max(10, Math.min(45, remainingSeconds / 4));
   }

   static String init_Str(String dataJson) {
      if (dataJson != null && !dataJson.isBlank()) {
         try {
            JsonObject data = AiJson.parseObject(dataJson);
            if (data.has("missing") && data.get("missing").isJsonObject()) {
               for (Entry<String, JsonElement> entry : data.getAsJsonObject("missing").entrySet()) {
                  if (read(entry.getKey())) {
                     return entry.getKey();
                  }
               }

               return null;
            } else {
               return null;
            }
         } catch (RuntimeException e) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static boolean read(String id) {
      return id != null && (id.endsWith("_pickaxe") || id.endsWith("_shovel") || id.endsWith("_axe") || id.endsWith("_hoe") || id.endsWith("shears"));
   }

   static int init_I_Z_Str(boolean needsTable, String item) {
      if (needsTable && !AiCraft.isCraftingTableInReach() && AiCraft.countItem("minecraft:crafting_table") <= 0) {
         String path = item.substring(item.indexOf(58) + 1);
         if (!path.endsWith("_planks") && !path.equals("planks")) {
            return !path.endsWith("_log") && !path.endsWith("_wood") && !path.endsWith("_stem") && !path.endsWith("_hyphae") && !path.equals("log") ? 0 : 1;
         } else {
            return 4;
         }
      } else {
         return 0;
      }
   }

   static boolean init(AiCraftPlanner.RecipeGraph graph, String outputId, AiCraftPlanner.CraftPlan plan) {
      if (plan.requiresTable()) {
         return true;
      }

      List<AiCraftPlanner.RecipeSpec> recipes = graph.recipesFor(outputId);
      return !recipes.isEmpty() && recipes.get(0).requiresTable();
   }

   static boolean load(String dataJson) {
      if (dataJson != null && !dataJson.isBlank()) {
         try {
            JsonObject data = AiJson.parseObject(dataJson);
            return data.has("missing") && data.get("missing").isJsonObject() && data.getAsJsonObject("missing").has("minecraft:crafting_table");
         } catch (RuntimeException e) {
            return false;
         }
      } else {
         return false;
      }
   }

   static boolean init(boolean alreadyDeferred, boolean mineTried, boolean hasMineRoute, boolean sourceNearby) {
      return !alreadyDeferred && !mineTried && hasMineRoute && !sourceNearby;
   }

   static int init(String itemId, Set<String> directlyMineable, boolean craftable, boolean smeltable) {
      if (directlyMineable.contains(itemId)) {
         return 0;
      } else {
         return !craftable && !smeltable ? 2 : 1;
      }
   }

   static Set<String> init() {
      return AiItems.directMineableItems();
   }

   static AiCraftPlanner.RecipeGraph init(final AiCraftPlanner.RecipeGraph inner) {
      final Minecraft mc = Minecraft.getInstance();
      final Set<String> mineable = init();
      final Map<String, Double> distances = new HashMap<>();
      return new AiCraftPlanner.RecipeGraph() {
         @Override
         public List<AiCraftPlanner.RecipeSpec> recipesFor(String itemId) {
            return inner.recipesFor(itemId);
         }

         @Override
         public int held(String itemId) {
            return inner.held(itemId);
         }

         @Override
         public int load_I(String itemId) {
            boolean craftable = !inner.recipesFor(itemId).isEmpty();
            boolean smeltable = BuiltInRegistries.ITEM
               .getOptional(Identifier.parse(itemId))
               .map(item -> !autismclient.util.AutoCraftingEngine.getSmeltRecipesFor(mc, item).isEmpty())
               .orElse(false);
            return AiObtain.init(itemId, mineable, craftable, smeltable);
         }

         @Override
         public String init(Set<String> variantIds) {
            return AiObtain.init_Str_Collection_Function(variantIds, AiObtain::init_Set);
         }

         @Override
         public boolean init_Z(String itemId) {
            return BuiltInRegistries.ITEM.getOptional(Identifier.parse(itemId)).map(item -> AiObtain.init_Z(AiItems.sourceEntitiesFor(item))).orElse(false);
         }

         @Override
         public double init_D(String itemId) {
            return mc != null && mc.player != null && mc.level != null ? distances.computeIfAbsent(itemId, id -> {
               double distance = AiObtain.nearestSourceDistance(id);
               return distance == 1.7976931348623157E308 ? 256.0 : 1.0 + Math.sqrt(distance);
            }) : inner.init_D(itemId);
         }
      };
   }

   static String init_Str_Collection_Function(Collection<String> variantIds, Function<String, Set<String>> resolver) {
      if (variantIds != null && variantIds.size() >= 2) {
         Set<String> wanted = new HashSet<>(variantIds);
         Set<String> candidates = new LinkedHashSet<>();

         for (String id : variantIds) {
            String path = id.contains(":") ? id.substring(id.indexOf(58) + 1) : id;
            candidates.add(path);
            int underscore = path.indexOf(95);
            if (underscore >= 0 && underscore + 1 < path.length()) {
               candidates.add(path.substring(underscore + 1));
            }
         }

         List<String> ordered = new ArrayList<>(candidates);
         ordered.sort(String.CASE_INSENSITIVE_ORDER);

         for (String candidate : ordered) {
            Set<String> resolved = resolver.apply(candidate);
            if (resolved != null && resolved.equals(wanted)) {
               return candidate;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   static Set<String> init_Set(String spec) {
      try {
         Set<String> ids = new HashSet<>();

         for (Item item : AiItems.resolve(spec).items()) {
            ids.add(BuiltInRegistries.ITEM.getKey(item).toString());
         }

         return ids.isEmpty() ? null : ids;
      } catch (RuntimeException e) {
         return null;
      }
   }

   static String init_Str_Str_Function(String itemId, Function<String, Set<String>> resolver) {
      if (itemId != null && resolver != null) {
         String path = itemId.contains(":") ? itemId.substring(itemId.indexOf(58) + 1) : itemId;
         List<String> candidates = new ArrayList<>();
         if (path.endsWith("_ore")) {
            candidates.add(path.substring(0, path.length() - 4));
         }

         int underscore = path.indexOf(95);
         if (underscore >= 0 && underscore + 1 < path.length()) {
            String tail = path.substring(underscore + 1);
            candidates.add(tail);
            if (tail.endsWith("_ore")) {
               candidates.add(tail.substring(0, tail.length() - 4));
            }
         }

         candidates.add(path);

         for (String candidate : candidates) {
            if (!candidate.isEmpty() && !"ore".equals(candidate)) {
               Set<String> resolved = resolver.apply(candidate);
               if (resolved != null && resolved.size() > 1 && resolved.contains(itemId)) {
                  return candidate;
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   static boolean init(AiOperation.Result result) {
      if (result != null && result.status() == AiOperation.Status.FAILED) {
         String message = result.message();
         return message != null && message.startsWith("No more reachable sources") && message.contains("(collected 0 of");
      } else {
         return false;
      }
   }

   static String init(String spec, String widenedFamily, String obtainedId) {
      return widenedFamily == null ? spec : obtainedId + " (family fallback from " + spec + ")";
   }

   static JsonObject init(String strategy, String detail, AiOperation.Result result) {
      JsonObject entry = AiJson.objectOf(
         "strategy", strategy.toLowerCase(Locale.ROOT), "detail", detail, "status", result.status().name(), "message", result.message()
      );

      try {
         JsonElement parsed = JsonParser.parseString(result.dataJson());
         if (parsed.isJsonObject() && !parsed.getAsJsonObject().keySet().isEmpty()) {
            entry.add("data", parsed);
         }
      } catch (RuntimeException var5) {
      }

      return entry;
   }

   enum Inner_DataUtil {
      INVENTORY,
      TOOL,
      CRAFT,
      SMELT,
      GATHER,
      MINE,
      PROSPECT,
      HUNT;
   }
}
