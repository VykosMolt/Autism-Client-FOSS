package autismclient.util;

import autismclient.AutismClientAddon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;

final class ServerPluginScanCache {
   static final int init_I = 128;
   static final long init_J = 8388608L;
   private static final Gson init_Gson = new GsonBuilder().setPrettyPrinting().create();
   private static final Logger init_Logger = LogUtils.getLogger();
   private static volatile ServerPluginScanCache init_ServerPluginScanCa;
   private final File init_File;
   private final String init_Str;
   private final Object init_Obj = new Object();
   private final LinkedHashMap<String, AutismConfig.PluginScanCacheEntry> init_LinkedHashMap = new LinkedHashMap<>();
   private final LinkedHashMap<String, Long> load_LinkedHashMap = new LinkedHashMap<>();
   private volatile Map<String, AutismConfig.PluginScanCacheEntry> init_Map = Map.of();
   private volatile Map<String, AutismConfig.PluginScanCacheEntry> load_Map = Map.of();
   private long load_J;
   private long read = -1L;
   private long write = 9223372036854775807L;

   private ServerPluginScanCache(File file) {
      this.init_File = file;
      this.init_Str = "plugin-scans:" + file.getAbsolutePath();
      this.read_V();
   }

   static ServerPluginScanCache init_ServerPluginScanCa() {
      ServerPluginScanCache current = init_ServerPluginScanCa;
      if (current != null) {
         return current;
      }

      synchronized (ServerPluginScanCache.class) {
         if (init_ServerPluginScanCa == null) {
            init_ServerPluginScanCa = new ServerPluginScanCache(new File(AutismClientAddon.FOLDER, "server-plugin-scans.json"));
         }

         return init_ServerPluginScanCa;
      }
   }

   static ServerPluginScanCache init_ServerPluginScanCa_File_2(File file) {
      return new ServerPluginScanCache(file);
   }

   Map<String, AutismConfig.PluginScanCacheEntry> init_Map() {
      return this.init_Map;
   }

   Map<String, AutismConfig.PluginScanCacheEntry> load_Map() {
      return this.load_Map;
   }

   synchronized AutismConfig.PluginScanCacheEntry init_AutismConfig_Plugi_Str(String key) {
      return init_AutismConfig_Plugi_AutismConfig_Plugi(this.init_LinkedHashMap.get(key));
   }

   synchronized AutismConfig.PluginScanCacheEntry load_AutismConfig_Plugi_Str(String normalizedAddress) {
      if (normalizedAddress != null && !normalizedAddress.isBlank()) {
         AutismConfig.PluginScanCacheEntry newest = null;

         for (Entry<String, AutismConfig.PluginScanCacheEntry> item : this.init_LinkedHashMap.entrySet()) {
            String key = item.getKey();
            if ((key.equals(normalizedAddress) || key.startsWith(normalizedAddress + "|"))
               && (newest == null || item.getValue().scannedAtMs > newest.scannedAtMs)) {
               newest = item.getValue();
            }
         }

         return init_AutismConfig_Plugi_AutismConfig_Plugi(newest);
      } else {
         return null;
      }
   }

   synchronized int init_I() {
      return this.init_LinkedHashMap.size();
   }

   synchronized long init_J() {
      return this.load_J;
   }

   synchronized Map<String, AutismConfig.PluginScanCacheEntry> read_Map() {
      return init_LinkedHashMap_Map(this.init_LinkedHashMap);
   }

   void init_V_Str_AutismConfig_Plugi(String key, AutismConfig.PluginScanCacheEntry entry) {
      if (key != null && !key.isBlank() && entry != null) {
         AutismConfig.PluginScanCacheEntry copied = init_AutismConfig_Plugi_AutismConfig_Plugi(entry);
         long serializedSize = init_J_Str_AutismConfig_Plugi(key, copied);
         synchronized (this) {
            this.init_LinkedHashMap.put(key, copied);
            this.load_LinkedHashMap.put(key, serializedSize);
            this.load_V();
            this.load_J++;
            this.init_V();
         }
      }
   }

