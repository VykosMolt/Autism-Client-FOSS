package autismclient.gui.screens;

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
   private static final autismclient.gui.f.b.CharEncoder THEME = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = 380;
   private static final int load_I = 8;
   private static final int read_I = 6;
   private static final int[] TIMEOUT_CHOICES = new int[]{1000, 1500, 2000, 2500, 3000, 5000, 7500, 10000, 15000};
   private static final int[] THREAD_CHOICES = new int[]{1, 2, 4, 8, 12, 16, 24, 32, 48, 64, 96, 128, 192, 256};
   private static final int[] RETRY_CHOICES = new int[]{0, 1, 2, 3, 5};
   private static final int[] PRUNE_LATENCY_CHOICES = new int[]{0, 500, 1000, 1500, 2000, 3000, 5000, 10000};
   private static final int[] PRUNE_LIMIT_CHOICES = new int[]{0, 25, 50, 100, 250, 500, 1000};
   private final Screen parent;
   private final List<autismclient.gui.ui.widgets.OverlayActionButton> buttons = new ArrayList<>();
   private final List<autismclient.gui.ui.widgets.DropdownWidget> dropdowns = new ArrayList<>();
   private final List<ProxyConfigScreen.SettingRow> rows = new ArrayList<>();
   private int scroll;
   private int panelWidth = 380;
   private int lastWidth;
   private int lastHeight;

   public ProxyConfigScreen(Screen parent) {
      super(Component.literal("Proxy Config"));
      this.parent = parent;
   }

   @Override
   protected void init() {
      this.rebuildProxyWidgets();
   }

   private void rebuildProxyWidgets() {
      this.buttons.clear();
      this.dropdowns.clear();
      this.rows.clear();
      this.scroll = Math.max(0, Math.min(this.maxScroll(), this.scroll));
      int panelX = this.panelX();
      int panelW = this.panelWidth();
      int y = this.rowsTop() - this.scroll;
      ProxyListManager mgr = ProxyListManager.getInstance();
      this.addChoiceRow("Timeout", "How long each proxy check can take.", mgr.getTimeoutMs(), mgr::setTimeoutMs, TIMEOUT_CHOICES, this::formatSeconds, panelX, panelW, y);
      y += this.rowPitch();
      this.addChoiceRow("Threads", "How many proxies can be checked at once.", mgr.getWorkerThreads(), mgr::setWorkerThreads, THREAD_CHOICES, Integer::toString, panelX, panelW, y);
      y += this.rowPitch();
      this.addChoiceRow("Retries", "Extra attempts when a proxy check times out.", mgr.getRetryCount(), mgr::setRetryCount, RETRY_CHOICES, Integer::toString, panelX, panelW, y);
      y += this.rowPitch();
      this.addChoiceRow(
         "Prune latency",
         "Cleanup removes alive proxies slower than this.",
         mgr.getPruneLatencyMs(),
         mgr::setPruneLatencyMs,
         PRUNE_LATENCY_CHOICES,
         value -> value <= 0 ? "Off" : this.formatSeconds(value),
         panelX,
         panelW,
         y
      );
      y += this.rowPitch();
      this.addChoiceRow(
         "Prune limit",
         "Cleanup keeps only the fastest proxies when enabled.",
         mgr.getPruneToCount(),
         mgr::setPruneToCount,
         PRUNE_LIMIT_CHOICES,
         value -> value <= 0 ? "No limit" : Integer.toString(value),
         panelX,
         panelW,
         y
      );
      y += this.rowPitch();
      this.addToggleRow("Sort by latency", mgr.isSortByLatency(), "Refresh/cancel sorts alive proxies from fastest to slowest.", value -> mgr.setSortByLatency(value), panelX, panelW, y);
      y += this.rowPitch();
      this.addToggleRow("Prune dead", mgr.isPruneDead(), "Cleanup removes proxies marked as dead.", value -> mgr.setPruneDead(value), panelX, panelW, y);
      y += this.rowPitch();
      this.addToggleRow("Location lookup", mgr.auditGeoLookup(), "Sends proxy IPs to api.country.is to show their country.", value -> mgr.auditSetGeoLookup(value), panelX, panelW, y);
      int wanted = this.wantedWidth();
      if (wanted != this.panelWidth) {
         this.panelWidth = wanted;
         this.rebuildProxyWidgets();
      } else {
         if (!this.isCompactLayout()) {
            int ctl = this.controlHeight();
            int footerY = this.footerY();
            int backWidth = this.buttonWidthFor("Back");
            this.buttons
               .add(
                  autismclient.gui.ui.widgets.OverlayActionButton.create(
                        panelX + 4, footerY, backWidth, ctl, Component.literal("Back"), b -> this.minecraft.gui.setScreen(this.parent)
                     )
                     .setTone(autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY)
               );
            int defaultsWidth = this.buttonWidthFor("Defaults");
            this.buttons
               .add(
                  autismclient.gui.ui.widgets.OverlayActionButton.create(
                        panelX + panelW - 4 - defaultsWidth, footerY, defaultsWidth, ctl, Component.literal("Defaults"), b -> this.resetDefaults()
                     )
                     .setTone(autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY)
               );
         }

         this.lastWidth = this.virtualWidth();
         this.lastHeight = this.virtualHeight();
      }
   }

   private void addChoiceRow(
      String label, String hint, int current, IntConsumer setter, int[] options, ProxyConfigScreen.IntFormatter formatter, int panelX, int panelW, int y
   ) {
      List<String> labels = this.init(options, formatter);
      int controlW = this.widestOption(labels);
      this.rows.add(new ProxyConfigScreen.SettingRow(label, "", hint, y, false, false, controlW));
      if (this.isRowVisible(y)) {
         this.dropdowns
            .add(
               new autismclient.gui.ui.widgets.DropdownWidget(
                  this.controlX(panelX, panelW, controlW), this.controlY(y), controlW, this.controlHeight(), labels, this.init(current, options), index -> {
                     if (index >= 0 && index < options.length) {
                        setter.accept(options[index]);
                        this.rebuildProxyWidgets();
                     }
                  }
               )
            );
      }
   }

   private void addToggleRow(String label, boolean enabled, String hint, ProxyConfigScreen.BoolSetter setter, int panelX, int panelW, int y) {
      int controlW = Math.max(this.buttonWidthFor("Enabled"), this.buttonWidthFor("Disabled"));
      this.rows.add(new ProxyConfigScreen.SettingRow(label, enabled ? "Enabled" : "Disabled", hint, y, true, enabled, controlW));
      if (this.isRowVisible(y)) {
         this.buttons
            .add(
               autismclient.gui.ui.widgets.OverlayActionButton.create(
                     this.controlX(panelX, panelW, controlW), this.controlY(y), controlW, this.controlHeight(), Component.literal(enabled ? "Enabled" : "Disabled"), b -> {
                        setter.accept(!enabled);
                        this.rebuildProxyWidgets();
                     }
                  )
                  .setTone(
                     enabled ? autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.DANGER
                  )
                  .setToggleState(enabled)
                  .setAnimationKey("proxy-config:" + label)
            );
      }
   }

   private void resetDefaults() {
      ProxyListManager mgr = ProxyListManager.getInstance();
      mgr.setTimeoutMs(3000);
      mgr.setWorkerThreads(64);
      mgr.setRetryCount(0);
      mgr.setPruneLatencyMs(2000);
      mgr.setPruneToCount(0);
      mgr.setSortByLatency(true);
      mgr.setPruneDead(true);
      mgr.auditSetGeoLookup(false);
      this.rebuildProxyWidgets();
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

   private List<String> init(int[] options, ProxyConfigScreen.IntFormatter formatter) {
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
         if (this.lastWidth != this.virtualWidth() || this.lastHeight != this.virtualHeight()) {
            this.rebuildProxyWidgets();
         }

         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, this.virtualWidth(), this.virtualHeight()), -15987181);
         int panelX = this.panelX();
         int panelW = this.panelWidth();
         int panelTop = this.panelTop();
         autismclient.gui.f.NetHelper panelBounds = autismclient.gui.f.NetHelper.init(panelX, panelTop, panelW, this.panelBottom() - panelTop);
         autismclient.gui.ui.widgets.DialogFrameRenderer.draw(
            autismclient.gui.f.UiHelper.init(graphics, this.font, virtualMouseX, virtualMouseY),
            panelBounds,
            this.read_I_5(),
            "Proxy Settings",
            panelBounds.init_Z(virtualMouseX, virtualMouseY)
         );
         autismclient.gui.ui.widgets.SectionCardRenderer.drawPlain(
            autismclient.gui.f.UiHelper.init(graphics, this.font, virtualMouseX, virtualMouseY),
            autismclient.gui.f.NetHelper.init(panelX + 4, this.listTop(), Math.max(0, panelW - 8), Math.max(0, this.rowsBottom() + 4 - this.listTop())),
            -1206840035
         );
         boolean suppressUnderlyingPointer = autismclient.gui.ui.widgets.DropdownWidget.isAnyMenuActive();
         boolean dropdownMenuHovered = autismclient.gui.ui.widgets.DropdownWidget.isPointInAnyPopup(this.dropdowns, virtualMouseX, virtualMouseY);
         int buttonMouseX = !dropdownMenuHovered && !suppressUnderlyingPointer ? virtualMouseX : -2147483648;
         int buttonMouseY = !dropdownMenuHovered && !suppressUnderlyingPointer ? virtualMouseY : -2147483648;
         if (this.isCompactLayout()) {
            this.drawText(graphics, "Window too small.", this.contentX(panelX), this.centerTextY(this.listTop(), 12), GuiColorPalette.close, false, Math.max(0, panelW - 16));
            return;
         }

         for (ProxyConfigScreen.SettingRow row : this.rows) {
            this.renderRow(graphics, row, panelX, panelW);
         }

         for (autismclient.gui.ui.widgets.OverlayActionButton button : this.buttons) {
            autismclient.gui.ui.widgets.OverlayActionButton.render(graphics, this.font, button, buttonMouseX, buttonMouseY);
         }

         autismclient.gui.ui.widgets.DropdownWidget.renderTriggers(graphics, this.font, this.dropdowns, virtualMouseX, virtualMouseY);
         if (autismclient.gui.ui.widgets.DropdownWidget.hasOpenMenu(this.dropdowns)) {
            autismclient.gui.ui.widgets.DropdownWidget.renderOpenPopup(graphics, this.font, this.dropdowns, virtualMouseX, virtualMouseY);
         }
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void renderRow(GuiGraphicsExtractor graphics, ProxyConfigScreen.SettingRow row, int panelX, int panelW) {
      if (this.isRowVisible(row.y())) {
         int x = this.contentX(panelX);
         int y = row.y();
         int w = this.rowWidth(panelW);
         int h = this.rowHeight();
         int line = this.lineHeight();
         int fill = row.toggle() && row.enabled() ? 571483674 : 403771667;
         autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(x, y, w, h), 4, fill);
         if (row.toggle() && row.enabled()) {
            autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(x, y + 2, 2, h - 4), 1, GuiColorPalette.open);
         }

         int textMaxWidth = Math.max(1, this.controlX(panelX, panelW, row.controlWidth()) - 4 - (x + 6));
         this.drawText(graphics, row.label(), x + 6, this.centerTextY(y, line), GuiColorPalette.flush, false, textMaxWidth);
         this.drawText(graphics, row.hint(), x + 6, this.centerTextY(y + line, line), GuiColorPalette.close, false, textMaxWidth);
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      MouseButtonEvent virtualEvent = toVirtual(event);
      if (Compat263.mouseEventGlfwButton(virtualEvent) == 0
         && autismclient.gui.ui.widgets.DialogFrameRenderer.isOverHeader(
            autismclient.gui.f.NetHelper.init(this.panelX(), this.panelTop(), this.panelWidth(), this.panelBottom() - this.panelTop()),
            this.read_I_5(),
            (int)virtualEvent.x(),
            (int)virtualEvent.y()
         )) {
         this.minecraft.gui.setScreen(this.parent);
         return true;
      }

      if (autismclient.gui.ui.widgets.DropdownWidget.handleMouseClick(this.dropdowns, virtualEvent.x(), virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent))) {
         return true;
      }

      if (Compat263.mouseEventGlfwButton(virtualEvent) != 0) {
         return super.mouseClicked(virtualEvent, doubleClick);
      }

      for (autismclient.gui.ui.widgets.OverlayActionButton button : this.buttons) {
         if (autismclient.gui.ui.widgets.OverlayActionButton.handleClick(button, virtualEvent.x(), virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent))) {
            return true;
         }
      }

      return super.mouseClicked(virtualEvent, doubleClick);
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      double virtualX = cA.init_D_D(mouseX);
      double virtualY = cA.init_D_D(mouseY);
      if (autismclient.gui.ui.widgets.DropdownWidget.handleMouseScroll(this.dropdowns, virtualX, virtualY, scrollY)) {
         return true;
      } else if (!(virtualX < this.panelX()) && !(virtualX > this.panelX() + this.panelWidth()) && !(virtualY < this.rowsTop()) && !(virtualY > this.rowsBottom())) {
         this.scroll = Math.max(0, Math.min(this.maxScroll(), this.scroll + (scrollY < 0.0 ? this.rowPitch() : -this.rowPitch())));
         this.rebuildProxyWidgets();
         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
      }
   }

   @Override
   public void onClose() {
      this.minecraft.gui.setScreen(this.parent);
   }

   private int read_I_5() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int controlHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int lineHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(12);
   }

   private int rowHeight() {
      return this.lineHeight() * 2;
   }

   private int rowPitch() {
      return this.rowHeight() + 2;
   }

   private int contentX(int panelX) {
      return panelX + 8;
   }

   private int rowWidth(int panelW) {
      return panelW - 16;
   }

   private int controlX(int panelX, int panelW, int controlW) {
      return this.contentX(panelX) + this.rowWidth(panelW) - 4 - controlW;
   }

   private int controlY(int rowY) {
      return rowY + (this.rowHeight() - this.controlHeight()) / 2;
   }

   private int centerTextY(int top, int height) {
      return autismclient.gui.f.UiHelper.init(this.font).centerTextY(autismclient.gui.f.NetHelper.init(0, top, 1, height));
   }

   private int panelX() {
      return autismclient.gui.ui.layout.PanelLayoutMath.centerWithMargin(this.virtualWidth(), this.panelWidth(), 6);
   }

   private int panelWidth() {
      return autismclient.gui.ui.layout.PanelLayoutMath.clampSizeToMargin(this.virtualWidth(), 6, Math.min(380, this.panelWidth));
   }

   private int wantedWidth() {
      int text = 0;
      int control = 0;

      for (ProxyConfigScreen.SettingRow row : this.rows) {
         text = Math.max(text, Math.max(this.textWidth(row.label()), this.textWidth(row.hint())));
         control = Math.max(control, row.controlWidth());
      }

      return 14 + text + 4 + control + 4 + 8;
   }

   private int panelTop() {
      return 6;
   }

   private int chromeHeight() {
      return this.read_I_5() + 4 + 4 + 4 + 4 + this.controlHeight() + 4;
   }

   private int visibleRowCount() {
      int available = this.virtualHeight() - 12 - this.chromeHeight();
      return Math.max(0, Math.min(8, (available + 2) / this.rowPitch()));
   }

   private int rowsHeight() {
      int shown = this.visibleRowCount();
      return shown <= 0 ? 0 : shown * this.rowPitch() - 2;
   }

   private int panelHeight() {
      return autismclient.gui.ui.layout.PanelLayoutMath.clampSizeToMargin(this.virtualHeight(), 6, this.chromeHeight() + this.rowsHeight());
   }

   private int panelBottom() {
      return this.panelTop() + this.panelHeight();
   }

   private int listTop() {
      return this.panelTop() + this.read_I_5() + 4;
   }

   private int rowsTop() {
      return this.listTop() + 4;
   }

   private int footerY() {
      return this.panelBottom() - 4 - this.controlHeight();
   }

   private int rowsBottom() {
      return this.footerY() - 4 - 4;
   }

   private int contentHeight() {
      return 8 * this.rowPitch() - 2;
   }

   private int maxScroll() {
      return Math.max(0, this.contentHeight() - Math.max(0, this.rowsBottom() - this.rowsTop()));
   }

   private boolean isRowVisible(int y) {
      return y >= this.rowsTop() && y + this.rowHeight() <= this.rowsBottom();
   }

   private boolean isCompactLayout() {
      return this.panelWidth() < 220 || this.rowsBottom() - this.rowsTop() < this.rowHeight();
   }

   private int buttonWidthFor(String label) {
      return autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(k.init(this.font, label, THEME.init_Identifier_l(l.read), 0));
   }

   private int textWidth(String text) {
      return k.init(this.font, text, THEME.init_Identifier_l(l.read), 0);
   }

   private int widestOption(List<String> options) {
      int widest = 0;

      for (String option : options) {
         widest = Math.max(widest, k.init(this.font, option, THEME.init_Identifier_l(l.read), 0));
      }

      return Math.max(56, widest + 26);
   }

   private void drawText(GuiGraphicsExtractor graphics, String text, int x, int y, int color, boolean right) {
      this.drawText(graphics, text, x, y, color, right, 2147483647);
   }

   private void drawText(GuiGraphicsExtractor graphics, String text, int x, int y, int color, boolean right, int maxWidth) {
      Font renderer = this.font;
      Identifier font = THEME.init_Identifier_l(l.read);
      if (maxWidth != 2147483647 && !right) {
         k.init(graphics, renderer, text, font, color, x, y, Math.max(1, maxWidth), false);
      } else {
         String display = maxWidth == 2147483647 ? text : k.init(renderer, text, maxWidth, font, color);
         int w = k.init(renderer, display, font, color);
         int drawX = right ? x - w : x;
         k.init(graphics, renderer, display, font, color, drawX, y, false);
      }
   }

   private String formatSeconds(int value) {
      return value % 1000 == 0 ? value / 1000 + "s" : value / 1000.0 + "s";
   }

   @FunctionalInterface
   private interface IntFormatter {
      String format(int var1);
   }

   @FunctionalInterface
   private interface BoolSetter {
      void accept(boolean var1);
   }

   private record SettingRow(String label, String value, String hint, int y, boolean toggle, boolean enabled, int controlWidth) {
   }
}
