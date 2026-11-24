package autismclient.mixin;

import autismclient.modules.az;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.List;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.state.MapRenderState.MapDecorationRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapRenderer.class)
public abstract class AutismNoRenderMapMixin {
   @Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$noMapContents(CallbackInfo ci) {
      if (az.node()) {
         ci.cancel();
      }
   }

   @ModifyExpressionValue(
      method = "render",
      at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/MapRenderState;decorations:Ljava/util/List;", opcode = 180),
      require = 0
   )
   private List<MapDecorationRenderState> autism$noMapMarkers(List<MapDecorationRenderState> original) {
      return az.entry() ? List.of() : original;
   }
}
