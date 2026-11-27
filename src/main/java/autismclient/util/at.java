package autismclient.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.screens.inventory.BookSignScreen;

public final class at {
   private static final Minecraft init = Minecraft.getInstance();

   private at() {
   }

   public static boolean init() {
      if (!AutismLiteVariant.enabled() && autismclient.ai.aP.load_Z()) {
         return false;
      } else {
         AutismConfig config = AutismConfig.getGlobal();
         if (init == null) {
            return false;
         } else if (autismclient.gui.ui.widgets.TextInputWidget.hasAnyFocused()) {
            return false;
         } else if (init.gui.screen() == null) {
            return true;
         } else if (config == null || !config.keybindInsideGui) {
            return false;
         } else if (init.gui.screen() instanceof ChatScreen || init.gui.screen() instanceof InBedChatScreen) {
            return false;
         } else if (init.gui.screen() instanceof AbstractSignEditScreen) {
            return false;
         } else if (init.gui.screen() instanceof BookEditScreen || init.gui.screen() instanceof BookSignScreen) {
            return false;
         } else if (init(init.gui.screen().getFocused())) {
            return false;
         } else if (init(init.gui.screen(), 0)) {
            return false;
         } else if (OverlayManager.getInstance().hasFocusedTextInput()) {
            return false;
         } else {
            return !AutismLiteVariant.enabled() && init.gui.screen() instanceof autismclient.gui.screens.AutismModulesScreen moduleScreen ? !moduleScreen.load_Z_2() : true;
         }
      }
   }

   private static boolean init(GuiEventListener listener, int depth) {
      if (listener == null || depth > 4) {
         return false;
      }

      if (!(listener instanceof EditBox) && !(listener instanceof MultiLineEditBox)) {
         if (listener instanceof ContainerEventHandler container) {
            for (GuiEventListener child : container.children()) {
               if (init(child, depth + 1)) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return init(listener);
      }
   }

   private static boolean init(GuiEventListener listener) {
      if (!(listener instanceof EditBox editBox)) {
         return listener instanceof MultiLineEditBox multiLine ? multiLine.isFocused() : false;
      } else {
         return editBox.isFocused() && editBox.isVisible();
      }
   }
}
