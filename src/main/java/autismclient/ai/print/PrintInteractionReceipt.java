package autismclient.ai.print;

final class PrintInteractionReceipt {
   static final long init_J = 10000000000L;
   private Object init_Obj;
   private Object load_Obj;
   private int init_I = -1;
   private long load_J;
   private boolean init_Z;

   int init() {
      return this.init_I;
   }

   void init(Object world, Object player, int before, int after, long now) {
      if (before != after) {
         if (this.init_I < 0 && after >= 0) {
            this.init_Obj = world;
            this.load_Obj = player;
            this.init_I = after;
            this.load_J = now;
         } else {
            this.init_Z = true;
         }
      }
   }

   PrintEnv.Inner_StreamUtil init(Object currentWorld, Object currentPlayer, boolean acknowledged, long now) {
      if (this.init_Z) {
         return PrintEnv.Inner_StreamUtil.UNCERTAIN;
      } else if (this.init_I < 0) {
         return PrintEnv.Inner_StreamUtil.NONE;
      } else if (this.init_Obj != currentWorld || this.load_Obj != currentPlayer) {
         return PrintEnv.Inner_StreamUtil.UNCERTAIN;
      } else if (acknowledged) {
         this.init_I = -1;
         this.init_Obj = this.load_Obj = null;
         return PrintEnv.Inner_StreamUtil.NONE;
      } else {
         return now - this.load_J >= 10000000000L ? PrintEnv.Inner_StreamUtil.UNCERTAIN : PrintEnv.Inner_StreamUtil.PENDING;
      }
   }
}
