package autismclient.ai.a;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public final class AiNavDebug {
   private static final int init_I = 128;
   private static final boolean init_Z = Boolean.getBoolean("autism.navigation.debug");
   private static final ArrayDeque<String> init_ArrayDeque = init_Z ? new ArrayDeque<>(128) : null;
   private static long init_J;

   private AiNavDebug() {
   }

   public static boolean init_Z() {
      return init_Z;
   }

   public static void init(String message) {
      if (init_Z) {
         System.out.println("[Navigation] " + message);
         synchronized (init_ArrayDeque) {
            if (init_ArrayDeque.size() == 128) {
               init_ArrayDeque.pollFirst();
            }

            init_ArrayDeque.addLast(++init_J + " " + message);
         }
      }
   }

   public static long init_J() {
      return init_J;
   }

   public static List<String> init(long cursor) {
      if (!init_Z) {
         return List.of();
      }

      synchronized (init_ArrayDeque) {
         List<String> out = new ArrayList<>();

         for (String entry : init_ArrayDeque) {
            int space = entry.indexOf(32);
            if (space > 0 && Long.parseLong(entry.substring(0, space)) > cursor) {
               out.add(entry.substring(space + 1));
            }
         }

         return out;
      }
   }
}
