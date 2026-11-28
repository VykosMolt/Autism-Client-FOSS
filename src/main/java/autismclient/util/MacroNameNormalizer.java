package autismclient.util;

import java.util.Locale;

public final class MacroNameNormalizer {
   private MacroNameNormalizer() {
   }

   public static String normalize(String name) {
      return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
   }

   public static boolean sameName(String a, String b) {
      return normalize(a).equals(normalize(b));
   }

   public static boolean changed(String before, String after) {
      return before != null && after != null ? !before.equals(after) : false;
   }
}
