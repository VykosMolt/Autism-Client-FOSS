package autismclient.util;

public final class bL {
   private static volatile int init = -2147483648;

   private bL() {
   }

   public static void init_V() {
      init = ck.init_ck().state_I();
   }

   public static boolean init_Z() {
      return init == ck.init_ck().state_I();
   }
}
