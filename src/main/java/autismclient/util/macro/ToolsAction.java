package autismclient.util.macro;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.math.BigDecimal;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;

public final class ToolsAction implements MacroAction {
   public static final String ARG_PREFIX = "toolArg.";
   public String operation = "RUN";
   public String toolId = "game:goto";
   public String completion = "WAIT";
   public String scope = "THIS_MACRO";
   public String destination = "CURRENT";
   public String waypoint = "";
   public int f_x_I;
   public int f_y_I;
   public int f_z_I;
   public String argumentsJson = "{}";
   public boolean advanced;
   public boolean stopOnFailure = true;
   public String saveAs = "";
   private boolean init = true;

   public boolean init() {
      MacroToolBridge.Inner_TaskQueue tool = MacroToolBridge.findTool(this.toolId);
      if (tool == null) {
         return false;
      }

      JsonObject schema = JsonParser.parseString(tool.read()).getAsJsonObject();
      JsonObject properties = schema.getAsJsonObject("properties");
      if (properties == null) {
         return false;
      }

      String[] var4 = new String[]{"x", "y", "z"};
      int var5 = var4.length;
      int var6 = 0;

      while (var6 < var5) {
         String axis = var4[var6];
         if (properties.has(axis) && properties.get(axis).isJsonObject()) {
            JsonObject coordinate = properties.getAsJsonObject(axis);
            if (coordinate.has("type") && coordinate.get("type").isJsonPrimitive() && "integer".equals(coordinate.get("type").getAsString())) {
               var6++;
               continue;
            }

            return false;
         }

         return false;
      }

      return true;
   }

   public boolean load() {
      return "game:goto".equals(this.toolId) || "game:navigate".equals(this.toolId);
   }

   public JsonObject init_JsonObject_Minecraft(Minecraft mc) {
      JsonObject args = init_JsonObject_Str(this.argumentsJson);
      init_V_JsonElement_Minecraft(args, mc);
      if (this.init()) {
         if (this.load()) {
            for (String key : new String[]{"x", "y", "z", "dx", "dy", "dz", "waypoint"}) {
               args.remove(key);
            }
         }

         if ("WAYPOINT".equals(this.destination)) {
            if (!this.load()) {
               throw new IllegalArgumentException("This tool needs coordinates");
            }

            if (this.waypoint == null || this.waypoint.isBlank()) {
               throw new IllegalArgumentException("Choose a waypoint");
            }

            MacroTemplateResolver.ResolveResult resolved = MacroVariableContext.resolveTemplate(this.waypoint, mc);
            if (!resolved.success()) {
               throw new IllegalArgumentException("Waypoint variable has no value");
            }

            args.addProperty("waypoint", resolved.value());
         } else {
            int px = this.f_x_I;
            int py = this.f_y_I;
            int pz = this.f_z_I;
            if ("CURRENT".equals(this.destination)) {
               if (mc == null || mc.player == null) {
                  throw new IllegalStateException("Join a world to use the current position");
               }

               BlockPos pos = mc.player.blockPosition();
               px = pos.getX();
               py = pos.getY();
               pz = pos.getZ();
            }

            boolean relative = "OFFSET".equals(this.destination) && this.load();
            args.addProperty(relative ? "dx" : "x", px);
            args.addProperty(relative ? "dy" : "y", py);
            args.addProperty(relative ? "dz" : "z", pz);
         }
      }

      return args;
   }

