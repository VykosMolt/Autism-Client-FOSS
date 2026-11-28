package autismclient.ai;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class AiTaskDuration {
   private static final Pattern RAISE_PATTERN = Pattern.compile("\\b(?:for|during|over the next)\\s+(\\d+(?:\\.\\d+)?)\\s*(hours?|hrs?|minutes?|mins?|days?)\\b");
   private static final Pattern CAP_PATTERN = Pattern.compile(
      "\\b(?:in under|under|within|less than|no more than|max(?:imum)? of|cap(?:ped)?\\s+(?:at|of))\\s+(\\d+(?:\\.\\d+)?)\\s*(hours?|hrs?|minutes?|mins?|days?)\\b"
   );

   static int estimateMinutes(String userRequest, int configured) {
      String text = userRequest.toLowerCase(Locale.ROOT);
      if (text.matches("(?s).*\\b(macro|script|explain|example)\\b.*")) {
         return configured;
      }

      Matcher raise = RAISE_PATTERN.matcher(text);
      int result = configured;

      while (raise.find()) {
         double minutes = toMinutes(raise.group(1), raise.group(2));
         if (!Double.isNaN(minutes)) {
            result = Math.max(result, (int)Math.ceil(minutes));
         }
      }

      Matcher cap = CAP_PATTERN.matcher(text);

      while (cap.find()) {
         double minutes = toMinutes(cap.group(1), cap.group(2));
         if (!Double.isNaN(minutes)) {
            result = Math.max(1, (int)Math.ceil(minutes));
         }
      }

      return result;
   }

   private static double toMinutes(String amount, String unit) {
      double minutes = Double.parseDouble(amount) * (unit.startsWith("h") ? 60 : (unit.startsWith("d") ? 1440 : 1));
      return Double.isFinite(minutes) && minutes <= 525600.0 ? minutes : 0.0 / 0.0;
   }
}
