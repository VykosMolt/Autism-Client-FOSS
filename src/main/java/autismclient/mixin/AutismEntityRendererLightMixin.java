package autismclient.mixin;

import autismclient.modules.am;
import autismclient.modules.as;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LightLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class AutismEntityRendererLightMixin<T extends Entity> {
   @Inject(method = "getSkyLightLevel", at = @At("RETURN"), cancellable = true)
   private void autism$fullbrightSkyLight(T entity, BlockPos blockPos, CallbackInfoReturnable<Integer> cir) {
      if (am.close()) {
         int boosted = am.init(LightLayer.SKY);
         if (boosted > cir.getReturnValue()) {
            cir.setReturnValue(boosted);
         }
      }
   }

   @Inject(method = "getBlockLightLevel", at = @At("RETURN"), cancellable = true)
   private void autism$fullbrightBlockLight(T entity, BlockPos blockPos, CallbackInfoReturnable<Integer> cir) {
      if (am.close()) {
         int boosted = am.init(LightLayer.BLOCK);
         if (boosted > cir.getReturnValue()) {
            cir.setReturnValue(boosted);
         }
      }
   }

   @Inject(method = "getNameTag", at = @At("RETURN"), cancellable = true)
   private void autism$censorNameTag(T entity, CallbackInfoReturnable<Component> cir) {
      if (as.init_Z_33() && cir.getReturnValue() != null) {
         cir.setReturnValue(as.load(cir.getReturnValue()));
      }
   }
}
