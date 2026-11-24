package autismclient.util.g.a;

import java.awt.Font;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class BufferHelper {
   private static final String init_Str = "abcdefhjkmnoprstuxyz";
   private static final String load_Str = "0123456789";
   private static final int init_I = 3;
   private static volatile StreamUtil init_StreamUtil;
   private static volatile StreamUtil load_StreamUtil;
   private static final Object init_Obj = new Object();
   private static final double read_D = 0.15;
   static final double init_D = 0.82;
   static final double load_D = 0.26;
   private static final int load_I = "abcdefhjkmnoprstuxyz".length();
   private static final int read_I = 24;
   private static final double write = 0.55;
   private static final double update = 0.4;

   private BufferHelper() {
   }

   public static BufferHelper.Inner_CachePool init_BufferHelper_Cache(CachePool img) {
      List<BufferHelper.Inner_CachePool> ranked = init_List_CachePool_I(img, 1);
      return ranked.isEmpty() ? new BufferHelper.Inner_CachePool("", 0.0) : ranked.get(0);
   }

   private static BufferHelper.Inner_BufferHelper init_BufferHelper_Buffe(CachePool img) {
      if (init_Z(img)) {
         boolean[] fg = new boolean[16384];

         for (int i = 0; i < fg.length; i++) {
            fg[i] = CachePool.write(img.init_IArr[i]) > 170;
         }

         fg = CachePool.init_ZArr(fg, 128, 128);
         return new BufferHelper.Inner_BufferHelper(fg, init(), 3, 6);
      } else {
         boolean[] fg = init_ZArr(img);
         fg = CachePool.init_ZArr(fg, 128, 128);
         fg = init_ZArr(fg, 128, 128, 2, 14);
         fg = load(fg);
         fg = init_ZArr(fg);
         return new BufferHelper.Inner_BufferHelper(fg, load(), 3, 4);
      }
   }

   public static List<BufferHelper.Inner_CachePool> init_List_CachePool_I(CachePool img, int maxCandidates) {
      if (img == null) {
         return List.of();
      }

      BufferHelper.Inner_BufferHelper prepared = init_BufferHelper_Buffe(img);
      boolean[] fg = prepared.init_ZArr();
      StreamUtil matcher = prepared.init_StreamUtil();
      int minLen = prepared.init_I();
      int maxLen = prepared.load();
      int[] box = init_IArr(fg);
      if (box == null) {
         return List.of();
      }

      if (!init_Z_ZArr_IArr(fg, box)) {
         return List.of();
      }

      if (matcher.load()) {
         List<BufferHelper.Inner_CachePool> ctc = init_List_ZArr(fg, box, matcher, maxCandidates);
         if (!ctc.isEmpty()) {
            return ctc;
         }
      }

      Map<String, Double> merged = new LinkedHashMap<>();

      for (int len = minLen; len <= maxLen; len++) {
         BufferHelper.Inner_ConfigStore s = init(fg, box, len);
         if (s != null) {
            init_V_List(s.init_List(), matcher, merged, -0.15 * s.init_D());
         }
      }

      List<BufferHelper.Inner_CachePool> out = new ArrayList<>();

      for (Entry<String, Double> e : merged.entrySet()) {
         out.add(new BufferHelper.Inner_CachePool(e.getKey(), e.getValue()));
      }

      out.sort((a, b) -> Double.compare(b.init_D(), a.init_D()));
      return out.size() > maxCandidates ? out.subList(0, maxCandidates) : out;
   }

   private static boolean init_Z_ZArr_IArr(boolean[] fg, int[] box) {
      int size = 128;
      int ink = 0;

      for (boolean b : fg) {
         if (b) {
            ink++;
         }
      }

      double frac = (double)ink / (size * size);
      int bw = box[2] - box[0] + 1;
      int bh = box[3] - box[1] + 1;
      if (!(frac < 0.008) && !(frac > 0.42)) {
         if (bh > size * 0.75) {
            return false;
         }

         if (bw < size * 0.1) {
            return false;
         }

         int comps = CachePool.init_List(fg, size, size).size();
         return comps >= 1 && comps <= 60;
      } else {
         return false;
      }
   }

   private static List<BufferHelper.Inner_CachePool> init_List_ZArr(boolean[] fg, int[] box, StreamUtil matcher, int maxCandidates) {
      int h = box[3] - box[1] + 1;
      if (h < 8) {
         return List.of();
      }

      int winW = Math.max(6, (int)Math.round(0.82 * h));
      int step = Math.max(2, (int)Math.round(0.26 * h));
      List<float[]> post = new ArrayList<>();

      for (int wc = box[0]; wc <= box[2]; wc += step) {
         float[] cov = init_FArr_ZArr(fg, wc, winW, box);
         if (cov != null) {
            float[] p = matcher.init_FArr_FArr(cov);
            if (p == null || p.length != load_I + 1) {
               return List.of();
            }

            post.add(p);
         }
      }

      return post.isEmpty() ? List.of() : init_List_List_I(post, maxCandidates);
   }

   static float[] init_FArr_ZArr(boolean[] fg, int wc, int winW, int[] box) {
      int x0 = Math.max(box[0], wc - winW / 2);
      int x1 = Math.min(box[2], wc + winW / 2);
      if (x1 < x0) {
         return null;
      }

      int y0 = box[1];
      int y1 = box[3];
      int w = x1 - x0 + 1;
      int hh = y1 - y0 + 1;
      boolean[] cell = new boolean[w * hh];

      for (int y = 0; y < hh; y++) {
         for (int x = 0; x < w; x++) {
            cell[y * w + x] = fg[(y0 + y) * 128 + x0 + x];
         }
      }

      return StreamUtil.init_FArr_ZArr(cell, w, hh);
   }

   private static List<BufferHelper.Inner_CachePool> init_List_List_I(List<float[]> post, int maxCandidates) {
      Map<String, double[]> beams = new HashMap<>();
      beams.put("", new double[]{1.0, 0.0});

      for (float[] p : post) {
         Map<String, double[]> next = new HashMap<>();

         for (Entry<String, double[]> e : beams.entrySet()) {
            String prefix = e.getKey();
            double pb = e.getValue()[0];
            double pnb = e.getValue()[1];
            double ptot = pb + pnb;
            if (!(ptot <= 1.0E-9)) {
               init_V_Map(next, prefix, ptot * p[load_I], 0.0);
               char last = prefix.isEmpty() ? '\u0000' : prefix.charAt(prefix.length() - 1);

               for (int c = 0; c < load_I; c++) {
                  double pc = p[c];
                  if (!(pc < 1.0E-4)) {
                     char ch = "abcdefhjkmnoprstuxyz".charAt(c);
                     if (ch == last) {
                        init_V_Map(next, prefix, 0.0, pnb * pc);
                        init_V_Map(next, prefix + ch, 0.0, pb * pc * 0.55);
                     } else {
                        init_V_Map(next, prefix + ch, 0.0, ptot * pc * 0.55);
                     }
                  }
               }
            }
         }

         beams = init_Map_Map_I(next, 24);
      }

      List<BufferHelper.Inner_CachePool> ranked = new ArrayList<>();

      for (Entry<String, double[]> e : beams.entrySet()) {
         String s = e.getKey();
         if (!s.isEmpty()) {
            ranked.add(new BufferHelper.Inner_CachePool(s, e.getValue()[0] + e.getValue()[1]));
         }
      }

      ranked.sort((a, b) -> {
         boolean la = a.init_Str().length() >= 3 && a.init_Str().length() <= 4;
         boolean lb = b.init_Str().length() >= 3 && b.init_Str().length() <= 4;
         if (la != lb) {
            return la ? -1 : 1;
         } else {
            return Double.compare(b.init_D(), a.init_D());
         }
      });
      return ranked.size() > maxCandidates ? ranked.subList(0, maxCandidates) : ranked;
   }

   private static void init_V_Map(Map<String, double[]> m, String key, double addBlank, double addNonBlank) {
      double[] v = m.get(key);
      if (v == null) {
         v = new double[2];
         m.put(key, v);
      }

      v[0] += addBlank;
      v[1] += addNonBlank;
   }

   private static Map<String, double[]> init_Map_Map_I(Map<String, double[]> beams, int width) {
      if (beams.size() <= width) {
         return beams;
      }

      List<Entry<String, double[]>> es = new ArrayList<>(beams.entrySet());
      es.sort((a, b) -> Double.compare(b.getValue()[0] + b.getValue()[1], a.getValue()[0] + a.getValue()[1]));
      Map<String, double[]> out = new HashMap<>();

      for (int i = 0; i < width; i++) {
         out.put(es.get(i).getKey(), es.get(i).getValue());
      }

      return out;
   }

   private static void init_V_List(List<BufferHelper.Inner_StreamUtil> cells, StreamUtil matcher, Map<String, Double> into, double bonus) {
      if (cells != null && !cells.isEmpty()) {
         record Inner_DataUtil(String init_Str, double init_D, double load) {
         }

         List<Inner_DataUtil> beam = new ArrayList<>();
         beam.add(new Inner_DataUtil("", 0.0, 1.0 / 0.0));

         for (BufferHelper.Inner_StreamUtil cell : cells) {
            List<StreamUtil.Inner_StreamUtil> topk = matcher.init_List_ZArr(cell.init_ZArr, cell.init_I, cell.load, 3);
            if (topk.isEmpty()) {
               return;
            }

            List<Inner_DataUtil> next = new ArrayList<>();

            for (Inner_DataUtil p : beam) {
               for (StreamUtil.Inner_StreamUtil r : topk) {
                  next.add(new Inner_DataUtil(p.init_Str() + r.init_C(), p.init_D() + r.init_D(), Math.min(p.load(), r.init_D())));
               }
            }

            next.sort((a, b) -> Double.compare(b.init_D(), a.init_D()));
            beam = next.size() > 8 ? next.subList(0, 8) : next;
         }

         int n = cells.size();

         for (Inner_DataUtil p : beam) {
            double avg = p.init_D() / n;
            double conf = Math.min(1.0, 0.6 * avg + 0.4 * p.load() + bonus);
            into.merge(p.init_Str(), conf, Math::max);
         }
      }
   }

   private static BufferHelper.Inner_ConfigStore init(boolean[] fg, int[] box, int len) {
      int x0 = box[0];
      int y0 = box[1];
      int x1 = box[2];
      int y1 = box[3];
      int totalW = x1 - x0 + 1;
      int h = y1 - y0 + 1;
      if (totalW >= len && h >= 6) {
         int[] colInk = new int[totalW];
         int maxCol = 1;

         for (int x = 0; x < totalW; x++) {
            int cnt = 0;

            for (int y = y0; y <= y1; y++) {
               if (fg[y * 128 + x0 + x]) {
                  cnt++;
               }
            }

            colInk[x] = cnt;
            if (cnt > maxCol) {
               maxCol = cnt;
            }
         }

         int[] cut = new int[len + 1];
         cut[0] = 0;
         cut[len] = totalW;
         int win = Math.max(2, totalW / (len * 3));
         double cutInk = 0.0;

         for (int i = 1; i < len; i++) {
            int ideal = (int)((long)i * totalW / len);
            int bestX = ideal;
            int bestInk = 2147483647;

            for (int dx = -win; dx <= win; dx++) {
               int x = ideal + dx;
               if (x > cut[i - 1] + 1 && x < totalW - 1 && colInk[x] < bestInk) {
                  bestInk = colInk[x];
                  bestX = x;
               }
            }

            cut[i] = bestX;
            cutInk += (double)colInk[bestX] / maxCol;
         }

         double penalty = len > 1 ? cutInk / (len - 1) : 0.0;
         List<BufferHelper.Inner_StreamUtil> cells = new ArrayList<>(len);

         for (int c = 0; c < len; c++) {
            int cx0 = x0 + cut[c];
            int cx1 = x0 + cut[c + 1] - 1;
            if (cx1 < cx0) {
               cx1 = cx0;
            }

            int[] cellBox = init_IArr(fg, cx0, cx1, y0, y1);
            if (cellBox == null) {
               return null;
            }

            int cw = cellBox[2] - cellBox[0] + 1;
            int ch = cellBox[3] - cellBox[1] + 1;
            boolean[] cell = new boolean[cw * ch];

            for (int y = 0; y < ch; y++) {
               for (int x = 0; x < cw; x++) {
                  cell[y * cw + x] = fg[(cellBox[1] + y) * 128 + cellBox[0] + x];
               }
            }

            cells.add(new BufferHelper.Inner_StreamUtil(cell, cw, ch));
         }

         return new BufferHelper.Inner_ConfigStore(cells, penalty);
      } else {
         return null;
      }
   }

   private static boolean init_Z(CachePool img) {
      int dark = 0;
      int brightWhite = 0;
      int brightColor = 0;

      for (int c : img.init_IArr) {
         int b = CachePool.write(c);
         if (b < 40) {
            dark++;
         } else if (b > 120) {
            if (CachePool.update(c) < 45) {
               brightWhite++;
            } else {
               brightColor++;
            }
         }
      }

      return dark > img.init_IArr.length * 55 / 100 && brightWhite >= brightColor;
   }

   private static boolean[] init_ZArr(CachePool img) {
      int[] value = new int[img.init_IArr.length];
      int[] hist = new int[256];

      for (int i = 0; i < img.init_IArr.length; i++) {
         int c = img.init_IArr[i];
         int v = Math.max(c >> 16 & 0xFF, Math.max(c >> 8 & 0xFF, c & 0xFF));
         value[i] = v;
         hist[v]++;
      }

      int threshold = Math.max(init_I_IArr_I(hist, img.init_IArr.length) + 8, 110);
      boolean[] fg = new boolean[img.init_IArr.length];

      for (int i = 0; i < fg.length; i++) {
         fg[i] = value[i] > threshold;
      }

      return fg;
   }

   private static boolean[] init_ZArr(boolean[] fg) {
      List<CachePool.Inner_DataUtil> blobs = CachePool.init_List(fg, 128, 128);
      if (blobs.isEmpty()) {
         return fg;
      }

      int maxArea = 0;

      for (CachePool.Inner_DataUtil bl : blobs) {
         maxArea = Math.max(maxArea, bl.update);
      }

      int glyphArea = (int)(maxArea * 0.3);
      int bandTop = 2147483647;
      int bandBottom = -1;

      for (CachePool.Inner_DataUtil bl : blobs) {
         if (bl.update >= glyphArea) {
            bandTop = Math.min(bandTop, bl.load);
            bandBottom = Math.max(bandBottom, bl.write);
         }
      }

      if (bandBottom < 0) {
         return fg;
      }

      boolean[] out = new boolean[fg.length];

      for (CachePool.Inner_DataUtil bl : blobs) {
         boolean glyph = bl.update >= glyphArea;
         int overlap = Math.min(bl.write, bandBottom) - Math.max(bl.load, bandTop) + 1;
         double frac = overlap <= 0 ? 0.0 : (double)overlap / bl.load();
         if (glyph || !(frac < 0.5)) {
            int bw = bl.init();

            for (int y = 0; y < bl.load(); y++) {
               for (int x = 0; x < bw; x++) {
                  if (bl.init_ZArr[y * bw + x]) {
                     out[(bl.load + y) * 128 + bl.init_I + x] = true;
                  }
               }
            }
         }
      }

      return out;
   }

   private static boolean[] init_ZArr(boolean[] fg, int w, int h, int maxThin, int minLong) {
      boolean[] out = new boolean[fg.length];

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            if (fg[y * w + x]) {
               int hr = init(fg, w, h, x, y, 1, 0) + init(fg, w, h, x, y, -1, 0) - 1;
               int vr = init(fg, w, h, x, y, 0, 1) + init(fg, w, h, x, y, 0, -1) - 1;
               int d1 = init(fg, w, h, x, y, 1, 1) + init(fg, w, h, x, y, -1, -1) - 1;
               int d2 = init(fg, w, h, x, y, 1, -1) + init(fg, w, h, x, y, -1, 1) - 1;
               int min = Math.min(Math.min(hr, vr), Math.min(d1, d2));
               out[y * w + x] = min > maxThin;
            }
         }
      }

      return out;
   }

   private static int init(boolean[] fg, int w, int h, int x, int y, int dx, int dy) {
      int n = 0;

      while (x >= 0 && y >= 0 && x < w && y < h && fg[y * w + x]) {
         n++;
         x += dx;
         y += dy;
      }

      return n;
   }

   private static int init_I_IArr_I(int[] hist, int total) {
      double sum = 0.0;

      for (int t = 0; t < 256; t++) {
         sum += (double)t * hist[t];
      }

      double sumB = 0.0;
      long wB = 0L;
      double maxVar = -1.0;
      int best = 127;

      for (int t = 0; t < 256; t++) {
         wB += hist[t];
         if (wB != 0L) {
            long wF = total - wB;
            if (wF == 0L) {
               break;
            }

            sumB += (double)t * hist[t];
            double mB = sumB / wB;
            double mF = (sum - sumB) / wF;
            double between = (double)wB * wF * (mB - mF) * (mB - mF);
            if (between > maxVar) {
               maxVar = between;
               best = t;
            }
         }
      }

      return best;
   }

   private static boolean[] load(boolean[] fg) {
      List<CachePool.Inner_DataUtil> blobs = CachePool.init_List(fg, 128, 128);
      boolean[] out = new boolean[fg.length];

      for (CachePool.Inner_DataUtil bl : blobs) {
         int bw = bl.init();
         int bh = bl.load();
         double fill = (double)bl.update / (bw * bh);
         boolean junk = bl.update < 40 || bh < 12 || fill < 0.18 && Math.max(bw, bh) > 24;
         if (!junk) {
            for (int y = 0; y < bh; y++) {
               for (int x = 0; x < bw; x++) {
                  if (bl.init_ZArr[y * bw + x]) {
                     out[(bl.load + y) * 128 + bl.init_I + x] = true;
                  }
               }
            }
         }
      }

      return out;
   }

   private static int[] init_IArr(boolean[] fg) {
      int minX = 2147483647;
      int minY = 2147483647;
      int maxX = -1;
      int maxY = -1;

      for (int y = 0; y < 128; y++) {
         for (int x = 0; x < 128; x++) {
            if (fg[y * 128 + x]) {
               if (x < minX) {
                  minX = x;
               }

               if (x > maxX) {
                  maxX = x;
               }

               if (y < minY) {
                  minY = y;
               }

               if (y > maxY) {
                  maxY = y;
               }
            }
         }
      }

      return maxX < 0 ? null : new int[]{minX, minY, maxX, maxY};
   }

   private static int[] init_IArr(boolean[] fg, int x0, int x1, int y0, int y1) {
      int minX = 2147483647;
      int minY = 2147483647;
      int maxX = -1;
      int maxY = -1;

      for (int y = y0; y <= y1; y++) {
         for (int x = x0; x <= x1; x++) {
            if (fg[y * 128 + x]) {
               if (x < minX) {
                  minX = x;
               }

               if (x > maxX) {
                  maxX = x;
               }

               if (y < minY) {
                  minY = y;
               }

               if (y > maxY) {
                  maxY = y;
               }
            }
         }
      }

      return maxX < 0 ? null : new int[]{minX, minY, maxX, maxY};
   }

   private static StreamUtil init() {
      StreamUtil m = init_StreamUtil;
      if (m != null) {
         return m;
      }

      synchronized (init_Obj) {
         if (init_StreamUtil == null) {
            init_StreamUtil = new StreamUtil(new Font("Monospaced", 1, 48), "0123456789", new double[]{0.0}, false);
         }

         return init_StreamUtil;
      }
   }

   private static StreamUtil load() {
      StreamUtil m = load_StreamUtil;
      if (m != null) {
         return m;
      }

      synchronized (init_Obj) {
         if (load_StreamUtil == null) {
            Font font = init_Font_Str_F("Kingthings_Trypewriter_2.ttf", 48.0F);
            if (font == null) {
               font = new Font("Serif", 0, 48);
            }

            StreamUtil built = new StreamUtil(font, "abcdefhjkmnoprstuxyz", new double[]{-0.17, -0.08, 0.0, 0.08, 0.17}, true);
            built.init_V_ConfigStore(ConfigStore.init_ConfigStore());
            load_StreamUtil = built;
         }

         return load_StreamUtil;
      }
   }

   private static Font init_Font_Str_F(String name, float size) {
      try (InputStream is = BufferHelper.class.getResourceAsStream("/assets/autismclient/captcha/fonts/" + name)) {
         return is == null ? null : Font.createFont(0, is).deriveFont(0, size);
      } catch (Throwable ignored) {
         return null;
      }
   }

   private record Inner_BufferHelper(boolean[] init_ZArr, StreamUtil init_StreamUtil, int init_I, int load) {
   }

   public record Inner_CachePool(String init_Str, double init_D) {
      public boolean init_Z() {
         return this.init_Str != null && !this.init_Str.isEmpty();
      }
   }

   private record Inner_ConfigStore(List<BufferHelper.Inner_StreamUtil> init_List, double init_D) {
   }

   private record Inner_StreamUtil(boolean[] init_ZArr, int init_I, int load) {
   }
}
