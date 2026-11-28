package autismclient.ai;

import autismclient.gui.f.PacketUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;

final class AiMemoryView {
   private static final long init_J = 3000L;
   private static final long load_J = 1000L;
   private static final int init_I = 2;
   private final AiService service;
   private final AiViewState view;
   private final AiAssistantOverlay host;
   final autismclient.gui.ui.widgets.TextInputWidget searchInput = new autismclient.gui.ui.widgets.TextInputWidget().setMaxLength(96).setPlaceholder("Search memory");
   final autismclient.gui.ui.widgets.TextInputWidget editInput = new autismclient.gui.ui.widgets.TextInputWidget().setMaxLength(4000);
   private final AiUiKit.ScrollPane scroll = new AiUiKit.ScrollPane(false);
   private final Set<String> expandedIds = new HashSet<>();
   private final Map<String, AiMemoryView.WrappedText> wrapCache = new HashMap<>();
   private List<AiMemoryView.MemoryRow> rows = List.of();
   private Object lastSource;
   private String query;
   private long nextRefreshAt;
   private String editingId;
   private String id;
   private String armedClearScope;
   private long armedForgetAt;
   private long armedClearAt;

   AiMemoryView(AiService service, AiViewState view, AiAssistantOverlay host) {
      this.service = service;
      this.view = view;
      this.host = host;
      this.editInput.setOnSubmit(text -> this.commitEdit());
   }

   void reset() {
      this.searchInput.setValue("");
      this.editingId = this.id = this.armedClearScope = null;
      this.expandedIds.clear();
      this.wrapCache.clear();
      this.lastSource = null;
      this.scroll.scrollToTop();
   }

   boolean cancelEdit() {
      if (this.editingId == null) {
         return false;
      }

      this.editingId = null;
      this.editInput.setFocused(false);
      return true;
   }

   private void refreshRows(long now) {
      Object source = this.service.getMemoryStore().memories();
      String query = this.searchInput.getValue().trim();
      if (source != this.lastSource || !query.equals(this.query) || now >= this.nextRefreshAt) {
         this.lastSource = source;
         this.query = query;
         this.nextRefreshAt = now + 1000L;
         List<AiMemoryView.MemoryRow> rows = new ArrayList<>();

         for (AiMemoryStore.Memory memory : this.service.searchMemories(query)) {
            rows.add(new AiMemoryView.MemoryRow(memory, AiUiKit.flattenSingleLine(memory.text())));
         }

         this.rows = rows;
      }
   }

   void render(AiUiKit kit, autismclient.gui.f.NetHelper area, long now) {
      this.refreshRows(now);
      if (this.editingId != null && !this.editInput.isFocused()) {
         this.editingId = null;
      }

      if (this.id != null && now - this.armedForgetAt > 3000L) {
         this.id = null;
      }

      if (this.armedClearScope != null && now - this.armedClearAt > 3000L) {
         this.armedClearScope = null;
      }

      autismclient.gui.ui.UiColorScheme colors = kit.colors;
      int ctrl = AiUiKit.toolbarHeight();
      String count = this.rows.size() == 1 ? "1 memory" : this.rows.size() + " memories";
      int countW = kit.measureTextWidth(count);
      kit.drawTextInput(
         this.searchInput, autismclient.gui.f.NetHelper.init(area.read(), area.write(), Math.max(40, area.update() - countW - 8), ctrl)
      );
      kit.drawCenteredRowText(
         count, autismclient.gui.f.NetHelper.init(area.init() - countW - 2, area.write(), countW + 2, ctrl), area.init() - countW - 2, countW + 2, colors.textSecondary
      );
      autismclient.gui.f.NetHelper footer = autismclient.gui.f.NetHelper.init(area.read(), area.load() - ctrl, area.update(), ctrl);
      int top = area.write() + ctrl + 2;
      autismclient.gui.f.NetHelper viewport = autismclient.gui.f.NetHelper.init(area.read(), top, area.update(), Math.max(0, footer.write() - 2 - top));
      this.renderRows(kit, viewport, now);
      if (!this.rows.isEmpty() || !this.searchInput.getValue().isBlank()) {
         this.renderFooter(kit, footer);
      }
   }

