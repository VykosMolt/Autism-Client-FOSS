package autismclient.commands.impl;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.modules.aD;
import autismclient.modules.al;
import autismclient.util.AutismClientMessaging;
import autismclient.util.macro.ak;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

public class ToggleCommand extends Command {
   public ToggleCommand() {
      super("toggle", "Toggle a module on/off.", "t");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         AutismClientMessaging.sendPrefixed("§eUsage: §f" + autismclient.commands.CommandManager.init_Str() + "toggle <module> [on|off|toggle]");
         return 1;
      });
      root.then(
         ((RequiredArgumentBuilder)RequiredArgumentBuilder.<AutismCommandSource, String>argument("module", autismclient.commands.arguments.ModuleArgumentType.init_ConfigStore())
               .executes(ctx -> init(autismclient.commands.arguments.ModuleArgumentType.init(ctx, "module"), ak.Inner_StreamUtil.init)))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("state", StringArgumentType.word())
                  .suggests(autismclient.commands.CommandSuggestions::init_CompletableFuture_CommandContext_SuggestionsBuilder)
                  .executes(
                     ctx -> {
                        String state = StringArgumentType.getString(ctx, "state").toLowerCase();
                        if (!state.equals("on")
                           && !state.equals("enable")
                           && !state.equals("true")
                           && !state.equals("1")
                           && !state.equals("off")
                           && !state.equals("disable")
                           && !state.equals("false")
                           && !state.equals("0")
                           && !state.equals("toggle")) {
                           AutismClientMessaging.sendPrefixed("§cUnknown toggle state: §f" + state);
                           AutismClientMessaging.sendPrefixed("§7Use §fon§7, §foff§7, or §ftoggle§7.");
                           return 1;
                        }

                        ak.Inner_StreamUtil var10000;
                        switch (state) {
                           case "on":
                           case "enable":
                           case "true":
                           case "1":
                              var10000 = ak.Inner_StreamUtil.load;
                              break;
                           case "off":
                           case "disable":
                           case "false":
                           case "0":
                              var10000 = ak.Inner_StreamUtil.read;
                              break;
                           default:
                              var10000 = ak.Inner_StreamUtil.init;
                        }

                        ak.Inner_StreamUtil mode = var10000;
                        return init(autismclient.commands.arguments.ModuleArgumentType.init(ctx, "module"), mode);
                     }
                  )
            )
      );
   }

   private static int init(String moduleName, ak.Inner_StreamUtil mode) {
      boolean ok = al.init_Z_Str_ak_StreamUtil(moduleName, mode);
      if (ok && aD.init(moduleName)) {
         return 1;
      }

      if (ok) {
         AutismClientMessaging.sendPrefixed("§a" + mode.name().toLowerCase() + ": §f" + moduleName);
      } else {
         AutismClientMessaging.sendPrefixed("§cModule not found: §f" + moduleName);
      }

      return 1;
   }
}
