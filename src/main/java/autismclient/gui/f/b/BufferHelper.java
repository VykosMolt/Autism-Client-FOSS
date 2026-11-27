package autismclient.gui.f.b;
import autismclient.gui.ui.widgets.UiLayoutMath;

public final class BufferHelper {
   private BufferHelper() {
   }

   public static void init(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds) {
      init_V(context, bounds, null);
   }

   public static void init_V(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String title) {
      autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().colors();
      autismclient.gui.f.PacketUtil.read(context.init_GuiGraphicsExtract(), bounds, 5, colors.cardSurface, colors.borderSubtle);
      if (title != null && !title.isBlank()) {
         autismclient.gui.f.NetHelper header = init(bounds);
         context.init_PoolEntry()
            .drawFitted(
               context.init_GuiGraphicsExtract(),
               title,
               bounds.read() + 5,
               context.init_PoolEntry().centerTextY(header),
               Math.max(1, bounds.update() - 10),
               colors.textSecondary
            );
         int lineY = header.load();
         autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract_2(
            context.init_GuiGraphicsExtract(), bounds.read() + 4, lineY, Math.max(1, bounds.update() - 8), colors.divider
         );
      }
   }

   public static void init(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String key, boolean active, boolean hovered) {
      autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().colors();
      float hover = autismclient.gui.ui.HoverTransitionCache.sample(key, hovered);
      int fill = UiLayoutMath.lerpColor(colors.cardSurface, colors.cardSurfaceElevated, hover);
      if (active) {
         fill = UiLayoutMath.lerpColor(fill, colors.success, 0.09F);
         int rim = UiLayoutMath.lerpColor(fill, colors.success, 0.65F);
         autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), bounds, 5, fill, rim);
      } else {
         autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), bounds, 5, fill);
      }
   }

   public static void init(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, float alpha) {
      autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().colors();
      autismclient.gui.f.PacketUtil.read(
         context.init_GuiGraphicsExtract(),
         bounds,
         5,
         autismclient.gui.f.PacketUtil.init(colors.cardSurface, alpha),
         autismclient.gui.f.PacketUtil.init(colors.borderSubtle, alpha)
      );
   }

   public static int init_I(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String title) {
      return title != null && !title.isBlank() ? init(bounds).load() + 2 : bounds.write() + 4;
   }

   private static autismclient.gui.f.NetHelper init(autismclient.gui.f.NetHelper bounds) {
      return autismclient.gui.f.NetHelper.init(bounds.read(), bounds.write(), bounds.update(), autismclient.gui.ui.UiDensityMetrics.scaledHeight(14));
   }
}
