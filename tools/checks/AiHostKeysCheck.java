package autismclient.ai;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

// A task state written by the model must not drop or replace what the host keeps in it.
public class AiHostKeysCheck {
   static void check(boolean ok, String what) {
      if (!ok) {
         System.out.println("FAIL AiHostKeysCheck: " + what);
         System.exit(1);
      }
   }

   static JsonObject json(String text) {
      return JsonParser.parseString(text).getAsJsonObject();
   }

   public static void main(String[] args) {
      JsonObject current = json(
         "{\"hostChatMode\":{\"active\":true},\"hostGetTargets\":{\"1:logs\":{\"count\":10}},\"builtInAction\":{\"k\":1},"
            + "\"activeOperation\":{\"id\":\"op-2\"},\"note\":\"old\"}"
      );
      JsonObject fromModel = json("{\"note\":\"new\",\"hostGetTargets\":{\"forged\":1},\"activeOperation\":{\"id\":\"op-1\"}}");
      JsonObject out = ag.auditKeepHostKeys(current, fromModel);
      check(out.get("note").getAsString().equals("new"), "the model's own keys are taken");
      check(out.has("hostChatMode") && out.has("builtInAction"), "host keys the model left out are kept");
      check(out.get("hostGetTargets").equals(current.get("hostGetTargets")), "a host key the model sent is not taken from the model");
      check(out.getAsJsonObject("activeOperation").get("id").getAsString().equals("op-2"), "a stale operation is not brought back");
      check(out.get("hostGetTargets") != current.get("hostGetTargets"), "kept values are copies");
      JsonObject none = ag.auditKeepHostKeys(new JsonObject(), json("{\"hostChatMode\":{\"active\":true},\"activeOperation\":{\"id\":\"x\"}}"));
      check(none.isEmpty(), "the model cannot create a host key");
      System.out.println("PASS AiHostKeysCheck");
   }
}
