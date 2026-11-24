package autismclient.mixin;

import autismclient.modules.am;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.LightCoordsUtil.BrightnessGetter;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightCoordsUtil.class)
public class AutismLightCoordsUtilMixin {
   @Inject(
      method = "getLightCoords(Lnet/minecraft/util/LightCoordsUtil$BrightnessGetter;Lnet/minecraft/world/level/BlockAndLightGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I",
      at = @At("RETURN"),
      cancellable = true
   )
   private static void autism$fullbrightLuminance(
      BrightnessGetter brightnessGetter, BlockAndLightGetter level, BlockState state, BlockPos pos, CallbackInfoReturnable<Integer> cir
   ) {
      if (am.close()) {
         cir.setReturnValue(am.init_I_BlockAndLightGette(level, pos, cir.getReturnValue()));
      }
   }
}
