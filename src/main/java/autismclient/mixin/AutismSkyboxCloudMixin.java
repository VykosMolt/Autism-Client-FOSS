package autismclient.mixin;

import autismclient.modules.CustomSkyboxRenderer;
import net.minecraft.client.renderer.CloudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CloudRenderer.class)
public abstract class AutismSkyboxCloudMixin {
   @Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$skyboxNoClouds(CallbackInfo ci) {
      if (CustomSkyboxRenderer.init_Z()) {
         ci.cancel();
      }
   }
}
