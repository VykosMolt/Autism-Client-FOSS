package autismclient.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.Map.Entry;

final class AiHistory {
   static final String INTERRUPTED_RESULT = "{\"status\":\"INTERRUPTED\",\"message\":\"The run stopped before this call's result was recorded; re-observe before assuming its effect\"}";
   static final String load = "CONTEXT COMPACTED by the host";
   static final String read = "EARLIER WORK (host digest): ";
   static final int init_I = 3000;

   private AiHistory() {
   }

   static String role(JsonObject m) {
      return AiJson.getString(m, "role", "");
   }

   static boolean isAssistant(JsonObject m) {
      String r = role(m);
      return r.equals("assistant") || r.equals("model");
   }

   static boolean isUser(JsonObject m) {
      return role(m).equals("user");
   }

   private static JsonArray init(JsonObject m, String key) {
      return m.has(key) && m.get(key).isJsonArray() ? m.getAsJsonArray(key) : null;
   }

   private static String init(JsonElement block) {
      return block.isJsonObject() ? AiJson.getString(block.getAsJsonObject(), "type", "") : "";
   }

   static JsonObject init_JsonObject(JsonObject message) {
      JsonArray content = init(message, "content");
      if (role(message).equals("assistant") && content != null) {
         JsonArray kept = new JsonArray();
         boolean removed = false;

         for (JsonElement block : content) {
            String t = init(block);
            if (!t.equals("thinking") && !t.equals("redacted_thinking")) {
               kept.add(block.deepCopy());
            } else {
               removed = true;
            }
         }

         if (!removed) {
            return message;
         }

         if (kept.isEmpty()) {
            kept.add(AiJson.objectOf("type", "text", "text", "(reasoning omitted)"));
         }

         JsonObject copy = message.deepCopy();
         copy.add("content", kept);
         return copy;
      } else {
         return message;
      }
   }

   static List<JsonObject> init_List(List<JsonObject> history) {
      List<JsonObject> out = new ArrayList<>(history.size());

      for (JsonObject m : history) {
         out.add(init_JsonObject(m));
      }

      return out;
   }

   static List<JsonObject> forProvider(AiSettings.Provider provider, List<JsonObject> stored) {
      List<JsonObject> out = new ArrayList<>(stored.size());

      for (JsonObject m : stored) {
         out.add(m.deepCopy());
      }

      while (!out.isEmpty() && !isUser(out.getFirst())) {
         out.removeFirst();
      }

      if (provider == AiSettings.Provider.ANTHROPIC) {
         out = init_List(out);
      }

      return load(provider, out);
   }

   static List<AiHistory.Inner_DataUtil> init(AiSettings.Provider provider, JsonObject assistant) {
      List<AiHistory.Inner_DataUtil> out = new ArrayList<>();
      if (!isAssistant(assistant)) {
         return out;
      }

      if (provider == AiSettings.Provider.ANTHROPIC) {
         JsonArray content = init(assistant, "content");
         if (content != null) {
            for (JsonElement block : content) {
               if (init(block).equals("tool_use")) {
                  JsonObject b = block.getAsJsonObject();
                  String name = AiJson.getString(b, "name", "");
                  out.add(
                     new AiHistory.Inner_DataUtil(
                        AiJson.getString(b, "id", ""),
                        name,
                        init(name, b.has("input") && b.get("input").isJsonObject() ? b.getAsJsonObject("input") : null)
                     )
                  );
               }
            }
         }
      } else if (provider == AiSettings.Provider.GEMINI) {
         JsonArray parts = init(assistant, "parts");
         if (parts != null) {
            for (JsonElement part : parts) {
               if (part.isJsonObject() && part.getAsJsonObject().has("functionCall")) {
                  JsonObject f = part.getAsJsonObject().getAsJsonObject("functionCall");
                  String name = AiJson.getString(f, "name", "");
                  out.add(
                     new AiHistory.Inner_DataUtil(
                        AiJson.getString(f, "id", ""), name, init(name, f.has("args") && f.get("args").isJsonObject() ? f.getAsJsonObject("args") : null)
                     )
                  );
               }
            }
         }
      } else {
         JsonArray calls = init(assistant, "tool_calls");
         if (calls != null) {
            for (JsonElement item : calls) {
               if (item.isJsonObject()) {
                  JsonObject c = item.getAsJsonObject();
                  JsonObject f = c.has("function") && c.get("function").isJsonObject() ? c.getAsJsonObject("function") : new JsonObject();
                  String name = AiJson.getString(f, "name", "");
                  JsonObject args = null;

                  try {
                     args = AiJson.parseObject(AiJson.getString(f, "arguments", "{}"));
                  } catch (RuntimeException var11) {
                  }

                  out.add(new AiHistory.Inner_DataUtil(AiJson.getString(c, "id", ""), name, init(name, args)));
               }
            }
         }
      }

      return out;
   }

