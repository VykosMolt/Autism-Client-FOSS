package autismclient.mixin;

import autismclient.modules.az;
import net.minecraft.client.renderer.SkyRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public abstract class AutismNoRenderSkyMixin {
   @Inject(method = "renderSkyDisc", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$noSkyDisc(CallbackInfo ci) {
      if (az.index()) {
         ci.cancel();
      }
   }

   @Inject(method = "renderEndSky", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$noEndSky(CallbackInfo ci) {
      if (az.index()) {
         ci.cancel();
      }
   }

   @Inject(method = "renderStars", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$noStars(CallbackInfo ci) {
      if (az.count()) {
         ci.cancel();
      }
   }

   @Inject(method = "renderSun", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$noSun(CallbackInfo ci) {
      if (az.flags()) {
         ci.cancel();
      }
   }

   @Inject(method = "renderMoon", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$noMoon(CallbackInfo ci) {
      if (az.level()) {
         ci.cancel();
      }
   }

   @Inject(method = "renderSunriseAndSunset", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$noSunriseGlow(CallbackInfo ci) {
      if (az.frame()) {
         ci.cancel();
      }
   }
}
