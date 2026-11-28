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
   private static final ThreadLocal<TranslationSnapshot.SnapshotBuilder> ACTIVE_BUILDER = new ThreadLocal<>();
   private final ResourceManager resourceManager;
   private final Map<String, TranslationSnapshot.SourcedEntry> entries;
   private final Map<String, String> vanillaEntries;

   private TranslationSnapshot(ResourceManager resourceManager, Map<String, TranslationSnapshot.SourcedEntry> entries, Map<String, String> vanillaEntries) {
      this.resourceManager = resourceManager;
      this.entries = entries;
      this.vanillaEntries = vanillaEntries;
   }

   public static String resolve(Language language, String key, String fallback) {
      return resolve(language, key, fallback, null, false);
   }

   public static String translate(Language language, String key, String fallback, ConnectionPrivacyRegistry.ConnectionSession origin) {
      return resolve(language, key, fallback, origin, true);
   }

   private static String resolve(Language language, String key, String fallback, ConnectionPrivacyRegistry.ConnectionSession origin, boolean connectionBound) {
      if (language instanceof TranslationSnapshotDuck access) {
         TranslationSnapshot snapshot = access.autism$getTranslationSnapshot();
         if (snapshot != null) {
            TranslationSnapshot.SourcedEntry sourced = snapshot.entries.get(key);
            if (sourced != null) {
               TranslationSnapshot.TranslationEntry entry = sourced.entry();
               if (entry.origin() == TranslationSnapshot.EntryOrigin.VANILLA) {
                  return entry.value();
               }

               if (sourced.source() != null
                  && sourced.init_Str() != null
                  && (
                     connectionBound
                        ? PackTranslationPolicyResolver.init(snapshot.resourceManager, sourced.source(), sourced.init_Str(), origin)
                        : PackTranslationPolicyResolver.init(snapshot.resourceManager, sourced.source(), sourced.init_Str())
                  )) {
                  return entry.value();
               }
            }

            if (connectionBound) {
               String vanilla = snapshot.vanillaEntries.get(key);
               if (vanilla != null) {
                  return vanilla;
               }
            }
         }
      }

      return fallback != null ? fallback : key;
   }

   private record SourcedEntry(TranslationSnapshot.TranslationEntry entry, PackResources source, String init_Str) {
      private SourcedEntry {
         Objects.requireNonNull(entry, "entry");
      }
   }

   public enum EntryOrigin {
      VANILLA,
      MOD,
      OTHER;
   }

   public static final class SnapshotBuilder implements AutoCloseable {
      private final ResourceManager resourceManager;
      private final Map<String, TranslationSnapshot.SourcedEntry> entries = new HashMap<>();
      private final Map<String, String> vanilla = new HashMap<>();
      private final TranslationSnapshot.SnapshotBuilder previous;
      private boolean closed;

      private SnapshotBuilder(ResourceManager resourceManager, TranslationSnapshot.SnapshotBuilder previous) {
         this.resourceManager = Objects.requireNonNull(resourceManager, "resourceManager");
         this.previous = previous;
      }

      public static TranslationSnapshot.SnapshotBuilder begin(ResourceManager resourceManager) {
         TranslationSnapshot.SnapshotBuilder builder = new TranslationSnapshot.SnapshotBuilder(resourceManager, TranslationSnapshot.ACTIVE_BUILDER.get());
         TranslationSnapshot.ACTIVE_BUILDER.set(builder);
         return builder;
      }

      public static TranslationSnapshot.SnapshotBuilder current() {
         return TranslationSnapshot.ACTIVE_BUILDER.get();
      }

      public void putFromPack(PackResources source, String sourcePackId, String key, String value) {
         Objects.requireNonNull(source, "source");
         Objects.requireNonNull(key, "key");
         Objects.requireNonNull(value, "value");
         if (source instanceof VanillaPackResources) {
            this.put(TranslationSnapshot.EntryOrigin.VANILLA, key, value);
         } else if (PackTranslationPolicyResolver.init(this.resourceManager, source, sourcePackId)) {
            this.put(originOf(source), key, value, source, sourcePackId);
         }
      }

      void put(TranslationSnapshot.EntryOrigin origin, String key, String value) {
         this.put(origin, key, value, null, null);
      }

      private void put(TranslationSnapshot.EntryOrigin origin, String key, String value, PackResources source, String sourcePackId) {
         Objects.requireNonNull(origin, "origin");
         Objects.requireNonNull(key, "key");
         Objects.requireNonNull(value, "value");
         if (origin == TranslationSnapshot.EntryOrigin.VANILLA) {
            this.vanilla.put(key, value);
         }

         if (origin == TranslationSnapshot.EntryOrigin.MOD) {
            TranslationSnapshot.SourcedEntry existing = this.entries.get(key);
            if (existing != null && existing.entry().origin() == TranslationSnapshot.EntryOrigin.VANILLA) {
               return;
            }
         }

         this.entries.put(key, new TranslationSnapshot.SourcedEntry(new TranslationSnapshot.TranslationEntry(value, origin), source, sourcePackId));
      }

      private static TranslationSnapshot.EntryOrigin originOf(PackResources source) {
         if (source instanceof IsolatedResourcePack) {
            return TranslationSnapshot.EntryOrigin.OTHER;
         }

         String modId = source instanceof ModNioPackResources modPack ? modPack.getFabricModMetadata().getId() : CallerModInspector.modIdOf(source.getClass());
         return modId == null ? TranslationSnapshot.EntryOrigin.OTHER : TranslationSnapshot.EntryOrigin.MOD;
      }

      public void applyDeprecations(List<String> removed, Map<String, String> renamed) {
         for (String key : removed) {
            this.entries.remove(key);
            this.vanilla.remove(key);
         }

         for (Entry<String, String> rename : renamed.entrySet()) {
            String previousVanilla = this.vanilla.remove(rename.getKey());
            if (previousVanilla == null) {
               this.vanilla.remove(rename.getValue());
            } else {
               this.vanilla.put(rename.getValue(), previousVanilla);
            }

            TranslationSnapshot.SourcedEntry previousValue = this.entries.remove(rename.getKey());
            if (previousValue == null) {
               this.entries.remove(rename.getValue());
            } else {
               this.entries.put(rename.getValue(), previousValue);
            }
         }
      }

      public TranslationSnapshot build() {
         return new TranslationSnapshot(this.resourceManager, Map.copyOf(this.entries), Map.copyOf(this.vanilla));
      }

      TranslationSnapshot.TranslationEntry lookup(String key) {
         TranslationSnapshot.SourcedEntry entry = this.entries.get(key);
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

   public record TranslationEntry(String value, TranslationSnapshot.EntryOrigin origin) {
      public TranslationEntry {
         Objects.requireNonNull(value, "value");
         Objects.requireNonNull(origin, "origin");
      }
   }
}
