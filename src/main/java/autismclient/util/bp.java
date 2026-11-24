package autismclient.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;

public final class bp {
   private static final Minecraft init = Minecraft.getInstance();

   private bp() {
   }

   public static bp.Inner_CachePool init_bp_CachePool(Class<? extends Packet<?>> packetClass, byte[] plaintextBytes, String protocolPhase) {
      ByteBuf buf = Unpooled.wrappedBuffer(plaintextBytes == null ? new byte[0] : plaintextBytes);

      try {
         int packetId = init(buf);
         byte[] body = new byte[buf.readableBytes()];
         if (body.length > 0) {
            buf.readBytes(body);
         }

         Packet<?> packet = init_Packet(packetClass, body, protocolPhase);
         return new bp.Inner_CachePool(packet, packetId, body, "minecraft full packet STREAM_CODEC");
      } finally {
         buf.release();
      }
   }

   public static Packet<?> init_Packet(Class<? extends Packet<?>> packetClass, byte[] bodyBytes, String protocolPhase) {
      if (packetClass == null) {
         throw new IllegalArgumentException("Packet class is missing.");
      }

      byte[] body = bodyBytes == null ? new byte[0] : (byte[])bodyBytes.clone();
      if (packetClass != ServerboundCustomPayloadPacket.class && packetClass != ClientboundCustomPayloadPacket.class) {
         List<bp.Inner_StreamUtil> codecs = init(packetClass, protocolPhase);
         if (codecs.isEmpty()) {
            throw new IllegalArgumentException("No STREAM_CODEC found for " + packetClass.getName() + ".");
         }

         Throwable firstFailure = null;

         for (bp.Inner_StreamUtil candidate : codecs) {
            for (bp.Inner_DataUtil factory : init_List(body)) {
               ByteBuf buffer = factory.create();

               try {
                  Object decoded = candidate.init(buffer);
                  if (!(decoded instanceof Packet<?> packet)) {
                     throw new IllegalArgumentException(
                        "Codec " + candidate.init_Str + " returned " + (decoded == null ? "null" : decoded.getClass().getName()) + ", not a packet."
                     );
                  }

                  if (!packetClass.isInstance(packet)) {
                     throw new IllegalArgumentException(
                        "Codec " + candidate.init_Str + " returned " + packet.getClass().getName() + ", expected " + packetClass.getName() + "."
                     );
                  }

                  if (buffer.readableBytes() > 0) {
                     throw new IllegalArgumentException("Codec " + candidate.init_Str + " left " + buffer.readableBytes() + " trailing byte(s).");
                  }

                  return packet;
               } catch (Throwable t) {
                  if (firstFailure == null) {
                     firstFailure = t;
                  }
               } finally {
                  buffer.release();
               }
            }
         }

         throw new IllegalArgumentException(init(firstFailure));
      } else {
         return init(packetClass, body);
      }
   }

   public static byte[] init_BArr(byte[] plaintextBytes) {
      ByteBuf buf = Unpooled.wrappedBuffer(plaintextBytes == null ? new byte[0] : plaintextBytes);

      try {
         init(buf);
         byte[] body = new byte[buf.readableBytes()];
         if (body.length > 0) {
            buf.readBytes(body);
         }

         return body;
      } finally {
         buf.release();
      }
   }

   private static List<bp.Inner_StreamUtil> init(Class<?> packetClass, String protocolPhase) {
      List<bp.Inner_StreamUtil> candidates = new ArrayList<>();

      for (Class<?> cursor = packetClass; cursor != null && cursor != Object.class; cursor = cursor.getSuperclass()) {
         for (Field field : cursor.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && StreamCodec.class.isAssignableFrom(field.getType())) {
               try {
                  field.setAccessible(true);
                  if (field.get(null) instanceof StreamCodec codec) {
                     candidates.add(new bp.Inner_StreamUtil(field.getName(), codec));
                  }
               } catch (Throwable var10) {
               }
            }
         }
      }

