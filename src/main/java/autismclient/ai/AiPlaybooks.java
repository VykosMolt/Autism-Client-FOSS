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
   private static volatile List<AiPlaybooks.Playbook> playbooks = List.of();
   private static volatile String bundledText;

   static void load() throws IOException {
      try (InputStream in = AiPlaybooks.class.getResourceAsStream("/assets/autismclient/ai/playbooks.json")) {
         if (in == null) {
            throw new IOException("AI playbooks are missing from the full jar");
         }

         JsonObject root = JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
         List<AiPlaybooks.Playbook> found = new ArrayList<>();

         for (JsonElement element : root.getAsJsonArray("playbooks")) {
            JsonObject p = element.getAsJsonObject();
            found.add(
               new AiPlaybooks.Playbook(
                  p.get("id").getAsString(),
                  p.get("title").getAsString(),
                  p.getAsJsonArray("keywords").asList().stream().map(JsonElement::getAsString).toList(),
                  p.getAsJsonArray("body").asList().stream().map(JsonElement::getAsString).toList()
               )
            );
         }

         playbooks = List.copyOf(found);
      }
   }

   static String strategyPlaybooks(String text, int maxCharacters) {
      if (text != null && !text.isBlank()) {
         String lower = " " + text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ") + " ";
         StringBuilder result = new StringBuilder();
         int used = 0;

         for (AiPlaybooks.Playbook p : playbooks) {
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

   static int count() {
      return playbooks.size();
   }

   static String addonTemplate(String text, int maxCharacters) {
      if (text != null && !text.isBlank()) {
         String lower = " " + text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ") + " ";
         if (!lower.contains(" addon") && !lower.contains("add on")) {
            return "";
         }

         if (!lower.matches("(?s).*\\b(create|make|write|build|develop|author|code|new)\\b.*")) {
            return "";
         }

         if (bundledText == null) {
            try (InputStream in = AiPlaybooks.class.getResourceAsStream("/assets/autismclient/ai/addon-template.md")) {
               bundledText = in == null ? "" : new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
               bundledText = "";
            }
         }

         if (bundledText.isBlank()) {
            return "";
         }

         String section = "\nADDON AUTHORING GUIDE (client-authoritative; follow it exactly when creating addons):\n" + bundledText;
         return section.length() > maxCharacters ? section.substring(0, maxCharacters) : section;
      } else {
         return "";
      }
   }

   private record Playbook(String id, String title, List<String> keywords, List<String> body) {
   }
}
