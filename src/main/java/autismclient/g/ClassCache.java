package autismclient.g;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import net.fabricmc.fabric.impl.resource.pack.ModNioPackResources;
import net.minecraft.locale.Language;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.resources.ResourceManager;

public final class ClassCache {
   private static final ThreadLocal<ClassCache.Inner_DataUtil> init_ThreadLocal = new ThreadLocal<>();
   private final ResourceManager init_ResourceManager;
   private final Map<String, ClassCache.Inner_BufferHelper> init_Map;
   private final Map<String, String> load;

   private ClassCache(ResourceManager resourceManager, Map<String, ClassCache.Inner_BufferHelper> entries, Map<String, String> vanillaEntries) {
      this.init_ResourceManager = resourceManager;
      this.init_Map = entries;
      this.load = vanillaEntries;
   }

   public static String init(Language language, String key, String fallback) {
      return init(language, key, fallback, null, false);
   }

   public static String init(Language language, String key, String fallback, BufferHelper.Inner_StreamUtil origin) {
      return init(language, key, fallback, origin, true);
   }

   private static String init(Language language, String key, String fallback, BufferHelper.Inner_StreamUtil origin, boolean connectionBound) {
      if (language instanceof DataUtil access) {
         ClassCache snapshot = access.autism$getTranslationSnapshot();
         if (snapshot != null) {
            ClassCache.Inner_BufferHelper sourced = snapshot.init_Map.get(key);
            if (sourced != null) {
               ClassCache.Inner_StreamUtil entry = sourced.init_ClassCache_StreamU();
               if (entry.init_ClassCache_CachePo() == ClassCache.Inner_CachePool.VANILLA) {
                  return entry.init_Str();
               }

               if (sourced.init_PackResources() != null
                  && sourced.init_Str() != null
                  && (
                     connectionBound
                        ? ArrayUtil.init(snapshot.init_ResourceManager, sourced.init_PackResources(), sourced.init_Str(), origin)
                        : ArrayUtil.init(snapshot.init_ResourceManager, sourced.init_PackResources(), sourced.init_Str())
                  )) {
                  return entry.init_Str();
               }
            }

            if (connectionBound) {
               String vanilla = snapshot.load.get(key);
               if (vanilla != null) {
                  return vanilla;
               }
            }
         }
      }

      return fallback != null ? fallback : key;
   }

   private record Inner_BufferHelper(ClassCache.Inner_StreamUtil init_ClassCache_StreamU, PackResources init_PackResources, String init_Str) {
      private Inner_BufferHelper {
         Objects.requireNonNull(init_ClassCache_StreamU, "entry");
      }
   }

   public enum Inner_CachePool {
      VANILLA,
      MOD,
      OTHER;
   }

   public static final class Inner_DataUtil implements AutoCloseable {
      private final ResourceManager init_ResourceManager;
      private final Map<String, ClassCache.Inner_BufferHelper> init_Map = new HashMap<>();
      private final Map<String, String> load = new HashMap<>();
      private final ClassCache.Inner_DataUtil init_ClassCache_DataUti;
      private boolean init_Z;

      private Inner_DataUtil(ResourceManager resourceManager, ClassCache.Inner_DataUtil previous) {
         this.init_ResourceManager = Objects.requireNonNull(resourceManager, "resourceManager");
         this.init_ClassCache_DataUti = previous;
      }

      public static ClassCache.Inner_DataUtil init_ClassCache_DataUti_ResourceManager(ResourceManager resourceManager) {
         ClassCache.Inner_DataUtil builder = new ClassCache.Inner_DataUtil(resourceManager, ClassCache.init_ThreadLocal.get());
         ClassCache.init_ThreadLocal.set(builder);
         return builder;
      }

      public static ClassCache.Inner_DataUtil init_ClassCache_DataUti() {
         return ClassCache.init_ThreadLocal.get();
      }

