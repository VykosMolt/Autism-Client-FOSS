package autismclient.gui.ui.widgets;
import autismclient.gui.f.b.CachePool;

public final class TabStripRenderer {
   private TabStripRenderer() {
   }

   public static autismclient.gui.f.NetHelper tabBounds(autismclient.gui.f.NetHelper strip, int count, int index, int gap) {
      int safeCount = Math.max(1, count);
      int safeGap = Math.max(0, gap);
      int width = Math.max(1, (strip.update() - safeGap * (safeCount - 1)) / safeCount);
      int x = strip.read() + index * (width + safeGap);
      int resolvedWidth = index == safeCount - 1 ? Math.max(1, strip.init() - x) : width;
      return autismclient.gui.f.NetHelper.init(x, strip.write(), resolvedWidth, strip.sync());
   }

   public static void drawTab(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String label, boolean active) {
      CachePool.init(
         context, bounds, label, active ? CachePool.Inner_DataUtil.read : CachePool.Inner_DataUtil.init, bounds.init_Z(context.read(), context.write()), active
      );
   }
}
