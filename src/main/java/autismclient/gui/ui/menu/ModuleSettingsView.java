package autismclient.gui.ui.menu;

import autismclient.api.module.ActionSetting;
import autismclient.api.module.DisplayMode;
import autismclient.api.module.Kind;
import autismclient.api.module.Setting;
import autismclient.api.module.ValueRange;
import autismclient.gui.f.NetHelper;
import autismclient.gui.f.PacketUtil;
import autismclient.gui.ui.UiDensityMetrics;
import autismclient.gui.f.SyncManager;
import autismclient.gui.ui.UiTheme;
import autismclient.gui.ui.widgets.KeybindButtonRenderer;
import autismclient.gui.ui.widgets.RangeSliderRenderer;
import autismclient.gui.ui.widgets.CrosshairPreviewRenderer;
import autismclient.gui.ui.widgets.DropdownMenu;
import autismclient.gui.ui.widgets.GroupHeaderRenderer;
import autismclient.gui.ui.widgets.SliderRenderer;
import autismclient.gui.ui.widgets.TextFieldRenderer;
import autismclient.gui.ui.widgets.ToggleSwitchRenderer;
import autismclient.gui.f.b.k;
import autismclient.modules.Module;
import autismclient.modules.CrosshairModule;
import autismclient.util.ValueSink;
import autismclient.util.MacroLibraryManager;
import autismclient.util.PacketNameResolver;
import autismclient.util.PacketFilterListCodec;
import autismclient.util.PipeDelimitedListUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.Identifier;

final class ModuleSettingsView {
   private static final int init_I = 34;
   private static final int load_I = 40;
   private static final int read = 62;
   private final ModulesMenuController init_ConfigStore;
   private final Map<String, String> init_Map = new HashMap<>();
   private final Map<String, ModuleSettingsView.Inner_DataUtil> load_Map = new HashMap<>();
   private String init_Str = "";
   private boolean init_Z;

   ModuleSettingsView(ModulesMenuController controller) {
      this.init_ConfigStore = controller;
   }

   void setClassic(boolean classic) {
      this.init_Z = classic;
   }

   void onModuleOpened(Module module) {
      this.load_Map.clear();
      this.init_Str = "";
      this.init_ConfigStore.setScroll("settings", 0.0F);
   }

   String getQuery() {
      return this.init_Str;
   }

   void setQuery(String query) {
      this.init_Str = query == null ? "" : query;
      this.init_ConfigStore.setScroll("settings", 0.0F);
   }

   boolean hasVisibleSettings(Module module) {
      if (module == null) {
         return false;
      }

      for (Setting<?, ?> option : module.settings()) {
         if (option.isVisible() && this.init_Z_Setting(option)) {
            return true;
         }
      }

      return false;
   }

   private boolean init_Z_Setting(Setting<?, ?> option) {
      Minecraft minecraft = Minecraft.getInstance();
      boolean inWorld = minecraft != null && minecraft.level != null;
      boolean inContainer = this.init_ConfigStore.getHost().screen() instanceof AbstractContainerScreen;
      return option.isAvailable(inWorld, inContainer);
   }

   void render(SyncManager context, NetHelper content, Module module, float frameSeconds) {
      if (!this.hasVisibleSettings(module)) {
         this.load(context, content, module, frameSeconds);
      } else {
         UiTheme theme = context.init_TableIndex();
         if (this.init_Z) {
            this.load(context, content, module, theme);
         } else {
            this.init_V_SyncManager_3(context, content, module, theme);
         }

         int headerH = this.init_I();
         int groupH = load();
         NetHelper listArea = NetHelper.init(content.read(), content.write() + headerH, content.update(), content.sync() - headerH);
         if (!module.settingsHint().isBlank()) {
            context.init_PoolEntry()
               .draw(
                  context.init_GuiGraphicsExtract(), module.settingsHint(), listArea.read() + 5, listArea.write() + 3, theme.colors().textSecondary
               );
            int hintH = this.load_I_Module(module);
            listArea = NetHelper.init(listArea.read(), listArea.write() + hintH, listArea.update(), Math.max(0, listArea.sync() - hintH));
         }

         CrosshairPreviewLayout preview = module instanceof CrosshairModule ? CrosshairPreviewLayout.init(listArea) : null;
         if (preview != null) {
            listArea = preview.listArea();
         }

         List<ModuleSettingsView.Inner_StreamUtil> rows = this.init_List_Module(module);
         int contentHeight = this.init_I_List(rows) + 4;
         int scroll = this.init_I_Str("settings", listArea, contentHeight, frameSeconds);
         int y = listArea.write() + 2 - scroll;
         this.init_ConfigStore.pushClip(context, listArea);

         try {
            for (ModuleSettingsView.Inner_StreamUtil row : rows) {
               if (row.init_Str() != null) {
                  NetHelper bounds = NetHelper.init(listArea.read() + 3, y, listArea.update() - 6 - (contentHeight > listArea.sync() ? 6 : 0), groupH);
                  if (bounds.load() >= listArea.write() && bounds.write() <= listArea.load()) {
                     boolean collapsed = this.init_ConfigStore.isCollapsed(module, row.init_Str());
                     boolean hovered = bounds.init_Z(context.read(), context.write());
                     GroupHeaderRenderer.init(context, bounds, row.init_Str(), collapsed, hovered);
                     this.init_ConfigStore.addHit(ModulesMenuController.Inner_CachePool.GROUP, bounds, module, null, row.init_Str(), -1);
                  }

                  y += groupH + 1;
               } else if (row.init_Setting() != null) {
                  int rowH = this.init_I_Setting(row.init_Setting());
                  NetHelper bounds = NetHelper.init(listArea.read() + 3, y, listArea.update() - 6 - (contentHeight > listArea.sync() ? 6 : 0), rowH);
                  if (bounds.load() >= listArea.write() && bounds.write() <= listArea.load()) {
                     this.init_V_SyncManager_4(context, module, row.init_Setting(), bounds);
                  }

                  y += rowH + 1;
               }
            }
         } finally {
            this.init_ConfigStore.popClip(context);
         }

         this.init_V_SyncManager(context, listArea, contentHeight, scroll);
         if (preview != null && module instanceof CrosshairModule crosshair) {
            CrosshairPreviewRenderer.init(context, preview.load(), crosshair.init_q_DataUtil_m(crosshair.init_m()));
         }
      }
   }

