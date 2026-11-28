package autismclient.commands.impl;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import autismclient.util.PacketCommandBuilder;
import autismclient.util.PacketTypeRegistry;
import autismclient.util.AutismSessionState;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;

public class SendCommand extends Command {
   public SendCommand() {
      super("send", "Send a raw C2S packet by class name with optional field=value arguments.");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         AutismClientMessaging.sendPrefixed("§eUsage: §f" + autismclient.commands.CommandManager.getPrefix() + "send <packetName> [field=value ...]");
         AutismClientMessaging.sendPrefixed("§7Tip: press Tab after the space to search C2S packet classes.");
         AutismClientMessaging.sendPrefixed("§7Example: §f" + autismclient.commands.CommandManager.getPrefix() + "send ServerboundSetCarriedItemPacket slot=0");
         return 1;
      });
      root.then(
         ((RequiredArgumentBuilder)RequiredArgumentBuilder.<AutismCommandSource, String>argument("packet", autismclient.commands.arguments.PacketNameArgumentType.packetName())
               .executes(ctx -> this.init_I_Str_Str_3(autismclient.commands.arguments.PacketNameArgumentType.init(ctx, "packet"), "")))
            .then(RequiredArgumentBuilder.<AutismCommandSource, String>argument("args", StringArgumentType.greedyString()).suggests((ctx, builder) -> {
               Class<? extends Packet<?>> cls = PacketTypeRegistry.resolvePacketClass(autismclient.commands.arguments.PacketNameArgumentType.init(ctx, "packet"));
               return PacketCommandBuilder.suggest(cls, builder);
            }).executes(ctx -> this.init_I_Str_Str_3(autismclient.commands.arguments.PacketNameArgumentType.init(ctx, "packet"), StringArgumentType.getString(ctx, "args"))))
      );
   }

   private int init_I_Str_Str_3(String name, String args) {
      Class<? extends Packet<?>> cls = PacketTypeRegistry.resolvePacketClass(name);
      if (cls == null) {
         AutismClientMessaging.sendPrefixed("§cUnknown packet: §f" + name);
         return 1;
      }

      if (!PacketTypeRegistry.getC2sPackets().contains(cls)) {
         AutismClientMessaging.sendPrefixed("§cRefusing to send non-C2S packet: §f" + name);
         return 1;
      }

      PacketCommandBuilder.PreparedArgs prepared;
      try {
         prepared = PacketCommandBuilder.prepareArgs(args);
      } catch (IllegalArgumentException e) {
         AutismClientMessaging.sendPrefixed("§cBad arguments: " + e.getMessage());
         return 1;
      }

      PacketCommandBuilder.BuildResult result = PacketCommandBuilder.build(cls, prepared.args());
      if (result.infoOnly()) {
         init_V_Str_Str("§e", result.message());
         return 1;
      } else if (!result.success()) {
         init_V_Str_Str("§c", "Failed to build §f" + name + "§c: " + result.message());
         return 1;
      } else if (prepared.dryRun()) {
         AutismClientMessaging.sendPrefixed("§eBuilt §f" + name + "§7 (" + result.source() + ") §e- dry run, not sent.");
         return 1;
      } else {
         ClientPacketListener conn = Minecraft.getInstance().getConnection();
         if (conn == null) {
            AutismClientMessaging.sendPrefixed("§cNo network connection.");
            return 1;
         } else {
            AutismSessionState.getInstance().sendPacketBypassingInterception(conn, result.packet());
            AutismClientMessaging.sendPrefixed("§aSent §f" + name + "§7 (" + result.source() + ")");
            return 1;
         }
      }
   }

   private static void init_V_Str_Str(String color, String message) {
      if (message != null && !message.isBlank()) {
         for (String line : message.split("\\R")) {
            if (!line.isBlank()) {
               AutismClientMessaging.sendPrefixed(color + line);
            }
         }
      }
   }
}
