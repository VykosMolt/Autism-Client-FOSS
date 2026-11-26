package autismclient.ai;

import autismclient.gui.f.PacketUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;

final class ap {
   private static final long init_J = 3000L;
   private static final long load_J = 1000L;
   private static final int init_I = 2;
   private final cc init_cc;
   private final cF init_cF;
   private final AiAssistantOverlay init_AiAssistantOverlay;
   final autismclient.gui.f.b.BitSetUtil init_BitSetUtil = new autismclient.gui.f.b.BitSetUtil().init_BitSetUtil_I(96).load_BitSetUtil_Str("Search memory");
   final autismclient.gui.f.b.BitSetUtil load_BitSetUtil = new autismclient.gui.f.b.BitSetUtil().init_BitSetUtil_I(4000);
   private final cE.Inner_StreamUtil init_cE_StreamUtil = new cE.Inner_StreamUtil(false);
   private final Set<String> init_Set = new HashSet<>();
   private final Map<String, ap.Inner_StreamUtil> init_Map = new HashMap<>();
   private List<ap.Inner_DataUtil> init_List = List.of();
   private Object init_Obj;
   private String init_Str;
   private long read_J;
   private String load_Str;
   private String read_Str;
   private String write_Str;
   private long write_J;
   private long update;

   ap(cc service, cF view, AiAssistantOverlay host) {
      this.init_cc = service;
      this.init_cF = view;
      this.init_AiAssistantOverlay = host;
      this.load_BitSetUtil.load_BitSetUtil_Consumer(text -> this.load());
   }

   void init_V() {
      this.init_BitSetUtil.init_BitSetUtil_Str("");
      this.load_Str = this.read_Str = this.write_Str = null;
      this.init_Set.clear();
      this.init_Map.clear();
      this.init_Obj = null;
      this.init_cE_StreamUtil.load();
   }

   boolean init_Z() {
      if (this.load_Str == null) {
         return false;
      }

      this.load_Str = null;
      this.load_BitSetUtil.init_V_Z(false);
      return true;
   }

   private void init_V_J(long now) {
      Object source = this.init_cc.init_AiMemoryStore().init_List();
      String query = this.init_BitSetUtil.init_Str().trim();
      if (source != this.init_Obj || !query.equals(this.init_Str) || now >= this.read_J) {
         this.init_Obj = source;
         this.init_Str = query;
         this.read_J = now + 1000L;
         List<ap.Inner_DataUtil> rows = new ArrayList<>();

         for (AiMemoryStore.Memory memory : this.init_cc.init_List_Str(query)) {
            rows.add(new ap.Inner_DataUtil(memory, cE.init_Str_Str(memory.text())));
         }

         this.init_List = rows;
      }
   }

   void init_V_cE(cE kit, autismclient.gui.f.NetHelper area, long now) {
      this.init_V_J(now);
      if (this.load_Str != null && !this.load_BitSetUtil.load_Z()) {
         this.load_Str = null;
      }

      if (this.read_Str != null && now - this.write_J > 3000L) {
         this.read_Str = null;
      }

      if (this.write_Str != null && now - this.update > 3000L) {
         this.write_Str = null;
      }

      autismclient.gui.f.JsonUtil colors = kit.init_JsonUtil;
      int ctrl = cE.init_I();
      String count = this.init_List.size() == 1 ? "1 memory" : this.init_List.size() + " memories";
      int countW = kit.init_I_Str(count);
      kit.init_V_BitSetUtil_NetHelper(
         this.init_BitSetUtil, autismclient.gui.f.NetHelper.init(area.read(), area.write(), Math.max(40, area.update() - countW - 8), ctrl)
      );
      kit.init_V_Str_3(
         count, autismclient.gui.f.NetHelper.init(area.init() - countW - 2, area.write(), countW + 2, ctrl), area.init() - countW - 2, countW + 2, colors.valid
      );
      autismclient.gui.f.NetHelper footer = autismclient.gui.f.NetHelper.init(area.read(), area.load() - ctrl, area.update(), ctrl);
      int top = area.write() + ctrl + 2;
      autismclient.gui.f.NetHelper viewport = autismclient.gui.f.NetHelper.init(area.read(), top, area.update(), Math.max(0, footer.write() - 2 - top));
      this.load(kit, viewport, now);
      if (!this.init_List.isEmpty() || !this.init_BitSetUtil.init_Str().isBlank()) {
         this.init_V_cE_NetHelper(kit, footer);
      }
   }

