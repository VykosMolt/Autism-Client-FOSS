package autismclient.util.macro;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class MacroVariableStore {
   private final ConcurrentHashMap<String, MacroVarValue> init = new ConcurrentHashMap<>();

   public void clear() {
      this.init.clear();
   }

   public void init(String name, MacroVarValue value) {
      String key = load(name);
      if (!key.isEmpty() && value != null) {
         this.init.put(key, value);
      }
   }

   public void putAll(Map<String, MacroVarValue> additions) {
      if (additions != null) {
         additions.forEach(this::init);
      }
   }

   public Optional<MacroVarValue> lookup(String name) {
      String key = init_Str_Str(name);
      if (key.isEmpty()) {
         return Optional.empty();
      } else {
         int dot = key.indexOf(46);
         String root = dot < 0 ? key : key.substring(0, dot);
         MacroVarValue value = this.init.get(root);
         if (value == null) {
            return Optional.empty();
         } else {
            return dot < 0 ? Optional.of(value) : value.resolvePath(key.substring(dot + 1));
         }
      }
   }

   public void remove(String name) {
      String key = init_Str_Str(name);
      int dot = key.indexOf(46);
      this.init.remove(dot < 0 ? key : key.substring(0, dot));
   }

   public Map<String, MacroVarValue> snapshot() {
      return Map.copyOf(new LinkedHashMap<>(this.init));
   }

   public static String init_Str_Str(String name) {
      if (name == null) {
         return "";
      }

      String cleaned = name.trim().replace("{", "").replace("}", "").toLowerCase(Locale.ROOT);
      return cleaned.matches("[a-z_][a-z0-9_.-]{0,63}") ? cleaned : "";
   }

   public static String load(String name) {
      String cleaned = init_Str_Str(name);
      return cleaned.contains(".") ? "" : cleaned;
   }
}
