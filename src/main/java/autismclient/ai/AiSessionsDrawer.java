package autismclient.ai;

import autismclient.gui.f.PacketUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class AiSessionsDrawer {
   static final String[] MENU_ITEMS = new String[]{"Rename", "Branch", "Delete"};
   private static final int init_I = 8;
   private static final long init_J = 3000L;
   private static final int load_I = 4;
   private static final int read_I = 2;
   private final AiService service;
   private final AiViewState view;
   private final AiAssistantOverlay host;
   final autismclient.gui.ui.widgets.TextInputWidget searchInput = new autismclient.gui.ui.widgets.TextInputWidget().setMaxLength(96).setPlaceholder("Search chats");
   final autismclient.gui.ui.widgets.TextInputWidget renameInput = new autismclient.gui.ui.widgets.TextInputWidget().setMaxLength(96);
   private final AiUiKit.ScrollPane scroller = new AiUiKit.ScrollPane(false);
   private autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
   boolean open;
   private String init_Str;
   private String load_Str;
   private long load_J;
   private List<AiSessions.SessionMeta> filteredSessions = List.of();
   private List<AiSessions.SessionMeta> lastSessions;
   private String lastQuery = "";

   AiSessionsDrawer(AiService service, AiViewState view, AiAssistantOverlay host) {
      this.service = service;
      this.view = view;
      this.host = host;
      this.renameInput.setOnSubmit(text -> this.commitRename());
   }

   void toggle() {
      this.open = !this.open;
      if (this.open) {
         this.view.invalidate();
         this.scroller.scrollToTop();
      } else {
         this.closeDrawer();
      }
   }

   void closeDrawer() {
      this.open = false;
      this.init_Str = null;
      this.load_Str = null;
      this.searchInput.setValue("");
      this.searchInput.setFocused(false);
      this.renameInput.setFocused(false);
   }

   boolean contains(double x, double y) {
      return this.open && this.bounds.init_Z((int)x, (int)y);
   }

   boolean cancelRename() {
      if (this.init_Str == null) {
         return false;
      }

      this.init_Str = null;
      this.renameInput.setFocused(false);
      return true;
   }

   void render(AiUiKit kit, autismclient.gui.f.NetHelper area, long now) {
      if (this.open && area.sync() >= 30) {
         if (this.init_Str != null && !this.renameInput.isFocused()) {
            this.init_Str = null;
         }

         if (this.load_Str != null && now - this.load_J > 3000L) {
            this.load_Str = null;
         }

         int w = Math.max(110, Math.min(160, area.update() * 2 / 5));
         this.bounds = autismclient.gui.f.NetHelper.init(area.read(), area.write(), Math.min(w, area.update()), area.sync());
         kit.blockClicks(this.bounds);
         kit.blockScroll(this.bounds);
         autismclient.gui.f.b.BufferHelper.init(kit.renderContext, this.bounds);
         autismclient.gui.f.NetHelper inner = this.bounds.init(4);
         int ctrl = AiUiKit.toolbarHeight();
         int y = inner.write();
         kit.drawIconLabelButton(
            autismclient.gui.f.NetHelper.init(inner.read(), y, inner.update(), ctrl),
            autismclient.util.GuiIconAtlas.PLUS,
            "New chat",
            autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
            this::createSession
         );
         y += ctrl + 2;
         List<AiSessions.SessionMeta> sessions = this.view.sessions;
         if (sessions.size() > 8) {
            kit.drawTextInput(this.searchInput, autismclient.gui.f.NetHelper.init(inner.read(), y, inner.update(), ctrl));
            y += ctrl + 2;
         } else if (!this.searchInput.getValue().isEmpty() || this.searchInput.isFocused()) {
            this.searchInput.setValue("");
            this.searchInput.setFocused(false);
         }

         List<AiSessions.SessionMeta> rows = this.filterSessions(sessions, this.searchInput.getValue());
         autismclient.gui.f.NetHelper list = autismclient.gui.f.NetHelper.init(inner.read(), y, inner.update(), Math.max(0, inner.load() - y));
         int rowH = AiUiKit.inputHeight();
         int step = rowH + 1;
         this.scroller.layout(list, rows.size() * step);
         kit.registerScrollPane(this.scroller);
         if (rows.isEmpty()) {
            kit.drawEllipsizedText(sessions.isEmpty() ? "No chats yet" : "No matches", list.read() + 4, list.write() + 3, list.update() - 8, kit.colors.textSecondary);
         } else {
            kit.pushClip(list);
            int rowW = list.update() - (this.scroller.maxScroll() > 0 ? 7 : 0);

            for (int i = Math.max(0, this.scroller.offset / step); i < rows.size(); i++) {
               int ry = list.write() - this.scroller.offset + i * step;
               if (ry >= list.load()) {
                  break;
               }

               this.renderRow(kit, rows.get(i), autismclient.gui.f.NetHelper.init(list.read(), ry, rowW, rowH), now);
            }

            kit.popClip();
            kit.drawScrollbar(this.scroller);
         }
      }
   }

   private void renderRow(AiUiKit kit, AiSessions.SessionMeta meta, autismclient.gui.f.NetHelper row, long now) {
      autismclient.gui.ui.UiColorScheme colors = kit.colors;
      String id = meta.id();
      boolean active = id.equals(this.view.activeSessionId);
      boolean hovered = kit.isHovered(row);
      boolean armed = id.equals(this.load_Str);
      if (active) {
         PacketUtil.load(kit.graphics, row, 3, PacketUtil.init(colors.accent, 0.16F));
      } else if (hovered) {
         PacketUtil.load(kit.graphics, row, 3, colors.cardSurfaceElevated);
      }

      kit.addInteractiveRegion(row, () -> this.openSession(id), () -> this.host.openContextMenu(id), null);
      kit.drawStatusDot(
         row.read() + 5, row.write() + row.sync() / 2, 2.0F, AiChatView.stateColor(kit, active ? this.view.runState.name() : meta.state())
      );
      int textX = row.read() + 11;
      int icon = row.sync() - 2;
      if (id.equals(this.init_Str)) {
         kit.drawTextInput(this.renameInput, autismclient.gui.f.NetHelper.init(textX - 2, row.write(), row.init() - textX + 2, row.sync()));
      } else {
         int right = row.init() - 3;
         if (!hovered && !armed) {
            String age = AiUiKit.formatRelativeAge(meta.lastActiveAt(), now);
            int ageW = kit.measureTextWidth(age);
            kit.drawCenteredRowText(age, row, right - ageW, ageW, colors.textMuted);
            right -= ageW + 4;
         } else {
            autismclient.gui.f.NetHelper trash = autismclient.gui.f.NetHelper.init(row.init() - icon - 1, row.write() + 1, icon, icon);
            kit.drawIconButton(trash, autismclient.util.GuiIconAtlas.DELETE, false, armed, armed ? "Click again to delete" : "Delete chat", () -> this.confirmDelete(id));
            right = trash.read() - 3;
         }

         kit.drawCenteredRowText(displayTitle(meta), row, textX, Math.max(1, right - textX), active ? colors.textPrimary : colors.textSecondary);
      }
   }

   static String displayTitle(AiSessions.SessionMeta meta) {
      String title = meta.title() == null ? "" : meta.title().trim();
      return !title.isEmpty() && !title.equals("New session") ? title : "New chat";
   }

   private List<AiSessions.SessionMeta> filterSessions(List<AiSessions.SessionMeta> sessions, String query) {
      String q = query.trim().toLowerCase(Locale.ROOT);
      if (sessions == this.lastSessions && q.equals(this.lastQuery)) {
         return this.filteredSessions;
      }

      this.lastSessions = sessions;
      this.lastQuery = q;
      if (q.isEmpty()) {
         return this.filteredSessions = sessions;
      }

      List<AiSessions.SessionMeta> out = new ArrayList<>();

      for (AiSessions.SessionMeta meta : sessions) {
         if (displayTitle(meta).toLowerCase(Locale.ROOT).contains(q)) {
            out.add(meta);
         }
      }

      return this.filteredSessions = out;
   }

   private void createSession() {
      this.service.createSession();
      this.closeDrawer();
      this.view.invalidate();
      this.host.focusActiveInput();
   }

   private void openSession(String id) {
      if (!id.equals(this.view.activeSessionId)) {
         this.service.switchSession(id);
      }

      this.closeDrawer();
      this.view.invalidate();
   }

   private void confirmDelete(String id) {
      if (!id.equals(this.load_Str)) {
         this.load_Str = id;
         this.load_J = System.currentTimeMillis();
      } else {
         this.load_Str = null;
         this.service.deleteSession(id);
         this.view.invalidate();
         autismclient.util.bi.load("Chat deleted");
      }
   }

   void onMenuAction(String id, int index) {
      switch (index) {
         case 0:
            AiSessions.SessionMeta meta = null;

            for (AiSessions.SessionMeta m : this.view.sessions) {
               if (m.id().equals(id)) {
                  meta = m;
               }
            }

            this.init_Str = id;
            this.renameInput.setValue(meta == null ? "" : displayTitle(meta));
            this.renameInput.moveCursorToEnd();
            this.host.focusInput(this.renameInput);
            break;
         case 1:
            if (!this.service.branchSession(id)) {
               autismclient.util.bi.read("Nothing to branch yet");
            } else {
               autismclient.util.bi.load("Branched into a new chat");
               this.closeDrawer();
            }

            this.view.invalidate();
            break;
         case 2:
            this.service.deleteSession(id);
            this.view.invalidate();
            autismclient.util.bi.load("Chat deleted");
      }
   }

   private void commitRename() {
      String id = this.init_Str;
      String text = this.renameInput.getValue().trim();
      this.init_Str = null;
      this.renameInput.setFocused(false);
      if (id != null && !text.isEmpty()) {
         this.service.renameSession(id, text);
         this.view.invalidate();
      }
   }
}
