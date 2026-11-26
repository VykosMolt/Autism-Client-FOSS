package autismclient.gui.f.c;

import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class ScrollableListView {
   private final int init_I;
   private final int load;
   private final int read;
   private final int write;
   private final int update;
   private final int sync;
   private int flush = 0;
   private int close = 0;
   private boolean init_Z = false;
   private int open = 0;
   private int poll = 0;
   private final int push;
   private final int pop;
   private final int tick;
   private final int run;

   public ScrollableListView(int x, int y, int width, int height, int rowHeight, int scrollbarWidth) {
      this.init_I = x;
      this.load = y;
      this.read = Math.max(4, width);
      this.write = Math.max(4, height);
      this.update = Math.max(1, rowHeight);
      this.sync = Math.max(0, scrollbarWidth);
      this.push = this.init_I;
      this.pop = this.load;
      this.tick = this.init_I + this.read;
      this.run = this.load + this.write;
   }

   public int init_I() {
      return this.init_I;
   }

   public int load_I() {
      return this.load;
   }

   public int read_I() {
      return this.read;
   }

   public int write_I() {
      return this.write;
   }

   public int update() {
      return this.read - this.sync - 3;
   }

   public int sync() {
      return this.read - 2;
   }

   public int flush() {
      return this.update;
   }

   public int close() {
      return this.flush;
   }

   public int open() {
      return this.close;
   }

   private int run() {
      return Math.max(0, this.run - this.pop);
   }

   private int get() {
      int viewHeight = this.run();
      if (viewHeight > 0 && this.update > 0) {
         int fullRows = viewHeight / this.update;
         return fullRows > 0 ? fullRows * this.update : viewHeight;
      } else {
         return 0;
      }
   }

   private int set() {
      return this.pop + this.get();
   }

   public void init_V(int height) {
      this.flush = Math.max(0, height);
      this.write_V();
   }

   public int poll() {
      int viewHeight = this.get();
      return Math.max(0, this.flush - viewHeight);
   }

   public void load_V(int offset) {
      this.close = this.read_I(Math.max(0, Math.min(offset, this.poll())));
   }

   public void read_V(int deltaRows) {
      int deltaPixels = deltaRows * this.update;
      this.load_V(this.close + deltaPixels);
   }

   public void init_V() {
      this.load_V(this.poll());
   }

   public void load_V() {
      this.load_V(0);
   }

   private void write_V() {
      int max = this.poll();
      if (this.close > max) {
         this.close = this.read_I(max);
      }
   }

   private int read_I(int offset) {
      return this.update <= 0 ? 0 : offset / this.update * this.update;
   }

   public int push() {
      int viewHeight = this.get();
      return viewHeight > 0 && this.update > 0 ? (viewHeight + this.update - 1) / this.update : 0;
   }

   public int pop() {
      return this.update <= 0 ? 0 : this.close / this.update;
   }

   public int tick() {
      int viewHeight = this.get();
      if (viewHeight > 0 && this.update > 0) {
         int lastVisiblePixel = viewHeight - 1;
         return this.close >= 0 && this.close <= 2147483647 - lastVisiblePixel ? (this.close + lastVisiblePixel) / this.update : 2147483647 / this.update;
      } else {
         return -1;
      }
   }

   public int init_I(int rowIndex) {
      return this.load + rowIndex * this.update - this.close;
   }

   public boolean init_Z(int rowY) {
      int effectiveClipBottom = this.set();
      return rowY + this.update > this.pop && rowY < effectiveClipBottom;
   }

   public int load_I(int y) {
      return Math.max(this.pop, Math.min(y, this.set()));
   }

   public int init_I_I_I(int y, int height) {
      int maxBottom = Math.min(y + height, this.set());
      return Math.max(0, maxBottom - y);
   }

   public ScrollableListView.Inner_DataUtil init_EventBus_DataUtil(int rowY) {
      if (!this.init_Z(rowY)) {
         return null;
      }

      int drawY = Math.max(rowY, this.pop);
      int drawBottom = Math.min(rowY + this.update, this.set());
      int drawHeight = drawBottom - drawY;
      return drawHeight <= 0 ? null : new ScrollableListView.Inner_DataUtil(this.push, drawY, this.tick - this.push, drawHeight);
   }

   public void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor ctx, int borderColor, int fillColor) {
      this.poll = borderColor;
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(this.init_I, this.load, this.read, this.write), fillColor);
      autismclient.gui.f.UiClipStack.init_HashUtil().init(ctx, autismclient.gui.f.NetHelper.init(this.push, this.pop, this.tick - this.push, this.run - this.pop));
   }

   public void init(GuiGraphicsExtractor ctx, Font textRenderer, List<? extends ScrollableListView.Inner_CachePool> rows, ScrollableListView.Inner_StreamUtil renderer) {
      int startRow = Math.max(0, this.pop() - 1);
      int endRow = Math.min(rows.size(), startRow + this.push() + 2);

      for (int i = startRow; i < endRow; i++) {
         int rowY = this.init_I(i);
         ScrollableListView.Inner_DataUtil bounds = this.init_EventBus_DataUtil(rowY);
         if (bounds != null) {
            ScrollableListView.Inner_CachePool row = rows.get(i);
            renderer.init(ctx, textRenderer, row, i, bounds.init, bounds.load, bounds.read, bounds.write, rowY);
         }
      }
   }

   public void init_V_GuiGraphicsExtract_3(GuiGraphicsExtractor ctx, int totalRows, BiConsumer<Integer, ScrollableListView.Inner_DataUtil> rowRenderer) {
      int startRow = Math.max(0, this.pop() - 1);
      int endRow = Math.min(totalRows, startRow + this.push() + 2);

      for (int i = startRow; i < endRow; i++) {
         int rowY = this.init_I(i);
         ScrollableListView.Inner_DataUtil bounds = this.init_EventBus_DataUtil(rowY);
         if (bounds != null) {
            rowRenderer.accept(i, bounds);
         }
      }
   }

   public void init(GuiGraphicsExtractor ctx) {
      autismclient.gui.f.UiClipStack.init_HashUtil().init(ctx);
      autismclient.gui.f.PacketUtil.load(ctx, autismclient.gui.f.NetHelper.init(this.init_I, this.load, this.read, this.write), this.poll);
   }

   public void init_V_GuiGraphicsExtract(GuiGraphicsExtractor ctx, double mouseX, double mouseY) {
      if (this.poll() > 0) {
         int trackX = this.init_I + this.read - this.sync - 2;
         int trackY = this.load + 2;
         int trackWidth = this.sync;
         int trackHeight = this.write - 4;
         autismclient.gui.f.b.ScrollbarMetricsHelper.Inner_DataUtil metrics = autismclient.gui.f.b.ScrollbarMetricsHelper.init_PoolEntry_DataUtil_I_2(
            this.flush, this.get(), trackX, trackY, trackWidth, trackHeight, this.close
         );
         boolean hovered = metrics.init(mouseX, mouseY);
         autismclient.gui.f.b.ScrollbarMetricsHelper.init(ctx, metrics, hovered, this.init_Z);
      }
   }

   public boolean init_Z_D(double mouseX, double mouseY, double amount) {
      if (!this.init_Z_D_D(mouseX, mouseY)) {
         return false;
      }

      if (this.poll() <= 0) {
         return false;
      }

      int deltaRows = amount > 0.0 ? -1 : 1;
      this.read_V(deltaRows);
      return true;
   }

   public boolean init_Z_D_2(double mouseX, double mouseY, int button) {
      if (this.poll() <= 0) {
         return false;
      } else {
         autismclient.gui.f.b.ScrollbarMetricsHelper.Inner_DataUtil metrics = this.init_PoolEntry_DataUtil();
         if (metrics == null) {
            return false;
         } else if (metrics.load(mouseX, mouseY)) {
            this.init_Z = true;
            this.open = (int)Math.round(mouseY) - metrics.update();
            return true;
         } else if (metrics.init(mouseX, mouseY)) {
            int newScroll = autismclient.gui.f.b.ScrollbarMetricsHelper.init(metrics, mouseY, this.open);
            this.load_V(this.read_I(newScroll));
            this.init_Z = true;
            this.open = metrics.sync() / 2;
            return true;
         } else {
            return false;
         }
      }
   }

   public void read_V() {
      this.init_Z = false;
      this.open = 0;
   }

   public void init_V_D_D(double mouseX, double mouseY) {
      if (this.init_Z) {
         autismclient.gui.f.b.ScrollbarMetricsHelper.Inner_DataUtil metrics = this.init_PoolEntry_DataUtil();
         if (metrics != null) {
            int newScroll = autismclient.gui.f.b.ScrollbarMetricsHelper.init(metrics, mouseY, this.open);
            this.load_V(this.read_I(newScroll));
         }
      }
   }

   public boolean init_Z_D_D(double mouseX, double mouseY) {
      return mouseX >= this.init_I && mouseX < this.init_I + this.read && mouseY >= this.load && mouseY < this.load + this.write;
   }

   public autismclient.gui.f.b.ScrollbarMetricsHelper.Inner_DataUtil init_PoolEntry_DataUtil() {
      if (this.poll() <= 0) {
         return null;
      }

      int trackX = this.init_I + this.read - this.sync - 2;
      int trackY = this.load + 2;
      int trackWidth = this.sync;
      int trackHeight = this.write - 4;
      return autismclient.gui.f.b.ScrollbarMetricsHelper.init_PoolEntry_DataUtil_I_2(this.flush, this.get(), trackX, trackY, trackWidth, trackHeight, this.close);
   }

   public boolean init_Z() {
      return this.init_Z;
   }

   public boolean load(double mouseX, double mouseY) {
      autismclient.gui.f.b.ScrollbarMetricsHelper.Inner_DataUtil metrics = this.init_PoolEntry_DataUtil();
      return metrics != null && metrics.init(mouseX, mouseY);
   }

   public interface Inner_CachePool {
      String init_Str();

      boolean init_Z();

      boolean load();
   }

   public static class Inner_DataUtil {
      public final int init;
      public final int load;
      public final int read;
      public final int write;

      public Inner_DataUtil(int x, int y, int width, int height) {
         this.init = x;
         this.load = y;
         this.read = width;
         this.write = height;
      }
   }

   @FunctionalInterface
   public interface Inner_StreamUtil {
      void init(GuiGraphicsExtractor var1, Font var2, ScrollableListView.Inner_CachePool var3, int var4, int var5, int var6, int var7, int var8, int var9);
   }
}
