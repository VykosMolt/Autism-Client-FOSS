package autismclient.modules;

import java.util.Arrays;
import java.util.Optional;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class FireballInterceptMath {
   private FireballInterceptMath() {
   }

   static int init(double height, double verticalMotion) {
      if (Double.isFinite(height) && Double.isFinite(verticalMotion)) {
         for (int tick = 1; tick <= 20; tick++) {
            height += verticalMotion;
            if (height < -0.05) {
               return tick;
            }

            verticalMotion = (verticalMotion - 0.08) * 0.98;
         }

         return 20;
      } else {
         return 0;
      }
   }

   static float init(double yawDifference, double pitchDifference, double strike, double impact, double gcd) {
      double quantum = Double.isFinite(gcd) && gcd > 0.0 ? gcd : 0.15;
      double deadline = strike >= 0.0 ? Math.min(strike, impact) : impact;
      int intervals = Double.isFinite(deadline) ? Math.max(1, (int)Math.floor(deadline)) : 1;
      double difference = Math.max(Math.abs(yawDifference), Math.abs(pitchDifference));
      return (float)Math.max(60.0, difference / intervals + quantum);
   }

   static int init(double yawDifference, double pitchDifference, float step, double gcd) {
      double quantum = Double.isFinite(gcd) && gcd > 0.0 ? gcd : 0.15;
      long counts = Math.max(Math.abs(Math.round(yawDifference / quantum)), Math.abs(Math.round(pitchDifference / quantum)));
      long capacity = Math.max(1L, (long)Math.floor(step / quantum + 1.0E-9));
      return Math.max(1, (int)((counts + capacity - 1L) / capacity));
   }

   static boolean init(int elapsedTicks) {
      return elapsedTicks >= 1;
   }

   static double init_D_Vec3(Vec3 eye, Vec3 bodyMotion, AABB projectile, Vec3 velocity, double acceleration, double inertia, double reach, int horizon) {
      if (init(velocity) && init(bodyMotion) && !(velocity.lengthSqr() < 1.0E-8)) {
         Vec3 relativeEye = eye;
         Vec3 motion = velocity;

         for (int tick = 0; tick <= horizon; tick++) {
            if (projectile.distanceToSqr(relativeEye) <= reach * reach) {
               return tick;
            }

            if (tick == horizon) {
               break;
            }

            Vec3 nextEye = relativeEye.subtract(motion.subtract(bodyMotion));
            double entry = init(projectile, relativeEye, nextEye, reach);
            if (entry >= 0.0) {
               return tick + entry;
            }

            relativeEye = nextEye;
            motion = motion.add(motion.normalize().scale(acceleration)).scale(inertia);
         }

         return -1.0;
      } else {
         return -1.0;
      }
   }

   static double init(AABB body, Vec3 bodyMotion, AABB projectile, Vec3 velocity, int horizon, double margin) {
      return init_D_AABB(body, bodyMotion, projectile, velocity, horizon, margin, 0.1, 0.95);
   }

   static double init_D_AABB(AABB body, Vec3 bodyMotion, AABB projectile, Vec3 velocity, int horizon, double margin, double acceleration, double inertia) {
      if (!(velocity.lengthSqr() < 1.0E-8) && init(velocity) && init(bodyMotion)) {
         Vec3 center = projectile.getCenter();
         AABB expanded = body.inflate(projectile.getXsize() * 0.5 + margin, projectile.getYsize() * 0.5 + margin, projectile.getZsize() * 0.5 + margin);
         Vec3 motion = velocity;

         for (int tick = 0; tick < horizon; tick++) {
            Vec3 next = center.add(motion.subtract(bodyMotion));
            if (expanded.contains(center)) {
               return tick;
            }

            Optional<Vec3> hit = expanded.clip(center, next);
            if (hit.isPresent()) {
               return tick + center.distanceTo(hit.get()) / Math.max(1.0E-8, center.distanceTo(next));
            }

            center = next;
            motion = motion.add(motion.normalize().scale(acceleration)).scale(inertia);
         }

         return -1.0;
      } else {
         return -1.0;
      }
   }

   static double load(AABB body, Vec3 bodyMotion, AABB projectile, Vec3 velocity, int horizon, double margin, double acceleration, double inertia) {
      if (!(velocity.lengthSqr() < 1.0E-8) && init(velocity) && init(bodyMotion)) {
         Vec3 center = projectile.getCenter();
         Vec3 motion = velocity;
         double previous = Math.sqrt(body.distanceToSqr(center));

         for (int tick = 0; tick < horizon; tick++) {
            Vec3 next = center.add(motion.subtract(bodyMotion));
            Vec3 away = center.subtract(
               new Vec3(
                  Math.max(body.minX, Math.min(body.maxX, center.x)),
                  Math.max(body.minY, Math.min(body.maxY, center.y)),
                  Math.max(body.minZ, Math.min(body.maxZ, center.z))
               )
            );
            if (away.dot(next.subtract(center)) > 1.0E-6) {
               return -1.0;
            }

            if (previous <= margin) {
               return tick;
            }

            double entry = init(body, center, next, margin);
            if (entry >= 0.0) {
               return tick + entry;
            }

            previous = Math.sqrt(body.distanceToSqr(next));
            center = next;
            motion = motion.add(motion.normalize().scale(acceleration)).scale(inertia);
         }

         return -1.0;
      } else {
         return -1.0;
      }
   }

   private static double init(AABB box, Vec3 from, Vec3 to, double radius) {
      double[] start = new double[]{from.x, from.y, from.z};
      double[] delta = new double[]{to.x - from.x, to.y - from.y, to.z - from.z};
      double[] min = new double[]{box.minX, box.minY, box.minZ};
      double[] max = new double[]{box.maxX, box.maxY, box.maxZ};
      double[] cuts = new double[8];
      cuts[0] = 0.0;
      cuts[1] = 1.0;
      int size = 2;

      for (int axis = 0; axis < 3; axis++) {
         if (!(Math.abs(delta[axis]) < 1.0E-12)) {
            for (double plane : new double[]{min[axis], max[axis]}) {
               double at = (plane - start[axis]) / delta[axis];
               if (at > 0.0 && at < 1.0) {
                  cuts[size++] = at;
               }
            }
         }
      }

      Arrays.sort(cuts, 0, size);

      for (int i = 1; i < size; i++) {
         double lo = cuts[i - 1];
         double hi = cuts[i];
         double middle = (lo + hi) * 0.5;
         double a = 0.0;
         double b = 0.0;
         double c = -radius * radius;

         for (int axis = 0; axis < 3; axis++) {
            double coordinate = start[axis] + delta[axis] * middle;
            if (!(coordinate >= min[axis]) || !(coordinate <= max[axis])) {
               double offset = start[axis] - (coordinate < min[axis] ? min[axis] : max[axis]);
               a += delta[axis] * delta[axis];
               b += 2.0 * offset * delta[axis];
               c += offset * offset;
            }
         }

         if ((a * lo + b) * lo + c <= 1.0E-9) {
            return lo;
         }

         if (!(a < 1.0E-12)) {
            double discriminant = b * b - 4.0 * a * c;
            if (!(discriminant < 0.0)) {
               double entry = (-b - Math.sqrt(discriminant)) / (2.0 * a);
               if (entry >= lo - 1.0E-9 && entry <= hi + 1.0E-9) {
                  return Math.max(lo, Math.min(hi, entry));
               }
            }
         }
      }

      return -1.0;
   }

   static FireballInterceptMath.Inner_DataUtil init(Vec3 eye, Vec3 bodyMotion, AABB box, Vec3 velocity, Vec3 wantedDirection, double reach) {
      Vec3 nextEye = eye.add(bodyMotion);
      AABB nextBox = box.move(velocity);
      if (init(bodyMotion) && init(velocity) && box.distanceToSqr(eye) > reach * reach && nextBox.distanceToSqr(nextEye) <= reach * reach) {
         eye = nextEye;
         box = nextBox;
      }

      if (box.distanceToSqr(eye) < 25.0) {
         box = box.deflate(Math.min(0.2, Math.min(box.getXsize(), Math.min(box.getYsize(), box.getZsize())) * 0.2));
      }

      return new FireballInterceptMath.Inner_DataUtil(eye, init(eye, box, wantedDirection));
   }

   static Vec3 init(Vec3 eye, AABB box, Vec3 wantedDirection) {
      AABB inset = box.deflate(Math.min(0.18, Math.min(box.getXsize(), Math.min(box.getYsize(), box.getZsize())) * 0.25));
      Vec3 preferred = wantedDirection.normalize();
      Vec3 far = eye.add(preferred.scale(128.0));
      Optional<Vec3> direct = inset.clip(eye, far);
      if (direct.isPresent()) {
         return direct.get().add(preferred.scale(0.001));
      }

      Vec3 best = inset.getCenter();
      double score = best.subtract(eye).normalize().dot(preferred);

      for (double x : new double[]{inset.minX, (inset.minX + inset.maxX) * 0.5, inset.maxX}) {
         for (double y : new double[]{inset.minY, (inset.minY + inset.maxY) * 0.5, inset.maxY}) {
            for (double z : new double[]{inset.minZ, (inset.minZ + inset.maxZ) * 0.5, inset.maxZ}) {
               Vec3 point = new Vec3(x, y, z);
               double candidate = point.subtract(eye).normalize().dot(preferred);
               if (candidate > score) {
                  score = candidate;
                  best = point;
               }
            }
         }
      }

      return best;
   }

   static boolean init(Vec3 vector) {
      return Double.isFinite(vector.x) && Double.isFinite(vector.y) && Double.isFinite(vector.z);
   }

   record Inner_DataUtil(Vec3 init, Vec3 load) {
   }
}
