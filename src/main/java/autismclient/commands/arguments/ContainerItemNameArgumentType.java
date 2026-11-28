package autismclient.commands.arguments;

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

public final class ContainerItemNameArgumentType implements ArgumentType<String> {
   private static AbstractContainerMenu cachedMenu;
   private static int cachedRevision = -2147483648;
   private static List<ContainerItemNameArgumentType.ItemCandidate> cachedItems = List.of();

   private ContainerItemNameArgumentType() {
   }

   public static ContainerItemNameArgumentType init() {
      return new ContainerItemNameArgumentType();
   }

   public static String init(CommandContext<AutismCommandSource> context, String name) {
      return context.getArgument(name, String.class);
   }

   public String parse(StringReader reader) throws CommandSyntaxException {
      return this.readName(reader);
   }

   public String readName(StringReader reader) throws CommandSyntaxException {
      return reader.readString();
   }

   @Override
   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
      if (remaining.startsWith("\"")) {
         remaining = remaining.substring(1);
      }

      Minecraft mc = Minecraft.getInstance();

      for (ContainerItemNameArgumentType.ItemCandidate candidate : containerItems(mc)) {
         if (candidate.load().startsWith(remaining)) {
            builder.suggest(StringArgumentType.escapeIfRequired(candidate.init()));
         }
      }

      return builder.buildFuture();
   }

   private static List<ContainerItemNameArgumentType.ItemCandidate> containerItems(Minecraft mc) {
      AbstractContainerMenu menu = mc.player == null ? null : mc.player.containerMenu;
      int revision = menu == null ? -1 : menu.getStateId();
      if (menu == cachedMenu && revision == cachedRevision) {
         return cachedItems;
      }

      Set<String> names = new LinkedHashSet<>();
      if (menu != null) {
         for (int slot = 0; slot < menu.slots.size(); slot++) {
            ItemStack stack = menu.slots.get(slot).getItem();
            if (!stack.isEmpty()) {
               String displayName = normalize(stack.getHoverName().getString());
               if (!displayName.isEmpty()) {
                  names.add(displayName);
               }
            }
         }
      }

      List<ContainerItemNameArgumentType.ItemCandidate> rebuilt = new ArrayList<>(names.size());

      for (String name : names) {
         rebuilt.add(new ContainerItemNameArgumentType.ItemCandidate(name, name.toLowerCase(Locale.ROOT)));
      }

      cachedMenu = menu;
      cachedRevision = revision;
      cachedItems = List.copyOf(rebuilt);
      return cachedItems;
   }

   private static String normalize(String input) {
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

   private record ItemCandidate(String init, String load) {
   }
}
