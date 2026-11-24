package autismclient.gui.b;

import autismclient.gui.f.PacketUtil;
import autismclient.gui.f.b.k;
import autismclient.util.g.ClassCache;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.FontDescription.Resource;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

public final class JsonUtil {
   private static final FontDescription init_FontDescription = new Resource(Identifier.withDefaultNamespace("alt"));
   private static final int init_I = -1184275;
   private static final int load = -6645094;
   private static final int read = -11207852;
   private static final int write = -42149;
   private static final int update = -1513280;
   private static final int sync = -10790053;
   private static final int flush = -12961222;
   private static final int close = -14671318;
   private static final int open = -2060246;
   private static final int poll = -34274;

   private JsonUtil() {
   }

   public static void init(
      GuiGraphicsExtractor g,
      Font font,
      ClassCache.Inner_ByteReader view,
      int originX,
      int originY,
      int scrollY,
      int mouseX,
      int mouseY,
      List<JsonUtil.Inner_TaskQueue> hitsOut,
      NetHelper in
   ) {
      if (view != null && view.init_ClassCache_FileCac() != null) {
         JsonUtil.Inner_BufferHelper c = new JsonUtil.Inner_BufferHelper(g, font, originX, originY, scrollY, mouseX, mouseY, hitsOut);
         ClassCache.Inner_FileCache ex = view.init_ClassCache_FileCac();
         switch (init_Str_Str(ex.init_Str())) {
            case "enchantment":
               init(c, ex);
               break;
            case "furnace":
            case "blast_furnace":
            case "smoker":
               load(c, ex);
               break;
            case "brewing_stand":
               read(c, ex);
               break;
            case "anvil":
               init(c, ex, view, in.init_SyncManager.init_Z() ? in.init_SyncManager.init_Str() : null);
               break;
            case "beacon":
               init(c, ex, in);
               break;
            case "merchant":
               update(c, ex);
               break;
            case "lectern":
               sync(c, ex);
               break;
            case "stonecutter":
            case "loom":
               write(c, ex);
         }
      }
   }

   public static int[] init_IArr_ClassCache_ByteRea(ClassCache.Inner_ByteReader view) {
      int w = 18;
      int h = 18;
      if (view != null) {
         for (ClassCache.Inner_MapStore s : view.init_List()) {
            w = Math.max(w, s.init_I() + 18);
            h = Math.max(h, s.load() + 18);
         }

         int[] ext = load(view);
         w = Math.max(w, ext[0]);
         h = Math.max(h, ext[1]);
      }

      return new int[]{w, h};
   }

   private static int[] load(ClassCache.Inner_ByteReader view) {
      ClassCache.Inner_FileCache ex = view.init_ClassCache_FileCac();
      if (ex == null) {
         return new int[]{0, 0};
      }

      int[] var10000;
      switch (init_Str_Str(ex.init_Str())) {
         case "enchantment":
            var10000 = new int[]{174, 66};
            break;
         case "merchant":
            var10000 = new int[]{144, 8 + Math.max(1, ex.init_List().size()) * 20};
            break;
         case "beacon":
            var10000 = new int[]{232, 108};
            break;
         case "lectern":
            var10000 = new int[]{120, 44};
            break;
         case "anvil":
            var10000 = new int[]{132, 50};
            break;
         case "stonecutter":
            var10000 = new int[]{130, 54};
            break;
         case "loom":
            var10000 = new int[]{130, 70};
            break;
         default:
            var10000 = new int[]{0, 0};
      }

      return var10000;
   }