      public void init(PackResources source, String sourcePackId, String key, String value) {
         Objects.requireNonNull(source, "source");
         Objects.requireNonNull(key, "key");
         Objects.requireNonNull(value, "value");
         if (source instanceof VanillaPackResources) {
            this.init(ClassCache.Inner_CachePool.VANILLA, key, value);
         } else if (ArrayUtil.init(this.init_ResourceManager, source, sourcePackId)) {
            this.init(init_ClassCache_CachePo_PackResources(source), key, value, source, sourcePackId);
         }
      }

      void init(ClassCache.Inner_CachePool origin, String key, String value) {
         this.init(origin, key, value, null, null);
      }

      private void init(ClassCache.Inner_CachePool origin, String key, String value, PackResources source, String sourcePackId) {
         Objects.requireNonNull(origin, "origin");
         Objects.requireNonNull(key, "key");
         Objects.requireNonNull(value, "value");
         if (origin == ClassCache.Inner_CachePool.VANILLA) {
            this.load.put(key, value);
         }

         if (origin == ClassCache.Inner_CachePool.MOD) {
            ClassCache.Inner_BufferHelper existing = this.init_Map.get(key);
            if (existing != null && existing.init_ClassCache_StreamU().init_ClassCache_CachePo() == ClassCache.Inner_CachePool.VANILLA) {
               return;
            }
         }

         this.init_Map.put(key, new ClassCache.Inner_BufferHelper(new ClassCache.Inner_StreamUtil(value, origin), source, sourcePackId));
      }

      private static ClassCache.Inner_CachePool init_ClassCache_CachePo_PackResources(PackResources source) {
         if (source instanceof JsonUtil) {
            return ClassCache.Inner_CachePool.OTHER;
         }

         String modId = source instanceof ModNioPackResources modPack ? modPack.getFabricModMetadata().getId() : BitSetUtil.init(source.getClass());
         return modId == null ? ClassCache.Inner_CachePool.OTHER : ClassCache.Inner_CachePool.MOD;
      }

      public void init(List<String> removed, Map<String, String> renamed) {
         for (String key : removed) {
            this.init_Map.remove(key);
            this.load.remove(key);
         }

         for (Entry<String, String> rename : renamed.entrySet()) {
            String previousVanilla = this.load.remove(rename.getKey());
            if (previousVanilla == null) {
               this.load.remove(rename.getValue());
            } else {
               this.load.put(rename.getValue(), previousVanilla);
            }

            ClassCache.Inner_BufferHelper previousValue = this.init_Map.remove(rename.getKey());
            if (previousValue == null) {
               this.init_Map.remove(rename.getValue());
            } else {
               this.init_Map.put(rename.getValue(), previousValue);
            }
         }
      }

      public ClassCache init_ClassCache() {
         return new ClassCache(this.init_ResourceManager, Map.copyOf(this.init_Map), Map.copyOf(this.load));
      }

      ClassCache.Inner_StreamUtil init_ClassCache_StreamU_Str(String key) {
         ClassCache.Inner_BufferHelper entry = this.init_Map.get(key);
         return entry != null ? entry.init_ClassCache_StreamU() : null;
      }

      @Override
      public void close() {
         if (!this.init_Z) {
            this.init_Z = true;
            if (ClassCache.init_ThreadLocal.get() != this) {
               throw new IllegalStateException("Translation snapshot builders closed out of order");
            }

            if (this.init_ClassCache_DataUti == null) {
               ClassCache.init_ThreadLocal.remove();
            } else {
               ClassCache.init_ThreadLocal.set(this.init_ClassCache_DataUti);
            }
         }
      }
   }

   public record Inner_StreamUtil(String init_Str, ClassCache.Inner_CachePool init_ClassCache_CachePo) {
      public Inner_StreamUtil {
         Objects.requireNonNull(init_Str, "value");
         Objects.requireNonNull(init_ClassCache_CachePo, "origin");
      }
   }
}
