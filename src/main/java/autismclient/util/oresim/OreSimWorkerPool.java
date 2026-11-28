package autismclient.util.oresim;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

final class OreSimWorkerPool {
   private final int workerCount;
   private final ThreadPoolExecutor executor;
   private final Set<Future<?>> activeTasks = Collections.newSetFromMap(new IdentityHashMap<>());
   private OreSimWorkerPool.Inner_DataUtil work;

   OreSimWorkerPool(String threadName) {
      this.workerCount = Math.max(2, Math.min(4, Runtime.getRuntime().availableProcessors() / 2));
      AtomicInteger ids = new AtomicInteger();
      this.executor = new ThreadPoolExecutor(
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

   synchronized void submit(Runnable task) {
      if (task == null) {
         throw new IllegalArgumentException("OreSim work must not be null");
      }

      this.cancelPending();
      OreSimWorkerPool.Inner_DataUtil work = new OreSimWorkerPool.Inner_DataUtil();
      this.work = work;
      Future<?> future = this.executor.submit(() -> {
         try {
            task.run();
         } finally {
            this.finishWork(work);
         }
      });
      work.future = future;
      this.activeTasks.add(future);
      if (this.work != work) {
         future.cancel(true);
         this.activeTasks.remove(future);
      }
   }

   synchronized void submitParallel(Runnable task) {
      if (task == null) {
         throw new IllegalArgumentException("OreSim work must not be null");
      }

      Future<?>[] holder = new Future[1];
      Future<?> future = this.executor.submit(() -> {
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
      this.cancelPending();
   }

   synchronized boolean isIdle() {
      return this.work == null && this.activeTasks.isEmpty() && this.executor.getQueue().isEmpty();
   }

   private void cancelPending() {
      OreSimWorkerPool.Inner_DataUtil work = this.work;
      this.work = null;
      this.executor.getQueue().clear();
      if (work != null && work.future != null) {
         work.future.cancel(true);
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

   private synchronized void finishWork(OreSimWorkerPool.Inner_DataUtil work) {
      if (work != null && work.future != null) {
         this.activeTasks.remove(work.future);
      }

      if (this.work == work) {
         this.work = null;
      }
   }

   void shutdown() {
      this.init_V();
      this.executor.shutdownNow();
   }

   private static final class Inner_DataUtil {
      volatile Future<?> future;
   }
}
