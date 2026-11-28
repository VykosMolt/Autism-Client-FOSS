package autismclient.protector;

public final class PackFailureGuard {
   private static volatile long init;

   private PackFailureGuard() {
   }

   public static void init_V() {
      init = Math.max(init, System.currentTimeMillis() + 15000L);
   }

   public static boolean isBlocked() {
      return System.currentTimeMillis() < init;
   }

   public static void reset() {
      init = 0L;
   }
}
