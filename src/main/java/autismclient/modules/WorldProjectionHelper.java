package autismclient.modules;

import autismclient.mixin.accessor.AutismGameRendererAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

final class WorldProjectionHelper {
   private static final Vector3f ROTATION_AXIS = new Vector3f(0.0F, Mth.SQRT_OF_TWO / 2.0F, Mth.SQRT_OF_TWO / 2.0F);

   private WorldProjectionHelper() {
   }

   static boolean init(Matrix4f dest) {
      Minecraft mc = Minecraft.getInstance();
      GameRenderer renderer = mc.gameRenderer;
      LocalPlayer player = mc.player;
      if (renderer != null && player != null) {
         GameRenderState state = renderer.gameRenderState();
         CameraRenderState camera = state.levelRenderState.cameraRenderState;
         if (!camera.initialized) {
            return false;
         }

         dest.set(camera.projectionMatrix);
         AutismGameRendererAccessor access = (AutismGameRendererAccessor)renderer;
         PoseStack bob = new PoseStack();
         access.autism$bobHurt(camera, bob);
         if (state.optionsRenderState.bobView) {
            access.autism$bobView(camera, bob);
         }

         dest.mul(bob.last().pose());
         float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
         float effectScale = state.optionsRenderState.screenEffectScale;
         float portal = Mth.lerp(partialTick, player.oPortalEffectIntensity, player.portalEffectIntensity);
         float nausea = player.getEffectBlendFactor(MobEffects.NAUSEA, partialTick);
         float spin = NoRenderState.hideNausea() ? 0.0F : Math.max(portal, nausea) * (effectScale * effectScale);
         if (spin > 0.0F) {
            float skew = 5.0F / (spin * spin + 5.0F) - spin * 0.04F;
            skew *= skew;
            float angle = 0.0F;
            dest.rotate(angle, ROTATION_AXIS);
            dest.scale(1.0F / skew, 1.0F, 1.0F);
            dest.rotate(-angle, ROTATION_AXIS);
         }

         dest.mul(camera.viewRotationMatrix);
         return true;
      } else {
         return false;
      }
   }

   static Vec3 init() {
      Minecraft mc = Minecraft.getInstance();
      CameraRenderState camera = mc.gameRenderer.gameRenderState().levelRenderState.cameraRenderState;
      return camera.initialized ? camera.pos : mc.gameRenderer.mainCamera().position();
   }
}
