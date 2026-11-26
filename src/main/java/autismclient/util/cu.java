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

public final class cu {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   public static final Identifier init_Identifier = Identifier.fromNamespaceAndPath("autismclient", "textures/gui/title/background/panorama");
   private static final Identifier load_Identifier = Identifier.fromNamespaceAndPath("autismclient", "textures/gui/title/background/panorama_overlay.png");
   private static final Identifier read_Identifier = Identifier.fromNamespaceAndPath("autismclient", "dynamic/theme/panorama_overlay");
   private static final Map<Identifier, cu.Inner_BufferHelper> init_Map = new HashMap<>();
   private static final Map<Identifier, cu.Inner_DataUtil> load_Map = new HashMap<>();
   private static final Set<Identifier> init_Set = new HashSet<>();
   private static final Set<Identifier> load_Set = new HashSet<>();
   private static final Map<Identifier, Integer> read_Map = new HashMap<>();
   private static final Map<Identifier, Identifier> write = new HashMap<>();
   private static int init_I;
   private static cu.Inner_ConfigStore init_cu_ConfigStore;
   private static volatile boolean init_Z;
   private static AbstractTexture init_AbstractTexture;
   private static boolean load_Z;

   private cu() {
   }

   public static Identifier init(Identifier source, ct.Inner_DataUtil ch) {
      if (source == null) {
         return null;
      }

      ct.Inner_StreamUtil st = ct.init_ct_StreamUtil();
      if (!st.init_Z_ct_DataUtil(ch)) {
         return source;
      }

      cu.Inner_BufferHelper cached = init_Map.get(source);
      if (cached != null && cached.init_I == init_I) {
         return cached.init_Identifier;
      }

      Integer failedGen = read_Map.get(source);
      if (failedGen != null && failedGen == init_I) {
         return cached != null ? cached.init_Identifier : source;
      }

      if (cached == null && !load_Set.contains(source)) {
         Identifier built = init_Identifier(source, ch, st);
         if (built != null) {
            return built;
         }
      }

      init_V(source, ch, st);
      return cached != null ? cached.init_Identifier : source;
   }

   public static Identifier init_Identifier(Identifier source, int targetColor) {
      if (source == null) {
         return null;
      }

      cu.Inner_DataUtil cached = load_Map.get(source);
      if (cached != null && cached.init_I == targetColor) {
         return cached.init_Identifier;
      }

      init_V(source, targetColor);
      return cached != null ? cached.init_Identifier : source;
   }

   private static void init_V(Identifier source, int targetColor) {
      if (init_Set.add(source)) {
         byte[] png = init_BArr(source);
         if (png == null) {
            init_Set.remove(source);
         } else {
            float[] hsb = Color.RGBtoHSB(targetColor >> 16 & 0xFF, targetColor >> 8 & 0xFF, targetColor & 0xFF, null);
            float targetHue = hsb[0];
            float targetSat = hsb[1];
            ClassCache.init_Future_Str_Runnable("skybox-recolor", () -> {
               NativeImage recolored = null;

               try (NativeImage src = NativeImage.read(png)) {
                  recolored = src.mappedCopy(argb -> ct.init_I_I(argb, targetHue, targetSat));
               } catch (Throwable t) {
                  AutismClientAddon.LOG.warn("Skybox recolor failed for {}", source, t);
               }

               NativeImage result = recolored;
               init_Minecraft.execute(() -> {
                  init_Set.remove(source);
                  if (result != null) {
                     Identifier id = read(source);
                     init_Minecraft.getTextureManager().register(id, new cu.Inner_StreamUtil(source.toString(), result, FilterMode.LINEAR));
                     load_Map.put(source, new cu.Inner_DataUtil(id, targetColor));
                  }
               });
            });
         }
      }
   }

   private static Identifier read(Identifier source) {
      return Identifier.fromNamespaceAndPath(
         "autismclient", "dynamic/skybox/" + source.getNamespace() + "/" + source.getPath().replace('/', '_').replace(".png", "")
      );
   }

