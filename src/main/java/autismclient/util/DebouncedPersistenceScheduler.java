package autismclient.util;

import autismclient.AutismClientAddon;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;

final class DebouncedPersistenceScheduler {
   static final long init_J = 300L;
   private static final Object init_Obj = new Object();
   private static final Map<String, Runnable> init_Map = new HashMap<>();
   private static final Map<String, Boolean> load_Map = new HashMap<>();
   private static final Map<String, ScheduledFuture<?>> read = new HashMap<>();
   private static final Map<String, Long> write = new HashMap<>();
   private static final ScheduledExecutorService init_ScheduledExecutorS = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread thread = new Thread(r, "autism-persistence");
      thread.setDaemon(true);
      return thread;
   });
   private static AutismConfig init_AutismConfig;
   private static long load_J;
   private static final String init_Str = "config-capture";

   private DebouncedPersistenceScheduler() {
   }

   static void init(AutismConfig config) {
      if (config != null) {
         synchronized (init_Obj) {
            init_AutismConfig = config;
            long version = ++load_J;
            load("config-capture", 300L, () -> load(version));
         }
      }
   }

   private static void load(long version) {
      Runnable capture = () -> read(version);

      try {
         Minecraft client = Minecraft.getInstance();
         if (client != null) {
            client.execute(capture);
            return;
         }
      } catch (Throwable t) {
         AutismClientAddon.LOG.debug("Could not dispatch config capture to client thread", t);
      }

      capture.run();
   }

   private static void read(long version) {
      AutismConfig config;
      synchronized (init_Obj) {
         if (version != load_J || init_AutismConfig == null) {
            return;
         }

         config = init_AutismConfig;
         init_AutismConfig = null;
      }

      AutismConfigPersistence.load(config);
   }

   static void init() {
      AutismConfig config;
      synchronized (init_Obj) {
         config = init_AutismConfig;
         if (config == null) {
            return;
         }

         init_AutismConfig = null;
         load_J++;
         init("config-capture");
      }

      AutismConfigPersistence.load(config);
   }

   static void init(String key, long delayMs, Runnable work) {
      if (key != null && work != null) {
         synchronized (init_Obj) {
            load(key, delayMs, work);
         }
      }
   }

   private static void load(String key, long delayMs, Runnable work) {
      ScheduledFuture<?> old = read.remove(key);
      if (old != null) {
         old.cancel(false);
      }

      long version = write.getOrDefault(key, 0L) + 1L;
      write.put(key, version);
      ScheduledFuture<?> next = init_ScheduledExecutorS.schedule(() -> {
         synchronized (init_Obj) {
            if (write.getOrDefault(key, 0L) != version) {
               return;
            }

            read.remove(key);
         }

         work.run();
      }, Math.max(0L, delayMs), TimeUnit.MILLISECONDS);
      read.put(key, next);
   }

   private static void init(String key) {
      ScheduledFuture<?> pending = read.remove(key);
      if (pending != null) {
         pending.cancel(false);
      }

      write.put(key, write.getOrDefault(key, 0L) + 1L);
   }

   static void init(String key, Runnable work) {
      if (key != null && work != null) {
         synchronized (init_Obj) {
            init_Map.put(key, work);
            if (load_Map.putIfAbsent(key, Boolean.TRUE) == null) {
               init_ScheduledExecutorS.execute(() -> load(key));
            }
         }
      }
   }

   private static void load(String key) {
      while (true) {
         Runnable work;
         synchronized (init_Obj) {
            work = init_Map.remove(key);
            if (work == null) {
               load_Map.remove(key);
               return;
            }
         }

         try {
            work.run();
         } catch (Throwable t) {
            AutismClientAddon.LOG.error("Persistence task failed for {}", key, t);
         }
      }
   }

   static void init(long timeoutMs) {
      init();
      CountDownLatch latch = new CountDownLatch(1);
      init_ScheduledExecutorS.execute(latch::countDown);

      try {
         latch.await(Math.max(0L, timeoutMs), TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
      } catch (Throwable var5) {
      }
   }
}
