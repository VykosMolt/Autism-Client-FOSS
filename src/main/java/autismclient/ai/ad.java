package autismclient.ai;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

final class ad {
   private static final Pattern init_Pattern = Pattern.compile(
      "\\s*(?:,\\s*(?:and\\s+)?(?:then\\s+)?|;\\s*|\\.\\s+|\\s+and\\s+then\\s+|\\s+then\\s+|\\s+and\\s+|\\s+after\\s+that\\s*,?\\s*|\\s+afterwards\\s*,?\\s*)",
      2
   );
   private static final Pattern load_Pattern = Pattern.compile(
      "^(?:(?:please|pls|now|first|then|also|and|next|finally|go\\s+and|can\\s+you|could\\s+you|would\\s+you|will\\s+you|i\\s+want\\s+you\\s+to|i\\s+need\\s+you\\s+to|i\\s+want\\s+to|i\\s+need\\s+to|i\\s+wanna|i\\s+need|i\\s+want|lets|let's)\\s+)+"
   );
   private static final Pattern read_Pattern = Pattern.compile(
      "^(?:please\\s+|pls\\s+)?(?:i\\s+(?:really\\s+)?(?:want|need|would\\s+like|'d\\s+like|wanna\\s+(?:have|get)|want\\s+to\\s+(?:get|have)|need\\s+to\\s+(?:get|have))|i'd\\s+like|give\\s+me|gimme|hand\\s+me|(?:can|could|may)\\s+(?:i|u|you)\\s+(?:get|have|give\\s+me|grab|fetch))\\s+(?!you\\b|to\\b)"
   );
   private static final Pattern write_Pattern = Pattern.compile("^(?:please\\s+|pls\\s+)?(?:can|could|would|will|may)\\s+(?:you|u|i)\\s+");
   private static final Pattern update_Pattern = Pattern.compile(
      "(?:\\s+(?:please|pls|now|for\\s+me|right\\s+now|asap|thanks?|thank\\s+you|from\\s+scratch|from\\s+nothing|from\\s+zero|quickly|quick|fast|real\\s+quick|as\\s+fast\\s+as\\s+(?:you\\s+can|possible)|if\\s+you\\s+can|by\\s+yourself|yourself|on\\s+your\\s+own|bro|mate|dude))+$"
   );
   static final String init_Str = "(?:(\\d{1,4})\\s*x?\\s+|x(\\d{1,4})\\s+|(a\\s+stack\\s+of|one\\s+stack\\s+of|a\\s+couple(?:\\s+of)?|a\\s+few|an?|one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twenty|thirty|thirty[- ]two|forty|fifty|sixty|sixty[- ]four|some|more|a\\s+bit\\s+of|a\\s+lot\\s+of|lots\\s+of)\\s+|(\\d{1,2})\\s+stacks?\\s+of\\s+)?";
   private static final Map<String, Integer> init_Map = Map.ofEntries(
      Map.entry("a", 1),
      Map.entry("an", 1),
      Map.entry("one", 1),
      Map.entry("two", 2),
      Map.entry("three", 3),
      Map.entry("four", 4),
      Map.entry("five", 5),
      Map.entry("six", 6),
      Map.entry("seven", 7),
      Map.entry("eight", 8),
      Map.entry("nine", 9),
      Map.entry("ten", 10),
      Map.entry("eleven", 11),
      Map.entry("twelve", 12),
      Map.entry("thirteen", 13),
      Map.entry("fourteen", 14),
      Map.entry("fifteen", 15),
      Map.entry("sixteen", 16),
      Map.entry("seventeen", 17),
      Map.entry("eighteen", 18),
      Map.entry("nineteen", 19),
      Map.entry("twenty", 20),
      Map.entry("thirty", 30),
      Map.entry("thirty-two", 32),
      Map.entry("thirty two", 32),
      Map.entry("forty", 40),
      Map.entry("fifty", 50),
      Map.entry("sixty", 60),
      Map.entry("sixty-four", 64),
      Map.entry("sixty four", 64),
      Map.entry("a few", 3),
      Map.entry("a stack of", 64),
      Map.entry("one stack of", 64)
   );
   private static final Set<String> init_Set = Set.of("some", "more", "a bit of", "a lot of", "lots of");
   private static final Set<String> load_Set = Set.of(
      "log", "planks", "cobblestone", "stone", "dirt", "sand", "gravel", "stick", "wheat_seeds", "torch", "coal", "wool", "glass"
   );
   private static final int write_I = 16;
   private static final Pattern sync_Pattern = init_Pattern(
      "get\\s+me|go\\s+get|go\\s+find|go\\s+grab|go\\s+fetch|find\\s+me|bring\\s+me|get|obtain|collect|gather|grab|fetch|acquire|find|look\\s+for|search\\s+for"
   );
   private static final Pattern flush = init_Pattern("mine|dig\\s+up|dig\\s+for");
   private static final Pattern close = Pattern.compile(
      "^(?:(\\d{1,4})\\s*x?\\s+|x(\\d{1,4})\\s+|(a\\s+stack\\s+of|one\\s+stack\\s+of|a\\s+couple(?:\\s+of)?|a\\s+few|an?|one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twenty|thirty|thirty[- ]two|forty|fifty|sixty|sixty[- ]four|some|more|a\\s+bit\\s+of|a\\s+lot\\s+of|lots\\s+of)\\s+|(\\d{1,2})\\s+stacks?\\s+of\\s+)?([a-z][a-z _'-]*?)$"
   );
   private static final Pattern open = init_Pattern("craft|make|build\\s+me");
   private static final Pattern poll = init_Pattern("smelt|cook|bake|roast");
   private static final Pattern push = init_Pattern("harvest|farm");
   private static final Pattern pop = Pattern.compile(
      "^(?:chop|cut)(?:\\s+down)?\\s+(?:(\\d{1,4})\\s*x?\\s+|x(\\d{1,4})\\s+|(a\\s+stack\\s+of|one\\s+stack\\s+of|a\\s+couple(?:\\s+of)?|a\\s+few|an?|one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twenty|thirty|thirty[- ]two|forty|fifty|sixty|sixty[- ]four|some|more|a\\s+bit\\s+of|a\\s+lot\\s+of|lots\\s+of)\\s+|(\\d{1,2})\\s+stacks?\\s+of\\s+)?(?:(?:the\\s+)?(?:nearest\\s+|closest\\s+)?(?:([a-z ]+?)\\s+)?(?:trees?|logs?|wood))$"
   );
   private static final Pattern tick = Pattern.compile(
      "^(?:(?:plant|sow)\\s+(?:a\\s+|some\\s+)?(?:small\\s+|little\\s+|big\\s+)?([a-z ]+?)(?:\\s+(?:farm|field|seeds))?|(?:make|build|create|start|set\\s+up)\\s+(?:a\\s+|an\\s+)?(?:small\\s+|little\\s+|big\\s+)?([a-z ]+?)\\s+(?:farm|field))$"
   );
   private static final Pattern run = Pattern.compile(
      "^(?:kill|fight|attack|slay|hunt)\\s+(?:the\\s+)?(?:nearest\\s+|closest\\s+|nearby\\s+|all\\s+(?:the\\s+)?)?(?:(\\d{1,4})\\s*x?\\s+|x(\\d{1,4})\\s+|(a\\s+stack\\s+of|one\\s+stack\\s+of|a\\s+couple(?:\\s+of)?|a\\s+few|an?|one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twenty|thirty|thirty[- ]two|forty|fifty|sixty|sixty[- ]four|some|more|a\\s+bit\\s+of|a\\s+lot\\s+of|lots\\s+of)\\s+|(\\d{1,2})\\s+stacks?\\s+of\\s+)?([a-z ]+?)(?:\\s+(?:nearby|around\\s+here|near\\s+me))?$"
   );
   private static final Pattern get = Pattern.compile(
      "^(?:turn|switch|toggle)\\s+(on|off)\\s+(?:the\\s+)?(.+?)(?:\\s+module)?$|^(?:turn|switch)\\s+(?:the\\s+)?(.+?)(?:\\s+module)?\\s+(on|off)$|^(enable|disable|activate|deactivate)\\s+(?:the\\s+)?(.+?)(?:\\s+module)?$"
   );
   private static final Pattern set = Pattern.compile(
      "^(?:drop|throw\\s+out|throw\\s+away|empty)\\s+(?:out\\s+)?(?:all\\s+(?:of\\s+)?)?(?:my\\s+|the\\s+)?(?:whole\\s+|entire\\s+|full\\s+)?(?:inventory|everything|all\\s+(?:my\\s+)?(?:items|stuff)|all|stuff)$"
   );
   private static final Pattern put = Pattern.compile(
      "^drop\\s+(?:the\\s+|my\\s+)?(?:held\\s+item|item\\s+in\\s+my\\s+hand|what\\s+i(?:'m|\\s+am)\\s+holding|hand)$"
   );
   private static final Pattern add = Pattern.compile(
      "^(?:drop|throw\\s+away|throw\\s+out)\\s+(?:(\\d{1,4})\\s*x?\\s+|x(\\d{1,4})\\s+|(a\\s+stack\\s+of|one\\s+stack\\s+of|a\\s+couple(?:\\s+of)?|a\\s+few|an?|one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twenty|thirty|thirty[- ]two|forty|fifty|sixty|sixty[- ]four|some|more|a\\s+bit\\s+of|a\\s+lot\\s+of|lots\\s+of)\\s+|(\\d{1,2})\\s+stacks?\\s+of\\s+)?(?:my\\s+|the\\s+|all\\s+(?:my\\s+|the\\s+)?)?([a-z][a-z _-]*?)$"
   );
   private static final Pattern clear = Pattern.compile("^(?:stop|halt|stay(?:\\s+there)?|wait(?:\\s+there)?|stand\\s+still|that'?s\\s+it|done)$");
   private static final Pattern reset = Pattern.compile(
      "^(?:beat|finish|complete|win)\\s+(?:the\\s+game|minecraft)|speed\\s*run(?:\\s+(?:the\\s+game|minecraft))?|(?:kill|beat|defeat)\\s+the\\s+(?:ender\\s+)?dragon$"
   );
   private static final Pattern start = Pattern.compile(
      "^(?:eat(?:\\s+(?:something|some\\s+food|food|a\\s+snack|it|them|that|those|up|the\\s+(?:meat|food)|my\\s+(?:meat|food)))?|have\\s+a\\s+snack|heal\\s+up|fill\\s+(?:up\\s+)?(?:my\\s+)?(?:hunger|food)(?:\\s+bar)?)$"
   );
   private static final Pattern stop = Pattern.compile("^(?:sleep|go\\s+to\\s+(?:sleep|bed)|sleep\\s+in\\s+(?:a|the)\\s+bed|skip\\s+the\\s+night)$");
   private static final Pattern next = Pattern.compile(
      "^(?:equip|wear|put\\s+on|hold|take\\s+out)\\s+(?:my\\s+|the\\s+|a\\s+)?(?:best\\s+)?(armor|armour|sword|weapon|shield)$"
   );
   private static final Pattern prev = Pattern.compile(
      "^(?:clean(?:\\s+up)?|tidy(?:\\s+up)?|sort\\s+out|clear\\s+out|empty)\\s+(?:my\\s+|the\\s+)?(?:inventory|inv|bag|backpack)|(?:throw|toss|get\\s+rid\\s+of|dump|drop)\\s+(?:out\\s+|away\\s+)?(?:all\\s+)?(?:the\\s+|my\\s+)?(?:junk|trash|garbage|useless\\s+(?:stuff|items|things|shit)|crap|shit)(?:\\s+(?:out|away))?|make\\s+(?:some\\s+)?(?:room|space)(?:\\s+in\\s+(?:my\\s+|the\\s+)?(?:inventory|inv|bag))?$"
   );
   private static final Pattern valid = Pattern.compile(
      "^(?:put|place|set)\\s+(?:(it|that|them)|(?:the|a|an|my)\\s+([a-z][a-z ]*?))(?:\\s+down)?(?:\\s+(?:next\\s+to\\s+me|near\\s+me|beside\\s+me|by\\s+me|here|right\\s+here))?(?:\\s+down)?$"
   );
   private static final Pattern size = Pattern.compile(
      "^(?:fill|top\\s+up)\\s+(?:it|them|that|the\\s+bucket|a\\s+bucket|my\\s+bucket|(?:the\\s+|my\\s+)?buckets)(?:\\s+up)?(?:\\s+with\\s+water)?$|^(?:get|grab|fetch)\\s+(?:some\\s+)?water$"
   );
   private static final Pattern data = Pattern.compile("^(?:go\\s+)?fish(?:ing)?$");
   private static final Pattern value = Pattern.compile(
      "^(?:surface|go\\s+(?:up\\s+)?to\\s+the\\s+surface|get\\s+(?:out\\s+of\\s+(?:this|the)\\s+cave|to\\s+the\\s+surface|back\\s+to\\s+the\\s+surface))$"
   );
   private static final Pattern cache = Pattern.compile(
      "^(?:recover|get\\s+back|pick\\s+up|collect|go\\s+get)\\s+(?:all\\s+)?(?:my\\s+)?(?:stuff|items|things|loot|drops)(?:\\s+(?:back|from\\s+where\\s+i\\s+died|from\\s+my\\s+death))?$"
   );
   private static final Map<String, String> load_Map = Map.ofEntries(
      Map.entry("iron", "minecraft:iron_ingot"),
      Map.entry("raw iron", "minecraft:iron_ingot"),
      Map.entry("iron ore", "minecraft:iron_ingot"),
      Map.entry("gold", "minecraft:gold_ingot"),
      Map.entry("raw gold", "minecraft:gold_ingot"),
      Map.entry("gold ore", "minecraft:gold_ingot"),
      Map.entry("copper", "minecraft:copper_ingot"),
      Map.entry("raw copper", "minecraft:copper_ingot"),
      Map.entry("copper ore", "minecraft:copper_ingot"),
      Map.entry("beef", "minecraft:cooked_beef"),
      Map.entry("steak", "minecraft:cooked_beef"),
      Map.entry("pork", "minecraft:cooked_porkchop"),
      Map.entry("porkchop", "minecraft:cooked_porkchop"),
      Map.entry("chicken", "minecraft:cooked_chicken"),
      Map.entry("mutton", "minecraft:cooked_mutton"),
      Map.entry("rabbit", "minecraft:cooked_rabbit"),
      Map.entry("cod", "minecraft:cooked_cod"),
      Map.entry("fish", "minecraft:cooked_cod"),
      Map.entry("salmon", "minecraft:cooked_salmon"),
      Map.entry("potato", "minecraft:baked_potato"),
      Map.entry("sand", "minecraft:glass"),
      Map.entry("cobblestone", "minecraft:stone"),
      Map.entry("cobble", "minecraft:stone"),
      Map.entry("clay", "minecraft:brick"),
      Map.entry("log", "minecraft:charcoal"),
      Map.entry("logs", "minecraft:charcoal"),
      Map.entry("wood", "minecraft:charcoal"),
      Map.entry("kelp", "minecraft:dried_kelp"),
      Map.entry("cactus", "minecraft:green_dye")
   );
   private static final Set<String> read_Set = Set.of("meat", "the meat", "my meat", "food", "the food", "my food", "it", "them", "that", "those");
   private static final Map<String, String> read_Map = Map.of(
      "minecraft:cow",
      "minecraft:beef",
      "minecraft:mooshroom",
      "minecraft:beef",
      "minecraft:pig",
      "minecraft:porkchop",
      "minecraft:chicken",
      "minecraft:chicken",
      "minecraft:sheep",
      "minecraft:mutton",
      "minecraft:rabbit",
      "minecraft:rabbit"
   );
   private static final Set<String> write_Set = Set.of("oak", "spruce", "birch", "jungle", "acacia", "dark oak", "mangrove", "cherry", "pale oak");
   private static final Set<String> update_Set = Set.of("mobs", "mob", "monsters", "monster", "hostiles", "hostile mobs", "hostile", "enemies", "enemy");
   private static final Pattern state = Pattern.compile(
      "^(?:make|build|create|start|set\\s+up|plant|sow|lay\\s+out)\\s+(?:me\\s+|us\\s+)?(.*?)\\b(?:farm|field|garden|crop\\s+field)s?\\b(.*)$"
   );
   private static final Pattern queue = Pattern.compile("(\\d{1,2})\\s*(?:x|by|\\*)\\s*(\\d{1,2})");
   private static final Pattern buffer = Pattern.compile("(\\d{1,3})\\s*(?:crops?|plots?|plants?|blocks?|seeds?|tiles?)?");
   private static final Set<String> sync_Set = Set.of(
      "a",
      "an",
      "some",
      "the",
      "of",
      "with",
      "size",
      "sized",
      "big",
      "for",
      "me",
      "us",
      "crops",
      "crop",
      "plots",
      "plot",
      "plants",
      "blocks",
      "tiles",
      "seeds",
      "square",
      "area",
      "one",
      "new",
      "nice"
   );
   static final int init_I = 9;
   static final int load_I = 25;
   static final int read_I = 80;
   static final List<String> init_List = List.of("Wheat", "Carrots", "Potatoes", "Beetroots", "Nether wart");
   static final List<String> load_List = List.of("Small (about 10 crops)", "Medium (about 25 crops)", "Large (9x9, 80 crops around one water block)");

