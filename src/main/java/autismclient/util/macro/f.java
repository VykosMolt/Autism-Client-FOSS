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

public final class f {
   private f() {
   }

   public static boolean init(Screen screen, String query) {
      if (screen != null && !init_Z(screen)) {
         String trimmed = query == null ? "" : query.trim();
         if (!trimmed.isEmpty() && !trimmed.equalsIgnoreCase("any")) {
            f.Inner_DataUtil type = init_f_DataUtil(trimmed);
            if (type != null && init(screen, type)) {
               return true;
            }

            String title = screen.getTitle() == null ? "" : screen.getTitle().getString();
            String semantic = init_Str(screen);
            String lower = trimmed.toLowerCase(Locale.ROOT);
            return title.toLowerCase(Locale.ROOT).contains(lower) || semantic.toLowerCase(Locale.ROOT).contains(lower);
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public static boolean init(Screen screen, String guiType, String titleFilter) {
      f.Inner_DataUtil type = init_f_DataUtil(guiType);
      if (type == null) {
         type = f.Inner_DataUtil.init;
      }

      return !init(screen, type) ? false : titleFilter == null || titleFilter.isBlank() || init(screen, titleFilter);
   }

   public static boolean init(Screen screen, f.Inner_DataUtil type) {
      if (screen != null && !init_Z(screen)) {
         boolean var10000;
         switch (type == null ? f.Inner_DataUtil.init : type) {
            case init:
               var10000 = true;
               break;
            case load:
               var10000 = screen instanceof AbstractContainerScreen && !(screen instanceof InventoryScreen) && !(screen instanceof CreativeModeInventoryScreen);
               break;
            case read:
               var10000 = screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen;
               break;
            case write:
               var10000 = screen instanceof AbstractSignEditScreen;
               break;
            case update:
               var10000 = screen instanceof HangingSignEditScreen;
               break;
            case sync:
               var10000 = screen instanceof BookEditScreen || screen instanceof BookSignScreen || screen instanceof BookViewScreen;
               break;
            case flush:
               var10000 = screen instanceof BookEditScreen;
               break;
            case close:
               var10000 = screen instanceof BookSignScreen;
               break;
            case open:
               var10000 = screen instanceof BookViewScreen;
               break;
            case poll:
               var10000 = screen instanceof ChatScreen;
               break;
            case push:
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

   public static String init_Str(Screen screen) {
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

   public static boolean init_Z(Screen screen) {
      return screen != null && screen.getClass().getName().startsWith("autismclient.");
   }

   private static f.Inner_DataUtil init_f_DataUtil(String raw) {
      if (raw != null && !raw.isBlank()) {
         String key = raw.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
         f.Inner_DataUtil var10000;
         switch (key) {
            case "ANY":
               var10000 = f.Inner_DataUtil.init;
               break;
            case "CONTAINER":
            case "GUI":
            case "CHEST":
            case "SHULKER":
               var10000 = f.Inner_DataUtil.load;
               break;
            case "INVENTORY":
            case "PLAYER_INVENTORY":
            case "PLAYER":
            case "INV":
               var10000 = f.Inner_DataUtil.read;
               break;
            case "SIGN":
            case "SIGN_GUI":
               var10000 = f.Inner_DataUtil.write;
               break;
            case "HANGING_SIGN":
            case "HANGINGSIGN":
               var10000 = f.Inner_DataUtil.update;
               break;
            case "BOOK":
               var10000 = f.Inner_DataUtil.sync;
               break;
            case "BOOK_EDIT":
            case "EDIT_BOOK":
            case "WRITABLE_BOOK":
               var10000 = f.Inner_DataUtil.flush;
               break;
            case "BOOK_SIGN":
            case "SIGN_BOOK":
               var10000 = f.Inner_DataUtil.close;
               break;
            case "BOOK_VIEW":
            case "READ_BOOK":
               var10000 = f.Inner_DataUtil.open;
               break;
            case "CHAT":
               var10000 = f.Inner_DataUtil.poll;
               break;
            case "CUSTOM_MENU":
            case "CUSTOM":
            case "CUSTOM_SCREEN":
            case "CUSTOMSCREEN":
            case "DIALOG":
               var10000 = f.Inner_DataUtil.push;
               break;
            default:
               var10000 = null;
         }

         return var10000;
      } else {
         return null;
      }
   }

   public static boolean init_Z(String raw) {
      return init_f_DataUtil(raw) == f.Inner_DataUtil.push;
   }

   public enum Inner_DataUtil {
      init,
      load,
      read,
      write,
      update,
      sync,
      flush,
      close,
      open,
      poll,
      push;
   }
}