   private void load(SyncManager context, NetHelper content, Module module, float frameSeconds) {
      int pad = 8;
      int controlH = UiDensityMetrics.scaledHeight(13);
      int rowH = controlH + 8;
      int x = content.read() + pad;
      int contentHeight = this.contentHeight(module);
      int scroll = this.init_I_Str("settings", content, contentHeight, frameSeconds);
      int w = Math.max(1, content.update() - pad * 2 - (contentHeight > content.sync() ? 6 : 0));
      int y = content.write() + pad - scroll;
      this.init_ConfigStore.pushClip(context, content);

      try {
         if (!this.init_Z) {
            NetHelper back = NetHelper.init(x, y, UiDensityMetrics.buttonWidthForText(context.init_PoolEntry().measureWidth("Back")), controlH);
            autismclient.gui.f.b.CachePool.init(
               context, back, "Back", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, back.init_Z(context.read(), context.write()), false
            );
            this.init_ConfigStore.addHit(ModulesMenuController.Inner_CachePool.BACK, back, module, null, "", -1);
            y += rowH;
         }

         if (module.hasActivationToggle()) {
            NetHelper row = NetHelper.init(x, y, w, rowH);
            context.init_PoolEntry()
               .draw(
                  context.init_GuiGraphicsExtract(), "Enabled", x, context.init_PoolEntry().centerTextY(row), context.init_TableIndex().colors().textPrimary
               );
            NetHelper toggle = ModulesMenuController.togglePillBounds(row, row.init());
            ToggleSwitchRenderer.init(context, toggle, module.isEnabled(), toggle.init_Z(context.read(), context.write()), "settings-toggle:" + module.id());
            this.init_ConfigStore.addHit(ModulesMenuController.Inner_CachePool.TOGGLE_MODULE, toggle, module, null, "", -1);
            y += rowH;
         }

         NetHelper row = NetHelper.init(x, y, w, rowH);
         context.init_PoolEntry()
            .draw(
               context.init_GuiGraphicsExtract(), "Keybind", x, context.init_PoolEntry().centerTextY(row), context.init_TableIndex().colors().textPrimary
            );
         boolean listening = this.init_ConfigStore.isListeningForBind(module);
         String label = listening ? "Press a key" : (module.keybind() == -1 ? "Unbound" : ValueSink.init_Str(module.keybind()));
         int bindW = Math.min(Math.max(70, UiDensityMetrics.buttonWidthForText(context.init_PoolEntry().measureWidth(label))), Math.max(1, w / 2));
         NetHelper bind = NetHelper.init(row.init() - bindW, y + 4, bindW, controlH);
         autismclient.gui.f.b.CachePool.init(
            context,
            bind,
            label,
            listening ? autismclient.gui.f.b.CachePool.Inner_DataUtil.read : autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
            bind.init_Z(context.read(), context.write()),
            listening
         );
         this.init_ConfigStore.addHit(ModulesMenuController.Inner_CachePool.BIND_MODULE, bind, module, null, "", -1);
         y += rowH + 4;
         String hint = listening ? "Esc, Backspace or Delete clears the bind" : "Click the keybind to change it";
         context.init_PoolEntry().drawEllipsized(context.init_GuiGraphicsExtract(), hint, x, y, w, context.init_TableIndex().colors().textSecondary);
         y += UiDensityMetrics.scaledHeight(12) + 8;
         NetHelper macro = NetHelper.init(x, y, w, controlH);
         autismclient.gui.f.b.CachePool.init(
            context, macro, "Create toggle macro", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, macro.init_Z(context.read(), context.write()), false
         );
         this.init_ConfigStore.addHit(ModulesMenuController.Inner_CachePool.MACRO_MODULE, macro, module, null, "", -1);
      } finally {
         this.init_ConfigStore.popClip(context);
      }

      this.init_V_SyncManager(context, content, contentHeight, scroll);
   }

