package autismclient.commands.impl;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismSessionState;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

public class DelayCommand extends Command {
   public DelayCommand() {
      super("delay", "Toggle the GUI packet delay queue.");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> toggleDelay((String)null));
      root.then(
         RequiredArgumentBuilder.<AutismCommandSource, String>argument("state", StringArgumentType.word())
            .suggests(autismclient.commands.CommandSuggestions::init_CompletableFuture_CommandContext_SuggestionsBuilder)
            .executes(ctx -> toggleDelay(StringArgumentType.getString(ctx, "state")))
      );
   }

   private static int toggleDelay(String state) {
      boolean current = AutismSessionState.getInstance().isDelayPacketsEnabled();
      if (state != null) {
         String lower = state.toLowerCase();
         if (!lower.equals("on")
            && !lower.equals("enable")
            && !lower.equals("true")
            && !lower.equals("1")
            && !lower.equals("off")
            && !lower.equals("disable")
            && !lower.equals("false")
            && !lower.equals("0")
            && !lower.equals("toggle")) {
            AutismClientMessaging.sendPrefixed("Unknown delay state: " + state);
            AutismClientMessaging.sendPrefixed("Use on, off, or toggle.");
            return 1;
         }
      }

      boolean var10000;
      if (state == null) {
         var10000 = !current;
      } else {
         switch (state.toLowerCase()) {
            case "on":
            case "enable":
            case "true":
            case "1":
               var10000 = true;
               break;
            case "off":
            case "disable":
            case "false":
            case "0":
               var10000 = false;
               break;
            case "toggle":
               var10000 = !current;
               break;
            default:
               var10000 = !current;
         }
      }

      boolean target = var10000;
      autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
      int flushed = module.toggleDelayPackets(target);
      module.notifyDelayToggle(target, flushed);
      return 1;
   }
}
