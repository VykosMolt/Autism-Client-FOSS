package autismclient.mixin.security;

import autismclient.protector.ConnectionPrivacyRegistry;
import autismclient.protector.ResourcePackSessionTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class AutismProtectorClientCommonNetworkHandlerMixin {
   @Shadow
   @Final
   protected Minecraft minecraft;
   @Shadow
   @Final
   protected Connection connection;

   @Inject(method = "handleResourcePackPop", at = @At("HEAD"))
   private void autism$onPackPop(ClientboundResourcePackPopPacket packet, CallbackInfo ci) {
      if (this.minecraft.isSameThread()) {
         ResourcePackSessionTracker.init(ConnectionPrivacyRegistry.getActive(this.connection), packet.id().orElse(null));
      }
   }
}
