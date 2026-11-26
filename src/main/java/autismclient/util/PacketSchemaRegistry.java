package autismclient.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class PacketSchemaRegistry {
   private static volatile Map<String, PacketSchemaRegistry.Inner_StreamUtil> init;

   private PacketSchemaRegistry() {
   }

   public static PacketSchemaRegistry.Inner_StreamUtil init(Class<?> packetClass) {
      if (packetClass == null) {
         return null;
      }

      Map<String, PacketSchemaRegistry.Inner_StreamUtil> map = init_Map();
      PacketSchemaRegistry.Inner_StreamUtil exact = map.get(packetClass.getName());
      if (exact != null) {
         return exact;
      }

      exact = map.get(packetClass.getSimpleName());
      if (exact != null) {
         return exact;
      }

      for (Class<?> parent = packetClass.getSuperclass(); parent != null && parent != Object.class; parent = parent.getSuperclass()) {
         exact = map.get(parent.getName());
         if (exact != null) {
            return exact.init(packetClass);
         }
      }

      return null;
   }

   public static int init_I() {
      return init_Map().values().stream().map(PacketSchemaRegistry.Inner_StreamUtil::load_Str).collect(Collectors.toSet()).size();
   }

   private static Map<String, PacketSchemaRegistry.Inner_StreamUtil> init_Map() {
      Map<String, PacketSchemaRegistry.Inner_StreamUtil> current = init;
      if (current != null) {
         return current;
      }

      synchronized (PacketSchemaRegistry.class) {
         current = init;
         if (current != null) {
            return current;
         }

         Map<String, PacketSchemaRegistry.Inner_StreamUtil> loaded = new LinkedHashMap<>();

         try (InputStream in = PacketSchemaRegistry.class.getResourceAsStream("/autism-packet-schemas.tsv")) {
            if (in != null) {
               String line;
               try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                  while ((line = reader.readLine()) != null) {
                     if (!line.isBlank() && !line.startsWith("#")) {
                        PacketSchemaRegistry.Inner_StreamUtil schema = init(line);
                        if (schema != null) {
                           loaded.put(schema.load_Str(), schema);
                           loaded.put(schema.init_Str(), schema);
                        }
                     }
                  }
               }
            }
         } catch (Throwable ignored) {
            loaded.clear();
         }

         init = Collections.unmodifiableMap(loaded);
         return init;
      }
   }

   private static PacketSchemaRegistry.Inner_StreamUtil init(String line) {
      String[] parts = line.split("\t", -1);
      if (parts.length < 8) {
         return null;
      }

      List<PacketSchemaRegistry.Inner_DataUtil> fields = new ArrayList<>();
      if (!parts[7].isBlank()) {
         for (String fieldRaw : parts[7].split("\\|", -1)) {
            String[] fieldParts = fieldRaw.split("~", -1);
            if (fieldParts.length >= 4) {
               fields.add(new PacketSchemaRegistry.Inner_DataUtil(fieldParts[0], fieldParts[1], fieldParts[2].toLowerCase(Locale.ROOT), Boolean.parseBoolean(fieldParts[3])));
            }
         }
      }

      return new PacketSchemaRegistry.Inner_StreamUtil(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], Boolean.parseBoolean(parts[6]), fields, false);
   }

   private static String init(String value, String fallback) {
      return value != null && !value.isBlank() ? value : fallback;
   }

   public record Inner_DataUtil(String init_Str, String load, String read, boolean init_Z) {
      public Inner_DataUtil {
         init_Str = PacketSchemaRegistry.init(init_Str, "field");
         load = PacketSchemaRegistry.init(load, "Object");
         read = PacketSchemaRegistry.init(read, "object");
      }
   }

   public record Inner_StreamUtil(
      String init_Str,
      String load_Str,
      String read,
      String write,
      String update,
      String sync,
      boolean init_Z,
      List<PacketSchemaRegistry.Inner_DataUtil> init_List,
      boolean load_Z
   ) {
      public Inner_StreamUtil {
         load_Str = PacketSchemaRegistry.init(load_Str, "unknown");
         read = PacketSchemaRegistry.init(read, "ANY");
         write = PacketSchemaRegistry.init(write, "unknown");
         update = update == null ? "" : update;
         sync = PacketSchemaRegistry.init(sync, "fallback");
         init_List = init_List == null ? List.of() : List.copyOf(init_List);
      }

      public String init_Str() {
         String name = this.init_Str == null ? "" : this.init_Str.substring(this.init_Str.lastIndexOf(46) + 1);
         return name.replace('$', '.');
      }

      public PacketSchemaRegistry.Inner_StreamUtil init(Class<?> concreteClass) {
         return concreteClass == null
            ? this
            : new PacketSchemaRegistry.Inner_StreamUtil(concreteClass.getName(), this.load_Str, this.read, this.write, this.update, this.sync, this.init_Z, this.init_List, true);
      }

      public String load_Str() {
         return this.init_Str;
      }

      public String read() {
         return this.load_Str;
      }

      public String write() {
         return this.read;
      }

      public String update() {
         return this.write;
      }

      public String sync() {
         return this.update;
      }

      public String flush() {
         return this.sync;
      }
   }
}