      String phase = protocolPhase == null ? "" : protocolPhase.toLowerCase(Locale.ROOT);
      candidates.sort(Comparator.comparingInt(candidate -> init(candidate.init_Str, phase)));
      return candidates;
   }

   private static int init(String fieldName, String phase) {
      String name = fieldName == null ? "" : fieldName.toLowerCase(Locale.ROOT);
      if (phase.contains("config") && name.contains("config")) {
         return 0;
      }

      if (!phase.contains("play") && !phase.contains("game") || !name.contains("game") && !name.equals("stream_codec")) {
         if (name.equals("stream_codec")) {
            return 1;
         } else if (name.contains("game")) {
            return 2;
         } else {
            return name.contains("config") ? 3 : 4;
         }
      } else {
         return 0;
      }
   }

   private static List<bp.Inner_DataUtil> init_List(byte[] bodyBytes) {
      byte[] bytes = bodyBytes == null ? new byte[0] : (byte[])bodyBytes.clone();
      List<bp.Inner_DataUtil> out = new ArrayList<>();
      RegistryAccess access = init();
      if (access != null) {
         out.add(() -> new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer((byte[])bytes.clone()), access));
      }

      out.add(() -> new FriendlyByteBuf(Unpooled.wrappedBuffer((byte[])bytes.clone())));
      out.add(() -> Unpooled.wrappedBuffer((byte[])bytes.clone()));
      return out;
   }

   private static RegistryAccess init() {
      try {
         if (init != null && init.level != null) {
            return init.level.registryAccess();
         }

         if (init != null && init.getConnection() != null) {
            return init.getConnection().registryAccess();
         }
      } catch (Throwable var1) {
      }

      return null;
   }

   private static Packet<?> init(Class<? extends Packet<?>> packetClass, byte[] body) {
      FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(body == null ? new byte[0] : body));

      try {
         Identifier channel = buf.readIdentifier();
         byte[] payload = new byte[buf.readableBytes()];
         if (payload.length > 0) {
            buf.readBytes(payload);
         }

         AutismPayloadSupport.RawCustomPacketPayload rawPayload = new AutismPayloadSupport.RawCustomPacketPayload(channel, payload);
         AutismPayloadSupport.init(rawPayload, payload);
         if (packetClass == ServerboundCustomPayloadPacket.class) {
            bD.init_V(channel.toString());
            return new ServerboundCustomPayloadPacket(rawPayload);
         } else {
            return new ClientboundCustomPayloadPacket(rawPayload);
         }
      } catch (Throwable t) {
         throw new IllegalArgumentException("Custom payload body must be <channel identifier><payload bytes>: " + init(t));
      } finally {
         buf.release();
      }
   }

   private static int init(ByteBuf buf) {
      int value = 0;
      int position = 0;

      while (buf.isReadable()) {
         byte currentByte = buf.readByte();
         value |= (currentByte & 127) << position;
         if ((currentByte & 128) == 0) {
            return value;
         }

         position += 7;
         if (position >= 32) {
            throw new IllegalArgumentException("Packet id VarInt is too long.");
         }
      }

      throw new IllegalArgumentException("Full packet bytes are missing the packet id VarInt.");
   }

   private static String init(Throwable t) {
      if (t == null) {
         return "unknown";
      }

      String message = t.getMessage();
      return message != null && !message.isBlank() ? message : t.getClass().getSimpleName();
   }

   public record Inner_CachePool(Packet<?> init_Packet, int init_I, byte[] init_BArr, String init_Str) {
      public Inner_CachePool {
         init_BArr = init_BArr == null ? new byte[0] : (byte[])init_BArr.clone();
         init_Str = init_Str != null && !init_Str.isBlank() ? init_Str : "minecraft STREAM_CODEC";
      }

      public byte[] init_BArr() {
         return (byte[])this.init_BArr.clone();
      }
   }

   private interface Inner_DataUtil {
      ByteBuf create();
   }

   private record Inner_StreamUtil(String init_Str, StreamCodec<ByteBuf, ?> init_StreamCodec) {
      Object init(ByteBuf buffer) {
         return this.init_StreamCodec.decode(buffer);
      }
   }
}
