package autismclient.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

final class bZ implements AutoCloseable {
   private static final Set<Process> init_Set = ConcurrentHashMap.newKeySet();
   private final Path init_Path;
   private final bZ.Inner_DataUtil init_bZ_DataUtil;
   private volatile Process init_Process;
   private final ExecutorService init_ExecutorService = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "autism-ai-script-pipe");
      t.setDaemon(true);
      return t;
   });

   bZ(Path directory, bZ.Inner_DataUtil tools) {
      this.init_Path = directory;
      this.init_bZ_DataUtil = tools;
   }

   JsonObject init(String source, int timeout, BooleanSupplier cancelled) throws IOException, InterruptedException, TimeoutException, ExecutionException {
      return this.init(source, timeout, cancelled, new JsonObject());
   }

   JsonObject init(String source, JsonArray fixtures, BooleanSupplier cancelled) throws IOException, InterruptedException, TimeoutException, ExecutionException {
      if (fixtures.size() <= 4 && fixtures.toString().length() <= 64000) {
         this.init(source, 3, cancelled, ag.init_JsonObject_ObjArr("validateOnly", true));
         JsonArray results = new JsonArray();

         for (JsonElement fixture : fixtures) {
            results.add(this.init(source, 2, cancelled, ag.init_JsonObject_ObjArr("fixture", fixture)));
         }

         JsonObject report = ag.init_JsonObject_ObjArr(
            "status",
            "CHECKED",
            "sourceFingerprint",
            ag.init_Str_Str(source),
            "fixtureResults",
            results,
            "coverage",
            fixtures.isEmpty()
               ? "Syntax only; no behavioral fixture supplied"
               : "Supplied tool fixtures only; live game and unrestricted Java effects are not simulated"
         );
         Path checks = this.init_Path.resolve("script-checks");
         ag.init_V_Path_Obj(checks.resolve(ag.init_Str_Str(source) + ".json"), report);
         AiMemoryStore.init_V_Path_I(checks, 100);
         return report;
      } else {
         throw new IllegalArgumentException("Provide at most four bounded fixtures (64,000 characters total)");
      }
   }

   static boolean init(String name) {
      String key = name.toUpperCase(Locale.ROOT);
      return key.contains("API_KEY")
         || key.endsWith("TOKEN")
         || key.contains("SECRET")
         || key.contains("PASSWORD")
         || key.contains("CREDENTIAL")
         || key.endsWith("_KEY")
         || key.endsWith("_PASS")
         || key.endsWith("_PWD")
         || key.endsWith("_AUTH")
         || key.endsWith("_SESSION")
         || key.endsWith("_COOKIE")
         || key.contains("BEARER")
         || key.contains("JWT")
         || key.contains("KUBECONFIG")
         || key.startsWith("AWS_")
         || key.startsWith("GCP_")
         || key.startsWith("AZURE_")
         || key.startsWith("SSH_");
   }

   private JsonObject init(String source, int timeout, BooleanSupplier cancelled, JsonObject request) throws IOException, InterruptedException, TimeoutException, ExecutionException {
      if (!source.isBlank() && source.length() <= 128000) {
         request.addProperty("source", source);
         request.addProperty("deadlineSeconds", Math.max(1, Math.min(3600, timeout)) + 30);
         String requestLine = request.toString();
         if (requestLine.length() > 500000) {
            throw new IllegalArgumentException("Script request exceeds 500,000 characters after encoding; shorten the script");
         }

         Path scripts = this.init_Path.resolve("scripts");
         Files.createDirectories(scripts);
         String fingerprint = ag.init_Str_Str(source);
         Path saved = scripts.resolve(fingerprint + ".js");
         ag.auditPrivateFile(saved);
         Files.writeString(saved, source);
         AiMemoryStore.init_V_Path_I(scripts, 100);
         Path jar = this.init_Path.resolve("runtime").resolve("worker.jar");
         Files.createDirectories(jar.getParent());

         try (InputStream bundled = bZ.class.getResourceAsStream("/autism-ai/worker.jar")) {
            if (bundled == null) {
               throw new IOException("Bundled script worker missing; build the full jar with prepareAiWorker");
            }

            byte[] bytes = bundled.readAllBytes();
            if (!Files.exists(jar) || Files.size(jar) != bytes.length || !Arrays.equals(Files.readAllBytes(jar), bytes)) {
               Path temp = Files.createTempFile(jar.getParent(), "worker-", ".jar");
               Files.write(temp, bytes);

               try {
                  Files.move(temp, jar, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
               } catch (AtomicMoveNotSupportedException e) {
                  Files.move(temp, jar, StandardCopyOption.REPLACE_EXISTING);
               }
            }
         }

         String java = Path.of(System.getProperty("java.home"), "bin", System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java")
            .toString();
         ProcessBuilder builder = new ProcessBuilder(java, "-Xmx256m", "-jar", jar.toAbsolutePath().toString());
         builder.directory(scripts.toFile());
         builder.redirectError(Redirect.DISCARD);
         builder.environment().keySet().removeIf(bZ::init);
         this.init_Process = builder.start();
         Process live = this.init_Process;
         init_Set.add(live);
         long deadline = System.nanoTime() + Math.max(1, Math.min(3600, timeout)) * 1000000000L;
         BufferedReader input = new BufferedReader(new InputStreamReader(this.init_Process.getInputStream(), StandardCharsets.UTF_8));
         PrintWriter output = new PrintWriter(new OutputStreamWriter(this.init_Process.getOutputStream(), StandardCharsets.UTF_8), true);

         try {
            output.println(requestLine);
            int total = 0;

            label165:
            while (true) {
               Future<String> next = this.init_ExecutorService.submit(() -> RenderState.init(input, 256000));

               while (!cancelled.getAsBoolean()) {
                  if (System.nanoTime() > deadline) {
                     throw new TimeoutException("Script deadline reached");
                  }

                  String line;
                  try {
                     line = next.get(100L, TimeUnit.MILLISECONDS);
                  } catch (TimeoutException var33) {
                     continue;
                  }

                  if (line == null) {
                     throw new IOException("Script worker exited without a result");
                  }

                  total += line.length();
                  if (total > 2000000) {
                     throw new IOException("Script output limit exceeded");
                  }

                  JsonObject message = ag.init_JsonObject_Str(line);
                  if (message.has("error")) {
                     throw new IllegalStateException(message.get("error").getAsString());
                  }

                  if (message.has("result")) {
                     return ag.init_JsonObject_ObjArr(
                        "status",
                        "EXECUTED",
                        "message",
                        "Script returned; gameplay effects require verification",
                        "sourceFingerprint",
                        fingerprint,
                        "result",
                        message.get("result")
                     );
                  }

                  String name = ag.init_Str_JsonObject(message, "tool", "");
                  if (!name.equals("script_run") && !name.equals("script_check") && !name.equals("finish")) {
                     try {
                        output.println(this.init_bZ_DataUtil.call(name, message.getAsJsonObject("arguments")));
                     } catch (Exception e) {
                        output.println(ag.init_JsonObject_ObjArr("status", "FAILED", "error", e.getMessage()));
                     }
                     continue label165;
                  }

                  throw new IllegalArgumentException("Scripts cannot recursively launch workers or finish the parent task");
               }

               throw new CancellationException();
            }
         } finally {
            this.init();
            init_Set.remove(live);
            output.close();
            input.close();
         }
      } else {
         throw new IllegalArgumentException("Script requires 1..128000 characters");
      }
   }

   void init() {
      Process p = this.init_Process;
      if (p != null) {
         try {
            if (p.isAlive()) {
               Set<Long> keep = new HashSet<>();

               for (Optional<ProcessHandle> h = Optional.of(ProcessHandle.current()); h.isPresent(); h = h.get().parent()) {
                  keep.add(h.get().pid());
               }

               p.descendants().filter(hx -> !keep.contains(hx.pid())).forEach(hx -> {
                  try {
                     hx.destroyForcibly();
                  } catch (RuntimeException var2x) {
                  }
               });
            }
         } catch (RuntimeException var6) {
         }

         try {
            p.descendants().forEach(ProcessHandle::destroyForcibly);
         } catch (RuntimeException var4) {
         }

         try {
            p.destroyForcibly();
         } catch (RuntimeException var5) {
         }

         try {
            p.waitFor(2L, TimeUnit.SECONDS);
         } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
         }
      }
   }

   @Override
   public void close() {
      this.init();
      this.init_ExecutorService.shutdownNow();
   }

   static {
      Runtime.getRuntime().addShutdownHook(new Thread(() -> init_Set.forEach(p -> {
         try {
            p.descendants().forEach(ProcessHandle::destroyForcibly);
         } catch (RuntimeException ignored) {
         }
         p.destroyForcibly();
      }), "autism-ai-script-cleanup"));
   }

   interface Inner_DataUtil {
      JsonObject call(String var1, JsonObject var2) throws Exception;
   }
}
