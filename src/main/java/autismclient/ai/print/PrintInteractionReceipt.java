package autismclient.ai.print;

final class PrintInteractionReceipt {
   static final long init_J = 10000000000L;
   private Object world;
   private Object player;
   private int after = -1;
   private long load_J;
   private boolean completed;

   int afterSequence() {
      return this.after;
   }

   void recordInteraction(Object world, Object player, int before, int after, long now) {
      if (before != after) {
         if (this.after < 0 && after >= 0) {
            this.world = world;
            this.player = player;
            this.after = after;
            this.load_J = now;
         } else {
            this.completed = true;
         }
      }
   }

   PrintEnv.ReceiptState receiptState(Object currentWorld, Object currentPlayer, boolean acknowledged, long now) {
      if (this.completed) {
         return PrintEnv.ReceiptState.UNCERTAIN;
      } else if (this.after < 0) {
         return PrintEnv.ReceiptState.NONE;
      } else if (this.world != currentWorld || this.player != currentPlayer) {
         return PrintEnv.ReceiptState.UNCERTAIN;
      } else if (acknowledged) {
         this.after = -1;
         this.world = this.player = null;
         return PrintEnv.ReceiptState.NONE;
      } else {
         return now - this.load_J >= 10000000000L ? PrintEnv.ReceiptState.UNCERTAIN : PrintEnv.ReceiptState.PENDING;
      }
   }
}
