package autismclient.protector;

import java.nio.file.Path;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.client.resources.server.PackReloadConfig.IdAndPath;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;

public final class ServerPackSelectionRegistry {
   private static final Object init_Obj = new Object();
   private static final Map<ResourcePackSessionTracker.PackSession, ServerPackSelectionRegistry.Inner_CachePool> init_Map = new IdentityHashMap<>();
   private static final ServerPackSelectionRegistry.Inner_DataUtil init_CharEncoder_DataUt = new ServerPackSelectionRegistry.Inner_DataUtil("", null, -1L, null, null);

   private ServerPackSelectionRegistry() {
   }

   public static void init(ResourcePackSessionTracker.PackSession session, Collection<IdAndPath> requested, Collection<Pack> loaded) {
      if (loaded != null && session != null && ResourcePackSessionTracker.isActive(session)) {
         long revision = ResourcePackSessionTracker.revision(session);
         if (loaded.isEmpty()) {
            init(session, revision);
         } else {
            Map<String, ServerPackSelectionRegistry.Inner_DataUtil> replacement = new LinkedHashMap<>();

            for (Pack pack : loaded) {
               ServerPackSelectionRegistry.Inner_DataUtil entry = init(session, requested, pack);
               if (entry == init_CharEncoder_DataUt) {
                  return;
               }

               if (entry == null) {
                  init(session, revision);
                  return;
               }

               replacement.put(entry.init_Str(), entry);
            }

            synchronized (init_Obj) {
               if (revision >= 0L && revision == ResourcePackSessionTracker.revision(session)) {
                  ServerPackSelectionRegistry.Inner_CachePool selection = init_Map.computeIfAbsent(session, ignored -> new ServerPackSelectionRegistry.Inner_CachePool());
                  selection.init.clear();
                  selection.init.putAll(replacement);
               }
            }
         }
      }
   }

