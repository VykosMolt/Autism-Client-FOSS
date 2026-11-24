package autismclient.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class cK {
   private static final String init_Str = "/autism-inspector-mappings.tsv";
   private static final Map<String, String> init_Map = new HashMap<>();
   private static final Map<String, String> load = new HashMap<>();
   private static final Map<String, String> read = new HashMap<>();
   private static final Map<String, String> write = new HashMap<>();
   private static volatile boolean init_Z;

   private cK() {
   }

   public static String init(Class<?> type) {
      return type == null ? null : init_Str(type.getName());
   }

   public static String init_Str(String className) {
      if (className != null && !className.isBlank()) {
         init();
         return init_Map.get(className.replace('/', '.'));
      } else {
         return null;
      }
   }

   public static String load(String simpleName) {
      if (simpleName != null && !simpleName.isBlank()) {
         init();
         String alias = load.get(simpleName);
         return alias != null && !alias.isBlank() ? alias : null;
      } else {
         return null;
      }
   }

   public static String init(Method method) {
      if (method == null) {
         return null;
      }

      init();
      return read.get(init(method.getDeclaringClass().getName(), method.getName(), load(method)));
   }

   public static String init(Field field) {
      if (field == null) {
         return null;
      }

      init();
      return write.get(init(field.getDeclaringClass().getName(), field.getName(), load(field.getType())));
   }

   private static void init() {
      if (!init_Z) {
         synchronized (cK.class) {
            if (!init_Z) {
               load();
               init_Z = true;
            }
         }
      }
   }

   private static void load() {
      try {
         label66: {
            try (InputStream stream = cK.class.getResourceAsStream("/autism-inspector-mappings.tsv")) {
               if (stream != null) {
                  String line;
                  try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                     while ((line = reader.readLine()) != null) {
                        init_V(line);
                     }
                     break label66;
                  }
               }
            }

            return;
         }
      } catch (IOException var9) {
      }
   }

   private static void init_V(String line) {
      if (line != null) {
         String trimmed = line.trim();
         if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
            String[] parts = trimmed.split("\t");
            if (parts.length >= 3) {
               switch (parts[0]) {
                  case "C":
                     String owner = parts[1];
                     String alias = parts[2];
                     if (owner.isBlank() || alias.isBlank()) {
                        return;
                     }

                     init_Map.put(owner, alias);
                     String simpleOwner = owner.substring(owner.lastIndexOf(46) + 1);
                     init(simpleOwner, alias);
                     break;
                  case "M":
                     if (parts.length < 5) {
                        return;
                     }

                     read.put(init(parts[1], parts[2], parts[3]), parts[4]);
                     break;
                  case "F":
                     if (parts.length < 5) {
                        return;
                     }

                     write.put(init(parts[1], parts[2], parts[3]), parts[4]);
               }
            }
         }
      }
   }

   private static void init(String simpleOwner, String alias) {
      if (simpleOwner != null && !simpleOwner.isBlank() && alias != null && !alias.isBlank()) {
         String existing = load.get(simpleOwner);
         if (existing == null) {
            load.put(simpleOwner, alias);
         } else if (!existing.equals(alias)) {
            load.put(simpleOwner, "");
         }
      }
   }

   private static String init(String owner, String name, String descriptor) {
      return owner + "#" + name + "#" + descriptor;
   }

   private static String load(Method method) {
      StringBuilder builder = new StringBuilder();
      builder.append('(');

      for (Class<?> parameterType : method.getParameterTypes()) {
         builder.append(load(parameterType));
      }

      builder.append(')').append(load(method.getReturnType()));
      return builder.toString();
   }

   private static String load(Class<?> type) {
      if (type.isPrimitive()) {
         if (type == void.class) {
            return "V";
         }

         if (type == boolean.class) {
            return "Z";
         }

         if (type == byte.class) {
            return "B";
         }

         if (type == char.class) {
            return "C";
         }

         if (type == short.class) {
            return "S";
         }

         if (type == int.class) {
            return "I";
         }

         if (type == long.class) {
            return "J";
         }

         if (type == float.class) {
            return "F";
         }

         if (type == double.class) {
            return "D";
         }
      }

      return type.isArray() ? type.getName().replace('.', '/') : "L" + type.getName().replace('.', '/') + ";";
   }
}
