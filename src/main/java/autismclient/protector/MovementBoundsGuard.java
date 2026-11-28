package autismclient.protector;

import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.phys.Vec3;

public final class MovementBoundsGuard {
   public static final double init = 1.0E9;
   public static final double load = 10000.0;

   private MovementBoundsGuard() {
   }

   public static boolean isBadCoord(double value) {
      return Double.isNaN(value) || Double.isInfinite(value) || Math.abs(value) > 1.0E9;
   }

   public static boolean isBadPosition(Vec3 vec) {
      return vec == null || isBadCoord(vec.x) || isBadCoord(vec.y) || isBadCoord(vec.z);
   }

   public static boolean isOutOfBounds(double value) {
      return Double.isNaN(value) || Double.isInfinite(value) || Math.abs(value) > 10000.0;
   }

   public static boolean isBadMotion(Vec3 vec) {
      return vec == null || isOutOfBounds(vec.x) || isOutOfBounds(vec.y) || isOutOfBounds(vec.z);
   }

   public static boolean isBadChange(PositionMoveRotation change) {
      return change == null || isBadPosition(change.position()) || isBadMotion(change.deltaMovement()) || isBadCoord(change.yRot()) || isBadCoord(change.xRot());
   }
}
