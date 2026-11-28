package autismclient.gui.ui.menu;

import autismclient.api.module.Setting;
import autismclient.gui.ui.UiColorScheme;
import autismclient.gui.f.NetHelper;
import autismclient.gui.f.PacketUtil;
import autismclient.gui.ui.UiDensityMetrics;
import autismclient.gui.ui.EaseOutAnimator;
import autismclient.gui.f.SyncManager;
import autismclient.gui.ui.UiTheme;
import autismclient.gui.f.UiHelper;
import autismclient.gui.ui.widgets.WindowHeaderHelper;
import autismclient.gui.ui.widgets.KeybindButtonRenderer;
import autismclient.gui.ui.widgets.WindowFrameRenderer;
import autismclient.gui.ui.widgets.ConnectedButtonRenderer;
import autismclient.gui.ui.widgets.DropdownMenu;
import autismclient.gui.ui.widgets.ScrollbarRenderer;
import autismclient.gui.ui.widgets.TextFieldRenderer;
import autismclient.gui.ui.widgets.ToggleSwitchRenderer;
import autismclient.gui.ui.widgets.TooltipRenderer;
import autismclient.gui.ui.widgets.TitleBarRenderer;
import autismclient.gui.f.b.k;
import autismclient.modules.Module;
import autismclient.modules.ae;
import autismclient.modules.al;
import autismclient.modules.CrosshairModule;
import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismConfig;
import autismclient.util.AutismLiteVariant;
import autismclient.util.ValueSink;
import autismclient.util.AutismMacro;
import autismclient.util.OverlayManager;
import autismclient.util.AutismSessionState;
import autismclient.util.GuiIconAtlas;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.ToIntFunction;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;

public final class ModulesMenuController {
   private static final int init_I = 290;
   private static final int load_I = 344;
   private static final int read_I = 4;
   private static final int write_I = 3;
   private static final int update_I = 5;
   private static final int sync_I = 12;
   private static final int flush_I = 5;
   private static final String GENERAL_KEY = "\u0000general";
   private static final String PROFILES_KEY = "\u0000profiles";
   private static final String MAIN_MENU_ID = "main-menu";
   private static final String SETTINGS_WINDOW_ID = "settings-window";
   private static final String SEARCH_ID = "search";
   private static final int close_I = 150;
   private static final int open_I = 360;
   private static final int poll_I = 14;
   private static final int push_I = 2;
   private static final long init_J = 350L;
   private static final int pop_I = -536870912;
   private static final List<String> init_List = new ArrayList<>();
   private static final ModulesMenuController.Inner_ConfigStore MACROS_TOOL = new ModulesMenuController.Inner_ConfigStore(
      "macros", "Macros", autismclient.gui.f.a.DataUtil.sync
   );
   private static final ModulesMenuController.Inner_ConfigStore ADMIN_TOOL = new ModulesMenuController.Inner_ConfigStore(
      "admin", "Admin Tools", autismclient.gui.f.a.DataUtil.flush
   );
   private static final ModulesMenuController.Inner_ConfigStore AI_TOOL = new ModulesMenuController.Inner_ConfigStore(
      "ai", "AI Assistant", autismclient.gui.f.a.DataUtil.get
   );
   private static final ModulesMenuController.Inner_ConfigStore SCHEMATICS_TOOL = new ModulesMenuController.Inner_ConfigStore(
      "schematics", "Schematics", autismclient.gui.f.a.DataUtil.clear
   );
   private static final List<ModulesMenuController.Inner_ConfigStore> UTILITY_TOOLS = List.of(
      new ModulesMenuController.Inner_ConfigStore("lan", "LAN Sync", autismclient.gui.f.a.DataUtil.update),
      new ModulesMenuController.Inner_ConfigStore("queue", "Queue", autismclient.gui.f.a.DataUtil.poll),
      new ModulesMenuController.Inner_ConfigStore("logger", "Logger", autismclient.gui.f.a.DataUtil.open),
      new ModulesMenuController.Inner_ConfigStore("packets", "Packets", autismclient.gui.f.a.DataUtil.close),
      new ModulesMenuController.Inner_ConfigStore("server", "Server Info", autismclient.gui.f.a.DataUtil.push),
      new ModulesMenuController.Inner_ConfigStore("keys", "Settings", autismclient.gui.f.a.DataUtil.write)
   );
   private static final ModulesMenuController.Inner_DataUtil[] ACTION_BUTTONS = new ModulesMenuController.Inner_DataUtil[]{
      new ModulesMenuController.Inner_DataUtil("flush", "Flush", autismclient.gui.f.b.CachePool.Inner_DataUtil.init),
      new ModulesMenuController.Inner_DataUtil("clear", "Clear", autismclient.gui.f.b.CachePool.Inner_DataUtil.init)
   };
   private static final Map<String, Float> scrollTargets = new HashMap<>();
   private static final Map<String, int[]> load_Map = new HashMap<>();
   private static final Map<String, Boolean> read_Map = new HashMap<>();
   private static String selectedCategoryKey = null;
   private static int savedPanelX = -1;
   private static int savedPanelY = -1;
   private final ModulesMenuController.Inner_BufferHelper host;
   private final ModuleSettingsView settingsView;
   private final List<ModulesMenuController.Inner_StreamUtil> hits = new ArrayList<>();
   private final Deque<NetHelper> init_Deque = new ArrayDeque<>();
   private final Map<String, Float> write_Map = new HashMap<>();
   private int panelX;
   private int panelY;
   private boolean positionInitialized;
   private final EaseOutAnimator panelHeightAnimation = new EaseOutAnimator(0.0F, 180);
   private int headerHeight = 17;
   private int railPixelWidth = 90;
   private int railItemHeight = 14;
   private int rowHeight = 15;
   private int settingsRowHeight = 13;
   private int screenWidth;
   private int screenHeight;
   private boolean headerPressed;
   private int pressX;
   private int pressY;
   private boolean headerDragged;
   private boolean panelCollapsed;
   private boolean update_Z;
   private final Map<String, EaseOutAnimator> collapseAnimations = new HashMap<>();
   private final EaseOutAnimator load_RingBuffer = new EaseOutAnimator(0.0F, 180);
   private boolean sync_Z;
   private int dragOffsetX;
   private int dragOffsetY;
   private String close_Str;
   private String open_Str;
   private int unusedResizePressX;
   private int resizePressY;
   private int resizeStartRows;
   private String lastClickedId;
   private long lastClickTime;
   private final Map<String, NetHelper> sync_Map = new HashMap<>();
   private int mainMenuWidth = -1;
   private int classicGridWidth;
   private int classicMaxBodyHeight;
   private long openedAtNanos;
   private long lastFrameNanos;
   private Module settingsModule;
   private autismclient.gui.profiles.ProfilesLibraryView profilesView;
   private NetHelper contentArea;
   private long update_J;
   private boolean flush_Z;
   private InlineTextEditState searchEdit;
   private String searchQuery = "";
   private NetHelper searchBounds = NetHelper.init(0, 0, 1, 1);
   private Module bindingModule;
   private Module bindingOptionModule;
   private Setting<?, ?> bindingOption;
   private long lastBindNanos;
   InlineTextEditState textEdit;
   NetHelper textEditBounds;
   private boolean close_Z;
   private boolean open_Z;
   private DropdownMenu dropdown;
   private Module dropdownModule;
   private Setting<?, ?> dropdownSetting;
   private autismclient.gui.ui.widgets.ColorPickerPopup colorPicker;
   private MacroPickerPopup macroPicker;
   private Module sliderModule;
   private Setting<?, ?> sliderSetting;
   private int activeRangeHandle = -1;
   private String pop_Str;
   private int scrollbarGrabOffset;
   String tooltipText;
   private final Map<String, ScrollbarRenderer.ScrollbarBounds> scrollbarBounds = new HashMap<>();
   private Map<String, Integer> savedVisibleRows;
   private boolean savedLayoutCustom;

   public ModulesMenuController(ModulesMenuController.Inner_BufferHelper host) {
      this.host = host;
      this.settingsView = new ModuleSettingsView(this);
      this.openedAtNanos = System.nanoTime();
      this.lastFrameNanos = this.openedAtNanos;
   }

   public void restoreState() {
      AutismConfig config = AutismConfig.getGlobal();
      if (savedPanelX < 0 && config.menuV2X >= 0) {
         savedPanelX = config.menuV2X;
         savedPanelY = config.menuV2Y;
      }

      if (selectedCategoryKey == null) {
         selectedCategoryKey = "\u0000general";
      }
   }

   public boolean isCapturingInput() {
      return this.textEdit != null
         || this.searchEdit != null
         || this.bindingModule != null
         || this.bindingOption != null
         || this.hasPopup()
         || this.sync_Z() && this.profilesView.hasFocusedInput();
   }

   public boolean hasPopup() {
      return this.dropdown != null || this.colorPicker != null || this.macroPicker != null || this.flush_Z();
   }

   public boolean isModuleOpen() {
      return this.settingsModule != null;
   }

   public void onClose() {
      this.endTextEdit(true);
      if (this.searchEdit != null) {
         this.commitSearchEdit();
      }

      if (this.sliderModule != null) {
         this.sliderModule.persistConfiguredState();
      }

      this.sliderModule = null;
      this.sliderSetting = null;
      this.headerPressed = false;
      this.close_Str = null;
      this.open_Str = null;
      this.pop_Str = null;
      this.update_Z = true;
      this.closeDropdown();
      this.closeColorPicker();
      this.closeMacroPicker();
   }

   public void read_V() {
      this.update_Z = false;
   }

   public void openModule(String moduleId) {
      Module module = moduleId == null ? null : al.init_Module_Str(moduleId);
      if (module != null) {
         this.showModuleSettings(module);
      }
   }

   public void openCategory(String categoryKeyOrLabel) {
      if ("profiles".equalsIgnoreCase(categoryKeyOrLabel)) {
         this.selectCategory("\u0000profiles");
      } else {
         if (categoryKeyOrLabel != null) {
            for (ae category : ae.init_List()) {
               if (category.init_Str().equalsIgnoreCase(categoryKeyOrLabel) || category.load().equalsIgnoreCase(categoryKeyOrLabel)) {
                  this.selectCategory(category.init_Str());
                  return;
               }
            }
         }

         this.selectCategory("\u0000general");
      }
   }

   public void setCollapsed(String id, boolean collapsed, boolean snap) {
      String key = id == null ? "single" : id;
      if (id == null) {
         this.panelCollapsed = collapsed;
      } else {
         this.categoryLayout(id).collapsed = collapsed;
      }

      if (snap) {
         this.collapseAnimations.computeIfAbsent(key, ignored -> new EaseOutAnimator(collapsed ? 0.0F : 1.0F, 180)).snapTo(collapsed ? 0.0F : 1.0F);
      }
   }

   public Map<String, NetHelper> init_Map() {
      Map<String, NetHelper> out = new LinkedHashMap<>();

      for (ModulesMenuController.Inner_StreamUtil hit : this.hits) {
         if (hit.type == ModulesMenuController.Inner_CachePool.WINDOW_HEADER) {
            out.putIfAbsent(hit.id == null ? "" : hit.id, hit.bounds);
         }
      }

      return out;
   }

   public boolean isCollapsed(String id) {
      return id != null && !id.isEmpty() ? this.categoryLayout(id).collapsed : this.panelCollapsed;
   }

   public void setSearchQuery(String query) {
      this.applySearchQuery(query);
   }

   public void write_V() {
      ModulesMenuController.Inner_StreamUtil hit = this.hits
         .stream()
         .filter(candidate -> candidate.type == ModulesMenuController.Inner_CachePool.RAIL_ITEM && "\u0000profiles".equals(candidate.id))
         .findFirst()
         .orElseThrow();
      int x = hit.bounds.read() + hit.bounds.update() / 2;
      int y = hit.bounds.write() + hit.bounds.sync() / 2;
      this.click(x, y, 0);
      this.mouseReleased(x, y, 0);
      if (!"\u0000profiles".equals(selectedCategoryKey)) {
         throw new IllegalStateException("Profiles rail item did not open");
      }
   }

   public void update_V() {
      NetHelper area = this.contentArea;
      if (area == null) {
         throw new IllegalStateException("Profiles Library was not rendered");
      }

      int x = area.read() + area.update() / 4;
      int y = area.write() + UiDensityMetrics.scaledHeight(13) + 12;
      this.click(x, y, 0);
      this.mouseReleased(x, y, 0);
   }

   public NetHelper dropdownBounds(String optionId) {
      ModulesMenuController.Inner_StreamUtil hit = this.hits
         .stream()
         .filter(
            candidate -> candidate.type == ModulesMenuController.Inner_CachePool.DROPDOWN
               && candidate.setting != null
               && candidate.setting.id().equals(optionId)
         )
         .findFirst()
         .orElseThrow();
      int x = hit.bounds.read() + hit.bounds.update() / 2;
      int y = hit.bounds.write() + hit.bounds.sync() / 2;
      this.click(x, y, 0);
      this.mouseReleased(x, y, 0);
      if (this.dropdown != null && this.dropdown.isOpen()) {
         return this.dropdown.getPopupBounds();
      } else {
         throw new IllegalStateException("Dropdown did not open");
      }
   }

   public void setSliderFraction(String optionId, double fraction) {
      ModulesMenuController.Inner_StreamUtil hit = this.hits
         .stream()
         .filter(
            candidate -> candidate.type == ModulesMenuController.Inner_CachePool.SLIDER
               && candidate.setting != null
               && candidate.setting.id().equals(optionId)
         )
         .findFirst()
         .orElseThrow();
      int x = hit.bounds.read() + (int)Math.round((hit.bounds.update() - 1) * Math.clamp(fraction, 0.0, 1.0));
      int y = hit.bounds.write() + hit.bounds.sync() / 2;
      this.click(x, y, 0);
      this.mouseReleased(x, y, 0);
   }

   public void openModuleGroup(String moduleId, String group) {
      this.openModule(moduleId);
      if (this.settingsModule != null) {
         for (Setting<?, ?> option : this.settingsModule.settings()) {
            if (option.group() != null) {
               read_Map.put(moduleId + "|" + option.group(), group != null && !group.equals(option.group()));
            }
         }

         this.setScroll("settings", 0.0F);
      }
   }

