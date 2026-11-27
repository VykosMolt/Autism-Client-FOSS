package autismclient.modules;

import java.util.List;
import java.util.Locale;

public final class CrosshairPresetGeometry {
   public static final double init = 15.0;
   public static final double load = 16.0;

   private CrosshairPresetGeometry() {
   }

   static int init(double thickness, double unit) {
      return Math.max(1, (int)Math.round(thickness * unit));
   }

   public static CrosshairPresetGeometry.Inner_CachePool init(CrosshairPresetGeometry.Inner_NetHelper style, double unit, double thickness, double gap, int outlinePx, int pad) {
      double u = Math.max(0.05, unit);
      int t = init(thickness, u);
      int s0;
      List<CrosshairPresetGeometry.Inner_ConfigStore> shapes;
      switch (style) {
         case TACTICAL: {
            int g = Math.max(0, (int)Math.round(gap * u));
            int l = Math.max(1, (int)Math.round(5.0 * u));
            int c = g + l;
            s0 = 2 * c + t;
            shapes = List.of(
               new CrosshairPresetGeometry.Inner_DataUtil(0.0, c, l, c + t),
               new CrosshairPresetGeometry.Inner_DataUtil(c + t + g, c, s0, c + t),
               new CrosshairPresetGeometry.Inner_DataUtil(c, 0.0, c + t, l),
               new CrosshairPresetGeometry.Inner_DataUtil(c, c + t + g, c + t, s0),
               new CrosshairPresetGeometry.Inner_DataUtil(c, c, c + t, c + t)
            );
            break;
         }
         case CIRCLE: {
            double outer = 7.0 * u;
            double half = t / 2.0;
            double dot = Math.max(1.0, t * 0.75);
            double radius = Math.max(dot + half + 1.0, outer - half);
            s0 = 2 * (int)Math.ceil(radius + half) + (t & 1);
            double c = s0 / 2.0;
            shapes = List.of(new CrosshairPresetGeometry.Inner_BufferHelper(c, c, radius, half), new CrosshairPresetGeometry.Inner_StreamUtil(c, c, dot));
            break;
         }
         case CHEVRON: {
            double length = 7.0 * u;
            double half = t / 2.0;
            double dx = length * Math.cos(Math.toRadians(45.0));
            double dy = length * Math.sin(Math.toRadians(45.0));
            s0 = 2 * (int)Math.ceil(Math.max(dx, dy) + half) + (t & 1);
            double c = s0 / 2.0;
            shapes = List.of(new CrosshairPresetGeometry.Inner_TaskQueue(c, c, c - dx, c + dy, half), new CrosshairPresetGeometry.Inner_TaskQueue(c, c, c + dx, c + dy, half));
            break;
         }
         default:
            int a = Math.max(1, (int)Math.round(7.0 * u));
            s0 = 2 * a + t;
            shapes = List.of(new CrosshairPresetGeometry.Inner_DataUtil(0.0, a, s0, a + t), new CrosshairPresetGeometry.Inner_DataUtil(a, 0.0, a + t, s0));
      }

      return init_p_CachePool_List(shapes, s0, Math.max(0, outlinePx), Math.max(0, pad));
   }

   public static CrosshairPresetGeometry.Inner_CachePool rasterize(CrosshairPixelArt art, double unit, int outlinePx, int pad) {
      int n = art.size();
      int s0 = Math.max(1, (int)Math.round(16.0 * Math.max(0.05, unit)));
      pad = Math.max(0, pad);
      outlinePx = Math.max(0, outlinePx);
      int size = s0 + 2 * pad;
      boolean[] solid = new boolean[size * size];
      float[] fill = new float[size * size];
      int[] color = new int[size * size];
      double unitPerPixel = (double)n / s0;

      for (int y = 0; y < s0; y++) {
         for (int x = 0; x < s0; x++) {
            int p = art.init((int)((x + 0.5) * unitPerPixel), (int)((y + 0.5) * unitPerPixel));
            if (p >>> 24 == 0 && s0 < n) {
               p = init_I_m(art, x, y, unitPerPixel);
            }

            int i = (y + pad) * size + x + pad;
            fill[i] = (p >>> 24) / 255.0F;
            solid[i] = fill[i] > 0.0F;
            color[i] = p;
         }
      }

      float[] outline = new float[size * size];
      if (outlinePx > 0) {
         boolean[] grown = init_ZArr_ZArr(solid, size, outlinePx);

         for (int i = 0; i < outline.length; i++) {
            if (grown[i] && !solid[i]) {
               outline[i] = 1.0F;
            }
         }
      }

      return new CrosshairPresetGeometry.Inner_CachePool(size, fill, color, outline);
   }

