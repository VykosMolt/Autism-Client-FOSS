package autismclient.mixin;

import autismclient.aggressivepvp.Rotations;
import autismclient.aggressivepvp.Versions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The Aggressive Crystal engine's send point and its one-tick silent look.
 * Completely idle unless the Aggressive engine queued a rotation this tick.
 */
@Mixin(LocalPlayer.class)
public abstract class AggressiveLocalPlayerMixin {
    @Inject(method = "aiStep()V", at = @At("HEAD"))
    private void ap$movementTick(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        if (self != Minecraft.getInstance().player) return;
        Rotations.movingBySentYaw = false;
        Rotations.clearPending();
        Versions.onMovementTick();

        Float sent = Rotations.movementYaw();
        if (sent != null) {
            Rotations.cameraYaw = self.getYRot();
            Rotations.movingBySentYaw = true;
            self.setYRot(sent);
        }
    }

    @Inject(method = "aiStep()V", at = @At("RETURN"))
    private void ap$movementTickEnd(CallbackInfo ci) {
        if ((Object) this != Minecraft.getInstance().player || !Rotations.movingBySentYaw) return;
        Rotations.movingBySentYaw = false;
        ((LocalPlayer) (Object) this).setYRot(Rotations.cameraYaw);
    }

    @Inject(method = "sendPosition()V", at = @At("HEAD"))
    private void ap$beforeSend(CallbackInfo ci) {
        if ((Object) this == Minecraft.getInstance().player) Rotations.beforeSend();
    }

    @Inject(method = "sendPosition()V", at = @At("TAIL"))
    private void ap$afterSend(CallbackInfo ci) {
        if ((Object) this == Minecraft.getInstance().player) Rotations.afterSend();
    }
}
