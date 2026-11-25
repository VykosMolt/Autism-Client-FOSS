package autismclient.commands.b;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import autismclient.util.W;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.Locale;
import net.minecraft.world.level.GameType;

public class ByteReader extends Command {
   public ByteReader() {
      super("fakegm", "Set a fake client-side game mode.");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         AutismClientMessaging.sendPrefixed("§eUsage: " + autismclient.commands.DataUtil.init_Str() + "fakegm <survival|creative|adventure|spectator|reset>");
         AutismClientMessaging.sendPrefixed("§7Real change: " + autismclient.commands.DataUtil.init_Str() + "gamemode");
         return 1;
      });
      root.then(RequiredArgumentBuilder.<AutismCommandSource, String>argument("mode", StringArgumentType.word()).suggests(autismclient.commands.StreamUtil::set).executes(ctx -> {
         String mode = StringArgumentType.getString(ctx, "mode").toLowerCase(Locale.ROOT);
         W.Inner_DataUtil result;
         if (!"reset".equals(mode) && !"r".equals(mode)) {
            GameType resolved = W.init(mode);
            if (resolved == null) {
               AutismClientMessaging.sendPrefixed("§cUnknown mode: §f" + mode);
               return 1;
            }

            result = W.init_W_DataUtil(resolved);
         } else {
            result = W.init_W_DataUtil();
         }

         AutismClientMessaging.sendPrefixed((result.init_Z() ? "§a" : "§c") + result.init_Str());
         return 1;
      }));
   }
}
