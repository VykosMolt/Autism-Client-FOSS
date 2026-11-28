package autismclient.ai.engine;

public final class AiStrafe {
   static final double init_D = 0.12;
   static final double load_D = 0.08;
   static final double read = 0.003;
   static final int init_I = 8;
   static final int load_I = 5;

   private AiStrafe() {
   }

   static double lateral(float yawDeg, double vx, double vz) {
      double r = Math.toRadians(yawDeg);
      return vx * Math.cos(r) + vz * Math.sin(r);
   }

   static double lateralOffset(float yawDeg, double bodyX, double bodyZ, double centreX, double centreZ, double dirX, double dirZ) {
      double vx = centreX - bodyX;
      double vz = centreZ - bodyZ;
      double len = Math.hypot(dirX, dirZ);
      if (len > 1.0E-6) {
         double ux = dirX / len;
         double uz = dirZ / len;
         double along = vx * ux + vz * uz;
         vx -= along * ux;
         vz -= along * uz;
      }

      return lateral(yawDeg, vx, vz);
   }

   static int strafeDirection(double offset, boolean narrow, boolean blocked, double currentLeft, int blockedTicks) {
      double tolerance = narrow ? 0.12 : (blocked ? 0.08 : 1.0 / 0.0);
      if (Math.abs(offset) > tolerance) {
         return offset > 0.0 ? 1 : -1;
      } else if (Math.abs(currentLeft) > 0.003) {
         int lean = currentLeft > 0.0 ? -1 : 1;
         return Math.abs(offset) > 0.25 && Math.signum(offset) == -lean ? 0 : lean;
      } else if (blocked && blockedTicks >= 8) {
         return (blockedTicks - 8) / 5 % 2 == 0 ? 1 : -1;
      } else {
         return 0;
      }
   }

   static boolean isStuck(boolean horizontalCollision, double speedX, double speedZ) {
      return horizontalCollision && Math.hypot(speedX, speedZ) < 0.06;
   }
}