   private void init_V_SyncManager_3(SyncManager context, NetHelper content, Module module, UiTheme theme) {
      int headerH = this.init_I();
      NetHelper strip = NetHelper.init(content.read(), content.write(), content.update(), headerH - 1);
      int controlH = UiDensityMetrics.scaledHeight(13);
      int controlY = strip.write() + Math.max(0, (strip.sync() - controlH) / 2);
      NetHelper back = NetHelper.init(content.read() + 3, controlY, UiDensityMetrics.buttonWidthForText(context.init_PoolEntry().measureWidth("Back")), controlH);
      autismclient.gui.f.b.CachePool.init(
         context, back, "Back", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, back.init_Z(context.read(), context.write()), false
      );
      this.init_ConfigStore.addHit(ModulesMenuController.Inner_CachePool.BACK, back, module, null, "", -1);
      NetHelper pill = ModulesMenuController.togglePillBounds(strip, content.init() - 4);
      int right = pill.read() - 4;
      NetHelper bind = NetHelper.init(right - 34, controlY, 34, controlH);
      right = bind.read() - 2;
      int resetW = UiDensityMetrics.buttonWidthForText(context.init_PoolEntry().measureWidth("Reset"));
      NetHelper reset = NetHelper.init(right - resetW, controlY, resetW, controlH);
      right = reset.read() - 2;
      int macroW = UiDensityMetrics.buttonWidthForText(context.init_PoolEntry().measureWidth("+Macro"));
      NetHelper macro = NetHelper.init(right - macroW, controlY, macroW, controlH);
      autismclient.gui.f.b.CachePool.init(
         context, macro, "+Macro", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, macro.init_Z(context.read(), context.write()), false
      );
      this.init_ConfigStore.addHit(ModulesMenuController.Inner_CachePool.MACRO_MODULE, macro, module, null, "", -1);
      autismclient.gui.f.b.CachePool.init(
         context, reset, "Reset", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, reset.init_Z(context.read(), context.write()), false
      );
      this.init_ConfigStore.addHit(ModulesMenuController.Inner_CachePool.RESET_MODULE, reset, module, null, "", -1);
      KeybindButtonRenderer.init(context, bind, module.keybind(), this.init_ConfigStore.isListeningForBind(module), bind.init_Z(context.read(), context.write()));
      this.init_ConfigStore.addHit(ModulesMenuController.Inner_CachePool.BIND_MODULE, bind, module, null, "", -1);
      if (module.hasActivationToggle()) {
         ToggleSwitchRenderer.init(context, pill, module.isEnabled(), pill.init_Z(context.read(), context.write()), "settings-toggle:" + module.id());
      }

      this.init_ConfigStore.addHit(ModulesMenuController.Inner_CachePool.TOGGLE_MODULE, pill, module, null, "", -1);
      PacketUtil.init_V_GuiGraphicsExtract_2(
         context.init_GuiGraphicsExtract(), content.read(), content.write() + headerH - 1, content.update(), theme.colors().divider
      );
   }

   private int init_I() {
      return UiDensityMetrics.scaledHeight(13) + (this.init_Z ? 10 : 3);
   }

   private void load(SyncManager context, NetHelper content, Module module, UiTheme theme) {
      int headerH = this.init_I();
      int controlH = UiDensityMetrics.scaledHeight(13);
      int gap = 2;
      int y = content.write() + 5;
      int x = content.read() + 5;
      int right = content.init() - 5;
      int macroW = UiDensityMetrics.buttonWidthForText(context.init_PoolEntry().measureWidth("Macro"));
      int resetW = UiDensityMetrics.buttonWidthForText(context.init_PoolEntry().measureWidth("Reset"));
      NetHelper macro = NetHelper.init(right - macroW, y, macroW, controlH);
      NetHelper reset = NetHelper.init(macro.read() - gap - resetW, y, resetW, controlH);
      int actionsLeft = reset.read() - gap;
      if (module.hasActivationToggle()) {
         boolean on = module.isEnabled();
         int stateW = UiDensityMetrics.buttonWidthForText(context.init_PoolEntry().measureWidth("Disabled"));
         NetHelper active = NetHelper.init(x, y, stateW, controlH);
         autismclient.gui.f.b.CachePool.init(
            context,
            active,
            on ? "Enabled" : "Disabled",
            on ? autismclient.gui.f.b.CachePool.Inner_DataUtil.write : autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
            active.init_Z(context.read(), context.write()),
            on
         );
         this.init_ConfigStore.addHit(ModulesMenuController.Inner_CachePool.TOGGLE_MODULE, active, module, null, "", -1);
         x = active.init() + gap;
      }

      boolean binding = this.init_ConfigStore.isListeningForBind(module);
      String bindLabel = binding ? "Press key" : "Bind " + ValueSink.init_Str(module.keybind());
      int bindW = Math.max(1, Math.min(actionsLeft - x, UiDensityMetrics.buttonWidthForText(context.init_PoolEntry().measureWidth(bindLabel))));
      NetHelper bind = NetHelper.init(x, y, bindW, controlH);
      autismclient.gui.f.b.CachePool.init(
         context, bind, bindLabel, autismclient.gui.f.b.CachePool.Inner_DataUtil.init, bind.init_Z(context.read(), context.write()), binding
      );
      this.init_ConfigStore.addHit(ModulesMenuController.Inner_CachePool.BIND_MODULE, bind, module, null, "", -1);
      autismclient.gui.f.b.CachePool.init(
         context, reset, "Reset", autismclient.gui.f.b.CachePool.Inner_DataUtil.update, reset.init_Z(context.read(), context.write()), false
      );
      this.init_ConfigStore.addHit(ModulesMenuController.Inner_CachePool.RESET_MODULE, reset, module, null, "", -1);
      autismclient.gui.f.b.CachePool.init(
         context, macro, "Macro", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, macro.init_Z(context.read(), context.write()), false
      );
      this.init_ConfigStore.addHit(ModulesMenuController.Inner_CachePool.MACRO_MODULE, macro, module, null, "", -1);
      PacketUtil.init_V_GuiGraphicsExtract_2(
         context.init_GuiGraphicsExtract(), content.read(), content.write() + headerH - 1, content.update(), theme.colors().divider
      );
   }

   private static int load() {
      return UiDensityMetrics.scaledHeight(12);
   }

   private List<ModuleSettingsView.Inner_StreamUtil> init_List_Module(Module module) {
      List<ModuleSettingsView.Inner_StreamUtil> rows = new ArrayList<>();
      Map<String, List<Setting<?, ?>>> groups = new LinkedHashMap<>();
      String query = this.init_Str.trim().toLowerCase(Locale.ROOT);

      for (Setting<?, ?> option : module.settings()) {
         if (option.isVisible()
            && this.init_Z_Setting(option)
            && (query.isEmpty() || option.label().toLowerCase(Locale.ROOT).contains(query) || option.description().toLowerCase(Locale.ROOT).contains(query))) {
            groups.computeIfAbsent(option.group() == null ? "" : option.group(), key -> new ArrayList<>()).add(option);
         }
      }

      Iterator var10 = groups.entrySet().iterator();

      while (true) {
         Entry<String, List<Setting<?, ?>>> entry;
         String group;
         do {
            if (!var10.hasNext()) {
               return rows;
            }

            entry = (Entry<String, List<Setting<?, ?>>>)var10.next();
            group = entry.getKey();
            if (group.isBlank()) {
               break;
            }

            rows.add(new ModuleSettingsView.Inner_StreamUtil(group, null));
         } while (this.init_ConfigStore.isCollapsed(module, group));

         for (Setting<?, ?> option : entry.getValue()) {
            rows.add(new ModuleSettingsView.Inner_StreamUtil(null, option));
         }
      }
   }

