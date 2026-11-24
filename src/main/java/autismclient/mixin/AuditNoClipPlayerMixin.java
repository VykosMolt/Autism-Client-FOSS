package autismclient.mixin;

import autismclient.util.AuditClientFixes;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
abstract class AuditNoClipPlayerMixin {
   @Inject(method = "travel", at = @At("HEAD"), cancellable = true, require = 1)
   private void audit$noClipTravel(Vec3 var1, CallbackInfo var2) {
      Player var3 = (Player)(Object)this;
      if (AuditClientFixes.shouldOverrideNoClipTravel(var3)) {
         var3.noPhysics = true;
         var3.move(MoverType.SELF, var3.getDeltaMovement());
         var3.setOnGround(false);
         var3.fallDistance = 0.0;
         var2.cancel();
      }
   }

   @Inject(method = "updatePlayerPose", at = @At("HEAD"), cancellable = true, require = 1)
   private void audit$noClipPose(CallbackInfo var1) {
      Player var2 = (Player)(Object)this;
      if (AuditClientFixes.shouldOverrideNoClipTravel(var2)) {
         var2.setPose(Pose.STANDING);
         var1.cancel();
      }
   }
}
