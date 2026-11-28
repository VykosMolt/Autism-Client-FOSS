package autismclient.gui.ui.widgets;
import autismclient.gui.f.b.CachePool;

public final class KeybindButtonRenderer {
   public static final int BUTTON_WIDTH = 34;
   public static final int load = 13;

   private KeybindButtonRenderer() {
   }

   public static autismclient.gui.f.NetHelper buttonRect(autismclient.gui.f.NetHelper row, int rightInset, int verticalInset) {
      int height = Math.max(1, row.sync() - verticalInset * 2);
      return autismclient.gui.f.NetHelper.init(row.init() - 34 - rightInset, row.write() + verticalInset, 34, height);
   }

   public static String label(int bindCode, boolean capturing) {
      if (capturing) {
         return "Key";
      } else {
         return bindCode == -1 ? "Bind" : autismclient.util.ValueSink.init_Str(bindCode);
      }
   }

   public static int normalizeKey(int keyCode) {
      return keyCode != 256 && keyCode != 259 && keyCode != 261 ? keyCode : -1;
   }

   public static void draw(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, int bindCode, boolean capturing, boolean hovered) {
      CachePool.init(context, bounds, label(bindCode, capturing), capturing ? CachePool.Inner_DataUtil.read : CachePool.Inner_DataUtil.init, hovered, capturing);
   }
}