   private static void init(JsonUtil.Inner_BufferHelper c, ClassCache.Inner_FileCache ex) {
      int[] d = ex.init_IArr();
      EnchantmentNames names = EnchantmentNames.getInstance();
      names.initSeed(init(d, 3, 0));
      Registry<Enchantment> reg = init();
      int rw = 120;
      int rh = 20;

      for (int i = 0; i < 3; i++) {
         int cost = init(d, i, 0);
         boolean enabled = cost > 0;
         int sx = c.init(54);
         int sy = c.load(i * 22);
         boolean hover = enabled && c.init(sx, sy, rw, rh);
         c.init(sx, sy, rw, rh, hover ? 872415231 : (enabled ? 452984831 : 234881023), enabled ? -10790053 : -12961222);
         String sga = names.getRandomName(c.init_Font, rw - 22).getString();
         c.init(Component.literal(sga).setStyle(Style.EMPTY.withFont(init_FontDescription)), sx + 4, sy + 3, enabled ? -9803158 : -12632257);
         int clueId = init(d, 4 + i, -1);
         int clueLvl = init(d, 7 + i, 1);
         if (enabled && clueId >= 0 && reg != null) {
            try {
               reg.get(clueId).ifPresent(h -> c.init(Enchantment.getFullname(h, clueLvl), sx + 4, sy + 12, -1513280));
            } catch (Throwable var17) {
            }
         }

         if (enabled) {
            String cs = Integer.toString(cost);
            c.init(cs, sx + rw - 4 - k.init(c.init_Font, cs, autismclient.gui.f.a.DataUtil.init, -11207852), sy + 3, -11207852);
            c.init_List.add(new JsonUtil.Inner_TaskQueue(sx, sy, rw, rh, new JsonUtil.Inner_CachePool(i)));
         }
      }
   }

   private static void load(JsonUtil.Inner_BufferHelper c, ClassCache.Inner_FileCache ex) {
      int[] d = ex.init_IArr();
      int litTime = init(d, 0, 0);
      int litDur = init(d, 1, 0);
      int cook = init(d, 2, 0);
      int cookTotal = init(d, 3, 0);
      double burn = litDur > 0 ? init_D_D((double)litTime / litDur) : 0.0;
      double prog = cookTotal > 0 ? init_D_D((double)cook / cookTotal) : 0.0;
      int ax = c.init(20);
      int ay = c.load(23);
      c.init(ax, ay, 46, 6, -14671318);
      c.init(ax, ay, (int)(46.0 * prog), 6, -2060246);
      int fx = c.init(6);
      int fyTop = c.load(20);
      int fh = 14;
      c.init(fx, fyTop, 6, fh, -14671318);
      int lit = (int)(fh * burn);
      c.init(fx, fyTop + (fh - lit), 6, lit, -34274);
   }

   private static void read(JsonUtil.Inner_BufferHelper c, ClassCache.Inner_FileCache ex) {
      int[] d = ex.init_IArr();
      int brewTime = init(d, 0, 0);
      int fuel = init(d, 1, 0);
      double brew = brewTime > 0 ? init_D_D((400 - brewTime) / 400.0) : 0.0;
      int bx = c.init(50);
      int byTop = c.load(18);
      c.init(bx, byTop, 6, 30, -14671318);
      c.init(bx, byTop, 6, (int)(30.0 * brew), -3116832);
      int gx = c.init(0);
      int gy = c.load(50);
      c.init(gx, gy, 18, 4, -14671318);
      c.init(gx, gy, (int)(18.0 * init_D_D(fuel / 20.0)), 4, -1003472);
   }

   private static void init(JsonUtil.Inner_BufferHelper c, ClassCache.Inner_FileCache ex, ClassCache.Inner_ByteReader view, String renameText) {
      int cost = init(ex.init_IArr(), 0, 0);
      int bx = c.init(0);
      int by = c.load(0);
      int bw = 132;
      int bh = 14;
      boolean focused = renameText != null;
      c.init(bx, by, bw, bh, 570425344, focused ? -1513280 : -10790053);
      ItemStack result = init(view, 2);
      String label;
      int color;
      if (focused) {
         label = renameText + "_";
         color = -1184275;
      } else {
         boolean has = !result.isEmpty();
         label = has ? result.getHoverName().getString() : "Rename...";
         color = has ? -1184275 : -6645094;
      }

      c.init(Component.literal(init(c.init_Font, label, bw - 6)), bx + 3, by + 3, color);
      c.init_List.add(new JsonUtil.Inner_TaskQueue(bx, by, bw, bh, new JsonUtil.Inner_JsonUtil()));
      if (cost > 0) {
         c.init("Cost: " + cost, c.init(0), c.load(38), cost >= 40 ? -42149 : -11207852);
      }
   }

