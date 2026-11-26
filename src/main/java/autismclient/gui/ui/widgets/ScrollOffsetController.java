package autismclient.gui.ui.widgets;

public final class ScrollOffsetController {
   private int init = 0;

   public void init(int offset) {
      this.init = Math.max(0, offset);
   }

   public void init_V_I_I(int offset, int maxScroll) {
      this.init = init_I_I_I(offset, maxScroll);
   }

   public void load(int offset, int maxScroll) {
      this.init = init_I_I_I(offset, maxScroll);
   }

   public void init_V_D(double amount, float stepPixels, int maxScroll) {
      this.init_V_I_I(this.init - Math.round((float)amount * stepPixels), maxScroll);
   }

   public void init_V_PoolEntry_DataUtil(ScrollbarMetricsHelper.Metrics metrics, double mouseY, int grabOffset) {
      if (metrics == null) {
         this.load(0, 0);
      } else {
         this.init_V_I_I(ScrollbarMetricsHelper.init(metrics, mouseY, grabOffset), metrics.flush());
      }
   }

   public void init(ScrollbarMetricsHelper.Metrics metrics, double mouseY, int grabOffset, int stepSize) {
      if (metrics == null) {
         this.load(0, 0);
      } else {
         int rawScroll = ScrollbarMetricsHelper.init(metrics, mouseY, grabOffset);
         int steppedScroll = rawScroll / stepSize * stepSize;
         this.init_V_I_I(steppedScroll, metrics.flush());
      }
   }

   public int init_I_F_I(float delta, int maxScroll) {
      this.init = init_I_I_I(this.init, maxScroll);
      return this.init;
   }

   public int init() {
      return this.init;
   }

   public int load() {
      return this.init;
   }

   private static int init_I_I_I(int value, int max) {
      return Math.max(0, Math.min(max, value));
   }
}
