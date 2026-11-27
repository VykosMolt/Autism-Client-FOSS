package autismclient.ai;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class AiMiningQuota {
   private final Map<String, Integer> init;
   private final Map<String, Integer> load = new LinkedHashMap<>();

   private AiMiningQuota(Map<String, Integer> requested) {
      this.init = requested;
      requested.keySet().forEach(k -> this.load.put(k, 0));
   }

   public static JsonObject normalizeArgs(JsonObject args) {
      AiMiningTargets targets = AiMiningTargets.parse(AiJson.getString(args, "block", ""));
      int count = init(args.has("count") ? args.get("count") : new JsonPrimitive(1), "default");
      LinkedHashMap<String, Integer> overrides = new LinkedHashMap<>(targets.counts());
      if (args.has("counts")) {
         if (!args.get("counts").isJsonObject()) {
            throw new IllegalArgumentException("Mining counts must map block selectors to amounts");
         }

         for (Entry<String, JsonElement> e : args.getAsJsonObject("counts").entrySet()) {
            AiMiningTargets key = AiMiningTargets.parse(e.getKey());
            if (key.types().size() != 1 || !key.counts().isEmpty() || !targets.types().contains(key.spec())) {
               throw new IllegalArgumentException("Mining amount has no selected block: " + e.getKey());
            }

            overrides.put(key.spec(), init(e.getValue(), key.spec()));
         }
      }

      JsonObject counts = new JsonObject();

      for (String type : targets.types()) {
         counts.addProperty(type, overrides.getOrDefault(type, count));
      }

      JsonObject out = args.deepCopy();
      out.addProperty("block", targets.spec());
      out.addProperty("count", count);
      out.add("counts", counts);
      return out;
   }

   private static int init(JsonElement e, String type) {
      try {
         if (!e.isJsonPrimitive() || !e.getAsString().matches("[0-9]+")) {
            throw new NumberFormatException();
         }

         int n = Integer.parseInt(e.getAsString());
         if (n >= 1 && n <= 640) {
            return n;
         }
      } catch (RuntimeException var3) {
      }

      throw new IllegalArgumentException("Choose an amount from 1 to 640 for " + type);
   }

   public static AiMiningQuota fromArgs(JsonObject args) {
      if (!args.has("counts")) {
         return null;
      }

      JsonObject normalized = normalizeArgs(args);
      LinkedHashMap<String, Integer> counts = new LinkedHashMap<>();
      normalized.getAsJsonObject("counts").entrySet().forEach(e -> counts.put(e.getKey(), e.getValue().getAsInt()));
      return new AiMiningQuota(counts);
   }

   public boolean covers(String id) {
      return this.init_Str_Str(id) != null;
   }

   private String init_Str_Str(String id) {
      String best = null;
      int score = -1;

      for (Entry<String, Integer> e : this.init.entrySet()) {
         if (this.load.get(e.getKey()) < e.getValue() && AiMiningTargets.matches(e.getKey(), id)) {
            int rank = (e.getKey().contains(":") ? 10000 : 0) + e.getKey().length();
            if (rank > score) {
               best = e.getKey();
               score = rank;
            }
         }
      }

      return best;
   }

   public boolean load(String id) {
      String key = this.init_Str_Str(id);
      if (key == null) {
         return false;
      }

      this.load.put(key, this.load.get(key) + 1);
      return true;
   }

   public boolean isMet() {
      return this.init.entrySet().stream().allMatch(e -> this.load.get(e.getKey()) >= e.getValue());
   }

   public int totalCount() {
      return this.init.values().stream().mapToInt(Integer::intValue).sum();
   }

   public JsonObject brokenByBlock() {
      JsonObject out = new JsonObject();
      this.load.forEach(out::addProperty);
      return out;
   }

   public JsonObject load() {
      JsonObject out = new JsonObject();
      this.init.forEach((k, n) -> {
         if (n > this.load.get(k)) {
            out.addProperty(k, n - this.load.get(k));
         }
      });
      return out;
   }

   public static JsonObject init(JsonObject args, JsonObject data) {
      JsonObject normalized = normalizeArgs(args);
      JsonObject progress = data.has("brokenByBlock") && data.get("brokenByBlock").isJsonObject() ? data.getAsJsonObject("brokenByBlock") : new JsonObject();
      JsonObject left = new JsonObject();

      for (Entry<String, JsonElement> e : normalized.getAsJsonObject("counts").entrySet()) {
         int n = e.getValue().getAsInt();
         int done = Math.max(0, AiJson.getInt(progress, e.getKey(), 0));
         if (done < n) {
            left.addProperty(e.getKey(), n - done);
         }
      }

      if (left.isEmpty()) {
         throw new IllegalStateException("Mining quotas are already complete");
      }

      normalized.addProperty("block", String.join(",", left.keySet()));
      normalized.add("counts", left);
      return normalized;
   }
}
