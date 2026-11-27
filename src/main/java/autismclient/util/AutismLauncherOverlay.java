package autismclient.util;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class AutismLauncherOverlay extends DraggableOverlayWindow {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
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
   private final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private final autismclient.gui.ui.layout.SurfaceBox init_StateCache = new autismclient.gui.ui.layout.SurfaceBox();
   private final autismclient.gui.ui.layout.LayoutTreeHost init_TextFormat = new autismclient.gui.ui.layout.LayoutTreeHost(this.init_CharEncoder, this.init_StateCache);
   private final AutismMacroListOverlay init_AutismMacroListOve;
   private final AutismFabricatorOverlay init_AutismFabricatorOv;
   private final AutismLANSyncOverlay init_AutismLANSyncOverl;
   private final AutismQueueEditorOverlay init_AutismQueueEditorO;
   private final AutismPacketLoggerOverlay init_AutismPacketLogger;
   private final AutismCustomFilterOverlay init_AutismCustomFilter;
   private Supplier<AutismPacketLoggerOverlay> init_Supplier;
   private Supplier<AutismServerInfoOverlay> load_Supplier;
   private final String read_Str;
   private final boolean write_Z;
   private final boolean update_Z;
   private final boolean sync_Z;
   private Runnable init_Runnable;
   private Runnable load_Runnable;
   private AutismKeybindOverlay init_AutismKeybindOverl;
   private AutismServerInfoOverlay init_AutismServerInfoOv;
   private autismclient.gui.ui.layout.ButtonNode init_ByteReader;
   private autismclient.gui.ui.layout.ButtonNode load_ByteReader;
   private autismclient.gui.ui.widgets.TextInputWidget init_BitSetUtil;
   private static final String write_Str = "Type message or /command...";
   private autismclient.gui.ui.layout.KeyValueLabelNode init_JsonUtil;
   private autismclient.gui.ui.layout.KeyValueLabelNode load_JsonUtil;
   private autismclient.gui.ui.layout.KeyValueLabelNode read_JsonUtil;
   private autismclient.gui.ui.layout.CollapsibleSectionNode init_DataUtil;
   private int stop = 0;
   private int next = 0;
   private int prev = 164;
   private int valid = 220;
   private boolean flush_Z = true;
   private boolean close_Z = false;
   private float load_F;
   private float read_F;
   private float write_F;
   private float update_F;
   private int size;
   private int data;
   private boolean open_Z = false;
   private Font init_Font;
   private int value = -2147483648;
   private boolean poll_Z;
   private boolean push_Z;
   private boolean pop_Z;
   private int cache = -2147483648;

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
      this.init_AutismMacroListOve = macroListOverlay;
      this.init_AutismFabricatorOv = fabricatorOverlay;
      this.init_AutismLANSyncOverl = lanSyncOverlay;
      this.init_AutismQueueEditorO = queueEditorOverlay;
      this.init_AutismPacketLogger = packetLoggerOverlay;
      this.init_AutismCustomFilter = customFilterOverlay;
      this.init_Supplier = () -> this.init_AutismPacketLogger;
      this.read_Str = overlayId != null && !overlayId.isBlank() ? overlayId : "autism-launcher";
      this.write_Z = includeFabricatorButton;
      this.update_Z = includeScreenSection;
      this.sync_Z = includeChatSection;
      this.write_V_11();
      this.loadBounds();
      if (this.init_DataUtil != null) {
         this.init_DataUtil.snapExpanded(this.init_DataUtil.isExpanded());
      }
   }

   public void init_V_Runnable(Runnable action) {
      this.init_Runnable = action;
      this.write_V_11();
   }

   public void load_V_Runnable(Runnable action) {
      this.load_Runnable = action;
      this.write_V_11();
   }

   public void setKeybindOverlay(AutismKeybindOverlay overlay) {
      this.init_AutismKeybindOverl = overlay;
   }

   public void init_V_AutismServerInfoOv(AutismServerInfoOverlay overlay) {
      this.init_AutismServerInfoOv = overlay;
      this.load_Supplier = () -> this.init_AutismServerInfoOv;
   }

   public void setPacketLoggerSupplier(Supplier<AutismPacketLoggerOverlay> supplier) {
      this.init_Supplier = supplier == null ? () -> this.init_AutismPacketLogger : supplier;
   }

   public void setServerInfoSupplier(Supplier<AutismServerInfoOverlay> supplier) {
      this.load_Supplier = supplier == null ? () -> this.init_AutismServerInfoOv : supplier;
   }

   private AutismPacketLoggerOverlay init_AutismPacketLogger() {
      AutismPacketLoggerOverlay overlay = this.init_Supplier == null ? this.init_AutismPacketLogger : this.init_Supplier.get();
      if (overlay != null) {
         OverlayManager.getInstance().register(overlay);
      }

      return overlay;
   }

   private AutismServerInfoOverlay init_AutismServerInfoOv() {
      AutismServerInfoOverlay overlay = this.load_Supplier == null ? this.init_AutismServerInfoOv : this.load_Supplier.get();
      if (overlay != null) {
         OverlayManager.getInstance().register(overlay);
      }

      return overlay;
   }

   private void write_V_11() {
      this.init_StateCache.init(autismclient.gui.ui.layout.EdgeInsets.uniform(0)).setDrawBorder(false).setDrawFill(false);
      this.init_StateCache.getContent().clearChildren();
      this.init_StateCache.getContent().init(autismclient.gui.ui.layout.EdgeInsets.ZERO).init(0);
      this.init_DataUtil = new autismclient.gui.ui.layout.CollapsibleSectionNode("Main Menu").init(this.prev()).load(this.valid()).setExpanded(true);
      this.init_DataUtil.getContent().init(new autismclient.gui.ui.layout.EdgeInsets(1, 0, 1, 1)).init(this.get_I_9());
      this.init_StateCache.getContent().addChild(this.init_DataUtil);
      autismclient.gui.ui.layout.ButtonNode macrosButton = this.init(
         "Macros", autismclient.gui.f.a.DataUtil.sync, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, () -> {
            AutismMacroEditorOverlay macroEditorOverlay = AutismMacroEditorOverlay.getInstance();
            if (macroEditorOverlay != null && macroEditorOverlay.isVisible()) {
               if (this.init_AutismMacroListOve != null) {
                  this.init_AutismMacroListOve.setVisible(false);
               }

               OverlayManager.getInstance().bringToFront(macroEditorOverlay);
            } else if (this.init_AutismMacroListOve != null) {
               this.init_V_cX(this.init_AutismMacroListOve);
            }
         }
      );
      if (this.write_Z) {
         this.init_V_PacketUtil(
            this.init_DataUtil.getContent(),
            macrosButton,
            this.init("Fabricator", autismclient.gui.f.a.DataUtil.flush, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, () -> {
               if (this.init_AutismFabricatorOv != null) {
                  this.init_V_cX(this.init_AutismFabricatorOv);
               }
            })
         );
      } else {
         this.init_DataUtil.getContent().addChild(macrosButton);
      }

      this.init_V_PacketUtil(
         this.init_DataUtil.getContent(),
         this.init("LAN Sync", autismclient.gui.f.a.DataUtil.update, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, () -> {
            if (this.init_AutismLANSyncOverl != null) {
               this.init_V_cX(this.init_AutismLANSyncOverl);
            }
         }),
         this.init("Queue", autismclient.gui.f.a.DataUtil.poll, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, () -> {
            if (this.init_AutismQueueEditorO != null) {
               this.init_V_cX(this.init_AutismQueueEditorO);
            }
         })
      );
      this.init_V_PacketUtil(
         this.init_DataUtil.getContent(),
         this.init("Logger", autismclient.gui.f.a.DataUtil.open, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, () -> {
            AutismPacketLoggerOverlay overlay = this.init_AutismPacketLogger();
            this.init_V_cX(overlay);
         }),
         this.init("Packets", autismclient.gui.f.a.DataUtil.close, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, () -> {
            if (this.init_AutismCustomFilter != null) {
               this.init_V_cX(this.init_AutismCustomFilter);
            }
         })
      );
      this.init_V_PacketUtil(
         this.init_DataUtil.getContent(),
         this.init("Server Info", autismclient.gui.f.a.DataUtil.push, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, () -> {
            AutismServerInfoOverlay overlay = this.init_AutismServerInfoOv();
            this.init_V_cX(overlay);
         }),
         this.init("Settings", autismclient.gui.f.a.DataUtil.write, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, () -> {
            if (this.init_AutismKeybindOverl != null) {
               this.init_V_cX(this.init_AutismKeybindOverl);
            }
         })
      );
      if (!AutismLiteVariant.enabled()) {
         this.init_V_PacketUtil(
            this.init_DataUtil.getContent(),
            this.init("AI Assistant", autismclient.gui.f.a.DataUtil.get, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, () -> {
               if (!AutismLiteVariant.enabled()) {
                  autismclient.ai.AiAssistant.getInstance().read();
               }
            }),
            this.init("Schematics", autismclient.gui.f.a.DataUtil.clear, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, () -> {
               if (!AutismLiteVariant.enabled()) {
                  autismclient.ai.SchematicWorkspace.toggleWindow();
               }
            })
         );
      }

      this.init_V_Str_Identifier("PACKET", autismclient.gui.f.a.DataUtil.tick);
      this.init_ByteReader = this.init("Send", null, autismclient.gui.ui.layout.ButtonNode.Variant.DANGER, this::update_V_9).setConnectedToggle(true);
      this.load_ByteReader = this.init("Delay", null, autismclient.gui.ui.layout.ButtonNode.Variant.DANGER, this::sync_V_8).setConnectedToggle(true);
      this.init_V_PacketUtil(this.init_DataUtil.getContent(), this.init_ByteReader, this.load_ByteReader);
      this.init_V_PacketUtil(
         this.init_DataUtil.getContent(),
         this.init("Flush", null, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, this::flush_V_8),
         this.init("Clear", null, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, this::close_V_7)
      );
      if (this.update_Z) {
         this.init_V_Str_Identifier("SCREEN", autismclient.gui.f.a.DataUtil.run);
         this.init_V_PacketUtil(
            this.init_DataUtil.getContent(),
            this.init("Close", null, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, this.init_Runnable != null ? this.init_Runnable : LauncherQuickActions::update),
            this.init("De-sync", null, autismclient.gui.ui.layout.ButtonNode.Variant.DANGER, this.load_Runnable != null ? this.load_Runnable : this::open_V_6)
         );
         this.init_V_PacketUtil(
            this.init_DataUtil.getContent(),
            this.init("Save", null, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, this::poll_V_4),
            this.init("Load", null, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, this::set_V_4)
         );
         this.init_V_PacketUtil(
            this.init_DataUtil.getContent(),
            this.init("Title", null, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, LauncherQuickActions::open),
            this.init("Disc+Send", null, autismclient.gui.ui.layout.ButtonNode.Variant.DANGER, this::put_V_3)
         );
      }

      if (this.sync_Z) {
         this.init_BitSetUtil = new autismclient.gui.ui.widgets.TextInputWidget()
            .setCornerRadius(0)
            .setFixedHeight(this.size())
            .setPreferredWidth(this.data())
            .setMinWidth(this.data())
            .setHorizontalPadding(6)
            .setPlaceholder("Type message or /command...")
            .setHistoryNavigationEnabled(true)
            .setHistoryProvider(() -> init_Minecraft.gui != null ? init_Minecraft.gui.hud.getChat().getRecentChat() : List.of())
            .setOnSubmit(this::init_V_Str_6);
         this.init_DataUtil.getContent().addChild(this.init_BitSetUtil);
         this.init_DataUtil.getContent().addChild(new autismclient.gui.ui.layout.FixedSpacerNode(0.0F, 2.0F));
         autismclient.gui.ui.layout.RowLayout syncRow = new autismclient.gui.ui.layout.RowLayout()
            .init(this.value())
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
         this.init_DataUtil.getContent().addChild(syncRow);
      } else {
         this.init_BitSetUtil = null;
         this.init_JsonUtil = null;
         this.load_JsonUtil = null;
         this.read_JsonUtil = null;
      }

      if (!this.sync_Z && !this.init_DataUtil.getContent().getChildren().isEmpty()) {
         autismclient.gui.ui.layout.LayoutNode last = this.init_DataUtil.getContent().getChildren().getLast();
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
      if (!this.init_DataUtil.getContent().getChildren().isEmpty()) {
         this.init_DataUtil.getContent().addChild(new autismclient.gui.ui.layout.FixedSpacerNode(0.0F, this.run_I_11()));
      }

      this.init_DataUtil
         .getContent()
         .addChild(new autismclient.gui.ui.layout.IconHeaderNode(label, icon).init(this.reset()).setConnectedStyle(true));
   }

   private autismclient.gui.ui.layout.ButtonNode init(autismclient.gui.ui.layout.ColumnLayout parent, String label, Identifier icon, Runnable action) {
      autismclient.gui.ui.layout.ButtonNode button = this.init(label, icon, autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, action);
      parent.addChild(button);
      return button;
   }

   private void init_V_PacketUtil(autismclient.gui.ui.layout.ColumnLayout parent, autismclient.gui.ui.layout.ButtonNode left, autismclient.gui.ui.layout.ButtonNode right) {
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

   private autismclient.gui.ui.layout.ButtonNode init(String label, Identifier icon, autismclient.gui.ui.layout.ButtonNode.Variant variant, Runnable action) {
      autismclient.gui.ui.layout.ButtonNode button = new autismclient.gui.ui.layout.ButtonNode(label, variant, action)
         .withGrowX(false)
         .setConnectedStyle(true)
         .setConnectedEdges(autismclient.gui.ui.widgets.ConnectedButtonRenderer.write)
         .setFixedHeight(this.set_I_9())
         .setHorizontalPadding(this.put_I_3())
         .setMinWidth(this.init_I_Str_Identifier(label, icon))
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
      return this.read_Str;
   }

   @Override
   public int getMinWidth() {
      return this.push_I_13();
   }

   @Override
   public int getMinHeight() {
      return this.next();
   }

   @Override
   public OverlayBounds getBounds() {
      return new OverlayBounds(this.stop, this.next, this.prev, this.valid, this.flush_Z, !this.init_DataUtil.isExpanded());
   }

   @Override
   public void setBounds(OverlayBounds bounds) {
      if (bounds != null) {
         OverlayBounds clamped = this.init_cH_cH_2(bounds);
         this.stop = clamped.x;
         this.next = clamped.y;
         this.prev = clamped.width;
         this.valid = clamped.height;
         this.flush_Z = clamped.visible;
         if (this.init_DataUtil != null) {
            this.init_DataUtil.snapExpanded(!clamped.collapsed);
         }
      }
   }

   @Override
   public void saveBounds() {
      AutismSessionState.getInstance().setOverlayBounds(this.getOverlayId(), this.getBounds());
      AutismConfig config = AutismConfig.getGlobal();
      AutismConfig.ModuleCategoryLayout layout = config.moduleCategoryLayouts.computeIfAbsent("MAIN_MENU", ignored -> new AutismConfig.ModuleCategoryLayout());
      OverlayBounds trueGeometry = OverlayManager.getInstance().getStashedBounds(this.getOverlayId());
      int saveX = trueGeometry != null ? trueGeometry.x : this.stop;
      int saveY = trueGeometry != null ? trueGeometry.y : this.next;
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
         this.setBounds(new OverlayBounds(layout.f_x_I, layout.f_y_I, this.prev, this.valid, savedVisible, layout.collapsed || savedCollapsed));
      } else if (saved != null) {
         this.setBounds(saved);
      }
   }

   @Override
   public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      if (this.flush_Z || this.isClosing()) {
         AutismSessionState shared = AutismSessionState.getInstance();
         if (this.init_ByteReader != null) {
            boolean sending = shared.isSendPacketsEnabled();
            this.init_ByteReader.setText("Send");
            this.init_ByteReader
               .setVariant(
                  sending ? autismclient.gui.ui.layout.ButtonNode.Variant.SUCCESS : autismclient.gui.ui.layout.ButtonNode.Variant.DANGER
               );
         }

         if (this.load_ByteReader != null) {
            boolean delaying = shared.isDelayPacketsEnabled();
            this.load_ByteReader.setText("Delay");
            this.load_ByteReader
               .setVariant(
                  delaying ? autismclient.gui.ui.layout.ButtonNode.Variant.SUCCESS : autismclient.gui.ui.layout.ButtonNode.Variant.DANGER
               );
         }

         this.clear_V_3();
         this.add_V_4();
         this.prev = this.push_I_13();
         autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.createViewport();
         float uiMouseX = viewport.toUiX(mouseX);
         float uiMouseY = viewport.toUiY(mouseY);
         autismclient.gui.ui.layout.LayoutContext metrics = new autismclient.gui.ui.layout.LayoutContext(
            context, init_Minecraft.font, viewport, this.init_CharEncoder, uiMouseX, uiMouseY, delta
         );
         this.valid = Math.max(this.getMinHeight(), Math.round(this.init_StateCache.measureHeight(metrics, this.prev)));
         OverlayBounds clamped = this.init_cH_cH_2(
            new OverlayBounds(this.stop, this.next, this.prev, this.valid, this.flush_Z, this.init_DataUtil != null && !this.init_DataUtil.isExpanded())
         );
         this.stop = clamped.x;
         this.next = clamped.y;
         this.prev = clamped.width;
         this.valid = clamped.height;
         this.init_StateCache.setActive(true);
         this.init_StateCache.setBounds(this.stop, this.next, this.prev, this.valid);
         this.init_TextFormat.render(context, mouseX, mouseY, delta);
      }
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.flush_Z) {
         return false;
      } else {
         autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.createViewport();
         float uiMouseX = viewport.toUiX(mouseX);
         float uiMouseY = viewport.toUiY(mouseY);
         if (button == 0 && this.init_Z_F_F_3(uiMouseX, uiMouseY) && this.init_DataUtil != null) {
            this.close_Z = true;
            this.open_Z = false;
            this.load_F = uiMouseX - this.stop;
            this.read_F = uiMouseY - this.next;
            this.write_F = uiMouseX;
            this.update_F = uiMouseY;
            this.size = this.stop;
            this.data = this.next;
            return true;
         } else {
            return this.init_TextFormat.mouseClicked(mouseX, mouseY, button) ? true : this.containsPoint(mouseX, mouseY);
         }
      }
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (this.close_Z) {
         this.close_Z = false;
         this.saveBounds();
         return true;
      } else {
         return this.init_TextFormat.mouseReleased(mouseX, mouseY, button);
      }
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (!this.close_Z) {
         return this.init_TextFormat.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.createViewport();
      float uiMouseX = viewport.toUiX(mouseX);
      float uiMouseY = viewport.toUiY(mouseY);
      OverlayBounds clamped = this.init_cH_cH_2(
         new OverlayBounds(
            Math.round(uiMouseX - this.load_F),
            Math.round(uiMouseY - this.read_F),
            this.prev,
            this.valid,
            this.flush_Z,
            this.init_DataUtil != null && !this.init_DataUtil.isExpanded()
         )
      );
      this.stop = clamped.x;
      this.next = clamped.y;
      this.open_Z = this.open_Z
         || Math.abs(uiMouseX - this.write_F) >= 3.0F
         || Math.abs(uiMouseY - this.update_F) >= 3.0F
         || this.stop != this.size
         || this.next != this.data;
      return true;
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      return this.init_TextFormat.mouseScrolled(mouseX, mouseY, amount);
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (!this.flush_Z || this.isCollapsed()) {
         return false;
      }

      if (keyCode == 258 && this.init_BitSetUtil != null && this.init_BitSetUtil.isFocused() && this.init_BitSetUtil.getValue().isEmpty()) {
         String last = this.load_Str_6();
         if (last != null && !last.isBlank()) {
            this.init_BitSetUtil.setValue(last);
            this.init_BitSetUtil.moveCursorToEnd();
         }

         return true;
      } else {
         return this.init_TextFormat.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   @Override
   public boolean charTyped(char chr, int modifiers) {
      return this.flush_Z && !this.isCollapsed() ? this.init_TextFormat.charTyped(chr, modifiers) : false;
   }

   @Override
   public boolean isVisible() {
      return this.flush_Z;
   }

   @Override
   public void setVisible(boolean visible) {
      this.flush_Z = visible;
      if (!visible) {
         this.init_TextFormat.clearFocus();
         this.close_Z = false;
         this.open_Z = false;
      } else if (this.init_DataUtil != null) {
         this.init_DataUtil.snapExpanded(this.init_DataUtil.isExpanded());
      }
   }

   @Override
   public boolean containsPoint(double mouseX, double mouseY) {
      if (!this.flush_Z) {
         return false;
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.createViewport();
      float uiX = viewport.toUiX(mouseX);
      float uiY = viewport.toUiY(mouseY);
      return uiX >= this.stop && uiX < this.stop + this.prev && uiY >= this.next && uiY < this.next + this.valid;
   }

   @Override
   public boolean isOverHeader(double mouseX, double mouseY) {
      if (!this.flush_Z) {
         return false;
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.createViewport();
      return this.init_Z_F_F_3(viewport.toUiX(mouseX), viewport.toUiY(mouseY));
   }

   @Override
   public boolean supportsHeaderCollapseClick() {
      return true;
   }

   @Override
   public boolean isCollapsed() {
      return this.init_DataUtil != null && !this.init_DataUtil.isExpanded();
   }

   @Override
   public void setCollapsed(boolean collapsed) {
      if (this.isCollapsed() != collapsed) {
         if (this.init_DataUtil != null) {
            this.init_DataUtil.setExpanded(!collapsed);
         }

         if (collapsed) {
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
   public void clearInputFocus() {
      this.init_TextFormat.clearFocus();
   }

   private boolean init_Z_F_F_3(float uiMouseX, float uiMouseY) {
      return this.init_DataUtil != null
         && uiMouseX >= this.stop
         && uiMouseX < this.stop + this.prev
         && uiMouseY >= this.next
         && uiMouseY < this.next + this.prev();
   }

   private OverlayBounds init_cH_cH_2(OverlayBounds bounds) {
      autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.createViewport();
      int margin = 4;
      int viewportW = Math.round(viewport.width());
      int viewportH = Math.round(viewport.height());
      int availableW = Math.max(1, viewportW - margin * 2);
      int availableH = Math.max(this.prev(), viewportH - margin * 2);
      int width = Math.max(Math.min(this.getMinWidth(), availableW), Math.min(bounds.width, availableW));
      int minHeight = bounds.collapsed ? this.prev() : this.getMinHeight();
      int height = Math.max(Math.min(minHeight, availableH), Math.min(bounds.height, availableH));
      int renderedHeight = bounds.collapsed ? this.prev() : height;
      int x = Math.max(margin, Math.min(bounds.x, Math.max(margin, viewportW - margin - width)));
      int y = Math.max(margin, Math.min(bounds.y, Math.max(margin, viewportH - margin - renderedHeight)));
      return new OverlayBounds(x, y, width, height, bounds.visible, bounds.collapsed);
   }

   private int push_I_13() {
      if (init_Minecraft.font == null) {
         return this.stop();
      }

      int reloadGen = autismclient.gui.f.b.k.init_I();
      if (this.cache != -2147483648
         && this.init_Font == init_Minecraft.font
         && this.value == reloadGen
         && this.poll_Z == this.write_Z
         && this.push_Z == this.update_Z
         && this.pop_Z == this.sync_Z) {
         return this.cache;
      }

      int computed = this.pop_I_13();
      this.init_Font = init_Minecraft.font;
      this.value = reloadGen;
      this.poll_Z = this.write_Z;
      this.push_Z = this.update_Z;
      this.pop_Z = this.sync_Z;
      this.cache = computed;
      return computed;
   }

   private int pop_I_13() {
      int pairWidth = this.start_I_8();
      pairWidth = Math.max(pairWidth, this.init_I_Str_Identifier("Macros", autismclient.gui.f.a.DataUtil.sync));
      if (this.write_Z) {
         pairWidth = Math.max(pairWidth, this.init_I_Str_Identifier("Fabricator", autismclient.gui.f.a.DataUtil.flush));
      }

      pairWidth = Math.max(pairWidth, this.init_I_Str_Identifier("LAN Sync", autismclient.gui.f.a.DataUtil.update));
      pairWidth = Math.max(pairWidth, this.init_I_Str_Identifier("Queue", autismclient.gui.f.a.DataUtil.poll));
      pairWidth = Math.max(pairWidth, this.init_I_Str_Identifier("Logger", autismclient.gui.f.a.DataUtil.open));
      pairWidth = Math.max(pairWidth, this.init_I_Str_Identifier("Packets", autismclient.gui.f.a.DataUtil.close));
      pairWidth = Math.max(pairWidth, this.init_I_Str_Identifier("Server Info", autismclient.gui.f.a.DataUtil.push));
      pairWidth = Math.max(pairWidth, this.init_I_Str_Identifier("Settings", autismclient.gui.f.a.DataUtil.write));

      pairWidth = Math.max(pairWidth, this.init_I_Str_Identifier("Send", null));
      pairWidth = Math.max(pairWidth, this.init_I_Str_Identifier("Delay", null));
      pairWidth = Math.max(pairWidth, this.init_I_Str_Identifier("Flush", null));
      pairWidth = Math.max(pairWidth, this.init_I_Str_Identifier("Clear", null));
      if (this.update_Z) {
         pairWidth = Math.max(pairWidth, this.init_I_Str_Identifier("Close", null));
         pairWidth = Math.max(pairWidth, this.init_I_Str_Identifier("De-sync", null));
         pairWidth = Math.max(pairWidth, this.init_I_Str_Identifier("Save", null));
         pairWidth = Math.max(pairWidth, this.init_I_Str_Identifier("Load", null));
         pairWidth = Math.max(pairWidth, this.init_I_Str_Identifier("Title", null));
         pairWidth = Math.max(pairWidth, this.init_I_Str_Identifier("Disc+Send", null));
      }

      int chatWidth = this.sync_Z ? Math.max(this.data(), this.load_I_Str_2("Type message or /command...")) : 0;
      int headerWidth = autismclient.gui.f.b.k.init(
            init_Minecraft.font,
            "MAIN MENU",
            this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.load),
            this.init_CharEncoder.load(autismclient.gui.f.b.l.load)
         )
         + this.reset()
         + this.clear_I_3()
         + this.cache();
      int subLabelWidth = this.init_I_Str_3("PACKET");
      if (this.update_Z) {
         subLabelWidth = Math.max(subLabelWidth, this.init_I_Str_3("SCREEN"));
      }

      int contentWidth = Math.max(headerWidth, Math.max(subLabelWidth, Math.max(pairWidth * 2 + this.get_I_9(), chatWidth)));
      return Math.max(this.stop(), contentWidth + this.tick_I_11() * 2 + this.buffer());
   }

   private int init_I_Str_Identifier(String label, Identifier icon) {
      if (init_Minecraft.font == null) {
         return this.start_I_8();
      }

      int width = autismclient.gui.f.b.k.init(
            init_Minecraft.font,
            label,
            this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
            this.init_CharEncoder.load(autismclient.gui.f.b.l.read)
         )
         + this.put_I_3() * 2
         + this.queue();
      if (icon != null) {
         width += this.add_I_9() + this.clear_I_3();
      }

      return width;
   }

   private int init_I_Str_3(String label) {
      return autismclient.gui.f.b.k.init(
            init_Minecraft.font,
            label,
            this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.load),
            this.init_CharEncoder.load(autismclient.gui.f.b.l.load)
         )
         + this.reset()
         + this.clear_I_3()
         + this.queue();
   }

   private int load_I_Str_2(String placeholder) {
      return autismclient.gui.f.b.k.init(
            init_Minecraft.font,
            placeholder,
            this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
            this.init_CharEncoder.load(autismclient.gui.f.b.l.write)
         )
         + this.state()
         + this.queue();
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

   private int reset() {
      return 11;
   }

   private int start_I_8() {
      return 54;
   }

   private int stop() {
      return 120;
   }

   private int next() {
      return 56;
   }

   private int prev() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int valid() {
      return 0;
   }

   private int size() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int data() {
      return 124;
   }

   private int value() {
      return 4;
   }

   private int cache() {
      return 16;
   }

   private int state() {
      return 10;
   }

   private int queue() {
      return 2;
   }

   private int buffer() {
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

   private void init_V_Str_6(String raw) {
      String sent = LauncherQuickActions.init(raw);
      if (sent != null && this.init_BitSetUtil != null) {
         if (!sent.isEmpty()) {
            this.init_BitSetUtil.pushHistoryEntry(sent);
         }

         this.init_BitSetUtil.setValue("");
         this.init_BitSetUtil.setFocused(false);
      }
   }

   private void add_V_4() {
      if (this.init_BitSetUtil != null) {
         this.init_BitSetUtil.setPlaceholder(LauncherQuickActions.inputPlaceholder());
      }
   }

   private String load_Str_6() {
      return LauncherQuickActions.lastSentMessage();
   }

   private void clear_V_3() {
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
