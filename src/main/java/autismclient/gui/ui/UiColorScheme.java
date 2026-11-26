package autismclient.gui.ui;

import autismclient.gui.ui.widgets.UiLayoutMath;
import autismclient.util.ThemePaletteManager;

public final class UiColorScheme {
   public int init;
   public int load;
   public int read;
   public int write;
   public int update;
   public int sync;
   public int flush;
   public int close;
   public int open;
   public int poll;
   public int push;
   public int pop;
   public int tick;
   public int run;
   public int get;
   public int set;
   public int put;
   public int add;
   public int clear;
   public int reset;
   public int start;
   public int stop;
   public int next;
   public int prev;
   public int valid;
   public int size;
   public int data;

   public UiColorScheme() {
      this.init();
   }

   public void init() {
      this.init(ThemePaletteManager.init_ct_StreamUtil());
   }

   public void init(ThemePaletteManager.CompiledPalette state) {
      this.clear = ThemePaletteManager.init_I_I_2(-50373, ThemePaletteManager.ColorRole.ACCENT, state);
      this.init = 1208486416;
      this.update = this.init(1777705, 212, 0.035F);
      this.sync = this.init(1711912, 230, 0.035F);
      this.flush = this.init(3883598, 88, 0.045F);
      this.close = this.init(4805473, 120, 0.065F);
      this.open = this.init(2238772, 191, 0.035F);
      this.poll = this.init(2567737, 196, 0.035F);
      this.push = this.init(3358283, 206, 0.065F);
      this.pop = this.init(1251875, 196, 0.025F);
      this.tick = this.init(2042422, 210, 0.05F);
      this.load = this.init(3160391, 200, 0.035F);
      this.read = this.init(4345181, 212, 0.065F);
      this.run = this.init(96, 0.12F);
      this.get = this.init(60, 0.09F);
      this.set = this.init(76, 0.1F);
      this.put = this.init(34, 0.06F);
      this.add = 385875967;
      this.write = this.init(84, 0.06F);
      this.reset = ThemePaletteManager.init_I_I_2(-7397596, ThemePaletteManager.ColorRole.ACCENT, state);
      this.start = ThemePaletteManager.init_I_I_2(1157577531, ThemePaletteManager.ColorRole.ACCENT, state);
      this.stop = ThemePaletteManager.init_I_I_2(-12588930, ThemePaletteManager.ColorRole.SUCCESS, state);
      this.next = ThemePaletteManager.init_I_I_2(1715464318, ThemePaletteManager.ColorRole.SUCCESS, state);
      this.prev = ThemePaletteManager.init_I_I_2(-854534, ThemePaletteManager.ColorRole.TEXT, state);
      this.valid = ThemePaletteManager.init_I_I_2(-6774093, ThemePaletteManager.ColorRole.TEXT, state);
      this.size = ThemePaletteManager.init_I_I_2(-10853776, ThemePaletteManager.ColorRole.TEXT, state);
      this.data = ThemePaletteManager.init_I_I_2(-43691, ThemePaletteManager.ColorRole.DANGER, state);
   }

   private int init(int rgb, int alpha, float tint) {
      int tinted = UiLayoutMath.init_I_I(0xFF000000 | rgb & 16777215, 0xFF000000 | this.clear & 16777215, tint);
      return alpha << 24 | tinted & 16777215;
   }

   private int init(int alpha, float tint) {
      int tinted = UiLayoutMath.init_I_I(-2300686, 0xFF000000 | this.clear & 16777215, tint);
      return alpha << 24 | tinted & 16777215;
   }
}
