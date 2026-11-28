package autismclient.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.Builder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

final class AiModels {
   private static final int init = 2097152;
   private static final int load = 32;

   private AiModels() {
   }

   static AiModels.Inner_CachePool init_aw_CachePool(AiSettings.Provider provider, String endpoint, String key) {
      return fetchModels(provider, endpoint, key, () -> {});
   }

   static AiModels.Inner_CachePool fetchModels(AiSettings.Provider provider, String endpoint, String key, Runnable checkpoint) {
      return fetchModelsWith(provider, endpoint, key, (url, p, k) -> getJson(url, p, k, checkpoint));
   }

   static AiModels.Inner_CachePool fetchModelsWith(AiSettings.Provider provider, String endpoint, String key, AiModels.Inner_DataUtil fetch) {
      if (provider == null) {
         return new AiModels.Inner_CachePool(List.of(), "No provider selected");
      }

      if (key != null && !key.isBlank()) {
         try {
            String base = baseUrl(provider, endpoint);
            AiHttp.authHeaders(provider, key);
            Set<String> ids = new LinkedHashSet<>();
            switch (provider) {
               case GEMINI:
                  listTokenPagedModels(base, key.trim(), ids, fetch);
                  break;
               case ANTHROPIC:
                  listPagedModels(base, key.trim(), provider, ids, fetch, true);
                  break;
               case DASHSCOPE:
               case MOONSHOT:
                  List<String> ring = new ArrayList<>();
                  ring.add(base);
                  ring.addAll(init_List(provider, base, key));
                  AiModels.Inner_StreamUtil worth = null;
                  AiModels.Inner_StreamUtil last = null;

                  for (int i = 0; i < ring.size(); i++) {
                     try {
                        ids.clear();
                        if (provider == AiSettings.Provider.DASHSCOPE) {
                           listOpenRouterModels(ring.get(i), key.trim(), ids, fetch);
                        } else {
                           listPagedModels(ring.get(i), key.trim(), provider, ids, fetch, false);
                        }

                        base = ring.get(i);
                        last = null;
                        break;
                     } catch (AiModels.Inner_StreamUtil failure) {
                        boolean rejection = failure.status == 401 || failure.status == 403;
                        if (!rejection && worth == null) {
                           worth = failure;
                        }

                        if (i == ring.size() - 1) {
                           throw worth != null ? worth : failure;
                        }
                     }
                  }

                  if (provider == AiSettings.Provider.DASHSCOPE) {
                     ids.removeIf(id -> !init_Z(id));
                  }
                  break;
               default:
                  listPagedModels(base, key.trim(), provider, ids, fetch, false);
            }

            List<String> models = new ArrayList<>(ids);
            models.sort(AiModels::compareVersions);
            return new AiModels.Inner_CachePool(List.copyOf(models), "", base, provider);
         } catch (Exception e) {
            if (e instanceof InterruptedException) {
               Thread.currentThread().interrupt();
            }

            String reason = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            reason = reason.replace(key, "[redacted]")
               .replace(key.trim(), "[redacted]")
               .replaceAll("(?i)Bearer\\s+[^\\s\"']+", "Bearer [redacted]")
               .replaceAll("sk-[A-Za-z0-9_-]+", "[redacted]")
               .replace('\n', ' ')
               .replace('\r', ' ');
            return new AiModels.Inner_CachePool(List.of(), reason.length() > 200 ? reason.substring(0, 200) : reason);
         }
      } else {
         return new AiModels.Inner_CachePool(List.of(), "Add an API key to scan for models");
      }
   }

   static String baseUrl(AiSettings.Provider provider, String endpoint) {
      return AiHttp.baseUrl(provider, endpoint);
   }

