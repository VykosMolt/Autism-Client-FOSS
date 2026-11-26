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
   public static final String init_Str = "Usage: .tools goto <x> <y> <z> [radius] [strategy] | goto <direction> <blocks> | goto <waypoint>";
   static final String load = "nofall";
   static final List<String> parkourSpeedFlags = List.of("speed0", "speed1", "speed2");
   public static final String read = "Usage: .tools parkour [nofall] [speed0|speed1|speed2] [<x> <y> <z> ...]. Flags can go before or after the destination. Omit the destination to pick checkpoints in Freecam.";
   static final int maxParkourDestinations = 32;
   public static final String write = "Usage: .tools mine <block[=amount][,block[=amount]...]> [count per block]";
   public static final String update = "Usage: .tools chop <block> [count]";
   public static final String sync = "Usage: .tools follow [type]";
   public static final String flush = "Usage: .tools fight <type> [count]";
   public static final String close = "Usage: .tools fill <x> <y> <z> <x2> <y2> <z2> <block> [hollow|solid]";
   public static final String open = "Usage: .tools dig tunnel <north|south|east|west> [length]";
   public static final String poll = "Usage: .tools smelt <item> [count]";
   public static final String push = "Usage: .tools craft <item> [count]";
   public static final String pop = "Usage: .tools obtain <item> [count]";
   public static final String tick = "Usage: .tools collect <item> [count]";
   public static final String run = "Usage: .tools trade <index|item>";
   public static final String get = "Usage: .tools events [since]";
   public static final String set = "Usage: .tools build <schematic name|blueprint file|{inline json}>";
   public static final String put = "Usage: .tools farm <wheat|carrots|potatoes|beetroots> [plots]";
   private static final Set<String> init_Set = Set.of(
      "north", "south", "east", "west", "forward", "forwards", "ahead", "back", "backward", "backwards", "left", "right", "up", "down", "n", "s", "e", "w"
   );

   private AiToolCallParser() {
   }

   public static List<String> init_List(String input) {
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

   public static JsonObject init_JsonObject(String input) {
      return init_JsonObject(init_List(input));
   }

   public static JsonObject init_JsonObject(List<String> tokens) {
      JsonObject out = new JsonObject();

      for (String token : tokens) {
         int eq = init_I(token);
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

         out.add(key, init_JsonElement_Str_I(raw, 0));
      }

      return out;
   }

   private static int init_I(String token) {
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

   public static JsonElement init_JsonElement(String raw) {
      return init_JsonElement_Str_I(raw, 0);
   }

   private static JsonElement init_JsonElement_Str_I(String raw, int depth) {
      if (depth > 16) {
         throw new IllegalArgumentException("Value nested too deeply (max 16)");
      }

      if (raw != null && !raw.isEmpty()) {
         if (raw.length() < 2 || !raw.startsWith("\"")) {
            char first = raw.charAt(0);
            if (first == '[') {
               if (!raw.endsWith("]")) {
                  throw new IllegalArgumentException("Unclosed '[' in " + load_Str(raw));
               }

               JsonArray array = new JsonArray();

               for (String element : init_List_Str_C(raw.substring(1, raw.length() - 1), ',')) {
                  if (element.isBlank()) {
                     throw new IllegalArgumentException("Empty array element in " + load_Str(raw));
                  }

                  array.add(init_JsonElement_Str_I(element.trim(), depth + 1));
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
                  throw new IllegalArgumentException("Unclosed '{' in " + load_Str(raw));
               }

               JsonObject object = new JsonObject();
               String body = raw.substring(1, raw.length() - 1);

               for (String entry : init_List_Str_C(body, ',')) {
                  if (entry.isBlank()) {
                     if (!body.isBlank()) {
                        throw new IllegalArgumentException("Empty object entry in " + load_Str(raw));
                     }
                     break;
                  }

                  int colon = load_I(entry);
                  if (colon < 0) {
                     throw new IllegalArgumentException("Expected k:v in object entry '" + entry.trim() + "'");
                  }

                  String key = entry.substring(0, colon).trim();
                  if (key.length() >= 2 && key.startsWith("\"") && key.endsWith("\"")) {
                     key = init_Str(key.substring(1, key.length() - 1));
                  }

                  if (key.isEmpty()) {
                     throw new IllegalArgumentException("Empty object key in " + load_Str(raw));
                  }

                  String value = entry.substring(colon + 1).trim();
                  if (value.isEmpty()) {
                     throw new IllegalArgumentException("Missing value for object key '" + key + "'");
                  }

                  object.add(key, init_JsonElement_Str_I(value, depth + 1));
               }

               return object;
            }
         } else if (!raw.endsWith("\"")) {
            throw new IllegalArgumentException("Unterminated \" in " + raw);
         } else {
            return new JsonPrimitive(init_Str(raw.substring(1, raw.length() - 1)));
         }
      } else {
         throw new IllegalArgumentException("Empty value");
      }
   }

   private static int load_I(String token) {
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

   private static List<String> init_List_Str_C(String body, char separator) {
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

   private static String init_Str(String body) {
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

   private static String load_Str(String raw) {
      return raw.length() <= 24 ? raw : raw.substring(0, 21) + "...";
   }

   public static double init_D_Str_D(String token, double base) {
      if (token != null && !token.isBlank()) {
         String text = token.trim();
         if (text.startsWith("~")) {
            String offset = text.substring(1);
            double value = offset.isEmpty() ? 0.0 : init_D_Str_Str(offset, token);
            return base + value;
         } else {
            return init_D_Str_Str(text, token);
         }
      } else {
         throw new IllegalArgumentException("Missing coordinate");
      }
   }

   private static double init_D_Str_Str(String text, String token) {
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

   public static int init_I_Str_D(String token, double base) {
      return (int)Math.floor(init_D_Str_D(token, base));
   }

   public static AiToolCallParser.Inner_StreamUtil init(String sub, List<String> args, double px, double py, double pz) {
      AiToolCallParser.Inner_StreamUtil var10000;
      switch (sub) {
         case "goto":
            var10000 = load(args, px, py, pz);
            break;
         case "parkour":
            var10000 = init(args, px, py, pz);
            break;
         case "mine":
            AiToolCallParser.Inner_StreamUtil mine = init(args, "game:mine", "block", "Usage: .tools mine <block[=amount][,block[=amount]...]> [count per block]");
            var10000 = new AiToolCallParser.Inner_StreamUtil("game:mine", AiMiningQuota.init_JsonObject_JsonObject(mine.arguments()));
            break;
         case "chop":
            var10000 = init(args, "game:chop", "block", "Usage: .tools chop <block> [count]");
            break;
         case "farm":
            AiToolCallParser.Inner_StreamUtil farm = init(args, "game:farm", "crop", "Usage: .tools farm <wheat|carrots|potatoes|beetroots> [plots]");
            farm.arguments().addProperty("action", "build");
            var10000 = farm;
            break;
         case "follow":
            JsonObject out = new JsonObject();
            if (!args.isEmpty()) {
               out.addProperty("type", init_Str_List_I(args, args.size()));
            }

            var10000 = new AiToolCallParser.Inner_StreamUtil("game:follow", out);
            break;
         case "fight":
            var10000 = init(args, "game:fight", "target", "Usage: .tools fight <type> [count]");
            break;
         case "fill":
            var10000 = read(args, px, py, pz);
            break;
         case "dig":
            var10000 = init_cy_StreamUtil(args);
            break;
         case "surface":
            var10000 = init(args, "game:surface", "Usage: .tools surface");
            break;
         case "cave":
            var10000 = init(args, "game:cave", "Usage: .tools cave");
            break;
         case "eat":
            var10000 = init(args, "game:eat", "Usage: .tools eat");
            break;
         case "sleep":
            var10000 = init(args, "game:sleep", "Usage: .tools sleep");
            break;
         case "smelt":
            var10000 = init(args, "game:smelt_auto", "item", "Usage: .tools smelt <item> [count]");
            break;
         case "craft":
            var10000 = init(args, "game:craft", "item", "Usage: .tools craft <item> [count]");
            break;
         case "obtain":
         case "get":
            var10000 = init(args, "game:obtain", "item", "Usage: .tools obtain <item> [count]");
            break;
         case "collect":
         case "gather":
            AiToolCallParser.Inner_StreamUtil collect = init(args, "game:collect", "item", "Usage: .tools collect <item> [count]");
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
            String value = init_Str_List_I(args, args.size());
            if (value.matches("\\d+")) {
               out2.addProperty("tradeIndex", Integer.parseInt(value));
            } else {
               out2.addProperty("item", value);
            }

            var10000 = new AiToolCallParser.Inner_StreamUtil("game:trade", out2);
            break;
         }
         case "events": {
            if (args.size() > 1) {
               throw new IllegalArgumentException("Usage: .tools events [since]");
            }

            JsonObject out3 = new JsonObject();
            if (!args.isEmpty()) {
               int since = init_I_Str_Str(args.get(0), "Usage: .tools events [since]");
               if (since < 0) {
                  throw new IllegalArgumentException("since must be >= 0\nUsage: .tools events [since]");
               }

               out3.addProperty("since", since);
            }

            var10000 = new AiToolCallParser.Inner_StreamUtil("game:events", out3);
            break;
         }
         default:
            throw new IllegalArgumentException("Unknown shorthand '" + sub + "'");
      }

      return var10000;
   }

   private static AiToolCallParser.Inner_StreamUtil init(List<String> args, double px, double py, double pz) {
      AiToolCallParser.Inner_CachePool options = parseParkourFlags(args);
      String usage = read;
      args = args.stream().filter(arg -> !isParkourFlag(arg)).toList();

      AiToolCallParser.Inner_StreamUtil trip;
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
            trip = new AiToolCallParser.Inner_StreamUtil("game:goto", dest);
         } else {
            trip = load(args, px, py, pz);
         }
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException(e.getMessage().replace(init_Str, usage));
      }

      return new AiToolCallParser.Inner_StreamUtil(trip.id(), init_JsonObject_JsonObject_ParkourOptions(trip.arguments(), options));
   }

   private static JsonObject parseCoordsObject(List<String> coords, double px, double py, double pz) {
      JsonObject out = new JsonObject();
      out.addProperty("x", init_I_Str_D(coords.get(0), px));
      out.addProperty("y", init_I_Str_D(coords.get(1), py));
      out.addProperty("z", init_I_Str_D(coords.get(2), pz));
      return out;
   }

   public static boolean init_Z(String token) {
      return "nofall".equalsIgnoreCase(token) || "--no-fall-damage".equalsIgnoreCase(token);
   }

   public static boolean isParkourFlag(String token) {
      return init_Z(token) || parkourSpeedFlags.contains(token.toLowerCase(Locale.ROOT));
   }

   public static AiToolCallParser.Inner_CachePool parseParkourFlags(List<String> args) {
      boolean noFall = false;
      int speed = -1;

      for (String token : args) {
         if (init_Z(token)) {
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

      return new AiToolCallParser.Inner_CachePool(noFall, speed);
   }

   static JsonObject init_JsonObject_JsonObject_Z(JsonObject destination, boolean assumeNoDamage) {
      return init_JsonObject_JsonObject_ParkourOptions(destination, new AiToolCallParser.Inner_CachePool(assumeNoDamage, -1));
   }

   static JsonObject init_JsonObject_JsonObject_ParkourOptions(JsonObject destination, AiToolCallParser.Inner_CachePool options) {
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

   public record Inner_CachePool(boolean noFall, int speedLevel) {
   }

   private static AiToolCallParser.Inner_StreamUtil load(List<String> args, double px, double py, double pz) {
      if (args.size() >= 2 && args.get(0).equalsIgnoreCase("to")) {
         args = args.subList(1, args.size());
      }

      if (args.size() == 2) {
         String a = args.get(0).toLowerCase(Locale.ROOT);
         String b = args.get(1).toLowerCase(Locale.ROOT);
         if (init_Set.contains(a) && b.matches("\\d+(\\.\\d+)?")) {
            return new AiToolCallParser.Inner_StreamUtil("game:goto", AiJson.objectOf("direction", a, "distance", Double.parseDouble(b)));
         }

         if (init_Set.contains(b) && a.matches("\\d+(\\.\\d+)?")) {
            return new AiToolCallParser.Inner_StreamUtil("game:goto", AiJson.objectOf("direction", b, "distance", Double.parseDouble(a)));
         }
      }

      if (args.size() == 1 && !args.get(0).matches("-?~?\\d.*")) {
         return new AiToolCallParser.Inner_StreamUtil("game:goto", AiJson.objectOf("waypoint", args.get(0)));
      }

      if (args.size() >= 3 && args.size() <= 5) {
         JsonObject out = new JsonObject();
         out.addProperty("x", init_I_Str_D(args.get(0), px));
         out.addProperty("y", init_I_Str_D(args.get(1), py));
         out.addProperty("z", init_I_Str_D(args.get(2), pz));
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

         return new AiToolCallParser.Inner_StreamUtil("game:goto", out);
      } else {
         throw new IllegalArgumentException("Usage: .tools goto <x> <y> <z> [radius] [strategy] | goto <direction> <blocks> | goto <waypoint>");
      }
   }

   private static AiToolCallParser.Inner_StreamUtil init(List<String> args, String id, String key, String usage) {
      if (args.isEmpty()) {
         throw new IllegalArgumentException(usage);
      }

      boolean inlineValue = id.equals("game:mine") && args.size() > 1 && args.get(args.size() - 2).endsWith("=");
      boolean counted = !inlineValue && args.size() > 1 && args.get(args.size() - 1).matches("\\d+");
      JsonObject out = new JsonObject();
      if (!counted && args.size() > 1 && args.get(0).matches("\\d+")) {
         out.addProperty(key, String.join("_", args.subList(1, args.size())));
         out.addProperty("count", init_I_Str_Str(args.get(0), usage));
         return new AiToolCallParser.Inner_StreamUtil(id, out);
      }

      out.addProperty(key, init_Str_List_I(args, counted ? args.size() - 1 : args.size()));
      if (counted) {
         out.addProperty("count", init_I_Str_Str(args.get(args.size() - 1), usage));
      }

      return new AiToolCallParser.Inner_StreamUtil(id, out);
   }

   static String init_Str_List_I(List<String> args, int end) {
      return String.join("_", args.subList(0, end));
   }

   private static AiToolCallParser.Inner_StreamUtil read(List<String> args, double px, double py, double pz) {
      if (args.size() >= 7 && args.size() <= 8) {
         JsonObject out = new JsonObject();
         out.addProperty("x", init_I_Str_D(args.get(0), px));
         out.addProperty("y", init_I_Str_D(args.get(1), py));
         out.addProperty("z", init_I_Str_D(args.get(2), pz));
         out.addProperty("x2", init_I_Str_D(args.get(3), px));
         out.addProperty("y2", init_I_Str_D(args.get(4), py));
         out.addProperty("z2", init_I_Str_D(args.get(5), pz));
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

         return new AiToolCallParser.Inner_StreamUtil("game:fill", out);
      } else {
         throw new IllegalArgumentException("Usage: .tools fill <x> <y> <z> <x2> <y2> <z2> <block> [hollow|solid]");
      }
   }

   private static AiToolCallParser.Inner_StreamUtil init_cy_StreamUtil(List<String> args) {
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
         int length = init_I_Str_Str(args.get(2), "Usage: .tools dig tunnel <north|south|east|west> [length]");
         if (length < 1 || length > 128) {
            throw new IllegalArgumentException("Length must be 1..128\nUsage: .tools dig tunnel <north|south|east|west> [length]");
         }

         out.addProperty("length", length);
      }

      return new AiToolCallParser.Inner_StreamUtil("game:dig", out);
   }

   private static AiToolCallParser.Inner_StreamUtil init(List<String> args, String id, String usage) {
      if (!args.isEmpty()) {
         throw new IllegalArgumentException(usage);
      } else {
         return new AiToolCallParser.Inner_StreamUtil(id, new JsonObject());
      }
   }

   private static int init_I_Str_Str(String token, String usage) {
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

   public static List<String> init(String input, Collection<String> ids, int max) {
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
                  int distance = init(needleLocal, local, 3);
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

   static int init(String a, String b, int limit) {
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

   public record Inner_StreamUtil(String id, JsonObject arguments) {
   }
}
