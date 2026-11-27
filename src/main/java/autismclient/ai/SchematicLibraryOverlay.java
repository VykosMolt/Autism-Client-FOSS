package autismclient.ai;

import autismclient.compat263.Compat263;
import autismclient.gui.f.PacketUtil;
import autismclient.gui.ui.widgets.ScrollbarMetricsHelper;
import autismclient.util.OverlayBounds;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.network.chat.Component;

final class SchematicLibraryOverlay extends autismclient.util.DraggableOverlayWindow {
   static final String init_Str_2 = "schematic-library";
   private static final int flush_I = 4;
   private static final int close_I = 13;
   private static final int open = 3;
   private static final int poll = 20;
   private static final int push = 21;
   private static final int pop = 2;
   private static final int tick = 14;
   private static final DateTimeFormatter init_DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
   private static final Comparator<SchematicLibrary.Inner_DataUtil> init_Comparator = Comparator.comparing(SchematicLibrary.Inner_DataUtil::name, String.CASE_INSENSITIVE_ORDER)
      .thenComparing(entry -> entry.file().toString());
   private final SchematicWorkspace init_cV;
   private final Minecraft init_Minecraft = Minecraft.getInstance();
   private final Font init_Font = this.init_Minecraft.font;
   private final List<SchematicLibraryOverlay.Inner_BufferHelper> init_List = new ArrayList<>();
   private final autismclient.gui.ui.widgets.ScrollOffsetController init_CacheEntry = new autismclient.gui.ui.widgets.ScrollOffsetController();
   private final NumberFormat init_NumberFormat = NumberFormat.getIntegerInstance();
   private SchematicLibraryOverlay.Inner_ConfigStore init_SchematicLibraryOv_2 = SchematicLibraryOverlay.Inner_ConfigStore.FILES;
   private autismclient.util.ThemedTextInputBox init_k;
   private autismclient.gui.f.NetHelper init_NetHelper;
   private autismclient.gui.f.NetHelper load_NetHelper;
   private autismclient.gui.ui.widgets.ScrollListLayout.ListLayout init_TextFormat_DataUti;
   private boolean write_Z;
   private boolean update_Z;
   private boolean sync_Z = true;
   private boolean flush_Z;
   private double init_D;
   private double load_D;
   private int run;
   private String load_Str = "";
   private Path init_Path;
   private List<SchematicLibrary.Inner_DataUtil> load_List = List.of();
   private List<SchematicLibrary.Inner_DataUtil> read_List = List.of();
   private String read_Str;
   private SchematicLibraryOverlay.Inner_CachePool init_SchematicLibraryOv;
   private SchematicLibraryOverlay.Inner_CachePool load_SchematicLibraryOv;
   private SchematicLibraryOverlay.Inner_StreamUtil init_SchematicLibraryOv_3;
   private SchematicLibraryOverlay.Inner_StreamUtil load_SchematicLibraryOv_2;
   private Set<String> init_Set;
   private final autismclient.gui.ui.widgets.DropdownMenu init_RefQueue;
   private final autismclient.gui.ui.widgets.DropdownMenu load_RefQueue;

   SchematicLibraryOverlay(SchematicWorkspace workspace) {
      super("schematic-library", 270, 132);
      this.init_SchematicLibraryOv = SchematicLibraryOverlay.Inner_CachePool.EDITED;
      this.init_SchematicLibraryOv_3 = SchematicLibraryOverlay.Inner_StreamUtil.ALL;
      this.init_Set = Set.of();
      this.init_RefQueue = new autismclient.gui.ui.widgets.DropdownMenu(
         null, Arrays.stream(SchematicLibraryOverlay.Inner_CachePool.values()).map(v -> v.init_Str).toList(), this.init_SchematicLibraryOv.init_Str, label -> {
            for (SchematicLibraryOverlay.Inner_CachePool value : SchematicLibraryOverlay.Inner_CachePool.values()) {
               if (value.init_Str.equals(label)) {
                  this.init_SchematicLibraryOv = value;
               }
            }

            this.init_CacheEntry.setOffsetUnclamped(0);
         }
      );
      this.load_RefQueue = new autismclient.gui.ui.widgets.DropdownMenu(
         null,
         Arrays.stream(SchematicLibraryOverlay.Inner_StreamUtil.values()).map(v -> v.init_Str).toList(),
         this.init_SchematicLibraryOv_3.init_Str,
         label -> {
            for (SchematicLibraryOverlay.Inner_StreamUtil value : SchematicLibraryOverlay.Inner_StreamUtil.values()) {
               if (value.init_Str.equals(label)) {
                  this.init_SchematicLibraryOv_3 = value;
               }
            }

            this.init_CacheEntry.setOffsetUnclamped(0);
         }
      );
      this.init_cV = workspace;
      this.windowX = 100;
      this.windowY = 38;
      this.loadBounds();
      this.write_V_2();
   }

   void init_V_Str(String name) {
      this.update_V_2();
      if (!name.equals("SORT") && !name.equals("FILTER")) {
         this.init_V_SchematicLibraryOv(SchematicLibraryOverlay.Inner_ConfigStore.valueOf(name));
      } else {
         this.init_V_SchematicLibraryOv(SchematicLibraryOverlay.Inner_ConfigStore.FILES);
         (name.equals("SORT") ? this.init_RefQueue : this.load_RefQueue).open();
      }
   }

   @Override
   public boolean shouldRememberOnDismiss() {
      return true;
   }

   @Override
   public boolean isResizable() {
      return true;
   }

   @Override
   public boolean supportsHeaderCollapseClick() {
      return true;
   }

   @Override
   public int getMinWidth() {
      return 238;
   }

   @Override
   public int getMinHeight() {
      return this.init_I_I(1);
   }

