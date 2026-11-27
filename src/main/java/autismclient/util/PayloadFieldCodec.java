package autismclient.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class PayloadFieldCodec {
   private static final int LARGE_PAYLOAD_WARNING_BYTES = 8192;
   private static final int MAX_SERVERBOUND_BYTES = 32767;

   private PayloadFieldCodec() {
   }

   public static PayloadFieldCodec.Inner_JsonUtil fromAction(autismclient.util.macro.PayloadAction action) {
      if (action == null) {
         return defaultBrandTemplate();
      }

      String channel = action.channel != null && !action.channel.isBlank() ? action.channel.strip() : "minecraft:brand";
      PayloadFieldCodec.Inner_ConfigStore direction = PayloadFieldCodec.Inner_ConfigStore.parse(
         action.payloadDirection != null && !action.payloadDirection.isBlank() ? action.payloadDirection : action.sourceDirection
      );
      PayloadFieldCodec.Inner_TaskQueue phase = PayloadFieldCodec.Inner_TaskQueue.parse(
         action.payloadPhase != null && !action.payloadPhase.isBlank() ? action.payloadPhase : action.sourceProtocol
      );
      PayloadFieldCodec.Inner_StreamUtil mode = PayloadFieldCodec.Inner_StreamUtil.parse(action.payloadEncodingMode, channel);
      List<PayloadFieldCodec.Inner_CachePool> fields = parseFields(action.payloadFields);
      if (fields.isEmpty() && action.payloadData != null && !action.payloadData.isBlank()) {
         fields = List.of(new PayloadFieldCodec.Inner_CachePool(PayloadFieldCodec.Inner_BufferHelper.HEX_BYTES, action.payloadData, true));
         mode = PayloadFieldCodec.Inner_StreamUtil.MANUAL_HEX;
      }

      if (fields.isEmpty()) {
         if (AutismPayloadSupport.isBrandChannel(channel)) {
            fields = List.of(new PayloadFieldCodec.Inner_CachePool(PayloadFieldCodec.Inner_BufferHelper.MINECRAFT_STRING, AutismPayloadSupport.defaultClientBrand(), true));
            mode = PayloadFieldCodec.Inner_StreamUtil.MINECRAFT_BYTEBUF;
         } else {
            fields = List.of(new PayloadFieldCodec.Inner_CachePool(PayloadFieldCodec.Inner_BufferHelper.RAW_UTF8_STRING, "", true));
         }
      }

      return new PayloadFieldCodec.Inner_JsonUtil(channel, direction, phase, mode, fields);
   }

   public static PayloadFieldCodec.Inner_JsonUtil defaultBrandTemplate() {
      return new PayloadFieldCodec.Inner_JsonUtil(
         "minecraft:brand",
         PayloadFieldCodec.Inner_ConfigStore.C2S,
         PayloadFieldCodec.Inner_TaskQueue.PLAY,
         PayloadFieldCodec.Inner_StreamUtil.MINECRAFT_BYTEBUF,
         List.of(new PayloadFieldCodec.Inner_CachePool(PayloadFieldCodec.Inner_BufferHelper.MINECRAFT_STRING, AutismPayloadSupport.defaultClientBrand(), true))
      );
   }

   public static PayloadFieldCodec.Inner_JsonUtil pingTemplate() {
      return new PayloadFieldCodec.Inner_JsonUtil(
         "testmod:ping",
         PayloadFieldCodec.Inner_ConfigStore.C2S,
         PayloadFieldCodec.Inner_TaskQueue.PLAY,
         PayloadFieldCodec.Inner_StreamUtil.RAW_UTF8,
         List.of(new PayloadFieldCodec.Inner_CachePool(PayloadFieldCodec.Inner_BufferHelper.RAW_UTF8_STRING, "hello-from-client", true))
      );
   }

   public static PayloadFieldCodec.Inner_JsonUtil brandTemplate(String brand) {
      return new PayloadFieldCodec.Inner_JsonUtil(
         "minecraft:brand",
         PayloadFieldCodec.Inner_ConfigStore.C2S,
         PayloadFieldCodec.Inner_TaskQueue.PLAY,
         PayloadFieldCodec.Inner_StreamUtil.MINECRAFT_BYTEBUF,
         List.of(new PayloadFieldCodec.Inner_CachePool(PayloadFieldCodec.Inner_BufferHelper.MINECRAFT_STRING, brand != null && !brand.isBlank() ? brand : AutismPayloadSupport.defaultClientBrand(), true))
      );
   }

   public static PayloadFieldCodec.Inner_JsonUtil bungeeGetServerTemplate() {
      return new PayloadFieldCodec.Inner_JsonUtil(
         "bungeecord:main",
         PayloadFieldCodec.Inner_ConfigStore.C2S,
         PayloadFieldCodec.Inner_TaskQueue.PLAY,
         PayloadFieldCodec.Inner_StreamUtil.JAVA_DATA_OUTPUT,
         List.of(new PayloadFieldCodec.Inner_CachePool(PayloadFieldCodec.Inner_BufferHelper.JAVA_WRITE_UTF, "GetServer", true))
      );
   }

   public static PayloadFieldCodec.Inner_JsonUtil bungeePlayerCountTemplate() {
      return new PayloadFieldCodec.Inner_JsonUtil(
         "bungeecord:main",
         PayloadFieldCodec.Inner_ConfigStore.C2S,
         PayloadFieldCodec.Inner_TaskQueue.PLAY,
         PayloadFieldCodec.Inner_StreamUtil.JAVA_DATA_OUTPUT,
         List.of(new PayloadFieldCodec.Inner_CachePool(PayloadFieldCodec.Inner_BufferHelper.JAVA_WRITE_UTF, "PlayerCount", true), new PayloadFieldCodec.Inner_CachePool(PayloadFieldCodec.Inner_BufferHelper.JAVA_WRITE_UTF, "ALL", true))
      );
   }

   public static String formatFields(List<PayloadFieldCodec.Inner_CachePool> fields) {
      if (fields != null && !fields.isEmpty()) {
         StringBuilder sb = new StringBuilder();

         for (PayloadFieldCodec.Inner_CachePool field : fields) {
            if (field != null) {
               if (!sb.isEmpty()) {
                  sb.append('\n');
               }

               if (!field.enabled()) {
                  sb.append("# ");
               }

               sb.append(field.type().label()).append(" = ").append(field.value());
            }
         }

         return sb.toString();
      } else {
         return "";
      }
   }

   public static List<PayloadFieldCodec.Inner_CachePool> parseFields(String text) {
      if (text != null && !text.isBlank()) {
         List<PayloadFieldCodec.Inner_CachePool> fields = new ArrayList<>();
         String[] lines = text.replace("\r\n", "\n").replace('\r', '\n').split("\n");

         for (String rawLine : lines) {
            if (rawLine != null) {
               String line = rawLine.strip();
               if (!line.isEmpty()) {
                  boolean enabled = true;
                  if (line.startsWith("#")) {
                     enabled = false;
                     line = line.substring(1).strip();
                  }

                  int split = line.indexOf(61);
                  if (split < 0) {
                     split = line.indexOf(58);
                  }

                  String typeText = split < 0 ? "RAW_UTF8_STRING" : line.substring(0, split).strip();
                  String value = split < 0 ? line : line.substring(split + 1).strip();
                  fields.add(new PayloadFieldCodec.Inner_CachePool(PayloadFieldCodec.Inner_BufferHelper.parse(typeText), value, enabled));
               }
            }
         }

         return fields;
      } else {
         return Collections.emptyList();
      }
   }

   public static String nextEncodingMode(String currentMode, String channel) {
      PayloadFieldCodec.Inner_StreamUtil current = PayloadFieldCodec.Inner_StreamUtil.parse(currentMode, channel);
      PayloadFieldCodec.Inner_StreamUtil[] values = PayloadFieldCodec.Inner_StreamUtil.values();
      return values[(current.ordinal() + 1) % values.length].name();
   }

   public static String toggleDirection(String current) {
      return PayloadFieldCodec.Inner_ConfigStore.parse(current) == PayloadFieldCodec.Inner_ConfigStore.C2S ? PayloadFieldCodec.Inner_ConfigStore.S2C.name() : PayloadFieldCodec.Inner_ConfigStore.C2S.name();
   }

   public static String togglePhase(String current) {
      return PayloadFieldCodec.Inner_TaskQueue.parse(current) == PayloadFieldCodec.Inner_TaskQueue.PLAY ? PayloadFieldCodec.Inner_TaskQueue.CONFIGURATION.name() : PayloadFieldCodec.Inner_TaskQueue.PLAY.name();
   }

   public static PayloadFieldCodec.Inner_NetHelper buildPreview(byte[] bytes) {
      byte[] raw = bytes == null ? new byte[0] : bytes;
      return new PayloadFieldCodec.Inner_NetHelper(AutismPayloadSupport.toHexPreview(raw, 96), previewUtf8(raw), previewMcString(raw), previewJavaUtf(raw));
   }

   public static void applyTemplateToAction(autismclient.util.macro.PayloadAction action, PayloadFieldCodec.Inner_JsonUtil template) {
      if (action != null && template != null) {
         action.channel = template.channel();
         action.payloadDirection = template.direction().name();
         action.payloadPhase = template.phase().name();
         action.payloadEncodingMode = template.encodingMode().name();
         action.payloadFields = formatFields(template.fields());
         action.sourceDirection = action.payloadDirection;
         action.sourceProtocol = "";
         PayloadFieldCodec.Inner_DataUtil result = template.encode();
         if (result.isValid()) {
            action.payloadData = AutismPayloadSupport.toHexString(result.bytes());
         }
      }
   }

   private static void validateHeader(
      String channel, PayloadFieldCodec.Inner_ConfigStore direction, PayloadFieldCodec.Inner_TaskQueue phase, PayloadFieldCodec.Inner_StreamUtil mode, List<String> warnings, List<String> errors
   ) {
      if (direction == PayloadFieldCodec.Inner_ConfigStore.S2C) {
         warnings.add("S2C is inspect/replay metadata only. Sending from macros is serverbound C2S.");
      }

      if (phase == PayloadFieldCodec.Inner_TaskQueue.CONFIGURATION) {
         warnings.add("Configuration payloads usually cannot be replayed during Play.");
      }

      try {
         AutismPayloadSupport.parseChannelId(channel);
      } catch (Exception e) {
         errors.add(AutismPayloadSupport.describeError(e));
      }

      if (channel != null && !channel.contains(":")) {
         warnings.add("Use a namespaced channel like namespace:path.");
      }

      if (AutismPayloadSupport.isBrandChannel(channel) && mode != PayloadFieldCodec.Inner_StreamUtil.MINECRAFT_BYTEBUF) {
         warnings.add("minecraft:brand normally expects a Minecraft String.");
      }

      if ("bungeecord:main".equalsIgnoreCase(channel == null ? "" : channel.strip()) && mode != PayloadFieldCodec.Inner_StreamUtil.JAVA_DATA_OUTPUT) {
         warnings.add("bungeecord:main normally uses Java DataOutput/writeUTF fields.");
      }
   }

   private static void encodeField(ByteArrayOutputStream out, PayloadFieldCodec.Inner_CachePool field, PayloadFieldCodec.Inner_StreamUtil mode, List<String> warnings) throws Exception {
      String value = field.value();
      switch (field.type()) {
         case BYTE:
            out.write((byte)parseLongInRange(value, -128L, 127L));
            break;
         case UNSIGNED_BYTE:
            out.write((byte)parseLongInRange(value, 0L, 255L));
            break;
         case BOOLEAN:
            out.write(Boolean.parseBoolean(value.strip()) ? 1 : 0);
            break;
         case SHORT:
            writeBigEndianShort(out, (short)parseLongInRange(value, -32768L, 32767L));
            break;
         case UNSIGNED_SHORT:
            writeBigEndianShort(out, (short)parseLongInRange(value, 0L, 65535L));
            break;
         case CHAR:
            writeBigEndianShort(out, (short)parseCharCode(value));
            break;
         case INT:
            writeBigEndianInt(out, (int)parseLongInRange(value, -2147483648L, 2147483647L));
            break;
         case LONG:
            writeBigEndianLong(out, parseLongInRange(value, -9223372036854775808L, 9223372036854775807L));
            break;
         case FLOAT:
            writeBigEndianInt(out, Float.floatToIntBits(Float.parseFloat(value.strip())));
            break;
         case DOUBLE:
            writeBigEndianLong(out, Double.doubleToLongBits(Double.parseDouble(value.strip())));
            break;
         case JAVA_WRITE_UTF:
            DataOutputStream dos = new DataOutputStream(out);
            dos.writeUTF(unescapeString(value));
            dos.flush();
            break;
         case RAW_BYTES:
         case HEX_BYTES:
            out.write(decodeHexOrBase64(value));
            break;
         case BYTE_ARRAY:
            writeByteArrayField(out, value);
            break;
         case STRING_BYTES:
         case RAW_UTF8_STRING:
            out.write(unescapeString(value).getBytes(StandardCharsets.UTF_8));
            break;
         case VAR_INT:
         case ENUM_VAR_INT:
            writeVarInt(out, (int)parseLongInRange(value, -2147483648L, 2147483647L));
            break;
         case VAR_LONG:
            writeVarLong(out, parseLongInRange(value, -9223372036854775808L, 9223372036854775807L));
            break;
         case MINECRAFT_STRING:
         case IDENTIFIER:
            out.write(AutismPayloadSupport.encodeMcString(unescapeString(value)));
            break;
         case UUID_FIELD:
            writeUuid(out, UUID.fromString(value.strip()));
            break;
         case BLOCK_POS:
            writeBigEndianLong(out, packBlockPos(value));
            break;
         case OPTIONAL_VALUE:
            writeOptionalField(out, value, mode, warnings);
            break;
         case NBT:
            writeNbtField(out, value);
            break;
         case ITEM_STACK:
            writeItemStackField(out, value);
            break;
         case TEXT_COMPONENT:
         case JSON_STRING:
            writeComponentField(out, value);
      }
   }

   private static void writeNbtField(ByteArrayOutputStream out, String value) throws Exception {
      String text = value != null && !value.isBlank() ? value.strip() : "{}";
      CompoundTag tag = TagParser.parseCompoundFully(text);
      FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

      try {
         buf.writeNbt(tag);
         writeBufferBytes(out, buf);
      } finally {
         buf.release();
      }
   }

   private static void writeComponentField(ByteArrayOutputStream out, String value) {
      Component component = parseComponent(value);
      RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), currentRegistryAccess());

      try {
         ComponentSerialization.STREAM_CODEC.encode(buf, component);
         writeBufferBytes(out, buf);
      } finally {
         buf.release();
      }
   }

   private static Component parseComponent(String value) {
      String text = value != null && !value.isBlank() ? value.strip() : "{\"text\":\"\"}";

      try {
         JsonElement element = JsonParser.parseString(text);
         return element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()
            ? Component.literal(element.getAsString())
            : ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, element).getOrThrow();
      } catch (Throwable ignored) {
         return Component.literal(unescapeString(value));
      }
   }

   private static void writeItemStackField(ByteArrayOutputStream out, String value) {
      ItemStack stack = parseItemStack(value);
      RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), currentRegistryAccess());

      try {
         ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
         writeBufferBytes(out, buf);
      } finally {
         buf.release();
      }
   }

   private static ItemStack parseItemStack(String value) {
      String text = value == null ? "" : value.strip();
      if (text.isEmpty() || "{}".equals(text)) {
         return ItemStack.EMPTY;
      }

      if (looksLikeItemId(text)) {
         return itemStackFromId(text, 1);
      }

      try {
         JsonElement json = JsonParser.parseString(text);
         return json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()
            ? itemStackFromId(json.getAsString(), 1)
            : ItemStack.CODEC.parse(currentRegistryAccess().createSerializationContext(JsonOps.INSTANCE), json).getOrThrow();
      } catch (Throwable jsonFailure) {
         try {
            Tag tag = TagParser.parseCompoundFully(text);
            return ItemStack.CODEC.parse(currentRegistryAccess().createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow();
         } catch (Throwable snbtFailure) {
            IllegalArgumentException ex = new IllegalArgumentException("Invalid item stack JSON/SNBT/id");
            ex.addSuppressed(jsonFailure);
            ex.addSuppressed(snbtFailure);
            throw ex;
         }
      }
   }

   private static boolean looksLikeItemId(String text) {
      return text != null && text.matches("[a-z0-9_./-]+(:[a-z0-9_./-]+)?");
   }

   private static ItemStack itemStackFromId(String rawId, int count) {
      String idText = rawId == null ? "" : rawId.strip();
      Identifier id = Identifier.parse(idText.contains(":") ? idText : "minecraft:" + idText);
      if (!BuiltInRegistries.ITEM.containsKey(id)) {
         throw new IllegalArgumentException("Unknown item id: " + id);
      } else {
         Item item = BuiltInRegistries.ITEM.getValue(id);
         if (item == null) {
            throw new IllegalArgumentException("Unknown item id: " + id);
         } else {
            return new ItemStack(item, Math.max(1, count));
         }
      }
   }

   private static RegistryAccess currentRegistryAccess() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null) {
         return mc.level.registryAccess();
      } else {
         return mc.getConnection() != null ? mc.getConnection().registryAccess() : RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
      }
   }

   private static void writeBufferBytes(ByteArrayOutputStream out, FriendlyByteBuf buf) {
      byte[] bytes = AutismPayloadSupport.copyBufferBytes(buf);
      out.write(bytes, 0, bytes.length);
   }

   private static byte[] decodeHexOrBase64(String value) {
      String text = value == null ? "" : value.strip();
      if (text.isEmpty()) {
         return new byte[0];
      }

      try {
         return AutismPayloadSupport.parseHexOrBase64(text);
      } catch (Exception ignored) {
         return Base64.getDecoder().decode(text);
      }
   }

   private static String unescapeString(String value) {
      if (value != null && !value.isEmpty()) {
         StringBuilder sb = new StringBuilder(value.length());
         boolean escaping = false;

         for (int i = 0; i < value.length(); i++) {
            char chr = value.charAt(i);
            if (!escaping) {
               if (chr == '\\') {
                  escaping = true;
               } else {
                  sb.append(chr);
               }
            } else {
               switch (chr) {
                  case '\\':
                     sb.append('\\');
                     break;
                  case 'n':
                     sb.append('\n');
                     break;
                  case 'r':
                     sb.append('\r');
                     break;
                  case 't':
                     sb.append('\t');
                     break;
                  default:
                     sb.append('\\');
                     sb.append(chr);
               }

               escaping = false;
            }
         }

         if (escaping) {
            sb.append('\\');
         }

         return sb.toString();
      } else {
         return "";
      }
   }

   private static long parseLongInRange(String value, long min, long max) {
      String text = value == null ? "" : value.strip();
      long parsed = Long.decode(text.isEmpty() ? "0" : text);
      if (parsed >= min && parsed <= max) {
         return parsed;
      } else {
         throw new IllegalArgumentException("Value out of range: " + parsed);
      }
   }

   private static int parseCharCode(String value) {
      String text = value == null ? "" : value.strip();
      if (text.isEmpty()) {
         return 0;
      } else {
         return text.length() == 1 ? text.charAt(0) : (int)parseLongInRange(text, 0L, 65535L);
      }
   }

   private static void writeByteArrayField(ByteArrayOutputStream out, String value) {
      String text = value == null ? "" : value.strip();
      String prefix = "raw";
      String body = text;
      int split = text.indexOf(58);
      if (split > 0) {
         prefix = text.substring(0, split).strip().toLowerCase(Locale.ROOT);
         body = text.substring(split + 1).strip();
      }

      byte[] bytes = decodeHexOrBase64(body);
      switch (prefix) {
         case "varint":
         case "var":
            writeVarInt(out, bytes.length);
            break;
         case "short":
         case "ushort":
            writeBigEndianShort(out, (short)bytes.length);
            break;
         case "int":
            writeBigEndianInt(out, bytes.length);
         case "raw":
         case "":
            break;
         default:
            throw new IllegalArgumentException("Byte Array prefix must be raw, varint, short, or int");
      }

      out.write(bytes, 0, bytes.length);
   }

   private static void writeOptionalField(ByteArrayOutputStream out, String value, PayloadFieldCodec.Inner_StreamUtil mode, List<String> warnings) throws Exception {
      String text = value == null ? "" : value.strip();
      if (!text.isEmpty() && !"false".equalsIgnoreCase(text) && !"empty".equalsIgnoreCase(text)) {
         out.write(1);
         String body = text;
         int split = text.indexOf(58);
         PayloadFieldCodec.Inner_BufferHelper nestedType = PayloadFieldCodec.Inner_BufferHelper.MINECRAFT_STRING;
         if (split > 0) {
            nestedType = PayloadFieldCodec.Inner_BufferHelper.parse(text.substring(0, split));
            body = text.substring(split + 1).strip();
         }

         encodeField(out, new PayloadFieldCodec.Inner_CachePool(nestedType, body, true), mode, warnings);
      } else {
         out.write(0);
      }
   }

   private static void writeBigEndianShort(ByteArrayOutputStream out, short value) {
      out.write(value >>> 8 & 0xFF);
      out.write(value & 255);
   }

   private static void writeBigEndianInt(ByteArrayOutputStream out, int value) {
      out.write(value >>> 24 & 0xFF);
      out.write(value >>> 16 & 0xFF);
      out.write(value >>> 8 & 0xFF);
      out.write(value & 0xFF);
   }

   private static void writeBigEndianLong(ByteArrayOutputStream out, long value) {
      for (int shift = 56; shift >= 0; shift -= 8) {
         out.write((int)(value >>> shift & 255L));
      }
   }

   private static void writeVarInt(ByteArrayOutputStream out, int value) {
      while ((value & -128) != 0) {
         out.write(value & 127 | 128);
         value >>>= 7;
      }

      out.write(value);
   }

   private static void writeVarLong(ByteArrayOutputStream out, long value) {
      while ((value & -128L) != 0L) {
         out.write((int)(value & 127L) | 128);
         value >>>= 7;
      }

      out.write((int)value);
   }

   private static void writeUuid(ByteArrayOutputStream out, UUID uuid) {
      writeBigEndianLong(out, uuid.getMostSignificantBits());
      writeBigEndianLong(out, uuid.getLeastSignificantBits());
   }

   private static long packBlockPos(String value) {
      String[] parts = (value == null ? "" : value).split(",");
      if (parts.length != 3) {
         throw new IllegalArgumentException("Block Pos must be x,y,z");
      }

      long x = Long.parseLong(parts[0].strip()) & 67108863L;
      long y = Long.parseLong(parts[1].strip()) & 4095L;
      long z = Long.parseLong(parts[2].strip()) & 67108863L;
      return x << 38 | z << 12 | y;
   }

   private static String previewUtf8(byte[] bytes) {
      if (bytes != null && bytes.length != 0) {
         String text = AutismPayloadSupport.decodePrintableUtf8(bytes);
         return text.isBlank() ? "<binary>" : truncatePreview(text.replace('\n', ' '), 96);
      } else {
         return "";
      }
   }

   private static String previewMcString(byte[] bytes) {
      String decoded = AutismPayloadSupport.decodeMcString(bytes);
      return decoded == null ? "<not a full Minecraft String>" : truncatePreview(decoded, 96);
   }

   private static String previewJavaUtf(byte[] bytes) {
      if (bytes != null && bytes.length >= 2) {
         int len = ByteBuffer.wrap(bytes, 0, 2).order(ByteOrder.BIG_ENDIAN).getShort() & '\uffff';
         if (len + 2 != bytes.length) {
            return "<not writeUTF>";
         }

         try {
            return truncatePreview(new String(bytes, 2, len, StandardCharsets.UTF_8), 96);
         } catch (Throwable ignored) {
            return "<invalid writeUTF>";
         }
      } else {
         return "<not writeUTF>";
      }
   }

   private static String truncatePreview(String value, int max) {
      if (value == null) {
         return "";
      }

      String text = value.strip();
      return text.length() <= max ? text : text.substring(0, Math.max(0, max - 3)) + "...";
   }

   public enum Inner_BufferHelper {
      BYTE("writeByte", "0"),
      UNSIGNED_BYTE("writeUnsignedByte", "0"),
      BOOLEAN("writeBoolean", "true"),
      SHORT("writeShort", "0"),
      UNSIGNED_SHORT("writeUnsignedShort", "0"),
      CHAR("writeChar", "A"),
      INT("writeInt", "0"),
      LONG("writeLong", "0"),
      FLOAT("writeFloat", "0.0"),
      DOUBLE("writeDouble", "0.0"),
      JAVA_WRITE_UTF("writeUTF", "GetServer"),
      RAW_BYTES("writeBytes", ""),
      HEX_BYTES("writeBytesHex", ""),
      BYTE_ARRAY("writeByteArray", "raw:"),
      STRING_BYTES("writeStringBytes", "hello-from-client"),
      RAW_UTF8_STRING("writeStringBytes", "hello-from-client"),
      VAR_INT("writeVarInt", "0"),
      VAR_LONG("writeVarLong", "0"),
      MINECRAFT_STRING("writeMcString", AutismPayloadSupport.defaultClientBrand()),
      IDENTIFIER("writeIdentifier", "minecraft:brand"),
      UUID_FIELD("writeUUID", "00000000-0000-0000-0000-000000000000"),
      BLOCK_POS("writeBlockPos", "0,64,0"),
      ENUM_VAR_INT("writeEnumVarInt", "0"),
      OPTIONAL_VALUE("writeOptional", "false"),
      NBT("writeNbt", "{}"),
      ITEM_STACK("writeItemStack", "{\"id\":\"minecraft:stone\",\"count\":1}"),
      TEXT_COMPONENT("writeComponent", "{\"text\":\"hello\"}"),
      JSON_STRING("writeComponent", "{\"text\":\"hello\"}");

      private final String label;
      private final String defaultValue;

      Inner_BufferHelper(String label, String defaultValue) {
         this.label = label;
         this.defaultValue = defaultValue;
      }

      public String label() {
         return this.label;
      }

      public String defaultValue() {
         return this.defaultValue;
      }

      public static PayloadFieldCodec.Inner_BufferHelper parse(String value) {
         if (value != null && !value.isBlank()) {
            String normalized = value.strip().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
            if ("UUID".equals(normalized)) {
               normalized = "UUID_FIELD";
            }

            if ("UTF".equals(normalized) || "WRITEUTF".equals(normalized)) {
               normalized = "JAVA_WRITE_UTF";
            }

            if ("MINECRAFT_STRING".equals(normalized) || "MC_STRING".equals(normalized)) {
               normalized = "MINECRAFT_STRING";
            }

            if ("TEXT_COMPONENT".equals(normalized) || "COMPONENT".equals(normalized)) {
               normalized = "TEXT_COMPONENT";
            }

            for (PayloadFieldCodec.Inner_BufferHelper type : values()) {
               if (type.name().equals(normalized)) {
                  return type;
               }

               if (type.label().equalsIgnoreCase(value.strip())) {
                  return type;
               }
            }

            return RAW_UTF8_STRING;
         } else {
            return RAW_UTF8_STRING;
         }
      }
   }

   public record Inner_CachePool(PayloadFieldCodec.Inner_BufferHelper type, String init_Str, boolean enabled) {
      public Inner_CachePool {
         type = type == null ? PayloadFieldCodec.Inner_BufferHelper.RAW_UTF8_STRING : type;
         init_Str = init_Str == null ? "" : init_Str;
      }

      public String init_Str() {
         return this.type.label() + " = " + this.init_Str;
      }

      public String value() {
         return this.init_Str;
      }
   }

   public enum Inner_ConfigStore {
      C2S,
      S2C;

      public static PayloadFieldCodec.Inner_ConfigStore parse(String value) {
         if (value == null) {
            return C2S;
         } else {
            return "S2C".equalsIgnoreCase(value.strip()) ? S2C : C2S;
         }
      }
   }

   public record Inner_DataUtil(byte[] bytes, List<String> warnings, List<String> errors, PayloadFieldCodec.Inner_NetHelper preview) {
      public Inner_DataUtil {
         bytes = bytes == null ? new byte[0] : (byte[])bytes.clone();
         warnings = warnings == null ? List.of() : List.copyOf(warnings);
         errors = errors == null ? List.of() : List.copyOf(errors);
         preview = preview == null ? PayloadFieldCodec.buildPreview(bytes) : preview;
      }

      public byte[] bytes() {
         return (byte[])this.bytes.clone();
      }

      public boolean isValid() {
         return this.errors.isEmpty();
      }
   }

   public record Inner_JsonUtil(
      String channel,
      PayloadFieldCodec.Inner_ConfigStore direction,
      PayloadFieldCodec.Inner_TaskQueue phase,
      PayloadFieldCodec.Inner_StreamUtil encodingMode,
      List<PayloadFieldCodec.Inner_CachePool> fields
   ) {
      public Inner_JsonUtil {
         channel = channel != null && !channel.isBlank() ? channel.strip() : "minecraft:brand";
         direction = direction == null ? PayloadFieldCodec.Inner_ConfigStore.C2S : direction;
         phase = phase == null ? PayloadFieldCodec.Inner_TaskQueue.PLAY : phase;
         encodingMode = encodingMode == null ? PayloadFieldCodec.Inner_StreamUtil.parse("", channel) : encodingMode;
         fields = fields == null ? List.of() : List.copyOf(fields);
      }

      public PayloadFieldCodec.Inner_DataUtil encode() {
         List<String> warnings = new ArrayList<>();
         List<String> errors = new ArrayList<>();
         PayloadFieldCodec.validateHeader(this.channel, this.direction, this.phase, this.encodingMode, warnings, errors);
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         List<PayloadFieldCodec.Inner_CachePool> enabledFields = this.fields.stream().filter(PayloadFieldCodec.Inner_CachePool::enabled).toList();
         if (enabledFields.isEmpty()) {
            warnings.add("Payload is empty. Some servers reject empty custom payloads.");
         }

         for (PayloadFieldCodec.Inner_CachePool field : enabledFields) {
            try {
               PayloadFieldCodec.encodeField(out, field, this.encodingMode, warnings);
            } catch (Exception e) {
               errors.add(field.type().label() + ": " + AutismPayloadSupport.describeError(e));
            }
         }

         byte[] bytes = out.toByteArray();
         if (bytes.length > 8192) {
            warnings.add("Payload is large (" + bytes.length + "B). Use low repeat rates.");
         }

         if (bytes.length > 32767) {
            errors.add("Payload is " + bytes.length + "B; serverbound custom payload max is 32767B.");
         }

         PayloadFieldCodec.Inner_NetHelper preview = PayloadFieldCodec.buildPreview(bytes);
         return new PayloadFieldCodec.Inner_DataUtil(bytes, warnings, errors, preview);
      }
   }

   public record Inner_NetHelper(String hexPreview, String utf8Preview, String mcStringPreview, String javaUtfPreview) {
   }

   public enum Inner_StreamUtil {
      RAW_UTF8("Raw UTF-8"),
      MANUAL_HEX("Manual Hex"),
      JAVA_DATA_OUTPUT("Java DataOutput"),
      MINECRAFT_BYTEBUF("Minecraft ByteBuf"),
      JSON_TEXT("JSON/Text"),
      ADVANCED_MIXED("Advanced Mixed");

      private final String label;

      Inner_StreamUtil(String label) {
         this.label = label;
      }

      public String label() {
         return this.label;
      }

      public static PayloadFieldCodec.Inner_StreamUtil parse(String value, String channel) {
         if (value != null && !value.isBlank()) {
            String normalized = value.strip().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');

            for (PayloadFieldCodec.Inner_StreamUtil mode : values()) {
               if (mode.name().equals(normalized)) {
                  return mode;
               }
            }
         }

         if (AutismPayloadSupport.isBrandChannel(channel)) {
            return MINECRAFT_BYTEBUF;
         } else {
            return "bungeecord:main".equalsIgnoreCase(channel == null ? "" : channel.strip()) ? JAVA_DATA_OUTPUT : ADVANCED_MIXED;
         }
      }
   }

   public enum Inner_TaskQueue {
      PLAY,
      CONFIGURATION;

      public static PayloadFieldCodec.Inner_TaskQueue parse(String value) {
         if (value == null) {
            return PLAY;
         }

         String normalized = value.strip().toUpperCase(Locale.ROOT);
         return normalized.contains("CONFIG") ? CONFIGURATION : PLAY;
      }
   }
}
