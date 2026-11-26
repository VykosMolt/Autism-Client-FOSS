package autismclient.gui.ui.widgets;

public final class RangeSliderRenderer {
   public static final int init = -1;
   public static final int load = 0;
   public static final int read = 1;

   private RangeSliderRenderer() {
   }

   public static void init(
      autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, double minRatio, double maxRatio, boolean hovered, int activeThumb
   ) {
      autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().init_JsonUtil();
      minRatio = init(minRatio);
      maxRatio = init(maxRatio);
      if (maxRatio < minRatio) {
         double swap = minRatio;
         minRatio = maxRatio;
         maxRatio = swap;
      }

      int radius = 3;
      autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), bounds, radius, colors.pop, colors.set);
      autismclient.gui.f.NetHelper track = init(bounds);
      int trackRadius = track.sync() / 2;
      autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), track, trackRadius, -869256388);
      int lowX = init(track, minRatio);
      int highX = init(track, maxRatio);
      if (highX > lowX) {
         autismclient.gui.f.PacketUtil.load(
            context.init_GuiGraphicsExtract(), autismclient.gui.f.NetHelper.init(lowX, track.write(), highX - lowX, track.sync()), trackRadius, colors.clear
         );
      }

      float hoverT = autismclient.gui.ui.HoverTransitionCache.init(autismclient.gui.ui.HoverTransitionCache.init(bounds), hovered);
      if (hoverT > 0.001F) {
         autismclient.gui.f.PacketUtil.load(
            context.init_GuiGraphicsExtract(), bounds.init(1), Math.max(1, radius - 1), Math.round(20.0F * hoverT) << 24 | 16777215
         );
      }

      init_V_SyncManager(context, bounds, lowX, hovered || activeThumb == 0);
      init_V_SyncManager(context, bounds, highX, hovered || activeThumb == 1);
   }

   private static void init_V_SyncManager(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, int centerX, boolean lit) {
      autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().init_JsonUtil();
      float r = Math.max(1.5F, bounds.sync() / 2.0F - 2.0F);
      float cx = Math.max(bounds.read() + 2 + r, Math.min(bounds.init() - 2 - r, centerX));
      autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract(
         context.init_GuiGraphicsExtract(), cx, bounds.write() + bounds.sync() / 2.0F, r, lit ? colors.prev : colors.valid
      );
   }

   public static autismclient.gui.f.NetHelper init(autismclient.gui.f.NetHelper bounds) {
      int inset = Math.max(4, bounds.sync() / 2 - 1);
      autismclient.gui.f.NetHelper track = bounds.load(3, inset, 3, inset);
      return track.sync() <= 0
         ? autismclient.gui.f.NetHelper.init(bounds.read() + 3, bounds.write() + bounds.sync() / 2, Math.max(1, bounds.update() - 6), 1)
         : track;
   }

   private static int init(autismclient.gui.f.NetHelper track, double ratio) {
      return track.read() + (int)Math.round(track.update() * init(ratio));
   }

   public static int init_I_D(double mouseX, autismclient.gui.f.NetHelper bounds, double minRatio, double maxRatio) {
      autismclient.gui.f.NetHelper track = init(bounds);
      int lowX = init(track, minRatio);
      int highX = init(track, maxRatio);
      double toLow = Math.abs(mouseX - lowX);
      double toHigh = Math.abs(mouseX - highX);
      if (Math.abs(toLow - toHigh) < 0.5) {
         return mouseX < lowX ? 0 : 1;
      } else {
         return toLow <= toHigh ? 0 : 1;
      }
   }

   public static double init(double value, double min, double max) {
      return max <= min ? 0.0 : init((value - min) / (max - min));
   }

   public static double init(double mouseX, autismclient.gui.f.NetHelper bounds, double min, double max, double step) {
      autismclient.gui.f.NetHelper track = init(bounds);
      double ratio = track.update() <= 0 ? 0.0 : init((mouseX - track.read()) / track.update());
      double value = min + ratio * (max - min);
      double safeStep = Math.max(1.0E-4, step);
      return min + Math.round((value - min) / safeStep) * safeStep;
   }

   private static double init(double value) {
      return Math.max(0.0, Math.min(1.0, value));
   }
}
