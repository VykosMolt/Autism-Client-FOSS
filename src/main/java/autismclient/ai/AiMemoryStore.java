package autismclient.ai;

import autismclient.api.ai.AiObservation;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Stream;

public final class AiMemoryStore {
   private final Path init_Path;
   private final Map<String, AiMemoryStore.Memory> init_Map = new LinkedHashMap<>();
   private final Map<String, Integer> load_Map = new HashMap<>();
   private final Map<String, AiObservation> read_Map = new LinkedHashMap<>();
   private volatile List<AiMemoryStore.Memory> init_List = List.of();
   private volatile Map<String, Integer> write_Map = Map.of();
   private volatile String init_Str = "";
   static final int init_I = 4000;
   int load_I = 4000;
   static final int read_I = 6;
   static final int write_I = 8;
   private static final Set<String> init_Set = Set.of(
      "the",
      "and",
      "for",
      "with",
      "then",
      "than",
      "that",
      "this",
      "from",
      "into",
      "onto",
      "you",
      "your",
      "get",
      "got",
      "make",
      "some",
      "any",
      "all",
      "use",
      "can",
      "please",
      "now",
      "are",
      "was",
      "not",
      "but",
      "out",
      "its",
      "too",
      "very",
      "just"
   );

   public AiMemoryStore(Path directory) {
      this.init_Path = directory;
   }

   public synchronized void reload() throws IOException {
      this.pruneExpired();
      Path file = this.init_Path.resolve("memory.json");
      if (Files.exists(file)) {
         try {
            if (Files.size(file) > 16000000L) {
               throw new IllegalArgumentException("Memory snapshot exceeds supported size");
            }

            JsonObject root = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            if (AiJson.getInt(root, "version", 0) != 1) {
               throw new IllegalArgumentException("Unsupported memory version");
            }

            Map<String, AiMemoryStore.Memory> loaded = new LinkedHashMap<>();

            for (JsonElement e : root.getAsJsonArray("records")) {
               AiMemoryStore.Memory m = AiJson.GSON.fromJson(e, AiMemoryStore.Memory.class);
               if (m == null || m.id() == null || m.id().isBlank() || m.kind() == null || m.scope() == null || m.text() == null || m.catalogRevision() == null) {
                  throw new IllegalArgumentException("Incomplete memory record");
               }

               loaded.put(m.id(), m);
            }

            Map<String, Integer> loadedEpochs = new HashMap<>();
            root.getAsJsonObject("epochs").entrySet().forEach(ex -> loadedEpochs.put(ex.getKey(), ex.getValue().getAsInt()));
            this.init_Map.clear();
            this.init_Map.putAll(loaded);
            this.load_Map.clear();
            this.load_Map.putAll(loadedEpochs);
            this.refreshSnapshots();
         } catch (RuntimeException e) {
            Path backup = this.init_Path.resolve("memory-corrupt-" + System.currentTimeMillis() + ".json");

            try {
               Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException var7) {
            }

            this.trimBackups();
            this.init_Map.clear();
            this.load_Map.clear();
            this.refreshSnapshots();
            this.init_Str = "Memory could not be loaded; preserved as " + backup.getFileName();
         }
      }
   }

   private void pruneExpired() {
      try (Stream<Path> entries = Files.list(this.init_Path)) {
         LocalDate cutoff = LocalDate.now().minusDays(14L);

         for (Path path : (Iterable<Path>)entries::iterator) {
            String name = path.getFileName().toString();
            if (name.startsWith("journal-") && name.endsWith(".jsonl") && name.length() >= 18) {
               try {
                  if (LocalDate.parse(name.substring(8, 18)).isBefore(cutoff)) {
                     Files.deleteIfExists(path);
                  }
               } catch (Exception var8) {
               }
            }
         }
      } catch (Exception var10) {
      }
   }

   private void trimBackups() {
      try (Stream<Path> entries = Files.list(this.init_Path)) {
         List<Path> backups = entries.filter(x$0 -> Files.isRegularFile(x$0))
            .filter(p -> p.getFileName().toString().startsWith("memory-corrupt-"))
            .sorted()
            .toList();

         for (int i = 0; i < backups.size() - 5; i++) {
            try {
               Files.deleteIfExists(backups.get(i));
            } catch (Exception var6) {
            }
         }
      } catch (Exception var8) {
      }
   }

   static void prune(Path directory, int keep) {
      try (Stream<Path> entries = Files.list(directory)) {
         for (Path path : entries.filter(x$0 -> Files.isRegularFile(x$0))
            .sorted(Comparator.<Path>comparingLong(p -> p.toFile().lastModified()).reversed())
            .skip(keep)
            .toList()) {
            try {
               Files.deleteIfExists(path);
            } catch (Exception var7) {
            }
         }
      } catch (Exception var9) {
      }
   }

