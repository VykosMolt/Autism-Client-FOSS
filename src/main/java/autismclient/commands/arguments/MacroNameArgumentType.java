package autismclient.commands.arguments;

import autismclient.commands.AutismCommandSource;
import autismclient.util.AutismMacro;
import autismclient.util.MacroLibraryManager;
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

public class MacroNameArgumentType implements ArgumentType<String> {
   private static volatile long cachedRevision = -9223372036854775808L;
   private static volatile List<MacroNameArgumentType.NameEntry> cachedNames = List.of();

   public static MacroNameArgumentType macroName() {
      return new MacroNameArgumentType();
   }

   public static String getValue(CommandContext<AutismCommandSource> ctx, String name) {
      return ctx.getArgument(name, String.class);
   }

   public String parse(StringReader reader) throws CommandSyntaxException {
      return this.readName(reader);
   }

   public String readName(StringReader reader) throws CommandSyntaxException {
      return reader.canRead() && reader.peek() == '"' ? reader.readQuotedString() : reader.readUnquotedString();
   }

   @Override
   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

      for (MacroNameArgumentType.NameEntry candidate : namesForSuggestions()) {
         if (candidate.load().startsWith(remaining)) {
            builder.suggest(candidate.init());
         }
      }

      return builder.buildFuture();
   }

   private static List<MacroNameArgumentType.NameEntry> namesForSuggestions() {
      MacroLibraryManager manager = MacroLibraryManager.getInstance();
      long revision = manager.getRevision();
      List<MacroNameArgumentType.NameEntry> hit = cachedNames;
      if (cachedRevision == revision) {
         return hit;
      }

      List<MacroNameArgumentType.NameEntry> rebuilt = new ArrayList<>();

      for (AutismMacro macro : manager.getMacros()) {
         if (macro != null && macro.name != null) {
            String name = macro.name;
            String value = name.contains(" ") ? "\"" + name + "\"" : name;
            rebuilt.add(new MacroNameArgumentType.NameEntry(value, name.toLowerCase(Locale.ROOT)));
         }
      }

      hit = List.copyOf(rebuilt);
      cachedNames = hit;
      cachedRevision = revision;
      return hit;
   }

   private record NameEntry(String init, String load) {
   }
}
