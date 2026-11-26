package autismclient.commands.impl;

import autismclient.ai.AiSettings;
import autismclient.ai.AiRunTrace;
import autismclient.ai.AiService;
import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.mixin.accessor.AutismChatComponentAccessor;
import autismclient.mixin.accessor.AutismCommandHistoryAccessor;
import autismclient.util.AutismClientMessaging;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;

public final class AiCommand extends Command {
   private static final int init_I = 45;
   private static final List<String> init_List = AiSettings.EFFORTS;

   public AiCommand() {
      super("ai", "Control the AI assistant: objectives, provider settings, speedrun, run trace.");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> this.init_I_2());
      root.then(
         ((LiteralArgumentBuilder)LiteralArgumentBuilder.<AutismCommandSource>literal("start")
               .executes(ctx -> this.sync("Missing objective\nUsage: " + load_Str_2() + "ai start <objective...>")))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("objective", StringArgumentType.greedyString())
                  .executes(ctx -> this.init_I_Str_2(StringArgumentType.getString(ctx, "objective")))
            )
      );
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("stop").executes(ctx -> {
         AiService.init_cc().update_V();
         init_V_Str_2("§7AI task stopped.");
         return 1;
      }));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("pause").executes(ctx -> {
         AiService.init_cc().write_V();
         init_V_Str_2("§7AI task paused.");
         return 1;
      }));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("resume").executes(ctx -> {
         AiService.init_cc().read_V();
         init_V_Str_2("§7AI task resumed (when one was paused).");
         return 1;
      }));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("status").executes(ctx -> this.load_I_2()));
      root.then(
         ((LiteralArgumentBuilder)LiteralArgumentBuilder.<AutismCommandSource>literal("chat").executes(ctx -> this.load((String)null)))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("instructions", StringArgumentType.greedyString())
                  .executes(ctx -> this.load(StringArgumentType.getString(ctx, "instructions")))
            )
      );
      root.then(
         ((LiteralArgumentBuilder)LiteralArgumentBuilder.<AutismCommandSource>literal("provider").executes(ctx -> this.read((String)null)))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("name", StringArgumentType.word())
                  .suggests(AiCommand::load)
                  .executes(ctx -> this.read(StringArgumentType.getString(ctx, "name")))
            )
      );
      root.then(
         ((LiteralArgumentBuilder)LiteralArgumentBuilder.<AutismCommandSource>literal("model").executes(ctx -> this.write((String)null)))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("id", StringArgumentType.greedyString())
                  .suggests(AiCommand::init_CompletableFuture_CommandContext_SuggestionsBuilder_2)
                  .executes(ctx -> this.write(StringArgumentType.getString(ctx, "id")))
            )
      );
      root.then(
         ((LiteralArgumentBuilder)LiteralArgumentBuilder.<AutismCommandSource>literal("effort").executes(ctx -> this.update((String)null)))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("level", StringArgumentType.word())
                  .suggests(AiCommand::read)
                  .executes(ctx -> this.update(StringArgumentType.getString(ctx, "level")))
            )
      );
      root.then(
         ((LiteralArgumentBuilder)LiteralArgumentBuilder.<AutismCommandSource>literal("key").executes(ctx -> this.sync("Usage: " + load_Str_2() + "ai key <provider> <keyvalue>")))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("provider", StringArgumentType.word())
                  .suggests(AiCommand::load)
                  .then(
                     RequiredArgumentBuilder.<AutismCommandSource, String>argument("value", StringArgumentType.greedyString())
                        .executes(ctx -> this.init_I_Str_Str_2(StringArgumentType.getString(ctx, "provider"), StringArgumentType.getString(ctx, "value")))
                  )
            )
      );
      root.then(
         ((LiteralArgumentBuilder)LiteralArgumentBuilder.<AutismCommandSource>literal("speedrun").executes(ctx -> this.init_I_I(45)))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, Integer>argument("minutes", IntegerArgumentType.integer(1))
                  .suggests(autismclient.commands.CommandSuggestions::poll)
                  .executes(ctx -> this.init_I_I(IntegerArgumentType.getInteger(ctx, "minutes")))
            )
      );
      root.then(
         ((LiteralArgumentBuilder)LiteralArgumentBuilder.<AutismCommandSource>literal("trace").executes(ctx -> this.load(10)))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, Integer>argument("n", IntegerArgumentType.integer(1, 50))
                  .suggests(autismclient.commands.CommandSuggestions::push)
                  .executes(ctx -> this.load(IntegerArgumentType.getInteger(ctx, "n")))
            )
      );
   }

   private static CompletableFuture<Suggestions> init_CompletableFuture_CommandContext_SuggestionsBuilder_2(
      CommandContext<AutismCommandSource> ignored, SuggestionsBuilder builder
   ) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

      try {
         AiService service = AiService.load_cc();
         if (service != null) {
            TreeSet<String> ids = new TreeSet<>();
            AiSettings settings = service.init_AiSettings();
            if (settings.model != null && !settings.model.isBlank()) {
               ids.add(settings.model);
            }

            for (List<String> known : settings.knownModels.values()) {
               if (known != null) {
                  ids.addAll(known);
               }
            }

            for (String id : ids) {
               if (id.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                  builder.suggest(id);
               }
            }
         }
      } catch (Throwable var8) {
      }

      return builder.buildFuture();
   }

   private int init_I_2() {
      String t = load_Str_2() + "ai";
      init_V_Str_2("§e" + t + " §7- drive the AI assistant");
      init_V_Str_2("§7" + t + " start <objective...> §8· §7" + t + " stop §8· §7" + t + " pause §8· §7" + t + " resume §8· §7" + t + " status");
      init_V_Str_2("§7" + t + " chat [instructions|stop|status] §8- keep a background conversation going without ending the task");
      init_V_Str_2("§7" + t + " provider [name] §8· §7" + t + " model [id] §8· §7" + t + " effort [low|medium|high|max] §8· §7" + t + " key <provider> <key>");
      init_V_Str_2("§7" + t + " speedrun [minutes] §8- beat the Ender Dragon in under N minutes (default 45)");
      init_V_Str_2("§7" + t + " trace [n] §8- last n run-trace events (default 10, max 50)");
      return 1;
   }

   private int init_I_Str_2(String objective) {
      AiService service = AiService.init_cc();
      if (init_Z_cc(service)) {
         return this.sync("An objective is already executing - " + load_Str_2() + "ai stop first.");
      }

      service.write_V_Str(objective);
      init_V_Str_2("§7Objective submitted: §f" + init_Str_Str_I(objective.trim(), 80));
      return 1;
   }

   private int load(String instructions) {
      AiService service = AiService.init_cc();
      String text = instructions == null ? "" : instructions.trim();
      if (text.isEmpty() || text.equalsIgnoreCase("status")) {
         init_V_Str_2("§eChat mode §7- " + (service.read_Z() ? "§aactive" : "§7off") + "§7: §f" + init_Str_Str_I(service.init_Str(), 140));
         JsonObject status = service.init_JsonObject();
         if (status.has("replies")) {
            init_V_Str_2(
               "§7replies: §f" + status.get("replies") + " §8· §7pending: §f" + status.get("pending") + " §8· §7requests: §f" + status.get("requests")
            );
         }

         return 1;
      } else if (!text.equalsIgnoreCase("stop") && !text.equalsIgnoreCase("off")) {
         JsonObject result = service.init_JsonObject_Str(text);
         if ("FAILED".equalsIgnoreCase(init_Str_JsonObject_Str(result, "status"))) {
            return this.sync(init_Str_JsonObject_Str(result, "error"));
         }

         init_V_Str_2("§7Chat mode started/updated: §f" + init_Str_Str_I(service.init_Str(), 120));
         init_V_Str_2("§8It answers players in the background and never owns movement; " + load_Str_2() + "ai chat stop ends it.");
         return 1;
      } else {
         service.sync_V_Str("Chat mode stopped by user");
         init_V_Str_2("§7Chat mode stopped. The current objective, if any, keeps running.");
         return 1;
      }
   }

   private static String init_Str_JsonObject_Str(JsonObject object, String key) {
      return object != null && object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : "";
   }

   private int load_I_2() {
      AiService service = AiService.init_cc();
      AiSettings settings = service.init_AiSettings();
      init_V_Str_2("§eAI Assistant §7- state: §f" + service.init_cc_FileCache());
      init_V_Str_2("§7progress: §f" + (service.open_Str().isBlank() ? "-" : init_Str_Str_I(service.open_Str(), 100)));
      init_V_Str_2("§7provider: §f" + settings.provider + " §8· §7model: §f" + settings.model + " §8· §7effort: §f" + settings.effort);
      String objective = service.load_Str();
      init_V_Str_2("§7elapsed: §f" + init_Str_J(service.read_J()) + " §8· §7objective: §f" + (objective.isBlank() ? "-" : init_Str_Str_I(objective, 80)));
      if (service.read_Z()) {
         init_V_Str_2("§7chat: §aactive §8· §f" + init_Str_Str_I(service.init_Str(), 120));
      }

      return 1;
   }

   private int read(String name) {
      AiService service = AiService.init_cc();
      if (name == null) {
         init_V_Str_2("§7provider: §f" + service.init_AiSettings().provider + " §8(providers: " + init_Str_2() + ")");
         return 1;
      }

      String normalized = name.trim().toUpperCase(Locale.ROOT);
      if (!service.flush_Z(normalized)) {
         return this.sync("Unknown provider '" + name + "'\nProviders: " + init_Str_2());
      }

      init_V_Str_2("§7Provider set to §f" + normalized + "§7 (stock endpoint/model restored); saving and testing the connection in the background.");
      return 1;
   }

   private int write(String id) {
      AiService service = AiService.init_cc();
      if (id == null) {
         init_V_Str_2("§7model: §f" + service.init_AiSettings().model);
         return 1;
      }

      if (id.isBlank()) {
         return this.sync("Model id is blank; nothing changed.");
      }

      service.push(id.trim());
      init_V_Str_2("§7Model set to §f" + id.trim() + "§7; saving in the background.");
      return 1;
   }

   private int update(String level) {
      AiService service = AiService.init_cc();
      if (level == null) {
         init_V_Str_2("§7effort: §f" + service.init_AiSettings().effort);
         return 1;
      }

      String normalized = level.trim().toLowerCase(Locale.ROOT);
      if (!init_List.contains(normalized)) {
         return this.sync("Unknown effort '" + level + "' (low|medium|high|max)");
      }

      service.poll(normalized);
      init_V_Str_2("§7Effort set to §f" + normalized);
      return 1;
   }

   private int init_I_Str_Str_2(String provider, String value) {
      if (value != null && !value.isBlank()) {
         auditForgetKeyLine(value.trim());
         String normalized = provider.trim().toUpperCase(Locale.ROOT);
         if (!AiService.init_cc().init_Z_Str_Str(normalized, value.trim())) {
            return this.sync("Could not set the key for '" + provider + "' (unknown provider, or protected credential storage unavailable).");
         }

         init_V_Str_2("§7Key sealed and saved for §f" + normalized + "§7; it is never shown again.");
         return 1;
      } else {
         return this.sync("Key value is blank; nothing changed.");
      }
   }

   // The typed line already went into chat recall and command_history.txt; drop it there.
   private static void auditForgetKeyLine(String key) {
      Minecraft mc = Minecraft.getInstance();
      mc.execute(() -> {
         try {
            ChatComponent chat = mc.gui.hud.getChat();
            chat.getRecentChat().removeIf(line -> line.contains(key));
            AutismCommandHistoryAccessor history = (AutismCommandHistoryAccessor)((AutismChatComponentAccessor)chat).autism$getCommandHistory();
            if (history.autism$getLastCommands().removeIf(line -> line.contains(key))) {
               history.autism$save();
            }
         } catch (Throwable e) {
         }
      });
   }

   private int init_I_I(int minutes) {
      AiService service = AiService.init_cc();
      if (init_Z_cc(service)) {
         return this.sync("An objective is already executing - " + load_Str_2() + "ai stop first.");
      }

      String objective = "Beat the Ender Dragon in this survival world in under "
         + minutes
         + " minutes, using only legitimate survival actions (no cheats): gear up, obtain blaze rods and ender pearls, find and light the End portal, and kill the Ender Dragon. Move fast, skip anything that does not shorten the run, and when the dragon is dead finish with outcome completed and postcondition {\"kind\":\"beat_game\"}.";
      service.write_V_Str(objective);
      init_V_Str_2("§7Speedrun submitted: beat the Ender Dragon in under §f" + minutes + " minutes§7.");
      return 1;
   }

   private int load(int n) {
      AiRunTrace trace = AiRunTrace.init_bL();
      if (trace == null) {
         init_V_Str_2("§7No run trace yet - start an objective first.");
         return 1;
      }

      List<String> lines = trace.init(Math.min(50, Math.max(1, n)));
      if (lines.isEmpty()) {
         init_V_Str_2("§7Trace is empty (" + trace.init_Path().getFileName() + ").");
         return 1;
      }

      init_V_Str_2("§eLast " + lines.size() + " trace events §8(" + trace.init_Path().getFileName() + "):");

      for (String line : lines) {
         init_V_Str_2("§8" + init_Str_Str_I(line, 220));
      }

      return 1;
   }

   private static boolean init_Z_cc(AiService service) {
      return service.init_cc_FileCache() == AiService.RunState.RUNNING || service.init_cc_FileCache() == AiService.RunState.PLANNING;
   }

   private static String init_Str_2() {
      StringBuilder out = new StringBuilder();

      for (AiSettings.Provider provider : AiSettings.Provider.values()) {
         if (out.length() > 0) {
            out.append(", ");
         }

         out.append(provider.name());
      }

      return out.toString();
   }

   private static String init_Str_Str_I(String text, int max) {
      String flat = text.replaceAll("\\s+", " ").trim();
      return flat.length() <= max ? flat : flat.substring(0, max - 1) + "…";
   }

   private static String init_Str_J(long millis) {
      long seconds = Math.max(0L, millis / 1000L);
      if (seconds >= 3600L) {
         return seconds / 3600L + "h " + seconds % 3600L / 60L + "m";
      } else {
         return seconds >= 60L ? seconds / 60L + "m " + seconds % 60L + "s" : seconds + "s";
      }
   }

   private static CompletableFuture<Suggestions> load(CommandContext<AutismCommandSource> context, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

      for (AiSettings.Provider provider : AiSettings.Provider.values()) {
         if (provider.name().toLowerCase(Locale.ROOT).startsWith(remaining)) {
            builder.suggest(provider.name());
         }
      }

      return builder.buildFuture();
   }

   private static CompletableFuture<Suggestions> read(CommandContext<AutismCommandSource> context, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

      for (String effort : init_List) {
         if (effort.startsWith(remaining)) {
            builder.suggest(effort);
         }
      }

      return builder.buildFuture();
   }

   private int sync(String message) {
      for (String line : (message == null ? "error" : message).split("\n")) {
         AutismClientMessaging.sendPrefixed((line.startsWith("Usage:") ? "§e" : "§c") + line.replace(".ai", load_Str_2() + "ai"));
      }

      return 1;
   }

   private static void init_V_Str_2(String line) {
      AutismClientMessaging.sendPrefixed(line.replace(".ai", load_Str_2() + "ai"));
   }

   private static String load_Str_2() {
      return autismclient.commands.CommandManager.getPrefix();
   }
}
