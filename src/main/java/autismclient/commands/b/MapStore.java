package autismclient.commands.b;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public class MapStore extends Command {
   public MapStore() {
      super("plugins", "Open the plugin scanner (alias of `server plugins`).");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         ValueSink.load();
         return 1;
      });
   }
}
