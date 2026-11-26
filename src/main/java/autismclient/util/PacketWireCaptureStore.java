package autismclient.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.ProtocolInfo.Details;
import net.minecraft.network.ProtocolInfo.DetailsProvider;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.game.GameProtocols;

public final class PacketWireCaptureStore {
   private static final Map<Packet<?>, PacketWireCaptureStore.Inner_StreamUtil> init = Collections.synchronizedMap(new WeakHashMap<>());
   private static final Map<Channel, PacketWireCaptureStore.Inner_DataUtil> load = Collections.synchronizedMap(new WeakHashMap<>());
   private static final Map<String, Map<String, Integer>> read = new ConcurrentHashMap<>();

   private PacketWireCaptureStore() {
   }

   public static void init(Packet<?> packet, String direction, String protocolPhase, PacketType<?> packetType, ByteBuf source) {
      if (packet != null && source != null) {
         init(packet, direction, protocolPhase, packetType, init(source));
      }
   }

   public static void init(Packet<?> packet, String direction, String protocolPhase, PacketType<?> packetType, byte[] bytes) {
      if (packet != null && bytes != null) {
         int numericPacketId = init(bytes);
         if (numericPacketId < 0) {
            numericPacketId = init(protocolPhase, direction, packetType);
         }

         init.put(
            packet,
            new PacketWireCaptureStore.Inner_StreamUtil(
               direction,
               protocolPhase,
               packet.getClass().getName(),
               packetType == null ? "" : packetType.toString(),
               numericPacketId,
               bytes,
               System.currentTimeMillis()
            )
         );
      }
   }

   public static PacketWireCaptureStore.Inner_StreamUtil init(Packet<?> packet) {
      return packet == null ? null : init.get(packet);
   }

   public static void init_V(Channel channel) {
      if (channel != null) {
         PacketWireCaptureStore.Inner_DataUtil previous = load.get(channel);
         load.put(channel, previous == null ? PacketWireCaptureStore.Inner_DataUtil.init_bn_DataUtil() : previous.init(true));
      }
   }

   public static void init(Channel channel, String direction, ByteBuf source) {
      if (channel != null && source != null) {
         PacketWireCaptureStore.Inner_DataUtil previous = load.get(channel);
         if (previous == null) {
            previous = PacketWireCaptureStore.Inner_DataUtil.init_bn_DataUtil();
         }

         load.put(channel, previous.init(direction, init(source)));
      }
   }

   public static PacketWireCaptureStore.Inner_DataUtil init_bn_DataUtil(Channel channel) {
      return channel == null ? null : load.get(channel);
   }

   public static byte[] init(ByteBuf source) {
      if (source == null) {
         return PacketCaptureState.init_BArr;
      }

      int length = Math.max(0, source.readableBytes());
      if (length == 0) {
         return PacketCaptureState.init_BArr;
      }

      byte[] bytes = new byte[length];
      source.getBytes(source.readerIndex(), bytes);
      return bytes;
   }

   public static String init(byte[] bytes, int maxBytes) {
      return AutismPayloadSupport.init_Str(bytes, maxBytes);
   }

   private static int init(byte[] bytes) {
      if (bytes != null && bytes.length != 0) {
         int value = 0;
         int shift = 0;

         for (int i = 0; i < Math.min(5, bytes.length); i++) {
            int b = bytes[i] & 255;
            value |= (b & 127) << shift;
            if ((b & 128) == 0) {
               return value;
            }

            shift += 7;
         }

         return -1;
      } else {
         return -1;
      }
   }

   public static int init(String protocolPhase, String direction, PacketType<?> packetType) {
      if (packetType == null) {
         return -1;
      } else {
         String phase = init(protocolPhase);
         String flow = load(direction);
         if (!phase.isBlank() && !flow.isBlank()) {
            Map<String, Integer> ids = read.computeIfAbsent(phase + "|" + flow, key -> init_Map(phase, flow));
            Integer id = ids.get(packetType.toString());
            return id == null ? -1 : id;
         } else {
            return -1;
         }
      }
   }

