package autismclient.gui.f.b;

import autismclient.util.ThemePaletteManager;
import net.minecraft.resources.Identifier;
import autismclient.gui.ui.widgets.OverlayActionButton;
import autismclient.gui.ui.widgets.UiLayoutMath;

public final class CharEncoder {
   public static final float init_F = 2.0F;
   private static final int init_I = 11;
   private static final int load = 12;
   private static final int read = 13;
   private static final int write = 14;
   private static final int update = 3;
   private static final int sync = 2;
   private static final int flush = 13;
   private static final int close = 1;
   private autismclient.gui.ui.UiColorScheme init_JsonUtil;
   private int open = -2147483648;

   private static int init_I_I_ct_DataUtil(int color, ThemePaletteManager.Inner_DataUtil channel) {
      return ThemePaletteManager.init_I_I_ct_DataUtil(color, channel);
   }

   private autismclient.gui.ui.UiColorScheme init_JsonUtil() {
      int generation = ThemePaletteManager.init_I();
      if (this.init_JsonUtil == null) {
         this.init_JsonUtil = new autismclient.gui.ui.UiColorScheme();
         this.open = generation;
      } else if (this.open != generation) {
         this.init_JsonUtil.init();
         this.open = generation;
      }

      return this.init_JsonUtil;
   }

   public static int init_I_I(int color) {
      return init_I_I_2(color, 13, 10);
   }

   private static int init_I_I_2(int color, int numerator, int denominator) {
      int alpha = color >>> 24 & 0xFF;
      int boosted = Math.min(255, alpha * numerator / denominator);
      return boosted << 24 | color & 16777215;
   }

