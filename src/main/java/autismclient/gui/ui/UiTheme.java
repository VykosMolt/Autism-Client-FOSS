package autismclient.gui.ui;

public final class UiTheme {
   private final UiColorScheme colors = new UiColorScheme();
   private final UiSpacingMetrics spacing = new UiSpacingMetrics();
   private final UiTypographyMetrics typography = new UiTypographyMetrics();

   public UiColorScheme colors() {
      return this.colors;
   }

   public void refresh() {
      this.colors.refresh();
   }

   public UiSpacingMetrics spacing() {
      return this.spacing;
   }

   public UiTypographyMetrics typography() {
      return this.typography;
   }
}
