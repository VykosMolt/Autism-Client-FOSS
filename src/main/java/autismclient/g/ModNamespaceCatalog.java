package autismclient.g;

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
   private static final Set<String> init_Set = Set.of("minecraft", "java", "mixinextras", "fabricloader", "fabric-loader", "fabric", "fabric-api", "fabric_api");
   private static final Set<String> load_Set = Set.of("voicechat");
   private static final Set<String> read_Set = ConcurrentHashMap.newKeySet();
   private static final Set<String> write_Set = ConcurrentHashMap.newKeySet();
   private static final Set<String> update_Set = ConcurrentHashMap.newKeySet();
   private static final Set<String> sync_Set = ConcurrentHashMap.newKeySet();
   private static final Map<String, String> init_Map = new ConcurrentHashMap<>();
   private static final Map<String, Set<String>> load_Map = new ConcurrentHashMap<>();
   private static final Set<String> flush = ConcurrentHashMap.newKeySet();
   private static final Map<String, Set<String>> read_Map = new ConcurrentHashMap<>();
   private static final int init_I = 8192;
   private static final Map<String, ModNamespaceCatalog.Inner_DataUtil> write_Map = new ConcurrentHashMap<>();
   private static final Map<String, Boolean> update_Map = new ConcurrentHashMap<>();
   private static final Map<String, Boolean> sync_Map = new ConcurrentHashMap<>();
   private static final AtomicBoolean init_AtomicBoolean = new AtomicBoolean(false);

   private ModNamespaceCatalog() {
   }

   public static void init_V() {
      if (init_AtomicBoolean.compareAndSet(false, true)) {
         try {
            for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
               String normalized = init_Str(mod.getMetadata().getId());
               if (normalized != null && !init_Set.contains(normalized)) {
                  if (!normalized.startsWith("fabric-") && !normalized.startsWith("fabric_")) {
                     write_Set.add(normalized);
                  } else {
                     update_Set.add(normalized);
                  }
               }
            }

            update_Set.add("fabric");
            update_Set.add("fabric-api");
            update_Set.addAll(load_Set);

            for (String id : load_Set) {
               read_V(id);
            }

            read();
            write();
         } catch (Throwable t) {
            AutismClientAddon.LOG.warn("[AutismProtector] Failed to bootstrap mod ID set: {}", t.getMessage());
         }
      }
   }

   private static void read() {
      read_V("fabric-resource-loader-v1");
      read_V("fabric-creative-tab-api-v1");
      read_V("fabric-registry-sync-v0");
      read_V("fabric-convention-tags-v2");
      read_V("fabric-data-attachment-api-v1");
   }

   public static void load() {
      sync_Set.clear();
      init_Map.clear();
      load_Map.clear();
      write();
   }

   public static void init_V(String key) {
      if (key != null && !key.isBlank() && sync_Set.add(key)) {
         write();
      }
   }

   public static void init_V(String key, String value) {
      if (key != null && !key.isBlank() && value != null) {
         init_Map.put(key, value);
         write();
      }
   }

   public static void load(String key, String modId) {
      String normalized = init_Str(modId);
      if (key != null && !key.isBlank() && normalized != null && load_Map.computeIfAbsent(key, ignored -> ConcurrentHashMap.newKeySet()).add(normalized)) {
         write();
      }
   }

   public static void init(Iterable<String> removed, Map<String, String> renamed) {
      if (removed != null) {
         for (String key : removed) {
            sync_Set.remove(key);
            init_Map.remove(key);
            load_Map.remove(key);
         }
      }

      if (renamed != null) {
         for (Entry<String, String> entry : renamed.entrySet()) {
            String from = entry.getKey();
            String to = entry.getValue();
            sync_Set.remove(from);
            init_Map.remove(from);
            load_Map.remove(from);
            if (to != null && !to.isBlank()) {
               sync_Set.add(to);
            }
         }
      }

      write();
   }

   public static void load_V(String key) {
      if (key != null && !key.isBlank() && flush.add(key)) {
         write();
      }
   }

   public static void read(String key, String modId) {
      String normalized = init_Str(modId);
      if (key != null && !key.isBlank() && normalized != null && read_Map.computeIfAbsent(key, ignored -> ConcurrentHashMap.newKeySet()).add(normalized)) {
         write();
      }
   }

   public static void read_V(String modId) {
      String normalized = init_Str(modId);
      if (normalized != null && !init_Set.contains(normalized) && read_Set.add(normalized)) {
         write();
      }
   }

   public static void init(Iterable<String> modIds) {
      if (modIds != null) {
         for (String modId : modIds) {
            read_V(modId);
         }
      }
   }

   public static String init_Str(String key, String defaultValue) {
      if (key != null && !key.isEmpty()) {
         ModNamespaceCatalog.Inner_DataUtil decision = write_Map.get(key);
         if (decision == null) {
            decision = init_MethodUtil_DataUti(key);
            init(write_Map, key, decision);
         }

         String var10000;
         switch (decision.init_I()) {
            case 1:
               var10000 = defaultValue;
               break;
            case 2:
               var10000 = decision.init_Str();
               break;
            default:
               var10000 = null;
         }

         return var10000;
      } else {
         return null;
      }
   }

   public static boolean init_Z(String key) {
      return key != null && flush.contains(key) && !read_Map.containsKey(key);
   }

   public static boolean load_Z(String key) {
      if (key != null && !key.isEmpty()) {
         Boolean cached = update_Map.get(key);
         if (cached != null) {
            return cached;
         }

         boolean blocked = sync(key);
         init(update_Map, key, blocked);
         return blocked;
      } else {
         return false;
      }
   }

   public static boolean read_Z(String key) {
      return init_Str(key, key) != null ? true : load_Z(key);
   }

   public static boolean write(String key) {
      if (key != null && !key.isEmpty() && !write_Set.isEmpty()) {
         Boolean cached = sync_Map.get(key);
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
                  if (write_Set.contains(segment)) {
                     result = true;
                     break;
                  }
               }

               start = i + 1;
            }
         }

         init(sync_Map, key, result);
         return result;
      } else {
         return false;
      }
   }

   public static boolean init(Identifier channel) {
      if (channel == null) {
         return false;
      }

      String namespace = channel.getNamespace();
      if (namespace == null) {
         return false;
      }

      String lower = namespace.toLowerCase(Locale.ROOT);
      return !"minecraft".equals(lower) && !"c".equals(lower) ? update_Set.contains(lower) : true;
   }

   private static boolean update(String modId) {
      String normalized = init_Str(modId);
      return normalized != null && read_Set.contains(normalized);
   }

   private static ModNamespaceCatalog.Inner_DataUtil init_MethodUtil_DataUti(String key) {
      if (!sync_Set.contains(key) && !VanillaTranslationKeyLoader.init(key)) {
         String serverValue = init_Map.get(key);
         if (serverValue != null) {
            return ModNamespaceCatalog.Inner_DataUtil.init(serverValue);
         }

         Set<String> owners = load_Map.get(key);
         if (owners != null && !owners.isEmpty()) {
            for (String owner : owners) {
               if (update(owner)) {
                  return ModNamespaceCatalog.Inner_DataUtil.init_MethodUtil_DataUti;
               }
            }

            return ModNamespaceCatalog.Inner_DataUtil.load_MethodUtil_DataUti;
         } else {
            return write(key) ? ModNamespaceCatalog.Inner_DataUtil.load_MethodUtil_DataUti : ModNamespaceCatalog.Inner_DataUtil.init_MethodUtil_DataUti;
         }
      } else {
         return ModNamespaceCatalog.Inner_DataUtil.init_MethodUtil_DataUti;
      }
   }

   private static boolean sync(String key) {
      if (flush.contains(key)) {
         return false;
      }

      Set<String> owners = read_Map.get(key);
      if (owners != null && !owners.isEmpty()) {
         for (String owner : owners) {
            if (update(owner)) {
               return false;
            }
         }

         return true;
      } else {
         return write(key);
      }
   }

   private static <T> void init(Map<String, T> cache, String key, T value) {
      if (cache.size() >= 8192) {
         cache.clear();
      }

      cache.put(key, value);
   }

   private static void write() {
      write_Map.clear();
      update_Map.clear();
      sync_Map.clear();
   }

   private static String init_Str(String modId) {
      return modId != null && !modId.isBlank() ? modId.toLowerCase(Locale.ROOT).trim() : null;
   }

   public static Set<String> init_Set() {
      return new HashSet<>(read_Set);
   }

   private record Inner_DataUtil(int write, String init_Str) {
      static final int init_I = 0;
      static final int load_I = 1;
      static final int read = 2;
      static final ModNamespaceCatalog.Inner_DataUtil init_MethodUtil_DataUti = new ModNamespaceCatalog.Inner_DataUtil(0, null);
      static final ModNamespaceCatalog.Inner_DataUtil load_MethodUtil_DataUti = new ModNamespaceCatalog.Inner_DataUtil(1, null);

      static ModNamespaceCatalog.Inner_DataUtil init(String value) {
         return new ModNamespaceCatalog.Inner_DataUtil(2, value);
      }

      public int init_I() {
         return this.write;
      }
   }
}
