package autismclient.util.g;

import autismclient.util.AutismClientMessaging;
import java.util.Locale;
import java.util.Set;

public final class BitSetUtil {
   private static final Set<String> init = Set.of("click-slot", "click-item", "change-slot", "drop", "send", "say", "damage", "hclip", "vclip", "tp");
   private static final Set<String> load = Set.of("delay", "gamemode", "server", "plugins", "matchmaking", "mm", "dismount");

   private BitSetUtil() {
   }

   public static boolean init(String body) {
      ClassCache session = PoolEntry.init_ClassCache();
      if (session == null) {
         return false;
      }

      String trimmed = body == null ? "" : body.trim();
      if (trimmed.isEmpty()) {
         return false;
      }

      int split = trimmed.indexOf(32);
      String name = (split < 0 ? trimmed : trimmed.substring(0, split)).toLowerCase(Locale.ROOT);
      String args = split < 0 ? "" : trimmed.substring(split + 1).trim();
      if (load(name)) {
         String result;
         try {
            String var10000;
            switch (name) {
               case "hclip":
                  var10000 = PoolEntry.init_Str_Str(args);
                  break;
               case "vclip":
                  var10000 = PoolEntry.read_Str(args);
                  break;
               case "tp":
                  var10000 = PoolEntry.load_Str(args);
                  break;
               default:
                  var10000 = session.load_Str_Str_Str(name, args);
            }

            result = var10000;
         } catch (RuntimeException error) {
            result = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
         }

         init(session, name, result);
         return true;
      } else if ("disconnect".equals(name)) {
         String account = session.open_Str();
         session.update_V_Str("Disconnected via POV command");
         AutismClientMessaging.sendPrefixed("§aDisconnected POV bot §f" + account + "§a.");
         return true;
      } else if (read(name)) {
         AutismClientMessaging.sendPrefixed("§c" + name + " cannot safely run on this POV bot yet; the main account was left untouched.");
         return true;
      } else {
         return false;
      }
   }

   static boolean load(String name) {
      return name != null && init.contains(name.toLowerCase(Locale.ROOT));
   }

   static boolean read(String name) {
      return name != null && load.contains(name.toLowerCase(Locale.ROOT));
   }

   private static void init(ClassCache session, String command, String result) {
      String detail = result != null && !result.isBlank() ? result : "No result";
      String color = !"Sent".equals(detail) && !detail.startsWith("Queued ") && !detail.startsWith("TP started") && !detail.startsWith("TP defaults")
         ? (!detail.contains("Nothing") && !detail.startsWith("No active") && !detail.startsWith("Usage") ? "§c" : "§e")
         : "§a";
      AutismClientMessaging.sendPrefixed(color + "POV §f" + session.open_Str() + color + " · " + command + ": §f" + detail);
   }
}
