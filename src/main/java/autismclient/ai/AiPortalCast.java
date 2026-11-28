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
   private static final double WIDE_STANCE_RADIUS = 3.0;
   static final boolean init_Z = false;
   private static final int flush = 64;
   static final int MIN_THROWAWAY_BLOCKS = 16;
   private static final int close = 60;
   private static final int open = 40;
   private static final int poll = 140;
   private static final int push = 120;
   private static final int pop = 600;
   private static final int tick = 2;
   static final List<String> THROWAWAY_BLOCKS = List.of(
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
   static final List<String> DOORWAY_WOOD = List.of(
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

   static AiPortalCast.CellKind classify(ClientLevel level, BlockPos pos) {
      if (!AiTerrain.isLoaded(level, pos)) {
         return AiPortalCast.CellKind.SOLID;
      }

      BlockState state = level.getBlockState(pos);
      if (state.is(Blocks.OBSIDIAN)) {
         return AiPortalCast.CellKind.OBSIDIAN;
      }

      FluidState fluid = state.getFluidState();
      if (!fluid.isEmpty()) {
         if (!fluid.isSource()) {
            return AiPortalCast.CellKind.SOLID;
         } else if (fluid.is(FluidTags.LAVA)) {
            return AiPortalCast.CellKind.LAVA;
         } else {
            return fluid.is(FluidTags.WATER) ? AiPortalCast.CellKind.WATER : AiPortalCast.CellKind.SOLID;
         }
      } else {
         return !state.isAir() && !state.canBeReplaced() ? AiPortalCast.CellKind.SOLID : AiPortalCast.CellKind.AIR;
      }
   }

   static boolean init_Z(ClientLevel level, BlockPos anchor) {
      AiPortalCast.CellLookup lookup = pos -> classify(level, pos);
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
                  Set<BlockPos> reserved = reservedCells(origin, axis);
                  AiPortalCast.CastPlan plan = planCast(frameCellsSorted(origin, axis), axis, reserved, lookup);
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
                           if (classify(level, cell) == AiPortalCast.CellKind.SOLID) {
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

                        AiPortalCast.CellLookup after = pos -> dug.contains(pos) ? AiPortalCast.CellKind.AIR : classify(level, pos);
                        plan = planCast(frameCellsSorted(origin, axis), axis, reserved, after);
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

   static List<BlockPos> frameCellsSorted(BlockPos origin, Direction axis) {
      List<BlockPos> cells = new ArrayList<>(AiDimension.frameBlocks(origin, axis));
      cells.sort(Comparator.<BlockPos>comparingInt(p -> p.getY()).thenComparingInt(p -> p.getX()).thenComparingInt(p -> p.getZ()));
      return cells;
   }

   static Set<BlockPos> reservedCells(BlockPos origin, Direction axis) {
      Set<BlockPos> reserved = new HashSet<>(AiDimension.frameBlocks(origin, axis));
      reserved.addAll(AiDimension.write(origin, axis));
      return reserved;
   }

   static AiPortalCast.CastPlan planCast(List<BlockPos> cells, Direction axis, Set<BlockPos> reserved, AiPortalCast.CellLookup lookup) {
      List<AiPortalCast.CastStep> steps = new ArrayList<>(cells.size());
      int lavaNeeded = 0;
      int moldBlocks = 0;

      for (BlockPos cell : cells) {
         AiPortalCast.CellKind kind = lookup.at(cell);
         if (kind == AiPortalCast.CellKind.OBSIDIAN) {
            steps.add(new AiPortalCast.CastStep(cell.immutable(), AiPortalCast.CastAction.SKIP, null, List.of(), null));
         } else {
            if (kind != AiPortalCast.CellKind.LAVA && kind != AiPortalCast.CellKind.AIR && kind != AiPortalCast.CellKind.WATER) {
               return new AiPortalCast.CastPlan(steps, cell.immutable(), "frame cell " + cell.toShortString() + " is occupied", lavaNeeded, moldBlocks);
            }

            AiPortalCast.CastAction action = kind == AiPortalCast.CellKind.LAVA ? AiPortalCast.CastAction.WATER_OVER_LAVA : AiPortalCast.CastAction.LAVA_THEN_WATER;
            if (action == AiPortalCast.CastAction.LAVA_THEN_WATER) {
               lavaNeeded++;
            }

            BlockPos waterCell = cell.above();
            AiPortalCast.CellKind above = lookup.at(waterCell);
            if (above != AiPortalCast.CellKind.AIR && above != AiPortalCast.CellKind.WATER) {
               return new AiPortalCast.CastPlan(steps, cell.immutable(), "no room to pour above frame cell " + cell.toShortString(), lavaNeeded, moldBlocks);
            }

            List<BlockPos> dams = damCells(waterCell, reserved, lookup);
            AiPortalCast.PourFace face = pourFace(waterCell, axis, reserved, lookup, dams);
            if (face == null) {
               return new AiPortalCast.CastPlan(steps, cell.immutable(), "no face to pour water against above " + cell.toShortString(), lavaNeeded, moldBlocks);
            }

            moldBlocks += dams.size() + (face.mold() != null ? 1 : 0);
            steps.add(new AiPortalCast.CastStep(cell.immutable(), action, waterCell.immutable(), dams, face));
         }
      }

      return new AiPortalCast.CastPlan(steps, null, "", lavaNeeded, moldBlocks);
   }

   static List<BlockPos> damCells(BlockPos waterCell, Set<BlockPos> reserved, AiPortalCast.CellLookup lookup) {
      List<BlockPos> dams = new ArrayList<>(4);

      for (Direction side : Plane.HORIZONTAL) {
         BlockPos n = waterCell.relative(side);
         if (!reserved.contains(n)) {
            AiPortalCast.CellKind kind = lookup.at(n);
            if (kind == AiPortalCast.CellKind.LAVA || kind == AiPortalCast.CellKind.AIR && lookup.at(n.below()) == AiPortalCast.CellKind.LAVA) {
               dams.add(n.immutable());
            }
         }
      }

      return dams;
   }

   static AiPortalCast.PourFace pourFace(BlockPos pourCell, Direction axis, Set<BlockPos> reserved, AiPortalCast.CellLookup lookup, List<BlockPos> dams) {
      AiPortalCast.CellKind below = lookup.at(pourCell.below());
      if (below != AiPortalCast.CellKind.SOLID && below != AiPortalCast.CellKind.OBSIDIAN) {
         for (BlockPos dam : dams) {
            for (Direction side : Plane.HORIZONTAL) {
               if (pourCell.relative(side).equals(dam)) {
                  return new AiPortalCast.PourFace(dam, side.getOpposite(), null);
               }
            }
         }

         for (Direction side : sideOrder(axis)) {
            BlockPos n = pourCell.relative(side);
            if (!reserved.contains(n) && lookup.at(n) == AiPortalCast.CellKind.AIR && lookup.at(n.below()) != AiPortalCast.CellKind.LAVA) {
               return new AiPortalCast.PourFace(n.immutable(), side.getOpposite(), n.immutable());
            }
         }

         return null;
      } else {
         return new AiPortalCast.PourFace(pourCell.below().immutable(), Direction.UP, null);
      }
   }

   static List<Direction> sideOrder(Direction axis) {
      return List.of(axis.getCounterClockWise(), axis.getClockWise(), axis.getOpposite(), axis);
   }

   static autismclient.ai.engine.AiPathfinder.PathGoal faceApproachGoal(
      AiPortalCast.PourFace face, Set<BlockPos> reserved, List<BlockPos> molds, Set<BlockPos> supports, double reach, double eyeHeight
   ) {
      return faceApproachGoalWith(face, reserved, molds, supports, reach, eyeHeight, false);
   }

   static autismclient.ai.engine.AiPathfinder.PathGoal faceApproachGoalWith(
      AiPortalCast.PourFace face, Set<BlockPos> reserved, List<BlockPos> molds, Set<BlockPos> supports, double reach, double eyeHeight, boolean lava
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
      return lava ? AiGoto.excludingStandCellsCompat(goal, lavaForbiddenStances(face.support().relative(face.face()))) : goal;
   }

   static boolean isClearOfPour(BlockPos feet, BlockPos pourCell) {
      return feet.distManhattan(pourCell) > 1 && feet.above().distManhattan(pourCell) > 1;
   }

   static Set<Long> lavaForbiddenStances(BlockPos pourCell) {
      Set<Long> forbidden = new HashSet<>();

      for (BlockPos feet : BlockPos.betweenClosed(pourCell.offset(-1, -2, -1), pourCell.offset(1, 1, 1))) {
         if (!isClearOfPour(feet, pourCell)) {
            forbidden.add(feet.asLong());
         }
      }

      return Set.copyOf(forbidden);
   }

   static boolean isSettled(boolean onGround, Vec3 motion) {
      return onGround && motion.x * motion.x + motion.z * motion.z <= 1.0E-4;
   }

   static int secondsUntil(long deadline, long now) {
      return (int)Math.max(0L, Math.min(30L, (deadline - now) / 1000000000L));
   }

   static Vec3 nudgedHitPoint(Vec3 eye, BlockHitResult hit) {
      return hit.getLocation().add(hit.getLocation().subtract(eye).normalize().scale(0.01));
   }

   static List<BlockPos> spreadZone(BlockPos waterCell) {
      List<BlockPos> zone = new ArrayList<>(8);

      for (Direction side : Plane.HORIZONTAL) {
         zone.add(waterCell.relative(side));
         zone.add(waterCell.relative(side).below());
      }

      return zone;
   }

   static BlockPos nearestFreeSource(List<BlockPos> poolSources, Set<BlockPos> reserved, BlockPos player) {
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

   static String chooseLighter(int flintAndSteel, int fireCharge, boolean lavaCapable, int flammable) {
      if (flintAndSteel > 0) {
         return "flint_and_steel";
      } else if (fireCharge > 0) {
         return "fire_charge";
      } else {
         return lavaCapable && flammable > 0 ? "fire_spread" : null;
      }
   }

   static JsonObject missingMaterials(
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

      if (chooseLighter(flintAndSteel, fireCharge, lavaNeeded > 0 || lavaBuckets > 0 || buckets > 0, flammable) == null) {
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
               final Minecraft mc = Minecraft.getInstance();
               final Object startLevel = this.mc.level;
               final Object startConnection = this.mc.getConnection();
               final UUID playerId = this.mc.player.getUUID();
               final long deadline = System.nanoTime() + timeoutSeconds * 1000000000L;
               final int originalSlot = this.mc.player.getInventory().getSelectedSlot();
               final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
               final autismclient.ai.engine.AiPlacer placer = new autismclient.ai.engine.AiPlacer();
               final AiGuard.GuardPolicy guardPolicy = AiGuard.GuardPolicy.standard().withoutCombat();
               final AiPortalCast.CellLookup cellLookup = this::classify;
               AiPortalCast.CastPhase phase = AiPortalCast.CastPhase.PREFLIGHT;
               AiPortalCast.CastStage stage = AiPortalCast.CastStage.STANCE;
               final ArrayDeque<BlockPos> siteDigs = new ArrayDeque<>();
               final Set<BlockPos> moldRetried = new HashSet<>();
               boolean approachingMold;
               int siteClearings;
               int siteDigFailures;
               BlockPos pool;
               BlockPos frameOrigin;
               Direction frameAxis = Direction.EAST;
               List<BlockPos> poolSources = new ArrayList<>();
               Set<BlockPos> reservedCells = Set.of();
               Set<BlockPos> frameCells = Set.of();
               AiPortalCast.CastPlan plan;
               int waterBuckets;
               int lavaBuckets;
               int emptyBuckets;
               int throwawayCount;
               int flintAndSteelCount;
               int fireChargeCount;
               int doorwayWoodCount;
               int stepIndex;
               int pop;
               int tick;
               int castCellCount;
               int lavaPouredCount;
               int set;
               int put;
               int lightWaitTicks;
               int clear;
               long stepCellRevision;
               AiPortalCast.PourFace lavaFace;
               AiPortalCast.PourFace fireSpreadFace;
               List<BlockPos> molds = List.of();
               Set<BlockPos> preexistingObsidian = new HashSet<>();
               AiOperation moveOp;
               AiOperation bucketOp;
               AiOperation guardOp;
               AiPortalCast.FaceApproachOperation faceApproach;
               AiPortalCast.PourFace approachFace;
               long approachDeadline;
               boolean approaching;
               final Set<AiPortalCast.PourFace> approachedFaces = new HashSet<>();
               String guardReason = "";
               String lighter = "";
               AiGuard.StatusSnapshot vitals = AiGuard.sampleStatus();
               boolean waterHeld;
               boolean pourClicked;
               boolean lit;
               boolean cancelled;
               final AiPortalCast.WaterRecovery waterRecovery = new AiPortalCast.WaterRecovery();
               final AiPortalCast.PourAck pourAck = new AiPortalCast.PourAck();
               final AiPortalCast.TerminalGuard terminalGuard = new AiPortalCast.TerminalGuard();
               final Set<BlockPos> supports = new HashSet<>();

               @Override
               public AiOperation.Result tick() {
                  if (!this.cancelled && this.mc.player != null && this.mc.level != null) {
                     if (this.mc.level == this.startLevel
                        && this.mc.getConnection() == this.startConnection
                        && this.playerId.equals(this.mc.player.getUUID())
                        && this.mc.player.isAlive()) {
                        if (this.pourAck.isPending()) {
                           this.pourAck
                              .acknowledge(AiGameState.countInventoryItem(this.pourAck.pendingBucket()), AiServerEvidence.inventoryRevision(), AiServerEvidence.blockRevision(this.pourAck.pendingCell()));
                        }

                        if (this.mc.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.WATER_EVAPORATES)) {
                           return this.finish(AiOperation.Status.FAILED, "Water casting prohibited by the current dimension attributes");
                        }

                        if (this.mc.player.getHealth() <= 4.0F && !AiGuard.isLowHealthOverrideActive()) {
                           this.cancel();
                           return AiOperation.Result.failed(
                              AiFailure.LOW_HEALTH,
                              AiGuard.formatLowHealthAbort("portal casting", this.mc.player.getHealth(), this.mc.player.getMaxHealth()),
                              this.progressJson().toString()
                           );
                        }

                        if (System.nanoTime() > this.deadline) {
                           return this.finish(
                              AiOperation.Status.TIMED_OUT, "Portal cast deadline reached in " + this.phase.name().toLowerCase(Locale.ROOT)
                           );
                        }

                        AiOperation.Result guarded = this.tickSurvivalGuard();
                        if (guarded != null) {
                           return guarded;
                        }

                        if (this.approaching) {
                           return this.tickFaceApproach();
                        }

                        AiOperation.Result var10000;
                        switch (this.phase) {
                           case PREFLIGHT:
                              var10000 = this.preflight();
                              break;
                           case LOCATE:
                              var10000 = this.locatePool();
                              break;
                           case SITE:
                              var10000 = this.tickSitePhase();
                              break;
                           case EXCAVATE:
                              var10000 = this.tickExcavatePhase();
                              break;
                           case WATER:
                              var10000 = this.tickWaterPhase();
                              break;
                           case CAST:
                              var10000 = this.tickCastPhase();
                              break;
                           case LIGHT:
                              var10000 = this.tickLightPhase();
                              break;
                           default:
                              throw new MatchException(null, null);
                        }

                        return var10000;
                     } else {
                        return this.finish(AiOperation.Status.CANCELLED, "Portal casting player, world or connection changed");
                     }
                  } else {
                     return this.finish(AiOperation.Status.CANCELLED, "Portal cast cancelled");
                  }
               }

               AiOperation.Result preflight() {
                  if (this.mc.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.WATER_EVAPORATES)) {
                     return this.finish(AiOperation.Status.FAILED, "Water evaporates in this dimension; a cast portal is an overworld technique");
                  }

                  this.countMaterials();
                  this.waterHeld = this.waterBuckets > 0;
                  JsonObject missing = AiPortalCast.missingMaterials(this.waterBuckets, this.lavaBuckets, this.emptyBuckets, this.throwawayCount, this.flintAndSteelCount, this.fireChargeCount, this.doorwayWoodCount, 1, 16);
                  if (!missing.keySet().isEmpty()) {
                     return this.failWith(AiFailure.MISSING_MATERIALS, "Portal cast is missing materials: " + missing, missing);
                  }

                  this.phase = AiPortalCast.CastPhase.LOCATE;
                  return this.running("Materials ready; locating the lava pool");
               }

               AiOperation.Result locatePool() {
                  if (this.pool == null) {
                     BlockPos around = explicit != null ? explicit : this.mc.player.blockPosition();
                     int radius = explicit != null ? 6 : 24;
                     this.pool = this.nearestSourceWith(around, radius, 8, true, true);
                     if (this.pool == null) {
                        this.pool = this.nearestSource(around, radius, 8, true);
                     }

                     if (this.pool == null) {
                        return this.failWith(
                           AiFailure.TARGET_NOT_FOUND,
                           explicit != null
                              ? "No lava source near " + explicit.toShortString()
                              : "No lava source within 24 blocks; pass x,y,z of a pool (game:oracle query 'lava' finds one)",
                           null
                        );
                     }
                  }

                  if (this.mc.player.blockPosition().distSqr(this.pool) > 16.0) {
                     AiOperation.Result moved = this.tickMove(() -> this.init(this.pool, 3.0, 60), "Travelling to the lava pool");
                     if (moved != null) {
                        return moved;
                     }
                  }

                  this.phase = AiPortalCast.CastPhase.SITE;
                  return this.running("At the pool; planning the cast");
               }

               AiOperation.Result tickSitePhase() {
                  this.poolSources = this.lavaSourcesAround(this.pool);
                  Set<Long> tried = new HashSet<>();
                  AiPortalCast.CastPlan bestPlan = null;
                  BlockPos bestOrigin = null;
                  Direction bestAxis = Direction.EAST;
                  double bestScore = 1.7976931348623157E308;
                  int considered = 0;

                  for (BlockPos source : this.poolSources) {
                     if (++considered > 24) {
                        break;
                     }

                     for (Direction axis : List.of(Direction.EAST, Direction.SOUTH)) {
                        for (int offset = -2; offset <= 1; offset++) {
                           BlockPos origin = source.above().relative(axis, offset);
                           if (tried.add(origin.asLong())) {
                              Set<BlockPos> siteReserved = AiPortalCast.reservedCells(origin, axis);
                              AiPortalCast.CastPlan candidate = AiPortalCast.planCast(AiPortalCast.frameCellsSorted(origin, axis), axis, siteReserved, this.cellLookup);
                              if (candidate.blocked() == null) {
                                 long usable = this.poolSources.stream().filter(p -> !siteReserved.contains(p)).count();
                                 if (candidate.lavaNeeded() <= usable) {
                                    double score = candidate.lavaNeeded() * 25.0 + origin.distSqr(this.mc.player.blockPosition());
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

                  if (bestPlan == null && this.siteClearings < 2) {
                     List<BlockPos> digs = this.siteClearPlan(this.poolSources);
                     if (digs != null) {
                        this.siteClearings++;
                        this.siteDigs.clear();
                        this.siteDigs.addAll(digs);
                        AutismClientAddon.LOG
                           .info("[AI] portal cast: clearing {} blocks over the pool at {} for the frame", digs.size(), this.pool.toShortString());
                        this.phase = AiPortalCast.CastPhase.EXCAVATE;
                        return this.running("Clearing " + digs.size() + " blocks for the frame");
                     }
                  }

                  if (bestPlan == null) {
                     return this.failWith(
                        AiFailure.NO_REACHABLE_TARGET,
                        "No castable 4x5 frame site at this lava pool (frame cells blocked, or too few lava sources); try another pool or game:dimension build:true",
                        null
                     );
                  }

                  this.frameOrigin = bestOrigin;
                  this.frameAxis = bestAxis;
                  this.plan = bestPlan;
                  this.reservedCells = AiPortalCast.reservedCells(this.frameOrigin, this.frameAxis);
                  this.frameCells = new HashSet<>(AiDimension.frameBlocks(this.frameOrigin, this.frameAxis));
                  JsonObject missing = AiPortalCast.missingMaterials(
                     this.waterBuckets,
                     this.lavaBuckets,
                     this.emptyBuckets,
                     this.throwawayCount,
                     this.flintAndSteelCount,
                     this.fireChargeCount,
                     this.doorwayWoodCount,
                     this.plan.lavaNeeded(),
                     Math.max(16, this.plan.moldBlocks() + 2)
                  );
                  if (!missing.keySet().isEmpty()) {
                     return this.failWith(AiFailure.MISSING_MATERIALS, "Portal cast at " + this.frameOrigin.toShortString() + " is missing materials: " + missing, missing);
                  }

                  this.phase = this.waterHeld ? AiPortalCast.CastPhase.CAST : AiPortalCast.CastPhase.WATER;
                  return this.running("Cast site at " + this.frameOrigin.toShortString() + " along " + this.frameAxis.name().toLowerCase(Locale.ROOT));
               }

               AiOperation.Result tickWaterPhase() {
                  if (AiGameState.countInventoryItem("minecraft:water_bucket") > 0) {
                     this.phase = AiPortalCast.CastPhase.CAST;
                     return this.running("Water ready");
                  }

                  if (this.bucketOp == null) {
                     BlockPos open = this.nearestSourceWith(this.mc.player.blockPosition(), 32, 8, false, true);
                     BlockPos source = open != null ? open : this.nearestSource(this.mc.player.blockPosition(), 32, 8, false);
                     if (source == null) {
                        return this.failWith(AiFailure.MISSING_MATERIALS, "No water source within 32 blocks to fill the bucket", this.missingOne("minecraft:water_bucket"));
                     }

                     if (this.mc.player.blockPosition().distSqr(source) > 9.0) {
                        AiOperation.Result moved = this.tickMove(() -> this.init(source, 2.0, 45), "Travelling to water");
                        if (moved != null) {
                           return moved;
                        }
                     }

                     this.bucketOp = this.fillBucketOp("water", source, 20);
                  }

                  AiOperation.Result filled = this.awaitBucket("Filling the water bucket");
                  if (filled != null) {
                     return filled;
                  }

                  this.phase = AiPortalCast.CastPhase.CAST;
                  return this.running("Water bucket filled");
               }

               AiOperation.Result tickCastPhase() {
                  if (this.stepIndex >= this.plan.steps().size()) {
                     for (BlockPos cell : this.frameCells) {
                        if (!this.mc.level.getBlockState(cell).is(Blocks.OBSIDIAN)) {
                           return this.failWith(AiFailure.IMPOSSIBLE_STATE, "Frame cell " + cell.toShortString() + " is not obsidian after the cast", null);
                        }
                     }

                     this.phase = AiPortalCast.CastPhase.LIGHT;
                     return this.running("Frame complete; lighting the portal");
                  } else {
                     AiPortalCast.CastStep step = this.plan.steps().get(this.stepIndex);
                     if (this.mc.level.getBlockState(step.cell()).is(Blocks.OBSIDIAN) && !this.waterRecovery.isRecoveryPending()) {
                        this.nextCell();
                        return this.running("Frame cell " + step.cell().toShortString() + " is obsidian");
                     }

                     if (this.waterRecovery.isRecoveryPending()
                        && this.stage != AiPortalCast.CastStage.SCOOP
                        && this.mc.level.getBlockState(step.cell()).is(Blocks.OBSIDIAN)
                        && AiServerEvidence.blockRevision(step.cell()) > this.stepCellRevision) {
                        this.stage = AiPortalCast.CastStage.SCOOP;
                        this.pop = 0;
                     }

                     AiOperation.Result var10000;
                     switch (this.stage) {
                        case STANCE:
                           var10000 = this.tickStanceStage(step);
                           break;
                        case MOLDS:
                           var10000 = this.tickMoldsStage(step);
                           break;
                        case LAVA_REFILL:
                           var10000 = this.tickLavaRefillStage(step);
                           break;
                        case LAVA_POUR:
                           var10000 = this.tickLavaPourStage(step);
                           break;
                        case WATER_POUR:
                           var10000 = this.tickWaterPourStage(step);
                           break;
                        case SCOOP:
                           var10000 = this.tickScoopStage(step);
                           break;
                        default:
                           throw new MatchException(null, null);
                     }

                     return var10000;
                  }
               }

               AiOperation.Result tickStanceStage(AiPortalCast.CastStep step) {
                  this.stepCellRevision = AiServerEvidence.blockRevision(step.cell());
                  if (step.action() == AiPortalCast.CastAction.WATER_OVER_LAVA && this.classify(step.cell()) != AiPortalCast.CellKind.LAVA) {
                     return ++this.pop > 140
                        ? this.failWith(AiFailure.IMPOSSIBLE_STATE, "Frame cell " + step.cell().toShortString() + " no longer holds a lava source", null)
                        : this.running("Waiting for the lava at " + step.cell().toShortString() + " to settle");
                  }

                  this.pop = 0;
                  double reach = Math.max(4.5, this.mc.player.blockInteractionRange()) - 0.6;
                  if (this.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(step.cell())) > reach * reach) {
                     AiOperation.Result moved = this.tickMove(() -> this.init(step.cell(), 2.2, 45), "Moving to frame cell " + step.cell().toShortString());
                     if (moved != null) {
                        return moved;
                     }
                  }

                  List<BlockPos> queue = new ArrayList<>(step.dams());
                  if (step.face() != null && step.face().mold() != null && !queue.contains(step.face().mold())) {
                     queue.add(step.face().mold());
                  }

                  this.lavaFace = null;
                  if (step.action() == AiPortalCast.CastAction.LAVA_THEN_WATER && this.classify(step.cell()) != AiPortalCast.CellKind.LAVA) {
                     this.lavaFace = this.pourFaceFor(step.cell());
                     if (this.lavaFace == null) {
                        return this.failWith(AiFailure.PLACEMENT_IMPOSSIBLE, "No face to pour lava against at " + step.cell().toShortString(), null);
                     }

                     if (this.lavaFace.mold() != null && !queue.contains(this.lavaFace.mold())) {
                        queue.add(this.lavaFace.mold());
                     }
                  }

                  this.molds = queue;
                  this.set = 0;
                  this.preexistingObsidian = new HashSet<>();

                  for (BlockPos zone : AiPortalCast.spreadZone(step.waterCell())) {
                     if (this.mc.level.getBlockState(zone).is(Blocks.OBSIDIAN)) {
                        this.preexistingObsidian.add(zone);
                     }
                  }

                  this.stage = AiPortalCast.CastStage.MOLDS;
                  return this.running("Casting frame cell " + step.cell().toShortString() + " (" + step.action().name().toLowerCase(Locale.ROOT) + ")");
               }

               boolean hasSolidNeighbour(BlockPos cell) {
                  for (Direction d : Direction.values()) {
                     BlockPos side = cell.relative(d);
                     if (!this.mc.level.getBlockState(side).getCollisionShape(this.mc.level, side).isEmpty()) {
                        return true;
                     }
                  }

                  return false;
               }

               BlockPos init(BlockPos mold, AiPortalCast.CastStep step) {
                  BlockPos best = null;
                  double bestDistance = 1.7976931348623157E308;
                  BlockPos feet = this.mc.player.blockPosition();

                  for (Direction d : Direction.values()) {
                     BlockPos side = mold.relative(d);
                     if (!this.reservedCells.contains(side)
                        && !side.equals(step.cell())
                        && !side.equals(step.waterCell())
                        && !this.molds.contains(side)
                        && !side.equals(feet)
                        && !side.equals(feet.above())
                        && this.classify(side) == AiPortalCast.CellKind.AIR
                        && this.hasSolidNeighbour(side)) {
                        double distance = this.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(side));
                        if (distance < bestDistance) {
                           bestDistance = distance;
                           best = side;
                        }
                     }
                  }

                  return best;
               }

               AiOperation.Result tickMoldsStage(AiPortalCast.CastStep step) {
                  if (this.approachingMold && this.moveOp != null) {
                     AiOperation.Result r = this.moveOp.tick();
                     if (!r.terminal()) {
                        return this.running("Moving to reach a mold block");
                     }

                     this.moveOp.cancel();
                     this.moveOp = null;
                     this.approachingMold = false;
                  }

                  while (this.set < this.molds.size()) {
                     BlockPos mold = this.molds.get(this.set);
                     AiPortalCast.CellKind there = this.classify(mold);
                     if (there != AiPortalCast.CellKind.SOLID && there != AiPortalCast.CellKind.OBSIDIAN) {
                        Item item = this.firstPlaceable(AiPortalCast.THROWAWAY_BLOCKS);
                        if (item == null) {
                           return this.failWith(AiFailure.MISSING_MATERIALS, "Out of throwaway blocks for the mold at " + mold.toShortString(), this.missingOne("minecraft:dirt"));
                        }

                        if (!mold.equals(this.placer.targetPos()) && !this.hasSolidNeighbour(mold)) {
                           BlockPos support = this.init(mold, step);
                           if (support != null && this.supports.add(support)) {
                              List<BlockPos> withSupport = new ArrayList<>(this.molds);
                              withSupport.add(this.set, support);
                              this.molds = withSupport;
                              AutismClientAddon.LOG.info("[AI] portal cast: a support at {} for the mold at {}", support.toShortString(), mold.toShortString());
                              continue;
                           }
                        }

                        if (!mold.equals(this.placer.targetPos())) {
                           this.placer.start(mold, item);
                        }

                        autismclient.ai.engine.AiPlacer.PlaceStatus placed = this.placer.tick();
                        if (placed != autismclient.ai.engine.AiPlacer.PlaceStatus.PLACED) {
                           if (placed == autismclient.ai.engine.AiPlacer.PlaceStatus.FAILED) {
                              if (!this.moldRetried.contains(mold)) {
                                 this.moldRetried.add(mold);
                                 this.placer.reset();
                                 this.moveOp = AiGoto.approachToPlaceCompat(mold, item, 30, null, false);
                                 this.approachingMold = true;
                                 return this.running("Moving to reach the mold block at " + mold.toShortString());
                              }

                              return this.failWith(
                                 AiFailure.PLACEMENT_IMPOSSIBLE, "Could not place the mold block at " + mold.toShortString() + ": " + this.placer.failureReason(), null
                              );
                           }

                           return this.running("Placing the mold at " + mold.toShortString());
                        }

                        this.set++;
                     } else {
                        this.set++;
                     }
                  }

                  this.stage = step.action() == AiPortalCast.CastAction.LAVA_THEN_WATER && this.classify(step.cell()) != AiPortalCast.CellKind.LAVA
                     ? AiPortalCast.CastStage.LAVA_REFILL
                     : AiPortalCast.CastStage.WATER_POUR;
                  this.pop = 0;
                  this.pourClicked = false;
                  return this.running("Molds placed");
               }

               AiOperation.Result tickLavaRefillStage(AiPortalCast.CastStep step) {
                  if (AiGameState.countInventoryItem("minecraft:lava_bucket") > 0) {
                     this.stage = AiPortalCast.CastStage.LAVA_POUR;
                     this.pop = 0;
                     this.pourClicked = false;
                     return this.running("Lava ready");
                  }

                  if (this.bucketOp == null) {
                     List<BlockPos> sources = new ArrayList<>();

                     for (BlockPos source : this.poolSources) {
                        if (this.classify(source) == AiPortalCast.CellKind.LAVA) {
                           sources.add(source);
                        }
                     }

                     BlockPos refill = AiPortalCast.nearestFreeSource(sources, this.reservedCells, this.mc.player.blockPosition());
                     if (refill == null) {
                        return this.failWith(AiFailure.MISSING_MATERIALS, "The lava pool ran out of usable source cells", this.missingOne("minecraft:lava_bucket"));
                     }

                     if (this.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(refill)) > 12.0) {
                        AiOperation.Result moved = this.tickMove(() -> this.init(refill, 2.0, 45), "Moving to the pool for a lava refill");
                        if (moved != null) {
                           return moved;
                        }
                     }

                     this.bucketOp = this.fillBucketOp("lava", refill, 20);
                  }

                  AiOperation.Result filled = this.awaitBucket("Scooping lava from the pool");
                  if (filled != null) {
                     return filled;
                  }

                  this.stage = AiPortalCast.CastStage.LAVA_POUR;
                  this.pop = 0;
                  this.pourClicked = false;
                  return this.running("Lava bucket filled");
               }

               AiOperation.Result tickLavaPourStage(AiPortalCast.CastStep step) {
                  if (this.classify(step.cell()) == AiPortalCast.CellKind.LAVA) {
                     this.stage = AiPortalCast.CastStage.WATER_POUR;
                     this.pop = 0;
                     this.pourClicked = false;
                     this.lavaPouredCount++;
                     return this.running("Lava in place at " + step.cell().toShortString());
                  }

                  if (AiGameState.countInventoryItem("minecraft:lava_bucket") <= 0) {
                     if (++this.pop > 40) {
                        if (++this.tick >= 2) {
                           return this.failWith(AiFailure.PLACEMENT_IMPOSSIBLE, "Lava did not stay in frame cell " + step.cell().toShortString(), null);
                        }

                        this.pop = 0;
                        this.stage = AiPortalCast.CastStage.LAVA_REFILL;
                     }

                     return this.running("Confirming the lava pour at " + step.cell().toShortString());
                  } else {
                     if (!this.pourClicked && !this.pourAck.isPending()) {
                        AiOperation.Result approached = this.pourOnFace(this.lavaFace, true);
                        if (approached != null) {
                           return approached;
                        }
                     }

                     if (++this.pop > 60 * (this.tick + 1)) {
                        return this.failWith(AiFailure.PLACEMENT_IMPOSSIBLE, "Could not aim at the lava pour face for " + step.cell().toShortString(), null);
                     } else if (AiItems.equipHotbar(AiItems.resolveItem("minecraft:lava_bucket")) < 0) {
                        this.stage = AiPortalCast.CastStage.LAVA_REFILL;
                        return this.running("Refilling the lava bucket");
                     } else if (this.mc.player.containerMenu != this.mc.player.inventoryMenu) {
                        this.mc.player.closeContainer();
                        return this.running("Closing a screen before pouring");
                     } else {
                        AiOperation.Result poured = this.init(this.lavaFace);
                        return poured != null ? poured : this.running("Pouring lava into " + step.cell().toShortString());
                     }
                  }
               }

               AiOperation.Result tickWaterPourStage(AiPortalCast.CastStep step) {
                  if (this.mc.level.getBlockState(step.cell()).is(Blocks.OBSIDIAN) && AiServerEvidence.blockRevision(step.cell()) > this.stepCellRevision) {
                     this.stage = AiPortalCast.CastStage.SCOOP;
                     this.pop = 0;
                     return this.running("Obsidian confirmed at " + step.cell().toShortString());
                  }

                  AiOperation.Result stray = this.flush(step);
                  if (stray != null) {
                     return stray;
                  }

                  if (!this.waterRecovery.isRecoveryPending() && AiGameState.countInventoryItem("minecraft:water_bucket") > 0) {
                     if (!this.pourClicked && !this.pourAck.isPending()) {
                        AiOperation.Result approached = this.pourOnFace(step.face(), false);
                        if (approached != null) {
                           return approached;
                        }
                     }

                     if (++this.pop > 60) {
                        if (++this.tick >= 2) {
                           return this.failWith(AiFailure.PLACEMENT_IMPOSSIBLE, "Could not aim at the pour face above " + step.cell().toShortString(), null);
                        }

                        this.pop = 0;
                        this.pourClicked = false;
                     }

                     if (AiItems.equipHotbar(AiItems.resolveItem("minecraft:water_bucket")) < 0) {
                        return this.failWith(AiFailure.MISSING_MATERIALS, "Lost the water bucket mid-cast", this.missingOne("minecraft:water_bucket"));
                     }

                     if (this.mc.player.containerMenu != this.mc.player.inventoryMenu) {
                        this.mc.player.closeContainer();
                        return this.running("Closing a screen before pouring");
                     }

                     int filledBefore = AiGameState.countInventoryItem("minecraft:water_bucket");
                     boolean alreadyClicked = this.pourClicked;
                     AiOperation.Result poured = this.init(step.face());
                     if (!alreadyClicked && this.pourClicked) {
                        this.waterRecovery.beginWaterRecovery(step.waterCell(), filledBefore);
                     }

                     return poured != null ? poured : this.running("Pouring water above " + step.cell().toShortString());
                  } else {
                     return ++this.pop > 140
                        ? this.failWith(
                           AiFailure.PLACEMENT_IMPOSSIBLE,
                           "Water did not convert frame cell "
                              + step.cell().toShortString()
                              + " (now "
                              + BuiltInRegistries.BLOCK.getKey(this.mc.level.getBlockState(step.cell()).getBlock())
                              + ")",
                           null
                        )
                        : this.running("Waiting for obsidian at " + step.cell().toShortString());
                  }
               }

               AiOperation.Result tickScoopStage(AiPortalCast.CastStep step) {
                  if (!this.waterRecovery.isRecoveryPending()) {
                     this.nextCell();
                     return this.running("Water recovered; next cell");
                  }

                  if (this.bucketOp == null) {
                     this.bucketOp = this.fillBucketOp("water", step.waterCell(), 20);
                  }

                  AiOperation.Result scooped = this.tickBucketOp();
                  if (scooped == null) {
                     return this.running("Re-scooping the pour water");
                  } else if (this.waterRecovery
                     .isRecoveryConfirmed(step.waterCell(), AiGameState.countInventoryItem("minecraft:water_bucket"), scooped.status() == AiOperation.Status.SUCCEEDED)) {
                     this.pourAck.clearPour(step.waterCell());
                     this.nextCell();
                     return this.running("Water recovered; next cell");
                  } else {
                     return scooped.status() == AiOperation.Status.UNCERTAIN
                        ? this.finish(AiOperation.Status.UNCERTAIN, "The poured water refill is unconfirmed: " + scooped.message())
                        : this.failWith(
                           AiFailure.MISSING_MATERIALS,
                           "The pour's water was lost at "
                              + step.waterCell().toShortString()
                              + "; refill a water bucket and re-run (finished cells are skipped)",
                           this.missingOne("minecraft:water_bucket")
                        );
                  }
               }

               AiOperation.Result tickLightPhase() {
                  BlockPos portalCell = this.litPortalCell();
                  if (portalCell != null) {
                     this.lit = true;
                     AiRunTrace.recordGlobalSplit("portal_lit", this.frameOrigin.toShortString() + " cast:" + this.castCellCount + " litWith:" + this.lighter);
                     return this.finish(AiOperation.Status.SUCCEEDED, "Portal cast and lit at " + this.frameOrigin.toShortString());
                  }

                  for (BlockPos interior : AiDimension.write(this.frameOrigin, this.frameAxis)) {
                     BlockState state = this.mc.level.getBlockState(interior);
                     if (!state.isAir() && !state.is(Blocks.FIRE)) {
                        if (state.getFluidState().is(FluidTags.WATER) && AiGameState.countInventoryItem("minecraft:bucket") > 0) {
                           if (this.bucketOp == null) {
                              this.bucketOp = this.fillBucketOp("water", interior, 15);
                           }

                           AiOperation.Result cleared = this.awaitBucket("Clearing water from the doorway");
                           if (cleared != null) {
                              return cleared;
                           }

                           return this.running("Doorway cleared");
                        }

                        return this.failWith(
                           AiFailure.IMPOSSIBLE_STATE,
                           "The portal interior at " + interior.toShortString() + " is blocked by " + BuiltInRegistries.BLOCK.getKey(state.getBlock()),
                           null
                        );
                     }
                  }

                  if (this.lighter.isEmpty()) {
                     this.countMaterials();
                     String picked = AiPortalCast.chooseLighter(this.flintAndSteelCount, this.fireChargeCount, this.lavaBuckets > 0 || this.emptyBuckets > 0, this.doorwayWoodCount);
                     if (picked == null) {
                        return this.failWith(AiFailure.MISSING_MATERIALS, "Nothing to light the portal with", this.missingOne("minecraft:flint_and_steel"));
                     }

                     this.lighter = picked;
                  }

                  if (++this.lightWaitTicks > ("fire_spread".equals(this.lighter) ? 600 : 120)) {
                     return this.finish(
                        AiOperation.Status.FAILED,
                        "fire_spread".equals(this.lighter)
                           ? "The doorway wood did not spread fire into the frame in time"
                           : "The frame is complete but the portal did not light"
                     );
                  } else {
                     return "fire_spread".equals(this.lighter) ? this.lightWithFireSpread() : this.lightWithItem();
                  }
               }

               AiOperation.Result lightWithItem() {
                  if (AiItems.equipHotbar(AiItems.resolveItem("minecraft:" + this.lighter)) < 0) {
                     this.lighter = "";
                     return this.running("Re-selecting the lighter");
                  }

                  BlockPos floor = this.frameOrigin.below();
                  this.aim.aimAt(autismclient.ai.engine.AiAim.aimPointForBlockFace(floor, Direction.UP));
                  this.aim.tick();
                  if (!autismclient.ai.engine.AiAim.isLookingAtBlock(floor)) {
                     return this.running("Turning to light the portal");
                  }

                  BlockHitResult hit = autismclient.ai.engine.AiAim.raycastLookBlock();
                  if (hit == null) {
                     return this.running("Turning to light the portal");
                  }

                  this.mc.gameMode.useItemOn(this.mc.player, InteractionHand.MAIN_HAND, hit);
                  Compat263.swing(this.mc.player, InteractionHand.MAIN_HAND);
                  return this.running("Lighting the portal");
               }

               AiOperation.Result lightWithFireSpread() {
                  BlockPos side = this.frameOrigin.relative(this.frameAxis.getCounterClockWise());
                  if (this.clear == 0) {
                     Item wood = this.firstPlaceable(AiPortalCast.DOORWAY_WOOD);
                     if (wood == null) {
                        this.lighter = "";
                        return this.running("Lost the doorway wood");
                     }

                     if (!this.frameOrigin.equals(this.placer.targetPos())) {
                        this.placer.start(this.frameOrigin, wood);
                     }

                     autismclient.ai.engine.AiPlacer.PlaceStatus placed = this.placer.tick();
                     if (placed == autismclient.ai.engine.AiPlacer.PlaceStatus.PLACED) {
                        this.clear = 1;
                        return this.running("Doorway wood placed");
                     } else {
                        return placed == autismclient.ai.engine.AiPlacer.PlaceStatus.FAILED
                           ? this.failWith(AiFailure.PLACEMENT_IMPOSSIBLE, "Could not place the doorway wood: " + this.placer.failureReason(), null)
                           : this.running("Placing the doorway wood");
                     }
                  } else if (this.clear == 1) {
                     if (this.classify(side) == AiPortalCast.CellKind.LAVA) {
                        this.clear = 3;
                        return this.running("Lava already beside the doorway");
                     }

                     this.fireSpreadFace = this.pourFaceFor(side);
                     if (this.fireSpreadFace == null) {
                        return this.failWith(AiFailure.PLACEMENT_IMPOSSIBLE, "No face to pour lava beside the doorway", null);
                     }

                     BlockPos mold = this.fireSpreadFace.mold();
                     if (mold != null
                        && this.classify(mold) != AiPortalCast.CellKind.SOLID
                        && this.classify(mold) != AiPortalCast.CellKind.OBSIDIAN) {
                        Item moldItem = this.firstPlaceable(AiPortalCast.THROWAWAY_BLOCKS);
                        if (moldItem == null) {
                           return this.failWith(AiFailure.MISSING_MATERIALS, "No throwaway block for the fire-spread mold", this.missingOne("minecraft:dirt"));
                        }

                        if (!mold.equals(this.placer.targetPos())) {
                           this.placer.start(mold, moldItem);
                        }

                        autismclient.ai.engine.AiPlacer.PlaceStatus placed = this.placer.tick();
                        if (placed == autismclient.ai.engine.AiPlacer.PlaceStatus.PLACED) {
                           this.clear = 2;
                           this.pop = 0;
                           this.pourClicked = false;
                           return this.running("Fire-spread mold placed");
                        } else {
                           return placed == autismclient.ai.engine.AiPlacer.PlaceStatus.FAILED
                              ? this.failWith(AiFailure.PLACEMENT_IMPOSSIBLE, "Could not place the fire-spread mold: " + this.placer.failureReason(), null)
                              : this.running("Placing the fire-spread mold");
                        }
                     } else {
                        this.clear = 2;
                        this.pop = 0;
                        this.pourClicked = false;
                        return this.running("Fire-spread pour face ready");
                     }
                  } else {
                     if (this.clear != 2) {
                        return this.running("Waiting for the doorway wood to catch fire");
                     }

                     if (this.classify(side) == AiPortalCast.CellKind.LAVA) {
                        this.pourClicked = false;
                        this.clear = 3;
                        return this.running("Lava beside the doorway; waiting for the wood to catch");
                     }

                     if (!this.pourClicked && !this.pourAck.isPending() && AiGameState.countInventoryItem("minecraft:lava_bucket") > 0) {
                        AiOperation.Result approached = this.pourOnFace(this.fireSpreadFace, true);
                        if (approached != null) {
                           return approached;
                        }
                     }

                     if (++this.pop > 120) {
                        return this.failWith(AiFailure.PLACEMENT_IMPOSSIBLE, "Could not pour lava beside the doorway", null);
                     }

                     if (AiGameState.countInventoryItem("minecraft:lava_bucket") > 0) {
                        if (AiItems.equipHotbar(AiItems.resolveItem("minecraft:lava_bucket")) < 0) {
                           return this.failWith(AiFailure.MISSING_MATERIALS, "Lost the lava bucket for the fire spread", this.missingOne("minecraft:lava_bucket"));
                        } else if (this.mc.player.containerMenu != this.mc.player.inventoryMenu) {
                           this.mc.player.closeContainer();
                           return this.running("Closing a screen before pouring");
                        } else {
                           AiOperation.Result poured = this.init(this.fireSpreadFace);
                           return poured != null ? poured : this.running("Pouring lava beside the doorway");
                        }
                     } else {
                        if (this.bucketOp == null) {
                           List<BlockPos> sources = new ArrayList<>();

                           for (BlockPos pool : this.poolSources) {
                              if (this.classify(pool) == AiPortalCast.CellKind.LAVA) {
                                 sources.add(pool);
                              }
                           }

                           BlockPos refill = AiPortalCast.nearestFreeSource(sources, this.reservedCells, this.mc.player.blockPosition());
                           if (refill == null) {
                              return this.failWith(AiFailure.MISSING_MATERIALS, "No lava left for the fire spread", this.missingOne("minecraft:lava_bucket"));
                           }

                           this.bucketOp = this.fillBucketOp("lava", refill, 20);
                        }

                        AiOperation.Result filled = this.awaitBucket("Scooping lava for the fire spread");
                        if (filled != null) {
                           return filled;
                        }

                        this.pop = 0;
                        this.pourClicked = false;
                        return this.running("Lava ready for the fire spread");
                     }
                  }
               }

               AiOperation.Result pourOnFace(AiPortalCast.PourFace face, boolean lava) {
                  if (face == null) {
                     return this.failWith(AiFailure.PLACEMENT_IMPOSSIBLE, "The planned pour face is unavailable", null);
                  }

                  if (autismclient.ai.engine.AiMotionSafety.isPlayerSupported()
                     && (!lava || AiPortalCast.isClearOfPour(this.mc.player.blockPosition(), face.support().relative(face.face())))
                     && autismclient.ai.engine.AiBlockInteraction.findFaceHit(this.mc, face.support(), face.face()) != null) {
                     return null;
                  }

                  if (this.approachedFaces.contains(face)) {
                     return this.failWith(AiFailure.PLACEMENT_IMPOSSIBLE, "The required pour face is still hidden after approaching " + face.support().toShortString(), null);
                  }

                  long now = System.nanoTime();
                  if (!face.equals(this.approachFace)) {
                     this.approachFace = face;
                     this.approachDeadline = Math.min(this.deadline, now + 30000000000L);
                  }

                  int seconds = AiPortalCast.secondsUntil(this.approachDeadline, now);
                  if (seconds <= 0) {
                     return this.failWith(AiFailure.TIMEOUT, "No time remains to reach the required pour face", null);
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
                     AiPortalCast.faceApproachGoalWith(
                        face,
                        this.reservedCells,
                        this.molds,
                        this.supports,
                        Math.max(1.5, this.mc.player.blockInteractionRange() - 0.25),
                        this.mc.player.getEyeHeight(),
                        lava
                     )
                  );
                  this.faceApproach = new AiPortalCast.FaceApproachOperation(child, this.approachDeadline, System::nanoTime);
                  this.approaching = true;
                  return this.tickFaceApproach();
               }

               AiOperation.Result tickFaceApproach() {
                  AiOperation.Result result = this.faceApproach.tick();
                  if (!result.terminal()) {
                     return this.running("Moving to see the required pour face");
                  } else {
                     this.approaching = false;
                     this.approachedFaces.add(this.approachFace);
                     if (result.status() == AiOperation.Status.UNCERTAIN) {
                        return this.finish(AiOperation.Status.UNCERTAIN, result.message());
                     } else {
                        return result.status() != AiOperation.Status.SUCCEEDED
                           ? this.failWith(result.failure(), result.message(), null)
                           : this.running("Required pour face reached");
                     }
                  }
               }

               AiOperation.Result init(AiPortalCast.PourFace face) {
                  if (!this.pourClicked && !this.pourAck.isPending() && face != null) {
                     BlockPos pourCell = face.support().relative(face.face());
                     String bucket = BuiltInRegistries.ITEM.getKey(this.mc.player.getMainHandItem().getItem()).toString();
                     if (bucket.equals("minecraft:lava_bucket")) {
                        this.mc.options.keyUp.setDown(false);
                        this.mc.options.keyDown.setDown(false);
                        this.mc.options.keyLeft.setDown(false);
                        this.mc.options.keyRight.setDown(false);
                        this.mc.options.keyJump.setDown(false);
                        this.mc.options.keySprint.setDown(false);
                        if (!AiPortalCast.isSettled(this.mc.player.onGround(), this.mc.player.getDeltaMovement())) {
                           return this.running("Settling before the lava pour");
                        }

                        if (!AiPortalCast.isClearOfPour(this.mc.player.blockPosition(), pourCell)) {
                           return this.failWith(AiFailure.PLACEMENT_IMPOSSIBLE, "Refusing a lava pour into or beside the player's own cells at " + pourCell.toShortString(), null);
                        }
                     }

                     BlockHitResult visible = autismclient.ai.engine.AiBlockInteraction.findFaceHit(this.mc, face.support(), face.face());
                     if (visible == null) {
                        return null;
                     } else {
                        this.aim.aimAt(AiPortalCast.nudgedHitPoint(this.mc.player.getEyePosition(), visible));
                        this.aim.tick();
                        BlockHitResult hit = autismclient.ai.engine.AiAim.raycastLookBlock();
                        if (hit != null && hit.getBlockPos().equals(face.support()) && hit.getDirection() == face.face()) {
                           this.pourAck.beginPour(pourCell, bucket, AiGameState.countInventoryItem(bucket), AiServerEvidence.inventoryRevision(), AiServerEvidence.blockRevision(pourCell));
                           this.mc.gameMode.useItem(this.mc.player, InteractionHand.MAIN_HAND);
                           Compat263.swing(this.mc.player, InteractionHand.MAIN_HAND);
                           this.pourClicked = true;
                           return null;
                        } else {
                           return null;
                        }
                     }
                  } else {
                     return null;
                  }
               }

               AiOperation.Result flush(AiPortalCast.CastStep step) {
                  for (BlockPos zone : AiPortalCast.spreadZone(step.waterCell())) {
                     if (!this.frameCells.contains(zone) && !this.preexistingObsidian.contains(zone) && this.mc.level.getBlockState(zone).is(Blocks.OBSIDIAN)) {
                        return this.failWith(AiFailure.PLACEMENT_IMPOSSIBLE, "The pour spread: obsidian formed outside the frame at " + zone.toShortString(), null);
                     }
                  }

                  return null;
               }

               AiPortalCast.PourFace pourFaceFor(BlockPos cell) {
                  AiPortalCast.CellKind below = this.classify(cell.below());
                  return below != AiPortalCast.CellKind.SOLID && below != AiPortalCast.CellKind.OBSIDIAN
                     ? AiPortalCast.pourFace(cell, this.frameAxis, this.reservedCells, this.cellLookup, List.of())
                     : new AiPortalCast.PourFace(cell.below().immutable(), Direction.UP, null);
               }

               AiOperation.Result tickMove(Supplier<AiOperation> factory, String what) {
                  if (this.moveOp == null) {
                     this.moveOp = factory.get();
                  }

                  boolean pending = this.moveOp.requiresReconciliationAfterEmergency();
                  AiOperation.Result r = this.moveOp.tick();
                  if (!r.terminal()) {
                     return this.running(what);
                  } else {
                     r = AiGuardLayer.wrapPendingTermination(
                        r.status() != AiOperation.Status.SUCCEEDED && (pending || this.moveOp.requiresReconciliationAfterEmergency()), r
                     );
                     this.moveOp.cancel();
                     this.moveOp = null;
                     if (r.status() == AiOperation.Status.UNCERTAIN) {
                        return this.finish(AiOperation.Status.UNCERTAIN, what + ": " + r.message());
                     } else {
                        return r.status() != AiOperation.Status.SUCCEEDED ? this.failWith(r.failure(), what + ": " + r.message(), null) : null;
                     }
                  }
               }

               AiOperation.Result awaitBucket(String what) {
                  AiOperation.Result r = this.tickBucketOp();
                  if (r == null) {
                     return this.running(what);
                  } else if (r.status() == AiOperation.Status.UNCERTAIN) {
                     return this.finish(AiOperation.Status.UNCERTAIN, what + ": " + r.message());
                  } else {
                     return r.status() != AiOperation.Status.SUCCEEDED ? this.failWith(r.failure(), what + ": " + r.message(), null) : null;
                  }
               }

               AiOperation.Result tickBucketOp() {
                  boolean pending = this.bucketOp.requiresReconciliationAfterEmergency();
                  AiOperation.Result r = this.bucketOp.tick();
                  if (!r.terminal()) {
                     return null;
                  }

                  r = AiGuardLayer.wrapPendingTermination(
                     r.status() != AiOperation.Status.SUCCEEDED && (pending || this.bucketOp.requiresReconciliationAfterEmergency()), r
                  );
                  this.bucketOp.cancel();
                  this.bucketOp = null;
                  return r;
               }

               AiOperation fillBucketOp(String fluid, BlockPos source, int seconds) {
                  return AiBucket.init(
                     AiJson.objectOf(
                        "action", "fill", "fluid", fluid, "x", source.getX(), "y", source.getY(), "z", source.getZ(), "timeoutSeconds", seconds
                     )
                  );
               }

               AiOperation.Result tickSurvivalGuard() {
                  if (this.guardOp != null) {
                     AiOperation.Result result = this.guardOp.tick();
                     if (!result.terminal()) {
                        return this.running("Interrupted: " + this.guardReason + " - " + result.message());
                     }

                     this.guardOp.cancel();
                     this.guardOp = null;
                     return this.running("Resuming after " + this.guardReason);
                  } else {
                     if (!this.canInterruptNow()) {
                        return null;
                     }

                     this.vitals = AiGuard.sampleStatus();
                     AiOperation action = AiGuard.createResponseOperation(this.guardPolicy, this.vitals, this.secondsLeft());
                     if (action == null) {
                        return null;
                     }

                     this.guardReason = AiGuard.decisionReason(this.guardPolicy, this.vitals);
                     this.put++;
                     if (this.moveOp != null) {
                        this.moveOp.cancel();
                        this.moveOp = null;
                     }

                     if (this.bucketOp != null) {
                        this.bucketOp.cancel();
                        this.bucketOp = null;
                     }

                     if (this.approaching) {
                        this.faceApproach.cancel();
                        this.approaching = false;
                     }

                     this.guardOp = action;
                     return this.running("Interrupted: " + this.guardReason);
                  }
               }

               void nextCell() {
                  this.castCellCount++;
                  this.stepIndex++;
                  this.stage = AiPortalCast.CastStage.STANCE;
                  this.pop = 0;
                  this.tick = 0;
                  this.molds = List.of();
                  this.set = 0;
                  this.lavaFace = null;
                  this.pourClicked = false;
               }

               void countMaterials() {
                  this.waterBuckets = AiGameState.countInventoryItem("minecraft:water_bucket");
                  this.lavaBuckets = AiGameState.countInventoryItem("minecraft:lava_bucket");
                  this.emptyBuckets = AiGameState.countInventoryItem("minecraft:bucket");
                  this.throwawayCount = this.countAll(AiPortalCast.THROWAWAY_BLOCKS);
                  this.flintAndSteelCount = AiGameState.countInventoryItem("minecraft:flint_and_steel");
                  this.fireChargeCount = AiGameState.countInventoryItem("minecraft:fire_charge");
                  this.doorwayWoodCount = this.countAll(AiPortalCast.DOORWAY_WOOD);
               }

               int countAll(List<String> ids) {
                  int total = 0;

                  for (String id : ids) {
                     total += AiGameState.countInventoryItem(id);
                  }

                  return total;
               }

               Item firstPlaceable(List<String> ids) {
                  for (String id : ids) {
                     if (AiGameState.countInventoryItem(id) > 0) {
                        Item item = BuiltInRegistries.ITEM.getOptional(Identifier.parse(id)).orElse(null);
                        if (item instanceof BlockItem
                           && (ids != AiPortalCast.THROWAWAY_BLOCKS || autismclient.ai.engine.AiRestock.canSpendStack(item.getDefaultInstance(), this.mc.player.getInventory()))) {
                           return item;
                        }
                     }
                  }

                  return null;
               }

               BlockPos litPortalCell() {
                  for (BlockPos interior : AiDimension.write(this.frameOrigin, this.frameAxis)) {
                     if (this.mc.level.getBlockState(interior).is(Blocks.NETHER_PORTAL)) {
                        return interior;
                     }
                  }

                  return null;
               }

               BlockPos nearestSource(BlockPos center, int radius, int vertical, boolean lava) {
                  return this.nearestSourceWith(center, radius, vertical, lava, false);
               }

               BlockPos nearestSourceWith(BlockPos center, int radius, int vertical, boolean lava, boolean open) {
                  BlockPos best = null;
                  double bestDistance = 1.7976931348623157E308;

                  for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -vertical, -radius), center.offset(radius, vertical, radius))) {
                     if (AiTerrain.isLoaded(this.mc.level, pos)) {
                        FluidState fluid = this.mc.level.getFluidState(pos);
                        if (fluid.isSource()
                           && fluid.is(lava ? FluidTags.LAVA : FluidTags.WATER)
                           && (!open || this.mc.level.getBlockState(pos.above()).isAir())) {
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

               List<BlockPos> lavaSourcesAround(BlockPos center) {
                  List<BlockPos> sources = new ArrayList<>();

                  for (BlockPos pos : BlockPos.betweenClosed(center.offset(-12, -6, -12), center.offset(12, 6, 12))) {
                     if (sources.size() >= 64) {
                        break;
                     }

                     if (AiTerrain.isLoaded(this.mc.level, pos)) {
                        FluidState fluid = this.mc.level.getFluidState(pos);
                        if (fluid.isSource() && fluid.is(FluidTags.LAVA)) {
                           sources.add(pos.immutable());
                        }
                     }
                  }

                  sources.sort(Comparator.comparingDouble(center::distSqr));
                  return sources;
               }

               List<BlockPos> siteClearPlan(List<BlockPos> sources) {
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
                                 if (this.classify(cell) == AiPortalCast.CellKind.SOLID) {
                                    if (!this.load(cell)) {
                                       ok = false;
                                       break;
                                    }

                                    digs.add(cell.immutable());
                                 }
                              }

                              if (ok && !digs.isEmpty() && digs.size() <= 30) {
                                 Set<BlockPos> dug = new HashSet<>(digs);
                                 AiPortalCast.CellLookup after = pos -> dug.contains(pos) ? AiPortalCast.CellKind.AIR : this.classify(pos);
                                 Set<BlockPos> reserved = AiPortalCast.reservedCells(origin, axis);
                                 AiPortalCast.CastPlan plan = AiPortalCast.planCast(AiPortalCast.frameCellsSorted(origin, axis), axis, reserved, after);
                                 if (plan.blocked() == null) {
                                    long usable = sources.stream().filter(q -> !reserved.contains(q)).count();
                                    if (plan.lavaNeeded() <= usable) {
                                       double score = digs.size() * 40.0
                                          + plan.lavaNeeded() * 25.0
                                          + origin.distSqr(this.mc.player.blockPosition());
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
                        Comparator.<BlockPos>comparingInt(q -> -q.getY()).thenComparingDouble(q -> q.distSqr(this.mc.player.blockPosition()))
                     );
                  }

                  return best;
               }

               boolean load(BlockPos cell) {
                  if (!AiTerrain.isLoaded(this.mc.level, cell)) {
                     return false;
                  } else if (AiPortalCast.load(this.mc.level, cell)) {
                     return false;
                  } else {
                     BlockState state = this.mc.level.getBlockState(cell);
                     if (state.getFluidState().isEmpty()
                        && !state.hasBlockEntity()
                        && !state.is(Blocks.OBSIDIAN)
                        && !state.is(Blocks.BEDROCK)
                        && !(state.getBlock() instanceof FallingBlock)) {
                        float hardness = state.getDestroySpeed(this.mc.level, cell);
                        return hardness >= 0.0F && hardness <= 5.0F;
                     } else {
                        return false;
                     }
                  }
               }

               AiOperation init(final BlockPos target, double radius, final int seconds) {
                  final AiOperation first = this.load(target, radius, seconds);
                  return radius >= 3.0 ? first : new AiOperation() {
                     AiOperation stanceOp = first;
                     boolean widened;

                     @Override
                     public AiOperation.Result tick() {
                        AiOperation.Result r = this.stanceOp.tick();
                        if (r.terminal() && !this.widened && r.status() != AiOperation.Status.SUCCEEDED && r.status() != AiOperation.Status.CANCELLED) {
                           this.stanceOp.cancel();
                           this.widened = true;
                           this.stanceOp = load(target, 3.0, seconds);
                           return AiGameOperations.result(AiOperation.Status.RUNNING, r.message() + "; trying a wider stance", new JsonObject());
                        } else {
                           return r;
                        }
                     }

                     @Override
                     public boolean canInterruptNow() {
                        return this.stanceOp.canInterruptNow();
                     }

                     @Override
                     public boolean requiresReconciliationAfterEmergency() {
                        return this.stanceOp.requiresReconciliationAfterEmergency();
                     }

                     @Override
                     public void cancel() {
                        this.stanceOp.cancel();
                     }
                  };
               }

               AiOperation load(BlockPos target, double radius, int seconds) {
                  Set<Long> keep = new HashSet<>();
                  if (this.reservedCells != null) {
                     for (BlockPos cell : this.reservedCells) {
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

               AiOperation.Result tickExcavatePhase() {
                  if (this.moveOp != null) {
                     AiOperation.Result r = this.moveOp.tick();
                     if (!r.terminal()) {
                        return this.running("Clearing the frame site (" + (this.siteDigs.size() + 1) + " left)");
                     }

                     this.moveOp.cancel();
                     this.moveOp = null;
                     if (r.status() != AiOperation.Status.SUCCEEDED && ++this.siteDigFailures > 3) {
                        return this.failWith(AiFailure.NO_REACHABLE_TARGET, "Could not clear the frame site: " + r.message(), null);
                     }
                  }

                  while (!this.siteDigs.isEmpty() && this.classify(this.siteDigs.peek()) != AiPortalCast.CellKind.SOLID) {
                     this.siteDigs.poll();
                  }

                  if (this.siteDigs.isEmpty()) {
                     this.phase = AiPortalCast.CastPhase.SITE;
                     return this.running("Frame site cleared; planning the cast");
                  } else {
                     BlockPos cell = this.siteDigs.poll();
                     this.moveOp = autismclient.ai.engine.AiBreakBlock.breakBlock(cell, 20, false);
                     return this.running("Clearing the frame site at " + cell.toShortString());
                  }
               }

               AiPortalCast.CellKind classify(BlockPos pos) {
                  if (!AiTerrain.isLoaded(this.mc.level, pos)) {
                     return AiPortalCast.CellKind.SOLID;
                  }

                  BlockState state = this.mc.level.getBlockState(pos);
                  if (state.is(Blocks.OBSIDIAN)) {
                     return AiPortalCast.CellKind.OBSIDIAN;
                  }

                  FluidState fluid = state.getFluidState();
                  if (!fluid.isEmpty()) {
                     if (!fluid.isSource()) {
                        return AiPortalCast.CellKind.SOLID;
                     } else if (fluid.is(FluidTags.LAVA)) {
                        return AiPortalCast.CellKind.LAVA;
                     } else {
                        return fluid.is(FluidTags.WATER) ? AiPortalCast.CellKind.WATER : AiPortalCast.CellKind.SOLID;
                     }
                  } else {
                     return !state.isAir() && !state.canBeReplaced() ? AiPortalCast.CellKind.SOLID : AiPortalCast.CellKind.AIR;
                  }
               }

               int secondsLeft() {
                  return (int)Math.max(1L, (this.deadline - System.nanoTime()) / 1000000000L);
               }

               JsonObject missingOne(String id) {
                  JsonObject missing = new JsonObject();
                  missing.addProperty(id, 1);
                  return missing;
               }

               JsonObject progressJson() {
                  JsonObject out = AiJson.objectOf(
                     "phase",
                     this.phase.name().toLowerCase(Locale.ROOT),
                     "castCells",
                     this.castCellCount,
                     "frameCells",
                     this.frameCells.size(),
                     "lavaNeeded",
                     this.plan == null ? 0 : this.plan.lavaNeeded(),
                     "lavaPoured",
                     this.lavaPouredCount,
                     "waterRecoveryPending",
                     this.waterRecovery.isRecoveryPending(),
                     "pourAcknowledgementPending",
                     this.pourAck.isPending(),
                     "lit",
                     this.lit
                  );
                  if (!this.lighter.isEmpty()) {
                     out.addProperty("litWith", this.lighter);
                  }

                  if (this.frameOrigin != null) {
                     out.add(
                        "frame",
                        AiJson.objectOf(
                           "x",
                           this.frameOrigin.getX(),
                           "y",
                           this.frameOrigin.getY(),
                           "z",
                           this.frameOrigin.getZ(),
                           "axis",
                           this.frameAxis.name().toLowerCase(Locale.ROOT)
                        )
                     );
                  }

                  if (this.plan != null && this.stepIndex < this.plan.steps().size()) {
                     AiPortalCast.CastStep step = this.plan.steps().get(this.stepIndex);
                     out.add(
                        "step",
                        AiJson.objectOf(
                           "index", this.stepIndex, "action", step.action().name().toLowerCase(Locale.ROOT), "cell", step.cell().toShortString()
                        )
                     );
                  }

                  if (this.put > 0) {
                     out.add("guard", AiGuard.statusToJson(this.vitals, this.put, this.guardReason));
                  }

                  if (this.faceApproach != null && this.faceApproach.lastResult() != null) {
                     AiOperation.Result receipt = this.faceApproach.lastResult();
                     out.add(
                        "pourApproach",
                        AiJson.objectOf(
                           "status",
                           receipt.status().name(),
                           "message",
                           receipt.message(),
                           "pending",
                           this.faceApproach.requiresReconciliationAfterEmergency(),
                           "receipt",
                           AiRunResources.parseEvidenceJson(receipt.dataJson())
                        )
                     );
                  }

                  return out;
               }

               AiOperation.Result running(String message) {
                  return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.progressJson().toString(), List.of());
               }

               AiOperation.Result finish(AiOperation.Status status, String message) {
                  AiOperation.Result result = this.terminalGuard
                     .recordTerminal(this.unconfirmed(), new AiOperation.Result(status, message, this.progressJson().toString(), List.of()));
                  this.cancel();
                  return result;
               }

               AiOperation.Result failWith(AiFailure code, String message, JsonObject missing) {
                  JsonObject data = this.progressJson();
                  if (missing != null) {
                     data.add("missing", missing);
                  }

                  AiOperation.Result result = this.terminalGuard.recordTerminal(this.unconfirmed(), AiOperation.Result.failed(code, message, data.toString()));
                  this.cancel();
                  return result;
               }

               boolean unconfirmed() {
                  return this.pourAck.isPending()
                     || this.placer.needsReconciliation()
                     || this.faceApproach != null && this.faceApproach.requiresReconciliationAfterEmergency()
                     || this.moveOp != null && this.moveOp.requiresReconciliationAfterEmergency()
                     || this.bucketOp != null && this.bucketOp.requiresReconciliationAfterEmergency()
                     || this.guardOp != null && this.guardOp.requiresReconciliationAfterEmergency();
               }

               @Override
               public boolean canInterruptNow() {
                  return !this.pourClicked
                     && !this.waterRecovery.isRecoveryPending()
                     && !this.pourAck.isPending()
                     && !this.placer.needsReconciliation()
                     && (this.moveOp == null || this.moveOp.canInterruptNow())
                     && (this.bucketOp == null || this.bucketOp.canInterruptNow())
                     && (this.faceApproach == null || this.faceApproach.canInterruptNow())
                     && (this.guardOp == null || this.guardOp.canInterruptNow());
               }

               @Override
               public boolean requiresReconciliationAfterEmergency() {
                  return this.terminalGuard
                     .isUnconfirmed(
                        this.pourClicked
                           || this.waterRecovery.isRecoveryPending()
                           || this.pourAck.isPending()
                           || this.placer.needsReconciliation()
                           || this.faceApproach != null && this.faceApproach.requiresReconciliationAfterEmergency()
                           || this.moveOp != null && this.moveOp.requiresReconciliationAfterEmergency()
                           || this.bucketOp != null && this.bucketOp.requiresReconciliationAfterEmergency()
                           || this.guardOp != null && this.guardOp.requiresReconciliationAfterEmergency()
                     );
               }

               @Override
               public void cancel() {
                  if (!this.cancelled) {
                     this.cancelled = true;
                     this.aim.clear();
                     this.placer.reset();
                     if (this.faceApproach != null) {
                        this.faceApproach.cancel();
                     }

                     if (this.moveOp != null) {
                        this.moveOp.cancel();
                        this.moveOp = null;
                     }

                     if (this.bucketOp != null) {
                        this.bucketOp.cancel();
                        this.bucketOp = null;
                     }

                     if (this.guardOp != null) {
                        this.guardOp.cancel();
                        this.guardOp = null;
                     }

                     if (this.mc.player != null && this.mc.player.getInventory().getSelectedSlot() != this.originalSlot) {
                        this.mc.player.getInventory().setSelectedSlot(this.originalSlot);
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

   record CastStep(BlockPos cell, AiPortalCast.CastAction action, BlockPos waterCell, List<BlockPos> dams, AiPortalCast.PourFace face) {
   }

   private enum CastStage {
      STANCE,
      MOLDS,
      LAVA_REFILL,
      LAVA_POUR,
      WATER_POUR,
      SCOOP;
   }

   enum CellKind {
      AIR,
      WATER,
      LAVA,
      OBSIDIAN,
      SOLID;
   }

   enum CastAction {
      SKIP,
      WATER_OVER_LAVA,
      LAVA_THEN_WATER;
   }

   static final class PourAck {
      private BlockPos pourCell;
      private String bucketId;
      private int filledBefore;
      private long inventoryRevision;
      private long cellRevisionAtPour;

      void beginPour(BlockPos at, String bucketId, int filledBuckets, long inventoryRevision, long cellRevision) {
         if (!this.isPending() && filledBuckets > 0) {
            this.pourCell = at.immutable();
            this.bucketId = bucketId;
            this.filledBefore = filledBuckets;
            this.inventoryRevision = inventoryRevision;
            this.cellRevisionAtPour = cellRevision;
         } else {
            throw new IllegalStateException("Unacknowledged fluid pour");
         }
      }

      boolean isPending() {
         return this.pourCell != null;
      }

      BlockPos pendingCell() {
         return this.pourCell;
      }

      String pendingBucket() {
         return this.bucketId;
      }

      void acknowledge(int filledBuckets, long inventoryRevision, long cellRevision) {
         if (this.isPending() && filledBuckets < this.filledBefore && inventoryRevision > this.inventoryRevision && cellRevision > this.cellRevisionAtPour) {
            this.pourCell = null;
         }
      }

      void clearPour(BlockPos at) {
         if (this.isPending() && this.pourCell.equals(at)) {
            this.pourCell = null;
         }
      }
   }

   enum CastPhase {
      PREFLIGHT,
      LOCATE,
      SITE,
      EXCAVATE,
      WATER,
      CAST,
      LIGHT;
   }

   static final class WaterRecovery {
      private BlockPos pourCell;
      private int filledBefore;

      void beginWaterRecovery(BlockPos at, int filledBuckets) {
         if (this.pourCell == null && filledBuckets > 0) {
            this.pourCell = at.immutable();
            this.filledBefore = filledBuckets;
         } else {
            throw new IllegalStateException("Unreconciled water pour");
         }
      }

      boolean isRecoveryPending() {
         return this.pourCell != null;
      }

      boolean isRecoveryConfirmed(BlockPos at, int filledBuckets, boolean fillAcknowledged) {
         if (fillAcknowledged && this.pourCell != null && this.pourCell.equals(at) && filledBuckets >= this.filledBefore) {
            this.pourCell = null;
            return true;
         } else {
            return false;
         }
      }
   }

   static final class FaceApproachOperation implements AiOperation {
      private final AiOperation child;
      private final long deadline;
      private final LongSupplier clock;
      private final AiPreparationReceipt receipt = new AiPreparationReceipt();
      private AiOperation.Result finalResult;
      private boolean cancelled;

      FaceApproachOperation(AiOperation child, long deadline, LongSupplier now) {
         this.child = child;
         this.deadline = deadline;
         this.clock = now;
      }

      @Override
      public AiOperation.Result tick() {
         if (this.finalResult != null) {
            return this.finalResult;
         }

         if (this.cancelled) {
            return this.finishWith(new AiOperation.Result(AiOperation.Status.CANCELLED, "Pour face approach cancelled", "{}", List.of()));
         }

         if (this.clock.getAsLong() >= this.deadline) {
            return this.finishWith(AiOperation.Result.timedOut(AiFailure.TIMEOUT, "Pour face approach exceeded its bounded deadline", "{}"));
         }

         AiOperation.Result result = this.receipt.tickChild(this.child);
         return result.terminal() ? this.finishWith(this.receipt.lastChildResult()) : result;
      }

      private AiOperation.Result finishWith(AiOperation.Result result) {
         this.finalResult = this.receipt.finalizeResult(result, this.receipt.requiresReconciliation(this.child));
         this.receipt.cancelChild(this.child);
         return this.finalResult;
      }

      AiOperation.Result lastResult() {
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
         this.cancelled = true;
      }
   }

   record CastPlan(List<AiPortalCast.CastStep> steps, BlockPos blocked, String reason, int lavaNeeded, int moldBlocks) {
   }

   record PourFace(BlockPos support, Direction face, BlockPos mold) {
   }

   interface CellLookup {
      AiPortalCast.CellKind at(BlockPos var1);
   }

   static final class TerminalGuard {
      private boolean terminated;
      private boolean terminalUnconfirmed;

      AiOperation.Result recordTerminal(boolean unconfirmed, AiOperation.Result result) {
         this.terminalUnconfirmed = unconfirmed || result.status() == AiOperation.Status.UNCERTAIN;
         this.terminated = true;
         return AiGuardLayer.wrapPendingTermination(unconfirmed, result);
      }

      boolean isUnconfirmed(boolean activePending) {
         return this.terminated ? this.terminalUnconfirmed : activePending;
      }
   }
}
