package autismclient.gui.f.c;

public class BitSetUtil extends TableIndex {
   private float sync = 48.0F;

   public BitSetUtil init(float preferredHeight) {
      float next = Math.max(0.0F, preferredHeight);
      if (Float.compare(this.sync, next) != 0) {
         this.sync = next;
         this.load_V();
      }

      return this;
   }

   @Override
   public float init_F_SyncManager_F(SyncManager context, float availableWidth) {
      return context.init_CharEncoder().init_F_F(this.sync);
   }
}
