package autismclient.mixin;

import autismclient.compat263.Compat263;
import autismclient.util.AutismLiteVariant;
import autismclient.util.al;
import autismclient.util.cA;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ContainerEventHandler.class)
public interface AutismContainerEventHandlerMixin {
   @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
   private void autism$musicDisplayClick(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
      if (!AutismLiteVariant.enabled()
         && (Object)this instanceof Screen screen
         && Compat263.mouseEventGlfwButton(event) == 0
         && al.init_Z_I_2(cA.init_I_D(event.x()), cA.init_I_D(event.y()), screen)) {
         cir.setReturnValue(true);
      }
   }

   @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
   private void autism$musicDisplayDrag(MouseButtonEvent event, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
      if (!AutismLiteVariant.enabled() && (Object)this instanceof Screen screen && al.load_Z_I(cA.init_I_D(event.x()), cA.init_I_D(event.y()), screen)) {
         cir.setReturnValue(true);
      }
   }

   @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
   private void autism$musicDisplayRelease(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
      if (!AutismLiteVariant.enabled() && (Object)this instanceof Screen screen && al.init_Z_Screen(screen)) {
         cir.setReturnValue(true);
      }
   }
}
