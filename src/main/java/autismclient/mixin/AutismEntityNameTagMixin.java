package autismclient.mixin;

import autismclient.protector.ComponentSafetyValidator;
import autismclient.modules.NameTagsRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class AutismEntityNameTagMixin {
   @Inject(method = "extractRenderState", at = @At("TAIL"))
   private void autism$suppressVanillaNameTag(Entity entity, EntityRenderState state, float partialTick, CallbackInfo ci) {
      if (state != null && state.nameTag != null && NameTagsRenderer.overridesNameTag(entity)) {
         state.nameTag = null;
      }
   }

   @Inject(method = "submit", at = @At("HEAD"))
   private void autism$scrubHostileNameTags(
      EntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci
   ) {
      if (state != null) {
         if (state.nameTag != null && !ComponentSafetyValidator.init(state.nameTag)) {
            state.nameTag = Component.literal("[unsafe name removed]").withStyle(ChatFormatting.DARK_GRAY);
         }

         if (state.scoreText != null && !ComponentSafetyValidator.init(state.scoreText)) {
            state.scoreText = Component.literal("[unsafe score removed]").withStyle(ChatFormatting.DARK_GRAY);
         }
      }
   }
}
