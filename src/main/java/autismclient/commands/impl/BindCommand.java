package autismclient.commands.impl;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

public class BindCommand extends Command {
   public BindCommand() {
      super("bind", "Bind a key to a command. Usage: bind <key> <command…> | bind clear <key>");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         String prefix = autismclient.commands.CommandManager.getPrefix();
         AutismClientMessaging.sendPrefixed("§eUsage: §f" + prefix + "bind <key> <command>");
         AutismClientMessaging.sendPrefixed("§7Example: §f" + prefix + "bind G " + prefix + "macro myMacro");
         return 1;
      });
      root.then(
         LiteralArgumentBuilder.<AutismCommandSource>literal("clear").then(RequiredArgumentBuilder.<AutismCommandSource, Integer>argument("key", autismclient.commands.arguments.KeyBindArgumentType.key()).executes(ctx -> {
            int key = autismclient.commands.arguments.KeyBindArgumentType.getKey(ctx, "key");
            autismclient.modules.ModuleRuntimeManager.getInstance().removeCommandBind(key);
            AutismClientMessaging.sendPrefixed("§eCleared bind for §f" + autismclient.commands.arguments.KeyBindArgumentType.keyName(key));
            return 1;
         }))
      );
      root.then(
         RequiredArgumentBuilder.<AutismCommandSource, Integer>argument("key", autismclient.commands.arguments.KeyBindArgumentType.key())
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("command", StringArgumentType.greedyString()).suggests(autismclient.commands.CommandSuggestions::suggestCommand).executes(ctx -> {
                  int key = autismclient.commands.arguments.KeyBindArgumentType.getKey(ctx, "key");
                  String cmd = StringArgumentType.getString(ctx, "command");
                  autismclient.modules.ModuleRuntimeManager.getInstance().setCommandBind(key, cmd);
                  AutismClientMessaging.sendPrefixed("§aBound §f" + autismclient.commands.arguments.KeyBindArgumentType.keyName(key) + "§a → §f" + cmd);
                  return 1;
               })
            )
      );
   }
}
