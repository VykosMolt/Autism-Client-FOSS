package autismclient.util;

import autismclient.gui.ui.widgets.ToggleSwitchRenderer;
import autismclient.gui.ui.widgets.TooltipRenderer;
import autismclient.gui.ui.widgets.UiLayoutMath;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class AutismKeybindOverlay extends DraggableOverlayWindow {
   private static final Minecraft CLIENT = Minecraft.getInstance();
   private static final String SETTINGS_TITLE = "Settings";
   private static final String INSIDE_GUI_TOOLTIP = "Fire keybinds while a container screen is open. Blocked in chat/sign/book editors and focused text fields.";
   private static final String CUSTOM_MENU_TOOLTIP = "Use our custom main menu. Turn off for the normal vanilla/Fabric title screen (mods stay visible).";
   private static final int flush_I = 120;
   private static final int close_I = 14;
   private static final int open = 4;
   private static final int poll = 10;
   private static final int push = 4;
   private static final int pop = 22;
   private static final int tick = 47;
   private static final int run = 34;
   private static final float init_F_2 = 3.0F;
   private final autismclient.gui.f.b.CharEncoder theme = new autismclient.gui.f.b.CharEncoder();
   private final autismclient.gui.ui.layout.CollapsibleWindowNode settingsWindow = new autismclient.gui.ui.layout.CollapsibleWindowNode("Settings").setScrollable(true);
   private final autismclient.gui.ui.layout.LayoutTreeHost layoutHost = new autismclient.gui.ui.layout.LayoutTreeHost(this.theme, this.settingsWindow);
   private final List<AutismKeybindOverlay.KeybindEntry> keybindEntries = new ArrayList<>();
   private final List<AutismKeybindOverlay.KeybindRow> keybindRows = new ArrayList<>();
   private boolean draggingWindow = false;
   private float dragOffsetX;
   private float dragOffsetY;
   private float pressX;
   private float pressY;
   private int pressWindowX;
   private int pressWindowY;
   private boolean dragMoved = false;
   private int listeningEntryIndex = -1;
   private String hoveredTooltip = null;
   private float tooltipX;
   private float tooltipY;
   private int cachedLabelColumnWidth = -1;
   private int cachedPanelWidth = -1;
   private float cachedFontScale = 0.0F / 0.0F;
   private boolean draggingScrollbar;
   private int scrollbarGrabOffset;
   private autismclient.gui.ui.widgets.DropdownMenu openDropdown;

   public AutismKeybindOverlay() {
      super("autism-keybinds", 120, 142);
      this.windowX = 160;
      this.windowY = 46;
      if (!AutismLiteVariant.enabled()) {
         this.keybindEntries
            .add(
               new AutismKeybindOverlay.KeybindEntry(
                  "Module Menu",
                  "Opens the Autism module click GUI",
                  () -> this.globalConfig().keybindModuleMenu,
                  v -> this.globalConfig().keybindModuleMenu = v
               )
            );
         this.keybindEntries
            .add(
               new AutismKeybindOverlay.KeybindEntry(
                  "AI Assistant",
                  "Opens the AI Assistant utility window",
                  () -> this.globalConfig().keybindAiAssistant,
                  v -> this.globalConfig().keybindAiAssistant = v
               )
            );
      }

      this.keybindEntries
         .add(
            new AutismKeybindOverlay.KeybindEntry(
               "Load GUI",
               "Restores the last saved GUI screen and handler",
               () -> this.globalConfig().keybindLoadGui,
               v -> this.globalConfig().keybindLoadGui = v
            )
         );
      this.keybindEntries
         .add(
            new AutismKeybindOverlay.KeybindEntry(
               "Flush Queue",
               "Sends all queued packets to the server",
               () -> this.globalConfig().keybindFlushQueue,
               v -> this.globalConfig().keybindFlushQueue = v
            )
         );
      this.keybindEntries
         .add(
            new AutismKeybindOverlay.KeybindEntry(
               "Clear Queue",
               "Discards all queued packets",
               () -> this.globalConfig().keybindClearQueue,
               v -> this.globalConfig().keybindClearQueue = v
            )
         );
      this.keybindEntries
         .add(
            new AutismKeybindOverlay.KeybindEntry(
               "Toggle Logger",
               "Shows or hides the packet logger overlay",
               () -> this.globalConfig().keybindToggleLogger,
               v -> this.globalConfig().keybindToggleLogger = v
            )
         );
      this.keybindEntries
         .add(
            new AutismKeybindOverlay.KeybindEntry(
               "Toggle Send",
               "Turns packet sending on or off",
               () -> this.globalConfig().keybindToggleSend,
               v -> this.globalConfig().keybindToggleSend = v
            )
         );
      this.keybindEntries
         .add(
            new AutismKeybindOverlay.KeybindEntry(
               "Toggle Delay",
               "Turns packet delay on or off",
               () -> this.globalConfig().keybindToggleDelay,
               v -> this.globalConfig().keybindToggleDelay = v
            )
         );
      this.configureSettingsWindow();
   }

   private void configureSettingsWindow() {
      this.settingsWindow.setCenterTitle(false);
      this.settingsWindow.setTitleTone(autismclient.gui.f.b.l.load);
      this.settingsWindow.setHeaderButtons(true, true);
      this.settingsWindow.setTitleInsets(this.padding() + 2, this.padding() + this.headerButtonSize() * 2 + 8);
      this.settingsWindow.body().clearChildren();
      this.settingsWindow.body().withGap(this.bodyGap()).withPadding(autismclient.gui.ui.layout.EdgeInsets.uniform(this.padding()));
      this.settingsWindow
         .body()
         .addChild(
            new AutismKeybindOverlay.ToggleRow(
               "Inside GUI",
               "Fire keybinds while a container screen is open. Blocked in chat/sign/book editors and focused text fields.",
               () -> this.globalConfig().keybindInsideGui,
               v -> this.globalConfig().keybindInsideGui = v
            )
         );
      if (!AutismLiteVariant.enabled()) {
         this.settingsWindow
            .body()
            .addChild(
               new AutismKeybindOverlay.ToggleRow(
                  "Custom Menu",
                  "Use our custom main menu. Turn off for the normal vanilla/Fabric title screen (mods stay visible).",
                  () -> this.globalConfig().customMainMenu,
                  v -> {
                     AutismConfig config = this.globalConfig();
                     config.customMainMenu = v;
                     config.save();
                  }
               )
            );
      }

      this.settingsWindow.body().addChild(new AutismKeybindOverlay.OverlayScaleRow());
      this.settingsWindow.body().addChild(new AutismKeybindOverlay.FontScaleRow());
      if (!AutismLiteVariant.enabled()) {
         this.settingsWindow.body().addChild(new AutismKeybindOverlay.MenuStyleRow());
      }

      this.settingsWindow
         .body()
         .addChild(
            new AutismKeybindOverlay.ToggleRow("Auto Probe", "Auto-Scans on join.", () -> this.globalConfig().autoProbePlugins, v -> {
               AutismConfig config = this.globalConfig();
               config.autoProbePlugins = v;
               config.save();
            })
         );
      this.settingsWindow
         .body()
         .addChild(
            new AutismKeybindOverlay.ToggleRow("InfiniChat", "Remove chat length limit.", () -> this.globalConfig().infiniChat, v -> {
               AutismConfig config = this.globalConfig();
               config.infiniChat = v;
               config.save();
            })
         );
      this.settingsWindow
         .body()
         .addChild(
            new AutismKeybindOverlay.ToggleRow(
               "Chat Feedback", "Show module enable/disable messages in chat.", () -> this.globalConfig().chatFeedback, v -> {
                  AutismConfig config = this.globalConfig();
                  config.chatFeedback = v;
                  config.save();
               }
            )
         );
      this.settingsWindow
         .body()
         .addChild(
            new AutismKeybindOverlay.ToggleRow("Stop On Leave", "Stop macros on disconnect.", () -> this.globalConfig().stopMacroOnLeave, v -> {
               AutismConfig config = this.globalConfig();
               config.stopMacroOnLeave = v;
               config.save();
            })
         );
      this.settingsWindow.body().addChild(new AutismKeybindOverlay.CommandPrefixRow());
      this.keybindRows.clear();

      for (int i = 0; i < this.keybindEntries.size(); i++) {
         AutismKeybindOverlay.KeybindRow row = new AutismKeybindOverlay.KeybindRow(i, this.keybindEntries.get(i));
         this.keybindRows.add(row);
         this.settingsWindow.body().addChild(row);
      }

      if (!AutismLiteVariant.enabled()) {
         autismclient.gui.ui.layout.RowLayout settingsPages = new autismclient.gui.ui.layout.RowLayout()
            .gap(0)
            .padding(autismclient.gui.ui.layout.EdgeInsets.ZERO)
            .setUnderlineOnHover(false);
         settingsPages.setGrowX(true);
         settingsPages.addChild(
            new autismclient.gui.ui.layout.ButtonNode("Theme Color", autismclient.gui.ui.layout.ButtonNode.Variant.PRIMARY, this::openThemeColorScreen)
               .withGrowX(true)
               .setConnectedStyle(true)
               .setConnectedEdges(autismclient.gui.ui.widgets.ConnectedButtonRenderer.load)
               .setFixedHeight(this.toolbarHeight())
         );
         settingsPages.addChild(
            new autismclient.gui.ui.layout.ButtonNode("Profiles", autismclient.gui.ui.layout.ButtonNode.Variant.PRIMARY, this::openProfilesOverlay)
               .withGrowX(true)
               .setConnectedStyle(true)
               .setConnectedEdges(autismclient.gui.ui.widgets.ConnectedButtonRenderer.read)
               .setFixedHeight(this.toolbarHeight())
         );
         this.settingsWindow.body().addChild(settingsPages);
      }
   }

   private void openThemeColorScreen() {
      CLIENT.gui.setScreen(new autismclient.gui.screens.ThemeColorScreen(CLIENT.gui.screen()));
   }

   private void openProfilesOverlay() {
      if (!AutismLiteVariant.enabled()) {
         autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
         AutismOverlay profiles = module == null ? null : module.getOrCreateProfilesOverlay();
         if (profiles != null) {
            ((AutismProfilesOverlay)profiles).load(false);
            profiles.setVisible(true);
            OverlayManager manager = OverlayManager.getInstance();
            manager.register(profiles, AutismOverlay.RenderLayer.HOST_SCREEN);
            manager.bringToFront(profiles);
         }
      }
   }

   private AutismConfig globalConfig() {
      return AutismConfig.getGlobal();
   }

   @Override
   public int getMinWidth() {
      return this.contentWidth();
   }

   @Override
   public int getMinHeight() {
      return this.theme.load() + this.bodyMinHeight();
   }

   @Override
   public boolean isResizable() {
      return true;
   }

   @Override
   protected int getResizeMinHeight() {
      return this.theme.load() + this.padding() * 2 + 4 * (autismclient.gui.ui.UiDensityMetrics.scaledHeight(14) + 2);
   }

   @Override
   public OverlayBounds getBounds() {
      return new OverlayBounds(this.windowX, this.windowY, this.windowWidth, this.windowHeight, this.windowVisible, this.windowCollapsed);
   }

   @Override
   public void setBounds(OverlayBounds bounds) {
      OverlayBounds clamped = this.clampBoundsToViewport(bounds);
      this.windowX = clamped.x;
      this.windowY = clamped.y;
      this.windowWidth = clamped.width;
      this.windowHeight = clamped.height;
      this.windowVisible = clamped.visible;
      this.windowCollapsed = clamped.collapsed;
      this.settingsWindow.setShowBody(!this.windowCollapsed);
   }

   @Override
   public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      if (this.windowVisible || this.isClosing()) {
         this.hoveredTooltip = null;
         autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
         float uiMouseX = viewport.toUiX(mouseX);
         float uiMouseY = viewport.toUiY(mouseY);
         int contentWidth = this.contentWidth();
         this.windowWidth = this.resolveWindowWidth(contentWidth, contentWidth);
         boolean active = OverlayManager.getInstance().isFocused(this) || OverlayManager.getInstance().isTopmost(this);
         boolean headerHovered = uiMouseX >= this.windowX
            && uiMouseX < this.windowX + this.windowWidth
            && uiMouseY >= this.windowY
            && uiMouseY < this.windowY + this.theme.load();
         autismclient.gui.ui.layout.LayoutContext metrics = new autismclient.gui.ui.layout.LayoutContext(
            context, CLIENT.font, viewport, this.theme, uiMouseX, uiMouseY, delta
         );
         this.settingsWindow.setExpanded(!this.windowCollapsed);
         this.settingsWindow.setActive(active);
         this.settingsWindow.setHeaderHovered(headerHovered);
         int contentHeight = Math.max(this.theme.load(), Math.round(this.settingsWindow.measureHeight(metrics, this.windowWidth)));
         this.windowHeight = this.windowCollapsed ? contentHeight : Math.min(contentHeight, this.resolveWindowHeight(contentHeight, this.getResizeMinHeight()));
         OverlayBounds clamped = this.clampBoundsToViewport(new OverlayBounds(this.windowX, this.windowY, this.windowWidth, this.windowHeight, this.windowVisible, this.windowCollapsed));
         if (!this.windowCollapsed && clamped.height < contentHeight && this.windowWidth < contentWidth + 6) {
            this.windowWidth = contentWidth + 6;
            clamped = this.clampBoundsToViewport(new OverlayBounds(this.windowX, this.windowY, this.windowWidth, this.windowHeight, this.windowVisible, this.windowCollapsed));
         }

         this.windowX = clamped.x;
         this.windowY = clamped.y;
         this.windowWidth = clamped.width;
         this.windowHeight = clamped.height;
         this.settingsWindow.setBounds(this.windowX, this.windowY, this.windowWidth, this.windowHeight);
         this.layoutHost.render(context, mouseX, mouseY, delta);
         if (!this.windowCollapsed && this.openDropdown != null && this.openDropdown.isOpen()) {
            context.nextStratum();
            viewport.pushTransform(context);

            try {
               this.openDropdown
                  .render(autismclient.gui.f.UiHelper.init(context, CLIENT.font, Math.round(uiMouseX), Math.round(uiMouseY)));
            } finally {
               viewport.popTransform(context);
            }
         }

         if (!this.windowCollapsed && this.hoveredTooltip != null) {
            context.nextStratum();
            this.drawTooltipAt(context, viewport, this.hoveredTooltip, this.tooltipX, this.tooltipY);
         }
      }
   }

   private void drawTooltipAt(GuiGraphicsExtractor context, autismclient.gui.ui.layout.LayoutViewport viewport, String text, float uiX, float uiY) {
      viewport.pushTransform(context);

      try {
         TooltipRenderer.render(
            autismclient.gui.f.UiHelper.init(context, CLIENT.font, Math.round(uiX), Math.round(uiY)),
            text,
            Math.round(uiX),
            Math.round(uiY),
            this.tooltipMaxWidth()
         );
      } finally {
         viewport.popTransform(context);
      }
   }

   public static String keyName(int keyCode) {
      return ValueSink.init_Str(keyCode);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.windowVisible) {
         return false;
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      float uiMouseX = viewport.toUiX(mouseX);
      float uiMouseY = viewport.toUiY(mouseY);
      if (this.openDropdown != null && this.openDropdown.isOpen()) {
         autismclient.gui.ui.UiInputResult result = this.openDropdown.onMouseClicked(Math.round(uiMouseX), Math.round(uiMouseY), button);
         if (!this.openDropdown.isOpen()) {
            this.openDropdown = null;
         }

         return result == autismclient.gui.ui.UiInputResult.HANDLED;
      } else if (this.isOverCloseButton(uiMouseX, uiMouseY)) {
         this.windowVisible = false;
         this.listeningEntryIndex = -1;
         this.draggingWindow = false;
         this.dragMoved = false;
         return true;
      } else if (button == 0 && this.isOverDragHandle(uiMouseX, uiMouseY)) {
         this.draggingWindow = true;
         this.dragMoved = false;
         this.dragOffsetX = uiMouseX - this.windowX;
         this.dragOffsetY = uiMouseY - this.windowY;
         this.pressX = uiMouseX;
         this.pressY = uiMouseY;
         this.pressWindowX = this.windowX;
         this.pressWindowY = this.windowY;
         return true;
      } else if (this.windowCollapsed) {
         return this.containsPoint(mouseX, mouseY);
      } else if (this.listeningEntryIndex >= 0 && ValueSink.load_Z(button)) {
         this.clearDuplicateBinds(ValueSink.init_I(button));
         return true;
      } else {
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics bodyBar = this.settingsWindow.scrollbarMetrics();
         if (button == 0 && bodyBar != null && bodyBar.containsTrack(uiMouseX, uiMouseY)) {
            this.draggingScrollbar = true;
            this.settingsWindow.setScrollbarDragging(true);
            this.scrollbarGrabOffset = bodyBar.containsThumb(uiMouseX, uiMouseY) ? Math.round(uiMouseY) - bodyBar.thumbY() : bodyBar.thumbHeight() / 2;
            this.settingsWindow.setScrollOffset(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(bodyBar, uiMouseY, this.scrollbarGrabOffset));
            return true;
         } else {
            return this.layoutHost.mouseClicked(mouseX, mouseY, button) ? true : this.containsPoint(mouseX, mouseY);
         }
      }
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (!this.windowVisible || this.windowCollapsed) {
         return false;
      } else if (this.openDropdown != null && this.openDropdown.isOpen() && keyCode == 256) {
         this.openDropdown.close();
         this.openDropdown = null;
         return true;
      } else if (this.layoutHost.keyPressed(keyCode, scanCode, modifiers)) {
         return true;
      } else if (this.windowVisible && this.listeningEntryIndex >= 0) {
         this.clearDuplicateBinds(autismclient.gui.ui.widgets.KeybindButtonRenderer.normalizeKey(keyCode));
         return true;
      } else {
         return false;
      }
   }

   private void clearDuplicateBinds(int bindCode) {
      for (int i = 0; i < this.keybindEntries.size(); i++) {
         if (i != this.listeningEntryIndex && this.keybindEntries.get(i).getter.getAsInt() == bindCode) {
            this.keybindEntries.get(i).setter.accept(-1);
         }
      }

      this.keybindEntries.get(this.listeningEntryIndex).setter.accept(bindCode);
      this.globalConfig().save();
      this.listeningEntryIndex = -1;
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (this.openDropdown != null && this.openDropdown.isOpen()) {
         autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
         return this.openDropdown.onMouseReleased(Math.round(viewport.toUiX(mouseX)), Math.round(viewport.toUiY(mouseY)), button) == autismclient.gui.ui.UiInputResult.HANDLED;
      } else if (this.draggingWindow) {
         this.draggingWindow = false;
         this.saveBounds();
         return true;
      } else if (this.draggingScrollbar) {
         this.draggingScrollbar = false;
         this.settingsWindow.setScrollbarDragging(false);
         return true;
      } else {
         return this.windowCollapsed ? false : this.layoutHost.mouseReleased(mouseX, mouseY, button);
      }
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.openDropdown != null && this.openDropdown.isOpen()) {
         autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
         return this.openDropdown.onMouseDragged(Math.round(viewport.toUiX(mouseX)), Math.round(viewport.toUiY(mouseY)), button, deltaX, deltaY)
            == autismclient.gui.ui.UiInputResult.HANDLED;
      }

      if (!this.draggingWindow) {
         if (this.draggingScrollbar) {
            autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics bodyBar = this.settingsWindow.scrollbarMetrics();
            if (bodyBar != null) {
               this.settingsWindow.setScrollOffset(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(bodyBar, this.layoutHost.createViewport().toUiY(mouseY), this.scrollbarGrabOffset));
            }

            return true;
         } else {
            return this.windowCollapsed ? false : this.layoutHost.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
         }
      } else {
         autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
         float uiMouseX = viewport.toUiX(mouseX);
         float uiMouseY = viewport.toUiY(mouseY);
         OverlayBounds clamped = this.clampBoundsToViewport(
            new OverlayBounds(Math.round(uiMouseX - this.dragOffsetX), Math.round(uiMouseY - this.dragOffsetY), this.windowWidth, this.windowHeight, this.windowVisible, this.windowCollapsed)
         );
         this.windowX = clamped.x;
         this.windowY = clamped.y;
         this.dragMoved = this.dragMoved
            || Math.abs(uiMouseX - this.pressX) >= 3.0F
            || Math.abs(uiMouseY - this.pressY) >= 3.0F
            || this.windowX != this.pressWindowX
            || this.windowY != this.pressWindowY;
         return true;
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      if (this.windowCollapsed) {
         return false;
      } else if (this.openDropdown != null && this.openDropdown.isOpen()) {
         autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
         return this.openDropdown.onMouseScrolled(Math.round(viewport.toUiX(mouseX)), Math.round(viewport.toUiY(mouseY)), amount)
            == autismclient.gui.ui.UiInputResult.HANDLED;
      } else if (this.layoutHost.mouseScrolled(mouseX, mouseY, amount)) {
         return true;
      } else if (this.settingsWindow.maxScroll() > 0.0F && this.containsPoint(mouseX, mouseY)) {
         this.settingsWindow.setScrollOffset(this.settingsWindow.scrollOffset() - (float)amount * (autismclient.gui.ui.UiDensityMetrics.scaledHeight(14) + 2));
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean charTyped(char chr, int modifiers) {
      return this.windowVisible && !this.windowCollapsed ? this.layoutHost.charTyped(chr, modifiers) : false;
   }

   @Override
   public boolean isVisible() {
      return this.windowVisible;
   }

   @Override
   public void setVisible(boolean v) {
      this.windowVisible = v;
      if (!v) {
         this.listeningEntryIndex = -1;
         this.draggingWindow = false;
         this.dragMoved = false;
         this.closeDropdown();
      } else {
         this.settingsWindow.setShowBody(!this.windowCollapsed);
         OverlayManager.getInstance().bringToFront(this);
      }
   }

   public void toggleKeybindOverlay() {
      this.setVisible(!this.windowVisible);
   }

   @Override
   public boolean containsPoint(double mouseX, double mouseY) {
      if (!this.windowVisible) {
         return false;
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      float uiX = viewport.toUiX(mouseX);
      float uiY = viewport.toUiY(mouseY);
      return uiX >= this.windowX && uiX < this.windowX + this.windowWidth && uiY >= this.windowY && uiY < this.windowY + this.windowHeight;
   }

   @Override
   public boolean isOverHeader(double mouseX, double mouseY) {
      if (!this.windowVisible) {
         return false;
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      return this.isOverDragHandle(viewport.toUiX(mouseX), viewport.toUiY(mouseY));
   }

   private boolean isOverDragHandle(float uiMouseX, float uiMouseY) {
      return autismclient.gui.ui.widgets.WindowHeaderHelper.init(this.windowRect(), this.theme.load(), false, true, uiMouseX, uiMouseY);
   }

   private boolean unusedIsOverClose(float uiMouseX, float uiMouseY) {
      return autismclient.gui.ui.widgets.WindowHeaderHelper.read(this.windowRect(), this.theme.load(), uiMouseX, uiMouseY);
   }

   @Override
   public boolean isCollapsed() {
      return this.windowCollapsed;
   }

   @Override
   public boolean supportsHeaderCollapseClick() {
      return true;
   }

   @Override
   public void setCollapsed(boolean c) {
      if (this.windowCollapsed != c) {
         this.windowCollapsed = c;
         this.settingsWindow.setShowBody(!this.windowCollapsed);
         if (c) {
            this.listeningEntryIndex = -1;
            this.closeDropdown();
            this.clearFocusAndDismissMenu();
         }

         this.saveBounds();
      }
   }

   @Override
   public boolean hasFocusedTextInput() {
      return this.layoutHost.hasFocusedInput();
   }

   @Override
   public boolean isCapturingKeybind() {
      return this.windowVisible && this.listeningEntryIndex >= 0;
   }

   @Override
   public void clearInputFocus() {
      this.closeDropdown();
      this.layoutHost.clearFocus();
   }

   private boolean isOverCloseButton(float uiMouseX, float uiMouseY) {
      return autismclient.gui.ui.widgets.WindowHeaderHelper.overClose(this.windowRect(), this.theme.load(), uiMouseX, uiMouseY);
   }

   private autismclient.gui.f.NetHelper windowRect() {
      return autismclient.gui.f.NetHelper.init(this.windowX, this.windowY, this.windowWidth, Math.max(this.theme.load(), this.windowHeight));
   }

   private OverlayBounds clampBoundsToViewport(OverlayBounds bounds) {
      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      int margin = 4;
      int viewportW = Math.round(viewport.width());
      int viewportH = Math.round(viewport.height());
      int availableW = Math.max(1, viewportW - margin * 2);
      int availableH = Math.max(this.theme.load(), viewportH - margin * 2);
      int width = Math.max(Math.min(this.getMinWidth(), availableW), Math.min(bounds.width, availableW));
      int minHeight = bounds.collapsed ? this.theme.load() : this.getMinHeight();
      int height = Math.max(Math.min(minHeight, availableH), Math.min(bounds.height, availableH));
      int renderedHeight = bounds.collapsed ? this.theme.load() : height;
      int x = Math.max(margin, Math.min(bounds.x, Math.max(margin, viewportW - margin - width)));
      int y = Math.max(margin, Math.min(bounds.y, Math.max(margin, viewportH - margin - renderedHeight)));
      return new OverlayBounds(x, y, width, height, bounds.visible, bounds.collapsed);
   }

   private int labelColumnWidth() {
      if (CLIENT.font == null) {
         return this.fallbackPanelWidth();
      }

      if (this.cachedLabelColumnWidth > 0) {
         return this.cachedLabelColumnWidth;
      }

      List<String> labels = new ArrayList<>(this.keybindEntries.size() + 9);
      labels.add("Inside GUI");
      labels.add("Custom Menu");
      labels.add("Overlay Scale");
      labels.add("Font Scale");
      labels.add("Menu Style");
      labels.add("Auto Probe");
      labels.add("InfiniChat");
      labels.add("Stop On Leave");
      labels.add("Command Prefix");

      for (AutismKeybindOverlay.KeybindEntry entry : this.keybindEntries) {
         labels.add(entry.label);
      }

      this.cachedLabelColumnWidth = Math.max(
         this.minLabelColumnWidth(),
         UiLayoutMath.maxTextWidth(
               CLIENT.font,
               this.theme.init_Identifier_l(autismclient.gui.f.b.l.read),
               this.theme.load(autismclient.gui.f.b.l.read),
               labels
            )
            + this.labelColumnExtra()
      );
      return this.cachedLabelColumnWidth;
   }

   private int keybindContentWidth() {
      if (CLIENT.font == null) {
         return 112;
      }

      int keybindWidth = this.labelColumnWidth() + this.rowGap() + this.keybindButtonWidth();
      if (!AutismLiteVariant.enabled()) {
         int styleWidth = this.buttonWidthForLabel("DEFAULT", 47) + 2 + this.buttonWidthForLabel("CSGO", 34);
         keybindWidth = Math.max(keybindWidth, this.labelColumnWidth() + this.rowGap() + styleWidth);
      }

      return keybindWidth;
   }

   private int contentWidth() {
      if (CLIENT.font == null) {
         return 120;
      }

      float fontScale = autismclient.gui.sdf.MsdfTextRenderer.fontScale();
      if (fontScale != this.cachedFontScale) {
         this.cachedFontScale = fontScale;
         this.cachedPanelWidth = 0;
      }

      if (this.cachedPanelWidth > 0) {
         return this.cachedPanelWidth;
      }

      int headerReserve = this.headerButtonSize() * 2 + this.padding() * 2 + 14;
      int titleWidth = autismclient.gui.f.b.k.init(
         CLIENT.font,
         "Settings",
         this.theme.init_Identifier_l(autismclient.gui.f.b.l.init),
         this.theme.load(autismclient.gui.f.b.l.init)
      );
      this.cachedPanelWidth = Math.max(this.minPanelWidth(), Math.max(this.keybindContentWidth() + this.padding() * 2, titleWidth + headerReserve + this.padding()));
      return this.cachedPanelWidth;
   }

   private void setCommandPrefix(String requested) {
      String selected = CommandPrefixHelper.normalizePrefix(requested);
      autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
      if (module != null) {
         module.setCommandPrefix(selected);
      } else {
         this.globalConfig().commandPrefix = selected;
         this.globalConfig().save();
      }
   }

   private void closeDropdown() {
      if (this.openDropdown != null) {
         this.openDropdown.close();
      }

      this.openDropdown = null;
   }

   private int minPanelWidth() {
      return 120;
   }

   private int padding() {
      return 4;
   }

   private int bodyGap() {
      return 0;
   }

   private int bodyMinHeight() {
      return 20;
   }

   private int headerButtonSize() {
      return 10;
   }

   private int clear() {
      return 2;
   }

   private int tooltipMaxWidth() {
      return 170;
   }

   private int start_I_7() {
      return 3;
   }

   private int tooltipOffsetX() {
      return 8;
   }

   private int tooltipOffsetY() {
      return 2;
   }

   private int fallbackPanelWidth() {
      return 82;
   }

   private int minLabelColumnWidth() {
      return 68;
   }

   private int labelColumnExtra() {
      return 0;
   }

   private int rowGap() {
      return 4;
   }

   private int keybindButtonWidth() {
      return 34;
   }

   private int buttonPadding() {
      return 2;
   }

   private int toolbarHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private static int styleButtonHeight() {
      return rowHeight() - 2;
   }

   private static int smallControlHeight() {
      return Math.min(styleButtonHeight(), 11);
   }

   private int buttonWidthForLabel(String label, int fallback) {
      return CLIENT.font == null
         ? fallback
         : autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(
            autismclient.gui.f.b.k.init(
               CLIENT.font,
               label,
               this.theme.init_Identifier_l(autismclient.gui.f.b.l.read),
               this.theme.load(autismclient.gui.f.b.l.read)
            )
         );
   }

   private static int rowHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private final class KeybindRow extends autismclient.gui.ui.layout.LabeledControlRow {
      private final int rowIndex;
      private final AutismKeybindOverlay.KeybindEntry entry;
      private final autismclient.gui.ui.layout.LabelNode labelNode;

      private KeybindRow(int index, AutismKeybindOverlay.KeybindEntry entry) {
         super(
            new autismclient.gui.ui.layout.LabelNode(entry.label, autismclient.gui.f.b.l.read).setTrimToBounds(true),
            new AutismKeybindOverlay.BindButton(
               entry.getter,
               () -> AutismKeybindOverlay.this.listeningEntryIndex == index,
               () -> AutismKeybindOverlay.this.listeningEntryIndex = AutismKeybindOverlay.this.listeningEntryIndex == index ? -1 : index
            )
         );
         this.rowIndex = index;
         this.entry = entry;
         this.nodeHeight = 14.0F;
         this.nodeGrowX = true;
         this.labelNode = (autismclient.gui.ui.layout.LabelNode)this.getLabel();
         this.setLabelWidth(AutismKeybindOverlay.this.labelColumnWidth());
         this.setGap(AutismKeybindOverlay.this.rowGap());
         this.setAlignControlEnd(true);
         this.setPadding(autismclient.gui.ui.layout.EdgeInsets.ZERO);
      }

      @Override
      public float measurePreferredWidth(autismclient.gui.ui.layout.LayoutContext context) {
         return AutismKeybindOverlay.this.windowWidth - AutismKeybindOverlay.this.padding() * 2;
      }

      @Override
      public float measureHeight(autismclient.gui.ui.layout.LayoutContext context, float availableWidth) {
         return AutismKeybindOverlay.rowHeight();
      }

      @Override
      public void render(autismclient.gui.ui.layout.LayoutContext context) {
         this.labelNode.tone(autismclient.gui.f.b.l.read);
         super.render(context);
         if (this.labelNode.containsPoint(context.mouseX(), context.mouseY())) {
            AutismKeybindOverlay.this.hoveredTooltip = this.entry.tooltip;
            AutismKeybindOverlay.this.tooltipX = context.mouseX() + AutismKeybindOverlay.this.tooltipOffsetX();
            AutismKeybindOverlay.this.tooltipY = context.mouseY() - AutismKeybindOverlay.this.tooltipOffsetY();
         }
      }

      @Override
      public boolean mouseClicked(autismclient.gui.ui.layout.LayoutContext context, float mouseX, float mouseY, int button) {
         return super.mouseClicked(context, mouseX, mouseY, button);
      }
   }

   private static class KeybindEntry {
      final String label;
      final String tooltip;
      final IntSupplier getter;
      final IntConsumer setter;

      KeybindEntry(String label, String tooltip, IntSupplier getter, IntConsumer setter) {
         this.label = label;
         this.tooltip = tooltip;
         this.getter = getter;
         this.setter = setter;
      }
   }

   private final class MenuStyleRow extends autismclient.gui.ui.layout.LabeledControlRow {
      private final autismclient.gui.ui.layout.ButtonNode defaultStyleButton;
      private final autismclient.gui.ui.layout.ButtonNode csgoStyleButton;

      private MenuStyleRow() {
         super(
            new autismclient.gui.ui.layout.LabelNode("Menu Style", autismclient.gui.f.b.l.read).setTrimToBounds(true),
            new autismclient.gui.ui.layout.RowLayout().gap(2).padding(autismclient.gui.ui.layout.EdgeInsets.ZERO)
         );
         autismclient.gui.ui.layout.RowLayout choices = (autismclient.gui.ui.layout.RowLayout)this.getControl();
         this.defaultStyleButton = this.styleButton("DEFAULT", "DEFAULT", AutismKeybindOverlay.this.buttonWidthForLabel("DEFAULT", 47));
         this.csgoStyleButton = this.styleButton("CSGO", "CSGO", AutismKeybindOverlay.this.buttonWidthForLabel("CSGO", 34));
         choices.addChild(this.defaultStyleButton);
         choices.addChild(this.csgoStyleButton);
         this.nodeHeight = 14.0F;
         this.nodeGrowX = true;
         this.setLabelWidth(AutismKeybindOverlay.this.labelColumnWidth());
         this.setGap(AutismKeybindOverlay.this.rowGap());
         this.setAlignControlEnd(true);
         this.setPadding(autismclient.gui.ui.layout.EdgeInsets.ZERO);
      }

      private autismclient.gui.ui.layout.ButtonNode styleButton(String label, String style, int width) {
         return new autismclient.gui.ui.layout.ButtonNode(label, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, () -> {
            AutismKeybindOverlay.this.globalConfig().moduleMenuStyle = style;
            AutismKeybindOverlay.this.globalConfig().save();
         }).setPreferredWidth(width).setMinWidth(width).setMaxWidth(width).setHorizontalPadding(5).setFixedHeight(AutismKeybindOverlay.styleButtonHeight());
      }

      @Override
      public float measureHeight(autismclient.gui.ui.layout.LayoutContext context, float availableWidth) {
         return AutismKeybindOverlay.rowHeight();
      }

      @Override
      public void render(autismclient.gui.ui.layout.LayoutContext context) {
         boolean csgo = "CSGO".equals(AutismKeybindOverlay.this.globalConfig().moduleMenuStyle);
         this.defaultStyleButton
            .setVariant(
               csgo ? autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY : autismclient.gui.ui.layout.ButtonNode.Variant.PRIMARY
            );
         this.csgoStyleButton
            .setVariant(
               csgo ? autismclient.gui.ui.layout.ButtonNode.Variant.PRIMARY : autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY
            );
         super.render(context);
         if (this.containsPoint(context.mouseX(), context.mouseY())) {
            AutismKeybindOverlay.this.hoveredTooltip = "DEFAULT: movable category panels. CSGO: one window with a category sidebar. Changes apply immediately.";
            AutismKeybindOverlay.this.tooltipX = context.mouseX() + AutismKeybindOverlay.this.tooltipOffsetX();
            AutismKeybindOverlay.this.tooltipY = context.mouseY() - AutismKeybindOverlay.this.tooltipOffsetY();
         }
      }
   }

   private final class FontScaleRow extends autismclient.gui.ui.layout.LabeledControlRow {
      private static final double[] FONT_SCALES = new double[]{0.75, 0.85, 1.0, 1.15, 1.3};
      private final autismclient.gui.ui.layout.LabelNode labelNode;
      private final autismclient.gui.ui.layout.ButtonNode valueButton;
      private final String tooltip = "Scales the custom UI font only (layout stays on Overlay Scale). Left-click increases, right-click decreases.";

      private FontScaleRow() {
         super(
            new autismclient.gui.ui.layout.LabelNode("Font Scale", autismclient.gui.f.b.l.read).setTrimToBounds(true),
            new autismclient.gui.ui.layout.ButtonNode("1x", autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, null)
               .setPreferredWidth(AutismKeybindOverlay.this.keybindButtonWidth())
               .setMinWidth(AutismKeybindOverlay.this.keybindButtonWidth())
               .setMaxWidth(AutismKeybindOverlay.this.keybindButtonWidth())
               .setHorizontalPadding(AutismKeybindOverlay.this.buttonPadding())
               .setFixedHeight(AutismKeybindOverlay.styleButtonHeight())
               .setConnectedEdges(autismclient.gui.ui.widgets.ConnectedButtonRenderer.init)
               .setTextYOffset(0)
         );
         this.labelNode = (autismclient.gui.ui.layout.LabelNode)this.getLabel();
         this.valueButton = (autismclient.gui.ui.layout.ButtonNode)this.getControl();
         this.valueButton.setOnPress(() -> this.stepFontScale(1));
         this.nodeHeight = 14.0F;
         this.nodeGrowX = true;
         this.setLabelWidth(AutismKeybindOverlay.this.labelColumnWidth());
         this.setGap(AutismKeybindOverlay.this.rowGap());
         this.setAlignControlEnd(true);
         this.setPadding(autismclient.gui.ui.layout.EdgeInsets.ZERO);
      }

      private void stepFontScale(int direction) {
         double current = AutismKeybindOverlay.this.globalConfig().uiFontScale;
         int index = 0;
         double best = 1.7976931348623157E308;

         for (int i = 0; i < FONT_SCALES.length; i++) {
            double distance = Math.abs(FONT_SCALES[i] - current);
            if (distance < best) {
               best = distance;
               index = i;
            }
         }

         index = Math.floorMod(index + direction, FONT_SCALES.length);
         AutismKeybindOverlay.this.globalConfig().uiFontScale = FONT_SCALES[index];
         AutismKeybindOverlay.this.globalConfig().save();
         AutismKeybindOverlay.this.layoutHost.invalidateLayout();
      }

      @Override
      public float measurePreferredWidth(autismclient.gui.ui.layout.LayoutContext context) {
         return AutismKeybindOverlay.this.windowWidth - AutismKeybindOverlay.this.padding() * 2;
      }

      @Override
      public float measureHeight(autismclient.gui.ui.layout.LayoutContext context, float availableWidth) {
         return AutismKeybindOverlay.rowHeight();
      }

      @Override
      public void render(autismclient.gui.ui.layout.LayoutContext context) {
         double scale = AutismKeybindOverlay.this.globalConfig().uiFontScale;
         this.valueButton.setText(scale == 1.0 ? "1x" : String.format(Locale.ROOT, "%.2fx", scale).replaceAll("0+x$", "x"));
         this.valueButton
            .setVariant(
               scale != 1.0 ? autismclient.gui.ui.layout.ButtonNode.Variant.PRIMARY : autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY
            );
         super.render(context);
         if (this.labelNode.containsPoint(context.mouseX(), context.mouseY())) {
            AutismKeybindOverlay.this.hoveredTooltip = "Scales the custom UI font only (layout stays on Overlay Scale). Left-click increases, right-click decreases.";
            AutismKeybindOverlay.this.tooltipX = context.mouseX() + AutismKeybindOverlay.this.tooltipOffsetX();
            AutismKeybindOverlay.this.tooltipY = context.mouseY() - AutismKeybindOverlay.this.tooltipOffsetY();
         }
      }

      @Override
      public boolean mouseClicked(autismclient.gui.ui.layout.LayoutContext context, float mouseX, float mouseY, int button) {
         if ((button == 0 || button == 1) && this.valueButton.containsPoint(mouseX, mouseY)) {
            this.stepFontScale(button == 1 ? -1 : 1);
            return true;
         } else {
            return super.mouseClicked(context, mouseX, mouseY, button);
         }
      }
   }

   private final class ToggleRow extends autismclient.gui.ui.layout.LabeledControlRow {
      private final BooleanSupplier getter;
      private final Consumer<Boolean> setter;
      private final String tooltip;
      private final autismclient.gui.ui.layout.LabelNode labelNode;
      private final autismclient.gui.ui.layout.ButtonNode toggleButton;

      private ToggleRow(String label, String tooltip, BooleanSupplier getter, Consumer<Boolean> setter) {
         super(
            new autismclient.gui.ui.layout.LabelNode(label, autismclient.gui.f.b.l.read).setTrimToBounds(true),
            new autismclient.gui.ui.layout.ButtonNode("OFF", autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, null)
               .setConnectedToggle(true)
               .setPreferredWidth(22.0F)
               .setMinWidth(22.0F)
               .setMaxWidth(22.0F)
               .setHorizontalPadding(AutismKeybindOverlay.this.buttonPadding())
               .setFixedHeight(AutismKeybindOverlay.smallControlHeight())
               .setConnectedEdges(autismclient.gui.ui.widgets.ConnectedButtonRenderer.init)
               .setTextYOffset(0)
         );
         this.getter = getter;
         this.setter = setter;
         this.tooltip = tooltip;
         this.labelNode = (autismclient.gui.ui.layout.LabelNode)this.getLabel();
         this.toggleButton = (autismclient.gui.ui.layout.ButtonNode)this.getControl();
         this.toggleButton.setOnPress(() -> {
            setter.accept(!getter.getAsBoolean());
            AutismKeybindOverlay.this.globalConfig().save();
         });
         this.nodeHeight = 14.0F;
         this.nodeGrowX = true;
         this.setLabelWidth(AutismKeybindOverlay.this.labelColumnWidth());
         this.setGap(AutismKeybindOverlay.this.rowGap());
         this.setAlignControlEnd(true);
         this.setPadding(autismclient.gui.ui.layout.EdgeInsets.ZERO);
      }

      @Override
      public float measurePreferredWidth(autismclient.gui.ui.layout.LayoutContext context) {
         return AutismKeybindOverlay.this.windowWidth - AutismKeybindOverlay.this.padding() * 2;
      }

      @Override
      public float measureHeight(autismclient.gui.ui.layout.LayoutContext context, float availableWidth) {
         return AutismKeybindOverlay.rowHeight();
      }

      @Override
      public void render(autismclient.gui.ui.layout.LayoutContext context) {
         boolean on = this.getter.getAsBoolean();
         this.labelNode.render(context);
         autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(
            context.graphics(), context.font(), Math.round(context.mouseX()), Math.round(context.mouseY())
         );
         autismclient.gui.f.NetHelper pillBounds = autismclient.gui.f.NetHelper.init(
            Math.round(this.toggleButton.x()),
            Math.round(this.toggleButton.y()),
            Math.round(this.toggleButton.width()),
            Math.round(this.toggleButton.height())
         );
         ToggleSwitchRenderer.draw(ui, pillBounds, on, pillBounds.init_Z(ui.read(), ui.write()), "keybind:" + this.tooltip);
         if (this.labelNode.containsPoint(context.mouseX(), context.mouseY())) {
            AutismKeybindOverlay.this.hoveredTooltip = this.tooltip;
            AutismKeybindOverlay.this.tooltipX = context.mouseX() + AutismKeybindOverlay.this.tooltipOffsetX();
            AutismKeybindOverlay.this.tooltipY = context.mouseY() - AutismKeybindOverlay.this.tooltipOffsetY();
         }
      }
   }

   private final class OverlayScaleRow extends autismclient.gui.ui.layout.LabeledControlRow {
      private final autismclient.gui.ui.layout.LabelNode labelNode;
      private final autismclient.gui.ui.layout.ButtonNode valueButton;
      private final String tooltip = "Scales every Autism overlay/window. Left-click increases, right-click decreases. 1x is default; 1.5x/2x are for 4K/high-DPI screens.";

      private OverlayScaleRow() {
         super(
            new autismclient.gui.ui.layout.LabelNode("Overlay Scale", autismclient.gui.f.b.l.read).setTrimToBounds(true),
            new autismclient.gui.ui.layout.ButtonNode("1x", autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, null)
               .setPreferredWidth(AutismKeybindOverlay.this.keybindButtonWidth())
               .setMinWidth(AutismKeybindOverlay.this.keybindButtonWidth())
               .setMaxWidth(AutismKeybindOverlay.this.keybindButtonWidth())
               .setHorizontalPadding(AutismKeybindOverlay.this.buttonPadding())
               .setFixedHeight(AutismKeybindOverlay.styleButtonHeight())
               .setConnectedEdges(autismclient.gui.ui.widgets.ConnectedButtonRenderer.init)
               .setTextYOffset(0)
         );
         this.labelNode = (autismclient.gui.ui.layout.LabelNode)this.getLabel();
         this.valueButton = (autismclient.gui.ui.layout.ButtonNode)this.getControl();
         this.valueButton.setOnPress(() -> {
            cA.init_V_D(cA.load_D());
            AutismKeybindOverlay.this.layoutHost.invalidateLayout();
         });
         this.nodeHeight = 14.0F;
         this.nodeGrowX = true;
         this.setLabelWidth(AutismKeybindOverlay.this.labelColumnWidth());
         this.setGap(AutismKeybindOverlay.this.rowGap());
         this.setAlignControlEnd(true);
         this.setPadding(autismclient.gui.ui.layout.EdgeInsets.ZERO);
      }

      @Override
      public float measurePreferredWidth(autismclient.gui.ui.layout.LayoutContext context) {
         return AutismKeybindOverlay.this.windowWidth - AutismKeybindOverlay.this.padding() * 2;
      }

      @Override
      public float measureHeight(autismclient.gui.ui.layout.LayoutContext context, float availableWidth) {
         return AutismKeybindOverlay.rowHeight();
      }

      @Override
      public void render(autismclient.gui.ui.layout.LayoutContext context) {
         double scale = cA.init_D();
         this.valueButton.setText(cA.init_Str_D(scale));
         this.valueButton
            .setVariant(
               scale > 1.0 ? autismclient.gui.ui.layout.ButtonNode.Variant.PRIMARY : autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY
            );
         super.render(context);
         if (this.labelNode.containsPoint(context.mouseX(), context.mouseY())) {
            AutismKeybindOverlay.this.hoveredTooltip = "Scales every Autism overlay/window. Left-click increases, right-click decreases. 1x is default; 1.5x/2x are for 4K/high-DPI screens.";
            AutismKeybindOverlay.this.tooltipX = context.mouseX() + AutismKeybindOverlay.this.tooltipOffsetX();
            AutismKeybindOverlay.this.tooltipY = context.mouseY() - AutismKeybindOverlay.this.tooltipOffsetY();
         }
      }

      @Override
      public boolean mouseClicked(autismclient.gui.ui.layout.LayoutContext context, float mouseX, float mouseY, int button) {
         if ((button == 0 || button == 1) && this.valueButton.containsPoint(mouseX, mouseY)) {
            cA.init_V_D(button == 1 ? cA.read() : cA.load_D());
            AutismKeybindOverlay.this.layoutHost.invalidateLayout();
            return true;
         } else {
            return super.mouseClicked(context, mouseX, mouseY, button);
         }
      }
   }

   private static final class BindButton extends autismclient.gui.ui.layout.LayoutNode {
      private final IntSupplier bindCode;
      private final BooleanSupplier capturing;
      private final Runnable onPress;

      private BindButton(IntSupplier bindCode, BooleanSupplier capturing, Runnable onPress) {
         this.bindCode = bindCode;
         this.capturing = capturing;
         this.onPress = onPress;
         this.nodeWidth = 34.0F;
         this.nodeHeight = AutismKeybindOverlay.styleButtonHeight();
      }

      @Override
      public float measurePreferredWidth(autismclient.gui.ui.layout.LayoutContext context) {
         return 34.0F;
      }

      @Override
      public float measureHeight(autismclient.gui.ui.layout.LayoutContext context, float availableWidth) {
         return AutismKeybindOverlay.styleButtonHeight();
      }

      @Override
      public void render(autismclient.gui.ui.layout.LayoutContext context) {
         if (this.nodeVisible) {
            autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(
               Math.round(this.nodeX), Math.round(this.nodeY), Math.round(this.nodeWidth), Math.round(this.nodeHeight)
            );
            boolean hovered = this.nodeEnabled && this.containsPoint(context.mouseX(), context.mouseY());
            autismclient.gui.ui.widgets.KeybindButtonRenderer.draw(
               autismclient.gui.f.UiHelper.init(
                  context.graphics(), context.font(), Math.round(context.mouseX()), Math.round(context.mouseY())
               ),
               bounds,
               this.bindCode.getAsInt(),
               this.capturing.getAsBoolean(),
               hovered
            );
         }
      }

      @Override
      public boolean mouseClicked(autismclient.gui.ui.layout.LayoutContext context, float mouseX, float mouseY, int button) {
         if (this.nodeEnabled && this.containsPoint(mouseX, mouseY) && button == 0) {
            if (this.onPress != null) {
               this.onPress.run();
            }

            return true;
         } else {
            return false;
         }
      }
   }

   private final class CommandPrefixRow extends autismclient.gui.ui.layout.LayoutNode {
      private autismclient.gui.f.NetHelper controlBounds = autismclient.gui.f.NetHelper.init(0, 0, 1, 1);

      private CommandPrefixRow() {
         this.nodeHeight = 14.0F;
         this.nodeGrowX = true;
      }

      @Override
      public float measurePreferredWidth(autismclient.gui.ui.layout.LayoutContext context) {
         return AutismKeybindOverlay.this.windowWidth - AutismKeybindOverlay.this.padding() * 2;
      }

      @Override
      public float measureHeight(autismclient.gui.ui.layout.LayoutContext context, float availableWidth) {
         return AutismKeybindOverlay.rowHeight();
      }

      @Override
      public void render(autismclient.gui.ui.layout.LayoutContext context) {
         int rowX = Math.round(this.nodeX);
         int rowY = Math.round(this.nodeY);
         int rowW = Math.round(this.nodeWidth);
         int rowH = Math.round(this.nodeHeight);
         int controlW = AutismKeybindOverlay.this.keybindButtonWidth();
         int controlH = Math.min(rowH, AutismKeybindOverlay.styleButtonHeight());
         this.controlBounds = autismclient.gui.f.NetHelper.init(rowX + Math.max(0, rowW - controlW), rowY + (rowH - controlH) / 2, controlW, controlH);
         autismclient.gui.f.b.k.init(
            context.graphics(),
            context.font(),
            "Command Prefix",
            AutismKeybindOverlay.this.theme.init_Identifier_l(autismclient.gui.f.b.l.read),
            AutismKeybindOverlay.this.theme.load(autismclient.gui.f.b.l.read),
            rowX,
            UiLayoutMath.centerTextY(
               rowY, rowH, AutismKeybindOverlay.this.theme.init_I_l(autismclient.gui.f.b.l.read), AutismKeybindOverlay.this.theme.flush()
            ),
            Math.max(1, rowW - controlW - AutismKeybindOverlay.this.rowGap()),
            false
         );
         String value = CommandPrefixHelper.currentPrefix();
         autismclient.gui.ui.widgets.WidgetRenderFacade.dropdown(
            autismclient.gui.f.UiHelper.init(context.graphics(), context.font(), Math.round(context.mouseX()), Math.round(context.mouseY())),
            this.controlBounds,
            value,
            this.controlBounds.init_Z(Math.round(context.mouseX()), Math.round(context.mouseY())),
            AutismKeybindOverlay.this.openDropdown != null && AutismKeybindOverlay.this.openDropdown.isOpen()
         );
      }

      @Override
      public boolean mouseClicked(autismclient.gui.ui.layout.LayoutContext context, float mouseX, float mouseY, int button) {
         if (button == 0 && this.controlBounds.init_Z(Math.round(mouseX), Math.round(mouseY))) {
            AutismKeybindOverlay.this.openDropdown = new autismclient.gui.ui.widgets.DropdownMenu(
               this.controlBounds, CommandPrefixHelper.ALLOWED_PREFIXES, CommandPrefixHelper.currentPrefix(), AutismKeybindOverlay.this::setCommandPrefix
            );
            AutismKeybindOverlay.this.openDropdown.open();
            return true;
         } else {
            return false;
         }
      }
   }
}
