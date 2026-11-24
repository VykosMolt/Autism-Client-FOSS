package autismclient.gui.f.b;

import autismclient.util.ct;
import autismclient.util.cz;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public final class RingBuffer {
   public static final String init = "X";
   public static final String load = "^";
   public static final String read = "v";
   public static final String write = "+";
   public static final String update = "-";

   private RingBuffer() {
   }

   public static void init(
      GuiGraphicsExtractor graphics, Font font, autismclient.gui.f.NetHelper bounds, String symbol, boolean hovered, boolean active, boolean danger
   ) {
      if (graphics != null && font != null && bounds != null && bounds.update() > 0 && bounds.sync() > 0) {
         autismclient.gui.f.SyncManager context = autismclient.gui.f.UiHelper.init(graphics, font, -10000, -10000);
         autismclient.gui.f.JsonUtil colors = context.init_TableIndex().init_JsonUtil();
         int radius = init(bounds);
         int fill = danger ? ct.init_I_I_ct_DataUtil(-868936937, ct.Inner_DataUtil.flush) : -1156179423;
         autismclient.gui.f.PacketUtil.load(graphics, bounds, radius, fill, danger ? colors.data : colors.set);
         float hoverT = autismclient.gui.f.DataUtil.init(autismclient.gui.f.DataUtil.init(bounds), hovered && active);
         if (hoverT > 0.001F) {
            autismclient.gui.f.PacketUtil.load(graphics, bounds.init(1), Math.max(1, radius - 1), (int)(32.0F * hoverT) << 24 | 16777215);
         }

         if (!active) {
            autismclient.gui.f.PacketUtil.load(graphics, bounds.init(1), Math.max(1, radius - 1), 1711276032);
         }

         int color = active ? colors.prev : colors.size;
         if ("X".equals(symbol)) {
            autismclient.gui.f.PacketUtil.read(graphics, bounds.init(3), color);
         } else if ("^".equals(symbol) || "v".equals(symbol)) {
            autismclient.gui.f.NetHelper glyph = init(bounds, 3);
            if ("^".equals(symbol)) {
               autismclient.gui.f.PacketUtil.write(graphics, glyph, color);
            } else {
               autismclient.gui.f.PacketUtil.init(graphics, glyph, true, color);
            }
         } else if (!"+".equals(symbol) && !"-".equals(symbol)) {
            String glyph = symbol == null ? "" : symbol;
            context.init_PoolEntry().load(graphics, glyph, bounds, color);
         } else {
            autismclient.gui.f.NetHelper glyph = init(bounds, 3);
            cz.init(graphics, "+".equals(symbol) ? cz.pop : cz.tick, glyph.read(), glyph.write(), glyph.update(), glyph.sync(), color);
         }
      }
   }

   private static autismclient.gui.f.NetHelper init(autismclient.gui.f.NetHelper bounds, int inset) {
      int gs = Math.max(4, Math.min(bounds.update(), bounds.sync()) - inset * 2);
      int box = Math.min(bounds.update(), bounds.sync());
      if ((box - gs & 1) != 0) {
         gs--;
      }

      return autismclient.gui.f.NetHelper.init(
         bounds.read() + (bounds.update() - gs) / 2, bounds.write() + (bounds.sync() - gs) / 2, Math.max(1, gs), Math.max(1, gs)
      );
   }

   private static int init(autismclient.gui.f.NetHelper bounds) {
      return Math.min(3, Math.max(2, Math.min(bounds.update(), bounds.sync()) / 3));
   }

   public static void init(
      GuiGraphicsExtractor graphics, Font font, autismclient.gui.f.NetHelper bounds, Identifier icon, boolean hovered, boolean active, boolean danger
   ) {
      if (graphics != null && font != null && bounds != null && bounds.update() > 0 && bounds.sync() > 0) {
         autismclient.gui.f.SyncManager context = autismclient.gui.f.UiHelper.init(graphics, font, -10000, -10000);
         autismclient.gui.f.JsonUtil colors = context.init_TableIndex().init_JsonUtil();
         int radius = init(bounds);
         int fill = danger ? -799925730 : -1156179423;
         int borderColor = danger ? -1947062 : colors.set;
         autismclient.gui.f.PacketUtil.load(graphics, bounds, radius, fill, borderColor);
         float hoverT = autismclient.gui.f.DataUtil.init(autismclient.gui.f.DataUtil.init(bounds), hovered && active);
         if (hoverT > 0.001F) {
            autismclient.gui.f.PacketUtil.load(graphics, bounds.init(1), Math.max(1, radius - 1), (int)(32.0F * hoverT) << 24 | 16777215);
         }

         if (!active) {
            autismclient.gui.f.PacketUtil.load(graphics, bounds.init(1), Math.max(1, radius - 1), 1711276032);
         }

         int color = active ? -1 : -2130706433;
         int size = Math.max(4, Math.min(bounds.update(), bounds.sync()) - 5);
         int box = Math.min(bounds.update(), bounds.sync());
         if ((box - size & 1) != 0) {
            size--;
         }

         cz.init(graphics, icon, bounds.read() + (bounds.update() - size) / 2, bounds.write() + (bounds.sync() - size) / 2, size, color);
      }
   }

   public static void init(GuiGraphicsExtractor graphics, Font font, autismclient.gui.f.NetHelper bounds, String symbol, int color, float alpha) {
      if (graphics != null && font != null && bounds != null && !(alpha <= 0.001F)) {
         int resolved = autismclient.gui.f.PacketUtil.init(color, alpha);
         String glyph = symbol == null ? "" : symbol;
         autismclient.gui.f.UiHelper.init(graphics, font, -10000, -10000).init_PoolEntry().load(graphics, glyph, bounds, resolved);
      }
   }

   public static Font init() {
      Minecraft minecraft = Minecraft.getInstance();
      return minecraft == null ? null : minecraft.font;
   }
}
