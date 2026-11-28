package autismclient.util.macro;

import java.util.Locale;

public enum PacketOrderMode {
   INSTANT,
   GRIM;

   public static PacketOrderMode parse(String raw, PacketOrderMode fallback) {
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
