package autismclient.commands.impl;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

public class SayCommand extends Command {
   public SayCommand() {
      super("say", "Send a chat message.");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         AutismClientMessaging.sendPrefixed("§eUsage: " + autismclient.commands.CommandManager.getPrefix() + "say <message>");
         return 1;
      });
      root.then(RequiredArgumentBuilder.<AutismCommandSource, String>argument("message", StringArgumentType.greedyString()).executes(ctx -> {
         String msg = StringArgumentType.getString(ctx, "message");
         ClientPacketListener conn = Minecraft.getInstance().getConnection();
         if (conn == null) {
            AutismClientMessaging.sendPrefixed("§cNot connected.");
            return 1;
         }

         if (msg.startsWith("/")) {
            conn.sendCommand(msg.substring(1));
         } else {
            autismclient.commands.CommandManager.sendChatRaw(conn, msg);
         }

         return 1;
      }));
   }
}
