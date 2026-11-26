package autismclient.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

final class AiSchemas {
   private AiSchemas() {
   }

   static JsonObject init(JsonObject schema, JsonObject value, boolean dropUnknown, List<String> ignored) {
      JsonElement out = init(schema, value.deepCopy(), "", dropUnknown, ignored, 0);
      return out.isJsonObject() ? out.getAsJsonObject() : value;
   }

   private static boolean auditMatchesKind(JsonObject schema, JsonElement value) {
      String type = AiJson.getString(schema, "type", "");
      switch (type) {
         case "object":
            return value.isJsonObject();
         case "array":
            return value.isJsonArray();
         case "string":
            return value.isJsonPrimitive() && value.getAsJsonPrimitive().isString();
         case "boolean":
            return value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean();
         case "number":
         case "integer":
            return value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber();
         case "null":
            return value.isJsonNull();
         default:
            return false;
      }
   }

   private static boolean auditIsNullable(JsonObject property) {
      if (property == null) {
         return false;
      }
      if ("null".equals(AiJson.getString(property, "type", ""))) {
         return true;
      }
      if (property.has("anyOf") && property.get("anyOf").isJsonArray()) {
         for (JsonElement alt : property.getAsJsonArray("anyOf")) {
            if (alt.isJsonObject() && auditIsNullable(alt.getAsJsonObject())) {
               return true;
            }
         }
      }
      return false;
   }

