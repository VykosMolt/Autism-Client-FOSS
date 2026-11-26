package autismclient.ai;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

public final class AiRunTrace {
   static final long init_J = 4000000L;
   static final int init_I = 1000000;
   private static AiRunTrace init_bL;
   private final Path init_Path;
   private final Path load_Path;
   private final long load_J;
   private long read;
   private final List<AiRunTrace.Inner_DataUtil> init_List = new CopyOnWriteArrayList<>();
   private BufferedWriter init_BufferedWriter;
   private long write;

   AiRunTrace(Path file, long startedAt, long limit) {
      this.init_Path = file;
      this.load_Path = Path.of(file + ".1");
      this.load_J = startedAt;
      this.read = limit;
   }

   static synchronized AiRunTrace init_bL_Path_Str(Path aiDirectory, String sessionId) {
      AiRunTrace previous = init_bL;
      if (previous != null) {
         previous.flush();
      }

      init_bL = new AiRunTrace(aiDirectory.resolve("trace").resolve("ai-trace-" + sessionId + ".jsonl"), System.currentTimeMillis(), 4000000L);
      return init_bL;
   }

   public static synchronized AiRunTrace init_bL() {
      return init_bL;
   }

   static synchronized void init_V() {
      AiRunTrace t = init_bL;
      init_bL = null;
      if (t != null) {
         t.flush();
      }
   }

   public static void init_V_Str_Str(String name, String detail) {
      AiRunTrace t = init_bL();
      if (t != null) {
         t.read(name, detail);
      }
   }

   public static void init_V_Str_JsonObject(String kind, JsonObject data) {
      AiService service = AiService.load_cc();
      if (service != null) {
         service.init_V_Str_JsonObject(kind, data);
      }
   }

   synchronized void load(String kind, JsonObject data) {
      this.read(kind, data);
   }

   public Path init_Path() {
      return this.init_Path;
   }

   synchronized void load() {
      this.read = Math.max(this.read, 64000000L);
   }

   public List<AiRunTrace.Inner_DataUtil> init_List() {
      return List.copyOf(this.init_List);
   }

   long init_J() {
      return this.load_J;
   }

   public synchronized void init_V_Str_I(String objective, int taskMinutes) {
      this.read("task_start", AiJson.objectOf("objective", objective, "taskMinutes", taskMinutes));
   }

   public synchronized void init_V_J_J(long latencyMs, long estTokens) {
      this.read("provider_request", AiJson.objectOf("latencyMs", latencyMs, "estTokens", estTokens));
   }

   public synchronized void init(String name) {
      this.read("tool_call", AiJson.objectOf("name", name));
   }

   public synchronized void init_V_Str(String name, String status, String failureCode, long durationMs) {
      this.read(
         "tool_result",
         failureCode != null && !failureCode.isBlank()
            ? AiJson.objectOf("name", name, "status", status, "failureCode", failureCode, "durationMs", durationMs)
            : AiJson.objectOf("name", name, "status", status, "durationMs", durationMs)
      );
   }

   public synchronized void load(String kind) {
      this.read("op_interrupt", AiJson.objectOf("kind", kind));
   }

   public synchronized void read() {
      this.read("death", new JsonObject());
   }

   public synchronized void write() {
      this.read("respawn", new JsonObject());
   }

   public synchronized void load(String from, String to) {
      this.read("dimension_change", AiJson.objectOf("from", from, "to", to));
   }

   public synchronized void read(String name, String detail) {
      this.init_List.add(new AiRunTrace.Inner_DataUtil(name, Math.max(0L, System.currentTimeMillis() - this.load_J), detail == null ? "" : detail));
      this.read(
         "split", detail != null && !detail.isBlank() ? AiJson.objectOf("name", name, "detail", detail) : AiJson.objectOf("name", name)
      );
   }

   public synchronized void read(String outcome) {
      this.read("finish", AiJson.objectOf("outcome", outcome));
   }

   static String init_Str_J(long ts, long startedAt, String kind, JsonObject data) {
      JsonObject o = new JsonObject();
      if (data != null) {
         for (Entry<String, JsonElement> e : data.entrySet()) {
            o.add(e.getKey(), e.getValue());
         }
      }

      o.addProperty("ts", ts);
      o.addProperty("elapsedMs", Math.max(0L, ts - startedAt));
      o.addProperty("event", kind);
      return AiJson.GSON.toJson(o);
   }

   static boolean init(long written, long incomingBytes, long limit) {
      return written > 0L && written + incomingBytes > limit;
   }

   private void read(String kind, JsonObject data) {
      try {
         String line = init_Str_J(System.currentTimeMillis(), this.load_J, kind, data);
         byte[] bytes = (line + "\n").getBytes(StandardCharsets.UTF_8);
         this.update();
         if (init(this.write, bytes.length, this.read)) {
            this.sync();
            this.update();
         }

         this.init_BufferedWriter.write(line);
         this.init_BufferedWriter.newLine();
         this.init_BufferedWriter.flush();
         this.write += bytes.length;
      } catch (Exception var5) {
      }
   }

   private void update() throws IOException {
      if (this.init_BufferedWriter == null) {
         Files.createDirectories(this.init_Path.getParent());
         AiJson.auditPrivateFile(this.init_Path);
         this.write = Files.isRegularFile(this.init_Path) ? Files.size(this.init_Path) : 0L;
         this.init_BufferedWriter = Files.newBufferedWriter(this.init_Path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
      }
   }

   private void sync() throws IOException {
      this.flush();
      for (int i = 2; i >= 1; i--) {
         Path older = Path.of(this.init_Path + "." + i);
         if (Files.exists(older)) {
            Files.move(older, Path.of(this.init_Path + "." + (i + 1)), StandardCopyOption.REPLACE_EXISTING);
         }
      }

      Files.move(this.init_Path, this.load_Path, StandardCopyOption.REPLACE_EXISTING);
      this.write = 0L;
   }

   private synchronized void flush() {
      if (this.init_BufferedWriter != null) {
         try {
            this.init_BufferedWriter.close();
         } catch (IOException var2) {
         }

         this.init_BufferedWriter = null;
      }
   }

   public synchronized List<String> init(int n) {
      if (n <= 0) {
         return List.of();
      }

      List<String> lines = new ArrayList<>();

      for (Path path : List.of(this.load_Path, this.init_Path)) {
         if (Files.isRegularFile(path)) {
            String line;
            try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
               while ((line = AiBoundedIo.init(reader, 1001000)) != null) {
                  if (!line.isBlank()) {
                     lines.add(line);
                  }
               }
            } catch (IOException var11) {
            }
         }
      }

      return lines.size() <= n ? List.copyOf(lines) : List.copyOf(lines.subList(lines.size() - n, lines.size()));
   }

   public record Inner_DataUtil(String name, long elapsedMs, String detail) {
   }
}
