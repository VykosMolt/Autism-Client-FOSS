package autismclient.gui.ui;

import autismclient.gui.ui.widgets.UiLayoutMath;
import autismclient.util.ThemePaletteManager;

public final class UiColorScheme {
   public int backdropScrim;
   public int buttonSurface;
   public int buttonSurfaceHover;
   public int borderMuted;
   public int windowSurface;
   public int popupSurface;
   public int headerSurface;
   public int headerSurfaceActive;
   public int cardSurface;
   public int cardSurfaceHover;
   public int cardSurfaceElevated;
   public int inputSurface;
   public int inputSurfaceFocused;
   public int borderStrong;
   public int borderSubtle;
   public int borderControl;
   public int divider;
   public int innerHighlight;
   public int accent;
   public int accentDark;
   public int accentSoft;
   public int success;
   public int successSoft;
   public int textPrimary;
   public int textSecondary;
   public int textMuted;
   public int danger;

   public UiColorScheme() {
      this.refresh();
   }

   public void refresh() {
      this.applyPalette(ThemePaletteManager.getPalette());
   }

   public void applyPalette(ThemePaletteManager.CompiledPalette state) {
      this.accent = ThemePaletteManager.recolorWithPalette(-50373, ThemePaletteManager.ColorRole.ACCENT, state);
      this.backdropScrim = 1208486416;
      this.windowSurface = this.tintSurface(1777705, 212, 0.035F);
      this.popupSurface = this.tintSurface(1711912, 230, 0.035F);
      this.headerSurface = this.tintSurface(3883598, 88, 0.045F);
      this.headerSurfaceActive = this.tintSurface(4805473, 120, 0.065F);
      this.cardSurface = this.tintSurface(2238772, 191, 0.035F);
      this.cardSurfaceHover = this.tintSurface(2567737, 196, 0.035F);
      this.cardSurfaceElevated = this.tintSurface(3358283, 206, 0.065F);
      this.inputSurface = this.tintSurface(1251875, 196, 0.025F);
      this.inputSurfaceFocused = this.tintSurface(2042422, 210, 0.05F);
      this.buttonSurface = this.tintSurface(3160391, 200, 0.035F);
      this.buttonSurfaceHover = this.tintSurface(4345181, 212, 0.065F);
      this.borderStrong = this.tintOutline(96, 0.12F);
      this.borderSubtle = this.tintOutline(60, 0.09F);
      this.borderControl = this.tintOutline(76, 0.1F);
      this.divider = this.tintOutline(34, 0.06F);
      this.innerHighlight = 385875967;
      this.borderMuted = this.tintOutline(84, 0.06F);
      this.accentDark = ThemePaletteManager.recolorWithPalette(-7397596, ThemePaletteManager.ColorRole.ACCENT, state);
      this.accentSoft = ThemePaletteManager.recolorWithPalette(1157577531, ThemePaletteManager.ColorRole.ACCENT, state);
      this.success = ThemePaletteManager.recolorWithPalette(-12588930, ThemePaletteManager.ColorRole.SUCCESS, state);
      this.successSoft = ThemePaletteManager.recolorWithPalette(1715464318, ThemePaletteManager.ColorRole.SUCCESS, state);
      this.textPrimary = ThemePaletteManager.recolorWithPalette(-854534, ThemePaletteManager.ColorRole.TEXT, state);
      this.textSecondary = ThemePaletteManager.recolorWithPalette(-6774093, ThemePaletteManager.ColorRole.TEXT, state);
      this.textMuted = ThemePaletteManager.recolorWithPalette(-10853776, ThemePaletteManager.ColorRole.TEXT, state);
      this.danger = ThemePaletteManager.recolorWithPalette(-43691, ThemePaletteManager.ColorRole.DANGER, state);
   }

   private int tintSurface(int rgb, int alpha, float tint) {
      int tinted = UiLayoutMath.lerpColor(0xFF000000 | rgb & 16777215, 0xFF000000 | this.accent & 16777215, tint);
      return alpha << 24 | tinted & 16777215;
   }

   private int tintOutline(int alpha, float tint) {
      int tinted = UiLayoutMath.lerpColor(-2300686, 0xFF000000 | this.accent & 16777215, tint);
      return alpha << 24 | tinted & 16777215;
   }
}
