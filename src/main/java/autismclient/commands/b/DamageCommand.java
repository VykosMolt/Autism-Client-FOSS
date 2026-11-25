package autismclient.commands.b;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

public class DamageCommand extends Command {
   public DamageCommand() {
      super("damage", "Take damage to self (vanilla server allows via /damage if op).");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         AutismClientMessaging.sendPrefixed("§eUsage: " + autismclient.commands.DataUtil.init_Str() + "damage <amount>");
         return 1;
      });
      root.then(
         RequiredArgumentBuilder.<AutismCommandSource, Double>argument("amount", DoubleArgumentType.doubleArg(0.1, 1024.0))
            .suggests(autismclient.commands.StreamUtil::update)
            .executes(ctx -> {
               double amt = DoubleArgumentType.getDouble(ctx, "amount");
               ClientPacketListener conn = Minecraft.getInstance().getConnection();
               if (conn == null) {
                  AutismClientMessaging.sendPrefixed("§cNot connected.");
                  return 1;
               } else {
                  conn.sendCommand("damage @s " + amt);
                  return 1;
               }
            })
      );
   }
}
