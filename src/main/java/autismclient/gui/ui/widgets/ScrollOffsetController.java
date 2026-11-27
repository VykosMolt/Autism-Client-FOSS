package autismclient.gui.ui.widgets;

public final class ScrollOffsetController {
   private int offset = 0;

   public void setOffsetUnclamped(int offset) {
      this.offset = Math.max(0, offset);
   }

   public void setOffset(int offset, int maxScroll) {
      this.offset = clampOffset(offset, maxScroll);
   }

   public void jumpTo(int offset, int maxScroll) {
      this.offset = clampOffset(offset, maxScroll);
   }

   public void scrollByWheel(double amount, float stepPixels, int maxScroll) {
      this.setOffset(this.offset - Math.round((float)amount * stepPixels), maxScroll);
   }

   public void dragToMouseY(ScrollbarMetricsHelper.Metrics metrics, double mouseY, int grabOffset) {
      if (metrics == null) {
         this.jumpTo(0, 0);
      } else {
         this.setOffset(ScrollbarMetricsHelper.scrollOffsetForMouseY(metrics, mouseY, grabOffset), metrics.maxScroll());
      }
   }

   public void dragToMouseYStepped(ScrollbarMetricsHelper.Metrics metrics, double mouseY, int grabOffset, int stepSize) {
      if (metrics == null) {
         this.jumpTo(0, 0);
      } else {
         int rawScroll = ScrollbarMetricsHelper.scrollOffsetForMouseY(metrics, mouseY, grabOffset);
         int steppedScroll = rawScroll / stepSize * stepSize;
         this.setOffset(steppedScroll, metrics.maxScroll());
      }
   }

   public int tickAndClamp(float delta, int maxScroll) {
      this.offset = clampOffset(this.offset, maxScroll);
      return this.offset;
   }

   public int getOffset() {
      return this.offset;
   }

   public int getTargetOffset() {
      return this.offset;
   }

   private static int clampOffset(int value, int max) {
      return Math.max(0, Math.min(max, value));
   }
}
