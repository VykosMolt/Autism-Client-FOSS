package autismclient.ai;

import autismclient.api.ai.AiCapabilityDescriptor;
import autismclient.api.macro.MacroActionRegistry;
import autismclient.gui.macro.editor.ActionFieldSchema;
import autismclient.gui.macro.editor.FieldDef;
import autismclient.util.macro.MacroAction;
import autismclient.util.macro.MacroActionType;
import autismclient.util.macro.MacroCondition;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;

public final class AiMacroValidator {
   private AiMacroValidator() {
   }

   public static autismclient.util.AutismMacro validate(JsonObject root, AiCatalog.Snapshot catalog) {
      if (root == null) {
         throw new IllegalArgumentException("Macro requires a JSON object with a name and actions array");
      }

      if (root.has("actions") && root.get("actions").isJsonArray()) {
         JsonArray actions = root.getAsJsonArray("actions");
         if (!actions.isEmpty() && actions.size() <= 512) {
            String name = AiJson.getString(root, "name", "AI Macro").trim();
            if (!name.isEmpty() && name.length() <= 128) {
               Set<String> labels = new HashSet<>();

               for (JsonElement value : actions) {
                  JsonObject action = value.getAsJsonObject();
                  validateAction(action, catalog, 0);
                  if (AiJson.getString(action, "type", "").equals("LABEL")) {
                     String label = AiJson.getString(action, "name", AiJson.getString(action, "label", ""));
                     if (!label.isEmpty() && !labels.add(label.trim().toLowerCase(Locale.ROOT))) {
                        throw new IllegalArgumentException("Duplicate label " + label);
                     }
                  }
               }

               for (int i = 0; i < actions.size(); i++) {
                  JsonObject a = actions.get(i).getAsJsonObject();
                  String type = AiJson.getString(a, "type", "");
                  if (type.equals("REPEAT") && AiJson.getInt(a, "stepCount", 1) > actions.size() - i - 1) {
                     throw new IllegalArgumentException("Repeat extends beyond final step");
                  }

                  if (type.equals("IF") && (long)AiJson.getInt(a, "thenSteps", 1) + AiJson.getInt(a, "elseSteps", 0) > actions.size() - i - 1) {
                     throw new IllegalArgumentException("If/else span extends beyond final step");
                  }

                  if (type.equals("FLOW")
                     && AiJson.getString(a, "target", "LABEL").equalsIgnoreCase("LABEL")
                     && !labels.contains(AiJson.getString(a, "labelName", "").trim().toLowerCase(Locale.ROOT))) {
                     throw new IllegalArgumentException("Missing flow label");
                  }
               }

               CompoundTag tag = (CompoundTag)JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, root);
               autismclient.util.AutismMacro macro = new autismclient.util.AutismMacro().fromTag(tag);
               macro.name = name;
               if (macro.actions.size() != actions.size()) {
                  throw new IllegalArgumentException("Macro contains an unsupported action");
               }

               for (int i = 0; i < actions.size(); i++) {
                  JsonObject supplied = actions.get(i).getAsJsonObject();
                  MacroAction built = macro.actions.get(i);
                  String type = AiJson.getString(supplied, "type", "");
                  if (built instanceof autismclient.util.macro.MissingAddonAction) {
                     throw new IllegalArgumentException("Action " + type + " could not be deserialized");
                  }

                  JsonObject serialized = NbtOps.INSTANCE
                     .convertTo(JsonOps.INSTANCE, autismclient.util.AutismMacro.serializeAction(built))
                     .getAsJsonObject();
                  Set<String> bound = supplied.get("_dynamicBindings") instanceof JsonObject bindings ? bindings.keySet() : Set.of();
                  Set<String> derived = type.equals("RACE") ? Set.of("conditionType", "actionType") : Set.of();

                  for (String key : supplied.keySet()) {
                     if (!key.equals("_dynamicBindings")
                        && !bound.contains(key)
                        && !derived.contains(key)
                        && (!serialized.has(key) || !matchesSerialized(supplied.get(key), serialized.get(key)))) {
                        throw new IllegalArgumentException(
                           type
                              + " field "
                              + key
                              + " is lost or changed by native serialization ("
                              + supplied.get(key)
                              + " becomes "
                              + serialized.get(key)
                              + "); copy exact serialized defaults from catalog_inspect"
                        );
                     }
                  }
               }

               return macro;
            } else {
               throw new IllegalArgumentException("Invalid macro name");
            }
         } else {
            throw new IllegalArgumentException("Macro requires 1..512 actions");
         }
      } else {
         throw new IllegalArgumentException("Macro requires actions array");
      }
   }

   private static boolean matchesSerialized(JsonElement supplied, JsonElement serialized) {
      if (supplied.isJsonNull()) {
         return serialized.isJsonNull();
      }

      if (supplied.isJsonObject() && serialized.isJsonObject()) {
         for (Entry<String, JsonElement> e : supplied.getAsJsonObject().entrySet()) {
            if (!serialized.getAsJsonObject().has(e.getKey()) || !matchesSerialized(e.getValue(), serialized.getAsJsonObject().get(e.getKey()))) {
               return false;
            }
         }

         return true;
      } else if (supplied.isJsonArray() && serialized.isJsonArray()) {
         JsonArray a = supplied.getAsJsonArray();
         JsonArray b = serialized.getAsJsonArray();
         if (a.size() != b.size()) {
            return false;
         }

         for (int i = 0; i < a.size(); i++) {
            if (!matchesSerialized(a.get(i), b.get(i))) {
               return false;
            }
         }

         return true;
      } else {
         if (supplied.isJsonPrimitive() && serialized.isJsonPrimitive()) {
            JsonPrimitive s = supplied.getAsJsonPrimitive();
            JsonPrimitive r = serialized.getAsJsonPrimitive();
            if (s.isString() && r.isString()) {
               return s.getAsString().equals(r.getAsString()) || s.getAsString().equalsIgnoreCase(r.getAsString());
            }

            if (s.isNumber() && r.isNumber()) {
               if (Double.isFinite(s.getAsDouble()) && Double.isFinite(r.getAsDouble())) {
                  return s.getAsBigDecimal().compareTo(r.getAsBigDecimal()) == 0 || Float.compare(s.getAsFloat(), r.getAsFloat()) == 0;
               }

               return false;
            }

            if (s.isBoolean() && r.isBoolean()) {
               return s.getAsBoolean() == r.getAsBoolean();
            }

            if (s.isBoolean() && r.isNumber()) {
               return zeroOrOne(r) != null && zeroOrOne(r) == 1 == s.getAsBoolean();
            }

            if (s.isNumber() && r.isBoolean()) {
               return false;
            }
         }

         return false;
      }
   }

   private static Integer zeroOrOne(JsonPrimitive value) {
      try {
         int n = value.getAsBigDecimal().intValueExact();
         return n != 0 && n != 1 ? null : n;
      } catch (ArithmeticException e) {
         return null;
      }
   }

   private static void validateAction(JsonObject input, AiCatalog.Snapshot catalog, int depth) {
      if (depth > 16) {
         throw new IllegalArgumentException("Macro nesting exceeds 16");
      }

      String id = AiJson.getString(input, "type", "");
      AiCatalog.Entry entry = catalog.get("macro:" + id);
      if (entry != null && entry.contract().classification() != AiCapabilityDescriptor.Classification.DISCOVER_ONLY) {
         JsonObject defaults = AiJson.parseObject(entry.defaultsJson());
         Set<String> conditional = id.equals("RACE") ? Set.of("triggerAction", "triggerType") : Set.of();

         for (String key : input.keySet()) {
            if (!defaults.has(key) && !conditional.contains(key) && !key.equals("_dynamicBindings")) {
               throw new IllegalArgumentException("Unknown " + id + " field: " + key);
            }
         }

         if (id.equals("RACE")) {
            if (input.has("triggerType")) {
               try {
                  autismclient.util.macro.RaceAction.ConditionType.valueOf(AiJson.getString(input, "triggerType", "").trim().toUpperCase(Locale.ROOT));
               } catch (IllegalArgumentException e) {
                  throw new IllegalArgumentException("Unknown RACE triggerType " + AiJson.getString(input, "triggerType", ""));
               }
            }

            if (input.has("triggerAction") && !input.get("triggerAction").isJsonObject()) {
               throw new IllegalArgumentException("RACE triggerAction must be a nested action object");
            }
         }

         ActionFieldSchema schema;
         try {
            schema = autismclient.gui.macro.editor.ActionFieldRegistry.schemaForType(MacroActionType.valueOf(id));
         } catch (IllegalArgumentException e) {
            schema = MacroActionRegistry.schema(id);
         }

         if (input.has("_dynamicBindings")) {
            for (Entry<String, JsonElement> binding : input.getAsJsonObject("_dynamicBindings").entrySet()) {
               boolean allowed = schema != null && schema.fields().stream().anyMatch(fx -> fx.key().equals(binding.getKey()) && fx.dynamic());
               if (!allowed
                  || !binding.getValue().isJsonPrimitive()
                  || !binding.getValue().getAsJsonPrimitive().isString()
                  || binding.getValue().getAsString().isBlank()) {
                  throw new IllegalArgumentException("Invalid dynamic binding: " + binding.getKey());
               }
            }
         }

         if (schema != null) {
            for (FieldDef f : schema.fields()) {
               if (input.has(f.key()) && !input.get(f.key()).isJsonNull()) {
                  JsonElement v = input.get(f.key());
                  if (f.dynamic() && v.isJsonPrimitive() && v.getAsJsonPrimitive().isString() && v.getAsString().contains("{")) {
                     throw new IllegalArgumentException("Place templates in _dynamicBindings, leaving the field's native typed fallback intact: " + f.key());
                  }

                  switch (f.type()) {
                     case NUMBER:
                     case SLOT:
                        if (!v.isJsonPrimitive() || !v.getAsJsonPrimitive().isNumber() || !Double.isFinite(v.getAsDouble())) {
                           throw new IllegalArgumentException("Field " + f.key() + " must be a number");
                        }

                        BigDecimal decimal = v.getAsBigDecimal();
                        if (decimal.stripTrailingZeros().scale() > 0) {
                           throw new IllegalArgumentException("Field " + f.key() + " must be a whole number");
                        }

                        long n;
                        try {
                           n = decimal.longValueExact();
                        } catch (ArithmeticException e) {
                           throw new IllegalArgumentException("Out of range " + f.key());
                        }

                        if (n < f.min() || n > f.max()) {
                           throw new IllegalArgumentException("Out of range " + f.key());
                        }
                        break;
                     case DECIMAL:
                        if (!v.isJsonPrimitive() || !v.getAsJsonPrimitive().isNumber()) {
                           throw new IllegalArgumentException("Field " + f.key() + " must be a number");
                        }

                        double dVal = v.getAsDouble();
                        if (!Double.isFinite(dVal) || dVal < f.decMin() || dVal > f.decMax()) {
                           throw new IllegalArgumentException("Out of range " + f.key());
                        }
                        break;
                     case TOGGLE:
                        AiJson.getBoolean(input, f.key(), false);
                        break;
                     case ENUM:
                        if (!v.isJsonPrimitive()
                           || !v.getAsJsonPrimitive().isString()
                           || f.enumOptions().stream().noneMatch(o -> o.equalsIgnoreCase(v.getAsString()))) {
                           throw new IllegalArgumentException("Unknown enum value " + f.key());
                        }
                  }

                  if (f.type() == autismclient.gui.macro.editor.FieldType.TOGGLE && AiJson.getBoolean(input, f.key(), false)) {
                     for (String other : f.mutuallyExclusiveWith()) {
                        if (AiJson.getBoolean(input, other, false)) {
                           throw new IllegalArgumentException("Mutually exclusive fields " + f.key() + " and " + other);
                        }
                     }
                  }
               }
            }
         }

         if (input.has("condition") && input.get("condition").isJsonObject()) {
            validateCondition(input.getAsJsonObject("condition"), 0);
         }

         validateNested((JsonElement)input, catalog, depth);
      } else {
         throw new IllegalArgumentException("No executable action contract for " + id);
      }
   }

   static void validateCondition(JsonObject node, int depth) {
      if (depth > 16) {
         throw new IllegalArgumentException("Condition nesting exceeds 16");
      }

      MacroCondition.NodeType type = MacroCondition.NodeType.valueOf(AiJson.getString(node, "node", "GROUP"));
      AiJson.getBoolean(node, "negate", false);
      if (type == MacroCondition.NodeType.GROUP) {
         MacroCondition.Combine.valueOf(AiJson.getString(node, "combine", "ALL"));
         if (!node.has("children") || !node.get("children").isJsonArray() || node.getAsJsonArray("children").isEmpty()) {
            throw new IllegalArgumentException("Condition group must contain at least one explicit child");
         }

         if (node.getAsJsonArray("children").size() > 128) {
            throw new IllegalArgumentException("Condition group is too large");
         }

         for (JsonElement child : node.getAsJsonArray("children")) {
            validateCondition(child.getAsJsonObject(), depth + 1);
         }
      } else {
         MacroCondition.ConditionKind.valueOf(AiJson.getString(node, "kind", "ALWAYS"));
         MacroCondition.Operator.valueOf(AiJson.getString(node, "op", "EQ"));
         MacroCondition.Comparison.valueOf(AiJson.getString(node, "cmp", "AT_LEAST"));
         if (node.has("range") && AiJson.getDouble(node, "range", 0.0) < 0.0) {
            throw new IllegalArgumentException("Condition range cannot be negative");
         }
      }
   }

   private static void validateNested(JsonElement value, AiCatalog.Snapshot catalog, int depth) {
      if (depth > 24) {
         throw new IllegalArgumentException("Macro structure too deep");
      }

      if (value.isJsonArray()) {
         for (JsonElement child : value.getAsJsonArray()) {
            validateNested(child, catalog, depth + 1);
         }
      } else if (value.isJsonObject()) {
         for (Entry<String, JsonElement> e : value.getAsJsonObject().entrySet()) {
            JsonElement child = e.getValue();
            if (child.isJsonObject()
               && child.getAsJsonObject().has("type")
               && catalog.get("macro:" + AiJson.getString(child.getAsJsonObject(), "type", "")) != null) {
               validateAction(child.getAsJsonObject(), catalog, depth + 1);
            } else if (child.isJsonArray() || child.isJsonObject()) {
               validateNested(child, catalog, depth + 1);
            }
         }
      }
   }

   public static String describe(MacroActionType type) {
      String var10000;
      switch (type) {
         case REPEAT:
            var10000 = "Repeats the NEXT stepCount steps repeatCount times; this is not a nested action array.";
            break;
         case FLOW:
            var10000 = "Branches to a label, step, relative offset, top, end, or stop; label targets must exist.";
            break;
         case IF:
            var10000 = "Evaluates native macro conditions and controls subsequent flow; inspect serialized defaults for branch layout.";
            break;
         case GO_TO:
         case MINE:
            var10000 = "Delegates to an external navigation integration when present; does not provide native navigation. Use game:navigate for native travel.";
            break;
         case TOGGLE_MODULE:
            var10000 = "Changes module activation; enabling a module is not proof of its gameplay effect.";
            break;
         case TRADE:
            var10000 = "Trades with a reachable merchant. Amount counts offer uses, not output items. Writes trade_count, trade_spent and trade_status; uncertain purchases are never repeated. Rerolling is the AutoTrade module.";
            break;
         case CRAFT:
            var10000 = "Uses the client's crafting workflow; ingredients, recipe availability, station, and inventory capacity matter.";
            break;
         case CUSTOM_MENU:
            var10000 = "Submits a server custom form using live adapter and generation data; stale forms require a fresh observation.";
            break;
         case START_MACRO:
         case STOP_MACRO:
            var10000 = "Controls another named macro. The target must exist; starting does not imply completion.";
            break;
         case DELAY:
            var10000 = "Waits locally for the configured interval.";
            break;
         default:
            var10000 = type.name().startsWith("WAIT")
               ? "Waits for the configured live condition, with its timeout policy; does not create that condition."
               : "Native "
                  + type.name().toLowerCase(Locale.ROOT).replace('_', ' ')
                  + " action. Use the exact serialized field layout and inspect game observations after execution.";
      }

      return var10000;
   }
}
