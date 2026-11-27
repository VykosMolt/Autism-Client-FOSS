package autismclient.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public final class AiSessions {
   static final int init_I = 48;
   static final long init_J = 4000000L;
   static final int load = 1000000;
   static final int read = 1000000;
   private final Path init_Path;
   private final Map<String, AiSessions.Inner_StreamUtil> init_Map = new LinkedHashMap<>();
   private final Set<String> init_Set = new HashSet<>();

   public static boolean init_Z_cd_DataUtil(AiSessions.Inner_DataUtil d) {
      return d.objective.isBlank() && d.queue.isEmpty() && d.messages.stream().noneMatch(m -> "user".equals(m.role()));
   }

   public AiSessions(Path aiDirectory) {
      this.init_Path = aiDirectory.resolve("sessions");
   }

   public Path init_Path() {
      return this.init_Path;
   }

   public synchronized void reloadIndex() throws IOException {
      this.init_Map.clear();
      this.init_Set.clear();
      if (Files.isDirectory(this.init_Path)) {
         try (Stream<Path> entries = Files.list(this.init_Path)) {
            for (Path file : entries.filter(p -> p.getFileName().toString().endsWith(".json")).sorted().toList()) {
               try {
                  AiSessions.Inner_DataUtil d = init_cd_DataUtil_JsonObject(AiJson.parseObject(Files.readString(file)));
                  this.load(d);
               } catch (Exception var6) {
               }
            }
         }
      }
   }

   private void load(AiSessions.Inner_DataUtil d) {
      this.init_Map.put(d.id, init_cd_StreamUtil_cd_DataUtil(d));
      if (init_Z_cd_DataUtil(d)) {
         this.init_Set.add(d.id);
      } else {
         this.init_Set.remove(d.id);
      }
   }

   public synchronized boolean init_Z_Str(String id) {
      return this.init_Set.contains(id);
   }

   public synchronized List<String> purgeDeleted() throws IOException {
      List<String> removed = new ArrayList<>(this.init_Set);

      for (String id : removed) {
         this.delete(id);
      }

      return removed;
   }

   public synchronized List<AiSessions.Inner_StreamUtil> load() {
      return this.init_Map.values().stream().sorted(Comparator.comparingLong(AiSessions.Inner_StreamUtil::lastActiveAt).reversed()).toList();
   }

   public synchronized AiSessions.Inner_DataUtil init_cd_DataUtil() throws IOException {
      AiSessions.Inner_DataUtil d = new AiSessions.Inner_DataUtil();
      d.createdAt = d.lastActiveAt = System.currentTimeMillis();
      this.save(d);
      return d;
   }

   public synchronized AiSessions.Inner_DataUtil readSession(String id) throws IOException {
      Path file = this.init_Path_Str(id);
      if (!Files.isRegularFile(file)) {
         throw new IOException("Unknown session " + id);
      } else {
         return init_cd_DataUtil_JsonObject(AiJson.parseObject(Files.readString(file)));
      }
   }

   public synchronized void save(AiSessions.Inner_DataUtil d) throws IOException {
      AiJson.writeAtomicJson(this.init_Path_Str(d.id), init_JsonObject_cd_DataUtil(d));
      this.load(d);
   }

   public synchronized void delete(String id) throws IOException {
      this.init_Map.remove(id);
      this.init_Set.remove(id);
      Files.deleteIfExists(this.init_Path_Str(id));
      Files.deleteIfExists(this.load_Path(id));
      Files.deleteIfExists(this.read(id));
   }

   public synchronized AiSessions.Inner_DataUtil fork(String id) throws IOException {
      AiSessions.Inner_DataUtil parent = this.readSession(id);
      AiSessions.Inner_DataUtil child = init_cd_DataUtil_JsonObject(init_JsonObject_cd_DataUtil(parent).deepCopy());
      child.id = UUID.randomUUID().toString();
      child.parentId = id;
      String base = parent.title;
      if (base.length() > 48 - " (branch)".length() - 1) {
         base = base.substring(0, 48 - " (branch)".length() - 1).trim() + "…";
      }

      child.title = base + " (branch)";
      child.createdAt = child.lastActiveAt = System.currentTimeMillis();
      this.save(child);
      if (Files.isRegularFile(this.load_Path(id))) {
         Files.copy(this.load_Path(id), this.load_Path(child.id), StandardCopyOption.REPLACE_EXISTING);
      }

      if (Files.isRegularFile(this.read(id))) {
         Files.copy(this.read(id), this.read(child.id), StandardCopyOption.REPLACE_EXISTING);
      }

      return child;
   }

   public synchronized void rename(String id, String title) throws IOException {
      if (title != null && !title.isBlank()) {
         AiSessions.Inner_DataUtil d = this.readSession(id);
         d.title = title.trim();
         this.save(d);
      } else {
         throw new IllegalArgumentException("Session title is blank");
      }
   }

   public synchronized void appendLine(String id, JsonObject line) throws IOException {
      Path file = this.load_Path(id);
      Files.createDirectories(this.init_Path);
      AiJson.auditPrivateFile(file);
      Files.writeString(file, init_Str_JsonObject(line) + "\n", StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
      if (Files.size(file) > 4000000L) {
         Files.move(file, this.read(id), StandardCopyOption.REPLACE_EXISTING);
      }
   }

   public synchronized void writeLines(String id, List<JsonObject> lines) throws IOException {
      StringBuilder text = new StringBuilder();

      for (JsonObject line : lines) {
         text.append(init_Str_JsonObject(line)).append('\n');
      }

      Files.createDirectories(this.init_Path);
      Path temp = Files.createTempFile(this.init_Path, id, ".transcript.tmp");

      try {
         Files.writeString(temp, text, StandardCharsets.UTF_8);

         try {
            Files.move(temp, this.load_Path(id), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
         } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, this.load_Path(id), StandardCopyOption.REPLACE_EXISTING);
         }
      } finally {
         Files.deleteIfExists(temp);
      }

      Files.deleteIfExists(this.read(id));
   }

   private static String init_Str_JsonObject(JsonObject line) {
      String text = AiJson.GSON.toJson(line);
      return text.length() > 1000000
         ? AiJson.GSON.toJson(AiJson.objectOf("role", "user", "content", "[transcript line elided: " + text.length() + " characters]"))
         : text;
   }

   public List<JsonObject> init_List_Str_J(String id, long charBudget) {
      return this.init(id, charBudget, true);
   }

   public synchronized List<JsonObject> init(String id, long charBudget, boolean plainHead) {
      List<JsonObject> parsed = new ArrayList<>();

      // the rotated archive holds the older lines, so it is read first and the live file appended
      for (Path file : List.of(this.read(id), this.load_Path(id))) {
         if (Files.isRegularFile(file)) {
            String line;
            try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
               while ((line = AiBoundedIo.init(reader, 1001000)) != null) {
                  if (!line.isBlank()) {
                     try {
                        parsed.add(JsonParser.parseString(line).getAsJsonObject());
                     } catch (Exception var14) {
                     }
                  }
               }
            } catch (IOException var16) {
            }
         }
      }

      List<JsonObject> tail = new ArrayList<>();
      long used = 0L;

      for (int i = parsed.size() - 1; i >= 0; i--) {
         JsonObject line = parsed.get(i);
         long cost = AiContext.init(line);
         if (!tail.isEmpty() && used + cost > charBudget) {
            break;
         }

         used += cost;
         tail.add(0, line);
      }

      while (!tail.isEmpty() && (plainHead ? !init_Z_JsonObject(tail.get(0)) : !"user".equals(AiJson.getString(tail.get(0), "role", "")))) {
         tail.remove(0);
      }

      return tail;
   }

   static boolean init_Z_JsonObject(JsonObject message) {
      if (!"user".equals(AiJson.getString(message, "role", ""))) {
         return false;
      }

      if (message.has("content")) {
         JsonElement content = message.get("content");
         if (content.isJsonPrimitive()) {
            return true;
         }

         if (!content.isJsonArray()) {
            return false;
         }

         for (JsonElement part : content.getAsJsonArray()) {
            if (!part.isJsonObject()) {
               return false;
            }

            JsonObject o = part.getAsJsonObject();
            if (!o.has("text") && !"text".equals(AiJson.getString(o, "type", ""))) {
               return false;
            }
         }

         return true;
      } else if (message.has("parts") && message.get("parts").isJsonArray()) {
         for (JsonElement part : message.getAsJsonArray("parts")) {
            if (!part.isJsonObject() || !part.getAsJsonObject().has("text")) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static String titleFor(String firstUserMessage) {
      if (firstUserMessage == null) {
         return "New session";
      }

      String collapsed = firstUserMessage.replaceAll("\\s+", " ").trim();
      if (collapsed.isEmpty()) {
         return "New session";
      }

      if (collapsed.length() <= 48) {
         return collapsed;
      }

      String cut = collapsed.substring(0, 47);
      int space = cut.lastIndexOf(32);
      if (space >= 24) {
         cut = cut.substring(0, space);
      }

      return cut.trim() + "…";
   }

   public static AiSessions.Inner_DataUtil init(JsonObject legacy, String newId, long now, int defaultTaskMinutes) {
      AiSessions.Inner_DataUtil d = new AiSessions.Inner_DataUtil();
      d.id = newId;
      d.createdAt = d.lastActiveAt = now;
      d.objective = AiJson.getString(legacy, "objective", "");
      d.title = d.objective.isBlank() ? "First session" : titleFor(d.objective);
      d.taskState = AiJson.getString(legacy, "taskState", "");
      d.scope = AiJson.getString(legacy, "scope", "");
      d.taskId = AiJson.getString(legacy, "taskId", UUID.randomUUID().toString());
      d.state = AiJson.getString(legacy, "state", "PAUSED");
      d.requests = legacy.has("requests") ? legacy.get("requests").getAsLong() : 0L;
      d.tokens = legacy.has("tokens") ? legacy.get("tokens").getAsLong() : 0L;
      d.elapsedMillis = legacy.has("elapsedMillis") ? legacy.get("elapsedMillis").getAsLong() : 0L;
      d.wheatBaseline = AiJson.getInt(legacy, "wheatBaseline", -1);
      d.taskMinutes = legacy.has("taskMinutes") ? AiJson.getInt(legacy, "taskMinutes", defaultTaskMinutes) : defaultTaskMinutes;
      if (legacy.has("structuredTask")) {
         d.structuredTask = legacy.getAsJsonObject("structuredTask");
      }

      if (legacy.has("queue")) {
         for (JsonElement item : legacy.getAsJsonArray("queue")) {
            d.queue.add(item.getAsString());
         }
      }

      return d;
   }

   public static AiSessions.Inner_StreamUtil init_cd_StreamUtil_cd_DataUtil(AiSessions.Inner_DataUtil d) {
      return new AiSessions.Inner_StreamUtil(d.id, d.title, d.createdAt, d.lastActiveAt, d.parentId, d.state, d.messages.size());
   }

   static JsonObject init_JsonObject_cd_StreamUtil(AiSessions.Inner_StreamUtil m) {
      return AiJson.objectOf(
         "id",
         m.id(),
         "title",
         m.title(),
         "createdAt",
         m.createdAt(),
         "lastActiveAt",
         m.lastActiveAt(),
         "parentId",
         m.parentId() == null ? "" : m.parentId(),
         "state",
         m.state(),
         "messageCount",
         m.messageCount()
      );
   }

   static AiSessions.Inner_StreamUtil init_cd_StreamUtil_JsonObject(JsonObject o) {
      return new AiSessions.Inner_StreamUtil(
         AiJson.getString(o, "id", ""),
         AiJson.getString(o, "title", "New session"),
         o.has("createdAt") ? o.get("createdAt").getAsLong() : 0L,
         o.has("lastActiveAt") ? o.get("lastActiveAt").getAsLong() : 0L,
         AiJson.getString(o, "parentId", "").isBlank() ? null : AiJson.getString(o, "parentId", ""),
         AiJson.getString(o, "state", "IDLE"),
         AiJson.getInt(o, "messageCount", 0)
      );
   }

   static List<AiService.TranscriptEntry> init_List_List_I(List<AiService.TranscriptEntry> messages, int charBudget) {
      List<AiService.TranscriptEntry> kept = new ArrayList<>();
      int used = 0;

      for (int i = messages.size() - 1; i >= 0; i--) {
         AiService.TranscriptEntry m = messages.get(i);
         int cost = m.text().length() + m.detail().length() + m.role().length() + 24;
         if (!kept.isEmpty() && used + cost > charBudget) {
            break;
         }

         used += cost;
         kept.add(0, m);
      }

      return kept;
   }

   static JsonObject init_JsonObject_cd_DataUtil(AiSessions.Inner_DataUtil d) {
      JsonArray queue = new JsonArray();
      d.queue.forEach(queue::add);
      JsonArray updates = new JsonArray();
      d.updates.forEach(updates::add);
      JsonArray messages = new JsonArray();

      for (AiService.TranscriptEntry m : init_List_List_I(d.messages, 1000000)) {
         messages.add(AiJson.objectOf("at", m.at(), "role", m.role(), "text", m.text(), "detail", m.detail()));
      }

      return AiJson.objectOf(
         "version",
         2,
         "id",
         d.id,
         "title",
         d.title,
         "parentId",
         d.parentId == null ? "" : d.parentId,
         "createdAt",
         d.createdAt,
         "lastActiveAt",
         d.lastActiveAt,
         "state",
         d.state,
         "taskId",
         d.taskId,
         "objective",
         d.objective,
         "taskState",
         d.taskState,
         "structuredTask",
         d.structuredTask,
         "queue",
         queue,
         "scope",
         d.scope,
         "requests",
         d.requests,
         "tokens",
         d.tokens,
         "elapsedMillis",
         d.elapsedMillis,
         "wheatBaseline",
         d.wheatBaseline,
         "taskMinutes",
         d.taskMinutes,
         "attemptedAction",
         d.attemptedAction,
         "messages",
         messages,
         "updates",
         updates,
         "usage",
         AiJson.objectOf(
            "input", d.inputTokens, "cachedInput", d.cachedInputTokens, "cacheWrite", d.cacheWriteTokens, "output", d.outputTokens, "requests", d.poll, "lastPrompt", d.push
         ),
         "wireProvider",
         d.wire == null ? "" : d.wire
      );
   }

   static AiSessions.Inner_DataUtil init_cd_DataUtil_JsonObject(JsonObject o) {
      AiSessions.Inner_DataUtil d = new AiSessions.Inner_DataUtil();
      d.id = AiJson.getString(o, "id", UUID.randomUUID().toString());
      d.title = AiJson.getString(o, "title", "New session");
      String parent = AiJson.getString(o, "parentId", "");
      d.parentId = parent.isBlank() ? null : parent;
      d.createdAt = o.has("createdAt") ? o.get("createdAt").getAsLong() : 0L;
      d.lastActiveAt = o.has("lastActiveAt") ? o.get("lastActiveAt").getAsLong() : d.createdAt;
      d.state = AiJson.getString(o, "state", "IDLE");
      d.taskId = AiJson.getString(o, "taskId", UUID.randomUUID().toString());
      d.objective = AiJson.getString(o, "objective", "");
      d.taskState = AiJson.getString(o, "taskState", "");
      d.scope = AiJson.getString(o, "scope", "");
      if (o.has("structuredTask")) {
         d.structuredTask = o.getAsJsonObject("structuredTask");
      }

      d.taskMinutes = AiJson.getInt(o, "taskMinutes", 60);
      d.wheatBaseline = AiJson.getInt(o, "wheatBaseline", -1);
      d.requests = o.has("requests") ? o.get("requests").getAsLong() : 0L;
      d.tokens = o.has("tokens") ? o.get("tokens").getAsLong() : 0L;
      d.elapsedMillis = o.has("elapsedMillis") ? o.get("elapsedMillis").getAsLong() : 0L;
      d.attemptedAction = AiJson.getBoolean(o, "attemptedAction", false);
      d.queue = new ArrayList<>();
      if (o.has("queue")) {
         for (JsonElement item : o.getAsJsonArray("queue")) {
            d.queue.add(item.getAsString());
         }
      }

      d.messages = new ArrayList<>();
      if (o.has("messages")) {
         for (JsonElement item : o.getAsJsonArray("messages")) {
            JsonObject m = item.getAsJsonObject();
            d.messages
               .add(
                  new AiService.TranscriptEntry(
                     m.has("at") ? m.get("at").getAsLong() : 0L,
                     AiJson.getString(m, "role", ""),
                     AiJson.getString(m, "text", ""),
                     AiJson.getString(m, "detail", "")
                  )
               );
         }
      }

      d.updates = new ArrayList<>();
      if (o.has("updates")) {
         for (JsonElement item : o.getAsJsonArray("updates")) {
            d.updates.add(item.getAsString());
         }
      }

      if (o.has("usage") && o.get("usage").isJsonObject()) {
         JsonObject u = o.getAsJsonObject("usage");
         d.inputTokens = init_J_JsonObject_Str(u, "input");
         d.cachedInputTokens = init_J_JsonObject_Str(u, "cachedInput");
         d.cacheWriteTokens = init_J_JsonObject_Str(u, "cacheWrite");
         d.outputTokens = init_J_JsonObject_Str(u, "output");
         d.poll = init_J_JsonObject_Str(u, "requests");
         d.push = init_J_JsonObject_Str(u, "lastPrompt");
      }

      String wire = AiJson.getString(o, "wireProvider", "");
      d.wire = wire.isBlank() ? null : wire;
      return d;
   }

   private static long init_J_JsonObject_Str(JsonObject o, String key) {
      return o.has(key) && o.get(key).isJsonPrimitive() ? o.get(key).getAsLong() : 0L;
   }

   private Path init_Path_Str(String id) {
      return this.init_Path.resolve(id + ".json");
   }

   private Path load_Path(String id) {
      return this.init_Path.resolve(id + ".transcript.jsonl");
   }

   private Path read(String id) {
      return this.init_Path.resolve(id + ".transcript.jsonl.1");
   }

   public static final class Inner_DataUtil {
      public String id = UUID.randomUUID().toString();
      public String title = "New session";
      public String parentId;
      public long createdAt;
      public long lastActiveAt;
      public String state = "IDLE";
      public String taskId = UUID.randomUUID().toString();
      public String objective = "";
      public String taskState = "";
      public String scope = "";
      public JsonObject structuredTask = new JsonObject();
      public int taskMinutes = 60;
      public int wheatBaseline = -1;
      public long requests;
      public long tokens;
      public long elapsedMillis;
      public boolean attemptedAction;
      public List<String> queue = new ArrayList<>();
      public List<AiService.TranscriptEntry> messages = new ArrayList<>();
      public List<String> updates = new ArrayList<>();
      public long inputTokens;
      public long cachedInputTokens;
      public long cacheWriteTokens;
      public long outputTokens;
      public long poll;
      public long push;
      public String wire;
   }

   public record Inner_StreamUtil(String id, String title, long createdAt, long lastActiveAt, String parentId, String state, int messageCount) {
   }
}
