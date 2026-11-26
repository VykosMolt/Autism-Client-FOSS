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
   private static final String[] init_StrArr = new String[]{"Edit", "Create", "Password"};
   private final Screen init_Screen;
   private final List<JoinMacroScreen.Inner_StreamUtil> init_List = new ArrayList<>();
   private final List<JoinMacroScreen.Inner_DataUtil> load_List = new ArrayList<>();
   private ThemedTextInputBox init_k;
   private int read_I;
   private String init_Str = "";
   private long init_J = -9223372036854775808L;
   private boolean init_Z = true;
   private final List<autismclient.gui.ui.widgets.DropdownWidget> read_List = new ArrayList<>();
   private autismclient.gui.ui.widgets.DropdownWidget init_SyncManager;
   private autismclient.gui.ui.widgets.DropdownWidget load_SyncManager;
   private boolean load_Z;
   private int write;

   public JoinMacroScreen(Screen parent) {
      super(Component.literal("Join Macro"));
      this.init_Screen = parent;
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   protected void init() {
      if (this.init_k == null) {
         this.init_k = new ThemedTextInputBox(this.minecraft, this.font, this.open(), this.get(), this.put(), this.write_I_2(), false);
         this.init_k.init_V_Component(Component.literal("Search macros..."));
         this.init_k.load(64);
         this.init_k.init_V_Consumer(value -> {
            this.read_I = 0;
            this.init_Z = true;
         });
      }

      this.update_V_2();
      this.write_V_2();
   }

   @Override
   public void tick() {
      super.tick();
      this.read_V_2();
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      int virtualMouseX = cA.init_I_D(mouseX);
      int virtualMouseY = cA.init_I_D(mouseY);
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         this.update_V_2();
         this.load_List.clear();
         this.read_List.clear();
         graphics.fill(0, 0, this.init_I(), this.load_I(), -15987181);
         int x = this.flush();
         int panelW = this.close();
         int ctl = this.write_I_2();
         this.init_V_I(
            this.open(),
            this.pop(),
            0,
            ctl,
            "Back",
            autismclient.gui.ui.widgets.OverlayActionButton.Inner_StreamUtil.SECONDARY,
            true,
            () -> {
               if (this.minecraft != null) {
                  this.minecraft.gui.setScreen(this.init_Screen);
               }
            }
         );
         autismclient.gui.f.PacketUtil.load(
            graphics,
            autismclient.gui.f.NetHelper.init(x, this.push(), panelW, this.reset()),
            5,
            -401402080,
            ThemePaletteManager.init_I_I_ct_DataUtil(GuiColorPalette.update, ThemePaletteManager.Inner_DataUtil.OUTLINE)
         );
         autismclient.gui.f.PacketUtil.load(
            graphics,
            autismclient.gui.f.NetHelper.init(this.start(), this.stop(), this.next(), this.valid()),
            5,
            -1206840035,
            ThemePaletteManager.init_I_I_ct_DataUtil(GuiColorPalette.update, ThemePaletteManager.Inner_DataUtil.OUTLINE)
         );
         int titleX = this.open() + this.init_I_Str_3("Back") + 4;
         autismclient.gui.f.b.k.init(
            graphics, this.font, "Join Macro", autismclient.gui.f.a.DataUtil.read, GuiColorPalette.flush, titleX, this.init_I_I_I_2(this.pop(), ctl), false
         );
         this.init(graphics, this.open(), this.tick_I_3(), this.poll());
         boolean menuOpen = autismclient.gui.ui.widgets.DropdownWidget.init_Z_List(this.read_List);
         int hoverX = menuOpen ? -2147483648 : virtualMouseX;
         int hoverY = menuOpen ? -2147483648 : virtualMouseY;
         this.init_k.init_V(graphics, virtualMouseX, virtualMouseY, delta);
         this.load(hoverX, hoverY);
         this.init(graphics, hoverX, hoverY);
         this.load(graphics, hoverX, hoverY);
         this.read(graphics, hoverX, hoverY);
         autismclient.gui.ui.widgets.DropdownWidget.load(graphics, this.font, this.read_List, virtualMouseX, virtualMouseY);
         autismclient.gui.ui.widgets.DropdownWidget.read(graphics, this.font, this.read_List, virtualMouseX, virtualMouseY);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void load(int mouseX, int mouseY) {
      int x = this.set() + this.put() + 2;
      int y = this.get();
      int ctl = this.write_I_2();
      this.init_V_I(x, y, 0, ctl, "Edit", autismclient.gui.ui.widgets.OverlayActionButton.Inner_StreamUtil.SECONDARY, this.init_aM() != null, () -> this.init_V_aM_2(this.init_aM()));
      x += this.init_I_Str_3("Edit") + 2;
      this.init_V_I(x, y, 0, ctl, "Create", autismclient.gui.ui.widgets.OverlayActionButton.Inner_StreamUtil.SUCCESS, true, () -> this.init_V_aM_2(null));
      x += this.init_I_Str_3("Create") + 2;
      this.init_V_I(x, y, 0, ctl, "Password", autismclient.gui.ui.widgets.OverlayActionButton.Inner_StreamUtil.SECONDARY, true, this::load_V_4);
   }

   private void load_V_4() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(new LoginPasswordScreen(this, JoinMacroController.init_Map(), updated -> {
            if (!JoinMacroController.init(updated)) {
               bi.write("Secure encryption is unavailable; values were not saved.");
            }
         }));
      }
   }

   private void init(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      int x = this.open();
      int y = this.add();
      int ctl = this.write_I_2();
      autismclient.gui.f.b.k.init(graphics, this.font, "Execution Method", autismclient.gui.f.a.DataUtil.init, -1054492, x, this.init_I_I_I_2(y, ctl), false);
      JoinMacroController.Inner_StreamUtil[] timings = JoinMacroController.Inner_StreamUtil.values();
      List<String> methodOptions = new ArrayList<>();

      for (JoinMacroController.Inner_StreamUtil t : timings) {
         methodOptions.add(t.load());
      }

      int methodSel = JoinMacroController.init_aA_StreamUtil().ordinal();
      int dropdownX = x + this.read_I_2() + 8;
      int dropdownW = this.init_I_List(methodOptions);
      this.init_SyncManager = this.init_SyncManager_SyncManager(
         this.init_SyncManager, dropdownX, y, dropdownW, ctl, methodOptions, methodSel, idx -> JoinMacroController.init(timings[Math.max(0, Math.min(idx, timings.length - 1))])
      );
      this.read_List.add(this.init_SyncManager);
      boolean keep = JoinMacroController.init_Z();
      this.init_V_I(
         dropdownX + dropdownW + 2,
         y,
         0,
         ctl,
         keep ? "Stays Enabled" : "Clears After",
         keep ? autismclient.gui.ui.widgets.OverlayActionButton.Inner_StreamUtil.SUCCESS : autismclient.gui.ui.widgets.OverlayActionButton.Inner_StreamUtil.SECONDARY,
         true,
         () -> JoinMacroController.load(!JoinMacroController.init_Z())
      );
      int triggerRowY = this.clear();
      autismclient.gui.f.b.k.init(
         graphics, this.font, keep ? "Repeat On" : "Run On", autismclient.gui.f.a.DataUtil.init, -1054492, x, this.init_I_I_I_2(triggerRowY, ctl), false
      );
      JoinMacroController.Inner_CachePool[] triggerOpts = this.init_aA_CachePoolArr();
      List<String> triggerLabels = new ArrayList<>();
      int triggerSel = 0;

      for (int i = 0; i < triggerOpts.length; i++) {
         triggerLabels.add(this.init_Str_aA_CachePool(triggerOpts[i]));
         if (triggerOpts[i] == JoinMacroController.init_aA_CachePool()) {
            triggerSel = i;
         }
      }

      this.load_SyncManager = this.init_SyncManager_SyncManager(
         this.load_SyncManager,
         dropdownX,
         triggerRowY,
         this.init_I_List(triggerLabels),
         ctl,
         triggerLabels,
         triggerSel,
         idx -> JoinMacroController.init(triggerOpts[Math.max(0, Math.min(idx, triggerOpts.length - 1))])
      );
      this.read_List.add(this.load_SyncManager);
   }

   private int init_I_List(List<String> options) {
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

      existing.init_SyncManager_I(x, y, w, h).init_SyncManager_List(options).init_SyncManager_I(selected).init_SyncManager_IntConsumer(onSelect);
      return existing;
   }

   private void init(GuiGraphicsExtractor graphics, int x, int y, int maxWidth) {
      int line = 12;
      String selected = JoinMacroController.init_Str();
      if (selected.isBlank()) {
         autismclient.gui.f.b.k.init(
            graphics, this.font, "Click a macro to select it.", autismclient.gui.f.a.DataUtil.init, GuiColorPalette.close, x, this.init_I_I_I_2(y, line), false
         );
      } else {
         String selectedLine = "Selected: " + selected;
         String modeLine = JoinMacroController.load_Str();
         if (!this.init_Z_I(maxWidth)) {
            autismclient.gui.f.b.k.init(
               graphics, this.font, selectedLine + "  " + modeLine, autismclient.gui.f.a.DataUtil.init, GuiColorPalette.open, x, this.init_I_I_I_2(y, line), false
            );
         } else {
            autismclient.gui.f.b.k.init(
               graphics,
               this.font,
               autismclient.gui.f.b.k.init(this.font, selectedLine, maxWidth, autismclient.gui.f.a.DataUtil.init, GuiColorPalette.open),
               autismclient.gui.f.a.DataUtil.init,
               GuiColorPalette.open,
               x,
               this.init_I_I_I_2(y, line),
               false
            );
            autismclient.gui.f.b.k.init(
               graphics,
               this.font,
               autismclient.gui.f.b.k.init(this.font, modeLine, maxWidth, autismclient.gui.f.a.DataUtil.init, GuiColorPalette.open),
               autismclient.gui.f.a.DataUtil.init,
               GuiColorPalette.open,
               x,
               this.init_I_I_I_2(y + line, line),
               false
            );
         }
      }
   }

   private boolean init_Z_I(int maxWidth) {
      String selected = JoinMacroController.init_Str();
      if (selected.isBlank()) {
         return false;
      }

      String combined = "Selected: " + selected + "  " + JoinMacroController.load_Str();
      return autismclient.gui.f.b.k.init(this.font, combined, autismclient.gui.f.a.DataUtil.init, 0) > maxWidth;
   }

   private void load(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      int listX = this.start();
      int listW = this.next();
      autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Inner_DataUtil scrollbar = this.init_PoolEntry_DataUtil_2();
      int rowX = this.value();
      int rowRight = this.init_I_PoolEntry_DataUtil(scrollbar);
      int rowH = this.update_I_2();
      int pitch = this.sync();
      int visibleRows = this.data();
      String title = this.init_List.size() <= visibleRows
         ? "Macros"
         : "Macros  showing " + (this.read_I + 1) + "-" + Math.min(this.init_List.size(), this.read_I + visibleRows) + " / " + this.init_List.size();
      autismclient.gui.f.b.k.init(
         graphics,
         this.font,
         autismclient.gui.f.b.k.init(this.font, title, listW - 8, autismclient.gui.f.a.DataUtil.init, GuiColorPalette.flush),
         autismclient.gui.f.a.DataUtil.init,
         GuiColorPalette.flush,
         listX + 4,
         this.init_I_I_I_2(this.stop() + 4, 12),
         false
      );
      if (this.init_List.isEmpty()) {
         autismclient.gui.f.b.k.init(
            graphics, this.font, "No macros found.", autismclient.gui.f.a.DataUtil.init, GuiColorPalette.close, rowX + 8, this.init_I_I_I_2(this.size(), rowH), false
         );
      } else {
         String selected = JoinMacroController.init_Str();
         int max = Math.min(this.init_List.size(), this.read_I + visibleRows);

         for (int i = this.read_I; i < max; i++) {
            JoinMacroScreen.Inner_StreamUtil row = this.init_List.get(i);
            int rowY = this.size() + (i - this.read_I) * pitch;
            autismclient.gui.f.NetHelper rowBounds = autismclient.gui.f.NetHelper.init(rowX, rowY, rowRight - rowX, rowH);
            boolean hovered = rowBounds.init_Z(mouseX, mouseY);
            boolean active = row.init_Str.equalsIgnoreCase(selected);
            if (row.init_cY_DataUtil.init_Z()) {
               autismclient.gui.ui.widgets.GroupHeaderRenderer.load(
                  autismclient.gui.f.UiHelper.init(graphics, this.font, mouseX, mouseY),
                  rowBounds,
                  row.init_cY_DataUtil.init_Str(),
                  row.init_cY_DataUtil.load_Z(),
                  hovered
               );
            } else {
               this.init(graphics, rowBounds, row.init_Str, active, hovered);
               String stepsText = row.init_I + (row.init_I == 1 ? " step" : " steps");
               int stepsW = autismclient.gui.f.b.k.init(this.font, stepsText, autismclient.gui.f.a.DataUtil.init, 0);
               int rightX = rowRight - 4 - stepsW;
               int nameX = rowX + 8;
               int nameW = Math.max(1, rightX - nameX - 4);
               int ty = this.init_I_I_I_2(rowY, rowH);
               autismclient.gui.f.b.k.init(
                  graphics,
                  this.font,
                  autismclient.gui.f.b.k.init(this.font, row.init_Str, nameW, autismclient.gui.f.a.DataUtil.init, -1054492),
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

         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.init(graphics, scrollbar, scrollbar.init(mouseX, mouseY), this.load_Z);
      }
   }

   private void read(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      for (JoinMacroScreen.Inner_DataUtil button : this.load_List) {
         autismclient.gui.ui.widgets.ButtonDrawHelper.init(
            graphics,
            this.font,
            button.init_I,
            button.load,
            button.read,
            button.write,
            button.init_Str,
            button.init_ByteReader_StreamU,
            button.init_Z,
            mouseX,
            mouseY
         );
      }
   }

   private void init_V_I(int x, int y, int w, int h, String label, autismclient.gui.ui.widgets.OverlayActionButton.Inner_StreamUtil variant, boolean enabled, Runnable action) {
      if (w <= 0) {
         w = this.init_I_Str_3(label);
      }

      this.load_List.add(new JoinMacroScreen.Inner_DataUtil(x, y, w, h, label, variant, enabled, action));
   }

   private void init(GuiGraphicsExtractor graphics, autismclient.gui.f.NetHelper bounds, String key, boolean selected, boolean hovered) {
      autismclient.gui.f.PacketUtil.load(
         graphics,
         bounds,
         4,
         selected
            ? ThemePaletteManager.init_I_I_ct_DataUtil(976607347, ThemePaletteManager.Inner_DataUtil.SUCCESS)
            : (hovered ? ThemePaletteManager.init_I_I_ct_DataUtil(606804509, ThemePaletteManager.Inner_DataUtil.ACCENT) : 403771667)
      );
      if (selected) {
         autismclient.gui.f.PacketUtil.load(
            graphics, bounds.load(1, 1, 1, 1), 3, ThemePaletteManager.init_I_I_ct_DataUtil(hovered ? 1463146611 : 976607347, ThemePaletteManager.Inner_DataUtil.SUCCESS)
         );
         autismclient.gui.f.PacketUtil.load(
            graphics, autismclient.gui.f.NetHelper.init(bounds.read(), bounds.write() + 2, 2, bounds.sync() - 4), 1, GuiColorPalette.open
         );
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
      event = init_MouseButtonEvent_MouseButtonEvent(event);
      if (autismclient.gui.ui.widgets.DropdownWidget.init_Z_List_2(this.read_List, event.x(), event.y(), Compat263.mouseEventGlfwButton(event))) {
         return true;
      }

      if (this.init_k != null && this.init_k.init_Z_D_2(event.x(), event.y(), Compat263.mouseEventGlfwButton(event))) {
         return true;
      }

      if (Compat263.mouseEventGlfwButton(event) == 0) {
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Inner_DataUtil scrollbar = this.init_PoolEntry_DataUtil_2();
         if (scrollbar.init_Z() && scrollbar.init(event.x(), event.y())) {
            this.load_Z = true;
            this.write = scrollbar.load(event.x(), event.y()) ? Math.max(0, (int)Math.round(event.y()) - scrollbar.update()) : scrollbar.sync() / 2;
            this.init_V_I_2(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.init(scrollbar, event.y(), this.write));
            if (this.init_k != null) {
               this.init_k.push(false);
            }

            return true;
         }

         for (JoinMacroScreen.Inner_DataUtil button : this.load_List) {
            if (button.init(event.x(), event.y())) {
               if (button.init_Z && button.init_Runnable != null) {
                  button.init_Runnable.run();
               }

               return true;
            }
         }

         int rowIndex = this.init_I_D_D(event.x(), event.y());
         if (rowIndex >= 0) {
            JoinMacroScreen.Inner_StreamUtil row = this.init_List.get(rowIndex);
            if (row.init_cY_DataUtil.init_Z()) {
               MacroLibraryManager.init_aN().init_V_Str(row.init_cY_DataUtil.load_Str());
               this.write_V_2();
               return true;
            }

            String selected = JoinMacroController.init_Str();
            JoinMacroController.init_V(row.init_Str.equalsIgnoreCase(selected) ? "" : row.init_Str);
            return true;
         }
      }

      if (this.init_k != null) {
         this.init_k.push(false);
      }

      return true;
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      event = init_MouseButtonEvent_MouseButtonEvent(event);
      if (autismclient.gui.ui.widgets.DropdownWidget.write(this.read_List)) {
         return true;
      } else if (this.load_Z) {
         this.load_Z = false;
         return true;
      } else {
         return this.init_k != null && this.init_k.load(event.x(), event.y(), Compat263.mouseEventGlfwButton(event));
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      event = init_MouseButtonEvent_MouseButtonEvent(event);
      dx = cA.init_D_D(dx);
      dy = cA.init_D_D(dy);
      if (autismclient.gui.ui.widgets.DropdownWidget.load(this.read_List, event.x(), event.y(), Compat263.mouseEventGlfwButton(event))) {
         return true;
      } else if (this.load_Z) {
         this.init_V_I_2(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.init(this.init_PoolEntry_DataUtil_2(), event.y(), this.write));
         return true;
      } else {
         return this.init_k != null && this.init_k.init(event.x(), event.y(), Compat263.mouseEventGlfwButton(event), dx, dy);
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      mouseX = cA.init_D_D(mouseX);
      mouseY = cA.init_D_D(mouseY);
      if (autismclient.gui.ui.widgets.DropdownWidget.init_Z_List(this.read_List, mouseX, mouseY, scrollY)) {
         return true;
      }

      if (this.init_k != null && this.init_k.init_Z_D(mouseX, mouseY, scrollY)) {
         return true;
      }

      if (mouseX >= this.start() && mouseX < this.start() + this.next() && mouseY >= this.stop() && mouseY < this.stop() + this.valid()) {
         int maxScroll = Math.max(0, this.init_List.size() - this.data());
         if (maxScroll > 0) {
            this.read_I = Math.max(0, Math.min(maxScroll, this.read_I - (int)Math.signum(scrollY)));
         }

         return true;
      } else {
         return true;
      }
   }

   @Override
   public boolean keyPressed(KeyEvent input) {
      if (this.init_k != null && this.init_k.init_Z_KeyEvent(input)) {
         return true;
      }

      if (Compat263.keyEventGlfwKey(input) == 256) {
         if (autismclient.gui.ui.widgets.DropdownWidget.read(this.read_List)) {
            return true;
         }

         if (this.minecraft != null) {
            this.minecraft.gui.setScreen(this.init_Screen);
         }
         return true;
      } else {
         return true;
      }
   }

   @Override
   public boolean charTyped(CharacterEvent input) {
      return this.init_k != null && this.init_k.init_Z_CharacterEvent(input);
   }

   @Override
   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.init_Screen);
      }
   }

   private void read_V_2() {
      String query = this.init_Str();
      long revision = MacroLibraryManager.init_aN().init_J();
      if (this.init_Z || !query.equals(this.init_Str) || revision != this.init_J) {
         this.write_V_2();
      }
   }

   private void write_V_2() {
      String query = this.init_Str();
      this.init_Str = query;
      this.init_J = MacroLibraryManager.init_aN().init_J();
      this.init_Z = false;
      this.init_List.clear();

      for (MacroFolderLayout.Inner_DataUtil item : MacroLibraryManager.init_aN().init_List_Str(query)) {
         this.init_List
            .add(
               new JoinMacroScreen.Inner_StreamUtil(item.init_Str(), !item.init_Z() && item.init_aM().init_List != null ? item.init_aM().init_List.size() : 0, item)
            );
      }

      this.read_I = Math.max(0, Math.min(this.read_I, Math.max(0, this.init_List.size() - this.data())));
   }

   private String init_Str() {
      return this.init_k == null ? "" : this.init_k.load_Str().trim().toLowerCase(Locale.ROOT);
   }

   private AutismMacro init_aM() {
      String selected = JoinMacroController.init_Str();
      return selected.isBlank() ? null : MacroLibraryManager.init_aN().init_aM_Str(selected);
   }

   private void init_V_aM_2(AutismMacro macro) {
      AutismMacroEditorOverlay editor = AutismMacroEditorOverlay.init_AutismMacroEditorO_2();
      OverlayManager.init_bk().init_V_cX_cX_DataUtil(editor, AutismOverlay.Inner_DataUtil.HOST_SCREEN);
      editor.init_V_aM_Consumer_2(macro, saved -> {
         if (saved != null && saved.init_Str != null && !saved.init_Str.isBlank()) {
            JoinMacroController.init_V(saved.init_Str);
            bi.init("Join macro selected: " + saved.init_Str, -10035062);
         }
      });
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(new AutismOverlaysScreen(editor, new JoinMacroScreen(this.init_Screen)));
      }
   }

   private int init_I_D_D(double mouseX, double mouseY) {
      int y = this.size();
      int pitch = this.sync();
      int h = this.data() * pitch;
      if (!(mouseX < this.value()) && !(mouseX >= this.init_I_PoolEntry_DataUtil(this.init_PoolEntry_DataUtil_2())) && !(mouseY < y) && !(mouseY >= y + h)) {
         int local = (int)mouseY - y;
         if (local % pitch >= this.update_I_2()) {
            return -1;
         }

         int index = this.read_I + local / pitch;
         return index >= 0 && index < this.init_List.size() ? index : -1;
      } else {
         return -1;
      }
   }

   private void update_V_2() {
      if (this.init_k != null) {
         this.init_k.read(this.set());
         this.init_k.write(this.get());
         this.init_k.update(this.put());
         this.init_k.sync(this.write_I_2());
      }
   }

   private int init_I_Str_3(String label) {
      return autismclient.gui.ui.UiDensityMetrics.load(autismclient.gui.f.b.k.init(this.font, label, autismclient.gui.f.a.DataUtil.init, 0));
   }

   private int init_I_I_I_2(int top, int height) {
      return autismclient.gui.f.UiHelper.init(this.font).init_I(autismclient.gui.f.NetHelper.init(0, top, 1, height));
   }

   private int read_I_2() {
      int w = 0;

      for (String label : new String[]{"Execution Method", "Run On", "Repeat On"}) {
         w = Math.max(w, autismclient.gui.f.b.k.init(this.font, label, autismclient.gui.f.a.DataUtil.init, 0));
      }

      return w;
   }

   private int write_I_2() {
      return autismclient.gui.ui.UiDensityMetrics.init(13);
   }

   private int update_I_2() {
      return autismclient.gui.ui.UiDensityMetrics.init(14);
   }

   private int sync() {
      return this.update_I_2() + 2;
   }

   private int flush() {
      return Math.max(6, (this.init_I() - this.close()) / 2);
   }

   private int close() {
      return Math.max(1, Math.min(382, this.init_I() - 12));
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
      return this.pop() + this.write_I_2() + 2;
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

      for (String label : init_StrArr) {
         buttons += this.init_I_Str_3(label) + 2;
      }

      return Math.max(40, this.poll() - buttons);
   }

   private int add() {
      return this.get() + this.write_I_2() + 2;
   }

   private int clear() {
      return this.add() + this.write_I_2() + 2;
   }

   private int reset() {
      return this.clear() + this.write_I_2() + 4 - this.push();
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
      int available = Math.max(this.prev() + this.update_I_2() + 4, this.load_I() - 6 - this.stop());
      int cap = Math.max(3, this.load_I() * 65 / 100 / this.sync());
      int shown = Math.max(3, Math.min(cap, this.init_List.size()));
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

   private int init_I_PoolEntry_DataUtil(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Inner_DataUtil scrollbar) {
      int right = this.start() + this.next() - 4;
      return scrollbar.init_Z() ? this.cache() - 2 : right;
   }

   private int cache() {
      return this.start() + this.next() - 4 - 6;
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Inner_DataUtil init_PoolEntry_DataUtil_2() {
      int pitch = this.sync();
      int trackY = this.size();
      int trackH = Math.max(1, this.data() * pitch - 2);
      int contentPixels = Math.max(0, this.init_List.size() * pitch);
      int viewPixels = Math.max(1, this.data() * pitch);
      return autismclient.gui.ui.widgets.ScrollbarMetricsHelper.init_PoolEntry_DataUtil_I_2(contentPixels, viewPixels, this.cache(), trackY, 6, trackH, this.read_I * pitch);
   }

   private void init_V_I_2(int scrollPixels) {
      int maxScrollRows = Math.max(0, this.init_List.size() - this.data());
      this.read_I = Math.max(0, Math.min(maxScrollRows, Math.round((float)scrollPixels / this.sync())));
   }

   private JoinMacroController.Inner_CachePool[] init_aA_CachePoolArr() {
      return JoinMacroController.init_Z()
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
      return triggerJoin.init_Str_Z(JoinMacroController.init_Z());
   }

   private record Inner_DataUtil(
      int init_I,
      int load,
      int read,
      int write,
      String init_Str,
      autismclient.gui.ui.widgets.OverlayActionButton.Inner_StreamUtil init_ByteReader_StreamU,
      boolean init_Z,
      Runnable init_Runnable
   ) {
      boolean init(double mx, double my) {
         return mx >= this.init_I && mx < this.init_I + this.read && my >= this.load && my < this.load + this.write;
      }
   }

   private record Inner_StreamUtil(String init_Str, int init_I, MacroFolderLayout.Inner_DataUtil init_cY_DataUtil) {
   }
}