   private static void init_V_JsonElement_Minecraft(JsonElement element, Minecraft mc) {
      if (element instanceof JsonObject object) {
         for (Entry<String, JsonElement> entry : object.entrySet()) {
            JsonElement value = entry.getValue();
            if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
               MacroTemplateResolver.ResolveResult resolved = MacroVariableContext.resolveTemplate(value.getAsString(), mc);
               if (!resolved.success()) {
                  throw new IllegalArgumentException("Tool argument has an unset variable: " + entry.getKey());
               }

               entry.setValue(new JsonPrimitive(resolved.value()));
            } else {
               init_V_JsonElement_Minecraft(value, mc);
            }
         }
      } else if (element instanceof JsonArray array) {
         for (int index = 0; index < array.size(); index++) {
            JsonElement value = array.get(index);
            if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
               MacroTemplateResolver.ResolveResult resolved = MacroVariableContext.resolveTemplate(value.getAsString(), mc);
               if (!resolved.success()) {
                  throw new IllegalArgumentException("Tool argument has an unset variable");
               }

               array.set(index, new JsonPrimitive(resolved.value()));
            } else {
               init_V_JsonElement_Minecraft(value, mc);
            }
         }
      }
   }

   public MacroToolBridge.Inner_StreamUtil startJob(Minecraft mc, long owner) {
      if (!"RUN".equals(this.operation)) {
         return MacroToolBridge.load(owner, this.scope, "");
      } else if (this.toolId != null && !this.toolId.isBlank()) {
         return MacroToolBridge.startTool(owner, this.toolId, this.init_JsonObject_Minecraft(mc).toString());
      } else {
         throw new IllegalArgumentException("Choose a tool");
      }
   }

   @Override
   public void execute(Minecraft mc) {
      if ("RUN".equals(this.operation)) {
         this.startJob(mc, MacroExecutor.getCurrentRunId());
      } else {
         MacroToolBridge.runOperation(MacroExecutor.getCurrentRunId(), this.scope, this.operation);
      }
   }

   private static JsonObject init_JsonObject_Str(String json) {
      JsonElement parsed = JsonParser.parseString(json != null && !json.isBlank() ? json : "{}");
      if (!parsed.isJsonObject()) {
         throw new IllegalArgumentException("Tool arguments must be an object");
      } else {
         return parsed.getAsJsonObject();
      }
   }

   public boolean applyToolArgBinding(String key, String value) {
      if (!key.startsWith("toolArg.")) {
         return false;
      }

      try {
         MacroToolBridge.Inner_TaskQueue definition = MacroToolBridge.findTool(this.toolId);
         if (definition == null) {
            return false;
         }

         JsonObject properties = JsonParser.parseString(definition.read()).getAsJsonObject().getAsJsonObject("properties");
         String name = key.substring("toolArg.".length());
         if (properties != null && properties.has(name)) {
            JsonObject property = properties.getAsJsonObject(name);
            if (property.has("type") && "boolean".equals(property.get("type").getAsString())) {
               if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("on")) {
                  value = "ON";
               } else if (!value.equalsIgnoreCase("false") && !value.equalsIgnoreCase("off")) {
                  if (!value.equalsIgnoreCase("default")) {
                     return false;
                  }

                  value = "DEFAULT";
               } else {
                  value = "OFF";
               }
            }

            CompoundTag tag = this.toTag();
            tag.putString(key, value);
            this.fromTag(tag);
            return true;
         } else {
            return false;
         }
      } catch (RuntimeException invalid) {
         return false;
      }
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "TOOLS");
      tag.putBoolean("enabled", this.init);
      tag.putString("operation", this.operation);
      tag.putString("toolId", this.toolId);
      tag.putString("completion", this.completion);
      tag.putString("scope", this.scope);
      tag.putString("destination", this.destination);
      tag.putString("waypoint", this.waypoint == null ? "" : this.waypoint);
      tag.putInt("x", this.f_x_I);
      tag.putInt("y", this.f_y_I);
      tag.putInt("z", this.f_z_I);
      tag.putBoolean("advanced", this.advanced);
      tag.putBoolean("stopOnFailure", this.stopOnFailure);
      tag.putString("saveAs", this.saveAs);
      tag.putString("argumentsJson", this.argumentsJson);

      for (Entry<String, JsonElement> entry : init_JsonObject_Str(this.argumentsJson).entrySet()) {
         String key = "toolArg." + entry.getKey();
         JsonElement value = entry.getValue();
         if (value.isJsonPrimitive()) {
            JsonPrimitive p = value.getAsJsonPrimitive();
            if (p.isBoolean()) {
               tag.putString(key, p.getAsBoolean() ? "ON" : "OFF");
            } else if (p.isNumber()) {
               double number = p.getAsDouble();
               if (number == (int)number) {
                  tag.putInt(key, (int)number);
               } else {
                  tag.putDouble(key, number);
               }
            } else {
               tag.putString(key, p.getAsString());
            }
         } else {
            tag.putString(key, value.toString());
         }
      }

      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.operation = tag.getStringOr("operation", "RUN");
      this.toolId = tag.getStringOr("toolId", "game:goto");
      this.completion = tag.getStringOr("completion", "WAIT");
      this.scope = tag.getStringOr("scope", "THIS_MACRO");
      this.destination = tag.getStringOr("destination", "CURRENT");
      this.waypoint = tag.getStringOr("waypoint", "");
      this.f_x_I = tag.getIntOr("x", 0);
      this.f_y_I = tag.getIntOr("y", 0);
      this.f_z_I = tag.getIntOr("z", 0);
      this.advanced = tag.getBooleanOr("advanced", false);
      this.stopOnFailure = tag.getBooleanOr("stopOnFailure", true);
      this.saveAs = tag.getStringOr("saveAs", "");
      this.init = tag.getBooleanOr("enabled", true);
      JsonObject args = init_JsonObject_Str(tag.getStringOr("argumentsJson", "{}"));
      MacroToolBridge.Inner_TaskQueue definition = MacroToolBridge.findTool(this.toolId);
      JsonObject schema = definition == null ? new JsonObject() : JsonParser.parseString(definition.read()).getAsJsonObject();
      JsonObject properties = schema.has("properties") ? schema.getAsJsonObject("properties") : new JsonObject();

      for (String key : tag.keySet()) {
         if (key.startsWith("toolArg.")) {
            String name = key.substring("toolArg.".length());
            JsonObject property = properties.has(name) ? properties.getAsJsonObject(name) : new JsonObject();
            String type = property.has("type")
               ? property.get("type").getAsString()
               : (args.has(name) && args.get(name).isJsonPrimitive() && args.getAsJsonPrimitive(name).isBoolean() ? "boolean" : "string");
            if (tag.get(key) instanceof NumericTag number) {
               args.addProperty(name, number.doubleValue());
            } else {
               String text = tag.getStringOr(key, "");
               if (!text.isBlank() && (!"DEFAULT".equals(text) || !property.has("enum") && !"boolean".equals(type))) {
                  if ("boolean".equals(type)) {
                     args.addProperty(name, "ON".equals(text));
                  } else if ("number".equals(type) || "integer".equals(type)) {
                     args.addProperty(name, new BigDecimal(text));
                  } else if (!"object".equals(type) && !"array".equals(type)) {
                     args.addProperty(name, text);
                  } else {
                     args.add(name, JsonParser.parseString(text));
                  }
               } else {
                  args.remove(name);
               }
            }
         }
      }

      this.argumentsJson = args.toString();
   }

   @Override
   public void sanitizeForSharing() {
      this.toolId = "";
      this.argumentsJson = "{}";
      this.waypoint = "";
      this.destination = "CURRENT";
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.TOOLS;
   }

   @Override
   public String getDisplayName() {
      return "Tools: " + ("RUN".equals(this.operation) ? MacroToolBridge.toolLabel(this.toolId) : this.operation.toLowerCase());
   }

   @Override
   public String getIcon() {
      return "TOOLS";
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean value) {
      this.init = value;
   }
}
