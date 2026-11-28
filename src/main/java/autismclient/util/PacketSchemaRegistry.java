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
   private static volatile Map<String, PacketSchemaRegistry.PacketSchema> schemas;

   private PacketSchemaRegistry() {
   }

   public static PacketSchemaRegistry.PacketSchema schemaFor(Class<?> packetClass) {
      if (packetClass == null) {
         return null;
      }

      Map<String, PacketSchemaRegistry.PacketSchema> map = init_Map();
      PacketSchemaRegistry.PacketSchema exact = map.get(packetClass.getName());
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
            return exact.forClass(packetClass);
         }
      }

      return null;
   }

   public static int schemaClassCount() {
      return init_Map().values().stream().map(PacketSchemaRegistry.PacketSchema::load_Str).collect(Collectors.toSet()).size();
   }

   private static Map<String, PacketSchemaRegistry.PacketSchema> init_Map() {
      Map<String, PacketSchemaRegistry.PacketSchema> current = schemas;
      if (current != null) {
         return current;
      }

      synchronized (PacketSchemaRegistry.class) {
         current = schemas;
         if (current != null) {
            return current;
         }

         Map<String, PacketSchemaRegistry.PacketSchema> loaded = new LinkedHashMap<>();

         try (InputStream in = PacketSchemaRegistry.class.getResourceAsStream("/autism-packet-schemas.tsv")) {
            if (in != null) {
               String line;
               try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                  while ((line = reader.readLine()) != null) {
                     if (!line.isBlank() && !line.startsWith("#")) {
                        PacketSchemaRegistry.PacketSchema schema = parseLine(line);
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

         schemas = Collections.unmodifiableMap(loaded);
         return schemas;
      }
   }

   private static PacketSchemaRegistry.PacketSchema parseLine(String line) {
      String[] parts = line.split("\t", -1);
      if (parts.length < 8) {
         return null;
      }

      List<PacketSchemaRegistry.FieldSchema> fields = new ArrayList<>();
      if (!parts[7].isBlank()) {
         for (String fieldRaw : parts[7].split("\\|", -1)) {
            String[] fieldParts = fieldRaw.split("~", -1);
            if (fieldParts.length >= 4) {
               fields.add(new PacketSchemaRegistry.FieldSchema(fieldParts[0], fieldParts[1], fieldParts[2].toLowerCase(Locale.ROOT), Boolean.parseBoolean(fieldParts[3])));
            }
         }
      }

      return new PacketSchemaRegistry.PacketSchema(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], Boolean.parseBoolean(parts[6]), fields, false);
   }

   private static String orDefault(String value, String fallback) {
      return value != null && !value.isBlank() ? value : fallback;
   }

   public record FieldSchema(String name, String load, String read, boolean editableCandidate) {
      public FieldSchema {
         name = PacketSchemaRegistry.orDefault(name, "field");
         load = PacketSchemaRegistry.orDefault(load, "Object");
         read = PacketSchemaRegistry.orDefault(read, "object");
      }
   }

   public record PacketSchema(
      String init_Str,
      String load_Str,
      String read,
      String write,
      String update,
      String sync,
      boolean complete,
      List<PacketSchemaRegistry.FieldSchema> fields,
      boolean inherited
   ) {
      public PacketSchema {
         load_Str = PacketSchemaRegistry.orDefault(load_Str, "unknown");
         read = PacketSchemaRegistry.orDefault(read, "ANY");
         write = PacketSchemaRegistry.orDefault(write, "unknown");
         update = update == null ? "" : update;
         sync = PacketSchemaRegistry.orDefault(sync, "fallback");
         fields = fields == null ? List.of() : List.copyOf(fields);
      }

      public String init_Str() {
         String name = this.init_Str == null ? "" : this.init_Str.substring(this.init_Str.lastIndexOf(46) + 1);
         return name.replace('$', '.');
      }

      public PacketSchemaRegistry.PacketSchema forClass(Class<?> concreteClass) {
         return concreteClass == null
            ? this
            : new PacketSchemaRegistry.PacketSchema(concreteClass.getName(), this.load_Str, this.read, this.write, this.update, this.sync, this.complete, this.fields, true);
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
