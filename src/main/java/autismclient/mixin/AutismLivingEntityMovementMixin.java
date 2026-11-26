package autismclient.mixin;

import autismclient.modules.AirJumpModule;
import autismclient.modules.ScaffoldModule;
import autismclient.modules.MovementModuleHooks;
import autismclient.modules.NoJumpDelayModule;
import autismclient.modules.ClutchModule;
import autismclient.util.AutismLiteVariant;
import autismclient.util.SilentMovementCoordinator;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class AutismLivingEntityMovementMixin {
   @Shadow
   protected boolean jumping;
   @Shadow
   private int noJumpDelay;

   @Inject(method = "aiStep", at = @At("HEAD"))
   private void autism$removeGroundJumpDelay(CallbackInfo ci) {
      if (!AutismLiteVariant.enabled() && this.noJumpDelay > 0 && (Object)this instanceof LocalPlayer && NoJumpDelayModule.init((LivingEntity)(Object)this)) {
         this.noJumpDelay = 0;
      }
   }

   @Inject(method = "aiStep", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;jumping:Z", opcode = 180))
   private void autism$airJump(CallbackInfo ci) {
      if (this.jumping && this.noJumpDelay == 0 && AirJumpModule.init_Z_4()) {
         ((LivingEntity)(Object)this).jumpFromGround();
         this.noJumpDelay = 10;
      }
   }

   @Inject(method = "jumpFromGround", at = @At("HEAD"))
   private void autism$airJumpConsume(CallbackInfo ci) {
      AirJumpModule.init((LivingEntity)(Object)this);
   }

   @Inject(method = "jumpFromGround", at = @At("RETURN"))
   private void autism$clutchOwnJump(CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         ClutchModule.init_V_LivingEntity_5((LivingEntity)(Object)this);
      }
   }

   @ModifyExpressionValue(method = "jumpFromGround", at = @At(value = "NEW", target = "(DDD)Lnet/minecraft/world/phys/Vec3;"))
   private Vec3 autism$killAuraSilentJump(Vec3 original) {
      LivingEntity entity = (LivingEntity)(Object)this;
      Vec3 scaffold = ScaffoldModule.init_Vec3_LivingEntity_Vec3_2(entity, original);
      return SilentMovementCoordinator.getJumpImpulse(entity, scaffold);
   }

   @ModifyExpressionValue(method = "updateFallFlyingMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getXRot()F"))
   private float autism$killAuraSilentGlidePitch(float original) {
      LivingEntity entity = (LivingEntity)(Object)this;
      float scaffold = ScaffoldModule.init_F_LivingEntity_F(entity, original);
      return SilentMovementCoordinator.getEntityPitch(entity, scaffold);
   }

   @ModifyExpressionValue(
      method = "updateFallFlyingMovement",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getLookAngle()Lnet/minecraft/world/phys/Vec3;")
   )
   private Vec3 autism$killAuraSilentGlideLook(Vec3 original) {
      LivingEntity entity = (LivingEntity)(Object)this;
      Vec3 scaffold = ScaffoldModule.load_Vec3_LivingEntity_Vec3(entity, original);
      return SilentMovementCoordinator.getEntityLookVector(entity, scaffold);
   }

   @Inject(method = "travelInFluid", at = @At("RETURN"))
   private void autism$restoreLiquidSpeed(Vec3 input, CallbackInfo ci) {
      MovementModuleHooks.init_V((LivingEntity)(Object)this);
   }
}
