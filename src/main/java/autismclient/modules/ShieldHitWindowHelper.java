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

   static double init(double raiseTicks) {
      return Math.min(10.0, 1.0 + Math.max(0.0, raiseTicks));
   }

   static ShieldHitWindowHelper.Inner_CachePool init_l_CachePool_Z(boolean threat, boolean raised, int ownHurtTime, int raiseTicks, int ticksUntilOwnHit) {
      if (threat && ticksUntilOwnHit > 0) {
         int raise = Math.max(0, raiseTicks);
         if (raised) {
            return ticksUntilOwnHit <= 1 ? ShieldHitWindowHelper.Inner_CachePool.DOWN : ShieldHitWindowHelper.Inner_CachePool.HOLD;
         } else {
            return ticksUntilOwnHit <= raise + 1 ? ShieldHitWindowHelper.Inner_CachePool.DOWN : ShieldHitWindowHelper.Inner_CachePool.RAISE;
         }
      } else {
         return ShieldHitWindowHelper.Inner_CachePool.DOWN;
      }
   }

   static ShieldHitWindowHelper.Inner_CachePool init(boolean threat, boolean raised, int raiseTicks, int ticksUntilClick) {
      return !raised && threat && raiseTicks == 0 && ticksUntilClick == 1
         ? ShieldHitWindowHelper.Inner_CachePool.RAISE
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

   static boolean init_Z_Vec3_2(Vec3 eye, Vec3 view, AABB victim, double reach, double margin) {
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

   static boolean init(Vec3 eye, Vec3 view, AABB victim, double minReach, double maxReach, double margin) {
      return init(eye, view, victim) && isInReachWindow(eye, victim, minReach, maxReach + 0.5, margin);
   }

   static int init(AABB body, Vec3 motion, ShieldHitWindowHelper.Inner_DataUtil collision) {
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

   static final class Inner_BufferHelper {
      static final int init_I = 60;
      private final Map<UUID, ShieldHitWindowHelper.Inner_BufferHelper.Inner_DataUtil> init_Map = new HashMap<>();
      private double init_D = 1.0;

      boolean init_Z(UUID id, int hurt, int tick, boolean afterFirstHit) {
         if (!afterFirstHit) {
            return true;
         } else {
            ShieldHitWindowHelper.Inner_BufferHelper.Inner_DataUtil w = this.init_Map.get(id);
            if (w == null) {
               return false;
            } else {
               this.init_V_l_BufferHelper_Dat(w, hurt, tick);
               if (!init_Z_l_BufferHelper_Dat_I(w, tick)) {
                  w.poll = -2147483648;
                  return false;
               } else {
                  return true;
               }
            }
         }
      }

      void init() {
         for (ShieldHitWindowHelper.Inner_BufferHelper.Inner_DataUtil w : this.init_Map.values()) {
            w.poll = -2147483648;
         }
      }

      void init_V_UUID_I(UUID id, int tick) {
         ShieldHitWindowHelper.Inner_BufferHelper.Inner_DataUtil w = this.init_Map.get(id);
         if (w != null) {
            w.poll = tick;
         }
      }

      private static boolean init_Z_l_BufferHelper_Dat_I(ShieldHitWindowHelper.Inner_BufferHelper.Inner_DataUtil w, int tick) {
         long age = (long)tick - w.poll;
         return w.poll != -2147483648 && age >= 0L && age < 60L;
      }

      void init(double scale) {
         this.init_D = Double.isFinite(scale) ? Math.clamp(scale, 0.25, 4.0) : 1.0;
      }

      boolean init_Z(UUID id, int hurt, int tick, double damage) {
         return this.init(id, hurt, tick, damage, false);
      }

      boolean init(UUID id, int hurt, int tick, double damage, boolean movingOpportunity) {
         ShieldHitWindowHelper.Inner_BufferHelper.Inner_DataUtil w = this.init_Map.computeIfAbsent(id, ignored -> new ShieldHitWindowHelper.Inner_BufferHelper.Inner_DataUtil());
         this.init_V_l_BufferHelper_Dat(w, hurt, tick);
         if (tick == w.sync) {
            return false;
         } else if (tick < init_I_l_BufferHelper_Dat(w, hurt, movingOpportunity)) {
            return false;
         } else {
            return w.init_Z && damage > w.init_D + 0.01 ? true : tick >= w.update;
         }
      }

      boolean load(UUID id, int hurt, int tick, double damage) {
         ShieldHitWindowHelper.Inner_BufferHelper.Inner_DataUtil w = this.init_Map.computeIfAbsent(id, ignored -> new ShieldHitWindowHelper.Inner_BufferHelper.Inner_DataUtil());
         this.init_V_l_BufferHelper_Dat(w, hurt, tick);
         if (tick == w.sync) {
            return false;
         } else {
            return w.init_Z && damage > w.init_D + 0.01 ? true : tick >= w.update;
         }
      }

      int init_I(UUID id, int hurt, int tick, boolean strict) {
         return this.init(id, hurt, tick, strict, false);
      }

      int init_I(UUID id, int hurt, int tick, double damage) {
         int ready = this.init_I(id, hurt, tick, false);
         ShieldHitWindowHelper.Inner_BufferHelper.Inner_DataUtil w = this.init_Map.get(id);
         return w.init_Z && damage > w.init_D + 0.01 ? (tick == w.sync ? 1 : 0) : ready;
      }

      int init(UUID id, int hurt, int tick, boolean strict, boolean movingOpportunity) {
         ShieldHitWindowHelper.Inner_BufferHelper.Inner_DataUtil w = this.init_Map.computeIfAbsent(id, ignored -> new ShieldHitWindowHelper.Inner_BufferHelper.Inner_DataUtil());
         this.init_V_l_BufferHelper_Dat(w, hurt, tick);
         int open = strict ? Math.max(init_I_l_BufferHelper_Dat(w, hurt, movingOpportunity), w.update) : w.update;
         if (w.sync == tick) {
            open = Math.max(open, tick + 1);
         }

         return Math.max(0, open - tick);
      }

      private static int init_I_l_BufferHelper_Dat(ShieldHitWindowHelper.Inner_BufferHelper.Inner_DataUtil w, int hurt, boolean movingOpportunity) {
         return movingOpportunity && hurt == 0 && !w.read_Z ? Math.min(w.write, w.load_I + 2) : w.write;
      }

      boolean init(UUID id, int tick, int waitChance, DoubleSupplier random) {
         ShieldHitWindowHelper.Inner_BufferHelper.Inner_DataUtil w = this.init_Map.get(id);
         if (w != null && tick != w.sync && tick >= w.write && tick < w.update) {
            int chance = Math.clamp(waitChance, 10, 100);
            if (chance == 100) {
               return false;
            }

            if (w.flush != tick || w.close != chance) {
               w.flush = tick;
               w.close = chance;
               w.load_Z = random.getAsDouble() >= chance * 0.01;
            }

            return w.load_Z;
         } else {
            return false;
         }
      }

      void init_V(UUID id, int hurt, int tick, double damage) {
         ShieldHitWindowHelper.Inner_BufferHelper.Inner_DataUtil w = this.init_Map.computeIfAbsent(id, ignored -> new ShieldHitWindowHelper.Inner_BufferHelper.Inner_DataUtil());
         this.init_V_l_BufferHelper_Dat(w, hurt, tick);
         w.sync = tick;
         if (tick < w.update) {
            if (w.init_Z && damage > w.init_D + 0.01) {
               w.init_D = damage;
            }
         } else if (!w.init_Z && tick >= w.load_I && tick - w.load_I < w.open) {
            w.read_Z = true;
         } else {
            w.load_I = tick;
            w.write = tick + 4;
            w.read_Z = false;
            w.open = Math.max(1, (int)Math.round(10.0 * this.init_D));
            w.init_D = damage;
            w.init_Z = false;
         }
      }

      private void init_V_l_BufferHelper_Dat(ShieldHitWindowHelper.Inner_BufferHelper.Inner_DataUtil w, int hurt, int tick) {
         if (w.init_I != tick) {
            if (hurt > w.read_I) {
               boolean ours = tick >= w.load_I && tick - w.load_I <= w.open && tick >= w.sync && tick - w.sync <= 4;
               w.update = tick + (int)Math.ceil(Math.max(0, hurt) * this.init_D);
               if (ours) {
                  w.update = w.load_I + w.open;
                  w.write = tick;
                  w.poll = tick;
               }

               w.init_Z = ours;
            }

            w.read_I = hurt;
            w.init_I = tick;
            if (this.init_Map.size() > 64) {
               this.init_Map.values().removeIf(old -> tick - old.init_I > 200);
            }
         }
      }

      void load() {
         this.init_Map.clear();
         this.init_D = 1.0;
      }

      private static final class Inner_DataUtil {
         int init_I = -2147483648;
         int load_I = -1000;
         int read_I;
         int write;
         int update;
         int sync = -2147483648;
         int flush = -2147483648;
         int close;
         int open = 10;
         int poll = -2147483648;
         double init_D;
         boolean init_Z;
         boolean load_Z;
         boolean read_Z;
      }
   }

   enum Inner_CachePool {
      DOWN,
      HOLD,
      RAISE;
   }

   static final class Inner_ConfigStore {
      private Vec3 init_Vec3;
      private Vec3 load = Vec3.ZERO;
      private int init_I;

      Vec3 init(Vec3 latest, int tick, Vec3 fallback) {
         if (this.init_Vec3 != null && tick >= this.init_I) {
            if (latest.distanceToSqr(this.init_Vec3) > 1.0E-8) {
               int elapsed = Math.max(1, tick - this.init_I);
               Vec3 delta = latest.subtract(this.init_Vec3);
               this.load = elapsed <= 3 && delta.lengthSqr() <= 2.25 ? ShieldHitWindowHelper.init(delta.scale(1.0 / elapsed)) : Vec3.ZERO;
               this.init_Vec3 = latest;
               this.init_I = tick;
            }
         } else {
            this.init_Vec3 = latest;
            this.init_I = tick;
            this.load = ShieldHitWindowHelper.init(fallback);
         }

         return tick - this.init_I <= 2 ? this.load : Vec3.ZERO;
      }
   }

   @FunctionalInterface
   interface Inner_DataUtil {
      Vec3 clip(AABB var1, Vec3 var2);
   }

   static final class Inner_StreamUtil {
      private int init_I = -1000;
      private int load = -1000;
      private UUID init_UUID;

      boolean init(UUID target, int tick, boolean certain, boolean canProtect) {
         if (this.init_UUID == null || tick < this.init_I || tick - this.init_I >= 1) {
            this.init_UUID = null;
            if (certain && canProtect && tick - this.load >= 100) {
               this.init_UUID = target;
               this.init_I = this.load = tick;
               return true;
            } else {
               return false;
            }
         } else {
            if (this.init_UUID.equals(target) && certain && canProtect) {
               return true;
            }

            this.init_UUID = null;
            return false;
         }
      }

      boolean init(int tick) {
         return this.init_UUID != null && tick == this.init_I;
      }

      void init() {
         this.init_UUID = null;
         this.init_I = this.load = -1000;
      }
   }
}
