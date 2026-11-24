package autismclient.util;

import com.mojang.blaze3d.platform.NativeImage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public final class aq {
   private aq() {
   }

   public static boolean init_Z(byte[] data) {
      return data != null && data.length > 3 && (data[0] & 255) == 137 && (data[1] & 255) == 80 && (data[2] & 255) == 78;
   }

   public static boolean load(byte[] data) {
      return data != null && data.length > 2 && (data[0] & 255) == 255 && (data[1] & 255) == 216;
   }

   public static byte[] init_BArr(byte[] data) {
      if (init_Z(data)) {
         return data;
      }

      if (!load(data)) {
         return null;
      }

      try {
         BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
         if (image == null) {
            return null;
         }

         ByteArrayOutputStream out = new ByteArrayOutputStream(data.length);
         return !ImageIO.write(image, "png", out) ? null : out.toByteArray();
      } catch (Throwable t) {
         return null;
      }
   }

   public static NativeImage init_NativeImage(byte[] data) {
      if (data == null || data.length == 0) {
         return null;
      }

      if (init_Z(data)) {
         try {
            return NativeImage.read(new ByteArrayInputStream(data));
         } catch (Throwable t) {
            return null;
         }
      } else {
         try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
            if (image == null) {
               return null;
            }

            NativeImage out = new NativeImage(image.getWidth(), image.getHeight(), true);
            try {
               for (int y = 0; y < image.getHeight(); y++) {
                  for (int x = 0; x < image.getWidth(); x++) {
                     out.setPixel(x, y, image.getRGB(x, y));
                  }
               }

               return out;
            } catch (Throwable t) {
               out.close();
               throw t;
            }
         } catch (Throwable t) {
            return null;
         }
      }
   }

   public static NativeImage[] init_NativeImageArr(NativeImage base) {
      return init(base, 512);
   }

   public static NativeImage[] init(NativeImage base, int maxTop) {
      List<NativeImage> levels = new ArrayList<>();
      NativeImage current = base;

      try {
         while (Math.max(current.getWidth(), current.getHeight()) > maxTop && (current.getWidth() > 1 || current.getHeight() > 1)) {
            NativeImage next = init_NativeImage(current);
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
            current = init_NativeImage(current);
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

   public static NativeImage init_NativeImage(NativeImage src) {
      int w = Math.max(1, src.getWidth() / 2);
      int h = Math.max(1, src.getHeight() / 2);
      NativeImage out = new NativeImage(w, h, true);

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            out.setPixel(x, y, init(src, x * 2, y * 2));
         }
      }

      return out;
   }

   private static int init(NativeImage src, int x, int y) {
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
