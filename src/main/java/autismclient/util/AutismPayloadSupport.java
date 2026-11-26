package autismclient.util;

import com.google.common.collect.MapMaker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.CommonPacketTypes;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public final class AutismPayloadSupport {
   // Weak identity keys: payloads and packets are records, so equal-valued instances must not share an entry.
   private static final Map<CustomPacketPayload, byte[]> init_Map = new MapMaker().weakKeys().makeMap();
   private static final Map<Packet<?>, String> load_Map = new MapMaker().weakKeys().makeMap();
   private static final int init_I = 2147483639;
   private static final int load_I = 64;
   private static final int read = 32767;

   private AutismPayloadSupport() {
   }

   public static <T extends FriendlyByteBuf, V> StreamCodec<T, V> init(Identifier id, int maxBytes, StreamCodec<T, V> original) {
      return StreamCodec.of((buf, payload) -> {
         if (!init_Z()) {
            original.encode(buf, payload);
         } else {
            byte[] rawBytes = init(payload);
            if (rawBytes != null) {
               if (rawBytes.length > maxBytes) {
                  throw new IllegalArgumentException("Payload may not be larger than " + maxBytes + " bytes");
               }

               buf.writeBytes(rawBytes);
            } else {
               original.encode(buf, payload);
            }
         }
      }, buf -> {
         int start = buf.readerIndex();
         V decoded = original.decode(buf);
         int end = buf.readerIndex();
         if (init_Z() && decoded instanceof CustomPacketPayload payload) {
            byte[] rawBytes = new byte[Math.max(0, end - start)];
            if (rawBytes.length > 0) {
               buf.getBytes(start, rawBytes);
            }

            load(payload, rawBytes);
         }

         return decoded;
      });
   }

   private static boolean init_Z() {
      return PacketCaptureState.load(PacketCaptureState.load_J());
   }

   public static void init(CustomPacketPayload payload, byte[] rawBytes) {
      if (!autismclient.modules.aD.read_Z() && payload != null) {
         init_Map.put(payload, rawBytes == null ? new byte[0] : (byte[])rawBytes.clone());
      }
   }

   public static void init_V(CustomPacketPayload payload, String channel, byte[] encodedBytes) {
      if (!autismclient.modules.aD.read_Z() && payload != null) {
         byte[] body = init(encodedBytes, channel);
         load(payload, body);
      }
   }

   private static void load(CustomPacketPayload payload, byte[] rawBytes) {
      if (payload != null) {
         init_Map.put(payload, rawBytes == null ? PacketCaptureState.init_BArr : rawBytes);
      }
   }

   public static byte[] init_BArr(CustomPacketPayload payload) {
      if (payload == null) {
         return null;
      }

      byte[] bytes = init_Map.get(payload);
      return bytes == null ? null : (byte[])bytes.clone();
   }

   public static AutismPayloadSupport.PayloadEntry init_AutismPayloadSuppo(Packet<?> packet, String direction) {
      if (packet == null) {
         return null;
      }

      CustomPacketPayload payload = init(packet);
      if (payload == null) {
         return null;
      }

      String channel = init_Str(payload);
      PacketWireCaptureStore.Inner_StreamUtil packetSnapshot = PacketWireCaptureStore.init(packet);
      AutismPayloadSupport.EncodedBytes payloadBytes = init(packetSnapshot, channel);
      if (payloadBytes == null) {
         payloadBytes = init_AutismPayloadSuppo(payload);
      }

      byte[] rawBytes = payloadBytes.init_BArr();
      Integer commandApiValue = init_Integer(payload, channel, rawBytes);
      String dump = init(direction, channel, rawBytes, commandApiValue);
      String protocolPhase = packetSnapshot == null ? load_Map.getOrDefault(packet, "") : packetSnapshot.load();
      int packetId = packetSnapshot == null ? -1 : packetSnapshot.init_I();
      if (packetId < 0) {
         packetId = PacketWireCaptureStore.init(protocolPhase, direction, packet.type());
      }

      return new AutismPayloadSupport.PayloadEntry(
         direction,
         channel,
         rawBytes,
         rawBytes.length,
         commandApiValue,
         dump,
         packet instanceof ServerboundCustomPayloadPacket,
         packet instanceof ClientboundCustomPayloadPacket,
         payload.getClass().getName(),
         protocolPhase,
         packetId,
         payloadBytes.init_Str()
      );
   }

   public static AutismPayloadSupport.PayloadEntry init(byte[] frameBytes, String direction, String protocolPhase) {
      if (frameBytes != null && frameBytes.length != 0) {
         byte[] frame = (byte[])frameBytes.clone();
         int[] index = new int[]{0};
         Integer packetId = init_Integer(frame, index);
         if (packetId != null && packetId >= 0 && index[0] < frame.length) {
            boolean c2s = direction != null && direction.equalsIgnoreCase("C2S");
            boolean s2c = direction != null && direction.equalsIgnoreCase("S2C");
            int expectedId = PacketWireCaptureStore.init(
               protocolPhase, direction, c2s ? CommonPacketTypes.SERVERBOUND_CUSTOM_PAYLOAD : CommonPacketTypes.CLIENTBOUND_CUSTOM_PAYLOAD
            );
            if (expectedId >= 0 && packetId != expectedId) {
               return null;
            }

            int channelStart = index[0];
            String channel = init_Str(frame, index);
            if (channel != null && !channel.isBlank()) {
               try {
                  init_Identifier(channel);
               } catch (Throwable ignored) {
                  return null;
               }

               if (index[0] > channelStart && index[0] <= frame.length) {
                  byte[] body = new byte[Math.max(0, frame.length - index[0])];
                  if (body.length > 0) {
                     System.arraycopy(frame, index[0], body, 0, body.length);
                  }

                  AutismPayloadSupport.RawCustomPacketPayload payload = new AutismPayloadSupport.RawCustomPacketPayload(init_Identifier(channel), body);
                  Integer commandApiValue = init_Integer(payload, channel, body);
                  String dump = init(direction, channel, body, commandApiValue);
                  String payloadClassName = c2s ? ServerboundCustomPayloadPacket.class.getName() : ClientboundCustomPayloadPacket.class.getName();
                  return new AutismPayloadSupport.PayloadEntry(
                     direction,
                     channel,
                     body,
                     body.length,
                     commandApiValue,
                     dump,
                     c2s,
                     s2c,
                     payloadClassName,
                     protocolPhase,
                     packetId,
                     PayloadEditorModel.Inner_StreamUtil.CAPTURED_EXACT.init()
                  );
               } else {
                  return null;
               }
            } else {
               return null;
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static void init_V(Packet<?> packet, String protocolPhase) {
      if (packet != null && protocolPhase != null && !protocolPhase.isBlank()) {
         load_Map.put(packet, protocolPhase);
      }
   }

   public static CustomPacketPayload init(Packet<?> packet) {
      if (packet instanceof ServerboundCustomPayloadPacket c2s) {
         return c2s.payload();
      } else {
         return packet instanceof ClientboundCustomPayloadPacket s2c ? s2c.payload() : null;
      }
   }

   public static String init_Str(CustomPacketPayload payload) {
      if (payload == null) {
         return "";
      }

      try {
         Type<?> type = payload.type();
         if (type != null && type.id() != null) {
            return type.id().toString();
         }
      } catch (Throwable var2) {
      }

      return payload instanceof DiscardedPayload unknown && unknown.id() != null ? unknown.id().toString() : "";
   }

   public static byte[] load(CustomPacketPayload payload) {
      return init_AutismPayloadSuppo(payload).init_BArr();
   }

   private static AutismPayloadSupport.EncodedBytes init_AutismPayloadSuppo(CustomPacketPayload payload) {
      if (payload == null) {
         return new AutismPayloadSupport.EncodedBytes(new byte[0], PayloadEditorModel.Inner_StreamUtil.REENCODED_FROM_PAYLOAD.init());
      } else if (payload instanceof AutismPayloadSupport.RawCustomPacketPayload raw) {
         return new AutismPayloadSupport.EncodedBytes(raw.bytes(), PayloadEditorModel.Inner_StreamUtil.CAPTURED_EXACT.init());
      } else {
         byte[] remembered = init_BArr(payload);
         if (remembered != null) {
            return new AutismPayloadSupport.EncodedBytes(remembered, PayloadEditorModel.Inner_StreamUtil.CAPTURED_EXACT.init());
         }

         byte[] codecBytes = read(payload);
         if (codecBytes != null) {
            return new AutismPayloadSupport.EncodedBytes(codecBytes, PayloadEditorModel.Inner_StreamUtil.REENCODED_FROM_PAYLOAD.init());
         }

         try {
            Method writeMethod = payload.getClass().getMethod("write", FriendlyByteBuf.class);
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            writeMethod.invoke(payload, buf);
            return new AutismPayloadSupport.EncodedBytes(init(buf), PayloadEditorModel.Inner_StreamUtil.REENCODED_FROM_PAYLOAD.init());
         } catch (Throwable var5) {
            return new AutismPayloadSupport.EncodedBytes(new byte[0], PayloadEditorModel.Inner_StreamUtil.REENCODED_FROM_PAYLOAD.init());
         }
      }
   }

   private static AutismPayloadSupport.EncodedBytes init(PacketWireCaptureStore.Inner_StreamUtil packetSnapshot, String expectedChannel) {
      if (packetSnapshot == null) {
         return null;
      }

      byte[] bytes = packetSnapshot.init_BArr();
      if (bytes.length == 0) {
         return null;
      }

      int[] index = new int[]{0};
      Integer packetId = init_Integer(bytes, index);
      if (packetId != null && packetId >= 0 && index[0] < bytes.length) {
         String channel = init_Str(bytes, index);
         if (channel == null || channel.isBlank()) {
            return null;
         }

         if (expectedChannel != null && !expectedChannel.isBlank() && !channel.equals(expectedChannel)) {
            return null;
         }

         byte[] body = new byte[Math.max(0, bytes.length - index[0])];
         if (body.length > 0) {
            System.arraycopy(bytes, index[0], body, 0, body.length);
         }

         return new AutismPayloadSupport.EncodedBytes(body, PayloadEditorModel.Inner_StreamUtil.CAPTURED_EXACT.init());
      } else {
         return null;
      }
   }

   public static boolean init_Z(CustomPacketPayload payload) {
      if (payload == null) {
         return false;
      }

      String className = payload.getClass().getName();
      String simpleName = payload.getClass().getSimpleName();
      if (write_Z(className)) {
         return true;
      }

      if (write_Z(simpleName)) {
         return true;
      }

      String channel = init_Str(payload);
      return write_Z(channel) ? true : init_Z(payload.getClass());
   }

   public static boolean init_Z(Class<?> clazz) {
      if (clazz == null) {
         return false;
      }

      if (!clazz.isRecord()) {
         return false;
      }

      RecordComponent[] components = clazz.getRecordComponents();
      return components.length != 1 ? false : components[0].getType() == int.class || components[0].getType() == Integer.class;
   }

   public static Integer init_Integer(CustomPacketPayload payload, String channel, byte[] rawBytes) {
      Integer value = init_Integer(rawBytes);
      if (value == null) {
         return null;
      } else {
         return init(payload, channel, rawBytes, value) ? value : null;
      }
   }

   public static String init(AutismPayloadSupport.PayloadEntry snapshot, boolean includeCommandValue) {
      if (snapshot == null) {
         return "";
      }

      StringBuilder sb = new StringBuilder();
      String compactChannel = init_Str(snapshot.load_Str());
      if (!compactChannel.isBlank()) {
         sb.append('[').append(compactChannel).append(']');
      }

      if (snapshot.init_I() > 0 || sb.isEmpty()) {
         if (!sb.isEmpty()) {
            sb.append(' ');
         }

         sb.append(snapshot.init_I()).append('B');
      }

      if (includeCommandValue && snapshot.init_Integer() != null) {
         sb.append(" cmd=").append(snapshot.init_Integer());
      }

      String guess = sync(snapshot.init_BArr());
      if (!guess.isBlank()) {
         sb.append(" ").append(guess);
      }

      return sb.toString();
   }

   private static String sync(byte[] bytes) {
      if (bytes != null && bytes.length != 0) {
         String minecraftString = read(bytes);
         if (minecraftString != null && !minecraftString.isBlank()) {
            return "mc=\"" + load_Str(minecraftString) + "\"";
         }

         String text = load(bytes, 32);
         return text != null && !text.isBlank() ? "utf8=\"" + load_Str(text) + "\"" : "";
      } else {
         return "";
      }
   }

   private static String load_Str(String value) {
      String text = value == null ? "" : value.replace('\n', ' ').replace('\r', ' ').trim();
      return text.length() <= 32 ? text : text.substring(0, 29) + "...";
   }

   public static String init_Str(String channel) {
      String value = channel == null ? "" : channel.trim();
      if (!value.isEmpty() && value.length() > 30) {
         int colon = value.indexOf(58);
         if (colon > 0 && colon < value.length() - 1) {
            String namespace = value.substring(0, colon);
            String path = value.substring(colon + 1);
            int slash = Math.max(path.lastIndexOf(47), path.lastIndexOf(46));
            String tail = slash >= 0 && slash < path.length() - 1 ? path.substring(slash + 1) : path;
            String compact = namespace + ":" + tail;
            if (compact.length() <= 30) {
               return compact;
            }
         }

         return value.substring(0, 14) + "..." + value.substring(value.length() - 12);
      } else {
         return value;
      }
   }

   public static byte[] init_BArr(String text) {
      String value = text == null ? "" : text.trim();
      if (value.isEmpty()) {
         return new byte[0];
      }

      String compactHex = value.replace("0x", "").replace(" ", "").replace("\n", "").replace("\r", "").replace("\t", "");
      if (!compactHex.isEmpty() && compactHex.length() % 2 == 0 && compactHex.matches("(?i)[0-9a-f]+")) {
         byte[] out = new byte[compactHex.length() / 2];

         for (int i = 0; i < compactHex.length(); i += 2) {
            out[i / 2] = (byte)Integer.parseInt(compactHex.substring(i, i + 2), 16);
         }

         return out;
      } else {
         return Base64.getDecoder().decode(value);
      }
   }

   public static byte[] init(byte[] encodedBytes, String expectedChannel) {
      if (encodedBytes != null && encodedBytes.length != 0) {
         byte[] raw = (byte[])encodedBytes.clone();
         int[] index = new int[]{0};
         String channel = init_Str(raw, index);
         if (channel == null || channel.isBlank()) {
            return raw;
         }

         if (expectedChannel != null && !expectedChannel.isBlank() && !channel.equals(expectedChannel)) {
            return raw;
         }

         byte[] body = new byte[Math.max(0, raw.length - index[0])];
         if (body.length > 0) {
            System.arraycopy(raw, index[0], body, 0, body.length);
         }

         return body;
      } else {
         return new byte[0];
      }
   }

   public static String init_Str(byte[] bytes) {
      if (bytes != null && bytes.length != 0) {
         StringBuilder sb = new StringBuilder(bytes.length * 3);

         for (int i = 0; i < bytes.length; i++) {
            if (i > 0) {
               sb.append(' ');
            }

            sb.append(String.format(Locale.ROOT, "%02X", bytes[i] & 255));
         }

         return sb.toString();
      } else {
         return "";
      }
   }

   public static String load(byte[] bytes) {
      return bytes != null && bytes.length != 0 ? Base64.getEncoder().encodeToString(bytes) : "";
   }

   public static boolean init_Z(String channel) {
      return channel != null && "minecraft:brand".equals(channel.trim());
   }

   public static String init_Str() {
      try {
         String brand = ClientBrandRetriever.getClientModName();
         if (brand != null && !brand.isBlank()) {
            return brand;
         }
      } catch (Throwable var1) {
      }

      return "vanilla";
   }

   public static byte[] load_BArr(String value) {
      byte[] stringBytes = (value == null ? "" : value).getBytes(StandardCharsets.UTF_8);
      byte[] lengthBytes = init(stringBytes.length);
      byte[] out = new byte[lengthBytes.length + stringBytes.length];
      System.arraycopy(lengthBytes, 0, out, 0, lengthBytes.length);
      System.arraycopy(stringBytes, 0, out, lengthBytes.length, stringBytes.length);
      return out;
   }

   public static String read(byte[] rawBytes) {
      if (rawBytes != null && rawBytes.length != 0) {
         int[] index = new int[]{0};
         Integer length = init_Integer(rawBytes, index);
         if (length != null && length >= 0) {
            if (index[0] + length != rawBytes.length) {
               return null;
            }

            try {
               return new String(rawBytes, index[0], length, StandardCharsets.UTF_8);
            } catch (Throwable ignored) {
               return null;
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static String init_Str(byte[] bytes, int maxBytes) {
      if (bytes != null && bytes.length != 0) {
         int shown = maxBytes <= 0 ? bytes.length : Math.min(bytes.length, maxBytes);
         StringBuilder sb = new StringBuilder(shown * 3 + 16);

         for (int i = 0; i < shown; i++) {
            if (i > 0) {
               sb.append(' ');
            }

            sb.append(String.format(Locale.ROOT, "%02X", bytes[i] & 255));
         }

         if (shown < bytes.length) {
            sb.append(" ... +").append(bytes.length - shown).append("B");
         }

         return sb.toString();
      } else {
         return "";
      }
   }

   public static String write(byte[] bytes) {
      if (bytes != null && bytes.length != 0) {
         String text = new String(bytes, StandardCharsets.UTF_8);
         return !update(text) ? "" : text;
      } else {
         return "";
      }
   }

   public static List<String> init_List(String channel, byte[] rawBytes) {
      String normalizedChannel = channel == null ? "" : channel.trim().toLowerCase(Locale.ROOT);
      if (!"minecraft:register".equals(normalizedChannel)
         && !"register".equals(normalizedChannel)
         && !"minecraft:unregister".equals(normalizedChannel)
         && !"unregister".equals(normalizedChannel)) {
         return List.of();
      }

      if (rawBytes != null && rawBytes.length != 0) {
         LinkedHashSet<String> channels = new LinkedHashSet<>();
         init(channels, write(rawBytes));
         String mcString = read(rawBytes);
         if (mcString != null) {
            init(channels, mcString);
         }

         String javaUtf = update(rawBytes);
         if (javaUtf != null) {
            init(channels, javaUtf);
         }

         return List.copyOf(channels);
      } else {
         return List.of();
      }
   }

   private static void init(Set<String> out, String text) {
      if (out != null && text != null && !text.isBlank()) {
         String[] parts = text.split("[\\u0000,;\\s]+");

         for (String part : parts) {
            String token = part == null ? "" : part.trim().toLowerCase(Locale.ROOT);
            if (!token.isBlank()) {
               try {
                  Identifier id = Identifier.tryParse(token);
                  if (id != null && token.indexOf(58) > 0 && !"minecraft:register".equals(token) && !"minecraft:unregister".equals(token)) {
                     out.add(token);
                  }
               } catch (Throwable var9) {
               }
            }
         }
      }
   }

   public static String load(byte[] bytes, int maxChars) {
      String text = write(bytes);
      if (text.isEmpty()) {
         return "";
      }

      String normalized = text.replace("\r\n", "\n").replace('\r', '\n');
      return maxChars > 0 && normalized.length() > maxChars ? normalized.substring(0, Math.max(0, maxChars - 3)) + "..." : normalized;
   }

   public static ServerboundCustomPayloadPacket init_ServerboundCustomP(String channel, byte[] bytes) {
      Identifier id = init_Identifier(channel);
      PayloadSubscriptionManager.init_V(id.toString());
      AutismPayloadSupport.RawCustomPacketPayload payload = new AutismPayloadSupport.RawCustomPacketPayload(id, bytes);
      init(payload, bytes);
      return new ServerboundCustomPayloadPacket(payload);
   }

   public static boolean init_Z(String channel, byte[] bytes) {
      return init_Z(channel, bytes, "");
   }

   public static boolean init_Z(String channel, byte[] bytes, String sourceProtocol) {
      return init(channel, bytes, sourceProtocol, true);
   }

   public static boolean load(String channel, byte[] bytes, String sourceProtocol) {
      return init(channel, bytes, sourceProtocol, false);
   }

   private static boolean init(String channel, byte[] bytes, String sourceProtocol, boolean notifyUser) {
      if (autismclient.modules.aD.read_Z()) {
         return false;
      }

      try {
         init_V(channel, bytes, sourceProtocol);
         ServerboundCustomPayloadPacket packet = init_ServerboundCustomP(channel, bytes);
         init(packet);
         DirectPacketSender.init(packet);
         return true;
      } catch (Throwable t) {
         if (notifyUser) {
            AutismClientMessaging.sendPrefixed("§cFailed to send payload: " + init(t));
         }

         return false;
      }
   }

   private static void init_V(String channel, byte[] bytes, String sourceProtocol) {
      Identifier id = init_Identifier(channel);
      if (load_Z(id.toString()) && !PayloadSubscriptionManager.init_Z()) {
         throw new IllegalStateException("Payload registration is locked. Unlock it in Packet Logger > Payload > Channels.");
      }

      int size = bytes == null ? 0 : bytes.length;
      if (size > 32767) {
         throw new IllegalArgumentException("Payload is " + size + " bytes, max is 32767");
      }

      if (read_Z(sourceProtocol) && load()) {
         throw new IllegalStateException("Configuration payloads must be sent during join/configuration, not after spawning.");
      }
   }

   private static boolean load_Z(String channel) {
      String normalized = channel == null ? "" : channel.trim().toLowerCase(Locale.ROOT);
      return normalized.equals("minecraft:register")
         || normalized.equals("minecraft:unregister")
         || normalized.equals("register")
         || normalized.equals("unregister");
   }

   private static boolean read_Z(String sourceProtocol) {
      if (sourceProtocol != null && !sourceProtocol.isBlank()) {
         String normalized = sourceProtocol.toLowerCase(Locale.ROOT);
         return normalized.contains("config");
      } else {
         return false;
      }
   }

   private static boolean load() {
      Minecraft mc = Minecraft.getInstance();
      return mc != null && mc.level != null && mc.player != null;
   }

   private static void init(ServerboundCustomPayloadPacket packet) {
      FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

      try {
         ServerboundCustomPayloadPacket.STREAM_CODEC.encode(buf, packet);
      } finally {
         buf.release();
      }
   }

   private static void init(ByteBuf buf, int value) {
      while ((value & -128) != 0) {
         buf.writeByte(value & 127 | 128);
         value >>>= 7;
      }

      buf.writeByte(value);
   }

   private static void init(ByteBuf buf, String value) {
      byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
      init(buf, bytes.length);
      buf.writeBytes(bytes);
   }

   public static void init(AutismPayloadSupport.RawCustomPacketPayload payload, ByteBuf buf) {
      String channelStr = payload.id().toString();
      byte[] channelBytes = channelStr.getBytes(StandardCharsets.UTF_8);
      init(buf, channelBytes.length);
      buf.writeBytes(channelBytes);
      byte[] data = payload.bytes();
      buf.writeBytes(data);
   }

   public static Identifier init_Identifier(String channel) {
      if (channel != null && !channel.isBlank()) {
         try {
            return Identifier.parse(channel.trim());
         } catch (Throwable t) {
            throw new IllegalArgumentException("Invalid channel identifier: " + channel);
         }
      } else {
         throw new IllegalArgumentException("Channel cannot be blank");
      }
   }

   public static AutismPayloadSupport.PayloadEntry init_AutismPayloadSuppo(AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      if (entry == null) {
         return null;
      } else {
         AutismPayloadSupport.PayloadEntry cached = entry.init_AutismPayloadSuppo;
         if (entry.init_Packet == null) {
            return cached;
         } else {
            AutismPayloadSupport.PayloadEntry refreshed = init_AutismPayloadSuppo(entry.init_Packet, entry.init_Str);
            if (cached == null) {
               return refreshed;
            } else {
               return init(cached, refreshed) ? refreshed : cached;
            }
         }
      }
   }

   public static autismclient.util.macro.PayloadAction init_H(AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      AutismPayloadSupport.PayloadEntry snapshot = init_AutismPayloadSuppo(entry);
      if (snapshot == null) {
         return null;
      }

      autismclient.util.macro.PayloadAction action = new autismclient.util.macro.PayloadAction();
      action.channel = snapshot.load_Str();
      action.payloadData = init_Str(snapshot.init_BArr());
      action.payloadClassName = "";
      CustomPacketPayload payload = entry != null && entry.init_Packet != null ? init(entry.init_Packet) : null;
      action.payloadJson = PayloadJsonCodec.init(
         snapshot.init_Str(),
         action.channel,
         payload == null ? new AutismPayloadSupport.RawCustomPacketPayload(init_Identifier(action.channel), snapshot.init_BArr()) : payload
      );
      action.javaSource = "";
      action.payloadScriptEnabled = false;
      action.commandApiRecognized = snapshot.init_Integer() != null;
      action.commandApiOverride = false;
      action.commandApiValue = snapshot.init_Integer() == null ? 0 : snapshot.init_Integer();
      action.sourceDirection = snapshot.init_Str();
      action.sourceProtocol = snapshot.update();
      action.payloadDirection = snapshot.init_Str();
      action.payloadPhase = snapshot.update() != null && snapshot.update().toLowerCase(Locale.ROOT).contains("config") ? "CONFIGURATION" : "PLAY";
      action.payloadPacketId = snapshot.load_I();
      action.payloadProvenance = snapshot.sync();
      action.payloadFields = PayloadFieldCodec.init(init_List(snapshot.init_BArr()));
      action.payloadEncodingMode = init_Str(action.channel, snapshot.init_BArr());
      return action;
   }

   public static List<PayloadFieldCodec.Inner_CachePool> init_List(byte[] bytes) {
      byte[] safeBytes = bytes == null ? new byte[0] : bytes;
      String mcString = read(safeBytes);
      if (mcString != null) {
         return List.of(new PayloadFieldCodec.Inner_CachePool(PayloadFieldCodec.Inner_BufferHelper.MINECRAFT_STRING, mcString, true));
      }

      String writeUtf = update(safeBytes);
      if (writeUtf != null) {
         return List.of(new PayloadFieldCodec.Inner_CachePool(PayloadFieldCodec.Inner_BufferHelper.JAVA_WRITE_UTF, writeUtf, true));
      }

      String utf8 = write(safeBytes);
      return utf8.isBlank() && safeBytes.length != 0
         ? List.of(new PayloadFieldCodec.Inner_CachePool(PayloadFieldCodec.Inner_BufferHelper.HEX_BYTES, init_Str(safeBytes), true))
         : List.of(new PayloadFieldCodec.Inner_CachePool(PayloadFieldCodec.Inner_BufferHelper.STRING_BYTES, utf8, true));
   }

   public static String init_Str(String channel, byte[] bytes) {
      byte[] safeBytes = bytes == null ? new byte[0] : bytes;
      if (read(safeBytes) != null) {
         return "MINECRAFT_BYTEBUF";
      } else if (update(safeBytes) != null) {
         return "JAVA_DATA_OUTPUT";
      } else {
         return write(safeBytes).isBlank() && safeBytes.length != 0 ? "MANUAL_HEX" : "RAW_UTF8";
      }
   }

   public static String update(byte[] bytes) {
      if (bytes != null && bytes.length >= 2) {
         try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            String value = in.readUTF();
            return in.available() != 0 ? null : value;
         } catch (IOException | RuntimeException ignored) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static byte[] init_BArr(byte[] source, int value) {
      byte[] bytes = source == null ? new byte[4] : (byte[])source.clone();
      if (bytes.length < 4) {
         bytes = new byte[4];
      }

      ByteBuffer.wrap(bytes, 0, 4).order(ByteOrder.BIG_ENDIAN).putInt(value);
      return bytes;
   }

   public static byte[] init(FriendlyByteBuf buf) {
      if (buf == null) {
         return new byte[0];
      }

      byte[] bytes = new byte[buf.writerIndex()];
      if (bytes.length > 0) {
         buf.getBytes(0, bytes);
      }

      return bytes;
   }

   public static String init(Throwable t) {
      if (t == null) {
         return "Unknown error";
      }

      String message = t.getMessage();
      return message != null && !message.isBlank() ? message : t.getClass().getSimpleName();
   }

   private static byte[] init(Object payload) {
      if (payload instanceof AutismPayloadSupport.RawCustomPacketPayload raw) {
         return raw.bytes();
      } else {
         return payload instanceof CustomPacketPayload customPayload ? init_BArr(customPayload) : null;
      }
   }

   private static byte[] read(CustomPacketPayload payload) {
      StreamCodec codec = init_StreamCodec(payload);
      if (codec == null) {
         return null;
      }

      try {
         FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
         codec.encode(buf, payload);
         return init(buf);
      } catch (Throwable ignored) {
         return null;
      }
   }

   public static StreamCodec<?, ?> init_StreamCodec(CustomPacketPayload payload) {
      return payload == null ? null : init_StreamCodec(payload.getClass());
   }

   public static StreamCodec<?, ?> init_StreamCodec(Class<?> payloadClass) {
      if (payloadClass == null) {
         return null;
      }

      Object codecValue = init(payloadClass, "CODEC", StreamCodec.class);
      if (codecValue == null) {
         codecValue = init(payloadClass, StreamCodec.class);
      }

      return codecValue instanceof StreamCodec<?, ?> codec ? codec : null;
   }

   private static Object init(Class<?> owner, String fieldName, Class<?> expectedType) {
      if (owner != null && fieldName != null && !fieldName.isBlank() && expectedType != null) {
         for (Class<?> cursor = owner; cursor != null && cursor != Object.class; cursor = cursor.getSuperclass()) {
            try {
               Field field = cursor.getDeclaredField(fieldName);
               if (!Modifier.isStatic(field.getModifiers())) {
                  return null;
               }

               if (!expectedType.isAssignableFrom(field.getType())) {
                  return null;
               }

               if (!field.canAccess(null)) {
                  field.setAccessible(true);
               }

               return field.get(null);
            } catch (Throwable var5) {
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static Object init(Class<?> owner, Class<?> expectedType) {
      if (owner != null && expectedType != null) {
         for (Class<?> cursor = owner; cursor != null && cursor != Object.class; cursor = cursor.getSuperclass()) {
            for (Field field : cursor.getDeclaredFields()) {
               if (Modifier.isStatic(field.getModifiers()) && expectedType.isAssignableFrom(field.getType())) {
                  try {
                     if (!field.canAccess(null)) {
                        field.setAccessible(true);
                     }

                     Object value = field.get(null);
                     if (value != null) {
                        return value;
                     }
                  } catch (Throwable var8) {
                  }
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static boolean init(CustomPacketPayload payload, String channel, byte[] rawBytes, Integer intValue) {
      if (write_Z(payload == null ? null : payload.getClass().getName())) {
         return true;
      } else if (write_Z(payload == null ? null : payload.getClass().getSimpleName())) {
         return true;
      } else if (write_Z(channel)) {
         return true;
      } else {
         return rawBytes != null && rawBytes.length == 4 && intValue != null ? Math.abs(intValue.intValue() - 2147483639L) <= 64L : false;
      }
   }

   private static boolean write_Z(String value) {
      String normalized = read_Str(value);
      return normalized.isEmpty() ? false : normalized.contains("commandapi") || normalized.contains("command") && normalized.contains("api");
   }

   private static String read_Str(String value) {
      if (value != null && !value.isBlank()) {
         StringBuilder sb = new StringBuilder(value.length());

         for (int i = 0; i < value.length(); i++) {
            char c = Character.toLowerCase(value.charAt(i));
            if (c >= 'a' && c <= 'z' || c >= '0' && c <= '9') {
               sb.append(c);
            }
         }

         return sb.toString();
      } else {
         return "";
      }
   }

   private static Integer init_Integer(byte[] rawBytes) {
      if (rawBytes != null && rawBytes.length >= 4) {
         try {
            return ByteBuffer.wrap(rawBytes, 0, 4).order(ByteOrder.BIG_ENDIAN).getInt();
         } catch (Throwable ignored) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static byte[] init(int value) {
      byte[] out = new byte[5];
      int index = 0;

      while ((value & -128) != 0) {
         out[index++] = (byte)(value & 127 | 128);
         value >>>= 7;
      }

      out[index++] = (byte)value;
      byte[] trimmed = new byte[index];
      System.arraycopy(out, 0, trimmed, 0, index);
      return trimmed;
   }

   private static Integer init_Integer(byte[] bytes, int[] indexRef) {
      if (bytes != null && indexRef != null && indexRef.length != 0) {
         int value = 0;
         int position = 0;
         int index = Math.max(0, indexRef[0]);

         while (index < bytes.length) {
            byte currentByte = bytes[index++];
            value |= (currentByte & 127) << position;
            if ((currentByte & 128) == 0) {
               indexRef[0] = index;
               return value;
            }

            position += 7;
            if (position >= 32) {
               return null;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static String init_Str(byte[] bytes, int[] indexRef) {
      Integer length = init_Integer(bytes, indexRef);
      if (length != null && length >= 0) {
         int index = Math.max(0, indexRef[0]);
         if (index + length > bytes.length) {
            return null;
         }

         try {
            String value = new String(bytes, index, length, StandardCharsets.UTF_8);
            indexRef[0] = index + length;
            return value;
         } catch (Throwable ignored) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static boolean init(AutismPayloadSupport.PayloadEntry cached, AutismPayloadSupport.PayloadEntry refreshed) {
      if (cached != null && refreshed != null) {
         if ((cached.load_Str() == null || cached.load_Str().isBlank()) && refreshed.load_Str() != null && !refreshed.load_Str().isBlank()) {
            return true;
         } else {
            return (cached.write() == null || cached.write().isBlank()) && refreshed.write() != null && !refreshed.write().isBlank()
               ? true
               : cached.init_BArr().length == 0 && refreshed.init_BArr().length > 0;
         }
      } else {
         return false;
      }
   }

   private static boolean update(String text) {
      if (text != null && !text.isBlank()) {
         int printable = 0;
         int suspicious = 0;

         for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '�') {
               suspicious += 3;
            } else if (c == '\n' || c == '\r' || c == '\t') {
               printable++;
            } else if (Character.isISOControl(c)) {
               suspicious++;
            } else {
               printable++;
            }
         }

         return printable == 0 ? false : suspicious * 5 < printable;
      } else {
         return false;
      }
   }

   private static String init(String direction, String channel, byte[] rawBytes, Integer commandApiValue) {
      StringBuilder sb = new StringBuilder();
      sb.append('{');
      sb.append("\"direction\":\"").append(write_Str(direction)).append("\"");
      sb.append(",\"channel\":\"").append(write_Str(channel)).append("\"");
      sb.append(",\"size\":").append(rawBytes == null ? 0 : rawBytes.length);
      sb.append(",\"payloadHex\":\"").append(write_Str(init_Str(rawBytes))).append("\"");
      sb.append(",\"payloadBase64\":\"").append(write_Str(load(rawBytes))).append("\"");
      if (commandApiValue != null) {
         sb.append(",\"commandApiValue\":").append(commandApiValue);
      }

      sb.append('}');
      return sb.toString();
   }

   private static String write_Str(String value) {
      return value != null && !value.isEmpty() ? value.replace("\\", "\\\\").replace("\"", "\\\"") : "";
   }

   private record EncodedBytes(byte[] init_BArr, String init_Str) {
      private EncodedBytes {
         init_BArr = init_BArr == null ? new byte[0] : (byte[])init_BArr.clone();
         init_Str = init_Str != null && !init_Str.isBlank() ? init_Str : PayloadEditorModel.Inner_StreamUtil.REENCODED_FROM_PAYLOAD.init();
      }

      public byte[] init_BArr() {
         return this.init_BArr == null ? new byte[0] : (byte[])this.init_BArr.clone();
      }
   }

   public record PayloadEntry(
      String init_Str,
      String load_Str,
      byte[] init_BArr,
      int init_I,
      Integer init_Integer,
      String read,
      boolean init_Z,
      boolean load_Z,
      String write,
      String update,
      int load_I,
      String sync
   ) {
      public byte[] init_BArr() {
         return this.init_BArr == null ? new byte[0] : (byte[])this.init_BArr.clone();
      }
   }

   public record RawCustomPacketPayload(Identifier id, byte[] bytes) implements CustomPacketPayload {
      public RawCustomPacketPayload {
         bytes = bytes == null ? new byte[0] : (byte[])bytes.clone();
      }

      @Override
      public Type<AutismPayloadSupport.RawCustomPacketPayload> type() {
         return new Type<>(this.id);
      }

      public void write(FriendlyByteBuf buf) {
         if (this.bytes.length > 0) {
            buf.writeBytes(this.bytes);
         }
      }

      public byte[] bytes() {
         return (byte[])this.bytes.clone();
      }
   }
}
