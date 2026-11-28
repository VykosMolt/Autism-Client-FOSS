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
      return copyConfig(source, true);
   }

   static AutismConfig persistentSnapshot(AutismConfig source) {
      return copyConfig(source, false);
   }

   private static AutismConfig copyConfig(AutismConfig source, boolean applyDefaults) {
      if (source == null) {
         return new AutismConfig();
      }

      try {
         AutismConfig copy = (AutismConfig)deepCopy(source, new IdentityHashMap<>());
         if (applyDefaults) {
            copy.applyRuntimeDefaults();
         }

         return copy;
      } catch (ReflectiveOperationException e) {
         throw new IllegalStateException("Could not capture Autism config", e);
      }
   }

   private static Object deepCopy(Object value, IdentityHashMap<Object, Object> seen) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
      if (value != null && !isImmutable(value.getClass())) {
         Object prior = seen.get(value);
         if (prior != null) {
            return prior;
         }

         if (value instanceof List<?> list) {
            List<Object> copy = new ArrayList<>(list.size());
            seen.put(value, copy);

            for (Object element : list) {
               copy.add(deepCopy(element, seen));
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
            copyFields(value, copy, type, seen);
            return copy;
         } else {
            LinkedHashMap copy = new LinkedHashMap(Math.max(4, map.size()));
            seen.put(value, copy);

            for (Entry<?, ?> entry : map.entrySet()) {
               copy.put(deepCopy(entry.getKey(), seen), deepCopy(entry.getValue(), seen));
            }

            return copy;
         }
      } else {
         return value;
      }
   }

   private static void copyFields(Object source, Object target, Class<?> type, IdentityHashMap<Object, Object> seen) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
      for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
         for (Field field : current.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
               field.setAccessible(true);
               field.set(target, deepCopy(field.get(source), seen));
            }
         }
      }
   }

   private static boolean isImmutable(Class<?> type) {
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
