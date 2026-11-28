package autismclient.gui.sdf;

import autismclient.mixin.accessor.AutismGuiGraphicsExtractorAccessor;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.NativeImage.Format;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;
import org.lwjgl.system.MemoryUtil;

public final class SdfIconRenderer {
   private static final SdfIconRenderer.Glyph[] GLYPHS = new SdfIconRenderer.Glyph[]{
      new SdfIconRenderer.Glyph("x", new float[]{4.0F, 4.0F, 12.0F, 12.0F, 12.0F, 4.0F, 4.0F, 12.0F}),
      new SdfIconRenderer.Glyph("plus", new float[]{8.0F, 3.0F, 8.0F, 13.0F, 3.0F, 8.0F, 13.0F, 8.0F}),
      new SdfIconRenderer.Glyph("minus", new float[]{3.0F, 8.0F, 13.0F, 8.0F}),
      new SdfIconRenderer.Glyph("check", new float[]{3.0F, 8.0F, 6.5F, 11.5F, 6.5F, 11.5F, 13.0F, 4.5F}),
      new SdfIconRenderer.Glyph("chevron_down", new float[]{3.5F, 5.5F, 8.0F, 10.0F, 8.0F, 10.0F, 12.5F, 5.5F}),
      new SdfIconRenderer.Glyph("chevron_up", new float[]{3.5F, 10.0F, 8.0F, 5.5F, 8.0F, 5.5F, 12.5F, 10.0F}),
      new SdfIconRenderer.Glyph("chevron_right", new float[]{5.5F, 3.5F, 10.0F, 8.0F, 10.0F, 8.0F, 5.5F, 12.5F}),
      new SdfIconRenderer.Glyph("play", new float[]{4.5F, 3.0F, 12.0F, 8.0F, 12.0F, 8.0F, 4.5F, 13.0F, 4.5F, 13.0F, 4.5F, 3.0F}),
      new SdfIconRenderer.Glyph("stop", new float[]{4.0F, 4.0F, 12.0F, 4.0F, 12.0F, 4.0F, 12.0F, 12.0F, 12.0F, 12.0F, 4.0F, 12.0F, 4.0F, 12.0F, 4.0F, 4.0F}),
      new SdfIconRenderer.Glyph(
         "edit",
         new float[]{
            3.0F,
            10.0F,
            10.5F,
            2.5F,
            10.5F,
            2.5F,
            13.5F,
            5.5F,
            13.5F,
            5.5F,
            6.0F,
            13.0F,
            6.0F,
            13.0F,
            3.0F,
            13.0F,
            3.0F,
            13.0F,
            3.0F,
            10.0F,
            9.0F,
            4.0F,
            12.0F,
            7.0F
         }
      ),
      new SdfIconRenderer.Glyph(
         "duplicate",
         new float[]{
            6.0F,
            6.0F,
            13.0F,
            6.0F,
            13.0F,
            6.0F,
            13.0F,
            13.0F,
            13.0F,
            13.0F,
            6.0F,
            13.0F,
            6.0F,
            13.0F,
            6.0F,
            6.0F,
            3.0F,
            10.0F,
            3.0F,
            3.0F,
            3.0F,
            3.0F,
            10.0F,
            3.0F
         }
      ),
      new SdfIconRenderer.Glyph(
         "macros",
         new float[]{
            5.0F,
            3.0F,
            13.0F,
            3.0F,
            13.0F,
            3.0F,
            13.0F,
            13.0F,
            13.0F,
            13.0F,
            5.0F,
            13.0F,
            5.0F,
            13.0F,
            5.0F,
            3.0F,
            3.0F,
            6.0F,
            7.0F,
            6.0F,
            3.0F,
            10.0F,
            7.0F,
            10.0F,
            9.0F,
            6.0F,
            11.0F,
            8.0F,
            11.0F,
            8.0F,
            9.0F,
            10.0F
         }
      ),
      new SdfIconRenderer.Glyph(
         "fabricator",
         new float[]{
            3.0F,
            3.0F,
            13.0F,
            3.0F,
            13.0F,
            3.0F,
            13.0F,
            13.0F,
            13.0F,
            13.0F,
            3.0F,
            13.0F,
            3.0F,
            13.0F,
            3.0F,
            3.0F,
            3.0F,
            8.0F,
            13.0F,
            8.0F,
            8.0F,
            3.0F,
            8.0F,
            13.0F
         }
      ),
      new SdfIconRenderer.Glyph(
         "lansync",
         new float[]{
            2.5F,
            3.0F,
            13.5F,
            3.0F,
            13.5F,
            3.0F,
            13.5F,
            9.0F,
            13.5F,
            9.0F,
            2.5F,
            9.0F,
            2.5F,
            9.0F,
            2.5F,
            3.0F,
            8.0F,
            9.0F,
            8.0F,
            13.0F,
            4.0F,
            13.0F,
            12.0F,
            13.0F
         }
      ),
      new SdfIconRenderer.Glyph(
         "packetqeditor",
         new float[]{3.0F, 4.0F, 13.0F, 4.0F, 3.0F, 8.0F, 10.0F, 8.0F, 3.0F, 12.0F, 8.0F, 12.0F, 11.0F, 10.0F, 13.5F, 12.0F, 13.5F, 12.0F, 11.0F, 14.0F}
      ),
      new SdfIconRenderer.Glyph(
         "packetlogger",
         new float[]{
            3.0F,
            3.0F,
            13.0F,
            3.0F,
            13.0F,
            3.0F,
            13.0F,
            13.0F,
            13.0F,
            13.0F,
            3.0F,
            13.0F,
            3.0F,
            13.0F,
            3.0F,
            3.0F,
            5.5F,
            6.0F,
            10.5F,
            6.0F,
            5.5F,
            9.0F,
            10.5F,
            9.0F
         }
      ),
      new SdfIconRenderer.Glyph(
         "filter",
         new float[]{
            2.5F,
            3.5F,
            13.5F,
            3.5F,
            13.5F,
            3.5F,
            9.5F,
            8.0F,
            9.5F,
            8.0F,
            9.5F,
            12.0F,
            9.5F,
            12.0F,
            6.5F,
            13.0F,
            6.5F,
            13.0F,
            6.5F,
            8.0F,
            6.5F,
            8.0F,
            2.5F,
            3.5F
         }
      ),
      new SdfIconRenderer.Glyph(
         "serverinfo",
         new float[]{
            3.0F,
            3.0F,
            13.0F,
            3.0F,
            13.0F,
            3.0F,
            13.0F,
            7.0F,
            13.0F,
            7.0F,
            3.0F,
            7.0F,
            3.0F,
            7.0F,
            3.0F,
            3.0F,
            3.0F,
            9.0F,
            13.0F,
            9.0F,
            13.0F,
            9.0F,
            13.0F,
            13.0F,
            13.0F,
            13.0F,
            3.0F,
            13.0F,
            3.0F,
            13.0F,
            3.0F,
            9.0F,
            5.0F,
            5.0F,
            5.1F,
            5.0F,
            5.0F,
            11.0F,
            5.1F,
            11.0F
         }
      ),
      new SdfIconRenderer.Glyph(
         "keybinds",
         new float[]{
            3.0F,
            4.0F,
            13.0F,
            4.0F,
            3.0F,
            8.0F,
            13.0F,
            8.0F,
            3.0F,
            12.0F,
            13.0F,
            12.0F,
            6.0F,
            2.5F,
            6.0F,
            5.5F,
            10.0F,
            6.5F,
            10.0F,
            9.5F,
            6.0F,
            10.5F,
            6.0F,
            13.5F
         }
      ),
      new SdfIconRenderer.Glyph(
         "profiles",
         new float[]{3.0F, 13.0F, 3.0F, 11.0F, 3.0F, 11.0F, 5.0F, 9.5F, 5.0F, 9.5F, 11.0F, 9.5F, 11.0F, 9.5F, 13.0F, 11.0F, 13.0F, 11.0F, 13.0F, 13.0F}
      ),
      new SdfIconRenderer.Glyph(
         "mainmenucategory",
         new float[]{
            3.0F,
            3.0F,
            7.0F,
            3.0F,
            7.0F,
            3.0F,
            7.0F,
            7.0F,
            7.0F,
            7.0F,
            3.0F,
            7.0F,
            3.0F,
            7.0F,
            3.0F,
            3.0F,
            10.0F,
            3.0F,
            13.0F,
            3.0F,
            13.0F,
            3.0F,
            13.0F,
            7.0F,
            13.0F,
            7.0F,
            10.0F,
            7.0F,
            10.0F,
            7.0F,
            10.0F,
            3.0F,
            3.0F,
            10.0F,
            7.0F,
            10.0F,
            7.0F,
            10.0F,
            7.0F,
            13.0F,
            7.0F,
            13.0F,
            3.0F,
            13.0F,
            3.0F,
            13.0F,
            3.0F,
            10.0F,
            10.0F,
            10.0F,
            13.0F,
            10.0F,
            13.0F,
            10.0F,
            13.0F,
            13.0F,
            13.0F,
            13.0F,
            10.0F,
            13.0F,
            10.0F,
            13.0F,
            10.0F,
            10.0F
         }
      ),
      new SdfIconRenderer.Glyph(
         "packetcategory",
         new float[]{
            3.0F,
            5.0F,
            8.0F,
            2.5F,
            8.0F,
            2.5F,
            13.0F,
            5.0F,
            13.0F,
            5.0F,
            13.0F,
            11.0F,
            13.0F,
            11.0F,
            8.0F,
            13.5F,
            8.0F,
            13.5F,
            3.0F,
            11.0F,
            3.0F,
            11.0F,
            3.0F,
            5.0F,
            3.0F,
            5.0F,
            8.0F,
            8.0F,
            8.0F,
            8.0F,
            13.0F,
            5.0F,
            8.0F,
            8.0F,
            8.0F,
            13.5F
         }
      ),
      new SdfIconRenderer.Glyph(
         "screencategory",
         new float[]{2.5F, 3.0F, 13.5F, 3.0F, 13.5F, 3.0F, 13.5F, 11.0F, 13.5F, 11.0F, 2.5F, 11.0F, 2.5F, 11.0F, 2.5F, 3.0F, 6.0F, 13.0F, 10.0F, 13.0F}
      ),
      new SdfIconRenderer.Glyph(
         "chatcategory",
         new float[]{3.0F, 3.0F, 13.0F, 3.0F, 13.0F, 3.0F, 13.0F, 11.0F, 13.0F, 11.0F, 7.0F, 11.0F, 7.0F, 11.0F, 3.0F, 14.0F, 3.0F, 14.0F, 3.0F, 3.0F}
      ),
      new SdfIconRenderer.Glyph("pause", new float[]{5.5F, 3.5F, 5.5F, 12.5F, 10.5F, 3.5F, 10.5F, 12.5F}),
      new SdfIconRenderer.Glyph(
         "sessions",
         new float[]{
            3.0F,
            4.0F,
            3.1F,
            4.0F,
            6.0F,
            4.0F,
            13.0F,
            4.0F,
            3.0F,
            8.0F,
            3.1F,
            8.0F,
            6.0F,
            8.0F,
            13.0F,
            8.0F,
            3.0F,
            12.0F,
            3.1F,
            12.0F,
            6.0F,
            12.0F,
            13.0F,
            12.0F
         }
      ),
      new SdfIconRenderer.Glyph(
         "settings", new float[]{3.0F, 4.5F, 13.0F, 4.5F, 3.0F, 11.5F, 13.0F, 11.5F, 10.0F, 2.5F, 10.0F, 6.5F, 6.0F, 9.5F, 6.0F, 13.5F}
      ),
      new SdfIconRenderer.Glyph(
         "queue",
         new float[]{2.5F, 4.0F, 12.5F, 4.0F, 2.5F, 8.0F, 8.5F, 8.0F, 2.5F, 12.0F, 7.0F, 12.0F, 11.5F, 8.75F, 11.5F, 14.25F, 8.75F, 11.5F, 14.25F, 11.5F}
      ),
      new SdfIconRenderer.Glyph(
         "pin",
         new float[]{
            5.0F,
            3.0F,
            11.0F,
            3.0F,
            5.0F,
            3.0F,
            5.0F,
            7.5F,
            11.0F,
            3.0F,
            11.0F,
            7.5F,
            5.0F,
            7.5F,
            3.0F,
            9.5F,
            11.0F,
            7.5F,
            13.0F,
            9.5F,
            3.0F,
            9.5F,
            13.0F,
            9.5F,
            8.0F,
            9.5F,
            8.0F,
            14.0F
         }
      ),
      new SdfIconRenderer.Glyph(
         "refresh",
         new float[]{
            10.5F,
            12.33F,
            8.0F,
            13.0F,
            8.0F,
            13.0F,
            5.5F,
            12.33F,
            5.5F,
            12.33F,
            3.67F,
            10.5F,
            3.67F,
            10.5F,
            3.0F,
            8.0F,
            3.0F,
            8.0F,
            3.67F,
            5.5F,
            3.67F,
            5.5F,
            5.5F,
            3.67F,
            5.5F,
            3.67F,
            8.0F,
            3.0F,
            8.0F,
            3.0F,
            10.5F,
            3.67F,
            10.5F,
            3.67F,
            12.33F,
            5.5F,
            10.5F,
            12.33F,
            7.3F,
            11.73F,
            10.5F,
            12.33F,
            10.9F,
            8.93F
         }
      ),
      new SdfIconRenderer.Glyph(
         "cat_combat", new float[]{8.0F, 2.0F, 8.0F, 8.5F, 4.5F, 8.5F, 11.5F, 8.5F, 8.0F, 8.5F, 8.0F, 12.0F, 8.0F, 13.0F, 8.1F, 13.0F}
      ),
      new SdfIconRenderer.Glyph(
         "cat_movement",
         new float[]{
            8.0F,
            4.5F,
            8.0F,
            11.5F,
            4.5F,
            8.0F,
            11.5F,
            8.0F,
            6.5F,
            4.0F,
            8.0F,
            2.0F,
            8.0F,
            2.0F,
            9.5F,
            4.0F,
            6.5F,
            12.0F,
            8.0F,
            14.0F,
            8.0F,
            14.0F,
            9.5F,
            12.0F,
            4.0F,
            6.5F,
            2.0F,
            8.0F,
            2.0F,
            8.0F,
            4.0F,
            9.5F,
            12.0F,
            6.5F,
            14.0F,
            8.0F,
            14.0F,
            8.0F,
            12.0F,
            9.5F
         }
      ),
      new SdfIconRenderer.Glyph(
         "cat_player",
         new float[]{4.0F, 13.0F, 4.5F, 10.5F, 4.5F, 10.5F, 6.5F, 9.0F, 6.5F, 9.0F, 9.5F, 9.0F, 9.5F, 9.0F, 11.5F, 10.5F, 11.5F, 10.5F, 12.0F, 13.0F}
      ),
      new SdfIconRenderer.Glyph("cat_misc", new float[]{4.0F, 8.0F, 4.1F, 8.0F, 8.0F, 8.0F, 8.1F, 8.0F, 12.0F, 8.0F, 12.1F, 8.0F}),
      new SdfIconRenderer.Glyph(
         "cat_render", new float[]{3.0F, 8.0F, 8.0F, 4.5F, 8.0F, 4.5F, 13.0F, 8.0F, 3.0F, 8.0F, 8.0F, 11.5F, 8.0F, 11.5F, 13.0F, 8.0F}
      ),
      new SdfIconRenderer.Glyph(
         "cat_addon", new float[]{5.5F, 5.5F, 5.6F, 5.5F, 10.5F, 5.5F, 10.6F, 5.5F, 5.5F, 10.5F, 5.6F, 10.5F, 10.5F, 10.5F, 10.6F, 10.5F}
      ),
      new SdfIconRenderer.Glyph("dot", new float[0])
   };
   private static final Map<String, Integer> TILE_INDEX = new HashMap<>(GLYPHS.length * 2);
   private static final int init_I = 64;
   private static final Identifier TEXTURE_ID;
   private static volatile SdfIconRenderer.AtlasTexture atlas;
   private static volatile boolean buildFailed;
   private static volatile NativeImage prewarmedImage;

