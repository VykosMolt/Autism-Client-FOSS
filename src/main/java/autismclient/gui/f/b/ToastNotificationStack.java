package autismclient.gui.f.b;

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
   private final List<ToastNotificationStack.Inner_DataUtil> init_List = new CopyOnWriteArrayList<>();
   private volatile boolean init_Z;
   private final long load_J;
   private final float read_F;
   private final float write_F;
   private final int update;
   private final int sync;
   private final int flush;

   public ToastNotificationStack() {
      this(1800L, 140.0F, 220.0F, 4, 3, 15);
   }

   public ToastNotificationStack(long lifetimeMs, float enterMs, float exitMs, int maxVisible, int gap, int height) {
      this.load_J = Math.max(1L, lifetimeMs);
      this.read_F = Math.max(1.0F, enterMs);
      this.write_F = Math.max(1.0F, exitMs);
      this.update = Math.max(1, maxVisible);
      this.sync = Math.max(0, gap);
      this.flush = Math.max(1, height);
   }

   public void init(String message, int accentColor) {
      if (message != null && !message.isBlank()) {
         long nowNanos = System.nanoTime();
         this.init_V_J(nowNanos);
         if (this.init_List.size() >= this.update) {
            this.init_List.remove(0);
         }

         this.init_List.add(new ToastNotificationStack.Inner_DataUtil(message, nowNanos, accentColor));
         this.init_Z = true;
      }
   }

   public boolean init_Z() {
      if (!this.init_Z) {
         return false;
      } else {
         this.init_V_J(System.nanoTime());
         if (this.init_List.isEmpty()) {
            this.init_Z = false;
            return false;
         } else {
            return true;
         }
      }
   }

   public int init_I() {
      if (!this.init_Z) {
         return 0;
      }

      this.init_V_J(System.nanoTime());
      if (this.init_List.isEmpty()) {
         this.init_Z = false;
      }

      return this.init_List.size();
   }

   public void init_V() {
      this.init_List.clear();
      this.init_Z = false;
   }

   public void init(autismclient.gui.f.SyncManager context, int anchorX, int anchorY, int anchorWidth) {
      if (context != null && anchorWidth > 0) {
         long nowNanos = System.nanoTime();
         this.init_V_J(nowNanos);
         if (this.init_List.isEmpty()) {
            this.init_Z = false;
         } else {
            int maxToastWidth = Math.max(1, Math.min(anchorWidth, 260));
            int padX = 6;
            int lineHeight = 10;
            int verticalPad = Math.max(4, this.flush - lineHeight);
            int maxTextWidth = Math.max(1, maxToastWidth - padX * 2);
            int y = anchorY;

            for (int i = this.init_List.size() - 1; i >= 0; i--) {
               ToastNotificationStack.Inner_DataUtil toast = this.init_List.get(i);
               float ageMs = Math.max(0.0F, (float)(nowNanos - toast.init_J()) / 1000000.0F);
               float alpha = Math.min(init_F_F(ageMs / this.read_F), init_F_F(((float)this.load_J - ageMs) / this.write_F));
               if (!(alpha <= 0.001F)) {
                  List<String> lines = context.init_PoolEntry().init_List(toast.init_Str(), maxTextWidth);
                  int widest = 0;

                  for (String line : lines) {
                     widest = Math.max(widest, context.init_PoolEntry().init(line));
                  }

                  int toastWidth = Math.min(maxToastWidth, Math.max(Math.min(48, maxToastWidth), widest + padX * 2));
                  int textBlockHeight = lines.size() * lineHeight;
                  int toastHeight = Math.max(this.flush, textBlockHeight + verticalPad);
                  int drawX = anchorX + Math.max(0, (anchorWidth - toastWidth) / 2);
                  float slide = init_F_F(ageMs / this.read_F);
                  float eased = 1.0F - (1.0F - slide) * (1.0F - slide) * (1.0F - slide);
                  int drawY = y - Math.round((1.0F - eased) * (toastHeight + this.sync + 2));
                  autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(drawX, drawY, toastWidth, toastHeight);
                  autismclient.gui.f.UiColorScheme colors = context.init_TableIndex().init_JsonUtil();
                  int fill = init(colors.update & 16777215, toast.init_I(), 0.22F);
                  autismclient.gui.f.PacketUtil.load(
                     context.init_GuiGraphicsExtract(),
                     bounds,
                     5,
                     autismclient.gui.f.PacketUtil.init(-704643072 | fill, alpha),
                     autismclient.gui.f.PacketUtil.init(toast.init_I(), alpha)
                  );
                  int textColor = autismclient.gui.f.PacketUtil.init(colors.prev, alpha);
                  int textTop = drawY + Math.max(2, (toastHeight - textBlockHeight + 1) / 2 + 1);

                  for (int li = 0; li < lines.size(); li++) {
                     String line = lines.get(li);
                     int lineX = drawX + Math.max(padX, (toastWidth - context.init_PoolEntry().init(line)) / 2);
                     context.init_PoolEntry().init_V_GuiGraphicsExtract_2(context.init_GuiGraphicsExtract(), line, lineX, textTop + li * lineHeight, textColor);
                  }

                  y += toastHeight + this.sync;
               }
            }
         }
      }
   }

   private void init_V_J(long nowNanos) {
      this.init_List.removeIf(toast -> (nowNanos - toast.init_J()) / 1000000L >= this.load_J);
   }

   private static float init_F_F(float value) {
      return Math.max(0.0F, Math.min(1.0F, value));
   }

   private static int init(int baseRgb, int accentColor, float t) {
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

   private record Inner_DataUtil(String init_Str, long init_J, int init_I) {
   }
}
