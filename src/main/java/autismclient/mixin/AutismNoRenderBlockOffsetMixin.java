package autismclient.mixin;

import autismclient.modules.NoRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockStateBase.class)
public abstract class AutismNoRenderBlockOffsetMixin {
   @Inject(method = "getOffset", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$noOffset(BlockPos pos, CallbackInfoReturnable<Vec3> cir) {
      if (NoRenderState.pool()) {
         cir.setReturnValue(Vec3.ZERO);
      }
   }
}
