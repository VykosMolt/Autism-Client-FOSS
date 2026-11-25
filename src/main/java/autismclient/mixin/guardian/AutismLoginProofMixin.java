package autismclient.mixin.guardian;

import autismclient.util.L;
import autismclient.util.mm.guardian.impl.LoginProof;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(L.class)
public class AutismLoginProofMixin {
   @ModifyArg(
      method = {"exchangeCode", "freshJwt"},
      at = @At(value = "INVOKE", target = "Lautismclient/util/AutismHttp;postForm(Ljava/lang/String;Ljava/lang/String;)Lcom/google/gson/JsonObject;"),
      index = 1
   )
   private static String autism$appendProof(String body) {
      return body + LoginProof.build();
   }
}
