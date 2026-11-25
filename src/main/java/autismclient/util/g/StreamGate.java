package autismclient.util.g;

import autismclient.util.bW;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Locale;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.Connection;

final class StreamGate {
   private StreamGate() {
   }

   static StreamGate.Inner_CachePool init() {
      if (!FabricLoader.getInstance().isModLoaded("viafabricplus")) {
         return new StreamGate.Inner_CachePool(false, null, "Native");
      }

      try {
         Class<?> apiClass = Class.forName("com.viaversion.viafabricplus.ViaFabricPlus");
         Object api = apiClass.getMethod("getImpl").invoke(null);
         Method targetMethod = api.getClass().getMethod("getTargetVersion");
         Object target = targetMethod.invoke(api);
         return new StreamGate.Inner_CachePool(true, target, String.valueOf(target));
      } catch (ReflectiveOperationException ignored) {
         return new StreamGate.Inner_CachePool(true, null, "ViaFabricPlus");
      }
   }

   static StreamGate.Inner_CachePool init(ServerData serverData) {
      if (serverData != null && FabricLoader.getInstance().isModLoaded("viafabricplus")) {
         try {
            Class<?> apiClass = Class.forName("com.viaversion.viafabricplus.ViaFabricPlus");
            Object api = apiClass.getMethod("getImpl").invoke(null);
            Object target = api.getClass().getMethod("getServerVersion", ServerData.class).invoke(api, serverData);
            return new StreamGate.Inner_CachePool(true, target, String.valueOf(target));
         } catch (ReflectiveOperationException ignored) {
            return new StreamGate.Inner_CachePool(true, null, "ViaFabricPlus Auto Detect");
         }
      } else {
         return new StreamGate.Inner_CachePool(false, null, "Native");
      }
   }

   static boolean init_Z(StreamGate.Inner_CachePool target) {
      if (target == null) {
         return false;
      }

      String label = target.init_Str().toLowerCase(Locale.ROOT);
      return label.contains("auto") && label.contains("detect");
   }

   static StreamGate.Inner_CachePool init(StreamGate.Inner_CachePool global, StreamGate.Inner_CachePool serverOverride) {
      return serverOverride != null && serverOverride.init_Obj() != null ? serverOverride : global;
   }

   static StreamGate.Inner_CachePool init(StreamGate.Inner_CachePool selected, InetSocketAddress address, String host, int port, bW proxy) throws Exception {
      return init(
         selected,
         () -> {
            Class<?> detector = Class.forName("com.viaversion.viafabricplus.protocoltranslator.util.ProtocolVersionDetector");
            Class<?> protocol = Class.forName("com.viaversion.viaversion.api.protocol.version.ProtocolVersion");
            Object nativeVersion = init(Class::forName);

            try (autismclient.compat.a.DataUtil.CachePool ignored = autismclient.compat.a.DataUtil.init(proxy, address)) {
               return detector.getMethod("get", ServerAddress.class, InetSocketAddress.class, protocol)
                  .invoke(null, new ServerAddress(host, port), address, nativeVersion);
            }
         }
      );
   }

   static Object init(StreamGate.Inner_DataUtil classes) throws ReflectiveOperationException {
      try {
         return classes.load("com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator").getField("NATIVE_VERSION").get(null);
      } catch (ClassNotFoundException | NoSuchFieldException olderApiAbsent) {
         return classes.load("com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslationImpl").getField("NATIVE_VERSION").get(null);
      }
   }

   static StreamGate.Inner_CachePool init(StreamGate.Inner_CachePool selected, StreamGate.Inner_StreamUtil detector) throws Exception {
      if (!init_Z(selected)) {
         return selected;
      } else {
         Object version = detector.detect();
         if (version == null) {
            throw new IOException("AUTO protocol detection returned no version");
         } else {
            StreamGate.Inner_CachePool resolved = new StreamGate.Inner_CachePool(true, version, String.valueOf(version));
            if (init_Z(resolved)) {
               throw new IOException("AUTO protocol detection did not resolve a server version");
            } else {
               String unsupported = init_Str(resolved);
               if (!unsupported.isBlank()) {
                  throw new IOException(unsupported);
               } else {
                  return resolved;
               }
            }
         }
      }
   }

   static String init_Str(StreamGate.Inner_CachePool target) {
      if (target == null || !target.init_Z()) {
         return "";
      } else if (target.init_Obj() == null) {
         return "Cannot read the selected ViaFabricPlus protocol; check its compatibility/version";
      } else {
         String label = target.init_Str().toLowerCase(Locale.ROOT);
         if (label.contains("bedrock")) {
            return "Multi does not support ViaFabricPlus Bedrock targets";
         } else {
            return label.contains("classicube") ? "Multi supports offline Classic, not authenticated ClassiCube" : "";
         }
      }
   }

   static void init(Connection connection, StreamGate.Inner_CachePool target) {
      if (connection != null && target != null && target.init_Z()) {
         if (target.init_Obj() == null || init_Z(target)) {
            throw new IllegalStateException("ViaFabricPlus protocol must be resolved before connecting");
         }

         try {
            Class<?> access = Class.forName("com.viaversion.viafabricplus.injection.access.core.IConnection");
            Class<?> protocol = Class.forName("com.viaversion.viaversion.api.protocol.version.ProtocolVersion");
            access.getMethod("viaFabricPlus$setTargetVersion", protocol).invoke(connection, target.init_Obj());
         } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Could not apply this bot's ViaFabricPlus protocol", error);
         }
      }
   }

   record Inner_CachePool(boolean init_Z, Object init_Obj, String init_Str) {
   }

   @FunctionalInterface
   interface Inner_DataUtil {
      Class<?> load(String var1) throws ClassNotFoundException;
   }

   @FunctionalInterface
   interface Inner_StreamUtil {
      Object detect() throws Exception;
   }
}