   private static Map<String, Integer> init_Map(String phase, String flow) {
      DetailsProvider provider = init_ProtocolInfo_Detai(phase, flow);
      if (provider == null) {
         return Map.of();
      }

      Map<String, Integer> ids = new HashMap<>();

      try {
         Details details = provider.details();
         details.listPackets((type, networkId) -> {
            if (type != null && networkId >= 0) {
               ids.put(type.toString(), networkId);
            }
         });
      } catch (Throwable var5) {
      }

      return ids.isEmpty() ? Map.of() : Map.copyOf(ids);
   }

   private static DetailsProvider init_ProtocolInfo_Detai(String phase, String flow) {
      boolean c2s = "C2S".equals(flow);
      if ("CONFIGURATION".equals(phase)) {
         return c2s ? ConfigurationProtocols.SERVERBOUND_TEMPLATE : ConfigurationProtocols.CLIENTBOUND_TEMPLATE;
      } else if ("PLAY".equals(phase)) {
         return c2s ? GameProtocols.SERVERBOUND_TEMPLATE : GameProtocols.CLIENTBOUND_TEMPLATE;
      } else {
         return null;
      }
   }

   private static String init(String protocolPhase) {
      if (protocolPhase != null && !protocolPhase.isBlank()) {
         String value = protocolPhase.strip().toUpperCase(Locale.ROOT);
         if (value.contains("CONFIG")) {
            return "CONFIGURATION";
         } else {
            return !value.contains("PLAY") && !value.contains("GAME") ? value : "PLAY";
         }
      } else {
         return "";
      }
   }

   private static String load(String direction) {
      if (direction != null && !direction.isBlank()) {
         String value = direction.strip().toUpperCase(Locale.ROOT);
         if (!value.contains("SERVERBOUND") && !value.contains("OUT") && !value.contains("C2S")) {
            return !value.contains("CLIENTBOUND") && !value.contains("IN") && !value.contains("S2C") ? value : "S2C";
         } else {
            return "C2S";
         }
      } else {
         return "";
      }
   }

   public record Inner_DataUtil(boolean init_Z, long init_J, byte[] init_BArr, byte[] load_BArr, long load_J, long read) {
      static PacketWireCaptureStore.Inner_DataUtil init_bn_DataUtil() {
         return new PacketWireCaptureStore.Inner_DataUtil(true, System.currentTimeMillis(), new byte[0], new byte[0], 0L, 0L);
      }

      PacketWireCaptureStore.Inner_DataUtil init(boolean value) {
         return new PacketWireCaptureStore.Inner_DataUtil(
            value, value && this.init_J == 0L ? System.currentTimeMillis() : this.init_J, this.init_BArr, this.load_BArr, this.load_J, this.read
         );
      }

      PacketWireCaptureStore.Inner_DataUtil init(String direction, byte[] bytes) {
         long now = System.currentTimeMillis();
         return !"C2S".equalsIgnoreCase(direction) && !"OUT".equalsIgnoreCase(direction)
            ? new PacketWireCaptureStore.Inner_DataUtil(this.init_Z, this.init_J, bytes == null ? new byte[0] : (byte[])bytes.clone(), this.load_BArr, now, this.read)
            : new PacketWireCaptureStore.Inner_DataUtil(this.init_Z, this.init_J, this.init_BArr, bytes == null ? new byte[0] : (byte[])bytes.clone(), this.load_J, now);
      }

      public byte[] init_BArr() {
         return this.init_BArr == null ? new byte[0] : (byte[])this.init_BArr.clone();
      }

      public byte[] load_BArr() {
         return this.load_BArr == null ? new byte[0] : (byte[])this.load_BArr.clone();
      }
   }

   public record Inner_StreamUtil(String init_Str, String load, String read, String write, int init_I, byte[] init_BArr, long init_J) {
      public byte[] init_BArr() {
         return this.init_BArr == null ? new byte[0] : (byte[])this.init_BArr.clone();
      }
   }
}
