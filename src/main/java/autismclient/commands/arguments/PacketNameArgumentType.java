package autismclient.commands.arguments;

import autismclient.commands.AutismCommandSource;
import autismclient.util.PacketTypeRegistry;
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
import net.minecraft.network.protocol.Packet;

public class PacketNameArgumentType implements ArgumentType<String> {
   private static volatile List<PacketNameArgumentType.Inner_DataUtil> init;

   public static PacketNameArgumentType init_JsonUtil() {
      return new PacketNameArgumentType();
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

      for (PacketNameArgumentType.Inner_DataUtil candidate : init_List()) {
         if (candidate.load().contains(remaining)) {
            builder.suggest(candidate.init());
         }
      }

      return builder.buildFuture();
   }

   private static List<PacketNameArgumentType.Inner_DataUtil> init_List() {
      List<PacketNameArgumentType.Inner_DataUtil> hit = init;
      if (hit != null) {
         return hit;
      }

      List<PacketNameArgumentType.Inner_DataUtil> rebuilt = new ArrayList<>();

      for (Class<? extends Packet<?>> cls : PacketTypeRegistry.load()) {
         String name = PacketTypeRegistry.init_Str_Class(cls);
         if (name != null) {
            rebuilt.add(new PacketNameArgumentType.Inner_DataUtil(name, name.toLowerCase(Locale.ROOT)));
         }
      }

      hit = List.copyOf(rebuilt);
      init = hit;
      return hit;
   }

   private record Inner_DataUtil(String init, String load) {
   }
}
