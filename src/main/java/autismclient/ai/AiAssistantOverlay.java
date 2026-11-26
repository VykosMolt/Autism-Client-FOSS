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
   private final cc init_cc;
   private final cF init_cF;
   private final cE init_cE = new cE();
   private final TaskTimer init_TaskTimer;
   private final ce init_ce;
   private final cf init_cf;
   private final ap init_ap;
   private final autismclient.gui.f.b.BitSetUtil init_BitSetUtil = new autismclient.gui.f.b.BitSetUtil()
      .init_BitSetUtil_I(96)
      .load_BitSetUtil_Str("Chat title");
   private final List<autismclient.gui.f.b.BitSetUtil> init_List;
   private final autismclient.util.OverlayContextMenu<String> init_F_2;
   private AiAssistantOverlay.Inner_DataUtil init_AiAssistantOverlay = AiAssistantOverlay.Inner_DataUtil.init;
   private boolean write_Z;
   private boolean update_Z;
   private boolean sync_Z;
   private boolean flush_Z;
   private double init_D;
   private double load_D;
   private int push;
   private int pop;

   AiAssistantOverlay(cc service) {
      super("ai-assistant", 360, 240);
      this.init_cc = service;
      this.init_I_2 = 35;
      this.load_I = 30;
      this.b__V();
      this.init_cF = new cF(service);
      this.init_TaskTimer = new TaskTimer(service, this.init_cF, this);
      this.init_ce = new ce(service, this.init_cF, this);
      this.init_cf = new cf(service, this.init_cF, this);
      this.init_ap = new ap(service, this.init_cF, this);
      this.write_Z = service.init_AiSettings().pinned;
      this.init_BitSetUtil.load_BitSetUtil_Consumer(text -> this.clear());
      List<autismclient.gui.f.b.BitSetUtil> all = new ArrayList<>(
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
      this.init_F_2 = new autismclient.util.OverlayContextMenu<>(this.init_cE.init_CharEncoder, Minecraft.getInstance().font, id -> ce.init_StrArr, cE.read());
      this.init_V_F(this.init_F_2);
   }

   @Override
   public int init_I() {
      return 280;
   }

   @Override
   public int load_I() {
      return this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.init && this.init_TaskTimer != null
         ? Math.max(180, 22 + cE.init_I() + 4 + 1 + this.init_TaskTimer.init_I())
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
   public AutismOverlay.Inner_DataUtil init_cX_DataUtil() {
      return this.write_Z ? AutismOverlay.Inner_DataUtil.BACKGROUND_STATUS : super.init_cX_DataUtil();
   }

   private void set_V() {
      boolean next = !this.write_Z;
      this.write_Z = next;
      this.init_cc.init_V_Consumer(s -> s.pinned = next);
      OverlayManager.init_bk().init_V_cX_cX_DataUtil(this, next ? AutismOverlay.Inner_DataUtil.BACKGROUND_STATUS : AutismOverlay.Inner_DataUtil.HOST_SCREEN);
      if (!next && Minecraft.getInstance().gui.screen() == null) {
         this.init_V_Z(false);
      }

      this.init_cF.init_V();
   }

   @Override
   public boolean init_Z_D_D(double x, double y) {
      return super.init_Z_D_D(x, y)
         || this.init_Z && !this.load_Z && this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.load && this.init_cf.init_Z_D_D(x, y);
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
      this.init(AiAssistantOverlay.Inner_DataUtil.load);
   }

   void read_V() {
      this.init(AiAssistantOverlay.Inner_DataUtil.read);
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

   void init(autismclient.gui.f.b.BitSetUtil field) {
      for (autismclient.gui.f.b.BitSetUtil other : this.init_List) {
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
            this.init(AiAssistantOverlay.Inner_DataUtil.load);
            break;
         case "memory":
            this.init(AiAssistantOverlay.Inner_DataUtil.read);
            break;
         case "sessions":
            this.init(AiAssistantOverlay.Inner_DataUtil.init);
            if (!this.init_ce.init_Z) {
               this.init_ce.init_V();
            }
            break;
         default:
            this.init(AiAssistantOverlay.Inner_DataUtil.init);
            this.init_ce.load();
      }

      this.init_cF.init_V();
   }

   private void init(AiAssistantOverlay.Inner_DataUtil next) {
      if (next != this.init_AiAssistantOverlay) {
         for (autismclient.gui.f.b.BitSetUtil field : this.init_List) {
            field.init_V_Z(false);
         }

         this.update_Z = false;
         this.init_cf.read_Z();
         this.init_cf.load_V();
         this.init_F_2.init_V();
         if (next == AiAssistantOverlay.Inner_DataUtil.load) {
            this.init_cf.init_V_AiSettings(this.init_cc.init_AiSettings());
         }

         if (next == AiAssistantOverlay.Inner_DataUtil.read) {
            this.init_ap.init_V();
         }

         if (next != AiAssistantOverlay.Inner_DataUtil.init) {
            this.init_ce.load();
         }

         this.init_AiAssistantOverlay = next;
         this.init_cF.init_V();
      }
   }

   private void put() {
      if (this.init_AiAssistantOverlay != AiAssistantOverlay.Inner_DataUtil.init) {
         this.init(AiAssistantOverlay.Inner_DataUtil.init);
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
         this.init_cc.load_V_Str_Str(this.init_cF.read_Str, text);
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
         if (this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.init) {
            int body = this.init_TaskTimer.init_I_I(this.read_I - 8);
            if (body > 0) {
               this.write = Math.min(this.write, Math.max(this.load_I(), 22 + cE.init_I() + 4 + 1 + body));
            }
         }

         OverlayBounds bounds = this.init_cH_cX(this);
         this.init_I_2 = bounds.init_I;
         this.load_I = bounds.load_I;
         this.read_I = bounds.read;
         this.write = bounds.write;
         this.init_V_GuiGraphicsExtract_6(graphics, mx, my, this.init_cH(), "AI Assistant", this.load_Z, this.sync_Z);
         if (!this.init_Z_Z(this.load_Z)) {
            long now = System.currentTimeMillis();
            this.init_cF.init(now, this.init_ce.init_Z);
            this.load_V_I_I(mx, my);
            boolean blocked = this.init_F_2.init(mx, my)
               || this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.load && this.init_cf.init_Z_D_D(mx, my);
            this.init_cE.init_V_GuiGraphicsExtract(graphics, mx, my, blocked, delta);
            autismclient.gui.f.NetHelper content = autismclient.gui.f.NetHelper.init(
               this.init_I_2 + 4, this.load_I + 14 + 4, this.read_I - 8, this.write - 14 - 8
            );
            autismclient.gui.f.NetHelper toolbar = autismclient.gui.f.NetHelper.init(content.read(), content.write(), content.update(), cE.init_I());
            int divider = toolbar.load() + 2;
            PacketUtil.init_V_GuiGraphicsExtract_2(graphics, content.read(), divider, content.update(), this.init_cE.init_JsonUtil.put);
            autismclient.gui.f.NetHelper area = autismclient.gui.f.NetHelper.init(
               content.read(), divider + 1 + 2, content.update(), Math.max(0, content.load() - divider - 1 - 2)
            );
            this.init(toolbar);
            switch (this.init_AiAssistantOverlay) {
               case init:
                  this.init_TaskTimer.init_V_cE_2(this.init_cE, area, now);
                  this.init_ce.init(this.init_cE, this.init_TaskTimer.init_NetHelper(), now);
                  break;
               case load:
                  this.init_cf.init_V_cE(this.init_cE, area, now);
                  break;
               case read:
                  this.init_ap.init_V_cE(this.init_cE, area, now);
            }

            if (this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.load) {
               this.init_cf.init_V_cE(this.init_cE);
            }

            this.init_F_2.init(graphics, mx, my);
            if (!this.init_F_2.init_Z() && (this.init_AiAssistantOverlay != AiAssistantOverlay.Inner_DataUtil.load || !this.init_cf.init_Z())) {
               this.init_cE.load_V();
            }
         }
      }
   }

   private void init(autismclient.gui.f.NetHelper bar) {
      autismclient.gui.f.JsonUtil colors = this.init_cE.init_JsonUtil;
      int h = bar.sync();
      int x = bar.read();
      int right = bar.init();
      this.init_cE
         .init(
            autismclient.gui.f.NetHelper.init(x, bar.write(), h, h),
            autismclient.util.GuiIconAtlas.get,
            this.init_ce.init_Z,
            false,
            this.init_ce.init_Z ? "Hide chats" : "Chats",
            this::put
         );
      x += h + 2 + 2;
      right -= h;
      boolean inChat = this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.init;
      this.init_cE
         .init(
            autismclient.gui.f.NetHelper.init(right, bar.write(), h, h),
            autismclient.util.GuiIconAtlas.set,
            !inChat,
            false,
            inChat ? "Settings" : "Back to chat (Esc)",
            () -> this.init(inChat ? AiAssistantOverlay.Inner_DataUtil.load : AiAssistantOverlay.Inner_DataUtil.init)
         );
      right -= h + 2;
      this.init_cE
         .init(
            autismclient.gui.f.NetHelper.init(right, bar.write(), h, h),
            autismclient.util.GuiIconAtlas.add,
            this.write_Z,
            false,
            this.write_Z ? "Pinned: stays open in-game (click to unpin)" : "Pin: keep open after the menu closes",
            this::set_V
         );
      right -= 4;
      List<String> levels = this.init_cF.write_List;
      int chipsW = 0;

      for (String level : levels) {
         chipsW += this.init_cE.init_I_Str(init_Map.getOrDefault(level, level)) + 10 + 2;
      }

      if (!levels.isEmpty() && right - chipsW - x >= 60) {
         int cx = right - chipsW + 2;

         for (String level : levels) {
            String label = init_Map.getOrDefault(level, level);
            autismclient.gui.f.NetHelper chip = autismclient.gui.f.NetHelper.init(cx, bar.write(), this.init_cE.init_I_Str(label) + 10, h);
            autismclient.gui.f.b.ConfigStore.init(
               this.init_cE.init_SyncManager, chip, label, colors.clear, level.equals(this.init_cF.sync_Str), this.init_cE.init_Z_NetHelper(chip)
            );
            this.init_cE.init_V_NetHelper(chip, () -> this.read(level), null, "Reasoning effort");
            cx += chip.update() + 2;
         }

         right -= chipsW + 2;
      }

      autismclient.gui.f.NetHelper title = autismclient.gui.f.NetHelper.init(x, bar.write(), Math.max(10, right - x), h);
      if (!inChat) {
         this.init_cE
            .init_V_Str_3(
               this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.load ? "Settings" : "Memory",
               title,
               title.read() + 4,
               title.update() - 8,
               colors.prev
            );
      } else {
         if (this.update_Z && !this.init_BitSetUtil.load_Z()) {
            this.update_Z = false;
         }

         if (this.update_Z) {
            this.init_cE.init_V_BitSetUtil_NetHelper(this.init_BitSetUtil, title);
         } else {
            if (this.init_cE.init_Z_NetHelper(title)) {
               PacketUtil.load(this.init_cE.init_GuiGraphicsExtract, title, 3, colors.push);
            }

            String text = this.load_Str();
            this.init_cE.init_V_Str_3(text, title, title.read() + 4, title.update() - 8, text.equals("New chat") ? colors.valid : colors.prev);
            this.init_cE.init_V_NetHelper(title, this::add_V, null, "Click to rename");
         }
      }
   }

   private void read(String level) {
      this.init_cc.poll(level);
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

         if (this.write(x, y, this.init_cH())) {
            this.init_V_Z(false);
            return true;
         }

         if (this.load_Z_D_D(x, y)) {
            this.sync_Z = true;
            this.init_D = x - this.init_I_2;
            this.load_D = y - this.load_I;
            return true;
         }

         if (!this.load_Z && this.init_cE.init_Z()) {
            this.flush_Z = true;
            if (this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.load && this.init_cf.init_Z_D_2(x, y, button)) {
               return true;
            } else if (this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.init
               && this.init_ce.init_Z
               && !this.init_ce.init_Z_D_D(x, y)
               && this.init_TaskTimer.init_NetHelper().init_Z((int)x, (int)y)) {
               this.init_ce.load();
               return true;
            } else {
               this.init_cE.init_Z_D(x, y, button, this.init_List);
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

      boolean used = this.init_cE.load_Z();
      if (this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.load && this.init_cf.load_Z()) {
         used = true;
      }

      autismclient.gui.f.b.BitSetUtil focused = this.init_BitSetUtil();
      if (focused != null && this.init_cE.init_Z() && focused.load_Z_SyncManager(this.init_cE.init_SyncManager_2, (float)x, (float)y, button)) {
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

      if (this.init_cE.init_Z_D(y)) {
         return true;
      }

      if (this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.load && this.init_cf.load(x, y, button)) {
         return true;
      }

      autismclient.gui.f.b.BitSetUtil focused = this.init_BitSetUtil();
      return focused != null
         && this.init_cE.init_Z()
         && focused.init_Z_SyncManager(this.init_cE.init_SyncManager_2, (float)x, (float)y, button, (float)dx, (float)dy);
   }

   @Override
   public boolean init_Z_D_2(double x, double y, double amount) {
      if (!this.init_Z || this.load_Z || !this.init_Z_D_D(x, y)) {
         return false;
      }

      if (this.init_F_2.init_Z()) {
         return true;
      }

      if (this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.load && this.init_cf.init_Z_D(x, y, amount)) {
         return true;
      }

      this.init_cE.init(x, y, amount);
      return true;
   }

   @Override
   public boolean init_Z_I(int key, int scan, int mods) {
      if (!this.init_cE.init_Z()) {
         return false;
      }

      if (key == 256) {
         return this.poll_Z();
      }

      if (key == 258
         && (mods & 1) != 0
         && this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.init
         && !this.init_F_2.init_Z()
         && !this.init_cF.init_AiSettings.builtIn) {
         this.init_TaskTimer.init_V();
         return true;
      }

      if (this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.load && this.init_cf.init_Z_I(key)) {
         return true;
      }

      autismclient.gui.f.b.BitSetUtil focused = this.init_BitSetUtil();
      return this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.init
            && !this.init_ce.init_Z
            && !this.init_F_2.init_Z()
            && this.init_TaskTimer.init_Z_I_BitSetUtil(key, focused)
         ? true
         : focused != null && focused.init_Z_SyncManager_3(this.init_cE.init_SyncManager_2, key, scan, mods);
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
      } else if (this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.init && !this.init_ce.init_Z && this.init_TaskTimer.load_Z()) {
         return true;
      } else {
         autismclient.gui.f.b.BitSetUtil focused = this.init_BitSetUtil();
         if (focused != null) {
            focused.init_V_Z(false);
            return true;
         } else if (this.init_AiAssistantOverlay != AiAssistantOverlay.Inner_DataUtil.init) {
            this.init(AiAssistantOverlay.Inner_DataUtil.init);
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
      if (!this.init_cE.init_Z()) {
         return false;
      }

      autismclient.gui.f.b.BitSetUtil focused = this.init_BitSetUtil();
      if (focused == null && this.init_AiAssistantOverlay == AiAssistantOverlay.Inner_DataUtil.init && !this.init_ce.init_Z && !this.init_F_2.init_Z()) {
         autismclient.gui.f.b.BitSetUtil target = this.init_TaskTimer.init_BitSetUtil_C(c);
         if (target != null) {
            this.init(target);
            return target.init_Z_SyncManager_2(this.init_cE.init_SyncManager_2, c, mods);
         }
      }

      return focused != null && focused.init_Z_SyncManager_2(this.init_cE.init_SyncManager_2, c, mods);
   }

   private autismclient.gui.f.b.BitSetUtil init_BitSetUtil() {
      for (autismclient.gui.f.b.BitSetUtil field : this.init_List) {
         if (field.load_Z()) {
            return field;
         }
      }

      return null;
   }

   private boolean push_Z() {
      return this.flush_Z
            && (
               this.init_AiAssistantOverlay != AiAssistantOverlay.Inner_DataUtil.init
                  || this.init_ce.init_Z
                  || this.init_cf.write_Z()
                  || this.init_F_2.init_Z()
                  || this.init_TaskTimer.init_Z()
            )
         ? Minecraft.getInstance().gui.screen() instanceof autismclient.gui.d.AutismOverlaysScreen host && host.init(this)
         : false;
   }

   @Override
   public boolean write_Z() {
      return this.init_BitSetUtil() != null || this.push_Z();
   }

   @Override
   public void push_V() {
      for (autismclient.gui.f.b.BitSetUtil field : this.init_List) {
         field.init_V_Z(false);
      }

      this.flush_Z = false;
      this.init_cf.load_V();
   }

   enum Inner_DataUtil {
      init,
      load,
      read;
   }
}
