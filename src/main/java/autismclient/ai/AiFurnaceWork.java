package autismclient.ai;

import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

final class AiFurnaceWork implements AiOperation {
   private final Minecraft mc = Minecraft.getInstance();
   private final Object level = this.mc.level;
   private final Object connection = this.mc.getConnection();
   private final Object player = this.mc.player;
   private final BlockPos station;
   private final BlockPos home;
   private final String input;
   private final int cookBudgetTicks;
   private final AiFurnaceWork.ResourcePlan plan;
   private final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
   private final autismclient.ai.engine.AiBlockDigger init_RefQueue_DataUtil;
   private final Set<BlockPos> init_Set = new HashSet<>();
   private Map<BlockPos, BlockPos> route = Map.of();
   private AiOperation init_AiOperation;
   private AiOperation load_AiOperation;
   private AiOperation mineOp;
   private AiOperation craftOp;
   private AiFurnaceReturn furnaceReturn;
   private String preparingItem;
   private final Set<String> preparedItems = new HashSet<>();
   private final JsonArray preparationReceipts = new JsonArray();
   private BlockPos read_BlockPos;
   private BlockPos write_BlockPos;
   private int ticks;
   private int read_I;
   private int write_I;
   private int lastRouteTick = -100;
   private int nextCollectTick;
   private boolean init_Z;
   private boolean load_Z;
   private boolean read_Z;
   private int resourceSearchStops;
   private final Set<BlockPos> read_Set = new HashSet<>();
   private final JsonArray load_JsonArray = new JsonArray();
   private String activity = "planning useful furnace work";

   static boolean init_Z_Q_DataUtil(AiFurnaceWork.ResourcePlan plan) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         AiCraftPlanner.RecipeGraph graph = AiCraft.buildRecipeGraph();

         for (Entry<String, Integer> preparation : plan.crafts().entrySet()) {
            AiCraftPlanner.CraftPlan craft = AiCraftPlanner.plan(graph, preparation.getKey(), preparation.getValue());
            if (craft.complete() && !craft.satisfied() && !craft.requiresTable()) {
               return true;
            }
         }

         BlockPos home = mc.player.blockPosition();