   private SdfIconRenderer() {
   }

   public static boolean init(GuiGraphicsExtractor graphics, Identifier icon, int x, int y, int w, int h, int color) {
      if (icon != null && "autismclient".equals(icon.getNamespace())) {
         String path = icon.getPath();
         if (!path.startsWith("textures/gui/icons/") && !path.startsWith("textures/gui/vanillaui/icons/")) {
            return false;
         }

         String name = path.substring(path.lastIndexOf(47) + 1);
         if (name.endsWith(".png")) {
            name = name.substring(0, name.length() - 4);
         }

         Integer tileObj = TILE_INDEX.get(name);
         int tile = tileObj != null ? tileObj : -1;
         if (tile < 0 || !SdfRenderPipelines.isAvailable()) {
            return false;
         }

         if (w > 0 && h > 0 && color >>> 24 != 0) {
            SdfIconRenderer.AtlasTexture atlas = ensureAtlas();
            if (atlas == null) {
               return false;
            }

            float size = Math.min(w, h);
            float left = x + (w - size) * 0.5F;
            float top = y + (h - size) * 0.5F;
            float u = (float)tile / GLYPHS.length;
            float u1 = (tile + 1.0F) / GLYPHS.length;
            TexturedQuadRenderState element = TexturedQuadRenderState.create(
               SdfRenderPipelines.pipeline(),
               TextureSetup.singleTexture(atlas.getTextureView(), atlas.getSampler()),
               new Matrix3x2f(graphics.pose()),
               new float[]{left, top, left + size, top + size, u, 0.0F, u1, 1.0F},
               1,
               color,
               UiScissorStack.peek()
            );
            if (element != null) {
               ((AutismGuiGraphicsExtractorAccessor)graphics).autism$guiRenderState().addGuiElement(element);
            }

            return true;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public static void prewarm() {
      if (atlas == null && !buildFailed && prewarmedImage == null) {
         try {
            NativeImage built = renderAtlasImage();
            synchronized (SdfIconRenderer.class) {
               if (atlas == null && !buildFailed && prewarmedImage == null) {
                  prewarmedImage = built;
               } else {
                  built.close();
               }
            }
         } catch (Throwable var4) {
         }
      }
   }

   public static void releaseAll() {
      SdfIconRenderer.AtlasTexture toRelease;
      synchronized (SdfIconRenderer.class) {
         toRelease = atlas;
         atlas = null;
         buildFailed = false;
         if (prewarmedImage != null) {
            prewarmedImage.close();
            prewarmedImage = null;
         }
      }

      if (toRelease != null) {
         Minecraft.getInstance().getTextureManager().release(TEXTURE_ID);
      }
   }

   private static SdfIconRenderer.AtlasTexture ensureAtlas() {
      SdfIconRenderer.AtlasTexture current = atlas;
      if (current == null && !buildFailed) {
         synchronized (SdfIconRenderer.class) {
            current = atlas;
            if (current == null && !buildFailed) {
               NativeImage pixels = null;
               boolean ownedByAtlas = false;
               try {
                  pixels = prewarmedImage;
                  prewarmedImage = null;
                  if (pixels == null) {
                     pixels = renderAtlasImage();
                  }

                  current = new SdfIconRenderer.AtlasTexture(pixels);
                  ownedByAtlas = true;
                  Minecraft.getInstance().getTextureManager().register(TEXTURE_ID, current);
                  atlas = current;
               } catch (Throwable error) {
                  if (pixels != null && !ownedByAtlas) {
                     try {
                        pixels.close();
                     } catch (Throwable ignored) {
                     }
                  }
                  buildFailed = true;
                  return null;
               }
            }
         }
      }

      return current;
   }

   private static NativeImage renderAtlasImage() {
      int width = 64 * GLYPHS.length;
      NativeImage image = new NativeImage(Format.LUMINANCE, width, 64, false);
      long base = image.getPointer();

      for (int tile = 0; tile < GLYPHS.length; tile++) {
         SdfIconRenderer.Glyph glyph = GLYPHS[tile];
         float[] segments = glyph.segments();
         String name = glyph.name();
         boolean isDot = "dot".equals(name);
         boolean isProfiles = "profiles".equals(name);
         boolean isCatPlayer = "cat_player".equals(name);
         boolean isCatRender = "cat_render".equals(name);
         int segCount = segments.length;
         int tileOffset = tile * 64;

         for (int py = 0; py < 64; py++) {
            float y = (py + 0.5F) * 0.25F;
            long rowBase = base + (long)py * width + tileOffset;

            for (int px = 0; px < 64; px++) {
               float x = (px + 0.5F) * 0.25F;
               float minDistSq = 256.0F;

               for (int i = 0; i < segCount; i += 4) {
                  float ax = segments[i];
                  float ay = segments[i + 1];
                  float dx = segments[i + 2] - ax;
                  float dy = segments[i + 3] - ay;
                  float lenSq = dx * dx + dy * dy;
                  float t = lenSq > 0.0F ? Math.clamp(((x - ax) * dx + (y - ay) * dy) / lenSq, 0.0F, 1.0F) : 0.0F;
                  float ex = x - ax - t * dx;
                  float ey = y - ay - t * dy;
                  float dSq = ex * ex + ey * ey;
                  if (dSq < minDistSq) {
                     minDistSq = dSq;
                  }
               }

               float distance = (float)Math.sqrt(minDistSq) - 0.8F;
               if (isDot) {
                  float ex = x - 8.0F;
                  float ey = y - 8.0F;
                  distance = (float)Math.sqrt(ex * ex + ey * ey) - 4.0F;
               } else if (isProfiles || isCatPlayer) {
                  float ex = x - 8.0F;
                  float ey = y - 5.0F;
                  distance = Math.min(distance, Math.abs((float)Math.sqrt(ex * ex + ey * ey) - 2.3F) - 0.8F);
               } else if (isCatRender) {
                  float ex = x - 8.0F;
                  float ey = y - 8.0F;
                  distance = Math.min(distance, (float)Math.sqrt(ex * ex + ey * ey) - 1.6F);
               }

               int value = Math.clamp(Math.round(255.0F * (0.5F - distance * 0.25F)), 0, 255);
               MemoryUtil.memPutByte(rowBase + px, (byte)value);
            }
         }
      }

      return image;
   }

   static {
      for (int i = 0; i < GLYPHS.length; i++) {
         TILE_INDEX.put(GLYPHS[i].name(), i);
      }

      TEXTURE_ID = Identifier.fromNamespaceAndPath("autismclient", "ui/sdf_icons");
   }

   private record Glyph(String name, float[] segments) {
   }

   private static final class AtlasTexture extends AbstractTexture {
      AtlasTexture(NativeImage pixels) {
         try (pixels) {
            this.texture = RenderSystem.getDevice()
               .createTexture("autismclient vector icons", 15, GpuFormat.R8_UNORM, pixels.getWidth(), pixels.getHeight(), 1, 1);
            this.sampler = RenderSystem.getSamplerCache()
               .getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, false);
            this.textureView = RenderSystem.getDevice().createTextureView(this.texture);
            RenderSystem.getDevice().createCommandEncoder().writeToTexture(this.texture, pixels);
         }
      }
   }
}
