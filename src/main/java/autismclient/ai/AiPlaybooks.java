package autismclient.ai;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class AiPlaybooks {
   private static volatile List<AiPlaybooks.Inner_DataUtil> init_List = List.of();
   private static volatile String init_Str;

   static void load() throws IOException {
      try (InputStream in = AiPlaybooks.class.getResourceAsStream("/assets/autismclient/ai/playbooks.json")) {
         if (in == null) {
            throw new IOException("AI playbooks are missing from the full jar");
         }

         JsonObject root = JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
         List<AiPlaybooks.Inner_DataUtil> found = new ArrayList<>();

         for (JsonElement element : root.getAsJsonArray("playbooks")) {
            JsonObject p = element.getAsJsonObject();
            found.add(
               new AiPlaybooks.Inner_DataUtil(
                  p.get("id").getAsString(),
                  p.get("title").getAsString(),
                  p.getAsJsonArray("keywords").asList().stream().map(JsonElement::getAsString).toList(),
                  p.getAsJsonArray("body").asList().stream().map(JsonElement::getAsString).toList()
               )
            );
         }

         init_List = List.copyOf(found);
      }
   }

   static String init(String text, int maxCharacters) {
      if (text != null && !text.isBlank()) {
         String lower = " " + text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ") + " ";
         StringBuilder result = new StringBuilder();
         int used = 0;

         for (AiPlaybooks.Inner_DataUtil p : init_List) {
            boolean hit = false;

            for (String keyword : p.keywords()) {
               if (lower.contains(" " + keyword.toLowerCase(Locale.ROOT) + " ")) {
                  hit = true;
                  break;
               }
            }

            if (hit) {
               StringBuilder section = new StringBuilder("\nSTRATEGY PLAYBOOK (" + p.title() + ", client-authoritative):");

               for (String line : p.body()) {
                  section.append("\n- ").append(line);
               }

               if (result.length() + section.length() <= maxCharacters) {
                  result.append(section);
                  if (++used >= 2) {
                     break;
                  }
               }
            }
         }

         return result.toString();
      } else {
         return "";
      }
   }

   static int init_I() {
      return init_List.size();
   }

   static String load(String text, int maxCharacters) {
      if (text != null && !text.isBlank()) {
         String lower = " " + text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ") + " ";
         if (!lower.contains(" addon") && !lower.contains("add on")) {
            return "";
         }

         if (!lower.matches("(?s).*\\b(create|make|write|build|develop|author|code|new)\\b.*")) {
            return "";
         }

         if (init_Str == null) {
            try (InputStream in = AiPlaybooks.class.getResourceAsStream("/assets/autismclient/ai/addon-template.md")) {
               init_Str = in == null ? "" : new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
               init_Str = "";
            }
         }

         if (init_Str.isBlank()) {
            return "";
         }

         String section = "\nADDON AUTHORING GUIDE (client-authoritative; follow it exactly when creating addons):\n" + init_Str;
         return section.length() > maxCharacters ? section.substring(0, maxCharacters) : section;
      } else {
         return "";
      }
   }

   private record Inner_DataUtil(String id, String title, List<String> keywords, List<String> body) {
   }
}
