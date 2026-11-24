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

public final class bJ {
   private static final int init = 8192;
   private static final int load = 32767;

   private bJ() {
   }

   public static bJ.Inner_JsonUtil init(autismclient.util.macro.H action) {
      if (action == null) {
         return init_bJ_JsonUtil();
      }

      String channel = action.channel != null && !action.channel.isBlank() ? action.channel.strip() : "minecraft:brand";
      bJ.Inner_ConfigStore direction = bJ.Inner_ConfigStore.init(
         action.payloadDirection != null && !action.payloadDirection.isBlank() ? action.payloadDirection : action.sourceDirection
      );
      bJ.Inner_TaskQueue phase = bJ.Inner_TaskQueue.init(
         action.payloadPhase != null && !action.payloadPhase.isBlank() ? action.payloadPhase : action.sourceProtocol
      );
      bJ.Inner_StreamUtil mode = bJ.Inner_StreamUtil.init(action.payloadEncodingMode, channel);
      List<bJ.Inner_CachePool> fields = init_List(action.payloadFields);
      if (fields.isEmpty() && action.payloadData != null && !action.payloadData.isBlank()) {
         fields = List.of(new bJ.Inner_CachePool(bJ.Inner_BufferHelper.tick, action.payloadData, true));
         mode = bJ.Inner_StreamUtil.load;
      }

      if (fields.isEmpty()) {
         if (AutismPayloadSupport.init_Z(channel)) {
            fields = List.of(new bJ.Inner_CachePool(bJ.Inner_BufferHelper.clear, AutismPayloadSupport.init_Str(), true));
            mode = bJ.Inner_StreamUtil.write;
         } else {
            fields = List.of(new bJ.Inner_CachePool(bJ.Inner_BufferHelper.set, "", true));
         }
      }

      return new bJ.Inner_JsonUtil(channel, direction, phase, mode, fields);
   }

   public static bJ.Inner_JsonUtil init_bJ_JsonUtil() {
      return new bJ.Inner_JsonUtil(
         "minecraft:brand",
         bJ.Inner_ConfigStore.init,
         bJ.Inner_TaskQueue.init,
         bJ.Inner_StreamUtil.write,
         List.of(new bJ.Inner_CachePool(bJ.Inner_BufferHelper.clear, AutismPayloadSupport.init_Str(), true))
      );
   }

   public static bJ.Inner_JsonUtil load() {
      return new bJ.Inner_JsonUtil(
         "testmod:ping",
         bJ.Inner_ConfigStore.init,
         bJ.Inner_TaskQueue.init,
         bJ.Inner_StreamUtil.init_bJ_StreamUtil,
         List.of(new bJ.Inner_CachePool(bJ.Inner_BufferHelper.set, "hello-from-client", true))
      );
   }

   public static bJ.Inner_JsonUtil init_bJ_JsonUtil(String brand) {
      return new bJ.Inner_JsonUtil(
         "minecraft:brand",
         bJ.Inner_ConfigStore.init,
         bJ.Inner_TaskQueue.init,
         bJ.Inner_StreamUtil.write,
         List.of(new bJ.Inner_CachePool(bJ.Inner_BufferHelper.clear, brand != null && !brand.isBlank() ? brand : AutismPayloadSupport.init_Str(), true))
      );
   }

   public static bJ.Inner_JsonUtil read() {
      return new bJ.Inner_JsonUtil(
         "bungeecord:main",
         bJ.Inner_ConfigStore.init,
         bJ.Inner_TaskQueue.init,
         bJ.Inner_StreamUtil.read,
         List.of(new bJ.Inner_CachePool(bJ.Inner_BufferHelper.push, "GetServer", true))
      );
   }

   public static bJ.Inner_JsonUtil write() {
      return new bJ.Inner_JsonUtil(
         "bungeecord:main",
         bJ.Inner_ConfigStore.init,
         bJ.Inner_TaskQueue.init,
         bJ.Inner_StreamUtil.read,
         List.of(new bJ.Inner_CachePool(bJ.Inner_BufferHelper.push, "PlayerCount", true), new bJ.Inner_CachePool(bJ.Inner_BufferHelper.push, "ALL", true))
      );
   }

