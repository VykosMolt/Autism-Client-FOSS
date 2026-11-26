package autismclient.util.macro;

import java.util.Locale;

public enum D {
   INSTANT,
   GRIM;

   public static D init(String raw, D fallback) {
      if (raw == null) {
         return fallback;
      }

      switch (raw.trim().toUpperCase(Locale.ROOT)) {
         case "INSTANT":
            return INSTANT;
         case "GRIM":
            return GRIM;
         default:
            return fallback;
      }
   }
}