   private void renderRows(AiUiKit kit, autismclient.gui.f.NetHelper v, long now) {
      autismclient.gui.ui.UiColorScheme colors = kit.colors;
      int rowH = AiUiKit.inputHeight();
      int line = AiUiKit.lineHeight();
      int rowW = v.update() - 6 - 2;
      int chipW = 0;

      for (String label : new String[]{"USER", "WORLD", "PROC", "HYP"}) {
         chipW = Math.max(chipW, kit.measureTextWidth(label) + 8);
      }

      int textX = chipW + 6;
      int textW = Math.max(20, rowW - textX - 4 - (rowH - 2) * 2 - 2 - 4);
      int total = 0;

      for (AiMemoryView.MemoryRow row : this.rows) {
         total += this.rowHeight(kit, row, textW, rowH, line) + 1;
      }

      this.scroll.layout(v, total);
      kit.registerScrollPane(this.scroll);
      if (this.rows.isEmpty()) {
         kit.drawEllipsizedText(
            this.searchInput.getValue().isBlank() ? "Nothing remembered yet" : "No matches", v.read() + 4, v.write() + 3, v.update() - 8, colors.textSecondary
         );
      } else {
         kit.pushClip(v);
         int y = v.write() - this.scroll.offset;

         for (AiMemoryView.MemoryRow entry : this.rows) {
            int h = this.rowHeight(kit, entry, textW, rowH, line);
            if (y >= v.load()) {
               break;
            }

            if (y + h > v.write()) {
               this.renderRow(kit, entry, autismclient.gui.f.NetHelper.init(v.read(), y, rowW, h), chipW, textX, textW, now);
            }

            y += h + 1;
         }

         kit.popClip();
         kit.drawScrollbar(this.scroll);
      }
   }

   private int rowHeight(AiUiKit kit, AiMemoryView.MemoryRow entry, int textW, int rowH, int line) {
      String id = entry.memory().id();
      if (this.expandedIds.contains(id) && !id.equals(this.editingId)) {
         int lines = this.wrappedLines(kit, entry, textW).size();
         return rowH + Math.max(0, lines - 1) * line + line + 2;
      } else {
         return rowH;
      }
   }

   private List<String> wrappedLines(AiUiKit kit, AiMemoryView.MemoryRow entry, int textW) {
      int epoch = AiChatView.textEpoch();
      AiMemoryView.WrappedText wrap = this.wrapCache.get(entry.memory().id());
      if (wrap == null || wrap.width() != textW || wrap.epoch() != epoch || !wrap.text().equals(entry.text())) {
         wrap = new AiMemoryView.WrappedText(entry.text(), textW, epoch, kit.renderContext.init_PoolEntry().wrapLines(entry.text(), textW));
         this.wrapCache.put(entry.memory().id(), wrap);
      }

      return wrap.lines();
   }

   private void renderRow(AiUiKit kit, AiMemoryView.MemoryRow entry, autismclient.gui.f.NetHelper row, int chipW, int textX, int textW, long now) {
      autismclient.gui.ui.UiColorScheme colors = kit.colors;
      AiMemoryStore.Memory memory = entry.memory();
      String id = memory.id();
      int rowH = AiUiKit.inputHeight();
      int icon = rowH - 2;
      autismclient.gui.f.NetHelper head = autismclient.gui.f.NetHelper.init(row.read(), row.write(), row.update(), rowH);
      boolean hovered = kit.isHovered(row);
      boolean armed = id.equals(this.id);
      if (hovered || this.expandedIds.contains(id)) {
         PacketUtil.load(kit.graphics, row, 3, colors.cardSurfaceElevated);
      }

      kit.addInteractiveRegion(row, () -> {
         if (this.expandedIds.remove(id)) {
            this.wrapCache.remove(id);
         } else {
            this.expandedIds.add(id);
         }
      }, () -> copyToClipboard(memory.text()), null);
      autismclient.gui.ui.widgets.PillBadgeRenderer.init(
         kit.renderContext,
         autismclient.gui.f.NetHelper.init(row.read() + 2, row.write() + 1, chipW, rowH - 2),
         kindLabel(memory.kind()),
         kindColor(kit, memory.kind()),
         true,
         false
      );
      int x = row.read() + textX;
      if (id.equals(this.editingId)) {
         int right = row.init() - (icon + 2) * 2;
         kit.drawTextInput(this.editInput, autismclient.gui.f.NetHelper.init(x - 2, row.write(), Math.max(20, right - x), rowH));
         kit.drawIconButton(
            autismclient.gui.f.NetHelper.init(right + 2, row.write() + 1, icon, icon), autismclient.util.GuiIconAtlas.CHECK, false, false, "Save (Enter)", this::commitEdit
         );
         kit.drawIconButton(
            autismclient.gui.f.NetHelper.init(right + 4 + icon, row.write() + 1, icon, icon),
            autismclient.util.GuiIconAtlas.CLOSE,
            false,
            false,
            "Cancel (Esc)",
            () -> this.editingId = null
         );
      } else {
         int right = row.init() - 3;
         if (!hovered && !armed) {
            String age = AiUiKit.formatRelativeAge(memory.timestamp(), now);
            int ageW = kit.measureTextWidth(age);
            kit.drawCenteredRowText(age, head, right - ageW, ageW, colors.textMuted);
            right -= ageW + 4;
         } else {
            autismclient.gui.f.NetHelper trash = autismclient.gui.f.NetHelper.init(row.init() - icon - 1, row.write() + 1, icon, icon);
            autismclient.gui.f.NetHelper pen = autismclient.gui.f.NetHelper.init(trash.read() - 2 - icon, row.write() + 1, icon, icon);
            kit.drawIconButton(pen, autismclient.util.GuiIconAtlas.EDIT, false, false, "Edit", () -> this.beginEdit(memory));
            kit.drawIconButton(trash, autismclient.util.GuiIconAtlas.DELETE, false, armed, armed ? "Click again to forget" : "Forget", () -> this.armForget(id));
            right = pen.read() - 3;
         }

         if (!this.expandedIds.contains(id)) {
            kit.drawCenteredRowText(entry.text(), head, x, Math.max(1, right - x), colors.textPrimary);
         } else {
            List<String> lines = this.wrappedLines(kit, entry, textW);
            int ty = kit.renderContext.init_PoolEntry().centerTextY(head);

            for (int i = 0; i < lines.size(); i++) {
               kit.drawEllipsizedText(lines.get(i), x, ty, textW, colors.textPrimary);
               ty += AiUiKit.lineHeight();
            }

            kit.drawEllipsizedText(memory.scope(), x, ty, textW, colors.textMuted);
         }
      }
   }

