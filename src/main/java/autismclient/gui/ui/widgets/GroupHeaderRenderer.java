package autismclient.gui.ui.widgets;

import java.util.Locale;

public final class GroupHeaderRenderer {
   private GroupHeaderRenderer() {
   }

   public static void init(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String title, boolean collapsed, boolean hovered) {
      autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().colors();
      autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), bounds, 3, colors.cardSurface);
      init(context, bounds, hovered);
      TitleBarRenderer.init(context, autismclient.gui.f.NetHelper.init(bounds.read() + 5, bounds.write() + Math.max(0, (bounds.sync() - 8) / 2), 8, 8), collapsed, colors.textPrimary);
      context.init_PoolEntry()
         .drawFitted(
            context.init_GuiGraphicsExtract(),
            init(title).toUpperCase(Locale.ROOT),
            bounds.read() + 17,
            context.init_PoolEntry().centerTextY(bounds),
            Math.max(1, bounds.update() - 22),
            colors.textSecondary
         );
   }

   public static void load(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String title, boolean collapsed, boolean hovered) {
      autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().colors();
      autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), bounds, 3, colors.cardSurface);
      init(context, bounds, hovered);
      TitleBarRenderer.init(context, autismclient.gui.f.NetHelper.init(bounds.read() + 5, bounds.write() + Math.max(0, (bounds.sync() - 8) / 2), 8, 8), collapsed, colors.textPrimary);
      context.init_PoolEntry()
         .drawFitted(
            context.init_GuiGraphicsExtract(),
            init(title),
            bounds.read() + 18,
            context.init_PoolEntry().centerTextY(bounds),
            Math.max(1, bounds.update() - 22),
            colors.textPrimary
         );
   }

   private static void init(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, boolean hovered) {
      float hoverT = autismclient.gui.ui.HoverTransitionCache.sample(autismclient.gui.ui.HoverTransitionCache.boundsKey(bounds), hovered);
      if (hoverT > 0.001F) {
         autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), bounds, 3, Math.round(20.0F * hoverT) << 24 | 16777215);
      }
   }

   private static String init(String value) {
      return value == null ? "" : value;
   }
}
