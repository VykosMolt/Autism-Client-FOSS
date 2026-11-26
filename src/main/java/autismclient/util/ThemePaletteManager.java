package autismclient.util;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public final class ThemePaletteManager {
   public static final int[] DEFAULT_COLORS = new int[]{-50373, -5035221, -791321, -50373, -5093304, -13248397, -1938838, -7397596, -50373, -39836};
   private static final float MIN_SATURATION_THRESHOLD = 0.1F;
   private static final float RED_HUE_HALF_WIDTH = 0.092F;
   private static final float HUE_FEATHER_WIDTH = 0.027777778F;
   private static final float BACKDROP_HUE_START = 0.5416667F;
   private static final float BACKDROP_HUE_END = 0.875F;
   private static volatile ThemePaletteManager.CompiledPalette activePalette;
   private static final Map<Long, Integer> RECOLOR_CACHE = new HashMap<>(512);
   private static volatile int generation;

   private ThemePaletteManager() {
   }

   private static float hueDistance(float a, float b) {
      float d = Math.abs(a - b);
      return Math.min(d, 1.0F - d);
   }

   private static float smoothstep(float a, float b, float x) {
      float t = clamp01((x - a) / (b - a));
      return t * t * (3.0F - 2.0F * t);
   }

   private static float shortestHueDelta(float a, float b) {
      return (b - a + 1.5F) % 1.0F - 0.5F;
   }

   private static float wrapHue(float h) {
      return h - (float)Math.floor(h);
   }

   public static ThemePaletteManager.CompiledPalette getPalette() {
      ThemePaletteManager.CompiledPalette s = activePalette;
      if (s == null) {
         s = new ThemePaletteManager.CompiledPalette(AutismLiteVariant.enabled() ? new AutismConfig.ThemeColors() : AutismConfig.getGlobal().themeColors);
         activePalette = s;
      }

      return s;
   }

   public static boolean isCustomActive() {
      return getPalette().anyModified;
   }

   public static int generation() {
      return generation;
   }

   public static void reload() {
      if (!AutismLiteVariant.enabled()) {
         activePalette = new ThemePaletteManager.CompiledPalette(AutismConfig.getGlobal().themeColors);
         generation++;
         synchronized (RECOLOR_CACHE) {
            RECOLOR_CACHE.clear();
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

   public static int recolorAccent(int argb) {
      return recolor(argb, ThemePaletteManager.ColorRole.ACCENT);
   }

   public static int recolor(int argb, ThemePaletteManager.ColorRole ch) {
      ThemePaletteManager.CompiledPalette st = getPalette();
      if (st.anyModified && st.modified[ch.ordinal()]) {
         long key = (long)ch.ordinal() << 56 | argb & 4294967295L;
         synchronized (RECOLOR_CACHE) {
            Integer cached = RECOLOR_CACHE.get(key);
            if (cached != null) {
               return cached;
            }

            int out = recolorWithPalette(argb, ch, st);
            RECOLOR_CACHE.put(key, out);
            return out;
         }
      } else {
         return argb;
      }
   }

   public static int recolorWithPalette(int argb, ThemePaletteManager.ColorRole ch, ThemePaletteManager.CompiledPalette st) {
      int ci = ch.ordinal();
      if (st != null && st.modified[ci]) {
         int a = Math.round((argb >>> 24 & 0xFF) * st.alphaScales[ci]);
         int r = argb >>> 16 & 0xFF;
         int g = argb >>> 8 & 0xFF;
         int b = argb & 0xFF;
         float[] hsb = Color.RGBtoHSB(r, g, b, null);
         float s = hsb[1];
         float v = hsb[2];
         float th = st.hues[ci];
         float ts = st.saturations[ci];
         float tv = st.brightnesses[ci];
         if (ch == ThemePaletteManager.ColorRole.TEXT) {
            float outS = Math.min(0.4F, s + ts * 0.3F);
            return a << 24 | Color.HSBtoRGB(th, outS, v) & 16777215;
         }

         if (s < 0.1F) {
            return a << 24 | argb & 16777215;
         }

         float outS = s * ts;
         float outV = clamp01(v + (tv - v) * (1.0F - ts) * 0.7F);
         return a << 24 | Color.HSBtoRGB(th, outS, outV) & 16777215;
      } else {
         return argb;
      }
   }

   private static float clamp01(float v) {
      return Math.max(0.0F, Math.min(1.0F, v));
   }

   public static int recolorTexturePixel(int argb, ThemePaletteManager.ColorRole ch, ThemePaletteManager.CompiledPalette st) {
      int ci = ch.ordinal();
      if (st != null && st.modified[ci]) {
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
         float w = saturationWeight(s) * roleHueWeight(h, ch);
         if (w <= 0.0F) {
            return argb;
         }

         float outS = s * st.saturations[ci];
         float hOut = wrapHue(h + shortestHueDelta(h, st.hues[ci]) * w);
         float sOut = s + (outS - s) * w;
         return a << 24 | Color.HSBtoRGB(hOut, sOut, v) & 16777215;
      } else {
         return argb;
      }
   }

   private static float saturationWeight(float s) {
      return smoothstep(0.12F, 0.24F, s);
   }

   private static float roleHueWeight(float h, ThemePaletteManager.ColorRole ch) {
      return ch == ThemePaletteManager.ColorRole.BACKDROP ? backdropHueWeight(h) : 1.0F - smoothstep(0.092F, 0.11977778F, hueDistance(h, 0.0F));
   }

   private static float backdropHueWeight(float h) {
      return smoothstep(0.5138889F, 0.5416667F, h) * (1.0F - smoothstep(0.875F, 0.9027778F, h));
   }

   public static int recolorBackdropPixel(int argb, float targetHue, float targetSat) {
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
      float w = saturationWeight(s) * backdropHueWeight(h);
      if (w <= 0.0F) {
         return argb;
      }

      float outS = clamp01(targetSat * (0.65F + 0.35F * s));
      float hOut = wrapHue(h + shortestHueDelta(h, targetHue) * w);
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
      public final boolean advanced;
      public final boolean anyModified;
      final boolean[] modified = new boolean[ThemePaletteManager.ColorRole.values().length];
      final float[] hues = new float[ThemePaletteManager.ColorRole.values().length];
      final float[] saturations = new float[ThemePaletteManager.ColorRole.values().length];
      final float[] brightnesses = new float[ThemePaletteManager.ColorRole.values().length];
      final float[] alphaScales = new float[ThemePaletteManager.ColorRole.values().length];

      private CompiledPalette(AutismConfig.ThemeColors cfg) {
         this.advanced = cfg.advanced;
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
            targets[ThemePaletteManager.ColorRole.TEXT.ordinal()] = ThemePaletteManager.DEFAULT_COLORS[ThemePaletteManager.ColorRole.TEXT.ordinal()];
            targets[ThemePaletteManager.ColorRole.SUCCESS.ordinal()] = ThemePaletteManager.DEFAULT_COLORS[ThemePaletteManager.ColorRole.SUCCESS.ordinal()];
         }

         int defaultMaster = ThemePaletteManager.DEFAULT_COLORS[ThemePaletteManager.ColorRole.ACCENT.ordinal()];
         boolean any = false;

         for (int i = 0; i < targets.length; i++) {
            float[] hsb = Color.RGBtoHSB(targets[i] >> 16 & 0xFF, targets[i] >> 8 & 0xFF, targets[i] & 0xFF, null);
            this.hues[i] = hsb[0];
            this.saturations[i] = hsb[1];
            this.brightnesses[i] = hsb[2];
            this.alphaScales[i] = (targets[i] >>> 24 & 0xFF) / 255.0F;
            int baseline = !cfg.advanced && i != ThemePaletteManager.ColorRole.TEXT.ordinal() && i != ThemePaletteManager.ColorRole.SUCCESS.ordinal() ? defaultMaster : ThemePaletteManager.DEFAULT_COLORS[i];
            boolean rgbDiff = (targets[i] & 16777215) != (baseline & 16777215);
            boolean alphaDiff = (targets[i] >>> 24 & 0xFF) != (baseline >>> 24 & 0xFF);
            this.modified[i] = rgbDiff || alphaDiff;
            any |= this.modified[i];
         }

         this.anyModified = any;
      }

      public static ThemePaletteManager.CompiledPalette compile(AutismConfig.ThemeColors cfg) {
         return new ThemePaletteManager.CompiledPalette(cfg);
      }

      public boolean isModified(ThemePaletteManager.ColorRole ch) {
         return this.modified[ch.ordinal()];
      }

      public float hue(ThemePaletteManager.ColorRole ch) {
         return this.hues[ch.ordinal()];
      }

      public float saturation(ThemePaletteManager.ColorRole ch) {
         return this.saturations[ch.ordinal()];
      }

      public int targetArgb(ThemePaletteManager.ColorRole ch) {
         int i = ch.ordinal();
         int rgb = Color.HSBtoRGB(this.hues[i], this.saturations[i], this.brightnesses[i]) & 16777215;
         return Math.round(this.alphaScales[i] * 255.0F) << 24 | rgb;
      }

      public int roleSignature(ThemePaletteManager.ColorRole ch) {
         int i = ch.ordinal();
         if (!this.modified[i]) {
            return 0;
         }

         int sig = Float.floatToIntBits(this.hues[i]);
         sig = sig * 31 + Float.floatToIntBits(this.saturations[i]);
         sig = sig * 31 + Float.floatToIntBits(this.brightnesses[i]);
         sig = sig * 31 + Float.floatToIntBits(this.alphaScales[i]);
         return sig == 0 ? 1 : sig;
      }
   }
}
