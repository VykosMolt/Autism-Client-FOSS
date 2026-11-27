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
   private final ModulesMenuController init_ConfigStore;
   private final Module init_Module;
   private final Setting<?, ?> init_Setting;
   private final NetHelper init_NetHelper;
   private final InlineTextEditState init_StreamUtil = new InlineTextEditState(this, "", value -> {});
   private UiTextRenderer init_PoolEntry;
   private boolean init_Z;
   private int load_I;
   private int read_I = 12;
   private NetHelper load_NetHelper = NetHelper.init(0, 0, 0, 0);
   private NetHelper read_NetHelper = NetHelper.init(0, 0, 0, 0);
   private NetHelper write = NetHelper.init(0, 0, 0, 0);
   private NetHelper update = NetHelper.init(0, 0, 0, 0);
   private NetHelper sync = NetHelper.init(0, 0, 0, 0);
   private List<String> init_List = List.of();

   MacroPickerPopup(ModulesMenuController controller, Module module, Setting<?, ?> option, NetHelper anchor) {
      this.init_ConfigStore = controller;
      this.init_Module = module;
      this.init_Setting = option;
      this.init_NetHelper = anchor == null ? NetHelper.init(0, 0, 120, 16) : anchor;
      this.init();
   }

   void init(SyncManager context) {
      this.init_PoolEntry = context.init_PoolEntry();
      UiTheme theme = context.init_TableIndex();
      this.read_I = UiDensityMetrics.scaledHeight(12);
      int controlH = UiDensityMetrics.scaledHeight(13);
      int pad = 3;
      int width = Math.min(Math.max(this.init_NetHelper.update(), ModulesMenuController.scaled(150)), Math.max(120, context.init_I() - 8));
      int rows = Math.max(1, Math.min(8, Math.max(this.init_List.size(), 1)));
      int listH = rows * this.read_I;
      int height = pad + controlH + 2 + controlH + 2 + listH + pad;
      int x = ModulesMenuController.clampInt(this.init_NetHelper.read(), 4, Math.max(4, context.init_I() - width - 4));
      int below = this.init_NetHelper.load() + 2;
      int above = this.init_NetHelper.write() - height - 2;
      int y = below + height > context.load_I() - 4 && above >= 4 ? above : below;
      y = ModulesMenuController.clampInt(y, 4, Math.max(4, context.load_I() - height - 4));
      this.load_NetHelper = NetHelper.init(x, y, width, height);
      this.read_NetHelper = NetHelper.init(x + pad, y + pad, width - pad * 2, controlH);
      int half = (width - pad * 2 - 2) / 2;
      this.write = NetHelper.init(x + pad, this.read_NetHelper.load() + 2, half, controlH);
      this.update = NetHelper.init(this.write.init() + 2, this.read_NetHelper.load() + 2, width - pad * 2 - 2 - half, controlH);
      this.sync = NetHelper.init(x + pad, this.update.load() + 2, width - pad * 2, listH);
      PacketUtil.init(context, this.load_NetHelper, theme.colors().popupSurface, theme.colors().borderStrong);
      TextFieldRenderer.init(
         context,
         this.read_NetHelper,
         this.init_StreamUtil.text,
         "Search macros",
         true,
         this.init_StreamUtil.cursor,
         this.init_StreamUtil.selectionStart(),
         this.init_StreamUtil.selectionEnd(),
         this.init_StreamUtil.init(this.read_NetHelper.update() - 8, fragment -> this.init_PoolEntry.measureFragmentWidth(this.init_StreamUtil.text, fragment))
      );
      autismclient.gui.f.b.CachePool.init(
         context, this.write, "Clear", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, this.write.init_Z(context.read(), context.write()), false
      );
      autismclient.gui.f.b.CachePool.init(
         context, this.update, "New", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, this.update.init_Z(context.read(), context.write()), false
      );
      context.init_GuiGraphicsExtract().enableScissor(this.sync.read(), this.sync.write(), this.sync.init(), this.sync.load());

      try {
         int rowY = this.sync.write() - this.load_I;

         for (String name : this.init_List) {
            NetHelper row = NetHelper.init(this.sync.read(), rowY, this.sync.update(), this.read_I);
            if (row.load() >= this.sync.write() && row.write() <= this.sync.load()) {
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

            rowY += this.read_I;
         }

         if (this.init_List.isEmpty()) {
            context.init_PoolEntry()
               .drawCentered(
                  context.init_GuiGraphicsExtract(),
                  "No macros",
                  NetHelper.init(this.sync.read(), this.sync.write(), this.sync.update(), this.read_I),
                  theme.colors().textSecondary
               );
         }
      } finally {
         context.init_GuiGraphicsExtract().disableScissor();
      }
   }

   boolean init_Z_I_2(int mouseX, int mouseY, int button) {
      return this.init(mouseX, mouseY, button, 0);
   }

   boolean init(int mouseX, int mouseY, int button, int modifiers) {
      if (!this.load_NetHelper.init_Z(mouseX, mouseY)) {
         return false;
      }

      if (button != 0) {
         return true;
      }

      if (this.read_NetHelper.init_Z(mouseX, mouseY)) {
         this.init_StreamUtil.init(this.init(mouseX), (modifiers & 1) != 0);
         this.init_Z = true;
         return true;
      }

      if (this.write.init_Z(mouseX, mouseY)) {
         this.init_ConfigStore.setValue(this.init_Module, this.init_Setting, "");
         return false;
      }

      if (this.update.init_Z(mouseX, mouseY)) {
         this.init_ConfigStore.init_V_Module_Setting(this.init_Module, this.init_Setting);
         return false;
      }

      if (this.sync.init_Z(mouseX, mouseY)) {
         int index = (mouseY - this.sync.write() + this.load_I) / this.read_I;
         if (index >= 0 && index < this.init_List.size()) {
            this.init_ConfigStore.setValue(this.init_Module, this.init_Setting, this.init_List.get(index));
            return false;
         }
      }

      return true;
   }

   boolean load(int mouseX, int mouseY, int button) {
      if (button == 0) {
         this.init_Z = false;
      }

      return true;
   }

   boolean init(int mouseX, int mouseY, int button, double dx, double dy) {
      if (button == 0 && this.init_Z) {
         this.init_StreamUtil.init(this.init(mouseX), true);
      }

      return true;
   }

   private int init(int mouseX) {
      return this.init_PoolEntry == null
         ? this.init_StreamUtil.cursor
         : this.init_StreamUtil
            .indexAt(
               mouseX,
               this.read_NetHelper.read() + 4 - this.init_StreamUtil.scroll(),
               fragment -> this.init_PoolEntry.measureFragmentWidth(this.init_StreamUtil.text, fragment)
            );
   }

   boolean mouseScrolled(int mouseX, int mouseY, double amount) {
      int max = Math.max(0, this.init_List.size() * this.read_I - this.sync.sync());
      this.load_I = ModulesMenuController.clampInt(this.load_I - (int)Math.round(amount * this.read_I), 0, max);
      return true;
   }

   boolean read(int key, int scanCode, int modifiers) {
      String before = this.init_StreamUtil.text;
      this.init_StreamUtil.init(key, modifiers);
      if (!before.equals(this.init_StreamUtil.text)) {
         this.init();
      }

      return true;
   }

   boolean init(char chr) {
      String before = this.init_StreamUtil.text;
      this.init_StreamUtil.typeChar(chr);
      if (!before.equals(this.init_StreamUtil.text)) {
         this.init();
      }

      return true;
   }

   private void init() {
      List<String> names = new ArrayList<>();
      String needle = this.init_StreamUtil.text.toLowerCase(Locale.ROOT);
      if (this.init_Setting != null && this.init_Setting.displayMode() == DisplayMode.CONDITIONAL_MACRO_PICKER) {
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

      this.init_List = names;
      this.load_I = 0;
   }
}
