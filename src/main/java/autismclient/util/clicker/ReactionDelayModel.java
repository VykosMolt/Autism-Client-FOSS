package autismclient.util.clicker;

import java.util.Random;

public final class ReactionDelayModel {
   private static final double init = 0.04;

   private ReactionDelayModel() {
   }

   public static double init(Random rng, double lowMs, double highMs, double bias, double angleDegrees) {
      double low = Math.max(0.0, Math.min(lowMs, highMs));
      double high = Math.max(low, Math.max(lowMs, highMs));
      if (high <= 0.0) {
         return 0.0;
      }

      double base = low + (high - low) * (0.2 + 0.6 * rng.nextDouble());
      double skewed = base * Math.exp(rng.nextGaussian() * 0.22);
      if (rng.nextDouble() < 0.04) {
         skewed *= 2.2 + 1.3 * rng.nextDouble();
      }

      skewed *= 1.0 + Math.min(1.0, Math.max(0.0, angleDegrees) / 120.0) * 0.6;
      return Math.max(0.0, Math.min(high * 2.5, skewed) * bias);
   }
}
