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
   private static final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = 3;

   private InfoCardRenderer() {
   }

   public static int init(
      GuiGraphicsExtractor context, Font textRenderer, int x, int y, int width, String title, List<InfoCardRenderer.Inner_DataUtil> rows, int accentColor
   ) {
      String titleTrimmed = k.init(textRenderer, title, width - 12, init_CharEncoder.init_Identifier_l(l.load), init_CharEncoder.load(l.read));
      ArrayList<InfoCardRenderer.Inner_DataUtil> trimmedRows = new ArrayList<>(rows.size());

      for (InfoCardRenderer.Inner_DataUtil row : rows) {
         trimmedRows.add(
            new InfoCardRenderer.Inner_DataUtil(
               k.init(textRenderer, row.init_Str(), width - 12, row.init_Identifier(), row.init_I()), row.init_Identifier(), row.init_I()
            )
         );
      }

      return load(context, textRenderer, x, y, width, titleTrimmed, trimmedRows, accentColor);
   }

   public static int height(int rowCount) {
      int headerHeight = Math.max(init_CharEncoder.load(), init_CharEncoder.init_I_l_I(l.load, 2));
      int rowHeight = init_CharEncoder.init_I_l_I(l.read, 2);
      int contentHeight = Math.max(rowHeight, rowCount * rowHeight);
      return headerHeight + 6 + contentHeight;
   }

   public static int load(
      GuiGraphicsExtractor context, Font textRenderer, int x, int y, int width, String title, List<InfoCardRenderer.Inner_DataUtil> rows, int accentColor
   ) {
      return init(context, textRenderer, x, y, width, title, rows, accentColor, 0, true, true);
   }

   public static int init(
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
      int headerHeight = Math.max(init_CharEncoder.load(), init_CharEncoder.init_I_l_I(l.load, 2));
      int rowHeight = init_CharEncoder.init_I_l_I(l.read, 2);
      int contentHeight = Math.max(rowHeight, rows.size() * rowHeight);
      int height = Math.max(minHeight, headerHeight + 6 + contentHeight);
      autismclient.gui.f.PacketUtil.init(context, autismclient.gui.f.NetHelper.init(x, y, width, height), init_CharEncoder.poll());
      autismclient.gui.f.PacketUtil.init(context, autismclient.gui.f.NetHelper.init(x + 1, y + 1, width - 2, headerHeight - 1), init_CharEncoder.pop());
      if (topBorder) {
         autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract_2(context, x, y, width, accentColor);
      }

      if (bottomBorder) {
         autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract_2(context, x, y + height - 1, width, init_CharEncoder.get());
      }

      if (leftBorder) {
         autismclient.gui.f.PacketUtil.load(context, x, y, height, init_CharEncoder.get());
      }

      if (rightBorder) {
         autismclient.gui.f.PacketUtil.load(context, x + width - 1, y, height, init_CharEncoder.get());
      }

      autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract_2(context, x + 1, y + 1, width - 2, 620756991);
      int padX = 6;
      int titleY = UiLayoutMath.centerTextY(y, headerHeight, init_CharEncoder.init_I_l(l.load), init_CharEncoder.flush());
      k.init(context, textRenderer, title, init_CharEncoder.init_Identifier_l(l.load), init_CharEncoder.load(l.read), x + padX, titleY, false);
      int rowY = y + headerHeight + 3;

      for (InfoCardRenderer.Inner_DataUtil row : rows) {
         k.init(
            context,
            textRenderer,
            row.init_Str(),
            row.init_Identifier(),
            row.init_I(),
            x + padX,
            UiLayoutMath.centerTextY(rowY, rowHeight, init_CharEncoder.init_I_l(l.read), init_CharEncoder.flush()),
            false
         );
         rowY += rowHeight;
      }

      return height;
   }

   public record Inner_DataUtil(String init_Str, Identifier init_Identifier, int init_I) {
      public static InfoCardRenderer.Inner_DataUtil init(String text, int color) {
         return new InfoCardRenderer.Inner_DataUtil(text, InfoCardRenderer.init_CharEncoder.init_Identifier_l(l.read), color);
      }

      public static InfoCardRenderer.Inner_DataUtil init(String text) {
         return new InfoCardRenderer.Inner_DataUtil(text, InfoCardRenderer.init_CharEncoder.init_Identifier_l(l.write), InfoCardRenderer.init_CharEncoder.load(l.write));
      }
   }
}
