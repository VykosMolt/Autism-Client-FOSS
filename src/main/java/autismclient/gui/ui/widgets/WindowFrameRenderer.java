package autismclient.gui.ui.widgets;

public final class WindowFrameRenderer {
   private WindowFrameRenderer() {
   }

   public static autismclient.gui.f.NetHelper clampBounds(autismclient.gui.f.NetHelper wanted, int screenWidth, int screenHeight, int minWidth, int minHeight) {
      int width = Math.max(Math.min(wanted.update(), Math.max(1, screenWidth - 8)), Math.min(minWidth, Math.max(1, screenWidth - 8)));
      int height = Math.max(Math.min(wanted.sync(), Math.max(1, screenHeight - 8)), Math.min(minHeight, Math.max(1, screenHeight - 8)));
      return autismclient.gui.f.NetHelper.init(wanted.read(), wanted.write(), width, height).init_NetHelper(screenWidth - 4, screenHeight - 4);
   }

   public static void init(
      autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String title, boolean collapsed, boolean close, boolean headerHovered
   ) {
      init(context, bounds, title, collapsed, close, headerHovered, true);
   }

   public static void init(
      autismclient.gui.f.SyncManager context,
      autismclient.gui.f.NetHelper bounds,
      String title,
      boolean collapsed,
      boolean close,
      boolean headerHovered,
      boolean active
   ) {
      init(context, bounds, title, collapsed, true, close, headerHovered, active);
   }

   public static void init(
      autismclient.gui.f.SyncManager context,
      autismclient.gui.f.NetHelper bounds,
      String title,
      boolean collapsed,
      boolean collapse,
      boolean close,
      boolean headerHovered,
      boolean active
   ) {
      init(context, bounds, title, collapsed, collapse, close, headerHovered, active, 4, 4);
   }

   public static void init(
      autismclient.gui.f.SyncManager context,
      autismclient.gui.f.NetHelper bounds,
      String title,
      boolean collapsed,
      boolean collapse,
      boolean close,
      boolean headerHovered,
      boolean active,
      int titleLeftInset,
      int titleRightInset
   ) {
      init(context, bounds, title, collapsed, collapse, close, headerHovered, active, titleLeftInset, titleRightInset, 14);
   }

   public static void init(
      autismclient.gui.f.SyncManager context,
      autismclient.gui.f.NetHelper bounds,
      String title,
      boolean collapsed,
      boolean collapse,
      boolean close,
      boolean headerHovered,
      boolean active,
      int titleLeftInset,
      int titleRightInset,
      int headerHeight
   ) {
      autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().colors();
      autismclient.gui.f.PacketUtil.init_V_SyncManager(context, bounds, 8, colors.windowSurface, colors.borderSubtle);
      WindowHeaderHelper.render(context, bounds, headerHeight, title, collapsed, collapse, close, headerHovered, active, titleLeftInset, titleRightInset);
   }
}
