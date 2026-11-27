package autismclient.gui.ui.widgets;

import java.util.function.BiConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class ScrollListLayout {
   private ScrollListLayout() {
   }

   public static ScrollListLayout.ListLayout compute(
      int x, int y, int width, int height, int rowCount, int rowHeight, int rowStep, int scrollOffset, int scrollbarWidth, int scrollbarGutter
   ) {
      return compute(x, y, width, height, rowCount, rowHeight, rowStep, scrollOffset, scrollbarWidth, scrollbarGutter, 2, 5, true);
   }

   public static ScrollListLayout.ListLayout compute(
      int x,
      int y,
      int width,
      int height,
      int rowCount,
      int rowHeight,
      int rowStep,
      int scrollOffset,
      int scrollbarWidth,
      int scrollbarGutter,
      int contentInset
   ) {
      return compute(x, y, width, height, rowCount, rowHeight, rowStep, scrollOffset, scrollbarWidth, scrollbarGutter, contentInset, 5, true);
   }

   public static ScrollListLayout.ListLayout compute(
      int x,
      int y,
      int width,
      int height,
      int rowCount,
      int rowHeight,
      int rowStep,
      int scrollOffset,
      int scrollbarWidth,
      int scrollbarGutter,
      int contentInset,
      int scrollbarRightInset,
      boolean alignRows
   ) {
      int safeWidth = Math.max(1, width);
      int safeHeight = Math.max(1, height);
      int safeRowHeight = Math.max(1, rowHeight);
      int safeRowStep = Math.max(1, rowStep);
      int safeInset = Math.max(0, contentInset);
      int innerHeight = Math.max(1, safeHeight - safeInset * 2);
      int viewHeight = alignRows ? alignViewHeight(innerHeight, safeRowStep) : innerHeight;
      int contentHeight = Math.max(0, rowCount) * safeRowStep;
      int maxScroll = Math.max(0, contentHeight - viewHeight);
      int clampedScroll = Math.max(0, Math.min(scrollOffset, maxScroll));
      ScrollbarMetricsHelper.Metrics scrollbar = ScrollbarMetricsHelper.compute(
         contentHeight,
         viewHeight,
         x + safeWidth - Math.max(1, scrollbarRightInset),
         y + safeInset,
         Math.max(1, scrollbarWidth),
         Math.max(1, safeHeight - safeInset * 2),
         clampedScroll
      );
      int contentWidth = Math.max(1, safeWidth - safeInset * 2 - (scrollbar.isScrollable() ? Math.max(0, scrollbarGutter) : 0));
      return new ScrollListLayout.ListLayout(
         x, y, safeWidth, safeHeight, safeRowHeight, safeRowStep, viewHeight, contentHeight, maxScroll, clampedScroll, contentWidth, safeInset, scrollbar
      );
   }

   private static int alignViewHeight(int innerHeight, int rowStep) {
      int safeHeight = Math.max(0, innerHeight);
      int safeStep = Math.max(1, rowStep);
      if (safeHeight != 0 && safeStep > 1) {
         int aligned = safeHeight / safeStep * safeStep;
         return aligned > 0 ? aligned : Math.min(safeHeight, safeStep);
      } else {
         return safeHeight;
      }
   }

   public record ListLayout(
      int x,
      int y,
      int width,
      int height,
      int rowHeight,
      int rowStep,
      int viewHeight,
      int contentHeight,
      int maxScroll,
      int clampedScroll,
      int contentWidth,
      int contentInset,
      ScrollbarMetricsHelper.Metrics scrollbar
   ) {
      public void drawFrame(GuiGraphicsExtractor graphics, boolean focused) {
         ListRowRenderer.drawListFrame(graphics, this.x, this.y, this.width, this.height, focused);
      }

      public void pushContentClip(GuiGraphicsExtractor graphics) {
         autismclient.gui.ui.layout.LayoutViewport.create(1.0F)
            .pushClip(graphics, this.x + this.contentInset, this.y + this.contentInset, this.x + this.contentInset + this.contentWidth, this.y + this.contentInset + this.viewHeight);
      }

      public void popContentClip(GuiGraphicsExtractor graphics) {
         autismclient.gui.ui.layout.LayoutViewport.create(1.0F).popClip(graphics);
      }

      public void forEachVisibleRow(int rowCount, BiConsumer<Integer, Integer> consumer) {
         int firstVisible = this.clampedScroll / this.rowStep;
         int rowY = this.y + this.contentInset - this.clampedScroll % this.rowStep;
         int rowBottom = this.y + this.contentInset + this.viewHeight;

         for (int index = firstVisible; index < rowCount && rowY < rowBottom; rowY += this.rowStep) {
            if (rowY + this.rowHeight > this.y + this.contentInset) {
               consumer.accept(index, rowY);
            }

            index++;
         }
      }

      public void renderScrollbar(GuiGraphicsExtractor graphics, double mouseX, double mouseY, boolean dragging) {
         ScrollbarMetricsHelper.render(graphics, this.scrollbar, this.scrollbar.containsTrack(mouseX, mouseY), dragging);
      }
   }
}