         for (BlockPos p : BlockPos.betweenClosed(home.offset(-8, -2, -8), home.offset(8, 3, 8))) {
            if (AiTerrain.isLoaded(mc.level, p)) {
               for (Entry<String, Integer> resource : plan.resources().entrySet()) {
                  if (AiGameState.countInventoryItem(resource.getKey()) < resource.getValue()
                     && AiItems.resolveItem(resource.getKey()).items().stream().anyMatch(item -> AiItems.sourceBlocksFor(item).contains(mc.level.getBlockState(p).getBlock()))
                     && autismclient.ai.engine.MiningCost.bestToolSlot(mc, mc.level.getBlockState(p), true) >= 0) {
                     return true;
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   AiFurnaceWork(BlockPos station, BlockPos home, String input, int cookingTicks) {
      this(station, home, input, cookingTicks, AiFurnaceWork.ResourcePlan.forInput(input));
   }

   AiFurnaceWork(BlockPos station, BlockPos home, String input, int cookingTicks, AiFurnaceWork.ResourcePlan plan) {
      this.station = station.immutable();
      this.home = home.immutable();
      this.input = input;
      this.plan = plan;
      this.cookBudgetTicks = Math.max(0, cookingTicks - 30);
      this.init_RefQueue_DataUtil = new autismclient.ai.engine.AiBlockDigger(
         this.mc,
         slot -> this.mc.player.getInventory().setSelectedSlot(slot),
         this.aim::aimAt,
         true,
         true,
         this.aim::tick
      );
   }

   static Map<BlockPos, BlockPos> routeTree(BlockPos home, Predicate<BlockPos> safe, Predicate<BlockPos> clear) {
      Map<BlockPos, BlockPos> parents = new LinkedHashMap<>();
      if (!safe.test(home)) {
         return parents;
      }

      ArrayDeque<BlockPos> queue = new ArrayDeque<>();
      parents.put(home, home);
      queue.add(home);

      while (!queue.isEmpty() && parents.size() < 512) {
         BlockPos from = queue.remove();

         for (Direction d : Plane.HORIZONTAL) {
            for (int dy = -1; dy <= 1; dy++) {
               BlockPos to = from.relative(d).above(dy);
               if (Math.abs(to.getX() - home.getX()) + Math.abs(to.getZ() - home.getZ()) <= 12
                  && Math.abs(to.getY() - home.getY()) <= 4
                  && !parents.containsKey(to)
                  && safe.test(to)
                  && (dy <= 0 || clear.test(from.above(2)))
                  && (dy >= 0 || clear.test(to.above(2)))) {
                  parents.put(to, from);
                  queue.add(to);
               }
            }
         }
      }

      return parents;
   }

   static int routeLength(Map<BlockPos, BlockPos> route, BlockPos from) {
      int steps;
      for (steps = 0; route.containsKey(from) && !route.get(from).equals(from) && steps < 512; steps++) {
         from = route.get(from);
      }

      return route.containsKey(from) ? steps * 12 + 30 : 2147483647;
   }

   static boolean routeStandsOn(Map<BlockPos, BlockPos> route, BlockPos from, BlockPos block) {
      for (int steps = 0; route.containsKey(from) && steps < 512; steps++) {
         if (from.below().equals(block)) {
            return true;
         }

         BlockPos parent = route.get(from);
         if (parent.equals(from)) {
            break;
         }

         from = parent;
      }

      return false;
   }

   private boolean init_Z_BlockPos_2(BlockPos p) {
      if (!AiTerrain.isLoaded(this.mc.level, p)) {
         return false;
      }

      BlockState s = this.mc.level.getBlockState(p);
      return s.getCollisionShape(this.mc.level, p).isEmpty() && s.getFluidState().isEmpty() && !s.is(Blocks.FIRE) && !s.is(Blocks.SOUL_FIRE);
   }

   private boolean isStandable(BlockPos p) {
      if (this.init_Z_BlockPos_2(p) && this.init_Z_BlockPos_2(p.above()) && AiTerrain.isLoaded(this.mc.level, p.below())) {
         BlockState floor = this.mc.level.getBlockState(p.below());
         return floor.isCollisionShapeFullBlock(this.mc.level, p.below())
            && floor.getFluidState().isEmpty()
            && !floor.is(Blocks.MAGMA_BLOCK)
            && !floor.is(Blocks.CAMPFIRE)
            && !floor.is(Blocks.SOUL_CAMPFIRE);
      } else {
         return false;
      }
   }

   @Override
   public AiOperation.Result tick() {
      if (this.load_Z
         || this.mc.level != this.level
         || this.mc.getConnection() != this.connection
         || this.mc.player != this.player
         || this.mc.player == null) {
         return this.init_AiOperation_Result_AiOperation_Status_Str_3(AiOperation.Status.CANCELLED, "Furnace work world/player changed");
      }

      if (!this.mc.player.isAlive()) {
         return this.init_AiOperation_Result_AiOperation_Status_Str_3(AiOperation.Status.FAILED, "Player died during furnace work");
      }

      this.ticks++;
      if (this.ticks - this.lastRouteTick >= 20 || this.route.isEmpty()) {
         this.route = routeTree(this.home, this::isStandable, this::init_Z_BlockPos_2);
         this.lastRouteTick = this.ticks;
      }

      if ((long)this.ticks + routeLength(this.route, this.mc.player.blockPosition()) >= this.cookBudgetTicks) {
         this.init_Z = true;
      }

      if (this.craftOp != null) {
         AiOperation.Result r = this.craftOp.tick();
         if (!r.terminal()) {
            return this.running("Preparing " + this.preparingItem + " while the furnace cooks: " + r.message());
         }

         this.preparationReceipts.add(AiJson.objectOf("item", this.preparingItem, "status", r.status().name(), "receipt", AiRunResources.parseEvidenceJson(r.dataJson())));
         boolean unsettled = r.status() == AiOperation.Status.UNCERTAIN || this.craftOp.requiresReconciliationAfterEmergency();
         this.craftOp.cancel();
         this.craftOp = null;
         return unsettled
            ? this.init_AiOperation_Result_AiOperation_Status_Str_3(AiOperation.Status.UNCERTAIN, "Furnace preparation needs inventory reconciliation")
            : this.running("Furnace-time preparation finished; continue useful work");
      } else if (this.init_Z) {
         this.init_RefQueue_DataUtil.stopCompat();
         this.aim.clear();
         if (this.init_AiOperation != null) {
            if (!this.init_AiOperation.canInterruptNow()) {
               AiOperation.Result flight = this.init_AiOperation.tick();
               if (!flight.terminal()) {
                  return this.running("Finish committed movement before returning: " + flight.message());
               }

               if (flight.status() == AiOperation.Status.UNCERTAIN || this.init_AiOperation.requiresReconciliationAfterEmergency()) {
                  return this.init_AiOperation_Result_AiOperation_Status_Str_3(AiOperation.Status.UNCERTAIN, "Furnace excursion movement needs reconciliation");
               }
            }

            this.init_AiOperation.cancel();
            this.init_AiOperation = null;
         }

         if (this.furnaceReturn == null) {
            if (this.load_AiOperation != null) {
               this.load_AiOperation.cancel();
               this.load_AiOperation = null;
            }

            if (this.mineOp != null) {
               this.mineOp.cancel();
               this.mineOp = null;
            }

            this.furnaceReturn = new AiFurnaceReturn(
               this.station,
               this.route.containsKey(this.mc.player.blockPosition())
                  ? "Batch collection due"
                  : "Terrain changed or survival moved the body off the planned return"
            );
         }

         AiOperation.Result r = this.furnaceReturn.tick();
         this.activity = r.message();
         return r.terminal() ? this.init_AiOperation_Result_AiOperation_Status_Str_3(r.status(), r.message()) : this.running(r.message());
      } else {
         if (this.init_AiOperation != null) {
            AiOperation.Result r = this.init_AiOperation.tick();
            if (!r.terminal()) {
               return this.running(this.activity + ": " + r.message());
            }

            if (r.status() == AiOperation.Status.UNCERTAIN || this.init_AiOperation.requiresReconciliationAfterEmergency()) {
               return this.init_AiOperation_Result_AiOperation_Status_Str_3(
                  AiOperation.Status.UNCERTAIN, "Productive furnace travel needs reconciliation: " + r.message()
               );
            }

            this.init_AiOperation.cancel();
            this.init_AiOperation = null;
            if (r.status() != AiOperation.Status.SUCCEEDED) {
               this.init_Set.add(this.read_BlockPos == null ? this.write_BlockPos : this.read_BlockPos);
               this.read_BlockPos = null;
            }
         }

         if (this.read_BlockPos != null) {
            if (this.mc.level.getBlockState(this.read_BlockPos).isAir()) {
               this.read_I++;
               this.read_BlockPos = null;
               this.init_RefQueue_DataUtil.stopCompat();
            } else if (this.isSafeToBreak(this.read_BlockPos) && autismclient.ai.engine.AiBlockInteraction.findReachableHit(this.mc, this.read_BlockPos) != null) {
               autismclient.ai.engine.AiBlockDigger.DigOutcome r = this.init_RefQueue_DataUtil.digCompat(this.read_BlockPos);
               this.aim.tick();
               if (r == autismclient.ai.engine.AiBlockDigger.DigOutcome.init) {
                  return this.running("Mining needed " + this.init_Str_BlockPos(this.read_BlockPos) + " while the furnace cooks");
               }

               this.init_RefQueue_DataUtil.stopCompat();
               if (r == autismclient.ai.engine.AiBlockDigger.DigOutcome.load) {
                  this.read_I++;
               }

               this.read_BlockPos = null;
            } else {
               this.read_BlockPos = null;
               this.init_RefQueue_DataUtil.stopCompat();
            }
         }

         if (this.plan.resources().entrySet().stream().allMatch(e -> AiGameState.countInventoryItem(e.getKey()) >= e.getValue())
            && this.plan.crafts().entrySet().stream().allMatch(e -> this.preparedItems.contains(e.getKey()) || AiGameState.countInventoryItem(e.getKey()) >= e.getValue())) {
            this.init_Z = true;
            return this.tick();
         }

         if (this.ticks + routeLength(this.route, this.mc.player.blockPosition()) + 120 < this.cookBudgetTicks) {
            for (Entry<String, Integer> preparation : this.plan.crafts().entrySet()) {
               if (this.preparedItems.add(preparation.getKey())) {
                  AiCraftPlanner.CraftPlan recipe = AiCraftPlanner.plan(AiCraft.buildRecipeGraph(), preparation.getKey(), preparation.getValue());
                  if (recipe.complete()
                     && !recipe.satisfied()
                     && !recipe.requiresTable()
                     && !recipe.steps()
                        .stream()
                        .anyMatch(s -> !s.recipe().station().isEmpty() || s.recipe().inputs().stream().anyMatch(variants -> variants.contains(this.input)))) {
                     this.preparingItem = preparation.getKey();
                     this.craftOp = AiCraft.craft(AiJson.objectOf("item", this.preparingItem, "count", preparation.getValue(), "timeoutSeconds", 6));
                     return this.running("Prepare " + this.preparingItem + " during furnace time");
                  }
               }
            }
         }

         for (ItemEntity drop : this.mc.level.getEntitiesOfClass(ItemEntity.class, this.mc.player.getBoundingBox().inflate(10.0))) {
            String id = BuiltInRegistries.ITEM.getKey(drop.getItem().getItem()).toString();
            if (this.plan.resources().containsKey(id) || id.equals("minecraft:cobblestone")) {
               BlockPos at = drop.blockPosition();
               if (this.route.containsKey(at)
                  && !drop.hasPickUpDelay()
                  && this.ticks + 2L * routeLength(this.route, at) + 40L < this.cookBudgetTicks
                  && !this.mc.player.getBoundingBox().inflate(1.0, 0.5, 1.0).intersects(drop.getBoundingBox())) {
                  this.write_I++;
                  this.write_BlockPos = at;
                  this.init_AiOperation = this.init_AiOperation_BlockPos(at);
                  this.activity = "collecting furnace-work drops";
                  return this.running(this.activity);
               }
            }
         }

         if (this.ticks >= this.nextCollectTick && AiItems.countFreeSlots() > 0 && this.read_I < 8) {
            this.nextCollectTick = this.ticks + 20;
            BlockPos best = null;
            BlockPos bestStand = null;
            double score = 1.7976931348623157E308;

            for (BlockPos at : BlockPos.betweenClosed(this.home.offset(-12, -2, -12), this.home.offset(12, 4, 12))) {
               if (!this.init_Set.contains(at) && this.matchesPlanResource(at) && this.isSafeToBreak(at)) {
                  for (BlockPos stand : this.route.keySet()) {
                     if (!stand.below().equals(at) && !routeStandsOn(this.route, stand, at)) {
                        Vec3 eye = Vec3.atBottomCenterOf(stand).add(0.0, this.mc.player.getEyeHeight(), 0.0);
                        if (!(eye.distanceToSqr(Vec3.atCenterOf(at)) > 9.0) && this.ticks + 2L * routeLength(this.route, stand) + 80L < this.cookBudgetTicks
                           )
                         {
                           BlockHitResult hit = this.mc
                              .level
                              .clip(new ClipContext(eye, Vec3.atCenterOf(at), Block.COLLIDER, Fluid.NONE, this.mc.player));
                           if (hit.getBlockPos().equals(at)) {
                              double cost = stand.distSqr(this.mc.player.blockPosition()) + (this.holdsInput(at) ? 0 : 8);
                              if (cost < score) {
                                 score = cost;
                                 best = at.immutable();
                                 bestStand = stand;
                              }
                           }
                        }
                     }
                  }
               }
            }

            if (best != null) {
               this.read_BlockPos = best;
               this.write_BlockPos = bestStand;
               this.init_Set.add(best);
               this.init_AiOperation = this.init_AiOperation_BlockPos(this.write_BlockPos);
               this.activity = "walking to useful furnace-time mining";
               return this.running(this.activity);
            }
         }

         if (this.resourceSearchStops >= 4 && !this.read_Z) {
            if (this.mineOp == null) {
               this.mineOp = AiGameplay.find(AiJson.objectOf("block", "ore", "radius", 64, "limit", 32));
            }

            AiOperation.Result scan = this.mineOp.tick();
            if (scan.terminal()) {
               JsonObject found = AiRunResources.parseEvidenceJson(scan.dataJson());
               if (found.has("results")) {
                  for (JsonElement lead : found.getAsJsonArray("results")) {
                     if (this.load_JsonArray.size() < 32) {
                        this.load_JsonArray.add(lead.deepCopy());
                     }
                  }
               }

               this.mineOp.cancel();
               this.init_Z = true;
            }

            return this.running("Locate the next ore from observed terrain while this batch finishes");
         } else if (!this.read_Z && this.resourceSearchStops < 4) {
            this.write_BlockPos = this.route
               .keySet()
               .stream()
               .filter(p -> Math.abs(p.getX() - this.home.getX()) + Math.abs(p.getZ() - this.home.getZ()) <= 8)
               .filter(p -> this.read_Set.stream().allMatch(old -> old.distSqr(p) > 25.0))
               .filter(p -> this.ticks + 2L * routeLength(this.route, p) + 60L < this.cookBudgetTicks)
               .max(Comparator.comparingDouble(p -> p.distSqr(this.home)))
               .orElse(this.home);
            if (this.write_BlockPos.equals(this.home)) {
               this.resourceSearchStops = 4;
               return this.running("No further safe walking viewpoint; prospect observed ore without leaving the return route");
            } else {
               this.read_Set.add(this.write_BlockPos);
               this.resourceSearchStops++;
               this.read_Z = true;
               this.init_AiOperation = this.init_AiOperation_BlockPos(this.write_BlockPos);
               this.activity = "surveying a clear route ahead while the furnace cooks";
               return this.running(this.activity);
            }
         } else {
            if (this.load_AiOperation == null) {
               JsonArray blocks = new JsonArray();

               for (String id : List.of(
                  "minecraft:iron_ore", "minecraft:deepslate_iron_ore", "minecraft:coal_ore", "minecraft:diamond_ore", "minecraft:deepslate_diamond_ore"
               )) {
                  blocks.add(id);
               }

               this.load_AiOperation = AiGameplay.survey(AiJson.objectOf("blocks", blocks, "radius", 16));
            }

            this.aim
               .aimAt(
                  this.mc
                     .player
                     .getEyePosition()
                     .add(Vec3.atCenterOf(this.write_BlockPos).subtract(Vec3.atCenterOf(this.home)).normalize().scale(8.0))
               );
            this.aim.tick();
            AiOperation.Result scan = this.load_AiOperation.tick();
            if (scan.terminal()) {
               JsonObject observed = AiRunResources.parseEvidenceJson(scan.dataJson());
               if (observed.has("matches")) {
                  for (JsonElement match : observed.getAsJsonArray("matches")) {
                     JsonObject fact = match.getAsJsonObject();
                     AiWorldMemory.observeChange(new BlockPos(AiJson.getInt(fact, "x", 0), AiJson.getInt(fact, "y", 0), AiJson.getInt(fact, "z", 0)));
                     if (this.load_JsonArray.size() < 32) {
                        this.load_JsonArray.add(fact.deepCopy());
                     }
                  }
               }

               this.load_AiOperation.cancel();
               this.load_AiOperation = null;
               this.read_Z = false;
            }

            return this.running("Checking observed resources along the next clear route");
         }
      }
   }

   private String init_Str_BlockPos(BlockPos at) {
      BlockState s = this.mc.level.getBlockState(at);
      if (s.is(Blocks.IRON_ORE) || s.is(Blocks.DEEPSLATE_IRON_ORE)) {
         return "minecraft:raw_iron";
      } else if (s.is(Blocks.GOLD_ORE) || s.is(Blocks.DEEPSLATE_GOLD_ORE)) {
         return "minecraft:raw_gold";
      } else if (s.is(Blocks.COPPER_ORE) || s.is(Blocks.DEEPSLATE_COPPER_ORE)) {
         return "minecraft:raw_copper";
      } else if (s.is(Blocks.COAL_ORE) || s.is(Blocks.DEEPSLATE_COAL_ORE)) {
         return "minecraft:coal";
      } else if (s.is(Blocks.DIAMOND_ORE) || s.is(Blocks.DEEPSLATE_DIAMOND_ORE)) {
         return "minecraft:diamond";
      } else if (s.is(Blocks.REDSTONE_ORE) || s.is(Blocks.DEEPSLATE_REDSTONE_ORE)) {
         return "minecraft:redstone";
      } else if (!s.is(Blocks.LAPIS_ORE) && !s.is(Blocks.DEEPSLATE_LAPIS_ORE)) {
         return !s.is(Blocks.EMERALD_ORE) && !s.is(Blocks.DEEPSLATE_EMERALD_ORE) ? "" : "minecraft:emerald";
      } else {
         return "minecraft:lapis_lazuli";
      }
   }

   private boolean holdsInput(BlockPos at) {
      return this.init_Str_BlockPos(at).equals(this.input);
   }

   private boolean matchesPlanResource(BlockPos at) {
      if (!AiTerrain.isLoaded(this.mc.level, at)) {
         return false;
      }

      String resource = this.init_Str_BlockPos(at);
      return this.plan.resources().containsKey(resource) && AiGameState.countInventoryItem(resource) < this.plan.resources().get(resource);
   }

   private boolean isSafeToBreak(BlockPos at) {
      if (!at.equals(this.station)
         && !at.equals(this.station.below())
         && !routeStandsOn(this.route, this.mc.player.blockPosition(), at)
         && !at.equals(this.mc.player.blockPosition().below())
         && (
            !(at.getY() + 1 <= this.mc.player.getBoundingBox().minY + 0.05)
               || !(at.getY() + 1 >= this.mc.player.getBoundingBox().minY - 0.05)
               || !new AABB(at).inflate(0.0, 0.1, 0.0).intersects(this.mc.player.getBoundingBox())
         )) {
         for (Direction d : Direction.values()) {
            BlockPos near = at.relative(d);
            if (!AiTerrain.isLoaded(this.mc.level, near)
               || !this.mc.level.getFluidState(near).isEmpty()
               || this.mc.level.getBlockState(near).getBlock() instanceof FallingBlock) {
               return false;
            }
         }

         return this.mc.level.getBlockEntity(at) == null
            && autismclient.ai.engine.MiningCost.bestToolSlot(this.mc, this.mc.level.getBlockState(at), true) >= 0;
      } else {
         return false;
      }
   }

   private AiOperation init_AiOperation_BlockPos(final BlockPos to) {
      final Set<BlockPos> permitted = Set.copyOf(this.route.keySet());
      autismclient.ai.engine.AiPathfinder.PathGoal goal = new autismclient.ai.engine.AiPathfinder.PathGoal() {
         @Override
         public boolean isInGoal(int x, int y, int z) {
            return to.equals(new BlockPos(x, y, z));
         }

         @Override
         public double heuristic(int x, int y, int z) {
            return Math.sqrt(to.distSqr(new BlockPos(x, y, z)));
         }

         @Override
         public boolean allowsCell(BlockPos p) {
            return permitted.contains(p);
         }

         @Override
         public boolean mayBreak(BlockPos p) {
            return false;
         }

         @Override
         public boolean mayPlace(BlockPos p) {
            return false;
         }

         @Override
         public Set<autismclient.ai.engine.AiPathfinder.MoveType> disallowedMoves() {
            return Set.of(
               autismclient.ai.engine.AiPathfinder.MoveType.JUMP,
               autismclient.ai.engine.AiPathfinder.MoveType.JUMP_GAP_2,
               autismclient.ai.engine.AiPathfinder.MoveType.JUMP_GAP_3,
               autismclient.ai.engine.AiPathfinder.MoveType.PARKOUR_ASCEND
            );
         }
      };
      return AiGoto.travelToCompat(
         to,
         0.6,
         false,
         false,
         false,
         20,
         autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST,
         false,
         0.0,
         goal,
         new AiGoto.GotoOptions(false, false, 1, true, false, false)
      );
   }

   JsonObject progressJson() {
      return AiJson.objectOf(
         "activity",
         this.activity,
         "home",
         this.home.toShortString(),
         "station",
         this.station.toShortString(),
         "ticks",
         this.ticks,
         "budgetTicks",
         this.cookBudgetTicks,
         "mined",
         this.read_I,
         "pickupRoutes",
         this.write_I,
         "returning",
         this.init_Z,
         "returnRouteCells",
         this.route.size(),
         "returnEditsAllowed",
         this.furnaceReturn != null && AiJson.getBoolean(this.furnaceReturn.json(), "returnEditsAllowed", false),
         "returnRecovery",
         this.furnaceReturn == null ? new JsonObject() : this.furnaceReturn.json(),
         "preparations",
         this.preparationReceipts,
         "resourceTargets",
         this.plan.resources(),
         "bodyCell",
         this.mc.player == null ? "" : this.mc.player.blockPosition().toShortString(),
         "bodyOnVerifiedRoute",
         this.mc.player != null && this.route.containsKey(this.mc.player.blockPosition()),
         "resourceSearchStops",
         this.resourceSearchStops,
         "observedResourceLeads",
         this.load_JsonArray
      );
   }

   private AiOperation.Result running(String message) {
      return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.progressJson());
   }

   private AiOperation.Result init_AiOperation_Result_AiOperation_Status_Str_3(AiOperation.Status s, String message) {
      JsonObject d = this.progressJson();
      this.cancel();
      return AiGameOperations.result(s, message, d);
   }

   @Override
   public boolean canInterruptNow() {
      return (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow())
         && (this.craftOp == null || this.craftOp.canInterruptNow())
         && (this.furnaceReturn == null || this.furnaceReturn.canInterruptNow());
   }

   @Override
   public boolean requiresReconciliationAfterEmergency() {
      return this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency()
         || this.craftOp != null && this.craftOp.requiresReconciliationAfterEmergency()
         || this.furnaceReturn != null && this.furnaceReturn.requiresReconciliationAfterEmergency();
   }

   @Override
   public void cancel() {
      this.load_Z = true;
      this.init_RefQueue_DataUtil.stopCompat();
      this.aim.clear();
      if (this.init_AiOperation != null) {
         this.init_AiOperation.cancel();
      }

      if (this.load_AiOperation != null) {
         this.load_AiOperation.cancel();
      }

      if (this.mineOp != null) {
         this.mineOp.cancel();
      }

      if (this.craftOp != null) {
         this.craftOp.cancel();
      }

      if (this.furnaceReturn != null) {
         this.furnaceReturn.cancel();
      }
   }

   record ResourcePlan(Map<String, Integer> resources, Map<String, Integer> crafts) {
      ResourcePlan {
         resources = Collections.unmodifiableMap(new LinkedHashMap<>(resources));
         crafts = Collections.unmodifiableMap(new LinkedHashMap<>(crafts));
      }

      static AiFurnaceWork.ResourcePlan forInput(String input) {
         Map<String, Integer> resources = new LinkedHashMap<>();
         resources.put(input, AiGameState.countInventoryItem(input) + 8);
         resources.merge("minecraft:coal", 8, Math::max);
         return new AiFurnaceWork.ResourcePlan(resources, Map.of());
      }
   }
}
