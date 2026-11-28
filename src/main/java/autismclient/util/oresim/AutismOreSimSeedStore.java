package autismclient.util.oresim;

import autismclient.AutismClientAddon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AutismOreSimSeedStore {
   private static final int init_I = 1;
   private static final int load_I = 2048;
   private static final long init_J = 400L;
   private static final Gson init_Gson = new GsonBuilder().setPrettyPrinting().create();
   private static final Type init_Type = (new TypeToken<Map<String, String>>() {}).getType();
   private static final ScheduledExecutorService init_ScheduledExecutorS = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread thread = new Thread(r, "ore-sim-seeds-writer");
      thread.setDaemon(true);
      return thread;
   });
   private static volatile AutismOreSimSeedStore init_AutismOreSimSeedSt;
   private final File init_File;
   private final File load_File;
   private final Map<String, String> init_Map = new LinkedHashMap<>();
   private final AtomicBoolean init_AtomicBoolean = new AtomicBoolean(false);
   private final AtomicBoolean load_AtomicBoolean = new AtomicBoolean(false);
   private boolean init_Z;
   private int auditSaveFailures;
   private boolean auditRetryPending;

   private AutismOreSimSeedStore(File file) {
      this.init_File = file;
      this.load_File = new File(file.getParentFile(), file.getName() + ".tmp");
   }

   public static AutismOreSimSeedStore getInstance() {
      AutismOreSimSeedStore local = init_AutismOreSimSeedSt;
      if (local == null) {
         synchronized (AutismOreSimSeedStore.class) {
            if (init_AutismOreSimSeedSt == null) {
               init_AutismOreSimSeedSt = loadFrom(new File(AutismClientAddon.FOLDER, "ore-sim-seeds.json"));
               Runtime.getRuntime().addShutdownHook(new Thread(init_AutismOreSimSeedSt::saveIfDirty, "ore-sim-seeds-flush"));
            }

            local = init_AutismOreSimSeedSt;
         }
      }

      return local;
   }

   public String seedFor(String scope, String legacyGlobalValue) {
      String key = init_Str_Str(scope);
      if (key == null) {
         return "";
      }

      boolean changed = false;
      String result;
      synchronized (this.init_Map) {
         if (!this.init_Z) {
            this.init_Z = true;
            if (!this.init_Map.containsKey(key) && legacyGlobalValue != null && !legacyGlobalValue.isBlank()) {
               this.init_Map.put(key, legacyGlobalValue);
            }

            changed = true;
         }

         result = this.init_Map.getOrDefault(key, "");
      }

      if (changed) {
         this.scheduleSave();
      }

      return result;
   }

   public void setSeed(String scope, String rawValue) {
      String key = init_Str_Str(scope);
      if (key != null) {
         String value = rawValue == null ? "" : rawValue;
         boolean changed;
         synchronized (this.init_Map) {
            changed = !this.init_Z;
            this.init_Z = true;
            if (value.isBlank()) {
               changed |= this.init_Map.remove(key) != null;
            } else if (this.init_Map.containsKey(key) || this.init_Map.size() < 2048) {
               changed |= !Objects.equals(this.init_Map.put(key, value), value);
            }
         }

         if (changed) {
            this.scheduleSave();
         }
      }
   }

   private static String init_Str_Str(String scope) {
      if (scope != null && !scope.isBlank()) {
         String normalized = scope.trim();
         return "unknown".equals(normalized) ? null : normalized;
      } else {
         return null;
      }
   }

   private void scheduleSave() {
      this.init_AtomicBoolean.set(true);
      if (this.load_AtomicBoolean.compareAndSet(false, true)) {
         try {
            init_ScheduledExecutorS.schedule(() -> {
               this.load_AtomicBoolean.set(false);
               this.saveIfDirty();
            }, 400L, TimeUnit.MILLISECONDS);
         } catch (Throwable unavailable) {
            this.load_AtomicBoolean.set(false);
            this.saveIfDirty();
         }
      }
   }

   private void saveIfDirty() {
      if (this.init_AtomicBoolean.compareAndSet(true, false)) {
         this.writeToDisk();
      }
   }

   private synchronized void writeToDisk() {
      try {
         File directory = this.init_File.getParentFile();
         if (directory != null) {
            Files.createDirectories(directory.toPath());
         }

         AutismOreSimSeedStore.Persisted snapshot = new AutismOreSimSeedStore.Persisted();
         snapshot.version = 1;
         synchronized (this.init_Map) {
            snapshot.legacyMigrationComplete = this.init_Z;
            snapshot.seeds = new LinkedHashMap<>(this.init_Map);
         }

         try (Writer writer = Files.newBufferedWriter(this.load_File.toPath(), StandardCharsets.UTF_8)) {
            init_Gson.toJson(snapshot, writer);
         }

         try {
            Files.move(this.load_File.toPath(), this.init_File.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
         } catch (AtomicMoveNotSupportedException unsupported) {
            Files.move(this.load_File.toPath(), this.init_File.toPath(), StandardCopyOption.REPLACE_EXISTING);
         }

         this.auditSaveFailures = 0;
      } catch (Throwable failure) {
         this.init_AtomicBoolean.set(true);
         AutismClientAddon.LOG.warn("Failed to save scoped OreSim seeds", failure);
         long retryMs = Math.min(60000L, 1000L << Math.min(this.auditSaveFailures++, 6));
         if (!this.auditRetryPending) {
            this.auditRetryPending = true;
            init_ScheduledExecutorS.schedule(() -> {
               synchronized (this) {
                  this.auditRetryPending = false;
               }

               this.saveIfDirty();
            }, retryMs, TimeUnit.MILLISECONDS);
         }
      }
   }

   private static AutismOreSimSeedStore loadFrom(File file) {
      AutismOreSimSeedStore store = new AutismOreSimSeedStore(file);
      if (!file.exists()) {
         return store;
      }

      try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
         JsonElement root = JsonParser.parseReader(reader);
         if (root instanceof JsonObject object && object.has("seeds")) {
            AutismOreSimSeedStore.Persisted parsed = init_Gson.fromJson(object, AutismOreSimSeedStore.Persisted.class);
            if (parsed != null) {
               store.init_Z = parsed.legacyMigrationComplete;
               store.init_V_Map(parsed.seeds);
            }
         } else {
            Map<String, String> legacyMap = init_Gson.fromJson(root, init_Type);
            store.init_V_Map(legacyMap);
            store.init_Z = true;
         }
      } catch (Throwable failure) {
         AutismClientAddon.LOG.warn("Failed to read scoped OreSim seeds", failure);
      }

      return store;
   }

   private void init_V_Map(Map<String, String> entries) {
      if (entries != null) {
         for (Entry<String, String> entry : entries.entrySet()) {
            String key = init_Str_Str(entry.getKey());
            String value = entry.getValue();
            if (key != null && value != null && !value.isBlank()) {
               this.init_Map.put(key, value);
               if (this.init_Map.size() >= 2048) {
                  break;
               }
            }
         }
      }
   }

   static AutismOreSimSeedStore init_AutismOreSimSeedSt_File(File file) {
      return loadFrom(file);
   }

   void init_V() {
      this.saveIfDirty();
   }

   private static final class Persisted {
      int version = 1;
      boolean legacyMigrationComplete;
      Map<String, String> seeds = new LinkedHashMap<>();
   }
}
