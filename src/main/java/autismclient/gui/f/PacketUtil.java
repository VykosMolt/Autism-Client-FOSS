package autismclient.gui.f;

import autismclient.gui.ui.widgets.UiLayoutMath;
import autismclient.util.GuiIconAtlas;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class PacketUtil {
   public static final int init = 8;
   public static final int load = 5;
   public static final int read = 3;

   private PacketUtil() {
   }

   public static void init(GuiGraphicsExtractor graphics, NetHelper bounds, int color) {
      if (bounds.update() > 0 && bounds.sync() > 0) {
         graphics.fill(bounds.read(), bounds.write(), bounds.init(), bounds.load(), color);
      }
   }

   public static void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor graphics, int x, int y, int width, int color) {
      if (width > 0) {
         init(graphics, NetHelper.init(x, y, width, 1), color);
      }
   }

   public static void load(GuiGraphicsExtractor graphics, int x, int y, int height, int color) {
      if (height > 0) {
         init(graphics, NetHelper.init(x, y, 1, height), color);
      }
   }

   public static void load(GuiGraphicsExtractor graphics, NetHelper bounds, int color) {
      if (bounds.update() > 0 && bounds.sync() > 0) {
         init_V_GuiGraphicsExtract_2(graphics, bounds.read(), bounds.write(), bounds.update(), color);
         if (bounds.sync() > 1) {
            init_V_GuiGraphicsExtract_2(graphics, bounds.read(), bounds.load() - 1, bounds.update(), color);
         }

         if (bounds.sync() > 2) {
            load(graphics, bounds.read(), bounds.write() + 1, bounds.sync() - 2, color);
            if (bounds.update() > 1) {
               load(graphics, bounds.init() - 1, bounds.write() + 1, bounds.sync() - 2, color);
            }
         }
      }
   }

   public static void init(GuiGraphicsExtractor graphics, NetHelper bounds, int fill, int border) {
      init(graphics, bounds, fill);
      load(graphics, bounds, border);
   }

   public static void init(
      GuiGraphicsExtractor graphics, NetHelper bounds, int headerHeight, int fill, int headerFill, int bodyShade, int border, int accent, float alpha
   ) {
      if (bounds.update() > 0 && bounds.sync() > 0) {
         int headerBottom = Math.min(bounds.load(), bounds.write() + Math.max(1, headerHeight));
         read(graphics, bounds, 8, init(fill, alpha), init(border, alpha));
         if (headerBottom > bounds.write()) {
            NetHelper header = NetHelper.init(
               bounds.read() + 1, bounds.write() + 1, bounds.update() - 2, Math.min(bounds.sync() - 2, headerBottom - bounds.write() - 1)
            );
            if (headerBottom >= bounds.load() - 1) {
               load(graphics, header, 7, init(headerFill, alpha));
            } else {
               read(graphics, header, 7, init(headerFill, alpha));
               init_V_GuiGraphicsExtract_2(graphics, bounds.read() + 1, headerBottom - 1, Math.max(0, bounds.update() - 2), init(border, alpha * 0.4F));
            }
         }
      }
   }

   public static void init_V_GuiGraphicsExtract_3(GuiGraphicsExtractor graphics, NetHelper bounds, int fill, int border, int accent) {
      if (bounds.update() > 0 && bounds.sync() > 0) {
         autismclient.gui.sdf.SdfShapeRenderer.load(
            graphics, bounds.read(), bounds.write(), bounds.update(), bounds.sync(), 5.0F, init(1073741824, (fill >>> 24) / 255.0F)
         );
         read(graphics, bounds, 5, fill, border);
      }
   }

   public static void init(SyncManager context, NetHelper bounds, int fill, int border) {
      if (context.init_Z()) {
         load(context.init_GuiGraphicsExtract(), bounds, 5, fill, border);
      } else {
         init_V_GuiGraphicsExtract_3(context.init_GuiGraphicsExtract(), bounds, fill, border, context.init_TableIndex().init_JsonUtil().clear);
      }
   }

   public static int init(int color, float alpha) {
      int sourceAlpha = color >>> 24 & 0xFF;
      int resolvedAlpha = Math.max(0, Math.min(255, Math.round(sourceAlpha * Math.max(0.0F, Math.min(1.0F, alpha)))));
      return resolvedAlpha << 24 | color & 16777215;
   }

   public static void read(GuiGraphicsExtractor graphics, NetHelper bounds, int color) {
      GuiIconAtlas.init(graphics, GuiIconAtlas.init_Identifier, bounds.read(), bounds.write(), bounds.update(), bounds.sync(), color);
   }

   public static void load(GuiGraphicsExtractor graphics, NetHelper bounds, int radius, int color) {
      if (bounds.update() > 0 && bounds.sync() > 0) {
         autismclient.gui.sdf.SdfShapeRenderer.init(graphics, bounds.read(), bounds.write(), bounds.update(), bounds.sync(), radius, color);
      }
   }

   public static void init_V_GuiGraphicsExtract(GuiGraphicsExtractor graphics, float cx, float cy, float r, int color) {
      autismclient.gui.sdf.SdfShapeRenderer.init_V_GuiGraphicsExtract(graphics, cx, cy, r, color);
   }

   public static void init(GuiGraphicsExtractor graphics, float centerX, float top, float[] widths, float rowHeight, float radius, int color) {
      autismclient.gui.sdf.SdfShapeRenderer.init(graphics, centerX, top, widths, rowHeight, radius, color);
   }

   public static void init(GuiGraphicsExtractor graphics, float centerX, float top, float[] widths, float[] heights, float radius, int color) {
      autismclient.gui.sdf.SdfShapeRenderer.init(graphics, centerX, top, widths, heights, radius, color);
   }

   public static void load(GuiGraphicsExtractor graphics, NetHelper bounds, int radius, int fill, int border) {
      autismclient.gui.sdf.SdfShapeRenderer.init(graphics, bounds.read(), bounds.write(), bounds.update(), bounds.sync(), radius, fill, border);
   }

   public static void init_V_SyncManager(SyncManager context, NetHelper bounds, int radius, int fill, int border) {
      if (context.init_Z()) {
         load(context.init_GuiGraphicsExtract(), bounds, radius, fill, border);
      } else {
         read(context.init_GuiGraphicsExtract(), bounds, radius, fill, border);
      }
   }

   public static void read(GuiGraphicsExtractor graphics, NetHelper bounds, int radius, int fill, int border) {
      if (bounds.update() > 0 && bounds.sync() > 0) {
         int topFill = UiLayoutMath.init_I_I(fill, fill & 0xFF000000 | 16777215, 0.045F);
         int bottomFill = UiLayoutMath.init_I_I(fill, fill & 0xFF000000, 0.025F);
         int topBorder = UiLayoutMath.init_I_I(border, border & 0xFF000000 | 16777215, 0.28F);
         topBorder = Math.min(255, Math.round((border >>> 24) * 1.4F)) << 24 | topBorder & 16777215;
         int bottomBorder = init(border, 0.55F);
         autismclient.gui.sdf.SdfShapeRenderer.init_V_GuiGraphicsExtract(
            graphics, bounds.read(), bounds.write(), bounds.update(), bounds.sync(), radius, topFill, bottomFill, topBorder, bottomBorder
         );
      }
   }

   public static void read(GuiGraphicsExtractor graphics, NetHelper bounds, int radius, int color) {
      if (bounds.update() > 0 && bounds.sync() > 0) {
         autismclient.gui.sdf.SdfShapeRenderer.read(graphics, bounds.read(), bounds.write(), bounds.update(), bounds.sync(), radius, color);
      }
   }

   public static void init(GuiGraphicsExtractor graphics, NetHelper bounds, boolean open, int color) {
      GuiIconAtlas.init(graphics, open ? GuiIconAtlas.write : GuiIconAtlas.sync, bounds.read(), bounds.write(), bounds.update(), bounds.sync(), color);
   }

   public static void write(GuiGraphicsExtractor graphics, NetHelper bounds, int color) {
      GuiIconAtlas.init(graphics, GuiIconAtlas.update, bounds.read(), bounds.write(), bounds.update(), bounds.sync(), color);
   }

   public static void read(GuiGraphicsExtractor graphics, int x, int y, int h, int color) {
      for (int i = 0; i < h; i++) {
         int w = Math.min(i + 1, h - i);
         graphics.fill(x, y + i, x + w, y + i + 1, color);
      }
   }

   public static void update(GuiGraphicsExtractor graphics, NetHelper bounds, int color) {
      GuiIconAtlas.init(graphics, GuiIconAtlas.read, bounds.read(), bounds.write(), bounds.update(), bounds.sync(), color);
   }
}
