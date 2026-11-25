package autismclient.commands.b;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import autismclient.util.bm;
import autismclient.util.bw;
import autismclient.util.ck;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;

public class SendPacketCommand extends Command {
   public SendPacketCommand() {
      super("send", "Send a raw C2S packet by class name with optional field=value arguments.");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         AutismClientMessaging.sendPrefixed("§eUsage: §f" + autismclient.commands.DataUtil.init_Str() + "send <packetName> [field=value ...]");
         AutismClientMessaging.sendPrefixed("§7Tip: press Tab after the space to search C2S packet classes.");
         AutismClientMessaging.sendPrefixed("§7Example: §f" + autismclient.commands.DataUtil.init_Str() + "send ServerboundSetCarriedItemPacket slot=0");
         return 1;
      });
      root.then(
         ((RequiredArgumentBuilder)RequiredArgumentBuilder.<AutismCommandSource, String>argument("packet", autismclient.commands.a.JsonUtil.init_JsonUtil())
               .executes(ctx -> this.init_I_Str_Str_3(autismclient.commands.a.JsonUtil.init(ctx, "packet"), "")))
            .then(RequiredArgumentBuilder.<AutismCommandSource, String>argument("args", StringArgumentType.greedyString()).suggests((ctx, builder) -> {
               Class<? extends Packet<?>> cls = bw.init_Class_Str(autismclient.commands.a.JsonUtil.init(ctx, "packet"));
               return bm.init_CompletableFuture_Class_SuggestionsBuilder(cls, builder);
            }).executes(ctx -> this.init_I_Str_Str_3(autismclient.commands.a.JsonUtil.init(ctx, "packet"), StringArgumentType.getString(ctx, "args"))))
      );
   }

   private int init_I_Str_Str_3(String name, String args) {
      Class<? extends Packet<?>> cls = bw.init_Class_Str(name);
      if (cls == null) {
         AutismClientMessaging.sendPrefixed("§cUnknown packet: §f" + name);
         return 1;
      }

      if (!bw.load().contains(cls)) {
         AutismClientMessaging.sendPrefixed("§cRefusing to send non-C2S packet: §f" + name);
         return 1;
      }

      bm.Inner_DataUtil prepared;
      try {
         prepared = bm.init_bm_DataUtil_Str(args);
      } catch (IllegalArgumentException e) {
         AutismClientMessaging.sendPrefixed("§cBad arguments: " + e.getMessage());
         return 1;
      }

      bm.Inner_StreamUtil result = bm.init_bm_StreamUtil_Class_Str(cls, prepared.init_Str());
      if (result.load_Z()) {
         init_V_Str_Str("§e", result.init_Str());
         return 1;
      } else if (!result.init_Z()) {
         init_V_Str_Str("§c", "Failed to build §f" + name + "§c: " + result.init_Str());
         return 1;
      } else if (prepared.init_Z()) {
         AutismClientMessaging.sendPrefixed("§eBuilt §f" + name + "§7 (" + result.load_Str() + ") §e- dry run, not sent.");
         return 1;
      } else {
         ClientPacketListener conn = Minecraft.getInstance().getConnection();
         if (conn == null) {
            AutismClientMessaging.sendPrefixed("§cNo network connection.");
            return 1;
         } else {
            ck.init_ck().init_V_ClientPacketListen_Packet(conn, result.init_Packet());
            AutismClientMessaging.sendPrefixed("§aSent §f" + name + "§7 (" + result.load_Str() + ")");
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
