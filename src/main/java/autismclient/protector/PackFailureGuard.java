package autismclient.protector;

public final class PackFailureGuard {
   private static volatile long suppressUntilMs;

   private PackFailureGuard() {
   }

   public static void suppressBriefly() {
      suppressUntilMs = Math.max(suppressUntilMs, System.currentTimeMillis() + 15000L);
   }

   public static boolean isBlocked() {
      return System.currentTimeMillis() < suppressUntilMs;
   }

   public static void reset() {
      suppressUntilMs = 0L;
   }
}
