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

public final class ax {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final Set<KeyMapping> init_Set = Collections.newSetFromMap(new IdentityHashMap<>());

   private ax() {
   }

   public static boolean init(KeyEvent input, boolean pressed) {
      if (input == null) {
         return false;
      } else if (autismclient.modules.FieldCache.init_Z_6()) {
         load_V();
         return false;
      } else {
         autismclient.modules.PoolEntry module = autismclient.modules.PoolEntry.init_PoolEntry();
         if (module == null || !module.init_Z() || !module.run_Z()) {
            load_V();
            return false;
         } else if (MacroExecutor.read_Z()) {
            load_V();
            return false;
         } else if (!init_Z()) {
            load_V();
            return false;
         } else if (bk.init_bk().update_Z()) {
            load_V();
            return false;
         } else if (init_Minecraft != null && init_Minecraft.options != null) {
            boolean handled = false;
            handled |= init(init_Minecraft.options.keyUp, input, pressed);
            handled |= init(init_Minecraft.options.keyDown, input, pressed);
            handled |= init(init_Minecraft.options.keyLeft, input, pressed);
            handled |= init(init_Minecraft.options.keyRight, input, pressed);
            handled |= init(init_Minecraft.options.keyJump, input, pressed);
            handled |= init(init_Minecraft.options.keyShift, input, pressed);
            return handled | init(init_Minecraft.options.keySprint, input, pressed);
         } else {
            load_V();
            return false;
         }
      }
   }

   public static void init_V() {
      if (load_Z()) {
         init(init_Minecraft.options.keyUp);
         init(init_Minecraft.options.keyDown);
         init(init_Minecraft.options.keyLeft);
         init(init_Minecraft.options.keyRight);
         init(init_Minecraft.options.keyJump);
         init(init_Minecraft.options.keyShift);
         init(init_Minecraft.options.keySprint);
      }
   }

   public static void load_V() {
      if (init_Minecraft != null && init_Minecraft.options != null) {
         load(init_Minecraft.options.keyUp);
         load(init_Minecraft.options.keyDown);
         load(init_Minecraft.options.keyLeft);
         load(init_Minecraft.options.keyRight);
         load(init_Minecraft.options.keyJump);
         load(init_Minecraft.options.keyShift);
         load(init_Minecraft.options.keySprint);
      }
   }

   public static void read() {
      if (init_Minecraft != null && init_Minecraft.options != null) {
         if (autismclient.modules.FieldCache.init_Z_6()) {
            load_V();
         } else if (bk.init_bk().update_Z()
            || init_Minecraft.gui.screen() instanceof ChatScreen
            || init_Minecraft.gui.screen() instanceof AbstractSignEditScreen
            || init_Minecraft.gui.screen() instanceof BookEditScreen
            || init_Minecraft.gui.screen() != null && !init_Z()) {
            load_V();
         } else {
            init(init_Minecraft.options.keyUp);
            init(init_Minecraft.options.keyDown);
            init(init_Minecraft.options.keyLeft);
            init(init_Minecraft.options.keyRight);
            init(init_Minecraft.options.keyJump);
            init(init_Minecraft.options.keyShift);
            init(init_Minecraft.options.keySprint);
         }
      }
   }

   private static boolean init(KeyMapping binding, KeyEvent input, boolean pressed) {
      if (binding == null || !binding.matches(input)) {
         return false;
      }

      if (autismclient.modules.aC.init_Z_KeyMapping(binding)) {
         return true;
      }

      binding.setDown(pressed);
      if (pressed) {
         init_Set.add(binding);
      } else {
         init_Set.remove(binding);
      }

      return true;
   }

   private static void init(KeyMapping binding) {
      if (binding != null && !autismclient.modules.aC.init_Z_KeyMapping(binding)) {
         boolean physical = aB.init(binding).autism$isActuallyDown();
         boolean pressed = physical || binding == init_Minecraft.options.keyShift && autismclient.modules.al.tick_Z();
         binding.setDown(pressed);
         if (physical) {
            init_Set.add(binding);
         } else {
            init_Set.remove(binding);
         }
      }
   }

   private static void load(KeyMapping binding) {
      if (binding != null && init_Set.remove(binding)) {
         binding.setDown(false);
      }
   }

   private static boolean init_Z() {
      if (init_Minecraft != null && init_Minecraft.gui.screen() != null) {
         Screen screen = init_Minecraft.gui.screen();
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

   private static boolean load_Z() {
      if (autismclient.modules.FieldCache.init_Z_6()) {
         load_V();
         return false;
      }

      autismclient.modules.PoolEntry module = autismclient.modules.PoolEntry.init_PoolEntry();
      if (module == null || !module.init_Z() || !module.run_Z()) {
         load_V();
         return false;
      }

      if (MacroExecutor.read_Z()) {
         load_V();
         return false;
      }

      if (!init_Z()) {
         load_V();
         return false;
      }

      if (bk.init_bk().update_Z()) {
         load_V();
         return false;
      }

      if (init_Minecraft != null && init_Minecraft.options != null) {
         return true;
      }

      load_V();
      return false;
   }
}