   private static Identifier init_Identifier(Identifier source, ct.Inner_DataUtil ch, ct.Inner_StreamUtil st) {
      byte[] png = init_BArr(source);
      if (png == null) {
         return null;
      }

      int generation = init_I;

      try (NativeImage src = NativeImage.read(png)) {
         NativeImage recolored = src.mappedCopy(argb -> ct.load(argb, ch, st));
         Identifier id = write(source);
         init_Minecraft.getTextureManager().register(id, new cu.Inner_StreamUtil(source.toString(), recolored, FilterMode.LINEAR));
         init_Map.put(source, new cu.Inner_BufferHelper(id, generation));
         return id;
      } catch (Throwable t) {
         read_Map.put(source, generation);
         AutismClientAddon.LOG.warn("Theme recolor (sync) failed for {} (backing off this theme generation)", source, t);
         return null;
      }
   }

   private static void init_V(Identifier source, ct.Inner_DataUtil ch, ct.Inner_StreamUtil st) {
      if (load_Set.add(source)) {
         int generation = init_I;
         byte[] png = init_BArr(source);
         if (png == null) {
            load_Set.remove(source);
         } else {
            ClassCache.init_Future_Str_Runnable("theme-recolor", () -> {
               NativeImage recolored = null;

               try (NativeImage src = NativeImage.read(png)) {
                  recolored = src.mappedCopy(argb -> ct.load(argb, ch, st));
               } catch (Throwable t) {
                  AutismClientAddon.LOG.warn("Theme recolor failed for {} (backing off this theme generation)", source, t);
               }

               NativeImage result = recolored;
               init_Minecraft.execute(() -> {
                  load_Set.remove(source);
                  if (generation != init_I) {
                     if (result != null) {
                        result.close();
                     }
                  } else if (result == null) {
                     read_Map.put(source, generation);
                  } else {
                     Identifier id = write(source);
                     init_Minecraft.getTextureManager().register(id, new cu.Inner_StreamUtil(source.toString(), result, FilterMode.LINEAR));
                     init_Map.put(source, new cu.Inner_BufferHelper(id, generation));
                  }
               });
            });
         }
      }
   }

   private static byte[] init_BArr(Identifier source) {
      try {
         Optional<Resource> res = init_Minecraft.getResourceManager().getResource(source);
         if (res.isEmpty()) {
            return null;
         }

         try (InputStream in = res.get().open()) {
            return in.readAllBytes();
         }
      } catch (Throwable t) {
         read_Map.put(source, init_I);
         AutismClientAddon.LOG.warn("Theme recolor failed for {} (backing off this theme generation)", source, t);
         return null;
      }
   }

   public static Identifier init_Identifier(Identifier source) {
      if (source == null) {
         return null;
      }

      Identifier cached = write.get(source);
      if (cached != null) {
         return cached;
      }

      try {
         TextureContents contents = TextureContents.load(init_Minecraft.getResourceManager(), source);
         NativeImage src = contents.image();
         NativeImage white = src.mappedCopy(argb -> argb & 0xFF000000 | 16777215);
         src.close();
         Identifier id = Identifier.fromNamespaceAndPath(
            "autismclient", "dynamic/white/" + source.getNamespace() + "/" + source.getPath().replace('/', '_').replace(".png", "")
         );
         init_Minecraft.getTextureManager().register(id, new cu.Inner_StreamUtil(source.toString(), white, FilterMode.LINEAR));
         write.put(source, id);
         return id;
      } catch (Throwable t) {
         AutismClientAddon.LOG.warn("Theme whiten failed for {}", source, t);
         return source;
      }
   }

   private static Identifier write(Identifier source) {
      return Identifier.fromNamespaceAndPath(
         "autismclient", "dynamic/theme/" + source.getNamespace() + "/" + source.getPath().replace('/', '_').replace(".png", "")
      );
   }

   public static void init(TextureManager textureManager) {
      try {
         init_cu_ConfigStore = new cu.Inner_ConfigStore(init_Identifier);
         textureManager.register(init_Identifier, init_cu_ConfigStore);
      } catch (Throwable t) {
         init_Z = false;
         AutismClientAddon.LOG.warn("Failed to register themed panorama", t);
      }
   }

   public static boolean init_Z() {
      return init_Z;
   }

