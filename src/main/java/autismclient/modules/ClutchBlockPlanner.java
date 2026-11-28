package autismclient.modules;

import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2IntMap.Entry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

final class ClutchBlockPlanner {
   private static final Direction[] DIRECTIONS = Direction.values();
   private static final double load_D = 0.3;
   private static final double read_D = 0.03;
   private static final int GOAL_CANDIDATES = 6;
   static final double[] init_DArr = new double[]{0.5, 0.15, 0.85, 0.92, 0.03, 0.97};
   static final double init_D = 0.02;
   private static final long NO_POS = -9223372036854775808L;
   private static final int read_I = 3;
   private static final double write_D = 1.5;
   private static final double STRICT_MARGIN = 0.06;
   private static final int CANDIDATE_HORIZON_TICKS = 12;
   static final int init_I = 4;
   private static final List<AABB> UNIT_CUBE = List.of(new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));

   private static boolean nothingPlaceableNear(ClutchBlockPlanner.PlacementWorld world, Vec3 eye, double cellReach, LongOpenHashSet linkable) {
      double distance = Math.sqrt(cellReach) + 1.0 + 1.0E-6;
      if (!world.init(eye, distance)) {
         return false;
      }

      if (linkable != null) {
         for (long key : linkable) {
            if (eye.distanceToSqr(BlockPos.getX(key) + 0.5, BlockPos.getY(key) + 0.5, BlockPos.getZ(key) + 0.5) <= distance * distance) {
               return false;
            }
         }
      }

      return true;
   }

   private ClutchBlockPlanner() {
   }

   static ClutchBlockPlanner.ClutchPlan plan(
      List<AABB> bodies, double eyeHeight, double reach, ClutchBlockPlanner.PlacementWorld world, int firstTick, double fallSoFar, double health, BlockPos keep
   ) {
      return bodies != null && !bodies.isEmpty()
         ? plan(bodies, eyeHeight, reach, world, firstTick, fallSoFar, health, keep, Mth.floor(bodies.getFirst().minY + 1.0E-6) - 1, null)
         : null;
   }

   static ClutchBlockPlanner.ClutchPlan plan(
      List<AABB> bodies,
      double eyeHeight,
      double reach,
      ClutchBlockPlanner.PlacementWorld world,
      int firstTick,
      double fallSoFar,
      double health,
      BlockPos keep,
      int homeRow,
      ToDoubleFunction<ClutchBlockPlanner.PlacementLink> firstCost
   ) {
      return plan(bodies, eyeHeight, reach, world, firstTick, fallSoFar, health, keep, homeRow, firstCost, 2147483647);
   }

   static ClutchBlockPlanner.ClutchPlan plan(
      List<AABB> bodies,
      double eyeHeight,
      double reach,
      ClutchBlockPlanner.PlacementWorld world,
      int firstTick,
      double fallSoFar,
      double health,
      BlockPos keep,
      int homeRow,
      ToDoubleFunction<ClutchBlockPlanner.PlacementLink> firstCost,
      int blocks
   ) {
      return plan(bodies, eyeHeight, reach, world, firstTick, fallSoFar, health, keep, homeRow, firstCost, blocks, ClutchFallPhysics::estimateFallDamage, 0.0, 1.0 / 0.0);
   }

   static ClutchBlockPlanner.ClutchPlan plan(
      List<AABB> bodies,
      double eyeHeight,
      double reach,
      ClutchBlockPlanner.PlacementWorld world,
      int firstTick,
      double fallSoFar,
      double health,
      BlockPos keep,
      int homeRow,
      ToDoubleFunction<ClutchBlockPlanner.PlacementLink> firstCost,
      int blocks,
      DoubleUnaryOperator damage,
      double bestEffortReach
   ) {
      return plan(bodies, eyeHeight, reach, world, firstTick, fallSoFar, health, keep, homeRow, firstCost, blocks, damage, bestEffortReach, 1.0 / 0.0);
   }

   static ClutchBlockPlanner.ClutchPlan plan(
      List<AABB> bodies,
      double eyeHeight,
      double reach,
      ClutchBlockPlanner.PlacementWorld world,
      int firstTick,
      double fallSoFar,
      double health,
      BlockPos keep,
      int homeRow,
      ToDoubleFunction<ClutchBlockPlanner.PlacementLink> firstCost,
      int blocks,
      DoubleUnaryOperator damage,
      double bestEffortReach,
      double uncaught
   ) {
      return plan(bodies, eyeHeight, reach, world, firstTick, fallSoFar, health, keep, homeRow, firstCost, blocks, damage, bestEffortReach, uncaught, List.of());
   }

   static ClutchBlockPlanner.ClutchPlan plan(
      List<AABB> bodies,
      double eyeHeight,
      double reach,
      ClutchBlockPlanner.PlacementWorld world,
      int firstTick,
      double fallSoFar,
      double health,
      BlockPos keep,
      int homeRow,
      ToDoubleFunction<ClutchBlockPlanner.PlacementLink> firstCost,
      int blocks,
      DoubleUnaryOperator damage,
      double bestEffortReach,
      double uncaught,
      List<Vec3> writtenEyes
   ) {
      return planClutch(
         bodies, eyeHeight, reach, world, firstTick, fallSoFar, health, keep, homeRow, firstCost, blocks, damage, bestEffortReach, uncaught, writtenEyes, false
      );
   }

   static ClutchBlockPlanner.ClutchPlan planClutch(
      List<AABB> bodies,
      double eyeHeight,
      double reach,
      ClutchBlockPlanner.PlacementWorld world,
      int firstTick,
      double fallSoFar,
      double health,
      BlockPos keep,
      int homeRow,
      ToDoubleFunction<ClutchBlockPlanner.PlacementLink> firstCost,
      int blocks,
      DoubleUnaryOperator damage,
      double bestEffortReach,
      double uncaught,
      List<Vec3> writtenEyes,
      boolean marginal
   ) {
      return planSearch(
         bodies,
         eyeHeight,
         reach,
         world,
         firstTick,
         fallSoFar,
         health,
         keep,
         homeRow,
         firstCost,
         blocks,
         damage,
         bestEffortReach,
         uncaught,
         writtenEyes,
         marginal,
         false
      );
   }

   static ClutchBlockPlanner.ClutchPlan planUrgent(
      List<AABB> bodies,
      double eyeHeight,
      double reach,
      ClutchBlockPlanner.PlacementWorld world,
      int firstTick,
      double fallSoFar,
      double health,
      BlockPos keep,
      int homeRow,
      ToDoubleFunction<ClutchBlockPlanner.PlacementLink> firstCost,
      int blocks,
      DoubleUnaryOperator damage,
      double uncaught,
      List<Vec3> writtenEyes,
      boolean marginal
   ) {
      ClutchBlockPlanner.ClutchPlan result = planSearch(
         bodies, eyeHeight, reach, world, firstTick, fallSoFar, health, keep, homeRow, firstCost, blocks, damage, 0.0, uncaught, writtenEyes, marginal, true
      );
      return result == null ? null : new ClutchBlockPlanner.ClutchPlan(result.init_List(), result.init_BlockPos(), result.init_I(), result.init_D(), result.load(), true);
   }

   private static ClutchBlockPlanner.ClutchPlan planSearch(
      List<AABB> bodies,
      double eyeHeight,
      double reach,
      ClutchBlockPlanner.PlacementWorld world,
      int firstTick,
      double fallSoFar,
      double health,
      BlockPos keep,
      int homeRow,
      ToDoubleFunction<ClutchBlockPlanner.PlacementLink> firstCost,
      int blocks,
      DoubleUnaryOperator damage,
      double bestEffortReach,
      double uncaught,
      List<Vec3> writtenEyes,
      boolean marginal,
      boolean immediate
   ) {
      if (bodies != null && bodies.size() >= 2 && world != null && blocks > 0) {
         ClutchBlockPlanner.FallFlight flight = new ClutchBlockPlanner.FallFlight(
            bodies, eyeHeight, world, fallSoFar, writtenEyes, !(bestEffortReach > 0.0) && !marginal ? 0.06 : 1.0E-6
         );
         flight.margin = marginal ? 1.0E-5 : 0.02;
         flight.immediate = immediate;
         if (flight.landingDue.isEmpty()) {
            return bestEffortReach > 0.0 ? flight.nearestPlan(firstTick, bestEffortReach) : null;
         }

         ClutchBlockPlanner.PlanChooser choice = new ClutchBlockPlanner.PlanChooser(flight, health, keep, homeRow, firstCost, blocks, damage, Math.max(0.0, uncaught - 2.0));
         int horizon = choice.survivableHorizon(bestEffortReach > 0.0);
         if (bestEffortReach > 0.0 && uncaught >= health) {
            horizon = Math.max(horizon, Math.max(1, firstTick) + 3 + 1);
         }

         if (horizon <= Math.max(0, firstTick)) {
            return bestEffortReach > 0.0 ? flight.nearestPlan(firstTick, bestEffortReach) : null;
         }

         ClutchBlockPlanner.LinkSearch strict = flight.search(firstTick, reach, !immediate, horizon);
         ClutchBlockPlanner.ClutchPlan plan = choice.bestPlan(strict, false, false);
         if (plan == null && !(bestEffortReach <= 0.0)) {
            ClutchBlockPlanner.LinkSearch relaxed = flight.search(firstTick, Math.max(reach, bestEffortReach), false, horizon);
            plan = choice.bestPlan(relaxed, false, true);
            if (plan == null) {
               plan = choice.bestPlan(strict, true, false);
            }

            if (plan == null) {
               plan = choice.bestPlan(relaxed, true, true);
            }

            if (plan == null) {
               plan = flight.nearestPlan(firstTick, Math.max(reach, bestEffortReach));
            }

            return plan;
         } else {
            return plan;
         }
      } else {
         return null;
      }
   }

   static ClutchBlockPlanner.ClutchPlan nearestReachPlan(
      List<AABB> bodies, double eyeHeight, double reach, ClutchBlockPlanner.PlacementWorld world, int firstTick, List<Vec3> writtenEyes
   ) {
      return bodies != null && bodies.size() >= 2 && world != null
         ? new ClutchBlockPlanner.FallFlight(bodies, eyeHeight, world, 0.0, writtenEyes, 1.0E-6).nearestPlanWithin(firstTick, reach, Math.min(bodies.size(), 8))
         : null;
   }

   static ClutchBlockPlanner.ClutchPlan load(List<AABB> bodies, double eyeHeight, double reach, final ClutchBlockPlanner.PlacementWorld world, int firstTick, List<Vec3> eyes) {
      ClutchBlockPlanner.PlacementLink tower = towerLink(bodies, eyeHeight, reach, world, firstTick, eyes);
      ClutchBlockPlanner.ClutchPlan nearby = tower != null && tower.init_I() <= firstTick + 1
         ? null
         : nearestReachPlan(bodies, eyeHeight, reach, world, firstTick, eyes);
      ClutchBlockPlanner.ClutchPlan first = nearby == null || tower != null && nearby.firstLink().init_I() >= tower.init_I()
         ? (tower == null ? null : new ClutchBlockPlanner.ClutchPlan(List.of(tower), tower.init_BlockPos(), tower.load_I() + 1, 0.0, false))
         : nearby;
      if (first == null) {
         return null;
      }

      final ClutchBlockPlanner.PlacementLink root = first.firstLink();
      ClutchBlockPlanner.PlacementWorld withRoot = new ClutchBlockPlanner.PlacementWorld() {
         @Override
         public boolean supportAt(int x, int y, int z) {
            return x == root.init_BlockPos().getX() && y == root.init_BlockPos().getY() && z == root.init_BlockPos().getZ() || world.supportAt(x, y, z);
         }

         @Override
         public boolean canPlaceAt(int x, int y, int z) {
            return (x != root.init_BlockPos().getX() || y != root.init_BlockPos().getY() || z != root.init_BlockPos().getZ()) && world.canPlaceAt(x, y, z);
         }

         @Override
         public boolean allowedStart(int x, int y, int z) {
            return world.allowedStart(x, y, z);
         }

         @Override
         public boolean init(Vec3 eye, double distance) {
            return eye.distanceToSqr(Vec3.atCenterOf(root.init_BlockPos())) > distance * distance && world.init(eye, distance);
         }
      };
      int after = root.init_I() + 1;
      ClutchBlockPlanner.PlacementLink next = towerLink(bodies, eyeHeight, reach, withRoot, after, eyes);
      ClutchBlockPlanner.ClutchPlan extra = next != null && next.init_I() <= after ? null : nearestReachPlan(bodies, eyeHeight, reach, withRoot, after, eyes);
      if (extra != null && (next == null || extra.firstLink().init_I() < next.init_I())) {
         next = extra.firstLink();
      }

      return next != null && next.init_I() == after && next.load_BlockPos().equals(root.init_BlockPos())
         ? new ClutchBlockPlanner.ClutchPlan(List.of(root, next), first.init_BlockPos(), first.init_I(), first.init_D(), first.load())
         : first;
   }

   static List<ClutchBlockPlanner.PlacementLink> unusedCandidateLinks(List<AABB> bodies, double eyeHeight, double reach, ClutchBlockPlanner.PlacementWorld world, int firstTick, int limit) {
      return init_List_List(bodies, eyeHeight, reach, world, firstTick, limit, List.of());
   }

   static List<ClutchBlockPlanner.PlacementLink> init_List_List(
      List<AABB> bodies, double eyeHeight, double reach, ClutchBlockPlanner.PlacementWorld world, int firstTick, int limit, List<Vec3> writtenEyes
   ) {
      if (bodies != null && bodies.size() >= 2 && world != null && limit > 0) {
         ClutchBlockPlanner.FallFlight flight = new ClutchBlockPlanner.FallFlight(bodies, eyeHeight, world, 0.0, writtenEyes);
         int horizon = Math.min(bodies.size() - 1, 12);
         int radius = Mth.ceil(reach) + 1;
         double cellReach = (reach + 1.0) * (reach + 1.0);
         LongOpenHashSet seen = new LongOpenHashSet();
         List<long[]> found = new ArrayList<>();

         for (int k = Math.max(0, firstTick); k < horizon; k++) {
            Vec3 eye = flight.eyes[k];
            AABB body = bodies.get(k).deflate(1.0E-4);
            int ex = Mth.floor(eye.x);
            int ey = Mth.floor(eye.y);
            int ez = Mth.floor(eye.z);
            if (!nothingPlaceableNear(world, eye, cellReach, null)) {
               for (int x = ex - radius; x <= ex + radius; x++) {
                  for (int y = ey - radius; y <= ey + radius; y++) {
                     for (int z = ez - radius; z <= ez + radius; z++) {
                        if (!(eye.distanceToSqr(x + 0.5, y + 0.5, z + 0.5) > cellReach)) {
                           long key = BlockPos.asLong(x, y, z);
                           if (!seen.contains(key)
                              && flight.cells.isReplaceable(key, x, y, z)
                              && (
                                 !(body.minX < x + 1)
                                    || !(body.maxX > x)
                                    || !(body.minY < y + 1)
                                    || !(body.maxY > y)
                                    || !(body.minZ < z + 1)
                                    || !(body.maxZ > z)
                              )
                              && flight.cells.hasAdjacentSupport(key, x, y, z)) {
                              int hit = flight.contacts.init(key, k);
                              if (hit >= 0) {
                                 for (int d = 0; d < DIRECTIONS.length; d++) {
                                    Direction direction = DIRECTIONS[d];
                                    int sx = x + direction.getStepX();
                                    int sy = y + direction.getStepY();
                                    int sz = z + direction.getStepZ();
                                    if (flight.cells.isSupport(BlockPos.asLong(sx, sy, sz), sx, sy, sz)
                                       && (k != 0 || world.init(sx, sy, sz, direction.getOpposite()))
                                       && canReachFace(eye, sx, sy, sz, direction.getOpposite(), reach)) {
                                       seen.add(key);
                                       found.add(new long[]{key, direction.getOpposite().ordinal(), k, hit});
                                       break;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         found.sort((a, b) -> a[2] != b[2] ? Long.compare(a[2], b[2]) : Long.compare(a[3], b[3]));
         List<ClutchBlockPlanner.PlacementLink> links = new ArrayList<>(Math.min(limit, found.size()));

         for (long[] entry : found.subList(0, Math.min(limit, found.size()))) {
            BlockPos cell = BlockPos.of(entry[0]);
            Direction face = DIRECTIONS[(int)entry[1]];
            int k = (int)entry[2];
            links.add(new ClutchBlockPlanner.PlacementLink(cell, cell.relative(face.getOpposite()), face, k, k));
         }

         return links;
      } else {
         return List.of();
      }
   }

   static ClutchBlockPlanner.PlacementLink unusedTowerLink(List<AABB> bodies, double eyeHeight, double reach, ClutchBlockPlanner.PlacementWorld world, int firstTick) {
      return towerLink(bodies, eyeHeight, reach, world, firstTick, List.of());
   }

   static ClutchBlockPlanner.PlacementLink towerLink(
      List<AABB> bodies, double eyeHeight, double reach, ClutchBlockPlanner.PlacementWorld world, int firstTick, List<Vec3> writtenEyes
   ) {
      if (bodies != null && bodies.size() >= 2 && world != null) {
         int horizon = Math.min(bodies.size() - 2, 4);

         for (int k = Math.max(0, firstTick); k <= horizon; k++) {
            AABB body = bodies.get(k);
            Vec3 eye = writtenEyes.size() == bodies.size() ? writtenEyes.get(k) : eyeOf(body, eyeHeight);
            int top = Mth.floor(body.minY - 1.0E-6) - 1;
            BlockPos cell = null;
            BlockPos support = null;
            Direction face = null;

            for (int x = Mth.floor(body.minX + 1.0E-6); x <= Mth.floor(body.maxX - 1.0E-6); x++) {
               for (int z = Mth.floor(body.minZ + 1.0E-6); z <= Mth.floor(body.maxZ - 1.0E-6); z++) {
                  for (int y = top; y >= top - 3 && (cell == null || y > cell.getY()) && !world.supportAt(x, y, z); y--) {
                     if (world.canPlaceAt(x, y, z) && intersectsBody(bodies, k, x, y, z)) {
                        for (Direction toward : DIRECTIONS) {
                           if (toward != Direction.UP) {
                              int sx = x + toward.getStepX();
                              int sy = y + toward.getStepY();
                              int sz = z + toward.getStepZ();
                              if (world.supportAt(sx, sy, sz)
                                 && (k != 0 || world.init(sx, sy, sz, toward.getOpposite()))
                                 && canReachFace(eye, sx, sy, sz, toward.getOpposite(), reach)) {
                                 cell = new BlockPos(x, y, z);
                                 support = new BlockPos(sx, sy, sz);
                                 face = toward.getOpposite();
                                 break;
                              }
                           }
                        }
                     }
                  }
               }
            }

            if (cell != null) {
               int last = k;
               int later = k + 1;

               while (
                  later <= horizon
                     && intersectsBody(bodies, later, cell.getX(), cell.getY(), cell.getZ())
                     && canReachFace(
                        writtenEyes.size() == bodies.size() ? writtenEyes.get(later) : eyeOf(bodies.get(later), eyeHeight),
                        support.getX(),
                        support.getY(),
                        support.getZ(),
                        face,
                        reach
                     )
               ) {
                  last = later++;
               }

               return new ClutchBlockPlanner.PlacementLink(cell, support, face, k, last);
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static boolean intersectsBody(List<AABB> bodies, int k, int x, int y, int z) {
      AABB cell = new AABB(x, y, z, x + 1, y + 1, z + 1);
      return !cell.intersects(bodies.get(k).deflate(1.0E-4)) && !cell.intersects(bodies.get(k + 1).deflate(1.0E-4));
   }

   private static Vec3 eyeOf(AABB body, double eyeHeight) {
      return new Vec3((body.minX + body.maxX) * 0.5, body.minY + eyeHeight, (body.minZ + body.maxZ) * 0.5);
   }

   static boolean canChainPlace(Vec3 clickEye, Vec3 nextEye, BlockPos support, Direction face, BlockPos placed, double reach) {
      return canChainPlace(clickEye, nextEye, support, face, placed, reach, null);
   }

   static boolean canChainPlace(Vec3 clickEye, Vec3 nextEye, BlockPos support, Direction face, BlockPos placed, double reach, Direction nextFace) {
      return init(clickEye, nextEye, support, face, placed, reach, nextFace, 0.02);
   }

   static boolean init(Vec3 clickEye, Vec3 nextEye, BlockPos support, Direction face, BlockPos placed, double reach, Direction nextFace, double margin) {
      double plane = facePlane(support.getX(), support.getY(), support.getZ(), face);

      for (double a : init_DArr) {
         for (double b : init_DArr) {
            Vec3 var10000;
            switch (face.getAxis()) {
               case X:
                  var10000 = new Vec3(plane, support.getY() + a, support.getZ() + b);
                  break;
               case Y:
                  var10000 = new Vec3(support.getX() + a, plane, support.getZ() + b);
                  break;
               case Z:
                  var10000 = new Vec3(support.getX() + b, support.getY() + a, plane);
                  break;
               default:
                  throw new MatchException(null, null);
            }

            Vec3 point = var10000;
            Vec3 look = point.subtract(clickEye);
            if (!(look.lengthSqr() > reach * reach) && init_Z_Vec3_2(nextEye, look, placed, nextFace, reach)) {
               return true;
            }
         }
      }

      if (nextFace != null) {
         for (Vec3 point : init_List_Vec3(clickEye, nextEye, faceBox(support, face), placed, nextFace, margin)) {
            Vec3 look = point.subtract(clickEye);
            if (look.lengthSqr() <= reach * reach
               && init_Z_Vec3_2(nextEye, look, placed, nextFace, reach)
               && ClutchFallPhysics.matchesPlacementHit(AABB.clip(UNIT_CUBE, clickEye, clickEye.add(look.normalize().scale(reach)), support), support, face)) {
               return true;
            }
         }
      }

      return false;
   }

   static AABB faceBox(BlockPos support, Direction face) {
      double p = facePlane(support.getX(), support.getY(), support.getZ(), face);
      AABB var10000;
      switch (face.getAxis()) {
         case X:
            var10000 = new AABB(p, support.getY(), support.getZ(), p, support.getY() + 1, support.getZ() + 1);
            break;
         case Y:
            var10000 = new AABB(support.getX(), p, support.getZ(), support.getX() + 1, p, support.getZ() + 1);
            break;
         case Z:
            var10000 = new AABB(support.getX(), support.getY(), p, support.getX() + 1, support.getY() + 1, p);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   static List<Vec3> unusedChainAimPoints(Vec3 eye, Vec3 nextEye, AABB rect, BlockPos placed, Direction nextFace) {
      return init_List_Vec3(eye, nextEye, rect, placed, nextFace, 0.02);
   }

   static List<Vec3> init_List_Vec3(Vec3 eye, Vec3 nextEye, AABB rect, BlockPos placed, Direction nextFace, double margin) {
      List<Vec3> polygon = facePolygon(eye, nextEye, rect, placed, nextFace, margin);
      if (polygon.isEmpty()) {
         return List.of();
      }

      Vec3 centre = Vec3.ZERO;

      for (Vec3 point : polygon) {
         centre = centre.add(point);
      }

      centre = centre.scale(1.0 / polygon.size());
      List<Vec3> points = new ArrayList<>(1 + polygon.size() * 3);
      points.add(centre);

      for (int i = 0; i < polygon.size(); i++) {
         Vec3 a = polygon.get(i);
         Vec3 b = polygon.get((i + 1) % polygon.size());
         Vec3 edge = b.subtract(a);
         points.add(a.lerp(centre, 0.02));
         points.add(a.lerp(b, 0.5).lerp(centre, 0.02));
         double t = edge.lengthSqr() < 1.0E-16 ? 0.0 : Math.max(0.0, Math.min(1.0, eye.subtract(a).dot(edge) / edge.lengthSqr()));
         points.add(a.add(edge.scale(t)).lerp(centre, 0.02));
      }

      return points;
   }

   private static List<Vec3> facePolygon(Vec3 eye, Vec3 nextEye, AABB rect, BlockPos placed, Direction nextFace, double margin) {
      if (nextFace == null) {
         return List.of();
      }

      Axis normal = nextFace.getAxis();
      double offset = facePlane(placed.getX(), placed.getY(), placed.getZ(), nextFace) - coord(nextEye, normal);
      double sign = nextFace.getAxisDirection() == AxisDirection.POSITIVE ? -1.0 : 1.0;
      if (offset * sign <= 1.0E-7) {
         return List.of();
      }

      Axis flat = rect.getXsize() < 1.0E-9 ? Axis.X : (rect.getYsize() < 1.0E-9 ? Axis.Y : Axis.Z);
      Axis u = flat == Axis.X ? Axis.Y : Axis.X;
      Axis v = flat == Axis.Z ? Axis.Y : Axis.Z;
      double iu = Math.max(0.0, Math.min(margin, (rect.max(u) - rect.min(u)) * 0.5 - margin));
      double iv = Math.max(0.0, Math.min(margin, (rect.max(v) - rect.min(v)) * 0.5 - margin));
      rect = rect.deflate(u == Axis.X ? iu : (v == Axis.X ? iv : 0.0), u == Axis.Y ? iu : (v == Axis.Y ? iv : 0.0), u == Axis.Z ? iu : (v == Axis.Z ? iv : 0.0));
      List<Vec3> polygon = new ArrayList<>(
         List.of(
            new Vec3(rect.minX, rect.minY, rect.minZ),
            rectCorner(rect, u, true, v, false),
            rectCorner(rect, u, true, v, true),
            rectCorner(rect, u, false, v, true)
         )
      );
      polygon = clipPolygon(polygon, point -> sign * (coord(point, normal) - coord(eye, normal)) - 1.0E-8);
      double distance = Math.abs(offset);
      AABB target = new AABB(placed);

      for (Axis axis : Axis.values()) {
         if (axis != normal && !polygon.isEmpty()) {
            double low = target.min(axis) + 1.0E-5;
            double high = target.max(axis) - 1.0E-5;
            polygon = clipPolygon(
               polygon,
               point -> (coord(nextEye, axis) - low) * sign * (coord(point, normal) - coord(eye, normal)) + distance * (coord(point, axis) - coord(eye, axis))
            );
            polygon = clipPolygon(
               polygon,
               point -> (high - coord(nextEye, axis)) * sign * (coord(point, normal) - coord(eye, normal)) - distance * (coord(point, axis) - coord(eye, axis))
            );
         }
      }

      return polygon;
   }

   static List<ClutchBlockPlanner.AimAngles> init_List_Vec3_2(
      Vec3 eye,
      Vec3 nextEye,
      AABB rect,
      BlockPos support,
      Direction face,
      BlockPos placed,
      Direction nextFace,
      double reach,
      float fromYaw,
      float fromPitch,
      double gcd,
      double margin
   ) {
      if (face.getAxis() != Axis.Y && Double.isFinite(gcd) && !(gcd < 0.009) && !(gcd > 10.0)) {
         List<Vec3> polygon = facePolygon(eye, nextEye, rect, placed, nextFace, margin);
         if (polygon.isEmpty()) {
            return List.of();
         }

         double middle = Math.toDegrees(Math.atan2(-(rect.getCenter().x - eye.x), rect.getCenter().z - eye.z));
         double min = 1.0 / 0.0;
         double max = -1.0 / 0.0;

         for (Vec3 point : polygon) {
            double yaw = middle + Mth.wrapDegrees(Math.toDegrees(Math.atan2(-(point.x - eye.x), point.z - eye.z)) - middle);
            min = Math.min(min, yaw);
            max = Math.max(max, yaw);
         }

         double base = middle + Mth.wrapDegrees(fromYaw - middle);
         int low = (int)Math.ceil((min - base) / gcd) - 1;
         int high = (int)Math.floor((max - base) / gcd) + 1;
         int centre = (int)Math.round(((min + max) * 0.5 - base) / gcd);
         List<ClutchBlockPlanner.AimAngles> result = new ArrayList<>(8);
         List<Double> margins = new ArrayList<>(8);

         for (int step = 0; step <= Math.max(centre - low, high - centre) * 2; step++) {
            int count = centre + (step % 2 == 0 ? step / 2 : -(step + 1) / 2);
            if (count >= low && count <= high) {
               float yaw = fromYaw + (float)(count * gcd);
               Vec3 flat = Vec3.directionFromRotation(0.0F, yaw);
               Axis axis = face.getAxis();
               Axis tangent = axis == Axis.X ? Axis.Z : Axis.X;
               double normal = coord(flat, axis);
               double distance = (rect.min(axis) - coord(eye, axis)) / normal;
               if (Double.isFinite(distance) && !(distance <= 0.0)) {
                  Vec3 crossing = eye.add(flat.scale(distance));
                  double across = coord(crossing, tangent);
                  double bottom = 1.0 / 0.0;
                  double top = -1.0 / 0.0;

                  for (int i = 0; i < polygon.size(); i++) {
                     Vec3 a = polygon.get(i);
                     Vec3 b = polygon.get((i + 1) % polygon.size());
                     double da = coord(a, tangent);
                     double db = coord(b, tangent);
                     if (!(across < Math.min(da, db) - 1.0E-9) && !(across > Math.max(da, db) + 1.0E-9)) {
                        if (Math.abs(db - da) < 1.0E-12) {
                           bottom = Math.min(bottom, Math.min(a.y, b.y));
                           top = Math.max(top, Math.max(a.y, b.y));
                        } else {
                           double y = a.y + (b.y - a.y) * (across - da) / (db - da);
                           bottom = Math.min(bottom, y);
                           top = Math.max(top, y);
                        }
                     }
                  }

                  if (!(bottom > top)) {
                     double horizontal = Math.sqrt(crossing.subtract(eye).horizontalDistanceSqr());
                     int first = (int)Math.ceil((Math.toDegrees(Math.atan2(eye.y - top, horizontal)) - fromPitch) / gcd) - 1;
                     int last = (int)Math.floor((Math.toDegrees(Math.atan2(eye.y - bottom, horizontal)) - fromPitch) / gcd) + 1;
                     int mid = (first + last) / 2;

                     for (int pitchCount : new int[]{mid, mid - 1, mid + 1, first, last}) {
                        float pitch = fromPitch + (float)(pitchCount * gcd);
                        if (!(Math.abs(pitch) > 89.5)) {
                           Vec3 look = Vec3.directionFromRotation(pitch, yaw);
                           BlockHitResult hit = AABB.clip(UNIT_CUBE, eye, eye.add(look.scale(reach)), support);
                           if (ClutchFallPhysics.matchesPlacementHit(hit, support, face)
                              && autismclient.util.BlockPlacementPlanner.isPointInsideFace(rect, face, hit.getLocation(), margin)
                              && init_Z_Vec3_2(nextEye, look, placed, nextFace, reach)) {
                              ClutchBlockPlanner.AimAngles found = new ClutchBlockPlanner.AimAngles(yaw, pitch);
                              if (!result.contains(found)) {
                                 BlockHitResult following = AABB.clip(UNIT_CUBE, nextEye, nextEye.add(look.scale(reach)), placed);
                                 Vec3 point = following.getLocation();
                                 AABB box = new AABB(placed);
                                 double clearance = 1.0 / 0.0;

                                 for (Axis inPlane : Axis.values()) {
                                    if (inPlane != nextFace.getAxis()) {
                                       clearance = Math.min(
                                          clearance, Math.min(coord(point, inPlane) - box.min(inPlane), box.max(inPlane) - coord(point, inPlane))
                                       );
                                    }
                                 }

                                 int rank = 0;

                                 while (rank < margins.size() && margins.get(rank) >= clearance) {
                                    rank++;
                                 }

                                 if (rank < 8) {
                                    result.add(rank, found);
                                    margins.add(rank, clearance);
                                    if (result.size() > 8) {
                                       result.removeLast();
                                       margins.removeLast();
                                    }
                                 }

                                 if (clearance >= 0.02) {
                                    return result;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         return result;
      } else {
         return List.of();
      }
   }

   static List<ClutchBlockPlanner.AimAngles> init_List_Vec3(
      Vec3 approximateEye,
      AABB rect,
      BlockPos support,
      Direction face,
      BlockPos placed,
      Direction nextFace,
      double reach,
      float fromYaw,
      float fromPitch,
      double gcd,
      double margin,
      DoubleFunction<ClutchFallPhysics.AirSweep> eyes
   ) {
      if (face.getAxis() != Axis.Y && nextFace != null && nextFace.getAxis() != Axis.Y && Double.isFinite(gcd) && !(gcd < 0.009) && !(gcd > 10.0)) {
         double middle = Math.toDegrees(Math.atan2(-(rect.getCenter().x - approximateEye.x), rect.getCenter().z - approximateEye.z));
         double min = 1.0 / 0.0;
         double max = -1.0 / 0.0;

         for (double dx : new double[]{-0.05, 0.05}) {
            for (double dz : new double[]{-0.05, 0.05}) {
               for (double x : new double[]{rect.minX, rect.maxX}) {
                  for (double z : new double[]{rect.minZ, rect.maxZ}) {
                     double yaw = middle + Mth.wrapDegrees(Math.toDegrees(Math.atan2(-(x - approximateEye.x - dx), z - approximateEye.z - dz)) - middle);
                     min = Math.min(min, yaw);
                     max = Math.max(max, yaw);
                  }
               }
            }
         }

         double base = middle + Mth.wrapDegrees(fromYaw - middle);
         int low = (int)Math.ceil((min - base) / gcd) - 1;
         int high = (int)Math.floor((max - base) / gcd) + 1;
         int centre = (int)Math.round(((min + max) * 0.5 - base) / gcd);
         List<ClutchBlockPlanner.AimAngles> result = new ArrayList<>(8);
         List<Double> scores = new ArrayList<>(8);
         AABB followingRect = faceBox(placed, nextFace);

         for (int step = 0; step <= Math.max(centre - low, high - centre) * 2; step++) {
            int count = centre + (step % 2 == 0 ? step / 2 : -(step + 1) / 2);
            if (count >= low && count <= high) {
               float yaw = fromYaw + (float)(count * gcd);
               ClutchFallPhysics.AirSweep window = eyes.apply(yaw);
               if (window != null) {
                  Vec3 flat = Vec3.directionFromRotation(0.0F, yaw);
                  double[] here = pitchRangeOnFace(window.startEye(), flat, rect, face, margin);
                  double[] next = pitchRangeOnFace(window.endEye(), flat, followingRect, nextFace, 1.0E-5);
                  if (here != null && next != null) {
                     double bottom = Math.max(here[0], next[0]);
                     double top = Math.min(here[1], next[1]);
                     if (!(bottom > top)) {
                        int first = (int)Math.ceil((bottom - fromPitch) / gcd) - 1;
                        int last = (int)Math.floor((top - fromPitch) / gcd) + 1;
                        int mid = (first + last) / 2;

                        for (int pitchCount : new int[]{mid, mid - 1, mid + 1, first, last}) {
                           float pitch = fromPitch + (float)(pitchCount * gcd);
                           if (!(Math.abs(pitch) > 89.5)) {
                              Vec3 look = Vec3.directionFromRotation(pitch, yaw);
                              BlockHitResult hit = AABB.clip(UNIT_CUBE, window.startEye(), window.startEye().add(look.scale(reach)), support);
                              BlockHitResult following = AABB.clip(UNIT_CUBE, window.endEye(), window.endEye().add(look.scale(reach)), placed);
                              if (ClutchFallPhysics.matchesPlacementHit(hit, support, face)
                                 && autismclient.util.BlockPlacementPlanner.isPointInsideFace(rect, face, hit.getLocation(), margin)
                                 && ClutchFallPhysics.matchesPlacementHit(following, placed, nextFace)) {
                                 ClutchBlockPlanner.AimAngles found = new ClutchBlockPlanner.AimAngles(yaw, pitch);
                                 if (!result.contains(found)) {
                                    Vec3 point = following.getLocation();
                                    AABB box = new AABB(placed);
                                    double clearance = 1.0 / 0.0;

                                    for (Axis inPlane : Axis.values()) {
                                       if (inPlane != nextFace.getAxis()) {
                                          clearance = Math.min(
                                             clearance, Math.min(coord(point, inPlane) - box.min(inPlane), box.max(inPlane) - coord(point, inPlane))
                                          );
                                       }
                                    }

                                    int rank = 0;

                                    while (rank < scores.size() && scores.get(rank) >= clearance) {
                                       rank++;
                                    }

                                    if (rank < 8) {
                                       result.add(rank, found);
                                       scores.add(rank, clearance);
                                       if (result.size() > 8) {
                                          result.removeLast();
                                          scores.removeLast();
                                       }
                                    }

                                    if (clearance >= 0.02) {
                                       return result;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         return result;
      } else {
         return List.of();
      }
   }

   private static double[] pitchRangeOnFace(Vec3 eye, Vec3 flat, AABB rect, Direction face, double margin) {
      Axis axis = face.getAxis();
      Axis tangent = axis == Axis.X ? Axis.Z : Axis.X;
      double plane = rect.min(axis);
      if ((coord(eye, axis) - plane) * (face.getAxisDirection() == AxisDirection.POSITIVE ? 1 : -1) <= 1.0E-7) {
         return null;
      }

      double distance = (plane - coord(eye, axis)) / coord(flat, axis);
      if (Double.isFinite(distance) && !(distance <= 0.0)) {
         Vec3 point = eye.add(flat.scale(distance));
         double across = coord(point, tangent);
         if (!(across < rect.min(tangent) + margin) && !(across > rect.max(tangent) - margin)) {
            double horizontal = Math.sqrt(point.subtract(eye).horizontalDistanceSqr());
            return new double[]{
               Math.toDegrees(Math.atan2(eye.y - rect.maxY + margin, horizontal)), Math.toDegrees(Math.atan2(eye.y - rect.minY - margin, horizontal))
            };
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static Vec3 rectCorner(AABB rect, Axis u, boolean upperU, Axis v, boolean upperV) {
      double x = rect.minX;
      double y = rect.minY;
      double z = rect.minZ;
      if (upperU) {
         switch (u) {
            case X:
               x = rect.maxX;
               break;
            case Y:
               y = rect.maxY;
               break;
            case Z:
               z = rect.maxZ;
         }
      }

      if (upperV) {
         switch (v) {
            case X:
               x = rect.maxX;
               break;
            case Y:
               y = rect.maxY;
               break;
            case Z:
               z = rect.maxZ;
         }
      }

      return new Vec3(x, y, z);
   }

   private static List<Vec3> clipPolygon(List<Vec3> polygon, ToDoubleFunction<Vec3> halfPlane) {
      if (polygon.isEmpty()) {
         return polygon;
      }

      List<Vec3> clipped = new ArrayList<>(polygon.size() + 1);
      Vec3 before = polygon.getLast();
      double a = halfPlane.applyAsDouble(before);

      for (Vec3 point : polygon) {
         double b = halfPlane.applyAsDouble(point);
         if (a >= 0.0 != b >= 0.0) {
            clipped.add(before.lerp(point, a / (a - b)));
         }

         if (b >= 0.0) {
            clipped.add(point);
         }

         before = point;
         a = b;
      }

      return clipped;
   }

   private static double coord(Vec3 point, Axis axis) {
      double var10000;
      switch (axis) {
         case X:
            var10000 = point.x;
            break;
         case Y:
            var10000 = point.y;
            break;
         case Z:
            var10000 = point.z;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   static boolean init_Z_Vec3_2(Vec3 eye, Vec3 look, BlockPos cell, Direction face, double reach) {
      Vec3 end = eye.add(look.normalize().scale(reach));
      return face == null ? new AABB(cell).deflate(0.05).clip(eye, end).isPresent() : ClutchFallPhysics.matchesPlacementHit(AABB.clip(UNIT_CUBE, eye, end, cell), cell, face);
   }

   private static int pathLength(long key, Long2LongOpenHashMap parent) {
      int length = 1;

      for (long at = parent.get(key); at != -9223372036854775808L; at = parent.get(at)) {
         length++;
      }

      return length;
   }

   static boolean canReachFace(Vec3 eye, int sx, int sy, int sz, Direction face, double reach) {
      return canReachFace(eye, sx, sy, sz, face, reach, 0.3);
   }

   private static boolean canReachFace(Vec3 eye, int sx, int sy, int sz, Direction face, double reach, double inset) {
      return facesEye(eye, sx, sy, sz, face) && eye.distanceToSqr(closestFacePoint(eye, sx, sy, sz, face, inset)) <= reach * reach;
   }

   private static boolean facesEye(Vec3 eye, int sx, int sy, int sz, Direction face) {
      double plane = facePlane(sx, sy, sz, face);
      double var10000;
      switch (face.getAxis()) {
         case X:
            var10000 = eye.x;
            break;
         case Y:
            var10000 = eye.y;
            break;
         case Z:
            var10000 = eye.z;
            break;
         default:
            throw new MatchException(null, null);
      }

      double eyeCoord = var10000;
      double past = face.getAxisDirection() == AxisDirection.POSITIVE ? eyeCoord - plane : plane - eyeCoord;
      return past >= 0.02;
   }

   static Vec3 init_Vec3_Vec3(Vec3 eye, int sx, int sy, int sz, Direction face) {
      return closestFacePoint(eye, sx, sy, sz, face, 0.3);
   }

   static Vec3 closestFacePoint(Vec3 eye, int sx, int sy, int sz, Direction face, double inset) {
      double plane = facePlane(sx, sy, sz, face);
      double px = face.getAxis() == Axis.X ? plane : Mth.clamp(eye.x, sx + inset, sx + 1 - inset);
      double py = face.getAxis() == Axis.Y ? plane : Mth.clamp(eye.y, sy + inset, sy + 1 - inset);
      double pz = face.getAxis() == Axis.Z ? plane : Mth.clamp(eye.z, sz + inset, sz + 1 - inset);
      return new Vec3(px, py, pz);
   }

   private static double facePlane(int sx, int sy, int sz, Direction face) {
      double var10000;
      switch (face) {
         case EAST:
            var10000 = sx + 1;
            break;
         case WEST:
            var10000 = sx;
            break;
         case UP:
            var10000 = sy + 1;
            break;
         case DOWN:
            var10000 = sy;
            break;
         case SOUTH:
            var10000 = sz + 1;
            break;
         case NORTH:
            var10000 = sz;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private record PlanChooser(
      ClutchBlockPlanner.FallFlight flight,
      double health,
      BlockPos keep,
      int homeRow,
      ToDoubleFunction<ClutchBlockPlanner.PlacementLink> firstCost,
      int blocks,
      DoubleUnaryOperator damage,
      double maxCatchDamage
   ) {
      int survivableHorizon(boolean fatal) {
         int last = 0;
         double limit = fatal ? this.health * 1.5 : this.health;

         for (Entry entry : this.flight.landingDue.long2IntEntrySet()) {
            double hurt = this.damage.applyAsDouble(this.flight.landingFall.get(entry.getLongKey()));
            if (hurt < limit && hurt <= this.maxCatchDamage) {
               last = Math.max(last, entry.getIntValue());
            }
         }

         return last;
      }

      ClutchBlockPlanner.ClutchPlan bestPlan(ClutchBlockPlanner.LinkSearch search, boolean fatal, boolean reachOut) {
         LongArrayList goals = new LongArrayList();
         Long2DoubleOpenHashMap cheap = new Long2DoubleOpenHashMap();

         for (Entry entry : this.flight.landingDue.long2IntEntrySet()) {
            long key = entry.getLongKey();
            int due = entry.getIntValue();
            int placed = search.earliestTick().get(key);
            if (placed >= 0 && placed <= due - 1) {
               int length = ClutchBlockPlanner.pathLength(key, search.parent());
               if (length <= this.blocks) {
                  double hurt = this.damage.applyAsDouble(this.flight.landingFall.get(key));
                  if (!(hurt >= (fatal ? this.health * 1.5 : this.health)) && !(hurt > this.maxCatchDamage)) {
                     int row = BlockPos.getY(key);
                     double score = hurt * 100.0 + length * 1.5 + Math.max(0, row - this.homeRow) * 2 + Math.max(0, this.homeRow - row) + due * 0.3;
                     if (this.keep != null && this.keep.asLong() == key) {
                        score -= 0.75;
                     }

                     cheap.put(key, score);
                     goals.add(key);
                  }
               }
            }
         }

         if (goals.isEmpty()) {
            return null;
         }

         goals.sort((a, b) -> Double.compare(cheap.get(a), cheap.get(b)));
         ClutchBlockPlanner.ClutchPlan chosen = null;
         double chosenScore = 1.0 / 0.0;

         for (int g = 0; g < Math.min(goals.size(), this.firstCost == null ? 1 : 6); g++) {
            long goal = goals.getLong(g);
            ClutchBlockPlanner.ClutchPlan plan = this.planToGoal(
               goal, this.flight.landingDue.get(goal), this.flight.landingFall.get(goal), search, reachOut
            );
            double score = cheap.get(goal) + (this.firstCost == null ? 0.0 : this.firstCost.applyAsDouble(plan.firstLink()));
            if (score < chosenScore) {
               chosen = plan;
               chosenScore = score;
            }
         }

         return chosen;
      }

      private ClutchBlockPlanner.ClutchPlan planToGoal(long goal, int landingTick, double fall, ClutchBlockPlanner.LinkSearch search, boolean reachOut) {
         Vec3[] eyes = this.flight.eyes;
         double reach = search.reach();
         LongArrayList chain = new LongArrayList();

         for (long at = goal; at != -9223372036854775808L; at = search.parent().get(at)) {
            chain.add(0, at);
         }

         int[] latest = new int[chain.size()];
         int next = landingTick;

         for (int i = chain.size() - 1; i >= 0; i--) {
            long key = chain.getLong(i);
            BlockPos cell = BlockPos.of(key);
            Direction face = ClutchBlockPlanner.DIRECTIONS[search.parentFace().get(key)];
            BlockPos support = cell.relative(face.getOpposite());
            int earliest = search.earliestTick().get(key);
            int last = earliest;
            boolean landing = i == chain.size() - 1;
            Direction nextFace = landing ? null : autismclient.modules.ClutchBlockPlanner.DIRECTIONS[search.parentFace().get(chain.getLong(i + 1))];

            for (int k = next - 1; k > earliest; k--) {
               if (this.canPlaceAt(key, support, face, k, landing ? landingTick : this.flight.bodies.size(), reach)
                  && (
                     this.flight.immediate
                        || landing
                        || k < next - 1
                        || autismclient.modules.ClutchBlockPlanner.init(eyes[k], eyes[next], support, face, cell, reach, nextFace, this.flight.margin)
                  )) {
                  last = k;
                  break;
               }
            }

            latest[i] = last;
            next = last;
         }

         List<ClutchBlockPlanner.PlacementLink> links = new ArrayList<>(chain.size());

         for (int i = 0; i < chain.size(); i++) {
            long key = chain.getLong(i);
            BlockPos cell = BlockPos.of(key);
            Direction face = autismclient.modules.ClutchBlockPlanner.DIRECTIONS[search.parentFace().get(key)];
            links.add(new ClutchBlockPlanner.PlacementLink(cell, cell.relative(face.getOpposite()), face, search.earliestTick().get(key), latest[i]));
         }

         return new ClutchBlockPlanner.ClutchPlan(List.copyOf(links), BlockPos.of(goal), landingTick, fall, reachOut);
      }

      private boolean canPlaceAt(long key, BlockPos support, Direction face, int k, int clearUntil, double reach) {
         if (new AABB(BlockPos.of(key)).intersects(this.flight.bodies.get(k).deflate(1.0E-4))) {
            return false;
         } else {
            return !this.flight.contacts.init(key, k, clearUntil)
               ? false
               : autismclient.modules.ClutchBlockPlanner.canReachFace(this.flight.eyes[k], support.getX(), support.getY(), support.getZ(), face, reach);
         }
      }
   }

   private static final class CellCache {
      private final ClutchBlockPlanner.PlacementWorld world;
      private final Long2ByteOpenHashMap cache = new Long2ByteOpenHashMap();
      private final Long2ByteOpenHashMap supportCache = new Long2ByteOpenHashMap();
      private final Long2ByteOpenHashMap adjacentSupportCache = new Long2ByteOpenHashMap();

      CellCache(ClutchBlockPlanner.PlacementWorld world) {
         this.world = world;
      }

      boolean isReplaceable(long key, int x, int y, int z) {
         byte known = this.cache.get(key);
         if (known == 0) {
            this.cache.put(key, known = (byte)(this.world.canPlaceAt(x, y, z) ? 1 : 2));
         }

         return known == 1;
      }

      boolean isSupport(long key, int x, int y, int z) {
         byte known = this.supportCache.get(key);
         if (known == 0) {
            this.supportCache.put(key, known = (byte)(this.world.supportAt(x, y, z) ? 1 : 2));
         }

         return known == 1;
      }

      boolean hasAdjacentSupport(long key, int x, int y, int z) {
         byte known = this.adjacentSupportCache.get(key);
         if (known == 0) {
            boolean any = false;

            for (Direction direction : autismclient.modules.ClutchBlockPlanner.DIRECTIONS) {
               int sx = x + direction.getStepX();
               int sy = y + direction.getStepY();
               int sz = z + direction.getStepZ();
               if (this.isSupport(BlockPos.asLong(sx, sy, sz), sx, sy, sz)) {
                  any = true;
                  break;
               }
            }

            this.adjacentSupportCache.put(key, known = (byte)(any ? 1 : 2));
         }

         return known == 1;
      }
   }

   private static final class BodyContacts {
      private final List<AABB> bodies;
      private final AABB bounds;
      private final Long2ObjectOpenHashMap<int[]> contactsByCell = new Long2ObjectOpenHashMap<>();

      BodyContacts(List<AABB> bodies) {
         this.bodies = bodies;
         AABB all = bodies.getFirst();

         for (AABB body : bodies) {
            all = all.minmax(body);
         }

         this.bounds = all;
      }

      boolean init(long key, int from, int until) {
         for (int value : this.contactsFor(key)) {
            int t = value >> 1;
            if (t > from) {
               return t >= until || (value & 1) == 1;
            }
         }

         return true;
      }

      int init(long key, int from) {
         for (int value : this.contactsFor(key)) {
            int t = value >> 1;
            if (t > from) {
               return (value & 1) == 1 ? -1 : t;
            }
         }

         return -1;
      }

      private int[] contactsFor(long key) {
         int[] met = this.contactsByCell.get(key);
         if (met == null) {
            this.contactsByCell.put(key, met = this.computeContacts(key));
         }

         return met;
      }

      private int[] computeContacts(long key) {
         AABB shrunk = new AABB(BlockPos.of(key)).deflate(0.001);
         if (!shrunk.intersects(this.bounds)) {
            return new int[0];
         }

         int[] found = new int[this.bodies.size()];
         int count = 0;

         for (int t = 1; t < this.bodies.size(); t++) {
            if (shrunk.intersects(this.bodies.get(t))) {
               AABB before = this.bodies.get(t - 1);
               boolean onTop = before.minY >= shrunk.maxY + 0.001 - 1.0E-6
                  && before.maxX > shrunk.minX
                  && before.minX < shrunk.maxX
                  && before.maxZ > shrunk.minZ
                  && before.minZ < shrunk.maxZ;
               found[count++] = t << 1 | (onTop ? 1 : 0);
            }
         }

         return Arrays.copyOf(found, count);
      }
   }

   interface PlacementWorld {
      boolean supportAt(int var1, int var2, int var3);

      boolean canPlaceAt(int var1, int var2, int var3);

      default boolean write(int x, int y, int z) {
         return false;
      }

      default boolean init(int x, int y, int z, Direction face) {
         return this.write(x, y, z);
      }

      default boolean allowedStart(int x, int y, int z) {
         return true;
      }

      default boolean init(Vec3 eye, double distance) {
         return false;
      }
   }

   record ClutchPlan(List<ClutchBlockPlanner.PlacementLink> init_List, BlockPos init_BlockPos, int init_I, double init_D, boolean init_Z, boolean load) {
      ClutchPlan(List<ClutchBlockPlanner.PlacementLink> links, BlockPos landing, int landingTick, double fall, boolean reachOut) {
         this(links, landing, landingTick, fall, reachOut, false);
      }

      ClutchBlockPlanner.PlacementLink firstLink() {
         return this.init_List.getFirst();
      }

      boolean init_Z_2() {
         return this.init_Z && Double.isNaN(this.init_D);
      }

      public boolean load() {
         return this.init_Z;
      }

      public boolean read() {
         return this.load;
      }
   }

   record PlacementLink(BlockPos init_BlockPos, BlockPos load_BlockPos, Direction init_Direction, int init_I, int load_I) {
   }

   private record LinkSearch(
      Long2IntOpenHashMap earliestTick,
      Long2LongOpenHashMap parent,
      Long2ByteOpenHashMap parentFace,
      LongOpenHashSet linkable,
      double reach
   ) {
   }

   record AimAngles(float init, float load) {
   }

   private static final class FallFlight {
      final List<AABB> bodies;
      final Vec3[] eyes;
      final ClutchBlockPlanner.PlacementWorld world;
      double margin = 0.02;
      boolean immediate;
      final ClutchBlockPlanner.CellCache cells;
      final ClutchBlockPlanner.BodyContacts contacts;
      final Long2IntOpenHashMap landingDue = new Long2IntOpenHashMap();
      final Long2DoubleOpenHashMap landingFall = new Long2DoubleOpenHashMap();

      FallFlight(List<AABB> bodies, double eyeHeight, ClutchBlockPlanner.PlacementWorld world, double fallSoFar) {
         this(bodies, eyeHeight, world, fallSoFar, List.of());
      }

      FallFlight(List<AABB> bodies, double eyeHeight, ClutchBlockPlanner.PlacementWorld world, double fallSoFar, List<Vec3> writtenEyes) {
         this(bodies, eyeHeight, world, fallSoFar, writtenEyes, 0.06);
      }

      FallFlight(List<AABB> bodies, double eyeHeight, ClutchBlockPlanner.PlacementWorld world, double fallSoFar, List<Vec3> writtenEyes, double landingInset) {
         this.bodies = bodies;
         this.world = world;
         this.cells = new ClutchBlockPlanner.CellCache(world);
         this.contacts = new ClutchBlockPlanner.BodyContacts(bodies);
         int horizon = bodies.size() - 1;
         this.eyes = new Vec3[bodies.size()];

         for (int t = 0; t <= horizon; t++) {
            AABB body = bodies.get(t);
            this.eyes[t] = writtenEyes.size() == bodies.size()
               ? writtenEyes.get(t)
               : new Vec3((body.minX + body.maxX) * 0.5, body.minY + eyeHeight, (body.minZ + body.maxZ) * 0.5);
         }

         this.landingDue.defaultReturnValue(-1);
         double highest = bodies.getFirst().minY;

         for (int t = 1; t <= horizon; t++) {
            AABB a = bodies.get(t - 1);
            AABB b = bodies.get(t);
            highest = Math.max(highest, a.minY);
            if (!(b.minY >= a.minY - 1.0E-9)) {
               for (int level = Mth.floor(a.minY); level > b.minY; level--) {
                  if (!(level > a.minY + 1.0E-9)) {
                     double minX = a.minX + landingInset;
                     double maxX = a.maxX - landingInset;
                     double minZ = a.minZ + landingInset;
                     double maxZ = a.maxZ - landingInset;
                     int y = level - 1;

                     for (int x = Mth.floor(minX); x <= Mth.floor(maxX); x++) {
                        for (int z = Mth.floor(minZ); z <= Mth.floor(maxZ); z++) {
                           long key = BlockPos.asLong(x, y, z);
                           if (!this.landingDue.containsKey(key) && this.cells.isReplaceable(key, x, y, z)) {
                              this.landingDue.put(key, t);
                              this.landingFall.put(key, fallSoFar + Math.max(0.0, highest - level));
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      ClutchBlockPlanner.LinkSearch search(int firstTick, double reach, boolean carried, int lastTick) {
         int horizon = Math.min(this.bodies.size() - 1, lastTick);
         Long2IntOpenHashMap best = new Long2IntOpenHashMap();
         best.defaultReturnValue(-1);
         Long2LongOpenHashMap parent = new Long2LongOpenHashMap();
         parent.defaultReturnValue(-9223372036854775808L);
         Long2ByteOpenHashMap parentFace = new Long2ByteOpenHashMap();
         LongOpenHashSet linkable = new LongOpenHashSet();
         LongOpenHashSet besideLink = new LongOpenHashSet();
         LongArrayList placedNow = new LongArrayList();
         int[] carriedFrom = new int[autismclient.modules.ClutchBlockPlanner.DIRECTIONS.length];
         int radius = Mth.ceil(reach) + 1;
         double cellReach = (reach + 1.0) * (reach + 1.0);

         for (int k = Math.max(0, firstTick); k < horizon; k++) {
            Vec3 eye = this.eyes[k];
            AABB body = this.bodies.get(k).deflate(1.0E-4);
            int ex = Mth.floor(eye.x);
            int ey = Mth.floor(eye.y);
            int ez = Mth.floor(eye.z);
            if (!autismclient.modules.ClutchBlockPlanner.nothingPlaceableNear(this.world, eye, cellReach, linkable)) {
               placedNow.clear();

               for (int x = ex - radius; x <= ex + radius; x++) {
                  for (int y = ey - radius; y <= ey + radius; y++) {
                     for (int z = ez - radius; z <= ez + radius; z++) {
                        if (!(eye.distanceToSqr(x + 0.5, y + 0.5, z + 0.5) > cellReach)) {
                           long key = BlockPos.asLong(x, y, z);
                           if (!best.containsKey(key)
                              && this.cells.isReplaceable(key, x, y, z)
                              && (
                                 !(body.minX < x + 1)
                                    || !(body.maxX > x)
                                    || !(body.minY < y + 1)
                                    || !(body.maxY > y)
                                    || !(body.minZ < z + 1)
                                    || !(body.maxZ > z)
                              )
                              && (besideLink.contains(key) || this.cells.hasAdjacentSupport(key, x, y, z))) {
                              int due = this.landingDue.get(key);
                              boolean landing = due > k && this.contacts.init(key, k, due);
                              boolean link = this.contacts.init(key, k, this.bodies.size());
                              if (landing || link) {
                                 int chosen = -1;
                                 int carriedCount = 0;
                                 long support = -9223372036854775808L;

                                 for (int d = 0; d < autismclient.modules.ClutchBlockPlanner.DIRECTIONS.length; d++) {
                                    Direction direction = autismclient.modules.ClutchBlockPlanner.DIRECTIONS[d];
                                    int sx = x + direction.getStepX();
                                    int sy = y + direction.getStepY();
                                    int sz = z + direction.getStepZ();
                                    long at = BlockPos.asLong(sx, sy, sz);
                                    int placed = best.get(at);
                                    boolean existing = placed < 0 && this.cells.isSupport(at, sx, sy, sz);
                                    if ((existing || placed >= 0 && placed < k && linkable.contains(at))
                                       && (k != 0 || existing && (this.immediate || this.world.init(sx, sy, sz, direction.getOpposite())))) {
                                       Direction face = direction.getOpposite();
                                       if ((!existing || this.world.allowedStart(x, y, z))
                                          && autismclient.modules.ClutchBlockPlanner.canReachFace(eye, sx, sy, sz, face, reach, carried ? 0.3 : 0.03)) {
                                          if (!carried || existing || placed != k - 1) {
                                             chosen = face.ordinal();
                                             support = existing ? -9223372036854775808L : at;
                                             break;
                                          }

                                          carriedFrom[carriedCount++] = d;
                                       }
                                    }
                                 }

                                 for (int i = 0; chosen < 0 && i < carriedCount; i++) {
                                    Direction direction = autismclient.modules.ClutchBlockPlanner.DIRECTIONS[carriedFrom[i]];
                                    BlockPos placedAt = new BlockPos(x + direction.getStepX(), y + direction.getStepY(), z + direction.getStepZ());
                                    Direction under = autismclient.modules.ClutchBlockPlanner.DIRECTIONS[parentFace.get(placedAt.asLong())];
                                    if (autismclient.modules.ClutchBlockPlanner.init(
                                       this.eyes[k - 1],
                                       eye,
                                       placedAt.relative(under.getOpposite()),
                                       under,
                                       placedAt,
                                       reach,
                                       direction.getOpposite(),
                                       this.margin
                                    )) {
                                       chosen = direction.getOpposite().ordinal();
                                       support = placedAt.asLong();
                                    }
                                 }

                                 if (chosen >= 0) {
                                    placedNow.add(key);
                                    placedNow.add(support);
                                    placedNow.add(chosen | (link ? 8 : 0));
                                 }
                              }
                           }
                        }
                     }
                  }
               }

               for (int i = 0; i < placedNow.size(); i += 3) {
                  long key = placedNow.getLong(i);
                  long support = placedNow.getLong(i + 1);
                  int packed = (int)placedNow.getLong(i + 2);
                  best.put(key, k);
                  if (support != -9223372036854775808L) {
                     parent.put(key, support);
                  }

                  parentFace.put(key, (byte)(packed & 7));
                  if ((packed & 8) != 0) {
                     linkable.add(key);

                     for (Direction direction : autismclient.modules.ClutchBlockPlanner.DIRECTIONS) {
                        besideLink.add(BlockPos.offset(key, direction));
                     }
                  }
               }
            }
         }

         return new ClutchBlockPlanner.LinkSearch(best, parent, parentFace, linkable, reach);
      }

      ClutchBlockPlanner.ClutchPlan nearestPlan(int firstTick, double reach) {
         return this.nearestPlanWithin(firstTick, reach, this.bodies.size());
      }

      ClutchBlockPlanner.ClutchPlan nearestPlanWithin(int firstTick, double reach, int nearbyTicks) {
         ClutchBlockPlanner.PlacementLink chosen = null;
         double chosenScore = 1.0 / 0.0;
         int radius = Mth.ceil(reach) + 1;
         double cellReach = (reach + 1.0) * (reach + 1.0);
         int last = Math.min(this.bodies.size(), Math.max(0, firstTick) + 3 + 1);

         for (int k = Math.max(0, firstTick); k < last; k++) {
            Vec3 eye = this.eyes[k];
            AABB body = this.bodies.get(k).deflate(1.0E-4);
            int ex = Mth.floor(eye.x);
            int ey = Mth.floor(eye.y);
            int ez = Mth.floor(eye.z);
            if (!autismclient.modules.ClutchBlockPlanner.nothingPlaceableNear(this.world, eye, cellReach, null)) {
               for (int x = ex - radius; x <= ex + radius; x++) {
                  for (int y = ey - radius; y <= ey + radius; y++) {
                     for (int z = ez - radius; z <= ez + radius; z++) {
                        if (!(eye.distanceToSqr(x + 0.5, y + 0.5, z + 0.5) > (reach + 1.0) * (reach + 1.0))) {
                           long key = BlockPos.asLong(x, y, z);
                           if (this.world.allowedStart(x, y, z)
                              && this.cells.isReplaceable(key, x, y, z)
                              && this.cells.hasAdjacentSupport(key, x, y, z)
                              && !new AABB(x, y, z, x + 1, y + 1, z + 1).intersects(body)) {
                              Direction face = null;
                              BlockPos support = null;

                              for (Direction direction : autismclient.modules.ClutchBlockPlanner.DIRECTIONS) {
                                 int sx = x + direction.getStepX();
                                 int sy = y + direction.getStepY();
                                 int sz = z + direction.getStepZ();
                                 if (this.cells.isSupport(BlockPos.asLong(sx, sy, sz), sx, sy, sz)
                                    && (k != 0 || this.world.init(sx, sy, sz, direction.getOpposite()))) {
                                    Direction outward = direction.getOpposite();
                                    if (autismclient.modules.ClutchBlockPlanner.canReachFace(eye, sx, sy, sz, outward, reach, 0.03)) {
                                       face = outward;
                                       support = new BlockPos(sx, sy, sz);
                                       break;
                                    }
                                 }
                              }

                              if (face != null) {
                                 double nearest = 1.0 / 0.0;

                                 for (int t = k; t < nearbyTicks; t++) {
                                    AABB future = this.bodies.get(t);
                                    double dx = (future.minX + future.maxX) * 0.5 - x - 0.5;
                                    double dz = (future.minZ + future.maxZ) * 0.5 - z - 0.5;
                                    double dy = Math.max(0.0, future.minY - y - 1.0) * 0.5 + Math.max(0.0, y + 1 - future.minY);
                                    nearest = Math.min(nearest, dx * dx + dz * dz + dy * dy);
                                 }

                                 double score = nearest + k * 0.25;
                                 if (score < chosenScore) {
                                    chosenScore = score;
                                    chosen = new ClutchBlockPlanner.PlacementLink(new BlockPos(x, y, z), support, face, k, k);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         return chosen == null ? null : new ClutchBlockPlanner.ClutchPlan(List.of(chosen), chosen.init_BlockPos(), this.bodies.size() - 1, 0.0 / 0.0, true);
      }
   }
}