   private static void init(JsonUtil.Inner_BufferHelper c, ClassCache.Inner_FileCache ex, NetHelper in) {
      int[] d = ex.init_IArr();
      int tier = init(d, 0, 0);
      int effPrimary = in.init_I >= 0 ? in.init_I : init(d, 1, -1);
      int effSecondary = in.load >= 0 ? in.load : init(d, 2, -1);
      c.init("Beacon tier " + tier, c.init(24), c.load(2), tier > 0 ? -1184275 : -6645094);
      List<List<Holder<MobEffect>>> effects = BeaconBlockEntity.BEACON_EFFECTS;
      int primaryW = 40;

      for (int row = 0; row < 3 && row < effects.size() && row < tier; row++) {
         for (Holder<MobEffect> h : effects.get(row)) {
            primaryW = Math.max(primaryW, k.init(c.init_Font, init_Str_I(init_I_Holder(h)), autismclient.gui.f.a.DataUtil.init, 0) + 16);
         }
      }

      int secondaryW = 40;
      if (tier >= 4) {
         if (effPrimary >= 0) {
            secondaryW = Math.max(secondaryW, k.init(c.init_Font, init_Str_I(effPrimary) + " II", autismclient.gui.f.a.DataUtil.init, 0) + 16);
         }

         if (effects.size() >= 4) {
            for (Holder<MobEffect> h : effects.get(3)) {
               secondaryW = Math.max(secondaryW, k.init(c.init_Font, init_Str_I(init_I_Holder(h)), autismclient.gui.f.a.DataUtil.init, 0) + 16);
            }
         }
      }

      int y = 14;
      c.init("Power", c.init(24), c.load(y), -6645094);
      y += 10;
      int primaryBottom = y;

      for (int row = 0; row < 3 && row < effects.size() && row < tier; row++) {
         for (Holder<MobEffect> h : effects.get(row)) {
            int id = init_I_Holder(h);
            init(c, c.init(24), c.load(primaryBottom), primaryW, 11, init_Str_I(id), id == effPrimary, new JsonUtil.Inner_StreamUtil(false, id));
            primaryBottom += 12;
         }
      }

      if (tier >= 4) {
         int sy = 14;
         c.init("Secondary", c.init(132), c.load(sy), -6645094);
         sy += 10;
         if (effPrimary >= 0) {
            init(
               c,
               c.init(132),
               c.load(sy),
               secondaryW,
               11,
               init_Str_I(effPrimary) + " II",
               effSecondary == effPrimary,
               new JsonUtil.Inner_StreamUtil(true, effPrimary)
            );
            sy += 12;
         }

         if (effects.size() >= 4) {
            for (Holder<MobEffect> h : effects.get(3)) {
               int id = init_I_Holder(h);
               init(c, c.init(132), c.load(sy), secondaryW, 11, init_Str_I(id), effSecondary == id, new JsonUtil.Inner_StreamUtil(true, id));
               sy += 12;
            }
         }
      }

      init(c, c.init(24), c.load(primaryBottom + 6), 0, 12, "Confirm", new JsonUtil.Inner_DataUtil(effPrimary, effSecondary));
   }

   private static void init(JsonUtil.Inner_BufferHelper c, int sx, int sy, int w, int h, String label, boolean selected, JsonUtil.Inner_ConfigStore act) {
      boolean hover = c.init(sx, sy, w, h);
      c.init(sx, sy, w, h, selected ? 861207380 : (hover ? 872415231 : 452984831), selected ? -11207852 : -10790053);
      int color = selected ? -11207852 : -1184275;
      c.init(k.load(c.init_Font, label, w - 6, autismclient.gui.f.a.DataUtil.init, color), sx + 3, sy + 2, color);
      c.init_List.add(new JsonUtil.Inner_TaskQueue(sx, sy, w, h, act));
   }

