package autismclient.util.g;

final class DataPipe {
   static final long init = 50000000L;
   static final long load = 120000000L;
   private long read;

   DataPipe() {
      this.init_V(System.nanoTime());
   }

   synchronized void init_V(long nowNanos) {
      this.read = nowNanos - 120000000L;
   }

   synchronized boolean init_Z(long nowNanos) {
      if (this.read < nowNanos - 120000000L) {
         this.read = nowNanos - 120000000L;
      }

      if (this.read > nowNanos) {
         return false;
      }

      this.read += 50000000L;
      return true;
   }

   synchronized int init_I(long nowNanos) {
      long floored = Math.max(this.read, nowNanos - 120000000L);
      return floored > nowNanos ? 0 : (int)((nowNanos - floored) / 50000000L) + 1;
   }

   synchronized long init(long nowNanos, int packets) {
      if (packets <= 0) {
         return 0L;
      }

      long floored = Math.max(this.read, nowNanos - 120000000L);
      long finishAt = floored + (packets - 1) * 50000000L;
      return Math.max(0L, (finishAt - nowNanos) / 1000000L);
   }
}
