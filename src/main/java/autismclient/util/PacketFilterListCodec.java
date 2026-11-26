package autismclient.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.network.protocol.Packet;

public final class PacketFilterListCodec {
   private PacketFilterListCodec() {
   }

   public static String init_Str(String value) {
      return value == null ? "" : value.replace(',', '|');
   }

   public static boolean init_Z(String optionId) {
      return optionId != null && optionId.toLowerCase(Locale.ROOT).contains("c2s");
   }

   public static Set<Class<? extends Packet<?>>> init_Set(String value, boolean c2s) {
      Set<Class<? extends Packet<?>>> packets = new LinkedHashSet<>();
      Set<Class<? extends Packet<?>>> pool = c2s ? PacketTypeRegistry.load() : PacketTypeRegistry.init_Set();

      for (String token : init_List(value)) {
         Class<? extends Packet<?>> packetClass = init(token, pool);
         if (packetClass != null) {
            packets.add(packetClass);
         }
      }

      return packets;
   }

   public static List<String> init_List(String value, boolean c2s) {
      List<String> invalid = new ArrayList<>();
      Set<Class<? extends Packet<?>>> pool = c2s ? PacketTypeRegistry.load() : PacketTypeRegistry.init_Set();

      for (String token : init_List(value)) {
         if (init(token, pool) == null) {
            invalid.add(token);
         }
      }

      return invalid;
   }

   public static String init(Collection<Class<? extends Packet<?>>> packets) {
      if (packets != null && !packets.isEmpty()) {
         List<String> names = new ArrayList<>();

         for (Class<? extends Packet<?>> packetClass : packets) {
            String name = PacketTypeRegistry.init_Str_Class(packetClass);
            names.add(name == null ? packetClass.getName() : name);
         }

         return String.join("|", names);
      } else {
         return "";
      }
   }

   public static String init_Str(String value, boolean c2s) {
      return init(init_Set(value, c2s));
   }

   private static List<String> init_List(String value) {
      List<String> out = new ArrayList<>();
      String normalized = init_Str(value);
      if (normalized.isBlank()) {
         return out;
      }

      for (String raw : normalized.split("\\|")) {
         String token = raw.trim();
         if (!token.isEmpty()) {
            out.add(token);
         }
      }

      return out;
   }

   private static Class<? extends Packet<?>> init(String token, Set<Class<? extends Packet<?>>> pool) {
      Class<? extends Packet<?>> packetClass = PacketTypeRegistry.init_Class_Str(token);
      if (packetClass != null && pool.contains(packetClass)) {
         return packetClass;
      }

      String friendly = PacketNameResolver.init_Str_Str(token);
      packetClass = PacketTypeRegistry.init_Class_Str(friendly);
      if (packetClass != null && pool.contains(packetClass)) {
         return packetClass;
      }

      for (Class<? extends Packet<?>> candidate : pool) {
         String registryName = PacketTypeRegistry.init_Str_Class(candidate);
         if (init(token, registryName, candidate)) {
            return candidate;
         }
      }

      return null;
   }

   private static boolean init(String token, String registryName, Class<? extends Packet<?>> candidate) {
      String needle = load(token);
      if (needle.isEmpty()) {
         return false;
      }

      String registry = load(registryName);
      String registryNoSuffix = registry.endsWith("packet") ? registry.substring(0, registry.length() - 6) : registry;
      String simple = load(candidate.getSimpleName());
      String full = load(candidate.getName());
      String friendly = load(PacketNameResolver.init_Str_Class(candidate));
      return needle.equals(registry)
         || needle.equals(registryNoSuffix)
         || needle.equals(simple)
         || needle.equals(full)
         || needle.equals(friendly)
         || full.endsWith(needle);
   }

   private static String load(String value) {
      return value == null ? "" : value.trim().replace(" ", "").replace("_", "").replace("-", "").toLowerCase(Locale.ROOT);
   }
}