   private static String kindLabel(AiMemoryStore.MemoryKind kind) {
      String var10000;
      switch (kind) {
         case USER:
            var10000 = "USER";
            break;
         case WORLD:
            var10000 = "WORLD";
            break;
         case PROCEDURE:
            var10000 = "PROC";
            break;
         case HYPOTHESIS:
            var10000 = "HYP";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static int kindColor(AiUiKit kit, AiMemoryStore.MemoryKind kind) {
      int var10000;
      switch (kind) {
         case USER:
            var10000 = -10497050;
            break;
         case WORLD:
            var10000 = kit.colors.success;
            break;
         case PROCEDURE:
            var10000 = kit.colors.accent;
            break;
         case HYPOTHESIS:
            var10000 = -14249;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private void renderFooter(AiUiKit kit, autismclient.gui.f.NetHelper footer) {
      int ctrl = footer.sync();
      int x = footer.read();
      String world = "world".equals(this.armedClearScope) ? "Click again to reset" : "Reset world memory";
      int worldW = Math.max(kit.measureButtonWidth("Click again to reset"), kit.measureButtonWidth("Reset world memory"));
      kit.drawButton(
         autismclient.gui.f.NetHelper.init(x, footer.write(), worldW, ctrl),
         world,
         autismclient.gui.f.b.CachePool.Inner_DataUtil.update,
         () -> this.armClear("world"),
         "Forget everything learned about this world"
      );
      x += worldW + 2;
      String all = "all".equals(this.armedClearScope) ? "Click again to clear" : "Clear all";
      kit.drawButton(
         autismclient.gui.f.NetHelper.init(x, footer.write(), kit.measureButtonWidth("Click again to clear"), ctrl),
         all,
         autismclient.gui.f.b.CachePool.Inner_DataUtil.update,
         () -> this.armClear("all"),
         "Forget every memory, including about you"
      );
   }

   private void armClear(String which) {
      if (!which.equals(this.armedClearScope)) {
         this.armedClearScope = which;
         this.armedClearAt = System.currentTimeMillis();
      } else {
         this.armedClearScope = null;
         this.service.clearWorldMemory(which.equals("world"));
         this.rows = List.of();
         this.lastSource = null;
         this.expandedIds.clear();
         autismclient.util.bi.load(which.equals("world") ? "World memory reset" : "Memory cleared");
      }
   }

   private void beginEdit(AiMemoryStore.Memory memory) {
      this.editingId = memory.id();
      this.id = null;
      this.editInput.setValue(memory.text());
      this.editInput.moveCursorToEnd();
      this.host.init(this.editInput);
   }

   private void commitEdit() {
      String id = this.editingId;
      String text = this.editInput.getValue().trim();
      this.editingId = null;
      this.editInput.setFocused(false);
      if (id != null && !text.isEmpty()) {
         this.service.correctMemory(id, text);
         this.lastSource = null;
         autismclient.util.bi.load("Memory updated");
      }
   }

   private void armForget(String id) {
      if (!id.equals(this.id)) {
         this.id = id;
         this.armedForgetAt = System.currentTimeMillis();
      } else {
         this.id = null;
         this.service.forgetMemory(id);
         List<AiMemoryView.MemoryRow> next = new ArrayList<>(this.rows);
         next.removeIf(r -> r.memory().id().equals(id));
         this.rows = next;
         this.expandedIds.remove(id);
         autismclient.util.bi.load("Memory forgotten");
      }
   }

   private static void copyToClipboard(String text) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.keyboardHandler != null) {
         mc.keyboardHandler.setClipboard(text);
         autismclient.util.bi.init("Memory copied");
      }
   }

   private record MemoryRow(AiMemoryStore.Memory memory, String text) {
   }

   private record WrappedText(String text, int width, int epoch, List<String> lines) {
   }
}
