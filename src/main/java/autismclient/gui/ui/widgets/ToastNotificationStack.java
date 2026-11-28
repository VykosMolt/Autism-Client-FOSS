package autismclient.gui.ui.widgets;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ToastNotificationStack {
   private static final long init_J = 1800L;
   private static final float init_F = 140.0F;
   private static final float load_F = 220.0F;
   private static final int init_I = 4;
   private static final int load_I = 3;
   private static final int read_I = 15;
   private static final int write_I = 10;
   private final List<ToastNotificationStack.Toast> toasts = new CopyOnWriteArrayList<>();
   private volatile boolean init_Z;
   private final long lifetimeMs;
   private final float enterMs;
   private final float exitMs;
   private final int maxVisible;
   private final int gap;
   private final int toastHeight;

   public ToastNotificationStack() {
      this(1800L, 140.0F, 220.0F, 4, 3, 15);
   }

   public ToastNotificationStack(long lifetimeMs, float enterMs, float exitMs, int maxVisible, int gap, int height) {
      this.lifetimeMs = Math.max(1L, lifetimeMs);
      this.enterMs = Math.max(1.0F, enterMs);
      this.exitMs = Math.max(1.0F, exitMs);
      this.maxVisible = Math.max(1, maxVisible);
      this.gap = Math.max(0, gap);
      this.toastHeight = Math.max(1, height);
   }

   public void init(String message, int accentColor) {
      if (message != null && !message.isBlank()) {
         long nowNanos = System.nanoTime();
         this.expire(nowNanos);
         if (this.toasts.size() >= this.maxVisible) {
            this.toasts.remove(0);
         }

         this.toasts.add(new ToastNotificationStack.Toast(message, nowNanos, accentColor));
         this.init_Z = true;
      }
   }

   public boolean hasToasts() {
      if (!this.init_Z) {
         return false;
      } else {
         this.expire(System.nanoTime());
         if (this.toasts.isEmpty()) {
            this.init_Z = false;
            return false;
         } else {
            return true;
         }
      }
   }

   public int count() {
      if (!this.init_Z) {
         return 0;
      }

      this.expire(System.nanoTime());
      if (this.toasts.isEmpty()) {
         this.init_Z = false;
      }

      return this.toasts.size();
   }

   public void clear() {
      this.toasts.clear();
      this.init_Z = false;
   }

   public void init(autismclient.gui.f.SyncManager context, int anchorX, int anchorY, int anchorWidth) {
      if (context != null && anchorWidth > 0) {
         long nowNanos = System.nanoTime();
         this.expire(nowNanos);
         if (this.toasts.isEmpty()) {
            this.init_Z = false;
         } else {
            int maxToastWidth = Math.max(1, Math.min(anchorWidth, 260));
            int padX = 6;
            int lineHeight = 10;
            int verticalPad = Math.max(4, this.toastHeight - lineHeight);
            int maxTextWidth = Math.max(1, maxToastWidth - padX * 2);
            int y = anchorY;

            for (int i = this.toasts.size() - 1; i >= 0; i--) {
               ToastNotificationStack.Toast toast = this.toasts.get(i);
               float ageMs = Math.max(0.0F, (float)(nowNanos - toast.createdAt()) / 1000000.0F);
               float alpha = Math.min(clamp01(ageMs / this.enterMs), clamp01(((float)this.lifetimeMs - ageMs) / this.exitMs));
               if (!(alpha <= 0.001F)) {
                  List<String> lines = context.init_PoolEntry().wrapLines(toast.text(), maxTextWidth);
                  int widest = 0;

                  for (String line : lines) {
                     widest = Math.max(widest, context.init_PoolEntry().measureWidth(line));
                  }

                  int toastWidth = Math.min(maxToastWidth, Math.max(Math.min(48, maxToastWidth), widest + padX * 2));
                  int textBlockHeight = lines.size() * lineHeight;
                  int toastHeight = Math.max(this.toastHeight, textBlockHeight + verticalPad);
                  int drawX = anchorX + Math.max(0, (anchorWidth - toastWidth) / 2);
                  float slide = clamp01(ageMs / this.enterMs);
                  float eased = 1.0F - (1.0F - slide) * (1.0F - slide) * (1.0F - slide);
                  int drawY = y - Math.round((1.0F - eased) * (toastHeight + this.gap + 2));
                  autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(drawX, drawY, toastWidth, toastHeight);
                  autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().colors();
                  int fill = blendColor(colors.windowSurface & 16777215, toast.color(), 0.22F);
                  autismclient.gui.f.PacketUtil.load(
                     context.init_GuiGraphicsExtract(),
                     bounds,
                     5,
                     autismclient.gui.f.PacketUtil.init(-704643072 | fill, alpha),
                     autismclient.gui.f.PacketUtil.init(toast.color(), alpha)
                  );
                  int textColor = autismclient.gui.f.PacketUtil.init(colors.textPrimary, alpha);
                  int textTop = drawY + Math.max(2, (toastHeight - textBlockHeight + 1) / 2 + 1);

                  for (int li = 0; li < lines.size(); li++) {
                     String line = lines.get(li);
                     int lineX = drawX + Math.max(padX, (toastWidth - context.init_PoolEntry().measureWidth(line)) / 2);
                     context.init_PoolEntry().draw(context.init_GuiGraphicsExtract(), line, lineX, textTop + li * lineHeight, textColor);
                  }

                  y += toastHeight + this.gap;
               }
            }
         }
      }
   }

   private void expire(long nowNanos) {
      this.toasts.removeIf(toast -> (nowNanos - toast.createdAt()) / 1000000L >= this.lifetimeMs);
   }

   private static float clamp01(float value) {
      return Math.max(0.0F, Math.min(1.0F, value));
   }

   private static int blendColor(int baseRgb, int accentColor, float t) {
      int r = baseRgb >> 16 & 0xFF;
      int g = baseRgb >> 8 & 0xFF;
      int b = baseRgb & 0xFF;
      int ar = accentColor >> 16 & 0xFF;
      int ag = accentColor >> 8 & 0xFF;
      int ab = accentColor & 0xFF;
      r += Math.round((ar - r) * t);
      g += Math.round((ag - g) * t);
      b += Math.round((ab - b) * t);
      return r << 16 | g << 8 | b;
   }

   private record Toast(String text, long createdAt, int color) {
   }
}