   private static String init(String name, JsonObject args) {
      if (name.equals("perform") && args != null && args.has("id") && args.get("id").isJsonPrimitive()) {
         return "perform " + args.get("id").getAsString();
      } else {
         return name.isBlank() ? "(unnamed)" : name;
      }
   }

   static List<JsonObject> load(AiSettings.Provider provider, List<JsonObject> history) {
      List var10000;
      switch (provider) {
         case ANTHROPIC:
            var10000 = load(history);
            break;
         case GEMINI:
            var10000 = write(history);
            break;
         default:
            var10000 = read(history);
      }

      return var10000;
   }

   private static List<JsonObject> load(List<JsonObject> history) {
      List<JsonObject> out = new ArrayList<>();

      for (int i = 0; i < history.size(); i++) {
         JsonObject m = history.get(i);
         Set<String> expected = new LinkedHashSet<>();
         if (!out.isEmpty() && isAssistant(out.getLast())) {
            for (AiHistory.Inner_DataUtil c : init(AiSettings.Provider.ANTHROPIC, out.getLast())) {
               expected.add(c.id());
            }
         }

         if (!isUser(m)) {
            if (!expected.isEmpty()) {
               out.add(init(expected));
            }

            if (isAssistant(m)) {
               init_V(m);
            }

            out.add(m);
         } else {
            JsonArray content = init(m, "content");
            if (content != null) {
               JsonArray results = new JsonArray();
               JsonArray rest = new JsonArray();
               Set<String> answered = new HashSet<>();

               for (JsonElement block : content) {
                  if (init(block).equals("tool_result")) {
                     String id = AiJson.getString(block.getAsJsonObject(), "tool_use_id", "");
                     if (expected.contains(id) && answered.add(id)) {
                        results.add(block);
                     }
                  } else {
                     rest.add(block);
                  }
               }

               for (String id : expected) {
                  if (!answered.contains(id)) {
                     results.add(
                        AiJson.objectOf(
                           "type",
                           "tool_result",
                           "tool_use_id",
                           id,
                           "content",
                           "{\"status\":\"INTERRUPTED\",\"message\":\"The run stopped before this call's result was recorded; re-observe before assuming its effect\"}"
                        )
                     );
                  }
               }

               JsonArray merged = new JsonArray();
               merged.addAll(results);
               merged.addAll(rest);
               if (merged.isEmpty()) {
                  merged.add(AiJson.objectOf("type", "text", "text", "(earlier tool results omitted)"));
               }

               m.add("content", merged);
            } else if (!expected.isEmpty()) {
               JsonArray merged = new JsonArray();

               for (String id : expected) {
                  merged.add(
                     AiJson.objectOf(
                        "type",
                        "tool_result",
                        "tool_use_id",
                        id,
                        "content",
                        "{\"status\":\"INTERRUPTED\",\"message\":\"The run stopped before this call's result was recorded; re-observe before assuming its effect\"}"
                     )
                  );
               }

               String text = AiJson.getString(m, "content", "");
               if (!text.isEmpty()) {
                  merged.add(AiJson.objectOf("type", "text", "text", text));
               }

               m.add("content", merged);
            }

            out.add(m);
         }
      }

      if (!out.isEmpty() && isAssistant(out.getLast())) {
         Set<String> expected = new LinkedHashSet<>();

         for (AiHistory.Inner_DataUtil c : init(AiSettings.Provider.ANTHROPIC, out.getLast())) {
            expected.add(c.id());
         }

         if (!expected.isEmpty()) {
            out.add(init(expected));
         }
      }

      return out;
   }

   private static JsonObject init(Set<String> ids) {
      JsonArray content = new JsonArray();

      for (String id : ids) {
         content.add(
            AiJson.objectOf(
               "type",
               "tool_result",
               "tool_use_id",
               id,
               "content",
               "{\"status\":\"INTERRUPTED\",\"message\":\"The run stopped before this call's result was recorded; re-observe before assuming its effect\"}"
            )
         );
      }

      return AiJson.objectOf("role", "user", "content", content);
   }

