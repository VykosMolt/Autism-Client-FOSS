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
   private static final ConcurrentMap<String, String> nameCache = new ConcurrentHashMap<>();
   private static final ConcurrentMap<Class<?>, String> s2cClassNames = new ConcurrentHashMap<>();
   private static final ConcurrentMap<Class<?>, String> c2sClassNames = new ConcurrentHashMap<>();
   private static final ConcurrentMap<String, String> normalizedCache = new ConcurrentHashMap<>();
   private static final Map<String, String> ALIASES = buildAliases();

   private static String stripPacketSuffix(String name) {
      return name != null && name.endsWith("Packet") ? name.substring(0, name.length() - 6) : name;
   }

   public static String nameOf(Packet<?> packet) {
      Class<? extends Packet<?>> packetClass = (Class<? extends Packet<?>>)packet.getClass();
      String cached = NAME_CACHE.get(packetClass);
      if (cached != null) {
         return cached;
      }

      String name = registryName(packetClass);
      if (name != null) {
         return cacheDisplayName(packetClass, stripPacketSuffix(name));
      }

      String typeName = packetTypeName(packet);
      if (typeName != null) {
         return cacheDisplayName(packetClass, stripPacketSuffix(typeName));
      }

      String inspected = inspectSlotPacket(packet, packetClass.getSimpleName());
      if (inspected != null) {
         return cacheDisplayName(packetClass, inspected);
      }

      String readable = readableName(packetClass);
      return cacheDisplayName(packetClass, readable != null ? readable : "Unknown Packet");
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

      String cacheKey = cacheKey(packetClass, direction);
      String cached = nameCache.get(cacheKey);
      if (cached != null) {
         return cacheClassName(classCache, packetClass, cached);
      }

      String name = registryNameFor(packetClass, direction);
      if (name != null) {
         return cacheClassName(classCache, packetClass, cacheKeyName(cacheKey, stripPacketSuffix(name)));
      }

      String typeName = packetTypeNameFor(packet, direction);
      if (typeName != null) {
         return cacheClassName(classCache, packetClass, cacheKeyName(cacheKey, stripPacketSuffix(typeName)));
      }

      String inspected = inspectSlotPacket(packet, packetClass.getSimpleName());
      if (inspected != null) {
         return cacheClassName(classCache, packetClass, cacheKeyName(cacheKey, inspected));
      }

      String readable = readableName(packetClass);
      return cacheClassName(classCache, packetClass, cacheKeyName(cacheKey, readable != null ? readable : "Unknown Packet"));
   }

   private static ConcurrentMap<Class<?>, String> cacheFor(String direction) {
      if ("S2C".equalsIgnoreCase(direction)) {
         return s2cClassNames;
      } else {
         return "C2S".equalsIgnoreCase(direction) ? c2sClassNames : null;
      }
   }

   private static String cacheClassName(ConcurrentMap<Class<?>, String> classCache, Class<?> packetClass, String value) {
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

      String name = registryName(packetClass);
      if (name != null) {
         return cacheDisplayName(packetClass, stripPacketSuffix(name));
      }

      String readable = readableName(packetClass);
      return cacheDisplayName(packetClass, readable != null ? readable : "Unknown Packet");
   }

   private static String cacheDisplayName(Class<?> packetClass, String value) {
      String safe = value == null ? "Unknown Packet" : value;
      if (packetClass != null) {
         NAME_CACHE.putIfAbsent(packetClass, safe);
      }

      return safe;
   }

   private static String cacheKeyName(String key, String value) {
      String safe = value == null ? "Unknown Packet" : value;
      if (key != null) {
         nameCache.putIfAbsent(key, safe);
      }

      return safe;
   }

   private static String cacheKey(Class<?> packetClass, String direction) {
      String dir = direction == null ? "" : direction.toUpperCase(Locale.ROOT);
      return dir + "|" + (packetClass == null ? "" : packetClass.getName());
   }

   public static String friendlyName(String name) {
      if (name != null && !name.isEmpty()) {
         Class<? extends Packet<?>> packetClass = PacketTypeRegistry.resolvePacketClass(name);
         if (packetClass != null) {
            String resolved = registryName(packetClass);
            return resolved != null ? stripPacketSuffix(resolved) : "Unknown Packet";
         }

         String resolved = resolveText(name);
         if (resolved != null) {
            return stripPacketSuffix(resolved);
         }

         String readable = readableFromRaw(name);
         return readable != null ? readable : "Unknown Packet";
      } else {
         return "";
      }
   }

   private static String registryName(Class<? extends Packet<?>> packetClass) {
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

      String byText = resolveText(packetClass.getName());
      return byText != null ? byText : resolveText(packetClass.getCanonicalName());
   }

   private static String registryNameFor(Class<? extends Packet<?>> packetClass, String direction) {
      if (packetClass == null) {
         return null;
      }

      String direct = registeredNameFor(packetClass, direction);
      if (direct != null) {
         return direct;
      }

      String byText = resolveTextFor(packetClass.getName(), direction);
      return byText != null ? byText : resolveTextFor(packetClass.getCanonicalName(), direction);
   }

   private static String packetTypeName(Packet<?> packet) {
      if (packet == null) {
         return null;
      }

      try {
         Object packetType = packet.getClass().getMethod("getPacketType").invoke(packet);
         return packetType == null ? null : resolveText(packetType.toString());
      } catch (Throwable ignored) {
         return null;
      }
   }

   private static String packetTypeNameFor(Packet<?> packet, String direction) {
      if (packet == null) {
         return null;
      }

      try {
         Object packetType = packet.getClass().getMethod("getPacketType").invoke(packet);
         return packetType == null ? null : resolveTextFor(packetType.toString(), direction);
      } catch (Throwable ignored) {
         return null;
      }
   }

   private static String resolveText(String text) {
      if (text != null && !text.isEmpty()) {
         String normalizedText = normalizeCached(text);
         if (normalizedText.isEmpty()) {
            return null;
         }

         String manual = ALIASES.get(normalizedText);
         if (manual != null) {
            return manual;
         }

         for (Class<? extends Packet<?>> registeredClass : PacketTypeRegistry.ALL_PACKETS) {
            String registeredName = PacketTypeRegistry.getPacketName(registeredClass);
            if (registeredName != null && matchesRegistered(normalizedText, registeredName, registeredClass)) {
               return registeredName;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static String resolveTextFor(String text, String direction) {
      if (text != null && !text.isEmpty()) {
         String normalizedText = normalizeCached(text);
         if (normalizedText.isEmpty()) {
            return null;
         }

         String manual = ALIASES.get(normalizedText);
         if (manual != null) {
            return manual;
         }

         for (Class<? extends Packet<?>> registeredClass : registeredClasses(direction)) {
            String registeredName = PacketTypeRegistry.getPacketName(registeredClass);
            if (registeredName != null && matchesRegistered(normalizedText, registeredName, registeredClass)) {
               return registeredName;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static String registeredNameFor(Class<? extends Packet<?>> packetClass, String direction) {
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

   private static boolean matchesRegistered(String normalizedText, String registeredName, Class<? extends Packet<?>> registeredClass) {
      String normalizedRegistered = normalizeCached(registeredName);
      if (normalizedRegistered.equals(normalizedText)) {
         return true;
      }

      String strippedRegistered = normalizeCached(stripPacketSuffix(registeredName));
      if (strippedRegistered.equals(normalizedText)) {
         return true;
      }

      String className = normalizeCached(registeredClass.getName());
      if (!className.equals(normalizedText) && !normalizedText.endsWith(className)) {
         String simpleName = normalizeCached(registeredClass.getSimpleName());
         if (!simpleName.equals(normalizedText) && !normalizedText.endsWith(simpleName)) {
            String canonicalName = normalizeCached(registeredClass.getCanonicalName());
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
      aliases.put(normalizeCached(canonicalName), canonicalName);
      aliases.put(normalizeCached(stripPacketSuffix(canonicalName)), canonicalName);

      for (String variant : variants) {
         if (variant != null && !variant.isEmpty()) {
            aliases.put(normalizeCached(variant), canonicalName);
         }
      }
   }

   private static String readableName(Class<?> packetClass) {
      if (packetClass == null) {
         return null;
      }

      String canonical = readableFromRaw(packetClass.getCanonicalName());
      return canonical != null ? canonical : readableFromRaw(packetClass.getName());
   }

   private static String readableFromRaw(String rawName) {
      if (rawName != null && !rawName.isEmpty()) {
         int lastDot = rawName.lastIndexOf(46);
         String shortName = lastDot >= 0 ? rawName.substring(lastDot + 1) : rawName;
         shortName = shortName.replace('$', '.');
         return isAnonymousClassName(shortName) ? null : stripPacketSuffix(shortName);
      } else {
         return null;
      }
   }

   private static boolean isAnonymousClassName(String name) {
      String normalized = normalizeCached(name);
      return normalized.startsWith("class") && normalized.length() > 5;
   }

   public static String normalizeCached(String value) {
      if (value != null && !value.isEmpty()) {
         if (normalizedCache.size() > 4096) {
            normalizedCache.clear();
         }

         return normalizedCache.computeIfAbsent(value, PacketNameResolver::normalize);
      } else {
         return "";
      }
   }

   private static String normalize(String value) {
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

   private static String inspectSlotPacket(Packet<?> packet, String className) {
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
