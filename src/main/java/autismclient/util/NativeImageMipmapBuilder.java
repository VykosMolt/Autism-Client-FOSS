package autismclient.util;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.ArrayList;
import java.util.List;

public final class NativeImageMipmapBuilder {
   private NativeImageMipmapBuilder() {
   }

   public static NativeImage[] build(NativeImage base, int maxTop) {
      List<NativeImage> levels = new ArrayList<>();
      NativeImage current = base;

      try {
         while (Math.max(current.getWidth(), current.getHeight()) > maxTop && (current.getWidth() > 1 || current.getHeight() > 1)) {
            NativeImage next = halfSize(current);
            if (current != base) {
               current.close();
            }
            current = next;
         }

         if (current != base) {
            base.close();
         }

         levels.add(current);

         while (current.getWidth() > 1 || current.getHeight() > 1) {
            current = halfSize(current);
            levels.add(current);
         }

         return levels.toArray(new NativeImage[0]);
      } catch (Throwable t) {
         for (NativeImage img : levels) {
            if (img != null && img != base) {
               img.close();
            }
         }
         if (current != null && current != base && !levels.contains(current)) {
            current.close();
         }
         throw t;
      }
   }

   public static NativeImage halfSize(NativeImage src) {
      int w = Math.max(1, src.getWidth() / 2);
      int h = Math.max(1, src.getHeight() / 2);
      NativeImage out = new NativeImage(w, h, true);

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            out.setPixel(x, y, averagePixel(src, x * 2, y * 2));
         }
      }

      return out;
   }

   private static int averagePixel(NativeImage src, int x, int y) {
      int a = 0;
      int r = 0;
      int g = 0;
      int b = 0;
      int n = 0;

      for (int dy = 0; dy < 2; dy++) {
         for (int dx = 0; dx < 2; dx++) {
            int sx = x + dx;
            int sy = y + dy;
            if (sx < src.getWidth() && sy < src.getHeight()) {
               int pixel = src.getPixel(sx, sy);
               a += pixel >>> 24 & 0xFF;
               r += pixel >>> 16 & 0xFF;
               g += pixel >>> 8 & 0xFF;
               b += pixel & 0xFF;
               n++;
            }
         }
      }

      return n == 0 ? 0 : a / n << 24 | r / n << 16 | g / n << 8 | b / n;
   }
}
