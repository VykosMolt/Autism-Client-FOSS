package autismclient.gui.ui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class ScrollbarMetricsHelper {
   public static final int TRACK_WIDTH = 6;

   private ScrollbarMetricsHelper() {
   }

   public static ScrollbarMetricsHelper.Metrics compute(
      int contentPixels, int viewPixels, int trackX, int trackY, int trackWidth, int trackHeight, int scrollOffset
   ) {
      return computeFloat(contentPixels, viewPixels, trackX, trackY, trackWidth, trackHeight, scrollOffset);
   }

   public static ScrollbarMetricsHelper.Metrics computeFloat(
      int contentPixels, int viewPixels, int trackX, int trackY, int trackWidth, int trackHeight, float scrollOffset
   ) {
      int right = trackX + trackWidth;
      int var14 = 6;
      trackX = right - var14;
      if (contentPixels > 0 && viewPixels > 0 && trackHeight > 0) {
         int maxScroll = Math.max(0, contentPixels - viewPixels);
         if (maxScroll <= 0) {
            return new ScrollbarMetricsHelper.Metrics(trackX, trackY, var14, trackHeight, trackY, trackHeight, 0);
         }

         int thumbHeight = Math.max(12, (int)Math.round(trackHeight * ((double)viewPixels / Math.max(viewPixels, contentPixels))));
         thumbHeight = Math.min(trackHeight, thumbHeight);
         int travel = Math.max(1, trackHeight - thumbHeight);
         float clampedScroll = Math.max(0.0F, Math.min(scrollOffset, maxScroll));
         int thumbY = trackY + Math.round(clampedScroll / maxScroll * travel);
         return new ScrollbarMetricsHelper.Metrics(trackX, trackY, var14, trackHeight, thumbY, thumbHeight, maxScroll);
      } else {
         return new ScrollbarMetricsHelper.Metrics(trackX, trackY, var14, trackHeight, trackY, trackHeight, 0);
      }
   }

   public static void render(GuiGraphicsExtractor context, ScrollbarMetricsHelper.Metrics metrics, boolean hovered, boolean dragging) {
      if (metrics != null && metrics.isScrollable()) {
         ScrollbarRenderer.init(
            autismclient.gui.f.UiHelper.init(context, Minecraft.getInstance().font, -1, -1),
            new ScrollbarRenderer.ScrollbarBounds(
               autismclient.gui.f.NetHelper.init(metrics.trackX(), metrics.trackY(), metrics.trackWidth(), metrics.trackHeight()),
               autismclient.gui.f.NetHelper.init(metrics.trackX(), metrics.thumbY(), metrics.trackWidth(), metrics.thumbHeight()),
               metrics.maxScroll()
            ),
            hovered,
            dragging
         );
      }
   }

   public static int scrollOffsetForMouseY(ScrollbarMetricsHelper.Metrics metrics, double mouseY, int grabOffset) {
      if (metrics != null && metrics.isScrollable()) {
         int travel = Math.max(1, metrics.trackHeight() - metrics.thumbHeight());
         int thumbTop = (int)Math.round(mouseY) - grabOffset;
         thumbTop = Math.max(metrics.trackY(), Math.min(metrics.trackY() + travel, thumbTop));
         float progress = (float)(thumbTop - metrics.trackY()) / travel;
         return Math.max(0, Math.min(metrics.maxScroll(), Math.round(progress * metrics.maxScroll())));
      } else {
         return 0;
      }
   }

   public record Metrics(int trackX, int trackY, int trackWidth, int trackHeight, int thumbY, int thumbHeight, int maxScroll) {
      public boolean isScrollable() {
         return this.maxScroll > 0;
      }

      public boolean containsTrack(double mouseX, double mouseY) {
         return mouseX >= this.trackX && mouseX < this.trackX + this.trackWidth && mouseY >= this.trackY && mouseY < this.trackY + this.trackHeight;
      }

      public boolean containsThumb(double mouseX, double mouseY) {
         return this.isScrollable() && mouseX >= this.trackX && mouseX < this.trackX + this.trackWidth && mouseY >= this.thumbY && mouseY < this.thumbY + this.thumbHeight;
      }

      public int trackX() {
         return this.trackX;
      }
   }
}