   int contentHeight(Module module) {
      if (!this.hasVisibleSettings(module)) {
         int controlH = UiDensityMetrics.scaledHeight(13);
         int rows = 1 + (module.hasActivationToggle() ? 1 : 0) + (this.init_Z ? 0 : 1);
         return 24 + rows * (controlH + 8) + UiDensityMetrics.scaledHeight(12) + controlH;
      } else {
         return this.init_I() + this.load_I_Module(module) + this.init_I_List(this.init_List_Module(module)) + 4;
      }
   }

   private int load_I_Module(Module module) {
      return module.settingsHint().isBlank() ? 0 : UiDensityMetrics.scaledHeight(12) + 6;
   }

   private int init_I_List(List<ModuleSettingsView.Inner_StreamUtil> rows) {
      int height = 0;
      int groupH = load();

      for (ModuleSettingsView.Inner_StreamUtil row : rows) {
         height += (row.init_Str() != null ? groupH : this.init_I_Setting(row.init_Setting())) + 1;
      }

      return height;
   }

   private static String init_Str_Setting(Setting<?, ?> option) {
      String description = option.description();
      if (description != null && !description.isBlank()) {
         return description.trim().split("\\s+").length > 5 ? "" : description;
      } else {
         return "";
      }
   }

   private int init_I_Setting(Setting<?, ?> option) {
      int row = UiDensityMetrics.scaledHeight(14);
      if (!this.init_Z) {
         return row;
      } else {
         return init_Str_Setting(option).isBlank() ? row + 4 : row + 14;
      }
   }

