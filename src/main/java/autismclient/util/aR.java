package autismclient.util;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public final class aR {
   public static final int init = 36;

   private aR() {
   }

   public static int init(int textWidth) {
      return textWidth + 36;
   }

   public static float init_F_I(int textWidth, int clipWidth, long nowMs, long holdUntilMs, int speedPxPerSec) {
      return textWidth > clipWidth && nowMs >= holdUntilMs ? (float)((nowMs - holdUntilMs) * speedPxPerSec / 1000.0 % init(textWidth)) : 0.0F;
   }

   public static void init(
      GuiGraphicsExtractor graphics,
      Font font,
      String text,
      int color,
      int x,
      int y,
      int clipWidth,
      boolean shadow,
      long nowMs,
      long holdUntilMs,
      int speedPxPerSec
   ) {
      init(graphics, font, text, autismclient.gui.f.a.DataUtil.init, color, x, y, clipWidth, shadow, nowMs, holdUntilMs, speedPxPerSec);
   }

   public static void init(
      GuiGraphicsExtractor graphics,
      Font font,
      String text,
      Identifier fontId,
      int color,
      int x,
      int y,
      int clipWidth,
      boolean shadow,
      long nowMs,
      long holdUntilMs,
      int speedPxPerSec
   ) {
      String safe = text == null ? "" : text;
      if (!safe.isEmpty() && clipWidth > 0) {
         init(
            graphics,
            font,
            safe,
            fontId,
            color,
            x,
            y,
            clipWidth,
            shadow,
            nowMs,
            holdUntilMs,
            speedPxPerSec,
            autismclient.gui.f.b.k.init(font, safe, fontId, color)
         );
      }
   }

   public static void init(
      GuiGraphicsExtractor graphics,
      Font font,
      String text,
      Identifier fontId,
      int color,
      int x,
      int y,
      int clipWidth,
      boolean shadow,
      long nowMs,
      long holdUntilMs,
      int speedPxPerSec,
      int textWidth
   ) {
      String safe = text == null ? "" : text;
      if (!safe.isEmpty() && clipWidth > 0) {
         if (textWidth <= clipWidth) {
            autismclient.gui.f.b.k.init(graphics, font, safe, fontId, color, x, y, shadow);
         } else {
            float offset = init_F_I(textWidth, clipWidth, nowMs, holdUntilMs, speedPxPerSec);
            autismclient.gui.f.HashUtil.init_HashUtil().init(graphics, autismclient.gui.f.NetHelper.init(x, y, clipWidth, 11));
            graphics.pose().pushMatrix();

            try {
               graphics.pose().translate(-offset, 0.0F);
               autismclient.gui.f.b.k.init(graphics, font, safe, fontId, color, x, y, shadow);
               autismclient.gui.f.b.k.init(graphics, font, safe, fontId, color, x + init(textWidth), y, shadow);
            } finally {
               graphics.pose().popMatrix();
               autismclient.gui.f.HashUtil.init_HashUtil().init(graphics);
            }
         }
      }
   }

   public static double init_D_D(double anchorSec, long anchorAtMs, boolean playing, double durationSec, long nowMs) {
      double position = anchorSec;
      if (playing && durationSec > 0.0) {
         position += (nowMs - anchorAtMs) / 1000.0;
      }

      return durationSec > 0.0 ? Math.max(0.0, Math.min(durationSec, position)) : Math.max(0.0, position);
   }

   public static String init(cq.Inner_ConfigStore snapshot) {
      if (snapshot != null && snapshot.load_Str() != null && !snapshot.load_Str().isBlank()) {
         String title = snapshot.load_Str().trim();
         String artist = snapshot.init_Str() == null ? "" : snapshot.init_Str().trim();
         return artist.isEmpty() ? title : artist + " — " + title;
      } else {
         return "";
      }
   }

   public static final class Inner_DataUtil {
      private final long init_J;
      private final String init_Str;
      private final Object init_Obj;
      private final int init_I;

      public Inner_DataUtil(long stamp, String mode, Object font, int metricsGeneration) {
         this.init_J = stamp;
         this.init_Str = mode == null ? "" : mode;
         this.init_Obj = font;
         this.init_I = metricsGeneration;
      }

      public boolean init(long stamp, String mode, Object font, int metricsGeneration) {
         return this.init_J == stamp && this.init_I == metricsGeneration && this.init_Obj == font && this.init_Str.equals(mode == null ? "" : mode);
      }
   }
}
