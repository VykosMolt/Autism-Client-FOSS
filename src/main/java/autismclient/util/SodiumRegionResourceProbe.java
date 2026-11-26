package autismclient.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public final class SodiumRegionResourceProbe {
   private static final int init_I = 600;
   private static final AtomicInteger init_AtomicInteger = new AtomicInteger(1);
   private static int load_I;
   private static int read_I;
   private static volatile boolean init_Z;
   private static volatile boolean load_Z;
   private static final ClassValue<SodiumRegionResourceProbe.Inner_CachePool> init_ClassValue = new ClassValue<SodiumRegionResourceProbe.Inner_CachePool>() {
      protected SodiumRegionResourceProbe.Inner_CachePool computeValue(Class<?> type) {
         return this.init(type);
      }

      protected SodiumRegionResourceProbe.Inner_CachePool init(Class<?> type) {
         return new SodiumRegionResourceProbe.Inner_CachePool(SodiumRegionResourceProbe.init(type, "isTranslucent", 0));
      }
   };
   private static final ClassValue<SodiumRegionResourceProbe.Inner_StreamUtil> load_ClassValue = new ClassValue<SodiumRegionResourceProbe.Inner_StreamUtil>() {
      protected SodiumRegionResourceProbe.Inner_StreamUtil computeValue(Class<?> type) {
         return this.init(type);
      }

      protected SodiumRegionResourceProbe.Inner_StreamUtil init(Class<?> type) {
         return new SodiumRegionResourceProbe.Inner_StreamUtil(SodiumRegionResourceProbe.init(type, "iterator", 1));
      }
   };
   private static final ClassValue<SodiumRegionResourceProbe.Inner_ConfigStore> read_ClassValue = new ClassValue<SodiumRegionResourceProbe.Inner_ConfigStore>() {
      protected SodiumRegionResourceProbe.Inner_ConfigStore computeValue(Class<?> type) {
         return this.init(type);
      }

      protected SodiumRegionResourceProbe.Inner_ConfigStore init(Class<?> type) {
         return new SodiumRegionResourceProbe.Inner_ConfigStore(SodiumRegionResourceProbe.init(type, "getRegion", 0), SodiumRegionResourceProbe.init(type, "sectionsWithGeometryIterator", 1));
      }
   };
   private static final ClassValue<SodiumRegionResourceProbe.Inner_BufferHelper> write = new ClassValue<SodiumRegionResourceProbe.Inner_BufferHelper>() {
      protected SodiumRegionResourceProbe.Inner_BufferHelper computeValue(Class<?> type) {
         return this.init(type);
      }

      protected SodiumRegionResourceProbe.Inner_BufferHelper init(Class<?> type) {
         return new SodiumRegionResourceProbe.Inner_BufferHelper(SodiumRegionResourceProbe.init(type, "getStorage", 1), SodiumRegionResourceProbe.init(type, "getCachedBatch", 1), SodiumRegionResourceProbe.init(type, "getResources", 0));
      }
   };
   private static final ClassValue<SodiumRegionResourceProbe.Inner_DataUtil> update = new ClassValue<SodiumRegionResourceProbe.Inner_DataUtil>() {
      protected SodiumRegionResourceProbe.Inner_DataUtil computeValue(Class<?> type) {
         return this.init(type);
      }

      protected SodiumRegionResourceProbe.Inner_DataUtil init(Class<?> type) {
         return new SodiumRegionResourceProbe.Inner_DataUtil(SodiumRegionResourceProbe.init(type, "isEmpty", 0));
      }
   };

   private SodiumRegionResourceProbe() {
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
            SodiumRegionResourceProbe.Inner_CachePool passAccess = init_ClassValue.get(pass.getClass());
            SodiumRegionResourceProbe.Inner_StreamUtil listsAccess = load_ClassValue.get(lists.getClass());
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
                  SodiumRegionResourceProbe.Inner_ConfigStore renderAccess = read_ClassValue.get(renderList.getClass());
                  if (renderAccess.init() == null || renderAccess.load() == null) {
                     return load_Z();
                  }

                  Object region = (Object)renderAccess.init().invoke((Object)renderList);
                  if (region != null) {
                     SodiumRegionResourceProbe.Inner_BufferHelper regionAccess = write.get(region.getClass());
                     if (regionAccess.init() == null || regionAccess.load() == null || regionAccess.read() == null) {
                        return load_Z();
                     }

                     if ((Object)regionAccess.init().invoke((Object)region, (Object)pass) != null) {
                        Object batch = (Object)regionAccess.load().invoke((Object)region, (Object)pass);
                        SodiumRegionResourceProbe.Inner_DataUtil batchAccess = batch == null ? null : update.get(batch.getClass());
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
