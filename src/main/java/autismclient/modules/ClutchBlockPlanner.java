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
   private static final Direction[] init_DirectionArr = Direction.values();
   private static final double load_D = 0.3;
   private static final double read_D = 0.03;
   private static final int load_I = 6;
   static final double[] init_DArr = new double[]{0.5, 0.15, 0.85, 0.92, 0.03, 0.97};
   static final double init_D = 0.02;
   private static final long init_J = -9223372036854775808L;
   private static final int read_I = 3;
   private static final double write_D = 1.5;
   private static final double update = 0.06;
   private static final int write_I = 12;
   static final int init_I = 4;
   private static final List<AABB> init_List = List.of(new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));

   private static boolean init_Z_k_DataUtil(ClutchBlockPlanner.Inner_DataUtil world, Vec3 eye, double cellReach, LongOpenHashSet linkable) {
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

   static ClutchBlockPlanner.Inner_JsonUtil init(
      List<AABB> bodies, double eyeHeight, double reach, ClutchBlockPlanner.Inner_DataUtil world, int firstTick, double fallSoFar, double health, BlockPos keep
   ) {
      return bodies != null && !bodies.isEmpty()
         ? init(bodies, eyeHeight, reach, world, firstTick, fallSoFar, health, keep, Mth.floor(bodies.getFirst().minY + 1.0E-6) - 1, null)
         : null;
   }

   static ClutchBlockPlanner.Inner_JsonUtil init(
      List<AABB> bodies,
      double eyeHeight,
      double reach,
      ClutchBlockPlanner.Inner_DataUtil world,
      int firstTick,
      double fallSoFar,
      double health,
      BlockPos keep,
      int homeRow,
      ToDoubleFunction<ClutchBlockPlanner.Inner_NetHelper> firstCost
   ) {
      return init(bodies, eyeHeight, reach, world, firstTick, fallSoFar, health, keep, homeRow, firstCost, 2147483647);
   }

   static ClutchBlockPlanner.Inner_JsonUtil init(
      List<AABB> bodies,
      double eyeHeight,
      double reach,
      ClutchBlockPlanner.Inner_DataUtil world,
      int firstTick,
      double fallSoFar,
      double health,
      BlockPos keep,
      int homeRow,
      ToDoubleFunction<ClutchBlockPlanner.Inner_NetHelper> firstCost,
      int blocks
   ) {
      return init(bodies, eyeHeight, reach, world, firstTick, fallSoFar, health, keep, homeRow, firstCost, blocks, ClutchFallPhysics::init_D_D, 0.0, 1.0 / 0.0);
   }

   static ClutchBlockPlanner.Inner_JsonUtil init(
      List<AABB> bodies,
      double eyeHeight,
      double reach,
      ClutchBlockPlanner.Inner_DataUtil world,
      int firstTick,
      double fallSoFar,
      double health,
      BlockPos keep,
      int homeRow,
      ToDoubleFunction<ClutchBlockPlanner.Inner_NetHelper> firstCost,
      int blocks,
      DoubleUnaryOperator damage,
      double bestEffortReach
   ) {
      return init(bodies, eyeHeight, reach, world, firstTick, fallSoFar, health, keep, homeRow, firstCost, blocks, damage, bestEffortReach, 1.0 / 0.0);
   }

   static ClutchBlockPlanner.Inner_JsonUtil init(
      List<AABB> bodies,
      double eyeHeight,
      double reach,
      ClutchBlockPlanner.Inner_DataUtil world,
      int firstTick,
      double fallSoFar,
      double health,
      BlockPos keep,
      int homeRow,
      ToDoubleFunction<ClutchBlockPlanner.Inner_NetHelper> firstCost,
      int blocks,
      DoubleUnaryOperator damage,
      double bestEffortReach,
      double uncaught
   ) {
      return init(bodies, eyeHeight, reach, world, firstTick, fallSoFar, health, keep, homeRow, firstCost, blocks, damage, bestEffortReach, uncaught, List.of());
   }

   static ClutchBlockPlanner.Inner_JsonUtil init(
      List<AABB> bodies,
      double eyeHeight,
      double reach,
      ClutchBlockPlanner.Inner_DataUtil world,
      int firstTick,
      double fallSoFar,
      double health,
      BlockPos keep,
      int homeRow,
      ToDoubleFunction<ClutchBlockPlanner.Inner_NetHelper> firstCost,
      int blocks,
      DoubleUnaryOperator damage,
      double bestEffortReach,
      double uncaught,
      List<Vec3> writtenEyes
   ) {
      return init(
         bodies, eyeHeight, reach, world, firstTick, fallSoFar, health, keep, homeRow, firstCost, blocks, damage, bestEffortReach, uncaught, writtenEyes, false
      );
   }

   static ClutchBlockPlanner.Inner_JsonUtil init(
      List<AABB> bodies,
      double eyeHeight,
      double reach,
      ClutchBlockPlanner.Inner_DataUtil world,
      int firstTick,
      double fallSoFar,
      double health,
      BlockPos keep,
      int homeRow,
      ToDoubleFunction<ClutchBlockPlanner.Inner_NetHelper> firstCost,
      int blocks,
      DoubleUnaryOperator damage,
      double bestEffortReach,
      double uncaught,
      List<Vec3> writtenEyes,
      boolean marginal
   ) {
      return init(
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

   static ClutchBlockPlanner.Inner_JsonUtil init(
      List<AABB> bodies,
      double eyeHeight,
      double reach,
      ClutchBlockPlanner.Inner_DataUtil world,
      int firstTick,
      double fallSoFar,
      double health,
      BlockPos keep,
      int homeRow,
      ToDoubleFunction<ClutchBlockPlanner.Inner_NetHelper> firstCost,
      int blocks,
      DoubleUnaryOperator damage,
      double uncaught,
      List<Vec3> writtenEyes,
      boolean marginal
   ) {
      ClutchBlockPlanner.Inner_JsonUtil result = init(
         bodies, eyeHeight, reach, world, firstTick, fallSoFar, health, keep, homeRow, firstCost, blocks, damage, 0.0, uncaught, writtenEyes, marginal, true
      );
      return result == null ? null : new ClutchBlockPlanner.Inner_JsonUtil(result.init_List(), result.init_BlockPos(), result.init_I(), result.init_D(), result.load(), true);
   }

   private static ClutchBlockPlanner.Inner_JsonUtil init(
      List<AABB> bodies,
      double eyeHeight,
      double reach,
      ClutchBlockPlanner.Inner_DataUtil world,
      int firstTick,
      double fallSoFar,
      double health,
      BlockPos keep,
      int homeRow,
      ToDoubleFunction<ClutchBlockPlanner.Inner_NetHelper> firstCost,
      int blocks,
      DoubleUnaryOperator damage,
      double bestEffortReach,
      double uncaught,
      List<Vec3> writtenEyes,
      boolean marginal,
      boolean immediate
   ) {
      if (bodies != null && bodies.size() >= 2 && world != null && blocks > 0) {
         ClutchBlockPlanner.Inner_TaskQueue flight = new ClutchBlockPlanner.Inner_TaskQueue(
            bodies, eyeHeight, world, fallSoFar, writtenEyes, !(bestEffortReach > 0.0) && !marginal ? 0.06 : 1.0E-6
         );
         flight.init_D = marginal ? 1.0E-5 : 0.02;
         flight.init_Z = immediate;
         if (flight.init_Long2IntOpenHashMa.isEmpty()) {
            return bestEffortReach > 0.0 ? flight.init(firstTick, bestEffortReach) : null;
         }

         ClutchBlockPlanner.Inner_BufferHelper choice = new ClutchBlockPlanner.Inner_BufferHelper(flight, health, keep, homeRow, firstCost, blocks, damage, Math.max(0.0, uncaught - 2.0));
         int horizon = choice.init(bestEffortReach > 0.0);
         if (bestEffortReach > 0.0 && uncaught >= health) {
            horizon = Math.max(horizon, Math.max(1, firstTick) + 3 + 1);
         }

         if (horizon <= Math.max(0, firstTick)) {
            return bestEffortReach > 0.0 ? flight.init(firstTick, bestEffortReach) : null;
         }

         ClutchBlockPlanner.Inner_StateCache strict = flight.init(firstTick, reach, !immediate, horizon);
         ClutchBlockPlanner.Inner_JsonUtil plan = choice.init(strict, false, false);
         if (plan == null && !(bestEffortReach <= 0.0)) {
            ClutchBlockPlanner.Inner_StateCache relaxed = flight.init(firstTick, Math.max(reach, bestEffortReach), false, horizon);
            plan = choice.init(relaxed, false, true);
            if (plan == null) {
               plan = choice.init(strict, true, false);
            }

            if (plan == null) {
               plan = choice.init(relaxed, true, true);
            }

            if (plan == null) {
               plan = flight.init(firstTick, Math.max(reach, bestEffortReach));
            }

            return plan;
         } else {
            return plan;
         }
      } else {
         return null;
      }
   }

   static ClutchBlockPlanner.Inner_JsonUtil init_k_JsonUtil_List(
      List<AABB> bodies, double eyeHeight, double reach, ClutchBlockPlanner.Inner_DataUtil world, int firstTick, List<Vec3> writtenEyes
   ) {
      return bodies != null && bodies.size() >= 2 && world != null
         ? new ClutchBlockPlanner.Inner_TaskQueue(bodies, eyeHeight, world, 0.0, writtenEyes, 1.0E-6).init(firstTick, reach, Math.min(bodies.size(), 8))
         : null;
   }

   static ClutchBlockPlanner.Inner_JsonUtil load(List<AABB> bodies, double eyeHeight, double reach, final ClutchBlockPlanner.Inner_DataUtil world, int firstTick, List<Vec3> eyes) {
      ClutchBlockPlanner.Inner_NetHelper tower = init_k_NetHelper_List(bodies, eyeHeight, reach, world, firstTick, eyes);
      ClutchBlockPlanner.Inner_JsonUtil nearby = tower != null && tower.init_I() <= firstTick + 1
         ? null
         : init_k_JsonUtil_List(bodies, eyeHeight, reach, world, firstTick, eyes);
      ClutchBlockPlanner.Inner_JsonUtil first = nearby == null || tower != null && nearby.init_k_NetHelper().init_I() >= tower.init_I()
         ? (tower == null ? null : new ClutchBlockPlanner.Inner_JsonUtil(List.of(tower), tower.init_BlockPos(), tower.load_I() + 1, 0.0, false))
         : nearby;
      if (first == null) {
         return null;
      }

      final ClutchBlockPlanner.Inner_NetHelper root = first.init_k_NetHelper();
      ClutchBlockPlanner.Inner_DataUtil withRoot = new ClutchBlockPlanner.Inner_DataUtil() {
         @Override
         public boolean load(int x, int y, int z) {
            return x == root.init_BlockPos().getX() && y == root.init_BlockPos().getY() && z == root.init_BlockPos().getZ() || world.load(x, y, z);
         }

         @Override
         public boolean read(int x, int y, int z) {
            return (x != root.init_BlockPos().getX() || y != root.init_BlockPos().getY() || z != root.init_BlockPos().getZ()) && world.read(x, y, z);
         }

         @Override
         public boolean init(int x, int y, int z) {
            return world.init(x, y, z);
         }

         @Override
         public boolean init(Vec3 eye, double distance) {
            return eye.distanceToSqr(Vec3.atCenterOf(root.init_BlockPos())) > distance * distance && world.init(eye, distance);
         }
      };
      int after = root.init_I() + 1;
      ClutchBlockPlanner.Inner_NetHelper next = init_k_NetHelper_List(bodies, eyeHeight, reach, withRoot, after, eyes);
      ClutchBlockPlanner.Inner_JsonUtil extra = next != null && next.init_I() <= after ? null : init_k_JsonUtil_List(bodies, eyeHeight, reach, withRoot, after, eyes);
      if (extra != null && (next == null || extra.init_k_NetHelper().init_I() < next.init_I())) {
         next = extra.init_k_NetHelper();
      }

      return next != null && next.init_I() == after && next.load_BlockPos().equals(root.init_BlockPos())
         ? new ClutchBlockPlanner.Inner_JsonUtil(List.of(root, next), first.init_BlockPos(), first.init_I(), first.init_D(), first.load())
         : first;
   }

   static List<ClutchBlockPlanner.Inner_NetHelper> init_List_List(List<AABB> bodies, double eyeHeight, double reach, ClutchBlockPlanner.Inner_DataUtil world, int firstTick, int limit) {
      return init_List_List(bodies, eyeHeight, reach, world, firstTick, limit, List.of());
   }

   static List<ClutchBlockPlanner.Inner_NetHelper> init_List_List(
      List<AABB> bodies, double eyeHeight, double reach, ClutchBlockPlanner.Inner_DataUtil world, int firstTick, int limit, List<Vec3> writtenEyes
   ) {
      if (bodies != null && bodies.size() >= 2 && world != null && limit > 0) {
         ClutchBlockPlanner.Inner_TaskQueue flight = new ClutchBlockPlanner.Inner_TaskQueue(bodies, eyeHeight, world, 0.0, writtenEyes);
         int horizon = Math.min(bodies.size() - 1, 12);
         int radius = Mth.ceil(reach) + 1;
         double cellReach = (reach + 1.0) * (reach + 1.0);
         LongOpenHashSet seen = new LongOpenHashSet();
         List<long[]> found = new ArrayList<>();

         for (int k = Math.max(0, firstTick); k < horizon; k++) {
            Vec3 eye = flight.init_Vec3Arr[k];
            AABB body = bodies.get(k).deflate(1.0E-4);
            int ex = Mth.floor(eye.x);
            int ey = Mth.floor(eye.y);
            int ez = Mth.floor(eye.z);
            if (!init_Z_k_DataUtil(world, eye, cellReach, null)) {
               for (int x = ex - radius; x <= ex + radius; x++) {
                  for (int y = ey - radius; y <= ey + radius; y++) {
                     for (int z = ez - radius; z <= ez + radius; z++) {
                        if (!(eye.distanceToSqr(x + 0.5, y + 0.5, z + 0.5) > cellReach)) {
                           long key = BlockPos.asLong(x, y, z);
                           if (!seen.contains(key)
                              && flight.init_k_CachePool.init(key, x, y, z)
                              && (
                                 !(body.minX < x + 1)
                                    || !(body.maxX > x)
                                    || !(body.minY < y + 1)
                                    || !(body.maxY > y)
                                    || !(body.minZ < z + 1)
                                    || !(body.maxZ > z)
                              )
                              && flight.init_k_CachePool.read(key, x, y, z)) {
                              int hit = flight.init_k_ConfigStore.init(key, k);
                              if (hit >= 0) {
                                 for (int d = 0; d < init_DirectionArr.length; d++) {
                                    Direction direction = init_DirectionArr[d];
                                    int sx = x + direction.getStepX();
                                    int sy = y + direction.getStepY();
                                    int sz = z + direction.getStepZ();
                                    if (flight.init_k_CachePool.load(BlockPos.asLong(sx, sy, sz), sx, sy, sz)
                                       && (k != 0 || world.init(sx, sy, sz, direction.getOpposite()))
                                       && init_Z_Vec3(eye, sx, sy, sz, direction.getOpposite(), reach)) {
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
         List<ClutchBlockPlanner.Inner_NetHelper> links = new ArrayList<>(Math.min(limit, found.size()));

         for (long[] entry : found.subList(0, Math.min(limit, found.size()))) {
            BlockPos cell = BlockPos.of(entry[0]);
            Direction face = init_DirectionArr[(int)entry[1]];
            int k = (int)entry[2];
            links.add(new ClutchBlockPlanner.Inner_NetHelper(cell, cell.relative(face.getOpposite()), face, k, k));
         }

         return links;
      } else {
         return List.of();
      }
   }

   static ClutchBlockPlanner.Inner_NetHelper init_k_NetHelper_List(List<AABB> bodies, double eyeHeight, double reach, ClutchBlockPlanner.Inner_DataUtil world, int firstTick) {
      return init_k_NetHelper_List(bodies, eyeHeight, reach, world, firstTick, List.of());
   }

   static ClutchBlockPlanner.Inner_NetHelper init_k_NetHelper_List(
      List<AABB> bodies, double eyeHeight, double reach, ClutchBlockPlanner.Inner_DataUtil world, int firstTick, List<Vec3> writtenEyes
   ) {
      if (bodies != null && bodies.size() >= 2 && world != null) {
         int horizon = Math.min(bodies.size() - 2, 4);

         for (int k = Math.max(0, firstTick); k <= horizon; k++) {
            AABB body = bodies.get(k);
            Vec3 eye = writtenEyes.size() == bodies.size() ? writtenEyes.get(k) : init(body, eyeHeight);
            int top = Mth.floor(body.minY - 1.0E-6) - 1;
            BlockPos cell = null;
            BlockPos support = null;
            Direction face = null;

            for (int x = Mth.floor(body.minX + 1.0E-6); x <= Mth.floor(body.maxX - 1.0E-6); x++) {
               for (int z = Mth.floor(body.minZ + 1.0E-6); z <= Mth.floor(body.maxZ - 1.0E-6); z++) {
                  for (int y = top; y >= top - 3 && (cell == null || y > cell.getY()) && !world.load(x, y, z); y--) {
                     if (world.read(x, y, z) && init_Z_List(bodies, k, x, y, z)) {
                        for (Direction toward : init_DirectionArr) {
                           if (toward != Direction.UP) {
                              int sx = x + toward.getStepX();
                              int sy = y + toward.getStepY();
                              int sz = z + toward.getStepZ();
                              if (world.load(sx, sy, sz)
                                 && (k != 0 || world.init(sx, sy, sz, toward.getOpposite()))
                                 && init_Z_Vec3(eye, sx, sy, sz, toward.getOpposite(), reach)) {
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
                     && init_Z_List(bodies, later, cell.getX(), cell.getY(), cell.getZ())
                     && init_Z_Vec3(
                        writtenEyes.size() == bodies.size() ? writtenEyes.get(later) : init(bodies.get(later), eyeHeight),
                        support.getX(),
                        support.getY(),
                        support.getZ(),
                        face,
                        reach
                     )
               ) {
                  last = later++;
               }

               return new ClutchBlockPlanner.Inner_NetHelper(cell, support, face, k, last);
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static boolean init_Z_List(List<AABB> bodies, int k, int x, int y, int z) {
      AABB cell = new AABB(x, y, z, x + 1, y + 1, z + 1);
      return !cell.intersects(bodies.get(k).deflate(1.0E-4)) && !cell.intersects(bodies.get(k + 1).deflate(1.0E-4));
   }

   private static Vec3 init(AABB body, double eyeHeight) {
      return new Vec3((body.minX + body.maxX) * 0.5, body.minY + eyeHeight, (body.minZ + body.maxZ) * 0.5);
   }

   static boolean init_Z_Vec3_2(Vec3 clickEye, Vec3 nextEye, BlockPos support, Direction face, BlockPos placed, double reach) {
      return init_Z_Vec3_2(clickEye, nextEye, support, face, placed, reach, null);
   }

   static boolean init_Z_Vec3_2(Vec3 clickEye, Vec3 nextEye, BlockPos support, Direction face, BlockPos placed, double reach, Direction nextFace) {
      return init(clickEye, nextEye, support, face, placed, reach, nextFace, 0.02);
   }

   static boolean init(Vec3 clickEye, Vec3 nextEye, BlockPos support, Direction face, BlockPos placed, double reach, Direction nextFace, double margin) {
      double plane = init_D_I(support.getX(), support.getY(), support.getZ(), face);

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
         for (Vec3 point : init_List_Vec3(clickEye, nextEye, init(support, face), placed, nextFace, margin)) {
            Vec3 look = point.subtract(clickEye);
            if (look.lengthSqr() <= reach * reach
               && init_Z_Vec3_2(nextEye, look, placed, nextFace, reach)
               && ClutchFallPhysics.init_Z_BlockHitResult(AABB.clip(init_List, clickEye, clickEye.add(look.normalize().scale(reach)), support), support, face)) {
               return true;
            }
         }
      }

      return false;
   }

   static AABB init(BlockPos support, Direction face) {
      double p = init_D_I(support.getX(), support.getY(), support.getZ(), face);
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

   static List<Vec3> init_List_Vec3(Vec3 eye, Vec3 nextEye, AABB rect, BlockPos placed, Direction nextFace) {
      return init_List_Vec3(eye, nextEye, rect, placed, nextFace, 0.02);
   }

   static List<Vec3> init_List_Vec3(Vec3 eye, Vec3 nextEye, AABB rect, BlockPos placed, Direction nextFace, double margin) {
      List<Vec3> polygon = load(eye, nextEye, rect, placed, nextFace, margin);
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

   private static List<Vec3> load(Vec3 eye, Vec3 nextEye, AABB rect, BlockPos placed, Direction nextFace, double margin) {
      if (nextFace == null) {
         return List.of();
      }

      Axis normal = nextFace.getAxis();
      double offset = init_D_I(placed.getX(), placed.getY(), placed.getZ(), nextFace) - init(nextEye, normal);
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
            init_Vec3_AABB(rect, u, true, v, false),
            init_Vec3_AABB(rect, u, true, v, true),
            init_Vec3_AABB(rect, u, false, v, true)
         )
      );
      polygon = init(polygon, point -> sign * (init(point, normal) - init(eye, normal)) - 1.0E-8);
      double distance = Math.abs(offset);
      AABB target = new AABB(placed);

      for (Axis axis : Axis.values()) {
         if (axis != normal && !polygon.isEmpty()) {
            double low = target.min(axis) + 1.0E-5;
            double high = target.max(axis) - 1.0E-5;
            polygon = init(
               polygon,
               point -> (init(nextEye, axis) - low) * sign * (init(point, normal) - init(eye, normal)) + distance * (init(point, axis) - init(eye, axis))
            );
            polygon = init(
               polygon,
               point -> (high - init(nextEye, axis)) * sign * (init(point, normal) - init(eye, normal)) - distance * (init(point, axis) - init(eye, axis))
            );
         }
      }

      return polygon;
   }

   static List<ClutchBlockPlanner.Inner_StreamUtil> init_List_Vec3_2(
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
         List<Vec3> polygon = load(eye, nextEye, rect, placed, nextFace, margin);
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
         List<ClutchBlockPlanner.Inner_StreamUtil> result = new ArrayList<>(8);
         List<Double> margins = new ArrayList<>(8);

         for (int step = 0; step <= Math.max(centre - low, high - centre) * 2; step++) {
            int count = centre + (step % 2 == 0 ? step / 2 : -(step + 1) / 2);
            if (count >= low && count <= high) {
               float yaw = fromYaw + (float)(count * gcd);
               Vec3 flat = Vec3.directionFromRotation(0.0F, yaw);
               Axis axis = face.getAxis();
               Axis tangent = axis == Axis.X ? Axis.Z : Axis.X;
               double normal = init(flat, axis);
               double distance = (rect.min(axis) - init(eye, axis)) / normal;
               if (Double.isFinite(distance) && !(distance <= 0.0)) {
                  Vec3 crossing = eye.add(flat.scale(distance));
                  double across = init(crossing, tangent);
                  double bottom = 1.0 / 0.0;
                  double top = -1.0 / 0.0;

                  for (int i = 0; i < polygon.size(); i++) {
                     Vec3 a = polygon.get(i);
                     Vec3 b = polygon.get((i + 1) % polygon.size());
                     double da = init(a, tangent);
                     double db = init(b, tangent);
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
                           BlockHitResult hit = AABB.clip(init_List, eye, eye.add(look.scale(reach)), support);
                           if (ClutchFallPhysics.init_Z_BlockHitResult(hit, support, face)
                              && autismclient.util.BlockPlacementPlanner.isPointInsideFace(rect, face, hit.getLocation(), margin)
                              && init_Z_Vec3_2(nextEye, look, placed, nextFace, reach)) {
                              ClutchBlockPlanner.Inner_StreamUtil found = new ClutchBlockPlanner.Inner_StreamUtil(yaw, pitch);
                              if (!result.contains(found)) {
                                 BlockHitResult following = AABB.clip(init_List, nextEye, nextEye.add(look.scale(reach)), placed);
                                 Vec3 point = following.getLocation();
                                 AABB box = new AABB(placed);
                                 double clearance = 1.0 / 0.0;

                                 for (Axis inPlane : Axis.values()) {
                                    if (inPlane != nextFace.getAxis()) {
                                       clearance = Math.min(
                                          clearance, Math.min(init(point, inPlane) - box.min(inPlane), box.max(inPlane) - init(point, inPlane))
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

   static List<ClutchBlockPlanner.Inner_StreamUtil> init_List_Vec3(
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
      DoubleFunction<ClutchFallPhysics.Inner_DataUtil> eyes
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
         List<ClutchBlockPlanner.Inner_StreamUtil> result = new ArrayList<>(8);
         List<Double> scores = new ArrayList<>(8);
         AABB followingRect = init(placed, nextFace);

         for (int step = 0; step <= Math.max(centre - low, high - centre) * 2; step++) {
            int count = centre + (step % 2 == 0 ? step / 2 : -(step + 1) / 2);
            if (count >= low && count <= high) {
               float yaw = fromYaw + (float)(count * gcd);
               ClutchFallPhysics.Inner_DataUtil window = eyes.apply(yaw);
               if (window != null) {
                  Vec3 flat = Vec3.directionFromRotation(0.0F, yaw);
                  double[] here = init_DArr_Vec3(window.init_Vec3(), flat, rect, face, margin);
                  double[] next = init_DArr_Vec3(window.load(), flat, followingRect, nextFace, 1.0E-5);
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
                              BlockHitResult hit = AABB.clip(init_List, window.init_Vec3(), window.init_Vec3().add(look.scale(reach)), support);
                              BlockHitResult following = AABB.clip(init_List, window.load(), window.load().add(look.scale(reach)), placed);
                              if (ClutchFallPhysics.init_Z_BlockHitResult(hit, support, face)
                                 && autismclient.util.BlockPlacementPlanner.isPointInsideFace(rect, face, hit.getLocation(), margin)
                                 && ClutchFallPhysics.init_Z_BlockHitResult(following, placed, nextFace)) {
                                 ClutchBlockPlanner.Inner_StreamUtil found = new ClutchBlockPlanner.Inner_StreamUtil(yaw, pitch);
                                 if (!result.contains(found)) {
                                    Vec3 point = following.getLocation();
                                    AABB box = new AABB(placed);
                                    double clearance = 1.0 / 0.0;

                                    for (Axis inPlane : Axis.values()) {
                                       if (inPlane != nextFace.getAxis()) {
                                          clearance = Math.min(
                                             clearance, Math.min(init(point, inPlane) - box.min(inPlane), box.max(inPlane) - init(point, inPlane))
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

   private static double[] init_DArr_Vec3(Vec3 eye, Vec3 flat, AABB rect, Direction face, double margin) {
      Axis axis = face.getAxis();
      Axis tangent = axis == Axis.X ? Axis.Z : Axis.X;
      double plane = rect.min(axis);
      if ((init(eye, axis) - plane) * (face.getAxisDirection() == AxisDirection.POSITIVE ? 1 : -1) <= 1.0E-7) {
         return null;
      }

      double distance = (plane - init(eye, axis)) / init(flat, axis);
      if (Double.isFinite(distance) && !(distance <= 0.0)) {
         Vec3 point = eye.add(flat.scale(distance));
         double across = init(point, tangent);
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

   private static Vec3 init_Vec3_AABB(AABB rect, Axis u, boolean upperU, Axis v, boolean upperV) {
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

   private static List<Vec3> init(List<Vec3> polygon, ToDoubleFunction<Vec3> halfPlane) {
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

   private static double init(Vec3 point, Axis axis) {
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
      return face == null ? new AABB(cell).deflate(0.05).clip(eye, end).isPresent() : ClutchFallPhysics.init_Z_BlockHitResult(AABB.clip(init_List, eye, end, cell), cell, face);
   }

   private static int init(long key, Long2LongOpenHashMap parent) {
      int length = 1;

      for (long at = parent.get(key); at != -9223372036854775808L; at = parent.get(at)) {
         length++;
      }

      return length;
   }

   static boolean init_Z_Vec3(Vec3 eye, int sx, int sy, int sz, Direction face, double reach) {
      return init_Z_Vec3(eye, sx, sy, sz, face, reach, 0.3);
   }

   private static boolean init_Z_Vec3(Vec3 eye, int sx, int sy, int sz, Direction face, double reach, double inset) {
      return init_Z_Vec3(eye, sx, sy, sz, face) && eye.distanceToSqr(init_Vec3_Vec3(eye, sx, sy, sz, face, inset)) <= reach * reach;
   }

   private static boolean init_Z_Vec3(Vec3 eye, int sx, int sy, int sz, Direction face) {
      double plane = init_D_I(sx, sy, sz, face);
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
      return init_Vec3_Vec3(eye, sx, sy, sz, face, 0.3);
   }

   static Vec3 init_Vec3_Vec3(Vec3 eye, int sx, int sy, int sz, Direction face, double inset) {
      double plane = init_D_I(sx, sy, sz, face);
      double px = face.getAxis() == Axis.X ? plane : Mth.clamp(eye.x, sx + inset, sx + 1 - inset);
      double py = face.getAxis() == Axis.Y ? plane : Mth.clamp(eye.y, sy + inset, sy + 1 - inset);
      double pz = face.getAxis() == Axis.Z ? plane : Mth.clamp(eye.z, sz + inset, sz + 1 - inset);
      return new Vec3(px, py, pz);
   }

   private static double init_D_I(int sx, int sy, int sz, Direction face) {
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

   private record Inner_BufferHelper(
      ClutchBlockPlanner.Inner_TaskQueue init_k_TaskQueue,
      double init_D,
      BlockPos init_BlockPos,
      int init_I,
      ToDoubleFunction<ClutchBlockPlanner.Inner_NetHelper> init_ToDoubleFunction,
      int load_I,
      DoubleUnaryOperator init_DoubleUnaryOperato,
      double load_D
   ) {
      int init(boolean fatal) {
         int last = 0;
         double limit = fatal ? this.init_D * 1.5 : this.init_D;

         for (Entry entry : this.init_k_TaskQueue.init_Long2IntOpenHashMa.long2IntEntrySet()) {
            double hurt = this.init_DoubleUnaryOperato.applyAsDouble(this.init_k_TaskQueue.init_Long2DoubleOpenHas.get(entry.getLongKey()));
            if (hurt < limit && hurt <= this.load_D) {
               last = Math.max(last, entry.getIntValue());
            }
         }

         return last;
      }

      ClutchBlockPlanner.Inner_JsonUtil init(ClutchBlockPlanner.Inner_StateCache search, boolean fatal, boolean reachOut) {
         LongArrayList goals = new LongArrayList();
         Long2DoubleOpenHashMap cheap = new Long2DoubleOpenHashMap();

         for (Entry entry : this.init_k_TaskQueue.init_Long2IntOpenHashMa.long2IntEntrySet()) {
            long key = entry.getLongKey();
            int due = entry.getIntValue();
            int placed = search.init_Long2IntOpenHashMa().get(key);
            if (placed >= 0 && placed <= due - 1) {
               int length = ClutchBlockPlanner.init(key, search.init_Long2LongOpenHashM());
               if (length <= this.load_I) {
                  double hurt = this.init_DoubleUnaryOperato.applyAsDouble(this.init_k_TaskQueue.init_Long2DoubleOpenHas.get(key));
                  if (!(hurt >= (fatal ? this.init_D * 1.5 : this.init_D)) && !(hurt > this.load_D)) {
                     int row = BlockPos.getY(key);
                     double score = hurt * 100.0 + length * 1.5 + Math.max(0, row - this.init_I) * 2 + Math.max(0, this.init_I - row) + due * 0.3;
                     if (this.init_BlockPos != null && this.init_BlockPos.asLong() == key) {
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
         ClutchBlockPlanner.Inner_JsonUtil chosen = null;
         double chosenScore = 1.0 / 0.0;

         for (int g = 0; g < Math.min(goals.size(), this.init_ToDoubleFunction == null ? 1 : 6); g++) {
            long goal = goals.getLong(g);
            ClutchBlockPlanner.Inner_JsonUtil plan = this.init(
               goal, this.init_k_TaskQueue.init_Long2IntOpenHashMa.get(goal), this.init_k_TaskQueue.init_Long2DoubleOpenHas.get(goal), search, reachOut
            );
            double score = cheap.get(goal) + (this.init_ToDoubleFunction == null ? 0.0 : this.init_ToDoubleFunction.applyAsDouble(plan.init_k_NetHelper()));
            if (score < chosenScore) {
               chosen = plan;
               chosenScore = score;
            }
         }

         return chosen;
      }

      private ClutchBlockPlanner.Inner_JsonUtil init(long goal, int landingTick, double fall, ClutchBlockPlanner.Inner_StateCache search, boolean reachOut) {
         Vec3[] eyes = this.init_k_TaskQueue.init_Vec3Arr;
         double reach = search.init_D();
         LongArrayList chain = new LongArrayList();

         for (long at = goal; at != -9223372036854775808L; at = search.init_Long2LongOpenHashM().get(at)) {
            chain.add(0, at);
         }

         int[] latest = new int[chain.size()];
         int next = landingTick;

         for (int i = chain.size() - 1; i >= 0; i--) {
            long key = chain.getLong(i);
            BlockPos cell = BlockPos.of(key);
            Direction face = ClutchBlockPlanner.init_DirectionArr[search.init_Long2ByteOpenHashM().get(key)];
            BlockPos support = cell.relative(face.getOpposite());
            int earliest = search.init_Long2IntOpenHashMa().get(key);
            int last = earliest;
            boolean landing = i == chain.size() - 1;
            Direction nextFace = landing ? null : autismclient.modules.ClutchBlockPlanner.init_DirectionArr[search.init_Long2ByteOpenHashM().get(chain.getLong(i + 1))];

            for (int k = next - 1; k > earliest; k--) {
               if (this.init(key, support, face, k, landing ? landingTick : this.init_k_TaskQueue.init_List.size(), reach)
                  && (
                     this.init_k_TaskQueue.init_Z
                        || landing
                        || k < next - 1
                        || autismclient.modules.ClutchBlockPlanner.init(eyes[k], eyes[next], support, face, cell, reach, nextFace, this.init_k_TaskQueue.init_D)
                  )) {
                  last = k;
                  break;
               }
            }

            latest[i] = last;
            next = last;
         }

         List<ClutchBlockPlanner.Inner_NetHelper> links = new ArrayList<>(chain.size());

         for (int i = 0; i < chain.size(); i++) {
            long key = chain.getLong(i);
            BlockPos cell = BlockPos.of(key);
            Direction face = autismclient.modules.ClutchBlockPlanner.init_DirectionArr[search.init_Long2ByteOpenHashM().get(key)];
            links.add(new ClutchBlockPlanner.Inner_NetHelper(cell, cell.relative(face.getOpposite()), face, search.init_Long2IntOpenHashMa().get(key), latest[i]));
         }

         return new ClutchBlockPlanner.Inner_JsonUtil(List.copyOf(links), BlockPos.of(goal), landingTick, fall, reachOut);
      }

      private boolean init(long key, BlockPos support, Direction face, int k, int clearUntil, double reach) {
         if (new AABB(BlockPos.of(key)).intersects(this.init_k_TaskQueue.init_List.get(k).deflate(1.0E-4))) {
            return false;
         } else {
            return !this.init_k_TaskQueue.init_k_ConfigStore.init(key, k, clearUntil)
               ? false
               : autismclient.modules.ClutchBlockPlanner.init_Z_Vec3(this.init_k_TaskQueue.init_Vec3Arr[k], support.getX(), support.getY(), support.getZ(), face, reach);
         }
      }
   }

   private static final class Inner_CachePool {
      private final ClutchBlockPlanner.Inner_DataUtil init_k_DataUtil;
      private final Long2ByteOpenHashMap init_Long2ByteOpenHashM = new Long2ByteOpenHashMap();
      private final Long2ByteOpenHashMap load = new Long2ByteOpenHashMap();
      private final Long2ByteOpenHashMap read = new Long2ByteOpenHashMap();

      Inner_CachePool(ClutchBlockPlanner.Inner_DataUtil world) {
         this.init_k_DataUtil = world;
      }

      boolean init(long key, int x, int y, int z) {
         byte known = this.init_Long2ByteOpenHashM.get(key);
         if (known == 0) {
            this.init_Long2ByteOpenHashM.put(key, known = (byte)(this.init_k_DataUtil.read(x, y, z) ? 1 : 2));
         }

         return known == 1;
      }

      boolean load(long key, int x, int y, int z) {
         byte known = this.load.get(key);
         if (known == 0) {
            this.load.put(key, known = (byte)(this.init_k_DataUtil.load(x, y, z) ? 1 : 2));
         }

         return known == 1;
      }

      boolean read(long key, int x, int y, int z) {
         byte known = this.read.get(key);
         if (known == 0) {
            boolean any = false;

            for (Direction direction : autismclient.modules.ClutchBlockPlanner.init_DirectionArr) {
               int sx = x + direction.getStepX();
               int sy = y + direction.getStepY();
               int sz = z + direction.getStepZ();
               if (this.load(BlockPos.asLong(sx, sy, sz), sx, sy, sz)) {
                  any = true;
                  break;
               }
            }

            this.read.put(key, known = (byte)(any ? 1 : 2));
         }

         return known == 1;
      }
   }

   private static final class Inner_ConfigStore {
      private final List<AABB> init_List;
      private final AABB init_AABB;
      private final Long2ObjectOpenHashMap<int[]> init_Long2ObjectOpenHas = new Long2ObjectOpenHashMap<>();

      Inner_ConfigStore(List<AABB> bodies) {
         this.init_List = bodies;
         AABB all = bodies.getFirst();

         for (AABB body : bodies) {
            all = all.minmax(body);
         }

         this.init_AABB = all;
      }

      boolean init(long key, int from, int until) {
         for (int value : this.init(key)) {
            int t = value >> 1;
            if (t > from) {
               return t >= until || (value & 1) == 1;
            }
         }

         return true;
      }

      int init(long key, int from) {
         for (int value : this.init(key)) {
            int t = value >> 1;
            if (t > from) {
               return (value & 1) == 1 ? -1 : t;
            }
         }

         return -1;
      }

      private int[] init(long key) {
         int[] met = this.init_Long2ObjectOpenHas.get(key);
         if (met == null) {
            this.init_Long2ObjectOpenHas.put(key, met = this.load(key));
         }

         return met;
      }

      private int[] load(long key) {
         AABB shrunk = new AABB(BlockPos.of(key)).deflate(0.001);
         if (!shrunk.intersects(this.init_AABB)) {
            return new int[0];
         }

         int[] found = new int[this.init_List.size()];
         int count = 0;

         for (int t = 1; t < this.init_List.size(); t++) {
            if (shrunk.intersects(this.init_List.get(t))) {
               AABB before = this.init_List.get(t - 1);
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

   interface Inner_DataUtil {
      boolean load(int var1, int var2, int var3);

      boolean read(int var1, int var2, int var3);

      default boolean write(int x, int y, int z) {
         return false;
      }

      default boolean init(int x, int y, int z, Direction face) {
         return this.write(x, y, z);
      }

      default boolean init(int x, int y, int z) {
         return true;
      }

      default boolean init(Vec3 eye, double distance) {
         return false;
      }
   }

   record Inner_JsonUtil(List<ClutchBlockPlanner.Inner_NetHelper> init_List, BlockPos init_BlockPos, int init_I, double init_D, boolean init_Z, boolean load) {
      Inner_JsonUtil(List<ClutchBlockPlanner.Inner_NetHelper> links, BlockPos landing, int landingTick, double fall, boolean reachOut) {
         this(links, landing, landingTick, fall, reachOut, false);
      }

      ClutchBlockPlanner.Inner_NetHelper init_k_NetHelper() {
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

   record Inner_NetHelper(BlockPos init_BlockPos, BlockPos load_BlockPos, Direction init_Direction, int init_I, int load_I) {
   }

   private record Inner_StateCache(
      Long2IntOpenHashMap init_Long2IntOpenHashMa,
      Long2LongOpenHashMap init_Long2LongOpenHashM,
      Long2ByteOpenHashMap init_Long2ByteOpenHashM,
      LongOpenHashSet init_LongOpenHashSet,
      double init_D
   ) {
   }

   record Inner_StreamUtil(float init, float load) {
   }

   private static final class Inner_TaskQueue {
      final List<AABB> init_List;
      final Vec3[] init_Vec3Arr;
      final ClutchBlockPlanner.Inner_DataUtil init_k_DataUtil;
      double init_D = 0.02;
      boolean init_Z;
      final ClutchBlockPlanner.Inner_CachePool init_k_CachePool;
      final ClutchBlockPlanner.Inner_ConfigStore init_k_ConfigStore;
      final Long2IntOpenHashMap init_Long2IntOpenHashMa = new Long2IntOpenHashMap();
      final Long2DoubleOpenHashMap init_Long2DoubleOpenHas = new Long2DoubleOpenHashMap();

      Inner_TaskQueue(List<AABB> bodies, double eyeHeight, ClutchBlockPlanner.Inner_DataUtil world, double fallSoFar) {
         this(bodies, eyeHeight, world, fallSoFar, List.of());
      }

      Inner_TaskQueue(List<AABB> bodies, double eyeHeight, ClutchBlockPlanner.Inner_DataUtil world, double fallSoFar, List<Vec3> writtenEyes) {
         this(bodies, eyeHeight, world, fallSoFar, writtenEyes, 0.06);
      }

      Inner_TaskQueue(List<AABB> bodies, double eyeHeight, ClutchBlockPlanner.Inner_DataUtil world, double fallSoFar, List<Vec3> writtenEyes, double landingInset) {
         this.init_List = bodies;
         this.init_k_DataUtil = world;
         this.init_k_CachePool = new ClutchBlockPlanner.Inner_CachePool(world);
         this.init_k_ConfigStore = new ClutchBlockPlanner.Inner_ConfigStore(bodies);
         int horizon = bodies.size() - 1;
         this.init_Vec3Arr = new Vec3[bodies.size()];

         for (int t = 0; t <= horizon; t++) {
            AABB body = bodies.get(t);
            this.init_Vec3Arr[t] = writtenEyes.size() == bodies.size()
               ? writtenEyes.get(t)
               : new Vec3((body.minX + body.maxX) * 0.5, body.minY + eyeHeight, (body.minZ + body.maxZ) * 0.5);
         }

         this.init_Long2IntOpenHashMa.defaultReturnValue(-1);
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
                           if (!this.init_Long2IntOpenHashMa.containsKey(key) && this.init_k_CachePool.init(key, x, y, z)) {
                              this.init_Long2IntOpenHashMa.put(key, t);
                              this.init_Long2DoubleOpenHas.put(key, fallSoFar + Math.max(0.0, highest - level));
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      ClutchBlockPlanner.Inner_StateCache init(int firstTick, double reach, boolean carried, int lastTick) {
         int horizon = Math.min(this.init_List.size() - 1, lastTick);
         Long2IntOpenHashMap best = new Long2IntOpenHashMap();
         best.defaultReturnValue(-1);
         Long2LongOpenHashMap parent = new Long2LongOpenHashMap();
         parent.defaultReturnValue(-9223372036854775808L);
         Long2ByteOpenHashMap parentFace = new Long2ByteOpenHashMap();
         LongOpenHashSet linkable = new LongOpenHashSet();
         LongOpenHashSet besideLink = new LongOpenHashSet();
         LongArrayList placedNow = new LongArrayList();
         int[] carriedFrom = new int[autismclient.modules.ClutchBlockPlanner.init_DirectionArr.length];
         int radius = Mth.ceil(reach) + 1;
         double cellReach = (reach + 1.0) * (reach + 1.0);

         for (int k = Math.max(0, firstTick); k < horizon; k++) {
            Vec3 eye = this.init_Vec3Arr[k];
            AABB body = this.init_List.get(k).deflate(1.0E-4);
            int ex = Mth.floor(eye.x);
            int ey = Mth.floor(eye.y);
            int ez = Mth.floor(eye.z);
            if (!autismclient.modules.ClutchBlockPlanner.init_Z_k_DataUtil(this.init_k_DataUtil, eye, cellReach, linkable)) {
               placedNow.clear();

               for (int x = ex - radius; x <= ex + radius; x++) {
                  for (int y = ey - radius; y <= ey + radius; y++) {
                     for (int z = ez - radius; z <= ez + radius; z++) {
                        if (!(eye.distanceToSqr(x + 0.5, y + 0.5, z + 0.5) > cellReach)) {
                           long key = BlockPos.asLong(x, y, z);
                           if (!best.containsKey(key)
                              && this.init_k_CachePool.init(key, x, y, z)
                              && (
                                 !(body.minX < x + 1)
                                    || !(body.maxX > x)
                                    || !(body.minY < y + 1)
                                    || !(body.maxY > y)
                                    || !(body.minZ < z + 1)
                                    || !(body.maxZ > z)
                              )
                              && (besideLink.contains(key) || this.init_k_CachePool.read(key, x, y, z))) {
                              int due = this.init_Long2IntOpenHashMa.get(key);
                              boolean landing = due > k && this.init_k_ConfigStore.init(key, k, due);
                              boolean link = this.init_k_ConfigStore.init(key, k, this.init_List.size());
                              if (landing || link) {
                                 int chosen = -1;
                                 int carriedCount = 0;
                                 long support = -9223372036854775808L;

                                 for (int d = 0; d < autismclient.modules.ClutchBlockPlanner.init_DirectionArr.length; d++) {
                                    Direction direction = autismclient.modules.ClutchBlockPlanner.init_DirectionArr[d];
                                    int sx = x + direction.getStepX();
                                    int sy = y + direction.getStepY();
                                    int sz = z + direction.getStepZ();
                                    long at = BlockPos.asLong(sx, sy, sz);
                                    int placed = best.get(at);
                                    boolean existing = placed < 0 && this.init_k_CachePool.load(at, sx, sy, sz);
                                    if ((existing || placed >= 0 && placed < k && linkable.contains(at))
                                       && (k != 0 || existing && (this.init_Z || this.init_k_DataUtil.init(sx, sy, sz, direction.getOpposite())))) {
                                       Direction face = direction.getOpposite();
                                       if ((!existing || this.init_k_DataUtil.init(x, y, z))
                                          && autismclient.modules.ClutchBlockPlanner.init_Z_Vec3(eye, sx, sy, sz, face, reach, carried ? 0.3 : 0.03)) {
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
                                    Direction direction = autismclient.modules.ClutchBlockPlanner.init_DirectionArr[carriedFrom[i]];
                                    BlockPos placedAt = new BlockPos(x + direction.getStepX(), y + direction.getStepY(), z + direction.getStepZ());
                                    Direction under = autismclient.modules.ClutchBlockPlanner.init_DirectionArr[parentFace.get(placedAt.asLong())];
                                    if (autismclient.modules.ClutchBlockPlanner.init(
                                       this.init_Vec3Arr[k - 1],
                                       eye,
                                       placedAt.relative(under.getOpposite()),
                                       under,
                                       placedAt,
                                       reach,
                                       direction.getOpposite(),
                                       this.init_D
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

                     for (Direction direction : autismclient.modules.ClutchBlockPlanner.init_DirectionArr) {
                        besideLink.add(BlockPos.offset(key, direction));
                     }
                  }
               }
            }
         }

         return new ClutchBlockPlanner.Inner_StateCache(best, parent, parentFace, linkable, reach);
      }

      ClutchBlockPlanner.Inner_JsonUtil init(int firstTick, double reach) {
         return this.init(firstTick, reach, this.init_List.size());
      }

      ClutchBlockPlanner.Inner_JsonUtil init(int firstTick, double reach, int nearbyTicks) {
         ClutchBlockPlanner.Inner_NetHelper chosen = null;
         double chosenScore = 1.0 / 0.0;
         int radius = Mth.ceil(reach) + 1;
         double cellReach = (reach + 1.0) * (reach + 1.0);
         int last = Math.min(this.init_List.size(), Math.max(0, firstTick) + 3 + 1);

         for (int k = Math.max(0, firstTick); k < last; k++) {
            Vec3 eye = this.init_Vec3Arr[k];
            AABB body = this.init_List.get(k).deflate(1.0E-4);
            int ex = Mth.floor(eye.x);
            int ey = Mth.floor(eye.y);
            int ez = Mth.floor(eye.z);
            if (!autismclient.modules.ClutchBlockPlanner.init_Z_k_DataUtil(this.init_k_DataUtil, eye, cellReach, null)) {
               for (int x = ex - radius; x <= ex + radius; x++) {
                  for (int y = ey - radius; y <= ey + radius; y++) {
                     for (int z = ez - radius; z <= ez + radius; z++) {
                        if (!(eye.distanceToSqr(x + 0.5, y + 0.5, z + 0.5) > (reach + 1.0) * (reach + 1.0))) {
                           long key = BlockPos.asLong(x, y, z);
                           if (this.init_k_DataUtil.init(x, y, z)
                              && this.init_k_CachePool.init(key, x, y, z)
                              && this.init_k_CachePool.read(key, x, y, z)
                              && !new AABB(x, y, z, x + 1, y + 1, z + 1).intersects(body)) {
                              Direction face = null;
                              BlockPos support = null;

                              for (Direction direction : autismclient.modules.ClutchBlockPlanner.init_DirectionArr) {
                                 int sx = x + direction.getStepX();
                                 int sy = y + direction.getStepY();
                                 int sz = z + direction.getStepZ();
                                 if (this.init_k_CachePool.load(BlockPos.asLong(sx, sy, sz), sx, sy, sz)
                                    && (k != 0 || this.init_k_DataUtil.init(sx, sy, sz, direction.getOpposite()))) {
                                    Direction outward = direction.getOpposite();
                                    if (autismclient.modules.ClutchBlockPlanner.init_Z_Vec3(eye, sx, sy, sz, outward, reach, 0.03)) {
                                       face = outward;
                                       support = new BlockPos(sx, sy, sz);
                                       break;
                                    }
                                 }
                              }

                              if (face != null) {
                                 double nearest = 1.0 / 0.0;

                                 for (int t = k; t < nearbyTicks; t++) {
                                    AABB future = this.init_List.get(t);
                                    double dx = (future.minX + future.maxX) * 0.5 - x - 0.5;
                                    double dz = (future.minZ + future.maxZ) * 0.5 - z - 0.5;
                                    double dy = Math.max(0.0, future.minY - y - 1.0) * 0.5 + Math.max(0.0, y + 1 - future.minY);
                                    nearest = Math.min(nearest, dx * dx + dz * dz + dy * dy);
                                 }

                                 double score = nearest + k * 0.25;
                                 if (score < chosenScore) {
                                    chosenScore = score;
                                    chosen = new ClutchBlockPlanner.Inner_NetHelper(new BlockPos(x, y, z), support, face, k, k);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         return chosen == null ? null : new ClutchBlockPlanner.Inner_JsonUtil(List.of(chosen), chosen.init_BlockPos(), this.init_List.size() - 1, 0.0 / 0.0, true);
      }
   }
}