   static List<String> init_List(AiSettings.Provider provider, String key) {
      String k = key == null ? "" : key.trim();
      List var10000;
      switch (provider) {
         case DASHSCOPE:
            List<String> ring = new ArrayList<>();
            if (k.startsWith("sk-sp-")) {
               ring.add("https://token-plan.ap-southeast-1.maas.aliyuncs.com/compatible-mode/v1");
               ring.add("https://coding-intl.dashscope.aliyuncs.com/v1");
            }

            ring.add(AiSettings.Provider.DASHSCOPE.endpoint);
            ring.add("https://dashscope.aliyuncs.com/compatible-mode/v1");
            ring.add("https://dashscope-us.aliyuncs.com/compatible-mode/v1");
            var10000 = List.copyOf(ring);
            break;
         case MOONSHOT:
            var10000 = List.of(
               AiSettings.Provider.MOONSHOT.endpoint, "https://api.moonshot.cn/v1", "https://api.kimi.ai/coding/v1", "https://api.kimi.com/coding/v1"
            );
            break;
         default:
            var10000 = List.of(provider.endpoint);
      }

      return var10000;
   }

   static List<String> init_List(AiSettings.Provider provider, String base, String key) {
      List<String> ring = init_List(provider, key);
      int at = ring.indexOf(base);
      if (at < 0) {
         return List.of();
      }

      List<String> others = new ArrayList<>();

      for (int i = 1; i < ring.size(); i++) {
         others.add(ring.get((at + i) % ring.size()));
      }

      return others;
   }

   static String load(AiSettings.Provider provider, String base) {
      List<String> others = init_List(provider, base, "");
      return others.isEmpty() ? null : others.getFirst();
   }

   private static void listPagedModels(String base, String key, AiSettings.Provider provider, Set<String> out, AiModels.Inner_DataUtil fetch, boolean anthropic) throws Exception {
      String after = "";
      Set<String> seen = new HashSet<>();

      for (int page = 0; page < 32; page++) {
         String query = anthropic ? "?limit=100" : "";
         if (!after.isEmpty()) {
            query = query + (query.isEmpty() ? "?" : "&") + (anthropic ? "after_id=" : "after=") + urlEncode(after);
         }

         JsonObject root = fetch.get(base + "/models" + query, provider, key);
         String last = "";

         for (JsonElement entry : arrayField(root, "data")) {
            if (entry.isJsonObject()) {
               String id = stringField(entry.getAsJsonObject(), "id");
               if (!id.isEmpty()) {
                  last = id;
                  if (load_Z(id)) {
                     out.add(id);
                  }
               }
            }
         }

         if (!booleanField(root, "has_more")) {
            return;
         }

         String cursor = stringField(root, "last_id");
         if (cursor.isEmpty()) {
            cursor = last;
         }

         if (cursor.isEmpty() || !seen.add(cursor)) {
            throw new IllegalStateException("Model list pagination did not advance");
         }

         after = cursor;
      }

      throw new IllegalStateException("Model list exceeded the pagination limit; narrow the endpoint catalog");
   }

   private static void listTokenPagedModels(String base, String key, Set<String> out, AiModels.Inner_DataUtil fetch) throws Exception {
      String token = "";
      Set<String> seen = new HashSet<>();

      for (int page = 0; page < 32; page++) {
         String url = base + "/models?pageSize=1000" + (token.isEmpty() ? "" : "&pageToken=" + urlEncode(token));
         JsonObject root = fetch.get(url, AiSettings.Provider.GEMINI, key);

         for (JsonElement entry : arrayField(root, "models")) {
            if (entry.isJsonObject()) {
               JsonObject model = entry.getAsJsonObject();
               String id = stringField(model, "name").replaceFirst("^models/", "");
               if (!id.isEmpty() && fieldEquals(model, "supportedGenerationMethods", "generateContent") && load_Z(id)) {
                  out.add(id);
               }
            }
         }

         token = stringField(root, "nextPageToken");
         if (token.isEmpty()) {
            return;
         }

         if (!seen.add(token)) {
            throw new IllegalStateException("Model list pagination did not advance");
         }
      }

      throw new IllegalStateException("Model list exceeded the pagination limit");
   }