   private ad() {
   }

   private static Pattern init_Pattern(String verbs) {
      return Pattern.compile(
         "^(?:"
            + verbs
            + ")\\s+(?:me\\s+|us\\s+)?(?:(\\d{1,4})\\s*x?\\s+|x(\\d{1,4})\\s+|(a\\s+stack\\s+of|one\\s+stack\\s+of|a\\s+couple(?:\\s+of)?|a\\s+few|an?|one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twenty|thirty|thirty[- ]two|forty|fifty|sixty|sixty[- ]four|some|more|a\\s+bit\\s+of|a\\s+lot\\s+of|lots\\s+of)\\s+|(\\d{1,2})\\s+stacks?\\s+of\\s+)?([a-z][a-z _'-]*?)$"
      );
   }

   static ad.Inner_CachePool init_ad_CachePool(String objective, ad.Inner_StreamUtil lookups) {
      ad.Inner_BufferHelper schematic = init_ad_BufferHelper(objective);
      if (schematic != null) {
         return new ad.Inner_CachePool(List.of(schematic), "");
      }

      if (objective != null && !objective.isBlank() && objective.length() <= 400) {
         String text = objective.trim();
         if (text.endsWith("?")) {
            String asked = text.replaceAll("\\?+$", "").trim();
            if (!write_Pattern.matcher(asked.toLowerCase(Locale.ROOT)).find()) {
               return new ad.Inner_CachePool(List.of(), text);
            }

            text = asked;
         }

         List<int[]> spans = new ArrayList<>();
         Matcher m = init_Pattern.matcher(text);

         int at;
         for (at = 0; m.find(); at = m.end()) {
            if (m.start() > at) {
               spans.add(new int[]{at, m.start()});
            }
         }

         if (at < text.length()) {
            spans.add(new int[]{at, text.length()});
         }

         List<ad.Inner_BufferHelper> steps = new ArrayList<>();
         String lastVerb = null;

         for (int[] span : spans) {
            String clause = text.substring(span[0], span[1]);
            if (!clear.matcher(init_Str(clause)).matches() || steps.isEmpty()) {
               ad.Inner_BufferHelper step = lastVerb != null && flush(init_Str(clause)) == null
                  ? init_ad_BufferHelper(lastVerb + " " + init_Str(clause), lookups)
                  : null;
               if (step == null) {
                  step = init_ad_BufferHelper(clause, lookups);
               }

               if (step == null) {
                  return new ad.Inner_CachePool(steps, text.substring(span[0]).trim());
               }

               steps.add(step);
               lastVerb = flush(init_Str(clause));
            }
         }

         return new ad.Inner_CachePool(init(load(read(steps))), "");
      } else {
         return new ad.Inner_CachePool(List.of(), objective == null ? "" : objective.trim());
      }
   }

