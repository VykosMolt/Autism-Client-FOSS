package autismclient.mixin.security;

import autismclient.protector.ProtectorFeatureFlags;
import com.mojang.authlib.minecraft.TelemetrySession;
import com.mojang.authlib.services.MinecraftServicesUserApiService;
import java.util.concurrent.Executor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MinecraftServicesUserApiService.class, remap = false)
public class AutismProtectorYggdrasilMixin {
   @Inject(method = "newTelemetrySession", at = @At("HEAD"), cancellable = true)
   private void autism$disableTelemetrySession(Executor executor, CallbackInfoReturnable<TelemetrySession> info) {
      if (ProtectorFeatureFlags.poll()) {
         info.setReturnValue(TelemetrySession.DISABLED);
      }
   }
}
