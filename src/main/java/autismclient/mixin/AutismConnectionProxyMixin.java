package autismclient.mixin;

import autismclient.g.BufferHelper;
import autismclient.util.AutismLiteVariant;
import autismclient.util.bW;
import autismclient.util.bY;
import autismclient.util.bZ;
import autismclient.util.g.ConfigStore;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.local.LocalChannel;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.BandwidthDebugMonitor;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Connection.class, priority = 2000)
public abstract class AutismConnectionProxyMixin {
   @Inject(method = "configureSerialization", at = @At("HEAD"))
   private static void autism$disableMeteorProxyBeforeHandlers(
      ChannelPipeline pipeline, PacketFlow inboundDirection, boolean local, @Nullable BandwidthDebugMonitor monitor, CallbackInfo ci
   ) {
      if (!local && inboundDirection == PacketFlow.CLIENTBOUND) {
         autism$disableMeteorProxy();
      }
   }

   @Inject(method = "configurePacketHandler", at = @At("HEAD"))
   private void autism$applyProxy(ChannelPipeline pipeline, CallbackInfo ci) {
      Connection connection = (Connection)(Object)this;
      if (connection.getReceiving() == PacketFlow.CLIENTBOUND && !(pipeline.channel() instanceof LocalChannel)) {
         if (!AutismLiteVariant.enabled() && ConfigStore.init_Z(connection)) {
            autism$removeAllProxyHandlers(pipeline);
            ConfigStore.Inner_DataUtil proxy = ConfigStore.init_ConfigStore_DataUt(connection);
            BufferHelper.init(
               connection,
               proxy == null ? null : new BufferHelper.Inner_ConfigStore(proxy.init_bZ(), proxy.init_Str(), proxy.init_I(), proxy.load(), proxy.read())
            );
            if (proxy != null && !proxy.init_Str().isBlank() && proxy.init_I() > 0) {
               autism$installProxy(pipeline, proxy.init_bZ(), proxy.init_Str(), proxy.init_I(), proxy.load(), proxy.read());
            }
         } else {
            bY manager = bY.init_bY();
            BufferHelper.Inner_ConfigStore route;
            synchronized (manager) {
               bW main = manager.init_bW();
               route = main == null ? null : new BufferHelper.Inner_ConfigStore(main.init_bZ, main.read, main.init_I, main.write, main.update);
            }

            BufferHelper.init(connection, route);
            if (route != null) {
               autism$removeAllProxyHandlers(pipeline);
               autism$installProxy(pipeline, route.init_bZ(), route.init_Str(), route.init_I(), route.load(), route.read());
            }
         }
      }
   }

   @Unique
   private static void autism$installProxy(ChannelPipeline pipeline, bZ type, String address, int port, String username, String password) {
      if (address != null && !address.isBlank() && port > 0) {
         InetSocketAddress target = new InetSocketAddress(address, port);
         String user = username == null ? "" : username;
         String pass = password == null ? "" : password;
         switch (type) {
            case init:
               pipeline.addFirst("autism_socks4_proxy", new Socks4ProxyHandler(target, user));
               break;
            case load:
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

   private static void autism$disableMeteorProxy() {
      if (FabricLoader.getInstance().isModLoaded("meteor-client")) {
         try {
            Class<?> proxiesClass = Class.forName("meteordevelopment.meteorclient.systems.proxies.Proxies");
            Class<?> proxyClass = Class.forName("meteordevelopment.meteorclient.systems.proxies.Proxy");
            Object proxies = proxiesClass.getMethod("get").invoke(null);
            Object enabled = proxiesClass.getMethod("getEnabled").invoke(proxies);
            if (enabled != null) {
               proxiesClass.getMethod("setEnabled", proxyClass, boolean.class).invoke(proxies, enabled, false);
            }
         } catch (ReflectiveOperationException var4) {
         }
      }
   }
}
