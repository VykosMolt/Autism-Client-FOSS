package autismclient.util;

import autismclient.gui.f.b.j;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class AutismCustomFilterPresetOverlay extends bj {
   private final Font init_Font;
   private final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private final autismclient.gui.f.b.BitSetUtil init_BitSetUtil;
   private final List<AutismCustomFilterPresetOverlay.Inner_DataUtil> init_List = new ArrayList<>();
   private final AutismCustomFilterPresetOverlay.Inner_StreamUtil init_AutismCustomFilter = new AutismCustomFilterPresetOverlay.Inner_StreamUtil();
   private List<bT.Inner_DataUtil> load_List = List.of();
   private List<bT.Inner_DataUtil> read_List = List.of();
   private List<AutismCustomFilterPresetOverlay.Inner_CachePool> write_List = List.of();
   private int flush_I = 570;
   private int close_I = 48;
   private int open;
   private int poll;
   private boolean write_Z = false;
   private boolean update_Z = false;
   private boolean sync_Z = false;
   private double init_D = 0.0;
   private double load_D = 0.0;
   private boolean flush_Z = false;
   private int push = 0;
   private String init_Str_2;

   public AutismCustomFilterPresetOverlay(Font textRenderer) {
      this.init_Font = textRenderer;
      this.open = this.pop_I_6();
      this.poll = this.tick_I_4();
      this.init_BitSetUtil = new autismclient.gui.f.b.BitSetUtil()
         .load_BitSetUtil_Str("Preset name")
         .read_BitSetUtil_I(this.start_I_4())
         .init_BitSetUtil_F(120.0F)
         .read_BitSetUtil_F(120.0F)
         .init_BitSetUtil_l(autismclient.gui.f.b.l.read)
         .load_BitSetUtil_l(autismclient.gui.f.b.l.write);
   }

   @Override
   public String init_Str() {
      return "autism-custom-filter-presets";
   }

   @Override
   public int init_I() {
      return this.pop_I_6();
   }

   @Override
   public int load_I() {
      return this.run_I_4();
   }

   @Override
   public cH init_cH() {
      return new cH(this.flush_I, this.close_I, this.open, this.poll, this.write_Z, this.update_Z);
   }

   @Override
   public void init_V_cH(cH bounds) {
      if (bounds != null) {
         cH clamped = this.init_cH_cX_cH(this, bounds);
         this.flush_I = clamped.init_I;
         this.close_I = clamped.load_I;
         this.open = clamped.read;
         this.poll = clamped.write;
         this.write_Z = clamped.init_Z;
         this.update_Z = clamped.load_Z;
      }
   }

   @Override
   public void init_V_Z(boolean visible) {
      this.write_Z = visible;
      if (visible) {
         bk.init_bk().update_V(this);
      } else {
         this.open_V_4();
      }

      this.a__V();
   }

   public void load_V_6() {
      this.init_V_Z(!this.write_Z);
   }

   @Override
   public boolean sync_Z() {
      return this.write_Z;
   }

   @Override
   public boolean flush_Z() {
      return this.update_Z;
   }

   @Override
   public void write_V_Z(boolean collapsed) {
      if (this.update_Z != collapsed) {
         this.update_Z = collapsed;
         if (collapsed) {
            this.get_V();
         }

         this.a__V();
      }
   }

   @Override
   public boolean load_Z() {
      return true;
   }

   @Override
   public boolean load_Z_D_D(double mouseX, double mouseY) {
      if (!this.write_Z) {
         return false;
      }

      cH bounds = this.init_cH();
      return mouseX >= this.flush_I
         && mouseX <= this.flush_I + this.open
         && mouseY >= this.close_I
         && mouseY <= this.close_I + 14
         && !this.read_Z_D_2(mouseX, mouseY, bounds);
   }

   @Override
   public boolean write_Z() {
      return this.init_BitSetUtil.load_Z();
   }

   @Override
   public void push_V() {
      this.open_V_4();
   }

   @Override
   public void init_V_GuiGraphicsExtract_15(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      if (this.write_Z || this.a__Z()) {
         this.init_List.clear();
         this.poll = this.push_I_6() + this.load(this.init(this.init_List_3().size()));
         cH clamped = this.init_cH_cX_cH(this, new cH(this.flush_I, this.close_I, this.open, this.poll, this.write_Z, this.update_Z));
         this.flush_I = clamped.init_I;
         this.close_I = clamped.load_I;
         this.open = clamped.read;
         this.poll = clamped.write;
         cH bounds = new cH(this.flush_I, this.close_I, this.open, this.poll, this.write_Z, this.update_Z);
         this.init_V_GuiGraphicsExtract_6(context, mouseX, mouseY, bounds, "Preset Manager", this.update_Z, this.sync_Z);
         boolean clipBody = this.init_Z_GuiGraphicsExtract(context, bounds, this.update_Z);
         if (!clipBody) {
            this.init_V_GuiGraphicsExtract_14(context, bounds, this.update_Z, this.sync_Z);
         } else {
            try {
               int x = this.flush_I + this.get_I_4();
               int y = this.close_I + 14 + this.set_I_4();
               int width = this.open - this.get_I_4() * 2;
               autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.init_PoolEntry();
               autismclient.gui.f.b.FileCache.init(context, this.init_Font, "Preset Name", x, y);
               String counts = "C2S " + module.load_Set().size() + " | S2C " + module.read_Set().size();
               cs.init(context, this.init_Font, counts, cs.Inner_DataUtil.MUTED, x + width - cs.init(this.init_Font, counts, cs.Inner_DataUtil.MUTED), y, false);
               y += this.put();
               this.init_BitSetUtil.init_TableIndex_F(x, y, width, this.start_I_4());
               this.init_BitSetUtil.init_V_SyncManager(this.init_SyncManager_GuiGraphicsExtract_2(context, mouseX, mouseY, delta));
               y += this.start_I_4() + this.clear();
               y = this.init(
                  context,
                  mouseX,
                  mouseY,
                  x,
                  y,
                  width,
                  new String[]{"Save", "Load", "Overwrite", "Delete"},
                  new Runnable[]{this::write_V_7, this::update_V_5, this::sync_V_4, this::flush_V_4},
                  new boolean[]{this.poll_Z_4(), this.push_Z_3(), this.pop_Z_2(), this.tick_Z_2()}
               );
               String resetLabel = "Reset To Default";
               int resetW = Math.min(width, autismclient.gui.f.RenderState.load(cs.init(this.init_Font, resetLabel, cs.Inner_DataUtil.BODY)));
               y = this.init(context, mouseX, mouseY, x, y, resetW, resetLabel, this::close_V_4, true);
               y += this.reset() - this.clear();
               int listHeight = Math.max(this.load(1), this.poll - (y - this.close_I) - this.put() - this.get_I_4());
               this.init_I_GuiGraphicsExtract_6(context, mouseX, mouseY, delta, x, y, width, listHeight);
            } finally {
               this.init_V_GuiGraphicsExtract_Z(context, clipBody);
               this.init_V_GuiGraphicsExtract_14(context, bounds, this.update_Z, this.sync_Z);
            }
         }
      }
   }

   private int init_I_GuiGraphicsExtract_6(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, int x, int y, int width, int listHeight) {
      String headerLabel = "Saved Presets";
      autismclient.gui.f.b.FileCache.init(context, this.init_Font, headerLabel, x, y);
      int headerY = y;
      y += this.put();
      List<AutismCustomFilterPresetOverlay.Inner_CachePool> rows = this.init_List_3();
      this.init_AutismCustomFilter.init_List = rows;
      autismclient.gui.f.b.TextFormat.Inner_DataUtil baseLayout = this.init(x, y, width, listHeight, rows.size(), 0);
      int visualScroll = this.init_AutismCustomFilter.init_CacheEntry.init_I_F_I(delta, baseLayout.open());
      autismclient.gui.f.b.TextFormat.Inner_DataUtil listLayout = this.init(x, y, width, listHeight, rows.size(), visualScroll);
      int contentWidth = Math.max(40, listLayout.push());
      this.init_AutismCustomFilter.init(x, y, width, listHeight, listLayout.init_PoolEntry_DataUtil());
      boolean focused = bk.init_bk().update_Z(this) || bk.init_bk().write_Z(this);
      listLayout.init(context, focused);
      if (rows.isEmpty()) {
         autismclient.gui.f.b.FileCache.init(context, this.init_Font, "No presets", x, y, contentWidth);
      } else {
         listLayout.init(context);

         try {
            listLayout.init(
               rows.size(),
               (index, rowY) -> {
                  AutismCustomFilterPresetOverlay.Inner_CachePool row = rows.get(index);
                  int rowTextY = j.init(rowY, this.stop(), this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.read), this.init_CharEncoder.flush());
                  if (row.init_Z) {
                     autismclient.gui.f.b.FileCache.init(
                        context,
                        this.init_Font,
                        row.init_Str,
                        x + 2,
                        rowY,
                        contentWidth,
                        this.stop(),
                        false,
                        false,
                        autismclient.gui.f.b.FileCache.Inner_DataUtil.WARNING
                     );
                  } else if (row.load) {
                     autismclient.gui.f.b.FileCache.init(
                        context,
                        this.init_Font,
                        row.init_Str,
                        x + 2,
                        rowY,
                        contentWidth,
                        this.stop(),
                        false,
                        false,
                        autismclient.gui.f.b.FileCache.Inner_DataUtil.NORMAL
                     );
                  } else if (row.init_bT_DataUtil != null) {
                     boolean selected = row.init_bT_DataUtil.init_Str().equals(this.init_Str_2);
                     boolean hovered = mouseX >= x + 2 && mouseX < x + 2 + contentWidth && mouseY >= rowY && mouseY < rowY + this.stop();
                     autismclient.gui.f.b.FileCache.init(
                        context,
                        this.init_Font,
                        "",
                        x + 2,
                        rowY,
                        contentWidth,
                        this.stop(),
                        hovered,
                        selected,
                        row.init_bT_DataUtil.load_Z() ? autismclient.gui.f.b.FileCache.Inner_DataUtil.WARNING : autismclient.gui.f.b.FileCache.Inner_DataUtil.NORMAL
                     );
                     int textColor = selected ? p.table() : (row.init_bT_DataUtil.load_Z() ? -4729601 : p.b());
                     String prefix = row.init_bT_DataUtil.load_Z() ? "[DEV] " : "[USR] ";
                     String name = cs.init(this.init_Font, prefix + row.init_bT_DataUtil.init_Str(), contentWidth - 70, cs.Inner_DataUtil.BODY);
                     cs.init(context, this.init_Font, name, textColor, x + 5, rowTextY, false);
                     String counts = row.init_bT_DataUtil.init_I() + " / " + row.init_bT_DataUtil.load_I();
                     int countsWidth = cs.init(this.init_Font, counts, cs.Inner_DataUtil.BODY);
                     cs.init(context, this.init_Font, counts, textColor, x + contentWidth - countsWidth - 1, rowTextY, false);
                  }

                  autismclient.gui.f.b.FileCache.init(context, x + 2, rowY + this.stop(), contentWidth);
               }
            );
         } finally {
            listLayout.load(context);
         }
      }

      listLayout.init(context, mouseX, mouseY, this.flush_Z);
      String footer = listLayout.open() > 0
         ? "Scroll: "
            + Math.min(rows.size(), Math.round((float)this.init_AutismCustomFilter.init_CacheEntry.init() / this.next()) + 1)
            + "/"
            + Math.max(1, Math.round((float)listLayout.open() / this.next()) + 1)
         : (this.init_Str_2 == null ? "Select a preset row" : "Selected: " + this.init_Str_2);
      int footerMaxW = Math.max(1, width - cs.init(this.init_Font, headerLabel, cs.Inner_DataUtil.BODY) - 8);
      footer = cs.init(this.init_Font, footer, footerMaxW, cs.Inner_DataUtil.MUTED);
      int footerW = cs.init(this.init_Font, footer, cs.Inner_DataUtil.MUTED);
      cs.init(context, this.init_Font, footer, cs.Inner_DataUtil.MUTED, x + width - footerW, headerY, false);
      return y + listHeight;
   }

   private int init(int count) {
      return Math.max(3, Math.min(12, count));
   }

   private int load(int rows) {
      return rows * this.next() + 4;
   }

   private int push_I_6() {
      return 14
         + this.set_I_4()
         + this.put()
         + this.start_I_4()
         + this.clear()
         + (this.add_I_4() + this.clear()) * 2
         + this.reset()
         - this.clear()
         + this.put()
         + this.get_I_4();
   }

   private List<AutismCustomFilterPresetOverlay.Inner_CachePool> init_List_3() {
      bT manager = bT.init_bT();
      List<bT.Inner_DataUtil> builtInEntries = manager.init_List();
      List<bT.Inner_DataUtil> userEntries = manager.load_List();
      if (builtInEntries == this.load_List && userEntries == this.read_List) {
         return this.write_List;
      }

      List<AutismCustomFilterPresetOverlay.Inner_CachePool> rows = new ArrayList<>();
      rows.add(AutismCustomFilterPresetOverlay.Inner_CachePool.init("Developer Templates"));

      for (bT.Inner_DataUtil entry : builtInEntries) {
         rows.add(AutismCustomFilterPresetOverlay.Inner_CachePool.init(entry));
      }

      rows.add(AutismCustomFilterPresetOverlay.Inner_CachePool.init("User Presets"));
      if (userEntries.isEmpty()) {
         rows.add(AutismCustomFilterPresetOverlay.Inner_CachePool.load("No user presets yet"));
      } else {
         for (bT.Inner_DataUtil entry : userEntries) {
            rows.add(AutismCustomFilterPresetOverlay.Inner_CachePool.init(entry));
         }
      }

      this.load_List = builtInEntries;
      this.read_List = userEntries;
      this.write_List = List.copyOf(rows);
      return this.write_List;
   }

   private void write_V_7() {
      String name = this.load_Str_4();
      if (name == null) {
         AutismClientMessaging.sendPrefixed("Enter a preset name first.");
      } else if (bT.init_bT().init_Z(name)) {
         this.init_Str_2 = name;
         this.init_BitSetUtil.init_BitSetUtil_Str(name);
      }
   }

   private void update_V_5() {
      String presetName = this.read_Str_3();
      if (presetName == null) {
         AutismClientMessaging.sendPrefixed("Select a preset first.");
      } else if (bT.init_bT().read_Z(presetName)) {
         this.init_Str_2 = bT.init_bT().init_bT_DataUtil(presetName) != null ? bT.init_bT().init_bT_DataUtil(presetName).init_Str() : presetName;
         this.init_BitSetUtil.init_BitSetUtil_Str(this.init_Str_2);
      }
   }

   private void sync_V_4() {
      String presetName = this.read_Str_3();
      if (presetName == null) {
         AutismClientMessaging.sendPrefixed("Select a user preset first.");
      } else {
         bT.Inner_DataUtil entry = bT.init_bT().init_bT_DataUtil(presetName);
         if (entry != null && !entry.load_Z()) {
            if (bT.init_bT().load_Z(entry.init_Str())) {
               this.init_Str_2 = entry.init_Str();
               this.init_BitSetUtil.init_BitSetUtil_Str(entry.init_Str());
            }
         } else {
            AutismClientMessaging.sendPrefixed("Select a user preset to overwrite.");
         }
      }
   }

   private void flush_V_4() {
      String presetName = this.read_Str_3();
      if (presetName == null) {
         AutismClientMessaging.sendPrefixed("Select a user preset first.");
      } else {
         bT.Inner_DataUtil entry = bT.init_bT().init_bT_DataUtil(presetName);
         if (entry != null && !entry.load_Z()) {
            if (bT.init_bT().write_Z(entry.init_Str())) {
               if (entry.init_Str().equals(this.init_Str_2)) {
                  this.init_Str_2 = null;
               }

               this.init_BitSetUtil.init_BitSetUtil_Str("");
            }
         } else {
            AutismClientMessaging.sendPrefixed("Developer presets cannot be deleted.");
         }
      }
   }

   private void close_V_4() {
      autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.init_PoolEntry();
      module.open_V();
      module.poll_V();
      AutismClientMessaging.sendPrefixed("Reset current filter to default packet lists.");
   }

   private String load_Str_4() {
      String value = this.init_BitSetUtil.init_Str();
      if (value == null) {
         return null;
      }

      value = value.trim();
      return value.isEmpty() ? null : value;
   }

   private String read_Str_3() {
      if (this.init_Str_2 != null) {
         bT.Inner_DataUtil selectedEntry = bT.init_bT().init_bT_DataUtil(this.init_Str_2);
         if (selectedEntry != null) {
            return selectedEntry.init_Str();
         }
      }

      String typedName = this.load_Str_4();
      if (typedName == null) {
         return null;
      }

      bT.Inner_DataUtil typedEntry = bT.init_bT().init_bT_DataUtil(typedName);
      return typedEntry != null ? typedEntry.init_Str() : null;
   }

   private bT.Inner_DataUtil init_bT_DataUtil() {
      return this.init_Str_2 == null ? null : bT.init_bT().init_bT_DataUtil(this.init_Str_2);
   }

   private boolean poll_Z_4() {
      String name = this.load_Str_4();
      return name != null && !bT.init_bT().update(name);
   }

   private boolean push_Z_3() {
      return this.read_Str_3() != null;
   }

   private boolean pop_Z_2() {
      bT.Inner_DataUtil entry = this.init_bT_DataUtil();
      return entry != null && !entry.load_Z();
   }

   private boolean tick_Z_2() {
      return this.pop_Z_2();
   }

   private int init(GuiGraphicsExtractor context, int mouseX, int mouseY, int x, int y, int width, String label, Runnable action, boolean enabled) {
      this.init_V_GuiGraphicsExtract_8(context, mouseX, mouseY, x, y, width, label, enabled);
      this.init_List.add(new AutismCustomFilterPresetOverlay.Inner_DataUtil(x, y, width, this.add_I_4(), action, enabled));
      return y + this.add_I_4() + this.clear();
   }

   private int init(GuiGraphicsExtractor context, int mouseX, int mouseY, int x, int y, int width, String[] labels, Runnable[] actions, boolean[] enabled) {
      int n = labels.length;
      int cellW = (width - this.clear() * (n - 1)) / n;
      int cx = x;

      for (int i = 0; i < n; i++) {
         int w = i == n - 1 ? x + width - cx : cellW;
         this.init_V_GuiGraphicsExtract_8(context, mouseX, mouseY, cx, y, w, labels[i], enabled[i]);
         this.init_List.add(new AutismCustomFilterPresetOverlay.Inner_DataUtil(cx, y, w, this.add_I_4(), actions[i], enabled[i]));
         cx += w + this.clear();
      }

      return y + this.add_I_4() + this.clear();
   }

   private void init_V_GuiGraphicsExtract_8(GuiGraphicsExtractor context, int mouseX, int mouseY, int x, int y, int width, String label, boolean enabled) {
      autismclient.gui.f.b.PacketUtil.init(
         context,
         this.init_Font,
         x,
         y,
         width,
         this.add_I_4(),
         label,
         enabled ? autismclient.gui.f.b.ByteReader.Inner_StreamUtil.SECONDARY : autismclient.gui.f.b.ByteReader.Inner_StreamUtil.GHOST,
         enabled,
         mouseX,
         mouseY
      );
   }

   @Override
   public boolean init_Z_D_3(double mouseX, double mouseY, int button) {
      if (!this.write_Z) {
         return false;
      }

      if (button == 0 && mouseX >= this.flush_I && mouseX < this.flush_I + this.open && mouseY >= this.close_I && mouseY < this.close_I + 14) {
         cH bounds = this.init_cH();
         if (this.write(mouseX, mouseY, bounds)) {
            this.init_V_Z(false);
            return true;
         } else {
            this.sync_Z = true;
            this.init_D = mouseX - this.flush_I;
            this.load_D = mouseY - this.close_I;
            this.open_V_4();
            return true;
         }
      } else {
         if (this.update_Z) {
            return false;
         }

         if (button == 0) {
            autismclient.gui.f.b.PoolEntry.Inner_DataUtil scrollbar = this.init_AutismCustomFilter.init_PoolEntry_DataUtil;
            if (scrollbar != null && scrollbar.init_Z() && scrollbar.init(mouseX, mouseY)) {
               this.flush_Z = true;
               this.push = Math.max(0, (int)mouseY - scrollbar.update());
               this.init_AutismCustomFilter.init_CacheEntry.init(scrollbar, mouseY, this.push, this.next());
               return true;
            }
         }

         if (this.init_Z_BitSetUtil_2(this.init_BitSetUtil, mouseX, mouseY, button)) {
            return true;
         }

         if (button == 0 && this.init_AutismCustomFilter.init(mouseX, mouseY)) {
            AutismCustomFilterPresetOverlay.Inner_CachePool row = this.init_AutismCustomFilter.init(mouseY);
            if (row != null && row.init_bT_DataUtil != null) {
               this.init_Str_2 = row.init_bT_DataUtil.init_Str();
               this.init_BitSetUtil.init_BitSetUtil_Str(this.init_Str_2);
               this.open_V_4();
               return true;
            }
         }

         this.open_V_4();
         if (button == 0) {
            for (AutismCustomFilterPresetOverlay.Inner_DataUtil actionButton : this.init_List) {
               if (actionButton.init(mouseX, mouseY)) {
                  if (!actionButton.init_Z()) {
                     return true;
                  }

                  actionButton.init_Runnable.run();
                  return true;
               }
            }
         }

         return this.init_Z_D_D(mouseX, mouseY);
      }
   }

   private boolean init_Z_BitSetUtil_2(autismclient.gui.f.b.BitSetUtil field, double mouseX, double mouseY, int button) {
      boolean clicked = field.init_Z_SyncManager_2(this.init_SyncManager_D_D_2(mouseX, mouseY), (float)mouseX, (float)mouseY, button);
      if (clicked) {
         this.open_V_4();
         field.init_V_Z(true);
      }

      return clicked;
   }

   @Override
   public boolean load_Z_D_3(double mouseX, double mouseY, int button) {
      if (this.init_BitSetUtil.load_Z_SyncManager(this.init_SyncManager_D_D_2(mouseX, mouseY), (float)mouseX, (float)mouseY, button)) {
         return true;
      }

      if (button == 0 && this.flush_Z) {
         this.flush_Z = false;
         return true;
      }

      if (button == 0) {
         if (this.sync_Z) {
            this.a__V();
         }

         this.sync_Z = false;
      }

      return false;
   }

   @Override
   public boolean init_Z_D(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.init_BitSetUtil
         .init_Z_SyncManager(this.init_SyncManager_D_D_2(mouseX, mouseY), (float)mouseX, (float)mouseY, button, (float)deltaX, (float)deltaY)) {
         return true;
      } else if (this.flush_Z) {
         this.init_AutismCustomFilter.init_CacheEntry.init(this.init_AutismCustomFilter.init_PoolEntry_DataUtil, mouseY, this.push, this.next());
         return true;
      } else if (this.sync_Z && button == 0) {
         cH nextBounds = this.init_cH_cX_cH(
            this, new cH((int)(mouseX - this.init_D), (int)(mouseY - this.load_D), this.open, this.poll, this.write_Z, this.update_Z)
         );
         this.flush_I = nextBounds.init_I;
         this.close_I = nextBounds.load_I;
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean init_Z_D_2(double mouseX, double mouseY, double amount) {
      if (this.init_AutismCustomFilter.init(mouseX, mouseY)) {
         int maxScroll = this.init(
               this.init_AutismCustomFilter.init_I,
               this.init_AutismCustomFilter.load,
               this.init_AutismCustomFilter.read,
               this.init_AutismCustomFilter.write,
               this.init_AutismCustomFilter.init_List.size(),
               0
            )
            .open();
         this.init_AutismCustomFilter.init_CacheEntry.init_V_D(amount, this.next(), maxScroll);
         return true;
      } else {
         return false;
      }
   }

   private autismclient.gui.f.b.TextFormat.Inner_DataUtil init(int x, int y, int width, int listHeight, int rowCount, int scrollOffset) {
      return autismclient.gui.f.b.TextFormat.init(x, y, width, listHeight, rowCount, this.stop(), this.next(), scrollOffset, this.valid(), this.prev());
   }

   @Override
   public boolean init_Z_I(int keyCode, int scanCode, int modifiers) {
      if (!this.write_Z) {
         return false;
      }

      if (keyCode == 256) {
         if (this.init_BitSetUtil.load_Z()) {
            this.open_V_4();
            return true;
         } else {
            this.init_V_Z(false);
            return true;
         }
      } else if ((keyCode == 257 || keyCode == 335) && this.init_BitSetUtil.load_Z() && this.poll_Z_4()) {
         this.write_V_7();
         return true;
      } else {
         return !this.init_BitSetUtil.load_Z()
            ? false
            : this.init_BitSetUtil.init_Z_SyncManager_3(this.init_SyncManager_D_D_2(0.0, 0.0), keyCode, scanCode, modifiers);
      }
   }

   @Override
   public boolean init_Z_C_I(char chr, int modifiers) {
      return this.write_Z && this.init_BitSetUtil.load_Z()
         ? this.init_BitSetUtil.init_Z_SyncManager_2(this.init_SyncManager_D_D_2(0.0, 0.0), chr, modifiers)
         : false;
   }

   @Override
   public boolean init_Z_D_D(double mouseX, double mouseY) {
      if (!this.write_Z) {
         return false;
      }

      cH bounds = this.init_cH();
      int frameHeight = this.update_Z ? 14 : this.poll;
      return mouseX >= bounds.init_I && mouseX <= bounds.init_I + bounds.read && mouseY >= bounds.load_I && mouseY <= bounds.load_I + frameHeight
         ? true
         : mouseX >= this.init_BitSetUtil.load_F()
            && mouseX <= this.init_BitSetUtil.load_F() + this.init_BitSetUtil.write_F()
            && mouseY >= this.init_BitSetUtil.read_F()
            && mouseY <= this.init_BitSetUtil.read_F() + this.init_BitSetUtil.update_F();
   }

   private void open_V_4() {
      this.init_BitSetUtil.init_V_Z(false);
   }

   private autismclient.gui.f.c.SyncManager init_SyncManager_GuiGraphicsExtract_2(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      autismclient.gui.f.c.RingBuffer viewport = autismclient.gui.f.c.RingBuffer.init_RingBuffer_F(1.0F);
      return new autismclient.gui.f.c.SyncManager(
         context, this.init_Font, viewport, this.init_CharEncoder, viewport.init_F_D(mouseX), viewport.load(mouseY), delta
      );
   }

   private autismclient.gui.f.c.SyncManager init_SyncManager_D_D_2(double mouseX, double mouseY) {
      autismclient.gui.f.c.RingBuffer viewport = autismclient.gui.f.c.RingBuffer.init_RingBuffer_F(1.0F);
      return new autismclient.gui.f.c.SyncManager(null, this.init_Font, viewport, this.init_CharEncoder, viewport.init_F_D(mouseX), viewport.load(mouseY), 0.0F);
   }

   private int pop_I_6() {
      return 236;
   }

   private int tick_I_4() {
      return this.push_I_6() + this.load(8);
   }

   private int run_I_4() {
      return this.push_I_6() + this.load(3);
   }

   private int get_I_4() {
      return 4;
   }

   private int set_I_4() {
      return 4;
   }

   private int put() {
      return this.init_CharEncoder.init_I_l_I(autismclient.gui.f.b.l.load, 0);
   }

   private int add_I_4() {
      return autismclient.gui.f.RenderState.init(13);
   }

   private int clear() {
      return 2;
   }

   private int reset() {
      return 4;
   }

   private int start_I_4() {
      return autismclient.gui.f.RenderState.init(13);
   }

   private int stop() {
      return autismclient.gui.f.RenderState.init(14);
   }

   private int next() {
      return this.stop() + 1;
   }

   private int prev() {
      return 8;
   }

   private int valid() {
      return 3;
   }

   private static final class Inner_CachePool {
      private final boolean init_Z;
      private final boolean load;
      private final String init_Str;
      private final bT.Inner_DataUtil init_bT_DataUtil;

      private Inner_CachePool(boolean header, boolean note, String label, bT.Inner_DataUtil entry) {
         this.init_Z = header;
         this.load = note;
         this.init_Str = label;
         this.init_bT_DataUtil = entry;
      }

      private static AutismCustomFilterPresetOverlay.Inner_CachePool init(String label) {
         return new AutismCustomFilterPresetOverlay.Inner_CachePool(true, false, label, null);
      }

      private static AutismCustomFilterPresetOverlay.Inner_CachePool load(String label) {
         return new AutismCustomFilterPresetOverlay.Inner_CachePool(false, true, label, null);
      }

      private static AutismCustomFilterPresetOverlay.Inner_CachePool init(bT.Inner_DataUtil entry) {
         return new AutismCustomFilterPresetOverlay.Inner_CachePool(false, false, null, entry);
      }
   }

   private record Inner_DataUtil(int init_I, int load, int read, int write, Runnable init_Runnable, boolean init_Z) {
      boolean init(double mouseX, double mouseY) {
         return mouseX >= this.init_I && mouseX < this.init_I + this.read && mouseY >= this.load && mouseY < this.load + this.write;
      }
   }

   private final class Inner_StreamUtil {
      private int init_I;
      private int load;
      private int read;
      private int write;
      private final autismclient.gui.f.b.CacheEntry init_CacheEntry = new autismclient.gui.f.b.CacheEntry();
      private autismclient.gui.f.b.PoolEntry.Inner_DataUtil init_PoolEntry_DataUtil = null;
      private List<AutismCustomFilterPresetOverlay.Inner_CachePool> init_List = List.of();

      private void init(int x, int y, int width, int height, autismclient.gui.f.b.PoolEntry.Inner_DataUtil scrollbarMetrics) {
         this.init_I = x;
         this.load = y;
         this.read = width;
         this.write = height;
         this.init_PoolEntry_DataUtil = scrollbarMetrics;
      }

      private boolean init(double mouseX, double mouseY) {
         return mouseX >= this.init_I && mouseX <= this.init_I + this.read && mouseY >= this.load && mouseY <= this.load + this.write;
      }

      private AutismCustomFilterPresetOverlay.Inner_CachePool init(double mouseY) {
         int idx = (int)((mouseY - this.load - 2.0 + this.init_CacheEntry.load()) / AutismCustomFilterPresetOverlay.this.next());
         return idx >= 0 && idx < this.init_List.size() ? this.init_List.get(idx) : null;
      }
   }
}
