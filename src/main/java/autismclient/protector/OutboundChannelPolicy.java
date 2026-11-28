package autismclient.protector;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;

public final class OutboundChannelPolicy {
   private static final int init_I = 256;
   private static final int load_I = 1024;
   private static final Map<ConnectionPrivacyRegistry.ConnectionSession, Set<ServerboundCustomQueryAnswerPacket>> init_Map = new IdentityHashMap<>();
   private static final OutboundChannelPolicy.Verdict init_ConfigStore_DataUt = new OutboundChannelPolicy.Verdict(
      OutboundChannelPolicy.Verdict.VerdictKind.PASS, null
   );
   private static final OutboundChannelPolicy.Verdict load_ConfigStore_DataUt = new OutboundChannelPolicy.Verdict(
      OutboundChannelPolicy.Verdict.VerdictKind.DROP, null
   );

   private OutboundChannelPolicy() {
   }

   public static OutboundChannelPolicy.Verdict init(Connection connection, Packet<?> packet) {
      if (!(packet instanceof ServerboundCustomPayloadPacket) && !(packet instanceof ServerboundCustomQueryAnswerPacket)) {
         return init_ConfigStore_DataUt;
      } else {
         ConnectionPrivacyRegistry.ConnectionSession context = ConnectionPrivacyRegistry.getActive(connection);
         if (context == null) {
            return load_ConfigStore_DataUt;
         } else if (context.isLocalOrMemory()) {
            return init_ConfigStore_DataUt;
         } else {
            return packet instanceof ServerboundCustomQueryAnswerPacket answer
               ? init(context, answer)
               : init(connection, context, (ServerboundCustomPayloadPacket)packet);
         }
      }
   }

   public static boolean init_Z(Connection connection) {
      return connection != null && (connection.getReceiving() == PacketFlow.SERVERBOUND || connection.isMemoryConnection())
         ? true
         : init_Z(ConnectionPrivacyRegistry.getActive(connection));
   }

   public static boolean init(PacketListener listener) {
      return init_Z(ConnectionPrivacyRegistry.getByListener(listener));
   }

   private static boolean init_Z(ConnectionPrivacyRegistry.ConnectionSession context) {
      return context != null && context.privacyMode() == ConnectionPrivacyRegistry.PrivacyMode.MODDED;
   }

   public static void init(Connection connection, ServerboundCustomQueryAnswerPacket answer) {
      if (connection != null && answer != null) {
         ConnectionPrivacyRegistry.ConnectionSession context = ConnectionPrivacyRegistry.getActive(connection);
         if (context != null && context.privacyMode() == ConnectionPrivacyRegistry.PrivacyMode.VANILLA) {
            try {
               if (answer.payload() != null) {
                  return;
               }
            } catch (Throwable t) {
               return;
            }

            synchronized (init_Map) {
               Set<ServerboundCustomQueryAnswerPacket> authorized = init_Map.get(context);
               if (authorized == null) {
                  if (init_Map.size() >= 1024) {
                     return;
                  }

                  authorized = Collections.newSetFromMap(new IdentityHashMap<>());
                  init_Map.put(context, authorized);
               }

               if (authorized.size() < 256) {
                  authorized.add(answer);
               }
            }
         }
      }
   }

   public static void init_V(Connection connection) {
      if (connection != null) {
         forget(ConnectionPrivacyRegistry.getActive(connection));
      }
   }

   public static void forget(ConnectionPrivacyRegistry.ConnectionSession context) {
      if (context != null) {
         synchronized (init_Map) {
            init_Map.remove(context);
         }

         boolean current = ConnectionPrivacyRegistry.isActive(context);
         if (current) {
            OutboundPacketFilterHandler.init(context.connection());
         }

         ResourcePackSessionTracker.init_V(context);
         if (current) {
            LateRegisterSynthesizer.load(context.connection());
         }

         ConnectionPrivacyRegistry.unregisterSession(context);
      }
   }

   private static OutboundChannelPolicy.Verdict init(Connection connection, ConnectionPrivacyRegistry.ConnectionSession context, ServerboundCustomPayloadPacket packet) {
      CustomPacketPayload payload;
      try {
         payload = packet.payload();
      } catch (Throwable t) {
         return load_ConfigStore_DataUt;
      }

      if (context.privacyMode() == ConnectionPrivacyRegistry.PrivacyMode.VANILLA) {
         return payload instanceof BrandPayload ? init(new ServerboundCustomPayloadPacket(new BrandPayload("vanilla"))) : load_ConfigStore_DataUt;
      }

      CustomPayloadFilter.Verdict verdict = CustomPayloadFilter.init(connection, packet, false);
      OutboundChannelPolicy.Verdict var10000;
      switch (verdict.kind) {
         case PASS:
            var10000 = init_ConfigStore_DataUt;
            break;
         case DROP:
            var10000 = load_ConfigStore_DataUt;
            break;
         case REPLACE:
            var10000 = init(verdict.replacement);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static OutboundChannelPolicy.Verdict init(ConnectionPrivacyRegistry.ConnectionSession context, ServerboundCustomQueryAnswerPacket answer) {
      if (context.privacyMode() == ConnectionPrivacyRegistry.PrivacyMode.MODDED) {
         return init_ConfigStore_DataUt;
      }

      boolean authorized;
      synchronized (init_Map) {
         Set<ServerboundCustomQueryAnswerPacket> answers = init_Map.get(context);
         authorized = answers != null && answers.remove(answer);
         if (answers != null && answers.isEmpty()) {
            init_Map.remove(context);
         }
      }

      if (!authorized) {
         return load_ConfigStore_DataUt;
      }

      try {
         return answer.payload() == null ? init_ConfigStore_DataUt : load_ConfigStore_DataUt;
      } catch (Throwable t) {
         return load_ConfigStore_DataUt;
      }
   }

   private static OutboundChannelPolicy.Verdict init(Packet<?> replacement) {
      return replacement == null ? load_ConfigStore_DataUt : new OutboundChannelPolicy.Verdict(OutboundChannelPolicy.Verdict.VerdictKind.REPLACE, replacement);
   }

   static void init() {
      synchronized (init_Map) {
         init_Map.clear();
      }

      LateRegisterSynthesizer.reset();
   }

   public static final class Verdict {
      public final OutboundChannelPolicy.Verdict.VerdictKind kind;
      public final Packet<?> replacement;

      private Verdict(OutboundChannelPolicy.Verdict.VerdictKind kind, Packet<?> replacement) {
         this.kind = kind;
         this.replacement = replacement;
      }

      public enum VerdictKind {
         PASS,
         DROP,
         REPLACE;
      }
   }
}
