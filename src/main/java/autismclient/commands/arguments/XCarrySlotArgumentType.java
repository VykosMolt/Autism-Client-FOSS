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
   private static final Map<String, Integer> SLOTS = new LinkedHashMap<>();
   private static final SimpleCommandExceptionType UNKNOWN_SLOT = new SimpleCommandExceptionType(
      Component.literal("Unknown XCarry slot — use craft1..craft5 / helmet / chestplate / leggings / boots / offhand / cursor.")
   );

   public static XCarrySlotArgumentType create() {
      return new XCarrySlotArgumentType();
   }

   public static int getValue(CommandContext<AutismCommandSource> ctx, String name) {
      return ctx.getArgument(name, Integer.class);
   }

   public static String slotName(int slotId) {
      for (Entry<String, Integer> e : SLOTS.entrySet()) {
         if (e.getValue() == slotId) {
            return e.getKey();
         }
      }

      return "slot " + slotId;
   }

   public Integer parse(StringReader reader) throws CommandSyntaxException {
      return this.parseToken(reader);
   }

   public Integer parseToken(StringReader reader) throws CommandSyntaxException {
      String raw = reader.readUnquotedString().toLowerCase(Locale.ROOT);
      Integer id = SLOTS.get(raw);
      if (id == null) {
         throw UNKNOWN_SLOT.create();
      } else {
         return id;
      }
   }

   @Override
   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

      for (String name : SLOTS.keySet()) {
         if (name.startsWith(remaining)) {
            builder.suggest(name);
         }
      }

      return builder.buildFuture();
   }

   static {
      SLOTS.put("craft1", 1);
      SLOTS.put("craft2", 2);
      SLOTS.put("craft3", 3);
      SLOTS.put("craft4", 4);
      SLOTS.put("craft5", 0);
      SLOTS.put("crafting5", 0);
      SLOTS.put("result", 0);
      SLOTS.put("helmet", 5);
      SLOTS.put("chestplate", 6);
      SLOTS.put("leggings", 7);
      SLOTS.put("boots", 8);
      SLOTS.put("offhand", 45);
      SLOTS.put("cursor", -2147483648);
   }
}
