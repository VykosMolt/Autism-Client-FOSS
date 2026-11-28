package autismclient.gui.ui;

public final class UiAlphaStack {
   private static final int init_I = 16;
   private static final float[] STACK = new float[16];
   private static int depth;
   private static float current = 1.0F;

   private UiAlphaStack() {
   }

   public static float current() {
      return current;
   }

   public static void push(float alpha) {
      if (depth < 16) {
         STACK[depth] = current;
      }

      depth++;
      current = current * Math.max(0.0F, Math.min(1.0F, alpha));
   }

   public static void pop() {
      if (depth > 0) {
         depth--;
         current = depth < 16 ? STACK[depth] : current;
         if (depth == 0) {
            current = 1.0F;
         }
      }
   }

   public static int depth() {
      return depth;
   }

   public static void popTo(int target) {
      while (depth > Math.max(0, target)) {
         pop();
      }
   }

   public static int apply(int argb) {
      float a = current;
      if (a >= 1.0F) {
         return argb;
      }

      int alpha = argb >>> 24 & 0xFF;
      return alpha == 0 ? argb : Math.round(alpha * a) << 24 | argb & 16777215;
   }
}
