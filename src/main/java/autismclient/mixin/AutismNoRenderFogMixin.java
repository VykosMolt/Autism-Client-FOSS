package autismclient.mixin;

import autismclient.modules.aJ;
import autismclient.modules.az;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class AutismNoRenderFogMixin {
   @ModifyExpressionValue(
      method = "getBuffer",
      at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/fog/FogRenderer;fogEnabled:Z", opcode = 178),
      require = 0
   )
   private boolean autism$noFog(boolean original) {
      return original && !autism$fogOff();
   }

   @Inject(method = "updateBuffer(Lnet/minecraft/client/renderer/fog/FogData;)V", at = @At("HEAD"), require = 0)
   private void autism$noFogBuffer(FogData data, CallbackInfo ci) {
      if (autism$fogOff() && data != null) {
         data.environmentalStart = 1.0E7F;
         data.environmentalEnd = 2.0E7F;
         data.renderDistanceStart = 1.0E7F;
         data.renderDistanceEnd = 2.0E7F;
         data.skyEnd = 2.0E7F;
         data.cloudEnd = 2.0E7F;
         if (data.color != null) {
            data.color.w = 0.0F;
         }
      }
   }

   private static boolean autism$fogOff() {
      return az.size() || aJ.init_Z();
   }
}
