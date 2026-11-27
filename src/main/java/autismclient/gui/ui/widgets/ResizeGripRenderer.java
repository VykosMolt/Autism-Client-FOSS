package autismclient.gui.ui.widgets;

import autismclient.util.OverlayBounds;
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class ResizeGripRenderer {
   public static final int init_I = 10;
   private static final float init_F = 2.3F;
   private static final float load = 0.7F;
   private static final float read = 4.5F;
   private static CursorType resizeCursor;

   private ResizeGripRenderer() {
   }

   public static boolean init(OverlayBounds b, double mx, double my) {
      return b != null && init(b.x, b.y, b.width, b.height, mx, my);
   }

   public static boolean init(autismclient.gui.f.NetHelper b, double mx, double my) {
      return b != null && init(b.read(), b.write(), b.update(), b.sync(), mx, my);
   }

   private static boolean init(int x, int y, int w, int h, double mx, double my) {
      return w > 0 && h > 0 ? mx >= x + w - 10 && mx < x + w && my >= y + h - 10 && my < y + h : false;
   }

   public static void init(GuiGraphicsExtractor graphics, autismclient.gui.ui.UiColorScheme colors, OverlayBounds b, boolean hover, boolean active) {
      if (b != null) {
         init(graphics, colors, b.x, b.y, b.width, b.height, hover, active);
      }
   }

   public static void init(GuiGraphicsExtractor graphics, autismclient.gui.ui.UiColorScheme colors, autismclient.gui.f.NetHelper b, boolean hover, boolean active) {
      if (b != null) {
         init(graphics, colors, b.read(), b.write(), b.update(), b.sync(), hover, active);
      }
   }

   private static void init(GuiGraphicsExtractor graphics, autismclient.gui.ui.UiColorScheme colors, int x, int y, int w, int h, boolean hover, boolean active) {
      if (graphics != null && w > 20 && h > 20) {
         int color = active ? colors.accent : (hover ? colors.textPrimary : colors.textSecondary & 16777215 | -1946157056);
         float cx = x + w - 4.5F;
         float cy = y + h - 4.5F;

         for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3 - row; col++) {
               autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract(graphics, cx - col * 2.3F, cy - row * 2.3F, 0.7F, color);
            }
         }
      }
   }

   public static void init(GuiGraphicsExtractor graphics) {
      if (graphics != null) {
         graphics.requestCursor(init());
      }
   }

   private static CursorType init() {
      if (resizeCursor == null) {
         try {
            resizeCursor = CursorType.createStandardCursor(221191, "resize_nwse", CursorTypes.RESIZE_ALL);
         } catch (Throwable t) {
            resizeCursor = CursorTypes.RESIZE_ALL;
         }
      }

      return resizeCursor;
   }
}
