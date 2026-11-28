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

public final class InspectorMappingsIndex {
   private static final String RESOURCE_PATH = "/autism-inspector-mappings.tsv";
   private static final Map<String, String> classAliases = new HashMap<>();
   private static final Map<String, String> simpleAliases = new HashMap<>();
   private static final Map<String, String> methodAliases = new HashMap<>();
   private static final Map<String, String> fieldAliases = new HashMap<>();
   private static volatile boolean loaded;

   private InspectorMappingsIndex() {
   }

   public static String init(Class<?> type) {
      return type == null ? null : aliasForClass(type.getName());
   }

   public static String aliasForClass(String className) {
      if (className != null && !className.isBlank()) {
         ensureLoaded();
         return classAliases.get(className.replace('/', '.'));
      } else {
         return null;
      }
   }

   public static String load(String simpleName) {
      if (simpleName != null && !simpleName.isBlank()) {
         ensureLoaded();
         String alias = simpleAliases.get(simpleName);
         return alias != null && !alias.isBlank() ? alias : null;
      } else {
         return null;
      }
   }

   public static String init(Method method) {
      if (method == null) {
         return null;
      }

      ensureLoaded();
      return methodAliases.get(memberKey(method.getDeclaringClass().getName(), method.getName(), methodDescriptor(method)));
   }

   public static String init(Field field) {
      if (field == null) {
         return null;
      }

      ensureLoaded();
      return fieldAliases.get(memberKey(field.getDeclaringClass().getName(), field.getName(), typeDescriptor(field.getType())));
   }

   private static void ensureLoaded() {
      if (!loaded) {
         synchronized (InspectorMappingsIndex.class) {
            if (!loaded) {
               loadMappings();
               loaded = true;
            }
         }
      }
   }

   private static void loadMappings() {
      try {
         label66: {
            try (InputStream stream = InspectorMappingsIndex.class.getResourceAsStream("/autism-inspector-mappings.tsv")) {
               if (stream != null) {
                  String line;
                  try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                     while ((line = reader.readLine()) != null) {
                        parseLine(line);
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

   private static void parseLine(String line) {
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

                     classAliases.put(owner, alias);
                     String simpleOwner = owner.substring(owner.lastIndexOf(46) + 1);
                     putSimpleAlias(simpleOwner, alias);
                     break;
                  case "M":
                     if (parts.length < 5) {
                        return;
                     }

                     methodAliases.put(memberKey(parts[1], parts[2], parts[3]), parts[4]);
                     break;
                  case "F":
                     if (parts.length < 5) {
                        return;
                     }

                     fieldAliases.put(memberKey(parts[1], parts[2], parts[3]), parts[4]);
               }
            }
         }
      }
   }

   private static void putSimpleAlias(String simpleOwner, String alias) {
      if (simpleOwner != null && !simpleOwner.isBlank() && alias != null && !alias.isBlank()) {
         String existing = simpleAliases.get(simpleOwner);
         if (existing == null) {
            simpleAliases.put(simpleOwner, alias);
         } else if (!existing.equals(alias)) {
            simpleAliases.put(simpleOwner, "");
         }
      }
   }

   private static String memberKey(String owner, String name, String descriptor) {
      return owner + "#" + name + "#" + descriptor;
   }

   private static String methodDescriptor(Method method) {
      StringBuilder builder = new StringBuilder();
      builder.append('(');

      for (Class<?> parameterType : method.getParameterTypes()) {
         builder.append(typeDescriptor(parameterType));
      }

      builder.append(')').append(typeDescriptor(method.getReturnType()));
      return builder.toString();
   }

   private static String typeDescriptor(Class<?> type) {
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
