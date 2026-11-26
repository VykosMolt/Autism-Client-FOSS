package autismclient.mixin;

import autismclient.compat263.Compat263;
import autismclient.modules.ModuleRuntimeManager;
import autismclient.util.bk;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractRecipeBookScreen.class)
public abstract class AutismRecipeBookScreenMixin {
   @Unique
   private boolean autism$isActive() {
      ModuleRuntimeManager module = ModuleRuntimeManager.init_PoolEntry();
      return module != null && module.init_Z();
   }

   @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
   private void autism$handleOverlayClick(MouseButtonEvent click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
      if (this.autism$isActive()) {
         bk manager = bk.init_bk();
         if (manager.init_Z_D_2(click.x(), click.y(), Compat263.mouseEventGlfwButton(click))) {
            cir.setReturnValue(true);
         }
      }
   }

   @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
   private void autism$handleOverlayDrag(MouseButtonEvent click, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
      if (this.autism$isActive() && bk.init_bk().init(click.x(), click.y(), Compat263.mouseEventGlfwButton(click), deltaX, deltaY)) {
         cir.setReturnValue(true);
      }
   }

   @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
   private void autism$handleOverlayKeys(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
      if (this.autism$isActive() && bk.init_bk().init_Z_I(Compat263.keyEventGlfwKey(input), input.keycode(), Compat263.keyEventGlfwModifiers(input))) {
         cir.setReturnValue(true);
      }
   }

   @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
   private void autism$handleOverlayChars(CharacterEvent input, CallbackInfoReturnable<Boolean> cir) {
      if (this.autism$isActive() && bk.init_bk().init_Z_C_I((char)input.codepoint(), 0)) {
         cir.setReturnValue(true);
      }
   }
}
