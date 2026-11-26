package autismclient.util.macro;

import autismclient.util.AutismSessionState;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public final class MacroTemplateResolver {
   private static final Pattern EXPRESSION_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9_.-]{0,63}(?:\\|[^{}|]+)*");
   private static final Pattern RANGE_PATTERN = Pattern.compile("([-+]?\\d+(?:\\.\\d+)?)\\s*-\\s*([-+]?\\d+(?:\\.\\d+)?)");
   private static final Pattern WEIGHTED_OPTION_PATTERN = Pattern.compile("(.*?)\\s*\\*\\s*(\\d{1,3})\\s*$");
   private static final Pattern COMMA_NUMBER_PATTERN = Pattern.compile("\\d+,\\d+");
   private static final String FORBIDDEN_OPTION_CHARS = "\"':={}\\";
   private static final Pattern NUMBER_TOKEN_PATTERN = Pattern.compile("(?i)(?:[-+]\\p{Sc}?)?(?:\\d(?:[.,_]?\\d)*|[.,]\\d+)(?:[kmbt](?![\\p{L}\\d]))?");
   public static final Set<String> BUILTIN_VARIABLES = Set.of(
      "timestamp",
      "player",
      "user",
      "username",
      "uuid",
      "x",
      "y",
      "z",
      "bx",
      "by",
      "bz",
      "pos",
      "yaw",
      "pitch",
      "rot",
      "facing",
      "dimension",
      "dim",
      "selected_slot",
      "target_slot",
      "server",
      "last_sent",
      "random"
   );

   private MacroTemplateResolver() {
   }

   public static MacroTemplateResolver.ResolveResult resolve(String template, MacroVariableStore context, Minecraft mc) {
      if (template != null && !template.isEmpty()) {
         StringBuilder out = new StringBuilder(template.length());
         List<String> missing = new ArrayList<>();
         int i = 0;

         while (i < template.length()) {
            char ch = template.charAt(i);
            if (ch == '{' && i + 1 < template.length() && template.charAt(i + 1) == '{') {
               out.append('{');
               i += 2;
            } else if (ch == '}' && i + 1 < template.length() && template.charAt(i + 1) == '}') {
               out.append('}');
               i += 2;
            } else if (ch != '{') {
               out.append(ch);
               i++;
            } else {
               int end = template.indexOf(125, i + 1);
               if (end < 0) {
                  out.append(ch);
                  i++;
               } else {
                  String expression = template.substring(i + 1, end).trim();
                  MacroTemplateResolver.TokenResult value;
                  if (EXPRESSION_PATTERN.matcher(expression).matches()) {
                     value = resolveNamedExpression(expression, context, mc);
                  } else {
                     if (!isInlineRandomExpression(expression)) {
                        out.append(ch);
                        i++;
                        continue;
                     }

                     value = resolveInlineRandomExpression(expression);
                  }

                  if (!value.resolved) {
                     missing.add(value.missingName.isEmpty() ? expression : value.missingName);
                  } else {
                     out.append(stripLeadingSlashForLastSent(expression, value.value, out.length() == 0));
                  }

                  i = end + 1;
               }
            }
         }

         return missing.isEmpty() ? MacroTemplateResolver.ResolveResult.ok(out.toString()) : MacroTemplateResolver.ResolveResult.failed(missing, "Missing macro value");
      } else {
         return MacroTemplateResolver.ResolveResult.ok(template == null ? "" : template);
      }
   }

   private static String stripLeadingSlashForLastSent(String expression, String rendered, boolean atStart) {
      if (!atStart && rendered.length() >= 2 && rendered.charAt(0) == '/') {
         String[] parts = expression.split("\\|", -1);
         if (parts.length != 0 && "last_sent".equalsIgnoreCase(parts[0].trim())) {
            for (int i = 1; i < parts.length; i++) {
               if ("raw".equalsIgnoreCase(parts[i].trim())) {
                  return rendered;
               }
            }

            return rendered.substring(1);
         } else {
            return rendered;
         }
      } else {
         return rendered;
      }
   }

   public static boolean containsPlaceholder(String text) {
      if (text != null && !text.isBlank()) {
         for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '{' && (i + 1 >= text.length() || text.charAt(i + 1) != '{')) {
               int end = text.indexOf(125, i + 1);
               if (end > i) {
                  String expression = text.substring(i + 1, end).trim();
                  if (EXPRESSION_PATTERN.matcher(expression).matches() || isInlineRandomExpression(expression)) {
                     return true;
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static MacroTemplateResolver.TokenResult resolveNamedExpression(String expression, MacroVariableStore context, Minecraft mc) {
      String[] parts = expression.split("\\|", -1);
      String name = parts.length == 0 ? "" : parts[0].trim();
      Optional<MacroVarValue> found = context == null ? Optional.empty() : context.init_Optional_Str(name);
      if (found.isEmpty() && "random".equalsIgnoreCase(name)) {
         return resolveRandomExpression(parts);
      }

      if (found.isEmpty()) {
         found = resolveBuiltinVariable(name, mc);
      }

      String fallback = null;

      for (int i = 1; i < parts.length; i++) {
         String formatter = parts[i].trim();
         if (formatter.regionMatches(true, 0, "default:", 0, 8)) {
            fallback = formatter.substring(8);
         }
      }

      if (found.isEmpty()) {
         return fallback == null ? MacroTemplateResolver.TokenResult.missing(name) : MacroTemplateResolver.TokenResult.ok(fallback);
      }

      MacroVarValue value = found.get();
      String rendered = value.asString();

      try {
         for (int i = 1; i < parts.length; i++) {
            String formatter = parts[i].trim();
            if (!formatter.isEmpty() && !formatter.regionMatches(true, 0, "default:", 0, 8)) {
               rendered = applyFormatter(rendered, formatter, value);
            }
         }

         return MacroTemplateResolver.TokenResult.ok(rendered);
      } catch (IllegalArgumentException invalid) {
         return MacroTemplateResolver.TokenResult.missing(name);
      }
   }

   private static String applyFormatter(String text, String formatter, MacroVarValue value) {
      String key = formatter.toLowerCase(Locale.ROOT);
      String var10000;
      switch (key) {
         case "trim":
            var10000 = text.trim();
            break;
         case "lower":
            var10000 = text.toLowerCase(Locale.ROOT);
            break;
         case "upper":
            var10000 = text.toUpperCase(Locale.ROOT);
            break;
         case "number":
            var10000 = parseIntegerNumber(text);
            break;
         case "name":
            var10000 = value.resolvePath("name").map(MacroVarValue::asString).orElse(text);
            break;
         case "id":
            var10000 = value.resolvePath("id").map(MacroVarValue::asString).orElseThrow(() -> new IllegalArgumentException("No id"));
            break;
         case "count":
            var10000 = value.resolvePath("count").map(MacroVarValue::asString).orElseThrow(() -> new IllegalArgumentException("No count"));
            break;
         case "slot":
            var10000 = value.resolvePath("slot").map(MacroVarValue::asString).orElseThrow(() -> new IllegalArgumentException("No slot"));
            break;
         default:
            var10000 = value.resolvePath(key).map(MacroVarValue::asString).orElseThrow(() -> new IllegalArgumentException("Unknown formatter: " + formatter));
      }

      return var10000;
   }

   private static MacroTemplateResolver.TokenResult resolveRandomExpression(String[] parts) {
      int specIndex = -1;
      String rendered = null;

      for (int i = 1; i < parts.length && specIndex < 0; i++) {
         String token = parts[i].trim();
         if (!token.isEmpty() && !token.regionMatches(true, 0, "default:", 0, 8)) {
            if (token.regionMatches(true, 0, "pick:", 0, 5)) {
               List<String> options = new ArrayList<>();

               for (String option : token.substring(5).split(",")) {
                  String trimmed = option.trim();
                  if (!trimmed.isEmpty()) {
                     options.add(trimmed);
                  }
               }

               if (options.isEmpty()) {
                  return MacroTemplateResolver.TokenResult.missing("random");
               }

               rendered = options.get(ThreadLocalRandom.current().nextInt(options.size()));
               specIndex = i;
            } else {
               Matcher range = RANGE_PATTERN.matcher(token);
               if (range.matches()) {
                  rendered = rollNumericRange(range.group(1), range.group(2));
                  specIndex = i;
               }
            }
         }
      }

      if (rendered == null) {
         rendered = Integer.toString(ThreadLocalRandom.current().nextInt(100));
      }

      return applyTrailingFormatters(rendered, parts, specIndex, "random");
   }

   private static MacroTemplateResolver.TokenResult applyTrailingFormatters(String rendered, String[] parts, int specIndex, String missingName) {
      MacroVarValue value = MacroVarValue.ofText(rendered);

      try {
         for (int i = 1; i < parts.length; i++) {
            if (i != specIndex) {
               String formatter = parts[i].trim();
               if (!formatter.isEmpty() && !formatter.regionMatches(true, 0, "default:", 0, 8)) {
                  rendered = applyFormatter(rendered, formatter, value);
               }
            }
         }

         return MacroTemplateResolver.TokenResult.ok(rendered);
      } catch (IllegalArgumentException invalid) {
         return MacroTemplateResolver.TokenResult.missing(missingName);
      }
   }

   private static boolean isInlineRandomExpression(String expression) {
      String spec = extractPrimarySpec(expression);
      if (spec.isEmpty()) {
         return false;
      } else {
         return RANGE_PATTERN.matcher(spec).matches() ? true : parseWeightedOptions(spec) != null;
      }
   }

   private static MacroTemplateResolver.TokenResult resolveInlineRandomExpression(String expression) {
      String[] parts = expression.split("\\|", -1);
      String spec = parts[0].trim();
      Matcher range = RANGE_PATTERN.matcher(spec);
      String rendered = range.matches() ? rollNumericRange(range.group(1), range.group(2)) : pickWeightedOption(parseWeightedOptions(spec));
      return applyTrailingFormatters(rendered, parts, 0, spec);
   }

   private static String extractPrimarySpec(String expression) {
      int pipe = expression.indexOf(124);
      return (pipe < 0 ? expression : expression.substring(0, pipe)).trim();
   }

   private static List<MacroTemplateResolver.PlaceholderSpan> parseWeightedOptions(String spec) {
      if (spec.indexOf(44) < 0) {
         return null;
      }

      for (int i = 0; i < spec.length(); i++) {
         if ("\"':={}\\".indexOf(spec.charAt(i)) >= 0) {
            return null;
         }
      }

      if (COMMA_NUMBER_PATTERN.matcher(spec).matches()) {
         return null;
      }

      List<MacroTemplateResolver.PlaceholderSpan> options = new ArrayList<>();

      for (String entry : spec.split(",", -1)) {
         String text = entry.trim();
         if (!text.isEmpty()) {
            int weight = 1;
            Matcher weighted = WEIGHTED_OPTION_PATTERN.matcher(text);
            if (weighted.matches()) {
               String base = weighted.group(1).trim();
               int parsed = Integer.parseInt(weighted.group(2));
               if (!base.isEmpty() && parsed >= 1 && parsed <= 100) {
                  text = base;
                  weight = parsed;
               }
            }

            Matcher range = RANGE_PATTERN.matcher(text);
            options.add(
               range.matches() ? new MacroTemplateResolver.PlaceholderSpan(text, weight, range.group(1), range.group(2)) : new MacroTemplateResolver.PlaceholderSpan(text, weight, null, null)
            );
         }
      }

      return options.size() >= 2 ? options : null;
   }

   private static String pickWeightedOption(List<MacroTemplateResolver.PlaceholderSpan> options) {
      int total = 0;

      for (MacroTemplateResolver.PlaceholderSpan option : options) {
         total += option.weight();
      }

      int roll = ThreadLocalRandom.current().nextInt(total);

      for (MacroTemplateResolver.PlaceholderSpan option : options) {
         roll -= option.weight();
         if (roll < 0) {
            return option.rangeLo() == null ? option.text() : rollNumericRange(option.rangeLo(), option.rangeHi());
         }
      }

      return options.get(options.size() - 1).text();
   }

   private static String rollNumericRange(String loText, String hiText) {
      BigDecimal lo = new BigDecimal(loText);
      BigDecimal hi = new BigDecimal(hiText);
      if (lo.compareTo(hi) > 0) {
         BigDecimal swap = lo;
         lo = hi;
         hi = swap;
      }

      if (loText.indexOf(46) < 0 && hiText.indexOf(46) < 0) {
         try {
            long min = lo.longValueExact();
            long max = hi.longValueExact();
            long picked;
            if (min == max) {
               picked = min;
            } else if (max != 9223372036854775807L) {
               picked = ThreadLocalRandom.current().nextLong(min, max + 1L);
            } else if (min != -9223372036854775808L) {
               picked = ThreadLocalRandom.current().nextLong(min - 1L, max) + 1L;
            } else {
               picked = ThreadLocalRandom.current().nextLong();
            }

            int padWidth = inferZeroPadWidth(loText, hiText);
            if (padWidth > 0 && picked != -9223372036854775808L) {
               String digits = Long.toString(Math.abs(picked));
               StringBuilder padded = new StringBuilder(padWidth + 1);
               if (picked < 0L) {
                  padded.append('-');
               }

               for (int i = digits.length(); i < padWidth; i++) {
                  padded.append('0');
               }

               return padded.append(digits).toString();
            }

            return Long.toString(picked);
         } catch (ArithmeticException var14) {
         }
      }

      int scale = Math.max(lo.scale(), hi.scale());
      BigDecimal picked = lo.add(hi.subtract(lo).multiply(BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble())));
      return picked.setScale(scale, RoundingMode.HALF_UP).toPlainString();
   }

   private static int inferZeroPadWidth(String loText, String hiText) {
      String loDigits = stripLeadingSign(loText);
      String hiDigits = stripLeadingSign(hiText);
      boolean padded = loDigits.length() >= 2 && loDigits.charAt(0) == '0' || hiDigits.length() >= 2 && hiDigits.charAt(0) == '0';
      return padded ? Math.max(loDigits.length(), hiDigits.length()) : 0;
   }

   private static String stripLeadingSign(String text) {
      return !text.startsWith("-") && !text.startsWith("+") ? text : text.substring(1);
   }

   public static String extractNormalizedNumber(String raw) {
      return extractNormalizedNumber(raw, MacroTemplateResolver.ResolveMode.AUTO);
   }

   public static String extractNormalizedNumber(String raw, MacroTemplateResolver.ResolveMode style) {
      if (raw == null) {
         throw new IllegalArgumentException("Empty number");
      } else {
         Matcher found = NUMBER_TOKEN_PATTERN.matcher(raw);
         if (!found.find()) {
            throw new IllegalArgumentException("Not a number: " + raw);
         } else {
            return normalizeMatchedNumber(found.group(), raw, style);
         }
      }
   }

   public static String parseNormalizedNumber(String raw) {
      return parseNormalizedNumber(raw, MacroTemplateResolver.ResolveMode.AUTO);
   }

   public static String parseNormalizedNumber(String raw, MacroTemplateResolver.ResolveMode style) {
      if (raw == null) {
         throw new IllegalArgumentException("Empty number");
      } else {
         Matcher found = NUMBER_TOKEN_PATTERN.matcher(raw.trim());
         if (!found.matches()) {
            throw new IllegalArgumentException("Not a number: " + raw);
         } else {
            return normalizeMatchedNumber(found.group(), raw, style);
         }
      }
   }

   private static String normalizeMatchedNumber(String match, String raw, MacroTemplateResolver.ResolveMode style) {
      String token = match.trim().replace("_", "").replaceAll("\\p{Sc}", "").toUpperCase(Locale.ROOT);
      if (token.isEmpty()) {
         throw new IllegalArgumentException("Not a number: " + raw);
      }

      BigDecimal var10000;
      switch (token.charAt(token.length() - 1)) {
         case 'B':
            var10000 = new BigDecimal("1000000000");
            break;
         case 'K':
            var10000 = new BigDecimal("1000");
            break;
         case 'M':
            var10000 = new BigDecimal("1000000");
            break;
         case 'T':
            var10000 = new BigDecimal("1000000000000");
            break;
         default:
            var10000 = BigDecimal.ONE;
      }

      BigDecimal multiplier = var10000;
      boolean scaled = multiplier.compareTo(BigDecimal.ONE) != 0;
      if (scaled) {
         token = token.substring(0, token.length() - 1);
      }

      String cleaned = style == MacroTemplateResolver.ResolveMode.WHOLE && !scaled ? normalizeWholeNumberDigits(token) : normalizeDecimalSeparators(token);
      if (!cleaned.isEmpty() && !"-".equals(cleaned) && !"+".equals(cleaned)) {
         return formatBigDecimal(new BigDecimal(cleaned).multiply(multiplier));
      } else {
         throw new IllegalArgumentException("Not a number: " + raw);
      }
   }

   private static String normalizeWholeNumberDigits(String token) {
      int last = Math.max(token.lastIndexOf(46), token.lastIndexOf(44));
      String body = last >= 0 && token.length() - last - 1 != 3 ? token.substring(0, last) : token;
      StringBuilder out = new StringBuilder(body.length());

      for (int i = 0; i < body.length(); i++) {
         char ch = body.charAt(i);
         if (ch != '.' && ch != ',') {
            out.append(ch);
         }
      }

      String digits = out.toString();
      return !digits.isEmpty() && !"-".equals(digits) && !"+".equals(digits) ? digits : digits + "0";
   }

   private static String normalizeDecimalSeparators(String token) {
      int lastDot = token.lastIndexOf(46);
      int lastComma = token.lastIndexOf(44);
      char decimal = 0;
      if (lastDot >= 0 && lastComma >= 0) {
         decimal = (char)(lastDot > lastComma ? 46 : 44);
      } else if (lastDot >= 0 || lastComma >= 0) {
         char only = (char)(lastDot >= 0 ? 46 : 44);
         int index = Math.max(lastDot, lastComma);
         boolean groups = countOccurrences(token, only) > 1 || only == ',' && token.length() - index - 1 == 3;
         if (!groups) {
            decimal = only;
         }
      }

      StringBuilder out = new StringBuilder(token.length());

      for (int i = 0; i < token.length(); i++) {
         char ch = token.charAt(i);
         if (ch != '.' && ch != ',') {
            out.append(ch);
         } else if (ch == decimal) {
            out.append('.');
         }
      }

      return out.toString();
   }

   private static int countOccurrences(String text, char ch) {
      int total = 0;

      for (int i = 0; i < text.length(); i++) {
         if (text.charAt(i) == ch) {
            total++;
         }
      }

      return total;
   }

   public static String formatBigDecimal(BigDecimal value) {
      BigDecimal stripped = value.stripTrailingZeros();
      return stripped.scale() < 0 ? stripped.setScale(0).toPlainString() : stripped.toPlainString();
   }

   public static String parseIntegerNumber(String raw) {
      if (raw == null) {
         throw new IllegalArgumentException("Empty number");
      }

      String token = raw.trim().replace(" ", "").toUpperCase(Locale.ROOT);
      if (token.isEmpty()) {
         throw new IllegalArgumentException("Empty number");
      }

      BigDecimal multiplier = BigDecimal.ONE;
      char suffix = token.charAt(token.length() - 1);
      BigDecimal var10000;
      switch (suffix) {
         case 'B':
            var10000 = new BigDecimal("1000000000");
            break;
         case 'K':
            var10000 = new BigDecimal("1000");
            break;
         case 'M':
            var10000 = new BigDecimal("1000000");
            break;
         case 'T':
            var10000 = new BigDecimal("1000000000000");
            break;
         default:
            var10000 = BigDecimal.ONE;
      }

      multiplier = var10000;
      if (multiplier.compareTo(BigDecimal.ONE) != 0) {
         token = token.substring(0, token.length() - 1);
      }

      BigDecimal number = new BigDecimal(normalizeDecimalSeparators(token)).multiply(multiplier).setScale(0, RoundingMode.DOWN);
      return number.toPlainString();
   }

   private static Optional<MacroVarValue> resolveBuiltinVariable(String name, Minecraft mc) {
      if (name == null) {
         return Optional.empty();
      }

      String key = name.trim().toLowerCase(Locale.ROOT);
      if ("timestamp".equals(key)) {
         return Optional.of(MacroVarValue.ofText(Instant.now().toString()));
      }

      if ("last_sent".equals(key)) {
         String sent = AutismSessionState.getInstance().getLastSentMessage();
         return Optional.of(MacroVarValue.of(MacroVarValue.ValueType.TEXT, sent, Map.of("raw", MacroVarValue.ofText(sent))));
      }

      if (mc != null && mc.player != null) {
         Optional var10000;
         switch (key) {
            case "player":
            case "user":
            case "username":
               var10000 = Optional.of(MacroVarValue.ofText(mc.player.getName().getString()));
               break;
            case "uuid":
               var10000 = Optional.of(MacroVarValue.ofText(mc.player.getUUID().toString()));
               break;
            case "x":
               var10000 = Optional.of(MacroVarValue.ofText(String.format(Locale.ROOT, "%.3f", mc.player.getX())));
               break;
            case "y":
               var10000 = Optional.of(MacroVarValue.ofText(String.format(Locale.ROOT, "%.3f", mc.player.getY())));
               break;
            case "z":
               var10000 = Optional.of(MacroVarValue.ofText(String.format(Locale.ROOT, "%.3f", mc.player.getZ())));
               break;
            case "bx":
               var10000 = Optional.of(MacroVarValue.ofText(Integer.toString(mc.player.blockPosition().getX())));
               break;
            case "by":
               var10000 = Optional.of(MacroVarValue.ofText(Integer.toString(mc.player.blockPosition().getY())));
               break;
            case "bz":
               var10000 = Optional.of(MacroVarValue.ofText(Integer.toString(mc.player.blockPosition().getZ())));
               break;
            case "pos":
               BlockPos pos = mc.player.blockPosition();
               var10000 = Optional.of(MacroVarValue.ofText(pos.getX() + " " + pos.getY() + " " + pos.getZ()));
               break;
            case "yaw":
               var10000 = Optional.of(MacroVarValue.ofText(String.format(Locale.ROOT, "%.2f", mc.player.getYRot())));
               break;
            case "pitch":
               var10000 = Optional.of(MacroVarValue.ofText(String.format(Locale.ROOT, "%.2f", mc.player.getXRot())));
               break;
            case "rot":
               var10000 = Optional.of(MacroVarValue.ofText(String.format(Locale.ROOT, "%.2f %.2f", mc.player.getYRot(), mc.player.getXRot())));
               break;
            case "facing":
               var10000 = Optional.of(MacroVarValue.ofText(Direction.fromYRot(mc.player.getYRot()).getName()));
               break;
            case "dimension":
            case "dim":
               var10000 = mc.level == null ? Optional.empty() : Optional.of(MacroVarValue.ofText(mc.level.dimension().identifier().toString()));
               break;
            case "selected_slot":
            case "target_slot":
               var10000 = Optional.of(MacroVarValue.ofSlot(mc.player.getInventory().getSelectedSlot()));
               break;
            case "server":
               ServerData server = mc.getCurrentServer();
               var10000 = server != null && server.ip != null ? Optional.of(MacroVarValue.ofText(server.ip)) : Optional.empty();
               break;
            default:
               var10000 = Optional.empty();
         }

         return var10000;
      } else {
         return Optional.empty();
      }
   }

   private record TokenResult(boolean resolved, String value, String missingName) {
      static MacroTemplateResolver.TokenResult ok(String value) {
         return new MacroTemplateResolver.TokenResult(true, value, "");
      }

      static MacroTemplateResolver.TokenResult missing(String name) {
         return new MacroTemplateResolver.TokenResult(false, "", name == null ? "" : name);
      }
   }

   public record ResolveResult(boolean success, String value, List<String> missingVariables, String errorMessage) {
      public static MacroTemplateResolver.ResolveResult ok(String value) {
         return new MacroTemplateResolver.ResolveResult(true, value, List.of(), "");
      }

      public static MacroTemplateResolver.ResolveResult failed(List<String> missing, String error) {
         return new MacroTemplateResolver.ResolveResult(false, "", List.copyOf(missing), error == null ? "" : error);
      }
   }

   public enum ResolveMode {
      AUTO,
      WHOLE;
   }

   private record PlaceholderSpan(String text, int weight, String rangeLo, String rangeHi) {
   }
}
