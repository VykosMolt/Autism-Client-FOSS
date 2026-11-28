package autismclient.util.autologin;

public record AutoLoginTimings(long giveUpMs, long load, long read, int maxAttempts, long write, long update) {
   public AutoLoginTimings {
      giveUpMs = clamp(giveUpMs, 1000L, 300000L);
      load = clamp(load, 0L, 60000L);
      read = clamp(read, 250L, 30000L);
      maxAttempts = (int)clamp(maxAttempts, 1L, 20L);
      write = clamp(write, 100L, 10000L);
      update = clamp(update, 250L, 30000L);
   }

   public static AutoLoginTimings legacyDefaults() {
      return new AutoLoginTimings(40000L, 2000L, 2500L, 4, 1000L, 3000L);
   }

   public static AutoLoginTimings defaults() {
      return new AutoLoginTimings(10000L, 2000L, 2500L, 4, 1000L, 3000L);
   }

   private static long clamp(long value, long low, long high) {
      return Math.max(low, Math.min(high, value));
   }

   public long promptDelayMs() {
      return this.load;
   }
}
