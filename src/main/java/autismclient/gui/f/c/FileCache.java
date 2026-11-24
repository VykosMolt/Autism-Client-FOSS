package autismclient.gui.f.c;

public class FileCache extends TableIndex {
   public FileCache(float width, float height) {
      this.read_F = Math.max(0.0F, width);
      this.write = Math.max(0.0F, height);
   }

   @Override
   public float init_F_SyncManager(SyncManager context) {
      return context.init_CharEncoder().init_F_F(this.read_F);
   }

   @Override
   public float init_F_SyncManager_F(SyncManager context, float availableWidth) {
      return context.init_CharEncoder().init_F_F(this.write);
   }
}
