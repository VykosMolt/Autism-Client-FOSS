package autismclient.gui.f.b;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.FilterMode;
import java.awt.Color;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;

public final class NetHelper {
   private static final int init_I = 224;
   private static final int load_I = 171;
   private static final int read_I = 14;
   private static final int write = 13;
   private static final int update = 2;
   private static final int sync = 6;
   private static final int flush = 19;
   private static final int close = 135;
   private static final int open = 107;
   private static final int poll = 102;
   private static final int push = 82;
   private static final int pop = 13;
   private static final int tick = 82;
   private static final int run = 360;
   private static final int get = -1728053248;
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static NetHelper.Inner_StreamUtil init_NetHelper_StreamUt;
   private static NetHelper.Inner_StreamUtil load_NetHelper_StreamUt;
   private static int set = -2147483648;
   private final Consumer<Integer> init_Consumer;
   private final int put;
   private final int add;
   private final int clear;
   private autismclient.gui.f.NetHelper init_NetHelper;
   private boolean init_Z = true;
   private float init_F;
   private float load_F;
   private float read_F;
   private boolean load_Z;
   private String init_Str;
   private int reset;
   private int start = -1;
   private int stop;
   private int next;
   private int prev;
   private int valid;
   private int size;
   private NetHelper.Inner_DataUtil init_NetHelper_DataUtil = NetHelper.Inner_DataUtil.init;

   public NetHelper(autismclient.gui.f.NetHelper anchor, int initialArgb, int defaultArgb, int screenWidth, int screenHeight, Consumer<Integer> onSave) {
      this.init_Consumer = onSave;
      this.put = initialArgb;
      this.add = defaultArgb;
      this.clear = initialArgb >>> 24 & 0xFF;
      this.load_V_I(initialArgb & 16777215);
      int x = anchor == null ? 8 : anchor.init() - 224;
      int y = anchor == null ? 8 : anchor.load() + 4;
      if (anchor != null && y + 171 > screenHeight - 4) {
         y = anchor.write() - 171 - 4;
      }

      this.init_NetHelper = autismclient.gui.f.NetHelper.init(x, y, 224, 171).init_NetHelper(Math.max(1, screenWidth - 4), Math.max(1, screenHeight - 4));
   }

   public boolean init_Z() {
      return this.init_Z;
   }

   public void init_V() {
      this.init_Z = false;
   }

   public boolean init_Z_I_I(int mouseX, int mouseY) {
      return this.init_Z && this.init_NetHelper.init_Z(mouseX, mouseY);
   }

   public void init_V_SyncManager(autismclient.gui.f.SyncManager context) {
      if (this.init_Z) {
         autismclient.gui.f.PacketUtil.init(
            context.init_GuiGraphicsExtract(), autismclient.gui.f.NetHelper.init(0, 0, context.init_I(), context.load_I()), -1728053248
         );
         FieldCache.init(
            context, this.init_NetHelper, "Color", false, false, true, this.init_NetHelper().init_Z(context.read(), context.write()), true, 5, 5, 14
         );
         autismclient.gui.f.NetHelper close = i.load(this.init_NetHelper());
         boolean closeHover = close.init_Z(context.read(), context.write());
         float closeFade = autismclient.gui.f.DataUtil.init("colorpicker-close:" + autismclient.gui.f.DataUtil.init(close), closeHover);
         if (closeFade > 0.001F) {
            autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), close.init(1), 3, Math.round(34.0F * closeFade) << 24 | 16777215);
         }

