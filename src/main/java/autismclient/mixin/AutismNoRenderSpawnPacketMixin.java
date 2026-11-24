package autismclient.mixin;

import autismclient.modules.az;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class AutismNoRenderSpawnPacketMixin {
   @Inject(method = "handleAddEntity", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$dropSpawnPacket(ClientboundAddEntityPacket packet, CallbackInfo ci) {
      if (packet != null && az.load(packet.getType())) {
         ci.cancel();
      }
   }
}
