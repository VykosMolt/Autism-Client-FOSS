package autismclient.gui.ui.widgets;

import java.util.List;

public final class StatusToastCardRenderer {
   private static final int init = 5;
   private static final int load = 3;
   private static final int read = 4;

   private StatusToastCardRenderer() {
   }

   public static int init(autismclient.gui.f.SyncManager context, boolean hasDetail) {
      int headerHeight = 14;
      int lineHeight = 9;
      return headerHeight + 3 + lineHeight + (hasDetail ? 3 + lineHeight : 0) + 4;
   }

   public static int init(autismclient.gui.f.SyncManager context, int width, String message, String detail) {
      int maxTextWidth = Math.max(1, width - 10);
      int lineHeight = 9;
      List<String> messageLines = context.init_PoolEntry().wrapLines(message, maxTextWidth);
      List<String> detailLines = detail != null && !detail.isEmpty() ? context.init_PoolEntry().wrapLines(detail, maxTextWidth) : List.of();
      return 17 + Math.max(1, messageLines.size()) * lineHeight + (detailLines.isEmpty() ? 0 : 3 + detailLines.size() * lineHeight) + 4;
   }

   public static int init(autismclient.gui.f.SyncManager context, int width, String message, String detail, int percent) {
      return init(context, width, message, detail) + (percent >= 0 ? 7 : 0);
   }

   public static void init(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper requestedBounds, String title, String message, String detail) {
      init(context, requestedBounds, title, message, detail, context.init_TableIndex().colors().textPrimary);
   }

   public static void init(
      autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper requestedBounds, String title, String message, String detail, int titleColor
   ) {
      init(context, requestedBounds, title, message, detail, titleColor, -1);
   }

   public static void init(
      autismclient.gui.f.SyncManager context,
      autismclient.gui.f.NetHelper requestedBounds,
      String title,
      String message,
      String detail,
      int titleColor,
      int percent
   ) {
      autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().colors();
      autismclient.gui.ui.UiTextRenderer text = context.init_PoolEntry();
      boolean hasDetail = detail != null && !detail.isEmpty();
      boolean dockedToTop = requestedBounds.write() <= 0;
      int margin = 4;
      int width = Math.max(1, Math.min(requestedBounds.update(), Math.max(1, context.init_I() - margin * 2)));
      int height = init(context, width, message, detail, percent);
      int x = Math.max(margin, Math.min(requestedBounds.read(), Math.max(margin, context.init_I() - margin - width)));
      int y = dockedToTop ? 0 : Math.max(margin, Math.min(requestedBounds.write(), Math.max(margin, context.load_I() - margin - height)));
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(x, y, width, height);
      int headerHeight = 14;
      int radius = 5;
      if (dockedToTop) {
         autismclient.gui.f.PacketUtil.read(context.init_GuiGraphicsExtract(), bounds, radius, colors.windowSurface, colors.borderSubtle);
         int half = Math.round(height / 2.0F);
         context.init_GuiGraphicsExtract().fill(x, y, x + width, y + half, colors.borderSubtle);
         context.init_GuiGraphicsExtract().fill(x + 1, y, x + width - 1, y + half, colors.windowSurface);
         autismclient.gui.f.PacketUtil.init(
            context.init_GuiGraphicsExtract(),
            autismclient.gui.f.NetHelper.init(x + 1, y + 1, Math.max(0, width - 2), Math.max(0, headerHeight - 1)),
            colors.headerSurface
         );
      } else {
         autismclient.gui.f.PacketUtil.read(context.init_GuiGraphicsExtract(), bounds, radius, colors.windowSurface, colors.borderSubtle);
         autismclient.gui.f.PacketUtil.read(
            context.init_GuiGraphicsExtract(),
            autismclient.gui.f.NetHelper.init(x + 1, y + 1, Math.max(0, width - 2), Math.max(0, headerHeight - 1)),
            Math.max(1, radius - 1),
            colors.headerSurface
         );
      }

      autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract_2(context.init_GuiGraphicsExtract(), x + 1, y + headerHeight, Math.max(0, width - 2), colors.divider);
      int maxTextWidth = Math.max(0, width - 10);
      int titleY = text.centerTextY(autismclient.gui.f.NetHelper.init(x, y, width, headerHeight));
      int messageY = y + headerHeight + 3;
      String percentText = percent >= 0 ? percent + "%" : "";
      int percentWidth = percentText.isEmpty() ? 0 : text.measureWidth(percentText);
      int titleMaxWidth = Math.max(0, maxTextWidth - (percentWidth > 0 ? percentWidth + 5 : 0));
      text.drawFitted(context.init_GuiGraphicsExtract(), title, x + 5, titleY, titleMaxWidth, titleColor);
      if (percentWidth > 0 && percentWidth <= maxTextWidth) {
         text.draw(context.init_GuiGraphicsExtract(), percentText, x + width - 5 - percentWidth, titleY, colors.textPrimary);
      }

      List<String> messageLines = text.wrapLines(message, maxTextWidth);

      for (int i = 0; i < messageLines.size(); i++) {
         text.draw(context.init_GuiGraphicsExtract(), messageLines.get(i), x + 5, messageY + i * 9, colors.textPrimary);
      }

      if (hasDetail) {
         int detailY = messageY + messageLines.size() * 9 + 3;
         List<String> detailLines = text.wrapLines(detail, maxTextWidth);

         for (int i = 0; i < detailLines.size(); i++) {
            text.draw(context.init_GuiGraphicsExtract(), detailLines.get(i), x + 5, detailY + i * 9, colors.textSecondary);
         }
      }

      if (percent >= 0 && maxTextWidth > 0) {
         ProgressBarRenderer.draw(context, autismclient.gui.f.NetHelper.init(x + 5, bounds.load() - 4 - 4, maxTextWidth, 4), percent / 100.0);
      }
   }
}
