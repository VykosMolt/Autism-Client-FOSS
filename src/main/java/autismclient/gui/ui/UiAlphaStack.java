package autismclient.gui.ui;

public final class UiAlphaStack {
   private static final int init_I = 16;
   private static final float[] init_FArr = new float[16];
   private static int load;
   private static float init_F = 1.0F;

   private UiAlphaStack() {
   }

   public static float current() {
      return init_F;
   }

   public static void push(float alpha) {
      if (load < 16) {
         init_FArr[load] = init_F;
      }

      load++;
      init_F = init_F * Math.max(0.0F, Math.min(1.0F, alpha));
   }

   public static void pop() {
      if (load > 0) {
         load--;
         init_F = load < 16 ? init_FArr[load] : init_F;
         if (load == 0) {
            init_F = 1.0F;
         }
      }
   }

   public static int depth() {
      return load;
   }

   public static void popTo(int target) {
      while (load > Math.max(0, target)) {
         pop();
      }
   }

   public static int apply(int argb) {
      float a = init_F;
      if (a >= 1.0F) {
         return argb;
      }

      int alpha = argb >>> 24 & 0xFF;
      return alpha == 0 ? argb : Math.round(alpha * a) << 24 | argb & 16777215;
   }
}
