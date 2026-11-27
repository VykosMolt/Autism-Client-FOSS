package autismclient.ai;

import net.minecraft.world.phys.Vec3;

final class AiPlacementControls {
   private AiPlacementControls() {
   }

   static int rotateBy(int current, int steps) {
      return Math.floorMod(current + Math.floorMod(steps, 4) * 90, 360);
   }

   static double adjustDistance(double current, double scroll) {
      return Math.max(0.5, Math.min(5.9999968E7, current + scroll));
   }

   static Vec3 init(Vec3 eye, Vec3 direction, double distance) {
      return eye.add(direction.normalize().scale(distance));
   }

   static double init(Vec3 eye, Vec3 direction, double y, double fallback) {
      double d = Math.abs(direction.y) > 1.0E-7 ? (y - eye.y) / direction.y : -1.0;
      return Double.isFinite(d) && d >= 0.5 && d <= 5.9999968E7 ? d : fallback;
   }
}
