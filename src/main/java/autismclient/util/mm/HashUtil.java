package autismclient.util.mm;

public final class HashUtil {
   private HashUtil() {
   }

   public static String init(String s, int maxLen) {
      if (s == null) {
         return "";
      }

      StringBuilder sb = new StringBuilder(Math.min(s.length(), maxLen));

      for (int i = 0; i < s.length() && sb.length() < maxLen; i++) {
         char c = s.charAt(i);
         if (c != 167 && c >= ' ' && c != 127) {
            sb.append(c);
         }
      }

      return sb.toString();
   }

   public static String init(String s) {
      return init(s, 256);
   }
}
