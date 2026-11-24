package autismclient.util;

import java.util.Locale;

public final class cZ {
   private cZ() {
   }

   public static String init(String name) {
      return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
   }

   public static boolean init(String a, String b) {
      return init(a).equals(init(b));
   }

   public static boolean load(String before, String after) {
      return before != null && after != null ? !before.equals(after) : false;
   }
}