   static List<ad.Inner_BufferHelper> init(List<ad.Inner_BufferHelper> steps) {
      List<ad.Inner_BufferHelper> out = new ArrayList<>(steps);

      for (int i = 1; i < out.size(); i++) {
         ad.Inner_BufferHelper step = out.get(i);
         if (step.capability().equals("game:place_near") && ag.init_Str_JsonObject(step.arguments(), "item", "").isEmpty()) {
            String item = ag.init_Str_JsonObject(out.get(i - 1).arguments(), "item", "");
            if (!item.isEmpty() && !item.contains(",")) {
               out.set(
                  i,
                  new ad.Inner_BufferHelper(
                     step.clause(),
                     step.capability(),
                     ag.init_JsonObject_ObjArr("item", item),
                     "put " + item.replace("minecraft:", "") + " down beside the player",
                     step.asks()
                  )
               );
            }
         }
      }

      return out;
   }

   static List<ad.Inner_BufferHelper> load(List<ad.Inner_BufferHelper> steps) {
      List<ad.Inner_BufferHelper> out = new ArrayList<>(steps);

      for (int i = 1; i < out.size(); i++) {
         ad.Inner_BufferHelper step = out.get(i);
         ad.Inner_BufferHelper before = out.get(i - 1);
         if (!init_Z(step.clause()) && step.arguments().has("count")) {
            String output = ag.init_Str_JsonObject(step.arguments(), "item", "");
            String input = ag.init_Str_JsonObject(before.arguments(), "item", ag.init_Str_JsonObject(before.arguments(), "block", ""))
               .replace("minecraft:", "")
               .replace('_', ' ');
            if (!output.isEmpty() && !input.isEmpty() && output.equals(load_Map.get(input)) && before.arguments().has("count")) {
               int count = Math.max(1, ag.init_I_JsonObject(before.arguments(), "count", 1));
               JsonObject args = step.arguments().deepCopy();
               args.addProperty("count", count);
               out.set(
                  i,
                  new ad.Inner_BufferHelper(step.clause(), step.capability(), args, step.describe().replaceFirst("\\d+", String.valueOf(count)), step.asks())
               );
            }
         }
      }

      return out;
   }

