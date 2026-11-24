package autismclient.mixin;

import autismclient.aggressivepvp.ActionWindow;
import autismclient.aggressivepvp.MoveFix;
import autismclient.aggressivepvp.Rotations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keys turned to the sent yaw in a tick the Aggressive Crystal engine turned the look, and held up in a tick Auto Totem asked for. */
@Mixin(KeyboardInput.class)
public abstract class AggressiveKeyboardInputMixin extends ClientInput {
    @Inject(method = "tick()V", at = @At("TAIL"))
    private void ap$keys(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (Rotations.movingBySentYaw && mc.player != null) {
            boolean[] keys = MoveFix.remap(keyPresses.forward(), keyPresses.backward(), keyPresses.left(), keyPresses.right(), Rotations.cameraYaw, mc.player.getYRot());
            keyPresses = new Input(keys[0], keys[1], keys[2], keys[3], keyPresses.jump(), keyPresses.shift(), keyPresses.sprint());
            moveVector = new Vec2((keys[2] ? 1 : 0) - (keys[3] ? 1 : 0), (keys[0] ? 1 : 0) - (keys[1] ? 1 : 0)).normalized();
        }
        if (ActionWindow.inputHeld()) {
            keyPresses = Input.EMPTY;
            moveVector = Vec2.ZERO;
            if (mc.player != null) mc.player.setSprinting(false);
        }
    }
}
