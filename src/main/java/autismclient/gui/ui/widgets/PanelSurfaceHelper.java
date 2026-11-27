package autismclient.gui.ui.widgets;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import autismclient.gui.f.b.CharEncoder;

public final class PanelSurfaceHelper {
   private static final CharEncoder THEME = new CharEncoder();

   private PanelSurfaceHelper() {
   }

   public static void drawCardRow(GuiGraphicsExtractor graphics, int x, int y, int width, int height, boolean hovered, boolean selected) {
      int fill = selected ? THEME.prev() : (hovered ? THEME.next() : THEME.stop());
      fillRect(graphics, x, y, width, height, fill);
   }

   public static void fillRect(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int fill) {
      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x, y, width, height), fill);
   }

   public static void drawDivider(GuiGraphicsExtractor graphics, int x, int y, int width) {
      drawDividerColored(graphics, x, y, width, THEME.set());
   }

   public static void drawDividerColored(GuiGraphicsExtractor graphics, int x, int y, int width, int color) {
      autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract_2(graphics, x, y, width, color);
   }

   public static void drawSectionHeader(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x, y, width, height), THEME.tick());
   }

   public static void drawElevatedPanel(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int accent) {
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(x, y, width, height);
      autismclient.gui.f.PacketUtil.read(graphics, bounds, 5, THEME.push(), accent != 0 ? accent : THEME.set());
   }

   public static void drawInputPill(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
      autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(x, y, width, height), 3, THEME.reset(), THEME.set());
   }

   public static void drawAccentBar(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x, y, width, height), color);
   }
}
