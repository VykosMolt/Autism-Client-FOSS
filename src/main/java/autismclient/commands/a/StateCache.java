package autismclient.commands.a;

import autismclient.commands.AutismCommandSource;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public final class StateCache implements ArgumentType<String> {
   private static AbstractContainerMenu init_AbstractContainerM;
   private static int init_I = -2147483648;
   private static List<StateCache.Inner_DataUtil> init_List = List.of();

   private StateCache() {
   }

   public static StateCache init() {
      return new StateCache();
   }

   public static String init(CommandContext<AutismCommandSource> context, String name) {
      return context.getArgument(name, String.class);
   }

   public String parse(StringReader reader) throws CommandSyntaxException {
      return this.init_Str_StringReader(reader);
   }

   public String init_Str_StringReader(StringReader reader) throws CommandSyntaxException {
      return reader.readString();
   }

   @Override
   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
      if (remaining.startsWith("\"")) {
         remaining = remaining.substring(1);
      }

      Minecraft mc = Minecraft.getInstance();

      for (StateCache.Inner_DataUtil candidate : init_List_Minecraft(mc)) {
         if (candidate.load().startsWith(remaining)) {
            builder.suggest(StringArgumentType.escapeIfRequired(candidate.init()));
         }
      }

      return builder.buildFuture();
   }

   private static List<StateCache.Inner_DataUtil> init_List_Minecraft(Minecraft mc) {
      AbstractContainerMenu menu = mc.player == null ? null : mc.player.containerMenu;
      int revision = menu == null ? -1 : menu.getStateId();
      if (menu == init_AbstractContainerM && revision == init_I) {
         return init_List;
      }

      Set<String> names = new LinkedHashSet<>();
      if (menu != null) {
         for (int slot = 0; slot < menu.slots.size(); slot++) {
            ItemStack stack = menu.slots.get(slot).getItem();
            if (!stack.isEmpty()) {
               String displayName = init_Str_Str(stack.getHoverName().getString());
               if (!displayName.isEmpty()) {
                  names.add(displayName);
               }
            }
         }
      }

      List<StateCache.Inner_DataUtil> rebuilt = new ArrayList<>(names.size());

      for (String name : names) {
         rebuilt.add(new StateCache.Inner_DataUtil(name, name.toLowerCase(Locale.ROOT)));
      }

      init_AbstractContainerM = menu;
      init_I = revision;
      init_List = List.copyOf(rebuilt);
      return init_List;
   }

   private static String init_Str_Str(String input) {
      if (input != null && !input.isBlank()) {
         StringBuilder out = new StringBuilder(input.length());
         boolean pendingSpace = false;

         for (int index = 0; index < input.length(); index++) {
            char chr = input.charAt(index);
            if (Character.isWhitespace(chr)) {
               pendingSpace = out.length() > 0;
            } else {
               if (pendingSpace) {
                  out.append(' ');
               }

               out.append(chr);
               pendingSpace = false;
            }
         }

         return out.toString();
      } else {
         return "";
      }
   }

   private record Inner_DataUtil(String init, String load) {
   }
}
