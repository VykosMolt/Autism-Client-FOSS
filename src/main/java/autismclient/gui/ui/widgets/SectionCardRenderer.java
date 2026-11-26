package autismclient.gui.ui.widgets;

public final class SectionCardRenderer {
   private SectionCardRenderer() {
   }

   public static void init(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String title) {
      autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().init_JsonUtil();
      int headerHeight = Math.min(14, Math.max(1, bounds.sync() - 2));
      init(context, bounds, colors.open);
      autismclient.gui.f.NetHelper header = autismclient.gui.f.NetHelper.init(
         bounds.read() + 1, bounds.write() + 1, Math.max(0, bounds.update() - 2), Math.max(0, headerHeight - 1)
      );
      autismclient.gui.f.PacketUtil.read(context.init_GuiGraphicsExtract(), header, 4, colors.flush);
      autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract_2(
         context.init_GuiGraphicsExtract(), bounds.read() + 1, bounds.write() + headerHeight, Math.max(0, bounds.update() - 2), colors.put
      );
      context.init_PoolEntry()
         .load(
            context.init_GuiGraphicsExtract(),
            title == null ? "" : title,
            bounds.read() + 6,
            context.init_PoolEntry().init_I(header),
            Math.max(1, bounds.update() - 12),
            colors.prev
         );
   }

   public static void init(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds) {
      init(context, bounds, context.init_TableIndex().init_JsonUtil().open);
   }

   public static void init(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, int fill) {
      autismclient.gui.f.PacketUtil.read(context.init_GuiGraphicsExtract(), bounds, 5, fill, context.init_TableIndex().init_JsonUtil().get);
   }
}
