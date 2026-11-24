package autismclient.commands.a;

import autismclient.commands.AutismCommandSource;
import autismclient.util.g.FieldCache;
import autismclient.util.g.TaskTimer;
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

public class NetHelper implements ArgumentType<String> {
   private static volatile long init_J = -9223372036854775808L;
   private static volatile List<NetHelper.Inner_DataUtil> init_List = List.of();

   public static NetHelper init_NetHelper() {
      return new NetHelper();
   }

   public static String init(CommandContext<AutismCommandSource> ctx, String name) {
      return ctx.getArgument(name, String.class);
   }

   public String parse(StringReader reader) throws CommandSyntaxException {
      return this.init(reader);
   }

   public String init(StringReader reader) throws CommandSyntaxException {
      return reader.canRead() && reader.peek() == '"' ? reader.readQuotedString() : reader.readUnquotedString();
   }

   @Override
   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

      for (NetHelper.Inner_DataUtil candidate : init_List()) {
         if (candidate.load().startsWith(remaining)) {
            builder.suggest(candidate.init());
         }
      }

      return builder.buildFuture();
   }

   private static List<NetHelper.Inner_DataUtil> init_List() {
      TaskTimer manager = TaskTimer.init_TaskTimer();
      long revision = manager.init_J_2();
      List<NetHelper.Inner_DataUtil> hit = init_List;
      if (init_J == revision) {
         return hit;
      }

      List<NetHelper.Inner_DataUtil> rebuilt = new ArrayList<>();

      for (FieldCache profile : manager) {
         if (profile != null && profile.write_Str != null) {
            String name = profile.write_Str;
            String value = name.contains(" ") ? "\"" + name + "\"" : name;
            rebuilt.add(new NetHelper.Inner_DataUtil(value, name.toLowerCase(Locale.ROOT)));
         }
      }

      hit = List.copyOf(rebuilt);
      init_List = hit;
      init_J = revision;
      return hit;
   }

   private record Inner_DataUtil(String init, String load) {
   }
}