   private static ServerPackSelectionRegistry.Inner_DataUtil init(ResourcePackSessionTracker.PackSession session, Collection<IdAndPath> requested, Pack pack) {
      if (pack != null && pack.location() != null && pack.location().id() != null) {
         String repositoryId = pack.location().id();
         if (repositoryId.length() == 52 && repositoryId.startsWith("server/") && repositoryId.charAt(15) == '/') {
            for (int index = 7; index < 15; index++) {
               if (Character.digit(repositoryId.charAt(index), 16) < 0) {
                  return null;
               }
            }

            UUID repositoryPackId;
            try {
               repositoryPackId = UUID.fromString(repositoryId.substring(16));
            } catch (IllegalArgumentException e) {
               return null;
            }

            UUID packId = null;
            Path path = null;
            if (requested != null) {
               for (IdAndPath candidate : requested) {
                  if (candidate != null && repositoryPackId.equals(candidate.id())) {
                     packId = candidate.id();
                     path = candidate.path();
                     break;
                  }
               }
            }

            if (packId == null) {
               return null;
            }

            long generation = ResourcePackSessionTracker.generation(session, packId);
            ResourcePackSessionTracker.PackPolicy policy = ResourcePackSessionTracker.policyFor(session, packId);

            try (Stream<PackResources> stream = pack.open()) {
               for (PackResources resources : stream.toList()) {
                  if (resources == null) {
                     return null;
                  }

                  if (resources instanceof IsolatedPackDownloader.Inner_BufferHelper isolated) {
                     IsolatedPackDownloader.Inner_ConfigStore binding = isolated.init();
                     if (policy != ResourcePackSessionTracker.PackPolicy.ISOLATED
                        || binding.session() != session
                        || !packId.equals(binding.packId())
                        || binding.generation() != generation
                        || !Objects.equals(path, binding.path())) {
                        return init_CharEncoder_DataUt;
                     }
                  }
               }
            } catch (RuntimeException e) {
               return null;
            }

            if (policy == null) {
               generation = 0L;
               policy = ResourcePackSessionTracker.PackPolicy.FULL;
            } else if (!ResourcePackSessionTracker.isCurrent(session, packId, generation)) {
               return null;
            }

            return new ServerPackSelectionRegistry.Inner_DataUtil(repositoryId, packId, generation, policy, path);
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static ServerPackSelectionRegistry.Inner_StreamUtil provenanceOf(ResourcePackSessionTracker.PackSession session, PackResources resources) {
      if (resources != null && resources.location() != null) {
         ServerPackSelectionRegistry.Inner_StreamUtil provenance = init_CharEncoder_Stream(session, resources.location().id());
         if (provenance != null && resources instanceof IsolatedPackDownloader.Inner_BufferHelper isolated) {
            IsolatedPackDownloader.Inner_ConfigStore binding = isolated.init();
            if (provenance.init_FileCache_DataUtil() != ResourcePackSessionTracker.PackPolicy.ISOLATED
               || binding.session() != session
               || !provenance.init_UUID().equals(binding.packId())
               || provenance.init_J() != binding.generation()) {
               return null;
            }
         }

         return provenance;
      } else {
         return null;
      }
   }

   public static ServerPackSelectionRegistry.Inner_StreamUtil init_CharEncoder_Stream(ResourcePackSessionTracker.PackSession session, String repositoryId) {
      if (session != null && repositoryId != null) {
         synchronized (init_Obj) {
            if (!ResourcePackSessionTracker.isActive(session)) {
               return null;
            } else {
               ServerPackSelectionRegistry.Inner_CachePool selection = init_Map.get(session);
               if (selection == null) {
                  return null;
               } else {
                  ServerPackSelectionRegistry.Inner_DataUtil pack = selection.init.get(repositoryId);
                  if (pack != null
                     && pack.init_J() > 0L
                     && ResourcePackSessionTracker.policyFor(session, pack.init_UUID()) != null
                     && !ResourcePackSessionTracker.isCurrent(session, pack.init_UUID(), pack.init_J())) {
                     return null;
                  } else {
                     return pack == null
                        ? null
                        : new ServerPackSelectionRegistry.Inner_StreamUtil(pack.init_Str(), pack.init_UUID(), pack.init_J(), pack.init_FileCache_DataUtil());
                  }
               }
            }
         }
      } else {
         return null;
      }
   }

   static boolean init(ResourcePackSessionTracker.PackSession session, UUID packId, Path path) {
      synchronized (init_Obj) {
         if (!ResourcePackSessionTracker.isActive(session)) {
            return false;
         }

         ServerPackSelectionRegistry.Inner_CachePool selection = init_Map.get(session);
         if (selection == null) {
            return false;
         }

         for (ServerPackSelectionRegistry.Inner_DataUtil pack : selection.init.values()) {
            if (pack.init_FileCache_DataUtil() == ResourcePackSessionTracker.PackPolicy.FULL
               && packId.equals(pack.init_UUID())
               && path.equals(pack.init_Path())
               && init_CharEncoder_Stream(session, pack.init_Str()) != null) {
               return true;
            }
         }

         return false;
      }
   }

   private static void init(ResourcePackSessionTracker.PackSession session, long revision) {
      synchronized (init_Obj) {
         if (revision >= 0L && revision == ResourcePackSessionTracker.revision(session)) {
            init_V(session, null);
         }
      }
   }

   public static ResourcePackSessionTracker.PackPolicy init_FileCache_DataUtil(ResourcePackSessionTracker.PackSession session, PackResources resources) {
      ServerPackSelectionRegistry.Inner_StreamUtil provenance = provenanceOf(session, resources);
      return provenance == null ? null : provenance.init_FileCache_DataUtil();
   }

   public static ResourcePackSessionTracker.PackPolicy init_FileCache_DataUtil(ResourcePackSessionTracker.PackSession session, String repositoryId) {
      ServerPackSelectionRegistry.Inner_StreamUtil provenance = init_CharEncoder_Stream(session, repositoryId);
      return provenance == null ? null : provenance.init_FileCache_DataUtil();
   }

   public static boolean init_Z(ResourcePackSessionTracker.PackSession session, PackResources resources) {
      return provenanceOf(session, resources) != null;
   }

   public static boolean init_Z(ResourcePackSessionTracker.PackSession session, String repositoryId) {
      return init_CharEncoder_Stream(session, repositoryId) != null;
   }

   public static boolean load(ResourcePackSessionTracker.PackSession session, PackResources resources) {
      return init_FileCache_DataUtil(session, resources) == ResourcePackSessionTracker.PackPolicy.ISOLATED;
   }

   public static boolean load(ResourcePackSessionTracker.PackSession session, String repositoryId) {
      return init_FileCache_DataUtil(session, repositoryId) == ResourcePackSessionTracker.PackPolicy.ISOLATED;
   }

   public static String init_Str(ResourcePackSessionTracker.PackSession session, UUID packId) {
      if (session != null && packId != null) {
         synchronized (init_Obj) {
            ServerPackSelectionRegistry.Inner_CachePool selection = init_Map.get(session);
            if (selection == null) {
               return null;
            }

            for (ServerPackSelectionRegistry.Inner_DataUtil pack : selection.init.values()) {
               if (packId.equals(pack.init_UUID())) {
                  return pack.init_Str();
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }

   static void init_V(ResourcePackSessionTracker.PackSession session, UUID packId) {
      if (session != null) {
         synchronized (init_Obj) {
            ServerPackSelectionRegistry.Inner_CachePool selection = init_Map.get(session);
            if (selection != null) {
               if (packId == null) {
                  selection.init.clear();
               } else {
                  selection.init.values().removeIf(pack -> packId.equals(pack.init_UUID()));
               }
            }
         }
      }
   }

   static void init(ResourcePackSessionTracker.PackSession session) {
      if (session != null) {
         synchronized (init_Obj) {
            init_Map.remove(session);
         }
      }
   }

   static void init() {
      synchronized (init_Obj) {
         init_Map.clear();
      }
   }

   public static void init_V(UUID id) {
   }

   public static void init(UUID id, Object action) {
   }

   public static void load(UUID id) {
   }

   public static boolean init_Z(UUID id) {
      return false;
   }

   public static void load() {
   }

   private static final class Inner_CachePool {
      private final Map<String, ServerPackSelectionRegistry.Inner_DataUtil> init = new LinkedHashMap<>();
   }

   public record Inner_DataUtil(String init_Str, UUID init_UUID, long init_J, ResourcePackSessionTracker.PackPolicy init_FileCache_DataUtil, Path init_Path) {
   }

   public record Inner_StreamUtil(String init_Str, UUID init_UUID, long init_J, ResourcePackSessionTracker.PackPolicy init_FileCache_DataUtil) {
   }
}
