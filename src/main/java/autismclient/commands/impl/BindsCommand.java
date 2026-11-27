package autismclient.commands.impl;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Map;
import java.util.Map.Entry;

public class BindsCommand extends Command {
   public BindsCommand() {
      super("binds", "List command keybinds (set with `bind`).");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         Map<Integer, String> binds = autismclient.modules.ModuleRuntimeManager.getInstance().getCommandBinds();
         if (binds.isEmpty()) {
            AutismClientMessaging.sendPrefixed("§eNo command keybinds set.");
            return 1;
         }

         AutismClientMessaging.sendPrefixed("§e" + binds.size() + " command keybinds:");

         for (Entry<Integer, String> e : binds.entrySet()) {
            AutismClientMessaging.sendPrefixed("§b" + autismclient.commands.arguments.KeyBindArgumentType.keyName(e.getKey()) + "§7 → §f" + e.getValue());
         }

         return 1;
      });
   }
}
