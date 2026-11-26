package autismclient.protector;

import autismclient.util.AutismConfig;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.protocol.Packet;

public final class ProtectorFeatureFlags {
   private static final String init_Str = "opsec";
   private static final String load_Str = "exploitpreventer";
   private static final boolean init_Z;
   private static final boolean load_Z;
   private static final int init_I = 1;
   private static final int load_I = 4;
   private static final int read = 8;
   private static final int write = 16;
   private static final int update = 32;
   private static final int sync = 64;
   private static final int flush = 128;
   private static final int close = 256;
   private static final int open = 512;
   private static final ProtectorFeatureFlags.Inner_DataUtil init_HashUtil_DataUtil = new ProtectorFeatureFlags.Inner_DataUtil(false, null, 0, 0);
   private static volatile ProtectorFeatureFlags.Inner_DataUtil load_HashUtil_DataUtil = init_HashUtil_DataUtil;
   private static final int poll = 1024;
   private static final Map<Object, Boolean> init_Map;

   private ProtectorFeatureFlags() {
   }

   public static boolean init_Z() {
      return load();
   }

   public static boolean load() {
      return init_Z;
   }

   public static boolean read() {
      return init_Z || load_Z;
   }

   public static boolean write() {
      return load_Z && !init_Z;
   }

   public static boolean update() {
      return init(1);
   }

   public static boolean sync() {
      return init(512);
   }

   public static boolean flush() {
      return init(4);
   }

   public static boolean close() {
      return init(8);
   }

   public static boolean open() {
      return init(8);
   }

   public static boolean poll() {
      return init(16);
   }

   public static boolean push() {
      return init(32);
   }

   public static boolean pop() {
      return init(64);
   }

   public static boolean tick() {
      return init(128);
   }

   public static boolean run() {
      return init(256);
   }

   public static void init_V() {
      init_V(AutismConfig.getGlobal());
   }

   public static void init_V(AutismConfig config) {
      if (config == null) {
         load_HashUtil_DataUtil = init_HashUtil_DataUtil;
      } else {
         int sourceFlags = init_I(config);
         ProtectorFeatureFlags.Inner_DataUtil current = load_HashUtil_DataUtil;
         if (!current.init_Z || current.init_AutismConfig != config || current.init_I != sourceFlags) {
            load_HashUtil_DataUtil = new ProtectorFeatureFlags.Inner_DataUtil(true, config, sourceFlags, init(sourceFlags, init_Z, read()));
         }
      }
   }

   private static boolean init(int flag) {
      return (init_HashUtil_DataUtil().load & flag) != 0;
   }

   private static ProtectorFeatureFlags.Inner_DataUtil init_HashUtil_DataUtil() {
      ProtectorFeatureFlags.Inner_DataUtil state = load_HashUtil_DataUtil;
      if (state.init_Z) {
         return state;
      }

      init_V(AutismConfig.getGlobal());
      return load_HashUtil_DataUtil;
   }

   static int init_I(AutismConfig config) {
      int flags = 0;
      if (config.protectorEnabled) {
         flags |= 1;
      }

      if (config.protectorFilterChannels) {
         flags |= 4;
      }

      if (config.protectorTranslationProtection) {
         flags |= 8;
      }

      if (config.protectorDisableTelemetry) {
         flags |= 16;
      }

      if (config.protectorBlockLocalUrls) {
         flags |= 32;
      }

      if (config.protectorIsolatePackCache) {
         flags |= 64;
      }

      if (config.protectorStripServerPacks) {
         flags |= 128;
      }

      if (config.protectorChatSigningOff) {
         flags |= 256;
      }

      if (config.spoofClientVanilla) {
         flags |= 512;
      }

      return flags;
   }

   static int init(int sourceFlags, boolean fullExternalProtector, boolean overlapExternalProtector) {
      if (!fullExternalProtector && (sourceFlags & 1) != 0) {
         int flags = sourceFlags;
         if (overlapExternalProtector) {
            flags &= -105;
         }

         return flags;
      } else {
         return 0;
      }
   }

   static Object init_Obj() {
      return load_HashUtil_DataUtil;
   }

   public static void init_V(Packet<?> packet) {
      if (packet != null) {
         synchronized (init_Map) {
            if (init_Map.size() >= 1024) {
               Iterator<Entry<Object, Boolean>> it = init_Map.entrySet().iterator();
               if (it.hasNext()) {
                  it.next();
                  it.remove();
               }
            }

            init_Map.put(packet, Boolean.TRUE);
         }
      }
   }

   public static boolean init_Z(Packet<?> packet) {
      return packet == null ? false : init_Map.containsKey(packet);
   }

   public static void load(Packet<?> packet) {
      if (packet != null) {
         init_Map.remove(packet);
      }
   }

   static {
      boolean opsec = false;
      boolean exploitPreventer = false;

      try {
         FabricLoader loader = FabricLoader.getInstance();
         opsec = loader.isModLoaded("opsec");
         exploitPreventer = loader.isModLoaded("exploitpreventer");
      } catch (Throwable var3) {
      }

      init_Z = opsec;
      load_Z = exploitPreventer;
      init_Map = Collections.synchronizedMap(new IdentityHashMap<>());
   }

   private record Inner_DataUtil(boolean init_Z, AutismConfig init_AutismConfig, int init_I, int load) {
   }
}