   static boolean init_Z(String clause) {
      String c = clause.toLowerCase(Locale.ROOT);
      if (c.matches(".*\\d.*")) {
         return true;
      }

      for (String word : c.split("[^a-z]+")) {
         if (init_Map.containsKey(word)) {
            return true;
         }
      }

      return false;
   }

   static List<ad.Inner_BufferHelper> read(List<ad.Inner_BufferHelper> steps) {
      List<ad.Inner_BufferHelper> out = new ArrayList<>(steps);

      for (int i = 0; i < out.size(); i++) {
         ad.Inner_BufferHelper step = out.get(i);
         if (step.capability().equals("game:fight")) {
            boolean usedLater = false;

            for (ad.Inner_BufferHelper later : out.subList(i + 1, out.size())) {
               usedLater |= later.capability().equals("game:eat") || ag.init_Z_JsonObject(later.arguments(), "cookHeld", false);
            }

            if (usedLater) {
               List<String> meats = new ArrayList<>();

               for (String target : ag.init_Str_JsonObject(step.arguments(), "target", "").split(",")) {
                  String meat = read_Map.get(target);
                  if (meat == null) {
                     meats = null;
                     break;
                  }

                  if (!meats.contains(meat)) {
                     meats.add(meat);
                  }
               }

               if (meats != null && !meats.isEmpty()) {
                  int kills = Math.max(1, ag.init_I_JsonObject(step.arguments(), "count", 1));
                  out.set(
                     i,
                     new ad.Inner_BufferHelper(
                        step.clause(),
                        "game:obtain",
                        ag.init_JsonObject_ObjArr("item", String.join(",", meats), "count", kills, "additional", true),
                        "hunt for " + String.join(" or ", meats).replace("minecraft:", "") + (kills > 1 ? " x" + kills : "")
                     )
                  );
               }
            }
         }
      }

      return out;
   }