   private static int init_I_m(CrosshairPixelArt art, int x, int y, double unit) {
      int p = 0;
      int x1 = Math.min(art.size(), (int)Math.ceil((x + 1) * unit));
      int y1 = Math.min(art.size(), (int)Math.ceil((y + 1) * unit));

      for (int dy = (int)(y * unit); dy < y1; dy++) {
         for (int dx = (int)(x * unit); dx < x1; dx++) {
            int sample = art.init(dx, dy);
            if (sample >>> 24 > p >>> 24) {
               p = sample;
            }

            if (p >>> 24 == 255) {
               return p;
            }
         }
      }

      return p;
   }

   public static CrosshairPixelArt init(CrosshairPresetGeometry.Inner_NetHelper style, int size, double thickness, double gap, int argb) {
      int n = CrosshairPixelArt.isValidSize(size) ? size : 16;
      CrosshairPresetGeometry.Inner_CachePool raster = init(style.isPreset() ? style : CrosshairPresetGeometry.Inner_NetHelper.CROSS, (n - 1) / 15.0, thickness, gap, 0, 0);
      int[] pixels = new int[n * n];
      int offset = (n - raster.pixelCount()) / 2;
      int baseAlpha = argb >>> 24;

      for (int y = 0; y < raster.pixelCount(); y++) {
         int ty = y + offset;
         if (ty >= 0 && ty < n) {
            for (int x = 0; x < raster.pixelCount(); x++) {
               int tx = x + offset;
               if (tx >= 0 && tx < n) {
                  float cover = raster.coverage()[y * raster.pixelCount() + x];
                  if (!(cover <= 0.0F)) {
                     int a = Math.round(baseAlpha * cover);
                     if (a > 0) {
                        pixels[ty * n + tx] = a << 24 | argb & 16777215;
                     }
                  }
               }
            }
         }
      }

      return new CrosshairPixelArt(n, pixels);
   }

   private static CrosshairPresetGeometry.Inner_CachePool init_p_CachePool_List(List<CrosshairPresetGeometry.Inner_ConfigStore> shapes, int s0, int outlinePx, int pad) {
      int size = s0 + 2 * pad;
      float[] fill = new float[size * size];
      float[] outline = new float[size * size];

      for (int y = 0; y < size; y++) {
         for (int x = 0; x < size; x++) {
            int i = y * size + x;
            double d = init_D_List(shapes, x + 0.5 - pad, y + 0.5 - pad);
            if (d <= 1.0E-9) {
               fill[i] = 1.0F;
            } else if (outlinePx > 0 && d <= outlinePx + 1.0E-9) {
               outline[i] = 1.0F;
            }
         }
      }

      return new CrosshairPresetGeometry.Inner_CachePool(size, fill, null, outline);
   }

   private static double init_D_List(List<CrosshairPresetGeometry.Inner_ConfigStore> shapes, double x, double y) {
      double best = 1.7976931348623157E308;

      for (CrosshairPresetGeometry.Inner_ConfigStore shape : shapes) {
         best = Math.min(best, shape.init(x, y));
      }

      return best;
   }

