package autismclient.protector;

import autismclient.AutismClientAddon;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.resources.Identifier;

public final class ModNamespaceCatalog {
   private static final Set<String> BUILTIN_NAMESPACES = Set.of("minecraft", "java", "mixinextras", "fabricloader", "fabric-loader", "fabric", "fabric-api", "fabric_api");
   private static final Set<String> FORCED_MODS = Set.of("voicechat");
   private static final Set<String> foreignModIds = ConcurrentHashMap.newKeySet();
   private static final Set<String> localModIds = ConcurrentHashMap.newKeySet();
   private static final Set<String> fabricNamespaces = ConcurrentHashMap.newKeySet();
   private static final Set<String> serverKnownKeys = ConcurrentHashMap.newKeySet();
   private static final Map<String, String> serverTranslations = new ConcurrentHashMap<>();
   private static final Map<String, Set<String>> keyOwners = new ConcurrentHashMap<>();
   private static final Set<String> flush = ConcurrentHashMap.newKeySet();
   private static final Map<String, Set<String>> blockKeyOwners = new ConcurrentHashMap<>();
   private static final int MAX_CACHE_ENTRIES = 8192;
   private static final Map<String, ModNamespaceCatalog.Decision> decisionCache = new ConcurrentHashMap<>();
   private static final Map<String, Boolean> blockedCache = new ConcurrentHashMap<>();
   private static final Map<String, Boolean> modSegmentCache = new ConcurrentHashMap<>();
   private static final AtomicBoolean initialized = new AtomicBoolean(false);

   private ModNamespaceCatalog() {
   }

