package autismclient.mixin.security;

import autismclient.protector.ConnectionPrivacyRegistry;
import autismclient.protector.ModdedParticleGuard;
import autismclient.protector.PacketDecodeOriginStack;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.network.PacketProcessor$ListenerAndPacket")
public class AutismProtectorPacketProcessorMixin {
   @WrapOperation(
      method = "handle",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/Packet;handle(Lnet/minecraft/network/PacketListener;)V")
   )
   private <T extends PacketListener> void autism$wrapHandle(Packet<?> instance, T listener, Operation<Void> original) {
      if (!(instance instanceof ClientboundLevelParticlesPacket particles && ModdedParticleGuard.isPlaceholder(particles))) {
         if (!PacketDecodeOriginStack.tracks(instance)) {
            original.call(instance, listener);
         } else {
            PacketDecodeOriginStack.push(ConnectionPrivacyRegistry.getByListener(listener));

            try {
               original.call(instance, listener);
            } finally {
               PacketDecodeOriginStack.pushOrPop(false);
            }
         }
      }
   }
}
