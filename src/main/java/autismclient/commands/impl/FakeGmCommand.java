package autismclient.commands.impl;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import autismclient.util.FakeGamemodeManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.Locale;
import net.minecraft.world.level.GameType;

public class FakeGmCommand extends Command {
   public FakeGmCommand() {
      super("fakegm", "Set a fake client-side game mode.");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         AutismClientMessaging.sendPrefixed("§eUsage: " + autismclient.commands.CommandManager.getPrefix() + "fakegm <survival|creative|adventure|spectator|reset>");
         AutismClientMessaging.sendPrefixed("§7Real change: " + autismclient.commands.CommandManager.getPrefix() + "gamemode");
         return 1;
      });
      root.then(RequiredArgumentBuilder.<AutismCommandSource, String>argument("mode", StringArgumentType.word()).suggests(autismclient.commands.CommandSuggestions::set).executes(ctx -> {
         String mode = StringArgumentType.getString(ctx, "mode").toLowerCase(Locale.ROOT);
         FakeGamemodeManager.Inner_DataUtil result;
         if (!"reset".equals(mode) && !"r".equals(mode)) {
            GameType resolved = FakeGamemodeManager.init(mode);
            if (resolved == null) {
               AutismClientMessaging.sendPrefixed("§cUnknown mode: §f" + mode);
               return 1;
            }

            result = FakeGamemodeManager.applyFake(resolved);
         } else {
            result = FakeGamemodeManager.clearFake();
         }

         AutismClientMessaging.sendPrefixed((result.success() ? "§a" : "§c") + result.message());
         return 1;
      }));
   }
}
