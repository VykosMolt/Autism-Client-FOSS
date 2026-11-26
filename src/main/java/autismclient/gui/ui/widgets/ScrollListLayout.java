package autismclient.gui.ui.widgets;

import java.util.function.BiConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class ScrollListLayout {
   private ScrollListLayout() {
   }

   public static ScrollListLayout.ListLayout init(
      int x, int y, int width, int height, int rowCount, int rowHeight, int rowStep, int scrollOffset, int scrollbarWidth, int scrollbarGutter
   ) {
      return init(x, y, width, height, rowCount, rowHeight, rowStep, scrollOffset, scrollbarWidth, scrollbarGutter, 2, 5, true);
   }

   public static ScrollListLayout.ListLayout init(
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
      return init(x, y, width, height, rowCount, rowHeight, rowStep, scrollOffset, scrollbarWidth, scrollbarGutter, contentInset, 5, true);
   }

   public static ScrollListLayout.ListLayout init(
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
      int viewHeight = alignRows ? init(innerHeight, safeRowStep) : innerHeight;
      int contentHeight = Math.max(0, rowCount) * safeRowStep;
      int maxScroll = Math.max(0, contentHeight - viewHeight);
      int clampedScroll = Math.max(0, Math.min(scrollOffset, maxScroll));
      ScrollbarMetricsHelper.Metrics scrollbar = ScrollbarMetricsHelper.init_PoolEntry_DataUtil_I_2(
         contentHeight,
         viewHeight,
         x + safeWidth - Math.max(1, scrollbarRightInset),
         y + safeInset,
         Math.max(1, scrollbarWidth),
         Math.max(1, safeHeight - safeInset * 2),
         clampedScroll
      );
      int contentWidth = Math.max(1, safeWidth - safeInset * 2 - (scrollbar.init_Z() ? Math.max(0, scrollbarGutter) : 0));
      return new ScrollListLayout.ListLayout(
         x, y, safeWidth, safeHeight, safeRowHeight, safeRowStep, viewHeight, contentHeight, maxScroll, clampedScroll, contentWidth, safeInset, scrollbar
      );
   }

   private static int init(int innerHeight, int rowStep) {
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
      int init_I,
      int load,
      int read,
      int write,
      int update,
      int sync,
      int flush,
      int close,
      int open,
      int poll,
      int push,
      int pop,
      ScrollbarMetricsHelper.Metrics init_PoolEntry_DataUtil
   ) {
      public void init(GuiGraphicsExtractor graphics, boolean focused) {
         ListRowRenderer.init(graphics, this.init_I, this.load, this.read, this.write, focused);
      }

      public void init(GuiGraphicsExtractor graphics) {
         autismclient.gui.ui.layout.LayoutViewport.init_RingBuffer_F(1.0F)
            .init(graphics, this.init_I + this.pop, this.load + this.pop, this.init_I + this.pop + this.push, this.load + this.pop + this.flush);
      }

      public void load(GuiGraphicsExtractor graphics) {
         autismclient.gui.ui.layout.LayoutViewport.init_RingBuffer_F(1.0F).read(graphics);
      }

      public void init(int rowCount, BiConsumer<Integer, Integer> consumer) {
         int firstVisible = this.poll / this.sync;
         int rowY = this.load + this.pop - this.poll % this.sync;
         int rowBottom = this.load + this.pop + this.flush;

         for (int index = firstVisible; index < rowCount && rowY < rowBottom; rowY += this.sync) {
            if (rowY + this.update > this.load + this.pop) {
               consumer.accept(index, rowY);
            }

            index++;
         }
      }

      public void init(GuiGraphicsExtractor graphics, double mouseX, double mouseY, boolean dragging) {
         ScrollbarMetricsHelper.init(graphics, this.init_PoolEntry_DataUtil, this.init_PoolEntry_DataUtil.init(mouseX, mouseY), dragging);
      }
   }
}
