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

final class AiIntents {
   private static final Pattern CLAUSE_SPLIT = Pattern.compile(
      "\\s*(?:,\\s*(?:and\\s+)?(?:then\\s+)?|;\\s*|\\.\\s+|\\s+and\\s+then\\s+|\\s+then\\s+|\\s+and\\s+|\\s+after\\s+that\\s*,?\\s*|\\s+afterwards\\s*,?\\s*)",
      2
   );
   private static final Pattern FILLER_PREFIX = Pattern.compile(
      "^(?:(?:please|pls|now|first|then|also|and|next|finally|go\\s+and|can\\s+you|could\\s+you|would\\s+you|will\\s+you|i\\s+want\\s+you\\s+to|i\\s+need\\s+you\\s+to|i\\s+want\\s+to|i\\s+need\\s+to|i\\s+wanna|i\\s+need|i\\s+want|lets|let's)\\s+)+"
   );
   private static final Pattern WANT_PREFIX = Pattern.compile(
      "^(?:please\\s+|pls\\s+)?(?:i\\s+(?:really\\s+)?(?:want|need|would\\s+like|'d\\s+like|wanna\\s+(?:have|get)|want\\s+to\\s+(?:get|have)|need\\s+to\\s+(?:get|have))|i'd\\s+like|give\\s+me|gimme|hand\\s+me|(?:can|could|may)\\s+(?:i|u|you)\\s+(?:get|have|give\\s+me|grab|fetch))\\s+(?!you\\b|to\\b)"
   );
   private static final Pattern POLITE_QUESTION = Pattern.compile("^(?:please\\s+|pls\\s+)?(?:can|could|would|will|may)\\s+(?:you|u|i)\\s+");
   private static final Pattern FILLER_SUFFIX = Pattern.compile(
      "(?:\\s+(?:please|pls|now|for\\s+me|right\\s+now|asap|thanks?|thank\\s+you|from\\s+scratch|from\\s+nothing|from\\s+zero|quickly|quick|fast|real\\s+quick|as\\s+fast\\s+as\\s+(?:you\\s+can|possible)|if\\s+you\\s+can|by\\s+yourself|yourself|on\\s+your\\s+own|bro|mate|dude))+$"
   );
   static final String init_Str = "(?:(\\d{1,4})\\s*x?\\s+|x(\\d{1,4})\\s+|(a\\s+stack\\s+of|one\\s+stack\\s+of|a\\s+couple(?:\\s+of)?|a\\s+few|an?|one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twenty|thirty|thirty[- ]two|forty|fifty|sixty|sixty[- ]four|some|more|a\\s+bit\\s+of|a\\s+lot\\s+of|lots\\s+of)\\s+|(\\d{1,2})\\s+stacks?\\s+of\\s+)?";
   private static final Map<String, Integer> NUMBER_WORDS = Map.ofEntries(
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
   private static final Set<String> VAGUE_QUANTITIES = Set.of("some", "more", "a bit of", "a lot of", "lots of");
   private static final Set<String> ITEM_FAMILIES = Set.of(
      "log", "planks", "cobblestone", "stone", "dirt", "sand", "gravel", "stick", "wheat_seeds", "torch", "coal", "wool", "glass"
   );
   private static final int write_I = 16;
   private static final Pattern GET_PATTERN = quantityPattern(
      "get\\s+me|go\\s+get|go\\s+find|go\\s+grab|go\\s+fetch|find\\s+me|bring\\s+me|get|obtain|collect|gather|grab|fetch|acquire|find|look\\s+for|search\\s+for"
   );
   private static final Pattern MINE_PATTERN = quantityPattern("mine|dig\\s+up|dig\\s+for");
   private static final Pattern BARE_QUANTITY_PATTERN = Pattern.compile(
      "^(?:(\\d{1,4})\\s*x?\\s+|x(\\d{1,4})\\s+|(a\\s+stack\\s+of|one\\s+stack\\s+of|a\\s+couple(?:\\s+of)?|a\\s+few|an?|one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twenty|thirty|thirty[- ]two|forty|fifty|sixty|sixty[- ]four|some|more|a\\s+bit\\s+of|a\\s+lot\\s+of|lots\\s+of)\\s+|(\\d{1,2})\\s+stacks?\\s+of\\s+)?([a-z][a-z _'-]*?)$"
   );
   private static final Pattern CRAFT_PATTERN = quantityPattern("craft|make|build\\s+me");
   private static final Pattern SMELT_PATTERN = quantityPattern("smelt|cook|bake|roast");
   private static final Pattern HARVEST_PATTERN = quantityPattern("harvest|farm");
   private static final Pattern CHOP_PATTERN = Pattern.compile(
      "^(?:chop|cut)(?:\\s+down)?\\s+(?:(\\d{1,4})\\s*x?\\s+|x(\\d{1,4})\\s+|(a\\s+stack\\s+of|one\\s+stack\\s+of|a\\s+couple(?:\\s+of)?|a\\s+few|an?|one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twenty|thirty|thirty[- ]two|forty|fifty|sixty|sixty[- ]four|some|more|a\\s+bit\\s+of|a\\s+lot\\s+of|lots\\s+of)\\s+|(\\d{1,2})\\s+stacks?\\s+of\\s+)?(?:(?:the\\s+)?(?:nearest\\s+|closest\\s+)?(?:([a-z ]+?)\\s+)?(?:trees?|logs?|wood))$"
   );
   private static final Pattern PLANT_PATTERN = Pattern.compile(
      "^(?:(?:plant|sow)\\s+(?:a\\s+|some\\s+)?(?:small\\s+|little\\s+|big\\s+)?([a-z ]+?)(?:\\s+(?:farm|field|seeds))?|(?:make|build|create|start|set\\s+up)\\s+(?:a\\s+|an\\s+)?(?:small\\s+|little\\s+|big\\s+)?([a-z ]+?)\\s+(?:farm|field))$"
   );
   private static final Pattern KILL_PATTERN = Pattern.compile(
      "^(?:kill|fight|attack|slay|hunt)\\s+(?:the\\s+)?(?:nearest\\s+|closest\\s+|nearby\\s+|all\\s+(?:the\\s+)?)?(?:(\\d{1,4})\\s*x?\\s+|x(\\d{1,4})\\s+|(a\\s+stack\\s+of|one\\s+stack\\s+of|a\\s+couple(?:\\s+of)?|a\\s+few|an?|one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twenty|thirty|thirty[- ]two|forty|fifty|sixty|sixty[- ]four|some|more|a\\s+bit\\s+of|a\\s+lot\\s+of|lots\\s+of)\\s+|(\\d{1,2})\\s+stacks?\\s+of\\s+)?([a-z ]+?)(?:\\s+(?:nearby|around\\s+here|near\\s+me))?$"
   );
   private static final Pattern TOGGLE_MODULE_PATTERN = Pattern.compile(
      "^(?:turn|switch|toggle)\\s+(on|off)\\s+(?:the\\s+)?(.+?)(?:\\s+module)?$|^(?:turn|switch)\\s+(?:the\\s+)?(.+?)(?:\\s+module)?\\s+(on|off)$|^(enable|disable|activate|deactivate)\\s+(?:the\\s+)?(.+?)(?:\\s+module)?$"
   );
   private static final Pattern DROP_ALL_PATTERN = Pattern.compile(
      "^(?:drop|throw\\s+out|throw\\s+away|empty)\\s+(?:out\\s+)?(?:all\\s+(?:of\\s+)?)?(?:my\\s+|the\\s+)?(?:whole\\s+|entire\\s+|full\\s+)?(?:inventory|everything|all\\s+(?:my\\s+)?(?:items|stuff)|all|stuff)$"
   );
   private static final Pattern DROP_HELD_PATTERN = Pattern.compile(
      "^drop\\s+(?:the\\s+|my\\s+)?(?:held\\s+item|item\\s+in\\s+my\\s+hand|what\\s+i(?:'m|\\s+am)\\s+holding|hand)$"
   );
   private static final Pattern DROP_COUNT_PATTERN = Pattern.compile(
      "^(?:drop|throw\\s+away|throw\\s+out)\\s+(?:(\\d{1,4})\\s*x?\\s+|x(\\d{1,4})\\s+|(a\\s+stack\\s+of|one\\s+stack\\s+of|a\\s+couple(?:\\s+of)?|a\\s+few|an?|one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twenty|thirty|thirty[- ]two|forty|fifty|sixty|sixty[- ]four|some|more|a\\s+bit\\s+of|a\\s+lot\\s+of|lots\\s+of)\\s+|(\\d{1,2})\\s+stacks?\\s+of\\s+)?(?:my\\s+|the\\s+|all\\s+(?:my\\s+|the\\s+)?)?([a-z][a-z _-]*?)$"
   );
   private static final Pattern STOP_PATTERN = Pattern.compile("^(?:stop|halt|stay(?:\\s+there)?|wait(?:\\s+there)?|stand\\s+still|that'?s\\s+it|done)$");
   private static final Pattern SPEEDRUN_PATTERN = Pattern.compile(
      "^(?:beat|finish|complete|win)\\s+(?:the\\s+game|minecraft)|speed\\s*run(?:\\s+(?:the\\s+game|minecraft))?|(?:kill|beat|defeat)\\s+the\\s+(?:ender\\s+)?dragon$"
   );
   private static final Pattern EAT_PATTERN = Pattern.compile(
      "^(?:eat(?:\\s+(?:something|some\\s+food|food|a\\s+snack|it|them|that|those|up|the\\s+(?:meat|food)|my\\s+(?:meat|food)))?|have\\s+a\\s+snack|heal\\s+up|fill\\s+(?:up\\s+)?(?:my\\s+)?(?:hunger|food)(?:\\s+bar)?)$"
   );
   private static final Pattern SLEEP_PATTERN = Pattern.compile("^(?:sleep|go\\s+to\\s+(?:sleep|bed)|sleep\\s+in\\s+(?:a|the)\\s+bed|skip\\s+the\\s+night)$");
   private static final Pattern EQUIP_PATTERN = Pattern.compile(
      "^(?:equip|wear|put\\s+on|hold|take\\s+out)\\s+(?:my\\s+|the\\s+|a\\s+)?(?:best\\s+)?(armor|armour|sword|weapon|shield)$"
   );
   private static final Pattern TIDY_PATTERN = Pattern.compile(
      "^(?:clean(?:\\s+up)?|tidy(?:\\s+up)?|sort\\s+out|clear\\s+out|empty)\\s+(?:my\\s+|the\\s+)?(?:inventory|inv|bag|backpack)|(?:throw|toss|get\\s+rid\\s+of|dump|drop)\\s+(?:out\\s+|away\\s+)?(?:all\\s+)?(?:the\\s+|my\\s+)?(?:junk|trash|garbage|useless\\s+(?:stuff|items|things|shit)|crap|shit)(?:\\s+(?:out|away))?|make\\s+(?:some\\s+)?(?:room|space)(?:\\s+in\\s+(?:my\\s+|the\\s+)?(?:inventory|inv|bag))?$"
   );
   private static final Pattern PLACE_PATTERN = Pattern.compile(
      "^(?:put|place|set)\\s+(?:(it|that|them)|(?:the|a|an|my)\\s+([a-z][a-z ]*?))(?:\\s+down)?(?:\\s+(?:next\\s+to\\s+me|near\\s+me|beside\\s+me|by\\s+me|here|right\\s+here))?(?:\\s+down)?$"
   );
   private static final Pattern FILL_BUCKET_PATTERN = Pattern.compile(
      "^(?:fill|top\\s+up)\\s+(?:it|them|that|the\\s+bucket|a\\s+bucket|my\\s+bucket|(?:the\\s+|my\\s+)?buckets)(?:\\s+up)?(?:\\s+with\\s+water)?$|^(?:get|grab|fetch)\\s+(?:some\\s+)?water$"
   );
   private static final Pattern FISH_PATTERN = Pattern.compile("^(?:go\\s+)?fish(?:ing)?$");
   private static final Pattern SURFACE_PATTERN = Pattern.compile(
      "^(?:surface|go\\s+(?:up\\s+)?to\\s+the\\s+surface|get\\s+(?:out\\s+of\\s+(?:this|the)\\s+cave|to\\s+the\\s+surface|back\\s+to\\s+the\\s+surface))$"
   );
   private static final Pattern RECOVER_ITEMS_PATTERN = Pattern.compile(
      "^(?:recover|get\\s+back|pick\\s+up|collect|go\\s+get)\\s+(?:all\\s+)?(?:my\\s+)?(?:stuff|items|things|loot|drops)(?:\\s+(?:back|from\\s+where\\s+i\\s+died|from\\s+my\\s+death))?$"
   );
   private static final Map<String, String> SMELT_OUTPUTS = Map.ofEntries(
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
   private static final Set<String> GENERIC_FOOD_WORDS = Set.of("meat", "the meat", "my meat", "food", "the food", "my food", "it", "them", "that", "those");
   private static final Map<String, String> MOB_MEAT = Map.of(
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
   private static final Set<String> WOOD_SPECIES = Set.of("oak", "spruce", "birch", "jungle", "acacia", "dark oak", "mangrove", "cherry", "pale oak");
   private static final Set<String> HOSTILE_WORDS = Set.of("mobs", "mob", "monsters", "monster", "hostiles", "hostile mobs", "hostile", "enemies", "enemy");
   private static final Pattern FARM_BUILD_PATTERN = Pattern.compile(
      "^(?:make|build|create|start|set\\s+up|plant|sow|lay\\s+out)\\s+(?:me\\s+|us\\s+)?(.*?)\\b(?:farm|field|garden|crop\\s+field)s?\\b(.*)$"
   );
   private static final Pattern DIMENSIONS_PATTERN = Pattern.compile("(\\d{1,2})\\s*(?:x|by|\\*)\\s*(\\d{1,2})");
   private static final Pattern PLOT_COUNT_PATTERN = Pattern.compile("(\\d{1,3})\\s*(?:crops?|plots?|plants?|blocks?|seeds?|tiles?)?");
   private static final Set<String> FARM_FILLER_WORDS = Set.of(
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
   static final List<String> CROPS = List.of("Wheat", "Carrots", "Potatoes", "Beetroots", "Nether wart");
   static final List<String> UNUSED_FARM_SIZE_LABELS = List.of("Small (about 10 crops)", "Medium (about 25 crops)", "Large (9x9, 80 crops around one water block)");

   private AiIntents() {
   }

   private static Pattern quantityPattern(String verbs) {
      return Pattern.compile(
         "^(?:"
            + verbs
            + ")\\s+(?:me\\s+|us\\s+)?(?:(\\d{1,4})\\s*x?\\s+|x(\\d{1,4})\\s+|(a\\s+stack\\s+of|one\\s+stack\\s+of|a\\s+couple(?:\\s+of)?|a\\s+few|an?|one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twenty|thirty|thirty[- ]two|forty|fifty|sixty|sixty[- ]four|some|more|a\\s+bit\\s+of|a\\s+lot\\s+of|lots\\s+of)\\s+|(\\d{1,2})\\s+stacks?\\s+of\\s+)?([a-z][a-z _'-]*?)$"
      );
   }

   static AiIntents.IntentPlan plan(String objective, AiIntents.NameLookups lookups) {
      AiIntents.IntentStep schematic = schematicIntent(objective);
      if (schematic != null) {
         return new AiIntents.IntentPlan(List.of(schematic), "");
      }

      if (objective != null && !objective.isBlank() && objective.length() <= 400) {
         String text = objective.trim();
         if (text.endsWith("?")) {
            String asked = text.replaceAll("\\?+$", "").trim();
            if (!POLITE_QUESTION.matcher(asked.toLowerCase(Locale.ROOT)).find()) {
               return new AiIntents.IntentPlan(List.of(), text);
            }

            text = asked;
         }

         List<int[]> spans = new ArrayList<>();
         Matcher m = CLAUSE_SPLIT.matcher(text);

         int at;
         for (at = 0; m.find(); at = m.end()) {
            if (m.start() > at) {
               spans.add(new int[]{at, m.start()});
            }
         }

         if (at < text.length()) {
            spans.add(new int[]{at, text.length()});
         }

         List<AiIntents.IntentStep> steps = new ArrayList<>();
         String lastVerb = null;

         for (int[] span : spans) {
            String clause = text.substring(span[0], span[1]);
            if (!STOP_PATTERN.matcher(normalizeClause(clause)).matches() || steps.isEmpty()) {
               AiIntents.IntentStep step = lastVerb != null && leadingVerb(normalizeClause(clause)) == null
                  ? parseClause(lastVerb + " " + normalizeClause(clause), lookups)
                  : null;
               if (step == null) {
                  step = parseClause(clause, lookups);
               }

               if (step == null) {
                  return new AiIntents.IntentPlan(steps, text.substring(span[0]).trim());
               }

               steps.add(step);
               lastVerb = leadingVerb(normalizeClause(clause));
            }
         }

         return new AiIntents.IntentPlan(fillPlaceNearItem(inheritSmeltCount(linkFightToMeat(steps))), "");
      } else {
         return new AiIntents.IntentPlan(List.of(), objective == null ? "" : objective.trim());
      }
   }

   static List<AiIntents.IntentStep> fillPlaceNearItem(List<AiIntents.IntentStep> steps) {
      List<AiIntents.IntentStep> out = new ArrayList<>(steps);

      for (int i = 1; i < out.size(); i++) {
         AiIntents.IntentStep step = out.get(i);
         if (step.capability().equals("game:place_near") && AiJson.getString(step.arguments(), "item", "").isEmpty()) {
            String item = AiJson.getString(out.get(i - 1).arguments(), "item", "");
            if (!item.isEmpty() && !item.contains(",")) {
               out.set(
                  i,
                  new AiIntents.IntentStep(
                     step.clause(),
                     step.capability(),
                     AiJson.objectOf("item", item),
                     "put " + item.replace("minecraft:", "") + " down beside the player",
                     step.asks()
                  )
               );
            }
         }
      }

      return out;
   }

   static List<AiIntents.IntentStep> inheritSmeltCount(List<AiIntents.IntentStep> steps) {
      List<AiIntents.IntentStep> out = new ArrayList<>(steps);

      for (int i = 1; i < out.size(); i++) {
         AiIntents.IntentStep step = out.get(i);
         AiIntents.IntentStep before = out.get(i - 1);
         if (!hasExplicitQuantity(step.clause()) && step.arguments().has("count")) {
            String output = AiJson.getString(step.arguments(), "item", "");
            String input = AiJson.getString(before.arguments(), "item", AiJson.getString(before.arguments(), "block", ""))
               .replace("minecraft:", "")
               .replace('_', ' ');
            if (!output.isEmpty() && !input.isEmpty() && output.equals(SMELT_OUTPUTS.get(input)) && before.arguments().has("count")) {
               int count = Math.max(1, AiJson.getInt(before.arguments(), "count", 1));
               JsonObject args = step.arguments().deepCopy();
               args.addProperty("count", count);
               out.set(
                  i,
                  new AiIntents.IntentStep(step.clause(), step.capability(), args, step.describe().replaceFirst("\\d+", String.valueOf(count)), step.asks())
               );
            }
         }
      }

      return out;
   }

   static boolean hasExplicitQuantity(String clause) {
      String c = clause.toLowerCase(Locale.ROOT);
      if (c.matches(".*\\d.*")) {
         return true;
      }

      for (String word : c.split("[^a-z]+")) {
         if (NUMBER_WORDS.containsKey(word)) {
            return true;
         }
      }

      return false;
   }

   static List<AiIntents.IntentStep> linkFightToMeat(List<AiIntents.IntentStep> steps) {
      List<AiIntents.IntentStep> out = new ArrayList<>(steps);

      for (int i = 0; i < out.size(); i++) {
         AiIntents.IntentStep step = out.get(i);
         if (step.capability().equals("game:fight")) {
            boolean usedLater = false;

            for (AiIntents.IntentStep later : out.subList(i + 1, out.size())) {
               usedLater |= later.capability().equals("game:eat") || AiJson.getBoolean(later.arguments(), "cookHeld", false);
            }

            if (usedLater) {
               List<String> meats = new ArrayList<>();

               for (String target : AiJson.getString(step.arguments(), "target", "").split(",")) {
                  String meat = MOB_MEAT.get(target);
                  if (meat == null) {
                     meats = null;
                     break;
                  }

                  if (!meats.contains(meat)) {
                     meats.add(meat);
                  }
               }

               if (meats != null && !meats.isEmpty()) {
                  int kills = Math.max(1, AiJson.getInt(step.arguments(), "count", 1));
                  out.set(
                     i,
                     new AiIntents.IntentStep(
                        step.clause(),
                        "game:obtain",
                        AiJson.objectOf("item", String.join(",", meats), "count", kills, "additional", true),
                        "hunt for " + String.join(" or ", meats).replace("minecraft:", "") + (kills > 1 ? " x" + kills : "")
                     )
                  );
               }
            }
         }
      }

      return out;
   }

   static AiIntents.IntentStep schematicIntent(String objective) {
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
         String name = AiSchematic.validateName(m.group(1) != null ? m.group(1) : m.group(2));
         JsonObject args = AiJson.objectOf("schematic", name, "craftMissing", false, "timeoutSeconds", 2592000);
         if (m.group(3) != null) {
            args.add(
               "origin", AiJson.objectOf("x", Integer.parseInt(m.group(3)), "y", Integer.parseInt(m.group(4)), "z", Integer.parseInt(m.group(5)))
            );
         }

         return new AiIntents.IntentStep(objective, "game:build", args, "build " + name);
      } catch (IllegalArgumentException e) {
         return null;
      }
   }

   static String normalizeClause(String clause) {
      String c = clause.trim().toLowerCase(Locale.ROOT).replaceAll("[.!]+$", "").replaceAll("\\s+", " ");
      c = WANT_PREFIX.matcher(c).replaceFirst("get ");
      c = FILLER_PREFIX.matcher(c).replaceFirst("");
      c = FILLER_SUFFIX.matcher(c).replaceFirst("");
      return c.trim();
   }

   private static String leadingVerb(String clause) {
      int space = clause.indexOf(32);
      String first = space < 0 ? clause : clause.substring(0, space);
      return Set.of("get", "collect", "gather", "craft", "make", "mine", "smelt", "cook", "obtain", "grab").contains(first) ? first : null;
   }

   static AiIntents.IntentStep parseClause(String raw, AiIntents.NameLookups lookups) {
      String c = normalizeClause(raw);
      if (c.isEmpty()) {
         return null;
      }

      AiFastPath.Inner_DataUtil move = AiFastPath.init(c, lookups.waypoint());
      if (move != null) {
         return new AiIntents.IntentStep(raw, "game:goto", move.args(), move.describe());
      }

      Matcher m;
      if ((m = TOGGLE_MODULE_PATTERN.matcher(c)).matches()) {
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
            : new AiIntents.IntentStep(raw, "module:" + id, AiJson.objectOf("enabled", on, "persistent", true), (on ? "turn on " : "turn off ") + id);
      } else {
         if (DROP_ALL_PATTERN.matcher(c).matches()) {
            return new AiIntents.IntentStep(raw, "command:drop", AiJson.objectOf("body", "drop fullinventory"), "drop the whole inventory");
         }

         if (DROP_HELD_PATTERN.matcher(c).matches()) {
            return new AiIntents.IntentStep(raw, "command:drop", AiJson.objectOf("body", "drop hand"), "drop the held item");
         }

         if (SPEEDRUN_PATTERN.matcher(c).find()) {
            return new AiIntents.IntentStep(raw, "game:speedrun", new JsonObject(), "speedrun: beat the game");
         }

         if (EAT_PATTERN.matcher(c).matches()) {
            return new AiIntents.IntentStep(raw, "game:eat", new JsonObject(), "eat");
         }

         if (SLEEP_PATTERN.matcher(c).matches()) {
            return new AiIntents.IntentStep(raw, "game:sleep", new JsonObject(), "sleep in a bed");
         }

         if (FISH_PATTERN.matcher(c).matches()) {
            return new AiIntents.IntentStep(raw, "game:fish", new JsonObject(), "fish");
         }

         if ((m = PLACE_PATTERN.matcher(c)).matches()) {
            if (m.group(1) != null) {
               return new AiIntents.IntentStep(raw, "game:place_near", AiJson.objectOf("item", ""), "put it down beside the player");
            }

            String id = resolveItemId(cleanItemWords(m.group(2)));
            return id == null
               ? null
               : new AiIntents.IntentStep(
                  raw, "game:place_near", AiJson.objectOf("item", id), "put " + id.replace("minecraft:", "") + " down beside the player"
               );
         } else {
            if (FILL_BUCKET_PATTERN.matcher(c).matches()) {
               return new AiIntents.IntentStep(
                  raw, "game:obtain", AiJson.objectOf("item", "minecraft:water_bucket", "count", 1), "fill a bucket with water"
               );
            }

            if (SURFACE_PATTERN.matcher(c).matches()) {
               return new AiIntents.IntentStep(raw, "game:surface", new JsonObject(), "go to the surface");
            }

            if (RECOVER_ITEMS_PATTERN.matcher(c).matches()) {
               return new AiIntents.IntentStep(raw, "game:recover", new JsonObject(), "recover the items from the last death");
            }

            if (TIDY_PATTERN.matcher(c).matches()) {
               return new AiIntents.IntentStep(
                  raw,
                  "game:tidy",
                  AiJson.objectOf("action", "drop", "freeSlots", 9),
                  "throw out junk to free 9 slots (tools, food and valuables stay)"
               );
            }

            if ((m = EQUIP_PATTERN.matcher(c)).matches()) {
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
               return new AiIntents.IntentStep(raw, "game:equip", AiJson.objectOf("what", what), "equip the best " + what);
            } else {
               AiIntents.IntentStep field = farmBuildIntent(raw, c);
               if (field != null) {
                  return field;
               }

               if ((m = PLANT_PATTERN.matcher(c)).matches()) {
                  String crop = cropId(m.group(1) != null ? m.group(1) : m.group(2));
                  return crop == null
                     ? null
                     : new AiIntents.IntentStep(raw, "game:farm", AiJson.objectOf("action", "plant", "crop", crop), "plant a " + crop + " field");
               }

               if ((m = CHOP_PATTERN.matcher(c)).matches()) {
                  Integer count = matchedCount(m);
                  String species = m.group(5) == null ? null : m.group(5).trim();
                  if (species != null && !WOOD_SPECIES.contains(species)) {
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
                     : new AiIntents.IntentStep(
                        raw, "game:chop", args, "chop " + (species == null ? "the nearest tree" : species + " logs") + (args.has("count") ? " x" + count : "")
                     );
               } else if (!(m = KILL_PATTERN.matcher(c)).matches()) {
                  if ((m = HARVEST_PATTERN.matcher(c)).matches()) {
                     String crop = cropId(m.group(5));
                     if (crop == null) {
                        return null;
                     }

                     Integer count = matchedCount(m);
                     return new AiIntents.IntentStep(
                        raw, "game:farm", AiJson.objectOf("action", "harvest", "crop", crop, "count", count == null ? 16 : count), "harvest " + crop
                     );
                  } else if ((m = SMELT_PATTERN.matcher(c)).matches()) {
                     if (GENERIC_FOOD_WORDS.contains(m.group(5).trim())) {
                        return new AiIntents.IntentStep(
                           raw, "game:smelt_auto", AiJson.objectOf("item", "cooked_food", "count", 1, "cookHeld", true), "cook the raw food held"
                        );
                     }

                     String what = cleanItemWords(m.group(5));
                     String output = SMELT_OUTPUTS.get(what);
                     if (output == null) {
                        output = SMELT_OUTPUTS.get(singular(what));
                     }

                     if (output == null) {
                        output = resolveItemId(what);
                     }

                     if (output == null) {
                        return null;
                     }

                     int count = resolveCount(m, output, what);
                     return new AiIntents.IntentStep(
                        raw, "game:smelt_auto", AiJson.objectOf("item", output, "count", count), "smelt " + count + " " + output
                     );
                  } else if ((m = DROP_COUNT_PATTERN.matcher(c)).matches()) {
                     String what = cleanItemWords(m.group(5));
                     String id = resolveItemId(what);
                     if (id == null) {
                        return null;
                     }

                     Integer count = matchedCount(m);
                     String body = "drop " + (count == null ? "" : count + " ") + id.replace("minecraft:", "");
                     return new AiIntents.IntentStep(raw, "command:drop", AiJson.objectOf("body", body), body);
                  } else if ((m = MINE_PATTERN.matcher(c)).matches()) {
                     String what = cleanItemWords(m.group(5));
                     String block = resolveBlockId(what);
                     if (block == null) {
                        String id = resolveItemId(what);
                        if (id == null) {
                           return null;
                        }

                        int count = resolveCount(m, id, what);
                        return new AiIntents.IntentStep(
                           raw, "game:obtain", AiJson.objectOf("item", id, "count", count, "additional", true), "get " + count + " " + id
                        );
                     } else {
                        int count = resolveCount(m, block, what);
                        return new AiIntents.IntentStep(
                           raw, "game:mine", AiJson.objectOf("block", block, "count", count), "mine " + count + " " + block
                        );
                     }
                  } else if ((m = CRAFT_PATTERN.matcher(c)).matches()) {
                     String what = cleanItemWords(m.group(5));
                     String id = resolveItemId(what);
                     if (id == null) {
                        return null;
                     }

                     int count = resolveCount(m, id, what);
                     return new AiIntents.IntentStep(raw, "game:craft", AiJson.objectOf("item", id, "count", count), "craft " + count + " " + id);
                  } else if ((m = GET_PATTERN.matcher(c)).matches()) {
                     String what = cleanItemWords(m.group(5));
                     String spec = itemOrFamilySpec(what);
                     if (spec == null) {
                        return null;
                     }

                     int count = resolveCount(m, spec, what);
                     return new AiIntents.IntentStep(
                        raw, "game:obtain", AiJson.objectOf("item", spec, "count", count, "additional", true), "get " + count + " " + spec
                     );
                  } else if ((m = BARE_QUANTITY_PATTERN.matcher(c)).matches()) {
                     String what = cleanItemWords(m.group(5));
                     String spec = itemOrFamilySpec(what);
                     if (spec == null) {
                        return null;
                     }

                     int count = resolveCount(m, spec, what);
                     return new AiIntents.IntentStep(
                        raw, "game:obtain", AiJson.objectOf("item", spec, "count", count, "additional", true), "get " + count + " " + spec
                     );
                  } else {
                     return null;
                  }
               } else {
                  String who = m.group(5).trim();
                  List<String> targets = new ArrayList<>();

                  for (String one : who.split("\\s+or\\s+(?:an?\\s+|the\\s+)?")) {
                     String id = HOSTILE_WORDS.contains(one.trim()) ? "hostiles" : lookups.entityId().apply(singular(one.trim()));
                     if (id == null) {
                        return null;
                     }

                     if (!targets.contains(id)) {
                        targets.add(id);
                     }
                  }

                  String target = String.join(",", targets);
                  Integer count = matchedCount(m);
                  JsonObject args = AiJson.objectOf("target", target);
                  if (count != null && count > 1) {
                     args.addProperty("count", count);
                  }

                  return new AiIntents.IntentStep(raw, "game:fight", args, "fight " + target + (count != null && count > 1 ? " x" + count : ""));
               }
            }
         }
      }
   }

   static String cleanItemWords(String words) {
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

      for (String species : WOOD_SPECIES) {
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

   private static String itemOrFamilySpec(String words) {
      String family = words.replace(' ', '_');
      if (!ITEM_FAMILIES.contains(family) && !ITEM_FAMILIES.contains(singular(words).replace(' ', '_'))) {
         return resolveItemId(words);
      } else {
         return ITEM_FAMILIES.contains(family) ? family : singular(words).replace(' ', '_');
      }
   }

   static String resolveItemId(String words) {
      try {
         AiItems.ItemSpec target = AiItems.resolveItem(words.replace(' ', '_'));
         if (target.items().size() == 1) {
            return BuiltInRegistries.ITEM.getKey(target.items().iterator().next()).toString();
         } else {
            return target.items().isEmpty() ? null : target.spec();
         }
      } catch (RuntimeException e) {
         String near = fuzzyItemId(words);
         return near != null ? near : fuzzyItemId(singular(words));
      }
   }

   static String fuzzyItemId(String words) {
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
               int edits = boundedEditDistance(w, path, allowed);
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

   static int boundedEditDistance(String a, String b, int cap) {
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

   private static String resolveBlockId(String words) {
      try {
         AiItems.ItemSpec target = AiItems.resolveBlock(words.replace(' ', '_'));
         if (target.blocks().size() == 1) {
            return BuiltInRegistries.BLOCK.getKey(target.blocks().iterator().next()).toString();
         } else {
            return target.blocks().isEmpty() ? null : target.spec();
         }
      } catch (RuntimeException e) {
         return null;
      }
   }

   static AiIntents.IntentStep farmBuildIntent(String raw, String c) {
      Matcher m = FARM_BUILD_PATTERN.matcher(c);
      if (!m.matches()) {
         return null;
      }

      String words = (m.group(1) + " " + m.group(2)).trim().replaceAll("\\s+", " ");
      JsonObject args = AiJson.objectOf("action", "build");
      Matcher dims = DIMENSIONS_PATTERN.matcher(words);
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
            if (two != null && cropId(two) != null && crop == null) {
               crop = cropId(two);
               i++;
            } else if (cropId(t) != null && crop == null) {
               crop = cropId(t);
            } else {
               switch (t) {
                  case "small":
                  case "little":
                  case "tiny":
                  case "starter":
                     defaultPlotCount(args, 9);
                     break;
                  case "medium":
                  case "decent":
                  case "normal":
                  case "average":
                  case "mid":
                     defaultPlotCount(args, 25);
                     break;
                  case "big":
                  case "large":
                  case "huge":
                  case "massive":
                  case "giant":
                     defaultPlotCount(args, 80);
                     break;
                  default:
                     Matcher n = PLOT_COUNT_PATTERN.matcher(t);
                     if (n.matches() && !args.has("count")) {
                        args.addProperty("count", Math.max(1, Math.min(128, Integer.parseInt(n.group(1)))));
                     } else if (!FARM_FILLER_WORDS.contains(t)) {
                        rest.add(t);
                     }
               }
            }
         }
      }

      if (!rest.isEmpty()) {
         return null;
      }

      List<AiIntents.ClarifyingQuestion> asks = new ArrayList<>();
      if (crop != null) {
         args.addProperty("crop", crop);
      } else {
         asks.add(new AiIntents.ClarifyingQuestion("crop", "Which crop should the farm grow?", CROPS));
      }

      if (!args.has("count")) {
         args.addProperty("count", 80);
      }

      return new AiIntents.IntentStep(raw, "game:farm", args, init(args), asks);
   }

   static String init(JsonObject args) {
      String crop = AiJson.getString(args, "crop", "");
      String name = crop.isEmpty() ? "" : crop.substring(crop.indexOf(58) + 1) + " ";
      int count = AiJson.getInt(args, "count", 80);
      String size = args.has("width") ? AiJson.getInt(args, "width", 9) + "x" + AiJson.getInt(args, "length", 9) + ", " : "";
      return "build a " + name + "farm (" + size + count + " plots)";
   }

   private static void defaultPlotCount(JsonObject args, int plots) {
      if (!args.has("count")) {
         args.addProperty("count", plots);
      }
   }

   static boolean init(AiIntents.ClarifyingQuestion ask, String answer, JsonObject args) {
      String a = answer == null ? "" : answer.trim().toLowerCase(Locale.ROOT);
      if (!ask.key().equals("crop")) {
         Matcher dims = DIMENSIONS_PATTERN.matcher(a);
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
                  defaultPlotCount(args, 9);
                  return true;
               case "medium":
               case "decent":
               case "normal":
                  defaultPlotCount(args, 25);
                  return true;
               case "large":
               case "big":
               case "huge":
                  defaultPlotCount(args, 80);
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
         String crop = cropId(a.replaceAll("[^a-z ]", " ").trim());
         if (crop == null) {
            for (String word : a.split("[^a-z]+")) {
               if (cropId(word) != null) {
                  crop = cropId(word);
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

   static String cropId(String words) {
      if (words == null) {
         return null;
      }

      String w = singular(words.trim()).replaceAll("\\s+(?:farm|field|seed|crop|plant)$", "").trim();
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

   static String singular(String words) {
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

   static Integer matchedCount(Matcher m) {
      if (m.group(1) != null) {
         return Integer.parseInt(m.group(1));
      } else if (m.group(2) != null) {
         return Integer.parseInt(m.group(2));
      } else if (m.group(4) != null) {
         return Integer.parseInt(m.group(4)) * 64;
      } else if (m.group(3) != null) {
         String w = m.group(3).trim().replaceAll("\\s+", " ");
         return VAGUE_QUANTITIES.contains(w) ? null : w.startsWith("a couple") ? 2 : NUMBER_WORDS.get(w);
      } else {
         return null;
      }
   }

   private static int resolveCount(Matcher m, String spec, String words) {
      Integer count = matchedCount(m);
      if (count != null) {
         return Math.max(1, Math.min(2304, count));
      } else {
         String family = spec.replace("minecraft:", "");
         boolean plural = !singular(words).equals(words.trim()) || m.group(3) != null;
         if (family.contains("seed")) {
            return 8;
         } else {
            return !ITEM_FAMILIES.contains(family) && !plural ? 1 : 16;
         }
      }
   }

   record IntentStep(String clause, String capability, JsonObject arguments, String describe, List<AiIntents.ClarifyingQuestion> asks) {
      IntentStep(String clause, String capability, JsonObject arguments, String describe) {
         this(clause, capability, arguments, describe, List.of());
      }
   }

   record IntentPlan(List<AiIntents.IntentStep> steps, String remainder) {
      boolean handledAll() {
         return this.remainder.isBlank();
      }
   }

   record ClarifyingQuestion(String key, String question, List<String> options) {
   }

   record NameLookups(Function<String, int[]> waypoint, Function<String, String> moduleId, Function<String, String> entityId) {
   }
}
