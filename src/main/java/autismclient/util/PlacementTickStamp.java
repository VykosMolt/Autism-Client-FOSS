package autismclient.util;

public final class PlacementTickStamp {
   private static volatile int stampTick = -2147483648;

   private PlacementTickStamp() {
   }

   public static void stamp() {
      stampTick = AutismSessionState.getInstance().getClientTick();
   }

   public static boolean isThisTick() {
      return stampTick == AutismSessionState.getInstance().getClientTick();
   }
}
