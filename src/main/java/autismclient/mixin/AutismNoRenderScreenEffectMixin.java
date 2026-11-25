package autismclient.mixin;

import autismclient.modules.az;
import autismclient.util.AuditClientFixes;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public abstract class AutismNoRenderScreenEffectMixin {
   @Inject(method = "submitFire", at = @At("HEAD"), cancellable = true, require = 0)
   private static void autism$noFireOverlay(CallbackInfo ci) {
      if (az.sync() || AuditClientFixes.isFreecamActive()) {
         ci.cancel();
      }
   }

   @Inject(method = "submitWater", at = @At("HEAD"), cancellable = true, require = 0)
   private static void autism$noLiquidOverlay(CallbackInfo ci) {
      if (az.flush() || AuditClientFixes.isFreecamActive()) {
         ci.cancel();
      }
   }

   @Inject(method = "submitBlockSprite", at = @At("HEAD"), cancellable = true, require = 0)
   private static void autism$noInWallOverlay(CallbackInfo ci) {
      if (AuditClientFixes.shouldSuppressInWallOverlay()) {
         ci.cancel();
      }
   }
}
