package autismclient.gui.f.b;

public final class GridRowSplitter {
   private GridRowSplitter() {
   }

   public static autismclient.gui.f.NetHelper init(autismclient.gui.f.NetHelper row, int count, int index, int gap) {
      int safeCount = Math.max(1, count);
      int safeGap = Math.max(0, gap);
      int width = Math.max(1, (row.update() - safeGap * (safeCount - 1)) / safeCount);
      int x = row.read() + index * (width + safeGap);
      int resolvedWidth = index == safeCount - 1 ? Math.max(1, row.init() - x) : width;
      return autismclient.gui.f.NetHelper.init(x, row.write(), resolvedWidth, row.sync());
   }
}
