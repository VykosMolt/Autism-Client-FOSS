package autismclient.commands.b;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import autismclient.util.z;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

public class ArrayUtil extends Command {
   public ArrayUtil() {
      super("prefix", "Change the AUTISM command prefix.");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         AutismClientMessaging.sendPrefixed("Current prefix: " + z.init_Str());
         return 1;
      });
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("reset").executes(ctx -> {
         String prefix = z.load_Str();
         autismclient.modules.PoolEntry.init_PoolEntry().init_V_Str(prefix);
         AutismClientMessaging.sendPrefixed("Prefix reset to " + prefix);
         return 1;
      }));
      root.then(RequiredArgumentBuilder.<AutismCommandSource, String>argument("new", StringArgumentType.word()).suggests(autismclient.commands.StreamUtil::add).executes(ctx -> {
         String requested = StringArgumentType.getString(ctx, "new");
         if (!z.init_List.contains(requested)) {
            AutismClientMessaging.sendPrefixed("Prefix must be one of: . % - _ * # @ & =");
            return 1;
         }

         String selected = z.init_Str(requested);
         autismclient.modules.PoolEntry.init_PoolEntry().init_V_Str(selected);
         AutismClientMessaging.sendPrefixed("Prefix set to " + selected);
         return 1;
      }));
   }
}
