package autismclient.util;

public final class BlockSequenceSyncTracker {
   private Object init_Obj;
   private int init_I;
   private boolean init_Z;

   public void init(Object world, int sequence) {
      this.init_Obj = world;
      this.init_I = sequence;
      this.init_Z = true;
   }

   public void init(Object world, boolean expectedBlock) {
      if (this.init_Obj == world && expectedBlock) {
         this.init_Z = false;
      }
   }

   public void load(Object world, int sequence) {
      if (this.init_Obj == world && sequence - this.init_I >= 0) {
         this.init_Z = false;
      }
   }

   public boolean init(Object world) {
      return this.init_Z && this.init_Obj == world;
   }

   public void init() {
      this.init_Z = false;
      this.init_Obj = null;
   }
}
