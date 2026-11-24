package autismclient.mixin;

import autismclient.modules.az;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LayerRenderState.class)
public abstract class AutismNoRenderGlintMixin {
   @ModifyVariable(method = "setFoilType", at = @At("HEAD"), argsOnly = true, require = 0)
   private FoilType autism$noGlint(FoilType foilType) {
      return az.tick() ? FoilType.NONE : foilType;
   }
}
