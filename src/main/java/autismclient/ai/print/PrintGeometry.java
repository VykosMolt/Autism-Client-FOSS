package autismclient.ai.print;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class PrintGeometry {
   public static final double init = 1.62;
   public static final double load = 1.27;
   public static final double read = 0.3;
   public static final double write = 1.8;
   public static final double update = 1.5;
   public static final double sync = 4.5;
   public static final double flush = 4.4;
   public static final double close = 3.0;
   public static final double open = 0.6;
   public static final double poll = 1.25;
   public static final double push = 0.25;

   private PrintGeometry() {
   }

   public static AABB init(double x, double feetY, double z, boolean crouched) {
      return new AABB(x - 0.3, feetY, z - 0.3, x + 0.3, feetY + (crouched ? 1.5 : 1.8), z + 0.3);
   }

   public static Vec3 init(Vec3 feet, boolean crouched) {
      return new Vec3(feet.x, feet.y + (crouched ? 1.27 : 1.62), feet.z);
   }

   public static float init(Vec3 from, Vec3 to) {
      return (float)(Mth.atan2(to.z - from.z, to.x - from.x) * 57.2957763671875) - 90.0F;
   }

   public static float load(Vec3 from, Vec3 to) {
      double dx = to.x - from.x;
      double dy = to.y - from.y;
      double dz = to.z - from.z;
      return (float)(-(Mth.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * 57.2957763671875));
   }

   public static Vec3 init_Vec3(float yaw, float pitch) {
      return Vec3.directionFromRotation(pitch, yaw);
   }

   public static float init_F(float from, float to) {
      return Mth.wrapDegrees(to - from);
   }

   public static double[] init(float cameraYaw, double dx, double dz) {
      double rad = Math.toRadians(cameraYaw);
      double fx = -Math.sin(rad);
      double fz = Math.cos(rad);
      double forward = dx * fx + dz * fz;
      double left = dx * fz - dz * fx;
      return new double[]{forward, left};
   }
}
