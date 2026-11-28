package autismclient.gui.screens;

import autismclient.api.module.DisplayMode;
import autismclient.api.module.Setting;
import autismclient.compat263.Compat263;
import autismclient.gui.ui.widgets.UiLayoutMath;
import autismclient.gui.f.b.l;
import autismclient.modules.Module;
import autismclient.modules.al;
import autismclient.util.OnlinePlayerCollector;
import autismclient.util.TabPlayerListParser;
import autismclient.util.bi;
import autismclient.util.cA;
import autismclient.util.ThemePaletteManager;
import autismclient.util.PipeDelimitedListUtil;
import autismclient.util.ThemedTextInputBox;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;

public class StringListEditorScreen extends VirtualScaledScreen {
   private static final autismclient.gui.f.b.CharEncoder theme = new autismclient.gui.f.b.CharEncoder();
   private static final int MAX_PANEL_WIDTH = 300;
   private static final int load_I = 4;
   private static final int read_I = 3;
   private static final int BACKDROP_COLOR = -872086265;
   private static final int update = -1441722092;
   private static final int sync = -1441000940;
   private static final int flush = -791321;
   private static final int close = -4743522;
   private static final int open = -50373;
   private final Screen parent;
   private final Module module;
   private final Setting<?, ?> setting;
   private final List<StringListEditorScreen.HitBox> hitBoxes = new ArrayList<>();
   private ThemedTextInputBox entryInput;
   private ThemedTextInputBox searchInput;
   private int scrollOffset;
   private boolean draggingScrollbar;
   private int scrollbarGrabOffset;
   private int editIndex = -1;
   private int suggestionIndex = 0;
   private String lastRawValue;
   private String lastFilter;
   private List<String> parsedValues = List.of();
   private List<String> filteredCache = List.of();
   private final List<String> scannedNames = new ArrayList<>();
   private final Map<String, String> ranksByName = new HashMap<>();
   private boolean tabScanned;

   private static int mutedTextColor() {
      return ThemePaletteManager.recolor(-4743522, ThemePaletteManager.ColorRole.TEXT);
   }

   public StringListEditorScreen(Screen parent, Module module, Setting<?, ?> option) {
      super(Component.literal(option == null ? "Edit List" : "Edit " + option.label()));
      this.parent = parent;
      this.module = module;
      this.setting = option;
   }

