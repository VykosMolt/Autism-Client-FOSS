package autismclient.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

final class AutismConfigSnapshotter {
   private AutismConfigSnapshotter() {
   }

   static AutismConfig snapshot(AutismConfig source) {
      return init(source, true);
   }

   static AutismConfig persistentSnapshot(AutismConfig source) {
      return init(source, false);
   }

   private static AutismConfig init(AutismConfig source, boolean applyDefaults) {
      if (source == null) {
         return new AutismConfig();
      }

      try {
         AutismConfig copy = (AutismConfig)init(source, new IdentityHashMap<>());
         if (applyDefaults) {
            copy.applyRuntimeDefaults();
         }

         return copy;
      } catch (ReflectiveOperationException e) {
         throw new IllegalStateException("Could not capture Autism config", e);
      }
   }

   private static Object init(Object value, IdentityHashMap<Object, Object> seen) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
      if (value != null && !init(value.getClass())) {
         Object prior = seen.get(value);
         if (prior != null) {
            return prior;
         }

         if (value instanceof List<?> list) {
            List<Object> copy = new ArrayList<>(list.size());
            seen.put(value, copy);

            for (Object element : list) {
               copy.add(init(element, seen));
            }

            return copy;
         } else if (!(value instanceof Map<?, ?> map)) {
            Class<?> type = value.getClass();
            if (type != AutismConfig.class && type.getEnclosingClass() != AutismConfig.class) {
               throw new IllegalStateException("Unsupported config value: " + type.getName());
            }

            Constructor<?> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object copy = constructor.newInstance();
            seen.put(value, copy);
            init(value, copy, type, seen);
            return copy;
         } else {
            LinkedHashMap copy = new LinkedHashMap(Math.max(4, map.size()));
            seen.put(value, copy);

            for (Entry<?, ?> entry : map.entrySet()) {
               copy.put(init(entry.getKey(), seen), init(entry.getValue(), seen));
            }

            return copy;
         }
      } else {
         return value;
      }
   }

   private static void init(Object source, Object target, Class<?> type, IdentityHashMap<Object, Object> seen) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
      for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
         for (Field field : current.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
               field.setAccessible(true);
               field.set(target, init(field.get(source), seen));
            }
         }
      }
   }

   private static boolean init(Class<?> type) {
      return type.isPrimitive()
         || type.isEnum()
         || type == String.class
         || type == Boolean.class
         || type == Byte.class
         || type == Short.class
         || type == Integer.class
         || type == Long.class
         || type == Float.class
         || type == Double.class
         || type == Character.class;
   }
}