   private static JsonElement init(JsonObject schema, JsonElement value, String path, boolean dropUnknown, List<String> ignored, int depth) {
      if (depth <= 24 && value != null) {
         if (schema.has("anyOf") && schema.get("anyOf").isJsonArray()) {
            JsonArray alternatives = schema.getAsJsonArray("anyOf");
            String checkPath = path.isEmpty() ? "arguments" : path;
            for (int pass = 0; pass < 2; pass++) {
               for (JsonElement altEl : alternatives) {
                  if (!altEl.isJsonObject()) {
                     continue;
                  }
                  JsonObject alt = altEl.getAsJsonObject();
                  boolean matchesKind = auditMatchesKind(alt, value);
                  if ((pass == 0 && !matchesKind) || (pass == 1 && matchesKind)) {
                     continue;
                  }
                  List<String> altIgnored = new ArrayList<>();
                  JsonElement candidate = init(alt, value.deepCopy(), path, dropUnknown, altIgnored, depth + 1);
                  try {
                     init(alt, candidate, checkPath, depth + 1);
                     ignored.addAll(altIgnored);
                     return candidate;
                  } catch (IllegalArgumentException ignoredErr) {
                  }
               }
            }

            return value;
         }

         String type = AiJson.getString(schema, "type", "");
         switch (type) {
            case "integer":
            case "number":
               if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                  try {
                     value = new JsonPrimitive(new BigDecimal(value.getAsString().trim()));
                  } catch (RuntimeException notNumeric) {
                     return value;
                  }
               }

               if (type.equals("integer") && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
                  BigDecimal n = value.getAsBigDecimal().stripTrailingZeros();
                  if (n.scale() <= 0) {
                     return new JsonPrimitive(n.toBigIntegerExact());
                  }

                  return new JsonPrimitive(n.setScale(0, RoundingMode.FLOOR).toBigIntegerExact());
               }

               return value;
            case "boolean":
               if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                  String v = value.getAsString().trim().toLowerCase(Locale.ROOT);
                  if (v.equals("true") || v.equals("false")) {
                     return new JsonPrimitive(v.equals("true"));
                  }
               }

               return value;
            case "string":
               return value.isJsonPrimitive() && !value.getAsJsonPrimitive().isString() ? new JsonPrimitive(value.getAsString()) : value;
            case "array":
               if (value.isJsonNull()) {
                  return value;
               } else {
                  JsonArray array = value.isJsonArray() ? value.getAsJsonArray() : new JsonArray();
                  if (!value.isJsonArray()) {
                     array.add(value);
                  }

                  if (schema.has("items") && schema.get("items").isJsonObject()) {
                     JsonArray fixed = new JsonArray();

                     for (JsonElement item : array) {
                        fixed.add(init(schema.getAsJsonObject("items"), item, path + "[]", dropUnknown, ignored, depth + 1));
                     }

                     return fixed;
                  }

                  return array;
               }
            case "object":
               if (!value.isJsonObject()) {
                  return value;
               }

               JsonObject object = value.getAsJsonObject();
               JsonObject properties = schema.has("properties") && schema.get("properties").isJsonObject()
                  ? schema.getAsJsonObject("properties")
                  : new JsonObject();
               HashSet<String> required = new HashSet<>();
               if (schema.has("required") && schema.get("required").isJsonArray()) {
                  for (JsonElement key : schema.getAsJsonArray("required")) {
                     required.add(key.getAsString());
                  }
               }

               boolean closed = schema.has("additionalProperties")
                  && schema.get("additionalProperties").isJsonPrimitive()
                  && !schema.get("additionalProperties").getAsBoolean();

               for (String key : new ArrayList<>(object.keySet())) {
                  JsonObject property = properties.has(key) && properties.get(key).isJsonObject() ? properties.getAsJsonObject(key) : null;
                  boolean nullable = auditIsNullable(property);
                  if (object.get(key).isJsonNull() && !required.contains(key) && !nullable) {
                     object.remove(key);
                  } else if (property != null) {
                     object.add(key, init(property, object.get(key), path + "." + key, dropUnknown, ignored, depth + 1));
                  } else if (closed && dropUnknown) {
                     object.remove(key);
                     ignored.add((path + "." + key).substring(1));
                  }
               }

               return object;
            default:
               return value;
         }
      } else {
         return value;
      }
   }

   static void init(JsonObject schema, JsonElement value) {
      init(schema, value, "arguments", 0);
   }

   private static String init(JsonElement value) {
      String text = String.valueOf(value);
      return text.length() <= 60 ? text : text.substring(0, 57) + "...";
   }

   private static void init(JsonObject schema, JsonElement value, String path, int depth) {
      if (depth > 24) {
         throw new IllegalArgumentException(path + ": structure too deep");
      }

      if (schema.has("$ref")) {
         throw new IllegalArgumentException(path + ": contract requires an inline execution schema");
      }

      if (schema.has("anyOf") && schema.get("anyOf").isJsonArray()) {
         for (JsonElement alternative : schema.getAsJsonArray("anyOf")) {
            if (!alternative.isJsonObject()) {
               continue;
            }
            try {
               init(alternative.getAsJsonObject(), value, path, depth + 1);
               return;
            } catch (IllegalArgumentException var10) {
            }
         }

         throw new IllegalArgumentException(path + ": value does not match any supported shape: " + init(value));
      } else {
         String type = AiJson.getString(schema, "type", "");
         boolean var10000;
         switch (type) {
            case "object":
               var10000 = value.isJsonObject();
               break;
            case "array":
               var10000 = value.isJsonArray();
               break;
            case "string":
               var10000 = value.isJsonPrimitive() && value.getAsJsonPrimitive().isString();
               break;
            case "boolean":
               var10000 = value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean();
               break;
            case "number":
            case "integer":
               var10000 = value.isJsonPrimitive()
                  && value.getAsJsonPrimitive().isNumber()
                  && Double.isFinite(value.getAsDouble())
                  && (!type.equals("integer") || value.getAsBigDecimal().stripTrailingZeros().scale() <= 0);
               break;
            case "null":
               var10000 = value.isJsonNull();
               break;
            case "":
               var10000 = true;
               break;
            default:
               throw new IllegalArgumentException(path + ": unsupported schema type " + type);
         }

         boolean valid = var10000;
         if (!valid) {
            throw new IllegalArgumentException(path + ": expected " + type + ", got " + init(value));
         }

         if (schema.has("enum") && !schema.getAsJsonArray("enum").contains(value)) {
            throw new IllegalArgumentException(path + ": " + init(value) + " is not one of " + schema.getAsJsonArray("enum"));
         }

         if (value.isJsonObject()) {
            JsonObject object = value.getAsJsonObject();
            JsonObject properties = schema.has("properties") ? schema.getAsJsonObject("properties") : new JsonObject();
            if (schema.has("required")) {
               for (JsonElement key : schema.getAsJsonArray("required")) {
                  if (!object.has(key.getAsString())) {
                     throw new IllegalArgumentException(path + ": missing required field " + key.getAsString());
                  }
               }
            }

            for (Entry<String, JsonElement> entry : object.entrySet()) {
               if (properties.has(entry.getKey())) {
                  init(properties.getAsJsonObject(entry.getKey()), entry.getValue(), path + "." + entry.getKey(), depth + 1);
               } else {
                  if (schema.has("additionalProperties")
                     && schema.get("additionalProperties").isJsonPrimitive()
                     && !schema.get("additionalProperties").getAsBoolean()) {
                     throw new IllegalArgumentException(
                        path + ": unknown field " + entry.getKey() + " (fields: " + String.join(", ", properties.keySet()) + ")"
                     );
                  }

                  if (schema.has("additionalProperties") && schema.get("additionalProperties").isJsonObject()) {
                     init(schema.getAsJsonObject("additionalProperties"), entry.getValue(), path + "." + entry.getKey(), depth + 1);
                  }
               }
            }
         }

         if (value.isJsonArray()) {
            JsonArray array = value.getAsJsonArray();
            if (array.size() > 2048 || schema.has("maxItems") && array.size() > schema.get("maxItems").getAsInt()) {
               throw new IllegalArgumentException(path + ": too many items");
            }

            if (schema.has("minItems") && array.size() < schema.get("minItems").getAsInt()) {
               throw new IllegalArgumentException(path + ": too few items");
            }

            if (schema.has("items")) {
               for (JsonElement item : array) {
                  init(schema.getAsJsonObject("items"), item, path + "[]", depth + 1);
               }
            }
         }

         if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
            double number = value.getAsDouble();
            if (schema.has("minimum") && number < schema.get("minimum").getAsDouble() || schema.has("maximum") && number > schema.get("maximum").getAsDouble()) {
               throw new IllegalArgumentException(path + ": out of range");
            }
         }

         if (value.isJsonPrimitive()
            && value.getAsJsonPrimitive().isString()
            && schema.has("maxLength")
            && value.getAsString().length() > schema.get("maxLength").getAsInt()) {
            throw new IllegalArgumentException(path + ": text too long");
         }
      }
   }
}
