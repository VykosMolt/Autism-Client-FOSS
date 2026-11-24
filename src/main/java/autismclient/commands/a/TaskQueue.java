package autismclient.commands.a;

import autismclient.commands.AutismCommandSource;
import autismclient.util.g.PacketUtil;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class TaskQueue implements ArgumentType<String> {
   public static TaskQueue init() {
      return new TaskQueue();
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

      for (PacketUtil.Inner_StreamUtil bot : PacketUtil.init_PacketUtil().read_List()) {
         String name = bot.load();
         if (name != null && !name.isBlank() && name.toLowerCase(Locale.ROOT).startsWith(remaining)) {
            builder.suggest(name.contains(" ") ? "\"" + name + "\"" : name);
         }
      }

      return builder.buildFuture();
   }
}
