package autismclient.util.macro;

import java.util.Locale;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.dialog.DialogScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.screens.inventory.BookSignScreen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.HangingSignEditScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;

public final class GuiScreenMatcher {
   private GuiScreenMatcher() {
   }

   public static boolean matchesTitle(Screen screen, String query) {
      if (screen != null && !isClientScreen(screen)) {
         String trimmed = query == null ? "" : query.trim();
         if (!trimmed.isEmpty() && !trimmed.equalsIgnoreCase("any")) {
            GuiScreenMatcher.GuiType type = parseGuiType(trimmed);
            if (type != null && screenMatches(screen, type)) {
               return true;
            }

            String title = screen.getTitle() == null ? "" : screen.getTitle().getString();
            String semantic = screenTypeName(screen);
            String lower = trimmed.toLowerCase(Locale.ROOT);
            return title.toLowerCase(Locale.ROOT).contains(lower) || semantic.toLowerCase(Locale.ROOT).contains(lower);
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public static boolean matches(Screen screen, String guiType, String titleFilter) {
      GuiScreenMatcher.GuiType type = parseGuiType(guiType);
      if (type == null) {
         type = GuiScreenMatcher.GuiType.ANY;
      }

      return !screenMatches(screen, type) ? false : titleFilter == null || titleFilter.isBlank() || matchesTitle(screen, titleFilter);
   }

   public static boolean screenMatches(Screen screen, GuiScreenMatcher.GuiType type) {
      if (screen != null && !isClientScreen(screen)) {
         boolean var10000;
         switch (type == null ? GuiScreenMatcher.GuiType.ANY : type) {
            case ANY:
               var10000 = true;
               break;
            case CONTAINER:
               var10000 = screen instanceof AbstractContainerScreen && !(screen instanceof InventoryScreen) && !(screen instanceof CreativeModeInventoryScreen);
               break;
            case INVENTORY:
               var10000 = screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen;
               break;
            case SIGN:
               var10000 = screen instanceof AbstractSignEditScreen;
               break;
            case HANGING_SIGN:
               var10000 = screen instanceof HangingSignEditScreen;
               break;
            case BOOK:
               var10000 = screen instanceof BookEditScreen || screen instanceof BookSignScreen || screen instanceof BookViewScreen;
               break;
            case BOOK_EDIT:
               var10000 = screen instanceof BookEditScreen;
               break;
            case BOOK_SIGN:
               var10000 = screen instanceof BookSignScreen;
               break;
            case BOOK_VIEW:
               var10000 = screen instanceof BookViewScreen;
               break;
            case CHAT:
               var10000 = screen instanceof ChatScreen;
               break;
            case CUSTOM_MENU:
               var10000 = screen instanceof DialogScreen;
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      } else {
         return false;
      }
   }

   public static String screenTypeName(Screen screen) {
      if (screen == null) {
         return "";
      } else if (screen instanceof DialogScreen) {
         return "CustomScreen";
      } else if (screen instanceof HangingSignEditScreen) {
         return "Hanging Sign";
      } else if (screen instanceof SignEditScreen || screen instanceof AbstractSignEditScreen) {
         return "Sign";
      } else if (screen instanceof BookSignScreen) {
         return "Book Sign";
      } else if (screen instanceof BookEditScreen) {
         return "Book Edit";
      } else if (screen instanceof BookViewScreen) {
         return "Book View";
      } else if (screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen) {
         return "Inventory";
      } else if (screen instanceof AbstractContainerScreen) {
         return "Container";
      } else {
         return screen instanceof ChatScreen ? "Chat" : screen.getClass().getSimpleName();
      }
   }

   public static boolean isClientScreen(Screen screen) {
      return screen != null && screen.getClass().getName().startsWith("autismclient.");
   }

   private static GuiScreenMatcher.GuiType parseGuiType(String raw) {
      if (raw != null && !raw.isBlank()) {
         String key = raw.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
         GuiScreenMatcher.GuiType var10000;
         switch (key) {
            case "ANY":
               var10000 = GuiScreenMatcher.GuiType.ANY;
               break;
            case "CONTAINER":
            case "GUI":
            case "CHEST":
            case "SHULKER":
               var10000 = GuiScreenMatcher.GuiType.CONTAINER;
               break;
            case "INVENTORY":
            case "PLAYER_INVENTORY":
            case "PLAYER":
            case "INV":
               var10000 = GuiScreenMatcher.GuiType.INVENTORY;
               break;
            case "SIGN":
            case "SIGN_GUI":
               var10000 = GuiScreenMatcher.GuiType.SIGN;
               break;
            case "HANGING_SIGN":
            case "HANGINGSIGN":
               var10000 = GuiScreenMatcher.GuiType.HANGING_SIGN;
               break;
            case "BOOK":
               var10000 = GuiScreenMatcher.GuiType.BOOK;
               break;
            case "BOOK_EDIT":
            case "EDIT_BOOK":
            case "WRITABLE_BOOK":
               var10000 = GuiScreenMatcher.GuiType.BOOK_EDIT;
               break;
            case "BOOK_SIGN":
            case "SIGN_BOOK":
               var10000 = GuiScreenMatcher.GuiType.BOOK_SIGN;
               break;
            case "BOOK_VIEW":
            case "READ_BOOK":
               var10000 = GuiScreenMatcher.GuiType.BOOK_VIEW;
               break;
            case "CHAT":
               var10000 = GuiScreenMatcher.GuiType.CHAT;
               break;
            case "CUSTOM_MENU":
            case "CUSTOM":
            case "CUSTOM_SCREEN":
            case "CUSTOMSCREEN":
            case "DIALOG":
               var10000 = GuiScreenMatcher.GuiType.CUSTOM_MENU;
               break;
            default:
               var10000 = null;
         }

         return var10000;
      } else {
         return null;
      }
   }

   public static boolean isCustomMenuType(String raw) {
      return parseGuiType(raw) == GuiScreenMatcher.GuiType.CUSTOM_MENU;
   }

   public enum GuiType {
      ANY,
      CONTAINER,
      INVENTORY,
      SIGN,
      HANGING_SIGN,
      BOOK,
      BOOK_EDIT,
      BOOK_SIGN,
      BOOK_VIEW,
      CHAT,
      CUSTOM_MENU;
   }
}
