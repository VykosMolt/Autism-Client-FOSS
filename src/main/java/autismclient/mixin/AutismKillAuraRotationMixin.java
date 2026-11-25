package autismclient.mixin;

import autismclient.modules.aI;
import autismclient.util.cn;
import autismclient.util.macro.g;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public class AutismKillAuraRotationMixin {
   @ModifyExpressionValue(method = "sendChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getYRot()F"))
   private float autism$killAuraSilentYaw(float original) {
      LocalPlayer self = (LocalPlayer)(Object)this;
      return g.init_Z() ? g.init(self, original) : cn.init(self, original);
   }

   @ModifyExpressionValue(method = "sendChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getXRot()F"))
   private float autism$killAuraSilentPitch(float original) {
      LocalPlayer self = (LocalPlayer)(Object)this;
      return g.init_Z() ? g.load(self, original) : cn.load(self, original);
   }

   @ModifyExpressionValue(
      method = "pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getViewVector(F)Lnet/minecraft/world/phys/Vec3;")
   )
   private static Vec3 autism$killAuraSilentViewVector(
      Vec3 original, Entity camera, double blockInteractionRange, double entityInteractionRange, float tickDelta
   ) {
      if (camera != Minecraft.getInstance().player) {
         return original;
      }

      if (g.init_Z()) {
         return g.init((LocalPlayer)camera, original);
      }

      Vec3 scaffold = aI.init_Vec3_LocalPlayer_Vec3((LocalPlayer)camera, original);
      return cn.init((LocalPlayer)camera, scaffold);
   }
}
