package autismclient.gui.f.b;

public final class BufferHelper {
   private BufferHelper() {
   }

   public static void init(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds) {
      init_V(context, bounds, null);
   }

   public static void init_V(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String title) {
      autismclient.gui.f.JsonUtil colors = context.init_TableIndex().init_JsonUtil();
      autismclient.gui.f.PacketUtil.read(context.init_GuiGraphicsExtract(), bounds, 5, colors.open, colors.get);
      if (title != null && !title.isBlank()) {
         autismclient.gui.f.NetHelper header = init(bounds);
         context.init_PoolEntry()
            .load(
               context.init_GuiGraphicsExtract(),
               title,
               bounds.read() + 5,
               context.init_PoolEntry().init_I(header),
               Math.max(1, bounds.update() - 10),
               colors.valid
            );
         int lineY = header.load();
         autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract_2(
            context.init_GuiGraphicsExtract(), bounds.read() + 4, lineY, Math.max(1, bounds.update() - 8), colors.put
         );
      }
   }

   public static void init(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String key, boolean active, boolean hovered) {
      autismclient.gui.f.JsonUtil colors = context.init_TableIndex().init_JsonUtil();
      float hover = autismclient.gui.f.DataUtil.init(key, hovered);
      int fill = j.init_I_I(colors.open, colors.push, hover);
      if (active) {
         fill = j.init_I_I(fill, colors.stop, 0.09F);
         int rim = j.init_I_I(fill, colors.stop, 0.65F);
         autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), bounds, 5, fill, rim);
      } else {
         autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), bounds, 5, fill);
      }
   }

   public static void init(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, float alpha) {
      autismclient.gui.f.JsonUtil colors = context.init_TableIndex().init_JsonUtil();
      autismclient.gui.f.PacketUtil.read(
         context.init_GuiGraphicsExtract(),
         bounds,
         5,
         autismclient.gui.f.PacketUtil.init(colors.open, alpha),
         autismclient.gui.f.PacketUtil.init(colors.get, alpha)
      );
   }

   public static int init_I(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String title) {
      return title != null && !title.isBlank() ? init(bounds).load() + 2 : bounds.write() + 4;
   }

   private static autismclient.gui.f.NetHelper init(autismclient.gui.f.NetHelper bounds) {
      return autismclient.gui.f.NetHelper.init(bounds.read(), bounds.write(), bounds.update(), autismclient.gui.f.RenderState.init(14));
   }
}