   private static void listOpenRouterModels(String base, String key, Set<String> out, AiModels.Inner_DataUtil fetch) throws Exception {
      if (!base.endsWith("/api/v1")) {
         try {
            listPagedModels(base, key, AiSettings.Provider.DASHSCOPE, out, fetch, false);
            return;
         } catch (AiModels.Inner_StreamUtil failure) {
            if (failure.status != 404 && failure.status != 405 || !base.endsWith("/compatible-mode/v1")) {
               throw failure;
            }
         }

         out.clear();
         base = base.substring(0, base.length() - "/compatible-mode/v1".length()) + "/api/v1";
      }

      int received = 0;
      Set<String> pages = new HashSet<>();
      int page = 1;

      while (page <= 32) {
         JsonObject root = fetch.get(
            base + "/models?page_no=" + page + "&page_size=100&capabilities=TG&capabilities=Reasoning", AiSettings.Provider.DASHSCOPE, key
         );
         if (root.has("success") && !booleanField(root, "success")) {
            throw new IllegalStateException("Provider rejected the model list request");
         }

         JsonElement output = root.get("output");
         if (output != null && output.isJsonObject()) {
            JsonObject data = output.getAsJsonObject();
            JsonArray entries = arrayField(data, "models");
            if (!pages.add(entries.toString())) {
               throw new IllegalStateException("Model list pagination did not advance");
            }

            for (JsonElement entry : entries) {
               if (entry.isJsonObject()) {
                  JsonObject model = entry.getAsJsonObject();
                  String id = stringField(model, "model");
                  JsonElement metadata = model.get("inference_metadata");
                  boolean textOutput = metadata == null
                     || !metadata.isJsonObject()
                     || !metadata.getAsJsonObject().has("response_modality")
                     || fieldEquals(metadata.getAsJsonObject(), "response_modality", "Text");
                  if (!id.isEmpty() && textOutput && load_Z(id)) {
                     out.add(id);
                  }
               }
            }

            received += entries.size();
            if (data.has("total") && data.get("total").isJsonPrimitive()) {
               int total = data.get("total").getAsInt();
               if (received >= total) {
                  return;
               }

               if (entries.isEmpty()) {
                  throw new IllegalStateException("Model list pagination ended early");
               }

               page++;
               continue;
            }

            throw new IllegalStateException("Model list omitted its total");
         }

         throw new IllegalStateException("Unexpected model list response");
      }

      throw new IllegalStateException("Model list exceeded the pagination limit");
   }

   private static JsonObject getJson(String url, AiSettings.Provider provider, String key, Runnable checkpoint) throws InterruptedException, TimeoutException, ExecutionException {
      Builder request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(20L)).header("Accept", "application/json").GET();
      AiHttp.authHeaders(provider, key).forEach(request::header);
      HttpResponse<byte[]> response = AiHttp.send(request.build(), 2097152, checkpoint);
      String body = new String(response.body(), StandardCharsets.UTF_8);
      if (response.statusCode() != 200) {
         throw new AiModels.Inner_StreamUtil(response.statusCode(), AiHttp.errorMessage(provider, response.statusCode(), body, key));
      }

      JsonElement parsed;
      try {
         parsed = JsonParser.parseString(body);
      } catch (RuntimeException e) {
         throw new IllegalStateException("Unexpected model list response");
      }

