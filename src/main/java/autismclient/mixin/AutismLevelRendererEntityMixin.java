package autismclient.mixin;

import autismclient.modules.FrameEspCatalog;
import autismclient.modules.am;
import autismclient.util.ch;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class AutismLevelRendererEntityMixin {
   @Inject(method = "extractEntity", at = @At("RETURN"))
   private void autism$espEntityOutline(Entity entity, float partialTickTime, CallbackInfoReturnable<EntityRenderState> cir) {
      EntityRenderState state = cir.getReturnValue();
      if (state != null) {
         ch.init_V_Entity_2(entity, state, partialTickTime);
         if (am.put()) {
            int frameOutline = FrameEspCatalog.init_I(entity);
            if (frameOutline != 0) {
               state.outlineColor = frameOutline;
            } else {
               int itemOutline = am.flush(entity);
               if (itemOutline != 0) {
                  state.outlineColor = itemOutline;
               } else {
                  int entityOutline = am.close(entity);
                  if (entityOutline != 0) {
                     state.outlineColor = entityOutline;
                  }
               }
            }
         }
      }
   }
}
