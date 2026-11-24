package autismclient.ai.a;

public final class TaskQueue {
   private TaskQueue() {
   }

   public static boolean init(boolean onGround, boolean feetEnterJustPlaced, boolean justPlacedClientSolid) {
      return onGround && feetEnterJustPlaced && !justPlacedClientSolid;
   }
}
