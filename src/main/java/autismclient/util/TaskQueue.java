package autismclient.util;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class TaskQueue {
   private TaskQueue() {
   }

   public static String init_Str(String text) {
      if (text != null && !text.isBlank()) {
         String decomposed = Normalizer.normalize(load(text), Form.NFKD).toLowerCase(Locale.ROOT);
         StringBuilder out = new StringBuilder(decomposed.length());
         boolean spaced = true;
         int offset = 0;

         while (offset < decomposed.length()) {
            int codePoint = decomposed.codePointAt(offset);
            offset += Character.charCount(codePoint);
            if (!init_Z(codePoint) && !load(codePoint)) {
               int folded = init_I(codePoint);
               if ((folded < 97 || folded > 122) && (folded < 48 || folded > 57)) {
                  if (!spaced) {
                     out.append(' ');
                     spaced = true;
                  }
               } else {
                  out.appendCodePoint(folded);
                  spaced = false;
               }
            }
         }

         offset = out.length();
         if (offset > 0 && out.charAt(offset - 1) == ' ') {
            out.setLength(offset - 1);
         }

         return out.toString();
      } else {
         return "";
      }
   }

   public static String init(String text, String playerName, List<String> keywords, String mode) {
      String normalized = init_Str(text);
      String normalizedName = init_Str(playerName);
      if (!normalizedName.isBlank()) {
         normalized = init_Str(normalized, normalizedName);
      }

      String matchMode = mode == null ? "Contains" : mode;

      for (String raw : keywords == null ? List.<String>of() : keywords) {
         String keyword = init_Str(raw);
         if (keyword.isBlank()) {
            String literal = load(raw == null ? "" : raw).trim().toLowerCase(Locale.ROOT);
            if (!literal.isBlank() && load(text).toLowerCase(Locale.ROOT).contains(literal)) {
               return raw.trim();
            }
         } else {
            boolean var10000;
            switch (matchMode) {
               case "Exact":
                  var10000 = normalized.equals(keyword);
                  break;
               case "Word":
                  var10000 = read(normalized, keyword);
                  break;
               default:
                  var10000 = normalized.replace(" ", "").contains(keyword.replace(" ", "")) || normalized.contains(keyword);
            }

            boolean matches = var10000;
            if (matches) {
               return raw == null ? keyword : raw.trim();
            }
         }
      }

      return "";
   }

   public static List<String> init(List<String> keywords) {
      List<String> out = new ArrayList<>();
      if (keywords == null) {
         return out;
      }

      for (String keyword : keywords) {
         String normalized = init_Str(keyword);
         if (!normalized.isBlank() && !out.contains(normalized)) {
            out.add(normalized);
         }
      }

      return out;
   }

   public static boolean init_Z(String displayedName, String playerName) {
      String rawDisplay = load(displayedName == null ? "" : displayedName).toLowerCase(Locale.ROOT);
      String rawPlayer = load(playerName == null ? "" : playerName).trim().toLowerCase(Locale.ROOT);
      if (rawPlayer.matches("[a-z0-9_]{1,16}")) {
         int from = 0;

         while (from <= rawDisplay.length() - rawPlayer.length()) {
            int match = rawDisplay.indexOf(rawPlayer, from);
            if (match < 0) {
               return false;
            }

            int end = match + rawPlayer.length();
            boolean leftBoundary = match == 0 || !init(rawDisplay.charAt(match - 1));
            boolean rightBoundary = end == rawDisplay.length() || !init(rawDisplay.charAt(end));
            if (leftBoundary && rightBoundary) {
               return true;
            }

            from = match + 1;
         }

         return false;
      } else {
         String displayed = init_Str(displayedName);
         String player = init_Str(playerName);
         return !displayed.isBlank() && !player.isBlank() ? displayed.equals(player) || (" " + displayed + " ").contains(" " + player + " ") : false;
      }
   }

   public static boolean load(String message, String playerName) {
      if (!init_Z(message, playerName)) {
         return false;
      }

      String text = " " + init_Str(message) + " ";
      String player = init_Str(playerName);
      if (player.isBlank()) {
         return false;
      }

      String prefix = " " + player + " ";
      return text.contains(prefix + "left the game ")
         || text.contains(prefix + "left ")
         || text.contains(prefix + "has left ")
         || text.contains(prefix + "quit ")
         || text.contains(prefix + "has quit ")
         || text.contains(prefix + "disconnected ")
         || text.contains(prefix + "logged out ");
   }

   public static boolean init_Z(String name) {
      if (name == null) {
         return false;
      }

      String trimmed = name.trim();
      int len = trimmed.length();
      return len >= 3 && len <= 16 ? trimmed.indexOf(32) < 0 : false;
   }

   private static boolean init(char character) {
      return character >= 'a' && character <= 'z' || character >= '0' && character <= '9' || character == '_';
   }

   private static boolean read(String text, String phrase) {
      return text.equals(phrase) ? true : (" " + text + " ").contains(" " + phrase + " ");
   }

   private static String init_Str(String text, String phrase) {
      String padded = " " + text + " ";
      String needle = " " + phrase + " ";
      return padded.replace(needle, " ").trim().replaceAll("\\s+", " ");
   }

   private static boolean init_Z(int codePoint) {
      int type = Character.getType(codePoint);
      return type == 6 || type == 8 || type == 7;
   }

   private static boolean load(int codePoint) {
      return codePoint == 167 || codePoint == 8203 || codePoint == 8204 || codePoint == 8205 || codePoint == 65279;
   }

   private static String load(String text) {
      if (text != null && !text.isEmpty()) {
         StringBuilder out = new StringBuilder(text.length());

         for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == 167 && i + 1 < text.length()) {
               char code = Character.toLowerCase(text.charAt(i + 1));
               if ("0123456789abcdefklmnorx".indexOf(code) >= 0) {
                  i++;
                  continue;
               }
            }

            out.append(c);
         }

         return out.toString();
      } else {
         return "";
      }
   }

   private static int init_I(int codePoint) {
      int var10000;
      switch (codePoint) {
         case 593:
         case 945:
         case 1072:
         case 7424:
            var10000 = 97;
            break;
         case 609:
         case 610:
            var10000 = 103;
            break;
         case 618:
         case 953:
         case 1110:
            var10000 = 105;
            break;
         case 628:
         case 1400:
            var10000 = 110;
            break;
         case 640:
            var10000 = 114;
            break;
         case 655:
         case 1091:
            var10000 = 121;
            break;
         case 665:
         case 946:
         case 1074:
            var10000 = 98;
            break;
         case 668:
         case 1085:
            var10000 = 104;
            break;
         case 671:
            var10000 = 108;
            break;
         case 949:
         case 1077:
         case 7431:
            var10000 = 101;
            break;
         case 954:
         case 1082:
         case 7435:
            var10000 = 107;
            break;
         case 957:
         case 7456:
            var10000 = 118;
            break;
         case 959:
         case 1086:
         case 7439:
            var10000 = 111;
            break;
         case 961:
         case 1088:
         case 7448:
            var10000 = 112;
            break;
         case 964:
         case 1090:
         case 7451:
            var10000 = 116;
            break;
         case 965:
         case 7452:
            var10000 = 117;
            break;
         case 1010:
         case 1089:
         case 7428:
            var10000 = 99;
            break;
         case 1084:
         case 7437:
            var10000 = 109;
            break;
         case 1093:
            var10000 = 120;
            break;
         case 1109:
         case 42801:
            var10000 = 115;
            break;
         case 1112:
         case 7434:
            var10000 = 106;
            break;
         case 1121:
         case 7457:
            var10000 = 119;
            break;
         case 1281:
         case 7429:
            var10000 = 100;
            break;
         case 1307:
            var10000 = 113;
            break;
         case 7458:
         case 7459:
            var10000 = 122;
            break;
         case 42800:
            var10000 = 102;
            break;
         default:
            var10000 = codePoint;
      }

      return var10000;
   }
}
