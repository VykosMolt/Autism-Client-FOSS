package autismclient.ai;

import autismclient.gui.f.PacketUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class AiSessionsDrawer {
   static final String[] init_StrArr = new String[]{"Rename", "Branch", "Delete"};
   private static final int init_I = 8;
   private static final long init_J = 3000L;
   private static final int load_I = 4;
   private static final int read_I = 2;
   private final AiService init_cc;
   private final AiViewState init_cF;
   private final AiAssistantOverlay init_AiAssistantOverlay;
   final autismclient.gui.ui.widgets.TextInputWidget init_BitSetUtil = new autismclient.gui.ui.widgets.TextInputWidget().setMaxLength(96).setPlaceholder("Search chats");
   final autismclient.gui.ui.widgets.TextInputWidget load_BitSetUtil = new autismclient.gui.ui.widgets.TextInputWidget().setMaxLength(96);
   private final AiUiKit.Inner_StreamUtil init_cE_StreamUtil = new AiUiKit.Inner_StreamUtil(false);
   private autismclient.gui.f.NetHelper init_NetHelper = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
   boolean init_Z;
   private String init_Str;
   private String load_Str;
   private long load_J;
   private List<AiSessions.Inner_StreamUtil> init_List = List.of();
   private List<AiSessions.Inner_StreamUtil> load_List;
   private String read_Str = "";

   AiSessionsDrawer(AiService service, AiViewState view, AiAssistantOverlay host) {
      this.init_cc = service;
      this.init_cF = view;
      this.init_AiAssistantOverlay = host;
      this.load_BitSetUtil.setOnSubmit(text -> this.write());
   }

   void init_V() {
      this.init_Z = !this.init_Z;
      if (this.init_Z) {
         this.init_cF.init_V();
         this.init_cE_StreamUtil.scrollToTop();
      } else {
         this.load();
      }
   }

   void load() {
      this.init_Z = false;
      this.init_Str = null;
      this.load_Str = null;
      this.init_BitSetUtil.setValue("");
      this.init_BitSetUtil.setFocused(false);
      this.load_BitSetUtil.setFocused(false);
   }

   boolean init_Z_D_D(double x, double y) {
      return this.init_Z && this.init_NetHelper.init_Z((int)x, (int)y);
   }

   boolean init_Z() {
      if (this.init_Str == null) {
         return false;
      }

      this.init_Str = null;
      this.load_BitSetUtil.setFocused(false);
      return true;
   }

   void init(AiUiKit kit, autismclient.gui.f.NetHelper area, long now) {
      if (this.init_Z && area.sync() >= 30) {
         if (this.init_Str != null && !this.load_BitSetUtil.isFocused()) {
            this.init_Str = null;
         }

         if (this.load_Str != null && now - this.load_J > 3000L) {
            this.load_Str = null;
         }

         int w = Math.max(110, Math.min(160, area.update() * 2 / 5));
         this.init_NetHelper = autismclient.gui.f.NetHelper.init(area.read(), area.write(), Math.min(w, area.update()), area.sync());
         kit.blockClicks(this.init_NetHelper);
         kit.blockScroll(this.init_NetHelper);
         autismclient.gui.f.b.BufferHelper.init(kit.renderContext, this.init_NetHelper);
         autismclient.gui.f.NetHelper inner = this.init_NetHelper.init(4);
         int ctrl = AiUiKit.toolbarHeight();
         int y = inner.write();
         kit.drawIconLabelButton(
            autismclient.gui.f.NetHelper.init(inner.read(), y, inner.update(), ctrl),
            autismclient.util.GuiIconAtlas.PLUS,
            "New chat",
            autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
            this::read
         );
         y += ctrl + 2;
         List<AiSessions.Inner_StreamUtil> sessions = this.init_cF.update_List;
         if (sessions.size() > 8) {
            kit.drawTextInput(this.init_BitSetUtil, autismclient.gui.f.NetHelper.init(inner.read(), y, inner.update(), ctrl));
            y += ctrl + 2;
         } else if (!this.init_BitSetUtil.getValue().isEmpty() || this.init_BitSetUtil.isFocused()) {
            this.init_BitSetUtil.setValue("");
            this.init_BitSetUtil.setFocused(false);
         }

         List<AiSessions.Inner_StreamUtil> rows = this.init_List_List_Str(sessions, this.init_BitSetUtil.getValue());
         autismclient.gui.f.NetHelper list = autismclient.gui.f.NetHelper.init(inner.read(), y, inner.update(), Math.max(0, inner.load() - y));
         int rowH = AiUiKit.inputHeight();
         int step = rowH + 1;
         this.init_cE_StreamUtil.layout(list, rows.size() * step);
         kit.registerScrollPane(this.init_cE_StreamUtil);
         if (rows.isEmpty()) {
            kit.drawEllipsizedText(sessions.isEmpty() ? "No chats yet" : "No matches", list.read() + 4, list.write() + 3, list.update() - 8, kit.colors.textSecondary);
         } else {
            kit.pushClip(list);
            int rowW = list.update() - (this.init_cE_StreamUtil.maxScroll() > 0 ? 7 : 0);

            for (int i = Math.max(0, this.init_cE_StreamUtil.offset / step); i < rows.size(); i++) {
               int ry = list.write() - this.init_cE_StreamUtil.offset + i * step;
               if (ry >= list.load()) {
                  break;
               }

               this.init(kit, rows.get(i), autismclient.gui.f.NetHelper.init(list.read(), ry, rowW, rowH), now);
            }

            kit.popClip();
            kit.drawScrollbar(this.init_cE_StreamUtil);
         }
      }
   }

   private void init(AiUiKit kit, AiSessions.Inner_StreamUtil meta, autismclient.gui.f.NetHelper row, long now) {
      autismclient.gui.ui.UiColorScheme colors = kit.colors;
      String id = meta.id();
      boolean active = id.equals(this.init_cF.read_Str);
      boolean hovered = kit.isHovered(row);
      boolean armed = id.equals(this.load_Str);
      if (active) {
         PacketUtil.load(kit.graphics, row, 3, PacketUtil.init(colors.accent, 0.16F));
      } else if (hovered) {
         PacketUtil.load(kit.graphics, row, 3, colors.cardSurfaceElevated);
      }

      kit.addInteractiveRegion(row, () -> this.init_V_Str(id), () -> this.init_AiAssistantOverlay.init(id), null);
      kit.drawStatusDot(
         row.read() + 5, row.write() + row.sync() / 2, 2.0F, AiChatView.init_I_cE_Str(kit, active ? this.init_cF.init_cc_FileCache.name() : meta.state())
      );
      int textX = row.read() + 11;
      int icon = row.sync() - 2;
      if (id.equals(this.init_Str)) {
         kit.drawTextInput(this.load_BitSetUtil, autismclient.gui.f.NetHelper.init(textX - 2, row.write(), row.init() - textX + 2, row.sync()));
      } else {
         int right = row.init() - 3;
         if (!hovered && !armed) {
            String age = AiUiKit.formatRelativeAge(meta.lastActiveAt(), now);
            int ageW = kit.measureTextWidth(age);
            kit.drawCenteredRowText(age, row, right - ageW, ageW, colors.textMuted);
            right -= ageW + 4;
         } else {
            autismclient.gui.f.NetHelper trash = autismclient.gui.f.NetHelper.init(row.init() - icon - 1, row.write() + 1, icon, icon);
            kit.drawIconButton(trash, autismclient.util.GuiIconAtlas.DELETE, false, armed, armed ? "Click again to delete" : "Delete chat", () -> this.load(id));
            right = trash.read() - 3;
         }

         kit.drawCenteredRowText(init_Str_cd_StreamUtil(meta), row, textX, Math.max(1, right - textX), active ? colors.textPrimary : colors.textSecondary);
      }
   }

   static String init_Str_cd_StreamUtil(AiSessions.Inner_StreamUtil meta) {
      String title = meta.title() == null ? "" : meta.title().trim();
      return !title.isEmpty() && !title.equals("New session") ? title : "New chat";
   }

   private List<AiSessions.Inner_StreamUtil> init_List_List_Str(List<AiSessions.Inner_StreamUtil> sessions, String query) {
      String q = query.trim().toLowerCase(Locale.ROOT);
      if (sessions == this.load_List && q.equals(this.read_Str)) {
         return this.init_List;
      }

      this.load_List = sessions;
      this.read_Str = q;
      if (q.isEmpty()) {
         return this.init_List = sessions;
      }

      List<AiSessions.Inner_StreamUtil> out = new ArrayList<>();

      for (AiSessions.Inner_StreamUtil meta : sessions) {
         if (init_Str_cd_StreamUtil(meta).toLowerCase(Locale.ROOT).contains(q)) {
            out.add(meta);
         }
      }

      return this.init_List = out;
   }

   private void read() {
      this.init_cc.createSession();
      this.load();
      this.init_cF.init_V();
      this.init_AiAssistantOverlay.write_V();
   }

   private void init_V_Str(String id) {
      if (!id.equals(this.init_cF.read_Str)) {
         this.init_cc.switchSession(id);
      }

      this.load();
      this.init_cF.init_V();
   }

   private void load(String id) {
      if (!id.equals(this.load_Str)) {
         this.load_Str = id;
         this.load_J = System.currentTimeMillis();
      } else {
         this.load_Str = null;
         this.init_cc.deleteSession(id);
         this.init_cF.init_V();
         autismclient.util.bi.load("Chat deleted");
      }
   }

   void init_V_Str_I(String id, int index) {
      switch (index) {
         case 0:
            AiSessions.Inner_StreamUtil meta = null;

            for (AiSessions.Inner_StreamUtil m : this.init_cF.update_List) {
               if (m.id().equals(id)) {
                  meta = m;
               }
            }

            this.init_Str = id;
            this.load_BitSetUtil.setValue(meta == null ? "" : init_Str_cd_StreamUtil(meta));
            this.load_BitSetUtil.moveCursorToEnd();
            this.init_AiAssistantOverlay.init(this.load_BitSetUtil);
            break;
         case 1:
            if (!this.init_cc.branchSession(id)) {
               autismclient.util.bi.read("Nothing to branch yet");
            } else {
               autismclient.util.bi.load("Branched into a new chat");
               this.load();
            }

            this.init_cF.init_V();
            break;
         case 2:
            this.init_cc.deleteSession(id);
            this.init_cF.init_V();
            autismclient.util.bi.load("Chat deleted");
      }
   }

   private void write() {
      String id = this.init_Str;
      String text = this.load_BitSetUtil.getValue().trim();
      this.init_Str = null;
      this.load_BitSetUtil.setFocused(false);
      if (id != null && !text.isEmpty()) {
         this.init_cc.renameSession(id, text);
         this.init_cF.init_V();
      }
   }
}
