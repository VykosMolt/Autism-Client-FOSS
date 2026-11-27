package autismclient.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.network.protocol.Packet;

public class PacketNameResolver {
   private static final String UNKNOWN = "Unknown Packet";
   private static final ConcurrentMap<Class<?>, String> NAME_CACHE = new ConcurrentHashMap<>();
   private static final ConcurrentMap<String, String> load = new ConcurrentHashMap<>();
   private static final ConcurrentMap<Class<?>, String> read = new ConcurrentHashMap<>();
   private static final ConcurrentMap<Class<?>, String> write = new ConcurrentHashMap<>();
   private static final ConcurrentMap<String, String> update = new ConcurrentHashMap<>();
   private static final Map<String, String> ALIASES = buildAliases();

   private static String read(String name) {
      return name != null && name.endsWith("Packet") ? name.substring(0, name.length() - 6) : name;
   }

   public static String nameOf(Packet<?> packet) {
      Class<? extends Packet<?>> packetClass = (Class<? extends Packet<?>>)packet.getClass();
      String cached = NAME_CACHE.get(packetClass);
      if (cached != null) {
         return cached;
      }

      String name = load(packetClass);
      if (name != null) {
         return init_Str_Class_Str(packetClass, read(name));
      }

      String typeName = load(packet);
      if (typeName != null) {
         return init_Str_Class_Str(packetClass, read(typeName));
      }

      String inspected = read(packet, packetClass.getSimpleName());
      if (inspected != null) {
         return init_Str_Class_Str(packetClass, inspected);
      }

      String readable = read(packetClass);
      return init_Str_Class_Str(packetClass, readable != null ? readable : "Unknown Packet");
   }

   public static String directionalName(Packet<?> packet, String direction) {
      Class<? extends Packet<?>> packetClass = (Class<? extends Packet<?>>)packet.getClass();
      ConcurrentMap<Class<?>, String> classCache = cacheFor(direction);
      if (classCache != null) {
         String fast = classCache.get(packetClass);
         if (fast != null) {
            return fast;
         }
      }

      String cacheKey = load(packetClass, direction);
      String cached = load.get(cacheKey);
      if (cached != null) {
         return init_Str_ConcurrentMap(classCache, packetClass, cached);
      }

      String name = read(packetClass, direction);
      if (name != null) {
         return init_Str_ConcurrentMap(classCache, packetClass, init_Str_Str_Str(cacheKey, read(name)));
      }

      String typeName = load(packet, direction);
      if (typeName != null) {
         return init_Str_ConcurrentMap(classCache, packetClass, init_Str_Str_Str(cacheKey, read(typeName)));
      }

      String inspected = read(packet, packetClass.getSimpleName());
      if (inspected != null) {
         return init_Str_ConcurrentMap(classCache, packetClass, init_Str_Str_Str(cacheKey, inspected));
      }

      String readable = read(packetClass);
      return init_Str_ConcurrentMap(classCache, packetClass, init_Str_Str_Str(cacheKey, readable != null ? readable : "Unknown Packet"));
   }

   private static ConcurrentMap<Class<?>, String> cacheFor(String direction) {
      if ("S2C".equalsIgnoreCase(direction)) {
         return read;
      } else {
         return "C2S".equalsIgnoreCase(direction) ? write : null;
      }
   }

   private static String init_Str_ConcurrentMap(ConcurrentMap<Class<?>, String> classCache, Class<?> packetClass, String value) {
      if (classCache != null && value != null) {
         classCache.putIfAbsent(packetClass, value);
      }

      return value;
   }

   public static String nameOfClass(Class<? extends Packet<?>> packetClass) {
      String cached = NAME_CACHE.get(packetClass);
      if (cached != null) {
         return cached;
      }

      String name = load(packetClass);
      if (name != null) {
         return init_Str_Class_Str(packetClass, read(name));
      }

      String readable = read(packetClass);
      return init_Str_Class_Str(packetClass, readable != null ? readable : "Unknown Packet");
   }

   private static String init_Str_Class_Str(Class<?> packetClass, String value) {
      String safe = value == null ? "Unknown Packet" : value;
      if (packetClass != null) {
         NAME_CACHE.putIfAbsent(packetClass, safe);
      }

      return safe;
   }

