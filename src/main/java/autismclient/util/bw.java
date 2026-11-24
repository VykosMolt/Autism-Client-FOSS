package autismclient.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.CommonPacketTypes;
import net.minecraft.network.protocol.configuration.ConfigurationPacketTypes;
import net.minecraft.network.protocol.cookie.CookiePacketTypes;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.protocol.game.ServerboundPunchPacket;
import net.minecraft.network.protocol.handshake.HandshakePacketTypes;
import net.minecraft.network.protocol.login.LoginPacketTypes;
import net.minecraft.network.protocol.ping.PingPacketTypes;
import net.minecraft.network.protocol.status.StatusPacketTypes;

public final class bw {
   private static final Class<?>[] init_ClassArr = new Class[]{
      HandshakePacketTypes.class,
      StatusPacketTypes.class,
      LoginPacketTypes.class,
      ConfigurationPacketTypes.class,
      CommonPacketTypes.class,
      CookiePacketTypes.class,
      PingPacketTypes.class,
      GamePacketTypes.class
   };
   private static final Map<Class<? extends Packet<?>>, String> init_Map;
   private static final Map<Class<? extends Packet<?>>, String> load;
   private static final Map<String, Class<? extends Packet<?>>> read;
   public static final Set<Class<? extends Packet<?>>> init_Set;

   private bw() {
   }

   private static void init_V_Class(
      Class<?> holder,
      Map<Class<? extends Packet<?>>, String> s2cPackets,
      Map<Class<? extends Packet<?>>, String> c2sPackets,
      Map<String, Class<? extends Packet<?>>> packetsByName,
      Set<String> ambiguousAliases
   ) {
      for (Field field : holder.getDeclaredFields()) {
         int modifiers = field.getModifiers();
         if (Modifier.isStatic(modifiers) && PacketType.class.isAssignableFrom(field.getType())) {
            Class<? extends Packet<?>> packetClass = init_Class_Field(field);
            if (packetClass != null) {
               String fieldName = field.getName();
               Map<Class<? extends Packet<?>>, String> target;
               if (fieldName.startsWith("CLIENTBOUND_")) {
                  target = s2cPackets;
               } else {
                  if (!fieldName.startsWith("SERVERBOUND_")) {
                     continue;
                  }

                  target = c2sPackets;
               }

               String primaryName = load(packetClass);
               target.putIfAbsent(packetClass, primaryName);
               init_V_Map(
                  packetsByName,
                  ambiguousAliases,
                  packetClass,
                  primaryName,
                  packetClass.getSimpleName(),
                  packetClass.getName(),
                  fieldName,
                  init_Str_Str(fieldName),
                  "minecraft:" + init_Str_Str(fieldName)
               );
            }
         }
      }
   }

   private static Class<? extends Packet<?>> init_Class_Field(Field field) {
      if (!(field.getGenericType() instanceof ParameterizedType parameterizedType)) {
         return null;
      } else {
         Type[] args = parameterizedType.getActualTypeArguments();
         return (Class<? extends Packet<?>>)(args.length == 1 && args[0] instanceof Class<?> packetClass && Packet.class.isAssignableFrom(packetClass)
            ? packetClass
            : null);
      }
   }

   private static String load(Class<?> packetClass) {
      Class<?> enclosing = packetClass.getEnclosingClass();
      return enclosing != null && packetClass.getPackageName() != null && packetClass.getPackageName().equals(enclosing.getPackageName())
         ? load(enclosing) + "." + packetClass.getSimpleName()
         : packetClass.getSimpleName();
   }

   private static String init_Str_Str(String fieldName) {
      String name = fieldName;
      if (name.startsWith("CLIENTBOUND_")) {
         name = name.substring("CLIENTBOUND_".length());
      } else if (name.startsWith("SERVERBOUND_")) {
         name = name.substring("SERVERBOUND_".length());
      }

      return name.toLowerCase(Locale.ROOT);
   }

