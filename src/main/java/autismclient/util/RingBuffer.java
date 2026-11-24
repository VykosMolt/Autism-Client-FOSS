package autismclient.util;

public final class RingBuffer {
   public static final double init = 0.5;
   public static final double load = 1.5;
   public static final double read = 0.75;
   public static final double write = 0.7;
   public static final double update = 1.5;

   private RingBuffer() {
   }

   public static boolean init(String mode, double challengerScore, double incumbentScore) {
      boolean var10000;
      switch (mode) {
         case "HP":
            var10000 = challengerScore <= incumbentScore * 0.75;
            break;
         case "FOV":
            var10000 = challengerScore <= incumbentScore * 0.7 && challengerScore <= incumbentScore - 1.5;
            break;
         default:
            var10000 = Math.sqrt(challengerScore) <= Math.sqrt(incumbentScore) - 1.5;
      }

      return var10000;
   }
}
