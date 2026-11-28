package autismclient.util;

public final class BlockBreakGraceCalc {
   private BlockBreakGraceCalc() {
   }

   public static int graceTicks(int latencyMillis) {
      return 16 + Math.min(44, Math.max(0, latencyMillis) / 50);
   }

   public static boolean inGrace(int now, int brokenAt, int latencyMillis) {
      int elapsed = now - brokenAt;
      return elapsed < 0 || elapsed < graceTicks(latencyMillis);
   }
}
