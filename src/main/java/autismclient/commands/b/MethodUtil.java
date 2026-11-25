package autismclient.commands.b;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.modules.al;
import autismclient.util.AutismClientMessaging;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;

public class MethodUtil extends Command {
   public MethodUtil() {
      super("modules", "List installed modules.", "features");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         List<String> names = al.init_List();
         AutismClientMessaging.sendPrefixed("§e" + names.size() + " modules:");
         StringBuilder line = new StringBuilder();

         for (String n : names) {
            if (line.length() > 0) {
               line.append("§7, ");
            }

            line.append("§f").append(n);
            if (line.length() > 200) {
               AutismClientMessaging.sendPrefixed(line.toString());
               line.setLength(0);
            }
         }

         if (line.length() > 0) {
            AutismClientMessaging.sendPrefixed(line.toString());
         }

         return 1;
      });
   }
}
