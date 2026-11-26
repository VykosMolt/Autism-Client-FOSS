package autismclient.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.network.chat.Component;

public final class ItemOrBlockIdArgumentType implements ArgumentType<String> {
   private static final SimpleCommandExceptionType init = new SimpleCommandExceptionType(Component.literal("Expected an item or block id"));

   private ItemOrBlockIdArgumentType() {
   }

   public static ItemOrBlockIdArgumentType init() {
      return new ItemOrBlockIdArgumentType();
   }

   public static String init(CommandContext<?> context, String name) {
      return context.getArgument(name, String.class);
   }

   static boolean init_Z_C(char c) {
      return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_' || c == '-' || c == '.' || c == '+' || c == ':' || c == '/';
   }

   public String parse(StringReader reader) throws CommandSyntaxException {
      return this.init_Str_StringReader(reader);
   }

   public String init_Str_StringReader(StringReader reader) throws CommandSyntaxException {
      int start = reader.getCursor();

      while (reader.canRead() && init_Z_C(reader.peek())) {
         reader.skip();
      }

      if (reader.getCursor() == start) {
         throw init.createWithContext(reader);
      } else {
         return reader.getString().substring(start, reader.getCursor());
      }
   }
}
