package autismclient.util.macro;

import java.util.Locale;

public enum TaskTimer {
   init,
   load,
   read,
   write,
   update;

   public static TaskTimer init(String raw, TaskTimer fallback) {
      if (raw == null) {
         return fallback;
      }

      switch (raw.trim().toUpperCase(Locale.ROOT)) {
         case "BEFORE":
         case "TICK_BEFORE":
            return init;
         case "WITH":
         case "SAME_TICK":
            return load;
         case "AFTER":
            return read;
         case "AFTER_PLUS":
         case "AFTER+":
         case "TICK_AFTER":
            return write;
         case "CUSTOM":
            return update;
         default:
            return fallback;
      }
   }
}