   static ad.Inner_BufferHelper init_ad_BufferHelper(String objective) {
      if (objective == null) {
         return null;
      }

      String request = objective.split("\\nAttached schematic files:", 2)[0].trim();
      Matcher m = Pattern.compile(
            "(?i)^(?:please\\s+)?(?:build|place|construct)\\s+(?:the\\s+)?(?:schematic\\s+)?(?:\"([^\"]+\\.(?:schem|schematic|litematic))\"|([^\\s]+\\.(?:schem|schematic|litematic)))(?:\\s+(?:here|at\\s+(-?\\d+)\\s+(-?\\d+)\\s+(-?\\d+)))?[.!]*$"
         )
         .matcher(request);
      if (!m.matches()) {
         return null;
      }

      try {
         String name = bP.init_Str(m.group(1) != null ? m.group(1) : m.group(2));
         JsonObject args = ag.init_JsonObject_ObjArr("schematic", name, "craftMissing", false, "timeoutSeconds", 2592000);
         if (m.group(3) != null) {
            args.add(
               "origin", ag.init_JsonObject_ObjArr("x", Integer.parseInt(m.group(3)), "y", Integer.parseInt(m.group(4)), "z", Integer.parseInt(m.group(5)))
            );
         }

         return new ad.Inner_BufferHelper(objective, "game:build", args, "build " + name);
      } catch (IllegalArgumentException e) {
         return null;
      }
   }

   static String init_Str(String clause) {
      String c = clause.trim().toLowerCase(Locale.ROOT).replaceAll("[.!]+$", "").replaceAll("\\s+", " ");
      c = read_Pattern.matcher(c).replaceFirst("get ");
      c = load_Pattern.matcher(c).replaceFirst("");
      c = update_Pattern.matcher(c).replaceFirst("");
      return c.trim();
   }

   private static String flush(String clause) {
      int space = clause.indexOf(32);
      String first = space < 0 ? clause : clause.substring(0, space);
      return Set.of("get", "collect", "gather", "craft", "make", "mine", "smelt", "cook", "obtain", "grab").contains(first) ? first : null;
   }

