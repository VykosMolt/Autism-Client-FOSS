package autismclient.ai;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.Vec3;

final class AiEyeNavigation {
   static final double init_D = 0.001;
   static final double load = 0.008;
   static final double read = 8.0;
   private final List<AiEyeNavigation.EyeRay> rays = new ArrayList<>();
   private AiEyeNavigation.Triangulation located;
   private int targetChanges;

   void addThrow(Vec3 origin, Vec3 displacement) {
      double length = Math.hypot(displacement.x, displacement.z);
      if (!(length < 4.0)) {
         AiEyeNavigation.EyeRay ray = new AiEyeNavigation.EyeRay(origin, new Vec3(displacement.x / length, 0.0, displacement.z / length));
         if (this.rays.isEmpty() || !(horizontalDistance(origin, this.rays.get(this.rays.size() - 1).origin) < 8.0)) {
            this.rays.add(ray);
            if (this.rays.size() > 8) {
               this.rays.remove(0);
            }

            this.located = this.triangulate();
            if (this.located == null && this.rays.size() >= 3) {
               List<AiEyeNavigation.EyeRay> old = new ArrayList<>(this.rays);
               this.rays.clear();
               this.rays.add(old.get(old.size() - 2));
               this.rays.add(old.get(old.size() - 1));
               AiEyeNavigation.Triangulation fresh = this.triangulate();
               if (fresh != null && fresh.uncertainty < 160.0) {
                  this.targetChanges++;
                  this.located = fresh;
               } else {
                  this.rays.clear();
                  this.rays.addAll(old);
               }
            }
         }
      }
   }

   AiEyeNavigation.Triangulation getLocated() {
      return this.located;
   }

   private AiEyeNavigation.Triangulation triangulate() {
      if (this.rays.size() < 2) {
         return null;
      }

      AiEyeNavigation.EyeRay last = this.rays.get(this.rays.size() - 1);
      AiEyeNavigation.Triangulation best = null;

      for (int i = 0; i < this.rays.size() - 1; i++) {
         AiEyeNavigation.EyeRay first = this.rays.get(i);
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

               for (AiEyeNavigation.EyeRay other : this.rays) {
                  Vec3 delta = intersection.subtract(other.origin);
                  if (delta.dot(other.direction) < -24.0 || Math.abs(delta.x * other.direction.z - delta.z * other.direction.x) > uncertainty * 2.0) {
                     consistent = false;
                     break;
                  }
               }

               if (consistent && (best == null || uncertainty < best.uncertainty)) {
                  best = new AiEyeNavigation.Triangulation(intersection, uncertainty, baseline, Math.abs(cross));
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
         ? AiJson.objectOf("rays", this.rays.size(), "triangulated", false, "possibleTargetChanges", this.targetChanges)
         : AiJson.objectOf(
            "rays",
            this.rays.size(),
            "triangulated",
            true,
            "possibleTargetChanges",
            this.targetChanges,
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

   record EyeRay(Vec3 origin, Vec3 direction) {
   }

   record Triangulation(Vec3 position, double uncertainty, double baseline, double crossing) {
   }

   static final class LineFit {
      private final Vec3 origin;
      private double sumX;
      private double sumZ;
      private double sumXX;
      private double sumZZ;
      private double sumXZ;
      private double maxDistance;
      private int count;

      LineFit(Vec3 origin) {
         this.origin = origin;
      }

      Vec3 addSample(Vec3 at) {
         double x = at.x - this.origin.x;
         double z = at.z - this.origin.z;
         double d = Math.hypot(x, z);
         if (Double.isFinite(d) && !(d < 0.1)) {
            this.count++;
            this.sumX += x;
            this.sumZ += z;
            this.sumXX += x * x;
            this.sumZZ += z * z;
            this.sumXZ += x * z;
            this.maxDistance = Math.max(this.maxDistance, d);
            return this.maxDistance >= 8.0 && this.count >= 6 ? this.fitLine() : null;
         } else {
            return null;
         }
      }

      Vec3 fitLine() {
         if (this.count < 2) {
            return null;
         }

         double xx = this.sumXX - this.sumX * this.sumX / this.count;
         double zz = this.sumZZ - this.sumZ * this.sumZ / this.count;
         double xz = this.sumXZ - this.sumX * this.sumZ / this.count;
         double angle = 0.5 * Math.atan2(2.0 * xz, xx - zz);
         double x = Math.cos(angle);
         double z = Math.sin(angle);
         if (x * this.sumX + z * this.sumZ < 0.0) {
            x = -x;
            z = -z;
         }

         return new Vec3(x, 0.0, z);
      }

      Vec3 meanPoint() {
         return this.count == 0 ? this.origin : this.origin.add(this.sumX / this.count, 0.0, this.sumZ / this.count);
      }

      double init_D() {
         return this.maxDistance;
      }
   }
}
