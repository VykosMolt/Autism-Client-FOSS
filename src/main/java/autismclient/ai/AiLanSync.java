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
   static final int MAX_REMOTE_MACROS = 100;
   static final List<String> ACTIONS = List.of(
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

   static void runAction(String action, JsonObject args) {
      if (action == null || action.isBlank()) {
         throw new IllegalArgumentException("action is required; one of: " + String.join(", ", ACTIONS));
      }

      if (!ACTIONS.contains(action)) {
         throw new IllegalArgumentException("Unknown LAN action '" + action + "'; valid actions: " + String.join(", ", ACTIONS));
      }

      switch (action) {
         case "joinSession":
            requireString(args, "sessionId");
            break;
         case "executeMacroSynchronized":
            requireString(args, "macro");
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
            requireString(args, "name");
            break;
         case "sharePreset":
            requireString(args, "name");
            break;
         case "importMacro":
            requireString(args, "peer");
            requireString(args, "name");
            break;
         case "setDelayOffset":
            requireString(args, "peer");
            if (!args.has("offsetMs")) {
               throw new IllegalArgumentException("offsetMs is required for setDelayOffset");
            }
            break;
         case "setExecutionMethod":
            String method = requireString(args, "method").toUpperCase(Locale.ROOT);
            if (!method.equals("INSTANT") && !method.equals("TICK")) {
               throw new IllegalArgumentException("method must be INSTANT or TICK");
            }
            break;
         case "sendChat":
            String text = requireString(args, "text");
            if (text.length() > 256) {
               throw new IllegalArgumentException("text must be at most 256 characters");
            }
      }
   }

   private static String requireString(JsonObject args, String key) {
      String value = AiJson.getString(args, key, "");
      if (value.isBlank()) {
         throw new IllegalArgumentException(key + " is required for this LAN action");
      } else {
         return value.trim();
      }
   }

   public static JsonObject status(JsonObject args) {
      autismclient.util.LanSyncManager lan = autismclient.util.LanSyncManager.getInstance();
      JsonObject out = new JsonObject();
      out.addProperty("running", lan.isRunning());
      out.addProperty("inSession", lan.isInSession());
      out.addProperty("host", lan.isHost());
      if (lan.getSessionId() != null && !lan.getSessionId().isBlank()) {
         out.addProperty("sessionId", lan.getSessionId());
      }

      out.addProperty("syncState", lan.getSyncState().name());
      out.addProperty("searching", lan.isSearching());
      if (lan.isSearching()) {
         out.addProperty("searchSecondsRemaining", lan.getSearchSecondsRemaining());
      }

      String self = lan.getLocalUsername();
      JsonArray clients = new JsonArray();

      for (Entry<String, autismclient.util.LanSyncManager.ClientInfo> entry : lan.getConnectedClients().entrySet()) {
         autismclient.util.LanSyncManager.ClientInfo info = entry.getValue();
         clients.add(
            AiJson.objectOf(
               "name",
               info.username,
               "host",
               info.isHost,
               "self",
               entry.getKey().equals(self),
               "status",
               lan.getPeerHealth(entry.getKey()).name(),
               "delayOffsetMs",
               info.offsetMs
            )
         );
      }

      out.add("clients", clients);
      out.addProperty("connectedCount", lan.getConnectedClientCount());
      Map<String, String> assignments = lan.getSyncedMacroAssignments();
      if (!assignments.isEmpty()) {
         out.add("assignments", AiJson.GSON.toJsonTree(assignments));
         if (!lan.getSyncedAssignmentsSender().isBlank()) {
            out.addProperty("assignmentSetBy", lan.getSyncedAssignmentsSender());
         }
      }

      List<String> remoteMacros = lan.getRemoteMacroNames();
      out.add("remoteMacros", AiJson.GSON.toJsonTree(remoteMacros.size() > 100 ? remoteMacros.subList(0, 100) : remoteMacros));
      if (remoteMacros.size() > 100) {
         out.addProperty("remoteMacrosTruncated", remoteMacros.size() - 100);
      }

      out.addProperty("lanSyncEnabled", AutismConfig.getGlobal().lanSyncEnabled);
      out.addProperty("executionMethod", lan.getExecutionMethod().name());
      return out;
   }

   public static JsonObject control(JsonObject args) {
      String action = AiJson.getString(args, "action", "");
      runAction(action, args);
      autismclient.util.LanSyncManager lan = autismclient.util.LanSyncManager.getInstance();
      switch (action) {
         case "start":
            if (autismclient.modules.aD.read_Z()) {
               throw new IllegalStateException("Client is hidden (panic mode); LAN Sync cannot start");
            }

            if (lan.isRunning()) {
               throw new IllegalArgumentException("LAN Sync is already running");
            }

            setLanSyncEnabled(true);
            lan.startService();
            break;
         case "stop":
            if (!lan.isRunning()) {
               throw new IllegalArgumentException("LAN Sync is not running");
            }

            setLanSyncEnabled(false);
            lan.stopService();
            break;
         case "createSession":
            requireRunning(lan);
            lan.createSession();
            break;
         case "joinSession":
            requireRunning(lan);
            String sessionId = requireString(args, "sessionId");
            if (sessionId.equals(lan.getSessionId())) {
               throw new IllegalArgumentException("Already in session " + sessionId);
            }

            String hostIp = AiJson.getString(args, "hostIp", "");
            if (hostIp.isBlank()) {
               lan.joinSession(sessionId);
            } else {
               lan.joinSession(sessionId, hostIp);
            }
            break;
         case "leaveSession":
            if (!lan.isInSession()) {
               throw new IllegalArgumentException("Not in a LAN Sync session");
            }

            lan.leaveSession();
            break;
         case "startSearching":
            requireRunning(lan);
            if (lan.isInSession()) {
               throw new IllegalArgumentException("Already in a session; leave it before searching");
            }

            lan.startSessionSearch();
            break;
         case "cancelSearch":
            if (!lan.isSearching()) {
               throw new IllegalArgumentException("No session search in progress");
            }

            lan.cancelSessionSearch();
            break;
         case "executeMacroSynchronized":
            requireSession(lan);
            String macro = requireString(args, "macro");
            if (autismclient.util.MacroLibraryManager.getInstance().findByName(macro) == null) {
               throw new IllegalArgumentException("Unknown macro '" + macro + "'; inspect client:macros for saved names");
            }

            lan.triggerMacroSync(macro);
            break;
         case "executeMacrosSynchronized":
            requireSession(lan);
            Map<String, String> assignments = new LinkedHashMap<>();

            for (Entry<String, JsonElement> entry : args.getAsJsonObject("assignments").entrySet()) {
               assignments.put(entry.getKey(), entry.getValue().getAsString().trim());
            }

            assignMacros(lan, assignments);
            lan.triggerPerUserMacroSync(assignments);
            break;
         case "broadcastStopAll":
            if (!lan.isInSession()) {
               throw new IllegalArgumentException("Not in a LAN Sync session");
            }

            lan.stopAllMacrosOnLan();
            break;
         case "shareMacro":
            if (!lan.isInSession()) {
               throw new IllegalArgumentException("Not in a LAN Sync session");
            }

            String name = requireString(args, "name");
            if (autismclient.util.MacroLibraryManager.getInstance().findByName(name) == null) {
               throw new IllegalArgumentException("Unknown macro '" + name + "'; inspect client:macros for saved names");
            }

            if (!lan.shareMacroWithPeers(name)) {
               throw new IllegalStateException("Macro share was rejected by LAN Sync");
            }
            break;
         case "importMacro":
            if (!lan.isInSession()) {
               throw new IllegalArgumentException("Not in a LAN Sync session");
            }

            String peer = requireString(args, "peer");
            String macroName = requireString(args, "name");
            if (!lan.getConnectedClients().containsKey(peer)) {
               throw new IllegalArgumentException("Unknown peer '" + peer + "'; check clients in the LAN status");
            }

            if (!peerHasMacro(lan, peer, macroName)) {
               throw new IllegalArgumentException("Peer '" + peer + "' has not shared a macro named '" + macroName + "'");
            }

            lan.importPeerMacro(peer, macroName);
            break;
         case "sendChat":
            requireSession(lan);
            if (!lan.sendSyncedChat(requireString(args, "text"))) {
               throw new IllegalStateException("LAN chat was rejected by LAN Sync");
            }
            break;
         case "sharePreset":
            requireRunning(lan);
            lan.sharePresetWithPeers(requireString(args, "name"));
            break;
         case "setDelayOffset":
            requireRunning(lan);
            lan.setClientOffsetMs(requireString(args, "peer"), Math.max(0, AiJson.getInt(args, "offsetMs", 0)));
            break;
         case "setExecutionMethod":
            lan.setExecutionMethod(autismclient.util.LanSyncManager.ExecutionMethod.valueOf(requireString(args, "method").toUpperCase(Locale.ROOT)));
            break;
         default:
            throw new IllegalArgumentException("Unknown LAN action '" + action + "'");
      }

      JsonObject out = status(new JsonObject());
      out.addProperty("action", action);
      out.addProperty("dispatched", true);
      return out;
   }

   private static void setLanSyncEnabled(boolean enabled) {
      AutismConfig config = AutismConfig.getGlobal();
      config.lanSyncEnabled = enabled;
      config.save();
   }

   private static void requireRunning(autismclient.util.LanSyncManager lan) {
      if (!lan.isRunning()) {
         throw new IllegalArgumentException("LAN Sync is not running; use action start first");
      }
   }

   private static void requireSession(autismclient.util.LanSyncManager lan) {
      if (!lan.isInSession()) {
         throw new IllegalArgumentException("Not in a LAN Sync session; createSession or joinSession first");
      }

      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null && mc.getConnection() != null) {
         autismclient.util.LanSyncManager.SyncState state = lan.getSyncState();
         if (state != autismclient.util.LanSyncManager.SyncState.IDLE && state != autismclient.util.LanSyncManager.SyncState.DONE) {
            throw new IllegalStateException("LAN Sync is busy with another synchronized action (" + state.name() + ")");
         }
      } else {
         throw new IllegalArgumentException("LAN Sync execution requires a connected world");
      }
   }

   private static void assignMacros(autismclient.util.LanSyncManager lan, Map<String, String> assignments) {
      Map<String, autismclient.util.LanSyncManager.ClientInfo> clients = lan.getConnectedClients();
      StringBuilder problems = new StringBuilder();

      for (Entry<String, String> entry : assignments.entrySet()) {
         String peer = entry.getKey();
         String macro = entry.getValue();
         if (!clients.containsKey(peer)) {
            problems.append(problems.length() == 0 ? "" : "; ").append("unknown peer '").append(peer).append("'");
         } else if (autismclient.util.MacroLibraryManager.getInstance().findByName(macro) == null && !peerHasMacro(lan, peer, macro)) {
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

   private static boolean peerHasMacro(autismclient.util.LanSyncManager lan, String peer, String macro) {
      String key = MacroNameNormalizer.normalize(macro);

      for (String remote : lan.getPeerMacroNames(peer)) {
         if (MacroNameNormalizer.normalize(remote).equals(key)) {
            return true;
         }
      }

      return false;
   }
}
