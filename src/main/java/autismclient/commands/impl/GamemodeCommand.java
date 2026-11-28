package autismclient.commands.impl;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import autismclient.util.FakeGamemodeManager;
import autismclient.util.RealGamemodeSender;
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
         AutismClientMessaging.sendPrefixed("§eUsage: " + autismclient.commands.CommandManager.getPrefix() + "gamemode <survival|creative|adventure|spectator>");
         AutismClientMessaging.sendPrefixed("§7Client-side only: " + autismclient.commands.CommandManager.getPrefix() + "fakegm");
         return 1;
      });
      root.then(RequiredArgumentBuilder.<AutismCommandSource, String>argument("mode", StringArgumentType.word()).suggests(autismclient.commands.CommandSuggestions::suggestGamemode).executes(ctx -> {
         String mode = StringArgumentType.getString(ctx, "mode").toLowerCase(Locale.ROOT);
         GameType resolved = FakeGamemodeManager.parseMode(mode);
         if (resolved != null) {
            FakeGamemodeManager.ApplyResult result = RealGamemodeSender.init(resolved);
            AutismClientMessaging.sendPrefixed((result.success() ? "§a" : "§c") + result.message());
            return 1;
         }

         AutismClientMessaging.sendPrefixed("§cUnknown mode: §f" + mode);
         if ("reset".equals(mode) || "r".equals(mode)) {
            AutismClientMessaging.sendPrefixed("§7Reset only applies to the fake mode: §f" + autismclient.commands.CommandManager.getPrefix() + "fakegm reset");
         }

         return 1;
      }));
   }
}
