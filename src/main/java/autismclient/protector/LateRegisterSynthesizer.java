package autismclient.protector;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.impl.networking.RegistrationPayload;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

public final class LateRegisterSynthesizer {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final boolean DEBUG = Boolean.getBoolean("autism.protector.debug");
   private static final long init_J = 5L;
   private static final String init_Str = "fabric-menu-api-v1";
   private static final String load_Str = "open_screen";
   private static final String read = "voicechat";
   private static final String[] VOICECHAT_PATHS = new String[]{
      "secret", "state", "states", "remove_state", "add_group", "remove_group", "joined_group", "add_category", "remove_category"
   };
   private static final int init_I = 1024;
   private static final Map<Connection, LateRegisterSynthesizer.SessionState> SESSIONS = new IdentityHashMap<>();
   private static long tickCounter;

   private LateRegisterSynthesizer() {
   }

   public static void markRegisterSeen(Connection connection) {
      LateRegisterSynthesizer.SessionState session = sessionFor(connection, true);
      if (session != null) {
         synchronized (SESSIONS) {
            if (ConnectionPrivacyRegistry.isActive(session.privacySession)) {
               session.registerSeen = true;
            }
         }
      }
   }

   static boolean shouldWarnOnce(Connection connection) {
      LateRegisterSynthesizer.SessionState session = sessionFor(connection, true);
      if (session == null) {
         return false;
      }

      synchronized (SESSIONS) {
         if (ConnectionPrivacyRegistry.isActive(session.privacySession) && !session.warnedOnce) {
            session.warnedOnce = true;
            return true;
         } else {
            return false;
         }
      }
   }

   public static void tick(Minecraft client) {
      tickCounter++;
      ClientPacketListener listener = client == null ? null : client.getConnection();
      if (client != null && client.player != null && listener != null) {
         ConnectionPrivacyRegistry.ConnectionSession context = ConnectionPrivacyRegistry.getByListener(listener);
         if (context != null && context.privacyMode() == ConnectionPrivacyRegistry.PrivacyMode.MODDED && ProtectorFeatureFlags.protectorEnabled()) {
            List<Identifier> channels = channelsToSynthesize(context.connection(), tickCounter);
            if (channels != null) {
               sendSyntheticRegister(context, channels, listener, listener::send);
            }
         } else {
            pruneSessions();
         }
      } else {
         pruneSessions();
      }
   }

   static void sendSyntheticRegister(ConnectionPrivacyRegistry.ConnectionSession context, List<Identifier> channels, PacketListener listener, Consumer<ServerboundCustomPayloadPacket> sender) {
      boolean completed = false;

      try {
         if (context.packetListener() != listener) {
            return;
         }

         if (channels.isEmpty()) {
            completed = true;
            return;
         }

         sender.accept(new ServerboundCustomPayloadPacket(new RegistrationPayload(RegistrationPayload.REGISTER, channels)));
         completed = true;
         if (DEBUG) {
            LOGGER.debug("[AutismProtector] Synthesized late register with {} channel(s)", channels.size());
         }
      } catch (Throwable t) {
         LOGGER.warn("[AutismProtector] Late register send failed: {}", t.getMessage());
         return;
      } finally {
         finishSynthesis(context.connection(), completed);
      }
   }

   static List<Identifier> channelsToSynthesize(Connection connection, long tick) {
      LateRegisterSynthesizer.SessionState session = sessionFor(connection, true);
      if (session == null) {
         return null;
      }

      synchronized (SESSIONS) {
         if (!ConnectionPrivacyRegistry.isActive(session.privacySession)) {
            return null;
         }

         if (session.playStartTick < 0L) {
            session.playStartTick = tick;
         }

         if (!shouldSynthesize(true, session.registerSeen, session.synthesized, session.synthesisInFlight, tick - session.playStartTick)) {
            return null;
         }

         session.synthesisInFlight = true;
      }

      try {
         return receivableChannels();
      } catch (Throwable t) {
         finishSynthesis(connection, false);
         if (DEBUG) {
            LOGGER.debug("[AutismProtector] Late register preparation failed: {}", t.getMessage());
         }

         return null;
      }
   }

