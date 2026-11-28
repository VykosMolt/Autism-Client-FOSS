package autismclient.util;

import autismclient.util.macro.MacroExecutor;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractCommandBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.screens.inventory.StructureBlockEditScreen;
import net.minecraft.client.input.KeyEvent;

public final class GuiMovementPassThrough {
   private static final Minecraft MINECRAFT = Minecraft.getInstance();
   private static final Set<KeyMapping> heldKeys = Collections.newSetFromMap(new IdentityHashMap<>());

   private GuiMovementPassThrough() {
   }

   public static boolean init(KeyEvent input, boolean pressed) {
      if (input == null) {
         return false;
      } else if (autismclient.modules.AutoTotemModule.init_Z_6()) {
         releaseAll();
         return false;
      } else {
         autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
         if (module == null || !module.isActive() || !module.isInventoryMoveEnabled()) {
            releaseAll();
            return false;
         } else if (MacroExecutor.isHoldingMovementInput()) {
            releaseAll();
            return false;
         } else if (!isPassThroughScreen()) {
            releaseAll();
            return false;
         } else if (OverlayManager.getInstance().hasFocusedTextInput()) {
            releaseAll();
            return false;
         } else if (MINECRAFT != null && MINECRAFT.options != null) {
            boolean handled = false;
            handled |= passKey(MINECRAFT.options.keyUp, input, pressed);
            handled |= passKey(MINECRAFT.options.keyDown, input, pressed);
            handled |= passKey(MINECRAFT.options.keyLeft, input, pressed);
            handled |= passKey(MINECRAFT.options.keyRight, input, pressed);
            handled |= passKey(MINECRAFT.options.keyJump, input, pressed);
            handled |= passKey(MINECRAFT.options.keyShift, input, pressed);
            return handled | passKey(MINECRAFT.options.keySprint, input, pressed);
         } else {
            releaseAll();
            return false;
         }
      }
   }

   public static void tick() {
      if (shouldPassThrough()) {
         syncKey(MINECRAFT.options.keyUp);
         syncKey(MINECRAFT.options.keyDown);
         syncKey(MINECRAFT.options.keyLeft);
         syncKey(MINECRAFT.options.keyRight);
         syncKey(MINECRAFT.options.keyJump);
         syncKey(MINECRAFT.options.keyShift);
         syncKey(MINECRAFT.options.keySprint);
      }
   }

   public static void releaseAll() {
      if (MINECRAFT != null && MINECRAFT.options != null) {
         release(MINECRAFT.options.keyUp);
         release(MINECRAFT.options.keyDown);
         release(MINECRAFT.options.keyLeft);
         release(MINECRAFT.options.keyRight);
         release(MINECRAFT.options.keyJump);
         release(MINECRAFT.options.keyShift);
         release(MINECRAFT.options.keySprint);
      }
   }

   public static void read() {
      if (MINECRAFT != null && MINECRAFT.options != null) {
         if (autismclient.modules.AutoTotemModule.init_Z_6()) {
            releaseAll();
         } else if (OverlayManager.getInstance().hasFocusedTextInput()
            || MINECRAFT.gui.screen() instanceof ChatScreen
            || MINECRAFT.gui.screen() instanceof AbstractSignEditScreen
            || MINECRAFT.gui.screen() instanceof BookEditScreen
            || MINECRAFT.gui.screen() != null && !isPassThroughScreen()) {
            releaseAll();
         } else {
            syncKey(MINECRAFT.options.keyUp);
            syncKey(MINECRAFT.options.keyDown);
            syncKey(MINECRAFT.options.keyLeft);
            syncKey(MINECRAFT.options.keyRight);
            syncKey(MINECRAFT.options.keyJump);
            syncKey(MINECRAFT.options.keyShift);
            syncKey(MINECRAFT.options.keySprint);
         }
      }
   }

   private static boolean passKey(KeyMapping binding, KeyEvent input, boolean pressed) {
      if (binding == null || !binding.matches(input)) {
         return false;
      }

      if (autismclient.modules.FreecamController.shouldSuppressKey(binding)) {
         return true;
      }

      binding.setDown(pressed);
      if (pressed) {
         heldKeys.add(binding);
      } else {
         heldKeys.remove(binding);
      }

      return true;
   }

   private static void syncKey(KeyMapping binding) {
      if (binding != null && !autismclient.modules.FreecamController.shouldSuppressKey(binding)) {
         boolean physical = KeyMappingControl.of(binding).autism$isActuallyDown();
         boolean pressed = physical || binding == MINECRAFT.options.keyShift && autismclient.modules.al.tick_Z();
         binding.setDown(pressed);
         if (physical) {
            heldKeys.add(binding);
         } else {
            heldKeys.remove(binding);
         }
      }
   }

   private static void release(KeyMapping binding) {
      if (binding != null && heldKeys.remove(binding)) {
         binding.setDown(false);
      }
   }

   private static boolean isPassThroughScreen() {
      if (MINECRAFT != null && MINECRAFT.gui.screen() != null) {
         Screen screen = MINECRAFT.gui.screen();
         if (!(screen instanceof AbstractContainerScreen)) {
            return false;
         } else if (screen instanceof AnvilScreen) {
            return false;
         } else if (screen instanceof AbstractCommandBlockEditScreen) {
            return false;
         } else {
            return screen instanceof StructureBlockEditScreen ? false : !(screen.getFocused() instanceof EditBox);
         }
      } else {
         return false;
      }
   }

   private static boolean shouldPassThrough() {
      if (autismclient.modules.AutoTotemModule.init_Z_6()) {
         releaseAll();
         return false;
      }

      autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
      if (module == null || !module.isActive() || !module.isInventoryMoveEnabled()) {
         releaseAll();
         return false;
      }

      if (MacroExecutor.isHoldingMovementInput()) {
         releaseAll();
         return false;
      }

      if (!isPassThroughScreen()) {
         releaseAll();
         return false;
      }

      if (OverlayManager.getInstance().hasFocusedTextInput()) {
         releaseAll();
         return false;
      }

      if (MINECRAFT != null && MINECRAFT.options != null) {
         return true;
      }

      releaseAll();
      return false;
   }
}