   private void load(cE kit, autismclient.gui.f.NetHelper v, long now) {
      autismclient.gui.f.JsonUtil colors = kit.init_JsonUtil;
      int rowH = cE.load_I();
      int line = cE.write();
      int rowW = v.update() - 6 - 2;
      int chipW = 0;

      for (String label : new String[]{"USER", "WORLD", "PROC", "HYP"}) {
         chipW = Math.max(chipW, kit.init_I_Str(label) + 8);
      }

      int textX = chipW + 6;
      int textW = Math.max(20, rowW - textX - 4 - (rowH - 2) * 2 - 2 - 4);
      int total = 0;

      for (ap.Inner_DataUtil row : this.init_List) {
         total += this.init(kit, row, textW, rowH, line) + 1;
      }

      this.init_cE_StreamUtil.init(v, total);
      kit.init_V_cE_StreamUtil(this.init_cE_StreamUtil);
      if (this.init_List.isEmpty()) {
         kit.init_V_Str_2(
            this.init_BitSetUtil.init_Str().isBlank() ? "Nothing remembered yet" : "No matches", v.read() + 4, v.write() + 3, v.update() - 8, colors.valid
         );
      } else {
         kit.init_V_NetHelper(v);
         int y = v.write() - this.init_cE_StreamUtil.init_I;

         for (ap.Inner_DataUtil entry : this.init_List) {
            int h = this.init(kit, entry, textW, rowH, line);
            if (y >= v.load()) {
               break;
            }

            if (y + h > v.write()) {
               this.init(kit, entry, autismclient.gui.f.NetHelper.init(v.read(), y, rowW, h), chipW, textX, textW, now);
            }

            y += h + 1;
         }

         kit.init_V();
         kit.load(this.init_cE_StreamUtil);
      }
   }

   private int init(cE kit, ap.Inner_DataUtil entry, int textW, int rowH, int line) {
      String id = entry.memory().id();
      if (this.init_Set.contains(id) && !id.equals(this.load_Str)) {
         int lines = this.init_List_cE(kit, entry, textW).size();
         return rowH + Math.max(0, lines - 1) * line + line + 2;
      } else {
         return rowH;
      }
   }

   private List<String> init_List_cE(cE kit, ap.Inner_DataUtil entry, int textW) {
      int epoch = TaskTimer.load_I();
      ap.Inner_StreamUtil wrap = this.init_Map.get(entry.memory().id());
      if (wrap == null || wrap.width() != textW || wrap.epoch() != epoch || !wrap.text().equals(entry.text())) {
         wrap = new ap.Inner_StreamUtil(entry.text(), textW, epoch, kit.init_SyncManager.init_PoolEntry().init_List(entry.text(), textW));
         this.init_Map.put(entry.memory().id(), wrap);
      }

      return wrap.lines();
   }

   private void init(cE kit, ap.Inner_DataUtil entry, autismclient.gui.f.NetHelper row, int chipW, int textX, int textW, long now) {
      autismclient.gui.f.JsonUtil colors = kit.init_JsonUtil;
      AiMemoryStore.Memory memory = entry.memory();
      String id = memory.id();
      int rowH = cE.load_I();
      int icon = rowH - 2;
      autismclient.gui.f.NetHelper head = autismclient.gui.f.NetHelper.init(row.read(), row.write(), row.update(), rowH);
      boolean hovered = kit.init_Z_NetHelper(row);
      boolean armed = id.equals(this.read_Str);
      if (hovered || this.init_Set.contains(id)) {
         PacketUtil.load(kit.init_GuiGraphicsExtract, row, 3, colors.push);
      }

      kit.init_V_NetHelper(row, () -> {
         if (this.init_Set.remove(id)) {
            this.init_Map.remove(id);
         } else {
            this.init_Set.add(id);
         }
      }, () -> read(memory.text()), null);
      autismclient.gui.f.b.ConfigStore.init(
         kit.init_SyncManager,
         autismclient.gui.f.NetHelper.init(row.read() + 2, row.write() + 1, chipW, rowH - 2),
         init_Str_AiMemoryStore_Data(memory.kind()),
         init_I_cE_AiMemoryStore_Data(kit, memory.kind()),
         true,
         false
      );
      int x = row.read() + textX;
      if (id.equals(this.load_Str)) {
         int right = row.init() - (icon + 2) * 2;
         kit.init_V_BitSetUtil_NetHelper(this.load_BitSetUtil, autismclient.gui.f.NetHelper.init(x - 2, row.write(), Math.max(20, right - x), rowH));
         kit.init(
            autismclient.gui.f.NetHelper.init(right + 2, row.write() + 1, icon, icon), autismclient.util.GuiIconAtlas.read, false, false, "Save (Enter)", this::load
         );
         kit.init(
            autismclient.gui.f.NetHelper.init(right + 4 + icon, row.write() + 1, icon, icon),
            autismclient.util.GuiIconAtlas.init_Identifier,
            false,
            false,
            "Cancel (Esc)",
            () -> this.load_Str = null
         );
      } else {
         int right = row.init() - 3;
         if (!hovered && !armed) {
            String age = cE.init_Str_J_J(memory.timestamp(), now);
            int ageW = kit.init_I_Str(age);
            kit.init_V_Str_3(age, head, right - ageW, ageW, colors.size);
            right -= ageW + 4;
         } else {
            autismclient.gui.f.NetHelper trash = autismclient.gui.f.NetHelper.init(row.init() - icon - 1, row.write() + 1, icon, icon);
            autismclient.gui.f.NetHelper pen = autismclient.gui.f.NetHelper.init(trash.read() - 2 - icon, row.write() + 1, icon, icon);
            kit.init(pen, autismclient.util.GuiIconAtlas.poll, false, false, "Edit", () -> this.init_V_AiMemoryStore_Memo(memory));
            kit.init(trash, autismclient.util.GuiIconAtlas.open, false, armed, armed ? "Click again to forget" : "Forget", () -> this.load(id));
            right = pen.read() - 3;
         }

         if (!this.init_Set.contains(id)) {
            kit.init_V_Str_3(entry.text(), head, x, Math.max(1, right - x), colors.prev);
         } else {
            List<String> lines = this.init_List_cE(kit, entry, textW);
            int ty = kit.init_SyncManager.init_PoolEntry().init_I(head);

            for (int i = 0; i < lines.size(); i++) {
               kit.init_V_Str_2(lines.get(i), x, ty, textW, colors.prev);
               ty += cE.write();
            }

            kit.init_V_Str_2(memory.scope(), x, ty, textW, colors.size);
         }
      }
   }

