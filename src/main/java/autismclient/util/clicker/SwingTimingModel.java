package autismclient.util.clicker;

import java.util.Random;

public final class SwingTimingModel {
   public static final double init_D = 0.92;
   public static final double load = -75.0;
   public static final double read = 50.0;
   private static final double NOISE_DECAY = 0.4;
   private static final double NOISE_SCALE = 0.9;
   private static final double PAUSE_MIN_MS = 60.0;
   private static final double PAUSE_RANGE_MS = 120.0;
   private final Random random;
   private double noise;

   public SwingTimingModel() {
      this(new Random());
   }

   public SwingTimingModel(Random rng) {
      this.random = rng == null ? new Random() : rng;
      this.noise = 0.9 * this.random.nextGaussian();
   }

   public double sampleOffset(double lowMs, double highMs, double hesitationChance) {
      double low = Math.min(lowMs, highMs);
      double high = Math.max(lowMs, highMs);
      double ms = low + (high - low) * this.random.nextDouble();
      if (this.random.nextDouble() < hesitationChance) {
         ms += 60.0 + 120.0 * this.random.nextDouble();
      }

      return ms;
   }

   public long sampleHoldNanos(double lowMs, double highMs) {
      double low = Math.max(1.0, Math.min(lowMs, highMs));
      double high = Math.max(low, Math.max(lowMs, highMs));
      this.noise = 0.4 * this.noise + 0.9 * this.random.nextGaussian();
      return ClickTimingMath.msToNanos(low + (high - low) * sigmoid(this.noise));
   }

   public static boolean isReady(double shownCharge, int ticker, double delayTicks, double msPerTick, double offsetMs) {
      if (delayTicks <= 0.0) {
         return true;
      } else {
         return (shownCharge - delayTicks) * msPerTick < offsetMs ? false : (ticker + 0.5) / delayTicks >= 0.9199;
      }
   }

   private static double sigmoid(double x) {
      return 1.0 / (1.0 + Math.exp(-x));
   }
}
