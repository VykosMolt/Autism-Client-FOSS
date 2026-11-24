package autismclient.util;

public final class aZ {
   private static int init_I = -2147483648;
   private static double init_D;
   private static double load;

   private aZ() {
   }

   public static void init(double dx, double dy, int clientTick) {
      if (Double.isFinite(dx) && Double.isFinite(dy)) {
         init_D += dx;
         load += dy;
      }

      if (!(Math.abs(dx) < 0.5) || !(Math.abs(dy) < 0.5)) {
         init_I = clientTick;
      }
   }

   public static boolean init(int withinTicks, int clientTick) {
      return init_I != -2147483648 && clientTick - init_I <= withinTicks;
   }

   public static double[] init(double degreesPerCount, boolean invertX, boolean invertY) {
      double yaw = (invertX ? -init_D : init_D) * degreesPerCount;
      double pitch = (invertY ? -load : load) * degreesPerCount;
      init_D = 0.0;
      load = 0.0;
      return new double[]{yaw, pitch};
   }

   public static void init() {
      init_I = -2147483648;
      init_D = 0.0;
      load = 0.0;
   }
}
