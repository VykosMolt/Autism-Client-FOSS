package autismclient.commands.impl;

import autismclient.ai.AiCatalog;
import autismclient.ai.aP;
import autismclient.ai.AiJson;
import autismclient.ai.AiToolsSuggest;
import autismclient.ai.AiToolCallParser;
import autismclient.ai.AiDirectOps;
import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;

public final class ToolsCommand extends Command {
   private static final List<String> SUBCOMMANDS = List.of(
      "goto",
      "parkour",
      "mine",
      "chop",
      "farm",
      "follow",
      "fight",
      "build",
      "fill",
      "dig",
      "surface",
      "cave",
      "smelt",
      "craft",
      "obtain",
      "collect",
      "eat",
      "sleep",
      "trade",
      "events"
   );
   private static final int init_I = 40;
   private static final Set<String> POSITIONED_TOOLS = Set.of(
      "game:goto",
      "game:navigate",
      "game:fill",
      "game:replace",
      "game:mine_area",
      "game:break",
      "game:interact",
      "game:place",
      "game:container",
      "game:read_sign"
   );

   public ToolsCommand() {
      super("tools", "Run AI game/client capabilities directly - no assistant session needed.");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> this.printHelp());
      root.then(
         ((LiteralArgumentBuilder)LiteralArgumentBuilder.<AutismCommandSource>literal("list").executes(ctx -> this.init_I_Str("")))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("category", StringArgumentType.greedyString())
                  .suggests((ctx, builder) -> AiToolsSuggest.suggestFamilies(builder))
                  .executes(ctx -> this.init_I_Str(StringArgumentType.getString(ctx, "category")))
            )
      );
      root.then(
         ((LiteralArgumentBuilder)LiteralArgumentBuilder.<AutismCommandSource>literal("schema")
               .executes(ctx -> this.sync("Missing capability id\nUsage: " + prefix() + "tools schema <id>")))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("id", StringArgumentType.greedyString())
                  .suggests(ToolsCommand::init_CompletableFuture_CommandContext_SuggestionsBuilder)
                  .executes(ctx -> this.load(StringArgumentType.getString(ctx, "id")))
            )
      );
      root.then(
         ((LiteralArgumentBuilder)LiteralArgumentBuilder.<AutismCommandSource>literal("call")
               .executes(ctx -> this.sync("Missing capability id\nUsage: " + prefix() + "tools call <id> [k=v ...]")))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("invocation", StringArgumentType.greedyString())
                  .suggests((ctx, builder) -> AiToolsSuggest.suggestTools(builder))
                  .executes(ctx -> this.read(StringArgumentType.getString(ctx, "invocation")))
            )
      );

      for (String sub : SUBCOMMANDS) {
         LiteralArgumentBuilder<AutismCommandSource> shorthand = (LiteralArgumentBuilder<AutismCommandSource>)LiteralArgumentBuilder.<AutismCommandSource>literal(sub)
            .executes(ctx -> this.load(sub, ""));
         shorthand.then(
            RequiredArgumentBuilder.<AutismCommandSource, String>argument("args", StringArgumentType.greedyString())
               .suggests((ctx, builder) -> AiToolsSuggest.suggestArgs(sub, builder))
               .executes(ctx -> this.load(sub, StringArgumentType.getString(ctx, "args")))
         );
         root.then(shorthand);
      }

      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("status").executes(ctx -> this.printStatus()));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("stop").executes(ctx -> {
         AiDirectOps.stop();
         return 1;
      }));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("pause").executes(ctx -> {
         AiDirectOps.pause();
         return 1;
      }));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("resume").executes(ctx -> {
         AiDirectOps.resume();
         return 1;
      }));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("settings").executes(ctx -> this.read()));
   }

   private int printHelp() {
      String t = prefix() + "tools";
      send("§e" + t + " §7- run AI capabilities directly (no assistant)");
      send("§7" + t + " list [category] §8· §7" + t + " schema <id> §8· §7" + t + " call <id> [k=v ...]");
      send("§7" + t + " goto|parkour|mine|chop|follow|fight|build|fill|dig §8- §7positional shorthands (Tab completes every argument)");
      send("§8goto may dig, pillar and bridge (never where the player cannot build, e.g. adventure mode); parkour never breaks or places");
      send("§7" + t + " parkour [nofall] [speed0|speed1|speed2] [<x> <y> <z> ...]");
      send("§8The flag can go last. Omit the destination to pick it in Freecam.");
      send(
         "§7"
            + t
            + " craft|smelt|obtain|collect <item> [count] §8· §7mine <block[=amount][,block[=amount]...]> [count per block] / chop <block> [count] §8· §7surface|cave|eat|sleep|trade|events"
      );
      send("§7" + t + " status §8· §7" + t + " stop §8· §7" + t + " pause §8· §7" + t + " resume §8· §7" + t + " settings");
      send("§8~ coords are feet-relative; pause suspends ticking (stops fight/shoot/dodge/mlg/telly)");
      return 1;
   }

   private int init_I_Str(String category) {
      Map<String, List<AiCatalog.Entry>> grouped = new TreeMap<>();
      int total = 0;

      for (AiCatalog.Entry entry : AiDirectOps.catalogSnapshot().entries()) {
         if (isToolId(entry.contract().id())) {
            grouped.computeIfAbsent(entry.contract().family(), f -> new ArrayList<>()).add(entry);
            total++;
         }
      }

      String filter = category == null ? "" : category.trim().toLowerCase(Locale.ROOT);
      if (filter.isEmpty()) {
         send(
            "§e" + total + " capabilities §7- " + prefix() + "tools list <category> shows one group; " + prefix() + "tools schema <id> shows arguments:"
         );
         StringBuilder line = new StringBuilder();
         int onLine = 0;

         for (Entry<String, List<AiCatalog.Entry>> family : grouped.entrySet()) {
            if (onLine == 3) {
               send(line.toString());
               line.setLength(0);
               onLine = 0;
            }

            if (onLine > 0) {
               line.append("§8, ");
            }

            line.append("§e").append(family.getKey()).append(" §7(").append(family.getValue().size()).append(")");
            onLine++;
         }

         if (onLine > 0) {
            send(line.toString());
         }

         return 1;
      } else {
         String family = null;

         for (String candidate : grouped.keySet()) {
            if (candidate.equals(filter)) {
               family = candidate;
               break;
            }
         }

         if (family == null) {
            for (String candidate : grouped.keySet()) {
               if (candidate.contains(filter)) {
                  family = candidate;
                  break;
               }
            }
         }

         if (family == null) {
            this.sync("Unknown category '" + category + "'\nCategories: " + String.join(", ", grouped.keySet()));
            return 1;
         }

         List<AiCatalog.Entry> entries = grouped.get(family);
         send("§e" + family + " §7(" + entries.size() + "):");
         int shown = 0;

         for (AiCatalog.Entry entry : entries) {
            if (++shown > 40) {
               break;
            }

            send("§8- §f" + entry.contract().id() + " §7" + AiDirectOps.summarizePurpose(entry.contract().purpose()));
         }

         if (entries.size() > 40) {
            send("§7… and " + (entries.size() - 40) + " more - " + prefix() + "tools schema <id> for any of them.");
         }

         return 1;
      }
   }

   private int load(String rawId) {
      String id = resolveToolId(rawId);
      if (id == null) {
         return this.update(rawId);
      }

      AiCatalog.Entry entry = AiDirectOps.catalogEntry(id);
      if (entry == null) {
         return this.update(rawId);
      }

      send("§f" + id + " §8[" + entry.contract().family() + "]");
      send("§7" + AiDirectOps.summarizePurpose(entry.contract().purpose()));

      JsonObject schema;
      try {
         schema = AiJson.parseObject(entry.contract().inputSchema());
      } catch (Exception e) {
         send("§7(no argument schema)");
         return 1;
      }

      if (schema.has("properties") && !schema.getAsJsonObject("properties").isEmpty()) {
         List<String> required = new ArrayList<>();
         if (schema.has("required")) {
            for (JsonElement r : schema.getAsJsonArray("required")) {
               required.add(r.getAsString());
            }
         }

         for (Entry<String, JsonElement> property : schema.getAsJsonObject("properties").entrySet()) {
            String type = AiJson.getString(property.getValue().getAsJsonObject(), "type", "any");
            send("§8- §f" + property.getKey() + " §7" + type + (required.contains(property.getKey()) ? " §c(required)" : ""));
         }

         return 1;
      } else {
         send("§7(no arguments)");
         return 1;
      }
   }

   private int read(String invocation) {
      String trimmed = invocation.trim();
      int space = -1;

      for (int i = 0; i < trimmed.length(); i++) {
         if (Character.isWhitespace(trimmed.charAt(i))) {
            space = i;
            break;
         }
      }

      String rawId = space < 0 ? trimmed : trimmed.substring(0, space);
      String argsString = space < 0 ? "" : trimmed.substring(space + 1).trim();
      return this.init_I_Str_Str(rawId, argsString);
   }

   private int init_I_Str_Str(String rawId, String argsString) {
      String id = resolveToolId(rawId);
      if (id == null) {
         return this.update(rawId);
      }

      JsonObject args;
      try {
         args = AiToolCallParser.parse(argsString);
      } catch (IllegalArgumentException e) {
         return this.sync(e.getMessage());
      }

      try {
         args = init_JsonObject_Str_JsonObject(id, args);
      } catch (IllegalArgumentException e) {
         return this.sync(e.getMessage());
      }

      return this.init_I_Str_JsonObject(id, args);
   }

   private static JsonObject init_JsonObject_Str_JsonObject(String id, JsonObject args) {
      if (!POSITIONED_TOOLS.contains(id)) {
         return args;
      }

      Minecraft mc = Minecraft.getInstance();
      double px = mc.player != null ? mc.player.getX() : 0.0;
      double py = mc.player != null ? mc.player.getY() : 0.0;
      double pz = mc.player != null ? mc.player.getZ() : 0.0;
      JsonObject out = args.deepCopy();
      String[] keys = new String[]{"x", "y", "z", "x2", "y2", "z2"};
      double[] bases = new double[]{px, py, pz, px, py, pz};

      for (int i = 0; i < keys.length; i++) {
         if (out.has(keys[i]) && out.get(keys[i]).isJsonPrimitive() && out.get(keys[i]).getAsJsonPrimitive().isString()) {
            String raw = out.get(keys[i]).getAsString();
            if (raw.startsWith("~")) {
               out.addProperty(keys[i], AiToolCallParser.parseBlockCoord(raw, bases[i]));
            }
         }
      }

      return out;
   }

   private int load(String sub, String argsString) {
      if (sub.equals("build")) {
         return this.write(argsString.trim());
      }

      List<String> args;
      try {
         args = AiToolCallParser.tokenize(argsString);
      } catch (IllegalArgumentException e) {
         return this.sync(e.getMessage());
      }

      boolean parkour = sub.equals("parkour");
      boolean parkourPicker = parkour && !args.isEmpty() && args.stream().allMatch(AiToolCallParser::isParkourFlag);
      if (parkourPicker || args.isEmpty() && (sub.equals("goto") || parkour)) {
         AiToolCallParser.ParkourFlags parkourOptions;
         try {
            parkourOptions = parkour ? AiToolCallParser.parseParkourFlags(args) : new AiToolCallParser.ParkourFlags(false, -1);
         } catch (IllegalArgumentException e) {
            return this.sync(e.getMessage());
         }

         return !aP.init_Z_Z_cy_CachePool(parkour, parkourOptions)
            ? this.sync("Join a world and stop any running movement task before choosing a destination")
            : 1;
      }

      Minecraft mc = Minecraft.getInstance();
      double px = mc.player != null ? mc.player.getX() : 0.0;
      double py = mc.player != null ? mc.player.getY() : 0.0;
      double pz = mc.player != null ? mc.player.getZ() : 0.0;

      AiToolCallParser.ToolCall invocation;
      try {
         invocation = AiToolCallParser.init(sub, args, px, py, pz);
      } catch (IllegalArgumentException e) {
         return this.sync(e.getMessage());
      }

      return this.init_I_Str_JsonObject(invocation.id(), invocation.arguments());
   }

   private int write(String name) {
      if (name.isEmpty()) {
         this.sync(
            "Usage: "
               + prefix()
               + "tools build <schematic name|blueprint file|{inline json}>"
               + init_Str()
               + "\nBlueprint JSON: {origin:{x,y,z}?, blocks:[{dx,dy,dz,block},...]} in config/autism/ai/blueprints/; schematics load from the game's schematics/ folder"
         );
         return 1;
      }

      if (!name.startsWith("{")) {
         String clean = name.endsWith(".json") ? name.substring(0, name.length() - 5) : name;
         if (clean.contains("..")) {
            return this.sync("Blueprint names are plain file names (no paths)");
         }

         Path file = AiDirectOps.blueprintsDir().resolve(clean + ".json");
         if (!name.contains("/") && !name.contains("\\") && Files.isRegularFile(file)) {
            String content;
            try {
               if (Files.size(file) > 8_000_000L) {
                  return this.sync("Blueprint file exceeds the 2,000,000 character limit");
               }
               content = Files.readString(file);
            } catch (Exception e) {
               return this.sync("Could not read blueprint: " + e.getMessage());
            }

            if (content.length() > 2000000) {
               return this.sync("Blueprint exceeds the 2,000,000 character limit");
            }

            try {
               AiJson.parseObject(content);
            } catch (Exception e) {
               return this.sync("Blueprint is not a JSON object: " + e.getMessage());
            }

            return this.init_I_Str_JsonObject("game:build", AiJson.objectOf("blueprint", content));
         } else {
            return this.init_I_Str_JsonObject("game:build", AiJson.objectOf("schematic", name));
         }
      } else {
         String content = name;
         if (content.length() > 2000000) {
            return this.sync("Blueprint exceeds the 2,000,000 character limit");
         }

         try {
            AiJson.parseObject(content);
         } catch (Exception e) {
            return this.sync("Blueprint is not a JSON object: " + e.getMessage());
         }

         return this.init_I_Str_JsonObject("game:build", AiJson.objectOf("blueprint", content));
      }
   }

   private int printStatus() {
      for (String line : AiDirectOps.statusLines()) {
         send(line);
      }

      return 1;
   }

   private int read() {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.gui != null) {
         mc.execute(() -> mc.gui.setScreen(new autismclient.gui.screens.AiToolsScreen(mc.gui.screen())));
         return 1;
      } else {
         return 1;
      }
   }

   private int init_I_Str_JsonObject(String id, JsonObject args) {
      try {
         AiDirectOps.run(id, args);
         return 1;
      } catch (IllegalArgumentException | IllegalStateException e) {
         return this.sync(e.getMessage());
      } catch (Throwable t) {
         return this.sync(id + " could not start: " + (t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage()));
      }
   }

   private static String resolveToolId(String raw) {
      return AiDirectOps.resolveToolId(raw);
   }

   private int update(String raw) {
      this.sync("Unknown capability: §f" + raw);
      List<String> ids = new ArrayList<>();

      for (AiCatalog.Entry entry : AiDirectOps.catalogSnapshot().entries()) {
         if (isToolId(entry.contract().id())) {
            ids.add(entry.contract().id());
         }
      }

      List<String> near = AiToolCallParser.nearestIds(raw, ids, 4);
      if (!near.isEmpty()) {
         send("§7Did you mean: §f" + String.join("§7, §f", near));
      }

      send("§7" + prefix() + "tools list shows everything callable.");
      return 1;
   }

   private static boolean isToolId(String id) {
      return AiDirectOps.isToolId(id);
   }

   private static CompletableFuture<Suggestions> init_CompletableFuture_CommandContext_SuggestionsBuilder(
      CommandContext<AutismCommandSource> context, SuggestionsBuilder builder
   ) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

      try {
         for (AiCatalog.Entry entry : AiDirectOps.catalogSnapshot().entries()) {
            String id = entry.contract().id();
            if (isToolId(id) && id.toLowerCase(Locale.ROOT).startsWith(remaining)) {
               builder.suggest(id);
            }
         }
      } catch (Throwable var6) {
      }

      return builder.buildFuture();
   }

   private static String init_Str() {
      StringBuilder out = new StringBuilder();

      try {
         Path dir = AiDirectOps.blueprintsDir();
         if (Files.isDirectory(dir)) {
            List<String> names = new ArrayList<>();

            try (Stream<Path> stream = Files.list(dir)) {
               stream.filter(x$0 -> Files.isRegularFile(x$0))
                  .map(f -> f.getFileName().toString())
                  .filter(f -> f.endsWith(".json"))
                  .map(f -> f.substring(0, f.length() - 5))
                  .sorted()
                  .limit(10L)
                  .forEach(names::add);
            }

            if (!names.isEmpty()) {
               out.append("\nBlueprints: ").append(String.join(", ", names));
            }
         }
      } catch (Exception var11) {
      }

      try {
         Path dir = AiDirectOps.schematicsDir();
         if (Files.isDirectory(dir)) {
            List<String> names = new ArrayList<>();

            try (Stream<Path> stream = Files.list(dir)) {
               stream.filter(x$0 -> Files.isRegularFile(x$0))
                  .map(f -> f.getFileName().toString())
                  .filter(f -> f.endsWith(".schem") || f.endsWith(".schematic") || f.endsWith(".litematic"))
                  .sorted()
                  .limit(10L)
                  .forEach(names::add);
            }

            if (!names.isEmpty()) {
               out.append("\nSchematics: ").append(String.join(", ", names));
            }
         }
      } catch (Exception var9) {
      }

      return out.toString();
   }

   private int sync(String message) {
      for (String line : (message == null ? "error" : message).split("\n")) {
         AutismClientMessaging.sendPrefixed((line.startsWith("Usage:") ? "§e" : "§c") + line.replace(".tools", prefix() + "tools"));
      }

      return 1;
   }

   private static void send(String line) {
      AutismClientMessaging.sendPrefixed(line.replace(".tools", prefix() + "tools"));
   }

   private static String prefix() {
      return autismclient.commands.CommandManager.getPrefix();
   }
}
