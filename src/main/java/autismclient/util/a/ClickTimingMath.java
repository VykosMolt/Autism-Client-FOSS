package autismclient.util.a;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ClickTimingMath {
   public static final long init_J = 1000000000L;
   private static final BigDecimal init_BigDecimal = BigDecimal.valueOf(1000000000L);

   private ClickTimingMath() {
   }

   public static long init(double cps) {
      if (cps > 0.0 && !Double.isInfinite(cps)) {
         return init_BigDecimal.divide(new BigDecimal(Double.toString(cps)), 0, RoundingMode.CEILING).longValueExact();
      } else {
         throw new IllegalArgumentException("cps " + cps);
      }
   }

   public static double init(ClickTechnique technique, double userUpper) {
      double ceiling = Math.min(technique.load_D, 30.0);
      return Double.isFinite(userUpper) ? Math.min(userUpper, ceiling) : ceiling;
   }

   public static double[] init(double lowCps, double highCps) {
      return new double[]{1000.0 / highCps, 1000.0 / lowCps};
   }

   public static double[] load(double fastMs, double slowMs) {
      return new double[]{1000.0 / slowMs, 1000.0 / fastMs};
   }

   public static long load(double ms) {
      return Math.round(ms * 1000000.0);
   }
}
