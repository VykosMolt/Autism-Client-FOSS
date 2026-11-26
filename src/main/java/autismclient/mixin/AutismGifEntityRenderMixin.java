package autismclient.mixin;

import autismclient.util.gif.GifRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class AutismGifEntityRenderMixin {
   @Inject(method = "submit", at = @At("HEAD"), require = 0)
   private void autism$pushShieldOwner(
      EntityRenderState renderState,
      CameraRenderState camera,
      double x,
      double y,
      double z,
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      CallbackInfo ci
   ) {
      if (renderState instanceof AvatarRenderState avatar) {
         ClientLevel level = Minecraft.getInstance().level;
         if (level != null && level.getEntity(avatar.id) instanceof Player player && player.getGameProfile() != null) {
            String name = player.getGameProfile().name();
            if (name != null && !name.isEmpty()) {
               GifRenderContext.init(name);
            }
         }
      }
   }

   @Inject(method = "submit", at = @At("RETURN"), require = 0)
   private void autism$popShieldOwner(
      EntityRenderState renderState,
      CameraRenderState camera,
      double x,
      double y,
      double z,
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      CallbackInfo ci
   ) {
      GifRenderContext.init_V();
   }
}
