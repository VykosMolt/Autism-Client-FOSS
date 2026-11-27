package autismclient.mixin;

import autismclient.modules.EntityControlModule;
import autismclient.modules.KBDisplacementModule;
import autismclient.modules.NoSlowModule;
import autismclient.modules.ScaffoldModule;
import autismclient.modules.KillAuraModule;
import autismclient.modules.LeafDupeModule;
import autismclient.modules.MovementModuleHooks;
import autismclient.modules.al;
import autismclient.modules.NoPushModule;
import autismclient.modules.f;
import autismclient.modules.DangerModule;
import autismclient.util.AutismLiteVariant;
import autismclient.util.SprintStateTracker;
import autismclient.util.SilentMovementCoordinator;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.PlayerRideableJumping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class AutismLocalPlayerMovementMixin {
   @Inject(method = "moveTowardsClosestSpace", at = @At("HEAD"), cancellable = true)
   private void autism$noBlockPush(double x, double z, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled() && NoPushModule.init((LocalPlayer)(Object)this, "blocks")) {
         ci.cancel();
      }
   }

   @ModifyReturnValue(method = "itemUseSpeedMultiplier", at = @At("RETURN"))
   private float autism$noItemSlowdown(float multiplier) {
      return !AutismLiteVariant.enabled() && NoSlowModule.init((LocalPlayer)(Object)this) ? 1.0F : multiplier;
   }

   @ModifyReturnValue(method = "isSlowDueToUsingItem", at = @At("RETURN"))
   private boolean autism$allowSprintWhileUsing(boolean slow) {
      return !AutismLiteVariant.enabled() && slow && NoSlowModule.init((LocalPlayer)(Object)this) ? false : slow;
   }

   @ModifyExpressionValue(
      method = {"modifyInput", "canStartSprinting"},
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isMovingSlowly()Z")
   )
   private boolean autism$noPostureSlowdown(boolean slow) {
      return !AutismLiteVariant.enabled() && slow && NoSlowModule.load((LocalPlayer)(Object)this) ? false : slow;
   }

   @ModifyExpressionValue(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getYRot()F"))
   private float autism$scaffoldSilentMovementYaw(float original) {
      LocalPlayer player = (LocalPlayer)(Object)this;
      float fastExp = f.init(player, original);
      float scaffold = ScaffoldModule.overrideYaw(player, fastExp);
      float yaw = SilentMovementCoordinator.getPlayerYaw(player, scaffold);
      if (!AutismLiteVariant.enabled()) {
         yaw = LeafDupeModule.overrideYaw(player, yaw);
      }

      SilentMovementCoordinator.setCameraYaw(yaw);
      return yaw;
   }

   @ModifyExpressionValue(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getXRot()F"))
   private float autism$scaffoldSilentMovementPitch(float original) {
      LocalPlayer player = (LocalPlayer)(Object)this;
      float fastExp = f.load(player, original);
      float scaffold = ScaffoldModule.overridePitch(player, fastExp);
      float pitch = SilentMovementCoordinator.getPlayerPitch(player, scaffold);
      if (!AutismLiteVariant.enabled()) {
         pitch = LeafDupeModule.overridePitch(player, pitch);
      }

      SilentMovementCoordinator.setCameraPitch(pitch);
      return pitch;
   }

   @Inject(method = "isShiftKeyDown", at = @At("HEAD"), cancellable = true)
   private void autism$flightNoSneak(CallbackInfoReturnable<Boolean> cir) {
      if (MovementModuleHooks.init_Z((LocalPlayer)(Object)this)) {
         cir.setReturnValue(false);
      }
   }

   @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;canStartSprinting()Z"))
   private boolean autism$sprintDecisionMovementTick(boolean original) {
      return !AutismLiteVariant.enabled() && DangerModule.push() ? false : MovementModuleHooks.init(original, true) && !KillAuraModule.close_Z_6() && !ScaffoldModule.close_Z_4();
   }

   @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Input;sprint()Z"))
   private boolean autism$sprintDecisionInput(boolean original) {
      return !AutismLiteVariant.enabled() && DangerModule.push() ? false : MovementModuleHooks.init(original, false) && !KillAuraModule.close_Z_6() && !ScaffoldModule.close_Z_4();
   }

   @ModifyExpressionValue(method = "sendIsSprintingIfNeeded", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSprinting()Z"))
   private boolean autism$sprintNetworkDecision(boolean original) {
      boolean announced = MovementModuleHooks.init(original);
      SprintStateTracker.init_V(announced);
      return announced;
   }

   @ModifyExpressionValue(method = "canStartSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/ClientInput;hasForwardImpulse()Z"))
   private boolean autism$omniSprintForwardImpulse(boolean original) {
      return MovementModuleHooks.load() ? ((LocalPlayer)(Object)this).input.getMoveVector().length() > 1.0E-5F : original;
   }

   @ModifyExpressionValue(
      method = "shouldStopRunSprinting",
      at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;horizontalCollision:Z", opcode = 180)
   )
   private boolean autism$sprintIgnoreCollision(boolean original) {
      return !MovementModuleHooks.read() && original;
   }

   @ModifyReturnValue(method = "shouldStopRunSprinting", at = @At("RETURN"))
   private boolean autism$sprintForceStop(boolean shouldStop) {
      return !AutismLiteVariant.enabled() && DangerModule.push() ? true : shouldStop || MovementModuleHooks.update() || KillAuraModule.close_Z_6() || ScaffoldModule.close_Z_4();
   }

   @ModifyExpressionValue(method = "isSprintingPossible", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isMobilityRestricted()Z"))
   private boolean autism$sprintIgnoreBlindness(boolean original) {
      return !MovementModuleHooks.write() && original;
   }

   @Inject(method = "getJumpRidingScale", at = @At("RETURN"), cancellable = true)
   private void autism$entityControlMaxJump(CallbackInfoReturnable<Float> cir) {
      if (EntityControlModule.load_Z_3()) {
         cir.setReturnValue(1.0F);
      }
   }

   @Inject(method = "jumpableVehicle", at = @At("RETURN"), cancellable = true)
   private void autism$entityControlFlightJump(CallbackInfoReturnable<PlayerRideableJumping> cir) {
      if (EntityControlModule.read()) {
         cir.setReturnValue(null);
      }
   }

   @Inject(method = "sendPosition", at = @At("HEAD"))
   private void autism$networkMovementTickPre(CallbackInfo ci) {
      if ((LocalPlayer)(Object)this == Minecraft.getInstance().player) {
         if (!AutismLiteVariant.enabled()) {
            LeafDupeModule.beforeSendPosition((LocalPlayer)(Object)this);
         }

         al.close_V();
      }
   }

   @Inject(method = "sendPosition", at = @At("TAIL"))
   private void autism$debugRotationTick(CallbackInfo ci) {
      if ((LocalPlayer)(Object)this == Minecraft.getInstance().player) {
         if (!AutismLiteVariant.enabled()) {
            LeafDupeModule.afterSendPosition((LocalPlayer)(Object)this);
         }

         SilentMovementCoordinator.captureDebugTelemetry(Minecraft.getInstance());
         if (!AutismLiteVariant.enabled()) {
            KBDisplacementModule.afterSendPosition();
         }
      }
   }

   @Inject(
      method = "tick",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V", shift = Shift.BEFORE, ordinal = 0),
      cancellable = true
   )
   private void autism$playerTickCancel(CallbackInfo ci) {
      if ((LocalPlayer)(Object)this == Minecraft.getInstance().player && al.run_Z()) {
         ci.cancel();
      }
   }
}
