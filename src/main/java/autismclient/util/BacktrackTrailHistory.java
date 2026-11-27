package autismclient.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BacktrackTrailHistory {
   public static final int init_I = 24;
   public static final long init_J = 450000000L;
   private final ArrayDeque<BacktrackTrailHistory.Inner_StreamUtil> init_ArrayDeque = new ArrayDeque<>();
   private Object target;
   private Vec3 position;
   private long load;
   private long read;
   private BacktrackTrailHistory.Inner_DataUtil init_cP_DataUtil;

   public void init() {
      this.target = null;
      this.position = null;
      this.init_cP_DataUtil = null;
      this.load = this.read = 0L;
      this.init_ArrayDeque.clear();
   }

   public boolean hasRecentTrail(Object target, long now) {
      return this.target == target && this.init_cP_DataUtil != null && now - this.read < 450000000L;
   }

   public void init(Object target, AABB drawn, AABB received, Vec3 position, long now) {
      if (target != null && init(position) && init(drawn.getCenter()) && init(received.getCenter())) {
         if (this.target != target || this.position == null || this.position.distanceToSqr(position) > 16.0) {
            this.init();
            this.target = target;
            this.position = position;
            this.load = now;
         }

         this.init_cP_DataUtil = new BacktrackTrailHistory.Inner_DataUtil(drawn, received, position);
         this.read = now;
         if (this.init_ArrayDeque.isEmpty() || this.init_ArrayDeque.peekLast().init_Vec3().distanceToSqr(position) > 1.0E-4) {
            this.init_ArrayDeque.addLast(new BacktrackTrailHistory.Inner_StreamUtil(position, now));

            while (this.init_ArrayDeque.size() > 24) {
               this.init_ArrayDeque.removeFirst();
            }
         }
      } else {
         this.init();
      }
   }

   public List<autismclient.modules.EspHighlightBox> init(
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
      if (target != null && init(position) && init(drawn.getCenter()) && init(received.getCenter())) {
         if (holding) {
            this.init(target, drawn, received, position, now);
         }

         if (this.target == target && this.init_cP_DataUtil != null && !(this.position.distanceToSqr(position) > 16.0)) {
            double fade = holding ? 1.0 : Math.max(0.0, 1.0 - Math.max(0L, now - this.read) / 4.5E8);
            if (fade <= 0.0) {
               this.init();
               return List.of();
            }

            drawn = this.init_cP_DataUtil.drawn();
            received = this.init_cP_DataUtil.load();
            position = this.init_cP_DataUtil.init_Vec3();
            long elapsed = Math.max(0L, Math.min(250000000L, now - this.load));
            this.load = now;
            this.position = this.position.lerp(position, init_D_J_I(elapsed, smoothingMs));

            while (!this.init_ArrayDeque.isEmpty() && now - this.init_ArrayDeque.peekFirst().init_J() > 650000000L) {
               this.init_ArrayDeque.removeFirst();
            }

            ArrayList<autismclient.modules.EspHighlightBox> boxes = new ArrayList<>();
            int outline = init_I_I_D(receivedColor, fade);
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

            boxes.add(new autismclient.modules.EspHighlightBox(received, "Box".equals(style) ? init_I_I_D(outline, 0.18) : 0, outline));
            if (delayed && drawn.getCenter().distanceToSqr(received.getCenter()) > 4.0E-4) {
               int delayedOutline = init_I_I_D(delayedColor, fade);
               boxes.add(new autismclient.modules.EspHighlightBox(drawn, init_I_I_D(delayedOutline, 0.12), delayedOutline));
               boxes.add(autismclient.modules.EspHighlightBox.init(drawn.getCenter(), received.getCenter(), init_I_I_D(outline, 0.8), 2.0F));
            }

            if (history) {
               BacktrackTrailHistory.Inner_StreamUtil last = null;

               for (BacktrackTrailHistory.Inner_StreamUtil sample : this.init_ArrayDeque) {
                  if (last != null) {
                     double ageFade = Math.max(0.0, 1.0 - (now - sample.init_J()) / 6.5E8);
                     boxes.add(
                        autismclient.modules.EspHighlightBox.init(
                           last.init_Vec3().add(0.0, 0.035, 0.0), sample.init_Vec3().add(0.0, 0.035, 0.0), init_I_I_D(outline, ageFade * 0.8), 1.5F
                        )
                     );
                  }

                  last = sample;
               }
            }

            return List.copyOf(boxes);
         } else {
            this.init();
            return List.of();
         }
      } else {
         this.init();
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
            new AABB(x + a * w, y + b * h, z + c * d, x + e * w, y + f * h, z + g * d), init_I_I_D(color, 0.2), init_I_I_D(color, 0.65)
         )
      );
   }

   public static double init_D_J_I(long deltaNanos, int smoothingMs) {
      if (smoothingMs <= 0) {
         return 1.0;
      } else {
         return deltaNanos <= 0L ? 0.0 : -Math.expm1(-deltaNanos / (smoothingMs * 1000000.0));
      }
   }

   private static int init_I_I_D(int color, double factor) {
      int alpha = (int)Math.round((color >>> 24) * Math.max(0.0, Math.min(1.0, factor)));
      return color & 16777215 | alpha << 24;
   }

   private static boolean init(Vec3 p) {
      return Double.isFinite(p.x) && Double.isFinite(p.y) && Double.isFinite(p.z);
   }

   private record Inner_DataUtil(AABB drawn, AABB load, Vec3 init_Vec3) {
   }

   private record Inner_StreamUtil(Vec3 init_Vec3, long init_J) {
   }
}
