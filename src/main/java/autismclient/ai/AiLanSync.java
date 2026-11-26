package autismclient.ai;

import autismclient.util.AutismConfig;
import autismclient.util.MacroNameNormalizer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;

public final class AiLanSync {
   static final int init_I = 256;
   static final int load = 100;
   static final List<String> init_List = List.of(
      "start",
      "stop",
      "createSession",
      "joinSession",
      "leaveSession",
      "startSearching",
      "cancelSearch",
      "executeMacroSynchronized",
      "executeMacrosSynchronized",
      "broadcastStopAll",
      "shareMacro",
      "importMacro",
      "sendChat",
      "sharePreset",
      "setDelayOffset",
      "setExecutionMethod"
   );

   private AiLanSync() {
   }

   static void init(String action, JsonObject args) {
      if (action == null || action.isBlank()) {
         throw new IllegalArgumentException("action is required; one of: " + String.join(", ", init_List));
      }

      if (!init_List.contains(action)) {
         throw new IllegalArgumentException("Unknown LAN action '" + action + "'; valid actions: " + String.join(", ", init_List));
      }

      switch (action) {
         case "joinSession":
            init(args, "sessionId");
            break;
         case "executeMacroSynchronized":
            init(args, "macro");
            break;
         case "executeMacrosSynchronized":
            if (!args.has("assignments") || !args.get("assignments").isJsonObject() || args.getAsJsonObject("assignments").size() == 0) {
               throw new IllegalArgumentException("executeMacrosSynchronized requires a non-empty assignments object mapping peer name to macro name");
            }

            for (Entry<String, JsonElement> entry : args.getAsJsonObject("assignments").entrySet()) {
               if (entry.getKey().isBlank() || !entry.getValue().isJsonPrimitive() || entry.getValue().getAsString().isBlank()) {
                  throw new IllegalArgumentException("assignments must map each peer name to a non-blank macro name");
               }
            }
            break;
         case "shareMacro":
            init(args, "name");
            break;
         case "sharePreset":
            init(args, "name");
            break;
         case "importMacro":
            init(args, "peer");
            init(args, "name");
            break;
         case "setDelayOffset":
            init(args, "peer");
            if (!args.has("offsetMs")) {
               throw new IllegalArgumentException("offsetMs is required for setDelayOffset");
            }
            break;
         case "setExecutionMethod":
            String method = init(args, "method").toUpperCase(Locale.ROOT);
            if (!method.equals("INSTANT") && !method.equals("TICK")) {
               throw new IllegalArgumentException("method must be INSTANT or TICK");
            }
            break;
         case "sendChat":
            String text = init(args, "text");
            if (text.length() > 256) {
               throw new IllegalArgumentException("text must be at most 256 characters");
            }
      }
   }

   private static String init(JsonObject args, String key) {
      String value = AiJson.init_Str_JsonObject(args, key, "");
      if (value.isBlank()) {
         throw new IllegalArgumentException(key + " is required for this LAN action");
      } else {
         return value.trim();
      }
   }

   public static JsonObject init(JsonObject args) {
      autismclient.util.LanSyncManager lan = autismclient.util.LanSyncManager.init_aE();
      JsonObject out = new JsonObject();
      out.addProperty("running", lan.init_Z());
      out.addProperty("inSession", lan.write_Z());
      out.addProperty("host", lan.update_Z());
      if (lan.init_Str() != null && !lan.init_Str().isBlank()) {
         out.addProperty("sessionId", lan.init_Str());
      }

      out.addProperty("syncState", lan.init_aE_JsonUtil().name());
      out.addProperty("searching", lan.sync_Z());
      if (lan.sync_Z()) {
         out.addProperty("searchSecondsRemaining", lan.read_I());
      }

      String self = lan.load_Str();
      JsonArray clients = new JsonArray();

      for (Entry<String, autismclient.util.LanSyncManager.Inner_DataUtil> entry : lan.init_Map().entrySet()) {
         autismclient.util.LanSyncManager.Inner_DataUtil info = entry.getValue();
         clients.add(
            AiJson.init_JsonObject_ObjArr(
               "name",
               info.init_Str,
               "host",
               info.init_Z,
               "self",
               entry.getKey().equals(self),
               "status",
               lan.init_aE_CachePool_Str(entry.getKey()).name(),
               "delayOffsetMs",
               info.init_I
            )
         );
      }

      out.add("clients", clients);
      out.addProperty("connectedCount", lan.load_I());
      Map<String, String> assignments = lan.update_Map();
      if (!assignments.isEmpty()) {
         out.add("assignments", AiJson.init.toJsonTree(assignments));
         if (!lan.read_Str().isBlank()) {
            out.addProperty("assignmentSetBy", lan.read_Str());
         }
      }

      List<String> remoteMacros = lan.load_List();
      out.add("remoteMacros", AiJson.init.toJsonTree(remoteMacros.size() > 100 ? remoteMacros.subList(0, 100) : remoteMacros));
      if (remoteMacros.size() > 100) {
         out.addProperty("remoteMacrosTruncated", remoteMacros.size() - 100);
      }

      out.addProperty("lanSyncEnabled", AutismConfig.getGlobal().lanSyncEnabled);
      out.addProperty("executionMethod", lan.init_aE_StreamUtil().name());
      return out;
   }

