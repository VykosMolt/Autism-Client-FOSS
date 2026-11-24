package autismclient.commands.b;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.modules.aL;
import autismclient.util.AutismClientMessaging;
import autismclient.util.bO;
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

public final class PacketUtil extends Command {
   public PacketUtil() {
      super("friend", "Manage the friends/teams list. add <name>, remove <name>, clear.", "team");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> init());
      root.then(
         LiteralArgumentBuilder.<AutismCommandSource>literal("add")
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("name", StringArgumentType.word())
                  .suggests(PacketUtil::init)
                  .executes(ctx -> aL.init_Z_Str_2(StringArgumentType.getString(ctx, "name")) ? 1 : 0)
            )
      );
      root.then(
         LiteralArgumentBuilder.<AutismCommandSource>literal("remove")
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("name", StringArgumentType.word())
                  .suggests(PacketUtil::load)
                  .executes(ctx -> aL.load_Z_Str(StringArgumentType.getString(ctx, "name")) ? 1 : 0)
            )
      );
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("clear").executes(ctx -> aL.init_Z_28() ? 1 : 0));
   }

   private static int init() {
      List<String> friends = aL.init_List_11();
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

      for (bO.Inner_StreamUtil player : bO.init(Minecraft.getInstance())) {
         String name = player.init_Str();
         if (name != null && name.toLowerCase(Locale.ROOT).startsWith(remaining)) {
            builder.suggest(name);
         }
      }

      return builder.buildFuture();
   }

   private static CompletableFuture<Suggestions> load(CommandContext<AutismCommandSource> ctx, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

      for (String name : aL.init_List_11()) {
         if (name != null && name.toLowerCase(Locale.ROOT).startsWith(remaining)) {
            builder.suggest(name);
         }
      }

      return builder.buildFuture();
   }
}
