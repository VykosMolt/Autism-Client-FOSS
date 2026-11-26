package autismclient.util;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public class UiColorPalette {
   private static final autismclient.gui.f.b.CharEncoder THEME = new autismclient.gui.f.b.CharEncoder();
   private static final int COLOR_ACCENT = -5093304;
   private static final int COLOR_ACCENT_SOFT = 1722959944;
   private static final int COLOR_OUTLINE = 1715151911;
   private static final int COLOR_TEXT_BRIGHT = -791058;
   private static final int COLOR_TEXT_PRIMARY = -3361096;
   private static final int COLOR_TEXT_MUTED = -6388344;
   private static final int COLOR_DANGER = -1938838;
   private static final int COLOR_SUCCESS = -11018872;
   private static final int COLOR_TEAL = -8592483;
   private static final int COLOR_MAGENTA = -27948;
   private static final int COLOR_AMBER = -19091;
   private static final int COLOR_BLUE = -8340993;
   private static final int COLOR_BLACK = -16777216;
   private static final int COLOR_YELLOW = -10134;
   private static final int COLOR_CYAN = -9115393;
   private static final int COLOR_SOFT_WHITE = -461586;
   private static final int COLOR_SILVER = -5195582;
   private static final int COLOR_WARM_WHITE = -3921;

   public static int boostAlphaLight(int color) {
      return scaleAlpha(color, 11, 10);
   }

   public static int boostAlphaStrong(int color) {
      return scaleAlpha(color, 13, 10);
   }

   private static int scaleAlpha(int color, int numerator, int denominator) {
      int alpha = color >>> 24 & 0xFF;
      int boosted = Math.min(255, alpha * numerator / denominator);
      return boosted << 24 | color & 16777215;
   }

   public static int accent() {
      return ThemePaletteManager.recolor(-5093304, ThemePaletteManager.ColorRole.ACCENT);
   }

   public static int accentSoft() {
      return ThemePaletteManager.recolor(1722959944, ThemePaletteManager.ColorRole.ACCENT);
   }

   public static int accentAlt() {
      return ThemePaletteManager.recolor(-5093304, ThemePaletteManager.ColorRole.ACCENT);
   }

   public static int accentDark() {
      return ThemePaletteManager.recolor(-7522248, ThemePaletteManager.ColorRole.ACCENT);
   }

   public static int inputSurface() {
      return THEME.reset();
   }

   public static int windowSurface() {
      return THEME.poll();
   }

   public static int borderSubtle() {
      return THEME.set();
   }

   public static int windowSurfaceElevated() {
      return THEME.push();
   }

   public static int panelSurface() {
      return THEME.poll();
   }

   public static int headerSurface() {
      return THEME.pop();
   }

   public static int headerSurfaceAlt() {
      return THEME.pop();
   }

   public static int outline() {
      return ThemePaletteManager.recolor(1715151911, ThemePaletteManager.ColorRole.OUTLINE);
   }

   public static int secondaryButtonSurface() {
      return THEME.init_I_ByteReader_StreamU_2(autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY);
   }

   public static int cardSurfaceElevated() {
      return THEME.next();
   }

   public static int borderSubtleAlt() {
      return THEME.set();
   }

   public static int rowHoverSurface() {
      return cardSurfaceElevated();
   }

   public static int accentHighlight() {
      return ThemePaletteManager.recolor(-5093304, ThemePaletteManager.ColorRole.ACCENT);
   }

   public static int textMutedAccent() {
      return ThemePaletteManager.recolor(-6388344, ThemePaletteManager.ColorRole.TEXT);
   }

   public static int sectionBarFill() {
      return boostAlphaLight(857679146);
   }

   public static int textPrimary() {
      return ThemePaletteManager.recolor(-3361096, ThemePaletteManager.ColorRole.TEXT);
   }

   public static int inputSurfaceAlt() {
      return THEME.reset();
   }

   public static int inputBorder() {
      return THEME.set();
   }

   public static int dangerSurface() {
      return THEME.add();
   }

   public static int danger() {
      return ThemePaletteManager.recolor(-1938838, ThemePaletteManager.ColorRole.DANGER);
   }

   public static int errorText() {
      return ThemePaletteManager.recolor(-1938838, ThemePaletteManager.ColorRole.DANGER);
   }

   public static int successButtonSurface() {
      return THEME.init_I_ByteReader_StreamU_2(autismclient.gui.ui.layout.ButtonNode.Variant.SUCCESS);
   }

   public static int success() {
      return ThemePaletteManager.recolor(-11018872, ThemePaletteManager.ColorRole.SUCCESS);
   }

   public static int successText() {
      return ThemePaletteManager.recolor(-11018872, ThemePaletteManager.ColorRole.SUCCESS);
   }

   public static int teal() {
      return -8592483;
   }

   public static int magenta() {
      return -27948;
   }

   public static int amber() {
      return -19091;
   }

   public static int blue() {
      return -8340993;
   }

   public static int warningYellow() {
      return -10134;
   }

   public static int cyan() {
      return -9115393;
   }

   public static int softWhite() {
      return -461586;
   }

   public static int silver() {
      return -5195582;
   }

   public static int warmWhite() {
      return -3921;
   }

   public static int listBoxSurface() {
      return THEME.reset();
   }

   public static int rowSurface() {
      return THEME.stop();
   }

   public static int rowHover() {
      return THEME.next();
   }

   public static int rowSelected() {
      return THEME.prev();
   }

   public static int toggleEnabled() {
      return ThemePaletteManager.recolor(-10035062, ThemePaletteManager.ColorRole.TOGGLE);
   }

   public static int toggleDisabled() {
      return ThemePaletteManager.recolor(-12531347, ThemePaletteManager.ColorRole.TOGGLE);
   }

   public static int selectedText() {
      return ThemePaletteManager.recolor(-2828, ThemePaletteManager.ColorRole.TEXT);
   }

   public static int packetRowFill(boolean c2s, int rowIndex, boolean hovered) {
      if (hovered) {
         return c2s ? boostAlphaLight(1145068152) : boostAlphaLight(1146173989);
      } else if (c2s) {
         return (rowIndex & 1) == 0 ? boostAlphaLight(539243587) : boostAlphaLight(405355858);
      } else {
         return (rowIndex & 1) == 0 ? boostAlphaLight(539632411) : boostAlphaLight(405939741);
      }
   }

   public static int packetRowAccent(boolean c2s, int rowIndex) {
      if (c2s) {
         return (rowIndex & 1) == 0 ? -6496513 : -8861448;
      } else {
         return (rowIndex & 1) == 0 ? -14193 : -20889;
      }
   }

   public static int filterRowFill(boolean hovered) {
      return hovered ? THEME.read(5413731) : THEME.write(4225104);
   }

   public static int filterActiveText() {
      return -4787259;
   }

   public static int filterActiveBar() {
      return -9184882;
   }

   public static int blockedRowFill(boolean hovered) {
      return hovered ? boostAlphaLight(-2000670660) : boostAlphaLight(1721245482);
   }

   public static int blockedRowBar() {
      return -1549714;
   }

   public static int subtleDivider() {
      return boostAlphaLight(822083583);
   }

   public static int textBright() {
      return ThemePaletteManager.recolor(-791058, ThemePaletteManager.ColorRole.TEXT);
   }

   public static int textBody() {
      return ThemePaletteManager.recolor(-3361096, ThemePaletteManager.ColorRole.TEXT);
   }

   public static int textSecondary() {
      return ThemePaletteManager.recolor(-6388344, ThemePaletteManager.ColorRole.TEXT);
   }

   public static int textMuted() {
      return ThemePaletteManager.recolor(-6388344, ThemePaletteManager.ColorRole.TEXT);
   }

   public static int menuItemText() {
      return ThemePaletteManager.recolor(-791058, ThemePaletteManager.ColorRole.TEXT);
   }

   public static int divider() {
      return THEME.set();
   }

   public static int popupSurface() {
      return THEME.update(1711912);
   }

   public static int solidBlack() {
      return -16777216;
   }

   public static int tooltipSurface() {
      return THEME.read(658190);
   }

   public static int contextMenuHoverFill() {
      return boostAlphaStrong(822083583);
   }

   public static int violetAccent() {
      return -4668985;
   }

   public static int pinkAccent() {
      return -1511694;
   }

   public static int accentAlt2() {
      return ThemePaletteManager.recolor(-5093304, ThemePaletteManager.ColorRole.ACCENT);
   }

   public static int accentDarkAlt() {
      return ThemePaletteManager.recolor(-7522248, ThemePaletteManager.ColorRole.ACCENT);
   }

   public static int slateAccent() {
      return -9997443;
   }

   public static void drawOutlineBox(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int c) {
      autismclient.gui.f.PacketUtil.load(ctx, autismclient.gui.f.NetHelper.init(x, y, w, h), c);
   }

   public static void drawHorizontalLine(GuiGraphicsExtractor ctx, int x, int y, int width, int color) {
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(x, y, width, 1), color);
   }

   public static void drawResizeGrip(GuiGraphicsExtractor ctx, int x, int y, int size, int color) {
      for (int i = 0; i < 3; i++) {
         int start = x + size - 3 - i * 4;
         autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(start, y + size - 2, 2, 2), color);
         autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(x + size - 2, y + size - 3 - i * 4, 2, 2), color);
      }
   }

   public static void drawInputFrame(GuiGraphicsExtractor ctx, int x, int y, int w, int h, boolean focused) {
      autismclient.gui.f.PacketUtil.read(ctx, autismclient.gui.f.NetHelper.init(x, y, w, h), 5, listBoxSurface(), focused ? accent() : divider());
   }
}
