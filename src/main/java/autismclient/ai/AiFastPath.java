package autismclient.ai;

import com.google.gson.JsonObject;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class AiFastPath {
   private static final String init_Str = "(?:please\\s+)?(parkour|go|goto|walk|run|move|travel|head|navigate|get)\\s*(?:to|towards|toward|over\\s+to|back\\s+to)?\\s*(?:the\\s+)?";
   private static final String load_Str = "(-?\\d+(?:\\.\\d+)?)";
   private static final Pattern GOTO_XYZ = Pattern.compile(
      "^(?:please\\s+)?(parkour|go|goto|walk|run|move|travel|head|navigate|get)\\s*(?:to|towards|toward|over\\s+to|back\\s+to)?\\s*(?:the\\s+)?(?:coords?|coordinates?|position|pos|block|location)?\\s*:?\\s*(?:x\\s*[:=]?\\s*)?(-?\\d+(?:\\.\\d+)?)[,\\s]+(?:y\\s*[:=]?\\s*)?(-?\\d+(?:\\.\\d+)?)[,\\s]+(?:z\\s*[:=]?\\s*)?(-?\\d+(?:\\.\\d+)?)\\s*$",
      2
   );
   private static final Pattern GOTO_XZ = Pattern.compile(
      "^(?:please\\s+)?(parkour|go|goto|walk|run|move|travel|head|navigate|get)\\s*(?:to|towards|toward|over\\s+to|back\\s+to)?\\s*(?:the\\s+)?(?:coords?|coordinates?|position|pos|xz)?\\s*:?\\s*(?:x\\s*[:=]?\\s*)?(-?\\d+(?:\\.\\d+)?)[,\\s]+(?:z\\s*[:=]?\\s*)?(-?\\d+(?:\\.\\d+)?)\\s*$",
      2
   );
   private static final String read_Str = "(north|south|east|west|forward|forwards|ahead|straight|back|backward|backwards|left|right)";
   private static final Pattern MOVE_AMOUNT_DIRECTION = Pattern.compile(
      "^(?:please\\s+)?(parkour|go|walk|run|move|travel|head|sprint)\\s+(-?\\d+(?:\\.\\d+)?)\\s*(?:blocks?|meters?|metres?|m)?\\s+(north|south|east|west|forward|forwards|ahead|straight|back|backward|backwards|left|right)\\s*$",
      2
   );
   private static final Pattern MOVE_DIRECTION_AMOUNT = Pattern.compile(
      "^(?:please\\s+)?(parkour|go|walk|run|move|travel|head|sprint)\\s+(north|south|east|west|forward|forwards|ahead|straight|back|backward|backwards|left|right)\\s+(?:for\\s+|by\\s+)?(-?\\d+(?:\\.\\d+)?)\\s*(?:blocks?|meters?|metres?|m)?\\s*$",
      2
   );
   private static final Pattern POLITE_PREFIX = Pattern.compile("^(?:(?:can|could|would|will)\\s+you\\s+|please\\s+|pls\\s+|just\\s+|now\\s+)+", 2);
   private static final Pattern TRAILING_FILLER = Pattern.compile(
      "(?:\\s*,?\\s*(?:and\\s+then|and|then)?\\s*(?:stop|halt|stay(?:\\s+there)?|wait(?:\\s+there)?)|\\s+(?:please|pls|now|for\\s+me|thanks?|thank\\s+you))+$",
      2
   );
   private static final Pattern GOTO_WAYPOINT = Pattern.compile(
      "^(?:please\\s+)?(parkour|go|goto|walk|run|move|travel|head|navigate|get)\\s*(?:to|towards|toward|over\\s+to|back\\s+to)?\\s*(?:the\\s+)?(?:waypoint\\s+|wp\\s+)?[\"']?([A-Za-z][A-Za-z0-9 _-]{0,31})[\"']?\\s*$",
      2
   );

   private AiFastPath() {
   }

   static AiFastPath.ParsedObjective parse(String objective, Function<String, int[]> waypoint) {
      if (objective == null) {
         return null;
      }

      String text = objective.trim().replaceAll("[.!?]+$", "").trim();
      text = TRAILING_FILLER.matcher(POLITE_PREFIX.matcher(text).replaceFirst("")).replaceFirst("").replaceAll("[.!?,]+$", "").trim();
      if (!text.isEmpty() && text.length() <= 96) {
         Matcher m = GOTO_XYZ.matcher(text);
         if (m.matches()) {
            boolean parkour = "parkour".equalsIgnoreCase(m.group(1));
            int x = floorInt(m.group(2));
            int y = floorInt(m.group(3));
            int z = floorInt(m.group(4));
            JsonObject args = AiJson.objectOf("x", x, "y", y, "z", z);
            if (parkour) {
               applyParkourDefaults(args, true);
            }

            return new AiFastPath.ParsedObjective(args, (parkour ? "parkour to " : "go to ") + x + " " + y + " " + z);
         } else {
            m = GOTO_XZ.matcher(text);
            if (m.matches()) {
               boolean parkour = "parkour".equalsIgnoreCase(m.group(1));
               int x = floorInt(m.group(2));
               int z = floorInt(m.group(3));
               JsonObject args = AiJson.objectOf("x", x, "z", z);
               if (parkour) {
                  applyParkourDefaults(args, true);
               }

               return new AiFastPath.ParsedObjective(args, (parkour ? "parkour to " : "go to ") + "x=" + x + " z=" + z + " (nearest standable height)");
            } else {
               m = MOVE_AMOUNT_DIRECTION.matcher(text);
               if (m.matches()) {
                  return relativeMove("parkour".equalsIgnoreCase(m.group(1)), m.group(3), m.group(2));
               }

               m = MOVE_DIRECTION_AMOUNT.matcher(text);
               if (m.matches()) {
                  return relativeMove("parkour".equalsIgnoreCase(m.group(1)), m.group(2), m.group(3));
               }

               m = GOTO_WAYPOINT.matcher(text);
               if (m.matches()) {
                  boolean parkour = "parkour".equalsIgnoreCase(m.group(1));
                  String name = m.group(2).trim();
                  if (name.matches("(?i)(the\\s+)?(nearest|closest|next|a|an|some|my|our|that|this|there|here|home\\s+base\\s+of.*)")) {
                     return null;
                  }

                  int[] at = waypoint == null ? null : waypoint.apply(name);
                  if (at == null) {
                     return null;
                  }

                  JsonObject args = AiJson.objectOf("x", at[0], "y", at[1], "z", at[2]);
                  if (parkour) {
                     applyParkourDefaults(args, true);
                  }

                  return new AiFastPath.ParsedObjective(args, (parkour ? "parkour to " : "go to ") + "waypoint '" + name + "' at " + at[0] + " " + at[1] + " " + at[2]);
               } else {
                  return null;
               }
            }
         }
      } else {
         return null;
      }
   }

   private static AiFastPath.ParsedObjective relativeMove(boolean parkour, String direction, String amount) {
      double distance = Double.parseDouble(amount);
      if (distance > 0.0 && !(distance > 512.0)) {
         String dir = direction.toLowerCase(Locale.ROOT);
         String var10000;
         switch (dir) {
            case "forwards":
            case "ahead":
            case "straight":
               var10000 = "forward";
               break;
            case "backward":
            case "backwards":
               var10000 = "back";
               break;
            default:
               var10000 = dir;
         }

         String canonical = var10000;
         JsonObject args = AiJson.objectOf("direction", canonical, "distance", distance);
         if (parkour) {
            applyParkourDefaults(args, false);
         }

         return new AiFastPath.ParsedObjective(args, (parkour ? "parkour " : "go ") + (int)distance + " blocks " + canonical);
      } else {
         return null;
      }
   }

   private static void applyParkourDefaults(JsonObject args, boolean coordinates) {
      args.addProperty("parkour", true);
      args.addProperty("allowDig", false);
      args.addProperty("allowPillar", false);
      args.addProperty("allowBridge", false);
      if (coordinates) {
         args.addProperty("radius", 0.6);
      }
   }

   private static int floorInt(String number) {
      return (int)Math.floor(Double.parseDouble(number));
   }

   record ParsedObjective(JsonObject args, String describe) {
   }
}
