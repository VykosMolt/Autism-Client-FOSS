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
   private static final Object LOCK = new Object();
   private static final Map<ResourcePackSessionTracker.PackSession, ServerPackSelectionRegistry.SessionSelection> SELECTIONS = new IdentityHashMap<>();
   private static final ServerPackSelectionRegistry.SelectedPack NONE = new ServerPackSelectionRegistry.SelectedPack("", null, -1L, null, null);

   private ServerPackSelectionRegistry() {
   }

   public static void recordSelection(ResourcePackSessionTracker.PackSession session, Collection<IdAndPath> requested, Collection<Pack> loaded) {
      if (loaded != null && session != null && ResourcePackSessionTracker.isActive(session)) {
         long revision = ResourcePackSessionTracker.revision(session);
         if (loaded.isEmpty()) {
            dropIfStale(session, revision);
         } else {
            Map<String, ServerPackSelectionRegistry.SelectedPack> replacement = new LinkedHashMap<>();

            for (Pack pack : loaded) {
               ServerPackSelectionRegistry.SelectedPack entry = select(session, requested, pack);
               if (entry == NONE) {
                  return;
               }

               if (entry == null) {
                  dropIfStale(session, revision);
                  return;
               }

               replacement.put(entry.id(), entry);
            }

            synchronized (LOCK) {
               if (revision >= 0L && revision == ResourcePackSessionTracker.revision(session)) {
                  ServerPackSelectionRegistry.SessionSelection selection = SELECTIONS.computeIfAbsent(session, ignored -> new ServerPackSelectionRegistry.SessionSelection());
                  selection.byRepository.clear();
                  selection.byRepository.putAll(replacement);
               }
            }
         }
      }
   }

   private static ServerPackSelectionRegistry.SelectedPack select(ResourcePackSessionTracker.PackSession session, Collection<IdAndPath> requested, Pack pack) {
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

                  if (resources instanceof IsolatedPackDownloader.IsolationAware isolated) {
                     IsolatedPackDownloader.PackIsolation binding = isolated.packIsolation();
                     if (policy != ResourcePackSessionTracker.PackPolicy.ISOLATED
                        || binding.session() != session
                        || !packId.equals(binding.packId())
                        || binding.generation() != generation
                        || !Objects.equals(path, binding.path())) {
                        return NONE;
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

            return new ServerPackSelectionRegistry.SelectedPack(repositoryId, packId, generation, policy, path);
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static ServerPackSelectionRegistry.Provenance provenanceOf(ResourcePackSessionTracker.PackSession session, PackResources resources) {
      if (resources != null && resources.location() != null) {
         ServerPackSelectionRegistry.Provenance provenance = init_CharEncoder_Stream(session, resources.location().id());
         if (provenance != null && resources instanceof IsolatedPackDownloader.IsolationAware isolated) {
            IsolatedPackDownloader.PackIsolation binding = isolated.packIsolation();
            if (provenance.policy() != ResourcePackSessionTracker.PackPolicy.ISOLATED
               || binding.session() != session
               || !provenance.packId().equals(binding.packId())
               || provenance.generation() != binding.generation()) {
               return null;
            }
         }

         return provenance;
      } else {
         return null;
      }
   }

   public static ServerPackSelectionRegistry.Provenance init_CharEncoder_Stream(ResourcePackSessionTracker.PackSession session, String repositoryId) {
      if (session != null && repositoryId != null) {
         synchronized (LOCK) {
            if (!ResourcePackSessionTracker.isActive(session)) {
               return null;
            } else {
               ServerPackSelectionRegistry.SessionSelection selection = SELECTIONS.get(session);
               if (selection == null) {
                  return null;
               } else {
                  ServerPackSelectionRegistry.SelectedPack pack = selection.byRepository.get(repositoryId);
                  if (pack != null
                     && pack.generation() > 0L
                     && ResourcePackSessionTracker.policyFor(session, pack.packId()) != null
                     && !ResourcePackSessionTracker.isCurrent(session, pack.packId(), pack.generation())) {
                     return null;
                  } else {
                     return pack == null
                        ? null
                        : new ServerPackSelectionRegistry.Provenance(pack.id(), pack.packId(), pack.generation(), pack.policy());
                  }
               }
            }
         }
      } else {
         return null;
      }
   }

   static boolean init(ResourcePackSessionTracker.PackSession session, UUID packId, Path path) {
      synchronized (LOCK) {
         if (!ResourcePackSessionTracker.isActive(session)) {
            return false;
         }

         ServerPackSelectionRegistry.SessionSelection selection = SELECTIONS.get(session);
         if (selection == null) {
            return false;
         }

         for (ServerPackSelectionRegistry.SelectedPack pack : selection.byRepository.values()) {
            if (pack.policy() == ResourcePackSessionTracker.PackPolicy.FULL
               && packId.equals(pack.packId())
               && path.equals(pack.path())
               && init_CharEncoder_Stream(session, pack.id()) != null) {
               return true;
            }
         }

         return false;
      }
   }

   private static void dropIfStale(ResourcePackSessionTracker.PackSession session, long revision) {
      synchronized (LOCK) {
         if (revision >= 0L && revision == ResourcePackSessionTracker.revision(session)) {
            init_V(session, null);
         }
      }
   }

   public static ResourcePackSessionTracker.PackPolicy init_FileCache_DataUtil(ResourcePackSessionTracker.PackSession session, PackResources resources) {
      ServerPackSelectionRegistry.Provenance provenance = provenanceOf(session, resources);
      return provenance == null ? null : provenance.policy();
   }

   public static ResourcePackSessionTracker.PackPolicy init_FileCache_DataUtil(ResourcePackSessionTracker.PackSession session, String repositoryId) {
      ServerPackSelectionRegistry.Provenance provenance = init_CharEncoder_Stream(session, repositoryId);
      return provenance == null ? null : provenance.policy();
   }

   public static boolean init_Z(ResourcePackSessionTracker.PackSession session, PackResources resources) {
      return provenanceOf(session, resources) != null;
   }

   public static boolean init_Z(ResourcePackSessionTracker.PackSession session, String repositoryId) {
      return init_CharEncoder_Stream(session, repositoryId) != null;
   }

   public static boolean isSelectedPack(ResourcePackSessionTracker.PackSession session, PackResources resources) {
      return init_FileCache_DataUtil(session, resources) == ResourcePackSessionTracker.PackPolicy.ISOLATED;
   }

   public static boolean isSelectedRepository(ResourcePackSessionTracker.PackSession session, String repositoryId) {
      return init_FileCache_DataUtil(session, repositoryId) == ResourcePackSessionTracker.PackPolicy.ISOLATED;
   }

   public static String init_Str(ResourcePackSessionTracker.PackSession session, UUID packId) {
      if (session != null && packId != null) {
         synchronized (LOCK) {
            ServerPackSelectionRegistry.SessionSelection selection = SELECTIONS.get(session);
            if (selection == null) {
               return null;
            }

            for (ServerPackSelectionRegistry.SelectedPack pack : selection.byRepository.values()) {
               if (packId.equals(pack.packId())) {
                  return pack.id();
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
         synchronized (LOCK) {
            ServerPackSelectionRegistry.SessionSelection selection = SELECTIONS.get(session);
            if (selection != null) {
               if (packId == null) {
                  selection.byRepository.clear();
               } else {
                  selection.byRepository.values().removeIf(pack -> packId.equals(pack.packId()));
               }
            }
         }
      }
   }

   static void forget(ResourcePackSessionTracker.PackSession session) {
      if (session != null) {
         synchronized (LOCK) {
            SELECTIONS.remove(session);
         }
      }
   }

   static void clear() {
      synchronized (LOCK) {
         SELECTIONS.clear();
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

   private static final class SessionSelection {
      private final Map<String, ServerPackSelectionRegistry.SelectedPack> byRepository = new LinkedHashMap<>();
   }

   public record SelectedPack(String id, UUID packId, long generation, ResourcePackSessionTracker.PackPolicy policy, Path path) {
   }

   public record Provenance(String id, UUID packId, long generation, ResourcePackSessionTracker.PackPolicy policy) {
   }
}
