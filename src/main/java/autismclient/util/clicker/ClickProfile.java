package autismclient.util.clicker;

public final class ClickProfile {
   public static final double init_D = 0.5;
   public final ClickTechnique technique;
   public final double load_D;
   public final double cpsHigh;
   public final long minIntervalNanos;
   public final long maxIntervalNanos;
   public final double holdLowMs;
   public final double holdHighMs;
   public final double sync;
   public final double flush;
   public final double close;
   public final double open;
   public final int init_I;
   public final int load_I;
   public final double poll;
   public final double push;
   public final double pop;
   public final boolean freeSpin;
   public final double freeSpinDecayPerSecond;

   private ClickProfile(ClickProfile.ProfileBuilder b) {
      this.technique = b.technique == null ? ClickTechnique.NORMAL : b.technique;
      double ceiling = Math.min(this.technique.load_D, 30.0);
      if (this.technique == ClickTechnique.RAGE) {
         double cps = clamp(finiteOr(b.cpsHigh, this.technique.write_D), 1.0, ceiling);
         this.load_D = cps;
         this.cpsHigh = cps;
      } else {
         double lo = finiteOr(b.cpsLow, 0.0 / 0.0);
         double hi = finiteOr(b.cpsHigh, 0.0 / 0.0);
         if (Double.isNaN(lo) || Double.isNaN(hi)) {
            lo = this.technique.read_D;
            hi = this.technique.write_D;
         }

         lo = clamp(lo, 1.0, ceiling);
         hi = clamp(hi, 1.0, ceiling);
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
         this.cpsHigh = hi;
      }

      this.minIntervalNanos = ClickTimingMath.intervalNanos(ClickTimingMath.effectiveUpperCps(this.technique, this.cpsHigh));
      this.maxIntervalNanos = Math.max(this.minIntervalNanos, ClickTimingMath.intervalNanos(this.load_D));
      double holdLo = clamp(finiteOr(b.holdLowMs, 40.0), 1.0, 1000.0);
      double holdHi = clamp(finiteOr(b.holdHighMs, 90.0), 1.0, 1000.0);
      this.holdLowMs = Math.min(holdLo, holdHi);
      this.holdHighMs = Math.max(holdLo, holdHi);
      this.sync = clamp(finiteOr(b.update, 1.0), 0.0, 2.0);
      this.flush = clamp(finiteOr(b.sync, 0.5), 0.0, 1.0);
      this.close = clamp(finiteOr(b.flush, 1.0), 0.0, 3.0);
      this.open = clamp(finiteOr(b.close, 1.0), 0.25, 4.0);
      int sLo = Math.max(1, Math.min(32, b.init_I));
      int sHi = Math.max(1, Math.min(32, b.load_I));
      this.init_I = Math.min(sLo, sHi);
      this.load_I = Math.max(sLo, sHi);
      double rLo = clamp(finiteOr(b.open, 100.0), 0.0, 2000.0);
      double rHi = clamp(finiteOr(b.poll, 220.0), 0.0, 2000.0);
      this.poll = Math.min(rLo, rHi);
      this.push = Math.max(rLo, rHi);
      this.pop = clamp(finiteOr(b.push, 10.0), 1.0, 100.0);
      this.freeSpin = b.freeSpin;
      this.freeSpinDecayPerSecond = clamp(finiteOr(b.freeSpinDecayPerSecond, 1.0), 0.1, 20.0);
   }

   public static ClickProfile.ProfileBuilder builder(ClickTechnique technique) {
      return new ClickProfile.ProfileBuilder(technique);
   }

   public static ClickProfile forTechnique(ClickTechnique technique) {
      return builder(technique).build();
   }

   private static double finiteOr(double value, double fallback) {
      return Double.isFinite(value) ? value : fallback;
   }

   private static double clamp(double value, double lo, double hi) {
      return Math.max(lo, Math.min(hi, value));
   }

   public static final class ProfileBuilder {
      private final ClickTechnique technique;
      private double cpsLow;
      private double cpsHigh;
      private double holdLowMs;
      private double holdHighMs;
      private double update = 1.0;
      private double sync = 0.5;
      private double flush = 1.0;
      private double close = 1.0;
      private int init_I;
      private int load_I;
      private double open;
      private double poll;
      private double push = 10.0;
      private boolean freeSpin;
      private double freeSpinDecayPerSecond = 1.0;

      private ProfileBuilder(ClickTechnique technique) {
         this.technique = technique == null ? ClickTechnique.NORMAL : technique;
         this.cpsLow = this.technique.read_D;
         this.cpsHigh = this.technique.write_D;
         switch (this.technique) {
            case BUTTERFLY:
               this.holdMs(10.0, 80.0);
               break;
            case DRAG:
               this.holdMs(5.0, 35.0);
               break;
            default:
               this.holdMs(40.0, 90.0);
         }

         if (this.technique == ClickTechnique.SCROLL) {
            this.burstCount(4, 9);
            this.resetPause(100.0, 250.0);
         } else {
            this.burstCount(5, 10);
            this.resetPause(100.0, 220.0);
         }
      }

      public ClickProfile.ProfileBuilder cpsRange(double low, double high) {
         this.cpsLow = low;
         this.cpsHigh = high;
         return this;
      }

      public ClickProfile.ProfileBuilder holdMs(double lowMs, double highMs) {
         this.holdLowMs = lowMs;
         this.holdHighMs = highMs;
         return this;
      }

      public ClickProfile.ProfileBuilder variation(double share) {
         this.update = share;
         return this;
      }

      public ClickProfile.ProfileBuilder fatigue(double share) {
         this.sync = share;
         return this;
      }

      public ClickProfile.ProfileBuilder wobble(double scale) {
         this.flush = scale;
         return this;
      }

      public ClickProfile.ProfileBuilder fingerSync(double scale) {
         this.close = scale;
         return this;
      }

      public ClickProfile.ProfileBuilder burstCount(int min, int max) {
         this.init_I = min;
         this.load_I = max;
         return this;
      }

      public ClickProfile.ProfileBuilder resetPause(double lowMs, double highMs) {
         this.open = lowMs;
         this.poll = highMs;
         return this;
      }

      public ClickProfile.ProfileBuilder pulse(double ms) {
         this.push = ms;
         return this;
      }

      public ClickProfile.ProfileBuilder freeSpin(boolean freeSpin, double decayPerSecond) {
         this.freeSpin = freeSpin;
         this.freeSpinDecayPerSecond = decayPerSecond;
         return this;
      }

      public ClickProfile build() {
         return new ClickProfile(this);
      }
   }
}
