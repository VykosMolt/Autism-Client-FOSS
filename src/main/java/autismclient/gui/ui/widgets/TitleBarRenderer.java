package autismclient.gui.ui.widgets;

import autismclient.util.GuiIconAtlas;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

public final class TitleBarRenderer {
   private TitleBarRenderer() {
   }

   public static autismclient.gui.f.NetHelper collapseButton(autismclient.gui.f.NetHelper bounds) {
      int size = Math.min(12, Math.max(8, bounds.sync() - 3));
      return autismclient.gui.f.NetHelper.init(bounds.read() + 2, bounds.write() + Math.max(1, (bounds.sync() - size) / 2), size, size);
   }

   public static autismclient.gui.f.NetHelper closeButton(autismclient.gui.f.NetHelper bounds) {
      int size = Math.min(14, Math.max(10, bounds.sync() - 1));
      return autismclient.gui.f.NetHelper.init(bounds.init() - size - 2, bounds.write() + Math.max(1, (bounds.sync() - size) / 2), size, size);
   }

   public static void init(
      autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String title, boolean collapsed, boolean close, boolean hovered
   ) {
      init(context, bounds, title, collapsed, true, close, hovered);
   }

   public static void init(
      autismclient.gui.f.SyncManager context,
      autismclient.gui.f.NetHelper bounds,
      String title,
      boolean collapsed,
      boolean collapse,
      boolean close,
      boolean hovered
   ) {
      init(context, bounds, title, collapsed, collapse, close, hovered, 4, 4);
   }

   public static void init(
      autismclient.gui.f.SyncManager context,
      autismclient.gui.f.NetHelper bounds,
      String title,
      boolean collapsed,
      boolean collapse,
      boolean close,
      boolean hovered,
      int titleLeftInset,
      int titleRightInset
   ) {
      render(context, bounds, title, collapsed, collapsed, collapse, close, hovered, titleLeftInset, titleRightInset);
   }

   public static void render(
      autismclient.gui.f.SyncManager context,
      autismclient.gui.f.NetHelper bounds,
      String title,
      boolean collapsed,
      boolean shapeCollapsed,
      boolean collapse,
      boolean close,
      boolean hovered,
      int titleLeftInset,
      int titleRightInset
   ) {
      GuiGraphicsExtractor graphics = context.init_GuiGraphicsExtract();
      autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().colors();
      init(context, bounds, shapeCollapsed, hovered);
      autismclient.gui.f.NetHelper collapseBounds = collapseButton(bounds);
      if (collapse) {
         init(context, collapseBounds.init(1), collapsed, colors.textPrimary);
      }

      autismclient.gui.f.NetHelper closeBounds = close ? closeButton(bounds) : null;
      int titleLeft = collapse ? collapseBounds.init() + 3 : bounds.read() + Math.max(0, titleLeftInset);
      int titleRight = close ? closeBounds.read() - 3 : bounds.init() - Math.max(0, titleRightInset);
      context.init_PoolEntry().drawEllipsized(graphics, title, titleLeft, context.init_PoolEntry().centerTextY(bounds), Math.max(1, titleRight - titleLeft), colors.textPrimary);
      if (close) {
         boolean closeHovered = closeBounds.init_Z(context.read(), context.write());
         float closeT = autismclient.gui.ui.HoverTransitionCache.sample(autismclient.gui.ui.HoverTransitionCache.boundsKey(closeBounds), closeHovered);
         int icon = 8;
         int ix = closeBounds.read() + (closeBounds.update() - icon) / 2;
         int iy = closeBounds.write() + (closeBounds.sync() - icon) / 2;
         if (closeT > 0.001F) {
            autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract(
               graphics,
               closeBounds.read() + closeBounds.update() / 2.0F,
               closeBounds.write() + closeBounds.sync() / 2.0F,
               Math.min(6.5F, closeBounds.sync() / 2.0F + 0.5F),
               Math.round(20.0F * closeT) << 24 | 16777215
            );
         }

         GuiIconAtlas.draw(graphics, GuiIconAtlas.CLOSE, ix, iy, icon, icon, init(colors.textSecondary, colors.textPrimary, closeT));
      }
   }

   public static void init(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, boolean shapeCollapsed, boolean hovered) {
      GuiGraphicsExtractor graphics = context.init_GuiGraphicsExtract();
      autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().colors();
      int radius = 7;
      float headerT = autismclient.gui.ui.HoverTransitionCache.sample(autismclient.gui.ui.HoverTransitionCache.boundsKey(bounds), hovered);
      int headerFill = init(colors.headerSurface, colors.headerSurfaceActive, headerT);
      if (shapeCollapsed) {
         autismclient.gui.f.PacketUtil.load(graphics, bounds, radius, headerFill);
      } else {
         autismclient.gui.f.PacketUtil.read(graphics, bounds, radius, headerFill);
         autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract_2(graphics, bounds.read(), bounds.load() - 1, bounds.update(), colors.divider);
      }
   }

   public static void init(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, boolean collapsed, int color) {
      float amount = autismclient.gui.ui.ToggleTransitionCache.init("collapse:" + autismclient.gui.ui.ToggleTransitionCache.init(bounds), !collapsed);
      float cx = bounds.read() + bounds.update() / 2.0F;
      float cy = bounds.write() + bounds.sync() / 2.0F;
      Matrix3x2fStack pose = context.init_GuiGraphicsExtract().pose();
      pose.pushMatrix();

      try {
         pose.translate(cx, cy);
         pose.rotate(amount * 1.5707964F);
         pose.translate(-cx, -cy);
         autismclient.gui.f.PacketUtil.init(context.init_GuiGraphicsExtract(), bounds, false, color);
      } finally {
         pose.popMatrix();
      }
   }

   private static int init(int from, int to, float t) {
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