   @Override
   protected void init() {
      this.ensureInputs();
      this.layoutInputs();
      this.layoutInputs();
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
         this.hitBoxes.clear();
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, this.virtualWidth(), this.virtualHeight()), -872086265);
         int x = this.panelX();
         int y = this.panelY();
         int panelW = this.panelWidth();
         int panelH = this.panelHeight();
         this.ensureInputs();
         this.layoutInputs();
         this.drawHeader(graphics, x, y, panelW, panelH, this.screenTitle(), mx, my);
         if (panelW < 180 || panelH < 100) {
            this.drawClippedText(graphics, "Window too small.", x + 4, this.centeredTextY(y + this.headerHeight(), 12), mutedTextColor(), Math.max(0, panelW - 8));
            return;
         }

         int fieldX = x + 4;
         int fieldY = this.fieldY();
         int fieldH = this.fieldHeight();
         this.entryInput.render(graphics, mx, my, delta);
         int addW = this.submitButtonWidth();
         this.drawButton(
            graphics,
            this.editIndex >= 0 ? "Save" : "Add",
            x + panelW - 4 - addW,
            fieldY,
            addW,
            fieldH,
            autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS,
            StringListEditorScreen.HitKind.ADD,
            -1,
            mx,
            my
         );
         this.renderSuggestions(graphics, fieldX, fieldY, this.entryFieldWidth(), mx, my);
         this.searchInput.render(graphics, mx, my, delta);
         int actionsY = this.actionsY();
         int nextX = fieldX;
         int clearW = this.buttonWidth("Clear");
         this.drawButton(
            graphics,
            "Clear",
            nextX,
            actionsY,
            clearW,
            fieldH,
            autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.DANGER,
            StringListEditorScreen.HitKind.CLEAR,
            -1,
            mx,
            my
         );
         nextX += clearW + 2;
         String importLabel = this.importButtonLabel();
         if (importLabel != null) {
            int importW = this.buttonWidth(importLabel);
            this.drawButton(
               graphics,
               importLabel,
               nextX,
               actionsY,
               importW,
               fieldH,
               autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY,
               StringListEditorScreen.HitKind.IMPORT,
               -1,
               mx,
               my
            );
            nextX += importW + 2;
         }

         if (this.isRankTagList() || this.isPlayerRankPicker()) {
            int scanW = this.buttonWidth("Scan");
            this.drawButton(
               graphics,
               "Scan",
               nextX,
               actionsY,
               scanW,
               fieldH,
               autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY,
               StringListEditorScreen.HitKind.SCAN,
               -1,
               mx,
               my
            );
            nextX += scanW + 2;
         }

         int summaryX = nextX + 4;
         String summary = this.isRankPickerReady()
            ? this.scannedNames.size() + " scanned | " + this.currentValues().size() + " picked"
            : this.filteredValues().size() + " shown | " + this.currentValues().size() + " total";
         this.drawClippedText(graphics, summary, summaryX, this.centeredTextY(actionsY, fieldH), mutedTextColor(), x + panelW - 4 - summaryX);
         int listY = this.listY();
         int listH = this.listHeight();
         autismclient.gui.ui.widgets.ScrollListLayout.ListLayout listLayout = this.listLayout(listY, listH);
         this.drawRoundedBox(graphics, fieldX, listY, panelW - 8, listH, 5, -1442248437, theme.set());
         listLayout.pushContentClip(graphics);

         try {
            this.renderValueRows(graphics, listLayout, mx, my);
         } finally {
            listLayout.popContentClip(graphics);
         }

         listLayout.renderScrollbar(graphics, mx, my, this.draggingScrollbar);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void renderValueRows(GuiGraphicsExtractor graphics, autismclient.gui.ui.widgets.ScrollListLayout.ListLayout layout, int mx, int my) {
      if (this.isRankPickerReady()) {
         this.renderRankRows(graphics, layout, mx, my);
      } else {
         List<String> rows = this.filteredValues();
         this.scrollOffset = this.clamp(this.scrollOffset, 0, layout.maxScroll());
         int x = layout.x() + layout.contentInset();
         int w = layout.contentWidth();
         int rowH = layout.rowHeight();
         if (rows.isEmpty()) {
            this.drawClippedText(graphics, "No entries.", x + 4, this.centeredTextY(layout.y() + layout.contentInset(), rowH), mutedTextColor(), w - 8);
         } else {
            List<String> allValues = this.currentValues();
            int delH = rowH - 2;
            int delW = delH + 4;
            int delX = x + w - 2 - delW;
            int textW = delX - 2 - x;
            layout.forEachVisibleRow(rows.size(), (i, rowY) -> {
               String value = rows.get(i);
               boolean over = mx >= x && mx < x + w && my >= rowY && my < rowY + rowH;
               autismclient.gui.ui.widgets.PanelSurfaceHelper.fillRect(graphics, x, rowY, w, rowH - 1, over ? -1441000940 : -1441722092);
               int delY = rowY + (rowH - delH) / 2;
               this.drawClippedText(graphics, value, x + 4, this.centeredTextY(rowY, rowH - 1), -791321, textW - 4);
               autismclient.gui.ui.widgets.ListRowRenderer.drawDeleteButton(graphics, delX, delY, delW, delH, over);
               int index = this.sourceIndex(rows, allValues, i);
               this.hitBoxes.add(new StringListEditorScreen.HitBox(StringListEditorScreen.HitKind.EDIT_ENTRY, x, rowY, Math.max(1, textW), rowH, index));
               this.hitBoxes.add(new StringListEditorScreen.HitBox(StringListEditorScreen.HitKind.REMOVE, delX, delY, delW, delH, index));
            });
         }
      }
   }

   private void renderRankRows(GuiGraphicsExtractor graphics, autismclient.gui.ui.widgets.ScrollListLayout.ListLayout layout, int mx, int my) {
      List<StringListEditorScreen.RankRow> rows = this.rankRows();
      this.scrollOffset = this.clamp(this.scrollOffset, 0, layout.maxScroll());
      int x = layout.x() + layout.contentInset();
      int w = layout.contentWidth();
      int rowH = layout.rowHeight();
      if (rows.isEmpty()) {
         this.drawClippedText(graphics, "No players found.", x + 4, this.centeredTextY(layout.y() + layout.contentInset(), rowH), mutedTextColor(), w - 8);
      } else {
         layout.forEachVisibleRow(rows.size(), (i, rowY) -> {
            StringListEditorScreen.RankRow row = rows.get(i);
            boolean over = mx >= x && mx < x + w && my >= rowY && my < rowY + rowH;
            int ty = this.centeredTextY(rowY, rowH - 1);
            if (row.name() == null) {
               autismclient.gui.ui.widgets.PanelSurfaceHelper.fillRect(graphics, x, rowY, w, rowH - 1, over ? -1441000940 : -1441327333);
               String count = row.picked() + "/" + row.total();
               int countW = autismclient.gui.f.b.k.init(this.font, count, theme.init_Identifier_l(l.read), 0);
               this.drawClippedText(graphics, row.rank(), x + 4, ty, -791321, w - countW - 12);
               this.drawClippedText(graphics, count, x + w - 4 - countW, ty, mutedTextColor(), countW + 1);
               this.hitBoxes.add(new StringListEditorScreen.HitBox(StringListEditorScreen.HitKind.TOGGLE_RANK, x, rowY, w, rowH, i));
            } else {
               boolean picked = row.picked() > 0;
               autismclient.gui.ui.widgets.PanelSurfaceHelper.fillRect(graphics, x, rowY, w, rowH - 1, over ? -1441000940 : -1441722092);
               this.drawClippedText(graphics, (picked ? "[x] " : "[ ] ") + row.name(), x + 8, ty, picked ? -791321 : mutedTextColor(), w - 12);
               this.hitBoxes.add(new StringListEditorScreen.HitBox(StringListEditorScreen.HitKind.TOGGLE_PLAYER, x, rowY, w, rowH, i));
            }
         });
      }
   }

   private void renderSuggestions(GuiGraphicsExtractor graphics, int fieldX, int fieldY, int fieldW, int mx, int my) {
      if (this.isPlayerNameList() && this.entryInput != null && this.entryInput.isFocused()) {
         List<String> suggestions = this.nameSuggestions();
         if (!suggestions.isEmpty()) {
            int visible = Math.min(5, suggestions.size());
            int rowH = autismclient.gui.ui.UiDensityMetrics.scaledHeight(12);
            int popupH = visible * rowH + 2;
            int popupY = Math.max(4, fieldY - popupH - 2);
            int popupW = Math.min(fieldW, 170);
            int popupX = fieldX;
            this.drawRoundedBox(graphics, popupX, popupY, popupW, popupH, 5, -301397749, ThemePaletteManager.recolor(-50373, ThemePaletteManager.ColorRole.OUTLINE));
            int start = Math.max(0, Math.min(this.suggestionIndex - visible + 1, suggestions.size() - visible));

            for (int i = 0; i < visible; i++) {
               int idx = start + i;
               String suggestion = suggestions.get(idx);
               int y = popupY + 1 + i * rowH;
               boolean selected = idx == this.suggestionIndex;
               boolean over = mx >= popupX && mx < popupX + popupW && my >= y && my < y + rowH;
               autismclient.gui.ui.widgets.PanelSurfaceHelper.fillRect(graphics, popupX + 1, y, popupW - 2, rowH - 1, !selected && !over ? -1441722092 : -1441000940);
               this.drawClippedText(graphics, suggestion, popupX + 4, this.centeredTextY(y, rowH - 1), -791321, popupW - 8);
               this.hitBoxes.add(new StringListEditorScreen.HitBox(StringListEditorScreen.HitKind.SUGGESTION, popupX + 1, y, popupW - 2, rowH, idx));
            }
         }
      }
   }

   private void drawButton(
      GuiGraphicsExtractor graphics,
      String label,
      int x,
      int y,
      int w,
      int h,
      autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone variant,
      StringListEditorScreen.HitKind type,
      int index,
      int mx,
      int my
   ) {
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(graphics, this.font, x, y, w, h, label, variant, true, mx, my);
      this.hitBoxes.add(new StringListEditorScreen.HitBox(type, x, y, w, h, index));
   }

   private void drawHeader(GuiGraphicsExtractor graphics, int x, int y, int width, int height, String title, int mx, int my) {
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(x, y, width, height);
      int headerH = this.headerHeight();
      autismclient.gui.ui.widgets.DialogFrameRenderer.draw(
         autismclient.gui.f.UiHelper.init(graphics, this.font, mx, my), bounds, headerH, title, mx >= x && mx < x + width && my >= y && my < y + headerH
      );
      autismclient.gui.f.NetHelper close = autismclient.gui.ui.widgets.DialogFrameRenderer.headerBounds(bounds, headerH);
      this.hitBoxes.add(new StringListEditorScreen.HitBox(StringListEditorScreen.HitKind.CLOSE, close.read(), close.write(), close.update(), close.sync(), -1));
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      if (Compat263.mouseEventGlfwButton(event) != 0) {
         return true;
      }

      autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = this.scrollbarMetrics();
      if (metrics.isScrollable() && metrics.containsTrack(mx, my)) {
         this.draggingScrollbar = true;
         this.scrollbarGrabOffset = metrics.containsThumb(mx, my) ? my - metrics.thumbY() : metrics.thumbHeight() / 2;
         this.scrollOffset = this.snapToRow(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(metrics, my, this.scrollbarGrabOffset));
         return true;
      }

      this.ensureInputs();
      if (this.entryInput.mouseClicked(mx, my, Compat263.mouseEventGlfwButton(event))) {
         this.searchInput.setFocused(false);
         return true;
      }

      if (this.searchInput.mouseClicked(mx, my, Compat263.mouseEventGlfwButton(event))) {
         this.entryInput.setFocused(false);
         return true;
      }

      for (int i = this.hitBoxes.size() - 1; i >= 0; i--) {
         StringListEditorScreen.HitBox hit = this.hitBoxes.get(i);
         if (hit.contains(mx, my) && (hit.kind != StringListEditorScreen.HitKind.REMOVE || this.isOverList(mx, my))) {
            switch (hit.kind) {
               case ADD:
                  this.commitEntry();
                  break;
               case EDIT_ENTRY:
                  this.beginEdit(hit.update);
                  break;
               case REMOVE:
                  this.removeEntry(hit.update);
                  break;
               case SUGGESTION:
                  this.applySuggestion(hit.update);
                  break;
               case CLEAR:
                  this.storeValue("");
                  this.clearEditor();
                  this.clampScroll();
                  break;
               case SCAN:
                  if (this.isPlayerRankPicker()) {
                     this.loadTabRanks();
                  } else {
                     this.importRanksFromTab();
                  }
                  break;
               case TOGGLE_PLAYER:
                  List<StringListEditorScreen.RankRow> rowsx = this.rankRows();
                  if (hit.update >= 0 && hit.update < rowsx.size()) {
                     this.toggleName(rowsx.get(hit.update).name());
                  }
                  break;
               case TOGGLE_RANK:
                  List<StringListEditorScreen.RankRow> rows = this.rankRows();
                  if (hit.update >= 0 && hit.update < rows.size()) {
                     this.toggleRankMembers(rows.get(hit.update).rank());
                  }
                  break;
               case IMPORT:
                  this.importFromSourceSetting();
                  break;
               case CLOSE:
                  this.onClose();
            }

            return true;
         }
      }

      this.entryInput.setFocused(false);
      this.searchInput.setFocused(false);
      return true;
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      int my = cA.init_I_D(mouseY);
      int mx = cA.init_I_D(mouseX);
      if (!this.isOverList(mx, my)) {
         return true;
      }

      this.scrollOffset = this.scrollOffset + (scrollY < 0.0 ? this.rowHeight() : -this.rowHeight());
      this.scrollOffset = this.clamp(this.scrollOffset, 0, this.scrollbarMetrics().maxScroll());
      return true;
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      this.draggingScrollbar = false;
      if (this.entryInput != null && this.entryInput.mouseReleased(mx, my, Compat263.mouseEventGlfwButton(event))) {
         return true;
      } else {
         return this.searchInput != null && this.searchInput.mouseReleased(mx, my, Compat263.mouseEventGlfwButton(event)) ? true : true;
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      int my = cA.init_I_D(event.y());
      int mx = cA.init_I_D(event.x());
      if (this.entryInput != null && this.entryInput.mouseDragged(mx, my, Compat263.mouseEventGlfwButton(event), dx, dy)) {
         return true;
      }

      if (this.searchInput != null && this.searchInput.mouseDragged(mx, my, Compat263.mouseEventGlfwButton(event), dx, dy)) {
         return true;
      }

      if (!this.draggingScrollbar) {
         return true;
      }

      this.scrollOffset = this.snapToRow(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(this.scrollbarMetrics(), my, this.scrollbarGrabOffset));
      return true;
   }

   @Override
   public boolean keyPressed(KeyEvent input) {
      this.ensureInputs();
      if (Compat263.keyEventGlfwKey(input) == 256) {
         if (this.editIndex >= 0) {
            if (!this.entryText().trim().isEmpty()) {
               this.commitEntry();
            } else {
               this.clearEditor();
            }

            return true;
         } else {
            this.onClose();
            return true;
         }
      } else if (this.entryInput.isFocused() && this.isPlayerNameList() && Compat263.keyEventGlfwKey(input) == 258) {
         List<String> suggestions = this.nameSuggestions();
         if (!suggestions.isEmpty()) {
            this.suggestionIndex = Math.floorMod(this.suggestionIndex + ((Compat263.keyEventGlfwModifiers(input) & 1) != 0 ? -1 : 1), suggestions.size());
            this.setEntryText(this.replaceEntryKey(this.entryText(), suggestions.get(this.suggestionIndex)));
         }

         return true;
      } else if (Compat263.keyEventGlfwKey(input) != 257 && Compat263.keyEventGlfwKey(input) != 335) {
         if (this.entryInput.keyPressed(input)) {
            return true;
         } else {
            return this.searchInput.keyPressed(input) ? true : true;
         }
      } else {
         if (this.entryInput.isFocused()) {
            this.commitEntry();
         }

         return true;
      }
   }

   @Override
   public boolean charTyped(CharacterEvent input) {
      this.ensureInputs();
      if (this.entryInput.charTyped(input)) {
         return true;
      } else {
         return this.searchInput.charTyped(input) ? true : true;
      }
   }

   @Override
   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.parent);
      }
   }

   private void commitEntry() {
      String value = this.entryText().trim();
      if (!value.isEmpty()) {
         List<String> values = this.mutableValues();
         if (this.editIndex >= 0 && this.editIndex < values.size()) {
            values.set(this.editIndex, value);
         } else {
            values.add(value);
         }

         this.storeValue(PipeDelimitedListUtil.join(values));
         this.setEntryText("");
         this.editIndex = -1;
         this.suggestionIndex = 0;
         this.clampScroll();
      }
   }

   private void beginEdit(int index) {
      List<String> values = this.currentValues();
      if (index >= 0 && index < values.size()) {
         this.editIndex = index;
         this.setEntryText(values.get(index));
         if (this.entryInput != null) {
            this.entryInput.setFocused(true);
         }

         if (this.searchInput != null) {
            this.searchInput.setFocused(false);
         }

         this.suggestionIndex = 0;
      }
   }

   private void clearEditor() {
      this.editIndex = -1;
      this.setEntryText("");
      this.suggestionIndex = 0;
   }

   private void removeEntry(int index) {
      List<String> values = this.mutableValues();
      if (index >= 0 && index < values.size()) {
         values.remove(index);
         this.storeValue(PipeDelimitedListUtil.join(values));
         if (this.editIndex == index) {
            this.clearEditor();
         } else if (this.editIndex > index) {
            this.editIndex--;
         }

         this.clampScroll();
      }
   }

   private boolean isTitleSetupContext() {
      return this.parent instanceof AutismModulesScreen moduleScreen && moduleScreen.isTitleSetup();
   }

   private void storeValue(String value) {
      if (this.isTitleSetupContext()) {
         this.module.setConfiguredValue(this.setting.id(), value);
      } else {
         this.module.setValue(this.setting.id(), value);
      }
   }

   private List<String> filteredValues() {
      String needle = this.searchText().trim().toLowerCase(Locale.ROOT);
      List<String> values = this.currentValues();
      if (needle.equals(this.lastFilter)) {
         return this.filteredCache;
      }

      this.lastFilter = needle;
      if (needle.isEmpty()) {
         this.filteredCache = values;
         return this.filteredCache;
      }

      List<String> out = new ArrayList<>();

      for (String value : values) {
         if (value.toLowerCase(Locale.ROOT).contains(needle)) {
            out.add(value);
         }
      }

      this.filteredCache = List.copyOf(out);
      return this.filteredCache;
   }

   private List<String> mutableValues() {
      return new ArrayList<>(this.currentValues());
   }

   private List<String> currentValues() {
      String rawValue = this.module.value(this.setting.id());
      if (!rawValue.equals(this.lastRawValue)) {
         this.lastRawValue = rawValue;
         this.parsedValues = List.copyOf(PipeDelimitedListUtil.split(rawValue));
         this.lastFilter = null;
         this.filteredCache = List.of();
      }

      return this.parsedValues;
   }

   private int sourceIndex(List<String> rows, List<String> allValues, int filteredIndex) {
      if (filteredIndex >= 0 && filteredIndex < rows.size()) {
         String value = rows.get(filteredIndex);
         int occurrence = 0;

         for (int i = 0; i <= filteredIndex; i++) {
            if (value.equals(rows.get(i))) {
               occurrence++;
            }
         }

         for (int i = 0; i < allValues.size(); i++) {
            if (value.equals(allValues.get(i))) {
               if (--occurrence == 0) {
                  return i;
               }
            }
         }

         return allValues.indexOf(value);
      } else {
         return -1;
      }
   }

   private boolean isPlayerNameList() {
      return this.setting != null && this.setting.displayMode() == DisplayMode.PLAYER_NAME_LIST;
   }

   private boolean isRankTagList() {
      return this.setting != null && this.setting.displayMode() == DisplayMode.RANK_TAG_LIST;
   }

   private boolean isPlayerRankPicker() {
      return this.setting != null && this.setting.displayMode() == DisplayMode.PLAYER_RANK_PICKER;
   }

   private boolean isRankPickerReady() {
      return this.isPlayerRankPicker() && this.tabScanned;
   }

   private void loadTabRanks() {
      if (this.minecraft != null && this.minecraft.getConnection() != null) {
         this.scannedNames.clear();
         this.ranksByName.clear();
         this.tabScanned = true;
         this.scrollOffset = 0;

         for (TabPlayerListParser.ParsedPlayer player : TabPlayerListParser.parse(this.minecraft)) {
            if (player != null && player.name() != null && !player.name().isBlank() && !containsIgnoreCase(this.scannedNames, player.name())) {
               this.scannedNames.add(player.name());
               if (player.hasPrefix()) {
                  this.ranksByName.put(player.name().toLowerCase(Locale.ROOT), player.load().trim());
               }
            }
         }

         this.scannedNames.sort(String::compareToIgnoreCase);
         bi.init("Scanned " + this.scannedNames.size() + " players.", -13248397);
         autismclient.util.BackgroundTaskExecutor.submit("player-rank-scan", () -> {
            List<String> probed = OnlinePlayerCollector.collect(this.minecraft, true);
            if (!probed.isEmpty()) {
               this.minecraft.execute(() -> {
                  boolean changed = false;

                  for (String name : probed) {
                     if (name != null && !name.isBlank() && !containsIgnoreCase(this.scannedNames, name)) {
                        this.scannedNames.add(name);
                        changed = true;
                     }
                  }

                  if (changed) {
                     this.scannedNames.sort(String::compareToIgnoreCase);
                  }
               });
            }
         });
      } else {
         bi.read("Join a server first.");
      }
   }

   private String rankOf(String name) {
      String rank = name == null ? "" : this.ranksByName.getOrDefault(name.toLowerCase(Locale.ROOT), "");
      return rank != null && !rank.isBlank() ? rank : "No rank";
   }

   private List<StringListEditorScreen.RankRow> rankRows() {
      String needle = this.searchText().trim().toLowerCase(Locale.ROOT);
      List<String> selected = this.currentValues();
      LinkedHashMap<String, List<String>> groups = new LinkedHashMap<>();
      List<String> manual = new ArrayList<>();

      for (String value : selected) {
         if (value != null && !value.isBlank() && !containsIgnoreCase(this.scannedNames, value)) {
            manual.add(value);
         }
      }

      if (!manual.isEmpty()) {
         groups.put("Not online", manual);
      }

      TreeMap<String, List<String>> byRank = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

      for (String name : this.scannedNames) {
         byRank.computeIfAbsent(this.rankOf(name), ignored -> new ArrayList<>()).add(name);
      }

      List<String> rankless = byRank.remove("No rank");
      groups.putAll(byRank);
      if (rankless != null) {
         groups.put("No rank", rankless);
      }

      List<StringListEditorScreen.RankRow> rows = new ArrayList<>();
      groups.forEach((rank, members) -> {
         List<String> shown = new ArrayList<>();

         for (String name : members) {
            if (needle.isEmpty() || name.toLowerCase(Locale.ROOT).contains(needle) || rank.toLowerCase(Locale.ROOT).contains(needle)) {
               shown.add(name);
            }
         }

         if (!shown.isEmpty()) {
            int picked = 0;

            for (String name : shown) {
               if (containsIgnoreCase(selected, name)) {
                  picked++;
               }
            }

            rows.add(new StringListEditorScreen.RankRow(rank, null, picked, shown.size()));

            for (String name : shown) {
               rows.add(new StringListEditorScreen.RankRow(rank, name, containsIgnoreCase(selected, name) ? 1 : 0, 1));
            }
         }
      });
      return rows;
   }

   private void toggleName(String name) {
      if (name != null && !name.isBlank()) {
         List<String> values = this.mutableValues();
         if (!removeIgnoreCase(values, name)) {
            values.add(name);
         }

         this.storeValue(PipeDelimitedListUtil.join(values));
      }
   }

   private void toggleRankMembers(String rank) {
      if (rank != null) {
         List<String> members = new ArrayList<>();

         for (StringListEditorScreen.RankRow row : this.rankRows()) {
            if (row.name() != null && row.rank().equals(rank)) {
               members.add(row.name());
            }
         }

         if (!members.isEmpty()) {
            List<String> values = this.mutableValues();
            boolean allPicked = true;

            for (String name : members) {
               if (!containsIgnoreCase(values, name)) {
                  allPicked = false;
                  break;
               }
            }

            for (String name : members) {
               if (allPicked) {
                  removeIgnoreCase(values, name);
               } else if (!containsIgnoreCase(values, name)) {
                  values.add(name);
               }
            }

            this.storeValue(PipeDelimitedListUtil.join(values));
         }
      }
   }

   private static boolean removeIgnoreCase(List<String> values, String target) {
      for (int i = 0; i < values.size(); i++) {
         if (values.get(i).equalsIgnoreCase(target)) {
            values.remove(i);
            return true;
         }
      }

      return false;
   }

   private void importRanksFromTab() {
      if (this.isRankTagList() && this.minecraft != null && this.minecraft.getConnection() != null) {
         List<String> values = this.mutableValues();
         int added = 0;

         for (TabPlayerListParser.ParsedPlayer player : TabPlayerListParser.parse(this.minecraft)) {
            if (player != null && player.hasPrefix()) {
               String tag = player.load().trim();
               if (!tag.isEmpty() && !containsIgnoreCase(values, tag)) {
                  values.add(tag);
                  added++;
               }
            }
         }

         if (added > 0) {
            this.storeValue(PipeDelimitedListUtil.join(values));
            this.clampScroll();
            bi.init("Rank scan added " + added + " tag" + (added == 1 ? "." : "s."), -13248397);
         } else {
            bi.read("Rank scan found nothing.");
         }
      } else {
         bi.read("Join a server first.");
      }
   }

   private static boolean containsIgnoreCase(List<String> values, String target) {
      for (String value : values) {
         if (value.equalsIgnoreCase(target)) {
            return true;
         }
      }

      return false;
   }

   private String importButtonLabel() {
      if (this.module == null || this.setting == null) {
         return null;
      } else if ("teams".equals(this.module.id()) && "friends".equals(this.setting.id())) {
         return "Import Names";
      } else {
         return "name-censor".equals(this.module.id()) && "names".equals(this.setting.id()) ? "Import Teams" : null;
      }
   }

   private void importFromSourceSetting() {
      String sourceModuleId = null;
      String sourceOptionId = null;
      if (this.module != null && this.setting != null) {
         if ("teams".equals(this.module.id()) && "friends".equals(this.setting.id())) {
            sourceModuleId = "name-censor";
            sourceOptionId = "names";
         } else if ("name-censor".equals(this.module.id()) && "names".equals(this.setting.id())) {
            sourceModuleId = "teams";
            sourceOptionId = "friends";
         }
      }

      Module source = sourceModuleId == null ? null : al.init_Module_Str(sourceModuleId);
      if (source == null) {
         bi.read("Nothing to import.");
      } else {
         List<String> values = this.mutableValues();
         int added = 0;

         for (String entry : PipeDelimitedListUtil.split(source.value(sourceOptionId))) {
            if (entry != null && !entry.isBlank() && !containsIgnoreCase(values, entry)) {
               values.add(entry.trim());
               added++;
            }
         }

         if (added > 0) {
            this.storeValue(PipeDelimitedListUtil.join(values));
            this.clampScroll();
            bi.init("Imported " + added + " name" + (added == 1 ? "." : "s."), -13248397);
         } else {
            bi.read("Nothing new to import.");
         }
      }
   }

   private List<String> nameSuggestions() {
      if (this.isPlayerNameList() && this.minecraft != null && this.minecraft.getConnection() != null) {
         String query = this.entryKey(this.entryText()).toLowerCase(Locale.ROOT);
         List<String> names = new ArrayList<>();

         for (PlayerInfo info : this.minecraft.getConnection().getOnlinePlayers()) {
            if (info != null && info.getProfile() != null && info.getProfile().name() != null) {
               String name = info.getProfile().name();
               if (this.isSuggestableName(name) && (query.isEmpty() || name.toLowerCase(Locale.ROOT).startsWith(query)) && !names.contains(name)) {
                  names.add(name);
               }
            }
         }

         if (this.minecraft.level != null) {
            for (AbstractClientPlayer player : this.minecraft.level.players()) {
               if (player != null && player.getGameProfile() != null && player.getGameProfile().name() != null) {
                  String name = player.getGameProfile().name();
                  if (this.isSuggestableName(name) && (query.isEmpty() || name.toLowerCase(Locale.ROOT).startsWith(query)) && !names.contains(name)) {
                     names.add(name);
                  }
               }
            }
         }

         names.sort(String.CASE_INSENSITIVE_ORDER);
         if (this.suggestionIndex >= names.size()) {
            this.suggestionIndex = Math.max(0, names.size() - 1);
         }

         return names;
      } else {
         return List.of();
      }
   }

   private boolean isSuggestableName(String name) {
      String trimmed = name == null ? "" : name.trim();
      if (trimmed.length() < 4) {
         return false;
      }

      String lower = trimmed.toLowerCase(Locale.ROOT);
      if (lower.startsWith("slot_") && lower.length() > 5) {
         for (int i = 5; i < lower.length(); i++) {
            if (!Character.isDigit(lower.charAt(i))) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   private String entryKey(String entry) {
      String value = entry == null ? "" : entry;
      int eq = value.indexOf(61);
      if (eq >= 0) {
         value = value.substring(0, eq);
      }

      return value.trim();
   }

   private String replaceEntryKey(String entry, String suggestion) {
      String value = entry == null ? "" : entry;
      int eq = value.indexOf(61);
      return eq >= 0 ? suggestion + value.substring(eq) : suggestion;
   }

   private void applySuggestion(int index) {
      List<String> suggestions = this.nameSuggestions();
      if (index >= 0 && index < suggestions.size()) {
         this.suggestionIndex = index;
         this.setEntryText(this.replaceEntryKey(this.entryText(), suggestions.get(index)));
         if (this.entryInput != null) {
            this.entryInput.setFocused(true);
         }

         if (this.searchInput != null) {
            this.searchInput.setFocused(false);
         }
      }
   }

   private void ensureInputs() {
      if (this.entryInput == null) {
         this.entryInput = new ThemedTextInputBox(this.minecraft, this.font, 0, 0, 1, this.fieldHeight(), false);
         this.entryInput.setMaxLength(512);
         this.entryInput.setChangedListener(value -> this.suggestionIndex = 0);
      }

      if (this.searchInput == null) {
         this.searchInput = new ThemedTextInputBox(this.minecraft, this.font, 0, 0, 1, this.fieldHeight(), false);
         this.searchInput.setMaxLength(128);
         this.searchInput.setPlaceholder(Component.literal("Search"));
         this.searchInput.setChangedListener(value -> {
            this.lastFilter = null;
            this.clampScroll();
         });
      }
   }

   private void layoutInputs() {
      if (this.entryInput != null && this.searchInput != null) {
         int fieldX = this.panelX() + 4;
         int fieldH = this.fieldHeight();
         this.entryInput.setX(fieldX);
         this.entryInput.setY(this.fieldY());
         this.entryInput.setWidth(this.entryFieldWidth());
         this.entryInput.setHeight(fieldH);
         this.entryInput.setPlaceholder(Component.literal(this.editIndex >= 0 ? "Edit entry" : this.entryPlaceholder()));
         this.searchInput.setX(fieldX);
         this.searchInput.setY(this.searchFieldY());
         this.searchInput.setWidth(this.panelWidth() - 8);
         this.searchInput.setHeight(fieldH);
      }
   }

   private String entryPlaceholder() {
      return this.module != null
            && this.setting != null
            && "name-censor".equals(this.module.id())
            && "custom-aliases".equals(this.setting.id())
         ? "Player=Alias"
         : "New entry";
   }

   private int entryFieldWidth() {
      return Math.max(40, this.panelWidth() - 8 - 2 - this.submitButtonWidth());
   }

   private int submitButtonWidth() {
      return this.buttonWidth(this.editIndex >= 0 ? "Save" : "Add");
   }

   private int buttonWidth(String label) {
      return autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(autismclient.gui.f.b.k.init(this.font, label, theme.init_Identifier_l(l.read), 0));
   }

   private int headerHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int fieldHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int rowHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int fieldY() {
      return this.panelY() + this.headerHeight() + 4;
   }

   private int searchFieldY() {
      return this.fieldY() + this.fieldHeight() + 2;
   }

   private int actionsY() {
      return this.searchFieldY() + this.fieldHeight() + 2;
   }

   private int listY() {
      return this.actionsY() + this.fieldHeight() + 2;
   }

   private int listHeight() {
      return this.panelY() + this.panelHeight() - this.listY() - 4;
   }

   private int centeredTextY(int boxY, int boxH) {
      return UiLayoutMath.centerTextY(boxY, boxH, theme.init_I_l(l.read), theme.flush());
   }

   private String entryText() {
      return this.entryInput == null ? "" : this.entryInput.getValue();
   }

   private void setEntryText(String value) {
      this.ensureInputs();
      this.entryInput.setValue(value == null ? "" : value);
      this.entryInput.setSelectionEnd(this.entryInput.getValue().length());
   }

   private String searchText() {
      return this.searchInput == null ? "" : this.searchInput.getValue();
   }

   private String screenTitle() {
      return this.setting == null ? "Edit List" : "Edit " + this.setting.label();
   }

   private int panelX() {
      return autismclient.gui.ui.layout.PanelLayoutMath.centerWithMargin(this.virtualWidth(), this.panelWidth(), 6);
   }

   private int panelY() {
      return autismclient.gui.ui.layout.PanelLayoutMath.centerWithMargin(this.virtualHeight(), this.panelHeight(), 6);
   }

   private int panelWidth() {
      return autismclient.gui.ui.layout.PanelLayoutMath.clampSizeToMargin(this.virtualWidth(), 6, 300);
   }

   private int panelHeight() {
      int rowH = this.rowHeight();
      int chrome = this.headerHeight() + 4 + (this.fieldHeight() + 2) * 3 + 6 + 4;
      int maxRows = Math.max(4, (int)(this.virtualHeight() * 0.65F) / rowH);
      int content = this.isRankPickerReady() ? this.scannedNames.size() + this.currentValues().size() : this.currentValues().size();
      int rows = Math.max(4, Math.min(maxRows, content));
      return autismclient.gui.ui.layout.PanelLayoutMath.clampSizeToMargin(this.virtualHeight(), 6, chrome + rows * rowH);
   }

   private int rowCount() {
      return this.isRankPickerReady() ? this.rankRows().size() : this.filteredValues().size();
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics() {
      return this.scrollbarMetrics(this.listY(), this.listHeight());
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics(int listY, int listH) {
      return this.listLayout(listY, listH).scrollbar();
   }

   private autismclient.gui.ui.widgets.ScrollListLayout.ListLayout listLayout(int listY, int listH) {
      return autismclient.gui.ui.widgets.ScrollListLayout.compute(
         this.panelX() + 4, listY, this.panelWidth() - 8, listH, this.rowCount(), this.rowHeight(), this.rowHeight(), this.scrollOffset, 4, 6, 3, 6, false
      );
   }

   private boolean isOverList(int mx, int my) {
      autismclient.gui.ui.widgets.ScrollListLayout.ListLayout listLayout = this.listLayout(this.listY(), this.listHeight());
      return mx >= listLayout.x() + listLayout.contentInset()
         && mx < listLayout.x() + listLayout.contentInset() + listLayout.contentWidth()
         && my >= listLayout.y() + listLayout.contentInset()
         && my < listLayout.y() + listLayout.contentInset() + listLayout.viewHeight();
   }

   private void clampScroll() {
      this.scrollOffset = this.clamp(this.scrollOffset, 0, this.scrollbarMetrics().maxScroll());
   }

   private int snapToRow(int offset) {
      int rowH = this.rowHeight();
      return Math.max(0, offset / rowH) * rowH;
   }

   private int clamp(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   private void drawRoundedBox(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int radius, int fill, int border) {
      autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(x, y, w, h), radius, fill, border);
   }

   private void drawClippedText(GuiGraphicsExtractor graphics, String text, int x, int y, int color, int maxWidth) {
      String display = autismclient.gui.f.b.k.init(
         this.font, text == null ? "" : text, Math.max(0, maxWidth), theme.init_Identifier_l(l.read), color
      );
      autismclient.gui.f.b.k.init(graphics, this.font, display, theme.init_Identifier_l(l.read), color, x, y, false);
   }

   private void drawCenteredText(GuiGraphicsExtractor graphics, String text, int x, int y, int w, int h, int color) {
      String display = autismclient.gui.f.b.k.init(this.font, text == null ? "" : text, Math.max(0, w - 4), theme.init_Identifier_l(l.read), color);
      int tw = autismclient.gui.f.b.k.init(this.font, display, theme.init_Identifier_l(l.read), color);
      int th = theme.init_I_l(l.read);
      int drawX = x + Math.max(2, (w - tw) / 2);
      int drawY = y + Math.max(1, (h - th + 1) / 2 + 1);
      autismclient.gui.f.b.k.init(graphics, this.font, display, theme.init_Identifier_l(l.read), color, drawX, drawY, false);
   }

   private enum HitKind {
      ADD,
      EDIT_ENTRY,
      REMOVE,
      SUGGESTION,
      CLEAR,
      SCAN,
      TOGGLE_PLAYER,
      TOGGLE_RANK,
      IMPORT,
      CLOSE;
   }

   private record RankRow(String rank, String name, int picked, int total) {
   }

   private record HitBox(StringListEditorScreen.HitKind kind, int x, int load, int read, int write, int update) {
      boolean contains(int mx, int my) {
         return mx >= this.x && mx < this.x + this.read && my >= this.load && my < this.load + this.write;
      }
   }
}
