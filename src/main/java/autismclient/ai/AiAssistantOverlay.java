package autismclient.ai;

import autismclient.gui.f.PacketUtil;
import autismclient.util.OverlayManager;
import autismclient.util.OverlayBounds;
import autismclient.util.AutismOverlay;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

final class AiAssistantOverlay extends autismclient.util.DraggableOverlayWindow {
   private static final int flush_I = 4;
   private static final int close_I = 2;
   private static final int DEFAULT_WIDTH = 360;
   private static final int DEFAULT_HEIGHT = 240;
   private static final Map<String, String> EFFORT_LABELS = Map.of("low", "Low", "medium", "Med", "high", "High", "max", "Max");
   private final AiService service;
   private final AiViewState viewState;
   private final AiUiKit ui = new AiUiKit();
   private final AiChatView chatView;
   private final AiSessionsDrawer sessionsDrawer;
   private final AiSettingsView settingsView;
   private final AiMemoryView memoryView;
   private final autismclient.gui.ui.widgets.TextInputWidget renameInput = new autismclient.gui.ui.widgets.TextInputWidget()
      .setMaxLength(96)
      .setPlaceholder("Chat title");
   private final List<autismclient.gui.ui.widgets.TextInputWidget> inputs;
   private final autismclient.util.OverlayContextMenu<String> contextMenu;
   private AiAssistantOverlay.OverlayTab tab = AiAssistantOverlay.OverlayTab.CHAT;
   private boolean pinned;
   private boolean renaming;
   private boolean sync_Z;
   private boolean flush_Z;
   private double dragOffsetX;
   private double dragOffsetY;
   private int contextMenuX;
   private int contextMenuY;

   AiAssistantOverlay(AiService service) {
      super("ai-assistant", 360, 240);
      this.service = service;
      this.windowX = 35;
      this.windowY = 30;
      this.loadBounds();
      this.viewState = new AiViewState(service);
      this.chatView = new AiChatView(service, this.viewState, this);
      this.sessionsDrawer = new AiSessionsDrawer(service, this.viewState, this);
      this.settingsView = new AiSettingsView(service, this.viewState, this);
      this.memoryView = new AiMemoryView(service, this.viewState, this);
      this.pinned = service.getSettings().pinned;
      this.renameInput.setOnSubmit(text -> this.commitRename());
      List<autismclient.gui.ui.widgets.TextInputWidget> all = new ArrayList<>(
         List.of(
            this.chatView.input,
            this.sessionsDrawer.searchInput,
            this.sessionsDrawer.renameInput,
            this.renameInput,
            this.memoryView.searchInput,
            this.memoryView.editInput
         )
      );
      all.addAll(this.chatView.actionMenu.inputs());
      all.addAll(this.settingsView.inputs());
      this.inputs = List.copyOf(all);
      this.contextMenu = new autismclient.util.OverlayContextMenu<>(this.ui.theme, Minecraft.getInstance().font, id -> AiSessionsDrawer.MENU_ITEMS, AiUiKit.rowHeight());
      this.setActiveContextMenu(this.contextMenu);
   }

   @Override
   public int getMinWidth() {
      return 280;
   }

   @Override
   public int getMinHeight() {
      return this.tab == AiAssistantOverlay.OverlayTab.CHAT && this.chatView != null
         ? Math.max(180, 22 + AiUiKit.toolbarHeight() + 4 + 1 + this.chatView.preferredHeight())
         : 180;
   }

   @Override
   public boolean isPersistent() {
      return this.pinned;
   }

   @Override
   public boolean supportsHeaderCollapseClick() {
      return true;
   }

   @Override
   public AutismOverlay.RenderLayer getRenderLayer() {
      return this.pinned ? AutismOverlay.RenderLayer.BACKGROUND_STATUS : super.getRenderLayer();
   }

