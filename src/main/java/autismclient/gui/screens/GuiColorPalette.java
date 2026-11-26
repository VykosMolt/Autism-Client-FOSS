package autismclient.gui.screens;

import autismclient.util.ThemePaletteManager;

public final class GuiColorPalette {
   public static final int init = -15987181;
   public static final int load = -401402080;
   public static final int read = -1206840035;
   public static final int write = -14249;
   public static int update = -12761513;
   public static int sync = -12588930;
   public static int flush = -854534;
   public static int close = -6774093;
   public static int open = -13248397;
   public static int poll = -42149;

   private GuiColorPalette() {
   }

   public static void init() {
      update = ThemePaletteManager.recolor(-9547177, ThemePaletteManager.ColorRole.OUTLINE);
      sync = ThemePaletteManager.recolor(-12588930, ThemePaletteManager.ColorRole.SUCCESS);
      flush = ThemePaletteManager.recolor(-854534, ThemePaletteManager.ColorRole.TEXT);
      close = ThemePaletteManager.recolor(-6774093, ThemePaletteManager.ColorRole.TEXT);
      open = ThemePaletteManager.recolor(-13248397, ThemePaletteManager.ColorRole.SUCCESS);
      poll = ThemePaletteManager.recolor(-42149, ThemePaletteManager.ColorRole.DANGER);
   }

   static {
      init();
   }
}