   public static JsonObject load(JsonObject args) {
      String action = AiJson.init_Str_JsonObject(args, "action", "");
      init(action, args);
      autismclient.util.LanSyncManager lan = autismclient.util.LanSyncManager.init_aE();
      switch (action) {
         case "start":
            if (autismclient.modules.aD.read_Z()) {
               throw new IllegalStateException("Client is hidden (panic mode); LAN Sync cannot start");
            }

            if (lan.init_Z()) {
               throw new IllegalArgumentException("LAN Sync is already running");
            }

            init(true);
            lan.init_V();
            break;
         case "stop":
            if (!lan.init_Z()) {
               throw new IllegalArgumentException("LAN Sync is not running");
            }

            init(false);
            lan.load_V();
            break;
         case "createSession":
            init(lan);
            lan.write_V();
            break;
         case "joinSession":
            init(lan);
            String sessionId = init(args, "sessionId");
            if (sessionId.equals(lan.init_Str())) {
               throw new IllegalArgumentException("Already in session " + sessionId);
            }

            String hostIp = AiJson.init_Str_JsonObject(args, "hostIp", "");
            if (hostIp.isBlank()) {
               lan.init_V_Str(sessionId);
            } else {
               lan.init_V_Str_Str(sessionId, hostIp);
            }
            break;
         case "leaveSession":
            if (!lan.write_Z()) {
               throw new IllegalArgumentException("Not in a LAN Sync session");
            }

            lan.update_V();
            break;
         case "startSearching":
            init(lan);
            if (lan.write_Z()) {
               throw new IllegalArgumentException("Already in a session; leave it before searching");
            }

            lan.close_V();
            break;
         case "cancelSearch":
            if (!lan.sync_Z()) {
               throw new IllegalArgumentException("No session search in progress");
            }

            lan.open_V();
            break;
         case "executeMacroSynchronized":
            load(lan);
            String macro = init(args, "macro");
            if (autismclient.util.MacroLibraryManager.init_aN().init_aM_Str(macro) == null) {
               throw new IllegalArgumentException("Unknown macro '" + macro + "'; inspect client:macros for saved names");
            }

            lan.load_V(macro);
            break;
         case "executeMacrosSynchronized":
            load(lan);
            Map<String, String> assignments = new LinkedHashMap<>();

            for (Entry<String, JsonElement> entry : args.getAsJsonObject("assignments").entrySet()) {
               assignments.put(entry.getKey(), entry.getValue().getAsString().trim());
            }

            init(lan, assignments);
            lan.init_V_Map(assignments);
            break;
         case "broadcastStopAll":
            if (!lan.write_Z()) {
               throw new IllegalArgumentException("Not in a LAN Sync session");
            }

            lan.run();
            break;
         case "shareMacro":
            if (!lan.write_Z()) {
               throw new IllegalArgumentException("Not in a LAN Sync session");
            }

            String name = init(args, "name");
            if (autismclient.util.MacroLibraryManager.init_aN().init_aM_Str(name) == null) {
               throw new IllegalArgumentException("Unknown macro '" + name + "'; inspect client:macros for saved names");
            }

            if (!lan.load_Z(name)) {
               throw new IllegalStateException("Macro share was rejected by LAN Sync");
            }
            break;
         case "importMacro":
            if (!lan.write_Z()) {
               throw new IllegalArgumentException("Not in a LAN Sync session");
            }

            String peer = init(args, "peer");
            String macroName = init(args, "name");
            if (!lan.init_Map().containsKey(peer)) {
               throw new IllegalArgumentException("Unknown peer '" + peer + "'; check clients in the LAN status");
            }

            if (!init(lan, peer, macroName)) {
               throw new IllegalArgumentException("Peer '" + peer + "' has not shared a macro named '" + macroName + "'");
            }

            lan.load(peer, macroName);
            break;
         case "sendChat":
            load(lan);
            if (!lan.init_Z_Str(init(args, "text"))) {
               throw new IllegalStateException("LAN chat was rejected by LAN Sync");
            }
            break;
         case "sharePreset":
            init(lan);
            lan.update(init(args, "name"));
            break;
         case "setDelayOffset":
            init(lan);
            lan.init_V_Str_I(init(args, "peer"), Math.max(0, AiJson.init_I_JsonObject(args, "offsetMs", 0)));
            break;
         case "setExecutionMethod":
            lan.init_V_aE_StreamUtil(autismclient.util.LanSyncManager.Inner_StreamUtil.valueOf(init(args, "method").toUpperCase(Locale.ROOT)));
            break;
         default:
            throw new IllegalArgumentException("Unknown LAN action '" + action + "'");
      }

      JsonObject out = init(new JsonObject());
      out.addProperty("action", action);
      out.addProperty("dispatched", true);
      return out;
   }

