package autismclient.gui.d;

import autismclient.compat263.Compat263;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import autismclient.util.ProxyListManager;
import autismclient.util.cA;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ProxyConfigScreen extends VirtualScaledScreen {
   private static final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = 380;
   private static final int load_I = 8;
   private static final int read_I = 6;
   private static final int[] init_IArr = new int[]{1000, 1500, 2000, 2500, 3000, 5000, 7500, 10000, 15000};
   private static final int[] load_IArr = new int[]{1, 2, 4, 8, 12, 16, 24, 32, 48, 64, 96, 128, 192, 256};
   private static final int[] read_IArr = new int[]{0, 1, 2, 3, 5};
   private static final int[] write_IArr = new int[]{0, 500, 1000, 1500, 2000, 3000, 5000, 10000};
   private static final int[] update_IArr = new int[]{0, 25, 50, 100, 250, 500, 1000};
   private final Screen init_Screen;
   private final List<autismclient.gui.f.b.OverlayActionButton> init_List = new ArrayList<>();
   private final List<autismclient.gui.f.b.DropdownWidget> load_List = new ArrayList<>();
   private final List<ProxyConfigScreen.Inner_StreamUtil> read_List = new ArrayList<>();
   private int write_I;
   private int update_I = 380;
   private int sync;
   private int flush;

   public ProxyConfigScreen(Screen parent) {
      super(Component.literal("Proxy Config"));
      this.init_Screen = parent;
   }

   @Override
   protected void init() {
      this.load_V_7();
   }

   private void load_V_7() {
      this.init_List.clear();
      this.load_List.clear();
      this.read_List.clear();
      this.write_I = Math.max(0, Math.min(this.stop(), this.write_I));
      int panelX = this.close();
      int panelW = this.open();
      int y = this.add() - this.write_I;
      ProxyListManager mgr = ProxyListManager.init_bY();
      this.init("Timeout", "How long each proxy check can take.", mgr.read_I(), mgr::init_V_I, init_IArr, this::init_Str_I, panelX, panelW, y);
      y += this.flush();
      this.init("Threads", "How many proxies can be checked at once.", mgr.write_I(), mgr::load_V_I, load_IArr, Integer::toString, panelX, panelW, y);
      y += this.flush();
      this.init("Retries", "Extra attempts when a proxy check times out.", mgr.update_I(), mgr::read_V_I, read_IArr, Integer::toString, panelX, panelW, y);
      y += this.flush();
      this.init(
         "Prune latency",
         "Cleanup removes alive proxies slower than this.",
         mgr.sync_I(),
         mgr::write,
         write_IArr,
         value -> value <= 0 ? "Off" : this.init_Str_I(value),
         panelX,
         panelW,
         y
      );
      y += this.flush();
      this.init(
         "Prune limit",
         "Cleanup keeps only the fastest proxies when enabled.",
         mgr.flush_I(),
         mgr::update,
         update_IArr,
         value -> value <= 0 ? "No limit" : Integer.toString(value),
         panelX,
         panelW,
         y
      );
      y += this.flush();
      this.init("Sort by latency", mgr.load_Z(), "Refresh/cancel sorts alive proxies from fastest to slowest.", value -> mgr.init_V_Z(value), panelX, panelW, y);
      y += this.flush();
      this.init("Prune dead", mgr.read_Z(), "Cleanup removes proxies marked as dead.", value -> mgr.load_V_Z(value), panelX, panelW, y);
      y += this.flush();
      this.init("Location lookup", mgr.auditGeoLookup(), "Sends proxy IPs to api.country.is to show their country.", value -> mgr.auditSetGeoLookup(value), panelX, panelW, y);
      int wanted = this.poll();
      if (wanted != this.update_I) {
         this.update_I = wanted;
         this.load_V_7();
      } else {
         if (!this.init_Z_3()) {
            int ctl = this.write();
            int footerY = this.clear();
            int backWidth = this.init_I_Str_6("Back");
            this.init_List
               .add(
                  autismclient.gui.f.b.OverlayActionButton.init(
                        panelX + 4, footerY, backWidth, ctl, Component.literal("Back"), b -> this.minecraft.gui.setScreen(this.init_Screen)
                     )
                     .init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.SECONDARY)
               );
            int defaultsWidth = this.init_I_Str_6("Defaults");
            this.init_List
               .add(
                  autismclient.gui.f.b.OverlayActionButton.init(
                        panelX + panelW - 4 - defaultsWidth, footerY, defaultsWidth, ctl, Component.literal("Defaults"), b -> this.read_V_5()
                     )
                     .init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.SECONDARY)
               );
         }

         this.sync = this.init_I();
         this.flush = this.load_I();
      }
   }

   private void init(
      String label, String hint, int current, IntConsumer setter, int[] options, ProxyConfigScreen.Inner_CachePool formatter, int panelX, int panelW, int y
   ) {
      List<String> labels = this.init(options, formatter);
      int controlW = this.init_I_List_2(labels);
      this.read_List.add(new ProxyConfigScreen.Inner_StreamUtil(label, "", hint, y, false, false, controlW));
      if (this.init_Z_I_2(y)) {
         this.load_List
            .add(
               new autismclient.gui.f.b.DropdownWidget(
                  this.init(panelX, panelW, controlW), this.read(y), controlW, this.write(), labels, this.init(current, options), index -> {
                     if (index >= 0 && index < options.length) {
                        setter.accept(options[index]);
                        this.load_V_7();
                     }
                  }
               )
            );
      }
   }

   private void init(String label, boolean enabled, String hint, ProxyConfigScreen.Inner_DataUtil setter, int panelX, int panelW, int y) {
      int controlW = Math.max(this.init_I_Str_6("Enabled"), this.init_I_Str_6("Disabled"));
      this.read_List.add(new ProxyConfigScreen.Inner_StreamUtil(label, enabled ? "Enabled" : "Disabled", hint, y, true, enabled, controlW));
      if (this.init_Z_I_2(y)) {
         this.init_List
            .add(
               autismclient.gui.f.b.OverlayActionButton.init(
                     this.init(panelX, panelW, controlW), this.read(y), controlW, this.write(), Component.literal(enabled ? "Enabled" : "Disabled"), b -> {
                        setter.accept(!enabled);
                        this.load_V_7();
                     }
                  )
                  .init_ByteReader_ByteReader_StreamU(
                     enabled ? autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.SUCCESS : autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.DANGER
                  )
                  .init_ByteReader_Z(enabled)
                  .init_ByteReader_Str("proxy-config:" + label)
            );
      }
   }

   private void read_V_5() {
      ProxyListManager mgr = ProxyListManager.init_bY();
      mgr.init_V_I(3000);
      mgr.load_V_I(64);
      mgr.read_V_I(0);
      mgr.write(2000);
      mgr.update(0);
      mgr.init_V_Z(true);
      mgr.load_V_Z(true);
      mgr.auditSetGeoLookup(false);
      this.load_V_7();
   }

   private int init(int current, int[] options) {
      if (options.length == 0) {
         return current;
      }

      int index = 0;
      int bestDistance = 2147483647;

      for (int i = 0; i < options.length; i++) {
         int distance = Math.abs(options[i] - current);
         if (distance < bestDistance) {
            bestDistance = distance;
            index = i;
         }
      }

      return index;
   }

   private List<String> init(int[] options, ProxyConfigScreen.Inner_CachePool formatter) {
      List<String> labels = new ArrayList<>();

      for (int option : options) {
         labels.add(formatter.format(option));
      }

      return labels;
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      int virtualMouseX = cA.init_I_D(mouseX);
      int virtualMouseY = cA.init_I_D(mouseY);
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         if (this.sync != this.init_I() || this.flush != this.load_I()) {
            this.load_V_7();
         }

         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, this.init_I(), this.load_I()), -15987181);
         int panelX = this.close();
         int panelW = this.open();
         int panelTop = this.push();
         autismclient.gui.f.NetHelper panelBounds = autismclient.gui.f.NetHelper.init(panelX, panelTop, panelW, this.set() - panelTop);
         autismclient.gui.f.b.DialogFrameRenderer.init(
            autismclient.gui.f.UiHelper.init(graphics, this.font, virtualMouseX, virtualMouseY),
            panelBounds,
            this.read_I_5(),
            "Proxy Settings",
            panelBounds.init_Z(virtualMouseX, virtualMouseY)
         );
         autismclient.gui.f.b.SectionCardRenderer.init(
            autismclient.gui.f.UiHelper.init(graphics, this.font, virtualMouseX, virtualMouseY),
            autismclient.gui.f.NetHelper.init(panelX + 4, this.put(), Math.max(0, panelW - 8), Math.max(0, this.reset() + 4 - this.put())),
            -1206840035
         );
         boolean suppressUnderlyingPointer = autismclient.gui.f.b.DropdownWidget.init_Z();
         boolean dropdownMenuHovered = autismclient.gui.f.b.DropdownWidget.init(this.load_List, virtualMouseX, virtualMouseY);
         int buttonMouseX = !dropdownMenuHovered && !suppressUnderlyingPointer ? virtualMouseX : -2147483648;
         int buttonMouseY = !dropdownMenuHovered && !suppressUnderlyingPointer ? virtualMouseY : -2147483648;
         if (this.init_Z_3()) {
            this.init(graphics, "Window too small.", this.init_I_I_5(panelX), this.init_I_2(this.put(), 12), GuiColorPalette.close, false, Math.max(0, panelW - 16));
            return;
         }

         for (ProxyConfigScreen.Inner_StreamUtil row : this.read_List) {
            this.init(graphics, row, panelX, panelW);
         }

         for (autismclient.gui.f.b.OverlayActionButton button : this.init_List) {
            autismclient.gui.f.b.OverlayActionButton.init(graphics, this.font, button, buttonMouseX, buttonMouseY);
         }

         autismclient.gui.f.b.DropdownWidget.load(graphics, this.font, this.load_List, virtualMouseX, virtualMouseY);
         if (autismclient.gui.f.b.DropdownWidget.init_Z_List(this.load_List)) {
            autismclient.gui.f.b.DropdownWidget.read(graphics, this.font, this.load_List, virtualMouseX, virtualMouseY);
         }
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void init(GuiGraphicsExtractor graphics, ProxyConfigScreen.Inner_StreamUtil row, int panelX, int panelW) {
      if (this.init_Z_I_2(row.init_I())) {
         int x = this.init_I_I_5(panelX);
         int y = row.init_I();
         int w = this.load(panelW);
         int h = this.sync();
         int line = this.update();
         int fill = row.init_Z() && row.load_Z() ? 571483674 : 403771667;
         autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(x, y, w, h), 4, fill);
         if (row.init_Z() && row.load_Z()) {
            autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(x, y + 2, 2, h - 4), 1, GuiColorPalette.open);
         }

         int textMaxWidth = Math.max(1, this.init(panelX, panelW, row.load_I()) - 4 - (x + 6));
         this.init(graphics, row.init_Str(), x + 6, this.init_I_2(y, line), GuiColorPalette.flush, false, textMaxWidth);
         this.init(graphics, row.read(), x + 6, this.init_I_2(y + line, line), GuiColorPalette.close, false, textMaxWidth);
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      MouseButtonEvent virtualEvent = init_MouseButtonEvent_MouseButtonEvent(event);
      if (Compat263.mouseEventGlfwButton(virtualEvent) == 0
         && autismclient.gui.f.b.DialogFrameRenderer.init(
            autismclient.gui.f.NetHelper.init(this.close(), this.push(), this.open(), this.set() - this.push()),
            this.read_I_5(),
            (int)virtualEvent.x(),
            (int)virtualEvent.y()
         )) {
         this.minecraft.gui.setScreen(this.init_Screen);
         return true;
      }

      if (autismclient.gui.f.b.DropdownWidget.init_Z_List_2(this.load_List, virtualEvent.x(), virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent))) {
         return true;
      }

      if (Compat263.mouseEventGlfwButton(virtualEvent) != 0) {
         return super.mouseClicked(virtualEvent, doubleClick);
      }

      for (autismclient.gui.f.b.OverlayActionButton button : this.init_List) {
         if (autismclient.gui.f.b.OverlayActionButton.init(button, virtualEvent.x(), virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent))) {
            return true;
         }
      }

      return super.mouseClicked(virtualEvent, doubleClick);
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      double virtualX = cA.init_D_D(mouseX);
      double virtualY = cA.init_D_D(mouseY);
      if (autismclient.gui.f.b.DropdownWidget.init_Z_List(this.load_List, virtualX, virtualY, scrollY)) {
         return true;
      } else if (!(virtualX < this.close()) && !(virtualX > this.close() + this.open()) && !(virtualY < this.add()) && !(virtualY > this.reset())) {
         this.write_I = Math.max(0, Math.min(this.stop(), this.write_I + (scrollY < 0.0 ? this.flush() : -this.flush())));
         this.load_V_7();
         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
      }
   }

   @Override
   public void onClose() {
      this.minecraft.gui.setScreen(this.init_Screen);
   }

   private int read_I_5() {
      return autismclient.gui.f.UiDensityMetrics.init(14);
   }

   private int write() {
      return autismclient.gui.f.UiDensityMetrics.init(13);
   }

   private int update() {
      return autismclient.gui.f.UiDensityMetrics.init(12);
   }

   private int sync() {
      return this.update() * 2;
   }

   private int flush() {
      return this.sync() + 2;
   }

   private int init_I_I_5(int panelX) {
      return panelX + 8;
   }

   private int load(int panelW) {
      return panelW - 16;
   }

   private int init(int panelX, int panelW, int controlW) {
      return this.init_I_I_5(panelX) + this.load(panelW) - 4 - controlW;
   }

   private int read(int rowY) {
      return rowY + (this.sync() - this.write()) / 2;
   }

   private int init_I_2(int top, int height) {
      return autismclient.gui.f.UiHelper.init(this.font).init_I(autismclient.gui.f.NetHelper.init(0, top, 1, height));
   }

   private int close() {
      return autismclient.gui.f.c.PanelLayoutMath.load(this.init_I(), this.open(), 6);
   }

   private int open() {
      return autismclient.gui.f.c.PanelLayoutMath.init(this.init_I(), 6, Math.min(380, this.update_I));
   }

   private int poll() {
      int text = 0;
      int control = 0;

      for (ProxyConfigScreen.Inner_StreamUtil row : this.read_List) {
         text = Math.max(text, Math.max(this.load(row.init_Str()), this.load(row.read())));
         control = Math.max(control, row.load_I());
      }

      return 14 + text + 4 + control + 4 + 8;
   }

   private int push() {
      return 6;
   }

   private int pop() {
      return this.read_I_5() + 4 + 4 + 4 + 4 + this.write() + 4;
   }

   private int tick_I_6() {
      int available = this.load_I() - 12 - this.pop();
      return Math.max(0, Math.min(8, (available + 2) / this.flush()));
   }

   private int run() {
      int shown = this.tick_I_6();
      return shown <= 0 ? 0 : shown * this.flush() - 2;
   }

   private int get() {
      return autismclient.gui.f.c.PanelLayoutMath.init(this.load_I(), 6, this.pop() + this.run());
   }

   private int set() {
      return this.push() + this.get();
   }

   private int put() {
      return this.push() + this.read_I_5() + 4;
   }

   private int add() {
      return this.put() + 4;
   }

   private int clear() {
      return this.set() - 4 - this.write();
   }

   private int reset() {
      return this.clear() - 4 - 4;
   }

   private int start() {
      return 8 * this.flush() - 2;
   }

   private int stop() {
      return Math.max(0, this.start() - Math.max(0, this.reset() - this.add()));
   }

   private boolean init_Z_I_2(int y) {
      return y >= this.add() && y + this.sync() <= this.reset();
   }

   private boolean init_Z_3() {
      return this.open() < 220 || this.reset() - this.add() < this.sync();
   }

   private int init_I_Str_6(String label) {
      return autismclient.gui.f.UiDensityMetrics.load(k.init(this.font, label, init_CharEncoder.init_Identifier_l(l.read), 0));
   }

   private int load(String text) {
      return k.init(this.font, text, init_CharEncoder.init_Identifier_l(l.read), 0);
   }

   private int init_I_List_2(List<String> options) {
      int widest = 0;

      for (String option : options) {
         widest = Math.max(widest, k.init(this.font, option, init_CharEncoder.init_Identifier_l(l.read), 0));
      }

      return Math.max(56, widest + 26);
   }

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int color, boolean right) {
      this.init(graphics, text, x, y, color, right, 2147483647);
   }

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int color, boolean right, int maxWidth) {
      Font renderer = this.font;
      Identifier font = init_CharEncoder.init_Identifier_l(l.read);
      if (maxWidth != 2147483647 && !right) {
         k.init(graphics, renderer, text, font, color, x, y, Math.max(1, maxWidth), false);
      } else {
         String display = maxWidth == 2147483647 ? text : k.init(renderer, text, maxWidth, font, color);
         int w = k.init(renderer, display, font, color);
         int drawX = right ? x - w : x;
         k.init(graphics, renderer, display, font, color, drawX, y, false);
      }
   }

   private String init_Str_I(int value) {
      return value % 1000 == 0 ? value / 1000 + "s" : value / 1000.0 + "s";
   }

   @FunctionalInterface
   private interface Inner_CachePool {
      String format(int var1);
   }

   @FunctionalInterface
   private interface Inner_DataUtil {
      void accept(boolean var1);
   }

   private record Inner_StreamUtil(String init_Str, String load_Str, String read, int init_I, boolean init_Z, boolean load_Z, int load_I) {
   }
}