   static ad.Inner_BufferHelper init_ad_BufferHelper(String raw, ad.Inner_StreamUtil lookups) {
      String c = init_Str(raw);
      if (c.isEmpty()) {
         return null;
      }

      G.Inner_DataUtil move = G.init(c, lookups.waypoint());
      if (move != null) {
         return new ad.Inner_BufferHelper(raw, "game:goto", move.args(), move.describe());
      }

      Matcher m;
      if ((m = get.matcher(c)).matches()) {
         boolean on;
         String name;
         if (m.group(1) != null) {
            on = m.group(1).equals("on");
            name = m.group(2);
         } else if (m.group(3) != null) {
            on = m.group(4).equals("on");
            name = m.group(3);
         } else {
            on = m.group(5).equals("enable") || m.group(5).equals("activate");
            name = m.group(6);
         }

         String id = lookups.moduleId().apply(name.trim());
         return id == null
            ? null
            : new ad.Inner_BufferHelper(raw, "module:" + id, ag.init_JsonObject_ObjArr("enabled", on, "persistent", true), (on ? "turn on " : "turn off ") + id);
      } else {
         if (set.matcher(c).matches()) {
            return new ad.Inner_BufferHelper(raw, "command:drop", ag.init_JsonObject_ObjArr("body", "drop fullinventory"), "drop the whole inventory");
         }

         if (put.matcher(c).matches()) {
            return new ad.Inner_BufferHelper(raw, "command:drop", ag.init_JsonObject_ObjArr("body", "drop hand"), "drop the held item");
         }

         if (reset.matcher(c).find()) {
            return new ad.Inner_BufferHelper(raw, "game:speedrun", new JsonObject(), "speedrun: beat the game");
         }

         if (start.matcher(c).matches()) {
            return new ad.Inner_BufferHelper(raw, "game:eat", new JsonObject(), "eat");
         }

         if (stop.matcher(c).matches()) {
            return new ad.Inner_BufferHelper(raw, "game:sleep", new JsonObject(), "sleep in a bed");
         }

         if (data.matcher(c).matches()) {
            return new ad.Inner_BufferHelper(raw, "game:fish", new JsonObject(), "fish");
         }

         if ((m = valid.matcher(c)).matches()) {
            if (m.group(1) != null) {
               return new ad.Inner_BufferHelper(raw, "game:place_near", ag.init_JsonObject_ObjArr("item", ""), "put it down beside the player");
            }

            String id = read(load(m.group(2)));
            return id == null
               ? null
               : new ad.Inner_BufferHelper(
                  raw, "game:place_near", ag.init_JsonObject_ObjArr("item", id), "put " + id.replace("minecraft:", "") + " down beside the player"
               );
         } else {
            if (size.matcher(c).matches()) {
               return new ad.Inner_BufferHelper(
                  raw, "game:obtain", ag.init_JsonObject_ObjArr("item", "minecraft:water_bucket", "count", 1), "fill a bucket with water"
               );
            }

            if (value.matcher(c).matches()) {
               return new ad.Inner_BufferHelper(raw, "game:surface", new JsonObject(), "go to the surface");
            }

            if (cache.matcher(c).matches()) {
               return new ad.Inner_BufferHelper(raw, "game:recover", new JsonObject(), "recover the items from the last death");
            }

            if (prev.matcher(c).matches()) {
               return new ad.Inner_BufferHelper(
                  raw,
                  "game:tidy",
                  ag.init_JsonObject_ObjArr("action", "drop", "freeSlots", 9),
                  "throw out junk to free 9 slots (tools, food and valuables stay)"
               );
            }

            if ((m = next.matcher(c)).matches()) {
               String var10000;
               switch (m.group(1)) {
                  case "armor":
                  case "armour":
                     var10000 = "armor";
                     break;
                  case "shield":
                     var10000 = "shield";
                     break;
                  default:
                     var10000 = "weapon";
               }

               String what = var10000;
               return new ad.Inner_BufferHelper(raw, "game:equip", ag.init_JsonObject_ObjArr("what", what), "equip the best " + what);
            } else {
               ad.Inner_BufferHelper field = init(raw, c);
               if (field != null) {
                  return field;
               }

               if ((m = tick.matcher(c)).matches()) {
                  String crop = update(m.group(1) != null ? m.group(1) : m.group(2));
                  return crop == null
                     ? null
                     : new ad.Inner_BufferHelper(raw, "game:farm", ag.init_JsonObject_ObjArr("action", "plant", "crop", crop), "plant a " + crop + " field");
               }

               if ((m = pop.matcher(c)).matches()) {
                  Integer count = init(m);
                  String species = m.group(5) == null ? null : m.group(5).trim();
                  if (species != null && !write_Set.contains(species)) {
                     return null;
                  }

                  JsonObject args = new JsonObject();
                  if (species != null) {
                     args.addProperty("block", "minecraft:" + species.replace(' ', '_') + "_log");
                  }

                  boolean trees = c.contains("tree");
                  if (count != null && !trees) {
                     args.addProperty("count", count);
                  }

                  return trees && count != null && count > 1
                     ? null
                     : new ad.Inner_BufferHelper(
                        raw, "game:chop", args, "chop " + (species == null ? "the nearest tree" : species + " logs") + (args.has("count") ? " x" + count : "")
                     );
               } else if (!(m = run.matcher(c)).matches()) {
                  if ((m = push.matcher(c)).matches()) {
                     String crop = update(m.group(5));
                     if (crop == null) {
                        return null;
                     }

                     Integer count = init(m);
                     return new ad.Inner_BufferHelper(
                        raw, "game:farm", ag.init_JsonObject_ObjArr("action", "harvest", "crop", crop, "count", count == null ? 16 : count), "harvest " + crop
                     );
                  } else if ((m = poll.matcher(c)).matches()) {
                     if (read_Set.contains(m.group(5).trim())) {
                        return new ad.Inner_BufferHelper(
                           raw, "game:smelt_auto", ag.init_JsonObject_ObjArr("item", "cooked_food", "count", 1, "cookHeld", true), "cook the raw food held"
                        );
                     }

                     String what = load(m.group(5));
                     String output = load_Map.get(what);
                     if (output == null) {
                        output = load_Map.get(sync(what));
                     }

                     if (output == null) {
                        output = read(what);
                     }

                     if (output == null) {
                        return null;
                     }

                     int count = init(m, output, what);
                     return new ad.Inner_BufferHelper(
                        raw, "game:smelt_auto", ag.init_JsonObject_ObjArr("item", output, "count", count), "smelt " + count + " " + output
                     );
                  } else if ((m = add.matcher(c)).matches()) {
                     String what = load(m.group(5));
                     String id = read(what);
                     if (id == null) {
                        return null;
                     }

                     Integer count = init(m);
                     String body = "drop " + (count == null ? "" : count + " ") + id.replace("minecraft:", "");
                     return new ad.Inner_BufferHelper(raw, "command:drop", ag.init_JsonObject_ObjArr("body", body), body);
                  } else if ((m = flush.matcher(c)).matches()) {
                     String what = load(m.group(5));
                     String block = open(what);
                     if (block == null) {
                        String id = read(what);
                        if (id == null) {
                           return null;
                        }

                        int count = init(m, id, what);
                        return new ad.Inner_BufferHelper(
                           raw, "game:obtain", ag.init_JsonObject_ObjArr("item", id, "count", count, "additional", true), "get " + count + " " + id
                        );
                     } else {
                        int count = init(m, block, what);
                        return new ad.Inner_BufferHelper(
                           raw, "game:mine", ag.init_JsonObject_ObjArr("block", block, "count", count), "mine " + count + " " + block
                        );
                     }
                  } else if ((m = open.matcher(c)).matches()) {
                     String what = load(m.group(5));
                     String id = read(what);
                     if (id == null) {
                        return null;
                     }

                     int count = init(m, id, what);
                     return new ad.Inner_BufferHelper(raw, "game:craft", ag.init_JsonObject_ObjArr("item", id, "count", count), "craft " + count + " " + id);
                  } else if ((m = sync_Pattern.matcher(c)).matches()) {
                     String what = load(m.group(5));
                     String spec = close(what);
                     if (spec == null) {
                        return null;
                     }

                     int count = init(m, spec, what);
                     return new ad.Inner_BufferHelper(
                        raw, "game:obtain", ag.init_JsonObject_ObjArr("item", spec, "count", count, "additional", true), "get " + count + " " + spec
                     );
                  } else if ((m = close.matcher(c)).matches()) {
                     String what = load(m.group(5));
                     String spec = close(what);
                     if (spec == null) {
                        return null;
                     }

                     int count = init(m, spec, what);
                     return new ad.Inner_BufferHelper(
                        raw, "game:obtain", ag.init_JsonObject_ObjArr("item", spec, "count", count, "additional", true), "get " + count + " " + spec
                     );
                  } else {
                     return null;
                  }
               } else {
                  String who = m.group(5).trim();
                  List<String> targets = new ArrayList<>();

                  for (String one : who.split("\\s+or\\s+(?:an?\\s+|the\\s+)?")) {
                     String id = update_Set.contains(one.trim()) ? "hostiles" : lookups.entityId().apply(sync(one.trim()));
                     if (id == null) {
                        return null;
                     }

                     if (!targets.contains(id)) {
                        targets.add(id);
                     }
                  }

                  String target = String.join(",", targets);
                  Integer count = init(m);
                  JsonObject args = ag.init_JsonObject_ObjArr("target", target);
                  if (count != null && count > 1) {
                     args.addProperty("count", count);
                  }

                  return new ad.Inner_BufferHelper(raw, "game:fight", args, "fight " + target + (count != null && count > 1 ? " x" + count : ""));
               }
            }
         }
      }
   }

