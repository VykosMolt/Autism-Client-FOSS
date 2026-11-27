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
   private static final int[][] CARDINAL_OFFSETS = new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
   static final double LAVA_MARGIN_XZ = 0.3;
   static final double LAVA_MARGIN_Y = 0.4;
   private static final AABB UNIT_BLOCK_BOX = new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);

   static Vec3 cellFlightPoint(long key) {
      return new Vec3(BlockPos.getX(key) + 0.5, BlockPos.getY(key) + 0.15, BlockPos.getZ(key) + 0.5);
   }

   static boolean isNearObservedLava(PrintView view, Vec3 point) {
      if (view.observedLavaCells.isEmpty()) {
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
               if (view.observedLavaCells.contains(BlockPos.asLong(x, y, z))) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   static boolean isFlightPointSafe(PrintView view, Vec3 point) {
      AABB box = PrintGeometry.init(point.x, point.y, point.z, false);
      if (!view.isBoxClear(box)) {
         return false;
      }

      for (BlockPos p : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ), BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
         BlockState state = view.getBlockState(p);
         if (state == PrintView.UNLOADED_SENTINEL || PrintView.isHazardBlock(state)) {
            return false;
         }
      }

      return true;
   }

   static boolean isFlightSegmentClear(PrintView view, Vec3 from, Vec3 to) {
      return findSegmentBlocker(view, from, to) == null;
   }

   static BlockPos findSegmentBlocker(PrintView view, Vec3 from, Vec3 to) {
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
                     if ((state == PrintView.UNLOADED_SENTINEL || PrintView.isHazardBlock(state)) && sweptBoxIntersectsBlock(start, end, UNIT_BLOCK_BOX, x, y, z, -1.0E-7)) {
                        return position.immutable();
                     }

                     for (AABB box : view.getCollisionBoxesForState(state, position)) {
                        if (sweptBoxIntersectsBlock(start, end, box, x, y, z, 1.0E-6)) {
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

   private static boolean sweptBoxIntersectsBlock(Vec3 from, Vec3 to, AABB box, int x, int y, int z, double inset) {
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

   static int advanceVisibleWaypoint(PrintView view, Vec3 body, List<Vec3> points, int current) {
      for (int next = Math.min(points.size() - 1, current + 8); next > current; next--) {
         if (isFlightSegmentClear(view, body, points.get(next))) {
            return next;
         }
      }

      return current;
   }

   static PrintFlight.Inner_BufferHelper findFlightPath(PrintView view, Vec3 start, Set<Long> goals, int limit) {
      PrintFlight.Inner_ConfigStore search = new PrintFlight.Inner_ConfigStore(view, start, goals, limit);
      search.stepSearch(9223372036854775807L);
      return search.result;
   }

   private static double estimateDistanceToGoals(Vec3 point, Set<Long> goals) {
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

   static AiPhysics.MotionState simulateFlightTick(
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
      List<VoxelShape> shapes = world.collisionBoxes(box.expandTowards(vx, vy, vz).inflate(1.0E-7)).stream().map(Shapes::create).toList();
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

   static boolean hasArrivedAtGoal(AiPhysics.MotionState body, Vec3 goal) {
      return body.position().distanceToSqr(goal) < 0.25 && body.speed() < 0.025 && Math.abs(body.vy()) < 0.025;
   }

   static boolean hasSettledAtGoal(AiPhysics.MotionState body, Vec3 goal) {
      return body.position().distanceToSqr(goal) < 0.16 && body.speed() < 0.015 && Math.abs(body.vy()) < 0.015;
   }

   static PrintMover.Inner_StreamUtil chooseFlightInput(PrintEnv env, Vec3 goal) {
      return chooseFlightInput(env, goal, env.playerYaw());
   }

   static PrintMover.Inner_StreamUtil chooseFlightInput(PrintEnv env, Vec3 goal, float yaw) {
      return chooseFlightInput(env, goal, yaw, 0.4);
   }

   static PrintMover.Inner_StreamUtil chooseFlightInput(PrintEnv env, Vec3 goal, float yaw, double tolerance) {
      return chooseFlightInput(env, goal, yaw, tolerance, null);
   }

   static PrintMover.Inner_StreamUtil chooseFlightInput(PrintEnv env, Vec3 goal, float yaw, double tolerance, Predicate<Vec3> continuation) {
      return chooseFlightInput(env, goal, yaw, tolerance, continuation, null);
   }

   static PrintMover.Inner_StreamUtil chooseFlightInput(PrintEnv env, Vec3 goal, float yaw, double tolerance, Predicate<Vec3> continuation, Predicate<Vec3> nextStep) {
      return solveFlightInput(env, goal, yaw, tolerance, continuation, nextStep, false);
   }

   static PrintMover.Inner_StreamUtil chooseTransitFlightInput(PrintEnv env, Vec3 goal, float yaw, double tolerance, Predicate<Vec3> continuation, Predicate<Vec3> nextStep) {
      return solveFlightInput(env, goal, yaw, tolerance, continuation, nextStep, true);
   }

   private static PrintMover.Inner_StreamUtil chooseCruiseFlightInput(PrintEnv env, Vec3 goal, float yaw, Predicate<Vec3> nextStep) {
      AiPhysics.MotionState body = env.playerMotionState();
      double dx = goal.x - body.x();
      double dz = goal.z - body.z();
      double distance = Math.hypot(dx, dz);
      double speed = env.flyingSpeed();
      if (speed > 0.0 && Double.isFinite(speed)) {
         double maxSpeed = speed * 0.98 * 0.91 / 0.09 * (env.canSprint() ? 2 : 1);
         double wantedSpeed = Math.min(maxSpeed, Math.sqrt(2.0 * speed * 0.98 * Math.max(0.0, distance - 0.6)));
         double vx = distance < 1.0E-8 ? 0.0 : dx / distance * wantedSpeed;
         double vz = distance < 1.0E-8 ? 0.0 : dz / distance * wantedSpeed;
         double vy = Math.copySign(Math.min(speed * 4.5, Math.abs(goal.y - body.y()) * 0.55), goal.y - body.y());
         boolean sprintAllowed = env.canSprint() && distance > 1.0 && (-Math.sin(Math.toRadians(yaw)) * dx + Math.cos(Math.toRadians(yaw)) * dz) / distance > 0.7;

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
                     AiPhysics.MotionState next = simulateFlightTick(body, keys, yaw, env.flyingSpeed(), env.livePhysicsWorld());
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
                  if (segmentHitsHazard(env.livePhysicsWorld(), prior, next)) {
                     safe = false;
                     break;
                  }

                  prior = next;
                  next = simulateFlightTick(next, PrintMover.Inner_StreamUtil.NONE, yaw, env.flyingSpeed(), env.livePhysicsWorld());
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

   private static boolean segmentHitsHazard(AiPhysics.PhysicsWorld world, AiPhysics.MotionState from, AiPhysics.MotionState to) {
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

   private static PrintMover.Inner_StreamUtil solveFlightInput(
      PrintEnv env, Vec3 goal, float yaw, double tolerance, Predicate<Vec3> continuation, Predicate<Vec3> nextStep, boolean transit
   ) {
      AiPhysics.MotionState body = env.playerMotionState();
      AiPhysics.PhysicsWorld world = env.livePhysicsWorld();
      if (body.position().distanceToSqr(goal) > 4.0) {
         PrintMover.Inner_StreamUtil travel = chooseCruiseFlightInput(env, goal, yaw, nextStep);
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
               coast = simulateFlightTick(coast, PrintMover.Inner_StreamUtil.NONE, yaw, env.flyingSpeed(), world);
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
               AiPhysics.MotionState next = simulateFlightTick(body, keys, yaw, env.flyingSpeed(), world);
               if (!PrintMover.init(world, body, next) && (nextStep == null || nextStep.test(next.position()))) {
                  double distance = next.position().distanceToSqr(goal);
                  double fineValue = distance + 2.0 * (next.vx() * next.vx() + next.vy() * next.vy() + next.vz() * next.vz());
                  boolean canOpenTurn = openingTurn && distance < 0.16 && fineValue < turnScore && continuation.test(next.position());
                  boolean safe = true;

                  for (int i = 0; i < 25; i++) {
                     next = simulateFlightTick(next, PrintMover.Inner_StreamUtil.NONE, yaw, env.flyingSpeed(), world);
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
      private Vec3 lastDestination;
      private double bestDistanceSq;
      private long lastProgressTick;

      boolean isStalled(Vec3 position, Vec3 destination, long tick) {
         double distance = position.distanceToSqr(destination);
         if (this.lastDestination != null && !(this.lastDestination.distanceToSqr(destination) > 1.0E-8) && !(distance + 0.0025 < this.bestDistanceSq)) {
            return tick - this.lastProgressTick >= 40L;
         }

         this.lastDestination = destination;
         this.bestDistanceSq = distance;
         this.lastProgressTick = tick;
         return false;
      }

      void reset() {
         this.lastDestination = null;
      }
   }

   static final class Inner_ConfigStore {
      private static final int REVERSE_PROBE_AFTER_EXPANSIONS = 256;
      private static final int MAX_REVERSE_PROBE_GOALS = 64;
      final PrintView view;
      final Set<Long> goalCells;
      final Vec3 exactGoal;
      final int maxExpansions;
      final PriorityQueue<PrintFlight.Inner_StreamUtil> openQueue = new PriorityQueue<>(
         Comparator.comparingDouble(PrintFlight.Inner_StreamUtil::estimate)
            .thenComparing(Comparator.comparingDouble(PrintFlight.Inner_StreamUtil::cost).reversed())
            .thenComparingLong(PrintFlight.Inner_StreamUtil::key)
      );
      final Long2DoubleOpenHashMap bestCostByCell = new Long2DoubleOpenHashMap();
      final long startCellKey;
      Vec3 initialPoint;
      PrintFlight.Inner_ConfigStore.Inner_DataUtil reverseProbe;
      boolean reverseProbeStarted;
      int expandedNodes;
      boolean finished;
      PrintFlight.Inner_BufferHelper result;

      Inner_ConfigStore(PrintView view, Vec3 start, Set<Long> goals, int limit) {
         this(view, start, goals, null, limit);
      }

      Inner_ConfigStore(PrintView view, Vec3 start, Vec3 goal, int limit) {
         this(view, start, Set.of(BlockPos.containing(goal).asLong()), goal, limit);
      }

      private Inner_ConfigStore(PrintView view, Vec3 start, Set<Long> goals, Vec3 exactGoal, int limit) {
         this.view = view;
         this.goalCells = new LinkedHashSet<>(goals);
         this.exactGoal = exactGoal;
         this.maxExpansions = limit;
         this.startCellKey = BlockPos.containing(start).asLong();
         this.initialPoint = start;
         this.bestCostByCell.defaultReturnValue(1.0 / 0.0);
         if (goals.isEmpty()) {
            this.finished = true;
         } else if (exactGoal != null && start.distanceToSqr(exactGoal) <= 4.0 && PrintFlight.isFlightSegmentClear(view, start, exactGoal)) {
            this.result = new PrintFlight.Inner_BufferHelper(List.of(exactGoal), BlockPos.containing(exactGoal).asLong());
            this.finished = true;
         } else {
            Vec3 entry = PrintFlight.cellFlightPoint(this.startCellKey);
            PrintFlight.Inner_StreamUtil origin = new PrintFlight.Inner_StreamUtil(this.startCellKey, start, 0.0, 0.0, null);
            boolean centred = PrintFlight.isFlightSegmentClear(view, start, entry);
            Vec3 initial = centred ? entry : start;
            this.initialPoint = initial;
            double initialCost = start.distanceTo(initial);
            this.openQueue
               .add(
                  new PrintFlight.Inner_StreamUtil(
                     this.startCellKey, initial, initialCost, initialCost + this.heuristic(initial), centred && initialCost > 1.0E-7 ? origin : null
                  )
               );
            this.bestCostByCell.put(this.startCellKey, initialCost);
         }
      }

      void stepSearch(long workBudget) {
         long started = this.view.workUnits();

         while (!this.finished && this.view.workUnits() - started < workBudget) {
            if (!this.reverseProbeStarted && this.exactGoal == null && this.expandedNodes >= 256) {
               this.reverseProbeStarted = true;
               if (this.goalCells.size() <= 64) {
                  this.reverseProbe = new PrintFlight.Inner_ConfigStore.Inner_DataUtil();
               }
            }

            if (this.reverseProbe != null && !this.reverseProbe.done) {
               this.reverseProbe.stepProbe(workBudget - (this.view.workUnits() - started));
               if (!this.reverseProbe.done) {
                  return;
               }

               if (this.reverseProbe.provedUnreachable) {
                  this.finished = true;
                  return;
               }
            }

            if (this.openQueue.isEmpty() || this.expandedNodes++ >= this.maxExpansions) {
               this.finished = true;
               return;
            }

            PrintFlight.Inner_StreamUtil at = this.openQueue.remove();
            if (!(at.cost > this.bestCostByCell.get(at.key) + 1.0E-7)) {
               Vec3 finish = this.exactGoal == null ? PrintFlight.cellFlightPoint(at.key) : this.exactGoal;
               boolean nearGoal = this.exactGoal == null ? this.goalCells.contains(at.key) : at.point.distanceToSqr(this.exactGoal) <= 4.0;
               if (nearGoal && PrintFlight.isFlightSegmentClear(this.view, at.point, finish)) {
                  ArrayList<Vec3> reverse = new ArrayList<>();
                  reverse.add(finish);

                  for (PrintFlight.Inner_StreamUtil p = at; p.parent != null; p = p.parent) {
                     if (!p.point.equals(reverse.get(reverse.size() - 1))) {
                        reverse.add(p.point);
                     }
                  }

                  Collections.reverse(reverse);
                  this.result = new PrintFlight.Inner_BufferHelper(
                     List.copyOf(reverse), this.exactGoal == null ? at.key : BlockPos.containing(this.exactGoal).asLong()
                  );
                  this.finished = true;
                  return;
               }

               BlockPos cell = BlockPos.of(at.key);

               for (int[] d : PrintFlight.CARDINAL_OFFSETS) {
                  long key = cell.offset(d[0], d[1], d[2]).asLong();
                  Vec3 next = PrintFlight.cellFlightPoint(key);
                  double cost = at.cost + at.point.distanceTo(next);
                  if (!(cost + 1.0E-7 >= this.bestCostByCell.get(key))
                     && PrintFlight.isFlightSegmentClear(this.view, at.point, next)
                     && (this.goalCells.contains(key) || !PrintFlight.isNearObservedLava(this.view, next))) {
                     this.bestCostByCell.put(key, cost);
                     this.openQueue.add(new PrintFlight.Inner_StreamUtil(key, next, cost, cost + this.heuristic(next), at));
                  }
               }
            }
         }
      }

      private double heuristic(Vec3 point) {
         return this.exactGoal == null ? PrintFlight.estimateDistanceToGoals(point, this.goalCells) : point.distanceTo(this.exactGoal);
      }

      final class Inner_DataUtil {
         final Iterator<Long> goalIterator = Inner_ConfigStore.this.goalCells.iterator();
         final LongOpenHashSet visited = new LongOpenHashSet();
         final LongArrayFIFOQueue frontier = new LongArrayFIFOQueue();
         long currentKey;
         Vec3 currentPoint;
         int nextDirIndex;
         boolean done;
         boolean provedUnreachable;

         private boolean reachesStart(long key, Vec3 to) {
            int distance = Math.abs(BlockPos.getX(key) - BlockPos.getX(Inner_ConfigStore.this.startCellKey))
               + Math.abs(BlockPos.getY(key) - BlockPos.getY(Inner_ConfigStore.this.startCellKey))
               + Math.abs(BlockPos.getZ(key) - BlockPos.getZ(Inner_ConfigStore.this.startCellKey));
            return distance <= 1 && PrintFlight.isFlightSegmentClear(Inner_ConfigStore.this.view, Inner_ConfigStore.this.initialPoint, to);
         }

         private void enqueueOrComplete(long key, Vec3 value) {
            if (!this.reachesStart(key, value) && this.visited.size() < 64) {
               this.visited.add(key);
               this.frontier.enqueue(key);
            } else {
               this.done = true;
            }
         }

         void stepProbe(long allowance) {
            long started = Inner_ConfigStore.this.view.workUnits();
            int structural = 0;

            while (!this.done && structural++ < 1024 && Inner_ConfigStore.this.view.workUnits() - started < allowance) {
               if (this.goalIterator.hasNext()) {
                  long key = this.goalIterator.next();
                  if (!this.visited.contains(key) && PrintFlight.isFlightPointSafe(Inner_ConfigStore.this.view, PrintFlight.cellFlightPoint(key))) {
                     this.enqueueOrComplete(key, PrintFlight.cellFlightPoint(key));
                  }
               } else {
                  if (this.currentPoint == null) {
                     if (this.frontier.isEmpty()) {
                        this.provedUnreachable = true;
                        this.done = true;
                        return;
                     }

                     this.currentKey = this.frontier.dequeueLong();
                     this.currentPoint = PrintFlight.cellFlightPoint(this.currentKey);
                     this.nextDirIndex = 0;
                  }

                  if (this.nextDirIndex == PrintFlight.CARDINAL_OFFSETS.length) {
                     this.currentPoint = null;
                  } else {
                     int[] d = PrintFlight.CARDINAL_OFFSETS[this.nextDirIndex++];
                     long key = BlockPos.offset(this.currentKey, d[0], d[1], d[2]);
                     if (!this.visited.contains(key)) {
                        Vec3 predecessor = PrintFlight.cellFlightPoint(key);
                        if (PrintFlight.isFlightPointSafe(Inner_ConfigStore.this.view, predecessor)
                           && PrintFlight.isFlightSegmentClear(Inner_ConfigStore.this.view, predecessor, this.currentPoint)) {
                           this.enqueueOrComplete(key, predecessor);
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
