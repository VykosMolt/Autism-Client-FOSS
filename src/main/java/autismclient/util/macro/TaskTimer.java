package autismclient.util.macro;

import java.util.Locale;

public enum TaskTimer {
   BEFORE,
   WITH,
   AFTER,
   AFTER_PLUS,
   CUSTOM;

   public static TaskTimer init(String raw, TaskTimer fallback) {
      if (raw == null) {
         return fallback;
      }

      switch (raw.trim().toUpperCase(Locale.ROOT)) {
         case "BEFORE":
         case "TICK_BEFORE":
            return BEFORE;
         case "WITH":
         case "SAME_TICK":
            return WITH;
         case "AFTER":
            return AFTER;
         case "AFTER_PLUS":
         case "AFTER+":
         case "TICK_AFTER":
            return AFTER_PLUS;
         case "CUSTOM":
            return CUSTOM;
         default:
            return fallback;
      }
   }
}
