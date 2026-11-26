package autismclient.mixin;

import autismclient.protector.ConnectionPrivacyRegistry;
import autismclient.util.bW;
import autismclient.util.ProxyListManager;
import autismclient.util.ProxyProtocolType;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.local.LocalChannel;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Connection.class, priority = 2000)
public abstract class AutismConnectionProxyMixin {
   @Inject(method = "configurePacketHandler", at = @At("HEAD"))
   private void autism$applyProxy(ChannelPipeline pipeline, CallbackInfo ci) {
      Connection connection = (Connection)(Object)this;
      if (connection.getReceiving() == PacketFlow.CLIENTBOUND && !(pipeline.channel() instanceof LocalChannel)) {
         ProxyListManager manager = ProxyListManager.init_bY();
         ConnectionPrivacyRegistry.ProxyEndpoint route;
         synchronized (manager) {
            bW main = manager.init_bW();
            route = main == null ? null : new ConnectionPrivacyRegistry.ProxyEndpoint(main.init_bZ, main.read, main.init_I, main.write, main.update);
         }

         ConnectionPrivacyRegistry.bindProxy(connection, route);
         if (route != null) {
            autism$removeAllProxyHandlers(pipeline);
            autism$installProxy(pipeline, route.type(), route.host(), route.port(), route.username(), route.password());
         }
      }
   }

   @Unique
   private static void autism$installProxy(ChannelPipeline pipeline, ProxyProtocolType type, String address, int port, String username, String password) {
      if (address != null && !address.isBlank() && port > 0) {
         InetSocketAddress target = new InetSocketAddress(address, port);
         String user = username == null ? "" : username;
         String pass = password == null ? "" : password;
         switch (type) {
            case Socks4:
               pipeline.addFirst("autism_socks4_proxy", new Socks4ProxyHandler(target, user));
               break;
            case Socks5:
               pipeline.addFirst("autism_socks5_proxy", new Socks5ProxyHandler(target, user, pass));
         }
      }
   }

   @Unique
   private static void autism$removeAllProxyHandlers(ChannelPipeline pipeline) {
      for (String name : new ArrayList<>(pipeline.names())) {
         if (pipeline.get(name) instanceof ProxyHandler) {
            pipeline.remove(name);
         }
      }
   }
}
