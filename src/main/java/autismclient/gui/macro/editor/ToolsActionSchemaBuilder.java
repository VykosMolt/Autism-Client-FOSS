package autismclient.gui.macro.editor;

import autismclient.util.macro.ToolsAction;
import autismclient.util.macro.MacroToolBridge;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Map.Entry;

public final class ToolsActionSchemaBuilder {
   private ToolsActionSchemaBuilder() {
   }

   public static ActionFieldSchema init(ToolsAction action) {
      ActionFieldSchema.Builder builder = ActionFieldSchema.builder().enumField("operation", "Action", "RUN", "WAIT", "STOP", "PAUSE", "RESUME");
      List<String> ids = new ArrayList<>(MacroToolBridge.listTools().stream().map(MacroToolBridge.ToolInfo::init).toList());
      if (!ids.contains(action.toolId)) {
         ids.addFirst(action.toolId);
      }

      builder.enumField("toolId", "Tool", ids.toArray(String[]::new))
         .showWhenEnum("operation", "RUN")
         .enumField("completion", "Continue", "WAIT", "CONTINUE")
         .showWhenEnum("operation", "RUN")
         .enumField("scope", "Target", "THIS_MACRO", "ANY_TOOL")
         .hideWhenEnum("operation", "RUN");
      if (action.init()) {
         builder.enumField(
               "destination", "Destination", action.isNavigationTool() ? new String[]{"CURRENT", "CUSTOM", "WAYPOINT", "OFFSET"} : new String[]{"CURRENT", "CUSTOM"}
            )
            .showWhenEnum("operation", "RUN");
         builder.blockPos("position", "Position").xyzKeys("x", "y", "z").dynamic();
         ArrayList<String> waypoints = new ArrayList<>(MacroToolBridge.waypointNames());
         if (!waypoints.contains(action.waypoint)) {
            waypoints.addFirst(action.waypoint);
         }

         builder.enumField("waypoint", "Waypoint", waypoints.toArray(String[]::new)).dynamic();
      }

      builder.toggle("advanced", "More Options").showWhenEnum("operation", "RUN");
      MacroToolBridge.ToolInfo tool = MacroToolBridge.findTool(action.toolId);
      if (tool != null) {
         JsonObject schema = JsonParser.parseString(tool.read()).getAsJsonObject();
         Set<String> required = new HashSet<>();
         if (schema.has("required")) {
            for (JsonElement key : schema.getAsJsonArray("required")) {
               required.add(key.getAsString());
            }
         }

         JsonObject properties = schema.getAsJsonObject("properties");
         if (properties != null) {
            for (Entry<String, JsonElement> entry : properties.entrySet()) {
               String name = entry.getKey();
               if ((!action.init() || !Set.of("x", "y", "z").contains(name))
                  && (!action.isNavigationTool() || !Set.of("waypoint", "dx", "dy", "dz", "entity", "player", "direction", "distance").contains(name))) {
                  JsonObject property = entry.getValue().getAsJsonObject();
                  String type = property.has("type") && property.get("type").isJsonPrimitive() ? property.get("type").getAsString() : "string";
                  String key = "toolArg." + name;
                  String label = init(name);
                  boolean optional = !required.contains(name);
                  if (property.has("enum")) {
                     List<String> options = new ArrayList<>();
                     if (optional) {
                        options.add("DEFAULT");
                     }

                     for (JsonElement value : property.getAsJsonArray("enum")) {
                        options.add(value.getAsString());
                     }

                     builder.enumField(key, label, options.toArray(String[]::new));
                  } else {
                     label154: {
                        switch (type) {
                           case "boolean":
                              builder.enumField(key, label, optional ? new String[]{"DEFAULT", "ON", "OFF"} : new String[]{"ON", "OFF"});
                              break label154;
                           case "integer":
                              builder.number(key, label).dynamic();
                              break label154;
                           case "number":
                              builder.decimal(key, label).dynamic();
                              break label154;
                        }

                        builder.text(key, label + (!type.equals("object") && !type.equals("array") ? "" : " (JSON)")).dynamic();
                     }
                  }

                  builder.dynamic();
                  if (optional
                     && !Set.of("block", "item", "count", "target", "entity", "player", "action", "message", "name", "radius", "x", "y", "z", "yaw", "pitch")
                        .contains(name)) {
                     builder.showWhen("advanced");
                  }
               }
            }
         }
      }

      return builder.toggle("stopOnFailure", "Stop Macro on Failure").text("saveAs", "Save Result As").showWhen("advanced").build();
   }

   public static String init(String value) {
      if (value != null && !value.isBlank()) {
         String words = value.replaceAll("([a-z])([A-Z])", "$1 $2").replace('_', ' ');
         return words.substring(0, 1).toUpperCase(Locale.ROOT) + words.substring(1);
      } else {
         return "Choose...";
      }
   }

   public static String init(String key, String value) {
      if (!key.equals("toolId") && !key.equals("cond_toolFilter")) {
         if (key.equals("completion")) {
            return value.equals("WAIT") ? "When tool finishes" : "While tool runs";
         }

         if (key.equals("destination")) {
            String var10000;
            switch (value) {
               case "CURRENT":
                  var10000 = "Current position";
                  break;
               case "CUSTOM":
                  var10000 = "Coordinates";
                  break;
               case "WAYPOINT":
                  var10000 = "Waypoint";
                  break;
               case "OFFSET":
                  var10000 = "Relative offset";
                  break;
               default:
                  var10000 = value;
            }

            return var10000;
         } else if (key.equals("scope") || key.equals("cond_toolScope")) {
            return value.equals("THIS_MACRO") ? "This macro's last tool" : "Current tool";
         } else if (key.equals("waypoint")) {
            return value.isBlank() ? "Choose waypoint..." : value;
         } else {
            return init(value.toLowerCase(Locale.ROOT));
         }
      } else {
         return value.isBlank() ? "Any tool" : MacroToolBridge.toolLabel(value);
      }
   }
}
