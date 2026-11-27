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
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final String init_Str_2 = "Settings";
   private static final String load_Str = "Fire keybinds while a container screen is open. Blocked in chat/sign/book editors and focused text fields.";
   private static final String read_Str = "Use our custom main menu. Turn off for the normal vanilla/Fabric title screen (mods stay visible).";
   private static final int flush_I = 120;
   private static final int close_I = 14;
   private static final int open = 4;
   private static final int poll = 10;
   private static final int push = 4;
   private static final int pop = 22;
   private static final int tick = 47;
   private static final int run = 34;
   private static final float init_F_2 = 3.0F;
   private final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private final autismclient.gui.ui.layout.CollapsibleWindowNode init_CharEncoder_2 = new autismclient.gui.ui.layout.CollapsibleWindowNode("Settings").setScrollable(true);
   private final autismclient.gui.ui.layout.LayoutTreeHost init_TextFormat = new autismclient.gui.ui.layout.LayoutTreeHost(this.init_CharEncoder, this.init_CharEncoder_2);
   private final List<AutismKeybindOverlay.Inner_CachePool> init_List = new ArrayList<>();
   private final List<AutismKeybindOverlay.Inner_BufferHelper> load_List = new ArrayList<>();
   private boolean write_Z = false;
   private float load_F;
   private float read_F;
   private float write_F;
   private float update_F;
   private int get;
   private int set;
   private boolean update_Z = false;
   private int put = -1;
   private String write_Str = null;
   private float sync_F;
   private float flush_F;
   private int add = -1;
   private int clear = -1;
   private float close_F = 0.0F / 0.0F;
   private boolean sync_Z;
   private int reset;
   private autismclient.gui.ui.widgets.DropdownMenu init_RefQueue;

   public AutismKeybindOverlay() {
      super("autism-keybinds", 120, 142);
      this.windowX = 160;
      this.windowY = 46;
      if (!AutismLiteVariant.enabled()) {
         this.init_List
            .add(
               new AutismKeybindOverlay.Inner_CachePool(
                  "Module Menu",
                  "Opens the Autism module click GUI",
                  () -> this.init_AutismConfig().keybindModuleMenu,
                  v -> this.init_AutismConfig().keybindModuleMenu = v
               )
            );
         this.init_List
            .add(
               new AutismKeybindOverlay.Inner_CachePool(
                  "AI Assistant",
                  "Opens the AI Assistant utility window",
                  () -> this.init_AutismConfig().keybindAiAssistant,
                  v -> this.init_AutismConfig().keybindAiAssistant = v
               )
            );
      }

      this.init_List
         .add(
            new AutismKeybindOverlay.Inner_CachePool(
               "Load GUI",
               "Restores the last saved GUI screen and handler",
               () -> this.init_AutismConfig().keybindLoadGui,
               v -> this.init_AutismConfig().keybindLoadGui = v
            )
         );
      this.init_List
         .add(
            new AutismKeybindOverlay.Inner_CachePool(
               "Flush Queue",
               "Sends all queued packets to the server",
               () -> this.init_AutismConfig().keybindFlushQueue,
               v -> this.init_AutismConfig().keybindFlushQueue = v
            )
         );
      this.init_List
         .add(
            new AutismKeybindOverlay.Inner_CachePool(
               "Clear Queue",
               "Discards all queued packets",
               () -> this.init_AutismConfig().keybindClearQueue,
               v -> this.init_AutismConfig().keybindClearQueue = v
            )
         );
      this.init_List
         .add(
            new AutismKeybindOverlay.Inner_CachePool(
               "Toggle Logger",
               "Shows or hides the packet logger overlay",
               () -> this.init_AutismConfig().keybindToggleLogger,
               v -> this.init_AutismConfig().keybindToggleLogger = v
            )
         );
      this.init_List
         .add(
            new AutismKeybindOverlay.Inner_CachePool(
               "Toggle Send",
               "Turns packet sending on or off",
               () -> this.init_AutismConfig().keybindToggleSend,
               v -> this.init_AutismConfig().keybindToggleSend = v
            )
         );
      this.init_List
         .add(
            new AutismKeybindOverlay.Inner_CachePool(
               "Toggle Delay",
               "Turns packet delay on or off",
               () -> this.init_AutismConfig().keybindToggleDelay,
               v -> this.init_AutismConfig().keybindToggleDelay = v
            )
         );
      this.write_V_9();
   }

   private void write_V_9() {
      this.init_CharEncoder_2.setCenterTitle(false);
      this.init_CharEncoder_2.setTitleTone(autismclient.gui.f.b.l.load);
      this.init_CharEncoder_2.setHeaderButtons(true, true);
      this.init_CharEncoder_2.setTitleInsets(this.get_I_7() + 2, this.get_I_7() + this.add_I_7() * 2 + 8);
      this.init_CharEncoder_2.body().clearChildren();
      this.init_CharEncoder_2.body().init(this.set_I_7()).init(autismclient.gui.ui.layout.EdgeInsets.uniform(this.get_I_7()));
      this.init_CharEncoder_2
         .body()
         .addChild(
            new AutismKeybindOverlay.Inner_JsonUtil(
               "Inside GUI",
               "Fire keybinds while a container screen is open. Blocked in chat/sign/book editors and focused text fields.",
               () -> this.init_AutismConfig().keybindInsideGui,
               v -> this.init_AutismConfig().keybindInsideGui = v
            )
         );
      if (!AutismLiteVariant.enabled()) {
         this.init_CharEncoder_2
            .body()
            .addChild(
               new AutismKeybindOverlay.Inner_JsonUtil(
                  "Custom Menu",
                  "Use our custom main menu. Turn off for the normal vanilla/Fabric title screen (mods stay visible).",
                  () -> this.init_AutismConfig().customMainMenu,
                  v -> {
                     AutismConfig config = this.init_AutismConfig();
                     config.customMainMenu = v;
                     config.save();
                  }
               )
            );
      }

      this.init_CharEncoder_2.body().addChild(new AutismKeybindOverlay.Inner_NetHelper());
      this.init_CharEncoder_2.body().addChild(new AutismKeybindOverlay.Inner_DataUtil());
      if (!AutismLiteVariant.enabled()) {
         this.init_CharEncoder_2.body().addChild(new AutismKeybindOverlay.Inner_ConfigStore());
      }

      this.init_CharEncoder_2
         .body()
         .addChild(
            new AutismKeybindOverlay.Inner_JsonUtil("Auto Probe", "Auto-Scans on join.", () -> this.init_AutismConfig().autoProbePlugins, v -> {
               AutismConfig config = this.init_AutismConfig();
               config.autoProbePlugins = v;
               config.save();
            })
         );
      this.init_CharEncoder_2
         .body()
         .addChild(
            new AutismKeybindOverlay.Inner_JsonUtil("InfiniChat", "Remove chat length limit.", () -> this.init_AutismConfig().infiniChat, v -> {
               AutismConfig config = this.init_AutismConfig();
               config.infiniChat = v;
               config.save();
            })
         );
      this.init_CharEncoder_2
         .body()
         .addChild(
            new AutismKeybindOverlay.Inner_JsonUtil(
               "Chat Feedback", "Show module enable/disable messages in chat.", () -> this.init_AutismConfig().chatFeedback, v -> {
                  AutismConfig config = this.init_AutismConfig();
                  config.chatFeedback = v;
                  config.save();
               }
            )
         );
      this.init_CharEncoder_2
         .body()
         .addChild(
            new AutismKeybindOverlay.Inner_JsonUtil("Stop On Leave", "Stop macros on disconnect.", () -> this.init_AutismConfig().stopMacroOnLeave, v -> {
               AutismConfig config = this.init_AutismConfig();
               config.stopMacroOnLeave = v;
               config.save();
            })
         );
      this.init_CharEncoder_2.body().addChild(new AutismKeybindOverlay.Inner_TaskQueue());
      this.load_List.clear();

      for (int i = 0; i < this.init_List.size(); i++) {
         AutismKeybindOverlay.Inner_BufferHelper row = new AutismKeybindOverlay.Inner_BufferHelper(i, this.init_List.get(i));
         this.load_List.add(row);
         this.init_CharEncoder_2.body().addChild(row);
      }

      if (!AutismLiteVariant.enabled()) {
         autismclient.gui.ui.layout.RowLayout settingsPages = new autismclient.gui.ui.layout.RowLayout()
            .init(0)
            .init(autismclient.gui.ui.layout.EdgeInsets.ZERO)
            .setUnderlineOnHover(false);
         settingsPages.setGrowX(true);
         settingsPages.addChild(
            new autismclient.gui.ui.layout.ButtonNode("Theme Color", autismclient.gui.ui.layout.ButtonNode.Variant.PRIMARY, this::update_V_7)
               .withGrowX(true)
               .setConnectedStyle(true)
               .setConnectedEdges(autismclient.gui.ui.widgets.ConnectedButtonRenderer.load)
               .setFixedHeight(this.state())
         );
         settingsPages.addChild(
            new autismclient.gui.ui.layout.ButtonNode("Profiles", autismclient.gui.ui.layout.ButtonNode.Variant.PRIMARY, this::sync_V_6)
               .withGrowX(true)
               .setConnectedStyle(true)
               .setConnectedEdges(autismclient.gui.ui.widgets.ConnectedButtonRenderer.read)
               .setFixedHeight(this.state())
         );
         this.init_CharEncoder_2.body().addChild(settingsPages);
      }
   }

   private void update_V_7() {
      init_Minecraft.gui.setScreen(new autismclient.gui.screens.ThemeColorScreen(init_Minecraft.gui.screen()));
   }

   private void sync_V_6() {
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

   private AutismConfig init_AutismConfig() {
      return AutismConfig.getGlobal();
   }

   @Override
   public int getMinWidth() {
      return this.tick_I_9();
   }

   @Override
   public int getMinHeight() {
      return this.init_CharEncoder.load() + this.put();
   }

   @Override
   public boolean isResizable() {
      return true;
   }

   @Override
   protected int getResizeMinHeight() {
      return this.init_CharEncoder.load() + this.get_I_7() * 2 + 4 * (autismclient.gui.ui.UiDensityMetrics.scaledHeight(14) + 2);
   }

   @Override
   public OverlayBounds getBounds() {
      return new OverlayBounds(this.windowX, this.windowY, this.windowWidth, this.windowHeight, this.windowVisible, this.windowCollapsed);
   }

   @Override
   public void setBounds(OverlayBounds bounds) {
      OverlayBounds clamped = this.init_cH_cH(bounds);
      this.windowX = clamped.x;
      this.windowY = clamped.y;
      this.windowWidth = clamped.width;
      this.windowHeight = clamped.height;
      this.windowVisible = clamped.visible;
      this.windowCollapsed = clamped.collapsed;
      this.init_CharEncoder_2.setShowBody(!this.windowCollapsed);
   }

   @Override
   public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      if (this.windowVisible || this.isClosing()) {
         this.write_Str = null;
         autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.createViewport();
         float uiMouseX = viewport.toUiX(mouseX);
         float uiMouseY = viewport.toUiY(mouseY);
         int contentWidth = this.tick_I_9();
         this.windowWidth = this.resolveWindowWidth(contentWidth, contentWidth);
         boolean active = OverlayManager.getInstance().isFocused(this) || OverlayManager.getInstance().isTopmost(this);
         boolean headerHovered = uiMouseX >= this.windowX
            && uiMouseX < this.windowX + this.windowWidth
            && uiMouseY >= this.windowY
            && uiMouseY < this.windowY + this.init_CharEncoder.load();
         autismclient.gui.ui.layout.LayoutContext metrics = new autismclient.gui.ui.layout.LayoutContext(
            context, init_Minecraft.font, viewport, this.init_CharEncoder, uiMouseX, uiMouseY, delta
         );
         this.init_CharEncoder_2.setExpanded(!this.windowCollapsed);
         this.init_CharEncoder_2.setActive(active);
         this.init_CharEncoder_2.setHeaderHovered(headerHovered);
         int contentHeight = Math.max(this.init_CharEncoder.load(), Math.round(this.init_CharEncoder_2.measureHeight(metrics, this.windowWidth)));
         this.windowHeight = this.windowCollapsed ? contentHeight : Math.min(contentHeight, this.resolveWindowHeight(contentHeight, this.getResizeMinHeight()));
         OverlayBounds clamped = this.init_cH_cH(new OverlayBounds(this.windowX, this.windowY, this.windowWidth, this.windowHeight, this.windowVisible, this.windowCollapsed));
         if (!this.windowCollapsed && clamped.height < contentHeight && this.windowWidth < contentWidth + 6) {
            this.windowWidth = contentWidth + 6;
            clamped = this.init_cH_cH(new OverlayBounds(this.windowX, this.windowY, this.windowWidth, this.windowHeight, this.windowVisible, this.windowCollapsed));
         }

         this.windowX = clamped.x;
         this.windowY = clamped.y;
         this.windowWidth = clamped.width;
         this.windowHeight = clamped.height;
         this.init_CharEncoder_2.setBounds(this.windowX, this.windowY, this.windowWidth, this.windowHeight);
         this.init_TextFormat.render(context, mouseX, mouseY, delta);
         if (!this.windowCollapsed && this.init_RefQueue != null && this.init_RefQueue.isOpen()) {
            context.nextStratum();
            viewport.pushTransform(context);

            try {
               this.init_RefQueue
                  .render(autismclient.gui.f.UiHelper.init(context, init_Minecraft.font, Math.round(uiMouseX), Math.round(uiMouseY)));
            } finally {
               viewport.popTransform(context);
            }
         }

         if (!this.windowCollapsed && this.write_Str != null) {
            context.nextStratum();
            this.init_V_GuiGraphicsExtract_2(context, viewport, this.write_Str, this.sync_F, this.flush_F);
         }
      }
   }

   private void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor context, autismclient.gui.ui.layout.LayoutViewport viewport, String text, float uiX, float uiY) {
      viewport.pushTransform(context);

      try {
         TooltipRenderer.init(
            autismclient.gui.f.UiHelper.init(context, init_Minecraft.font, Math.round(uiX), Math.round(uiY)),
            text,
            Math.round(uiX),
            Math.round(uiY),
            this.reset()
         );
      } finally {
         viewport.popTransform(context);
      }
   }

   public static String init_Str_I_3(int keyCode) {
      return ValueSink.init_Str(keyCode);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.windowVisible) {
         return false;
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.createViewport();
      float uiMouseX = viewport.toUiX(mouseX);
      float uiMouseY = viewport.toUiY(mouseY);
      if (this.init_RefQueue != null && this.init_RefQueue.isOpen()) {
         autismclient.gui.ui.UiInputResult result = this.init_RefQueue.onMouseClicked(Math.round(uiMouseX), Math.round(uiMouseY), button);
         if (!this.init_RefQueue.isOpen()) {
            this.init_RefQueue = null;
         }

         return result == autismclient.gui.ui.UiInputResult.HANDLED;
      } else if (this.read_Z_F_F(uiMouseX, uiMouseY)) {
         this.windowVisible = false;
         this.put = -1;
         this.write_Z = false;
         this.update_Z = false;
         return true;
      } else if (button == 0 && this.init_Z_F_F_2(uiMouseX, uiMouseY)) {
         this.write_Z = true;
         this.update_Z = false;
         this.load_F = uiMouseX - this.windowX;
         this.read_F = uiMouseY - this.windowY;
         this.write_F = uiMouseX;
         this.update_F = uiMouseY;
         this.get = this.windowX;
         this.set = this.windowY;
         return true;
      } else if (this.windowCollapsed) {
         return this.containsPoint(mouseX, mouseY);
      } else if (this.put >= 0 && ValueSink.load_Z(button)) {
         this.init_V_I_4(ValueSink.init_I(button));
         return true;
      } else {
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics bodyBar = this.init_CharEncoder_2.scrollbarMetrics();
         if (button == 0 && bodyBar != null && bodyBar.containsTrack(uiMouseX, uiMouseY)) {
            this.sync_Z = true;
            this.init_CharEncoder_2.setScrollbarDragging(true);
            this.reset = bodyBar.containsThumb(uiMouseX, uiMouseY) ? Math.round(uiMouseY) - bodyBar.thumbY() : bodyBar.thumbHeight() / 2;
            this.init_CharEncoder_2.setScrollOffset(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(bodyBar, uiMouseY, this.reset));
            return true;
         } else {
            return this.init_TextFormat.mouseClicked(mouseX, mouseY, button) ? true : this.containsPoint(mouseX, mouseY);
         }
      }
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (!this.windowVisible || this.windowCollapsed) {
         return false;
      } else if (this.init_RefQueue != null && this.init_RefQueue.isOpen() && keyCode == 256) {
         this.init_RefQueue.close();
         this.init_RefQueue = null;
         return true;
      } else if (this.init_TextFormat.keyPressed(keyCode, scanCode, modifiers)) {
         return true;
      } else if (this.windowVisible && this.put >= 0) {
         this.init_V_I_4(autismclient.gui.ui.widgets.KeybindButtonRenderer.init(keyCode));
         return true;
      } else {
         return false;
      }
   }

   private void init_V_I_4(int bindCode) {
      for (int i = 0; i < this.init_List.size(); i++) {
         if (i != this.put && this.init_List.get(i).init_IntSupplier.getAsInt() == bindCode) {
            this.init_List.get(i).init_IntConsumer.accept(-1);
         }
      }

      this.init_List.get(this.put).init_IntConsumer.accept(bindCode);
      this.init_AutismConfig().save();
      this.put = -1;
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (this.init_RefQueue != null && this.init_RefQueue.isOpen()) {
         autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.createViewport();
         return this.init_RefQueue.onMouseReleased(Math.round(viewport.toUiX(mouseX)), Math.round(viewport.toUiY(mouseY)), button) == autismclient.gui.ui.UiInputResult.HANDLED;
      } else if (this.write_Z) {
         this.write_Z = false;
         this.saveBounds();
         return true;
      } else if (this.sync_Z) {
         this.sync_Z = false;
         this.init_CharEncoder_2.setScrollbarDragging(false);
         return true;
      } else {
         return this.windowCollapsed ? false : this.init_TextFormat.mouseReleased(mouseX, mouseY, button);
      }
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.init_RefQueue != null && this.init_RefQueue.isOpen()) {
         autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.createViewport();
         return this.init_RefQueue.onMouseDragged(Math.round(viewport.toUiX(mouseX)), Math.round(viewport.toUiY(mouseY)), button, deltaX, deltaY)
            == autismclient.gui.ui.UiInputResult.HANDLED;
      }

      if (!this.write_Z) {
         if (this.sync_Z) {
            autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics bodyBar = this.init_CharEncoder_2.scrollbarMetrics();
            if (bodyBar != null) {
               this.init_CharEncoder_2.setScrollOffset(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(bodyBar, this.init_TextFormat.createViewport().toUiY(mouseY), this.reset));
            }

            return true;
         } else {
            return this.windowCollapsed ? false : this.init_TextFormat.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
         }
      } else {
         autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.createViewport();
         float uiMouseX = viewport.toUiX(mouseX);
         float uiMouseY = viewport.toUiY(mouseY);
         OverlayBounds clamped = this.init_cH_cH(
            new OverlayBounds(Math.round(uiMouseX - this.load_F), Math.round(uiMouseY - this.read_F), this.windowWidth, this.windowHeight, this.windowVisible, this.windowCollapsed)
         );
         this.windowX = clamped.x;
         this.windowY = clamped.y;
         this.update_Z = this.update_Z
            || Math.abs(uiMouseX - this.write_F) >= 3.0F
            || Math.abs(uiMouseY - this.update_F) >= 3.0F
            || this.windowX != this.get
            || this.windowY != this.set;
         return true;
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      if (this.windowCollapsed) {
         return false;
      } else if (this.init_RefQueue != null && this.init_RefQueue.isOpen()) {
         autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.createViewport();
         return this.init_RefQueue.onMouseScrolled(Math.round(viewport.toUiX(mouseX)), Math.round(viewport.toUiY(mouseY)), amount)
            == autismclient.gui.ui.UiInputResult.HANDLED;
      } else if (this.init_TextFormat.mouseScrolled(mouseX, mouseY, amount)) {
         return true;
      } else if (this.init_CharEncoder_2.maxScroll() > 0.0F && this.containsPoint(mouseX, mouseY)) {
         this.init_CharEncoder_2.setScrollOffset(this.init_CharEncoder_2.scrollOffset() - (float)amount * (autismclient.gui.ui.UiDensityMetrics.scaledHeight(14) + 2));
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean charTyped(char chr, int modifiers) {
      return this.windowVisible && !this.windowCollapsed ? this.init_TextFormat.charTyped(chr, modifiers) : false;
   }

   @Override
   public boolean isVisible() {
      return this.windowVisible;
   }

   @Override
   public void setVisible(boolean v) {
      this.windowVisible = v;
      if (!v) {
         this.put = -1;
         this.write_Z = false;
         this.update_Z = false;
         this.flush_V_6();
      } else {
         this.init_CharEncoder_2.setShowBody(!this.windowCollapsed);
         OverlayManager.getInstance().bringToFront(this);
      }
   }

   public void load_V_9() {
      this.setVisible(!this.windowVisible);
   }

   @Override
   public boolean containsPoint(double mouseX, double mouseY) {
      if (!this.windowVisible) {
         return false;
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.createViewport();
      float uiX = viewport.toUiX(mouseX);
      float uiY = viewport.toUiY(mouseY);
      return uiX >= this.windowX && uiX < this.windowX + this.windowWidth && uiY >= this.windowY && uiY < this.windowY + this.windowHeight;
   }

   @Override
   public boolean isOverHeader(double mouseX, double mouseY) {
      if (!this.windowVisible) {
         return false;
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.createViewport();
      return this.init_Z_F_F_2(viewport.toUiX(mouseX), viewport.toUiY(mouseY));
   }

   private boolean init_Z_F_F_2(float uiMouseX, float uiMouseY) {
      return autismclient.gui.ui.widgets.WindowHeaderHelper.init(this.init_NetHelper_3(), this.init_CharEncoder.load(), false, true, uiMouseX, uiMouseY);
   }

   private boolean load_Z_F_F_2(float uiMouseX, float uiMouseY) {
      return autismclient.gui.ui.widgets.WindowHeaderHelper.read(this.init_NetHelper_3(), this.init_CharEncoder.load(), uiMouseX, uiMouseY);
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
         this.init_CharEncoder_2.setShowBody(!this.windowCollapsed);
         if (c) {
            this.put = -1;
            this.flush_V_6();
            this.clearFocusAndDismissMenu();
         }

         this.saveBounds();
      }
   }

   @Override
   public boolean hasFocusedTextInput() {
      return this.init_TextFormat.hasFocusedInput();
   }

   @Override
   public boolean isCapturingKeybind() {
      return this.windowVisible && this.put >= 0;
   }

   @Override
   public void clearInputFocus() {
      this.flush_V_6();
      this.init_TextFormat.clearFocus();
   }

   private boolean read_Z_F_F(float uiMouseX, float uiMouseY) {
      return autismclient.gui.ui.widgets.WindowHeaderHelper.init(this.init_NetHelper_3(), this.init_CharEncoder.load(), uiMouseX, uiMouseY);
   }

   private autismclient.gui.f.NetHelper init_NetHelper_3() {
      return autismclient.gui.f.NetHelper.init(this.windowX, this.windowY, this.windowWidth, Math.max(this.init_CharEncoder.load(), this.windowHeight));
   }

   private OverlayBounds init_cH_cH(OverlayBounds bounds) {
      autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.createViewport();
      int margin = 4;
      int viewportW = Math.round(viewport.width());
      int viewportH = Math.round(viewport.height());
      int availableW = Math.max(1, viewportW - margin * 2);
      int availableH = Math.max(this.init_CharEncoder.load(), viewportH - margin * 2);
      int width = Math.max(Math.min(this.getMinWidth(), availableW), Math.min(bounds.width, availableW));
      int minHeight = bounds.collapsed ? this.init_CharEncoder.load() : this.getMinHeight();
      int height = Math.max(Math.min(minHeight, availableH), Math.min(bounds.height, availableH));
      int renderedHeight = bounds.collapsed ? this.init_CharEncoder.load() : height;
      int x = Math.max(margin, Math.min(bounds.x, Math.max(margin, viewportW - margin - width)));
      int y = Math.max(margin, Math.min(bounds.y, Math.max(margin, viewportH - margin - renderedHeight)));
      return new OverlayBounds(x, y, width, height, bounds.visible, bounds.collapsed);
   }

   private int push_I_11() {
      if (init_Minecraft.font == null) {
         return this.prev();
      }

      if (this.add > 0) {
         return this.add;
      }

      List<String> labels = new ArrayList<>(this.init_List.size() + 9);
      labels.add("Inside GUI");
      labels.add("Custom Menu");
      labels.add("Overlay Scale");
      labels.add("Font Scale");
      labels.add("Menu Style");
      labels.add("Auto Probe");
      labels.add("InfiniChat");
      labels.add("Stop On Leave");
      labels.add("Command Prefix");

      for (AutismKeybindOverlay.Inner_CachePool entry : this.init_List) {
         labels.add(entry.init_Str);
      }

      this.add = Math.max(
         this.valid(),
         UiLayoutMath.maxTextWidth(
               init_Minecraft.font,
               this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
               this.init_CharEncoder.load(autismclient.gui.f.b.l.read),
               labels
            )
            + this.size()
      );
      return this.add;
   }

   private int pop_I_11() {
      if (init_Minecraft.font == null) {
         return 112;
      }

      int keybindWidth = this.push_I_11() + this.data() + this.value();
      if (!AutismLiteVariant.enabled()) {
         int styleWidth = this.init_I_Str_I_3("DEFAULT", 47) + 2 + this.init_I_Str_I_3("CSGO", 34);
         keybindWidth = Math.max(keybindWidth, this.push_I_11() + this.data() + styleWidth);
      }

      return keybindWidth;
   }

   private int tick_I_9() {
      if (init_Minecraft.font == null) {
         return 120;
      }

      float fontScale = autismclient.gui.sdf.MsdfTextRenderer.fontScale();
      if (fontScale != this.close_F) {
         this.close_F = fontScale;
         this.clear = 0;
      }

      if (this.clear > 0) {
         return this.clear;
      }

      int headerReserve = this.add_I_7() * 2 + this.get_I_7() * 2 + 14;
      int titleWidth = autismclient.gui.f.b.k.init(
         init_Minecraft.font,
         "Settings",
         this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.init),
         this.init_CharEncoder.load(autismclient.gui.f.b.l.init)
      );
      this.clear = Math.max(this.run_I_9(), Math.max(this.pop_I_11() + this.get_I_7() * 2, titleWidth + headerReserve + this.get_I_7()));
      return this.clear;
   }

   private void init_V_Str_5(String requested) {
      String selected = CommandPrefixHelper.normalizePrefix(requested);
      autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
      if (module != null) {
         module.setCommandPrefix(selected);
      } else {
         this.init_AutismConfig().commandPrefix = selected;
         this.init_AutismConfig().save();
      }
   }

   private void flush_V_6() {
      if (this.init_RefQueue != null) {
         this.init_RefQueue.close();
      }

      this.init_RefQueue = null;
   }

   private int run_I_9() {
      return 120;
   }

   private int get_I_7() {
      return 4;
   }

   private int set_I_7() {
      return 0;
   }

   private int put() {
      return 20;
   }

   private int add_I_7() {
      return 10;
   }

   private int clear() {
      return 2;
   }

   private int reset() {
      return 170;
   }

   private int start_I_7() {
      return 3;
   }

   private int stop() {
      return 8;
   }

   private int next() {
      return 2;
   }

   private int prev() {
      return 82;
   }

   private int valid() {
      return 68;
   }

   private int size() {
      return 0;
   }

   private int data() {
      return 4;
   }

   private int value() {
      return 34;
   }

   private int cache() {
      return 2;
   }

   private int state() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private static int queue() {
      return entry() - 2;
   }

   private static int buffer() {
      return Math.min(queue(), 11);
   }

   private int init_I_Str_I_3(String label, int fallback) {
      return init_Minecraft.font == null
         ? fallback
         : autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(
            autismclient.gui.f.b.k.init(
               init_Minecraft.font,
               label,
               this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
               this.init_CharEncoder.load(autismclient.gui.f.b.l.read)
            )
         );
   }

   private static int entry() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private final class Inner_BufferHelper extends autismclient.gui.ui.layout.LabeledControlRow {
      private final int init_I_2;
      private final AutismKeybindOverlay.Inner_CachePool init_AutismKeybindOverl;
      private final autismclient.gui.ui.layout.LabelNode init_PoolEntry;

      private Inner_BufferHelper(int index, AutismKeybindOverlay.Inner_CachePool entry) {
         super(
            new autismclient.gui.ui.layout.LabelNode(entry.init_Str, autismclient.gui.f.b.l.read).setTrimToBounds(true),
            new AutismKeybindOverlay.Inner_StreamUtil(
               entry.init_IntSupplier,
               () -> AutismKeybindOverlay.this.put == index,
               () -> AutismKeybindOverlay.this.put = AutismKeybindOverlay.this.put == index ? -1 : index
            )
         );
         this.init_I_2 = index;
         this.init_AutismKeybindOverl = entry;
         this.nodeHeight = 14.0F;
         this.nodeGrowX = true;
         this.init_PoolEntry = (autismclient.gui.ui.layout.LabelNode)this.getLabel();
         this.setLabelWidth(AutismKeybindOverlay.this.push_I_11());
         this.setGap(AutismKeybindOverlay.this.data());
         this.setAlignControlEnd(true);
         this.setPadding(autismclient.gui.ui.layout.EdgeInsets.ZERO);
      }

      @Override
      public float measurePreferredWidth(autismclient.gui.ui.layout.LayoutContext context) {
         return AutismKeybindOverlay.this.windowWidth - AutismKeybindOverlay.this.get_I_7() * 2;
      }

      @Override
      public float measureHeight(autismclient.gui.ui.layout.LayoutContext context, float availableWidth) {
         return AutismKeybindOverlay.entry();
      }

      @Override
      public void render(autismclient.gui.ui.layout.LayoutContext context) {
         this.init_PoolEntry.init(autismclient.gui.f.b.l.read);
         super.render(context);
         if (this.init_PoolEntry.containsPoint(context.mouseX(), context.mouseY())) {
            AutismKeybindOverlay.this.write_Str = this.init_AutismKeybindOverl.load;
            AutismKeybindOverlay.this.sync_F = context.mouseX() + AutismKeybindOverlay.this.stop();
            AutismKeybindOverlay.this.flush_F = context.mouseY() - AutismKeybindOverlay.this.next();
         }
      }

      @Override
      public boolean mouseClicked(autismclient.gui.ui.layout.LayoutContext context, float mouseX, float mouseY, int button) {
         return super.mouseClicked(context, mouseX, mouseY, button);
      }
   }

   private static class Inner_CachePool {
      final String init_Str;
      final String load;
      final IntSupplier init_IntSupplier;
      final IntConsumer init_IntConsumer;

      Inner_CachePool(String label, String tooltip, IntSupplier getter, IntConsumer setter) {
         this.init_Str = label;
         this.load = tooltip;
         this.init_IntSupplier = getter;
         this.init_IntConsumer = setter;
      }
   }

   private final class Inner_ConfigStore extends autismclient.gui.ui.layout.LabeledControlRow {
      private final autismclient.gui.ui.layout.ButtonNode init_ByteReader;
      private final autismclient.gui.ui.layout.ButtonNode load_ByteReader;

      private Inner_ConfigStore() {
         super(
            new autismclient.gui.ui.layout.LabelNode("Menu Style", autismclient.gui.f.b.l.read).setTrimToBounds(true),
            new autismclient.gui.ui.layout.RowLayout().init(2).init(autismclient.gui.ui.layout.EdgeInsets.ZERO)
         );
         autismclient.gui.ui.layout.RowLayout choices = (autismclient.gui.ui.layout.RowLayout)this.getControl();
         this.init_ByteReader = this.init("DEFAULT", "DEFAULT", AutismKeybindOverlay.this.init_I_Str_I_3("DEFAULT", 47));
         this.load_ByteReader = this.init("CSGO", "CSGO", AutismKeybindOverlay.this.init_I_Str_I_3("CSGO", 34));
         choices.addChild(this.init_ByteReader);
         choices.addChild(this.load_ByteReader);
         this.nodeHeight = 14.0F;
         this.nodeGrowX = true;
         this.setLabelWidth(AutismKeybindOverlay.this.push_I_11());
         this.setGap(AutismKeybindOverlay.this.data());
         this.setAlignControlEnd(true);
         this.setPadding(autismclient.gui.ui.layout.EdgeInsets.ZERO);
      }

      private autismclient.gui.ui.layout.ButtonNode init(String label, String style, int width) {
         return new autismclient.gui.ui.layout.ButtonNode(label, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, () -> {
            AutismKeybindOverlay.this.init_AutismConfig().moduleMenuStyle = style;
            AutismKeybindOverlay.this.init_AutismConfig().save();
         }).setPreferredWidth(width).setMinWidth(width).setMaxWidth(width).setHorizontalPadding(5).setFixedHeight(AutismKeybindOverlay.queue());
      }

      @Override
      public float measureHeight(autismclient.gui.ui.layout.LayoutContext context, float availableWidth) {
         return AutismKeybindOverlay.entry();
      }

      @Override
      public void render(autismclient.gui.ui.layout.LayoutContext context) {
         boolean csgo = "CSGO".equals(AutismKeybindOverlay.this.init_AutismConfig().moduleMenuStyle);
         this.init_ByteReader
            .setVariant(
               csgo ? autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY : autismclient.gui.ui.layout.ButtonNode.Variant.PRIMARY
            );
         this.load_ByteReader
            .setVariant(
               csgo ? autismclient.gui.ui.layout.ButtonNode.Variant.PRIMARY : autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY
            );
         super.render(context);
         if (this.containsPoint(context.mouseX(), context.mouseY())) {
            AutismKeybindOverlay.this.write_Str = "DEFAULT: movable category panels. CSGO: one window with a category sidebar. Changes apply immediately.";
            AutismKeybindOverlay.this.sync_F = context.mouseX() + AutismKeybindOverlay.this.stop();
            AutismKeybindOverlay.this.flush_F = context.mouseY() - AutismKeybindOverlay.this.next();
         }
      }
   }

   private final class Inner_DataUtil extends autismclient.gui.ui.layout.LabeledControlRow {
      private static final double[] init_DArr = new double[]{0.75, 0.85, 1.0, 1.15, 1.3};
      private final autismclient.gui.ui.layout.LabelNode init_PoolEntry;
      private final autismclient.gui.ui.layout.ButtonNode init_ByteReader;
      private final String init_Str = "Scales the custom UI font only (layout stays on Overlay Scale). Left-click increases, right-click decreases.";

      private Inner_DataUtil() {
         super(
            new autismclient.gui.ui.layout.LabelNode("Font Scale", autismclient.gui.f.b.l.read).setTrimToBounds(true),
            new autismclient.gui.ui.layout.ButtonNode("1x", autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, null)
               .setPreferredWidth(AutismKeybindOverlay.this.value())
               .setMinWidth(AutismKeybindOverlay.this.value())
               .setMaxWidth(AutismKeybindOverlay.this.value())
               .setHorizontalPadding(AutismKeybindOverlay.this.cache())
               .setFixedHeight(AutismKeybindOverlay.queue())
               .setConnectedEdges(autismclient.gui.ui.widgets.ConnectedButtonRenderer.init)
               .setTextYOffset(0)
         );
         this.init_PoolEntry = (autismclient.gui.ui.layout.LabelNode)this.getLabel();
         this.init_ByteReader = (autismclient.gui.ui.layout.ButtonNode)this.getControl();
         this.init_ByteReader.setOnPress(() -> this.init_V_I_2(1));
         this.nodeHeight = 14.0F;
         this.nodeGrowX = true;
         this.setLabelWidth(AutismKeybindOverlay.this.push_I_11());
         this.setGap(AutismKeybindOverlay.this.data());
         this.setAlignControlEnd(true);
         this.setPadding(autismclient.gui.ui.layout.EdgeInsets.ZERO);
      }

      private void init_V_I_2(int direction) {
         double current = AutismKeybindOverlay.this.init_AutismConfig().uiFontScale;
         int index = 0;
         double best = 1.7976931348623157E308;

         for (int i = 0; i < init_DArr.length; i++) {
            double distance = Math.abs(init_DArr[i] - current);
            if (distance < best) {
               best = distance;
               index = i;
            }
         }

         index = Math.floorMod(index + direction, init_DArr.length);
         AutismKeybindOverlay.this.init_AutismConfig().uiFontScale = init_DArr[index];
         AutismKeybindOverlay.this.init_AutismConfig().save();
         AutismKeybindOverlay.this.init_TextFormat.invalidateLayout();
      }

      @Override
      public float measurePreferredWidth(autismclient.gui.ui.layout.LayoutContext context) {
         return AutismKeybindOverlay.this.windowWidth - AutismKeybindOverlay.this.get_I_7() * 2;
      }

      @Override
      public float measureHeight(autismclient.gui.ui.layout.LayoutContext context, float availableWidth) {
         return AutismKeybindOverlay.entry();
      }

      @Override
      public void render(autismclient.gui.ui.layout.LayoutContext context) {
         double scale = AutismKeybindOverlay.this.init_AutismConfig().uiFontScale;
         this.init_ByteReader.setText(scale == 1.0 ? "1x" : String.format(Locale.ROOT, "%.2fx", scale).replaceAll("0+x$", "x"));
         this.init_ByteReader
            .setVariant(
               scale != 1.0 ? autismclient.gui.ui.layout.ButtonNode.Variant.PRIMARY : autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY
            );
         super.render(context);
         if (this.init_PoolEntry.containsPoint(context.mouseX(), context.mouseY())) {
            AutismKeybindOverlay.this.write_Str = "Scales the custom UI font only (layout stays on Overlay Scale). Left-click increases, right-click decreases.";
            AutismKeybindOverlay.this.sync_F = context.mouseX() + AutismKeybindOverlay.this.stop();
            AutismKeybindOverlay.this.flush_F = context.mouseY() - AutismKeybindOverlay.this.next();
         }
      }

      @Override
      public boolean mouseClicked(autismclient.gui.ui.layout.LayoutContext context, float mouseX, float mouseY, int button) {
         if ((button == 0 || button == 1) && this.init_ByteReader.containsPoint(mouseX, mouseY)) {
            this.init_V_I_2(button == 1 ? -1 : 1);
            return true;
         } else {
            return super.mouseClicked(context, mouseX, mouseY, button);
         }
      }
   }

   private final class Inner_JsonUtil extends autismclient.gui.ui.layout.LabeledControlRow {
      private final BooleanSupplier init_BooleanSupplier;
      private final Consumer<Boolean> init_Consumer;
      private final String init_Str;
      private final autismclient.gui.ui.layout.LabelNode init_PoolEntry;
      private final autismclient.gui.ui.layout.ButtonNode init_ByteReader;

      private Inner_JsonUtil(String label, String tooltip, BooleanSupplier getter, Consumer<Boolean> setter) {
         super(
            new autismclient.gui.ui.layout.LabelNode(label, autismclient.gui.f.b.l.read).setTrimToBounds(true),
            new autismclient.gui.ui.layout.ButtonNode("OFF", autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, null)
               .setConnectedToggle(true)
               .setPreferredWidth(22.0F)
               .setMinWidth(22.0F)
               .setMaxWidth(22.0F)
               .setHorizontalPadding(AutismKeybindOverlay.this.cache())
               .setFixedHeight(AutismKeybindOverlay.buffer())
               .setConnectedEdges(autismclient.gui.ui.widgets.ConnectedButtonRenderer.init)
               .setTextYOffset(0)
         );
         this.init_BooleanSupplier = getter;
         this.init_Consumer = setter;
         this.init_Str = tooltip;
         this.init_PoolEntry = (autismclient.gui.ui.layout.LabelNode)this.getLabel();
         this.init_ByteReader = (autismclient.gui.ui.layout.ButtonNode)this.getControl();
         this.init_ByteReader.setOnPress(() -> {
            setter.accept(!getter.getAsBoolean());
            AutismKeybindOverlay.this.init_AutismConfig().save();
         });
         this.nodeHeight = 14.0F;
         this.nodeGrowX = true;
         this.setLabelWidth(AutismKeybindOverlay.this.push_I_11());
         this.setGap(AutismKeybindOverlay.this.data());
         this.setAlignControlEnd(true);
         this.setPadding(autismclient.gui.ui.layout.EdgeInsets.ZERO);
      }

      @Override
      public float measurePreferredWidth(autismclient.gui.ui.layout.LayoutContext context) {
         return AutismKeybindOverlay.this.windowWidth - AutismKeybindOverlay.this.get_I_7() * 2;
      }

      @Override
      public float measureHeight(autismclient.gui.ui.layout.LayoutContext context, float availableWidth) {
         return AutismKeybindOverlay.entry();
      }

      @Override
      public void render(autismclient.gui.ui.layout.LayoutContext context) {
         boolean on = this.init_BooleanSupplier.getAsBoolean();
         this.init_PoolEntry.render(context);
         autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(
            context.graphics(), context.font(), Math.round(context.mouseX()), Math.round(context.mouseY())
         );
         autismclient.gui.f.NetHelper pillBounds = autismclient.gui.f.NetHelper.init(
            Math.round(this.init_ByteReader.x()),
            Math.round(this.init_ByteReader.y()),
            Math.round(this.init_ByteReader.width()),
            Math.round(this.init_ByteReader.height())
         );
         ToggleSwitchRenderer.init(ui, pillBounds, on, pillBounds.init_Z(ui.read(), ui.write()), "keybind:" + this.init_Str);
         if (this.init_PoolEntry.containsPoint(context.mouseX(), context.mouseY())) {
            AutismKeybindOverlay.this.write_Str = this.init_Str;
            AutismKeybindOverlay.this.sync_F = context.mouseX() + AutismKeybindOverlay.this.stop();
            AutismKeybindOverlay.this.flush_F = context.mouseY() - AutismKeybindOverlay.this.next();
         }
      }
   }

   private final class Inner_NetHelper extends autismclient.gui.ui.layout.LabeledControlRow {
      private final autismclient.gui.ui.layout.LabelNode init_PoolEntry;
      private final autismclient.gui.ui.layout.ButtonNode init_ByteReader;
      private final String init_Str = "Scales every Autism overlay/window. Left-click increases, right-click decreases. 1x is default; 1.5x/2x are for 4K/high-DPI screens.";

      private Inner_NetHelper() {
         super(
            new autismclient.gui.ui.layout.LabelNode("Overlay Scale", autismclient.gui.f.b.l.read).setTrimToBounds(true),
            new autismclient.gui.ui.layout.ButtonNode("1x", autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, null)
               .setPreferredWidth(AutismKeybindOverlay.this.value())
               .setMinWidth(AutismKeybindOverlay.this.value())
               .setMaxWidth(AutismKeybindOverlay.this.value())
               .setHorizontalPadding(AutismKeybindOverlay.this.cache())
               .setFixedHeight(AutismKeybindOverlay.queue())
               .setConnectedEdges(autismclient.gui.ui.widgets.ConnectedButtonRenderer.init)
               .setTextYOffset(0)
         );
         this.init_PoolEntry = (autismclient.gui.ui.layout.LabelNode)this.getLabel();
         this.init_ByteReader = (autismclient.gui.ui.layout.ButtonNode)this.getControl();
         this.init_ByteReader.setOnPress(() -> {
            cA.init_V_D(cA.load_D());
            AutismKeybindOverlay.this.init_TextFormat.invalidateLayout();
         });
         this.nodeHeight = 14.0F;
         this.nodeGrowX = true;
         this.setLabelWidth(AutismKeybindOverlay.this.push_I_11());
         this.setGap(AutismKeybindOverlay.this.data());
         this.setAlignControlEnd(true);
         this.setPadding(autismclient.gui.ui.layout.EdgeInsets.ZERO);
      }

      @Override
      public float measurePreferredWidth(autismclient.gui.ui.layout.LayoutContext context) {
         return AutismKeybindOverlay.this.windowWidth - AutismKeybindOverlay.this.get_I_7() * 2;
      }

      @Override
      public float measureHeight(autismclient.gui.ui.layout.LayoutContext context, float availableWidth) {
         return AutismKeybindOverlay.entry();
      }

      @Override
      public void render(autismclient.gui.ui.layout.LayoutContext context) {
         double scale = cA.init_D();
         this.init_ByteReader.setText(cA.init_Str_D(scale));
         this.init_ByteReader
            .setVariant(
               scale > 1.0 ? autismclient.gui.ui.layout.ButtonNode.Variant.PRIMARY : autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY
            );
         super.render(context);
         if (this.init_PoolEntry.containsPoint(context.mouseX(), context.mouseY())) {
            AutismKeybindOverlay.this.write_Str = "Scales every Autism overlay/window. Left-click increases, right-click decreases. 1x is default; 1.5x/2x are for 4K/high-DPI screens.";
            AutismKeybindOverlay.this.sync_F = context.mouseX() + AutismKeybindOverlay.this.stop();
            AutismKeybindOverlay.this.flush_F = context.mouseY() - AutismKeybindOverlay.this.next();
         }
      }

      @Override
      public boolean mouseClicked(autismclient.gui.ui.layout.LayoutContext context, float mouseX, float mouseY, int button) {
         if ((button == 0 || button == 1) && this.init_ByteReader.containsPoint(mouseX, mouseY)) {
            cA.init_V_D(button == 1 ? cA.read() : cA.load_D());
            AutismKeybindOverlay.this.init_TextFormat.invalidateLayout();
            return true;
         } else {
            return super.mouseClicked(context, mouseX, mouseY, button);
         }
      }
   }

   private static final class Inner_StreamUtil extends autismclient.gui.ui.layout.LayoutNode {
      private final IntSupplier init_IntSupplier;
      private final BooleanSupplier init_BooleanSupplier;
      private final Runnable init_Runnable;

      private Inner_StreamUtil(IntSupplier bindCode, BooleanSupplier capturing, Runnable onPress) {
         this.init_IntSupplier = bindCode;
         this.init_BooleanSupplier = capturing;
         this.init_Runnable = onPress;
         this.nodeWidth = 34.0F;
         this.nodeHeight = AutismKeybindOverlay.queue();
      }

      @Override
      public float measurePreferredWidth(autismclient.gui.ui.layout.LayoutContext context) {
         return 34.0F;
      }

      @Override
      public float measureHeight(autismclient.gui.ui.layout.LayoutContext context, float availableWidth) {
         return AutismKeybindOverlay.queue();
      }

      @Override
      public void render(autismclient.gui.ui.layout.LayoutContext context) {
         if (this.nodeVisible) {
            autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(
               Math.round(this.nodeX), Math.round(this.nodeY), Math.round(this.nodeWidth), Math.round(this.nodeHeight)
            );
            boolean hovered = this.nodeEnabled && this.containsPoint(context.mouseX(), context.mouseY());
            autismclient.gui.ui.widgets.KeybindButtonRenderer.init(
               autismclient.gui.f.UiHelper.init(
                  context.graphics(), context.font(), Math.round(context.mouseX()), Math.round(context.mouseY())
               ),
               bounds,
               this.init_IntSupplier.getAsInt(),
               this.init_BooleanSupplier.getAsBoolean(),
               hovered
            );
         }
      }

      @Override
      public boolean mouseClicked(autismclient.gui.ui.layout.LayoutContext context, float mouseX, float mouseY, int button) {
         if (this.nodeEnabled && this.containsPoint(mouseX, mouseY) && button == 0) {
            if (this.init_Runnable != null) {
               this.init_Runnable.run();
            }

            return true;
         } else {
            return false;
         }
      }
   }

   private final class Inner_TaskQueue extends autismclient.gui.ui.layout.LayoutNode {
      private autismclient.gui.f.NetHelper init_NetHelper = autismclient.gui.f.NetHelper.init(0, 0, 1, 1);

      private Inner_TaskQueue() {
         this.nodeHeight = 14.0F;
         this.nodeGrowX = true;
      }

      @Override
      public float measurePreferredWidth(autismclient.gui.ui.layout.LayoutContext context) {
         return AutismKeybindOverlay.this.windowWidth - AutismKeybindOverlay.this.get_I_7() * 2;
      }

      @Override
      public float measureHeight(autismclient.gui.ui.layout.LayoutContext context, float availableWidth) {
         return AutismKeybindOverlay.entry();
      }

      @Override
      public void render(autismclient.gui.ui.layout.LayoutContext context) {
         int rowX = Math.round(this.nodeX);
         int rowY = Math.round(this.nodeY);
         int rowW = Math.round(this.nodeWidth);
         int rowH = Math.round(this.nodeHeight);
         int controlW = AutismKeybindOverlay.this.value();
         int controlH = Math.min(rowH, AutismKeybindOverlay.queue());
         this.init_NetHelper = autismclient.gui.f.NetHelper.init(rowX + Math.max(0, rowW - controlW), rowY + (rowH - controlH) / 2, controlW, controlH);
         autismclient.gui.f.b.k.init(
            context.graphics(),
            context.font(),
            "Command Prefix",
            AutismKeybindOverlay.this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
            AutismKeybindOverlay.this.init_CharEncoder.load(autismclient.gui.f.b.l.read),
            rowX,
            UiLayoutMath.centerTextY(
               rowY, rowH, AutismKeybindOverlay.this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.read), AutismKeybindOverlay.this.init_CharEncoder.flush()
            ),
            Math.max(1, rowW - controlW - AutismKeybindOverlay.this.data()),
            false
         );
         String value = CommandPrefixHelper.currentPrefix();
         autismclient.gui.ui.widgets.WidgetRenderFacade.init(
            autismclient.gui.f.UiHelper.init(context.graphics(), context.font(), Math.round(context.mouseX()), Math.round(context.mouseY())),
            this.init_NetHelper,
            value,
            this.init_NetHelper.init_Z(Math.round(context.mouseX()), Math.round(context.mouseY())),
            AutismKeybindOverlay.this.init_RefQueue != null && AutismKeybindOverlay.this.init_RefQueue.isOpen()
         );
      }

      @Override
      public boolean mouseClicked(autismclient.gui.ui.layout.LayoutContext context, float mouseX, float mouseY, int button) {
         if (button == 0 && this.init_NetHelper.init_Z(Math.round(mouseX), Math.round(mouseY))) {
            AutismKeybindOverlay.this.init_RefQueue = new autismclient.gui.ui.widgets.DropdownMenu(
               this.init_NetHelper, CommandPrefixHelper.ALLOWED_PREFIXES, CommandPrefixHelper.currentPrefix(), AutismKeybindOverlay.this::init_V_Str_5
            );
            AutismKeybindOverlay.this.init_RefQueue.open();
            return true;
         } else {
            return false;
         }
      }
   }
}
