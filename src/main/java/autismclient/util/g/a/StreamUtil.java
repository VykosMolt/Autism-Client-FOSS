package autismclient.util.g.a;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class StreamUtil {
   static final int init_I = 32;
   static final int load = 48;
   private static final double init_D = 0.12;
   private static final int read = 22;
   private static final FontRenderContext init_FontRenderContext = new FontRenderContext(null, true, true);
   private final List<StreamUtil.Inner_CachePool> init_List = new ArrayList<>();
   private final boolean init_Z;
   private final String init_Str;
   private ConfigStore init_ConfigStore;

   public StreamUtil(Font font, String charset, double[] shears, boolean heightAware) {
      this.init_Z = heightAware;
      this.init_Str = charset;

      for (int i = 0; i < charset.length(); i++) {
         char ch = charset.charAt(i);

         for (double shear : shears) {
            StreamUtil.Inner_DataUtil r = this.init_StreamUtil_DataUti_Font(font, ch, shear);
            if (r != null && r.init_FArr() != null) {
               float[] cc = load(r.init_FArr());
               double norm = init_D_FArr(cc);
               if (norm > 1.0E-6) {
                  this.init_List.add(new StreamUtil.Inner_CachePool(ch, cc, norm, r.init_D(), init_Z_ZArr(r.init_ZArr())));
               }
            }
         }
      }
   }

   public boolean init() {
      return this.init_Z;
   }

   public boolean load() {
      return this.init_ConfigStore != null;
   }

   public float[] init_FArr_FArr(float[] cov) {
      return this.init_ConfigStore == null ? null : this.init_ConfigStore.init_FArr_FArr(cov);
   }

   public void init_V_ConfigStore(ConfigStore net) {
      if (net != null && (net.init_I() == this.init_Str.length() || net.init_I() == this.init_Str.length() + 1)) {
         this.init_ConfigStore = net;
      }
   }

   private StreamUtil.Inner_DataUtil init_StreamUtil_DataUti_Font(Font font, char ch, double shear) {
      GlyphVector gv = font.createGlyphVector(init_FontRenderContext, String.valueOf(ch));
      Shape outline = gv.getOutline();
      if (Math.abs(shear) > 1.0E-6) {
         AffineTransform sh = AffineTransform.getShearInstance(shear, shear);
         outline = sh.createTransformedShape(outline);
      }

      Rectangle2D b = outline.getBounds2D();
      if (!(b.getWidth() < 1.0) && !(b.getHeight() < 1.0)) {
         double aspect = b.getWidth() / b.getHeight();
         int rw = (int)Math.ceil(b.getWidth()) + 2;
         int rh = (int)Math.ceil(b.getHeight()) + 2;
         BufferedImage img = new BufferedImage(rw, rh, 10);
         Graphics2D g = img.createGraphics();
         g.setColor(Color.BLACK);
         g.fillRect(0, 0, rw, rh);
         g.translate(1.0 - b.getX(), 1.0 - b.getY());
         g.setColor(Color.WHITE);
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g.fill(outline);
         g.dispose();
         int[] gray = new int[rw * rh];
         boolean[] src = new boolean[rw * rh];

         for (int y = 0; y < rh; y++) {
            for (int x = 0; x < rw; x++) {
               int v = img.getRGB(x, y) & 0xFF;
               gray[y * rw + x] = v;
               src[y * rw + x] = v > 96;
            }
         }

         return new StreamUtil.Inner_DataUtil(init_FArr_IArr(gray, rw, rh), init_ZArr_ZArr(src, rw, rh), aspect);
      } else {
         return null;
      }
   }

   static float[] init_FArr_IArr(int[] gray, int w, int h) {
      float[] out = new float[1536];
      if (w > 0 && h > 0) {
         for (int gy = 0; gy < 48; gy++) {
            int ya = gy * h / 48;
            int yb = Math.max(ya + 1, (gy + 1) * h / 48);

            for (int gx = 0; gx < 32; gx++) {
               int xa = gx * w / 32;
               int xb = Math.max(xa + 1, (gx + 1) * w / 32);
               double sum = 0.0;
               int tot = 0;

               for (int y = ya; y < yb && y < h; y++) {
                  for (int x = xa; x < xb && x < w; x++) {
                     sum += gray[y * w + x];
                     tot++;
                  }
               }

               out[gy * 32 + gx] = tot == 0 ? 0.0F : (float)(sum / (tot * 255.0));
            }
         }

         return out;
      } else {
         return out;
      }
   }

   static float[] init_FArr_ZArr(boolean[] src, int w, int h) {
      float[] out = new float[1536];
      if (w > 0 && h > 0) {
         for (int gy = 0; gy < 48; gy++) {
            int ya = gy * h / 48;
            int yb = Math.max(ya + 1, (gy + 1) * h / 48);

            for (int gx = 0; gx < 32; gx++) {
               int xa = gx * w / 32;
               int xb = Math.max(xa + 1, (gx + 1) * w / 32);
               int cnt = 0;
               int tot = 0;

               for (int y = ya; y < yb && y < h; y++) {
                  for (int x = xa; x < xb && x < w; x++) {
                     tot++;
                     if (src[y * w + x]) {
                        cnt++;
                     }
                  }
               }

               out[gy * 32 + gx] = tot == 0 ? 0.0F : (float)cnt / tot;
            }
         }

         return out;
      } else {
         return out;
      }
   }

   static boolean[] init_ZArr_ZArr(boolean[] src, int w, int h) {
      boolean[] out = new boolean[1536];
      if (w > 0 && h > 0) {
         for (int gy = 0; gy < 48; gy++) {
            int sy = (int)((gy + 0.5) * h / 48.0);
            if (sy >= h) {
               sy = h - 1;
            }

            for (int gx = 0; gx < 32; gx++) {
               int sx = (int)((gx + 0.5) * w / 32.0);
               if (sx >= w) {
                  sx = w - 1;
               }

               out[gy * 32 + gx] = src[sy * w + sx];
            }
         }

         return out;
      } else {
         return out;
      }
   }

   private static float[] load(float[] v) {
      double mean = 0.0;

      for (float f : v) {
         mean += f;
      }

      mean /= v.length;
      float[] out = new float[v.length];

      for (int i = 0; i < v.length; i++) {
         out[i] = (float)(v[i] - mean);
      }

      return out;
   }

   private static double init_D_FArr(float[] v) {
      double s = 0.0;

      for (float f : v) {
         s += (double)f * f;
      }

      return Math.sqrt(s);
   }

   public StreamUtil.Inner_StreamUtil init_StreamUtil_StreamU_ZArr(boolean[] cellMask, int w, int h) {
      List<StreamUtil.Inner_StreamUtil> ranked = this.init_List_ZArr(cellMask, w, h, 1);
      return ranked.isEmpty() ? new StreamUtil.Inner_StreamUtil('\u0000', 0.0) : ranked.get(0);
   }

   public List<StreamUtil.Inner_StreamUtil> init_List_ZArr(boolean[] cellMask, int w, int h, int k) {
      double cellAspect = h > 0 ? (double)w / h : 1.0;
      float[] cov = init_FArr_ZArr(cellMask, w, h);
      if (this.init_ConfigStore != null) {
         float[] probs = this.init_ConfigStore.init_FArr_FArr(cov);
         if (probs != null) {
            List<StreamUtil.Inner_StreamUtil> out = new ArrayList<>(this.init_Str.length());

            for (int i = 0; i < this.init_Str.length(); i++) {
               out.add(new StreamUtil.Inner_StreamUtil(this.init_Str.charAt(i), probs[i]));
            }

            out.sort((a, b) -> Double.compare(b.init_D(), a.init_D()));
            return out.size() > k ? out.subList(0, k) : out;
         }
      }

      float[] cc = load(cov);
      double cellNorm = init_D_FArr(cc);
      if (cellNorm < 1.0E-6) {
         return List.of();
      }

      boolean cellHole = this.init_Z && init_Z_ZArr(cellMask, w, h, Math.max(14, (int)(0.03 * w * h)));
      Map<Character, Double> bestPerChar = new HashMap<>();

      for (StreamUtil.Inner_CachePool t : this.init_List) {
         double dot = 0.0;

         for (int i = 0; i < cc.length; i++) {
            dot += (double)cc[i] * t.init_FArr[i];
         }

         double corr = dot / (cellNorm * t.init_D);
         double aspectSim = init(cellAspect, t.load);
         double score = Math.max(0.0, corr) * (0.65 + 0.35 * aspectSim);
         if (this.init_Z) {
            score += t.init_Z == cellHole ? 0.12 : -0.12;
         }

         Double prev = bestPerChar.get(t.init_C);
         if (prev == null || score > prev) {
            bestPerChar.put(t.init_C, score);
         }
      }

      List<StreamUtil.Inner_StreamUtil> out = new ArrayList<>();

      for (Entry<Character, Double> e : bestPerChar.entrySet()) {
         out.add(new StreamUtil.Inner_StreamUtil(e.getKey(), e.getValue()));
      }

      out.sort((a, b) -> Double.compare(b.init_D(), a.init_D()));
      return out.size() > k ? out.subList(0, k) : out;
   }

   static boolean init_Z_ZArr(boolean[] mask) {
      return load(mask, 32, 48, 22);
   }

   static boolean init_Z_ZArr(boolean[] mask, int w, int h, int minArea) {
      boolean[] sealed = CachePool.read(mask, w, h);
      return load(sealed, w, h, minArea);
   }

   static boolean load(boolean[] mask, int w, int h, int minArea) {
      int n = w * h;
      boolean[] reached = new boolean[n];
      int[] stack = new int[n];
      int sp = 0;

      for (int x = 0; x < w; x++) {
         int top = x;
         int bot = (h - 1) * w + x;
         if (!mask[top] && !reached[top]) {
            reached[top] = true;
            stack[sp++] = top;
         }

         if (!mask[bot] && !reached[bot]) {
            reached[bot] = true;
            stack[sp++] = bot;
         }
      }

      for (int y = 0; y < h; y++) {
         int left = y * w;
         int right = y * w + w - 1;
         if (!mask[left] && !reached[left]) {
            reached[left] = true;
            stack[sp++] = left;
         }

         if (!mask[right] && !reached[right]) {
            reached[right] = true;
            stack[sp++] = right;
         }
      }

      while (sp > 0) {
         int p = stack[--sp];
         int px = p % w;
         int py = p / w;
         if (px > 0) {
            sp = init(mask, reached, stack, sp, p - 1);
         }

         if (px < w - 1) {
            sp = init(mask, reached, stack, sp, p + 1);
         }

         if (py > 0) {
            sp = init(mask, reached, stack, sp, p - w);
         }

         if (py < h - 1) {
            sp = init(mask, reached, stack, sp, p + w);
         }
      }

      int enclosed = 0;

      for (int i = 0; i < n; i++) {
         if (!mask[i] && !reached[i]) {
            enclosed++;
         }
      }

      return enclosed >= minArea;
   }

   private static int init(boolean[] mask, boolean[] reached, int[] stack, int sp, int idx) {
      if (!mask[idx] && !reached[idx]) {
         reached[idx] = true;
         stack[sp++] = idx;
      }

      return sp;
   }

   private static double init(double a, double b) {
      return !(a <= 0.0) && !(b <= 0.0) ? Math.min(a, b) / Math.max(a, b) : 1.0;
   }

   private record Inner_CachePool(char init_C, float[] init_FArr, double init_D, double load, boolean init_Z) {
   }

   private record Inner_DataUtil(float[] init_FArr, boolean[] init_ZArr, double init_D) {
   }

   public record Inner_StreamUtil(char init_C, double init_D) {
   }
}
