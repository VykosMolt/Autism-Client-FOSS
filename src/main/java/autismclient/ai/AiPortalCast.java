package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import com.google.gson.JsonObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

final class AiPortalCast {
   private static final int load_I = 24;
   private static final int read = 12;
   private static final int write = 30;
   private static final int update = 2;
   private static final int sync = 3;
   private static final double init_D = 3.0;
   static final boolean init_Z = false;
   private static final int flush = 64;
   static final int init_I = 16;
   private static final int close = 60;
   private static final int open = 40;
   private static final int poll = 140;
   private static final int push = 120;
   private static final int pop = 600;
   private static final int tick = 2;
   static final List<String> init_List = List.of(
      "minecraft:dirt",
      "minecraft:cobblestone",
      "minecraft:netherrack",
      "minecraft:cobbled_deepslate",
      "minecraft:coarse_dirt",
      "minecraft:gravel",
      "minecraft:sand",
      "minecraft:stone",
      "minecraft:deepslate",
      "minecraft:andesite",
      "minecraft:diorite",
      "minecraft:granite",
      "minecraft:tuff"
   );
   static final List<String> load_List = List.of(
      "minecraft:oak_planks",
      "minecraft:spruce_planks",
      "minecraft:birch_planks",
      "minecraft:jungle_planks",
      "minecraft:acacia_planks",
      "minecraft:dark_oak_planks",
      "minecraft:mangrove_planks",
      "minecraft:cherry_planks",
      "minecraft:oak_log",
      "minecraft:spruce_log",
      "minecraft:birch_log",
      "minecraft:jungle_log",
      "minecraft:acacia_log",
      "minecraft:dark_oak_log",
      "minecraft:mangrove_log",
      "minecraft:cherry_log"
   );

   private AiPortalCast() {
   }

   static AiPortalCast.Inner_ConfigStore init_aX_ConfigStore(ClientLevel level, BlockPos pos) {
      if (!AiTerrain.isLoaded(level, pos)) {
         return AiPortalCast.Inner_ConfigStore.SOLID;
      }

      BlockState state = level.getBlockState(pos);
      if (state.is(Blocks.OBSIDIAN)) {
         return AiPortalCast.Inner_ConfigStore.OBSIDIAN;
      }

      FluidState fluid = state.getFluidState();
      if (!fluid.isEmpty()) {
         if (!fluid.isSource()) {
            return AiPortalCast.Inner_ConfigStore.SOLID;
         } else if (fluid.is(FluidTags.LAVA)) {
            return AiPortalCast.Inner_ConfigStore.LAVA;
         } else {
            return fluid.is(FluidTags.WATER) ? AiPortalCast.Inner_ConfigStore.WATER : AiPortalCast.Inner_ConfigStore.SOLID;
         }
      } else {
         return !state.isAir() && !state.canBeReplaced() ? AiPortalCast.Inner_ConfigStore.SOLID : AiPortalCast.Inner_ConfigStore.AIR;
      }
   }

   static boolean init_Z(ClientLevel level, BlockPos anchor) {
      AiPortalCast.Inner_TaskQueue lookup = pos -> init_aX_ConfigStore(level, pos);
      List<BlockPos> sources = new ArrayList<>();

      for (BlockPos pos : BlockPos.betweenClosed(anchor.offset(-12, -6, -12), anchor.offset(12, 6, 12))) {
         if (sources.size() >= 64) {
            break;
         }

         if (AiTerrain.isLoaded(level, pos)) {
            FluidState fluid = level.getFluidState(pos);
            if (fluid.isSource() && fluid.is(FluidTags.LAVA)) {
               sources.add(pos.immutable());
            }
         }
      }

      sources.sort(Comparator.comparingDouble(anchor::distSqr));
      Set<Long> tried = new HashSet<>();
      int considered = 0;

      for (BlockPos source : sources) {
         if (++considered > 24) {
            break;
         }

         for (Direction axis : List.of(Direction.EAST, Direction.SOUTH)) {
            for (int offset = -2; offset <= 1; offset++) {
               BlockPos origin = source.above().relative(axis, offset);
               if (tried.add(origin.asLong())) {
                  Set<BlockPos> reserved = init_Set(origin, axis);
                  AiPortalCast.Inner_StreamUtil plan = init(init_List(origin, axis), axis, reserved, lookup);
                  if (plan.blocked() == null) {
                     if (plan.blocked() != null) {
                        Set<BlockPos> need = new HashSet<>(AiDimension.write(origin, axis));

                        for (BlockPos cell : AiDimension.frameBlocks(origin, axis)) {
                           need.add(cell);
                           need.add(cell.above());
                        }

                        Set<BlockPos> dug = new HashSet<>();
                        boolean ok = true;

                        for (BlockPos cell : need) {
                           if (init_aX_ConfigStore(level, cell) == AiPortalCast.Inner_ConfigStore.SOLID) {
                              if (!read(level, cell)) {
                                 ok = false;
                                 break;
                              }

                              dug.add(cell.immutable());
                           }
                        }

                        if (!ok || dug.size() > 30) {
                           continue;
                        }

                        AiPortalCast.Inner_TaskQueue after = pos -> dug.contains(pos) ? AiPortalCast.Inner_ConfigStore.AIR : init_aX_ConfigStore(level, pos);
                        plan = init(init_List(origin, axis), axis, reserved, after);
                        if (plan.blocked() != null) {
                           continue;
                        }
                     }

                     long usable = sources.stream().filter(p -> !reserved.contains(p)).count();
                     if (plan.lavaNeeded() <= usable) {
                        return true;
                     }
                  }
               }
            }
         }
      }

      return false;
   }

   static boolean load(ClientLevel level, BlockPos cell) {
      for (Direction d : Direction.values()) {
         FluidState fluid = level.getFluidState(cell.relative(d));
         if (fluid.is(FluidTags.LAVA)) {
            return true;
         }

         if (d != Direction.DOWN && fluid.is(FluidTags.WATER)) {
            return true;
         }
      }

      return false;
   }

   static boolean read(ClientLevel level, BlockPos cell) {
      if (!AiTerrain.isLoaded(level, cell)) {
         return false;
      } else if (load(level, cell)) {
         return false;
      } else {
         BlockState state = level.getBlockState(cell);
         if (state.getFluidState().isEmpty()
            && !state.hasBlockEntity()
            && !state.is(Blocks.OBSIDIAN)
            && !state.is(Blocks.BEDROCK)
            && !(state.getBlock() instanceof FallingBlock)) {
            float hardness = state.getDestroySpeed(level, cell);
            return hardness >= 0.0F && hardness <= 5.0F;
         } else {
            return false;
         }
      }
   }

   static List<BlockPos> init_List(BlockPos origin, Direction axis) {
      List<BlockPos> cells = new ArrayList<>(AiDimension.frameBlocks(origin, axis));
      cells.sort(Comparator.<BlockPos>comparingInt(p -> p.getY()).thenComparingInt(p -> p.getX()).thenComparingInt(p -> p.getZ()));
      return cells;
   }

   static Set<BlockPos> init_Set(BlockPos origin, Direction axis) {
      Set<BlockPos> reserved = new HashSet<>(AiDimension.frameBlocks(origin, axis));
      reserved.addAll(AiDimension.write(origin, axis));
      return reserved;
   }

   static AiPortalCast.Inner_StreamUtil init(List<BlockPos> cells, Direction axis, Set<BlockPos> reserved, AiPortalCast.Inner_TaskQueue lookup) {
      List<AiPortalCast.Inner_BufferHelper> steps = new ArrayList<>(cells.size());
      int lavaNeeded = 0;
      int moldBlocks = 0;

      for (BlockPos cell : cells) {
         AiPortalCast.Inner_ConfigStore kind = lookup.at(cell);
         if (kind == AiPortalCast.Inner_ConfigStore.OBSIDIAN) {
            steps.add(new AiPortalCast.Inner_BufferHelper(cell.immutable(), AiPortalCast.Inner_DataUtil.SKIP, null, List.of(), null));
         } else {
            if (kind != AiPortalCast.Inner_ConfigStore.LAVA && kind != AiPortalCast.Inner_ConfigStore.AIR && kind != AiPortalCast.Inner_ConfigStore.WATER) {
               return new AiPortalCast.Inner_StreamUtil(steps, cell.immutable(), "frame cell " + cell.toShortString() + " is occupied", lavaNeeded, moldBlocks);
            }

            AiPortalCast.Inner_DataUtil action = kind == AiPortalCast.Inner_ConfigStore.LAVA ? AiPortalCast.Inner_DataUtil.WATER_OVER_LAVA : AiPortalCast.Inner_DataUtil.LAVA_THEN_WATER;
            if (action == AiPortalCast.Inner_DataUtil.LAVA_THEN_WATER) {
               lavaNeeded++;
            }

            BlockPos waterCell = cell.above();
            AiPortalCast.Inner_ConfigStore above = lookup.at(waterCell);
            if (above != AiPortalCast.Inner_ConfigStore.AIR && above != AiPortalCast.Inner_ConfigStore.WATER) {
               return new AiPortalCast.Inner_StreamUtil(steps, cell.immutable(), "no room to pour above frame cell " + cell.toShortString(), lavaNeeded, moldBlocks);
            }

            List<BlockPos> dams = init(waterCell, reserved, lookup);
            AiPortalCast.Inner_SyncManager face = init(waterCell, axis, reserved, lookup, dams);
            if (face == null) {
               return new AiPortalCast.Inner_StreamUtil(steps, cell.immutable(), "no face to pour water against above " + cell.toShortString(), lavaNeeded, moldBlocks);
            }

            moldBlocks += dams.size() + (face.mold() != null ? 1 : 0);
            steps.add(new AiPortalCast.Inner_BufferHelper(cell.immutable(), action, waterCell.immutable(), dams, face));
         }
      }

      return new AiPortalCast.Inner_StreamUtil(steps, null, "", lavaNeeded, moldBlocks);
   }

   static List<BlockPos> init(BlockPos waterCell, Set<BlockPos> reserved, AiPortalCast.Inner_TaskQueue lookup) {
      List<BlockPos> dams = new ArrayList<>(4);

      for (Direction side : Plane.HORIZONTAL) {
         BlockPos n = waterCell.relative(side);
         if (!reserved.contains(n)) {
            AiPortalCast.Inner_ConfigStore kind = lookup.at(n);
            if (kind == AiPortalCast.Inner_ConfigStore.LAVA || kind == AiPortalCast.Inner_ConfigStore.AIR && lookup.at(n.below()) == AiPortalCast.Inner_ConfigStore.LAVA) {
               dams.add(n.immutable());
            }
         }
      }

      return dams;
   }

