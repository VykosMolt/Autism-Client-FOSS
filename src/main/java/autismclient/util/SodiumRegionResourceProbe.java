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
   private static final ClassValue<SodiumRegionResourceProbe.TranslucencyHandle> init_ClassValue = new ClassValue<SodiumRegionResourceProbe.TranslucencyHandle>() {
      protected SodiumRegionResourceProbe.TranslucencyHandle computeValue(Class<?> type) {
         return this.init(type);
      }

      protected SodiumRegionResourceProbe.TranslucencyHandle init(Class<?> type) {
         return new SodiumRegionResourceProbe.TranslucencyHandle(SodiumRegionResourceProbe.init(type, "isTranslucent", 0));
      }
   };
   private static final ClassValue<SodiumRegionResourceProbe.IteratorHandle> load_ClassValue = new ClassValue<SodiumRegionResourceProbe.IteratorHandle>() {
      protected SodiumRegionResourceProbe.IteratorHandle computeValue(Class<?> type) {
         return this.init(type);
      }

      protected SodiumRegionResourceProbe.IteratorHandle init(Class<?> type) {
         return new SodiumRegionResourceProbe.IteratorHandle(SodiumRegionResourceProbe.init(type, "iterator", 1));
      }
   };
   private static final ClassValue<SodiumRegionResourceProbe.RenderListHandles> read_ClassValue = new ClassValue<SodiumRegionResourceProbe.RenderListHandles>() {
      protected SodiumRegionResourceProbe.RenderListHandles computeValue(Class<?> type) {
         return this.init(type);
      }

      protected SodiumRegionResourceProbe.RenderListHandles init(Class<?> type) {
         return new SodiumRegionResourceProbe.RenderListHandles(SodiumRegionResourceProbe.init(type, "getRegion", 0), SodiumRegionResourceProbe.init(type, "sectionsWithGeometryIterator", 1));
      }
   };
   private static final ClassValue<SodiumRegionResourceProbe.RegionStorageHandles> REGION_HANDLES = new ClassValue<SodiumRegionResourceProbe.RegionStorageHandles>() {
      protected SodiumRegionResourceProbe.RegionStorageHandles computeValue(Class<?> type) {
         return this.init(type);
      }

      protected SodiumRegionResourceProbe.RegionStorageHandles init(Class<?> type) {
         return new SodiumRegionResourceProbe.RegionStorageHandles(SodiumRegionResourceProbe.init(type, "getStorage", 1), SodiumRegionResourceProbe.init(type, "getCachedBatch", 1), SodiumRegionResourceProbe.init(type, "getResources", 0));
      }
   };
   private static final ClassValue<SodiumRegionResourceProbe.EmptyCheckHandle> EMPTY_CHECK_HANDLES = new ClassValue<SodiumRegionResourceProbe.EmptyCheckHandle>() {
      protected SodiumRegionResourceProbe.EmptyCheckHandle computeValue(Class<?> type) {
         return this.init(type);
      }

      protected SodiumRegionResourceProbe.EmptyCheckHandle init(Class<?> type) {
         return new SodiumRegionResourceProbe.EmptyCheckHandle(SodiumRegionResourceProbe.init(type, "isEmpty", 0));
      }
   };

   private SodiumRegionResourceProbe() {
   }

   public static void invalidate() {
      init_AtomicInteger.incrementAndGet();
   }

   public static void load_V() {
      if (!init_Z()) {
         invalidate();
      }
   }

   public static void setActive(boolean active) {
      if (init_Z != active) {
         invalidate();
      }

      init_Z = active;
   }

   static boolean init_Z() {
      return !load_Z && (init_Z || init_AtomicInteger.get() != load_I || read_I > 0);
   }

   static void resetFrame() {
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
            SodiumRegionResourceProbe.TranslucencyHandle passAccess = init_ClassValue.get(pass.getClass());
            SodiumRegionResourceProbe.IteratorHandle listsAccess = load_ClassValue.get(lists.getClass());
            if (passAccess.handle() == null || listsAccess.handle() == null) {
               return load_Z();
            }

            boolean translucent = (boolean)passAccess.handle().invoke((Object)pass);
            if (!((Object)listsAccess.handle().invoke((Object)lists, (boolean)translucent) instanceof Iterator<?> iterator)) {
               return false;
            }

            while (iterator.hasNext()) {
               Object renderList = iterator.next();
               if (renderList != null) {
                  SodiumRegionResourceProbe.RenderListHandles renderAccess = read_ClassValue.get(renderList.getClass());
                  if (renderAccess.getRegion() == null || renderAccess.sectionsWithGeometryIterator() == null) {
                     return load_Z();
                  }

                  Object region = (Object)renderAccess.getRegion().invoke((Object)renderList);
                  if (region != null) {
                     SodiumRegionResourceProbe.RegionStorageHandles regionAccess = REGION_HANDLES.get(region.getClass());
                     if (regionAccess.getStorage() == null || regionAccess.getCachedBatch() == null || regionAccess.getResources() == null) {
                        return load_Z();
                     }

                     if ((Object)regionAccess.getStorage().invoke((Object)region, (Object)pass) != null) {
                        Object batch = (Object)regionAccess.getCachedBatch().invoke((Object)region, (Object)pass);
                        SodiumRegionResourceProbe.EmptyCheckHandle batchAccess = batch == null ? null : EMPTY_CHECK_HANDLES.get(batch.getClass());
                        if (batchAccess != null && batchAccess.handle() == null) {
                           return load_Z();
                        }

                        boolean cachedDraws = batchAccess != null && !(boolean)batchAccess.handle().invoke((Object)batch);
                        boolean listedGeometry = (Object)renderAccess.sectionsWithGeometryIterator().invoke((Object)renderList, (boolean)translucent) != null;
                        if ((cachedDraws || listedGeometry) && (Object)regionAccess.getResources().invoke((Object)region) == null) {
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

   private record RegionStorageHandles(MethodHandle getStorage, MethodHandle getCachedBatch, MethodHandle getResources) {
   }

   private record TranslucencyHandle(MethodHandle handle) {
   }

   private record RenderListHandles(MethodHandle getRegion, MethodHandle sectionsWithGeometryIterator) {
   }

   private record EmptyCheckHandle(MethodHandle handle) {
   }

   private record IteratorHandle(MethodHandle handle) {
   }
}
