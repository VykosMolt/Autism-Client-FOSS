package autismclient.gui.b;

public final class NetHelper {
   public final SyncManager init_SyncManager = new SyncManager();
   public int init_I = -1;
   public int load = -1;
   public int read = -1;
   private String init_Str = "";

   public void init(String typeId) {
      String t = typeId == null ? "" : typeId;
      if (!t.equals(this.init_Str)) {
         this.init_Str = t;
         if (!t.endsWith("anvil")) {
            this.init_SyncManager.load();
         }

         this.init_I = -1;
         this.load = -1;
         this.read = -1;
      }
   }
}
