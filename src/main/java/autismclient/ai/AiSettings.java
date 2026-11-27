package autismclient.ai;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class AiSettings {
   public static final List<String> EFFORTS = List.of("low", "medium", "high", "max");
   public static final String DEFAULT_EFFORT = "high";
   public int version = 1;
   public boolean builtIn;
   public AiSettings.Provider provider = AiSettings.Provider.OPENAI;
   public String endpoint;
   public String model;
   public int contextTokens;
   public int outputTokens;
   public int maxRequests;
   public long maxTokens;
   public int maxMinutes;
   public int emergencyKey;
   public boolean vision;
   public boolean nativeSearch;
   public boolean unrestrictedScripts;
   public boolean advanced;
   public String effort;
   public boolean autoRespawn;
   public boolean stopOnInput;
   public boolean allowTeleport;
   public boolean allowClips;
   public boolean allowFly;
   public boolean aiSilentAim;
   public boolean autoRejoin;
   public boolean planMode;
   public boolean verifyOutcomes;
   public boolean hostIntents;
   public boolean pinned;
   public boolean visualizePaths;
   public boolean visualizeTargets;
   public boolean visualizeGhosts;
   public int ghostOpacity;
   public int visualizationDistance;
   public int visualizationBlocks;
   public Map<String, List<String>> knownModels;
   public Map<String, String> sealedKeys;

   public AiSettings() {
      this.endpoint = AiSettings.Provider.OPENAI.endpoint;
      this.model = defaultModel(AiSettings.Provider.OPENAI);
      this.contextTokens = 131072;
      this.outputTokens = 16384;
      this.maxRequests = 100000;
      this.maxTokens = 1000000000L;
      this.maxMinutes = 43200;
      this.emergencyKey = 299;
      this.vision = true;
      this.nativeSearch = true;
      this.unrestrictedScripts = false;
      this.effort = "high";
      this.autoRespawn = true;
      this.stopOnInput = false;
      this.autoRejoin = true;
      this.planMode = false;
      this.verifyOutcomes = false;
      this.hostIntents = true;
      this.pinned = false;
      this.visualizePaths = true;
      this.visualizeTargets = true;
      this.visualizeGhosts = true;
      this.ghostOpacity = 42;
      this.visualizationDistance = 96;
      this.visualizationBlocks = 1024;
      this.knownModels = new HashMap<>();
      this.sealedKeys = new HashMap<>();
   }

   public static String defaultModel(AiSettings.Provider p) {
      String var10000;
      switch (p) {
         case OPENAI:
            var10000 = "gpt-6-astra";
            break;
         case DASHSCOPE:
            var10000 = "qwen3.8-max";
            break;
         case MOONSHOT:
            var10000 = "kimi-k3";
            break;
         case DEEPSEEK:
            var10000 = "deepseek-flash";
            break;
         case GEMINI:
            var10000 = "gemini-3.8-flash";
            break;
         case ANTHROPIC:
            var10000 = "claude-opus-5";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public static int defaultOutputTokens(AiSettings.Provider p) {
      char var10000;
      switch (p) {
         case GEMINI:
         case ANTHROPIC:
            var10000 = '耀';
            break;
         default:
            var10000 = 16384;
      }

      return var10000;
   }

   public static boolean defaultVision(AiSettings.Provider p) {
      boolean var10000;
      switch (p) {
         case OPENAI:
         case DEEPSEEK:
         case GEMINI:
         case ANTHROPIC:
            var10000 = true;
            break;
         case DASHSCOPE:
         case MOONSHOT:
         default:
            var10000 = false;
      }

      return var10000;
   }

   public static String effortField(AiSettings.Provider p, String model) {
      String id = modelId(model);
      String var10000;
      switch (p) {
         case OPENAI:
            var10000 = id.matches("(?:gpt-[56](?:[.-].*)?|o[134](?:-.*)?)") ? "reasoning_effort" : null;
            break;
         case DASHSCOPE:
            var10000 = !id.equals("kimi-k3") && !id.equals("kimi/kimi-k3") && !id.startsWith("deepseek-v4") && !id.startsWith("glm-5")
               ? (
                  !id.contains("instruct")
                        && !id.contains("coder")
                        && !id.contains("omni")
                        && (id.matches("qwen3(?:[.-].*)?") || id.matches("kimi-k2\\.[567](?:-.*)?") || id.equals("kimi-k2-thinking"))
                     ? "thinking_budget"
                     : null
               )
               : "reasoning_effort";
            break;
         case MOONSHOT:
            var10000 = !id.equals("kimi-k3") && !id.startsWith("kimi-k3-") ? null : "reasoning_effort";
            break;
         case DEEPSEEK:
            var10000 = !id.equals("deepseek-flash") && !id.startsWith("deepseek-v4") ? null : "reasoning_effort";
            break;
         default:
            var10000 = null;
      }

      return var10000;
   }

   public static String effortField(AiSettings.Provider p) {
      return effortField(p, defaultModel(p));
   }

   private static String modelId(String model) {
      return model == null ? "" : model.trim().toLowerCase(Locale.ROOT).replaceFirst("^models/", "");
   }

   public static String normalizeEffort(String effort) {
      String e = effort == null ? "" : effort.trim().toLowerCase(Locale.ROOT);
      return EFFORTS.contains(e) ? e : "high";
   }

   public static String nextEffort(String effort) {
      int i = EFFORTS.indexOf(effort);
      return i < 0 ? EFFORTS.getFirst() : EFFORTS.get((i + 1) % EFFORTS.size());
   }

   public static List<String> effortLevels(AiSettings.Provider provider, String model) {
      List var10000;
      switch (provider) {
         case OPENAI:
            var10000 = effortField(provider, model) == null ? List.of() : List.of("low", "medium", "high");
            break;
         case DASHSCOPE:
            String field = effortField(provider, model);
            String id = modelId(model);
            var10000 = field == null
               ? List.of()
               : (
                  field.equals("thinking_budget")
                     ? EFFORTS
                     : (
                        id.equals("kimi/kimi-k3")
                           ? List.of("max")
                           : (
                              !id.equals("kimi-k3")
                                    && !id.startsWith("glm-5.3")
                                    && !id.startsWith("deepseek-v4.1")
                                    && !id.equals("deepseek-v4-flash-0731")
                                    && !id.equals("deepseek-v4-pro-0813")
                                 ? List.of("high", "max")
                                 : List.of("low", "high", "max")
                           )
                     )
               );
            break;
         case MOONSHOT:
         case DEEPSEEK:
            var10000 = effortField(provider, model) == null ? List.of() : List.of("low", "high", "max");
            break;
         case GEMINI:
            if (geminiThinkingLevels(model)) {
               String id2 = modelId(model);
               var10000 = id2.startsWith("gemini-3-pro")
                  ? List.of("low", "high")
                  : (id2.startsWith("gemini-3.1-flash-lite-image") ? List.of("high") : List.of("low", "medium", "high"));
            } else {
               var10000 = geminiThinkingBudget(model) ? EFFORTS : List.of();
            }
            break;
         case ANTHROPIC:
            var10000 = anthropicThinking(model) == AiSettings.Thinking.NONE ? List.of() : EFFORTS;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public static String nearestEffort(List<String> levels, String effort) {
      String e = normalizeEffort(effort);
      if (!levels.isEmpty() && !levels.contains(e)) {
         int want = EFFORTS.indexOf(e);
         String best = levels.getFirst();
         int bestDistance = 2147483647;

         for (String level : levels) {
            int at = EFFORTS.indexOf(level);
            int distance = Math.abs(at - want);
            if (distance < bestDistance || distance == bestDistance && at > EFFORTS.indexOf(best)) {
               best = level;
               bestDistance = distance;
            }
         }

         return best;
      } else {
         return e;
      }
   }

   public static AiSettings.Thinking anthropicThinking(String model) {
      if (modelId(model).equals("claude-mythos-preview")) {
         return AiSettings.Thinking.ADAPTIVE;
      } else {
         double version = claudeVersion(model);
         if (version < 0.0) {
            return AiSettings.Thinking.NONE;
         } else if (version < 3.7) {
            return AiSettings.Thinking.NONE;
         } else {
            return version >= 4.6 ? AiSettings.Thinking.ADAPTIVE : AiSettings.Thinking.BUDGET;
         }
      }
   }

   static double claudeVersion(String model) {
      if (model == null) {
         return -1.0;
      }

      String id = model.toLowerCase(Locale.ROOT);
      int at = id.indexOf("claude-");
      if (at < 0) {
         return -1.0;
      }

      String[] parts = id.substring(at + 7).split("[^a-z0-9]+");
      int i = 0;
      if (i < parts.length && !isNumber(parts[i])) {
         i++;
      }

      if (i < parts.length && isNumber(parts[i]) && parts[i].length() <= 2) {
         int major = Integer.parseInt(parts[i]);
         int minor = 0;
         if (i + 1 < parts.length && isNumber(parts[i + 1]) && parts[i + 1].length() <= 2) {
            minor = Integer.parseInt(parts[i + 1]);
         }

         return major + minor / 10.0;
      } else {
         return -1.0;
      }
   }

   private static boolean isNumber(String s) {
      return !s.isEmpty() && s.chars().allMatch(Character::isDigit);
   }

   public static boolean geminiThinkingLevels(String model) {
      String id = modelId(model);
      return id.matches("gemini-3(?:\\.[1-8])?-(?:pro|flash)(?:-.*)?") && !id.contains("tts") && !id.contains("audio");
   }

   public static boolean geminiThinkingBudget(String model) {
      String id = modelId(model);
      return id.matches("gemini-2\\.5-(?:pro|flash)(?:-.*)?") && !id.contains("image") && !id.contains("tts") && !id.contains("audio");
   }

   static int thinkingBudget(AiSettings.Provider provider, String effort, int outputTokens) {
      int var10000;
      switch (effort) {
         case "low":
            var10000 = provider == AiSettings.Provider.ANTHROPIC ? 2048 : 1024;
            break;
         case "medium":
            var10000 = provider == AiSettings.Provider.ANTHROPIC ? 8192 : 4096;
            break;
         case "max":
            var10000 = provider == AiSettings.Provider.ANTHROPIC ? 32000 : (provider == AiSettings.Provider.GEMINI ? 24576 : 32768);
            break;
         default:
            var10000 = 16384;
      }

      int wanted = var10000;
      int cap = outputTokens - 1024;
      return cap < 1024 ? 0 : Math.max(1024, Math.min(wanted, cap));
   }

   public static boolean isDefaultModel(String model) {
      if (model != null && !model.isBlank()) {
         for (AiSettings.Provider p : AiSettings.Provider.values()) {
            if (defaultModel(p).equals(model)) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public static boolean shouldAutofillModel(String model) {
      return model == null || model.isBlank() || isDefaultModel(model.trim());
   }

   public static boolean shouldAutofillEndpoint(String endpoint) {
      if (endpoint != null && !endpoint.isBlank()) {
         String trimmed = endpoint.trim();

         for (AiSettings.Provider p : AiSettings.Provider.values()) {
            if (p.endpoint.equals(trimmed)) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public AiSettings withProvider(AiSettings.Provider next) {
      AiSettings draft = this.copy();
      draft.provider = next;
      if (shouldAutofillModel(draft.model)) {
         draft.model = defaultModel(next);
         draft.outputTokens = defaultOutputTokens(next);
         draft.endpoint = next.endpoint;
         draft.vision = defaultVision(next);
      } else if (shouldAutofillEndpoint(draft.endpoint)) {
         draft.endpoint = next.endpoint;
      }

      return draft;
   }

   public static String alternateDashScopeEndpoint(String endpoint) {
      try {
         return AiModels.load(AiSettings.Provider.DASHSCOPE, AiHttp.baseUrl(AiSettings.Provider.DASHSCOPE, endpoint));
      } catch (IllegalArgumentException e) {
         return null;
      }
   }

   public synchronized String key(String id) {
      String stored = this.sealedKeys.get(id);
      if (stored == null) {
         return externalKey(id);
      }

      byte[] clear;
      try {
         clear = autismclient.util.mm.a.DataUtil.write(Base64.getDecoder().decode(stored));
      } catch (RuntimeException e) {
         throw new IllegalStateException("Cannot decrypt " + id + " key on this OS account", e);
      }

      if (clear == null) {
         throw new IllegalStateException("Cannot decrypt " + id + " key on this OS account");
      }

      try {
         return new String(clear, StandardCharsets.UTF_8).trim();
      } finally {
         Arrays.fill(clear, (byte)0);
      }
   }

   public synchronized boolean hasKey(String id) {
      return this.sealedKeys.containsKey(id) || !externalKey(id).isEmpty();
   }

   synchronized String keyMarker(String id) {
      String stored = this.sealedKeys.get(id);
      if (stored != null) {
         return AiJson.sha256Hex(stored);
      }

      String external = externalKey(id);
      return external.isEmpty() ? "none" : AiJson.sha256Hex(external);
   }

   private static String externalKey(String id) {
      AiSettings.Provider provider;
      try {
         provider = AiSettings.Provider.valueOf(id);
      } catch (IllegalArgumentException e) {
         return "";
      }

      String key = System.getProperty("autism.ai.key");
      if (key == null || key.isBlank()) {
         key = System.getProperty("autism.ai." + provider.name().toLowerCase(Locale.ROOT) + ".key");
      }

      if (key == null || key.isBlank()) {
         key = System.getenv("AUTISM_AI_KEY");
      }

      if (key == null || key.isBlank()) {
         key = System.getenv(provider.name() + "_API_KEY");
      }

      return key != null && !key.isBlank() ? key.trim() : "";
   }

   public synchronized void setKey(String id, String value) {
      value = value == null ? "" : value.trim();
      if (value.isBlank()) {
         this.sealedKeys.remove(id);
      } else {
         byte[] clear = value.getBytes(StandardCharsets.UTF_8);

         try {
            byte[] sealed = autismclient.util.mm.a.DataUtil.load(clear);
            if (sealed == null) {
               throw new IllegalStateException("Protected credential storage unavailable");
            }

            this.sealedKeys.put(id, Base64.getEncoder().encodeToString(sealed));
         } finally {
            Arrays.fill(clear, (byte)0);
         }
      }
   }

   public boolean sameConnection(AiSettings other) {
      return other != null
         && this.builtIn == other.builtIn
         && this.provider == other.provider
         && Objects.equals(this.endpoint, other.endpoint)
         && Objects.equals(this.model, other.model)
         && this.keyMarker(this.provider.name()).equals(other.keyMarker(other.provider.name()));
   }

   public void validate() {
      AiHttp.baseUrl(this.provider, this.endpoint);
      if (this.model != null && !this.model.isBlank()) {
         if (this.model.chars().anyMatch(c -> c <= 32 || c == 127) || this.model.indexOf(63) >= 0 || this.model.indexOf(35) >= 0) {
            throw new IllegalArgumentException("Model ID contains invalid characters; choose a model from Scan models");
         } else if (this.contextTokens < 16000
            || this.outputTokens < 256
            || this.outputTokens > this.contextTokens / 2
            || this.contextTokens > 2000000
            || this.maxRequests < 1
            || this.maxTokens < 1000L
            || this.maxMinutes < 1
            || this.maxMinutes > 525600) {
            throw new IllegalArgumentException("Invalid AI limits");
         }
      } else {
         throw new IllegalArgumentException("Enter a model ID in AI settings");
      }
   }

   public synchronized void save(Path directory) throws IOException {
      AiJson.writeAtomicJson(directory.resolve("settings.json"), this);
   }

   public static AiSettings load(Path directory) throws IOException {
      Path path = directory.resolve("settings.json");
      AiSettings s;
      if (!Files.exists(path)) {
         s = new AiSettings();
         s.builtIn = true;
      } else {
         String text = Files.readString(path);
         s = AiJson.GSON.fromJson(text, AiSettings.class);
         if (s == null || s.version != 1 || s.provider == null || s.sealedKeys == null) {
            throw new IllegalArgumentException("Invalid AI settings file");
         }

         if (s.endpoint == null || s.endpoint.isBlank()) {
            s.endpoint = s.provider.endpoint;
         }

         if (s.model == null || s.model.isBlank()) {
            s.model = defaultModel(s.provider);
         }

         s.effort = normalizeEffort(s.effort);
         if (!text.contains("\"builtIn\"") && !s.hasKey(s.provider.name())) {
            s.builtIn = true;
         }
      }

      applyRuntimeOverrides(s);
      return s;
   }

   private static void applyRuntimeOverrides(AiSettings s) {
      String provider = System.getProperty("autism.ai.provider");
      if (provider != null && !provider.isBlank()) {
         try {
            s.provider = AiSettings.Provider.valueOf(provider.trim().toUpperCase(Locale.ROOT));
            s.endpoint = s.provider.endpoint;
            s.model = defaultModel(s.provider);
         } catch (IllegalArgumentException var4) {
         }
      }

      String model = System.getProperty("autism.ai.model");
      if (model != null && !model.isBlank()) {
         s.model = model.trim();
      }

      String effort = System.getProperty("autism.ai.effort");
      if (effort != null && EFFORTS.contains(effort.trim().toLowerCase(Locale.ROOT))) {
         s.effort = effort.trim().toLowerCase(Locale.ROOT);
      }
   }

   public synchronized AiSettings copy() {
      return AiJson.GSON.fromJson(AiJson.GSON.toJson(this), AiSettings.class);
   }

   public enum Provider {
      OPENAI("https://api.openai.com/v1"),
      DASHSCOPE("https://dashscope-intl.aliyuncs.com/compatible-mode/v1"),
      MOONSHOT("https://api.moonshot.ai/v1"),
      DEEPSEEK("https://api.deepseek.com/v1"),
      GEMINI("https://generativelanguage.googleapis.com/v1beta"),
      ANTHROPIC("https://api.anthropic.com/v1");

      public final String endpoint;

      Provider(String endpoint) {
         this.endpoint = endpoint;
      }
   }

   public enum Thinking {
      NONE,
      BUDGET,
      ADAPTIVE;
   }
}
