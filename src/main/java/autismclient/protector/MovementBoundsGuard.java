package autismclient.protector;

import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.phys.Vec3;

public final class MovementBoundsGuard {
   public static final double init = 1.0E9;
   public static final double load = 10000.0;

   private MovementBoundsGuard() {
   }

   public static boolean init(double value) {
      return Double.isNaN(value) || Double.isInfinite(value) || Math.abs(value) > 1.0E9;
   }

   public static boolean init(Vec3 vec) {
      return vec == null || init(vec.x) || init(vec.y) || init(vec.z);
   }

   public static boolean load(double value) {
      return Double.isNaN(value) || Double.isInfinite(value) || Math.abs(value) > 10000.0;
   }

   public static boolean load(Vec3 vec) {
      return vec == null || load(vec.x) || load(vec.y) || load(vec.z);
   }

   public static boolean init(PositionMoveRotation change) {
      return change == null || init(change.position()) || load(change.deltaMovement()) || init(change.yRot()) || init(change.xRot());
   }
}
