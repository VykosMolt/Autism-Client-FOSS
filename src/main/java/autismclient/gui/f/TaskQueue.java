package autismclient.gui.f;

public final class TaskQueue {
   private static final int init_I = 16;
   private static final float[] init_FArr = new float[16];
   private static int load;
   private static float init_F = 1.0F;

   private TaskQueue() {
   }

   public static float init_F() {
      return init_F;
   }

   public static void init_V_F(float alpha) {
      if (load < 16) {
         init_FArr[load] = init_F;
      }

      load++;
      init_F = init_F * Math.max(0.0F, Math.min(1.0F, alpha));
   }

   public static void init_V() {
      if (load > 0) {
         load--;
         init_F = load < 16 ? init_FArr[load] : init_F;
         if (load == 0) {
            init_F = 1.0F;
         }
      }
   }

   public static int init_I() {
      return load;
   }

   public static void init_V_I(int target) {
      while (load > Math.max(0, target)) {
         init_V();
      }
   }

   public static int init_I_I(int argb) {
      float a = init_F;
      if (a >= 1.0F) {
         return argb;
      }

      int alpha = argb >>> 24 & 0xFF;
      return alpha == 0 ? argb : Math.round(alpha * a) << 24 | argb & 16777215;
   }
}