   private void togglePinnedSetting() {
      boolean next = !this.pinned;
      this.pinned = next;
      this.service.editSettingsAsync(s -> s.pinned = next);
      OverlayManager.getInstance().register(this, next ? AutismOverlay.RenderLayer.BACKGROUND_STATUS : AutismOverlay.RenderLayer.HOST_SCREEN);
      if (!next && Minecraft.getInstance().gui.screen() == null) {
         this.setVisible(false);
      }

      this.viewState.invalidate();
   }

   @Override
   public boolean containsPoint(double x, double y) {
      return super.containsPoint(x, y)
         || this.windowVisible && !this.windowCollapsed && this.tab == AiAssistantOverlay.OverlayTab.SETTINGS && this.settingsView.isOverPopup(x, y);
   }

   @Override
   public boolean isResizable() {
      return true;
   }

   @Override
   protected void onSizeChanged() {
      if (!this.hasCustomStoredSize()) {
         this.windowWidth = 360;
         this.windowHeight = 240;
      }
   }

   void showSettings() {
      this.switchTab(AiAssistantOverlay.OverlayTab.SETTINGS);
   }

   void showMemory() {
      this.switchTab(AiAssistantOverlay.OverlayTab.MEMORY);
   }

   void focusActiveInput() {
      this.focusInput(this.chatView.activeInput());
   }

   int contextMenuX() {
      return this.contextMenuX;
   }

   int contextMenuY() {
      return this.contextMenuY;
   }

   void focusInput(autismclient.gui.ui.widgets.TextInputWidget field) {
      for (autismclient.gui.ui.widgets.TextInputWidget other : this.inputs) {
         if (other != field) {
            other.setFocused(false);
         }
      }

      field.setFocused(true);
   }

   void openContextMenu(String id) {
      this.contextMenu.open(this.contextMenuX, this.contextMenuY, id);
   }

   void openAction(String action, String query) {
      this.showView("chat");
      this.chatView.openAction(action, query);
   }

   void confirmActionMenu() {
      this.chatView.actionMenu.confirm();
   }

   void openSettings() {
      this.showView("settings");
      this.settingsView.init_V();
   }

   void confirmPlacement() {
      this.chatView.actionMenu.confirmPlacement();
   }

   void close_V() {
      this.chatView.actionMenu.update_V();
   }

   JsonObject actionMenuStatus() {
      return this.chatView.actionMenu.statusJson();
   }

   void hide() {
      this.setVisible(false);
   }

   void cancelActionMenu() {
      this.chatView.actionMenu.cancel();
   }

   void showView(String name) {
      this.chatView.setAttachmentsEnabled("attachments".equals(name));
      switch (name == null ? "" : name) {
         case "settings":
            this.switchTab(AiAssistantOverlay.OverlayTab.SETTINGS);
            break;
         case "memory":
            this.switchTab(AiAssistantOverlay.OverlayTab.MEMORY);
            break;
         case "sessions":
            this.switchTab(AiAssistantOverlay.OverlayTab.CHAT);
            if (!this.sessionsDrawer.open) {
               this.sessionsDrawer.toggle();
            }
            break;
         default:
            this.switchTab(AiAssistantOverlay.OverlayTab.CHAT);
            this.sessionsDrawer.closeDrawer();
      }

      this.viewState.invalidate();
   }

   private void switchTab(AiAssistantOverlay.OverlayTab next) {
      if (next != this.tab) {
         for (autismclient.gui.ui.widgets.TextInputWidget field : this.inputs) {
            field.setFocused(false);
         }

         this.renaming = false;
         this.settingsView.closePopups();
         this.settingsView.cancelKeyCapture();
         this.contextMenu.close();
         if (next == AiAssistantOverlay.OverlayTab.SETTINGS) {
            this.settingsView.loadSettings(this.service.getSettings());
         }

         if (next == AiAssistantOverlay.OverlayTab.MEMORY) {
            this.memoryView.reset();
         }

         if (next != AiAssistantOverlay.OverlayTab.CHAT) {
            this.sessionsDrawer.closeDrawer();
         }

         this.tab = next;
         this.viewState.invalidate();
      }
   }