   static String load(String words) {
      String w = words.trim()
         .replaceAll("^(?:of|the|my|some|more)\\s+", "")
         .replaceAll("\\bpick\\s*axe", "pickaxe")
         .replaceAll("\\b(?:pick|pic|pix)(s?)$", "pickaxe$1")
         .replaceAll("^dias?\\b", "diamond")
         .replaceAll("\\bspades?$", "shovel")
         .replaceAll("\\s+(?:from\\s+)?(?:nearby|around\\s+here|close\\s+by|near\\s+me)(?:\\s+trees?)?$", "")
         .replaceAll("\\s+from\\s+(?:the\\s+|nearby\\s+|a\\s+)?trees?$", "")
         .replaceAll("\\s+(?:blocks?|pieces?|items?)$", "")
         .trim();

      for (String species : write_Set) {
         if (w.equals(species + " wood") || w.equals(species + " woods")) {
            return species + " log";
         }
      }

      if (w.equals("wood") || w.equals("woods")) {
         return "log";
      }

      if (w.equals("cobble")) {
         return "cobblestone";
      }

      if (!w.equals("seeds") && !w.equals("seed")) {
         switch (w) {
            case "netherite":
               return "netherite ingot";
            case "iron":
               return "iron ingot";
            case "gold":
               return "gold ingot";
            case "copper":
               return "copper ingot";
            case "diamond":
            case "diamonds":
               return "diamond";
            default:
               return w;
         }
      } else {
         return "wheat seeds";
      }
   }

   private static String close(String words) {
      String family = words.replace(' ', '_');
      if (!load_Set.contains(family) && !load_Set.contains(sync(words).replace(' ', '_'))) {
         return read(words);
      } else {
         return load_Set.contains(family) ? family : sync(words).replace(' ', '_');
      }
   }

   static String read(String words) {
      try {
         af.Inner_DataUtil target = af.load_af_DataUtil(words.replace(' ', '_'));
         if (target.items().size() == 1) {
            return BuiltInRegistries.ITEM.getKey(target.items().iterator().next()).toString();
         } else {
            return target.items().isEmpty() ? null : target.spec();
         }
      } catch (RuntimeException e) {
         String near = write(words);
         return near != null ? near : write(sync(words));
      }
   }

   static String write(String words) {
      String w = words.trim().replace(' ', '_');
      if (w.length() < 5) {
         return null;
      }

      int allowed = w.length() >= 10 ? 2 : 1;
      String best = null;
      int bestEdits = 2147483647;
      boolean tie = false;

      for (Identifier id : BuiltInRegistries.ITEM.keySet()) {
         if ("minecraft".equals(id.getNamespace())) {
            String path = id.getPath();
            if (Math.abs(path.length() - w.length()) <= allowed) {
               int edits = init(w, path, allowed);
               if (edits < bestEdits) {
                  bestEdits = edits;
                  best = id.toString();
                  tie = false;
               } else if (edits == bestEdits) {
                  tie = true;
               }
            }
         }
      }

      return bestEdits <= allowed && !tie ? best : null;
   }

   static int init(String a, String b, int cap) {
      int[] prev = new int[b.length() + 1];
      int[] cur = new int[b.length() + 1];
      int j = 0;

      while (j <= b.length()) {
         prev[j] = j++;
      }

      for (int i = 1; i <= a.length(); i++) {
         cur[0] = i;
         int rowMin = cur[0];

         for (int jx = 1; jx <= b.length(); jx++) {
            int cost = a.charAt(i - 1) == b.charAt(jx - 1) ? 0 : 1;
            cur[jx] = Math.min(Math.min(cur[jx - 1] + 1, prev[jx] + 1), prev[jx - 1] + cost);
            rowMin = Math.min(rowMin, cur[jx]);
         }

         if (rowMin > cap) {
            return cap + 1;
         }

         int[] t = prev;
         prev = cur;
         cur = t;
      }

      return Math.min(prev[b.length()], cap + 1);
   }

   private static String open(String words) {
      try {
         af.Inner_DataUtil target = af.read_af_DataUtil(words.replace(' ', '_'));
         if (target.blocks().size() == 1) {
            return BuiltInRegistries.BLOCK.getKey(target.blocks().iterator().next()).toString();
         } else {
            return target.blocks().isEmpty() ? null : target.spec();
         }
      } catch (RuntimeException e) {
         return null;
      }
   }

   static ad.Inner_BufferHelper init(String raw, String c) {
      Matcher m = state.matcher(c);
      if (!m.matches()) {
         return null;
      }

      String words = (m.group(1) + " " + m.group(2)).trim().replaceAll("\\s+", " ");
      JsonObject args = ag.init_JsonObject_ObjArr("action", "build");
      Matcher dims = queue.matcher(words);
      if (dims.find()) {
         int w = Math.max(1, Math.min(15, Integer.parseInt(dims.group(1))));
         int l = Math.max(1, Math.min(15, Integer.parseInt(dims.group(2))));
         args.addProperty("width", w);
         args.addProperty("length", l);
         args.addProperty("count", w * l);
         words = (words.substring(0, dims.start()) + " " + words.substring(dims.end())).trim();
      }

      String crop = null;
      List<String> rest = new ArrayList<>();
      String[] tokens = words.isEmpty() ? new String[0] : words.split(" ");

      for (int i = 0; i < tokens.length; i++) {
         String t = tokens[i];
         if (!t.isEmpty()) {
            String two = i + 1 < tokens.length ? t + " " + tokens[i + 1] : null;
            if (two != null && update(two) != null && crop == null) {
               crop = update(two);
               i++;
            } else if (update(t) != null && crop == null) {
               crop = update(t);
            } else {
               switch (t) {
                  case "small":
                  case "little":
                  case "tiny":
                  case "starter":
                     init(args, 9);
                     break;
                  case "medium":
                  case "decent":
                  case "normal":
                  case "average":
                  case "mid":
                     init(args, 25);
                     break;
                  case "big":
                  case "large":
                  case "huge":
                  case "massive":
                  case "giant":
                     init(args, 80);
                     break;
                  default:
                     Matcher n = buffer.matcher(t);
                     if (n.matches() && !args.has("count")) {
                        args.addProperty("count", Math.max(1, Math.min(128, Integer.parseInt(n.group(1)))));
                     } else if (!sync_Set.contains(t)) {
                        rest.add(t);
                     }
               }
            }
         }
      }

      if (!rest.isEmpty()) {
         return null;
      }

      List<ad.Inner_DataUtil> asks = new ArrayList<>();
      if (crop != null) {
         args.addProperty("crop", crop);
      } else {
         asks.add(new ad.Inner_DataUtil("crop", "Which crop should the farm grow?", init_List));
      }

      if (!args.has("count")) {
         args.addProperty("count", 80);
      }

      return new ad.Inner_BufferHelper(raw, "game:farm", args, init(args), asks);
   }

