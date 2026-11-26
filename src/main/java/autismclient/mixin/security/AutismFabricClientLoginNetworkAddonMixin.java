package autismclient.mixin.security;

import autismclient.g.OutboundChannelPolicy;
import net.fabricmc.fabric.impl.networking.client.ClientLoginNetworkAddon;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLoginNetworkAddon.class)
public abstract class AutismFabricClientLoginNetworkAddonMixin {
   @Shadow
   @Final
   private ClientHandshakePacketListenerImpl listener;

   @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
   private void autism$gateLoginQuery(ClientboundCustomQueryPacket packet, CallbackInfoReturnable<Boolean> cir) {
      if (!OutboundChannelPolicy.init(this.listener)) {
         cir.setReturnValue(false);
      }
   }
}
