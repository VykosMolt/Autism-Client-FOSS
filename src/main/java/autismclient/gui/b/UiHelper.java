package autismclient.gui.b;

import autismclient.gui.f.PacketUtil;
import autismclient.gui.f.b.k;
import autismclient.util.cA;
import autismclient.util.ct;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class UiHelper {
   private static final int init = 190;
   private static final int load = -267386864;
   private static final int read = 1347420320;
   private static final int write = -1;

   private UiHelper() {
   }

   public static void init(GuiGraphicsExtractor graphics, Font font, String text, int mouseX, int mouseY) {
      if (graphics != null && font != null && text != null && !text.isBlank()) {
         List<String> lines = init(font, text, 190);
         if (!lines.isEmpty()) {
            int width = 0;

            for (String line : lines) {
               width = Math.max(width, k.init(font, line, autismclient.gui.f.a.DataUtil.init, -1));
            }

            int height = lines.size() * 10 - 2;
            int screenWidth = Math.max(1, cA.init_I());
            int screenHeight = Math.max(1, cA.load_I());
            int x = mouseX + 10;
            int y = mouseY - 12;
            if (x + width + 4 > screenWidth) {
               x = Math.max(4, mouseX - width - 12);
            }

            if (y + height + 4 > screenHeight) {
               y = Math.max(4, screenHeight - height - 4);
            }

            if (y < 4) {
               y = 4;
            }

            graphics.nextStratum();
            PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x - 3, y - 3, width + 6, height + 6), -267386864);
            PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x - 3, y - 3, 2, height + 6), ct.init_I_I_ct_DataUtil(-50373, ct.Inner_DataUtil.init));
            int lineY = y;

            for (String line : lines) {
               k.init(graphics, font, line, autismclient.gui.f.a.DataUtil.init, -1, x, lineY, true);
               lineY += 10;
            }
         }
      }
   }

   public static boolean init(int x, int y, int w, int h, int mouseX, int mouseY) {
      return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
   }

   private static List<String> init(Font font, String text, int maxWidth) {
      List<String> out = new ArrayList<>();
      StringBuilder line = new StringBuilder();

      for (String word : text.split(" ")) {
         String candidate = line.isEmpty() ? word : line + " " + word;
         if (k.init(font, candidate, autismclient.gui.f.a.DataUtil.init, -1) > maxWidth && !line.isEmpty()) {
            out.add(line.toString());
            line = new StringBuilder(word);
         } else {
            line = new StringBuilder(candidate);
         }
      }

      if (!line.isEmpty()) {
         out.add(line.toString());
      }

      return out;
   }
}
