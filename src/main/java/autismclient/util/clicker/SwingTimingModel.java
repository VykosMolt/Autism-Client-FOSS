package autismclient.util.clicker;

import java.util.Random;

public final class SwingTimingModel {
   public static final double init_D = 0.92;
   public static final double load = -75.0;
   public static final double read = 50.0;
   private static final double write = 0.4;
   private static final double update = 0.9;
   private static final double sync = 60.0;
   private static final double flush = 120.0;
   private final Random init_Random;
   private double close;

   public SwingTimingModel() {
      this(new Random());
   }

   public SwingTimingModel(Random rng) {
      this.init_Random = rng == null ? new Random() : rng;
      this.close = 0.9 * this.init_Random.nextGaussian();
   }

   public double init(double lowMs, double highMs, double hesitationChance) {
      double low = Math.min(lowMs, highMs);
      double high = Math.max(lowMs, highMs);
      double ms = low + (high - low) * this.init_Random.nextDouble();
      if (this.init_Random.nextDouble() < hesitationChance) {
         ms += 60.0 + 120.0 * this.init_Random.nextDouble();
      }

      return ms;
   }

   public long init(double lowMs, double highMs) {
      double low = Math.max(1.0, Math.min(lowMs, highMs));
      double high = Math.max(low, Math.max(lowMs, highMs));
      this.close = 0.4 * this.close + 0.9 * this.init_Random.nextGaussian();
      return ClickTimingMath.load(low + (high - low) * init(this.close));
   }

   public static boolean init(double shownCharge, int ticker, double delayTicks, double msPerTick, double offsetMs) {
      if (delayTicks <= 0.0) {
         return true;
      } else {
         return (shownCharge - delayTicks) * msPerTick < offsetMs ? false : (ticker + 0.5) / delayTicks >= 0.9199;
      }
   }

   private static double init(double x) {
      return 1.0 / (1.0 + Math.exp(-x));
   }
}
