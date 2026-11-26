package autismclient.gui.ui.widgets;

public final class ScrollbarContentMath {
   public static final int init = 4;
   private static final int load = 3;

   private ScrollbarContentMath() {
   }

   public static int init(int viewportWidth, int innerPadding) {
      return Math.max(22, viewportWidth - innerPadding * 2 - 4 - 3);
   }

   public static int load(int contentWidth, int wantedOffset) {
      return Math.max(0, Math.min(wantedOffset, Math.max(0, contentWidth - 1)));
   }

   public static int read(int contentWidth, int offset) {
      return Math.max(1, contentWidth - load(contentWidth, offset));
   }
}
