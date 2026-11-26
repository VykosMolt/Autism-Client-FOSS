package autismclient.gui.ui.widgets;

import java.util.List;
import autismclient.gui.f.b.k;

public final class TooltipRenderer {
   private static final int init = 4;
   private static final int load = 4;
   private static final int read = 3;
   private static final int write = 8;
   private static final int update = 10;

   private TooltipRenderer() {
   }

   public static void init(autismclient.gui.f.SyncManager context, String text, int mouseX, int mouseY) {
      init(context, text, mouseX, mouseY, 220);
   }

   public static void init(autismclient.gui.f.SyncManager context, String text, int mouseX, int mouseY, int preferredMaxWidth) {
      if (text != null && !text.isBlank()) {
         int lineHeight = Math.max(10, k.init(autismclient.gui.f.a.DataUtil.init) + 2);
         int availableW = Math.max(1, context.init_I() - 8);
         int availableH = Math.max(1, context.load_I() - 8);
         int maxTextWidth = Math.max(1, Math.min(Math.max(32, preferredMaxWidth), Math.max(1, availableW - 8)));
         List<String> lines = context.init_PoolEntry().wrapLines(text, maxTextWidth);
         int maxLineWidth = 0;

         for (String line : lines) {
            maxLineWidth = Math.max(maxLineWidth, context.init_PoolEntry().measureWidth(line));
         }

         int w = Math.min(availableW, Math.max(1, maxLineWidth + 8));
         int maxLines = Math.max(1, Math.min(lines.size(), Math.max(1, (availableH - 6) / lineHeight)));
         int h = Math.min(availableH, Math.max(1, maxLines * lineHeight + 6));
         int x = mouseX + 8;
         int y = mouseY + 8;
         if (x + w > context.init_I() - 4) {
            x = mouseX - w - 8;
         }

         if (y + h > context.load_I() - 4) {
            y = mouseY - h - 8;
         }

         autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(
            init(x, 4, Math.max(4, context.init_I() - 4 - w)), init(y, 4, Math.max(4, context.load_I() - 4 - h)), w, h
         );
         autismclient.gui.f.PacketUtil.init(context, bounds, context.init_TableIndex().colors().popupSurface, context.init_TableIndex().colors().borderSubtle);

         for (int i = 0; i < maxLines; i++) {
            context.init_PoolEntry()
               .draw(
                  context.init_GuiGraphicsExtract(),
                  lines.get(i),
                  bounds.read() + 4,
                  bounds.write() + 3 + i * lineHeight,
                  context.init_TableIndex().colors().textPrimary
               );
         }
      }
   }

   private static int init(int value, int min, int max) {
      return Math.max(min, Math.min(value, max));
   }
}