   private static void init_V_Map(
      Map<String, Class<? extends Packet<?>>> target, Set<String> ambiguousAliases, Class<? extends Packet<?>> packetClass, String... aliases
   ) {
      for (String alias : aliases) {
         if (alias != null) {
            String trimmed = alias.trim();
            if (!trimmed.isEmpty()) {
               init_V_Map_2(target, ambiguousAliases, trimmed, packetClass);
               init_V_Map_2(target, ambiguousAliases, load(trimmed), packetClass);
               int namespaceSeparator = trimmed.indexOf(58);
               if (namespaceSeparator >= 0 && namespaceSeparator + 1 < trimmed.length()) {
                  String pathOnly = trimmed.substring(namespaceSeparator + 1);
                  init_V_Map_2(target, ambiguousAliases, pathOnly, packetClass);
                  init_V_Map_2(target, ambiguousAliases, load(pathOnly), packetClass);
               }
            }
         }
      }
   }

   private static void init_V_Map_2(
      Map<String, Class<? extends Packet<?>>> target, Set<String> ambiguousAliases, String alias, Class<? extends Packet<?>> packetClass
   ) {
      if (!alias.isEmpty() && !ambiguousAliases.contains(alias)) {
         Class<? extends Packet<?>> existing = target.get(alias);
         if (existing != null && existing != packetClass) {
            target.remove(alias);
            ambiguousAliases.add(alias);
         } else {
            target.put(alias, packetClass);
         }
      }
   }

   private static String load(String value) {
      return value.trim().replace('$', '.').replace('_', ' ').replace('-', ' ').replace('/', '.').toLowerCase(Locale.ROOT);
   }

   public static String init_Str_Class(Class<? extends Packet<?>> packetClass) {
      String name = init_Map.get(packetClass);
      return name != null ? name : load.get(packetClass);
   }

   public static Class<? extends Packet<?>> init_Class_Str(String name) {
      if (name == null) {
         return null;
      }

      String trimmed = name.trim();
      if (trimmed.isEmpty()) {
         return null;
      }

      Class<? extends Packet<?>> packet = read.get(trimmed);
      if (packet != null) {
         return packet;
      }

      packet = read.get(load(trimmed));
      if (packet != null) {
         return packet;
      }

      try {
         Class<?> direct = Class.forName(trimmed);
         if (Packet.class.isAssignableFrom(direct)) {
            return (Class<? extends Packet<?>>)direct;
         }
      } catch (ClassNotFoundException var4) {
      }

      return null;
   }

   public static Set<Class<? extends Packet<?>>> init_Set() {
      return init_Map.keySet();
   }

   public static Set<Class<? extends Packet<?>>> load() {
      return load.keySet();
   }

   static {
      LinkedHashMap<Class<? extends Packet<?>>, String> s2cPackets = new LinkedHashMap<>();
      LinkedHashMap<Class<? extends Packet<?>>, String> c2sPackets = new LinkedHashMap<>();
      LinkedHashMap<String, Class<? extends Packet<?>>> packetsByName = new LinkedHashMap<>();
      LinkedHashSet<String> ambiguousAliases = new LinkedHashSet<>();

      for (Class<?> holder : init_ClassArr) {
         init_V_Class(holder, s2cPackets, c2sPackets, packetsByName, ambiguousAliases);
      }

      init_V_Map(packetsByName, ambiguousAliases, ServerboundPunchPacket.class, "CompatSwingPacket", "HandSwingC2SPacket", "HandSwingC2S");

      LinkedHashSet<Class<? extends Packet<?>>> packets = new LinkedHashSet<>();
      packets.addAll(c2sPackets.keySet());
      packets.addAll(s2cPackets.keySet());
      init_Map = Collections.unmodifiableMap(s2cPackets);
      load = Collections.unmodifiableMap(c2sPackets);
      read = Collections.unmodifiableMap(packetsByName);
      init_Set = Collections.unmodifiableSet(packets);
   }
}
