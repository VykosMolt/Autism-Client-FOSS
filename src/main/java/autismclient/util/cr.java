package autismclient.util;

import autismclient.AutismClientAddon;
import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.LoaderContext;
import com.github.weisj.jsvg.parser.SVGLoader;
import com.github.weisj.jsvg.view.ViewBox;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.FilterMode;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.ARGB;

public final class cr {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final Identifier init_Identifier = Identifier.fromNamespaceAndPath("autismclient", "textures/gui/hud/autismclient.svg");
   private static final Identifier load_Identifier = Identifier.fromNamespaceAndPath("autismclient", "dynamic/hud/autismclient_svg");
   private static final int init_I = 2400;
   private static final int load_I = 800;
   private static final int read = 4;
   private static final long init_J = 150L;
   private static cr.Inner_StreamUtil init_cr_StreamUtil;
   private static int write = -1;
   private static int update = -1;
   private static int sync = -1;
   private static int flush = -1;
   private static long load_J;
   private static BufferedImage init_BufferedImage;
   private static boolean init_Z;
   private static final int close = 3;
   private static int open;

   private cr() {
   }

   public static boolean init(GuiGraphicsExtractor context, int x, int y, int width, int height, float alpha) {
      if (!init_Z && width > 0 && height > 0) {
         cr.Inner_StreamUtil entry = init_cr_StreamUtil(width, height);
         if (entry == null) {
            return false;
         }

         context.blit(
            RenderPipelines.GUI_TEXTURED,
            entry.init_Identifier,
            x,
            y,
            0.0F,
            0.0F,
            width,
            height,
            entry.init_I,
            entry.load,
            entry.init_I,
            entry.load,
            ARGB.white(alpha)
         );
         return true;
      } else {
         return false;
      }
   }

   public static void init_V() {
      load();
      sync = -1;
      flush = -1;
      init_BufferedImage = null;
      init_Z = false;
      open = 0;
   }

   private static void load() {
      if (init_cr_StreamUtil != null) {
         try {
            init_Minecraft.getTextureManager().release(init_cr_StreamUtil.init_Identifier());
         } catch (Throwable var1) {
         }

         init_cr_StreamUtil = null;
         write = -1;
         update = -1;
      }
   }

   private static cr.Inner_StreamUtil init_cr_StreamUtil(int width, int height) {
      int textureWidth = Math.max(width, width * 4);
      int textureHeight = Math.max(height, height * 4);
      if (init_cr_StreamUtil != null && write == textureWidth && update == textureHeight) {
         return init_cr_StreamUtil;
      }

      if (init_cr_StreamUtil != null) {
         long now = System.currentTimeMillis();
         if (textureWidth != sync || textureHeight != flush) {
            sync = textureWidth;
            flush = textureHeight;
            load_J = now;
         }

         if (now - load_J < 150L) {
            return init_cr_StreamUtil;
         }
      }

      String key = textureWidth + "x" + textureHeight;

      try {
         NativeImage image = init_NativeImage(textureWidth, textureHeight);
         Identifier id = Identifier.fromNamespaceAndPath(load_Identifier.getNamespace(), load_Identifier.getPath() + "/" + key);
         init_Minecraft.getTextureManager().register(id, new cr.Inner_CachePool("AUTISM SVG HUD Logo " + key, image));
         load();
         cr.Inner_StreamUtil entry = new cr.Inner_StreamUtil(id, textureWidth, textureHeight);
         init_cr_StreamUtil = entry;
         write = textureWidth;
         update = textureHeight;
         open = 0;
         return entry;
      } catch (Throwable t) {
         if (++open >= 3) {
            init_Z = true;
            AutismClientAddon.LOG.warn("SVG HUD logo disabled after {} failed loads", open, t);
         }

         return init_cr_StreamUtil;
      }
   }

   private static NativeImage init_NativeImage(int textureWidth, int textureHeight) throws IOException {
      if (init_BufferedImage == null) {
         init_BufferedImage = init_BufferedImage();
      }

      BufferedImage cropped = init_BufferedImage;
      BufferedImage fitted = new BufferedImage(textureWidth, textureHeight, 2);
      Graphics2D fitGraphics = fitted.createGraphics();
      init(fitGraphics);
      int targetW = textureWidth;
      int targetH = Math.max(1, Math.round(targetW * ((float)cropped.getHeight() / cropped.getWidth())));
      if (targetH > textureHeight) {
         targetH = textureHeight;
         targetW = Math.max(1, Math.round(targetH * ((float)cropped.getWidth() / cropped.getHeight())));
      }

      int dx = (textureWidth - targetW) / 2;
      int dy = (textureHeight - targetH) / 2;
      fitGraphics.drawImage(cropped, dx, dy, targetW, targetH, null);
      fitGraphics.dispose();
      return init_NativeImage(fitted);
   }

