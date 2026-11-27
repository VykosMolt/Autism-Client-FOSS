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
   private static final Map<String, GifTextureManager.Inner_StreamUtil> init_Map = new ConcurrentHashMap<>();
   private static final Map<String, GifTextureManager.Inner_StreamUtil> load_Map = new ConcurrentHashMap<>();
   private static final Queue<GifTextureManager.Inner_CachePool> init_Queue = new ConcurrentLinkedQueue<>();
   private static final AtomicBoolean init_AtomicBoolean = new AtomicBoolean();
   private static final AtomicLong init_AtomicLong = new AtomicLong();
   private static long close_J;
   private static final Map<String, GifTextureManager.ConfigStore> read_Map = new ConcurrentHashMap<>();
   private static final Map<String, int[]> write_Map = new ConcurrentHashMap<>();
   private static volatile long open_J;

   private GifTextureManager() {
   }

   public static Identifier texture(String path, double targetFps, double quality, String owner) {
      return init(path, targetFps, false, quality, GifTextureManager.NetHelper.HUD, owner);
   }

   public static Identifier textureKeepAspect(String path, double targetFps, boolean keepAspect, double quality) {
      return init(path, targetFps, keepAspect, quality, GifTextureManager.NetHelper.CAPE, null);
   }

   public static Identifier load(String path, double targetFps, boolean keepAspect, double quality) {
      return init(path, targetFps, keepAspect, quality, GifTextureManager.NetHelper.SHIELD, null);
   }

   public static int[] naturalSize(String path) {
      GifTextureManager.ConfigStore resolved = init_CachePool_ConfigSt_Str(path);
      if (resolved.init_Path() == null) {
         return null;
      }

      int[] dims = write_Map.get(resolved.init_Path().toString());
      return dims == null ? null : (int[])dims.clone();
   }

   public static void init() {
      open_J++;
      List<Identifier> ids = new ArrayList<>();

      for (GifTextureManager.Inner_StreamUtil entry : init_Map.values()) {
         ids.add(entry.init_Identifier);
      }

      init_Map.clear();
      load_Map.clear();
      init_Queue.clear();
      read_Map.clear();
      write_Map.clear();
      init_Z_Runnable(() -> {
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

   private static Identifier init(String raw, double targetFps, boolean keepAspect, double quality, GifTextureManager.NetHelper surface, String owner) {
      double fps = init_D_D(targetFps);
      double clampedQuality = load(quality);
      GifTextureManager.ConfigStore resolved = init_CachePool_ConfigSt_Str(raw);
      String source = resolved.init_Path() != null ? resolved.init_Path().toString() : String.valueOf(raw);
      String key = source + "|" + surface.init_Str + "|" + keepAspect + "|" + clampedQuality + (owner == null ? "" : "|" + owner);
      String slot = surface.init_Str + "|" + (owner == null ? source : "@" + owner);
      long now = System.currentTimeMillis();
      GifTextureManager.Inner_StreamUtil entry = init_Map.get(key);
      if (entry == null) {
         Identifier id = Identifier.fromNamespaceAndPath("autismclient", "dynamic/gif/" + init_Str_Str(key) + "_" + init_AtomicLong.incrementAndGet());
         GifTextureManager.Inner_StreamUtil created = new GifTextureManager.Inner_StreamUtil(key, slot, id, resolved.init_Path(), surface, keepAspect, clampedQuality, fps);
         GifTextureManager.Inner_StreamUtil existing = init_Map.putIfAbsent(key, created);
         if (existing != null) {
            entry = existing;
         } else {
            entry = created;
            init_V_CachePool_StreamUt(entry);
            if (resolved.init_Str() != null) {
               init(created, resolved.init_Str());
            } else {
               read(created);
            }
         }
      } else if (entry.init_CachePool_TaskQueu != GifTextureManager.TaskQueue.LOADING && entry.init_Path != null && now >= entry.read_J) {
         entry.read_J = now + 1000L;

         try {
            BasicFileAttributes attrs = Files.readAttributes(entry.init_Path, BasicFileAttributes.class);
            long mtime = attrs.lastModifiedTime().toMillis();
            long size = attrs.size();
            if (mtime != entry.init_J || size != entry.load_J) {
               entry.init_J = mtime;
               entry.load_J = size;
               entry.init_CachePool_TaskQueu = GifTextureManager.TaskQueue.LOADING;
               write(entry);
               read(entry);
            }
         } catch (Exception var24) {
         }
      }

      if (load_Map.get(slot) != entry) {
         init_V_CachePool_StreamUt(entry);
      }

      init_V_CachePool_StreamUt(entry, fps, now);
      if (entry.init_CachePool_TaskQueu == GifTextureManager.TaskQueue.READY) {
         return entry.init_Identifier;
      } else {
         GifTextureManager.Inner_StreamUtil fallback = entry.init_CachePool_StreamUt;
         if (fallback != null && fallback.init_CachePool_TaskQueu == GifTextureManager.TaskQueue.READY) {
            init_V_CachePool_StreamUt(fallback, fps, now);
            return fallback.init_Identifier;
         } else {
            return null;
         }
      }
   }

   private static void init_V_CachePool_StreamUt(GifTextureManager.Inner_StreamUtil entry, double fps, long now) {
      entry.update = now;
      entry.load_D = fps;
      GifTextureManager.Inner_DataUtil texture = entry.init_CachePool_DataUtil;
      if (texture != null) {
         texture.init_D = fps;
      }
   }

   private static void init_V_CachePool_StreamUt(GifTextureManager.Inner_StreamUtil entry) {
      GifTextureManager.Inner_StreamUtil previous = load_Map.put(entry.load_Str, entry);
      if (previous != null && previous != entry) {
         GifTextureManager.Inner_StreamUtil keep = previous.init_CachePool_TaskQueu == GifTextureManager.TaskQueue.READY ? previous : previous.init_CachePool_StreamUt;
         previous.init_CachePool_StreamUt = null;
         if (previous != keep) {
            init_J_CachePool_StreamUt(previous);
         }

         if (keep != null && keep != entry) {
            if (entry.init_CachePool_TaskQueu == GifTextureManager.TaskQueue.LOADING) {
               if (entry.init_CachePool_StreamUt != null && entry.init_CachePool_StreamUt != keep) {
                  init_J_CachePool_StreamUt(entry.init_CachePool_StreamUt);
               }

               entry.init_CachePool_StreamUt = keep;
            } else {
               init_J_CachePool_StreamUt(keep);
            }
         }
      }
   }

   private static long init_J_CachePool_StreamUt(GifTextureManager.Inner_StreamUtil entry) {
      if (entry.load_Z) {
         return 0L;
      }

      entry.load_Z = true;
      init_Map.remove(entry.init_Str, entry);
      load_Map.remove(entry.load_Str, entry);
      GifTextureManager.Inner_StreamUtil current = load_Map.get(entry.load_Str);
      if (current != null && current.init_CachePool_StreamUt == entry) {
         current.init_CachePool_StreamUt = null;
      }

      entry.write++;
      long freed = entry.sync;
      write(entry);
      GifTextureManager.Inner_StreamUtil fallback = entry.init_CachePool_StreamUt;
      entry.init_CachePool_StreamUt = null;
      if (fallback != null) {
         freed += init_J_CachePool_StreamUt(fallback);
      }

      return freed;
   }

   private static void load(GifTextureManager.Inner_StreamUtil entry) {
      GifTextureManager.Inner_StreamUtil fallback = entry.init_CachePool_StreamUt;
      entry.init_CachePool_StreamUt = null;
      if (fallback != null) {
         init_J_CachePool_StreamUt(fallback);
      }
   }

   private static void load() {
      long now = System.currentTimeMillis();
      long total = 0L;
      List<GifTextureManager.Inner_StreamUtil> offScreen = new ArrayList<>();

      for (GifTextureManager.Inner_StreamUtil entry : init_Map.values()) {
         total += entry.sync;
         if (now - entry.update > 2000L) {
            offScreen.add(entry);
         }
      }

      offScreen.sort(Comparator.comparingLong(entryx -> entryx.update));

      for (GifTextureManager.Inner_StreamUtil entry : offScreen) {
         if (now - entry.update <= 300000L && total <= 536870912L) {
            break;
         }

         total -= init_J_CachePool_StreamUt(entry);
      }
   }

   private static void read() {
      long now = System.currentTimeMillis();
      if (now >= close_J) {
         close_J = now + 1000L;
         Minecraft mc = Minecraft.getInstance();
         if (mc != null) {
            mc.schedule(GifTextureManager::load);
         }
      }
   }

   private static void read(GifTextureManager.Inner_StreamUtil entry) {
      init_Queue.add(new GifTextureManager.Inner_CachePool(entry, ++entry.write, open_J, System.currentTimeMillis()));
      write();
   }

   private static void write() {
      if (init_AtomicBoolean.compareAndSet(false, true)) {
         BackgroundTaskExecutor.submit("gif-load", () -> {
            GifTextureManager.Inner_CachePool job;
            try {
               while ((job = init_Queue.poll()) != null) {
                  init_V_CachePool_CachePoo(job);
               }
            } finally {
               init_AtomicBoolean.set(false);
               if (!init_Queue.isEmpty()) {
                  write();
               }
            }
         });
      }
   }

   private static void init_V_CachePool_CachePoo(GifTextureManager.Inner_CachePool job) {
      long waitMs = job.read() + 150L - System.currentTimeMillis();
      if (waitMs > 0L) {
         LockSupport.parkNanos(waitMs * 1000000L);
      }

      if (!job.init_Z()) {
         GifTextureManager.BufferHelper payload = init_CachePool_BufferHe_CachePool_CachePoo(job);
         if (payload != null && !init_Z_Runnable(() -> init(job, payload))) {
            payload.init_V();
         }
      }
   }

   private static void init(GifTextureManager.Inner_CachePool job, GifTextureManager.BufferHelper payload) {
      GifTextureManager.Inner_StreamUtil entry = job.init_CachePool_StreamUt();
      if (job.init_Z()) {
         payload.init_V();
      } else if (payload.init_NativeImageArr2() == null) {
         init(entry, payload.init_Str() != null ? payload.init_Str() : "could not load image");
         entry.init_J = payload.init_J();
         entry.load_J = payload.load_J();
      } else {
         GifTextureManager.Inner_DataUtil texture = null;

         try {
            texture = new GifTextureManager.Inner_DataUtil(
               "autism " + entry.init_Identifier.getPath(),
               payload.init_NativeImageArr2(),
               payload.init_IArr(),
               payload.init_I(),
               entry.load_D,
               entry.init_CachePool_NetHelpe == GifTextureManager.NetHelper.HUD
            );
            Minecraft.getInstance().getTextureManager().register(entry.init_Identifier, texture);
         } catch (Throwable t) {
            if (texture != null) {
               texture.close();
            } else {
               payload.init_V();
            }

            init(entry, "texture upload failed");
            return;
         }

         entry.init_CachePool_DataUtil = texture;
         entry.sync = texture.init_J;
         entry.init_CachePool_TaskQueu = GifTextureManager.TaskQueue.READY;
         entry.read_Str = null;
         entry.init_J = payload.init_J();
         entry.load_J = payload.load_J();
         entry.read_J = System.currentTimeMillis() + 1000L;
         if (entry.init_Path != null && payload.load_I() > 0 && payload.read() > 0) {
            write_Map.put(entry.init_Path.toString(), new int[]{payload.load_I(), payload.read()});
         }

         load(entry);
      }
   }

   private static void init(GifTextureManager.Inner_StreamUtil entry, String reason) {
      boolean report = entry.init_CachePool_TaskQueu != GifTextureManager.TaskQueue.FAILED || !reason.equals(entry.read_Str);
      entry.init_CachePool_TaskQueu = GifTextureManager.TaskQueue.FAILED;
      entry.read_Str = reason;
      load(entry);
      if (report) {
         bi.write("GIF: " + reason);
      }
   }

   private static void write(GifTextureManager.Inner_StreamUtil entry) {
      entry.init_CachePool_DataUtil = null;
      entry.sync = 0L;
      init_Z_Runnable(() -> {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null) {
            try {
               mc.getTextureManager().release(entry.init_Identifier);
            } catch (Throwable var3) {
            }
         }
      });
   }

   private static boolean init_Z_Runnable(Runnable task) {
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

   private static GifTextureManager.BufferHelper init_CachePool_BufferHe_CachePool_CachePoo(GifTextureManager.Inner_CachePool job) {
      GifTextureManager.Inner_StreamUtil entry = job.init_CachePool_StreamUt();
      BooleanSupplier stale = job::init_Z;
      BasicFileAttributes attrs = null;

      try {
         attrs = Files.readAttributes(entry.init_Path, BasicFileAttributes.class);
         byte[] bytes = Files.readAllBytes(entry.init_Path);
         GifImageDecoder.Inner_DataUtil decoded = GifImageDecoder.decode(bytes);
         init_V_BooleanSupplier(stale);
         GifTextureManager.JsonUtil var10000;
         switch (entry.init_CachePool_NetHelpe) {
            case CAPE:
               var10000 = init_CachePool_JsonUtil_DataUtil_DataUtil(decoded, entry.init_Z, entry.init_D, stale);
               break;
            case SHIELD:
               var10000 = load(decoded, entry.init_Z, entry.init_D, stale);
               break;
            default:
               var10000 = init_CachePool_JsonUtil_DataUtil_DataUtil(decoded, entry.init_D, stale);
         }

         GifTextureManager.JsonUtil transformed = var10000;
         int[][] argbFrames = transformed.init_IArr2();
         int width = transformed.init_I();
         int height = transformed.load();
         boolean mipmap = entry.init_CachePool_NetHelpe == GifTextureManager.NetHelper.HUD;
         NativeImage[][] chains = new NativeImage[argbFrames.length][];

         try {
            for (int i = 0; i < argbFrames.length; i++) {
               init_V_BooleanSupplier(stale);
               NativeImage top = init_NativeImage_IArr(argbFrames[i], width, height);
               argbFrames[i] = null;
               chains[i] = mipmap ? NativeImageMipmapBuilder.init(top, 3072) : new NativeImage[]{top};
            }
         } catch (Throwable t) {
            init_V_NativeImageArr2(chains);
            throw t;
         }

         return new GifTextureManager.BufferHelper(
            attrs.lastModifiedTime().toMillis(), attrs.size(), chains, decoded.delays(), decoded.read(), decoded.width(), decoded.load(), null
         );
      } catch (CancellationException e) {
         return null;
      } catch (GifImageDecoder.Inner_CachePool e) {
         return GifTextureManager.BufferHelper.init(e.getMessage(), attrs);
      } catch (Throwable t) {
         AutismClientAddon.LOG.warn("GIF load failed for {}: {}", entry.init_Path, t.toString());
         return GifTextureManager.BufferHelper.init("could not read image file", attrs);
      }
   }

   static int[] init_IArr_IArr(int[] delaysMs, int averageDelayMs, double targetFps) {
      double clamped = init_D_D(targetFps);
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
         init_V_BooleanSupplier(stale);
         BufferedImage canvas = new BufferedImage(width, height, 2);
         Graphics2D graphics = canvas.createGraphics();
         init_V_Graphics2D(graphics);
         BufferedImage frame = init_BufferedImage_IArr(decoded.frames()[i], decoded.width(), decoded.load());
         init(graphics, frame, decoded.width(), decoded.load(), init_IArr, sx, sy, keepAspect);
         init(graphics, frame, decoded.width(), decoded.load(), load_IArr, sx, sy, keepAspect);
         int w = decoded.width();
         int hh = decoded.load();
         init_V_Graphics2D_2(graphics, frame, read_IArr, sx, sy, 0, 0, 1, hh);
         init_V_Graphics2D_2(graphics, frame, write_IArr, sx, sy, w - 1, 0, w, hh);
         init_V_Graphics2D_2(graphics, frame, update_IArr, sx, sy, 0, hh - 1, w, hh);
         init_V_Graphics2D_2(graphics, frame, sync_IArr, sx, sy, 0, 0, w, 1);
         graphics.dispose();
         out[i] = canvas.getRGB(0, 0, width, height, null, 0, width);
      }

      return new GifTextureManager.JsonUtil(width, height, out);
   }

   private static GifTextureManager.JsonUtil init_CachePool_JsonUtil_DataUtil_DataUtil(GifImageDecoder.Inner_DataUtil decoded, double quality, BooleanSupplier stale) {
      if (quality >= 0.999) {
         return new GifTextureManager.JsonUtil(decoded.width(), decoded.load(), decoded.frames());
      }

      int width = Math.max(1, (int)Math.round(decoded.width() * quality));
      int height = Math.max(1, (int)Math.round(decoded.load() * quality));
      int[][] out = new int[decoded.frames().length][];

      for (int i = 0; i < out.length; i++) {
         init_V_BooleanSupplier(stale);
         BufferedImage canvas = new BufferedImage(width, height, 2);
         Graphics2D graphics = canvas.createGraphics();
         init_V_Graphics2D(graphics);
         graphics.drawImage(init_BufferedImage_IArr(decoded.frames()[i], decoded.width(), decoded.load()), 0, 0, width, height, null);
         graphics.dispose();
         out[i] = canvas.getRGB(0, 0, width, height, null, 0, width);
      }

      return new GifTextureManager.JsonUtil(width, height, out);
   }

   private static GifTextureManager.JsonUtil load(GifImageDecoder.Inner_DataUtil decoded, boolean keepAspect, double quality, BooleanSupplier stale) {
      int frames = decoded.frames().length;
      double faceScale = 6.4;
      int target = (int)Math.round(Math.max(decoded.width(), decoded.load()) * quality * faceScale);
      int size = Math.max(64, Math.min(3072, target));

      while (size > 64 && (long)size * size * frames > 64000000L) {
         size /= 2;
      }

      int[][] out = new int[frames][];

      for (int i = 0; i < frames; i++) {
         init_V_BooleanSupplier(stale);
         out[i] = init(decoded.frames()[i], decoded.width(), decoded.load(), keepAspect, size);
      }

      return new GifTextureManager.JsonUtil(size, size, out);
   }

   private static void init_V_BooleanSupplier(BooleanSupplier stale) {
      if (stale.getAsBoolean()) {
         throw new CancellationException();
      }
   }

   static int[] init_IArr_IArr(int[] frameArgb, int frameW, int frameH, boolean keepAspect) {
      return init(frameArgb, frameW, frameH, keepAspect, 64);
   }

   static int[] init(int[] frameArgb, int frameW, int frameH, boolean keepAspect, int outSize) {
      if (outSize < 64) {
         outSize = 64;
      }

      double scale = outSize / 64.0;
      BufferedImage overlay = new BufferedImage(outSize, outSize, 2);
      Graphics2D graphics = overlay.createGraphics();
      init_V_Graphics2D(graphics);
      BufferedImage frame = init_BufferedImage_IArr(frameArgb, frameW, frameH);
      init_V_Graphics2D(graphics, frame, frameW, frameH, init(2, scale), init(2, scale), init(10, scale), init(20, scale), keepAspect);
      graphics.dispose();
      int[] out = new int[outSize * outSize];
      overlay.getRGB(0, 0, outSize, outSize, out, 0, outSize);
      return out;
   }

   private static void init_V_Graphics2D(Graphics2D graphics) {
      graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
   }

   private static void init(Graphics2D graphics, BufferedImage frame, int srcW, int srcH, int[] region, double sx, double sy, boolean keepAspect) {
      init_V_Graphics2D(
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

   private static void init_V_Graphics2D_2(
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

   private static int init(int coordinate, double scale) {
      return (int)Math.round(coordinate * scale);
   }

   private static void init_V_Graphics2D(Graphics2D graphics, BufferedImage frame, int srcW, int srcH, int x, int y, int w, int h, boolean keepAspect) {
      if (keepAspect) {
         int[] rect = init_IArr_I(srcW, srcH, w, h);
         graphics.drawImage(frame, x + rect[0], y + rect[1], rect[2], rect[3], null);
      } else {
         graphics.drawImage(frame, x, y, w, h, null);
      }
   }

   private static int[] init_IArr_I(int srcW, int srcH, int dstW, int dstH) {
      double scale = Math.min((double)dstW / srcW, (double)dstH / srcH);
      int w = Math.max(1, (int)Math.round(srcW * scale));
      int h = Math.max(1, (int)Math.round(srcH * scale));
      return new int[]{(dstW - w) / 2, (dstH - h) / 2, w, h};
   }

   private static NativeImage init_NativeImage_IArr(int[] argb, int width, int height) {
      NativeImage image = new NativeImage(width, height, true);

      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            image.setPixel(x, y, argb[y * width + x]);
         }
      }

      return image;
   }

   private static BufferedImage init_BufferedImage_IArr(int[] argb, int width, int height) {
      BufferedImage image = new BufferedImage(width, height, 2);
      image.setRGB(0, 0, width, height, argb, 0, width);
      return image;
   }

   private static void init_V_NativeImageArr2(NativeImage[][] chains) {
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

   private static GifTextureManager.ConfigStore init_CachePool_ConfigSt_Str(String raw) {
      if (raw == null) {
         return new GifTextureManager.ConfigStore(null, "no image path set");
      }

      GifTextureManager.ConfigStore cached = read_Map.get(raw);
      if (cached != null) {
         return cached;
      }

      GifTextureManager.ConfigStore resolved = load(raw);
      read_Map.put(raw, resolved);
      return resolved;
   }

   private static GifTextureManager.ConfigStore load(String raw) {
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

   private static double init_D_D(double fps) {
      return !Double.isNaN(fps) && !(fps <= 0.0) ? Math.max(1.0, Math.min(60.0, fps)) : 0.0;
   }

   private static double load(double quality) {
      return !Double.isNaN(quality) && !(quality <= 0.0) && !(quality > 1.0) ? quality : 1.0;
   }

   private static String init_Str_Str(String key) {
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
      long init_J, long load_J, NativeImage[][] init_NativeImageArr2, int[] init_IArr, int init_I, int load_I, int read, String init_Str
   ) {
      static GifTextureManager.BufferHelper init(String reason, BasicFileAttributes attrs) {
         long mtime = attrs == null ? -1L : attrs.lastModifiedTime().toMillis();
         long size = attrs == null ? -1L : attrs.size();
         return new GifTextureManager.BufferHelper(mtime, size, null, null, 1, 0, 0, reason);
      }

      void init_V() {
         GifTextureManager.init_V_NativeImageArr2(this.init_NativeImageArr2);
      }
   }

   private record ConfigStore(Path init_Path, String init_Str) {
   }

   private record Inner_CachePool(GifTextureManager.Inner_StreamUtil init_CachePool_StreamUt, long init_J, long load, long read) {
      boolean init_Z() {
         return this.init_J != this.init_CachePool_StreamUt.write || this.load != GifTextureManager.open_J;
      }
   }

   private static final class Inner_DataUtil extends AbstractTexture implements TickableTexture {
      private final NativeImage[][] init_NativeImageArr2;
      private final int[] init_IArr;
      private final int init_I;
      final long init_J;
      volatile double init_D;
      private double load_D;
      private int[] load_IArr;
      private long load_J;
      private int load_I;
      private long read = -1L;
      private long write;
      private boolean init_Z;

      private Inner_DataUtil(String label, NativeImage[][] chains, int[] delaysMs, int averageDelayMs, double targetFps, boolean mipmap) {
         this.init_NativeImageArr2 = chains;
         this.init_IArr = delaysMs;
         this.init_I = averageDelayMs;
         this.init_D = targetFps;
         long size = 0L;

         for (NativeImage[] chain : chains) {
            for (NativeImage image : chain) {
               size += (long)image.getWidth() * image.getHeight() * 4L;
            }
         }

         this.init_J = size;
         this.init();
         NativeImage top = chains[0][0];
         this.texture = RenderSystem.getDevice().createTexture(label, 5, GpuFormat.RGBA8_UNORM, top.getWidth(), top.getHeight(), 1, chains[0].length);
         this.sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR, mipmap);
         this.textureView = RenderSystem.getDevice().createTextureView(this.texture);
         this.init(0);
      }

      private void init() {
         this.load_D = this.init_D;
         this.load_IArr = GifTextureManager.init_IArr_IArr(this.init_IArr, this.init_I, this.load_D);
         long cycle = 0L;

         for (int delay : this.load_IArr) {
            cycle += delay;
         }

         this.load_J = Math.max(1L, cycle);
      }

      private void init(int index) {
         for (int level = 0; level < this.init_NativeImageArr2[index].length; level++) {
            RenderSystem.getDevice().createCommandEncoder().writeToTexture(this.texture, this.init_NativeImageArr2[index][level], level, 0, 0, 0);
         }
      }

      @Override
      public void tick() {
         GifTextureManager.read();
         if (!this.init_Z && this.init_IArr.length >= 2) {
            try {
               this.load();
            } catch (Throwable t) {
               this.init_Z = true;
               AutismClientAddon.LOG.warn("GIF animation stopped: {}", t.toString());
            }
         }
      }

      private void load() {
         if (Double.compare(this.init_D, this.load_D) != 0) {
            this.init();
         }

         long now = Util.getMillis();
         if (this.read < 0L) {
            this.read = now;
         } else {
            long delta = now - this.read;
            this.read = now;
            if (delta > 0L) {
               this.write = (this.write + delta) % this.load_J;

               int next;
               for (next = this.load_I; this.write >= this.load_IArr[next]; next = (next + 1) % this.load_IArr.length) {
                  this.write = this.write - this.load_IArr[next];
               }

               if (next != this.load_I) {
                  this.load_I = next;
                  this.init(next);
               }
            }
         }
      }

      @Override
      public void close() {
         this.releaseTextures();
         GifTextureManager.init_V_NativeImageArr2(this.init_NativeImageArr2);
      }
   }

   private static final class Inner_StreamUtil {
      final String init_Str;
      final String load_Str;
      final Identifier init_Identifier;
      final Path init_Path;
      final GifTextureManager.NetHelper init_CachePool_NetHelpe;
      final boolean init_Z;
      final double init_D;
      volatile double load_D;
      volatile GifTextureManager.TaskQueue init_CachePool_TaskQueu = GifTextureManager.TaskQueue.LOADING;
      volatile String read_Str;
      volatile long init_J = -1L;
      volatile long load_J = -1L;
      volatile long read_J;
      volatile long write;
      volatile GifTextureManager.Inner_DataUtil init_CachePool_DataUtil;
      GifTextureManager.Inner_StreamUtil init_CachePool_StreamUt;
      long update;
      long sync;
      boolean load_Z;

      Inner_StreamUtil(String key, String slot, Identifier id, Path file, GifTextureManager.NetHelper surface, boolean keepAspect, double quality, double fps) {
         this.init_Str = key;
         this.load_Str = slot;
         this.init_Identifier = id;
         this.init_Path = file;
         this.init_CachePool_NetHelpe = surface;
         this.init_Z = keepAspect;
         this.init_D = quality;
         this.load_D = fps;
      }
   }

   private record JsonUtil(int init_I, int load, int[][] init_IArr2) {
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
