package autismclient.gui.screens;

import autismclient.api.hud.HudElements;
import autismclient.compat263.Compat263;
import autismclient.gui.ui.widgets.ToggleSwitchRenderer;
import autismclient.gui.ui.widgets.UiLayoutMath;
import autismclient.gui.f.b.l;
import autismclient.modules.Module;
import autismclient.util.AutismHudManager;
import autismclient.util.NativeImageFilePicker;
import autismclient.util.cA;
import autismclient.util.RegistryDisplayNameCache;
import autismclient.util.ThemePaletteManager;
import autismclient.util.ThemedTextInputBox;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class HudElementSettingsScreen extends Screen {
   private static final autismclient.gui.f.b.CharEncoder THEME = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = -791321;
   private static final int load_I = -4743522;
   private static final int read_I = -50373;
   private static final int write = -10682470;
   private static final int update = 368;
   private static final int sync = 220;
   private static final int flush = 150;
   private static final int close = 40;
   private static final int open = 20;
   private static final int poll = 10;
   private static final int push = 200;
   private static final int pop = 4;
   private final Screen parent;
   private final String id;
   private int scrollOffset;
   private boolean scrollbarDragging;
   private int scrollGrabOffset;
   private String editingKey;
   private ThemedTextInputBox textInput;
   private String unusedColorPickerKey;
   private autismclient.gui.ui.widgets.ColorPickerPopup colorPicker;
   private final List<autismclient.gui.ui.widgets.DropdownWidget> dropdowns = new ArrayList<>();
   private final Map<String, autismclient.gui.ui.widgets.DropdownWidget> dropdownsByKey = new HashMap<>();
   private boolean modulePickerOpen;
   private int pickerScroll;
   private HudElementSettingsScreen.SettingRow row;
   private static final Map<String, HudElementSettingsScreen.CachedRows> rowsCache = new HashMap<>();
   private List<HudElementSettingsScreen.SettingRow> rows;
   private int labelWidthScaleKey = -1;
   private int cachedLabelColumnWidth;

   private static int labelTextColor() {
      return ThemePaletteManager.recolor(-4743522, ThemePaletteManager.ColorRole.TEXT);
   }

   private static int accentColor(int argb) {
      return ThemePaletteManager.recolor(argb, ThemePaletteManager.ColorRole.ACCENT);
   }

   public HudElementSettingsScreen(Screen parent, String id) {
      super(Component.literal("HUD Element Settings"));
      this.parent = parent;
      this.id = id;
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
         int[] panel = this.panelLayout();
         int x = panel[0];
         int y = panel[1];
         int w = panel[2];
         int h = panel[3];
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, sw, sh), 1711276032);
         this.drawWindow(graphics, x, y, w, h, this.headerHeight(), AutismHudManager.displayName(this.id), mx, my);
         if (this.isTooSmall(w, h)) {
            return;
         }

         int bodyTop = this.bodyTop(panel);
         int bodyBottom = this.bodyBottom(panel);
         int viewH = this.viewHeight(panel);
         List<HudElementSettingsScreen.SettingRow> rows = this.settingRows();
         int contentH = this.rowsHeight(rows);
         this.scrollOffset = this.clamp(this.scrollOffset, 0, Math.max(0, contentH - viewH));
         boolean menuOpen = autismclient.gui.ui.widgets.DropdownWidget.hasOpenMenu(this.dropdowns);
         int hoverX = menuOpen ? -2147483648 : mx;
         int hoverY = menuOpen ? -2147483648 : my;
         this.dropdowns.clear();
         autismclient.gui.ui.UiClipStack.getInstance()
            .push(graphics, autismclient.gui.f.NetHelper.init(x + 2, bodyTop, Math.max(0, w - 9), Math.max(0, bodyBottom - bodyTop)));

         try {
            this.drawRows(graphics, rows, this.rowsX(panel), bodyTop - this.scrollOffset, this.rowsWidth(panel), hoverX, hoverY, bodyTop, bodyBottom);
            autismclient.gui.ui.widgets.DropdownWidget.renderTriggers(graphics, this.font, this.dropdowns, mx, my);
         } finally {
            autismclient.gui.ui.UiClipStack.getInstance().pop(graphics);
         }

         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(
            contentH, viewH, x + w - 6, bodyTop, 4, viewH, this.scrollOffset
         );
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.render(graphics, metrics, metrics.containsTrack(hoverX, hoverY), this.scrollbarDragging);
         int footerY = this.footerY(panel);
         int ctrlH = this.controlHeight();
         this.drawButton(graphics, "Back", x + 4, footerY, this.labelWidth("Back"), ctrlH, hoverX, hoverY, autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY);
         this.drawButton(
            graphics, "Reset", this.resetButtonX(x), footerY, this.labelWidth("Reset"), ctrlH, hoverX, hoverY, autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY
         );
         boolean enabled = AutismHudManager.getState(this.id).enabled;
         int switchX = x + w - 4 - 20;
         int labelW = autismclient.gui.f.b.k.init(this.font, "Enabled", THEME.init_Identifier_l(l.read), -791321);
         this.drawTrimmedText(graphics, "Enabled", this.enabledLabelX(panel), this.centeredTextY(footerY, ctrlH), enabled ? -791321 : labelTextColor(), labelW + 2);
         this.drawToggle(graphics, enabled, switchX, footerY, ctrlH, "hud-elem-settings:" + this.id + ":enabled");
         autismclient.gui.ui.widgets.DropdownWidget.renderOpenPopup(graphics, this.font, this.dropdowns, mx, my);
         if (this.modulePickerOpen) {
            this.drawModulePicker(graphics, sw, sh, mx, my);
         }

         if (this.colorPicker != null) {
            graphics.nextStratum();
            this.colorPicker.render(autismclient.gui.f.UiHelper.init(graphics, this.font, mx, my));
            if (!this.colorPicker.isOpen()) {
               this.closeColorPicker();
            }
         }
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void drawRows(GuiGraphicsExtractor graphics, List<HudElementSettingsScreen.SettingRow> rows, int x, int y, int w, int mx, int my, int clipTop, int clipBottom) {
      int cy = y;
      int rowStep = this.rowStep();
      int sectionStep = this.sectionStep();

      for (HudElementSettingsScreen.SettingRow row : rows) {
         if (row.isSectionRow()) {
            if (this.isRowVisible(cy, sectionStep, clipTop, clipBottom)) {
               this.drawSectionHeader(graphics, row.label, x, cy, w);
            }

            cy += sectionStep;
         } else {
            if (this.isRowVisible(cy, rowStep, clipTop, clipBottom)) {
               this.drawRow(graphics, row, x, cy, w, mx, my);
            }

            cy += rowStep;
         }
      }
   }

   private void drawSectionHeader(GuiGraphicsExtractor graphics, String label, int x, int y, int w) {
      int boxY = y + 2;
      int boxH = autismclient.gui.ui.UiDensityMetrics.scaledHeight(12);
      autismclient.gui.ui.widgets.PanelSurfaceHelper.fillRect(graphics, x, boxY, w, boxH, accentColor(1142492176));
      autismclient.gui.ui.widgets.PanelSurfaceHelper.drawDividerColored(graphics, x, boxY, w, accentColor(1720655652));
      autismclient.gui.ui.widgets.PanelSurfaceHelper.drawDividerColored(graphics, x, boxY + boxH - 1, w, accentColor(1720655652));
      this.drawTrimmedText(graphics, label, x + 4, this.centeredTextY(boxY, boxH), labelTextColor(), w - 8);
   }

   private void drawRow(GuiGraphicsExtractor graphics, HudElementSettingsScreen.SettingRow row, int x, int y, int w, int mx, int my) {
      int rowH = this.rowHeight();
      int ctrlH = this.inputHeight();
      int ctrlY = y + (rowH - ctrlH) / 2;
      int textY = this.centeredTextY(y, rowH);
      boolean over = this.inside(mx, my, x, y, w, rowH);
      autismclient.gui.ui.widgets.PanelSurfaceHelper.fillRect(graphics, x, y, w, rowH, over ? accentColor(1714756383) : 856888344);
      int labelColW = this.labelColumnWidth();
      this.drawTrimmedText(graphics, row.label, x + 4, textY, -791321, labelColW - 8);
      int valueX = x + labelColW;
      int valueW = this.valueWidth(w);
      if (row.type == HudElementSettingsScreen.RowType.BOOL) {
         boolean on = this.isSettingEnabled(row.key);
         this.drawToggle(graphics, on, x + w - 4 - 20, y, rowH, "hud-elem-settings:" + this.id + ":" + row.key);
      } else if (row.type == HudElementSettingsScreen.RowType.ENUM) {
         this.renderChoiceRow(row, valueX, ctrlY, valueW, ctrlH);
      } else if (row.type == HudElementSettingsScreen.RowType.COLOR) {
         int pickW = this.textButtonWidth("Pick");
         int color = AutismHudManager.parseColor(AutismHudManager.setting(this.id, row.key), this.defaultColor(row.key));
         int swatchX = valueX;
         int swatchW = 24;
         autismclient.gui.f.PacketUtil.load(
            graphics, autismclient.gui.f.NetHelper.init(swatchX, ctrlY + 1, swatchW, ctrlH - 2), 3, color | 0xFF000000, THEME.set()
         );
         int hexX = swatchX + swatchW + 4;
         this.drawTrimmedText(graphics, "#" + String.format(Locale.ROOT, "%06X", color & 16777215), hexX, textY, -791321, Math.max(1, valueX + valueW - pickW - 4 - hexX));
         this.drawButton(graphics, "Pick", valueX + valueW - pickW, ctrlY, pickW, ctrlH, mx, my, autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY);
      } else if (row.type == HudElementSettingsScreen.RowType.MODULE_LIST) {
         String editLabel = "Edit (" + this.hiddenModuleIds().size() + ")";
         int editW = this.textButtonWidth(editLabel);
         this.drawButton(graphics, editLabel, valueX + valueW - editW, ctrlY, editW, ctrlH, mx, my, autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY);
      } else if (row.type == HudElementSettingsScreen.RowType.NUMBER) {
         double value = AutismHudManager.doubleSetting(this.id, row.key, row.min);
         this.drawSlider(
            graphics,
            row,
            valueX,
            ctrlY,
            valueW,
            ctrlH,
            value,
            row.key != null && row.key.equals(this.editingKey) || row == this.row,
            mx,
            my
         );
      } else if (row.type == HudElementSettingsScreen.RowType.ITEM) {
         int pickW = this.textButtonWidth("Pick");
         String raw = AutismHudManager.setting(this.id, row.key);
         String itemName = raw != null && !raw.isBlank() ? RegistryDisplayNameCache.itemName(raw.contains(":") ? raw : "minecraft:" + raw) : "None";
         this.drawTrimmedText(graphics, itemName, valueX, textY, -791321, Math.max(1, valueW - pickW - 4));
         this.drawButton(graphics, "Pick", valueX + valueW - pickW, ctrlY, pickW, ctrlH, mx, my, autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY);
      } else if (row.type == HudElementSettingsScreen.RowType.PICK) {
         String buttonLabel = row.choices.isEmpty() ? "Pick…" : row.choices.get(0);
         int pickW = this.textButtonWidth(buttonLabel);
         this.drawTrimmedText(graphics, fileNameOf(AutismHudManager.setting(this.id, row.key)), valueX, textY, -791321, Math.max(1, valueW - pickW - 4));
         this.drawButton(graphics, buttonLabel, valueX + valueW - pickW, ctrlY, pickW, ctrlH, mx, my, autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY);
      }
   }

   private void renderChoiceRow(HudElementSettingsScreen.SettingRow row, int x, int y, int w, int h) {
      List<String> choices = row.choices;
      String current = AutismHudManager.setting(this.id, row.key);
      int selected = Math.max(0, choices.indexOf(current));
      int choiceW = 0;

      for (String choice : choices) {
         choiceW = Math.max(choiceW, autismclient.gui.f.b.k.init(this.font, choice, THEME.init_Identifier_l(l.read), 0));
      }

      int dw = Math.min(w, Math.max(56, choiceW + 26));
      int dx = x + w - dw;
      autismclient.gui.ui.widgets.DropdownWidget dropdown = this.dropdownsByKey
         .computeIfAbsent(row.key, ignored -> new autismclient.gui.ui.widgets.DropdownWidget(dx, y, dw, h, choices, selected, index -> {}));
      dropdown.setBounds(dx, y, dw, h).setOptions(choices).setSelectedIndex(selected).setOnSelect(index -> {
         AutismHudManager.setSetting(this.id, row.key, choices.get(index));
         this.cancelTextEdit();
      });
      this.dropdowns.add(dropdown);
   }

   private void drawSlider(GuiGraphicsExtractor graphics, HudElementSettingsScreen.SettingRow row, int x, int y, int w, int h, double value, boolean editing, int mx, int my) {
      int fieldW = 40;
      int sliderW = this.sliderWidth(w);
      int fieldX = x + sliderW + 4;
      double ratio = autismclient.gui.ui.widgets.SliderRenderer.ratio(value, row.min, row.max);
      autismclient.gui.ui.widgets.SliderRenderer.draw(
         autismclient.gui.f.UiHelper.init(graphics, this.font, x, y), autismclient.gui.f.NetHelper.init(x, y, sliderW, h), ratio, editing
      );
      if (editing) {
         this.placeTextInput(fieldX, y, fieldW, h);
         this.textInput.render(graphics, mx, my, 0.0F);
      } else {
         autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(fieldX, y, fieldW, h), 4, -15594224, THEME.set());
         this.drawBoxText(graphics, this.sliderValueText(row, value), fieldX, y, fieldW, h, -791321);
      }
   }

   private void placeTextInput(int x, int y, int w, int h) {
      if (this.textInput == null) {
         this.textInput = new ThemedTextInputBox(Minecraft.getInstance(), this.font, x, y, w, h, false);
      }

      this.textInput.setX(x);
      this.textInput.setY(y);
      this.textInput.setWidth(w);
      this.textInput.setHeight(h);
   }

   private void beginTextEdit(String key, String value) {
      this.editingKey = key;
      if (this.textInput == null) {
         this.textInput = new ThemedTextInputBox(Minecraft.getInstance(), this.font, 0, 0, 20, this.inputHeight(), false);
      }

      this.textInput.setValue(value == null ? "" : value);
      this.textInput.setFocused(true);
      this.textInput.setSelectionEnd(this.textInput.getValue().length());
   }

   private void drawToggle(GuiGraphicsExtractor graphics, boolean checked, int x, int y, int boxH, String animationKey) {
      int pillY = y + (boxH - 10) / 2;
      ToggleSwitchRenderer.draw(autismclient.gui.f.UiHelper.init(graphics, this.font, x, pillY), autismclient.gui.f.NetHelper.init(x, pillY, 20, 10), checked, false, animationKey);
   }

   private void drawButton(
      GuiGraphicsExtractor graphics, String label, int x, int y, int w, int h, int mx, int my, autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone variant
   ) {
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(graphics, this.font, x, y, w, h, label, variant, true, mx, my);
   }

   private int labelWidth(String label) {
      return this.textButtonWidth(label);
   }

   private int textButtonWidth(String label) {
      return autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(autismclient.gui.f.b.k.init(this.font, label, THEME.init_Identifier_l(l.read), 0));
   }

   private static String fileNameOf(String path) {
      if (path != null && !path.isBlank()) {
         int slash = Math.max(path.lastIndexOf(47), path.lastIndexOf(92));
         return slash >= 0 ? path.substring(slash + 1) : path;
      } else {
         return "None";
      }
   }

   private int resetButtonX(int panelX) {
      return panelX + 4 + this.labelWidth("Back") + 2;
   }

   private int enabledLabelX(int[] panel) {
      int labelW = autismclient.gui.f.b.k.init(this.font, "Enabled", THEME.init_Identifier_l(l.read), -791321);
      return panel[0] + panel[2] - 4 - 20 - 4 - labelW;
   }

   private void drawModulePicker(GuiGraphicsExtractor graphics, int sw, int sh, int mx, int my) {
      List<Module> modules = this.allModulesList();
      int[] picker = this.modulePickerLayout(modules.size());
      int x = picker[0];
      int y = picker[1];
      int w = picker[2];
      int h = picker[3];
      int viewTop = picker[4];
      int viewBottom = picker[5];
      int rowStep = this.rowStep();
      int rowH = this.rowHeight();
      this.pickerScroll = this.clamp(this.pickerScroll, 0, this.modulePickerMaxScroll());
      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, sw, sh), 1140850688);
      this.drawWindow(graphics, x, y, w, h, this.headerHeight(), "Hidden Modules", mx, my);
      autismclient.gui.ui.UiClipStack.getInstance()
         .push(graphics, autismclient.gui.f.NetHelper.init(x + 2, viewTop, Math.max(0, w - 4), Math.max(0, viewBottom - viewTop)));

      try {
         List<String> hidden = this.hiddenModuleIds();
         int cy = viewTop - this.pickerScroll;
         int rowX = x + 4;
         int rowW = w - 8;

         for (Module module : modules) {
            if (this.isRowVisible(cy, rowStep, viewTop, viewBottom)) {
               boolean selected = hidden.contains(module.id());
               boolean over = this.inside(mx, my, rowX, cy, rowW, rowH);
               autismclient.gui.ui.widgets.PanelSurfaceHelper.fillRect(graphics, rowX, cy, rowW, rowH, over ? accentColor(1714756383) : 856888344);
               this.drawTrimmedText(graphics, module.name(), rowX + 4, this.centeredTextY(cy, rowH), selected ? -791321 : labelTextColor(), rowW - 20 - 12);
               this.drawToggle(graphics, selected, rowX + rowW - 4 - 20, cy, rowH, "hud-hidden-modules:" + module.id());
            }

            cy += rowStep;
         }
      } finally {
         autismclient.gui.ui.UiClipStack.getInstance().pop(graphics);
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      int[] panel = this.panelLayout();
      int x = panel[0];
      int y = panel[1];
      int w = panel[2];
      int h = panel[3];
      if (Compat263.mouseEventGlfwButton(event) != 0 && Compat263.mouseEventGlfwButton(event) != 1) {
         return true;
      }

      if (this.colorPicker != null) {
         this.colorPicker.mouseClicked(mx, my, Compat263.mouseEventGlfwButton(event));
         if (!this.colorPicker.isOpen()) {
            this.closeColorPicker();
         }

         return true;
      } else {
         if (this.modulePickerOpen) {
            this.handleModulePickerClick(mx, my);
            return true;
         }

         if (this.isOverDialogCloseButton(mx, my, x, y, w, h, this.headerHeight())) {
            this.minecraft.gui.setScreen(this.parent);
            return true;
         }

         if (autismclient.gui.ui.widgets.DropdownWidget.handleMouseClick(this.dropdowns, mx, my, Compat263.mouseEventGlfwButton(event))) {
            this.cancelTextEdit();
            return true;
         }

         if (autismclient.gui.ui.widgets.DropdownWidget.hasOpenMenu(this.dropdowns)) {
            return true;
         }

         if (this.editingKey != null && this.textInput != null && this.textInput.mouseClicked(mx, my, Compat263.mouseEventGlfwButton(event))) {
            return true;
         }

         int bodyTop = this.bodyTop(panel);
         int bodyBottom = this.bodyBottom(panel);
         int viewH = this.viewHeight(panel);
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(
            this.rowsHeight(this.settingRows()), viewH, x + w - 6, bodyTop, 4, viewH, this.scrollOffset
         );
         if (metrics.isScrollable() && metrics.containsTrack(mx, my)) {
            this.scrollbarDragging = true;
            this.scrollGrabOffset = metrics.containsThumb(mx, my) ? my - metrics.thumbY() : metrics.thumbHeight() / 2;
            this.scrollOffset = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(metrics, my, this.scrollGrabOffset);
            return true;
         }

         int footerY = this.footerY(panel);
         int ctrlH = this.controlHeight();
         if (this.inside(mx, my, x + 4, footerY, this.labelWidth("Back"), ctrlH)) {
            this.minecraft.gui.setScreen(this.parent);
            return true;
         }

         if (this.inside(mx, my, this.resetButtonX(x), footerY, this.labelWidth("Reset"), ctrlH)) {
            AutismHudManager.resetElement(this.id);
            this.cancelTextEdit();
            return true;
         }

         int toggleX = this.enabledLabelX(panel);
         if (this.inside(mx, my, toggleX, footerY, x + w - 4 - toggleX, ctrlH)) {
            AutismHudManager.toggleEnabled(this.id);
            return true;
         }

         if (my >= bodyTop && my < bodyBottom) {
            int rowsX = this.rowsX(panel);
            int rowsW = this.rowsWidth(panel);
            HudElementSettingsScreen.SettingRow row = this.rowAt(mx, my, rowsX, bodyTop - this.scrollOffset, rowsW, bodyTop, bodyBottom);
            if (row != null && !row.isSectionRow()) {
               if (row.type != HudElementSettingsScreen.RowType.ENUM) {
                  this.handleRowClick(row, Compat263.mouseEventGlfwButton(event), mx, my, rowsX + this.labelColumnWidth(), this.valueWidth(rowsW));
               }

               return true;
            } else {
               this.cancelTextEdit();
               return true;
            }
         } else {
            this.cancelTextEdit();
            return true;
         }
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      this.scrollbarDragging = false;
      this.row = null;
      if (this.colorPicker != null) {
         int mx = cA.init_I_D(event.x());
         int my = cA.init_I_D(event.y());
         this.colorPicker.mouseReleased(mx, my, Compat263.mouseEventGlfwButton(event));
         if (!this.colorPicker.isOpen()) {
            this.closeColorPicker();
         }

         return true;
      } else {
         if (autismclient.gui.ui.widgets.DropdownWidget.handleMouseRelease(this.dropdowns)) {
            return true;
         }

         if (this.textInput != null) {
            this.textInput.mouseReleased(cA.init_I_D(event.x()), cA.init_I_D(event.y()), Compat263.mouseEventGlfwButton(event));
         }

         return true;
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      if (this.row != null) {
         int[] panel = this.panelLayout();
         int valueX = this.rowsX(panel) + this.labelColumnWidth();
         int valueW = this.valueWidth(this.rowsWidth(panel));
         this.setSliderValue(
            this.row,
            autismclient.gui.ui.widgets.SliderRenderer.valueAt(
               mx, valueX, this.sliderWidth(valueW), this.row.min, this.row.max, this.row.step
            )
         );
         return true;
      }

      if (this.colorPicker != null) {
         this.colorPicker.mouseDragged(mx, my, Compat263.mouseEventGlfwButton(event), dx, dy);
         if (!this.colorPicker.isOpen()) {
            this.closeColorPicker();
         }

         return true;
      } else {
         if (autismclient.gui.ui.widgets.DropdownWidget.handleMouseDrag(this.dropdowns, mx, my, Compat263.mouseEventGlfwButton(event))) {
            return true;
         }

         if (this.editingKey != null && this.textInput != null && this.textInput.mouseDragged(mx, my, Compat263.mouseEventGlfwButton(event), dx, dy)) {
            return true;
         }

         if (!this.scrollbarDragging) {
            return true;
         }

         int[] panel = this.panelLayout();
         int bodyTop = this.bodyTop(panel);
         int viewH = this.viewHeight(panel);
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(
            this.rowsHeight(this.settingRows()), viewH, panel[0] + panel[2] - 6, bodyTop, 4, viewH, this.scrollOffset
         );
         this.scrollOffset = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(metrics, my, this.scrollGrabOffset);
         return true;
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      double virtualX = cA.init_D_D(mouseX);
      double virtualY = cA.init_D_D(mouseY);
      if (this.colorPicker != null) {
         this.colorPicker.mouseScrolled((int)Math.round(virtualX), (int)Math.round(virtualY), scrollY);
         return true;
      } else if (autismclient.gui.ui.widgets.DropdownWidget.handleMouseScroll(this.dropdowns, virtualX, virtualY, scrollY)) {
         return true;
      } else if (this.modulePickerOpen) {
         this.pickerScroll = this.clamp(this.pickerScroll + (scrollY < 0.0 ? this.rowStep() : -this.rowStep()), 0, this.modulePickerMaxScroll());
         return true;
      } else {
         int[] panel = this.panelLayout();
         int contentH = this.rowsHeight(this.settingRows());
         this.scrollOffset = this.clamp(this.scrollOffset + (scrollY < 0.0 ? this.rowStep() : -this.rowStep()), 0, Math.max(0, contentH - this.viewHeight(panel)));
         return true;
      }
   }

   @Override
   public boolean keyPressed(KeyEvent input) {
      if (this.colorPicker != null) {
         this.colorPicker.keyPressed(Compat263.keyEventGlfwKey(input), input.keycode(), Compat263.keyEventGlfwModifiers(input));
         if (!this.colorPicker.isOpen()) {
            this.closeColorPicker();
         }

         return true;
      } else if (Compat263.keyEventGlfwKey(input) == 256) {
         if (this.modulePickerOpen) {
            this.modulePickerOpen = false;
         } else if (this.editingKey != null) {
            this.cancelTextEdit();
         } else {
            this.minecraft.gui.setScreen(this.parent);
         }

         return true;
      } else {
         if (this.editingKey != null) {
            if (Compat263.keyEventGlfwKey(input) == 257 || Compat263.keyEventGlfwKey(input) == 335) {
               this.commitTextEdit();
               return true;
            }

            if (this.textInput != null && this.textInput.keyPressed(input)) {
               return true;
            }
         }

         return true;
      }
   }

   @Override
   public boolean charTyped(CharacterEvent input) {
      if (this.colorPicker != null) {
         this.colorPicker.charTyped((char)input.codepoint());
         if (!this.colorPicker.isOpen()) {
            this.closeColorPicker();
         }

         return true;
      } else {
         if (this.editingKey != null && this.textInput != null) {
            this.textInput.charTyped(input);
         }

         return true;
      }
   }

   private void handleRowClick(HudElementSettingsScreen.SettingRow row, int button, int mx, int my, int valueX, int valueW) {
      int dir = button == 1 ? -1 : 1;
      if (row.type == HudElementSettingsScreen.RowType.BOOL) {
         AutismHudManager.setSetting(this.id, row.key, Boolean.toString(!this.isSettingEnabled(row.key)));
         this.cancelTextEdit();
      } else if (row.type == HudElementSettingsScreen.RowType.COLOR) {
         this.openColorPickerFor(row.key, autismclient.gui.f.NetHelper.init(valueX, Math.max(0, my - this.rowHeight() / 2), valueW, this.rowHeight()));
         this.cancelTextEdit();
      } else if (row.type == HudElementSettingsScreen.RowType.MODULE_LIST) {
         this.modulePickerOpen = true;
         this.pickerScroll = 0;
         this.cancelTextEdit();
      } else if (row.type == HudElementSettingsScreen.RowType.NUMBER) {
         int fieldX = valueX + this.sliderWidth(valueW) + 4;
         if (mx >= fieldX && mx < fieldX + 40) {
            this.beginTextEdit(row.key, AutismHudManager.setting(this.id, row.key));
            return;
         }

         double value = autismclient.gui.ui.widgets.SliderRenderer.valueAt(mx, valueX, this.sliderWidth(valueW), row.min, row.max, row.step);
         this.setSliderValue(row, value);
         this.row = row;
         this.cancelTextEdit();
      } else if (row.type == HudElementSettingsScreen.RowType.ITEM) {
         this.cancelTextEdit();
         this.minecraft
            .gui
            .setScreen(new PickItemScreen(this, AutismHudManager.setting(this.id, row.key), valuex -> AutismHudManager.setSetting(this.id, row.key, valuex)));
      } else if (row.type == HudElementSettingsScreen.RowType.PICK) {
         this.cancelTextEdit();
         String elementId = this.id;
         String settingId = row.key;
         NativeImageFilePicker.pickImage(AutismHudManager.setting(elementId, settingId), picked -> {
            AutismHudManager.setSetting(elementId, settingId, picked);
            AutismHudManager.setSetting(elementId, "gif-natural", "true");
         });
      }
   }

   private void openColorPickerFor(String key, autismclient.gui.f.NetHelper anchor) {
      if (key != null && !key.isBlank()) {
         this.unusedColorPickerKey = key;
         this.modulePickerOpen = false;
         autismclient.gui.ui.widgets.DropdownWidget.closeAll(this.dropdowns);
         this.cancelTextEdit();
         int fallback = this.defaultColor(key);
         int initial = AutismHudManager.parseColor(AutismHudManager.setting(this.id, key), fallback);
         this.colorPicker = new autismclient.gui.ui.widgets.ColorPickerPopup(anchor, initial, fallback, cA.init_I(), cA.load_I(), argb -> {
            AutismHudManager.setSetting(this.id, key, String.format(Locale.ROOT, "%08X", argb));
            this.closeColorPicker();
         });
      }
   }

   private void closeColorPicker() {
      if (this.colorPicker != null) {
         this.colorPicker.dismiss();
      }

      this.colorPicker = null;
      this.unusedColorPickerKey = null;
   }

   private int defaultColor(String key) {
      return AutismHudManager.parseColor(AutismHudManager.defaultSetting(this.id, key), -1);
   }

   private void setSliderValue(HudElementSettingsScreen.SettingRow row, double value) {
      double clamped = Math.max(row.min, Math.min(row.max, value));
      AutismHudManager.setSetting(this.id, row.key, this.formatSliderValue(clamped, row.step));
   }

   private void commitTextEdit() {
      HudElementSettingsScreen.SettingRow row = this.rowForKey(this.editingKey);
      if (row == null) {
         this.cancelTextEdit();
      } else {
         String editingText = this.textInput == null ? "" : this.textInput.getValue();
         if (row.type == HudElementSettingsScreen.RowType.NUMBER) {
            try {
               this.setSliderValue(row, Double.parseDouble(editingText.trim()));
            } catch (NumberFormatException var4) {
            }
         }

         this.cancelTextEdit();
      }
   }

   private void cancelTextEdit() {
      this.editingKey = null;
      if (this.textInput != null) {
         this.textInput.setFocused(false);
         this.textInput.setValue("");
      }
   }

   private HudElementSettingsScreen.SettingRow rowAt(int mx, int my, int x, int y, int w, int clipTop, int clipBottom) {
      int cy = y;
      int rowH = this.rowHeight();

      for (HudElementSettingsScreen.SettingRow row : this.settingRows()) {
         int h = row.isSectionRow() ? this.sectionStep() : this.rowStep();
         if (!row.isSectionRow() && my >= clipTop && my < clipBottom && this.inside(mx, my, x, cy, w, rowH)) {
            return row;
         }

         cy += h;
      }

      return null;
   }

   private HudElementSettingsScreen.SettingRow rowForKey(String key) {
      if (key == null) {
         return null;
      }

      for (HudElementSettingsScreen.SettingRow row : this.settingRows()) {
         if (key.equals(row.key)) {
            return row;
         }
      }

      return null;
   }

   private List<HudElementSettingsScreen.SettingRow> settingRows() {
      long rev = settingsRevision();
      HudElementSettingsScreen.CachedRows cached = rowsCache.get(this.id);
      if (cached != null && cached.rev == rev) {
         return cached.rows;
      }

      List<HudElementSettingsScreen.SettingRow> rows = new ArrayList<>();
      if ("bed_danger".equals(this.id)) {
         rows.add(HudElementSettingsScreen.SettingRow.sectionRow("Drag Bed Danger in the HUD Editor to position it."));
         rows.add(HudElementSettingsScreen.SettingRow.sectionRow("Distance and size: Danger > Bed Danger."));
         return List.copyOf(rows);
      }

      boolean isArrayList = "active_modules".equals(this.id);
      boolean isCompass = "compass".equals(this.id);
      boolean isKeystrokes = "keystrokes".equals(this.id);
      boolean isSlots = "armor".equals(this.id) || "inventory".equals(this.id);
      boolean isAddon = HudElements.isAddon(this.id);
      boolean background = AutismHudManager.boolSetting(this.id, "background");
      boolean outline = AutismHudManager.boolSetting(this.id, "outline");
      if (("tps".equals(this.id) || "ping".equals(this.id)) && AutismHudManager.getState("fps").enabled) {
         rows.add(HudElementSettingsScreen.SettingRow.sectionRow("Shown in the FPS element while it is on"));
         List<HudElementSettingsScreen.SettingRow> merged = List.copyOf(rows);
         rowsCache.put(this.id, new HudElementSettingsScreen.CachedRows(rev, merged));
         return merged;
      }

      boolean customMatters;
      if (isArrayList) {
         customMatters = false;
      } else if (isCompass || isKeystrokes || isAddon) {
         customMatters = true;
      } else if (!"watermark".equals(this.id) && !"gif".equals(this.id) && !isSlots) {
         if ("fps_graph".equals(this.id)) {
            customMatters = background || outline || AutismHudManager.boolSetting(this.id, "show-current-fps");
         } else {
            customMatters = true;
         }
      } else {
         customMatters = background || outline;
      }

      boolean customColors = isArrayList || AutismHudManager.boolSetting(this.id, "use-custom-colors");
      rows.add(HudElementSettingsScreen.SettingRow.sectionRow("Visibility"));
      if (!this.usesCustomLayout() && !"breaking_progress".equals(this.id)) {
         rows.add(HudElementSettingsScreen.SettingRow.enumRow("alignment", "Alignment", "Left", "Center", "Right"));
      }

      if (customMatters) {
         rows.add(HudElementSettingsScreen.SettingRow.boolRow("use-custom-colors", "Custom Colors"));
      }

      if ("watermark".equals(this.id)) {
         rows.add(HudElementSettingsScreen.SettingRow.sectionRow("Logo"));
         rows.add(HudElementSettingsScreen.SettingRow.sliderRow("logo-width", "Width", HudElementSettingsScreen.RowType.NUMBER, 48.0, 420.0, 1.0));
      } else if ("gif".equals(this.id)) {
         rows.add(HudElementSettingsScreen.SettingRow.sectionRow("GIF"));
         rows.add(HudElementSettingsScreen.SettingRow.buttonRow("gif-path", "Image", "Pick…"));
         rows.add(HudElementSettingsScreen.SettingRow.sliderRow("gif-width", "Width", HudElementSettingsScreen.RowType.NUMBER, 16.0, 512.0, 1.0));
         rows.add(HudElementSettingsScreen.SettingRow.sliderRow("gif-height", "Height", HudElementSettingsScreen.RowType.NUMBER, 16.0, 512.0, 1.0));
         if (!AutismHudManager.setting(this.id, "gif-path").isBlank()) {
            rows.add(HudElementSettingsScreen.SettingRow.sliderRow("gif-fps", "Framerate (FPS)", HudElementSettingsScreen.RowType.NUMBER, 0.0, 60.0, 1.0));
            rows.add(HudElementSettingsScreen.SettingRow.enumRow("gif-quality", "Quality", "Low", "Medium", "High", "Original"));
            rows.add(HudElementSettingsScreen.SettingRow.boolRow("gif-natural", "Match File Aspect"));
         }

         rows.add(HudElementSettingsScreen.SettingRow.boolRow("gif-keep-aspect", "Lock Ratio When Resizing"));
      } else if (customColors && customMatters) {
         rows.add(HudElementSettingsScreen.SettingRow.sectionRow("Text"));
         if (isCompass) {
            rows.add(HudElementSettingsScreen.SettingRow.color("label-color", "Letter Color"));
            rows.add(HudElementSettingsScreen.SettingRow.color("accent-color", "Facing Color"));
         } else if (isAddon) {
            rows.add(HudElementSettingsScreen.SettingRow.color("label-color", "Label Color"));
            rows.add(HudElementSettingsScreen.SettingRow.color("value-color", "Value Color"));
            rows.add(HudElementSettingsScreen.SettingRow.color("accent-color", "Accent Color"));
         } else if ("fps_graph".equals(this.id)) {
            if (AutismHudManager.boolSetting(this.id, "show-current-fps")) {
               rows.add(HudElementSettingsScreen.SettingRow.color("value-color", "Value Color"));
            }
         } else if (!isKeystrokes && !isSlots) {
            boolean labelShown = !"item_counter".equals(this.id) || AutismHudManager.boolSetting(this.id, "item-show-name");
            boolean valueOverridden = "tps".equals(this.id) && AutismHudManager.boolSetting(this.id, "tps-color-threshold")
               || "ping".equals(this.id) && AutismHudManager.boolSetting(this.id, "ping-color-threshold");
            if (labelShown) {
               rows.add(HudElementSettingsScreen.SettingRow.color("label-color", "Label Color"));
            }

            if (!valueOverridden) {
               rows.add(HudElementSettingsScreen.SettingRow.color("value-color", "Value Color"));
            }
         }
      }

      rows.add(HudElementSettingsScreen.SettingRow.sectionRow("Background"));
      rows.add(HudElementSettingsScreen.SettingRow.boolRow("background", "Background"));
      if (customColors && background) {
         rows.add(HudElementSettingsScreen.SettingRow.color("background-color", "Background Color"));
      }

      rows.add(HudElementSettingsScreen.SettingRow.sectionRow("Outline"));
      rows.add(HudElementSettingsScreen.SettingRow.boolRow("outline", "Outline"));
      if (outline) {
         if (customColors) {
            rows.add(HudElementSettingsScreen.SettingRow.color("outline-color", "Outline Color"));
         }

         boolean flatSlots = isSlots && "Flat".equals(AutismHudManager.setting(this.id, "slot-style"));
         if (!flatSlots) {
            rows.add(HudElementSettingsScreen.SettingRow.sliderRow("outline-width", "Outline Width", HudElementSettingsScreen.RowType.NUMBER, 1.0, 6.0, 1.0));
         }
      }

      if (isSlots) {
         rows.add(HudElementSettingsScreen.SettingRow.sectionRow("Slots"));
         rows.add(HudElementSettingsScreen.SettingRow.enumRow("slot-style", "Slot Style", "Textured", "Flat"));
      }

      if (isArrayList) {
         rows.add(HudElementSettingsScreen.SettingRow.sectionRow("Active Modules"));
         rows.add(HudElementSettingsScreen.SettingRow.boolRow("module-info", "Module Info"));
         rows.add(HudElementSettingsScreen.SettingRow.boolRow("show-keybind", "Show Keybind"));
         rows.add(HudElementSettingsScreen.SettingRow.enumRow("sort", "Sort", "Width", "Name", "Category"));
         rows.add(HudElementSettingsScreen.SettingRow.enumRow("color-mode", "Color Mode", "Flat", "Random", "Rainbow", "Gradient"));
         String mode = AutismHudManager.setting(this.id, "color-mode");
         if ("Flat".equals(mode)) {
            rows.add(HudElementSettingsScreen.SettingRow.color("flat-color", "Flat Color"));
         }

         if ("Gradient".equals(mode)) {
            rows.add(HudElementSettingsScreen.SettingRow.color("gradient-start-color", "Gradient Start"));
            rows.add(HudElementSettingsScreen.SettingRow.color("gradient-end-color", "Gradient End"));
         }

         if ("Rainbow".equals(mode)) {
            rows.add(HudElementSettingsScreen.SettingRow.color("value-color", "Rainbow Tint"));
         }

         if (AutismHudManager.boolSetting(this.id, "module-info") || AutismHudManager.boolSetting(this.id, "show-keybind")) {
            rows.add(HudElementSettingsScreen.SettingRow.color("module-info-color", "Info Color"));
         }

         if ("Rainbow".equals(mode) || "Gradient".equals(mode)) {
            rows.add(HudElementSettingsScreen.SettingRow.sliderRow("rainbow-speed", "Animation Speed", HudElementSettingsScreen.RowType.NUMBER, 0.1, 2.0, 0.05));
            rows.add(HudElementSettingsScreen.SettingRow.sliderRow("rainbow-spread", "Row Spread", HudElementSettingsScreen.RowType.NUMBER, 0.001, 0.12, 0.001));
            rows.add(HudElementSettingsScreen.SettingRow.enumRow("rainbow-direction", "Direction", "Forward", "Reverse"));
         }

         if ("Rainbow".equals(mode)) {
            rows.add(HudElementSettingsScreen.SettingRow.sliderRow("rainbow-saturation", "Saturation", HudElementSettingsScreen.RowType.NUMBER, 0.0, 0.35, 0.05));
            rows.add(HudElementSettingsScreen.SettingRow.sliderRow("rainbow-brightness", "Brightness", HudElementSettingsScreen.RowType.NUMBER, 0.0, 1.0, 0.05));
         }

         rows.add(HudElementSettingsScreen.SettingRow.sliderRow("stair-snap", "Stair Snap", HudElementSettingsScreen.RowType.NUMBER, 0.0, 24.0, 1.0));
         rows.add(HudElementSettingsScreen.SettingRow.moduleList("hidden-modules", "Hidden Modules"));
      }

      if ("tps".equals(this.id)) {
         rows.add(HudElementSettingsScreen.SettingRow.sectionRow("TPS"));
         rows.add(HudElementSettingsScreen.SettingRow.boolRow("tps-precise", "Precise"));
         rows.add(HudElementSettingsScreen.SettingRow.boolRow("tps-color-threshold", "Color Thresholds"));
         rows.add(HudElementSettingsScreen.SettingRow.boolRow("tps-show-jitter", "Show Min"));
      }

      if ("ping".equals(this.id)) {
         rows.add(HudElementSettingsScreen.SettingRow.sectionRow("Ping"));
         rows.add(HudElementSettingsScreen.SettingRow.boolRow("ping-color-threshold", "Color Thresholds"));
         rows.add(HudElementSettingsScreen.SettingRow.boolRow("ping-show-jitter", "Show Jitter"));
      }

      if ("cps".equals(this.id)) {
         rows.add(HudElementSettingsScreen.SettingRow.sectionRow("CPS"));
         rows.add(HudElementSettingsScreen.SettingRow.boolRow("show-total", "Show Total"));
      }

      if ("durability".equals(this.id)) {
         rows.add(HudElementSettingsScreen.SettingRow.sectionRow("Durability"));
         rows.add(HudElementSettingsScreen.SettingRow.boolRow("show-item-name", "Show Item Name"));
         rows.add(HudElementSettingsScreen.SettingRow.boolRow("low-durability-warn", "Low Durability Warning"));
      }

      if ("looking_at".equals(this.id)) {
         rows.add(HudElementSettingsScreen.SettingRow.sectionRow("Looking At"));
         rows.add(HudElementSettingsScreen.SettingRow.boolRow("show-distance", "Show Distance"));
         rows.add(HudElementSettingsScreen.SettingRow.boolRow("show-block-id", "Show Block ID"));
      }

      if ("breaking_progress".equals(this.id)) {
         rows.add(HudElementSettingsScreen.SettingRow.sectionRow("Breaking Progress"));
         rows.add(HudElementSettingsScreen.SettingRow.boolRow("show-block-name", "Show Block Name"));
      }

      if ("weather".equals(this.id)) {
         rows.add(HudElementSettingsScreen.SettingRow.sectionRow("Weather"));
         rows.add(HudElementSettingsScreen.SettingRow.boolRow("show-temperature", "Show Temperature"));
      }

      if ("world_time".equals(this.id)) {
         rows.add(HudElementSettingsScreen.SettingRow.sectionRow("World Time"));
         rows.add(HudElementSettingsScreen.SettingRow.enumRow("world-time-format", "Format", "24h", "ticks"));
         rows.add(HudElementSettingsScreen.SettingRow.boolRow("show-day", "Show Day"));
      }

      if ("real_time".equals(this.id)) {
         rows.add(HudElementSettingsScreen.SettingRow.sectionRow("Real Time"));
         rows.add(HudElementSettingsScreen.SettingRow.enumRow("real-time-format", "Format", "12h", "24h"));
         rows.add(HudElementSettingsScreen.SettingRow.boolRow("show-date", "Show Date"));
      }

      if ("memory".equals(this.id)) {
         rows.add(HudElementSettingsScreen.SettingRow.sectionRow("Memory"));
         rows.add(HudElementSettingsScreen.SettingRow.enumRow("memory-format", "Format", "used/max", "used%"));
         rows.add(HudElementSettingsScreen.SettingRow.boolRow("show-bar", "Show Bar"));
         if ("used/max".equals(AutismHudManager.setting(this.id, "memory-format"))) {
            rows.add(HudElementSettingsScreen.SettingRow.boolRow("show-percent", "Show Percentage"));
         }
      }

      if ("server_ip".equals(this.id)) {
         rows.add(HudElementSettingsScreen.SettingRow.sectionRow("Server IP"));
         rows.add(HudElementSettingsScreen.SettingRow.boolRow("show-port", "Show Port"));
      }

      if ("fps_graph".equals(this.id)) {
         rows.add(HudElementSettingsScreen.SettingRow.sectionRow("FPS Graph"));
         rows.add(HudElementSettingsScreen.SettingRow.sliderRow("graph-samples", "Samples", HudElementSettingsScreen.RowType.NUMBER, 50.0, 200.0, 10.0));
         rows.add(HudElementSettingsScreen.SettingRow.boolRow("show-current-fps", "Show Current FPS"));
      }

      if ("item_counter".equals(this.id)) {
         rows.add(HudElementSettingsScreen.SettingRow.sectionRow("Item Counter"));
         rows.add(HudElementSettingsScreen.SettingRow.boolRow("item-count-held", "Count Held Item"));
         rows.add(HudElementSettingsScreen.SettingRow.boolRow("item-show-name", "Show Item Name"));
         if (!AutismHudManager.boolSetting(this.id, "item-count-held")) {
            rows.add(HudElementSettingsScreen.SettingRow.item("item-id", "Item"));
         }
      }

      if (isKeystrokes) {
         rows.add(HudElementSettingsScreen.SettingRow.sectionRow("Keystrokes"));
         rows.add(HudElementSettingsScreen.SettingRow.sliderRow("keystroke-size", "Key Size", HudElementSettingsScreen.RowType.NUMBER, 12.0, 40.0, 1.0));
         if (customColors) {
            rows.add(HudElementSettingsScreen.SettingRow.color("keystroke-active-color", "Fill Color"));
            rows.add(HudElementSettingsScreen.SettingRow.color("keystroke-idle-color", "Key Color"));
            rows.add(HudElementSettingsScreen.SettingRow.color("keystroke-text-color", "Text Color"));
         }

         rows.add(HudElementSettingsScreen.SettingRow.boolRow("keystroke-show-space", "Show Space"));
         rows.add(HudElementSettingsScreen.SettingRow.boolRow("keystroke-show-mouse", "Show Mouse"));
      }

      for (int i = rows.size() - 1; i >= 0; i--) {
         if (rows.get(i).type == HudElementSettingsScreen.RowType.SECTION
            && (i == rows.size() - 1 || rows.get(i + 1).type == HudElementSettingsScreen.RowType.SECTION)) {
            rows.remove(i);
         }
      }

      List<HudElementSettingsScreen.SettingRow> frozen = List.copyOf(rows);
      rowsCache.put(this.id, new HudElementSettingsScreen.CachedRows(rev, frozen));
      return frozen;
   }

   private static long settingsRevision() {
      return (long)AutismHudManager.revision() << 32 | autismclient.modules.al.init_I() & 4294967295L;
   }

   private List<Module> allModulesList() {
      return new ArrayList<>(autismclient.modules.al.init_Collection());
   }

   private List<String> hiddenModuleIds() {
      List<String> ids = new ArrayList<>();
      String raw = AutismHudManager.setting("active_modules", "hidden-modules");
      if (raw != null && !raw.isBlank()) {
         for (String token : raw.split("\\|")) {
            String parsed = token.trim().toLowerCase(Locale.ROOT);
            if (!parsed.isBlank() && !ids.contains(parsed)) {
               ids.add(parsed);
            }
         }

         return ids;
      } else {
         return ids;
      }
   }

   private void toggleHiddenModule(String moduleId) {
      if (moduleId != null && !moduleId.isBlank()) {
         String normalized = moduleId.trim().toLowerCase(Locale.ROOT);
         List<String> ids = this.hiddenModuleIds();
         if (ids.contains(normalized)) {
            ids.remove(normalized);
         } else {
            ids.add(normalized);
         }

         AutismHudManager.setSetting("active_modules", "hidden-modules", String.join("|", ids));
      }
   }

   private int[] modulePickerLayout(int moduleCount) {
      int sw = cA.init_I();
      int sh = cA.load_I();
      int margin = 6;
      int w = autismclient.gui.ui.layout.PanelLayoutMath.clampSizeToMargin(sw, margin, 200);
      int chrome = this.headerHeight() + 8;
      int h = autismclient.gui.ui.layout.PanelLayoutMath.clampSizeToMargin(sh, margin, chrome + Math.max(4, moduleCount) * this.rowStep());
      int x = autismclient.gui.ui.layout.PanelLayoutMath.centerWithMargin(sw, w, margin);
      int y = autismclient.gui.ui.layout.PanelLayoutMath.centerWithMargin(sh, h, margin);
      int viewTop = y + this.headerHeight() + 4;
      return new int[]{x, y, w, h, viewTop, y + h - 4};
   }

   private int modulePickerMaxScroll() {
      int count = this.allModulesList().size();
      int[] picker = this.modulePickerLayout(count);
      return Math.max(0, count * this.rowStep() - (picker[5] - picker[4]));
   }

   private void handleModulePickerClick(int mx, int my) {
      List<Module> modules = this.allModulesList();
      int[] picker = this.modulePickerLayout(modules.size());
      int x = picker[0];
      int y = picker[1];
      int w = picker[2];
      int h = picker[3];
      int viewTop = picker[4];
      int viewBottom = picker[5];
      if (!this.isOverDialogCloseButton(mx, my, x, y, w, h, this.headerHeight()) && this.inside(mx, my, x, y, w, h)) {
         if (this.inside(mx, my, x + 2, viewTop, w - 4, viewBottom - viewTop)) {
            int index = (my - viewTop + this.pickerScroll) / this.rowStep();
            if (index >= 0 && index < modules.size()) {
               this.toggleHiddenModule(modules.get(index).id());
            }
         }
      } else {
         this.modulePickerOpen = false;
      }
   }

   private boolean usesCustomLayout() {
      return "armor".equals(this.id)
         || "inventory".equals(this.id)
         || "compass".equals(this.id)
         || "watermark".equals(this.id)
         || "keystrokes".equals(this.id)
         || "gif".equals(this.id);
   }

   private void drawWindow(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int headerHeight, String title, int mx, int my) {
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(x, y, width, height);
      autismclient.gui.ui.widgets.DialogFrameRenderer.draw(
         autismclient.gui.f.UiHelper.init(graphics, this.font, mx, my),
         bounds,
         headerHeight,
         title,
         mx >= x && mx < x + width && my >= y && my < y + headerHeight
      );
   }

   private boolean isOverDialogCloseButton(int mx, int my, int x, int y, int width, int height, int headerHeight) {
      return autismclient.gui.ui.widgets.DialogFrameRenderer.isOverHeader(autismclient.gui.f.NetHelper.init(x, y, width, height), headerHeight, mx, my);
   }

   private int bodyHeight() {
      int sh = cA.load_I();
      int minBody = 4 * this.rowStep();
      int maxBody = Math.max(minBody, (int)(sh * 0.65F));
      int body = this.clamp(this.rowsHeight(this.settingRows()), minBody, maxBody);
      return this.headerHeight() + 4 + body + this.footerHeight();
   }

   private int rowsHeight(List<HudElementSettingsScreen.SettingRow> rows) {
      int h = 0;
      int rowStep = this.rowStep();
      int sectionStep = this.sectionStep();

      for (HudElementSettingsScreen.SettingRow row : rows) {
         h += row.isSectionRow() ? sectionStep : rowStep;
      }

      return h;
   }

   private int[] panelLayout() {
      int sw = cA.init_I();
      int sh = cA.load_I();
      int margin = 6;
      int preferredW = Math.max(220, Math.min(368, 4 + this.labelColumnWidth() + 150 + 10));
      int w = autismclient.gui.ui.layout.PanelLayoutMath.clampSizeToMargin(sw, margin, preferredW);
      int h = autismclient.gui.ui.layout.PanelLayoutMath.clampSizeToMargin(sh, margin, this.bodyHeight());
      return new int[]{autismclient.gui.ui.layout.PanelLayoutMath.centerWithMargin(sw, w, margin), autismclient.gui.ui.layout.PanelLayoutMath.centerWithMargin(sh, h, margin), w, h};
   }

   private boolean isTooSmall(int width, int height) {
      return width < 220 || height < this.headerHeight() + this.footerHeight() + this.rowStep() * 2;
   }

   private int headerHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int controlHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int footerHeight() {
      return this.controlHeight() + 8;
   }

   private int rowHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int inputHeight() {
      return this.rowHeight() - 2;
   }

   private int rowStep() {
      return this.rowHeight() + 2;
   }

   private int sectionStep() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(12) + 4;
   }

   private int bodyTop(int[] panel) {
      return panel[1] + this.headerHeight() + 4;
   }

   private int bodyBottom(int[] panel) {
      return panel[1] + panel[3] - this.footerHeight();
   }

   private int viewHeight(int[] panel) {
      return Math.max(30, this.bodyBottom(panel) - this.bodyTop(panel));
   }

   private int footerY(int[] panel) {
      return panel[1] + panel[3] - 4 - this.controlHeight();
   }

   private int rowsX(int[] panel) {
      return panel[0] + 4;
   }

   private int rowsWidth(int[] panel) {
      return panel[2] - 4 - 10;
   }

   private int labelColumnWidth() {
      List<HudElementSettingsScreen.SettingRow> rows = this.settingRows();
      int scaleKey = Math.round(autismclient.gui.sdf.MsdfTextRenderer.fontScale() * 100.0F);
      if (rows != this.rows || scaleKey != this.labelWidthScaleKey) {
         int widest = 0;

         for (HudElementSettingsScreen.SettingRow row : rows) {
            if (!row.isSectionRow()) {
               widest = Math.max(widest, autismclient.gui.f.b.k.init(this.font, row.label, THEME.init_Identifier_l(l.read), 0));
            }
         }

         this.rows = rows;
         this.labelWidthScaleKey = scaleKey;
         this.cachedLabelColumnWidth = Math.min(177, widest + 8);
      }

      return this.cachedLabelColumnWidth;
   }

   private int valueWidth(int rowsW) {
      return Math.max(1, rowsW - this.labelColumnWidth() - 4);
   }

   private int sliderWidth(int valueW) {
      return Math.max(20, valueW - 40 - 4);
   }

   private int centeredTextY(int boxY, int boxH) {
      return UiLayoutMath.centerTextY(boxY, boxH, THEME.init_I_l(l.read), THEME.flush());
   }

   private boolean isSettingEnabled(String key) {
      return AutismHudManager.boolSetting(this.id, key);
   }

   private String formatSliderValue(double value, double step) {
      return step >= 1.0 ? Integer.toString((int)Math.round(value)) : String.format(Locale.ROOT, step < 0.01 ? "%.3f" : "%.2f", value);
   }

   private String sliderValueText(HudElementSettingsScreen.SettingRow row, double value) {
      return "gif-fps".equals(row.key) && value <= 0.0 ? "Original" : this.formatSliderValue(value, row.step);
   }

   private boolean isRowVisible(int y, int h, int top, int bottom) {
      return y + h >= top && y <= bottom;
   }

   private int clamp(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   private boolean inside(int mx, int my, int x, int y, int w, int h) {
      return mx >= x && mx < x + w && my >= y && my < y + h;
   }

   private void drawTrimmedText(GuiGraphicsExtractor graphics, String text, int x, int y, int color, int maxW) {
      String trimmed = autismclient.gui.f.b.k.init(this.font, text, maxW, THEME.init_Identifier_l(l.read), color);
      autismclient.gui.f.b.k.init(graphics, this.font, trimmed, THEME.init_Identifier_l(l.read), color, x, y, false);
   }

   private int centeredTextOffset(int h) {
      return Math.max(3, (h - THEME.init_I_l(l.read) + 1) / 2 + 1);
   }

   private void drawBoxText(GuiGraphicsExtractor graphics, String text, int x, int y, int w, int h, int color) {
      String trimmed = autismclient.gui.f.b.k.init(this.font, text, w - 8, THEME.init_Identifier_l(l.read), color);
      int tw = autismclient.gui.f.b.k.init(this.font, trimmed, THEME.init_Identifier_l(l.read), color);
      autismclient.gui.f.b.k.init(
         graphics, this.font, trimmed, THEME.init_Identifier_l(l.read), color, x + Math.max(4, (w - tw) / 2), y + this.centeredTextOffset(h), false
      );
   }

   private enum RowType {
      BOOL,
      ENUM,
      COLOR,
      NUMBER,
      ITEM,
      PICK,
      MODULE_LIST,
      SECTION;
   }

   private record CachedRows(long rev, List<HudElementSettingsScreen.SettingRow> rows) {
   }

   private record SettingRow(
      String key, String label, HudElementSettingsScreen.RowType type, double min, double max, double step, List<String> choices
   ) {
      static HudElementSettingsScreen.SettingRow sectionRow(String label) {
         return new HudElementSettingsScreen.SettingRow("", label, HudElementSettingsScreen.RowType.SECTION, 0.0, 0.0, 1.0, List.of());
      }

      static HudElementSettingsScreen.SettingRow boolRow(String key, String label) {
         return new HudElementSettingsScreen.SettingRow(key, label, HudElementSettingsScreen.RowType.BOOL, 0.0, 1.0, 1.0, List.of());
      }

      static HudElementSettingsScreen.SettingRow enumRow(String key, String label, String... choices) {
         return new HudElementSettingsScreen.SettingRow(key, label, HudElementSettingsScreen.RowType.ENUM, 0.0, 1.0, 1.0, List.of(choices));
      }

      static HudElementSettingsScreen.SettingRow color(String key, String label) {
         return new HudElementSettingsScreen.SettingRow(key, label, HudElementSettingsScreen.RowType.COLOR, 0.0, 1.0, 1.0, List.of());
      }

      static HudElementSettingsScreen.SettingRow sliderRow(String key, String label, HudElementSettingsScreen.RowType type, double min, double max, double step) {
         return new HudElementSettingsScreen.SettingRow(key, label, type, min, max, step, List.of());
      }

      static HudElementSettingsScreen.SettingRow item(String key, String label) {
         return new HudElementSettingsScreen.SettingRow(key, label, HudElementSettingsScreen.RowType.ITEM, 0.0, 1.0, 1.0, List.of());
      }

      static HudElementSettingsScreen.SettingRow buttonRow(String key, String label, String buttonLabel) {
         return new HudElementSettingsScreen.SettingRow(key, label, HudElementSettingsScreen.RowType.PICK, 0.0, 1.0, 1.0, List.of(buttonLabel));
      }

      static HudElementSettingsScreen.SettingRow moduleList(String key, String label) {
         return new HudElementSettingsScreen.SettingRow(key, label, HudElementSettingsScreen.RowType.MODULE_LIST, 0.0, 1.0, 1.0, List.of());
      }

      boolean isSectionRow() {
         return this.type == HudElementSettingsScreen.RowType.SECTION;
      }
   }
}
