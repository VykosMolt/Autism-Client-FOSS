package autismclient.gui.ui.widgets;

import autismclient.util.cA;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class OverlayFrameHelper {
   private static final int init = 4;

   private OverlayFrameHelper() {
   }

   public static autismclient.gui.f.NetHelper clampToScreen(autismclient.gui.f.NetHelper wanted, int minWidth, int minHeight) {
      int screenWidth = Math.max(1, cA.init_I());
      int screenHeight = Math.max(1, cA.load_I());
      int availableWidth = Math.max(1, screenWidth - 8);
      int availableHeight = Math.max(1, screenHeight - 8);
      int width = Math.max(Math.min(minWidth, availableWidth), Math.min(wanted.update(), availableWidth));
      int height = Math.max(Math.min(minHeight, availableHeight), Math.min(wanted.sync(), availableHeight));
      int x = Math.max(4, Math.min(wanted.read(), Math.max(4, screenWidth - 4 - width)));
      int y = Math.max(4, Math.min(wanted.write(), Math.max(4, screenHeight - 4 - height)));
      return autismclient.gui.f.NetHelper.init(x, y, width, height);
   }

   public static void drawFrame(
      autismclient.gui.f.SyncManager context,
      autismclient.gui.f.NetHelper bounds,
      int headerHeight,
      String title,
      boolean collapsed,
      boolean active,
      boolean headerHovered
   ) {
      WindowFrameRenderer.init(context, bounds, title, collapsed, true, true, headerHovered, active, 4, 4, headerHeight);
   }

   public static boolean beginClip(GuiGraphicsExtractor graphics, autismclient.gui.f.NetHelper bounds, int headerHeight, boolean collapsed) {
      if (!collapsed && bounds.sync() > headerHeight + 1) {
         autismclient.gui.ui.UiClipStack.getInstance()
            .push(
               graphics,
               autismclient.gui.f.NetHelper.init(
                  bounds.read() + 1, bounds.write() + headerHeight, Math.max(0, bounds.update() - 2), Math.max(0, bounds.sync() - headerHeight - 1)
               )
            );
         return true;
      } else {
         return false;
      }
   }

   public static void endClip(GuiGraphicsExtractor graphics, boolean clipped) {
      if (clipped) {
         autismclient.gui.ui.UiClipStack.getInstance().pop(graphics);
      }
   }
}
