package autismclient.gui.d;

import autismclient.compat263.Compat263;
import autismclient.gui.f.b.j;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import autismclient.util.AutismConfig;
import autismclient.util.AutismPacketSelectorOverlay;
import autismclient.util.bi;
import autismclient.util.bv;
import autismclient.util.bw;
import autismclient.util.cA;
import autismclient.util.ct;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public final class CharEncoder extends KeyHolder {
   private static final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = 470;
   private static final int load_I = 260;
   private static final int read = 6;
   private static final int write = 4;
   private static final int update = 4;
   private static final String init_Str = "Advanced - gravity & packet canceller";
   private static final String load_Str = "Bots only, separate from the PacketCanceller module. Empty = allow all.";
   private static final String[] init_StrArr = new String[]{
      "Bot falls to and rests on the ground. Off: it holds where the server puts it.",
      "Send a look packet every second (anti-AFK).",
      "Swing the arm every second (anti-AFK).",
      "Block the packets picked below on every bot. Off keeps the lists."
   };
   private final Screen init_Screen;
   private final Consumer<autismclient.util.g.IoWorker> init_Consumer;
   private final boolean init_Z;
   private final List<CharEncoder.Inner_DataUtil> init_List = new ArrayList<>();
   private autismclient.util.g.IoWorker init_IoWorker;
   private autismclient.util.g.CachePool init_CachePool;
   private final Consumer<autismclient.util.g.CachePool> load_Consumer;
   private AutismPacketSelectorOverlay init_AutismPacketSelect;
   private boolean load_Z;

   public CharEncoder(
      Screen parent,
      autismclient.util.g.IoWorker policy,
      Consumer<autismclient.util.g.IoWorker> save,
      autismclient.util.g.CachePool autoAccept,
      Consumer<autismclient.util.g.CachePool> saveAutoAccept,
      boolean live
   ) {
      super(Component.literal("Multi Packet Policy"));
      this.init_Screen = parent;
      this.init_IoWorker = new autismclient.util.g.IoWorker(policy);
      this.init_Consumer = save;
      this.init_CachePool = new autismclient.util.g.CachePool(autoAccept);
      this.load_Consumer = saveAutoAccept;
      this.init_Z = live;
   }

   @Override
   protected void init() {
      this.load_V_6();
   }

   private void load_V_6() {
      this.clearWidgets();
      this.init_List.clear();
      int x = this.read_I_4();
      int ctl = flush_I_3();
      int y = this.init_I_I_4(0);
      this.init(x, y, "Gravity", init_StrArr[0], this.init_IoWorker.init_Z(), this.init_IoWorker::init_V_Z);
      y = this.init_I_I_4(1);
      this.init(x, y, "Auto look", init_StrArr[1], this.init_IoWorker.read(), this.init_IoWorker::read);
      y = this.init_I_I_4(2);
      this.init(x, y, "Auto swing", init_StrArr[2], this.init_IoWorker.write(), this.init_IoWorker::write);
      y = this.init_I_I_4(3);
      this.init(x, y, "Packet canceller", init_StrArr[3], this.init_IoWorker.update(), this.init_IoWorker::update);
      y = this.push();
      int inner = this.write_I_4() - 8;
      int gap = 2;
      int each = Math.max(1, (inner - gap * 3) / 4);
      boolean cancelling = this.init_IoWorker.update();
      int c2sCount = this.init_IoWorker.init_I_IoWorker_DataUtil(autismclient.util.g.IoWorker.Inner_DataUtil.init);
      int s2cCount = this.init_IoWorker.init_I_IoWorker_DataUtil(autismclient.util.g.IoWorker.Inner_DataUtil.load);
      this.init(
            x + 4,
            y,
            each,
            ctl,
            "Block C2S (" + c2sCount + ")",
            autismclient.gui.f.b.CachePool.Inner_DataUtil.update,
            button -> this.init_V_IoWorker_DataUtil(autismclient.util.g.IoWorker.Inner_DataUtil.init)
         )
         .init(cancelling && c2sCount > 0);
      this.init(
            x + 4 + each + gap,
            y,
            each,
            ctl,
            "Block S2C (" + s2cCount + ")",
            autismclient.gui.f.b.CachePool.Inner_DataUtil.update,
            button -> this.init_V_IoWorker_DataUtil(autismclient.util.g.IoWorker.Inner_DataUtil.load)
         )
         .init(cancelling && s2cCount > 0);
      this.init(x + 4 + (each + gap) * 2, y, each, ctl, "Import", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, button -> this.read_V_4());
      this.init(x + 4 + (each + gap) * 3, y, inner - (each + gap) * 3, ctl, "Clear", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, button -> {
         this.init_IoWorker.init_V_List(List.of());
         this.sync_V_3();
         this.load_V_6();
      });
      y += ctl + gap;
      this.init(
         x + 4,
         y,
         this.init_I_Str_5("Auto-Accept: TPA & Trade..."),
         ctl,
         "Auto-Accept: TPA & Trade...",
         autismclient.gui.f.b.CachePool.Inner_DataUtil.read,
         button -> this.minecraft.gui.setScreen(new PoolEntry(this, this.init_CachePool, cfg -> {
            this.init_CachePool = new autismclient.util.g.CachePool(cfg);
            if (this.load_Consumer != null) {
               this.load_Consumer.accept(cfg);
            }
         }, this.init_Z))
      );
      int botY = this.tick_I_5();
      boolean tips = AutismConfig.getGlobal().multiShowTooltips;
      String tipsLabel = tips ? "Hover tips: On" : "Hover tips: Off";
      this.init(
         x + 4,
         botY,
         this.init_I_Str_5(tipsLabel),
         ctl,
         tipsLabel,
         tips ? autismclient.gui.f.b.CachePool.Inner_DataUtil.write : autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
         button -> {
            AutismConfig config = AutismConfig.getGlobal();
            config.multiShowTooltips = !config.multiShowTooltips;
            config.save();
            this.load_V_6();
         }
      );
      int doneW = this.init_I_Str_5("Done");
      this.init(x + this.write_I_4() - 4 - doneW, botY, doneW, ctl, "Done", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, button -> this.flush_V_3());
   }

   private void init(int x, int y, String label, String description, boolean on, Consumer<Boolean> setter) {
      this.init_List.add(new CharEncoder.Inner_DataUtil(label, description, y));
      int toggleW = this.init_I_Str_5(on ? "On" : "Off");
      int ctl = flush_I_3();
      this.init(
            x + this.write_I_4() - 4 - toggleW,
            y + (poll() - ctl) / 2,
            toggleW,
            ctl,
            on ? "On" : "Off",
            on ? autismclient.gui.f.b.CachePool.Inner_DataUtil.write : autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
            button -> {
               setter.accept(!on);
               this.sync_V_3();
               this.load_V_6();
            }
         )
         .init(on);
   }

   private int read_I_4() {
      return Math.max(1, (this.init_I() - this.write_I_4()) / 2);
   }

   private int write_I_4() {
      int want = k.init(this.font, "Advanced - gravity & packet canceller", init_CharEncoder.init_Identifier_l(l.init), 0);

      for (String description : init_StrArr) {
         want = Math.max(want, k.init(this.font, description, init_CharEncoder.init_Identifier_l(l.read), 0) + 4 + this.init_I_Str_5("Off"));
      }

      want = Math.max(260, want + 8);
      return Math.max(1, Math.min(Math.min(470, want), this.init_I() - 12));
   }

   private int update_I_4() {
      return this.tick_I_5() + flush_I_3() + 4 - this.sync_I_3();
   }

   private int sync_I_3() {
      int content = 4 + close() + 2 + 4 * (poll() + 2) - 2 + 4 + (flush_I_3() + 2) * 2 + open() + 2 + flush_I_3() + 4;
      return Math.max(6, (this.load_I() - content) / 2);
   }

   private static int flush_I_3() {
      return autismclient.gui.f.RenderState.init(13);
   }

   private static int close() {
      return autismclient.gui.f.RenderState.init(14);
   }

   private static int open() {
      return autismclient.gui.f.RenderState.init(12);
   }

   private static int poll() {
      return open() * 2;
   }

   private int init_I_I_4(int index) {
      return this.sync_I_3() + 4 + close() + 2 + index * (poll() + 2);
   }

   private int push() {
      return this.init_I_I_4(4) - 2 + 4;
   }

   private int pop() {
      return this.push() + (flush_I_3() + 2) * 2;
   }

   private int tick_I_5() {
      return this.pop() + open() + 2;
   }

   private int init(int y, int h, l tone) {
      return j.init(y, h, k.init(init_CharEncoder.init_Identifier_l(tone)), 0);
   }

   private void init_V_IoWorker_DataUtil(autismclient.util.g.IoWorker.Inner_DataUtil direction) {
      this.update_V_4();
      Set<Class<? extends Packet<?>>> selected = new LinkedHashSet<>();

      for (autismclient.util.g.IoWorker.Inner_CachePool rule : this.init_IoWorker.load_List()) {
         if (rule.init_IoWorker_DataUtil() == direction) {
            Class<? extends Packet<?>> packet = bw.init_Class_Str(rule.init_Str());
            if (packet != null) {
               selected.add(packet);
            }
         }
      }

      BiConsumer<Class<? extends Packet<?>>, Boolean> callback = (packetClass, enabled) -> {
         List<autismclient.util.g.IoWorker.Inner_CachePool> rules = new ArrayList<>(this.init_IoWorker.load_List());
         rules.removeIf(rulex -> rulex.init_IoWorker_DataUtil() == direction && rulex.init_Str().equals(packetClass.getName()));
         if (enabled) {
            rules.add(new autismclient.util.g.IoWorker.Inner_CachePool(direction, packetClass.getName()));
         }

         this.init_IoWorker.init_V_List(rules);
         this.sync_V_3();
         this.write_V_4();
      };
      if (direction == autismclient.util.g.IoWorker.Inner_DataUtil.load) {
         this.init_AutismPacketSelect.load_V_BiConsumer(callback, selected, autismclient.util.g.IoWorker.init_Set_IoWorker_DataUtil(direction));
      } else {
         this.init_AutismPacketSelect.init_V_BiConsumer(callback, selected, autismclient.util.g.IoWorker.init_Set_IoWorker_DataUtil(direction));
      }
   }

   private void read_V_4() {
      autismclient.util.g.IoWorker.Inner_StreamUtil imported = this.init_IoWorker.init_IoWorker_StreamUti();
      this.sync_V_3();
      this.load_V_6();
      if (this.init_IoWorker.update()) {
         bi.load(imported.init_Str());
      } else {
         bi.read(imported.init_Str() + " - canceller is off");
      }
   }

   private void write_V_4() {
      this.load_Z = true;
   }

   private void update_V_4() {
      if (this.init_AutismPacketSelect == null) {
         this.init_AutismPacketSelect = new AutismPacketSelectorOverlay(this.font);
      }
   }

   private void sync_V_3() {
      if (this.init_Z && this.init_Consumer != null) {
         this.init_Consumer.accept(new autismclient.util.g.IoWorker(this.init_IoWorker));
      }
   }

   private void flush_V_3() {
      if (!this.init_Z && this.init_Consumer != null) {
         this.init_Consumer.accept(new autismclient.util.g.IoWorker(this.init_IoWorker));
      }

      this.minecraft.gui.setScreen(this.init_Screen);
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
         int x = this.read_I_4();
         int panelW = this.write_I_4();
         int panelH = this.update_I_4();
         int text = ct.init_I_I_ct_DataUtil(ValueSink.flush, ct.Inner_DataUtil.read);
         int muted = ct.init_I_I_ct_DataUtil(ValueSink.close, ct.Inner_DataUtil.read);
         int panelY = this.sync_I_3();
         autismclient.gui.f.PacketUtil.load(
            graphics,
            autismclient.gui.f.NetHelper.init(x, panelY, panelW, panelH),
            6,
            ct.init_I_I_ct_DataUtil(-401402080, ct.Inner_DataUtil.close),
            ct.init_I_I_ct_DataUtil(ValueSink.update, ct.Inner_DataUtil.load)
         );
         this.init(graphics, "Advanced - gravity & packet canceller", x + 4, this.init(panelY + 4, close(), l.init), panelW - 8, text, l.init);
         int line = open();

         for (CharEncoder.Inner_DataUtil row : this.init_List) {
            int textW = panelW - 12 - this.init_I_Str_5("Off");
            this.init(graphics, row.init_Str(), x + 4, this.init(row.init_I(), line, l.read), textW, text);
            this.init(graphics, row.load(), x + 4, this.init(row.init_I() + line, line, l.read), textW, muted);
         }

         this.init(
            graphics, "Bots only, separate from the PacketCanceller module. Empty = allow all.", x + 4, this.init(this.pop(), line, l.read), panelW - 8, muted
         );
         super.extractRenderState(graphics, virtualMouseX, virtualMouseY, delta);
         if (this.init_AutismPacketSelect != null && this.init_AutismPacketSelect.sync_Z()) {
            this.init_AutismPacketSelect.init_V_GuiGraphicsExtract_15(graphics, virtualMouseX, virtualMouseY, delta);
         }
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   @Override
   public void tick() {
      super.tick();
      if (this.load_Z && (this.init_AutismPacketSelect == null || !this.init_AutismPacketSelect.sync_Z())) {
         this.load_Z = false;
         this.load_V_6();
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
   public void onClose() {
      this.flush_V_3();
   }

   private int init_I_Str_5(String label) {
      return autismclient.gui.f.RenderState.load(k.init(this.font, label, init_CharEncoder.init_Identifier_l(l.read), 0));
   }

   private StreamGate init(int x, int y, int w, int h, String text, autismclient.gui.f.b.CachePool.Inner_DataUtil tone, OnPress press) {
      String label = k.load(
         this.font,
         autismclient.util.g.PacketUtil.update(text, 64),
         Math.max(1, w - 8),
         init_CharEncoder.init_Identifier_l(l.read),
         ct.init_I_I_ct_DataUtil(ValueSink.flush, ct.Inner_DataUtil.read)
      );
      autismclient.gui.f.b.CachePool.Inner_DataUtil interactiveTone = tone == autismclient.gui.f.b.CachePool.Inner_DataUtil.init
         ? autismclient.gui.f.b.CachePool.Inner_DataUtil.load
         : tone;
      StreamGate button = new StreamGate(x, y, w, h, Component.literal(label), interactiveTone, press);
      this.addRenderableWidget(button);
      return button;
   }

   private static String load(String className) {
      Class<? extends Packet<?>> packet = bw.init_Class_Str(className);
      if (packet != null) {
         return bv.init_Str_Class(packet);
      }

      int dot = className.lastIndexOf(46);
      return dot < 0 ? className : className.substring(dot + 1);
   }

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int width, int color) {
      this.init(graphics, text, x, y, width, color, l.read);
   }

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int width, int color, l tone) {
      String safe = k.load(this.font, autismclient.util.g.PacketUtil.update(text, 96), Math.max(1, width), init_CharEncoder.init_Identifier_l(tone), color);
      k.init(graphics, this.font, safe, init_CharEncoder.init_Identifier_l(tone), color, x, y, false);
   }

   private record Inner_DataUtil(String init_Str, String load, int init_I) {
   }
}
