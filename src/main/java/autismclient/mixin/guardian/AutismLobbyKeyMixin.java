package autismclient.mixin.guardian;

import autismclient.util.mm.BufferHelper;
import autismclient.util.mm.guardian.impl.LobbyUnwrap;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BufferHelper.class)
public class AutismLobbyKeyMixin {
   @ModifyReturnValue(method = "resolveLobbyKey", at = @At("RETURN"))
   private static byte[] autism$unwrap(byte[] original, JsonObject b) {
      return LobbyUnwrap.apply(original, b);
   }
}
