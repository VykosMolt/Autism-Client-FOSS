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
      .init_BitSetUtil_I(96)
      .load_BitSetUtil_Str("Chat title");
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
      this.init_I_2 = 35;
      this.load_I = 30;
      this.b__V();
      this.init_cF = new AiViewState(service);
      this.init_TaskTimer = new AiChatView(service, this.init_cF, this);
      this.init_ce = new AiSessionsDrawer(service, this.init_cF, this);
      this.init_cf = new AiSettingsView(service, this.init_cF, this);
      this.init_ap = new AiMemoryView(service, this.init_cF, this);
      this.write_Z = service.getSettings().pinned;
      this.init_BitSetUtil.load_BitSetUtil_Consumer(text -> this.clear());
      List<autismclient.gui.ui.widgets.TextInputWidget> all = new ArrayList<>(
         List.of(
            this.init_TaskTimer.init_BitSetUtil,
            this.init_ce.init_BitSetUtil,
            this.init_ce.load_BitSetUtil,
            this.init_BitSetUtil,
            this.init_ap.init_BitSetUtil,
            this.init_ap.load_BitSetUtil
         )
      );
      all.addAll(this.init_TaskTimer.init_StreamUtil.init_List());
      all.addAll(this.init_cf.init_List());
      this.init_List = List.copyOf(all);
      this.init_F_2 = new autismclient.util.OverlayContextMenu<>(this.init_cE.theme, Minecraft.getInstance().font, id -> AiSessionsDrawer.init_StrArr, AiUiKit.rowHeight());
      this.init_V_F(this.init_F_2);
   }

   @Override
   public int init_I() {
      return 280;
   }

   @Override
   public int load_I() {
      return this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.CHAT && this.init_TaskTimer != null
         ? Math.max(180, 22 + AiUiKit.toolbarHeight() + 4 + 1 + this.init_TaskTimer.init_I())
         : 180;
   }

   @Override
   public boolean f_() {
      return this.write_Z;
   }

   @Override
   public boolean load_Z() {
      return true;
   }

   @Override
   public AutismOverlay.RenderLayer init_cX_DataUtil() {
      return this.write_Z ? AutismOverlay.RenderLayer.BACKGROUND_STATUS : super.init_cX_DataUtil();
   }

   private void set_V() {
      boolean next = !this.write_Z;
      this.write_Z = next;
      this.init_cc.editSettingsAsync(s -> s.pinned = next);
      OverlayManager.getInstance().register(this, next ? AutismOverlay.RenderLayer.BACKGROUND_STATUS : AutismOverlay.RenderLayer.HOST_SCREEN);
      if (!next && Minecraft.getInstance().gui.screen() == null) {
         this.init_V_Z(false);
      }

      this.init_cF.init_V();
   }

   @Override
   public boolean init_Z_D_D(double x, double y) {
      return super.init_Z_D_D(x, y)
         || this.init_Z && !this.load_Z && this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.SETTINGS && this.init_cf.init_Z_D_D(x, y);
   }

   @Override
   public boolean read_Z() {
      return true;
   }

   @Override
   protected void c_() {
      if (!this.set_Z()) {
         this.read_I = 360;
         this.write = 240;
      }
   }

   void load_V() {
      this.init(AiAssistantOverlay.Inner_DataUtil.SETTINGS);
   }

   void read_V() {
      this.init(AiAssistantOverlay.Inner_DataUtil.MEMORY);
   }

   void write_V() {
      this.init(this.init_TaskTimer.init_BitSetUtil());
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
            other.init_V_Z(false);
         }
      }

      field.init_V_Z(true);
   }

   void init(String id) {
      this.init_F_2.init(this.push, this.pop, id);
   }

   void init_V_Str_Str(String action, String query) {
      this.load("chat");
      this.init_TaskTimer.init_V_Str_Str(action, query);
   }

   void update_V() {
      this.init_TaskTimer.init_StreamUtil.read_V();
   }

   void sync_V() {
      this.load("settings");
      this.init_cf.init_V();
   }

   void flush_V() {
      this.init_TaskTimer.init_StreamUtil.write_V();
   }

   void close_V() {
      this.init_TaskTimer.init_StreamUtil.update_V();
   }

   JsonObject init_JsonObject() {
      return this.init_TaskTimer.init_StreamUtil.init_JsonObject();
   }

   void open_V() {
      this.init_V_Z(false);
   }

   void poll_V() {
      this.init_TaskTimer.init_StreamUtil.load_V();
   }

   void load(String name) {
      this.init_TaskTimer.init_V_Z("attachments".equals(name));
      switch (name == null ? "" : name) {
         case "settings":
            this.init(AiAssistantOverlay.Inner_DataUtil.SETTINGS);
            break;
         case "memory":
            this.init(AiAssistantOverlay.Inner_DataUtil.MEMORY);
            break;
         case "sessions":
            this.init(AiAssistantOverlay.Inner_DataUtil.CHAT);
            if (!this.init_ce.init_Z) {
               this.init_ce.init_V();
            }
            break;
         default:
            this.init(AiAssistantOverlay.Inner_DataUtil.CHAT);
            this.init_ce.load();
      }

      this.init_cF.init_V();
   }

   private void init(AiAssistantOverlay.Inner_DataUtil next) {
      if (next != this.init_AiAssistantOverlay) {
         for (autismclient.gui.ui.widgets.TextInputWidget field : this.init_List) {
            field.init_V_Z(false);
         }

         this.update_Z = false;
         this.init_cf.read_Z();
         this.init_cf.load_V();
         this.init_F_2.init_V();
         if (next == AiAssistantOverlay.Inner_DataUtil.SETTINGS) {
            this.init_cf.init_V_AiSettings(this.init_cc.getSettings());
         }

         if (next == AiAssistantOverlay.Inner_DataUtil.MEMORY) {
            this.init_ap.init_V();
         }

         if (next != AiAssistantOverlay.Inner_DataUtil.CHAT) {
            this.init_ce.load();
         }

         this.init_AiAssistantOverlay = next;
         this.init_cF.init_V();
      }
   }

   private void put() {
      if (this.init_AiAssistantOverlay != AiAssistantOverlay.Inner_DataUtil.CHAT) {
         this.init(AiAssistantOverlay.Inner_DataUtil.CHAT);
      }

      this.init_ce.init_V();
   }

   private void add_V() {
      this.update_Z = true;
      this.init_BitSetUtil.init_BitSetUtil_Str(this.load_Str());
      this.init_BitSetUtil.init_BitSetUtil();
      this.init(this.init_BitSetUtil);
   }

   private void clear() {
      String text = this.init_BitSetUtil.init_Str().trim();
      this.update_Z = false;
      this.init_BitSetUtil.init_V_Z(false);
      if (!text.isEmpty()) {
         this.init_cc.renameSession(this.init_cF.read_Str, text);
      }

      this.init_cF.init_V();
   }

   private String load_Str() {
      String title = this.init_cF.load_Str == null ? "" : this.init_cF.load_Str.trim();
      return !title.isEmpty() && !title.equals("New session") ? title : (this.init_cF.init_AiSettings.builtIn ? "Local tasks" : "New chat");
   }

   @Override
   public void init_V_GuiGraphicsExtract_15(GuiGraphicsExtractor graphics, int mx, int my, float delta) {
      if (this.init_Z || this.a__Z()) {
         this.read_I = this.init_I_I_I(this.read_I, this.init_I());
         this.write = this.load_I_I_I(this.write, this.load_I());
         if (this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.CHAT) {
            int body = this.init_TaskTimer.init_I_I(this.read_I - 8);
            if (body > 0) {
               this.write = Math.min(this.write, Math.max(this.load_I(), 22 + AiUiKit.toolbarHeight() + 4 + 1 + body));
            }
         }

         OverlayBounds bounds = this.clampCurrentBounds(this);
         this.init_I_2 = bounds.x;
         this.load_I = bounds.y;
         this.read_I = bounds.width;
         this.write = bounds.height;
         this.renderWindowFrame(graphics, mx, my, this.init_cH(), "AI Assistant", this.load_Z, this.sync_Z);
         if (!this.isVisuallyCollapsed(this.load_Z)) {
            long now = System.currentTimeMillis();
            this.init_cF.init(now, this.init_ce.init_Z);
            this.load_V_I_I(mx, my);
            boolean blocked = this.init_F_2.init(mx, my)
               || this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.SETTINGS && this.init_cf.init_Z_D_D(mx, my);
            this.init_cE.beginFrame(graphics, mx, my, blocked, delta);
            autismclient.gui.f.NetHelper content = autismclient.gui.f.NetHelper.init(
               this.init_I_2 + 4, this.load_I + 14 + 4, this.read_I - 8, this.write - 14 - 8
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
                  this.init_TaskTimer.init_V_cE_2(this.init_cE, area, now);
                  this.init_ce.init(this.init_cE, this.init_TaskTimer.init_NetHelper(), now);
                  break;
               case SETTINGS:
                  this.init_cf.init_V_cE(this.init_cE, area, now);
                  break;
               case MEMORY:
                  this.init_ap.init_V_cE(this.init_cE, area, now);
            }

            if (this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.SETTINGS) {
               this.init_cf.init_V_cE(this.init_cE);
            }

            this.init_F_2.init(graphics, mx, my);
            if (!this.init_F_2.init_Z() && (this.init_AiAssistantOverlay != AiAssistantOverlay.Inner_DataUtil.SETTINGS || !this.init_cf.init_Z())) {
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
            this.init_ce.init_Z,
            false,
            this.init_ce.init_Z ? "Hide chats" : "Chats",
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
      List<String> levels = this.init_cF.write_List;
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
               this.init_cE.renderContext, chip, label, colors.accent, level.equals(this.init_cF.sync_Str), this.init_cE.isHovered(chip)
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
         if (this.update_Z && !this.init_BitSetUtil.load_Z()) {
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
      this.init_cF.sync_Str = level;
      this.init_cF.init_V();
   }

   @Override
   public boolean init_Z_D_3(double x, double y, int button) {
      if (this.init_Z && this.init_Z_D_D(x, y)) {
         this.push = (int)x;
         this.pop = (int)y;
         if (this.init_F_2.init_Z()) {
            this.init_F_2.init(x, y, button, (id, label, index) -> this.init_ce.init_V_Str_I(id, index));
            return true;
         }

         if (this.isOverHeaderCloseButton(x, y, this.init_cH())) {
            this.init_V_Z(false);
            return true;
         }

         if (this.load_Z_D_D(x, y)) {
            this.sync_Z = true;
            this.init_D = x - this.init_I_2;
            this.load_D = y - this.load_I;
            return true;
         }

         if (!this.load_Z && this.init_cE.isInitialized()) {
            this.flush_Z = true;
            if (this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.SETTINGS && this.init_cf.init_Z_D_2(x, y, button)) {
               return true;
            } else if (this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.CHAT
               && this.init_ce.init_Z
               && !this.init_ce.init_Z_D_D(x, y)
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
   public boolean load_Z_D_3(double x, double y, int button) {
      boolean moved = this.sync_Z;
      this.sync_Z = false;
      if (moved) {
         this.a__V();
      }

      boolean used = this.init_cE.mouseReleased();
      if (this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.SETTINGS && this.init_cf.load_Z()) {
         used = true;
      }

      autismclient.gui.ui.widgets.TextInputWidget focused = this.init_BitSetUtil();
      if (focused != null && this.init_cE.isInitialized() && focused.load_Z_SyncManager(this.init_cE.layoutContext, (float)x, (float)y, button)) {
         used = true;
      }

      return moved || used;
   }

   @Override
   public boolean init_Z_D(double x, double y, int button, double dx, double dy) {
      if (this.sync_Z) {
         this.init_I_2 = (int)(x - this.init_D);
         this.load_I = (int)(y - this.load_D);
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
         && focused.init_Z_SyncManager(this.init_cE.layoutContext, (float)x, (float)y, button, (float)dx, (float)dy);
   }

   @Override
   public boolean init_Z_D_2(double x, double y, double amount) {
      if (!this.init_Z || this.load_Z || !this.init_Z_D_D(x, y)) {
         return false;
      }

      if (this.init_F_2.init_Z()) {
         return true;
      }

      if (this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.SETTINGS && this.init_cf.init_Z_D(x, y, amount)) {
         return true;
      }

      this.init_cE.mouseScrolled(x, y, amount);
      return true;
   }

   @Override
   public boolean init_Z_I(int key, int scan, int mods) {
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
         && !this.init_cF.init_AiSettings.builtIn) {
         this.init_TaskTimer.init_V();
         return true;
      }

      if (this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.SETTINGS && this.init_cf.init_Z_I(key)) {
         return true;
      }

      autismclient.gui.ui.widgets.TextInputWidget focused = this.init_BitSetUtil();
      return this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.CHAT
            && !this.init_ce.init_Z
            && !this.init_F_2.init_Z()
            && this.init_TaskTimer.init_Z_I_BitSetUtil(key, focused)
         ? true
         : focused != null && focused.init_Z_SyncManager_3(this.init_cE.layoutContext, key, scan, mods);
   }

   private boolean poll_Z() {
      if (this.init_F_2.init_Z()) {
         this.init_F_2.init_V();
         return true;
      } else if (this.init_cf.read_Z() || this.init_cf.update_Z()) {
         return true;
      } else if (this.update_Z) {
         this.update_Z = false;
         this.init_BitSetUtil.init_V_Z(false);
         return true;
      } else if (this.init_ce.init_Z() || this.init_ap.init_Z()) {
         return true;
      } else if (this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.CHAT && !this.init_ce.init_Z && this.init_TaskTimer.load_Z()) {
         return true;
      } else {
         autismclient.gui.ui.widgets.TextInputWidget focused = this.init_BitSetUtil();
         if (focused != null) {
            focused.init_V_Z(false);
            return true;
         } else if (this.init_AiAssistantOverlay != AiAssistantOverlay.Inner_DataUtil.CHAT) {
            this.init(AiAssistantOverlay.Inner_DataUtil.CHAT);
            return true;
         } else if (this.init_ce.init_Z) {
            this.init_ce.load();
            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public boolean init_Z_C_I(char c, int mods) {
      if (!this.init_cE.isInitialized()) {
         return false;
      }

      autismclient.gui.ui.widgets.TextInputWidget focused = this.init_BitSetUtil();
      if (focused == null && this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.CHAT && !this.init_ce.init_Z && !this.init_F_2.init_Z()) {
         autismclient.gui.ui.widgets.TextInputWidget target = this.init_TaskTimer.init_BitSetUtil_C(c);
         if (target != null) {
            this.init(target);
            return target.init_Z_SyncManager_2(this.init_cE.layoutContext, c, mods);
         }
      }

      return focused != null && focused.init_Z_SyncManager_2(this.init_cE.layoutContext, c, mods);
   }

   private autismclient.gui.ui.widgets.TextInputWidget init_BitSetUtil() {
      for (autismclient.gui.ui.widgets.TextInputWidget field : this.init_List) {
         if (field.load_Z()) {
            return field;
         }
      }

      return null;
   }

   private boolean push_Z() {
      return this.flush_Z
            && (
               this.init_AiAssistantOverlay != AiAssistantOverlay.Inner_DataUtil.CHAT
                  || this.init_ce.init_Z
                  || this.init_cf.write_Z()
                  || this.init_F_2.init_Z()
                  || this.init_TaskTimer.init_Z()
            )
         ? Minecraft.getInstance().gui.screen() instanceof autismclient.gui.screens.AutismOverlaysScreen host && host.init(this)
         : false;
   }

   @Override
   public boolean write_Z() {
      return this.init_BitSetUtil() != null || this.push_Z();
   }

   @Override
   public void push_V() {
      for (autismclient.gui.ui.widgets.TextInputWidget field : this.init_List) {
         field.init_V_Z(false);
      }

      this.flush_Z = false;
      this.init_cf.load_V();
   }

   enum Inner_DataUtil {
      CHAT,
      SETTINGS,
      MEMORY;
   }
}
