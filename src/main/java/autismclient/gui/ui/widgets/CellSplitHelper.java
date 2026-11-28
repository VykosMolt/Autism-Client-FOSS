package autismclient.gui.ui.widgets;

public final class CellSplitHelper {
   private CellSplitHelper() {
   }

   public static autismclient.gui.f.NetHelper cell(autismclient.gui.f.NetHelper row, int count, int index, int gap) {
      return GridRowSplitter.cell(row, count, index, gap);
   }
}
