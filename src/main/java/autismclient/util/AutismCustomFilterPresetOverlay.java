package autismclient.util;

import autismclient.gui.ui.widgets.UiLayoutMath;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class AutismCustomFilterPresetOverlay extends DraggableOverlayWindow {
   private final Font init_Font;
   private final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private final autismclient.gui.ui.widgets.TextInputWidget init_BitSetUtil;
   private final List<AutismCustomFilterPresetOverlay.Inner_DataUtil> init_List = new ArrayList<>();
   private final AutismCustomFilterPresetOverlay.Inner_StreamUtil init_AutismCustomFilter = new AutismCustomFilterPresetOverlay.Inner_StreamUtil();
   private List<PacketPresetManager.Inner_DataUtil> load_List = List.of();
   private List<PacketPresetManager.Inner_DataUtil> read_List = List.of();
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
      this.init_BitSetUtil = new autismclient.gui.ui.widgets.TextInputWidget()
         .setPlaceholder("Preset name")
         .setFixedHeight(this.start_I_4())
         .setMinWidth(120.0F)
         .setPreferredWidth(120.0F)
         .setTextTone(autismclient.gui.f.b.l.read)
         .setPlaceholderTone(autismclient.gui.f.b.l.write);
   }

   @Override
   public String getOverlayId() {
      return "autism-custom-filter-presets";
   }

   @Override
   public int getMinWidth() {
      return this.pop_I_6();
   }

   @Override
   public int getMinHeight() {
      return this.run_I_4();
   }

   @Override
   public OverlayBounds getBounds() {
      return new OverlayBounds(this.flush_I, this.close_I, this.open, this.poll, this.write_Z, this.update_Z);
   }

   @Override
   public void setBounds(OverlayBounds bounds) {
      if (bounds != null) {
         OverlayBounds clamped = this.clampToScreen(this, bounds);
         this.flush_I = clamped.x;
         this.close_I = clamped.y;
         this.open = clamped.width;
         this.poll = clamped.height;
         this.write_Z = clamped.visible;
         this.update_Z = clamped.collapsed;
      }
   }

   @Override
   public void setVisible(boolean visible) {
      this.write_Z = visible;
      if (visible) {
         OverlayManager.getInstance().bringToFront(this);
      } else {
         this.open_V_4();
      }

      this.saveBounds();
   }

   public void toggleVisible() {
      this.setVisible(!this.write_Z);
   }

   @Override
   public boolean isVisible() {
      return this.write_Z;
   }

   @Override
   public boolean isCollapsed() {
      return this.update_Z;
   }

   @Override
   public void setCollapsed(boolean collapsed) {
      if (this.update_Z != collapsed) {
         this.update_Z = collapsed;
         if (collapsed) {
            this.clearFocusAndDismissMenu();
         }

         this.saveBounds();
      }
   }

   @Override
   public boolean supportsHeaderCollapseClick() {
      return true;
   }

   @Override
   public boolean isOverHeader(double mouseX, double mouseY) {
      if (!this.write_Z) {
         return false;
      }

      OverlayBounds bounds = this.getBounds();
      return mouseX >= this.flush_I
         && mouseX <= this.flush_I + this.open
         && mouseY >= this.close_I
         && mouseY <= this.close_I + 14
         && !this.isOverHeaderControl(mouseX, mouseY, bounds);
   }

   @Override
   public boolean hasFocusedTextInput() {
      return this.init_BitSetUtil.isFocused();
   }

   @Override
   public void clearInputFocus() {
      this.open_V_4();
   }

   @Override
   public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      if (this.write_Z || this.isClosing()) {
         this.init_List.clear();
         this.poll = this.push_I_6() + this.load(this.init(this.init_List_3().size()));
         OverlayBounds clamped = this.clampToScreen(this, new OverlayBounds(this.flush_I, this.close_I, this.open, this.poll, this.write_Z, this.update_Z));
         this.flush_I = clamped.x;
         this.close_I = clamped.y;
         this.open = clamped.width;
         this.poll = clamped.height;
         OverlayBounds bounds = new OverlayBounds(this.flush_I, this.close_I, this.open, this.poll, this.write_Z, this.update_Z);
         this.renderWindowFrame(context, mouseX, mouseY, bounds, "Preset Manager", this.update_Z, this.sync_Z);
         boolean clipBody = this.beginBodyClip(context, bounds, this.update_Z);
         if (!clipBody) {
            this.renderInactiveDim(context, bounds, this.update_Z, this.sync_Z);
         } else {
            try {
               int x = this.flush_I + this.get_I_4();
               int y = this.close_I + 14 + this.set_I_4();
               int width = this.open - this.get_I_4() * 2;
               autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
               autismclient.gui.ui.widgets.ListRowRenderer.drawHeaderTitle(context, this.init_Font, "Preset Name", x, y);
               String counts = "C2S " + module.getCustomC2sPacketsCopy().size() + " | S2C " + module.getCustomS2cPacketsCopy().size();
               ThemedTextHelper.init(context, this.init_Font, counts, ThemedTextHelper.TextTone.MUTED, x + width - ThemedTextHelper.init(this.init_Font, counts, ThemedTextHelper.TextTone.MUTED), y, false);
               y += this.put();
               this.init_BitSetUtil.setBounds(x, y, width, this.start_I_4());
               this.init_BitSetUtil.render(this.init_SyncManager_GuiGraphicsExtract_2(context, mouseX, mouseY, delta));
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
               int resetW = Math.min(width, autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(ThemedTextHelper.init(this.init_Font, resetLabel, ThemedTextHelper.TextTone.BODY)));
               y = this.init(context, mouseX, mouseY, x, y, resetW, resetLabel, this::close_V_4, true);
               y += this.reset() - this.clear();
               int listHeight = Math.max(this.load(1), this.poll - (y - this.close_I) - this.put() - this.get_I_4());
               this.init_I_GuiGraphicsExtract_6(context, mouseX, mouseY, delta, x, y, width, listHeight);
            } finally {
               this.endBodyClip(context, clipBody);
               this.renderInactiveDim(context, bounds, this.update_Z, this.sync_Z);
            }
         }
      }
   }

   private int init_I_GuiGraphicsExtract_6(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, int x, int y, int width, int listHeight) {
      String headerLabel = "Saved Presets";
      autismclient.gui.ui.widgets.ListRowRenderer.drawHeaderTitle(context, this.init_Font, headerLabel, x, y);
      int headerY = y;
      y += this.put();
      List<AutismCustomFilterPresetOverlay.Inner_CachePool> rows = this.init_List_3();
      this.init_AutismCustomFilter.init_List = rows;
      autismclient.gui.ui.widgets.ScrollListLayout.ListLayout baseLayout = this.init(x, y, width, listHeight, rows.size(), 0);
      int visualScroll = this.init_AutismCustomFilter.init_CacheEntry.tickAndClamp(delta, baseLayout.maxScroll());
      autismclient.gui.ui.widgets.ScrollListLayout.ListLayout listLayout = this.init(x, y, width, listHeight, rows.size(), visualScroll);
      int contentWidth = Math.max(40, listLayout.contentWidth());
      this.init_AutismCustomFilter.init(x, y, width, listHeight, listLayout.scrollbar());
      boolean focused = OverlayManager.getInstance().isFocused(this) || OverlayManager.getInstance().isTopmost(this);
      listLayout.drawFrame(context, focused);
      if (rows.isEmpty()) {
         autismclient.gui.ui.widgets.ListRowRenderer.drawEmptyPlaceholder(context, this.init_Font, "No presets", x, y, contentWidth);
      } else {
         listLayout.pushContentClip(context);

         try {
            listLayout.forEachVisibleRow(
               rows.size(),
               (index, rowY) -> {
                  AutismCustomFilterPresetOverlay.Inner_CachePool row = rows.get(index);
                  int rowTextY = UiLayoutMath.centerTextY(rowY, this.stop(), this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.read), this.init_CharEncoder.flush());
                  if (row.init_Z) {
                     autismclient.gui.ui.widgets.ListRowRenderer.drawRow(
                        context,
                        this.init_Font,
                        row.init_Str,
                        x + 2,
                        rowY,
                        contentWidth,
                        this.stop(),
                        false,
                        false,
                        autismclient.gui.ui.widgets.ListRowRenderer.RowTone.WARNING
                     );
                  } else if (row.load) {
                     autismclient.gui.ui.widgets.ListRowRenderer.drawRow(
                        context,
                        this.init_Font,
                        row.init_Str,
                        x + 2,
                        rowY,
                        contentWidth,
                        this.stop(),
                        false,
                        false,
                        autismclient.gui.ui.widgets.ListRowRenderer.RowTone.NORMAL
                     );
                  } else if (row.init_bT_DataUtil != null) {
                     boolean selected = row.init_bT_DataUtil.name().equals(this.init_Str_2);
                     boolean hovered = mouseX >= x + 2 && mouseX < x + 2 + contentWidth && mouseY >= rowY && mouseY < rowY + this.stop();
                     autismclient.gui.ui.widgets.ListRowRenderer.drawRow(
                        context,
                        this.init_Font,
                        "",
                        x + 2,
                        rowY,
                        contentWidth,
                        this.stop(),
                        hovered,
                        selected,
                        row.init_bT_DataUtil.isBuiltIn() ? autismclient.gui.ui.widgets.ListRowRenderer.RowTone.WARNING : autismclient.gui.ui.widgets.ListRowRenderer.RowTone.NORMAL
                     );
                     int textColor = selected ? UiColorPalette.selectedText() : (row.init_bT_DataUtil.isBuiltIn() ? -4729601 : UiColorPalette.menuItemText());
                     String prefix = row.init_bT_DataUtil.isBuiltIn() ? "[DEV] " : "[USR] ";
                     String name = ThemedTextHelper.init(this.init_Font, prefix + row.init_bT_DataUtil.name(), contentWidth - 70, ThemedTextHelper.TextTone.BODY);
                     ThemedTextHelper.init(context, this.init_Font, name, textColor, x + 5, rowTextY, false);
                     String counts = row.init_bT_DataUtil.c2sCount() + " / " + row.init_bT_DataUtil.s2cCount();
                     int countsWidth = ThemedTextHelper.init(this.init_Font, counts, ThemedTextHelper.TextTone.BODY);
                     ThemedTextHelper.init(context, this.init_Font, counts, textColor, x + contentWidth - countsWidth - 1, rowTextY, false);
                  }

                  autismclient.gui.ui.widgets.ListRowRenderer.drawRowSeparator(context, x + 2, rowY + this.stop(), contentWidth);
               }
            );
         } finally {
            listLayout.popContentClip(context);
         }
      }

      listLayout.renderScrollbar(context, mouseX, mouseY, this.flush_Z);
      String footer = listLayout.maxScroll() > 0
         ? "Scroll: "
            + Math.min(rows.size(), Math.round((float)this.init_AutismCustomFilter.init_CacheEntry.getOffset() / this.next()) + 1)
            + "/"
            + Math.max(1, Math.round((float)listLayout.maxScroll() / this.next()) + 1)
         : (this.init_Str_2 == null ? "Select a preset row" : "Selected: " + this.init_Str_2);
      int footerMaxW = Math.max(1, width - ThemedTextHelper.init(this.init_Font, headerLabel, ThemedTextHelper.TextTone.BODY) - 8);
      footer = ThemedTextHelper.init(this.init_Font, footer, footerMaxW, ThemedTextHelper.TextTone.MUTED);
      int footerW = ThemedTextHelper.init(this.init_Font, footer, ThemedTextHelper.TextTone.MUTED);
      ThemedTextHelper.init(context, this.init_Font, footer, ThemedTextHelper.TextTone.MUTED, x + width - footerW, headerY, false);
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
      PacketPresetManager manager = PacketPresetManager.getInstance();
      List<PacketPresetManager.Inner_DataUtil> builtInEntries = manager.builtInPresets();
      List<PacketPresetManager.Inner_DataUtil> userEntries = manager.userPresets();
      if (builtInEntries == this.load_List && userEntries == this.read_List) {
         return this.write_List;
      }

      List<AutismCustomFilterPresetOverlay.Inner_CachePool> rows = new ArrayList<>();
      rows.add(AutismCustomFilterPresetOverlay.Inner_CachePool.init("Developer Templates"));

      for (PacketPresetManager.Inner_DataUtil entry : builtInEntries) {
         rows.add(AutismCustomFilterPresetOverlay.Inner_CachePool.init(entry));
      }

      rows.add(AutismCustomFilterPresetOverlay.Inner_CachePool.init("User Presets"));
      if (userEntries.isEmpty()) {
         rows.add(AutismCustomFilterPresetOverlay.Inner_CachePool.load("No user presets yet"));
      } else {
         for (PacketPresetManager.Inner_DataUtil entry : userEntries) {
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
      } else if (PacketPresetManager.getInstance().savePreset(name)) {
         this.init_Str_2 = name;
         this.init_BitSetUtil.setValue(name);
      }
   }

   private void update_V_5() {
      String presetName = this.read_Str_3();
      if (presetName == null) {
         AutismClientMessaging.sendPrefixed("Select a preset first.");
      } else if (PacketPresetManager.getInstance().loadPreset(presetName)) {
         this.init_Str_2 = PacketPresetManager.getInstance().findPreset(presetName) != null ? PacketPresetManager.getInstance().findPreset(presetName).name() : presetName;
         this.init_BitSetUtil.setValue(this.init_Str_2);
      }
   }

   private void sync_V_4() {
      String presetName = this.read_Str_3();
      if (presetName == null) {
         AutismClientMessaging.sendPrefixed("Select a user preset first.");
      } else {
         PacketPresetManager.Inner_DataUtil entry = PacketPresetManager.getInstance().findPreset(presetName);
         if (entry != null && !entry.isBuiltIn()) {
            if (PacketPresetManager.getInstance().overwritePreset(entry.name())) {
               this.init_Str_2 = entry.name();
               this.init_BitSetUtil.setValue(entry.name());
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
         PacketPresetManager.Inner_DataUtil entry = PacketPresetManager.getInstance().findPreset(presetName);
         if (entry != null && !entry.isBuiltIn()) {
            if (PacketPresetManager.getInstance().deletePreset(entry.name())) {
               if (entry.name().equals(this.init_Str_2)) {
                  this.init_Str_2 = null;
               }

               this.init_BitSetUtil.setValue("");
            }
         } else {
            AutismClientMessaging.sendPrefixed("Developer presets cannot be deleted.");
         }
      }
   }

   private void close_V_4() {
      autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
      module.resetCustomC2sPackets();
      module.resetCustomS2cPackets();
      AutismClientMessaging.sendPrefixed("Reset current filter to default packet lists.");
   }

   private String load_Str_4() {
      String value = this.init_BitSetUtil.getValue();
      if (value == null) {
         return null;
      }

      value = value.trim();
      return value.isEmpty() ? null : value;
   }

   private String read_Str_3() {
      if (this.init_Str_2 != null) {
         PacketPresetManager.Inner_DataUtil selectedEntry = PacketPresetManager.getInstance().findPreset(this.init_Str_2);
         if (selectedEntry != null) {
            return selectedEntry.name();
         }
      }

      String typedName = this.load_Str_4();
      if (typedName == null) {
         return null;
      }

      PacketPresetManager.Inner_DataUtil typedEntry = PacketPresetManager.getInstance().findPreset(typedName);
      return typedEntry != null ? typedEntry.name() : null;
   }

   private PacketPresetManager.Inner_DataUtil init_bT_DataUtil() {
      return this.init_Str_2 == null ? null : PacketPresetManager.getInstance().findPreset(this.init_Str_2);
   }

   private boolean poll_Z_4() {
      String name = this.load_Str_4();
      return name != null && !PacketPresetManager.getInstance().update(name);
   }

   private boolean push_Z_3() {
      return this.read_Str_3() != null;
   }

   private boolean pop_Z_2() {
      PacketPresetManager.Inner_DataUtil entry = this.init_bT_DataUtil();
      return entry != null && !entry.isBuiltIn();
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
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(
         context,
         this.init_Font,
         x,
         y,
         width,
         this.add_I_4(),
         label,
         enabled ? autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST,
         enabled,
         mouseX,
         mouseY
      );
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.write_Z) {
         return false;
      }

      if (button == 0 && mouseX >= this.flush_I && mouseX < this.flush_I + this.open && mouseY >= this.close_I && mouseY < this.close_I + 14) {
         OverlayBounds bounds = this.getBounds();
         if (this.isOverHeaderCloseButton(mouseX, mouseY, bounds)) {
            this.setVisible(false);
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
            autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbar = this.init_AutismCustomFilter.init_PoolEntry_DataUtil;
            if (scrollbar != null && scrollbar.isScrollable() && scrollbar.containsTrack(mouseX, mouseY)) {
               this.flush_Z = true;
               this.push = Math.max(0, (int)mouseY - scrollbar.thumbY());
               this.init_AutismCustomFilter.init_CacheEntry.dragToMouseYStepped(scrollbar, mouseY, this.push, this.next());
               return true;
            }
         }

         if (this.init_Z_BitSetUtil_2(this.init_BitSetUtil, mouseX, mouseY, button)) {
            return true;
         }

         if (button == 0 && this.init_AutismCustomFilter.init(mouseX, mouseY)) {
            AutismCustomFilterPresetOverlay.Inner_CachePool row = this.init_AutismCustomFilter.init(mouseY);
            if (row != null && row.init_bT_DataUtil != null) {
               this.init_Str_2 = row.init_bT_DataUtil.name();
               this.init_BitSetUtil.setValue(this.init_Str_2);
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

         return this.containsPoint(mouseX, mouseY);
      }
   }

   private boolean init_Z_BitSetUtil_2(autismclient.gui.ui.widgets.TextInputWidget field, double mouseX, double mouseY, int button) {
      boolean clicked = field.mouseClicked(this.init_SyncManager_D_D_2(mouseX, mouseY), (float)mouseX, (float)mouseY, button);
      if (clicked) {
         this.open_V_4();
         field.setFocused(true);
      }

      return clicked;
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (this.init_BitSetUtil.mouseReleased(this.init_SyncManager_D_D_2(mouseX, mouseY), (float)mouseX, (float)mouseY, button)) {
         return true;
      }

      if (button == 0 && this.flush_Z) {
         this.flush_Z = false;
         return true;
      }

      if (button == 0) {
         if (this.sync_Z) {
            this.saveBounds();
         }

         this.sync_Z = false;
      }

      return false;
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.init_BitSetUtil
         .mouseDragged(this.init_SyncManager_D_D_2(mouseX, mouseY), (float)mouseX, (float)mouseY, button, (float)deltaX, (float)deltaY)) {
         return true;
      } else if (this.flush_Z) {
         this.init_AutismCustomFilter.init_CacheEntry.dragToMouseYStepped(this.init_AutismCustomFilter.init_PoolEntry_DataUtil, mouseY, this.push, this.next());
         return true;
      } else if (this.sync_Z && button == 0) {
         OverlayBounds nextBounds = this.clampToScreen(
            this, new OverlayBounds((int)(mouseX - this.init_D), (int)(mouseY - this.load_D), this.open, this.poll, this.write_Z, this.update_Z)
         );
         this.flush_I = nextBounds.x;
         this.close_I = nextBounds.y;
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      if (this.init_AutismCustomFilter.init(mouseX, mouseY)) {
         int maxScroll = this.init(
               this.init_AutismCustomFilter.init_I,
               this.init_AutismCustomFilter.load,
               this.init_AutismCustomFilter.read,
               this.init_AutismCustomFilter.write,
               this.init_AutismCustomFilter.init_List.size(),
               0
            )
            .maxScroll();
         this.init_AutismCustomFilter.init_CacheEntry.scrollByWheel(amount, this.next(), maxScroll);
         return true;
      } else {
         return false;
      }
   }

   private autismclient.gui.ui.widgets.ScrollListLayout.ListLayout init(int x, int y, int width, int listHeight, int rowCount, int scrollOffset) {
      return autismclient.gui.ui.widgets.ScrollListLayout.compute(x, y, width, listHeight, rowCount, this.stop(), this.next(), scrollOffset, this.valid(), this.prev());
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (!this.write_Z) {
         return false;
      }

      if (keyCode == 256) {
         if (this.init_BitSetUtil.isFocused()) {
            this.open_V_4();
            return true;
         } else {
            this.setVisible(false);
            return true;
         }
      } else if ((keyCode == 257 || keyCode == 335) && this.init_BitSetUtil.isFocused() && this.poll_Z_4()) {
         this.write_V_7();
         return true;
      } else {
         return !this.init_BitSetUtil.isFocused()
            ? false
            : this.init_BitSetUtil.keyPressed(this.init_SyncManager_D_D_2(0.0, 0.0), keyCode, scanCode, modifiers);
      }
   }

   @Override
   public boolean charTyped(char chr, int modifiers) {
      return this.write_Z && this.init_BitSetUtil.isFocused()
         ? this.init_BitSetUtil.charTyped(this.init_SyncManager_D_D_2(0.0, 0.0), chr, modifiers)
         : false;
   }

   @Override
   public boolean containsPoint(double mouseX, double mouseY) {
      if (!this.write_Z) {
         return false;
      }

      OverlayBounds bounds = this.getBounds();
      int frameHeight = this.update_Z ? 14 : this.poll;
      return mouseX >= bounds.x && mouseX <= bounds.x + bounds.width && mouseY >= bounds.y && mouseY <= bounds.y + frameHeight
         ? true
         : mouseX >= this.init_BitSetUtil.x()
            && mouseX <= this.init_BitSetUtil.x() + this.init_BitSetUtil.width()
            && mouseY >= this.init_BitSetUtil.y()
            && mouseY <= this.init_BitSetUtil.y() + this.init_BitSetUtil.height();
   }

   private void open_V_4() {
      this.init_BitSetUtil.setFocused(false);
   }

   private autismclient.gui.ui.layout.LayoutContext init_SyncManager_GuiGraphicsExtract_2(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      autismclient.gui.ui.layout.LayoutViewport viewport = autismclient.gui.ui.layout.LayoutViewport.create(1.0F);
      return new autismclient.gui.ui.layout.LayoutContext(
         context, this.init_Font, viewport, this.init_CharEncoder, viewport.toUiX(mouseX), viewport.toUiY(mouseY), delta
      );
   }

   private autismclient.gui.ui.layout.LayoutContext init_SyncManager_D_D_2(double mouseX, double mouseY) {
      autismclient.gui.ui.layout.LayoutViewport viewport = autismclient.gui.ui.layout.LayoutViewport.create(1.0F);
      return new autismclient.gui.ui.layout.LayoutContext(null, this.init_Font, viewport, this.init_CharEncoder, viewport.toUiX(mouseX), viewport.toUiY(mouseY), 0.0F);
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
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int clear() {
      return 2;
   }

   private int reset() {
      return 4;
   }

   private int start_I_4() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int stop() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
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
      private final PacketPresetManager.Inner_DataUtil init_bT_DataUtil;

      private Inner_CachePool(boolean header, boolean note, String label, PacketPresetManager.Inner_DataUtil entry) {
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

      private static AutismCustomFilterPresetOverlay.Inner_CachePool init(PacketPresetManager.Inner_DataUtil entry) {
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
      private final autismclient.gui.ui.widgets.ScrollOffsetController init_CacheEntry = new autismclient.gui.ui.widgets.ScrollOffsetController();
      private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics init_PoolEntry_DataUtil = null;
      private List<AutismCustomFilterPresetOverlay.Inner_CachePool> init_List = List.of();

      private void init(int x, int y, int width, int height, autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics) {
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
         int idx = (int)((mouseY - this.load - 2.0 + this.init_CacheEntry.getTargetOffset()) / AutismCustomFilterPresetOverlay.this.next());
         return idx >= 0 && idx < this.init_List.size() ? this.init_List.get(idx) : null;
      }
   }
}
