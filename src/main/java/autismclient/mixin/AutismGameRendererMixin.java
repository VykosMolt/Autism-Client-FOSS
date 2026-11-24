package autismclient.mixin;

import autismclient.compat263.Compat263;
import autismclient.modules.al;
import autismclient.util.cf;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class AutismGameRendererMixin {
   @ModifyExpressionValue(
      method = {"extract", "render"},
      at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;level:Lnet/minecraft/client/multiplayer/ClientLevel;")
   )
   private ClientLevel autism$treatLevelAsUnavailableWithoutPlayer(ClientLevel level) {
      Minecraft minecraft = Minecraft.getInstance();
      return minecraft.player == null ? null : level;
   }

   @Inject(method = "renderLevel", at = @At("HEAD"), cancellable = true)
   private void autism$onRenderLevel(CallbackInfo ci) {
      if (Minecraft.getInstance().player == null) {
         ci.cancel();
      } else if (cf.init_Z_J(2L)) {
         al.init_V_F(Compat263.getPartialTick());
      }
   }
}
