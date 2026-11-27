package autismclient.commands.impl;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import autismclient.util.CommandPrefixHelper;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

public class PrefixCommand extends Command {
   public PrefixCommand() {
      super("prefix", "Change the AUTISM command prefix.");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         AutismClientMessaging.sendPrefixed("Current prefix: " + CommandPrefixHelper.currentPrefix());
         return 1;
      });
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("reset").executes(ctx -> {
         String prefix = CommandPrefixHelper.defaultPrefix();
         autismclient.modules.ModuleRuntimeManager.getInstance().setCommandPrefix(prefix);
         AutismClientMessaging.sendPrefixed("Prefix reset to " + prefix);
         return 1;
      }));
      root.then(RequiredArgumentBuilder.<AutismCommandSource, String>argument("new", StringArgumentType.word()).suggests(autismclient.commands.CommandSuggestions::add).executes(ctx -> {
         String requested = StringArgumentType.getString(ctx, "new");
         if (!CommandPrefixHelper.ALLOWED_PREFIXES.contains(requested)) {
            AutismClientMessaging.sendPrefixed("Prefix must be one of: . % - _ * # @ & =");
            return 1;
         }

         String selected = CommandPrefixHelper.normalizePrefix(requested);
         autismclient.modules.ModuleRuntimeManager.getInstance().setCommandPrefix(selected);
         AutismClientMessaging.sendPrefixed("Prefix set to " + selected);
         return 1;
      }));
   }
}
