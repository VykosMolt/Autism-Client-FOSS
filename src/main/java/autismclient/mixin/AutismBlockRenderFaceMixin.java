package autismclient.mixin;

import autismclient.modules.am;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class AutismBlockRenderFaceMixin {
   @Inject(method = "shouldRenderFace", at = @At("HEAD"), cancellable = true)
   private static void autism$xrayForceSelectedFaces(BlockState state, BlockState neighborState, Direction direction, CallbackInfoReturnable<Boolean> cir) {
      if (am.read_Z() && am.init_Z_BlockState(state, neighborState, direction)) {
         cir.setReturnValue(true);
      }
   }
}
