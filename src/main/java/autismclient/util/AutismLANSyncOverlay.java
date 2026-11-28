package autismclient.util;

import autismclient.compat263.Compat263;
import autismclient.gui.ui.widgets.UiLayoutMath;
import autismclient.util.macro.MacroAction;
import autismclient.util.macro.MacroExecutor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

public class AutismLANSyncOverlay extends DraggableOverlayWindow {
   private static final Minecraft MC = Minecraft.getInstance();
   private static final autismclient.gui.f.b.CharEncoder THEME = new autismclient.gui.f.b.CharEncoder();
   private static AutismLANSyncOverlay instance;
   private static final int flush_I = 4;
   private static final int close_I = 12;
   private static final int open_I = 6;
   private static final int poll = 3;
   private int defaultWidth = 264;
   private int panelX = 500;
   private int panelY = 5;
   private int panelWidth = this.defaultWidth;
   private int panelHeight = 220;
   private static final int set = 14;
   private int tabHeight = 13;
   private int buttonHeight = 13;
   private int rowHeight = 14;
   private int lineHeight = 12;
   private int contentPadding = 6;
   private int inputHeight = 13;
   private int inputLabelHeight = 11;
   private int chatInputAreaHeight = this.inputHeight + this.inputLabelHeight + 4;
   private boolean dragging = false;
   private double dragOffsetX = 0.0;
   private double dragOffsetY = 0.0;
   private boolean collapsed = false;
   private boolean visible = false;
   private boolean configurationOnly = false;
   private final Font textRenderer;
   private static final String[] TAB_NAMES = new String[]{"Info", "Peers", "Macros", "Queue"};
   private int activeTab = 0;
   private int scrollOffset = 0;
   private int maxScroll = 0;
   private int visualScroll = 0;
   private boolean close_Z = false;
   private int scrollbarGrabOffset = 0;
   private final autismclient.gui.ui.widgets.ScrollOffsetController scroll = new autismclient.gui.ui.widgets.ScrollOffsetController();
   private autismclient.gui.ui.layout.SelectableListBox<AutismMacro> init_RenderState = null;
   private int localListX = 0;
   private int localListY = 0;
   private int localListWidth = 0;
   private int localListHeight = 0;
   private autismclient.gui.ui.layout.ScrollableListView init_EventBus = null;
   private int peerListX = 0;
   private int peerListY = 0;
   private int peerListWidth = 0;
   private int peerListHeight = 0;
   private int serviceCheckFrames = 0;
   private static final int flags = 10;
   private String selectedMacroName = null;
   private boolean perUserMode = false;
   private final Map<String, String> macroAssignments = new LinkedHashMap<>();
   private String expandedExecutePeer = null;
   private String selectedPeer = null;
   private final List<AutismLANSyncOverlay.ClickButton> init_List = new ArrayList<>();
   private ThemedTextInputBox inputBox;

   public AutismLANSyncOverlay(Font textRenderer) {
      this.textRenderer = textRenderer;
      this.computeLayout();
      this.panelWidth = this.defaultWidth;
      this.panelHeight = this.defaultPanelHeight();
      LanSyncManager sync = LanSyncManager.getInstance();
      sync.setOnClientJoined(() -> {});
      sync.setOnClientLeft(() -> {});
      sync.setOnSyncStateChanged(() -> {});
      sync.setOnSpreadUpdated(() -> {});
      sync.setOnPeerStateChanged(() -> {});
   }

   public static synchronized AutismLANSyncOverlay getOrCreate(Font textRenderer) {
      if (instance == null) {
         instance = new AutismLANSyncOverlay(textRenderer);
         instance.restoreState();
      }

      return instance;
   }

   @Override
   public int getMinWidth() {
      return this.defaultWidth;
   }

   @Override
   public int getMinHeight() {
      return this.minPanelHeight();
   }

   @Override
   public OverlayBounds getBounds() {
      return new OverlayBounds(this.panelX, this.panelY, this.panelWidth, this.push_I_12(), this.visible, this.collapsed);
   }

   @Override
   public void setBounds(OverlayBounds bounds) {
      if (bounds != null) {
         OverlayBounds clamped = this.clampToScreen(this, bounds);
         this.panelX = clamped.x;
         this.panelY = clamped.y;
         this.panelWidth = clamped.width;
         this.panelHeight = clamped.height;
         this.visible = clamped.visible;
         this.collapsed = clamped.collapsed;
      }
   }

   public void toggleVisible() {
      this.setVisible(!this.visible);
   }

   public void load(boolean configurationOnly) {
      this.configurationOnly = configurationOnly;
      LanSyncManager.getInstance().setConfigurationOnly(configurationOnly);
      if (configurationOnly && this.inputBox != null) {
         this.inputBox.setFocused(false);
      }
   }

   @Override
   public void setVisible(boolean visible) {
      this.visible = visible;
      if (visible) {
         LanSyncManager sync = LanSyncManager.getInstance();
         autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
         if ((this.configurationOnly || module.isActive() && module.isLanSyncEnabled()) && !sync.isRunning()) {
            sync.startService();
         }

         OverlayManager.getInstance().bringToFront(this);
      } else if (this.inputBox != null) {
         this.inputBox.setFocused(false);
      }

      this.saveState();
   }

   @Override
   public boolean isVisible() {
      return this.visible;
   }

   @Override
   public boolean isCollapsed() {
      return this.collapsed;
   }

   @Override
   public void setCollapsed(boolean collapsed) {
      if (this.collapsed != collapsed) {
         this.collapsed = collapsed;
         if (collapsed) {
            this.clearFocusAndDismissMenu();
         }

         this.saveState();
      }
   }

   @Override
   public boolean supportsHeaderCollapseClick() {
      return true;
   }

   @Override
   public boolean hasFocusedTextInput() {
      return this.inputBox != null && this.inputBox.isFocused();
   }

   @Override
   public void clearInputFocus() {
      if (this.inputBox != null && this.inputBox.isFocused()) {
         this.inputBox.setFocused(false);
      }
   }

   @Override
   public boolean containsPoint(double mouseX, double mouseY) {
      if (!this.visible) {
         return false;
      }

      int h = this.collapsed ? 14 : this.push_I_12();
      return mouseX >= this.panelX && mouseX <= this.panelX + this.panelWidth && mouseY >= this.panelY && mouseY <= this.panelY + h;
   }

   @Override
   public boolean isOverHeader(double mouseX, double mouseY) {
      if (!this.visible) {
         return false;
      }

      OverlayBounds bounds = this.getBounds();
      return mouseX >= this.panelX && mouseX <= this.panelX + this.panelWidth && mouseY >= this.panelY && mouseY <= this.panelY + 14 && !this.isOverHeaderControl(mouseX, mouseY, bounds);
   }

   private int push_I_12() {
      LanSyncManager sync = LanSyncManager.getInstance();
      if (!sync.isRunning()) {
         return 18 + this.lineHeight * 2 + 4;
      }

      if (!sync.isInSession()) {
         return sync.isSearching() ? 14 + this.set_I_8() + this.buttonHeight + 4 : 14 + this.get_I_8() + this.buttonHeight + 4;
      }

      int contentH = this.init_I_aE(sync);
      int total = 14 + this.tabHeight + 2 + contentH + 4 + this.chatInputAreaHeight;
      return Math.max(this.minPanelHeight(), Math.min(total, 500));
   }

   private int init_I_aE(LanSyncManager sync) {
      switch (this.activeTab) {
         case 0:
            int h = 4 + this.lineHeight * 3;
            if (sync.isReconnecting()) {
               h += this.lineHeight;
            }

            h += 2 + this.lineHeight;
            if (sync.getLastSpreadResult() != null) {
               h += this.lineHeight;
            }

            if (!sync.getSpreadHistory().isEmpty()) {
               h += this.lineHeight;
            }

            return h + 4 + this.buttonHeight + 4;
         case 1:
            int peerCount = sync.getConnectedClientCount();
            int h1 = 4 + this.lineHeight + this.buttonHeight + 2 + this.lineHeight + 2 + peerCount * (this.rowHeight + 1);
            if (this.selectedPeer != null) {
               h1 += this.detailHeight() + 2;
            }

            return h1 + 4;
         case 2:
            int h2 = 4 + this.buttonHeight + 4;
            if (this.perUserMode) {
               int listContentHeight = this.peerListContentHeight(sync);
               h2 += this.clampedListHeight(listContentHeight);
               h2 += 4 + this.lineHeight + this.buttonHeight + 4;
            } else {
               int macros = MacroLibraryManager.getInstance().getMacros().size();
               h2 += 12 + (macros == 0 ? this.lineHeight : this.macroListViewHeight(macros)) + 4;
               if (this.selectedMacroName != null) {
                  h2 += 3 * this.lineHeight;
               }

               h2 += this.buttonHeight + 4;
            }

            if (MacroExecutor.hasVisibleActiveRun()) {
               h2 += this.buttonHeight + 4;
            }

            h2 += 4 + this.lineHeight;
            if (this.perUserMode && !sync.getSyncedAssignmentsSender().isEmpty()) {
               h2 += this.lineHeight;
            }

            if (sync.getLastSpreadResult() != null) {
               h2 += this.lineHeight;
            }

            return h2 + 4;
         case 3:
            return 4 + this.lineHeight + this.buttonHeight + 2 + this.lineHeight * 2 + 4 + this.buttonHeight + 2 + this.buttonHeight + 2 + this.buttonHeight + 4;
         default:
            return 120;
      }
   }

   public void saveState() {
      AutismSessionState shared = AutismSessionState.getInstance();
      shared.setLanSyncOverlayVisible(this.visible);
      shared.setLanSyncOverlayX(this.panelX);
      shared.setLanSyncOverlayY(this.panelY);
      shared.setLanSyncActiveTab(this.activeTab);
      shared.setLanSyncScrollOffset(this.scrollOffset);
      shared.setLanSyncPerUserMode(this.perUserMode);
      shared.setLanSyncSelectedMacroName(this.selectedMacroName);
      shared.setLanSyncExpandedExecutePeer(this.expandedExecutePeer);
      shared.setLanSyncSelectedPeer(this.selectedPeer);
      this.saveBounds();
   }

   public void restoreState() {
      AutismSessionState shared = AutismSessionState.getInstance();
      this.loadBounds();
      this.visible = shared.isLanSyncOverlayVisible();
      this.panelX = shared.getLanSyncOverlayX();
      this.panelY = shared.getLanSyncOverlayY();
      this.activeTab = Math.max(0, Math.min(TAB_NAMES.length - 1, shared.getLanSyncActiveTab()));
      this.scrollOffset = Math.max(0, shared.getLanSyncScrollOffset());
      this.scroll.setOffsetUnclamped(this.scrollOffset);
      this.perUserMode = shared.isLanSyncPerUserMode();
      this.selectedMacroName = blankToNull(shared.getLanSyncSelectedMacroName());
      this.expandedExecutePeer = blankToNull(shared.getLanSyncExpandedExecutePeer());
      this.selectedPeer = blankToNull(shared.getLanSyncSelectedPeer());
      this.panelWidth = this.defaultWidth;
   }

