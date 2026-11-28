package autismclient.ai;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.Vec3;

final class AiEyeNavigation {
   static final double init_D = 0.001;
   static final double load = 0.008;
   static final double read = 8.0;
   private final List<AiEyeNavigation.Inner_CachePool> init_List = new ArrayList<>();
   private AiEyeNavigation.Inner_DataUtil located;
   private int init_I;

   void addThrow(Vec3 origin, Vec3 displacement) {
      double length = Math.hypot(displacement.x, displacement.z);
      if (!(length < 4.0)) {
         AiEyeNavigation.Inner_CachePool ray = new AiEyeNavigation.Inner_CachePool(origin, new Vec3(displacement.x / length, 0.0, displacement.z / length));
         if (this.init_List.isEmpty() || !(horizontalDistance(origin, this.init_List.get(this.init_List.size() - 1).origin) < 8.0)) {
            this.init_List.add(ray);
            if (this.init_List.size() > 8) {
               this.init_List.remove(0);
            }

            this.located = this.load();
            if (this.located == null && this.init_List.size() >= 3) {
               List<AiEyeNavigation.Inner_CachePool> old = new ArrayList<>(this.init_List);
               this.init_List.clear();
               this.init_List.add(old.get(old.size() - 2));
               this.init_List.add(old.get(old.size() - 1));
               AiEyeNavigation.Inner_DataUtil fresh = this.load();
               if (fresh != null && fresh.uncertainty < 160.0) {
                  this.init_I++;
                  this.located = fresh;
               } else {
                  this.init_List.clear();
                  this.init_List.addAll(old);
               }
            }
         }
      }
   }

   AiEyeNavigation.Inner_DataUtil getLocated() {
      return this.located;
   }

   private AiEyeNavigation.Inner_DataUtil load() {
      if (this.init_List.size() < 2) {
         return null;
      }

      AiEyeNavigation.Inner_CachePool last = this.init_List.get(this.init_List.size() - 1);
      AiEyeNavigation.Inner_DataUtil best = null;

      for (int i = 0; i < this.init_List.size() - 1; i++) {
         AiEyeNavigation.Inner_CachePool first = this.init_List.get(i);
         double cross = first.direction.x * last.direction.z - first.direction.z * last.direction.x;
         double baseline = horizontalDistance(first.origin, last.origin);
         if (!(baseline < 32.0) && !(Math.abs(cross) < 0.008)) {
            double dx = last.origin.x - first.origin.x;
            double dz = last.origin.z - first.origin.z;
            double t = (dx * last.direction.z - dz * last.direction.x) / cross;
            double u = (dx * first.direction.z - dz * first.direction.x) / cross;
            if (!(t < 0.0) && !(u < 0.0) && !(t > 20000.0) && !(u > 20000.0)) {
               Vec3 intersection = first.origin.add(first.direction.scale(t));
               double uncertainty = Math.max(24.0, 0.0012 * (t + u) / Math.abs(cross));
               boolean consistent = true;

               for (AiEyeNavigation.Inner_CachePool other : this.init_List) {
                  Vec3 delta = intersection.subtract(other.origin);
                  if (delta.dot(other.direction) < -24.0 || Math.abs(delta.x * other.direction.z - delta.z * other.direction.x) > uncertainty * 2.0) {
                     consistent = false;
                     break;
                  }
               }

               if (consistent && (best == null || uncertainty < best.uncertainty)) {
                  best = new AiEyeNavigation.Inner_DataUtil(intersection, uncertainty, baseline, Math.abs(cross));
               }
            }
         }
      }

      return best;
   }

   Vec3 sidestepTarget(Vec3 at, Vec3 bearing) {
      return this.located == null ? at.add(-bearing.z * 64.0, 0.0, bearing.x * 64.0) : null;
   }

   JsonObject statusJson() {
      return this.located == null
         ? AiJson.objectOf("rays", this.init_List.size(), "triangulated", false, "possibleTargetChanges", this.init_I)
         : AiJson.objectOf(
            "rays",
            this.init_List.size(),
            "triangulated",
            true,
            "possibleTargetChanges",
            this.init_I,
            "x",
            this.located.position.x,
            "z",
            this.located.position.z,
            "uncertaintyBlocks",
            this.located.uncertainty,
            "baselineBlocks",
            this.located.baseline,
            "crossing",
            this.located.crossing
         );
   }

   private static double horizontalDistance(Vec3 a, Vec3 b) {
      return Math.hypot(a.x - b.x, a.z - b.z);
   }

   record Inner_CachePool(Vec3 origin, Vec3 direction) {
   }

   record Inner_DataUtil(Vec3 position, double uncertainty, double baseline, double crossing) {
   }

   static final class Inner_StreamUtil {
      private final Vec3 origin;
      private double init_D;
      private double load;
      private double read;
      private double write;
      private double update;
      private double sync;
      private int init_I;

      Inner_StreamUtil(Vec3 origin) {
         this.origin = origin;
      }

      Vec3 addSample(Vec3 at) {
         double x = at.x - this.origin.x;
         double z = at.z - this.origin.z;
         double d = Math.hypot(x, z);
         if (Double.isFinite(d) && !(d < 0.1)) {
            this.init_I++;
            this.init_D += x;
            this.load += z;
            this.read += x * x;
            this.write += z * z;
            this.update += x * z;
            this.sync = Math.max(this.sync, d);
            return this.sync >= 8.0 && this.init_I >= 6 ? this.fitLine() : null;
         } else {
            return null;
         }
      }

      Vec3 fitLine() {
         if (this.init_I < 2) {
            return null;
         }

         double xx = this.read - this.init_D * this.init_D / this.init_I;
         double zz = this.write - this.load * this.load / this.init_I;
         double xz = this.update - this.init_D * this.load / this.init_I;
         double angle = 0.5 * Math.atan2(2.0 * xz, xx - zz);
         double x = Math.cos(angle);
         double z = Math.sin(angle);
         if (x * this.init_D + z * this.load < 0.0) {
            x = -x;
            z = -z;
         }

         return new Vec3(x, 0.0, z);
      }

      Vec3 meanPoint() {
         return this.init_I == 0 ? this.origin : this.origin.add(this.init_D / this.init_I, 0.0, this.load / this.init_I);
      }

      double init_D() {
         return this.sync;
      }
   }
}
