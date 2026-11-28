package autismclient.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.network.chat.Component;

public final class ItemOrBlockIdArgumentType implements ArgumentType<String> {
   private static final SimpleCommandExceptionType INVALID = new SimpleCommandExceptionType(Component.literal("Expected an item or block id"));

   private ItemOrBlockIdArgumentType() {
   }

   public static ItemOrBlockIdArgumentType id() {
      return new ItemOrBlockIdArgumentType();
   }

   public static String getId(CommandContext<?> context, String name) {
      return context.getArgument(name, String.class);
   }

   static boolean isIdChar(char c) {
      return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_' || c == '-' || c == '.' || c == '+' || c == ':' || c == '/';
   }

   public String parse(StringReader reader) throws CommandSyntaxException {
      return this.readId(reader);
   }

   public String readId(StringReader reader) throws CommandSyntaxException {
      int start = reader.getCursor();

      while (reader.canRead() && isIdChar(reader.peek())) {
         reader.skip();
      }

      if (reader.getCursor() == start) {
         throw INVALID.createWithContext(reader);
      } else {
         return reader.getString().substring(start, reader.getCursor());
      }
   }
}