   private void toggleSessions() {
      if (this.tab != AiAssistantOverlay.OverlayTab.CHAT) {
         this.switchTab(AiAssistantOverlay.OverlayTab.CHAT);
      }

      this.sessionsDrawer.toggle();
   }

   private void startRename() {
      this.renaming = true;
      this.renameInput.setValue(this.sessionTitle());
      this.renameInput.moveCursorToEnd();
      this.focusInput(this.renameInput);
   }

   private void commitRename() {
      String text = this.renameInput.getValue().trim();
      this.renaming = false;
      this.renameInput.setFocused(false);
      if (!text.isEmpty()) {
         this.service.renameSession(this.viewState.activeSessionId, text);
      }

      this.viewState.invalidate();
   }

   private String sessionTitle() {
      String title = this.viewState.activeSessionTitle == null ? "" : this.viewState.activeSessionTitle.trim();
      return !title.isEmpty() && !title.equals("New session") ? title : (this.viewState.settings.builtIn ? "Local tasks" : "New chat");
   }

   @Override
   public void render(GuiGraphicsExtractor graphics, int mx, int my, float delta) {
      if (this.windowVisible || this.isClosing()) {
         this.windowWidth = this.resolveWindowWidth(this.windowWidth, this.getMinWidth());
         this.windowHeight = this.resolveWindowHeight(this.windowHeight, this.getMinHeight());
         if (this.tab == AiAssistantOverlay.OverlayTab.CHAT) {
            int body = this.chatView.bodyHeight(this.windowWidth - 8);
            if (body > 0) {
               this.windowHeight = Math.min(this.windowHeight, Math.max(this.getMinHeight(), 22 + AiUiKit.toolbarHeight() + 4 + 1 + body));
            }
         }

         OverlayBounds bounds = this.clampCurrentBounds(this);
         this.windowX = bounds.x;
         this.windowY = bounds.y;
         this.windowWidth = bounds.width;
         this.windowHeight = bounds.height;
         this.renderWindowFrame(graphics, mx, my, this.getBounds(), "AI Assistant", this.windowCollapsed, this.sync_Z);
         if (!this.isVisuallyCollapsed(this.windowCollapsed)) {
            long now = System.currentTimeMillis();
            this.viewState.refresh(now, this.sessionsDrawer.open);
            this.recordMousePosition(mx, my);
            boolean blocked = this.contextMenu.handleClick(mx, my)
               || this.tab == AiAssistantOverlay.OverlayTab.SETTINGS && this.settingsView.isOverPopup(mx, my);
            this.ui.beginFrame(graphics, mx, my, blocked, delta);
            autismclient.gui.f.NetHelper content = autismclient.gui.f.NetHelper.init(
               this.windowX + 4, this.windowY + 14 + 4, this.windowWidth - 8, this.windowHeight - 14 - 8
            );
            autismclient.gui.f.NetHelper toolbar = autismclient.gui.f.NetHelper.init(content.read(), content.write(), content.update(), AiUiKit.toolbarHeight());
            int divider = toolbar.load() + 2;
            PacketUtil.init_V_GuiGraphicsExtract_2(graphics, content.read(), divider, content.update(), this.ui.colors.divider);
            autismclient.gui.f.NetHelper area = autismclient.gui.f.NetHelper.init(
               content.read(), divider + 1 + 2, content.update(), Math.max(0, content.load() - divider - 1 - 2)
            );
            this.drawToolbar(toolbar);
            switch (this.tab) {
               case CHAT:
                  this.chatView.render(this.ui, area, now);
                  this.sessionsDrawer.render(this.ui, this.chatView.init_NetHelper(), now);
                  break;
               case SETTINGS:
                  this.settingsView.render(this.ui, area, now);
                  break;
               case MEMORY:
                  this.memoryView.render(this.ui, area, now);
            }

            if (this.tab == AiAssistantOverlay.OverlayTab.SETTINGS) {
               this.settingsView.renderPopups(this.ui);
            }

            this.contextMenu.render(graphics, mx, my);
            if (!this.contextMenu.isOpen() && (this.tab != AiAssistantOverlay.OverlayTab.SETTINGS || !this.settingsView.hasOpenDropdown())) {
               this.ui.renderHoveredTooltip();
            }
         }
      }
   }