         autismclient.gui.f.NetHelper sv = this.load_NetHelper();
         this.init_V_SyncManager_NetHelper(context, sv);
         this.write(context, sv);
         autismclient.gui.f.NetHelper hueStrip = this.read_NetHelper();
         this.load(context, hueStrip);
         int hueY = hueStrip.write() + Math.round((1.0F - this.init_F) * (hueStrip.sync() - 1));
         autismclient.gui.f.NetHelper hueMarker = autismclient.gui.f.NetHelper.init(hueStrip.read() - 1, hueY - 1, hueStrip.update() + 2, 3);
         autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), hueMarker, 1, -16777216);
         autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), hueMarker.init(1), 0, context.init_TableIndex().init_JsonUtil().prev);
         autismclient.gui.f.NetHelper current = autismclient.gui.f.NetHelper.init(this.init_NetHelper.read() + 135, this.init_NetHelper.write() + 19, 38, 20);
         autismclient.gui.f.NetHelper draft = autismclient.gui.f.NetHelper.init(this.init_NetHelper.read() + 135 + 44, this.init_NetHelper.write() + 19, 38, 20);
         this.init_V_SyncManager(context, current, this.put);
         this.init_V_SyncManager(context, draft, this.read_I());
         context.init_PoolEntry()
            .load(
               context.init_GuiGraphicsExtract(),
               "Old",
               autismclient.gui.f.NetHelper.init(current.read(), current.load() + 2, current.update(), 10),
               context.init_TableIndex().init_JsonUtil().valid
            );
         context.init_PoolEntry()
            .load(
               context.init_GuiGraphicsExtract(),
               "New",
               autismclient.gui.f.NetHelper.init(draft.read(), draft.load() + 2, draft.update(), 10),
               context.init_TableIndex().init_JsonUtil().valid
            );
         this.init_V_SyncManager(context, this.write_NetHelper(), "R", this.write_I(), -43691);
         this.init_V_SyncManager(context, this.update_NetHelper(), "G", this.update_I(), -11149961);
         this.init_V_SyncManager(context, this.sync_NetHelper(), "B", this.sync_I(), -10053121);
         autismclient.gui.f.NetHelper hex = this.flush_NetHelper();
         e.init(context, hex, this.init_Str, "RRGGBB", this.load_Z, this.reset, this.init_I(), this.load_I());
         context.init_PoolEntry()
            .load(
               context.init_GuiGraphicsExtract(), "#", hex.read() - 9, context.init_PoolEntry().init_I(hex), 8, context.init_TableIndex().init_JsonUtil().valid
            );
         CachePool.init(context, this.close(), "Save", CachePool.Inner_DataUtil.write, this.close().init_Z(context.read(), context.write()), false);
         CachePool.init(context, this.open(), "Reset", CachePool.Inner_DataUtil.init, this.open().init_Z(context.read(), context.write()), false);
         CachePool.init(context, this.poll(), "Cancel", CachePool.Inner_DataUtil.update, this.poll().init_Z(context.read(), context.write()), false);
      }
   }

   public boolean init_Z_I_2(int mouseX, int mouseY, int button) {
      if (!this.init_Z) {
         return false;
      }

      if (button != 0) {
         return true;
      }

      if (!this.init_NetHelper.init_Z(mouseX, mouseY)) {
         this.init_V();
         return true;
      }

      if (i.load(this.init_NetHelper()).init_Z(mouseX, mouseY) || this.poll().init_Z(mouseX, mouseY)) {
         this.init_V();
         return true;
      }

      if (this.close().init_Z(mouseX, mouseY)) {
         if (this.init_Consumer != null) {
            this.init_Consumer.accept(this.read_I());
         }

         this.init_Z = false;
         return true;
      } else {
         if (this.open().init_Z(mouseX, mouseY)) {
            this.load_V_I(this.add & 16777215);
            return true;
         }

         autismclient.gui.f.NetHelper hex = this.flush_NetHelper();
         this.load_Z = hex.init_Z(mouseX, mouseY);
         if (this.load_Z) {
            this.reset = this.init_I_I_NetHelper(mouseX, hex);
            this.start = this.reset;
            this.init_NetHelper_DataUtil = NetHelper.Inner_DataUtil.flush;
            return true;
         }

         if (this.load_NetHelper().init_Z(mouseX, mouseY)) {
            this.init_NetHelper_DataUtil = NetHelper.Inner_DataUtil.load;
            this.init_V_I_I(mouseX, mouseY);
         } else if (this.read_NetHelper().init_Z(mouseX, mouseY)) {
            this.init_NetHelper_DataUtil = NetHelper.Inner_DataUtil.read;
            this.init_V_I(mouseY);
         } else if (this.write_NetHelper().init_Z(mouseX, mouseY)) {
            this.init_NetHelper_DataUtil = NetHelper.Inner_DataUtil.write;
            this.init_V_I(mouseX, this.write_NetHelper(), 0);
         } else if (this.update_NetHelper().init_Z(mouseX, mouseY)) {
            this.init_NetHelper_DataUtil = NetHelper.Inner_DataUtil.update;
            this.init_V_I(mouseX, this.update_NetHelper(), 1);
         } else if (this.sync_NetHelper().init_Z(mouseX, mouseY)) {
            this.init_NetHelper_DataUtil = NetHelper.Inner_DataUtil.sync;
            this.init_V_I(mouseX, this.sync_NetHelper(), 2);
         } else {
            this.init_NetHelper_DataUtil = NetHelper.Inner_DataUtil.init;
         }

         return true;
      }
   }

   public boolean load(int mouseX, int mouseY, int button) {
      if (button == 0) {
         this.init_NetHelper_DataUtil = NetHelper.Inner_DataUtil.init;
      }

      return this.init_Z;
   }

   public boolean init_Z_I(int mouseX, int mouseY, int button, double deltaX, double deltaY) {
      if (this.init_Z && button == 0) {
         switch (this.init_NetHelper_DataUtil) {
            case load:
               this.init_V_I_I(mouseX, mouseY);
               break;
            case read:
               this.init_V_I(mouseY);
               break;
            case write:
               this.init_V_I(mouseX, this.write_NetHelper(), 0);
               break;
            case update:
               this.init_V_I(mouseX, this.update_NetHelper(), 1);
               break;
            case sync:
               this.init_V_I(mouseX, this.sync_NetHelper(), 2);
               break;
            case flush:
               this.reset = this.init_I_I_NetHelper(mouseX, this.flush_NetHelper());
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean init_Z_I(int mouseX, int mouseY, double amount) {
      return this.init_Z;
   }

   public boolean read(int key, int scanCode, int modifiers) {
      if (!this.init_Z) {
         return false;
      }

      if (key == 256) {
         this.init_V();
         return true;
      }

      if (key != 257 && key != 335) {
         if (!this.load_Z) {
            return true;
         }

         boolean ctrl = (modifiers & 2) != 0;
         if (ctrl && key == 67) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.init_Str());
            return true;
         }

         if (ctrl && key == 86) {
            this.init_V_Str_Z(Minecraft.getInstance().keyboardHandler.getClipboard(), this.load_Z());
            return true;
         }

         if (ctrl && key == 65) {
            this.load_V();
            return true;
         }

         if (key == 259 && !this.init_Str.isEmpty()) {
            if (this.load_Z()) {
               this.init_V_Str("");
            } else if (this.reset > 0) {
               this.init_Str = this.init_Str.substring(0, this.reset - 1) + this.init_Str.substring(this.reset);
               this.reset--;
               this.update_V();
            }
         } else if (key == 261) {
            if (this.load_Z()) {
               this.init_V_Str("");
            } else if (this.reset < this.init_Str.length()) {
               this.init_Str = this.init_Str.substring(0, this.reset) + this.init_Str.substring(this.reset + 1);
               this.update_V();
            }
         } else if (key == 268 || key == 269 || key == 263 || key == 262) {
            boolean shift = (modifiers & 1) != 0;
            this.init_V_I_Z(key, shift);
            return true;
         }

         return true;
      } else {
         if (this.load_Z) {
            this.update_V();
         }

         if (this.init_Consumer != null) {
            this.init_Consumer.accept(this.read_I());
         }

         this.init_Z = false;
         return true;
      }
   }

   public boolean init_Z_C(char chr) {
      if (this.init_Z && this.load_Z) {
         if (load_Z_C(chr)) {
            if (this.load_Z()) {
               this.init_V_Str(Character.toString(Character.toUpperCase(chr)));
            } else if (this.init_Str.length() < 6) {
               this.init_Str = (this.init_Str.substring(0, this.reset) + Character.toUpperCase(chr) + this.init_Str.substring(this.reset))
                  .toUpperCase(Locale.ROOT);
               this.reset++;
               this.read_V();
               this.update_V();
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private int init_I_I_NetHelper(int mouseX, autismclient.gui.f.NetHelper hex) {
      int local = Math.max(0, mouseX - hex.read() - 4);
      autismclient.gui.f.PoolEntry text = autismclient.gui.f.UiHelper.init(init_Minecraft.font);
      int previousWidth = 0;

      for (int i = 1; i <= this.init_Str.length(); i++) {
         int width = text.init(this.init_Str, this.init_Str.substring(0, i));
         if (local < (previousWidth + width) / 2.0F) {
            return i - 1;
         }

         previousWidth = width;
      }

      return this.init_Str.length();
   }

   private void init_V_I_Z(int key, boolean shift) {
      if (shift && this.start < 0) {
         this.start = this.reset;
      }

      if (!shift) {
         this.read_V();
      }

      switch (key) {
         case 262:
            this.reset = Math.min(this.init_Str.length(), this.reset + 1);
            break;
         case 263:
            this.reset = Math.max(0, this.reset - 1);
         case 264:
         case 265:
         case 266:
         case 267:
         default:
            break;
         case 268:
            this.reset = 0;
            break;
         case 269:
            this.reset = this.init_Str.length();
      }

      if (shift && this.start == this.reset) {
         this.read_V();
      }
   }

   private void load_V() {
      this.start = 0;
      this.reset = this.init_Str.length();
   }

   private void read_V() {
      this.start = -1;
   }

   private boolean load_Z() {
      return this.start >= 0 && this.start != this.reset;
   }

   private int init_I() {
      return this.load_Z() ? Math.min(this.start, this.reset) : -1;
   }

   private int load_I() {
      return this.load_Z() ? Math.max(this.start, this.reset) : -1;
   }

   private String init_Str() {
      return !this.load_Z() ? this.init_Str : this.init_Str.substring(this.init_I(), this.load_I());
   }

   private void init_V_Str(String value) {
      int start = this.load_Z() ? this.init_I() : this.reset;
      int end = this.load_Z() ? this.load_I() : this.reset;
      String cleaned = init_Str_Str_I(value, 6 - (this.init_Str.length() - (end - start)));
      this.init_Str = (this.init_Str.substring(0, start) + cleaned + this.init_Str.substring(end)).toUpperCase(Locale.ROOT);
      this.reset = start + cleaned.length();
      this.read_V();
      this.update_V();
   }

   private static String init_Str_Str_I(String value, int maxLength) {
      if (value != null && maxLength > 0) {
         String text = value.trim();
         if (text.startsWith("#")) {
            text = text.substring(1);
         }

         if (text.length() >= 8) {
            text = text.substring(text.length() - 6);
         }

         StringBuilder cleaned = new StringBuilder(Math.min(6, maxLength));

         for (int i = 0; i < text.length() && cleaned.length() < maxLength; i++) {
            char chr = text.charAt(i);
            if (load_Z_C(chr)) {
               cleaned.append(Character.toUpperCase(chr));
            }
         }

         return cleaned.toString();
      } else {
         return "";
      }
   }

   private void write_V() {
      this.reset = Math.max(0, Math.min(this.reset, this.init_Str == null ? 0 : this.init_Str.length()));
      if (this.start > (this.init_Str == null ? 0 : this.init_Str.length())) {
         this.start = this.init_Str.length();
      }
   }

   private void update_V() {
      this.write_V();
      if (this.init_Str.length() == 6) {
         try {
            int cursor = this.reset;
            int anchor = this.start;
            this.load_V_I(Integer.parseInt(this.init_Str, 16) & 16777215);
            this.reset = cursor;
            this.start = anchor;
         } catch (Exception var3) {
         }
      }
   }

   private void init_V_Str_Z(String clipboard, boolean replaceSelectionOnly) {
      String cleaned = init_Str_Str_I(clipboard, 6);
      if (cleaned.length() == 6) {
         if (replaceSelectionOnly && this.load_Z()) {
            this.init_V_Str(cleaned);
         } else {
            this.init_Str = cleaned.toUpperCase(Locale.ROOT);
            this.reset = this.init_Str.length();
            this.read_V();
            this.update_V();
         }
      }
   }

   private void init_V_SyncManager_NetHelper(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper area) {
      this.read(context, area);
      init_NetHelper_StreamUt_F(this.init_F).init(context, area);
   }

   private void load(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper area) {
      this.read(context, area);
      init_NetHelper_StreamUt().init(context, area);
   }

   private void read(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper area) {
      autismclient.gui.f.PacketUtil.load(
         context.init_GuiGraphicsExtract(),
         autismclient.gui.f.NetHelper.init(area.read() - 1, area.write() - 1, area.update() + 2, area.sync() + 2),
         5,
         context.init_TableIndex().init_JsonUtil().get
      );
   }

   private void init_V_SyncManager(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String label, int value, int accent) {
      autismclient.gui.f.JsonUtil colors = context.init_TableIndex().init_JsonUtil();
      context.init_PoolEntry()
         .load(context.init_GuiGraphicsExtract(), label, autismclient.gui.f.NetHelper.init(bounds.read() - 15, bounds.write(), 12, bounds.sync()), colors.valid);
      int radius = Math.min(3, bounds.sync() / 2);
      autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), bounds, radius, colors.pop, colors.set);
      int fillW = Math.max(0, Math.round((bounds.update() - 2) * (value / 255.0F)));
      if (fillW > 0) {
         autismclient.gui.f.PacketUtil.load(
            context.init_GuiGraphicsExtract(),
            autismclient.gui.f.NetHelper.init(bounds.read() + 1, bounds.write() + 1, fillW, bounds.sync() - 2),
            Math.max(1, radius - 1),
            accent
         );
      }

      int knobX = Math.min(bounds.init() - 3, bounds.read() + 1 + Math.round((bounds.update() - 3) * (value / 255.0F)));
      autismclient.gui.f.PacketUtil.load(
         context.init_GuiGraphicsExtract(), autismclient.gui.f.NetHelper.init(knobX, bounds.write() + 2, 2, Math.max(1, bounds.sync() - 4)), 1, -1
      );
      context.init_PoolEntry()
         .load(context.init_GuiGraphicsExtract(), Integer.toString(value), bounds.init() + 5, context.init_PoolEntry().init_I(bounds), 24, colors.prev);
   }

   private void init_V_SyncManager(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, int color) {
      autismclient.gui.f.PacketUtil.load(
         context.init_GuiGraphicsExtract(), bounds, 3, 0xFF000000 | color & 16777215, context.init_TableIndex().init_JsonUtil().get
      );
   }

   private void write(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper area) {
      int x = area.read() + Math.round(this.load_F * (area.update() - 1));
      int y = area.write() + Math.round((1.0F - this.read_F) * (area.sync() - 1));
      autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract(context.init_GuiGraphicsExtract(), x, y, 4.0F, -16777216);
      autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract(context.init_GuiGraphicsExtract(), x, y, 3.0F, -1);
      autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract(context.init_GuiGraphicsExtract(), x, y, 2.0F, 0xFF000000 | this.stop);
   }

   private void init_V_I_I(int mouseX, int mouseY) {
      autismclient.gui.f.NetHelper area = this.load_NetHelper();
      this.load_F = init_F_F((float)(mouseX - area.read()) / Math.max(1, area.update() - 1));
      this.read_F = init_F_F(1.0F - (float)(mouseY - area.write()) / Math.max(1, area.sync() - 1));
      this.sync_V();
   }

   private void init_V_I(int mouseY) {
      autismclient.gui.f.NetHelper area = this.read_NetHelper();
      this.init_F = init_F_F(1.0F - (float)(mouseY - area.write()) / Math.max(1, area.sync() - 1));
      this.sync_V();
   }

   private void init_V_I(int mouseX, autismclient.gui.f.NetHelper bounds, int channel) {
      int value = Math.round(init_F_F((float)(mouseX - bounds.read()) / Math.max(1, bounds.update())) * 255.0F);
      int r = this.prev;
      int g = this.valid;
      int b = this.size;
      if (channel == 0) {
         r = value;
      } else if (channel == 1) {
         g = value;
      } else {
         b = value;
      }

      this.load_V_I(r << 16 | g << 8 | b);
   }

   private void load_V_I(int rgb) {
      float[] hsb = Color.RGBtoHSB(rgb >>> 16 & 0xFF, rgb >>> 8 & 0xFF, rgb & 0xFF, null);
      this.init_F = hsb[0];
      this.load_F = hsb[1];
      this.read_F = hsb[2];
      this.read(rgb);
   }

   private void sync_V() {
      this.read(Color.HSBtoRGB(this.init_F, this.load_F, this.read_F) & 16777215);
   }

   private void read(int rgb) {
      this.stop = rgb & 16777215;
      this.prev = this.stop >>> 16 & 0xFF;
      this.valid = this.stop >>> 8 & 0xFF;
      this.size = this.stop & 0xFF;
      this.next = (this.clear & 0xFF) << 24 | this.stop;
      this.flush_V();
   }

   private void flush_V() {
      this.init_Str = String.format(Locale.ROOT, "%06X", this.stop);
      this.reset = this.init_Str.length();
      this.read_V();
   }

   private int read_I() {
      return this.next;
   }

   private int write_I() {
      return this.prev;
   }

   private int update_I() {
      return this.valid;
   }

   private int sync_I() {
      return this.size;
   }

   private autismclient.gui.f.NetHelper init_NetHelper() {
      return autismclient.gui.f.NetHelper.init(this.init_NetHelper.read(), this.init_NetHelper.write(), this.init_NetHelper.update(), 14);
   }

   private autismclient.gui.f.NetHelper load_NetHelper() {
      return autismclient.gui.f.NetHelper.init(this.init_NetHelper.read() + 6, this.init_NetHelper.write() + 19, 102, 82);
   }

   private autismclient.gui.f.NetHelper read_NetHelper() {
      return autismclient.gui.f.NetHelper.init(this.init_NetHelper.read() + 6 + 102 + 6, this.init_NetHelper.write() + 19, 13, 82);
   }

   private autismclient.gui.f.NetHelper init_NetHelper_I(int index) {
      return autismclient.gui.f.NetHelper.init(this.init_NetHelper.read() + 21, this.init_NetHelper.write() + 107 + index * 15, 168, 13);
   }

   private autismclient.gui.f.NetHelper write_NetHelper() {
      return this.init_NetHelper_I(0);
   }

   private autismclient.gui.f.NetHelper update_NetHelper() {
      return this.init_NetHelper_I(1);
   }

   private autismclient.gui.f.NetHelper sync_NetHelper() {
      return this.init_NetHelper_I(2);
   }

   private autismclient.gui.f.NetHelper flush_NetHelper() {
      return autismclient.gui.f.NetHelper.init(this.init_NetHelper.read() + 135 + 12, this.init_NetHelper.write() + 19 + 36, 70, 13);
   }

   private autismclient.gui.f.NetHelper load_NetHelper_I(int index) {
      int inner = this.init_NetHelper.update() - 12;
      int w = (inner - 4) / 3;
      int x = this.init_NetHelper.read() + 6 + index * (w + 2);
      int width = index == 2 ? this.init_NetHelper.read() + 6 + inner - x : w;
      return autismclient.gui.f.NetHelper.init(x, this.init_NetHelper.load() - 4 - 13, width, 13);
   }

   private autismclient.gui.f.NetHelper close() {
      return this.load_NetHelper_I(0);
   }

   private autismclient.gui.f.NetHelper open() {
      return this.load_NetHelper_I(1);
   }

   private autismclient.gui.f.NetHelper poll() {
      return this.load_NetHelper_I(2);
   }

   private static boolean load_Z_C(char chr) {
      return chr >= '0' && chr <= '9' || chr >= 'a' && chr <= 'f' || chr >= 'A' && chr <= 'F';
   }

   private static float init_F_F(float value) {
      return Math.max(0.0F, Math.min(1.0F, value));
   }

   private static NetHelper.Inner_StreamUtil init_NetHelper_StreamUt() {
      if (init_NetHelper_StreamUt == null) {
         init_NetHelper_StreamUt = NetHelper.Inner_StreamUtil.init("color_picker_hue", 13, 82);
         init_NetHelper_StreamUt.init();
      }

      return init_NetHelper_StreamUt;
   }

   private static NetHelper.Inner_StreamUtil init_NetHelper_StreamUt_F(float hue) {
      int bucket = Math.round(init_F_F(hue) * 360.0F);
      if (load_NetHelper_StreamUt == null) {
         load_NetHelper_StreamUt = NetHelper.Inner_StreamUtil.init("color_picker_sv", 102, 82);
         set = -2147483648;
      }

      if (set != bucket) {
         load_NetHelper_StreamUt.init(bucket / 360.0F);
         set = bucket;
      }

      return load_NetHelper_StreamUt;
   }

   private enum Inner_DataUtil {
      init,
      load,
      read,
      write,
      update,
      sync,
      flush;
   }

   private static final class Inner_StreamUtil extends AbstractTexture {
      private final Identifier init_Identifier;
      private final NativeImage init_NativeImage;
      private final int init_I;
      private final int load;

      private static NetHelper.Inner_StreamUtil init(String name, int width, int height) {
         Identifier id = Identifier.fromNamespaceAndPath("autismclient", "dynamic/ui/" + name);
         NetHelper.Inner_StreamUtil texture = new NetHelper.Inner_StreamUtil(id, name, width, height);
         NetHelper.init_Minecraft.getTextureManager().register(id, texture);
         return texture;
      }

      private Inner_StreamUtil(Identifier id, String name, int width, int height) {
         this.init_Identifier = id;
         this.init_I = width;
         this.load = height;
         this.init_NativeImage = new NativeImage(width, height, true);
         this.texture = RenderSystem.getDevice().createTexture("AUTISM " + name, 5, GpuFormat.RGBA8_UNORM, width, height, 1, 1);
         this.sampler = RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST);
         this.textureView = RenderSystem.getDevice().createTextureView(this.texture);
      }

      private void init() {
         for (int y = 0; y < this.load; y++) {
            float h = this.load <= 1 ? 0.0F : 1.0F - (float)y / (this.load - 1);
            int color = 0xFF000000 | Color.HSBtoRGB(h, 1.0F, 1.0F) & 16777215;

            for (int x = 0; x < this.init_I; x++) {
               this.init_NativeImage.setPixel(x, y, color);
            }
         }

         this.load();
      }

      private void init(float hue) {
         for (int x = 0; x < this.init_I; x++) {
            float s = this.init_I <= 1 ? 0.0F : (float)x / (this.init_I - 1);

            for (int y = 0; y < this.load; y++) {
               float v = this.load <= 1 ? 0.0F : 1.0F - (float)y / (this.load - 1);
               this.init_NativeImage.setPixel(x, y, 0xFF000000 | Color.HSBtoRGB(hue, s, v) & 16777215);
            }
         }

         this.load();
      }

      private void load() {
         RenderSystem.getDevice().createCommandEncoder().writeToTexture(this.texture, this.init_NativeImage);
      }

      private void init(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper area) {
         context.init_GuiGraphicsExtract()
            .blit(
               RenderPipelines.GUI_TEXTURED,
               this.init_Identifier,
               area.read(),
               area.write(),
               0.0F,
               0.0F,
               area.update(),
               area.sync(),
               this.init_I,
               this.load,
               this.init_I,
               this.load,
               -1
            );
      }

      @Override
      public void close() {
         this.init_NativeImage.close();
         super.close();
      }
   }
}