   private void init_V_SyncManager_4(SyncManager context, Module module, Setting<?, ?> option, NetHelper bounds) {
      UiTheme theme = context.init_TableIndex();
      boolean hovered = bounds.init_Z(context.read(), context.write());
      float hoverT = autismclient.gui.ui.HoverTransitionCache.sample("set:" + module.id() + ":" + option.id(), hovered);
      int rowFill = ModulesMenuController.lerpColor(theme.colors().cardSurface, theme.colors().cardSurfaceElevated, hoverT);
      if (this.init_Z) {
         PacketUtil.load(
            context.init_GuiGraphicsExtract(), bounds, 4, rowFill, ModulesMenuController.lerpColor(theme.colors().divider, theme.colors().borderSubtle, hoverT)
         );
      } else {
         PacketUtil.load(context.init_GuiGraphicsExtract(), bounds, 4, rowFill);
      }

      boolean macroDisplay = option.displayMode() == DisplayMode.MACRO_PICKER || option.displayMode() == DisplayMode.CONDITIONAL_MACRO_PICKER;
      boolean plainToggle = !macroDisplay
         && option.kind() == Kind.BOOLEAN
         && option.displayMode() != DisplayMode.FILE_PICKER
         && option.displayMode() != DisplayMode.READONLY_SUMMARY
         && option.displayMode() != DisplayMode.RANGE_SLIDER;
      int controlH = this.init_Z ? Math.min(UiDensityMetrics.scaledHeight(13), bounds.sync() - 2) : Math.max(11, bounds.sync() - 2);
      int controlW = macroDisplay
         ? Math.min(ModulesMenuController.scaled(170), Math.max(ModulesMenuController.scaled(120), bounds.update() / 2))
         : (
            plainToggle
               ? ModulesMenuController.togglePillBounds(bounds, bounds.init()).update()
               : (
                  load_Z_Setting(option)
                     ? Math.min(ModulesMenuController.scaled(190), Math.max(ModulesMenuController.scaled(110), bounds.update() * 3 / 5))
                     : Math.min(ModulesMenuController.scaled(120), Math.max(ModulesMenuController.scaled(90), bounds.update() / 3))
               )
         );
      int labelX = bounds.read() + 5;
      NetHelper control = NetHelper.init(bounds.init() - 4 - controlW, bounds.write() + (bounds.sync() - controlH) / 2, controlW, controlH);
      int labelW = Math.max(40, control.read() - 4 - labelX);
      String tip = init_Str_Setting(option);
      boolean described = this.init_Z && !tip.isBlank();
      if (option.kind() == Kind.ENTITY_TYPE_LIST && option.displayMode() == DisplayMode.DEFAULT) {
         int labelTextW = Math.max(context.init_PoolEntry().measureWidth(option.label()), described ? context.init_PoolEntry().measureWidth(tip) : 0);
         int summaryX = Math.min(control.read(), labelX + labelTextW + 4);
         control = NetHelper.init(summaryX, control.write(), control.init() - summaryX, control.sync());
         labelW = Math.max(1, summaryX - 4 - labelX);
      }

      if (described) {
         context.init_PoolEntry().drawFitted(context.init_GuiGraphicsExtract(), option.label(), labelX, bounds.write() + 4, labelW, theme.colors().textPrimary);
         context.init_PoolEntry().drawFitted(context.init_GuiGraphicsExtract(), tip, labelX, bounds.write() + 15, labelW, theme.colors().textSecondary);
      } else {
         context.init_PoolEntry()
            .drawFitted(context.init_GuiGraphicsExtract(), option.label(), labelX, context.init_PoolEntry().centerTextY(bounds), labelW, theme.colors().textPrimary);
      }

      if (hovered && !tip.isBlank()) {
         this.init_ConfigStore.write(tip);
      }

      if (option.displayMode() == DisplayMode.FILE_PICKER) {
         this.init(context, module, option, control, "Pick", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, ModulesMenuController.Inner_CachePool.FILE_PICKER);
      } else if (option.displayMode() == DisplayMode.READONLY_SUMMARY) {
         this.update(context, module, option, control);
      } else if (macroDisplay) {
         this.init(context, module, option, control, "Select", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, ModulesMenuController.Inner_CachePool.MACRO_PICKER);
      } else if (option.displayMode() == DisplayMode.RANGE_SLIDER) {
         this.read(context, module, option, control);
      } else {
         switch (option.kind()) {
            case BOOLEAN:
               NetHelper toggle = ModulesMenuController.togglePillBounds(bounds, control.init());
               ToggleSwitchRenderer.init(
                  context,
                  toggle,
                  Boolean.parseBoolean(module.value(option.id())),
                  hovered || toggle.init_Z(context.read(), context.write()),
                  module.id() + "/" + option.id()
               );
               this.init_ConfigStore.addHit(ModulesMenuController.Inner_CachePool.OPTION, bounds, module, option, "", -1);
               break;
            case INTEGER:
            case DOUBLE:
               this.load(context, module, option, control);
               break;
            case ENUM:
               int dropW = this.init_I(module, option, control.update());
               NetHelper dropdown = NetHelper.init(control.init() - dropW, control.write(), dropW, control.sync());
               DropdownMenu.renderTrigger(context, dropdown, module.displayValue(option), dropdown.init_Z(context.read(), context.write()), false);
               this.init_ConfigStore.addHit(ModulesMenuController.Inner_CachePool.DROPDOWN, dropdown, module, option, "", -1);
               break;
            case STRING:
               this.init(context, control, module, option, "<empty>");
               this.init_ConfigStore.addHit(ModulesMenuController.Inner_CachePool.TEXT_FIELD, control, module, option, "", -1);
               break;
            case COLOR:
               this.write(context, module, option, control);
               break;
            case STRING_LIST:
            case ITEM_LIST:
            case BLOCK_LIST:
            case ENTITY_TYPE_LIST:
            case SOUND_EVENT_LIST:
            case STORAGE_LIST: {
               int editW = UiDensityMetrics.buttonWidthForText(context.init_PoolEntry().measureWidth("Edit"));
               NetHelper edit = NetHelper.init(control.init() - editW, control.write(), editW, control.sync());
               NetHelper value = NetHelper.init(control.read(), control.write(), Math.max(1, control.update() - editW - 4), control.sync());
               this.update(context, module, option, value);
               autismclient.gui.f.b.CachePool.init(
                  context, edit, "Edit", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, edit.init_Z(context.read(), context.write()), false
               );
               this.init_ConfigStore.addHit(ModulesMenuController.Inner_CachePool.LIST_EDITOR, edit, module, option, "", -1);
               break;
            }
            case PACKET_LIST: {
               int pickW = UiDensityMetrics.buttonWidthForText(context.init_PoolEntry().measureWidth("Pick"));
               NetHelper edit = NetHelper.init(control.init() - pickW, control.write(), pickW, control.sync());
               NetHelper value = NetHelper.init(control.read(), control.write(), Math.max(1, control.update() - pickW - 4), control.sync());
               this.update(context, module, option, value);
               autismclient.gui.f.b.CachePool.init(
                  context, edit, "Pick", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, edit.init_Z(context.read(), context.write()), false
               );
               this.init_ConfigStore.addHit(ModulesMenuController.Inner_CachePool.PACKET_EDITOR, edit, module, option, "", -1);
               break;
            }
            case KEYBIND:
               Font font = this.init_ConfigStore.getHost().font();
               String bindName = ValueSink.init_Str(ModulesMenuController.parseInt(module.value(option.id()), -1));
               int bindW = UiDensityMetrics.buttonWidthForText(k.init(font, bindName, autismclient.gui.f.a.DataUtil.init, 0));
               int clearW = UiDensityMetrics.buttonWidthForText(k.init(font, "Clear", autismclient.gui.f.a.DataUtil.init, 0));
               NetHelper clear = NetHelper.init(control.init() - clearW, control.write(), clearW, control.sync());
               int bindX = Math.max(control.read(), clear.read() - 2 - bindW);
               NetHelper bind = NetHelper.init(bindX, control.write(), Math.max(1, clear.read() - 2 - bindX), control.sync());
               autismclient.gui.f.b.CachePool.init(
                  context, bind, bindName, autismclient.gui.f.b.CachePool.Inner_DataUtil.init, bind.init_Z(context.read(), context.write()), false
               );
               autismclient.gui.f.b.CachePool.init(
                  context, clear, "Clear", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, clear.init_Z(context.read(), context.write()), false
               );
               this.init_ConfigStore.addHit(ModulesMenuController.Inner_CachePool.BIND_OPTION, bind, module, option, "", -1);
               this.init_ConfigStore.addHit(ModulesMenuController.Inner_CachePool.RESET_OPTION_BIND, clear, module, option, "", -1);
               break;
            case ACTION:
               String buttonLabel = option instanceof ActionSetting action ? action.buttonLabel() : "Run";
               autismclient.gui.f.b.CachePool.init(
                  context, control, buttonLabel, autismclient.gui.f.b.CachePool.Inner_DataUtil.read, control.init_Z(context.read(), context.write()), false
               );
               this.init_ConfigStore.addHit(ModulesMenuController.Inner_CachePool.OPTION, control, module, option, "", -1);
         }
      }
   }

   private int init_I(Module module, Setting<?, ?> option, int maxWidth) {
      Font font = this.init_ConfigStore.getHost().font();
      int widest = k.init(font, module.displayValue(option), autismclient.gui.f.a.DataUtil.init, 0);

      for (String choice : option.choices()) {
         widest = Math.max(widest, k.init(font, option.format(choice), autismclient.gui.f.a.DataUtil.init, 0));
      }

      return Math.min(maxWidth, Math.max(56, widest + 26));
   }

