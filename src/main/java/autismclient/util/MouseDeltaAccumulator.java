package autismclient.util;

public final class MouseDeltaAccumulator {
   private static int lastTick = -2147483648;
   private static double pendingDx;
   private static double pendingDy;

   private MouseDeltaAccumulator() {
   }

   public static void add(double dx, double dy, int clientTick) {
      if (Double.isFinite(dx) && Double.isFinite(dy)) {
         pendingDx += dx;
         pendingDy += dy;
      }

      if (!(Math.abs(dx) < 0.5) || !(Math.abs(dy) < 0.5)) {
         lastTick = clientTick;
      }
   }

   public static boolean movedWithin(int withinTicks, int clientTick) {
      return lastTick != -2147483648 && clientTick - lastTick <= withinTicks;
   }

   public static double[] init(double degreesPerCount, boolean invertX, boolean invertY) {
      double yaw = (invertX ? -pendingDx : pendingDx) * degreesPerCount;
      double pitch = (invertY ? -pendingDy : pendingDy) * degreesPerCount;
      pendingDx = 0.0;
      pendingDy = 0.0;
      return new double[]{yaw, pitch};
   }

   public static void reset() {
      lastTick = -2147483648;
      pendingDx = 0.0;
      pendingDy = 0.0;
   }
}
