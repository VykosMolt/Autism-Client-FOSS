package autismclient.util.g.a;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.material.MapColor;

public final class CachePool {
   public static final int init_I = 128;
   public final int[] init_IArr = new int[16384];

   public static CachePool init(byte[] colors) {
      if (colors != null && colors.length == 16384) {
         CachePool img = new CachePool();

         for (int i = 0; i < colors.length; i++) {
            img.init_IArr[i] = MapColor.getColorFromPackedId(colors[i] & 255) & 16777215;
         }

         return img;
      } else {
         return null;
      }
   }

   static int init(int c) {
      return c >> 16 & 0xFF;
   }

   static int load(int c) {
      return c >> 8 & 0xFF;
   }

   static int read(int c) {
      return c & 0xFF;
   }

   static int write(int c) {
      return (init(c) + load(c) + read(c)) / 3;
   }

   static int update(int c) {
      int mx = Math.max(init(c), Math.max(load(c), read(c)));
      int mn = Math.min(init(c), Math.min(load(c), read(c)));
      return mx - mn;
   }

   static boolean[] init_ZArr(boolean[] mask, int w, int h) {
      boolean[] out = new boolean[mask.length];

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            int on = 0;
            int total = 0;

            for (int dy = -1; dy <= 1; dy++) {
               int ny = y + dy;
               if (ny >= 0 && ny < h) {
                  for (int dx = -1; dx <= 1; dx++) {
                     int nx = x + dx;
                     if (nx >= 0 && nx < w) {
                        total++;
                        if (mask[ny * w + nx]) {
                           on++;
                        }
                     }
                  }
               }
            }

            out[y * w + x] = on * 2 > total;
         }
      }

      return out;
   }

   static boolean[] load(boolean[] mask, int w, int h) {
      boolean[] out = new boolean[mask.length];

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            if (mask[y * w + x]
               && x != 0
               && y != 0
               && x != w - 1
               && y != h - 1
               && mask[y * w + x - 1]
               && mask[y * w + x + 1]
               && mask[(y - 1) * w + x]
               && mask[(y + 1) * w + x]) {
               out[y * w + x] = true;
            }
         }
      }

      return out;
   }

   static boolean[] read(boolean[] mask, int w, int h) {
      boolean[] out = new boolean[mask.length];

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            if (mask[y * w + x]
               || x > 0 && mask[y * w + x - 1]
               || x < w - 1 && mask[y * w + x + 1]
               || y > 0 && mask[(y - 1) * w + x]
               || y < h - 1 && mask[(y + 1) * w + x]) {
               out[y * w + x] = true;
            }
         }
      }

      return out;
   }

   static boolean[] write(boolean[] mask, int w, int h) {
      return read(load(mask, w, h), w, h);
   }

   static List<CachePool.Inner_DataUtil> init_List(boolean[] mask, int w, int h) {
      List<CachePool.Inner_DataUtil> blobs = new ArrayList<>();
      int[] label = new int[mask.length];
      int[] stack = new int[mask.length];

      for (int start = 0; start < mask.length; start++) {
         if (mask[start] && label[start] == 0) {
            int sp = 0;
            stack[sp++] = start;
            label[start] = 1;
            CachePool.Inner_DataUtil blob = new CachePool.Inner_DataUtil();
            blob.init_I = 2147483647;
            blob.load = 2147483647;
            blob.read = -2147483648;
            blob.write = -2147483648;
            List<Integer> pixels = new ArrayList<>();

            while (sp > 0) {
               int p = stack[--sp];
               int px = p % w;
               int py = p / w;
               pixels.add(p);
               blob.update++;
               if (px < blob.init_I) {
                  blob.init_I = px;
               }

               if (px > blob.read) {
                  blob.read = px;
               }

               if (py < blob.load) {
                  blob.load = py;
               }

               if (py > blob.write) {
                  blob.write = py;
               }

               sp = init(mask, label, stack, sp, w, h, px - 1, py);
               sp = init(mask, label, stack, sp, w, h, px + 1, py);
               sp = init(mask, label, stack, sp, w, h, px, py - 1);
               sp = init(mask, label, stack, sp, w, h, px, py + 1);
            }

            int bw = blob.init();
            int bh = blob.load();
            blob.init_ZArr = new boolean[bw * bh];

            for (int p : pixels) {
               int px = p % w - blob.init_I;
               int py = p / w - blob.load;
               blob.init_ZArr[py * bw + px] = true;
            }

            blobs.add(blob);
         }
      }

      return blobs;
   }

   private static int init(boolean[] mask, int[] label, int[] stack, int sp, int w, int h, int x, int y) {
      if (x >= 0 && y >= 0 && x < w && y < h) {
         int idx = y * w + x;
         if (mask[idx] && label[idx] == 0) {
            label[idx] = 1;
            stack[sp++] = idx;
            return sp;
         } else {
            return sp;
         }
      } else {
         return sp;
      }
   }

   public static final class Inner_DataUtil {
      public int init_I;
      public int load;
      public int read;
      public int write;
      public int update;
      public boolean[] init_ZArr;

      public int init() {
         return this.read - this.init_I + 1;
      }

      public int load() {
         return this.write - this.load + 1;
      }
   }
}
