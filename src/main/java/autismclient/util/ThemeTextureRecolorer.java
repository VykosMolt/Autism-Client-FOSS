package autismclient.util;

import autismclient.AutismClientAddon;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.FilterMode;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.CubeMapTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public final class ThemeTextureRecolorer {
   private static final Minecraft MINECRAFT = Minecraft.getInstance();
   public static final Identifier init_Identifier = Identifier.fromNamespaceAndPath("autismclient", "textures/gui/title/background/panorama");
   private static final Identifier load_Identifier = Identifier.fromNamespaceAndPath("autismclient", "textures/gui/title/background/panorama_overlay.png");
   private static final Identifier read_Identifier = Identifier.fromNamespaceAndPath("autismclient", "dynamic/theme/panorama_overlay");
   private static final Map<Identifier, ThemeTextureRecolorer.Inner_BufferHelper> recolorCache = new HashMap<>();
   private static final Map<Identifier, ThemeTextureRecolorer.Inner_DataUtil> tintCache = new HashMap<>();
   private static final Set<Identifier> init_Set = new HashSet<>();
   private static final Set<Identifier> load_Set = new HashSet<>();
   private static final Map<Identifier, Integer> failedGenerations = new HashMap<>();
   private static final Map<Identifier, Identifier> themeTextureCache = new HashMap<>();
   private static int currentGeneration;
   private static ThemeTextureRecolorer.Inner_ConfigStore preview;
   private static volatile boolean init_Z;
   private static AbstractTexture init_AbstractTexture;
   private static boolean load_Z;

   private ThemeTextureRecolorer() {
   }

   public static Identifier init(Identifier source, ThemePaletteManager.ColorRole ch) {
      if (source == null) {
         return null;
      }

      ThemePaletteManager.CompiledPalette st = ThemePaletteManager.getPalette();
      if (!st.isModified(ch)) {
         return source;
      }

      ThemeTextureRecolorer.Inner_BufferHelper cached = recolorCache.get(source);
      if (cached != null && cached.generation == currentGeneration) {
         return cached.id;
      }

      Integer failedGen = failedGenerations.get(source);
      if (failedGen != null && failedGen == currentGeneration) {
         return cached != null ? cached.id : source;
      }

      if (cached == null && !load_Set.contains(source)) {
         Identifier built = recoloredId(source, ch, st);
         if (built != null) {
            return built;
         }
      }

      scheduleRecolor(source, ch, st);
      return cached != null ? cached.id : source;
   }

   public static Identifier recolor(Identifier source, int targetColor) {
      if (source == null) {
         return null;
      }

      ThemeTextureRecolorer.Inner_DataUtil cached = tintCache.get(source);
      if (cached != null && cached.color == targetColor) {
         return cached.id;
      }

      scheduleTint(source, targetColor);
      return cached != null ? cached.id : source;
   }

   private static void scheduleTint(Identifier source, int targetColor) {
      if (init_Set.add(source)) {
         byte[] png = readPng(source);
         if (png == null) {
            init_Set.remove(source);
         } else {
            float[] hsb = Color.RGBtoHSB(targetColor >> 16 & 0xFF, targetColor >> 8 & 0xFF, targetColor & 0xFF, null);
            float targetHue = hsb[0];
            float targetSat = hsb[1];
            BackgroundTaskExecutor.submit("skybox-recolor", () -> {
               NativeImage recolored = null;

               try (NativeImage src = NativeImage.read(png)) {
                  recolored = src.mappedCopy(argb -> ThemePaletteManager.recolorBackdropPixel(argb, targetHue, targetSat));
               } catch (Throwable t) {
                  AutismClientAddon.LOG.warn("Skybox recolor failed for {}", source, t);
               }

               NativeImage result = recolored;
               MINECRAFT.execute(() -> {
                  init_Set.remove(source);
                  if (result != null) {
                     Identifier id = skyboxTextureId(source);
                     MINECRAFT.getTextureManager().register(id, new ThemeTextureRecolorer.Inner_StreamUtil(source.toString(), result, FilterMode.LINEAR));
                     tintCache.put(source, new ThemeTextureRecolorer.Inner_DataUtil(id, targetColor));
                  }
               });
            });
         }
      }
   }

   private static Identifier skyboxTextureId(Identifier source) {
      return Identifier.fromNamespaceAndPath(
         "autismclient", "dynamic/skybox/" + source.getNamespace() + "/" + source.getPath().replace('/', '_').replace(".png", "")
      );
   }

   private static Identifier recoloredId(Identifier source, ThemePaletteManager.ColorRole ch, ThemePaletteManager.CompiledPalette st) {
      byte[] png = readPng(source);
      if (png == null) {
         return null;
      }

      int generation = currentGeneration;

      try (NativeImage src = NativeImage.read(png)) {
         NativeImage recolored = src.mappedCopy(argb -> ThemePaletteManager.recolorTexturePixel(argb, ch, st));
         Identifier id = themeTextureId(source);
         MINECRAFT.getTextureManager().register(id, new ThemeTextureRecolorer.Inner_StreamUtil(source.toString(), recolored, FilterMode.LINEAR));
         recolorCache.put(source, new ThemeTextureRecolorer.Inner_BufferHelper(id, generation));
         return id;
      } catch (Throwable t) {
         failedGenerations.put(source, generation);
         AutismClientAddon.LOG.warn("Theme recolor (sync) failed for {} (backing off this theme generation)", source, t);
         return null;
      }
   }

   private static void scheduleRecolor(Identifier source, ThemePaletteManager.ColorRole ch, ThemePaletteManager.CompiledPalette st) {
      if (load_Set.add(source)) {
         int generation = currentGeneration;
         byte[] png = readPng(source);
         if (png == null) {
            load_Set.remove(source);
         } else {
            BackgroundTaskExecutor.submit("theme-recolor", () -> {
               NativeImage recolored = null;

               try (NativeImage src = NativeImage.read(png)) {
                  recolored = src.mappedCopy(argb -> ThemePaletteManager.recolorTexturePixel(argb, ch, st));
               } catch (Throwable t) {
                  AutismClientAddon.LOG.warn("Theme recolor failed for {} (backing off this theme generation)", source, t);
               }

               NativeImage result = recolored;
               MINECRAFT.execute(() -> {
                  load_Set.remove(source);
                  if (generation != currentGeneration) {
                     if (result != null) {
                        result.close();
                     }
                  } else if (result == null) {
                     failedGenerations.put(source, generation);
                  } else {
                     Identifier id = themeTextureId(source);
                     MINECRAFT.getTextureManager().register(id, new ThemeTextureRecolorer.Inner_StreamUtil(source.toString(), result, FilterMode.LINEAR));
                     recolorCache.put(source, new ThemeTextureRecolorer.Inner_BufferHelper(id, generation));
                  }
               });
            });
         }
      }
   }

   private static byte[] readPng(Identifier source) {
      try {
         Optional<Resource> res = MINECRAFT.getResourceManager().getResource(source);
         if (res.isEmpty()) {
            return null;
         }

         try (InputStream in = res.get().open()) {
            return in.readAllBytes();
         }
      } catch (Throwable t) {
         failedGenerations.put(source, currentGeneration);
         AutismClientAddon.LOG.warn("Theme recolor failed for {} (backing off this theme generation)", source, t);
         return null;
      }
   }

   public static Identifier themed(Identifier source) {
      if (source == null) {
         return null;
      }

      Identifier cached = themeTextureCache.get(source);
      if (cached != null) {
         return cached;
      }

      try {
         TextureContents contents = TextureContents.load(MINECRAFT.getResourceManager(), source);
         NativeImage src = contents.image();
         NativeImage white = src.mappedCopy(argb -> argb & 0xFF000000 | 16777215);
         src.close();
         Identifier id = Identifier.fromNamespaceAndPath(
            "autismclient", "dynamic/white/" + source.getNamespace() + "/" + source.getPath().replace('/', '_').replace(".png", "")
         );
         MINECRAFT.getTextureManager().register(id, new ThemeTextureRecolorer.Inner_StreamUtil(source.toString(), white, FilterMode.LINEAR));
         themeTextureCache.put(source, id);
         return id;
      } catch (Throwable t) {
         AutismClientAddon.LOG.warn("Theme whiten failed for {}", source, t);
         return source;
      }
   }

   private static Identifier themeTextureId(Identifier source) {
      return Identifier.fromNamespaceAndPath(
         "autismclient", "dynamic/theme/" + source.getNamespace() + "/" + source.getPath().replace('/', '_').replace(".png", "")
      );
   }

   public static void init(TextureManager textureManager) {
      try {
         preview = new ThemeTextureRecolorer.Inner_ConfigStore(init_Identifier);
         textureManager.register(init_Identifier, preview);
      } catch (Throwable t) {
         init_Z = false;
         AutismClientAddon.LOG.warn("Failed to register themed panorama", t);
      }
   }

   public static boolean isEnabled() {
      return init_Z;
   }

   public static void init_V() {
      if (preview != null && init_Z) {
         int generation = currentGeneration;
         Runnable jobDone = autismclient.gui.ThemeApplyProgressOverlay.init("Recoloring panorama");
         MINECRAFT.execute(() -> {
            try {
               if (generation != currentGeneration) {
                  return;
               }

               TextureContents contents = preview.loadContents(MINECRAFT.getResourceManager());

               try {
                  preview.apply(contents);
                  return;
               } catch (Throwable t) {
                  AutismClientAddon.LOG.warn("Failed to upload the recolored panorama (keeping the current one)", t);

                  try {
                     contents.image().close();
                  } catch (Throwable var10) {
                  }
               }
            } catch (Throwable t) {
               AutismClientAddon.LOG.warn("Failed to recolor themed panorama (keeping the current one)", t);
               return;
            } finally {
               jobDone.run();
            }
         });
      }
   }

   public static Identifier load(Identifier original) {
      ThemePaletteManager.CompiledPalette st = ThemePaletteManager.getPalette();
      if (!st.isModified(ThemePaletteManager.ColorRole.BACKDROP)) {
         return original;
      }

      if (init_AbstractTexture != null) {
         return read_Identifier;
      }

      if (!load_Z) {
         load_Z = true;
         recolorBackdrop(st, () -> {});
      }

      return original;
   }

   private static void recolorBackdrop(ThemePaletteManager.CompiledPalette st, Runnable jobDone) {
      int generation = currentGeneration;

      byte[] png;
      try {
         Optional<Resource> res = MINECRAFT.getResourceManager().getResource(load_Identifier);
         if (res.isEmpty()) {
            load_Z = false;
            jobDone.run();
            return;
         }

         try (InputStream in = res.get().open()) {
            png = in.readAllBytes();
         }
      } catch (Throwable t) {
         AutismClientAddon.LOG.warn("Failed to build themed panorama overlay", t);
         jobDone.run();
         return;
      }

      BackgroundTaskExecutor.submit("theme-overlay", () -> {
         NativeImage recolored = null;

         try (NativeImage src = NativeImage.read(png)) {
            recolored = src.mappedCopy(argb -> ThemePaletteManager.recolorTexturePixel(argb, ThemePaletteManager.ColorRole.BACKDROP, st));
         } catch (Throwable tx) {
            AutismClientAddon.LOG.warn("Failed to build themed panorama overlay", tx);
         }

         NativeImage result = recolored;
         MINECRAFT.execute(() -> {
            try {
               if (generation != currentGeneration) {
                  if (result != null) {
                     result.close();
                  }

                  return;
               }

               if (result != null) {
                  init_AbstractTexture = new ThemeTextureRecolorer.Inner_StreamUtil("panorama_overlay", result, FilterMode.LINEAR);
                  MINECRAFT.getTextureManager().register(read_Identifier, init_AbstractTexture);
                  return;
               }
            } finally {
               jobDone.run();
            }
         });
      });
   }

   public static void load() {
      currentGeneration++;
      failedGenerations.clear();
      load_Z = false;
      init_AbstractTexture = null;
      ThemePaletteManager.CompiledPalette st = ThemePaletteManager.getPalette();
      if (st.isModified(ThemePaletteManager.ColorRole.BACKDROP)) {
         load_Z = true;
         recolorBackdrop(st, autismclient.gui.ThemeApplyProgressOverlay.init("Recoloring backdrop"));
      }

      init_V();
   }

   public static ThemeTextureRecolorer.Inner_CachePool init(Identifier source, ThemePaletteManager.ColorRole channel, int maxDim) {
      try {
         TextureContents contents = TextureContents.load(MINECRAFT.getResourceManager(), source);
         NativeImage full = contents.image();
         NativeImage scaled = downscale(full, maxDim);
         if (scaled != full) {
            full.close();
         }

         NativeImage work = new NativeImage(scaled.getWidth(), scaled.getHeight(), false);

         for (int y = 0; y < scaled.getHeight(); y++) {
            for (int x = 0; x < scaled.getWidth(); x++) {
               work.setPixel(x, y, scaled.getPixel(x, y));
            }
         }

         Identifier id = Identifier.fromNamespaceAndPath("autismclient", "dynamic/theme/preview/" + source.getPath().replace('/', '_').replace(".png", ""));
         ThemeTextureRecolorer.Inner_StreamUtil tex = new ThemeTextureRecolorer.Inner_StreamUtil("preview " + source, work, FilterMode.LINEAR);
         MINECRAFT.getTextureManager().register(id, tex);
         return new ThemeTextureRecolorer.Inner_CachePool(channel, scaled, tex, id);
      } catch (Throwable t) {
         AutismClientAddon.LOG.warn("Failed to build theme preview for {}", source, t);
         return null;
      }
   }

   private static NativeImage downscale(NativeImage src, int maxDim) {
      int w = src.getWidth();
      int h = src.getHeight();
      if (w <= maxDim && h <= maxDim) {
         return src;
      }

      float scale = Math.min((float)maxDim / w, (float)maxDim / h);
      int nw = Math.max(1, Math.round(w * scale));
      int nh = Math.max(1, Math.round(h * scale));
      NativeImage out = new NativeImage(nw, nh, false);

      for (int y = 0; y < nh; y++) {
         int sy0 = (int)Math.floor(y * ((double)h / nh));
         int sy1 = Math.min(h, Math.max(sy0 + 1, (int)Math.ceil((y + 1) * ((double)h / nh))));

         for (int x = 0; x < nw; x++) {
            int sx0 = (int)Math.floor(x * ((double)w / nw));
            int sx1 = Math.min(w, Math.max(sx0 + 1, (int)Math.ceil((x + 1) * ((double)w / nw))));
            long aSum = 0L;
            long rSum = 0L;
            long gSum = 0L;
            long bSum = 0L;
            int n = 0;

            for (int yy = sy0; yy < sy1; yy++) {
               for (int xx = sx0; xx < sx1; xx++) {
                  int px = src.getPixel(xx, yy);
                  int pa = px >>> 24 & 0xFF;
                  aSum += pa;
                  rSum += (long)(px >>> 16 & 0xFF) * pa;
                  gSum += (long)(px >>> 8 & 0xFF) * pa;
                  bSum += (long)(px & 0xFF) * pa;
                  n++;
               }
            }

            if (n == 0) {
               out.setPixel(x, y, 0);
            } else {
               int outA = (int)(aSum / n);
               int outR = aSum == 0L ? 0 : (int)(rSum / aSum);
               int outG = aSum == 0L ? 0 : (int)(gSum / aSum);
               int outB = aSum == 0L ? 0 : (int)(bSum / aSum);
               out.setPixel(x, y, outA << 24 | outR << 16 | outG << 8 | outB);
            }
         }
      }

      return out;
   }

   private record Inner_BufferHelper(Identifier id, int generation) {
   }

   public static final class Inner_CachePool implements AutoCloseable {
      private final ThemePaletteManager.ColorRole channel;
      private final NativeImage source;
      private final ThemeTextureRecolorer.Inner_StreamUtil texture;
      private final Identifier id;
      private int lastSignature = -2147483648;
      private final Object lock = new Object();
      private boolean init_Z;
      private boolean load;
      private boolean read;
      private ThemePaletteManager.CompiledPalette pendingPalette;

      private Inner_CachePool(ThemePaletteManager.ColorRole channel, NativeImage source, ThemeTextureRecolorer.Inner_StreamUtil texture, Identifier id) {
         this.channel = channel;
         this.source = source;
         this.texture = texture;
         this.id = id;
      }

      public Identifier id() {
         return this.id;
      }

      public int width() {
         return this.source.getWidth();
      }

      public int height() {
         return this.source.getHeight();
      }

      private static int paletteSignature(ThemePaletteManager.CompiledPalette st) {
         int sig = Float.floatToIntBits(st.hue(ThemePaletteManager.ColorRole.BACKDROP)) * 31 + Float.floatToIntBits(st.saturation(ThemePaletteManager.ColorRole.BACKDROP));
         return sig == 0 ? 1 : sig;
      }

      public void applyPalette(ThemePaletteManager.CompiledPalette st) {
         int sig = this.channel == ThemePaletteManager.ColorRole.BACKDROP ? paletteSignature(st) : st.roleSignature(this.channel);
         if (sig != this.lastSignature) {
            this.lastSignature = sig;
            synchronized (this.lock) {
               if (this.init_Z) {
                  return;
               }

               this.pendingPalette = st;
               if (this.load) {
                  return;
               }

               this.load = true;
            }

            BackgroundTaskExecutor.submit("theme-preview", this::init_V);
         }
      }

      private void init_V() {
         try {
            while (true) {
               ThemePaletteManager.CompiledPalette st;
               synchronized (this.lock) {
                  if (this.init_Z || this.pendingPalette == null) {
                     this.load = false;
                     break;
                  }

                  st = this.pendingPalette;
                  this.pendingPalette = null;
               }

               int w = this.source.getWidth();
               int h = this.source.getHeight();
               int[] out = new int[w * h];

               for (int y = 0; y < h; y++) {
                  for (int x = 0; x < w; x++) {
                     out[y * w + x] = this.channel == ThemePaletteManager.ColorRole.BACKDROP
                        ? ThemePaletteManager.recolorBackdropPixel(
                           this.source.getPixel(x, y), st.hue(ThemePaletteManager.ColorRole.BACKDROP), st.saturation(ThemePaletteManager.ColorRole.BACKDROP)
                        )
                        : ThemePaletteManager.recolorTexturePixel(this.source.getPixel(x, y), this.channel, st);
                  }
               }

               ThemeTextureRecolorer.MINECRAFT.execute(() -> {
                  synchronized (this.lock) {
                     if (!this.init_Z) {
                        for (int yx = 0; yx < h; yx++) {
                           for (int xx = 0; xx < w; xx++) {
                              this.texture.pixels.setPixel(xx, yx, out[yx * w + xx]);
                           }
                        }

                        this.texture.upload();
                     }
                  }
               });
            }
         } catch (Throwable t) {
            synchronized (this.lock) {
               this.load = false;
            }

            AutismClientAddon.LOG.error("Theme preview recolor failed", t);
         }

         boolean freeNow;
         synchronized (this.lock) {
            freeNow = this.init_Z;
         }

         if (freeNow) {
            this.load_V();
         }
      }

      @Override
      public void close() {
         synchronized (this.lock) {
            this.init_Z = true;
            this.pendingPalette = null;
            if (this.load) {
               return;
            }
         }

         this.load_V();
      }

      private void load_V() {
         synchronized (this.lock) {
            if (this.read) {
               return;
            }

            this.read = true;
         }

         try {
            this.source.close();
         } catch (Throwable var4) {
         }

         try {
            this.texture.close();
         } catch (Throwable var3) {
         }
      }
   }

   private static final class Inner_ConfigStore extends CubeMapTexture {
      private Inner_ConfigStore(Identifier id) {
         super(id);
      }

      @Override
      public TextureContents loadContents(ResourceManager resourceManager) throws IOException {
         TextureContents contents = super.loadContents(resourceManager);
         ThemeTextureRecolorer.init_Z = true;
         ThemePaletteManager.CompiledPalette st = ThemePaletteManager.getPalette();
         float hue = st.hue(ThemePaletteManager.ColorRole.BACKDROP);
         float sat = st.saturation(ThemePaletteManager.ColorRole.BACKDROP);
         NativeImage img = contents.image();
         int changed = 0;

         for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
               int before = img.getPixel(x, y);
               int after = ThemePaletteManager.recolorBackdropPixel(before, hue, sat);
               if (before != after) {
                  img.setPixel(x, y, after);
                  changed++;
               }
            }
         }

         AutismClientAddon.LOG
            .info(
               "Panorama recolored to hue {} sat {}: {} of {} pixels changed",
               String.format(Locale.ROOT, "%.0f", hue * 360.0F),
               String.format(Locale.ROOT, "%.2f", sat),
               changed,
               img.getWidth() * img.getHeight()
            );
         return contents;
      }
   }

   private record Inner_DataUtil(Identifier id, int color) {
   }

   private static final class Inner_StreamUtil extends AbstractTexture {
      private final NativeImage pixels;

      private Inner_StreamUtil(String label, NativeImage pixels, FilterMode filter) {
         this.pixels = pixels;
         this.texture = RenderSystem.getDevice().createTexture(label, 5, GpuFormat.RGBA8_UNORM, pixels.getWidth(), pixels.getHeight(), 1, 1);
         this.sampler = RenderSystem.getSamplerCache().getRepeat(filter);
         this.textureView = RenderSystem.getDevice().createTextureView(this.texture);
         this.upload();
      }

      private void upload() {
         RenderSystem.getDevice().createCommandEncoder().writeToTexture(this.texture, this.pixels);
      }

      @Override
      public void close() {
         try {
            this.pixels.close();
         } catch (Throwable var2) {
         }

         super.close();
      }
   }
}
