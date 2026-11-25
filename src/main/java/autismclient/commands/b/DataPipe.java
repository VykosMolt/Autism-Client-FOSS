package autismclient.commands.b;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import autismclient.util.aE;
import autismclient.util.aM;
import autismclient.util.aN;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

public final class DataPipe extends Command {
   public DataPipe() {
      super("sync", "Synchronize a chat message, server command, or macro across the LAN session.", "lan-sync", "lansync");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(context -> init());
      root.then(
         ((LiteralArgumentBuilder)LiteralArgumentBuilder.<AutismCommandSource>literal("send").executes(context -> {
               AutismClientMessaging.sendPrefixed("§eUsage: §f" + autismclient.commands.DataUtil.init_Str() + "sync send <message or /command>");
               return 1;
            }))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("message", StringArgumentType.greedyString())
                  .executes(context -> init(StringArgumentType.getString(context, "message")))
            )
      );
      root.then(
         ((LiteralArgumentBuilder)LiteralArgumentBuilder.<AutismCommandSource>literal("macro").executes(context -> {
               AutismClientMessaging.sendPrefixed("§eUsage: §f" + autismclient.commands.DataUtil.init_Str() + "sync macro <name>");
               return 1;
            }))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("name", autismclient.commands.a.CachePool.init_CachePool())
                  .executes(context -> load(autismclient.commands.a.CachePool.init(context, "name")))
            )
      );
   }

   private static int init() {
      String prefix = autismclient.commands.DataUtil.init_Str();
      AutismClientMessaging.sendPrefixed("§eUsage: §f" + prefix + "sync send <message or /command>");
      AutismClientMessaging.sendPrefixed("§eUsage: §f" + prefix + "sync macro <name>");
      return 1;
   }

   private static int init(String message) {
      aE sync = aE.init_aE();
      if (!sync.write_Z()) {
         AutismClientMessaging.sendPrefixed("§cJoin a LAN Sync session first.");
         return 1;
      } else {
         sync.init_Z_Str(message);
         return 1;
      }
   }

   private static int load(String name) {
      aE sync = aE.init_aE();
      if (!sync.write_Z()) {
         AutismClientMessaging.sendPrefixed("§cJoin a LAN Sync session first.");
         return 1;
      } else {
         aM macro = aN.init_aN().init_aM_Str(name);
         if (macro == null) {
            AutismClientMessaging.sendPrefixed("§cMacro not found: §f" + name);
            return 1;
         } else {
            sync.load_V(macro.init_Str);
            return 1;
         }
      }
   }
}