   synchronized void init_V_Str(String key) {
      if (key != null && this.init_LinkedHashMap.remove(key) != null) {
         this.load_LinkedHashMap.remove(key);
         this.load_J++;
         this.init_V();
      }
   }

   synchronized void load_V_Str(String normalizedAddress) {
      if (normalizedAddress != null) {
         boolean changed = this.init_LinkedHashMap.keySet().removeIf(key -> key.equals(normalizedAddress) || key.startsWith(normalizedAddress + "|"));
         if (changed) {
            this.load_LinkedHashMap.keySet().retainAll(this.init_LinkedHashMap.keySet());
            this.load_J++;
            this.init_V();
         }
      }
   }

   boolean init_Z_Map(Map<String, AutismConfig.PluginScanCacheEntry> legacy) {
      if (legacy != null && !legacy.isEmpty()) {
         LinkedHashMap<String, AutismConfig.PluginScanCacheEntry> candidates = new LinkedHashMap<>();
         LinkedHashMap<String, Long> candidateSizes = new LinkedHashMap<>();

         for (Entry<String, AutismConfig.PluginScanCacheEntry> item : legacy.entrySet()) {
            if (item.getKey() != null && !item.getKey().isBlank() && item.getValue() != null) {
               AutismConfig.PluginScanCacheEntry copied = init_AutismConfig_Plugi_AutismConfig_Plugi(item.getValue());
               candidates.put(item.getKey(), copied);
               candidateSizes.put(item.getKey(), init_J_Str_AutismConfig_Plugi(item.getKey(), copied));
            }
         }

         if (candidates.isEmpty()) {
            return false;
         }

         Map<String, AutismConfig.PluginScanCacheEntry> snapshot;
         long revision;
         synchronized (this) {
            this.load_Map = Collections.unmodifiableMap(init_LinkedHashMap_Map(candidates));
            boolean changed = false;

            for (Entry<String, AutismConfig.PluginScanCacheEntry> item : candidates.entrySet()) {
               AutismConfig.PluginScanCacheEntry old = this.init_LinkedHashMap.get(item.getKey());
               if (old == null || item.getValue().scannedAtMs > old.scannedAtMs) {
                  this.init_LinkedHashMap.put(item.getKey(), item.getValue());
                  this.load_LinkedHashMap.put(item.getKey(), candidateSizes.get(item.getKey()));
                  changed = true;
               }
            }

            this.load_V();
            if (changed) {
               this.load_J++;
            }

            this.write = this.load_J;
            snapshot = this.write();
            revision = this.load_J;
         }

         return this.init_Z_Map_J(snapshot, revision);
      } else {
         return false;
      }
   }

   private void init_V() {
      Map<String, AutismConfig.PluginScanCacheEntry> snapshot = this.write();
      long revision = this.load_J;
      dj.init(this.init_Str, () -> this.init_Z_Map_J(snapshot, revision));
   }

   private Map<String, AutismConfig.PluginScanCacheEntry> write() {
      Map<String, AutismConfig.PluginScanCacheEntry> snapshot = init_LinkedHashMap_Map(this.init_LinkedHashMap);
      this.init_Map = Collections.unmodifiableMap(snapshot);
      return snapshot;
   }

