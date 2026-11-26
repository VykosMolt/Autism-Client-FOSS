package autismclient.mixin.security;

import autismclient.g.OutboundChannelPolicy;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientHandshakePacketListenerImpl.class)
public abstract class AutismProtectorClientHandshakePacketListenerMixin {
   @WrapOperation(
      method = "handleCustomQuery",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;)V")
   )
   private void autism$authorizeVanillaQueryAnswer(Connection connection, Packet<?> packet, Operation<Void> original) {
      if (packet instanceof ServerboundCustomQueryAnswerPacket answer) {
         OutboundChannelPolicy.init(connection, answer);
      }

      original.call(connection, packet);
   }
}
