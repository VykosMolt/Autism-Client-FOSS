package autismclient.gui.ui.widgets;

public final class PillBadgeRenderer {
   private PillBadgeRenderer() {
   }

   public static void init(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String label, boolean selected, boolean hovered) {
      init(context, bounds, label, context.init_TableIndex().colors().accent, selected, hovered);
   }

   public static void init(
      autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String label, int accentColor, boolean selected, boolean hovered
   ) {
      autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().colors();
      float hoverT = autismclient.gui.ui.HoverTransitionCache.init(autismclient.gui.ui.HoverTransitionCache.init(bounds), hovered);
      int border = selected ? accentColor : colors.borderControl;
      int fill = selected ? init(accentColor, 0.12F) : init(colors.cardSurface, colors.cardSurfaceElevated, hoverT);
      int radius = bounds.sync() / 2;
      autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), bounds, radius, fill, border);
      int text = !selected && !hovered ? colors.textSecondary : colors.textPrimary;
      context.init_PoolEntry().drawCentered(context.init_GuiGraphicsExtract(), label == null ? "" : label, bounds, text);
   }

   public static void init(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String label) {
      init(context, bounds, label, false, false);
      autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), bounds, bounds.sync() / 2, 1711276032);
   }

   private static int init(int color, float alpha) {
      int a = Math.round(255.0F * alpha);
      return a << 24 | color & 16777215;
   }

   private static int init(int from, int to, float t) {
      int ar = from >> 16 & 0xFF;
      int ag = from >> 8 & 0xFF;
      int ab = from & 0xFF;
      int br = to >> 16 & 0xFF;
      int bg = to >> 8 & 0xFF;
      int bb = to & 0xFF;
      return 0xFF000000 | Math.round(ar + (br - ar) * t) << 16 | Math.round(ag + (bg - ag) * t) << 8 | Math.round(ab + (bb - ab) * t);
   }
}
