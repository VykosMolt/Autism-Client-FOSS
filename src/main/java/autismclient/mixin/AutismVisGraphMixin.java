package autismclient.mixin;

import autismclient.modules.am;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VisGraph.class)
public class AutismVisGraphMixin {
   @Inject(method = "setOpaque", at = @At("HEAD"), cancellable = true)
   private void autism$xrayDisableChunkOcclusion(BlockPos pos, CallbackInfo ci) {
      if (am.read_Z()) {
         ci.cancel();
      }
   }
}
