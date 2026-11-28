package autismclient.commands.arguments;

import autismclient.commands.AutismCommandSource;
import autismclient.compat263.CompatGLFW;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import net.minecraft.network.chat.Component;

public class KeyBindArgumentType implements ArgumentType<Integer> {
   private static final Map<String, Integer> KEYS = new LinkedHashMap<>();
   private static final SimpleCommandExceptionType INVALID_KEY = new SimpleCommandExceptionType(Component.literal("Unknown key"));

   public static KeyBindArgumentType key() {
      return new KeyBindArgumentType();
   }

   public static int getKey(CommandContext<AutismCommandSource> ctx, String name) {
      return ctx.getArgument(name, Integer.class);
   }

   public static String keyName(int code) {
      if (code < 0) {
         return "NONE";
      }

      for (Entry<String, Integer> e : KEYS.entrySet()) {
         if (e.getValue() == code) {
            return e.getKey();
         }
      }

      return "KEY_" + code;
   }

   public Integer parse(StringReader reader) throws CommandSyntaxException {
      return this.parseKey(reader);
   }

   public Integer parseKey(StringReader reader) throws CommandSyntaxException {
      String raw = reader.readUnquotedString();
      String upper = raw.toUpperCase(Locale.ROOT);
      Integer code = KEYS.get(upper);
      if (code != null) {
         return code;
      }

      if (upper.length() == 1) {
         char c = upper.charAt(0);
         if (c >= 'A' && c <= 'Z') {
            return 65 + (c - 65);
         }

         if (c >= '0' && c <= '9') {
            return 48 + (c - 48);
         }
      }

      throw INVALID_KEY.create();
   }

   @Override
   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining().toUpperCase(Locale.ROOT);

      for (String n : KEYS.keySet()) {
         if (n.startsWith(remaining)) {
            builder.suggest(n);
         }
      }

      return builder.buildFuture();
   }

   static {
      for (Field f : CompatGLFW.class.getDeclaredFields()) {
         String n = f.getName();
         if (n.startsWith("GLFW_KEY_")) {
            try {
               int code = f.getInt(null);
               KEYS.put(n.substring("GLFW_KEY_".length()).toUpperCase(Locale.ROOT), code);
            } catch (Throwable var6) {
            }
         }
      }
   }
}
