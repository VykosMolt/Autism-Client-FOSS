package autismclient.gui.ui.widgets;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class DialogFrameRenderer {
   private DialogFrameRenderer() {
   }

   public static void draw(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, int headerHeight, String title, boolean headerHovered) {
      GuiGraphicsExtractor graphics = context.init_GuiGraphicsExtract();
      autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().colors();
      autismclient.gui.f.PacketUtil.read(graphics, bounds, 8, colors.windowSurface, colors.borderSubtle);
      WindowHeaderHelper.init(context, bounds, headerHeight, title, false, false, true, headerHovered, true, 4, 4);
   }

   public static autismclient.gui.f.NetHelper headerBounds(autismclient.gui.f.NetHelper bounds, int headerHeight) {
      return WindowHeaderHelper.load(bounds, headerHeight);
   }

   public static boolean isOverHeader(autismclient.gui.f.NetHelper bounds, int headerHeight, int mouseX, int mouseY) {
      return headerBounds(bounds, headerHeight).init_Z(mouseX, mouseY);
   }
}
