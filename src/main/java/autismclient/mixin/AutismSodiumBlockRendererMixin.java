package autismclient.mixin;

import autismclient.modules.GoldenLeverModule;
import autismclient.modules.am;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer", remap = false)
public abstract class AutismSodiumBlockRendererMixin {
   @Unique
   private int autism$xrayAlpha = -1;
   @Unique
   private boolean autism$goldenLever;
   @Unique
   private int autism$darkenTint = -1;

   @Inject(method = "renderModel", at = @At("HEAD"), cancellable = true)
   private void autism$xraySodiumBlockStart(BlockStateModel model, BlockState state, BlockPos pos, BlockPos origin, CallbackInfo ci) {
      boolean xrayActive = am.read_Z();
      boolean goldenLeverActive = GoldenLeverModule.isActive();
      boolean darkenActive = am.write_Z();
      if (!xrayActive && !goldenLeverActive && !darkenActive) {
         this.autism$xrayAlpha = -1;
         this.autism$goldenLever = false;
         this.autism$darkenTint = -1;
      } else {
         this.autism$xrayAlpha = xrayActive ? am.load(state, pos) : -1;
         this.autism$goldenLever = goldenLeverActive && GoldenLeverModule.init(state);
         this.autism$darkenTint = darkenActive ? am.init_I_BlockState_BlockPos(state, pos) : -1;
         if (this.autism$xrayAlpha == 0) {
            ci.cancel();
         }
      }
   }

   @Inject(method = "renderModel", at = @At("RETURN"))
   private void autism$xraySodiumBlockEnd(BlockStateModel model, BlockState state, BlockPos pos, BlockPos origin, CallbackInfo ci) {
      this.autism$xrayAlpha = -1;
      this.autism$goldenLever = false;
      this.autism$darkenTint = -1;
   }

   @Inject(method = "processQuad", at = @At("HEAD"))
   private void autism$xraySodiumBlockMaterial(@Coerce Object quad, CallbackInfo ci) {
      int alpha = this.autism$xrayAlpha;
      if (this.autism$goldenLever) {
         am.load(quad, -11702);
      }

      if (this.autism$darkenTint != -1) {
         am.load(quad, this.autism$darkenTint);
      }

      if (alpha >= 0) {
         am.init_V_Obj_I(quad, alpha);
         if (alpha > 0 && alpha < 255) {
            am.init_V_Obj_ChunkSectionLayer(quad, ChunkSectionLayer.TRANSLUCENT);
         }
      }
   }
}