   private static String init_Str_Str_Str(String key, String value) {
      String safe = value == null ? "Unknown Packet" : value;
      if (key != null) {
         load.putIfAbsent(key, safe);
      }

      return safe;
   }

   private static String load(Class<?> packetClass, String direction) {
      String dir = direction == null ? "" : direction.toUpperCase(Locale.ROOT);
      return dir + "|" + (packetClass == null ? "" : packetClass.getName());
   }

   public static String friendlyName(String name) {
      if (name != null && !name.isEmpty()) {
         Class<? extends Packet<?>> packetClass = PacketTypeRegistry.resolvePacketClass(name);
         if (packetClass != null) {
            String resolved = load(packetClass);
            return resolved != null ? read(resolved) : "Unknown Packet";
         }

         String resolved = write(name);
         if (resolved != null) {
            return read(resolved);
         }

         String readable = update(name);
         return readable != null ? readable : "Unknown Packet";
      } else {
         return "";
      }
   }

   private static String load(Class<? extends Packet<?>> packetClass) {
      if (packetClass == null) {
         return null;
      }

      String direct = PacketTypeRegistry.getPacketName(packetClass);
      if (direct != null) {
         return direct;
      }

      for (Class<? extends Packet<?>> registeredClass : PacketTypeRegistry.ALL_PACKETS) {
         if (registeredClass == packetClass || registeredClass.isAssignableFrom(packetClass)) {
            String name = PacketTypeRegistry.getPacketName(registeredClass);
            if (name != null) {
               return name;
            }
         }
      }

      String byText = write(packetClass.getName());
      return byText != null ? byText : write(packetClass.getCanonicalName());
   }

   private static String read(Class<? extends Packet<?>> packetClass, String direction) {
      if (packetClass == null) {
         return null;
      }

      String direct = write(packetClass, direction);
      if (direct != null) {
         return direct;
      }

      String byText = load(packetClass.getName(), direction);
      return byText != null ? byText : load(packetClass.getCanonicalName(), direction);
   }

   private static String load(Packet<?> packet) {
      if (packet == null) {
         return null;
      }

      try {
         Object packetType = packet.getClass().getMethod("getPacketType").invoke(packet);
         return packetType == null ? null : write(packetType.toString());
      } catch (Throwable ignored) {
         return null;
      }
   }

   private static String load(Packet<?> packet, String direction) {
      if (packet == null) {
         return null;
      }

      try {
         Object packetType = packet.getClass().getMethod("getPacketType").invoke(packet);
         return packetType == null ? null : load(packetType.toString(), direction);
      } catch (Throwable ignored) {
         return null;
      }
   }

