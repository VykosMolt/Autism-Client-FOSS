package autismclient.gui.f.b;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class PanelSurfaceHelper {
   private static final CharEncoder init = new CharEncoder();

   private PanelSurfaceHelper() {
   }

   public static void init(GuiGraphicsExtractor graphics, int x, int y, int width, int height, boolean hovered, boolean selected) {
      int fill = selected ? init.prev() : (hovered ? init.next() : init.stop());
      init(graphics, x, y, width, height, fill);
   }

   public static void init(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int fill) {
      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x, y, width, height), fill);
   }

   public static void init(GuiGraphicsExtractor graphics, int x, int y, int width) {
      init(graphics, x, y, width, init.set());
   }

   public static void init(GuiGraphicsExtractor graphics, int x, int y, int width, int color) {
      autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract_2(graphics, x, y, width, color);
   }

   public static void load(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x, y, width, height), init.tick());
   }

   public static void load(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int accent) {
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(x, y, width, height);
      autismclient.gui.f.PacketUtil.read(graphics, bounds, 5, init.push(), accent != 0 ? accent : init.set());
   }

   public static void read(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
      autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(x, y, width, height), 3, init.reset(), init.set());
   }

   public static void read(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x, y, width, height), color);
   }
}
