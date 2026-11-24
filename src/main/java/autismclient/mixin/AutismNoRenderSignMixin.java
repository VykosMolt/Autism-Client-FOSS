package autismclient.mixin;

import autismclient.modules.az;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSignRenderer.class)
public abstract class AutismNoRenderSignMixin {
   @Inject(method = "submitSignText", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$noSignText(CallbackInfo ci) {
      if (az.value()) {
         ci.cancel();
      }
   }
}
