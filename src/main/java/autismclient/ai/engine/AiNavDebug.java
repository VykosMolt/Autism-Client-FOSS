package autismclient.ai.engine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public final class AiNavDebug {
   private static final int MAX_HISTORY = 128;
   private static final boolean ENABLED = Boolean.getBoolean("autism.navigation.debug");
   private static final ArrayDeque<String> HISTORY = ENABLED ? new ArrayDeque<>(128) : null;
   private static long sequence;

   private AiNavDebug() {
   }

   public static boolean isEnabled() {
      return ENABLED;
   }

   public static void log(String message) {
      if (ENABLED) {
         System.out.println("[Navigation] " + message);
         synchronized (HISTORY) {
            if (HISTORY.size() == 128) {
               HISTORY.pollFirst();
            }

            HISTORY.addLast(++sequence + " " + message);
         }
      }
   }

   public static long cursor() {
      return sequence;
   }

   public static List<String> readSince(long cursor) {
      if (!ENABLED) {
         return List.of();
      }

      synchronized (HISTORY) {
         List<String> out = new ArrayList<>();

         for (String entry : HISTORY) {
            int space = entry.indexOf(32);
            if (space > 0 && Long.parseLong(entry.substring(0, space)) > cursor) {
               out.add(entry.substring(space + 1));
            }
         }

         return out;
      }
   }
}