   public Identifier init_Identifier_l(l tone) {
      Identifier var10000;
      switch (tone) {
         case init:
            var10000 = autismclient.gui.f.a.DataUtil.read;
            break;
         case load:
            var10000 = autismclient.gui.f.a.DataUtil.load;
            break;
         case read:
         case write:
         case update:
            var10000 = autismclient.gui.f.a.DataUtil.init;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public int init_I() {
      return 8;
   }

   public int load() {
      return Math.max(14, this.init_I_l_I(l.load, 3));
   }

   public int read() {
      return 3;
   }

   public int write() {
      return 2;
   }

   public int update() {
      return Math.max(13, this.init_I_l_I(l.read, 3));
   }

   public int sync() {
      return 1;
   }

   public float init_F() {
      return 1.0F;
   }

   public int load(int value) {
      return value;
   }

   public float init_F_F(float value) {
      return value;
   }

   public autismclient.gui.ui.layout.EdgeInsets init_IoWorker_IoWorker(autismclient.gui.ui.layout.EdgeInsets insets) {
      return insets == null
         ? autismclient.gui.ui.layout.EdgeInsets.init_IoWorker
         : new autismclient.gui.ui.layout.EdgeInsets(this.load(insets.read()), this.load(insets.write()), this.load(insets.update()), this.load(insets.sync()));
   }

   public int init_I_l(l tone) {
      int vanillaHeight = k.init(this.init_Identifier_l(tone));
      if (vanillaHeight > 0) {
         return vanillaHeight;
      }

      byte var10000;
      switch (tone) {
         case init:
            var10000 = 13;
            break;
         case load:
            var10000 = 12;
            break;
         case read:
         case write:
         case update:
            var10000 = 11;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public int init_I_l_I(l tone, int extraSpacing) {
      byte var10000;
      switch (tone) {
         case init:
            var10000 = 3;
            break;
         case load:
            var10000 = 2;
            break;
         case read:
         case write:
         case update:
            var10000 = 2;
            break;
         default:
            throw new MatchException(null, null);
      }

      int minimumSpacing = var10000;
      return this.init_I_l(tone) + Math.max(this.load(extraSpacing), minimumSpacing);
   }

   public int flush() {
      return 1;
   }

   public int close() {
      return 1;
   }

   public int open() {
      return 1;
   }

   public int load(l tone) {
      int var10000;
      switch (tone) {
         case init:
         case load:
         case read:
            var10000 = init_I_I_ct_DataUtil(-854534, ThemePaletteManager.Inner_DataUtil.TEXT);
            break;
         case write:
            var10000 = init_I_I_ct_DataUtil(-6774093, ThemePaletteManager.Inner_DataUtil.TEXT);
            break;
         case update:
            var10000 = init_I_I_ct_DataUtil(-45747, ThemePaletteManager.Inner_DataUtil.ACCENT);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public int read(int rgb) {
      return this.init_I_I(rgb, 212, 0.035F);
   }

   public int write(int rgb) {
      return this.init_I_I(rgb, 196, 0.035F);
   }

   public int update(int rgb) {
      return this.init_I_I(rgb, 230, 0.035F);
   }

   private int init_I_I(int rgb, int alpha, float tint) {
      int accent = init_I_I_ct_DataUtil(-50373, ThemePaletteManager.Inner_DataUtil.ACCENT);
      int tinted = UiLayoutMath.init_I_I(0xFF000000 | rgb & 16777215, 0xFF000000 | accent & 16777215, tint);
      return alpha << 24 | tinted & 16777215;
   }

   public int poll() {
      return this.init_JsonUtil().update;
   }

   public int push() {
      return UiLayoutMath.init_I_I(this.init_JsonUtil().update, -736814558, 0.16F);
   }

   public int pop() {
      return this.init_JsonUtil().flush;
   }

   public int tick() {
      return autismclient.gui.f.PacketUtil.init(this.init_JsonUtil().flush, 0.8F);
   }

   public int run() {
      return init_I_I_ct_DataUtil(-50373, ThemePaletteManager.Inner_DataUtil.HEADER);
   }

   public int get() {
      return this.init_JsonUtil().run;
   }

   public int set() {
      return this.init_JsonUtil().get;
   }

   public int put() {
      return this.init_JsonUtil().add;
   }

   public int add() {
      return init_I_I_ct_DataUtil(this.read(2756114), ThemePaletteManager.Inner_DataUtil.DANGER);
   }

   public int clear() {
      return init_I_I_ct_DataUtil(-33411, ThemePaletteManager.Inner_DataUtil.DANGER);
   }

   public int init_I_ByteReader_StreamU_2(autismclient.gui.ui.layout.ButtonNode.Inner_StreamUtil variant) {
      int var10000;
      switch (variant) {
         case PRIMARY:
         case DANGER:
         case SUCCESS:
            var10000 = UiLayoutMath.init_I_I(this.init_JsonUtil().load, this.flush(variant), 0.2F);
            break;
         case SECONDARY:
            var10000 = this.init_JsonUtil().load;
            break;
         case GHOST:
            var10000 = this.init_JsonUtil().open;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private int flush(autismclient.gui.ui.layout.ButtonNode.Inner_StreamUtil variant) {
      int var10000;
      switch (variant) {
         case PRIMARY:
            var10000 = init_I_I_ct_DataUtil(-50373, ThemePaletteManager.Inner_DataUtil.BUTTON);
            break;
         case DANGER:
            var10000 = this.init_JsonUtil().data;
            break;
         case SUCCESS:
            var10000 = this.init_JsonUtil().stop;
            break;
         case SECONDARY:
         case GHOST:
            var10000 = this.init_JsonUtil().set;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public int init_I_ByteReader_StreamU_Z_2(autismclient.gui.ui.layout.ButtonNode.Inner_StreamUtil variant, boolean active) {
      return !active ? this.close(variant) : this.init_I_ByteReader_StreamU_2(variant);
   }

   private int close(autismclient.gui.ui.layout.ButtonNode.Inner_StreamUtil variant) {
      int var10000;
      switch (variant) {
         case PRIMARY:
         case DANGER:
         case SUCCESS:
            var10000 = UiLayoutMath.init_I_I(this.init_JsonUtil().load, this.flush(variant), 0.07F);
            break;
         case SECONDARY:
            var10000 = this.init_JsonUtil().load;
            break;
         case GHOST:
            var10000 = this.init_JsonUtil().open;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public int load(autismclient.gui.ui.layout.ButtonNode.Inner_StreamUtil variant) {
      int var10000;
      switch (variant) {
         case PRIMARY:
         case DANGER:
         case SUCCESS:
            var10000 = this.flush(variant);
            break;
         case SECONDARY:
         case GHOST:
            var10000 = this.init_JsonUtil().set;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public int read(autismclient.gui.ui.layout.ButtonNode.Inner_StreamUtil variant) {
      int var10000;
      switch (variant) {
         case PRIMARY:
         case DANGER:
         case SUCCESS:
            var10000 = UiLayoutMath.init_I_I(this.close(variant), this.flush(variant), 0.5F);
            break;
         case SECONDARY:
         case GHOST:
            var10000 = autismclient.gui.f.PacketUtil.init(this.init_JsonUtil().set, 0.75F);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public int write(autismclient.gui.ui.layout.ButtonNode.Inner_StreamUtil variant) {
      int var10000;
      switch (variant) {
         case PRIMARY:
         case DANGER:
         case SUCCESS:
            var10000 = UiLayoutMath.init_I_I(this.flush(variant), -1, 0.18F);
            break;
         case SECONDARY:
         case GHOST:
            var10000 = this.init_JsonUtil().run;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public int update(autismclient.gui.ui.layout.ButtonNode.Inner_StreamUtil variant) {
      int var10000;
      switch (variant) {
         case PRIMARY:
            var10000 = init_I_I_ct_DataUtil(-2828, ThemePaletteManager.Inner_DataUtil.TEXT);
            break;
         case DANGER:
            var10000 = init_I_I_ct_DataUtil(-42406, ThemePaletteManager.Inner_DataUtil.DANGER);
            break;
         case SUCCESS:
            var10000 = init_I_I_ct_DataUtil(-4656181, ThemePaletteManager.Inner_DataUtil.SUCCESS);
            break;
         case SECONDARY:
         case GHOST:
            var10000 = this.load(l.read);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public int sync(autismclient.gui.ui.layout.ButtonNode.Inner_StreamUtil variant) {
      int var10000;
      switch (variant) {
         case PRIMARY:
            var10000 = init_I_I_ct_DataUtil(-2828, ThemePaletteManager.Inner_DataUtil.TEXT);
            break;
         case DANGER:
            var10000 = init_I_I_ct_DataUtil(-6521989, ThemePaletteManager.Inner_DataUtil.DANGER);
            break;
         case SUCCESS:
            var10000 = init_I_I_ct_DataUtil(-7821680, ThemePaletteManager.Inner_DataUtil.SUCCESS);
            break;
         case SECONDARY:
         case GHOST:
            var10000 = this.load(l.write);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public int init_I_ByteReader_StreamU_Z(OverlayActionButton.Inner_StreamUtil variant, boolean active) {
      if (variant == OverlayActionButton.Inner_StreamUtil.FILTER_ON) {
         return 1430088703;
      } else {
         return variant == OverlayActionButton.Inner_StreamUtil.FILTER_OFF
            ? 1713842993
            : this.init_I_ByteReader_StreamU_Z_2(this.init_ByteReader_StreamU_ByteReader_StreamU(variant), active);
      }
   }

   private int load(OverlayActionButton.Inner_StreamUtil variant) {
      return this.close(this.init_ByteReader_StreamU_ByteReader_StreamU(variant));
   }

   public int load(OverlayActionButton.Inner_StreamUtil variant, boolean active) {
      if (variant == OverlayActionButton.Inner_StreamUtil.FILTER_ON) {
         return -9466369;
      } else if (variant == OverlayActionButton.Inner_StreamUtil.FILTER_OFF) {
         return -11117210;
      } else {
         return active
            ? this.load(this.init_ByteReader_StreamU_ByteReader_StreamU(variant))
            : this.read(this.init_ByteReader_StreamU_ByteReader_StreamU(variant));
      }
   }

   private int read(OverlayActionButton.Inner_StreamUtil variant) {
      return this.read(this.init_ByteReader_StreamU_ByteReader_StreamU(variant));
   }

   public int init_I_ByteReader_StreamU(OverlayActionButton.Inner_StreamUtil variant) {
      if (variant == OverlayActionButton.Inner_StreamUtil.FILTER_ON) {
         return -7231489;
      } else {
         return variant == OverlayActionButton.Inner_StreamUtil.FILTER_OFF ? -9143161 : this.write(this.init_ByteReader_StreamU_ByteReader_StreamU(variant));
      }
   }

   public int read(OverlayActionButton.Inner_StreamUtil variant, boolean active) {
      if (variant == OverlayActionButton.Inner_StreamUtil.FILTER_ON) {
         return -1;
      } else if (variant == OverlayActionButton.Inner_StreamUtil.FILTER_OFF) {
         return -4669753;
      } else {
         return active
            ? this.update(this.init_ByteReader_StreamU_ByteReader_StreamU(variant))
            : this.sync(this.init_ByteReader_StreamU_ByteReader_StreamU(variant));
      }
   }

   private int write(OverlayActionButton.Inner_StreamUtil variant) {
      return this.sync(this.init_ByteReader_StreamU_ByteReader_StreamU(variant));
   }

   private autismclient.gui.ui.layout.ButtonNode.Inner_StreamUtil init_ByteReader_StreamU_ByteReader_StreamU(OverlayActionButton.Inner_StreamUtil variant) {
      autismclient.gui.ui.layout.ButtonNode.Inner_StreamUtil var10000;
      switch (variant) {
         case PRIMARY:
            var10000 = autismclient.gui.ui.layout.ButtonNode.Inner_StreamUtil.PRIMARY;
            break;
         case SECONDARY:
            var10000 = autismclient.gui.ui.layout.ButtonNode.Inner_StreamUtil.SECONDARY;
            break;
         case GHOST:
            var10000 = autismclient.gui.ui.layout.ButtonNode.Inner_StreamUtil.GHOST;
            break;
         case DANGER:
            var10000 = autismclient.gui.ui.layout.ButtonNode.Inner_StreamUtil.DANGER;
            break;
         case SUCCESS:
            var10000 = autismclient.gui.ui.layout.ButtonNode.Inner_StreamUtil.SUCCESS;
            break;
         case FILTER_ON:
            var10000 = autismclient.gui.ui.layout.ButtonNode.Inner_StreamUtil.PRIMARY;
            break;
         case FILTER_OFF:
            var10000 = autismclient.gui.ui.layout.ButtonNode.Inner_StreamUtil.GHOST;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public int reset() {
      return this.init_JsonUtil().pop;
   }

   public int start() {
      return this.init_JsonUtil().tick;
   }

   public int stop() {
      return this.init_JsonUtil().open;
   }

   public int next() {
      return this.init_JsonUtil().push;
   }

   public int prev() {
      return init_I_I_ct_DataUtil(this.read(2308139), ThemePaletteManager.Inner_DataUtil.TOGGLE);
   }

   public int valid() {
      return init_I_I_ct_DataUtil(this.read(1183762), ThemePaletteManager.Inner_DataUtil.BUTTON);
   }

   public int size() {
      return 905969664;
   }

   public int data() {
      return this.read(592139);
   }
}
