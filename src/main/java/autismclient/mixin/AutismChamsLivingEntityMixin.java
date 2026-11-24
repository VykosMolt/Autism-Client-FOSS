package autismclient.mixin;

import autismclient.modules.am;
import autismclient.util.PkgCls_f;
import autismclient.util.PkgCls_h;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class AutismChamsLivingEntityMixin {
   @Inject(method = "submit", at = @At("HEAD"), require = 0)
   private void autism$chamsContextStart(
      LivingEntityRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci
   ) {
      PkgCls_f.init_V();
      if (am.run() && state instanceof PkgCls_h holder && holder.autism$chamsActive()) {
         PkgCls_f.init(holder.autism$chamsVisible(), holder.autism$chamsOccluded(), am.reset());
      }
   }

   @Inject(method = "submit", at = @At("RETURN"), require = 0)
   private void autism$chamsContextEnd(LivingEntityRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
      PkgCls_f.init_V();
   }
}
