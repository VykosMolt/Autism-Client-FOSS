package autismclient.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BacktrackTrailHistory {
   public static final int init_I = 24;
   public static final long init_J = 450000000L;
   private final ArrayDeque<BacktrackTrailHistory.TrailPoint> points = new ArrayDeque<>();
   private Object target;
   private Vec3 position;
   private long load;
   private long lastUpdateAt;
   private BacktrackTrailHistory.TrailFrame frame;

   public void clear() {
      this.target = null;
      this.position = null;
      this.frame = null;
      this.load = this.lastUpdateAt = 0L;
      this.points.clear();
   }

   public boolean hasRecentTrail(Object target, long now) {
      return this.target == target && this.frame != null && now - this.lastUpdateAt < 450000000L;
   }

   public void add(Object target, AABB drawn, AABB received, Vec3 position, long now) {
      if (target != null && isFinite(position) && isFinite(drawn.getCenter()) && isFinite(received.getCenter())) {
         if (this.target != target || this.position == null || this.position.distanceToSqr(position) > 16.0) {
            this.clear();
            this.target = target;
            this.position = position;
            this.load = now;
         }

         this.frame = new BacktrackTrailHistory.TrailFrame(drawn, received, position);
         this.lastUpdateAt = now;
         if (this.points.isEmpty() || this.points.peekLast().position().distanceToSqr(position) > 1.0E-4) {
            this.points.addLast(new BacktrackTrailHistory.TrailPoint(position, now));

            while (this.points.size() > 24) {
               this.points.removeFirst();
            }
         }
      } else {
         this.clear();
      }
   }

   public List<autismclient.modules.EspHighlightBox> boxes(
      Object target,
      AABB drawn,
      AABB received,
      Vec3 position,
      long now,
      boolean holding,
      String style,
      boolean delayed,
      boolean history,
      int smoothingMs,
      int receivedColor,
      int delayedColor
   ) {
      if (target != null && isFinite(position) && isFinite(drawn.getCenter()) && isFinite(received.getCenter())) {
         if (holding) {
            this.add(target, drawn, received, position, now);
         }

         if (this.target == target && this.frame != null && !(this.position.distanceToSqr(position) > 16.0)) {
            double fade = holding ? 1.0 : Math.max(0.0, 1.0 - Math.max(0L, now - this.lastUpdateAt) / 4.5E8);
            if (fade <= 0.0) {
               this.clear();
               return List.of();
            }

            drawn = this.frame.drawn();
            received = this.frame.load();
            position = this.frame.position();
            long elapsed = Math.max(0L, Math.min(250000000L, now - this.load));
            this.load = now;
            this.position = this.position.lerp(position, smoothingFactor(elapsed, smoothingMs));

            while (!this.points.isEmpty() && now - this.points.peekFirst().time() > 650000000L) {
               this.points.removeFirst();
            }

            ArrayList<autismclient.modules.EspHighlightBox> boxes = new ArrayList<>();
            int outline = scaleAlpha(receivedColor, fade);
            if (!"Box".equals(style)) {
               AABB ghost = received.move(this.position.subtract(position));
               double x = ghost.minX;
               double y = ghost.minY;
               double z = ghost.minZ;
               double w = ghost.getXsize();
               double h = ghost.getYsize();
               double d = ghost.getZsize();
               init(boxes, x, y, z, w, h, d, 0.2, 0.78, 0.2, 0.8, 1.0, 0.8, outline);
               init(boxes, x, y, z, w, h, d, 0.2, 0.4, 0.27, 0.8, 0.77, 0.73, outline);
               init(boxes, x, y, z, w, h, d, 0.0, 0.4, 0.3, 0.18, 0.77, 0.7, outline);
               init(boxes, x, y, z, w, h, d, 0.82, 0.4, 0.3, 1.0, 0.77, 0.7, outline);
               init(boxes, x, y, z, w, h, d, 0.22, 0.0, 0.3, 0.47, 0.39, 0.7, outline);
               init(boxes, x, y, z, w, h, d, 0.53, 0.0, 0.3, 0.78, 0.39, 0.7, outline);
            }

            boxes.add(new autismclient.modules.EspHighlightBox(received, "Box".equals(style) ? scaleAlpha(outline, 0.18) : 0, outline));
            if (delayed && drawn.getCenter().distanceToSqr(received.getCenter()) > 4.0E-4) {
               int delayedOutline = scaleAlpha(delayedColor, fade);
               boxes.add(new autismclient.modules.EspHighlightBox(drawn, scaleAlpha(delayedOutline, 0.12), delayedOutline));
               boxes.add(autismclient.modules.EspHighlightBox.line(drawn.getCenter(), received.getCenter(), scaleAlpha(outline, 0.8), 2.0F));
            }

            if (history) {
               BacktrackTrailHistory.TrailPoint last = null;

               for (BacktrackTrailHistory.TrailPoint sample : this.points) {
                  if (last != null) {
                     double ageFade = Math.max(0.0, 1.0 - (now - sample.time()) / 6.5E8);
                     boxes.add(
                        autismclient.modules.EspHighlightBox.line(
                           last.position().add(0.0, 0.035, 0.0), sample.position().add(0.0, 0.035, 0.0), scaleAlpha(outline, ageFade * 0.8), 1.5F
                        )
                     );
                  }

                  last = sample;
               }
            }

            return List.copyOf(boxes);
         } else {
            this.clear();
            return List.of();
         }
      } else {
         this.clear();
         return List.of();
      }
   }

   private static void init(
      List<autismclient.modules.EspHighlightBox> result,
      double x,
      double y,
      double z,
      double w,
      double h,
      double d,
      double a,
      double b,
      double c,
      double e,
      double f,
      double g,
      int color
   ) {
      result.add(
         new autismclient.modules.EspHighlightBox(
            new AABB(x + a * w, y + b * h, z + c * d, x + e * w, y + f * h, z + g * d), scaleAlpha(color, 0.2), scaleAlpha(color, 0.65)
         )
      );
   }

   public static double smoothingFactor(long deltaNanos, int smoothingMs) {
      if (smoothingMs <= 0) {
         return 1.0;
      } else {
         return deltaNanos <= 0L ? 0.0 : -Math.expm1(-deltaNanos / (smoothingMs * 1000000.0));
      }
   }

   private static int scaleAlpha(int color, double factor) {
      int alpha = (int)Math.round((color >>> 24) * Math.max(0.0, Math.min(1.0, factor)));
      return color & 16777215 | alpha << 24;
   }

   private static boolean isFinite(Vec3 p) {
      return Double.isFinite(p.x) && Double.isFinite(p.y) && Double.isFinite(p.z);
   }

   private record TrailFrame(AABB drawn, AABB load, Vec3 position) {
   }

   private record TrailPoint(Vec3 position, long time) {
   }
}
