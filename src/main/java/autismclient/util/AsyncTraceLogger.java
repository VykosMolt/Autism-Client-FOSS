package autismclient.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class AsyncTraceLogger {
   private static final int QUEUE_CAPACITY = 4096;
   private static final BlockingQueue<String> QUEUE = new ArrayBlockingQueue<>(4096);
   private static volatile boolean started;
   private static volatile long dropped;

   private AsyncTraceLogger() {
   }

   public static void log(String line) {
      if (line != null) {
         ensureWriter();
         if (!QUEUE.offer(line)) {
            dropped++;
         }
      }
   }

   private static void ensureWriter() {
      if (!started) {
         synchronized (AsyncTraceLogger.class) {
            if (!started) {
               started = true;
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
