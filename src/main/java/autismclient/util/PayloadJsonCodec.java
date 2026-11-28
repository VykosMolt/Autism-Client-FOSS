package autismclient.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class PayloadJsonCodec {
   private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
   private static final int init_I = 16;

   private PayloadJsonCodec() {
   }

   public static void captureInto(autismclient.util.macro.PayloadAction action, String channel, CustomPacketPayload payload) {
      if (action != null) {
         action.channel = channel == null ? "" : channel;
         if (payload == null) {
            action.payloadClassName = "";
            action.payloadJson = emptyJson("", "");
         } else {
            action.payloadClassName = payload.getClass().getName();
            action.payloadJson = encode("C2S", action.channel, payload);
         }
      }
   }

   public static String encode(String direction, String channel, CustomPacketPayload payload) {
      JsonObject root = new JsonObject();
      root.addProperty("direction", direction == null ? "" : direction);
      root.addProperty("channel", channel == null ? "" : channel);
      if (payload != null) {
         root.addProperty("payloadType", payload.getClass().getSimpleName());
         byte[] rawBytes = AutismPayloadSupport.encodePayloadBytes(payload);
         root.addProperty("payloadHex", AutismPayloadSupport.toHexString(rawBytes));
         root.addProperty("payloadBase64", AutismPayloadSupport.toBase64(rawBytes));
         String payloadText = AutismPayloadSupport.decodePrintableUtf8(rawBytes);
         if (!payloadText.isBlank()) {
            root.addProperty("payloadText", payloadText);
         }

         Integer commandApiValue = AutismPayloadSupport.detectCommandApiValue(payload, channel, rawBytes);
         if (commandApiValue != null) {
            root.addProperty("commandApiValue", commandApiValue);
         }
      }

      root.add("payload", payload != null ? toJsonElement(payload, new IdentityHashMap<>(), 0) : new JsonObject());
      return PRETTY_GSON.toJson(root);
   }

   public static String payloadToJson(String channel, CustomPacketPayload payload) {
      return encode("", channel, payload);
   }

   public static String actionToJson(autismclient.util.macro.PayloadAction action) {
      if (action == null) {
         return emptyJson("", "");
      }

      if (action.payloadJson != null && !action.payloadJson.isBlank()) {
         return prettyPrint(action.payloadJson);
      }

      if (action.payloadClassName != null && !action.payloadClassName.isBlank() && action.payloadData != null && !action.payloadData.isBlank()) {
         try {
            CustomPacketPayload payload = decodePayload(action.payloadClassName, AutismPayloadSupport.parseHexOrBase64(action.payloadData));
            if (payload != null) {
               return encode(action.sourceDirection, action.channel, payload);
            }
         } catch (Throwable var2) {
         }
      }

      return emptyJson(action.sourceDirection, action.channel);
   }

   public static String prettyPrint(String jsonText) {
      if (jsonText != null && !jsonText.isBlank()) {
         try {
            return PRETTY_GSON.toJson(JsonParser.parseString(jsonText));
         } catch (Throwable ignored) {
            return jsonText;
         }
      } else {
         return emptyJson("", "");
      }
   }

   public static PayloadJsonCodec.EncodedPayload encode(autismclient.util.macro.PayloadAction action) {
      if (action == null) {
         throw new IllegalArgumentException("Missing payload action");
      }

      String jsonText = action.payloadJson != null && !action.payloadJson.isBlank() ? action.payloadJson : actionToJson(action);

      JsonObject root;
      try {
         root = JsonParser.parseString(jsonText).getAsJsonObject();
      } catch (Throwable t) {
         throw new IllegalArgumentException("Invalid payload JSON: " + t.getMessage(), t);
      }

      String channel = root.has("channel") && root.get("channel").isJsonPrimitive()
         ? root.get("channel").getAsString().trim()
         : (action.channel == null ? "" : action.channel.trim());
      if (channel.isBlank()) {
         throw new IllegalArgumentException("Payload JSON must contain a non-empty channel");
      }

      byte[] rawFallbackBytes = rawBytesOf(root);
      if (rawFallbackBytes == null) {
         rawFallbackBytes = action.payloadData != null && !action.payloadData.isBlank() ? AutismPayloadSupport.parseHexOrBase64(action.payloadData) : new byte[0];
      }

      if (action.payloadClassName != null && !action.payloadClassName.isBlank()) {
         try {
            return encodeTyped(action, root, channel, rawFallbackBytes);
         } catch (Throwable var9) {
            Throwable primaryFailure = var9;

            try {
               PayloadJsonCodec.EncodedPayload fallback = encodeRaw(action, root, channel, rawFallbackBytes);
               if (fallback.bytes().length == 0 && root.has("payload")) {
                  throw new IllegalArgumentException("Known payload rebuild failed and no raw fallback bytes were available", primaryFailure);
               } else {
                  return fallback;
               }
            } catch (Throwable fallbackFailure) {
               IllegalArgumentException error = new IllegalArgumentException(
                  "Could not rebuild payload " + action.payloadClassName + ": " + AutismPayloadSupport.describeError(var9)
               );
               error.addSuppressed(fallbackFailure);
               throw error;
            }
         }
      } else {
         return new PayloadJsonCodec.EncodedPayload(channel, rawFallbackBytes);
      }
   }

   private static boolean isMinecraftClass(String name) {
      return name != null && (name.startsWith("net.minecraft.") || name.startsWith("autismclient."));
   }

   private static PayloadJsonCodec.EncodedPayload encodeTyped(autismclient.util.macro.PayloadAction action, JsonObject root, String channel, byte[] rawFallbackBytes) {
      if (!isMinecraftClass(action.payloadClassName)) {
         return encodeRaw(action, root, channel, rawFallbackBytes);
      }

      Class<?> payloadClass;
      try {
         payloadClass = Class.forName(action.payloadClassName);
      } catch (ClassNotFoundException e) {
         return encodeRaw(action, root, channel, rawFallbackBytes);
      }

      if (!CustomPacketPayload.class.isAssignableFrom(payloadClass)) {
         return encodeRaw(action, root, channel, rawFallbackBytes);
      } else {
         JsonElement payloadElement = root.has("payload") ? root.get("payload") : JsonNull.INSTANCE;
         if (AutismPayloadSupport.looksLikeCommandApiPayload(decodeWithCodec(payloadClass, channel, rawFallbackBytes))) {
            return encodeCommandApiPayload(action, root, channel, payloadClass, rawFallbackBytes);
         } else {
            return payloadClass.isRecord()
               ? encodeFromJson(payloadElement, payloadClass, channel, rawFallbackBytes)
               : encodeFromJsonOrRaw(payloadElement, payloadClass, channel, rawFallbackBytes);
         }
      }
   }

   private static CustomPacketPayload decodeWithCodec(Class<?> payloadClass, String channel, byte[] rawFallbackBytes) {
      try {
         StreamCodec codec = AutismPayloadSupport.findStreamCodec(payloadClass);
         if (codec != null && rawFallbackBytes.length > 0) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer((byte[])rawFallbackBytes.clone()));
            if (codec.decode(buf) instanceof CustomPacketPayload cp) {
               return cp;
            }
         }
      } catch (Throwable var7) {
      }

      return null;
   }

   private static PayloadJsonCodec.EncodedPayload encodeCommandApiPayload(autismclient.util.macro.PayloadAction action, JsonObject root, String channel, Class<?> payloadClass, byte[] rawFallbackBytes) {
      int value;
      if (root.has("commandApiValue") && root.get("commandApiValue").isJsonPrimitive()) {
         value = root.get("commandApiValue").getAsInt();
      } else if (action.commandApiRecognized && action.commandApiOverride) {
         value = action.commandApiValue;
      } else {
         value = leadingInt(rawFallbackBytes);
      }

      try {
         Constructor<?> intCtor = findConstructor(payloadClass);
         if (intCtor != null) {
            if (!intCtor.canAccess(null)) {
               intCtor.setAccessible(true);
            }

            if (intCtor.newInstance(value) instanceof CustomPacketPayload cp) {
               byte[] bytes = AutismPayloadSupport.encodePayloadBytes(cp);
               if (bytes.length > 0) {
                  return new PayloadJsonCodec.EncodedPayload(channel, bytes);
               }
            }
         }
      } catch (Throwable var9) {
      }

      return new PayloadJsonCodec.EncodedPayload(channel, AutismPayloadSupport.withLeadingBigEndianInt(rawFallbackBytes, value));
   }

   private static Constructor<?> findConstructor(Class<?> clazz) {
      for (Constructor<?> ctor : clazz.getDeclaredConstructors()) {
         Class<?>[] params = ctor.getParameterTypes();
         if (params.length == 1 && (params[0] == int.class || params[0] == Integer.class)) {
            return ctor;
         }
      }

      return null;
   }

   private static PayloadJsonCodec.EncodedPayload encodeFromJson(JsonElement payloadElement, Class<?> payloadClass, String channel, byte[] rawFallbackBytes) {
      try {
         CustomPacketPayload templatePayload = decodePayload(payloadClass.getName(), rawFallbackBytes);
         if (fromJson(payloadElement, (Type)payloadClass, payloadClass, templatePayload, 0) instanceof CustomPacketPayload cp) {
            byte[] bytes = AutismPayloadSupport.encodePayloadBytes(cp);
            if (bytes.length > 0) {
               return new PayloadJsonCodec.EncodedPayload(channel, bytes);
            }
         }
      } catch (Throwable var7) {
      }

      byte[] bytes = reencodeRaw(payloadClass, rawFallbackBytes);
      return new PayloadJsonCodec.EncodedPayload(channel, bytes != null ? bytes : rawFallbackBytes);
   }

   private static PayloadJsonCodec.EncodedPayload encodeFromJsonOrRaw(JsonElement payloadElement, Class<?> payloadClass, String channel, byte[] rawFallbackBytes) {
      try {
         CustomPacketPayload templatePayload = decodePayload(payloadClass.getName(), rawFallbackBytes);
         if (fromJson(payloadElement, (Type)payloadClass, payloadClass, templatePayload, 0) instanceof CustomPacketPayload cp) {
            byte[] bytes = AutismPayloadSupport.encodePayloadBytes(cp);
            if (bytes.length > 0) {
               return new PayloadJsonCodec.EncodedPayload(channel, bytes);
            }
         }
      } catch (Throwable var7) {
      }

      byte[] bytes = reencodeRaw(payloadClass, rawFallbackBytes);
      return new PayloadJsonCodec.EncodedPayload(channel, bytes != null ? bytes : rawFallbackBytes);
   }

   private static PayloadJsonCodec.EncodedPayload encodeRaw(autismclient.util.macro.PayloadAction action, JsonObject root, String channel, byte[] rawFallbackBytes) {
      if (root.has("commandApiValue") && root.get("commandApiValue").isJsonPrimitive()) {
         int value = root.get("commandApiValue").getAsInt();
         return new PayloadJsonCodec.EncodedPayload(channel, AutismPayloadSupport.withLeadingBigEndianInt(rawFallbackBytes, value));
      } else {
         return action.commandApiRecognized && action.commandApiOverride
            ? new PayloadJsonCodec.EncodedPayload(channel, AutismPayloadSupport.withLeadingBigEndianInt(rawFallbackBytes, action.commandApiValue))
            : new PayloadJsonCodec.EncodedPayload(channel, rawFallbackBytes);
      }
   }

   private static byte[] reencodeRaw(Class<?> payloadClass, byte[] rawFallbackBytes) {
      if (rawFallbackBytes.length == 0) {
         return null;
      }

      try {
         CustomPacketPayload decoded = decodePayload(payloadClass.getName(), rawFallbackBytes);
         if (decoded != null) {
            StreamCodec codec = AutismPayloadSupport.findStreamCodec(payloadClass);
            if (codec != null) {
               FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
               codec.encode(buf, decoded);
               return AutismPayloadSupport.copyBufferBytes(buf);
            }
         }
      } catch (Throwable var5) {
      }

      return null;
   }

   public static String payloadToJsonSafe(String direction, String channel, CustomPacketPayload payload) {
      try {
         return encode(direction, channel, payload);
      } catch (Throwable t) {
         return emptyJson(direction, channel);
      }
   }

   public static String payloadToJsonSafe(String channel, CustomPacketPayload payload) {
      return payloadToJsonSafe("", channel, payload);
   }

   private static CustomPacketPayload decodePayload(String payloadClassName, byte[] bytes) {
      if (payloadClassName == null || payloadClassName.isBlank()) {
         return null;
      }

      if (!isMinecraftClass(payloadClassName)) {
         return null;
      }

      try {
         Class<?> payloadClass = Class.forName(payloadClassName);
         StreamCodec codec = AutismPayloadSupport.findStreamCodec(payloadClass);
         if (codec == null) {
            return null;
         }

         FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes == null ? new byte[0] : (byte[])bytes.clone()));
         return codec.decode(buf) instanceof CustomPacketPayload payload ? payload : null;
      } catch (Throwable e) {
         return null;
      }
   }

   private static String emptyJson(String direction, String channel) {
      JsonObject root = new JsonObject();
      root.addProperty("direction", direction == null ? "" : direction);
      root.addProperty("channel", channel == null ? "" : channel);
      root.addProperty("payloadHex", "");
      root.add("payload", new JsonObject());
      return PRETTY_GSON.toJson(root);
   }

   private static byte[] rawBytesOf(JsonObject root) {
      if (root == null) {
         return null;
      }

      String hex = stringField(root, "payloadHex");
      if (!hex.isBlank()) {
         return AutismPayloadSupport.parseHexOrBase64(hex);
      }

      String base64 = stringField(root, "payloadBase64");
      if (!base64.isBlank()) {
         return Base64.getDecoder().decode(base64);
      }

      String text = stringField(root, "payloadText");
      if (!text.isBlank()) {
         return text.getBytes(StandardCharsets.UTF_8);
      }

      JsonObject payload = root.has("payload") && root.get("payload").isJsonObject() ? root.getAsJsonObject("payload") : null;
      if (payload != null && payload.has("bytes") && payload.get("bytes").isJsonArray()) {
         JsonArray bytes = payload.getAsJsonArray("bytes");
         byte[] out = new byte[bytes.size()];

         for (int i = 0; i < bytes.size(); i++) {
            out[i] = (byte)bytes.get(i).getAsInt();
         }

         return out;
      } else {
         return null;
      }
   }

   private static String stringField(JsonObject root, String key) {
      if (root != null && key != null && root.has(key)) {
         JsonElement element = root.get(key);
         return element != null && element.isJsonPrimitive() ? element.getAsString().trim() : "";
      } else {
         return "";
      }
   }

   private static JsonElement toJsonElement(Object value, IdentityHashMap<Object, Boolean> visited, int depth) {
      if (value == null) {
         return JsonNull.INSTANCE;
      }

      if (depth >= 16) {
         return new JsonPrimitive("<max-depth>");
      }

      if (value instanceof CustomPacketPayload cp && AutismPayloadSupport.looksLikeCommandApiPayload(cp)) {
         byte[] rawBytes = AutismPayloadSupport.encodePayloadBytes(cp);
         Integer cmdVal = AutismPayloadSupport.detectCommandApiValue(cp, null, rawBytes);
         if (cmdVal != null) {
            JsonObject cmdObj = new JsonObject();
            cmdObj.addProperty("commandApiValue", cmdVal);
            return cmdObj;
         }
      }

      if (isSimpleValue(value)) {
         return toJson(value);
      } else if (value instanceof Optional<?> optional) {
         return optional.<JsonElement>map(v -> toJsonElement(v, visited, depth + 1)).orElse(JsonNull.INSTANCE);
      } else if (value instanceof Component text) {
         try {
            return ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, text).getOrThrow();
         } catch (Throwable t) {
            return new JsonPrimitive(text.getString());
         }
      } else if (value instanceof ItemStack stack) {
         try {
            return ItemStack.CODEC.encodeStart(registryProvider().createSerializationContext(JsonOps.INSTANCE), stack).getOrThrow();
         } catch (Throwable t) {
            return new JsonPrimitive(stack.toString());
         }
      } else if (value instanceof Tag nbt) {
         try {
            return new JsonPrimitive(nbt.asString().orElse(String.valueOf(nbt)));
         } catch (Throwable t) {
            return new JsonPrimitive(String.valueOf(nbt));
         }
      } else {
         if (visited.containsKey(value)) {
            return new JsonPrimitive("<cycle>");
         }

         visited.put(value, Boolean.TRUE);
         if (value.getClass().isArray()) {
            JsonArray array = new JsonArray();
            int length = Array.getLength(value);

            for (int i = 0; i < length; i++) {
               array.add(toJsonElement(Array.get(value, i), visited, depth + 1));
            }

            return array;
         } else if (value instanceof Collection<?> collection) {
            JsonArray array = new JsonArray();

            for (Object element : collection) {
               array.add(toJsonElement(element, visited, depth + 1));
            }

            return array;
         } else if (value instanceof Map<?, ?> map) {
            JsonObject object = new JsonObject();

            for (Entry<?, ?> entry : map.entrySet()) {
               String key = keyString(entry.getKey());
               object.add(key, toJsonElement(entry.getValue(), visited, depth + 1));
            }

            return object;
         } else {
            JsonObject object = new JsonObject();
            if (value.getClass().isRecord()) {
               RecordComponent[] components = value.getClass().getRecordComponents();

               for (RecordComponent component : components) {
                  String label = componentName(component);
                  Object fieldValue = invokeAccessor(component.getAccessor(), value);
                  object.add(label, toJsonElement(fieldValue, visited, depth + 1));
               }

               return object;
            } else {
               for (Field field : instanceFields(value.getClass())) {
                  String label = fieldName(field);
                  Object fieldValue = readField(field, value);
                  object.add(label, toJsonElement(fieldValue, visited, depth + 1));
               }

               return object;
            }
         }
      }
   }

   private static Object fromJson(JsonElement element, Type genericType, Class<?> targetType, Object template, int depth) {
      if (targetType == null) {
         return null;
      }

      if (depth >= 16) {
         return template;
      }

      if (element != null && !element.isJsonNull()) {
         if (targetType == String.class) {
            return element.getAsString();
         } else if (targetType == boolean.class || targetType == Boolean.class) {
            return element.getAsBoolean();
         } else if (targetType == byte.class || targetType == Byte.class) {
            return element.getAsByte();
         } else if (targetType == short.class || targetType == Short.class) {
            return element.getAsShort();
         } else if (targetType == int.class || targetType == Integer.class) {
            return element.getAsInt();
         } else if (targetType == long.class || targetType == Long.class) {
            return element.getAsLong();
         } else if (targetType == float.class || targetType == Float.class) {
            return element.getAsFloat();
         } else if (targetType == double.class || targetType == Double.class) {
            return element.getAsDouble();
         } else if (targetType == char.class || targetType == Character.class) {
            String text = element.getAsString();
            return text.isEmpty() ? '\u0000' : text.charAt(0);
         } else if (targetType == Identifier.class) {
            return Identifier.parse(element.getAsString());
         } else if (targetType == UUID.class) {
            return UUID.fromString(element.getAsString());
         } else if (targetType.isEnum()) {
            return enumConstant(targetType, element.getAsString(), template);
         } else if (targetType == Component.class) {
            return parseComponent(element);
         } else if (targetType == BlockPos.class) {
            return parseBlockPos(element);
         } else if (targetType == Vec3.class) {
            return parseVec3(element);
         } else if (ItemStack.class.isAssignableFrom(targetType)) {
            return ItemStack.CODEC.parse(registryProvider().createSerializationContext(JsonOps.INSTANCE), element).result().orElse(ItemStack.EMPTY);
         } else if (Tag.class.isAssignableFrom(targetType)) {
            return template;
         } else if (Optional.class.isAssignableFrom(targetType)) {
            Type innerType = typeArgument(genericType, 0);
            Class<?> innerClass = rawClass(innerType, template == null ? Object.class : template.getClass());
            return Optional.ofNullable(fromJson(element, innerType, innerClass, unwrapOptional(template), depth + 1));
         } else if (targetType.isArray()) {
            return arrayFromJson(element, targetType.getComponentType(), componentType(genericType), template, depth);
         } else if (Collection.class.isAssignableFrom(targetType)) {
            return collectionFromJson(element, genericType, targetType, template, depth);
         } else if (Map.class.isAssignableFrom(targetType)) {
            return mapFromJson(element, genericType, targetType, template, depth);
         } else if (targetType.isRecord()) {
            return recordFromJson(element, targetType, template, depth);
         } else if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return template != null ? template : element.getAsString();
         } else {
            return objectFromJson(element, targetType, template, depth);
         }
      } else {
         return targetType == Optional.class ? Optional.empty() : defaultValue(targetType, template);
      }
   }

   private static Object recordFromJson(JsonElement element, Class<?> targetType, Object template, int depth) {
      if (!element.isJsonObject()) {
         return template;
      }

      try {
         JsonObject jsonObj = element.getAsJsonObject();
         RecordComponent[] components = targetType.getRecordComponents();
         Class<?>[] componentTypes = new Class[components.length];
         Object[] args = new Object[components.length];

         for (int i = 0; i < components.length; i++) {
            RecordComponent component = components[i];
            componentTypes[i] = component.getType();
            String label = componentName(component);
            JsonElement valueElement = jsonObj.has(label) ? jsonObj.get(label) : null;
            Object templateValue = template == null ? null : invokeAccessor(component.getAccessor(), template);
            args[i] = fromJson(valueElement, component.getGenericType(), component.getType(), templateValue, depth + 1);
         }

         Constructor<?> ctor = targetType.getDeclaredConstructor(componentTypes);
         if (!ctor.canAccess(null)) {
            ctor.setAccessible(true);
         }

         return ctor.newInstance(args);
      } catch (Throwable t) {
         return template;
      }
   }

   private static Object objectFromJson(JsonElement element, Class<?> targetType, Object template, int depth) {
      if (!element.isJsonObject()) {
         return template;
      }

      Object instance = template != null ? template : instantiate(targetType);
      JsonObject object = element.getAsJsonObject();

      for (Field field : instanceFields(targetType)) {
         String label = fieldName(field);
         if (object.has(label)) {
            Object currentValue = readField(field, instance);
            Object decoded = fromJson(object.get(label), field.getGenericType(), field.getType(), currentValue, depth + 1);
            writeField(field, instance, decoded);
         }
      }

      return instance;
   }

   private static Object arrayFromJson(JsonElement element, Class<?> componentType, Type componentGenericType, Object template, int depth) {
      if (!element.isJsonArray()) {
         return template;
      }

      JsonArray array = element.getAsJsonArray();
      Object out = Array.newInstance(componentType, array.size());

      for (int i = 0; i < array.size(); i++) {
         Object templateValue = template != null && i < Array.getLength(template) ? Array.get(template, i) : null;
         Array.set(out, i, fromJson(array.get(i), componentGenericType, componentType, templateValue, depth + 1));
      }

      return out;
   }

   private static Object collectionFromJson(JsonElement element, Type genericType, Class<?> targetType, Object template, int depth) {
      if (!element.isJsonArray()) {
         return template;
      }

      Collection<Object> values = Set.class.isAssignableFrom(targetType) ? new LinkedHashSet<>() : new ArrayList<>();
      Type itemType = typeArgument(genericType, 0);
      Class<?> itemClass = rawClass(itemType, Object.class);
      int index = 0;

      for (JsonElement entry : element.getAsJsonArray()) {
         Object templateValue = template instanceof List<?> list && index < list.size() ? list.get(index) : null;
         values.add(fromJson(entry, itemType, itemClass, templateValue, depth + 1));
         index++;
      }

      return values;
   }

   private static Object mapFromJson(JsonElement element, Type genericType, Class<?> targetType, Object template, int depth) {
      if (!element.isJsonObject()) {
         return template;
      }

      Map<Object, Object> map = new LinkedHashMap<>();
      Type keyType = typeArgument(genericType, 0);
      Type valueType = typeArgument(genericType, 1);
      Class<?> keyClass = rawClass(keyType, String.class);
      Class<?> valueClass = rawClass(valueType, Object.class);

      for (Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
         Object key = parseMapKey(entry.getKey(), keyClass);
         map.put(key, fromJson(entry.getValue(), valueType, valueClass, null, depth + 1));
      }

      return map;
   }

   private static Object instantiate(Class<?> targetType) {
      try {
         Constructor<?> ctor = targetType.getDeclaredConstructor();
         if (!ctor.canAccess(null)) {
            ctor.setAccessible(true);
         }

         return ctor.newInstance();
      } catch (Throwable t) {
         throw new IllegalArgumentException("Cannot instantiate payload type " + targetType.getName(), t);
      }
   }

   private static Object defaultValue(Class<?> targetType, Object template) {
      if (template != null) {
         return template;
      } else if (!targetType.isPrimitive()) {
         return null;
      } else if (targetType == boolean.class) {
         return false;
      } else if (targetType == char.class) {
         return '\u0000';
      } else if (targetType == byte.class) {
         return (byte)0;
      } else if (targetType == short.class) {
         return (short)0;
      } else if (targetType == int.class) {
         return 0;
      } else if (targetType == long.class) {
         return 0L;
      } else if (targetType == float.class) {
         return 0.0F;
      } else {
         return targetType == double.class ? 0.0 : null;
      }
   }

   private static JsonElement toJson(Object value) {
      if (value == null) {
         return JsonNull.INSTANCE;
      } else if (value instanceof Number number) {
         return new JsonPrimitive(number);
      } else if (value instanceof Boolean bool) {
         return new JsonPrimitive(bool);
      } else if (value instanceof Character character) {
         return new JsonPrimitive(character);
      } else if (value instanceof String string) {
         return new JsonPrimitive(string);
      } else if (value instanceof Identifier identifier) {
         return new JsonPrimitive(identifier.toString());
      } else if (value instanceof UUID uuid) {
         return new JsonPrimitive(uuid.toString());
      } else if (value instanceof Enum<?> enumValue) {
         return new JsonPrimitive(enumValue.name());
      } else if (value instanceof BlockPos pos) {
         JsonObject object = new JsonObject();
         object.addProperty("x", pos.getX());
         object.addProperty("y", pos.getY());
         object.addProperty("z", pos.getZ());
         return object;
      } else if (value instanceof Vec3 vec) {
         JsonObject object = new JsonObject();
         object.addProperty("x", vec.x);
         object.addProperty("y", vec.y);
         object.addProperty("z", vec.z);
         return object;
      } else if (!(value instanceof byte[] bytes)) {
         return new JsonPrimitive(String.valueOf(value));
      } else {
         JsonArray array = new JsonArray();

         for (byte b : bytes) {
            array.add(Integer.valueOf(b));
         }

         return array;
      }
   }

   private static boolean isSimpleValue(Object value) {
      return value instanceof Number
         || value instanceof Boolean
         || value instanceof Character
         || value instanceof String
         || value instanceof Identifier
         || value instanceof UUID
         || value instanceof Enum
         || value instanceof BlockPos
         || value instanceof Vec3
         || value instanceof byte[];
   }

   private static List<Field> instanceFields(Class<?> type) {
      List<Field> fields = new ArrayList<>();

      for (Class<?> cursor = type; cursor != null && cursor != Object.class; cursor = cursor.getSuperclass()) {
         for (Field field : cursor.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers()) && !field.isSynthetic()) {
               fields.add(field);
            }
         }
      }

      return fields;
   }

   private static String fieldName(Field field) {
      String mapped = InspectorMappingsIndex.fieldAlias(field);
      return mapped != null && !mapped.isBlank() ? mapped : field.getName();
   }

   private static String componentName(RecordComponent component) {
      String mapped = InspectorMappingsIndex.methodAlias(component.getAccessor());
      return mapped != null && !mapped.isBlank() ? mapped : component.getName();
   }

   private static Object invokeAccessor(Method accessor, Object owner) {
      try {
         if (!accessor.canAccess(owner)) {
            accessor.setAccessible(true);
         }

         return accessor.invoke(owner);
      } catch (Throwable t) {
         return null;
      }
   }

   private static Object readField(Field field, Object owner) {
      try {
         if (!field.canAccess(owner)) {
            field.setAccessible(true);
         }

         return field.get(owner);
      } catch (Throwable t) {
         return null;
      }
   }

   private static void writeField(Field field, Object owner, Object value) {
      try {
         if (!field.canAccess(owner)) {
            field.setAccessible(true);
         }

         field.set(owner, value);
      } catch (Throwable var4) {
      }
   }

   private static Object enumConstant(Class<?> enumType, String name, Object template) {
      for (Object constant : enumType.getEnumConstants()) {
         if (constant instanceof Enum<?> enumValue && enumValue.name().equalsIgnoreCase(name)) {
            return constant;
         }
      }

      return template;
   }

   private static Component parseComponent(JsonElement element) {
      try {
         return element.isJsonPrimitive()
            ? Component.literal(element.getAsString())
            : ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, element).getOrThrow();
      } catch (Throwable t) {
         return Component.literal(element.toString());
      }
   }

   private static BlockPos parseBlockPos(JsonElement element) {
      if (!element.isJsonObject()) {
         return BlockPos.ZERO;
      }

      JsonObject object = element.getAsJsonObject();
      return new BlockPos(
         object.has("x") ? object.get("x").getAsInt() : 0, object.has("y") ? object.get("y").getAsInt() : 0, object.has("z") ? object.get("z").getAsInt() : 0
      );
   }

   private static Vec3 parseVec3(JsonElement element) {
      if (!element.isJsonObject()) {
         return Vec3.ZERO;
      }

      JsonObject object = element.getAsJsonObject();
      return new Vec3(
         object.has("x") ? object.get("x").getAsDouble() : 0.0,
         object.has("y") ? object.get("y").getAsDouble() : 0.0,
         object.has("z") ? object.get("z").getAsDouble() : 0.0
      );
   }

   private static String keyString(Object key) {
      if (key == null) {
         return "null";
      } else if (key instanceof Identifier identifier) {
         return identifier.toString();
      } else {
         return key instanceof Enum<?> enumValue ? enumValue.name() : String.valueOf(key);
      }
   }

   private static Object parseMapKey(String key, Class<?> keyClass) {
      if (keyClass == Identifier.class) {
         return Identifier.parse(key);
      } else if (keyClass == UUID.class) {
         return UUID.fromString(key);
      } else if (keyClass.isEnum()) {
         return enumConstant(keyClass, key, (Object)null);
      } else if (keyClass != Integer.class && keyClass != int.class) {
         return keyClass != Long.class && keyClass != long.class ? key : Long.parseLong(key);
      } else {
         return Integer.parseInt(key);
      }
   }

   private static Type typeArgument(Type type, int index) {
      if (type instanceof ParameterizedType parameterizedType) {
         Type[] args = parameterizedType.getActualTypeArguments();
         if (index >= 0 && index < args.length) {
            return args[index];
         }
      }

      return Object.class;
   }

   private static Type componentType(Type type) {
      if (type instanceof Class<?> cls && cls.isArray()) {
         return cls.getComponentType();
      } else {
         return type instanceof GenericArrayType genericArrayType ? genericArrayType.getGenericComponentType() : Object.class;
      }
   }

   private static Class<?> rawClass(Type type, Class<?> fallback) {
      if (type instanceof Class<?> cls) {
         return cls;
      } else if (type instanceof ParameterizedType parameterizedType) {
         return rawClass(parameterizedType.getRawType(), fallback);
      } else {
         return type instanceof GenericArrayType genericArrayType
            ? Array.newInstance(rawClass(genericArrayType.getGenericComponentType(), Object.class), 0).getClass()
            : fallback;
      }
   }

   private static Object unwrapOptional(Object template) {
      return template instanceof Optional<?> optional ? optional.orElse(null) : null;
   }

   private static Provider registryProvider() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null) {
         return mc.level.registryAccess();
      } else {
         return mc.getConnection() != null ? mc.getConnection().registryAccess() : RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
      }
   }

   private static int leadingInt(byte[] rawBytes) {
      return rawBytes != null && rawBytes.length >= 4 ? ByteBuffer.wrap(rawBytes, 0, 4).order(ByteOrder.BIG_ENDIAN).getInt() : 0;
   }

   public record EncodedPayload(String channel, byte[] bytes) {
      public EncodedPayload {
         bytes = bytes == null ? new byte[0] : (byte[])bytes.clone();
      }

      public byte[] bytes() {
         return (byte[])this.bytes.clone();
      }
   }
}
