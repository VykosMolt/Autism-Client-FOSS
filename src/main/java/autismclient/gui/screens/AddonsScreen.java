package autismclient.gui.screens;

import autismclient.compat263.Compat263;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import autismclient.util.bi;
import autismclient.util.cA;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public final class AddonsScreen extends VirtualScaledScreen {
   private static final autismclient.gui.f.b.CharEncoder THEME = new autismclient.gui.f.b.CharEncoder();
   private static final int API_TEXT_COLOR = -7429889;
   private static final int MAX_PANEL_WIDTH = 560;
   private static final int read_I = 3;
   private static final String[] DETAIL_LABELS = new String[]{"Status", "API", "Version", "Authors", "Extensions", "Rejected", "Runtime Errors"};
   private final Screen parent;
   private final List<autismclient.gui.ui.widgets.OverlayActionButton> buttons = new ArrayList<>();
   private final autismclient.gui.ui.widgets.ScrollOffsetController scroll = new autismclient.gui.ui.widgets.ScrollOffsetController();
   private EditBox searchBox;
   private String appliedQuery = "";
   private String pendingQuery = "";
   private boolean queryChanged;
   private String selectedAddonId = "";
   private int scrollOffset;
   private boolean draggingScrollbar;
   private int lastBodyBottom = -2147483648;
   private int scrollGrabOffset;
   private List<autismclient.b.DataUtil.StreamUtil> allReports = List.of();
   private String cachedQuery = "";
   private List<autismclient.b.DataUtil.StreamUtil> filteredReports = List.of();

   public AddonsScreen(Screen parent) {
      super(Component.literal("Addons"));
      this.parent = parent;
   }

   @Override
   protected void init() {
      this.allReports = List.copyOf(autismclient.b.DataUtil.load_List());
      this.cachedQuery = "\u0000";
      this.filteredReports = List.of();
      this.searchBox = new MsdfEditBox(this.font, this.listCardX() + 4, this.searchY(), this.listCardWidth() - 8, this.controlHeight(), Component.literal("Search addons"));
      this.searchBox.setHint(Component.literal("Search addons..."));
      this.searchBox.setMaxLength(96);
      this.searchBox.setResponder(value -> {
         this.pendingQuery = trim(value);
         this.queryChanged = true;
      });
      this.addRenderableWidget(this.searchBox);
      this.ensureSelection();
      this.rebuildButtons();
   }

   private void rebuildButtons() {
      this.buttons.clear();
      this.lastBodyBottom = this.bodyBottom();
      int ctl = this.controlHeight();
      this.buttons
         .add(
            autismclient.gui.ui.widgets.OverlayActionButton.create(
                  this.backButtonX(), this.headerControlsY(), this.backButtonWidth(), ctl, Component.literal("Back"), b -> this.minecraft.gui.setScreen(this.parent)
               )
               .setTone(autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY)
         );
      autismclient.b.DataUtil.StreamUtil selected = this.selectedReport();
      int gap = 2;
      int btnLeft = this.detailCardX() + 4;
      int areaW = this.detailInnerWidth();
      int row2Y = this.bodyBottom() - 4 - ctl;
      int row1Y = row2Y - ctl - gap;
      boolean disabledRestart = selected != null && autismclient.b.DataUtil.read(selected.init_Str());
      String toggleLabel = restartToggleLabel(selected);
      int copyIdW = this.buttonWidth("Copy ID");
      int copyReportW = this.buttonWidth("Copy Report");
      int toggleW = this.buttonWidth(toggleLabel);
      int modsFolderW = this.buttonWidth("Mods Folder");
      autismclient.gui.ui.widgets.OverlayActionButton toggle;
      if (this.buttonsFitOneRow(selected)) {
         int x = btnLeft;
         int y = row2Y;
         this.buttons
            .add(
               autismclient.gui.ui.widgets.OverlayActionButton.create(x, y, copyIdW, ctl, Component.literal("Copy ID"), b -> this.copySelectedId())
                  .setTone(autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY)
            );
         x += copyIdW + gap;
         this.buttons
            .add(
               autismclient.gui.ui.widgets.OverlayActionButton.create(x, y, copyReportW, ctl, Component.literal("Copy Report"), b -> this.copySelectedReport())
                  .setTone(autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY)
            );
         x += copyReportW + gap;
         toggle = autismclient.gui.ui.widgets.OverlayActionButton.create(x, y, toggleW, ctl, Component.literal(toggleLabel), b -> this.toggleSelectedRestart());
         x += toggleW + gap;
         this.buttons
            .add(
               autismclient.gui.ui.widgets.OverlayActionButton.create(x, y, modsFolderW, ctl, Component.literal("Mods Folder"), b -> this.openModsFolder())
                  .setTone(autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY)
            );
      } else {
         int halfW = (areaW - gap) / 2;
         int rightW = areaW - halfW - gap;
         int rightX = btnLeft + halfW + gap;
         boolean row1Fits = copyIdW + copyReportW + gap <= areaW;
         boolean row2Fits = toggleW + modsFolderW + gap <= areaW;
         this.buttons
            .add(
               autismclient.gui.ui.widgets.OverlayActionButton.create(btnLeft, row1Y, row1Fits ? copyIdW : halfW, ctl, Component.literal("Copy ID"), b -> this.copySelectedId())
                  .setTone(autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY)
            );
         this.buttons
            .add(
               autismclient.gui.ui.widgets.OverlayActionButton.create(
                     row1Fits ? btnLeft + copyIdW + gap : rightX,
                     row1Y,
                     row1Fits ? copyReportW : rightW,
                     ctl,
                     Component.literal("Copy Report"),
                     b -> this.copySelectedReport()
                  )
                  .setTone(autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY)
            );
         toggle = autismclient.gui.ui.widgets.OverlayActionButton.create(btnLeft, row2Y, row2Fits ? toggleW : halfW, ctl, Component.literal(toggleLabel), b -> this.toggleSelectedRestart());
         this.buttons
            .add(
               autismclient.gui.ui.widgets.OverlayActionButton.create(
                     row2Fits ? btnLeft + toggleW + gap : rightX,
                     row2Y,
                     row2Fits ? modsFolderW : rightW,
                     ctl,
                     Component.literal("Mods Folder"),
                     b -> this.openModsFolder()
                  )
                  .setTone(autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY)
            );
      }

      toggle.setTone(
         disabledRestart ? autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.DANGER
      );
      toggle.active = selected != null;
      this.buttons.add(toggle);
   }

   private static String restartToggleLabel(autismclient.b.DataUtil.StreamUtil selected) {
      return selected != null && autismclient.b.DataUtil.read(selected.init_Str()) ? "Enable Restart" : "Disable Restart";
   }

   private boolean buttonsFitOneRow(autismclient.b.DataUtil.StreamUtil selected) {
      return this.buttonWidth("Copy ID")
            + this.buttonWidth("Copy Report")
            + this.buttonWidth(restartToggleLabel(selected))
            + this.buttonWidth("Mods Folder")
            + 6
         <= this.detailInnerWidth();
   }

   private int buttonRowsHeight(autismclient.b.DataUtil.StreamUtil selected) {
      int ctl = this.controlHeight();
      return this.buttonsFitOneRow(selected) ? ctl : ctl * 2 + 2;
   }

   private int buttonWidth(String label) {
      return autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(k.init(this.font, label, THEME.init_Identifier_l(l.read), 0));
   }

   private int backButtonWidth() {
      return this.buttonWidth("Back");
   }

   private int backButtonX() {
      return this.cardX() + 4;
   }

   private void ensureSelection() {
      List<autismclient.b.DataUtil.StreamUtil> reports = this.visibleReports();
      if (reports.isEmpty()) {
         this.selectedAddonId = "";
      } else {
         for (autismclient.b.DataUtil.StreamUtil report : reports) {
            if (report.init_Str().equals(this.selectedAddonId)) {
               return;
            }
         }

         this.selectedAddonId = reports.get(0).init_Str();
      }
   }

   @Override
   public void tick() {
      super.tick();
      if (this.queryChanged) {
         this.queryChanged = false;
         this.appliedQuery = this.pendingQuery;
         this.scrollOffset = 0;
         this.scroll.jumpTo(0, 0);
         this.ensureSelection();
         this.rebuildButtons();
      }

      this.scroll.tickAndClamp(1.0F, 1);
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      int mx = cA.init_I_D(mouseX);
      int my = cA.init_I_D(mouseY);
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         if (this.lastBodyBottom != this.bodyBottom()) {
            this.rebuildButtons();
         }

         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, this.virtualWidth(), this.virtualHeight()), -15987181);
         this.drawCard(graphics, this.cardX(), this.cardY(), this.cardWidth(), this.headerCardHeight(), -401402080);
         this.drawCard(graphics, this.listCardX(), this.bodyY(), this.listCardWidth(), this.bodyHeight(), -1206840035);
         this.drawCard(graphics, this.detailCardX(), this.bodyY(), this.detailCardWidth(), this.bodyHeight(), -1206840035);
         int ctl = this.controlHeight();
         int titleX = this.backButtonX() + this.backButtonWidth() + 4;
         int titleY = this.centeredTextY(this.headerControlsY(), ctl);
         this.drawText(graphics, "Addons", titleX, titleY, GuiColorPalette.flush, false);
         int summaryLeft = titleX + this.textWidth("Addons") + 8;
         this.drawTextRight(
            graphics, this.summaryText(), this.cardX() + this.cardWidth() - 4, titleY, GuiColorPalette.close, Math.max(1, this.cardX() + this.cardWidth() - 4 - summaryLeft)
         );
         this.drawTextClipped(graphics, "Addon Library", this.listCardX() + 4, this.centeredTextY(this.bodyY() + 4, 12), GuiColorPalette.flush, false, this.listCardWidth() - 8);
         this.renderList(graphics, mx, my);
         this.renderDetails(graphics);

         for (autismclient.gui.ui.widgets.OverlayActionButton button : this.buttons) {
            autismclient.gui.ui.widgets.OverlayActionButton.render(graphics, this.font, button, mx, my);
         }

         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbar = this.scrollbarMetrics(this.visibleReports().size());
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.render(graphics, scrollbar, scrollbar.containsTrack(mx, my), this.draggingScrollbar);
         super.extractRenderState(graphics, mx, my, delta);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void renderList(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      List<autismclient.b.DataUtil.StreamUtil> reports = this.visibleReports();
      int pitch = this.rowPitch();
      int maxScroll = this.maxScrollFor(reports.size());
      this.scrollOffset = snapScroll(this.scrollOffset, pitch, maxScroll);
      this.scroll.jumpTo(this.scrollOffset, maxScroll);
      int first = this.scrollOffset / pitch;
      int top = this.listTop();
      int bottom = this.listBottom();
      int h = this.rowHeight();
      int y = top - this.scrollOffset % pitch;

      for (int i = first; i < reports.size() && y + h <= bottom; i++) {
         if (y + h > top) {
            this.drawRow(graphics, reports.get(i), y, mouseX, mouseY);
         }

         y += pitch;
      }

      if (reports.isEmpty()) {
         this.drawTextClipped(
            graphics,
            this.allReports.isEmpty() ? "No AUTISM addons discovered." : "No addons match the search.",
            this.rowLeft(),
            this.centeredTextY(this.listTop(), this.rowHeight()),
            GuiColorPalette.close,
            false,
            this.rowWidth()
         );
      }
   }

   private void drawRow(GuiGraphicsExtractor graphics, autismclient.b.DataUtil.StreamUtil report, int y, int mouseX, int mouseY) {
      int h = this.rowHeight();
      boolean selected = report.init_Str().equals(this.selectedAddonId);
      boolean hovered = mouseX >= this.rowLeft() && mouseX < this.rowRight() && mouseY >= y && mouseY < y + h;
      int fill = selected ? 858052714 : (hovered ? 606804509 : 403771667);
      int status = selected ? GuiColorPalette.sync : this.statusColor(report);
      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(this.rowLeft(), y, this.rowWidth(), h), fill);
      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(this.rowLeft(), y, 2, h), status);
      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(this.rowLeft() + 4, y + 3, 3, h - 6), report.init_I());
      int ty = this.centeredTextY(y, h);
      String statusText = this.statusLabel(report);
      int statusW = Math.min(this.textWidth(statusText), this.rowWidth() / 3);
      int statusX = this.rowRight() - 4 - statusW;
      this.drawTextClipped(graphics, statusText, statusX, ty, this.statusColor(report), false, statusW);
      int nameX = this.rowLeft() + 10;
      int nameMax = Math.max(1, statusX - 4 - nameX);
      int nameW = Math.min(this.textWidth(report.load_Str()), nameMax);
      this.drawTextClipped(graphics, report.load_Str(), nameX, ty, GuiColorPalette.flush, false, nameMax);
      int idX = nameX + nameW + 4;
      int idMax = statusX - 4 - idX;
      if (idMax > 12) {
         this.drawTextClipped(graphics, report.init_Str() + (report.read_Str().isBlank() ? "" : "  v" + report.read_Str()), idX, ty, GuiColorPalette.close, false, idMax);
      }
   }

   private void renderDetails(GuiGraphicsExtractor graphics) {
      this.renderDetailsFor(graphics, this.selectedReport());
   }

   private int renderDetailsFor(GuiGraphicsExtractor graphics, autismclient.b.DataUtil.StreamUtil report) {
      int line = 12;
      int x = this.detailCardX() + 4;
      int y = this.bodyY() + 4;
      int width = this.detailInnerWidth();
      this.drawTextClipped(graphics, "Details", x, this.centeredTextY(y, 12), GuiColorPalette.flush, false, width);
      y += 14;
      if (report == null) {
         this.drawTextClipped(graphics, "Select an addon to inspect its status.", x, this.centeredTextY(y, line), GuiColorPalette.close, false, width);
         return y + line;
      }

      int labelW = this.detailLabelWidth();
      this.drawTextClipped(graphics, report.load_Str(), x, this.centeredTextY(y, line), GuiColorPalette.flush, false, width);
      y += line;
      this.drawTextClipped(graphics, report.init_Str(), x, this.centeredTextY(y, line), GuiColorPalette.close, false, width);
      y += line + 2;
      y = this.drawDetailRow(graphics, x, y, width, labelW, "Status", this.statusLabel(report), this.statusColor(report));
      y = this.drawDetailRow(graphics, x, y, width, labelW, "API", this.apiVersionText(report), -7429889);
      y = this.drawDetailRow(graphics, x, y, width, labelW, "Version", report.read_Str().isBlank() ? "unknown" : report.read_Str(), GuiColorPalette.flush);
      y = this.drawDetailRow(graphics, x, y, width, labelW, "Authors", report.write_Str().isBlank() ? "unknown" : report.write_Str(), GuiColorPalette.flush);
      y = this.drawDetailRow(graphics, x, y, width, labelW, "Extensions", report.close(), GuiColorPalette.open);
      if (report.sync_I() > 0) {
         y = this.drawDetailRow(graphics, x, y, width, labelW, "Rejected", Integer.toString(report.sync_I()), -14249);
      }

      if (report.write_I() > 0) {
         y = this.drawDetailRow(graphics, x, y, width, labelW, "Runtime Errors", Integer.toString(report.write_I()), GuiColorPalette.poll);
      }

      if (autismclient.b.DataUtil.read(report.init_Str())) {
         y += 2;
         this.drawTextClipped(graphics, "Will be disabled after restart.", x, this.centeredTextY(y, line), -14249, false, width);
         y += line;
      }

      if (!report.sync_Str().isBlank()) {
         y += 2;
         this.drawTextClipped(graphics, report.sync_Str(), x, this.centeredTextY(y, line), GuiColorPalette.poll, false, width);
         y += line;
      }

      if (!report.init_List().isEmpty()) {
         y += 2;
         this.drawTextClipped(graphics, "Latest rejection:", x, this.centeredTextY(y, line), GuiColorPalette.close, false, width);
         y += line;
         this.drawTextClipped(graphics, report.init_List().get(report.init_List().size() - 1), x, this.centeredTextY(y, line), -14249, false, width);
         y += line;
      } else if (!report.flush().isBlank()) {
         y += 2;
         this.drawTextClipped(graphics, report.flush(), x, this.centeredTextY(y, line), -14249, false, width);
         y += line;
      }

      return y;
   }

   private int drawDetailRow(GuiGraphicsExtractor graphics, int x, int y, int width, int labelW, String label, String value, int valueColor) {
      int ty = this.centeredTextY(y, 12);
      this.drawTextClipped(graphics, label + ":", x, ty, GuiColorPalette.close, false, labelW);
      this.drawTextRight(graphics, value, x + width, ty, valueColor, Math.max(1, width - labelW - 4));
      return y + 12;
   }

   private int detailLabelWidth() {
      int w = 0;

      for (String label : DETAIL_LABELS) {
         w = Math.max(w, this.textWidth(label + ":"));
      }

      return w;
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      MouseButtonEvent virtual = toVirtual(event);
      if (Compat263.mouseEventGlfwButton(virtual) != 0) {
         return super.mouseClicked(virtual, doubleClick);
      }

      for (autismclient.gui.ui.widgets.OverlayActionButton button : this.buttons) {
         if (autismclient.gui.ui.widgets.OverlayActionButton.handleClick(button, virtual.x(), virtual.y(), Compat263.mouseEventGlfwButton(virtual))) {
            return true;
         }
      }

      autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbar = this.scrollbarMetrics(this.visibleReports().size());
      if (scrollbar.isScrollable() && scrollbar.containsTrack(virtual.x(), virtual.y())) {
         this.draggingScrollbar = true;
         this.scrollGrabOffset = scrollbar.containsThumb(virtual.x(), virtual.y()) ? Math.max(0, (int)Math.round(virtual.y()) - scrollbar.thumbY()) : scrollbar.thumbHeight() / 2;
         this.scrollOffset = snapScroll(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(scrollbar, virtual.y(), this.scrollGrabOffset), this.rowPitch(), scrollbar.maxScroll());
         this.scroll.jumpTo(this.scrollOffset, scrollbar.maxScroll());
         return true;
      } else {
         autismclient.b.DataUtil.StreamUtil row = this.reportAt(virtual.x(), virtual.y());
         if (row != null) {
            this.selectedAddonId = row.init_Str();
            this.rebuildButtons();
            this.unfocusSearch();
            return true;
         } else {
            return super.mouseClicked(virtual, doubleClick);
         }
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      if (this.draggingScrollbar) {
         this.draggingScrollbar = false;
         return true;
      } else {
         return super.mouseReleased(toVirtual(event));
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      MouseButtonEvent virtual = toVirtual(event);
      if (this.draggingScrollbar) {
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbar = this.scrollbarMetrics(this.visibleReports().size());
         this.scrollOffset = snapScroll(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(scrollbar, virtual.y(), this.scrollGrabOffset), this.rowPitch(), scrollbar.maxScroll());
         this.scroll.jumpTo(this.scrollOffset, scrollbar.maxScroll());
         return true;
      } else {
         return super.mouseDragged(virtual, cA.init_D_D(dx), cA.init_D_D(dy));
      }
   }

   @Override
   public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
      x = cA.init_D_D(x);
      y = cA.init_D_D(y);
      if (!(x < this.listCardX()) && !(x >= this.listCardX() + this.listCardWidth()) && !(y < this.bodyY()) && !(y >= this.bodyBottom())) {
         int maxScroll = this.maxScrollFor(this.visibleReports().size());
         if (maxScroll <= 0) {
            return true;
         }

         this.scrollOffset = snapScroll(this.scrollOffset - (int)Math.signum(scrollY) * this.rowPitch(), this.rowPitch(), maxScroll);
         this.scroll.setOffset(this.scrollOffset, maxScroll);
         return true;
      } else {
         return super.mouseScrolled(x, y, scrollX, scrollY);
      }
   }

   @Override
   public void onClose() {
      this.minecraft.gui.setScreen(this.parent);
   }

   private autismclient.b.DataUtil.StreamUtil reportAt(double mouseX, double mouseY) {
      if (!(mouseX < this.rowLeft()) && !(mouseX >= this.rowRight()) && !(mouseY < this.listTop()) && !(mouseY >= this.listBottom())) {
         List<autismclient.b.DataUtil.StreamUtil> reports = this.visibleReports();
         int local = (int)mouseY - this.listTop() + this.scrollOffset;
         if (local % this.rowPitch() >= this.rowHeight()) {
            return null;
         } else {
            int index = local / this.rowPitch();
            if (this.listTop() + index * this.rowPitch() - this.scrollOffset + this.rowHeight() > this.listBottom()) {
               return null;
            } else {
               return index >= 0 && index < reports.size() ? reports.get(index) : null;
            }
         }
      } else {
         return null;
      }
   }

   private void copySelectedId() {
      autismclient.b.DataUtil.StreamUtil report = this.selectedReport();
      if (report != null && this.minecraft != null) {
         this.minecraft.keyboardHandler.setClipboard(report.init_Str());
         bi.init("Addon ID copied.");
      }
   }

   private void copySelectedReport() {
      autismclient.b.DataUtil.StreamUtil report = this.selectedReport();
      if (report != null && this.minecraft != null) {
         this.minecraft.keyboardHandler.setClipboard(report.open());
         bi.init("Addon report copied.");
      }
   }

   private void toggleSelectedRestart() {
      autismclient.b.DataUtil.StreamUtil report = this.selectedReport();
      if (report != null) {
         boolean next = !autismclient.b.DataUtil.read(report.init_Str());
         autismclient.b.DataUtil.init(report.init_Str(), next);
         bi.init(next ? "Addon disables after restart." : "Addon enables after restart.", next ? -14249 : GuiColorPalette.open);
         this.rebuildButtons();
      }
   }

   private void openModsFolder() {
      Compat263.openFile(Util.getPlatform(), autismclient.b.DataUtil.init_File());
   }

   private List<autismclient.b.DataUtil.StreamUtil> visibleReports() {
      String query = normalizeQuery(this.appliedQuery);
      if (query.equals(this.cachedQuery)) {
         return this.filteredReports;
      }

      this.cachedQuery = query;
      if (query.isEmpty()) {
         this.filteredReports = this.allReports;
         return this.filteredReports;
      }

      List<autismclient.b.DataUtil.StreamUtil> out = new ArrayList<>();

      for (autismclient.b.DataUtil.StreamUtil report : this.allReports) {
         if (normalizeQuery(report.load_Str()).contains(query)
            || normalizeQuery(report.init_Str()).contains(query)
            || normalizeQuery(report.write_Str()).contains(query)) {
            out.add(report);
         }
      }

      this.filteredReports = List.copyOf(out);
      return this.filteredReports;
   }

   private autismclient.b.DataUtil.StreamUtil selectedReport() {
      for (autismclient.b.DataUtil.StreamUtil report : this.allReports) {
         if (report.init_Str().equals(this.selectedAddonId)) {
            return report;
         }
      }

      return null;
   }

   private String summaryText() {
      int total = this.allReports.size();
      int loaded = 0;
      int issues = 0;
      int disabled = 0;

      for (autismclient.b.DataUtil.StreamUtil report : this.allReports) {
         if (report.init_DataUtil_DataUtil() == autismclient.b.DataUtil.Inner_DataUtil.LOADED) {
            loaded++;
         }

         if (report.init_DataUtil_DataUtil().init_Z()) {
            issues++;
         }

         if (autismclient.b.DataUtil.read(report.init_Str()) || report.init_DataUtil_DataUtil() == autismclient.b.DataUtil.Inner_DataUtil.DISABLED) {
            disabled++;
         }
      }

      return total + " discovered  " + loaded + " loaded  " + issues + " issue(s)  " + disabled + " disabled/restart";
   }

   private String statusLabel(autismclient.b.DataUtil.StreamUtil report) {
      if (report == null) {
         return "";
      } else {
         return autismclient.b.DataUtil.read(report.init_Str())
               && report.init_DataUtil_DataUtil() == autismclient.b.DataUtil.Inner_DataUtil.LOADED
            ? "Disables"
            : report.init_DataUtil_DataUtil().init_Str();
      }
   }

   private int statusColor(autismclient.b.DataUtil.StreamUtil report) {
      if (report == null) {
         return GuiColorPalette.close;
      } else if (report.init_DataUtil_DataUtil().init_Z()) {
         return GuiColorPalette.poll;
      } else {
         return !autismclient.b.DataUtil.read(report.init_Str()) && report.init_DataUtil_DataUtil() != autismclient.b.DataUtil.Inner_DataUtil.DISABLED
            ? GuiColorPalette.open
            : -14249;
      }
   }

   private String apiVersionText(autismclient.b.DataUtil.StreamUtil report) {
      String addon = report.load_I() < 0 ? "unknown" : Integer.toString(report.load_I());
      return "addon v" + addon + " / host v" + report.read_I();
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics(int rows) {
      int view = Math.max(1, this.listBottom() - this.listTop());
      return autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(
         this.contentHeight(rows), view, this.scrollbarX(), this.listTop(), 6, this.listBottom() - this.listTop(), this.scrollOffset
      );
   }

   private int maxScrollFor(int rows) {
      return Math.max(0, this.contentHeight(rows) - Math.max(1, this.listBottom() - this.listTop()));
   }

   private int contentHeight(int rows) {
      return rows <= 0 ? 0 : rows * this.rowPitch() - 2;
   }

   private void unfocusSearch() {
      if (this.searchBox != null) {
         this.searchBox.setFocused(false);
      }

      this.setFocused(null);
   }

   private void drawText(GuiGraphicsExtractor graphics, String text, int x, int y, int color, boolean center) {
      this.drawTextClipped(graphics, text, x, y, color, center, 2147483647);
   }

   private void drawTextClipped(GuiGraphicsExtractor graphics, String text, int x, int y, int color, boolean center, int maxWidth) {
      if (graphics != null) {
         Font renderer = this.font;
         Identifier fontId = THEME.init_Identifier_l(l.read);
         String value = text == null ? "" : text;
         if (maxWidth != 2147483647 && !center) {
            k.load(graphics, renderer, value, fontId, color, x, y, Math.max(1, maxWidth), false);
         } else {
            if (maxWidth != 2147483647) {
               value = k.load(renderer, value, maxWidth, fontId, color);
            }

            int w = k.init(renderer, value, fontId, color);
            k.init(graphics, renderer, value, fontId, color, center ? x - w / 2 : x, y, false);
         }
      }
   }

   private void drawTextRight(GuiGraphicsExtractor graphics, String text, int right, int y, int color, int maxWidth) {
      if (graphics != null) {
         Identifier fontId = THEME.init_Identifier_l(l.read);
         String value = k.load(this.font, text == null ? "" : text, Math.max(1, maxWidth), fontId, color);
         k.init(graphics, this.font, value, fontId, color, right - k.init(this.font, value, fontId, color), y, false);
      }
   }

   private int textWidth(String text) {
      return k.init(this.font, text == null ? "" : text, THEME.init_Identifier_l(l.read), 0);
   }

   private int centeredTextY(int top, int height) {
      return autismclient.gui.f.UiHelper.init(this.font).centerTextY(autismclient.gui.f.NetHelper.init(0, top, 1, height));
   }

   private int controlHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int rowHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int rowPitch() {
      return this.rowHeight() + 2;
   }

   private int cardWidth() {
      return Math.max(200, Math.min(560, this.virtualWidth() - 12));
   }

   private int cardX() {
      return (this.virtualWidth() - this.cardWidth()) / 2;
   }

   private int cardY() {
      return 6;
   }

   private int headerControlsY() {
      return this.cardY() + 4;
   }

   private int headerCardHeight() {
      return this.controlHeight() + 8;
   }

   private int bodyY() {
      return this.cardY() + this.headerCardHeight() + 4;
   }

   private int listCardWidth() {
      return Math.max(140, Math.min(260, (this.cardWidth() - 4) * 45 / 100));
   }

   private int listCardX() {
      return this.cardX();
   }

   private int detailCardX() {
      return this.listCardX() + this.listCardWidth() + 4;
   }

   private int detailCardWidth() {
      return Math.max(1, this.cardX() + this.cardWidth() - this.detailCardX());
   }

   private int detailInnerWidth() {
      return Math.max(40, this.detailCardWidth() - 8);
   }

   private int searchY() {
      return this.bodyY() + 4 + 12 + 2;
   }

   private int listTop() {
      return this.searchY() + this.controlHeight() + 2;
   }

   private int listBottom() {
      return this.bodyBottom() - 4;
   }

   private int bodyBottom() {
      return this.bodyY() + this.bodyHeight();
   }

   private int bodyHeight() {
      int available = Math.max(60, this.virtualHeight() - 6 - this.bodyY());
      int maxRows = Math.max(3, this.virtualHeight() * 65 / 100 / this.rowPitch());
      int rows = Math.max(3, Math.min(maxRows, this.visibleReports().size()));
      int list = this.listTop() - this.bodyY() + this.contentHeight(rows) + 4;
      autismclient.b.DataUtil.StreamUtil selected = this.selectedReport();
      int details = this.renderDetailsFor(null, selected) - this.bodyY() + 4 + this.buttonRowsHeight(selected) + 4;
      return Math.min(available, Math.max(list, details));
   }

   private int rowLeft() {
      return this.listCardX() + 4;
   }

   private int scrollbarX() {
      return this.listCardX() + this.listCardWidth() - 4 - 6;
   }

   private int rowRight() {
      return this.scrollbarX() - 2;
   }

   private int rowWidth() {
      return Math.max(1, this.rowRight() - this.rowLeft());
   }

   private static int snapScroll(int value, int step, int maxScroll) {
      int clamped = Math.max(0, Math.min(maxScroll, value));
      if (step <= 0) {
         return clamped;
      }

      int rounded = Math.round((float)clamped / step) * step;
      return Math.max(0, Math.min(maxScroll, rounded));
   }

   private static String normalizeQuery(String value) {
      return trim(value).toLowerCase(Locale.ROOT);
   }
}
