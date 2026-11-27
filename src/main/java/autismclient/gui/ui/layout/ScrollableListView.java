package autismclient.gui.ui.layout;

import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class ScrollableListView {
   private final int x;
   private final int y;
   private final int width;
   private final int height;
   private final int rowHeight;
   private final int scrollbarWidth;
   private int contentHeight = 0;
   private int scrollOffset = 0;
   private boolean draggingScrollbar = false;
   private int scrollbarGrabOffset = 0;
   private int borderColor = 0;
   private final int clipLeft;
   private final int clipTop;
   private final int clipRight;
   private final int clipBottom;

   public ScrollableListView(int x, int y, int width, int height, int rowHeight, int scrollbarWidth) {
      this.x = x;
      this.y = y;
      this.width = Math.max(4, width);
      this.height = Math.max(4, height);
      this.rowHeight = Math.max(1, rowHeight);
      this.scrollbarWidth = Math.max(0, scrollbarWidth);
      this.clipLeft = this.x;
      this.clipTop = this.y;
      this.clipRight = this.x + this.width;
      this.clipBottom = this.y + this.height;
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public int getRowWidthWithScrollbar() {
      return this.width - this.scrollbarWidth - 3;
   }

   public int getRowWidth() {
      return this.width - 2;
   }

   public int getRowHeight() {
      return this.rowHeight;
   }

   public int getContentHeight() {
      return this.contentHeight;
   }

   public int getScrollOffset() {
      return this.scrollOffset;
   }

   private int getClipHeight() {
      return Math.max(0, this.clipBottom - this.clipTop);
   }

   private int getEffectiveViewHeight() {
      int viewHeight = this.getClipHeight();
      if (viewHeight > 0 && this.rowHeight > 0) {
         int fullRows = viewHeight / this.rowHeight;
         return fullRows > 0 ? fullRows * this.rowHeight : viewHeight;
      } else {
         return 0;
      }
   }

   private int getEffectiveClipBottom() {
      return this.clipTop + this.getEffectiveViewHeight();
   }

   public void setContentHeight(int height) {
      this.contentHeight = Math.max(0, height);
      this.clampScrollOffset();
   }

   public int getMaxScroll() {
      int viewHeight = this.getEffectiveViewHeight();
      return Math.max(0, this.contentHeight - viewHeight);
   }

   public void setScrollOffset(int offset) {
      this.scrollOffset = this.snapToRow(Math.max(0, Math.min(offset, this.getMaxScroll())));
   }

   public void scrollByRows(int deltaRows) {
      int deltaPixels = deltaRows * this.rowHeight;
      this.setScrollOffset(this.scrollOffset + deltaPixels);
   }

   public void scrollToBottom() {
      this.setScrollOffset(this.getMaxScroll());
   }

   public void scrollToTop() {
      this.setScrollOffset(0);
   }

   private void clampScrollOffset() {
      int max = this.getMaxScroll();
      if (this.scrollOffset > max) {
         this.scrollOffset = this.snapToRow(max);
      }
   }

   private int snapToRow(int offset) {
      return this.rowHeight <= 0 ? 0 : offset / this.rowHeight * this.rowHeight;
   }

   public int getVisibleRowCount() {
      int viewHeight = this.getEffectiveViewHeight();
      return viewHeight > 0 && this.rowHeight > 0 ? (viewHeight + this.rowHeight - 1) / this.rowHeight : 0;
   }

   public int getFirstVisibleRow() {
      return this.rowHeight <= 0 ? 0 : this.scrollOffset / this.rowHeight;
   }

   public int getLastVisibleRow() {
      int viewHeight = this.getEffectiveViewHeight();
      if (viewHeight > 0 && this.rowHeight > 0) {
         int lastVisiblePixel = viewHeight - 1;
         return this.scrollOffset >= 0 && this.scrollOffset <= 2147483647 - lastVisiblePixel ? (this.scrollOffset + lastVisiblePixel) / this.rowHeight : 2147483647 / this.rowHeight;
      } else {
         return -1;
      }
   }

   public int getRowY(int rowIndex) {
      return this.y + rowIndex * this.rowHeight - this.scrollOffset;
   }

   public boolean isRowVisible(int rowY) {
      int effectiveClipBottom = this.getEffectiveClipBottom();
      return rowY + this.rowHeight > this.clipTop && rowY < effectiveClipBottom;
   }

   public int clampYToClip(int y) {
      return Math.max(this.clipTop, Math.min(y, this.getEffectiveClipBottom()));
   }

   public int clippedRowHeight(int y, int height) {
      int maxBottom = Math.min(y + height, this.getEffectiveClipBottom());
      return Math.max(0, maxBottom - y);
   }

   public ScrollableListView.Inner_DataUtil getRowBounds(int rowY) {
      if (!this.isRowVisible(rowY)) {
         return null;
      }

      int drawY = Math.max(rowY, this.clipTop);
      int drawBottom = Math.min(rowY + this.rowHeight, this.getEffectiveClipBottom());
      int drawHeight = drawBottom - drawY;
      return drawHeight <= 0 ? null : new ScrollableListView.Inner_DataUtil(this.clipLeft, drawY, this.clipRight - this.clipLeft, drawHeight);
   }

   public void beginFrame(GuiGraphicsExtractor ctx, int borderColor, int fillColor) {
      this.borderColor = borderColor;
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(this.x, this.y, this.width, this.height), fillColor);
      autismclient.gui.ui.UiClipStack.getInstance().push(ctx, autismclient.gui.f.NetHelper.init(this.clipLeft, this.clipTop, this.clipRight - this.clipLeft, this.clipBottom - this.clipTop));
   }

   public void renderRows(GuiGraphicsExtractor ctx, Font textRenderer, List<? extends ScrollableListView.Inner_CachePool> rows, ScrollableListView.Inner_StreamUtil renderer) {
      int startRow = Math.max(0, this.getFirstVisibleRow() - 1);
      int endRow = Math.min(rows.size(), startRow + this.getVisibleRowCount() + 2);

      for (int i = startRow; i < endRow; i++) {
         int rowY = this.getRowY(i);
         ScrollableListView.Inner_DataUtil bounds = this.getRowBounds(rowY);
         if (bounds != null) {
            ScrollableListView.Inner_CachePool row = rows.get(i);
            renderer.renderRow(ctx, textRenderer, row, i, bounds.x, bounds.y, bounds.width, bounds.height, rowY);
         }
      }
   }

   public void forEachVisibleRow(GuiGraphicsExtractor ctx, int totalRows, BiConsumer<Integer, ScrollableListView.Inner_DataUtil> rowRenderer) {
      int startRow = Math.max(0, this.getFirstVisibleRow() - 1);
      int endRow = Math.min(totalRows, startRow + this.getVisibleRowCount() + 2);

      for (int i = startRow; i < endRow; i++) {
         int rowY = this.getRowY(i);
         ScrollableListView.Inner_DataUtil bounds = this.getRowBounds(rowY);
         if (bounds != null) {
            rowRenderer.accept(i, bounds);
         }
      }
   }

   public void endFrame(GuiGraphicsExtractor ctx) {
      autismclient.gui.ui.UiClipStack.getInstance().pop(ctx);
      autismclient.gui.f.PacketUtil.load(ctx, autismclient.gui.f.NetHelper.init(this.x, this.y, this.width, this.height), this.borderColor);
   }

   public void renderScrollbar(GuiGraphicsExtractor ctx, double mouseX, double mouseY) {
      if (this.getMaxScroll() > 0) {
         int trackX = this.x + this.width - this.scrollbarWidth - 2;
         int trackY = this.y + 2;
         int trackWidth = this.scrollbarWidth;
         int trackHeight = this.height - 4;
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(
            this.contentHeight, this.getEffectiveViewHeight(), trackX, trackY, trackWidth, trackHeight, this.scrollOffset
         );
         boolean hovered = metrics.containsTrack(mouseX, mouseY);
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.render(ctx, metrics, hovered, this.draggingScrollbar);
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      if (!this.containsPoint(mouseX, mouseY)) {
         return false;
      }

      if (this.getMaxScroll() <= 0) {
         return false;
      }

      int deltaRows = amount > 0.0 ? -1 : 1;
      this.scrollByRows(deltaRows);
      return true;
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.getMaxScroll() <= 0) {
         return false;
      } else {
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = this.computeScrollbarMetrics();
         if (metrics == null) {
            return false;
         } else if (metrics.containsThumb(mouseX, mouseY)) {
            this.draggingScrollbar = true;
            this.scrollbarGrabOffset = (int)Math.round(mouseY) - metrics.thumbY();
            return true;
         } else if (metrics.containsTrack(mouseX, mouseY)) {
            int newScroll = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(metrics, mouseY, this.scrollbarGrabOffset);
            this.setScrollOffset(this.snapToRow(newScroll));
            this.draggingScrollbar = true;
            this.scrollbarGrabOffset = metrics.thumbHeight() / 2;
            return true;
         } else {
            return false;
         }
      }
   }

   public void mouseReleased() {
      this.draggingScrollbar = false;
      this.scrollbarGrabOffset = 0;
   }

   public void mouseDragged(double mouseX, double mouseY) {
      if (this.draggingScrollbar) {
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = this.computeScrollbarMetrics();
         if (metrics != null) {
            int newScroll = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(metrics, mouseY, this.scrollbarGrabOffset);
            this.setScrollOffset(this.snapToRow(newScroll));
         }
      }
   }

   public boolean containsPoint(double mouseX, double mouseY) {
      return mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height;
   }

   public autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics computeScrollbarMetrics() {
      if (this.getMaxScroll() <= 0) {
         return null;
      }

      int trackX = this.x + this.width - this.scrollbarWidth - 2;
      int trackY = this.y + 2;
      int trackWidth = this.scrollbarWidth;
      int trackHeight = this.height - 4;
      return autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(this.contentHeight, this.getEffectiveViewHeight(), trackX, trackY, trackWidth, trackHeight, this.scrollOffset);
   }

   public boolean isDraggingScrollbar() {
      return this.draggingScrollbar;
   }

   public boolean isOverScrollbar(double mouseX, double mouseY) {
      autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = this.computeScrollbarMetrics();
      return metrics != null && metrics.containsTrack(mouseX, mouseY);
   }

   public interface Inner_CachePool {
      String label();

      boolean isSelected();

      boolean isEnabled();
   }

   public static class Inner_DataUtil {
      public final int x;
      public final int y;
      public final int width;
      public final int height;

      public Inner_DataUtil(int x, int y, int width, int height) {
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
      }
   }

   @FunctionalInterface
   public interface Inner_StreamUtil {
      void renderRow(GuiGraphicsExtractor var1, Font var2, ScrollableListView.Inner_CachePool var3, int var4, int var5, int var6, int var7, int var8, int var9);
   }
}
