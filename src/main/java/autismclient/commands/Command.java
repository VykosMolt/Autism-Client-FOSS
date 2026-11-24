package autismclient.commands;

import autismclient.util.AutismClientMessaging;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

public abstract class Command {
   public static final int SUCCESS = 1;
   private final String name;
   private final String description;
   private final String[] aliases;

   protected Command(String name, String description, String... aliases) {
      this.name = name;
      this.description = description == null ? "" : description;
      this.aliases = aliases == null ? new String[0] : (String[])aliases.clone();
   }

   public final String name() {
      return this.name;
   }

   public final String description() {
      return this.description;
   }

   public final String[] aliases() {
      return (String[])this.aliases.clone();
   }

   public abstract void build(LiteralArgumentBuilder<AutismCommandSource> var1);

   public int run(CommandContext<AutismCommandSource> ctx) {
      AutismClientMessaging.sendPrefixed("§eUsage: " + DataUtil.init_Str() + this.name + (this.aliases.length > 0 ? " (alias: " + this.aliases[0] + ")" : ""));
      if (!this.description.isEmpty()) {
         AutismClientMessaging.sendPrefixed("§7" + this.description);
      }

      return 1;
   }
}
