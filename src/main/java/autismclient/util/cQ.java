package autismclient.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class cQ {
   public static final long init_J = 134217728L;
   private static final Gson init_Gson = new Gson();
   private final ArrayBlockingQueue<JsonObject> init_ArrayBlockingQueue;
   private final AtomicLong init_AtomicLong = new AtomicLong();
   private final Path init_Path;
   private final Path load_Path;
   private final JsonObject init_JsonObject;
   private final long load_J;
   private final CompletableFuture<Path> init_CompletableFuture = new CompletableFuture<>();
   private volatile boolean init_Z;
   private volatile String init_Str = "stopped";

   public cQ(Path archive, JsonObject metadata) {
      this(archive, metadata, 1024, 134217728L);
   }

   cQ(Path archive, JsonObject metadata, int capacity, long maxBytes) {
      this.init_Path = archive.toAbsolutePath().normalize();
      this.load_Path = this.init_Path.resolveSibling(this.init_Path.getFileName() + ".partial.jsonl.gz");
      this.init_JsonObject = metadata.deepCopy();
      this.init_ArrayBlockingQueue = new ArrayBlockingQueue<>(capacity);
      this.load_J = maxBytes;
      Thread worker = new Thread(this::init_V, "autism-combat-recorder");
      worker.setDaemon(true);
      worker.setPriority(1);
      worker.start();
   }

   public synchronized void init(JsonObject row) {
      if (!this.init_Z && !this.init_ArrayBlockingQueue.offer(row)) {
         this.init_AtomicLong.incrementAndGet();
      }
   }

   public synchronized CompletableFuture<Path> init(String reason) {
      if (!this.init_Z) {
         this.init_Str = reason;
         this.init_Z = true;
      }

      return this.init_CompletableFuture;
   }

   public Path init_Path() {
      return this.init_Path;
   }

   public long init_J() {
      return this.init_AtomicLong.get();
   }

   public boolean init_Z() {
      return this.init_Z;
   }

   public CompletableFuture<Path> init_CompletableFuture() {
      return this.init_CompletableFuture;
   }

   private void init_V() {
      Map<String, Long> counts = new TreeMap<>();
      long bytes = 0L;
      Path temporaryZip = this.init_Path.resolveSibling(this.init_Path.getFileName() + ".partial");

      try {
         Files.createDirectories(this.init_Path.getParent());
         if (Files.exists(this.init_Path)) {
            throw new FileAlreadyExistsException(this.init_Path.toString());
         }

         try (BufferedWriter out = new BufferedWriter(
               new OutputStreamWriter(
                  new GZIPOutputStream(Files.newOutputStream(this.load_Path, StandardOpenOption.CREATE_NEW), 8192, true), StandardCharsets.UTF_8
               )
            )) {
            out.write(init_Gson.toJson(this.init_JsonObject));
            out.newLine();
            long flushed = System.nanoTime();

            while (!this.init_Z || !this.init_ArrayBlockingQueue.isEmpty()) {
               JsonObject row = this.init_ArrayBlockingQueue.poll(100L, TimeUnit.MILLISECONDS);
               if (row != null) {
                  String line = init_Gson.toJson(row);
                  bytes += line.getBytes(StandardCharsets.UTF_8).length + 1;
                  if (bytes > this.load_J) {
                     synchronized (this) {
                        this.init_Str = "size_limit";
                        this.init_Z = true;
                        this.init_AtomicLong.incrementAndGet();
                        this.init_AtomicLong.addAndGet(this.init_ArrayBlockingQueue.size());
                        this.init_ArrayBlockingQueue.clear();
                        break;
                     }
                  }

                  counts.merge(row.get("kind").getAsString(), 1L, Long::sum);
                  out.write(line);
                  out.newLine();
                  if (System.nanoTime() - flushed >= 1000000000L) {
                     out.flush();
                     flushed = System.nanoTime();
                  }
               }
            }

            JsonObject footer = this.init(counts, bytes);
            footer.addProperty("kind", "footer");
            out.write(init_Gson.toJson(footer));
            out.newLine();
         }

         try (ZipOutputStream var18 = new ZipOutputStream(Files.newOutputStream(temporaryZip, StandardOpenOption.CREATE_NEW))) {
            var18.setLevel(0);
            var18.putNextEntry(new ZipEntry("trace.jsonl.gz"));
            Files.copy(this.load_Path, var18);
            var18.closeEntry();
            var18.putNextEntry(new ZipEntry("summary.json"));
            var18.write(init_Gson.toJson(this.init(counts, bytes)).getBytes(StandardCharsets.UTF_8));
            var18.closeEntry();
            var18.putNextEntry(new ZipEntry("README.txt"));
            var18.write(
               "Combat recording schema 1. Local observations, not server ground truth.\nAnalyze with: python tools/analyze_combat_log.py <this.zip>\nPositions are relative to each segment's first player position. Entity IDs are session-local.\nNo player names/UUIDs, server address, chat, credentials, inventory NBT, ping or TPS samples.\nA swing/held block/nearby sound is not proof of a successful hit/block. Legacy servers can omit block signals.\nCheck dropped rows before drawing conclusions. Marks are the user's own annotations.\n"
                  .getBytes(StandardCharsets.UTF_8)
            );
            var18.closeEntry();
         }

         Files.move(temporaryZip, this.init_Path);
         Files.delete(this.load_Path);
         this.init_CompletableFuture.complete(this.init_Path);
      } catch (IOException | RuntimeException | InterruptedException failure) {
         this.init_Z = true;
         if (failure instanceof InterruptedException) {
            Thread.currentThread().interrupt();
         }

         this.init_CompletableFuture.completeExceptionally(failure);
      }
   }

   private JsonObject init(Map<String, Long> counts, long bytes) {
      JsonObject result = new JsonObject();
      result.addProperty("schema", 1);
      result.addProperty("stop_reason", this.init_Str);
      result.addProperty("dropped_rows", this.init_AtomicLong.get());
      result.addProperty("uncompressed_bytes", bytes);
      result.add("row_counts", init_Gson.toJsonTree(counts));
      return result;
   }
}
