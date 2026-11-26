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

final class ListPool {
   private static final int init = 12;

   private ListPool() {
   }

   static AiOperation init(String name, JsonObject args) {
      Command command = autismclient.commands.CommandManager.init_Command(name);
      if (command == null) {
         throw new IllegalArgumentException("Client command '" + name + "' is not registered");
      }

      String prefix = autismclient.commands.CommandManager.init_Str();
      String line = init(init(args), name, Arrays.asList(command.aliases()), prefix, word -> {
         Command other = autismclient.commands.CommandManager.init_Command(word);
         return other != null && other != command ? autismclient.commands.CommandManager.init_Str(other) : null;
      });
      List<String> raw = new ArrayList<>();

      autismclient.commands.CommandManager.Inner_DataUtil dispatch;
      try (AutismClientMessaging.Inner_DataUtil ignored = AutismClientMessaging.capture(raw)) {
         dispatch = autismclient.commands.CommandManager.init_DataUtil_DataUtil(line);
      }

      List<String> forms = autismclient.commands.CommandManager.init_List(command);
      String typed = prefix + line;
      JsonObject data = ag.init_JsonObject_ObjArr(
         "command", name, "body", line, "typed", typed, "dispatch", dispatch.init_DataUtil_StreamUti().name(), "replies", init(raw), "forms", forms
      );
      AiOperation var10000;
      switch (dispatch.init_DataUtil_StreamUti()) {
         case EXECUTED:
            String refusal = init(raw, "error");
            if (refusal != null) {
               var10000 = R.init(AiOperation.Status.FAILED, A.flush, "Client command " + typed + " refused: " + refusal, data);
            } else {
               String said = raw.isEmpty()
                  ? "Client command " + typed + " executed" + (dispatch.init_Str().isEmpty() ? "" : " (" + dispatch.init_Str() + ")")
                  : "Client command " + typed + " executed: " + load(raw.get(0));
               var10000 = R.init_AiOperation(AiOperation.Status.SUCCEEDED, said, data);
            }
            break;
         case SYNTAX_ERROR:
         case UNKNOWN_COMMAND:
            var10000 = R.init(
               AiOperation.Status.FAILED,
               A.flush,
               "Client command " + typed + " did not parse: " + dispatch.init_Str() + ". Accepted forms: " + String.join(" | ", forms),
               data
            );
            break;
         case BLOCKED:
            var10000 = R.init(AiOperation.Status.FAILED, A.data, dispatch.init_Str(), data);
            break;
         case ERROR:
            var10000 = R.init(AiOperation.Status.FAILED, A.flush, "Client command " + typed + " threw: " + dispatch.init_Str(), data);
            break;
         case EMPTY:
            var10000 = R.init(AiOperation.Status.FAILED, A.flush, "Empty command line", data);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   static String init(JsonObject args) {
      for (String key : List.of("body", "line", "text", "args", "arguments", "command")) {
         if (args.has(key) && args.get(key).isJsonPrimitive()) {
            return args.get(key).getAsString();
         }
      }

      return "";
   }

   static String init(String body, String name, Collection<String> aliases, String prefix, Function<String, String> otherCommand) {
      String line = body == null ? "" : body.trim();
      if (!prefix.isEmpty() && line.startsWith(prefix)) {
         line = line.substring(prefix.length()).trim();
      } else if (line.length() > 1 && (line.charAt(0) == '.' || line.charAt(0) == '/') && init(read(line.substring(1)), name, aliases)) {
         line = line.substring(1).trim();
      }

      if (line.isEmpty()) {
         return name;
      } else {
         String first = read(line);
         String rest = line.substring(first.length()).trim();
         if (init(first, name, aliases)) {
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

   static JsonArray init(List<String> raw) {
      JsonArray out = new JsonArray();

      for (String line : raw) {
         if (out.size() >= 12) {
            break;
         }

         String text = load(line);
         if (!text.isEmpty()) {
            out.add(ag.init_JsonObject_ObjArr("text", text, "tone", init(line)));
         }
      }

      return out;
   }

   static String init(String raw) {
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

   static String load(String raw) {
      return raw == null ? "" : raw.replaceAll("§.", "").trim();
   }

   private static String init(List<String> raw, String tone) {
      for (String line : raw) {
         if (init(line).equals(tone)) {
            return load(line);
         }
      }

      return null;
   }

   private static boolean init(String word, String name, Collection<String> aliases) {
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

   private static String read(String line) {
      int space = line.indexOf(32);
      return space < 0 ? line : line.substring(0, space);
   }
}
