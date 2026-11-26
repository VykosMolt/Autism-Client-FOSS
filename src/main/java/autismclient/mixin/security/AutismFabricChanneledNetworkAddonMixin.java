package autismclient.mixin.security;

import autismclient.g.OutboundChannelPolicy;
import net.fabricmc.fabric.impl.networking.AbstractChanneledNetworkAddon;
import net.fabricmc.fabric.impl.networking.CommonRegisterPayload;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractChanneledNetworkAddon.class)
public abstract class AutismFabricChanneledNetworkAddonMixin {
   @Shadow
   @Final
   protected Connection connection;

   @Inject(method = "onCommonVersionPacket", at = @At("HEAD"), cancellable = true)
   private void autism$gateCommonVersion(int version, CallbackInfo ci) {
      if (!OutboundChannelPolicy.init_Z(this.connection)) {
         ci.cancel();
      }
   }

   @Inject(method = "onCommonRegisterPacket", at = @At("HEAD"), cancellable = true)
   private void autism$gateCommonRegister(CommonRegisterPayload payload, CallbackInfo ci) {
      if (!OutboundChannelPolicy.init_Z(this.connection)) {
         ci.cancel();
      }
   }
}