   private void load(SyncManager context, Module module, Setting<?, ?> option, NetHelper control) {
      if (option.displayMode() == DisplayMode.NUMERIC_TEXT_FIELD) {
         this.init(context, control, module, option, "");
         this.init_ConfigStore.addHit(ModulesMenuController.Inner_CachePool.TEXT_FIELD, control, module, option, "", -1);
      } else {
         double current = option.kind() == Kind.INTEGER
            ? ModulesMenuController.parseInt(module.value(option.id()), ModulesMenuController.parseInt(option.defaultValue(), 0))
            : ModulesMenuController.parseDouble(module.value(option.id()), ModulesMenuController.parseDouble(option.defaultValue(), 0.0));
         String unit = option.unit(current);
         int unitWidth = unit.isEmpty() ? 0 : context.init_PoolEntry().measureWidth(unit);
         int reservedUnitWidth = Math.max(
            unitWidth, Math.max(context.init_PoolEntry().measureWidth(option.unit(option.sliderMin())), context.init_PoolEntry().measureWidth(option.unit(option.sliderMax())))
         );
         int unitSpace = reservedUnitWidth == 0 ? 0 : reservedUnitWidth + 3;
         int valueW = ModulesMenuController.scaled(34);
         int tail = 4 + valueW + unitSpace;
         NetHelper slider = NetHelper.init(control.read(), control.write(), Math.max(40, control.update() - tail), control.sync());
         NetHelper value = NetHelper.init(control.init() - unitSpace - valueW, control.write(), valueW, control.sync());
         double ratio = (current - option.sliderMin()) / Math.max(1.0E-4, option.sliderMax() - option.sliderMin());
         SliderRenderer.init(context, slider, ratio, slider.init_Z(context.read(), context.write()));
         this.init(context, value, module, option, "");
         if (!unit.isEmpty()) {
            context.init_PoolEntry()
               .drawTrimmed(
                  context.init_GuiGraphicsExtract(),
                  unit,
                  value.init() + 3,
                  context.init_PoolEntry().centerTextY(value),
                  unitWidth,
                  context.init_TableIndex().colors().textPrimary
               );
         }

         this.init_ConfigStore.addHit(ModulesMenuController.Inner_CachePool.SLIDER, slider, module, option, "", -1);
         this.init_ConfigStore.addHit(ModulesMenuController.Inner_CachePool.TEXT_FIELD, value, module, option, "", -1);
      }
   }

   private void init(SyncManager context, NetHelper bounds, Module module, Setting<?, ?> option, String placeholder) {
      boolean focused = this.init_ConfigStore.isEditing(module, option);
      String shown = focused ? this.init_ConfigStore.textEdit.text : module.value(option.id());
      if (focused) {
         this.init_ConfigStore.textEditBounds = bounds;
      }

      TextFieldRenderer.init(
         context,
         bounds,
         shown,
         placeholder,
         focused,
         focused ? this.init_ConfigStore.textEdit.cursor : shown.length(),
         focused ? this.init_ConfigStore.textEdit.selectionStart() : -1,
         focused ? this.init_ConfigStore.textEdit.selectionEnd() : -1,
         focused
            ? this.init_ConfigStore.textEdit.init(Math.max(1, bounds.update() - 8), fragment -> context.init_PoolEntry().measureFragmentWidth(shown, fragment))
            : 0
      );
   }

   private void read(SyncManager context, Module module, Setting<?, ?> option, NetHelper control) {
      String unit = option.unit() == null ? "" : option.unit();
      ValueRange range = this.init_ValueRange_Module_Setting(module, option);
      String label = range.format(option.step()) + (unit.isEmpty() ? "" : " " + unit);
      int tailW = ModulesMenuController.scaled(62);
      NetHelper slider = NetHelper.init(control.read(), control.write(), Math.max(40, control.update() - tailW), control.sync());
      NetHelper value = NetHelper.init(slider.init() + 4, control.write(), tailW - 4, control.sync());
      double lo = option.sliderMin();
      double hi = option.sliderMax();
      RangeSliderRenderer.init(
         context, slider, RangeSliderRenderer.init(range.min(), lo, hi), RangeSliderRenderer.init(range.max(), lo, hi), slider.init_Z(context.read(), context.write()), -1
      );
      context.init_PoolEntry()
         .drawTrimmed(
            context.init_GuiGraphicsExtract(),
            label,
            value.read(),
            context.init_PoolEntry().centerTextY(value),
            value.update(),
            context.init_TableIndex().colors().textPrimary
         );
      this.init_ConfigStore.addHit(ModulesMenuController.Inner_CachePool.RANGE_SLIDER, slider, module, option, "", -1);
   }

   private void write(SyncManager context, Module module, Setting<?, ?> option, NetHelper control) {
      UiTheme theme = context.init_TableIndex();
      int pickW = UiDensityMetrics.buttonWidthForText(context.init_PoolEntry().measureWidth("Pick"));
      NetHelper pick = NetHelper.init(control.init() - pickW, control.write(), pickW, control.sync());
      NetHelper value = NetHelper.init(control.read(), control.write(), Math.max(1, control.update() - pickW - 4), control.sync());
      int color = ModulesMenuController.parseColor(module.value(option.id()), ModulesMenuController.parseColor(option.defaultValue(), -1));
      PacketUtil.load(context.init_GuiGraphicsExtract(), value, 4, theme.colors().inputSurface, theme.colors().borderSubtle);
      NetHelper swatch = NetHelper.init(value.read() + 3, value.write() + 2, 14, Math.max(1, value.sync() - 4));
      PacketUtil.load(context.init_GuiGraphicsExtract(), swatch, 2, color | 0xFF000000, theme.colors().borderSubtle);
      String rgb = String.format(Locale.ROOT, "#%06X", color & 16777215);
      context.init_PoolEntry()
         .drawFitted(
            context.init_GuiGraphicsExtract(),
            rgb,
            swatch.init() + 5,
            context.init_PoolEntry().centerTextY(value),
            Math.max(1, value.init() - swatch.init() - 8),
            theme.colors().textPrimary
         );
      autismclient.gui.f.b.CachePool.init(
         context, pick, "Pick", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, pick.init_Z(context.read(), context.write()), false
      );
      this.init_ConfigStore.addHit(ModulesMenuController.Inner_CachePool.COLOR_PICKER, pick, module, option, "", -1);
      this.init_ConfigStore.addHit(ModulesMenuController.Inner_CachePool.COLOR_PICKER, value, module, option, "", -1);
   }

