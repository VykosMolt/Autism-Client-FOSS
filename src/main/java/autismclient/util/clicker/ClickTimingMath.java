package autismclient.util.clicker;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ClickTimingMath {
   public static final long init_J = 1000000000L;
   private static final BigDecimal NANOS_PER_SECOND = BigDecimal.valueOf(1000000000L);

   private ClickTimingMath() {
   }

   public static long intervalNanos(double cps) {
      if (cps > 0.0 && !Double.isInfinite(cps)) {
         return NANOS_PER_SECOND.divide(new BigDecimal(Double.toString(cps)), 0, RoundingMode.CEILING).longValueExact();
      } else {
         throw new IllegalArgumentException("cps " + cps);
      }
   }

   public static double effectiveUpperCps(ClickTechnique technique, double userUpper) {
      double ceiling = Math.min(technique.load_D, 30.0);
      return Double.isFinite(userUpper) ? Math.min(userUpper, ceiling) : ceiling;
   }

   public static double[] cpsToMs(double lowCps, double highCps) {
      return new double[]{1000.0 / highCps, 1000.0 / lowCps};
   }

   public static double[] msToCps(double fastMs, double slowMs) {
      return new double[]{1000.0 / slowMs, 1000.0 / fastMs};
   }

   public static long msToNanos(double ms) {
      return Math.round(ms * 1000000.0);
   }
}
