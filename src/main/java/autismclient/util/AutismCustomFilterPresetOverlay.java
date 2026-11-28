package autismclient.util;

import autismclient.gui.ui.widgets.UiLayoutMath;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class AutismCustomFilterPresetOverlay extends DraggableOverlayWindow {
   private final Font textRenderer;
   private final autismclient.gui.f.b.CharEncoder styleTheme = new autismclient.gui.f.b.CharEncoder();
   private final autismclient.gui.ui.widgets.TextInputWidget nameField;
   private final List<AutismCustomFilterPresetOverlay.ActionButton> actionButtons = new ArrayList<>();
   private final AutismCustomFilterPresetOverlay.PresetList presetList = new AutismCustomFilterPresetOverlay.PresetList();
   private List<PacketPresetManager.PresetEntry> builtInEntries = List.of();
   private List<PacketPresetManager.PresetEntry> userEntries = List.of();
   private List<AutismCustomFilterPresetOverlay.PresetRow> visibleRows = List.of();
   private int panelX = 570;
   private int panelY = 48;
   private int panelWidth;
   private int panelHeight;
   private boolean visible = false;
   private boolean collapsed = false;
   private boolean dragging = false;
   private double dragOffsetX = 0.0;
   private double dragOffsetY = 0.0;
   private boolean draggingScrollbar = false;
   private int scrollbarGrabOffset = 0;
   private String selectedPresetName;

   public AutismCustomFilterPresetOverlay(Font textRenderer) {
      this.textRenderer = textRenderer;
      this.panelWidth = this.minPanelWidth();
      this.panelHeight = this.defaultPanelHeight();
      this.nameField = new autismclient.gui.ui.widgets.TextInputWidget()
         .setPlaceholder("Preset name")
         .setFixedHeight(this.nameFieldHeight())
         .setMinWidth(120.0F)
         .setPreferredWidth(120.0F)
         .setTextTone(autismclient.gui.f.b.l.read)
         .setPlaceholderTone(autismclient.gui.f.b.l.write);
   }

   @Override
   public String getOverlayId() {
      return "autism-custom-filter-presets";
   }

   @Override
   public int getMinWidth() {
      return this.minPanelWidth();
   }

   @Override
   public int getMinHeight() {
      return this.minPanelHeight();
   }

   @Override
   public OverlayBounds getBounds() {
      return new OverlayBounds(this.panelX, this.panelY, this.panelWidth, this.panelHeight, this.visible, this.collapsed);
   }

   @Override
   public void setBounds(OverlayBounds bounds) {
      if (bounds != null) {
         OverlayBounds clamped = this.clampToScreen(this, bounds);
         this.panelX = clamped.x;
         this.panelY = clamped.y;
         this.panelWidth = clamped.width;
         this.panelHeight = clamped.height;
         this.visible = clamped.visible;
         this.collapsed = clamped.collapsed;
      }
   }

   @Override
   public void setVisible(boolean visible) {
      this.visible = visible;
      if (visible) {
         OverlayManager.getInstance().bringToFront(this);
      } else {
         this.unfocusNameField();
      }

      this.saveBounds();
   }

   public void toggleVisible() {
      this.setVisible(!this.visible);
   }

   @Override
   public boolean isVisible() {
      return this.visible;
   }

   @Override
   public boolean isCollapsed() {
      return this.collapsed;
   }

   @Override
   public void setCollapsed(boolean collapsed) {
      if (this.collapsed != collapsed) {
         this.collapsed = collapsed;
         if (collapsed) {
            this.clearFocusAndDismissMenu();
         }

         this.saveBounds();
      }
   }

   @Override
   public boolean supportsHeaderCollapseClick() {
      return true;
   }

   @Override
   public boolean isOverHeader(double mouseX, double mouseY) {
      if (!this.visible) {
         return false;
      }

      OverlayBounds bounds = this.getBounds();
      return mouseX >= this.panelX
         && mouseX <= this.panelX + this.panelWidth
         && mouseY >= this.panelY
         && mouseY <= this.panelY + 14
         && !this.isOverHeaderControl(mouseX, mouseY, bounds);
   }

   @Override
   public boolean hasFocusedTextInput() {
      return this.nameField.isFocused();
   }

   @Override
   public void clearInputFocus() {
      this.unfocusNameField();
   }

   @Override
   public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      if (this.visible || this.isClosing()) {
         this.actionButtons.clear();
         this.panelHeight = this.chromeHeight() + this.listHeightForRows(this.clampVisibleRows(this.buildRows().size()));
         OverlayBounds clamped = this.clampToScreen(this, new OverlayBounds(this.panelX, this.panelY, this.panelWidth, this.panelHeight, this.visible, this.collapsed));
         this.panelX = clamped.x;
         this.panelY = clamped.y;
         this.panelWidth = clamped.width;
         this.panelHeight = clamped.height;
         OverlayBounds bounds = new OverlayBounds(this.panelX, this.panelY, this.panelWidth, this.panelHeight, this.visible, this.collapsed);
         this.renderWindowFrame(context, mouseX, mouseY, bounds, "Preset Manager", this.collapsed, this.dragging);
         boolean clipBody = this.beginBodyClip(context, bounds, this.collapsed);
         if (!clipBody) {
            this.renderInactiveDim(context, bounds, this.collapsed, this.dragging);
         } else {
            try {
               int x = this.panelX + this.contentInset();
               int y = this.panelY + 14 + this.topPadding();
               int width = this.panelWidth - this.contentInset() * 2;
               autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
               autismclient.gui.ui.widgets.ListRowRenderer.drawHeaderTitle(context, this.textRenderer, "Preset Name", x, y);
               String counts = "C2S " + module.getCustomC2sPacketsCopy().size() + " | S2C " + module.getCustomS2cPacketsCopy().size();
               ThemedTextHelper.drawTone(context, this.textRenderer, counts, ThemedTextHelper.TextTone.MUTED, x + width - ThemedTextHelper.width(this.textRenderer, counts, ThemedTextHelper.TextTone.MUTED), y, false);
               y += this.textLineHeight();
               this.nameField.setBounds(x, y, width, this.nameFieldHeight());
               this.nameField.render(this.layoutContext(context, mouseX, mouseY, delta));
               y += this.nameFieldHeight() + this.sectionGap();
               y = this.renderButtonRow(
                  context,
                  mouseX,
                  mouseY,
                  x,
                  y,
                  width,
                  new String[]{"Save", "Load", "Overwrite", "Delete"},
                  new Runnable[]{this::savePreset, this::loadPreset, this::overwritePreset, this::deletePreset},
                  new boolean[]{this.canSave(), this.canLoad(), this.canOverwrite(), this.canDelete()}
               );
               String resetLabel = "Reset To Default";
               int resetW = Math.min(width, autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(ThemedTextHelper.width(this.textRenderer, resetLabel, ThemedTextHelper.TextTone.BODY)));
               y = this.renderActionButton(context, mouseX, mouseY, x, y, resetW, resetLabel, this::resetToDefault, true);
               y += this.resetButtonGap() - this.sectionGap();
               int listHeight = Math.max(this.listHeightForRows(1), this.panelHeight - (y - this.panelY) - this.textLineHeight() - this.contentInset());
               this.renderPresetList(context, mouseX, mouseY, delta, x, y, width, listHeight);
            } finally {
               this.endBodyClip(context, clipBody);
               this.renderInactiveDim(context, bounds, this.collapsed, this.dragging);
            }
         }
      }
   }

   private int renderPresetList(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, int x, int y, int width, int listHeight) {
      String headerLabel = "Saved Presets";
      autismclient.gui.ui.widgets.ListRowRenderer.drawHeaderTitle(context, this.textRenderer, headerLabel, x, y);
      int headerY = y;
      y += this.textLineHeight();
      List<AutismCustomFilterPresetOverlay.PresetRow> rows = this.buildRows();
      this.presetList.rows = rows;
      autismclient.gui.ui.widgets.ScrollListLayout.ListLayout baseLayout = this.listLayout(x, y, width, listHeight, rows.size(), 0);
      int visualScroll = this.presetList.scroll.tickAndClamp(delta, baseLayout.maxScroll());
      autismclient.gui.ui.widgets.ScrollListLayout.ListLayout listLayout = this.listLayout(x, y, width, listHeight, rows.size(), visualScroll);
      int contentWidth = Math.max(40, listLayout.contentWidth());
      this.presetList.setBounds(x, y, width, listHeight, listLayout.scrollbar());
      boolean focused = OverlayManager.getInstance().isFocused(this) || OverlayManager.getInstance().isTopmost(this);
      listLayout.drawFrame(context, focused);
      if (rows.isEmpty()) {
         autismclient.gui.ui.widgets.ListRowRenderer.drawEmptyPlaceholder(context, this.textRenderer, "No presets", x, y, contentWidth);
      } else {
         listLayout.pushContentClip(context);

         try {
            listLayout.forEachVisibleRow(
               rows.size(),
               (index, rowY) -> {
                  AutismCustomFilterPresetOverlay.PresetRow row = rows.get(index);
                  int rowTextY = UiLayoutMath.centerTextY(rowY, this.rowHeight(), this.styleTheme.init_I_l(autismclient.gui.f.b.l.read), this.styleTheme.flush());
                  if (row.header) {
                     autismclient.gui.ui.widgets.ListRowRenderer.drawRow(
                        context,
                        this.textRenderer,
                        row.label,
                        x + 2,
                        rowY,
                        contentWidth,
                        this.rowHeight(),
                        false,
                        false,
                        autismclient.gui.ui.widgets.ListRowRenderer.RowTone.WARNING
                     );
                  } else if (row.note) {
                     autismclient.gui.ui.widgets.ListRowRenderer.drawRow(
                        context,
                        this.textRenderer,
                        row.label,
                        x + 2,
                        rowY,
                        contentWidth,
                        this.rowHeight(),
                        false,
                        false,
                        autismclient.gui.ui.widgets.ListRowRenderer.RowTone.NORMAL
                     );
                  } else if (row.entry != null) {
                     boolean selected = row.entry.name().equals(this.selectedPresetName);
                     boolean hovered = mouseX >= x + 2 && mouseX < x + 2 + contentWidth && mouseY >= rowY && mouseY < rowY + this.rowHeight();
                     autismclient.gui.ui.widgets.ListRowRenderer.drawRow(
                        context,
                        this.textRenderer,
                        "",
                        x + 2,
                        rowY,
                        contentWidth,
                        this.rowHeight(),
                        hovered,
                        selected,
                        row.entry.isBuiltIn() ? autismclient.gui.ui.widgets.ListRowRenderer.RowTone.WARNING : autismclient.gui.ui.widgets.ListRowRenderer.RowTone.NORMAL
                     );
                     int textColor = selected ? UiColorPalette.selectedText() : (row.entry.isBuiltIn() ? -4729601 : UiColorPalette.menuItemText());
                     String prefix = row.entry.isBuiltIn() ? "[DEV] " : "[USR] ";
                     String name = ThemedTextHelper.ellipsize(this.textRenderer, prefix + row.entry.name(), contentWidth - 70, ThemedTextHelper.TextTone.BODY);
                     ThemedTextHelper.drawColor(context, this.textRenderer, name, textColor, x + 5, rowTextY, false);
                     String counts = row.entry.c2sCount() + " / " + row.entry.s2cCount();
                     int countsWidth = ThemedTextHelper.width(this.textRenderer, counts, ThemedTextHelper.TextTone.BODY);
                     ThemedTextHelper.drawColor(context, this.textRenderer, counts, textColor, x + contentWidth - countsWidth - 1, rowTextY, false);
                  }

                  autismclient.gui.ui.widgets.ListRowRenderer.drawRowSeparator(context, x + 2, rowY + this.rowHeight(), contentWidth);
               }
            );
         } finally {
            listLayout.popContentClip(context);
         }
      }

      listLayout.renderScrollbar(context, mouseX, mouseY, this.draggingScrollbar);
      String footer = listLayout.maxScroll() > 0
         ? "Scroll: "
            + Math.min(rows.size(), Math.round((float)this.presetList.scroll.getOffset() / this.rowStep()) + 1)
            + "/"
            + Math.max(1, Math.round((float)listLayout.maxScroll() / this.rowStep()) + 1)
         : (this.selectedPresetName == null ? "Select a preset row" : "Selected: " + this.selectedPresetName);
      int footerMaxW = Math.max(1, width - ThemedTextHelper.width(this.textRenderer, headerLabel, ThemedTextHelper.TextTone.BODY) - 8);
      footer = ThemedTextHelper.ellipsize(this.textRenderer, footer, footerMaxW, ThemedTextHelper.TextTone.MUTED);
      int footerW = ThemedTextHelper.width(this.textRenderer, footer, ThemedTextHelper.TextTone.MUTED);
      ThemedTextHelper.drawTone(context, this.textRenderer, footer, ThemedTextHelper.TextTone.MUTED, x + width - footerW, headerY, false);
      return y + listHeight;
   }

   private int clampVisibleRows(int count) {
      return Math.max(3, Math.min(12, count));
   }

   private int listHeightForRows(int rows) {
      return rows * this.rowStep() + 4;
   }

   private int chromeHeight() {
      return 14
         + this.topPadding()
         + this.textLineHeight()
         + this.nameFieldHeight()
         + this.sectionGap()
         + (this.buttonHeight() + this.sectionGap()) * 2
         + this.resetButtonGap()
         - this.sectionGap()
         + this.textLineHeight()
         + this.contentInset();
   }

   private List<AutismCustomFilterPresetOverlay.PresetRow> buildRows() {
      PacketPresetManager manager = PacketPresetManager.getInstance();
      List<PacketPresetManager.PresetEntry> builtInEntries = manager.builtInPresets();
      List<PacketPresetManager.PresetEntry> userEntries = manager.userPresets();
      if (builtInEntries == this.builtInEntries && userEntries == this.userEntries) {
         return this.visibleRows;
      }

      List<AutismCustomFilterPresetOverlay.PresetRow> rows = new ArrayList<>();
      rows.add(AutismCustomFilterPresetOverlay.PresetRow.headerRow("Developer Templates"));

      for (PacketPresetManager.PresetEntry entry : builtInEntries) {
         rows.add(AutismCustomFilterPresetOverlay.PresetRow.presetRow(entry));
      }

      rows.add(AutismCustomFilterPresetOverlay.PresetRow.headerRow("User Presets"));
      if (userEntries.isEmpty()) {
         rows.add(AutismCustomFilterPresetOverlay.PresetRow.noteRow("No user presets yet"));
      } else {
         for (PacketPresetManager.PresetEntry entry : userEntries) {
            rows.add(AutismCustomFilterPresetOverlay.PresetRow.presetRow(entry));
         }
      }

      this.builtInEntries = builtInEntries;
      this.userEntries = userEntries;
      this.visibleRows = List.copyOf(rows);
      return this.visibleRows;
   }

   private void savePreset() {
      String name = this.typedName();
      if (name == null) {
         AutismClientMessaging.sendPrefixed("Enter a preset name first.");
      } else if (PacketPresetManager.getInstance().savePreset(name)) {
         this.selectedPresetName = name;
         this.nameField.setValue(name);
      }
   }

   private void loadPreset() {
      String presetName = this.resolveSelectedPresetName();
      if (presetName == null) {
         AutismClientMessaging.sendPrefixed("Select a preset first.");
      } else if (PacketPresetManager.getInstance().loadPreset(presetName)) {
         this.selectedPresetName = PacketPresetManager.getInstance().findPreset(presetName) != null ? PacketPresetManager.getInstance().findPreset(presetName).name() : presetName;
         this.nameField.setValue(this.selectedPresetName);
      }
   }

   private void overwritePreset() {
      String presetName = this.resolveSelectedPresetName();
      if (presetName == null) {
         AutismClientMessaging.sendPrefixed("Select a user preset first.");
      } else {
         PacketPresetManager.PresetEntry entry = PacketPresetManager.getInstance().findPreset(presetName);
         if (entry != null && !entry.isBuiltIn()) {
            if (PacketPresetManager.getInstance().overwritePreset(entry.name())) {
               this.selectedPresetName = entry.name();
               this.nameField.setValue(entry.name());
            }
         } else {
            AutismClientMessaging.sendPrefixed("Select a user preset to overwrite.");
         }
      }
   }

   private void deletePreset() {
      String presetName = this.resolveSelectedPresetName();
      if (presetName == null) {
         AutismClientMessaging.sendPrefixed("Select a user preset first.");
      } else {
         PacketPresetManager.PresetEntry entry = PacketPresetManager.getInstance().findPreset(presetName);
         if (entry != null && !entry.isBuiltIn()) {
            if (PacketPresetManager.getInstance().deletePreset(entry.name())) {
               if (entry.name().equals(this.selectedPresetName)) {
                  this.selectedPresetName = null;
               }

               this.nameField.setValue("");
            }
         } else {
            AutismClientMessaging.sendPrefixed("Developer presets cannot be deleted.");
         }
      }
   }

   private void resetToDefault() {
      autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
      module.resetCustomC2sPackets();
      module.resetCustomS2cPackets();
      AutismClientMessaging.sendPrefixed("Reset current filter to default packet lists.");
   }

   private String typedName() {
      String value = this.nameField.getValue();
      if (value == null) {
         return null;
      }

      value = value.trim();
      return value.isEmpty() ? null : value;
   }

   private String resolveSelectedPresetName() {
      if (this.selectedPresetName != null) {
         PacketPresetManager.PresetEntry selectedEntry = PacketPresetManager.getInstance().findPreset(this.selectedPresetName);
         if (selectedEntry != null) {
            return selectedEntry.name();
         }
      }

      String typedName = this.typedName();
      if (typedName == null) {
         return null;
      }

      PacketPresetManager.PresetEntry typedEntry = PacketPresetManager.getInstance().findPreset(typedName);
      return typedEntry != null ? typedEntry.name() : null;
   }

   private PacketPresetManager.PresetEntry selectedEntry() {
      return this.selectedPresetName == null ? null : PacketPresetManager.getInstance().findPreset(this.selectedPresetName);
   }

   private boolean canSave() {
      String name = this.typedName();
      return name != null && !PacketPresetManager.getInstance().isBuiltIn(name);
   }

   private boolean canLoad() {
      return this.resolveSelectedPresetName() != null;
   }

   private boolean canOverwrite() {
      PacketPresetManager.PresetEntry entry = this.selectedEntry();
      return entry != null && !entry.isBuiltIn();
   }

   private boolean canDelete() {
      return this.canOverwrite();
   }

   private int renderActionButton(GuiGraphicsExtractor context, int mouseX, int mouseY, int x, int y, int width, String label, Runnable action, boolean enabled) {
      this.drawButton(context, mouseX, mouseY, x, y, width, label, enabled);
      this.actionButtons.add(new AutismCustomFilterPresetOverlay.ActionButton(x, y, width, this.buttonHeight(), action, enabled));
      return y + this.buttonHeight() + this.sectionGap();
   }

   private int renderButtonRow(GuiGraphicsExtractor context, int mouseX, int mouseY, int x, int y, int width, String[] labels, Runnable[] actions, boolean[] enabled) {
      int n = labels.length;
      int cellW = (width - this.sectionGap() * (n - 1)) / n;
      int cx = x;

      for (int i = 0; i < n; i++) {
         int w = i == n - 1 ? x + width - cx : cellW;
         this.drawButton(context, mouseX, mouseY, cx, y, w, labels[i], enabled[i]);
         this.actionButtons.add(new AutismCustomFilterPresetOverlay.ActionButton(cx, y, w, this.buttonHeight(), actions[i], enabled[i]));
         cx += w + this.sectionGap();
      }

      return y + this.buttonHeight() + this.sectionGap();
   }

   private void drawButton(GuiGraphicsExtractor context, int mouseX, int mouseY, int x, int y, int width, String label, boolean enabled) {
      autismclient.gui.ui.widgets.ButtonDrawHelper.drawButton(
         context,
         this.textRenderer,
         x,
         y,
         width,
         this.buttonHeight(),
         label,
         enabled ? autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST,
         enabled,
         mouseX,
         mouseY
      );
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.visible) {
         return false;
      }

      if (button == 0 && mouseX >= this.panelX && mouseX < this.panelX + this.panelWidth && mouseY >= this.panelY && mouseY < this.panelY + 14) {
         OverlayBounds bounds = this.getBounds();
         if (this.isOverHeaderCloseButton(mouseX, mouseY, bounds)) {
            this.setVisible(false);
            return true;
         } else {
            this.dragging = true;
            this.dragOffsetX = mouseX - this.panelX;
            this.dragOffsetY = mouseY - this.panelY;
            this.unfocusNameField();
            return true;
         }
      } else {
         if (this.collapsed) {
            return false;
         }

         if (button == 0) {
            autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbar = this.presetList.scrollbarMetrics;
            if (scrollbar != null && scrollbar.isScrollable() && scrollbar.containsTrack(mouseX, mouseY)) {
               this.draggingScrollbar = true;
               this.scrollbarGrabOffset = Math.max(0, (int)mouseY - scrollbar.thumbY());
               this.presetList.scroll.dragToMouseYStepped(scrollbar, mouseY, this.scrollbarGrabOffset, this.rowStep());
               return true;
            }
         }

         if (this.clickTextField(this.nameField, mouseX, mouseY, button)) {
            return true;
         }

         if (button == 0 && this.presetList.contains(mouseX, mouseY)) {
            AutismCustomFilterPresetOverlay.PresetRow row = this.presetList.rowAt(mouseY);
            if (row != null && row.entry != null) {
               this.selectedPresetName = row.entry.name();
               this.nameField.setValue(this.selectedPresetName);
               this.unfocusNameField();
               return true;
            }
         }

         this.unfocusNameField();
         if (button == 0) {
            for (AutismCustomFilterPresetOverlay.ActionButton actionButton : this.actionButtons) {
               if (actionButton.contains(mouseX, mouseY)) {
                  if (!actionButton.enabled()) {
                     return true;
                  }

                  actionButton.action.run();
                  return true;
               }
            }
         }

         return this.containsPoint(mouseX, mouseY);
      }
   }

   private boolean clickTextField(autismclient.gui.ui.widgets.TextInputWidget field, double mouseX, double mouseY, int button) {
      boolean clicked = field.mouseClicked(this.inputLayoutContext(mouseX, mouseY), (float)mouseX, (float)mouseY, button);
      if (clicked) {
         this.unfocusNameField();
         field.setFocused(true);
      }

      return clicked;
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (this.nameField.mouseReleased(this.inputLayoutContext(mouseX, mouseY), (float)mouseX, (float)mouseY, button)) {
         return true;
      }

      if (button == 0 && this.draggingScrollbar) {
         this.draggingScrollbar = false;
         return true;
      }

      if (button == 0) {
         if (this.dragging) {
            this.saveBounds();
         }

         this.dragging = false;
      }

      return false;
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.nameField
         .mouseDragged(this.inputLayoutContext(mouseX, mouseY), (float)mouseX, (float)mouseY, button, (float)deltaX, (float)deltaY)) {
         return true;
      } else if (this.draggingScrollbar) {
         this.presetList.scroll.dragToMouseYStepped(this.presetList.scrollbarMetrics, mouseY, this.scrollbarGrabOffset, this.rowStep());
         return true;
      } else if (this.dragging && button == 0) {
         OverlayBounds nextBounds = this.clampToScreen(
            this, new OverlayBounds((int)(mouseX - this.dragOffsetX), (int)(mouseY - this.dragOffsetY), this.panelWidth, this.panelHeight, this.visible, this.collapsed)
         );
         this.panelX = nextBounds.x;
         this.panelY = nextBounds.y;
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      if (this.presetList.contains(mouseX, mouseY)) {
         int maxScroll = this.listLayout(
               this.presetList.x,
               this.presetList.y,
               this.presetList.width,
               this.presetList.height,
               this.presetList.rows.size(),
               0
            )
            .maxScroll();
         this.presetList.scroll.scrollByWheel(amount, this.rowStep(), maxScroll);
         return true;
      } else {
         return false;
      }
   }

   private autismclient.gui.ui.widgets.ScrollListLayout.ListLayout listLayout(int x, int y, int width, int listHeight, int rowCount, int scrollOffset) {
      return autismclient.gui.ui.widgets.ScrollListLayout.compute(x, y, width, listHeight, rowCount, this.rowHeight(), this.rowStep(), scrollOffset, this.scrollbarWidth(), this.scrollbarGutter());
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (!this.visible) {
         return false;
      }

      if (keyCode == 256) {
         if (this.nameField.isFocused()) {
            this.unfocusNameField();
            return true;
         } else {
            this.setVisible(false);
            return true;
         }
      } else if ((keyCode == 257 || keyCode == 335) && this.nameField.isFocused() && this.canSave()) {
         this.savePreset();
         return true;
      } else {
         return !this.nameField.isFocused()
            ? false
            : this.nameField.keyPressed(this.inputLayoutContext(0.0, 0.0), keyCode, scanCode, modifiers);
      }
   }

   @Override
   public boolean charTyped(char chr, int modifiers) {
      return this.visible && this.nameField.isFocused()
         ? this.nameField.charTyped(this.inputLayoutContext(0.0, 0.0), chr, modifiers)
         : false;
   }

   @Override
   public boolean containsPoint(double mouseX, double mouseY) {
      if (!this.visible) {
         return false;
      }

      OverlayBounds bounds = this.getBounds();
      int frameHeight = this.collapsed ? 14 : this.panelHeight;
      return mouseX >= bounds.x && mouseX <= bounds.x + bounds.width && mouseY >= bounds.y && mouseY <= bounds.y + frameHeight
         ? true
         : mouseX >= this.nameField.x()
            && mouseX <= this.nameField.x() + this.nameField.width()
            && mouseY >= this.nameField.y()
            && mouseY <= this.nameField.y() + this.nameField.height();
   }

   private void unfocusNameField() {
      this.nameField.setFocused(false);
   }

   private autismclient.gui.ui.layout.LayoutContext layoutContext(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      autismclient.gui.ui.layout.LayoutViewport viewport = autismclient.gui.ui.layout.LayoutViewport.create(1.0F);
      return new autismclient.gui.ui.layout.LayoutContext(
         context, this.textRenderer, viewport, this.styleTheme, viewport.toUiX(mouseX), viewport.toUiY(mouseY), delta
      );
   }

   private autismclient.gui.ui.layout.LayoutContext inputLayoutContext(double mouseX, double mouseY) {
      autismclient.gui.ui.layout.LayoutViewport viewport = autismclient.gui.ui.layout.LayoutViewport.create(1.0F);
      return new autismclient.gui.ui.layout.LayoutContext(null, this.textRenderer, viewport, this.styleTheme, viewport.toUiX(mouseX), viewport.toUiY(mouseY), 0.0F);
   }

   private int minPanelWidth() {
      return 236;
   }

   private int defaultPanelHeight() {
      return this.chromeHeight() + this.listHeightForRows(8);
   }

   private int minPanelHeight() {
      return this.chromeHeight() + this.listHeightForRows(3);
   }

   private int contentInset() {
      return 4;
   }

   private int topPadding() {
      return 4;
   }

   private int textLineHeight() {
      return this.styleTheme.init_I_l_I(autismclient.gui.f.b.l.load, 0);
   }

   private int buttonHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int sectionGap() {
      return 2;
   }

   private int resetButtonGap() {
      return 4;
   }

   private int nameFieldHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int rowHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int rowStep() {
      return this.rowHeight() + 1;
   }

   private int scrollbarGutter() {
      return 8;
   }

   private int scrollbarWidth() {
      return 3;
   }

   private static final class PresetRow {
      private final boolean header;
      private final boolean note;
      private final String label;
      private final PacketPresetManager.PresetEntry entry;

      private PresetRow(boolean header, boolean note, String label, PacketPresetManager.PresetEntry entry) {
         this.header = header;
         this.note = note;
         this.label = label;
         this.entry = entry;
      }

      private static AutismCustomFilterPresetOverlay.PresetRow headerRow(String label) {
         return new AutismCustomFilterPresetOverlay.PresetRow(true, false, label, null);
      }

      private static AutismCustomFilterPresetOverlay.PresetRow noteRow(String label) {
         return new AutismCustomFilterPresetOverlay.PresetRow(false, true, label, null);
      }

      private static AutismCustomFilterPresetOverlay.PresetRow presetRow(PacketPresetManager.PresetEntry entry) {
         return new AutismCustomFilterPresetOverlay.PresetRow(false, false, null, entry);
      }
   }

   private record ActionButton(int x, int y, int width, int height, Runnable action, boolean enabled) {
      boolean contains(double mouseX, double mouseY) {
         return mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height;
      }
   }

   private final class PresetList {
      private int x;
      private int y;
      private int width;
      private int height;
      private final autismclient.gui.ui.widgets.ScrollOffsetController scroll = new autismclient.gui.ui.widgets.ScrollOffsetController();
      private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics = null;
      private List<AutismCustomFilterPresetOverlay.PresetRow> rows = List.of();

      private void setBounds(int x, int y, int width, int height, autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics) {
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
         this.scrollbarMetrics = scrollbarMetrics;
      }

      private boolean contains(double mouseX, double mouseY) {
         return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
      }

      private AutismCustomFilterPresetOverlay.PresetRow rowAt(double mouseY) {
         int idx = (int)((mouseY - this.y - 2.0 + this.scroll.getTargetOffset()) / AutismCustomFilterPresetOverlay.this.rowStep());
         return idx >= 0 && idx < this.rows.size() ? this.rows.get(idx) : null;
      }
   }
}
