package autismclient.g;

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
   private static final Map<ResourcePackSessionTracker.Inner_BufferHelper, ServerPackSelectionRegistry.Inner_CachePool> init_Map = new IdentityHashMap<>();
   private static final ServerPackSelectionRegistry.Inner_DataUtil init_CharEncoder_DataUt = new ServerPackSelectionRegistry.Inner_DataUtil("", null, -1L, null, null);

   private ServerPackSelectionRegistry() {
   }

   public static void init(ResourcePackSessionTracker.Inner_BufferHelper session, Collection<IdAndPath> requested, Collection<Pack> loaded) {
      if (loaded != null && session != null && ResourcePackSessionTracker.init_Z(session)) {
         long revision = ResourcePackSessionTracker.init_J(session);
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
               if (revision >= 0L && revision == ResourcePackSessionTracker.init_J(session)) {
                  ServerPackSelectionRegistry.Inner_CachePool selection = init_Map.computeIfAbsent(session, ignored -> new ServerPackSelectionRegistry.Inner_CachePool());
                  selection.init.clear();
                  selection.init.putAll(replacement);
               }
            }
         }
      }
   }

   private static ServerPackSelectionRegistry.Inner_DataUtil init(ResourcePackSessionTracker.Inner_BufferHelper session, Collection<IdAndPath> requested, Pack pack) {
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

            long generation = ResourcePackSessionTracker.init_J(session, packId);
            ResourcePackSessionTracker.Inner_DataUtil policy = ResourcePackSessionTracker.init_FileCache_DataUtil(session, packId);

            try (Stream<PackResources> stream = pack.open()) {
               for (PackResources resources : stream.toList()) {
                  if (resources == null) {
                     return null;
                  }

                  if (resources instanceof IsolatedPackDownloader.Inner_BufferHelper isolated) {
                     IsolatedPackDownloader.Inner_ConfigStore binding = isolated.init();
                     if (policy != ResourcePackSessionTracker.Inner_DataUtil.ISOLATED
                        || binding.init_FileCache_BufferHe() != session
                        || !packId.equals(binding.init_UUID())
                        || binding.init_J() != generation
                        || !Objects.equals(path, binding.init_Path())) {
                        return init_CharEncoder_DataUt;
                     }
                  }
               }
            } catch (RuntimeException e) {
               return null;
            }

            if (policy == null) {
               generation = 0L;
               policy = ResourcePackSessionTracker.Inner_DataUtil.FULL;
            } else if (!ResourcePackSessionTracker.init_Z(session, packId, generation)) {
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

   public static ServerPackSelectionRegistry.Inner_StreamUtil init_CharEncoder_Stream(ResourcePackSessionTracker.Inner_BufferHelper session, PackResources resources) {
      if (resources != null && resources.location() != null) {
         ServerPackSelectionRegistry.Inner_StreamUtil provenance = init_CharEncoder_Stream(session, resources.location().id());
         if (provenance != null && resources instanceof IsolatedPackDownloader.Inner_BufferHelper isolated) {
            IsolatedPackDownloader.Inner_ConfigStore binding = isolated.init();
            if (provenance.init_FileCache_DataUtil() != ResourcePackSessionTracker.Inner_DataUtil.ISOLATED
               || binding.init_FileCache_BufferHe() != session
               || !provenance.init_UUID().equals(binding.init_UUID())
               || provenance.init_J() != binding.init_J()) {
               return null;
            }
         }

         return provenance;
      } else {
         return null;
      }
   }

   public static ServerPackSelectionRegistry.Inner_StreamUtil init_CharEncoder_Stream(ResourcePackSessionTracker.Inner_BufferHelper session, String repositoryId) {
      if (session != null && repositoryId != null) {
         synchronized (init_Obj) {
            if (!ResourcePackSessionTracker.init_Z(session)) {
               return null;
            } else {
               ServerPackSelectionRegistry.Inner_CachePool selection = init_Map.get(session);
               if (selection == null) {
                  return null;
               } else {
                  ServerPackSelectionRegistry.Inner_DataUtil pack = selection.init.get(repositoryId);
                  if (pack != null
                     && pack.init_J() > 0L
                     && ResourcePackSessionTracker.init_FileCache_DataUtil(session, pack.init_UUID()) != null
                     && !ResourcePackSessionTracker.init_Z(session, pack.init_UUID(), pack.init_J())) {
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

   static boolean init(ResourcePackSessionTracker.Inner_BufferHelper session, UUID packId, Path path) {
      synchronized (init_Obj) {
         if (!ResourcePackSessionTracker.init_Z(session)) {
            return false;
         }

         ServerPackSelectionRegistry.Inner_CachePool selection = init_Map.get(session);
         if (selection == null) {
            return false;
         }

         for (ServerPackSelectionRegistry.Inner_DataUtil pack : selection.init.values()) {
            if (pack.init_FileCache_DataUtil() == ResourcePackSessionTracker.Inner_DataUtil.FULL
               && packId.equals(pack.init_UUID())
               && path.equals(pack.init_Path())
               && init_CharEncoder_Stream(session, pack.init_Str()) != null) {
               return true;
            }
         }

         return false;
      }
   }

   private static void init(ResourcePackSessionTracker.Inner_BufferHelper session, long revision) {
      synchronized (init_Obj) {
         if (revision >= 0L && revision == ResourcePackSessionTracker.init_J(session)) {
            init_V(session, null);
         }
      }
   }

   public static ResourcePackSessionTracker.Inner_DataUtil init_FileCache_DataUtil(ResourcePackSessionTracker.Inner_BufferHelper session, PackResources resources) {
      ServerPackSelectionRegistry.Inner_StreamUtil provenance = init_CharEncoder_Stream(session, resources);
      return provenance == null ? null : provenance.init_FileCache_DataUtil();
   }

   public static ResourcePackSessionTracker.Inner_DataUtil init_FileCache_DataUtil(ResourcePackSessionTracker.Inner_BufferHelper session, String repositoryId) {
      ServerPackSelectionRegistry.Inner_StreamUtil provenance = init_CharEncoder_Stream(session, repositoryId);
      return provenance == null ? null : provenance.init_FileCache_DataUtil();
   }

   public static boolean init_Z(ResourcePackSessionTracker.Inner_BufferHelper session, PackResources resources) {
      return init_CharEncoder_Stream(session, resources) != null;
   }

   public static boolean init_Z(ResourcePackSessionTracker.Inner_BufferHelper session, String repositoryId) {
      return init_CharEncoder_Stream(session, repositoryId) != null;
   }

   public static boolean load(ResourcePackSessionTracker.Inner_BufferHelper session, PackResources resources) {
      return init_FileCache_DataUtil(session, resources) == ResourcePackSessionTracker.Inner_DataUtil.ISOLATED;
   }

   public static boolean load(ResourcePackSessionTracker.Inner_BufferHelper session, String repositoryId) {
      return init_FileCache_DataUtil(session, repositoryId) == ResourcePackSessionTracker.Inner_DataUtil.ISOLATED;
   }

   public static String init_Str(ResourcePackSessionTracker.Inner_BufferHelper session, UUID packId) {
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

   static void init_V(ResourcePackSessionTracker.Inner_BufferHelper session, UUID packId) {
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

   static void init(ResourcePackSessionTracker.Inner_BufferHelper session) {
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

   public record Inner_DataUtil(String init_Str, UUID init_UUID, long init_J, ResourcePackSessionTracker.Inner_DataUtil init_FileCache_DataUtil, Path init_Path) {
   }

   public record Inner_StreamUtil(String init_Str, UUID init_UUID, long init_J, ResourcePackSessionTracker.Inner_DataUtil init_FileCache_DataUtil) {
   }
}
