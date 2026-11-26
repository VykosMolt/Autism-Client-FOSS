package autismclient.util;

public final class BlockBreakGraceCalc {
   private BlockBreakGraceCalc() {
   }

   public static int init(int latencyMillis) {
      return 16 + Math.min(44, Math.max(0, latencyMillis) / 50);
   }

   public static boolean init(int now, int brokenAt, int latencyMillis) {
      int elapsed = now - brokenAt;
      return elapsed < 0 || elapsed < init(latencyMillis);
   }
}
