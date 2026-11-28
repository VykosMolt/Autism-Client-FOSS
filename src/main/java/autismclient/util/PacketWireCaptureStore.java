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
   private static final Map<Packet<?>, PacketWireCaptureStore.PacketCapture> packetCaptures = Collections.synchronizedMap(new WeakHashMap<>());
   private static final Map<Channel, PacketWireCaptureStore.ChannelCapture> channelStates = Collections.synchronizedMap(new WeakHashMap<>());
   private static final Map<String, Map<String, Integer>> packetIdsByPhase = new ConcurrentHashMap<>();

   private PacketWireCaptureStore() {
   }

   public static void capture(Packet<?> packet, String direction, String protocolPhase, PacketType<?> packetType, ByteBuf source) {
      if (packet != null && source != null) {
         captureBytes(packet, direction, protocolPhase, packetType, copyBytes(source));
      }
   }

   public static void captureBytes(Packet<?> packet, String direction, String protocolPhase, PacketType<?> packetType, byte[] bytes) {
      if (packet != null && bytes != null) {
         int numericPacketId = readVarInt(bytes);
         if (numericPacketId < 0) {
            numericPacketId = numericId(protocolPhase, direction, packetType);
         }

         packetCaptures.put(
            packet,
            new PacketWireCaptureStore.PacketCapture(
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

   public static PacketWireCaptureStore.PacketCapture captureFor(Packet<?> packet) {
      return packet == null ? null : packetCaptures.get(packet);
   }

   public static void markEncrypted(Channel channel) {
      if (channel != null) {
         PacketWireCaptureStore.ChannelCapture previous = channelStates.get(channel);
         channelStates.put(channel, previous == null ? PacketWireCaptureStore.ChannelCapture.init_bn_DataUtil() : previous.withEnabled(true));
      }
   }

   public static void captureRaw(Channel channel, String direction, ByteBuf source) {
      if (channel != null && source != null) {
         PacketWireCaptureStore.ChannelCapture previous = channelStates.get(channel);
         if (previous == null) {
            previous = PacketWireCaptureStore.ChannelCapture.init_bn_DataUtil();
         }

         channelStates.put(channel, previous.withCapture(direction, copyBytes(source)));
      }
   }

   public static PacketWireCaptureStore.ChannelCapture init_bn_DataUtil(Channel channel) {
      return channel == null ? null : channelStates.get(channel);
   }

   public static byte[] copyBytes(ByteBuf source) {
      if (source == null) {
         return PacketCaptureState.EMPTY_BYTES;
      }

      int length = Math.max(0, source.readableBytes());
      if (length == 0) {
         return PacketCaptureState.EMPTY_BYTES;
      }

      byte[] bytes = new byte[length];
      source.getBytes(source.readerIndex(), bytes);
      return bytes;
   }

   public static String hexPreview(byte[] bytes, int maxBytes) {
      return AutismPayloadSupport.toHexPreview(bytes, maxBytes);
   }

   private static int readVarInt(byte[] bytes) {
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

   public static int numericId(String protocolPhase, String direction, PacketType<?> packetType) {
      if (packetType == null) {
         return -1;
      } else {
         String phase = normalizePhase(protocolPhase);
         String flow = normalizeDirection(direction);
         if (!phase.isBlank() && !flow.isBlank()) {
            Map<String, Integer> ids = packetIdsByPhase.computeIfAbsent(phase + "|" + flow, key -> packetIds(phase, flow));
            Integer id = ids.get(packetType.toString());
            return id == null ? -1 : id;
         } else {
            return -1;
         }
      }
   }

   private static Map<String, Integer> packetIds(String phase, String flow) {
      DetailsProvider provider = detailsProvider(phase, flow);
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

   private static DetailsProvider detailsProvider(String phase, String flow) {
      boolean c2s = "C2S".equals(flow);
      if ("CONFIGURATION".equals(phase)) {
         return c2s ? ConfigurationProtocols.SERVERBOUND_TEMPLATE : ConfigurationProtocols.CLIENTBOUND_TEMPLATE;
      } else if ("PLAY".equals(phase)) {
         return c2s ? GameProtocols.SERVERBOUND_TEMPLATE : GameProtocols.CLIENTBOUND_TEMPLATE;
      } else {
         return null;
      }
   }

   private static String normalizePhase(String protocolPhase) {
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

   private static String normalizeDirection(String direction) {
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

   public record ChannelCapture(boolean init_Z, long init_J, byte[] init_BArr, byte[] load_BArr, long load_J, long read) {
      static PacketWireCaptureStore.ChannelCapture init_bn_DataUtil() {
         return new PacketWireCaptureStore.ChannelCapture(true, System.currentTimeMillis(), new byte[0], new byte[0], 0L, 0L);
      }

      PacketWireCaptureStore.ChannelCapture withEnabled(boolean value) {
         return new PacketWireCaptureStore.ChannelCapture(
            value, value && this.init_J == 0L ? System.currentTimeMillis() : this.init_J, this.init_BArr, this.load_BArr, this.load_J, this.read
         );
      }

      PacketWireCaptureStore.ChannelCapture withCapture(String direction, byte[] bytes) {
         long now = System.currentTimeMillis();
         return !"C2S".equalsIgnoreCase(direction) && !"OUT".equalsIgnoreCase(direction)
            ? new PacketWireCaptureStore.ChannelCapture(this.init_Z, this.init_J, bytes == null ? new byte[0] : (byte[])bytes.clone(), this.load_BArr, now, this.read)
            : new PacketWireCaptureStore.ChannelCapture(this.init_Z, this.init_J, this.init_BArr, bytes == null ? new byte[0] : (byte[])bytes.clone(), this.load_J, now);
      }

      public byte[] init_BArr() {
         return this.init_BArr == null ? new byte[0] : (byte[])this.init_BArr.clone();
      }

      public byte[] load_BArr() {
         return this.load_BArr == null ? new byte[0] : (byte[])this.load_BArr.clone();
      }
   }

   public record PacketCapture(String direction, String load, String packetClassName, String write, int packetId, byte[] plaintext, long init_J) {
      public byte[] plaintext() {
         return this.plaintext == null ? new byte[0] : (byte[])this.plaintext.clone();
      }
   }
}
