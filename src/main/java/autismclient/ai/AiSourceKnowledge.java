package autismclient.ai;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

final class AiSourceKnowledge {
   private static volatile Map<String, String> init_Map = Map.of();
   private static volatile Map<String, String> knownHashes = Map.of();
   private static volatile String init_Str = "unloaded";

   static void load() throws IOException {
      try (InputStream in = AiSourceKnowledge.class.getResourceAsStream("/assets/autismclient/ai/source-contracts.json")) {
         if (in == null) {
            throw new IOException("AI source contracts are missing from the full jar");
         }

         String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
         JsonObject root = JsonParser.parseString(json).getAsJsonObject();
         Map<String, String> compiled = new HashMap<>();
         Map<String, String> hashes = new HashMap<>();

         for (Entry<String, JsonElement> e : root.getAsJsonObject("sources").entrySet()) {
            JsonObject value = e.getValue().getAsJsonObject();
            compiled.put(value.get("className").getAsString(), value.get("notes").getAsString());
            hashes.put(value.get("className").getAsString(), value.get("sha256").getAsString());
         }

         init_Map = Map.copyOf(compiled);
         knownHashes = Map.copyOf(hashes);
         init_Str = AiJson.sha256Hex(json);
      }
   }

   static String featureDocs(Class<?> type) {
      String text = init_Map.getOrDefault(type.getName(), "");
      return text.isBlank() ? "" : "\nFeature-owned documentation: " + text;
   }

   static String docsFor(Class<?> type) {
      return knownHashes.getOrDefault(type.getName(), "");
   }

   static String overview() {
      return init_Str;
   }
}
