package autismclient.util.gif;

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

   public static void popEntity() {
      if (init_I > 0) {
         init_I--;
      }

      if (init_I == 0) {
         init_Str = null;
      }
   }

   public static String currentName() {
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

   public static boolean isShieldRendering() {
      return load > 0;
   }
}
