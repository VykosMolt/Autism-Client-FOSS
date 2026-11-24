package autismclient.mixin.via;

import autismclient.compat.a.DataUtil;
import io.netty.channel.ChannelFuture;
import java.net.InetSocketAddress;
import net.minecraft.network.Connection;
import net.minecraft.server.network.EventLoopGroupHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Connection.class, priority = 1100)
public abstract class AutismViaConnectionMixin {
   @Inject(
      method = "connect(Ljava/net/InetSocketAddress;Lnet/minecraft/server/network/EventLoopGroupHolder;Lnet/minecraft/network/Connection;)Lio/netty/channel/ChannelFuture;",
      at = @At("HEAD")
   )
   private static void autism$selectNativeProtocol(
      InetSocketAddress address, EventLoopGroupHolder eventLoopGroup, Connection connection, CallbackInfoReturnable<ChannelFuture> cir
   ) {
      DataUtil.init_V(connection);
   }
}
