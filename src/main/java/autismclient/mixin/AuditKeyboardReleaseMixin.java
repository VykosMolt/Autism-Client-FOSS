package autismclient.mixin;

import autismclient.util.AuditClientFixes;
import autismclient.util.GuiMovementPassThrough;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(KeyboardHandler.class)
abstract class AuditKeyboardReleaseMixin {
   @Redirect(
      method = "keyPress",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;keyReleased(Lnet/minecraft/client/input/KeyEvent;)Z"),
      require = 1
   )
   private boolean audit$containerKeyReleased(Screen var1, KeyEvent var2) {
      return var1 instanceof AbstractContainerScreen && AuditClientFixes.isAutismActive() && GuiMovementPassThrough.handleKey(var2, false) ? true : var1.keyReleased(var2);
   }
}
