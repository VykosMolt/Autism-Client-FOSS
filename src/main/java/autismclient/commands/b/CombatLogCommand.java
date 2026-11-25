package autismclient.commands.b;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import autismclient.util.v;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

public final class CombatLogCommand extends Command {
   public CombatLogCommand() {
      super("combatlog", "Record combat decisions and observations to a local ZIP.");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> init(v.load()));
      root.then(
         ((LiteralArgumentBuilder)LiteralArgumentBuilder.<AutismCommandSource>literal("start").executes(ctx -> init(v.init_Str("match"))))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("label", StringArgumentType.greedyString())
                  .suggests(
                     (ctx, b) -> autismclient.commands.StreamUtil.init_CompletableFuture_SuggestionsBuilder_StrArr(
                        b, "match", "autoblock-on", "autoblock-off", "perfect-on", "perfect-off"
                     )
                  )
                  .executes(ctx -> init(v.init_Str(StringArgumentType.getString(ctx, "label"))))
            )
      );
      root.then(
         LiteralArgumentBuilder.<AutismCommandSource>literal("mark")
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("note", StringArgumentType.greedyString())
                  .suggests(
                     (ctx, b) -> autismclient.commands.StreamUtil.init_CompletableFuture_SuggestionsBuilder_StrArr(
                        b, "missed-block", "stale-block", "late-hit", "good-block", "round-start", "round-end"
                     )
                  )
                  .executes(ctx -> init(v.load(StringArgumentType.getString(ctx, "note"))))
            )
      );
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("stop").executes(ctx -> init(v.init_Str())));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("status").executes(ctx -> init(v.load())));
   }

   private static int init(String message) {
      AutismClientMessaging.sendPrefixed(message);
      return 1;
   }
}
