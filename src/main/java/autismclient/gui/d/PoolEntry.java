package autismclient.gui.d;

import autismclient.gui.f.b.j;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import autismclient.util.cA;
import autismclient.util.ct;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class PoolEntry extends KeyHolder {
   private static final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = -15594737;
   private static final int load_I = 474;
   private static final int read_I = 6;
   private static final int write_I = 4;
   private static final int update_I = 4;
   private static final int sync = 4;
   private static final int flush = 10;
   private static final String init_Str = "{bot} = bot name. {name}/{me} = your on-server name. Bots watch chat for these.";
   private static final String[] init_StrArr = new String[]{
      "Accept", "Arm window ms", "Accept delay ms", "TPA to me", "TPA to bot", "Trade command", "Trigger text", "Delay ms"
   };
   private static final int close = 5;
   private static final int open = 4;
   private static final int poll = 3;
   private final Screen init_Screen;
   private final Consumer<autismclient.util.g.CachePool> init_Consumer;
   private final boolean init_Z;
   private final autismclient.util.g.CachePool init_CachePool;
   private final List<PoolEntry.Inner_DataUtil> init_List = new ArrayList<>();
   private final List<Runnable> load_List = new ArrayList<>();
   private final List<autismclient.gui.f.NetHelper> read_List = new ArrayList<>();
   private final List<PoolEntry.Inner_CachePool> write_List = new ArrayList<>();
   private int push;
   private int pop;
   private int tick;
   private int run;
   private int get;
   private int set;
   private boolean load_Z;
   private int put;
   private int add;
   private int clear;
   private int reset;
   private int start = 6;
   private int stop = 1;
   private int next = 1;
   private int prev = 1;
   private List<String> update_List = List.of();
   private int valid;
   private boolean read_Z;
   private int size;

   public PoolEntry(Screen parent, autismclient.util.g.CachePool config, Consumer<autismclient.util.g.CachePool> onSave, boolean live) {
      super(Component.literal("Auto-Accept"));
      this.init_Screen = parent;
      this.init_Consumer = onSave;
      this.init_Z = live;
      this.init_CachePool = new autismclient.util.g.CachePool(config);

      for (autismclient.util.g.CachePool.Inner_DataUtil responder : this.init_CachePool.init_List) {
         PoolEntry.Inner_DataUtil draft = new PoolEntry.Inner_DataUtil();
         draft.init_Str = responder.init_Str();
         draft.load = responder.load_Str();
         draft.init_Z = responder.load_Z();
         draft.read = responder.read();
         draft.init_I = responder.init_I();
         this.init_List.add(draft);
      }
   }

   @Override
   protected void init() {
      this.load_V_19();
   }

   private void load_V_19() {
      this.clearWidgets();
      this.load_List.clear();
      this.read_List.clear();
      this.write_List.clear();
      int ctl = read_I_12();
      int headerH = autismclient.gui.f.RenderState.init(14);
      this.next = 1;

      for (String label : init_StrArr) {
         this.next = Math.max(this.next, k.init(this.font, label, init_CharEncoder.init_Identifier_l(l.read), 0));
      }

      this.prev = Math.max(this.init_I_Str_15("Macro"), this.init_I_Str_15("Text"));
      this.get = this.sync() + 4;
      this.set = this.sync() + this.flush() - 4;
      this.clear = this.init_I_Str_15("Done");
      this.reset = ctl;
      this.put = this.sync() + this.flush() - 4 - this.clear;
      int tipX = this.sync() + 4;
      this.update_List = this.init_List(
         "{bot} = bot name. {name}/{me} = your on-server name. Bots watch chat for these.",
         Math.max(1, this.put - tipX - 8),
         ct.init_I_I_ct_DataUtil(ValueSink.close, ct.Inner_DataUtil.read)
      );
      int footerH = Math.max(ctl, this.update_List.size() * 10);
      int tpaH = init_I_I_12(5);
      int tradeH = init_I_I_12(4);
      int customH = init_I_I_12(3);
      int addH = ctl;
      List<Integer> heights = new ArrayList<>();
      heights.add(tpaH);
      heights.add(tradeH);

      for (int i = 0; i < this.init_List.size(); i++) {
         heights.add(customH);
      }

      heights.add(addH);
      int contentH = 0;

      for (int h : heights) {
         contentH += h;
      }

      contentH += 4 * Math.max(0, heights.size() - 1);
      int chrome = 4 + headerH + 2 + 4 + footerH + 4;
      int viewportH = Math.max(1, Math.min(contentH, this.load_I() - 12 - chrome));
      this.stop = chrome + viewportH;
      this.start = Math.max(6, (this.load_I() - this.stop) / 2);
      this.tick = this.start + 4 + headerH + 2;
      this.run = this.tick + viewportH;
      int footerTop = this.run + 4;
      this.add = footerTop + (footerH - ctl) / 2;
      this.valid = footerTop + (footerH - this.update_List.size() * 10) / 2;
      this.load_Z = contentH > viewportH;
      this.pop = Math.max(0, contentH - viewportH);
      this.push = Math.max(0, Math.min(this.push, this.pop));
      int cardX = this.get;
      int cardW = this.set - this.get - (this.load_Z ? 8 : 0);
      List<PoolEntry.Inner_StreamUtil> sections = new ArrayList<>();
      sections.add(new PoolEntry.Inner_StreamUtil(tpaH, yx -> this.init(cardX, yx, cardW)));
      sections.add(new PoolEntry.Inner_StreamUtil(tradeH, yx -> this.load(cardX, yx, cardW)));

      for (int i = 0; i < this.init_List.size(); i++) {
         int index = i;
         sections.add(new PoolEntry.Inner_StreamUtil(customH, yx -> this.init(cardX, yx, cardW, index)));
      }

      sections.add(new PoolEntry.Inner_StreamUtil(addH, yx -> this.read(cardX, yx, cardW)));
      int y = this.tick - this.push;

      for (PoolEntry.Inner_StreamUtil section : sections) {
         if (y + section.init_I() > this.tick && y < this.run) {
            section.init_IntConsumer().accept(y);
         }

         y += section.init_I() + 4;
      }
   }

   private static int read_I_12() {
      return autismclient.gui.f.RenderState.init(13);
   }

   private static int write_I_10() {
      return read_I_12() + 2;
   }

   private static int update_I_9() {
      return 2 + read_I_12() + 2;
   }

   private static int init_I_I_12(int rows) {
      return update_I_9() + rows * write_I_10() - 2 + 4;
   }

   private int init(int y, int h, l tone) {
      return j.init(y, h, k.init(init_CharEncoder.init_Identifier_l(tone)), 0);
   }

   private void init(int cardX, int top, int cardW) {
      int toggleW = this.init_I_Str_15("Off");
      this.init_V_I(cardX, top, cardW, init_I_I_12(5), "TPA", toggleW + 2);
      this.init_V_I_2(cardX, top, cardW, toggleW, this.init_CachePool.init_Z, () -> this.init_CachePool.init_Z = !this.init_CachePool.init_Z);
      int innerX = cardX + 4;
      int innerW = cardW - 8;
      int ry = top + update_I_9();
      int fx = innerX + this.next + 2;
      int fw = innerW - this.next - 2;
      this.init(
         innerX,
         ry,
         innerW,
         this.init_CachePool.load_Z,
         this.init_CachePool.write_Str,
         this.init_CachePool.update_Str,
         "/tpaccept {name}",
         v -> this.init_CachePool.write_Str = v,
         () -> this.init_CachePool.load_Z = !this.init_CachePool.load_Z,
         () -> this.init(this.init_CachePool.update_Str, n -> this.init_CachePool.update_Str = n)
      );
      ry += write_I_10();
      this.init_V("Arm window ms", innerX, ry);
      this.init_V_I_3(fx, ry, fw, "15000", this.init_CachePool.load_I, v -> this.init_CachePool.load_I = v);
      ry += write_I_10();
      this.init_V("Accept delay ms", innerX, ry);
      this.init_V_I_3(fx, ry, fw, "300", this.init_CachePool.read_I, v -> this.init_CachePool.read_I = v);
      ry += write_I_10();
      this.init_V("TPA to me", innerX, ry);
      this.init_V_I_4(fx, ry, fw, "/tpahere {bot}", this.init_CachePool.init_Str, v -> this.init_CachePool.init_Str = v);
      ry += write_I_10();
      this.init_V("TPA to bot", innerX, ry);
      this.init_V_I_4(fx, ry, fw, "/tpa {bot}", this.init_CachePool.load_Str, v -> this.init_CachePool.load_Str = v);
   }

   private void load(int cardX, int top, int cardW) {
      int toggleW = this.init_I_Str_15("Off");
      this.init_V_I(cardX, top, cardW, init_I_I_12(4), "Trade", toggleW + 2);
      this.init_V_I_2(cardX, top, cardW, toggleW, this.init_CachePool.read_Z, () -> this.init_CachePool.read_Z = !this.init_CachePool.read_Z);
      int innerX = cardX + 4;
      int innerW = cardW - 8;
      int ry = top + update_I_9();
      int fx = innerX + this.next + 2;
      int fw = innerW - this.next - 2;
      this.init(
         innerX,
         ry,
         innerW,
         this.init_CachePool.write_Z,
         this.init_CachePool.sync,
         this.init_CachePool.flush,
         "/trade accept",
         v -> this.init_CachePool.sync = v,
         () -> this.init_CachePool.write_Z = !this.init_CachePool.write_Z,
         () -> this.init(this.init_CachePool.flush, n -> this.init_CachePool.flush = n)
      );
      ry += write_I_10();
      this.init_V("Arm window ms", innerX, ry);
      this.init_V_I_3(fx, ry, fw, "15000", this.init_CachePool.write_I, v -> this.init_CachePool.write_I = v);
      ry += write_I_10();
      this.init_V("Accept delay ms", innerX, ry);
      this.init_V_I_3(fx, ry, fw, "300", this.init_CachePool.update_I, v -> this.init_CachePool.update_I = v);
      ry += write_I_10();
      this.init_V("Trade command", innerX, ry);
      this.init_V_I_4(fx, ry, fw, "/trade {bot}", this.init_CachePool.read_Str, v -> this.init_CachePool.read_Str = v);
   }

   private void init(int cardX, int top, int cardW, int index) {
      PoolEntry.Inner_DataUtil draft = this.init_List.get(index);
      int removeW = this.init_I_Str_15("Remove");
      this.init_V_I(cardX, top, cardW, init_I_I_12(3), "Custom " + (index + 1), removeW + 2);
      this.init(cardX + cardW - 4 - removeW, top + 2, removeW, read_I_12(), "Remove", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, b -> {
         this.read_V_12();
         if (index < this.init_List.size()) {
            this.init_List.remove(index);
         }

         this.write_V_10();
         this.load_V_19();
      });
      int innerX = cardX + 4;
      int innerW = cardW - 8;
      int ry = top + update_I_9();
      int fx = innerX + this.next + 2;
      int fw = innerW - this.next - 2;
      this.init_V("Trigger text", innerX, ry);
      this.init_V_I_4(fx, ry, fw, "text to watch for", draft.init_Str, v -> draft.init_Str = v);
      ry += write_I_10();
      this.init(
         innerX,
         ry,
         innerW,
         draft.init_Z,
         draft.load,
         draft.read,
         "/reply",
         v -> draft.load = v,
         () -> draft.init_Z = !draft.init_Z,
         () -> this.init(draft.read, n -> draft.read = n)
      );
      ry += write_I_10();
      this.init_V("Delay ms", innerX, ry);
      this.init_V_I_3(fx, ry, fw, "300", draft.init_I, v -> draft.init_I = v);
   }

   private void read(int cardX, int top, int cardW) {
      if (this.init_List.size() >= 6) {
         this.write_List.add(new PoolEntry.Inner_CachePool("Maximum of 6 custom responders reached.", cardX, this.init(top, read_I_12(), l.read), true, cardW));
      } else {
         int addW = Math.min(cardW, this.init_I_Str_15("+ Add custom responder"));
         this.init(cardX, top, addW, read_I_12(), "+ Add custom responder", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, b -> {
            this.read_V_12();
            if (this.init_List.size() < 6) {
               this.init_List.add(new PoolEntry.Inner_DataUtil());
            }

            this.write_V_10();
            this.load_V_19();
         });
      }
   }

   private void init(
      int innerX,
      int y,
      int innerW,
      boolean useMacro,
      String textValue,
      String macroName,
      String hint,
      Consumer<String> textSink,
      Runnable toggleMode,
      Runnable pick
   ) {
      this.init_V("Accept", innerX, y);
      int gap = 2;
      int fieldX = innerX + this.next + gap;
      int fieldW = innerW - this.next - gap - this.prev - gap;
      if (useMacro) {
         this.init(fieldX, y, fieldW, read_I_12(), "Macro: " + load(macroName), autismclient.gui.f.b.CachePool.Inner_DataUtil.init, b -> pick.run());
      } else {
         this.init_V_I_4(fieldX, y, fieldW, hint, textValue, textSink);
      }

      this.init(
         fieldX + fieldW + gap,
         y,
         this.prev,
         read_I_12(),
         useMacro ? "Macro" : "Text",
         useMacro ? autismclient.gui.f.b.CachePool.Inner_DataUtil.read : autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
         b -> {
            this.read_V_12();
            toggleMode.run();
            this.write_V_10();
            this.load_V_19();
         }
      );
   }

   private void init_V_I_2(int cardX, int top, int cardW, int w, boolean on, Runnable toggle) {
      this.init(
         cardX + cardW - 4 - w,
         top + 2,
         w,
         read_I_12(),
         on ? "On" : "Off",
         on ? autismclient.gui.f.b.CachePool.Inner_DataUtil.write : autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
         b -> {
            this.read_V_12();
            toggle.run();
            this.write_V_10();
            this.load_V_19();
         }
      );
   }

   private void init_V_I(int x, int y, int w, int h, String title, int rightReserve) {
      this.read_List.add(autismclient.gui.f.NetHelper.init(x, y, w, h));
      this.write_List.add(new PoolEntry.Inner_CachePool(title, x + 4, this.init(y + 2, read_I_12(), l.read), false, w - 8 - rightReserve));
   }

   private void init_V(String text, int x, int y) {
      this.write_List.add(new PoolEntry.Inner_CachePool(text, x, this.init(y, read_I_12(), l.read), true, this.next));
   }

   private void init_V_I_4(int x, int y, int w, String hint, String value, Consumer<String> sink) {
      EditBox box = new StateCache(this.font, x, y, Math.max(1, w), read_I_12(), Component.literal(hint));
      box.setMaxLength(96);
      box.setValue(value == null ? "" : value);
      box.setHint(Component.literal(hint));
      this.addRenderableWidget(box);
      this.load_List.add(() -> sink.accept(box.getValue()));
   }

   private void init_V_I_3(int x, int y, int w, String hint, int value, IntConsumer sink) {
      EditBox box = new StateCache(this.font, x, y, Math.max(1, w), read_I_12(), Component.literal(hint));
      box.setMaxLength(8);
      box.setValue(Integer.toString(value));
      box.setHint(Component.literal(hint));
      this.addRenderableWidget(box);
      this.load_List.add(() -> {
         try {
            sink.accept(Integer.parseInt(box.getValue().trim()));
         } catch (RuntimeException var3x) {
         }
      });
   }

   private int init_I_Str_15(String label) {
      return autismclient.gui.f.RenderState.load(k.init(this.font, label, init_CharEncoder.init_Identifier_l(l.read), 0));
   }

   private StreamGate init(int x, int y, int w, int h, String label, autismclient.gui.f.b.CachePool.Inner_DataUtil tone, OnPress press) {
      autismclient.gui.f.b.CachePool.Inner_DataUtil interactive = tone == autismclient.gui.f.b.CachePool.Inner_DataUtil.init
         ? autismclient.gui.f.b.CachePool.Inner_DataUtil.load
         : tone;
      String fit = k.load(
         this.font, label, Math.max(1, w - 6), init_CharEncoder.init_Identifier_l(l.read), ct.init_I_I_ct_DataUtil(ValueSink.flush, ct.Inner_DataUtil.read)
      );
      StreamGate button = new StreamGate(x, y, Math.max(1, w), h, Component.literal(fit), interactive, press);
      this.addRenderableWidget(button);
      return button;
   }

   private void read_V_12() {
      for (Runnable op : this.load_List) {
         op.run();
      }

      this.init_CachePool.init_List.clear();

      for (PoolEntry.Inner_DataUtil draft : this.init_List) {
         this.init_CachePool
            .init_List
            .add(new autismclient.util.g.CachePool.Inner_DataUtil(draft.init_Str, draft.load, draft.init_Z, draft.read, draft.init_I));
      }

      this.init_CachePool.init_V();
   }

   private void init(String current, Consumer<String> setter) {
      this.read_V_12();
      this.minecraft.gui.setScreen(new BitSetUtil(this, current, name -> {
         setter.accept(name == null ? "" : name);
         this.write_V_10();
      }));
   }

   private void write_V_10() {
      if (this.init_Z && this.init_Consumer != null) {
         this.init_Consumer.accept(new autismclient.util.g.CachePool(this.init_CachePool));
      }
   }

   private void update_V_9() {
      this.read_V_12();
      if (this.init_Consumer != null) {
         this.init_Consumer.accept(new autismclient.util.g.CachePool(this.init_CachePool));
      }

      this.minecraft.gui.setScreen(this.init_Screen);
   }

   @Override
   public void onClose() {
      this.update_V_9();
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      int vmx = cA.init_I_D(mouseX);
      int vmy = cA.init_I_D(mouseY);
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         autismclient.gui.f.PacketUtil.init(
            graphics, autismclient.gui.f.NetHelper.init(0, 0, this.init_I(), this.load_I()), ct.init_I_I_ct_DataUtil(-15987181, ct.Inner_DataUtil.update)
         );
         int x = this.sync();
         int panelW = this.flush();
         int text = ct.init_I_I_ct_DataUtil(ValueSink.flush, ct.Inner_DataUtil.read);
         int muted = ct.init_I_I_ct_DataUtil(ValueSink.close, ct.Inner_DataUtil.read);
         autismclient.gui.f.PacketUtil.load(
            graphics,
            autismclient.gui.f.NetHelper.init(x, this.start, panelW, this.stop),
            6,
            ct.init_I_I_ct_DataUtil(-401402080, ct.Inner_DataUtil.close),
            ct.init_I_I_ct_DataUtil(ValueSink.update, ct.Inner_DataUtil.load)
         );
         this.init(
            graphics, "Auto-Accept - TPA & Trade", x + 4, this.init(this.start + 4, autismclient.gui.f.RenderState.init(14), l.init), text, panelW - 8, l.init
         );
         cA.init(graphics, this.get - 2, this.tick, this.set + 2, this.run);
         int cardFill = ct.init_I_I_ct_DataUtil(-15987181, ct.Inner_DataUtil.close);
         int cardBorder = ct.init_I_I_ct_DataUtil(ValueSink.update, ct.Inner_DataUtil.load);

         for (autismclient.gui.f.NetHelper card : this.read_List) {
            autismclient.gui.f.PacketUtil.load(graphics, card, 6, cardFill, cardBorder);
         }

         for (PoolEntry.Inner_CachePool td : this.write_List) {
            this.init_V_GuiGraphicsExtract_3(graphics, td.init_Str(), td.init_I(), td.load(), td.init_Z() ? muted : text, td.read());
         }

         super.extractRenderState(graphics, vmx, vmy, delta);
         graphics.disableScissor();
         autismclient.gui.f.b.PoolEntry.Inner_DataUtil metrics = this.init_PoolEntry_DataUtil_5();
         autismclient.gui.f.b.PoolEntry.init(graphics, metrics, metrics.init(vmx, vmy), this.read_Z);
         int tipX = x + 4;
         int tipW = Math.max(1, this.put - tipX - 8);

         for (int i = 0; i < this.update_List.size(); i++) {
            this.init_V_GuiGraphicsExtract_3(graphics, this.update_List.get(i), tipX, this.valid + i * 10, muted, tipW);
         }

         autismclient.gui.f.NetHelper doneBounds = autismclient.gui.f.NetHelper.init(this.put, this.add, this.clear, this.reset);
         autismclient.gui.f.PacketUtil.load(graphics, doneBounds, 3, ct.init_I_I_ct_DataUtil(-15594737, ct.Inner_DataUtil.close));
         boolean doneHover = vmx >= this.put && vmx < this.put + this.clear && vmy >= this.add && vmy < this.add + this.reset;
         autismclient.gui.f.b.CachePool.init(
            autismclient.gui.f.UiHelper.init(graphics, this.font, vmx, vmy),
            doneBounds,
            "Done",
            autismclient.gui.f.b.CachePool.Inner_DataUtil.read,
            doneHover,
            false
         );
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void init_V_GuiGraphicsExtract_3(GuiGraphicsExtractor graphics, String value, int x, int y, int color, int width) {
      this.init(graphics, value, x, y, color, width, l.read);
   }

   private void init(GuiGraphicsExtractor graphics, String value, int x, int y, int color, int width, l tone) {
      String shown = k.load(this.font, autismclient.util.g.PacketUtil.update(value, 96), Math.max(1, width), init_CharEncoder.init_Identifier_l(tone), color);
      k.init(graphics, this.font, shown, init_CharEncoder.init_Identifier_l(tone), color, x, y, false);
   }

   private List<String> init_List(String value, int width, int color) {
      List<String> lines = new ArrayList<>();
      StringBuilder line = new StringBuilder();

      for (String word : value.split(" ")) {
         String candidate = line.isEmpty() ? word : line + " " + word;
         if (!line.isEmpty() && k.init(this.font, candidate, init_CharEncoder.init_Identifier_l(l.read), color) > width) {
            lines.add(line.toString());
            line = new StringBuilder(word);
         } else {
            line = new StringBuilder(candidate);
         }
      }

      if (!line.isEmpty()) {
         lines.add(line.toString());
      }

      while (lines.size() > 2) {
         String tail = lines.remove(2);
         lines.set(1, lines.get(1) + " " + tail);
      }

      return lines;
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
      MouseButtonEvent virtual = init_MouseButtonEvent_MouseButtonEvent(event);
      int vmx = (int)virtual.x();
      int vmy = (int)virtual.y();
      if (vmx >= this.put && vmx < this.put + this.clear && vmy >= this.add && vmy < this.add + this.reset) {
         this.update_V_9();
         return true;
      } else {
         autismclient.gui.f.b.PoolEntry.Inner_DataUtil metrics = this.init_PoolEntry_DataUtil_5();
         if (metrics.init_Z() && metrics.init(vmx, vmy)) {
            this.read_Z = true;
            this.size = metrics.load(vmx, vmy) ? vmy - metrics.update() : metrics.sync() / 2;
            this.init_V_I_8(autismclient.gui.f.b.PoolEntry.init(metrics, vmy, this.size));
            return true;
         } else {
            return vmy >= this.tick && vmy < this.run ? super.mouseClicked(virtual, doubled) : true;
         }
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      this.read_Z = false;
      return super.mouseReleased(init_MouseButtonEvent_MouseButtonEvent(event));
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
      MouseButtonEvent virtual = init_MouseButtonEvent_MouseButtonEvent(event);
      if (this.read_Z) {
         this.init_V_I_8(autismclient.gui.f.b.PoolEntry.init(this.init_PoolEntry_DataUtil_5(), virtual.y(), this.size));
         return true;
      } else {
         return super.mouseDragged(virtual, cA.init_D_D(dragX), cA.init_D_D(dragY));
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
      int vmx = cA.init_I_D(mouseX);
      int vmy = cA.init_I_D(mouseY);
      if (vmx >= this.sync() && vmx < this.sync() + this.flush() && vmy >= this.tick && vmy < this.run && this.pop > 0) {
         this.read_V_12();
         this.push = Math.max(0, Math.min(this.pop, this.push - (int)(vertical * write_I_10())));
         this.load_V_19();
         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
      }
   }

   private autismclient.gui.f.b.PoolEntry.Inner_DataUtil init_PoolEntry_DataUtil_5() {
      int viewportH = Math.max(1, this.run - this.tick);
      return autismclient.gui.f.b.PoolEntry.init_PoolEntry_DataUtil_I_2(viewportH + this.pop, viewportH, this.set - 6, this.tick, 6, viewportH, this.push);
   }

   private void init_V_I_8(int value) {
      this.read_V_12();
      this.push = Math.max(0, Math.min(this.pop, value));
      this.load_V_19();
   }

   private int sync() {
      return Math.max(1, (this.init_I() - this.flush()) / 2);
   }

   private int flush() {
      return Math.max(1, Math.min(474, this.init_I() - 12));
   }

   private static String load(String name) {
      return name != null && !name.isBlank() ? name : "none";
   }

   private record Inner_CachePool(String init_Str, int init_I, int load, boolean init_Z, int read) {
   }

   private static final class Inner_DataUtil {
      String init_Str = "";
      String load = "";
      boolean init_Z = false;
      String read = "";
      int init_I = 300;
   }

   private record Inner_StreamUtil(int init_I, IntConsumer init_IntConsumer) {
   }
}
