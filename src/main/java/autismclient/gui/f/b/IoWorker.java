package autismclient.gui.f.b;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class IoWorker {
   private IoWorker() {
   }

   public static void init(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, int headerHeight, String title, boolean headerHovered) {
      GuiGraphicsExtractor graphics = context.init_GuiGraphicsExtract();
      autismclient.gui.f.JsonUtil colors = context.init_TableIndex().init_JsonUtil();
      autismclient.gui.f.PacketUtil.read(graphics, bounds, 8, colors.update, colors.get);
      ClassCache.init(context, bounds, headerHeight, title, false, false, true, headerHovered, true, 4, 4);
   }

   public static autismclient.gui.f.NetHelper init(autismclient.gui.f.NetHelper bounds, int headerHeight) {
      return ClassCache.load(bounds, headerHeight);
   }

   public static boolean init(autismclient.gui.f.NetHelper bounds, int headerHeight, int mouseX, int mouseY) {
      return init(bounds, headerHeight).init_Z(mouseX, mouseY);
   }
}