   static AiPortalCast.Inner_SyncManager init(BlockPos pourCell, Direction axis, Set<BlockPos> reserved, AiPortalCast.Inner_TaskQueue lookup, List<BlockPos> dams) {
      AiPortalCast.Inner_ConfigStore below = lookup.at(pourCell.below());
      if (below != AiPortalCast.Inner_ConfigStore.SOLID && below != AiPortalCast.Inner_ConfigStore.OBSIDIAN) {
         for (BlockPos dam : dams) {
            for (Direction side : Plane.HORIZONTAL) {
               if (pourCell.relative(side).equals(dam)) {
                  return new AiPortalCast.Inner_SyncManager(dam, side.getOpposite(), null);
               }
            }
         }

         for (Direction side : init(axis)) {
            BlockPos n = pourCell.relative(side);
            if (!reserved.contains(n) && lookup.at(n) == AiPortalCast.Inner_ConfigStore.AIR && lookup.at(n.below()) != AiPortalCast.Inner_ConfigStore.LAVA) {
               return new AiPortalCast.Inner_SyncManager(n.immutable(), side.getOpposite(), n.immutable());
            }
         }

         return null;
      } else {
         return new AiPortalCast.Inner_SyncManager(pourCell.below().immutable(), Direction.UP, null);
      }
   }

   static List<Direction> init(Direction axis) {
      return List.of(axis.getCounterClockWise(), axis.getClockWise(), axis.getOpposite(), axis);
   }

   static autismclient.ai.engine.AiPathfinder.PathGoal init(
      AiPortalCast.Inner_SyncManager face, Set<BlockPos> reserved, List<BlockPos> molds, Set<BlockPos> supports, double reach, double eyeHeight
   ) {
      return init(face, reserved, molds, supports, reach, eyeHeight, false);
   }

   static autismclient.ai.engine.AiPathfinder.PathGoal init(
      AiPortalCast.Inner_SyncManager face, Set<BlockPos> reserved, List<BlockPos> molds, Set<BlockPos> supports, double reach, double eyeHeight, boolean lava
   ) {
      Set<Long> keep = new HashSet<>();

      for (BlockPos cell : reserved) {
         keep.add(cell.asLong());

         for (Direction side : Direction.values()) {
            keep.add(cell.relative(side).asLong());
         }
      }

      for (BlockPos cell : molds) {
         keep.add(cell.asLong());
      }

      for (BlockPos cell : supports) {
         keep.add(cell.asLong());
      }

      keep.add(face.support().asLong());
      keep.add(face.support().relative(face.face()).asLong());
      autismclient.ai.engine.AiPathfinder.PathGoal goal = AiGoto.protectingCellsCompat(new autismclient.ai.engine.InteractionGoal(face.support(), reach, eyeHeight, face.face()), keep);
      return lava ? AiGoto.excludingStandCellsCompat(goal, init_Set(face.support().relative(face.face()))) : goal;
   }

   static boolean init(BlockPos feet, BlockPos pourCell) {
      return feet.distManhattan(pourCell) > 1 && feet.above().distManhattan(pourCell) > 1;
   }

   static Set<Long> init_Set(BlockPos pourCell) {
      Set<Long> forbidden = new HashSet<>();

      for (BlockPos feet : BlockPos.betweenClosed(pourCell.offset(-1, -2, -1), pourCell.offset(1, 1, 1))) {
         if (!init(feet, pourCell)) {
            forbidden.add(feet.asLong());
         }
      }

      return Set.copyOf(forbidden);
   }

   static boolean init(boolean onGround, Vec3 motion) {
      return onGround && motion.x * motion.x + motion.z * motion.z <= 1.0E-4;
   }

   static int init(long deadline, long now) {
      return (int)Math.max(0L, Math.min(30L, (deadline - now) / 1000000000L));
   }

   static Vec3 init(Vec3 eye, BlockHitResult hit) {
      return hit.getLocation().add(hit.getLocation().subtract(eye).normalize().scale(0.01));
   }

   static List<BlockPos> init_List(BlockPos waterCell) {
      List<BlockPos> zone = new ArrayList<>(8);

      for (Direction side : Plane.HORIZONTAL) {
         zone.add(waterCell.relative(side));
         zone.add(waterCell.relative(side).below());
      }

      return zone;
   }

   static BlockPos init(List<BlockPos> poolSources, Set<BlockPos> reserved, BlockPos player) {
      BlockPos best = null;
      double bestDistance = 1.7976931348623157E308;

      for (BlockPos source : poolSources) {
         if (!reserved.contains(source)) {
            double distance = source.distSqr(player);
            if (distance < bestDistance) {
               bestDistance = distance;
               best = source;
            }
         }
      }

      return best;
   }

   static String init(int flintAndSteel, int fireCharge, boolean lavaCapable, int flammable) {
      if (flintAndSteel > 0) {
         return "flint_and_steel";
      } else if (fireCharge > 0) {
         return "fire_charge";
      } else {
         return lavaCapable && flammable > 0 ? "fire_spread" : null;
      }
   }

   static JsonObject init(
      int waterBuckets, int lavaBuckets, int buckets, int throwaway, int flintAndSteel, int fireCharge, int flammable, int lavaNeeded, int minThrowaway
   ) {
      JsonObject missing = new JsonObject();
      if (waterBuckets <= 0 && buckets <= 0) {
         missing.addProperty("minecraft:water_bucket", 1);
      }

      if (lavaNeeded > 0 && lavaBuckets <= 0 && buckets - (waterBuckets > 0 ? 0 : 1) <= 0) {
         missing.addProperty("minecraft:lava_bucket", 1);
      }

      if (throwaway < minThrowaway) {
         missing.addProperty("minecraft:dirt", minThrowaway - throwaway);
      }

      if (init(flintAndSteel, fireCharge, lavaNeeded > 0 || lavaBuckets > 0 || buckets > 0, flammable) == null) {
         missing.addProperty("minecraft:flint_and_steel", 1);
      }

      return missing;
   }

   static AiOperation init(JsonObject args) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         boolean hasTarget = args.has("x") && args.has("y") && args.has("z");
         if (hasTarget || !args.has("x") && !args.has("y") && !args.has("z")) {
            final BlockPos explicit = hasTarget ? AiGameOperations.requireBlockPos(args) : null;
            final int timeoutSeconds = Math.max(30, Math.min(1800, AiJson.getInt(args, "timeoutSeconds", 600)));
            return new AiOperation() {
               final Minecraft init_Minecraft = Minecraft.getInstance();
               final Object init_Obj = this.init_Minecraft.level;
               final Object load_Obj = this.init_Minecraft.getConnection();
               final UUID init_UUID = this.init_Minecraft.player.getUUID();
               final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
               final int init_I = this.init_Minecraft.player.getInventory().getSelectedSlot();
               final autismclient.ai.engine.AiAim init_DataUtil = new autismclient.ai.engine.AiAim();
               final autismclient.ai.engine.AiPlacer init_DataPipe = new autismclient.ai.engine.AiPlacer();
               final AiGuard.GuardPolicy init_X_CachePool = AiGuard.GuardPolicy.standard().withoutCombat();
               final AiPortalCast.Inner_TaskQueue init_aX_TaskQueue = this::init_aX_ConfigStore;
               AiPortalCast.Inner_NetHelper init_aX_NetHelper = AiPortalCast.Inner_NetHelper.PREFLIGHT;
               AiPortalCast.Inner_CachePool init_aX_CachePool = AiPortalCast.Inner_CachePool.STANCE;
               final ArrayDeque<BlockPos> init_ArrayDeque = new ArrayDeque<>();
               final Set<BlockPos> init_Set = new HashSet<>();
               boolean init_Z;
               int load_I;
               int read_I;
               BlockPos init_BlockPos;
               BlockPos load_BlockPos;
               Direction init_Direction = Direction.EAST;
               List<BlockPos> init_List = new ArrayList<>();
               Set<BlockPos> load_Set = Set.of();
               Set<BlockPos> read_Set = Set.of();
               AiPortalCast.Inner_StreamUtil init_aX_StreamUtil;
               int write_I;
               int update_I;
               int sync_I;
               int flush;
               int close;
               int open;
               int poll;
               int push;
               int pop;
               int tick;
               int run;
               int get;
               int set;
               int put;
               int add;
               int clear;
               long load_J;
               AiPortalCast.Inner_SyncManager init_aX_SyncManager;
               AiPortalCast.Inner_SyncManager load_aX_SyncManager;
               List<BlockPos> load_List = List.of();
               Set<BlockPos> write_Set = new HashSet<>();
               AiOperation init_AiOperation;
               AiOperation load_AiOperation;
               AiOperation read_AiOperation;
               AiPortalCast.Inner_StateCache init_aX_StateCache;
               AiPortalCast.Inner_SyncManager read_aX_SyncManager;
               long read_J;
               boolean load_Z;
               final Set<AiPortalCast.Inner_SyncManager> update_Set = new HashSet<>();
               String init_Str = "";
               String load_Str = "";
               AiGuard.StatusSnapshot init_X_BufferHelper = AiGuard.sampleStatus();
               boolean read_Z;
               boolean write_Z;
               boolean update_Z;
               boolean sync_Z;
               final AiPortalCast.Inner_RenderState init_aX_RenderState = new AiPortalCast.Inner_RenderState();
               final AiPortalCast.Inner_JsonUtil init_aX_JsonUtil = new AiPortalCast.Inner_JsonUtil();
               final AiPortalCast.Inner_UiHelper init_aX_UiHelper = new AiPortalCast.Inner_UiHelper();
               final Set<BlockPos> sync_Set = new HashSet<>();

               @Override
               public AiOperation.Result tick() {
                  if (!this.sync_Z && this.init_Minecraft.player != null && this.init_Minecraft.level != null) {
                     if (this.init_Minecraft.level == this.init_Obj
                        && this.init_Minecraft.getConnection() == this.load_Obj
                        && this.init_UUID.equals(this.init_Minecraft.player.getUUID())
                        && this.init_Minecraft.player.isAlive()) {
                        if (this.init_aX_JsonUtil.init_Z()) {
                           this.init_aX_JsonUtil
                              .init(AiGameState.countInventoryItem(this.init_aX_JsonUtil.init_Str()), AiServerEvidence.inventoryRevision(), AiServerEvidence.blockRevision(this.init_aX_JsonUtil.init_BlockPos()));
                        }

                        if (this.init_Minecraft.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.WATER_EVAPORATES)) {
                           return this.init(AiOperation.Status.FAILED, "Water casting prohibited by the current dimension attributes");
                        }

                        if (this.init_Minecraft.player.getHealth() <= 4.0F && !AiGuard.isLowHealthOverrideActive()) {
                           this.cancel();
                           return AiOperation.Result.failed(
                              AiFailure.LOW_HEALTH,
                              AiGuard.formatLowHealthAbort("portal casting", this.init_Minecraft.player.getHealth(), this.init_Minecraft.player.getMaxHealth()),
                              this.init_JsonObject_36().toString()
                           );
                        }

                        if (System.nanoTime() > this.init_J) {
                           return this.init(
                              AiOperation.Status.TIMED_OUT, "Portal cast deadline reached in " + this.init_aX_NetHelper.name().toLowerCase(Locale.ROOT)
                           );
                        }

                        AiOperation.Result guarded = this.push();
                        if (guarded != null) {
                           return guarded;
                        }

                        if (this.load_Z) {
                           return this.open();
                        }

                        AiOperation.Result var10000;
                        switch (this.init_aX_NetHelper) {
                           case PREFLIGHT:
                              var10000 = this.init_AiOperation_Result_21();
                              break;
                           case LOCATE:
                              var10000 = this.load_AiOperation_Result_13();
                              break;
                           case SITE:
                              var10000 = this.read();
                              break;
                           case EXCAVATE:
                              var10000 = this.pop();
                              break;
                           case WATER:
                              var10000 = this.write();
                              break;
                           case CAST:
                              var10000 = this.update();
                              break;
                           case LIGHT:
                              var10000 = this.sync();
                              break;
                           default:
                              throw new MatchException(null, null);
                        }

                        return var10000;
                     } else {
                        return this.init(AiOperation.Status.CANCELLED, "Portal casting player, world or connection changed");
                     }
                  } else {
                     return this.init(AiOperation.Status.CANCELLED, "Portal cast cancelled");
                  }
               }

               AiOperation.Result init_AiOperation_Result_21() {
                  if (this.init_Minecraft.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.WATER_EVAPORATES)) {
                     return this.init(AiOperation.Status.FAILED, "Water evaporates in this dimension; a cast portal is an overworld technique");
                  }

                  this.load_V_11();
                  this.read_Z = this.write_I > 0;
                  JsonObject missing = AiPortalCast.init(this.write_I, this.update_I, this.sync_I, this.flush, this.close, this.open, this.poll, 1, 16);
                  if (!missing.keySet().isEmpty()) {
                     return this.init(AiFailure.MISSING_MATERIALS, "Portal cast is missing materials: " + missing, missing);
                  }

                  this.init_aX_NetHelper = AiPortalCast.Inner_NetHelper.LOCATE;
                  return this.load("Materials ready; locating the lava pool");
               }