   public static void init_V() {
      if (init_cu_ConfigStore != null && init_Z) {
         int generation = init_I;
         Runnable jobDone = autismclient.gui.CachePool.init("Recoloring panorama");
         init_Minecraft.execute(() -> {
            try {
               if (generation != init_I) {
                  return;
               }

               TextureContents contents = init_cu_ConfigStore.loadContents(init_Minecraft.getResourceManager());

               try {
                  init_cu_ConfigStore.apply(contents);
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
      ct.Inner_StreamUtil st = ct.init_ct_StreamUtil();
      if (!st.init_Z_ct_DataUtil(ct.Inner_DataUtil.BACKDROP)) {
         return original;
      }

      if (init_AbstractTexture != null) {
         return read_Identifier;
      }

      if (!load_Z) {
         load_Z = true;
         init(st, () -> {});
      }

      return original;
   }

   private static void init(ct.Inner_StreamUtil st, Runnable jobDone) {
      int generation = init_I;

      byte[] png;
      try {
         Optional<Resource> res = init_Minecraft.getResourceManager().getResource(load_Identifier);
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

      ClassCache.init_Future_Str_Runnable("theme-overlay", () -> {
         NativeImage recolored = null;

         try (NativeImage src = NativeImage.read(png)) {
            recolored = src.mappedCopy(argb -> ct.load(argb, ct.Inner_DataUtil.BACKDROP, st));
         } catch (Throwable tx) {
            AutismClientAddon.LOG.warn("Failed to build themed panorama overlay", tx);
         }

         NativeImage result = recolored;
         init_Minecraft.execute(() -> {
            try {
               if (generation != init_I) {
                  if (result != null) {
                     result.close();
                  }

                  return;
               }

               if (result != null) {
                  init_AbstractTexture = new cu.Inner_StreamUtil("panorama_overlay", result, FilterMode.LINEAR);
                  init_Minecraft.getTextureManager().register(read_Identifier, init_AbstractTexture);
                  return;
               }
            } finally {
               jobDone.run();
            }
         });
      });
   }

   public static void load() {
      init_I++;
      read_Map.clear();
      load_Z = false;
      init_AbstractTexture = null;
      ct.Inner_StreamUtil st = ct.init_ct_StreamUtil();
      if (st.init_Z_ct_DataUtil(ct.Inner_DataUtil.BACKDROP)) {
         load_Z = true;
         init(st, autismclient.gui.CachePool.init("Recoloring backdrop"));
      }

      init_V();
   }

   public static cu.Inner_CachePool init(Identifier source, ct.Inner_DataUtil channel, int maxDim) {
      try {
         TextureContents contents = TextureContents.load(init_Minecraft.getResourceManager(), source);
         NativeImage full = contents.image();
         NativeImage scaled = init(full, maxDim);
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
         cu.Inner_StreamUtil tex = new cu.Inner_StreamUtil("preview " + source, work, FilterMode.LINEAR);
         init_Minecraft.getTextureManager().register(id, tex);
         return new cu.Inner_CachePool(channel, scaled, tex, id);
      } catch (Throwable t) {
         AutismClientAddon.LOG.warn("Failed to build theme preview for {}", source, t);
         return null;
      }
   }

   private static NativeImage init(NativeImage src, int maxDim) {
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

   private record Inner_BufferHelper(Identifier init_Identifier, int init_I) {
   }

   public static final class Inner_CachePool implements AutoCloseable {
      private final ct.Inner_DataUtil init_ct_DataUtil;
      private final NativeImage init_NativeImage;
      private final cu.Inner_StreamUtil init_cu_StreamUtil;
      private final Identifier init_Identifier;
      private int init_I = -2147483648;
      private final Object init_Obj = new Object();
      private boolean init_Z;
      private boolean load;
      private boolean read;
      private ct.Inner_StreamUtil init_ct_StreamUtil;

      private Inner_CachePool(ct.Inner_DataUtil channel, NativeImage source, cu.Inner_StreamUtil texture, Identifier id) {
         this.init_ct_DataUtil = channel;
         this.init_NativeImage = source;
         this.init_cu_StreamUtil = texture;
         this.init_Identifier = id;
      }

      public Identifier init_Identifier() {
         return this.init_Identifier;
      }

      public int init_I() {
         return this.init_NativeImage.getWidth();
      }

      public int load_I() {
         return this.init_NativeImage.getHeight();
      }

      private static int init_I_ct_StreamUtil(ct.Inner_StreamUtil st) {
         int sig = Float.floatToIntBits(st.init_F_ct_DataUtil(ct.Inner_DataUtil.BACKDROP)) * 31 + Float.floatToIntBits(st.load_F(ct.Inner_DataUtil.BACKDROP));
         return sig == 0 ? 1 : sig;
      }

      public void init_V_ct_StreamUtil(ct.Inner_StreamUtil st) {
         int sig = this.init_ct_DataUtil == ct.Inner_DataUtil.BACKDROP ? init_I_ct_StreamUtil(st) : st.load_I(this.init_ct_DataUtil);
         if (sig != this.init_I) {
            this.init_I = sig;
            synchronized (this.init_Obj) {
               if (this.init_Z) {
                  return;
               }

               this.init_ct_StreamUtil = st;
               if (this.load) {
                  return;
               }

               this.load = true;
            }

            ClassCache.init_Future_Str_Runnable("theme-preview", this::init_V);
         }
      }

      private void init_V() {
         try {
            while (true) {
               ct.Inner_StreamUtil st;
               synchronized (this.init_Obj) {
                  if (this.init_Z || this.init_ct_StreamUtil == null) {
                     this.load = false;
                     break;
                  }

                  st = this.init_ct_StreamUtil;
                  this.init_ct_StreamUtil = null;
               }

               int w = this.init_NativeImage.getWidth();
               int h = this.init_NativeImage.getHeight();
               int[] out = new int[w * h];

               for (int y = 0; y < h; y++) {
                  for (int x = 0; x < w; x++) {
                     out[y * w + x] = this.init_ct_DataUtil == ct.Inner_DataUtil.BACKDROP
                        ? ct.init_I_I(
                           this.init_NativeImage.getPixel(x, y), st.init_F_ct_DataUtil(ct.Inner_DataUtil.BACKDROP), st.load_F(ct.Inner_DataUtil.BACKDROP)
                        )
                        : ct.load(this.init_NativeImage.getPixel(x, y), this.init_ct_DataUtil, st);
                  }
               }

               cu.init_Minecraft.execute(() -> {
                  synchronized (this.init_Obj) {
                     if (!this.init_Z) {
                        for (int yx = 0; yx < h; yx++) {
                           for (int xx = 0; xx < w; xx++) {
                              this.init_cu_StreamUtil.init.setPixel(xx, yx, out[yx * w + xx]);
                           }
                        }

                        this.init_cu_StreamUtil.init();
                     }
                  }
               });
            }
         } catch (Throwable t) {
            synchronized (this.init_Obj) {
               this.load = false;
            }

            AutismClientAddon.LOG.error("Theme preview recolor failed", t);
         }

         boolean freeNow;
         synchronized (this.init_Obj) {
            freeNow = this.init_Z;
         }

         if (freeNow) {
            this.load_V();
         }
      }

      @Override
      public void close() {
         synchronized (this.init_Obj) {
            this.init_Z = true;
            this.init_ct_StreamUtil = null;
            if (this.load) {
               return;
            }
         }

         this.load_V();
      }

      private void load_V() {
         synchronized (this.init_Obj) {
            if (this.read) {
               return;
            }

            this.read = true;
         }

         try {
            this.init_NativeImage.close();
         } catch (Throwable var4) {
         }

         try {
            this.init_cu_StreamUtil.close();
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
         cu.init_Z = true;
         ct.Inner_StreamUtil st = ct.init_ct_StreamUtil();
         float hue = st.init_F_ct_DataUtil(ct.Inner_DataUtil.BACKDROP);
         float sat = st.load_F(ct.Inner_DataUtil.BACKDROP);
         NativeImage img = contents.image();
         int changed = 0;

         for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
               int before = img.getPixel(x, y);
               int after = ct.init_I_I(before, hue, sat);
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

   private record Inner_DataUtil(Identifier init_Identifier, int init_I) {
   }

   private static final class Inner_StreamUtil extends AbstractTexture {
      private final NativeImage init;

      private Inner_StreamUtil(String label, NativeImage pixels, FilterMode filter) {
         this.init = pixels;
         this.texture = RenderSystem.getDevice().createTexture(label, 5, GpuFormat.RGBA8_UNORM, pixels.getWidth(), pixels.getHeight(), 1, 1);
         this.sampler = RenderSystem.getSamplerCache().getRepeat(filter);
         this.textureView = RenderSystem.getDevice().createTextureView(this.texture);
         this.init();
      }

      private void init() {
         RenderSystem.getDevice().createCommandEncoder().writeToTexture(this.texture, this.init);
      }

      @Override
      public void close() {
         try {
            this.init.close();
         } catch (Throwable var2) {
         }

         super.close();
      }
   }
}
