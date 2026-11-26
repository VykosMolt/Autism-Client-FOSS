package autismclient.gui.ui.widgets;

public final class Utf16TextCursor {
   private Utf16TextCursor() {
   }

   static int[] init(String text, Utf16TextCursor.Inner_DataUtil measurer) {
      String value = text == null ? "" : text;
      int[] widths = new int[value.length() + 1];
      float running = 0.0F;
      int index = 0;

      while (index < value.length()) {
         int end = value.offsetByCodePoints(index, 1);

         for (int intermediate = index + 1; intermediate < end; intermediate++) {
            widths[intermediate] = widths[index];
         }

         running += Math.max(0.0F, measurer.width(value, index, end));
         widths[end] = (int)Math.ceil(running - 1.0E-4F);
         index = end;
      }

      return widths;
   }

   public static int init_I(String text, int index) {
      String value = text == null ? "" : text;
      int clamped = Math.max(0, Math.min(index, value.length()));
      return clamped > 0 && clamped < value.length() && Character.isHighSurrogate(value.charAt(clamped - 1)) && Character.isLowSurrogate(value.charAt(clamped))
         ? clamped - 1
         : clamped;
   }

   public static int load(String text, int index) {
      String value = text == null ? "" : text;
      int clamped = Math.max(0, Math.min(index, value.length()));
      return clamped > 0 && clamped < value.length() && Character.isHighSurrogate(value.charAt(clamped - 1)) && Character.isLowSurrogate(value.charAt(clamped))
         ? clamped + 1
         : clamped;
   }

   public static int read(String text, int index) {
      String value = text == null ? "" : text;
      int boundary = init_I(value, index);
      return boundary <= 0 ? 0 : value.offsetByCodePoints(boundary, -1);
   }

   public static int write(String text, int index) {
      String value = text == null ? "" : text;
      int boundary = load(value, index);
      return boundary >= value.length() ? value.length() : value.offsetByCodePoints(boundary, 1);
   }

   static int init(String text, int[] widths, int candidate, int targetWidth) {
      String value = text == null ? "" : text;
      int clamped = Math.max(0, Math.min(candidate, value.length()));
      int left = init_I(value, clamped);
      int right = load(value, clamped);
      if (left == right) {
         return left;
      } else {
         return targetWidth - widths[left] < widths[right] - targetWidth ? left : right;
      }
   }

   static String init_Str(String text, int maxUtf16Units) {
      String value = text == null ? "" : text;
      return value.length() <= maxUtf16Units ? value : value.substring(0, init_I(value, Math.max(0, maxUtf16Units)));
   }

   @FunctionalInterface
   interface Inner_DataUtil {
      float width(String var1, int var2, int var3);
   }
}
