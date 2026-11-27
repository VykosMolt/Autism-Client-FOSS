package autismclient.gui.ui.widgets;

import autismclient.util.cA;
import java.util.List;
import java.util.function.Consumer;

public final class DropdownMenu implements autismclient.gui.ui.UiLayerComponent {
   private static final int SCREEN_EDGE_MARGIN = 4;
   private autismclient.gui.f.NetHelper anchorBounds;
   private autismclient.gui.f.NetHelper popupBounds = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
   private List<String> options;
   private String selectedOption;
   private final Consumer<String> onSelect;
   private int scrollRow;
   private int visibleRows;
   private boolean open;
   private long openedNanos;
   private boolean draggingScrollbar;
   private int dragAnchorOffsetY;
   private ScrollbarRenderer.ScrollbarBounds scrollbarBounds;
   private boolean widthCacheDirty = true;
   private int cachedPopupWidth = 1;
   private int cachedWidthScreenW = -1;
   private int lastScreenWidth = -1;
   private int lastScreenHeight = -1;
   private int lastRowHeight = -1;
   private boolean cachedWidthOverflow;

   public DropdownMenu(autismclient.gui.f.NetHelper bounds, List<String> options, String selected, Consumer<String> onSelect) {
      this.anchorBounds = bounds == null ? autismclient.gui.f.NetHelper.init(0, 0, 0, 0) : bounds;
      this.options = options == null ? List.of() : List.copyOf(options);
      this.selectedOption = selected == null ? "" : selected;
      this.onSelect = onSelect == null ? ignored -> {} : onSelect;
   }

   @Override
   public autismclient.gui.f.NetHelper getBounds() {
      return this.anchorBounds;
   }

   @Override
   public autismclient.gui.f.NetHelper getInteractiveBounds() {
      return this.open ? this.anchorBounds.init(this.popupBounds) : this.anchorBounds;
   }

   @Override
   public void setBounds(autismclient.gui.f.NetHelper bounds) {
      if (bounds != null) {
         if (this.anchorBounds.update() != bounds.update()) {
            this.widthCacheDirty = true;
         }

         this.anchorBounds = bounds;
      }
   }

   public boolean isOpen() {
      return this.open;
   }

   public autismclient.gui.f.NetHelper getPopupBounds() {
      this.updatePopupLayout();
      return this.popupBounds;
   }

   public int getScrollRow() {
      return this.scrollRow;
   }

   public void setOptions(List<String> options) {
      List<String> next = options == null ? List.of() : List.copyOf(options);
      if (!this.options.equals(next)) {
         this.options = next;
         this.widthCacheDirty = true;
         if (!this.options.contains(this.selectedOption)) {
            this.selectedOption = this.options.isEmpty() ? "" : this.options.get(0);
         }

         this.scrollRow = clamp(this.scrollRow, 0, this.maxScrollRow());
         this.scrollSelectedIntoView();
      }
   }

   public void setSelectedOption(String selected) {
      String next = selected == null ? "" : selected;
      if (!this.selectedOption.equals(next)) {
         this.selectedOption = next;
         this.scrollSelectedIntoView();
      }
   }

   public boolean containsPopupPoint(int mouseX, int mouseY) {
      this.updatePopupLayout();
      return this.open && this.popupBounds.init_Z(mouseX, mouseY);
   }

   public void open() {
      this.open = true;
      this.openedNanos = System.nanoTime();
      this.updatePopupLayout();
      this.scrollSelectedIntoView();
   }

   public void close() {
      this.open = false;
      this.openedNanos = 0L;
      this.draggingScrollbar = false;
   }

   public static void renderTrigger(
      autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String label, boolean hovered, boolean open
   ) {
      renderTriggerInternal(context, bounds, label, hovered, open, false, true);
   }

   public static void renderTrigger(
      autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String label, boolean hovered, boolean open, boolean enabled
   ) {
      renderTriggerInternal(context, bounds, label, hovered, open, false, enabled);
   }

   public static void renderCenteredTrigger(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String label, boolean hovered, boolean open) {
      renderTriggerInternal(context, bounds, label, hovered, open, true, true);
   }

