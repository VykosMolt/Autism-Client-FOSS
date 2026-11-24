package autismclient.util.macro;

import java.util.Locale;

public enum D {
   init,
   load;

   public static D init(String raw, D fallback) {
      if (raw == null) {
         return fallback;
      }

      switch (raw.trim().toUpperCase(Locale.ROOT)) {
         case "INSTANT":
            return init;
         case "GRIM":
            return load;
         default:
            return fallback;
      }
   }
}
