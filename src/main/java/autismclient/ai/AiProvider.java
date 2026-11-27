package autismclient.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class AiProvider implements AutoCloseable {
   static final String init_Str = "thinking-binding-controls-2026-08-01";
   private static final JsonObject init_JsonObject = AiJson.objectOf("type", "ephemeral", "ttl", "1h");
   static final int init_I = 2000;
   private static final long init_J = 300000L;
   private static final int load_I = 6;
   private final HttpClient init_HttpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20L)).followRedirects(Redirect.NEVER).build();
   private volatile CompletableFuture<?> init_CompletableFuture;
   private volatile InputStream init_InputStream;
   private volatile Thread init_Thread;
   private volatile boolean init_Z;
   private volatile long load_J = 2000L;
   private final AiSettings init_AiSettings;
   private volatile boolean load_Z;

   public AiProvider(AiSettings settings) {
      this.init_AiSettings = settings.copy();
      this.load_Z = init_Z_AiSettings(this.init_AiSettings);
   }

   static boolean init_Z_AiSettings(AiSettings s) {
      if (s.provider != AiSettings.Provider.OPENAI) {
         return false;
      }

      try {
         return "api.openai.com".equalsIgnoreCase(URI.create(AiHttp.baseUrl(s.provider, s.endpoint)).getHost());
      } catch (RuntimeException e) {
         return false;
      }
   }

   void init_V_J(long millis) {
      this.load_J = millis;
   }

   public AiSettings.Provider provider() {
      return this.init_AiSettings.provider;
   }

   public String model() {
      return this.init_AiSettings.model;
   }

   public synchronized void applySettings(AiSettings live) {
      if (live != null) {
         this.init_AiSettings.effort = AiSettings.normalizeEffort(live.effort);
         if (live.outputTokens >= 256 && live.outputTokens <= this.init_AiSettings.contextTokens / 2) {
            this.init_AiSettings.outputTokens = live.outputTokens;
         }
      }
   }

   public synchronized void init_V_Str(String level) {
      this.init_AiSettings.effort = AiSettings.normalizeEffort(level);
   }

   public synchronized String load() {
      return AiSettings.nearestEffort(AiSettings.effortLevels(this.init_AiSettings.provider, this.init_AiSettings.model), this.init_AiSettings.effort);
   }

   public JsonObject userMessage(String text) {
      return this.init_AiSettings.provider == AiSettings.Provider.GEMINI
         ? AiJson.objectOf("role", "user", "parts", List.of(AiJson.objectOf("text", text)))
         : AiJson.objectOf("role", "user", "content", text);
   }

   public void appendUserText(List<JsonObject> history, String text) {
      JsonObject last = history.isEmpty() ? null : history.getLast();
      boolean lastIsUser = last != null && "user".equals(AiJson.getString(last, "role", ""));
      if (this.init_AiSettings.provider == AiSettings.Provider.GEMINI) {
         if (lastIsUser) {
            if (!last.has("parts") || !last.get("parts").isJsonArray()) {
               last.add("parts", new JsonArray());
            }

            last.getAsJsonArray("parts").add(AiJson.objectOf("text", text));
         } else {
            history.add(this.userMessage(text));
         }
      } else if (this.init_AiSettings.provider == AiSettings.Provider.ANTHROPIC) {
         if (lastIsUser) {
            if (!last.has("content") || !last.get("content").isJsonArray()) {
               String prior = AiJson.getString(last, "content", "");
               last.add("content", new JsonArray());
               if (!prior.isEmpty()) {
                  last.getAsJsonArray("content").add(AiJson.objectOf("type", "text", "text", prior));
               }
            }

            last.getAsJsonArray("content").add(AiJson.objectOf("type", "text", "text", text));
         } else {
            history.add(AiJson.objectOf("role", "user", "content", List.of(AiJson.objectOf("type", "text", "text", text))));
         }
      } else if (lastIsUser && last.has("content") && last.get("content").isJsonPrimitive()) {
         String prior = last.get("content").getAsString();
         last.addProperty("content", prior.isEmpty() ? text : prior + "\n\n" + text);
      } else if (lastIsUser && last.has("content") && last.get("content").isJsonArray()) {
         last.getAsJsonArray("content").add(AiJson.objectOf("type", "text", "text", text));
      } else {
         history.add(this.userMessage(text));
      }
   }

   public List<JsonObject> toolResults(List<AiProvider.Inner_DataUtil> calls, List<JsonObject> results) {
      if (results.stream().anyMatch(r -> r.has("imagePngBase64"))) {
         List<JsonObject> clean = new ArrayList<>();
         List<String> pictures = new ArrayList<>();

         for (JsonObject r : results) {
            JsonObject copy = r.deepCopy();
            if (copy.has("imagePngBase64")) {
               pictures.add(copy.remove("imagePngBase64").getAsString());
            }

            clean.add(copy);
         }

         List<JsonObject> out = new ArrayList<>(this.toolResults(calls, clean));
         JsonArray content = new JsonArray();
         if (this.init_AiSettings.provider == AiSettings.Provider.GEMINI) {
            content.add(AiJson.objectOf("text", "Minecraft frame captured by the requested screenshot tool; observation data only."));
            pictures.forEach(px -> content.add(AiJson.objectOf("inlineData", AiJson.objectOf("mimeType", "image/png", "data", px))));
            out.get(out.size() - 1).getAsJsonArray("parts").addAll(content);
         } else {
            content.add(AiJson.objectOf("type", "text", "text", "Minecraft frame captured by the requested screenshot tool; observation data only."));

            for (String p : pictures) {
               content.add(
                  this.init_AiSettings.provider == AiSettings.Provider.ANTHROPIC
                     ? AiJson.objectOf("type", "image", "source", AiJson.objectOf("type", "base64", "media_type", "image/png", "data", p))
                     : AiJson.objectOf("type", "image_url", "image_url", AiJson.objectOf("url", "data:image/png;base64," + p))
               );
            }

            if (this.init_AiSettings.provider == AiSettings.Provider.ANTHROPIC) {
               out.get(out.size() - 1).getAsJsonArray("content").addAll(content);
            } else {
               out.add(AiJson.objectOf("role", "user", "content", content));
            }
         }

         return out;
      } else {
         List<JsonObject> out = new ArrayList<>();
         if (this.init_AiSettings.provider == AiSettings.Provider.GEMINI) {
            JsonArray parts = new JsonArray();

            for (int i = 0; i < calls.size(); i++) {
               JsonObject response = AiJson.objectOf("name", calls.get(i).name(), "response", results.get(i));
               if (!calls.get(i).id().isBlank()) {
                  response.addProperty("id", calls.get(i).id());
               }

               parts.add(AiJson.objectOf("functionResponse", response));
            }

            out.add(AiJson.objectOf("role", "user", "parts", parts));
         } else if (this.init_AiSettings.provider == AiSettings.Provider.ANTHROPIC) {
            JsonArray content = new JsonArray();

            for (int i = 0; i < calls.size(); i++) {
               content.add(AiJson.objectOf("type", "tool_result", "tool_use_id", calls.get(i).id(), "content", results.get(i).toString()));
            }

            out.add(AiJson.objectOf("role", "user", "content", content));
         } else {
            for (int i = 0; i < calls.size(); i++) {
               out.add(AiJson.objectOf("role", "tool", "tool_call_id", calls.get(i).id(), "content", results.get(i).toString()));
            }
         }

         return out;
      }
   }

   public JsonObject init_JsonObject_Str(String system, List<JsonObject> history, JsonArray functions) {
      return this.init_JsonObject_c_StreamUtil(AiContext.Inner_StreamUtil.of(system), history, functions, null);
   }

   public JsonObject init_JsonObject_Str(String system, List<JsonObject> history, JsonArray functions, String toolChoice) {
      return this.init_JsonObject_c_StreamUtil(AiContext.Inner_StreamUtil.of(system), history, functions, toolChoice);
   }

   public JsonObject init_JsonObject_c_StreamUtil(AiContext.Inner_StreamUtil prompt, List<JsonObject> history, JsonArray functions) {
      return this.init_JsonObject_c_StreamUtil(prompt, history, functions, null);
   }

   public synchronized JsonObject init_JsonObject_c_StreamUtil(AiContext.Inner_StreamUtil prompt, List<JsonObject> history, JsonArray functions, String toolChoice) {
      String effort = this.load();
      JsonObject body = AiJson.objectOf("model", this.init_AiSettings.model, "stream", true);
      JsonArray wireTools = new JsonArray();
      if (this.init_AiSettings.provider == AiSettings.Provider.GEMINI) {
         JsonArray declarations = new JsonArray();

         for (JsonElement function : functions) {
            JsonObject f = function.getAsJsonObject();
            declarations.add(AiJson.objectOf("name", f.get("name"), "description", f.get("description"), "parametersJsonSchema", f.get("parameters")));
         }

         JsonObject generation = AiJson.objectOf("maxOutputTokens", this.init_AiSettings.outputTokens);
         if (AiSettings.geminiThinkingLevels(this.init_AiSettings.model)) {
            generation.add("thinkingConfig", AiJson.objectOf("thinkingLevel", effort.toUpperCase(Locale.ROOT)));
         } else if (AiSettings.geminiThinkingBudget(this.init_AiSettings.model)) {
            int budget = AiSettings.thinkingBudget(this.init_AiSettings.provider, effort, this.init_AiSettings.outputTokens);
            if (budget > 0) {
               generation.add("thinkingConfig", AiJson.objectOf("thinkingBudget", budget));
            }
         }

         body = AiJson.objectOf(
            "systemInstruction",
            AiJson.objectOf("parts", List.of(AiJson.objectOf("text", prompt.text()))),
            "contents",
            history,
            "generationConfig",
            generation
         );
         if (!declarations.isEmpty()) {
            body.add("tools", AiJson.GSON.toJsonTree(List.of(AiJson.objectOf("functionDeclarations", declarations))));
            if (toolChoice != null) {
               body.add(
                  "toolConfig",
                  AiJson.objectOf("functionCallingConfig", AiJson.objectOf("mode", toolChoice.equals("none") ? "NONE" : "ANY"))
               );
            }
         }
      } else if (this.init_AiSettings.provider == AiSettings.Provider.ANTHROPIC) {
         for (JsonElement item : functions) {
            JsonObject f = item.getAsJsonObject();
            wireTools.add(AiJson.objectOf("name", f.get("name"), "description", f.get("description"), "input_schema", f.get("parameters")));
         }

         body.add("system", init_JsonArray_c_StreamUtil(prompt));
         body.add("messages", AiJson.GSON.toJsonTree(history));
         body.addProperty("max_tokens", this.init_AiSettings.outputTokens);
         if (!wireTools.isEmpty()) {
            body.add("tools", wireTools);
         }

         body.add("cache_control", init_JsonObject.deepCopy());
         JsonObject thinking = null;
         switch (AiSettings.anthropicThinking(this.init_AiSettings.model)) {
            case ADAPTIVE:
               thinking = AiJson.objectOf("type", "adaptive");
               body.add("output_config", AiJson.objectOf("effort", effort));
               break;
            case BUDGET:
               int budget = AiSettings.thinkingBudget(this.init_AiSettings.provider, effort, this.init_AiSettings.outputTokens);
               if (budget > 0) {
                  thinking = AiJson.objectOf("type", "enabled", "budget_tokens", budget);
               }
            case NONE:
         }

         if (thinking != null) {
            if (load(this.init_AiSettings)) {
               thinking.add("block_binding", AiJson.objectOf("prefix_mismatch_behavior", "drop_block"));
            }

            body.add("thinking", thinking);
         }

         if (!wireTools.isEmpty()) {
            if ("none".equals(toolChoice)) {
               body.add("tool_choice", AiJson.objectOf("type", "none"));
            } else if (toolChoice != null && thinking == null) {
               body.add("tool_choice", AiJson.objectOf("type", "any"));
            }
         }
      } else if (this.load_Z) {
         body = AiJson.objectOf(
            "model", this.init_AiSettings.model, "stream", true, "store", false, "instructions", prompt.text(), "input", init_JsonArray_List(history)
         );

         for (JsonElement item : functions) {
            JsonObject f = item.getAsJsonObject();
            wireTools.add(
               AiJson.objectOf(
                  "type", "function", "name", f.get("name"), "description", f.get("description"), "parameters", f.get("parameters"), "strict", false
               )
            );
         }

         if (!wireTools.isEmpty()) {
            body.add("tools", wireTools);
            if (toolChoice != null) {
               body.addProperty("tool_choice", toolChoice);
            }
         }

         body.addProperty("max_output_tokens", this.init_AiSettings.outputTokens);
         if (AiSettings.effortField(this.init_AiSettings.provider, this.init_AiSettings.model) != null) {
            body.add("reasoning", AiJson.objectOf("effort", effort));
         }
      } else {
         List<JsonObject> messages = new ArrayList<>();
         messages.add(AiJson.objectOf("role", "system", "content", prompt.text()));
         messages.addAll(history);

         for (JsonElement f : functions) {
            wireTools.add(AiJson.objectOf("type", "function", "function", f));
         }

         body.add("messages", AiJson.GSON.toJsonTree(messages));
         if (!wireTools.isEmpty()) {
            body.add("tools", wireTools);
         }

         boolean completionTokens = this.init_AiSettings.provider == AiSettings.Provider.OPENAI
            || this.init_AiSettings.provider == AiSettings.Provider.MOONSHOT
               && "reasoning_effort".equals(AiSettings.effortField(this.init_AiSettings.provider, this.init_AiSettings.model));
         body.addProperty(completionTokens ? "max_completion_tokens" : "max_tokens", this.init_AiSettings.outputTokens);
         body.add("stream_options", AiJson.objectOf("include_usage", true));
         String effortField = AiSettings.effortField(this.init_AiSettings.provider, this.init_AiSettings.model);
         String modelId = this.init_AiSettings.model.toLowerCase(Locale.ROOT);
         boolean thinkingRejectsForced = this.init_AiSettings.provider == AiSettings.Provider.DASHSCOPE
               && (modelId.startsWith("qwen") || "thinking_budget".equals(effortField))
            || this.init_AiSettings.provider == AiSettings.Provider.MOONSHOT && !modelId.startsWith("kimi-k3");
         if (!wireTools.isEmpty()
            && toolChoice != null
            && (this.init_AiSettings.provider != AiSettings.Provider.DEEPSEEK || "none".equals(toolChoice))
            && (!thinkingRejectsForced || !"required".equals(toolChoice))) {
            body.addProperty("tool_choice", toolChoice);
         }

         if ("reasoning_effort".equals(effortField)) {
            body.addProperty(effortField, effort);
         } else if (effortField != null) {
            int budget = AiSettings.thinkingBudget(this.init_AiSettings.provider, effort, this.init_AiSettings.outputTokens);
            if (budget > 0) {
               body.addProperty(effortField, budget);
            }
         }

         if (this.init_AiSettings.provider == AiSettings.Provider.DASHSCOPE && "thinking_budget".equals(effortField)) {
            body.addProperty("enable_thinking", true);
         }
      }

      return body;
   }

   static JsonArray init_JsonArray_c_StreamUtil(AiContext.Inner_StreamUtil prompt) {
      JsonArray system = new JsonArray();
      JsonObject stable = AiJson.objectOf("type", "text", "text", prompt.stable().isEmpty() ? "." : prompt.stable());
      stable.add("cache_control", init_JsonObject.deepCopy());
      system.add(stable);
      if (!prompt.objective().isEmpty()) {
         JsonObject objective = AiJson.objectOf("type", "text", "text", prompt.objective());
         if (prompt.objective().length() >= 2000) {
            objective.add("cache_control", init_JsonObject.deepCopy());
         }

         system.add(objective);
      }

      return system;
   }

   static JsonArray init_JsonArray_List(List<JsonObject> history) {
      JsonArray input = new JsonArray();

      for (JsonObject m : history) {
         String role = AiJson.getString(m, "role", "");
         if (role.equals("tool")) {
            input.add(
               AiJson.objectOf(
                  "type", "function_call_output", "call_id", AiJson.getString(m, "tool_call_id", ""), "output", AiJson.getString(m, "content", "")
               )
            );
         } else if (!role.equals("assistant")) {
            input.add(
               AiJson.objectOf("role", role.equals("system") ? "developer" : "user", "content", init_JsonElement_JsonElement(m.get("content")))
            );
         } else {
            String text = m.has("content") && m.get("content").isJsonPrimitive() ? m.get("content").getAsString() : "";
            if (!text.isBlank()) {
               input.add(AiJson.objectOf("role", "assistant", "content", text));
            }

            if (m.has("tool_calls") && m.get("tool_calls").isJsonArray()) {
               for (JsonElement item : m.getAsJsonArray("tool_calls")) {
                  if (item.isJsonObject()) {
                     JsonObject c = item.getAsJsonObject();
                     JsonObject f = c.has("function") && c.get("function").isJsonObject() ? c.getAsJsonObject("function") : new JsonObject();
                     String arguments = AiJson.getString(f, "arguments", "");
                     input.add(
                        AiJson.objectOf(
                           "type",
                           "function_call",
                           "call_id",
                           AiJson.getString(c, "id", ""),
                           "name",
                           AiJson.getString(f, "name", ""),
                           "arguments",
                           arguments.isBlank() ? "{}" : arguments
                        )
                     );
                  }
               }
            }
         }
      }

      return input;
   }

   private static JsonElement init_JsonElement_JsonElement(JsonElement content) {
      if (content != null && !content.isJsonNull()) {
         if (!content.isJsonArray()) {
            return content;
         }

         JsonArray parts = new JsonArray();

         for (JsonElement item : content.getAsJsonArray()) {
            if (item.isJsonObject()) {
               JsonObject p = item.getAsJsonObject();
               if ("image_url".equals(AiJson.getString(p, "type", ""))) {
                  JsonElement image = p.get("image_url");
                  String url = image != null && image.isJsonObject()
                     ? AiJson.getString(image.getAsJsonObject(), "url", "")
                     : (image != null && image.isJsonPrimitive() ? image.getAsString() : "");
                  parts.add(AiJson.objectOf("type", "input_image", "image_url", url));
               } else {
                  parts.add(AiJson.objectOf("type", "input_text", "text", AiJson.getString(p, "text", "")));
               }
            }
         }

         return parts;
      } else {
         return new JsonPrimitive("");
      }
   }

   static boolean load(AiSettings s) {
      if (s.provider != AiSettings.Provider.ANTHROPIC) {
         return false;
      }

      try {
         return "api.anthropic.com".equalsIgnoreCase(URI.create(s.endpoint.trim()).getHost());
      } catch (RuntimeException e) {
         return false;
      }
   }

   static String init_Str_AiSettings_JsonObject(AiSettings s, JsonObject body) {
      if (load(s) && body.has("thinking") && body.get("thinking").isJsonObject()) {
         return body.getAsJsonObject("thinking").has("block_binding") ? "thinking-binding-controls-2026-08-01" : null;
      } else {
         return null;
      }
   }

   static boolean init_Z_Str(String message) {
      if (message == null) {
         return false;
      }

      String m = message.toLowerCase(Locale.ROOT);

      for (String k : List.of(
         "prompt is too long",
         "context_length_exceeded",
         "maximum context length",
         "context window",
         "input is too long",
         "too many input tokens",
         "exceeds the context"
      )) {
         if (m.contains(k)) {
            return true;
         }
      }

      return false;
   }

   public AiProvider.Inner_BufferHelper init_bA_BufferHelper_Str(
      String system, List<JsonObject> history, JsonArray functions, Consumer<String> onText, BooleanSupplier cancelled
   ) throws IOException, InterruptedException, ExecutionException, TimeoutException {
      return this.completeWithContext(AiContext.Inner_StreamUtil.of(system), history, functions, null, onText, cancelled, call -> {});
   }

   public AiProvider.Inner_BufferHelper init_bA_BufferHelper_Str_2(
      String system,
      List<JsonObject> history,
      JsonArray functions,
      Consumer<String> onText,
      BooleanSupplier cancelled,
      Consumer<AiProvider.Inner_CachePool> onCallReady
   ) throws IOException, InterruptedException, ExecutionException, TimeoutException {
      return this.completeWithContext(AiContext.Inner_StreamUtil.of(system), history, functions, null, onText, cancelled, onCallReady);
   }

   public AiProvider.Inner_BufferHelper complete(
      String system, List<JsonObject> history, JsonArray functions, String toolChoice, Consumer<String> onText, BooleanSupplier cancelled
   ) throws IOException, InterruptedException, ExecutionException, TimeoutException {
      return this.completeWithContext(AiContext.Inner_StreamUtil.of(system), history, functions, toolChoice, onText, cancelled, call -> {});
   }

   public AiProvider.Inner_BufferHelper init_bA_BufferHelper_Str(
      String system,
      List<JsonObject> history,
      JsonArray functions,
      String toolChoice,
      Consumer<String> onText,
      BooleanSupplier cancelled,
      Consumer<AiProvider.Inner_CachePool> onCallReady
   ) throws IOException, InterruptedException, ExecutionException, TimeoutException {
      return this.completeWithContext(AiContext.Inner_StreamUtil.of(system), history, functions, toolChoice, onText, cancelled, onCallReady);
   }

   public AiProvider.Inner_BufferHelper completeWithContext(
      AiContext.Inner_StreamUtil prompt,
      List<JsonObject> history,
      JsonArray functions,
      String toolChoice,
      Consumer<String> onText,
      BooleanSupplier cancelled,
      Consumer<AiProvider.Inner_CachePool> onCallReady
   ) throws IOException, InterruptedException, ExecutionException, TimeoutException {
      this.init_AiSettings.validate();
      boolean responses = this.load_Z;
      JsonObject body = this.init_JsonObject_c_StreamUtil(prompt, history, functions, toolChoice);
      String beta = init_Str_AiSettings_JsonObject(this.init_AiSettings, body);
      String base = AiHttp.baseUrl(this.init_AiSettings.provider, this.init_AiSettings.endpoint);
      String suffix = this.init_AiSettings.provider == AiSettings.Provider.GEMINI
         ? "/models/"
            + URLEncoder.encode(this.init_AiSettings.model.replaceFirst("^models/", ""), StandardCharsets.UTF_8).replace("+", "%20")
            + ":streamGenerateContent?alt=sse"
         : (this.init_AiSettings.provider == AiSettings.Provider.ANTHROPIC ? "/messages" : (responses ? "/responses" : "/chat/completions"));
      String key = this.init_AiSettings.key(this.init_AiSettings.provider.name());
      Map<String, String> auth = AiHttp.authHeaders(this.init_AiSettings.provider, key);

      for (int attempt = 0; attempt < 6; attempt++) {
         if (cancelled.getAsBoolean() || this.init_Z) {
            throw new CancellationException();
         }

         Builder request = HttpRequest.newBuilder(URI.create(base + suffix))
            .timeout(Duration.ofSeconds(300L))
            .header("Content-Type", "application/json")
            .header("Accept", "text/event-stream")
            .POST(BodyPublishers.ofString(body.toString()));
         auth.forEach(request::header);
         if (beta != null) {
            request.header("anthropic-beta", beta);
         }

         CompletableFuture<HttpResponse<InputStream>> future = this.init_HttpClient.sendAsync(request.build(), BodyHandlers.ofInputStream());
         this.init_CompletableFuture = future;
         AtomicBoolean content = new AtomicBoolean();
         AtomicBoolean toolDeltas = new AtomicBoolean();

         try {
            HttpResponse<InputStream> response = null;
            long deadlineNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(300L);
            try {
               while (response == null) {
                  if (cancelled.getAsBoolean() || this.init_Z) {
                     future.cancel(true);
                     throw new CancellationException();
                  }

                  long remainingMs = TimeUnit.NANOSECONDS.toMillis(deadlineNanos - System.nanoTime());
                  if (remainingMs <= 0L) {
                     throw new TimeoutException("Timed out waiting for AI response headers");
                  }

                  try {
                     response = future.get(Math.min(250L, remainingMs), TimeUnit.MILLISECONDS);
                  } catch (TimeoutException ignored) {
                  }
               }
            } catch (TimeoutException e) {
               future.cancel(true);
               if (attempt < 5 && !this.init_Z && !cancelled.getAsBoolean()) {
                  this.init_V_HttpResponse(null, attempt, cancelled);
                  continue;
               }

               throw e;
            }

            InputStream responseBody = response.body();
            this.init_InputStream = responseBody;
            if (response.statusCode() != 200) {
               String detail;
               try (InputStream input = responseBody) {
                  detail = new String(input.readNBytes(4096), StandardCharsets.UTF_8);
               }

               int status = response.statusCode();
               if (status == 400 && !responses && this.init_AiSettings.provider == AiSettings.Provider.OPENAI && detail.contains("/v1/responses")) {
                  responses = true;
                  this.load_Z = true;
                  body = this.init_JsonObject_c_StreamUtil(prompt, history, functions, toolChoice);
                  suffix = "/responses";
                  attempt--;
                  continue;
               }

               if ((status == 429 || status >= 500) && attempt < 5) {
                  this.init_V_HttpResponse(response, attempt, cancelled);
                  continue;
               }

               String message = AiHttp.init(this.init_AiSettings.provider, status, detail, key);
               if (status != 429 && status < 500) {
                  throw new IOException(message);
               }

               throw new IOException("Provider retry limit reached; " + message);
            }

            AtomicLong activity = new AtomicLong(System.nanoTime());
            AtomicBoolean executing = new AtomicBoolean();
            Thread timer = new Thread(() -> init_V_InputStream(responseBody, activity, executing), "ai-provider-stall-watch");
            timer.setDaemon(true);
            timer.start();
            this.init_Thread = timer;
            Consumer<AiProvider.Inner_CachePool> timed = ready -> {
               executing.set(true);

               try {
                  onCallReady.accept(ready);
               } finally {
                  activity.set(System.nanoTime());
                  executing.set(false);
               }
            };

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new AiProvider.Inner_StreamUtil(responseBody, activity), StandardCharsets.UTF_8))) {
               return responses
                  ? init_bA_BufferHelper_BufferedReader(reader, onText, cancelled, content, toolDeltas, timed)
                  : init_bA_BufferHelper_AiSettings_Provide(this.init_AiSettings.provider, reader, onText, cancelled, content, toolDeltas, timed);
            }
         } catch (AiProvider.Inner_ConfigStore e) {
            if (attempt >= 5 || toolDeltas.get() || this.init_Z) {
               throw e;
            }

            this.init_V_HttpResponse(null, attempt, cancelled);
         } catch (IOException e) {
            String safe = AiHttp.init(e.getMessage(), key);
            if (!Objects.equals(safe, e.getMessage())) {
               throw new IOException(safe);
            }

            throw e;
         } finally {
            Thread timer = this.init_Thread;
            this.init_Thread = null;
            if (timer != null) {
               timer.interrupt();
            }

            InputStream input = this.init_InputStream;
            this.init_InputStream = null;
            if (input != null) {
               try {
                  input.close();
               } catch (IOException var49) {
               }
            }

            this.init_CompletableFuture = null;
         }
      }

      throw new IOException("Provider retry limit reached");
   }

   private void init_V_HttpResponse(HttpResponse<?> response, int attempt, BooleanSupplier cancelled) throws InterruptedException {
      long scheduled = this.load_J * (attempt == 0 ? 1 : (attempt == 1 ? 4 : 15));
      long header = response == null ? 0L : init_J_Str(response.headers().firstValue("Retry-After").orElse(null));
      long wait = Math.min(60000L, Math.max(scheduled, header));
      long slept = 0L;

      while (slept < wait) {
         if (cancelled.getAsBoolean()) {
            throw new CancellationException();
         }

         long slice = Math.min(100L, wait - slept);
         Thread.sleep(slice);
         slept += slice;
      }
   }

   private static long init_J_Str(String value) {
      if (value != null && !value.isBlank()) {
         String trimmed = value.trim();

         try {
            return Math.max(0L, Math.min(60L, Long.parseLong(trimmed))) * 1000L;
         } catch (NumberFormatException var4) {
            try {
               return Math.max(
                  0L, Math.min(60000L, DateTimeFormatter.RFC_1123_DATE_TIME.parse(trimmed, Instant::from).toEpochMilli() - System.currentTimeMillis())
               );
            } catch (RuntimeException e) {
               return 0L;
            }
         }
      } else {
         return 0L;
      }
   }

   private static void init_V_InputStream(InputStream body, AtomicLong activity, AtomicBoolean executing) {
      try {
         while (!Thread.currentThread().isInterrupted()) {
            if (executing.get()) {
               Thread.sleep(1000L);
            } else {
               long remaining = 300000L - (System.nanoTime() - activity.get()) / 1000000L;
               if (remaining <= 0L) {
                  try {
                     body.close();
                  } catch (IOException var6) {
                  }

                  return;
               }

               Thread.sleep(Math.min(remaining, 1000L));
            }
         }
      } catch (InterruptedException var7) {
      }
   }

   private static String init_Str_Str_I(String raw, int max) {
      String collapsed = AiHttp.init(raw, null).replaceAll("\\s+", " ").trim();
      return collapsed.length() <= max ? collapsed : collapsed.substring(0, max);
   }

   public static AiProvider.Inner_BufferHelper init_bA_BufferHelper_AiSettings_Provide(
      AiSettings.Provider provider, BufferedReader reader, Consumer<String> onText, BooleanSupplier cancelled
   ) throws AiProvider.Inner_ConfigStore, IOException {
      return init_bA_BufferHelper_AiSettings_Provide(provider, reader, onText, cancelled, new AtomicBoolean());
   }

   static AiProvider.Inner_BufferHelper init_bA_BufferHelper_AiSettings_Provide(
      AiSettings.Provider provider, BufferedReader reader, Consumer<String> onText, BooleanSupplier cancelled, AtomicBoolean content
   ) throws AiProvider.Inner_ConfigStore, IOException {
      return init_bA_BufferHelper_AiSettings_Provide(provider, reader, onText, cancelled, content, call -> {});
   }

   static AiProvider.Inner_BufferHelper init_bA_BufferHelper_AiSettings_Provide(
      AiSettings.Provider provider,
      BufferedReader reader,
      Consumer<String> onText,
      BooleanSupplier cancelled,
      AtomicBoolean content,
      Consumer<AiProvider.Inner_CachePool> onCallReady
   ) throws AiProvider.Inner_ConfigStore, IOException {
      return init_bA_BufferHelper_AiSettings_Provide(provider, reader, onText, cancelled, content, new AtomicBoolean(), onCallReady);
   }

   static AiProvider.Inner_BufferHelper init_bA_BufferHelper_AiSettings_Provide(
      AiSettings.Provider provider,
      BufferedReader reader,
      Consumer<String> onText,
      BooleanSupplier cancelled,
      AtomicBoolean content,
      AtomicBoolean toolDeltas,
      Consumer<AiProvider.Inner_CachePool> onCallReady
   ) throws AiProvider.Inner_ConfigStore, IOException {
      StringBuilder text = new StringBuilder();
      StringBuilder reasoning = new StringBuilder();
      TreeMap<Integer, JsonObject> blocks = new TreeMap<>();
      TreeMap<Integer, StringBuilder> arguments = new TreeMap<>();
      TreeMap<Integer, AiProvider.Inner_DataUtil> ready = new TreeMap<>();
      List<AiProvider.Inner_DataUtil> geminiCalls = new ArrayList<>();
      JsonArray geminiParts = new JsonArray();
      long[] meter = new long[5];
      String finish = "";
      int bytes = 0;

      String line;
      while ((line = AiBoundedIo.init(reader, 1000000)) != null) {
         if (cancelled.getAsBoolean()) {
            throw new CancellationException();
         }

         bytes += line.length();
         if (bytes > 4000000) {
            throw new IOException("Provider output exceeded limit");
         }

         if (line.startsWith("data:")) {
            String data = line.substring(5).trim();
            if (!data.equals("[DONE]")) {
               if (data.isBlank()) {
                  continue;
               }

               JsonObject event;
               try {
                  event = AiJson.parseObject(data);
               } catch (RuntimeException e) {
                  throw new IOException("Provider sent a malformed streaming event: " + init_Str_Str_I(data, 500), e);
               }

               if (!event.has("error") && !"error".equals(AiJson.getString(event, "type", ""))) {
                  if (provider == AiSettings.Provider.GEMINI) {
                     if (event.has("usageMetadata") && event.get("usageMetadata").isJsonObject()) {
                        content.set(true);
                        write(event.getAsJsonObject("usageMetadata"), meter);
                     }

                     JsonArray candidates = event.has("candidates") && event.get("candidates").isJsonArray() ? event.getAsJsonArray("candidates") : null;
                     if (candidates == null || candidates.isEmpty() || !candidates.get(0).isJsonObject()) {
                        continue;
                     }

                     JsonObject candidate = candidates.get(0).getAsJsonObject();
                     if (candidate.has("finishReason") && candidate.get("finishReason").isJsonPrimitive()) {
                        finish = candidate.get("finishReason").getAsString();
                     }

                     JsonObject contentObj = candidate.has("content") && candidate.get("content").isJsonObject() ? candidate.getAsJsonObject("content") : null;
                     JsonArray partsArr = contentObj != null && contentObj.has("parts") && contentObj.get("parts").isJsonArray()
                        ? contentObj.getAsJsonArray("parts")
                        : null;
                     if (partsArr != null) {
                        for (JsonElement part : partsArr) {
                           if (part == null || !part.isJsonObject()) {
                              continue;
                           }
                           content.set(true);
                           JsonElement copy = part.deepCopy();
                           geminiParts.add(copy);
                           JsonObject p = part.getAsJsonObject();
                           if (p.has("text") && p.get("text").isJsonPrimitive() && !AiJson.getBoolean(p, "thought", false)) {
                              String delta = p.get("text").getAsString();
                              text.append(delta);
                              onText.accept(delta);
                           }

                           if (p.has("functionCall") && p.get("functionCall").isJsonObject()) {
                              toolDeltas.set(true);
                              JsonObject f = copy.getAsJsonObject().getAsJsonObject("functionCall");
                              String callName = AiJson.getString(f, "name", "");
                              JsonObject callArgs = f.has("args") && f.get("args").isJsonObject() ? f.getAsJsonObject("args") : new JsonObject();
                              AiProvider.Inner_DataUtil call = new AiProvider.Inner_DataUtil(AiJson.getString(f, "id", ""), callName, callArgs);
                              geminiCalls.add(call);
                              if (geminiCalls.size() > 32) {
                                 throw new IOException("Too many tool calls");
                              }

                              onCallReady.accept(new AiProvider.Inner_CachePool(geminiCalls.size() - 1, call));
                           }
                        }
                     }
                     continue;
                  }

                  if (provider == AiSettings.Provider.ANTHROPIC) {
                     String type = AiJson.getString(event, "type", "");
                     int index = AiJson.getInt(event, "index", 0);
                     if (!type.equals("message_start")) {
                        if (type.equals("content_block_start")) {
                           blocks.put(index, event.getAsJsonObject("content_block").deepCopy());
                           if ("tool_use".equals(AiJson.getString(blocks.get(index), "type", ""))) {
                              content.set(true);
                              toolDeltas.set(true);
                           }
                        } else if (type.equals("content_block_delta")) {
                           JsonObject d = event.getAsJsonObject("delta");
                           JsonObject b = blocks.get(index);
                           if (b == null) {
                              throw new IOException("Missing content block");
                           }

                           content.set(true);
                           if (d.has("text")) {
                              String delta = d.get("text").getAsString();
                              init_V_JsonObject(b, "text", delta);
                              text.append(delta);
                              onText.accept(delta);
                           }

                           if (d.has("thinking")) {
                              init_V_JsonObject(b, "thinking", d.get("thinking").getAsString());
                           }

                           if (d.has("signature")) {
                              init_V_JsonObject(b, "signature", d.get("signature").getAsString());
                           }

                           if (d.has("partial_json")) {
                              arguments.computeIfAbsent(index, k -> new StringBuilder()).append(d.get("partial_json").getAsString());
                           }
                        } else if (type.equals("content_block_stop")) {
                           JsonObject b = blocks.get(index);
                           if (b != null && "tool_use".equals(AiJson.getString(b, "type", ""))) {
                              init_V_TreeMap(
                                 ready,
                                 index,
                                 init_bA_DataUtil_JsonObject_Str(b, arguments.containsKey(index) ? arguments.get(index).toString() : null),
                                 onCallReady
                              );
                           }
                        } else if (type.equals("message_delta")) {
                           finish = AiJson.getString(event.getAsJsonObject("delta"), "stop_reason", "");
                           if (event.has("usage") && event.get("usage").isJsonObject()) {
                              load(event.getAsJsonObject("usage"), meter);
                           }
                        }
                        continue;
                     }

                     content.set(true);
                     JsonObject message = event.has("message") && event.get("message").isJsonObject() ? event.getAsJsonObject("message") : null;
                     if (message != null && message.has("usage") && message.get("usage").isJsonObject()) {
                        load(message.getAsJsonObject("usage"), meter);
                     }
                     continue;
                  }

                  if (event.has("usage") && event.get("usage").isJsonObject()) {
                     content.set(true);
                     read(event.getAsJsonObject("usage"), meter);
                  }

                  JsonArray choices = event.has("choices") && event.get("choices").isJsonArray() ? event.getAsJsonArray("choices") : null;
                  if (choices == null || choices.isEmpty()) {
                     continue;
                  }

                  JsonObject choice = choices.get(0) != null && choices.get(0).isJsonObject() ? choices.get(0).getAsJsonObject() : null;
                  if (choice == null) {
                     continue;
                  }
                  if (choice.has("usage") && choice.get("usage").isJsonObject()) {
                     content.set(true);
                     read(choice.getAsJsonObject("usage"), meter);
                  }

                  boolean finishMarker = choice.has("finish_reason") && choice.get("finish_reason").isJsonPrimitive();
                  if (finishMarker) {
                     finish = choice.get("finish_reason").getAsString();
                  }

                  JsonObject delta = choice.has("delta") && choice.get("delta").isJsonObject() ? choice.getAsJsonObject("delta") : null;
                  if (delta == null) {
                     if (finishMarker) {
                        init_V_TreeMap(blocks, ready, onCallReady);
                     }
                     continue;
                  }

                  String deltaContent = AiJson.getString(delta, "content", "");
                  text.append(deltaContent);
                  if (!deltaContent.isEmpty()) {
                     content.set(true);
                     init_V_TreeMap(blocks, ready, onCallReady);
                     onText.accept(deltaContent);
                  }

                  reasoning.append(AiJson.getString(delta, "reasoning_content", ""));
                  if (delta.has("tool_calls") && delta.get("tool_calls").isJsonArray()) {
                     toolDeltas.set(true);
                     Set<Integer> touched = new HashSet<>();

                     for (JsonElement item : delta.getAsJsonArray("tool_calls")) {
                        if (item == null || !item.isJsonObject()) {
                           continue;
                        }
                        content.set(true);
                        JsonObject call = item.getAsJsonObject();
                        int index = AiJson.getInt(call, "index", 0);
                        touched.add(index);
                        JsonObject b = blocks.computeIfAbsent(
                           index, k -> AiJson.objectOf("type", "function", "function", AiJson.objectOf("name", "", "arguments", ""))
                        );
                        String id = AiJson.getString(call, "id", "");
                        if (!id.isEmpty() && AiJson.getString(b, "id", "").isEmpty()) {
                           b.addProperty("id", id);
                        }

                        if (call.has("function") && call.get("function").isJsonObject()) {
                           JsonObject f = call.getAsJsonObject("function");
                           JsonObject bf = b.getAsJsonObject("function");
                           String name = AiJson.getString(f, "name", "");
                           if (!name.isEmpty() && AiJson.getString(bf, "name", "").isEmpty()) {
                              bf.addProperty("name", name);
                           }

                           init_V_JsonObject(bf, "arguments", AiJson.getString(f, "arguments", ""));
                        }
                     }

                     for (int open : blocks.keySet()) {
                        if (!touched.contains(open)) {
                           load(blocks, ready, open, onCallReady);
                        }
                     }
                  }

                  if (finishMarker) {
                     init_V_TreeMap(blocks, ready, onCallReady);
                  }
                  continue;
               }

               throw new IOException(
                  "Provider returned a streaming error: "
                     + init_Str_Str_I(event.has("error") && !event.get("error").isJsonNull() ? event.get("error").toString() : data, 500)
               );
            }
            break;
         }
      }

      if (finish.isBlank()) {
         throw new AiProvider.Inner_ConfigStore("Provider stream ended before a completion marker");
      }

      if (Set.of("length", "max_tokens", "MAX_TOKENS").contains(finish)) {
         throw new IOException("Model output limit reached before a complete response");
      }

      if (finish.equals("model_context_window_exceeded")) {
         throw new IOException("Model context window exceeded before a complete response");
      }

      List<AiProvider.Inner_DataUtil> calls = new ArrayList<>();
      JsonObject wire;
      if (provider == AiSettings.Provider.GEMINI) {
         wire = AiJson.objectOf("role", "model", "parts", geminiParts);
         calls.addAll(geminiCalls);
      } else if (provider == AiSettings.Provider.ANTHROPIC) {
         Map<Integer, String> argumentErrors = new HashMap<>();

         for (Entry<Integer, StringBuilder> entry : arguments.entrySet()) {
            if (!ready.containsKey(entry.getKey())) {
               JsonObject block = blocks.get(entry.getKey());
               if (block != null) {
                  String raw = entry.getValue().toString();

                  try {
                     block.add("input", raw.isBlank() ? new JsonObject() : AiJson.parseObject(raw));
                  } catch (RuntimeException e) {
                     block.add("input", new JsonObject());
                     argumentErrors.put(entry.getKey(), init_Str_Str_I(raw, 1000));
                  }
               }
            }
         }

         wire = AiJson.objectOf("role", "assistant", "content", blocks.values());

         for (Entry<Integer, JsonObject> entry : blocks.entrySet()) {
            JsonObject b = entry.getValue();
            if ("tool_use".equals(AiJson.getString(b, "type", ""))) {
               AiProvider.Inner_DataUtil call = ready.get(entry.getKey());
               if (call == null) {
                  call = new AiProvider.Inner_DataUtil(
                     AiJson.getString(b, "id", ""),
                     AiJson.getString(b, "name", ""),
                     b.has("input") && b.get("input").isJsonObject() ? b.getAsJsonObject("input") : new JsonObject(),
                     argumentErrors.get(entry.getKey())
                  );
                  init_V_TreeMap(ready, entry.getKey(), call, onCallReady);
               }

               calls.add(call);
            }
         }
      } else {
         init_V_TreeMap(blocks, ready, onCallReady);
         wire = AiJson.objectOf("role", "assistant", "content", text.toString());
         if (!reasoning.isEmpty()) {
            wire.addProperty("reasoning_content", reasoning.toString());
         }

         if (!blocks.isEmpty()) {
            wire.add("tool_calls", AiJson.GSON.toJsonTree(blocks.values()));
         }

         for (int index : blocks.keySet()) {
            calls.add(ready.get(index));
         }
      }

      if (calls.size() > 32) {
         throw new IOException("Too many tool calls");
      }

      AiProvider.Inner_TaskQueue usage = new AiProvider.Inner_TaskQueue(meter[0], meter[1], meter[2], meter[3]);
      return new AiProvider.Inner_BufferHelper(wire, text.toString(), List.copyOf(calls), usage.reported() ? usage.total() : meter[4], finish, usage);
   }

   static AiProvider.Inner_BufferHelper init_bA_BufferHelper_BufferedReader(
      BufferedReader reader,
      Consumer<String> onText,
      BooleanSupplier cancelled,
      AtomicBoolean content,
      AtomicBoolean toolDeltas,
      Consumer<AiProvider.Inner_CachePool> onCallReady
   ) throws AiProvider.Inner_ConfigStore, IOException {
      StringBuilder text = new StringBuilder();
      TreeMap<Integer, JsonObject> blocks = new TreeMap<>();
      TreeMap<Integer, AiProvider.Inner_DataUtil> ready = new TreeMap<>();
      Set<Integer> streamedText = new HashSet<>();
      long[] meter = new long[5];
      String finish = "";
      int bytes = 0;

      String line;
      while ((line = AiBoundedIo.init(reader, 1000000)) != null) {
         if (cancelled.getAsBoolean()) {
            throw new CancellationException();
         }

         bytes += line.length();
         if (bytes > 4000000) {
            throw new IOException("Provider output exceeded limit");
         }

         if (line.startsWith("data:")) {
            String data = line.substring(5).trim();
            if (!data.isBlank() && !data.equals("[DONE]")) {
               JsonObject event;
               try {
                  event = AiJson.parseObject(data);
               } catch (RuntimeException e) {
                  throw new IOException("Provider sent a malformed streaming event: " + init_Str_Str_I(data, 500), e);
               }

               String type = AiJson.getString(event, "type", "");
               int index = event.has("output_index") ? AiJson.getInt(event, "output_index", 0) : 0;
               JsonObject item = event.has("item") && event.get("item").isJsonObject() ? event.getAsJsonObject("item") : null;
               JsonObject response = event.has("response") && event.get("response").isJsonObject() ? event.getAsJsonObject("response") : null;
               switch (type) {
                  case "error":
                     throw new IOException("Provider returned a streaming error: " + init_Str_Str_I(data, 500));
                  case "response.failed":
                     throw new IOException(
                        "Provider returned a streaming error: "
                           + init_Str_Str_I(
                              response != null && response.has("error") && !response.get("error").isJsonNull() ? response.get("error").toString() : data, 500
                           )
                     );
                  case "response.output_text.delta":
                  case "response.refusal.delta":
                     content.set(true);
                     streamedText.add(index);
                     String delta = AiJson.getString(event, "delta", "");
                     if (!delta.isEmpty()) {
                        text.append(delta);
                        onText.accept(delta);
                     }
                     break;
                  case "response.output_item.added":
                     content.set(true);
                     if (item != null && "function_call".equals(AiJson.getString(item, "type", ""))) {
                        toolDeltas.set(true);
                        blocks.put(index, init_JsonObject_JsonObject(item));
                     }
                     break;
                  case "response.function_call_arguments.delta":
                     content.set(true);
                     toolDeltas.set(true);
                     JsonObject b = blocks.get(index);
                     if (b != null) {
                        init_V_JsonObject(b.getAsJsonObject("function"), "arguments", AiJson.getString(event, "delta", ""));
                     }
                     break;
                  case "response.output_item.done":
                     if (item == null) {
                        break;
                     }

                     String itemType = AiJson.getString(item, "type", "");
                     if (itemType.equals("function_call")) {
                        content.set(true);
                        toolDeltas.set(true);
                        blocks.put(index, init_JsonObject_JsonObject(item));
                        init_V_TreeMap_2(blocks, ready, index, onCallReady);
                     } else if (itemType.equals("message") && !streamedText.contains(index) && item.has("content") && item.get("content").isJsonArray()) {
                        for (JsonElement part : item.getAsJsonArray("content")) {
                           if (part.isJsonObject()) {
                              JsonObject p = part.getAsJsonObject();
                              String piece = AiJson.getString(p, "text", AiJson.getString(p, "refusal", ""));
                              if (!piece.isEmpty()) {
                                 content.set(true);
                                 text.append(piece);
                                 onText.accept(piece);
                              }
                           }
                        }
                     }
                     break;
                  case "response.completed":
                  case "response.incomplete":
                     content.set(true);
                     if (response != null && response.has("usage") && response.get("usage").isJsonObject()) {
                        init_V_JsonObject_JArr(response.getAsJsonObject("usage"), meter);
                     }

                     if (type.equals("response.completed")) {
                        finish = blocks.isEmpty() ? "stop" : "tool_calls";
                     } else {
                        JsonObject details = response != null && response.has("incomplete_details") && response.get("incomplete_details").isJsonObject()
                           ? response.getAsJsonObject("incomplete_details")
                           : null;
                        String reason = details == null ? "" : AiJson.getString(details, "reason", "");
                        if (!reason.equals("max_output_tokens")) {
                           throw new IOException("Provider stopped the response early" + (reason.isEmpty() ? "" : ": " + reason));
                        }

                        finish = "length";
                     }
               }
            }
         }
      }

      if (finish.isBlank()) {
         throw new AiProvider.Inner_ConfigStore("Provider stream ended before a completion marker");
      }

      if (finish.equals("length")) {
         throw new IOException("Model output limit reached before a complete response");
      }

      init_V_TreeMap(blocks, ready, onCallReady);
      JsonObject wire = AiJson.objectOf("role", "assistant", "content", text.toString());
      if (!blocks.isEmpty()) {
         wire.add("tool_calls", AiJson.GSON.toJsonTree(blocks.values()));
      }

      List<AiProvider.Inner_DataUtil> calls = new ArrayList<>();

      for (int index : blocks.keySet()) {
         calls.add(ready.get(index));
      }

      if (calls.size() > 32) {
         throw new IOException("Too many tool calls");
      }

      AiProvider.Inner_TaskQueue usage = new AiProvider.Inner_TaskQueue(meter[0], meter[1], meter[2], meter[3]);
      return new AiProvider.Inner_BufferHelper(wire, text.toString(), List.copyOf(calls), usage.reported() ? usage.total() : meter[4], finish, usage);
   }

   private static JsonObject init_JsonObject_JsonObject(JsonObject item) {
      return AiJson.objectOf(
         "id",
         AiJson.getString(item, "call_id", ""),
         "type",
         "function",
         "function",
         AiJson.objectOf("name", AiJson.getString(item, "name", ""), "arguments", AiJson.getString(item, "arguments", ""))
      );
   }

   static void init_V_JsonObject_JArr(JsonObject u, long[] m) {
      long input = init_J_JsonObject_Str(u, "input_tokens");
      JsonObject details = u.has("input_tokens_details") && u.get("input_tokens_details").isJsonObject() ? u.getAsJsonObject("input_tokens_details") : null;
      long cached = init_Z_JsonObject_Str(details, "cached_tokens") ? init_J_JsonObject_Str(details, "cached_tokens") : 0L;
      m[1] = Math.min(cached, input);
      m[0] = Math.max(0L, input - cached);
      m[2] = 0L;
      m[3] = init_J_JsonObject_Str(u, "output_tokens");
      m[4] = init_J_JsonObject_Str(u, "total_tokens");
   }

   private static boolean init_Z_JsonObject_Str(JsonObject o, String key) {
      return o != null && o.has(key) && o.get(key).isJsonPrimitive();
   }

   private static long init_J_JsonObject_Str(JsonObject o, String key) {
      return init_Z_JsonObject_Str(o, key) ? o.get(key).getAsLong() : 0L;
   }

   static void load(JsonObject u, long[] m) {
      if (init_Z_JsonObject_Str(u, "input_tokens")) {
         m[0] = init_J_JsonObject_Str(u, "input_tokens");
      }

      if (init_Z_JsonObject_Str(u, "cache_read_input_tokens")) {
         m[1] = init_J_JsonObject_Str(u, "cache_read_input_tokens");
      }

      if (init_Z_JsonObject_Str(u, "cache_creation_input_tokens")) {
         m[2] = init_J_JsonObject_Str(u, "cache_creation_input_tokens");
      }

      if (init_Z_JsonObject_Str(u, "output_tokens")) {
         m[3] = init_J_JsonObject_Str(u, "output_tokens");
      }
   }

   static void read(JsonObject u, long[] m) {
      long prompt = init_J_JsonObject_Str(u, "prompt_tokens");
      if (!init_Z_JsonObject_Str(u, "prompt_cache_hit_tokens") && !init_Z_JsonObject_Str(u, "prompt_cache_miss_tokens")) {
         JsonObject details = u.has("prompt_tokens_details") && u.get("prompt_tokens_details").isJsonObject()
            ? u.getAsJsonObject("prompt_tokens_details")
            : null;
         long cached = init_Z_JsonObject_Str(details, "cached_tokens")
            ? init_J_JsonObject_Str(details, "cached_tokens")
            : init_J_JsonObject_Str(u, "cached_tokens");
         m[1] = Math.min(cached, prompt);
         m[0] = Math.max(0L, prompt - cached);
      } else {
         long hit = init_J_JsonObject_Str(u, "prompt_cache_hit_tokens");
         long miss = init_J_JsonObject_Str(u, "prompt_cache_miss_tokens");
         m[1] = hit;
         m[0] = hit + miss == 0L ? prompt : miss;
      }

      m[2] = 0L;
      m[3] = init_J_JsonObject_Str(u, "completion_tokens");
      m[4] = init_J_JsonObject_Str(u, "total_tokens");
   }

   static void write(JsonObject u, long[] m) {
      long prompt = init_J_JsonObject_Str(u, "promptTokenCount") + init_J_JsonObject_Str(u, "toolUsePromptTokenCount");
      long cached = init_J_JsonObject_Str(u, "cachedContentTokenCount");
      m[0] = Math.max(0L, prompt - cached);
      m[1] = Math.min(cached, prompt);
      m[2] = 0L;
      m[3] = init_J_JsonObject_Str(u, "candidatesTokenCount") + init_J_JsonObject_Str(u, "thoughtsTokenCount");
      m[4] = init_J_JsonObject_Str(u, "totalTokenCount");
   }

   private static AiProvider.Inner_DataUtil init_bA_DataUtil_Str(String id, String name, String rawArguments) {
      if (rawArguments.isBlank()) {
         return new AiProvider.Inner_DataUtil(id, name, new JsonObject());
      }

      try {
         return new AiProvider.Inner_DataUtil(id, name, AiJson.parseObject(rawArguments));
      } catch (RuntimeException e) {
         return new AiProvider.Inner_DataUtil(id, name, new JsonObject(), init_Str_Str_I(rawArguments, 1000));
      }
   }

   private static void init_V_TreeMap(TreeMap<Integer, AiProvider.Inner_DataUtil> ready, int index, AiProvider.Inner_DataUtil call, Consumer<AiProvider.Inner_CachePool> onCallReady) throws IOException {
      if (ready.putIfAbsent(index, call) == null) {
         if (ready.size() > 32) {
            throw new IOException("Too many tool calls");
         }

         onCallReady.accept(new AiProvider.Inner_CachePool(index, call));
      }
   }

   private static void init_V_TreeMap_2(
      TreeMap<Integer, JsonObject> blocks, TreeMap<Integer, AiProvider.Inner_DataUtil> ready, int index, Consumer<AiProvider.Inner_CachePool> onCallReady
   ) throws IOException {
      if (!ready.containsKey(index)) {
         JsonObject b = blocks.get(index);
         if (b != null) {
            JsonObject f = b.getAsJsonObject("function");
            init_V_TreeMap(
               ready,
               index,
               init_bA_DataUtil_Str(AiJson.getString(b, "id", ""), f.get("name").getAsString(), f.get("arguments").getAsString()),
               onCallReady
            );
         }
      }
   }

   private static void load(TreeMap<Integer, JsonObject> blocks, TreeMap<Integer, AiProvider.Inner_DataUtil> ready, int index, Consumer<AiProvider.Inner_CachePool> onCallReady) throws IOException {
      if (!ready.containsKey(index)) {
         JsonObject b = blocks.get(index);
         if (b != null) {
            JsonObject f = b.getAsJsonObject("function");
            String name = f.get("name").getAsString();
            String raw = f.get("arguments").getAsString();
            if (!name.isEmpty() && !raw.isBlank()) {
               try {
                  AiJson.parseObject(raw);
               } catch (RuntimeException e) {
                  return;
               }

               init_V_TreeMap(ready, index, init_bA_DataUtil_Str(AiJson.getString(b, "id", ""), name, raw), onCallReady);
            }
         }
      }
   }

   private static void init_V_TreeMap(TreeMap<Integer, JsonObject> blocks, TreeMap<Integer, AiProvider.Inner_DataUtil> ready, Consumer<AiProvider.Inner_CachePool> onCallReady) throws IOException {
      for (int index : blocks.keySet()) {
         init_V_TreeMap_2(blocks, ready, index, onCallReady);
      }
   }

   private static AiProvider.Inner_DataUtil init_bA_DataUtil_JsonObject_Str(JsonObject block, String raw) {
      String error = null;
      if (raw != null) {
         try {
            block.add("input", raw.isBlank() ? new JsonObject() : AiJson.parseObject(raw));
         } catch (RuntimeException e) {
            block.add("input", new JsonObject());
            error = init_Str_Str_I(raw, 1000);
         }
      }

      return new AiProvider.Inner_DataUtil(
         AiJson.getString(block, "id", ""),
         AiJson.getString(block, "name", ""),
         block.has("input") && block.get("input").isJsonObject() ? block.getAsJsonObject("input") : new JsonObject(),
         error
      );
   }

   private static void init_V_JsonObject(JsonObject o, String key, String value) {
      o.addProperty(key, AiJson.getString(o, key, "") + value);
   }

   @Override
   public void close() {
      this.init_Z = true;
      CompletableFuture<?> future = this.init_CompletableFuture;
      if (future != null) {
         future.cancel(true);
      }

      Thread timer = this.init_Thread;
      if (timer != null) {
         timer.interrupt();
      }

      InputStream input = this.init_InputStream;
      if (input != null) {
         try {
            input.close();
         } catch (IOException var5) {
         }
      }

      this.init_HttpClient.shutdownNow();
   }

   public record Inner_BufferHelper(JsonObject wireMessage, String text, List<AiProvider.Inner_DataUtil> calls, long tokens, String finish, AiProvider.Inner_TaskQueue usage) {
      public Inner_BufferHelper(JsonObject wireMessage, String text, List<AiProvider.Inner_DataUtil> calls, long tokens, String finish) {
         this(wireMessage, text, calls, tokens, finish, AiProvider.Inner_TaskQueue.NONE);
      }
   }

   public record Inner_CachePool(int index, AiProvider.Inner_DataUtil call) {
   }

   private static final class Inner_ConfigStore extends IOException {
      Inner_ConfigStore(String message) {
         super(message);
      }

      Inner_ConfigStore(String message, Throwable cause) {
         super(message, cause);
      }
   }

   public record Inner_DataUtil(String id, String name, JsonObject arguments, String argumentsError) {
      public Inner_DataUtil(String id, String name, JsonObject arguments) {
         this(id, name, arguments, null);
      }
   }

   private static final class Inner_StreamUtil extends FilterInputStream {
      private final AtomicLong init;

      Inner_StreamUtil(InputStream in, AtomicLong activity) {
         super(in);
         this.init = activity;
      }

      @Override
      public int read() throws AiProvider.Inner_ConfigStore {
         try {
            int r = super.read();
            if (r >= 0) {
               this.init.set(System.nanoTime());
            }

            return r;
         } catch (IOException e) {
            throw new AiProvider.Inner_ConfigStore("Provider stream read failed", e);
         }
      }

      @Override
      public int read(byte[] b, int off, int len) throws AiProvider.Inner_ConfigStore {
         try {
            int n = super.read(b, off, len);
            if (n > 0) {
               this.init.set(System.nanoTime());
            }

            return n;
         } catch (IOException e) {
            throw new AiProvider.Inner_ConfigStore("Provider stream read failed", e);
         }
      }
   }

   public record Inner_TaskQueue(long inputTokens, long cachedInputTokens, long cacheWriteTokens, long outputTokens) {
      public static final AiProvider.Inner_TaskQueue NONE = new AiProvider.Inner_TaskQueue(0L, 0L, 0L, 0L);

      public long promptTokens() {
         return this.inputTokens + this.cachedInputTokens + this.cacheWriteTokens;
      }

      public long total() {
         return this.promptTokens() + this.outputTokens;
      }

      public boolean reported() {
         return this.total() > 0L;
      }
   }
}