   @Override
   protected int getResizeMinHeight() {
      return this.init_I_I(Math.clamp(this.push_I(), 1, 3));
   }

   private int push_I() {
      int var10000;
      switch (this.init_SchematicLibraryOv_2) {
         case FILES:
            var10000 = this.init_List().size();
            break;
         case PLACEMENTS:
            var10000 = this.init_cV.init_List().size();
            break;
         case CAPTURE:
            var10000 = 0;
            break;
         case FOLDERS:
            var10000 = this.init_cV.init_cR.init_List.size();
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private int pop_I() {
      return 20 + (this.init_cV.load_Z() ? 16 : 0) + (this.init_cV.init_I() >= 0 ? 13 : 0);
   }

   private int captureHeight() {
      int bodyTop = 34;
      int contentBottom = bodyTop + 48 + 14;
      return contentBottom + 4 + (this.init_cV.load_Z() ? 16 : 0) + (this.init_cV.init_I() >= 0 ? 13 : 0);
   }

   private int init_I_I(int rows) {
      int bodyTop = 34;
      if (this.init_SchematicLibraryOv_2 == SchematicLibraryOverlay.Inner_ConfigStore.CAPTURE) {
         return this.captureHeight();
      }

      byte var10000;
      switch (this.init_SchematicLibraryOv_2) {
         case FILES:
            var10000 = 16;
            break;
         case PLACEMENTS:
            var10000 = (byte)(this.init_cV.update_Z() ? 17 : 0);
            break;
         default:
            var10000 = 0;
      }

      int beforeList = var10000;
      return bodyTop + beforeList + 4 + 21 * rows + this.pop_I();
   }

   @Override
   public boolean hasFocusedTextInput() {
      return this.init_k != null && this.init_k.isFocused();
   }

   @Override
   public void clearInputFocus() {
      if (this.init_k != null) {
         this.init_k.setFocused(false);
      }
   }

   @Override
   public void setVisible(boolean value) {
      if (!value) {
         this.update_V_2();
         this.clearInputFocus();
         this.update_Z = this.write_Z = false;
      }

      super.setVisible(value);
   }

   private void write_V_2() {
      if (this.init_Minecraft != null) {
         autismclient.gui.ui.widgets.TextInputWidget.clearGlobalFocus();
         this.init_k = null;
         if (this.init_SchematicLibraryOv_2 == SchematicLibraryOverlay.Inner_ConfigStore.FILES
            || this.init_SchematicLibraryOv_2 == SchematicLibraryOverlay.Inner_ConfigStore.CAPTURE) {
            this.init_k = new autismclient.util.ThemedTextInputBox(this.init_Minecraft, this.init_Font, 0, 0, 100, 13, false);
            this.init_k.setMaxLength(100);
            this.init_k
               .setPlaceholder(
                  Component.literal(
                     this.init_SchematicLibraryOv_2 == SchematicLibraryOverlay.Inner_ConfigStore.FILES ? "Search schematics" : "File name (optional)"
                  )
               );
            this.init_k.setValue(this.init_SchematicLibraryOv_2 == SchematicLibraryOverlay.Inner_ConfigStore.FILES ? this.load_Str : this.init_cV.init_Str);
            this.init_k.setChangedListener(value -> {
               if (this.init_SchematicLibraryOv_2 == SchematicLibraryOverlay.Inner_ConfigStore.FILES) {
                  this.load_Str = value;
                  this.init_CacheEntry.setOffsetUnclamped(0);
               } else {
                  this.init_cV.init_Str = value;
               }
            });
         }
      }
   }

   private void init_V_SchematicLibraryOv(SchematicLibraryOverlay.Inner_ConfigStore value) {
      if (this.init_SchematicLibraryOv_2 != value) {
         this.init_SchematicLibraryOv_2 = value;
         this.update_V_2();
         this.init_CacheEntry.setOffsetUnclamped(0);
         this.write_Z = false;
         this.write_V_2();
      }
   }

   @Override
   public void render(GuiGraphicsExtractor graphics, int mx, int my, float delta) {
      if (this.windowVisible || this.isClosing()) {
         this.init_List.clear();
         this.init_TextFormat_DataUti = null;
         this.load_NetHelper = null;
         int auto = this.init_I_I(Math.clamp(this.push_I(), 1, 5));
         this.windowWidth = this.resolveWindowWidth(270, this.getMinWidth());
         this.windowHeight = this.init_SchematicLibraryOv_2 == SchematicLibraryOverlay.Inner_ConfigStore.CAPTURE
            ? Math.min(auto, getMaxScreenWindowHeight())
            : this.resolveWindowHeight(auto, this.getResizeMinHeight());
         OverlayBounds clamped = this.clampCurrentBounds(this);
         this.windowX = clamped.x;
         this.windowY = clamped.y;
         this.windowWidth = clamped.width;
         this.windowHeight = clamped.height;
         this.init_NetHelper = autismclient.gui.f.NetHelper.init(this.windowX, this.windowY, this.windowWidth, this.windowHeight);
         this.renderWindowFrame(graphics, mx, my, this.getBounds(), "Schematic Library", this.windowCollapsed, this.update_Z);
         if (!this.isVisuallyCollapsed(this.windowCollapsed) && this.init_NetHelper.update() >= 190 && this.init_NetHelper.sync() >= 27) {
            boolean clipped = this.beginBodyClip(graphics, this.getBounds(), this.windowCollapsed);

            try {
               int width = this.init_NetHelper.update();
               autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(graphics, this.init_Font, mx, my);
               autismclient.gui.f.NetHelper tabs = autismclient.gui.f.NetHelper.init(
                  this.init_NetHelper.read() + 4, this.init_NetHelper.write() + 14 + 4, width - 8, 13
               );
               String[] names = new String[]{"Files", "Placements", "Capture", "Folders"};
               int tabX = tabs.read();

               for (SchematicLibraryOverlay.Inner_ConfigStore value : SchematicLibraryOverlay.Inner_ConfigStore.values()) {
                  String label = names[value.ordinal()];
                  int tabWidth = Math.min((tabs.update() - 9) / 4, ui.init_PoolEntry().measureWidth(label) + 12);
                  autismclient.gui.f.NetHelper at = autismclient.gui.f.NetHelper.init(tabX, tabs.write(), tabWidth, 13);
                  autismclient.gui.ui.widgets.TabStripRenderer.init(ui, at, label, value == this.init_SchematicLibraryOv_2);
                  this.init_List.add(new SchematicLibraryOverlay.Inner_BufferHelper(at, () -> this.init_V_SchematicLibraryOv(value)));
                  tabX = at.init() + 3;
               }

               int x = tabs.read();
               int y = tabs.load() + 3;
               int w = tabs.update();
               int bottom = this.init_NetHelper.load() - this.pop_I();
               switch (this.init_SchematicLibraryOv_2) {
                  case FILES:
                     this.init_V_SyncManager(ui, x, y, w, bottom, delta);
                     break;
                  case PLACEMENTS:
                     this.init_V_SyncManager_2(ui, x, y, w, bottom);
                     break;
                  case CAPTURE:
                     this.load(ui, x, y, w, bottom, delta);
                     break;
                  case FOLDERS:
                     this.load(ui, x, y, w, bottom);
               }

               if (this.init_cV.load_Z()) {
                  int buildY = bottom + 6 + 13;
                  String label = "Stop Build";
                  int buttonWidth = ui.init_PoolEntry().measureWidth(label) + 12;
                  this.init_V_SyncManager_5(ui, this.init_cV.init_Str(), x, buildY + 2, w - buttonWidth - 3, ui.init_TableIndex().colors().textSecondary);
                  this.init_V_SyncManager_2(
                     ui,
                     autismclient.gui.f.NetHelper.init(x + w - buttonWidth, buildY, buttonWidth, 13),
                     label,
                     autismclient.gui.f.b.CachePool.Inner_DataUtil.update,
                     true,
                     this.init_cV::flush_V
                  );
               }

               int progress = this.init_cV.init_I();
               if (progress >= 0) {
                  SchematicRecording recording = this.init_cV.activeRecording();
                  String statusText = recording != null && !recording.putAt_Z ? recording.putAt_Str() : this.init_cV.load_Str();
                  String pctText = progress + "%";
                  int pctWidth = ui.init_PoolEntry().measureWidth(pctText);
                  int barWidth = Math.min(90, w / 3);
                  int progressY = this.init_NetHelper.load() - 4 - 7;
                  int pctX = this.init_NetHelper.init() - 10 - 3 - pctWidth;
                  int barX = pctX - 3 - barWidth;
                  this.init_V_SyncManager_5(ui, statusText, x, progressY - 1, barX - x - 3, ui.init_TableIndex().colors().textSecondary);
                  autismclient.gui.ui.widgets.ProgressBarRenderer.init(ui, autismclient.gui.f.NetHelper.init(barX, progressY + 2, barWidth, 4), progress / 100.0);
                  this.init_V_SyncManager_5(ui, pctText, pctX, progressY - 1, pctWidth, ui.init_TableIndex().colors().textPrimary);
               }
            } finally {
               this.endBodyClip(graphics, clipped);
            }

            autismclient.gui.ui.widgets.DropdownMenu var231 = this.init_RefQueue();
            if (this.init_SchematicLibraryOv_2 == SchematicLibraryOverlay.Inner_ConfigStore.FILES && var231 != null) {
               graphics.nextStratum();
               var231.render(autismclient.gui.f.UiHelper.init(graphics, this.init_Font, mx, my));
            }
         }
      }
   }

   private void init_V_SyncManager(autismclient.gui.f.SyncManager ui, int x, int y, int width, int bottom, float delta) {
      int refreshWidth = ui.init_PoolEntry().measureWidth("Refresh") + 10;
      int sortWidth = Math.min(74, ui.init_PoolEntry().measureWidth(this.init_SchematicLibraryOv.init_Str) + 18);
      int filterWidth = Math.min(63, ui.init_PoolEntry().measureWidth(this.init_SchematicLibraryOv_3.init_Str) + 18);
      int refreshX = x + width - refreshWidth;
      int sortX = refreshX - 3 - sortWidth;
      int filterX = sortX - 3 - filterWidth;
      this.init_V_SyncManager(ui, x, y, Math.max(24, filterX - 3 - x), delta);
      this.init(ui, this.load_RefQueue, autismclient.gui.f.NetHelper.init(filterX, y, filterWidth, 13), this.init_SchematicLibraryOv_3.init_Str);
      this.init(ui, this.init_RefQueue, autismclient.gui.f.NetHelper.init(sortX, y, sortWidth, 13), this.init_SchematicLibraryOv.init_Str);
      this.init_V_SyncManager_2(
         ui,
         autismclient.gui.f.NetHelper.init(refreshX, y, refreshWidth, 13),
         "Refresh",
         autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
         this.init_cV.read_Z(),
         this.init_cV::write_V
      );
      List<SchematicLibrary.Inner_DataUtil> entries = this.init_List();
      this.init(
         ui,
         x,
         y + 13 + 3,
         width,
         Math.max(4, bottom - y - 13 - 3),
         entries.size(),
         (index, row) -> {
            SchematicLibrary.Inner_DataUtil e = entries.get(index);
            String blocks = e.blocks() >= 0L
               ? this.init_NumberFormat.format(e.blocks()) + " blocks"
               : (e.error().isEmpty() ? "Counting blocks…" : "Unreadable");
            boolean added = this.init_SchematicLibraryOv == SchematicLibraryOverlay.Inner_CachePool.ADDED && e.added() > 0L;
            String date = (added ? "Added " : "Edited ") + init_DateTimeFormatter.format(Instant.ofEpochMilli(added ? e.added() : e.modified()));
            this.init_V_SyncManager_3(
               ui,
               row,
               e.name(),
               blocks + " · " + init_Str_J(e.bytes()) + " · " + date + (this.init_cV.init_cR_StreamUtil_cR_DataUtil(e) != null ? " · Loaded" : ""),
               this.init_cV.load_Str.equals(e.id()),
               () -> this.init_cV.load_Str = e.id()
            );
         }
      );
      if (entries.isEmpty()) {
         this.init_V_SyncManager_4(ui, "No schematic files", x, y + 13 + 12, width);
      }

      SchematicLibrary.Inner_DataUtil file = entries.stream().filter(e -> e.id().equals(this.init_cV.load_Str)).findFirst().orElse(null);
      SchematicLibrary.Inner_StreamUtil active = this.init_cV.init_cR_StreamUtil_cR_DataUtil(file);
      this.init_V_SyncManager_3(
         ui,
         x,
         bottom + 3,
         width,
         List.of(
            new SchematicLibraryOverlay.Inner_DataUtil(
               active == null ? "Load" : "Unload",
               autismclient.gui.f.b.CachePool.Inner_DataUtil.read,
               this.init_cV.read_Z()
                  && this.init_cV.write_Z()
                  && (active != null ? this.init_cV.read_Z(active) : !this.init_cV.flush_Z() && file != null && file.error().isEmpty()),
               () -> {
                  if (active == null) {
                     this.init_cV.load_V_cR_DataUtil(file);
                     this.init_V_SchematicLibraryOv(SchematicLibraryOverlay.Inner_ConfigStore.PLACEMENTS);
                  } else {
                     this.init_cV.load_V_cR_StreamUtil(active);
                  }
               }
            ),
            new SchematicLibraryOverlay.Inner_DataUtil(
               "Rename",
               autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
               file != null && !this.init_cV.flush_Z(),
               () -> this.init_cV.init_V_cR_DataUtil(file)
            ),
            new SchematicLibraryOverlay.Inner_DataUtil(
               "Open folder",
               autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
               this.init_cV.read_Z(),
               () -> this.init_cV.load_V_Path(this.init_cV.init_cR.load_Path)
            )
         )
      );
   }

   private List<SchematicLibrary.Inner_DataUtil> init_List() {
      List<SchematicLibrary.Inner_DataUtil> current = this.init_cV.init_cR.load_List;
      Set<String> loaded = Set.of();
      if (this.init_SchematicLibraryOv_3 == SchematicLibraryOverlay.Inner_StreamUtil.LOADED
         || this.init_SchematicLibraryOv_3 == SchematicLibraryOverlay.Inner_StreamUtil.UNLOADED) {
         loaded = new HashSet<>();

         for (SchematicLibrary.Inner_StreamUtil placement : this.init_cV.init_List()) {
            if (placement.visible()) {
               loaded.add(placement.file());
            }
         }
      }

      if (current != this.load_List
         || !this.load_Str.equals(this.read_Str)
         || this.init_SchematicLibraryOv != this.load_SchematicLibraryOv
         || this.init_SchematicLibraryOv_3 != this.load_SchematicLibraryOv_2
         || !loaded.equals(this.init_Set)) {
         this.load_List = current;
         this.read_Str = this.load_Str;
         this.load_SchematicLibraryOv = this.init_SchematicLibraryOv;
         this.load_SchematicLibraryOv_2 = this.init_SchematicLibraryOv_3;
         this.init_Set = loaded;
         String[] words = this.load_Str.strip().toLowerCase(Locale.ROOT).split("\\s+");
         this.read_List = current.stream().filter(entry -> {
            if (!this.init_SchematicLibraryOv_3.init(entry, this.init_Set)) {
               return false;
            }

            String name = entry.name().toLowerCase(Locale.ROOT);

            for (String word : words) {
               if (!name.contains(word)) {
                  return false;
               }
            }

            return true;
         }).sorted(this.init_SchematicLibraryOv.init_Comparator).toList();
      }

      return this.read_List;
   }

   private void init(autismclient.gui.f.SyncManager ui, autismclient.gui.ui.widgets.DropdownMenu dropdown, autismclient.gui.f.NetHelper bounds, String label) {
      dropdown.setBounds(bounds);
      autismclient.gui.ui.widgets.DropdownMenu.renderTrigger(ui, bounds, label, bounds.init_Z(ui.read(), ui.write()), dropdown.isOpen());
   }

   private autismclient.gui.ui.widgets.DropdownMenu init_RefQueue() {
      if (this.init_RefQueue.isOpen()) {
         return this.init_RefQueue;
      } else {
         return this.load_RefQueue.isOpen() ? this.load_RefQueue : null;
      }
   }

   private void update_V_2() {
      this.init_RefQueue.close();
      this.load_RefQueue.close();
   }

   @Override
   public boolean containsPoint(double x, double y) {
      autismclient.gui.ui.widgets.DropdownMenu menu = this.init_RefQueue();
      return super.containsPoint(x, y) || this.windowVisible && menu != null && menu.containsPopupPoint((int)x, (int)y);
   }

   private void init_V_SyncManager_2(autismclient.gui.f.SyncManager ui, int x, int y, int width, int bottom) {
      List<SchematicLibrary.Inner_StreamUtil> placements = this.init_cV.init_List();
      SchematicLibrary.Inner_StreamUtil picked = this.init_cV.init_cR_StreamUtil_Str(this.init_cV.read_Str);
      boolean local = this.init_cV.update_Z();
      int listBottom = bottom - (local ? 17 : 0);
      this.init(ui, x, y, width, Math.max(4, listBottom - y), placements.size(), (index, row) -> {
         SchematicLibrary.Inner_StreamUtil p = placements.get(index);
         String detail = p.x() + ", " + p.y() + ", " + p.z() + "  ·  " + p.rotation() + "°  ·  " + this.init_cV.init_Str_cR_StreamUtil(p);
         this.init_V_SyncManager_3(ui, row, SchematicWorkspace.load_Str_cR_StreamUtil(p), detail, p.id().equals(this.init_cV.read_Str), () -> this.init_cV.read_Str = p.id());
      });
      if (placements.isEmpty()) {
         this.init_V_SyncManager_4(ui, this.init_cV.write_Z() ? "Load a file to place its hologram" : "Join a world to place schematics", x, y + 8, width);
      }

      if (local) {
         int airWidth = Math.min(width / 2, ui.init_PoolEntry().measureWidth("Paste air") + 37);
         int entityWidth = Math.min(width - airWidth - 3, ui.init_PoolEntry().measureWidth("Entities") + 37);
         this.init_V_SyncManager_4(
            ui,
            autismclient.gui.f.NetHelper.init(x, listBottom + 3, airWidth, 14),
            "Paste air",
            this.sync_Z,
            !this.init_cV.flush_Z(),
            () -> this.sync_Z = !this.sync_Z
         );
         this.init_V_SyncManager_4(
            ui,
            autismclient.gui.f.NetHelper.init(x + airWidth + 6, listBottom + 3, entityWidth, 14),
            "Entities",
            this.flush_Z,
            !this.init_cV.flush_Z(),
            () -> this.flush_Z = !this.flush_Z
         );
      }

      if (!this.init_cV.flush_Z() || this.init_cV.read_Z(picked)) {
         boolean available = this.init_cV.init_Z_cR_StreamUtil(picked);
         boolean paused = picked != null && !picked.paste().isEmpty();
         ArrayList<SchematicLibraryOverlay.Inner_DataUtil> actions = new ArrayList<>();
         actions.add(
            new SchematicLibraryOverlay.Inner_DataUtil(
               picked != null && picked.visible() ? "Unload" : "Show",
               autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
               this.init_cV.read_Z(picked) && !paused,
               () -> this.init_cV.load_V_cR_StreamUtil(picked)
            )
         );
         actions.add(
            new SchematicLibraryOverlay.Inner_DataUtil(
               "Move", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, this.init_cV.load_Z_cR_StreamUtil(picked), () -> this.init_cV.write(picked)
            )
         );
         actions.add(
            new SchematicLibraryOverlay.Inner_DataUtil(
               "Build",
               autismclient.gui.f.b.CachePool.Inner_DataUtil.read,
               available && !paused && !this.init_cV.load_Z() && !this.init_cV.flush_Z(),
               () -> this.init_cV.update(picked)
            )
         );
         if (local) {
            actions.add(
               new SchematicLibraryOverlay.Inner_DataUtil(
                  paused ? "Resume" : "Paste",
                  autismclient.gui.f.b.CachePool.Inner_DataUtil.write,
                  available && !this.init_cV.load_Z() && !this.init_cV.flush_Z(),
                  () -> this.init_cV.init(picked, this.sync_Z, this.flush_Z)
               )
            );
         }

         actions.add(
            new SchematicLibraryOverlay.Inner_DataUtil(
               "Rename",
               autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
               picked != null && !this.init_cV.flush_Z(),
               () -> this.init_cV.init_V_cR_StreamUtil(picked)
            )
         );
         actions.add(
            new SchematicLibraryOverlay.Inner_DataUtil(
               "Remove", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, this.init_cV.read_Z(picked), () -> this.init_cV.read_V(picked)
            )
         );
         this.init_V_SyncManager_3(ui, x, bottom + 3, width, actions);
      } else if (!this.init_cV.sync_Z()) {
         this.init_V_SyncManager_3(
            ui,
            x,
            bottom + 3,
            width,
            List.of(
               new SchematicLibraryOverlay.Inner_DataUtil(
                  this.init_cV.init_Z() ? "Terminate" : "Cancel", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, true, this.init_cV::close_V
               )
            )
         );
      }
   }

   private void load(autismclient.gui.f.SyncManager ui, int x, int y, int width, int bottom, float delta) {
      this.init_V_SyncManager(ui, x, y, width, delta);
      boolean busy = this.init_cV.flush_Z();
      int step = 16;
      int rowY = y + step;
      this.init_V_SyncManager_3(
         ui,
         x,
         rowY,
         width,
         List.of(
            new SchematicLibraryOverlay.Inner_DataUtil(
               "Select corners",
               autismclient.gui.f.b.CachePool.Inner_DataUtil.read,
               this.init_cV.write_Z() && !busy,
               this.init_cV::sync_V
            ),
            new SchematicLibraryOverlay.Inner_DataUtil(
               "Save area",
               autismclient.gui.f.b.CachePool.Inner_DataUtil.read,
               this.init_cV.read_Z() && this.init_cV.write_Z() && this.init_cV.init_cP_DataUtil != null && !busy,
               () -> this.init_cV.init_V_Z_Z(false, this.flush_Z)
            )
         )
      );
      int row2Y = rowY + step;
      SchematicRecording recording = this.init_cV.activeRecording();
      if (recording != null && recording.putAt_Z()) {
         this.init_V_SyncManager_3(
            ui,
            x,
            row2Y,
            width,
            List.of(
               new SchematicLibraryOverlay.Inner_DataUtil(
                  "Stop & save", autismclient.gui.f.b.CachePool.Inner_DataUtil.update, true, this.init_cV::stopAndSaveRecording
               ),
               new SchematicLibraryOverlay.Inner_DataUtil(
                  "Cancel", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, true, this.init_cV::close_V
               )
            )
         );
      } else if (busy) {
         this.init_V_SyncManager_3(
            ui,
            x,
            row2Y,
            width,
            List.of(
               new SchematicLibraryOverlay.Inner_DataUtil(
                  this.init_cV.init_Z() ? "Terminate" : "Cancel", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, true, this.init_cV::close_V
               )
            )
         );
      } else {
         this.init_V_SyncManager_3(
            ui,
            x,
            row2Y,
            width,
            List.of(
               new SchematicLibraryOverlay.Inner_DataUtil(
                  "Record chunks",
                  autismclient.gui.f.b.CachePool.Inner_DataUtil.write,
                  this.init_cV.read_Z() && this.init_cV.write_Z(),
                  () -> this.init_cV.startRecording(this.flush_Z)
               ),
               new SchematicLibraryOverlay.Inner_DataUtil(
                  "Save loaded chunks",
                  autismclient.gui.f.b.CachePool.Inner_DataUtil.write,
                  this.init_cV.read_Z() && this.init_cV.write_Z(),
                  () -> this.init_cV.init_V_Z_Z(true, this.flush_Z)
               )
            )
         );
      }

      this.init_V_SyncManager_4(
         ui,
         autismclient.gui.f.NetHelper.init(x, row2Y + step, Math.min(width, ui.init_PoolEntry().measureWidth("Capture entities") + 37), 14),
         "Capture entities",
         this.flush_Z,
         !busy,
         () -> this.flush_Z = !this.flush_Z
      );
   }

   private void load(autismclient.gui.f.SyncManager ui, int x, int y, int width, int bottom) {
      List<Path> folders = this.init_cV.init_cR.init_List;
      this.init(
         ui,
         x,
         y,
         width,
         Math.max(4, bottom - y),
         folders.size(),
         (index, row) -> {
            Path directory = folders.get(index);
            boolean local = directory.equals(this.init_cV.init_cR.load_Path);
            this.init_V_SyncManager_3(
               ui,
               row,
               local ? "Client schematics" : (directory.getFileName() == null ? directory.toString() : directory.getFileName().toString()),
               directory.toString(),
               directory.equals(this.init_Path),
               () -> this.init_Path = directory
            );
         }
      );
      boolean selected = this.init_Path != null && folders.contains(this.init_Path);
      this.init_V_SyncManager_3(
         ui,
         x,
         bottom + 3,
         width,
         List.of(
            new SchematicLibraryOverlay.Inner_DataUtil(
               "Add folder", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, this.init_cV.read_Z(), this.init_cV::update_V
            ),
            new SchematicLibraryOverlay.Inner_DataUtil(
               "Open", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, selected, () -> this.init_cV.load_V_Path(this.init_Path)
            ),
            new SchematicLibraryOverlay.Inner_DataUtil(
               "Remove", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, selected && !this.init_Path.equals(this.init_cV.init_cR.load_Path), () -> {
                  this.init_cV.init_V_Path(this.init_Path);
                  this.init_Path = null;
               }
            )
         )
      );
   }

   private void init_V_SyncManager_3(autismclient.gui.f.SyncManager ui, int x, int y, int width, List<SchematicLibraryOverlay.Inner_DataUtil> actions) {
      int available = width - (actions.size() - 1) * 3;
      int total = actions.stream().mapToInt(actionx -> ui.init_PoolEntry().measureWidth(actionx.label()) + 10).sum();

      for (SchematicLibraryOverlay.Inner_DataUtil action : actions) {
         int natural = ui.init_PoolEntry().measureWidth(action.label()) + 10;
         int buttonWidth = Math.max(1, total <= available ? natural : natural * available / total);
         this.init_V_SyncManager_2(ui, autismclient.gui.f.NetHelper.init(x, y, buttonWidth, 13), action.label(), action.tone(), action.enabled(), action.run());
         x += buttonWidth + 3;
      }
   }

   private void init_V_SyncManager_2(
      autismclient.gui.f.SyncManager ui,
      autismclient.gui.f.NetHelper bounds,
      String label,
      autismclient.gui.f.b.CachePool.Inner_DataUtil tone,
      boolean enabled,
      Runnable run
   ) {
      autismclient.gui.f.b.CachePool.init(
         ui, bounds, label, enabled ? tone : autismclient.gui.f.b.CachePool.Inner_DataUtil.load, enabled && bounds.init_Z(ui.read(), ui.write()), false
      );
      if (enabled) {
         this.init_List.add(new SchematicLibraryOverlay.Inner_BufferHelper(bounds, run));
      }
   }

   private void init_V_SyncManager_4(
      autismclient.gui.f.SyncManager ui, autismclient.gui.f.NetHelper bounds, String text, boolean value, boolean enabled, Runnable run
   ) {
      autismclient.gui.ui.widgets.ToggleSwitchRenderer.init(ui, bounds, text, value, enabled && bounds.init_Z(ui.read(), ui.write()), "schematic/" + text);
      if (enabled) {
         this.init_List.add(new SchematicLibraryOverlay.Inner_BufferHelper(bounds, run));
      }
   }

   private void init_V_SyncManager(autismclient.gui.f.SyncManager ui, int x, int y, int width, float delta) {
      if (this.init_k != null) {
         this.init_k.setX(x);
         this.init_k.setY(y);
         this.init_k.setWidth(width);
         this.init_k.render(ui.init_GuiGraphicsExtract(), ui.read(), ui.write(), delta);
      }
   }

   private void init(autismclient.gui.f.SyncManager ui, int x, int y, int w, int h, int count, BiConsumer<Integer, autismclient.gui.f.NetHelper> render) {
      this.init_TextFormat_DataUti = autismclient.gui.ui.widgets.ScrollListLayout.compute(x, y, w, h, count, 20, 21, this.init_CacheEntry.getOffset(), 6, 4, 2, 4, true);
      this.load_NetHelper = autismclient.gui.f.NetHelper.init(x, y, w, h);
      this.init_CacheEntry.setOffset(this.init_TextFormat_DataUti.clampedScroll(), this.init_TextFormat_DataUti.maxScroll());
      this.init_TextFormat_DataUti.drawFrame(ui.init_GuiGraphicsExtract(), false);
      if (this.init_TextFormat_DataUti.viewHeight() >= 4) {
         this.init_TextFormat_DataUti.pushContentClip(ui.init_GuiGraphicsExtract());

         try {
            this.init_TextFormat_DataUti
               .forEachVisibleRow(
                  count,
                  (index, top) -> render.accept(
                     index, autismclient.gui.f.NetHelper.init(x + this.init_TextFormat_DataUti.contentInset(), top, this.init_TextFormat_DataUti.contentWidth(), 20)
                  )
               );
         } finally {
            this.init_TextFormat_DataUti.popContentClip(ui.init_GuiGraphicsExtract());
         }

         this.init_TextFormat_DataUti.renderScrollbar(ui.init_GuiGraphicsExtract(), ui.read(), ui.write(), this.write_Z);
      }
   }

   private void init_V_SyncManager_3(
      autismclient.gui.f.SyncManager ui, autismclient.gui.f.NetHelper bounds, String title, String detail, boolean selected, Runnable run
   ) {
      boolean hovered = this.load_NetHelper.init_Z(ui.read(), ui.write()) && bounds.init_Z(ui.read(), ui.write());
      autismclient.gui.ui.UiColorScheme colors = ui.init_TableIndex().colors();
      PacketUtil.load(ui.init_GuiGraphicsExtract(), bounds, 3, selected ? colors.cardSurfaceElevated : (hovered ? colors.cardSurfaceHover : colors.cardSurface));
      if (selected) {
         PacketUtil.load(ui.init_GuiGraphicsExtract(), bounds, 3, 0, colors.accentSoft);
      }

      this.init_V_SyncManager_5(ui, title, bounds.read() + 4, bounds.write() + 1, bounds.update() - 8, colors.textPrimary);
      this.init_V_SyncManager_5(ui, detail, bounds.read() + 4, bounds.write() + 11, bounds.update() - 8, colors.textSecondary);
      int top = Math.max(bounds.write(), this.init_TextFormat_DataUti.y() + this.init_TextFormat_DataUti.contentInset());
      int end = Math.min(bounds.load(), this.init_TextFormat_DataUti.y() + this.init_TextFormat_DataUti.contentInset() + this.init_TextFormat_DataUti.viewHeight());
      if (end > top) {
         this.init_List
            .add(new SchematicLibraryOverlay.Inner_BufferHelper(autismclient.gui.f.NetHelper.init(bounds.read(), top, bounds.update(), end - top), run));
      }
   }

   private void init_V_SyncManager_4(autismclient.gui.f.SyncManager ui, String label, int x, int y, int width) {
      autismclient.gui.ui.widgets.ListRowRenderer.drawEmptyPlaceholder(ui.init_GuiGraphicsExtract(), this.init_Font, label, x, y, width);
   }

   private void init_V_SyncManager_5(autismclient.gui.f.SyncManager ui, String text, int x, int y, int width, int color) {
      autismclient.gui.f.b.k.load(
         ui.init_GuiGraphicsExtract(), this.init_Font, text, autismclient.gui.f.a.DataUtil.init, color, x, y, Math.max(0, width), false
      );
   }

   static String init_Str_J(long bytes) {
      if (bytes < 1024L) {
         return bytes + " B";
      } else {
         return bytes < 1048576L ? String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0) : String.format(Locale.ROOT, "%.1f MB", bytes / 1048576.0);
      }
   }

   @Override
   public boolean mouseClicked(double mx, double my, int button) {
      if (this.windowVisible && button == 0 && this.containsPoint(mx, my)) {
         autismclient.gui.ui.widgets.DropdownMenu popup = this.init_RefQueue();
         if (popup != null && popup.containsPopupPoint((int)mx, (int)my)) {
            popup.onMouseClicked((int)mx, (int)my, button);
            return true;
         }

         if (this.isOverHeaderCloseButton(mx, my, this.getBounds())) {
            this.setVisible(false);
            return true;
         }

         if (this.isOverHeader(mx, my)) {
            this.update_Z = true;
            this.init_D = mx - this.windowX;
            this.load_D = my - this.windowY;
            return true;
         }

         if (this.windowCollapsed) {
            return false;
         }

         int x = (int)mx;
         int y = (int)my;
         if (this.init_SchematicLibraryOv_2 == SchematicLibraryOverlay.Inner_ConfigStore.FILES) {
            autismclient.gui.ui.widgets.DropdownMenu menu = this.init_RefQueue();
            if (menu != null) {
               menu.onMouseClicked(x, y, button);
               return true;
            }

            if (this.load_RefQueue.getBounds().init_Z(x, y) || this.init_RefQueue.getBounds().init_Z(x, y)) {
               this.clearInputFocus();
               (this.load_RefQueue.getBounds().init_Z(x, y) ? this.load_RefQueue : this.init_RefQueue).onMouseClicked(x, y, button);
               return true;
            }
         }

         if (this.init_k != null && this.init_k.mouseClicked(x, y, button)) {
            return true;
         }

         this.clearInputFocus();
         if (this.init_TextFormat_DataUti != null
            && this.init_TextFormat_DataUti.scrollbar().isScrollable()
            && this.init_TextFormat_DataUti.scrollbar().containsTrack(x, y)) {
            ScrollbarMetricsHelper.Metrics bar = this.init_TextFormat_DataUti.scrollbar();
            this.run = bar.containsThumb(x, y) ? y - bar.thumbY() : bar.thumbHeight() / 2;
            this.init_CacheEntry.dragToMouseY(bar, y, this.run);
            this.write_Z = true;
            return true;
         }

         for (int i = this.init_List.size() - 1; i >= 0; i--) {
            SchematicLibraryOverlay.Inner_BufferHelper hit = this.init_List.get(i);
            if (hit.bounds().init_Z(x, y)) {
               hit.action().run();
               return true;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseScrolled(double x, double y, double amount) {
      autismclient.gui.ui.widgets.DropdownMenu menu = this.init_RefQueue();
      if (this.windowVisible && !this.windowCollapsed && menu != null && menu.containsPopupPoint((int)x, (int)y)) {
         menu.onMouseScrolled((int)x, (int)y, amount);
         return true;
      } else if (this.windowVisible && !this.windowCollapsed && this.init_TextFormat_DataUti != null && this.load_NetHelper.init_Z((int)x, (int)y)) {
         this.init_CacheEntry.scrollByWheel(amount, 42.0F, this.init_TextFormat_DataUti.maxScroll());
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
      if (this.update_Z) {
         OverlayBounds clamped = this.clampToScreen(this, new OverlayBounds((int)(x - this.init_D), (int)(y - this.load_D), this.windowWidth, this.windowHeight, this.windowVisible, this.windowCollapsed));
         this.windowX = clamped.x;
         this.windowY = clamped.y;
         return true;
      } else {
         autismclient.gui.ui.widgets.DropdownMenu menu = this.init_RefQueue();
         if (menu != null) {
            menu.onMouseDragged((int)x, (int)y, button, dx, dy);
            return true;
         } else if (this.write_Z && this.init_TextFormat_DataUti != null) {
            this.init_CacheEntry.dragToMouseY(this.init_TextFormat_DataUti.scrollbar(), y, this.run);
            return true;
         } else {
            return this.init_k != null && this.init_k.mouseDragged(x, y, button, dx, dy);
         }
      }
   }

   @Override
   public boolean mouseReleased(double x, double y, int button) {
      if (this.update_Z) {
         this.update_Z = false;
         this.saveBounds();
         return true;
      } else {
         autismclient.gui.ui.widgets.DropdownMenu menu = this.init_RefQueue();
         if (menu != null) {
            menu.onMouseReleased((int)x, (int)y, button);
            return true;
         } else if (this.write_Z) {
            this.write_Z = false;
            return true;
         } else {
            return this.init_k != null && this.init_k.mouseReleased(x, y, button);
         }
      }
   }

   @Override
   public boolean keyPressed(int key, int scan, int modifiers) {
      if (this.windowVisible && this.init_RefQueue() != null && key == 256) {
         this.update_V_2();
         return true;
      } else {
         return this.windowVisible && !this.windowCollapsed && this.init_k != null && this.init_k.keyPressed(Compat263.newGlfwKeyEvent(key, scan, modifiers));
      }
   }

   @Override
   public boolean charTyped(char value, int modifiers) {
      return this.windowVisible && !this.windowCollapsed && this.init_k != null && this.init_k.charTyped(new CharacterEvent(value));
   }

   private record Inner_BufferHelper(autismclient.gui.f.NetHelper bounds, Runnable action) {
   }

   private enum Inner_CachePool {
      EDITED("Last edited", Comparator.comparingLong(SchematicLibrary.Inner_DataUtil::modified).reversed()),
      ADDED("Date added", Comparator.comparingLong(SchematicLibrary.Inner_DataUtil::added).reversed()),
      NAME_ASC("Name A–Z", SchematicLibraryOverlay.init_Comparator),
      NAME_DESC("Name Z–A", SchematicLibraryOverlay.init_Comparator.reversed()),
      LARGE("Largest", Comparator.comparingLong(SchematicLibrary.Inner_DataUtil::bytes).reversed()),
      SMALL("Smallest", Comparator.comparingLong(SchematicLibrary.Inner_DataUtil::bytes)),
      BLOCKS("Most blocks", Comparator.comparingLong(SchematicLibrary.Inner_DataUtil::blocks).reversed());

      final String init_Str;
      final Comparator<SchematicLibrary.Inner_DataUtil> init_Comparator;

      Inner_CachePool(String label, Comparator<SchematicLibrary.Inner_DataUtil> order) {
         this.init_Str = label;
         this.init_Comparator = order.thenComparing(SchematicLibraryOverlay.init_Comparator);
      }
   }

   private enum Inner_ConfigStore {
      FILES,
      PLACEMENTS,
      CAPTURE,
      FOLDERS;
   }

   private record Inner_DataUtil(String label, autismclient.gui.f.b.CachePool.Inner_DataUtil tone, boolean enabled, Runnable run) {
   }

   private enum Inner_StreamUtil {
      ALL("All files"),
      LOADED("Loaded"),
      UNLOADED("Unloaded"),
      SCHEM("Schematic"),
      LITEMATIC("Litematic");

      final String init_Str;

      Inner_StreamUtil(String label) {
         this.init_Str = label;
      }

      boolean init(SchematicLibrary.Inner_DataUtil file, Set<String> loaded) {
         boolean var10000;
         switch (this) {
            case ALL:
               var10000 = true;
               break;
            case LOADED:
               var10000 = loaded.contains(file.file().toString());
               break;
            case UNLOADED:
               var10000 = !loaded.contains(file.file().toString());
               break;
            case SCHEM:
               var10000 = !file.name().toLowerCase(Locale.ROOT).endsWith(".litematic");
               break;
            case LITEMATIC:
               var10000 = file.name().toLowerCase(Locale.ROOT).endsWith(".litematic");
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      }
   }
}