   public void sync_V() {
      String previousQuery = this.searchQuery;
      AutismConfig.ModuleCategoryLayout layout = this.categoryLayout("main-menu");
      boolean previousCollapse = layout.collapsed;
      boolean custom = AutismConfig.getGlobal().classicMenuLayoutCustom;
      int previousX = layout.f_x_I;
      int previousY = layout.f_y_I;

      try {
         this.searchEdit = new InlineTextEditState("search", "selection", value -> {});
         int start = this.searchBounds.read() + 4;
         int y = this.searchBounds.write() + this.searchBounds.sync() / 2;
         int end = start + k.init(this.host.font(), "selec", autismclient.gui.f.a.DataUtil.init, 0);
         this.click(start, y, 0);
         this.mouseDragged(end, y, 0, end - start, 0.0);
         this.mouseReleased(end, y, 0);
         if (this.searchEdit == null || this.searchEdit.selectionStart() != 0 || this.searchEdit.selectionEnd() != 5) {
            throw new IllegalStateException("Search drag selection did not select the expected text");
         }

         this.mouseClicked(start, y, 0, 1);
         this.mouseReleased(start, y, 0);
         if (this.searchEdit.hasSelection()) {
            throw new IllegalStateException("Shift-click did not preserve the anchor");
         }

         this.searchEdit = null;
         ModulesMenuController.Inner_StreamUtil header = this.hits
            .stream()
            .filter(hit -> hit.type == ModulesMenuController.Inner_CachePool.WINDOW_HEADER && "main-menu".equals(hit.id))
            .findFirst()
            .orElseThrow();
         int hx = header.bounds.read() + 32;
         int hy = header.bounds.write() + header.bounds.sync() / 2;
         this.click(hx, hy, 0);
         this.mouseDragged(hx + 1, hy, 0, 1.0, 0.0);
         this.mouseReleased(hx + 1, hy, 0);
         if (layout.collapsed == previousCollapse) {
            throw new IllegalStateException("Header click with normal pointer jitter did not collapse");
         }

         layout.collapsed = previousCollapse;
         this.click(hx, hy, 0);
         this.mouseDragged(hx + 12, hy, 0, 12.0, 0.0);
         this.mouseReleased(hx + 12, hy, 0);
         if (layout.collapsed != previousCollapse) {
            throw new IllegalStateException("Dragging a header changed collapse state");
         }
      } finally {
         this.searchEdit = null;
         this.open_Z = false;
         this.searchQuery = previousQuery;
         layout.collapsed = previousCollapse;
         layout.f_x_I = previousX;
         layout.f_y_I = previousY;
         AutismConfig.getGlobal().classicMenuLayoutCustom = custom;
         this.host.load();
      }
   }

