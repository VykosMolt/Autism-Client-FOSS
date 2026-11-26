package autismclient.commands.b;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import autismclient.util.W;
import autismclient.util.ae;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.Locale;
import net.minecraft.world.level.GameType;

public class GamemodeCommand extends Command {
   public GamemodeCommand() {
      super("gamemode", "Set your real game mode (no chat command sent).");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         AutismClientMessaging.sendPrefixed("§eUsage: " + autismclient.commands.CommandManager.init_Str() + "gamemode <survival|creative|adventure|spectator>");
         AutismClientMessaging.sendPrefixed("§7Client-side only: " + autismclient.commands.CommandManager.init_Str() + "fakegm");
         return 1;
      });
      root.then(RequiredArgumentBuilder.<AutismCommandSource, String>argument("mode", StringArgumentType.word()).suggests(autismclient.commands.CommandSuggestions::put).executes(ctx -> {
         String mode = StringArgumentType.getString(ctx, "mode").toLowerCase(Locale.ROOT);
         GameType resolved = W.init(mode);
         if (resolved != null) {
            W.Inner_DataUtil result = ae.init(resolved);
            AutismClientMessaging.sendPrefixed((result.init_Z() ? "§a" : "§c") + result.init_Str());
            return 1;
         }

         AutismClientMessaging.sendPrefixed("§cUnknown mode: §f" + mode);
         if ("reset".equals(mode) || "r".equals(mode)) {
            AutismClientMessaging.sendPrefixed("§7Reset only applies to the fake mode: §f" + autismclient.commands.CommandManager.init_Str() + "fakegm reset");
         }

         return 1;
      }));
   }
}
