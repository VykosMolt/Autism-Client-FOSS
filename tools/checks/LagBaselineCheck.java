package autismclient.util;

public class LagBaselineCheck {
   public static void main(String[] args) {
      for (int[] drop : new int[][]{{144, 40}, {240, 50}, {120, 20}, {60, 16}}) {
         if (!fires(drop[0], 60, false, drop[1])) {
            fail("sustained " + drop[0] + " -> " + drop[1] + " fps drop never fired");
         }
      }

      // A 756 fps menu period followed by healthy 186 fps gameplay must not alarm.
      if (fires(756, 60, true, 186)) {
         fail("186 fps in game after a 756 fps menu alarmed");
      }

      System.out.println("PASS LagBaselineCheck: sustained drops fire, menu frame rates stay out of the baseline");
   }

   // Feeds aF one sample per second: `before` for `seconds`, then `after` for two minutes.
   private static boolean fires(int before, int seconds, boolean screenOpen, int after) {
      LagWatchdogReporter.init();
      for (int s = 0; s < seconds + 120; s++) {
         boolean early = s < seconds;
         if (LagWatchdogReporter.auditLow(early ? before : after, early && screenOpen, 1000L * (s + 1))) {
            return true;
         }
      }

      return false;
   }

   private static void fail(String message) {
      System.out.println("FAIL LagBaselineCheck: " + message);
      System.exit(1);
   }
}
