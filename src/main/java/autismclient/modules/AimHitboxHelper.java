package autismclient.modules;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class AimHitboxHelper {
   static final double init = 0.03;
   static final double load = 0.002;
   static final double read = 0.03;
   private static final double write = 0.08;

   private AimHitboxHelper() {
   }

   static boolean init(AABB box, Vec3 eye, float yaw, float pitch, double reach) {
      return init(box, eye, yaw, pitch, reach, 0.03);
   }

   static boolean init(AABB box, Vec3 eye, float yaw, float pitch, double reach, double margin) {
      box = box.deflate(margin);
      return box.contains(eye) || box.clip(eye, eye.add(Vec3.directionFromRotation(pitch, yaw).scale(reach))).isPresent();
   }

   static float init(float current, Vec3 eye, AABB now, Vec3 nextEye, AABB next) {
      double[] a = init(eye, now);
      double[] b = init(nextEye, next);
      if (a != null && b != null && Float.isFinite(current)) {
         double low = Math.max(a[0], b[0]);
         double high = Math.min(a[1], b[1]);
         return low > high ? current : (float)Math.max(low, Math.min(high, current));
      } else {
         return current;
      }
   }

   private static double[] init(Vec3 eye, AABB box) {
      if (eye != null && box != null) {
         Vec3 centre = box.getCenter();
         double horizontal = Math.hypot(centre.x - eye.x, centre.z - eye.z);
         double top = box.maxY - 0.03 - 0.08;
         double bottom = box.minY + 0.03 + 0.08;
         return !(horizontal < 0.45) && !(top <= bottom)
            ? new double[]{Math.toDegrees(Math.atan2(eye.y - top, horizontal)), Math.toDegrees(Math.atan2(eye.y - bottom, horizontal))}
            : null;
      } else {
         return null;
      }
   }
}