   private static String write(String text) {
      if (text != null && !text.isEmpty()) {
         String normalizedText = load(text);
         if (normalizedText.isEmpty()) {
            return null;
         }

         String manual = ALIASES.get(normalizedText);
         if (manual != null) {
            return manual;
         }

         for (Class<? extends Packet<?>> registeredClass : PacketTypeRegistry.ALL_PACKETS) {
            String registeredName = PacketTypeRegistry.getPacketName(registeredClass);
            if (registeredName != null && init_Z_Str(normalizedText, registeredName, registeredClass)) {
               return registeredName;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static String load(String text, String direction) {
      if (text != null && !text.isEmpty()) {
         String normalizedText = load(text);
         if (normalizedText.isEmpty()) {
            return null;
         }

         String manual = ALIASES.get(normalizedText);
         if (manual != null) {
            return manual;
         }

         for (Class<? extends Packet<?>> registeredClass : registeredClasses(direction)) {
            String registeredName = PacketTypeRegistry.getPacketName(registeredClass);
            if (registeredName != null && init_Z_Str(normalizedText, registeredName, registeredClass)) {
               return registeredName;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static String write(Class<? extends Packet<?>> packetClass, String direction) {
      for (Class<? extends Packet<?>> registeredClass : registeredClasses(direction)) {
         if (registeredClass == packetClass) {
            return PacketTypeRegistry.getPacketName(registeredClass);
         }
      }

      return null;
   }

   private static Iterable<Class<? extends Packet<?>>> registeredClasses(String direction) {
      if ("C2S".equalsIgnoreCase(direction)) {
         return PacketTypeRegistry.getC2sPackets();
      } else {
         return "S2C".equalsIgnoreCase(direction) ? PacketTypeRegistry.getS2cPackets() : PacketTypeRegistry.ALL_PACKETS;
      }
   }

   private static boolean init_Z_Str(String normalizedText, String registeredName, Class<? extends Packet<?>> registeredClass) {
      String normalizedRegistered = load(registeredName);
      if (normalizedRegistered.equals(normalizedText)) {
         return true;
      }

      String strippedRegistered = load(read(registeredName));
      if (strippedRegistered.equals(normalizedText)) {
         return true;
      }

      String className = load(registeredClass.getName());
      if (!className.equals(normalizedText) && !normalizedText.endsWith(className)) {
         String simpleName = load(registeredClass.getSimpleName());
         if (!simpleName.equals(normalizedText) && !normalizedText.endsWith(simpleName)) {
            String canonicalName = load(registeredClass.getCanonicalName());
            return canonicalName.isEmpty() || !canonicalName.equals(normalizedText) && !normalizedText.endsWith(canonicalName)
               ? normalizedText.endsWith(normalizedRegistered) || normalizedText.endsWith(strippedRegistered)
               : true;
         } else {
            return true;
         }
      } else {
         return true;
      }
   }

   private static Map<String, String> buildAliases() {
      Map<String, String> aliases = new HashMap<>();
      alias(
         aliases,
         "BundleS2CPacket",
         "BundleS2C",
         "class_8042",
         "net.minecraft.class_8042",
         "net.minecraft.network.protocol.game.BundleS2CPacket",
         "net.minecraft.network.protocol.game.ClientboundBundlePacket"
      );
      return Collections.unmodifiableMap(aliases);
   }

   private static void alias(Map<String, String> aliases, String canonicalName, String... variants) {
      aliases.put(load(canonicalName), canonicalName);
      aliases.put(load(read(canonicalName)), canonicalName);

      for (String variant : variants) {
         if (variant != null && !variant.isEmpty()) {
            aliases.put(load(variant), canonicalName);
         }
      }
   }

   private static String read(Class<?> packetClass) {
      if (packetClass == null) {
         return null;
      }

      String canonical = update(packetClass.getCanonicalName());
      return canonical != null ? canonical : update(packetClass.getName());
   }

   private static String update(String rawName) {
      if (rawName != null && !rawName.isEmpty()) {
         int lastDot = rawName.lastIndexOf(46);
         String shortName = lastDot >= 0 ? rawName.substring(lastDot + 1) : rawName;
         shortName = shortName.replace('$', '.');
         return init_Z_Str(shortName) ? null : read(shortName);
      } else {
         return null;
      }
   }

   private static boolean init_Z_Str(String name) {
      String normalized = load(name);
      return normalized.startsWith("class") && normalized.length() > 5;
   }

   public static String load(String value) {
      if (value != null && !value.isEmpty()) {
         if (update.size() > 4096) {
            update.clear();
         }

         return update.computeIfAbsent(value, PacketNameResolver::sync);
      } else {
         return "";
      }
   }

   private static String sync(String value) {
      String lower = value.toLowerCase(Locale.ROOT);
      StringBuilder out = new StringBuilder(lower.length());

      for (int i = 0; i < lower.length(); i++) {
         char c = lower.charAt(i);
         if (c >= 'a' && c <= 'z' || c >= '0' && c <= '9') {
            out.append(c);
         }
      }

      return out.toString();
   }

   private static String read(Packet<?> packet, String className) {
      try {
         String packetStr = packet.toString();
         if (!packetStr.contains("slot=") && !packetStr.contains("Slot")) {
            if (packetStr.contains("hand=") || packetStr.contains("Hand")) {
               return "Swing Hand";
            } else if (packetStr.contains("button=") || packetStr.contains("Button")) {
               return "Click Button";
            } else if (!packetStr.contains("action=") && !packetStr.contains("Action")) {
               return null;
            } else if (packetStr.contains("DROP_ITEM") || packetStr.contains("drop")) {
               return "Drop Item";
            } else if (!packetStr.contains("DROP_ALL") && !packetStr.contains("drop_all")) {
               return !packetStr.contains("SWAP") && !packetStr.contains("swap") ? "Player Action" : "Swap Hands";
            } else {
               return "Drop Stack";
            }
         } else {
            return !packetStr.contains("button=") && !packetStr.contains("Button") ? "Slot Action" : "Click Slot";
         }
      } catch (Exception e) {
         return null;
      }
   }
}
