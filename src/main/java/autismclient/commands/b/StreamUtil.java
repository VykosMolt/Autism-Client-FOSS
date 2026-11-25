package autismclient.commands.b;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

public class StreamUtil extends Command {
   public StreamUtil() {
      super("bind", "Bind a key to a command. Usage: bind <key> <command…> | bind clear <key>");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         String prefix = autismclient.commands.DataUtil.init_Str();
         AutismClientMessaging.sendPrefixed("§eUsage: §f" + prefix + "bind <key> <command>");
         AutismClientMessaging.sendPrefixed("§7Example: §f" + prefix + "bind G " + prefix + "macro myMacro");
         return 1;
      });
      root.then(
         LiteralArgumentBuilder.<AutismCommandSource>literal("clear").then(RequiredArgumentBuilder.<AutismCommandSource, Integer>argument("key", autismclient.commands.a.StreamUtil.init()).executes(ctx -> {
            int key = autismclient.commands.a.StreamUtil.init(ctx, "key");
            autismclient.modules.PoolEntry.init_PoolEntry().init_V_I(key);
            AutismClientMessaging.sendPrefixed("§eCleared bind for §f" + autismclient.commands.a.StreamUtil.init_Str_I(key));
            return 1;
         }))
      );
      root.then(
         RequiredArgumentBuilder.<AutismCommandSource, Integer>argument("key", autismclient.commands.a.StreamUtil.init())
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("command", StringArgumentType.greedyString()).suggests(autismclient.commands.StreamUtil::run).executes(ctx -> {
                  int key = autismclient.commands.a.StreamUtil.init(ctx, "key");
                  String cmd = StringArgumentType.getString(ctx, "command");
                  autismclient.modules.PoolEntry.init_PoolEntry().init(key, cmd);
                  AutismClientMessaging.sendPrefixed("§aBound §f" + autismclient.commands.a.StreamUtil.init_Str_I(key) + "§a → §f" + cmd);
                  return 1;
               })
            )
      );
   }
}
