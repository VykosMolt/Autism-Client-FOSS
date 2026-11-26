package autismclient.commands.impl;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.Locale;

public class HelpCommand extends Command {
   public HelpCommand() {
      super("help", "Show usage info for a command (or list all).");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         init();
         return 1;
      });
      root.then(RequiredArgumentBuilder.<AutismCommandSource, String>argument("command", StringArgumentType.word()).suggests((ctx, builder) -> {
         String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

         for (Command command : autismclient.commands.CommandManager.getCommands()) {
            if (command.name().toLowerCase(Locale.ROOT).startsWith(remaining)) {
               builder.suggest(command.name());
            }

            for (String alias : command.aliases()) {
               if (alias != null && alias.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                  builder.suggest(alias);
               }
            }
         }

         return builder.buildFuture();
      }).executes(ctx -> {
         String name = StringArgumentType.getString(ctx, "command");
         Command cmd = autismclient.commands.CommandManager.getCommand(name);
         if (cmd == null) {
            AutismClientMessaging.sendPrefixed("§cNo such command: §f" + name);
            return 1;
         }

         String prefix = autismclient.commands.CommandManager.getPrefix();
         AutismClientMessaging.sendPrefixed("§b" + prefix + cmd.name() + " §7" + cmd.description());
         if (cmd.aliases().length > 0) {
            AutismClientMessaging.sendPrefixed("§7aliases: §f" + String.join(", ", cmd.aliases()));
         }

         return 1;
      }));
   }

   private static void init() {
      String prefix = autismclient.commands.CommandManager.getPrefix();

      for (Command c : autismclient.commands.CommandManager.getCommands()) {
         AutismClientMessaging.sendPrefixed("§b" + prefix + c.name() + "§7 - " + c.description());
      }
   }
}
