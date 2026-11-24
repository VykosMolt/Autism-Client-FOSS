package autismclient.mixin;

import autismclient.modules.aC;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class AutismFreecamViewMixin {
   @Inject(method = "bobView", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$freecamNoBob(CameraRenderState cameraState, PoseStack poseStack, CallbackInfo ci) {
      if (aC.init_Z()) {
         ci.cancel();
      }
   }

   @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$freecamNoHurtTilt(CameraRenderState cameraState, PoseStack poseStack, CallbackInfo ci) {
      if (aC.init_Z()) {
         ci.cancel();
      }
   }

   @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$freecamNoHandWhileDriven(CameraRenderState cameraState, PlayerRenderState playerRenderState, GpuTextureView lightmap, CallbackInfo ci) {
      if (aC.init_Z() && aC.write()) {
         ci.cancel();
      }
   }

   @ModifyArg(
      method = {"render3dHud", "renderLevel"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/ScreenEffectRenderer;submit(FLnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/PlayerRenderState;Lnet/minecraft/client/renderer/state/level/CameraRenderState;Z)V"
      ),
      index = 4,
      require = 0
   )
   private boolean autism$freecamNoBodyOverlays(boolean isHudHidden) {
      return isHudHidden || aC.init_Z();
   }
}
