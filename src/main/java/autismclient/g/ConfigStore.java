package autismclient.g;

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

public final class ConfigStore {
   private static final int init_I = 256;
   private static final int load_I = 1024;
   private static final Map<BufferHelper.Inner_StreamUtil, Set<ServerboundCustomQueryAnswerPacket>> init_Map = new IdentityHashMap<>();
   private static final ConfigStore.Inner_DataUtil init_ConfigStore_DataUt = new ConfigStore.Inner_DataUtil(
      ConfigStore.Inner_DataUtil.Inner_DataUtil_2.PASS, null
   );
   private static final ConfigStore.Inner_DataUtil load_ConfigStore_DataUt = new ConfigStore.Inner_DataUtil(
      ConfigStore.Inner_DataUtil.Inner_DataUtil_2.DROP, null
   );

   private ConfigStore() {
   }

   public static ConfigStore.Inner_DataUtil init(Connection connection, Packet<?> packet) {
      if (!(packet instanceof ServerboundCustomPayloadPacket) && !(packet instanceof ServerboundCustomQueryAnswerPacket)) {
         return init_ConfigStore_DataUt;
      } else {
         BufferHelper.Inner_StreamUtil context = BufferHelper.init_BufferHelper_Strea(connection);
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
         : init_Z(BufferHelper.init_BufferHelper_Strea(connection));
   }

   public static boolean init(PacketListener listener) {
      return init_Z(BufferHelper.init(listener));
   }

   private static boolean init_Z(BufferHelper.Inner_StreamUtil context) {
      return context != null && context.init_BufferHelper_Cache() == BufferHelper.Inner_CachePool.MODDED;
   }

   public static void init(Connection connection, ServerboundCustomQueryAnswerPacket answer) {
      if (connection != null && answer != null) {
         BufferHelper.Inner_StreamUtil context = BufferHelper.init_BufferHelper_Strea(connection);
         if (context != null && context.init_BufferHelper_Cache() == BufferHelper.Inner_CachePool.VANILLA) {
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
         init_V(BufferHelper.init_BufferHelper_Strea(connection));
      }
   }

   public static void init_V(BufferHelper.Inner_StreamUtil context) {
      if (context != null) {
         synchronized (init_Map) {
            init_Map.remove(context);
         }

         boolean current = BufferHelper.init_Z(context);
         if (current) {
            ListPool.init(context.init_Connection());
         }

         FileCache.init_V(context);
         if (current) {
            TaskQueue.load(context.init_Connection());
         }

         BufferHelper.read_Z(context);
      }
   }

   private static ConfigStore.Inner_DataUtil init(Connection connection, BufferHelper.Inner_StreamUtil context, ServerboundCustomPayloadPacket packet) {
      CustomPacketPayload payload;
      try {
         payload = packet.payload();
      } catch (Throwable t) {
         return load_ConfigStore_DataUt;
      }

      if (context.init_BufferHelper_Cache() == BufferHelper.Inner_CachePool.VANILLA) {
         return payload instanceof BrandPayload ? init(new ServerboundCustomPayloadPacket(new BrandPayload("vanilla"))) : load_ConfigStore_DataUt;
      }

      IoWorker.Inner_DataUtil verdict = IoWorker.init(connection, packet, false);
      ConfigStore.Inner_DataUtil var10000;
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

   private static ConfigStore.Inner_DataUtil init(BufferHelper.Inner_StreamUtil context, ServerboundCustomQueryAnswerPacket answer) {
      if (context.init_BufferHelper_Cache() == BufferHelper.Inner_CachePool.MODDED) {
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

   private static ConfigStore.Inner_DataUtil init(Packet<?> replacement) {
      return replacement == null ? load_ConfigStore_DataUt : new ConfigStore.Inner_DataUtil(ConfigStore.Inner_DataUtil.Inner_DataUtil_2.REPLACE, replacement);
   }

   static void init() {
      synchronized (init_Map) {
         init_Map.clear();
      }

      TaskQueue.init_V();
   }

   public static final class Inner_DataUtil {
      public final ConfigStore.Inner_DataUtil.Inner_DataUtil_2 init_ConfigStore_DataUt;
      public final Packet<?> init_Packet;

      private Inner_DataUtil(ConfigStore.Inner_DataUtil.Inner_DataUtil_2 kind, Packet<?> replacement) {
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
