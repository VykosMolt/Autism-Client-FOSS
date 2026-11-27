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
   private static final int open = 360;
   private static final int poll = 240;
   private static final Map<String, String> init_Map = Map.of("low", "Low", "medium", "Med", "high", "High", "max", "Max");
   private final AiService init_cc;
   private final AiViewState init_cF;
   private final AiUiKit init_cE = new AiUiKit();
   private final AiChatView init_TaskTimer;
   private final AiSessionsDrawer init_ce;
   private final AiSettingsView init_cf;
   private final AiMemoryView init_ap;
   private final autismclient.gui.ui.widgets.TextInputWidget init_BitSetUtil = new autismclient.gui.ui.widgets.TextInputWidget()
      .setMaxLength(96)
      .setPlaceholder("Chat title");
   private final List<autismclient.gui.ui.widgets.TextInputWidget> init_List;
   private final autismclient.util.OverlayContextMenu<String> init_F_2;
   private AiAssistantOverlay.Inner_DataUtil init_AiAssistantOverlay = AiAssistantOverlay.Inner_DataUtil.CHAT;
   private boolean write_Z;
   private boolean update_Z;
   private boolean sync_Z;
   private boolean flush_Z;
   private double init_D;
   private double load_D;
   private int push;
   private int pop;

   AiAssistantOverlay(AiService service) {
      super("ai-assistant", 360, 240);
      this.init_cc = service;
      this.windowX = 35;
      this.windowY = 30;
      this.loadBounds();
      this.init_cF = new AiViewState(service);
      this.init_TaskTimer = new AiChatView(service, this.init_cF, this);
      this.init_ce = new AiSessionsDrawer(service, this.init_cF, this);
      this.init_cf = new AiSettingsView(service, this.init_cF, this);
      this.init_ap = new AiMemoryView(service, this.init_cF, this);
      this.write_Z = service.getSettings().pinned;
      this.init_BitSetUtil.setOnSubmit(text -> this.clear());
      List<autismclient.gui.ui.widgets.TextInputWidget> all = new ArrayList<>(
         List.of(
            this.init_TaskTimer.input,
            this.init_ce.searchInput,
            this.init_ce.renameInput,
            this.init_BitSetUtil,
            this.init_ap.searchInput,
            this.init_ap.editInput
         )
      );
      all.addAll(this.init_TaskTimer.actionMenu.inputs());
      all.addAll(this.init_cf.inputs());
      this.init_List = List.copyOf(all);
      this.init_F_2 = new autismclient.util.OverlayContextMenu<>(this.init_cE.theme, Minecraft.getInstance().font, id -> AiSessionsDrawer.MENU_ITEMS, AiUiKit.rowHeight());
      this.setActiveContextMenu(this.init_F_2);
   }

   @Override
   public int getMinWidth() {
      return 280;
   }

   @Override
   public int getMinHeight() {
      return this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.CHAT && this.init_TaskTimer != null
         ? Math.max(180, 22 + AiUiKit.toolbarHeight() + 4 + 1 + this.init_TaskTimer.preferredHeight())
         : 180;
   }

   @Override
   public boolean isPersistent() {
      return this.write_Z;
   }

   @Override
   public boolean supportsHeaderCollapseClick() {
      return true;
   }

   @Override
   public AutismOverlay.RenderLayer getRenderLayer() {
      return this.write_Z ? AutismOverlay.RenderLayer.BACKGROUND_STATUS : super.getRenderLayer();
   }

   private void set_V() {
      boolean next = !this.write_Z;
      this.write_Z = next;
      this.init_cc.editSettingsAsync(s -> s.pinned = next);
      OverlayManager.getInstance().register(this, next ? AutismOverlay.RenderLayer.BACKGROUND_STATUS : AutismOverlay.RenderLayer.HOST_SCREEN);
      if (!next && Minecraft.getInstance().gui.screen() == null) {
         this.setVisible(false);
      }

      this.init_cF.invalidate();
   }

   @Override
   public boolean containsPoint(double x, double y) {
      return super.containsPoint(x, y)
         || this.windowVisible && !this.windowCollapsed && this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.SETTINGS && this.init_cf.isOverPopup(x, y);
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
      this.init(AiAssistantOverlay.Inner_DataUtil.SETTINGS);
   }

   void showMemory() {
      this.init(AiAssistantOverlay.Inner_DataUtil.MEMORY);
   }

   void write_V() {
      this.init(this.init_TaskTimer.activeInput());
   }

   int read_I() {
      return this.push;
   }

   int write_I() {
      return this.pop;
   }

   void init(autismclient.gui.ui.widgets.TextInputWidget field) {
      for (autismclient.gui.ui.widgets.TextInputWidget other : this.init_List) {
         if (other != field) {
            other.setFocused(false);
         }
      }

      field.setFocused(true);
   }

   void init(String id) {
      this.init_F_2.init(this.push, this.pop, id);
   }

   void openAction(String action, String query) {
      this.load("chat");
      this.init_TaskTimer.openAction(action, query);
   }

   void update_V() {
      this.init_TaskTimer.actionMenu.confirm();
   }

   void sync_V() {
      this.load("settings");
      this.init_cf.init_V();
   }

   void flush_V() {
      this.init_TaskTimer.actionMenu.write_V();
   }

   void close_V() {
      this.init_TaskTimer.actionMenu.update_V();
   }

   JsonObject init_JsonObject() {
      return this.init_TaskTimer.actionMenu.statusJson();
   }

   void hide() {
      this.setVisible(false);
   }

   void poll_V() {
      this.init_TaskTimer.actionMenu.cancel();
   }

   void load(String name) {
      this.init_TaskTimer.setAttachmentsEnabled("attachments".equals(name));
      switch (name == null ? "" : name) {
         case "settings":
            this.init(AiAssistantOverlay.Inner_DataUtil.SETTINGS);
            break;
         case "memory":
            this.init(AiAssistantOverlay.Inner_DataUtil.MEMORY);
            break;
         case "sessions":
            this.init(AiAssistantOverlay.Inner_DataUtil.CHAT);
            if (!this.init_ce.open) {
               this.init_ce.toggle();
            }
            break;
         default:
            this.init(AiAssistantOverlay.Inner_DataUtil.CHAT);
            this.init_ce.load();
      }

      this.init_cF.invalidate();
   }

   private void init(AiAssistantOverlay.Inner_DataUtil next) {
      if (next != this.init_AiAssistantOverlay) {
         for (autismclient.gui.ui.widgets.TextInputWidget field : this.init_List) {
            field.setFocused(false);
         }

         this.update_Z = false;
         this.init_cf.closePopups();
         this.init_cf.cancelKeyCapture();
         this.init_F_2.init_V();
         if (next == AiAssistantOverlay.Inner_DataUtil.SETTINGS) {
            this.init_cf.loadSettings(this.init_cc.getSettings());
         }

         if (next == AiAssistantOverlay.Inner_DataUtil.MEMORY) {
            this.init_ap.reset();
         }

         if (next != AiAssistantOverlay.Inner_DataUtil.CHAT) {
            this.init_ce.load();
         }

         this.init_AiAssistantOverlay = next;
         this.init_cF.invalidate();
      }
   }

   private void put() {
      if (this.init_AiAssistantOverlay != AiAssistantOverlay.Inner_DataUtil.CHAT) {
         this.init(AiAssistantOverlay.Inner_DataUtil.CHAT);
      }

      this.init_ce.toggle();
   }

   private void add_V() {
      this.update_Z = true;
      this.init_BitSetUtil.setValue(this.load_Str());
      this.init_BitSetUtil.moveCursorToEnd();
      this.init(this.init_BitSetUtil);
   }

   private void clear() {
      String text = this.init_BitSetUtil.getValue().trim();
      this.update_Z = false;
      this.init_BitSetUtil.setFocused(false);
      if (!text.isEmpty()) {
         this.init_cc.renameSession(this.init_cF.activeSessionId, text);
      }

      this.init_cF.invalidate();
   }

   private String load_Str() {
      String title = this.init_cF.activeSessionTitle == null ? "" : this.init_cF.activeSessionTitle.trim();
      return !title.isEmpty() && !title.equals("New session") ? title : (this.init_cF.settings.builtIn ? "Local tasks" : "New chat");
   }

   @Override
   public void render(GuiGraphicsExtractor graphics, int mx, int my, float delta) {
      if (this.windowVisible || this.isClosing()) {
         this.windowWidth = this.resolveWindowWidth(this.windowWidth, this.getMinWidth());
         this.windowHeight = this.resolveWindowHeight(this.windowHeight, this.getMinHeight());
         if (this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.CHAT) {
            int body = this.init_TaskTimer.bodyHeight(this.windowWidth - 8);
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
            this.init_cF.refresh(now, this.init_ce.open);
            this.recordMousePosition(mx, my);
            boolean blocked = this.init_F_2.init(mx, my)
               || this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.SETTINGS && this.init_cf.isOverPopup(mx, my);
            this.init_cE.beginFrame(graphics, mx, my, blocked, delta);
            autismclient.gui.f.NetHelper content = autismclient.gui.f.NetHelper.init(
               this.windowX + 4, this.windowY + 14 + 4, this.windowWidth - 8, this.windowHeight - 14 - 8
            );
            autismclient.gui.f.NetHelper toolbar = autismclient.gui.f.NetHelper.init(content.read(), content.write(), content.update(), AiUiKit.toolbarHeight());
            int divider = toolbar.load() + 2;
            PacketUtil.init_V_GuiGraphicsExtract_2(graphics, content.read(), divider, content.update(), this.init_cE.colors.divider);
            autismclient.gui.f.NetHelper area = autismclient.gui.f.NetHelper.init(
               content.read(), divider + 1 + 2, content.update(), Math.max(0, content.load() - divider - 1 - 2)
            );
            this.init(toolbar);
            switch (this.init_AiAssistantOverlay) {
               case CHAT:
                  this.init_TaskTimer.render(this.init_cE, area, now);
                  this.init_ce.init(this.init_cE, this.init_TaskTimer.init_NetHelper(), now);
                  break;
               case SETTINGS:
                  this.init_cf.render(this.init_cE, area, now);
                  break;
               case MEMORY:
                  this.init_ap.render(this.init_cE, area, now);
            }

            if (this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.SETTINGS) {
               this.init_cf.renderPopups(this.init_cE);
            }

            this.init_F_2.init(graphics, mx, my);
            if (!this.init_F_2.init_Z() && (this.init_AiAssistantOverlay != AiAssistantOverlay.Inner_DataUtil.SETTINGS || !this.init_cf.hasOpenDropdown())) {
               this.init_cE.renderHoveredTooltip();
            }
         }
      }
   }

   private void init(autismclient.gui.f.NetHelper bar) {
      autismclient.gui.ui.UiColorScheme colors = this.init_cE.colors;
      int h = bar.sync();
      int x = bar.read();
      int right = bar.init();
      this.init_cE
         .drawIconButton(
            autismclient.gui.f.NetHelper.init(x, bar.write(), h, h),
            autismclient.util.GuiIconAtlas.SESSIONS,
            this.init_ce.open,
            false,
            this.init_ce.open ? "Hide chats" : "Chats",
            this::put
         );
      x += h + 2 + 2;
      right -= h;
      boolean inChat = this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.CHAT;
      this.init_cE
         .drawIconButton(
            autismclient.gui.f.NetHelper.init(right, bar.write(), h, h),
            autismclient.util.GuiIconAtlas.SETTINGS,
            !inChat,
            false,
            inChat ? "Settings" : "Back to chat (Esc)",
            () -> this.init(inChat ? AiAssistantOverlay.Inner_DataUtil.SETTINGS : AiAssistantOverlay.Inner_DataUtil.CHAT)
         );
      right -= h + 2;
      this.init_cE
         .drawIconButton(
            autismclient.gui.f.NetHelper.init(right, bar.write(), h, h),
            autismclient.util.GuiIconAtlas.PIN,
            this.write_Z,
            false,
            this.write_Z ? "Pinned: stays open in-game (click to unpin)" : "Pin: keep open after the menu closes",
            this::set_V
         );
      right -= 4;
      List<String> levels = this.init_cF.availableEffortLevels;
      int chipsW = 0;

      for (String level : levels) {
         chipsW += this.init_cE.measureTextWidth(init_Map.getOrDefault(level, level)) + 10 + 2;
      }

      if (!levels.isEmpty() && right - chipsW - x >= 60) {
         int cx = right - chipsW + 2;

         for (String level : levels) {
            String label = init_Map.getOrDefault(level, level);
            autismclient.gui.f.NetHelper chip = autismclient.gui.f.NetHelper.init(cx, bar.write(), this.init_cE.measureTextWidth(label) + 10, h);
            autismclient.gui.ui.widgets.PillBadgeRenderer.init(
               this.init_cE.renderContext, chip, label, colors.accent, level.equals(this.init_cF.effectiveEffortLevel), this.init_cE.isHovered(chip)
            );
            this.init_cE.addInteractiveRegion(chip, () -> this.read(level), null, "Reasoning effort");
            cx += chip.update() + 2;
         }

         right -= chipsW + 2;
      }

      autismclient.gui.f.NetHelper title = autismclient.gui.f.NetHelper.init(x, bar.write(), Math.max(10, right - x), h);
      if (!inChat) {
         this.init_cE
            .drawCenteredRowText(
               this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.SETTINGS ? "Settings" : "Memory",
               title,
               title.read() + 4,
               title.update() - 8,
               colors.textPrimary
            );
      } else {
         if (this.update_Z && !this.init_BitSetUtil.isFocused()) {
            this.update_Z = false;
         }

         if (this.update_Z) {
            this.init_cE.drawTextInput(this.init_BitSetUtil, title);
         } else {
            if (this.init_cE.isHovered(title)) {
               PacketUtil.load(this.init_cE.graphics, title, 3, colors.cardSurfaceElevated);
            }

            String text = this.load_Str();
            this.init_cE.drawCenteredRowText(text, title, title.read() + 4, title.update() - 8, text.equals("New chat") ? colors.textSecondary : colors.textPrimary);
            this.init_cE.addInteractiveRegion(title, this::add_V, null, "Click to rename");
         }
      }
   }

   private void read(String level) {
      this.init_cc.setEffortLevel(level);
      this.init_cF.effectiveEffortLevel = level;
      this.init_cF.invalidate();
   }

   @Override
   public boolean mouseClicked(double x, double y, int button) {
      if (this.windowVisible && this.containsPoint(x, y)) {
         this.push = (int)x;
         this.pop = (int)y;
         if (this.init_F_2.init_Z()) {
            this.init_F_2.init(x, y, button, (id, label, index) -> this.init_ce.onMenuAction(id, index));
            return true;
         }

         if (this.isOverHeaderCloseButton(x, y, this.getBounds())) {
            this.setVisible(false);
            return true;
         }

         if (this.isOverHeader(x, y)) {
            this.sync_Z = true;
            this.init_D = x - this.windowX;
            this.load_D = y - this.windowY;
            return true;
         }

         if (!this.windowCollapsed && this.init_cE.isInitialized()) {
            this.flush_Z = true;
            if (this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.SETTINGS && this.init_cf.clickPopup(x, y, button)) {
               return true;
            } else if (this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.CHAT
               && this.init_ce.open
               && !this.init_ce.contains(x, y)
               && this.init_TaskTimer.init_NetHelper().init_Z((int)x, (int)y)) {
               this.init_ce.load();
               return true;
            } else {
               this.init_cE.mouseClicked(x, y, button, this.init_List);
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

      boolean used = this.init_cE.mouseReleased();
      if (this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.SETTINGS && this.init_cf.load_Z()) {
         used = true;
      }

      autismclient.gui.ui.widgets.TextInputWidget focused = this.init_BitSetUtil();
      if (focused != null && this.init_cE.isInitialized() && focused.mouseReleased(this.init_cE.layoutContext, (float)x, (float)y, button)) {
         used = true;
      }

      return moved || used;
   }

   @Override
   public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
      if (this.sync_Z) {
         this.windowX = (int)(x - this.init_D);
         this.windowY = (int)(y - this.load_D);
         return true;
      }

      if (this.init_cE.mouseDragged(y)) {
         return true;
      }

      if (this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.SETTINGS && this.init_cf.load(x, y, button)) {
         return true;
      }

      autismclient.gui.ui.widgets.TextInputWidget focused = this.init_BitSetUtil();
      return focused != null
         && this.init_cE.isInitialized()
         && focused.mouseDragged(this.init_cE.layoutContext, (float)x, (float)y, button, (float)dx, (float)dy);
   }

   @Override
   public boolean mouseScrolled(double x, double y, double amount) {
      if (!this.windowVisible || this.windowCollapsed || !this.containsPoint(x, y)) {
         return false;
      }

      if (this.init_F_2.init_Z()) {
         return true;
      }

      if (this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.SETTINGS && this.init_cf.scrollPopup(x, y, amount)) {
         return true;
      }

      this.init_cE.mouseScrolled(x, y, amount);
      return true;
   }

   @Override
   public boolean keyPressed(int key, int scan, int mods) {
      if (!this.init_cE.isInitialized()) {
         return false;
      }

      if (key == 256) {
         return this.poll_Z();
      }

      if (key == 258
         && (mods & 1) != 0
         && this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.CHAT
         && !this.init_F_2.init_Z()
         && !this.init_cF.settings.builtIn) {
         this.init_TaskTimer.init_V();
         return true;
      }

      if (this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.SETTINGS && this.init_cf.captureKey(key)) {
         return true;
      }

      autismclient.gui.ui.widgets.TextInputWidget focused = this.init_BitSetUtil();
      return this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.CHAT
            && !this.init_ce.open
            && !this.init_F_2.init_Z()
            && this.init_TaskTimer.init_Z_I_BitSetUtil(key, focused)
         ? true
         : focused != null && focused.keyPressed(this.init_cE.layoutContext, key, scan, mods);
   }

   private boolean poll_Z() {
      if (this.init_F_2.init_Z()) {
         this.init_F_2.init_V();
         return true;
      } else if (this.init_cf.closePopups() || this.init_cf.consumeKeyCapture()) {
         return true;
      } else if (this.update_Z) {
         this.update_Z = false;
         this.init_BitSetUtil.setFocused(false);
         return true;
      } else if (this.init_ce.cancelRename() || this.init_ap.cancelEdit()) {
         return true;
      } else if (this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.CHAT && !this.init_ce.open && this.init_TaskTimer.load_Z()) {
         return true;
      } else {
         autismclient.gui.ui.widgets.TextInputWidget focused = this.init_BitSetUtil();
         if (focused != null) {
            focused.setFocused(false);
            return true;
         } else if (this.init_AiAssistantOverlay != AiAssistantOverlay.Inner_DataUtil.CHAT) {
            this.init(AiAssistantOverlay.Inner_DataUtil.CHAT);
            return true;
         } else if (this.init_ce.open) {
            this.init_ce.load();
            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public boolean charTyped(char c, int mods) {
      if (!this.init_cE.isInitialized()) {
         return false;
      }

      autismclient.gui.ui.widgets.TextInputWidget focused = this.init_BitSetUtil();
      if (focused == null && this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.CHAT && !this.init_ce.open && !this.init_F_2.init_Z()) {
         autismclient.gui.ui.widgets.TextInputWidget target = this.init_TaskTimer.init_BitSetUtil_C(c);
         if (target != null) {
            this.init(target);
            return target.charTyped(this.init_cE.layoutContext, c, mods);
         }
      }

      return focused != null && focused.charTyped(this.init_cE.layoutContext, c, mods);
   }

   private autismclient.gui.ui.widgets.TextInputWidget init_BitSetUtil() {
      for (autismclient.gui.ui.widgets.TextInputWidget field : this.init_List) {
         if (field.isFocused()) {
            return field;
         }
      }

      return null;
   }

   private boolean push_Z() {
      return this.flush_Z
            && (
               this.init_AiAssistantOverlay != AiAssistantOverlay.Inner_DataUtil.CHAT
                  || this.init_ce.open
                  || this.init_cf.isCapturingKey()
                  || this.init_F_2.init_Z()
                  || this.init_TaskTimer.init_Z()
            )
         ? Minecraft.getInstance().gui.screen() instanceof autismclient.gui.screens.AutismOverlaysScreen host && host.init(this)
         : false;
   }

   @Override
   public boolean hasFocusedTextInput() {
      return this.init_BitSetUtil() != null || this.push_Z();
   }

   @Override
   public void clearInputFocus() {
      for (autismclient.gui.ui.widgets.TextInputWidget field : this.init_List) {
         field.setFocused(false);
      }

      this.flush_Z = false;
      this.init_cf.cancelKeyCapture();
   }

   enum Inner_DataUtil {
      CHAT,
      SETTINGS,
      MEMORY;
   }
}