   static String init(JsonObject args) {
      String crop = ag.init_Str_JsonObject(args, "crop", "");
      String name = crop.isEmpty() ? "" : crop.substring(crop.indexOf(58) + 1) + " ";
      int count = ag.init_I_JsonObject(args, "count", 80);
      String size = args.has("width") ? ag.init_I_JsonObject(args, "width", 9) + "x" + ag.init_I_JsonObject(args, "length", 9) + ", " : "";
      return "build a " + name + "farm (" + size + count + " plots)";
   }

   private static void init(JsonObject args, int plots) {
      if (!args.has("count")) {
         args.addProperty("count", plots);
      }
   }

   static boolean init(ad.Inner_DataUtil ask, String answer, JsonObject args) {
      String a = answer == null ? "" : answer.trim().toLowerCase(Locale.ROOT);
      if (!ask.key().equals("crop")) {
         Matcher dims = queue.matcher(a);
         if (dims.find()) {
            int w = Math.max(1, Math.min(15, Integer.parseInt(dims.group(1))));
            int l = Math.max(1, Math.min(15, Integer.parseInt(dims.group(2))));
            args.addProperty("width", w);
            args.addProperty("length", l);
            args.addProperty("count", w * l);
            return true;
         }

         for (String word : a.split("[^a-z0-9]+")) {
            switch (word) {
               case "small":
               case "little":
               case "tiny":
                  init(args, 9);
                  return true;
               case "medium":
               case "decent":
               case "normal":
                  init(args, 25);
                  return true;
               case "large":
               case "big":
               case "huge":
                  init(args, 80);
                  return true;
               default:
                  if (word.matches("\\d{1,3}")) {
                     args.addProperty("count", Math.max(1, Math.min(128, Integer.parseInt(word))));
                     return true;
                  }
            }
         }

         return false;
      } else {
         String crop = update(a.replaceAll("[^a-z ]", " ").trim());
         if (crop == null) {
            for (String word : a.split("[^a-z]+")) {
               if (update(word) != null) {
                  crop = update(word);
                  break;
               }
            }
         }

         if (crop == null) {
            return false;
         }

         args.addProperty("crop", crop);
         return true;
      }
   }

   static String update(String words) {
      if (words == null) {
         return null;
      }

      String w = sync(words.trim()).replaceAll("\\s+(?:farm|field|seed|crop|plant)$", "").trim();
      String var10000;
      switch (w) {
         case "wheat":
         case "wheat seed":
            var10000 = "minecraft:wheat";
            break;
         case "carrot":
            var10000 = "minecraft:carrots";
            break;
         case "potato":
         case "potatoe":
            var10000 = "minecraft:potatoes";
            break;
         case "beetroot":
         case "beet":
            var10000 = "minecraft:beetroots";
            break;
         case "nether wart":
         case "wart":
            var10000 = "minecraft:nether_wart";
            break;
         default:
            var10000 = null;
      }

      return var10000;
   }

   static String sync(String words) {
      String w = words.trim();
      if (w.endsWith("zombies") || w.endsWith("cookies") || w.endsWith("pies")) {
         return w.substring(0, w.length() - 1);
      } else if (w.endsWith("ies")) {
         return w.substring(0, w.length() - 3) + "y";
      } else if (!w.endsWith("ches") && !w.endsWith("shes") && !w.endsWith("oes")) {
         return w.endsWith("s") && !w.endsWith("ss") ? w.substring(0, w.length() - 1) : w;
      } else {
         return w.substring(0, w.length() - 2);
      }
   }

   static Integer init(Matcher m) {
      if (m.group(1) != null) {
         return Integer.parseInt(m.group(1));
      } else if (m.group(2) != null) {
         return Integer.parseInt(m.group(2));
      } else if (m.group(4) != null) {
         return Integer.parseInt(m.group(4)) * 64;
      } else if (m.group(3) != null) {
         String w = m.group(3).trim().replaceAll("\\s+", " ");
         return init_Set.contains(w) ? null : w.startsWith("a couple") ? 2 : init_Map.get(w);
      } else {
         return null;
      }
   }

   private static int init(Matcher m, String spec, String words) {
      Integer count = init(m);
      if (count != null) {
         return Math.max(1, Math.min(2304, count));
      } else {
         String family = spec.replace("minecraft:", "");
         boolean plural = !sync(words).equals(words.trim()) || m.group(3) != null;
         if (family.contains("seed")) {
            return 8;
         } else {
            return !load_Set.contains(family) && !plural ? 1 : 16;
         }
      }
   }

   record Inner_BufferHelper(String clause, String capability, JsonObject arguments, String describe, List<ad.Inner_DataUtil> asks) {
      Inner_BufferHelper(String clause, String capability, JsonObject arguments, String describe) {
         this(clause, capability, arguments, describe, List.of());
      }
   }

   record Inner_CachePool(List<ad.Inner_BufferHelper> steps, String remainder) {
      boolean handledAll() {
         return this.remainder.isBlank();
      }
   }

   record Inner_DataUtil(String key, String question, List<String> options) {
   }

   record Inner_StreamUtil(Function<String, int[]> waypoint, Function<String, String> moduleId, Function<String, String> entityId) {
   }
}
