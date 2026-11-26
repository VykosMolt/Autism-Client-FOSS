package autismclient.gui.f.b;

import java.util.Collection;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.Identifier;

public final class j {
   private j() {
   }

   public static float init_F_F(float value, float min, float max) {
      float safeMax = Math.max(min, max);
      return Math.max(min, Math.min(value, safeMax));
   }

   public static float init_F_Font(Font renderer, String text, Identifier fontId, int color, float horizontalPadding, float minWidth, float maxWidth) {
      float textWidth = k.init(renderer, text == null ? "" : text, fontId, color);
      return init_F_F(textWidth + horizontalPadding * 2.0F, minWidth, maxWidth);
   }

   public static int init_I_Font(Font renderer, String text, Identifier fontId, int color, int horizontalPadding, int minWidth, int maxWidth) {
      return Math.round(init_F_Font(renderer, text, fontId, color, horizontalPadding, minWidth, maxWidth));
   }

   public static int init(Font renderer, Identifier fontId, int color, Collection<String> values) {
      if (renderer != null && values != null && !values.isEmpty()) {
         int widest = 0;

         for (String value : values) {
            widest = Math.max(widest, k.init(renderer, value == null ? "" : value, fontId, color));
         }

         return widest;
      } else {
         return 0;
      }
   }

   public static int init_I_I_2(int outerStart, int outerSize, int innerSize) {
      return outerStart + Math.max(0, (outerSize - innerSize) / 2);
   }

   public static int load(int outerStart, int outerSize, int innerSize) {
      return init_I_I_2(outerStart, outerSize, innerSize);
   }

   public static int init(int boxY, int boxHeight, int fontHeight, int offset) {
      autismclient.gui.e.ConfigStore face = autismclient.gui.e.ConfigStore.BODY;
      if (face.init_StreamUtil() != null) {
         return Math.round(autismclient.gui.e.SyncManager.init(face, boxY, boxHeight, autismclient.gui.e.SyncManager.init_F_ConfigStore(face)));
      }

      int spareHeight = Math.max(0, boxHeight - fontHeight);
      return boxY + (spareHeight + 1) / 2 + offset;
   }

   public static int init_I_I(int from, int to, float t) {
      float clamped = Math.max(0.0F, Math.min(1.0F, t));
      int a1 = from >>> 24 & 0xFF;
      int r1 = from >>> 16 & 0xFF;
      int g1 = from >>> 8 & 0xFF;
      int b1 = from & 0xFF;
      int a2 = to >>> 24 & 0xFF;
      int r2 = to >>> 16 & 0xFF;
      int g2 = to >>> 8 & 0xFF;
      int b2 = to & 0xFF;
      int a = Math.round(a1 + (a2 - a1) * clamped);
      int r = Math.round(r1 + (r2 - r1) * clamped);
      int g = Math.round(g1 + (g2 - g1) * clamped);
      int b = Math.round(b1 + (b2 - b1) * clamped);
      return a << 24 | r << 16 | g << 8 | b;
   }
}
