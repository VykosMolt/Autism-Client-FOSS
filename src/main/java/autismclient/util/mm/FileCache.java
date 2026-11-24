package autismclient.util.mm;

public final class FileCache {
   public static final int init_I = -13330213;
   public static final int load_I = -13330213;
   private static final int[] init_IArr = new int[]{-41892, -65536};
   private static final int[] load_IArr = new int[]{-11751984, -5047809};
   private static final int[] read = new int[]{-2849731, -8560};
   private static final int[] write = new int[]{-16718337, -12910848};
   private static final int[] update = new int[]{-3597588, -15384386};

   private FileCache() {
   }

   private static int[] init_IArr(String role) {
      int[] var10000;
      switch (role == null ? "" : role) {
         case "god":
            var10000 = init_IArr;
            break;
         case "admin":
            var10000 = load_IArr;
            break;
         case "gold":
            var10000 = read;
            break;
         case "aqua":
            var10000 = write;
            break;
         case "notbroke":
            var10000 = update;
            break;
         default:
            var10000 = null;
      }

      return var10000;
   }

   public static boolean init_Z(String role) {
      return init_IArr(role) != null;
   }

   public static int init_I(String role) {
      int[] g = init_IArr(role);
      if (g != null) {
         return g[0];
      } else {
         return "blue".equals(role) ? -13330213 : -13330213;
      }
   }

   public static int init_I_Str(String role, int index, int length) {
      int[] g = init_IArr(role);
      if (g == null) {
         return init_I(role);
      }

      if (length <= 1) {
         return g[0];
      }

      float t = Math.max(0.0F, Math.min(1.0F, (float)index / (length - 1)));
      return init_I_I(g[0], g[1], t);
   }

   static int init_I_I(int a, int b, float t) {
      int ar = a >> 16 & 0xFF;
      int ag = a >> 8 & 0xFF;
      int ab = a & 0xFF;
      int br = b >> 16 & 0xFF;
      int bg = b >> 8 & 0xFF;
      int bb = b & 0xFF;
      int r = Math.round(ar + (br - ar) * t);
      int g = Math.round(ag + (bg - ag) * t);
      int bl = Math.round(ab + (bb - ab) * t);
      return 0xFF000000 | r << 16 | g << 8 | bl;
   }
}
