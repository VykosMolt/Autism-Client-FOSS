package autismclient.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class AiToolCallParser {
   public static final String GOTO_USAGE = "Usage: .tools goto <x> <y> <z> [radius] [strategy] | goto <direction> <blocks> | goto <waypoint>";
   static final String NOFALL_FLAG = "nofall";
   static final List<String> parkourSpeedFlags = List.of("speed0", "speed1", "speed2");
   public static final String PARKOUR_USAGE = "Usage: .tools parkour [nofall] [speed0|speed1|speed2] [<x> <y> <z> ...]. Flags can go before or after the destination. Omit the destination to pick checkpoints in Freecam.";
   static final int maxParkourDestinations = 32;
   public static final String MINE_USAGE = "Usage: .tools mine <block[=amount][,block[=amount]...]> [count per block]";
   public static final String CHOP_USAGE = "Usage: .tools chop <block> [count]";
   public static final String FOLLOW_USAGE = "Usage: .tools follow [type]";
   public static final String FIGHT_USAGE = "Usage: .tools fight <type> [count]";
   public static final String FILL_USAGE = "Usage: .tools fill <x> <y> <z> <x2> <y2> <z2> <block> [hollow|solid]";
   public static final String DIG_USAGE = "Usage: .tools dig tunnel <north|south|east|west> [length]";
   public static final String SMELT_USAGE = "Usage: .tools smelt <item> [count]";
   public static final String CRAFT_USAGE = "Usage: .tools craft <item> [count]";
   public static final String OBTAIN_USAGE = "Usage: .tools obtain <item> [count]";
   public static final String COLLECT_USAGE = "Usage: .tools collect <item> [count]";
   public static final String TRADE_USAGE = "Usage: .tools trade <index|item>";
   public static final String EVENTS_USAGE = "Usage: .tools events [since]";
   public static final String BUILD_USAGE = "Usage: .tools build <schematic name|blueprint file|{inline json}>";
   public static final String FARM_USAGE = "Usage: .tools farm <wheat|carrots|potatoes|beetroots> [plots]";
   private static final Set<String> DIRECTION_WORDS = Set.of(
      "north", "south", "east", "west", "forward", "forwards", "ahead", "back", "backward", "backwards", "left", "right", "up", "down", "n", "s", "e", "w"
   );

   private AiToolCallParser() {
   }

   public static List<String> tokenize(String input) {
      List<String> tokens = new ArrayList<>();
      if (input != null && !input.isBlank()) {
         StringBuilder current = new StringBuilder();
         int depth = 0;
         boolean quoted = false;
         boolean escaped = false;

         for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (escaped) {
               current.append(ch);
               escaped = false;
            } else if (quoted) {
               current.append(ch);
               if (ch == '\\') {
                  escaped = true;
               } else if (ch == '"') {
                  quoted = false;
               }
            } else {
               switch (ch) {
                  case '"':
                     current.append(ch);
                     quoted = true;
                     break;
                  case '[':
                  case '{':
                     depth++;
                     current.append(ch);
                     break;
                  case ']':
                  case '}':
                     if (depth <= 0) {
                        throw new IllegalArgumentException("Unbalanced '" + ch + "' in arguments");
                     }

                     depth--;
                     current.append(ch);
                     break;
                  default:
                     if (!Character.isWhitespace(ch) || depth != 0) {
                        current.append(ch);
                     } else if (current.length() > 0) {
                        tokens.add(current.toString());
                        current.setLength(0);
                     }
               }
            }
         }

         if (quoted) {
            throw new IllegalArgumentException("Unterminated \" in arguments");
         }

         if (depth != 0) {
            throw new IllegalArgumentException("Unclosed '[' or '{' in arguments");
         }

         if (current.length() > 0) {
            tokens.add(current.toString());
         }

         return tokens;
      } else {
         return tokens;
      }
   }

   public static JsonObject parse(String input) {
      return parseTokens(tokenize(input));
   }

   public static JsonObject parseTokens(List<String> tokens) {
      JsonObject out = new JsonObject();

      for (String token : tokens) {
         int eq = topLevelEquals(token);
         if (eq < 0) {
            throw new IllegalArgumentException("Expected k=v, got '" + token + "'");
         }

         String key = token.substring(0, eq);
         if (!key.matches("[A-Za-z0-9_.\\-]+")) {
            throw new IllegalArgumentException("Bad argument name '" + key + "' (letters, digits, _ . -)");
         }

         String raw = token.substring(eq + 1);
         if (raw.isEmpty()) {
            throw new IllegalArgumentException("Missing value for '" + key + "'");
         }

         out.add(key, parseValue(raw, 0));
      }

      return out;
   }

   private static int topLevelEquals(String token) {
      int depth = 0;
      boolean quoted = false;
      boolean escaped = false;

      for (int i = 0; i < token.length(); i++) {
         char ch = token.charAt(i);
         if (escaped) {
            escaped = false;
         } else if (quoted) {
            if (ch == '\\') {
               escaped = true;
            } else if (ch == '"') {
               quoted = false;
            }
         } else if (ch == '"') {
            quoted = true;
         } else if (ch == '[' || ch == '{') {
            depth++;
         } else if (ch != ']' && ch != '}') {
            if (ch == '=' && depth == 0) {
               return i;
            }
         } else {
            depth--;
         }
      }

      return -1;
   }

   public static JsonElement parseValue(String raw) {
      return parseValue(raw, 0);
   }

   private static JsonElement parseValue(String raw, int depth) {
      if (depth > 16) {
         throw new IllegalArgumentException("Value nested too deeply (max 16)");
      }

      if (raw != null && !raw.isEmpty()) {
         if (raw.length() < 2 || !raw.startsWith("\"")) {
            char first = raw.charAt(0);
            if (first == '[') {
               if (!raw.endsWith("]")) {
                  throw new IllegalArgumentException("Unclosed '[' in " + abbreviate(raw));
               }

               JsonArray array = new JsonArray();

               for (String element : splitTopLevel(raw.substring(1, raw.length() - 1), ',')) {
                  if (element.isBlank()) {
                     throw new IllegalArgumentException("Empty array element in " + abbreviate(raw));
                  }

                  array.add(parseValue(element.trim(), depth + 1));
               }

               return array;
            } else if (first != '{') {
               if (raw.equals("true")) {
                  return new JsonPrimitive(true);
               }

               if (raw.equals("false")) {
                  return new JsonPrimitive(false);
               }

               if (raw.equals("null")) {
                  return JsonNull.INSTANCE;
               }

               if (raw.matches("-?\\d+")) {
                  try {
                     return new JsonPrimitive(Long.parseLong(raw));
                  } catch (NumberFormatException e) {
                     throw new IllegalArgumentException("Integer out of range: " + raw);
                  }
               } else if (!raw.matches("-?\\d*\\.\\d+([eE][+-]?\\d+)?") && !raw.matches("-?\\d+[eE][+-]?\\d+")) {
                  return new JsonPrimitive(raw);
               } else {
                  double number = Double.parseDouble(raw);
                  if (!Double.isFinite(number)) {
                     throw new IllegalArgumentException("Non-finite number: " + raw);
                  } else {
                     return new JsonPrimitive(number);
                  }
               }
            } else {
               if (!raw.endsWith("}")) {
                  throw new IllegalArgumentException("Unclosed '{' in " + abbreviate(raw));
               }

               JsonObject object = new JsonObject();
               String body = raw.substring(1, raw.length() - 1);

               for (String entry : splitTopLevel(body, ',')) {
                  if (entry.isBlank()) {
                     if (!body.isBlank()) {
                        throw new IllegalArgumentException("Empty object entry in " + abbreviate(raw));
                     }
                     break;
                  }

                  int colon = topLevelColon(entry);
                  if (colon < 0) {
                     throw new IllegalArgumentException("Expected k:v in object entry '" + entry.trim() + "'");
                  }

                  String key = entry.substring(0, colon).trim();
                  if (key.length() >= 2 && key.startsWith("\"") && key.endsWith("\"")) {
                     key = unescape(key.substring(1, key.length() - 1));
                  }

                  if (key.isEmpty()) {
                     throw new IllegalArgumentException("Empty object key in " + abbreviate(raw));
                  }

                  String value = entry.substring(colon + 1).trim();
                  if (value.isEmpty()) {
                     throw new IllegalArgumentException("Missing value for object key '" + key + "'");
                  }

                  object.add(key, parseValue(value, depth + 1));
               }

               return object;
            }
         } else if (!raw.endsWith("\"")) {
            throw new IllegalArgumentException("Unterminated \" in " + raw);
         } else {
            return new JsonPrimitive(unescape(raw.substring(1, raw.length() - 1)));
         }
      } else {
         throw new IllegalArgumentException("Empty value");
      }
   }

   private static int topLevelColon(String token) {
      int depth = 0;
      boolean quoted = false;
      boolean escaped = false;

      for (int i = 0; i < token.length(); i++) {
         char ch = token.charAt(i);
         if (escaped) {
            escaped = false;
         } else if (quoted) {
            if (ch == '\\') {
               escaped = true;
            } else if (ch == '"') {
               quoted = false;
            }
         } else if (ch == '"') {
            quoted = true;
         } else if (ch == '[' || ch == '{') {
            depth++;
         } else if (ch != ']' && ch != '}') {
            if (ch == ':' && depth == 0) {
               return i;
            }
         } else {
            depth--;
         }
      }

      return -1;
   }

   private static List<String> splitTopLevel(String body, char separator) {
      List<String> parts = new ArrayList<>();
      if (body.isEmpty()) {
         return parts;
      }

      StringBuilder current = new StringBuilder();
      int depth = 0;
      boolean quoted = false;
      boolean escaped = false;

      for (int i = 0; i < body.length(); i++) {
         char ch = body.charAt(i);
         if (escaped) {
            current.append(ch);
            escaped = false;
         } else if (quoted) {
            current.append(ch);
            if (ch == '\\') {
               escaped = true;
            } else if (ch == '"') {
               quoted = false;
            }
         } else if (ch == '"') {
            current.append(ch);
            quoted = true;
         } else if (ch == '[' || ch == '{') {
            depth++;
            current.append(ch);
         } else if (ch == ']' || ch == '}') {
            depth--;
            current.append(ch);
         } else if (ch == separator && depth == 0) {
            parts.add(current.toString());
            current.setLength(0);
         } else {
            current.append(ch);
         }
      }

      if (quoted) {
         throw new IllegalArgumentException("Unterminated \" in value");
      }

      parts.add(current.toString());
      return parts;
   }

   private static String unescape(String body) {
      StringBuilder out = new StringBuilder(body.length());
      boolean escaped = false;

      for (int i = 0; i < body.length(); i++) {
         char ch = body.charAt(i);
         if (escaped) {
            char var10001;
            switch (ch) {
               case 'n':
                  var10001 = '\n';
                  break;
               case 't':
                  var10001 = '\t';
                  break;
               default:
                  var10001 = ch;
            }

            out.append(var10001);
            escaped = false;
         } else if (ch == '\\') {
            escaped = true;
         } else {
            out.append(ch);
         }
      }

      if (escaped) {
         out.append('\\');
      }

      return out.toString();
   }

   private static String abbreviate(String raw) {
      return raw.length() <= 24 ? raw : raw.substring(0, 21) + "...";
   }

   public static double parseRelativeCoord(String token, double base) {
      if (token != null && !token.isBlank()) {
         String text = token.trim();
         if (text.startsWith("~")) {
            String offset = text.substring(1);
            double value = offset.isEmpty() ? 0.0 : parseCoordOffset(offset, token);
            return base + value;
         } else {
            return parseCoordOffset(text, token);
         }
      } else {
         throw new IllegalArgumentException("Missing coordinate");
      }
   }

   private static double parseCoordOffset(String text, String token) {
      double value;
      try {
         value = Double.parseDouble(text);
      } catch (NumberFormatException e) {
         throw new IllegalArgumentException("Bad coordinate '" + token + "' (number or ~relative)");
      }

      if (!Double.isFinite(value)) {
         throw new IllegalArgumentException("Bad coordinate '" + token + "' (number or ~relative)");
      } else {
         return value;
      }
   }

   public static int parseBlockCoord(String token, double base) {
      return (int)Math.floor(parseRelativeCoord(token, base));
   }

   public static AiToolCallParser.ToolCall init(String sub, List<String> args, double px, double py, double pz) {
      AiToolCallParser.ToolCall var10000;
      switch (sub) {
         case "goto":
            var10000 = gotoCall(args, px, py, pz);
            break;
         case "parkour":
            var10000 = parkourCall(args, px, py, pz);
            break;
         case "mine":
            AiToolCallParser.ToolCall mine = targetCountCall(args, "game:mine", "block", "Usage: .tools mine <block[=amount][,block[=amount]...]> [count per block]");
            var10000 = new AiToolCallParser.ToolCall("game:mine", AiMiningQuota.normalizeArgs(mine.arguments()));
            break;
         case "chop":
            var10000 = targetCountCall(args, "game:chop", "block", "Usage: .tools chop <block> [count]");
            break;
         case "farm":
            AiToolCallParser.ToolCall farm = targetCountCall(args, "game:farm", "crop", "Usage: .tools farm <wheat|carrots|potatoes|beetroots> [plots]");
            farm.arguments().addProperty("action", "build");
            var10000 = farm;
            break;
         case "follow":
            JsonObject out = new JsonObject();
            if (!args.isEmpty()) {
               out.addProperty("type", joinArgs(args, args.size()));
            }

            var10000 = new AiToolCallParser.ToolCall("game:follow", out);
            break;
         case "fight":
            var10000 = targetCountCall(args, "game:fight", "target", "Usage: .tools fight <type> [count]");
            break;
         case "fill":
            var10000 = fillCall(args, px, py, pz);
            break;
         case "dig":
            var10000 = digCall(args);
            break;
         case "surface":
            var10000 = noArgCall(args, "game:surface", "Usage: .tools surface");
            break;
         case "cave":
            var10000 = noArgCall(args, "game:cave", "Usage: .tools cave");
            break;
         case "eat":
            var10000 = noArgCall(args, "game:eat", "Usage: .tools eat");
            break;
         case "sleep":
            var10000 = noArgCall(args, "game:sleep", "Usage: .tools sleep");
            break;
         case "smelt":
            var10000 = targetCountCall(args, "game:smelt_auto", "item", "Usage: .tools smelt <item> [count]");
            break;
         case "craft":
            var10000 = targetCountCall(args, "game:craft", "item", "Usage: .tools craft <item> [count]");
            break;
         case "obtain":
         case "get":
            var10000 = targetCountCall(args, "game:obtain", "item", "Usage: .tools obtain <item> [count]");
            break;
         case "collect":
         case "gather":
            AiToolCallParser.ToolCall collect = targetCountCall(args, "game:collect", "item", "Usage: .tools collect <item> [count]");
            if (!collect.arguments().has("count")) {
               collect.arguments().addProperty("count", 1);
            }

            var10000 = collect;
            break;
         case "trade": {
            if (args.isEmpty()) {
               throw new IllegalArgumentException("Usage: .tools trade <index|item>");
            }

            JsonObject out2 = new JsonObject();
            out2.addProperty("action", "trade");
            String value = joinArgs(args, args.size());
            if (value.matches("\\d+")) {
               out2.addProperty("tradeIndex", Integer.parseInt(value));
            } else {
               out2.addProperty("item", value);
            }

            var10000 = new AiToolCallParser.ToolCall("game:trade", out2);
            break;
         }
         case "events": {
            if (args.size() > 1) {
               throw new IllegalArgumentException("Usage: .tools events [since]");
            }

            JsonObject out3 = new JsonObject();
            if (!args.isEmpty()) {
               int since = parseCount(args.get(0), "Usage: .tools events [since]");
               if (since < 0) {
                  throw new IllegalArgumentException("since must be >= 0\nUsage: .tools events [since]");
               }

               out3.addProperty("since", since);
            }

            var10000 = new AiToolCallParser.ToolCall("game:events", out3);
            break;
         }
         default:
            throw new IllegalArgumentException("Unknown shorthand '" + sub + "'");
      }

      return var10000;
   }

   private static AiToolCallParser.ToolCall parkourCall(List<String> args, double px, double py, double pz) {
      AiToolCallParser.ParkourFlags options = parseParkourFlags(args);
      String usage = PARKOUR_USAGE;
      args = args.stream().filter(arg -> !isParkourFlag(arg)).toList();

      AiToolCallParser.ToolCall trip;
      try {
         if (!args.isEmpty() && args.getFirst().equalsIgnoreCase("to")) {
            args = args.subList(1, args.size());
         }

         if (args.size() > 5) {
            if (args.size() % 3 != 0) {
               throw new IllegalArgumentException(usage);
            }

            if (args.size() / 3 > maxParkourDestinations) {
               throw new IllegalArgumentException("Parkour accepts up to 32 destinations");
            }

            JsonArray checkpoints = new JsonArray();
            for (int i = 0; i < args.size() - 3; i += 3) {
               checkpoints.add(parseCoordsObject(args.subList(i, i + 3), px, py, pz));
            }

            JsonObject dest = parseCoordsObject(args.subList(args.size() - 3, args.size()), px, py, pz);
            dest.add("checkpoints", checkpoints);
            dest.addProperty("allowInventory", false);
            trip = new AiToolCallParser.ToolCall("game:goto", dest);
         } else {
            trip = gotoCall(args, px, py, pz);
         }
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException(e.getMessage().replace(GOTO_USAGE, usage));
      }

      return new AiToolCallParser.ToolCall(trip.id(), init_JsonObject_JsonObject_ParkourOptions(trip.arguments(), options));
   }

   private static JsonObject parseCoordsObject(List<String> coords, double px, double py, double pz) {
      JsonObject out = new JsonObject();
      out.addProperty("x", parseBlockCoord(coords.get(0), px));
      out.addProperty("y", parseBlockCoord(coords.get(1), py));
      out.addProperty("z", parseBlockCoord(coords.get(2), pz));
      return out;
   }

   public static boolean isNoFallFlag(String token) {
      return "nofall".equalsIgnoreCase(token) || "--no-fall-damage".equalsIgnoreCase(token);
   }

   public static boolean isParkourFlag(String token) {
      return isNoFallFlag(token) || parkourSpeedFlags.contains(token.toLowerCase(Locale.ROOT));
   }

   public static AiToolCallParser.ParkourFlags parseParkourFlags(List<String> args) {
      boolean noFall = false;
      int speed = -1;

      for (String token : args) {
         if (isNoFallFlag(token)) {
            noFall = true;
         }

         int idx = parkourSpeedFlags.indexOf(token.toLowerCase(Locale.ROOT));
         if (idx >= 0) {
            if (speed >= 0 && speed != idx) {
               throw new IllegalArgumentException("Choose one parkour speed: speed0, speed1 or speed2");
            }

            speed = idx;
         }
      }

      return new AiToolCallParser.ParkourFlags(noFall, speed);
   }

   static JsonObject withParkourOptions(JsonObject destination, boolean assumeNoDamage) {
      return init_JsonObject_JsonObject_ParkourOptions(destination, new AiToolCallParser.ParkourFlags(assumeNoDamage, -1));
   }

   static JsonObject init_JsonObject_JsonObject_ParkourOptions(JsonObject destination, AiToolCallParser.ParkourFlags options) {
      JsonObject out = destination.deepCopy();
      out.addProperty("parkour", true);
      if (options.noFall()) {
         out.addProperty("assumeNoFallDamage", true);
      }

      if (options.speedLevel() >= 0) {
         out.addProperty("parkourSpeed", options.speedLevel());
      }

      out.addProperty("allowDig", false);
      out.addProperty("allowPillar", false);
      out.addProperty("allowBridge", false);
      if (out.has("x") && !out.has("radius")) {
         out.addProperty("radius", 0.6);
      }

      return out;
   }

   public record ParkourFlags(boolean noFall, int speedLevel) {
   }

   private static AiToolCallParser.ToolCall gotoCall(List<String> args, double px, double py, double pz) {
      if (args.size() >= 2 && args.get(0).equalsIgnoreCase("to")) {
         args = args.subList(1, args.size());
      }

      if (args.size() == 2) {
         String a = args.get(0).toLowerCase(Locale.ROOT);
         String b = args.get(1).toLowerCase(Locale.ROOT);
         if (DIRECTION_WORDS.contains(a) && b.matches("\\d+(\\.\\d+)?")) {
            return new AiToolCallParser.ToolCall("game:goto", AiJson.objectOf("direction", a, "distance", Double.parseDouble(b)));
         }

         if (DIRECTION_WORDS.contains(b) && a.matches("\\d+(\\.\\d+)?")) {
            return new AiToolCallParser.ToolCall("game:goto", AiJson.objectOf("direction", b, "distance", Double.parseDouble(a)));
         }
      }

      if (args.size() == 1 && !args.get(0).matches("-?~?\\d.*")) {
         return new AiToolCallParser.ToolCall("game:goto", AiJson.objectOf("waypoint", args.get(0)));
      }

      if (args.size() >= 3 && args.size() <= 5) {
         JsonObject out = new JsonObject();
         out.addProperty("x", parseBlockCoord(args.get(0), px));
         out.addProperty("y", parseBlockCoord(args.get(1), py));
         out.addProperty("z", parseBlockCoord(args.get(2), pz));
         if (args.size() >= 4) {
            double radius;
            try {
               radius = Double.parseDouble(args.get(3));
            } catch (NumberFormatException e) {
               throw new IllegalArgumentException(
                  "Bad radius '" + args.get(3) + "'\nUsage: .tools goto <x> <y> <z> [radius] [strategy] | goto <direction> <blocks> | goto <waypoint>"
               );
            }

            if (!Double.isFinite(radius) || radius <= 0.0 || radius > 64.0) {
               throw new IllegalArgumentException(
                  "Radius must be 0..64\nUsage: .tools goto <x> <y> <z> [radius] [strategy] | goto <direction> <blocks> | goto <waypoint>"
               );
            }

            out.addProperty("radius", radius);
         }

         if (args.size() == 5) {
            String strategy = args.get(4).toLowerCase(Locale.ROOT);
            if (!AiToolSettings.STRATEGIES.contains(strategy)) {
               throw new IllegalArgumentException(
                  "Unknown strategy '"
                     + args.get(4)
                     + "' (fastest|safest|saver)\nUsage: .tools goto <x> <y> <z> [radius] [strategy] | goto <direction> <blocks> | goto <waypoint>"
               );
            }

            out.addProperty("strategy", strategy);
         }

         return new AiToolCallParser.ToolCall("game:goto", out);
      } else {
         throw new IllegalArgumentException("Usage: .tools goto <x> <y> <z> [radius] [strategy] | goto <direction> <blocks> | goto <waypoint>");
      }
   }

   private static AiToolCallParser.ToolCall targetCountCall(List<String> args, String id, String key, String usage) {
      if (args.isEmpty()) {
         throw new IllegalArgumentException(usage);
      }

      boolean inlineValue = id.equals("game:mine") && args.size() > 1 && args.get(args.size() - 2).endsWith("=");
      boolean counted = !inlineValue && args.size() > 1 && args.get(args.size() - 1).matches("\\d+");
      JsonObject out = new JsonObject();
      if (!counted && args.size() > 1 && args.get(0).matches("\\d+")) {
         out.addProperty(key, String.join("_", args.subList(1, args.size())));
         out.addProperty("count", parseCount(args.get(0), usage));
         return new AiToolCallParser.ToolCall(id, out);
      }

      out.addProperty(key, joinArgs(args, counted ? args.size() - 1 : args.size()));
      if (counted) {
         out.addProperty("count", parseCount(args.get(args.size() - 1), usage));
      }

      return new AiToolCallParser.ToolCall(id, out);
   }

   static String joinArgs(List<String> args, int end) {
      return String.join("_", args.subList(0, end));
   }

   private static AiToolCallParser.ToolCall fillCall(List<String> args, double px, double py, double pz) {
      if (args.size() >= 7 && args.size() <= 8) {
         JsonObject out = new JsonObject();
         out.addProperty("x", parseBlockCoord(args.get(0), px));
         out.addProperty("y", parseBlockCoord(args.get(1), py));
         out.addProperty("z", parseBlockCoord(args.get(2), pz));
         out.addProperty("x2", parseBlockCoord(args.get(3), px));
         out.addProperty("y2", parseBlockCoord(args.get(4), py));
         out.addProperty("z2", parseBlockCoord(args.get(5), pz));
         out.addProperty("block", args.get(6));
         if (args.size() == 8) {
            String shape = args.get(7).toLowerCase(Locale.ROOT);
            if (shape.equals("hollow")) {
               out.addProperty("hollow", true);
            } else {
               if (!shape.equals("solid")) {
                  throw new IllegalArgumentException(
                     "Unknown shape '" + args.get(7) + "' (hollow|solid)\nUsage: .tools fill <x> <y> <z> <x2> <y2> <z2> <block> [hollow|solid]"
                  );
               }

               out.addProperty("hollow", false);
            }
         }

         return new AiToolCallParser.ToolCall("game:fill", out);
      } else {
         throw new IllegalArgumentException("Usage: .tools fill <x> <y> <z> <x2> <y2> <z2> <block> [hollow|solid]");
      }
   }

   private static AiToolCallParser.ToolCall digCall(List<String> args) {
      if (args.isEmpty() || !args.get(0).equalsIgnoreCase("tunnel") || args.size() > 3) {
         throw new IllegalArgumentException("Usage: .tools dig tunnel <north|south|east|west> [length]");
      }

      if (args.size() < 2) {
         throw new IllegalArgumentException("Usage: .tools dig tunnel <north|south|east|west> [length]");
      }

      String direction = args.get(1).toLowerCase(Locale.ROOT);
      if (!Set.of("north", "south", "east", "west").contains(direction)) {
         throw new IllegalArgumentException(
            "Direction must be north|south|east|west (a yaw works via .tools call game:dig shape=tunnel yaw=<deg>)\nUsage: .tools dig tunnel <north|south|east|west> [length]"
         );
      }

      JsonObject out = new JsonObject();
      out.addProperty("shape", "tunnel");
      out.addProperty("direction", direction);
      if (args.size() == 3) {
         int length = parseCount(args.get(2), "Usage: .tools dig tunnel <north|south|east|west> [length]");
         if (length < 1 || length > 128) {
            throw new IllegalArgumentException("Length must be 1..128\nUsage: .tools dig tunnel <north|south|east|west> [length]");
         }

         out.addProperty("length", length);
      }

      return new AiToolCallParser.ToolCall("game:dig", out);
   }

   private static AiToolCallParser.ToolCall noArgCall(List<String> args, String id, String usage) {
      if (!args.isEmpty()) {
         throw new IllegalArgumentException(usage);
      } else {
         return new AiToolCallParser.ToolCall(id, new JsonObject());
      }
   }

   private static int parseCount(String token, String usage) {
      if (!token.matches("\\d+")) {
         throw new IllegalArgumentException("Bad count '" + token + "'\n" + usage);
      }

      int value;
      try {
         value = Integer.parseInt(token);
      } catch (NumberFormatException e) {
         throw new IllegalArgumentException("Count out of range '" + token + "'\n" + usage);
      }

      if (value < 1) {
         throw new IllegalArgumentException("Count must be >= 1\n" + usage);
      } else {
         return value;
      }
   }

   public static List<String> nearestIds(String input, Collection<String> ids, int max) {
      if (input != null && !input.isBlank() && ids != null && !ids.isEmpty()) {
         String needle = input.trim().toLowerCase(Locale.ROOT);
         String needleLocal = needle.contains(":") ? needle.substring(needle.indexOf(58) + 1) : needle;

         record Inner_DataUtil(String id, long score) {
         }

         List<Inner_DataUtil> scored = new ArrayList<>();

         for (String id : ids) {
            if (id != null) {
               String lower = id.toLowerCase(Locale.ROOT);
               String local = lower.contains(":") ? lower.substring(lower.indexOf(58) + 1) : lower;
               long score;
               if (lower.startsWith(needle) || local.startsWith(needleLocal)) {
                  score = 1000L + Math.abs(lower.length() - needle.length());
               } else if (!lower.contains(needle) && !local.contains(needleLocal)) {
                  int distance = boundedEditDistance(needleLocal, local, 3);
                  if (distance > 3) {
                     continue;
                  }

                  score = 100000L + distance * 1000L + Math.abs(local.length() - needleLocal.length());
               } else {
                  int index = lower.contains(needle) ? lower.indexOf(needle) : local.indexOf(needleLocal);
                  score = 10000L + index + Math.abs(local.length() - needleLocal.length());
               }

               scored.add(new Inner_DataUtil(id, score));
            }
         }

         return scored.stream()
            .sorted(Comparator.comparingLong(Inner_DataUtil::score).thenComparing(Inner_DataUtil::id))
            .limit(Math.max(1, max))
            .map(Inner_DataUtil::id)
            .toList();
      } else {
         return List.of();
      }
   }

   static int boundedEditDistance(String a, String b, int limit) {
      int[] previous = new int[b.length() + 1];
      int j = 0;

      while (j <= b.length()) {
         previous[j] = j++;
      }

      for (int i = 1; i <= a.length(); i++) {
         int[] current = new int[b.length() + 1];
         current[0] = i;
         int rowMin = current[0];

         for (int jx = 1; jx <= b.length(); jx++) {
            int cost = a.charAt(i - 1) == b.charAt(jx - 1) ? 0 : 1;
            current[jx] = Math.min(Math.min(current[jx - 1] + 1, previous[jx] + 1), previous[jx - 1] + cost);
            rowMin = Math.min(rowMin, current[jx]);
         }

         if (rowMin > limit) {
            return limit + 1;
         }

         previous = current;
      }

      return previous[b.length()];
   }

   public record ToolCall(String id, JsonObject arguments) {
   }
}
