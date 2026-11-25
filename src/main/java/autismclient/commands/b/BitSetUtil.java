package autismclient.commands.b;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

public class BitSetUtil extends Command {
   public BitSetUtil() {
      super("irc", "Send a message to the Matchmaking lobby chat.");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         AutismClientMessaging.sendPrefixed("§eUsage: " + autismclient.commands.DataUtil.init_Str() + "irc <message>");
         return 1;
      });
      root.then(RequiredArgumentBuilder.<AutismCommandSource, String>argument("message", StringArgumentType.greedyString()).executes(ctx -> {
         String msg = StringArgumentType.getString(ctx, "message");
         autismclient.util.mm.BufferHelper mm = autismclient.util.mm.BufferHelper.init_BufferHelper();
         if (!mm.init_Z()) {
            AutismClientMessaging.sendPrefixed("§cNot in a lobby. Open Matchmaking and join or create one first.");
            return 1;
         } else {
            mm.load_V_Str(msg);
            return 1;
         }
      }));
   }
}
