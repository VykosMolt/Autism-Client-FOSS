package autismclient.gui.sdf;

import autismclient.mixin.accessor.AutismGuiGraphicsExtractorAccessor;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.NativeImage.Format;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;
import org.lwjgl.system.MemoryUtil;

public final class SdfShapeRenderer {
   public static final int FLOATS_PER_QUAD = 8;
   private static final int TILE_SIZE = 64;
   private static final int TILE_MARGIN = 12;
   private static final int write = 20;
   private static final int[] TILE_RADII = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
   private static final int update = 64 * TILE_RADII.length;
   private static final int sync = 128;
   private static final Identifier ATLAS_ID = Identifier.fromNamespaceAndPath("autismclient", "ui/sdf_shapes");
   private static volatile SdfShapeRenderer.ShapeAtlas atlasInstance;
   private static volatile boolean atlasFailed;
   private static final int flush = 512;
   private static final int close = 10;
   private static final LinkedHashMap<SdfShapeRenderer.ShapeKey, SdfShapeRenderer.ShapeAtlas> shapeCache = new LinkedHashMap<>(16, 0.75F, true);
   private static final ArrayDeque<SdfShapeRenderer.ShapeAtlas> retiredAtlases = new ArrayDeque<>();
   private static boolean retireHookRegistered;
   private static boolean shapeAtlasFailed;
   private static int cachedPixels;
   private static final int poll = 4194304;
   private static volatile NativeImage prewarmedImage;

   private SdfShapeRenderer() {
   }

   public static boolean isAvailable() {
      return SdfRenderPipelines.isAvailable() && atlas() != null;
   }

