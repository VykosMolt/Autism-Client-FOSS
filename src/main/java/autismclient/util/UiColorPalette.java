package autismclient.util;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public class UiColorPalette {
   private static final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = -5093304;
   private static final int load = 1722959944;
   private static final int read = 1715151911;
   private static final int write = -791058;
   private static final int update = -3361096;
   private static final int sync = -6388344;
   private static final int flush = -1938838;
   private static final int close = -11018872;
   private static final int open = -8592483;
   private static final int poll = -27948;
   private static final int push = -19091;
   private static final int pop = -8340993;
   private static final int tick = -16777216;
   private static final int run = -10134;
   private static final int get = -9115393;
   private static final int set = -461586;
   private static final int put = -5195582;
   private static final int add = -3921;

   public static int init(int color) {
      return init(color, 11, 10);
   }

   public static int load(int color) {
      return init(color, 13, 10);
   }

   private static int init(int color, int numerator, int denominator) {
      int alpha = color >>> 24 & 0xFF;
      int boosted = Math.min(255, alpha * numerator / denominator);
      return boosted << 24 | color & 16777215;
   }

   public static int init() {
      return ThemePaletteManager.init_I_I_ct_DataUtil(-5093304, ThemePaletteManager.ColorRole.ACCENT);
   }

   public static int load() {
      return ThemePaletteManager.init_I_I_ct_DataUtil(1722959944, ThemePaletteManager.ColorRole.ACCENT);
   }

   public static int read() {
      return ThemePaletteManager.init_I_I_ct_DataUtil(-5093304, ThemePaletteManager.ColorRole.ACCENT);
   }

   public static int write() {
      return ThemePaletteManager.init_I_I_ct_DataUtil(-7522248, ThemePaletteManager.ColorRole.ACCENT);
   }

   public static int update() {
      return init_CharEncoder.reset();
   }

   public static int sync() {
      return init_CharEncoder.poll();
   }

   public static int flush() {
      return init_CharEncoder.set();
   }

   public static int close() {
      return init_CharEncoder.push();
   }

   public static int open() {
      return init_CharEncoder.poll();
   }

   public static int poll() {
      return init_CharEncoder.pop();
   }

   public static int push() {
      return init_CharEncoder.pop();
   }

   public static int pop() {
      return ThemePaletteManager.init_I_I_ct_DataUtil(1715151911, ThemePaletteManager.ColorRole.OUTLINE);
   }

   public static int tick() {
      return init_CharEncoder.init_I_ByteReader_StreamU_2(autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY);
   }

   public static int run() {
      return init_CharEncoder.next();
   }

   public static int get() {
      return init_CharEncoder.set();
   }

   public static int set() {
      return run();
   }

   public static int put() {
      return ThemePaletteManager.init_I_I_ct_DataUtil(-5093304, ThemePaletteManager.ColorRole.ACCENT);
   }

   public static int add() {
      return ThemePaletteManager.init_I_I_ct_DataUtil(-6388344, ThemePaletteManager.ColorRole.TEXT);
   }

   public static int clear() {
      return init(857679146);
   }

   public static int reset() {
      return ThemePaletteManager.init_I_I_ct_DataUtil(-3361096, ThemePaletteManager.ColorRole.TEXT);
   }

   public static int start() {
      return init_CharEncoder.reset();
   }

   public static int stop() {
      return init_CharEncoder.set();
   }

   public static int next() {
      return init_CharEncoder.add();
   }

   public static int prev() {
      return ThemePaletteManager.init_I_I_ct_DataUtil(-1938838, ThemePaletteManager.ColorRole.DANGER);
   }

   public static int valid() {
      return ThemePaletteManager.init_I_I_ct_DataUtil(-1938838, ThemePaletteManager.ColorRole.DANGER);
   }

   public static int size() {
      return init_CharEncoder.init_I_ByteReader_StreamU_2(autismclient.gui.ui.layout.ButtonNode.Variant.SUCCESS);
   }

   public static int data() {
      return ThemePaletteManager.init_I_I_ct_DataUtil(-11018872, ThemePaletteManager.ColorRole.SUCCESS);
   }

   public static int value() {
      return ThemePaletteManager.init_I_I_ct_DataUtil(-11018872, ThemePaletteManager.ColorRole.SUCCESS);
   }

   public static int cache() {
      return -8592483;
   }

   public static int state() {
      return -27948;
   }

   public static int queue() {
      return -19091;
   }

   public static int buffer() {
      return -8340993;
   }

   public static int entry() {
      return -10134;
   }

   public static int node() {
      return -9115393;
   }

   public static int task() {
      return -461586;
   }

   public static int pool() {
      return -5195582;
   }

   public static int index() {
      return -3921;
   }

   public static int count() {
      return init_CharEncoder.reset();
   }

   public static int flags() {
      return init_CharEncoder.stop();
   }

   public static int level() {
      return init_CharEncoder.next();
   }

   public static int frame() {
      return init_CharEncoder.prev();
   }

   public static int block() {
      return ThemePaletteManager.init_I_I_ct_DataUtil(-10035062, ThemePaletteManager.ColorRole.TOGGLE);
   }

   public static int chunk() {
      return ThemePaletteManager.init_I_I_ct_DataUtil(-12531347, ThemePaletteManager.ColorRole.TOGGLE);
   }

   public static int table() {
      return ThemePaletteManager.init_I_I_ct_DataUtil(-2828, ThemePaletteManager.ColorRole.TEXT);
   }

   public static int init(boolean c2s, int rowIndex, boolean hovered) {
      if (hovered) {
         return c2s ? init(1145068152) : init(1146173989);
      } else if (c2s) {
         return (rowIndex & 1) == 0 ? init(539243587) : init(405355858);
      } else {
         return (rowIndex & 1) == 0 ? init(539632411) : init(405939741);
      }
   }

   public static int init(boolean c2s, int rowIndex) {
      if (c2s) {
         return (rowIndex & 1) == 0 ? -6496513 : -8861448;
      } else {
         return (rowIndex & 1) == 0 ? -14193 : -20889;
      }
   }

   public static int init(boolean hovered) {
      return hovered ? init_CharEncoder.read(5413731) : init_CharEncoder.write(4225104);
   }

   public static int store() {
      return -4787259;
   }

   public static int drain() {
      return -9184882;
   }

   public static int load(boolean hovered) {
      return hovered ? init(-2000670660) : init(1721245482);
   }

   public static int fill() {
      return -1549714;
   }

   public static int mark() {
      return init(822083583);
   }

   public static int scan() {
      return ThemePaletteManager.init_I_I_ct_DataUtil(-791058, ThemePaletteManager.ColorRole.TEXT);
   }

   public static int parse() {
      return ThemePaletteManager.init_I_I_ct_DataUtil(-3361096, ThemePaletteManager.ColorRole.TEXT);
   }

   public static int bind() {
      return ThemePaletteManager.init_I_I_ct_DataUtil(-6388344, ThemePaletteManager.ColorRole.TEXT);
   }

   public static int a() {
      return ThemePaletteManager.init_I_I_ct_DataUtil(-6388344, ThemePaletteManager.ColorRole.TEXT);
   }

   public static int b() {
      return ThemePaletteManager.init_I_I_ct_DataUtil(-791058, ThemePaletteManager.ColorRole.TEXT);
   }

   public static int c() {
      return init_CharEncoder.set();
   }

   public static int d() {
      return init_CharEncoder.update(1711912);
   }

   public static int e() {
      return -16777216;
   }

   public static int f() {
      return init_CharEncoder.read(658190);
   }

   public static int g() {
      return load(822083583);
   }

   public static int h() {
      return -4668985;
   }

   public static int i() {
      return -1511694;
   }

   public static int j() {
      return ThemePaletteManager.init_I_I_ct_DataUtil(-5093304, ThemePaletteManager.ColorRole.ACCENT);
   }

   public static int k() {
      return ThemePaletteManager.init_I_I_ct_DataUtil(-7522248, ThemePaletteManager.ColorRole.ACCENT);
   }

   public static int l() {
      return -9997443;
   }

   public static void init(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int c) {
      autismclient.gui.f.PacketUtil.load(ctx, autismclient.gui.f.NetHelper.init(x, y, w, h), c);
   }

   public static void init(GuiGraphicsExtractor ctx, int x, int y, int width, int color) {
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(x, y, width, 1), color);
   }

   public static void load(GuiGraphicsExtractor ctx, int x, int y, int size, int color) {
      for (int i = 0; i < 3; i++) {
         int start = x + size - 3 - i * 4;
         autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(start, y + size - 2, 2, 2), color);
         autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(x + size - 2, y + size - 3 - i * 4, 2, 2), color);
      }
   }

   public static void init(GuiGraphicsExtractor ctx, int x, int y, int w, int h, boolean focused) {
      autismclient.gui.f.PacketUtil.read(ctx, autismclient.gui.f.NetHelper.init(x, y, w, h), 5, count(), focused ? init() : c());
   }
}
