package autismclient.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public final class dm {
   private static final int init_I = 600;
   private static final AtomicInteger init_AtomicInteger = new AtomicInteger(1);
   private static int load_I;
   private static int read_I;
   private static volatile boolean init_Z;
   private static volatile boolean load_Z;
   private static final ClassValue<dm.Inner_CachePool> init_ClassValue = new ClassValue<dm.Inner_CachePool>() {
      protected dm.Inner_CachePool computeValue(Class<?> type) {
         return this.init(type);
      }

      protected dm.Inner_CachePool init(Class<?> type) {
         return new dm.Inner_CachePool(dm.init(type, "isTranslucent", 0));
      }
   };
   private static final ClassValue<dm.Inner_StreamUtil> load_ClassValue = new ClassValue<dm.Inner_StreamUtil>() {
      protected dm.Inner_StreamUtil computeValue(Class<?> type) {
         return this.init(type);
      }

      protected dm.Inner_StreamUtil init(Class<?> type) {
         return new dm.Inner_StreamUtil(dm.init(type, "iterator", 1));
      }
   };
   private static final ClassValue<dm.Inner_ConfigStore> read_ClassValue = new ClassValue<dm.Inner_ConfigStore>() {
      protected dm.Inner_ConfigStore computeValue(Class<?> type) {
         return this.init(type);
      }

      protected dm.Inner_ConfigStore init(Class<?> type) {
         return new dm.Inner_ConfigStore(dm.init(type, "getRegion", 0), dm.init(type, "sectionsWithGeometryIterator", 1));
      }
   };
   private static final ClassValue<dm.Inner_BufferHelper> write = new ClassValue<dm.Inner_BufferHelper>() {
      protected dm.Inner_BufferHelper computeValue(Class<?> type) {
         return this.init(type);
      }

      protected dm.Inner_BufferHelper init(Class<?> type) {
         return new dm.Inner_BufferHelper(dm.init(type, "getStorage", 1), dm.init(type, "getCachedBatch", 1), dm.init(type, "getResources", 0));
      }
   };
   private static final ClassValue<dm.Inner_DataUtil> update = new ClassValue<dm.Inner_DataUtil>() {
      protected dm.Inner_DataUtil computeValue(Class<?> type) {
         return this.init(type);
      }

      protected dm.Inner_DataUtil init(Class<?> type) {
         return new dm.Inner_DataUtil(dm.init(type, "isEmpty", 0));
      }
   };

   private dm() {
   }

   public static void init_V() {
      init_AtomicInteger.incrementAndGet();
   }

   public static void load_V() {
      if (!init_Z()) {
         init_V();
      }
   }

   public static void init(boolean active) {
      if (init_Z != active) {
         init_V();
      }

      init_Z = active;
   }

   static boolean init_Z() {
      return !load_Z && (init_Z || init_AtomicInteger.get() != load_I || read_I > 0);
   }

   static void read() {
      init_Z = false;
      load_Z = false;
      load_I = init_AtomicInteger.get();
      read_I = 0;
   }

   public static boolean init(Object lists, Object pass) {
      if (load_Z) {
         return false;
      }

      int generation = init_AtomicInteger.get();
      if (generation != load_I) {
         load_I = generation;
         read_I = 600;
      } else if (!init_Z && read_I <= 0) {
         return false;
      }

      if (!init_Z) {
         read_I--;
      }

      if (lists != null && pass != null) {
         try {
            dm.Inner_CachePool passAccess = init_ClassValue.get(pass.getClass());
            dm.Inner_StreamUtil listsAccess = load_ClassValue.get(lists.getClass());
            if (passAccess.init() == null || listsAccess.init() == null) {
               return load_Z();
            }

            boolean translucent = (boolean)passAccess.init().invoke((Object)pass);
            if (!((Object)listsAccess.init().invoke((Object)lists, (boolean)translucent) instanceof Iterator<?> iterator)) {
               return false;
            }

            while (iterator.hasNext()) {
               Object renderList = iterator.next();
               if (renderList != null) {
                  dm.Inner_ConfigStore renderAccess = read_ClassValue.get(renderList.getClass());
                  if (renderAccess.init() == null || renderAccess.load() == null) {
                     return load_Z();
                  }

                  Object region = (Object)renderAccess.init().invoke((Object)renderList);
                  if (region != null) {
                     dm.Inner_BufferHelper regionAccess = write.get(region.getClass());
                     if (regionAccess.init() == null || regionAccess.load() == null || regionAccess.read() == null) {
                        return load_Z();
                     }

                     if ((Object)regionAccess.init().invoke((Object)region, (Object)pass) != null) {
                        Object batch = (Object)regionAccess.load().invoke((Object)region, (Object)pass);
                        dm.Inner_DataUtil batchAccess = batch == null ? null : update.get(batch.getClass());
                        if (batchAccess != null && batchAccess.init() == null) {
                           return load_Z();
                        }

                        boolean cachedDraws = batchAccess != null && !(boolean)batchAccess.init().invoke((Object)batch);
                        boolean listedGeometry = (Object)renderAccess.load().invoke((Object)renderList, (boolean)translucent) != null;
                        if ((cachedDraws || listedGeometry) && (Object)regionAccess.read().invoke((Object)region) == null) {
                           read_I = Math.max(read_I, 600);
                           return true;
                        }
                     }
                  }
               }
            }
         } catch (Throwable var15) {
         }

         return false;
      } else {
         return false;
      }
   }

   private static boolean load_Z() {
      load_Z = true;
      read_I = 0;
      return false;
   }

   private static MethodHandle init(Class<?> owner, String name, int parameters) {
      for (Method candidate : owner.getMethods()) {
         if (candidate.getName().equals(name) && candidate.getParameterCount() == parameters) {
            try {
               return MethodHandles.publicLookup().unreflect(candidate);
            } catch (IllegalAccessException ignored) {
               return null;
            }
         }
      }

      return null;
   }

   private record Inner_BufferHelper(MethodHandle init, MethodHandle load, MethodHandle read) {
   }

   private record Inner_CachePool(MethodHandle init) {
   }

   private record Inner_ConfigStore(MethodHandle init, MethodHandle load) {
   }

   private record Inner_DataUtil(MethodHandle init) {
   }

   private record Inner_StreamUtil(MethodHandle init) {
   }
}
