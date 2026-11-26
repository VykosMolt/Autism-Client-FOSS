package autismclient.commands.impl;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.modules.TeamsModule;
import autismclient.util.AutismClientMessaging;
import autismclient.util.TabPlayerListParser;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;

public final class FriendCommand extends Command {
   public FriendCommand() {
      super("friend", "Manage the friends/teams list. add <name>, remove <name>, clear.", "team");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> init());
      root.then(
         LiteralArgumentBuilder.<AutismCommandSource>literal("add")
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("name", StringArgumentType.word())
                  .suggests(FriendCommand::init)
                  .executes(ctx -> TeamsModule.init_Z_Str_2(StringArgumentType.getString(ctx, "name")) ? 1 : 0)
            )
      );
      root.then(
         LiteralArgumentBuilder.<AutismCommandSource>literal("remove")
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("name", StringArgumentType.word())
                  .suggests(FriendCommand::load)
                  .executes(ctx -> TeamsModule.load_Z_Str(StringArgumentType.getString(ctx, "name")) ? 1 : 0)
            )
      );
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("clear").executes(ctx -> TeamsModule.init_Z_28() ? 1 : 0));
   }

   private static int init() {
      List<String> friends = TeamsModule.init_List_11();
      if (friends.isEmpty()) {
         AutismClientMessaging.sendPrefixed("§7Friend list is empty.");
         return 1;
      } else {
         AutismClientMessaging.sendPrefixed("§7Friends (§f" + friends.size() + "§7): §f" + String.join("§7, §f", friends));
         return 1;
      }
   }

   private static CompletableFuture<Suggestions> init(CommandContext<AutismCommandSource> ctx, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

      for (TabPlayerListParser.ParsedPlayer player : TabPlayerListParser.init(Minecraft.getInstance())) {
         String name = player.init_Str();
         if (name != null && name.toLowerCase(Locale.ROOT).startsWith(remaining)) {
            builder.suggest(name);
         }
      }

      return builder.buildFuture();
   }

   private static CompletableFuture<Suggestions> load(CommandContext<AutismCommandSource> ctx, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

      for (String name : TeamsModule.init_List_11()) {
         if (name != null && name.toLowerCase(Locale.ROOT).startsWith(remaining)) {
            builder.suggest(name);
         }
      }

      return builder.buildFuture();
   }
}
