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

public final class CharEncoder {
   private static final Object init_Obj = new Object();
   private static final Map<FileCache.Inner_BufferHelper, CharEncoder.Inner_CachePool> init_Map = new IdentityHashMap<>();
   private static final CharEncoder.Inner_DataUtil init_CharEncoder_DataUt = new CharEncoder.Inner_DataUtil("", null, -1L, null, null);

   private CharEncoder() {
   }

   public static void init(FileCache.Inner_BufferHelper session, Collection<IdAndPath> requested, Collection<Pack> loaded) {
      if (loaded != null && session != null && FileCache.init_Z(session)) {
         long revision = FileCache.init_J(session);
         if (loaded.isEmpty()) {
            init(session, revision);
         } else {
            Map<String, CharEncoder.Inner_DataUtil> replacement = new LinkedHashMap<>();

            for (Pack pack : loaded) {
               CharEncoder.Inner_DataUtil entry = init(session, requested, pack);
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
               if (revision >= 0L && revision == FileCache.init_J(session)) {
                  CharEncoder.Inner_CachePool selection = init_Map.computeIfAbsent(session, ignored -> new CharEncoder.Inner_CachePool());
                  selection.init.clear();
                  selection.init.putAll(replacement);
               }
            }
         }
      }
   }

   private static CharEncoder.Inner_DataUtil init(FileCache.Inner_BufferHelper session, Collection<IdAndPath> requested, Pack pack) {
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

            long generation = FileCache.init_J(session, packId);
            FileCache.Inner_DataUtil policy = FileCache.init_FileCache_DataUtil(session, packId);

            try (Stream<PackResources> stream = pack.open()) {
               for (PackResources resources : stream.toList()) {
                  if (resources == null) {
                     return null;
                  }

                  if (resources instanceof EventBus.Inner_BufferHelper isolated) {
                     EventBus.Inner_ConfigStore binding = isolated.init();
                     if (policy != FileCache.Inner_DataUtil.init
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
               policy = FileCache.Inner_DataUtil.load;
            } else if (!FileCache.init_Z(session, packId, generation)) {
               return null;
            }

            return new CharEncoder.Inner_DataUtil(repositoryId, packId, generation, policy, path);
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static CharEncoder.Inner_StreamUtil init_CharEncoder_Stream(FileCache.Inner_BufferHelper session, PackResources resources) {
      if (resources != null && resources.location() != null) {
         CharEncoder.Inner_StreamUtil provenance = init_CharEncoder_Stream(session, resources.location().id());
         if (provenance != null && resources instanceof EventBus.Inner_BufferHelper isolated) {
            EventBus.Inner_ConfigStore binding = isolated.init();
            if (provenance.init_FileCache_DataUtil() != FileCache.Inner_DataUtil.init
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

   public static CharEncoder.Inner_StreamUtil init_CharEncoder_Stream(FileCache.Inner_BufferHelper session, String repositoryId) {
      if (session != null && repositoryId != null) {
         synchronized (init_Obj) {
            if (!FileCache.init_Z(session)) {
               return null;
            } else {
               CharEncoder.Inner_CachePool selection = init_Map.get(session);
               if (selection == null) {
                  return null;
               } else {
                  CharEncoder.Inner_DataUtil pack = selection.init.get(repositoryId);
                  if (pack != null
                     && pack.init_J() > 0L
                     && FileCache.init_FileCache_DataUtil(session, pack.init_UUID()) != null
                     && !FileCache.init_Z(session, pack.init_UUID(), pack.init_J())) {
                     return null;
                  } else {
                     return pack == null
                        ? null
                        : new CharEncoder.Inner_StreamUtil(pack.init_Str(), pack.init_UUID(), pack.init_J(), pack.init_FileCache_DataUtil());
                  }
               }
            }
         }
      } else {
         return null;
      }
   }

   static boolean init(FileCache.Inner_BufferHelper session, UUID packId, Path path) {
      synchronized (init_Obj) {
         if (!FileCache.init_Z(session)) {
            return false;
         }

         CharEncoder.Inner_CachePool selection = init_Map.get(session);
         if (selection == null) {
            return false;
         }

         for (CharEncoder.Inner_DataUtil pack : selection.init.values()) {
            if (pack.init_FileCache_DataUtil() == FileCache.Inner_DataUtil.load
               && packId.equals(pack.init_UUID())
               && path.equals(pack.init_Path())
               && init_CharEncoder_Stream(session, pack.init_Str()) != null) {
               return true;
            }
         }

         return false;
      }
   }

   private static void init(FileCache.Inner_BufferHelper session, long revision) {
      synchronized (init_Obj) {
         if (revision >= 0L && revision == FileCache.init_J(session)) {
            init_V(session, null);
         }
      }
   }

   public static FileCache.Inner_DataUtil init_FileCache_DataUtil(FileCache.Inner_BufferHelper session, PackResources resources) {
      CharEncoder.Inner_StreamUtil provenance = init_CharEncoder_Stream(session, resources);
      return provenance == null ? null : provenance.init_FileCache_DataUtil();
   }

   public static FileCache.Inner_DataUtil init_FileCache_DataUtil(FileCache.Inner_BufferHelper session, String repositoryId) {
      CharEncoder.Inner_StreamUtil provenance = init_CharEncoder_Stream(session, repositoryId);
      return provenance == null ? null : provenance.init_FileCache_DataUtil();
   }

   public static boolean init_Z(FileCache.Inner_BufferHelper session, PackResources resources) {
      return init_CharEncoder_Stream(session, resources) != null;
   }

   public static boolean init_Z(FileCache.Inner_BufferHelper session, String repositoryId) {
      return init_CharEncoder_Stream(session, repositoryId) != null;
   }

   public static boolean load(FileCache.Inner_BufferHelper session, PackResources resources) {
      return init_FileCache_DataUtil(session, resources) == FileCache.Inner_DataUtil.init;
   }

   public static boolean load(FileCache.Inner_BufferHelper session, String repositoryId) {
      return init_FileCache_DataUtil(session, repositoryId) == FileCache.Inner_DataUtil.init;
   }

   public static String init_Str(FileCache.Inner_BufferHelper session, UUID packId) {
      if (session != null && packId != null) {
         synchronized (init_Obj) {
            CharEncoder.Inner_CachePool selection = init_Map.get(session);
            if (selection == null) {
               return null;
            }

            for (CharEncoder.Inner_DataUtil pack : selection.init.values()) {
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

   static void init_V(FileCache.Inner_BufferHelper session, UUID packId) {
      if (session != null) {
         synchronized (init_Obj) {
            CharEncoder.Inner_CachePool selection = init_Map.get(session);
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

   static void init(FileCache.Inner_BufferHelper session) {
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
      private final Map<String, CharEncoder.Inner_DataUtil> init = new LinkedHashMap<>();
   }

   public record Inner_DataUtil(String init_Str, UUID init_UUID, long init_J, FileCache.Inner_DataUtil init_FileCache_DataUtil, Path init_Path) {
   }

   public record Inner_StreamUtil(String init_Str, UUID init_UUID, long init_J, FileCache.Inner_DataUtil init_FileCache_DataUtil) {
   }
}
