package autismclient.gui.ui.widgets;

public final class WindowHeaderHelper {
   private WindowHeaderHelper() {
   }

   public static autismclient.gui.f.NetHelper init(autismclient.gui.f.NetHelper windowBounds, int headerHeight) {
      int height = Math.max(1, Math.min(Math.max(1, headerHeight) - 2, Math.max(1, windowBounds.sync() - 2)));
      return autismclient.gui.f.NetHelper.init(windowBounds.read() + 1, windowBounds.write() + 1, Math.max(0, windowBounds.update() - 2), height);
   }

   public static autismclient.gui.f.NetHelper load(autismclient.gui.f.NetHelper windowBounds, int headerHeight) {
      return TitleBarRenderer.load(init(windowBounds, headerHeight));
   }

   public static autismclient.gui.f.NetHelper read(autismclient.gui.f.NetHelper windowBounds, int headerHeight) {
      return TitleBarRenderer.init(init(windowBounds, headerHeight));
   }

   public static boolean init(autismclient.gui.f.NetHelper windowBounds, int headerHeight, double mouseX, double mouseY) {
      return load(windowBounds, headerHeight).init_Z((int)mouseX, (int)mouseY);
   }

   public static boolean load(autismclient.gui.f.NetHelper windowBounds, int headerHeight, double mouseX, double mouseY) {
      return read(windowBounds, headerHeight).init_Z((int)mouseX, (int)mouseY);
   }

   public static boolean read(autismclient.gui.f.NetHelper windowBounds, int headerHeight, double mouseX, double mouseY) {
      autismclient.gui.f.NetHelper header = autismclient.gui.f.NetHelper.init(
         windowBounds.read(), windowBounds.write(), windowBounds.update(), Math.max(1, headerHeight)
      );
      return header.init_Z((int)mouseX, (int)mouseY);
   }

   public static boolean init(autismclient.gui.f.NetHelper windowBounds, int headerHeight, boolean hasCollapse, boolean hasClose, double mouseX, double mouseY) {
      if (!read(windowBounds, headerHeight, mouseX, mouseY)) {
         return false;
      } else {
         return hasClose && init(windowBounds, headerHeight, mouseX, mouseY) ? false : !hasCollapse || !load(windowBounds, headerHeight, mouseX, mouseY);
      }
   }

   public static void init(
      autismclient.gui.f.SyncManager context,
      autismclient.gui.f.NetHelper windowBounds,
      int headerHeight,
      String title,
      boolean collapsed,
      boolean hasCollapse,
      boolean hasClose,
      boolean headerHovered,
      boolean active,
      int titleLeftInset,
      int titleRightInset
   ) {
      autismclient.gui.f.NetHelper bar = init(windowBounds, headerHeight);
      boolean shapeCollapsed = windowBounds.sync() <= headerHeight + 1;
      TitleBarRenderer.init(context, bar, title, collapsed, shapeCollapsed, hasCollapse, hasClose, active && headerHovered, titleLeftInset, titleRightInset);
   }
}
