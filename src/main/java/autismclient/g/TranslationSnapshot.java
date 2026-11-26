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

public final class TranslationSnapshot {
   private static final ThreadLocal<TranslationSnapshot.Inner_DataUtil> init_ThreadLocal = new ThreadLocal<>();
   private final ResourceManager init_ResourceManager;
   private final Map<String, TranslationSnapshot.Inner_BufferHelper> init_Map;
   private final Map<String, String> load;

   private TranslationSnapshot(ResourceManager resourceManager, Map<String, TranslationSnapshot.Inner_BufferHelper> entries, Map<String, String> vanillaEntries) {
      this.init_ResourceManager = resourceManager;
      this.init_Map = entries;
      this.load = vanillaEntries;
   }

   public static String init(Language language, String key, String fallback) {
      return init(language, key, fallback, null, false);
   }

   public static String init(Language language, String key, String fallback, ConnectionPrivacyRegistry.Inner_StreamUtil origin) {
      return init(language, key, fallback, origin, true);
   }

   private static String init(Language language, String key, String fallback, ConnectionPrivacyRegistry.Inner_StreamUtil origin, boolean connectionBound) {
      if (language instanceof TranslationSnapshotDuck access) {
         TranslationSnapshot snapshot = access.autism$getTranslationSnapshot();
         if (snapshot != null) {
            TranslationSnapshot.Inner_BufferHelper sourced = snapshot.init_Map.get(key);
            if (sourced != null) {
               TranslationSnapshot.Inner_StreamUtil entry = sourced.init_ClassCache_StreamU();
               if (entry.init_ClassCache_CachePo() == TranslationSnapshot.Inner_CachePool.VANILLA) {
                  return entry.init_Str();
               }

               if (sourced.init_PackResources() != null
                  && sourced.init_Str() != null
                  && (
                     connectionBound
                        ? PackTranslationPolicyResolver.init(snapshot.init_ResourceManager, sourced.init_PackResources(), sourced.init_Str(), origin)
                        : PackTranslationPolicyResolver.init(snapshot.init_ResourceManager, sourced.init_PackResources(), sourced.init_Str())
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

   private record Inner_BufferHelper(TranslationSnapshot.Inner_StreamUtil init_ClassCache_StreamU, PackResources init_PackResources, String init_Str) {
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
      private final Map<String, TranslationSnapshot.Inner_BufferHelper> init_Map = new HashMap<>();
      private final Map<String, String> load = new HashMap<>();
      private final TranslationSnapshot.Inner_DataUtil init_ClassCache_DataUti;
      private boolean init_Z;

      private Inner_DataUtil(ResourceManager resourceManager, TranslationSnapshot.Inner_DataUtil previous) {
         this.init_ResourceManager = Objects.requireNonNull(resourceManager, "resourceManager");
         this.init_ClassCache_DataUti = previous;
      }

      public static TranslationSnapshot.Inner_DataUtil init_ClassCache_DataUti_ResourceManager(ResourceManager resourceManager) {
         TranslationSnapshot.Inner_DataUtil builder = new TranslationSnapshot.Inner_DataUtil(resourceManager, TranslationSnapshot.init_ThreadLocal.get());
         TranslationSnapshot.init_ThreadLocal.set(builder);
         return builder;
      }

      public static TranslationSnapshot.Inner_DataUtil init_ClassCache_DataUti() {
         return TranslationSnapshot.init_ThreadLocal.get();
      }

      public void init(PackResources source, String sourcePackId, String key, String value) {
         Objects.requireNonNull(source, "source");
         Objects.requireNonNull(key, "key");
         Objects.requireNonNull(value, "value");
         if (source instanceof VanillaPackResources) {
            this.init(TranslationSnapshot.Inner_CachePool.VANILLA, key, value);
         } else if (PackTranslationPolicyResolver.init(this.init_ResourceManager, source, sourcePackId)) {
            this.init(init_ClassCache_CachePo_PackResources(source), key, value, source, sourcePackId);
         }
      }

      void init(TranslationSnapshot.Inner_CachePool origin, String key, String value) {
         this.init(origin, key, value, null, null);
      }

      private void init(TranslationSnapshot.Inner_CachePool origin, String key, String value, PackResources source, String sourcePackId) {
         Objects.requireNonNull(origin, "origin");
         Objects.requireNonNull(key, "key");
         Objects.requireNonNull(value, "value");
         if (origin == TranslationSnapshot.Inner_CachePool.VANILLA) {
            this.load.put(key, value);
         }

         if (origin == TranslationSnapshot.Inner_CachePool.MOD) {
            TranslationSnapshot.Inner_BufferHelper existing = this.init_Map.get(key);
            if (existing != null && existing.init_ClassCache_StreamU().init_ClassCache_CachePo() == TranslationSnapshot.Inner_CachePool.VANILLA) {
               return;
            }
         }

         this.init_Map.put(key, new TranslationSnapshot.Inner_BufferHelper(new TranslationSnapshot.Inner_StreamUtil(value, origin), source, sourcePackId));
      }

      private static TranslationSnapshot.Inner_CachePool init_ClassCache_CachePo_PackResources(PackResources source) {
         if (source instanceof IsolatedResourcePack) {
            return TranslationSnapshot.Inner_CachePool.OTHER;
         }

         String modId = source instanceof ModNioPackResources modPack ? modPack.getFabricModMetadata().getId() : CallerModInspector.init(source.getClass());
         return modId == null ? TranslationSnapshot.Inner_CachePool.OTHER : TranslationSnapshot.Inner_CachePool.MOD;
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

            TranslationSnapshot.Inner_BufferHelper previousValue = this.init_Map.remove(rename.getKey());
            if (previousValue == null) {
               this.init_Map.remove(rename.getValue());
            } else {
               this.init_Map.put(rename.getValue(), previousValue);
            }
         }
      }

      public TranslationSnapshot init_ClassCache() {
         return new TranslationSnapshot(this.init_ResourceManager, Map.copyOf(this.init_Map), Map.copyOf(this.load));
      }

      TranslationSnapshot.Inner_StreamUtil init_ClassCache_StreamU_Str(String key) {
         TranslationSnapshot.Inner_BufferHelper entry = this.init_Map.get(key);
         return entry != null ? entry.init_ClassCache_StreamU() : null;
      }

      @Override
      public void close() {
         if (!this.init_Z) {
            this.init_Z = true;
            if (TranslationSnapshot.init_ThreadLocal.get() != this) {
               throw new IllegalStateException("Translation snapshot builders closed out of order");
            }

            if (this.init_ClassCache_DataUti == null) {
               TranslationSnapshot.init_ThreadLocal.remove();
            } else {
               TranslationSnapshot.init_ThreadLocal.set(this.init_ClassCache_DataUti);
            }
         }
      }
   }

   public record Inner_StreamUtil(String init_Str, TranslationSnapshot.Inner_CachePool init_ClassCache_CachePo) {
      public Inner_StreamUtil {
         Objects.requireNonNull(init_Str, "value");
         Objects.requireNonNull(init_ClassCache_CachePo, "origin");
      }
   }
}
