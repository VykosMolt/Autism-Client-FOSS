package autismclient.util.oresim;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

final class ConfigStore {
   private final int workerCount;
   private final ThreadPoolExecutor init_ThreadPoolExecutor;
   private final Set<Future<?>> activeTasks = Collections.newSetFromMap(new IdentityHashMap<>());
   private ConfigStore.Inner_DataUtil init_ConfigStore_DataUt;

   ConfigStore(String threadName) {
      this.workerCount = Math.max(2, Math.min(4, Runtime.getRuntime().availableProcessors() / 2));
      AtomicInteger ids = new AtomicInteger();
      this.init_ThreadPoolExecutor = new ThreadPoolExecutor(
         this.workerCount, this.workerCount, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), task -> {
            Thread thread = new Thread(task, threadName + "-" + ids.incrementAndGet());
            thread.setDaemon(true);
            thread.setPriority(1);
            return thread;
         }
      );
   }

   int maxWorkers() {
      return this.workerCount;
   }

   synchronized void init(Runnable task) {
      if (task == null) {
         throw new IllegalArgumentException("OreSim work must not be null");
      }

      this.read();
      ConfigStore.Inner_DataUtil work = new ConfigStore.Inner_DataUtil();
      this.init_ConfigStore_DataUt = work;
      Future<?> future = this.init_ThreadPoolExecutor.submit(() -> {
         try {
            task.run();
         } finally {
            this.init(work);
         }
      });
      work.init = future;
      this.activeTasks.add(future);
      if (this.init_ConfigStore_DataUt != work) {
         future.cancel(true);
         this.activeTasks.remove(future);
      }
   }

   synchronized void submitParallel(Runnable task) {
      if (task == null) {
         throw new IllegalArgumentException("OreSim work must not be null");
      }

      Future<?>[] holder = new Future[1];
      Future<?> future = this.init_ThreadPoolExecutor.submit(() -> {
         try {
            task.run();
         } finally {
            this.finishParallel(holder[0]);
         }
      });
      holder[0] = future;
      this.activeTasks.add(future);
   }

   private synchronized void finishParallel(Future<?> future) {
      if (future != null) {
         this.activeTasks.remove(future);
      }
   }

   synchronized void init_V() {
      this.read();
   }

   synchronized boolean init_Z() {
      return this.init_ConfigStore_DataUt == null && this.activeTasks.isEmpty() && this.init_ThreadPoolExecutor.getQueue().isEmpty();
   }

   private void read() {
      ConfigStore.Inner_DataUtil work = this.init_ConfigStore_DataUt;
      this.init_ConfigStore_DataUt = null;
      this.init_ThreadPoolExecutor.getQueue().clear();
      if (work != null && work.init != null) {
         work.init.cancel(true);
      }

      if (!this.activeTasks.isEmpty()) {
         for (Future<?> f : this.activeTasks) {
            if (f != null) {
               f.cancel(true);
            }
         }

         this.activeTasks.clear();
      }
   }

   private synchronized void init(ConfigStore.Inner_DataUtil work) {
      if (work != null && work.init != null) {
         this.activeTasks.remove(work.init);
      }

      if (this.init_ConfigStore_DataUt == work) {
         this.init_ConfigStore_DataUt = null;
      }
   }

   void load() {
      this.init_V();
      this.init_ThreadPoolExecutor.shutdownNow();
   }

   private static final class Inner_DataUtil {
      volatile Future<?> init;
   }
}
