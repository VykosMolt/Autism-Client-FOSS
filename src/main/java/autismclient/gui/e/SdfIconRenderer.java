package autismclient.gui.e;

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
   private static final SdfIconRenderer.Inner_DataUtil[] init_CachePool_DataUtilArr = new SdfIconRenderer.Inner_DataUtil[]{
      new SdfIconRenderer.Inner_DataUtil("x", new float[]{4.0F, 4.0F, 12.0F, 12.0F, 12.0F, 4.0F, 4.0F, 12.0F}),
      new SdfIconRenderer.Inner_DataUtil("plus", new float[]{8.0F, 3.0F, 8.0F, 13.0F, 3.0F, 8.0F, 13.0F, 8.0F}),
      new SdfIconRenderer.Inner_DataUtil("minus", new float[]{3.0F, 8.0F, 13.0F, 8.0F}),
      new SdfIconRenderer.Inner_DataUtil("check", new float[]{3.0F, 8.0F, 6.5F, 11.5F, 6.5F, 11.5F, 13.0F, 4.5F}),
      new SdfIconRenderer.Inner_DataUtil("chevron_down", new float[]{3.5F, 5.5F, 8.0F, 10.0F, 8.0F, 10.0F, 12.5F, 5.5F}),
      new SdfIconRenderer.Inner_DataUtil("chevron_up", new float[]{3.5F, 10.0F, 8.0F, 5.5F, 8.0F, 5.5F, 12.5F, 10.0F}),
      new SdfIconRenderer.Inner_DataUtil("chevron_right", new float[]{5.5F, 3.5F, 10.0F, 8.0F, 10.0F, 8.0F, 5.5F, 12.5F}),
      new SdfIconRenderer.Inner_DataUtil("play", new float[]{4.5F, 3.0F, 12.0F, 8.0F, 12.0F, 8.0F, 4.5F, 13.0F, 4.5F, 13.0F, 4.5F, 3.0F}),
      new SdfIconRenderer.Inner_DataUtil("stop", new float[]{4.0F, 4.0F, 12.0F, 4.0F, 12.0F, 4.0F, 12.0F, 12.0F, 12.0F, 12.0F, 4.0F, 12.0F, 4.0F, 12.0F, 4.0F, 4.0F}),
      new SdfIconRenderer.Inner_DataUtil(
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
      new SdfIconRenderer.Inner_DataUtil(
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
      new SdfIconRenderer.Inner_DataUtil(
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
      new SdfIconRenderer.Inner_DataUtil(
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
      new SdfIconRenderer.Inner_DataUtil(
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
      new SdfIconRenderer.Inner_DataUtil(
         "packetqeditor",
         new float[]{3.0F, 4.0F, 13.0F, 4.0F, 3.0F, 8.0F, 10.0F, 8.0F, 3.0F, 12.0F, 8.0F, 12.0F, 11.0F, 10.0F, 13.5F, 12.0F, 13.5F, 12.0F, 11.0F, 14.0F}
      ),
      new SdfIconRenderer.Inner_DataUtil(
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
      new SdfIconRenderer.Inner_DataUtil(
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
      new SdfIconRenderer.Inner_DataUtil(
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
      new SdfIconRenderer.Inner_DataUtil(
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
      new SdfIconRenderer.Inner_DataUtil(
         "profiles",
         new float[]{3.0F, 13.0F, 3.0F, 11.0F, 3.0F, 11.0F, 5.0F, 9.5F, 5.0F, 9.5F, 11.0F, 9.5F, 11.0F, 9.5F, 13.0F, 11.0F, 13.0F, 11.0F, 13.0F, 13.0F}
      ),
      new SdfIconRenderer.Inner_DataUtil(
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
      new SdfIconRenderer.Inner_DataUtil(
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
      new SdfIconRenderer.Inner_DataUtil(
         "screencategory",
         new float[]{2.5F, 3.0F, 13.5F, 3.0F, 13.5F, 3.0F, 13.5F, 11.0F, 13.5F, 11.0F, 2.5F, 11.0F, 2.5F, 11.0F, 2.5F, 3.0F, 6.0F, 13.0F, 10.0F, 13.0F}
      ),
      new SdfIconRenderer.Inner_DataUtil(
         "chatcategory",
         new float[]{3.0F, 3.0F, 13.0F, 3.0F, 13.0F, 3.0F, 13.0F, 11.0F, 13.0F, 11.0F, 7.0F, 11.0F, 7.0F, 11.0F, 3.0F, 14.0F, 3.0F, 14.0F, 3.0F, 3.0F}
      ),
      new SdfIconRenderer.Inner_DataUtil("pause", new float[]{5.5F, 3.5F, 5.5F, 12.5F, 10.5F, 3.5F, 10.5F, 12.5F}),
      new SdfIconRenderer.Inner_DataUtil(
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
      new SdfIconRenderer.Inner_DataUtil(
         "settings", new float[]{3.0F, 4.5F, 13.0F, 4.5F, 3.0F, 11.5F, 13.0F, 11.5F, 10.0F, 2.5F, 10.0F, 6.5F, 6.0F, 9.5F, 6.0F, 13.5F}
      ),
      new SdfIconRenderer.Inner_DataUtil(
         "queue",
         new float[]{2.5F, 4.0F, 12.5F, 4.0F, 2.5F, 8.0F, 8.5F, 8.0F, 2.5F, 12.0F, 7.0F, 12.0F, 11.5F, 8.75F, 11.5F, 14.25F, 8.75F, 11.5F, 14.25F, 11.5F}
      ),
      new SdfIconRenderer.Inner_DataUtil(
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
      new SdfIconRenderer.Inner_DataUtil(
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
      new SdfIconRenderer.Inner_DataUtil(
         "cat_combat", new float[]{8.0F, 2.0F, 8.0F, 8.5F, 4.5F, 8.5F, 11.5F, 8.5F, 8.0F, 8.5F, 8.0F, 12.0F, 8.0F, 13.0F, 8.1F, 13.0F}
      ),
      new SdfIconRenderer.Inner_DataUtil(
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
      new SdfIconRenderer.Inner_DataUtil(
         "cat_player",
         new float[]{4.0F, 13.0F, 4.5F, 10.5F, 4.5F, 10.5F, 6.5F, 9.0F, 6.5F, 9.0F, 9.5F, 9.0F, 9.5F, 9.0F, 11.5F, 10.5F, 11.5F, 10.5F, 12.0F, 13.0F}
      ),
      new SdfIconRenderer.Inner_DataUtil("cat_misc", new float[]{4.0F, 8.0F, 4.1F, 8.0F, 8.0F, 8.0F, 8.1F, 8.0F, 12.0F, 8.0F, 12.1F, 8.0F}),
      new SdfIconRenderer.Inner_DataUtil(
         "cat_render", new float[]{3.0F, 8.0F, 8.0F, 4.5F, 8.0F, 4.5F, 13.0F, 8.0F, 3.0F, 8.0F, 8.0F, 11.5F, 8.0F, 11.5F, 13.0F, 8.0F}
      ),
      new SdfIconRenderer.Inner_DataUtil(
         "cat_addon", new float[]{5.5F, 5.5F, 5.6F, 5.5F, 10.5F, 5.5F, 10.6F, 5.5F, 5.5F, 10.5F, 5.6F, 10.5F, 10.5F, 10.5F, 10.6F, 10.5F}
      ),
      new SdfIconRenderer.Inner_DataUtil("dot", new float[0])
   };
   private static final Map<String, Integer> TILE_INDEX = new HashMap<>(init_CachePool_DataUtilArr.length * 2);
   private static final int init_I = 64;
   private static final Identifier init_Identifier;
   private static volatile SdfIconRenderer.Inner_StreamUtil init_CachePool_StreamUt;
   private static volatile boolean init_Z;
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
         if (tile < 0 || !SdfRenderPipelines.init_Z()) {
            return false;
         }

         if (w > 0 && h > 0 && color >>> 24 != 0) {
            SdfIconRenderer.Inner_StreamUtil atlas = init_CachePool_StreamUt();
            if (atlas == null) {
               return false;
            }

            float size = Math.min(w, h);
            float left = x + (w - size) * 0.5F;
            float top = y + (h - size) * 0.5F;
            float u = (float)tile / init_CachePool_DataUtilArr.length;
            float u1 = (tile + 1.0F) / init_CachePool_DataUtilArr.length;
            TexturedQuadRenderState element = TexturedQuadRenderState.init(
               SdfRenderPipelines.load(),
               TextureSetup.singleTexture(atlas.getTextureView(), atlas.getSampler()),
               new Matrix3x2f(graphics.pose()),
               new float[]{left, top, left + size, top + size, u, 0.0F, u1, 1.0F},
               1,
               color,
               UiScissorStack.init_ScreenRectangle()
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
      if (init_CachePool_StreamUt == null && !init_Z && prewarmedImage == null) {
         try {
            NativeImage built = init_NativeImage();
            synchronized (SdfIconRenderer.class) {
               if (init_CachePool_StreamUt == null && !init_Z && prewarmedImage == null) {
                  prewarmedImage = built;
               } else {
                  built.close();
               }
            }
         } catch (Throwable var4) {
         }
      }
   }

   public static void init_V() {
      SdfIconRenderer.Inner_StreamUtil toRelease;
      synchronized (SdfIconRenderer.class) {
         toRelease = init_CachePool_StreamUt;
         init_CachePool_StreamUt = null;
         init_Z = false;
         if (prewarmedImage != null) {
            prewarmedImage.close();
            prewarmedImage = null;
         }
      }

      if (toRelease != null) {
         Minecraft.getInstance().getTextureManager().release(init_Identifier);
      }
   }

   private static SdfIconRenderer.Inner_StreamUtil init_CachePool_StreamUt() {
      SdfIconRenderer.Inner_StreamUtil current = init_CachePool_StreamUt;
      if (current == null && !init_Z) {
         synchronized (SdfIconRenderer.class) {
            current = init_CachePool_StreamUt;
            if (current == null && !init_Z) {
               NativeImage pixels = null;
               boolean ownedByAtlas = false;
               try {
                  pixels = prewarmedImage;
                  prewarmedImage = null;
                  if (pixels == null) {
                     pixels = init_NativeImage();
                  }

                  current = new SdfIconRenderer.Inner_StreamUtil(pixels);
                  ownedByAtlas = true;
                  Minecraft.getInstance().getTextureManager().register(init_Identifier, current);
                  init_CachePool_StreamUt = current;
               } catch (Throwable error) {
                  if (pixels != null && !ownedByAtlas) {
                     try {
                        pixels.close();
                     } catch (Throwable ignored) {
                     }
                  }
                  init_Z = true;
                  return null;
               }
            }
         }
      }

      return current;
   }

   private static NativeImage init_NativeImage() {
      int width = 64 * init_CachePool_DataUtilArr.length;
      NativeImage image = new NativeImage(Format.LUMINANCE, width, 64, false);
      long base = image.getPointer();

      for (int tile = 0; tile < init_CachePool_DataUtilArr.length; tile++) {
         SdfIconRenderer.Inner_DataUtil glyph = init_CachePool_DataUtilArr[tile];
         float[] segments = glyph.init_FArr();
         String name = glyph.init_Str();
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
      for (int i = 0; i < init_CachePool_DataUtilArr.length; i++) {
         TILE_INDEX.put(init_CachePool_DataUtilArr[i].init_Str(), i);
      }

      init_Identifier = Identifier.fromNamespaceAndPath("autismclient", "ui/sdf_icons");
   }

   private record Inner_DataUtil(String init_Str, float[] init_FArr) {
   }

   private static final class Inner_StreamUtil extends AbstractTexture {
      Inner_StreamUtil(NativeImage pixels) {
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