   private static void init(boolean enabled) {
      AutismConfig config = AutismConfig.getGlobal();
      config.lanSyncEnabled = enabled;
      config.save();
   }

   private static void init(autismclient.util.LanSyncManager lan) {
      if (!lan.init_Z()) {
         throw new IllegalArgumentException("LAN Sync is not running; use action start first");
      }
   }

   private static void load(autismclient.util.LanSyncManager lan) {
      if (!lan.write_Z()) {
         throw new IllegalArgumentException("Not in a LAN Sync session; createSession or joinSession first");
      }

      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null && mc.getConnection() != null) {
         autismclient.util.LanSyncManager.Inner_JsonUtil state = lan.init_aE_JsonUtil();
         if (state != autismclient.util.LanSyncManager.Inner_JsonUtil.IDLE && state != autismclient.util.LanSyncManager.Inner_JsonUtil.DONE) {
            throw new IllegalStateException("LAN Sync is busy with another synchronized action (" + state.name() + ")");
         }
      } else {
         throw new IllegalArgumentException("LAN Sync execution requires a connected world");
      }
   }

   private static void init(autismclient.util.LanSyncManager lan, Map<String, String> assignments) {
      Map<String, autismclient.util.LanSyncManager.Inner_DataUtil> clients = lan.init_Map();
      StringBuilder problems = new StringBuilder();

      for (Entry<String, String> entry : assignments.entrySet()) {
         String peer = entry.getKey();
         String macro = entry.getValue();
         if (!clients.containsKey(peer)) {
            problems.append(problems.length() == 0 ? "" : "; ").append("unknown peer '").append(peer).append("'");
         } else if (autismclient.util.MacroLibraryManager.init_aN().init_aM_Str(macro) == null && !init(lan, peer, macro)) {
            problems.append(problems.length() == 0 ? "" : "; ")
               .append("macro '")
               .append(macro)
               .append("' is not saved locally and not shared by '")
               .append(peer)
               .append("'");
         }
      }

      if (problems.length() > 0) {
         throw new IllegalArgumentException(problems.toString());
      }
   }

   private static boolean init(autismclient.util.LanSyncManager lan, String peer, String macro) {
      String key = MacroNameNormalizer.init(macro);

      for (String remote : lan.load_List(peer)) {
         if (MacroNameNormalizer.init(remote).equals(key)) {
            return true;
         }
      }

      return false;
   }
}
