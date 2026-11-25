package autismclient.util.g;

import autismclient.util.C;
import autismclient.util.ProxyEntry;
import autismclient.util.ProxyType;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.Connection;

public final class ConfigStore {
   private static final ThreadLocal<Boolean> init_ThreadLocal = new ThreadLocal<>();
   private static final ThreadLocal<ConfigStore.Inner_DataUtil> load = new ThreadLocal<>();
   private static final AttributeKey<Boolean> init_AttributeKey = AttributeKey.valueOf("autismclient:multi_connection");
   private static final Map<Connection, ConfigStore.Inner_StreamUtil> init_Map = new ConcurrentHashMap<>();

   public static ConfigStore.Inner_DataUtil init_ConfigStore_DataUt(ConfigStore.Inner_DataUtil proxy) {
      init_ThreadLocal.set(Boolean.TRUE);
      ConfigStore.Inner_DataUtil previous = load.get();
      if (proxy == null) {
         load.remove();
      } else {
         load.set(proxy);
      }

      return previous;
   }

   public static void init_V(ConfigStore.Inner_DataUtil previous) {
      init_ThreadLocal.remove();
      if (previous == null) {
         load.remove();
      } else {
         load.set(previous);
      }
   }

   public static boolean init_Z() {
      return Boolean.TRUE.equals(init_ThreadLocal.get());
   }

   public static ConfigStore.Inner_DataUtil init_ConfigStore_DataUt() {
      return load.get();
   }

   private ConfigStore() {
   }

   public static void init(Connection connection, ProxyEntry proxy) {
      if (connection != null) {
         ConfigStore.Inner_DataUtil spec = ConfigStore.Inner_DataUtil.init(proxy);
         if (connection instanceof C marker) {
            marker.autism$setMultiManaged(spec);
         } else {
            init_Map.put(connection, new ConfigStore.Inner_StreamUtil(spec));
         }
      }
   }

   public static boolean init_Z(Connection connection) {
      return connection instanceof C marker ? marker.autism$isMultiManaged() : connection != null && init_Map.containsKey(connection);
   }

   public static void init(Connection connection, Channel channel) {
      if (init_Z(connection) && channel != null) {
         channel.attr(init_AttributeKey).set(Boolean.TRUE);
      }
   }

   public static boolean init_Z(Channel channel) {
      return channel != null && Boolean.TRUE.equals(channel.attr(init_AttributeKey).get());
   }

   public static void init_V(Channel channel) {
      if (channel != null) {
         channel.attr(init_AttributeKey).set(null);
      }
   }

   public static ConfigStore.Inner_DataUtil init_ConfigStore_DataUt(Connection connection) {
      if (connection instanceof C marker) {
         return marker.autism$multiProxy();
      } else {
         ConfigStore.Inner_StreamUtil holder = connection == null ? null : init_Map.get(connection);
         return holder == null ? null : holder.init;
      }
   }

   public static void init_V(Connection connection) {
      if (connection instanceof C marker) {
         marker.autism$clearMultiManaged();
      } else if (connection != null) {
         init_Map.remove(connection);
      }
   }

   public record Inner_DataUtil(ProxyType init_bZ, String init_Str, int init_I, String load, String read) {
      public static ConfigStore.Inner_DataUtil init(ProxyEntry proxy) {
         return proxy == null
            ? null
            : new ConfigStore.Inner_DataUtil(
               proxy.init_bZ,
               proxy.read == null ? "" : proxy.read,
               proxy.init_I,
               proxy.write == null ? "" : proxy.write,
               proxy.update == null ? "" : proxy.update
            );
      }

      public ProxyEntry init_bW() {
         ProxyEntry proxy = new ProxyEntry();
         proxy.init_bZ = this.init_bZ;
         proxy.read = this.init_Str == null ? "" : this.init_Str;
         proxy.init_I = this.init_I;
         proxy.write = this.load == null ? "" : this.load;
         proxy.update = this.read == null ? "" : this.read;
         return proxy;
      }
   }

   private record Inner_StreamUtil(ConfigStore.Inner_DataUtil init) {
   }
}
