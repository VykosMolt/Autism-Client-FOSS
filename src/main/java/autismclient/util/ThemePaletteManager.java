package autismclient.util;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public final class ThemePaletteManager {
   public static final int[] init_IArr = new int[]{-50373, -5035221, -791321, -50373, -5093304, -13248397, -1938838, -7397596, -50373, -39836};
   private static final float init_F = 0.1F;
   private static final float load = 0.092F;
   private static final float read = 0.027777778F;
   private static final float write = 0.5416667F;
   private static final float update = 0.875F;
   private static volatile ThemePaletteManager.CompiledPalette init_ct_StreamUtil;
   private static final Map<Long, Integer> init_Map = new HashMap<>(512);
   private static volatile int init_I;

   private ThemePaletteManager() {
   }

   private static float init_F_F_F(float a, float b) {
      float d = Math.abs(a - b);
      return Math.min(d, 1.0F - d);
   }

   private static float init_F_F(float a, float b, float x) {
      float t = load((x - a) / (b - a));
      return t * t * (3.0F - 2.0F * t);
   }

   private static float load(float a, float b) {
      return (b - a + 1.5F) % 1.0F - 0.5F;
   }

   private static float init_F_F(float h) {
      return h - (float)Math.floor(h);
   }

   public static ThemePaletteManager.CompiledPalette init_ct_StreamUtil() {
      ThemePaletteManager.CompiledPalette s = init_ct_StreamUtil;
      if (s == null) {
         s = new ThemePaletteManager.CompiledPalette(AutismLiteVariant.enabled() ? new AutismConfig.ThemeColors() : AutismConfig.getGlobal().themeColors);
         init_ct_StreamUtil = s;
      }

      return s;
   }

   public static boolean init_Z() {
      return init_ct_StreamUtil().load_Z;
   }

   public static int init_I() {
      return init_I;
   }

   public static void init_V() {
      if (!AutismLiteVariant.enabled()) {
         init_ct_StreamUtil = new ThemePaletteManager.CompiledPalette(AutismConfig.getGlobal().themeColors);
         init_I++;
         synchronized (init_Map) {
            init_Map.clear();
         }

         try {
            autismclient.gui.f.UiHelper.init();
         } catch (Throwable var3) {
         }

         try {
            ThemeTextureRecolorer.load();
         } catch (Throwable var2) {
         }
      }
   }

   public static int init_I_I(int argb) {
      return init_I_I_ct_DataUtil(argb, ThemePaletteManager.ColorRole.ACCENT);
   }

   public static int init_I_I_ct_DataUtil(int argb, ThemePaletteManager.ColorRole ch) {
      ThemePaletteManager.CompiledPalette st = init_ct_StreamUtil();
      if (st.load_Z && st.init_ZArr[ch.ordinal()]) {
         long key = (long)ch.ordinal() << 56 | argb & 4294967295L;
         synchronized (init_Map) {
            Integer cached = init_Map.get(key);
            if (cached != null) {
               return cached;
            }

            int out = init_I_I_2(argb, ch, st);
            init_Map.put(key, out);
            return out;
         }
      } else {
         return argb;
      }
   }

   public static int init_I_I_2(int argb, ThemePaletteManager.ColorRole ch, ThemePaletteManager.CompiledPalette st) {
      int ci = ch.ordinal();
      if (st != null && st.init_ZArr[ci]) {
         int a = Math.round((argb >>> 24 & 0xFF) * st.write[ci]);
         int r = argb >>> 16 & 0xFF;
         int g = argb >>> 8 & 0xFF;
         int b = argb & 0xFF;
         float[] hsb = Color.RGBtoHSB(r, g, b, null);
         float s = hsb[1];
         float v = hsb[2];
         float th = st.init_FArr[ci];
         float ts = st.load_FArr[ci];
         float tv = st.read[ci];
         if (ch == ThemePaletteManager.ColorRole.TEXT) {
            float outS = Math.min(0.4F, s + ts * 0.3F);
            return a << 24 | Color.HSBtoRGB(th, outS, v) & 16777215;
         }

         if (s < 0.1F) {
            return a << 24 | argb & 16777215;
         }

         float outS = s * ts;
         float outV = load(v + (tv - v) * (1.0F - ts) * 0.7F);
         return a << 24 | Color.HSBtoRGB(th, outS, outV) & 16777215;
      } else {
         return argb;
      }
   }

   private static float load(float v) {
      return Math.max(0.0F, Math.min(1.0F, v));
   }

   public static int load(int argb, ThemePaletteManager.ColorRole ch, ThemePaletteManager.CompiledPalette st) {
      int ci = ch.ordinal();
      if (st != null && st.init_ZArr[ci]) {
         int a = argb >>> 24 & 0xFF;
         if (a == 0) {
            return argb;
         }

         int r = argb >>> 16 & 0xFF;
         int g = argb >>> 8 & 0xFF;
         int b = argb & 0xFF;
         float[] hsb = Color.RGBtoHSB(r, g, b, null);
         float h = hsb[0];
         float s = hsb[1];
         float v = hsb[2];
         float w = read(s) * init_F_F_ct_DataUtil(h, ch);
         if (w <= 0.0F) {
            return argb;
         }

         float outS = s * st.load_FArr[ci];
         float hOut = init_F_F(h + load(h, st.init_FArr[ci]) * w);
         float sOut = s + (outS - s) * w;
         return a << 24 | Color.HSBtoRGB(hOut, sOut, v) & 16777215;
      } else {
         return argb;
      }
   }

   private static float read(float s) {
      return init_F_F(0.12F, 0.24F, s);
   }

   private static float init_F_F_ct_DataUtil(float h, ThemePaletteManager.ColorRole ch) {
      return ch == ThemePaletteManager.ColorRole.BACKDROP ? write(h) : 1.0F - init_F_F(0.092F, 0.11977778F, init_F_F_F(h, 0.0F));
   }

   private static float write(float h) {
      return init_F_F(0.5138889F, 0.5416667F, h) * (1.0F - init_F_F(0.875F, 0.9027778F, h));
   }

   public static int init_I_I(int argb, float targetHue, float targetSat) {
      int a = argb >>> 24 & 0xFF;
      if (a == 0) {
         return argb;
      }

      int r = argb >>> 16 & 0xFF;
      int g = argb >>> 8 & 0xFF;
      int b = argb & 0xFF;
      float[] hsb = Color.RGBtoHSB(r, g, b, null);
      float h = hsb[0];
      float s = hsb[1];
      float v = hsb[2];
      float w = read(s) * write(h);
      if (w <= 0.0F) {
         return argb;
      }

      float outS = load(targetSat * (0.65F + 0.35F * s));
      float hOut = init_F_F(h + load(h, targetHue) * w);
      float sOut = s + (outS - s) * w;
      return a << 24 | Color.HSBtoRGB(hOut, sOut, v) & 16777215;
   }

   public enum ColorRole {
      ACCENT,
      OUTLINE,
      TEXT,
      TOGGLE,
      BACKDROP,
      SUCCESS,
      DANGER,
      BUTTON,
      HEADER,
      HOVER;
   }

   public static final class CompiledPalette {
      public final boolean init_Z;
      public final boolean load_Z;
      final boolean[] init_ZArr = new boolean[ThemePaletteManager.ColorRole.values().length];
      final float[] init_FArr = new float[ThemePaletteManager.ColorRole.values().length];
      final float[] load_FArr = new float[ThemePaletteManager.ColorRole.values().length];
      final float[] read = new float[ThemePaletteManager.ColorRole.values().length];
      final float[] write = new float[ThemePaletteManager.ColorRole.values().length];

      private CompiledPalette(AutismConfig.ThemeColors cfg) {
         this.init_Z = cfg.advanced;
         int[] targets = new int[ThemePaletteManager.ColorRole.values().length];
         if (cfg.advanced) {
            targets[ThemePaletteManager.ColorRole.ACCENT.ordinal()] = cfg.accent;
            targets[ThemePaletteManager.ColorRole.OUTLINE.ordinal()] = cfg.outline;
            targets[ThemePaletteManager.ColorRole.TEXT.ordinal()] = cfg.text;
            targets[ThemePaletteManager.ColorRole.TOGGLE.ordinal()] = cfg.toggle;
            targets[ThemePaletteManager.ColorRole.BACKDROP.ordinal()] = cfg.backdrop;
            targets[ThemePaletteManager.ColorRole.SUCCESS.ordinal()] = cfg.success;
            targets[ThemePaletteManager.ColorRole.DANGER.ordinal()] = cfg.danger;
            targets[ThemePaletteManager.ColorRole.BUTTON.ordinal()] = cfg.button;
            targets[ThemePaletteManager.ColorRole.HEADER.ordinal()] = cfg.header;
            targets[ThemePaletteManager.ColorRole.HOVER.ordinal()] = cfg.hover;
         } else {
            targets[ThemePaletteManager.ColorRole.ACCENT.ordinal()] = cfg.master;
            targets[ThemePaletteManager.ColorRole.OUTLINE.ordinal()] = cfg.master;
            targets[ThemePaletteManager.ColorRole.TOGGLE.ordinal()] = cfg.master;
            targets[ThemePaletteManager.ColorRole.BACKDROP.ordinal()] = cfg.master;
            targets[ThemePaletteManager.ColorRole.BUTTON.ordinal()] = cfg.master;
            targets[ThemePaletteManager.ColorRole.DANGER.ordinal()] = cfg.master;
            targets[ThemePaletteManager.ColorRole.HEADER.ordinal()] = cfg.master;
            targets[ThemePaletteManager.ColorRole.HOVER.ordinal()] = cfg.master;
            targets[ThemePaletteManager.ColorRole.TEXT.ordinal()] = ThemePaletteManager.init_IArr[ThemePaletteManager.ColorRole.TEXT.ordinal()];
            targets[ThemePaletteManager.ColorRole.SUCCESS.ordinal()] = ThemePaletteManager.init_IArr[ThemePaletteManager.ColorRole.SUCCESS.ordinal()];
         }

         int defaultMaster = ThemePaletteManager.init_IArr[ThemePaletteManager.ColorRole.ACCENT.ordinal()];
         boolean any = false;

         for (int i = 0; i < targets.length; i++) {
            float[] hsb = Color.RGBtoHSB(targets[i] >> 16 & 0xFF, targets[i] >> 8 & 0xFF, targets[i] & 0xFF, null);
            this.init_FArr[i] = hsb[0];
            this.load_FArr[i] = hsb[1];
            this.read[i] = hsb[2];
            this.write[i] = (targets[i] >>> 24 & 0xFF) / 255.0F;
            int baseline = !cfg.advanced && i != ThemePaletteManager.ColorRole.TEXT.ordinal() && i != ThemePaletteManager.ColorRole.SUCCESS.ordinal() ? defaultMaster : ThemePaletteManager.init_IArr[i];
            boolean rgbDiff = (targets[i] & 16777215) != (baseline & 16777215);
            boolean alphaDiff = (targets[i] >>> 24 & 0xFF) != (baseline >>> 24 & 0xFF);
            this.init_ZArr[i] = rgbDiff || alphaDiff;
            any |= this.init_ZArr[i];
         }

         this.load_Z = any;
      }

      public static ThemePaletteManager.CompiledPalette init_ct_StreamUtil_AutismConfig_Theme(AutismConfig.ThemeColors cfg) {
         return new ThemePaletteManager.CompiledPalette(cfg);
      }

      public boolean init_Z_ct_DataUtil(ThemePaletteManager.ColorRole ch) {
         return this.init_ZArr[ch.ordinal()];
      }

      public float init_F_ct_DataUtil(ThemePaletteManager.ColorRole ch) {
         return this.init_FArr[ch.ordinal()];
      }

      public float load_F(ThemePaletteManager.ColorRole ch) {
         return this.load_FArr[ch.ordinal()];
      }

      public int init_I_ct_DataUtil(ThemePaletteManager.ColorRole ch) {
         int i = ch.ordinal();
         int rgb = Color.HSBtoRGB(this.init_FArr[i], this.load_FArr[i], this.read[i]) & 16777215;
         return Math.round(this.write[i] * 255.0F) << 24 | rgb;
      }

      public int load_I(ThemePaletteManager.ColorRole ch) {
         int i = ch.ordinal();
         if (!this.init_ZArr[i]) {
            return 0;
         }

         int sig = Float.floatToIntBits(this.init_FArr[i]);
         sig = sig * 31 + Float.floatToIntBits(this.load_FArr[i]);
         sig = sig * 31 + Float.floatToIntBits(this.read[i]);
         sig = sig * 31 + Float.floatToIntBits(this.write[i]);
         return sig == 0 ? 1 : sig;
      }
   }
}
