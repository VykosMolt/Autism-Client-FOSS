package autismclient.gui.screens;

import autismclient.compat263.Compat263;
import autismclient.util.AutismMacroEditorOverlay;
import autismclient.util.JoinMacroController;
import autismclient.util.AutismMacro;
import autismclient.util.MacroLibraryManager;
import autismclient.util.bi;
import autismclient.util.OverlayManager;
import autismclient.util.cA;
import autismclient.util.AutismOverlay;
import autismclient.util.MacroFolderLayout;
import autismclient.util.ThemePaletteManager;
import autismclient.util.ThemedTextInputBox;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.IntConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class JoinMacroScreen extends VirtualScaledScreen {
   private static final int init_I = 382;
   private static final int load_I = 3;
   private static final String[] ACTION_BUTTON_LABELS = new String[]{"Edit", "Create", "Password"};
   private final Screen parent;
   private final List<JoinMacroScreen.Inner_StreamUtil> rows = new ArrayList<>();
   private final List<JoinMacroScreen.Inner_DataUtil> buttons = new ArrayList<>();
   private ThemedTextInputBox searchField;
   private int scrollRow;
   private String lastQuery = "";
   private long lastRevision = -9223372036854775808L;
   private boolean rowsDirty = true;
   private final List<autismclient.gui.ui.widgets.DropdownWidget> dropdowns = new ArrayList<>();
   private autismclient.gui.ui.widgets.DropdownWidget methodDropdown;
   private autismclient.gui.ui.widgets.DropdownWidget load_SyncManager;
   private boolean scrollbarDragging;
   private int write;

   public JoinMacroScreen(Screen parent) {
      super(Component.literal("Join Macro"));
      this.parent = parent;
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   protected void init() {
      if (this.searchField == null) {
         this.searchField = new ThemedTextInputBox(this.minecraft, this.font, this.open(), this.get(), this.put(), this.controlHeight(), false);
         this.searchField.setPlaceholder(Component.literal("Search macros..."));
         this.searchField.setMaxLength(64);
         this.searchField.setChangedListener(value -> {
            this.scrollRow = 0;
            this.rowsDirty = true;
         });
      }

      this.layoutSearchField();
      this.rebuildRows();
   }

   @Override
   public void tick() {
      super.tick();
      this.refreshRowsIfNeeded();
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      int virtualMouseX = cA.init_I_D(mouseX);
      int virtualMouseY = cA.init_I_D(mouseY);
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         this.layoutSearchField();
         this.buttons.clear();
         this.dropdowns.clear();
         graphics.fill(0, 0, this.virtualWidth(), this.virtualHeight(), -15987181);
         int x = this.flush();
         int panelW = this.close();
         int ctl = this.controlHeight();
         this.addButton(
            this.open(),
            this.pop(),
            0,
            ctl,
            "Back",
            autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY,
            true,
            () -> {
               if (this.minecraft != null) {
                  this.minecraft.gui.setScreen(this.parent);
               }
            }
         );
         autismclient.gui.f.PacketUtil.load(
            graphics,
            autismclient.gui.f.NetHelper.init(x, this.push(), panelW, this.reset()),
            5,
            -401402080,
            ThemePaletteManager.recolor(GuiColorPalette.update, ThemePaletteManager.ColorRole.OUTLINE)
         );
         autismclient.gui.f.PacketUtil.load(
            graphics,
            autismclient.gui.f.NetHelper.init(this.start(), this.stop(), this.next(), this.valid()),
            5,
            -1206840035,
            ThemePaletteManager.recolor(GuiColorPalette.update, ThemePaletteManager.ColorRole.OUTLINE)
         );
         int titleX = this.open() + this.buttonWidth("Back") + 4;
         autismclient.gui.f.b.k.init(
            graphics, this.font, "Join Macro", autismclient.gui.f.a.DataUtil.read, GuiColorPalette.flush, titleX, this.centeredTextY(this.pop(), ctl), false
         );
         this.init(graphics, this.open(), this.tick_I_3(), this.poll());
         boolean menuOpen = autismclient.gui.ui.widgets.DropdownWidget.hasOpenMenu(this.dropdowns);
         int hoverX = menuOpen ? -2147483648 : virtualMouseX;
         int hoverY = menuOpen ? -2147483648 : virtualMouseY;
         this.searchField.render(graphics, virtualMouseX, virtualMouseY, delta);
         this.load(hoverX, hoverY);
         this.init(graphics, hoverX, hoverY);
         this.load(graphics, hoverX, hoverY);
         this.read(graphics, hoverX, hoverY);
         autismclient.gui.ui.widgets.DropdownWidget.renderTriggers(graphics, this.font, this.dropdowns, virtualMouseX, virtualMouseY);
         autismclient.gui.ui.widgets.DropdownWidget.renderOpenPopup(graphics, this.font, this.dropdowns, virtualMouseX, virtualMouseY);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void load(int mouseX, int mouseY) {
      int x = this.set() + this.put() + 2;
      int y = this.get();
      int ctl = this.controlHeight();
      this.addButton(x, y, 0, ctl, "Edit", autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY, this.selectedMacro() != null, () -> this.openEditor(this.selectedMacro()));
      x += this.buttonWidth("Edit") + 2;
      this.addButton(x, y, 0, ctl, "Create", autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS, true, () -> this.openEditor(null));
      x += this.buttonWidth("Create") + 2;
      this.addButton(x, y, 0, ctl, "Password", autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY, true, this::openPasswordScreen);
   }

   private void openPasswordScreen() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(new LoginPasswordScreen(this, JoinMacroController.formValues(), updated -> {
            if (!JoinMacroController.init(updated)) {
               bi.write("Secure encryption is unavailable; values were not saved.");
            }
         }));
      }
   }

   private void init(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      int x = this.open();
      int y = this.add();
      int ctl = this.controlHeight();
      autismclient.gui.f.b.k.init(graphics, this.font, "Execution Method", autismclient.gui.f.a.DataUtil.init, -1054492, x, this.centeredTextY(y, ctl), false);
      JoinMacroController.Inner_StreamUtil[] timings = JoinMacroController.Inner_StreamUtil.values();
      List<String> methodOptions = new ArrayList<>();

      for (JoinMacroController.Inner_StreamUtil t : timings) {
         methodOptions.add(t.load());
      }

      int methodSel = JoinMacroController.timing().ordinal();
      int dropdownX = x + this.read_I_2() + 8;
      int dropdownW = this.widestOption(methodOptions);
      this.methodDropdown = this.init_SyncManager_SyncManager(
         this.methodDropdown, dropdownX, y, dropdownW, ctl, methodOptions, methodSel, idx -> JoinMacroController.init(timings[Math.max(0, Math.min(idx, timings.length - 1))])
      );
      this.dropdowns.add(this.methodDropdown);
      boolean keep = JoinMacroController.keepEnabled();
      this.addButton(
         dropdownX + dropdownW + 2,
         y,
         0,
         ctl,
         keep ? "Stays Enabled" : "Clears After",
         keep ? autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY,
         true,
         () -> JoinMacroController.load(!JoinMacroController.keepEnabled())
      );
      int triggerRowY = this.clear();
      autismclient.gui.f.b.k.init(
         graphics, this.font, keep ? "Repeat On" : "Run On", autismclient.gui.f.a.DataUtil.init, -1054492, x, this.centeredTextY(triggerRowY, ctl), false
      );
      JoinMacroController.Inner_CachePool[] triggerOpts = this.init_aA_CachePoolArr();
      List<String> triggerLabels = new ArrayList<>();
      int triggerSel = 0;

      for (int i = 0; i < triggerOpts.length; i++) {
         triggerLabels.add(this.init_Str_aA_CachePool(triggerOpts[i]));
         if (triggerOpts[i] == JoinMacroController.trigger()) {
            triggerSel = i;
         }
      }

      this.load_SyncManager = this.init_SyncManager_SyncManager(
         this.load_SyncManager,
         dropdownX,
         triggerRowY,
         this.widestOption(triggerLabels),
         ctl,
         triggerLabels,
         triggerSel,
         idx -> JoinMacroController.init(triggerOpts[Math.max(0, Math.min(idx, triggerOpts.length - 1))])
      );
      this.dropdowns.add(this.load_SyncManager);
   }

   private int widestOption(List<String> options) {
      int widest = 0;

      for (String option : options) {
         widest = Math.max(widest, autismclient.gui.f.b.k.init(this.font, option, autismclient.gui.f.a.DataUtil.init, 0));
      }

      return Math.max(56, widest + 26);
   }

   private autismclient.gui.ui.widgets.DropdownWidget init_SyncManager_SyncManager(
      autismclient.gui.ui.widgets.DropdownWidget existing, int x, int y, int w, int h, List<String> options, int selected, IntConsumer onSelect
   ) {
      if (existing == null) {
         return new autismclient.gui.ui.widgets.DropdownWidget(x, y, w, h, options, selected, onSelect);
      }

      existing.setBounds(x, y, w, h).setOptions(options).setSelectedIndex(selected).setOnSelect(onSelect);
      return existing;
   }

   private void init(GuiGraphicsExtractor graphics, int x, int y, int maxWidth) {
      int line = 12;
      String selected = JoinMacroController.macroName();
      if (selected.isBlank()) {
         autismclient.gui.f.b.k.init(
            graphics, this.font, "Click a macro to select it.", autismclient.gui.f.a.DataUtil.init, GuiColorPalette.close, x, this.centeredTextY(y, line), false
         );
      } else {
         String selectedLine = "Selected: " + selected;
         String modeLine = JoinMacroController.summary();
         if (!this.init_Z_I(maxWidth)) {
            autismclient.gui.f.b.k.init(
               graphics, this.font, selectedLine + "  " + modeLine, autismclient.gui.f.a.DataUtil.init, GuiColorPalette.open, x, this.centeredTextY(y, line), false
            );
         } else {
            autismclient.gui.f.b.k.init(
               graphics,
               this.font,
               autismclient.gui.f.b.k.init(this.font, selectedLine, maxWidth, autismclient.gui.f.a.DataUtil.init, GuiColorPalette.open),
               autismclient.gui.f.a.DataUtil.init,
               GuiColorPalette.open,
               x,
               this.centeredTextY(y, line),
               false
            );
            autismclient.gui.f.b.k.init(
               graphics,
               this.font,
               autismclient.gui.f.b.k.init(this.font, modeLine, maxWidth, autismclient.gui.f.a.DataUtil.init, GuiColorPalette.open),
               autismclient.gui.f.a.DataUtil.init,
               GuiColorPalette.open,
               x,
               this.centeredTextY(y + line, line),
               false
            );
         }
      }
   }

   private boolean init_Z_I(int maxWidth) {
      String selected = JoinMacroController.macroName();
      if (selected.isBlank()) {
         return false;
      }

      String combined = "Selected: " + selected + "  " + JoinMacroController.summary();
      return autismclient.gui.f.b.k.init(this.font, combined, autismclient.gui.f.a.DataUtil.init, 0) > maxWidth;
   }

   private void load(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      int listX = this.start();
      int listW = this.next();
      autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbar = this.scrollbarMetrics();
      int rowX = this.value();
      int rowRight = this.init_I_PoolEntry_DataUtil(scrollbar);
      int rowH = this.update_I_2();
      int pitch = this.sync();
      int visibleRows = this.data();
      String title = this.rows.size() <= visibleRows
         ? "Macros"
         : "Macros  showing " + (this.scrollRow + 1) + "-" + Math.min(this.rows.size(), this.scrollRow + visibleRows) + " / " + this.rows.size();
      autismclient.gui.f.b.k.init(
         graphics,
         this.font,
         autismclient.gui.f.b.k.init(this.font, title, listW - 8, autismclient.gui.f.a.DataUtil.init, GuiColorPalette.flush),
         autismclient.gui.f.a.DataUtil.init,
         GuiColorPalette.flush,
         listX + 4,
         this.centeredTextY(this.stop() + 4, 12),
         false
      );
      if (this.rows.isEmpty()) {
         autismclient.gui.f.b.k.init(
            graphics, this.font, "No macros found.", autismclient.gui.f.a.DataUtil.init, GuiColorPalette.close, rowX + 8, this.centeredTextY(this.size(), rowH), false
         );
      } else {
         String selected = JoinMacroController.macroName();
         int max = Math.min(this.rows.size(), this.scrollRow + visibleRows);

         for (int i = this.scrollRow; i < max; i++) {
            JoinMacroScreen.Inner_StreamUtil row = this.rows.get(i);
            int rowY = this.size() + (i - this.scrollRow) * pitch;
            autismclient.gui.f.NetHelper rowBounds = autismclient.gui.f.NetHelper.init(rowX, rowY, rowRight - rowX, rowH);
            boolean hovered = rowBounds.init_Z(mouseX, mouseY);
            boolean active = row.name.equalsIgnoreCase(selected);
            if (row.folderRow.init_Z()) {
               autismclient.gui.ui.widgets.GroupHeaderRenderer.load(
                  autismclient.gui.f.UiHelper.init(graphics, this.font, mouseX, mouseY),
                  rowBounds,
                  row.folderRow.init_Str(),
                  row.folderRow.isFolder(),
                  hovered
               );
            } else {
               this.init(graphics, rowBounds, row.name, active, hovered);
               String stepsText = row.steps + (row.steps == 1 ? " step" : " steps");
               int stepsW = autismclient.gui.f.b.k.init(this.font, stepsText, autismclient.gui.f.a.DataUtil.init, 0);
               int rightX = rowRight - 4 - stepsW;
               int nameX = rowX + 8;
               int nameW = Math.max(1, rightX - nameX - 4);
               int ty = this.centeredTextY(rowY, rowH);
               autismclient.gui.f.b.k.init(
                  graphics,
                  this.font,
                  autismclient.gui.f.b.k.init(this.font, row.name, nameW, autismclient.gui.f.a.DataUtil.init, -1054492),
                  autismclient.gui.f.a.DataUtil.init,
                  -1054492,
                  nameX,
                  ty,
                  false
               );
               autismclient.gui.f.b.k.init(
                  graphics, this.font, stepsText, autismclient.gui.f.a.DataUtil.init, active ? GuiColorPalette.open : GuiColorPalette.close, rightX, ty, false
               );
            }
         }

         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.render(graphics, scrollbar, scrollbar.containsTrack(mouseX, mouseY), this.scrollbarDragging);
      }
   }

   private void read(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      for (JoinMacroScreen.Inner_DataUtil button : this.buttons) {
         autismclient.gui.ui.widgets.ButtonDrawHelper.init(
            graphics,
            this.font,
            button.x,
            button.load,
            button.read,
            button.write,
            button.label,
            button.tone,
            button.enabled,
            mouseX,
            mouseY
         );
      }
   }

   private void addButton(int x, int y, int w, int h, String label, autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone variant, boolean enabled, Runnable action) {
      if (w <= 0) {
         w = this.buttonWidth(label);
      }

      this.buttons.add(new JoinMacroScreen.Inner_DataUtil(x, y, w, h, label, variant, enabled, action));
   }

   private void init(GuiGraphicsExtractor graphics, autismclient.gui.f.NetHelper bounds, String key, boolean selected, boolean hovered) {
      autismclient.gui.f.PacketUtil.load(
         graphics,
         bounds,
         4,
         selected
            ? ThemePaletteManager.recolor(976607347, ThemePaletteManager.ColorRole.SUCCESS)
            : (hovered ? ThemePaletteManager.recolor(606804509, ThemePaletteManager.ColorRole.ACCENT) : 403771667)
      );
      if (selected) {
         autismclient.gui.f.PacketUtil.load(
            graphics, bounds.load(1, 1, 1, 1), 3, ThemePaletteManager.recolor(hovered ? 1463146611 : 976607347, ThemePaletteManager.ColorRole.SUCCESS)
         );
         autismclient.gui.f.PacketUtil.load(
            graphics, autismclient.gui.f.NetHelper.init(bounds.read(), bounds.write() + 2, 2, bounds.sync() - 4), 1, GuiColorPalette.open
         );
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
      event = toVirtual(event);
      if (autismclient.gui.ui.widgets.DropdownWidget.handleMouseClick(this.dropdowns, event.x(), event.y(), Compat263.mouseEventGlfwButton(event))) {
         return true;
      }

      if (this.searchField != null && this.searchField.mouseClicked(event.x(), event.y(), Compat263.mouseEventGlfwButton(event))) {
         return true;
      }

      if (Compat263.mouseEventGlfwButton(event) == 0) {
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbar = this.scrollbarMetrics();
         if (scrollbar.isScrollable() && scrollbar.containsTrack(event.x(), event.y())) {
            this.scrollbarDragging = true;
            this.write = scrollbar.containsThumb(event.x(), event.y()) ? Math.max(0, (int)Math.round(event.y()) - scrollbar.thumbY()) : scrollbar.thumbHeight() / 2;
            this.setScrollPixels(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(scrollbar, event.y(), this.write));
            if (this.searchField != null) {
               this.searchField.setFocused(false);
            }

            return true;
         }

         for (JoinMacroScreen.Inner_DataUtil button : this.buttons) {
            if (button.init(event.x(), event.y())) {
               if (button.enabled && button.action != null) {
                  button.action.run();
               }

               return true;
            }
         }

         int rowIndex = this.init_I_D_D(event.x(), event.y());
         if (rowIndex >= 0) {
            JoinMacroScreen.Inner_StreamUtil row = this.rows.get(rowIndex);
            if (row.folderRow.init_Z()) {
               MacroLibraryManager.getInstance().toggleFolderCollapsed(row.folderRow.folderName());
               this.rebuildRows();
               return true;
            }

            String selected = JoinMacroController.macroName();
            JoinMacroController.setMacroName(row.name.equalsIgnoreCase(selected) ? "" : row.name);
            return true;
         }
      }

      if (this.searchField != null) {
         this.searchField.setFocused(false);
      }

      return true;
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      event = toVirtual(event);
      if (autismclient.gui.ui.widgets.DropdownWidget.handleMouseRelease(this.dropdowns)) {
         return true;
      } else if (this.scrollbarDragging) {
         this.scrollbarDragging = false;
         return true;
      } else {
         return this.searchField != null && this.searchField.mouseReleased(event.x(), event.y(), Compat263.mouseEventGlfwButton(event));
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      event = toVirtual(event);
      dx = cA.init_D_D(dx);
      dy = cA.init_D_D(dy);
      if (autismclient.gui.ui.widgets.DropdownWidget.handleMouseDrag(this.dropdowns, event.x(), event.y(), Compat263.mouseEventGlfwButton(event))) {
         return true;
      } else if (this.scrollbarDragging) {
         this.setScrollPixels(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(this.scrollbarMetrics(), event.y(), this.write));
         return true;
      } else {
         return this.searchField != null && this.searchField.mouseDragged(event.x(), event.y(), Compat263.mouseEventGlfwButton(event), dx, dy);
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      mouseX = cA.init_D_D(mouseX);
      mouseY = cA.init_D_D(mouseY);
      if (autismclient.gui.ui.widgets.DropdownWidget.handleMouseScroll(this.dropdowns, mouseX, mouseY, scrollY)) {
         return true;
      }

      if (this.searchField != null && this.searchField.mouseScrolled(mouseX, mouseY, scrollY)) {
         return true;
      }

      if (mouseX >= this.start() && mouseX < this.start() + this.next() && mouseY >= this.stop() && mouseY < this.stop() + this.valid()) {
         int maxScroll = Math.max(0, this.rows.size() - this.data());
         if (maxScroll > 0) {
            this.scrollRow = Math.max(0, Math.min(maxScroll, this.scrollRow - (int)Math.signum(scrollY)));
         }

         return true;
      } else {
         return true;
      }
   }

   @Override
   public boolean keyPressed(KeyEvent input) {
      if (this.searchField != null && this.searchField.keyPressed(input)) {
         return true;
      }

      if (Compat263.keyEventGlfwKey(input) == 256) {
         if (autismclient.gui.ui.widgets.DropdownWidget.closeAll(this.dropdowns)) {
            return true;
         }

         if (this.minecraft != null) {
            this.minecraft.gui.setScreen(this.parent);
         }
         return true;
      } else {
         return true;
      }
   }

   @Override
   public boolean charTyped(CharacterEvent input) {
      return this.searchField != null && this.searchField.charTyped(input);
   }

   @Override
   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.parent);
      }
   }

   private void refreshRowsIfNeeded() {
      String query = this.searchQuery();
      long revision = MacroLibraryManager.getInstance().getRevision();
      if (this.rowsDirty || !query.equals(this.lastQuery) || revision != this.lastRevision) {
         this.rebuildRows();
      }
   }

   private void rebuildRows() {
      String query = this.searchQuery();
      this.lastQuery = query;
      this.lastRevision = MacroLibraryManager.getInstance().getRevision();
      this.rowsDirty = false;
      this.rows.clear();

      for (MacroFolderLayout.Inner_DataUtil item : MacroLibraryManager.getInstance().getFolderEntries(query)) {
         this.rows
            .add(
               new JoinMacroScreen.Inner_StreamUtil(item.init_Str(), !item.init_Z() && item.macro().actions != null ? item.macro().actions.size() : 0, item)
            );
      }

      this.scrollRow = Math.max(0, Math.min(this.scrollRow, Math.max(0, this.rows.size() - this.data())));
   }

   private String searchQuery() {
      return this.searchField == null ? "" : this.searchField.getValue().trim().toLowerCase(Locale.ROOT);
   }

   private AutismMacro selectedMacro() {
      String selected = JoinMacroController.macroName();
      return selected.isBlank() ? null : MacroLibraryManager.getInstance().findByName(selected);
   }

   private void openEditor(AutismMacro macro) {
      AutismMacroEditorOverlay editor = AutismMacroEditorOverlay.getInstance();
      OverlayManager.getInstance().register(editor, AutismOverlay.RenderLayer.HOST_SCREEN);
      editor.openForJoinMacro(macro, saved -> {
         if (saved != null && saved.name != null && !saved.name.isBlank()) {
            JoinMacroController.setMacroName(saved.name);
            bi.init("Join macro selected: " + saved.name, -10035062);
         }
      });
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(new AutismOverlaysScreen(editor, new JoinMacroScreen(this.parent)));
      }
   }

   private int init_I_D_D(double mouseX, double mouseY) {
      int y = this.size();
      int pitch = this.sync();
      int h = this.data() * pitch;
      if (!(mouseX < this.value()) && !(mouseX >= this.init_I_PoolEntry_DataUtil(this.scrollbarMetrics())) && !(mouseY < y) && !(mouseY >= y + h)) {
         int local = (int)mouseY - y;
         if (local % pitch >= this.update_I_2()) {
            return -1;
         }

         int index = this.scrollRow + local / pitch;
         return index >= 0 && index < this.rows.size() ? index : -1;
      } else {
         return -1;
      }
   }

   private void layoutSearchField() {
      if (this.searchField != null) {
         this.searchField.setX(this.set());
         this.searchField.setY(this.get());
         this.searchField.setWidth(this.put());
         this.searchField.setHeight(this.controlHeight());
      }
   }

   private int buttonWidth(String label) {
      return autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(autismclient.gui.f.b.k.init(this.font, label, autismclient.gui.f.a.DataUtil.init, 0));
   }

   private int centeredTextY(int top, int height) {
      return autismclient.gui.f.UiHelper.init(this.font).centerTextY(autismclient.gui.f.NetHelper.init(0, top, 1, height));
   }

   private int read_I_2() {
      int w = 0;

      for (String label : new String[]{"Execution Method", "Run On", "Repeat On"}) {
         w = Math.max(w, autismclient.gui.f.b.k.init(this.font, label, autismclient.gui.f.a.DataUtil.init, 0));
      }

      return w;
   }

   private int controlHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int update_I_2() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int sync() {
      return this.update_I_2() + 2;
   }

   private int flush() {
      return Math.max(6, (this.virtualWidth() - this.close()) / 2);
   }

   private int close() {
      return Math.max(1, Math.min(382, this.virtualWidth() - 12));
   }

   private int open() {
      return this.flush() + 4;
   }

   private int poll() {
      return Math.max(1, this.close() - 8);
   }

   private int push() {
      return 6;
   }

   private int pop() {
      return this.push() + 4;
   }

   private int tick_I_3() {
      return this.pop() + this.controlHeight() + 2;
   }

   private int run() {
      return (this.init_Z_I(this.poll()) ? 2 : 1) * 12;
   }

   private int get() {
      return this.tick_I_3() + this.run() + 2;
   }

   private int set() {
      return this.open();
   }

   private int put() {
      int buttons = 0;

      for (String label : ACTION_BUTTON_LABELS) {
         buttons += this.buttonWidth(label) + 2;
      }

      return Math.max(40, this.poll() - buttons);
   }

   private int add() {
      return this.get() + this.controlHeight() + 2;
   }

   private int clear() {
      return this.add() + this.controlHeight() + 2;
   }

   private int reset() {
      return this.clear() + this.controlHeight() + 4 - this.push();
   }

   private int start() {
      return this.flush();
   }

   private int stop() {
      return this.push() + this.reset() + 4;
   }

   private int next() {
      return this.close();
   }

   private int prev() {
      return 18;
   }

   private int valid() {
      int available = Math.max(this.prev() + this.update_I_2() + 4, this.virtualHeight() - 6 - this.stop());
      int cap = Math.max(3, this.virtualHeight() * 65 / 100 / this.sync());
      int shown = Math.max(3, Math.min(cap, this.rows.size()));
      return Math.min(available, this.prev() + shown * this.sync() - 2 + 4);
   }

   private int size() {
      return this.stop() + this.prev();
   }

   private int data() {
      return Math.max(1, (this.valid() - this.prev() - 4 + 2) / this.sync());
   }

   private int value() {
      return this.start() + 4;
   }

   private int init_I_PoolEntry_DataUtil(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbar) {
      int right = this.start() + this.next() - 4;
      return scrollbar.isScrollable() ? this.cache() - 2 : right;
   }

   private int cache() {
      return this.start() + this.next() - 4 - 6;
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics() {
      int pitch = this.sync();
      int trackY = this.size();
      int trackH = Math.max(1, this.data() * pitch - 2);
      int contentPixels = Math.max(0, this.rows.size() * pitch);
      int viewPixels = Math.max(1, this.data() * pitch);
      return autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(contentPixels, viewPixels, this.cache(), trackY, 6, trackH, this.scrollRow * pitch);
   }

   private void setScrollPixels(int scrollPixels) {
      int maxScrollRows = Math.max(0, this.rows.size() - this.data());
      this.scrollRow = Math.max(0, Math.min(maxScrollRows, Math.round((float)scrollPixels / this.sync())));
   }

   private JoinMacroController.Inner_CachePool[] init_aA_CachePoolArr() {
      return JoinMacroController.keepEnabled()
         ? new JoinMacroController.Inner_CachePool[]{
            JoinMacroController.Inner_CachePool.ANY,
            JoinMacroController.Inner_CachePool.SECOND,
            JoinMacroController.Inner_CachePool.THIRD,
            JoinMacroController.Inner_CachePool.FOURTH,
            JoinMacroController.Inner_CachePool.FIFTH,
            JoinMacroController.Inner_CachePool.SIXTH_PLUS
         }
         : new JoinMacroController.Inner_CachePool[]{
            JoinMacroController.Inner_CachePool.FIRST,
            JoinMacroController.Inner_CachePool.SECOND,
            JoinMacroController.Inner_CachePool.THIRD,
            JoinMacroController.Inner_CachePool.FOURTH,
            JoinMacroController.Inner_CachePool.FIFTH,
            JoinMacroController.Inner_CachePool.SIXTH_PLUS
         };
   }

   private String init_Str_aA_CachePool(JoinMacroController.Inner_CachePool triggerJoin) {
      return triggerJoin.label(JoinMacroController.keepEnabled());
   }

   private record Inner_DataUtil(
      int x,
      int load,
      int read,
      int write,
      String label,
      autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone tone,
      boolean enabled,
      Runnable action
   ) {
      boolean init(double mx, double my) {
         return mx >= this.x && mx < this.x + this.read && my >= this.load && my < this.load + this.write;
      }
   }

   private record Inner_StreamUtil(String name, int steps, MacroFolderLayout.Inner_DataUtil folderRow) {
   }
}
