package autismclient.gui.d;

import autismclient.compat263.Compat263;
import autismclient.gui.f.b.j;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import autismclient.modules.Module;
import autismclient.util.al;
import autismclient.util.cA;
import autismclient.util.ct;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class AutismHudEditorScreen extends Screen {
   private static final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = -791321;
   private static final int load_I = -4743522;
   private static final int read_I = -10682470;
   private static final int write = -50373;
   private static final String[] init_StrArr = new String[]{"World", "Player", "Client"};
   private static final String[][] init_StrArr2 = new String[][]{
      {"coordinates", "nether_coords", "compass", "looking_at", "biome", "weather", "world_time", "real_time", "server", "server_ip", "server_brand"},
      {"speed", "rotation", "game_mode", "armor", "inventory", "durability", "item_counter", "potion_timers", "breaking_progress"},
      {"active_modules", "watermark", "fps", "ping", "tps", "cps", "keystrokes", "anti_vanish", "memory", "fps_graph", "bed_danger", "gif"}
   };
   private static final int update = 10;
   private static final int sync = 6;
   private static final int flush = 0;
   private final Screen init_Screen;
   private String init_Str;
   private String load_Str;
   private int close;
   private int open;
   private boolean init_Z;
   private boolean load_Z;
   private int poll;
   private int push;
   private Integer init_Integer;
   private Integer load_Integer;
   private int pop = -1;
   private int tick;
   private int run;
   private int get;
   private int set;
   private int put;
   private int add;
   private int[] init_IArr;
   private boolean read_Z;
   private int clear;
   private int reset;
   private int start;
   private int stop;
   private int next;

   private static int init_I() {
      return ct.init_I_I_ct_DataUtil(-4743522, ct.Inner_DataUtil.TEXT);
   }

   private static int load() {
      return ct.init_I_I_ct_DataUtil(-50373, ct.Inner_DataUtil.ACCENT);
   }

   public AutismHudEditorScreen(Screen parent) {
      super(Component.literal("Autism HUD Editor"));
      this.init_Screen = parent;
      al.init_V();
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      int mx = cA.init_I_D(mouseX);
      int my = cA.init_I_D(mouseY);
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         int sw = cA.init_I();
         int sh = cA.load_I();
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, sw, sh), 1426063360);
         if (this.init_Z()) {
            this.init(graphics, sw, sh);
         }

         this.read(graphics, sw, sh);
         al.init_V_GuiGraphicsExtract_4(graphics, this.font, true, this.init_Str, mx, my);
         if ("gif".equals(this.init_Str) && al.init_AutismConfig_HudEl_Str(this.init_Str).enabled) {
            this.init(graphics);
         }

         this.init(graphics, sw, sh, mx, my);
         if (this.load_Z) {
            this.load(graphics, mx, my);
         }
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void init(GuiGraphicsExtractor graphics, int sw, int sh, int mx, int my) {
      String help = "Drag elements, right click edits/adds, Delete disables, arrows nudge.";
      int titleW = k.init(this.font, "Autism HUD Editor", init_CharEncoder.init_Identifier_l(l.read), -791321) + 8;
      int helpW = k.init(this.font, help, init_CharEncoder.init_Identifier_l(l.read), init_I()) + 8;
      int w = Math.min(sw - 12, Math.min(430, Math.max(titleW, helpW)));
      int headerH = autismclient.gui.f.RenderState.init(14);
      int lineH = autismclient.gui.f.RenderState.init(12);
      int h = headerH + lineH + 2;
      int[] pos = this.init_IArr(sw, sh, w, h);
      if (pos != null) {
         int x = pos[0];
         int y = pos[1];
         autismclient.gui.f.b.FieldCache.init(
            autismclient.gui.f.UiHelper.init(graphics, this.font, mx, my),
            autismclient.gui.f.NetHelper.init(x, y, w, h),
            "Autism HUD Editor",
            false,
            false,
            false,
            this.init(mx, my, x, y, w, headerH),
            true,
            4,
            4,
            headerH
         );
         this.init(graphics, help, x + 4, this.load(y + headerH, lineH), init_I(), w - 8);
      }
   }

   private int[] init_IArr(int sw, int sh, int w, int h) {
      int rev = al.load_I();
      if (this.read_Z && sw == this.clear && sh == this.reset && w == this.start && h == this.stop && rev == this.next) {
         return this.init_IArr;
      }

      int pad = 6;
      int[][] candidates = new int[][]{
         {6, 6}, {Math.max(pad, sw - w - pad), pad}, {pad, Math.max(pad, sh - h - pad)}, {Math.max(pad, sw - w - pad), Math.max(pad, sh - h - pad)}
      };
      int[] pos = null;

      for (int[] candidate : candidates) {
         if (!this.init_Z(candidate[0], candidate[1], w, h)) {
            pos = candidate;
            break;
         }
      }

      this.init_IArr = pos;
      this.read_Z = true;
      this.clear = sw;
      this.reset = sh;
      this.start = w;
      this.stop = h;
      this.next = rev;
      return pos;
   }

   private boolean init_Z(int x, int y, int w, int h) {
      for (String id : al.init_List()) {
         if (al.init_AutismConfig_HudEl_Str(id).enabled) {
            al.Inner_BufferHelper bounds = al.init_al_BufferHelper_Str_Font(id, this.font);
            if (this.init(x, y, w, h, bounds.read(), bounds.write(), bounds.update(), bounds.sync())) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean init(int ax, int ay, int aw, int ah, int bx, int by, int bw, int bh) {
      return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
   }

   private void init(GuiGraphicsExtractor graphics, int sw, int sh) {
      int step = this.read();

      for (int x = 0; x < sw; x += step) {
         boolean major = x / step % 5 == 0;
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x, 0, 1, sh), major ? 605098260 : 336662804);
      }

      for (int y = 0; y < sh; y += step) {
         boolean major = y / step % 5 == 0;
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, y, sw, 1), major ? 605098260 : 336662804);
      }

      autismclient.gui.f.PacketUtil.init(
         graphics, autismclient.gui.f.NetHelper.init(sw / 2, 0, 1, sh), ct.init_I_I_ct_DataUtil(1157577531, ct.Inner_DataUtil.ACCENT)
      );
      autismclient.gui.f.PacketUtil.init(
         graphics, autismclient.gui.f.NetHelper.init(0, sh / 2, sw, 1), ct.init_I_I_ct_DataUtil(1157577531, ct.Inner_DataUtil.ACCENT)
      );
   }

   private AutismHudEditorScreen.Inner_DataUtil init_RenderState_DataUt() {
      int pad = 4;
      int gap = 4;
      int titleH = autismclient.gui.f.RenderState.init(14);
      int catH = autismclient.gui.f.RenderState.init(12) + 2;
      int rowH = autismclient.gui.f.RenderState.init(12) + 2;
      int longest = 0;

      for (String[] cat : init_StrArr2) {
         for (String id : cat) {
            longest = Math.max(longest, k.init(this.font, al.init_Str_Str(id), init_CharEncoder.init_Identifier_l(l.read), -791321));
         }
      }

      int sw = cA.init_I();
      int sh = cA.load_I();
      int cols = init_StrArr2.length;
      int maxCol = (sw - 8 - pad * 2 - (cols - 1) * gap) / cols;
      int colW = Math.max(60, Math.min(longest + 12, Math.max(60, maxCol)));
      int w = pad * 2 + cols * colW + (cols - 1) * gap;
      int maxRows = 0;

      for (String[] cat : init_StrArr2) {
         maxRows = Math.max(maxRows, cat.length);
      }

      int h = titleH + catH + maxRows * rowH + pad;
      int x = this.init_I(this.poll, 4, Math.max(4, sw - w - 4));
      int y = this.init_I(this.push, 4, Math.max(4, sh - h - 4));
      return new AutismHudEditorScreen.Inner_DataUtil(x, y, w, h, colW, titleH, catH, rowH, gap, pad);
   }

   private void load(GuiGraphicsExtractor graphics, int mx, int my) {
      AutismHudEditorScreen.Inner_DataUtil p = this.init_RenderState_DataUt();
      autismclient.gui.f.b.FieldCache.init(
         autismclient.gui.f.UiHelper.init(graphics, this.font, mx, my),
         autismclient.gui.f.NetHelper.init(p.init, p.load, p.read, p.write),
         "Add Element",
         false,
         false,
         false,
         this.init(mx, my, p.init, p.load, p.read, p.sync),
         true,
         4,
         4,
         p.sync
      );
      int underline = ct.init_I_I_ct_DataUtil(1728002875, ct.Inner_DataUtil.ACCENT);

      for (int col = 0; col < init_StrArr2.length; col++) {
         int colX = p.init + p.poll + col * (p.update + p.open);
         int headerY = p.load + p.sync;
         this.init(graphics, init_StrArr[col], colX + 2, this.load(headerY, p.flush - 2), load(), p.update - 4);
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(colX, headerY + p.flush - 2, p.update, 1), underline);
         int cy = headerY + p.flush;

         for (String id : init_StrArr2[col]) {
            boolean over = this.init(mx, my, colX, cy, p.update, p.close - 2);
            boolean enabled = al.init_AutismConfig_HudEl_Str(id).enabled;
            autismclient.gui.f.b.TableIndex.init(graphics, colX, cy, p.update, p.close - 2, over ? 1714756383 : 856888344);
            if (enabled) {
               autismclient.gui.f.b.TableIndex.read(graphics, colX + 1, cy + 1, 3, p.close - 4, -10682470);
            }

            this.init(graphics, al.init_Str_Str(id), colX + 7, this.load(cy, p.close - 2), enabled ? -791321 : init_I(), p.update - 9);
            cy += p.close;
         }
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      if (this.load_Z) {
         String picked = this.init_Str(mx, my);
         if (picked != null) {
            al.init_V_Str_Z(picked, true);
            int[] snapped = this.init(picked, mx, my);
            al.init_V_Str(picked, snapped[0], snapped[1], cA.init_I(), cA.load_I());
            this.init_Str = picked;
            this.load_Z = false;
            return true;
         } else {
            this.load_Z = false;
            return true;
         }
      } else {
         String hit = al.init_Str_Font(this.font, mx, my);
         if (Compat263.mouseEventGlfwButton(event) == 1) {
            if (hit != null) {
               this.minecraft.gui.setScreen(new HudElementSettingsScreen(this, hit));
            } else {
               this.poll = mx;
               this.push = my;
               this.load_Z = true;
            }

            return true;
         } else {
            if (Compat263.mouseEventGlfwButton(event) == 0 && "gif".equals(this.init_Str) && al.init_AutismConfig_HudEl_Str(this.init_Str).enabled) {
               int corner = this.init_I(mx, my);
               if (corner >= 0) {
                  this.init_V(corner, mx, my);
                  return true;
               }
            }

            if (Compat263.mouseEventGlfwButton(event) == 0 && hit != null) {
               this.init_Str = hit;
               this.load_Str = hit;
               al.Inner_BufferHelper bounds = al.init_al_BufferHelper_Str_Font(hit, this.font);
               this.close = mx - bounds.read();
               this.open = my - bounds.write();
               this.init_Z = false;
               return true;
            } else {
               this.init_Str = null;
               return true;
            }
         }
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      this.load_Str = null;
      this.pop = -1;
      this.init_Integer = null;
      this.load_Integer = null;
      return true;
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      if (this.pop >= 0) {
         this.init(mx, my, event.hasShiftDown());
         return true;
      }

      if (this.load_Str == null) {
         return true;
      }

      int[] snapped = this.init(this.load_Str, mx - this.close, my - this.open);
      al.init_V_Str(this.load_Str, snapped[0], snapped[1], cA.init_I(), cA.load_I());
      this.init_Z = true;
      return true;
   }

   @Override
   public boolean keyPressed(KeyEvent input) {
      if (Compat263.keyEventGlfwKey(input) == 256) {
         this.minecraft.gui.setScreen(this.init_Screen);
         return true;
      }

      if (this.init_Str != null && Compat263.keyEventGlfwKey(input) == 261) {
         al.init_V_Str_Z(this.init_Str, false);
         return true;
      }

      if (this.init_Str != null) {
         int step = (Compat263.keyEventGlfwModifiers(input) & 2) != 0 ? 10 : 1;
         al.Inner_BufferHelper bounds = al.init_al_BufferHelper_Str_Font(this.init_Str, this.font);
         int x = bounds.read();
         int y = bounds.write();
         boolean moved = true;
         switch (Compat263.keyEventGlfwKey(input)) {
            case 262:
               x += step;
               break;
            case 263:
               x -= step;
               break;
            case 264:
               y += step;
               break;
            case 265:
               y -= step;
               break;
            default:
               moved = false;
         }

         if (moved) {
            int[] snapped = this.init(this.init_Str, x, y);
            al.init_V_Str(this.init_Str, snapped[0], snapped[1], cA.init_I(), cA.load_I());
            return true;
         }
      }

      return true;
   }

   private int[] init(String id, int x, int y) {
      al.Inner_BufferHelper bounds = al.init_al_BufferHelper_Str_Font(id, this.font);
      int sw = cA.init_I();
      int sh = cA.load_I();
      int outX = x;
      int outY = y;
      int maxX = Math.max(0, sw - bounds.update());
      int maxY = Math.max(0, sh - bounds.sync());
      this.init_Integer = null;
      this.load_Integer = null;
      AutismHudEditorScreen.Inner_StreamUtil snappedX = this.init(id, x, bounds.update(), sw, true);
      AutismHudEditorScreen.Inner_StreamUtil snappedY = this.init(id, y, bounds.sync(), sh, false);
      if (snappedX.init_Z()) {
         outX = snappedX.init_I();
         this.init_Integer = snappedX.load();
      }

      if (snappedY.init_Z()) {
         outY = snappedY.init_I();
         this.load_Integer = snappedY.load();
      }

      outX = this.init_I(outX, 0, maxX);
      outY = this.init_I(outY, 0, maxY);
      return new int[]{outX, outY};
   }

   private AutismHudEditorScreen.Inner_StreamUtil init(String id, int value, int size, int screenSize, boolean horizontal) {
      int max = Math.max(0, screenSize - size);
      AutismHudEditorScreen.Inner_StreamUtil best = AutismHudEditorScreen.Inner_StreamUtil.init_RenderState_Stream();
      best = this.init(best, value, 0, 0);
      best = this.init(best, value, max, screenSize);
      best = this.init(best, value, (screenSize - size) / 2, screenSize / 2);

      for (String other : al.init_List()) {
         if (!other.equals(id) && al.init_AutismConfig_HudEl_Str(other).enabled) {
            al.Inner_BufferHelper ob = al.init_al_BufferHelper_Str_Font(other, this.font);
            int otherStart = horizontal ? ob.read() : ob.write();
            int otherSize = horizontal ? ob.update() : ob.sync();
            int otherEnd = otherStart + otherSize;
            int otherCenter = otherStart + otherSize / 2;
            best = this.init(best, value, otherStart, otherStart);
            best = this.init(best, value, otherEnd - size, otherEnd);
            best = this.init(best, value, otherCenter - size / 2, otherCenter);
            best = this.init(best, value, otherStart - size - 0, otherStart);
            best = this.init(best, value, otherEnd + 0, otherEnd);
         }
      }

      return best;
   }

   private AutismHudEditorScreen.Inner_StreamUtil init(AutismHudEditorScreen.Inner_StreamUtil current, int value, int candidatePosition, int guide) {
      int distance = Math.abs(value - candidatePosition);
      if (distance > 6) {
         return current;
      } else {
         return current.init_Z() && distance >= current.read() ? current : new AutismHudEditorScreen.Inner_StreamUtil(candidatePosition, guide, distance, true);
      }
   }

   private int read() {
      return 10;
   }

   private String init_Str(int mx, int my) {
      AutismHudEditorScreen.Inner_DataUtil p = this.init_RenderState_DataUt();

      for (int col = 0; col < init_StrArr2.length; col++) {
         int colX = p.init + p.poll + col * (p.update + p.open);
         int cy = p.load + p.sync + p.flush;

         for (String id : init_StrArr2[col]) {
            if (this.init(mx, my, colX, cy, p.update, p.close - 2)) {
               return id;
            }

            cy += p.close;
         }
      }

      return null;
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      return true;
   }

   private boolean init_Z() {
      Module hud = autismclient.modules.al.init_Module_Str("hud");
      return hud == null || Boolean.parseBoolean(hud.value("editor-grid"));
   }

   private int init_I(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   private void read(GuiGraphicsExtractor graphics, int sw, int sh) {
      if (this.init_Integer != null) {
         autismclient.gui.f.PacketUtil.init(
            graphics, autismclient.gui.f.NetHelper.init(this.init_Integer, 0, 1, sh), ct.init_I_I_ct_DataUtil(-1426113733, ct.Inner_DataUtil.ACCENT)
         );
      }

      if (this.load_Integer != null) {
         autismclient.gui.f.PacketUtil.init(
            graphics, autismclient.gui.f.NetHelper.init(0, this.load_Integer, sw, 1), ct.init_I_I_ct_DataUtil(-1426113733, ct.Inner_DataUtil.ACCENT)
         );
      }
   }

   private void init(GuiGraphicsExtractor graphics) {
      for (int[] handle : this.init_IArr2()) {
         autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract(graphics, handle[0], handle[1], 3.0F, load());
         autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract(graphics, handle[0], handle[1], 2.0F, -791321);
      }
   }

   private int[][] init_IArr2() {
      al.Inner_BufferHelper b = al.init_al_BufferHelper_Str_Font("gif", this.font);
      return new int[][]{
         {b.read(), b.write()}, {b.read() + b.update(), b.write()}, {b.read() + b.update(), b.write() + b.sync()}, {b.read(), b.write() + b.sync()}
      };
   }

   private int init_I(int mx, int my) {
      int[][] handles = this.init_IArr2();

      for (int i = 0; i < handles.length; i++) {
         if (Math.abs(mx - handles[i][0]) <= 4 && Math.abs(my - handles[i][1]) <= 4) {
            return i;
         }
      }

      return -1;
   }

   private void init_V(int corner, int mx, int my) {
      String id = "gif";
      this.pop = corner;
      al.Inner_BufferHelper b = al.init_al_BufferHelper_Str_Font(id, this.font);
      int pad = this.init_I(al.init_I_Str_2(id, "padding", 1), 0, 16);
      this.put = b.read() + pad;
      this.add = b.write() + pad;
      this.tick = this.init_I(al.init_I_Str_2(id, "gif-width", 120), 16, 512);
      this.run = this.init_I(al.init_I_Str_2(id, "gif-height", 120), 16, 512);
      this.get = mx;
      this.set = my;
   }

   private void init(int mx, int my, boolean shiftDown) {
      String id = "gif";
      if (!id.equals(this.init_Str)) {
         this.pop = -1;
      } else {
         int signX = this.pop != 0 && this.pop != 3 ? 1 : -1;
         int signY = this.pop != 0 && this.pop != 1 ? 1 : -1;
         int w = this.tick + (mx - this.get) * signX;
         int h = this.run + (my - this.set) * signY;
         boolean keepRatio = al.init_Z_Str_Str(id, "gif-keep-aspect") != shiftDown;
         if (keepRatio) {
            double scale = Math.max((double)w / this.tick, (double)h / this.run);
            w = Math.max(1, (int)Math.round(this.tick * scale));
            h = Math.max(1, (int)Math.round(this.run * scale));
         }

         w = this.init_I(w, 16, 512);
         h = this.init_I(h, 16, 512);
         int pad = this.init_I(al.init_I_Str_2(id, "padding", 1), 0, 16);
         int anchorX = signX < 0 ? this.put + this.tick : this.put;
         int anchorY = signY < 0 ? this.add + this.run : this.add;
         int sw = cA.init_I();
         int sh = cA.load_I();
         int maxW = Math.max(16, signX < 0 ? anchorX - pad : sw - anchorX - pad);
         int maxH = Math.max(16, signY < 0 ? anchorY - pad : sh - anchorY - pad);
         w = Math.min(w, maxW);
         h = Math.min(h, maxH);
         if (keepRatio) {
            int hFromW = Math.max(16, (int)Math.round((double)(w * this.run) / this.tick));
            if (hFromW <= maxH) {
               h = hFromW;
            } else {
               w = this.init_I((int)Math.round((double)(h * this.tick) / this.run), 16, maxW);
            }
         }

         al.init_V_Str(id, "gif-width", Integer.toString(w));
         al.init_V_Str(id, "gif-height", Integer.toString(h));
         al.init_V_Str(id, "gif-natural", "false");
         int contentX = signX < 0 ? anchorX - w : anchorX;
         int contentY = signY < 0 ? anchorY - h : anchorY;
         al.init_V_Str(id, contentX - pad, contentY - pad, sw, sh);
      }
   }

   private int load(int boxY, int boxH) {
      return j.init(boxY, boxH, init_CharEncoder.init_I_l(l.read), init_CharEncoder.flush());
   }

   private boolean init(int mx, int my, int x, int y, int w, int h) {
      return mx >= x && mx < x + w && my >= y && my < y + h;
   }

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int color, int maxW) {
      String trimmed = k.init(this.font, text, maxW, init_CharEncoder.init_Identifier_l(l.read), color);
      k.init(graphics, this.font, trimmed, init_CharEncoder.init_Identifier_l(l.read), color, x, y, false);
   }

   private record Inner_DataUtil(int init, int load, int read, int write, int update, int sync, int flush, int close, int open, int poll) {
   }

   private record Inner_StreamUtil(int init_I, int load, int read, boolean init_Z) {
      static AutismHudEditorScreen.Inner_StreamUtil init_RenderState_Stream() {
         return new AutismHudEditorScreen.Inner_StreamUtil(0, 0, 2147483647, false);
      }
   }
}
