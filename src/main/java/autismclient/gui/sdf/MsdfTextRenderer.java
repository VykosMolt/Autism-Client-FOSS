package autismclient.gui.sdf;

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
   private static final int CACHE_LIMIT = 2048;
   private static final Map<MsdfTextRenderer.WidthKey, Float> WIDTH_CACHE = new LinkedHashMap<MsdfTextRenderer.WidthKey, Float>(256, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<MsdfTextRenderer.WidthKey, Float> eldest) {
         return this.size() > 2048;
      }
   };
   private static volatile boolean forceVanilla;
   public static final float VANILLA_LINE_HEIGHT = 9.0F;
   private static volatile int fontGeneration;
   private static volatile float lastFontScale = -1.0F;

   private MsdfTextRenderer() {
   }

   public static void setForceVanilla(boolean vanilla) {
      forceVanilla = vanilla;
   }

   public static int cacheGeneration() {
      return fontGeneration() * 2 + (forceVanilla ? 1 : 0);
   }

   public static boolean canRender(MsdfFontFace font, String text) {
      if (forceVanilla) {
         return false;
      }

      if (text == null || text.isEmpty()) {
         return false;
      }

      if (text.indexOf(167) >= 0) {
         return false;
      }

      if (!SdfRenderPipelines.isAvailable()) {
         return false;
      }

      MsdfFontAtlas msdf = font.atlas();
      return msdf != null && msdf.canRender(text);
   }

   public static float scaledFontSize(MsdfFontFace font) {
      return font.baseScale() * fontScale();
   }

   public static float fontScale() {
      try {
         double configured = AutismConfig.getGlobal().uiFontScale;
         return configured >= 0.75 && configured <= 1.4 ? (float)configured : 1.0F;
      } catch (Throwable t) {
         return 1.0F;
      }
   }

   public static float measureWidthFloat(MsdfFontFace font, String text, float size) {
      String safe = text == null ? "" : text;
      if (safe.isEmpty()) {
         return 0.0F;
      }

      MsdfFontAtlas msdf = font.atlas();
      if (msdf == null) {
         return 0.0F;
      }

      MsdfTextRenderer.WidthKey key = new MsdfTextRenderer.WidthKey(font, safe, quantizeSize(size), forceVanilla);
      synchronized (WIDTH_CACHE) {
         Float cached = WIDTH_CACHE.get(key);
         if (cached != null) {
            return cached;
         }
      }

      float width = msdf.width(safe, size);
      synchronized (WIDTH_CACHE) {
         WIDTH_CACHE.put(key, width);
         return width;
      }
   }

   public static int measureWidth(MsdfFontFace font, String text, float size) {
      return (int)Math.ceil(measureWidthFloat(font, text, size) - 1.0E-4);
   }

   public static float lineHeight(MsdfFontFace font, float size) {
      MsdfFontAtlas msdf = font.atlas();
      return (float)((msdf != null ? msdf.lineHeight() : 1.275) * size);
   }

   public static String trimToWidth(MsdfFontFace font, String text, float size, float maxWidth) {
      String safe = text == null ? "" : text;
      if (!safe.isEmpty() && !(maxWidth <= 0.0F)) {
         if (measureWidthFloat(font, safe, size) <= maxWidth) {
            return safe;
         }

         MsdfFontAtlas msdf = font.atlas();
         return msdf != null ? msdf.fit(safe, size, maxWidth) : "";
      } else {
         return "";
      }
   }

   public static String ellipsize(MsdfFontFace font, String text, float size, float maxWidth) {
      String safe = text == null ? "" : text;
      if (!safe.isEmpty() && !(maxWidth <= 0.0F)) {
         if (measureWidthFloat(font, safe, size) <= maxWidth) {
            return safe;
         }

         String ellipsis = "...";
         float ellipsisWidth = measureWidthFloat(font, ellipsis, size);
         if (maxWidth <= ellipsisWidth) {
            return trimToWidth(font, safe, size, maxWidth);
         }

         MsdfFontAtlas msdf = font.atlas();
         if (msdf == null) {
            return "";
         }

         String base = msdf.truncate(safe, size, maxWidth - ellipsisWidth);

         while (!base.isEmpty() && measureWidthFloat(font, base + ellipsis, size) > maxWidth) {
            base = base.substring(0, base.length() - 1);
         }

         return base.isEmpty() ? ellipsis : base + ellipsis;
      } else {
         return "";
      }
   }

   public static float alignToVanillaTop(MsdfFontFace font, float vanillaTop, float size) {
      MsdfFontAtlas msdf = font.atlas();
      if (msdf == null) {
         return vanillaTop + 7.0F - (float)(0.97 * size);
      }

      float capTop = vanillaTop + (9.0F - glyphBoxHeight(font, size)) * 0.5F;
      float baseline = capTop + (float)(msdf.write() * size);
      return baseline - (float)(msdf.load() * size);
   }

   public static float glyphBoxHeight(MsdfFontFace font, float size) {
      MsdfFontAtlas msdf = font.atlas();
      return msdf == null ? 9.0F : (float)((msdf.write() + msdf.read()) * size);
   }

   public static float centerVertically(MsdfFontFace font, float top, float height, float size) {
      MsdfFontAtlas msdf = font.atlas();
      if (msdf == null) {
         float spare = Math.max(0.0F, height - 9.0F);
         return top + Math.max(1.0F, (float)Math.floor((spare + 1.0F) * 0.5F) + 1.0F);
      } else {
         float caps = (float)(msdf.write() * size);
         float descender = (float)(msdf.read() * size);
         float capTop = top + (height - (caps + descender * 0.35F)) * 0.5F;
         return capTop - (9.0F - glyphBoxHeight(font, size)) * 0.5F;
      }
   }

   public static void draw(GuiGraphicsExtractor graphics, MsdfFontFace font, String text, float x, float y, float size, int color, boolean shadow) {
      if (text != null && !text.isEmpty() && (color & 0xFF000000) != 0) {
         MsdfFontAtlas msdf = font.atlas();
         if (msdf != null) {
            if (shadow) {
               float offset = Math.max(0.5F, size * 0.11F);
               drawAtlasQuads(graphics, msdf, text, x + offset, y + offset, size, shadowColor(color));
            }

            drawAtlasQuads(graphics, msdf, text, x, y, size, color);
         }
      }
   }

   private static int shadowColor(int color) {
      return (color & 16579836) >> 2 | color & 0xFF000000;
   }

   private static void drawAtlasQuads(GuiGraphicsExtractor graphics, MsdfFontAtlas msdf, String text, float x, float y, float size, int color) {
      MsdfFontAtlas.Inner_DataUtil atlas = msdf.texture();
      if (atlas != null) {
         float[] quads = new float[text.length() * 8];
         int quadCount = msdf.layoutQuads(text, x, y, size, quads, 0);
         if (quadCount > 0) {
            AutismGuiGraphicsExtractorAccessor accessor = (AutismGuiGraphicsExtractorAccessor)graphics;
            ScreenRectangle scissor = UiScissorStack.peek();
            GuiRenderState renderState = accessor.autism$guiRenderState();
            TexturedQuadRenderState element = TexturedQuadRenderState.create(
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

   public static void invalidateCaches() {
      synchronized (WIDTH_CACHE) {
         WIDTH_CACHE.clear();
      }

      MsdfFontFace.releaseAll();
      SdfShapeRenderer.reset();
      SdfIconRenderer.releaseAll();
      fontGeneration++;
   }

   public static int fontGeneration() {
      float scale = fontScale();
      if (scale != lastFontScale) {
         lastFontScale = scale;
         fontGeneration++;
         synchronized (WIDTH_CACHE) {
            WIDTH_CACHE.clear();
         }
      }

      return fontGeneration;
   }

   private static float quantizeSize(float size) {
      return Math.round(size * 64.0F) / 64.0F;
   }

   private record WidthKey(MsdfFontFace font, String text, float size, boolean forceVanilla) {
   }
}
