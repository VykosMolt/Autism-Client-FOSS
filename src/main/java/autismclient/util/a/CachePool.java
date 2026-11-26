package autismclient.util.a;

public final class CachePool {
   public static final double init_D = 0.5;
   public final BufferHelper init_BufferHelper;
   public final double load_D;
   public final double read;
   public final long init_J;
   public final long load_J;
   public final double write;
   public final double update;
   public final double sync;
   public final double flush;
   public final double close;
   public final double open;
   public final int init_I;
   public final int load_I;
   public final double poll;
   public final double push;
   public final double pop;
   public final boolean init_Z;
   public final double tick;

   private CachePool(CachePool.Inner_DataUtil b) {
      this.init_BufferHelper = b.init_BufferHelper == null ? BufferHelper.NORMAL : b.init_BufferHelper;
      double ceiling = Math.min(this.init_BufferHelper.load_D, 30.0);
      if (this.init_BufferHelper == BufferHelper.RAGE) {
         double cps = init(init(b.load_D, this.init_BufferHelper.write_D), 1.0, ceiling);
         this.load_D = cps;
         this.read = cps;
      } else {
         double lo = init(b.init_D, 0.0 / 0.0);
         double hi = init(b.load_D, 0.0 / 0.0);
         if (Double.isNaN(lo) || Double.isNaN(hi)) {
            lo = this.init_BufferHelper.read_D;
            hi = this.init_BufferHelper.write_D;
         }

         lo = init(lo, 1.0, ceiling);
         hi = init(hi, 1.0, ceiling);
         if (hi < lo) {
            double swap = lo;
            lo = hi;
            hi = swap;
         }

         if (hi - lo < 0.499999999) {
            hi = Math.min(ceiling, lo + 0.5);
            lo = Math.max(1.0, hi - 0.5);
         }

         this.load_D = lo;
         this.read = hi;
      }

      this.init_J = ConfigStore.init(ConfigStore.init(this.init_BufferHelper, this.read));
      this.load_J = Math.max(this.init_J, ConfigStore.init(this.load_D));
      double holdLo = init(init(b.read, 40.0), 1.0, 1000.0);
      double holdHi = init(init(b.write, 90.0), 1.0, 1000.0);
      this.write = Math.min(holdLo, holdHi);
      this.update = Math.max(holdLo, holdHi);
      this.sync = init(init(b.update, 1.0), 0.0, 2.0);
      this.flush = init(init(b.sync, 0.5), 0.0, 1.0);
      this.close = init(init(b.flush, 1.0), 0.0, 3.0);
      this.open = init(init(b.close, 1.0), 0.25, 4.0);
      int sLo = Math.max(1, Math.min(32, b.init_I));
      int sHi = Math.max(1, Math.min(32, b.load_I));
      this.init_I = Math.min(sLo, sHi);
      this.load_I = Math.max(sLo, sHi);
      double rLo = init(init(b.open, 100.0), 0.0, 2000.0);
      double rHi = init(init(b.poll, 220.0), 0.0, 2000.0);
      this.poll = Math.min(rLo, rHi);
      this.push = Math.max(rLo, rHi);
      this.pop = init(init(b.push, 10.0), 1.0, 100.0);
      this.init_Z = b.init_Z;
      this.tick = init(init(b.pop, 1.0), 0.1, 20.0);
   }

   public static CachePool.Inner_DataUtil init_CachePool_DataUtil(BufferHelper technique) {
      return new CachePool.Inner_DataUtil(technique);
   }

   public static CachePool init_CachePool(BufferHelper technique) {
      return init_CachePool_DataUtil(technique).init();
   }

   private static double init(double value, double fallback) {
      return Double.isFinite(value) ? value : fallback;
   }

   private static double init(double value, double lo, double hi) {
      return Math.max(lo, Math.min(hi, value));
   }

   public static final class Inner_DataUtil {
      private final BufferHelper init_BufferHelper;
      private double init_D;
      private double load_D;
      private double read;
      private double write;
      private double update = 1.0;
      private double sync = 0.5;
      private double flush = 1.0;
      private double close = 1.0;
      private int init_I;
      private int load_I;
      private double open;
      private double poll;
      private double push = 10.0;
      private boolean init_Z;
      private double pop = 1.0;

      private Inner_DataUtil(BufferHelper technique) {
         this.init_BufferHelper = technique == null ? BufferHelper.NORMAL : technique;
         this.init_D = this.init_BufferHelper.read_D;
         this.load_D = this.init_BufferHelper.write_D;
         switch (this.init_BufferHelper) {
            case BUTTERFLY:
               this.load(10.0, 80.0);
               break;
            case DRAG:
               this.load(5.0, 35.0);
               break;
            default:
               this.load(40.0, 90.0);
         }

         if (this.init_BufferHelper == BufferHelper.SCROLL) {
            this.init_CachePool_DataUtil_I_I(4, 9);
            this.read(100.0, 250.0);
         } else {
            this.init_CachePool_DataUtil_I_I(5, 10);
            this.read(100.0, 220.0);
         }
      }

      public CachePool.Inner_DataUtil init_CachePool_DataUtil_D_D(double low, double high) {
         this.init_D = low;
         this.load_D = high;
         return this;
      }

      public CachePool.Inner_DataUtil load(double lowMs, double highMs) {
         this.read = lowMs;
         this.write = highMs;
         return this;
      }

      public CachePool.Inner_DataUtil init(double share) {
         this.update = share;
         return this;
      }

      public CachePool.Inner_DataUtil load(double share) {
         this.sync = share;
         return this;
      }

      public CachePool.Inner_DataUtil read(double scale) {
         this.flush = scale;
         return this;
      }

      public CachePool.Inner_DataUtil write(double scale) {
         this.close = scale;
         return this;
      }

      public CachePool.Inner_DataUtil init_CachePool_DataUtil_I_I(int min, int max) {
         this.init_I = min;
         this.load_I = max;
         return this;
      }

      public CachePool.Inner_DataUtil read(double lowMs, double highMs) {
         this.open = lowMs;
         this.poll = highMs;
         return this;
      }

      public CachePool.Inner_DataUtil update(double ms) {
         this.push = ms;
         return this;
      }

      public CachePool.Inner_DataUtil init_CachePool_DataUtil_Z_D(boolean freeSpin, double decayPerSecond) {
         this.init_Z = freeSpin;
         this.pop = decayPerSecond;
         return this;
      }

      public CachePool init() {
         return new CachePool(this);
      }
   }
}
