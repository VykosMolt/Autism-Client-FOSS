package autismclient.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class AsyncTraceLogger {
   private static final int QUEUE_CAPACITY = 4096;
   private static final BlockingQueue<String> QUEUE = new ArrayBlockingQueue<>(4096);
   private static volatile boolean init_Z;
   private static volatile long dropped;

   private AsyncTraceLogger() {
   }

   public static void init(String line) {
      if (line != null) {
         ensureWriter();
         if (!QUEUE.offer(line)) {
            dropped++;
         }
      }
   }

   private static void ensureWriter() {
      if (!init_Z) {
         synchronized (AsyncTraceLogger.class) {
            if (!init_Z) {
               init_Z = true;
               Thread writer = new Thread(AsyncTraceLogger::drainQueue, "autism-trace-log");
               writer.setDaemon(true);
               writer.setPriority(1);
               writer.start();
            }
         }
      }
   }

   private static void drainQueue() {
      while (true) {
         try {
            System.out.println(QUEUE.take());
         } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return;
         } catch (RuntimeException var2) {
         }
      }
   }
}
