package autismclient.gui.f.b;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class ScrollbarMetricsHelper {
   public static final int init = 6;

   private ScrollbarMetricsHelper() {
   }

   public static ScrollbarMetricsHelper.Inner_DataUtil init_PoolEntry_DataUtil_I_2(
      int contentPixels, int viewPixels, int trackX, int trackY, int trackWidth, int trackHeight, int scrollOffset
   ) {
      return init_PoolEntry_DataUtil_I(contentPixels, viewPixels, trackX, trackY, trackWidth, trackHeight, scrollOffset);
   }

   public static ScrollbarMetricsHelper.Inner_DataUtil init_PoolEntry_DataUtil_I(
      int contentPixels, int viewPixels, int trackX, int trackY, int trackWidth, int trackHeight, float scrollOffset
   ) {
      int right = trackX + trackWidth;
      int var14 = 6;
      trackX = right - var14;
      if (contentPixels > 0 && viewPixels > 0 && trackHeight > 0) {
         int maxScroll = Math.max(0, contentPixels - viewPixels);
         if (maxScroll <= 0) {
            return new ScrollbarMetricsHelper.Inner_DataUtil(trackX, trackY, var14, trackHeight, trackY, trackHeight, 0);
         }

         int thumbHeight = Math.max(12, (int)Math.round(trackHeight * ((double)viewPixels / Math.max(viewPixels, contentPixels))));
         thumbHeight = Math.min(trackHeight, thumbHeight);
         int travel = Math.max(1, trackHeight - thumbHeight);
         float clampedScroll = Math.max(0.0F, Math.min(scrollOffset, maxScroll));
         int thumbY = trackY + Math.round(clampedScroll / maxScroll * travel);
         return new ScrollbarMetricsHelper.Inner_DataUtil(trackX, trackY, var14, trackHeight, thumbY, thumbHeight, maxScroll);
      } else {
         return new ScrollbarMetricsHelper.Inner_DataUtil(trackX, trackY, var14, trackHeight, trackY, trackHeight, 0);
      }
   }

   public static void init(GuiGraphicsExtractor context, ScrollbarMetricsHelper.Inner_DataUtil metrics, boolean hovered, boolean dragging) {
      if (metrics != null && metrics.init_Z()) {
         ScrollbarRenderer.init(
            autismclient.gui.f.UiHelper.init(context, Minecraft.getInstance().font, -1, -1),
            new ScrollbarRenderer.Inner_DataUtil(
               autismclient.gui.f.NetHelper.init(metrics.init_I(), metrics.load(), metrics.read(), metrics.write()),
               autismclient.gui.f.NetHelper.init(metrics.init_I(), metrics.update(), metrics.read(), metrics.sync()),
               metrics.flush()
            ),
            hovered,
            dragging
         );
      }
   }

   public static int init(ScrollbarMetricsHelper.Inner_DataUtil metrics, double mouseY, int grabOffset) {
      if (metrics != null && metrics.init_Z()) {
         int travel = Math.max(1, metrics.write() - metrics.sync());
         int thumbTop = (int)Math.round(mouseY) - grabOffset;
         thumbTop = Math.max(metrics.load(), Math.min(metrics.load() + travel, thumbTop));
         float progress = (float)(thumbTop - metrics.load()) / travel;
         return Math.max(0, Math.min(metrics.flush(), Math.round(progress * metrics.flush())));
      } else {
         return 0;
      }
   }

   public record Inner_DataUtil(int init, int load, int read, int write, int update, int sync, int flush) {
      public boolean init_Z() {
         return this.flush > 0;
      }

      public boolean init(double mouseX, double mouseY) {
         return mouseX >= this.init && mouseX < this.init + this.read && mouseY >= this.load && mouseY < this.load + this.write;
      }

      public boolean load(double mouseX, double mouseY) {
         return this.init_Z() && mouseX >= this.init && mouseX < this.init + this.read && mouseY >= this.update && mouseY < this.update + this.sync;
      }

      public int init_I() {
         return this.init;
      }
   }
}
