package autismclient.commands.arguments;

import autismclient.commands.AutismCommandSource;
import autismclient.util.InventorySlotHelper;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class SlotArgumentType implements ArgumentType<Integer> {
   private static final SimpleCommandExceptionType UNKNOWN_SLOT = new SimpleCommandExceptionType(
      Component.literal("Unknown slot. Use gui1, hotbar1, inventory1, helmet, chestplate, leggings, boots, offhand, or a visible numeric slot id.")
   );
   private static AbstractContainerMenu cachedMenu;
   private static int cachedSlotCount = -1;
   private static List<String> cachedSuggestions = List.of();

   private SlotArgumentType() {
   }

   public static SlotArgumentType create() {
      return new SlotArgumentType();
   }

   public static int getValue(CommandContext<AutismCommandSource> context, String name) {
      return context.getArgument(name, Integer.class);
   }

   public Integer parse(StringReader reader) throws CommandSyntaxException {
      return this.parseToken(reader);
   }

   public Integer parseToken(StringReader reader) throws CommandSyntaxException {
      String token = reader.readUnquotedString().trim().toLowerCase(Locale.ROOT);
      if (token.isEmpty()) {
         throw UNKNOWN_SLOT.create();
      }

      Integer named = armorSlot(token);
      if (named != null) {
         return named;
      }

      try {
         int numeric = Integer.parseInt(token);
         if (numeric >= 0) {
            return numeric;
         }
      } catch (NumberFormatException var5) {
      }

      throw UNKNOWN_SLOT.create();
   }

   @Override
   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
      Minecraft mc = Minecraft.getInstance();

      for (String value : suggestionsFor(mc)) {
         if (value.toLowerCase(Locale.ROOT).startsWith(remaining)) {
            builder.suggest(value);
         }
      }

      return builder.buildFuture();
   }

   private static List<String> suggestionsFor(Minecraft mc) {
      AbstractContainerMenu menu = mc.player == null ? null : mc.player.containerMenu;
      int slotCount = menu == null ? 0 : menu.slots.size();
      if (menu == cachedMenu && slotCount == cachedSlotCount) {
         return cachedSuggestions;
      }

      Set<String> values = new LinkedHashSet<>();
      if (menu != null) {
         for (int handlerSlot = 0; handlerSlot < menu.slots.size(); handlerSlot++) {
            int visible = InventorySlotHelper.toVisibleSlot(mc, handlerSlot);
            values.add(slotName(visible));
            values.add(String.valueOf(visible));
         }
      } else {
         for (int slot = 1; slot <= 9; slot++) {
            values.add("hotbar" + slot);
         }
      }

      cachedMenu = menu;
      cachedSlotCount = slotCount;
      cachedSuggestions = List.copyOf(values);
      return cachedSuggestions;
   }

   public static String slotName(int visibleSlot) {
      if (visibleSlot >= 100) {
         return "gui" + (visibleSlot - 100 + 1);
      }

      if (visibleSlot >= 0 && visibleSlot <= 8) {
         return "hotbar" + (visibleSlot + 1);
      }

      if (visibleSlot >= 9 && visibleSlot <= 35) {
         return "inventory" + (visibleSlot - 8);
      }

      String var10000;
      switch (visibleSlot) {
         case 36:
            var10000 = "boots";
            break;
         case 37:
            var10000 = "leggings";
            break;
         case 38:
            var10000 = "chestplate";
            break;
         case 39:
            var10000 = "helmet";
            break;
         case 40:
            var10000 = "offhand";
            break;
         default:
            var10000 = String.valueOf(visibleSlot);
      }

      return var10000;
   }

   private static Integer armorSlot(String token) {
      Integer var10000;
      switch (token) {
         case "boots":
            var10000 = 36;
            break;
         case "leggings":
            var10000 = 37;
            break;
         case "chestplate":
            var10000 = 38;
            break;
         case "helmet":
            var10000 = 39;
            break;
         case "offhand":
            var10000 = 40;
            break;
         default:
            Integer gui = indexedSlot(token, "gui", 100, 2147483647);
            if (gui != null) {
               var10000 = gui;
            } else {
               Integer hotbar = indexedSlot(token, "hotbar", 0, 9);
               var10000 = hotbar != null ? hotbar : indexedSlot(token, "inventory", 9, 27);
            }
      }

      return var10000;
   }

   private static Integer indexedSlot(String token, String prefix, int offset, int maxIndex) {
      if (!token.startsWith(prefix)) {
         return null;
      }

      String suffix = token.substring(prefix.length());
      if (suffix.startsWith(":")) {
         suffix = suffix.substring(1);
      }

      try {
         int oneBased = Integer.parseInt(suffix);
         return oneBased >= 1 && oneBased <= maxIndex ? offset + oneBased - 1 : null;
      } catch (NumberFormatException ignored) {
         return null;
      }
   }
}
