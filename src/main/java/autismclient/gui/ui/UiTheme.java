package autismclient.gui.ui;

public final class UiTheme {
   private final UiColorScheme init_JsonUtil = new UiColorScheme();
   private final UiSpacingMetrics init_IoWorker = new UiSpacingMetrics();
   private final UiTypographyMetrics init_BitSetUtil = new UiTypographyMetrics();

   public UiColorScheme init_JsonUtil() {
      return this.init_JsonUtil;
   }

   public void init_V() {
      this.init_JsonUtil.init();
   }

   public UiSpacingMetrics init_IoWorker() {
      return this.init_IoWorker;
   }

   public UiTypographyMetrics init_BitSetUtil() {
      return this.init_BitSetUtil;
   }
}
