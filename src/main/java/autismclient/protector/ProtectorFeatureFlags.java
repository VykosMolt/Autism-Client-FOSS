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
   private static final String OPSEC_ID = "opsec";
   private static final String EXPLOIT_PREVENTER_ID = "exploitpreventer";
   private static final boolean init_Z;
   private static final boolean load_Z;
   private static final int FLAG_ENABLED = 1;
   private static final int FLAG_FILTER_CHANNELS = 4;
   private static final int read = 8;
   private static final int write = 16;
   private static final int update = 32;
   private static final int sync = 64;
   private static final int flush = 128;
   private static final int close = 256;
   private static final int open = 512;
   private static final ProtectorFeatureFlags.Inner_DataUtil EMPTY_STATE = new ProtectorFeatureFlags.Inner_DataUtil(false, null, 0, 0);
   private static volatile ProtectorFeatureFlags.Inner_DataUtil cachedState = EMPTY_STATE;
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

   public static void refresh() {
      refresh(AutismConfig.getGlobal());
   }

   public static void refresh(AutismConfig config) {
      if (config == null) {
         cachedState = EMPTY_STATE;
      } else {
         int sourceFlags = flagsOf(config);
         ProtectorFeatureFlags.Inner_DataUtil current = cachedState;
         if (!current.init_Z || current.config != config || current.flags != sourceFlags) {
            cachedState = new ProtectorFeatureFlags.Inner_DataUtil(true, config, sourceFlags, init(sourceFlags, init_Z, read()));
         }
      }
   }

   private static boolean init(int flag) {
      return (currentState().load & flag) != 0;
   }

   private static ProtectorFeatureFlags.Inner_DataUtil currentState() {
      ProtectorFeatureFlags.Inner_DataUtil state = cachedState;
      if (state.init_Z) {
         return state;
      }

      refresh(AutismConfig.getGlobal());
      return cachedState;
   }

   static int flagsOf(AutismConfig config) {
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

   static Object identity() {
      return cachedState;
   }

   public static void markDirect(Packet<?> packet) {
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

   public static boolean isDirect(Packet<?> packet) {
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

   private record Inner_DataUtil(boolean init_Z, AutismConfig config, int flags, int load) {
   }
}
