package autismclient.util.autologin;

public record AutoLoginTimings(long giveUpMs, long load, long read, int maxAttempts, long write, long update) {
   public AutoLoginTimings {
      giveUpMs = init(giveUpMs, 1000L, 300000L);
      load = init(load, 0L, 60000L);
      read = init(read, 250L, 30000L);
      maxAttempts = (int)init(maxAttempts, 1L, 20L);
      write = init(write, 100L, 10000L);
      update = init(update, 250L, 30000L);
   }

   public static AutoLoginTimings init_StreamUtil() {
      return new AutoLoginTimings(40000L, 2000L, 2500L, 4, 1000L, 3000L);
   }

   public static AutoLoginTimings defaults() {
      return new AutoLoginTimings(10000L, 2000L, 2500L, 4, 1000L, 3000L);
   }

   private static long init(long value, long low, long high) {
      return Math.max(low, Math.min(high, value));
   }

   public long promptDelayMs() {
      return this.load;
   }
}
