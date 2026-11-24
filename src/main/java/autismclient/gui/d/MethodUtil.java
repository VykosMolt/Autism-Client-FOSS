package autismclient.gui.d;

import autismclient.compat263.Compat263;
import autismclient.gui.f.b.j;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import autismclient.util.AutismPacketSelectorOverlay;
import autismclient.util.cA;
import autismclient.util.ct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class MethodUtil extends KeyHolder {
   private static final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = 6;
   private static final int load_I = 4;
   private static final int read_I = 150;
   private static final String init_Str = "Packets sent together";
   private final Screen init_Screen;
   private final int write;
   private final Consumer<autismclient.util.g.ListPool> init_Consumer;
   private final Function<autismclient.util.g.ListPool, autismclient.util.g.PacketUtil.Inner_CachePool> init_Function;
   private final String load_Str;
   private final List<String[]> init_List = new ArrayList<>();
   private final List<EditBox> load_List = new ArrayList<>();
   private EditBox init_EditBox;
   private AutismPacketSelectorOverlay init_AutismPacketSelect;
   private String read_Str = "";
   private int update = ValueSink.close;

   public MethodUtil(
      Screen parent,
      int slot,
      autismclient.util.g.ListPool action,
      Consumer<autismclient.util.g.ListPool> save,
      Function<autismclient.util.g.ListPool, autismclient.util.g.PacketUtil.Inner_CachePool> test
   ) {
      super(Component.literal("Quick Action"));
      this.init_Screen = parent;
      this.write = slot;
      this.init_Consumer = save;
      this.init_Function = test;
      this.load_Str = action == null ? "" : action.init_Str;
      if (action != null) {
         for (autismclient.util.g.ListPool.Inner_DataUtil step : action.init_List) {
            this.init_List.add(new String[]{step.init_Str(), step.load()});
         }
      }

      if (this.init_List.isEmpty()) {
         this.init_List.add(new String[]{"", ""});
      }
   }

   @Override
   protected void init() {
      this.load_V_14();
   }

   private void load_V_14() {
      String name = this.init_EditBox == null ? this.load_Str : this.init_EditBox.getValue();
      this.read_V_8();
      this.clearWidgets();
      this.load_List.clear();
      int w = this.get();
      int x = (this.init_I() - w) / 2;
      int ctl = read_I_8();
      int gap = 2;
      this.init_EditBox = new StateCache(this.font, x + 4, this.poll(), w - 8, ctl, Component.literal("Name"));
      this.init_EditBox.setMaxLength(32);
      this.init_EditBox.setHint(Component.literal("Preset name (optional)"));
      this.init_EditBox.setValue(name);
      this.addRenderableWidget(this.init_EditBox);
      int y = this.pop();
      int packetW = Math.min(150, Math.max(40, (w - 8) / 3));
      int delW = this.flush_I_5();
      int argsX = x + 4 + packetW + gap;
      int argsW = Math.max(1, x + w - 4 - delW - gap - argsX);

      for (int i = 0; i < this.init_List.size(); i++) {
         int index = i;
         String[] step = this.init_List.get(i);
         String packetLabel = step[0].isBlank() ? "Choose packet" : autismclient.util.g.ListPool.init_Str_Str(step[0]);
         this.init(
            x + 4,
            y,
            packetW,
            ctl,
            packetLabel,
            step[0].isBlank() ? autismclient.gui.f.b.CachePool.Inner_DataUtil.read : autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
            b -> this.init_V_I_6(index)
         );
         EditBox args = new StateCache(this.font, argsX, y, argsW, ctl, Component.literal("Args"));
         args.setMaxLength(2048);
         args.setHint(Component.literal("packet args"));
         args.setValue(step[1]);
         this.addRenderableWidget(args);
         this.load_List.add(args);
         this.init(x + w - 4 - delW, y, delW, ctl, "X", autismclient.gui.f.b.CachePool.Inner_DataUtil.update, b -> this.load(index));
         y += write_I_7();
      }

      if (this.init_Z_7()) {
         this.init(x + 4, y, this.init_I_Str_12("+ Add packet"), ctl, "+ Add packet", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, b -> this.write_V_7());
      }

      int by = this.tick_I_10();
      String[] labels = new String[]{"Save", "Test", "Clear", "Cancel"};
      int[] widths = new int[labels.length];
      int sum = gap * (labels.length - 1);

      for (int i = 0; i < labels.length; i++) {
         widths[i] = this.init_I_Str_12(labels[i]);
         sum += widths[i];
      }

      int inner = w - 8;
      if (sum > inner) {
         int each = Math.max(1, (inner - gap * (labels.length - 1)) / labels.length);
         Arrays.fill(widths, each);
         widths[labels.length - 1] = Math.max(1, inner - (each + gap) * (labels.length - 1));
         sum = inner;
      }

      int fx = x + w - 4 - sum;
      this.init(fx, by, widths[0], ctl, "Save", autismclient.gui.f.b.CachePool.Inner_DataUtil.write, b -> this.update_V_7());
      fx += widths[0] + gap;
      this.init(fx, by, widths[1], ctl, "Test", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, b -> this.flush_V_5());
      fx += widths[1] + gap;
      this.init(fx, by, widths[2], ctl, "Clear", autismclient.gui.f.b.CachePool.Inner_DataUtil.update, b -> this.sync_V_6());
      fx += widths[2] + gap;
      this.init(fx, by, widths[3], ctl, "Cancel", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, b -> this.onClose());
   }

   private static int read_I_8() {
      return autismclient.gui.f.RenderState.init(13);
   }

   private static int write_I_7() {
      return read_I_8() + 2;
   }

   private static int update_I_7() {
      return autismclient.gui.f.RenderState.init(14);
   }

   private static int sync_I_6() {
      return autismclient.gui.f.RenderState.init(12);
   }

   private int init_I_Str_12(String text) {
      return autismclient.gui.f.RenderState.load(k.init(this.font, text, init_CharEncoder.init_Identifier_l(l.read), 0));
   }

   private int flush_I_5() {
      return Math.max(read_I_8(), k.init(this.font, "X", init_CharEncoder.init_Identifier_l(l.read), 0) + 10);
   }

   private boolean init_Z_7() {
      return this.init_List.size() < 6;
   }

   private int close() {
      int rows = this.init_List.size() + (this.init_Z_7() ? 1 : 0);
      return 4 + update_I_7() + 2 + read_I_8() + 4 + sync_I_6() + rows * write_I_7() - 2 + 4 + read_I_8() + 4;
   }

   private int open() {
      return Math.max(6, (this.load_I() - this.close()) / 2);
   }

   private int poll() {
      return this.open() + 4 + update_I_7() + 2;
   }

   private int push() {
      return this.poll() + read_I_8() + 4;
   }

   private int pop() {
      return this.push() + sync_I_6();
   }

   private int tick_I_10() {
      return this.open() + this.close() - 4 - read_I_8();
   }

   private int init(int y, int h, l tone) {
      return j.init(y, h, k.init(init_CharEncoder.init_Identifier_l(tone)), 0);
   }

   private void read_V_8() {
      for (int i = 0; i < this.load_List.size() && i < this.init_List.size(); i++) {
         this.init_List.get(i)[1] = this.load_List.get(i).getValue();
      }
   }

   private void init_V_I_6(int index) {
      if (this.init_AutismPacketSelect == null) {
         this.init_AutismPacketSelect = new AutismPacketSelectorOverlay(this.font);
      }

      this.init_AutismPacketSelect.init_V_Consumer(packetClass -> {
         this.init_List.get(index)[0] = packetClass.getName();
         if (this.init_EditBox == null || this.init_EditBox.getValue().isBlank()) {
            String label = autismclient.util.g.ListPool.init_Str_Str(packetClass.getName());
            if (this.init_EditBox != null) {
               this.init_EditBox.setValue(label);
            }
         }

         this.read_Str = "Selected";
         this.update = ValueSink.open;
         this.load_V_14();
      }, autismclient.util.g.HashUtil.init(), true);
   }

   private void write_V_7() {
      this.read_V_8();
      if (this.init_List.size() < 6) {
         this.init_List.add(new String[]{"", ""});
      }

      this.load_V_14();
   }

   private void load(int index) {
      this.read_V_8();
      if (index >= 0 && index < this.init_List.size()) {
         this.init_List.remove(index);
      }

      if (this.init_List.isEmpty()) {
         this.init_List.add(new String[]{"", ""});
      }

      this.load_V_14();
   }

   private autismclient.util.g.ListPool init_ListPool() {
      this.read_V_8();
      autismclient.util.g.ListPool action = new autismclient.util.g.ListPool();
      action.init_Str = this.init_EditBox == null ? "" : this.init_EditBox.getValue();

      for (String[] step : this.init_List) {
         if (!step[0].isBlank()) {
            action.init_List.add(new autismclient.util.g.ListPool.Inner_DataUtil(step[0], step[1]));
         }
      }

      action.init_V();
      return action;
   }

   private void update_V_7() {
      if (this.init_Consumer != null) {
         this.init_Consumer.accept(this.init_ListPool());
      }

      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.init_Screen);
      }
   }

   private void sync_V_6() {
      if (this.init_Consumer != null) {
         this.init_Consumer.accept(new autismclient.util.g.ListPool());
      }

      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.init_Screen);
      }
   }

   private void flush_V_5() {
      autismclient.util.g.ListPool action = this.init_ListPool();
      if (action.init_Z()) {
         this.read_Str = "Add a packet first";
         this.update = ValueSink.close;
      } else {
         autismclient.util.g.PacketUtil.Inner_CachePool result = this.init_Function == null ? null : this.init_Function.apply(action);
         this.read_Str = init_Str_PacketUtil_CachePo(result);
         this.update = result != null && result.read() <= 0 ? (result.load() > 0 ? ValueSink.close : ValueSink.open) : ValueSink.poll;
      }
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
         int w = this.get();
         int x = (this.init_I() - w) / 2;
         int panelY = this.open();
         autismclient.gui.f.PacketUtil.load(
            graphics,
            autismclient.gui.f.NetHelper.init(x, panelY, w, this.close()),
            6,
            ct.init_I_I_ct_DataUtil(-401402080, ct.Inner_DataUtil.close),
            ct.init_I_I_ct_DataUtil(ValueSink.update, ct.Inner_DataUtil.load)
         );
         this.init(
            graphics,
            "Edit Slot " + (this.write + 1),
            x + 4,
            this.init(panelY + 4, update_I_7(), l.init),
            ct.init_I_I_ct_DataUtil(ValueSink.flush, ct.Inner_DataUtil.read),
            l.init
         );
         String group = "Packets sent together (" + this.run() + "/6)";
         int groupTextY = this.init(this.push(), sync_I_6(), l.read);
         this.init(graphics, group, x + 4, groupTextY, ct.init_I_I_ct_DataUtil(ValueSink.close, ct.Inner_DataUtil.read));
         if (!this.read_Str.isBlank()) {
            int statusX = x + 4 + k.init(this.font, group, init_CharEncoder.init_Identifier_l(l.read), 0) + 8;
            this.init(graphics, this.read_Str, statusX, groupTextY, Math.max(1, x + w - 4 - statusX), init_I_I_8(this.update));
         }

         super.extractRenderState(graphics, virtualMouseX, virtualMouseY, delta);
         if (this.init_AutismPacketSelect != null && this.init_AutismPacketSelect.sync_Z()) {
            this.init_AutismPacketSelect.init_V_GuiGraphicsExtract_15(graphics, virtualMouseX, virtualMouseY, delta);
         }
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private int run() {
      int count = 0;

      for (String[] step : this.init_List) {
         if (!step[0].isBlank()) {
            count++;
         }
      }

      return count;
   }

   @Override
   public boolean keyPressed(KeyEvent event) {
      if (this.init_AutismPacketSelect != null && this.init_AutismPacketSelect.sync_Z()) {
         this.init_AutismPacketSelect.init_Z_I(Compat263.keyEventGlfwKey(event), event.keycode(), Compat263.keyEventGlfwModifiers(event));
         return true;
      } else {
         return super.keyPressed(event);
      }
   }

   @Override
   public boolean charTyped(CharacterEvent event) {
      if (this.init_AutismPacketSelect != null && this.init_AutismPacketSelect.sync_Z()) {
         this.init_AutismPacketSelect.init_Z_C_I((char)event.codepoint(), 0);
         return true;
      } else {
         return super.charTyped(event);
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
      MouseButtonEvent virtualEvent = init_MouseButtonEvent_MouseButtonEvent(event);
      if (this.init_AutismPacketSelect != null && this.init_AutismPacketSelect.sync_Z()) {
         this.init_AutismPacketSelect.init_Z_D_3((float)virtualEvent.x(), (float)virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent));
         return true;
      } else {
         return super.mouseClicked(virtualEvent, doubled);
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      MouseButtonEvent virtualEvent = init_MouseButtonEvent_MouseButtonEvent(event);
      if (this.init_AutismPacketSelect != null && this.init_AutismPacketSelect.sync_Z()) {
         this.init_AutismPacketSelect.load_Z_D_3((float)virtualEvent.x(), (float)virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent));
         return true;
      } else {
         return super.mouseReleased(virtualEvent);
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
      MouseButtonEvent virtualEvent = init_MouseButtonEvent_MouseButtonEvent(event);
      if (this.init_AutismPacketSelect != null && this.init_AutismPacketSelect.sync_Z()) {
         this.init_AutismPacketSelect
            .init_Z_D(
               (float)virtualEvent.x(),
               (float)virtualEvent.y(),
               Compat263.mouseEventGlfwButton(virtualEvent),
               (float)cA.init_D_D(dragX),
               (float)cA.init_D_D(dragY)
            );
         return true;
      } else {
         return super.mouseDragged(virtualEvent, cA.init_D_D(dragX), cA.init_D_D(dragY));
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
      double vx = cA.init_D_D(mouseX);
      double vy = cA.init_D_D(mouseY);
      if (this.init_AutismPacketSelect != null && this.init_AutismPacketSelect.sync_Z()) {
         this.init_AutismPacketSelect.init_Z_D_2((float)vx, (float)vy, vertical);
         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
      }
   }

   @Override
   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.init_Screen);
      }
   }

   private StreamGate init(int x, int y, int w, int h, String text, autismclient.gui.f.b.CachePool.Inner_DataUtil tone, OnPress press) {
      autismclient.gui.f.b.CachePool.Inner_DataUtil interactiveTone = tone == autismclient.gui.f.b.CachePool.Inner_DataUtil.init
         ? autismclient.gui.f.b.CachePool.Inner_DataUtil.load
         : tone;
      StreamGate button = new StreamGate(x, y, Math.max(1, w), h, Component.literal(this.init_Str(text, w - 8)), interactiveTone, press);
      this.addRenderableWidget(button);
      return button;
   }

   private String init_Str(String text, int width) {
      return k.load(
         this.font,
         autismclient.util.g.PacketUtil.update(text, 64),
         Math.max(1, width),
         init_CharEncoder.init_Identifier_l(l.read),
         ct.init_I_I_ct_DataUtil(ValueSink.flush, ct.Inner_DataUtil.read)
      );
   }

   private int get() {
      return Math.max(1, Math.min(560, this.init_I() - 12));
   }

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int width, int color) {
      k.init(graphics, this.font, this.init_Str(text, width), init_CharEncoder.init_Identifier_l(l.read), color, x, y, false);
   }

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int color) {
      this.init(graphics, text, x, y, color, l.read);
   }

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int color, l tone) {
      k.init(graphics, this.font, autismclient.util.g.PacketUtil.update(text, 140), init_CharEncoder.init_Identifier_l(tone), color, x, y, false);
   }

   private static String init_Str_PacketUtil_CachePo(autismclient.util.g.PacketUtil.Inner_CachePool result) {
      if (result == null) {
         return "Failed";
      } else if (result.read() > 0) {
         return "Failed";
      } else if (result.init_I() > 0 && result.load() == 0) {
         return "Sent";
      } else if (result.init_I() > 0) {
         return "Sent " + result.init_I();
      } else {
         return result.load() > 0 ? "Skipped" : "Failed";
      }
   }

   private static int init_I_I_8(int color) {
      if (color == ValueSink.open) {
         return ct.init_I_I_ct_DataUtil(ValueSink.open, ct.Inner_DataUtil.sync);
      } else {
         return color == ValueSink.poll
            ? ct.init_I_I_ct_DataUtil(ValueSink.poll, ct.Inner_DataUtil.flush)
            : ct.init_I_I_ct_DataUtil(ValueSink.close, ct.Inner_DataUtil.read);
      }
   }
}