      if (!parsed.isJsonObject()) {
         throw new IllegalStateException("Unexpected model list response");
      } else {
         return parsed.getAsJsonObject();
      }
   }

   private static JsonArray arrayField(JsonObject root, String field) {
      if (root.has(field) && root.get(field).isJsonArray()) {
         return root.getAsJsonArray(field);
      } else {
         throw new IllegalStateException("Unexpected model list response");
      }
   }

   private static String stringField(JsonObject object, String field) {
      JsonElement value = object.get(field);
      if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
         String text = value.getAsString().trim();
         return text.length() <= 512 && text.chars().noneMatch(Character::isISOControl) ? text : "";
      } else {
         return "";
      }
   }

   private static boolean booleanField(JsonObject object, String field) {
      JsonElement value = object.get(field);
      return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean() && value.getAsBoolean();
   }

   private static boolean fieldEquals(JsonObject object, String field, String wanted) {
      JsonElement value = object.get(field);
      if (value != null && value.isJsonArray()) {
         for (JsonElement item : value.getAsJsonArray()) {
            if (item.isJsonPrimitive() && item.getAsJsonPrimitive().isString() && wanted.equals(item.getAsString())) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static String urlEncode(String value) {
      return URLEncoder.encode(value, StandardCharsets.UTF_8);
   }

   private static String load_Str(String body) {
      try {
         JsonElement parsed = JsonParser.parseString(body);
         if (parsed.isJsonObject()) {
            JsonObject root = parsed.getAsJsonObject();
            JsonElement error = root.get("error");
            String text = error != null && error.isJsonObject() ? stringField(error.getAsJsonObject(), "message") : stringField(root, "message");
            if (!text.isBlank()) {
               return text;
            }
         }
      } catch (RuntimeException var5) {
      }

      return "Model discovery failed; check the API key and endpoint";
   }

   private static boolean load_Z(String id) {
      String lower = id.toLowerCase(Locale.ROOT);
      return !lower.contains("embedding")
         && !lower.contains("embed-")
         && !lower.contains("rerank")
         && !lower.contains("whisper")
         && !lower.contains("transcri")
         && !lower.contains("moderation")
         && !lower.startsWith("tts-")
         && !lower.contains("-tts")
         && !lower.startsWith("dall-e")
         && !lower.startsWith("imagen")
         && !lower.startsWith("veo-")
         && !lower.startsWith("lyria")
         && !lower.startsWith("qwen-image")
         && !lower.startsWith("gpt-image")
         && !lower.contains("realtime");
   }

   static boolean init_Z(String id) {
      String family = id.toLowerCase(Locale.ROOT);
      family = family.substring(family.indexOf(47) + 1);
      if (!family.startsWith("qwen") && !family.startsWith("kimi")) {
         return false;
      }

      for (String excluded : List.of("audio", "asr", "ocr", "-mt-", "livetranslate", "omni")) {
         if (family.contains(excluded)) {
            return false;
         }
      }

      return true;
   }

   private static int compareVersions(String a, String b) {
      String[] pa = a.toLowerCase(Locale.ROOT).split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
      String[] pb = b.toLowerCase(Locale.ROOT).split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
      int n = Math.min(pa.length, pb.length);

      for (int i = 0; i < n; i++) {
         boolean da = !pa[i].isEmpty() && Character.isDigit(pa[i].charAt(0));
         boolean db = !pb[i].isEmpty() && Character.isDigit(pb[i].charAt(0));
         int cmp;
         if (da && db) {
            cmp = Long.compare(init_J(pb[i]), init_J(pa[i]));
         } else {
            cmp = pb[i].compareTo(pa[i]);
         }

         if (cmp != 0) {
            return cmp;
         }
      }

      return Integer.compare(pb.length, pa.length);
   }

   private static long init_J(String digits) {
      try {
         return Long.parseLong(digits.length() > 15 ? digits.substring(0, 15) : digits);
      } catch (NumberFormatException e) {
         return 0L;
      }
   }

   record Inner_CachePool(List<String> models, String error, String endpoint, AiSettings.Provider provider) {
      Inner_CachePool(List<String> models, String error) {
         this(models, error, "", null);
      }

      Inner_CachePool(List<String> models, String error, String endpoint) {
         this(models, error, endpoint, null);
      }

      boolean ok() {
         return this.error == null || this.error.isEmpty();
      }
   }

   @FunctionalInterface
   interface Inner_DataUtil {
      JsonObject get(String var1, AiSettings.Provider var2, String var3) throws Exception;
   }

   static final class Inner_StreamUtil extends IllegalStateException {
      final int status;

      Inner_StreamUtil(int status, String message) {
         super(message);
         this.status = status;
      }
   }
}