   static void finishSynthesis(Connection connection, boolean success) {
      LateRegisterSynthesizer.SessionState session = sessionFor(connection, false);
      if (session != null) {
         synchronized (SESSIONS) {
            if (ConnectionPrivacyRegistry.isActive(session.privacySession) && session.synthesisInFlight) {
               session.synthesisInFlight = false;
               session.synthesized |= success;
            }
         }
      }
   }

   private static LateRegisterSynthesizer.SessionState sessionFor(Connection connection, boolean create) {
      if (connection == null) {
         return null;
      }

      ConnectionPrivacyRegistry.ConnectionSession context = ConnectionPrivacyRegistry.getActive(connection);
      if (context != null && context.privacyMode() == ConnectionPrivacyRegistry.PrivacyMode.MODDED) {
         synchronized (SESSIONS) {
            LateRegisterSynthesizer.SessionState existing = SESSIONS.get(connection);
            if (existing != null) {
               if (existing.privacySession == context && existing.listener == context.packetListener() && ConnectionPrivacyRegistry.isActive(context)
                  )
                {
                  return existing;
               }

               SESSIONS.remove(connection);
            }

            if (create && SESSIONS.size() < 1024 && ConnectionPrivacyRegistry.isActive(context)) {
               LateRegisterSynthesizer.SessionState created = new LateRegisterSynthesizer.SessionState(context);
               SESSIONS.put(connection, created);
               return created;
            } else {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   static void forget(Connection connection) {
      if (connection != null) {
         synchronized (SESSIONS) {
            SESSIONS.remove(connection);
         }
      }
   }

   private static void pruneSessions() {
      synchronized (SESSIONS) {
         SESSIONS.entrySet().removeIf(entry -> !ConnectionPrivacyRegistry.isActive(entry.getValue().privacySession));
      }
   }

   static void reset() {
      synchronized (SESSIONS) {
         SESSIONS.clear();
         tickCounter = 0L;
      }
   }

   static List<Identifier> receivableChannels() {
      Collection<Identifier> receivable;
      try {
         receivable = ClientPlayNetworking.getGlobalReceivers();
      } catch (Throwable t) {
         if (DEBUG) {
            LOGGER.debug("[AutismProtector] getGlobalReceivers unavailable, using stock fallback: {}", t.getMessage());
         }

         receivable = fallbackChannels(isVoiceChatLoaded());
      }

      if (receivable == null) {
         receivable = List.of();
      }

      return CustomPayloadFilter.filter(receivable, ModNamespaceCatalog::isVanillaOrFabricChannel);
   }

   static List<Identifier> fallbackChannels(boolean voicechatLoaded) {
      Set<Identifier> channels = new LinkedHashSet<>();
      channels.add(Identifier.fromNamespaceAndPath("fabric-menu-api-v1", "open_screen"));
      if (voicechatLoaded) {
         for (String path : VOICECHAT_PATHS) {
            channels.add(Identifier.fromNamespaceAndPath("voicechat", path));
         }
      }

      return new ArrayList<>(channels);
   }

   private static boolean isVoiceChatLoaded() {
      try {
         return FabricLoader.getInstance().isModLoaded("voicechat");
      } catch (Throwable t) {
         return false;
      }
   }

   static boolean shouldSynthesize(boolean moddedModeActive, boolean registerSeen, boolean alreadySynthesized, boolean synthesisInFlight, long ticksSincePlayStart) {
      return moddedModeActive && !registerSeen && !alreadySynthesized && !synthesisInFlight && ticksSincePlayStart >= 5L;
   }

   static long unusedDelayTicks() {
      return 5L;
   }

   private static final class SessionState {
      private final ConnectionPrivacyRegistry.ConnectionSession privacySession;
      private final PacketListener listener;
      private long playStartTick = -1L;
      private boolean registerSeen;
      private boolean synthesisInFlight;
      private boolean synthesized;
      private boolean warnedOnce;

      private SessionState(ConnectionPrivacyRegistry.ConnectionSession context) {
         this.privacySession = context;
         this.listener = context.packetListener();
      }
   }
}
