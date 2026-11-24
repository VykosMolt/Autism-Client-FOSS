package autismclient.commands.b;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;

public class FieldCache extends Command {
   public FieldCache() {
      super("matchmaking", "Toggle the Matchmaking overlay.", "mm");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         Minecraft mc = Minecraft.getInstance();
         mc.execute(() -> {
            autismclient.modules.PoolEntry mod = autismclient.modules.PoolEntry.init_PoolEntry();
            if (mod != null) {
               mod.queue();
            }
         });
         return 1;
      });
   }
}
