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

final class AiScriptHost implements AutoCloseable {
   private static final Set<Process> LIVE_PROCESSES = ConcurrentHashMap.newKeySet();
   private final Path directory;
   private final AiScriptHost.Inner_DataUtil tools;
   private volatile Process process;
   private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "autism-ai-script-pipe");
      t.setDaemon(true);
      return t;
   });

   AiScriptHost(Path directory, AiScriptHost.Inner_DataUtil tools) {
      this.directory = directory;
      this.tools = tools;
   }

   JsonObject init(String source, int timeout, BooleanSupplier cancelled) throws IOException, InterruptedException, TimeoutException, ExecutionException {
      return this.runScript(source, timeout, cancelled, new JsonObject());
   }

   JsonObject init(String source, JsonArray fixtures, BooleanSupplier cancelled) throws IOException, InterruptedException, TimeoutException, ExecutionException {
      if (fixtures.size() <= 4 && fixtures.toString().length() <= 64000) {
         this.runScript(source, 3, cancelled, AiJson.objectOf("validateOnly", true));
         JsonArray results = new JsonArray();

         for (JsonElement fixture : fixtures) {
            results.add(this.runScript(source, 2, cancelled, AiJson.objectOf("fixture", fixture)));
         }

         JsonObject report = AiJson.objectOf(
            "status",
            "CHECKED",
            "sourceFingerprint",
            AiJson.sha256Hex(source),
            "fixtureResults",
            results,
            "coverage",
            fixtures.isEmpty()
               ? "Syntax only; no behavioral fixture supplied"
               : "Supplied tool fixtures only; live game and unrestricted Java effects are not simulated"
         );
         Path checks = this.directory.resolve("script-checks");
         AiJson.writeAtomicJson(checks.resolve(AiJson.sha256Hex(source) + ".json"), report);
         AiMemoryStore.prune(checks, 100);
         return report;
      } else {
         throw new IllegalArgumentException("Provide at most four bounded fixtures (64,000 characters total)");
      }
   }

   static boolean isSecretEnvVar(String name) {
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

   private JsonObject runScript(String source, int timeout, BooleanSupplier cancelled, JsonObject request) throws IOException, InterruptedException, TimeoutException, ExecutionException {
      if (!source.isBlank() && source.length() <= 128000) {
         request.addProperty("source", source);
         request.addProperty("deadlineSeconds", Math.max(1, Math.min(3600, timeout)) + 30);
         String requestLine = request.toString();
         if (requestLine.length() > 500000) {
            throw new IllegalArgumentException("Script request exceeds 500,000 characters after encoding; shorten the script");
         }

         Path scripts = this.directory.resolve("scripts");
         Files.createDirectories(scripts);
         String fingerprint = AiJson.sha256Hex(source);
         Path saved = scripts.resolve(fingerprint + ".js");
         AiJson.auditPrivateFile(saved);
         Files.writeString(saved, source);
         AiMemoryStore.prune(scripts, 100);
         Path jar = this.directory.resolve("runtime").resolve("worker.jar");
         Files.createDirectories(jar.getParent());

         try (InputStream bundled = AiScriptHost.class.getResourceAsStream("/autism-ai/worker.jar")) {
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
         builder.environment().keySet().removeIf(AiScriptHost::isSecretEnvVar);
         this.process = builder.start();
         Process live = this.process;
         LIVE_PROCESSES.add(live);
         long deadline = System.nanoTime() + Math.max(1, Math.min(3600, timeout)) * 1000000000L;
         BufferedReader input = new BufferedReader(new InputStreamReader(this.process.getInputStream(), StandardCharsets.UTF_8));
         PrintWriter output = new PrintWriter(new OutputStreamWriter(this.process.getOutputStream(), StandardCharsets.UTF_8), true);

         try {
            output.println(requestLine);
            int total = 0;

            label165:
            while (true) {
               Future<String> next = this.executor.submit(() -> AiBoundedIo.init(input, 256000));

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

                  JsonObject message = AiJson.parseObject(line);
                  if (message.has("error")) {
                     throw new IllegalStateException(message.get("error").getAsString());
                  }

                  if (message.has("result")) {
                     return AiJson.objectOf(
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

                  String name = AiJson.getString(message, "tool", "");
                  if (!name.equals("script_run") && !name.equals("script_check") && !name.equals("finish")) {
                     try {
                        output.println(this.tools.call(name, message.getAsJsonObject("arguments")));
                     } catch (Exception e) {
                        output.println(AiJson.objectOf("status", "FAILED", "error", e.getMessage()));
                     }
                     continue label165;
                  }

                  throw new IllegalArgumentException("Scripts cannot recursively launch workers or finish the parent task");
               }

               throw new CancellationException();
            }
         } finally {
            this.init();
            LIVE_PROCESSES.remove(live);
            output.close();
            input.close();
         }
      } else {
         throw new IllegalArgumentException("Script requires 1..128000 characters");
      }
   }

   void init() {
      Process p = this.process;
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
      this.executor.shutdownNow();
   }

   static {
      Runtime.getRuntime().addShutdownHook(new Thread(() -> LIVE_PROCESSES.forEach(p -> {
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