   private static BufferedImage init_BufferedImage() throws IOException {
      Optional<Resource> resource = init_Minecraft.getResourceManager().getResource(init_Identifier);
      if (resource.isEmpty()) {
         throw new IllegalStateException("Missing SVG resource " + init_Identifier);
      }

      SVGDocument document;
      try (InputStream in = resource.get().open()) {
         document = new SVGLoader().load(in, null, LoaderContext.createDefault());
      }

      if (document == null) {
         throw new IllegalStateException("Unable to parse SVG resource " + init_Identifier);
      }

      BufferedImage raw = new BufferedImage(2400, 800, 2);
      Graphics2D graphics = raw.createGraphics();
      init(graphics);
      document.render(null, graphics, new ViewBox(2400.0F, 800.0F));
      graphics.dispose();
      cr.Inner_DataUtil bounds = init_cr_DataUtil(raw);
      return raw.getSubimage(bounds.init, bounds.load, bounds.read, bounds.write);
   }

   private static void init(Graphics2D graphics) {
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
   }

   private static cr.Inner_DataUtil init_cr_DataUtil(BufferedImage image) {
      int minX = image.getWidth();
      int minY = image.getHeight();
      int maxX = -1;
      int maxY = -1;

      for (int y = 0; y < image.getHeight(); y++) {
         for (int x = 0; x < image.getWidth(); x++) {
            if ((image.getRGB(x, y) >>> 24 & 0xFF) > 2) {
               if (x < minX) {
                  minX = x;
               }

               if (y < minY) {
                  minY = y;
               }

               if (x > maxX) {
                  maxX = x;
               }

               if (y > maxY) {
                  maxY = y;
               }
            }
         }
      }

      if (maxX >= minX && maxY >= minY) {
         int pad = 10;
         minX = Math.max(0, minX - pad);
         minY = Math.max(0, minY - pad);
         maxX = Math.min(image.getWidth() - 1, maxX + pad);
         maxY = Math.min(image.getHeight() - 1, maxY + pad);
         return new cr.Inner_DataUtil(minX, minY, maxX - minX + 1, maxY - minY + 1);
      } else {
         return new cr.Inner_DataUtil(0, 0, image.getWidth(), image.getHeight());
      }
   }

   private static NativeImage init_NativeImage(BufferedImage image) {
      ct.Inner_StreamUtil theme = ct.init_ct_StreamUtil();
      boolean recolor = theme.init_Z_ct_DataUtil(ct.Inner_DataUtil.init);
      NativeImage nativeImage = new NativeImage(image.getWidth(), image.getHeight(), true);

      for (int y = 0; y < image.getHeight(); y++) {
         for (int x = 0; x < image.getWidth(); x++) {
            int argb = image.getRGB(x, y);
            if (recolor) {
               argb = ct.load(argb, ct.Inner_DataUtil.init, theme);
            }

            nativeImage.setPixel(x, y, argb);
         }
      }

      return nativeImage;
   }

   private static final class Inner_CachePool extends AbstractTexture {
      private final NativeImage init;

      private Inner_CachePool(String label, NativeImage pixels) {
         this.init = pixels;
         this.texture = RenderSystem.getDevice().createTexture(label, 5, GpuFormat.RGBA8_UNORM, pixels.getWidth(), pixels.getHeight(), 1, 1);
         this.sampler = RenderSystem.getSamplerCache().getRepeat(FilterMode.LINEAR);
         this.textureView = RenderSystem.getDevice().createTextureView(this.texture);
         RenderSystem.getDevice().createCommandEncoder().writeToTexture(this.texture, pixels);
      }

      @Override
      public void close() {
         this.init.close();
         super.close();
      }
   }

   private record Inner_DataUtil(int init, int load, int read, int write) {
   }

   private record Inner_StreamUtil(Identifier init_Identifier, int init_I, int load) {
   }
}
