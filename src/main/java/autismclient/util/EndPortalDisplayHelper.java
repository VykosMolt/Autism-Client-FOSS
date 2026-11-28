package autismclient.util;

import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class EndPortalDisplayHelper {
   private EndPortalDisplayHelper() {
   }

   public static boolean init(Vec3 camera, Vec3 target, int radius) {
      if (StrongholdTriangulator.isValidPoint(camera) && StrongholdTriangulator.isValidPoint(target) && radius >= 1 && radius <= 46340) {
         int cx = (int)Math.floor(camera.x) >> 4;
         int cz = (int)Math.floor(camera.z) >> 4;
         int tx = (int)Math.floor(target.x) >> 4;
         int tz = (int)Math.floor(target.z) >> 4;
         return ChunkTrackingView.isInViewDistance(cx, cz, radius, tx, tz);
      } else {
         return false;
      }
   }

   public static EndPortalDisplayHelper.BeamColumn init(Vec3 camera, Vec3 target, double minY, double maxY) {
      if (StrongholdTriangulator.isValidPoint(camera) && StrongholdTriangulator.isValidPoint(target) && Double.isFinite(minY) && Double.isFinite(maxY) && !(maxY <= minY)) {
         double dx = target.x - camera.x;
         double dz = target.z - camera.z;
         return dx * dx + dz * dz < 4.0 ? null : new EndPortalDisplayHelper.BeamColumn(dx, dz, minY - camera.y, maxY - camera.y, 0.12, 0.7);
      } else {
         return null;
      }
   }

   public static String prompt(boolean hasTarget, boolean eyeDimension, boolean observing) {
      return !hasTarget && eyeDimension ? (observing ? "Reading Eye" : "Throw an Eye of Ender") : "";
   }

   public static int withOpacity(int argb, int opacity) {
      int alpha = ((argb >>> 24) * Math.max(0, Math.min(255, opacity)) + 127) / 255;
      return alpha << 24 | argb & 16777215;
   }

   public static String init(Vec3 target, boolean observed, boolean estimated) {
      if (StrongholdTriangulator.isValidPoint(target) && (observed || estimated)) {
         long x = (long)Math.floor(target.x);
         long z = (long)Math.floor(target.z);
         return observed ? x + ", " + (long)Math.floor(target.y) + ", " + z : x + ", " + z;
      } else {
         return "";
      }
   }

   public static String init(Vec3 player, Vec3 target, boolean observed, boolean estimated) {
      if (StrongholdTriangulator.isValidPoint(player) && StrongholdTriangulator.isValidPoint(target) && (observed || estimated)) {
         double distance = observed ? player.distanceTo(target) : StrongholdTriangulator.horizontalDistance(player, target);
         return Double.isFinite(distance) ? Math.round(distance) + " m" : "";
      } else {
         return "";
      }
   }

   public record BeamColumn(double init, double load, double read, double write, double update, double sync) {
      public AABB init(double halfWidth, double bottom, double top) {
         return new AABB(this.init - halfWidth, bottom, this.load - halfWidth, this.init + halfWidth, top, this.load + halfWidth);
      }
   }
}
