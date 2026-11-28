package autismclient.gui.ui.widgets;

public final class ScrollbarContentMath {
   public static final int init = 4;
   private static final int load = 3;

   private ScrollbarContentMath() {
   }

   public static int maxTextWidth(int viewportWidth, int innerPadding) {
      return Math.max(22, viewportWidth - innerPadding * 2 - 4 - 3);
   }

   public static int clampOffset(int contentWidth, int wantedOffset) {
      return Math.max(0, Math.min(wantedOffset, Math.max(0, contentWidth - 1)));
   }

   public static int availableWidth(int contentWidth, int offset) {
      return Math.max(1, contentWidth - clampOffset(contentWidth, offset));
   }
}
