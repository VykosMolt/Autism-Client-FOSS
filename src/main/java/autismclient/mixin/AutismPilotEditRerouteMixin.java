package autismclient.mixin;

import autismclient.util.g.PoolEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public class AutismPilotEditRerouteMixin {
   @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
   private void autism$rerouteEditsToBot(Packet<?> packet, CallbackInfo ci) {
      if ((packet instanceof ServerboundSignUpdatePacket || packet instanceof ServerboundEditBookPacket) && PoolEntry.init_Z()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.getConnection() == (Object)this && PoolEntry.init_Z_Packet(packet)) {
            ci.cancel();
         }
      }
   }
}