   private void update_V_8() {
      this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, this.maxScroll));
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics init_PoolEntry_DataUtil_I_I(int contentY, int contentHeight) {
      int totalContentHeight = this.maxScroll + contentHeight;
      return autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(
         totalContentHeight,
         contentHeight,
         this.panelX + this.panelWidth - 6,
         contentY,
         4,
         contentHeight,
         this.scroll.tickAndClamp(0.0F, Math.max(0, totalContentHeight - contentHeight))
      );
   }

   private boolean isSessionActive(LanSyncManager sync) {
      return sync.isRunning() && sync.isInSession() && !sync.isSearching();
   }

   private int tabContentY() {
      return this.panelY + 14 + this.tabHeight + 2;
   }

   private int init_I_I_5(int panelHeight) {
      int chatTopY = this.panelY + panelHeight - this.chatInputAreaHeight;
      return Math.max(40, chatTopY - 4 - this.tabContentY());
   }

   private int innerWidth() {
      return autismclient.gui.ui.layout.PanelLayoutMath.innerWidth(this.panelWidth, this.contentPadding);
   }

   private int contentWidth(boolean hasScroll) {
      return autismclient.gui.ui.layout.PanelLayoutMath.contentWidth(this.innerWidth(), hasScroll, 12);
   }

   private int rowPitch() {
      return this.rowHeight + 1;
   }

   private int macroListViewHeight(int macroCount) {
      int visibleRows = Math.max(1, Math.min(6, macroCount));
      return Math.max(this.rowPitch(), visibleRows * this.rowPitch());
   }

   private int macroListContentHeight(int macroCount) {
      return macroCount <= 0 ? 0 : macroCount * this.rowPitch();
   }

   private int clampedListHeight(int contentHeight) {
      int maxVisibleRows = Math.max(1, 6);
      int maxHeight = maxVisibleRows * this.rowPitch();
      int clampedHeight = Math.max(this.rowPitch(), Math.min(contentHeight, maxHeight));
      return (clampedHeight + this.rowPitch() - 1) / this.rowPitch() * this.rowPitch();
   }

   private void clearListBox() {
      this.init_RenderState = null;
      this.localListX = 0;
      this.localListY = 0;
      this.localListWidth = 0;
      this.localListHeight = 0;
   }

   private boolean poll_Z_7() {
      return this.init_RenderState != null && this.localListWidth > 0 && this.localListHeight > 0;
   }

   private boolean write_Z_D_D(double mouseX, double mouseY) {
      return this.poll_Z_7() && mouseX >= this.localListX && mouseX < this.localListX + this.localListWidth && mouseY >= this.localListY && mouseY < this.localListY + this.localListHeight;
   }

   private void clearListView() {
      this.init_EventBus = null;
      this.peerListX = 0;
      this.peerListY = 0;
      this.peerListWidth = 0;
      this.peerListHeight = 0;
   }

   private boolean push_Z_6() {
      return this.init_EventBus != null && this.peerListWidth > 0 && this.peerListHeight > 0;
   }

   private boolean update_Z_D_D(double mouseX, double mouseY) {
      return this.push_Z_6() && mouseX >= this.peerListX && mouseX < this.peerListX + this.peerListWidth && mouseY >= this.peerListY && mouseY < this.peerListY + this.peerListHeight;
   }

   private void init_V_aE(LanSyncManager sync) {
      Set<String> connectedPeers = sync.getConnectedClients().keySet();

      for (String peer : connectedPeers) {
         this.macroAssignments.putIfAbsent(peer, "");
      }

      this.macroAssignments.keySet().retainAll(connectedPeers);
      Map<String, String> synced = sync.getSyncedMacroAssignments();

      for (Entry<String, String> se : synced.entrySet()) {
         if (connectedPeers.contains(se.getKey())) {
            this.macroAssignments.put(se.getKey(), se.getValue());
         }
      }
   }

   private int peerListContentHeight(LanSyncManager sync) {
      this.init_V_aE(sync);
      int contentHeight = 0;

      for (Entry<String, String> entry : new ArrayList<>(this.macroAssignments.entrySet())) {
         String peer = entry.getKey();
         contentHeight += this.rowPitch();
         if (peer.equals(this.expandedExecutePeer)) {
            contentHeight += this.init_I_aE_Str(sync, peer) + 4;
         }
      }

      return Math.max(this.rowPitch(), contentHeight);
   }

   private int init_I_aE_Str(LanSyncManager sync, String peer) {
      int contentHeight = 0;
      List<AutismMacro> myMacroList = MacroLibraryManager.getInstance().getMacros();
      Set<String> myMacroKeys = new LinkedHashSet<>();

      for (AutismMacro macro : myMacroList) {
         myMacroKeys.add(MacroNameNormalizer.init(macro.name));
      }

      if (peer.equals(sync.getLocalUsername())) {
         return myMacroList.size() * this.rowPitch();
      }

      Map<String, Map<String, AutismMacro>> allRemote = sync.getPeerMacros();
      Map<String, AutismMacro> peerMacroData = allRemote.getOrDefault(peer, Collections.emptyMap());
      Set<String> peerMacroKeys = new LinkedHashSet<>();

      for (AutismMacro macro : peerMacroData.values()) {
         if (macro != null) {
            peerMacroKeys.add(MacroNameNormalizer.init(macro.name));
         }
      }

      List<String> shared = new ArrayList<>();
      Map<String, Integer> different = new LinkedHashMap<>();
      List<String> peerOnly = new ArrayList<>();
      List<String> mineOnly = new ArrayList<>();

      for (String key : peerMacroKeys) {
         if (myMacroKeys.contains(key)) {
            int diffs = countDifferences(MacroLibraryManager.getInstance().findByName(key), peerMacroData.get(key));
            if (diffs == 0) {
               shared.add(key);
            } else {
               different.put(key, diffs);
            }
         } else {
            peerOnly.add(key);
         }
      }

      for (String key : myMacroKeys) {
         if (!peerMacroKeys.contains(key)) {
            mineOnly.add(key);
         }
      }

      int rowPitch = this.rowPitch();
      if (!shared.isEmpty()) {
         contentHeight += shared.size() * rowPitch;
      }

      contentHeight += different.size() * rowPitch;
      if (!peerOnly.isEmpty()) {
         contentHeight += peerOnly.size() * rowPitch;
      }

      if (!mineOnly.isEmpty()) {
         contentHeight += mineOnly.size() * rowPitch;
      }

      if (shared.isEmpty() && different.isEmpty() && peerOnly.isEmpty() && mineOnly.isEmpty()) {
         contentHeight += rowPitch;
      }

      return Math.max(rowPitch, contentHeight);
   }

   private void drawText(GuiGraphicsExtractor ctx, String text, autismclient.gui.f.b.l tone, int color, int x, int y) {
      autismclient.gui.f.b.k.init(ctx, this.textRenderer, text, THEME.init_Identifier_l(tone), color, x, y, false);
   }

   private void drawButton(
      GuiGraphicsExtractor ctx,
      int x,
      int y,
      int w,
      int h,
      String label,
      autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone variant,
      boolean enabled,
      int mx,
      int my
   ) {
      this.drawButton(ctx, x, y, w, h, label, variant, enabled, autismclient.gui.f.b.l.read, mx, my);
   }

   private void drawButton(
      GuiGraphicsExtractor ctx,
      int x,
      int y,
      int w,
      int h,
      String label,
      autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone variant,
      boolean enabled,
      autismclient.gui.f.b.l tone,
      int mx,
      int my
   ) {
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(ctx, this.textRenderer, x, y, w, h, label, variant, enabled, tone, mx, my);
   }

   private static String blankToNull(String value) {
      return value != null && !value.isBlank() ? value : null;
   }

   @Override
   public void render(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
      if (this.visible || this.isClosing()) {
         LanSyncManager sync = LanSyncManager.getInstance();
         autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
         this.serviceCheckFrames++;
         if (this.serviceCheckFrames >= 10) {
            this.serviceCheckFrames = 0;
            if (!sync.isRunning() && (this.configurationOnly || module.isActive() && module.isLanSyncEnabled())) {
               sync.startService();
            }
         }

         this.init_List.clear();
         this.computeLayout();
         int panelHeight = this.push_I_12();
         OverlayBounds bounds = new OverlayBounds(this.panelX, this.panelY, this.panelWidth, panelHeight, this.visible, this.collapsed);
         this.renderWindowFrame(ctx, mouseX, mouseY, bounds, "LAN Sync", this.collapsed, this.dragging);
         boolean clipBody = this.beginBodyClip(ctx, bounds, this.collapsed);
         if (!clipBody) {
            this.renderInactiveDim(ctx, bounds, this.collapsed, this.dragging);
         } else {
            try {
               this.init_V_I_5(panelHeight);
               int contentY = this.panelY + 14;
               int chatTopY = this.panelY + panelHeight - this.chatInputAreaHeight;
               if (!sync.isRunning()) {
                  this.drawNotRunning(ctx, mouseX, mouseY, contentY);
               } else if (!sync.isInSession() && !sync.isSearching()) {
                  this.drawQuickStart(ctx, mouseX, mouseY, contentY);
               } else if (sync.isSearching()) {
                  this.drawSearching(ctx, mouseX, mouseY, contentY);
               } else {
                  this.drawTabs(ctx, mouseX, mouseY, contentY);
                  int tabContentY = this.tabContentY();
                  int actualTabContentH = this.init_I_I_5(panelHeight);
                  this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, this.maxScroll));
                  this.scroll.setOffset(this.scrollOffset, this.maxScroll);
                  this.visualScroll = this.scroll.tickAndClamp(delta, this.maxScroll);
                  this.maxScroll = 0;
                  autismclient.gui.ui.UiClipStack.getInstance().push(ctx, autismclient.gui.f.NetHelper.init(this.panelX, tabContentY, this.panelWidth, actualTabContentH));
                  switch (this.activeTab) {
                     case 0:
                        this.init_V_GuiGraphicsExtract_3(ctx, mouseX, mouseY, tabContentY, actualTabContentH);
                        break;
                     case 1:
                        this.drawPeersTab(ctx, mouseX, mouseY, tabContentY, actualTabContentH);
                        break;
                     case 2:
                        this.drawMacrosTab(ctx, mouseX, mouseY, tabContentY, actualTabContentH);
                        break;
                     case 3:
                        this.drawQueueTab(ctx, mouseX, mouseY, tabContentY, actualTabContentH);
                  }

                  autismclient.gui.ui.UiClipStack.getInstance().pop(ctx);
                  autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics = this.init_PoolEntry_DataUtil_I_I(tabContentY, actualTabContentH);
                  autismclient.gui.ui.widgets.ScrollbarMetricsHelper.render(ctx, scrollbarMetrics, scrollbarMetrics.containsTrack(mouseX, mouseY), this.close_Z);
               }

               if (sync.isRunning() && sync.isInSession()) {
                  this.init_V_GuiGraphicsExtract_3(ctx, mouseX, mouseY);
               }
            } finally {
               this.endBodyClip(ctx, clipBody);
               this.renderInactiveDim(ctx, bounds, this.collapsed, this.dragging);
            }
         }
      }
   }

   private void drawNotRunning(GuiGraphicsExtractor ctx, int mx, int my, int y) {
      int lx = this.panelX + this.contentPadding;
      this.drawText(ctx, "Not running", autismclient.gui.f.b.l.load, ThemePaletteManager.recolor(-43691, ThemePaletteManager.ColorRole.DANGER), lx, y + 4);
      this.drawText(ctx, "LAN Sync is currently stopped", autismclient.gui.f.b.l.write, UiColorPalette.textBody(), lx, y + 4 + this.lineHeight);
   }

   private int get_I_8() {
      return 4 + this.lineHeight * 2 + 4;
   }

   private int set_I_8() {
      return 4 + this.lineHeight + 4;
   }

   private void drawQuickStart(GuiGraphicsExtractor ctx, int mx, int my, int y) {
      int lx = this.panelX + this.contentPadding;
      this.drawText(ctx, "Quick Start", autismclient.gui.f.b.l.load, THEME.load(autismclient.gui.f.b.l.read), lx, y + 4);
      this.drawText(ctx, "Host creates, others search & join", autismclient.gui.f.b.l.write, UiColorPalette.textBody(), lx, y + 4 + this.lineHeight);
      int btnY = y + this.get_I_8();
      int createW = autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(
         autismclient.gui.f.b.k.init(this.textRenderer, "Create", THEME.init_Identifier_l(autismclient.gui.f.b.l.read), 0)
      );
      int searchW = autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(
         autismclient.gui.f.b.k.init(this.textRenderer, "Search", THEME.init_Identifier_l(autismclient.gui.f.b.l.read), 0)
      );
      int gap = 2;
      int totalW = createW + gap + searchW;
      int createX = this.panelX + this.contentPadding + Math.max(0, (this.panelWidth - this.contentPadding * 2 - totalW) / 2);
      int searchX = createX + createW + gap;
      this.drawButton(ctx, createX, btnY, createW, this.buttonHeight, "Create", autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY, true, mx, my);
      this.init_List.add(new AutismLANSyncOverlay.ClickButton(createX, btnY, createW, this.buttonHeight, () -> LanSyncManager.getInstance().createSession()));
      this.drawButton(ctx, searchX, btnY, searchW, this.buttonHeight, "Search", autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY, true, mx, my);
      this.init_List.add(new AutismLANSyncOverlay.ClickButton(searchX, btnY, searchW, this.buttonHeight, () -> LanSyncManager.getInstance().startSessionSearch()));
   }

   private void drawSearching(GuiGraphicsExtractor ctx, int mx, int my, int y) {
      int secondsLeft = LanSyncManager.getInstance().getSearchSecondsRemaining();
      String text = "Searching... " + secondsLeft + "s";
      int tw = autismclient.gui.f.b.k.init(this.textRenderer, text, THEME.init_Identifier_l(autismclient.gui.f.b.l.load), -22016);
      this.drawText(ctx, text, autismclient.gui.f.b.l.load, -22016, this.panelX + (this.panelWidth - tw) / 2, y + 4);
      int btnY = y + this.set_I_8();
      int cancelW = Math.min(
         this.panelWidth - this.contentPadding * 2,
         autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(
            autismclient.gui.f.b.k.init(this.textRenderer, "Cancel Search", THEME.init_Identifier_l(autismclient.gui.f.b.l.read), 0)
         )
      );
      int cancelX = this.panelX + (this.panelWidth - cancelW) / 2;
      this.drawButton(ctx, cancelX, btnY, cancelW, this.buttonHeight, "Cancel Search", autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY, true, mx, my);
      this.init_List.add(new AutismLANSyncOverlay.ClickButton(cancelX, btnY, cancelW, this.buttonHeight, () -> LanSyncManager.getInstance().cancelSessionSearch()));
   }

   private void init_V_I_5(int panelHeight) {
      if (this.inputBox == null) {
         this.inputBox = new ThemedTextInputBox(MC, this.textRenderer, this.panelX + this.contentPadding, this.panelY, this.panelWidth - this.contentPadding * 2, this.inputHeight, true);
         this.inputBox.setHistoryNavigationEnabled(true);
         this.inputBox.setSubmitHandler(message -> LanSyncManager.getInstance().sendSyncedChat(message));
      }

      int fieldX = this.panelX + this.contentPadding;
      int fieldY = this.panelY + panelHeight - this.inputHeight - 4;
      int fieldWidth = this.panelWidth - this.contentPadding * 2;
      this.inputBox.setX(fieldX);
      this.inputBox.setY(fieldY);
      this.inputBox.setWidth(fieldWidth);
      this.inputBox.setHeight(this.inputHeight);
      boolean enabled = !this.configurationOnly && LanSyncManager.getInstance().isRunning() && LanSyncManager.getInstance().isInSession();
      this.inputBox.setEditable(enabled);
      this.inputBox.setSubmitOnEnter(enabled);
      this.inputBox
         .setPlaceholder(
            Component.literal(this.configurationOnly ? "Available after joining a world" : (enabled ? "Sync message to all peers..." : "Join or create a session first"))
         );
      if (!enabled) {
         this.inputBox.setFocused(false);
      }
   }

   private void init_V_GuiGraphicsExtract_3(GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
      if (this.inputBox != null) {
         int labelY = this.inputBox.getY() - this.inputLabelHeight;
         ThemedTextHelper.init(ctx, this.textRenderer, "Sync Send", ThemedTextHelper.TextTone.MUTED, this.inputBox.getX(), labelY, false);
         this.inputBox.render(ctx, mouseX, mouseY, 0.0F);
      }
   }

   private void drawTabs(GuiGraphicsExtractor ctx, int mx, int my, int y) {
      int tabW = this.panelWidth / TAB_NAMES.length;

      for (int i = 0; i < TAB_NAMES.length; i++) {
         int tx = this.panelX + i * tabW;
         autismclient.gui.ui.widgets.ButtonDrawHelper.init(ctx, this.textRenderer, tx, y, tabW, this.tabHeight, TAB_NAMES[i], i == this.activeTab, mx, my);
         int tabIdx = i;
         this.init_List.add(new AutismLANSyncOverlay.ClickButton(tx, y, tabW, this.tabHeight, () -> {
            this.activeTab = tabIdx;
            this.scrollOffset = 0;
            this.scroll.jumpTo(0, 0);
            this.clearListBox();
            this.clearListView();
            this.saveState();
         }));
      }
   }

   private void init_V_GuiGraphicsExtract_3(GuiGraphicsExtractor ctx, int mx, int my, int y, int h) {
      LanSyncManager sync = LanSyncManager.getInstance();
      int lx = this.panelX + this.contentPadding;
      int cy = y + 4;
      int vx = lx + this.statusLabelWidth();
      this.drawText(ctx, "Session", autismclient.gui.f.b.l.write, UiColorPalette.textBody(), lx, cy);
      this.drawText(ctx, sync.getSessionId(), autismclient.gui.f.b.l.read, -1, vx, cy);
      cy += this.lineHeight;
      String role = sync.isHost() ? "Host" : "Client";
      int roleColor = sync.isHost() ? -22016 : -11141291;
      this.drawText(ctx, "Role", autismclient.gui.f.b.l.write, UiColorPalette.textBody(), lx, cy);
      this.drawText(ctx, role, autismclient.gui.f.b.l.read, roleColor, vx, cy);
      cy += this.lineHeight;
      this.drawText(ctx, "Peers", autismclient.gui.f.b.l.write, UiColorPalette.textBody(), lx, cy);
      this.drawText(ctx, String.valueOf(sync.getConnectedClientCount()), autismclient.gui.f.b.l.read, -1, vx, cy);
      cy += this.lineHeight;
      if (sync.isReconnecting()) {
         this.drawText(ctx, "Reconnecting...", autismclient.gui.f.b.l.load, -22016, lx, cy);
         cy += this.lineHeight;
      }

      cy += 2;
      LanSyncManager.SyncState state = sync.getSyncState();
      String stateStr = state.name();
      int stateColor = this.stateColor(state);
      this.drawText(ctx, "Sync", autismclient.gui.f.b.l.write, UiColorPalette.textBody(), lx, cy);
      this.drawText(ctx, stateStr, autismclient.gui.f.b.l.read, stateColor, vx, cy);
      cy += this.lineHeight;
      LanSyncManager.SpreadResult spread = sync.getLastSpreadResult();
      if (spread != null) {
         String spreadStr = spread.executionMethod == LanSyncManager.ExecutionMethod.TICK
            ? (spread.perfectTickSync ? "Tick perfect" : spread.maxTickOffset + " tick off")
            : String.format("%.1fms", spread.getSpreadMs());
         int spreadColor = this.spreadColor(spread.getSpreadMs());
         this.drawText(ctx, "Spread", autismclient.gui.f.b.l.write, UiColorPalette.textBody(), lx, cy);
         this.drawText(ctx, spreadStr, autismclient.gui.f.b.l.read, spreadColor, vx, cy);
         if (spread.lateClient != null && !spread.lateClient.isEmpty() && spread.getSpreadMs() > 0.0) {
            this.drawText(
               ctx,
               " (" + spread.lateClient + ")",
               autismclient.gui.f.b.l.write,
               UiColorPalette.textSecondary(),
               vx + autismclient.gui.f.b.k.init(this.textRenderer, spreadStr, THEME.init_Identifier_l(autismclient.gui.f.b.l.read), spreadColor),
               cy
            );
         }

         cy += this.lineHeight;
      }

      List<LanSyncManager.SpreadResult> history = sync.getSpreadHistory();
      if (!history.isEmpty()) {
         this.drawText(ctx, "History", autismclient.gui.f.b.l.write, UiColorPalette.textBody(), lx, cy);
         int dotX = vx + 2;

         for (LanSyncManager.SpreadResult sr : history) {
            int dotColor = this.spreadColor(sr.getSpreadMs());
            autismclient.gui.ui.widgets.PanelSurfaceHelper.drawAccentBar(ctx, dotX, cy + 2, 6, 6, dotColor);
            dotX += 9;
         }

         cy += this.lineHeight;
      }

      int btnY = y + h - this.buttonHeight - 4;
      this.drawButton(
         ctx,
         this.panelX + this.contentPadding,
         btnY,
         this.panelWidth - this.contentPadding * 2,
         this.buttonHeight,
         "Leave Session",
         autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY,
         true,
         mx,
         my
      );
      this.init_List
         .add(new AutismLANSyncOverlay.ClickButton(this.panelX + this.contentPadding, btnY, this.panelWidth - this.contentPadding * 2, this.buttonHeight, () -> LanSyncManager.getInstance().leaveSession()));
   }

   private int statusLabelWidth() {
      int w = 0;

      for (String label : new String[]{"Session", "Role", "Peers", "Sync", "Spread", "History"}) {
         w = Math.max(w, autismclient.gui.f.b.k.init(this.textRenderer, label, THEME.init_Identifier_l(autismclient.gui.f.b.l.write), UiColorPalette.textBody()));
      }

      return w + 6;
   }

   private int detailHeight() {
      return 4 + this.lineHeight * 2 + this.buttonHeight + 4;
   }

   private void drawPeersTab(GuiGraphicsExtractor ctx, int mx, int my, int y, int h) {
      LanSyncManager sync = LanSyncManager.getInstance();
      int lx = this.panelX + this.contentPadding;
      int cy = y + 4;
      int rowWidth = this.innerWidth() + 4;
      Map<String, LanSyncManager.ClientInfo> clients = sync.getConnectedClients();
      List<LanSyncManager.ClientInfo> sortedClients = new ArrayList<>(clients.values());
      sortedClients.sort((a, b) -> Boolean.compare(b.isHost, a.isHost));
      this.drawText(ctx, "Execution Method", autismclient.gui.f.b.l.write, UiColorPalette.textBody(), lx, cy);
      cy += this.lineHeight;
      int methodW = (rowWidth - 2) / 2;
      boolean instant = sync.getExecutionMethod() == LanSyncManager.ExecutionMethod.INSTANT;
      boolean syncIdle = sync.getSyncState() == LanSyncManager.SyncState.IDLE || sync.getSyncState() == LanSyncManager.SyncState.DONE;
      boolean canChangeMethod = syncIdle;
      this.drawButton(
         ctx,
         lx,
         cy,
         methodW,
         this.buttonHeight,
         "Instant",
         instant ? autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY,
         canChangeMethod,
         mx,
         my
      );
      if (canChangeMethod) {
         this.init_List.add(new AutismLANSyncOverlay.ClickButton(lx, cy, methodW, this.buttonHeight, () -> sync.setExecutionMethod(LanSyncManager.ExecutionMethod.INSTANT)));
      }

      int tickX = lx + methodW + 2;
      int tickW = rowWidth - methodW - 2;
      this.drawButton(
         ctx,
         tickX,
         cy,
         tickW,
         this.buttonHeight,
         "Tick",
         !instant ? autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY,
         canChangeMethod,
         mx,
         my
      );
      if (canChangeMethod) {
         this.init_List.add(new AutismLANSyncOverlay.ClickButton(tickX, cy, tickW, this.buttonHeight, () -> sync.setExecutionMethod(LanSyncManager.ExecutionMethod.TICK)));
      }

      cy += this.buttonHeight + 2;
      String methodHint = instant ? "Current: immediate GO-sync" : "Current: executes +" + sync.getTickSyncLeadTicks() + " server ticks";
      if (sync.isInSession() && !sync.isHost()) {
         methodHint = methodHint + " (host verified)";
      }

      this.drawText(ctx, methodHint, autismclient.gui.f.b.l.write, UiColorPalette.textSecondary(), lx, cy);
      cy += this.lineHeight + 2;

      for (LanSyncManager.ClientInfo client : sortedClients) {
         boolean isSelected = client.username.equals(this.selectedPeer);
         boolean isHov = this.inside(mx, my, lx - 2, cy, this.panelWidth - this.contentPadding * 2 + 4, this.rowHeight);
         autismclient.gui.ui.widgets.ListRowRenderer.drawRow(
            ctx, this.textRenderer, "", lx - 2, cy, rowWidth, this.rowHeight, isHov, isSelected, autismclient.gui.ui.widgets.ListRowRenderer.RowTone.NORMAL
         );
         autismclient.gui.ui.widgets.GlyphIndicatorRenderer.init(
            ctx,
            lx + 1,
            cy + 3,
            8,
            isSelected ? autismclient.gui.ui.widgets.GlyphIndicatorRenderer.GlyphDirection.DOWN : autismclient.gui.ui.widgets.GlyphIndicatorRenderer.GlyphDirection.RIGHT,
            isSelected ? -593937 : -1911596,
            ThemePaletteManager.recolor(-1204284392, ThemePaletteManager.ColorRole.OUTLINE),
            1.0F
         );
         LanSyncManager.PeerHealth status = sync.getPeerHealth(client.username);
         int dotColor = status == LanSyncManager.PeerHealth.HEALTHY ? -11141291 : (status == LanSyncManager.PeerHealth.STALE ? -22016 : -7829368);
         autismclient.gui.ui.widgets.PanelSurfaceHelper.drawAccentBar(ctx, lx + 10, cy + 4, 5, 5, dotColor);
         boolean isSelf = client.username.equals(sync.getLocalUsername());
         int nameColor = isHov ? -2245633 : (client.isHost ? -22016 : -1);
         int rightReserve = 4;
         if (isSelf) {
            rightReserve += autismclient.gui.f.b.k.init(this.textRenderer, " (YOU)", THEME.init_Identifier_l(autismclient.gui.f.b.l.write), UiColorPalette.textBody())
               + 2;
         }

         if (client.isHost) {
            rightReserve += autismclient.gui.f.b.k.init(this.textRenderer, " [HOST]", THEME.init_Identifier_l(autismclient.gui.f.b.l.load), -22016) + 2;
         }

         int offset = client.offsetMs;
         if (offset > 0) {
            rightReserve += autismclient.gui.f.b.k.init(
                  this.textRenderer, "+" + offset + "ms", THEME.init_Identifier_l(autismclient.gui.f.b.l.read), -11149825
               )
               + 4;
         }

         int nameMaxW = Math.max(20, rowWidth - 18 - rightReserve);
         String displayName = ThemedTextHelper.init(this.textRenderer, client.username, nameMaxW, ThemedTextHelper.TextTone.BODY);
         autismclient.gui.f.b.k.init(
            ctx, this.textRenderer, displayName, THEME.init_Identifier_l(autismclient.gui.f.b.l.read), nameColor, lx + 18, cy + 3, false
         );
         int badgeX = lx
            + 18
            + autismclient.gui.f.b.k.init(this.textRenderer, displayName, THEME.init_Identifier_l(autismclient.gui.f.b.l.read), nameColor)
            + 4;
         if (isSelf) {
            this.drawText(ctx, "(YOU)", autismclient.gui.f.b.l.write, UiColorPalette.textBody(), badgeX, cy + 3);
            badgeX += autismclient.gui.f.b.k.init(this.textRenderer, "(YOU)", THEME.init_Identifier_l(autismclient.gui.f.b.l.write), UiColorPalette.textBody()) + 4;
         }

         if (client.isHost) {
            this.drawText(ctx, "[HOST]", autismclient.gui.f.b.l.load, -22016, badgeX, cy + 3);
         }

         if (offset > 0) {
            String offsetStr = "+" + offset + "ms";
            int ofw = autismclient.gui.f.b.k.init(this.textRenderer, offsetStr, THEME.init_Identifier_l(autismclient.gui.f.b.l.read), -11149825);
            this.drawText(ctx, offsetStr, autismclient.gui.f.b.l.read, -11149825, this.panelX + this.panelWidth - this.contentPadding - ofw, cy + 3);
         }

         String peerName = client.username;
         this.init_List.add(new AutismLANSyncOverlay.ClickButton(lx - 2, cy, rowWidth, this.rowHeight, () -> {
            this.selectedPeer = peerName.equals(this.selectedPeer) ? null : peerName;
            this.saveState();
         }));
         cy += this.rowHeight + 1;
         if (isSelected) {
            int detailH = this.detailHeight();
            autismclient.gui.ui.widgets.PanelSurfaceHelper.drawElevatedPanel(ctx, lx + 4, cy, this.panelX + this.panelWidth - this.contentPadding - lx - 8, detailH, UiColorPalette.inputSurface());
            int dx = lx + 10;
            int dy = cy + 4;
            this.drawText(ctx, "Status: " + status.name(), autismclient.gui.f.b.l.read, status == LanSyncManager.PeerHealth.HEALTHY ? -11141291 : -22016, dx, dy);
            dy += this.lineHeight;
            this.drawText(ctx, isSelf ? "(This is you)" : "Connected peer", autismclient.gui.f.b.l.write, UiColorPalette.textSecondary(), dx, dy);
            dy += this.lineHeight;
            int btnS = this.buttonHeight;
            String delayLabel = "Exec delay";
            this.drawText(ctx, delayLabel, autismclient.gui.f.b.l.write, UiColorPalette.textBody(), dx, UiLayoutMath.centerTextY(dy, btnS, 9, 0));
            int ctrlX = dx
               + autismclient.gui.f.b.k.init(this.textRenderer, delayLabel, THEME.init_Identifier_l(autismclient.gui.f.b.l.write), UiColorPalette.textBody())
               + 6;
            autismclient.gui.ui.widgets.IconButtonRenderer.init(
               ctx,
               this.textRenderer,
               autismclient.gui.f.NetHelper.init(ctrlX, dy, btnS, btnS),
               "-",
               mx >= ctrlX && mx < ctrlX + btnS && my >= dy && my < dy + btnS,
               true,
               false
            );
            this.init_List.add(new AutismLANSyncOverlay.ClickButton(ctrlX, dy, btnS, btnS, () -> {
               int cur = sync.getClientOffsetMs(peerName);
               sync.setClientOffsetMs(peerName, Math.max(0, cur - 1));
            }));
            ctrlX += btnS + 2;
            String valStr = offset + "ms";
            int valW = autismclient.gui.f.b.k.init(this.textRenderer, valStr, THEME.init_Identifier_l(autismclient.gui.f.b.l.read), -1) + 8;
            autismclient.gui.ui.widgets.PanelSurfaceHelper.drawInputPill(ctx, ctrlX, dy, valW, btnS);
            this.drawText(ctx, valStr, autismclient.gui.f.b.l.read, -1, ctrlX + 4, UiLayoutMath.centerTextY(dy, btnS, 9, 0));
            ctrlX += valW + 2;
            autismclient.gui.ui.widgets.IconButtonRenderer.init(
               ctx,
               this.textRenderer,
               autismclient.gui.f.NetHelper.init(ctrlX, dy, btnS, btnS),
               "+",
               mx >= ctrlX && mx < ctrlX + btnS && my >= dy && my < dy + btnS,
               true,
               false
            );
            this.init_List.add(new AutismLANSyncOverlay.ClickButton(ctrlX, dy, btnS, btnS, () -> {
               int cur = sync.getClientOffsetMs(peerName);
               sync.setClientOffsetMs(peerName, cur + 1);
            }));
            ctrlX += btnS + 4;
            int resetW = autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(
               autismclient.gui.f.b.k.init(
                  this.textRenderer, "Reset", THEME.init_Identifier_l(autismclient.gui.f.b.l.read), THEME.load(autismclient.gui.f.b.l.read)
               )
            );
            this.drawButton(ctx, ctrlX, dy, resetW, btnS, "Reset", autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY, true, mx, my);
            this.init_List.add(new AutismLANSyncOverlay.ClickButton(ctrlX, dy, resetW, btnS, () -> sync.setClientOffsetMs(peerName, 0)));
            cy += detailH + 2;
         }
      }

      this.maxScroll = 0;
      this.update_V_8();
   }

   private void drawQueueTab(GuiGraphicsExtractor ctx, int mx, int my, int y, int h) {
      LanSyncManager sync = LanSyncManager.getInstance();
      int lx = this.panelX + this.contentPadding;
      int cy = y + 4;
      AutismSessionState state = AutismSessionState.getInstance();
      int queueSize = state.getDelayedPackets() != null ? state.getDelayedPackets().size() : 0;
      int btnW = this.panelWidth - this.contentPadding * 2;
      this.drawText(ctx, "Execution Method", autismclient.gui.f.b.l.write, UiColorPalette.textBody(), lx, cy);
      cy += this.lineHeight;
      int methodW = (btnW - 2) / 2;
      boolean instant = sync.getExecutionMethod() == LanSyncManager.ExecutionMethod.INSTANT;
      boolean syncIdle = sync.getSyncState() == LanSyncManager.SyncState.IDLE || sync.getSyncState() == LanSyncManager.SyncState.DONE;
      this.drawButton(
         ctx,
         lx,
         cy,
         methodW,
         this.buttonHeight,
         "Instant",
         instant ? autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY,
         syncIdle,
         mx,
         my
      );
      if (syncIdle) {
         this.init_List.add(new AutismLANSyncOverlay.ClickButton(lx, cy, methodW, this.buttonHeight, () -> sync.setExecutionMethod(LanSyncManager.ExecutionMethod.INSTANT)));
      }

      int tickX = lx + methodW + 2;
      int tickW = btnW - methodW - 2;
      this.drawButton(
         ctx,
         tickX,
         cy,
         tickW,
         this.buttonHeight,
         "Tick",
         !instant ? autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY,
         syncIdle,
         mx,
         my
      );
      if (syncIdle) {
         this.init_List.add(new AutismLANSyncOverlay.ClickButton(tickX, cy, tickW, this.buttonHeight, () -> sync.setExecutionMethod(LanSyncManager.ExecutionMethod.TICK)));
      }

      cy += this.buttonHeight + 2;
      this.drawText(ctx, "My Queue", autismclient.gui.f.b.l.load, -1, lx, cy);
      cy += this.lineHeight;
      this.drawText(ctx, queueSize + " packets queued", autismclient.gui.f.b.l.read, queueSize > 0 ? -11141291 : UiColorPalette.textSecondary(), lx, cy);
      cy += this.lineHeight + 4;
      boolean delayOn = state.isDelayPacketsEnabled();
      String delayLabel = delayOn ? "Sync Delay OFF" : "Sync Delay ON";
      this.drawButton(
         ctx,
         lx,
         cy,
         btnW,
         this.buttonHeight,
         this.configurationOnly ? "Delay: In Game" : delayLabel,
         delayOn ? autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.DANGER : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS,
         syncIdle && !this.configurationOnly,
         mx,
         my
      );
      if (syncIdle && !this.configurationOnly) {
         this.init_List.add(new AutismLANSyncOverlay.ClickButton(lx, cy, btnW, this.buttonHeight, () -> sync.triggerDelayPacketsSync(!AutismSessionState.getInstance().isDelayPacketsEnabled())));
      }

      cy += this.buttonHeight + 2;
      this.drawButton(ctx, lx, cy, btnW, this.buttonHeight, "Share Queue", autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY, !this.configurationOnly, mx, my);
      if (!this.configurationOnly) {
         this.init_List.add(new AutismLANSyncOverlay.ClickButton(lx, cy, btnW, this.buttonHeight, () -> {
            try {
               String queueData = MacroClipboardCodec.encodeQueue(AutismSessionState.getInstance().getDelayedPackets());
               sync.broadcastQueueData(queueData);
               AutismClientMessaging.sendPrefixed("§aQueue broadcasted to " + (sync.getConnectedClientCount() - 1) + " peers");
            } catch (Exception e) {
               AutismClientMessaging.sendPrefixed("§cFailed to broadcast queue");
            }
         }));
      }

      cy += this.buttonHeight + 2;
      this.drawButton(
         ctx,
         lx,
         cy,
         btnW,
         this.buttonHeight,
         this.configurationOnly ? "Execute: In Game" : "Sync Execute",
         autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY,
         syncIdle && !this.configurationOnly,
         mx,
         my
      );
      if (syncIdle && !this.configurationOnly) {
         this.init_List.add(new AutismLANSyncOverlay.ClickButton(lx, cy, btnW, this.buttonHeight, () -> sync.triggerQueueFlushSync()));
      }

      this.maxScroll = 0;
   }

   private void drawMacrosTab(GuiGraphicsExtractor ctx, int mx, int my, int y, int h) {
      LanSyncManager sync = LanSyncManager.getInstance();
      int lx = this.panelX + this.contentPadding;
      int cy = y + 4;
      int btnW = this.panelWidth - this.contentPadding * 2;
      String modeLabel = this.perUserMode ? "Mode: Per-User" : "Mode: Shared Macro";
      this.drawButton(ctx, lx, cy, btnW, this.buttonHeight, modeLabel, autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY, true, mx, my);
      this.init_List.add(new AutismLANSyncOverlay.ClickButton(lx, cy, btnW, this.buttonHeight, () -> {
         this.perUserMode = !this.perUserMode;
         this.expandedExecutePeer = null;
         this.scrollOffset = 0;
         this.scroll.jumpTo(0, 0);
         this.clearListBox();
         this.clearListView();
         if (this.perUserMode) {
            this.macroAssignments.clear();
            Map<String, String> synced = sync.getSyncedMacroAssignments();

            for (String peer : sync.getConnectedClients().keySet()) {
               this.macroAssignments.put(peer, synced.getOrDefault(peer, ""));
            }
         }

         this.saveState();
      }));
      cy += this.buttonHeight + 4;
      if (this.perUserMode) {
         this.clearListBox();
         cy = this.drawPeerMacros(ctx, mx, my, cy, lx, btnW, sync);
      } else {
         this.clearListView();
         cy = this.drawLocalMacros(ctx, mx, my, cy, lx, btnW, sync);
      }

      boolean anyRunning = MacroExecutor.hasVisibleActiveRun();
      if (anyRunning && !this.configurationOnly) {
         this.drawButton(ctx, lx, cy, btnW, this.buttonHeight, "Sync Stop All", autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.DANGER, true, mx, my);
         this.init_List.add(new AutismLANSyncOverlay.ClickButton(lx, cy, btnW, this.buttonHeight, () -> sync.stopAllMacrosOnLan()));
         cy += this.buttonHeight + 4;
      }

      autismclient.gui.ui.widgets.PanelSurfaceHelper.drawDivider(ctx, lx - 2, cy, this.panelX + this.panelWidth - this.contentPadding - lx + 4);
      cy += 4;
      LanSyncManager.SyncState state = sync.getSyncState();
      String stateStr = this.stateLabel(state, sync);
      int stateColor = this.stateColor(state);
      this.drawText(ctx, "Status: " + stateStr, autismclient.gui.f.b.l.read, stateColor, lx, cy);
      cy += this.lineHeight;
      String setBy = sync.getSyncedAssignmentsSender();
      if (this.perUserMode && !setBy.isEmpty()) {
         this.drawText(ctx, "Assigned by: " + setBy, autismclient.gui.f.b.l.write, UiColorPalette.textSecondary(), lx, cy);
         cy += this.lineHeight;
      }

      LanSyncManager.SpreadResult spread = sync.getLastSpreadResult();
      if (spread != null) {
         String spreadStr = spread.executionMethod == LanSyncManager.ExecutionMethod.TICK
            ? (
               spread.perfectTickSync
                  ? "Last: perfect tick " + spread.targetTick + " (" + spread.clientCount + " clients)"
                  : "Last: " + spread.maxTickOffset + " tick off @ " + spread.targetTick + " (" + spread.clientCount + " clients)"
            )
            : String.format("Last spread: %.1fms (%d clients)", spread.getSpreadMs(), spread.clientCount);
         this.drawText(ctx, spreadStr, autismclient.gui.f.b.l.read, this.spreadColor(spread.getSpreadMs()), lx, cy);
         cy += this.lineHeight;
      }

      this.maxScroll = 0;
      this.update_V_8();
   }

   private int drawLocalMacros(GuiGraphicsExtractor ctx, int mx, int my, int cy, int lx, int btnW, LanSyncManager sync) {
      autismclient.gui.ui.widgets.ListRowRenderer.drawHeaderTitle(ctx, this.textRenderer, "Local Macros", lx, cy);
      cy += 12;
      List<AutismMacro> localMacros = MacroLibraryManager.getInstance().getMacros();
      if (localMacros.isEmpty()) {
         this.clearListBox();
         this.drawText(ctx, "No macros", autismclient.gui.f.b.l.write, UiColorPalette.textSecondary(), lx, cy);
         cy += this.lineHeight;
      } else {
         this.localListX = lx - 2;
         this.localListY = cy;
         this.localListWidth = this.innerWidth() + 4;
         this.localListHeight = this.macroListViewHeight(localMacros.size());
         if (this.init_RenderState == null
            || this.init_RenderState.getX() != this.localListX
            || this.init_RenderState.getY() != this.localListY
            || this.init_RenderState.update() != this.localListWidth
            || this.init_RenderState.sync() != this.localListHeight) {
            this.init_RenderState = new autismclient.gui.ui.layout.SelectableListBox<>(
               this.localListX, this.localListY, this.localListWidth, this.localListHeight, this.rowPitch(), 3, macro -> macro.name, (macro, selected) -> {
                  if (selected) {
                     this.selectedMacroName = macro.name;
                  }
               }
            );
            this.init_RenderState.init(macro -> {
               int actionCount = macro.actions != null ? macro.actions.size() : 0;
               return "(" + actionCount + ")";
            });
            this.init_RenderState.select(-1);
         }

         this.init_RenderState.init(localMacros);

         for (int i = 0; i < localMacros.size(); i++) {
            if (localMacros.get(i).name.equals(this.selectedMacroName)) {
               this.init_RenderState.select(i);
               break;
            }
         }

         this.init_RenderState.init(ctx, this.textRenderer, mx, my);
         cy += this.localListHeight;
      }

      cy += 4;
      AutismLANSyncOverlay.MacroAvailability syncStatus = this.macroAvailability(sync, this.selectedMacroName);
      if (this.selectedMacroName != null) {
         String selectedLine = ThemedTextHelper.init(this.textRenderer, "Selected: " + this.selectedMacroName, btnW, ThemedTextHelper.TextTone.BODY);
         this.drawText(ctx, selectedLine, autismclient.gui.f.b.l.read, -1, lx, cy);
         cy += this.lineHeight;
         if (!syncStatus.missingPeers.isEmpty()) {
            String missingLine = ThemedTextHelper.init(this.textRenderer, "Missing on: " + this.joinNames(syncStatus.missingPeers), btnW, ThemedTextHelper.TextTone.BODY);
            this.drawText(ctx, missingLine, autismclient.gui.f.b.l.read, ThemePaletteManager.recolor(-38037, ThemePaletteManager.ColorRole.DANGER), lx, cy);
            cy += this.lineHeight;
         }

         if (!syncStatus.differentPeers.isEmpty()) {
            String differentLine = ThemedTextHelper.init(this.textRenderer, "Different on: " + this.formatDifferences(syncStatus.differentPeers), btnW, ThemedTextHelper.TextTone.BODY);
            this.drawText(ctx, differentLine, autismclient.gui.f.b.l.read, -14249, lx, cy);
            cy += this.lineHeight;
         }

         if (syncStatus.missingPeers.isEmpty() && syncStatus.differentPeers.isEmpty()) {
            String readyLine = sync.getConnectedClientCount() > 1 ? "All peers have this macro" : "Ready to execute";
            this.drawText(ctx, readyLine, autismclient.gui.f.b.l.read, -9837157, lx, cy);
            cy += this.lineHeight;
         } else if (syncStatus.missingPeers.isEmpty()) {
            this.drawText(ctx, "Execution allowed in Same Macro mode", autismclient.gui.f.b.l.read, -9837157, lx, cy);
            cy += this.lineHeight;
         }
      }

      int shareW = autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(
         autismclient.gui.f.b.k.init(this.textRenderer, "Send To Peers", THEME.init_Identifier_l(autismclient.gui.f.b.l.read), UiColorPalette.textBright())
      );
      int executeX = lx + shareW + 2;
      int executeW = btnW - shareW - 2;
      boolean shareEnabled = this.selectedMacroName != null && sync.getConnectedClientCount() > 1;
      boolean executeEnabled = !this.configurationOnly && this.selectedMacroName != null && syncStatus.allPeersPresent();
      if (shareEnabled) {
         this.drawButton(ctx, lx, cy, shareW, this.buttonHeight, "Send To Peers", autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS, true, mx, my);
         this.init_List.add(new AutismLANSyncOverlay.ClickButton(lx, cy, shareW, this.buttonHeight, () -> {
            if (this.selectedMacroName != null) {
               sync.shareMacroWithPeers(this.selectedMacroName);
            }
         }));
      } else {
         this.drawButton(ctx, lx, cy, shareW, this.buttonHeight, "Send To Peers", autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS, false, mx, my);
      }

      String executeLabel;
      if (this.selectedMacroName == null) {
         executeLabel = "Select a macro";
      } else if (this.configurationOnly) {
         executeLabel = "Execute: In Game";
      } else if (!executeEnabled) {
         executeLabel = "Need all peers to have it";
      } else {
         executeLabel = "Sync Execute";
      }

      if (executeEnabled) {
         this.drawButton(ctx, executeX, cy, executeW, this.buttonHeight, executeLabel, autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY, true, mx, my);
         this.init_List.add(new AutismLANSyncOverlay.ClickButton(executeX, cy, executeW, this.buttonHeight, () -> {
            if (this.selectedMacroName != null) {
               sync.triggerMacroSync(this.selectedMacroName);
            }
         }));
      } else {
         this.drawButton(ctx, executeX, cy, executeW, this.buttonHeight, executeLabel, autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY, false, mx, my);
      }

      return cy + this.buttonHeight + 4;
   }

   private int drawPeerMacros(GuiGraphicsExtractor ctx, int mx, int my, int cy, int lx, int btnW, LanSyncManager sync) {
      this.init_V_aE(sync);
      int rowW = this.innerWidth() + 4;
      this.peerListX = lx - 2;
      this.peerListY = cy;
      this.peerListWidth = rowW;
      int perUserListContentHeight = this.peerListContentHeight(sync);
      this.peerListHeight = this.clampedListHeight(perUserListContentHeight);
      if (this.init_EventBus == null
         || this.init_EventBus.getX() != this.peerListX
         || this.init_EventBus.getY() != this.peerListY
         || this.init_EventBus.getWidth() != this.peerListWidth
         || this.init_EventBus.getHeight() != this.peerListHeight) {
         this.init_EventBus = new autismclient.gui.ui.layout.ScrollableListView(this.peerListX, this.peerListY, this.peerListWidth, this.peerListHeight, this.rowPitch(), 3);
      }

      this.init_EventBus.setContentHeight(perUserListContentHeight);
      int drawScroll = this.init_EventBus.getScrollOffset();
      int clipTop = this.peerListY + 1;
      int clipBottom = this.peerListY + this.peerListHeight - 1;
      int rowPitch = this.init_EventBus.getRowHeight();
      autismclient.gui.f.PacketUtil.init(
         ctx, autismclient.gui.f.NetHelper.init(this.peerListX, this.peerListY, this.peerListWidth, this.peerListHeight), THEME.reset(), THEME.set()
      );
      autismclient.gui.ui.UiClipStack.getInstance()
         .push(ctx, autismclient.gui.f.NetHelper.init(this.peerListX + 1, this.peerListY + 1, Math.max(0, this.peerListWidth - 2), Math.max(0, this.peerListHeight - 2)));

      try {
         cy = this.peerListY - drawScroll;

         for (Entry<String, String> entry : new ArrayList<>(this.macroAssignments.entrySet())) {
            String peer = entry.getKey();
            String assigned = entry.getValue();
            boolean isExpanded = peer.equals(this.expandedExecutePeer);
            boolean isMe = peer.equals(sync.getLocalUsername());
            boolean rowVisible = cy + rowPitch > clipTop && cy < clipBottom;
            boolean rowHov = rowVisible && this.inside(mx, my, lx - 2, cy, rowW, rowPitch);
            String peerLabel = peer + (isMe ? " (you)" : "");
            String assignLabel = assigned.isEmpty() ? "(none)" : assigned;
            int assignColor = assigned.isEmpty() ? UiColorPalette.textSecondary() : -11141291;
            int maxAssignW = Math.min(
               (rowW - 12) / 2,
               autismclient.gui.f.b.k.init(this.textRenderer, assignLabel, THEME.init_Identifier_l(autismclient.gui.f.b.l.read), assignColor)
            );
            int peerNameMaxW = Math.max(20, rowW - 10 - maxAssignW - 8 - 12);
            if (rowVisible) {
               int rowTop = Math.max(cy, clipTop);
               int rowBottom = Math.min(cy + rowPitch, clipBottom);
               if (rowBottom > rowTop) {
                  autismclient.gui.ui.widgets.PanelSurfaceHelper.drawCardRow(ctx, this.peerListX + 1, rowTop, this.peerListWidth - 2, rowBottom - rowTop, rowHov, isExpanded);
                  int chevronY = Math.max(cy + 3, clipTop + 1);
                  if (chevronY < clipBottom - 10) {
                     autismclient.gui.ui.widgets.GlyphIndicatorRenderer.init(
                        ctx,
                        lx + 1,
                        chevronY,
                        8,
                        isExpanded ? autismclient.gui.ui.widgets.GlyphIndicatorRenderer.GlyphDirection.DOWN : autismclient.gui.ui.widgets.GlyphIndicatorRenderer.GlyphDirection.RIGHT,
                        isExpanded ? -593937 : -1911596,
                        ThemePaletteManager.recolor(-1204284392, ThemePaletteManager.ColorRole.OUTLINE),
                        1.0F
                     );
                  }

                  int textY = Math.max(cy + 3, clipTop + 1);
                  if (textY < clipBottom - 2) {
                     String truncPeerLabel = ThemedTextHelper.init(this.textRenderer, peerLabel, peerNameMaxW, ThemedTextHelper.TextTone.BODY);
                     this.drawText(ctx, truncPeerLabel, autismclient.gui.f.b.l.read, isMe ? UiColorPalette.textBody() : -1, lx + 10, textY);
                     String truncAssign = ThemedTextHelper.init(this.textRenderer, assignLabel, maxAssignW, ThemedTextHelper.TextTone.BODY);
                     int truncAssignW = autismclient.gui.f.b.k.init(
                        this.textRenderer, truncAssign, THEME.init_Identifier_l(autismclient.gui.f.b.l.read), assignColor
                     );
                     this.drawText(
                        ctx, truncAssign, autismclient.gui.f.b.l.read, assignColor, autismclient.gui.ui.layout.PanelLayoutMath.alignRightX(lx, rowW - 12, truncAssignW, 4), textY
                     );
                  }
               }
            }

            String peerKey = peer;
            if (rowVisible) {
               this.init_List.add(new AutismLANSyncOverlay.ClickButton(lx - 2, cy, rowW, rowPitch, () -> {
                  this.expandedExecutePeer = peerKey.equals(this.expandedExecutePeer) ? null : peerKey;
                  this.saveState();
               }));
            }

            cy += rowPitch;
            if (isExpanded) {
               int detailLx = lx + 6;
               int detailRx = this.panelX + this.panelWidth - this.contentPadding - 4;
               List<AutismMacro> myMacroList = MacroLibraryManager.getInstance().getMacros();
               Map<String, String> displayNames = new LinkedHashMap<>();
               Set<String> myMacroKeys = new LinkedHashSet<>();

               for (AutismMacro m : myMacroList) {
                  String key = MacroNameNormalizer.init(m.name);
                  myMacroKeys.add(key);
                  displayNames.put(key, m.name);
               }

               if (isMe) {
                  for (AutismMacro macro : myMacroList) {
                     cy = this.drawMacroDetailRow(
                        ctx, mx, my, cy, detailLx, detailRx, rowW, macro.name, assigned, null, peerKey, sync, false, clipTop, clipBottom, rowPitch
                     );
                  }
               } else {
                  Map<String, Map<String, AutismMacro>> allRemote = sync.getPeerMacros();
                  Map<String, AutismMacro> peerMacroData = allRemote.getOrDefault(peer, Collections.emptyMap());
                  Set<String> peerMacroKeys = new LinkedHashSet<>();

                  for (AutismMacro m : peerMacroData.values()) {
                     if (m != null) {
                        String key = MacroNameNormalizer.init(m.name);
                        peerMacroKeys.add(key);
                        displayNames.putIfAbsent(key, m.name);
                     }
                  }

                  List<String> shared = new ArrayList<>();
                  Map<String, Integer> different = new LinkedHashMap<>();
                  List<String> peerOnly = new ArrayList<>();
                  List<String> mineOnly = new ArrayList<>();

                  for (String key : peerMacroKeys) {
                     String name = displayNames.getOrDefault(key, key);
                     if (myMacroKeys.contains(key)) {
                        int diffs = countDifferences(MacroLibraryManager.getInstance().findByName(key), peerMacroData.get(key));
                        if (diffs == 0) {
                           shared.add(name);
                        } else {
                           different.put(name, diffs);
                        }
                     } else {
                        peerOnly.add(name);
                     }
                  }

                  for (String key : myMacroKeys) {
                     if (!peerMacroKeys.contains(key)) {
                        mineOnly.add(displayNames.getOrDefault(key, key));
                     }
                  }

                  if (!shared.isEmpty()) {
                     for (String name : shared) {
                        cy = this.drawMacroDetailRow(ctx, mx, my, cy, detailLx, detailRx, rowW, name, assigned, null, peerKey, sync, false, clipTop, clipBottom, rowPitch);
                     }
                  }

                  if (!different.isEmpty()) {
                     for (Entry<String, Integer> de : different.entrySet()) {
                        String name = de.getKey();
                        int diff = de.getValue();
                        String badge = " +" + diff + " Difference" + (diff > 1 ? "s" : "");
                        cy = this.drawMacroDetailRow(ctx, mx, my, cy, detailLx, detailRx, rowW, name, assigned, badge, peerKey, sync, false, clipTop, clipBottom, rowPitch);
                     }
                  }

                  if (!peerOnly.isEmpty()) {
                     for (String name : peerOnly) {
                        cy = this.drawMacroDetailRow(ctx, mx, my, cy, detailLx, detailRx, rowW, name, assigned, null, peerKey, sync, true, clipTop, clipBottom, rowPitch);
                     }
                  }

                  if (!mineOnly.isEmpty()) {
                     for (String name : mineOnly) {
                        cy = this.drawMacroDetailRow(ctx, mx, my, cy, detailLx, detailRx, rowW, name, assigned, null, peerKey, sync, false, clipTop, clipBottom, rowPitch);
                     }
                  }

                  if (shared.isEmpty() && different.isEmpty() && peerOnly.isEmpty() && mineOnly.isEmpty()) {
                     if (cy + rowPitch > clipTop && cy < clipBottom) {
                        int rowTop = Math.max(cy, clipTop);
                        int rowBottom = Math.min(cy + rowPitch, clipBottom);
                        if (rowBottom > rowTop) {
                           autismclient.gui.ui.widgets.PanelSurfaceHelper.drawCardRow(ctx, detailLx - 2, rowTop, detailRx - detailLx + 2, rowBottom - rowTop, true, false);
                           int textY = Math.max(cy + 3, clipTop + 1);
                           if (textY < clipBottom - 2) {
                              this.drawText(ctx, "No macros available", autismclient.gui.f.b.l.write, UiColorPalette.textSecondary(), detailLx, textY);
                           }
                        }
                     }

                     cy += rowPitch;
                  }
               }

               cy += 4;
            }
         }
      } finally {
         autismclient.gui.ui.UiClipStack.getInstance().pop(ctx);
      }

      this.init_EventBus.renderScrollbar(ctx, mx, my);
      cy = this.peerListY + this.peerListHeight;
      AutismLANSyncOverlay.AssignmentStatus var51 = this.init_AutismLANSyncOverl_aE(sync);
      LinkedHashSet<String> var521 = this.shareableMacros(var51);
      cy += 4;
      if (!var51.missingAssignments.isEmpty()) {
         String missingLine = ThemedTextHelper.init(this.textRenderer, "Missing: " + this.missingAssignmentsSummary(var51.missingAssignments), btnW, ThemedTextHelper.TextTone.BODY);
         this.drawText(ctx, missingLine, autismclient.gui.f.b.l.read, ThemePaletteManager.recolor(-38037, ThemePaletteManager.ColorRole.DANGER), lx, cy);
         cy += this.lineHeight;
      } else if (var51.hasAssignments()) {
         this.drawText(ctx, "Assigned macros are ready", autismclient.gui.f.b.l.read, -9837157, lx, cy);
         cy += this.lineHeight;
      }

      String sendLabelWide = var521.size() > 1 ? "Send Missing (" + var521.size() + ")" : "Send Missing";
      int shareW = autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(
         autismclient.gui.f.b.k.init(this.textRenderer, sendLabelWide, THEME.init_Identifier_l(autismclient.gui.f.b.l.read), UiColorPalette.textBright())
      );
      int executeX = lx + shareW + 2;
      int executeW = btnW - shareW - 2;
      if (!var521.isEmpty()) {
         String sendLabel = var521.size() > 1 ? "Send Missing (" + var521.size() + ")" : "Send Missing";
         this.drawButton(ctx, lx, cy, shareW, this.buttonHeight, sendLabel, autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS, true, mx, my);
         this.init_List.add(new AutismLANSyncOverlay.ClickButton(lx, cy, shareW, this.buttonHeight, () -> {
            for (String macroName : var521) {
               sync.shareMacroWithPeers(macroName);
            }
         }));
      } else {
         this.drawButton(ctx, lx, cy, shareW, this.buttonHeight, "Send Missing", autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS, false, mx, my);
      }

      String puLabel;
      if (!var51.hasAssignments()) {
         puLabel = "Assign macros to users above";
      } else if (!var51.isFullyAssigned()) {
         puLabel = "Need missing macros first";
      } else {
         puLabel = "Sync Execute (Per-User)";
      }

      if (var51.isFullyAssigned() && !this.configurationOnly) {
         this.drawButton(ctx, executeX, cy, executeW, this.buttonHeight, puLabel, autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY, true, mx, my);
         this.init_List.add(new AutismLANSyncOverlay.ClickButton(executeX, cy, executeW, this.buttonHeight, () -> {
            if (!var51.assignments.isEmpty()) {
               sync.triggerPerUserMacroSync(var51.assignments);
            }
         }));
      } else {
         this.drawButton(
            ctx,
            executeX,
            cy,
            executeW,
            this.buttonHeight,
            this.configurationOnly && var51.hasAssignments() ? "Execute: In Game" : puLabel,
            autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY,
            false,
            mx,
            my
         );
      }

      return cy + this.buttonHeight + 4;
   }

   private AutismLANSyncOverlay.MacroAvailability macroAvailability(LanSyncManager sync, String macroName) {
      List<String> missingPeers = new ArrayList<>(sync.findPeersMissingMacro(macroName));
      Map<String, Integer> differentPeers = new LinkedHashMap<>();
      if (macroName != null && !macroName.isBlank()) {
         AutismMacro localMacro = MacroLibraryManager.getInstance().findByName(macroName);
         if (localMacro == null) {
            return new AutismLANSyncOverlay.MacroAvailability(missingPeers, differentPeers);
         }

         Set<String> missingPeerSet = new HashSet<>(missingPeers);
         Map<String, Map<String, AutismMacro>> allRemote = sync.getPeerMacros();
         List<String> peers = new ArrayList<>(sync.getConnectedClients().keySet());
         Collections.sort(peers);

         for (String peer : peers) {
            if (!peer.equals(sync.getLocalUsername()) && !missingPeerSet.contains(peer)) {
               Map<String, AutismMacro> peerMacros = allRemote.get(peer);
               if (peerMacros != null) {
                  AutismMacro remoteMacro = peerMacros.get(MacroNameNormalizer.init(macroName));
                  int differences = countDifferences(localMacro, remoteMacro);
                  if (differences > 0) {
                     differentPeers.put(peer, differences);
                  }
               }
            }
         }

         return new AutismLANSyncOverlay.MacroAvailability(missingPeers, differentPeers);
      } else {
         return new AutismLANSyncOverlay.MacroAvailability(missingPeers, differentPeers);
      }
   }

   private AutismLANSyncOverlay.AssignmentStatus init_AutismLANSyncOverl_aE(LanSyncManager sync) {
      Map<String, String> assignments = new LinkedHashMap<>();
      Set<String> connectedPeers = new LinkedHashSet<>(sync.getConnectedClients().keySet());

      for (Entry<String, String> entry : new LinkedHashMap<>(this.macroAssignments).entrySet()) {
         String username = entry.getKey();
         String macroName = entry.getValue();
         if (connectedPeers.contains(username) && macroName != null && !macroName.isBlank()) {
            assignments.put(username, macroName);
         }
      }

      Map<String, String> missingAssignments = sync.findMissingAssignedMacros(assignments);
      return new AutismLANSyncOverlay.AssignmentStatus(assignments, missingAssignments);
   }

   private LinkedHashSet<String> shareableMacros(AutismLANSyncOverlay.AssignmentStatus status) {
      LinkedHashSet<String> shareable = new LinkedHashSet<>();

      for (String macroName : status.missingAssignments.values()) {
         if (macroName != null && !macroName.isBlank() && MacroLibraryManager.getInstance().findByName(macroName) != null) {
            shareable.add(macroName);
         }
      }

      return shareable;
   }

   private String joinNames(List<String> names) {
      return names != null && !names.isEmpty() ? String.join(", ", names) : "none";
   }

   private String formatDifferences(Map<String, Integer> differences) {
      if (differences != null && !differences.isEmpty()) {
         List<String> parts = new ArrayList<>();

         for (Entry<String, Integer> entry : differences.entrySet()) {
            int diffCount = Math.max(1, entry.getValue());
            parts.add(entry.getKey() + " (+" + diffCount + ")");
         }

         return String.join(", ", parts);
      } else {
         return "none";
      }
   }

   private String missingAssignmentsSummary(Map<String, String> missingAssignments) {
      if (missingAssignments != null && !missingAssignments.isEmpty()) {
         List<String> parts = new ArrayList<>();

         for (Entry<String, String> entry : missingAssignments.entrySet()) {
            parts.add(entry.getKey() + "=" + entry.getValue());
         }

         return String.join(", ", parts);
      } else {
         return "none";
      }
   }

   private int drawMacroDetailRow(
      GuiGraphicsExtractor ctx,
      int mx,
      int my,
      int cy,
      int detailLx,
      int detailRx,
      int rowW,
      String macroName,
      String assigned,
      String badge,
      String peerKey,
      LanSyncManager sync,
      boolean allowImport,
      int clipTop,
      int clipBottom,
      int rowPitch
   ) {
      boolean isCurrent = MacroNameNormalizer.init(macroName, assigned);
      int importW = allowImport ? 42 : 0;
      int assignW = Math.max(24, detailRx - (detailLx - 2) - importW - 2);
      boolean rowVisible = cy + rowPitch > clipTop && cy < clipBottom;
      if (!rowVisible) {
         return cy + rowPitch;
      }

      int drawY = Math.max(cy, clipTop);
      int drawH = Math.min(cy + rowPitch, clipBottom) - drawY;
      if (drawH <= 0) {
         return cy + rowPitch;
      }

      boolean mHov = this.inside(mx, my, detailLx - 2, cy, assignW, rowPitch);
      autismclient.gui.ui.widgets.ListRowRenderer.drawRow(
         ctx,
         this.textRenderer,
         "",
         detailLx - 2,
         drawY,
         detailRx - (detailLx - 2),
         drawH,
         mHov,
         isCurrent,
         badge != null ? autismclient.gui.ui.widgets.ListRowRenderer.RowTone.WARNING : autismclient.gui.ui.widgets.ListRowRenderer.RowTone.NORMAL
      );
      int tx = detailLx;
      int nameMaxW = Math.max(24, detailRx - tx - importW - (badge != null ? 74 : 4));
      String displayName = ThemedTextHelper.init(this.textRenderer, macroName, nameMaxW, ThemedTextHelper.TextTone.BODY);
      int textY = Math.max(cy + 3, clipTop + 1);
      if (textY < clipBottom - 2) {
         autismclient.gui.f.b.k.init(
            ctx, this.textRenderer, displayName, THEME.init_Identifier_l(autismclient.gui.f.b.l.read), isCurrent ? UiColorPalette.selectedText() : -1, tx, textY, false
         );
      }

      if (badge != null && textY < clipBottom - 2) {
         int badgeX = tx
            + autismclient.gui.f.b.k.init(
               this.textRenderer, displayName, THEME.init_Identifier_l(autismclient.gui.f.b.l.read), isCurrent ? UiColorPalette.selectedText() : -1
            )
            + 4;
         this.drawText(ctx, badge, autismclient.gui.f.b.l.write, -22016, badgeX, textY);
      }

      String mName = macroName;
      if (rowVisible) {
         this.init_List.add(new AutismLANSyncOverlay.ClickButton(detailLx - 2, cy, assignW, rowPitch, () -> {
            this.macroAssignments.put(peerKey, mName.equals(this.macroAssignments.get(peerKey)) ? "" : mName);
            sync.broadcastMacroAssignments(this.macroAssignments);
         }));
      }

      if (allowImport) {
         int importX = detailRx - importW + 2;
         int importY = Math.max(cy + 1, clipTop + 1);
         if (importY < clipBottom - 4) {
            this.drawButton(
               ctx,
               importX,
               importY,
               importW - 4,
               Math.min(rowPitch - 2, clipBottom - importY - 1),
               "Import",
               autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY,
               true,
               mx,
               my
            );
         }

         if (rowVisible) {
            this.init_List.add(new AutismLANSyncOverlay.ClickButton(importX, cy, importW - 4, rowPitch, () -> {
               Map<String, Map<String, AutismMacro>> allRemote = sync.getPeerMacros();
               Map<String, AutismMacro> peerMacros = allRemote.get(peerKey);
               AutismMacro sourceMacro = peerMacros != null ? peerMacros.get(MacroNameNormalizer.init(mName)) : null;
               if (sourceMacro == null) {
                  AutismClientMessaging.sendPrefixed("§cMacro not found: " + mName);
               } else {
                  AutismMacro imported = MacroLibraryManager.getInstance().importMacro(sourceMacro, sourceMacro.name);
                  if (imported == null) {
                     AutismClientMessaging.sendPrefixed("§cFailed to import macro: " + mName);
                  } else {
                     AutismClientMessaging.sendPrefixed("§aImported macro: " + imported.name);
                  }
               }
            }));
         }
      }

      return cy + rowPitch;
   }

   private static int countDifferences(AutismMacro a, AutismMacro b) {
      if (a != null && b != null) {
         List<MacroAction> aActions = a.actions != null ? a.actions : Collections.emptyList();
         List<MacroAction> bActions = b.actions != null ? b.actions : Collections.emptyList();
         int diffs = 0;
         int maxLen = Math.max(aActions.size(), bActions.size());

         for (int i = 0; i < maxLen; i++) {
            if (i < aActions.size() && i < bActions.size()) {
               CompoundTag tagA = aActions.get(i).toTag();
               CompoundTag tagB = bActions.get(i).toTag();
               if (!tagA.equals(tagB)) {
                  diffs++;
               }
            } else {
               diffs++;
            }
         }

         return diffs;
      } else {
         return -1;
      }
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.visible) {
         return false;
      }

      if (button == 0 && mouseX >= this.panelX && mouseX <= this.panelX + this.panelWidth && mouseY >= this.panelY && mouseY <= this.panelY + 14) {
         OverlayBounds bounds = new OverlayBounds(this.panelX, this.panelY, this.panelWidth, this.collapsed ? 14 : this.push_I_12(), this.visible, this.collapsed);
         if (this.isOverHeaderCloseButton(mouseX, mouseY, bounds)) {
            this.setVisible(false);
            return true;
         } else {
            this.dragging = true;
            this.dragOffsetX = mouseX - this.panelX;
            this.dragOffsetY = mouseY - this.panelY;
            return true;
         }
      } else {
         if (this.collapsed) {
            return false;
         }

         LanSyncManager sync = LanSyncManager.getInstance();
         if (this.inputBox != null && sync.isRunning() && sync.isInSession()) {
            MouseButtonEvent click = new MouseButtonEvent(mouseX, mouseY, Compat263.newGlfwMouseButtonInfo(button, 0));
            if (this.inputBox.mouseClicked(click, false)) {
               this.inputBox.setFocused(true);
               return true;
            }

            this.inputBox.setFocused(false);
         } else if (this.inputBox != null) {
            this.inputBox.setFocused(false);
         }

         if (button == 0 && this.isSessionActive(sync)) {
            int panelHeight = this.push_I_12();
            int tabContentY = this.tabContentY();
            int tabContentH = this.init_I_I_5(panelHeight);
            autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics = this.init_PoolEntry_DataUtil_I_I(tabContentY, tabContentH);
            if (scrollbarMetrics.isScrollable() && scrollbarMetrics.containsTrack(mouseX, mouseY)) {
               this.close_Z = true;
               this.scrollbarGrabOffset = Math.max(0, (int)Math.round(mouseY) - scrollbarMetrics.thumbY());
               this.scrollOffset = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(scrollbarMetrics, (int)Math.round(mouseY), this.scrollbarGrabOffset);
               this.scroll.jumpTo(this.scrollOffset, scrollbarMetrics.maxScroll());
               this.saveState();
               return true;
            }
         }

         if (button == 0
            && this.activeTab == 2
            && !this.perUserMode
            && this.poll_Z_7()
            && this.init_RenderState.containsPoint(mouseX, mouseY)
            && this.init_RenderState.mouseClicked(mouseX, mouseY, button)) {
            if (!this.init_RenderState.isDraggingScrollbar()) {
               AutismMacro selected = this.init_RenderState.selected();
               if (selected != null) {
                  this.selectedMacroName = selected.name;
                  this.saveState();
               }
            }

            return true;
         } else {
            if (button == 0
               && this.activeTab == 2
               && this.perUserMode
               && this.push_Z_6()
               && this.init_EventBus.containsPoint(mouseX, mouseY)
               && this.init_EventBus.mouseClicked(mouseX, mouseY, button)) {
               return true;
            }

            if (button == 0) {
               for (AutismLANSyncOverlay.ClickButton region : this.init_List) {
                  if (region.contains(mouseX, mouseY)) {
                     region.action.run();
                     return true;
                  }
               }
            }

            return this.containsPoint(mouseX, mouseY);
         }
      }
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (button == 0) {
         if (this.poll_Z_7()) {
            this.init_RenderState.mouseReleased();
         }

         if (this.push_Z_6()) {
            this.init_EventBus.mouseReleased();
         }

         if (this.close_Z) {
            this.saveState();
         }

         this.close_Z = false;
         if (this.dragging) {
            this.saveState();
         }

         this.dragging = false;
      }

      return this.inputBox != null && this.inputBox.mouseReleased(mouseX, mouseY, button);
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.close_Z) {
         int panelHeight = this.push_I_12();
         int tabContentY = this.tabContentY();
         int tabContentH = this.init_I_I_5(panelHeight);
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics = this.init_PoolEntry_DataUtil_I_I(tabContentY, tabContentH);
         this.scrollOffset = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(scrollbarMetrics, (int)Math.round(mouseY), this.scrollbarGrabOffset);
         this.scroll.jumpTo(this.scrollOffset, scrollbarMetrics.maxScroll());
         return true;
      } else if (this.poll_Z_7() && this.init_RenderState.isDraggingScrollbar()) {
         this.init_RenderState.mouseDragged(mouseX, mouseY);
         return true;
      } else if (this.push_Z_6() && this.init_EventBus.isDraggingScrollbar()) {
         this.init_EventBus.mouseDragged(mouseX, mouseY);
         return true;
      } else if (this.dragging) {
         OverlayBounds nextBounds = this.clampToScreen(
            this, new OverlayBounds((int)(mouseX - this.dragOffsetX), (int)(mouseY - this.dragOffsetY), this.panelWidth, this.panelHeight, this.visible, this.collapsed)
         );
         this.panelX = nextBounds.x;
         this.panelY = nextBounds.y;
         return true;
      } else {
         return this.inputBox != null && this.inputBox.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
      }
   }

   @Override
   public boolean keyPressed(int key, int scancode, int modifiers) {
      if (!this.visible || this.collapsed) {
         return false;
      } else if (this.inputBox == null || !this.inputBox.isFocused()) {
         return false;
      } else if (key == 256) {
         this.inputBox.setFocused(false);
         return true;
      } else {
         this.inputBox.keyPressed(Compat263.newGlfwKeyEvent(key, scancode, modifiers));
         return true;
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      if (!this.visible || this.collapsed || !this.containsPoint(mouseX, mouseY)) {
         return false;
      }

      if (this.activeTab == 2 && !this.perUserMode && this.poll_Z_7()) {
         return this.init_RenderState.mouseScrolled(mouseX, mouseY, amount);
      }

      if (this.activeTab == 2 && this.perUserMode && this.push_Z_6()) {
         return this.init_EventBus.mouseScrolled(mouseX, mouseY, amount);
      }

      this.scrollOffset = Math.max(0, Math.min(this.maxScroll, this.scrollOffset - (int)(amount * this.rowHeight)));
      this.saveState();
      return true;
   }

   @Override
   public boolean charTyped(char chr, int modifiers) {
      if (this.visible && !this.collapsed) {
         return this.inputBox != null && this.inputBox.isFocused() ? this.inputBox.charTyped(new CharacterEvent(chr)) : false;
      } else {
         return false;
      }
   }

   private boolean inside(int mx, int my, int x, int y, int w, int h) {
      return mx >= x && mx < x + w && my >= y && my < y + h;
   }

   private int stateColor(LanSyncManager.SyncState state) {
      switch (state) {
         case IDLE:
            return UiColorPalette.textSecondary();
         case PREPARING:
            return -22016;
         case ALL_READY:
            return -11141291;
         case DISPATCHING_GO:
            return -11141121;
         case EXECUTING:
            return -11141291;
         case REPORTING:
            return -22016;
         case DONE:
            return -11141291;
         default:
            return UiColorPalette.textBody();
      }
   }

   private String stateLabel(LanSyncManager.SyncState state, LanSyncManager sync) {
      switch (state) {
         case IDLE:
            return "Idle";
         case PREPARING:
            int ready = 0;
            int total = sync.getConnectedClientCount();
            return "Preparing (" + ready + "/" + total + " ready)";
         case ALL_READY:
            return "All Ready!";
         case DISPATCHING_GO:
            return "GO!";
         case EXECUTING:
            return "Executing...";
         case REPORTING:
            return "Collecting results...";
         case DONE:
            return "Done";
         default:
            return state.name();
      }
   }

   private int spreadColor(double spreadMs) {
      if (spreadMs <= 1.0) {
         return -11141291;
      } else {
         return spreadMs <= 5.0 ? -22016 : ThemePaletteManager.recolor(-43691, ThemePaletteManager.ColorRole.DANGER);
      }
   }

   private void computeLayout() {
      this.defaultWidth = 264;
      this.tabHeight = autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
      this.buttonHeight = autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
      this.rowHeight = autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
      this.lineHeight = autismclient.gui.ui.UiDensityMetrics.scaledHeight(12);
      this.contentPadding = 6;
      this.inputHeight = autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
      this.inputLabelHeight = autismclient.gui.ui.UiDensityMetrics.scaledHeight(12) - 1;
      this.chatInputAreaHeight = this.inputHeight + this.inputLabelHeight + 4;
   }

   private int minPanelHeight() {
      return 14 + this.tabHeight + 2 + this.rowHeight * 3 + 4 + this.chatInputAreaHeight;
   }

   private int defaultPanelHeight() {
      return 180;
   }

   private static class MacroAvailability {
      final List<String> missingPeers;
      final Map<String, Integer> differentPeers;

      MacroAvailability(List<String> missingPeers, Map<String, Integer> differentPeers) {
         this.missingPeers = missingPeers;
         this.differentPeers = differentPeers;
      }

      boolean allPeersPresent() {
         return this.missingPeers.isEmpty();
      }
   }

   private static class AssignmentStatus {
      final Map<String, String> assignments;
      final Map<String, String> missingAssignments;

      AssignmentStatus(Map<String, String> assignments, Map<String, String> missingAssignments) {
         this.assignments = assignments;
         this.missingAssignments = missingAssignments;
      }

      boolean hasAssignments() {
         return !this.assignments.isEmpty();
      }

      boolean isFullyAssigned() {
         return this.hasAssignments() && this.missingAssignments.isEmpty();
      }
   }

   private static class ClickButton {
      final int x;
      final int y;
      final int width;
      final int height;
      final Runnable action;

      ClickButton(int x, int y, int width, int height, Runnable action) {
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
         this.action = action;
      }

      boolean contains(double mx, double my) {
         return mx >= this.x && mx < this.x + this.width && my >= this.y && my < this.y + this.height;
      }
   }
}
