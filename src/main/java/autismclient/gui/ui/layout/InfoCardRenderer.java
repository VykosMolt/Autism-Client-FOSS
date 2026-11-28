package autismclient.gui.ui.layout;

import autismclient.gui.ui.widgets.UiLayoutMath;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public final class InfoCardRenderer {
   private static final autismclient.gui.f.b.CharEncoder THEME = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = 3;

   private InfoCardRenderer() {
   }

   public static int drawCard(
      GuiGraphicsExtractor context, Font textRenderer, int x, int y, int width, String title, List<InfoCardRenderer.Inner_DataUtil> rows, int accentColor
   ) {
      String titleTrimmed = k.init(textRenderer, title, width - 12, THEME.init_Identifier_l(l.load), THEME.load(l.read));
      ArrayList<InfoCardRenderer.Inner_DataUtil> trimmedRows = new ArrayList<>(rows.size());

      for (InfoCardRenderer.Inner_DataUtil row : rows) {
         trimmedRows.add(
            new InfoCardRenderer.Inner_DataUtil(
               k.init(textRenderer, row.text(), width - 12, row.font(), row.color()), row.font(), row.color()
            )
         );
      }

      return drawBorderedCard(context, textRenderer, x, y, width, titleTrimmed, trimmedRows, accentColor);
   }

   public static int height(int rowCount) {
      int headerHeight = Math.max(THEME.load(), THEME.init_I_l_I(l.load, 2));
      int rowHeight = THEME.init_I_l_I(l.read, 2);
      int contentHeight = Math.max(rowHeight, rowCount * rowHeight);
      return headerHeight + 6 + contentHeight;
   }

   public static int drawBorderedCard(
      GuiGraphicsExtractor context, Font textRenderer, int x, int y, int width, String title, List<InfoCardRenderer.Inner_DataUtil> rows, int accentColor
   ) {
      return drawCard(context, textRenderer, x, y, width, title, rows, accentColor, 0, true, true);
   }

   public static int drawCard(
      GuiGraphicsExtractor context,
      Font textRenderer,
      int x,
      int y,
      int width,
      String title,
      List<InfoCardRenderer.Inner_DataUtil> rows,
      int accentColor,
      int minHeight,
      boolean leftBorder,
      boolean rightBorder
   ) {
      return render(context, textRenderer, x, y, width, title, rows, accentColor, minHeight, leftBorder, rightBorder, true, true);
   }

   public static int render(
      GuiGraphicsExtractor context,
      Font textRenderer,
      int x,
      int y,
      int width,
      String title,
      List<InfoCardRenderer.Inner_DataUtil> rows,
      int accentColor,
      int minHeight,
      boolean leftBorder,
      boolean rightBorder,
      boolean topBorder,
      boolean bottomBorder
   ) {
      int headerHeight = Math.max(THEME.load(), THEME.init_I_l_I(l.load, 2));
      int rowHeight = THEME.init_I_l_I(l.read, 2);
      int contentHeight = Math.max(rowHeight, rows.size() * rowHeight);
      int height = Math.max(minHeight, headerHeight + 6 + contentHeight);
      autismclient.gui.f.PacketUtil.init(context, autismclient.gui.f.NetHelper.init(x, y, width, height), THEME.poll());
      autismclient.gui.f.PacketUtil.init(context, autismclient.gui.f.NetHelper.init(x + 1, y + 1, width - 2, headerHeight - 1), THEME.pop());
      if (topBorder) {
         autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract_2(context, x, y, width, accentColor);
      }

      if (bottomBorder) {
         autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract_2(context, x, y + height - 1, width, THEME.get());
      }

      if (leftBorder) {
         autismclient.gui.f.PacketUtil.load(context, x, y, height, THEME.get());
      }

      if (rightBorder) {
         autismclient.gui.f.PacketUtil.load(context, x + width - 1, y, height, THEME.get());
      }

      autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract_2(context, x + 1, y + 1, width - 2, 620756991);
      int padX = 6;
      int titleY = UiLayoutMath.centerTextY(y, headerHeight, THEME.init_I_l(l.load), THEME.flush());
      k.init(context, textRenderer, title, THEME.init_Identifier_l(l.load), THEME.load(l.read), x + padX, titleY, false);
      int rowY = y + headerHeight + 3;

      for (InfoCardRenderer.Inner_DataUtil row : rows) {
         k.init(
            context,
            textRenderer,
            row.text(),
            row.font(),
            row.color(),
            x + padX,
            UiLayoutMath.centerTextY(rowY, rowHeight, THEME.init_I_l(l.read), THEME.flush()),
            false
         );
         rowY += rowHeight;
      }

      return height;
   }

   public record Inner_DataUtil(String text, Identifier font, int color) {
      public static InfoCardRenderer.Inner_DataUtil coloredRow(String text, int color) {
         return new InfoCardRenderer.Inner_DataUtil(text, InfoCardRenderer.THEME.init_Identifier_l(l.read), color);
      }

      public static InfoCardRenderer.Inner_DataUtil plainRow(String text) {
         return new InfoCardRenderer.Inner_DataUtil(text, InfoCardRenderer.THEME.init_Identifier_l(l.write), InfoCardRenderer.THEME.load(l.write));
      }
   }
}