   private static void init_V(JsonObject assistant) {
      JsonArray content = init(assistant, "content");
      if (content != null && content.size() >= 2) {
         JsonArray kept = new JsonArray();

         for (JsonElement block : content) {
            if (!init(block).equals("text") || !AiJson.getString(block.getAsJsonObject(), "text", "").isEmpty()) {
               kept.add(block);
            }
         }

         if (!kept.isEmpty() && kept.size() != content.size()) {
            assistant.add("content", kept);
         }
      }
   }

   private static List<JsonObject> read(List<JsonObject> history) {
      List<JsonObject> out = new ArrayList<>();
      Set<String> open = new LinkedHashSet<>();

      for (JsonObject m : history) {
         boolean tool = role(m).equals("tool");
         if (tool) {
            String id = AiJson.getString(m, "tool_call_id", "");
            if (open.remove(id)) {
               out.add(m);
            }
         } else {
            for (String id : open) {
               out.add(
                  AiJson.objectOf(
                     "role",
                     "tool",
                     "tool_call_id",
                     id,
                     "content",
                     "{\"status\":\"INTERRUPTED\",\"message\":\"The run stopped before this call's result was recorded; re-observe before assuming its effect\"}"
                  )
               );
            }

            open.clear();
            out.add(m);
            if (isAssistant(m)) {
               for (AiHistory.Inner_DataUtil c : init(AiSettings.Provider.OPENAI, m)) {
                  open.add(c.id());
               }
            }
         }
      }

      for (String id : open) {
         out.add(
            AiJson.objectOf(
               "role",
               "tool",
               "tool_call_id",
               id,
               "content",
               "{\"status\":\"INTERRUPTED\",\"message\":\"The run stopped before this call's result was recorded; re-observe before assuming its effect\"}"
            )
         );
      }

      return out;
   }

   private static List<JsonObject> write(List<JsonObject> history) {
      List<JsonObject> out = new ArrayList<>();

      for (int i = 0; i < history.size(); i++) {
         JsonObject m = history.get(i);
         List<AiHistory.Inner_DataUtil> expected = !out.isEmpty() && isAssistant(out.getLast()) ? init(AiSettings.Provider.GEMINI, out.getLast()) : List.of();
         if (!isUser(m)) {
            if (!expected.isEmpty()) {
               out.add(init_JsonObject(expected));
            }

            out.add(m);
         } else {
            JsonArray parts = init(m, "parts");
            if (parts == null) {
               parts = new JsonArray();
               m.add("parts", parts);
            }

            JsonArray responses = new JsonArray();
            JsonArray rest = new JsonArray();

            for (JsonElement part : parts) {
               boolean response = part.isJsonObject() && part.getAsJsonObject().has("functionResponse");
               if (response) {
                  if (responses.size() < expected.size()) {
                     responses.add(part);
                  }
               } else {
                  rest.add(part);
               }
            }

            for (int k = responses.size(); k < expected.size(); k++) {
               responses.add(init(expected.get(k)));
            }

            JsonArray merged = new JsonArray();
            merged.addAll(responses);
            merged.addAll(rest);
            if (merged.isEmpty()) {
               merged.add(AiJson.objectOf("text", "(earlier tool results omitted)"));
            }

            m.add("parts", merged);
            out.add(m);
         }
      }

      if (!out.isEmpty() && isAssistant(out.getLast())) {
         List<AiHistory.Inner_DataUtil> expected = init(AiSettings.Provider.GEMINI, out.getLast());
         if (!expected.isEmpty()) {
            out.add(init_JsonObject(expected));
         }
      }

      return out;
   }

   private static JsonObject init(AiHistory.Inner_DataUtil call) {
      JsonObject response = AiJson.objectOf(
         "name",
         call.name(),
         "response",
         AiJson.parseObject(
            "{\"status\":\"INTERRUPTED\",\"message\":\"The run stopped before this call's result was recorded; re-observe before assuming its effect\"}"
         )
      );
      if (!call.id().isBlank()) {
         response.addProperty("id", call.id());
      }

      return AiJson.objectOf("functionResponse", response);
   }

   private static JsonObject init_JsonObject(List<AiHistory.Inner_DataUtil> calls) {
      JsonArray parts = new JsonArray();

      for (AiHistory.Inner_DataUtil c : calls) {
         parts.add(init(c));
      }

      return AiJson.objectOf("role", "user", "parts", parts);
   }

   static AiHistory.Inner_StreamUtil init(AiSettings.Provider provider, List<JsonObject> history, long keepChars, long hardChars) {
      return init(provider, history, keepChars, hardChars, false);
   }

