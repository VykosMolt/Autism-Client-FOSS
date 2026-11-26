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
      return d.sync_Str.isBlank() && d.init_List.isEmpty() && d.load_List.stream().noneMatch(m -> "user".equals(m.role()));
   }

   public AiSessions(Path aiDirectory) {
      this.init_Path = aiDirectory.resolve("sessions");
   }

   public Path init_Path() {
      return this.init_Path;
   }

   public synchronized void init_V() throws IOException {
      this.init_Map.clear();
      this.init_Set.clear();
      if (Files.isDirectory(this.init_Path)) {
         try (Stream<Path> entries = Files.list(this.init_Path)) {
            for (Path file : entries.filter(p -> p.getFileName().toString().endsWith(".json")).sorted().toList()) {
               try {
                  AiSessions.Inner_DataUtil d = init_cd_DataUtil_JsonObject(AiJson.init_JsonObject_Str(Files.readString(file)));
                  this.load(d);
               } catch (Exception var6) {
               }
            }
         }
      }
   }

   private void load(AiSessions.Inner_DataUtil d) {
      this.init_Map.put(d.init_Str, init_cd_StreamUtil_cd_DataUtil(d));
      if (init_Z_cd_DataUtil(d)) {
         this.init_Set.add(d.init_Str);
      } else {
         this.init_Set.remove(d.init_Str);
      }
   }

   public synchronized boolean init_Z_Str(String id) {
      return this.init_Set.contains(id);
   }

   public synchronized List<String> init_List() throws IOException {
      List<String> removed = new ArrayList<>(this.init_Set);

      for (String id : removed) {
         this.init_V_Str(id);
      }

      return removed;
   }

   public synchronized List<AiSessions.Inner_StreamUtil> load() {
      return this.init_Map.values().stream().sorted(Comparator.comparingLong(AiSessions.Inner_StreamUtil::lastActiveAt).reversed()).toList();
   }

   public synchronized AiSessions.Inner_DataUtil init_cd_DataUtil() throws IOException {
      AiSessions.Inner_DataUtil d = new AiSessions.Inner_DataUtil();
      d.init_J = d.load_J = System.currentTimeMillis();
      this.init_V_cd_DataUtil(d);
      return d;
   }

   public synchronized AiSessions.Inner_DataUtil init_cd_DataUtil_Str(String id) throws IOException {
      Path file = this.init_Path_Str(id);
      if (!Files.isRegularFile(file)) {
         throw new IOException("Unknown session " + id);
      } else {
         return init_cd_DataUtil_JsonObject(AiJson.init_JsonObject_Str(Files.readString(file)));
      }
   }

   public synchronized void init_V_cd_DataUtil(AiSessions.Inner_DataUtil d) throws IOException {
      AiJson.init_V_Path_Obj(this.init_Path_Str(d.init_Str), init_JsonObject_cd_DataUtil(d));
      this.load(d);
   }

   public synchronized void init_V_Str(String id) throws IOException {
      this.init_Map.remove(id);
      this.init_Set.remove(id);
      Files.deleteIfExists(this.init_Path_Str(id));
      Files.deleteIfExists(this.load_Path(id));
      Files.deleteIfExists(this.read(id));
   }

   public synchronized AiSessions.Inner_DataUtil load_cd_DataUtil(String id) throws IOException {
      AiSessions.Inner_DataUtil parent = this.init_cd_DataUtil_Str(id);
      AiSessions.Inner_DataUtil child = init_cd_DataUtil_JsonObject(init_JsonObject_cd_DataUtil(parent).deepCopy());
      child.init_Str = UUID.randomUUID().toString();
      child.read_Str = id;
      String base = parent.load_Str;
      if (base.length() > 48 - " (branch)".length() - 1) {
         base = base.substring(0, 48 - " (branch)".length() - 1).trim() + "…";
      }

      child.load_Str = base + " (branch)";
      child.init_J = child.load_J = System.currentTimeMillis();
      this.init_V_cd_DataUtil(child);
      if (Files.isRegularFile(this.load_Path(id))) {
         Files.copy(this.load_Path(id), this.load_Path(child.init_Str), StandardCopyOption.REPLACE_EXISTING);
      }

      if (Files.isRegularFile(this.read(id))) {
         Files.copy(this.read(id), this.read(child.init_Str), StandardCopyOption.REPLACE_EXISTING);
      }

      return child;
   }

   public synchronized void init_V_Str_Str(String id, String title) throws IOException {
      if (title != null && !title.isBlank()) {
         AiSessions.Inner_DataUtil d = this.init_cd_DataUtil_Str(id);
         d.load_Str = title.trim();
         this.init_V_cd_DataUtil(d);
      } else {
         throw new IllegalArgumentException("Session title is blank");
      }
   }

   public synchronized void init_V_Str_JsonObject(String id, JsonObject line) throws IOException {
      Path file = this.load_Path(id);
      Files.createDirectories(this.init_Path);
      AiJson.auditPrivateFile(file);
      Files.writeString(file, init_Str_JsonObject(line) + "\n", StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
      if (Files.size(file) > 4000000L) {
         Files.move(file, this.read(id), StandardCopyOption.REPLACE_EXISTING);
      }
   }

   public synchronized void init_V_Str_List(String id, List<JsonObject> lines) throws IOException {
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
      String text = AiJson.init.toJson(line);
      return text.length() > 1000000
         ? AiJson.init.toJson(AiJson.init_JsonObject_ObjArr("role", "user", "content", "[transcript line elided: " + text.length() + " characters]"))
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

      while (!tail.isEmpty() && (plainHead ? !init_Z_JsonObject(tail.get(0)) : !"user".equals(AiJson.init_Str_JsonObject(tail.get(0), "role", "")))) {
         tail.remove(0);
      }

      return tail;
   }

   static boolean init_Z_JsonObject(JsonObject message) {
      if (!"user".equals(AiJson.init_Str_JsonObject(message, "role", ""))) {
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
            if (!o.has("text") && !"text".equals(AiJson.init_Str_JsonObject(o, "type", ""))) {
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

   public static String init_Str_Str(String firstUserMessage) {
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
      d.init_Str = newId;
      d.init_J = d.load_J = now;
      d.sync_Str = AiJson.init_Str_JsonObject(legacy, "objective", "");
      d.load_Str = d.sync_Str.isBlank() ? "First session" : init_Str_Str(d.sync_Str);
      d.flush_Str = AiJson.init_Str_JsonObject(legacy, "taskState", "");
      d.close_Str = AiJson.init_Str_JsonObject(legacy, "scope", "");
      d.update_Str = AiJson.init_Str_JsonObject(legacy, "taskId", UUID.randomUUID().toString());
      d.write_Str = AiJson.init_Str_JsonObject(legacy, "state", "PAUSED");
      d.read_J = legacy.has("requests") ? legacy.get("requests").getAsLong() : 0L;
      d.write_J = legacy.has("tokens") ? legacy.get("tokens").getAsLong() : 0L;
      d.update_J = legacy.has("elapsedMillis") ? legacy.get("elapsedMillis").getAsLong() : 0L;
      d.load_I = AiJson.init_I_JsonObject(legacy, "wheatBaseline", -1);
      d.init_I = legacy.has("taskMinutes") ? AiJson.init_I_JsonObject(legacy, "taskMinutes", defaultTaskMinutes) : defaultTaskMinutes;
      if (legacy.has("structuredTask")) {
         d.init_JsonObject = legacy.getAsJsonObject("structuredTask");
      }

      if (legacy.has("queue")) {
         for (JsonElement item : legacy.getAsJsonArray("queue")) {
            d.init_List.add(item.getAsString());
         }
      }

      return d;
   }

   public static AiSessions.Inner_StreamUtil init_cd_StreamUtil_cd_DataUtil(AiSessions.Inner_DataUtil d) {
      return new AiSessions.Inner_StreamUtil(d.init_Str, d.load_Str, d.init_J, d.load_J, d.read_Str, d.write_Str, d.load_List.size());
   }

   static JsonObject init_JsonObject_cd_StreamUtil(AiSessions.Inner_StreamUtil m) {
      return AiJson.init_JsonObject_ObjArr(
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
         AiJson.init_Str_JsonObject(o, "id", ""),
         AiJson.init_Str_JsonObject(o, "title", "New session"),
         o.has("createdAt") ? o.get("createdAt").getAsLong() : 0L,
         o.has("lastActiveAt") ? o.get("lastActiveAt").getAsLong() : 0L,
         AiJson.init_Str_JsonObject(o, "parentId", "").isBlank() ? null : AiJson.init_Str_JsonObject(o, "parentId", ""),
         AiJson.init_Str_JsonObject(o, "state", "IDLE"),
         AiJson.init_I_JsonObject(o, "messageCount", 0)
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
      d.init_List.forEach(queue::add);
      JsonArray updates = new JsonArray();
      d.read_List.forEach(updates::add);
      JsonArray messages = new JsonArray();

      for (AiService.TranscriptEntry m : init_List_List_I(d.load_List, 1000000)) {
         messages.add(AiJson.init_JsonObject_ObjArr("at", m.at(), "role", m.role(), "text", m.text(), "detail", m.detail()));
      }

      return AiJson.init_JsonObject_ObjArr(
         "version",
         2,
         "id",
         d.init_Str,
         "title",
         d.load_Str,
         "parentId",
         d.read_Str == null ? "" : d.read_Str,
         "createdAt",
         d.init_J,
         "lastActiveAt",
         d.load_J,
         "state",
         d.write_Str,
         "taskId",
         d.update_Str,
         "objective",
         d.sync_Str,
         "taskState",
         d.flush_Str,
         "structuredTask",
         d.init_JsonObject,
         "queue",
         queue,
         "scope",
         d.close_Str,
         "requests",
         d.read_J,
         "tokens",
         d.write_J,
         "elapsedMillis",
         d.update_J,
         "wheatBaseline",
         d.load_I,
         "taskMinutes",
         d.init_I,
         "attemptedAction",
         d.init_Z,
         "messages",
         messages,
         "updates",
         updates,
         "usage",
         AiJson.init_JsonObject_ObjArr(
            "input", d.sync_J, "cachedInput", d.flush_J, "cacheWrite", d.close_J, "output", d.open_J, "requests", d.poll, "lastPrompt", d.push
         ),
         "wireProvider",
         d.open_Str == null ? "" : d.open_Str
      );
   }

   static AiSessions.Inner_DataUtil init_cd_DataUtil_JsonObject(JsonObject o) {
      AiSessions.Inner_DataUtil d = new AiSessions.Inner_DataUtil();
      d.init_Str = AiJson.init_Str_JsonObject(o, "id", UUID.randomUUID().toString());
      d.load_Str = AiJson.init_Str_JsonObject(o, "title", "New session");
      String parent = AiJson.init_Str_JsonObject(o, "parentId", "");
      d.read_Str = parent.isBlank() ? null : parent;
      d.init_J = o.has("createdAt") ? o.get("createdAt").getAsLong() : 0L;
      d.load_J = o.has("lastActiveAt") ? o.get("lastActiveAt").getAsLong() : d.init_J;
      d.write_Str = AiJson.init_Str_JsonObject(o, "state", "IDLE");
      d.update_Str = AiJson.init_Str_JsonObject(o, "taskId", UUID.randomUUID().toString());
      d.sync_Str = AiJson.init_Str_JsonObject(o, "objective", "");
      d.flush_Str = AiJson.init_Str_JsonObject(o, "taskState", "");
      d.close_Str = AiJson.init_Str_JsonObject(o, "scope", "");
      if (o.has("structuredTask")) {
         d.init_JsonObject = o.getAsJsonObject("structuredTask");
      }

      d.init_I = AiJson.init_I_JsonObject(o, "taskMinutes", 60);
      d.load_I = AiJson.init_I_JsonObject(o, "wheatBaseline", -1);
      d.read_J = o.has("requests") ? o.get("requests").getAsLong() : 0L;
      d.write_J = o.has("tokens") ? o.get("tokens").getAsLong() : 0L;
      d.update_J = o.has("elapsedMillis") ? o.get("elapsedMillis").getAsLong() : 0L;
      d.init_Z = AiJson.init_Z_JsonObject(o, "attemptedAction", false);
      d.init_List = new ArrayList<>();
      if (o.has("queue")) {
         for (JsonElement item : o.getAsJsonArray("queue")) {
            d.init_List.add(item.getAsString());
         }
      }

      d.load_List = new ArrayList<>();
      if (o.has("messages")) {
         for (JsonElement item : o.getAsJsonArray("messages")) {
            JsonObject m = item.getAsJsonObject();
            d.load_List
               .add(
                  new AiService.TranscriptEntry(
                     m.has("at") ? m.get("at").getAsLong() : 0L,
                     AiJson.init_Str_JsonObject(m, "role", ""),
                     AiJson.init_Str_JsonObject(m, "text", ""),
                     AiJson.init_Str_JsonObject(m, "detail", "")
                  )
               );
         }
      }

      d.read_List = new ArrayList<>();
      if (o.has("updates")) {
         for (JsonElement item : o.getAsJsonArray("updates")) {
            d.read_List.add(item.getAsString());
         }
      }

      if (o.has("usage") && o.get("usage").isJsonObject()) {
         JsonObject u = o.getAsJsonObject("usage");
         d.sync_J = init_J_JsonObject_Str(u, "input");
         d.flush_J = init_J_JsonObject_Str(u, "cachedInput");
         d.close_J = init_J_JsonObject_Str(u, "cacheWrite");
         d.open_J = init_J_JsonObject_Str(u, "output");
         d.poll = init_J_JsonObject_Str(u, "requests");
         d.push = init_J_JsonObject_Str(u, "lastPrompt");
      }

      String wire = AiJson.init_Str_JsonObject(o, "wireProvider", "");
      d.open_Str = wire.isBlank() ? null : wire;
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
      public String init_Str = UUID.randomUUID().toString();
      public String load_Str = "New session";
      public String read_Str;
      public long init_J;
      public long load_J;
      public String write_Str = "IDLE";
      public String update_Str = UUID.randomUUID().toString();
      public String sync_Str = "";
      public String flush_Str = "";
      public String close_Str = "";
      public JsonObject init_JsonObject = new JsonObject();
      public int init_I = 60;
      public int load_I = -1;
      public long read_J;
      public long write_J;
      public long update_J;
      public boolean init_Z;
      public List<String> init_List = new ArrayList<>();
      public List<AiService.TranscriptEntry> load_List = new ArrayList<>();
      public List<String> read_List = new ArrayList<>();
      public long sync_J;
      public long flush_J;
      public long close_J;
      public long open_J;
      public long poll;
      public long push;
      public String open_Str;
   }

   public record Inner_StreamUtil(String id, String title, long createdAt, long lastActiveAt, String parentId, String state, int messageCount) {
   }
}
