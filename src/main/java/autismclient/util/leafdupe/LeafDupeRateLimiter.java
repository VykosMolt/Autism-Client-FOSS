package autismclient.util.leafdupe;

public final class LeafDupeRateLimiter {
   public static final int init_I = 9;
   private long init_J;
   private int sentCount;

   public int remaining(int windowMs, long now) {
      if (windowMs <= 0) {
         return 2147483647;
      }

      if (now < this.init_J || now - this.init_J >= windowMs) {
         this.sentCount = 0;
      }

      return Math.max(0, 9 - this.sentCount);
   }

   public void recordUse(int windowMs, long now) {
      if (windowMs > 0) {
         if (this.remaining(windowMs, now) == 9) {
            this.init_J = now;
         }

         this.sentCount++;
      }
   }

   public void reset() {
      this.init_J = 0L;
      this.sentCount = 0;
   }
}
