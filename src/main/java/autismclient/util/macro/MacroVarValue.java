package autismclient.util.macro;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class MacroVarValue {
   private final MacroVarValue.Inner_DataUtil init_l_DataUtil;
   private final String init_Str;
   private final Map<String, MacroVarValue> init_Map;

   private MacroVarValue(MacroVarValue.Inner_DataUtil kind, String value, Map<String, MacroVarValue> properties) {
      this.init_l_DataUtil = kind == null ? MacroVarValue.Inner_DataUtil.TEXT : kind;
      this.init_Str = init_Str_Str(value);
      this.init_Map = properties != null && !properties.isEmpty() ? Collections.unmodifiableMap(new LinkedHashMap<>(properties)) : Map.of();
   }

   public static MacroVarValue init_l_Obj(Object value) {
      return new MacroVarValue(MacroVarValue.Inner_DataUtil.TEXT, value == null ? "" : String.valueOf(value), Map.of());
   }

   public static MacroVarValue init_l_Number(Number value) {
      String rendered = value == null ? "0" : new BigDecimal(value.toString()).stripTrailingZeros().toPlainString();
      return new MacroVarValue(MacroVarValue.Inner_DataUtil.NUMBER, rendered, Map.of());
   }

   public static MacroVarValue init_l_Str(String value) {
      return new MacroVarValue(MacroVarValue.Inner_DataUtil.IDENTIFIER, value, Map.of());
   }

   public static MacroVarValue init_l_I(int value) {
      return new MacroVarValue(MacroVarValue.Inner_DataUtil.SLOT, Integer.toString(value), Map.of());
   }

   public static MacroVarValue init(MacroVarValue.Inner_DataUtil kind, String value, Map<String, MacroVarValue> properties) {
      return new MacroVarValue(kind, value, properties);
   }

   public MacroVarValue.Inner_DataUtil init_l_DataUtil() {
      return this.init_l_DataUtil;
   }

   public String init_Str() {
      return this.init_Str;
   }

   public Map<String, MacroVarValue> init_Map() {
      return this.init_Map;
   }

   public Optional<MacroVarValue> init_Optional_Str(String path) {
      if (path != null && !path.isBlank()) {
         MacroVarValue current = this;

         for (String segment : path.split("\\.")) {
            if (!segment.isBlank()) {
               current = current.init_Map.get(segment.toLowerCase(Locale.ROOT));
               if (current == null) {
                  return Optional.empty();
               }
            }
         }

         return Optional.of(current);
      } else {
         return Optional.of(this);
      }
   }

   public MacroVarValue init(String name, MacroVarValue property) {
      if (name != null && !name.isBlank() && property != null) {
         Map<String, MacroVarValue> copy = new LinkedHashMap<>(this.init_Map);
         copy.put(name.trim().toLowerCase(Locale.ROOT), property);
         return new MacroVarValue(this.init_l_DataUtil, this.init_Str, copy);
      } else {
         return this;
      }
   }

   private static String init_Str_Str(String raw) {
      if (raw != null && !raw.isEmpty()) {
         StringBuilder out = new StringBuilder(Math.min(raw.length(), 4096));
         int offset = 0;

         while (offset < raw.length() && out.length() < 4096) {
            int cp = raw.codePointAt(offset);
            offset += Character.charCount(cp);
            if (cp != 10 && cp != 13 && !Character.isISOControl(cp)) {
               out.appendCodePoint(cp);
            } else {
               out.append(' ');
            }
         }

         return out.toString().trim();
      } else {
         return "";
      }
   }

   public enum Inner_DataUtil {
      TEXT,
      NUMBER,
      BOOLEAN,
      IDENTIFIER,
      ITEM,
      SLOT,
      ENTITY,
      PACKET,
      POSITION,
      GUI;
   }
}