   private void init(
      SyncManager context,
      Module module,
      Setting<?, ?> option,
      NetHelper control,
      String buttonText,
      autismclient.gui.f.b.CachePool.Inner_DataUtil tone,
      ModulesMenuController.Inner_CachePool hitType
   ) {
      int buttonW = UiDensityMetrics.buttonWidthForText(context.init_PoolEntry().measureWidth(buttonText));
      NetHelper button = NetHelper.init(control.init() - buttonW, control.write(), buttonW, control.sync());
      NetHelper value = NetHelper.init(control.read(), control.write(), Math.max(1, control.update() - buttonW - 4), control.sync());
      this.update(context, module, option, value);
      autismclient.gui.f.b.CachePool.init(context, button, buttonText, tone, button.init_Z(context.read(), context.write()), false);
      Setting<?, ?> linked = option.linkedActionId().isBlank() ? option : module.setting(option.linkedActionId());
      this.init_ConfigStore.addHit(hitType, button, module, linked == null ? option : linked, "", -1);
   }

   private void update(SyncManager context, Module module, Setting<?, ?> option, NetHelper bounds) {
      UiTheme theme = context.init_TableIndex();
      String summary = this.init_Str_Module_Setting(module, option);
      boolean empty = summary == null || summary.isBlank() || "Empty".equals(summary) || "No file selected".equals(summary);
      String shown = empty ? (summary != null && !summary.isBlank() ? summary : "Empty") : summary;
      int availableWidth = Math.max(1, bounds.update() - 4);
      if (option.kind() == Kind.ENTITY_TYPE_LIST) {
         ModuleSettingsView.Inner_DataUtil entities = this.init_BufferHelper_DataU_Str(module.value(option.id()));
         if (entities.init_I() > 1 && context.init_PoolEntry().measureWidth(shown) > availableWidth) {
            shown = entities.init_I() + " entries";
         }
      }

      int textX = bounds.read() + 2;
      if (option.kind() == Kind.ENTITY_TYPE_LIST) {
         textX += Math.max(0, availableWidth - context.init_PoolEntry().measureWidth(shown));
      }

      context.init_PoolEntry()
         .drawFitted(
            context.init_GuiGraphicsExtract(),
            shown,
            textX,
            context.init_PoolEntry().centerTextY(bounds),
            availableWidth,
            empty ? theme.colors().textSecondary : theme.colors().textPrimary
         );
      if (bounds.init_Z(context.read(), context.write()) && summary != null && !summary.isBlank()) {
         this.init_ConfigStore.write(summary);
      }
   }

   private String init_Str_Module_Setting(Module module, Setting<?, ?> option) {
      if (module != null && option != null) {
         String value = module.value(option.id());
         if (option.displayMode() != DisplayMode.MACRO_PICKER && option.displayMode() != DisplayMode.CONDITIONAL_MACRO_PICKER) {
            String var10000;
            switch (option.kind()) {
               case STRING_LIST:
               case ITEM_LIST:
               case BLOCK_LIST:
               case SOUND_EVENT_LIST:
               case STORAGE_LIST:
                  var10000 = this.init_Str_Module(module, option, value, () -> load_Str_Str(value));
                  break;
               case ENTITY_TYPE_LIST:
                  var10000 = this.init_BufferHelper_DataU_Str(value).init_Str();
                  break;
               case PACKET_LIST:
                  var10000 = this.init_Str_Module(module, option, value, () -> init_Str_Setting_Str(option, value));
                  break;
               default:
                  var10000 = option.format(value);
            }

            return var10000;
         } else {
            String macroName = value == null ? "" : value.trim();
            if (macroName.isBlank()) {
               return "No macro";
            } else {
               return MacroLibraryManager.getInstance().findByName(macroName) == null ? "Missing: " + macroName : macroName;
            }
         }
      } else {
         return "";
      }
   }

   private ModuleSettingsView.Inner_DataUtil init_BufferHelper_DataU_Str(String value) {
      if (this.load_Map.size() > 512) {
         this.load_Map.clear();
      }

      return this.load_Map
         .computeIfAbsent(
            value,
            raw -> {
               List<String> names = new ArrayList<>();

               for (String entry : PipeDelimitedListUtil.init(raw)) {
                  Identifier id = Identifier.tryParse(entry);
                  String name = id == null
                     ? entry
                     : BuiltInRegistries.ENTITY_TYPE.getOptional(id).map(type -> type.getDescription().getString()).orElseGet(() -> init_Str_Str(entry));
                  names.add(name);
               }

               return new ModuleSettingsView.Inner_DataUtil(names.isEmpty() ? "Empty" : String.join(", ", names), names.size());
            }
         );
   }

   private static String init_Str_Str(String entry) {
      String path = entry.startsWith("minecraft:") ? entry.substring("minecraft:".length()) : entry;
      if (path.indexOf(58) >= 0) {
         return path;
      }

      StringBuilder name = new StringBuilder();

      for (String word : path.split("_+")) {
         if (!word.isEmpty()) {
            if (!name.isEmpty()) {
               name.append(' ');
            }

            name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
         }
      }

      return name.toString();
   }

