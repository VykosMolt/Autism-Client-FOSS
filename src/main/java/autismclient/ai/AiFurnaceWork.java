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
   private final Minecraft init_Minecraft = Minecraft.getInstance();
   private final Object init_Obj = this.init_Minecraft.level;
   private final Object load_Obj = this.init_Minecraft.getConnection();
   private final Object read_Obj = this.init_Minecraft.player;
   private final BlockPos init_BlockPos;
   private final BlockPos load_BlockPos;
   private final String init_Str;
   private final int init_I;
   private final AiFurnaceWork.Inner_DataUtil init_Q_DataUtil;
   private final autismclient.ai.engine.AiAim init_DataUtil = new autismclient.ai.engine.AiAim();
   private final autismclient.ai.engine.AiBlockDigger init_RefQueue_DataUtil;
   private final Set<BlockPos> init_Set = new HashSet<>();
   private Map<BlockPos, BlockPos> init_Map = Map.of();
   private AiOperation init_AiOperation;
   private AiOperation load_AiOperation;
   private AiOperation read_AiOperation;
   private AiOperation write_AiOperation;
   private AiFurnaceReturn init_P;
   private String load_Str;
   private final Set<String> load_Set = new HashSet<>();
   private final JsonArray init_JsonArray = new JsonArray();
   private BlockPos read_BlockPos;
   private BlockPos write_BlockPos;
   private int load_I;
   private int read_I;
   private int write_I;
   private int update = -100;
   private int sync;
   private boolean init_Z;
   private boolean load_Z;
   private boolean read_Z;
   private int flush;
   private final Set<BlockPos> read_Set = new HashSet<>();
   private final JsonArray load_JsonArray = new JsonArray();
   private String read_Str = "planning useful furnace work";

   static boolean init_Z_Q_DataUtil(AiFurnaceWork.Inner_DataUtil plan) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         AiCraftPlanner.RecipeGraph graph = AiCraft.buildRecipeGraph();

         for (Entry<String, Integer> preparation : plan.crafts().entrySet()) {
            AiCraftPlanner.CraftPlan craft = AiCraftPlanner.init(graph, preparation.getKey(), preparation.getValue());
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
                     && autismclient.ai.engine.MiningCost.load(mc, mc.level.getBlockState(p), true) >= 0) {
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
      this(station, home, input, cookingTicks, AiFurnaceWork.Inner_DataUtil.forInput(input));
   }

   AiFurnaceWork(BlockPos station, BlockPos home, String input, int cookingTicks, AiFurnaceWork.Inner_DataUtil plan) {
      this.init_BlockPos = station.immutable();
      this.load_BlockPos = home.immutable();
      this.init_Str = input;
      this.init_Q_DataUtil = plan;
      this.init_I = Math.max(0, cookingTicks - 30);
      this.init_RefQueue_DataUtil = new autismclient.ai.engine.AiBlockDigger(
         this.init_Minecraft,
         slot -> this.init_Minecraft.player.getInventory().setSelectedSlot(slot),
         this.init_DataUtil::aimAt,
         true,
         true,
         this.init_DataUtil::tick
      );
   }

   static Map<BlockPos, BlockPos> init(BlockPos home, Predicate<BlockPos> safe, Predicate<BlockPos> clear) {
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

   static int init_I_Map_BlockPos(Map<BlockPos, BlockPos> route, BlockPos from) {
      int steps;
      for (steps = 0; route.containsKey(from) && !route.get(from).equals(from) && steps < 512; steps++) {
         from = route.get(from);
      }

      return route.containsKey(from) ? steps * 12 + 30 : 2147483647;
   }

   static boolean init(Map<BlockPos, BlockPos> route, BlockPos from, BlockPos block) {
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
      if (!AiTerrain.isLoaded(this.init_Minecraft.level, p)) {
         return false;
      }

      BlockState s = this.init_Minecraft.level.getBlockState(p);
      return s.getCollisionShape(this.init_Minecraft.level, p).isEmpty() && s.getFluidState().isEmpty() && !s.is(Blocks.FIRE) && !s.is(Blocks.SOUL_FIRE);
   }

   private boolean load(BlockPos p) {
      if (this.init_Z_BlockPos_2(p) && this.init_Z_BlockPos_2(p.above()) && AiTerrain.isLoaded(this.init_Minecraft.level, p.below())) {
         BlockState floor = this.init_Minecraft.level.getBlockState(p.below());
         return floor.isCollisionShapeFullBlock(this.init_Minecraft.level, p.below())
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
         || this.init_Minecraft.level != this.init_Obj
         || this.init_Minecraft.getConnection() != this.load_Obj
         || this.init_Minecraft.player != this.read_Obj
         || this.init_Minecraft.player == null) {
         return this.init_AiOperation_Result_AiOperation_Status_Str_3(AiOperation.Status.CANCELLED, "Furnace work world/player changed");
      }

      if (!this.init_Minecraft.player.isAlive()) {
         return this.init_AiOperation_Result_AiOperation_Status_Str_3(AiOperation.Status.FAILED, "Player died during furnace work");
      }

      this.load_I++;
      if (this.load_I - this.update >= 20 || this.init_Map.isEmpty()) {
         this.init_Map = init(this.load_BlockPos, this::load, this::init_Z_BlockPos_2);
         this.update = this.load_I;
      }

      if ((long)this.load_I + init_I_Map_BlockPos(this.init_Map, this.init_Minecraft.player.blockPosition()) >= this.init_I) {
         this.init_Z = true;
      }

      if (this.write_AiOperation != null) {
         AiOperation.Result r = this.write_AiOperation.tick();
         if (!r.terminal()) {
            return this.init_AiOperation_Result_Str_3("Preparing " + this.load_Str + " while the furnace cooks: " + r.message());
         }

         this.init_JsonArray.add(AiJson.objectOf("item", this.load_Str, "status", r.status().name(), "receipt", AiRunResources.parseEvidenceJson(r.dataJson())));
         boolean unsettled = r.status() == AiOperation.Status.UNCERTAIN || this.write_AiOperation.requiresReconciliationAfterEmergency();
         this.write_AiOperation.cancel();
         this.write_AiOperation = null;
         return unsettled
            ? this.init_AiOperation_Result_AiOperation_Status_Str_3(AiOperation.Status.UNCERTAIN, "Furnace preparation needs inventory reconciliation")
            : this.init_AiOperation_Result_Str_3("Furnace-time preparation finished; continue useful work");
      } else if (this.init_Z) {
         this.init_RefQueue_DataUtil.init_V();
         this.init_DataUtil.clear();
         if (this.init_AiOperation != null) {
            if (!this.init_AiOperation.canInterruptNow()) {
               AiOperation.Result flight = this.init_AiOperation.tick();
               if (!flight.terminal()) {
                  return this.init_AiOperation_Result_Str_3("Finish committed movement before returning: " + flight.message());
               }

               if (flight.status() == AiOperation.Status.UNCERTAIN || this.init_AiOperation.requiresReconciliationAfterEmergency()) {
                  return this.init_AiOperation_Result_AiOperation_Status_Str_3(AiOperation.Status.UNCERTAIN, "Furnace excursion movement needs reconciliation");
               }
            }

            this.init_AiOperation.cancel();
            this.init_AiOperation = null;
         }

         if (this.init_P == null) {
            if (this.load_AiOperation != null) {
               this.load_AiOperation.cancel();
               this.load_AiOperation = null;
            }

            if (this.read_AiOperation != null) {
               this.read_AiOperation.cancel();
               this.read_AiOperation = null;
            }

            this.init_P = new AiFurnaceReturn(
               this.init_BlockPos,
               this.init_Map.containsKey(this.init_Minecraft.player.blockPosition())
                  ? "Batch collection due"
                  : "Terrain changed or survival moved the body off the planned return"
            );
         }

         AiOperation.Result r = this.init_P.tick();
         this.read_Str = r.message();
         return r.terminal() ? this.init_AiOperation_Result_AiOperation_Status_Str_3(r.status(), r.message()) : this.init_AiOperation_Result_Str_3(r.message());
      } else {
         if (this.init_AiOperation != null) {
            AiOperation.Result r = this.init_AiOperation.tick();
            if (!r.terminal()) {
               return this.init_AiOperation_Result_Str_3(this.read_Str + ": " + r.message());
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
            if (this.init_Minecraft.level.getBlockState(this.read_BlockPos).isAir()) {
               this.read_I++;
               this.read_BlockPos = null;
               this.init_RefQueue_DataUtil.init_V();
            } else if (this.update(this.read_BlockPos) && autismclient.ai.engine.AiBlockInteraction.findReachableHit(this.init_Minecraft, this.read_BlockPos) != null) {
               autismclient.ai.engine.AiBlockDigger.Inner_DataUtil_2 r = this.init_RefQueue_DataUtil.init_RefQueue_DataUtil__BlockPos(this.read_BlockPos);
               this.init_DataUtil.tick();
               if (r == autismclient.ai.engine.AiBlockDigger.Inner_DataUtil_2.init) {
                  return this.init_AiOperation_Result_Str_3("Mining needed " + this.init_Str_BlockPos(this.read_BlockPos) + " while the furnace cooks");
               }

               this.init_RefQueue_DataUtil.init_V();
               if (r == autismclient.ai.engine.AiBlockDigger.Inner_DataUtil_2.load) {
                  this.read_I++;
               }

               this.read_BlockPos = null;
            } else {
               this.read_BlockPos = null;
               this.init_RefQueue_DataUtil.init_V();
            }
         }

         if (this.init_Q_DataUtil.resources().entrySet().stream().allMatch(e -> AiGameState.countInventoryItem(e.getKey()) >= e.getValue())
            && this.init_Q_DataUtil.crafts().entrySet().stream().allMatch(e -> this.load_Set.contains(e.getKey()) || AiGameState.countInventoryItem(e.getKey()) >= e.getValue())) {
            this.init_Z = true;
            return this.tick();
         }

         if (this.load_I + init_I_Map_BlockPos(this.init_Map, this.init_Minecraft.player.blockPosition()) + 120 < this.init_I) {
            for (Entry<String, Integer> preparation : this.init_Q_DataUtil.crafts().entrySet()) {
               if (this.load_Set.add(preparation.getKey())) {
                  AiCraftPlanner.CraftPlan recipe = AiCraftPlanner.init(AiCraft.buildRecipeGraph(), preparation.getKey(), preparation.getValue());
                  if (recipe.complete()
                     && !recipe.satisfied()
                     && !recipe.requiresTable()
                     && !recipe.steps()
                        .stream()
                        .anyMatch(s -> !s.recipe().station().isEmpty() || s.recipe().inputs().stream().anyMatch(variants -> variants.contains(this.init_Str)))) {
                     this.load_Str = preparation.getKey();
                     this.write_AiOperation = AiCraft.craft(AiJson.objectOf("item", this.load_Str, "count", preparation.getValue(), "timeoutSeconds", 6));
                     return this.init_AiOperation_Result_Str_3("Prepare " + this.load_Str + " during furnace time");
                  }
               }
            }
         }

         for (ItemEntity drop : this.init_Minecraft.level.getEntitiesOfClass(ItemEntity.class, this.init_Minecraft.player.getBoundingBox().inflate(10.0))) {
            String id = BuiltInRegistries.ITEM.getKey(drop.getItem().getItem()).toString();
            if (this.init_Q_DataUtil.resources().containsKey(id) || id.equals("minecraft:cobblestone")) {
               BlockPos at = drop.blockPosition();
               if (this.init_Map.containsKey(at)
                  && !drop.hasPickUpDelay()
                  && this.load_I + 2L * init_I_Map_BlockPos(this.init_Map, at) + 40L < this.init_I
                  && !this.init_Minecraft.player.getBoundingBox().inflate(1.0, 0.5, 1.0).intersects(drop.getBoundingBox())) {
                  this.write_I++;
                  this.write_BlockPos = at;
                  this.init_AiOperation = this.init_AiOperation_BlockPos(at);
                  this.read_Str = "collecting furnace-work drops";
                  return this.init_AiOperation_Result_Str_3(this.read_Str);
               }
            }
         }

         if (this.load_I >= this.sync && AiItems.countFreeSlots() > 0 && this.read_I < 8) {
            this.sync = this.load_I + 20;
            BlockPos best = null;
            BlockPos bestStand = null;
            double score = 1.7976931348623157E308;

            for (BlockPos at : BlockPos.betweenClosed(this.load_BlockPos.offset(-12, -2, -12), this.load_BlockPos.offset(12, 4, 12))) {
               if (!this.init_Set.contains(at) && this.write(at) && this.update(at)) {
                  for (BlockPos stand : this.init_Map.keySet()) {
                     if (!stand.below().equals(at) && !init(this.init_Map, stand, at)) {
                        Vec3 eye = Vec3.atBottomCenterOf(stand).add(0.0, this.init_Minecraft.player.getEyeHeight(), 0.0);
                        if (!(eye.distanceToSqr(Vec3.atCenterOf(at)) > 9.0) && this.load_I + 2L * init_I_Map_BlockPos(this.init_Map, stand) + 80L < this.init_I
                           )
                         {
                           BlockHitResult hit = this.init_Minecraft
                              .level
                              .clip(new ClipContext(eye, Vec3.atCenterOf(at), Block.COLLIDER, Fluid.NONE, this.init_Minecraft.player));
                           if (hit.getBlockPos().equals(at)) {
                              double cost = stand.distSqr(this.init_Minecraft.player.blockPosition()) + (this.read(at) ? 0 : 8);
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
               this.read_Str = "walking to useful furnace-time mining";
               return this.init_AiOperation_Result_Str_3(this.read_Str);
            }
         }

         if (this.flush >= 4 && !this.read_Z) {
            if (this.read_AiOperation == null) {
               this.read_AiOperation = AiGameplay.sync(AiJson.objectOf("block", "ore", "radius", 64, "limit", 32));
            }

            AiOperation.Result scan = this.read_AiOperation.tick();
            if (scan.terminal()) {
               JsonObject found = AiRunResources.parseEvidenceJson(scan.dataJson());
               if (found.has("results")) {
                  for (JsonElement lead : found.getAsJsonArray("results")) {
                     if (this.load_JsonArray.size() < 32) {
                        this.load_JsonArray.add(lead.deepCopy());
                     }
                  }
               }

               this.read_AiOperation.cancel();
               this.init_Z = true;
            }

            return this.init_AiOperation_Result_Str_3("Locate the next ore from observed terrain while this batch finishes");
         } else if (!this.read_Z && this.flush < 4) {
            this.write_BlockPos = this.init_Map
               .keySet()
               .stream()
               .filter(p -> Math.abs(p.getX() - this.load_BlockPos.getX()) + Math.abs(p.getZ() - this.load_BlockPos.getZ()) <= 8)
               .filter(p -> this.read_Set.stream().allMatch(old -> old.distSqr(p) > 25.0))
               .filter(p -> this.load_I + 2L * init_I_Map_BlockPos(this.init_Map, p) + 60L < this.init_I)
               .max(Comparator.comparingDouble(p -> p.distSqr(this.load_BlockPos)))
               .orElse(this.load_BlockPos);
            if (this.write_BlockPos.equals(this.load_BlockPos)) {
               this.flush = 4;
               return this.init_AiOperation_Result_Str_3("No further safe walking viewpoint; prospect observed ore without leaving the return route");
            } else {
               this.read_Set.add(this.write_BlockPos);
               this.flush++;
               this.read_Z = true;
               this.init_AiOperation = this.init_AiOperation_BlockPos(this.write_BlockPos);
               this.read_Str = "surveying a clear route ahead while the furnace cooks";
               return this.init_AiOperation_Result_Str_3(this.read_Str);
            }
         } else {
            if (this.load_AiOperation == null) {
               JsonArray blocks = new JsonArray();

               for (String id : List.of(
                  "minecraft:iron_ore", "minecraft:deepslate_iron_ore", "minecraft:coal_ore", "minecraft:diamond_ore", "minecraft:deepslate_diamond_ore"
               )) {
                  blocks.add(id);
               }

               this.load_AiOperation = AiGameplay.flush(AiJson.objectOf("blocks", blocks, "radius", 16));
            }

            this.init_DataUtil
               .aimAt(
                  this.init_Minecraft
                     .player
                     .getEyePosition()
                     .add(Vec3.atCenterOf(this.write_BlockPos).subtract(Vec3.atCenterOf(this.load_BlockPos)).normalize().scale(8.0))
               );
            this.init_DataUtil.tick();
            AiOperation.Result scan = this.load_AiOperation.tick();
            if (scan.terminal()) {
               JsonObject observed = AiRunResources.parseEvidenceJson(scan.dataJson());
               if (observed.has("matches")) {
                  for (JsonElement match : observed.getAsJsonArray("matches")) {
                     JsonObject fact = match.getAsJsonObject();
                     AiWorldMemory.init(new BlockPos(AiJson.getInt(fact, "x", 0), AiJson.getInt(fact, "y", 0), AiJson.getInt(fact, "z", 0)));
                     if (this.load_JsonArray.size() < 32) {
                        this.load_JsonArray.add(fact.deepCopy());
                     }
                  }
               }

               this.load_AiOperation.cancel();
               this.load_AiOperation = null;
               this.read_Z = false;
            }

            return this.init_AiOperation_Result_Str_3("Checking observed resources along the next clear route");
         }
      }
   }

   private String init_Str_BlockPos(BlockPos at) {
      BlockState s = this.init_Minecraft.level.getBlockState(at);
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

   private boolean read(BlockPos at) {
      return this.init_Str_BlockPos(at).equals(this.init_Str);
   }

   private boolean write(BlockPos at) {
      if (!AiTerrain.isLoaded(this.init_Minecraft.level, at)) {
         return false;
      }

      String resource = this.init_Str_BlockPos(at);
      return this.init_Q_DataUtil.resources().containsKey(resource) && AiGameState.countInventoryItem(resource) < this.init_Q_DataUtil.resources().get(resource);
   }

   private boolean update(BlockPos at) {
      if (!at.equals(this.init_BlockPos)
         && !at.equals(this.init_BlockPos.below())
         && !init(this.init_Map, this.init_Minecraft.player.blockPosition(), at)
         && !at.equals(this.init_Minecraft.player.blockPosition().below())
         && (
            !(at.getY() + 1 <= this.init_Minecraft.player.getBoundingBox().minY + 0.05)
               || !(at.getY() + 1 >= this.init_Minecraft.player.getBoundingBox().minY - 0.05)
               || !new AABB(at).inflate(0.0, 0.1, 0.0).intersects(this.init_Minecraft.player.getBoundingBox())
         )) {
         for (Direction d : Direction.values()) {
            BlockPos near = at.relative(d);
            if (!AiTerrain.isLoaded(this.init_Minecraft.level, near)
               || !this.init_Minecraft.level.getFluidState(near).isEmpty()
               || this.init_Minecraft.level.getBlockState(near).getBlock() instanceof FallingBlock) {
               return false;
            }
         }

         return this.init_Minecraft.level.getBlockEntity(at) == null
            && autismclient.ai.engine.MiningCost.load(this.init_Minecraft, this.init_Minecraft.level.getBlockState(at), true) >= 0;
      } else {
         return false;
      }
   }

   private AiOperation init_AiOperation_BlockPos(final BlockPos to) {
      final Set<BlockPos> permitted = Set.copyOf(this.init_Map.keySet());
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
         public boolean init_Z(BlockPos p) {
            return permitted.contains(p);
         }

         @Override
         public boolean load(BlockPos p) {
            return false;
         }

         @Override
         public boolean read(BlockPos p) {
            return false;
         }

         @Override
         public Set<autismclient.ai.engine.AiPathfinder.MoveType> init_Set() {
            return Set.of(
               autismclient.ai.engine.AiPathfinder.MoveType.JUMP,
               autismclient.ai.engine.AiPathfinder.MoveType.JUMP_GAP_2,
               autismclient.ai.engine.AiPathfinder.MoveType.JUMP_GAP_3,
               autismclient.ai.engine.AiPathfinder.MoveType.PARKOUR_ASCEND
            );
         }
      };
      return AiGoto.init(
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

   JsonObject init() {
      return AiJson.objectOf(
         "activity",
         this.read_Str,
         "home",
         this.load_BlockPos.toShortString(),
         "station",
         this.init_BlockPos.toShortString(),
         "ticks",
         this.load_I,
         "budgetTicks",
         this.init_I,
         "mined",
         this.read_I,
         "pickupRoutes",
         this.write_I,
         "returning",
         this.init_Z,
         "returnRouteCells",
         this.init_Map.size(),
         "returnEditsAllowed",
         this.init_P != null && AiJson.getBoolean(this.init_P.init(), "returnEditsAllowed", false),
         "returnRecovery",
         this.init_P == null ? new JsonObject() : this.init_P.init(),
         "preparations",
         this.init_JsonArray,
         "resourceTargets",
         this.init_Q_DataUtil.resources(),
         "bodyCell",
         this.init_Minecraft.player == null ? "" : this.init_Minecraft.player.blockPosition().toShortString(),
         "bodyOnVerifiedRoute",
         this.init_Minecraft.player != null && this.init_Map.containsKey(this.init_Minecraft.player.blockPosition()),
         "resourceSearchStops",
         this.flush,
         "observedResourceLeads",
         this.load_JsonArray
      );
   }

   private AiOperation.Result init_AiOperation_Result_Str_3(String message) {
      return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.init());
   }

   private AiOperation.Result init_AiOperation_Result_AiOperation_Status_Str_3(AiOperation.Status s, String message) {
      JsonObject d = this.init();
      this.cancel();
      return AiGameOperations.result(s, message, d);
   }

   @Override
   public boolean canInterruptNow() {
      return (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow())
         && (this.write_AiOperation == null || this.write_AiOperation.canInterruptNow())
         && (this.init_P == null || this.init_P.canInterruptNow());
   }

   @Override
   public boolean requiresReconciliationAfterEmergency() {
      return this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency()
         || this.write_AiOperation != null && this.write_AiOperation.requiresReconciliationAfterEmergency()
         || this.init_P != null && this.init_P.requiresReconciliationAfterEmergency();
   }

   @Override
   public void cancel() {
      this.load_Z = true;
      this.init_RefQueue_DataUtil.init_V();
      this.init_DataUtil.clear();
      if (this.init_AiOperation != null) {
         this.init_AiOperation.cancel();
      }

      if (this.load_AiOperation != null) {
         this.load_AiOperation.cancel();
      }

      if (this.read_AiOperation != null) {
         this.read_AiOperation.cancel();
      }

      if (this.write_AiOperation != null) {
         this.write_AiOperation.cancel();
      }

      if (this.init_P != null) {
         this.init_P.cancel();
      }
   }

   record Inner_DataUtil(Map<String, Integer> resources, Map<String, Integer> crafts) {
      Inner_DataUtil {
         resources = Collections.unmodifiableMap(new LinkedHashMap<>(resources));
         crafts = Collections.unmodifiableMap(new LinkedHashMap<>(crafts));
      }

      static AiFurnaceWork.Inner_DataUtil forInput(String input) {
         Map<String, Integer> resources = new LinkedHashMap<>();
         resources.put(input, AiGameState.countInventoryItem(input) + 8);
         resources.merge("minecraft:coal", 8, Math::max);
         return new AiFurnaceWork.Inner_DataUtil(resources, Map.of());
      }
   }
}
