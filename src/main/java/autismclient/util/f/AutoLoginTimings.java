package autismclient.util.f;

public record AutoLoginTimings(long init_J, long load, long read, int init_I, long write, long update) {
   public AutoLoginTimings {
      init_J = init(init_J, 1000L, 300000L);
      load = init(load, 0L, 60000L);
      read = init(read, 250L, 30000L);
      init_I = (int)init(init_I, 1L, 20L);
      write = init(write, 100L, 10000L);
      update = init(update, 250L, 30000L);
   }

   public static AutoLoginTimings init_StreamUtil() {
      return new AutoLoginTimings(40000L, 2000L, 2500L, 4, 1000L, 3000L);
   }

   public static AutoLoginTimings load_StreamUtil() {
      return new AutoLoginTimings(10000L, 2000L, 2500L, 4, 1000L, 3000L);
   }

   private static long init(long value, long low, long high) {
      return Math.max(low, Math.min(high, value));
   }

   public long load_J() {
      return this.load;
   }
}