   private static void write(JsonUtil.Inner_BufferHelper c, ClassCache.Inner_FileCache ex) {
      boolean loom = init_Str_Str(ex.init_Str()).equals("loom");
      int sy = loom ? 56 : 40;
      int pw = init(c.init_Font, "< Prev");
      int nw = init(c.init_Font, "Next >");
      int bx = Math.max(0, (130 - (pw + 6 + nw)) / 2);
      init(c, c.init(bx), c.load(sy), pw, 12, "< Prev", new JsonUtil.Inner_NetHelper(-1));
      init(c, c.init(bx + pw + 6), c.load(sy), nw, 12, "Next >", new JsonUtil.Inner_NetHelper(1));
   }

   private static void update(JsonUtil.Inner_BufferHelper c, ClassCache.Inner_FileCache ex) {
      List<ClassCache.Inner_ListPool> trades = ex.init_List();
      int rw = 86;
      int rh = 18;

      for (int i = 0; i < trades.size(); i++) {
         ClassCache.Inner_ListPool t = trades.get(i);
         int sx = c.init(0);
         int sy = c.load(8 + i * 20);
         boolean hover = c.init(sx, sy, rw, rh);
         c.init(sx, sy, rw, rh, hover ? 872415231 : 352321535, t.init_Z() ? -8372160 : -10790053);
         c.init(t.init_ItemStack(), sx + 2, sy + 1);
         if (!t.load_ItemStack().isEmpty()) {
            c.init(t.load_ItemStack(), sx + 20, sy + 1);
         }

         c.init(">", sx + 40, sy + 5, -4210753);
         c.init(t.read_ItemStack(), sx + 50, sy + 1);
         if (t.init_Z()) {
            c.init(sx + 1, sy + rh / 2, rw - 2, 1, -1057013696);
         }

         c.init_List.add(new JsonUtil.Inner_TaskQueue(sx, sy, rw, rh, new JsonUtil.Inner_StateCache(i)));
      }
   }

   private static void sync(JsonUtil.Inner_BufferHelper c, ClassCache.Inner_FileCache ex) {
      c.init("Page " + (init(ex.init_IArr(), 0, 0) + 1), c.init(24), c.load(2), -1184275);
      int pw = init(c.init_Font, "< Prev");
      init(c, c.init(24), c.load(14), pw, 12, "< Prev", new JsonUtil.Inner_CachePool(1));
      init(c, c.init(24 + pw + 6), c.load(14), 0, 12, "Next >", new JsonUtil.Inner_CachePool(2));
      init(c, c.init(24), c.load(30), 0, 12, "Take Book", new JsonUtil.Inner_CachePool(3));
   }

   private static int init(Font font, String label) {
      return Math.max(40, k.init(font, label, autismclient.gui.f.a.DataUtil.init, 0) + 16);
   }

   private static void init(JsonUtil.Inner_BufferHelper c, int sx, int sy, int w, int h, String label, JsonUtil.Inner_ConfigStore act) {
      if (w <= 0) {
         w = init(c.init_Font, label);
      }

      boolean hover = c.init(sx, sy, w, h);
      c.init(sx, sy, w, h, hover ? 872415231 : 452984831, -10790053);
      c.init(label, sx + Math.max(2, (w - k.init(c.init_Font, label, autismclient.gui.f.a.DataUtil.init, -1184275)) / 2), sy + 2, -1184275);
      c.init_List.add(new JsonUtil.Inner_TaskQueue(sx, sy, w, h, act));
   }

