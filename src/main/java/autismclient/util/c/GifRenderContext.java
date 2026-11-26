package autismclient.util.c;

public final class GifRenderContext {
   private static int init_I;
   private static String init_Str;
   private static int load;

   private GifRenderContext() {
   }

   public static void init(String name) {
      init_I++;
      init_Str = name;
   }

   public static void init_V() {
      if (init_I > 0) {
         init_I--;
      }

      if (init_I == 0) {
         init_Str = null;
      }
   }

   public static String init_Str() {
      return init_I > 0 ? init_Str : null;
   }

   public static void load() {
      load++;
   }

   public static void read() {
      if (load > 0) {
         load--;
      }
   }

   public static boolean init_Z() {
      return load > 0;
   }
}
