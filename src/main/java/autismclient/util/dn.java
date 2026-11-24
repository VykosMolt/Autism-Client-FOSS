package autismclient.util;

import java.util.ArrayList;
import java.util.List;

public final class dn {
   private dn() {
   }

   public static List<String> init(String value) {
      List<String> out = new ArrayList<>();
      if (value != null && !value.isBlank()) {
         for (String raw : value.split("\\|")) {
            String item = raw.trim();
            if (!item.isEmpty()) {
               out.add(item);
            }
         }

         return out;
      } else {
         return out;
      }
   }

   public static String init(List<String> values) {
      return values == null ? "" : String.join("|", values);
   }
}
