package autismclient.util.macro;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public final class MacroFpsOverrideManager {
   private static final long init_J = 0L;
   private static final long load = 600000000000L;
   private static final ConcurrentHashMap<Long, MacroFpsOverrideManager.FpsOverride> overrides = new ConcurrentHashMap<>();
   private static volatile int effectiveLimit = -1;
   private static volatile long earliestExpiry = 9223372036854775807L;

   private MacroFpsOverrideManager() {
   }

   public static void setTimedOverride(int fpsLimit, long durationNanos) {
      setOverride(0L, fpsLimit, durationNanos);
   }

   public static synchronized void setOverride(long ownerRunId, int fpsLimit, long durationNanos) {
      if (durationNanos <= 0L) {
         clearOverride(ownerRunId);
      } else {
         overrides.put(ownerRunId, new MacroFpsOverrideManager.FpsOverride(Math.max(0, fpsLimit), System.nanoTime() + durationNanos, false));
         recompute(System.nanoTime());
      }
   }

   public static void setGlobalOverride(int fpsLimit) {
      setOverride(0L, fpsLimit);
   }

   public static synchronized void setOverride(long ownerRunId, int fpsLimit) {
      int limit = Math.max(0, fpsLimit);
      long expiry = limit == 0 ? System.nanoTime() + 600000000000L : 0L;
      overrides.put(ownerRunId, new MacroFpsOverrideManager.FpsOverride(limit, expiry, true));
      recompute(System.nanoTime());
   }

   public static void clearGlobalOverride() {
      clearOverride(0L);
   }

   public static synchronized void clearOverride(long ownerRunId) {
      overrides.remove(ownerRunId);
      recompute(System.nanoTime());
   }

   public static synchronized void clearAll() {
      overrides.clear();
      effectiveLimit = -1;
      earliestExpiry = 9223372036854775807L;
   }

   public static boolean isActive() {
      return getEffectiveLimit() >= 0;
   }

   public static int activeLimit() {
      int active = getEffectiveLimit();
      return Math.max(0, active);
   }

   public static int getEffectiveLimit() {
      int limit = effectiveLimit;
      if (limit < 0) {
         return -1;
      }

      long expiry = earliestExpiry;
      return expiry != 9223372036854775807L && System.nanoTime() - expiry >= 0L ? refreshIfExpired() : limit;
   }

   public static boolean isRenderFrozen() {
      return getEffectiveLimit() == 0;
   }

   public static long remainingNanos() {
      if (getEffectiveLimit() < 0) {
         return 0L;
      }

      long now = System.nanoTime();
      long longest = 0L;

      for (MacroFpsOverrideManager.FpsOverride override : overrides.values()) {
         if (override.pinned && override.limit != 0) {
            return 9223372036854775807L;
         }

         longest = Math.max(longest, override.expiry - now);
      }

      return longest > 0L ? longest / 1000000L : 0L;
   }

   private static synchronized int refreshIfExpired() {
      long now = System.nanoTime();
      if (effectiveLimit >= 0 && earliestExpiry != 9223372036854775807L && now - earliestExpiry >= 0L) {
         recompute(now);
         return effectiveLimit;
      } else {
         return effectiveLimit;
      }
   }

   private static void recompute(long now) {
      int effective = 2147483647;
      long nextExpiry = 9223372036854775807L;

      for (Entry<Long, MacroFpsOverrideManager.FpsOverride> entry : overrides.entrySet()) {
         MacroFpsOverrideManager.FpsOverride override = entry.getValue();
         if (!override.isLive(now)) {
            overrides.remove(entry.getKey(), override);
         } else {
            effective = Math.min(effective, override.limit);
            if (!override.pinned || override.limit == 0) {
               nextExpiry = Math.min(nextExpiry, override.expiry);
            }
         }
      }

      effectiveLimit = effective == 2147483647 ? -1 : effective;
      earliestExpiry = effectiveLimit < 0 ? 9223372036854775807L : nextExpiry;
   }

   private record FpsOverride(int limit, long expiry, boolean pinned) {
      boolean isLive(long now) {
         return this.pinned && this.limit != 0 ? true : now - this.expiry < 0L;
      }
   }
}
