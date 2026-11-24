package autismclient.gui.f.c;

import autismclient.gui.f.b.j;
import autismclient.gui.f.b.l;
import net.minecraft.client.gui.Font;

public final class NetHelper {
   private NetHelper() {
   }

   public static int init(int panelWidth, int horizontalPadding) {
      return Math.max(0, panelWidth - horizontalPadding * 2);
   }

   public static int init(int width, boolean hasScroll, int gutter) {
      return Math.max(0, width - (hasScroll ? gutter : 0));
   }

   public static int init(Font renderer, autismclient.gui.f.b.CharEncoder theme, l tone, String label, int horizontalPadding, int minWidth, int maxWidth) {
      return renderer != null && theme != null
         ? j.init_I_Font(
            renderer,
            label == null ? "" : label,
            theme.init_Identifier_l(tone == null ? l.read : tone),
            theme.load(tone == null ? l.read : tone),
            horizontalPadding,
            minWidth,
            maxWidth
         )
         : Math.max(minWidth, 0);
   }

   public static int init(int leftX, int totalWidth, int childWidth, int rightInset) {
      return leftX + Math.max(0, totalWidth - childWidth - rightInset);
   }

   public static int load(int startY, int rowHeight, int gap, int index) {
      return startY + Math.max(0, index) * (rowHeight + Math.max(0, gap));
   }

   public static int init(int screenSize, int margin, int preferredSize) {
      int available = Math.max(1, screenSize - Math.max(0, margin) * 2);
      return Math.max(1, Math.min(preferredSize, available));
   }

   public static int load(int screenSize, int panelSize, int margin) {
      int safeMargin = Math.max(0, margin);
      return Math.max(safeMargin, Math.min((screenSize - panelSize) / 2, Math.max(safeMargin, screenSize - safeMargin - panelSize)));
   }
}