   private String init_Str_Module(Module module, Setting<?, ?> option, String value, Supplier<String> compute) {
      if (this.init_Map.size() > 512) {
         this.init_Map.clear();
      }

      return this.init_Map.computeIfAbsent(module.id() + " " + option.id() + " " + value, ignored -> compute.get());
   }

   private static String init_Str_Setting_Str(Setting<?, ?> option, String value) {
      boolean c2s = PacketFilterListCodec.init_Z(option == null ? "" : option.id());
      Set<Class<? extends Packet<?>>> packets = PacketFilterListCodec.init_Set(value, c2s);
      List<String> invalid = PacketFilterListCodec.init_List(value, c2s);
      int count = packets.size() + invalid.size();
      if (count <= 0) {
         return "Empty";
      } else if (count > 1) {
         return count + " packets";
      } else {
         return !packets.isEmpty() ? PacketNameResolver.init_Str_Class(packets.iterator().next()) : invalid.get(0);
      }
   }

   private static String load_Str_Str(String value) {
      if (value != null && !value.isBlank()) {
         String[] parts = value.split("\\|");
         int count = 0;
         String first = "";

         for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
               if (count == 0) {
                  first = trimmed;
               }

               count++;
            }
         }

         if (count == 0) {
            return "Empty";
         } else {
            return count == 1 ? first : count + " entries";
         }
      } else {
         return "Empty";
      }
   }

   private static boolean load_Z_Setting(Setting<?, ?> option) {
      boolean var10000;
      switch (option.displayMode()) {
         case RANGE_SLIDER:
            var10000 = true;
            break;
         case DEFAULT:
            var10000 = option.kind() == Kind.INTEGER || option.kind() == Kind.DOUBLE || option.kind() == Kind.STRING;
            break;
         default:
            var10000 = false;
      }

      return var10000;
   }

   private ValueRange init_ValueRange_Module_Setting(Module module, Setting<?, ?> option) {
      return ValueRange.parse(module.value(option.id()), ValueRange.parse(option.defaultValue(), new ValueRange(option.min(), option.max())));
   }

   int init_I_ConfigStore_Stream_I(ModulesMenuController.Inner_StreamUtil hit, int mx) {
      Setting<?, ?> option = hit.setting();
      Module module = hit.module();
      ValueRange range = this.init_ValueRange_Module_Setting(module, option);
      return RangeSliderRenderer.pickHandle(
         mx,
         hit.bounds(),
         RangeSliderRenderer.init(range.min(), option.sliderMin(), option.sliderMax()),
         RangeSliderRenderer.init(range.max(), option.sliderMin(), option.sliderMax())
      );
   }

   void init_V_ConfigStore_Stream_I(ModulesMenuController.Inner_StreamUtil hit, int mx) {
      Module module = hit.module();
      Setting<?, ?> option = hit.setting();
      if (module != null && option != null) {
         double ratio = (double)(mx - hit.bounds().read()) / Math.max(1, hit.bounds().update());
         ratio = Math.max(0.0, Math.min(1.0, ratio));
         if (hit.type() == ModulesMenuController.Inner_CachePool.RANGE_SLIDER) {
            this.init_V_ConfigStore_Stream_D(hit, ratio);
         } else {
            double value = option.sliderMin() + ratio * (option.sliderMax() - option.sliderMin());
            double step = Math.max(1.0E-4, option.step());
            value = option.sliderMin() + Math.round((value - option.sliderMin()) / step) * step;
            if (option.kind() == Kind.INTEGER) {
               this.init_ConfigStore.setValueTransient(module, option, Integer.toString((int)Math.round(value)));
            } else {
               this.init_ConfigStore.setValueTransient(module, option, String.format(Locale.ROOT, "%.2f", value));
            }
         }
      }
   }

   private void init_V_ConfigStore_Stream_D(ModulesMenuController.Inner_StreamUtil hit, double ratio) {
      Module module = hit.module();
      Setting<?, ?> option = hit.setting();
      ValueRange current = this.init_ValueRange_Module_Setting(module, option);
      double value = option.sliderMin() + ratio * (option.sliderMax() - option.sliderMin());
      double step = Math.max(1.0E-4, option.step());
      value = option.sliderMin() + Math.round((value - option.sliderMin()) / step) * step;
      int thumb = this.init_ConfigStore.init_I();
      double lo = thumb == 1 ? current.min() : value;
      double hi = thumb == 1 ? value : current.max();
      ValueRange next = new ValueRange(Math.min(lo, hi), Math.max(lo, hi));
      this.init_ConfigStore.setValueTransient(module, option, next.toString());
   }

   void clickSetting(Module module, Setting<?, ?> option, int button) {
      if (option != null) {
         if (option.kind() == Kind.BOOLEAN && button == 0) {
            this.init_ConfigStore.setValue(module, option, Boolean.toString(!Boolean.parseBoolean(module.value(option.id()))));
         } else if (option.kind() == Kind.ACTION && button == 0 && option.action() != null) {
            option.action().run();
         }
      }
   }

   private int init_I_Str(String key, NetHelper viewport, int contentHeight, float frameSeconds) {
      int max = Math.max(0, contentHeight - viewport.sync());
      this.init_ConfigStore.recordScrollMetrics(key, viewport.sync(), max);
      float target = ModulesMenuController.clampFloat(this.init_ConfigStore.scrollTarget(key), 0.0F, max);
      float render = this.init_ConfigStore.smoothScroll(key, target, frameSeconds);
      return Math.round(render);
   }

   private void init_V_SyncManager(SyncManager context, NetHelper viewport, int contentHeight, int scroll) {
      this.init_ConfigStore.drawScrollbar("settings", context, viewport, contentHeight, scroll);
   }

   private record Inner_DataUtil(String init_Str, int init_I) {
   }

   private record Inner_StreamUtil(String init_Str, Setting<?, ?> init_Setting) {
   }
}
