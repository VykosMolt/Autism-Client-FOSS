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
   private static final OutboundChannelPolicy.Inner_DataUtil init_ConfigStore_DataUt = new OutboundChannelPolicy.Inner_DataUtil(
      OutboundChannelPolicy.Inner_DataUtil.Inner_DataUtil_2.PASS, null
   );
   private static final OutboundChannelPolicy.Inner_DataUtil load_ConfigStore_DataUt = new OutboundChannelPolicy.Inner_DataUtil(
      OutboundChannelPolicy.Inner_DataUtil.Inner_DataUtil_2.DROP, null
   );

   private OutboundChannelPolicy() {
   }

   public static OutboundChannelPolicy.Inner_DataUtil init(Connection connection, Packet<?> packet) {
      if (!(packet instanceof ServerboundCustomPayloadPacket) && !(packet instanceof ServerboundCustomQueryAnswerPacket)) {
         return init_ConfigStore_DataUt;
      } else {
         ConnectionPrivacyRegistry.ConnectionSession context = ConnectionPrivacyRegistry.init_BufferHelper_Strea(connection);
         if (context == null) {
            return load_ConfigStore_DataUt;
         } else if (context.read()) {
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
         : init_Z(ConnectionPrivacyRegistry.init_BufferHelper_Strea(connection));
   }

   public static boolean init(PacketListener listener) {
      return init_Z(ConnectionPrivacyRegistry.init(listener));
   }

   private static boolean init_Z(ConnectionPrivacyRegistry.ConnectionSession context) {
      return context != null && context.init_BufferHelper_Cache() == ConnectionPrivacyRegistry.PrivacyMode.MODDED;
   }

   public static void init(Connection connection, ServerboundCustomQueryAnswerPacket answer) {
      if (connection != null && answer != null) {
         ConnectionPrivacyRegistry.ConnectionSession context = ConnectionPrivacyRegistry.init_BufferHelper_Strea(connection);
         if (context != null && context.init_BufferHelper_Cache() == ConnectionPrivacyRegistry.PrivacyMode.VANILLA) {
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
         init_V(ConnectionPrivacyRegistry.init_BufferHelper_Strea(connection));
      }
   }

   public static void init_V(ConnectionPrivacyRegistry.ConnectionSession context) {
      if (context != null) {
         synchronized (init_Map) {
            init_Map.remove(context);
         }

         boolean current = ConnectionPrivacyRegistry.init_Z(context);
         if (current) {
            OutboundPacketFilterHandler.init(context.init_Connection());
         }

         ResourcePackSessionTracker.init_V(context);
         if (current) {
            LateRegisterSynthesizer.load(context.init_Connection());
         }

         ConnectionPrivacyRegistry.read_Z(context);
      }
   }

   private static OutboundChannelPolicy.Inner_DataUtil init(Connection connection, ConnectionPrivacyRegistry.ConnectionSession context, ServerboundCustomPayloadPacket packet) {
      CustomPacketPayload payload;
      try {
         payload = packet.payload();
      } catch (Throwable t) {
         return load_ConfigStore_DataUt;
      }

      if (context.init_BufferHelper_Cache() == ConnectionPrivacyRegistry.PrivacyMode.VANILLA) {
         return payload instanceof BrandPayload ? init(new ServerboundCustomPayloadPacket(new BrandPayload("vanilla"))) : load_ConfigStore_DataUt;
      }

      CustomPayloadFilter.Inner_DataUtil verdict = CustomPayloadFilter.init(connection, packet, false);
      OutboundChannelPolicy.Inner_DataUtil var10000;
      switch (verdict.init_IoWorker_DataUtil_) {
         case PASS:
            var10000 = init_ConfigStore_DataUt;
            break;
         case DROP:
            var10000 = load_ConfigStore_DataUt;
            break;
         case REPLACE:
            var10000 = init(verdict.init_Packet);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static OutboundChannelPolicy.Inner_DataUtil init(ConnectionPrivacyRegistry.ConnectionSession context, ServerboundCustomQueryAnswerPacket answer) {
      if (context.init_BufferHelper_Cache() == ConnectionPrivacyRegistry.PrivacyMode.MODDED) {
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

   private static OutboundChannelPolicy.Inner_DataUtil init(Packet<?> replacement) {
      return replacement == null ? load_ConfigStore_DataUt : new OutboundChannelPolicy.Inner_DataUtil(OutboundChannelPolicy.Inner_DataUtil.Inner_DataUtil_2.REPLACE, replacement);
   }

   static void init() {
      synchronized (init_Map) {
         init_Map.clear();
      }

      LateRegisterSynthesizer.init_V();
   }

   public static final class Inner_DataUtil {
      public final OutboundChannelPolicy.Inner_DataUtil.Inner_DataUtil_2 init_ConfigStore_DataUt;
      public final Packet<?> init_Packet;

      private Inner_DataUtil(OutboundChannelPolicy.Inner_DataUtil.Inner_DataUtil_2 kind, Packet<?> replacement) {
         this.init_ConfigStore_DataUt = kind;
         this.init_Packet = replacement;
      }

      public enum Inner_DataUtil_2 {
         PASS,
         DROP,
         REPLACE;
      }
   }
}
