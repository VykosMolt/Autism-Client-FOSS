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
   private static final Object LOCK = new Object();
   private static final Map<String, Runnable> pendingWork = new HashMap<>();
   private static final Map<String, Boolean> draining = new HashMap<>();
   private static final Map<String, ScheduledFuture<?>> scheduled = new HashMap<>();
   private static final Map<String, Long> versions = new HashMap<>();
   private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread thread = new Thread(r, "autism-persistence");
      thread.setDaemon(true);
      return thread;
   });
   private static AutismConfig pendingConfig;
   private static long configVersion;
   private static final String CONFIG_CAPTURE_KEY = "config-capture";

   private DebouncedPersistenceScheduler() {
   }

   static void schedule(AutismConfig config) {
      if (config != null) {
         synchronized (LOCK) {
            pendingConfig = config;
            long version = ++configVersion;
            scheduleLocked("config-capture", 300L, () -> dispatchCapture(version));
         }
      }
   }

   private static void dispatchCapture(long version) {
      Runnable capture = () -> captureConfig(version);

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

   private static void captureConfig(long version) {
      AutismConfig config;
      synchronized (LOCK) {
         if (version != configVersion || pendingConfig == null) {
            return;
         }

         config = pendingConfig;
         pendingConfig = null;
      }

      AutismConfigPersistence.persist(config);
   }

   static void flushConfig() {
      AutismConfig config;
      synchronized (LOCK) {
         config = pendingConfig;
         if (config == null) {
            return;
         }

         pendingConfig = null;
         configVersion++;
         cancelLocked("config-capture");
      }

      AutismConfigPersistence.persist(config);
   }

   static void schedule(String key, long delayMs, Runnable work) {
      if (key != null && work != null) {
         synchronized (LOCK) {
            scheduleLocked(key, delayMs, work);
         }
      }
   }

   private static void scheduleLocked(String key, long delayMs, Runnable work) {
      ScheduledFuture<?> old = scheduled.remove(key);
      if (old != null) {
         old.cancel(false);
      }

      long version = versions.getOrDefault(key, 0L) + 1L;
      versions.put(key, version);
      ScheduledFuture<?> next = EXECUTOR.schedule(() -> {
         synchronized (LOCK) {
            if (versions.getOrDefault(key, 0L) != version) {
               return;
            }

            scheduled.remove(key);
         }

         work.run();
      }, Math.max(0L, delayMs), TimeUnit.MILLISECONDS);
      scheduled.put(key, next);
   }

   private static void cancelLocked(String key) {
      ScheduledFuture<?> pending = scheduled.remove(key);
      if (pending != null) {
         pending.cancel(false);
      }

      versions.put(key, versions.getOrDefault(key, 0L) + 1L);
   }

   static void schedule(String key, Runnable work) {
      if (key != null && work != null) {
         synchronized (LOCK) {
            pendingWork.put(key, work);
            if (draining.putIfAbsent(key, Boolean.TRUE) == null) {
               EXECUTOR.execute(() -> drain(key));
            }
         }
      }
   }

   private static void drain(String key) {
      while (true) {
         Runnable work;
         synchronized (LOCK) {
            work = pendingWork.remove(key);
            if (work == null) {
               draining.remove(key);
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

   static void flushAll(long timeoutMs) {
      flushConfig();
      CountDownLatch latch = new CountDownLatch(1);
      EXECUTOR.execute(latch::countDown);

      try {
         latch.await(Math.max(0L, timeoutMs), TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
      } catch (Throwable var5) {
      }
   }
}
