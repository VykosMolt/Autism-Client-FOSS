package autismclient.util;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;
import net.minecraft.client.Minecraft;

public final class MinecraftProxySelector {
   private static final ProxySelector init_ProxySelector = new ProxySelector() {
      @Override
      public List<Proxy> select(URI uri) {
         if (uri == null) {
            throw new IllegalArgumentException("URI is required");
         } else {
            return List.of(MinecraftProxySelector.currentProxy());
         }
      }

      @Override
      public void connectFailed(URI uri, SocketAddress address, IOException error) {
      }
   };

   private MinecraftProxySelector() {
   }

   // Account and session requests carry tokens, so they use the game's proxy like vanilla does.
   public static Proxy currentProxy() {
      Minecraft mc = Minecraft.getInstance();
      return mc == null ? Proxy.NO_PROXY : mc.getProxy();
   }

   public static ProxySelector init_ProxySelector() {
      return init_ProxySelector;
   }
}
