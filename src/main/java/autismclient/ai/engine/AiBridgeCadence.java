package autismclient.ai.engine;

public final class AiBridgeCadence {
   private AiBridgeCadence() {
   }

   public static boolean shouldDelayBridge(boolean onGround, boolean feetEnterJustPlaced, boolean justPlacedClientSolid) {
      return onGround && feetEnterJustPlaced && !justPlacedClientSolid;
   }
}
