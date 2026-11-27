package autismclient.ai.print;

import autismclient.ai.engine.AiPhysics;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

final class PrintMover {
   static final int init_I = 7;
   static final int load = 60;
   private static final int[][] init_IArr2 = new int[][]{{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
   static final boolean init_Z = !"false".equals(System.getProperty("autism.ai.print.braking", "true"));

   static boolean init(AiPhysics.MotionState s, PrintMover.Inner_DataUtil goal) {
      double dx = s.x() - goal.feet().x;
      double dz = s.z() - goal.feet().z;
      double dy = s.y() - goal.feet().y;
      return !(dx * dx + dz * dz > goal.tolerance() * goal.tolerance()) && !(Math.abs(dy) > 0.25) && s.onGround()
         ? !goal.stop() || s.vx() * s.vx() + s.vz() * s.vz() < 0.0025
         : false;
   }

   static PrintMover.Inner_StreamUtil init(AiPhysics.MotionState start, AiPhysics.PhysicsWorld world, float yaw, PrintMover.Inner_DataUtil goal) {
      double needRise = goal.feet().y - start.y();
      boolean tryJump = start.onGround() && (needRise > 0.65 || start.horizontalCollision());
      boolean tapDrop = start.onGround() && needRise < -1.0;
      PrintMover.Inner_StreamUtil best = PrintMover.Inner_StreamUtil.NONE;
      double bestCost = 1.0 / 0.0;

      for (int sneak = 0; sneak <= 1; sneak++) {
         boolean crouched = sneak == 1;
         if (!goal.crouch() || crouched) {
            for (int jump = 0; jump <= (tryJump ? 1 : 0); jump++) {
               for (int[] d : init_IArr2) {
                  PrintMover.Inner_StreamUtil keys = new PrintMover.Inner_StreamUtil(d[0] > 0, d[0] < 0, d[1] > 0, d[1] < 0, jump == 1, crouched, false);
                  if (!keys.moving() || !autismclient.ai.engine.AiFallGuard.init(world, start, init(keys, yaw))) {
                     double cost = init_D_NodeWalker_StreamU(start, world, yaw, keys, goal, false);
                     if (tapDrop && keys.moving() && jump == 0) {
                        cost = Math.min(cost, init_D_NodeWalker_StreamU(start, world, yaw, keys, goal, true));
                     }

                     if (init_Z && keys.moving() && jump == 0 && !crouched && goal.stop()) {
                        for (int held = 1; held < 7; held++) {
                           cost = Math.min(cost, init(start, world, yaw, keys, goal, held, true));
                        }
                     }

                     cost += (keys.moving() ? 1.0E-4 : 0.0) + (crouched && !goal.crouch() ? 0.02 : 0.0) + (jump == 1 ? 0.01 : 0.0);
                     if (cost < bestCost) {
                        bestCost = cost;
                        best = keys;
                     }
                  }
               }
            }
         }
      }

      return bestCost == 1.0 / 0.0 ? (goal.crouch() ? PrintMover.Inner_StreamUtil.CROUCH : PrintMover.Inner_StreamUtil.NONE) : best;
   }

   static PrintMover.Inner_StreamUtil init(
      AiPhysics.MotionState start,
      AiPhysics.PhysicsWorld world,
      float yaw,
      PrintMover.Inner_DataUtil goal,
      Predicate<AiPhysics.MotionState> works
   ) {
      PrintMover.Inner_StreamUtil best = goal.crouch() ? PrintMover.Inner_StreamUtil.CROUCH : PrintMover.Inner_StreamUtil.NONE;
      double bestCost = 1.0 / 0.0;
      boolean bestWorks = false;

      for (int[] direction : init_IArr2) {
         PrintMover.Inner_StreamUtil keys = new PrintMover.Inner_StreamUtil(
            direction[0] > 0, direction[0] < 0, direction[1] > 0, direction[1] < 0, false, goal.crouch(), false
         );
         if ((!keys.moving() || !autismclient.ai.engine.AiFallGuard.init(world, start, init(keys, yaw)))
            && Double.isFinite(init_D_NodeWalker_StreamU(start, world, yaw, keys, goal, true))) {
            AiPhysics.MotionState state = AiPhysics.stepCompat(start, init(keys, yaw), world);
            boolean workable = works.test(state);
            AiPhysics.MoveInput coast = init(goal.crouch() ? PrintMover.Inner_StreamUtil.CROUCH : PrintMover.Inner_StreamUtil.NONE, yaw);

            for (int tick = 1; tick < 7; tick++) {
               state = AiPhysics.stepCompat(state, coast, world);
            }

            double distance = state.position().distanceToSqr(goal.feet());
            double cost = distance + 4.0 * (state.vx() * state.vx() + state.vz() * state.vz());
            if (workable && !bestWorks || workable == bestWorks && cost < bestCost) {
               bestCost = cost;
               best = keys;
               bestWorks = workable;
            }
         }
      }

      return best;
   }

   static double init_D_NodeWalker_StreamU(
      AiPhysics.MotionState start,
      AiPhysics.PhysicsWorld world,
      float yaw,
      PrintMover.Inner_StreamUtil keys,
      PrintMover.Inner_DataUtil goal,
      boolean tap
   ) {
      return init(start, world, yaw, keys, goal, tap ? 1 : 7, false);
   }

   static double init(
      AiPhysics.MotionState start,
      AiPhysics.PhysicsWorld world,
      float yaw,
      PrintMover.Inner_StreamUtil keys,
      PrintMover.Inner_DataUtil goal,
      int heldTicks,
      boolean crouchedCoast
   ) {
      AiPhysics.MotionState s = start;
      AiPhysics.MoveInput held = init(keys, yaw);
      AiPhysics.MoveInput moving = init(
         new PrintMover.Inner_StreamUtil(keys.forward(), keys.back(), keys.left(), keys.right(), false, keys.sneak(), false), yaw
      );
      AiPhysics.MoveInput released = init(new PrintMover.Inner_StreamUtil(false, false, false, false, false, keys.sneak() || crouchedCoast, false), yaw);
      AiPhysics.MoveInput jumpless = heldTicks < 7 ? released : moving;
      double best = 1.0 / 0.0;
      double peak = Double.isFinite(goal.fallPeak()) ? Math.max(start.y(), goal.fallPeak()) : start.y();
      long startA = BlockPos.asLong((int)Math.floor(start.x()), (int)Math.floor(start.y()), (int)Math.floor(start.z()));
      long startB = BlockPos.asLong((int)Math.floor(start.x()), (int)Math.floor(start.y() - 0.2), (int)Math.floor(start.z()));

      for (int t = 1; t <= 7; t++) {
         AiPhysics.MotionState next = AiPhysics.stepCompat(s, t == 1 ? held : (t <= heldTicks ? moving : released), world);
         if (next.y() < goal.floor() - 0.05) {
            return 1.0 / 0.0;
         }

         if (init_Z_NodeWalker_CachePo(world, next.x(), next.y(), next.z(), startA, startB)
            || init_Z_NodeWalker_CachePo(world, next.x(), next.y() - 0.2, next.z(), startA, startB)) {
            return 1.0 / 0.0;
         }

         if (init(world, start, next, true)) {
            return 1.0 / 0.0;
         }

         if (next.onGround() && !s.onGround() && peak - next.y() > 3.000001) {
            return 1.0 / 0.0;
         }

         peak = next.onGround() ? next.y() : Math.max(peak, next.y());
         s = next;
         double dx = s.x() - goal.feet().x;
         double dz = s.z() - goal.feet().z;
         double dy = s.y() - goal.feet().y;
         double cost = dx * dx + dz * dz + (s.onGround() ? 1.0 : 0.25) * dy * dy;
         if (goal.stop()) {
            cost += 4.0 * (s.vx() * s.vx() + s.vz() * s.vz());
         }

         cost += 0.002 * t;
         best = Math.min(best, cost);
      }

      for (int t = 0; !s.onGround() && t < 60; t++) {
         s = AiPhysics.stepCompat(s, jumpless, world);
         peak = Math.max(peak, s.y());
         if (s.onGround() && peak - s.y() > 3.000001) {
            return 1.0 / 0.0;
         }

         if (s.y() < goal.floor() - 0.05) {
            return 1.0 / 0.0;
         }

         if (world.isInvisibleLightBlock((int)Math.floor(s.x()), (int)Math.floor(s.y()), (int)Math.floor(s.z())) || init(world, start, s, true)) {
            return 1.0 / 0.0;
         }
      }

      return !s.onGround() ? 1.0 / 0.0 : best;
   }

   static boolean init(AiPhysics.PhysicsWorld world, AiPhysics.MotionState start, AiPhysics.MotionState next) {
      return init(world, start, next, false);
   }

   private static boolean init(AiPhysics.PhysicsWorld world, AiPhysics.MotionState start, AiPhysics.MotionState next, boolean dry) {
      double r = 0.299;
      int sx0 = (int)Math.floor(start.x() - r);
      int sx1 = (int)Math.floor(start.x() + r);
      int sz0 = (int)Math.floor(start.z() - r);
      int sz1 = (int)Math.floor(start.z() + r);
      int sy0 = (int)Math.floor(start.y() + 0.01);
      int sy1 = (int)Math.floor(start.y() + 0.9);
      int sy2 = (int)Math.floor(start.y() + 1.75);
      int x0 = (int)Math.floor(next.x() - r);
      int x1 = (int)Math.floor(next.x() + r);
      int z0 = (int)Math.floor(next.z() - r);
      int z1 = (int)Math.floor(next.z() + r);

      for (int x = x0; x <= x1; x++) {
         for (int z = z0; z <= z1; z++) {
            for (int sample = 0; sample < 3; sample++) {
               int y = (int)Math.floor(next.y() + (sample == 0 ? 0.01 : (sample == 1 ? 0.9 : 1.75)));
               if ((x < sx0 || x > sx1 || z < sz0 || z > sz1 || y != sy0 && y != sy1 && y != sy2) && (world.isInvisibleLightBlock(x, y, z) || dry && world.isWater(x, y, z))) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   static LongOpenHashSet init(double x, double y, double z) {
      LongOpenHashSet out = new LongOpenHashSet(12);
      double r = 0.299;

      for (double dx : new double[]{-r, r}) {
         for (double dz : new double[]{-r, r}) {
            for (double dy : new double[]{0.01, 0.9, 1.75}) {
               out.add(BlockPos.asLong((int)Math.floor(x + dx), (int)Math.floor(y + dy), (int)Math.floor(z + dz)));
            }
         }
      }

      return out;
   }

   private static boolean init_Z_NodeWalker_CachePo(AiPhysics.PhysicsWorld world, double x, double y, double z, long skipA, long skipB) {
      int bx = (int)Math.floor(x);
      int by = (int)Math.floor(y);
      int bz = (int)Math.floor(z);
      long k = BlockPos.asLong(bx, by, bz);
      return k != skipA && k != skipB && world.isInvisibleLightBlock(bx, by, bz);
   }

   static AiPhysics.MoveInput init(PrintMover.Inner_StreamUtil k, float yaw) {
      double forward = (k.forward() ? 1 : 0) - (k.back() ? 1 : 0);
      double strafe = (k.left() ? 1 : 0) - (k.right() ? 1 : 0);
      return new AiPhysics.MoveInput(forward, strafe, k.jump(), k.sprint(), k.sneak(), yaw);
   }

   record Inner_DataUtil(Vec3 feet, double tolerance, double floor, boolean crouch, boolean stop, double fallPeak) {
      Inner_DataUtil(Vec3 feet, double tolerance, double floor, boolean crouch, boolean stop) {
         this(feet, tolerance, floor, crouch, stop, 0.0 / 0.0);
      }
   }

   record Inner_StreamUtil(boolean forward, boolean back, boolean left, boolean right, boolean jump, boolean sneak, boolean sprint) {
      static final PrintMover.Inner_StreamUtil NONE = new PrintMover.Inner_StreamUtil(false, false, false, false, false, false, false);
      static final PrintMover.Inner_StreamUtil CROUCH = new PrintMover.Inner_StreamUtil(false, false, false, false, false, true, false);

      boolean moving() {
         return this.forward || this.back || this.left || this.right || this.jump;
      }
   }
}