   private void load_V() {
      if (!this.init_LinkedHashMap.isEmpty()) {
         List<Entry<String, AutismConfig.PluginScanCacheEntry>> newest = new ArrayList<>(this.init_LinkedHashMap.entrySet());
         newest.sort(
            Comparator.<Entry<String, AutismConfig.PluginScanCacheEntry>>comparingLong(entry -> entry.getValue() == null ? 0L : entry.getValue().scannedAtMs)
               .reversed()
         );
         LinkedHashMap<String, AutismConfig.PluginScanCacheEntry> retained = new LinkedHashMap<>();
         long bytes = 32L;

         for (Entry<String, AutismConfig.PluginScanCacheEntry> item : newest) {
            if (retained.size() >= 128) {
               break;
            }

            Long knownSize = this.load_LinkedHashMap.get(item.getKey());
            long entryBytes = knownSize == null ? 8388608L : knownSize;
            if (!retained.isEmpty() && bytes + entryBytes > 8388608L) {
               break;
            }

            retained.put(item.getKey(), item.getValue());
            bytes += entryBytes;
         }

         this.init_LinkedHashMap.clear();
         this.init_LinkedHashMap.putAll(retained);
         this.load_LinkedHashMap.keySet().retainAll(retained.keySet());
      }
   }

   private static long init_J_Str_AutismConfig_Plugi(String key, AutismConfig.PluginScanCacheEntry entry) {
      return init_Gson.toJson(Map.of(key, entry)).getBytes(StandardCharsets.UTF_8).length;
   }

   private void read_V() {
      ServerPluginScanCache.CacheFile cache = this.init_ServerPluginScanCa_File(this.init_File);
      if (cache == null) {
         File backup = this.init_File();
         cache = this.init_ServerPluginScanCa_File(backup);
      }

      if (cache != null && cache.entries != null) {
         this.init_LinkedHashMap.putAll(init_LinkedHashMap_Map(cache.entries));

         for (Entry<String, AutismConfig.PluginScanCacheEntry> item : this.init_LinkedHashMap.entrySet()) {
            this.load_LinkedHashMap.put(item.getKey(), init_J_Str_AutismConfig_Plugi(item.getKey(), item.getValue()));
         }

         this.load_V();
         if (!this.init_LinkedHashMap.isEmpty()) {
            this.load_J++;
         }

         this.write();
      }
   }

   private ServerPluginScanCache.CacheFile init_ServerPluginScanCa_File(File source) {
      if (source != null && source.exists()) {
         try (FileReader reader = new FileReader(source)) {
            ServerPluginScanCache.CacheFile loaded = init_Gson.fromJson(reader, ServerPluginScanCache.CacheFile.class);
            return loaded != null && loaded.entries != null ? loaded : null;
         } catch (Throwable t) {
            init_Logger.warn("Failed to read server plugin cache {}", source.getName(), t);
            return null;
         }
      } else {
         return null;
      }
   }

   private boolean init_Z_Map_J(Map<String, AutismConfig.PluginScanCacheEntry> snapshot, long revision) {
      synchronized (this) {
         if (revision < this.read) {
            return true;
         }
      }

      ServerPluginScanCache.CacheFile value = new ServerPluginScanCache.CacheFile();
      value.entries = snapshot;

      String json;
      try {
         json = init_Gson.toJson(value);
      } catch (Throwable t) {
         init_Logger.error("Failed to serialize server plugin cache", t);
         return false;
      }

      synchronized (this.init_Obj) {
         synchronized (this) {
            if (revision < this.read) {
               return true;
            }
         }

         File parent = this.init_File.getAbsoluteFile().getParentFile();
         if (parent != null && (parent.isDirectory() || parent.mkdirs())) {
            File tmp = new File(parent, this.init_File.getName() + ".tmp");

            try (FileWriter writer = new FileWriter(tmp)) {
               writer.write(json);
            } catch (Throwable t) {
               init_Logger.error("Failed to write server plugin cache", t);
               tmp.delete();
               return false;
            }

            try {
               if (this.init_File.exists() && this.init_ServerPluginScanCa_File(this.init_File) != null) {
                  Files.copy(this.init_File.toPath(), this.init_File().toPath(), StandardCopyOption.REPLACE_EXISTING);
               }
            } catch (Throwable t) {
               init_Logger.warn("Failed to back up server plugin cache", t);
            }

            boolean persisted = init(tmp, this.init_File, "server plugin cache") && this.init_File.isFile();
            if (persisted) {
               this.init_V_J(revision);
            }

            return persisted;
         } else {
            init_Logger.error("Failed to create server plugin cache directory {}", parent);
            return false;
         }
      }
   }

