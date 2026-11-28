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
   static final long DEFAULT_ROTATE_BYTES = 4000000L;
   static final int MAX_LINE_CHARS = 1000000;
   private static AiRunTrace activeTrace;
   private final Path traceFile;
   private final Path rotatedFile;
   private final long startedAtMs;
   private long rotateLimitBytes;
   private final List<AiRunTrace.TraceSplit> splits = new CopyOnWriteArrayList<>();
   private BufferedWriter writer;
   private long bytesWritten;

   AiRunTrace(Path file, long startedAt, long limit) {
      this.traceFile = file;
      this.rotatedFile = Path.of(file + ".1");
      this.startedAtMs = startedAt;
      this.rotateLimitBytes = limit;
   }

   static synchronized AiRunTrace startSession(Path aiDirectory, String sessionId) {
      AiRunTrace previous = activeTrace;
      if (previous != null) {
         previous.closeWriter();
      }

      activeTrace = new AiRunTrace(aiDirectory.resolve("trace").resolve("ai-trace-" + sessionId + ".jsonl"), System.currentTimeMillis(), 4000000L);
      return activeTrace;
   }

   public static synchronized AiRunTrace current() {
      return activeTrace;
   }

   static synchronized void stopSession() {
      AiRunTrace t = activeTrace;
      activeTrace = null;
      if (t != null) {
         t.closeWriter();
      }
   }

   public static void recordGlobalSplit(String name, String detail) {
      AiRunTrace t = current();
      if (t != null) {
         t.recordSplit(name, detail);
      }
   }

   public static void emitServiceEvent(String kind, JsonObject data) {
      AiService service = AiService.getExistingInstance();
      if (service != null) {
         service.appendTraceEvent(kind, data);
      }
   }

   synchronized void appendEvent(String kind, JsonObject data) {
      this.writeEventLine(kind, data);
   }

   public Path filePath() {
      return this.traceFile;
   }

   synchronized void expandLimitForLongRun() {
      this.rotateLimitBytes = Math.max(this.rotateLimitBytes, 64000000L);
   }

   public List<AiRunTrace.TraceSplit> getSplits() {
      return List.copyOf(this.splits);
   }

   long startedAtMs() {
      return this.startedAtMs;
   }

   public synchronized void recordTaskStart(String objective, int taskMinutes) {
      this.writeEventLine("task_start", AiJson.objectOf("objective", objective, "taskMinutes", taskMinutes));
   }

   public synchronized void recordProviderRequest(long latencyMs, long estTokens) {
      this.writeEventLine("provider_request", AiJson.objectOf("latencyMs", latencyMs, "estTokens", estTokens));
   }

   public synchronized void recordToolCall(String name) {
      this.writeEventLine("tool_call", AiJson.objectOf("name", name));
   }

   public synchronized void recordToolResult(String name, String status, String failureCode, long durationMs) {
      this.writeEventLine(
         "tool_result",
         failureCode != null && !failureCode.isBlank()
            ? AiJson.objectOf("name", name, "status", status, "failureCode", failureCode, "durationMs", durationMs)
            : AiJson.objectOf("name", name, "status", status, "durationMs", durationMs)
      );
   }

   public synchronized void recordOpInterrupt(String kind) {
      this.writeEventLine("op_interrupt", AiJson.objectOf("kind", kind));
   }

   public synchronized void recordDeath() {
      this.writeEventLine("death", new JsonObject());
   }

   public synchronized void recordRespawn() {
      this.writeEventLine("respawn", new JsonObject());
   }

   public synchronized void recordDimensionChange(String from, String to) {
      this.writeEventLine("dimension_change", AiJson.objectOf("from", from, "to", to));
   }

   public synchronized void recordSplit(String name, String detail) {
      this.splits.add(new AiRunTrace.TraceSplit(name, Math.max(0L, System.currentTimeMillis() - this.startedAtMs), detail == null ? "" : detail));
      this.writeEventLine(
         "split", detail != null && !detail.isBlank() ? AiJson.objectOf("name", name, "detail", detail) : AiJson.objectOf("name", name)
      );
   }

   public synchronized void recordFinish(String outcome) {
      this.writeEventLine("finish", AiJson.objectOf("outcome", outcome));
   }

   static String formatEventLine(long ts, long startedAt, String kind, JsonObject data) {
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

   static boolean shouldRotate(long written, long incomingBytes, long limit) {
      return written > 0L && written + incomingBytes > limit;
   }

   private void writeEventLine(String kind, JsonObject data) {
      try {
         String line = formatEventLine(System.currentTimeMillis(), this.startedAtMs, kind, data);
         byte[] bytes = (line + "\n").getBytes(StandardCharsets.UTF_8);
         this.ensureWriterOpen();
         if (shouldRotate(this.bytesWritten, bytes.length, this.rotateLimitBytes)) {
            this.rotateFiles();
            this.ensureWriterOpen();
         }

         this.writer.write(line);
         this.writer.newLine();
         this.writer.flush();
         this.bytesWritten += bytes.length;
      } catch (Exception var5) {
      }
   }

   private void ensureWriterOpen() throws IOException {
      if (this.writer == null) {
         Files.createDirectories(this.traceFile.getParent());
         AiJson.auditPrivateFile(this.traceFile);
         this.bytesWritten = Files.isRegularFile(this.traceFile) ? Files.size(this.traceFile) : 0L;
         this.writer = Files.newBufferedWriter(this.traceFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
      }
   }

   private void rotateFiles() throws IOException {
      this.closeWriter();
      for (int i = 2; i >= 1; i--) {
         Path older = Path.of(this.traceFile + "." + i);
         if (Files.exists(older)) {
            Files.move(older, Path.of(this.traceFile + "." + (i + 1)), StandardCopyOption.REPLACE_EXISTING);
         }
      }

      Files.move(this.traceFile, this.rotatedFile, StandardCopyOption.REPLACE_EXISTING);
      this.bytesWritten = 0L;
   }

   private synchronized void closeWriter() {
      if (this.writer != null) {
         try {
            this.writer.close();
         } catch (IOException var2) {
         }

         this.writer = null;
      }
   }

   public synchronized List<String> tailLines(int n) {
      if (n <= 0) {
         return List.of();
      }

      List<String> lines = new ArrayList<>();

      for (Path path : List.of(this.rotatedFile, this.traceFile)) {
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

   public record TraceSplit(String name, long elapsedMs, String detail) {
   }
}
