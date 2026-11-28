package autismclient.util;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class AutismLauncherOverlay extends DraggableOverlayWindow {
   private static final Minecraft MC = Minecraft.getInstance();
   private static final int flush_I = 0;
   private static final int close_I = 0;
   private static final String init_Str_2 = "MAIN_MENU";
   private static final String load_Str = "UTILITIES";
   private static final int open_I = 0;
   private static final int poll_I = 4;
   private static final int push_I = 3;
   private static final int pop_I = 2;
   private static final int tick = 20;
   private static final int run = 20;
   private static final int get = 136;
   private static final int set = 8;
   private static final int put = 14;
   private static final int add = 4;
   private static final int clear = 13;
   private static final int reset = 58;
   private static final int start = 164;
   private static final float init_F_2 = 3.0F;
   private final autismclient.gui.f.b.CharEncoder styleTheme = new autismclient.gui.f.b.CharEncoder();
   private final autismclient.gui.ui.layout.SurfaceBox rootSurface = new autismclient.gui.ui.layout.SurfaceBox();
   private final autismclient.gui.ui.layout.LayoutTreeHost layoutHost = new autismclient.gui.ui.layout.LayoutTreeHost(this.styleTheme, this.rootSurface);
   private final AutismMacroListOverlay macroListOverlay;
   private final AutismFabricatorOverlay fabricatorOverlay;
   private final AutismLANSyncOverlay lanSyncOverlay;
   private final AutismQueueEditorOverlay queueEditorOverlay;
   private final AutismPacketLoggerOverlay packetLoggerOverlay;
   private final AutismCustomFilterOverlay customFilterOverlay;
   private Supplier<AutismPacketLoggerOverlay> packetLoggerSupplier;
   private Supplier<AutismServerInfoOverlay> serverInfoSupplier;
   private final String launcherOverlayId;
   private final boolean includeFabricatorButton;
   private final boolean includeScreenSection;
   private final boolean includeChatSection;
   private Runnable closeAction;
   private Runnable desyncAction;
   private AutismKeybindOverlay keybindOverlay;
   private AutismServerInfoOverlay serverInfoOverlay;
   private autismclient.gui.ui.layout.ButtonNode sendButton;
   private autismclient.gui.ui.layout.ButtonNode delayButton;
   private autismclient.gui.ui.widgets.TextInputWidget chatInput;
   private static final String write_Str = "Type message or /command...";
   private autismclient.gui.ui.layout.KeyValueLabelNode init_JsonUtil;
   private autismclient.gui.ui.layout.KeyValueLabelNode load_JsonUtil;
   private autismclient.gui.ui.layout.KeyValueLabelNode read_JsonUtil;
   private autismclient.gui.ui.layout.CollapsibleSectionNode mainSection;
   private int panelX = 0;
   private int panelY = 0;
   private int panelWidth = 164;
   private int panelHeight = 220;
   private boolean visible = true;
   private boolean headerPressed = false;
   private float dragOffsetX;
   private float dragOffsetY;
   private float pressX;
   private float pressY;
   private int pressPanelX;
   private int pressPanelY;
   private boolean dragMoved = false;
   private Font cachedFont;
   private int cachedWidthGeneration = -2147483648;
   private boolean cachedIncludeFabricator;
   private boolean cachedIncludeScreen;
   private boolean cachedIncludeChat;
   private int cachedContentWidth = -2147483648;

   public AutismLauncherOverlay(
      AutismMacroListOverlay macroListOverlay,
      AutismFabricatorOverlay fabricatorOverlay,
      AutismLANSyncOverlay lanSyncOverlay,
      AutismQueueEditorOverlay queueEditorOverlay,
      AutismPacketLoggerOverlay packetLoggerOverlay,
      AutismCustomFilterOverlay customFilterOverlay
   ) {
      this(macroListOverlay, fabricatorOverlay, lanSyncOverlay, queueEditorOverlay, packetLoggerOverlay, customFilterOverlay, "autism-launcher", true, true);
   }

   public AutismLauncherOverlay(
      AutismMacroListOverlay macroListOverlay,
      AutismFabricatorOverlay fabricatorOverlay,
      AutismLANSyncOverlay lanSyncOverlay,
      AutismQueueEditorOverlay queueEditorOverlay,
      AutismPacketLoggerOverlay packetLoggerOverlay,
      AutismCustomFilterOverlay customFilterOverlay,
      String overlayId,
      boolean includeScreenSection,
      boolean includeChatSection
   ) {
      this(
         macroListOverlay,
         fabricatorOverlay,
         lanSyncOverlay,
         queueEditorOverlay,
         packetLoggerOverlay,
         customFilterOverlay,
         overlayId,
         true,
         includeScreenSection,
         includeChatSection
      );
   }

   public AutismLauncherOverlay(
      AutismMacroListOverlay macroListOverlay,
      AutismFabricatorOverlay fabricatorOverlay,
      AutismLANSyncOverlay lanSyncOverlay,
      AutismQueueEditorOverlay queueEditorOverlay,
      AutismPacketLoggerOverlay packetLoggerOverlay,
      AutismCustomFilterOverlay customFilterOverlay,
      String overlayId,
      boolean includeFabricatorButton,
      boolean includeScreenSection,
      boolean includeChatSection
   ) {
      this.macroListOverlay = macroListOverlay;
      this.fabricatorOverlay = fabricatorOverlay;
      this.lanSyncOverlay = lanSyncOverlay;
      this.queueEditorOverlay = queueEditorOverlay;
      this.packetLoggerOverlay = packetLoggerOverlay;
      this.customFilterOverlay = customFilterOverlay;
      this.packetLoggerSupplier = () -> this.packetLoggerOverlay;
      this.launcherOverlayId = overlayId != null && !overlayId.isBlank() ? overlayId : "autism-launcher";
      this.includeFabricatorButton = includeFabricatorButton;
      this.includeScreenSection = includeScreenSection;
      this.includeChatSection = includeChatSection;
      this.rebuildLayout();
      this.loadBounds();
      if (this.mainSection != null) {
         this.mainSection.snapExpanded(this.mainSection.isExpanded());
      }
   }

   public void setCloseAction(Runnable action) {
      this.closeAction = action;
      this.rebuildLayout();
   }

   public void setDesyncAction(Runnable action) {
      this.desyncAction = action;
      this.rebuildLayout();
   }

   public void setKeybindOverlay(AutismKeybindOverlay overlay) {
      this.keybindOverlay = overlay;
   }

   public void setServerInfoOverlay(AutismServerInfoOverlay overlay) {
      this.serverInfoOverlay = overlay;
      this.serverInfoSupplier = () -> this.serverInfoOverlay;
   }

   public void setPacketLoggerSupplier(Supplier<AutismPacketLoggerOverlay> supplier) {
      this.packetLoggerSupplier = supplier == null ? () -> this.packetLoggerOverlay : supplier;
   }

   public void setServerInfoSupplier(Supplier<AutismServerInfoOverlay> supplier) {
      this.serverInfoSupplier = supplier == null ? () -> this.serverInfoOverlay : supplier;
   }

   private AutismPacketLoggerOverlay packetLogger() {
      AutismPacketLoggerOverlay overlay = this.packetLoggerSupplier == null ? this.packetLoggerOverlay : this.packetLoggerSupplier.get();
      if (overlay != null) {
         OverlayManager.getInstance().register(overlay);
      }

      return overlay;
   }

   private AutismServerInfoOverlay serverInfo() {
      AutismServerInfoOverlay overlay = this.serverInfoSupplier == null ? this.serverInfoOverlay : this.serverInfoSupplier.get();
      if (overlay != null) {
         OverlayManager.getInstance().register(overlay);
      }

      return overlay;
   }

   private void rebuildLayout() {
      this.rootSurface.init(autismclient.gui.ui.layout.EdgeInsets.uniform(0)).setDrawBorder(false).setDrawFill(false);
      this.rootSurface.getContent().clearChildren();
      this.rootSurface.getContent().init(autismclient.gui.ui.layout.EdgeInsets.ZERO).init(0);
      this.mainSection = new autismclient.gui.ui.layout.CollapsibleSectionNode("Main Menu").init(this.sectionHeaderHeight()).load(this.valid()).setExpanded(true);
      this.mainSection.getContent().init(new autismclient.gui.ui.layout.EdgeInsets(1, 0, 1, 1)).init(this.get_I_9());
      this.rootSurface.getContent().addChild(this.mainSection);
      autismclient.gui.ui.layout.ButtonNode macrosButton = this.createButton(
         "Macros", autismclient.gui.f.a.DataUtil.sync, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, () -> {
            AutismMacroEditorOverlay macroEditorOverlay = AutismMacroEditorOverlay.getInstance();
            if (macroEditorOverlay != null && macroEditorOverlay.isVisible()) {
               if (this.macroListOverlay != null) {
                  this.macroListOverlay.setVisible(false);
               }

               OverlayManager.getInstance().bringToFront(macroEditorOverlay);
            } else if (this.macroListOverlay != null) {
               this.init_V_cX(this.macroListOverlay);
            }
         }
      );
      if (this.includeFabricatorButton) {
         this.addButtonPair(
            this.mainSection.getContent(),
            macrosButton,
            this.createButton("Fabricator", autismclient.gui.f.a.DataUtil.flush, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, () -> {
               if (this.fabricatorOverlay != null) {
                  this.init_V_cX(this.fabricatorOverlay);
               }
            })
         );
      } else {
         this.mainSection.getContent().addChild(macrosButton);
      }

      this.addButtonPair(
         this.mainSection.getContent(),
         this.createButton("LAN Sync", autismclient.gui.f.a.DataUtil.update, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, () -> {
            if (this.lanSyncOverlay != null) {
               this.init_V_cX(this.lanSyncOverlay);
            }
         }),
         this.createButton("Queue", autismclient.gui.f.a.DataUtil.poll, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, () -> {
            if (this.queueEditorOverlay != null) {
               this.init_V_cX(this.queueEditorOverlay);
            }
         })
      );
      this.addButtonPair(
         this.mainSection.getContent(),
         this.createButton("Logger", autismclient.gui.f.a.DataUtil.open, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, () -> {
            AutismPacketLoggerOverlay overlay = this.packetLogger();
            this.init_V_cX(overlay);
         }),
         this.createButton("Packets", autismclient.gui.f.a.DataUtil.close, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, () -> {
            if (this.customFilterOverlay != null) {
               this.init_V_cX(this.customFilterOverlay);
            }
         })
      );
      this.addButtonPair(
         this.mainSection.getContent(),
         this.createButton("Server Info", autismclient.gui.f.a.DataUtil.push, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, () -> {
            AutismServerInfoOverlay overlay = this.serverInfo();
            this.init_V_cX(overlay);
         }),
         this.createButton("Settings", autismclient.gui.f.a.DataUtil.write, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, () -> {
            if (this.keybindOverlay != null) {
               this.init_V_cX(this.keybindOverlay);
            }
         })
      );
      if (!AutismLiteVariant.enabled()) {
         this.addButtonPair(
            this.mainSection.getContent(),
            this.createButton("AI Assistant", autismclient.gui.f.a.DataUtil.get, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, () -> {
               if (!AutismLiteVariant.enabled()) {
                  autismclient.ai.AiAssistant.getInstance().read();
               }
            }),
            this.createButton("Schematics", autismclient.gui.f.a.DataUtil.clear, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, () -> {
               if (!AutismLiteVariant.enabled()) {
                  autismclient.ai.SchematicWorkspace.toggleWindow();
               }
            })
         );
      }

      this.init_V_Str_Identifier("PACKET", autismclient.gui.f.a.DataUtil.tick);
      this.sendButton = this.createButton("Send", null, autismclient.gui.ui.layout.ButtonNode.Variant.DANGER, this::update_V_9).setConnectedToggle(true);
      this.delayButton = this.createButton("Delay", null, autismclient.gui.ui.layout.ButtonNode.Variant.DANGER, this::sync_V_8).setConnectedToggle(true);
      this.addButtonPair(this.mainSection.getContent(), this.sendButton, this.delayButton);
      this.addButtonPair(
         this.mainSection.getContent(),
         this.createButton("Flush", null, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, this::flush_V_8),
         this.createButton("Clear", null, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, this::close_V_7)
      );
      if (this.includeScreenSection) {
         this.init_V_Str_Identifier("SCREEN", autismclient.gui.f.a.DataUtil.run);
         this.addButtonPair(
            this.mainSection.getContent(),
            this.createButton("Close", null, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, this.closeAction != null ? this.closeAction : LauncherQuickActions::update),
            this.createButton("De-sync", null, autismclient.gui.ui.layout.ButtonNode.Variant.DANGER, this.desyncAction != null ? this.desyncAction : this::open_V_6)
         );
         this.addButtonPair(
            this.mainSection.getContent(),
            this.createButton("Save", null, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, this::poll_V_4),
            this.createButton("Load", null, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, this::set_V_4)
         );
         this.addButtonPair(
            this.mainSection.getContent(),
            this.createButton("Title", null, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, LauncherQuickActions::open),
            this.createButton("Disc+Send", null, autismclient.gui.ui.layout.ButtonNode.Variant.DANGER, this::put_V_3)
         );
      }

      if (this.includeChatSection) {
         this.chatInput = new autismclient.gui.ui.widgets.TextInputWidget()
            .setCornerRadius(0)
            .setFixedHeight(this.buttonHeight())
            .setPreferredWidth(this.buttonWidth())
            .setMinWidth(this.buttonWidth())
            .setHorizontalPadding(6)
            .setPlaceholder("Type message or /command...")
            .setHistoryNavigationEnabled(true)
            .setHistoryProvider(() -> MC.gui != null ? MC.gui.hud.getChat().getRecentChat() : List.of())
            .setOnSubmit(this::sendChatInput);
         this.mainSection.getContent().addChild(this.chatInput);
         this.mainSection.getContent().addChild(new autismclient.gui.ui.layout.FixedSpacerNode(0.0F, 2.0F));
         autismclient.gui.ui.layout.RowLayout syncRow = new autismclient.gui.ui.layout.RowLayout()
            .init(this.syncRowGap())
            .init(new autismclient.gui.ui.layout.EdgeInsets(4, 0, 2, 0))
            .setUnderlineOnHover(false);
         syncRow.setGrowX(true);
         int keyColor = LauncherQuickActions.keyColor();
         int[] valueColors = LauncherQuickActions.valueColors();
         this.init_JsonUtil = syncRow.addChild(
            new autismclient.gui.ui.layout.KeyValueLabelNode("Rev: ", "--").init(keyColor).load(valueColors[0]).withGrowX(true)
         );
         this.load_JsonUtil = syncRow.addChild(
            new autismclient.gui.ui.layout.KeyValueLabelNode("SyncID: ", "--").init(keyColor).load(valueColors[1]).withGrowX(true)
         );
         this.read_JsonUtil = syncRow.addChild(
            new autismclient.gui.ui.layout.KeyValueLabelNode("Slot: ", "--").init(keyColor).load(valueColors[2]).withGrowX(true)
         );
         this.mainSection.getContent().addChild(syncRow);
      } else {
         this.chatInput = null;
         this.init_JsonUtil = null;
         this.load_JsonUtil = null;
         this.read_JsonUtil = null;
      }

      if (!this.includeChatSection && !this.mainSection.getContent().getChildren().isEmpty()) {
         autismclient.gui.ui.layout.LayoutNode last = this.mainSection.getContent().getChildren().getLast();
         if (last instanceof autismclient.gui.ui.layout.ButtonNode button) {
            button.setConnectedEdges(autismclient.gui.ui.widgets.ConnectedButtonRenderer.init(true, true));
         } else if (last instanceof autismclient.gui.ui.layout.RowLayout pair) {
            List<autismclient.gui.ui.layout.ButtonNode> buttons = pair.getChildren()
               .stream()
               .filter(autismclient.gui.ui.layout.ButtonNode.class::isInstance)
               .map(autismclient.gui.ui.layout.ButtonNode.class::cast)
               .toList();

            for (int i = 0; i < buttons.size(); i++) {
               buttons.get(i).setConnectedEdges(autismclient.gui.ui.widgets.ConnectedButtonRenderer.init(i == 0, i == buttons.size() - 1));
            }
         }
      }
   }

   private void init_V_cX(AutismOverlay overlay) {
      if (overlay != null) {
         OverlayManager manager = OverlayManager.getInstance();
         boolean wasRegistered = manager.getOverlays().contains(overlay);
         manager.register(overlay);
         manager.setHidden(overlay, false);
         if (!wasRegistered && overlay.isVisible()) {
            manager.bringToFront(overlay);
         } else {
            overlay.setVisible(!overlay.isVisible());
            if (overlay.isVisible()) {
               manager.bringToFront(overlay);
            }
         }
      }
   }

   private void init_V_Str_Identifier(String label, Identifier icon) {
      if (!this.mainSection.getContent().getChildren().isEmpty()) {
         this.mainSection.getContent().addChild(new autismclient.gui.ui.layout.FixedSpacerNode(0.0F, this.run_I_11()));
      }

      this.mainSection
         .getContent()
         .addChild(new autismclient.gui.ui.layout.IconHeaderNode(label, icon).init(this.headerIconSize()).setConnectedStyle(true));
   }

   private autismclient.gui.ui.layout.ButtonNode addButtonTo(autismclient.gui.ui.layout.ColumnLayout parent, String label, Identifier icon, Runnable action) {
      autismclient.gui.ui.layout.ButtonNode button = this.createButton(label, icon, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, action);
      parent.addChild(button);
      return button;
   }

   private void addButtonPair(autismclient.gui.ui.layout.ColumnLayout parent, autismclient.gui.ui.layout.ButtonNode left, autismclient.gui.ui.layout.ButtonNode right) {
      autismclient.gui.ui.layout.RowLayout row = new autismclient.gui.ui.layout.RowLayout()
         .init(this.get_I_9())
         .init(autismclient.gui.ui.layout.EdgeInsets.ZERO)
         .setUnderlineOnHover(false);
      left.withGrowX(true);
      right.withGrowX(true);
      row.addChild(left.setConnectedEdges(autismclient.gui.ui.widgets.ConnectedButtonRenderer.write));
      row.addChild(right.setConnectedEdges(autismclient.gui.ui.widgets.ConnectedButtonRenderer.write));
      row.setGrowX(true);
      parent.addChild(row);
   }

   private autismclient.gui.ui.layout.ButtonNode createButton(String label, Identifier icon, autismclient.gui.ui.layout.ButtonNode.Variant variant, Runnable action) {
      autismclient.gui.ui.layout.ButtonNode button = new autismclient.gui.ui.layout.ButtonNode(label, variant, action)
         .withGrowX(false)
         .setConnectedStyle(true)
         .setConnectedEdges(autismclient.gui.ui.widgets.ConnectedButtonRenderer.write)
         .setFixedHeight(this.set_I_9())
         .setHorizontalPadding(this.put_I_3())
         .setMinWidth(this.iconButtonWidth(label, icon))
         .setTextYOffset(0);
      if (icon != null) {
         button.setLeadingIcon(icon)
            .setTextAlign(autismclient.gui.ui.layout.ButtonNode.TextAlign.START)
            .setIconSize(this.add_I_9())
            .setIconGap(this.clear_I_3());
      }

      return button;
   }

   @Override
   public String getOverlayId() {
      return this.launcherOverlayId;
   }

   @Override
   public int getMinWidth() {
      return this.push_I_13();
   }

   @Override
   public int getMinHeight() {
      return this.minPanelHeight();
   }

   @Override
   public OverlayBounds getBounds() {
      return new OverlayBounds(this.panelX, this.panelY, this.panelWidth, this.panelHeight, this.visible, !this.mainSection.isExpanded());
   }

   @Override
   public void setBounds(OverlayBounds bounds) {
      if (bounds != null) {
         OverlayBounds clamped = this.clampBounds(bounds);
         this.panelX = clamped.x;
         this.panelY = clamped.y;
         this.panelWidth = clamped.width;
         this.panelHeight = clamped.height;
         this.visible = clamped.visible;
         if (this.mainSection != null) {
            this.mainSection.snapExpanded(!clamped.collapsed);
         }
      }
   }

   @Override
   public void saveBounds() {
      AutismSessionState.getInstance().setOverlayBounds(this.getOverlayId(), this.getBounds());
      AutismConfig config = AutismConfig.getGlobal();
      AutismConfig.ModuleCategoryLayout layout = config.moduleCategoryLayouts.computeIfAbsent("MAIN_MENU", ignored -> new AutismConfig.ModuleCategoryLayout());
      OverlayBounds trueGeometry = OverlayManager.getInstance().getStashedBounds(this.getOverlayId());
      int saveX = trueGeometry != null ? trueGeometry.x : this.panelX;
      int saveY = trueGeometry != null ? trueGeometry.y : this.panelY;
      if (layout.f_x_I != saveX || layout.f_y_I != saveY || layout.collapsed != this.isCollapsed()) {
         layout.f_x_I = saveX;
         layout.f_y_I = saveY;
         layout.collapsed = this.isCollapsed();
         config.save();
      }
   }

   @Override
   public void loadBounds() {
      OverlayBounds saved = AutismSessionState.getInstance().getOverlayBounds(this.getOverlayId());
      boolean savedVisible = saved == null || saved.visible;
      boolean savedCollapsed = saved != null && saved.collapsed;
      AutismConfig config = AutismConfig.getGlobal();
      AutismConfig.ModuleCategoryLayout layout = config.moduleCategoryLayouts.get("MAIN_MENU");
      if (layout == null) {
         AutismConfig.ModuleCategoryLayout old = config.moduleCategoryLayouts.get("UTILITIES");
         if (old != null) {
            layout = new AutismConfig.ModuleCategoryLayout();
            layout.f_x_I = old.f_x_I;
            layout.f_y_I = old.f_y_I;
            layout.collapsed = old.collapsed;
            config.moduleCategoryLayouts.put("MAIN_MENU", layout);
         }
      }

      if (layout != null && layout.f_x_I >= 0 && layout.f_y_I >= 0) {
         this.setBounds(new OverlayBounds(layout.f_x_I, layout.f_y_I, this.panelWidth, this.panelHeight, savedVisible, layout.collapsed || savedCollapsed));
      } else if (saved != null) {
         this.setBounds(saved);
      }
   }

   @Override
   public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      if (this.visible || this.isClosing()) {
         AutismSessionState shared = AutismSessionState.getInstance();
         if (this.sendButton != null) {
            boolean sending = shared.isSendPacketsEnabled();
            this.sendButton.setText("Send");
            this.sendButton
               .setVariant(
                  sending ? autismclient.gui.ui.layout.ButtonNode.Variant.SUCCESS : autismclient.gui.ui.layout.ButtonNode.Variant.DANGER
               );
         }

         if (this.delayButton != null) {
            boolean delaying = shared.isDelayPacketsEnabled();
            this.delayButton.setText("Delay");
            this.delayButton
               .setVariant(
                  delaying ? autismclient.gui.ui.layout.ButtonNode.Variant.SUCCESS : autismclient.gui.ui.layout.ButtonNode.Variant.DANGER
               );
         }

         this.refreshSyncMetrics();
         this.refreshChatPlaceholder();
         this.panelWidth = this.push_I_13();
         autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
         float uiMouseX = viewport.toUiX(mouseX);
         float uiMouseY = viewport.toUiY(mouseY);
         autismclient.gui.ui.layout.LayoutContext metrics = new autismclient.gui.ui.layout.LayoutContext(
            context, MC.font, viewport, this.styleTheme, uiMouseX, uiMouseY, delta
         );
         this.panelHeight = Math.max(this.getMinHeight(), Math.round(this.rootSurface.measureHeight(metrics, this.panelWidth)));
         OverlayBounds clamped = this.clampBounds(
            new OverlayBounds(this.panelX, this.panelY, this.panelWidth, this.panelHeight, this.visible, this.mainSection != null && !this.mainSection.isExpanded())
         );
         this.panelX = clamped.x;
         this.panelY = clamped.y;
         this.panelWidth = clamped.width;
         this.panelHeight = clamped.height;
         this.rootSurface.setActive(true);
         this.rootSurface.setBounds(this.panelX, this.panelY, this.panelWidth, this.panelHeight);
         this.layoutHost.render(context, mouseX, mouseY, delta);
      }
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.visible) {
         return false;
      } else {
         autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
         float uiMouseX = viewport.toUiX(mouseX);
         float uiMouseY = viewport.toUiY(mouseY);
         if (button == 0 && this.init_Z_F_F_3(uiMouseX, uiMouseY) && this.mainSection != null) {
            this.headerPressed = true;
            this.dragMoved = false;
            this.dragOffsetX = uiMouseX - this.panelX;
            this.dragOffsetY = uiMouseY - this.panelY;
            this.pressX = uiMouseX;
            this.pressY = uiMouseY;
            this.pressPanelX = this.panelX;
            this.pressPanelY = this.panelY;
            return true;
         } else {
            return this.layoutHost.mouseClicked(mouseX, mouseY, button) ? true : this.containsPoint(mouseX, mouseY);
         }
      }
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (this.headerPressed) {
         this.headerPressed = false;
         this.saveBounds();
         return true;
      } else {
         return this.layoutHost.mouseReleased(mouseX, mouseY, button);
      }
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (!this.headerPressed) {
         return this.layoutHost.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      float uiMouseX = viewport.toUiX(mouseX);
      float uiMouseY = viewport.toUiY(mouseY);
      OverlayBounds clamped = this.clampBounds(
         new OverlayBounds(
            Math.round(uiMouseX - this.dragOffsetX),
            Math.round(uiMouseY - this.dragOffsetY),
            this.panelWidth,
            this.panelHeight,
            this.visible,
            this.mainSection != null && !this.mainSection.isExpanded()
         )
      );
      this.panelX = clamped.x;
      this.panelY = clamped.y;
      this.dragMoved = this.dragMoved
         || Math.abs(uiMouseX - this.pressX) >= 3.0F
         || Math.abs(uiMouseY - this.pressY) >= 3.0F
         || this.panelX != this.pressPanelX
         || this.panelY != this.pressPanelY;
      return true;
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      return this.layoutHost.mouseScrolled(mouseX, mouseY, amount);
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (!this.visible || this.isCollapsed()) {
         return false;
      }

      if (keyCode == 258 && this.chatInput != null && this.chatInput.isFocused() && this.chatInput.getValue().isEmpty()) {
         String last = this.load_Str_6();
         if (last != null && !last.isBlank()) {
            this.chatInput.setValue(last);
            this.chatInput.moveCursorToEnd();
         }

         return true;
      } else {
         return this.layoutHost.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   @Override
   public boolean charTyped(char chr, int modifiers) {
      return this.visible && !this.isCollapsed() ? this.layoutHost.charTyped(chr, modifiers) : false;
   }

   @Override
   public boolean isVisible() {
      return this.visible;
   }

   @Override
   public void setVisible(boolean visible) {
      this.visible = visible;
      if (!visible) {
         this.layoutHost.clearFocus();
         this.headerPressed = false;
         this.dragMoved = false;
      } else if (this.mainSection != null) {
         this.mainSection.snapExpanded(this.mainSection.isExpanded());
      }
   }

   @Override
   public boolean containsPoint(double mouseX, double mouseY) {
      if (!this.visible) {
         return false;
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      float uiX = viewport.toUiX(mouseX);
      float uiY = viewport.toUiY(mouseY);
      return uiX >= this.panelX && uiX < this.panelX + this.panelWidth && uiY >= this.panelY && uiY < this.panelY + this.panelHeight;
   }

   @Override
   public boolean isOverHeader(double mouseX, double mouseY) {
      if (!this.visible) {
         return false;
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      return this.init_Z_F_F_3(viewport.toUiX(mouseX), viewport.toUiY(mouseY));
   }

   @Override
   public boolean supportsHeaderCollapseClick() {
      return true;
   }

   @Override
   public boolean isCollapsed() {
      return this.mainSection != null && !this.mainSection.isExpanded();
   }

   @Override
   public void setCollapsed(boolean collapsed) {
      if (this.isCollapsed() != collapsed) {
         if (this.mainSection != null) {
            this.mainSection.setExpanded(!collapsed);
         }

         if (collapsed) {
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
   public void clearInputFocus() {
      this.layoutHost.clearFocus();
   }

   private boolean init_Z_F_F_3(float uiMouseX, float uiMouseY) {
      return this.mainSection != null
         && uiMouseX >= this.panelX
         && uiMouseX < this.panelX + this.panelWidth
         && uiMouseY >= this.panelY
         && uiMouseY < this.panelY + this.sectionHeaderHeight();
   }

   private OverlayBounds clampBounds(OverlayBounds bounds) {
      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      int margin = 4;
      int viewportW = Math.round(viewport.width());
      int viewportH = Math.round(viewport.height());
      int availableW = Math.max(1, viewportW - margin * 2);
      int availableH = Math.max(this.sectionHeaderHeight(), viewportH - margin * 2);
      int width = Math.max(Math.min(this.getMinWidth(), availableW), Math.min(bounds.width, availableW));
      int minHeight = bounds.collapsed ? this.sectionHeaderHeight() : this.getMinHeight();
      int height = Math.max(Math.min(minHeight, availableH), Math.min(bounds.height, availableH));
      int renderedHeight = bounds.collapsed ? this.sectionHeaderHeight() : height;
      int x = Math.max(margin, Math.min(bounds.x, Math.max(margin, viewportW - margin - width)));
      int y = Math.max(margin, Math.min(bounds.y, Math.max(margin, viewportH - margin - renderedHeight)));
      return new OverlayBounds(x, y, width, height, bounds.visible, bounds.collapsed);
   }

   private int push_I_13() {
      if (MC.font == null) {
         return this.minPanelWidth();
      }

      int reloadGen = autismclient.gui.f.b.k.init_I();
      if (this.cachedContentWidth != -2147483648
         && this.cachedFont == MC.font
         && this.cachedWidthGeneration == reloadGen
         && this.cachedIncludeFabricator == this.includeFabricatorButton
         && this.cachedIncludeScreen == this.includeScreenSection
         && this.cachedIncludeChat == this.includeChatSection) {
         return this.cachedContentWidth;
      }

      int computed = this.pop_I_13();
      this.cachedFont = MC.font;
      this.cachedWidthGeneration = reloadGen;
      this.cachedIncludeFabricator = this.includeFabricatorButton;
      this.cachedIncludeScreen = this.includeScreenSection;
      this.cachedIncludeChat = this.includeChatSection;
      this.cachedContentWidth = computed;
      return computed;
   }

   private int pop_I_13() {
      int pairWidth = this.minButtonWidth();
      pairWidth = Math.max(pairWidth, this.iconButtonWidth("Macros", autismclient.gui.f.a.DataUtil.sync));
      if (this.includeFabricatorButton) {
         pairWidth = Math.max(pairWidth, this.iconButtonWidth("Fabricator", autismclient.gui.f.a.DataUtil.flush));
      }

      pairWidth = Math.max(pairWidth, this.iconButtonWidth("LAN Sync", autismclient.gui.f.a.DataUtil.update));
      pairWidth = Math.max(pairWidth, this.iconButtonWidth("Queue", autismclient.gui.f.a.DataUtil.poll));
      pairWidth = Math.max(pairWidth, this.iconButtonWidth("Logger", autismclient.gui.f.a.DataUtil.open));
      pairWidth = Math.max(pairWidth, this.iconButtonWidth("Packets", autismclient.gui.f.a.DataUtil.close));
      pairWidth = Math.max(pairWidth, this.iconButtonWidth("Server Info", autismclient.gui.f.a.DataUtil.push));
      pairWidth = Math.max(pairWidth, this.iconButtonWidth("Settings", autismclient.gui.f.a.DataUtil.write));

      pairWidth = Math.max(pairWidth, this.iconButtonWidth("Send", null));
      pairWidth = Math.max(pairWidth, this.iconButtonWidth("Delay", null));
      pairWidth = Math.max(pairWidth, this.iconButtonWidth("Flush", null));
      pairWidth = Math.max(pairWidth, this.iconButtonWidth("Clear", null));
      if (this.includeScreenSection) {
         pairWidth = Math.max(pairWidth, this.iconButtonWidth("Close", null));
         pairWidth = Math.max(pairWidth, this.iconButtonWidth("De-sync", null));
         pairWidth = Math.max(pairWidth, this.iconButtonWidth("Save", null));
         pairWidth = Math.max(pairWidth, this.iconButtonWidth("Load", null));
         pairWidth = Math.max(pairWidth, this.iconButtonWidth("Title", null));
         pairWidth = Math.max(pairWidth, this.iconButtonWidth("Disc+Send", null));
      }

      int chatWidth = this.includeChatSection ? Math.max(this.buttonWidth(), this.placeholderWidth("Type message or /command...")) : 0;
      int headerWidth = autismclient.gui.f.b.k.init(
            MC.font,
            "MAIN MENU",
            this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.load),
            this.styleTheme.load(autismclient.gui.f.b.l.load)
         )
         + this.headerIconSize()
         + this.clear_I_3()
         + this.headerTrailingPad();
      int subLabelWidth = this.labelWidth("PACKET");
      if (this.includeScreenSection) {
         subLabelWidth = Math.max(subLabelWidth, this.labelWidth("SCREEN"));
      }

      int contentWidth = Math.max(headerWidth, Math.max(subLabelWidth, Math.max(pairWidth * 2 + this.get_I_9(), chatWidth)));
      return Math.max(this.minPanelWidth(), contentWidth + this.tick_I_11() * 2 + this.panelExtraWidth());
   }

   private int iconButtonWidth(String label, Identifier icon) {
      if (MC.font == null) {
         return this.minButtonWidth();
      }

      int width = autismclient.gui.f.b.k.init(
            MC.font,
            label,
            this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read),
            this.styleTheme.load(autismclient.gui.f.b.l.read)
         )
         + this.put_I_3() * 2
         + this.textPad();
      if (icon != null) {
         width += this.add_I_9() + this.clear_I_3();
      }

      return width;
   }

   private int labelWidth(String label) {
      return autismclient.gui.f.b.k.init(
            MC.font,
            label,
            this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.load),
            this.styleTheme.load(autismclient.gui.f.b.l.load)
         )
         + this.headerIconSize()
         + this.clear_I_3()
         + this.textPad();
   }

   private int placeholderWidth(String placeholder) {
      return autismclient.gui.f.b.k.init(
            MC.font,
            placeholder,
            this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read),
            this.styleTheme.load(autismclient.gui.f.b.l.write)
         )
         + this.placeholderPad()
         + this.textPad();
   }

   private int tick_I_11() {
      return 0;
   }

   private int run_I_11() {
      return 0;
   }

   private int get_I_9() {
      return 0;
   }

   private int set_I_9() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int put_I_3() {
      return 5;
   }

   private int add_I_9() {
      return 10;
   }

   private int clear_I_3() {
      return 3;
   }

   private int headerIconSize() {
      return 11;
   }

   private int minButtonWidth() {
      return 54;
   }

   private int minPanelWidth() {
      return 120;
   }

   private int minPanelHeight() {
      return 56;
   }

   private int sectionHeaderHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int valid() {
      return 0;
   }

   private int buttonHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int buttonWidth() {
      return 124;
   }

   private int syncRowGap() {
      return 4;
   }

   private int headerTrailingPad() {
      return 16;
   }

   private int placeholderPad() {
      return 10;
   }

   private int textPad() {
      return 2;
   }

   private int panelExtraWidth() {
      return 2;
   }

   private void update_V_9() {
      LauncherQuickActions.toggleSendPackets();
   }

   private void sync_V_8() {
      LauncherQuickActions.toggleDelayPackets();
   }

   private void flush_V_8() {
      LauncherQuickActions.read();
   }

   private void close_V_7() {
      LauncherQuickActions.write();
   }

   private void open_V_6() {
      LauncherQuickActions.sync();
   }

   private void poll_V_4() {
      LauncherQuickActions.flush();
   }

   private void set_V_4() {
      LauncherQuickActions.close();
   }

   private void put_V_3() {
      LauncherQuickActions.poll();
   }

   private void sendChatInput(String raw) {
      String sent = LauncherQuickActions.init(raw);
      if (sent != null && this.chatInput != null) {
         if (!sent.isEmpty()) {
            this.chatInput.pushHistoryEntry(sent);
         }

         this.chatInput.setValue("");
         this.chatInput.setFocused(false);
      }
   }

   private void refreshChatPlaceholder() {
      if (this.chatInput != null) {
         this.chatInput.setPlaceholder(LauncherQuickActions.inputPlaceholder());
      }
   }

   private String load_Str_6() {
      return LauncherQuickActions.lastSentMessage();
   }

   private void refreshSyncMetrics() {
      String[] metrics = LauncherQuickActions.containerMetrics();
      if (this.init_JsonUtil != null) {
         this.init_JsonUtil.init(metrics[0]);
      }

      if (this.load_JsonUtil != null) {
         this.load_JsonUtil.init(metrics[1]);
      }

      if (this.read_JsonUtil != null) {
         this.read_JsonUtil.init(metrics[2]);
      }
   }
}
