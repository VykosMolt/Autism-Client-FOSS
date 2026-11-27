package autismclient.util.macro;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public final class MacroFpsOverrideManager {
   private static final long init_J = 0L;
   private static final long load = 600000000000L;
   private static final ConcurrentHashMap<Long, MacroFpsOverrideManager.Inner_DataUtil> init_ConcurrentHashMap = new ConcurrentHashMap<>();
   private static volatile int init_I = -1;
   private static volatile long read = 9223372036854775807L;

   private MacroFpsOverrideManager() {
   }

   public static void init_V_I_J(int fpsLimit, long durationNanos) {
      init(0L, fpsLimit, durationNanos);
   }

   public static synchronized void init(long ownerRunId, int fpsLimit, long durationNanos) {
      if (durationNanos <= 0L) {
         clearOverride(ownerRunId);
      } else {
         init_ConcurrentHashMap.put(ownerRunId, new MacroFpsOverrideManager.Inner_DataUtil(Math.max(0, fpsLimit), System.nanoTime() + durationNanos, false));
         load(System.nanoTime());
      }
   }

   public static void init_V_I(int fpsLimit) {
      setOverride(0L, fpsLimit);
   }

   public static synchronized void setOverride(long ownerRunId, int fpsLimit) {
      int limit = Math.max(0, fpsLimit);
      long expiry = limit == 0 ? System.nanoTime() + 600000000000L : 0L;
      init_ConcurrentHashMap.put(ownerRunId, new MacroFpsOverrideManager.Inner_DataUtil(limit, expiry, true));
      load(System.nanoTime());
   }

   public static void init_V() {
      clearOverride(0L);
   }

   public static synchronized void clearOverride(long ownerRunId) {
      init_ConcurrentHashMap.remove(ownerRunId);
      load(System.nanoTime());
   }

   public static synchronized void clearAll() {
      init_ConcurrentHashMap.clear();
      init_I = -1;
      read = 9223372036854775807L;
   }

   public static boolean isActive() {
      return getEffectiveLimit() >= 0;
   }

   public static int init_I() {
      int active = getEffectiveLimit();
      return Math.max(0, active);
   }

   public static int getEffectiveLimit() {
      int limit = init_I;
      if (limit < 0) {
         return -1;
      }

      long expiry = read;
      return expiry != 9223372036854775807L && System.nanoTime() - expiry >= 0L ? read() : limit;
   }

   public static boolean isRenderFrozen() {
      return getEffectiveLimit() == 0;
   }

   public static long init_J() {
      if (getEffectiveLimit() < 0) {
         return 0L;
      }

      long now = System.nanoTime();
      long longest = 0L;

      for (MacroFpsOverrideManager.Inner_DataUtil override : init_ConcurrentHashMap.values()) {
         if (override.init_Z && override.init_I != 0) {
            return 9223372036854775807L;
         }

         longest = Math.max(longest, override.init_J - now);
      }

      return longest > 0L ? longest / 1000000L : 0L;
   }

   private static synchronized int read() {
      long now = System.nanoTime();
      if (init_I >= 0 && read != 9223372036854775807L && now - read >= 0L) {
         load(now);
         return init_I;
      } else {
         return init_I;
      }
   }

   private static void load(long now) {
      int effective = 2147483647;
      long nextExpiry = 9223372036854775807L;

      for (Entry<Long, MacroFpsOverrideManager.Inner_DataUtil> entry : init_ConcurrentHashMap.entrySet()) {
         MacroFpsOverrideManager.Inner_DataUtil override = entry.getValue();
         if (!override.init(now)) {
            init_ConcurrentHashMap.remove(entry.getKey(), override);
         } else {
            effective = Math.min(effective, override.init_I);
            if (!override.init_Z || override.init_I == 0) {
               nextExpiry = Math.min(nextExpiry, override.init_J);
            }
         }
      }

      init_I = effective == 2147483647 ? -1 : effective;
      read = init_I < 0 ? 9223372036854775807L : nextExpiry;
   }

   private record Inner_DataUtil(int init_I, long init_J, boolean init_Z) {
      boolean init(long now) {
         return this.init_Z && this.init_I != 0 ? true : now - this.init_J < 0L;
      }
   }
}
