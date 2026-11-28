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
   private static final boolean OPSEC;
   private static final boolean EXPLOIT_PREVENTER;
   private static final int FLAG_ENABLED = 1;
   private static final int FLAG_FILTER_CHANNELS = 4;
   private static final int FLAG_TRANSLATION_PROTECTION = 8;
   private static final int FLAG_DISABLE_TELEMETRY = 16;
   private static final int FLAG_BLOCK_LOCAL_URLS = 32;
   private static final int FLAG_ISOLATE_PACK_CACHE = 64;
   private static final int FLAG_STRIP_SERVER_PACKS = 128;
   private static final int FLAG_CHAT_SIGNING_OFF = 256;
   private static final int open = 512;
   private static final ProtectorFeatureFlags.FlagState EMPTY_STATE = new ProtectorFeatureFlags.FlagState(false, null, 0, 0);
   private static volatile ProtectorFeatureFlags.FlagState cachedState = EMPTY_STATE;
   private static final int MAX_DIRECT_PACKETS = 1024;
   private static final Map<Object, Boolean> DIRECT_PACKETS;

   private ProtectorFeatureFlags() {
   }

   public static boolean init_Z() {
      return hasFullExternalProtector();
   }

   public static boolean hasFullExternalProtector() {
      return OPSEC;
   }

   public static boolean hasExternalProtector() {
      return OPSEC || EXPLOIT_PREVENTER;
   }

   public static boolean hasOverlappingProtector() {
      return EXPLOIT_PREVENTER && !OPSEC;
   }

   public static boolean protectorEnabled() {
      return isSet(1);
   }

   public static boolean spoofClientVanilla() {
      return isSet(512);
   }

   public static boolean filterChannels() {
      return isSet(4);
   }

   public static boolean translationProtection() {
      return isSet(8);
   }

   public static boolean open() {
      return isSet(8);
   }

   public static boolean disableTelemetry() {
      return isSet(16);
   }

   public static boolean push() {
      return isSet(32);
   }

   public static boolean pop() {
      return isSet(64);
   }

   public static boolean tick() {
      return isSet(128);
   }

   public static boolean chatSigningOff() {
      return isSet(256);
   }

   public static void refresh() {
      refresh(AutismConfig.getGlobal());
   }

   public static void refresh(AutismConfig config) {
      if (config == null) {
         cachedState = EMPTY_STATE;
      } else {
         int sourceFlags = flagsOf(config);
         ProtectorFeatureFlags.FlagState current = cachedState;
         if (!current.init_Z || current.config != config || current.flags != sourceFlags) {
            cachedState = new ProtectorFeatureFlags.FlagState(true, config, sourceFlags, effectiveFlags(sourceFlags, OPSEC, hasExternalProtector()));
         }
      }
   }

   private static boolean isSet(int flag) {
      return (currentState().activeFlags & flag) != 0;
   }

   private static ProtectorFeatureFlags.FlagState currentState() {
      ProtectorFeatureFlags.FlagState state = cachedState;
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

   static int effectiveFlags(int sourceFlags, boolean fullExternalProtector, boolean overlapExternalProtector) {
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
         synchronized (DIRECT_PACKETS) {
            if (DIRECT_PACKETS.size() >= 1024) {
               Iterator<Entry<Object, Boolean>> it = DIRECT_PACKETS.entrySet().iterator();
               if (it.hasNext()) {
                  it.next();
                  it.remove();
               }
            }

            DIRECT_PACKETS.put(packet, Boolean.TRUE);
         }
      }
   }

   public static boolean isDirect(Packet<?> packet) {
      return packet == null ? false : DIRECT_PACKETS.containsKey(packet);
   }

   public static void unmarkDirect(Packet<?> packet) {
      if (packet != null) {
         DIRECT_PACKETS.remove(packet);
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

      OPSEC = opsec;
      EXPLOIT_PREVENTER = exploitPreventer;
      DIRECT_PACKETS = Collections.synchronizedMap(new IdentityHashMap<>());
   }

   private record FlagState(boolean init_Z, AutismConfig config, int flags, int activeFlags) {
   }
}
