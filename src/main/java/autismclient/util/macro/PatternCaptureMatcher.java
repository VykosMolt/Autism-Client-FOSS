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

public final class PatternCaptureMatcher {
   private static final Pattern TOKEN_PATTERN = Pattern.compile("\\{([A-Za-z_][A-Za-z0-9_-]{0,63})}");
   private static final Pattern NAMED_GROUP_PATTERN = Pattern.compile("\\(\\?<([A-Za-z_][A-Za-z0-9_]{0,63})>");
   private static final Pattern WHITESPACE = Pattern.compile("\\s+");
   private static final char init_C = '\ue000';
   private static final char load_C = '\ue001';
   private static final ConcurrentHashMap<String, Pattern> patternCache = new ConcurrentHashMap<>();

   public static Pattern compileCached(String regex) {
      Pattern cached = patternCache.get(regex);
      if (cached != null) {
         return cached;
      }

      if (patternCache.size() > 256) {
         patternCache.clear();
      }

      Pattern fresh = Pattern.compile(regex);
      patternCache.put(regex, fresh);
      return fresh;
   }

   private PatternCaptureMatcher() {
   }

   public static Optional<PatternCaptureMatcher.CaptureResult> match(PatternCaptureMatcher.MatchMode mode, String pattern, String candidate) {
      if (candidate == null) {
         return Optional.empty();
      }

      PatternCaptureMatcher.MatchMode effective = mode == null ? PatternCaptureMatcher.MatchMode.MATCH : mode;
      Optional var10000;
      switch (effective) {
         case MATCH:
            var10000 = Optional.empty();
            break;
         case CAPTURE:
            var10000 = matchTemplate(pattern, candidate);
            break;
         case REGEX:
            var10000 = matchRegex(pattern, candidate);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public static Set<String> captureNames(PatternCaptureMatcher.MatchMode mode, String pattern) {
      Pattern parser = mode == PatternCaptureMatcher.MatchMode.REGEX ? NAMED_GROUP_PATTERN : TOKEN_PATTERN;
      String source = pattern == null ? "" : pattern;
      if (mode != PatternCaptureMatcher.MatchMode.REGEX) {
         source = escapeBraces(source);
      }

      Matcher matcher = parser.matcher(source);
      Set<String> names = new LinkedHashSet<>();

      while (matcher.find()) {
         names.add(matcher.group(1).toLowerCase(Locale.ROOT));
      }

      return names;
   }

   private static Optional<PatternCaptureMatcher.CaptureResult> matchTemplate(String sourcePattern, String sourceCandidate) {
      String capturePattern = escapeBraces(sourcePattern == null ? "" : sourcePattern.trim());
      String candidate = normalizeWhitespace(sourceCandidate);
      if (capturePattern.isEmpty()) {
         return Optional.of(new PatternCaptureMatcher.CaptureResult(candidate, Map.of()));
      }

      Matcher tokenMatcher = TOKEN_PATTERN.matcher(capturePattern);
      StringBuilder regex = new StringBuilder("(?iu)");
      List<String> names = new ArrayList<>();

      int cursor;
      for (cursor = 0; tokenMatcher.find(); cursor = tokenMatcher.end()) {
         if (tokenMatcher.start() == cursor && cursor > 0) {
            return Optional.empty();
         }

         appendLiteral(regex, capturePattern.substring(cursor, tokenMatcher.start()));
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

      appendLiteral(regex, capturePattern.substring(cursor));

      try {
         Matcher matcher = compileCached(regex.toString()).matcher(candidate);
         if (!matcher.find()) {
            return Optional.empty();
         }

         Map<String, MacroVarValue> values = new LinkedHashMap<>();

         for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            String value = matcher.group("g" + i);
            if (value == null || value.isBlank()) {
               return Optional.empty();
            }

            values.put(name, MacroVarValue.ofText(value));
         }

         return Optional.of(new PatternCaptureMatcher.CaptureResult(matcher.group(), values));
      } catch (IllegalArgumentException ignored) {
         return Optional.empty();
      }
   }

   private static Optional<PatternCaptureMatcher.CaptureResult> matchRegex(String sourcePattern, String sourceCandidate) {
      String regex = sourcePattern == null ? "" : sourcePattern;
      if (regex.isBlank()) {
         return Optional.of(new PatternCaptureMatcher.CaptureResult(sourceCandidate, Map.of()));
      }

      List<String> names = new ArrayList<>(captureNames(PatternCaptureMatcher.MatchMode.REGEX, regex));

      try {
         Matcher matcher = compileCached(regex).matcher(sourceCandidate);
         if (!matcher.find()) {
            return Optional.empty();
         }

         Map<String, MacroVarValue> values = new LinkedHashMap<>();

         for (String name : names) {
            String value = matcher.group(name);
            if (value != null) {
               values.put(name, MacroVarValue.ofText(value));
            }
         }

         return Optional.of(new PatternCaptureMatcher.CaptureResult(matcher.group(), values));
      } catch (IllegalArgumentException ignored) {
         return Optional.empty();
      }
   }

   private static void appendLiteral(StringBuilder regex, String literal) {
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

   private static String escapeBraces(String value) {
      return value != null && !value.isEmpty() ? value.replace("{{", String.valueOf('\ue000')).replace("}}", String.valueOf('\ue001')) : "";
   }

   private static String normalizeWhitespace(String value) {
      return value == null ? "" : WHITESPACE.matcher(value.replace('\n', ' ').replace('\r', ' ')).replaceAll(" ").trim();
   }

   public enum MatchMode {
      MATCH,
      CAPTURE,
      REGEX;
   }

   public record CaptureResult(String matchedText, Map<String, MacroVarValue> variables) {
      public CaptureResult {
         matchedText = matchedText == null ? "" : matchedText;
         variables = variables == null ? Map.of() : Map.copyOf(variables);
      }
   }
}