   private static void renderTriggerInternal(
      autismclient.gui.f.SyncManager context,
      autismclient.gui.f.NetHelper bounds,
      String label,
      boolean hovered,
      boolean open,
      boolean centered,
      boolean enabled
   ) {
      autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().colors();
      int radius = 3;
      int fill = open && enabled ? colors.inputSurfaceFocused : colors.inputSurface;
      autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), bounds, radius, fill, open && enabled ? colors.accent : colors.borderControl);
      float hoverT = autismclient.gui.ui.HoverTransitionCache.sample(autismclient.gui.ui.HoverTransitionCache.boundsKey(bounds), hovered && enabled);
      if (hoverT > 0.001F) {
         autismclient.gui.f.PacketUtil.load(
            context.init_GuiGraphicsExtract(), bounds.init(1), Math.max(1, radius - 1), autismclient.gui.f.PacketUtil.init(colors.innerHighlight, hoverT)
         );
      }

      int textColor = enabled ? colors.textPrimary : colors.textMuted;
      if (centered) {
         context.init_PoolEntry().drawCentered(context.init_GuiGraphicsExtract(), label, bounds, textColor);
      } else {
         context.init_PoolEntry()
            .drawFitted(
               context.init_GuiGraphicsExtract(),
               label,
               bounds.read() + 5,
               context.init_PoolEntry().centerTextY(bounds),
               Math.max(1, bounds.update() - 18),
               textColor
            );
      }

      autismclient.gui.f.PacketUtil.init(
         context.init_GuiGraphicsExtract(),
         autismclient.gui.f.NetHelper.init(bounds.init() - 10, bounds.write() + Math.max(0, (bounds.sync() - 8) / 2), 8, 8),
         open,
         textColor
      );
   }

   @Override
   public void render(autismclient.gui.f.SyncManager context) {
      if (this.open && !this.options.isEmpty()) {
         int rowHeight = defaultRowHeight();
         this.lastScreenWidth = context.init_I();
         this.lastScreenHeight = context.load_I();
         this.lastRowHeight = rowHeight;
         this.visibleRows = this.computeVisibleRows(context.load_I(), rowHeight);
         int width = this.measurePopupWidth(context);
         this.layoutPopupBounds(context.init_I(), context.load_I(), rowHeight, width);
         autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().colors();
         float progress = this.openedNanos == 0L ? 1.0F : Math.max(0.0F, Math.min(1.0F, (float)(System.nanoTime() - this.openedNanos) / 1.6E8F));
         float fade = 1.0F - (1.0F - progress) * (1.0F - progress) * (1.0F - progress);
         int radius = 5;
         autismclient.gui.f.PacketUtil.load(
            context.init_GuiGraphicsExtract(), this.popupBounds, radius, autismclient.gui.f.PacketUtil.init(colors.borderSubtle, fade)
         );
         autismclient.gui.f.PacketUtil.load(
            context.init_GuiGraphicsExtract(), this.popupBounds.init(1), Math.max(1, radius - 1), autismclient.gui.f.PacketUtil.init(colors.popupSurface, fade)
         );
         int contentWidth = this.options.size() > this.visibleRows ? this.popupBounds.update() - 6 - 3 : this.popupBounds.update() - 2;

         for (int rowIndex = 0; rowIndex < this.visibleRows; rowIndex++) {
            int optionIndex = this.scrollRow + rowIndex;
            if (optionIndex >= this.options.size()) {
               break;
            }

            String option = this.options.get(optionIndex);
            autismclient.gui.f.NetHelper row = autismclient.gui.f.NetHelper.init(
               this.popupBounds.read() + 1, this.popupBounds.write() + 1 + rowIndex * rowHeight, Math.max(1, contentWidth), rowHeight
            );
            boolean hovered = row.init_Z(context.read(), context.write());
            boolean active = option.equals(this.selectedOption);
            float hoverT = autismclient.gui.ui.HoverTransitionCache.sample("dropdown:" + option, hovered);
            autismclient.gui.f.NetHelper pill = autismclient.gui.f.NetHelper.init(row.read() + 2, row.write(), Math.max(1, row.update() - 4), row.sync());
            if (active) {
               autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), pill, 4, autismclient.gui.f.PacketUtil.init(colors.accentSoft, fade));
            } else if (hoverT > 0.001F) {
               autismclient.gui.f.PacketUtil.load(
                  context.init_GuiGraphicsExtract(), pill, 4, autismclient.gui.f.PacketUtil.init(autismclient.gui.f.PacketUtil.init(colors.innerHighlight, hoverT), fade)
               );
            }

            context.init_PoolEntry()
               .drawFitted(
                  context.init_GuiGraphicsExtract(),
                  option,
                  row.read() + 4,
                  context.init_PoolEntry().centerTextY(row),
                  Math.max(1, row.update() - 18),
                  autismclient.gui.f.PacketUtil.init(colors.textPrimary, fade)
               );
            if (active) {
               autismclient.gui.f.PacketUtil.update(
                  context.init_GuiGraphicsExtract(),
                  autismclient.gui.f.NetHelper.init(row.init() - 11, row.write() + Math.max(0, (row.sync() - 8) / 2), 8, 8),
                  autismclient.gui.f.PacketUtil.init(colors.accent, fade)
               );
            }
         }

         if (this.options.size() > this.visibleRows) {
            autismclient.gui.f.NetHelper track = autismclient.gui.f.NetHelper.init(
               this.popupBounds.init() - 6 - 1, this.popupBounds.write() + 4, 6, Math.max(1, this.popupBounds.sync() - 8)
            );
            this.scrollbarBounds = ScrollbarRenderer.init(track, this.options.size() * rowHeight, this.visibleRows * rowHeight, this.scrollRow * rowHeight);
            ScrollbarRenderer.init(
               context, this.scrollbarBounds, this.scrollbarBounds.track().init_Z(context.read(), context.write()), this.draggingScrollbar
            );
         } else {
            this.scrollbarBounds = null;
         }
      }
   }

   @Override
   public autismclient.gui.ui.UiInputResult onMouseClicked(int mouseX, int mouseY, int button) {
      this.updatePopupLayout();
      if (button != 0) {
         if (!this.open) {
            return autismclient.gui.ui.UiInputResult.IGNORED;
         }

         if (!this.anchorBounds.init_Z(mouseX, mouseY) && !this.popupBounds.init_Z(mouseX, mouseY)) {
            this.close();
         }

         return autismclient.gui.ui.UiInputResult.HANDLED;
      } else if (this.open) {
         if (this.popupBounds.init_Z(mouseX, mouseY)) {
            if (this.scrollbarBounds != null && this.scrollbarBounds.track().init_Z(mouseX, mouseY)) {
               this.dragAnchorOffsetY = this.scrollbarBounds.load().init_Z(mouseX, mouseY)
                  ? mouseY - this.scrollbarBounds.load().write()
                  : this.scrollbarBounds.load().sync() / 2;
               this.draggingScrollbar = true;
               this.updateScrollFromMouseY(mouseY);
               return autismclient.gui.ui.UiInputResult.HANDLED;
            }

            int index = this.scrollRow + Math.max(0, (mouseY - this.popupBounds.write() - 1) / Math.max(1, this.effectiveRowHeight()));
            if (index >= 0 && index < this.options.size()) {
               this.selectedOption = this.options.get(index);
               this.onSelect.accept(this.selectedOption);
            }

            this.close();
            return autismclient.gui.ui.UiInputResult.HANDLED;
         } else if (this.anchorBounds.init_Z(mouseX, mouseY)) {
            this.close();
            return autismclient.gui.ui.UiInputResult.HANDLED;
         } else {
            this.close();
            return autismclient.gui.ui.UiInputResult.HANDLED;
         }
      } else if (this.anchorBounds.init_Z(mouseX, mouseY)) {
         this.open();
         return autismclient.gui.ui.UiInputResult.HANDLED;
      } else {
         return autismclient.gui.ui.UiInputResult.IGNORED;
      }
   }

   @Override
   public autismclient.gui.ui.UiInputResult onMouseReleased(int mouseX, int mouseY, int button) {
      this.updatePopupLayout();
      if (!this.draggingScrollbar) {
         return this.open ? autismclient.gui.ui.UiInputResult.HANDLED : autismclient.gui.ui.UiInputResult.IGNORED;
      }

      this.draggingScrollbar = false;
      return autismclient.gui.ui.UiInputResult.HANDLED;
   }

   @Override
   public autismclient.gui.ui.UiInputResult onMouseDragged(int mouseX, int mouseY, int button, double deltaX, double deltaY) {
      this.updatePopupLayout();
      if (this.draggingScrollbar && button == 0) {
         this.updateScrollFromMouseY(mouseY);
         return autismclient.gui.ui.UiInputResult.HANDLED;
      } else {
         return this.open ? autismclient.gui.ui.UiInputResult.HANDLED : autismclient.gui.ui.UiInputResult.IGNORED;
      }
   }

   @Override
   public autismclient.gui.ui.UiInputResult onMouseScrolled(int mouseX, int mouseY, double amount) {
      this.updatePopupLayout();
      if (!this.open) {
         return autismclient.gui.ui.UiInputResult.IGNORED;
      }

      if (this.popupBounds.init_Z(mouseX, mouseY)) {
         int direction = amount < 0.0 ? 1 : (amount > 0.0 ? -1 : 0);
         this.scrollRow = clamp(this.scrollRow + direction, 0, this.maxScrollRow());
      }

      return autismclient.gui.ui.UiInputResult.HANDLED;
   }

   private int measurePopupWidth(autismclient.gui.f.SyncManager context) {
      boolean overflow = this.options.size() > this.visibleRows;
      if (!this.widthCacheDirty && this.cachedWidthScreenW == context.init_I() && this.cachedWidthOverflow == overflow) {
         return this.cachedPopupWidth;
      }

      int chrome = 20 + (overflow ? 7 : 0);
      int width = Math.max(1, this.anchorBounds.update());

      for (String option : this.options) {
         width = Math.max(width, context.init_PoolEntry().measureWidth(option) + chrome);
      }

      this.cachedPopupWidth = Math.min(width, Math.max(1, context.init_I() - 8));
      this.cachedWidthScreenW = context.init_I();
      this.cachedWidthOverflow = overflow;
      this.widthCacheDirty = false;
      return this.cachedPopupWidth;
   }

   private void updatePopupLayout() {
      if (this.open) {
         int screenWidth = this.lastScreenWidth > 0 ? this.lastScreenWidth : Math.max(1, cA.init_I());
         int screenHeight = this.lastScreenHeight > 0 ? this.lastScreenHeight : Math.max(1, cA.load_I());
         int rowHeight = this.lastRowHeight > 0 ? this.lastRowHeight : defaultRowHeight();
         this.visibleRows = this.computeVisibleRows(screenHeight, rowHeight);
         int width = Math.max(1, Math.min(this.cachedPopupWidth > 1 ? this.cachedPopupWidth : this.anchorBounds.update(), screenWidth - 8));
         this.layoutPopupBounds(screenWidth, screenHeight, rowHeight, width);
      }
   }

   private void layoutPopupBounds(int screenWidth, int screenHeight, int rowHeight, int width) {
      this.visibleRows = Math.max(1, Math.min(this.options.size(), this.visibleRows <= 0 ? this.computeVisibleRows(screenHeight, rowHeight) : this.visibleRows));
      this.scrollRow = clamp(this.scrollRow, 0, this.maxScrollRow());
      int height = Math.max(1, this.visibleRows * rowHeight + 2);
      int x = this.anchorBounds.read();
      int below = this.anchorBounds.load() + 1;
      int above = this.anchorBounds.write() - height - 1;
      int availableBelow = screenHeight - 4 - below;
      int availableAbove = this.anchorBounds.write() - 4;
      int y = availableBelow < height && availableBelow < availableAbove ? above : below;
      this.popupBounds = autismclient.gui.f.NetHelper.init(
         clamp(x, 4, Math.max(4, screenWidth - 4 - width)), clamp(y, 4, Math.max(4, screenHeight - 4 - height)), width, height
      );
   }

   private int computeVisibleRows(int screenHeight, int rowHeight) {
      int below = Math.max(0, (screenHeight - 4 - this.anchorBounds.load() - 1) / Math.max(1, rowHeight));
      int above = Math.max(0, (this.anchorBounds.write() - 4 - 1) / Math.max(1, rowHeight));
      int cap = Math.max(3, screenHeight * 2 / 3 / Math.max(1, rowHeight));
      return Math.max(1, Math.min(this.options.size(), Math.min(cap, Math.max(below, above))));
   }

   private static int defaultRowHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(12);
   }

   private int effectiveRowHeight() {
      return this.visibleRows <= 0 ? 1 : Math.max(1, (this.popupBounds.sync() - 2) / this.visibleRows);
   }

   private int maxScrollRow() {
      return Math.max(0, this.options.size() - Math.max(1, this.visibleRows));
   }

   private void scrollSelectedIntoView() {
      int index = this.options.indexOf(this.selectedOption);
      if (index >= 0) {
         if (index < this.scrollRow) {
            this.scrollRow = index;
         }

         if (this.visibleRows > 0 && index >= this.scrollRow + this.visibleRows) {
            this.scrollRow = index - this.visibleRows + 1;
         }

         this.scrollRow = clamp(this.scrollRow, 0, this.maxScrollRow());
      }
   }

   private void updateScrollFromMouseY(int mouseY) {
      if (this.scrollbarBounds != null && this.scrollbarBounds.maxScroll() > 0) {
         int pixelScroll = ScrollbarRenderer.scrollForDrag(this.scrollbarBounds, mouseY, this.dragAnchorOffsetY);
         this.scrollRow = clamp(Math.round((float)pixelScroll / Math.max(1, this.effectiveRowHeight())), 0, this.maxScrollRow());
      }
   }

   private static int clamp(int value, int min, int max) {
      return Math.max(min, Math.min(value, max));
   }
}
