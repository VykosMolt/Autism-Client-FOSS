package autismclient.ai.print;

final class PrintInteractionReceipt {
   static final long init_J = 10000000000L;
   private Object world;
   private Object player;
   private int after = -1;
   private long load_J;
   private boolean init_Z;

   int init() {
      return this.after;
   }

   void init(Object world, Object player, int before, int after, long now) {
      if (before != after) {
         if (this.after < 0 && after >= 0) {
            this.world = world;
            this.player = player;
            this.after = after;
            this.load_J = now;
         } else {
            this.init_Z = true;
         }
      }
   }

   PrintEnv.Inner_StreamUtil init(Object currentWorld, Object currentPlayer, boolean acknowledged, long now) {
      if (this.init_Z) {
         return PrintEnv.Inner_StreamUtil.UNCERTAIN;
      } else if (this.after < 0) {
         return PrintEnv.Inner_StreamUtil.NONE;
      } else if (this.world != currentWorld || this.player != currentPlayer) {
         return PrintEnv.Inner_StreamUtil.UNCERTAIN;
      } else if (acknowledged) {
         this.after = -1;
         this.world = this.player = null;
         return PrintEnv.Inner_StreamUtil.NONE;
      } else {
         return now - this.load_J >= 10000000000L ? PrintEnv.Inner_StreamUtil.UNCERTAIN : PrintEnv.Inner_StreamUtil.PENDING;
      }
   }
}
