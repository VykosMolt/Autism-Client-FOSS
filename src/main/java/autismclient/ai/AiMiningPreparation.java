package autismclient.ai;

import autismclient.api.ai.AiOperation;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public final class AiMiningPreparation {
   private AiMiningPreparation() {
   }

   static boolean init(int blocks, double beforeTicks, double afterTicks, double setupTicks, int materialBlocks) {
      return blocks > materialBlocks && afterTicks < beforeTicks && (blocks - materialBlocks) * (beforeTicks - afterTicks) > setupTicks;
   }

   public static AiOperation init(JsonObject args) {
      if (!AiJson.getBoolean(args, "toolPrepared", false)
         && !AiJson.getBoolean(args, "dropsOnly", false)
         && !AiJson.getBoolean(args, "approach", false)
         && !AiJson.getBoolean(args, "descendOnly", false)) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null && mc.level != null && !mc.player.hasInfiniteMaterials()) {
            Set<Item> wanted = new HashSet<>();
            if (args.has("drops")) {
               for (JsonElement drop : args.getAsJsonArray("drops")) {
                  wanted.addAll(AiItems.resolveItem(drop.getAsString()).items());
               }
            }

            if (args.has("drop")) {
               wanted.addAll(AiItems.resolveItem(AiJson.getString(args, "drop", "")).items());
            }

            Set<Block> blocks = new LinkedHashSet<>();
            String id = AiJson.getString(args, "block", "");
            if (!id.isEmpty()) {
               blocks.addAll(AiItems.resolveBlock(id).blocks());
            }

            if (args.has("blocks")) {
               for (JsonElement block : args.getAsJsonArray("blocks")) {
                  blocks.addAll(AiItems.resolveBlock(block.getAsString()).blocks());
               }
            }

            return init(mc, args, wanted, blocks);
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static AiOperation.Result init(AiOperation.Result result, boolean pending, AiOperation.Result child) {
      if (child == result) {
         return AiGuardLayer.wrapPendingTermination(pending, result);
      }

      AiOperation.Result captured = AiNetherRun.init_AiOperation_Result_AiOperation_Status(
         result.status(), result.message(), AiRunResources.parseEvidenceJson(result.dataJson()), pending, child
      );
      return new AiOperation.Result(captured.status(), captured.message(), captured.dataJson(), result.evidence(), result.failure());
   }

   public static AiOperation load(final JsonObject args) {
      if (!AiJson.getBoolean(args, "toolPrepared", false)
         && !AiJson.getBoolean(args, "dropsOnly", false)
         && !AiJson.getBoolean(args, "approach", false)
         && !AiJson.getBoolean(args, "descendOnly", false)) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null && mc.level != null && !mc.player.hasInfiniteMaterials()) {
            Set<Item> wanted = new HashSet<>();
            if (args.has("drops")) {
               for (JsonElement drop : args.getAsJsonArray("drops")) {
                  wanted.addAll(AiItems.resolveItem(drop.getAsString()).items());
               }
            }

            if (args.has("drop")) {
               wanted.addAll(AiItems.resolveItem(AiJson.getString(args, "drop", "")).items());
            }

            Set<Block> blocksToMine = new LinkedHashSet<>();
            String id = AiJson.getString(args, "block", "");
            if (!id.isEmpty()) {
               blocksToMine.addAll(AiItems.resolveBlock(id).blocks());
            }

            if (args.has("blocks")) {
               for (JsonElement block : args.getAsJsonArray("blocks")) {
                  blocksToMine.addAll(AiItems.resolveBlock(block.getAsString()).blocks());
               }
            }

            AiOperation woodPreparation = init(mc, args, wanted, blocksToMine);
            if (woodPreparation != null) {
               return woodPreparation;
            }

            BlockState state = null;
            int slot = -1;
            double slowest = 0.0;

            for (Block block : blocksToMine) {
               BlockState candidate = block.defaultBlockState();
               int toolSlot = autismclient.ai.engine.MiningCost.bestToolSlot(mc, candidate, true, (s, tool) -> AiItems.canDropWantedItem(mc, s, tool, wanted));
               if (toolSlot >= 0) {
                  ItemStack tool = mc.player.getInventory().getItem(toolSlot);
                  if (!(tool.getDestroySpeed(candidate) <= 1.0F) && BuiltInRegistries.ITEM.getKey(tool.getItem()).getPath().endsWith("_pickaxe")) {
                     double cost = autismclient.ai.engine.MiningCost.breakTicks(mc, tool, candidate, block.defaultDestroyTime());
                     if (cost > slowest) {
                        slowest = cost;
                        state = candidate;
                        slot = toolSlot;
                     }
                  }
               }
            }

            if (state == null) {
               return null;
            }

            ItemStack current = mc.player.getInventory().getItem(slot);
            if (!(current.getDestroySpeed(state) <= 1.0F) && BuiltInRegistries.ITEM.getKey(current.getItem()).getPath().endsWith("_pickaxe")) {
               int blocks = AiJson.getInt(args, "count", 1);
               int held = AiJson.getInt(args, "held", 0);
               if (held > 0) {
                  int stocked = 0;

                  for (Item drop : wanted) {
                     stocked += AiGameState.countInventoryItem(BuiltInRegistries.ITEM.getKey(drop).toString());
                  }

                  blocks = Math.max(0, held - stocked);
               }

               if (blocks < 8) {
                  return null;
               }

               float hardness = state.getBlock().defaultDestroyTime();
               double before = autismclient.ai.engine.MiningCost.breakTicks(mc, current, state, hardness);
               AiCraftPlanner.RecipeGraph graph = AiCraft.buildRecipeGraph();
               Item chosen = null;
               AiCraftPlanner.CraftPlan chosenPlan = null;
               int chosenLogs = 0;
               double bestSaving = 0.0;
               boolean tableHeld = AiCraft.isCraftingTableInReach() || AiCraft.countItem("minecraft:crafting_table") > 0;
               BlockPos nearbyLog = null;

               for (BlockPos at : BlockPos.betweenClosed(mc.player.blockPosition().offset(-6, -2, -6), mc.player.blockPosition().offset(6, 3, 6))) {
                  if (AiTerrain.isLoaded(mc.level, at)
                     && mc.level.getBlockState(at).is(BlockTags.LOGS)
                     && init(mc, at)
                     && (nearbyLog == null || at.distSqr(mc.player.blockPosition()) < nearbyLog.distSqr(mc.player.blockPosition()))) {
                     nearbyLog = at.immutable();
                  }
               }

               BlockPos wood = nearbyLog;
               final String logId = wood == null ? "" : BuiltInRegistries.ITEM.getKey(mc.level.getBlockState(wood).getBlock().asItem()).toString();

               for (Item tool : List.of(Items.STONE_PICKAXE, Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE)) {
                  ItemStack upgraded = new ItemStack(tool);
                  if (upgraded.isCorrectToolForDrops(state) && AiItems.canDropWantedItem(mc, state, upgraded, wanted)) {
                     double after = autismclient.ai.engine.MiningCost.breakTicks(mc, upgraded, state, hardness);
                     if (!(after >= before)) {
                        String toolId = BuiltInRegistries.ITEM.getKey(tool).toString();
                        AiCraftPlanner.CraftPlan plan = null;
                        int logs = 0;
                        int cobble = 0;

                        while (logs <= (wood == null ? 0 : 3)) {
                           plan = init(graph, toolId, tableHeld, logId, logs);
                           cobble = plan.missing().getOrDefault("minecraft:cobblestone", 0);
                           if (plan.complete() || plan.missing().size() == 1 && cobble > 0 && cobble <= 3) {
                              break;
                           }

                           logs++;
                        }

                        if (plan != null
                           && (plan.complete() || plan.missing().size() == 1 && cobble <= 3 && cobble != 0)
                           && logs <= (wood == null ? 0 : 3)
                           && (cobble <= 0 || !(AiTerrain.nearestBlockDistSqr(mc, List.of(Blocks.STONE, Blocks.COBBLESTONE), 8) > 64.0))) {
                           double woodTicks = 0.0;
                           if (logs > 0) {
                              BlockState logState = mc.level.getBlockState(wood);
                              int woodSlot = autismclient.ai.engine.MiningCost.load(mc, logState, true);
                              woodTicks = logs
                                    * autismclient.ai.engine.MiningCost.breakTicks(
                                       mc,
                                       woodSlot < 0 ? ItemStack.EMPTY : mc.player.getInventory().getItem(woodSlot),
                                       logState,
                                       logState.getBlock().defaultDestroyTime()
                                    )
                                 + Math.sqrt(wood.distSqr(mc.player.blockPosition())) * 8.0
                                 + 30.0;
                           }

                           double setup = 40 + (tableHeld ? 0 : 60) + cobble * before + (cobble > 0 ? 30 : 0) + woodTicks;
                           double saved = (blocks - cobble) * (before - after) - setup;
                           if (init(blocks, before, after, setup, cobble) && saved > bestSaving) {
                              bestSaving = saved;
                              chosen = tool;
                              chosenPlan = plan;
                              chosenLogs = logs;
                           }
                        }
                     }
                  }
               }

               if (chosen == null) {
                  return null;
               }

               final String tool = BuiltInRegistries.ITEM.getKey(chosen).toString();
               final int cobble = chosenPlan.missing().getOrDefault("minecraft:cobblestone", 0);
               final int logs = chosenLogs;
               final JsonObject resume = args.deepCopy();
               resume.addProperty("toolPrepared", true);
               return new AiOperation() {
                  final long init_J = System.nanoTime() + Math.max(20, AiJson.getInt(args, "timeoutSeconds", 600)) * 1000000000L;
                  AiOperation init_AiOperation;
                  int init_I;
                  boolean init_Z;

                  @Override
                  public AiOperation.Result tick() {
                     if (this.init_Z) {
                        return AiGameOperations.result(AiOperation.Status.CANCELLED, "Mining cancelled", new JsonObject());
                     }

                     if (System.nanoTime() >= this.init_J) {
                        boolean uncertain = this.requiresReconciliationAfterEmergency();
                        this.cancel();
                        return AiGameOperations.result(
                           uncertain ? AiOperation.Status.UNCERTAIN : AiOperation.Status.TIMED_OUT,
                           "Mining preparation exceeded the job deadline",
                           new JsonObject()
                        );
                     }

                     int seconds = (int)Math.max(1L, (this.init_J - System.nanoTime()) / 1000000000L);
                     if (this.init_AiOperation != null) {
                        AiOperation.Result r = this.init_AiOperation.tick();
                        if (!r.terminal()) {
                           return r;
                        }

                        boolean uncertain = r.status() == AiOperation.Status.UNCERTAIN || this.init_AiOperation.requiresReconciliationAfterEmergency();
                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                        if (uncertain || r.status() == AiOperation.Status.CANCELLED) {
                           return r;
                        }

                        if (this.init_I == 4) {
                           return r;
                        }

                        if (r.status() != AiOperation.Status.SUCCEEDED) {
                           this.init_I = 3;
                        }
                     }

                     if (this.init_I == 0 && logs > 0) {
                        this.init_AiOperation = autismclient.ai.engine.AiBlockMine.create(
                           AiJson.objectOf(
                              "block",
                              logId,
                              "drops",
                              autismclient.ai.engine.AiBlockMine.init(List.of(logId)),
                              "held",
                              AiCraft.countItem(logId) + logs,
                              "toolPrepared",
                              true,
                              "explore",
                              false,
                              "radius",
                              16,
                              "timeoutSeconds",
                              Math.min(20, seconds)
                           )
                        );
                        this.init_I = 1;
                     } else if (this.init_I < 2 && cobble > 0) {
                        this.init_AiOperation = autismclient.ai.engine.AiBlockMine.create(
                           AiJson.objectOf(
                              "blocks",
                              autismclient.ai.engine.AiBlockMine.init(List.of("minecraft:stone", "minecraft:cobblestone")),
                              "drops",
                              autismclient.ai.engine.AiBlockMine.init(List.of("minecraft:cobblestone")),
                              "held",
                              AiCraft.countItem("minecraft:cobblestone") + cobble,
                              "toolPrepared",
                              true,
                              "explore",
                              false,
                              "radius",
                              16,
                              "timeoutSeconds",
                              Math.min(20, seconds)
                           )
                        );
                        this.init_I = 2;
                     } else if (this.init_I < 3) {
                        this.init_AiOperation = AiCraft.craft(
                           AiJson.objectOf("item", tool, "count", AiCraft.countItem(tool) + 1, "timeoutSeconds", Math.min(20, seconds))
                        );
                        this.init_I = 3;
                     } else {
                        resume.addProperty("timeoutSeconds", seconds);
                        this.init_AiOperation = autismclient.ai.engine.AiBlockMine.create(resume);
                        this.init_I = 4;
                     }

                     return AiGameOperations.result(
                        AiOperation.Status.RUNNING, this.init_I < 4 ? "Prepare a faster mining tool" : "Continue the mining batch", new JsonObject()
                     );
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
            } else {
               return null;
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   static AiMiningPreparation.Inner_DataUtil init(
      AiCraftPlanner.RecipeGraph graph,
      boolean tableHeld,
      String log,
      Set<String> goalLogs,
      int remaining,
      int nearbyLogs,
      double before,
      double after,
      double setup,
      double logTicks,
      int axeUses
   ) {
      AiMiningPreparation.Inner_DataUtil best = null;
      if (remaining > 0 && !(after >= before) && axeUses > 0 && Double.isFinite(before)) {
         for (int extra = 0; extra <= Math.min(3, Math.max(0, nearbyLogs)) && (extra <= 0 || !log.isEmpty()); extra++) {
            AiCraftPlanner.CraftPlan plan = init(graph, "minecraft:wooden_axe", tableHeld, log, extra);
            if (plan.complete()
               && !plan.steps()
                  .stream()
                  .anyMatch(
                     step -> !step.recipe().station().isEmpty() || step.recipe().inputs().stream().flatMap(Collection::stream).anyMatch(input -> !init(input))
                  )) {
               long consumed = 0L;

               for (AiCraftPlanner.CraftStep step : plan.steps()) {
                  for (List<String> slot : step.recipe().inputs()) {
                     if (slot.stream().anyMatch(goalLogs::contains)) {
                        consumed += step.crafts();
                     }
                  }
               }

               int credited = goalLogs.contains(log) ? extra : 0;
               int afterCraft = (int)Math.min(2147483647L, Math.max(0L, (long)remaining - credited + consumed));
               int fast = Math.min(afterCraft, axeUses);
               double cost = setup + extra * logTicks + (extra > 0 ? 30 : 0) + fast * after + (afterCraft - fast) * before;
               double saving = remaining * before - cost;
               if (saving > 0.0 && (best == null || saving > best.savingTicks())) {
                  best = new AiMiningPreparation.Inner_DataUtil(plan, extra, (int)Math.min(2147483647L, consumed), afterCraft, saving);
               }
            }
         }

         return best;
      } else {
         return null;
      }
   }

   private static boolean init(String id) {
      return id.equals("minecraft:wooden_axe")
         || id.equals("minecraft:crafting_table")
         || id.equals("minecraft:stick")
         || id.endsWith("_planks")
         || id.endsWith("_log")
         || id.endsWith("_wood")
         || id.endsWith("_stem")
         || id.endsWith("_hyphae");
   }

   static boolean init(BlockPos origin, BlockPos log, boolean loaded, boolean reachable, boolean easy) {
      return loaded && reachable && easy && log.getY() >= origin.getY() && log.distSqr(origin) <= 36.0;
   }

   private static AiOperation init(Minecraft mc, JsonObject args, Set<Item> wanted, Set<Block> blocks) {
      int held = AiJson.getInt(args, "held", 0);
      if (held > 0 && !blocks.isEmpty() && mc.player.onGround() && !mc.player.isInWater() && !mc.player.isInLava()) {
         Set<String> goalLogs = new HashSet<>();

         for (Item item : wanted) {
            if (!(item instanceof BlockItem block) || !block.getBlock().defaultBlockState().is(BlockTags.LOGS)) {
               return null;
            }

            goalLogs.add(BuiltInRegistries.ITEM.getKey(item).toString());
         }

         double before = 1.0 / 0.0;
         double after = 0.0;
         ItemStack axe = new ItemStack(Items.WOODEN_AXE);
         autismclient.ai.engine.MiningCost.Inner_DataUtil factors = autismclient.ai.engine.MiningCost.Inner_DataUtil.of(mc);

         for (Block block : blocks) {
            BlockState state = block.defaultBlockState();
            if (state.is(BlockTags.LOGS) && !state.requiresCorrectToolForDrops() && !(block.defaultDestroyTime() <= 0.0F)) {
               if (wanted.isEmpty()) {
                  goalLogs.add(BuiltInRegistries.ITEM.getKey(block.asItem()).toString());
               }

               int slot = autismclient.ai.engine.MiningCost.bestToolSlot(mc, state, true, (s, tool) -> AiItems.canDropWantedItem(mc, s, tool, wanted));
               ItemStack current = slot < 0 ? ItemStack.EMPTY : mc.player.getInventory().getItem(slot);
               if (!(current.getDestroySpeed(state) > 1.0F) && AiItems.canDropWantedItem(mc, state, axe, wanted)) {
                  before = Math.min(before, autismclient.ai.engine.MiningCost.breakTicks(mc, current, state, block.defaultDestroyTime(), factors));
                  after = Math.max(after, autismclient.ai.engine.MiningCost.breakTicks(mc, axe, state, block.defaultDestroyTime(), factors));
                  continue;
               }

               return null;
            }

            return null;
         }

         int stocked = goalLogs.stream().mapToInt(AiCraft::countItem).sum();
         int remaining = Math.max(0, held - stocked);
         if (remaining == 0) {
            return null;
         }

         BlockPos origin = mc.player.blockPosition().immutable();
         Map<String, List<BlockPos>> local = new LinkedHashMap<>();

         for (BlockPos at : BlockPos.betweenClosed(origin.offset(-6, 0, -6), origin.offset(6, 3, 6))) {
            if (AiTerrain.isLoaded(mc.level, at)
               && mc.level.getBlockState(at).is(BlockTags.LOGS)
               && !(at.distSqr(origin) > 36.0)
               && init(mc, at, origin.getY())
               && init(origin, at, true, autismclient.ai.engine.AiBlockInteraction.findReachableHit(mc, at) != null, init(mc, at))) {
               String log = BuiltInRegistries.ITEM.getKey(mc.level.getBlockState(at).getBlock().asItem()).toString();
               if (goalLogs.contains(log)) {
                  local.computeIfAbsent(log, ignored -> new ArrayList<>()).add(at.immutable());
               }
            }
         }

         AiCraftPlanner.RecipeGraph graph = AiCraft.buildRecipeGraph();
         boolean tableHeld = AiCraft.isCraftingTableInReach() || AiCraft.countItem("minecraft:crafting_table") > 0;
         if (!AiCraft.isCraftingTableInReach() && AiCraft.findPlacementSpot() == null) {
            return null;
         }

         double setup = 40 + (tableHeld ? 0 : 60);
         AiMiningPreparation.Inner_DataUtil chosen = init(graph, tableHeld, "", goalLogs, remaining, 0, before, after, setup, before, Math.max(1, axe.getMaxDamage() - 2));
         String chosenLog = "";

         for (Entry<String, List<BlockPos>> entry : local.entrySet()) {
            BlockState state = mc.level.getBlockState(entry.getValue().getFirst());
            double logTicks = autismclient.ai.engine.MiningCost.breakTicks(mc, ItemStack.EMPTY, state, state.getBlock().defaultDestroyTime(), factors);
            double walk = Math.sqrt(entry.getValue().stream().mapToDouble(at -> at.distSqr(origin)).max().orElse(0.0)) * 8.0;
            AiMiningPreparation.Inner_DataUtil candidate = init(
               graph,
               tableHeld,
               entry.getKey(),
               goalLogs,
               remaining,
               entry.getValue().size(),
               before,
               after,
               setup + walk,
               logTicks,
               Math.max(1, axe.getMaxDamage() - 2)
            );
            if (candidate != null && (chosen == null || candidate.savingTicks() > chosen.savingTicks())) {
               chosen = candidate;
               chosenLog = entry.getKey();
            }
         }

         if (chosen == null) {
            return null;
         }

         String log = chosenLog;
         int extra = chosen.extraLogs();
         Object world = mc.level;
         Object player = mc.player;
         Object connection = mc.getConnection();
         long deadline = System.nanoTime() + Math.max(1, AiJson.getInt(args, "timeoutSeconds", 600)) * 1000000000L;
         JsonObject resume = args.deepCopy();
         resume.addProperty("toolPrepared", true);
         IntSupplier seconds = () -> (int)Math.max(1L, (deadline - System.nanoTime()) / 1000000000L);
         AiOperation gather = extra == 0
            ? null
            : autismclient.ai.engine.AiBlockMine.create(
               AiJson.objectOf(
                  "block",
                  log,
                  "drops",
                  autismclient.ai.engine.AiBlockMine.init(List.of(log)),
                  "held",
                  AiCraft.countItem(log) + extra,
                  "toolPrepared",
                  true,
                  "trees",
                  false,
                  "explore",
                  false,
                  "radius",
                  16,
                  "timeoutSeconds",
                  Math.min(20, seconds.getAsInt())
               )
            );
         return init(
            gather,
            () -> AiCraft.craft(
               AiJson.objectOf(
                  "item", "minecraft:wooden_axe", "count", AiCraft.countItem("minecraft:wooden_axe") + 1, "timeoutSeconds", Math.min(20, seconds.getAsInt())
               )
            ),
            () -> autismclient.ai.engine.AiBlockMine.create(init(resume, seconds.getAsInt())),
            () -> mc.player.blockPosition().distSqr(origin) <= 36.0,
            () -> mc.level == world && mc.player == player && mc.getConnection() == connection,
            deadline
         );
      } else {
         return null;
      }
   }

   static JsonObject init(JsonObject args, int seconds) {
      JsonObject resume = args.deepCopy();
      resume.addProperty("toolPrepared", true);
      resume.addProperty("timeoutSeconds", seconds);
      return resume;
   }

   private static boolean init(Minecraft mc, BlockPos log, int floorY) {
      for (int y = log.getY() - 1; y >= floorY - 1; y--) {
         BlockPos at = new BlockPos(log.getX(), y, log.getZ());
         if (!AiTerrain.isLoaded(mc.level, at) || !mc.level.getFluidState(at).isEmpty()) {
            return false;
         }

         BlockState state = mc.level.getBlockState(at);
         if (!state.is(BlockTags.LOGS) && !state.is(BlockTags.LEAVES) && !state.getCollisionShape(mc.level, at).isEmpty()) {
            return true;
         }
      }

      return false;
   }

   static AiOperation init(
      final AiOperation gather,
      final Supplier<AiOperation> craft,
      final Supplier<AiOperation> resume,
      final BooleanSupplier local,
      final BooleanSupplier valid,
      final long deadline
   ) {
      return new AiOperation() {
         AiOperation init_AiOperation = gather;
         AiOperation.Result init_AiOperation_Result;
         AiOperation.Result load_AiOperation_Result;
         int init_I = gather == null ? 1 : 0;
         boolean init_Z;
         boolean load_Z;

         @Override
         public AiOperation.Result tick() {
            if (this.load_AiOperation_Result != null) {
               return this.load_AiOperation_Result;
            }

            if (this.init_Z) {
               return this.init(AiOperation.Status.CANCELLED, "Wood preparation cancelled");
            }

            if (!valid.getAsBoolean()) {
               return this.init(AiOperation.Status.UNCERTAIN, "World, connection or player changed during wood preparation");
            }

            if (System.nanoTime() >= deadline) {
               return this.init(AiOperation.Status.TIMED_OUT, "Wood preparation exceeded the mining deadline");
            }

            if (this.init_I == 0 && !local.getAsBoolean()) {
               AiOperation.Result refused = AiOperation.Result.failed(
                  AiFailure.UNREACHABLE, "Optional wood stock is no longer nearby", this.init_AiOperation_Result == null ? "{}" : this.init_AiOperation_Result.dataJson()
               );
               refused = AiNetherRun.init_AiOperation_Result_AiOperation_AiOperation_Result(this.init_AiOperation, refused);
               this.init_AiOperation = null;
               this.init_AiOperation_Result = refused;
               if (refused.status() == AiOperation.Status.UNCERTAIN) {
                  return this.init(refused.status(), refused.message());
               }

               this.init_I = 2;
               this.init_AiOperation_Result = null;
            }

            if (this.init_AiOperation == null) {
               this.init_AiOperation = this.init_I == 1 ? craft.get() : resume.get();
               this.init_AiOperation_Result = null;
               return AiGameOperations.result(
                  AiOperation.Status.RUNNING, this.init_I == 1 ? "Crafting an axe for the log batch" : "Continuing the log batch", new JsonObject()
               );
            }

            AiOperation.Result result = this.init_AiOperation.tick();
            this.init_AiOperation_Result = result;
            if (!result.terminal()) {
               return result;
            }

            result = AiNetherRun.init_AiOperation_Result_AiOperation_AiOperation_Result(this.init_AiOperation, result);
            this.init_AiOperation = null;
            this.init_AiOperation_Result = result;
            if (result.status() == AiOperation.Status.UNCERTAIN || result.status() == AiOperation.Status.CANCELLED) {
               return this.init(result.status(), result.message());
            }

            if (this.init_I == 2) {
               return this.init(result.status(), result.message());
            }

            this.init_I = result.status() == AiOperation.Status.SUCCEEDED ? this.init_I + 1 : 2;
            this.init_AiOperation_Result = null;
            return AiGameOperations.result(
               AiOperation.Status.RUNNING, this.init_I == 1 ? "Preparing a faster axe" : "Continuing the log batch", new JsonObject()
            );
         }

         AiOperation.Result init(AiOperation.Status status, String message) {
            this.load_Z = this.load_Z | this.requiresReconciliationAfterEmergency();
            this.load_AiOperation_Result = AiNetherRun.init_AiOperation_Result_AiOperation_Status(
               status,
               message,
               AiJson.objectOf("investment", "minecraft:wooden_axe", "stage", this.init_I),
               this.load_Z,
               this.init_AiOperation_Result
            );
            if (this.init_AiOperation_Result != null && this.init_AiOperation_Result.terminal()) {
               this.load_AiOperation_Result = new AiOperation.Result(
                  this.load_AiOperation_Result.status(),
                  this.load_AiOperation_Result.message(),
                  this.load_AiOperation_Result.dataJson(),
                  this.init_AiOperation_Result.evidence(),
                  this.init_AiOperation_Result.failure()
               );
            }

            this.cancel();
            return this.load_AiOperation_Result;
         }

         @Override
         public boolean canInterruptNow() {
            return !this.load_Z && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow());
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            JsonObject evidence = this.init_AiOperation_Result == null ? new JsonObject() : AiRunResources.parseEvidenceJson(this.init_AiOperation_Result.dataJson());
            return this.load_Z
               || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency()
               || AiJson.getBoolean(evidence, "reobserveBeforeResuming", false)
               || AiJson.getBoolean(evidence, "interruptedActionNeedsReconciliation", false);
         }

         @Override
         public void cancel() {
            if (!this.init_Z) {
               this.load_Z = this.load_Z | this.requiresReconciliationAfterEmergency();
               this.init_Z = true;
               if (this.init_AiOperation != null) {
                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
               }
            }
         }
      };
   }

   private static boolean init(Minecraft mc, BlockPos log) {
      int top = mc.level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, log.getX(), log.getZ());

      for (int y = log.getY() + 1; y < top && y <= log.getY() + 64; y++) {
         BlockPos at = new BlockPos(log.getX(), y, log.getZ());
         if (!AiTerrain.isLoaded(mc.level, at)) {
            return false;
         }

         BlockState state = mc.level.getBlockState(at);
         if (!state.isAir() && !state.is(BlockTags.LOGS) && !state.is(BlockTags.LEAVES) && !state.getCollisionShape(mc.level, at).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   static AiCraftPlanner.CraftPlan init(final AiCraftPlanner.RecipeGraph graph, final String tool, final boolean tableHeld, final String log, final int extraLogs) {
      String root = "autism:prepared_mining_tool";
      return AiCraftPlanner.init(
         new AiCraftPlanner.RecipeGraph() {
            @Override
            public List<AiCraftPlanner.RecipeSpec> recipesFor(String id) {
               return id.equals("autism:prepared_mining_tool")
                  ? List.of(
                     new AiCraftPlanner.RecipeSpec(
                        "autism:prepared_mining_tool",
                        "autism:prepared_mining_tool",
                        1,
                        false,
                        tableHeld ? List.of(List.of(tool)) : List.of(List.of("minecraft:crafting_table"), List.of(tool))
                     )
                  )
                  : graph.recipesFor(id);
            }

            @Override
            public int held(String id) {
               return !id.equals("autism:prepared_mining_tool") && !id.equals(tool) ? graph.held(id) + (id.equals(log) ? extraLogs : 0) : 0;
            }

            @Override
            public int load_I(String id) {
               return graph.load_I(id);
            }

            @Override
            public double init_D(String id) {
               return id.equals("minecraft:cobblestone") ? 0.0 : graph.init_D(id);
            }

            @Override
            public String init(Set<String> ids) {
               return graph.init(ids);
            }
         },
         "autism:prepared_mining_tool",
         1
      );
   }

   record Inner_DataUtil(AiCraftPlanner.CraftPlan plan, int extraLogs, int consumedGoalLogs, int remainingAfterCraft, double savingTicks) {
   }
}
