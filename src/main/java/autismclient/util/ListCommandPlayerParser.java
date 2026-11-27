package autismclient.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;

public final class ListCommandPlayerParser {
   private ListCommandPlayerParser() {
   }

   public static Optional<List<ListCommandPlayerParser.Inner_DataUtil>> init(Component message) {
      if (message != null
         && message.getSiblings().isEmpty()
         && message.getContents() instanceof TranslatableContents translated
         && translated.getKey().equals("commands.list.players")) {
         Object[] args = translated.getArgs();
         if (args.length == 3 && args[2] instanceof Component names) {
            int var12 = init_I(args[0]);
            int max = init_I(args[1]);
            if (var12 >= 0 && var12 <= 4096 && max >= var12) {
               List<ListCommandPlayerParser.Inner_DataUtil> players = new ArrayList<>();
               if (init(names, players, 0, new int[]{0}) && players.size() == var12) {
                  Set<UUID> ids = new HashSet<>();
                  Set<String> usernames = new HashSet<>();

                  for (ListCommandPlayerParser.Inner_DataUtil p : players) {
                     if (!ids.add(p.uuid) || !usernames.add(p.name.toLowerCase(Locale.ROOT))) {
                        return Optional.empty();
                     }
                  }

                  return Optional.of(List.copyOf(players));
               } else {
                  return Optional.empty();
               }
            } else {
               return Optional.empty();
            }
         } else {
            return Optional.empty();
         }
      } else {
         return Optional.empty();
      }
   }

   private static boolean init(Component node, List<ListCommandPlayerParser.Inner_DataUtil> out, int depth, int[] visited) {
      if (depth <= 16 && ++visited[0] <= 16384 && out.size() <= 4096) {
         if (node.getContents() instanceof TranslatableContents translated) {
            if (!translated.getKey().equals("commands.list.nameAndId")) {
               return false;
            }

            Object[] args = translated.getArgs();
            if (args.length != 2) {
               return false;
            }

            String name = init_Str(args[0]);
            String id = init_Str(args[1]);
            if (!name.matches("[A-Za-z0-9_]{1,16}") || !id.matches("[a-fA-F0-9]{8}(-[a-fA-F0-9]{4}){3}-[a-fA-F0-9]{12}")) {
               return false;
            }

            UUID uuid = UUID.fromString(id);
            if (uuid.getMostSignificantBits() == 0L && uuid.getLeastSignificantBits() == 0L) {
               return false;
            }

            out.add(new ListCommandPlayerParser.Inner_DataUtil(uuid, name));
         } else if (!(node.getContents() instanceof PlainTextContents text) || !text.text().matches("[,\\s]*")) {
            return false;
         }

         for (Component sibling : node.getSiblings()) {
            if (!init(sibling, out, depth + 1, visited)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static String init_Str(Object value) {
      if (value instanceof String text) {
         return text;
      } else {
         return value instanceof Component component && component.getSiblings().isEmpty() && component.getContents() instanceof PlainTextContents text
            ? text.text()
            : "";
      }
   }

   private static int init_I(Object value) {
      if (value instanceof Number n && n.doubleValue() == n.intValue()) {
         return n.intValue();
      } else {
         return value instanceof String text && text.matches("[0-9]{1,7}") ? Integer.parseInt(text) : -1;
      }
   }

   public record Inner_DataUtil(UUID uuid, String name) {
   }
}
