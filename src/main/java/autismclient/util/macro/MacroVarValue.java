package autismclient.util.macro;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class MacroVarValue {
   private final MacroVarValue.ValueType type;
   private final String text;
   private final Map<String, MacroVarValue> properties;

   private MacroVarValue(MacroVarValue.ValueType kind, String value, Map<String, MacroVarValue> properties) {
      this.type = kind == null ? MacroVarValue.ValueType.TEXT : kind;
      this.text = sanitizeValue(value);
      this.properties = properties != null && !properties.isEmpty() ? Collections.unmodifiableMap(new LinkedHashMap<>(properties)) : Map.of();
   }

   public static MacroVarValue ofText(Object value) {
      return new MacroVarValue(MacroVarValue.ValueType.TEXT, value == null ? "" : String.valueOf(value), Map.of());
   }

   public static MacroVarValue ofNumber(Number value) {
      String rendered = value == null ? "0" : new BigDecimal(value.toString()).stripTrailingZeros().toPlainString();
      return new MacroVarValue(MacroVarValue.ValueType.NUMBER, rendered, Map.of());
   }

   public static MacroVarValue ofIdentifier(String value) {
      return new MacroVarValue(MacroVarValue.ValueType.IDENTIFIER, value, Map.of());
   }

   public static MacroVarValue ofSlot(int value) {
      return new MacroVarValue(MacroVarValue.ValueType.SLOT, Integer.toString(value), Map.of());
   }

   public static MacroVarValue of(MacroVarValue.ValueType kind, String value, Map<String, MacroVarValue> properties) {
      return new MacroVarValue(kind, value, properties);
   }

   public MacroVarValue.ValueType getType() {
      return this.type;
   }

   public String asString() {
      return this.text;
   }

   public Map<String, MacroVarValue> getProperties() {
      return this.properties;
   }

   public Optional<MacroVarValue> resolvePath(String path) {
      if (path != null && !path.isBlank()) {
         MacroVarValue current = this;

         for (String segment : path.split("\\.")) {
            if (!segment.isBlank()) {
               current = current.properties.get(segment.toLowerCase(Locale.ROOT));
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

   public MacroVarValue withProperty(String name, MacroVarValue property) {
      if (name != null && !name.isBlank() && property != null) {
         Map<String, MacroVarValue> copy = new LinkedHashMap<>(this.properties);
         copy.put(name.trim().toLowerCase(Locale.ROOT), property);
         return new MacroVarValue(this.type, this.text, copy);
      } else {
         return this;
      }
   }

   private static String sanitizeValue(String raw) {
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

   public enum ValueType {
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
