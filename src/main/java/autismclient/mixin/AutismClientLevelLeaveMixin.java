package autismclient.mixin;

import autismclient.util.macro.MacroExecutor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class AutismClientLevelLeaveMixin {
   @Inject(method = "disconnect", at = @At("HEAD"))
   private void autism$notePlayerLeave(Component message, CallbackInfo ci) {
      MacroExecutor.write_V();
   }
}