   public static void fillRoundRect(GuiGraphicsExtractor graphics, float x, float y, float w, float h, float radius, int color) {
      float r = Math.min(radius, Math.min(w, h) * 0.5F);
      if (!(w <= 0.0F) && !(h <= 0.0F) && (color & 0xFF000000) != 0) {
         if (!(r < 0.75F) && isAvailable()) {
            drawTile(graphics, bestTile(r), x, y, w, h, r, color, false, false);
         } else {
            graphics.fill(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + h), color);
         }
      }
   }

   public static void fillRoundRect(GuiGraphicsExtractor graphics, float x, float y, float w, float h, float radius, int fill, int border) {
      fillRoundRect(graphics, x, y, w, h, radius, fill);
      if (!(w <= 0.0F) && !(h <= 0.0F) && border >>> 24 != 0) {
         float r = Math.min(radius, Math.min(w, h) * 0.5F);
         if (!(r < 0.75F) && isAvailable()) {
            drawTile(graphics, bestTile(r), x, y, w, h, r, border, true, false);
         } else {
            int left = Math.round(x);
            int top = Math.round(y);
            int right = Math.round(x + w);
            int bottom = Math.round(y + h);
            graphics.fill(left, top, right, Math.min(bottom, top + 1), border);
            if (bottom > top + 1) {
               graphics.fill(left, bottom - 1, right, bottom, border);
            }

            if (bottom > top + 2) {
               graphics.fill(left, top + 1, Math.min(right, left + 1), bottom - 1, border);
               if (right > left + 1) {
                  graphics.fill(right - 1, top + 1, right, bottom - 1, border);
               }
            }
         }
      }
   }

   public static void drawRoundedRect(
      GuiGraphicsExtractor graphics, float x, float y, float w, float h, float radius, int topFill, int bottomFill, int topBorder, int bottomBorder
   ) {
      if (!(w <= 0.0F) && !(h <= 0.0F)) {
         float r = Math.min(radius, Math.min(w, h) * 0.5F);
         if (!(r < 0.75F) && isAvailable()) {
            int tile = bestTile(r);
            if ((topFill | bottomFill) >>> 24 != 0) {
               init(graphics, tile, x, y, w, h, r, topFill, bottomFill, false, false);
            }

            if ((topBorder | bottomBorder) >>> 24 != 0) {
               init(graphics, tile, x, y, w, h, r, topBorder, bottomBorder, true, false);
            }
         } else {
            fillRoundRect(graphics, x, y, w, h, radius, bottomFill, bottomBorder);
         }
      }
   }

   public static void load(GuiGraphicsExtractor graphics, float x, float y, float w, float h, float radius, int color) {
      if (!(w <= 0.0F) && !(h <= 0.0F) && color >>> 24 != 0) {
         int alpha = color >>> 24;

         for (int spread = 6; spread >= 1; spread--) {
            int layerAlpha = Math.round(alpha * (7 - spread) / 28.0F);
            if (layerAlpha != 0) {
               fillRoundRect(graphics, x - spread, y + 2.0F - spread, w + spread * 2, h + spread * 2, radius + spread, layerAlpha << 24 | color & 16777215);
            }
         }
      }
   }

   public static void drawCircle(GuiGraphicsExtractor graphics, float cx, float cy, float r, int color) {
      if (!(r <= 0.0F) && (color & 0xFF000000) != 0) {
         if (!isAvailable()) {
            graphics.fill(Math.round(cx - r), Math.round(cy - r), Math.round(cx + r), Math.round(cy + r), color);
         } else {
            drawTile(graphics, TILE_RADII.length - 1, cx - r, cy - r, r * 2.0F, r * 2.0F, r, color, false, false);
         }
      }
   }

   public static void read(GuiGraphicsExtractor graphics, float x, float y, float w, float h, float radius, int color) {
      if (!(w <= 0.0F) && !(h <= 0.0F) && color >>> 24 != 0) {
         float r = Math.min(radius, Math.min(w, h) * 0.5F);
         if (!(r < 0.75F) && isAvailable()) {
            drawTile(graphics, bestTile(r), x, y, w, h, r, color, false, true);
         } else {
            graphics.fill(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + h), color);
         }
      }
   }

   public static void init(GuiGraphicsExtractor graphics, float centerX, float y, float[] widths, float rowHeight, float radius, int color) {
      float[] heights = new float[widths.length];
      Arrays.fill(heights, rowHeight);
      init(graphics, centerX, y, widths, heights, radius, color);
   }

   public static void init(GuiGraphicsExtractor graphics, float centerX, float y, float[] widths, float[] heights, float radius, int color) {
      if (widths.length != 0 && widths.length <= 32 && widths.length == heights.length && Float.isFinite(centerX) && Float.isFinite(y) && color >>> 24 != 0) {
         List<Float> values = new ArrayList<>(widths.length);
         float w = 0.0F;

         for (float width : widths) {
            if (!Float.isFinite(width) || width <= 0.0F || width > 1024.0F) {
               return;
            }

            values.add(width);
            w = Math.max(w, width);
         }

         ArrayList<Float> sizes = new ArrayList<>(heights.length);
         float h = 0.0F;
         float minHeight = 1.0F / 0.0F;

         for (float height : heights) {
            if (!Float.isFinite(height) || height <= 0.0F) {
               return;
            }

            sizes.add(height);
            h += height;
            minHeight = Math.min(minHeight, height);
         }

         if (!(h > 1024.0F)) {
            float r = Float.isFinite(radius) ? Math.clamp(radius, 0.0F, Math.min(4.0F, minHeight / 2.0F)) : 0.0F;
            values = SteppedRowContour.smoothWidths(values, r);
            boolean equal = true;

            for (int i = 1; i < values.size(); i++) {
               if (!values.get(i).equals(values.getFirst())) {
                  equal = false;
                  break;
               }
            }

            if (equal) {
               fillRoundRect(graphics, centerX - values.getFirst() / 2.0F, y, values.getFirst(), h, r, color);
            } else if (r <= 0.0F) {
               init(graphics, centerX, y, values, heights, color);
            } else if (!shapeAtlasFailed && isAvailable()) {
               SdfShapeRenderer.ShapeKey key = new SdfShapeRenderer.ShapeKey(values, List.copyOf(sizes), r);
               if (!retireHookRegistered) {
                  ClientTickEvents.END_CLIENT_TICK.register(mc -> {
                     while (!retiredAtlases.isEmpty()) {
                        retiredAtlases.removeFirst().close();
                     }
                  });
                  retireHookRegistered = true;
               }

               SdfShapeRenderer.ShapeAtlas shape = shapeCache.get(key);
               if (shape == null) {
                  NativeImage pixels = null;

                  try {
                     pixels = init(new SteppedRowContour(key.init_List, key.sizes, r));
                     shape = new SdfShapeRenderer.ShapeAtlas(pixels);
                  } catch (Throwable error) {
                     if (pixels != null) {
                        pixels.close();
                     }

                     shapeAtlasFailed = true;
                     init(graphics, centerX, y, values, heights, color);
                     return;
                  }

                  shapeCache.put(key, shape);
                  cachedPixels = cachedPixels + shape.pixelCount;

                  while (shapeCache.size() > 1 && (shapeCache.size() > 512 || cachedPixels > 4194304)) {
                     Iterator<SdfShapeRenderer.ShapeAtlas> iterator = shapeCache.values().iterator();
                     SdfShapeRenderer.ShapeAtlas retired = iterator.next();
                     cachedPixels = cachedPixels - retired.pixelCount;
                     retiredAtlases.add(retired);
                     iterator.remove();
                  }
               }

               float textureW = (float)Math.ceil(w) + 20.0F;
               float textureH = (float)Math.ceil(h) + 20.0F;
               float left = centerX - w / 2.0F - 10.0F;
               float top = y - 10.0F;
               float[] quad = new float[]{left, top, left + textureW, top + textureH, 0.0F, 0.0F, 1.0F, 1.0F};
               TexturedQuadRenderState element = TexturedQuadRenderState.create(
                  SdfRenderPipelines.pipeline(),
                  TextureSetup.singleTexture(shape.getTextureView(), shape.getSampler()),
                  new Matrix3x2f(graphics.pose()),
                  quad,
                  1,
                  color,
                  UiScissorStack.peek()
               );
               if (element != null) {
                  ((AutismGuiGraphicsExtractorAccessor)graphics).autism$guiRenderState().addGuiElement(element);
               }
            } else {
               init(graphics, centerX, y, values, heights, color);
            }
         }
      }
   }

   private static void init(GuiGraphicsExtractor graphics, float centerX, float y, List<Float> widths, float[] heights, int color) {
      for (int i = 0; i < widths.size(); i++) {
         graphics.pose().pushMatrix();

         try {
            graphics.pose().translate(centerX - widths.get(i) / 2.0F, y);
            graphics.pose().scale(widths.get(i), heights[i]);
            graphics.fill(0, 0, 1, 1, color);
         } finally {
            graphics.pose().popMatrix();
         }

         y += heights[i];
      }
   }

   private static NativeImage init(SteppedRowContour shape) {
      int width = (int)Math.ceil(shape.width) + 20;
      int height = (int)Math.ceil(shape.load) + 20;
      float[] distances = new float[width * height];
      Arrays.fill(distances, 64.0F);
      float offsetX = shape.width / 2.0F + 10.0F;
      float offsetY = 10.0F;

      for (SteppedRowContour.Inner_DataUtil edge : shape.edges) {
         int minX = Math.max(0, (int)Math.floor(Math.min(edge.init(), edge.init() + edge.read()) + offsetX - 8.0F));
         int maxX = Math.min(width - 1, (int)Math.ceil(Math.max(edge.init(), edge.init() + edge.read()) + offsetX + 8.0F));
         int minY = Math.max(0, (int)Math.floor(Math.min(edge.load(), edge.load() + edge.write()) + offsetY - 8.0F));
         int maxY = Math.min(height - 1, (int)Math.ceil(Math.max(edge.load(), edge.load() + edge.write()) + offsetY + 8.0F));

         for (int py = minY; py <= maxY; py++) {
            for (int px = minX; px <= maxX; px++) {
               int i = py * width + px;
               distances[i] = Math.min(distances[i], SteppedRowContour.distance(edge, px + 0.5F - offsetX, py + 0.5F - offsetY));
            }
         }
      }

      NativeImage pixels = new NativeImage(Format.LUMINANCE, width, height, false);
      long base = pixels.getPointer();

      for (int py = 0; py < height; py++) {
         for (int px = 0; px < width; px++) {
            int i = py * width + px;
            float distance = (float)Math.sqrt(distances[i]);
            if (shape.path.contains(px + 0.5F - offsetX, py + 0.5F - offsetY)) {
               distance = -distance;
            }

            MemoryUtil.memPutByte(base + i, distanceToAlpha(distance));
         }
      }

      return pixels;
   }

   public static void reset() {
      shapeAtlasFailed = false;
      shapeCache.values().forEach(AbstractTexture::close);
      shapeCache.clear();
      cachedPixels = 0;

      while (!retiredAtlases.isEmpty()) {
         retiredAtlases.removeFirst().close();
      }

      SdfShapeRenderer.ShapeAtlas old = atlasInstance;
      if (old != null) {
         try {
            Minecraft.getInstance().getTextureManager().release(ATLAS_ID);
         } catch (Throwable var3) {
         }
      }

      synchronized (SdfShapeRenderer.class) {
         if (prewarmedImage != null) {
            prewarmedImage.close();
            prewarmedImage = null;
         }
      }

      atlasInstance = null;
      atlasFailed = false;
   }

   private static int bestTile(float radius) {
      int best = 0;
      float bestScore = 3.4028235E38F;

      for (int i = 0; i < TILE_RADII.length; i++) {
         float ratio = radius / TILE_RADII[i];
         float score = Math.abs((float)(Math.log(ratio) / Math.log(2.0)));
         if (score < bestScore) {
            bestScore = score;
            best = i;
         }
      }

      return best;
   }

   private static void drawTile(
      GuiGraphicsExtractor graphics, int tile, float x, float y, float w, float h, float r, int color, boolean outline, boolean topOnly
   ) {
      init(graphics, tile, x, y, w, h, r, color, color, outline, topOnly);
   }

   private static void init(
      GuiGraphicsExtractor graphics, int tile, float x, float y, float w, float h, float r, int topColor, int bottomColor, boolean outline, boolean topOnly
   ) {
      SdfShapeRenderer.ShapeAtlas atlas = atlas();
      if (atlas != null) {
         int rTile = TILE_RADII[tile];
         float corner = Math.min(r, Math.min(w, h) * 0.5F);
         float[] xs = init(x, w, corner);
         float[] ys = init(y, h, corner);
         float tileU0 = (float)(tile * 64) / update;
         float uStep = 64.0F / update;
         float marginU = uStep * 0.1875F;
         float cornerU = uStep * ((12 + rTile) / 64.0F);
         float farCornerU = uStep * ((52 - rTile) / 64.0F);
         float farMarginU = uStep * 0.8125F;
         float[] us = new float[]{tileU0 + marginU, tileU0 + cornerU, tileU0 + farCornerU, tileU0 + farMarginU};
         int rowOffset = outline ? 64 : 0;
         float marginV = (rowOffset + 12) / 128.0F;
         float cornerV = (rowOffset + 12 + rTile) / 128.0F;
         float farCornerV = (rowOffset + 64 - 12 - rTile) / 128.0F;
         float farMarginV = (rowOffset + 64 - 12) / 128.0F;
         float[] vs = new float[]{marginV, cornerV, farCornerV, farMarginV};
         float midU = tileU0 + uStep * 0.5F;
         float midV = (rowOffset + 32.0F) / 128.0F;
         float[] quads = new float[72];
         int n = 0;

         for (int row = 0; row < (topOnly ? 1 : 3); row++) {
            for (int col = 0; col < 3; col++) {
               if (!(xs[col + 1] <= xs[col]) && !(ys[row + 1] <= ys[row])) {
                  int o = n * 8;
                  quads[o] = xs[col];
                  quads[o + 1] = ys[row];
                  quads[o + 2] = xs[col + 1];
                  quads[o + 3] = ys[row + 1];
                  quads[o + 4] = col == 1 ? midU : us[col];
                  quads[o + 5] = row == 1 ? midV : vs[row];
                  quads[o + 6] = col == 1 ? midU : us[col + 1];
                  quads[o + 7] = row == 1 ? midV : vs[row + 1];
                  n++;
               }
            }
         }

         if (topOnly && y + h > ys[1]) {
            int o = n++ * 8;
            quads[o] = x;
            quads[o + 1] = ys[1];
            quads[o + 2] = x + w;
            quads[o + 3] = y + h;
            quads[o + 4] = quads[o + 6] = (tile * 64 + 32.0F) / update;
            quads[o + 5] = quads[o + 7] = 0.25F;
         }

         AutismGuiGraphicsExtractorAccessor accessor = (AutismGuiGraphicsExtractorAccessor)graphics;
         ScreenRectangle scissor = UiScissorStack.peek();
         GuiRenderState renderState = accessor.autism$guiRenderState();
         TexturedQuadRenderState element = TexturedQuadRenderState.create(
            SdfRenderPipelines.pipeline(),
            TextureSetup.singleTexture(atlas.getTextureView(), atlas.getSampler()),
            new Matrix3x2f(graphics.pose()),
            quads,
            n,
            topColor,
            bottomColor,
            y,
            h,
            scissor
         );
         if (element != null) {
            renderState.addGuiElement(element);
         }
      }
   }

   static float[] init(float start, float size, float corner) {
      float end = start + size;
      float a = start + corner;
      float b = end - corner;
      if (b - a < 1.0F) {
         float mid = start + size * 0.5F;
         a = mid;
         b = mid;
      }

      return new float[]{start, a, b, end};
   }

   public static void prewarm() {
      if (atlasInstance == null && !atlasFailed && prewarmedImage == null) {
         try {
            NativeImage built = buildAtlasImage();
            synchronized (SdfShapeRenderer.class) {
               if (atlasInstance == null && !atlasFailed && prewarmedImage == null) {
                  prewarmedImage = built;
               } else {
                  built.close();
               }
            }
         } catch (Throwable var4) {
         }
      }
   }

   private static SdfShapeRenderer.ShapeAtlas atlas() {
      SdfShapeRenderer.ShapeAtlas current = atlasInstance;
      if (current == null && !atlasFailed) {
         synchronized (SdfShapeRenderer.class) {
            current = atlasInstance;
            if (current == null && !atlasFailed) {
               try {
                  NativeImage pixels = prewarmedImage;
                  prewarmedImage = null;
                  if (pixels == null) {
                     pixels = buildAtlasImage();
                  }

                  current = new SdfShapeRenderer.ShapeAtlas(pixels);
                  Minecraft.getInstance().getTextureManager().register(ATLAS_ID, current);
                  atlasInstance = current;
               } catch (Throwable t) {
                  atlasFailed = true;
                  return null;
               }
            }
         }
      }

      return current;
   }

   private static NativeImage buildAtlasImage() {
      NativeImage image = new NativeImage(Format.LUMINANCE, update, 128, false);
      long base = image.getPointer();

      for (int t = 0; t < TILE_RADII.length; t++) {
         float r = TILE_RADII[t];

         for (int py = 0; py < 64; py++) {
            for (int px = 0; px < 64; px++) {
               float cx = px + 0.5F - 32.0F;
               float cy = py + 0.5F - 32.0F;
               float dist = roundedBoxDistance(cx, cy, 20.0F, 20.0F, r);
               float inner = roundedBoxDistance(cx, cy, 19.0F, 19.0F, Math.max(0.0F, r - 1.0F));
               long offset = (long)py * update + t * 64 + px;
               MemoryUtil.memPutByte(base + offset, distanceToAlpha(dist));
               MemoryUtil.memPutByte(base + 64L * update + offset, distanceToAlpha(Math.max(dist, -inner)));
            }
         }
      }

      return image;
   }

   private static byte distanceToAlpha(float distance) {
      return (byte)Math.clamp(Math.round((0.5F - distance / 16.0F) * 255.0F), 0, 255);
   }

   private static float roundedBoxDistance(float px, float py, float bx, float by, float r) {
      float qx = Math.abs(px) - (bx - r);
      float qy = Math.abs(py) - (by - r);
      float ax = Math.max(qx, 0.0F);
      float ay = Math.max(qy, 0.0F);
      float outside = (float)Math.sqrt(ax * ax + ay * ay);
      float inside = Math.min(Math.max(qx, qy), 0.0F);
      return outside + inside - r;
   }

   private record ShapeKey(List<Float> init_List, List<Float> sizes, float init_F) {
   }

   private static final class ShapeAtlas extends AbstractTexture {
      final int pixelCount;

      ShapeAtlas(NativeImage pixels) {
         this.pixelCount = pixels.getWidth() * pixels.getHeight();
         this.texture = RenderSystem.getDevice().createTexture("autismclient sdf shapes", 15, GpuFormat.R8_UNORM, pixels.getWidth(), pixels.getHeight(), 1, 1);
         this.sampler = RenderSystem.getSamplerCache()
            .getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, false);
         this.textureView = RenderSystem.getDevice().createTextureView(this.texture);
         RenderSystem.getDevice().createCommandEncoder().writeToTexture(this.texture, pixels);
         pixels.close();
      }
   }
}
