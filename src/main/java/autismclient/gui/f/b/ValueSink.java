package autismclient.gui.f.b;

import autismclient.util.cH;
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class ValueSink {
   public static final int init_I = 10;
   private static final float init_F = 2.3F;
   private static final float load = 0.7F;
   private static final float read = 4.5F;
   private static CursorType init_CursorType;

   private ValueSink() {
   }

   public static boolean init(cH b, double mx, double my) {
      return b != null && init(b.init_I, b.load_I, b.read, b.write, mx, my);
   }

   public static boolean init(autismclient.gui.f.NetHelper b, double mx, double my) {
      return b != null && init(b.read(), b.write(), b.update(), b.sync(), mx, my);
   }

   private static boolean init(int x, int y, int w, int h, double mx, double my) {
      return w > 0 && h > 0 ? mx >= x + w - 10 && mx < x + w && my >= y + h - 10 && my < y + h : false;
   }

   public static void init(GuiGraphicsExtractor graphics, autismclient.gui.f.JsonUtil colors, cH b, boolean hover, boolean active) {
      if (b != null) {
         init(graphics, colors, b.init_I, b.load_I, b.read, b.write, hover, active);
      }
   }

   public static void init(GuiGraphicsExtractor graphics, autismclient.gui.f.JsonUtil colors, autismclient.gui.f.NetHelper b, boolean hover, boolean active) {
      if (b != null) {
         init(graphics, colors, b.read(), b.write(), b.update(), b.sync(), hover, active);
      }
   }

   private static void init(GuiGraphicsExtractor graphics, autismclient.gui.f.JsonUtil colors, int x, int y, int w, int h, boolean hover, boolean active) {
      if (graphics != null && w > 20 && h > 20) {
         int color = active ? colors.clear : (hover ? colors.prev : colors.valid & 16777215 | -1946157056);
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
      if (init_CursorType == null) {
         try {
            init_CursorType = CursorType.createStandardCursor(221191, "resize_nwse", CursorTypes.RESIZE_ALL);
         } catch (Throwable t) {
            init_CursorType = CursorTypes.RESIZE_ALL;
         }
      }

      return init_CursorType;
   }
}