   public static String init(List<bJ.Inner_CachePool> fields) {
      if (fields != null && !fields.isEmpty()) {
         StringBuilder sb = new StringBuilder();

         for (bJ.Inner_CachePool field : fields) {
            if (field != null) {
               if (!sb.isEmpty()) {
                  sb.append('\n');
               }

               if (!field.init_Z()) {
                  sb.append("# ");
               }

               sb.append(field.init_bJ_BufferHelper().init()).append(" = ").append(field.load());
            }
         }

         return sb.toString();
      } else {
         return "";
      }
   }

   public static List<bJ.Inner_CachePool> init_List(String text) {
      if (text != null && !text.isBlank()) {
         List<bJ.Inner_CachePool> fields = new ArrayList<>();
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
                  fields.add(new bJ.Inner_CachePool(bJ.Inner_BufferHelper.init(typeText), value, enabled));
               }
            }
         }

         return fields;
      } else {
         return Collections.emptyList();
      }
   }

   public static String init_Str_Str_Str(String currentMode, String channel) {
      bJ.Inner_StreamUtil current = bJ.Inner_StreamUtil.init(currentMode, channel);
      bJ.Inner_StreamUtil[] values = bJ.Inner_StreamUtil.values();
      return values[(current.ordinal() + 1) % values.length].name();
   }

   public static String init_Str(String current) {
      return bJ.Inner_ConfigStore.init(current) == bJ.Inner_ConfigStore.init ? bJ.Inner_ConfigStore.load.name() : bJ.Inner_ConfigStore.init.name();
   }

   public static String load(String current) {
      return bJ.Inner_TaskQueue.init(current) == bJ.Inner_TaskQueue.init ? bJ.Inner_TaskQueue.load.name() : bJ.Inner_TaskQueue.init.name();
   }

   public static bJ.Inner_NetHelper init_bJ_NetHelper(byte[] bytes) {
      byte[] raw = bytes == null ? new byte[0] : bytes;
      return new bJ.Inner_NetHelper(AutismPayloadSupport.init_Str(raw, 96), init_Str(raw), load(raw), read(raw));
   }

   public static void init_V_H_bJ_JsonUtil(autismclient.util.macro.H action, bJ.Inner_JsonUtil template) {
      if (action != null && template != null) {
         action.channel = template.init_Str();
         action.payloadDirection = template.init_bJ_ConfigStore().name();
         action.payloadPhase = template.init_bJ_TaskQueue().name();
         action.payloadEncodingMode = template.init_bJ_StreamUtil().name();
         action.payloadFields = init(template.init_List());
         action.sourceDirection = action.payloadDirection;
         action.sourceProtocol = "";
         bJ.Inner_DataUtil result = template.init_bJ_DataUtil();
         if (result.init_Z()) {
            action.payloadData = AutismPayloadSupport.init_Str(result.init_BArr());
         }
      }
   }

   private static void init(
      String channel, bJ.Inner_ConfigStore direction, bJ.Inner_TaskQueue phase, bJ.Inner_StreamUtil mode, List<String> warnings, List<String> errors
   ) {
      if (direction == bJ.Inner_ConfigStore.load) {
         warnings.add("S2C is inspect/replay metadata only. Sending from macros is serverbound C2S.");
      }

      if (phase == bJ.Inner_TaskQueue.load) {
         warnings.add("Configuration payloads usually cannot be replayed during Play.");
      }

      try {
         AutismPayloadSupport.init_Identifier(channel);
      } catch (Exception e) {
         errors.add(AutismPayloadSupport.init(e));
      }

      if (channel != null && !channel.contains(":")) {
         warnings.add("Use a namespaced channel like namespace:path.");
      }

      if (AutismPayloadSupport.init_Z(channel) && mode != bJ.Inner_StreamUtil.write) {
         warnings.add("minecraft:brand normally expects a Minecraft String.");
      }

      if ("bungeecord:main".equalsIgnoreCase(channel == null ? "" : channel.strip()) && mode != bJ.Inner_StreamUtil.read) {
         warnings.add("bungeecord:main normally uses Java DataOutput/writeUTF fields.");
      }
   }

   private static void init(ByteArrayOutputStream out, bJ.Inner_CachePool field, bJ.Inner_StreamUtil mode, List<String> warnings) throws Exception {
      String value = field.load();
      switch (field.init_bJ_BufferHelper()) {
         case init_bJ_BufferHelper:
            out.write((byte)init(value, -128L, 127L));
            break;
         case load_bJ_BufferHelper:
            out.write((byte)init(value, 0L, 255L));
            break;
         case read:
            out.write(Boolean.parseBoolean(value.strip()) ? 1 : 0);
            break;
         case write:
            init_V_ByteArrayOutputStr_S(out, (short)init(value, -32768L, 32767L));
            break;
         case update:
            init_V_ByteArrayOutputStr_S(out, (short)init(value, 0L, 65535L));
            break;
         case sync:
            init_V_ByteArrayOutputStr_S(out, (short)init_I(value));
            break;
         case flush:
            init_V_ByteArrayOutputStr_I(out, (int)init(value, -2147483648L, 2147483647L));
            break;
         case close:
            init_V_ByteArrayOutputStr_J(out, init(value, -9223372036854775808L, 9223372036854775807L));
            break;
         case open:
            init_V_ByteArrayOutputStr_I(out, Float.floatToIntBits(Float.parseFloat(value.strip())));
            break;
         case poll:
            init_V_ByteArrayOutputStr_J(out, Double.doubleToLongBits(Double.parseDouble(value.strip())));
            break;
         case push:
            DataOutputStream dos = new DataOutputStream(out);
            dos.writeUTF(read(value));
            dos.flush();
            break;
         case pop:
         case tick:
            out.write(init_BArr(value));
            break;
         case run:
            write(out, value);
            break;
         case get:
         case set:
            out.write(read(value).getBytes(StandardCharsets.UTF_8));
            break;
         case put:
         case next:
            load_V_ByteArrayOutputStr_I(out, (int)init(value, -2147483648L, 2147483647L));
            break;
         case add:
            load_V_ByteArrayOutputStr_J(out, init(value, -9223372036854775808L, 9223372036854775807L));
            break;
         case clear:
         case reset:
            out.write(AutismPayloadSupport.load_BArr(read(value)));
            break;
         case start:
            init_V_ByteArrayOutputStr_UUID(out, UUID.fromString(value.strip()));
            break;
         case stop:
            init_V_ByteArrayOutputStr_J(out, init_J(value));
            break;
         case prev:
            init(out, value, mode, warnings);
            break;
         case valid:
            init_V_ByteArrayOutputStr_Str(out, value);
            break;
         case size:
            read(out, value);
            break;
         case data:
         case value:
            load_V_ByteArrayOutputStr_Str(out, value);
      }
   }

   private static void init_V_ByteArrayOutputStr_Str(ByteArrayOutputStream out, String value) throws Exception {
      String text = value != null && !value.isBlank() ? value.strip() : "{}";
      CompoundTag tag = TagParser.parseCompoundFully(text);
      FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

      try {
         buf.writeNbt(tag);
         init_V_ByteArrayOutputStr_FriendlyByteBuf(out, buf);
      } finally {
         buf.release();
      }
   }

   private static void load_V_ByteArrayOutputStr_Str(ByteArrayOutputStream out, String value) {
      Component component = init_Component(value);
      RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), init_RegistryAccess());

      try {
         ComponentSerialization.STREAM_CODEC.encode(buf, component);
         init_V_ByteArrayOutputStr_FriendlyByteBuf(out, buf);
      } finally {
         buf.release();
      }
   }

   private static Component init_Component(String value) {
      String text = value != null && !value.isBlank() ? value.strip() : "{\"text\":\"\"}";

      try {
         JsonElement element = JsonParser.parseString(text);
         return element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()
            ? Component.literal(element.getAsString())
            : ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, element).getOrThrow();
      } catch (Throwable ignored) {
         return Component.literal(read(value));
      }
   }

   private static void read(ByteArrayOutputStream out, String value) {
      ItemStack stack = init_ItemStack(value);
      RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), init_RegistryAccess());

      try {
         ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
         init_V_ByteArrayOutputStr_FriendlyByteBuf(out, buf);
      } finally {
         buf.release();
      }
   }

   private static ItemStack init_ItemStack(String value) {
      String text = value == null ? "" : value.strip();
      if (text.isEmpty() || "{}".equals(text)) {
         return ItemStack.EMPTY;
      }

      if (init_Z(text)) {
         return init_ItemStack_Str_I(text, 1);
      }

      try {
         JsonElement json = JsonParser.parseString(text);
         return json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()
            ? init_ItemStack_Str_I(json.getAsString(), 1)
            : ItemStack.CODEC.parse(init_RegistryAccess().createSerializationContext(JsonOps.INSTANCE), json).getOrThrow();
      } catch (Throwable jsonFailure) {
         try {
            Tag tag = TagParser.parseCompoundFully(text);
            return ItemStack.CODEC.parse(init_RegistryAccess().createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow();
         } catch (Throwable snbtFailure) {
            IllegalArgumentException ex = new IllegalArgumentException("Invalid item stack JSON/SNBT/id");
            ex.addSuppressed(jsonFailure);
            ex.addSuppressed(snbtFailure);
            throw ex;
         }
      }
   }

   private static boolean init_Z(String text) {
      return text != null && text.matches("[a-z0-9_./-]+(:[a-z0-9_./-]+)?");
   }

   private static ItemStack init_ItemStack_Str_I(String rawId, int count) {
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

   private static RegistryAccess init_RegistryAccess() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null) {
         return mc.level.registryAccess();
      } else {
         return mc.getConnection() != null ? mc.getConnection().registryAccess() : RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
      }
   }

   private static void init_V_ByteArrayOutputStr_FriendlyByteBuf(ByteArrayOutputStream out, FriendlyByteBuf buf) {
      byte[] bytes = AutismPayloadSupport.init(buf);
      out.write(bytes, 0, bytes.length);
   }

   private static byte[] init_BArr(String value) {
      String text = value == null ? "" : value.strip();
      if (text.isEmpty()) {
         return new byte[0];
      }

      try {
         return AutismPayloadSupport.init_BArr(text);
      } catch (Exception ignored) {
         return Base64.getDecoder().decode(text);
      }
   }

   private static String read(String value) {
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

   private static long init(String value, long min, long max) {
      String text = value == null ? "" : value.strip();
      long parsed = Long.decode(text.isEmpty() ? "0" : text);
      if (parsed >= min && parsed <= max) {
         return parsed;
      } else {
         throw new IllegalArgumentException("Value out of range: " + parsed);
      }
   }

   private static int init_I(String value) {
      String text = value == null ? "" : value.strip();
      if (text.isEmpty()) {
         return 0;
      } else {
         return text.length() == 1 ? text.charAt(0) : (int)init(text, 0L, 65535L);
      }
   }

   private static void write(ByteArrayOutputStream out, String value) {
      String text = value == null ? "" : value.strip();
      String prefix = "raw";
      String body = text;
      int split = text.indexOf(58);
      if (split > 0) {
         prefix = text.substring(0, split).strip().toLowerCase(Locale.ROOT);
         body = text.substring(split + 1).strip();
      }

      byte[] bytes = init_BArr(body);
      switch (prefix) {
         case "varint":
         case "var":
            load_V_ByteArrayOutputStr_I(out, bytes.length);
            break;
         case "short":
         case "ushort":
            init_V_ByteArrayOutputStr_S(out, (short)bytes.length);
            break;
         case "int":
            init_V_ByteArrayOutputStr_I(out, bytes.length);
         case "raw":
         case "":
            break;
         default:
            throw new IllegalArgumentException("Byte Array prefix must be raw, varint, short, or int");
      }

      out.write(bytes, 0, bytes.length);
   }

   private static void init(ByteArrayOutputStream out, String value, bJ.Inner_StreamUtil mode, List<String> warnings) throws Exception {
      String text = value == null ? "" : value.strip();
      if (!text.isEmpty() && !"false".equalsIgnoreCase(text) && !"empty".equalsIgnoreCase(text)) {
         out.write(1);
         String body = text;
         int split = text.indexOf(58);
         bJ.Inner_BufferHelper nestedType = bJ.Inner_BufferHelper.clear;
         if (split > 0) {
            nestedType = bJ.Inner_BufferHelper.init(text.substring(0, split));
            body = text.substring(split + 1).strip();
         }

         init(out, new bJ.Inner_CachePool(nestedType, body, true), mode, warnings);
      } else {
         out.write(0);
      }
   }

   private static void init_V_ByteArrayOutputStr_S(ByteArrayOutputStream out, short value) {
      out.write(value >>> 8 & 0xFF);
      out.write(value & 255);
   }

   private static void init_V_ByteArrayOutputStr_I(ByteArrayOutputStream out, int value) {
      out.write(value >>> 24 & 0xFF);
      out.write(value >>> 16 & 0xFF);
      out.write(value >>> 8 & 0xFF);
      out.write(value & 0xFF);
   }

   private static void init_V_ByteArrayOutputStr_J(ByteArrayOutputStream out, long value) {
      for (int shift = 56; shift >= 0; shift -= 8) {
         out.write((int)(value >>> shift & 255L));
      }
   }

   private static void load_V_ByteArrayOutputStr_I(ByteArrayOutputStream out, int value) {
      while ((value & -128) != 0) {
         out.write(value & 127 | 128);
         value >>>= 7;
      }

      out.write(value);
   }

   private static void load_V_ByteArrayOutputStr_J(ByteArrayOutputStream out, long value) {
      while ((value & -128L) != 0L) {
         out.write((int)(value & 127L) | 128);
         value >>>= 7;
      }

      out.write((int)value);
   }

   private static void init_V_ByteArrayOutputStr_UUID(ByteArrayOutputStream out, UUID uuid) {
      init_V_ByteArrayOutputStr_J(out, uuid.getMostSignificantBits());
      init_V_ByteArrayOutputStr_J(out, uuid.getLeastSignificantBits());
   }

   private static long init_J(String value) {
      String[] parts = (value == null ? "" : value).split(",");
      if (parts.length != 3) {
         throw new IllegalArgumentException("Block Pos must be x,y,z");
      }

      long x = Long.parseLong(parts[0].strip()) & 67108863L;
      long y = Long.parseLong(parts[1].strip()) & 4095L;
      long z = Long.parseLong(parts[2].strip()) & 67108863L;
      return x << 38 | z << 12 | y;
   }

   private static String init_Str(byte[] bytes) {
      if (bytes != null && bytes.length != 0) {
         String text = AutismPayloadSupport.write(bytes);
         return text.isBlank() ? "<binary>" : init_Str_Str_I(text.replace('\n', ' '), 96);
      } else {
         return "";
      }
   }

   private static String load(byte[] bytes) {
      String decoded = AutismPayloadSupport.read(bytes);
      return decoded == null ? "<not a full Minecraft String>" : init_Str_Str_I(decoded, 96);
   }

   private static String read(byte[] bytes) {
      if (bytes != null && bytes.length >= 2) {
         int len = ByteBuffer.wrap(bytes, 0, 2).order(ByteOrder.BIG_ENDIAN).getShort() & '\uffff';
         if (len + 2 != bytes.length) {
            return "<not writeUTF>";
         }

         try {
            return init_Str_Str_I(new String(bytes, 2, len, StandardCharsets.UTF_8), 96);
         } catch (Throwable ignored) {
            return "<invalid writeUTF>";
         }
      } else {
         return "<not writeUTF>";
      }
   }

   private static String init_Str_Str_I(String value, int max) {
      if (value == null) {
         return "";
      }

      String text = value.strip();
      return text.length() <= max ? text : text.substring(0, Math.max(0, max - 3)) + "...";
   }

   public enum Inner_BufferHelper {
      init_bJ_BufferHelper("writeByte", "0"),
      load_bJ_BufferHelper("writeUnsignedByte", "0"),
      read("writeBoolean", "true"),
      write("writeShort", "0"),
      update("writeUnsignedShort", "0"),
      sync("writeChar", "A"),
      flush("writeInt", "0"),
      close("writeLong", "0"),
      open("writeFloat", "0.0"),
      poll("writeDouble", "0.0"),
      push("writeUTF", "GetServer"),
      pop("writeBytes", ""),
      tick("writeBytesHex", ""),
      run("writeByteArray", "raw:"),
      get("writeStringBytes", "hello-from-client"),
      set("writeStringBytes", "hello-from-client"),
      put("writeVarInt", "0"),
      add("writeVarLong", "0"),
      clear("writeMcString", AutismPayloadSupport.init_Str()),
      reset("writeIdentifier", "minecraft:brand"),
      start("writeUUID", "00000000-0000-0000-0000-000000000000"),
      stop("writeBlockPos", "0,64,0"),
      next("writeEnumVarInt", "0"),
      prev("writeOptional", "false"),
      valid("writeNbt", "{}"),
      size("writeItemStack", "{\"id\":\"minecraft:stone\",\"count\":1}"),
      data("writeComponent", "{\"text\":\"hello\"}"),
      value("writeComponent", "{\"text\":\"hello\"}");

      private final String init_Str;
      private final String load_Str;

      Inner_BufferHelper(String label, String defaultValue) {
         this.init_Str = label;
         this.load_Str = defaultValue;
      }

      public String init() {
         return this.init_Str;
      }

      public String load() {
         return this.load_Str;
      }

      public static bJ.Inner_BufferHelper init(String value) {
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

            for (bJ.Inner_BufferHelper type : values()) {
               if (type.name().equals(normalized)) {
                  return type;
               }

               if (type.init().equalsIgnoreCase(value.strip())) {
                  return type;
               }
            }

            return set;
         } else {
            return set;
         }
      }
   }

   public record Inner_CachePool(bJ.Inner_BufferHelper init_bJ_BufferHelper, String init_Str, boolean init_Z) {
      public Inner_CachePool {
         init_bJ_BufferHelper = init_bJ_BufferHelper == null ? bJ.Inner_BufferHelper.set : init_bJ_BufferHelper;
         init_Str = init_Str == null ? "" : init_Str;
      }

      public String init_Str() {
         return this.init_bJ_BufferHelper.init() + " = " + this.init_Str;
      }

      public String load() {
         return this.init_Str;
      }
   }

   public enum Inner_ConfigStore {
      init,
      load;

      public static bJ.Inner_ConfigStore init(String value) {
         if (value == null) {
            return init;
         } else {
            return "S2C".equalsIgnoreCase(value.strip()) ? load : init;
         }
      }
   }

   public record Inner_DataUtil(byte[] init_BArr, List<String> init_List, List<String> load, bJ.Inner_NetHelper init_bJ_NetHelper) {
      public Inner_DataUtil {
         init_BArr = init_BArr == null ? new byte[0] : (byte[])init_BArr.clone();
         init_List = init_List == null ? List.of() : List.copyOf(init_List);
         load = load == null ? List.of() : List.copyOf(load);
         init_bJ_NetHelper = init_bJ_NetHelper == null ? bJ.init_bJ_NetHelper(init_BArr) : init_bJ_NetHelper;
      }

      public byte[] init_BArr() {
         return (byte[])this.init_BArr.clone();
      }

      public boolean init_Z() {
         return this.load.isEmpty();
      }
   }

   public record Inner_JsonUtil(
      String init_Str,
      bJ.Inner_ConfigStore init_bJ_ConfigStore,
      bJ.Inner_TaskQueue init_bJ_TaskQueue,
      bJ.Inner_StreamUtil init_bJ_StreamUtil,
      List<bJ.Inner_CachePool> init_List
   ) {
      public Inner_JsonUtil {
         init_Str = init_Str != null && !init_Str.isBlank() ? init_Str.strip() : "minecraft:brand";
         init_bJ_ConfigStore = init_bJ_ConfigStore == null ? bJ.Inner_ConfigStore.init : init_bJ_ConfigStore;
         init_bJ_TaskQueue = init_bJ_TaskQueue == null ? bJ.Inner_TaskQueue.init : init_bJ_TaskQueue;
         init_bJ_StreamUtil = init_bJ_StreamUtil == null ? bJ.Inner_StreamUtil.init("", init_Str) : init_bJ_StreamUtil;
         init_List = init_List == null ? List.of() : List.copyOf(init_List);
      }

      public bJ.Inner_DataUtil init_bJ_DataUtil() {
         List<String> warnings = new ArrayList<>();
         List<String> errors = new ArrayList<>();
         bJ.init(this.init_Str, this.init_bJ_ConfigStore, this.init_bJ_TaskQueue, this.init_bJ_StreamUtil, warnings, errors);
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         List<bJ.Inner_CachePool> enabledFields = this.init_List.stream().filter(bJ.Inner_CachePool::init_Z).toList();
         if (enabledFields.isEmpty()) {
            warnings.add("Payload is empty. Some servers reject empty custom payloads.");
         }

         for (bJ.Inner_CachePool field : enabledFields) {
            try {
               bJ.init(out, field, this.init_bJ_StreamUtil, warnings);
            } catch (Exception e) {
               errors.add(field.init_bJ_BufferHelper().init() + ": " + AutismPayloadSupport.init(e));
            }
         }

         byte[] bytes = out.toByteArray();
         if (bytes.length > 8192) {
            warnings.add("Payload is large (" + bytes.length + "B). Use low repeat rates.");
         }

         if (bytes.length > 32767) {
            errors.add("Payload is " + bytes.length + "B; serverbound custom payload max is 32767B.");
         }

         bJ.Inner_NetHelper preview = bJ.init_bJ_NetHelper(bytes);
         return new bJ.Inner_DataUtil(bytes, warnings, errors, preview);
      }
   }

   public record Inner_NetHelper(String init, String load, String read, String write) {
   }

   public enum Inner_StreamUtil {
      init_bJ_StreamUtil("Raw UTF-8"),
      load("Manual Hex"),
      read("Java DataOutput"),
      write("Minecraft ByteBuf"),
      update("JSON/Text"),
      sync("Advanced Mixed");

      private final String init_Str;

      Inner_StreamUtil(String label) {
         this.init_Str = label;
      }

      public String init() {
         return this.init_Str;
      }

      public static bJ.Inner_StreamUtil init(String value, String channel) {
         if (value != null && !value.isBlank()) {
            String normalized = value.strip().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');

            for (bJ.Inner_StreamUtil mode : values()) {
               if (mode.name().equals(normalized)) {
                  return mode;
               }
            }
         }

         if (AutismPayloadSupport.init_Z(channel)) {
            return write;
         } else {
            return "bungeecord:main".equalsIgnoreCase(channel == null ? "" : channel.strip()) ? read : sync;
         }
      }
   }

   public enum Inner_TaskQueue {
      init,
      load;

      public static bJ.Inner_TaskQueue init(String value) {
         if (value == null) {
            return init;
         }

         String normalized = value.strip().toUpperCase(Locale.ROOT);
         return normalized.contains("CONFIG") ? load : init;
      }
   }
}
