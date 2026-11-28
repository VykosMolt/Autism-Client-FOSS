package autismclient.util;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class HitboxRayHelper {
   private HitboxRayHelper() {
   }

   public static Vec3 init(AABB box, Vec3 eye, Vec3 look, double range, Predicate<Vec3> blocked) {
      if (!Double.isFinite(range) || range <= 0.0 || !init(eye) || !init(look)) {
         return null;
      } else if (box.contains(eye)) {
         return blocked.test(eye) ? null : eye;
      } else {
         Optional<Vec3> hit = box.clip(eye.add(look.scale(-0.05)), eye.add(look.scale(range)));
         if (!hit.isEmpty() && init(hit.get()) && !(eye.distanceToSqr(hit.get()) > range * range)) {
            return blocked.test(hit.get()) ? null : hit.get();
         } else {
            return null;
         }
      }
   }

   private static boolean init(Vec3 vector) {
      return Double.isFinite(vector.x) && Double.isFinite(vector.y) && Double.isFinite(vector.z);
   }

   public static Vec3 nearestPoint(AABB box, Vec3 eye) {
      return new Vec3(init(eye.x, box.minX, box.maxX), init(eye.y, box.minY, box.maxY), init(eye.z, box.minZ, box.maxZ));
   }

   private static double init(double eye, double minimum, double maximum) {
      double inset = Math.min(0.04, (maximum - minimum) * 0.5);
      return Math.max(minimum + inset, Math.min(maximum - inset, eye));
   }
}
