package autismclient.util;

public final class PkgCls_f {
   private static boolean init_Z;
   private static boolean load_Z;
   private static boolean read;
   private static int init_I;
   private static int load_I;

   private PkgCls_f() {
   }

   public static void init(int visible, int occluded, boolean armor) {
      init_Z = true;
      load_Z = false;
      read = armor;
      init_I = visible;
      load_I = occluded;
   }

   public static boolean init_Z() {
      return read;
   }

   public static void init_V() {
      init_Z = false;
   }

   public static boolean load_Z() {
      return init_Z;
   }

   public static boolean read() {
      if (load_Z) {
         return false;
      }

      load_Z = true;
      return true;
   }

   public static int init_I() {
      return init_I;
   }

   public static int load_I() {
      return load_I;
   }
}
