package autismclient.util;

import java.security.SecureRandom;
import java.util.Locale;

public final class JoinSecretKeyHelper {
   private JoinSecretKeyHelper() {
   }

   public static String init(String name) {
      if (name == null) {
         return "";
      }

      String value = name.trim().toLowerCase(Locale.ROOT);
      if (value.startsWith("secret.")) {
         value = value.substring("secret.".length());
      }

      return value.matches("[a-z0-9_.-]{1,64}") ? value : "";
   }

   public static String generateRandomKey() {
      SecureRandom random = new SecureRandom();
      String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
      int length = 9 + random.nextInt(8);
      StringBuilder out = new StringBuilder(length);

      for (int i = 0; i < length; i++) {
         out.append(alphabet.charAt(random.nextInt(alphabet.length())));
      }

      return out.toString();
   }
}