   private static boolean[] init_ZArr_ZArr(boolean[] in, int grid, int radius) {
      if (radius <= 0) {
         return (boolean[])in.clone();
      }

      boolean[] rows = new boolean[in.length];
      int[] prefix = new int[grid + 1];

      for (int y = 0; y < grid; y++) {
         int base = y * grid;

         for (int x = 0; x < grid; x++) {
            prefix[x + 1] = prefix[x] + (in[base + x] ? 1 : 0);
         }

         for (int x = 0; x < grid; x++) {
            rows[base + x] = prefix[Math.min(grid, x + radius + 1)] - prefix[Math.max(0, x - radius)] > 0;
         }
      }

      boolean[] out = new boolean[in.length];

      for (int x = 0; x < grid; x++) {
         for (int y = 0; y < grid; y++) {
            prefix[y + 1] = prefix[y] + (rows[y * grid + x] ? 1 : 0);
         }

         for (int y = 0; y < grid; y++) {
            out[y * grid + x] = prefix[Math.min(grid, y + radius + 1)] - prefix[Math.max(0, y - radius)] > 0;
         }
      }

      return out;
   }

   private record Inner_BufferHelper(double init, double load, double read, double write) implements CrosshairPresetGeometry.Inner_ConfigStore {
      @Override
      public double init(double x, double y) {
         return Math.abs(Math.hypot(x - this.init, y - this.load) - this.read) - this.write;
      }
   }

   public record Inner_CachePool(int pixelCount, float[] coverage, int[] init_IArr, float[] load) {
      public int init(int index) {
         return this.init_IArr == null ? -1 : this.init_IArr[index];
      }
   }

   private interface Inner_ConfigStore {
      double init(double var1, double var3);
   }

   private record Inner_DataUtil(double init, double load, double read, double write) implements CrosshairPresetGeometry.Inner_ConfigStore {
      @Override
      public double init(double x, double y) {
         double dx = Math.max(this.init - x, x - this.read);
         double dy = Math.max(this.load - y, y - this.write);
         return dx <= 0.0 && dy <= 0.0 ? Math.max(dx, dy) : Math.hypot(Math.max(dx, 0.0), Math.max(dy, 0.0));
      }
   }

   public enum Inner_NetHelper {
      CROSS("Cross"),
      TACTICAL("Tactical"),
      CIRCLE("Circle"),
      CHEVRON("Chevron"),
      CUSTOM("Custom");

      public final String init_Str;

      Inner_NetHelper(String label) {
         this.init_Str = label;
      }

      public boolean isPreset() {
         return this != CUSTOM;
      }

      public static CrosshairPresetGeometry.Inner_NetHelper init(String label) {
         if (label != null) {
            for (CrosshairPresetGeometry.Inner_NetHelper style : values()) {
               if (style.init_Str.equalsIgnoreCase(label.trim())) {
                  return style;
               }
            }
         }

         return CROSS;
      }

      public static String[] ids() {
         CrosshairPresetGeometry.Inner_NetHelper[] all = values();
         String[] out = new String[all.length];

         for (int i = 0; i < all.length; i++) {
            out[i] = all[i].init_Str;
         }

         return out;
      }

      @Override
      public String toString() {
         return this.init_Str.toLowerCase(Locale.ROOT);
      }
   }

   private record Inner_StreamUtil(double init, double load, double read) implements CrosshairPresetGeometry.Inner_ConfigStore {
      @Override
      public double init(double x, double y) {
         return Math.hypot(x - this.init, y - this.load) - this.read;
      }
   }

   private record Inner_TaskQueue(double init, double load, double read, double write, double update) implements CrosshairPresetGeometry.Inner_ConfigStore {
      @Override
      public double init(double x, double y) {
         double vx = this.read - this.init;
         double vy = this.write - this.load;
         double t = Math.max(0.0, Math.min(1.0, ((x - this.init) * vx + (y - this.load) * vy) / (vx * vx + vy * vy)));
         return Math.hypot(x - (this.init + vx * t), y - (this.load + vy * t)) - this.update;
      }
   }
}
