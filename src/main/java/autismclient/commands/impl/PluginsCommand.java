package autismclient.commands.impl;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public class PluginsCommand extends Command {
   public PluginsCommand() {
      super("plugins", "Open the plugin scanner (alias of `server plugins`).");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         ServerCommand.load();
         return 1;
      });
   }
}
