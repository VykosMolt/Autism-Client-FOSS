package autismclient.util.macro;

import autismclient.gui.macro.editor.FieldDef;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public final class DynamicBindingManager {
   public static final String init_Str = "_dynamicBindings";
   private static final Map<MacroAction, Map<String, String>> init_Map = Collections.synchronizedMap(new WeakHashMap<>());

   private DynamicBindingManager() {
   }

   public static void init(MacroAction action, CompoundTag actionTag) {
      if (action != null) {
         Map<String, String> values = new LinkedHashMap<>();
         if (actionTag != null && actionTag.get("_dynamicBindings") instanceof CompoundTag bindings) {
            for (String key : bindings.keySet()) {
               String value = bindings.getStringOr(key, "");
               if (!value.isBlank()) {
                  values.put(key, value);
               }
            }
         }

         if (values.isEmpty()) {
            init_Map.remove(action);
         } else {
            init_Map.put(action, values);
         }
      }
   }

   public static void load(MacroAction action, CompoundTag actionTag) {
      if (action != null && actionTag != null) {
         synchronized (init_Map) {
            Map<String, String> values = init_Map.get(action);
            if (values != null && !values.isEmpty()) {
               CompoundTag bindings = new CompoundTag();
               values.forEach(bindings::putString);
               actionTag.put("_dynamicBindings", bindings);
            } else {
               actionTag.remove("_dynamicBindings");
            }
         }
      }
   }

   public static String init(MacroAction action, String key) {
      synchronized (init_Map) {
         Map<String, String> values = init_Map.get(action);
         return values == null ? "" : values.getOrDefault(key, "");
      }
   }

   public static void init_V(MacroAction action, String key, String template) {
      if (action != null && key != null && !key.isBlank()) {
         String value = template == null ? "" : template.trim();
         synchronized (init_Map) {
            Map<String, String> values = init_Map.computeIfAbsent(action, ignored -> new LinkedHashMap<>());
            if (value.isBlank()) {
               values.remove(key);
            } else {
               values.put(key, value);
            }

            if (values.isEmpty()) {
               init_Map.remove(action);
            }
         }
      }
   }

   public static Map<String, String> init(MacroAction action) {
      synchronized (init_Map) {
         Map<String, String> values = init_Map.get(action);
         return values == null ? Map.of() : Map.copyOf(values);
      }
   }

   public static boolean init(MacroAction action, Minecraft mc) {
      if (action == null) {
         return true;
      }

      for (Entry<String, String> binding : init(action).entrySet()) {
         MacroTemplateResolver.Inner_CachePool resolution = MacroVariableContext.init_j_CachePool_Str_Minecraft(binding.getValue(), mc);
         if (!resolution.init_Z()) {
            return false;
         }

         try {
            String value = resolution.init_Str().trim();
            if (action instanceof ToolsAction tools && binding.getKey().startsWith("toolArg.")) {
               if (!tools.init_Z_Str_Str_2(binding.getKey(), value)) {
                  return false;
               }
            } else {
               Field field;
               try {
                  field = action.getClass().getField(binding.getKey());
               } catch (NoSuchFieldException missingField) {
                  if (init_Z(action, binding.getKey(), value)) {
                     continue;
                  }

                  return false;
               }

               Class<?> type = field.getType();
               if (type == int.class || type == Integer.class) {
                  field.set(action, clampToEditorRange(action, binding.getKey(), Integer.parseInt(value)));
               } else if (type == long.class || type == Long.class) {
                  field.set(action, clampToEditorRange(action, binding.getKey(), Long.parseLong(value)));
               } else if (type != double.class && type != Double.class) {
                  if (type != float.class && type != Float.class) {
                     if (type == String.class) {
                        field.set(action, value);
                     } else {
                        if (!type.isEnum()) {
                           return false;
                        }

                        Object selected = null;
                        Object[] var9 = type.getEnumConstants();
                        int var10 = var9.length;
                        int var11 = 0;

                        while (true) {
                           if (var11 < var10) {
                              Object constant = var9[var11];
                              if (!((Enum)constant).name().equalsIgnoreCase(value)) {
                                 var11++;
                                 continue;
                              }

                              selected = constant;
                           }

                           if (selected == null) {
                              return false;
                           }

                           field.set(action, selected);
                           break;
                        }
                     }
                  } else {
                     field.set(action, clampToEditorRange(action, binding.getKey(), Float.parseFloat(value)));
                  }
               } else {
                  field.set(action, clampToEditorRange(action, binding.getKey(), Double.parseDouble(value)));
               }
            }
         } catch (ReflectiveOperationException | NumberFormatException invalid) {
            return false;
         }
      }

      return true;
   }

   // Fields without a declared range keep the parsed value.
   static Object clampToEditorRange(MacroAction action, String key, Object value) {
      for (FieldDef def : autismclient.gui.macro.editor.ActionFieldRegistry.init(action).fields()) {
         if (!key.equals(def.key())) {
            continue;
         }

         boolean intRange = def.min() != Integer.MIN_VALUE || def.max() != Integer.MAX_VALUE;
         if (value instanceof Integer v && intRange) {
            return Math.max(def.min(), Math.min(def.max(), v));
         } else if (value instanceof Long v && intRange) {
            return Math.max((long)def.min(), Math.min((long)def.max(), v));
         } else if (value instanceof Double v) {
            return Math.max(def.decMin(), Math.min(def.decMax(), v));
         } else if (value instanceof Float v) {
            return (float)Math.max(def.decMin(), Math.min(def.decMax(), v));
         }

         return value;
      }

      return value;
   }

   private static boolean init_Z(MacroAction action, String key, String rawValue) {
      String lower = key == null ? "" : key.toLowerCase(Locale.ROOT);
      int axis = lower.endsWith("x") ? 0 : (lower.endsWith("y") ? 1 : (lower.endsWith("z") ? 2 : -1));
      if (axis < 0) {
         return false;
      }

      int value;
      try {
         value = (int)Math.floor(Double.parseDouble(rawValue));
      } catch (NumberFormatException ignored) {
         return false;
      }

      String preferred = lower.startsWith("container") ? "containerPos" : "blockPos";
      Field positionField = null;

      try {
         positionField = action.getClass().getField(preferred);
      } catch (NoSuchFieldException ignored) {
         for (Field candidate : action.getClass().getFields()) {
            if (candidate.getType() == BlockPos.class) {
               positionField = candidate;
               break;
            }
         }
      }

      if (positionField != null && positionField.getType() == BlockPos.class) {
         try {
            BlockPos current = (BlockPos)positionField.get(action);
            if (current == null) {
               current = BlockPos.ZERO;
            }

            BlockPos var10000;
            switch (axis) {
               case 0:
                  var10000 = new BlockPos(value, current.getY(), current.getZ());
                  break;
               case 1:
                  var10000 = new BlockPos(current.getX(), value, current.getZ());
                  break;
               default:
                  var10000 = new BlockPos(current.getX(), current.getY(), value);
            }

            BlockPos next = var10000;
            positionField.set(action, next);
            return true;
         } catch (ReflectiveOperationException ignored) {
            return false;
         }
      } else {
         return false;
      }
   }

   public static int init_I_MacroAction(MacroAction action, String key, int fallback, Minecraft mc) {
      String template = init(action, key);
      if (template.isBlank()) {
         return fallback;
      }

      MacroTemplateResolver.Inner_CachePool resolution = MacroVariableContext.init_j_CachePool_Str_Minecraft(template, mc);
      if (!resolution.init_Z()) {
         throw new DynamicBindingManager.Inner_DataUtil();
      }

      try {
         return Integer.parseInt(resolution.init_Str().trim());
      } catch (NumberFormatException ignored) {
         long compact = PayAction.init_J_Str(resolution.init_Str());
         if (compact <= 2147483647L && compact >= -2147483648L) {
            return (int)compact;
         } else {
            throw new DynamicBindingManager.Inner_DataUtil();
         }
      }
   }

   public static double init_D_MacroAction(MacroAction action, String key, double fallback, Minecraft mc) {
      String template = init(action, key);
      if (template.isBlank()) {
         return fallback;
      }

      MacroTemplateResolver.Inner_CachePool resolution = MacroVariableContext.init_j_CachePool_Str_Minecraft(template, mc);
      if (!resolution.init_Z()) {
         throw new DynamicBindingManager.Inner_DataUtil();
      }

      try {
         return Double.parseDouble(resolution.init_Str().trim());
      } catch (NumberFormatException ignored) {
         throw new DynamicBindingManager.Inner_DataUtil();
      }
   }

   public static final class Inner_DataUtil extends RuntimeException {
      private Inner_DataUtil() {
         super("Missing or invalid dynamic value", null, false, false);
      }
   }
}
