package autismclient.protector;

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
   private static final ThreadLocal<TranslationSnapshot.Inner_DataUtil> ACTIVE_BUILDER = new ThreadLocal<>();
   private final ResourceManager resourceManager;
   private final Map<String, TranslationSnapshot.Inner_BufferHelper> entries;
   private final Map<String, String> load;

   private TranslationSnapshot(ResourceManager resourceManager, Map<String, TranslationSnapshot.Inner_BufferHelper> entries, Map<String, String> vanillaEntries) {
      this.resourceManager = resourceManager;
      this.entries = entries;
      this.load = vanillaEntries;
   }

   public static String init(Language language, String key, String fallback) {
      return init(language, key, fallback, null, false);
   }

   public static String init(Language language, String key, String fallback, ConnectionPrivacyRegistry.ConnectionSession origin) {
      return init(language, key, fallback, origin, true);
   }

   private static String init(Language language, String key, String fallback, ConnectionPrivacyRegistry.ConnectionSession origin, boolean connectionBound) {
      if (language instanceof TranslationSnapshotDuck access) {
         TranslationSnapshot snapshot = access.autism$getTranslationSnapshot();
         if (snapshot != null) {
            TranslationSnapshot.Inner_BufferHelper sourced = snapshot.entries.get(key);
            if (sourced != null) {
               TranslationSnapshot.Inner_StreamUtil entry = sourced.entry();
               if (entry.origin() == TranslationSnapshot.Inner_CachePool.VANILLA) {
                  return entry.init_Str();
               }

               if (sourced.source() != null
                  && sourced.init_Str() != null
                  && (
                     connectionBound
                        ? PackTranslationPolicyResolver.init(snapshot.resourceManager, sourced.source(), sourced.init_Str(), origin)
                        : PackTranslationPolicyResolver.init(snapshot.resourceManager, sourced.source(), sourced.init_Str())
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

   private record Inner_BufferHelper(TranslationSnapshot.Inner_StreamUtil entry, PackResources source, String init_Str) {
      private Inner_BufferHelper {
         Objects.requireNonNull(entry, "entry");
      }
   }

   public enum Inner_CachePool {
      VANILLA,
      MOD,
      OTHER;
   }

   public static final class Inner_DataUtil implements AutoCloseable {
      private final ResourceManager resourceManager;
      private final Map<String, TranslationSnapshot.Inner_BufferHelper> entries = new HashMap<>();
      private final Map<String, String> load = new HashMap<>();
      private final TranslationSnapshot.Inner_DataUtil previous;
      private boolean closed;

      private Inner_DataUtil(ResourceManager resourceManager, TranslationSnapshot.Inner_DataUtil previous) {
         this.resourceManager = Objects.requireNonNull(resourceManager, "resourceManager");
         this.previous = previous;
      }

      public static TranslationSnapshot.Inner_DataUtil begin(ResourceManager resourceManager) {
         TranslationSnapshot.Inner_DataUtil builder = new TranslationSnapshot.Inner_DataUtil(resourceManager, TranslationSnapshot.ACTIVE_BUILDER.get());
         TranslationSnapshot.ACTIVE_BUILDER.set(builder);
         return builder;
      }

      public static TranslationSnapshot.Inner_DataUtil current() {
         return TranslationSnapshot.ACTIVE_BUILDER.get();
      }

      public void init(PackResources source, String sourcePackId, String key, String value) {
         Objects.requireNonNull(source, "source");
         Objects.requireNonNull(key, "key");
         Objects.requireNonNull(value, "value");
         if (source instanceof VanillaPackResources) {
            this.init(TranslationSnapshot.Inner_CachePool.VANILLA, key, value);
         } else if (PackTranslationPolicyResolver.init(this.resourceManager, source, sourcePackId)) {
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
            TranslationSnapshot.Inner_BufferHelper existing = this.entries.get(key);
            if (existing != null && existing.entry().origin() == TranslationSnapshot.Inner_CachePool.VANILLA) {
               return;
            }
         }

         this.entries.put(key, new TranslationSnapshot.Inner_BufferHelper(new TranslationSnapshot.Inner_StreamUtil(value, origin), source, sourcePackId));
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
            this.entries.remove(key);
            this.load.remove(key);
         }

         for (Entry<String, String> rename : renamed.entrySet()) {
            String previousVanilla = this.load.remove(rename.getKey());
            if (previousVanilla == null) {
               this.load.remove(rename.getValue());
            } else {
               this.load.put(rename.getValue(), previousVanilla);
            }

            TranslationSnapshot.Inner_BufferHelper previousValue = this.entries.remove(rename.getKey());
            if (previousValue == null) {
               this.entries.remove(rename.getValue());
            } else {
               this.entries.put(rename.getValue(), previousValue);
            }
         }
      }

      public TranslationSnapshot build() {
         return new TranslationSnapshot(this.resourceManager, Map.copyOf(this.entries), Map.copyOf(this.load));
      }

      TranslationSnapshot.Inner_StreamUtil lookup(String key) {
         TranslationSnapshot.Inner_BufferHelper entry = this.entries.get(key);
         return entry != null ? entry.entry() : null;
      }

      @Override
      public void close() {
         if (!this.closed) {
            this.closed = true;
            if (TranslationSnapshot.ACTIVE_BUILDER.get() != this) {
               throw new IllegalStateException("Translation snapshot builders closed out of order");
            }

            if (this.previous == null) {
               TranslationSnapshot.ACTIVE_BUILDER.remove();
            } else {
               TranslationSnapshot.ACTIVE_BUILDER.set(this.previous);
            }
         }
      }
   }

   public record Inner_StreamUtil(String init_Str, TranslationSnapshot.Inner_CachePool origin) {
      public Inner_StreamUtil {
         Objects.requireNonNull(init_Str, "value");
         Objects.requireNonNull(origin, "origin");
      }
   }
}
