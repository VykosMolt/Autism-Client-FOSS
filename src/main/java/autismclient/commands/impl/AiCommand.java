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
   private static final List<String> EFFORTS = AiSettings.EFFORTS;

   public AiCommand() {
      super("ai", "Control the AI assistant: objectives, provider settings, speedrun, run trace.");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> this.showHelp());
      root.then(
         ((LiteralArgumentBuilder)LiteralArgumentBuilder.<AutismCommandSource>literal("start")
               .executes(ctx -> this.sendError("Missing objective\nUsage: " + commandPrefix() + "ai start <objective...>")))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("objective", StringArgumentType.greedyString())
                  .executes(ctx -> this.start(StringArgumentType.getString(ctx, "objective")))
            )
      );
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("stop").executes(ctx -> {
         AiService.getInstance().stopTask();
         sendLine("§7AI task stopped.");
         return 1;
      }));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("pause").executes(ctx -> {
         AiService.getInstance().pauseTask();
         sendLine("§7AI task paused.");
         return 1;
      }));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("resume").executes(ctx -> {
         AiService.getInstance().resumeTask();
         sendLine("§7AI task resumed (when one was paused).");
         return 1;
      }));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("status").executes(ctx -> this.status()));
      root.then(
         ((LiteralArgumentBuilder)LiteralArgumentBuilder.<AutismCommandSource>literal("chat").executes(ctx -> this.chat((String)null)))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("instructions", StringArgumentType.greedyString())
                  .executes(ctx -> this.chat(StringArgumentType.getString(ctx, "instructions")))
            )
      );
      root.then(
         ((LiteralArgumentBuilder)LiteralArgumentBuilder.<AutismCommandSource>literal("provider").executes(ctx -> this.provider((String)null)))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("name", StringArgumentType.word())
                  .suggests(AiCommand::suggestProviders)
                  .executes(ctx -> this.provider(StringArgumentType.getString(ctx, "name")))
            )
      );
      root.then(
         ((LiteralArgumentBuilder)LiteralArgumentBuilder.<AutismCommandSource>literal("model").executes(ctx -> this.model((String)null)))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("id", StringArgumentType.greedyString())
                  .suggests(AiCommand::suggestModels)
                  .executes(ctx -> this.model(StringArgumentType.getString(ctx, "id")))
            )
      );
      root.then(
         ((LiteralArgumentBuilder)LiteralArgumentBuilder.<AutismCommandSource>literal("effort").executes(ctx -> this.effort((String)null)))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("level", StringArgumentType.word())
                  .suggests(AiCommand::suggestEfforts)
                  .executes(ctx -> this.effort(StringArgumentType.getString(ctx, "level")))
            )
      );
      root.then(
         ((LiteralArgumentBuilder)LiteralArgumentBuilder.<AutismCommandSource>literal("key").executes(ctx -> this.sendError("Usage: " + commandPrefix() + "ai key <provider> <keyvalue>")))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("provider", StringArgumentType.word())
                  .suggests(AiCommand::suggestProviders)
                  .then(
                     RequiredArgumentBuilder.<AutismCommandSource, String>argument("value", StringArgumentType.greedyString())
                        .executes(ctx -> this.setKey(StringArgumentType.getString(ctx, "provider"), StringArgumentType.getString(ctx, "value")))
                  )
            )
      );
      root.then(
         ((LiteralArgumentBuilder)LiteralArgumentBuilder.<AutismCommandSource>literal("speedrun").executes(ctx -> this.speedrun(45)))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, Integer>argument("minutes", IntegerArgumentType.integer(1))
                  .suggests(autismclient.commands.CommandSuggestions::suggestMinutes)
                  .executes(ctx -> this.speedrun(IntegerArgumentType.getInteger(ctx, "minutes")))
            )
      );
      root.then(
         ((LiteralArgumentBuilder)LiteralArgumentBuilder.<AutismCommandSource>literal("trace").executes(ctx -> this.trace(10)))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, Integer>argument("n", IntegerArgumentType.integer(1, 50))
                  .suggests(autismclient.commands.CommandSuggestions::suggestCounts)
                  .executes(ctx -> this.trace(IntegerArgumentType.getInteger(ctx, "n")))
            )
      );
   }

   private static CompletableFuture<Suggestions> suggestModels(
      CommandContext<AutismCommandSource> ignored, SuggestionsBuilder builder
   ) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

      try {
         AiService service = AiService.getExistingInstance();
         if (service != null) {
            TreeSet<String> ids = new TreeSet<>();
            AiSettings settings = service.getSettings();
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

   private int showHelp() {
      String t = commandPrefix() + "ai";
      sendLine("§e" + t + " §7- drive the AI assistant");
      sendLine("§7" + t + " start <objective...> §8· §7" + t + " stop §8· §7" + t + " pause §8· §7" + t + " resume §8· §7" + t + " status");
      sendLine("§7" + t + " chat [instructions|stop|status] §8- keep a background conversation going without ending the task");
      sendLine("§7" + t + " provider [name] §8· §7" + t + " model [id] §8· §7" + t + " effort [low|medium|high|max] §8· §7" + t + " key <provider> <key>");
      sendLine("§7" + t + " speedrun [minutes] §8- beat the Ender Dragon in under N minutes (default 45)");
      sendLine("§7" + t + " trace [n] §8- last n run-trace events (default 10, max 50)");
      return 1;
   }

   private int start(String objective) {
      AiService service = AiService.getInstance();
      if (isBusy(service)) {
         return this.sendError("An objective is already executing - " + commandPrefix() + "ai stop first.");
      }

      service.submitPrompt(objective);
      sendLine("§7Objective submitted: §f" + flatClip(objective.trim(), 80));
      return 1;
   }

   private int chat(String instructions) {
      AiService service = AiService.getInstance();
      String text = instructions == null ? "" : instructions.trim();
      if (text.isEmpty() || text.equalsIgnoreCase("status")) {
         sendLine("§eChat mode §7- " + (service.isChatModeActive() ? "§aactive" : "§7off") + "§7: §f" + flatClip(service.getChatModeSummary(), 140));
         JsonObject status = service.getChatModeStatusJson();
         if (status.has("replies")) {
            sendLine(
               "§7replies: §f" + status.get("replies") + " §8· §7pending: §f" + status.get("pending") + " §8· §7requests: §f" + status.get("requests")
            );
         }

         return 1;
      } else if (!text.equalsIgnoreCase("stop") && !text.equalsIgnoreCase("off")) {
         JsonObject result = service.startChatMode(text);
         if ("FAILED".equalsIgnoreCase(stringField(result, "status"))) {
            return this.sendError(stringField(result, "error"));
         }

         sendLine("§7Chat mode started/updated: §f" + flatClip(service.getChatModeSummary(), 120));
         sendLine("§8It answers players in the background and never owns movement; " + commandPrefix() + "ai chat stop ends it.");
         return 1;
      } else {
         service.stopChatMode("Chat mode stopped by user");
         sendLine("§7Chat mode stopped. The current objective, if any, keeps running.");
         return 1;
      }
   }

   private static String stringField(JsonObject object, String key) {
      return object != null && object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : "";
   }

   private int status() {
      AiService service = AiService.getInstance();
      AiSettings settings = service.getSettings();
      sendLine("§eAI Assistant §7- state: §f" + service.getRunState());
      sendLine("§7progress: §f" + (service.getStatusLine().isBlank() ? "-" : flatClip(service.getStatusLine(), 100)));
      sendLine("§7provider: §f" + settings.provider + " §8· §7model: §f" + settings.model + " §8· §7effort: §f" + settings.effort);
      String objective = service.getObjective();
      sendLine("§7elapsed: §f" + formatDuration(service.getElapsedTaskMillis()) + " §8· §7objective: §f" + (objective.isBlank() ? "-" : flatClip(objective, 80)));
      if (service.isChatModeActive()) {
         sendLine("§7chat: §aactive §8· §f" + flatClip(service.getChatModeSummary(), 120));
      }

      return 1;
   }

   private int provider(String name) {
      AiService service = AiService.getInstance();
      if (name == null) {
         sendLine("§7provider: §f" + service.getSettings().provider + " §8(providers: " + providerList() + ")");
         return 1;
      }

      String normalized = name.trim().toUpperCase(Locale.ROOT);
      if (!service.selectProvider(normalized)) {
         return this.sendError("Unknown provider '" + name + "'\nProviders: " + providerList());
      }

      sendLine("§7Provider set to §f" + normalized + "§7 (stock endpoint/model restored); saving and testing the connection in the background.");
      return 1;
   }

   private int model(String id) {
      AiService service = AiService.getInstance();
      if (id == null) {
         sendLine("§7model: §f" + service.getSettings().model);
         return 1;
      }

      if (id.isBlank()) {
         return this.sendError("Model id is blank; nothing changed.");
      }

      service.selectModel(id.trim());
      sendLine("§7Model set to §f" + id.trim() + "§7; saving in the background.");
      return 1;
   }

   private int effort(String level) {
      AiService service = AiService.getInstance();
      if (level == null) {
         sendLine("§7effort: §f" + service.getSettings().effort);
         return 1;
      }

      String normalized = level.trim().toLowerCase(Locale.ROOT);
      if (!EFFORTS.contains(normalized)) {
         return this.sendError("Unknown effort '" + level + "' (low|medium|high|max)");
      }

      service.setEffortLevel(normalized);
      sendLine("§7Effort set to §f" + normalized);
      return 1;
   }

   private int setKey(String provider, String value) {
      if (value != null && !value.isBlank()) {
         auditForgetKeyLine(value.trim());
         String normalized = provider.trim().toUpperCase(Locale.ROOT);
         if (!AiService.getInstance().setProviderKey(normalized, value.trim())) {
            return this.sendError("Could not set the key for '" + provider + "' (unknown provider, or protected credential storage unavailable).");
         }

         sendLine("§7Key sealed and saved for §f" + normalized + "§7; it is never shown again.");
         return 1;
      } else {
         return this.sendError("Key value is blank; nothing changed.");
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

   private int speedrun(int minutes) {
      AiService service = AiService.getInstance();
      if (isBusy(service)) {
         return this.sendError("An objective is already executing - " + commandPrefix() + "ai stop first.");
      }

      String objective = "Beat the Ender Dragon in this survival world in under "
         + minutes
         + " minutes, using only legitimate survival actions (no cheats): gear up, obtain blaze rods and ender pearls, find and light the End portal, and kill the Ender Dragon. Move fast, skip anything that does not shorten the run, and when the dragon is dead finish with outcome completed and postcondition {\"kind\":\"beat_game\"}.";
      service.submitPrompt(objective);
      sendLine("§7Speedrun submitted: beat the Ender Dragon in under §f" + minutes + " minutes§7.");
      return 1;
   }

   private int trace(int n) {
      AiRunTrace trace = AiRunTrace.current();
      if (trace == null) {
         sendLine("§7No run trace yet - start an objective first.");
         return 1;
      }

      List<String> lines = trace.tailLines(Math.min(50, Math.max(1, n)));
      if (lines.isEmpty()) {
         sendLine("§7Trace is empty (" + trace.filePath().getFileName() + ").");
         return 1;
      }

      sendLine("§eLast " + lines.size() + " trace events §8(" + trace.filePath().getFileName() + "):");

      for (String line : lines) {
         sendLine("§8" + flatClip(line, 220));
      }

      return 1;
   }

   private static boolean isBusy(AiService service) {
      return service.getRunState() == AiService.RunState.RUNNING || service.getRunState() == AiService.RunState.PLANNING;
   }

   private static String providerList() {
      StringBuilder out = new StringBuilder();

      for (AiSettings.Provider provider : AiSettings.Provider.values()) {
         if (out.length() > 0) {
            out.append(", ");
         }

         out.append(provider.name());
      }

      return out.toString();
   }

   private static String flatClip(String text, int max) {
      String flat = text.replaceAll("\\s+", " ").trim();
      return flat.length() <= max ? flat : flat.substring(0, max - 1) + "…";
   }

   private static String formatDuration(long millis) {
      long seconds = Math.max(0L, millis / 1000L);
      if (seconds >= 3600L) {
         return seconds / 3600L + "h " + seconds % 3600L / 60L + "m";
      } else {
         return seconds >= 60L ? seconds / 60L + "m " + seconds % 60L + "s" : seconds + "s";
      }
   }

   private static CompletableFuture<Suggestions> suggestProviders(CommandContext<AutismCommandSource> context, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

      for (AiSettings.Provider provider : AiSettings.Provider.values()) {
         if (provider.name().toLowerCase(Locale.ROOT).startsWith(remaining)) {
            builder.suggest(provider.name());
         }
      }

      return builder.buildFuture();
   }

   private static CompletableFuture<Suggestions> suggestEfforts(CommandContext<AutismCommandSource> context, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

      for (String effort : EFFORTS) {
         if (effort.startsWith(remaining)) {
            builder.suggest(effort);
         }
      }

      return builder.buildFuture();
   }

   private int sendError(String message) {
      for (String line : (message == null ? "error" : message).split("\n")) {
         AutismClientMessaging.sendPrefixed((line.startsWith("Usage:") ? "§e" : "§c") + line.replace(".ai", commandPrefix() + "ai"));
      }

      return 1;
   }

   private static void sendLine(String line) {
      AutismClientMessaging.sendPrefixed(line.replace(".ai", commandPrefix() + "ai"));
   }

   private static String commandPrefix() {
      return autismclient.commands.CommandManager.getPrefix();
   }
}
