package autismclient.e;

public final class DataUtil {
   private DataUtil() {
   }

   public static String init(int[] var0) {
      char[] var1 = new char[var0.length - 1];
      int var2 = var0[0];

      for (int var3 = 1; var3 < var0.length; var3++) {
         var1[var3 - 1] = (char)(var0[var3] ^ var2);
         var2 = var2 * 31 + var3;
      }

      return new String(var1);
   }
}
