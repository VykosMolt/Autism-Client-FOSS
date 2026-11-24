package autismclient.util.e;

public final class CachePool {
   public static final int init_I = 9;
   private long init_J;
   private int load;

   public int init_I(int windowMs, long now) {
      if (windowMs <= 0) {
         return 2147483647;
      }

      if (now < this.init_J || now - this.init_J >= windowMs) {
         this.load = 0;
      }

      return Math.max(0, 9 - this.load);
   }

   public void init_V(int windowMs, long now) {
      if (windowMs > 0) {
         if (this.init_I(windowMs, now) == 9) {
            this.init_J = now;
         }

         this.load++;
      }
   }

   public void init() {
      this.init_J = 0L;
      this.load = 0;
   }
}
