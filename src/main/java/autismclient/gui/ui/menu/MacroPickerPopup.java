package autismclient.gui.ui.menu;

import autismclient.api.module.DisplayMode;
import autismclient.api.module.Setting;
import autismclient.gui.f.NetHelper;
import autismclient.gui.f.PacketUtil;
import autismclient.gui.ui.UiTextRenderer;
import autismclient.gui.ui.UiDensityMetrics;
import autismclient.gui.f.SyncManager;
import autismclient.gui.ui.UiTheme;
import autismclient.gui.ui.widgets.TextFieldRenderer;
import autismclient.modules.Module;
import autismclient.util.AutismMacro;
import autismclient.util.MacroLibraryManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class MacroPickerPopup {
   private static final int init_I = 8;
   private final ModulesMenuController controller;
   private final Module module;
   private final Setting<?, ?> option;
   private final NetHelper anchor;
   private final InlineTextEditState searchState = new InlineTextEditState(this, "", value -> {});
   private UiTextRenderer textRenderer;
   private boolean selectingText;
   private int scrollOffset;
   private int rowHeight = 12;
   private NetHelper panelRect = NetHelper.init(0, 0, 0, 0);
   private NetHelper searchRect = NetHelper.init(0, 0, 0, 0);
   private NetHelper clearRect = NetHelper.init(0, 0, 0, 0);
   private NetHelper newRect = NetHelper.init(0, 0, 0, 0);
   private NetHelper listRect = NetHelper.init(0, 0, 0, 0);
   private List<String> names = List.of();

   MacroPickerPopup(ModulesMenuController controller, Module module, Setting<?, ?> option, NetHelper anchor) {
      this.controller = controller;
      this.module = module;
      this.option = option;
      this.anchor = anchor == null ? NetHelper.init(0, 0, 120, 16) : anchor;
      this.refreshNames();
   }

   void init(SyncManager context) {
      this.textRenderer = context.init_PoolEntry();
      UiTheme theme = context.init_TableIndex();
      this.rowHeight = UiDensityMetrics.scaledHeight(12);
      int controlH = UiDensityMetrics.scaledHeight(13);
      int pad = 3;
      int width = Math.min(Math.max(this.anchor.update(), ModulesMenuController.scaled(150)), Math.max(120, context.init_I() - 8));
      int rows = Math.max(1, Math.min(8, Math.max(this.names.size(), 1)));
      int listH = rows * this.rowHeight;
      int height = pad + controlH + 2 + controlH + 2 + listH + pad;
      int x = ModulesMenuController.clampInt(this.anchor.read(), 4, Math.max(4, context.init_I() - width - 4));
      int below = this.anchor.load() + 2;
      int above = this.anchor.write() - height - 2;
      int y = below + height > context.load_I() - 4 && above >= 4 ? above : below;
      y = ModulesMenuController.clampInt(y, 4, Math.max(4, context.load_I() - height - 4));
      this.panelRect = NetHelper.init(x, y, width, height);
      this.searchRect = NetHelper.init(x + pad, y + pad, width - pad * 2, controlH);
      int half = (width - pad * 2 - 2) / 2;
      this.clearRect = NetHelper.init(x + pad, this.searchRect.load() + 2, half, controlH);
      this.newRect = NetHelper.init(this.clearRect.init() + 2, this.searchRect.load() + 2, width - pad * 2 - 2 - half, controlH);
      this.listRect = NetHelper.init(x + pad, this.newRect.load() + 2, width - pad * 2, listH);
      PacketUtil.init(context, this.panelRect, theme.colors().popupSurface, theme.colors().borderStrong);
      TextFieldRenderer.init(
         context,
         this.searchRect,
         this.searchState.text,
         "Search macros",
         true,
         this.searchState.cursor,
         this.searchState.selectionStart(),
         this.searchState.selectionEnd(),
         this.searchState.init(this.searchRect.update() - 8, fragment -> this.textRenderer.measureFragmentWidth(this.searchState.text, fragment))
      );
      autismclient.gui.f.b.CachePool.init(
         context, this.clearRect, "Clear", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, this.clearRect.init_Z(context.read(), context.write()), false
      );
      autismclient.gui.f.b.CachePool.init(
         context, this.newRect, "New", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, this.newRect.init_Z(context.read(), context.write()), false
      );
      context.init_GuiGraphicsExtract().enableScissor(this.listRect.read(), this.listRect.write(), this.listRect.init(), this.listRect.load());

      try {
         int rowY = this.listRect.write() - this.scrollOffset;

         for (String name : this.names) {
            NetHelper row = NetHelper.init(this.listRect.read(), rowY, this.listRect.update(), this.rowHeight);
            if (row.load() >= this.listRect.write() && row.write() <= this.listRect.load()) {
               boolean hovered = row.init_Z(context.read(), context.write());
               float hoverT = autismclient.gui.ui.HoverTransitionCache.sample("macropick:" + name, hovered);
               if (hoverT > 0.001F) {
                  PacketUtil.load(context.init_GuiGraphicsExtract(), row, 3, ModulesMenuController.lerpColor(0, 385875967, hoverT));
               }

               context.init_PoolEntry()
                  .drawFitted(
                     context.init_GuiGraphicsExtract(),
                     name,
                     row.read() + 5,
                     context.init_PoolEntry().centerTextY(row),
                     row.update() - 10,
                     theme.colors().textPrimary
                  );
            }

            rowY += this.rowHeight;
         }

         if (this.names.isEmpty()) {
            context.init_PoolEntry()
               .drawCentered(
                  context.init_GuiGraphicsExtract(),
                  "No macros",
                  NetHelper.init(this.listRect.read(), this.listRect.write(), this.listRect.update(), this.rowHeight),
                  theme.colors().textSecondary
               );
         }
      } finally {
         context.init_GuiGraphicsExtract().disableScissor();
      }
   }

   boolean click(int mouseX, int mouseY, int button) {
      return this.init(mouseX, mouseY, button, 0);
   }

   boolean init(int mouseX, int mouseY, int button, int modifiers) {
      if (!this.panelRect.init_Z(mouseX, mouseY)) {
         return false;
      }

      if (button != 0) {
         return true;
      }

      if (this.searchRect.init_Z(mouseX, mouseY)) {
         this.searchState.init(this.cursorIndexAt(mouseX), (modifiers & 1) != 0);
         this.selectingText = true;
         return true;
      }

      if (this.clearRect.init_Z(mouseX, mouseY)) {
         this.controller.setValue(this.module, this.option, "");
         return false;
      }

      if (this.newRect.init_Z(mouseX, mouseY)) {
         this.controller.init_V_Module_Setting(this.module, this.option);
         return false;
      }

      if (this.listRect.init_Z(mouseX, mouseY)) {
         int index = (mouseY - this.listRect.write() + this.scrollOffset) / this.rowHeight;
         if (index >= 0 && index < this.names.size()) {
            this.controller.setValue(this.module, this.option, this.names.get(index));
            return false;
         }
      }

      return true;
   }

   boolean load(int mouseX, int mouseY, int button) {
      if (button == 0) {
         this.selectingText = false;
      }

      return true;
   }

   boolean init(int mouseX, int mouseY, int button, double dx, double dy) {
      if (button == 0 && this.selectingText) {
         this.searchState.init(this.cursorIndexAt(mouseX), true);
      }

      return true;
   }

   private int cursorIndexAt(int mouseX) {
      return this.textRenderer == null
         ? this.searchState.cursor
         : this.searchState
            .indexAt(
               mouseX,
               this.searchRect.read() + 4 - this.searchState.scroll(),
               fragment -> this.textRenderer.measureFragmentWidth(this.searchState.text, fragment)
            );
   }

   boolean mouseScrolled(int mouseX, int mouseY, double amount) {
      int max = Math.max(0, this.names.size() * this.rowHeight - this.listRect.sync());
      this.scrollOffset = ModulesMenuController.clampInt(this.scrollOffset - (int)Math.round(amount * this.rowHeight), 0, max);
      return true;
   }

   boolean read(int key, int scanCode, int modifiers) {
      String before = this.searchState.text;
      this.searchState.init(key, modifiers);
      if (!before.equals(this.searchState.text)) {
         this.refreshNames();
      }

      return true;
   }

   boolean init(char chr) {
      String before = this.searchState.text;
      this.searchState.typeChar(chr);
      if (!before.equals(this.searchState.text)) {
         this.refreshNames();
      }

      return true;
   }

   private void refreshNames() {
      List<String> names = new ArrayList<>();
      String needle = this.searchState.text.toLowerCase(Locale.ROOT);
      if (this.option != null && this.option.displayMode() == DisplayMode.CONDITIONAL_MACRO_PICKER) {
         for (String preset : new String[]{"AutoFish - Free Slots Stop", "AutoFish - Durability Stop", "AutoFish - Custom Stop"}) {
            if (needle.isEmpty() || preset.toLowerCase(Locale.ROOT).contains(needle)) {
               names.add(preset);
            }
         }
      }

      for (AutismMacro macro : MacroLibraryManager.getInstance().getMacros()) {
         if (macro != null && macro.name != null && (needle.isEmpty() || macro.name.toLowerCase(Locale.ROOT).contains(needle))) {
            names.add(macro.name);
         }
      }

      this.names = names;
      this.scrollOffset = 0;
   }
}
