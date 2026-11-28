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
   private static volatile List<PacketNameArgumentType.NameEntry> cachedHits;

   public static PacketNameArgumentType packetName() {
      return new PacketNameArgumentType();
   }

   public static String getValue(CommandContext<AutismCommandSource> ctx, String name) {
      return ctx.getArgument(name, String.class);
   }

   public String parse(StringReader reader) throws CommandSyntaxException {
      return this.readName(reader);
   }

   public String readName(StringReader reader) {
      return reader.readUnquotedString();
   }

   @Override
   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

      for (PacketNameArgumentType.NameEntry candidate : namesForSuggestions()) {
         if (candidate.load().contains(remaining)) {
            builder.suggest(candidate.init());
         }
      }

      return builder.buildFuture();
   }

   private static List<PacketNameArgumentType.NameEntry> namesForSuggestions() {
      List<PacketNameArgumentType.NameEntry> hit = cachedHits;
      if (hit != null) {
         return hit;
      }

      List<PacketNameArgumentType.NameEntry> rebuilt = new ArrayList<>();

      for (Class<? extends Packet<?>> cls : PacketTypeRegistry.getC2sPackets()) {
         String name = PacketTypeRegistry.getPacketName(cls);
         if (name != null) {
            rebuilt.add(new PacketNameArgumentType.NameEntry(name, name.toLowerCase(Locale.ROOT)));
         }
      }

      hit = List.copyOf(rebuilt);
      cachedHits = hit;
      return hit;
   }

   private record NameEntry(String init, String load) {
   }
}
