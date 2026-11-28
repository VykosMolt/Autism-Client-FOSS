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
   private static final Set<String> commonTerrainBlockIds = Set.of(
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

   static AiOperation obtain(JsonObject args) {
      return obtainAtDepth(args, 0, new LinkedHashSet<>());
   }

   static AiOperation obtainAtDepth(JsonObject args, final int depth, final Set<String> visiting) {
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

            if (!equippedObsidian && AiProgression.needsToolTier(only, want)) {
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
               final long deadlineNanos = System.nanoTime() + timeoutSeconds * 1000000000L;
               final Object startLevel = mc.level;
               final Object startConnection = mc.getConnection();
               final LocalPlayer startPlayer = mc.player;
               final List<AiObtain.ObtainStrategy> triedStrategies = new ArrayList<>();
               final JsonArray attemptLog = new JsonArray();
               final Set<String> targetItemIds = new LinkedHashSet<>(target.itemIds());
               final AiGuard.GuardPolicy guardPolicy = AiGuard.GuardPolicy.standard().keeping(this.targetItemIds);
               AiOperation strategyOp;
               AiOperation survivalOp;
               String survivalReason = "";
               int survivalInterruptions;
               AiObtain.ObtainStrategy currentStrategy;
               String strategyDetail = "";
               Set<Item> craftableItemsCache;
               Set<Item> smeltableItemsCache;
               List<Block> sourceBlocksCache;
               List<EntityType<?>> sourceMobsCache;
               boolean init_Z;
               boolean furnaceDetourTried;
               int craftMissingRetries;
               boolean read_Z;
               boolean mineDeferred;
               Boolean sourceNearbyCache;
               AiItems.ItemSpec init_af_DataUtil = target;
               AiItems.ItemSpec labelledSpec;
               String visualTitle;
               boolean familyFallbackUsed;
               String fallbackFamily;
               boolean cancelled;
               int countAtStrategyStart;
               final EnumMap<AiObtain.ObtainStrategy, Integer> progressRepeats = new EnumMap<>(AiObtain.ObtainStrategy.class);

               @Override
               public AiOperation.Result tick() {
                  if (this.cancelled || mc.player == null || mc.level != this.startLevel || mc.getConnection() != this.startConnection || mc.player != this.startPlayer) {
                     return this.finish(AiOperation.Status.CANCELLED, "Obtain cancelled or world/player changed");
                  }

                  if (!mc.player.isAlive()) {
                     return this.finish(AiOperation.Status.FAILED, "Player died while obtaining supplies");
                  }

                  if (System.nanoTime() > this.deadlineNanos) {
                     return this.finish(
                        this.canInterruptNow() ? AiOperation.Status.TIMED_OUT : AiOperation.Status.UNCERTAIN,
                        "Obtain deadline reached with " + AiItems.countInInventory(this.init_af_DataUtil) + "/" + goal
                     );
                  }

                  if (AiItems.countInInventory(this.init_af_DataUtil) >= goal && this.strategyOp == null) {
                     if (this.strategyOp != null) {
                        this.strategyOp.cancel();
                        this.strategyOp = null;
                        this.currentStrategy = null;
                     }

                     return this.finish(AiOperation.Status.SUCCEEDED, "Obtained " + goal + " " + this.obtainedLabel());
                  } else {
                     AiOperation.Result guarded = this.tickSurvivalGuard();
                     if (guarded != null) {
                        return guarded;
                     }

                     if (this.strategyOp == null) {
                        AiObtain.ObtainStrategy next = this.nextStrategy();
                        if (next == null) {
                           String why = this.triedStrategies.isEmpty()
                              ? AiObtain.unavailableReason(
                                 !this.craftableItems().isEmpty(),
                                 !this.smeltableItems().isEmpty(),
                                 !this.sourceBlocks().isEmpty(),
                                 AiProspect.canProspect(target),
                                 !this.sourceMobs().isEmpty()
                              )
                              : "every strategy was tried";
                           return this.finish(
                              AiOperation.Status.FAILED, "No remaining route to " + spec + " (" + AiItems.countInInventory(this.init_af_DataUtil) + "/" + goal + "); " + why
                           );
                        } else {
                           this.strategyOp = AiRunDiagnostics.trace(
                              "obtain", next.name() + " " + spec + " " + AiItems.countInInventory(this.init_af_DataUtil) + "/" + goal, this.operationFor(next)
                           );
                           if (this.strategyOp == null) {
                              this.triedStrategies.add(next);
                              return this.running("Strategy " + next + " unavailable; trying the next");
                           } else {
                              this.currentStrategy = next;
                              this.countAtStrategyStart = AiItems.countInInventory(this.init_af_DataUtil);
                              return this.running("Starting " + next.name().toLowerCase(Locale.ROOT) + " for " + spec);
                           }
                        }
                     } else {
                        AiOperation.Result result = this.strategyOp.tick();
                        if (!result.terminal()) {
                           return this.running(result.message());
                        }

                        this.strategyOp.cancel();
                        this.strategyOp = null;
                        this.attemptLog.add(AiObtain.attemptEntry(this.currentStrategy.name(), this.strategyDetail, result));
                        if (result.status() == AiOperation.Status.CANCELLED) {
                           return this.finish(AiOperation.Status.CANCELLED, "Obtain cancelled");
                        }

                        if (result.status() == AiOperation.Status.UNCERTAIN) {
                           return this.finish(AiOperation.Status.UNCERTAIN, result.message());
                        }

                        if (AiItems.countInInventory(this.init_af_DataUtil) >= goal) {
                           return this.finish(AiOperation.Status.SUCCEEDED, "Obtained " + goal + " " + this.obtainedLabel());
                        }

                        if (result.status() != AiOperation.Status.SUCCEEDED) {
                           AiOperation detour = this.recoveryOperationFor(result);
                           if (detour != null) {
                              this.strategyOp = AiRunDiagnostics.trace("obtain", "station detour for " + spec, detour);
                              return this.running(
                                 this.furnaceDetourTried && this.currentStrategy == AiObtain.ObtainStrategy.SMELT
                                    ? "Smelting needs a furnace; making one first"
                                    : "Craft needs a crafting table; obtaining one first"
                              );
                           }

                           if ((this.currentStrategy == AiObtain.ObtainStrategy.MINE || this.currentStrategy == AiObtain.ObtainStrategy.GATHER)
                              && !this.read_Z
                              && AiObtain.missingToolId(result.dataJson()) != null) {
                              this.read_Z = true;
                              this.currentStrategy = null;
                              return this.running("Mining tool broke; re-tooling before one more attempt");
                           }

                           if (this.currentStrategy == AiObtain.ObtainStrategy.GATHER && !this.familyFallbackUsed && AiObtain.isSourcesExhausted(result)) {
                              String family = this.familyFallbackSpec();
                              if (family != null && !this.isSourceNearby()) {
                                 this.familyFallbackUsed = true;
                                 this.fallbackFamily = family;
                                 this.init_af_DataUtil = AiItems.resolveItem(family);
                                 this.craftableItemsCache = null;
                                 this.smeltableItemsCache = null;
                                 this.sourceBlocksCache = null;
                                 this.sourceMobsCache = null;
                                 this.strategyOp = AiRunDiagnostics.trace(
                                    "obtain", "family fallback " + family, this.gatherSourcesOp(Math.max(15, this.init_I_19()))
                                 );
                                 if (this.strategyOp == null) {
                                    return this.running("No harvestable source for the " + family + " family");
                                 }

                                 this.strategyDetail = "gather " + family + " (family fallback from " + spec + ")";
                                 return this.running("No " + spec + " in range; gathering the " + family + " family instead");
                              }
                           }

                           if (this.currentStrategy == AiObtain.ObtainStrategy.CRAFT && this.craftMissingRetries < 2 && result.dataJson().contains("\"missing\"")) {
                              this.craftMissingRetries++;
                              this.currentStrategy = null;
                              return this.running("Craft still short on materials; fetching the rest");
                           }

                           if (AiItems.countInInventory(this.init_af_DataUtil) > this.countAtStrategyStart && this.progressRepeats.merge(this.currentStrategy, 1, Integer::sum) <= 8) {
                              this.currentStrategy = null;
                              return this.running("Strategy " + result.status() + " after progress; another pass");
                           }

                           this.triedStrategies.add(this.currentStrategy);
                        }

                        this.currentStrategy = null;
                        return this.running("Strategy " + result.status() + "; re-evaluating");
                     }
                  }
               }

               AiOperation.Result tickSurvivalGuard() {
                  if (this.survivalOp != null) {
                     AiOperation.Result result = this.survivalOp.tick();
                     if (result.status() == AiOperation.Status.UNCERTAIN) {
                        return this.finish(AiOperation.Status.UNCERTAIN, result.message());
                     }

                     if (!result.terminal()) {
                        return this.running("Interrupted: " + this.survivalReason + " - " + result.message());
                     }

                     this.survivalOp.cancel();
                     this.survivalOp = null;
                     return this.running("Resuming after " + this.survivalReason);
                  } else if (autismclient.ai.engine.AiMotionSafety.isPlayerSupported() && (this.strategyOp == null || this.strategyOp.canInterruptNow())) {
                     AiGuard.StatusSnapshot vitals = AiGuard.sampleStatus();
                     AiOperation action = AiGuard.createResponseOperation(this.guardPolicy, vitals, this.init_I_19());
                     if (action == null) {
                        return null;
                     }

                     this.survivalReason = AiGuard.decisionReason(this.guardPolicy, vitals);
                     this.survivalInterruptions++;
                     if (this.strategyOp != null) {
                        this.strategyOp.cancel();
                        this.strategyOp = null;
                        this.currentStrategy = null;
                     }

                     this.survivalOp = AiRunDiagnostics.trace("survival", "obtain: " + this.survivalReason, action);
                     return this.running("Interrupted: " + this.survivalReason);
                  } else {
                     return null;
                  }
               }

               AiObtain.ObtainStrategy nextStrategy() {
                  if (!this.triedStrategies.contains(AiObtain.ObtainStrategy.CRAFT) && !this.craftableItems().isEmpty()) {
                     AiCraftPlanner.RecipeGraph graph = AiCraft.buildRecipeGraph();
                     if (this.craftableItems().stream().anyMatch(item -> {
                        AiCraftPlanner.CraftPlan plan = AiCraftPlanner.plan(graph, BuiltInRegistries.ITEM.getKey(item).toString(), goal);
                        return plan.complete() && !plan.steps().isEmpty();
                     })) {
                        return AiObtain.ObtainStrategy.CRAFT;
                     }
                  }

                  if (!this.triedStrategies.contains(AiObtain.ObtainStrategy.SMELT)
                     && this.smeltableItems()
                        .stream()
                        .anyMatch(
                           output -> autismclient.util.AutoCraftingEngine.getSmeltRecipesFor(mc, output)
                              .stream()
                              .anyMatch(
                                 p -> p.inputs().stream().mapToInt(i -> AiGameState.countInventoryItem(BuiltInRegistries.ITEM.getKey(i).toString())).sum()
                                    >= goal - AiItems.countInInventory(this.init_af_DataUtil)
                              )
                        )) {
                     return AiObtain.ObtainStrategy.SMELT;
                  }

                  if (allowMining
                     && !this.triedStrategies.contains(AiObtain.ObtainStrategy.GATHER)
                     && this.init_af_DataUtil
                        .items()
                        .stream()
                        .anyMatch(
                           item -> item instanceof BlockItem form
                              && !AiObtain.mineableItems().contains(BuiltInRegistries.ITEM.getKey(item).toString())
                              && AiObtain.init_Z_Item_Block(item, form.getBlock())
                              && this.canHarvestWithHeldTools(form.getBlock().defaultBlockState())
                              && AiTerrain.nearestBlockDistSqr(mc, List.of(form.getBlock()), 8) <= 64.0
                        )) {
                     return AiObtain.ObtainStrategy.GATHER;
                  }

                  if (!allowCombat
                     || this.triedStrategies.contains(AiObtain.ObtainStrategy.HUNT)
                     || this.sourceMobs().isEmpty()
                     || this.isSourceNearby()
                     || this.nearestLivingOf(this.sourceMobs(), 160.0) == null && !AiObtain.allPassive(this.sourceMobs())) {
                     for (AiObtain.ObtainStrategy strategy : AiObtain.ObtainStrategy.values()) {
                        if (!this.triedStrategies.contains(strategy)
                           && (strategy != AiObtain.ObtainStrategy.HUNT || allowCombat)
                           && (strategy != AiObtain.ObtainStrategy.GATHER && strategy != AiObtain.ObtainStrategy.MINE || allowMining)
                           && this.isStrategyAvailable(strategy)) {
                           return strategy;
                        }
                     }

                     return null;
                  } else {
                     return AiObtain.ObtainStrategy.HUNT;
                  }
               }

               boolean isStrategyAvailable(AiObtain.ObtainStrategy strategy) {
                  boolean var10000;
                  switch (strategy) {
                     case INVENTORY:
                        var10000 = false;
                        break;
                     case TOOL:
                        var10000 = depth < 5 && this.requiredToolId() != null;
                        break;
                     case CRAFT:
                        var10000 = !this.craftableItems().isEmpty();
                        break;
                     case SMELT:
                        var10000 = this.hasUsableSmeltInput();
                        break;
                     case GATHER:
                        var10000 = !this.sourceBlocks().isEmpty() && !this.shouldDeferToMining();
                        break;
                     case MINE:
                        var10000 = this.mineableSourceId() != null;
                        break;
                     case PROSPECT:
                        var10000 = AiProspect.canProspect(target) && !AiOracle.isActive();
                        break;
                     case HUNT:
                        var10000 = !this.sourceMobs().isEmpty();
                        break;
                     default:
                        throw new MatchException(null, null);
                  }

                  return var10000;
               }

               String requiredToolId() {
                  for (Block block : this.sourceBlocks()) {
                     BlockState state = block.defaultBlockState();
                     if (!this.canHarvestWithHeldTools(state)) {
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

               boolean canHarvestWithHeldTools(BlockState state) {
                  return autismclient.ai.engine.MiningCost.bestToolSlot(mc, state, true, (block, tool) -> AiItems.canDropWantedItem(mc, block, tool, this.init_af_DataUtil.items())) >= 0;
               }

               AiOperation operationFor(AiObtain.ObtainStrategy strategy) {
                  int missing = goal - AiItems.countInInventory(this.init_af_DataUtil);
                  int legSeconds = Math.max(15, this.init_I_19());
                  switch (strategy) {
                     case TOOL:
                        String toolId = this.requiredToolId();
                        if (toolId == null) {
                           return null;
                        }

                        this.strategyDetail = "obtain " + toolId + " to harvest " + spec;
                        Set<String> nested = new LinkedHashSet<>(visiting);

                        for (Item item : this.init_af_DataUtil.items()) {
                           nested.add(BuiltInRegistries.ITEM.getKey(item).toString());
                        }

                        return AiObtain.obtainAtDepth(
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
                        Item output = AiObtain.bestCraftTarget(this.craftableItems(), goal, graph, AiObtain::nearestSourceDistance);
                        if (output == null) {
                           return null;
                        } else {
                           String outputId = BuiltInRegistries.ITEM.getKey(output).toString();
                           AiCraftPlanner.CraftPlan plan = AiCraftPlanner.plan(graph, outputId, goal);
                           if (!plan.complete() && depth < 5) {
                              Entry<String, Integer> shortfall = this.missingCraftInput(plan);
                              if (shortfall != null) {
                                 String want = shortfall.getKey();
                                 if (want.endsWith("_log") && AiObtain.nearestSourceDistance(want) == 1.7976931348623157E308 && !visiting.contains("log")) {
                                    want = "log";
                                 }

                                 this.strategyDetail = "obtain " + want + " for " + outputId;
                                 Set<String> nestedCraft = new LinkedHashSet<>(visiting);
                                 nestedCraft.add(outputId);
                                 int count = shortfall.getValue() + AiObtain.tableMaterialExtra(AiObtain.needsCraftingTable(graph, outputId, plan), shortfall.getKey());
                                 return AiObtain.obtainAtDepth(
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

                           this.strategyDetail = "craft " + outputId;
                           return AiCraft.craft(AiJson.objectOf("item", outputId, "count", goal, "timeoutSeconds", legSeconds));
                        }
                     case SMELT:
                        Item outputx = this.smeltableItems()
                           .stream()
                           .filter(this::isSmeltInputUsable)
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
                           Entry<String, Integer> inputNeed = this.smeltInputNeed(outputx);
                           Set<String> nestedSmelt = new LinkedHashSet<>(visiting);
                           nestedSmelt.add(outputId);
                           if (inputNeed != null && depth < 5) {
                              String inputId = inputNeed.getKey();
                              this.strategyDetail = "obtain " + inputId + " to smelt into " + outputId;
                              return AiObtain.obtainAtDepth(
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
                                    this.strategyDetail = "obtain " + cooking.station() + " for " + outputId;
                                    return AiObtain.obtainAtDepth(
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
                                 Item fuel = AiSmelt.availableFuel("", source, missing);
                                 int burnDivisor = AiSmelt.burnDivisor(cooking.station());
                                 int burn = fuel == null ? 0 : AiItems.fuelBurnTime(new ItemStack(fuel)) / burnDivisor;
                                 int fuelCount = fuel == null ? 0 : AiSmelt.spareFuel(fuel);
                                 int furnaceCount = AiParallelSmelt.furnaceCountFor(missing);
                                 if (fuelCount < AiParallelSmelt.fuelNeeded(missing, furnaceCount, cooking.cookTimeTicks(), burn)) {
                                    ItemStack fuelStack = fuel == null ? ItemStack.EMPTY : new ItemStack(fuel);
                                    boolean coalNearby = AiObtain.nearestSourceDistance("minecraft:coal") != 1.7976931348623157E308
                                       && AiItems.hasUsableToolFor(Blocks.COAL_ORE.defaultBlockState());
                                    String fuelId;
                                    int needed;
                                    if (fuel == null || AiSmelt.fuelTier(fuelStack) != 1 || fuel != Items.COAL && fuel != Items.CHARCOAL) {
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
                                       this.strategyDetail = "obtain fuel " + fuelId + " for " + outputId;
                                       return AiObtain.obtainAtDepth(
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

                              this.strategyDetail = "smelt " + outputId;

                              try {
                                 int largestInput = autismclient.util.AutoCraftingEngine.getSmeltRecipesFor(mc, outputx)
                                    .stream()
                                    .flatMap(p -> p.inputs().stream())
                                    .mapToInt(i -> AiGameState.countInventoryItem(BuiltInRegistries.ITEM.getKey(i).toString()))
                                    .max()
                                    .orElse(0);
                                 int batchGoal = Math.min(goal, AiItems.countInInventory(this.init_af_DataUtil) + largestInput);
                                 return AiSmelt.smelt(AiJson.objectOf("item", outputId, "count", batchGoal, "timeoutSeconds", legSeconds));
                              } catch (IllegalArgumentException e) {
                                 this.strategyDetail = "smelt " + outputId + " (" + e.getMessage() + ")";
                                 return null;
                              }
                           }
                        }
                     case GATHER:
                        this.strategyDetail = "gather " + spec;
                        return this.gatherSourcesOp(legSeconds);
                     case MINE:
                        String mineId = this.mineableSourceId();
                        if (mineId == null) {
                           return null;
                        }

                        this.strategyDetail = "mine " + mineId + " below ground for " + spec;
                        return this.mineOrDescendOp(mineId, missing, legSeconds);
                     case PROSPECT:
                        this.strategyDetail = "prospect " + spec;
                        return AiProspect.create(AiJson.objectOf("item", spec, "count", goal, "timeoutSeconds", legSeconds));
                     case HUNT:
                        List<EntityType<?>> mobs = this.sourceMobs();
                        if (mobs.isEmpty()) {
                           return null;
                        }

                        List<String> names = new ArrayList<>();

                        for (EntityType<?> type : mobs) {
                           names.add(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
                        }

                        this.strategyDetail = "hunt " + String.join(",", names);
                        JsonObject fightArgs = AiJson.objectOf(
                           "target", String.join(",", names), "count", Math.max(1, Math.min(32, missing)), "radius", radius, "timeoutSeconds", legSeconds
                        );
                        String colors = AiObtain.woolColorList(this.init_af_DataUtil.items());
                        if (!colors.isEmpty()) {
                           fightArgs.addProperty("sheepColors", colors);
                        }

                        if (AiObtain.allPassive(mobs)) {
                           fightArgs.addProperty("retreatHealth", 0);
                        }

                        if (AiObtain.allPassive(mobs)) {
                           fightArgs.addProperty("radius", Math.max(radius, 64));
                        }

                        return this.huntWithSearch(mobs, fightArgs, names, legSeconds);
                     default:
                        return null;
                  }
               }

               AiOperation huntWithSearch(List<EntityType<?>> mobs, JsonObject fightArgs, List<String> names, int legSeconds) {
                  if (this.nearestLivingOf(mobs, 160.0) == null && AiObtain.allPassive(mobs)) {
                     this.strategyDetail = "look for " + String.join(",", names);
                     List<Item> items = List.copyOf(this.init_af_DataUtil.items());
                     return AiNetherRun.then(
                        AiObtain.searchForEntity(e -> mobs.contains(e.getType()) && AiObtain.sheepMatchesWool(e, items), String.join(",", names), legSeconds),
                        () -> this.approachMobThenHunt(mobs, fightArgs, this.init_I_19())
                     );
                  } else {
                     return this.approachMobThenHunt(mobs, fightArgs, legSeconds);
                  }
               }

               boolean canMakeWoodenSword() {
                  return AiItems.countInInventory(AiItems.resolveItem("log")) > 0 || AiItems.countInInventory(AiItems.resolveItem("planks")) >= 2 || AiObtain.nearestSourceDistance("log") <= 1024.0;
               }

               AiOperation approachMobThenHunt(List<EntityType<?>> mobs, JsonObject fightArgs, int legSeconds) {
                  Entity far = this.nearestLivingOf(mobs, 160.0);
                  if (far != null && mc.player.distanceTo(far) > 40.0) {
                     this.strategyDetail = "walk to the " + BuiltInRegistries.ENTITY_TYPE.getKey(far.getType()).getPath() + " and hunt";
                     AiOperation walk = AiGoto.travelToCompat(far.blockPosition(), 8.0, true, true, true, legSeconds);
                     return this.withDropPickup(AiNetherRun.sequence(walk, () -> this.huntWithWeaponPrep(mobs, fightArgs)));
                  } else {
                     return this.withDropPickup(this.huntWithWeaponPrep(mobs, fightArgs));
                  }
               }

               AiOperation huntWithWeaponPrep(List<EntityType<?>> mobs, JsonObject fightArgs) {
                  if (AiObtain.allPassive(mobs) && AiItems.bestWeaponSlot(36) < 0 && depth < 5 && !visiting.contains("minecraft:wooden_sword") && this.canMakeWoodenSword()) {
                     this.strategyDetail = "a wooden sword before the hunt";
                     Set<String> nested = new LinkedHashSet<>(visiting);
                     nested.add("minecraft:wooden_sword");
                     AiOperation sword = AiObtain.obtainAtDepth(
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
                     return AiNetherRun.sequence(sword, () -> AiCombat.fight(fightArgs));
                  } else {
                     return AiCombat.fight(fightArgs);
                  }
               }

               Entry<String, Integer> missingCraftInput(AiCraftPlanner.CraftPlan plan) {
                  return AiObtain.firstUnvisitedMissing(plan.missing(), visiting, this.targetItemIds);
               }

               Entity nearestLivingOf(List<EntityType<?>> types, double range) {
                  Entity best = null;
                  double bestSq = range * range;

                  for (Entity e : mc.level.entitiesForRendering()) {
                     if (e instanceof LivingEntity living
                        && living.isAlive()
                        && types.contains(e.getType())
                        && AiObtain.sheepMatchesWool(e, this.init_af_DataUtil.items())) {
                        double d = mc.player.distanceToSqr(e);
                        if (d < bestSq) {
                           bestSq = d;
                           best = e;
                        }
                     }
                  }

                  return best;
               }

               AiOperation withDropPickup(final AiOperation fight) {
                  return new AiOperation() {
                     final List<Item> wantedItems = new ArrayList<>(init_af_DataUtil.items());
                     AiOperation currentOp = fight;
                     AiOperation.Result fightResult;
                     int init_I = -1;
                     boolean cancelled;

                     @Override
                     public AiOperation.Result tick() {
                        if (!this.cancelled && mc.player != null && mc.level != null) {
                           if (this.currentOp != null) {
                              AiOperation.Result result = this.currentOp.tick();
                              if (!result.terminal()) {
                                 return result;
                              }

                              this.currentOp.cancel();
                              this.currentOp = null;
                              if (this.init_I < 0) {
                                 this.fightResult = result;
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
                           if (this.init_I == 0 && !this.wantedItems.isEmpty()) {
                              this.init_I++;
                              List<String> ids = new ArrayList<>();

                              for (Item variant : this.wantedItems) {
                                 ids.add(BuiltInRegistries.ITEM.getKey(variant).toString());
                              }

                              String dropIds = String.join(",", ids);
                              this.currentOp = AiGameplay.pickupItems(
                                 AiJson.objectOf(
                                    "item", dropIds, "count", missing, "additional", true, "timeoutSeconds", Math.max(1, Math.min(60, init_I_19()))
                                 )
                              );
                              return AiGameOperations.result(AiOperation.Status.RUNNING, "Collecting the dropped " + spec, new JsonObject());
                           } else {
                              return AiGameOperations.result(this.fightResult.status(), this.fightResult.message(), new JsonObject());
                           }
                        } else {
                           return AiGameOperations.result(AiOperation.Status.CANCELLED, "Hunt cancelled", new JsonObject());
                        }
                     }

                     @Override
                     public boolean canInterruptNow() {
                        return this.currentOp == null || this.currentOp.canInterruptNow();
                     }

                     @Override
                     public boolean requiresReconciliationAfterEmergency() {
                        return this.currentOp != null && this.currentOp.requiresReconciliationAfterEmergency();
                     }

                     @Override
                     public void cancel() {
                        this.cancelled = true;
                        if (this.currentOp != null) {
                           this.currentOp.cancel();
                           this.currentOp = null;
                        }
                     }
                  };
               }

               AiOperation recoveryOperationFor(AiOperation.Result result) {
                  if (this.currentStrategy == AiObtain.ObtainStrategy.SMELT
                     && !this.furnaceDetourTried
                     && depth < 5
                     && result.dataJson().contains("\"minecraft:furnace\"")
                     && !visiting.contains("minecraft:furnace")
                     && !this.targetItemIds.contains("minecraft:furnace")) {
                     this.furnaceDetourTried = true;
                     this.strategyDetail = "obtain minecraft:furnace to smelt";
                     Set<String> nested = new LinkedHashSet<>(visiting);
                     nested.addAll(this.targetItemIds);
                     return AiObtain.obtainAtDepth(
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

                  if (this.currentStrategy != AiObtain.ObtainStrategy.CRAFT || this.init_Z || depth >= 5) {
                     return null;
                  }

                  if (!AiObtain.hasMissingData(result.dataJson())) {
                     return null;
                  }

                  if (!visiting.contains("minecraft:crafting_table") && !this.targetItemIds.contains("minecraft:crafting_table")) {
                     this.init_Z = true;
                     if (AiItems.countInInventory(AiItems.resolveItem("planks")) < 4 && AiItems.countInInventory(AiItems.resolveItem("log")) <= 0) {
                        this.strategyDetail = "obtain minecraft:crafting_table for the 3x3 grid";
                        Set<String> nested = new LinkedHashSet<>(visiting);
                        nested.addAll(this.targetItemIds);
                        return AiObtain.obtainAtDepth(
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
                        this.strategyDetail = "craft a crafting table for the 3x3 grid";
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

               String mineableSourceId() {
                  List<String> ids = new ArrayList<>(this.sourceBlocks().size());

                  for (Block block : this.sourceBlocks()) {
                     ids.add(BuiltInRegistries.BLOCK.getKey(block).toString());
                  }

                  return AiObtain.firstCommonTerrainSource(ids);
               }

               AiOperation gatherSourcesOp(int timeoutSecondsx) {
                  List<String> blocks = new ArrayList<>();

                  for (Block block : this.sourceBlocks()) {
                     if (this.canHarvestWithHeldTools(block.defaultBlockState())) {
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
                        autismclient.ai.engine.AiBlockMine.jsonArray(blocks),
                        "drops",
                        autismclient.ai.engine.AiBlockMine.jsonArray(this.init_af_DataUtil.itemIds()),
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

               AiOperation mineOrDescendOp(final String blockId, final int count, final int timeoutSecondsx) {
                  return !AiOreRoute.LEGIT_MINING
                     ? this.gatherSourcesOp(timeoutSeconds)
                     : new AiOperation() {
                        final long deadlineNanos = System.nanoTime() + timeoutSeconds * 1000000000L;
                        final BlockPos startPos = mc.player.blockPosition();
                        final int startY = this.startPos.getY();
                        final Block targetBlock = AiCraft.requireBlock(blockId);
                        AiOperation mineOp;
                        boolean sourceSeen;
                        int mineFailures;
                        boolean cancelled;

                        @Override
                        public AiOperation.Result tick() {
                           if (!this.cancelled && mc.player != null && mc.level != null) {
                              if (System.nanoTime() > this.deadlineNanos) {
                                 return AiGameOperations.result(AiOperation.Status.TIMED_OUT, "Mine deadline reached", new JsonObject());
                              }

                              if (AiItems.countInInventory(init_af_DataUtil) >= goal) {
                                 return AiGameOperations.result(AiOperation.Status.SUCCEEDED, "Mined " + blockId + " to target", new JsonObject());
                              }

                              if (this.mineOp != null) {
                                 AiOperation.Result result = this.mineOp.tick();
                                 if (!result.terminal()) {
                                    return result;
                                 }

                                 this.mineOp.cancel();
                                 this.mineOp = null;
                                 if (result.status() == AiOperation.Status.CANCELLED) {
                                    return result;
                                 }

                                 if (this.sourceSeen) {
                                    return result;
                                 }

                                 if (result.status() != AiOperation.Status.SUCCEEDED && ++this.mineFailures >= 2) {
                                    this.sourceSeen = true;
                                 }
                              }

                              int currentY = mc.player.blockPosition().getY();
                              boolean found = this.sourceSeen || this.isTargetNearby();
                              Integer nextY = AiObtain.nextDescentY(this.startY, currentY, mc.level.getMinY(), found);
                              if (found) {
                                 this.sourceSeen = true;
                                 this.mineOp = autismclient.ai.engine.AiMine.mine(
                                    AiJson.objectOf("block", blockId, "count", count, "radius", radius, "timeoutSeconds", init_I_19())
                                 );
                                 return AiGameOperations.result(AiOperation.Status.RUNNING, "Mining " + blockId, new JsonObject());
                              }

                              if (nextY == null) {
                                 return AiGameOperations.result(
                                    AiOperation.Status.FAILED,
                                    "No " + blockId + " in the scan band after descending " + (this.startY - currentY) + " to Y=" + currentY,
                                    new JsonObject()
                                 );
                              }

                              this.mineOp = AiGoto.travelToCompat(
                                 new BlockPos(this.startPos.getX(), nextY, this.startPos.getZ()), 2.5, true, true, true, AiObtain.legSecondsFor(init_I_19())
                              );
                              return AiGameOperations.result(AiOperation.Status.RUNNING, "Descending to Y=" + nextY + " toward " + blockId, new JsonObject());
                           } else {
                              return AiGameOperations.result(AiOperation.Status.CANCELLED, "Mine cancelled", new JsonObject());
                           }
                        }

                        boolean isTargetNearby() {
                           return AiTerrain.nearestBlockDistSqr(mc, List.of(this.targetBlock), Math.min(radius, 96)) != 1.7976931348623157E308;
                        }

                        @Override
                        public boolean canInterruptNow() {
                           return this.mineOp == null || this.mineOp.canInterruptNow();
                        }

                        @Override
                        public boolean requiresReconciliationAfterEmergency() {
                           return this.mineOp != null && this.mineOp.requiresReconciliationAfterEmergency();
                        }

                        @Override
                        public void cancel() {
                           this.cancelled = true;
                           if (this.mineOp != null) {
                              this.mineOp.cancel();
                              this.mineOp = null;
                           }
                        }
                     };
               }

               Set<Item> craftableItems() {
                  if (this.craftableItemsCache == null) {
                     this.craftableItemsCache = this.knownRecipeItems(true);
                  }

                  return this.craftableItemsCache;
               }

               Set<Item> smeltableItems() {
                  if (this.smeltableItemsCache == null) {
                     this.smeltableItemsCache = this.knownRecipeItems(false);
                  }

                  return this.smeltableItemsCache;
               }

               Set<Item> knownRecipeItems(boolean crafting) {
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

               boolean isProcessedOrNonOre(String itemId) {
                  return itemId.startsWith("minecraft:raw_") || !itemId.endsWith("_ore");
               }

               boolean hasUsableSmeltInput() {
                  return this.smeltableItems().stream().anyMatch(this::isSmeltInputUsable);
               }

               boolean isSmeltInputUsable(Item output) {
                  Entry<String, Integer> need = this.smeltInputNeed(output);
                  return need == null || this.isProcessedOrNonOre(need.getKey());
               }

               Entry<String, Integer> smeltInputNeed(Item output) {
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
                           && (count > bestCount || count == bestCount && best != null && !this.isProcessedOrNonOre(best) && this.isProcessedOrNonOre(inputId))) {
                           bestCount = count;
                           best = inputId;
                        }
                     }
                  }

                  return best == null ? null : Map.entry(best, goal - AiItems.countInInventory(this.init_af_DataUtil));
               }

               boolean shouldDeferToMining() {
                  if (!AiOreRoute.LEGIT_MINING) {
                     return false;
                  }

                  if (this.mineDeferred || this.triedStrategies.contains(AiObtain.ObtainStrategy.MINE) || this.mineableSourceId() == null) {
                     return false;
                  }

                  if (!AiObtain.shouldDeferToMine(false, false, true, this.isSourceNearby())) {
                     return false;
                  }

                  this.mineDeferred = true;
                  this.attemptLog
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

               boolean isSourceNearby() {
                  if (this.sourceNearbyCache == null) {
                     this.sourceNearbyCache = AiTerrain.nearestBlockDistSqr(mc, this.sourceBlocks(), 32) != 1.7976931348623157E308;
                  }

                  return this.sourceNearbyCache;
               }

               String familyFallbackSpec() {
                  return target.items().size() != 1
                     ? null
                     : AiObtain.familyOf(BuiltInRegistries.ITEM.getKey(target.items().iterator().next()).toString(), AiObtain::resolveItemIds);
               }

               String obtainedLabel() {
                  if (this.fallbackFamily == null) {
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

                  return AiObtain.obtainedDescription(spec, this.fallbackFamily, bestId);
               }

               List<Block> sourceBlocks() {
                  if (this.sourceBlocksCache != null) {
                     return this.sourceBlocksCache;
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
                  this.sourceBlocksCache = List.copyOf(blocks);
                  return this.sourceBlocksCache;
               }

               List<EntityType<?>> sourceMobs() {
                  if (this.sourceMobsCache != null) {
                     return this.sourceMobsCache;
                  }

                  List<EntityType<?>> mobs = new ArrayList<>();

                  for (Item item : this.init_af_DataUtil.items()) {
                     for (EntityType<?> type : AiItems.sourceEntitiesFor(item)) {
                        if (!mobs.contains(type)) {
                           mobs.add(type);
                        }
                     }
                  }

                  this.sourceMobsCache = List.copyOf(mobs);
                  return this.sourceMobsCache;
               }

               int init_I_19() {
                  return (int)Math.max(1L, (this.deadlineNanos - System.nanoTime()) / 1000000000L);
               }

               JsonObject statusData() {
                  JsonObject out = AiJson.objectOf(
                     "item", spec, "goal", goal, "held", AiItems.countInInventory(this.init_af_DataUtil), "startedWith", held, "depth", depth
                  );
                  out.add("matches", AiJson.GSON.toJsonTree(this.init_af_DataUtil.itemIds()));
                  if (this.fallbackFamily != null) {
                     out.addProperty("familyFallbackFrom", spec);
                  }

                  out.add("attempts", this.attemptLog);
                  if (this.currentStrategy != null) {
                     out.addProperty("strategy", this.currentStrategy.name().toLowerCase(Locale.ROOT));
                     out.addProperty("strategyDetail", this.strategyDetail);
                  }

                  JsonArray spent = new JsonArray();

                  for (AiObtain.ObtainStrategy strategy : this.triedStrategies) {
                     spent.add(strategy.name().toLowerCase(Locale.ROOT));
                  }

                  out.add("exhausted", spent);
                  out.add("survival", AiGuard.statusToJson(AiGuard.sampleStatus(), this.survivalInterruptions, this.survivalReason));
                  return out;
               }

               AiOperation.Result running(String message) {
                  if (this.labelledSpec != this.init_af_DataUtil) {
                     this.labelledSpec = this.init_af_DataUtil;
                     String label = this.init_af_DataUtil.items().size() == 1
                        ? this.init_af_DataUtil.items().iterator().next().getDefaultInstance().getHoverName().getString()
                        : this.obtainedLabel().replace("minecraft:", "").replace('_', ' ');
                     this.visualTitle = "Getting " + label;
                  }

                  AiVisualization.showCount(this, this.visualTitle, AiTaskStatus.phaseLabel(message, "Preparing materials"), AiItems.countInInventory(this.init_af_DataUtil), goal);
                  return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.statusData().toString(), List.of());
               }

               AiOperation.Result finish(AiOperation.Status status, String message) {
                  JsonObject data = this.statusData();
                  this.cancel();
                  return new AiOperation.Result(status, message, data.toString(), List.of());
               }

               @Override
               public boolean canInterruptNow() {
                  return (this.strategyOp == null || this.strategyOp.canInterruptNow())
                     && (this.survivalOp == null || this.survivalOp.canInterruptNow());
               }

               @Override
               public boolean requiresReconciliationAfterEmergency() {
                  return this.strategyOp != null && this.strategyOp.requiresReconciliationAfterEmergency()
                     || this.survivalOp != null && this.survivalOp.requiresReconciliationAfterEmergency();
               }

               @Override
               public void cancel() {
                  if (!this.cancelled) {
                     this.cancelled = true;
                     AiVisualization.clearOwner(this);
                     if (this.survivalOp != null) {
                        this.survivalOp.cancel();
                        this.survivalOp = null;
                     }

                     if (this.strategyOp != null) {
                        this.strategyOp.cancel();
                        this.strategyOp = null;
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
      if (item instanceof BlockItem form && form.getBlock() == block && !mineableItems().contains(BuiltInRegistries.ITEM.getKey(item).toString())) {
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

   static Item bestCraftTarget(Set<Item> craftable, int goal, AiCraftPlanner.RecipeGraph graph, ToDoubleFunction<String> missingSourceDistance) {
      Item best = null;
      String bestId = null;
      int bestMissing = 2147483647;
      int bestHeldInputs = -1;
      double bestDistance = -1.0;
      AiCraftPlanner.CraftPlan bestPlan = null;

      for (Item item : craftable) {
         if (item != null) {
            String id = BuiltInRegistries.ITEM.getKey(item).toString();
            AiCraftPlanner.CraftPlan plan = AiCraftPlanner.plan(graph, id, goal);
            int missing = 0;

            for (int amount : plan.missing().values()) {
               missing += amount;
            }

            int heldInputs = heldInputsFor(plan, id, graph);
            double distance = -1.0;
            boolean replace;
            if (best == null) {
               replace = true;
            } else if (missing != bestMissing) {
               replace = missing < bestMissing;
            } else if (heldInputs != bestHeldInputs) {
               replace = heldInputs > bestHeldInputs;
            } else {
               distance = nearestMissingSourceDistance(plan, missingSourceDistance);
               if (bestDistance < 0.0) {
                  bestDistance = nearestMissingSourceDistance(bestPlan, missingSourceDistance);
               }

               replace = distance < bestDistance || distance == bestDistance && (isDefaultVariant(id) != isDefaultVariant(bestId) ? isDefaultVariant(id) : id.compareTo(bestId) < 0);
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

   static boolean isDefaultVariant(String id) {
      String path = id.contains(":") ? id.substring(id.indexOf(58) + 1) : id;
      return path.startsWith("white_") || path.startsWith("oak_");
   }

   static String woolColorList(Collection<Item> items) {
      List<String> colors = new ArrayList<>();

      for (Item item : items) {
         String path = BuiltInRegistries.ITEM.getKey(item).getPath();
         if (path.endsWith("_wool")) {
            colors.add(path.substring(0, path.length() - "_wool".length()));
         }
      }

      return String.join(",", colors);
   }

   static boolean sheepMatchesWool(Entity entity, Collection<Item> items) {
      if (!(entity instanceof Sheep sheep)) {
         return true;
      } else {
         String colors = woolColorList(items);
         return colors.isEmpty() ? true : !sheep.isSheared() && List.of(colors.split(",")).contains(sheep.getColor().getSerializedName());
      }
   }

   static boolean allPassive(List<EntityType<?>> mobs) {
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

   static AiOperation searchForEntity(final Predicate<Entity> wanted, final String what, final int seconds) {
      final Minecraft mc = Minecraft.getInstance();
      return new AiOperation() {
         final long deadlineNanos = System.nanoTime() + Math.max(15, seconds) * 1000000000L;
         final BlockPos searchOrigin = mc.player.blockPosition();
         final Object startLevel = mc.level;
         AiOperation legOp;
         int legCount;
         int seekRetries;
         boolean cancelled;

         boolean wantedEntityNearby() {
            for (Entity e : mc.level.entitiesForRendering()) {
               if (e instanceof LivingEntity living && living.isAlive() && wanted.test(e) && mc.player.distanceToSqr(e) <= 25600.0) {
                  return true;
               }
            }

            return false;
         }

         AiOperation.Result finish(AiOperation.Status status, String message) {
            return AiGameOperations.result(status, message, AiJson.objectOf("looking", what, "legs", this.legCount));
         }

         @Override
         public AiOperation.Result tick() {
            if (this.cancelled || mc.player == null || mc.level != this.startLevel) {
               return this.finish(AiOperation.Status.CANCELLED, "Search cancelled or world changed");
            }

            if (!mc.player.isAlive()) {
               return this.finish(AiOperation.Status.FAILED, "Player died while looking for " + what);
            }

            if (this.legOp != null) {
               if (this.legOp.canInterruptNow() && !this.legOp.requiresReconciliationAfterEmergency() && this.wantedEntityNearby()) {
                  this.legOp.cancel();
                  this.legOp = null;
                  return this.finish(AiOperation.Status.SUCCEEDED, "Found " + what);
               }

               AiOperation.Result r = this.legOp.tick();
               if (!r.terminal()) {
                  return this.finish(AiOperation.Status.RUNNING, "Looking for " + what + ": " + r.message());
               }

               this.legOp.cancel();
               this.legOp = null;
            }

            if (this.wantedEntityNearby()) {
               return this.finish(AiOperation.Status.SUCCEEDED, "Found " + what);
            }

            long left = (this.deadlineNanos - System.nanoTime()) / 1000000000L;
            if (left <= 0L || this.legCount >= 8) {
               return this.finish(AiOperation.Status.FAILED, "No " + what + " found in " + this.legCount + " legs");
            }

            if (!AiSpeedrun.seesSkyNear(mc, 5) && this.seekRetries < 2) {
               this.seekRetries++;
               this.legOp = autismclient.ai.engine.AiSeek.surfaceSeek(AiJson.objectOf("timeoutSeconds", (int)Math.min(120L, left)));
               return this.finish(AiOperation.Status.RUNNING, "Up to open sky to look for " + what);
            }

            double a = this.legCount++ * 2.399;
            int x = this.searchOrigin.getX() + (int)Math.round(Math.cos(a) * 64.0);
            int z = this.searchOrigin.getZ() + (int)Math.round(Math.sin(a) * 64.0);
            BlockPos surface = AiSpeedrun.findSurface(mc.player.blockPosition(), x, z, column -> AiSpeedrun.observedSurface(mc, column));
            if (surface == null) {
               return this.finish(AiOperation.Status.RUNNING, "No safe ground that way; trying another bearing");
            }

            this.legOp = AiGoto.travelToCompat(
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
            return this.finish(AiOperation.Status.RUNNING, "Looking for " + what);
         }

         @Override
         public boolean canInterruptNow() {
            return this.legOp == null || this.legOp.canInterruptNow();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.legOp != null && this.legOp.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            this.cancelled = true;
            if (this.legOp != null) {
               this.legOp.cancel();
            }
         }
      };
   }

   private static double nearestMissingSourceDistance(AiCraftPlanner.CraftPlan plan, ToDoubleFunction<String> missingSourceDistance) {
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
            if (e instanceof LivingEntity living && living.isAlive() && mobs.contains(e.getType()) && sheepMatchesWool(e, target.items())) {
               double d = mc.player.distanceToSqr(e);
               if (d <= 25600.0) {
                  best = Math.min(best, d);
               }
            }
         }
      }

      return best;
   }

   private static int heldInputsFor(AiCraftPlanner.CraftPlan plan, String outputId, AiCraftPlanner.RecipeGraph graph) {
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

   static Entry<String, Integer> firstUnvisitedMissing(Map<String, Integer> missing, Set<String> visiting, Set<String> ownItemIds) {
      for (Entry<String, Integer> entry : missing.entrySet()) {
         if (!visiting.contains(entry.getKey()) && !ownItemIds.contains(entry.getKey())) {
            return entry;
         }
      }

      return null;
   }

   static String unavailableReason(boolean craftKnown, boolean smeltKnown, boolean hasSourceBlocks, boolean prospectable, boolean hasMobDrops) {
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

   static String firstCommonTerrainSource(List<String> sourceBlockIds) {
      for (String id : sourceBlockIds) {
         if (commonTerrainBlockIds.contains(id)) {
            return id;
         }
      }

      return null;
   }

   static Integer nextDescentY(int startY, int currentY, int minY, boolean sourceInBand) {
      if (sourceInBand) {
         return null;
      }

      if (startY - currentY >= 32) {
         return null;
      }

      int next = Math.max(minY + 4, Math.max(startY - 32, currentY - 8));
      return next < currentY ? next : null;
   }

   static int legSecondsFor(int remainingSeconds) {
      return Math.max(10, Math.min(45, remainingSeconds / 4));
   }

   static String missingToolId(String dataJson) {
      if (dataJson != null && !dataJson.isBlank()) {
         try {
            JsonObject data = AiJson.parseObject(dataJson);
            if (data.has("missing") && data.get("missing").isJsonObject()) {
               for (Entry<String, JsonElement> entry : data.getAsJsonObject("missing").entrySet()) {
                  if (isToolId(entry.getKey())) {
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

   private static boolean isToolId(String id) {
      return id != null && (id.endsWith("_pickaxe") || id.endsWith("_shovel") || id.endsWith("_axe") || id.endsWith("_hoe") || id.endsWith("shears"));
   }

   static int tableMaterialExtra(boolean needsTable, String item) {
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

   static boolean needsCraftingTable(AiCraftPlanner.RecipeGraph graph, String outputId, AiCraftPlanner.CraftPlan plan) {
      if (plan.requiresTable()) {
         return true;
      }

      List<AiCraftPlanner.RecipeSpec> recipes = graph.recipesFor(outputId);
      return !recipes.isEmpty() && recipes.get(0).requiresTable();
   }

   static boolean hasMissingData(String dataJson) {
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

   static boolean shouldDeferToMine(boolean alreadyDeferred, boolean mineTried, boolean hasMineRoute, boolean sourceNearby) {
      return !alreadyDeferred && !mineTried && hasMineRoute && !sourceNearby;
   }

   static int sourceTier(String itemId, Set<String> directlyMineable, boolean craftable, boolean smeltable) {
      if (directlyMineable.contains(itemId)) {
         return 0;
      } else {
         return !craftable && !smeltable ? 2 : 1;
      }
   }

   static Set<String> mineableItems() {
      return AiItems.directMineableItems();
   }

   static AiCraftPlanner.RecipeGraph withMining(final AiCraftPlanner.RecipeGraph inner) {
      final Minecraft mc = Minecraft.getInstance();
      final Set<String> mineable = mineableItems();
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
         public int acquisitionRank(String itemId) {
            boolean craftable = !inner.recipesFor(itemId).isEmpty();
            boolean smeltable = BuiltInRegistries.ITEM
               .getOptional(Identifier.parse(itemId))
               .map(item -> !autismclient.util.AutoCraftingEngine.getSmeltRecipesFor(mc, item).isEmpty())
               .orElse(false);
            return AiObtain.sourceTier(itemId, mineable, craftable, smeltable);
         }

         @Override
         public String familyOf(Set<String> variantIds) {
            return AiObtain.commonFamily(variantIds, AiObtain::resolveItemIds);
         }

         @Override
         public boolean isFarmAnimalDrop(String itemId) {
            return BuiltInRegistries.ITEM.getOptional(Identifier.parse(itemId)).map(item -> AiObtain.allPassive(AiItems.sourceEntitiesFor(item))).orElse(false);
         }

         @Override
         public double acquisitionEffort(String itemId) {
            return mc != null && mc.player != null && mc.level != null ? distances.computeIfAbsent(itemId, id -> {
               double distance = AiObtain.nearestSourceDistance(id);
               return distance == 1.7976931348623157E308 ? 256.0 : 1.0 + Math.sqrt(distance);
            }) : inner.acquisitionEffort(itemId);
         }
      };
   }

   static String commonFamily(Collection<String> variantIds, Function<String, Set<String>> resolver) {
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

   static Set<String> resolveItemIds(String spec) {
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

   static String familyOf(String itemId, Function<String, Set<String>> resolver) {
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

   static boolean isSourcesExhausted(AiOperation.Result result) {
      if (result != null && result.status() == AiOperation.Status.FAILED) {
         String message = result.message();
         return message != null && message.startsWith("No more reachable sources") && message.contains("(collected 0 of");
      } else {
         return false;
      }
   }

   static String obtainedDescription(String spec, String widenedFamily, String obtainedId) {
      return widenedFamily == null ? spec : obtainedId + " (family fallback from " + spec + ")";
   }

   static JsonObject attemptEntry(String strategy, String detail, AiOperation.Result result) {
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

   enum ObtainStrategy {
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
