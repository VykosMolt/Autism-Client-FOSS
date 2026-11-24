package autismclient.gui.d;

import autismclient.compat263.Compat263;
import autismclient.gui.f.b.j;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import autismclient.util.cA;
import autismclient.util.ct;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class IoWorker extends KeyHolder {
   private static final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = 6;
   private static final int load_I = 4;
   private static final int read = 3;
   private final Screen init_Screen;
   private final String init_Str;
   private final LinkedHashSet<String> init_LinkedHashSet = new LinkedHashSet<>();
   private final List<int[]> init_List = new ArrayList<>();
   private int write;
   private int update = 3;
   private int sync;
   private String load_Str = "";
   private int flush = ValueSink.close;

   public IoWorker(Screen parent, String macroName, Set<String> preselect) {
      super(Component.literal("Assign Macro"));
      this.init_Screen = parent;
      this.init_Str = macroName == null ? "" : macroName;
      if (preselect != null) {
         this.init_LinkedHashSet.addAll(preselect);
      }
   }

   private List<IoWorker.Inner_DataUtil> init_List_6() {
      List<IoWorker.Inner_DataUtil> out = new ArrayList<>();

      for (autismclient.util.g.ClassCache.Inner_FieldCache s : autismclient.util.g.PacketUtil.init_PacketUtil().load_List()) {
         String m = autismclient.util.g.PacketUtil.init_PacketUtil().write_Str_Str(s.init_Str());
         out.add(new IoWorker.Inner_DataUtil(s.init_Str(), s.load_Str(), m == null ? "" : m));
      }

      return out;
   }

   @Override
   public void tick() {
      super.tick();
      if (this.read() != this.update) {
         this.init();
      }
   }

   @Override
   protected void init() {
      this.clearWidgets();
      this.update = this.read();
      int w = this.push();
      int x = (this.init_I() - w) / 2;
      int gap = 2;
      int toolY = this.flush();
      int allW = this.init_I_Str_10("Select All");
      int noneW = this.init_I_Str_10("Select None");
      int onlyW = this.init_I_Str_10("Only Without Macro");
      if (allW + noneW + onlyW + gap * 2 > w) {
         int third = Math.max(1, (w - gap * 2) / 3);
         allW = third;
         noneW = third;
         onlyW = Math.max(1, w - (third + gap) * 2);
      }

      this.init(x, toolY, allW, "Select All", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, b -> {
         this.init_LinkedHashSet.clear();

         for (IoWorker.Inner_DataUtil a : this.init_List_6()) {
            this.init_LinkedHashSet.add(a.init());
         }
      });
      this.init(x + allW + gap, toolY, noneW, "Select None", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, b -> this.init_LinkedHashSet.clear());
      this.init(x + allW + noneW + gap * 2, toolY, onlyW, "Only Without Macro", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, b -> {
         this.init_LinkedHashSet.clear();

         for (IoWorker.Inner_DataUtil a : this.init_List_6()) {
            if (a.read().isBlank()) {
               this.init_LinkedHashSet.add(a.init());
            }
         }
      });
      int assignW = this.init_I_Str_10("Assign to Selected");
      int backW = this.init_I_Str_10("Back");
      if (assignW + backW + gap > w) {
         int half = Math.max(1, (w - gap) / 2);
         assignW = half;
         backW = Math.max(1, w - half - gap);
      }

      int footerY = this.poll();
      int assignX = x + w - backW - gap - assignW;
      this.init(assignX, footerY, assignW, "Assign to Selected", autismclient.gui.f.b.CachePool.Inner_DataUtil.write, b -> this.load_V_12());
      this.init(x + w - backW, footerY, backW, "Back", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, b -> this.onClose());
      this.sync = assignX - 4;
   }

   private int read() {
      int fit = Math.max(1, (this.load_I() - 6 - 4 - write() - 4 - this.close() + 2) / sync());
      return Math.max(Math.min(3, fit), Math.min(this.init_List_6().size(), fit));
   }

   private static int write() {
      return autismclient.gui.f.RenderState.init(13);
   }

   private static int update() {
      return autismclient.gui.f.RenderState.init(14);
   }

   private static int sync() {
      return update() + 2;
   }

   private int flush() {
      return 10 + autismclient.gui.f.RenderState.init(14) + 2;
   }

   private int close() {
      return this.flush() + write() + 4;
   }

   private int open() {
      return this.close() + this.update * sync() - 2;
   }

   private int poll() {
      return this.open() + 4;
   }

   private int init_I_Str_10(String text) {
      return autismclient.gui.f.RenderState.load(k.init(this.font, text, init_CharEncoder.init_Identifier_l(l.read), 0));
   }

   private int init_I_4(int y, int h) {
      return j.init(y, h, k.init(init_CharEncoder.init_Identifier_l(l.read)), 0);
   }

   private void load_V_12() {
      if (this.init_LinkedHashSet.isEmpty()) {
         this.load("Tick at least one account.", ValueSink.poll);
      } else {
         int changed = autismclient.util.g.PacketUtil.init_PacketUtil().init_I_Set_Str(new LinkedHashSet<>(this.init_LinkedHashSet), this.init_Str);
         this.load("Assigned \"" + this.init_Str + "\" to " + changed + " account(s).", ValueSink.open);
      }
   }

   private void load(String text, int color) {
      this.load_Str = text == null ? "" : text;
      this.flush = color;
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
      MouseButtonEvent virtualEvent = init_MouseButtonEvent_MouseButtonEvent(event);
      if (Compat263.mouseEventGlfwButton(virtualEvent) == 0) {
         List<IoWorker.Inner_DataUtil> list = this.init_List_6();

         for (int[] rect : this.init_List) {
            if (virtualEvent.x() >= rect[0]
               && virtualEvent.x() < rect[0] + rect[2]
               && virtualEvent.y() >= rect[1]
               && virtualEvent.y() < rect[1] + rect[3]
               && rect[4] < list.size()) {
               String id = list.get(rect[4]).init();
               if (!this.init_LinkedHashSet.remove(id)) {
                  this.init_LinkedHashSet.add(id);
               }

               return true;
            }
         }
      }

      return super.mouseClicked(virtualEvent, doubled);
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
      this.write = Math.max(0, this.write + (vertical < 0.0 ? 1 : -1));
      return true;
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      int virtualMouseX = cA.init_I_D(mouseX);
      int virtualMouseY = cA.init_I_D(mouseY);
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         autismclient.gui.f.PacketUtil.init(
            graphics, autismclient.gui.f.NetHelper.init(0, 0, this.init_I(), this.load_I()), ct.init_I_I_ct_DataUtil(-15987181, ct.Inner_DataUtil.update)
         );
         int w = this.push();
         int x = (this.init_I() - w) / 2;
         int border = ct.init_I_I_ct_DataUtil(ValueSink.update, ct.Inner_DataUtil.load);
         int textColor = ct.init_I_I_ct_DataUtil(ValueSink.flush, ct.Inner_DataUtil.read);
         int muted = ct.init_I_I_ct_DataUtil(ValueSink.close, ct.Inner_DataUtil.read);
         int panelTop = 6;
         int panelH = Math.max(1, this.poll() + write() + 4 - panelTop);
         autismclient.gui.f.PacketUtil.load(
            graphics,
            autismclient.gui.f.NetHelper.init(x - 4, panelTop, w + 8, panelH),
            6,
            ct.init_I_I_ct_DataUtil(-401402080, ct.Inner_DataUtil.close),
            border
         );
         Identifier fontId = init_CharEncoder.init_Identifier_l(l.read);
         String title = "Assign \"" + autismclient.util.g.PacketUtil.update(this.init_Str, 40) + "\" to accounts:";
         k.load(graphics, this.font, title, fontId, textColor, x, this.init_I_4(panelTop + 4, autismclient.gui.f.RenderState.init(14)), w, false);
         this.init_List.clear();
         List<IoWorker.Inner_DataUtil> list = this.init_List_6();
         int top = this.close();
         int rowH = update();
         int visible = Math.max(1, this.update);
         this.write = Math.max(0, Math.min(this.write, Math.max(0, list.size() - visible)));
         int success = ct.init_I_I_ct_DataUtil(ValueSink.open, ct.Inner_DataUtil.sync);
         int y = top;

         for (int i = this.write; i < Math.min(list.size(), this.write + visible); i++) {
            IoWorker.Inner_DataUtil a = list.get(i);
            boolean sel = this.init_LinkedHashSet.contains(a.init());
            autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x, y, w, rowH), sel ? success & 16777215 | 855638016 : 402653184);
            if (sel) {
               autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x, y, 2, rowH), success);
            }

            int textY = this.init_I_4(y, rowH);
            String cur = a.read().isBlank() ? "no macro" : "-> " + a.read();
            int curMax = Math.max(1, w / 2 - 5);
            String curShown = k.load(this.font, cur, curMax, fontId, 0);
            int curW = k.init(this.font, curShown, fontId, 0);
            int curX = x + w - 5 - curW;
            k.init(graphics, this.font, curShown, fontId, a.read().isBlank() ? muted : textColor, curX, textY, false);
            String box = sel ? "[x] " : "[ ] ";
            int nameX = x + 5;
            k.load(
               graphics,
               this.font,
               box + autismclient.util.g.PacketUtil.update(a.load(), 40),
               fontId,
               sel ? success : textColor,
               nameX,
               textY,
               Math.max(1, curX - 4 - nameX),
               false
            );
            this.init_List.add(new int[]{x, y, w, rowH, i});
            y += sync();
         }

         if (!this.load_Str.isBlank()) {
            k.load(graphics, this.font, this.load_Str, fontId, this.flush, x, this.init_I_4(this.poll(), write()), Math.max(1, this.sync - x), false);
         }

         super.extractRenderState(graphics, virtualMouseX, virtualMouseY, delta);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private int push() {
      return Math.max(200, Math.min(360, this.init_I() - 20));
   }

   private void init(int x, int y, int w, String label, autismclient.gui.f.b.CachePool.Inner_DataUtil tone, OnPress press) {
      this.addRenderableWidget(new StreamGate(x, y, Math.max(1, w), write(), Component.literal(label), tone, press));
   }

   @Override
   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.init_Screen);
      }
   }

   private record Inner_DataUtil(String init, String load, String read) {
   }
}
