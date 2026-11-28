package autismclient.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public final class SodiumRegionResourceProbe {
   private static final int init_I = 600;
   private static final AtomicInteger GENERATION = new AtomicInteger(1);
   private static int seenGeneration;
   private static int probeBudget;
   private static volatile boolean forcedActive;
   private static volatile boolean disabled;
   private static final ClassValue<SodiumRegionResourceProbe.TranslucencyHandle> TRANSLUCENCY_HANDLES = new ClassValue<SodiumRegionResourceProbe.TranslucencyHandle>() {
      protected SodiumRegionResourceProbe.TranslucencyHandle computeValue(Class<?> type) {
         return this.createHandles(type);
      }

      protected SodiumRegionResourceProbe.TranslucencyHandle createHandles(Class<?> type) {
         return new SodiumRegionResourceProbe.TranslucencyHandle(SodiumRegionResourceProbe.findMethod(type, "isTranslucent", 0));
      }
   };
   private static final ClassValue<SodiumRegionResourceProbe.IteratorHandle> ITERATOR_HANDLES = new ClassValue<SodiumRegionResourceProbe.IteratorHandle>() {
      protected SodiumRegionResourceProbe.IteratorHandle computeValue(Class<?> type) {
         return this.createHandles(type);
      }

      protected SodiumRegionResourceProbe.IteratorHandle createHandles(Class<?> type) {
         return new SodiumRegionResourceProbe.IteratorHandle(SodiumRegionResourceProbe.findMethod(type, "iterator", 1));
      }
   };
   private static final ClassValue<SodiumRegionResourceProbe.RenderListHandles> RENDER_LIST_HANDLES = new ClassValue<SodiumRegionResourceProbe.RenderListHandles>() {
      protected SodiumRegionResourceProbe.RenderListHandles computeValue(Class<?> type) {
         return this.createHandles(type);
      }

      protected SodiumRegionResourceProbe.RenderListHandles createHandles(Class<?> type) {
         return new SodiumRegionResourceProbe.RenderListHandles(SodiumRegionResourceProbe.findMethod(type, "getRegion", 0), SodiumRegionResourceProbe.findMethod(type, "sectionsWithGeometryIterator", 1));
      }
   };
   private static final ClassValue<SodiumRegionResourceProbe.RegionStorageHandles> REGION_HANDLES = new ClassValue<SodiumRegionResourceProbe.RegionStorageHandles>() {
      protected SodiumRegionResourceProbe.RegionStorageHandles computeValue(Class<?> type) {
         return this.createHandles(type);
      }

      protected SodiumRegionResourceProbe.RegionStorageHandles createHandles(Class<?> type) {
         return new SodiumRegionResourceProbe.RegionStorageHandles(SodiumRegionResourceProbe.findMethod(type, "getStorage", 1), SodiumRegionResourceProbe.findMethod(type, "getCachedBatch", 1), SodiumRegionResourceProbe.findMethod(type, "getResources", 0));
      }
   };
   private static final ClassValue<SodiumRegionResourceProbe.EmptyCheckHandle> EMPTY_CHECK_HANDLES = new ClassValue<SodiumRegionResourceProbe.EmptyCheckHandle>() {
      protected SodiumRegionResourceProbe.EmptyCheckHandle computeValue(Class<?> type) {
         return this.createHandles(type);
      }

      protected SodiumRegionResourceProbe.EmptyCheckHandle createHandles(Class<?> type) {
         return new SodiumRegionResourceProbe.EmptyCheckHandle(SodiumRegionResourceProbe.findMethod(type, "isEmpty", 0));
      }
   };

   private SodiumRegionResourceProbe() {
   }

   public static void invalidate() {
      GENERATION.incrementAndGet();
   }

   public static void invalidateIfInactive() {
      if (!shouldProbe()) {
         invalidate();
      }
   }

   public static void setActive(boolean active) {
      if (forcedActive != active) {
         invalidate();
      }

      forcedActive = active;
   }

   static boolean shouldProbe() {
      return !disabled && (forcedActive || GENERATION.get() != seenGeneration || probeBudget > 0);
   }

   static void resetFrame() {
      forcedActive = false;
      disabled = false;
      seenGeneration = GENERATION.get();
      probeBudget = 0;
   }

   public static boolean init(Object lists, Object pass) {
      if (disabled) {
         return false;
      }

      int generation = GENERATION.get();
      if (generation != seenGeneration) {
         seenGeneration = generation;
         probeBudget = 600;
      } else if (!forcedActive && probeBudget <= 0) {
         return false;
      }

      if (!forcedActive) {
         probeBudget--;
      }

      if (lists != null && pass != null) {
         try {
            SodiumRegionResourceProbe.TranslucencyHandle passAccess = TRANSLUCENCY_HANDLES.get(pass.getClass());
            SodiumRegionResourceProbe.IteratorHandle listsAccess = ITERATOR_HANDLES.get(lists.getClass());
            if (passAccess.handle() == null || listsAccess.handle() == null) {
               return disableProbe();
            }

            boolean translucent = (boolean)passAccess.handle().invoke((Object)pass);
            if (!((Object)listsAccess.handle().invoke((Object)lists, (boolean)translucent) instanceof Iterator<?> iterator)) {
               return false;
            }

            while (iterator.hasNext()) {
               Object renderList = iterator.next();
               if (renderList != null) {
                  SodiumRegionResourceProbe.RenderListHandles renderAccess = RENDER_LIST_HANDLES.get(renderList.getClass());
                  if (renderAccess.getRegion() == null || renderAccess.sectionsWithGeometryIterator() == null) {
                     return disableProbe();
                  }

                  Object region = (Object)renderAccess.getRegion().invoke((Object)renderList);
                  if (region != null) {
                     SodiumRegionResourceProbe.RegionStorageHandles regionAccess = REGION_HANDLES.get(region.getClass());
                     if (regionAccess.getStorage() == null || regionAccess.getCachedBatch() == null || regionAccess.getResources() == null) {
                        return disableProbe();
                     }

                     if ((Object)regionAccess.getStorage().invoke((Object)region, (Object)pass) != null) {
                        Object batch = (Object)regionAccess.getCachedBatch().invoke((Object)region, (Object)pass);
                        SodiumRegionResourceProbe.EmptyCheckHandle batchAccess = batch == null ? null : EMPTY_CHECK_HANDLES.get(batch.getClass());
                        if (batchAccess != null && batchAccess.handle() == null) {
                           return disableProbe();
                        }

                        boolean cachedDraws = batchAccess != null && !(boolean)batchAccess.handle().invoke((Object)batch);
                        boolean listedGeometry = (Object)renderAccess.sectionsWithGeometryIterator().invoke((Object)renderList, (boolean)translucent) != null;
                        if ((cachedDraws || listedGeometry) && (Object)regionAccess.getResources().invoke((Object)region) == null) {
                           probeBudget = Math.max(probeBudget, 600);
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

   private static boolean disableProbe() {
      disabled = true;
      probeBudget = 0;
      return false;
   }

   private static MethodHandle findMethod(Class<?> owner, String name, int parameters) {
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