               AiOperation.Result load_AiOperation_Result_13() {
                  if (this.init_BlockPos == null) {
                     BlockPos around = explicit != null ? explicit : this.init_Minecraft.player.blockPosition();
                     int radius = explicit != null ? 6 : 24;
                     this.init_BlockPos = this.init(around, radius, 8, true, true);
                     if (this.init_BlockPos == null) {
                        this.init_BlockPos = this.init(around, radius, 8, true);
                     }

                     if (this.init_BlockPos == null) {
                        return this.init(
                           AiFailure.TARGET_NOT_FOUND,
                           explicit != null
                              ? "No lava source near " + explicit.toShortString()
                              : "No lava source within 24 blocks; pass x,y,z of a pool (game:oracle query 'lava' finds one)",
                           null
                        );
                     }
                  }

                  if (this.init_Minecraft.player.blockPosition().distSqr(this.init_BlockPos) > 16.0) {
                     AiOperation.Result moved = this.init(() -> this.init(this.init_BlockPos, 3.0, 60), "Travelling to the lava pool");
                     if (moved != null) {
                        return moved;
                     }
                  }

                  this.init_aX_NetHelper = AiPortalCast.Inner_NetHelper.SITE;
                  return this.load("At the pool; planning the cast");
               }

               AiOperation.Result read() {
                  this.init_List = this.init_List(this.init_BlockPos);
                  Set<Long> tried = new HashSet<>();
                  AiPortalCast.Inner_StreamUtil bestPlan = null;
                  BlockPos bestOrigin = null;
                  Direction bestAxis = Direction.EAST;
                  double bestScore = 1.7976931348623157E308;
                  int considered = 0;

                  for (BlockPos source : this.init_List) {
                     if (++considered > 24) {
                        break;
                     }

                     for (Direction axis : List.of(Direction.EAST, Direction.SOUTH)) {
                        for (int offset = -2; offset <= 1; offset++) {
                           BlockPos origin = source.above().relative(axis, offset);
                           if (tried.add(origin.asLong())) {
                              Set<BlockPos> siteReserved = AiPortalCast.init_Set(origin, axis);
                              AiPortalCast.Inner_StreamUtil candidate = AiPortalCast.init(AiPortalCast.init_List(origin, axis), axis, siteReserved, this.init_aX_TaskQueue);
                              if (candidate.blocked() == null) {
                                 long usable = this.init_List.stream().filter(p -> !siteReserved.contains(p)).count();
                                 if (candidate.lavaNeeded() <= usable) {
                                    double score = candidate.lavaNeeded() * 25.0 + origin.distSqr(this.init_Minecraft.player.blockPosition());
                                    if (score < bestScore) {
                                       bestScore = score;
                                       bestPlan = candidate;
                                       bestOrigin = origin;
                                       bestAxis = axis;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }

                  if (bestPlan == null && this.load_I < 2) {
                     List<BlockPos> digs = this.init_List(this.init_List);
                     if (digs != null) {
                        this.load_I++;
                        this.init_ArrayDeque.clear();
                        this.init_ArrayDeque.addAll(digs);
                        AutismClientAddon.LOG
                           .info("[AI] portal cast: clearing {} blocks over the pool at {} for the frame", digs.size(), this.init_BlockPos.toShortString());
                        this.init_aX_NetHelper = AiPortalCast.Inner_NetHelper.EXCAVATE;
                        return this.load("Clearing " + digs.size() + " blocks for the frame");
                     }
                  }

                  if (bestPlan == null) {
                     return this.init(
                        AiFailure.NO_REACHABLE_TARGET,
                        "No castable 4x5 frame site at this lava pool (frame cells blocked, or too few lava sources); try another pool or game:dimension build:true",
                        null
                     );
                  }

                  this.load_BlockPos = bestOrigin;
                  this.init_Direction = bestAxis;
                  this.init_aX_StreamUtil = bestPlan;
                  this.load_Set = AiPortalCast.init_Set(this.load_BlockPos, this.init_Direction);
                  this.read_Set = new HashSet<>(AiDimension.frameBlocks(this.load_BlockPos, this.init_Direction));
                  JsonObject missing = AiPortalCast.init(
                     this.write_I,
                     this.update_I,
                     this.sync_I,
                     this.flush,
                     this.close,
                     this.open,
                     this.poll,
                     this.init_aX_StreamUtil.lavaNeeded(),
                     Math.max(16, this.init_aX_StreamUtil.moldBlocks() + 2)
                  );
                  if (!missing.keySet().isEmpty()) {
                     return this.init(AiFailure.MISSING_MATERIALS, "Portal cast at " + this.load_BlockPos.toShortString() + " is missing materials: " + missing, missing);
                  }

                  this.init_aX_NetHelper = this.read_Z ? AiPortalCast.Inner_NetHelper.CAST : AiPortalCast.Inner_NetHelper.WATER;
                  return this.load("Cast site at " + this.load_BlockPos.toShortString() + " along " + this.init_Direction.name().toLowerCase(Locale.ROOT));
               }

               AiOperation.Result write() {
                  if (AiGameState.countInventoryItem("minecraft:water_bucket") > 0) {
                     this.init_aX_NetHelper = AiPortalCast.Inner_NetHelper.CAST;
                     return this.load("Water ready");
                  }

                  if (this.load_AiOperation == null) {
                     BlockPos open = this.init(this.init_Minecraft.player.blockPosition(), 32, 8, false, true);
                     BlockPos source = open != null ? open : this.init(this.init_Minecraft.player.blockPosition(), 32, 8, false);
                     if (source == null) {
                        return this.init(AiFailure.MISSING_MATERIALS, "No water source within 32 blocks to fill the bucket", this.init_JsonObject_2("minecraft:water_bucket"));
                     }

                     if (this.init_Minecraft.player.blockPosition().distSqr(source) > 9.0) {
                        AiOperation.Result moved = this.init(() -> this.init(source, 2.0, 45), "Travelling to water");
                        if (moved != null) {
                           return moved;
                        }
                     }

                     this.load_AiOperation = this.init("water", source, 20);
                  }

                  AiOperation.Result filled = this.init_AiOperation_Result_10("Filling the water bucket");
                  if (filled != null) {
                     return filled;
                  }

                  this.init_aX_NetHelper = AiPortalCast.Inner_NetHelper.CAST;
                  return this.load("Water bucket filled");
               }

               AiOperation.Result update() {
                  if (this.push >= this.init_aX_StreamUtil.steps().size()) {
                     for (BlockPos cell : this.read_Set) {
                        if (!this.init_Minecraft.level.getBlockState(cell).is(Blocks.OBSIDIAN)) {
                           return this.init(AiFailure.IMPOSSIBLE_STATE, "Frame cell " + cell.toShortString() + " is not obsidian after the cast", null);
                        }
                     }

                     this.init_aX_NetHelper = AiPortalCast.Inner_NetHelper.LIGHT;
                     return this.load("Frame complete; lighting the portal");
                  } else {
                     AiPortalCast.Inner_BufferHelper step = this.init_aX_StreamUtil.steps().get(this.push);
                     if (this.init_Minecraft.level.getBlockState(step.cell()).is(Blocks.OBSIDIAN) && !this.init_aX_RenderState.init()) {
                        this.init_V_20();
                        return this.load("Frame cell " + step.cell().toShortString() + " is obsidian");
                     }

                     if (this.init_aX_RenderState.init()
                        && this.init_aX_CachePool != AiPortalCast.Inner_CachePool.SCOOP
                        && this.init_Minecraft.level.getBlockState(step.cell()).is(Blocks.OBSIDIAN)
                        && AiServerEvidence.blockRevision(step.cell()) > this.load_J) {
                        this.init_aX_CachePool = AiPortalCast.Inner_CachePool.SCOOP;
                        this.pop = 0;
                     }

                     AiOperation.Result var10000;
                     switch (this.init_aX_CachePool) {
                        case STANCE:
                           var10000 = this.init(step);
                           break;
                        case MOLDS:
                           var10000 = this.load(step);
                           break;
                        case LAVA_REFILL:
                           var10000 = this.read(step);
                           break;
                        case LAVA_POUR:
                           var10000 = this.write(step);
                           break;
                        case WATER_POUR:
                           var10000 = this.update(step);
                           break;
                        case SCOOP:
                           var10000 = this.sync(step);
                           break;
                        default:
                           throw new MatchException(null, null);
                     }

                     return var10000;
                  }
               }

               AiOperation.Result init(AiPortalCast.Inner_BufferHelper step) {
                  this.load_J = AiServerEvidence.blockRevision(step.cell());
                  if (step.action() == AiPortalCast.Inner_DataUtil.WATER_OVER_LAVA && this.init_aX_ConfigStore(step.cell()) != AiPortalCast.Inner_ConfigStore.LAVA) {
                     return ++this.pop > 140
                        ? this.init(AiFailure.IMPOSSIBLE_STATE, "Frame cell " + step.cell().toShortString() + " no longer holds a lava source", null)
                        : this.load("Waiting for the lava at " + step.cell().toShortString() + " to settle");
                  }

                  this.pop = 0;
                  double reach = Math.max(4.5, this.init_Minecraft.player.blockInteractionRange()) - 0.6;
                  if (this.init_Minecraft.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(step.cell())) > reach * reach) {
                     AiOperation.Result moved = this.init(() -> this.init(step.cell(), 2.2, 45), "Moving to frame cell " + step.cell().toShortString());
                     if (moved != null) {
                        return moved;
                     }
                  }

                  List<BlockPos> queue = new ArrayList<>(step.dams());
                  if (step.face() != null && step.face().mold() != null && !queue.contains(step.face().mold())) {
                     queue.add(step.face().mold());
                  }

                  this.init_aX_SyncManager = null;
                  if (step.action() == AiPortalCast.Inner_DataUtil.LAVA_THEN_WATER && this.init_aX_ConfigStore(step.cell()) != AiPortalCast.Inner_ConfigStore.LAVA) {
                     this.init_aX_SyncManager = this.init_aX_SyncManager(step.cell());
                     if (this.init_aX_SyncManager == null) {
                        return this.init(AiFailure.PLACEMENT_IMPOSSIBLE, "No face to pour lava against at " + step.cell().toShortString(), null);
                     }

                     if (this.init_aX_SyncManager.mold() != null && !queue.contains(this.init_aX_SyncManager.mold())) {
                        queue.add(this.init_aX_SyncManager.mold());
                     }
                  }

                  this.load_List = queue;
                  this.set = 0;
                  this.write_Set = new HashSet<>();

                  for (BlockPos zone : AiPortalCast.init_List(step.waterCell())) {
                     if (this.init_Minecraft.level.getBlockState(zone).is(Blocks.OBSIDIAN)) {
                        this.write_Set.add(zone);
                     }
                  }

                  this.init_aX_CachePool = AiPortalCast.Inner_CachePool.MOLDS;
                  return this.load("Casting frame cell " + step.cell().toShortString() + " (" + step.action().name().toLowerCase(Locale.ROOT) + ")");
               }

               boolean init_Z_7(BlockPos cell) {
                  for (Direction d : Direction.values()) {
                     BlockPos side = cell.relative(d);
                     if (!this.init_Minecraft.level.getBlockState(side).getCollisionShape(this.init_Minecraft.level, side).isEmpty()) {
                        return true;
                     }
                  }

                  return false;
               }

               BlockPos init(BlockPos mold, AiPortalCast.Inner_BufferHelper step) {
                  BlockPos best = null;
                  double bestDistance = 1.7976931348623157E308;
                  BlockPos feet = this.init_Minecraft.player.blockPosition();

                  for (Direction d : Direction.values()) {
                     BlockPos side = mold.relative(d);
                     if (!this.load_Set.contains(side)
                        && !side.equals(step.cell())
                        && !side.equals(step.waterCell())
                        && !this.load_List.contains(side)
                        && !side.equals(feet)
                        && !side.equals(feet.above())
                        && this.init_aX_ConfigStore(side) == AiPortalCast.Inner_ConfigStore.AIR
                        && this.init_Z_7(side)) {
                        double distance = this.init_Minecraft.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(side));
                        if (distance < bestDistance) {
                           bestDistance = distance;
                           best = side;
                        }
                     }
                  }

                  return best;
               }

               AiOperation.Result load(AiPortalCast.Inner_BufferHelper step) {
                  if (this.init_Z && this.init_AiOperation != null) {
                     AiOperation.Result r = this.init_AiOperation.tick();
                     if (!r.terminal()) {
                        return this.load("Moving to reach a mold block");
                     }

                     this.init_AiOperation.cancel();
                     this.init_AiOperation = null;
                     this.init_Z = false;
                  }

                  while (this.set < this.load_List.size()) {
                     BlockPos mold = this.load_List.get(this.set);
                     AiPortalCast.Inner_ConfigStore there = this.init_aX_ConfigStore(mold);
                     if (there != AiPortalCast.Inner_ConfigStore.SOLID && there != AiPortalCast.Inner_ConfigStore.OBSIDIAN) {
                        Item item = this.init_Item(AiPortalCast.init_List);
                        if (item == null) {
                           return this.init(AiFailure.MISSING_MATERIALS, "Out of throwaway blocks for the mold at " + mold.toShortString(), this.init_JsonObject_2("minecraft:dirt"));
                        }

                        if (!mold.equals(this.init_DataPipe.targetPos()) && !this.init_Z_7(mold)) {
                           BlockPos support = this.init(mold, step);
                           if (support != null && this.sync_Set.add(support)) {
                              List<BlockPos> withSupport = new ArrayList<>(this.load_List);
                              withSupport.add(this.set, support);
                              this.load_List = withSupport;
                              AutismClientAddon.LOG.info("[AI] portal cast: a support at {} for the mold at {}", support.toShortString(), mold.toShortString());
                              continue;
                           }
                        }

                        if (!mold.equals(this.init_DataPipe.targetPos())) {
                           this.init_DataPipe.start(mold, item);
                        }

                        autismclient.ai.engine.AiPlacer.PlaceStatus placed = this.init_DataPipe.tick();
                        if (placed != autismclient.ai.engine.AiPlacer.PlaceStatus.PLACED) {
                           if (placed == autismclient.ai.engine.AiPlacer.PlaceStatus.FAILED) {
                              if (!this.init_Set.contains(mold)) {
                                 this.init_Set.add(mold);
                                 this.init_DataPipe.reset();
                                 this.init_AiOperation = AiGoto.approachToPlaceCompat(mold, item, 30, null, false);
                                 this.init_Z = true;
                                 return this.load("Moving to reach the mold block at " + mold.toShortString());
                              }

                              return this.init(
                                 AiFailure.PLACEMENT_IMPOSSIBLE, "Could not place the mold block at " + mold.toShortString() + ": " + this.init_DataPipe.failureReason(), null
                              );
                           }

                           return this.load("Placing the mold at " + mold.toShortString());
                        }

                        this.set++;
                     } else {
                        this.set++;
                     }
                  }

                  this.init_aX_CachePool = step.action() == AiPortalCast.Inner_DataUtil.LAVA_THEN_WATER && this.init_aX_ConfigStore(step.cell()) != AiPortalCast.Inner_ConfigStore.LAVA
                     ? AiPortalCast.Inner_CachePool.LAVA_REFILL
                     : AiPortalCast.Inner_CachePool.WATER_POUR;
                  this.pop = 0;
                  this.write_Z = false;
                  return this.load("Molds placed");
               }

               AiOperation.Result read(AiPortalCast.Inner_BufferHelper step) {
                  if (AiGameState.countInventoryItem("minecraft:lava_bucket") > 0) {
                     this.init_aX_CachePool = AiPortalCast.Inner_CachePool.LAVA_POUR;
                     this.pop = 0;
                     this.write_Z = false;
                     return this.load("Lava ready");
                  }

                  if (this.load_AiOperation == null) {
                     List<BlockPos> sources = new ArrayList<>();

                     for (BlockPos source : this.init_List) {
                        if (this.init_aX_ConfigStore(source) == AiPortalCast.Inner_ConfigStore.LAVA) {
                           sources.add(source);
                        }
                     }

                     BlockPos refill = AiPortalCast.init(sources, this.load_Set, this.init_Minecraft.player.blockPosition());
                     if (refill == null) {
                        return this.init(AiFailure.MISSING_MATERIALS, "The lava pool ran out of usable source cells", this.init_JsonObject_2("minecraft:lava_bucket"));
                     }

                     if (this.init_Minecraft.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(refill)) > 12.0) {
                        AiOperation.Result moved = this.init(() -> this.init(refill, 2.0, 45), "Moving to the pool for a lava refill");
                        if (moved != null) {
                           return moved;
                        }
                     }

                     this.load_AiOperation = this.init("lava", refill, 20);
                  }

                  AiOperation.Result filled = this.init_AiOperation_Result_10("Scooping lava from the pool");
                  if (filled != null) {
                     return filled;
                  }

                  this.init_aX_CachePool = AiPortalCast.Inner_CachePool.LAVA_POUR;
                  this.pop = 0;
                  this.write_Z = false;
                  return this.load("Lava bucket filled");
               }

               AiOperation.Result write(AiPortalCast.Inner_BufferHelper step) {
                  if (this.init_aX_ConfigStore(step.cell()) == AiPortalCast.Inner_ConfigStore.LAVA) {
                     this.init_aX_CachePool = AiPortalCast.Inner_CachePool.WATER_POUR;
                     this.pop = 0;
                     this.write_Z = false;
                     this.get++;
                     return this.load("Lava in place at " + step.cell().toShortString());
                  }

                  if (AiGameState.countInventoryItem("minecraft:lava_bucket") <= 0) {
                     if (++this.pop > 40) {
                        if (++this.tick >= 2) {
                           return this.init(AiFailure.PLACEMENT_IMPOSSIBLE, "Lava did not stay in frame cell " + step.cell().toShortString(), null);
                        }

                        this.pop = 0;
                        this.init_aX_CachePool = AiPortalCast.Inner_CachePool.LAVA_REFILL;
                     }

                     return this.load("Confirming the lava pour at " + step.cell().toShortString());
                  } else {
                     if (!this.write_Z && !this.init_aX_JsonUtil.init_Z()) {
                        AiOperation.Result approached = this.init(this.init_aX_SyncManager, true);
                        if (approached != null) {
                           return approached;
                        }
                     }

                     if (++this.pop > 60 * (this.tick + 1)) {
                        return this.init(AiFailure.PLACEMENT_IMPOSSIBLE, "Could not aim at the lava pour face for " + step.cell().toShortString(), null);
                     } else if (AiItems.equipHotbar(AiItems.resolveItem("minecraft:lava_bucket")) < 0) {
                        this.init_aX_CachePool = AiPortalCast.Inner_CachePool.LAVA_REFILL;
                        return this.load("Refilling the lava bucket");
                     } else if (this.init_Minecraft.player.containerMenu != this.init_Minecraft.player.inventoryMenu) {
                        this.init_Minecraft.player.closeContainer();
                        return this.load("Closing a screen before pouring");
                     } else {
                        AiOperation.Result poured = this.init(this.init_aX_SyncManager);
                        return poured != null ? poured : this.load("Pouring lava into " + step.cell().toShortString());
                     }
                  }
               }

               AiOperation.Result update(AiPortalCast.Inner_BufferHelper step) {
                  if (this.init_Minecraft.level.getBlockState(step.cell()).is(Blocks.OBSIDIAN) && AiServerEvidence.blockRevision(step.cell()) > this.load_J) {
                     this.init_aX_CachePool = AiPortalCast.Inner_CachePool.SCOOP;
                     this.pop = 0;
                     return this.load("Obsidian confirmed at " + step.cell().toShortString());
                  }

                  AiOperation.Result stray = this.flush(step);
                  if (stray != null) {
                     return stray;
                  }

                  if (!this.init_aX_RenderState.init() && AiGameState.countInventoryItem("minecraft:water_bucket") > 0) {
                     if (!this.write_Z && !this.init_aX_JsonUtil.init_Z()) {
                        AiOperation.Result approached = this.init(step.face(), false);
                        if (approached != null) {
                           return approached;
                        }
                     }

                     if (++this.pop > 60) {
                        if (++this.tick >= 2) {
                           return this.init(AiFailure.PLACEMENT_IMPOSSIBLE, "Could not aim at the pour face above " + step.cell().toShortString(), null);
                        }

                        this.pop = 0;
                        this.write_Z = false;
                     }

                     if (AiItems.equipHotbar(AiItems.resolveItem("minecraft:water_bucket")) < 0) {
                        return this.init(AiFailure.MISSING_MATERIALS, "Lost the water bucket mid-cast", this.init_JsonObject_2("minecraft:water_bucket"));
                     }

                     if (this.init_Minecraft.player.containerMenu != this.init_Minecraft.player.inventoryMenu) {
                        this.init_Minecraft.player.closeContainer();
                        return this.load("Closing a screen before pouring");
                     }

                     int filledBefore = AiGameState.countInventoryItem("minecraft:water_bucket");
                     boolean alreadyClicked = this.write_Z;
                     AiOperation.Result poured = this.init(step.face());
                     if (!alreadyClicked && this.write_Z) {
                        this.init_aX_RenderState.init(step.waterCell(), filledBefore);
                     }

                     return poured != null ? poured : this.load("Pouring water above " + step.cell().toShortString());
                  } else {
                     return ++this.pop > 140
                        ? this.init(
                           AiFailure.PLACEMENT_IMPOSSIBLE,
                           "Water did not convert frame cell "
                              + step.cell().toShortString()
                              + " (now "
                              + BuiltInRegistries.BLOCK.getKey(this.init_Minecraft.level.getBlockState(step.cell()).getBlock())
                              + ")",
                           null
                        )
                        : this.load("Waiting for obsidian at " + step.cell().toShortString());
                  }
               }

               AiOperation.Result sync(AiPortalCast.Inner_BufferHelper step) {
                  if (!this.init_aX_RenderState.init()) {
                     this.init_V_20();
                     return this.load("Water recovered; next cell");
                  }

                  if (this.load_AiOperation == null) {
                     this.load_AiOperation = this.init("water", step.waterCell(), 20);
                  }

                  AiOperation.Result scooped = this.poll();
                  if (scooped == null) {
                     return this.load("Re-scooping the pour water");
                  } else if (this.init_aX_RenderState
                     .init(step.waterCell(), AiGameState.countInventoryItem("minecraft:water_bucket"), scooped.status() == AiOperation.Status.SUCCEEDED)) {
                     this.init_aX_JsonUtil.init(step.waterCell());
                     this.init_V_20();
                     return this.load("Water recovered; next cell");
                  } else {
                     return scooped.status() == AiOperation.Status.UNCERTAIN
                        ? this.init(AiOperation.Status.UNCERTAIN, "The poured water refill is unconfirmed: " + scooped.message())
                        : this.init(
                           AiFailure.MISSING_MATERIALS,
                           "The pour's water was lost at "
                              + step.waterCell().toShortString()
                              + "; refill a water bucket and re-run (finished cells are skipped)",
                           this.init_JsonObject_2("minecraft:water_bucket")
                        );
                  }
               }

               AiOperation.Result sync() {
                  BlockPos portalCell = this.init_BlockPos_9();
                  if (portalCell != null) {
                     this.update_Z = true;
                     AiRunTrace.recordGlobalSplit("portal_lit", this.load_BlockPos.toShortString() + " cast:" + this.run + " litWith:" + this.load_Str);
                     return this.init(AiOperation.Status.SUCCEEDED, "Portal cast and lit at " + this.load_BlockPos.toShortString());
                  }

                  for (BlockPos interior : AiDimension.write(this.load_BlockPos, this.init_Direction)) {
                     BlockState state = this.init_Minecraft.level.getBlockState(interior);
                     if (!state.isAir() && !state.is(Blocks.FIRE)) {
                        if (state.getFluidState().is(FluidTags.WATER) && AiGameState.countInventoryItem("minecraft:bucket") > 0) {
                           if (this.load_AiOperation == null) {
                              this.load_AiOperation = this.init("water", interior, 15);
                           }

                           AiOperation.Result cleared = this.init_AiOperation_Result_10("Clearing water from the doorway");
                           if (cleared != null) {
                              return cleared;
                           }

                           return this.load("Doorway cleared");
                        }

                        return this.init(
                           AiFailure.IMPOSSIBLE_STATE,
                           "The portal interior at " + interior.toShortString() + " is blocked by " + BuiltInRegistries.BLOCK.getKey(state.getBlock()),
                           null
                        );
                     }
                  }

                  if (this.load_Str.isEmpty()) {
                     this.load_V_11();
                     String picked = AiPortalCast.init(this.close, this.open, this.update_I > 0 || this.sync_I > 0, this.poll);
                     if (picked == null) {
                        return this.init(AiFailure.MISSING_MATERIALS, "Nothing to light the portal with", this.init_JsonObject_2("minecraft:flint_and_steel"));
                     }

                     this.load_Str = picked;
                  }

                  if (++this.add > ("fire_spread".equals(this.load_Str) ? 600 : 120)) {
                     return this.init(
                        AiOperation.Status.FAILED,
                        "fire_spread".equals(this.load_Str)
                           ? "The doorway wood did not spread fire into the frame in time"
                           : "The frame is complete but the portal did not light"
                     );
                  } else {
                     return "fire_spread".equals(this.load_Str) ? this.close() : this.flush();
                  }
               }

               AiOperation.Result flush() {
                  if (AiItems.equipHotbar(AiItems.resolveItem("minecraft:" + this.load_Str)) < 0) {
                     this.load_Str = "";
                     return this.load("Re-selecting the lighter");
                  }

                  BlockPos floor = this.load_BlockPos.below();
                  this.init_DataUtil.aimAt(autismclient.ai.engine.AiAim.aimPointForBlockFace(floor, Direction.UP));
                  this.init_DataUtil.tick();
                  if (!autismclient.ai.engine.AiAim.isLookingAtBlock(floor)) {
                     return this.load("Turning to light the portal");
                  }

                  BlockHitResult hit = autismclient.ai.engine.AiAim.raycastLookBlock();
                  if (hit == null) {
                     return this.load("Turning to light the portal");
                  }

                  this.init_Minecraft.gameMode.useItemOn(this.init_Minecraft.player, InteractionHand.MAIN_HAND, hit);
                  Compat263.swing(this.init_Minecraft.player, InteractionHand.MAIN_HAND);
                  return this.load("Lighting the portal");
               }

               AiOperation.Result close() {
                  BlockPos side = this.load_BlockPos.relative(this.init_Direction.getCounterClockWise());
                  if (this.clear == 0) {
                     Item wood = this.init_Item(AiPortalCast.load_List);
                     if (wood == null) {
                        this.load_Str = "";
                        return this.load("Lost the doorway wood");
                     }

                     if (!this.load_BlockPos.equals(this.init_DataPipe.targetPos())) {
                        this.init_DataPipe.start(this.load_BlockPos, wood);
                     }

                     autismclient.ai.engine.AiPlacer.PlaceStatus placed = this.init_DataPipe.tick();
                     if (placed == autismclient.ai.engine.AiPlacer.PlaceStatus.PLACED) {
                        this.clear = 1;
                        return this.load("Doorway wood placed");
                     } else {
                        return placed == autismclient.ai.engine.AiPlacer.PlaceStatus.FAILED
                           ? this.init(AiFailure.PLACEMENT_IMPOSSIBLE, "Could not place the doorway wood: " + this.init_DataPipe.failureReason(), null)
                           : this.load("Placing the doorway wood");
                     }
                  } else if (this.clear == 1) {
                     if (this.init_aX_ConfigStore(side) == AiPortalCast.Inner_ConfigStore.LAVA) {
                        this.clear = 3;
                        return this.load("Lava already beside the doorway");
                     }

                     this.load_aX_SyncManager = this.init_aX_SyncManager(side);
                     if (this.load_aX_SyncManager == null) {
                        return this.init(AiFailure.PLACEMENT_IMPOSSIBLE, "No face to pour lava beside the doorway", null);
                     }

                     BlockPos mold = this.load_aX_SyncManager.mold();
                     if (mold != null
                        && this.init_aX_ConfigStore(mold) != AiPortalCast.Inner_ConfigStore.SOLID
                        && this.init_aX_ConfigStore(mold) != AiPortalCast.Inner_ConfigStore.OBSIDIAN) {
                        Item moldItem = this.init_Item(AiPortalCast.init_List);
                        if (moldItem == null) {
                           return this.init(AiFailure.MISSING_MATERIALS, "No throwaway block for the fire-spread mold", this.init_JsonObject_2("minecraft:dirt"));
                        }

                        if (!mold.equals(this.init_DataPipe.targetPos())) {
                           this.init_DataPipe.start(mold, moldItem);
                        }

                        autismclient.ai.engine.AiPlacer.PlaceStatus placed = this.init_DataPipe.tick();
                        if (placed == autismclient.ai.engine.AiPlacer.PlaceStatus.PLACED) {
                           this.clear = 2;
                           this.pop = 0;
                           this.write_Z = false;
                           return this.load("Fire-spread mold placed");
                        } else {
                           return placed == autismclient.ai.engine.AiPlacer.PlaceStatus.FAILED
                              ? this.init(AiFailure.PLACEMENT_IMPOSSIBLE, "Could not place the fire-spread mold: " + this.init_DataPipe.failureReason(), null)
                              : this.load("Placing the fire-spread mold");
                        }
                     } else {
                        this.clear = 2;
                        this.pop = 0;
                        this.write_Z = false;
                        return this.load("Fire-spread pour face ready");
                     }
                  } else {
                     if (this.clear != 2) {
                        return this.load("Waiting for the doorway wood to catch fire");
                     }

                     if (this.init_aX_ConfigStore(side) == AiPortalCast.Inner_ConfigStore.LAVA) {
                        this.write_Z = false;
                        this.clear = 3;
                        return this.load("Lava beside the doorway; waiting for the wood to catch");
                     }

                     if (!this.write_Z && !this.init_aX_JsonUtil.init_Z() && AiGameState.countInventoryItem("minecraft:lava_bucket") > 0) {
                        AiOperation.Result approached = this.init(this.load_aX_SyncManager, true);
                        if (approached != null) {
                           return approached;
                        }
                     }

                     if (++this.pop > 120) {
                        return this.init(AiFailure.PLACEMENT_IMPOSSIBLE, "Could not pour lava beside the doorway", null);
                     }

                     if (AiGameState.countInventoryItem("minecraft:lava_bucket") > 0) {
                        if (AiItems.equipHotbar(AiItems.resolveItem("minecraft:lava_bucket")) < 0) {
                           return this.init(AiFailure.MISSING_MATERIALS, "Lost the lava bucket for the fire spread", this.init_JsonObject_2("minecraft:lava_bucket"));
                        } else if (this.init_Minecraft.player.containerMenu != this.init_Minecraft.player.inventoryMenu) {
                           this.init_Minecraft.player.closeContainer();
                           return this.load("Closing a screen before pouring");
                        } else {
                           AiOperation.Result poured = this.init(this.load_aX_SyncManager);
                           return poured != null ? poured : this.load("Pouring lava beside the doorway");
                        }
                     } else {
                        if (this.load_AiOperation == null) {
                           List<BlockPos> sources = new ArrayList<>();

                           for (BlockPos pool : this.init_List) {
                              if (this.init_aX_ConfigStore(pool) == AiPortalCast.Inner_ConfigStore.LAVA) {
                                 sources.add(pool);
                              }
                           }

                           BlockPos refill = AiPortalCast.init(sources, this.load_Set, this.init_Minecraft.player.blockPosition());
                           if (refill == null) {
                              return this.init(AiFailure.MISSING_MATERIALS, "No lava left for the fire spread", this.init_JsonObject_2("minecraft:lava_bucket"));
                           }

                           this.load_AiOperation = this.init("lava", refill, 20);
                        }

                        AiOperation.Result filled = this.init_AiOperation_Result_10("Scooping lava for the fire spread");
                        if (filled != null) {
                           return filled;
                        }

                        this.pop = 0;
                        this.write_Z = false;
                        return this.load("Lava ready for the fire spread");
                     }
                  }
               }

               AiOperation.Result init(AiPortalCast.Inner_SyncManager face, boolean lava) {
                  if (face == null) {
                     return this.init(AiFailure.PLACEMENT_IMPOSSIBLE, "The planned pour face is unavailable", null);
                  }

                  if (autismclient.ai.engine.AiMotionSafety.isPlayerSupported()
                     && (!lava || AiPortalCast.init(this.init_Minecraft.player.blockPosition(), face.support().relative(face.face())))
                     && autismclient.ai.engine.AiBlockInteraction.findFaceHit(this.init_Minecraft, face.support(), face.face()) != null) {
                     return null;
                  }

                  if (this.update_Set.contains(face)) {
                     return this.init(AiFailure.PLACEMENT_IMPOSSIBLE, "The required pour face is still hidden after approaching " + face.support().toShortString(), null);
                  }

                  long now = System.nanoTime();
                  if (!face.equals(this.read_aX_SyncManager)) {
                     this.read_aX_SyncManager = face;
                     this.read_J = Math.min(this.init_J, now + 30000000000L);
                  }

                  int seconds = AiPortalCast.init(this.read_J, now);
                  if (seconds <= 0) {
                     return this.init(AiFailure.TIMEOUT, "No time remains to reach the required pour face", null);
                  }

                  AiOperation child = AiGoto.travelToCompat(
                     face.support(),
                     0.6,
                     true,
                     true,
                     false,
                     seconds,
                     autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST,
                     true,
                     2.0,
                     AiPortalCast.init(
                        face,
                        this.load_Set,
                        this.load_List,
                        this.sync_Set,
                        Math.max(1.5, this.init_Minecraft.player.blockInteractionRange() - 0.25),
                        this.init_Minecraft.player.getEyeHeight(),
                        lava
                     )
                  );
                  this.init_aX_StateCache = new AiPortalCast.Inner_StateCache(child, this.read_J, System::nanoTime);
                  this.load_Z = true;
                  return this.open();
               }

               AiOperation.Result open() {
                  AiOperation.Result result = this.init_aX_StateCache.tick();
                  if (!result.terminal()) {
                     return this.load("Moving to see the required pour face");
                  } else {
                     this.load_Z = false;
                     this.update_Set.add(this.read_aX_SyncManager);
                     if (result.status() == AiOperation.Status.UNCERTAIN) {
                        return this.init(AiOperation.Status.UNCERTAIN, result.message());
                     } else {
                        return result.status() != AiOperation.Status.SUCCEEDED
                           ? this.init(result.failure(), result.message(), null)
                           : this.load("Required pour face reached");
                     }
                  }
               }

               AiOperation.Result init(AiPortalCast.Inner_SyncManager face) {
                  if (!this.write_Z && !this.init_aX_JsonUtil.init_Z() && face != null) {
                     BlockPos pourCell = face.support().relative(face.face());
                     String bucket = BuiltInRegistries.ITEM.getKey(this.init_Minecraft.player.getMainHandItem().getItem()).toString();
                     if (bucket.equals("minecraft:lava_bucket")) {
                        this.init_Minecraft.options.keyUp.setDown(false);
                        this.init_Minecraft.options.keyDown.setDown(false);
                        this.init_Minecraft.options.keyLeft.setDown(false);
                        this.init_Minecraft.options.keyRight.setDown(false);
                        this.init_Minecraft.options.keyJump.setDown(false);
                        this.init_Minecraft.options.keySprint.setDown(false);
                        if (!AiPortalCast.init(this.init_Minecraft.player.onGround(), this.init_Minecraft.player.getDeltaMovement())) {
                           return this.load("Settling before the lava pour");
                        }

                        if (!AiPortalCast.init(this.init_Minecraft.player.blockPosition(), pourCell)) {
                           return this.init(AiFailure.PLACEMENT_IMPOSSIBLE, "Refusing a lava pour into or beside the player's own cells at " + pourCell.toShortString(), null);
                        }
                     }

                     BlockHitResult visible = autismclient.ai.engine.AiBlockInteraction.findFaceHit(this.init_Minecraft, face.support(), face.face());
                     if (visible == null) {
                        return null;
                     } else {
                        this.init_DataUtil.aimAt(AiPortalCast.init(this.init_Minecraft.player.getEyePosition(), visible));
                        this.init_DataUtil.tick();
                        BlockHitResult hit = autismclient.ai.engine.AiAim.raycastLookBlock();
                        if (hit != null && hit.getBlockPos().equals(face.support()) && hit.getDirection() == face.face()) {
                           this.init_aX_JsonUtil.init(pourCell, bucket, AiGameState.countInventoryItem(bucket), AiServerEvidence.inventoryRevision(), AiServerEvidence.blockRevision(pourCell));
                           this.init_Minecraft.gameMode.useItem(this.init_Minecraft.player, InteractionHand.MAIN_HAND);
                           Compat263.swing(this.init_Minecraft.player, InteractionHand.MAIN_HAND);
                           this.write_Z = true;
                           return null;
                        } else {
                           return null;
                        }
                     }
                  } else {
                     return null;
                  }
               }

               AiOperation.Result flush(AiPortalCast.Inner_BufferHelper step) {
                  for (BlockPos zone : AiPortalCast.init_List(step.waterCell())) {
                     if (!this.read_Set.contains(zone) && !this.write_Set.contains(zone) && this.init_Minecraft.level.getBlockState(zone).is(Blocks.OBSIDIAN)) {
                        return this.init(AiFailure.PLACEMENT_IMPOSSIBLE, "The pour spread: obsidian formed outside the frame at " + zone.toShortString(), null);
                     }
                  }

                  return null;
               }

               AiPortalCast.Inner_SyncManager init_aX_SyncManager(BlockPos cell) {
                  AiPortalCast.Inner_ConfigStore below = this.init_aX_ConfigStore(cell.below());
                  return below != AiPortalCast.Inner_ConfigStore.SOLID && below != AiPortalCast.Inner_ConfigStore.OBSIDIAN
                     ? AiPortalCast.init(cell, this.init_Direction, this.load_Set, this.init_aX_TaskQueue, List.of())
                     : new AiPortalCast.Inner_SyncManager(cell.below().immutable(), Direction.UP, null);
               }

               AiOperation.Result init(Supplier<AiOperation> factory, String what) {
                  if (this.init_AiOperation == null) {
                     this.init_AiOperation = factory.get();
                  }

                  boolean pending = this.init_AiOperation.requiresReconciliationAfterEmergency();
                  AiOperation.Result r = this.init_AiOperation.tick();
                  if (!r.terminal()) {
                     return this.load(what);
                  } else {
                     r = AiGuardLayer.wrapPendingTermination(
                        r.status() != AiOperation.Status.SUCCEEDED && (pending || this.init_AiOperation.requiresReconciliationAfterEmergency()), r
                     );
                     this.init_AiOperation.cancel();
                     this.init_AiOperation = null;
                     if (r.status() == AiOperation.Status.UNCERTAIN) {
                        return this.init(AiOperation.Status.UNCERTAIN, what + ": " + r.message());
                     } else {
                        return r.status() != AiOperation.Status.SUCCEEDED ? this.init(r.failure(), what + ": " + r.message(), null) : null;
                     }
                  }
               }

               AiOperation.Result init_AiOperation_Result_10(String what) {
                  AiOperation.Result r = this.poll();
                  if (r == null) {
                     return this.load(what);
                  } else if (r.status() == AiOperation.Status.UNCERTAIN) {
                     return this.init(AiOperation.Status.UNCERTAIN, what + ": " + r.message());
                  } else {
                     return r.status() != AiOperation.Status.SUCCEEDED ? this.init(r.failure(), what + ": " + r.message(), null) : null;
                  }
               }

               AiOperation.Result poll() {
                  boolean pending = this.load_AiOperation.requiresReconciliationAfterEmergency();
                  AiOperation.Result r = this.load_AiOperation.tick();
                  if (!r.terminal()) {
                     return null;
                  }

                  r = AiGuardLayer.wrapPendingTermination(
                     r.status() != AiOperation.Status.SUCCEEDED && (pending || this.load_AiOperation.requiresReconciliationAfterEmergency()), r
                  );
                  this.load_AiOperation.cancel();
                  this.load_AiOperation = null;
                  return r;
               }

               AiOperation init(String fluid, BlockPos source, int seconds) {
                  return AiBucket.init(
                     AiJson.objectOf(
                        "action", "fill", "fluid", fluid, "x", source.getX(), "y", source.getY(), "z", source.getZ(), "timeoutSeconds", seconds
                     )
                  );
               }

               AiOperation.Result push() {
                  if (this.read_AiOperation != null) {
                     AiOperation.Result result = this.read_AiOperation.tick();
                     if (!result.terminal()) {
                        return this.load("Interrupted: " + this.init_Str + " - " + result.message());
                     }

                     this.read_AiOperation.cancel();
                     this.read_AiOperation = null;
                     return this.load("Resuming after " + this.init_Str);
                  } else {
                     if (!this.canInterruptNow()) {
                        return null;
                     }

                     this.init_X_BufferHelper = AiGuard.sampleStatus();
                     AiOperation action = AiGuard.createResponseOperation(this.init_X_CachePool, this.init_X_BufferHelper, this.init_I_22());
                     if (action == null) {
                        return null;
                     }

                     this.init_Str = AiGuard.decisionReason(this.init_X_CachePool, this.init_X_BufferHelper);
                     this.put++;
                     if (this.init_AiOperation != null) {
                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                     }

                     if (this.load_AiOperation != null) {
                        this.load_AiOperation.cancel();
                        this.load_AiOperation = null;
                     }

                     if (this.load_Z) {
                        this.init_aX_StateCache.cancel();
                        this.load_Z = false;
                     }

                     this.read_AiOperation = action;
                     return this.load("Interrupted: " + this.init_Str);
                  }
               }

               void init_V_20() {
                  this.run++;
                  this.push++;
                  this.init_aX_CachePool = AiPortalCast.Inner_CachePool.STANCE;
                  this.pop = 0;
                  this.tick = 0;
                  this.load_List = List.of();
                  this.set = 0;
                  this.init_aX_SyncManager = null;
                  this.write_Z = false;
               }

               void load_V_11() {
                  this.write_I = AiGameState.countInventoryItem("minecraft:water_bucket");
                  this.update_I = AiGameState.countInventoryItem("minecraft:lava_bucket");
                  this.sync_I = AiGameState.countInventoryItem("minecraft:bucket");
                  this.flush = this.init_I(AiPortalCast.init_List);
                  this.close = AiGameState.countInventoryItem("minecraft:flint_and_steel");
                  this.open = AiGameState.countInventoryItem("minecraft:fire_charge");
                  this.poll = this.init_I(AiPortalCast.load_List);
               }

               int init_I(List<String> ids) {
                  int total = 0;

                  for (String id : ids) {
                     total += AiGameState.countInventoryItem(id);
                  }

                  return total;
               }

               Item init_Item(List<String> ids) {
                  for (String id : ids) {
                     if (AiGameState.countInventoryItem(id) > 0) {
                        Item item = BuiltInRegistries.ITEM.getOptional(Identifier.parse(id)).orElse(null);
                        if (item instanceof BlockItem
                           && (ids != AiPortalCast.init_List || autismclient.ai.engine.AiRestock.canSpendStack(item.getDefaultInstance(), this.init_Minecraft.player.getInventory()))) {
                           return item;
                        }
                     }
                  }

                  return null;
               }

               BlockPos init_BlockPos_9() {
                  for (BlockPos interior : AiDimension.write(this.load_BlockPos, this.init_Direction)) {
                     if (this.init_Minecraft.level.getBlockState(interior).is(Blocks.NETHER_PORTAL)) {
                        return interior;
                     }
                  }

                  return null;
               }

               BlockPos init(BlockPos center, int radius, int vertical, boolean lava) {
                  return this.init(center, radius, vertical, lava, false);
               }

               BlockPos init(BlockPos center, int radius, int vertical, boolean lava, boolean open) {
                  BlockPos best = null;
                  double bestDistance = 1.7976931348623157E308;

                  for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -vertical, -radius), center.offset(radius, vertical, radius))) {
                     if (AiTerrain.isLoaded(this.init_Minecraft.level, pos)) {
                        FluidState fluid = this.init_Minecraft.level.getFluidState(pos);
                        if (fluid.isSource()
                           && fluid.is(lava ? FluidTags.LAVA : FluidTags.WATER)
                           && (!open || this.init_Minecraft.level.getBlockState(pos.above()).isAir())) {
                           double distance = center.distSqr(pos);
                           if (distance < bestDistance) {
                              bestDistance = distance;
                              best = pos.immutable();
                           }
                        }
                     }
                  }

                  return best;
               }

