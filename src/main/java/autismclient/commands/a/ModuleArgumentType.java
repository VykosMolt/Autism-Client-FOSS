package autismclient.commands.a;

import autismclient.commands.AutismCommandSource;
import autismclient.modules.al;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class ModuleArgumentType implements ArgumentType<String> {
   private static volatile int init_I = -2147483648;
   private static volatile List<ModuleArgumentType.Inner_DataUtil> init_List = List.of();

   public static ModuleArgumentType init_ConfigStore() {
      return new ModuleArgumentType();
   }

   public static String init(CommandContext<AutismCommandSource> ctx, String name) {
      return ctx.getArgument(name, String.class);
   }

   public String parse(StringReader reader) throws CommandSyntaxException {
      return this.init(reader);
   }

   public String init(StringReader reader) {
      return reader.readUnquotedString();
   }

   @Override
   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

      for (ModuleArgumentType.Inner_DataUtil candidate : init_List()) {
         if (candidate.load().startsWith(remaining)) {
            builder.suggest(candidate.init());
         }
      }

      if ("all".startsWith(remaining)) {
         builder.suggest("all");
      }

      if ("hud".startsWith(remaining)) {
         builder.suggest("hud");
      }

      return builder.buildFuture();
   }

   private static List<ModuleArgumentType.Inner_DataUtil> init_List() {
      int revision = al.init_I();
      List<ModuleArgumentType.Inner_DataUtil> hit = init_List;
      if (init_I == revision) {
         return hit;
      }

      List<ModuleArgumentType.Inner_DataUtil> rebuilt = new ArrayList<>();

      for (String name : al.init_List()) {
         if (name != null) {
            String commandName = name.replace(' ', '-');
            rebuilt.add(new ModuleArgumentType.Inner_DataUtil(commandName, commandName.toLowerCase(Locale.ROOT)));
         }
      }

      hit = List.copyOf(rebuilt);
      init_List = hit;
      init_I = revision;
      return hit;
   }

   private record Inner_DataUtil(String init, String load) {
   }
}
