package autismclient.gui.f.b;

public final class StreamGate {
   private StreamGate() {
   }

   public static int init(int scroll, int contentHeight, int viewHeight) {
      return Math.max(0, Math.min(scroll, Math.max(0, contentHeight - Math.max(0, viewHeight))));
   }

   public static int load(int scroll, int rowHeight, int count) {
      return count > 0 && rowHeight > 0 ? Math.max(0, Math.min(count - 1, scroll / rowHeight)) : 0;
   }

   public static int init(int scroll, int viewHeight, int rowHeight, int count) {
      return count > 0 && rowHeight > 0 ? Math.max(0, Math.min(count - 1, (scroll + Math.max(0, viewHeight) + rowHeight - 1) / rowHeight)) : -1;
   }

   public static boolean init(autismclient.gui.f.NetHelper viewport, int mouseX, int mouseY) {
      return viewport != null && viewport.init_Z(mouseX, mouseY);
   }
}