   public static void initialize() {
      if (initialized.compareAndSet(false, true)) {
         try {
            for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
               String normalized = normalizeModId(mod.getMetadata().getId());
               if (normalized != null && !BUILTIN_NAMESPACES.contains(normalized)) {
                  if (!normalized.startsWith("fabric-") && !normalized.startsWith("fabric_")) {
                     localModIds.add(normalized);
                  } else {
                     fabricNamespaces.add(normalized);
                  }
               }
            }

            fabricNamespaces.add("fabric");
            fabricNamespaces.add("fabric-api");
            fabricNamespaces.addAll(FORCED_MODS);

            for (String id : FORCED_MODS) {
               addForeignModId(id);
            }

            read();
            clearCaches();
         } catch (Throwable t) {
            AutismClientAddon.LOG.warn("[AutismProtector] Failed to bootstrap mod ID set: {}", t.getMessage());
         }
      }
   }

   private static void read() {
      addForeignModId("fabric-resource-loader-v1");
      addForeignModId("fabric-creative-tab-api-v1");
      addForeignModId("fabric-registry-sync-v0");
      addForeignModId("fabric-convention-tags-v2");
      addForeignModId("fabric-data-attachment-api-v1");
   }

   public static void reset() {
      serverKnownKeys.clear();
      serverTranslations.clear();
      keyOwners.clear();
      clearCaches();
   }

   public static void addServerKey(String key) {
      if (key != null && !key.isBlank() && serverKnownKeys.add(key)) {
         clearCaches();
      }
   }

   public static void addServerTranslation(String key, String value) {
      if (key != null && !key.isBlank() && value != null) {
         serverTranslations.put(key, value);
         clearCaches();
      }
   }

   public static void addKeyOwner(String key, String modId) {
      String normalized = normalizeModId(modId);
      if (key != null && !key.isBlank() && normalized != null && keyOwners.computeIfAbsent(key, ignored -> ConcurrentHashMap.newKeySet()).add(normalized)) {
         clearCaches();
      }
   }

   public static void applyDeprecations(Iterable<String> removed, Map<String, String> renamed) {
      if (removed != null) {
         for (String key : removed) {
            serverKnownKeys.remove(key);
            serverTranslations.remove(key);
            keyOwners.remove(key);
         }
      }

      if (renamed != null) {
         for (Entry<String, String> entry : renamed.entrySet()) {
            String from = entry.getKey();
            String to = entry.getValue();
            serverKnownKeys.remove(from);
            serverTranslations.remove(from);
            keyOwners.remove(from);
            if (to != null && !to.isBlank()) {
               serverKnownKeys.add(to);
            }
         }
      }

      clearCaches();
   }

   public static void addVanillaKeybind(String key) {
      if (key != null && !key.isBlank() && flush.add(key)) {
         clearCaches();
      }
   }

   public static void read(String key, String modId) {
      String normalized = normalizeModId(modId);
      if (key != null && !key.isBlank() && normalized != null && blockKeyOwners.computeIfAbsent(key, ignored -> ConcurrentHashMap.newKeySet()).add(normalized)) {
         clearCaches();
      }
   }

   public static void addForeignModId(String modId) {
      String normalized = normalizeModId(modId);
      if (normalized != null && !BUILTIN_NAMESPACES.contains(normalized) && foreignModIds.add(normalized)) {
         clearCaches();
      }
   }

   public static void addForeignModIds(Iterable<String> modIds) {
      if (modIds != null) {
         for (String modId : modIds) {
            addForeignModId(modId);
         }
      }
   }

   public static String translationFor(String key, String defaultValue) {
      if (key != null && !key.isEmpty()) {
         ModNamespaceCatalog.Decision decision = decisionCache.get(key);
         if (decision == null) {
            decision = decide(key);
            putBounded(decisionCache, key, decision);
         }

         String var10000;
         switch (decision.kind()) {
            case 1:
               var10000 = defaultValue;
               break;
            case 2:
               var10000 = decision.value();
               break;
            default:
               var10000 = null;
         }

         return var10000;
      } else {
         return null;
      }
   }

   public static boolean isVanillaKeybind(String key) {
      return key != null && flush.contains(key) && !blockKeyOwners.containsKey(key);
   }

   public static boolean isBlockedKey(String key) {
      if (key != null && !key.isEmpty()) {
         Boolean cached = blockedCache.get(key);
         if (cached != null) {
            return cached;
         }

         boolean blocked = sync(key);
         putBounded(blockedCache, key, blocked);
         return blocked;
      } else {
         return false;
      }
   }

   public static boolean isHandledKey(String key) {
      return translationFor(key, key) != null ? true : isBlockedKey(key);
   }

   public static boolean hasLocalModSegment(String key) {
      if (key != null && !key.isEmpty() && !localModIds.isEmpty()) {
         Boolean cached = modSegmentCache.get(key);
         if (cached != null) {
            return cached;
         }

         boolean result = false;
         int start = 0;
         int len = key.length();

         for (int i = 0; i <= len; i++) {
            if (i == len || key.charAt(i) == '.') {
               if (i > start) {
                  String segment = key.substring(start, i).toLowerCase(Locale.ROOT);
                  if (localModIds.contains(segment)) {
                     result = true;
                     break;
                  }
               }

               start = i + 1;
            }
         }

         putBounded(modSegmentCache, key, result);
         return result;
      } else {
         return false;
      }
   }

   public static boolean isVanillaOrFabricChannel(Identifier channel) {
      if (channel == null) {
         return false;
      }

      String namespace = channel.getNamespace();
      if (namespace == null) {
         return false;
      }

      String lower = namespace.toLowerCase(Locale.ROOT);
      return !"minecraft".equals(lower) && !"c".equals(lower) ? fabricNamespaces.contains(lower) : true;
   }

   private static boolean isForeignMod(String modId) {
      String normalized = normalizeModId(modId);
      return normalized != null && foreignModIds.contains(normalized);
   }

   private static ModNamespaceCatalog.Decision decide(String key) {
      if (!serverKnownKeys.contains(key) && !VanillaTranslationKeyLoader.isVanillaKey(key)) {
         String serverValue = serverTranslations.get(key);
         if (serverValue != null) {
            return ModNamespaceCatalog.Decision.serverValue(serverValue);
         }

         Set<String> owners = keyOwners.get(key);
         if (owners != null && !owners.isEmpty()) {
            for (String owner : owners) {
               if (isForeignMod(owner)) {
                  return ModNamespaceCatalog.Decision.PASS;
               }
            }

            return ModNamespaceCatalog.Decision.BLOCK;
         } else {
            return hasLocalModSegment(key) ? ModNamespaceCatalog.Decision.BLOCK : ModNamespaceCatalog.Decision.PASS;
         }
      } else {
         return ModNamespaceCatalog.Decision.PASS;
      }
   }

   private static boolean sync(String key) {
      if (flush.contains(key)) {
         return false;
      }

      Set<String> owners = blockKeyOwners.get(key);
      if (owners != null && !owners.isEmpty()) {
         for (String owner : owners) {
            if (isForeignMod(owner)) {
               return false;
            }
         }

         return true;
      } else {
         return hasLocalModSegment(key);
      }
   }

   private static <T> void putBounded(Map<String, T> cache, String key, T value) {
      if (cache.size() >= 8192) {
         cache.clear();
      }

      cache.put(key, value);
   }

   private static void clearCaches() {
      decisionCache.clear();
      blockedCache.clear();
      modSegmentCache.clear();
   }

   private static String normalizeModId(String modId) {
      return modId != null && !modId.isBlank() ? modId.toLowerCase(Locale.ROOT).trim() : null;
   }

   public static Set<String> foreignMods() {
      return new HashSet<>(foreignModIds);
   }

   private record Decision(int write, String value) {
      static final int init_I = 0;
      static final int load_I = 1;
      static final int read = 2;
      static final ModNamespaceCatalog.Decision PASS = new ModNamespaceCatalog.Decision(0, null);
      static final ModNamespaceCatalog.Decision BLOCK = new ModNamespaceCatalog.Decision(1, null);

      static ModNamespaceCatalog.Decision serverValue(String value) {
         return new ModNamespaceCatalog.Decision(2, value);
      }

      public int kind() {
         return this.write;
      }
   }
}
