package autismclient.ai.print;

import autismclient.ai.engine.AiPhysics;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

final class PrintFlight {
   private static final int[][] init_IArr2 = new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
   static final double init_D = 0.3;
   static final double load = 0.4;
   private static final AABB init_AABB = new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);

   static Vec3 init(long key) {
      return new Vec3(BlockPos.getX(key) + 0.5, BlockPos.getY(key) + 0.15, BlockPos.getZ(key) + 0.5);
   }

   static boolean init(PrintView view, Vec3 point) {
      if (view.init_LongOpenHashSet.isEmpty()) {
         return false;
      }

      AABB box = PrintGeometry.init(point.x, point.y, point.z, false).inflate(0.3, 0.4, 0.3);
      int x0 = (int)Math.floor(box.minX);
      int x1 = (int)Math.floor(box.maxX);
      int y0 = (int)Math.floor(box.minY);
      int y1 = (int)Math.floor(box.maxY);
      int z0 = (int)Math.floor(box.minZ);
      int z1 = (int)Math.floor(box.maxZ);

      for (int x = x0; x <= x1; x++) {
         for (int y = y0; y <= y1; y++) {
            for (int z = z0; z <= z1; z++) {
               if (view.init_LongOpenHashSet.contains(BlockPos.asLong(x, y, z))) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   static boolean load(PrintView view, Vec3 point) {
      AABB box = PrintGeometry.init(point.x, point.y, point.z, false);
      if (!view.init_Z_AABB(box)) {
         return false;
      }

      for (BlockPos p : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ), BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
         BlockState state = view.getBlockState(p);
         if (state == PrintView.init_BlockState || PrintView.init_Z_BlockState(state)) {
            return false;
         }
      }

      return true;
   }

   static boolean init_Z(PrintView view, Vec3 from, Vec3 to) {
      return init_BlockPos(view, from, to) == null;
   }

   static BlockPos init_BlockPos(PrintView view, Vec3 from, Vec3 to) {
      int legs = Math.max(1, (int)Math.ceil(Math.max(Math.abs(to.x - from.x), Math.max(Math.abs(to.y - from.y), Math.abs(to.z - from.z)))));
      MutableBlockPos position = new MutableBlockPos();
      Vec3 start = from;

      for (int leg = 1; leg <= legs; leg++) {
         Vec3 end = leg == legs ? to : from.lerp(to, (double)leg / legs);
         int x0 = (int)Math.floor(Math.min(start.x, end.x) - 0.3);
         int x1 = (int)Math.floor(Math.max(start.x, end.x) + 0.3);
         int y0 = (int)Math.floor(Math.min(start.y, end.y)) - 1;
         int y1 = (int)Math.floor(Math.max(start.y, end.y) + 1.8);
         int z0 = (int)Math.floor(Math.min(start.z, end.z) - 0.3);
         int z1 = (int)Math.floor(Math.max(start.z, end.z) + 0.3);

         for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
               for (int z = z0; z <= z1; z++) {
                  position.set(x, y, z);
                  BlockState state = view.getBlockState(position);
                  if (!state.isAir()) {
                     if ((state == PrintView.init_BlockState || PrintView.init_Z_BlockState(state)) && init_Z_Vec3(start, end, init_AABB, x, y, z, -1.0E-7)) {
                        return position.immutable();
                     }

                     for (AABB box : view.init_List_BlockState_BlockPos(state, position)) {
                        if (init_Z_Vec3(start, end, box, x, y, z, 1.0E-6)) {
                           return position.immutable();
                        }
                     }
                  }
               }
            }
         }

         start = end;
      }

      return null;
   }

   private static boolean init_Z_Vec3(Vec3 from, Vec3 to, AABB box, int x, int y, int z, double inset) {
      double enter = 0.0;
      double leave = 1.0;
      double dx = to.x - from.x;
      double min = box.minX + x - 0.3 + inset;
      double max = box.maxX + x + 0.3 - inset;
      if (Math.abs(dx) < 1.0E-12) {
         if (from.x <= min || from.x >= max) {
            return false;
         }
      } else {
         double a = (min - from.x) / dx;
         double b = (max - from.x) / dx;
         enter = Math.max(enter, Math.min(a, b));
         leave = Math.min(leave, Math.max(a, b));
         if (enter >= leave) {
            return false;
         }
      }

      double dy = to.y - from.y;
      min = box.minY + y - 1.8 + inset;
      max = box.maxY + y - inset;
      if (Math.abs(dy) < 1.0E-12) {
         if (from.y <= min || from.y >= max) {
            return false;
         }
      } else {
         double a = (min - from.y) / dy;
         double b = (max - from.y) / dy;
         enter = Math.max(enter, Math.min(a, b));
         leave = Math.min(leave, Math.max(a, b));
         if (enter >= leave) {
            return false;
         }
      }

      double dz = to.z - from.z;
      min = box.minZ + z - 0.3 + inset;
      max = box.maxZ + z + 0.3 - inset;
      if (Math.abs(dz) < 1.0E-12) {
         if (from.z <= min || from.z >= max) {
            return false;
         }
      } else {
         double a = (min - from.z) / dz;
         double b = (max - from.z) / dz;
         enter = Math.max(enter, Math.min(a, b));
         leave = Math.min(leave, Math.max(a, b));
      }

      return enter < leave;
   }

   static int init_I_ArrayUtil(PrintView view, Vec3 body, List<Vec3> points, int current) {
      for (int next = Math.min(points.size() - 1, current + 8); next > current; next--) {
         if (init_Z(view, body, points.get(next))) {
            return next;
         }
      }

      return current;
   }

   static PrintFlight.Inner_BufferHelper init_TextFormat_BufferH_ArrayUtil(PrintView view, Vec3 start, Set<Long> goals, int limit) {
      PrintFlight.Inner_ConfigStore search = new PrintFlight.Inner_ConfigStore(view, start, goals, limit);
      search.init(9223372036854775807L);
      return search.init_TextFormat_BufferH;
   }

   private static double init(Vec3 point, Set<Long> goals) {
      boolean lattice = point.x == Math.floor(point.x) + 0.5 && point.y == Math.floor(point.y) + 0.15 && point.z == Math.floor(point.z) + 0.5;
      double best = 1.0 / 0.0;

      for (long key : goals) {
         double dx = Math.abs(point.x - BlockPos.getX(key) - 0.5);
         double dy = Math.abs(point.y - BlockPos.getY(key) - 0.15);
         double dz = Math.abs(point.z - BlockPos.getZ(key) - 0.5);
         best = Math.min(best, lattice ? dx + dy + dz : Math.sqrt(dx * dx + dy * dy + dz * dz));
      }

      return best;
   }

   static AiPhysics.MotionState init_NodeWalker_StreamU_NodeWalker_StreamU(
      AiPhysics.MotionState body, PrintMover.Inner_StreamUtil keys, float yaw, float speed, AiPhysics.PhysicsWorld world
   ) {
      AiPhysics.MoveInput input = PrintMover.init(keys, yaw);
      double forward = input.forward() * 0.98;
      double left = input.strafe() * 0.98;
      double length = Math.max(1.0, Math.hypot(forward, left));
      double angle = Math.toRadians(yaw);
      double sin = Math.sin(angle);
      double cos = Math.cos(angle);
      double vx = body.vx();
      double vz = body.vz();
      double vy = body.vy();
      if (vx * vx + vz * vz < 9.0E-6) {
         vx = 0.0;
         vz = 0.0;
      }

      if (Math.abs(vy) < 0.003) {
         vy = 0.0;
      }

      double horizontalSpeed = speed * (keys.sprint() && keys.forward() && !keys.back() ? 2 : 1);
      vx += (left * cos - forward * sin) * horizontalSpeed / length;
      vz += (forward * cos + left * sin) * horizontalSpeed / length;
      vy += ((keys.jump() ? 1 : 0) - (keys.sneak() ? 1 : 0)) * speed * 3.0F;
      AABB box = PrintGeometry.init(body.x(), body.y(), body.z(), false);
      List<VoxelShape> shapes = world.init_List(box.expandTowards(vx, vy, vz).inflate(1.0E-7)).stream().map(Shapes::create).toList();
      double y = Shapes.collide(Axis.Y, box, shapes, vy);
      box = box.move(0.0, y, 0.0);
      double x;
      double z;
      if (Math.abs(vx) < Math.abs(vz)) {
         z = Shapes.collide(Axis.Z, box, shapes, vz);
         box = box.move(0.0, 0.0, z);
         x = Shapes.collide(Axis.X, box, shapes, vx);
      } else {
         x = Shapes.collide(Axis.X, box, shapes, vx);
         box = box.move(x, 0.0, 0.0);
         z = Shapes.collide(Axis.Z, box, shapes, vz);
      }

      return new AiPhysics.MotionState(
         body.x() + x,
         body.y() + y,
         body.z() + z,
         Math.abs(vx - x) > 1.0E-7 ? 0.0 : vx * 0.91,
         vy * 0.6,
         Math.abs(vz - z) > 1.0E-7 ? 0.0 : vz * 0.91,
         vy < 0.0 && Math.abs(vy - y) > 1.0E-7,
         0,
         Math.abs(vx - x) + Math.abs(vz - z) > 1.0E-7,
         Math.abs(vy - y) > 1.0E-7
      );
   }

   static boolean init(AiPhysics.MotionState body, Vec3 goal) {
      return body.position().distanceToSqr(goal) < 0.25 && body.speed() < 0.025 && Math.abs(body.vy()) < 0.025;
   }

   static boolean load(AiPhysics.MotionState body, Vec3 goal) {
      return body.position().distanceToSqr(goal) < 0.16 && body.speed() < 0.015 && Math.abs(body.vy()) < 0.015;
   }

   static PrintMover.Inner_StreamUtil init(PrintEnv env, Vec3 goal) {
      return init(env, goal, env.init_F());
   }

   static PrintMover.Inner_StreamUtil init(PrintEnv env, Vec3 goal, float yaw) {
      return init_CharEncoder_Stream_EventBus(env, goal, yaw, 0.4);
   }

   static PrintMover.Inner_StreamUtil init_CharEncoder_Stream_EventBus(PrintEnv env, Vec3 goal, float yaw, double tolerance) {
      return init_CharEncoder_Stream_EventBus(env, goal, yaw, tolerance, null);
   }

   static PrintMover.Inner_StreamUtil init_CharEncoder_Stream_EventBus(PrintEnv env, Vec3 goal, float yaw, double tolerance, Predicate<Vec3> continuation) {
      return init(env, goal, yaw, tolerance, continuation, null);
   }

   static PrintMover.Inner_StreamUtil init(PrintEnv env, Vec3 goal, float yaw, double tolerance, Predicate<Vec3> continuation, Predicate<Vec3> nextStep) {
      return init_CharEncoder_Stream_EventBus(env, goal, yaw, tolerance, continuation, nextStep, false);
   }

   static PrintMover.Inner_StreamUtil load(PrintEnv env, Vec3 goal, float yaw, double tolerance, Predicate<Vec3> continuation, Predicate<Vec3> nextStep) {
      return init_CharEncoder_Stream_EventBus(env, goal, yaw, tolerance, continuation, nextStep, true);
   }

   private static PrintMover.Inner_StreamUtil init_CharEncoder_Stream_EventBus_2(PrintEnv env, Vec3 goal, float yaw, Predicate<Vec3> nextStep) {
      AiPhysics.MotionState body = env.init_NodeWalker_StreamU();
      double dx = goal.x - body.x();
      double dz = goal.z - body.z();
      double distance = Math.hypot(dx, dz);
      double speed = env.sync_F();
      if (speed > 0.0 && Double.isFinite(speed)) {
         double maxSpeed = speed * 0.98 * 0.91 / 0.09 * (env.push() ? 2 : 1);
         double wantedSpeed = Math.min(maxSpeed, Math.sqrt(2.0 * speed * 0.98 * Math.max(0.0, distance - 0.6)));
         double vx = distance < 1.0E-8 ? 0.0 : dx / distance * wantedSpeed;
         double vz = distance < 1.0E-8 ? 0.0 : dz / distance * wantedSpeed;
         double vy = Math.copySign(Math.min(speed * 4.5, Math.abs(goal.y - body.y()) * 0.55), goal.y - body.y());
         boolean sprintAllowed = env.push() && distance > 1.0 && (-Math.sin(Math.toRadians(yaw)) * dx + Math.cos(Math.toRadians(yaw)) * dz) / distance > 0.7;

         record Inner_DataUtil(PrintMover.Inner_StreamUtil keys, AiPhysics.MotionState next, double score) {
         }

         ArrayList<Inner_DataUtil> candidates = new ArrayList<>(36);

         for (int forward = -1; forward <= 1; forward++) {
            for (int side = -1; side <= 1; side++) {
               for (int vertical = -1; vertical <= 1; vertical++) {
                  for (int sprint = 0; sprint <= (forward > 0 && vertical >= 0 && sprintAllowed ? 1 : 0); sprint++) {
                     PrintMover.Inner_StreamUtil keys = new PrintMover.Inner_StreamUtil(
                        forward > 0, forward < 0, side > 0, side < 0, vertical > 0, vertical < 0, sprint > 0
                     );
                     AiPhysics.MotionState next = init_NodeWalker_StreamU_NodeWalker_StreamU(body, keys, yaw, env.sync_F(), env.init_NodeWalker_CachePo());
                     double value = Math.pow(next.vx() - vx, 2.0)
                        + Math.pow(next.vz() - vz, 2.0)
                        + Math.pow(next.vy() - vy, 2.0)
                        + next.position().distanceToSqr(goal) * 1.0E-4;
                     candidates.add(new Inner_DataUtil(keys, next, value));
                  }
               }
            }
         }

         candidates.sort(Comparator.comparingDouble(Inner_DataUtil::score));

         for (Inner_DataUtil candidate : candidates) {
            if (nextStep == null || nextStep.test(candidate.next.position())) {
               AiPhysics.MotionState prior = body;
               AiPhysics.MotionState next = candidate.next;
               boolean safe = true;

               for (int tick = 0; tick <= 25; tick++) {
                  if (init(env.init_NodeWalker_CachePo(), prior, next)) {
                     safe = false;
                     break;
                  }

                  prior = next;
                  next = init_NodeWalker_StreamU_NodeWalker_StreamU(next, PrintMover.Inner_StreamUtil.NONE, yaw, env.sync_F(), env.init_NodeWalker_CachePo());
               }

               if (safe) {
                  return candidate.keys;
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static boolean init(AiPhysics.PhysicsWorld world, AiPhysics.MotionState from, AiPhysics.MotionState to) {
      int samples = Math.max(1, (int)Math.ceil(from.position().distanceTo(to.position()) / 0.25));
      if (samples > 256) {
         return true;
      }

      for (int sample = 1; sample <= samples; sample++) {
         double f = (double)sample / samples;
         Vec3 point = from.position().lerp(to.position(), f);
         AiPhysics.MotionState body = new AiPhysics.MotionState(
            point.x, point.y, point.z, to.vx(), to.vy(), to.vz(), to.onGround(), 0, false, false
         );
         if (PrintMover.init(world, from, body)) {
            return true;
         }
      }

      return false;
   }

   private static PrintMover.Inner_StreamUtil init_CharEncoder_Stream_EventBus(
      PrintEnv env, Vec3 goal, float yaw, double tolerance, Predicate<Vec3> continuation, Predicate<Vec3> nextStep, boolean transit
   ) {
      AiPhysics.MotionState body = env.init_NodeWalker_StreamU();
      AiPhysics.PhysicsWorld world = env.init_NodeWalker_CachePo();
      if (body.position().distanceToSqr(goal) > 4.0) {
         PrintMover.Inner_StreamUtil travel = init_CharEncoder_Stream_EventBus_2(env, goal, yaw, nextStep);
         if (travel != null) {
            return travel;
         }
      }

      boolean openingTurn = transit && continuation != null && body.position().distanceToSqr(goal) < 0.16 && !continuation.test(body.position());
      if (!openingTurn && body.position().distanceToSqr(goal) < tolerance * tolerance) {
         AiPhysics.MotionState coast = body;
         boolean safe = true;
         int tick = 0;

         while (true) {
            if (tick < 25) {
               coast = init_NodeWalker_StreamU_NodeWalker_StreamU(coast, PrintMover.Inner_StreamUtil.NONE, yaw, env.sync_F(), world);
               if (!PrintMover.init(world, body, coast) && (tick != 0 || nextStep == null || nextStep.test(coast.position()))) {
                  tick++;
                  continue;
               }

               safe = false;
            }

            if (safe && coast.position().distanceToSqr(goal) < tolerance * tolerance && (continuation == null || continuation.test(coast.position()))) {
               return PrintMover.Inner_StreamUtil.NONE;
            }
            break;
         }
      }

      PrintMover.Inner_StreamUtil best = PrintMover.Inner_StreamUtil.NONE;
      PrintMover.Inner_StreamUtil fine = PrintMover.Inner_StreamUtil.NONE;
      double score = 1.0 / 0.0;
      double fineScore = 1.0 / 0.0;
      boolean bestContinues = false;
      PrintMover.Inner_StreamUtil opensTurn = null;
      double turnScore = 1.0 / 0.0;
      double arrivalRadius = Math.max(0.4, tolerance);

      for (int forward = -1; forward <= 1; forward++) {
         for (int side = -1; side <= 1; side++) {
            for (int vertical = -1; vertical <= 1; vertical++) {
               PrintMover.Inner_StreamUtil keys = new PrintMover.Inner_StreamUtil(
                  forward > 0, forward < 0, side > 0, side < 0, vertical > 0, vertical < 0, false
               );
               AiPhysics.MotionState next = init_NodeWalker_StreamU_NodeWalker_StreamU(body, keys, yaw, env.sync_F(), world);
               if (!PrintMover.init(world, body, next) && (nextStep == null || nextStep.test(next.position()))) {
                  double distance = next.position().distanceToSqr(goal);
                  double fineValue = distance + 2.0 * (next.vx() * next.vx() + next.vy() * next.vy() + next.vz() * next.vz());
                  boolean canOpenTurn = openingTurn && distance < 0.16 && fineValue < turnScore && continuation.test(next.position());
                  boolean safe = true;

                  for (int i = 0; i < 25; i++) {
                     next = init_NodeWalker_StreamU_NodeWalker_StreamU(next, PrintMover.Inner_StreamUtil.NONE, yaw, env.sync_F(), world);
                     if (PrintMover.init(world, body, next)) {
                        safe = false;
                        break;
                     }
                  }

                  if (safe) {
                     if (canOpenTurn) {
                        opensTurn = keys;
                        turnScore = fineValue;
                     }

                     if (fineValue < fineScore) {
                        fineScore = fineValue;
                        fine = keys;
                     }

                     boolean continues = continuation == null
                        || next.position().distanceToSqr(goal) <= arrivalRadius * arrivalRadius && continuation.test(next.position());
                     double value = next.position().distanceToSqr(goal) + distance * 0.001 + next.speed() * next.speed();
                     if (continues && !bestContinues || continues == bestContinues && value < score) {
                        score = value;
                        best = keys;
                        bestContinues = continues;
                     }
                  }
               }
            }
         }
      }

      if (opensTurn != null) {
         return opensTurn;
      } else {
         return body.position().distanceToSqr(goal) < 1.0
               && (!bestContinues || !transit && best.equals(PrintMover.Inner_StreamUtil.NONE) && score > tolerance * tolerance)
            ? fine
            : best;
      }
   }

   record Inner_BufferHelper(List<Vec3> points, long goal) {
   }

   static final class Inner_CachePool {
      private Vec3 init_Vec3;
      private double init_D;
      private long init_J;

      boolean init(Vec3 position, Vec3 destination, long tick) {
         double distance = position.distanceToSqr(destination);
         if (this.init_Vec3 != null && !(this.init_Vec3.distanceToSqr(destination) > 1.0E-8) && !(distance + 0.0025 < this.init_D)) {
            return tick - this.init_J >= 40L;
         }

         this.init_Vec3 = destination;
         this.init_D = distance;
         this.init_J = tick;
         return false;
      }

      void init() {
         this.init_Vec3 = null;
      }
   }

   static final class Inner_ConfigStore {
      private static final int read = 256;
      private static final int write = 64;
      final PrintView init_ArrayUtil;
      final Set<Long> init_Set;
      final Vec3 init_Vec3;
      final int init_I;
      final PriorityQueue<PrintFlight.Inner_StreamUtil> init_PriorityQueue = new PriorityQueue<>(
         Comparator.comparingDouble(PrintFlight.Inner_StreamUtil::estimate)
            .thenComparing(Comparator.comparingDouble(PrintFlight.Inner_StreamUtil::cost).reversed())
            .thenComparingLong(PrintFlight.Inner_StreamUtil::key)
      );
      final Long2DoubleOpenHashMap init_Long2DoubleOpenHas = new Long2DoubleOpenHashMap();
      final long init_J;
      Vec3 load_Vec3;
      PrintFlight.Inner_ConfigStore.Inner_DataUtil init_TextFormat_ConfigS;
      boolean init_Z;
      int load_I;
      boolean load_Z;
      PrintFlight.Inner_BufferHelper init_TextFormat_BufferH;

      Inner_ConfigStore(PrintView view, Vec3 start, Set<Long> goals, int limit) {
         this(view, start, goals, null, limit);
      }

      Inner_ConfigStore(PrintView view, Vec3 start, Vec3 goal, int limit) {
         this(view, start, Set.of(BlockPos.containing(goal).asLong()), goal, limit);
      }

      private Inner_ConfigStore(PrintView view, Vec3 start, Set<Long> goals, Vec3 exactGoal, int limit) {
         this.init_ArrayUtil = view;
         this.init_Set = new LinkedHashSet<>(goals);
         this.init_Vec3 = exactGoal;
         this.init_I = limit;
         this.init_J = BlockPos.containing(start).asLong();
         this.load_Vec3 = start;
         this.init_Long2DoubleOpenHas.defaultReturnValue(1.0 / 0.0);
         if (goals.isEmpty()) {
            this.load_Z = true;
         } else if (exactGoal != null && start.distanceToSqr(exactGoal) <= 4.0 && PrintFlight.init_Z(view, start, exactGoal)) {
            this.init_TextFormat_BufferH = new PrintFlight.Inner_BufferHelper(List.of(exactGoal), BlockPos.containing(exactGoal).asLong());
            this.load_Z = true;
         } else {
            Vec3 entry = PrintFlight.init(this.init_J);
            PrintFlight.Inner_StreamUtil origin = new PrintFlight.Inner_StreamUtil(this.init_J, start, 0.0, 0.0, null);
            boolean centred = PrintFlight.init_Z(view, start, entry);
            Vec3 initial = centred ? entry : start;
            this.load_Vec3 = initial;
            double initialCost = start.distanceTo(initial);
            this.init_PriorityQueue
               .add(
                  new PrintFlight.Inner_StreamUtil(
                     this.init_J, initial, initialCost, initialCost + this.init(initial), centred && initialCost > 1.0E-7 ? origin : null
                  )
               );
            this.init_Long2DoubleOpenHas.put(this.init_J, initialCost);
         }
      }

      void init(long workBudget) {
         long started = this.init_ArrayUtil.sync();

         while (!this.load_Z && this.init_ArrayUtil.sync() - started < workBudget) {
            if (!this.init_Z && this.init_Vec3 == null && this.load_I >= 256) {
               this.init_Z = true;
               if (this.init_Set.size() <= 64) {
                  this.init_TextFormat_ConfigS = new PrintFlight.Inner_ConfigStore.Inner_DataUtil();
               }
            }

            if (this.init_TextFormat_ConfigS != null && !this.init_TextFormat_ConfigS.init_Z) {
               this.init_TextFormat_ConfigS.init(workBudget - (this.init_ArrayUtil.sync() - started));
               if (!this.init_TextFormat_ConfigS.init_Z) {
                  return;
               }

               if (this.init_TextFormat_ConfigS.load) {
                  this.load_Z = true;
                  return;
               }
            }

            if (this.init_PriorityQueue.isEmpty() || this.load_I++ >= this.init_I) {
               this.load_Z = true;
               return;
            }

            PrintFlight.Inner_StreamUtil at = this.init_PriorityQueue.remove();
            if (!(at.cost > this.init_Long2DoubleOpenHas.get(at.key) + 1.0E-7)) {
               Vec3 finish = this.init_Vec3 == null ? PrintFlight.init(at.key) : this.init_Vec3;
               boolean nearGoal = this.init_Vec3 == null ? this.init_Set.contains(at.key) : at.point.distanceToSqr(this.init_Vec3) <= 4.0;
               if (nearGoal && PrintFlight.init_Z(this.init_ArrayUtil, at.point, finish)) {
                  ArrayList<Vec3> reverse = new ArrayList<>();
                  reverse.add(finish);

                  for (PrintFlight.Inner_StreamUtil p = at; p.parent != null; p = p.parent) {
                     if (!p.point.equals(reverse.get(reverse.size() - 1))) {
                        reverse.add(p.point);
                     }
                  }

                  Collections.reverse(reverse);
                  this.init_TextFormat_BufferH = new PrintFlight.Inner_BufferHelper(
                     List.copyOf(reverse), this.init_Vec3 == null ? at.key : BlockPos.containing(this.init_Vec3).asLong()
                  );
                  this.load_Z = true;
                  return;
               }

               BlockPos cell = BlockPos.of(at.key);

               for (int[] d : PrintFlight.init_IArr2) {
                  long key = cell.offset(d[0], d[1], d[2]).asLong();
                  Vec3 next = PrintFlight.init(key);
                  double cost = at.cost + at.point.distanceTo(next);
                  if (!(cost + 1.0E-7 >= this.init_Long2DoubleOpenHas.get(key))
                     && PrintFlight.init_Z(this.init_ArrayUtil, at.point, next)
                     && (this.init_Set.contains(key) || !PrintFlight.init(this.init_ArrayUtil, next))) {
                     this.init_Long2DoubleOpenHas.put(key, cost);
                     this.init_PriorityQueue.add(new PrintFlight.Inner_StreamUtil(key, next, cost, cost + this.init(next), at));
                  }
               }
            }
         }
      }

      private double init(Vec3 point) {
         return this.init_Vec3 == null ? PrintFlight.init(point, this.init_Set) : point.distanceTo(this.init_Vec3);
      }

      final class Inner_DataUtil {
         final Iterator<Long> init_Iterator = Inner_ConfigStore.this.init_Set.iterator();
         final LongOpenHashSet init_LongOpenHashSet = new LongOpenHashSet();
         final LongArrayFIFOQueue init_LongArrayFIFOQueue = new LongArrayFIFOQueue();
         long init_J;
         Vec3 init_Vec3;
         int init_I;
         boolean init_Z;
         boolean load;

         private boolean init_Z(long key, Vec3 to) {
            int distance = Math.abs(BlockPos.getX(key) - BlockPos.getX(Inner_ConfigStore.this.init_J))
               + Math.abs(BlockPos.getY(key) - BlockPos.getY(Inner_ConfigStore.this.init_J))
               + Math.abs(BlockPos.getZ(key) - BlockPos.getZ(Inner_ConfigStore.this.init_J));
            return distance <= 1 && PrintFlight.init_Z(Inner_ConfigStore.this.init_ArrayUtil, Inner_ConfigStore.this.load_Vec3, to);
         }

         private void init_V(long key, Vec3 value) {
            if (!this.init_Z(key, value) && this.init_LongOpenHashSet.size() < 64) {
               this.init_LongOpenHashSet.add(key);
               this.init_LongArrayFIFOQueue.enqueue(key);
            } else {
               this.init_Z = true;
            }
         }

         void init(long allowance) {
            long started = Inner_ConfigStore.this.init_ArrayUtil.sync();
            int structural = 0;

            while (!this.init_Z && structural++ < 1024 && Inner_ConfigStore.this.init_ArrayUtil.sync() - started < allowance) {
               if (this.init_Iterator.hasNext()) {
                  long key = this.init_Iterator.next();
                  if (!this.init_LongOpenHashSet.contains(key) && PrintFlight.load(Inner_ConfigStore.this.init_ArrayUtil, PrintFlight.init(key))) {
                     this.init_V(key, PrintFlight.init(key));
                  }
               } else {
                  if (this.init_Vec3 == null) {
                     if (this.init_LongArrayFIFOQueue.isEmpty()) {
                        this.load = true;
                        this.init_Z = true;
                        return;
                     }

                     this.init_J = this.init_LongArrayFIFOQueue.dequeueLong();
                     this.init_Vec3 = PrintFlight.init(this.init_J);
                     this.init_I = 0;
                  }

                  if (this.init_I == PrintFlight.init_IArr2.length) {
                     this.init_Vec3 = null;
                  } else {
                     int[] d = PrintFlight.init_IArr2[this.init_I++];
                     long key = BlockPos.offset(this.init_J, d[0], d[1], d[2]);
                     if (!this.init_LongOpenHashSet.contains(key)) {
                        Vec3 predecessor = PrintFlight.init(key);
                        if (PrintFlight.load(Inner_ConfigStore.this.init_ArrayUtil, predecessor)
                           && PrintFlight.init_Z(Inner_ConfigStore.this.init_ArrayUtil, predecessor, this.init_Vec3)) {
                           this.init_V(key, predecessor);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private record Inner_StreamUtil(long key, Vec3 point, double cost, double estimate, PrintFlight.Inner_StreamUtil parent) {
   }
}