   private static String init_Str_AiMemoryStore_Data(AiMemoryStore.Inner_DataUtil kind) {
      String var10000;
      switch (kind) {
         case init:
            var10000 = "USER";
            break;
         case load:
            var10000 = "WORLD";
            break;
         case read:
            var10000 = "PROC";
            break;
         case write:
            var10000 = "HYP";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static int init_I_cE_AiMemoryStore_Data(cE kit, AiMemoryStore.Inner_DataUtil kind) {
      int var10000;
      switch (kind) {
         case init:
            var10000 = -10497050;
            break;
         case load:
            var10000 = kit.init_JsonUtil.stop;
            break;
         case read:
            var10000 = kit.init_JsonUtil.clear;
            break;
         case write:
            var10000 = -14249;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private void init_V_cE_NetHelper(cE kit, autismclient.gui.f.NetHelper footer) {
      int ctrl = footer.sync();
      int x = footer.read();
      String world = "world".equals(this.write_Str) ? "Click again to reset" : "Reset world memory";
      int worldW = Math.max(kit.load("Click again to reset"), kit.load("Reset world memory"));
      kit.init_V_NetHelper(
         autismclient.gui.f.NetHelper.init(x, footer.write(), worldW, ctrl),
         world,
         autismclient.gui.f.b.CachePool.Inner_DataUtil.update,
         () -> this.init_V_Str("world"),
         "Forget everything learned about this world"
      );
      x += worldW + 2;
      String all = "all".equals(this.write_Str) ? "Click again to clear" : "Clear all";
      kit.init_V_NetHelper(
         autismclient.gui.f.NetHelper.init(x, footer.write(), kit.load("Click again to clear"), ctrl),
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
         this.init_cc.init_V_Z(which.equals("world"));
         this.init_List = List.of();
         this.init_Obj = null;
         this.init_Set.clear();
         autismclient.util.bi.load(which.equals("world") ? "World memory reset" : "Memory cleared");
      }
   }

   private void init_V_AiMemoryStore_Memo(AiMemoryStore.Memory memory) {
      this.load_Str = memory.id();
      this.read_Str = null;
      this.load_BitSetUtil.init_BitSetUtil_Str(memory.text());
      this.load_BitSetUtil.init_BitSetUtil();
      this.init_AiAssistantOverlay.init(this.load_BitSetUtil);
   }

   private void load() {
      String id = this.load_Str;
      String text = this.load_BitSetUtil.init_Str().trim();
      this.load_Str = null;
      this.load_BitSetUtil.init_V_Z(false);
      if (id != null && !text.isEmpty()) {
         this.init_cc.write(id, text);
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
         this.init_cc.tick(id);
         List<ap.Inner_DataUtil> next = new ArrayList<>(this.init_List);
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
