package autismclient.gui.ui.widgets;

public final class ScrollbarRenderer {
   private static final int init = 3;
   private static final int load = -6578008;
   private static final int read = -3551788;

   private ScrollbarRenderer() {
   }

   public static ScrollbarRenderer.ScrollbarBounds init(autismclient.gui.f.NetHelper track, int contentHeight, int viewHeight, int scroll) {
      int max = Math.max(0, contentHeight - Math.max(0, viewHeight));
      if (max > 0 && track.sync() > 0) {
         int thumbH = Math.max(12, (int)Math.round(track.sync() * ((double)viewHeight / Math.max(viewHeight, contentHeight))));
         thumbH = Math.min(track.sync(), thumbH);
         int thumbY = track.write() + (int)Math.round((track.sync() - thumbH) * ((double)scroll / max));
         return new ScrollbarRenderer.ScrollbarBounds(track, autismclient.gui.f.NetHelper.init(track.read(), thumbY, track.update(), thumbH), max);
      } else {
         return new ScrollbarRenderer.ScrollbarBounds(track, autismclient.gui.f.NetHelper.init(track.read(), track.write(), track.update(), track.sync()), 0);
      }
   }

   public static void init_V_SyncManager(autismclient.gui.f.SyncManager context, ScrollbarRenderer.ScrollbarBounds metrics, boolean dragging) {
      init(context, metrics, false, dragging);
   }

   public static void init(autismclient.gui.f.SyncManager context, ScrollbarRenderer.ScrollbarBounds metrics, boolean hovered, boolean dragging) {
      if (metrics != null && metrics.init_I() > 0) {
         autismclient.gui.f.NetHelper track = metrics.init_NetHelper();
         int trackWidth = Math.min(3, track.update());
         int trackX = track.read() + (track.update() - trackWidth) / 2;
         autismclient.gui.f.PacketUtil.load(
            context.init_GuiGraphicsExtract(),
            autismclient.gui.f.NetHelper.init(trackX, track.write(), trackWidth, track.sync()),
            (trackWidth + 1) / 2,
            context.init_TableIndex().colors().divider
         );
         autismclient.gui.f.NetHelper thumb = metrics.load();
         float activeT = autismclient.gui.ui.HoverTransitionCache.init(autismclient.gui.ui.HoverTransitionCache.init(track), hovered || dragging);
         int restWidth = Math.max(2, thumb.update() - 2);
         int width = Math.round(restWidth + (thumb.update() - restWidth) * activeT);
         int x = thumb.read() + (thumb.update() - width) / 2;
         autismclient.gui.f.PacketUtil.load(
            context.init_GuiGraphicsExtract(),
            autismclient.gui.f.NetHelper.init(x, thumb.write(), width, thumb.sync()),
            (width + 1) / 2,
            init_I_I(-6578008, -3551788, activeT)
         );
      }
   }

   public static int init_I_UtilCore_DataUtil(ScrollbarRenderer.ScrollbarBounds metrics, int mouseY, int grabOffset) {
      if (metrics != null && metrics.init_I() > 0) {
         int available = Math.max(1, metrics.init_NetHelper().sync() - metrics.load().sync());
         int y = Math.max(0, Math.min(available, mouseY - grabOffset - metrics.init_NetHelper().write()));
         return (int)Math.round(metrics.init_I() * ((double)y / available));
      } else {
         return 0;
      }
   }

   private static int init_I_I(int from, int to, float t) {
      int ar = from >> 16 & 0xFF;
      int ag = from >> 8 & 0xFF;
      int ab = from & 0xFF;
      int aa = from >>> 24 & 0xFF;
      int br = to >> 16 & 0xFF;
      int bg = to >> 8 & 0xFF;
      int bb = to & 0xFF;
      int ba = to >>> 24 & 0xFF;
      int r = Math.round(ar + (br - ar) * t);
      int g = Math.round(ag + (bg - ag) * t);
      int b = Math.round(ab + (bb - ab) * t);
      int a = Math.round(aa + (ba - aa) * t);
      return a << 24 | r << 16 | g << 8 | b;
   }

   public record ScrollbarBounds(autismclient.gui.f.NetHelper init_NetHelper, autismclient.gui.f.NetHelper load, int init_I) {
      public boolean init(int x, int y) {
         return this.init_NetHelper.init_Z(x, y);
      }

      public boolean load(int x, int y) {
         return this.load.init_Z(x, y);
      }
   }
}
