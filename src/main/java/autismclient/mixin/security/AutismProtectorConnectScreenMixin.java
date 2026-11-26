package autismclient.mixin.security;

import autismclient.protector.ConnectionPrivacyRegistry;
import autismclient.mixin.accessor.AutismDownloadedPackSourceAccessor;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.resources.server.DownloadedPackSource;
import net.minecraft.client.resources.server.ServerPackManager;
import net.minecraft.client.resources.server.ServerPackManager.PackPromptStatus;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.client.gui.screens.ConnectScreen$1")
public abstract class AutismProtectorConnectScreenMixin {
   @Shadow
   @Final
   private ServerAddress val$hostAndPort;
   @Shadow
   @Final
   private ServerData val$server;

   @WrapOperation(
      method = "run",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/resources/server/DownloadedPackSource;configureForServerControl(Lnet/minecraft/network/Connection;Lnet/minecraft/client/resources/server/ServerPackManager$PackPromptStatus;)V"
      )
   )
   private void autism$associatePreSrvOrigin(DownloadedPackSource source, Connection connection, PackPromptStatus status, Operation<Void> original) {
      ServerPackManager manager = ((AutismDownloadedPackSourceAccessor)source).autism$manager();
      ConnectionPrivacyRegistry.init(
         connection,
         this.val$hostAndPort.getHost(),
         this.val$hostAndPort.getPort(),
         this.val$server == null ? ServerData.Type.OTHER : this.val$server.type(),
         manager
      );
      original.call(source, connection, status);
   }
}
