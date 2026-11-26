package autismclient.util.macro;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class l {
   private final l.Inner_DataUtil init_l_DataUtil;
   private final String init_Str;
   private final Map<String, l> init_Map;

   private l(l.Inner_DataUtil kind, String value, Map<String, l> properties) {
      this.init_l_DataUtil = kind == null ? l.Inner_DataUtil.TEXT : kind;
      this.init_Str = init_Str_Str(value);
      this.init_Map = properties != null && !properties.isEmpty() ? Collections.unmodifiableMap(new LinkedHashMap<>(properties)) : Map.of();
   }

   public static l init_l_Obj(Object value) {
      return new l(l.Inner_DataUtil.TEXT, value == null ? "" : String.valueOf(value), Map.of());
   }

   public static l init_l_Number(Number value) {
      String rendered = value == null ? "0" : new BigDecimal(value.toString()).stripTrailingZeros().toPlainString();
      return new l(l.Inner_DataUtil.NUMBER, rendered, Map.of());
   }

   public static l init_l_Str(String value) {
      return new l(l.Inner_DataUtil.IDENTIFIER, value, Map.of());
   }

   public static l init_l_I(int value) {
      return new l(l.Inner_DataUtil.SLOT, Integer.toString(value), Map.of());
   }

   public static l init(l.Inner_DataUtil kind, String value, Map<String, l> properties) {
      return new l(kind, value, properties);
   }

   public l.Inner_DataUtil init_l_DataUtil() {
      return this.init_l_DataUtil;
   }

   public String init_Str() {
      return this.init_Str;
   }

   public Map<String, l> init_Map() {
      return this.init_Map;
   }

   public Optional<l> init_Optional_Str(String path) {
      if (path != null && !path.isBlank()) {
         l current = this;

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

   public l init(String name, l property) {
      if (name != null && !name.isBlank() && property != null) {
         Map<String, l> copy = new LinkedHashMap<>(this.init_Map);
         copy.put(name.trim().toLowerCase(Locale.ROOT), property);
         return new l(this.init_l_DataUtil, this.init_Str, copy);
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
