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
   private final AiService init_cc;
   private final AiViewState init_cF;
   private final AiAssistantOverlay init_AiAssistantOverlay;
   final autismclient.gui.ui.widgets.TextInputWidget searchInput = new autismclient.gui.ui.widgets.TextInputWidget().setMaxLength(96).setPlaceholder("Search memory");
   final autismclient.gui.ui.widgets.TextInputWidget editInput = new autismclient.gui.ui.widgets.TextInputWidget().setMaxLength(4000);
   private final AiUiKit.Inner_StreamUtil init_cE_StreamUtil = new AiUiKit.Inner_StreamUtil(false);
   private final Set<String> init_Set = new HashSet<>();
   private final Map<String, AiMemoryView.Inner_StreamUtil> init_Map = new HashMap<>();
   private List<AiMemoryView.Inner_DataUtil> init_List = List.of();
   private Object init_Obj;
   private String init_Str;
   private long read_J;
   private String load_Str;
   private String read_Str;
   private String write_Str;
   private long write_J;
   private long update;

   AiMemoryView(AiService service, AiViewState view, AiAssistantOverlay host) {
      this.init_cc = service;
      this.init_cF = view;
      this.init_AiAssistantOverlay = host;
      this.editInput.setOnSubmit(text -> this.load());
   }

   void reset() {
      this.searchInput.setValue("");
      this.load_Str = this.read_Str = this.write_Str = null;
      this.init_Set.clear();
      this.init_Map.clear();
      this.init_Obj = null;
      this.init_cE_StreamUtil.scrollToTop();
   }

   boolean cancelEdit() {
      if (this.load_Str == null) {
         return false;
      }

      this.load_Str = null;
      this.editInput.setFocused(false);
      return true;
   }

   private void init_V_J(long now) {
      Object source = this.init_cc.getMemoryStore().memories();
      String query = this.searchInput.getValue().trim();
      if (source != this.init_Obj || !query.equals(this.init_Str) || now >= this.read_J) {
         this.init_Obj = source;
         this.init_Str = query;
         this.read_J = now + 1000L;
         List<AiMemoryView.Inner_DataUtil> rows = new ArrayList<>();

         for (AiMemoryStore.Memory memory : this.init_cc.searchMemories(query)) {
            rows.add(new AiMemoryView.Inner_DataUtil(memory, AiUiKit.flattenSingleLine(memory.text())));
         }

         this.init_List = rows;
      }
   }

   void render(AiUiKit kit, autismclient.gui.f.NetHelper area, long now) {
      this.init_V_J(now);
      if (this.load_Str != null && !this.editInput.isFocused()) {
         this.load_Str = null;
      }

      if (this.read_Str != null && now - this.write_J > 3000L) {
         this.read_Str = null;
      }

      if (this.write_Str != null && now - this.update > 3000L) {
         this.write_Str = null;
      }

      autismclient.gui.ui.UiColorScheme colors = kit.colors;
      int ctrl = AiUiKit.toolbarHeight();
      String count = this.init_List.size() == 1 ? "1 memory" : this.init_List.size() + " memories";
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
      this.load(kit, viewport, now);
      if (!this.init_List.isEmpty() || !this.searchInput.getValue().isBlank()) {
         this.init_V_cE_NetHelper(kit, footer);
      }
   }

   private void load(AiUiKit kit, autismclient.gui.f.NetHelper v, long now) {
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

      for (AiMemoryView.Inner_DataUtil row : this.init_List) {
         total += this.init(kit, row, textW, rowH, line) + 1;
      }

      this.init_cE_StreamUtil.layout(v, total);
      kit.registerScrollPane(this.init_cE_StreamUtil);
      if (this.init_List.isEmpty()) {
         kit.drawEllipsizedText(
            this.searchInput.getValue().isBlank() ? "Nothing remembered yet" : "No matches", v.read() + 4, v.write() + 3, v.update() - 8, colors.textSecondary
         );
      } else {
         kit.pushClip(v);
         int y = v.write() - this.init_cE_StreamUtil.offset;

         for (AiMemoryView.Inner_DataUtil entry : this.init_List) {
            int h = this.init(kit, entry, textW, rowH, line);
            if (y >= v.load()) {
               break;
            }

            if (y + h > v.write()) {
               this.init(kit, entry, autismclient.gui.f.NetHelper.init(v.read(), y, rowW, h), chipW, textX, textW, now);
            }

            y += h + 1;
         }

         kit.popClip();
         kit.drawScrollbar(this.init_cE_StreamUtil);
      }
   }

   private int init(AiUiKit kit, AiMemoryView.Inner_DataUtil entry, int textW, int rowH, int line) {
      String id = entry.memory().id();
      if (this.init_Set.contains(id) && !id.equals(this.load_Str)) {
         int lines = this.init_List_cE(kit, entry, textW).size();
         return rowH + Math.max(0, lines - 1) * line + line + 2;
      } else {
         return rowH;
      }
   }

   private List<String> init_List_cE(AiUiKit kit, AiMemoryView.Inner_DataUtil entry, int textW) {
      int epoch = AiChatView.textEpoch();
      AiMemoryView.Inner_StreamUtil wrap = this.init_Map.get(entry.memory().id());
      if (wrap == null || wrap.width() != textW || wrap.epoch() != epoch || !wrap.text().equals(entry.text())) {
         wrap = new AiMemoryView.Inner_StreamUtil(entry.text(), textW, epoch, kit.renderContext.init_PoolEntry().wrapLines(entry.text(), textW));
         this.init_Map.put(entry.memory().id(), wrap);
      }

      return wrap.lines();
   }

   private void init(AiUiKit kit, AiMemoryView.Inner_DataUtil entry, autismclient.gui.f.NetHelper row, int chipW, int textX, int textW, long now) {
      autismclient.gui.ui.UiColorScheme colors = kit.colors;
      AiMemoryStore.Memory memory = entry.memory();
      String id = memory.id();
      int rowH = AiUiKit.inputHeight();
      int icon = rowH - 2;
      autismclient.gui.f.NetHelper head = autismclient.gui.f.NetHelper.init(row.read(), row.write(), row.update(), rowH);
      boolean hovered = kit.isHovered(row);
      boolean armed = id.equals(this.read_Str);
      if (hovered || this.init_Set.contains(id)) {
         PacketUtil.load(kit.graphics, row, 3, colors.cardSurfaceElevated);
      }

      kit.addInteractiveRegion(row, () -> {
         if (this.init_Set.remove(id)) {
            this.init_Map.remove(id);
         } else {
            this.init_Set.add(id);
         }
      }, () -> read(memory.text()), null);
      autismclient.gui.ui.widgets.PillBadgeRenderer.init(
         kit.renderContext,
         autismclient.gui.f.NetHelper.init(row.read() + 2, row.write() + 1, chipW, rowH - 2),
         init_Str_AiMemoryStore_Data(memory.kind()),
         init_I_cE_AiMemoryStore_Data(kit, memory.kind()),
         true,
         false
      );
      int x = row.read() + textX;
      if (id.equals(this.load_Str)) {
         int right = row.init() - (icon + 2) * 2;
         kit.drawTextInput(this.editInput, autismclient.gui.f.NetHelper.init(x - 2, row.write(), Math.max(20, right - x), rowH));
         kit.drawIconButton(
            autismclient.gui.f.NetHelper.init(right + 2, row.write() + 1, icon, icon), autismclient.util.GuiIconAtlas.CHECK, false, false, "Save (Enter)", this::load
         );
         kit.drawIconButton(
            autismclient.gui.f.NetHelper.init(right + 4 + icon, row.write() + 1, icon, icon),
            autismclient.util.GuiIconAtlas.CLOSE,
            false,
            false,
            "Cancel (Esc)",
            () -> this.load_Str = null
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
            kit.drawIconButton(pen, autismclient.util.GuiIconAtlas.EDIT, false, false, "Edit", () -> this.init_V_AiMemoryStore_Memo(memory));
            kit.drawIconButton(trash, autismclient.util.GuiIconAtlas.DELETE, false, armed, armed ? "Click again to forget" : "Forget", () -> this.load(id));
            right = pen.read() - 3;
         }

         if (!this.init_Set.contains(id)) {
            kit.drawCenteredRowText(entry.text(), head, x, Math.max(1, right - x), colors.textPrimary);
         } else {
            List<String> lines = this.init_List_cE(kit, entry, textW);
            int ty = kit.renderContext.init_PoolEntry().centerTextY(head);

            for (int i = 0; i < lines.size(); i++) {
               kit.drawEllipsizedText(lines.get(i), x, ty, textW, colors.textPrimary);
               ty += AiUiKit.lineHeight();
            }

            kit.drawEllipsizedText(memory.scope(), x, ty, textW, colors.textMuted);
         }
      }
   }

   private static String init_Str_AiMemoryStore_Data(AiMemoryStore.Inner_DataUtil kind) {
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

   private static int init_I_cE_AiMemoryStore_Data(AiUiKit kit, AiMemoryStore.Inner_DataUtil kind) {
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

   private void init_V_cE_NetHelper(AiUiKit kit, autismclient.gui.f.NetHelper footer) {
      int ctrl = footer.sync();
      int x = footer.read();
      String world = "world".equals(this.write_Str) ? "Click again to reset" : "Reset world memory";
      int worldW = Math.max(kit.measureButtonWidth("Click again to reset"), kit.measureButtonWidth("Reset world memory"));
      kit.drawButton(
         autismclient.gui.f.NetHelper.init(x, footer.write(), worldW, ctrl),
         world,
         autismclient.gui.f.b.CachePool.Inner_DataUtil.update,
         () -> this.init_V_Str("world"),
         "Forget everything learned about this world"
      );
      x += worldW + 2;
      String all = "all".equals(this.write_Str) ? "Click again to clear" : "Clear all";
      kit.drawButton(
         autismclient.gui.f.NetHelper.init(x, footer.write(), kit.measureButtonWidth("Click again to clear"), ctrl),
         all,
         autismclient.gui.f.b.CachePool.Inner_DataUtil.update,
         () -> this.init_V_Str("all"),
         "Forget every memory, including about you"
      );
   }

   private void init_V_Str(String which) {
      if (!which.equals(this.write_Str)) {
         this.write_Str = which;
         this.update = System.currentTimeMillis();
      } else {
         this.write_Str = null;
         this.init_cc.clearWorldMemory(which.equals("world"));
         this.init_List = List.of();
         this.init_Obj = null;
         this.init_Set.clear();
         autismclient.util.bi.load(which.equals("world") ? "World memory reset" : "Memory cleared");
      }
   }

   private void init_V_AiMemoryStore_Memo(AiMemoryStore.Memory memory) {
      this.load_Str = memory.id();
      this.read_Str = null;
      this.editInput.setValue(memory.text());
      this.editInput.moveCursorToEnd();
      this.init_AiAssistantOverlay.init(this.editInput);
   }

   private void load() {
      String id = this.load_Str;
      String text = this.editInput.getValue().trim();
      this.load_Str = null;
      this.editInput.setFocused(false);
      if (id != null && !text.isEmpty()) {
         this.init_cc.correctMemory(id, text);
         this.init_Obj = null;
         autismclient.util.bi.load("Memory updated");
      }
   }

   private void load(String id) {
      if (!id.equals(this.read_Str)) {
         this.read_Str = id;
         this.write_J = System.currentTimeMillis();
      } else {
         this.read_Str = null;
         this.init_cc.forgetMemory(id);
         List<AiMemoryView.Inner_DataUtil> next = new ArrayList<>(this.init_List);
         next.removeIf(r -> r.memory().id().equals(id));
         this.init_List = next;
         this.init_Set.remove(id);
         autismclient.util.bi.load("Memory forgotten");
      }
   }

   private static void read(String text) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.keyboardHandler != null) {
         mc.keyboardHandler.setClipboard(text);
         autismclient.util.bi.init("Memory copied");
      }
   }

   private record Inner_DataUtil(AiMemoryStore.Memory memory, String text) {
   }

   private record Inner_StreamUtil(String text, int width, int epoch, List<String> lines) {
   }
}
