package autismclient.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.DoubleSupplier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class ShieldHitWindowHelper {
   static final int init = 3;

   private ShieldHitWindowHelper() {
   }

   static double unusedRaiseWindowTicks(double raiseTicks) {
      return Math.min(10.0, 1.0 + Math.max(0.0, raiseTicks));
   }

   static ShieldHitWindowHelper.ShieldAction init_l_CachePool_Z(boolean threat, boolean raised, int ownHurtTime, int raiseTicks, int ticksUntilOwnHit) {
      if (threat && ticksUntilOwnHit > 0) {
         int raise = Math.max(0, raiseTicks);
         if (raised) {
            return ticksUntilOwnHit <= 1 ? ShieldHitWindowHelper.ShieldAction.DOWN : ShieldHitWindowHelper.ShieldAction.HOLD;
         } else {
            return ticksUntilOwnHit <= raise + 1 ? ShieldHitWindowHelper.ShieldAction.DOWN : ShieldHitWindowHelper.ShieldAction.RAISE;
         }
      } else {
         return ShieldHitWindowHelper.ShieldAction.DOWN;
      }
   }

   static ShieldHitWindowHelper.ShieldAction init(boolean threat, boolean raised, int raiseTicks, int ticksUntilClick) {
      return !raised && threat && raiseTicks == 0 && ticksUntilClick == 1
         ? ShieldHitWindowHelper.ShieldAction.RAISE
         : init_l_CachePool_Z(threat, raised, 0, raiseTicks, ticksUntilClick);
   }

   static double load(double clientTickMs) {
      return Double.isFinite(clientTickMs) && !(clientTickMs <= 0.0) ? Math.clamp(50.0 / clientTickMs, 0.25, 4.0) : 1.0;
   }

   static Vec3 init(Vec3 delta) {
      return Double.isFinite(delta.lengthSqr()) && delta.lengthSqr() <= 2.25 ? delta : Vec3.ZERO;
   }

   static Vec3 init(AABB box, Vec3 eye) {
      return new Vec3(Math.clamp(eye.x, box.minX, box.maxX), Math.clamp(eye.y, box.minY, box.maxY), Math.clamp(eye.z, box.minZ, box.maxZ));
   }

   static boolean init(Vec3 eye, Vec3 view, AABB victim) {
      Vec3 direction = view.normalize();
      if (victim.contains(eye)) {
         return true;
      }

      Vec3 closest = init(victim, eye);
      ArrayList<Vec3> points = new ArrayList<>();
      points.add(closest);
      points.add(victim.getCenter());

      for (double x : new double[]{victim.minX, victim.maxX}) {
         for (double y : new double[]{victim.minY, victim.maxY, Math.clamp(eye.y, victim.minY, victim.maxY)}) {
            for (double z : new double[]{victim.minZ, victim.maxZ}) {
               points.add(new Vec3(x, y, z));
            }
         }
      }

      for (Vec3 point : points) {
         Vec3 delta = point.subtract(eye);
         if (delta.lengthSqr() < 1.0E-8 || delta.normalize().dot(direction) >= 0.499999999) {
            return true;
         }
      }

      return false;
   }

   static boolean rayHitsInflated(Vec3 eye, Vec3 view, AABB victim, double reach, double margin) {
      AABB grown = victim.inflate(margin);
      if (grown.contains(eye)) {
         return true;
      } else {
         return view.lengthSqr() < 1.0E-8 ? false : grown.clip(eye, eye.add(view.normalize().scale(reach + margin + 1.0))).isPresent();
      }
   }

   static boolean isInReachWindow(Vec3 eye, AABB victim, double minReach, double maxReach, double margin) {
      double near = Math.sqrt(victim.distanceToSqr(eye));
      double far = eye.distanceTo(
         new Vec3(
            eye.x < victim.getCenter().x ? victim.maxX : victim.minX,
            eye.y < victim.getCenter().y ? victim.maxY : victim.minY,
            eye.z < victim.getCenter().z ? victim.maxZ : victim.minZ
         )
      );
      return near <= maxReach + margin && far + margin >= minReach;
   }

   static boolean unusedAimedInReachWindow(Vec3 eye, Vec3 view, AABB victim, double minReach, double maxReach, double margin) {
      return init(eye, view, victim) && isInReachWindow(eye, victim, minReach, maxReach + 0.5, margin);
   }

   static int init(AABB body, Vec3 motion, ShieldHitWindowHelper.CollisionClipper collision) {
      for (int step = 0; step <= 1; step++) {
         Vec3 moved = collision.clip(body, motion);
         if (motion.y < 0.0 && Math.abs(moved.y - motion.y) > 1.0E-5) {
            return 2147483647;
         }

         if (motion.y < -1.0E-5 && moved.y < -1.0E-5) {
            return step;
         }

         body = body.move(moved);
         motion = new Vec3(
            Math.abs(moved.x - motion.x) > 1.0E-5 ? 0.0 : motion.x * 0.91,
            ((Math.abs(moved.y - motion.y) > 1.0E-5 ? 0.0 : motion.y) - 0.08) * 0.98,
            Math.abs(moved.z - motion.z) > 1.0E-5 ? 0.0 : motion.z * 0.91
         );
      }

      return 2147483647;
   }

   static final class HitWindowTracker {
      static final int AFTER_HIT_WINDOW_TICKS = 60;
      private final Map<UUID, ShieldHitWindowHelper.HitWindowTracker.HitWindow> windows = new HashMap<>();
      private double timeScale = 1.0;

      boolean init_Z(UUID id, int hurt, int tick, boolean afterFirstHit) {
         if (!afterFirstHit) {
            return true;
         } else {
            ShieldHitWindowHelper.HitWindowTracker.HitWindow w = this.windows.get(id);
            if (w == null) {
               return false;
            } else {
               this.observeHurt(w, hurt, tick);
               if (!withinAfterHitWindow(w, tick)) {
                  w.poll = -2147483648;
                  return false;
               } else {
                  return true;
               }
            }
         }
      }

      void init() {
         for (ShieldHitWindowHelper.HitWindowTracker.HitWindow w : this.windows.values()) {
            w.poll = -2147483648;
         }
      }

      void init_V_UUID_I(UUID id, int tick) {
         ShieldHitWindowHelper.HitWindowTracker.HitWindow w = this.windows.get(id);
         if (w != null) {
            w.poll = tick;
         }
      }

      private static boolean withinAfterHitWindow(ShieldHitWindowHelper.HitWindowTracker.HitWindow w, int tick) {
         long age = (long)tick - w.poll;
         return w.poll != -2147483648 && age >= 0L && age < 60L;
      }

      void init(double scale) {
         this.timeScale = Double.isFinite(scale) ? Math.clamp(scale, 0.25, 4.0) : 1.0;
      }

      boolean canDamageNow(UUID id, int hurt, int tick, double damage) {
         return this.canAttackFor(id, hurt, tick, damage, false);
      }

      boolean canAttackFor(UUID id, int hurt, int tick, double damage, boolean movingOpportunity) {
         ShieldHitWindowHelper.HitWindowTracker.HitWindow w = this.windows.computeIfAbsent(id, ignored -> new ShieldHitWindowHelper.HitWindowTracker.HitWindow());
         this.observeHurt(w, hurt, tick);
         if (tick == w.lastAttackTick) {
            return false;
         } else if (tick < openTick(w, hurt, movingOpportunity)) {
            return false;
         } else {
            return w.ownWindow && damage > w.windowDamage + 0.01 ? true : tick >= w.windowCloseTick;
         }
      }

      boolean load(UUID id, int hurt, int tick, double damage) {
         ShieldHitWindowHelper.HitWindowTracker.HitWindow w = this.windows.computeIfAbsent(id, ignored -> new ShieldHitWindowHelper.HitWindowTracker.HitWindow());
         this.observeHurt(w, hurt, tick);
         if (tick == w.lastAttackTick) {
            return false;
         } else {
            return w.ownWindow && damage > w.windowDamage + 0.01 ? true : tick >= w.windowCloseTick;
         }
      }

      int ticksUntilOpen(UUID id, int hurt, int tick, boolean strict) {
         return this.ticksUntilOpenFor(id, hurt, tick, strict, false);
      }

      int init_I(UUID id, int hurt, int tick, double damage) {
         int ready = this.ticksUntilOpen(id, hurt, tick, false);
         ShieldHitWindowHelper.HitWindowTracker.HitWindow w = this.windows.get(id);
         return w.ownWindow && damage > w.windowDamage + 0.01 ? (tick == w.lastAttackTick ? 1 : 0) : ready;
      }

      int ticksUntilOpenFor(UUID id, int hurt, int tick, boolean strict, boolean movingOpportunity) {
         ShieldHitWindowHelper.HitWindowTracker.HitWindow w = this.windows.computeIfAbsent(id, ignored -> new ShieldHitWindowHelper.HitWindowTracker.HitWindow());
         this.observeHurt(w, hurt, tick);
         int open = strict ? Math.max(openTick(w, hurt, movingOpportunity), w.windowCloseTick) : w.windowCloseTick;
         if (w.lastAttackTick == tick) {
            open = Math.max(open, tick + 1);
         }

         return Math.max(0, open - tick);
      }

      private static int openTick(ShieldHitWindowHelper.HitWindowTracker.HitWindow w, int hurt, boolean movingOpportunity) {
         return movingOpportunity && hurt == 0 && !w.hitInsideWindow ? Math.min(w.windowOpenTick, w.hitTick + 2) : w.windowOpenTick;
      }

      boolean unusedShouldWait(UUID id, int tick, int waitChance, DoubleSupplier random) {
         ShieldHitWindowHelper.HitWindowTracker.HitWindow w = this.windows.get(id);
         if (w != null && tick != w.lastAttackTick && tick >= w.windowOpenTick && tick < w.windowCloseTick) {
            int chance = Math.clamp(waitChance, 10, 100);
            if (chance == 100) {
               return false;
            }

            if (w.waitRollTick != tick || w.waitRollChance != chance) {
               w.waitRollTick = tick;
               w.waitRollChance = chance;
               w.waitRoll = random.getAsDouble() >= chance * 0.01;
            }

            return w.waitRoll;
         } else {
            return false;
         }
      }

      void init_V(UUID id, int hurt, int tick, double damage) {
         ShieldHitWindowHelper.HitWindowTracker.HitWindow w = this.windows.computeIfAbsent(id, ignored -> new ShieldHitWindowHelper.HitWindowTracker.HitWindow());
         this.observeHurt(w, hurt, tick);
         w.lastAttackTick = tick;
         if (tick < w.windowCloseTick) {
            if (w.ownWindow && damage > w.windowDamage + 0.01) {
               w.windowDamage = damage;
            }
         } else if (!w.ownWindow && tick >= w.hitTick && tick - w.hitTick < w.windowLengthTicks) {
            w.hitInsideWindow = true;
         } else {
            w.hitTick = tick;
            w.windowOpenTick = tick + 4;
            w.hitInsideWindow = false;
            w.windowLengthTicks = Math.max(1, (int)Math.round(10.0 * this.timeScale));
            w.windowDamage = damage;
            w.ownWindow = false;
         }
      }

      private void observeHurt(ShieldHitWindowHelper.HitWindowTracker.HitWindow w, int hurt, int tick) {
         if (w.observedTick != tick) {
            if (hurt > w.lastHurt) {
               boolean ours = tick >= w.hitTick && tick - w.hitTick <= w.windowLengthTicks && tick >= w.lastAttackTick && tick - w.lastAttackTick <= 4;
               w.windowCloseTick = tick + (int)Math.ceil(Math.max(0, hurt) * this.timeScale);
               if (ours) {
                  w.windowCloseTick = w.hitTick + w.windowLengthTicks;
                  w.windowOpenTick = tick;
                  w.poll = tick;
               }

               w.ownWindow = ours;
            }

            w.lastHurt = hurt;
            w.observedTick = tick;
            if (this.windows.size() > 64) {
               this.windows.values().removeIf(old -> tick - old.observedTick > 200);
            }
         }
      }

      void load() {
         this.windows.clear();
         this.timeScale = 1.0;
      }

      private static final class HitWindow {
         int observedTick = -2147483648;
         int hitTick = -1000;
         int lastHurt;
         int windowOpenTick;
         int windowCloseTick;
         int lastAttackTick = -2147483648;
         int waitRollTick = -2147483648;
         int waitRollChance;
         int windowLengthTicks = 10;
         int poll = -2147483648;
         double windowDamage;
         boolean ownWindow;
         boolean waitRoll;
         boolean hitInsideWindow;
      }
   }

   enum ShieldAction {
      DOWN,
      HOLD,
      RAISE;
   }

   static final class VelocityTracker {
      private Vec3 lastPosition;
      private Vec3 velocity = Vec3.ZERO;
      private int lastTick;

      Vec3 unusedUpdateVelocity(Vec3 latest, int tick, Vec3 fallback) {
         if (this.lastPosition != null && tick >= this.lastTick) {
            if (latest.distanceToSqr(this.lastPosition) > 1.0E-8) {
               int elapsed = Math.max(1, tick - this.lastTick);
               Vec3 delta = latest.subtract(this.lastPosition);
               this.velocity = elapsed <= 3 && delta.lengthSqr() <= 2.25 ? ShieldHitWindowHelper.init(delta.scale(1.0 / elapsed)) : Vec3.ZERO;
               this.lastPosition = latest;
               this.lastTick = tick;
            }
         } else {
            this.lastPosition = latest;
            this.lastTick = tick;
            this.velocity = ShieldHitWindowHelper.init(fallback);
         }

         return tick - this.lastTick <= 2 ? this.velocity : Vec3.ZERO;
      }
   }

   @FunctionalInterface
   interface CollisionClipper {
      Vec3 clip(AABB var1, Vec3 var2);
   }

   static final class TargetLock {
      private int lockTick = -1000;
      private int lastLockTick = -1000;
      private UUID lockedTarget;

      boolean unusedTryLock(UUID target, int tick, boolean certain, boolean canProtect) {
         if (this.lockedTarget == null || tick < this.lockTick || tick - this.lockTick >= 1) {
            this.lockedTarget = null;
            if (certain && canProtect && tick - this.lastLockTick >= 100) {
               this.lockedTarget = target;
               this.lockTick = this.lastLockTick = tick;
               return true;
            } else {
               return false;
            }
         } else {
            if (this.lockedTarget.equals(target) && certain && canProtect) {
               return true;
            }

            this.lockedTarget = null;
            return false;
         }
      }

      boolean unusedLockedAt(int tick) {
         return this.lockedTarget != null && tick == this.lockTick;
      }

      void unusedClear() {
         this.lockedTarget = null;
         this.lockTick = this.lastLockTick = -1000;
      }
   }
}