   static AiHistory.Inner_StreamUtil init(AiSettings.Provider provider, List<JsonObject> history, long keepChars, long hardChars, boolean keepLastThinking) {
      List<Integer> starts = new ArrayList<>();

      for (int i = 0; i < history.size(); i++) {
         if (isAssistant(history.get(i))) {
            starts.add(i);
         }
      }

      if (starts.isEmpty()) {
         return null;
      }

      long[] suffix = new long[history.size() + 1];

      for (int i = history.size() - 1; i >= 0; i--) {
         suffix[i] = suffix[i + 1] + AiContext.init(history.get(i));
      }

      int cut = -1;

      for (int start : starts) {
         if (start > 0 && suffix[start] <= keepChars) {
            cut = start;
            break;
         }
      }

      if (cut < 0) {
         cut = suffix[starts.getLast()] <= hardChars ? starts.getLast() : history.size();
      }

      if (cut <= 0) {
         return null;
      }

      List<JsonObject> kept = new ArrayList<>(history.subList(cut, history.size()));
      if (provider == AiSettings.Provider.ANTHROPIC) {
         int last = -1;
         if (keepLastThinking) {
            for (int i = kept.size() - 1; i >= 0; i--) {
               if (isAssistant(kept.get(i))) {
                  last = i;
                  break;
               }
            }
         }

         List<JsonObject> stripped = new ArrayList<>(kept.size());

         for (int i = 0; i < kept.size(); i++) {
            stripped.add(i == last ? kept.get(i) : init_JsonObject(kept.get(i)));
         }

         kept = stripped;
      }

      List<JsonObject> dropped = new ArrayList<>(history.subList(0, cut));
      String digest = init_Str(provider, dropped);
      String prior = init_Str(dropped);
      if (!prior.isBlank()) {
         digest = digest + "\nEARLIER COMPACTIONS: " + (prior.length() > 3000 ? prior.substring(0, 3000) + " ..." : prior);
      }

      return new AiHistory.Inner_StreamUtil(kept, dropped, digest);
   }

   static String init_Str(List<JsonObject> dropped) {
      for (int i = dropped.size() - 1; i >= 0; i--) {
         JsonObject m = dropped.get(i);
         if (isUser(m)) {
            String text = contentText(m);
            int at = text.indexOf("EARLIER WORK (host digest): ");
            if (text.startsWith("CONTEXT COMPACTED by the host") && at >= 0) {
               String digest = text.substring(at + "EARLIER WORK (host digest): ".length());
               int merged = digest.indexOf("\n\n");
               return (merged < 0 ? digest : digest.substring(0, merged)).trim();
            }
         }
      }

      return "";
   }

   private static String contentText(JsonObject m) {
      if (m.has("content") && m.get("content").isJsonPrimitive()) {
         return m.get("content").getAsString();
      }

      JsonArray blocks = init(m, "content");
      if (blocks != null) {
         for (JsonElement b : blocks) {
            if (init(b).equals("text")) {
               return AiJson.getString(b.getAsJsonObject(), "text", "");
            }
         }
      }

      JsonArray parts = init(m, "parts");
      if (parts != null) {
         for (JsonElement p : parts) {
            if (p.isJsonObject() && p.getAsJsonObject().has("text")) {
               return AiJson.getString(p.getAsJsonObject(), "text", "");
            }
         }
      }

      return "";
   }

