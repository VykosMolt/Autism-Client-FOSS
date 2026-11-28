package autismclient.gui.ui.widgets;

public final class RectSplitHelper {
   private RectSplitHelper() {
   }

   public static autismclient.gui.f.NetHelper cell(autismclient.gui.f.NetHelper row, int count, int index, int gap) {
      return GridRowSplitter.cell(row, count, index, gap);
   }

   public static autismclient.gui.f.NetHelper inset(autismclient.gui.f.NetHelper bounds, int horizontal, int vertical) {
      return bounds == null
         ? autismclient.gui.f.NetHelper.init(0, 0, 0, 0)
         : autismclient.gui.f.NetHelper.init(
            bounds.read() + Math.max(0, horizontal),
            bounds.write() + Math.max(0, vertical),
            Math.max(0, bounds.update() - Math.max(0, horizontal) * 2),
            Math.max(0, bounds.sync() - Math.max(0, vertical) * 2)
         );
   }
}
