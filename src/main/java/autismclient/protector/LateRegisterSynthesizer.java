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
   private static final String[] init_StrArr = new String[]{
      "secret", "state", "states", "remove_state", "add_group", "remove_group", "joined_group", "add_category", "remove_category"
   };
   private static final int init_I = 1024;
   private static final Map<Connection, LateRegisterSynthesizer.SessionState> SESSIONS = new IdentityHashMap<>();
   private static long load_J;

   private LateRegisterSynthesizer() {
   }

   public static void init_V(Connection connection) {
      LateRegisterSynthesizer.SessionState session = sessionFor(connection, true);
      if (session != null) {
         synchronized (SESSIONS) {
            if (ConnectionPrivacyRegistry.isActive(session.privacySession)) {
               session.init_Z = true;
            }
         }
      }
   }

   static boolean init_Z(Connection connection) {
      LateRegisterSynthesizer.SessionState session = sessionFor(connection, true);
      if (session == null) {
         return false;
      }

      synchronized (SESSIONS) {
         if (ConnectionPrivacyRegistry.isActive(session.privacySession) && !session.write) {
            session.write = true;
            return true;
         } else {
            return false;
         }
      }
   }

   public static void tick(Minecraft client) {
      load_J++;
      ClientPacketListener listener = client == null ? null : client.getConnection();
      if (client != null && client.player != null && listener != null) {
         ConnectionPrivacyRegistry.ConnectionSession context = ConnectionPrivacyRegistry.getByListener(listener);
         if (context != null && context.privacyMode() == ConnectionPrivacyRegistry.PrivacyMode.MODDED && ProtectorFeatureFlags.protectorEnabled()) {
            List<Identifier> channels = init(context.connection(), load_J);
            if (channels != null) {
               init(context, channels, listener, listener::send);
            }
         } else {
            load();
         }
      } else {
         load();
      }
   }

   static void init(ConnectionPrivacyRegistry.ConnectionSession context, List<Identifier> channels, PacketListener listener, Consumer<ServerboundCustomPayloadPacket> sender) {
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
         init_V(context.connection(), completed);
      }
   }

   static List<Identifier> init(Connection connection, long tick) {
      LateRegisterSynthesizer.SessionState session = sessionFor(connection, true);
      if (session == null) {
         return null;
      }

      synchronized (SESSIONS) {
         if (!ConnectionPrivacyRegistry.isActive(session.privacySession)) {
            return null;
         }

         if (session.init_J < 0L) {
            session.init_J = tick;
         }

         if (!init(true, session.init_Z, session.read, session.load, tick - session.init_J)) {
            return null;
         }

         session.load = true;
      }

      try {
         return init_List();
      } catch (Throwable t) {
         init_V(connection, false);
         if (DEBUG) {
            LOGGER.debug("[AutismProtector] Late register preparation failed: {}", t.getMessage());
         }

         return null;
      }
   }

   static void init_V(Connection connection, boolean success) {
      LateRegisterSynthesizer.SessionState session = sessionFor(connection, false);
      if (session != null) {
         synchronized (SESSIONS) {
            if (ConnectionPrivacyRegistry.isActive(session.privacySession) && session.load) {
               session.load = false;
               session.read |= success;
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

   private static void load() {
      synchronized (SESSIONS) {
         SESSIONS.entrySet().removeIf(entry -> !ConnectionPrivacyRegistry.isActive(entry.getValue().privacySession));
      }
   }

   static void reset() {
      synchronized (SESSIONS) {
         SESSIONS.clear();
         load_J = 0L;
      }
   }

   static List<Identifier> init_List() {
      Collection<Identifier> receivable;
      try {
         receivable = ClientPlayNetworking.getGlobalReceivers();
      } catch (Throwable t) {
         if (DEBUG) {
            LOGGER.debug("[AutismProtector] getGlobalReceivers unavailable, using stock fallback: {}", t.getMessage());
         }

         receivable = init(isVoiceChatLoaded());
      }

      if (receivable == null) {
         receivable = List.of();
      }

      return CustomPayloadFilter.filter(receivable, ModNamespaceCatalog::init);
   }

   static List<Identifier> init(boolean voicechatLoaded) {
      Set<Identifier> channels = new LinkedHashSet<>();
      channels.add(Identifier.fromNamespaceAndPath("fabric-menu-api-v1", "open_screen"));
      if (voicechatLoaded) {
         for (String path : init_StrArr) {
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

   static boolean init(boolean moddedModeActive, boolean registerSeen, boolean alreadySynthesized, boolean synthesisInFlight, long ticksSincePlayStart) {
      return moddedModeActive && !registerSeen && !alreadySynthesized && !synthesisInFlight && ticksSincePlayStart >= 5L;
   }

   static long init_J() {
      return 5L;
   }

   private static final class SessionState {
      private final ConnectionPrivacyRegistry.ConnectionSession privacySession;
      private final PacketListener listener;
      private long init_J = -1L;
      private boolean init_Z;
      private boolean load;
      private boolean read;
      private boolean write;

      private SessionState(ConnectionPrivacyRegistry.ConnectionSession context) {
         this.privacySession = context;
         this.listener = context.packetListener();
      }
   }
}