   private synchronized void init_V_J(long revision) {
      this.read = Math.max(this.read, revision);
      if (!this.load_Map.isEmpty() && revision >= this.write) {
         this.load_Map = Map.of();
         this.write = 9223372036854775807L;
      }
   }

   private File init_File() {
      return new File(this.init_File.getParentFile(), this.init_File.getName() + ".bak");
   }

   private static boolean init(File tmp, File target, String description) {
      try {
         Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
         return true;
      } catch (Throwable atomicFailed) {
         try {
            Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
         } catch (Throwable t) {
            init_Logger.error("Failed to swap in {}", description, t);
            tmp.delete();
            return false;
         }
      }
   }

   private static LinkedHashMap<String, AutismConfig.PluginScanCacheEntry> init_LinkedHashMap_Map(Map<String, AutismConfig.PluginScanCacheEntry> source) {
      LinkedHashMap<String, AutismConfig.PluginScanCacheEntry> copy = new LinkedHashMap<>();
      if (source == null) {
         return copy;
      }

      for (Entry<String, AutismConfig.PluginScanCacheEntry> item : source.entrySet()) {
         if (item.getKey() != null && item.getValue() != null) {
            copy.put(item.getKey(), init_AutismConfig_Plugi_AutismConfig_Plugi(item.getValue()));
         }
      }

      return copy;
   }

   private static AutismConfig.PluginScanCacheEntry init_AutismConfig_Plugi_AutismConfig_Plugi(AutismConfig.PluginScanCacheEntry source) {
      if (source == null) {
         return null;
      }

      AutismConfig.PluginScanCacheEntry copy = new AutismConfig.PluginScanCacheEntry();
      copy.contextSignature = source.contextSignature == null ? "" : source.contextSignature;
      copy.serverName = source.serverName == null ? "" : source.serverName;
      copy.serverAddress = source.serverAddress == null ? "" : source.serverAddress;
      copy.plugins = source.plugins == null ? new ArrayList<>() : new ArrayList<>(source.plugins);
      copy.commands = load_LinkedHashMap_Map(source.commands);
      copy.evidence = source.evidence == null ? new LinkedHashMap<>() : new LinkedHashMap<>(source.evidence);
      copy.channels = load_LinkedHashMap_Map(source.channels);
      copy.guis = load_LinkedHashMap_Map(source.guis);
      copy.confidence = source.confidence == null ? new LinkedHashMap<>() : new LinkedHashMap<>(source.confidence);
      copy.copyEvidence = source.copyEvidence == null ? new LinkedHashMap<>() : new LinkedHashMap<>(source.copyEvidence);
      copy.copyCommands = load_LinkedHashMap_Map(source.copyCommands);
      copy.scanStatus = source.scanStatus == null ? "COMPLETE" : source.scanStatus;
      copy.totalProbes = source.totalProbes;
      copy.answeredProbes = source.answeredProbes;
      copy.retriedProbes = source.retriedProbes;
      copy.failedProbes = source.failedProbes;
      copy.scannedAtMs = source.scannedAtMs;
      return copy;
   }

   private static LinkedHashMap<String, List<String>> load_LinkedHashMap_Map(Map<String, List<String>> source) {
      LinkedHashMap<String, List<String>> copy = new LinkedHashMap<>();
      if (source == null) {
         return copy;
      }

      for (Entry<String, List<String>> item : source.entrySet()) {
         copy.put(item.getKey(), item.getValue() == null ? new ArrayList<>() : new ArrayList<>(item.getValue()));
      }

      return copy;
   }

   private static final class CacheFile {
      int version = 1;
      Map<String, AutismConfig.PluginScanCacheEntry> entries;
   }
}
