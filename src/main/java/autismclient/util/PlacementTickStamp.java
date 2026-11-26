package autismclient.util;

public final class PlacementTickStamp {
   private static volatile int init = -2147483648;

   private PlacementTickStamp() {
   }

   public static void init_V() {
      init = AutismSessionState.init_ck().state_I();
   }

   public static boolean init_Z() {
      return init == AutismSessionState.init_ck().state_I();
   }
}
