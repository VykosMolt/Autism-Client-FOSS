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
   private static final Logger init_Logger = LogUtils.getLogger();
   private static final boolean init_Z = Boolean.getBoolean("autism.protector.debug");
   private static final long init_J = 5L;
   private static final String init_Str = "fabric-menu-api-v1";
   private static final String load_Str = "open_screen";
   private static final String read = "voicechat";
   private static final String[] init_StrArr = new String[]{
      "secret", "state", "states", "remove_state", "add_group", "remove_group", "joined_group", "add_category", "remove_category"
   };
   private static final int init_I = 1024;
   private static final Map<Connection, LateRegisterSynthesizer.Inner_DataUtil> init_Map = new IdentityHashMap<>();
   private static long load_J;

   private LateRegisterSynthesizer() {
   }

   public static void init_V(Connection connection) {
      LateRegisterSynthesizer.Inner_DataUtil session = init_TaskQueue_DataUtil(connection, true);
      if (session != null) {
         synchronized (init_Map) {
            if (ConnectionPrivacyRegistry.init_Z(session.init_BufferHelper_Strea)) {
               session.init_Z = true;
            }
         }
      }
   }

   static boolean init_Z(Connection connection) {
      LateRegisterSynthesizer.Inner_DataUtil session = init_TaskQueue_DataUtil(connection, true);
      if (session == null) {
         return false;
      }

      synchronized (init_Map) {
         if (ConnectionPrivacyRegistry.init_Z(session.init_BufferHelper_Strea) && !session.write) {
            session.write = true;
            return true;
         } else {
            return false;
         }
      }
   }

   public static void init(Minecraft client) {
      load_J++;
      ClientPacketListener listener = client == null ? null : client.getConnection();
      if (client != null && client.player != null && listener != null) {
         ConnectionPrivacyRegistry.ConnectionSession context = ConnectionPrivacyRegistry.init(listener);
         if (context != null && context.init_BufferHelper_Cache() == ConnectionPrivacyRegistry.PrivacyMode.MODDED && ProtectorFeatureFlags.update()) {
            List<Identifier> channels = init(context.init_Connection(), load_J);
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
         if (context.init_PacketListener() != listener) {
            return;
         }

         if (channels.isEmpty()) {
            completed = true;
            return;
         }

         sender.accept(new ServerboundCustomPayloadPacket(new RegistrationPayload(RegistrationPayload.REGISTER, channels)));
         completed = true;
         if (init_Z) {
            init_Logger.debug("[AutismProtector] Synthesized late register with {} channel(s)", channels.size());
         }
      } catch (Throwable t) {
         init_Logger.warn("[AutismProtector] Late register send failed: {}", t.getMessage());
         return;
      } finally {
         init_V(context.init_Connection(), completed);
      }
   }

   static List<Identifier> init(Connection connection, long tick) {
      LateRegisterSynthesizer.Inner_DataUtil session = init_TaskQueue_DataUtil(connection, true);
      if (session == null) {
         return null;
      }

      synchronized (init_Map) {
         if (!ConnectionPrivacyRegistry.init_Z(session.init_BufferHelper_Strea)) {
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
         if (init_Z) {
            init_Logger.debug("[AutismProtector] Late register preparation failed: {}", t.getMessage());
         }

         return null;
      }
   }

   static void init_V(Connection connection, boolean success) {
      LateRegisterSynthesizer.Inner_DataUtil session = init_TaskQueue_DataUtil(connection, false);
      if (session != null) {
         synchronized (init_Map) {
            if (ConnectionPrivacyRegistry.init_Z(session.init_BufferHelper_Strea) && session.load) {
               session.load = false;
               session.read |= success;
            }
         }
      }
   }

   private static LateRegisterSynthesizer.Inner_DataUtil init_TaskQueue_DataUtil(Connection connection, boolean create) {
      if (connection == null) {
         return null;
      }

      ConnectionPrivacyRegistry.ConnectionSession context = ConnectionPrivacyRegistry.init_BufferHelper_Strea(connection);
      if (context != null && context.init_BufferHelper_Cache() == ConnectionPrivacyRegistry.PrivacyMode.MODDED) {
         synchronized (init_Map) {
            LateRegisterSynthesizer.Inner_DataUtil existing = init_Map.get(connection);
            if (existing != null) {
               if (existing.init_BufferHelper_Strea == context && existing.init_PacketListener == context.init_PacketListener() && ConnectionPrivacyRegistry.init_Z(context)
                  )
                {
                  return existing;
               }

               init_Map.remove(connection);
            }

            if (create && init_Map.size() < 1024 && ConnectionPrivacyRegistry.init_Z(context)) {
               LateRegisterSynthesizer.Inner_DataUtil created = new LateRegisterSynthesizer.Inner_DataUtil(context);
               init_Map.put(connection, created);
               return created;
            } else {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   static void load(Connection connection) {
      if (connection != null) {
         synchronized (init_Map) {
            init_Map.remove(connection);
         }
      }
   }

   private static void load() {
      synchronized (init_Map) {
         init_Map.entrySet().removeIf(entry -> !ConnectionPrivacyRegistry.init_Z(entry.getValue().init_BufferHelper_Strea));
      }
   }

   static void init_V() {
      synchronized (init_Map) {
         init_Map.clear();
         load_J = 0L;
      }
   }

   static List<Identifier> init_List() {
      Collection<Identifier> receivable;
      try {
         receivable = ClientPlayNetworking.getGlobalReceivers();
      } catch (Throwable t) {
         if (init_Z) {
            init_Logger.debug("[AutismProtector] getGlobalReceivers unavailable, using stock fallback: {}", t.getMessage());
         }

         receivable = init(init_Z());
      }

      if (receivable == null) {
         receivable = List.of();
      }

      return CustomPayloadFilter.init(receivable, ModNamespaceCatalog::init);
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

   private static boolean init_Z() {
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

   private static final class Inner_DataUtil {
      private final ConnectionPrivacyRegistry.ConnectionSession init_BufferHelper_Strea;
      private final PacketListener init_PacketListener;
      private long init_J = -1L;
      private boolean init_Z;
      private boolean load;
      private boolean read;
      private boolean write;

      private Inner_DataUtil(ConnectionPrivacyRegistry.ConnectionSession context) {
         this.init_BufferHelper_Strea = context;
         this.init_PacketListener = context.init_PacketListener();
      }
   }
}
