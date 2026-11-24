package autismclient.ai.b;

final class TableIndex {
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

   EventBus.Inner_StreamUtil init(Object currentWorld, Object currentPlayer, boolean acknowledged, long now) {
      if (this.init_Z) {
         return EventBus.Inner_StreamUtil.read;
      } else if (this.init_I < 0) {
         return EventBus.Inner_StreamUtil.init;
      } else if (this.init_Obj != currentWorld || this.load_Obj != currentPlayer) {
         return EventBus.Inner_StreamUtil.read;
      } else if (acknowledged) {
         this.init_I = -1;
         this.init_Obj = this.load_Obj = null;
         return EventBus.Inner_StreamUtil.init;
      } else {
         return now - this.load_J >= 10000000000L ? EventBus.Inner_StreamUtil.read : EventBus.Inner_StreamUtil.load;
      }
   }
}
