package autismclient.util.clicker;

import java.util.Random;

public final class DoubleClickSampler {
   private static final double init_D = 0.08;
   private final Random random;

   public DoubleClickSampler(Random rng) {
      this.random = rng == null ? new Random() : rng;
   }

   public int sampleFollowUps() {
      return this.random.nextDouble() < 0.08 ? 1 : 0;
   }
}
