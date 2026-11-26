package autismclient.mixin;

import autismclient.protector.ConnectionPrivacyRegistry;
import autismclient.protector.VanillaKnownPacksManager;
import autismclient.util.AutismSessionState;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.client.multiplayer.KnownPacksManager;
import net.minecraft.network.protocol.configuration.ClientboundSelectKnownPacks;
import net.minecraft.network.protocol.configuration.ServerboundSelectKnownPacks;
import net.minecraft.server.packs.repository.KnownPack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConfigurationPacketListenerImpl.class)
public abstract class AutismClientConfigurationNetworkHandlerMixin {
   @Shadow
   private KnownPacksManager knownPacks;

   @Inject(method = "handleSelectKnownPacks", at = @At("HEAD"), cancellable = true)
   private void yang$onSelectKnownPacks(ClientboundSelectKnownPacks packet, CallbackInfo ci) {
      if (packet != null && packet.knownPacks() != null) {
         String serverAddress = "";
         Minecraft client = Minecraft.getInstance();
         if (client != null && client.getCurrentServer() != null && client.getCurrentServer().ip != null) {
            serverAddress = client.getCurrentServer().ip;
         }

         String capturedVersion = null;

         for (KnownPack knownPack : packet.knownPacks()) {
            if (knownPack != null) {
               if ("core".equals(knownPack.id())) {
                  AutismSessionState.init_ck().init_V_Str_Str(serverAddress, knownPack.version());
                  break;
               }

               if (capturedVersion == null && !knownPack.id().isBlank() && !knownPack.version().isBlank()) {
                  capturedVersion = knownPack.version();
               }
            }
         }

         if (capturedVersion != null) {
            AutismSessionState.init_ck().init_V_Str_Str(serverAddress, capturedVersion);
         }

         if (client != null && client.isSameThread()) {
            ClientConfigurationPacketListenerImpl listener = (ClientConfigurationPacketListenerImpl)(Object)this;
            ConnectionPrivacyRegistry.ConnectionSession context = ConnectionPrivacyRegistry.init(listener);
            if (VanillaKnownPacksManager.init(context)) {
               if (!(this.knownPacks instanceof VanillaKnownPacksManager)) {
                  this.knownPacks = new VanillaKnownPacksManager();
               }

               List<KnownPack> selected = this.knownPacks.trySelectingPacks(packet.knownPacks());
               listener.send(new ServerboundSelectKnownPacks(selected));
               ci.cancel();
            }
         }
      }
   }
}
