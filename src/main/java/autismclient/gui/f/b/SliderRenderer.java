package autismclient.gui.f.b;

public final class SliderRenderer {
   private SliderRenderer() {
   }

   public static void init(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, double ratio, boolean hovered) {
      autismclient.gui.f.UiColorScheme colors = context.init_TableIndex().init_JsonUtil();
      ratio = Math.max(0.0, Math.min(1.0, ratio));
      int radius = 3;
      autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), bounds, radius, colors.pop, colors.set);
      autismclient.gui.f.NetHelper track = bounds.load(3, Math.max(4, bounds.sync() / 2 - 1), 3, Math.max(4, bounds.sync() / 2 - 1));
      if (track.sync() <= 0) {
         track = autismclient.gui.f.NetHelper.init(bounds.read() + 3, bounds.write() + bounds.sync() / 2, Math.max(1, bounds.update() - 6), 1);
      }

      int trackRadius = track.sync() / 2;
      autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), track, trackRadius, -869256388);
      int fillW = (int)Math.round(track.update() * ratio);
      autismclient.gui.f.PacketUtil.load(
         context.init_GuiGraphicsExtract(), autismclient.gui.f.NetHelper.init(track.read(), track.write(), fillW, track.sync()), trackRadius, colors.clear
      );
      float hoverT = autismclient.gui.f.HoverTransitionCache.init(autismclient.gui.f.HoverTransitionCache.init(bounds), hovered);
      if (hoverT > 0.001F) {
         autismclient.gui.f.PacketUtil.load(
            context.init_GuiGraphicsExtract(), bounds.init(1), Math.max(1, radius - 1), Math.round(20.0F * hoverT) << 24 | 16777215
         );
      }

      float knobR = Math.max(1.5F, bounds.sync() / 2.0F - 2.0F);
      float knobCx = Math.max(bounds.read() + 2 + knobR, Math.min(bounds.init() - 2 - knobR, track.read() + fillW));
      autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract(
         context.init_GuiGraphicsExtract(), knobCx, bounds.write() + bounds.sync() / 2.0F, knobR, init_I_I(colors.valid, colors.prev, hoverT)
      );
   }

   public static double init_D_D(double value, double min, double max) {
      return max <= min ? 0.0 : Math.max(0.0, Math.min(1.0, (value - min) / (max - min)));
   }

   public static double init(double mouseX, int x, int width, double min, double max, double step) {
      double ratio = width <= 0 ? 0.0 : Math.max(0.0, Math.min(1.0, (mouseX - x) / width));
      double value = min + ratio * (max - min);
      double safeStep = Math.max(1.0E-4, step);
      return min + Math.round((value - min) / safeStep) * safeStep;
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
}