   public List<AiMemoryStore.Memory> memories() {
      return this.init_List;
   }

   public String systemNote() {
      return this.init_Str;
   }

   public String epochKey(String world) {
      return world + "/epoch/" + this.write_Map.getOrDefault(world, 0);
   }

   public synchronized void startEpoch(String world) throws IOException {
      Integer previous = this.load_Map.get(world);
      this.load_Map.merge(world, 1, Integer::sum);

      try {
         this.save();
      } catch (IOException | RuntimeException e) {
         if (previous == null) {
            this.load_Map.remove(world);
         } else {
            this.load_Map.put(world, previous);
         }

         throw e;
      }
   }

   public synchronized void init(AiObservation observation) throws IOException {
      this.read_Map.put(observation.id(), observation);

      while (this.read_Map.size() > 512) {
         this.read_Map.remove(this.read_Map.keySet().iterator().next());
      }

      this.journal("observation", AiJson.GSON.toJsonTree(observation));
   }

   public synchronized AiMemoryStore.Memory init(
      AiMemoryStore.Inner_DataUtil kind, String scope, String text, String evidenceId, String revision, String payload
   ) throws IOException {
      if (text == null || text.isBlank() || text.length() > 16000) {
         throw new IllegalArgumentException("Invalid memory text");
      }

      if (payload != null && payload.length() <= 256000 && scope != null && scope.length() <= 2048) {
         AiObservation proof = this.read_Map.get(evidenceId);
         if (kind == AiMemoryStore.Inner_DataUtil.HYPOTHESIS || proof != null && (scope.equals("user") || scope.equals(proof.scope()))) {
            if (kind == AiMemoryStore.Inner_DataUtil.USER && !"user".equals(proof.source())) {
               throw new IllegalArgumentException("User preferences require a user statement");
            }

            if (kind == AiMemoryStore.Inner_DataUtil.PROCEDURE && !"verified-operation".equals(proof.source())) {
               throw new IllegalArgumentException("Procedures require a verified operation");
            }

            LinkedHashMap<String, AiMemoryStore.Memory> before = new LinkedHashMap<>(this.init_Map);
            String normalized = normalize(text);
            AiMemoryStore.Memory existing = this.init_Map
               .values()
               .stream()
               .filter(mx -> mx.kind() == kind && mx.scope().equals(scope) && normalize(mx.text()).equals(normalized))
               .findFirst()
               .orElse(null);
            AiMemoryStore.Memory m;
            if (existing != null) {
               m = new AiMemoryStore.Memory(existing.id(), kind, scope, text, evidenceId, System.currentTimeMillis(), revision, payload);
               this.init_Map.remove(existing.id());
            } else {
               if (this.init_Map.size() >= this.load_I) {
                  AiMemoryStore.Memory oldest = this.init_Map
                     .values()
                     .stream()
                     .filter(r -> r.kind() == AiMemoryStore.Inner_DataUtil.HYPOTHESIS)
                     .min(Comparator.comparingLong(AiMemoryStore.Memory::timestamp))
                     .orElse(null);
                  if (oldest == null) {
                     throw new IllegalArgumentException("Memory store limit reached; inspect and prune memory before adding more");
                  }

                  this.init_Map.remove(oldest.id());
               }

               m = new AiMemoryStore.Memory(UUID.randomUUID().toString(), kind, scope, text, evidenceId, System.currentTimeMillis(), revision, payload);
            }

            this.init_Map.put(m.id(), m);

            try {
               this.save();
               return m;
            } catch (IOException | RuntimeException e) {
               this.init_Map.clear();
               this.init_Map.putAll(before);
               throw e;
            }
         } else {
            throw new IllegalArgumentException("Memory requires evidence from this scope");
         }
      } else {
         throw new IllegalArgumentException("Memory payload must stay under 256,000 characters and scope under 2,048");
      }
   }

   static String normalize(String text) {
      return text.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
   }

   public List<AiMemoryStore.Memory> init(String scope, String query, int limit) {
      String[] terms = query.toLowerCase(Locale.ROOT).split("\\W+");
      return this.init_List
         .stream()
         .filter(m -> m.scope().equals("user") || m.scope().equals(scope))
         .sorted(
            Comparator.<AiMemoryStore.Memory>comparingInt(m -> matchCount(m.text(), terms))
               .reversed()
               .thenComparing(Comparator.comparingLong(AiMemoryStore.Memory::timestamp).reversed())
         )
         .limit(Math.max(0, Math.min(limit, 100)))
         .toList();
   }

