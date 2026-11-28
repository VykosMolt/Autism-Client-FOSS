package autismclient.commands.arguments;

import autismclient.commands.AutismCommandSource;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import net.minecraft.network.chat.Component;

public class XCarrySlotArgumentType implements ArgumentType<Integer> {
   public static final int init_I = -2147483648;
   private static final Map<String, Integer> init_Map = new LinkedHashMap<>();
   private static final SimpleCommandExceptionType init_SimpleCommandExcep = new SimpleCommandExceptionType(
      Component.literal("Unknown XCarry slot — use craft1..craft5 / helmet / chestplate / leggings / boots / offhand / cursor.")
   );

   public static XCarrySlotArgumentType create() {
      return new XCarrySlotArgumentType();
   }

   public static int getValue(CommandContext<AutismCommandSource> ctx, String name) {
      return ctx.getArgument(name, Integer.class);
   }

   public static String slotName(int slotId) {
      for (Entry<String, Integer> e : init_Map.entrySet()) {
         if (e.getValue() == slotId) {
            return e.getKey();
         }
      }

      return "slot " + slotId;
   }

   public Integer parse(StringReader reader) throws CommandSyntaxException {
      return this.init_Integer_StringReader(reader);
   }

   public Integer init_Integer_StringReader(StringReader reader) throws CommandSyntaxException {
      String raw = reader.readUnquotedString().toLowerCase(Locale.ROOT);
      Integer id = init_Map.get(raw);
      if (id == null) {
         throw init_SimpleCommandExcep.create();
      } else {
         return id;
      }
   }

   @Override
   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

      for (String name : init_Map.keySet()) {
         if (name.startsWith(remaining)) {
            builder.suggest(name);
         }
      }

      return builder.buildFuture();
   }

   static {
      init_Map.put("craft1", 1);
      init_Map.put("craft2", 2);
      init_Map.put("craft3", 3);
      init_Map.put("craft4", 4);
      init_Map.put("craft5", 0);
      init_Map.put("crafting5", 0);
      init_Map.put("result", 0);
      init_Map.put("helmet", 5);
      init_Map.put("chestplate", 6);
      init_Map.put("leggings", 7);
      init_Map.put("boots", 8);
      init_Map.put("offhand", 45);
      init_Map.put("cursor", -2147483648);
   }
}
