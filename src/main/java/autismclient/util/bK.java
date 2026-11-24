package autismclient.util;

import autismclient.AutismClientAddon;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public final class bK {
   private static final int init_I = 240;
   private static final long init_J = 4000000L;
   private static final boolean init_Z = Boolean.getBoolean("autism.perf");
   private static volatile Map<String, bK.Inner_DataUtil> init_Map;
   private static volatile boolean load_Z;
   private static volatile int load_I;
   private static volatile int read;

   private bK() {
   }

   public static long init_J() {
      return read_Z() ? System.nanoTime() : 0L;
   }

   public static long load_J() {
      return read <= 0 && !read_Z() ? 0L : System.nanoTime();
   }

   public static void init(int ticks) {
      read = Math.max(read, ticks);
   }

   public static void init_V() {
      if (read > 0) {
         read--;
      }
   }

   public static boolean init_Z() {
      return read > 0;
   }

   public static Map<String, long[]> init_Map() {
      Map<String, bK.Inner_DataUtil> source = init_Map;
      if (source != null && !source.isEmpty()) {
         Map<String, long[]> out = new HashMap<>(source.size());

         for (Entry<String, bK.Inner_DataUtil> entry : source.entrySet()) {
            bK.Inner_DataUtil c = entry.getValue();
            synchronized (c) {
               out.put(entry.getKey(), new long[]{c.init, c.load, c.read});
            }
         }

         return out;
      } else {
         return Map.of();
      }
   }

   public static long read_J() {
      return load_I > 0 && read_Z() ? System.nanoTime() : 0L;
   }

   public static void init(String name, long startNanos) {
      if (startNanos != 0L && name != null && !name.isBlank()) {
         long elapsed = System.nanoTime() - startNanos;
         read(name, elapsed);
      }
   }

   public static void load(String name, long startNanos) {
      init(name, startNanos, 4000000L);
   }

   public static void init(String name, long startNanos, long thresholdNanos) {
      if (startNanos != 0L && name != null && !name.isBlank()) {
         long elapsed = System.nanoTime() - startNanos;
         read(name, elapsed);
         if (load_I > 0 && elapsed >= thresholdNanos) {
            AutismClientAddon.LOG.warn("[AutismPerf] join spike {}={}ms", name, String.format(Locale.ROOT, "%.3f", elapsed / 1000000.0));
         }
      }
   }

   public static void load(String name, long startNanos, long thresholdNanos) {
      if (startNanos != 0L && name != null && !name.isBlank()) {
         long elapsed = System.nanoTime() - startNanos;
         read(name, elapsed);
         if (elapsed >= thresholdNanos) {
            AutismClientAddon.LOG.warn("[AutismPerf] spike {}={}ms", name, String.format(Locale.ROOT, "%.3f", elapsed / 1000000.0));
         }
      }
   }

   private static void read(String name, long elapsed) {
      bK.Inner_DataUtil counter = load_Map().computeIfAbsent(name, ignored -> new bK.Inner_DataUtil());
      long samples;
      long total;
      long max;
      synchronized (counter) {
         samples = ++counter.init;
         total = counter.load += elapsed;
         max = counter.read = Math.max(counter.read, elapsed);
      }

      if (samples % 240L == 0L) {
         double avgMs = (double)total / samples / 1000000.0;
         double maxMs = max / 1000000.0;
         AutismClientAddon.LOG
            .info(
               "[AutismPerf] {} avg={}ms max={}ms samples={}",
               name,
               String.format(Locale.ROOT, "%.3f", avgMs),
               String.format(Locale.ROOT, "%.3f", maxMs),
               samples
            );
      }
   }

   public static void load_V() {
      if (read_Z()) {
         load_I = 100;
         AutismClientAddon.LOG.info("[AutismPerf] join profiling window started.");
      }
   }

   public static void read_V() {
      if (load_I > 0) {
         load_I--;
      }
   }

   public static boolean load_Z() {
      return load_I > 0 && read_Z();
   }

   public static boolean read_Z() {
      return init_Z || load_Z;
   }

   static void init(AutismConfig config) {
      load_Z = config != null && config.performanceDebug;
   }

   private static Map<String, bK.Inner_DataUtil> load_Map() {
      Map<String, bK.Inner_DataUtil> current = init_Map;
      if (current != null) {
         return current;
      }

      synchronized (bK.class) {
         current = init_Map;
         if (current == null) {
            current = new ConcurrentHashMap<>();
            init_Map = current;
         }

         return current;
      }
   }

   private static final class Inner_DataUtil {
      private long init;
      private long load;
      private long read;
   }
}
