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
   private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
   private static final Comparator<SchematicLibrary.SchematicEntry> ENTRY_ORDER = Comparator.comparing(SchematicLibrary.SchematicEntry::name, String.CASE_INSENSITIVE_ORDER)
      .thenComparing(entry -> entry.file().toString());
   private final SchematicWorkspace workspace;
   private final Minecraft mc = Minecraft.getInstance();
   private final Font font = this.mc.font;
   private final List<SchematicLibraryOverlay.ClickRegion> init_List = new ArrayList<>();
   private final autismclient.gui.ui.widgets.ScrollOffsetController scroll = new autismclient.gui.ui.widgets.ScrollOffsetController();
   private final NumberFormat numberFormat = NumberFormat.getIntegerInstance();
   private SchematicLibraryOverlay.LibraryTab tab = SchematicLibraryOverlay.LibraryTab.FILES;
   private autismclient.util.ThemedTextInputBox textInput;
   private autismclient.gui.f.NetHelper windowRect;
   private autismclient.gui.f.NetHelper listRect;
   private autismclient.gui.ui.widgets.ScrollListLayout.ListLayout listLayout;
   private boolean write_Z;
   private boolean update_Z;
   private boolean sync_Z = true;
   private boolean flush_Z;
   private double dragOffsetX;
   private double dragOffsetY;
   private int run;
   private String query = "";
   private Path directory;
   private List<SchematicLibrary.SchematicEntry> cachedSource = List.of();
   private List<SchematicLibrary.SchematicEntry> cachedEntries = List.of();
   private String cachedQuery;
   private SchematicLibraryOverlay.SortOrder sort;
   private SchematicLibraryOverlay.SortOrder cachedSort;
   private SchematicLibraryOverlay.LibraryFilter filter;
   private SchematicLibraryOverlay.LibraryFilter cachedFilter;
   private Set<String> loaded;
   private final autismclient.gui.ui.widgets.DropdownMenu sortMenu;
   private final autismclient.gui.ui.widgets.DropdownMenu filterMenu;

   SchematicLibraryOverlay(SchematicWorkspace workspace) {
      super("schematic-library", 270, 132);
      this.sort = SchematicLibraryOverlay.SortOrder.EDITED;
      this.filter = SchematicLibraryOverlay.LibraryFilter.ALL;
      this.loaded = Set.of();
      this.sortMenu = new autismclient.gui.ui.widgets.DropdownMenu(
         null, Arrays.stream(SchematicLibraryOverlay.SortOrder.values()).map(v -> v.init_Str).toList(), this.sort.init_Str, label -> {
            for (SchematicLibraryOverlay.SortOrder value : SchematicLibraryOverlay.SortOrder.values()) {
               if (value.init_Str.equals(label)) {
                  this.sort = value;
               }
            }

            this.scroll.setOffsetUnclamped(0);
         }
      );
      this.filterMenu = new autismclient.gui.ui.widgets.DropdownMenu(
         null,
         Arrays.stream(SchematicLibraryOverlay.LibraryFilter.values()).map(v -> v.init_Str).toList(),
         this.filter.init_Str,
         label -> {
            for (SchematicLibraryOverlay.LibraryFilter value : SchematicLibraryOverlay.LibraryFilter.values()) {
               if (value.init_Str.equals(label)) {
                  this.filter = value;
               }
            }

            this.scroll.setOffsetUnclamped(0);
         }
      );
      this.workspace = workspace;
      this.windowX = 100;
      this.windowY = 38;
      this.loadBounds();
      this.write_V_2();
   }

   void init_V_Str(String name) {
      this.closeMenus();
      if (!name.equals("SORT") && !name.equals("FILTER")) {
         this.init_V_SchematicLibraryOv(SchematicLibraryOverlay.LibraryTab.valueOf(name));
      } else {
         this.init_V_SchematicLibraryOv(SchematicLibraryOverlay.LibraryTab.FILES);
         (name.equals("SORT") ? this.sortMenu : this.filterMenu).open();
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
      return this.heightForRows(1);
   }

   @Override
   protected int getResizeMinHeight() {
      return this.heightForRows(Math.clamp(this.rowCount(), 1, 3));
   }

   private int rowCount() {
      int var10000;
      switch (this.tab) {
         case FILES:
            var10000 = this.visibleEntries().size();
            break;
         case PLACEMENTS:
            var10000 = this.workspace.placements().size();
            break;
         case CAPTURE:
            var10000 = 0;
            break;
         case FOLDERS:
            var10000 = this.workspace.library.roots.size();
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private int libraryHeaderHeight() {
      return 20 + (this.workspace.hasActiveBuild() ? 16 : 0) + (this.workspace.getProgress() >= 0 ? 13 : 0);
   }

   private int captureHeight() {
      int bodyTop = 34;
      int contentBottom = bodyTop + 48 + 14;
      return contentBottom + 4 + (this.workspace.hasActiveBuild() ? 16 : 0) + (this.workspace.getProgress() >= 0 ? 13 : 0);
   }

   private int heightForRows(int rows) {
      int bodyTop = 34;
      if (this.tab == SchematicLibraryOverlay.LibraryTab.CAPTURE) {
         return this.captureHeight();
      }

      byte var10000;
      switch (this.tab) {
         case FILES:
            var10000 = 16;
            break;
         case PLACEMENTS:
            var10000 = (byte)(this.workspace.isLocalWorld() ? 17 : 0);
            break;
         default:
            var10000 = 0;
      }

      int beforeList = var10000;
      return bodyTop + beforeList + 4 + 21 * rows + this.libraryHeaderHeight();
   }

   @Override
   public boolean hasFocusedTextInput() {
      return this.textInput != null && this.textInput.isFocused();
   }

   @Override
   public void clearInputFocus() {
      if (this.textInput != null) {
         this.textInput.setFocused(false);
      }
   }

   @Override
   public void setVisible(boolean value) {
      if (!value) {
         this.closeMenus();
         this.clearInputFocus();
         this.update_Z = this.write_Z = false;
      }

      super.setVisible(value);
   }

   private void write_V_2() {
      if (this.mc != null) {
         autismclient.gui.ui.widgets.TextInputWidget.clearGlobalFocus();
         this.textInput = null;
         if (this.tab == SchematicLibraryOverlay.LibraryTab.FILES
            || this.tab == SchematicLibraryOverlay.LibraryTab.CAPTURE) {
            this.textInput = new autismclient.util.ThemedTextInputBox(this.mc, this.font, 0, 0, 100, 13, false);
            this.textInput.setMaxLength(100);
            this.textInput
               .setPlaceholder(
                  Component.literal(
                     this.tab == SchematicLibraryOverlay.LibraryTab.FILES ? "Search schematics" : "File name (optional)"
                  )
               );
            this.textInput.setValue(this.tab == SchematicLibraryOverlay.LibraryTab.FILES ? this.query : this.workspace.captureName);
            this.textInput.setChangedListener(value -> {
               if (this.tab == SchematicLibraryOverlay.LibraryTab.FILES) {
                  this.query = value;
                  this.scroll.setOffsetUnclamped(0);
               } else {
                  this.workspace.captureName = value;
               }
            });
         }
      }
   }

   private void init_V_SchematicLibraryOv(SchematicLibraryOverlay.LibraryTab value) {
      if (this.tab != value) {
         this.tab = value;
         this.closeMenus();
         this.scroll.setOffsetUnclamped(0);
         this.write_Z = false;
         this.write_V_2();
      }
   }

   @Override
   public void render(GuiGraphicsExtractor graphics, int mx, int my, float delta) {
      if (this.windowVisible || this.isClosing()) {
         this.init_List.clear();
         this.listLayout = null;
         this.listRect = null;
         int auto = this.heightForRows(Math.clamp(this.rowCount(), 1, 5));
         this.windowWidth = this.resolveWindowWidth(270, this.getMinWidth());
         this.windowHeight = this.tab == SchematicLibraryOverlay.LibraryTab.CAPTURE
            ? Math.min(auto, getMaxScreenWindowHeight())
            : this.resolveWindowHeight(auto, this.getResizeMinHeight());
         OverlayBounds clamped = this.clampCurrentBounds(this);
         this.windowX = clamped.x;
         this.windowY = clamped.y;
         this.windowWidth = clamped.width;
         this.windowHeight = clamped.height;
         this.windowRect = autismclient.gui.f.NetHelper.init(this.windowX, this.windowY, this.windowWidth, this.windowHeight);
         this.renderWindowFrame(graphics, mx, my, this.getBounds(), "Schematic Library", this.windowCollapsed, this.update_Z);
         if (!this.isVisuallyCollapsed(this.windowCollapsed) && this.windowRect.update() >= 190 && this.windowRect.sync() >= 27) {
            boolean clipped = this.beginBodyClip(graphics, this.getBounds(), this.windowCollapsed);

            try {
               int width = this.windowRect.update();
               autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(graphics, this.font, mx, my);
               autismclient.gui.f.NetHelper tabs = autismclient.gui.f.NetHelper.init(
                  this.windowRect.read() + 4, this.windowRect.write() + 14 + 4, width - 8, 13
               );
               String[] names = new String[]{"Files", "Placements", "Capture", "Folders"};
               int tabX = tabs.read();

               for (SchematicLibraryOverlay.LibraryTab value : SchematicLibraryOverlay.LibraryTab.values()) {
                  String label = names[value.ordinal()];
                  int tabWidth = Math.min((tabs.update() - 9) / 4, ui.init_PoolEntry().measureWidth(label) + 12);
                  autismclient.gui.f.NetHelper at = autismclient.gui.f.NetHelper.init(tabX, tabs.write(), tabWidth, 13);
                  autismclient.gui.ui.widgets.TabStripRenderer.drawTab(ui, at, label, value == this.tab);
                  this.init_List.add(new SchematicLibraryOverlay.ClickRegion(at, () -> this.init_V_SchematicLibraryOv(value)));
                  tabX = at.init() + 3;
               }

               int x = tabs.read();
               int y = tabs.load() + 3;
               int w = tabs.update();
               int bottom = this.windowRect.load() - this.libraryHeaderHeight();
               switch (this.tab) {
                  case FILES:
                     this.init_V_SyncManager(ui, x, y, w, bottom, delta);
                     break;
                  case PLACEMENTS:
                     this.drawPlacements(ui, x, y, w, bottom);
                     break;
                  case CAPTURE:
                     this.load(ui, x, y, w, bottom, delta);
                     break;
                  case FOLDERS:
                     this.load(ui, x, y, w, bottom);
               }

               if (this.workspace.hasActiveBuild()) {
                  int buildY = bottom + 6 + 13;
                  String label = "Stop Build";
                  int buttonWidth = ui.init_PoolEntry().measureWidth(label) + 12;
                  this.drawText(ui, this.workspace.activeBuildLabel(), x, buildY + 2, w - buttonWidth - 3, ui.init_TableIndex().colors().textSecondary);
                  this.drawButton(
                     ui,
                     autismclient.gui.f.NetHelper.init(x + w - buttonWidth, buildY, buttonWidth, 13),
                     label,
                     autismclient.gui.f.b.CachePool.Inner_DataUtil.update,
                     true,
                     this.workspace::stopBuild
                  );
               }

               int progress = this.workspace.getProgress();
               if (progress >= 0) {
                  SchematicRecording recording = this.workspace.activeRecording();
                  String statusText = recording != null && !recording.putAt_Z ? recording.putAt_Str() : this.workspace.getStatus();
                  String pctText = progress + "%";
                  int pctWidth = ui.init_PoolEntry().measureWidth(pctText);
                  int barWidth = Math.min(90, w / 3);
                  int progressY = this.windowRect.load() - 4 - 7;
                  int pctX = this.windowRect.init() - 10 - 3 - pctWidth;
                  int barX = pctX - 3 - barWidth;
                  this.drawText(ui, statusText, x, progressY - 1, barX - x - 3, ui.init_TableIndex().colors().textSecondary);
                  autismclient.gui.ui.widgets.ProgressBarRenderer.draw(ui, autismclient.gui.f.NetHelper.init(barX, progressY + 2, barWidth, 4), progress / 100.0);
                  this.drawText(ui, pctText, pctX, progressY - 1, pctWidth, ui.init_TableIndex().colors().textPrimary);
               }
            } finally {
               this.endBodyClip(graphics, clipped);
            }

            autismclient.gui.ui.widgets.DropdownMenu var231 = this.openDropdown();
            if (this.tab == SchematicLibraryOverlay.LibraryTab.FILES && var231 != null) {
               graphics.nextStratum();
               var231.render(autismclient.gui.f.UiHelper.init(graphics, this.font, mx, my));
            }
         }
      }
   }

   private void init_V_SyncManager(autismclient.gui.f.SyncManager ui, int x, int y, int width, int bottom, float delta) {
      int refreshWidth = ui.init_PoolEntry().measureWidth("Refresh") + 10;
      int sortWidth = Math.min(74, ui.init_PoolEntry().measureWidth(this.sort.init_Str) + 18);
      int filterWidth = Math.min(63, ui.init_PoolEntry().measureWidth(this.filter.init_Str) + 18);
      int refreshX = x + width - refreshWidth;
      int sortX = refreshX - 3 - sortWidth;
      int filterX = sortX - 3 - filterWidth;
      this.init_V_SyncManager(ui, x, y, Math.max(24, filterX - 3 - x), delta);
      this.init(ui, this.filterMenu, autismclient.gui.f.NetHelper.init(filterX, y, filterWidth, 13), this.filter.init_Str);
      this.init(ui, this.sortMenu, autismclient.gui.f.NetHelper.init(sortX, y, sortWidth, 13), this.sort.init_Str);
      this.drawButton(
         ui,
         autismclient.gui.f.NetHelper.init(refreshX, y, refreshWidth, 13),
         "Refresh",
         autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
         this.workspace.isLibraryReady(),
         this.workspace::refresh
      );
      List<SchematicLibrary.SchematicEntry> entries = this.visibleEntries();
      this.init(
         ui,
         x,
         y + 13 + 3,
         width,
         Math.max(4, bottom - y - 13 - 3),
         entries.size(),
         (index, row) -> {
            SchematicLibrary.SchematicEntry e = entries.get(index);
            String blocks = e.blocks() >= 0L
               ? this.numberFormat.format(e.blocks()) + " blocks"
               : (e.error().isEmpty() ? "Counting blocks…" : "Unreadable");
            boolean added = this.sort == SchematicLibraryOverlay.SortOrder.ADDED && e.added() > 0L;
            String date = (added ? "Added " : "Edited ") + DATE_FORMAT.format(Instant.ofEpochMilli(added ? e.added() : e.modified()));
            this.drawRow(
               ui,
               row,
               e.name(),
               blocks + " · " + formatBytes(e.bytes()) + " · " + date + (this.workspace.placementForFile(e) != null ? " · Loaded" : ""),
               this.workspace.selectedFileId.equals(e.id()),
               () -> this.workspace.selectedFileId = e.id()
            );
         }
      );
      if (entries.isEmpty()) {
         this.drawEmptyPlaceholder(ui, "No schematic files", x, y + 13 + 12, width);
      }

      SchematicLibrary.SchematicEntry file = entries.stream().filter(e -> e.id().equals(this.workspace.selectedFileId)).findFirst().orElse(null);
      SchematicLibrary.Placement active = this.workspace.placementForFile(file);
      this.drawActions(
         ui,
         x,
         bottom + 3,
         width,
         List.of(
            new SchematicLibraryOverlay.ActionButton(
               active == null ? "Load" : "Unload",
               autismclient.gui.f.b.CachePool.Inner_DataUtil.read,
               this.workspace.isLibraryReady()
                  && this.workspace.isInWorld()
                  && (active != null ? this.workspace.canModify(active) : !this.workspace.isBusy() && file != null && file.error().isEmpty()),
               () -> {
                  if (active == null) {
                     this.workspace.placeFile(file);
                     this.init_V_SchematicLibraryOv(SchematicLibraryOverlay.LibraryTab.PLACEMENTS);
                  } else {
                     this.workspace.toggleShown(active);
                  }
               }
            ),
            new SchematicLibraryOverlay.ActionButton(
               "Rename",
               autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
               file != null && !this.workspace.isBusy(),
               () -> this.workspace.renameFile(file)
            ),
            new SchematicLibraryOverlay.ActionButton(
               "Open folder",
               autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
               this.workspace.isLibraryReady(),
               () -> this.workspace.openPath(this.workspace.library.defaultRoot)
            )
         )
      );
   }

   private List<SchematicLibrary.SchematicEntry> visibleEntries() {
      List<SchematicLibrary.SchematicEntry> current = this.workspace.library.entries;
      Set<String> loaded = Set.of();
      if (this.filter == SchematicLibraryOverlay.LibraryFilter.LOADED
         || this.filter == SchematicLibraryOverlay.LibraryFilter.UNLOADED) {
         loaded = new HashSet<>();

         for (SchematicLibrary.Placement placement : this.workspace.placements()) {
            if (placement.visible()) {
               loaded.add(placement.file());
            }
         }
      }

      if (current != this.cachedSource
         || !this.query.equals(this.cachedQuery)
         || this.sort != this.cachedSort
         || this.filter != this.cachedFilter
         || !loaded.equals(this.loaded)) {
         this.cachedSource = current;
         this.cachedQuery = this.query;
         this.cachedSort = this.sort;
         this.cachedFilter = this.filter;
         this.loaded = loaded;
         String[] words = this.query.strip().toLowerCase(Locale.ROOT).split("\\s+");
         this.cachedEntries = current.stream().filter(entry -> {
            if (!this.filter.init(entry, this.loaded)) {
               return false;
            }

            String name = entry.name().toLowerCase(Locale.ROOT);

            for (String word : words) {
               if (!name.contains(word)) {
                  return false;
               }
            }

            return true;
         }).sorted(this.sort.init_Comparator).toList();
      }

      return this.cachedEntries;
   }

   private void init(autismclient.gui.f.SyncManager ui, autismclient.gui.ui.widgets.DropdownMenu dropdown, autismclient.gui.f.NetHelper bounds, String label) {
      dropdown.setBounds(bounds);
      autismclient.gui.ui.widgets.DropdownMenu.renderTrigger(ui, bounds, label, bounds.init_Z(ui.read(), ui.write()), dropdown.isOpen());
   }

   private autismclient.gui.ui.widgets.DropdownMenu openDropdown() {
      if (this.sortMenu.isOpen()) {
         return this.sortMenu;
      } else {
         return this.filterMenu.isOpen() ? this.filterMenu : null;
      }
   }

   private void closeMenus() {
      this.sortMenu.close();
      this.filterMenu.close();
   }

   @Override
   public boolean containsPoint(double x, double y) {
      autismclient.gui.ui.widgets.DropdownMenu menu = this.openDropdown();
      return super.containsPoint(x, y) || this.windowVisible && menu != null && menu.containsPopupPoint((int)x, (int)y);
   }

   private void drawPlacements(autismclient.gui.f.SyncManager ui, int x, int y, int width, int bottom) {
      List<SchematicLibrary.Placement> placements = this.workspace.placements();
      SchematicLibrary.Placement picked = this.workspace.placementById(this.workspace.selectedPlacementId);
      boolean local = this.workspace.isLocalWorld();
      int listBottom = bottom - (local ? 17 : 0);
      this.init(ui, x, y, width, Math.max(4, listBottom - y), placements.size(), (index, row) -> {
         SchematicLibrary.Placement p = placements.get(index);
         String detail = p.x() + ", " + p.y() + ", " + p.z() + "  ·  " + p.rotation() + "°  ·  " + this.workspace.placementStatus(p);
         this.drawRow(ui, row, SchematicWorkspace.displayName(p), detail, p.id().equals(this.workspace.selectedPlacementId), () -> this.workspace.selectedPlacementId = p.id());
      });
      if (placements.isEmpty()) {
         this.drawEmptyPlaceholder(ui, this.workspace.isInWorld() ? "Load a file to place its hologram" : "Join a world to place schematics", x, y + 8, width);
      }

      if (local) {
         int airWidth = Math.min(width / 2, ui.init_PoolEntry().measureWidth("Paste air") + 37);
         int entityWidth = Math.min(width - airWidth - 3, ui.init_PoolEntry().measureWidth("Entities") + 37);
         this.drawToggle(
            ui,
            autismclient.gui.f.NetHelper.init(x, listBottom + 3, airWidth, 14),
            "Paste air",
            this.sync_Z,
            !this.workspace.isBusy(),
            () -> this.sync_Z = !this.sync_Z
         );
         this.drawToggle(
            ui,
            autismclient.gui.f.NetHelper.init(x + airWidth + 6, listBottom + 3, entityWidth, 14),
            "Entities",
            this.flush_Z,
            !this.workspace.isBusy(),
            () -> this.flush_Z = !this.flush_Z
         );
      }

      if (!this.workspace.isBusy() || this.workspace.canModify(picked)) {
         boolean available = this.workspace.isLoaded(picked);
         boolean paused = picked != null && !picked.paste().isEmpty();
         ArrayList<SchematicLibraryOverlay.ActionButton> actions = new ArrayList<>();
         actions.add(
            new SchematicLibraryOverlay.ActionButton(
               picked != null && picked.visible() ? "Unload" : "Show",
               autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
               this.workspace.canModify(picked) && !paused,
               () -> this.workspace.toggleShown(picked)
            )
         );
         actions.add(
            new SchematicLibraryOverlay.ActionButton(
               "Move", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, this.workspace.canMove(picked), () -> this.workspace.write(picked)
            )
         );
         actions.add(
            new SchematicLibraryOverlay.ActionButton(
               "Build",
               autismclient.gui.f.b.CachePool.Inner_DataUtil.read,
               available && !paused && !this.workspace.hasActiveBuild() && !this.workspace.isBusy(),
               () -> this.workspace.update(picked)
            )
         );
         if (local) {
            actions.add(
               new SchematicLibraryOverlay.ActionButton(
                  paused ? "Resume" : "Paste",
                  autismclient.gui.f.b.CachePool.Inner_DataUtil.write,
                  available && !this.workspace.hasActiveBuild() && !this.workspace.isBusy(),
                  () -> this.workspace.init(picked, this.sync_Z, this.flush_Z)
               )
            );
         }

         actions.add(
            new SchematicLibraryOverlay.ActionButton(
               "Rename",
               autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
               picked != null && !this.workspace.isBusy(),
               () -> this.workspace.renamePlacement(picked)
            )
         );
         actions.add(
            new SchematicLibraryOverlay.ActionButton(
               "Remove", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, this.workspace.canModify(picked), () -> this.workspace.removePlacement(picked)
            )
         );
         this.drawActions(ui, x, bottom + 3, width, actions);
      } else if (!this.workspace.sync_Z()) {
         this.drawActions(
            ui,
            x,
            bottom + 3,
            width,
            List.of(
               new SchematicLibraryOverlay.ActionButton(
                  this.workspace.isPasting() ? "Terminate" : "Cancel", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, true, this.workspace::cancelOperation
               )
            )
         );
      }
   }

   private void load(autismclient.gui.f.SyncManager ui, int x, int y, int width, int bottom, float delta) {
      this.init_V_SyncManager(ui, x, y, width, delta);
      boolean busy = this.workspace.isBusy();
      int step = 16;
      int rowY = y + step;
      this.drawActions(
         ui,
         x,
         rowY,
         width,
         List.of(
            new SchematicLibraryOverlay.ActionButton(
               "Select corners",
               autismclient.gui.f.b.CachePool.Inner_DataUtil.read,
               this.workspace.isInWorld() && !busy,
               this.workspace::selectArea
            ),
            new SchematicLibraryOverlay.ActionButton(
               "Save area",
               autismclient.gui.f.b.CachePool.Inner_DataUtil.read,
               this.workspace.isLibraryReady() && this.workspace.isInWorld() && this.workspace.selectedArea != null && !busy,
               () -> this.workspace.startCapture(false, this.flush_Z)
            )
         )
      );
      int row2Y = rowY + step;
      SchematicRecording recording = this.workspace.activeRecording();
      if (recording != null && recording.putAt_Z()) {
         this.drawActions(
            ui,
            x,
            row2Y,
            width,
            List.of(
               new SchematicLibraryOverlay.ActionButton(
                  "Stop & save", autismclient.gui.f.b.CachePool.Inner_DataUtil.update, true, this.workspace::stopAndSaveRecording
               ),
               new SchematicLibraryOverlay.ActionButton(
                  "Cancel", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, true, this.workspace::cancelOperation
               )
            )
         );
      } else if (busy) {
         this.drawActions(
            ui,
            x,
            row2Y,
            width,
            List.of(
               new SchematicLibraryOverlay.ActionButton(
                  this.workspace.isPasting() ? "Terminate" : "Cancel", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, true, this.workspace::cancelOperation
               )
            )
         );
      } else {
         this.drawActions(
            ui,
            x,
            row2Y,
            width,
            List.of(
               new SchematicLibraryOverlay.ActionButton(
                  "Record chunks",
                  autismclient.gui.f.b.CachePool.Inner_DataUtil.write,
                  this.workspace.isLibraryReady() && this.workspace.isInWorld(),
                  () -> this.workspace.startRecording(this.flush_Z)
               ),
               new SchematicLibraryOverlay.ActionButton(
                  "Save loaded chunks",
                  autismclient.gui.f.b.CachePool.Inner_DataUtil.write,
                  this.workspace.isLibraryReady() && this.workspace.isInWorld(),
                  () -> this.workspace.startCapture(true, this.flush_Z)
               )
            )
         );
      }

      this.drawToggle(
         ui,
         autismclient.gui.f.NetHelper.init(x, row2Y + step, Math.min(width, ui.init_PoolEntry().measureWidth("Capture entities") + 37), 14),
         "Capture entities",
         this.flush_Z,
         !busy,
         () -> this.flush_Z = !this.flush_Z
      );
   }

   private void load(autismclient.gui.f.SyncManager ui, int x, int y, int width, int bottom) {
      List<Path> folders = this.workspace.library.roots;
      this.init(
         ui,
         x,
         y,
         width,
         Math.max(4, bottom - y),
         folders.size(),
         (index, row) -> {
            Path directory = folders.get(index);
            boolean local = directory.equals(this.workspace.library.defaultRoot);
            this.drawRow(
               ui,
               row,
               local ? "Client schematics" : (directory.getFileName() == null ? directory.toString() : directory.getFileName().toString()),
               directory.toString(),
               directory.equals(this.directory),
               () -> this.directory = directory
            );
         }
      );
      boolean selected = this.directory != null && folders.contains(this.directory);
      this.drawActions(
         ui,
         x,
         bottom + 3,
         width,
         List.of(
            new SchematicLibraryOverlay.ActionButton(
               "Add folder", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, this.workspace.isLibraryReady(), this.workspace::addFolder
            ),
            new SchematicLibraryOverlay.ActionButton(
               "Open", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, selected, () -> this.workspace.openPath(this.directory)
            ),
            new SchematicLibraryOverlay.ActionButton(
               "Remove", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, selected && !this.directory.equals(this.workspace.library.defaultRoot), () -> {
                  this.workspace.removeFolder(this.directory);
                  this.directory = null;
               }
            )
         )
      );
   }

   private void drawActions(autismclient.gui.f.SyncManager ui, int x, int y, int width, List<SchematicLibraryOverlay.ActionButton> actions) {
      int available = width - (actions.size() - 1) * 3;
      int total = actions.stream().mapToInt(actionx -> ui.init_PoolEntry().measureWidth(actionx.label()) + 10).sum();

      for (SchematicLibraryOverlay.ActionButton action : actions) {
         int natural = ui.init_PoolEntry().measureWidth(action.label()) + 10;
         int buttonWidth = Math.max(1, total <= available ? natural : natural * available / total);
         this.drawButton(ui, autismclient.gui.f.NetHelper.init(x, y, buttonWidth, 13), action.label(), action.tone(), action.enabled(), action.run());
         x += buttonWidth + 3;
      }
   }

   private void drawButton(
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
         this.init_List.add(new SchematicLibraryOverlay.ClickRegion(bounds, run));
      }
   }

   private void drawToggle(
      autismclient.gui.f.SyncManager ui, autismclient.gui.f.NetHelper bounds, String text, boolean value, boolean enabled, Runnable run
   ) {
      autismclient.gui.ui.widgets.ToggleSwitchRenderer.init(ui, bounds, text, value, enabled && bounds.init_Z(ui.read(), ui.write()), "schematic/" + text);
      if (enabled) {
         this.init_List.add(new SchematicLibraryOverlay.ClickRegion(bounds, run));
      }
   }

   private void init_V_SyncManager(autismclient.gui.f.SyncManager ui, int x, int y, int width, float delta) {
      if (this.textInput != null) {
         this.textInput.setX(x);
         this.textInput.setY(y);
         this.textInput.setWidth(width);
         this.textInput.render(ui.init_GuiGraphicsExtract(), ui.read(), ui.write(), delta);
      }
   }

   private void init(autismclient.gui.f.SyncManager ui, int x, int y, int w, int h, int count, BiConsumer<Integer, autismclient.gui.f.NetHelper> render) {
      this.listLayout = autismclient.gui.ui.widgets.ScrollListLayout.compute(x, y, w, h, count, 20, 21, this.scroll.getOffset(), 6, 4, 2, 4, true);
      this.listRect = autismclient.gui.f.NetHelper.init(x, y, w, h);
      this.scroll.setOffset(this.listLayout.clampedScroll(), this.listLayout.maxScroll());
      this.listLayout.drawFrame(ui.init_GuiGraphicsExtract(), false);
      if (this.listLayout.viewHeight() >= 4) {
         this.listLayout.pushContentClip(ui.init_GuiGraphicsExtract());

         try {
            this.listLayout
               .forEachVisibleRow(
                  count,
                  (index, top) -> render.accept(
                     index, autismclient.gui.f.NetHelper.init(x + this.listLayout.contentInset(), top, this.listLayout.contentWidth(), 20)
                  )
               );
         } finally {
            this.listLayout.popContentClip(ui.init_GuiGraphicsExtract());
         }

         this.listLayout.renderScrollbar(ui.init_GuiGraphicsExtract(), ui.read(), ui.write(), this.write_Z);
      }
   }

   private void drawRow(
      autismclient.gui.f.SyncManager ui, autismclient.gui.f.NetHelper bounds, String title, String detail, boolean selected, Runnable run
   ) {
      boolean hovered = this.listRect.init_Z(ui.read(), ui.write()) && bounds.init_Z(ui.read(), ui.write());
      autismclient.gui.ui.UiColorScheme colors = ui.init_TableIndex().colors();
      PacketUtil.load(ui.init_GuiGraphicsExtract(), bounds, 3, selected ? colors.cardSurfaceElevated : (hovered ? colors.cardSurfaceHover : colors.cardSurface));
      if (selected) {
         PacketUtil.load(ui.init_GuiGraphicsExtract(), bounds, 3, 0, colors.accentSoft);
      }

      this.drawText(ui, title, bounds.read() + 4, bounds.write() + 1, bounds.update() - 8, colors.textPrimary);
      this.drawText(ui, detail, bounds.read() + 4, bounds.write() + 11, bounds.update() - 8, colors.textSecondary);
      int top = Math.max(bounds.write(), this.listLayout.y() + this.listLayout.contentInset());
      int end = Math.min(bounds.load(), this.listLayout.y() + this.listLayout.contentInset() + this.listLayout.viewHeight());
      if (end > top) {
         this.init_List
            .add(new SchematicLibraryOverlay.ClickRegion(autismclient.gui.f.NetHelper.init(bounds.read(), top, bounds.update(), end - top), run));
      }
   }

   private void drawEmptyPlaceholder(autismclient.gui.f.SyncManager ui, String label, int x, int y, int width) {
      autismclient.gui.ui.widgets.ListRowRenderer.drawEmptyPlaceholder(ui.init_GuiGraphicsExtract(), this.font, label, x, y, width);
   }

   private void drawText(autismclient.gui.f.SyncManager ui, String text, int x, int y, int width, int color) {
      autismclient.gui.f.b.k.load(
         ui.init_GuiGraphicsExtract(), this.font, text, autismclient.gui.f.a.DataUtil.init, color, x, y, Math.max(0, width), false
      );
   }

   static String formatBytes(long bytes) {
      if (bytes < 1024L) {
         return bytes + " B";
      } else {
         return bytes < 1048576L ? String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0) : String.format(Locale.ROOT, "%.1f MB", bytes / 1048576.0);
      }
   }

   @Override
   public boolean mouseClicked(double mx, double my, int button) {
      if (this.windowVisible && button == 0 && this.containsPoint(mx, my)) {
         autismclient.gui.ui.widgets.DropdownMenu popup = this.openDropdown();
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
            this.dragOffsetX = mx - this.windowX;
            this.dragOffsetY = my - this.windowY;
            return true;
         }

         if (this.windowCollapsed) {
            return false;
         }

         int x = (int)mx;
         int y = (int)my;
         if (this.tab == SchematicLibraryOverlay.LibraryTab.FILES) {
            autismclient.gui.ui.widgets.DropdownMenu menu = this.openDropdown();
            if (menu != null) {
               menu.onMouseClicked(x, y, button);
               return true;
            }

            if (this.filterMenu.getBounds().init_Z(x, y) || this.sortMenu.getBounds().init_Z(x, y)) {
               this.clearInputFocus();
               (this.filterMenu.getBounds().init_Z(x, y) ? this.filterMenu : this.sortMenu).onMouseClicked(x, y, button);
               return true;
            }
         }

         if (this.textInput != null && this.textInput.mouseClicked(x, y, button)) {
            return true;
         }

         this.clearInputFocus();
         if (this.listLayout != null
            && this.listLayout.scrollbar().isScrollable()
            && this.listLayout.scrollbar().containsTrack(x, y)) {
            ScrollbarMetricsHelper.Metrics bar = this.listLayout.scrollbar();
            this.run = bar.containsThumb(x, y) ? y - bar.thumbY() : bar.thumbHeight() / 2;
            this.scroll.dragToMouseY(bar, y, this.run);
            this.write_Z = true;
            return true;
         }

         for (int i = this.init_List.size() - 1; i >= 0; i--) {
            SchematicLibraryOverlay.ClickRegion hit = this.init_List.get(i);
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
      autismclient.gui.ui.widgets.DropdownMenu menu = this.openDropdown();
      if (this.windowVisible && !this.windowCollapsed && menu != null && menu.containsPopupPoint((int)x, (int)y)) {
         menu.onMouseScrolled((int)x, (int)y, amount);
         return true;
      } else if (this.windowVisible && !this.windowCollapsed && this.listLayout != null && this.listRect.init_Z((int)x, (int)y)) {
         this.scroll.scrollByWheel(amount, 42.0F, this.listLayout.maxScroll());
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
      if (this.update_Z) {
         OverlayBounds clamped = this.clampToScreen(this, new OverlayBounds((int)(x - this.dragOffsetX), (int)(y - this.dragOffsetY), this.windowWidth, this.windowHeight, this.windowVisible, this.windowCollapsed));
         this.windowX = clamped.x;
         this.windowY = clamped.y;
         return true;
      } else {
         autismclient.gui.ui.widgets.DropdownMenu menu = this.openDropdown();
         if (menu != null) {
            menu.onMouseDragged((int)x, (int)y, button, dx, dy);
            return true;
         } else if (this.write_Z && this.listLayout != null) {
            this.scroll.dragToMouseY(this.listLayout.scrollbar(), y, this.run);
            return true;
         } else {
            return this.textInput != null && this.textInput.mouseDragged(x, y, button, dx, dy);
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
         autismclient.gui.ui.widgets.DropdownMenu menu = this.openDropdown();
         if (menu != null) {
            menu.onMouseReleased((int)x, (int)y, button);
            return true;
         } else if (this.write_Z) {
            this.write_Z = false;
            return true;
         } else {
            return this.textInput != null && this.textInput.mouseReleased(x, y, button);
         }
      }
   }

   @Override
   public boolean keyPressed(int key, int scan, int modifiers) {
      if (this.windowVisible && this.openDropdown() != null && key == 256) {
         this.closeMenus();
         return true;
      } else {
         return this.windowVisible && !this.windowCollapsed && this.textInput != null && this.textInput.keyPressed(Compat263.newGlfwKeyEvent(key, scan, modifiers));
      }
   }

   @Override
   public boolean charTyped(char value, int modifiers) {
      return this.windowVisible && !this.windowCollapsed && this.textInput != null && this.textInput.charTyped(new CharacterEvent(value));
   }

   private record ClickRegion(autismclient.gui.f.NetHelper bounds, Runnable action) {
   }

   private enum SortOrder {
      EDITED("Last edited", Comparator.comparingLong(SchematicLibrary.SchematicEntry::modified).reversed()),
      ADDED("Date added", Comparator.comparingLong(SchematicLibrary.SchematicEntry::added).reversed()),
      NAME_ASC("Name A–Z", SchematicLibraryOverlay.ENTRY_ORDER),
      NAME_DESC("Name Z–A", SchematicLibraryOverlay.ENTRY_ORDER.reversed()),
      LARGE("Largest", Comparator.comparingLong(SchematicLibrary.SchematicEntry::bytes).reversed()),
      SMALL("Smallest", Comparator.comparingLong(SchematicLibrary.SchematicEntry::bytes)),
      BLOCKS("Most blocks", Comparator.comparingLong(SchematicLibrary.SchematicEntry::blocks).reversed());

      final String init_Str;
      final Comparator<SchematicLibrary.SchematicEntry> init_Comparator;

      SortOrder(String label, Comparator<SchematicLibrary.SchematicEntry> order) {
         this.init_Str = label;
         this.init_Comparator = order.thenComparing(SchematicLibraryOverlay.ENTRY_ORDER);
      }
   }

   private enum LibraryTab {
      FILES,
      PLACEMENTS,
      CAPTURE,
      FOLDERS;
   }

   private record ActionButton(String label, autismclient.gui.f.b.CachePool.Inner_DataUtil tone, boolean enabled, Runnable run) {
   }

   private enum LibraryFilter {
      ALL("All files"),
      LOADED("Loaded"),
      UNLOADED("Unloaded"),
      SCHEM("Schematic"),
      LITEMATIC("Litematic");

      final String init_Str;

      LibraryFilter(String label) {
         this.init_Str = label;
      }

      boolean init(SchematicLibrary.SchematicEntry file, Set<String> loaded) {
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
