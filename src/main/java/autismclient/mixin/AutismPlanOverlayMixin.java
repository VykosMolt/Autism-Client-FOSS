package autismclient.mixin;

import autismclient.ai.AiPrinterWorld;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Level.class)
public abstract class AutismPlanOverlayMixin {
   @ModifyReturnValue(method = "getBlockState", at = @At("RETURN"))
   private BlockState autism$planBlock(BlockState original, BlockPos pos) {
      BlockState planned = AiPrinterWorld.plannedState((Level)(Object)this, pos);
      return planned != null ? planned : original;
   }

   @ModifyReturnValue(method = "getFluidState", at = @At("RETURN"))
   private FluidState autism$planFluid(FluidState original, BlockPos pos) {
      BlockState planned = AiPrinterWorld.plannedState((Level)(Object)this, pos);
      return planned != null ? planned.getFluidState() : original;
   }
}
