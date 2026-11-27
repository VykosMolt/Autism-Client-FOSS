package autismclient.mixin;

import autismclient.ai.AiVisualization;
import autismclient.modules.PearlGhostRenderer;
import autismclient.modules.EndPortalRenderer;
import autismclient.modules.BedEspOverlayRenderer;
import autismclient.modules.ar;
import autismclient.util.AutismLiteVariant;
import autismclient.util.SkeletonEspRenderer;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class AutismLevelRendererTracerMixin {
   @Inject(
      method = "render(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;Lorg/joml/Vector4f;ZZ)V",
      at = @At("RETURN")
   )
   private void autism$drawTracers(
      GraphicsResourceAllocator allocator,
      boolean renderBlockOutline,
      CameraRenderState cameraState,
      GpuBufferSlice gpuBufferSlice,
      Vector4f vector4f,
      boolean shouldRenderSky,
      boolean flag,
      CallbackInfo ci
   ) {
      boolean tracers = ar.init_Z();
      boolean esp = ar.load_Z();
      boolean waypoints = ar.read_Z();
      boolean skeleton = SkeletonEspRenderer.hasSkeletons();
      boolean ai = !AutismLiteVariant.enabled() && AiVisualization.hasContent();
      boolean danger = !AutismLiteVariant.enabled() && PearlGhostRenderer.init();
      boolean beds = !AutismLiteVariant.enabled() && BedEspOverlayRenderer.isActive();
      boolean portal = !AutismLiteVariant.enabled() && EndPortalRenderer.init_Z();
      if (tracers || esp || waypoints || skeleton || ai || danger || beds || portal) {
         PoseStack matrixStack = new PoseStack();
         matrixStack.mulPose(cameraState.viewRotationMatrix);
         if (tracers) {
            ar.init_V_PoseStack(matrixStack);
         }

         if (esp) {
            ar.load(matrixStack);
         }

         if (skeleton) {
            SkeletonEspRenderer.init(matrixStack);
         }

         if (waypoints) {
            ar.read(matrixStack);
         }

         if (!AutismLiteVariant.enabled()) {
            if (ai) {
               AiVisualization.init(matrixStack);
            }

            if (danger) {
               PearlGhostRenderer.init(matrixStack);
            }

            if (beds) {
               BedEspOverlayRenderer.init(matrixStack);
            }

            if (portal) {
               EndPortalRenderer.init(matrixStack);
            }
         }
      }
   }
}
