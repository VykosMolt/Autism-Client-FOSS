package autismclient.util;

public final class PlacementTickStamp {
   private static volatile int init = -2147483648;

   private PlacementTickStamp() {
   }

   public static void stamp() {
      init = AutismSessionState.getInstance().getClientTick();
   }

   public static boolean init_Z() {
      return init == AutismSessionState.getInstance().getClientTick();
   }
}
