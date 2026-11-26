package autismclient.mixin.security;

import autismclient.protector.ConnectionPrivacyRegistry;
import autismclient.protector.RegistryCompatGuard;
import autismclient.mixin.accessor.ClientCommonNetworkHandlerAccessor;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.client.multiplayer.RegistryDataCollector;
import net.minecraft.core.RegistryAccess.Frozen;
import net.minecraft.network.Connection;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientConfigurationPacketListenerImpl.class)
public abstract class AutismClientConfigurationRegistryMixin {
   @WrapOperation(
      method = "lambda$handleConfigurationFinished$0",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/multiplayer/RegistryDataCollector;collectGameRegistries(Lnet/minecraft/server/packs/resources/ResourceProvider;Lnet/minecraft/core/RegistryAccess$Frozen;Z)Lnet/minecraft/core/RegistryAccess$Frozen;"
      )
   )
   private Frozen autism$collectGameRegistriesWithConnectionScope(
      RegistryDataCollector collector,
      ResourceProvider knownDataSource,
      Frozen originalRegistries,
      boolean synchronizedRegistriesOnly,
      Operation<Frozen> original
   ) {
      Connection connection = ((ClientCommonNetworkHandlerAccessor)(Object)this).getConnection();
      RegistryCompatGuard.init_V(ConnectionPrivacyRegistry.init_BufferHelper_Strea(connection));

      try {
         return original.call(collector, knownDataSource, originalRegistries, synchronizedRegistriesOnly);
      } finally {
         RegistryCompatGuard.init_V();
      }
   }
}
