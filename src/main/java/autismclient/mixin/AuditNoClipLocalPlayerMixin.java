package autismclient.mixin;

import autismclient.util.AuditClientFixes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
abstract class AuditNoClipLocalPlayerMixin {
   @Shadow
   private boolean crouching;

   @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/ClientInput;tick()V", shift = Shift.AFTER), require = 1)
   private void audit$noClipPostInput(CallbackInfo var1) {
      LocalPlayer var2 = (LocalPlayer)(Object)this;
      Minecraft var3 = Minecraft.getInstance();
      if (var3 != null && var2 == var3.player && AuditClientFixes.isNoClipActive() && !var2.isSpectator() && !var2.isDeadOrDying() && !var2.isSleeping()) {
         if (this.crouching) {
            this.crouching = false;
         }

         if (var2.getControlledVehicle() != null
            && !AuditClientFixes.isBoatFlyEnabled()
            && var2.input != null
            && var2.input.keyPresses != null
            && var2.input.keyPresses.shift()) {
            Input var4 = var2.input.keyPresses;
            var2.input.keyPresses = new Input(var4.forward(), var4.backward(), var4.left(), var4.right(), var4.jump(), false, var4.sprint());
         }
      }
   }
}