   private void drawToolbar(autismclient.gui.f.NetHelper bar) {
      autismclient.gui.ui.UiColorScheme colors = this.ui.colors;
      int h = bar.sync();
      int x = bar.read();
      int right = bar.init();
      this.ui
         .drawIconButton(
            autismclient.gui.f.NetHelper.init(x, bar.write(), h, h),
            autismclient.util.GuiIconAtlas.SESSIONS,
            this.sessionsDrawer.open,
            false,
            this.sessionsDrawer.open ? "Hide chats" : "Chats",
            this::toggleSessions
         );
      x += h + 2 + 2;
      right -= h;
      boolean inChat = this.tab == AiAssistantOverlay.OverlayTab.CHAT;
      this.ui
         .drawIconButton(
            autismclient.gui.f.NetHelper.init(right, bar.write(), h, h),
            autismclient.util.GuiIconAtlas.SETTINGS,
            !inChat,
            false,
            inChat ? "Settings" : "Back to chat (Esc)",
            () -> this.switchTab(inChat ? AiAssistantOverlay.OverlayTab.SETTINGS : AiAssistantOverlay.OverlayTab.CHAT)
         );
      right -= h + 2;
      this.ui
         .drawIconButton(
            autismclient.gui.f.NetHelper.init(right, bar.write(), h, h),
            autismclient.util.GuiIconAtlas.PIN,
            this.pinned,
            false,
            this.pinned ? "Pinned: stays open in-game (click to unpin)" : "Pin: keep open after the menu closes",
            this::togglePinnedSetting
         );
      right -= 4;
      List<String> levels = this.viewState.availableEffortLevels;
      int chipsW = 0;

      for (String level : levels) {
         chipsW += this.ui.measureTextWidth(EFFORT_LABELS.getOrDefault(level, level)) + 10 + 2;
      }

      if (!levels.isEmpty() && right - chipsW - x >= 60) {
         int cx = right - chipsW + 2;

         for (String level : levels) {
            String label = EFFORT_LABELS.getOrDefault(level, level);
            autismclient.gui.f.NetHelper chip = autismclient.gui.f.NetHelper.init(cx, bar.write(), this.ui.measureTextWidth(label) + 10, h);
            autismclient.gui.ui.widgets.PillBadgeRenderer.init(
               this.ui.renderContext, chip, label, colors.accent, level.equals(this.viewState.effectiveEffortLevel), this.ui.isHovered(chip)
            );
            this.ui.addInteractiveRegion(chip, () -> this.setEffort(level), null, "Reasoning effort");
            cx += chip.update() + 2;
         }

         right -= chipsW + 2;
      }

      autismclient.gui.f.NetHelper title = autismclient.gui.f.NetHelper.init(x, bar.write(), Math.max(10, right - x), h);
      if (!inChat) {
         this.ui
            .drawCenteredRowText(
               this.tab == AiAssistantOverlay.OverlayTab.SETTINGS ? "Settings" : "Memory",
               title,
               title.read() + 4,
               title.update() - 8,
               colors.textPrimary
            );
      } else {
         if (this.renaming && !this.renameInput.isFocused()) {
            this.renaming = false;
         }

         if (this.renaming) {
            this.ui.drawTextInput(this.renameInput, title);
         } else {
            if (this.ui.isHovered(title)) {
               PacketUtil.load(this.ui.graphics, title, 3, colors.cardSurfaceElevated);
            }

            String text = this.sessionTitle();
            this.ui.drawCenteredRowText(text, title, title.read() + 4, title.update() - 8, text.equals("New chat") ? colors.textSecondary : colors.textPrimary);
            this.ui.addInteractiveRegion(title, this::startRename, null, "Click to rename");
         }
      }
   }