   public List<AiMemoryStore.Memory> recall(String scope, String query) {
      List<AiMemoryStore.Memory> all = this.init_List;
      List<AiMemoryStore.Memory> users = all.stream()
         .filter(m -> m.kind() == AiMemoryStore.Inner_DataUtil.USER && "user".equals(m.scope()))
         .sorted(Comparator.comparingLong(AiMemoryStore.Memory::timestamp).reversed())
         .limit(6L)
         .toList();
      Set<String> chosen = new HashSet<>();
      users.forEach(m -> chosen.add(m.id()));
      String[] terms = Arrays.stream((query == null ? "" : query).toLowerCase(Locale.ROOT).split("\\W+"))
         .filter(t -> t.length() >= 3 && !init_Set.contains(t))
         .distinct()
         .toArray(String[]::new);
      List<AiMemoryStore.Memory> relevant = terms.length == 0
         ? List.of()
         : all.stream()
            .filter(m -> !chosen.contains(m.id()) && (m.scope().equals("user") || m.scope().equals(scope)))
            .map(m -> Map.entry(m, matchCount(m.text(), terms)))
            .filter(e -> e.getValue() > 0)
            .sorted(Entry.<AiMemoryStore.Memory, Integer>comparingByValue().reversed().thenComparingLong(e -> -e.getKey().timestamp()))
            .limit(8L)
            .map(Entry::getKey)
            .toList();
      List<AiMemoryStore.Memory> out = new ArrayList<>(users);
      out.addAll(relevant);
      return List.copyOf(out);
   }

   public List<AiMemoryStore.Memory> load(String worldScope, String query) {
      String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
      return this.init_List
         .stream()
         .filter(m -> m.scope().equals("user") || m.scope().equals(worldScope) || m.scope().startsWith(worldScope + "/"))
         .filter(m -> q.isEmpty() || m.text().toLowerCase(Locale.ROOT).contains(q) || m.kind().name().toLowerCase(Locale.ROOT).contains(q))
         .sorted(Comparator.comparingLong(AiMemoryStore.Memory::timestamp).reversed())
         .toList();
   }

   private static int matchCount(String text, String[] terms) {
      int n = 0;
      String normalized = text.toLowerCase(Locale.ROOT);

      for (String term : terms) {
         if (!term.isBlank() && normalized.contains(term)) {
            n++;
         }
      }

      return n;
   }

   public synchronized void forget(String id) throws IOException {
      LinkedHashMap<String, AiMemoryStore.Memory> before = new LinkedHashMap<>(this.init_Map);
      this.init_Map.remove(id);

      try {
         this.save();
      } catch (IOException | RuntimeException e) {
         this.init_Map.clear();
         this.init_Map.putAll(before);
         throw e;
      }
   }

   public synchronized void load() throws IOException {
      LinkedHashMap<String, AiMemoryStore.Memory> before = new LinkedHashMap<>(this.init_Map);
      this.init_Map.clear();

      try {
         this.save();
         this.read_Map.clear();
      } catch (IOException | RuntimeException e) {
         this.init_Map.putAll(before);
         throw e;
      }
   }

   public synchronized void save() throws IOException {
      JsonObject snapshot = AiJson.objectOf("version", 1, "records", this.init_Map.values(), "epochs", this.load_Map);
      if (snapshot.toString().getBytes(StandardCharsets.UTF_8).length > 15000000) {
         throw new IOException("Memory capacity reached; inspect and prune records before adding more");
      }

      AiJson.writeAtomicJson(this.init_Path.resolve("memory.json"), snapshot);
      this.refreshSnapshots();
   }

   private void refreshSnapshots() {
      this.init_List = List.copyOf(this.init_Map.values());
      this.write_Map = Map.copyOf(this.load_Map);
   }

   public synchronized void journal(String event, JsonElement data) throws IOException {
      Files.createDirectories(this.init_Path);
      Path file = this.init_Path.resolve("journal-" + LocalDate.now() + ".jsonl");
      AiJson.auditPrivateFile(file);
      Files.writeString(
         file,
         AiJson.objectOf("at", System.currentTimeMillis(), "event", event, "data", data) + "\n",
         StandardOpenOption.CREATE,
         StandardOpenOption.APPEND
      );
   }

   public Path directory() {
      return this.init_Path;
   }

   public enum Inner_DataUtil {
      USER,
      WORLD,
      PROCEDURE,
      HYPOTHESIS;
   }

   public record Memory(
      String id, AiMemoryStore.Inner_DataUtil kind, String scope, String text, String evidenceId, long timestamp, String catalogRevision, String payload
   ) {
   }
}
