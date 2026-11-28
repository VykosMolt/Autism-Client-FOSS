package autismclient.ai;

import autismclient.api.ai.AiOperation;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

final class AiClientCommand {
   private static final int init = 12;

   private AiClientCommand() {
   }

   static AiOperation run(String name, JsonObject args) {
      Command command = autismclient.commands.CommandManager.getCommand(name);
      if (command == null) {
         throw new IllegalArgumentException("Client command '" + name + "' is not registered");
      }

      String prefix = autismclient.commands.CommandManager.getPrefix();
      String line = normalizeCommandLine(bodyOf(args), name, Arrays.asList(command.aliases()), prefix, word -> {
         Command other = autismclient.commands.CommandManager.getCommand(word);
         return other != null && other != command ? autismclient.commands.CommandManager.getPrimaryName(other) : null;
      });
      List<String> raw = new ArrayList<>();

      autismclient.commands.CommandManager.DispatchResult dispatch;
      try (AutismClientMessaging.CaptureHandle ignored = AutismClientMessaging.capture(raw)) {
         dispatch = autismclient.commands.CommandManager.dispatch(line);
      }

      List<String> forms = autismclient.commands.CommandManager.getUsageForms(command);
      String typed = prefix + line;
      JsonObject data = AiJson.objectOf(
         "command", name, "body", line, "typed", typed, "dispatch", dispatch.status().name(), "replies", toneLines(raw), "forms", forms
      );
      AiOperation var10000;
      switch (dispatch.status()) {
         case EXECUTED:
            String refusal = firstWithTone(raw, "error");
            if (refusal != null) {
               var10000 = AiGameOperations.immediateFailure(AiOperation.Status.FAILED, AiFailure.EXECUTION_FAILED, "Client command " + typed + " refused: " + refusal, data);
            } else {
               String said = raw.isEmpty()
                  ? "Client command " + typed + " executed" + (dispatch.message().isEmpty() ? "" : " (" + dispatch.message() + ")")
                  : "Client command " + typed + " executed: " + stripCodes(raw.get(0));
               var10000 = AiGameOperations.immediate(AiOperation.Status.SUCCEEDED, said, data);
            }
            break;
         case SYNTAX_ERROR:
         case UNKNOWN_COMMAND:
            var10000 = AiGameOperations.immediateFailure(
               AiOperation.Status.FAILED,
               AiFailure.EXECUTION_FAILED,
               "Client command " + typed + " did not parse: " + dispatch.message() + ". Accepted forms: " + String.join(" | ", forms),
               data
            );
            break;
         case BLOCKED:
            var10000 = AiGameOperations.immediateFailure(AiOperation.Status.FAILED, AiFailure.IMPOSSIBLE_STATE, dispatch.message(), data);
            break;
         case ERROR:
            var10000 = AiGameOperations.immediateFailure(AiOperation.Status.FAILED, AiFailure.EXECUTION_FAILED, "Client command " + typed + " threw: " + dispatch.message(), data);
            break;
         case EMPTY:
            var10000 = AiGameOperations.immediateFailure(AiOperation.Status.FAILED, AiFailure.EXECUTION_FAILED, "Empty command line", data);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   static String bodyOf(JsonObject args) {
      for (String key : List.of("body", "line", "text", "args", "arguments", "command")) {
         if (args.has(key) && args.get(key).isJsonPrimitive()) {
            return args.get(key).getAsString();
         }
      }

      return "";
   }

   static String normalizeCommandLine(String body, String name, Collection<String> aliases, String prefix, Function<String, String> otherCommand) {
      String line = body == null ? "" : body.trim();
      if (!prefix.isEmpty() && line.startsWith(prefix)) {
         line = line.substring(prefix.length()).trim();
      } else if (line.length() > 1 && (line.charAt(0) == '.' || line.charAt(0) == '/') && matchesName(firstWord(line.substring(1)), name, aliases)) {
         line = line.substring(1).trim();
      }

      if (line.isEmpty()) {
         return name;
      } else {
         String first = firstWord(line);
         String rest = line.substring(first.length()).trim();
         if (matchesName(first, name, aliases)) {
            return rest.isEmpty() ? name : name + " " + rest;
         } else {
            String other = otherCommand.apply(first);
            if (other != null) {
               throw new IllegalArgumentException(
                  "'" + first + "' is a different client command: perform command:" + other + " for it, or give command:" + name + " one of its own forms"
               );
            } else {
               return name + " " + line;
            }
         }
      }
   }

   static JsonArray toneLines(List<String> raw) {
      JsonArray out = new JsonArray();

      for (String line : raw) {
         if (out.size() >= 12) {
            break;
         }

         String text = stripCodes(line);
         if (!text.isEmpty()) {
            out.add(AiJson.objectOf("text", text, "tone", toneOf(line)));
         }
      }

      return out;
   }

   static String toneOf(String raw) {
      String line = raw == null ? "" : raw.stripLeading();
      if (line.length() >= 2 && line.charAt(0) == 167) {
         switch (Character.toLowerCase(line.charAt(1))) {
            case '2':
            case 'a':
               return "success";
            case '4':
            case 'c':
               return "error";
            case '6':
            case 'e':
               return "warning";
         }
      }

      return "info";
   }

   static String stripCodes(String raw) {
      return raw == null ? "" : raw.replaceAll("§.", "").trim();
   }

   private static String firstWithTone(List<String> raw, String tone) {
      for (String line : raw) {
         if (toneOf(line).equals(tone)) {
            return stripCodes(line);
         }
      }

      return null;
   }

   private static boolean matchesName(String word, String name, Collection<String> aliases) {
      if (word.equalsIgnoreCase(name)) {
         return true;
      }

      for (String alias : aliases) {
         if (alias != null && alias.equalsIgnoreCase(word)) {
            return true;
         }
      }

      return false;
   }

   private static String firstWord(String line) {
      int space = line.indexOf(32);
      return space < 0 ? line : line.substring(0, space);
   }
}
