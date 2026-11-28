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
   public static final Identifier PANORAMA_ID = Identifier.fromNamespaceAndPath("autismclient", "textures/gui/title/background/panorama");
   private static final Identifier BACKDROP_SOURCE = Identifier.fromNamespaceAndPath("autismclient", "textures/gui/title/background/panorama_overlay.png");
   private static final Identifier BACKDROP_ID = Identifier.fromNamespaceAndPath("autismclient", "dynamic/theme/panorama_overlay");
   private static final Map<Identifier, ThemeTextureRecolorer.RecolorEntry> recolorCache = new HashMap<>();
   private static final Map<Identifier, ThemeTextureRecolorer.TintEntry> tintCache = new HashMap<>();
   private static final Set<Identifier> tintsInFlight = new HashSet<>();
   private static final Set<Identifier> recolorsInFlight = new HashSet<>();
   private static final Map<Identifier, Integer> failedGenerations = new HashMap<>();
   private static final Map<Identifier, Identifier> themeTextureCache = new HashMap<>();
   private static int currentGeneration;
   private static ThemeTextureRecolorer.RecoloredPanorama preview;
   private static volatile boolean panoramaLoaded;
   private static AbstractTexture backdropTexture;
   private static boolean backdropRecolorStarted;

   private ThemeTextureRecolorer() {
   }

   public static Identifier recolored(Identifier source, ThemePaletteManager.ColorRole ch) {
      if (source == null) {
         return null;
      }

      ThemePaletteManager.CompiledPalette st = ThemePaletteManager.getPalette();
      if (!st.isModified(ch)) {
         return source;
      }

      ThemeTextureRecolorer.RecolorEntry cached = recolorCache.get(source);
      if (cached != null && cached.generation == currentGeneration) {
         return cached.id;
      }

      Integer failedGen = failedGenerations.get(source);
      if (failedGen != null && failedGen == currentGeneration) {
         return cached != null ? cached.id : source;
      }

      if (cached == null && !recolorsInFlight.contains(source)) {
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

      ThemeTextureRecolorer.TintEntry cached = tintCache.get(source);
      if (cached != null && cached.color == targetColor) {
         return cached.id;
      }

      scheduleTint(source, targetColor);
      return cached != null ? cached.id : source;
   }

   private static void scheduleTint(Identifier source, int targetColor) {
      if (tintsInFlight.add(source)) {
         byte[] png = readPng(source);
         if (png == null) {
            tintsInFlight.remove(source);
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
                  tintsInFlight.remove(source);
                  if (result != null) {
                     Identifier id = skyboxTextureId(source);
                     MINECRAFT.getTextureManager().register(id, new ThemeTextureRecolorer.PixelTexture(source.toString(), result, FilterMode.LINEAR));
                     tintCache.put(source, new ThemeTextureRecolorer.TintEntry(id, targetColor));
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
         MINECRAFT.getTextureManager().register(id, new ThemeTextureRecolorer.PixelTexture(source.toString(), recolored, FilterMode.LINEAR));
         recolorCache.put(source, new ThemeTextureRecolorer.RecolorEntry(id, generation));
         return id;
      } catch (Throwable t) {
         failedGenerations.put(source, generation);
         AutismClientAddon.LOG.warn("Theme recolor (sync) failed for {} (backing off this theme generation)", source, t);
         return null;
      }
   }

   private static void scheduleRecolor(Identifier source, ThemePaletteManager.ColorRole ch, ThemePaletteManager.CompiledPalette st) {
      if (recolorsInFlight.add(source)) {
         int generation = currentGeneration;
         byte[] png = readPng(source);
         if (png == null) {
            recolorsInFlight.remove(source);
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
                  recolorsInFlight.remove(source);
                  if (generation != currentGeneration) {
                     if (result != null) {
                        result.close();
                     }
                  } else if (result == null) {
                     failedGenerations.put(source, generation);
                  } else {
                     Identifier id = themeTextureId(source);
                     MINECRAFT.getTextureManager().register(id, new ThemeTextureRecolorer.PixelTexture(source.toString(), result, FilterMode.LINEAR));
                     recolorCache.put(source, new ThemeTextureRecolorer.RecolorEntry(id, generation));
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
         MINECRAFT.getTextureManager().register(id, new ThemeTextureRecolorer.PixelTexture(source.toString(), white, FilterMode.LINEAR));
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

   public static void register(TextureManager textureManager) {
      try {
         preview = new ThemeTextureRecolorer.RecoloredPanorama(PANORAMA_ID);
         textureManager.register(PANORAMA_ID, preview);
      } catch (Throwable t) {
         panoramaLoaded = false;
         AutismClientAddon.LOG.warn("Failed to register themed panorama", t);
      }
   }

   public static boolean isPanoramaLoaded() {
      return panoramaLoaded;
   }

   public static void recolorPanorama() {
      if (preview != null && panoramaLoaded) {
         int generation = currentGeneration;
         Runnable jobDone = autismclient.gui.ThemeApplyProgressOverlay.beginStep("Recoloring panorama");
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

      if (backdropTexture != null) {
         return BACKDROP_ID;
      }

      if (!backdropRecolorStarted) {
         backdropRecolorStarted = true;
         recolorBackdrop(st, () -> {});
      }

      return original;
   }

   private static void recolorBackdrop(ThemePaletteManager.CompiledPalette st, Runnable jobDone) {
      int generation = currentGeneration;

      byte[] png;
      try {
         Optional<Resource> res = MINECRAFT.getResourceManager().getResource(BACKDROP_SOURCE);
         if (res.isEmpty()) {
            backdropRecolorStarted = false;
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
                  backdropTexture = new ThemeTextureRecolorer.PixelTexture("panorama_overlay", result, FilterMode.LINEAR);
                  MINECRAFT.getTextureManager().register(BACKDROP_ID, backdropTexture);
                  return;
               }
            } finally {
               jobDone.run();
            }
         });
      });
   }

   public static void invalidate() {
      currentGeneration++;
      failedGenerations.clear();
      backdropRecolorStarted = false;
      backdropTexture = null;
      ThemePaletteManager.CompiledPalette st = ThemePaletteManager.getPalette();
      if (st.isModified(ThemePaletteManager.ColorRole.BACKDROP)) {
         backdropRecolorStarted = true;
         recolorBackdrop(st, autismclient.gui.ThemeApplyProgressOverlay.beginStep("Recoloring backdrop"));
      }

      recolorPanorama();
   }

   public static ThemeTextureRecolorer.RecolorPreview init(Identifier source, ThemePaletteManager.ColorRole channel, int maxDim) {
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
         ThemeTextureRecolorer.PixelTexture tex = new ThemeTextureRecolorer.PixelTexture("preview " + source, work, FilterMode.LINEAR);
         MINECRAFT.getTextureManager().register(id, tex);
         return new ThemeTextureRecolorer.RecolorPreview(channel, scaled, tex, id);
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

   private record RecolorEntry(Identifier id, int generation) {
   }

   public static final class RecolorPreview implements AutoCloseable {
      private final ThemePaletteManager.ColorRole channel;
      private final NativeImage source;
      private final ThemeTextureRecolorer.PixelTexture texture;
      private final Identifier id;
      private int lastSignature = -2147483648;
      private final Object lock = new Object();
      private boolean closed;
      private boolean workerRunning;
      private boolean freed;
      private ThemePaletteManager.CompiledPalette pendingPalette;

      private RecolorPreview(ThemePaletteManager.ColorRole channel, NativeImage source, ThemeTextureRecolorer.PixelTexture texture, Identifier id) {
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
               if (this.closed) {
                  return;
               }

               this.pendingPalette = st;
               if (this.workerRunning) {
                  return;
               }

               this.workerRunning = true;
            }

            BackgroundTaskExecutor.submit("theme-preview", this::runRecolorWorker);
         }
      }

      private void runRecolorWorker() {
         try {
            while (true) {
               ThemePaletteManager.CompiledPalette st;
               synchronized (this.lock) {
                  if (this.closed || this.pendingPalette == null) {
                     this.workerRunning = false;
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
                     if (!this.closed) {
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
               this.workerRunning = false;
            }

            AutismClientAddon.LOG.error("Theme preview recolor failed", t);
         }

         boolean freeNow;
         synchronized (this.lock) {
            freeNow = this.closed;
         }

         if (freeNow) {
            this.freeTextures();
         }
      }

      @Override
      public void close() {
         synchronized (this.lock) {
            this.closed = true;
            this.pendingPalette = null;
            if (this.workerRunning) {
               return;
            }
         }

         this.freeTextures();
      }

      private void freeTextures() {
         synchronized (this.lock) {
            if (this.freed) {
               return;
            }

            this.freed = true;
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

   private static final class RecoloredPanorama extends CubeMapTexture {
      private RecoloredPanorama(Identifier id) {
         super(id);
      }

      @Override
      public TextureContents loadContents(ResourceManager resourceManager) throws IOException {
         TextureContents contents = super.loadContents(resourceManager);
         ThemeTextureRecolorer.panoramaLoaded = true;
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

   private record TintEntry(Identifier id, int color) {
   }

   private static final class PixelTexture extends AbstractTexture {
      private final NativeImage pixels;

      private PixelTexture(String label, NativeImage pixels, FilterMode filter) {
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
