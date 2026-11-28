package autismclient.util.gif;

import autismclient.AutismClientAddon;
import autismclient.util.BackgroundTaskExecutor;
import autismclient.util.NativeImageMipmapBuilder;
import autismclient.util.bi;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.FilterMode;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TickableTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public final class GifTextureManager {
   private static final long init_J = 1000L;
   private static final double init_D = 1.0;
   private static final double load_D = 60.0;
   private static final long load_J = 150L;
   private static final long read_J = 2000L;
   private static final long write_J = 300000L;
   private static final long update_J = 536870912L;
   private static final long sync_J = 1000L;
   private static final int init_I = 64;
   private static final int load_I = 64;
   private static final int read_I = 3072;
   private static final long flush_J = 64000000L;
   private static final int write_I = 3072;
   private static final int update_I = 64;
   private static final int sync_I = 32;
   private static final int[] init_IArr = new int[]{1, 1, 10, 16};
   private static final int[] load_IArr = new int[]{12, 1, 10, 16};
   private static final int[] read_IArr = new int[]{0, 1, 1, 16};
   private static final int[] write_IArr = new int[]{11, 1, 1, 16};
   private static final int[] update_IArr = new int[]{1, 0, 10, 1};
   private static final int[] sync_IArr = new int[]{11, 0, 10, 1};
   private static final int flush_I = 2;
   private static final int close_I = 2;
   private static final int open_I = 10;
   private static final int poll = 20;
   private static final Map<String, GifTextureManager.Inner_StreamUtil> entriesByKey = new ConcurrentHashMap<>();
   private static final Map<String, GifTextureManager.Inner_StreamUtil> entriesBySlot = new ConcurrentHashMap<>();
   private static final Queue<GifTextureManager.Inner_CachePool> decodeJobs = new ConcurrentLinkedQueue<>();
   private static final AtomicBoolean workerRunning = new AtomicBoolean();
   private static final AtomicLong textureCounter = new AtomicLong();
   private static long nextPollMs;
   private static final Map<String, GifTextureManager.ConfigStore> resolvedPaths = new ConcurrentHashMap<>();
   private static final Map<String, int[]> imageDims = new ConcurrentHashMap<>();
   private static volatile long epoch;

   private GifTextureManager() {
   }

   public static Identifier texture(String path, double targetFps, double quality, String owner) {
      return textureFor(path, targetFps, false, quality, GifTextureManager.NetHelper.HUD, owner);
   }

   public static Identifier textureKeepAspect(String path, double targetFps, boolean keepAspect, double quality) {
      return textureFor(path, targetFps, keepAspect, quality, GifTextureManager.NetHelper.CAPE, null);
   }

   public static Identifier load(String path, double targetFps, boolean keepAspect, double quality) {
      return textureFor(path, targetFps, keepAspect, quality, GifTextureManager.NetHelper.SHIELD, null);
   }

   public static int[] naturalSize(String path) {
      GifTextureManager.ConfigStore resolved = resolvePath(path);
      if (resolved.path() == null) {
         return null;
      }

      int[] dims = imageDims.get(resolved.path().toString());
      return dims == null ? null : (int[])dims.clone();
   }

   public static void init() {
      epoch++;
      List<Identifier> ids = new ArrayList<>();

      for (GifTextureManager.Inner_StreamUtil entry : entriesByKey.values()) {
         ids.add(entry.textureId);
      }

      entriesByKey.clear();
      entriesBySlot.clear();
      decodeJobs.clear();
      resolvedPaths.clear();
      imageDims.clear();
      runOnRenderThread(() -> {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null) {
            for (Identifier id : ids) {
               try {
                  mc.getTextureManager().release(id);
               } catch (Throwable var5) {
               }
            }
         }
      });
   }

   private static Identifier textureFor(String raw, double targetFps, boolean keepAspect, double quality, GifTextureManager.NetHelper surface, String owner) {
      double fps = clampFps(targetFps);
      double clampedQuality = clampQuality(quality);
      GifTextureManager.ConfigStore resolved = resolvePath(raw);
      String source = resolved.path() != null ? resolved.path().toString() : String.valueOf(raw);
      String key = source + "|" + surface.init_Str + "|" + keepAspect + "|" + clampedQuality + (owner == null ? "" : "|" + owner);
      String slot = surface.init_Str + "|" + (owner == null ? source : "@" + owner);
      long now = System.currentTimeMillis();
      GifTextureManager.Inner_StreamUtil entry = entriesByKey.get(key);
      if (entry == null) {
         Identifier id = Identifier.fromNamespaceAndPath("autismclient", "dynamic/gif/" + sha1Hex(key) + "_" + textureCounter.incrementAndGet());
         GifTextureManager.Inner_StreamUtil created = new GifTextureManager.Inner_StreamUtil(key, slot, id, resolved.path(), surface, keepAspect, clampedQuality, fps);
         GifTextureManager.Inner_StreamUtil existing = entriesByKey.putIfAbsent(key, created);
         if (existing != null) {
            entry = existing;
         } else {
            entry = created;
            bindSlot(entry);
            if (resolved.error() != null) {
               markFailed(created, resolved.error());
            } else {
               queueDecode(created);
            }
         }
      } else if (entry.state != GifTextureManager.TaskQueue.LOADING && entry.path != null && now >= entry.nextCheckMs) {
         entry.nextCheckMs = now + 1000L;

         try {
            BasicFileAttributes attrs = Files.readAttributes(entry.path, BasicFileAttributes.class);
            long mtime = attrs.lastModifiedTime().toMillis();
            long size = attrs.size();
            if (mtime != entry.init_J || size != entry.load_J) {
               entry.init_J = mtime;
               entry.load_J = size;
               entry.state = GifTextureManager.TaskQueue.LOADING;
               clearTexture(entry);
               queueDecode(entry);
            }
         } catch (Exception var24) {
         }
      }

      if (entriesBySlot.get(slot) != entry) {
         bindSlot(entry);
      }

      markUsed(entry, fps, now);
      if (entry.state == GifTextureManager.TaskQueue.READY) {
         return entry.textureId;
      } else {
         GifTextureManager.Inner_StreamUtil fallback = entry.fallback;
         if (fallback != null && fallback.state == GifTextureManager.TaskQueue.READY) {
            markUsed(fallback, fps, now);
            return fallback.textureId;
         } else {
            return null;
         }
      }
   }

   private static void markUsed(GifTextureManager.Inner_StreamUtil entry, double fps, long now) {
      entry.lastUsedMs = now;
      entry.fps = fps;
      GifTextureManager.Inner_DataUtil texture = entry.texture;
      if (texture != null) {
         texture.targetFps = fps;
      }
   }

   private static void bindSlot(GifTextureManager.Inner_StreamUtil entry) {
      GifTextureManager.Inner_StreamUtil previous = entriesBySlot.put(entry.slot, entry);
      if (previous != null && previous != entry) {
         GifTextureManager.Inner_StreamUtil keep = previous.state == GifTextureManager.TaskQueue.READY ? previous : previous.fallback;
         previous.fallback = null;
         if (previous != keep) {
            releaseEntry(previous);
         }

         if (keep != null && keep != entry) {
            if (entry.state == GifTextureManager.TaskQueue.LOADING) {
               if (entry.fallback != null && entry.fallback != keep) {
                  releaseEntry(entry.fallback);
               }

               entry.fallback = keep;
            } else {
               releaseEntry(keep);
            }
         }
      }
   }

   private static long releaseEntry(GifTextureManager.Inner_StreamUtil entry) {
      if (entry.released) {
         return 0L;
      }

      entry.released = true;
      entriesByKey.remove(entry.key, entry);
      entriesBySlot.remove(entry.slot, entry);
      GifTextureManager.Inner_StreamUtil current = entriesBySlot.get(entry.slot);
      if (current != null && current.fallback == entry) {
         current.fallback = null;
      }

      entry.generation++;
      long freed = entry.textureBytes;
      clearTexture(entry);
      GifTextureManager.Inner_StreamUtil fallback = entry.fallback;
      entry.fallback = null;
      if (fallback != null) {
         freed += releaseEntry(fallback);
      }

      return freed;
   }

   private static void releaseFallback(GifTextureManager.Inner_StreamUtil entry) {
      GifTextureManager.Inner_StreamUtil fallback = entry.fallback;
      entry.fallback = null;
      if (fallback != null) {
         releaseEntry(fallback);
      }
   }

   private static void trimCache() {
      long now = System.currentTimeMillis();
      long total = 0L;
      List<GifTextureManager.Inner_StreamUtil> offScreen = new ArrayList<>();

      for (GifTextureManager.Inner_StreamUtil entry : entriesByKey.values()) {
         total += entry.textureBytes;
         if (now - entry.lastUsedMs > 2000L) {
            offScreen.add(entry);
         }
      }

      offScreen.sort(Comparator.comparingLong(entryx -> entryx.lastUsedMs));

      for (GifTextureManager.Inner_StreamUtil entry : offScreen) {
         if (now - entry.lastUsedMs <= 300000L && total <= 536870912L) {
            break;
         }

         total -= releaseEntry(entry);
      }
   }

   private static void pollFiles() {
      long now = System.currentTimeMillis();
      if (now >= nextPollMs) {
         nextPollMs = now + 1000L;
         Minecraft mc = Minecraft.getInstance();
         if (mc != null) {
            mc.schedule(GifTextureManager::trimCache);
         }
      }
   }

   private static void queueDecode(GifTextureManager.Inner_StreamUtil entry) {
      decodeJobs.add(new GifTextureManager.Inner_CachePool(entry, ++entry.generation, epoch, System.currentTimeMillis()));
      startWorker();
   }

   private static void startWorker() {
      if (workerRunning.compareAndSet(false, true)) {
         BackgroundTaskExecutor.submit("gif-load", () -> {
            GifTextureManager.Inner_CachePool job;
            try {
               while ((job = decodeJobs.poll()) != null) {
                  runDecodeJob(job);
               }
            } finally {
               workerRunning.set(false);
               if (!decodeJobs.isEmpty()) {
                  startWorker();
               }
            }
         });
      }
   }

   private static void runDecodeJob(GifTextureManager.Inner_CachePool job) {
      long waitMs = job.read() + 150L - System.currentTimeMillis();
      if (waitMs > 0L) {
         LockSupport.parkNanos(waitMs * 1000000L);
      }

      if (!job.isStale()) {
         GifTextureManager.BufferHelper payload = decode(job);
         if (payload != null && !runOnRenderThread(() -> applyDecoded(job, payload))) {
            payload.closeImages();
         }
      }
   }

   private static void applyDecoded(GifTextureManager.Inner_CachePool job, GifTextureManager.BufferHelper payload) {
      GifTextureManager.Inner_StreamUtil entry = job.entry();
      if (job.isStale()) {
         payload.closeImages();
      } else if (payload.mipChains() == null) {
         markFailed(entry, payload.error() != null ? payload.error() : "could not load image");
         entry.init_J = payload.init_J();
         entry.load_J = payload.load_J();
      } else {
         GifTextureManager.Inner_DataUtil texture = null;

         try {
            texture = new GifTextureManager.Inner_DataUtil(
               "autism " + entry.textureId.getPath(),
               payload.mipChains(),
               payload.delaysMs(),
               payload.averageDelayMs(),
               entry.fps,
               entry.surface == GifTextureManager.NetHelper.HUD
            );
            Minecraft.getInstance().getTextureManager().register(entry.textureId, texture);
         } catch (Throwable t) {
            if (texture != null) {
               texture.close();
            } else {
               payload.closeImages();
            }

            markFailed(entry, "texture upload failed");
            return;
         }

         entry.texture = texture;
         entry.textureBytes = texture.size;
         entry.state = GifTextureManager.TaskQueue.READY;
         entry.lastError = null;
         entry.init_J = payload.init_J();
         entry.load_J = payload.load_J();
         entry.nextCheckMs = System.currentTimeMillis() + 1000L;
         if (entry.path != null && payload.sourceWidth() > 0 && payload.read() > 0) {
            imageDims.put(entry.path.toString(), new int[]{payload.sourceWidth(), payload.read()});
         }

         releaseFallback(entry);
      }
   }

   private static void markFailed(GifTextureManager.Inner_StreamUtil entry, String reason) {
      boolean report = entry.state != GifTextureManager.TaskQueue.FAILED || !reason.equals(entry.lastError);
      entry.state = GifTextureManager.TaskQueue.FAILED;
      entry.lastError = reason;
      releaseFallback(entry);
      if (report) {
         bi.write("GIF: " + reason);
      }
   }

   private static void clearTexture(GifTextureManager.Inner_StreamUtil entry) {
      entry.texture = null;
      entry.textureBytes = 0L;
      runOnRenderThread(() -> {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null) {
            try {
               mc.getTextureManager().release(entry.textureId);
            } catch (Throwable var3) {
            }
         }
      });
   }

   private static boolean runOnRenderThread(Runnable task) {
      Minecraft mc = Minecraft.getInstance();
      if (mc == null) {
         return false;
      }

      if (RenderSystem.isOnRenderThread()) {
         task.run();
      } else {
         mc.execute(task);
      }

      return true;
   }

   private static GifTextureManager.BufferHelper decode(GifTextureManager.Inner_CachePool job) {
      GifTextureManager.Inner_StreamUtil entry = job.entry();
      BooleanSupplier stale = job::isStale;
      BasicFileAttributes attrs = null;

      try {
         attrs = Files.readAttributes(entry.path, BasicFileAttributes.class);
         byte[] bytes = Files.readAllBytes(entry.path);
         GifImageDecoder.Inner_DataUtil decoded = GifImageDecoder.decode(bytes);
         checkCancelled(stale);
         GifTextureManager.JsonUtil var10000;
         switch (entry.surface) {
            case CAPE:
               var10000 = init_CachePool_JsonUtil_DataUtil_DataUtil(decoded, entry.keepAspect, entry.quality, stale);
               break;
            case SHIELD:
               var10000 = buildShieldFrames(decoded, entry.keepAspect, entry.quality, stale);
               break;
            default:
               var10000 = applyQuality(decoded, entry.quality, stale);
         }

         GifTextureManager.JsonUtil transformed = var10000;
         int[][] argbFrames = transformed.argbFrames();
         int width = transformed.width();
         int height = transformed.load();
         boolean mipmap = entry.surface == GifTextureManager.NetHelper.HUD;
         NativeImage[][] chains = new NativeImage[argbFrames.length][];

         try {
            for (int i = 0; i < argbFrames.length; i++) {
               checkCancelled(stale);
               NativeImage top = toNativeImage(argbFrames[i], width, height);
               argbFrames[i] = null;
               chains[i] = mipmap ? NativeImageMipmapBuilder.init(top, 3072) : new NativeImage[]{top};
            }
         } catch (Throwable t) {
            closeChains(chains);
            throw t;
         }

         return new GifTextureManager.BufferHelper(
            attrs.lastModifiedTime().toMillis(), attrs.size(), chains, decoded.delays(), decoded.read(), decoded.width(), decoded.load(), null
         );
      } catch (CancellationException e) {
         return null;
      } catch (GifImageDecoder.Inner_CachePool e) {
         return GifTextureManager.BufferHelper.failure(e.getMessage(), attrs);
      } catch (Throwable t) {
         AutismClientAddon.LOG.warn("GIF load failed for {}: {}", entry.path, t.toString());
         return GifTextureManager.BufferHelper.failure("could not read image file", attrs);
      }
   }

   static int[] retimeDelays(int[] delaysMs, int averageDelayMs, double targetFps) {
      double clamped = clampFps(targetFps);
      int[] out = new int[delaysMs.length];
      if (clamped <= 0.0) {
         for (int i = 0; i < delaysMs.length; i++) {
            out[i] = Math.max(1, delaysMs[i]);
         }

         return out;
      } else {
         double warp = 1000.0 / Math.max(1, averageDelayMs) / clamped;

         for (int i = 0; i < delaysMs.length; i++) {
            out[i] = Math.max(1, (int)Math.round(delaysMs[i] * warp));
         }

         return out;
      }
   }

   private static GifTextureManager.JsonUtil init_CachePool_JsonUtil_DataUtil_DataUtil(
      GifImageDecoder.Inner_DataUtil decoded, boolean keepAspect, double quality, BooleanSupplier stale
   ) {
      int frames = decoded.frames().length;
      double faceScale = 64.0 / init_IArr[2];
      int width = Math.max(64, Math.min(3072, (int)Math.round(decoded.width() * quality * faceScale)));

      while (width > 64 && (long)width * (width / 2) * frames > 64000000L) {
         width /= 2;
      }

      int height = width / 2;
      double sx = width / 64.0;
      double sy = height / 32.0;
      int[][] out = new int[frames][];

      for (int i = 0; i < frames; i++) {
         checkCancelled(stale);
         BufferedImage canvas = new BufferedImage(width, height, 2);
         Graphics2D graphics = canvas.createGraphics();
         applyQualityHints(graphics);
         BufferedImage frame = toBufferedImage(decoded.frames()[i], decoded.width(), decoded.load());
         drawRegion(graphics, frame, decoded.width(), decoded.load(), init_IArr, sx, sy, keepAspect);
         drawRegion(graphics, frame, decoded.width(), decoded.load(), load_IArr, sx, sy, keepAspect);
         int w = decoded.width();
         int hh = decoded.load();
         drawRegion(graphics, frame, read_IArr, sx, sy, 0, 0, 1, hh);
         drawRegion(graphics, frame, write_IArr, sx, sy, w - 1, 0, w, hh);
         drawRegion(graphics, frame, update_IArr, sx, sy, 0, hh - 1, w, hh);
         drawRegion(graphics, frame, sync_IArr, sx, sy, 0, 0, w, 1);
         graphics.dispose();
         out[i] = canvas.getRGB(0, 0, width, height, null, 0, width);
      }

      return new GifTextureManager.JsonUtil(width, height, out);
   }

   private static GifTextureManager.JsonUtil applyQuality(GifImageDecoder.Inner_DataUtil decoded, double quality, BooleanSupplier stale) {
      if (quality >= 0.999) {
         return new GifTextureManager.JsonUtil(decoded.width(), decoded.load(), decoded.frames());
      }

      int width = Math.max(1, (int)Math.round(decoded.width() * quality));
      int height = Math.max(1, (int)Math.round(decoded.load() * quality));
      int[][] out = new int[decoded.frames().length][];

      for (int i = 0; i < out.length; i++) {
         checkCancelled(stale);
         BufferedImage canvas = new BufferedImage(width, height, 2);
         Graphics2D graphics = canvas.createGraphics();
         applyQualityHints(graphics);
         graphics.drawImage(toBufferedImage(decoded.frames()[i], decoded.width(), decoded.load()), 0, 0, width, height, null);
         graphics.dispose();
         out[i] = canvas.getRGB(0, 0, width, height, null, 0, width);
      }

      return new GifTextureManager.JsonUtil(width, height, out);
   }

   private static GifTextureManager.JsonUtil buildShieldFrames(GifImageDecoder.Inner_DataUtil decoded, boolean keepAspect, double quality, BooleanSupplier stale) {
      int frames = decoded.frames().length;
      double faceScale = 6.4;
      int target = (int)Math.round(Math.max(decoded.width(), decoded.load()) * quality * faceScale);
      int size = Math.max(64, Math.min(3072, target));

      while (size > 64 && (long)size * size * frames > 64000000L) {
         size /= 2;
      }

      int[][] out = new int[frames][];

      for (int i = 0; i < frames; i++) {
         checkCancelled(stale);
         out[i] = scaleFrame(decoded.frames()[i], decoded.width(), decoded.load(), keepAspect, size);
      }

      return new GifTextureManager.JsonUtil(size, size, out);
   }

   private static void checkCancelled(BooleanSupplier stale) {
      if (stale.getAsBoolean()) {
         throw new CancellationException();
      }
   }

   static int[] init_IArr_IArr(int[] frameArgb, int frameW, int frameH, boolean keepAspect) {
      return scaleFrame(frameArgb, frameW, frameH, keepAspect, 64);
   }

   static int[] scaleFrame(int[] frameArgb, int frameW, int frameH, boolean keepAspect, int outSize) {
      if (outSize < 64) {
         outSize = 64;
      }

      double scale = outSize / 64.0;
      BufferedImage overlay = new BufferedImage(outSize, outSize, 2);
      Graphics2D graphics = overlay.createGraphics();
      applyQualityHints(graphics);
      BufferedImage frame = toBufferedImage(frameArgb, frameW, frameH);
      drawFitted(graphics, frame, frameW, frameH, scaled(2, scale), scaled(2, scale), scaled(10, scale), scaled(20, scale), keepAspect);
      graphics.dispose();
      int[] out = new int[outSize * outSize];
      overlay.getRGB(0, 0, outSize, outSize, out, 0, outSize);
      return out;
   }

   private static void applyQualityHints(Graphics2D graphics) {
      graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
   }

   private static void drawRegion(Graphics2D graphics, BufferedImage frame, int srcW, int srcH, int[] region, double sx, double sy, boolean keepAspect) {
      drawFitted(
         graphics,
         frame,
         srcW,
         srcH,
         (int)Math.round(region[0] * sx),
         (int)Math.round(region[1] * sy),
         (int)Math.round(region[2] * sx),
         (int)Math.round(region[3] * sy),
         keepAspect
      );
   }

   private static void drawRegion(
      Graphics2D graphics, BufferedImage frame, int[] region, double sx, double sy, int srcX0, int srcY0, int srcX1, int srcY1
   ) {
      int dx1 = (int)Math.round(region[0] * sx);
      int dy1 = (int)Math.round(region[1] * sy);
      int dx2 = (int)Math.round((region[0] + region[2]) * sx);
      int dy2 = (int)Math.round((region[1] + region[3]) * sy);
      if (dx2 <= dx1) {
         dx2 = dx1 + 1;
      }

      if (dy2 <= dy1) {
         dy2 = dy1 + 1;
      }

      graphics.drawImage(frame, dx1, dy1, dx2, dy2, srcX0, srcY0, srcX1, srcY1, null);
   }

   private static int scaled(int coordinate, double scale) {
      return (int)Math.round(coordinate * scale);
   }

   private static void drawFitted(Graphics2D graphics, BufferedImage frame, int srcW, int srcH, int x, int y, int w, int h, boolean keepAspect) {
      if (keepAspect) {
         int[] rect = fitRect(srcW, srcH, w, h);
         graphics.drawImage(frame, x + rect[0], y + rect[1], rect[2], rect[3], null);
      } else {
         graphics.drawImage(frame, x, y, w, h, null);
      }
   }

   private static int[] fitRect(int srcW, int srcH, int dstW, int dstH) {
      double scale = Math.min((double)dstW / srcW, (double)dstH / srcH);
      int w = Math.max(1, (int)Math.round(srcW * scale));
      int h = Math.max(1, (int)Math.round(srcH * scale));
      return new int[]{(dstW - w) / 2, (dstH - h) / 2, w, h};
   }

   private static NativeImage toNativeImage(int[] argb, int width, int height) {
      NativeImage image = new NativeImage(width, height, true);

      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            image.setPixel(x, y, argb[y * width + x]);
         }
      }

      return image;
   }

   private static BufferedImage toBufferedImage(int[] argb, int width, int height) {
      BufferedImage image = new BufferedImage(width, height, 2);
      image.setRGB(0, 0, width, height, argb, 0, width);
      return image;
   }

   private static void closeChains(NativeImage[][] chains) {
      if (chains != null) {
         for (NativeImage[] chain : chains) {
            if (chain != null) {
               for (NativeImage image : chain) {
                  if (image != null && !image.isClosed()) {
                     image.close();
                  }
               }
            }
         }
      }
   }

   private static GifTextureManager.ConfigStore resolvePath(String raw) {
      if (raw == null) {
         return new GifTextureManager.ConfigStore(null, "no image path set");
      }

      GifTextureManager.ConfigStore cached = resolvedPaths.get(raw);
      if (cached != null) {
         return cached;
      }

      GifTextureManager.ConfigStore resolved = resolveSource(raw);
      resolvedPaths.put(raw, resolved);
      return resolved;
   }

   private static GifTextureManager.ConfigStore resolveSource(String raw) {
      if (raw.isBlank()) {
         return new GifTextureManager.ConfigStore(null, "no image path set");
      }

      if (!raw.contains("://") && !raw.regionMatches(true, 0, "jar:", 0, 4)) {
         try {
            Path path = Paths.get(raw);
            if (!path.isAbsolute()) {
               path = AutismClientAddon.FOLDER.toPath().resolve("gifs").resolve(raw);
            }

            String name = path.getFileName() == null ? "" : path.getFileName().toString().toLowerCase(Locale.ROOT);
            if (!name.endsWith(".gif") && !name.endsWith(".png") && !name.endsWith(".jpg") && !name.endsWith(".jpeg")) {
               return new GifTextureManager.ConfigStore(null, "unsupported file type (use .gif, .png or .jpg)");
            }

            if (!Files.exists(path)) {
               return new GifTextureManager.ConfigStore(null, "file not found: " + path.getFileName());
            }

            if (!Files.isRegularFile(path)) {
               return new GifTextureManager.ConfigStore(null, "not a file: " + path.getFileName());
            }

            try {
               path = path.toRealPath();
            } catch (Exception e) {
               path = path.toAbsolutePath().normalize();
            }

            return new GifTextureManager.ConfigStore(path, null);
         } catch (Exception e) {
            return new GifTextureManager.ConfigStore(null, "invalid image path");
         }
      } else {
         return new GifTextureManager.ConfigStore(null, "invalid image path");
      }
   }

   private static double clampFps(double fps) {
      return !Double.isNaN(fps) && !(fps <= 0.0) ? Math.max(1.0, Math.min(60.0, fps)) : 0.0;
   }

   private static double clampQuality(double quality) {
      return !Double.isNaN(quality) && !(quality <= 0.0) && !(quality > 1.0) ? quality : 1.0;
   }

   private static String sha1Hex(String key) {
      try {
         MessageDigest digest = MessageDigest.getInstance("SHA-1");
         byte[] hash = digest.digest(key.getBytes(StandardCharsets.UTF_8));
         StringBuilder out = new StringBuilder(hash.length * 2);

         for (byte b : hash) {
            out.append(Character.forDigit(b >> 4 & 15, 16)).append(Character.forDigit(b & 15, 16));
         }

         return out.toString();
      } catch (Exception e) {
         return Integer.toHexString(key.hashCode());
      }
   }

   private record BufferHelper(
      long init_J, long load_J, NativeImage[][] mipChains, int[] delaysMs, int averageDelayMs, int sourceWidth, int read, String error
   ) {
      static GifTextureManager.BufferHelper failure(String reason, BasicFileAttributes attrs) {
         long mtime = attrs == null ? -1L : attrs.lastModifiedTime().toMillis();
         long size = attrs == null ? -1L : attrs.size();
         return new GifTextureManager.BufferHelper(mtime, size, null, null, 1, 0, 0, reason);
      }

      void closeImages() {
         GifTextureManager.closeChains(this.mipChains);
      }
   }

   private record ConfigStore(Path path, String error) {
   }

   private record Inner_CachePool(GifTextureManager.Inner_StreamUtil entry, long generation, long load, long read) {
      boolean isStale() {
         return this.generation != this.entry.generation || this.load != GifTextureManager.epoch;
      }
   }

   private static final class Inner_DataUtil extends AbstractTexture implements TickableTexture {
      private final NativeImage[][] mipChains;
      private final int[] delaysMs;
      private final int averageDelayMs;
      final long size;
      volatile double targetFps;
      private double appliedFps;
      private int[] effectiveDelays;
      private long cycleMs;
      private int frameIndex;
      private long lastTickMs = -1L;
      private long cycleElapsedMs;
      private boolean init_Z;

      private Inner_DataUtil(String label, NativeImage[][] chains, int[] delaysMs, int averageDelayMs, double targetFps, boolean mipmap) {
         this.mipChains = chains;
         this.delaysMs = delaysMs;
         this.averageDelayMs = averageDelayMs;
         this.targetFps = targetFps;
         long size = 0L;

         for (NativeImage[] chain : chains) {
            for (NativeImage image : chain) {
               size += (long)image.getWidth() * image.getHeight() * 4L;
            }
         }

         this.size = size;
         this.retime();
         NativeImage top = chains[0][0];
         this.texture = RenderSystem.getDevice().createTexture(label, 5, GpuFormat.RGBA8_UNORM, top.getWidth(), top.getHeight(), 1, chains[0].length);
         this.sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR, mipmap);
         this.textureView = RenderSystem.getDevice().createTextureView(this.texture);
         this.uploadFrame(0);
      }

      private void retime() {
         this.appliedFps = this.targetFps;
         this.effectiveDelays = GifTextureManager.retimeDelays(this.delaysMs, this.averageDelayMs, this.appliedFps);
         long cycle = 0L;

         for (int delay : this.effectiveDelays) {
            cycle += delay;
         }

         this.cycleMs = Math.max(1L, cycle);
      }

      private void uploadFrame(int index) {
         for (int level = 0; level < this.mipChains[index].length; level++) {
            RenderSystem.getDevice().createCommandEncoder().writeToTexture(this.texture, this.mipChains[index][level], level, 0, 0, 0);
         }
      }

      @Override
      public void tick() {
         GifTextureManager.pollFiles();
         if (!this.init_Z && this.delaysMs.length >= 2) {
            try {
               this.advance();
            } catch (Throwable t) {
               this.init_Z = true;
               AutismClientAddon.LOG.warn("GIF animation stopped: {}", t.toString());
            }
         }
      }

      private void advance() {
         if (Double.compare(this.targetFps, this.appliedFps) != 0) {
            this.retime();
         }

         long now = Util.getMillis();
         if (this.lastTickMs < 0L) {
            this.lastTickMs = now;
         } else {
            long delta = now - this.lastTickMs;
            this.lastTickMs = now;
            if (delta > 0L) {
               this.cycleElapsedMs = (this.cycleElapsedMs + delta) % this.cycleMs;

               int next;
               for (next = this.frameIndex; this.cycleElapsedMs >= this.effectiveDelays[next]; next = (next + 1) % this.effectiveDelays.length) {
                  this.cycleElapsedMs = this.cycleElapsedMs - this.effectiveDelays[next];
               }

               if (next != this.frameIndex) {
                  this.frameIndex = next;
                  this.uploadFrame(next);
               }
            }
         }
      }

      @Override
      public void close() {
         this.releaseTextures();
         GifTextureManager.closeChains(this.mipChains);
      }
   }

   private static final class Inner_StreamUtil {
      final String key;
      final String slot;
      final Identifier textureId;
      final Path path;
      final GifTextureManager.NetHelper surface;
      final boolean keepAspect;
      final double quality;
      volatile double fps;
      volatile GifTextureManager.TaskQueue state = GifTextureManager.TaskQueue.LOADING;
      volatile String lastError;
      volatile long init_J = -1L;
      volatile long load_J = -1L;
      volatile long nextCheckMs;
      volatile long generation;
      volatile GifTextureManager.Inner_DataUtil texture;
      GifTextureManager.Inner_StreamUtil fallback;
      long lastUsedMs;
      long textureBytes;
      boolean released;

      Inner_StreamUtil(String key, String slot, Identifier id, Path file, GifTextureManager.NetHelper surface, boolean keepAspect, double quality, double fps) {
         this.key = key;
         this.slot = slot;
         this.textureId = id;
         this.path = file;
         this.surface = surface;
         this.keepAspect = keepAspect;
         this.quality = quality;
         this.fps = fps;
      }
   }

   private record JsonUtil(int width, int load, int[][] argbFrames) {
   }

   private enum NetHelper {
      HUD("hud"),
      CAPE("cape"),
      SHIELD("shield");

      final String init_Str;

      NetHelper(String tag) {
         this.init_Str = tag;
      }
   }

   private enum TaskQueue {
      LOADING,
      READY,
      FAILED;
   }
}
