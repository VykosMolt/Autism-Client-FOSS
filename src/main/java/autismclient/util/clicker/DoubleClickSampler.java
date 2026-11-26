package autismclient.util.clicker;

import java.util.Random;

public final class DoubleClickSampler {
   private static final double init_D = 0.08;
   private final Random init_Random;

   public DoubleClickSampler(Random rng) {
      this.init_Random = rng == null ? new Random() : rng;
   }

   public int init() {
      return this.init_Random.nextDouble() < 0.08 ? 1 : 0;
   }
}
