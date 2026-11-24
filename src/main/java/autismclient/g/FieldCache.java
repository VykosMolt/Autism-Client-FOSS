package autismclient.g;

public final class FieldCache {
   private static volatile long init;

   private FieldCache() {
   }

   public static void init_V() {
      init = Math.max(init, System.currentTimeMillis() + 15000L);
   }

   public static boolean init_Z() {
      return System.currentTimeMillis() < init;
   }

   public static void load() {
      init = 0L;
   }
}
