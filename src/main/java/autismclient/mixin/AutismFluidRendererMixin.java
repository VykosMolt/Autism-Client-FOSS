package autismclient.mixin;

import autismclient.modules.am;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.block.FluidRenderer.Output;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidRenderer.class)
public class AutismFluidRendererMixin {
   @Unique
   private static final ThreadLocal<Integer> PACKUTIL_XRAY_FLUID_ALPHA = ThreadLocal.withInitial(() -> -1);
   @Unique
   private static final ThreadLocal<Boolean> PACKUTIL_FORCE_XRAY_FLUID_SIDES = ThreadLocal.withInitial(() -> false);

   @Inject(method = "tesselate", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$xrayFluidMode(BlockAndTintGetter level, BlockPos pos, Output output, BlockState blockState, FluidState fluidState, CallbackInfo ci) {
      if (am.read_Z()) {
         int alpha = am.init_I_BlockAndTintGetter_2(level, pos, fluidState);
         if (alpha == 0) {
            PACKUTIL_XRAY_FLUID_ALPHA.set(-1);
            PACKUTIL_FORCE_XRAY_FLUID_SIDES.set(false);
            ci.cancel();
         } else {
            PACKUTIL_XRAY_FLUID_ALPHA.set(alpha);
            PACKUTIL_FORCE_XRAY_FLUID_SIDES.set(am.push());
         }
      }
   }

   @Inject(method = "tesselate", at = @At("RETURN"), require = 0)
   private void autism$clearXrayFluidMode(BlockAndTintGetter level, BlockPos pos, Output output, BlockState blockState, FluidState fluidState, CallbackInfo ci) {
      PACKUTIL_XRAY_FLUID_ALPHA.set(-1);
      PACKUTIL_FORCE_XRAY_FLUID_SIDES.set(false);
   }

   @Inject(method = "isFaceOccludedByNeighbor", at = @At("RETURN"), cancellable = true, require = 0)
   private static void autism$xrayFluidSides(Direction direction, float height, BlockState neighborState, CallbackInfoReturnable<Boolean> cir) {
      if (am.read_Z()) {
         boolean occluded = cir.getReturnValue();
         if (occluded && !direction.getAxis().isVertical() && PACKUTIL_FORCE_XRAY_FLUID_SIDES.get()) {
            cir.setReturnValue(!am.init(neighborState));
         }
      }
   }

   @Inject(method = "vertex", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$xrayFluidAlpha(VertexConsumer builder, float x, float y, float z, int color, float u, float v, int lightCoords, CallbackInfo ci) {
      if (am.read_Z()) {
         int alpha = PACKUTIL_XRAY_FLUID_ALPHA.get();
         if (alpha != -1) {
            builder.addVertex(x, y, z, ARGB.color(alpha, color), u, v, OverlayTexture.NO_OVERLAY, lightCoords, 0.0F, 1.0F, 0.0F);
            ci.cancel();
         }
      }
   }

   @ModifyArg(
      method = "tesselate",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/block/FluidRenderer$Output;getBuilder(Lnet/minecraft/client/renderer/chunk/ChunkSectionLayer;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
      ),
      index = 0,
      require = 0
   )
   private ChunkSectionLayer autism$xrayFluidLayer(ChunkSectionLayer layer) {
      if (!am.read_Z()) {
         return layer;
      }

      int alpha = PACKUTIL_XRAY_FLUID_ALPHA.get();
      return alpha > 0 && alpha < 255 ? ChunkSectionLayer.TRANSLUCENT : layer;
   }
}