   static String init_Str(AiSettings.Provider provider, List<JsonObject> dropped) {
      Map<String, String> labels = new HashMap<>();
      Map<String, Map<String, Integer>> byTool = new TreeMap<>();
      List<AiHistory.Inner_DataUtil> geminiPending = new ArrayList<>();
      int rounds = 0;
      int calls = 0;
      String lastText = "";

      for (JsonObject m : dropped) {
         if (isAssistant(m)) {
            rounds++;
            List<AiHistory.Inner_DataUtil> found = init(provider, m);
            calls += found.size();

            for (AiHistory.Inner_DataUtil c : found) {
               labels.put(c.id(), c.label());
               byTool.computeIfAbsent(c.label(), k -> new TreeMap<>());
            }

            if (provider == AiSettings.Provider.GEMINI) {
               geminiPending = new ArrayList<>(found);
            }

            String text = read(m);
            if (!text.isBlank()) {
               lastText = text;
            }
         } else if (provider == AiSettings.Provider.ANTHROPIC) {
            JsonArray content = init(m, "content");
            if (content != null) {
               for (JsonElement block : content) {
                  if (init(block).equals("tool_result")) {
                     JsonObject b = block.getAsJsonObject();
                     init(byTool, labels.get(AiJson.getString(b, "tool_use_id", "")), load(b.get("content")));
                  }
               }
            }
         } else if (provider == AiSettings.Provider.GEMINI) {
            JsonArray parts = init(m, "parts");
            int k = 0;
            if (parts != null) {
               for (JsonElement part : parts) {
                  if (part.isJsonObject() && part.getAsJsonObject().has("functionResponse")) {
                     JsonObject r = part.getAsJsonObject().getAsJsonObject("functionResponse");
                     String label = k < geminiPending.size() ? geminiPending.get(k).label() : AiJson.getString(r, "name", "");
                     init(byTool, label, load(r.get("response")));
                     k++;
                  }
               }
            }
         } else if (role(m).equals("tool")) {
            init(byTool, labels.get(AiJson.getString(m, "tool_call_id", "")), load(m.get("content")));
         }
      }

      StringBuilder out = new StringBuilder()
         .append(rounds)
         .append(rounds == 1 ? " round" : " rounds")
         .append(", ")
         .append(calls)
         .append(calls == 1 ? " tool call" : " tool calls");

      for (Entry<String, Map<String, Integer>> e : byTool.entrySet()) {
         int total = e.getValue().values().stream().mapToInt(Integer::intValue).sum();
         out.append("\n- ").append(e.getKey()).append(" x").append(total);
         if (!e.getValue().isEmpty()) {
            StringJoiner statuses = new StringJoiner(", ", " (", ")");
            e.getValue().forEach((status, n) -> statuses.add(status + " " + n));
            out.append(statuses);
         }

         if (out.length() > 4000) {
            out.append("\n- ...");
            break;
         }
      }

      if (!lastText.isBlank()) {
         String flat = lastText.replaceAll("\\s+", " ").trim();
         out.append("\nYour last note before the cut: ").append(flat.length() > 600 ? flat.substring(0, 600) + "..." : flat);
      }

      return out.toString();
   }

   private static void init(Map<String, Map<String, Integer>> byTool, String label, String status) {
      byTool.computeIfAbsent(label == null ? "(unknown call)" : label, k -> new TreeMap<>()).merge(status, 1, Integer::sum);
   }

   private static String load(JsonElement result) {
      try {
         JsonObject o = result == null
            ? null
            : (result.isJsonObject() ? result.getAsJsonObject() : (result.isJsonPrimitive() ? AiJson.parseObject(result.getAsString()) : null));
         if (o != null) {
            return AiJson.getString(o, "status", AiJson.getString(o, "outcome", "returned"));
         }
      } catch (RuntimeException var2) {
      }

      return "returned";
   }

   private static String read(JsonObject m) {
      if (m.has("content") && m.get("content").isJsonPrimitive()) {
         return m.get("content").getAsString();
      }

      StringBuilder text = new StringBuilder();
      JsonArray blocks = init(m, "content");
      if (blocks != null) {
         for (JsonElement b : blocks) {
            if (init(b).equals("text")) {
               text.append(AiJson.getString(b.getAsJsonObject(), "text", ""));
            }
         }
      }

      JsonArray parts = init(m, "parts");
      if (parts != null) {
         for (JsonElement p : parts) {
            if (p.isJsonObject() && p.getAsJsonObject().has("text") && !AiJson.getBoolean(p.getAsJsonObject(), "thought", false)) {
               text.append(AiJson.getString(p.getAsJsonObject(), "text", ""));
            }
         }
      }

      return text.toString();
   }

   static String init(String objective, List<String> updates, String taskState, String digest) {
      StringBuilder out = new StringBuilder(
         "CONTEXT COMPACTED by the host: older rounds of this conversation were removed to fit the context window. Continue the objective from here; the HOST STATE at the end of this turn is current."
      );
      out.append("\nOBJECTIVE: ").append(objective);
      if (updates != null && !updates.isEmpty()) {
         out.append("\nUSER UPDATES (oldest first):");

         for (String u : updates) {
            out.append("\n- ").append(u);
         }
      }

      out.append("\nTASK STATE: ").append(taskState != null && !taskState.isBlank() ? taskState : "(none recorded)");
      out.append("\n").append("EARLIER WORK (host digest): ").append(digest);
      return out.toString();
   }

   record Inner_DataUtil(String id, String name, String label) {
   }

   record Inner_StreamUtil(List<JsonObject> kept, List<JsonObject> dropped, String digest) {
   }
}
