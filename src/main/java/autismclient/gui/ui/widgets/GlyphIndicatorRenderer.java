package autismclient.gui.ui.widgets;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class GlyphIndicatorRenderer {
   private GlyphIndicatorRenderer() {
   }

   public static void init(GuiGraphicsExtractor context, int x, int y, int size, int color, int shadowColor, float alpha) {
      if (!(alpha <= 0.001F)) {
         autismclient.gui.f.PacketUtil.read(context, autismclient.gui.f.NetHelper.init(x, y, size, size), autismclient.gui.f.PacketUtil.init(color, alpha));
      }
   }

   public static void init(GuiGraphicsExtractor context, int x, int y, int size, GlyphIndicatorRenderer.GlyphDirection direction, int color, int shadowColor, float alpha) {
      if (!(alpha <= 0.001F)) {
         int resolved = autismclient.gui.f.PacketUtil.init(color, alpha);
         autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(x, y, size, size);
         switch (direction) {
            case RIGHT:
               autismclient.gui.f.PacketUtil.init(context, bounds, false, resolved);
               break;
            case DOWN:
               autismclient.gui.f.PacketUtil.init(context, bounds, true, resolved);
               break;
            case UP:
               autismclient.gui.f.PacketUtil.write(context, bounds, resolved);
         }
      }
   }

   public static void init(GuiGraphicsExtractor context, int x, int y, int size, float progress, int color, int shadowColor, float alpha) {
      if (!(alpha <= 0.001F)) {
         float clamped = Math.max(0.0F, Math.min(1.0F, progress));
         int resolved = autismclient.gui.f.PacketUtil.init(color, alpha);
         autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(x, y, size, size);
         if (clamped >= 0.5F) {
            autismclient.gui.f.PacketUtil.init(context, bounds, true, resolved);
         } else {
            autismclient.gui.f.PacketUtil.init(context, bounds, false, resolved);
         }
      }
   }

   public enum GlyphDirection {
      RIGHT,
      DOWN,
      UP;
   }
}