   private static Registry<Enchantment> init() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null) {
         return null;
      }

      try {
         return mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
      } catch (Throwable t) {
         return null;
      }
   }

   private static String init_Str_I(int id) {
      return id < 0 ? "-" : BuiltInRegistries.MOB_EFFECT.get(id).map(h -> h.value().getDisplayName().getString()).orElse("?");
   }

   private static int init_I_Holder(Holder<MobEffect> h) {
      return h == null ? -1 : BuiltInRegistries.MOB_EFFECT.getId(h.value());
   }

   private static ItemStack init(ClassCache.Inner_ByteReader view, int handler) {
      for (ClassCache.Inner_MapStore s : view.init_List()) {
         if (s.read() == handler) {
            return s.init_ItemStack() == null ? ItemStack.EMPTY : s.init_ItemStack();
         }
      }

      return ItemStack.EMPTY;
   }

   private static int init(int[] d, int i, int def) {
      return d != null && i >= 0 && i < d.length ? d[i] : def;
   }

   private static double init_D_D(double v) {
      return v < 0.0 ? 0.0 : Math.min(1.0, v);
   }

   private static String init_Str_Str(String typeId) {
      if (typeId == null) {
         return "";
      }

      int c = typeId.indexOf(58);
      return c >= 0 ? typeId.substring(c + 1) : typeId;
   }

   private static String init(Font font, String s, int maxW) {
      if (font.width(s) <= maxW) {
         return s;
      }

      StringBuilder b = new StringBuilder();

      for (int i = 0; i < s.length() && font.width(b.toString() + s.charAt(i) + "...") <= maxW; i++) {
         b.append(s.charAt(i));
      }

      return b + "...";
   }

   private static final class Inner_BufferHelper {
      private final GuiGraphicsExtractor init_GuiGraphicsExtract;
      private final Font init_Font;
      private final int init_I;
      private final int load;
      private final int read;
      private final int write;
      private final int update;
      private final List<JsonUtil.Inner_TaskQueue> init_List;

      Inner_BufferHelper(GuiGraphicsExtractor g, Font font, int ox, int oy, int scrollY, int mx, int my, List<JsonUtil.Inner_TaskQueue> hits) {
         this.init_GuiGraphicsExtract = g;
         this.init_Font = font;
         this.init_I = ox;
         this.load = oy;
         this.read = scrollY;
         this.write = mx;
         this.update = my;
         this.init_List = hits;
      }

      int init(int wx) {
         return this.init_I + wx;
      }

      int load(int wy) {
         return this.load + wy - this.read;
      }

      boolean init(int sx, int sy, int w, int h) {
         return this.write >= sx && this.write < sx + w && this.update >= sy && this.update < sy + h;
      }

      void init(int sx, int sy, int w, int h, int color) {
         if (w > 0 && h > 0) {
            PacketUtil.init(this.init_GuiGraphicsExtract, autismclient.gui.f.NetHelper.init(sx, sy, w, h), color);
         }
      }

      void init(int sx, int sy, int w, int h, int fill, int outline) {
         PacketUtil.load(this.init_GuiGraphicsExtract, autismclient.gui.f.NetHelper.init(sx, sy, w, h), 4, fill, outline);
      }

      void init(String s, int sx, int sy, int color) {
         k.init(this.init_GuiGraphicsExtract, this.init_Font, s, autismclient.gui.f.a.DataUtil.init, color, sx, sy, false);
      }

      void init(Component comp, int sx, int sy, int color) {
         this.init_GuiGraphicsExtract.text(this.init_Font, comp, sx, sy, color, false);
      }

      void init(ItemStack stack, int sx, int sy) {
         if (stack != null && !stack.isEmpty()) {
            try {
               this.init_GuiGraphicsExtract.item(stack, sx, sy);
               this.init_GuiGraphicsExtract.itemDecorations(this.init_Font, stack, sx, sy);
            } catch (Throwable var5) {
            }
         }
      }
   }

   public record Inner_CachePool(int init) implements JsonUtil.Inner_ConfigStore {
   }

   public sealed interface Inner_ConfigStore
      permits JsonUtil.Inner_CachePool,
      JsonUtil.Inner_StateCache,
      JsonUtil.Inner_DataUtil,
      JsonUtil.Inner_StreamUtil,
      JsonUtil.Inner_JsonUtil,
      JsonUtil.Inner_NetHelper {
   }

   public record Inner_DataUtil(int init, int load) implements JsonUtil.Inner_ConfigStore {
   }

   public record Inner_JsonUtil() implements JsonUtil.Inner_ConfigStore {
   }

   public record Inner_NetHelper(int init) implements JsonUtil.Inner_ConfigStore {
   }

   public record Inner_StateCache(int init) implements JsonUtil.Inner_ConfigStore {
   }

   public record Inner_StreamUtil(boolean init_Z, int init_I) implements JsonUtil.Inner_ConfigStore {
   }

   public record Inner_TaskQueue(int init_I, int load, int read, int write, JsonUtil.Inner_ConfigStore init_JsonUtil_ConfigSto) {
   }
}
