package autismclient.ai;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.LongSupplier;

final class AiRetryGuard {
   static final long init_J = 300000L;
   private final Map<String, ArrayDeque<Long>> init_Map = new HashMap<>();
   private final Set<String> init_Set = new HashSet<>();
   private final LongSupplier init_LongSupplier;

   AiRetryGuard() {
      this(System::currentTimeMillis);
   }

   AiRetryGuard(LongSupplier clock) {
      this.init_LongSupplier = clock;
   }

   synchronized void init() {
      this.init_Map.clear();
      this.init_Set.clear();
   }

   synchronized void load() {
      this.init_Set.clear();
   }

   synchronized void init_V_Str(String key) {
      this.init_V_Str_Z(key, true);
   }

   synchronized void init_V_Str_Z(String key, boolean latchUncertain) {
      if (latchUncertain && this.init_Set.contains(key)) {
         throw new IllegalStateException("Previous dispatch was uncertain; observe to see whether it took effect, then retry only if it did not");
      }

      if (this.init_I_Str(key) >= 3) {
         throw new IllegalStateException("Three identical failures; choose another strategy or report the blocker");
      }
   }

   synchronized void init_V_Str_Str(String key, String status) {
      if (status.equals("UNCERTAIN")) {
         this.init_Set.add(key);
      }

      if (Set.of("FAILED", "TIMED_OUT").contains(status)) {
         this.init_Map.computeIfAbsent(key, k -> new ArrayDeque<>()).addLast(this.init_LongSupplier.getAsLong());
      }

      if (status.equals("SUCCEEDED")) {
         this.init_Map.remove(key);
      }
   }

   private int init_I_Str(String key) {
      ArrayDeque<Long> stamps = this.init_Map.get(key);
      if (stamps == null) {
         return 0;
      }

      long cutoff = this.init_LongSupplier.getAsLong() - 300000L;

      while (!stamps.isEmpty() && stamps.peekFirst() <= cutoff) {
         stamps.pollFirst();
      }

      if (stamps.isEmpty()) {
         this.init_Map.remove(key);
      }

      return stamps.size();
   }

   static boolean init_Z_JsonObject(JsonObject result) {
      return result != null && result.has("failure") && result.get("failure").isJsonObject()
         ? Set.of("THREAT_INTERRUPT", "HUNGER", "LOW_HEALTH", "ENVIRONMENTAL_HAZARD")
            .contains(AiJson.init_Str_JsonObject(result.getAsJsonObject("failure"), "code", ""))
         : false;
   }

   static String init_Str_Str_JsonObject(String tool, JsonObject args) {
      JsonObject copy = args.deepCopy();
      if (tool.equals("perform")) {
         copy.remove("fingerprint");
      }

      return tool + ":" + AiJson.init_Str_Str(init_Str_JsonElement(copy));
   }

   private static String init_Str_JsonElement(JsonElement value) {
      if (value.isJsonObject()) {
         ArrayList<String> parts = new ArrayList<>();

         for (String key : new TreeSet<>(value.getAsJsonObject().keySet())) {
            parts.add(AiJson.init.toJson(key) + ":" + init_Str_JsonElement(value.getAsJsonObject().get(key)));
         }

         return "{" + String.join(",", parts) + "}";
      } else {
         if (!value.isJsonArray()) {
            return value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()
               ? value.getAsBigDecimal().stripTrailingZeros().toPlainString()
               : value.toString();
         }

         ArrayList<String> parts = new ArrayList<>();

         for (JsonElement item : value.getAsJsonArray()) {
            parts.add(init_Str_JsonElement(item));
         }

         return "[" + String.join(",", parts) + "]";
      }
   }
}