   private void setEffort(String level) {
      this.service.setEffortLevel(level);
      this.viewState.effectiveEffortLevel = level;
      this.viewState.invalidate();
   }

   @Override
   public boolean mouseClicked(double x, double y, int button) {
      if (this.windowVisible && this.containsPoint(x, y)) {
         this.contextMenuX = (int)x;
         this.contextMenuY = (int)y;
         if (this.contextMenu.isOpen()) {
            this.contextMenu.init(x, y, button, (id, label, index) -> this.sessionsDrawer.onMenuAction(id, index));
            return true;
         }

         if (this.isOverHeaderCloseButton(x, y, this.getBounds())) {
            this.setVisible(false);
            return true;
         }

         if (this.isOverHeader(x, y)) {
            this.sync_Z = true;
            this.dragOffsetX = x - this.windowX;
            this.dragOffsetY = y - this.windowY;
            return true;
         }

         if (!this.windowCollapsed && this.ui.isInitialized()) {
            this.flush_Z = true;
            if (this.tab == AiAssistantOverlay.OverlayTab.SETTINGS && this.settingsView.clickPopup(x, y, button)) {
               return true;
            } else if (this.tab == AiAssistantOverlay.OverlayTab.CHAT
               && this.sessionsDrawer.open
               && !this.sessionsDrawer.contains(x, y)
               && this.chatView.init_NetHelper().init_Z((int)x, (int)y)) {
               this.sessionsDrawer.closeDrawer();
               return true;
            } else {
               this.ui.mouseClicked(x, y, button, this.inputs);
               return true;
            }
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseReleased(double x, double y, int button) {
      boolean moved = this.sync_Z;
      this.sync_Z = false;
      if (moved) {
         this.saveBounds();
      }

      boolean used = this.ui.mouseReleased();
      if (this.tab == AiAssistantOverlay.OverlayTab.SETTINGS && this.settingsView.load_Z()) {
         used = true;
      }

      autismclient.gui.ui.widgets.TextInputWidget focused = this.focusedInput();
      if (focused != null && this.ui.isInitialized() && focused.mouseReleased(this.ui.layoutContext, (float)x, (float)y, button)) {
         used = true;
      }

      return moved || used;
   }

   @Override
   public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
      if (this.sync_Z) {
         this.windowX = (int)(x - this.dragOffsetX);
         this.windowY = (int)(y - this.dragOffsetY);
         return true;
      }

      if (this.ui.mouseDragged(y)) {
         return true;
      }

      if (this.tab == AiAssistantOverlay.OverlayTab.SETTINGS && this.settingsView.load(x, y, button)) {
         return true;
      }

      autismclient.gui.ui.widgets.TextInputWidget focused = this.focusedInput();
      return focused != null
         && this.ui.isInitialized()
         && focused.mouseDragged(this.ui.layoutContext, (float)x, (float)y, button, (float)dx, (float)dy);
   }

   @Override
   public boolean mouseScrolled(double x, double y, double amount) {
      if (!this.windowVisible || this.windowCollapsed || !this.containsPoint(x, y)) {
         return false;
      }

      if (this.contextMenu.isOpen()) {
         return true;
      }

      if (this.tab == AiAssistantOverlay.OverlayTab.SETTINGS && this.settingsView.scrollPopup(x, y, amount)) {
         return true;
      }

      this.ui.mouseScrolled(x, y, amount);
      return true;
   }

   @Override
   public boolean keyPressed(int key, int scan, int mods) {
      if (!this.ui.isInitialized()) {
         return false;
      }

      if (key == 256) {
         return this.poll_Z();
      }

      if (key == 258
         && (mods & 1) != 0
         && this.tab == AiAssistantOverlay.OverlayTab.CHAT
         && !this.contextMenu.isOpen()
         && !this.viewState.settings.builtIn) {
         this.chatView.init_V();
         return true;
      }

      if (this.tab == AiAssistantOverlay.OverlayTab.SETTINGS && this.settingsView.captureKey(key)) {
         return true;
      }

      autismclient.gui.ui.widgets.TextInputWidget focused = this.focusedInput();
      return this.tab == AiAssistantOverlay.OverlayTab.CHAT
            && !this.sessionsDrawer.open
            && !this.contextMenu.isOpen()
            && this.chatView.init_Z_I_BitSetUtil(key, focused)
         ? true
         : focused != null && focused.keyPressed(this.ui.layoutContext, key, scan, mods);
   }

   private boolean poll_Z() {
      if (this.contextMenu.isOpen()) {
         this.contextMenu.close();
         return true;
      } else if (this.settingsView.closePopups() || this.settingsView.consumeKeyCapture()) {
         return true;
      } else if (this.renaming) {
         this.renaming = false;
         this.renameInput.setFocused(false);
         return true;
      } else if (this.sessionsDrawer.cancelRename() || this.memoryView.cancelEdit()) {
         return true;
      } else if (this.tab == AiAssistantOverlay.OverlayTab.CHAT && !this.sessionsDrawer.open && this.chatView.load_Z()) {
         return true;
      } else {
         autismclient.gui.ui.widgets.TextInputWidget focused = this.focusedInput();
         if (focused != null) {
            focused.setFocused(false);
            return true;
         } else if (this.tab != AiAssistantOverlay.OverlayTab.CHAT) {
            this.switchTab(AiAssistantOverlay.OverlayTab.CHAT);
            return true;
         } else if (this.sessionsDrawer.open) {
            this.sessionsDrawer.closeDrawer();
            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public boolean charTyped(char c, int mods) {
      if (!this.ui.isInitialized()) {
         return false;
      }

      autismclient.gui.ui.widgets.TextInputWidget focused = this.focusedInput();
      if (focused == null && this.tab == AiAssistantOverlay.OverlayTab.CHAT && !this.sessionsDrawer.open && !this.contextMenu.isOpen()) {
         autismclient.gui.ui.widgets.TextInputWidget target = this.chatView.init_BitSetUtil_C(c);
         if (target != null) {
            this.focusInput(target);
            return target.charTyped(this.ui.layoutContext, c, mods);
         }
      }

      return focused != null && focused.charTyped(this.ui.layoutContext, c, mods);
   }

   private autismclient.gui.ui.widgets.TextInputWidget focusedInput() {
      for (autismclient.gui.ui.widgets.TextInputWidget field : this.inputs) {
         if (field.isFocused()) {
            return field;
         }
      }

      return null;
   }

   private boolean push_Z() {
      return this.flush_Z
            && (
               this.tab != AiAssistantOverlay.OverlayTab.CHAT
                  || this.sessionsDrawer.open
                  || this.settingsView.isCapturingKey()
                  || this.contextMenu.isOpen()
                  || this.chatView.init_Z()
            )
         ? Minecraft.getInstance().gui.screen() instanceof autismclient.gui.screens.AutismOverlaysScreen host && host.init(this)
         : false;
   }

   @Override
   public boolean hasFocusedTextInput() {
      return this.focusedInput() != null || this.push_Z();
   }

   @Override
   public void clearInputFocus() {
      for (autismclient.gui.ui.widgets.TextInputWidget field : this.inputs) {
         field.setFocused(false);
      }

      this.flush_Z = false;
      this.settingsView.cancelKeyCapture();
   }

   enum OverlayTab {
      CHAT,
      SETTINGS,
      MEMORY;
   }
}
