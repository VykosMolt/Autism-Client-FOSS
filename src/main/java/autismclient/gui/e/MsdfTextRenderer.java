package autismclient.gui.e;

import autismclient.mixin.accessor.AutismGuiGraphicsExtractorAccessor;
import autismclient.util.AutismConfig;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.joml.Matrix3x2f;

public final class MsdfTextRenderer {
   private static final int init_I = 2048;
   private static final Map<MsdfTextRenderer.Inner_DataUtil, Float> init_Map = new LinkedHashMap<MsdfTextRenderer.Inner_DataUtil, Float>(256, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<MsdfTextRenderer.Inner_DataUtil, Float> eldest) {
         return this.size() > 2048;
      }
   };
   private static volatile boolean init_Z;
   public static final float init_F = 9.0F;
   private static volatile int load_I;
   private static volatile float load_F = -1.0F;

   private MsdfTextRenderer() {
   }

   public static void init_V_Z(boolean vanilla) {
      init_Z = vanilla;
   }

   public static int init_I() {
      return load() * 2 + (init_Z ? 1 : 0);
   }

   public static boolean init(MsdfFontFace font, String text) {
      if (init_Z) {
         return false;
      }

      if (text == null || text.isEmpty()) {
         return false;
      }

      if (text.indexOf(167) >= 0) {
         return false;
      }

      if (!SdfRenderPipelines.init_Z()) {
         return false;
      }

      MsdfFontAtlas msdf = font.init_StreamUtil();
      return msdf != null && msdf.init_Z_Str(text);
   }

   public static float init_F_ConfigStore(MsdfFontFace font) {
      return font.init_F() * init_F();
   }

   public static float init_F() {
      try {
         double configured = AutismConfig.getGlobal().uiFontScale;
         return configured >= 0.75 && configured <= 1.4 ? (float)configured : 1.0F;
      } catch (Throwable t) {
         return 1.0F;
      }
   }

   public static float init_F(MsdfFontFace font, String text, float size) {
      String safe = text == null ? "" : text;
      if (safe.isEmpty()) {
         return 0.0F;
      }

      MsdfFontAtlas msdf = font.init_StreamUtil();
      if (msdf == null) {
         return 0.0F;
      }

      MsdfTextRenderer.Inner_DataUtil key = new MsdfTextRenderer.Inner_DataUtil(font, safe, init_F_F(size), init_Z);
      synchronized (init_Map) {
         Float cached = init_Map.get(key);
         if (cached != null) {
            return cached;
         }
      }

      float width = msdf.init(safe, size);
      synchronized (init_Map) {
         init_Map.put(key, width);
         return width;
      }
   }

   public static int init_I(MsdfFontFace font, String text, float size) {
      return (int)Math.ceil(init_F(font, text, size) - 1.0E-4);
   }

   public static float init(MsdfFontFace font, float size) {
      MsdfFontAtlas msdf = font.init_StreamUtil();
      return (float)((msdf != null ? msdf.init_D() : 1.275) * size);
   }

   public static String init(MsdfFontFace font, String text, float size, float maxWidth) {
      String safe = text == null ? "" : text;
      if (!safe.isEmpty() && !(maxWidth <= 0.0F)) {
         if (init_F(font, safe, size) <= maxWidth) {
            return safe;
         }

         MsdfFontAtlas msdf = font.init_StreamUtil();
         return msdf != null ? msdf.load(safe, size, maxWidth) : "";
      } else {
         return "";
      }
   }

   public static String load(MsdfFontFace font, String text, float size, float maxWidth) {
      String safe = text == null ? "" : text;
      if (!safe.isEmpty() && !(maxWidth <= 0.0F)) {
         if (init_F(font, safe, size) <= maxWidth) {
            return safe;
         }

         String ellipsis = "...";
         float ellipsisWidth = init_F(font, ellipsis, size);
         if (maxWidth <= ellipsisWidth) {
            return init(font, safe, size, maxWidth);
         }

         MsdfFontAtlas msdf = font.init_StreamUtil();
         if (msdf == null) {
            return "";
         }

         String base = msdf.init_Str_Str(safe, size, maxWidth - ellipsisWidth);

         while (!base.isEmpty() && init_F(font, base + ellipsis, size) > maxWidth) {
            base = base.substring(0, base.length() - 1);
         }

         return base.isEmpty() ? ellipsis : base + ellipsis;
      } else {
         return "";
      }
   }

   public static float init(MsdfFontFace font, float vanillaTop, float size) {
      MsdfFontAtlas msdf = font.init_StreamUtil();
      if (msdf == null) {
         return vanillaTop + 7.0F - (float)(0.97 * size);
      }

      float capTop = vanillaTop + (9.0F - load(font, size)) * 0.5F;
      float baseline = capTop + (float)(msdf.write() * size);
      return baseline - (float)(msdf.load() * size);
   }

   public static float load(MsdfFontFace font, float size) {
      MsdfFontAtlas msdf = font.init_StreamUtil();
      return msdf == null ? 9.0F : (float)((msdf.write() + msdf.read()) * size);
   }

   public static float init(MsdfFontFace font, float top, float height, float size) {
      MsdfFontAtlas msdf = font.init_StreamUtil();
      if (msdf == null) {
         float spare = Math.max(0.0F, height - 9.0F);
         return top + Math.max(1.0F, (float)Math.floor((spare + 1.0F) * 0.5F) + 1.0F);
      } else {
         float caps = (float)(msdf.write() * size);
         float descender = (float)(msdf.read() * size);
         float capTop = top + (height - (caps + descender * 0.35F)) * 0.5F;
         return capTop - (9.0F - load(font, size)) * 0.5F;
      }
   }

   public static void init(GuiGraphicsExtractor graphics, MsdfFontFace font, String text, float x, float y, float size, int color, boolean shadow) {
      if (text != null && !text.isEmpty() && (color & 0xFF000000) != 0) {
         MsdfFontAtlas msdf = font.init_StreamUtil();
         if (msdf != null) {
            if (shadow) {
               float offset = Math.max(0.5F, size * 0.11F);
               init(graphics, msdf, text, x + offset, y + offset, size, init_I_I(color));
            }

            init(graphics, msdf, text, x, y, size, color);
         }
      }
   }

   private static int init_I_I(int color) {
      return (color & 16579836) >> 2 | color & 0xFF000000;
   }

   private static void init(GuiGraphicsExtractor graphics, MsdfFontAtlas msdf, String text, float x, float y, float size, int color) {
      MsdfFontAtlas.Inner_DataUtil atlas = msdf.init_StreamUtil_DataUti();
      if (atlas != null) {
         float[] quads = new float[text.length() * 8];
         int quadCount = msdf.init(text, x, y, size, quads, 0);
         if (quadCount > 0) {
            AutismGuiGraphicsExtractorAccessor accessor = (AutismGuiGraphicsExtractorAccessor)graphics;
            ScreenRectangle scissor = UiScissorStack.init_ScreenRectangle();
            GuiRenderState renderState = accessor.autism$guiRenderState();
            TexturedQuadRenderState element = TexturedQuadRenderState.init(
               SdfRenderPipelines.init_RenderPipeline(),
               TextureSetup.singleTexture(atlas.getTextureView(), atlas.getSampler()),
               new Matrix3x2f(graphics.pose()),
               quads,
               quadCount,
               color,
               scissor
            );
            if (element != null) {
               renderState.addGuiElement(element);
            }
         }
      }
   }

   public static void init_V() {
      synchronized (init_Map) {
         init_Map.clear();
      }

      MsdfFontFace.init_V();
      SdfShapeRenderer.init_V();
      SdfIconRenderer.init_V();
      load_I++;
   }

   public static int load() {
      float scale = init_F();
      if (scale != load_F) {
         load_F = scale;
         load_I++;
         synchronized (init_Map) {
            init_Map.clear();
         }
      }

      return load_I;
   }

   private static float init_F_F(float size) {
      return Math.round(size * 64.0F) / 64.0F;
   }

   private record Inner_DataUtil(MsdfFontFace init_ConfigStore, String init_Str, float init_F, boolean init_Z) {
   }
}
