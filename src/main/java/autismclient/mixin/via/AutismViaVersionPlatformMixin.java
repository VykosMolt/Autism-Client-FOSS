package autismclient.mixin.via;

import autismclient.compat.via.ViaFabricPlusBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.viaversion.viafabricplus.protocoltranslator.impl.platform.ViaFabricPlusViaVersionPlatform", remap = false)
public abstract class AutismViaVersionPlatformMixin {
   @Inject(method = "sendCustomPayload", at = @At("HEAD"), cancellable = true, remap = false)
   private void autism$filterViaCustomPayload(@Coerce Object userConnection, String channel, byte[] data, CallbackInfo ci) {
      if (!ViaFabricPlusBridge.init(userConnection, channel, data)) {
         ci.cancel();
      }
   }
}
