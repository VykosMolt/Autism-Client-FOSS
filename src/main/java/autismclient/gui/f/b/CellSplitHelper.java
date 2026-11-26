package autismclient.gui.f.b;

public final class CellSplitHelper {
   private CellSplitHelper() {
   }

   public static autismclient.gui.f.NetHelper init(autismclient.gui.f.NetHelper row, int count, int index, int gap) {
      return GridRowSplitter.init(row, count, index, gap);
   }
}