               List<BlockPos> init_List(BlockPos center) {
                  List<BlockPos> sources = new ArrayList<>();

                  for (BlockPos pos : BlockPos.betweenClosed(center.offset(-12, -6, -12), center.offset(12, 6, 12))) {
                     if (sources.size() >= 64) {
                        break;
                     }

                     if (AiTerrain.isLoaded(this.init_Minecraft.level, pos)) {
                        FluidState fluid = this.init_Minecraft.level.getFluidState(pos);
                        if (fluid.isSource() && fluid.is(FluidTags.LAVA)) {
                           sources.add(pos.immutable());
                        }
                     }
                  }

                  sources.sort(Comparator.comparingDouble(center::distSqr));
                  return sources;
               }

               List<BlockPos> init_List(List<BlockPos> sources) {
                  List<BlockPos> best = null;
                  double bestScore = 1.7976931348623157E308;
                  Set<Long> tried = new HashSet<>();
                  int considered = 0;

                  for (BlockPos source : sources) {
                     if (++considered > 24) {
                        break;
                     }

                     for (Direction axis : List.of(Direction.EAST, Direction.SOUTH)) {
                        for (int offset = -2; offset <= 1; offset++) {
                           BlockPos origin = source.above().relative(axis, offset);
                           if (tried.add(origin.asLong())) {
                              Set<BlockPos> need = new LinkedHashSet<>(AiDimension.write(origin, axis));

                              for (BlockPos cell : AiDimension.frameBlocks(origin, axis)) {
                                 need.add(cell);
                                 need.add(cell.above());
                              }

                              List<BlockPos> digs = new ArrayList<>();
                              boolean ok = true;

                              for (BlockPos cell : need) {
                                 if (this.init_aX_ConfigStore(cell) == AiPortalCast.Inner_ConfigStore.SOLID) {
                                    if (!this.load(cell)) {
                                       ok = false;
                                       break;
                                    }

                                    digs.add(cell.immutable());
                                 }
                              }

                              if (ok && !digs.isEmpty() && digs.size() <= 30) {
                                 Set<BlockPos> dug = new HashSet<>(digs);
                                 AiPortalCast.Inner_TaskQueue after = pos -> dug.contains(pos) ? AiPortalCast.Inner_ConfigStore.AIR : this.init_aX_ConfigStore(pos);
                                 Set<BlockPos> reserved = AiPortalCast.init_Set(origin, axis);
                                 AiPortalCast.Inner_StreamUtil plan = AiPortalCast.init(AiPortalCast.init_List(origin, axis), axis, reserved, after);
                                 if (plan.blocked() == null) {
                                    long usable = sources.stream().filter(q -> !reserved.contains(q)).count();
                                    if (plan.lavaNeeded() <= usable) {
                                       double score = digs.size() * 40.0
                                          + plan.lavaNeeded() * 25.0
                                          + origin.distSqr(this.init_Minecraft.player.blockPosition());
                                       if (score < bestScore) {
                                          bestScore = score;
                                          best = digs;
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }

                  if (best != null) {
                     best.sort(
                        Comparator.<BlockPos>comparingInt(q -> -q.getY()).thenComparingDouble(q -> q.distSqr(this.init_Minecraft.player.blockPosition()))
                     );
                  }

                  return best;
               }

               boolean load(BlockPos cell) {
                  if (!AiTerrain.isLoaded(this.init_Minecraft.level, cell)) {
                     return false;
                  } else if (AiPortalCast.load(this.init_Minecraft.level, cell)) {
                     return false;
                  } else {
                     BlockState state = this.init_Minecraft.level.getBlockState(cell);
                     if (state.getFluidState().isEmpty()
                        && !state.hasBlockEntity()
                        && !state.is(Blocks.OBSIDIAN)
                        && !state.is(Blocks.BEDROCK)
                        && !(state.getBlock() instanceof FallingBlock)) {
                        float hardness = state.getDestroySpeed(this.init_Minecraft.level, cell);
                        return hardness >= 0.0F && hardness <= 5.0F;
                     } else {
                        return false;
                     }
                  }
               }

               AiOperation init(final BlockPos target, double radius, final int seconds) {
                  final AiOperation first = this.load(target, radius, seconds);
                  return radius >= 3.0 ? first : new AiOperation() {
                     AiOperation init_AiOperation = first;
                     boolean init_Z;

                     @Override
                     public AiOperation.Result tick() {
                        AiOperation.Result r = this.init_AiOperation.tick();
                        if (r.terminal() && !this.init_Z && r.status() != AiOperation.Status.SUCCEEDED && r.status() != AiOperation.Status.CANCELLED) {
                           this.init_AiOperation.cancel();
                           this.init_Z = true;
                           this.init_AiOperation = load(target, 3.0, seconds);
                           return AiGameOperations.result(AiOperation.Status.RUNNING, r.message() + "; trying a wider stance", new JsonObject());
                        } else {
                           return r;
                        }
                     }

                     @Override
                     public boolean canInterruptNow() {
                        return this.init_AiOperation.canInterruptNow();
                     }

                     @Override
                     public boolean requiresReconciliationAfterEmergency() {
                        return this.init_AiOperation.requiresReconciliationAfterEmergency();
                     }

                     @Override
                     public void cancel() {
                        this.init_AiOperation.cancel();
                     }
                  };
               }

               AiOperation load(BlockPos target, double radius, int seconds) {
                  Set<Long> keep = new HashSet<>();
                  if (this.load_Set != null) {
                     for (BlockPos cell : this.load_Set) {
                        keep.add(cell.asLong());

                        for (Direction d : Direction.values()) {
                           keep.add(cell.relative(d).asLong());
                        }
                     }
                  }

                  return AiGoto.travelToCompat(
                     target,
                     radius,
                     true,
                     true,
                     false,
                     seconds,
                     autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST,
                     true,
                     2.0,
                     AiGoto.protectingCellsCompat(autismclient.ai.engine.AiPathfinder.PathGoal.nearCompat(target, radius), keep)
                  );
               }

               AiOperation.Result pop() {
                  if (this.init_AiOperation != null) {
                     AiOperation.Result r = this.init_AiOperation.tick();
                     if (!r.terminal()) {
                        return this.load("Clearing the frame site (" + (this.init_ArrayDeque.size() + 1) + " left)");
                     }

                     this.init_AiOperation.cancel();
                     this.init_AiOperation = null;
                     if (r.status() != AiOperation.Status.SUCCEEDED && ++this.read_I > 3) {
                        return this.init(AiFailure.NO_REACHABLE_TARGET, "Could not clear the frame site: " + r.message(), null);
                     }
                  }

                  while (!this.init_ArrayDeque.isEmpty() && this.init_aX_ConfigStore(this.init_ArrayDeque.peek()) != AiPortalCast.Inner_ConfigStore.SOLID) {
                     this.init_ArrayDeque.poll();
                  }

                  if (this.init_ArrayDeque.isEmpty()) {
                     this.init_aX_NetHelper = AiPortalCast.Inner_NetHelper.SITE;
                     return this.load("Frame site cleared; planning the cast");
                  } else {
                     BlockPos cell = this.init_ArrayDeque.poll();
                     this.init_AiOperation = autismclient.ai.engine.AiBreakBlock.breakBlock(cell, 20, false);
                     return this.load("Clearing the frame site at " + cell.toShortString());
                  }
               }

               AiPortalCast.Inner_ConfigStore init_aX_ConfigStore(BlockPos pos) {
                  if (!AiTerrain.isLoaded(this.init_Minecraft.level, pos)) {
                     return AiPortalCast.Inner_ConfigStore.SOLID;
                  }

                  BlockState state = this.init_Minecraft.level.getBlockState(pos);
                  if (state.is(Blocks.OBSIDIAN)) {
                     return AiPortalCast.Inner_ConfigStore.OBSIDIAN;
                  }

                  FluidState fluid = state.getFluidState();
                  if (!fluid.isEmpty()) {
                     if (!fluid.isSource()) {
                        return AiPortalCast.Inner_ConfigStore.SOLID;
                     } else if (fluid.is(FluidTags.LAVA)) {
                        return AiPortalCast.Inner_ConfigStore.LAVA;
                     } else {
                        return fluid.is(FluidTags.WATER) ? AiPortalCast.Inner_ConfigStore.WATER : AiPortalCast.Inner_ConfigStore.SOLID;
                     }
                  } else {
                     return !state.isAir() && !state.canBeReplaced() ? AiPortalCast.Inner_ConfigStore.SOLID : AiPortalCast.Inner_ConfigStore.AIR;
                  }
               }

               int init_I_22() {
                  return (int)Math.max(1L, (this.init_J - System.nanoTime()) / 1000000000L);
               }

               JsonObject init_JsonObject_2(String id) {
                  JsonObject missing = new JsonObject();
                  missing.addProperty(id, 1);
                  return missing;
               }

               JsonObject init_JsonObject_36() {
                  JsonObject out = AiJson.objectOf(
                     "phase",
                     this.init_aX_NetHelper.name().toLowerCase(Locale.ROOT),
                     "castCells",
                     this.run,
                     "frameCells",
                     this.read_Set.size(),
                     "lavaNeeded",
                     this.init_aX_StreamUtil == null ? 0 : this.init_aX_StreamUtil.lavaNeeded(),
                     "lavaPoured",
                     this.get,
                     "waterRecoveryPending",
                     this.init_aX_RenderState.init(),
                     "pourAcknowledgementPending",
                     this.init_aX_JsonUtil.init_Z(),
                     "lit",
                     this.update_Z
                  );
                  if (!this.load_Str.isEmpty()) {
                     out.addProperty("litWith", this.load_Str);
                  }

                  if (this.load_BlockPos != null) {
                     out.add(
                        "frame",
                        AiJson.objectOf(
                           "x",
                           this.load_BlockPos.getX(),
                           "y",
                           this.load_BlockPos.getY(),
                           "z",
                           this.load_BlockPos.getZ(),
                           "axis",
                           this.init_Direction.name().toLowerCase(Locale.ROOT)
                        )
                     );
                  }

                  if (this.init_aX_StreamUtil != null && this.push < this.init_aX_StreamUtil.steps().size()) {
                     AiPortalCast.Inner_BufferHelper step = this.init_aX_StreamUtil.steps().get(this.push);
                     out.add(
                        "step",
                        AiJson.objectOf(
                           "index", this.push, "action", step.action().name().toLowerCase(Locale.ROOT), "cell", step.cell().toShortString()
                        )
                     );
                  }

                  if (this.put > 0) {
                     out.add("guard", AiGuard.statusToJson(this.init_X_BufferHelper, this.put, this.init_Str));
                  }

                  if (this.init_aX_StateCache != null && this.init_aX_StateCache.init() != null) {
                     AiOperation.Result receipt = this.init_aX_StateCache.init();
                     out.add(
                        "pourApproach",
                        AiJson.objectOf(
                           "status",
                           receipt.status().name(),
                           "message",
                           receipt.message(),
                           "pending",
                           this.init_aX_StateCache.requiresReconciliationAfterEmergency(),
                           "receipt",
                           AiRunResources.parseEvidenceJson(receipt.dataJson())
                        )
                     );
                  }

                  return out;
               }

               AiOperation.Result load(String message) {
                  return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_36().toString(), List.of());
               }

               AiOperation.Result init(AiOperation.Status status, String message) {
                  AiOperation.Result result = this.init_aX_UiHelper
                     .init(this.init_Z_19(), new AiOperation.Result(status, message, this.init_JsonObject_36().toString(), List.of()));
                  this.cancel();
                  return result;
               }

               AiOperation.Result init(AiFailure code, String message, JsonObject missing) {
                  JsonObject data = this.init_JsonObject_36();
                  if (missing != null) {
                     data.add("missing", missing);
                  }

                  AiOperation.Result result = this.init_aX_UiHelper.init(this.init_Z_19(), AiOperation.Result.failed(code, message, data.toString()));
                  this.cancel();
                  return result;
               }

               boolean init_Z_19() {
                  return this.init_aX_JsonUtil.init_Z()
                     || this.init_DataPipe.needsReconciliation()
                     || this.init_aX_StateCache != null && this.init_aX_StateCache.requiresReconciliationAfterEmergency()
                     || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency()
                     || this.load_AiOperation != null && this.load_AiOperation.requiresReconciliationAfterEmergency()
                     || this.read_AiOperation != null && this.read_AiOperation.requiresReconciliationAfterEmergency();
               }

               @Override
               public boolean canInterruptNow() {
                  return !this.write_Z
                     && !this.init_aX_RenderState.init()
                     && !this.init_aX_JsonUtil.init_Z()
                     && !this.init_DataPipe.needsReconciliation()
                     && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow())
                     && (this.load_AiOperation == null || this.load_AiOperation.canInterruptNow())
                     && (this.init_aX_StateCache == null || this.init_aX_StateCache.canInterruptNow())
                     && (this.read_AiOperation == null || this.read_AiOperation.canInterruptNow());
               }

               @Override
               public boolean requiresReconciliationAfterEmergency() {
                  return this.init_aX_UiHelper
                     .init(
                        this.write_Z
                           || this.init_aX_RenderState.init()
                           || this.init_aX_JsonUtil.init_Z()
                           || this.init_DataPipe.needsReconciliation()
                           || this.init_aX_StateCache != null && this.init_aX_StateCache.requiresReconciliationAfterEmergency()
                           || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency()
                           || this.load_AiOperation != null && this.load_AiOperation.requiresReconciliationAfterEmergency()
                           || this.read_AiOperation != null && this.read_AiOperation.requiresReconciliationAfterEmergency()
                     );
               }

               @Override
               public void cancel() {
                  if (!this.sync_Z) {
                     this.sync_Z = true;
                     this.init_DataUtil.clear();
                     this.init_DataPipe.reset();
                     if (this.init_aX_StateCache != null) {
                        this.init_aX_StateCache.cancel();
                     }

                     if (this.init_AiOperation != null) {
                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                     }

                     if (this.load_AiOperation != null) {
                        this.load_AiOperation.cancel();
                        this.load_AiOperation = null;
                     }

                     if (this.read_AiOperation != null) {
                        this.read_AiOperation.cancel();
                        this.read_AiOperation = null;
                     }

                     if (this.init_Minecraft.player != null && this.init_Minecraft.player.getInventory().getSelectedSlot() != this.init_I) {
                        this.init_Minecraft.player.getInventory().setSelectedSlot(this.init_I);
                     }
                  }
               }
            };
         } else {
            throw new IllegalArgumentException("x, y and z are given together (a lava pool cell), or omitted for the nearest pool");
         }
      } else {
         throw new IllegalArgumentException("Portal casting requires a connected living player");
      }
   }

   record Inner_BufferHelper(BlockPos cell, AiPortalCast.Inner_DataUtil action, BlockPos waterCell, List<BlockPos> dams, AiPortalCast.Inner_SyncManager face) {
   }

   private enum Inner_CachePool {
      STANCE,
      MOLDS,
      LAVA_REFILL,
      LAVA_POUR,
      WATER_POUR,
      SCOOP;
   }

   enum Inner_ConfigStore {
      AIR,
      WATER,
      LAVA,
      OBSIDIAN,
      SOLID;
   }

   enum Inner_DataUtil {
      SKIP,
      WATER_OVER_LAVA,
      LAVA_THEN_WATER;
   }

   static final class Inner_JsonUtil {
      private BlockPos init_BlockPos;
      private String init_Str;
      private int init_I;
      private long init_J;
      private long load;

      void init(BlockPos at, String bucketId, int filledBuckets, long inventoryRevision, long cellRevision) {
         if (!this.init_Z() && filledBuckets > 0) {
            this.init_BlockPos = at.immutable();
            this.init_Str = bucketId;
            this.init_I = filledBuckets;
            this.init_J = inventoryRevision;
            this.load = cellRevision;
         } else {
            throw new IllegalStateException("Unacknowledged fluid pour");
         }
      }

      boolean init_Z() {
         return this.init_BlockPos != null;
      }

      BlockPos init_BlockPos() {
         return this.init_BlockPos;
      }

      String init_Str() {
         return this.init_Str;
      }

      void init(int filledBuckets, long inventoryRevision, long cellRevision) {
         if (this.init_Z() && filledBuckets < this.init_I && inventoryRevision > this.init_J && cellRevision > this.load) {
            this.init_BlockPos = null;
         }
      }

      void init(BlockPos at) {
         if (this.init_Z() && this.init_BlockPos.equals(at)) {
            this.init_BlockPos = null;
         }
      }
   }

   enum Inner_NetHelper {
      PREFLIGHT,
      LOCATE,
      SITE,
      EXCAVATE,
      WATER,
      CAST,
      LIGHT;
   }

   static final class Inner_RenderState {
      private BlockPos init_BlockPos;
      private int init_I;

      void init(BlockPos at, int filledBuckets) {
         if (this.init_BlockPos == null && filledBuckets > 0) {
            this.init_BlockPos = at.immutable();
            this.init_I = filledBuckets;
         } else {
            throw new IllegalStateException("Unreconciled water pour");
         }
      }

      boolean init() {
         return this.init_BlockPos != null;
      }

      boolean init(BlockPos at, int filledBuckets, boolean fillAcknowledged) {
         if (fillAcknowledged && this.init_BlockPos != null && this.init_BlockPos.equals(at) && filledBuckets >= this.init_I) {
            this.init_BlockPos = null;
            return true;
         } else {
            return false;
         }
      }
   }

   static final class Inner_StateCache implements AiOperation {
      private final AiOperation child;
      private final long deadline;
      private final LongSupplier clock;
      private final AiPreparationReceipt receipt = new AiPreparationReceipt();
      private AiOperation.Result finalResult;
      private boolean init_Z;

      Inner_StateCache(AiOperation child, long deadline, LongSupplier now) {
         this.child = child;
         this.deadline = deadline;
         this.clock = now;
      }

      @Override
      public AiOperation.Result tick() {
         if (this.finalResult != null) {
            return this.finalResult;
         }

         if (this.init_Z) {
            return this.init(new AiOperation.Result(AiOperation.Status.CANCELLED, "Pour face approach cancelled", "{}", List.of()));
         }

         if (this.clock.getAsLong() >= this.deadline) {
            return this.init(AiOperation.Result.timedOut(AiFailure.TIMEOUT, "Pour face approach exceeded its bounded deadline", "{}"));
         }

         AiOperation.Result result = this.receipt.tickChild(this.child);
         return result.terminal() ? this.init(this.receipt.lastChildResult()) : result;
      }

      private AiOperation.Result init(AiOperation.Result result) {
         this.finalResult = this.receipt.finalizeResult(result, this.receipt.requiresReconciliation(this.child));
         this.receipt.cancelChild(this.child);
         return this.finalResult;
      }

      AiOperation.Result init() {
         return this.finalResult == null ? this.receipt.lastChildResult() : this.finalResult;
      }

      @Override
      public boolean canInterruptNow() {
         return !this.receipt.requiresReconciliation(this.child) && (this.finalResult != null || this.child.canInterruptNow());
      }

      @Override
      public boolean requiresReconciliationAfterEmergency() {
         return this.receipt.requiresReconciliation(this.child);
      }

      @Override
      public void cancel() {
         this.receipt.cancelChild(this.child);
         this.init_Z = true;
      }
   }

   record Inner_StreamUtil(List<AiPortalCast.Inner_BufferHelper> steps, BlockPos blocked, String reason, int lavaNeeded, int moldBlocks) {
   }

   record Inner_SyncManager(BlockPos support, Direction face, BlockPos mold) {
   }

   interface Inner_TaskQueue {
      AiPortalCast.Inner_ConfigStore at(BlockPos var1);
   }

   static final class Inner_UiHelper {
      private boolean init;
      private boolean load;

      AiOperation.Result init(boolean unconfirmed, AiOperation.Result result) {
         this.load = unconfirmed || result.status() == AiOperation.Status.UNCERTAIN;
         this.init = true;
         return AiGuardLayer.wrapPendingTermination(unconfirmed, result);
      }

      boolean init(boolean activePending) {
         return this.init ? this.load : activePending;
      }
   }
}