   public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, int width, int height) {
      long now = System.nanoTime();
      float frameSeconds = Math.min(0.1F, (float)(now - this.lastFrameNanos) / 1.0E9F);
      this.lastFrameNanos = now;
      this.screenWidth = width;
      this.screenHeight = height;
      this.tooltipText = null;
      this.hits.clear();
      this.init_Deque.clear();
      SyncManager popupContext = UiHelper.init(graphics, this.host.font(), mouseX, mouseY).init_SyncManager();
      SyncManager context = this.hasPopup() ? popupContext.load_SyncManager() : popupContext;
      this.contentArea = null;
      UiTheme theme = context.init_TableIndex();
      this.headerHeight = UiDensityMetrics.scaledHeight(14) + 3;
      this.railItemHeight = UiDensityMetrics.scaledHeight(14);
      this.rowHeight = UiDensityMetrics.scaledHeight(14) + 1;
      this.settingsRowHeight = UiDensityMetrics.scaledHeight(13);
      this.settingsView.setClassic(this.isClassicStyle());
      float settingsProgress = this.settingsModule != null && !this.sync_Z ? 1.0F : 0.0F;
      if (this.sync_Z && settingsProgress <= 0.001F) {
         this.settingsModule = null;
         this.sync_Z = false;
      }

      if (this.isClassicStyle()) {
         this.close();
         this.init_V_SyncManager_2(context, frameSeconds, now);
         this.renderPopups(popupContext, mouseX, mouseY);
      } else {
         if (this.settingsModule == null && "\u0000profiles".equals(selectedCategoryKey) && !AutismLiteVariant.enabled() && this.profilesView == null) {
            this.profilesView = new autismclient.gui.profiles.ProfilesLibraryView(this.host.font());
         }

         this.railPixelWidth = Math.min(this.railWidth(context), Math.max(45, width / 3));
         int contentW = scaled(344 + (this.settingsModule instanceof CrosshairModule ? 140 : 0));
         int w = Math.min(this.railPixelWidth + 1 + contentW + 2, Math.max(160, width - 8));
         int contentWidth = w - this.railPixelWidth - 3;
         List<Module> modules = this.settingsModule == null && this.profilesView == null && !this.isGeneralHome() ? this.visibleModules() : List.of();
         int naturalHeight = this.settingsModule != null
            ? this.settingsView.contentHeight(this.settingsModule)
            : (
               this.profilesView != null
                  ? this.profilesView.init(contentWidth)
                  : (this.isGeneralHome() ? this.generalContentHeight(contentWidth) : this.gridContentHeight(modules.size(), gridColumns(contentWidth)))
            );
         int expandedH = Math.min(Math.min(290, Math.max(1, height - 8)), this.headerHeight + Math.max(this.railContentHeight(), naturalHeight) + 1);
         if (this.panelHeightAnimation.value() <= 0.0F) {
            this.panelHeightAnimation.snapTo(expandedH);
         }

         int easedHeight = Math.round(this.panelHeightAnimation.animateTo(expandedH));
         int h = this.headerHeight + this.animatedBodyHeight("single", Math.max(0, easedHeight - this.headerHeight), this.panelCollapsed);
         if (!this.positionInitialized) {
            this.panelX = savedPanelX >= 0 ? savedPanelX : (width - w) / 2;
            this.panelY = savedPanelY >= 0 ? savedPanelY : (height - Math.min(290, height - 8)) / 2;
            this.positionInitialized = true;
         }

         this.panelX = clampInt(this.panelX, 4, Math.max(4, width - w - 4));
         this.panelY = clampInt(this.panelY, 4, Math.max(4, height - h - 4));
         NetHelper window = NetHelper.init(this.panelX, this.panelY, w, h);
         PacketUtil.init_V_SyncManager(context, window, 8, theme.colors().windowSurface, theme.colors().borderSubtle);
         NetHelper header = WindowHeaderHelper.init(window, this.headerHeight);
         TitleBarRenderer.init(context, header, h <= this.headerHeight + 1, this.headerPressed);
         this.addHit(
            ModulesMenuController.Inner_CachePool.WINDOW_HEADER, NetHelper.init(window.read(), window.write(), window.update(), this.headerHeight), null, null, "", -1
         );
         this.load_V_SyncManager_NetHelper(context, window);
         if (h <= this.headerHeight) {
            this.renderPopups(popupContext, mouseX, mouseY);
         } else {
            NetHelper body = NetHelper.init(window.read() + 1, window.write() + this.headerHeight, window.update() - 2, window.sync() - this.headerHeight - 1);
            int targetBodyH = Math.max(0, expandedH - this.headerHeight - 1);
            NetHelper rail = NetHelper.init(body.read(), body.write(), this.railPixelWidth, targetBodyH);
            NetHelper content = NetHelper.init(rail.init() + 1, body.write(), body.update() - this.railPixelWidth - 1, targetBodyH);
            this.pushClip(context, body);

            try {
               this.renderSidebarLayout(context, popupContext, rail, content, body, modules, frameSeconds, delta);
            } finally {
               this.popClip(context);
            }

            this.renderPopups(popupContext, mouseX, mouseY);
         }
      }
   }

   private void renderSidebarLayout(
      SyncManager context, SyncManager popupContext, NetHelper rail, NetHelper content, NetHelper body, List<Module> modules, float frameSeconds, float delta
   ) {
      this.renderRail(context, rail);
      PacketUtil.load(context.init_GuiGraphicsExtract(), rail.init(), body.write(), body.sync(), context.init_TableIndex().colors().divider);
      if (this.settingsModule != null) {
         this.pushClip(context, content);

         try {
            this.settingsView.render(context, content, this.settingsModule, frameSeconds);
         } finally {
            this.popClip(context);
         }
      } else if (this.profilesView != null) {
         this.contentArea = NetHelper.init(content.read(), content.write(), content.update(), Math.min(content.sync(), body.sync()));
         SyncManager profileContext = this.flush_Z() ? popupContext : context;
         if (!this.contentArea.init_Z(profileContext.read(), profileContext.write())) {
            profileContext = profileContext.load_SyncManager();
         }

         this.profilesView.render(profileContext, content, delta);
      } else if (this.isGeneralHome()) {
         this.renderGeneralPage(context, content, frameSeconds);
      } else {
         this.renderModuleGrid(context, content, modules, frameSeconds);
      }
   }

   private void renderPopups(SyncManager context, int mouseX, int mouseY) {
      if (this.dropdown != null) {
         this.dropdown.render(context);
      }

      if (this.colorPicker != null) {
         this.colorPicker.render(context);
      }

      if (this.macroPicker != null) {
         this.macroPicker.init(context);
      }

      if (this.tooltipText != null && !this.tooltipText.isBlank() && !this.init_Z_I_I(mouseX, mouseY)) {
         TooltipRenderer.init(context, this.tooltipText, mouseX, mouseY);
      }
   }

   private boolean init_Z_I_I(int mouseX, int mouseY) {
      if (this.headerPressed || this.close_Str != null || this.open_Str != null) {
         return true;
      }

      if (OverlayManager.getInstance().isDraggingOrResizing()) {
         return true;
      }

      ModulesMenuController.Inner_StreamUtil hit = this.hitAt(mouseX, mouseY);
      return hit != null && hit.type == ModulesMenuController.Inner_CachePool.CLASSIC_RESIZE;
   }

   private void load_V_SyncManager_NetHelper(SyncManager context, NetHelper window) {
      UiColorScheme colors = context.init_TableIndex().colors();
      String title = this.settingsModule != null
         ? this.settingsModule.name()
         : (
            this.profilesView != null
               ? "Profiles Library"
               : (this.isGeneralHome() ? "Main Menu" : (!this.searchQuery.isBlank() ? "Search results" : (this.selectedCategory() != null ? this.selectedCategory().load() : "Modules")))
         );
      NetHelper bar = NetHelper.init(window.read(), window.write(), window.update(), this.headerHeight);
      int controlH = UiDensityMetrics.scaledHeight(13);
      int controlY = window.write() + Math.max(1, (this.headerHeight - controlH) / 2);
      NetHelper close = NetHelper.init(window.init() - 3 - controlH, controlY, controlH, controlH);
      int titleX = window.read() + this.railPixelWidth + 4 + 2;
      int titleEnd = close.read() - 4;
      if (this.profilesView == null || this.settingsModule != null) {
         int searchW = Math.min(scaled(112), Math.max(44, (close.read() - titleX) / 2));
         this.searchBounds = NetHelper.init(titleEnd - searchW, controlY, searchW, controlH);
         titleEnd = this.searchBounds.read() - 4;
         this.renderSearchBox(context, this.settingsModule == null ? "Search modules" : "Search settings");
      }

      int titleY = context.init_PoolEntry().centerTextY(bar);
      context.init_PoolEntry().drawFitted(context.init_GuiGraphicsExtract(), "AUTISM", window.read() + 4 + 3, titleY, this.railPixelWidth - 8, colors.textSecondary);
      context.init_PoolEntry().drawEllipsized(context.init_GuiGraphicsExtract(), title, titleX, titleY, titleEnd - titleX, colors.textPrimary);
      float closeT = autismclient.gui.ui.HoverTransitionCache.sample(autismclient.gui.ui.HoverTransitionCache.boundsKey(close), close.init_Z(context.read(), context.write()));
      PacketUtil.read(context.init_GuiGraphicsExtract(), close.init(2), lerpColor(colors.textSecondary, colors.danger, closeT));
      this.addHit(ModulesMenuController.Inner_CachePool.CLOSE, close, null, null, "", -1);
   }

   private void renderRail(SyncManager context, NetHelper rail) {
      int y = rail.write() + 3 - this.railScroll(rail, this.railContentHeight());
      int x = rail.read() + 3;
      int w = rail.update() - 6;
      this.pushClip(context, rail);

      try {
         for (ae category : ae.init_List()) {
            if (!menuModules(category).isEmpty()) {
               this.renderRailItem(context, NetHelper.init(x, y, w, this.railItemHeight), category.load(), category.init_Str());
               y += this.railItemHeight + 1;
            }
         }

         PacketUtil.init_V_GuiGraphicsExtract_2(context.init_GuiGraphicsExtract(), x + 2, y + 1, w - 4, context.init_TableIndex().colors().divider);
         y += 4;
         this.renderRailItem(context, NetHelper.init(x, y, w, this.railItemHeight), "Profiles", "\u0000profiles");
         y += this.railItemHeight + 1;
         this.renderRailItem(context, NetHelper.init(x, y, w, this.railItemHeight), "Main Menu", "\u0000general");
      } finally {
         this.popClip(context);
      }
   }

   private void renderRailItem(SyncManager context, NetHelper row, String label, String key) {
      boolean selected = key.equals(selectedCategoryKey) && (!"\u0000general".equals(key) || this.isGeneralHome());
      boolean hovered = row.init_Z(context.read(), context.write())
         && (this.init_Deque.peek() == null || this.init_Deque.peek().init_Z(context.read(), context.write()));
      float hoverT = autismclient.gui.ui.HoverTransitionCache.sample("rail:" + key, hovered);
      float selectT = autismclient.gui.ui.ToggleTransitionCache.init("railsel:" + key, selected);
      UiColorScheme colors = context.init_TableIndex().colors();
      if (hoverT > 0.001F || selectT > 0.001F) {
         int fill = lerpColor(PacketUtil.init(colors.cardSurfaceElevated, hoverT), colors.accentSoft, selectT);
         if (selectT > 0.001F) {
            PacketUtil.load(context.init_GuiGraphicsExtract(), row, 3, fill, PacketUtil.init(colors.accent, selectT * 0.65F));
         } else {
            PacketUtil.load(context.init_GuiGraphicsExtract(), row, 3, fill);
         }
      }

      int textColor = !selected && !hovered ? colors.textSecondary : colors.textPrimary;
      int iconSize = Math.min(12, row.sync() - 2);
      int iconX = row.read() + 5;
      int iconY = row.write() + (row.sync() - iconSize) / 2;
      GuiIconAtlas.draw(context.init_GuiGraphicsExtract(), categoryIcon(key), iconX, iconY, iconSize, textColor);
      int labelX = iconX + iconSize + 5;
      context.init_PoolEntry().drawEllipsized(context.init_GuiGraphicsExtract(), label, labelX, context.init_PoolEntry().centerTextY(row), row.init() - 5 - labelX, textColor);
      this.addHit(ModulesMenuController.Inner_CachePool.RAIL_ITEM, row, null, null, key, -1);
   }

   private static Identifier categoryIcon(String key) {
      Identifier var10000;
      switch (key) {
         case "\u0000general":
            var10000 = autismclient.gui.f.a.DataUtil.pop;
            break;
         case "\u0000profiles":
            var10000 = autismclient.gui.f.a.DataUtil.add;
            break;
         case "COMBAT":
            var10000 = autismclient.gui.f.a.DataUtil.reset;
            break;
         case "MOVEMENT":
            var10000 = autismclient.gui.f.a.DataUtil.start;
            break;
         case "PLAYER":
            var10000 = autismclient.gui.f.a.DataUtil.stop;
            break;
         case "MISC":
            var10000 = autismclient.gui.f.a.DataUtil.next;
            break;
         case "RENDER":
            var10000 = autismclient.gui.f.a.DataUtil.prev;
            break;
         default:
            var10000 = autismclient.gui.f.a.DataUtil.valid;
      }

      return var10000;
   }

   private List<Module> visibleModules() {
      String query = this.searchQuery.strip();
      if (query.isEmpty()) {
         ae category = this.selectedCategory();
         return category == null ? List.of() : menuModules(category);
      }

      List<Module> matches = new ArrayList<>();

      for (ae category : ae.init_List()) {
         for (Module module : menuModules(category)) {
            if (this.matchesQuery(query, module)) {
               matches.add(module);
            }
         }
      }

      return matches;
   }

   private void renderModuleGrid(SyncManager context, NetHelper content, List<Module> modules, float frameSeconds) {
      int columns = gridColumns(content.update());
      int contentHeight = this.gridContentHeight(modules.size(), columns);
      int scroll = this.updateScroll("modules", content, contentHeight, frameSeconds);
      int width = content.update() - 8 - (contentHeight > content.sync() ? 6 : 0);
      int cardW = (width - (columns - 1) * 4) / columns;
      int pitch = this.cardPitch();
      int cardH = pitch - 4;
      this.pushClip(context, content);

      try {
         for (int i = 0; i < modules.size(); i++) {
            int x = content.read() + 4 + i % columns * (cardW + 4);
            int y = content.write() + 4 + i / columns * pitch - scroll;
            if (y + cardH > content.write() && y < content.load()) {
               this.renderModuleCard(context, modules.get(i), NetHelper.init(x, y, cardW, cardH));
            }
         }

         if (modules.isEmpty() && !this.searchQuery.isBlank()) {
            context.init_PoolEntry()
               .drawCentered(
                  context.init_GuiGraphicsExtract(),
                  "No matching modules",
                  NetHelper.init(content.read(), content.write() + 4, content.update(), this.railItemHeight),
                  context.init_TableIndex().colors().textSecondary
               );
         }
      } finally {
         this.popClip(context);
      }

      this.renderScrollbar(context, "modules", content, contentHeight, scroll, frameSeconds);
   }

   private void renderModuleCard(SyncManager context, Module module, NetHelper card) {
      UiTheme theme = context.init_TableIndex();
      NetHelper clip = this.init_Deque.peek();
      boolean hovered = card.init_Z(context.read(), context.write()) && (clip == null || clip.init_Z(context.read(), context.write()));
      autismclient.gui.f.b.BufferHelper.init(context, card, "mod:" + module.id(), module.isEnabled(), hovered);
      NetHelper row = NetHelper.init(card.read(), card.write() + 1, card.update() - 2, this.rowHeight);
      String label = this.bindingModule == module ? "Press key" : module.name();
      boolean hasSettings = this.settingsView.hasVisibleSettings(module);
      int iconSize = Math.min(13, row.sync() - 2);
      NetHelper settings = NetHelper.init(row.init() - 1 - iconSize, row.write() + (row.sync() - iconSize) / 2, iconSize, iconSize);
      NetHelper pill = togglePillBounds(row, settings.read() - 4);
      int tail = module.hasActivationToggle() ? pill.read() : settings.read();
      int bindH = Math.max(13, row.sync() - 4);
      NetHelper bindButton = NetHelper.init(tail - 4 - 34, row.write() + (row.sync() - bindH) / 2, 34, bindH);
      int labelMax = Math.max(1, bindButton.read() - row.read() - 9);
      int nameColor = module.isEnabled() ? theme.colors().success : theme.colors().textPrimary;
      if (!this.searchQuery.isBlank()) {
         this.drawHighlightedLabel(context, label, row.read() + 5, context.init_PoolEntry().centerTextY(row), labelMax, nameColor, this.searchQuery.trim());
      } else {
         context.init_PoolEntry().drawFitted(context.init_GuiGraphicsExtract(), label, row.read() + 5, context.init_PoolEntry().centerTextY(row), labelMax, nameColor);
      }

      String description = module.description();
      NetHelper descriptionLine = NetHelper.init(card.read() + 5, row.load(), card.update() - 10, card.load() - 4 - row.load());
      context.init_PoolEntry()
         .drawEllipsized(
            context.init_GuiGraphicsExtract(),
            description,
            card.read() + 5,
            context.init_PoolEntry().centerTextY(descriptionLine),
            descriptionLine.update(),
            theme.colors().textSecondary
         );
      this.addHit(ModulesMenuController.Inner_CachePool.MODULE, card, module, null, "", -1);
      KeybindButtonRenderer.init(context, bindButton, module.keybind(), this.bindingModule == module, bindButton.init_Z(context.read(), context.write()));
      this.addHit(ModulesMenuController.Inner_CachePool.MODULE_BIND, bindButton, module, null, "", -1);
      if (module.hasActivationToggle()) {
         ToggleSwitchRenderer.init(context, pill, module.isEnabled(), pill.init_Z(context.read(), context.write()), "module-toggle:" + module.id());
         this.addHit(ModulesMenuController.Inner_CachePool.TOGGLE_MODULE, pill, module, null, "", -1);
      }

      if (hasSettings) {
         autismclient.gui.ui.widgets.IconButtonRenderer.init(
            context.init_GuiGraphicsExtract(),
            this.host.font(),
            settings,
            GuiIconAtlas.CHEVRON_RIGHT,
            settings.init_Z(context.read(), context.write()),
            true,
            false
         );
         this.addHit(ModulesMenuController.Inner_CachePool.MODULE_SETTINGS, settings, module, null, "", -1);
      }

      if (hovered && description != null && context.init_PoolEntry().measureWidth(description) > card.update() - 10) {
         this.tooltipText = description;
      }
   }

   private void renderGeneralPage(SyncManager context, NetHelper content, float frameSeconds) {
      int contentHeight = this.generalContentHeight(content.update());
      int scroll = this.updateScroll("general", content, contentHeight, frameSeconds);
      int gutter = contentHeight > content.sync() ? 6 : 0;
      this.pushClip(context, content);

      try {
         this.renderToolSection(context, content.read() + 4, content.write() + 4 - scroll, content.update() - 8 - gutter);
      } finally {
         this.popClip(context);
      }

      this.renderScrollbar(context, "general", content, contentHeight, scroll, frameSeconds);
   }

   private int generalContentHeight(int width) {
      return this.renderToolSection(null, 0, 0, Math.max(1, width - 8)) + 8;
   }

   private int renderToolSection(SyncManager context, int x, int top, int w) {
      boolean inWorld = !this.host.isTitleSetup();
      int y = top;
      y = inWorld
         ? this.renderToolPair(context, MACROS_TOOL, ADMIN_TOOL, x, y, w)
         : this.renderToolEntry(context, MACROS_TOOL, x, y, w, false);

      for (int i = 0; i + 1 < UTILITY_TOOLS.size(); i += 2) {
         y = this.renderToolPair(context, UTILITY_TOOLS.get(i), UTILITY_TOOLS.get(i + 1), x, y, w);
      }

      y = this.renderToolPair(context, AI_TOOL, SCHEMATICS_TOOL, x, y, w);
      if (inWorld) {
         y = this.init_I_SyncManager_3(context, "PACKET", autismclient.gui.f.a.DataUtil.tick, x, y, w);
         if (context != null) {
            this.renderPacketToggles(context, x, y, w);
         }

         y += this.settingsRowHeight;
         y = this.renderActionRow(context, ACTION_BUTTONS, x, y, w);
      }

      return y - top + 1;
   }

   private int renderToolPair(SyncManager context, ModulesMenuController.Inner_ConfigStore left, ModulesMenuController.Inner_ConfigStore right, int x, int y, int w) {
      if (context != null) {
         NetHelper[] cells = splitCells(x, y, w, this.settingsRowHeight);
         autismclient.gui.f.b.CachePool.Inner_DataUtil leftTone = entryTone(left);
         autismclient.gui.f.b.CachePool.Inner_DataUtil rightTone = entryTone(right);
         this.renderToolButton(context, left, leftTone, cells[0], ConnectedButtonRenderer.write);
         this.renderToolButton(context, right, rightTone, cells[1], ConnectedButtonRenderer.write);
         this.init_V_SyncManager(context, cells[1].read(), y, leftTone, rightTone);
      }

      return y + this.settingsRowHeight;
   }

   private int renderToolEntry(SyncManager context, ModulesMenuController.Inner_ConfigStore entry, int x, int y, int w, boolean bottom) {
      if (context != null) {
         this.renderToolButton(
            context,
            entry,
            entryTone(entry),
            NetHelper.init(x, y, w, this.settingsRowHeight),
            bottom ? ConnectedButtonRenderer.init(true, true) : ConnectedButtonRenderer.write
         );
      }

      return y + this.settingsRowHeight;
   }

   private int renderActionRow(SyncManager context, ModulesMenuController.Inner_DataUtil[] row, int x, int y, int w) {
      if (context != null) {
         NetHelper[] cells = splitCells(x, y, w, this.settingsRowHeight);
         this.renderActionButton(context, row[0], cells[0], ConnectedButtonRenderer.init(true, false));
         this.renderActionButton(context, row[1], cells[1], ConnectedButtonRenderer.init(false, true));
         this.init_V_SyncManager(context, cells[1].read(), y, row[0].style(), row[1].style());
      }

      return y + this.settingsRowHeight;
   }

   private int init_I_SyncManager_3(SyncManager context, String label, Identifier icon, int x, int y, int w) {
      int labelH = UiDensityMetrics.scaledHeight(14);
      if (context != null) {
         ConnectedButtonRenderer.init(context, NetHelper.init(x, y, w, labelH), label, icon);
      }

      return y + labelH;
   }

   private void renderPacketToggles(SyncManager context, int x, int y, int w) {
      NetHelper[] toggles = splitCells(x, y, w, this.settingsRowHeight);
      float sendT = autismclient.gui.ui.ToggleTransitionCache.init("packet:send", AutismSessionState.getInstance().isSendPacketsEnabled());
      float delayT = autismclient.gui.ui.ToggleTransitionCache.init("packet:delay", AutismSessionState.getInstance().isDelayPacketsEnabled());
      ConnectedButtonRenderer.init(context, toggles[0], "Send", null, toggles[0].init_Z(context.read(), context.write()), sendT, ConnectedButtonRenderer.write);
      this.addHit(ModulesMenuController.Inner_CachePool.UTILITY, toggles[0], null, null, "send", -1);
      ConnectedButtonRenderer.init(context, toggles[1], "Delay", null, toggles[1].init_Z(context.read(), context.write()), delayT, ConnectedButtonRenderer.write);
      this.addHit(ModulesMenuController.Inner_CachePool.UTILITY, toggles[1], null, null, "delay", -1);
      ConnectedButtonRenderer.init(
         context,
         toggles[1].read(),
         y,
         this.settingsRowHeight,
         ConnectedButtonRenderer.init(
            ConnectedButtonRenderer.init(context, sendT),
            ConnectedButtonRenderer.init_F_CachePool_DataUtil(autismclient.gui.f.b.CachePool.Inner_DataUtil.init, true, sendT),
            ConnectedButtonRenderer.init(context, delayT),
            ConnectedButtonRenderer.init_F_CachePool_DataUtil(autismclient.gui.f.b.CachePool.Inner_DataUtil.init, true, delayT)
         )
      );
   }

   private void init_V_SyncManager(
      SyncManager context, int x, int y, autismclient.gui.f.b.CachePool.Inner_DataUtil left, autismclient.gui.f.b.CachePool.Inner_DataUtil right
   ) {
      ConnectedButtonRenderer.init(
         context,
         x,
         y,
         this.settingsRowHeight,
         ConnectedButtonRenderer.init(
            ConnectedButtonRenderer.init(context, left),
            ConnectedButtonRenderer.init_F_CachePool_DataUtil(left, false, 0.0F),
            ConnectedButtonRenderer.init(context, right),
            ConnectedButtonRenderer.init_F_CachePool_DataUtil(right, false, 0.0F)
         )
      );
   }

   private static NetHelper[] splitCells(int x, int y, int w, int h) {
      int leftW = w / 2;
      return new NetHelper[]{NetHelper.init(x, y, leftW, h), NetHelper.init(x + leftW, y, w - leftW, h)};
   }

   private void renderToolButton(
      SyncManager context,
      ModulesMenuController.Inner_ConfigStore entry,
      autismclient.gui.f.b.CachePool.Inner_DataUtil tone,
      NetHelper cell,
      ConnectedButtonRenderer.Inner_DataUtil edges
   ) {
      ConnectedButtonRenderer.init(context, cell, entryLabel(entry), entry.icon(), tone, cell.init_Z(context.read(), context.write()), edges);
      this.addHit(ModulesMenuController.Inner_CachePool.UTILITY, cell, null, null, entry.id(), -1);
   }

   private void renderActionButton(SyncManager context, ModulesMenuController.Inner_DataUtil action, NetHelper cell, ConnectedButtonRenderer.Inner_DataUtil edges) {
      ConnectedButtonRenderer.init(context, cell, action.load(), null, action.style(), cell.init_Z(context.read(), context.write()), edges);
      this.addHit(ModulesMenuController.Inner_CachePool.UTILITY, cell, null, null, action.id(), -1);
   }

   private static String entryLabel(ModulesMenuController.Inner_ConfigStore entry) {
      return entry.load();
   }

   private static autismclient.gui.f.b.CachePool.Inner_DataUtil entryTone(ModulesMenuController.Inner_ConfigStore entry) {
      return autismclient.gui.f.b.CachePool.Inner_DataUtil.init;
   }

   private int init_I_SyncManager(SyncManager context) {
      boolean inWorld = !this.host.isTitleSetup();
      int pair = 54;
      List<ModulesMenuController.Inner_ConfigStore> iconCells = new ArrayList<>(UTILITY_TOOLS);
      iconCells.add(MACROS_TOOL);
      iconCells.add(AI_TOOL);
      iconCells.add(SCHEMATICS_TOOL);
      if (inWorld) {
         iconCells.add(ADMIN_TOOL);
      }

      for (ModulesMenuController.Inner_ConfigStore entry : iconCells) {
         pair = Math.max(pair, buttonWidth(context, entry.load(), true));
      }

      if (inWorld) {
         for (String label : new String[]{"Send", "Delay"}) {
            pair = Math.max(pair, buttonWidth(context, label, false));
         }

         for (ModulesMenuController.Inner_DataUtil cell : ACTION_BUTTONS) {
            pair = Math.max(pair, buttonWidth(context, cell.load(), false));
         }
      }

      int widest = Math.max(pair * 2, context.init_PoolEntry().measureWidth("Main Menu") + 11 + 3 + 16);
      return Math.max(120, widest) + 2;
   }

   private static int buttonWidth(SyncManager context, String label, boolean icon) {
      return context.init_PoolEntry().measureWidth(label) + 10 + 2 + (icon ? 13 : 0);
   }

   public boolean click(int mx, int my, int button) {
      return this.mouseClicked(mx, my, button, 0);
   }

   public boolean mouseClicked(int mx, int my, int button, int modifiers) {
      if (this.sync_Z) {
         return true;
      }

      if (this.captureBind(button)) {
         return true;
      }

      if (this.sync_Z()) {
         boolean overProfiles = this.contentArea != null && this.contentArea.init_Z(mx, my);
         if (overProfiles) {
            return this.profilesView.mouseClicked(mx, my, button);
         }

         if (this.flush_Z()) {
            return true;
         }

         this.profilesView.blurInputs();
      }

      if (this.dropdown != null) {
         this.dropdown.onMouseClicked(mx, my, button);
         if (!this.dropdown.isOpen()) {
            this.closeDropdown();
         }

         return true;
      } else if (this.colorPicker != null) {
         this.colorPicker.mouseClicked(mx, my, button);
         if (!this.colorPicker.isOpen()) {
            this.colorPicker = null;
         }

         return true;
      } else if (this.macroPicker != null) {
         if (!this.macroPicker.init(mx, my, button, modifiers)) {
            this.closeMacroPicker();
         }

         return true;
      } else {
         ModulesMenuController.Inner_StreamUtil clicked = this.hitAt(mx, my);
         boolean sameField = this.textEdit != null
            && clicked != null
            && clicked.type == ModulesMenuController.Inner_CachePool.TEXT_FIELD
            && clicked.setting == this.textEdit.owner;
         if (!sameField) {
            this.endTextEdit(true);
         }

         this.close_Z = false;
         this.open_Z = false;
         if (this.searchEdit != null
            && (clicked == null || clicked.type != ModulesMenuController.Inner_CachePool.TEXT_FIELD || !"search".equals(clicked.id))) {
            this.commitSearchEdit();
         }

         for (int i = this.hits.size() - 1; i >= 0; i--) {
            ModulesMenuController.Inner_StreamUtil hit = this.hits.get(i);
            if (hit.bounds.init_Z(mx, my) && (hit.load == null || hit.load.init_Z(mx, my))) {
               if (button != 0
                  && hit.type != ModulesMenuController.Inner_CachePool.MODULE
                  && hit.type != ModulesMenuController.Inner_CachePool.WINDOW_HEADER) {
                  return true;
               }

               switch (hit.type) {
                  case WINDOW_HEADER:
                     if (this.isClassicStyle()) {
                        this.onWindowHeaderPress(hit.id, mx, my, button);
                        return true;
                     }

                     if (button != 0) {
                        return true;
                     }

                     this.headerPressed = true;
                     this.pressX = mx;
                     this.pressY = my;
                     this.headerDragged = false;
                     this.dragOffsetX = mx - this.panelX;
                     this.dragOffsetY = my - this.panelY;
                     return true;
                  case WINDOW_BODY:
                     if (this.isClassicStyle()) {
                        this.bringToFront(hit.id);
                     }

                     return true;
                  case COLLAPSE:
                     if (button == 0) {
                        this.toggleCollapsed(hit.id);
                     }

                     return true;
                  case CLOSE:
                     if ("settings-window".equals(hit.id)) {
                        this.leaveSettings();
                     } else {
                        this.host.closeScreen();
                     }

                     return true;
                  case RAIL_ITEM:
                     this.selectCategory(hit.id);
                     return true;
                  case UTILITY:
                     if (button != 0) {
                        return true;
                     }

                     this.host.init(hit.id);
                     return true;
                  case MODULE:
                     if (button == 1) {
                        this.showModuleSettings(hit.module);
                     } else if (button == 2) {
                        this.bindingModule = hit.module;
                     } else if (button == 0) {
                        if (this.settingsView.hasVisibleSettings(hit.module)
                           && (hit.module.opensSettingsOnClick() || !hit.module.hasActivationToggle())) {
                           this.showModuleSettings(hit.module);
                        } else {
                           this.toggleModule(hit.module);
                        }
                     }

                     return true;
                  case MODULE_BIND:
                     if (button == 0) {
                        this.bindingModule = hit.module;
                     }

                     return true;
                  case MODULE_SETTINGS:
                     if (button == 0) {
                        this.showModuleSettings(hit.module);
                     }

                     return true;
                  case BACK:
                     this.leaveSettings();
                     return true;
                  case TOGGLE_MODULE:
                     if (hit.module.hasActivationToggle()) {
                        this.toggleModule(hit.module);
                     }

                     return true;
                  case BIND_MODULE:
                     this.bindingModule = hit.module;
                     return true;
                  case RESET_MODULE:
                     if (this.host.isTitleSetup()) {
                        hit.module.resetConfiguredSettings();
                     } else {
                        hit.module.resetSettings();
                     }

                     this.bindingOptionModule = null;
                     this.bindingOption = null;
                     AutismClientMessaging.sendPrefixed(hit.module.name() + " settings reset to defaults.");
                     return true;
                  case MACRO_MODULE:
                     Module module = hit.module;
                     this.leaveSettings();
                     this.host.init(module);
                     return true;
                  case GROUP:
                     read_Map.put(hit.module.id() + "|" + hit.id, !read_Map.getOrDefault(hit.module.id() + "|" + hit.id, false));
                     return true;
                  case OPTION:
                     this.settingsView.clickSetting(hit.module, hit.setting, button);
                     return true;
                  case FILE_PICKER:
                     if (hit.setting != null && hit.setting.action() != null) {
                        hit.setting.action().run();
                     }

                     return true;
                  case SLIDER:
                     this.sliderModule = hit.module;
                     this.sliderSetting = hit.setting;
                     this.activeRangeHandle = -1;
                     this.settingsView.init_V_ConfigStore_Stream_I(hit, mx);
                     return true;
                  case RANGE_SLIDER:
                     this.sliderModule = hit.module;
                     this.sliderSetting = hit.setting;
                     this.activeRangeHandle = this.settingsView.init_I_ConfigStore_Stream_I(hit, mx);
                     this.settingsView.init_V_ConfigStore_Stream_I(hit, mx);
                     return true;
                  case TEXT_FIELD:
                     if (button != 0) {
                        return true;
                     }

                     boolean extend = (modifiers & 1) != 0;
                     if ("search".equals(hit.id)) {
                        this.focusSearch(mx, extend);
                     } else if (this.textEdit != null && this.textEdit.owner == hit.setting) {
                        this.textEdit.init(this.init_I_StreamUtil(this.textEdit, mx, hit.bounds), extend);
                        this.close_Z = true;
                     } else {
                        this.beginTextEdit(hit.module, hit.setting, mx, hit.bounds);
                     }

                     return true;
                  case DROPDOWN:
                     this.init_V_ConfigStore_Stream(hit);
                     return true;
                  case COLOR_PICKER:
                     this.openColorPicker(hit.module, hit.setting, hit.bounds);
                     return true;
                  case LIST_EDITOR:
                     if (hit.setting != null) {
                        switch (hit.setting.kind()) {
                           case STRING_LIST:
                              this.host.load(hit.module, hit.setting);
                              break;
                           default:
                              this.host.read(hit.module, hit.setting);
                        }
                     }

                     return true;
                  case PACKET_EDITOR:
                     this.host.init(hit.module, hit.setting);
                     return true;
                  case MACRO_PICKER:
                     this.openMacroPicker(hit.module, hit.setting, hit.bounds);
                     return true;
                  case BIND_OPTION:
                     this.bindingOptionModule = hit.module;
                     this.bindingOption = hit.setting;
                     return true;
                  case RESET_OPTION_BIND:
                     this.setValue(hit.module, hit.setting, "-1");
                     return true;
                  case SCROLLBAR:
                     ScrollbarRenderer.ScrollbarBounds metrics = this.scrollbarFor(hit.id);
                     if (metrics != null && metrics.maxScroll() > 0) {
                        this.scrollbarGrabOffset = metrics.load(mx, my) ? my - metrics.load().write() : metrics.load().sync() / 2;
                        this.pop_Str = hit.id;
                        this.setScroll(hit.id, ScrollbarRenderer.scrollForDrag(metrics, my, this.scrollbarGrabOffset));
                     }

                     return true;
                  case CLASSIC_RESIZE:
                     this.handleHeaderClick(hit, mx, my);
                     return true;
               }
            }
         }

         return true;
      }
   }

   public boolean mouseReleased(int mx, int my, int button) {
      if (button != 0) {
         return true;
      }

      if (this.sync_Z() && this.profilesView.load(mx, my, button)) {
         return true;
      }

      Module releasedSlider = this.sliderModule;
      this.close_Z = false;
      this.open_Z = false;
      this.pop_Str = null;
      this.sliderModule = null;
      this.sliderSetting = null;
      this.activeRangeHandle = -1;
      if (releasedSlider != null) {
         releasedSlider.persistConfiguredState();
      }

      if (this.open_Str != null) {
         this.open_Str = null;
         this.host.load();
         return true;
      }

      if (this.colorPicker != null) {
         this.colorPicker.load(mx, my, button);
         if (!this.colorPicker.isOpen()) {
            this.colorPicker = null;
         }

         return true;
      } else if (this.dropdown != null) {
         this.dropdown.onMouseReleased(mx, my, button);
         if (!this.dropdown.isOpen()) {
            this.closeDropdown();
         }

         return true;
      } else {
         if (this.macroPicker != null) {
            this.macroPicker.load(mx, my, button);
            return true;
         }

         if (button == 0 && this.headerPressed) {
            this.headerPressed = false;
            if (!this.headerDragged && this.withinClickSlop(mx, my)) {
               this.panelCollapsed = !this.panelCollapsed;
            }

            savedPanelX = this.panelX;
            savedPanelY = this.panelY;
            AutismConfig config = AutismConfig.getGlobal();
            config.menuV2X = this.panelX;
            config.menuV2Y = this.panelY;
            this.host.load();
         }

         if (button == 0 && this.close_Str != null) {
            String releasedId = this.close_Str;
            this.close_Str = null;
            if (!this.headerDragged && this.withinClickSlop(mx, my)) {
               this.toggleCollapsed(releasedId);
            } else if (this.headerDragged) {
               AutismConfig.getGlobal().classicMenuLayoutCustom = true;
               this.host.load();
            }
         }

         return true;
      }
   }

   public boolean mouseDragged(int mx, int my, int button, double dx, double dy) {
      if (button != 0) {
         return true;
      }

      if (this.sync_Z() && this.profilesView.mouseDragged(mx, my, button, dx, dy)) {
         return true;
      }

      if (this.colorPicker != null) {
         this.colorPicker.mouseDragged(mx, my, button, dx, dy);
         if (!this.colorPicker.isOpen()) {
            this.colorPicker = null;
         }

         return true;
      } else {
         if (this.dropdown != null) {
            this.dropdown.onMouseDragged(mx, my, button, dx, dy);
            return true;
         }

         if (this.macroPicker != null) {
            this.macroPicker.init(mx, my, button, dx, dy);
            return true;
         }

         if (this.open_Z && this.searchEdit != null) {
            this.searchEdit.init(this.init_I_StreamUtil(this.searchEdit, mx, this.searchBounds), true);
            return true;
         }

         if (this.close_Z && this.textEdit != null && this.textEditBounds != null) {
            this.moveTextCursor(this.init_I_StreamUtil(this.textEdit, mx, this.textEditBounds), true);
            return true;
         }

         if (this.pop_Str != null) {
            ScrollbarRenderer.ScrollbarBounds metrics = this.scrollbarFor(this.pop_Str);
            if (metrics != null) {
               this.setScroll(this.pop_Str, ScrollbarRenderer.scrollForDrag(metrics, my, this.scrollbarGrabOffset));
            }

            return true;
         } else {
            if (this.open_Str != null) {
               this.dragResize(mx, my);
               return true;
            }

            if (this.close_Str != null) {
               if (!this.headerDragged && this.withinClickSlop(mx, my)) {
                  return true;
               }

               this.headerDragged = true;
               AutismConfig.getGlobal().classicMenuLayoutCustom = true;
               AutismConfig.ModuleCategoryLayout layout = this.categoryLayout(this.close_Str);
               NetHelper current = this.sync_Map.get(this.close_Str);
               int w = current != null ? current.update() : scaled(150);
               int h = current != null ? current.sync() : this.headerHeight;
               layout.f_x_I = clampInt(mx - this.dragOffsetX, 0, Math.max(0, this.screenWidth - 4 - w));
               layout.f_y_I = clampInt(my - this.dragOffsetY, 0, Math.max(0, this.screenHeight - 4 - h));
               return true;
            } else if (this.headerPressed) {
               if (!this.headerDragged && this.withinClickSlop(mx, my)) {
                  return true;
               }

               this.headerDragged = true;
               savedPanelX = this.panelX = mx - this.dragOffsetX;
               savedPanelY = this.panelY = my - this.dragOffsetY;
               return true;
            } else if (this.sliderModule != null && this.sliderSetting != null) {
               ModulesMenuController.Inner_StreamUtil hit = this.hitFor(this.sliderModule, this.sliderSetting);
               if (hit != null) {
                  this.settingsView.init_V_ConfigStore_Stream_I(hit, mx);
               }

               return true;
            } else {
               return true;
            }
         }
      }
   }

   public boolean mouseScrolled(int mx, int my, double scrollY) {
      if (this.colorPicker != null) {
         this.colorPicker.mouseScrolled(mx, my, scrollY);
         return true;
      }

      if (this.dropdown != null) {
         this.dropdown.onMouseScrolled(mx, my, scrollY);
         return true;
      }

      if (this.macroPicker != null) {
         this.macroPicker.mouseScrolled(mx, my, scrollY);
         return true;
      }

      String key;
      if (this.isClassicStyle()) {
         key = this.load_Str_I_I(mx, my);
         if (key == null) {
            return true;
         }
      } else {
         if (this.flush_Z()) {
            return true;
         }

         if (mx < this.panelX + 1 + this.railPixelWidth) {
            key = "rail";
         } else {
            if (this.sync_Z()) {
               if (this.contentArea != null && this.contentArea.init_Z(mx, my)) {
                  this.profilesView.mouseScrolled(mx, my, scrollY);
               }

               return true;
            }

            key = this.settingsModule != null ? "settings" : (this.isGeneralHome() ? "general" : "modules");
         }
      }

      int[] metrics = load_Map.get(key);
      if (metrics != null && metrics[1] > 0) {
         int current = Math.round(this.scrollTarget(key));
         int step = !this.isClassicStyle() && "modules".equals(key) ? this.cardPitch() : this.rowHeight + 1;
         int next = clampInt(current - (int)Math.round(scrollY * step), 0, metrics[1]);
         this.setScroll(key, next);
      }

      return true;
   }

   public boolean keyPressed(int key, int scanCode, int modifiers) {
      if (this.sync_Z || this.update_Z) {
         return true;
      }

      if (!this.sync_Z()) {
         long now = System.nanoTime();
         if (this.bindingModule != null) {
            this.bindingModule.setKeybind(KeybindButtonRenderer.init(key));
            this.bindingModule = null;
            this.lastBindNanos = now;
            return true;
         }

         if (this.bindingOption != null) {
            this.setValue(this.bindingOptionModule, this.bindingOption, Integer.toString(KeybindButtonRenderer.init(key)));
            this.bindingOption = null;
            this.bindingOptionModule = null;
            this.lastBindNanos = now;
            return true;
         }

         if (this.colorPicker != null && this.colorPicker.read(key, scanCode, modifiers)) {
            return true;
         }

         if (this.macroPicker != null) {
            if (key == 256) {
               this.closeMacroPicker();
            } else {
               this.macroPicker.read(key, scanCode, modifiers);
            }

            return true;
         } else if (this.dropdown != null) {
            if (key == 256) {
               this.closeDropdown();
            }

            return true;
         } else if (this.textEdit != null) {
            if (key == 256) {
               this.endTextEdit(false);
            } else {
               InlineTextEditState session = this.textEdit;
               boolean committed = key == 257 || key == 335;
               if (session.init(key, modifiers) && committed) {
                  this.endTextEdit(true);
               }
            }

            return true;
         } else if (this.searchEdit == null) {
            if (key == 256) {
               if (this.settingsModule != null) {
                  this.leaveSettings();
               } else {
                  this.host.closeScreen();
               }

               return true;
            } else if (key == 70 && (modifiers & 10) != 0) {
               this.focusSearch(-1);
               if (this.searchEdit != null) {
                  this.searchEdit.init(0, false);
                  this.searchEdit.init(this.searchEdit.text.length(), true);
               }

               return true;
            } else {
               return false;
            }
         } else {
            if (key == 256 && this.isClassicStyle() && this.settingsModule == null) {
               this.commitSearchEdit();
            } else if (key == 256) {
               if (!this.searchEdit.text.isEmpty()) {
                  this.searchEdit.text = "";
                  this.searchEdit.init(0, false);
                  this.applySearchQuery("");
               } else {
                  this.searchEdit = null;
               }
            } else if (key != 257 && key != 335) {
               this.searchEdit.init(key, modifiers);
               this.applySearchQuery(this.searchEdit.text);
            } else {
               this.commitSearchEdit();
            }

            return true;
         }
      } else {
         if (this.profilesView.read(key, scanCode, modifiers)) {
            return true;
         }

         if (key == 256) {
            this.host.closeScreen();
         }

         return true;
      }
   }

   public boolean charTyped(char chr) {
      if (this.sync_Z || this.update_Z) {
         return true;
      }

      if (this.sync_Z()) {
         return this.profilesView.charTyped(chr, 0);
      }

      if (System.nanoTime() - this.lastBindNanos < 100000000L) {
         return true;
      }

      if (this.dropdown != null) {
         return true;
      }

      if (this.colorPicker != null && this.colorPicker.charTyped(chr)) {
         return true;
      }

      if (this.macroPicker != null) {
         this.macroPicker.init(chr);
         return true;
      }

      if (this.textEdit != null) {
         this.textEdit.typeChar(chr);
         return true;
      }

      if (this.searchEdit != null) {
         this.searchEdit.typeChar(chr);
         this.applySearchQuery(this.searchEdit.text);
         return true;
      }

      boolean typeToSearch = this.isClassicStyle() ? (chr >= 'a' && chr <= 'z' || chr >= 'A' && chr <= 'Z') && !OverlayManager.getInstance().isActive() : chr >= ' ' && chr != 127;
      if (typeToSearch && this.settingsModule == null) {
         this.focusSearch(-1);
         if (this.searchEdit != null) {
            this.searchEdit.typeChar(chr);
            this.applySearchQuery(this.searchEdit.text);
         }

         return true;
      } else {
         return false;
      }
   }

   void addHit(ModulesMenuController.Inner_CachePool type, NetHelper bounds, Module module, Setting<?, ?> option, String id, int index) {
      this.hits.add(new ModulesMenuController.Inner_StreamUtil(type, bounds, module, option, id == null ? "" : id, index, this.init_Deque.peek()));
   }

   private ModulesMenuController.Inner_StreamUtil hitAt(int mx, int my) {
      for (int i = this.hits.size() - 1; i >= 0; i--) {
         ModulesMenuController.Inner_StreamUtil hit = this.hits.get(i);
         if (hit.bounds.init_Z(mx, my) && (hit.load == null || hit.load.init_Z(mx, my))) {
            return hit;
         }
      }

      return null;
   }

   void pushClip(SyncManager context, NetHelper bounds) {
      NetHelper parent = this.init_Deque.peek();
      if (parent != null) {
         int x = Math.max(parent.read(), bounds.read());
         int y = Math.max(parent.write(), bounds.write());
         bounds = NetHelper.init(x, y, Math.min(parent.init(), bounds.init()) - x, Math.min(parent.load(), bounds.load()) - y);
      }

      this.init_Deque.push(bounds);
      context.init_GuiGraphicsExtract().enableScissor(bounds.read(), bounds.write(), bounds.init(), bounds.load());
   }

   void popClip(SyncManager context) {
      context.init_GuiGraphicsExtract().disableScissor();
      this.init_Deque.pop();
   }

   private ModulesMenuController.Inner_StreamUtil hitFor(Module module, Setting<?, ?> option) {
      for (ModulesMenuController.Inner_StreamUtil hit : this.hits) {
         if ((hit.type == ModulesMenuController.Inner_CachePool.SLIDER || hit.type == ModulesMenuController.Inner_CachePool.RANGE_SLIDER)
            && hit.module == module
            && hit.setting == option) {
            return hit;
         }
      }

      return null;
   }

   void write(String text) {
      this.tooltipText = text;
   }

   boolean isListeningForBind(Module module) {
      return module != null && this.bindingModule == module;
   }

   boolean isEditing(Module module, Setting<?, ?> option) {
      return this.textEdit != null && this.textEdit.owner == option && option != null;
   }

   void beginTextEdit(Module module, Setting<?, ?> option, int mx, NetHelper bounds) {
      this.endTextEdit(true);
      this.textEditBounds = bounds;
      this.textEdit = new InlineTextEditState(option, module.value(option.id()), value -> this.setValue(module, option, value));
      if (mx >= 0) {
         this.textEdit.init(this.init_I_StreamUtil(this.textEdit, mx, bounds), false);
      }

      this.close_Z = true;
   }

   void endTextEdit(boolean commit) {
      if (this.textEdit != null) {
         if (commit) {
            this.textEdit.commit();
         }

         this.textEdit = null;
         this.textEditBounds = null;
         this.close_Z = false;
      }
   }

   private void moveTextCursor(int position, boolean extend) {
      if (this.textEdit != null) {
         this.textEdit.init(position, extend);
      }
   }

   private int init_I_StreamUtil(InlineTextEditState session, int mx, NetHelper bounds) {
      ToIntFunction<String> widths = value -> k.init(this.host.font(), session.text, value, autismclient.gui.f.a.DataUtil.init, 0);
      int scroll = session.scroll();
      return session.indexAt(mx, bounds.read() + 4 - scroll, widths::applyAsInt);
   }

   void setValue(Module module, Setting<?, ?> option, String value) {
      if (module != null && option != null) {
         if (this.host.isTitleSetup()) {
            module.setConfiguredValue(option.id(), value);
         } else {
            module.setValue(option.id(), value);
         }
      }
   }

   void setValueTransient(Module module, Setting<?, ?> option, String value) {
      if (module != null && option != null) {
         if (this.host.isTitleSetup()) {
            module.setConfiguredValueTransient(option.id(), value);
         } else {
            module.setValueTransient(option.id(), value);
         }
      }
   }

   ModulesMenuController.Inner_BufferHelper getHost() {
      return this.host;
   }

   void toggleModule(Module module) {
      if (module != null) {
         if (this.host.isTitleSetup()) {
            module.setConfiguredEnabled(!module.isEnabled());
         } else {
            module.toggle();
         }
      }
   }

   void showModuleSettings(Module module) {
      if (module != null) {
         this.close();
         this.endTextEdit(true);
         this.searchEdit = null;
         this.settingsModule = module;
         this.sync_Z = false;
         this.load_RingBuffer.snapTo(1.0F);
         this.update_J = System.nanoTime();
         this.flush_Z = true;
         if (this.isClassicStyle()) {
            AutismConfig.ModuleCategoryLayout settingsLayout = this.categoryLayout("settings-window");
            settingsLayout.f_x_I = -1;
            settingsLayout.f_y_I = -1;
            settingsLayout.collapsed = false;
            this.bringToFront("settings-window");
         }

         this.settingsView.onModuleOpened(module);
      }
   }

   private void leaveSettings() {
      this.endTextEdit(true);
      this.bindingModule = null;
      this.bindingOption = null;
      this.bindingOptionModule = null;
      this.closeDropdown();
      if (this.colorPicker != null) {
         this.colorPicker = null;
      }

      this.closeMacroPicker();
      this.searchEdit = null;
      this.open_Z = false;
      this.sync_Z = true;
      this.load_RingBuffer.snapTo(0.0F);
      this.update_J = System.nanoTime();
      this.flush_Z = false;
   }

   boolean isCollapsed(Module module, String group) {
      return read_Map.getOrDefault(module.id() + "|" + group, false);
   }

   private void focusSearch(int mx) {
      this.focusSearch(mx, false);
   }

   private void focusSearch(int mx, boolean extend) {
      if (!this.isClassicStyle() || this.settingsModule == null || this.settingsView.hasVisibleSettings(this.settingsModule)) {
         String initial = this.settingsModule != null ? this.settingsView.getQuery() : this.searchQuery;
         if (this.searchEdit == null) {
            this.searchEdit = new InlineTextEditState("search", initial, value -> {});
         }

         if (mx >= 0) {
            this.searchEdit.init(this.init_I_StreamUtil(this.searchEdit, mx, this.searchBounds), extend);
            this.open_Z = true;
         }

         if (this.isClassicStyle() && this.settingsModule == null) {
            AutismConfig.ModuleCategoryLayout search = this.categoryLayout("search");
            if (search.collapsed) {
               search.collapsed = false;
               this.host.load();
            }

            this.bringToFront("search");
         }
      }
   }

   private void commitSearchEdit() {
      this.applySearchQuery(this.searchEdit == null ? "" : this.searchEdit.text);
      this.searchEdit = null;
      this.open_Z = false;
   }

   private void applySearchQuery(String query) {
      if (this.settingsModule != null) {
         this.settingsView.setQuery(query);
      } else {
         this.searchQuery = query == null ? "" : query;
         this.setScroll("modules", 0.0F);
      }
   }

   private boolean matchesQuery(String query, Module module) {
      String needle = normalizeNeedle(query);
      if (needle.isEmpty()) {
         return true;
      }

      if (normalizeNeedle(module.name()).contains(needle)) {
         return true;
      }

      String description = module.description();
      return description != null && normalizeNeedle(description).contains(needle);
   }

   private static String normalizeNeedle(String value) {
      return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "");
   }

   private void drawHighlightedLabel(SyncManager context, String label, int x, int y, int maxWidth, int color, String query) {
      String safe = label == null ? "" : label;
      String needle = query == null ? "" : query.toLowerCase(Locale.ROOT);
      int match = needle.isEmpty() ? -1 : safe.toLowerCase(Locale.ROOT).indexOf(needle);
      if (match < 0) {
         context.init_PoolEntry().drawFitted(context.init_GuiGraphicsExtract(), safe, x, y, maxWidth, color);
      } else {
         int accent = context.init_TableIndex().colors().accent;
         String before = safe.substring(0, match);
         String hit = safe.substring(match, match + needle.length());
         String after = safe.substring(match + needle.length());
         int used = 0;
         context.init_PoolEntry().drawFitted(context.init_GuiGraphicsExtract(), before, x, y, maxWidth, color);
         used += context.init_PoolEntry().measureWidth(before);
         context.init_PoolEntry().drawFitted(context.init_GuiGraphicsExtract(), hit, x + used, y, Math.max(1, maxWidth - used), accent);
         used += context.init_PoolEntry().measureWidth(hit);
         context.init_PoolEntry().drawFitted(context.init_GuiGraphicsExtract(), after, x + used, y, Math.max(1, maxWidth - used), color);
      }
   }

   private void init_V_ConfigStore_Stream(ModulesMenuController.Inner_StreamUtil hit) {
      Module module = hit.module;
      Setting<?, ?> option = hit.setting;
      if (module != null && option != null) {
         this.closeColorPicker();
         this.closeMacroPicker();
         this.sliderModule = null;
         this.sliderSetting = null;
         this.headerPressed = false;
         this.dropdownModule = module;
         this.dropdownSetting = option;
         this.dropdown = new DropdownMenu(hit.bounds, option.choices(), module.value(option.id()), value -> {
            if (this.dropdownModule == module && this.dropdownSetting == option) {
               this.setValue(module, option, value);
            }
         });
         this.dropdown.open();
      } else {
         this.closeDropdown();
      }
   }

   private void closeDropdown() {
      this.dropdown = null;
      this.dropdownModule = null;
      this.dropdownSetting = null;
   }

   private void openColorPicker(Module module, Setting<?, ?> option, NetHelper anchor) {
      if (module != null && option != null) {
         this.closeDropdown();
         this.closeMacroPicker();
         this.endTextEdit(true);
         int fallback = parseColor(option.defaultValue(), -1);
         int initial = parseColor(module.value(option.id()), fallback);
         this.colorPicker = new autismclient.gui.ui.widgets.ColorPickerPopup(
            anchor, initial, fallback, this.screenWidth, this.screenHeight, argb -> this.setValue(module, option, String.format(Locale.ROOT, "%08X", argb))
         );
      }
   }

   private void closeColorPicker() {
      if (this.colorPicker != null) {
         this.colorPicker.dismiss();
      }

      this.colorPicker = null;
   }

   private void openMacroPicker(Module module, Setting<?, ?> option, NetHelper anchor) {
      if (module != null && option != null) {
         this.closeDropdown();
         this.closeColorPicker();
         this.endTextEdit(true);
         this.macroPicker = new MacroPickerPopup(this, module, option, anchor);
      }
   }

   void closeMacroPicker() {
      this.macroPicker = null;
   }

   void init_V_Module_Setting(Module module, Setting<?, ?> option) {
      this.closeMacroPicker();
      this.host.write(module, option);
   }

   private int railScroll(NetHelper rail, int contentHeight) {
      int max = Math.max(0, contentHeight - rail.sync());
      int scroll = clampInt(Math.round(this.scrollTarget("rail")), 0, max);
      load_Map.put("rail", new int[]{rail.sync(), max});
      return scroll;
   }

   private int railContentHeight() {
      int rows = 2;

      for (ae category : ae.init_List()) {
         if (!menuModules(category).isEmpty()) {
            rows++;
         }
      }

      return 6 + rows * (this.railItemHeight + 1) + 4 - 1;
   }

   private int railWidth(SyncManager context) {
      int widest = Math.max(context.init_PoolEntry().measureWidth("Main Menu"), context.init_PoolEntry().measureWidth("Profiles"));

      for (ae category : ae.init_List()) {
         if (!menuModules(category).isEmpty()) {
            widest = Math.max(widest, context.init_PoolEntry().measureWidth(category.load()));
         }
      }

      return widest + 12 + 5 + 16;
   }

   static int scaled(int px) {
      return Math.round(px * Math.max(1.0F, autismclient.gui.sdf.MsdfTextRenderer.fontScale()));
   }

   static NetHelper togglePillBounds(NetHelper row, int right) {
      int pillH = Math.min(11, Math.max(9, row.sync() - 3));
      int pillW = pillH * 2;
      return NetHelper.init(right - pillW, row.write() + (row.sync() - pillH) / 2, pillW, pillH);
   }

   private static int gridColumns(int width) {
      return width >= scaled(288) ? 2 : 1;
   }

   private int cardPitch() {
      int descriptionHeight = (int)Math.ceil(
         autismclient.gui.sdf.MsdfTextRenderer.lineHeight(
            autismclient.gui.sdf.MsdfFontFace.BODY, autismclient.gui.sdf.MsdfTextRenderer.scaledFontSize(autismclient.gui.sdf.MsdfFontFace.BODY)
         )
      );
      return 1 + this.rowHeight + Math.max(UiDensityMetrics.scaledHeight(12), descriptionHeight) + 4 + 4;
   }

   private int gridContentHeight(int count, int columns) {
      return (count + columns - 1) / columns * this.cardPitch() + 4;
   }

   private int updateScroll(String key, NetHelper viewport, int contentHeight, float frameSeconds) {
      int max = Math.max(0, contentHeight - viewport.sync());
      this.recordScrollMetrics(key, viewport.sync(), max);
      float target = clampFloat(this.scrollTarget(key), 0.0F, max);
      return Math.round(this.smoothScroll(key, target, frameSeconds));
   }

   void recordScrollMetrics(String key, int viewHeight, int maxScroll) {
      load_Map.put(key, new int[]{viewHeight, maxScroll});
   }

   float smoothScroll(String key, float target, float frameSeconds) {
      Float render = this.write_Map.get(key);
      if (render == null) {
         render = target;
      }

      float speed = 1.0F - (float)Math.exp(-frameSeconds * 18.0F);
      render = render + (target - render) * speed;
      if (Math.abs(render - target) < 0.4F) {
         render = target;
      }

      this.write_Map.put(key, render);
      return render;
   }

   private void renderScrollbar(SyncManager context, String key, NetHelper viewport, int contentHeight, int scroll, float frameSeconds) {
      this.drawScrollbar(key, context, viewport, contentHeight, scroll);
   }

   void drawScrollbar(String key, SyncManager context, NetHelper viewport, int contentHeight, int scroll) {
      int[] metrics = load_Map.get(key);
      if (metrics != null && metrics[1] > 0) {
         NetHelper track = NetHelper.init(viewport.init() - 4, viewport.write() + 2, 3, viewport.sync() - 4);
         ScrollbarRenderer.ScrollbarBounds m = ScrollbarRenderer.init(track, contentHeight, viewport.sync(), scroll);
         this.recordScrollbar(key, m);
         boolean hovered = m.init(context.read(), context.write());
         ScrollbarRenderer.init(context, m, hovered, key.equals(this.pop_Str));
         this.addHit(ModulesMenuController.Inner_CachePool.SCROLLBAR, track, null, null, key, -1);
      }
   }

   int init_I() {
      return this.activeRangeHandle;
   }

   private void recordScrollbar(String key, ScrollbarRenderer.ScrollbarBounds metrics) {
      this.scrollbarBounds.put(key, metrics);
   }

   private ScrollbarRenderer.ScrollbarBounds scrollbarFor(String key) {
      return this.scrollbarBounds.get(key);
   }

   float scrollTarget(String key) {
      return scrollTargets.getOrDefault(key, 0.0F);
   }

   void setScroll(String key, float value) {
      scrollTargets.put(key, value);
      this.write_Map.put(key, value);
   }

   private boolean isClassicStyle() {
      return !"CSGO".equals(AutismConfig.getGlobal().moduleMenuStyle);
   }

   private void init_V_SyncManager_2(SyncManager base, float frameSeconds, long now) {
      String hoverOwner = this.windowAt(base.read(), base.write());
      this.sync_Map.clear();
      this.mainMenuWidth = this.init_I_SyncManager(base);
      this.ensureClassicLayout();
      int rise = 0;
      if (this.settingsModule != null) {
         PacketUtil.init(base.init_GuiGraphicsExtract(), NetHelper.init(0, 0, this.screenWidth, this.screenHeight), base.init_TableIndex().colors().backdropScrim);
         this.renderSettingsWindow(base, frameSeconds, rise);
      } else {
         SyncManager blocked = null;

         for (String id : this.windowOrder()) {
            SyncManager context = base;
            if (hoverOwner != null && !hoverOwner.equals(id)) {
               if (blocked == null) {
                  blocked = UiHelper.init(base.init_GuiGraphicsExtract(), this.host.font(), -536870912, -536870912);
               }

               context = blocked;
            }

            if ("main-menu".equals(id)) {
               this.renderMainMenuWindow(context, frameSeconds, rise);
            } else if ("search".equals(id)) {
               this.renderSearchWindow(context, frameSeconds, rise);
            } else if ("settings-window".equals(id)) {
               if (this.settingsModule != null) {
                  this.renderSettingsWindow(context, frameSeconds, rise);
               }
            } else {
               ae category = categoryById(id);
               if (category != null) {
                  this.renderCategoryWindow(context, category, frameSeconds, rise);
               }
            }
         }
      }
   }

   private void renderCategoryWindow(SyncManager context, ae category, float frameSeconds, int rise) {
      List<Module> modules = menuModules(category);
      if (!modules.isEmpty()) {
         AutismConfig.ModuleCategoryLayout layout = this.categoryLayout(category.init_Str());
         int rowStep = this.rowHeight + 1;
         int contentH = modules.size() * rowStep + 3;
         int maxRows = layout.visibleRows > 0 ? layout.visibleRows : 14;
         int naturalBody = Math.min(contentH, maxRows * rowStep + 3);
         boolean automatic = !AutismConfig.getGlobal().classicMenuLayoutCustom;
         if (automatic) {
            naturalBody = Math.min(naturalBody, this.classicMaxBodyHeight);
         }

         int bodyH = this.animatedBodyHeight(category.init_Str(), naturalBody, layout.collapsed);
         NetHelper bounds = this.windowBounds(category.init_Str(), layout, automatic && this.classicGridWidth > 0 ? this.classicGridWidth : scaled(150), bodyH, rise);
         this.renderCategoryWindow(context, category.init_Str(), bounds, category.load(), layout.collapsed, true, false);
         if (bodyH > 0) {
            NetHelper viewport = this.windowViewport(bounds, bodyH);
            NetHelper target = this.load_NetHelper_NetHelper_I(viewport, naturalBody);
            String key = "classic:" + category.init_Str();
            int scroll = this.updateScroll(key, target, contentH, frameSeconds);
            boolean scrollbar = contentH > target.sync();
            int y = viewport.write() + 2 - scroll;
            this.pushClip(context, viewport);

            try {
               for (Module module : modules) {
                  NetHelper row = NetHelper.init(viewport.read() + 2, y, viewport.update() - 4 - (scrollbar ? 6 : 0), this.rowHeight);
                  if (row.load() >= viewport.write() && row.write() <= viewport.load()) {
                     this.init_V_SyncManager_5(context, module, row, false);
                  }

                  y += rowStep;
               }

               this.drawScrollbar(key, context, target, contentH, scroll);
            } finally {
               this.popClip(context);
            }

            if (!layout.collapsed && modules.size() > 2 && bodyH >= target.sync()) {
               this.renderResizeGrip(context, category.init_Str(), bounds, clampInt((target.sync() - 3) / rowStep, 1, modules.size()));
            }
         }
      }
   }

   private void renderResizeGrip(SyncManager context, String id, NetHelper bounds, int shownRows) {
      NetHelper grip = NetHelper.init(bounds.read() + 6, bounds.load() - 4, Math.max(1, bounds.update() - 12), 4);
      boolean active = id.equals(this.open_Str);
      boolean hover = !active && grip.init_Z(context.read(), context.write());
      UiColorScheme colors = context.init_TableIndex().colors();
      int color = active ? colors.accent : (hover ? colors.textPrimary : colors.textSecondary & 16777215 | 1929379840);
      int pillW = Math.min(20, Math.max(8, bounds.update() / 5));
      PacketUtil.load(context.init_GuiGraphicsExtract(), NetHelper.init(bounds.read() + (bounds.update() - pillW) / 2, bounds.load() - 2, pillW, 3), 1, color);
      if (hover || active) {
         context.init_GuiGraphicsExtract().requestCursor(CursorTypes.RESIZE_NS);
      }

      this.addHit(ModulesMenuController.Inner_CachePool.CLASSIC_RESIZE, grip, null, null, id, shownRows);
   }

   private void handleHeaderClick(ModulesMenuController.Inner_StreamUtil hit, int mx, int my) {
      String id = hit.id;
      this.bringToFront(id);
      long now = System.currentTimeMillis();
      if (id.equals(this.lastClickedId) && now - this.lastClickTime <= 350L) {
         this.lastClickedId = null;
         this.clearVisibleRows(id);
      } else {
         this.lastClickedId = id;
         this.lastClickTime = now;
         this.open_Str = id;
         this.unusedResizePressX = mx;
         this.resizePressY = my;
         this.resizeStartRows = hit.index;
      }
   }

   private void dragResize(int mx, int my) {
      ae category = categoryById(this.open_Str);
      if (category != null) {
         this.setVisibleRows(category, this.resizeStartRows + Math.round((float)(my - this.resizePressY) / (this.rowHeight + 1)));
      }
   }

   private void setVisibleRows(ae category, int rows) {
      AutismConfig.ModuleCategoryLayout layout = this.categoryLayout(category.init_Str());
      int rowStep = this.rowHeight + 1;
      int fitRows = (this.screenHeight - 4 - Math.max(0, layout.f_y_I) - this.headerHeight - 1 - 3) / rowStep;
      rows = clampInt(rows, 2, Math.max(2, Math.min(menuModules(category).size(), fitRows)));
      if (rows != layout.visibleRows) {
         AutismConfig.getGlobal().classicMenuLayoutCustom = true;
         layout.visibleRows = rows;
      }
   }

   public void setAllVisibleRows(int rows) {
      AutismConfig config = AutismConfig.getGlobal();
      if (rows < 0) {
         if (this.savedVisibleRows != null) {
            this.savedVisibleRows.forEach((id, saved) -> this.categoryLayout(id).visibleRows = saved);
            config.classicMenuLayoutCustom = this.savedLayoutCustom;
            this.savedVisibleRows = null;
         }
      } else {
         if (this.savedVisibleRows == null) {
            this.savedVisibleRows = new LinkedHashMap<>();
            this.savedLayoutCustom = config.classicMenuLayoutCustom;

            for (ae category : ae.init_List()) {
               this.savedVisibleRows.put(category.init_Str(), this.categoryLayout(category.init_Str()).visibleRows);
            }
         }

         for (ae category : ae.init_List()) {
            if (!menuModules(category).isEmpty()) {
               this.setVisibleRows(category, rows);
            }
         }
      }
   }

   private void clearVisibleRows(String id) {
      AutismConfig.ModuleCategoryLayout layout = this.categoryLayout(id);
      layout.visibleRows = 0;
      this.host.load();
   }

   private NetHelper load_NetHelper_NetHelper_I(NetHelper viewport, int targetBody) {
      int capped = Math.max(0, Math.min(targetBody, this.screenHeight - 4 - this.headerHeight - 1));
      return NetHelper.init(viewport.read(), viewport.write(), viewport.update(), capped);
   }

   private void renderSearchWindow(SyncManager context, float frameSeconds, int rise) {
      AutismConfig.ModuleCategoryLayout layout = this.categoryLayout("search");
      boolean automatic = !AutismConfig.getGlobal().classicMenuLayoutCustom;
      int w = automatic && this.classicGridWidth > 0 ? this.classicGridWidth : scaled(150);
      String query = this.searchEdit != null ? this.searchEdit.text : this.searchQuery;
      boolean hasQuery = !query.isBlank();
      List<Module> results = hasQuery ? this.searchModules(query) : List.of();
      int rowStep = this.rowHeight + 1;
      int fieldH = this.rowHeight;
      int strip = fieldH + 6;
      int listH = hasQuery ? Math.max(1, results.size()) * rowStep + 3 : 0;
      int contentH = strip + listH;
      int maxBody = automatic && this.classicMaxBodyHeight > 0
         ? Math.max(strip + rowStep, this.classicMaxBodyHeight)
         : Math.max(strip + rowStep, this.screenHeight - 4 - Math.max(0, layout.f_y_I) - this.headerHeight - 1);
      int naturalBody = Math.min(contentH, maxBody);
      int bodyH = this.animatedBodyHeight("search", naturalBody, layout.collapsed);
      NetHelper bounds = this.windowBounds("search", layout, w, bodyH, rise);
      this.renderCategoryWindow(context, "search", bounds, "Search", layout.collapsed, true, false);
      if (bodyH > 0) {
         NetHelper viewport = this.windowViewport(bounds, bodyH);
         NetHelper target = this.load_NetHelper_NetHelper_I(viewport, naturalBody);
         this.pushClip(context, viewport);

         try {
            this.searchBounds = NetHelper.init(viewport.read() + 3, viewport.write() + 3, viewport.update() - 6, fieldH);
            this.renderSearchBox(context, "Search modules...");
            if (hasQuery) {
               NetHelper list = NetHelper.init(target.read(), target.write() + strip, target.update(), Math.max(0, target.sync() - strip));
               String key = "classic:search";
               int scroll = this.updateScroll(key, list, listH, frameSeconds);
               boolean scrollbar = listH > list.sync();
               this.pushClip(context, list);

               try {
                  int y = list.write() + 2 - scroll;
                  if (results.isEmpty()) {
                     NetHelper row = NetHelper.init(list.read() + 2, y, list.update() - 4, this.rowHeight);
                     context.init_PoolEntry()
                        .drawFitted(
                           context.init_GuiGraphicsExtract(),
                           "No matches",
                           row.read() + 6,
                           context.init_PoolEntry().centerTextY(row),
                           Math.max(1, row.update() - 12),
                           context.init_TableIndex().colors().textSecondary
                        );
                  }

                  for (Module module : results) {
                     NetHelper row = NetHelper.init(list.read() + 2, y, list.update() - 4 - (scrollbar ? 6 : 0), this.rowHeight);
                     if (row.load() >= list.write() && row.write() <= list.load()) {
                        this.init_V_SyncManager_5(context, module, row, true);
                     }

                     y += rowStep;
                  }

                  this.drawScrollbar(key, context, list, listH, scroll);
               } finally {
                  this.popClip(context);
               }
            }
         } finally {
            this.popClip(context);
         }
      }
   }

   private void init_V_SyncManager_5(SyncManager context, Module module, NetHelper row, boolean highlightQuery) {
      UiTheme theme = context.init_TableIndex();
      boolean hovered = row.init_Z(context.read(), context.write());
      float hoverT = autismclient.gui.ui.HoverTransitionCache.sample("cmod:" + module.id(), hovered);
      float onT = autismclient.gui.ui.ToggleTransitionCache.init("modon:" + module.id(), module.isEnabled());
      PacketUtil.load(context.init_GuiGraphicsExtract(), row, 4, lerpColor(theme.colors().cardSurface, theme.colors().cardSurfaceElevated, hoverT));
      if (onT > 0.001F) {
         int accent = theme.colors().accent;
         PacketUtil.load(context.init_GuiGraphicsExtract(), row, 4, PacketUtil.init(accent, (0.16F + 0.06F * hoverT) * onT));
         PacketUtil.load(context.init_GuiGraphicsExtract(), row, 4, 0, PacketUtil.init(accent, 0.85F * onT));
      }

      String label = this.bindingModule == module ? "Press key" : module.name();
      int iconSize = Math.min(13, row.sync() - 2);
      int bindH = Math.max(13, row.sync() - 4);
      int tail = row.init() - 2;
      NetHelper settings = NetHelper.init(tail - iconSize, row.write() + (row.sync() - iconSize) / 2, iconSize, iconSize);
      tail = settings.read() - 2;
      NetHelper bindButton = NetHelper.init(tail - 34, row.write() + (row.sync() - bindH) / 2, 34, bindH);
      boolean showBind = row.update() >= scaled(110);
      int labelMax = Math.max(1, (showBind ? bindButton.read() : tail) - row.read() - 9);
      int nameColor = module.isEnabled() ? theme.colors().textPrimary : theme.colors().textSecondary;
      if (highlightQuery && !this.searchQuery.isBlank()) {
         this.drawHighlightedLabel(context, label, row.read() + 6, context.init_PoolEntry().centerTextY(row), labelMax, nameColor, this.searchQuery.trim());
      } else {
         context.init_PoolEntry().drawFitted(context.init_GuiGraphicsExtract(), label, row.read() + 6, context.init_PoolEntry().centerTextY(row), labelMax, nameColor);
      }

      this.addHit(ModulesMenuController.Inner_CachePool.MODULE, row, module, null, "", -1);
      if (showBind) {
         KeybindButtonRenderer.init(context, bindButton, module.keybind(), this.bindingModule == module, bindButton.init_Z(context.read(), context.write()));
         this.addHit(ModulesMenuController.Inner_CachePool.MODULE_BIND, bindButton, module, null, "", -1);
      }

      if (settings != null) {
         autismclient.gui.ui.widgets.IconButtonRenderer.init(
            context.init_GuiGraphicsExtract(),
            this.host.font(),
            settings,
            GuiIconAtlas.CHEVRON_RIGHT,
            settings.init_Z(context.read(), context.write()),
            true,
            false
         );
         this.addHit(ModulesMenuController.Inner_CachePool.MODULE_SETTINGS, settings, module, null, "", -1);
      }

      if (hovered) {
         String description = module.description();
         this.tooltipText = showBind ? description : module.name() + (description != null && !description.isBlank() ? "\n" + description : "");
      }
   }

   private void renderMainMenuWindow(SyncManager context, float frameSeconds, int rise) {
      AutismConfig.ModuleCategoryLayout layout = this.categoryLayout("main-menu");
      this.mainMenuWidth = this.init_I_SyncManager(context);
      int w = this.mainMenuWidth;
      int contentH = this.renderToolSection(null, 0, 0, w - 2);
      int maxBody = Math.max(40, this.screenHeight - 8 - this.headerHeight);
      int bodyH = this.animatedBodyHeight("main-menu", Math.min(contentH, maxBody), layout.collapsed);
      NetHelper bounds = this.windowBounds("main-menu", layout, w, bodyH, rise);
      this.renderCategoryWindow(context, "main-menu", bounds, "Main Menu", layout.collapsed, true, false);
      if (bodyH > 0) {
         NetHelper viewport = this.windowViewport(bounds, bodyH);
         NetHelper target = this.load_NetHelper_NetHelper_I(viewport, Math.min(contentH, maxBody));
         String scrollKey = "classic:main-menu";
         int scroll = this.updateScroll(scrollKey, target, contentH, frameSeconds);
         int gutter = contentH > target.sync() ? 6 : 0;
         this.pushClip(context, viewport);

         try {
            this.renderToolSection(context, viewport.read(), viewport.write() - scroll, viewport.update() - gutter);
            this.drawScrollbar(scrollKey, context, target, contentH, scroll);
         } finally {
            this.popClip(context);
         }
      }
   }

   private void renderSettingsWindow(SyncManager context, float frameSeconds, int rise) {
      Module module = this.settingsModule;
      AutismConfig.ModuleCategoryLayout layout = this.categoryLayout("settings-window");
      boolean searchable = this.settingsView.hasVisibleSettings(module);
      int searchStrip = searchable ? UiDensityMetrics.scaledHeight(13) + 8 : 0;
      int maxW = Math.max(160, this.screenWidth - 8);
      int w = Math.min(scaled((searchable ? 360 : 250) + (module instanceof CrosshairModule ? 140 : 0)), maxW);
      int maxBody = Math.max(60, this.screenHeight - 12 - this.headerHeight);
      int expandedBodyH = Math.min(this.settingsView.contentHeight(module) + searchStrip, maxBody);
      int bodyH = Math.round(this.animatedBodyHeight("settings-window", expandedBodyH, layout.collapsed) * this.load_RingBuffer.value());
      if (layout.f_x_I < 0 || layout.f_y_I < 0) {
         layout.f_x_I = Math.max(4, (this.screenWidth - w) / 2);
         layout.f_y_I = Math.max(6, (this.screenHeight - (this.headerHeight + expandedBodyH + 1)) / 2);
      }

      NetHelper bounds = this.windowBounds("settings-window", layout, w, bodyH, rise);
      this.renderCategoryWindow(context, "settings-window", bounds, module.name(), layout.collapsed, true, true);
      if (bodyH > 0) {
         NetHelper viewport = this.windowViewport(bounds, bodyH);
         this.pushClip(context, viewport);

         try {
            if (searchable) {
               this.searchBounds = NetHelper.init(viewport.read() + 4, viewport.write() + 4, viewport.update() - 8, UiDensityMetrics.scaledHeight(13));
               this.renderSearchBox(context);
            }

            NetHelper fullContent = NetHelper.init(
               viewport.read(),
               viewport.write() + searchStrip,
               viewport.update(),
               Math.max(1, Math.min(expandedBodyH, Math.max(1, this.screenHeight - this.headerHeight - 5)) - searchStrip)
            );
            this.settingsView.render(context, fullContent, module, frameSeconds);
         } finally {
            this.popClip(context);
         }
      }
   }

   private void renderCategoryWindow(SyncManager context, String id, NetHelper bounds, String title, boolean collapsed, boolean collapse, boolean close) {
      int header = this.headerHeight;
      boolean headerHovered = WindowHeaderHelper.read(bounds, header, context.read(), context.write());
      WindowFrameRenderer.init(context, bounds, title, collapsed, collapse, close, headerHovered, true, 4, 4, header);
      this.addHit(ModulesMenuController.Inner_CachePool.WINDOW_BODY, bounds, null, null, id, -1);
      this.addHit(ModulesMenuController.Inner_CachePool.WINDOW_HEADER, WindowHeaderHelper.init(bounds, header), null, null, id, -1);
      if (collapse) {
         this.addHit(ModulesMenuController.Inner_CachePool.COLLAPSE, WindowHeaderHelper.read(bounds, header), null, null, id, -1);
      }

      if (close) {
         this.addHit(ModulesMenuController.Inner_CachePool.CLOSE, WindowHeaderHelper.load(bounds, header), null, null, id, -1);
      }
   }

   private NetHelper windowBounds(String id, AutismConfig.ModuleCategoryLayout layout, int w, int bodyH, int rise) {
      int h = this.headerHeight + bodyH + (bodyH > 0 ? 1 : 0);
      NetHelper bounds = NetHelper.init(Math.max(0, layout.f_x_I), Math.max(0, layout.f_y_I), w, h).init_NetHelper(this.screenWidth - 4, this.screenHeight - 4);
      NetHelper drawn = NetHelper.init(bounds.read(), bounds.write() + rise, bounds.update(), bounds.sync());
      this.sync_Map.put(id, drawn);
      return drawn;
   }

   private int animatedBodyHeight(String id, int expandedHeight, boolean collapsed) {
      EaseOutAnimator transition = this.collapseAnimations.computeIfAbsent(id, key -> new EaseOutAnimator(collapsed ? 0.0F : 1.0F, 180));
      return Math.round(Math.max(0, expandedHeight) * transition.animateTo(!collapsed && !this.update_Z ? 1.0F : 0.0F));
   }

   private NetHelper windowViewport(NetHelper bounds, int bodyH) {
      return NetHelper.init(bounds.read() + 1, bounds.write() + this.headerHeight, bounds.update() - 2, Math.min(bodyH, Math.max(0, bounds.sync() - this.headerHeight - 1)));
   }

   private void renderSearchBox(SyncManager context) {
      this.renderSearchBox(context, "Search");
   }

   private void renderSearchBox(SyncManager context, String placeholder) {
      boolean searchFocused = this.searchEdit != null;
      String shown = searchFocused ? this.searchEdit.text : (this.settingsModule != null ? this.settingsView.getQuery() : this.searchQuery);
      TextFieldRenderer.init(
         context,
         this.searchBounds,
         shown,
         placeholder,
         searchFocused,
         searchFocused ? this.searchEdit.cursor : shown.length(),
         searchFocused ? this.searchEdit.selectionStart() : -1,
         searchFocused ? this.searchEdit.selectionEnd() : -1,
         searchFocused
            ? this.searchEdit.init(Math.max(1, this.searchBounds.update() - 8), fragment -> context.init_PoolEntry().measureFragmentWidth(shown, fragment))
            : 0
      );
      this.addHit(ModulesMenuController.Inner_CachePool.TEXT_FIELD, this.searchBounds, null, null, "search", -1);
   }

   private void ensureClassicLayout() {
      AutismConfig config = AutismConfig.getGlobal();
      if (!config.classicMenuLayoutCustom) {
         this.autoArrangeClassicLayout();
      } else {
         boolean broken = false;
         Set<Long> taken = new HashSet<>();
         List<String> ids = new ArrayList<>();
         ids.add("main-menu");

         for (ae category : ae.init_List()) {
            if (!menuModules(category).isEmpty()) {
               ids.add(category.init_Str());
            }
         }

         for (String id : ids) {
            AutismConfig.ModuleCategoryLayout layout = this.categoryLayout(id);
            if (layout.f_x_I < 0 || layout.f_y_I < 0) {
               broken = true;
            }

            if (!taken.add((long)layout.f_x_I << 32 | layout.f_y_I & 4294967295L)) {
               broken = true;
            }
         }

         if (broken) {
            config.classicMenuLayoutCustom = false;
            this.autoArrangeClassicLayout();
            this.host.load();
         } else {
            AutismConfig.ModuleCategoryLayout search = this.categoryLayout("search");
            if (search.f_x_I < 0 || search.f_y_I < 0) {
               AutismConfig.ModuleCategoryLayout utility = this.categoryLayout("main-menu");
               search.f_x_I = Math.max(4, utility.f_x_I);
               search.f_y_I = Math.min(
                  Math.max(4, utility.f_y_I) + this.headerHeight + this.renderToolSection(null, 0, 0, this.mainMenuWidth - 2) + 1 + 4, Math.max(4, this.screenHeight - this.headerHeight - 40)
               );
               this.host.load();
            }
         }
      }

      this.normalizeWindowOrder();
   }

   private void autoArrangeClassicLayout() {
      int gap = 4;
      int rowStep = this.rowHeight + 1;
      List<String> ids = new ArrayList<>();

      for (ae category : ae.init_List()) {
         if (!menuModules(category).isEmpty()) {
            ids.add(category.init_Str());
         }
      }

      ids.add("search");
      int utilityW = Math.max(1, this.mainMenuWidth);
      int categoryW = scaled(150);
      AutismConfig.ModuleCategoryLayout utility = this.categoryLayout("main-menu");
      utility.f_x_I = gap;
      utility.f_y_I = gap;
      utility.visibleRows = 0;
      int left = gap + utilityW + gap;
      int[] grid = gridLayout(Math.max(categoryW + gap * 2, this.screenWidth - left + gap), this.screenHeight, ids.size(), categoryW, this.headerHeight, rowStep);
      int columns = grid[0];
      int slotH = grid[2];
      this.classicGridWidth = grid[1];
      this.classicMaxBodyHeight = Math.max(0, slotH - this.headerHeight - 1);

      for (int index = 0; index < ids.size(); index++) {
         AutismConfig.ModuleCategoryLayout layout = this.categoryLayout(ids.get(index));
         layout.f_x_I = left + index % columns * (this.classicGridWidth + gap);
         layout.f_y_I = gap + index / columns * (slotH + gap);
         layout.visibleRows = Math.max(1, (this.classicMaxBodyHeight - 3) / rowStep);
      }
   }

   static int[] gridLayout(int width, int height, int count, int desiredWidth, int header, int rowStep) {
      int gap = 4;
      count = Math.max(1, count);
      int maxRows = Math.max(1, (height - gap) / Math.max(1, header + rowStep + 8));
      int columns = Math.min(count, Math.max(Math.max(1, (width - gap) / Math.max(1, desiredWidth + gap)), (count + maxRows - 1) / maxRows));
      int rows = (count + columns - 1) / columns;
      return new int[]{columns, Math.max(1, Math.min(desiredWidth, (width - gap * (columns + 1)) / columns)), Math.max(1, (height - gap * (rows + 1)) / rows)};
   }

   private AutismConfig.ModuleCategoryLayout categoryLayout(String id) {
      return AutismConfig.getGlobal().moduleCategoryLayouts.computeIfAbsent(id, ignored -> new AutismConfig.ModuleCategoryLayout());
   }

   private List<String> windowOrder() {
      this.normalizeWindowOrder();
      return List.copyOf(init_List);
   }

   private void normalizeWindowOrder() {
      List<String> valid = new ArrayList<>();
      valid.add("main-menu");

      for (ae category : ae.init_List()) {
         valid.add(category.init_Str());
      }

      valid.add("search");
      valid.add("settings-window");
      init_List.removeIf(idx -> !valid.contains(idx));

      for (String id : valid) {
         if (!init_List.contains(id)) {
            init_List.add(id);
         }
      }
   }

   private void bringToFront(String id) {
      if (id != null && !id.isBlank()) {
         this.normalizeWindowOrder();
         init_List.remove(id);
         init_List.add(id);
      }
   }

   private String windowAt(int mx, int my) {
      List<String> order = this.windowOrder();

      for (int i = order.size() - 1; i >= 0; i--) {
         NetHelper bounds = this.sync_Map.get(order.get(i));
         if (bounds != null && bounds.init_Z(mx, my)) {
            return order.get(i);
         }
      }

      return null;
   }

   private String load_Str_I_I(int mx, int my) {
      String id = this.windowAt(mx, my);
      if (id == null) {
         return null;
      } else if ("main-menu".equals(id)) {
         return "classic:main-menu";
      } else {
         return "settings-window".equals(id) ? "settings" : "classic:" + id;
      }
   }

   private void onWindowHeaderPress(String id, int mx, int my, int button) {
      this.bringToFront(id);
      if (button == 1) {
         this.toggleCollapsed(id);
      } else if (button == 0) {
         AutismConfig.ModuleCategoryLayout layout = this.categoryLayout(id);
         this.close_Str = id;
         this.pressX = mx;
         this.pressY = my;
         this.headerDragged = false;
         NetHelper drawn = this.sync_Map.get(id);
         this.dragOffsetX = mx - (drawn == null ? layout.f_x_I : drawn.read());
         this.dragOffsetY = my - (drawn == null ? layout.f_y_I : drawn.write());
      }
   }

   private boolean withinClickSlop(int mx, int my) {
      return Math.abs(mx - this.pressX) <= 3 && Math.abs(my - this.pressY) <= 3;
   }

   private void toggleCollapsed(String id) {
      if (id != null) {
         AutismConfig.ModuleCategoryLayout layout = this.categoryLayout(id);
         layout.collapsed = !layout.collapsed;
         this.host.load();
      }
   }

   private static ae categoryById(String id) {
      for (ae category : ae.init_List()) {
         if (category.init_Str().equals(id)) {
            return category;
         }
      }

      return null;
   }

   private List<Module> searchModules(String query) {
      String needle = load_Str_Str(query);
      if (needle.isEmpty()) {
         return List.of();
      }

      List<Module> out = new ArrayList<>();

      for (ae category : ae.init_List()) {
         for (Module module : menuModules(category)) {
            String name = load_Str_Str(module.name());
            if (name.contains(needle) || load_Str_Str(module.id()).contains(needle) || isSubsequence(needle, name)) {
               out.add(module);
            }
         }
      }

      return out;
   }

   private static String load_Str_Str(String value) {
      if (value == null) {
         return "";
      }

      StringBuilder out = new StringBuilder(value.length());

      for (int i = 0; i < value.length(); i++) {
         char c = value.charAt(i);
         if (c != ' ' && c != '-' && c != '_' && c != '.') {
            out.append(Character.toLowerCase(c));
         }
      }

      return out.toString();
   }

   private static boolean isSubsequence(String needle, String haystack) {
      if (needle.isEmpty()) {
         return false;
      }

      int i = 0;

      for (int j = 0; j < haystack.length() && i < needle.length(); j++) {
         if (haystack.charAt(j) == needle.charAt(i)) {
            i++;
         }
      }

      return i == needle.length();
   }

   private boolean isGeneralHome() {
      return "\u0000general".equals(selectedCategoryKey) && this.searchQuery.isBlank();
   }

   private boolean sync_Z() {
      return this.profilesView != null && !this.isClassicStyle() && !this.panelCollapsed;
   }

   private boolean flush_Z() {
      return this.sync_Z() && this.profilesView.init_Z();
   }

   public void close() {
      if (this.profilesView != null) {
         this.profilesView.reset();
         this.profilesView = null;
         this.contentArea = null;
      }
   }

   private ae selectedCategory() {
      if (selectedCategoryKey == null) {
         return null;
      }

      for (ae category : ae.init_List()) {
         if (category.init_Str().equals(selectedCategoryKey)) {
            return category;
         }
      }

      return null;
   }

   private void selectCategory(String key) {
      if (key != null && (!key.equals(selectedCategoryKey) || this.settingsModule != null || !this.searchQuery.isBlank())) {
         this.close();
         this.searchEdit = null;
         this.searchQuery = "";
         this.setScroll("modules", 0.0F);
         selectedCategoryKey = key;
         this.settingsModule = null;
         this.sync_Z = false;
         this.load_RingBuffer.snapTo(0.0F);
         this.update_J = System.nanoTime();
         this.flush_Z = false;
      }
   }

   private static List<Module> menuModules(ae category) {
      List<Module> out = new ArrayList<>();

      for (Module module : al.init_List_ae(category)) {
         if (module.showInModuleMenu()) {
            out.add(module);
         }
      }

      return out;
   }

   private boolean captureBind(int button) {
      if (this.bindingModule != null) {
         this.bindingModule.setKeybind(ValueSink.init_I(button));
         this.bindingModule = null;
         this.lastBindNanos = System.nanoTime();
         return true;
      } else if (this.bindingOption != null) {
         this.setValue(this.bindingOptionModule, this.bindingOption, Integer.toString(ValueSink.init_I(button)));
         this.bindingOption = null;
         this.bindingOptionModule = null;
         this.lastBindNanos = System.nanoTime();
         return true;
      } else {
         return false;
      }
   }

   private static GuiGraphicsExtractor init_GuiGraphicsExtract_SyncManager(SyncManager context) {
      return context.init_GuiGraphicsExtract();
   }

   static int lerpColor(int from, int to, float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      int ar = from >> 16 & 0xFF;
      int ag = from >> 8 & 0xFF;
      int ab = from & 0xFF;
      int aa = from >>> 24 & 0xFF;
      int br = to >> 16 & 0xFF;
      int bg = to >> 8 & 0xFF;
      int bb = to & 0xFF;
      int ba = to >>> 24 & 0xFF;
      int r = Math.round(ar + (br - ar) * t);
      int g = Math.round(ag + (bg - ag) * t);
      int b = Math.round(ab + (bb - ab) * t);
      int a = Math.round(aa + (ba - aa) * t);
      return a << 24 | r << 16 | g << 8 | b;
   }

   static int scaleAlpha(int color, float alphaScale) {
      int a = Math.round((color >>> 24 & 0xFF) * Math.max(0.0F, Math.min(1.0F, alphaScale)));
      return a << 24 | color & 16777215;
   }

   static int parseInt(String value, int fallback) {
      try {
         return Integer.parseInt(value.trim());
      } catch (Throwable t) {
         return fallback;
      }
   }

   static double parseDouble(String value, double fallback) {
      try {
         return Double.parseDouble(value.trim());
      } catch (Throwable t) {
         return fallback;
      }
   }

   static int parseColor(String value, int fallback) {
      if (value == null) {
         return fallback;
      }

      String v = value.trim();
      if (v.startsWith("#")) {
         v = v.substring(1);
      }

      if (!v.isEmpty() && v.length() <= 8) {
         try {
            long parsed = Long.parseLong(v, 16);
            if (v.length() <= 6) {
               parsed |= 4278190080L;
            }

            return (int)(parsed & 4294967295L);
         } catch (Throwable t) {
            return fallback;
         }
      } else {
         return fallback;
      }
   }

   static int clampInt(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   static float clampFloat(float value, float min, float max) {
      return Math.max(min, Math.min(max, value));
   }

   public interface Inner_BufferHelper {
      boolean isTitleSetup();

      Screen screen();

      Font font();

      void closeScreen();

      void load();

      void init(Module var1, Setting<?, ?> var2);

      void load(Module var1, Setting<?, ?> var2);

      void read(Module var1, Setting<?, ?> var2);

      void write(Module var1, Setting<?, ?> var2);

      void init(Module var1, Setting<?, ?> var2, AutismMacro var3);

      void init(String var1);

      void init(Module var1);
   }

   enum Inner_CachePool {
      WINDOW_HEADER,
      WINDOW_BODY,
      COLLAPSE,
      CLOSE,
      RAIL_ITEM,
      UTILITY,
      MODULE,
      MODULE_BIND,
      MODULE_SETTINGS,
      BACK,
      TOGGLE_MODULE,
      BIND_MODULE,
      RESET_MODULE,
      MACRO_MODULE,
      GROUP,
      OPTION,
      FILE_PICKER,
      SLIDER,
      RANGE_SLIDER,
      TEXT_FIELD,
      DROPDOWN,
      COLOR_PICKER,
      LIST_EDITOR,
      PACKET_EDITOR,
      MACRO_PICKER,
      BIND_OPTION,
      RESET_OPTION_BIND,
      SCROLLBAR,
      CLASSIC_RESIZE;
   }

   private record Inner_ConfigStore(String id, String load, Identifier icon) {
   }

   private record Inner_DataUtil(String id, String load, autismclient.gui.f.b.CachePool.Inner_DataUtil style) {
   }

   record Inner_StreamUtil(
      ModulesMenuController.Inner_CachePool type,
      NetHelper bounds,
      Module module,
      Setting<?, ?> setting,
      String id,
      int index,
      NetHelper load
   ) {
   }
}
