package autismclient.util.macro;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UtilCore {
   private static final Pattern init_Pattern = Pattern.compile("\\{([A-Za-z_][A-Za-z0-9_-]{0,63})}");
   private static final Pattern load_Pattern = Pattern.compile("\\(\\?<([A-Za-z_][A-Za-z0-9_]{0,63})>");
   private static final Pattern read = Pattern.compile("\\s+");
   private static final char init_C = '\ue000';
   private static final char load_C = '\ue001';
   private static final ConcurrentHashMap<String, Pattern> init_ConcurrentHashMap = new ConcurrentHashMap<>();

   public static Pattern init_Pattern(String regex) {
      Pattern cached = init_ConcurrentHashMap.get(regex);
      if (cached != null) {
         return cached;
      }

      if (init_ConcurrentHashMap.size() > 256) {
         init_ConcurrentHashMap.clear();
      }

      Pattern fresh = Pattern.compile(regex);
      init_ConcurrentHashMap.put(regex, fresh);
      return fresh;
   }

   private UtilCore() {
   }

   public static Optional<UtilCore.Inner_StreamUtil> init(UtilCore.Inner_DataUtil mode, String pattern, String candidate) {
      if (candidate == null) {
         return Optional.empty();
      }

      UtilCore.Inner_DataUtil effective = mode == null ? UtilCore.Inner_DataUtil.init : mode;
      Optional var10000;
      switch (effective) {
         case init:
            var10000 = Optional.empty();
            break;
         case load:
            var10000 = init(pattern, candidate);
            break;
         case read:
            var10000 = load(pattern, candidate);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public static Set<String> init(UtilCore.Inner_DataUtil mode, String pattern) {
      Pattern parser = mode == UtilCore.Inner_DataUtil.read ? load_Pattern : init_Pattern;
      String source = pattern == null ? "" : pattern;
      if (mode != UtilCore.Inner_DataUtil.read) {
         source = init_Str(source);
      }

      Matcher matcher = parser.matcher(source);
      Set<String> names = new LinkedHashSet<>();

      while (matcher.find()) {
         names.add(matcher.group(1).toLowerCase(Locale.ROOT));
      }

      return names;
   }

   private static Optional<UtilCore.Inner_StreamUtil> init(String sourcePattern, String sourceCandidate) {
      String capturePattern = init_Str(sourcePattern == null ? "" : sourcePattern.trim());
      String candidate = load(sourceCandidate);
      if (capturePattern.isEmpty()) {
         return Optional.of(new UtilCore.Inner_StreamUtil(candidate, Map.of()));
      }

      Matcher tokenMatcher = init_Pattern.matcher(capturePattern);
      StringBuilder regex = new StringBuilder("(?iu)");
      List<String> names = new ArrayList<>();

      int cursor;
      for (cursor = 0; tokenMatcher.find(); cursor = tokenMatcher.end()) {
         if (tokenMatcher.start() == cursor && cursor > 0) {
            return Optional.empty();
         }

         init(regex, capturePattern.substring(cursor, tokenMatcher.start()));
         String name = tokenMatcher.group(1).toLowerCase(Locale.ROOT);
         if (names.contains(name)) {
            return Optional.empty();
         }

         names.add(name);
         boolean capturesRest = capturePattern.substring(tokenMatcher.end()).trim().isEmpty();
         regex.append("(?<g").append(names.size() - 1).append(capturesRest ? ">.+)" : ">.+?)");
      }

      if (names.isEmpty()) {
         return Optional.empty();
      }

      init(regex, capturePattern.substring(cursor));

      try {
         Matcher matcher = init_Pattern(regex.toString()).matcher(candidate);
         if (!matcher.find()) {
            return Optional.empty();
         }

         Map<String, l> values = new LinkedHashMap<>();

         for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            String value = matcher.group("g" + i);
            if (value == null || value.isBlank()) {
               return Optional.empty();
            }

            values.put(name, l.init_l_Obj(value));
         }

         return Optional.of(new UtilCore.Inner_StreamUtil(matcher.group(), values));
      } catch (IllegalArgumentException ignored) {
         return Optional.empty();
      }
   }

   private static Optional<UtilCore.Inner_StreamUtil> load(String sourcePattern, String sourceCandidate) {
      String regex = sourcePattern == null ? "" : sourcePattern;
      if (regex.isBlank()) {
         return Optional.of(new UtilCore.Inner_StreamUtil(sourceCandidate, Map.of()));
      }

      List<String> names = new ArrayList<>(init(UtilCore.Inner_DataUtil.read, regex));

      try {
         Matcher matcher = init_Pattern(regex).matcher(sourceCandidate);
         if (!matcher.find()) {
            return Optional.empty();
         }

         Map<String, l> values = new LinkedHashMap<>();

         for (String name : names) {
            String value = matcher.group(name);
            if (value != null) {
               values.put(name, l.init_l_Obj(value));
            }
         }

         return Optional.of(new UtilCore.Inner_StreamUtil(matcher.group(), values));
      } catch (IllegalArgumentException ignored) {
         return Optional.empty();
      }
   }

   private static void init(StringBuilder regex, String literal) {
      if (literal != null && !literal.isEmpty()) {
         literal = literal.replace('\ue000', '{').replace('\ue001', '}');
         String[] pieces = literal.trim().split("\\s+");
         if (literal.startsWith(" ") || literal.startsWith("\t")) {
            regex.append("\\s+");
         }

         for (int i = 0; i < pieces.length; i++) {
            if (!pieces[i].isEmpty()) {
               if (i > 0) {
                  regex.append("\\s+");
               }

               regex.append(Pattern.quote(pieces[i]));
            }
         }

         if (literal.endsWith(" ") || literal.endsWith("\t")) {
            regex.append("\\s+");
         }
      }
   }

   private static String init_Str(String value) {
      return value != null && !value.isEmpty() ? value.replace("{{", String.valueOf('\ue000')).replace("}}", String.valueOf('\ue001')) : "";
   }

   private static String load(String value) {
      return value == null ? "" : read.matcher(value.replace('\n', ' ').replace('\r', ' ')).replaceAll(" ").trim();
   }

   public enum Inner_DataUtil {
      init,
      load,
      read;
   }

   public record Inner_StreamUtil(String init_Str, Map<String, l> init_Map) {
      public Inner_StreamUtil {
         init_Str = init_Str == null ? "" : init_Str;
         init_Map = init_Map == null ? Map.of() : Map.copyOf(init_Map);
      }
   }
}
