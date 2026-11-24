package autismclient.gui.f;

import java.util.ArrayList;
import java.util.List;

public final class ConfigStore {
   private ConfigStore() {
   }

   public static List<ConfigStore.Inner_DataUtil> init(String text, int maxWidth, ConfigStore.Inner_StreamUtil width) {
      String source = text == null ? "" : text;
      int available = Math.max(1, maxWidth);
      List<ConfigStore.Inner_DataUtil> lines = new ArrayList<>();
      if (source.isEmpty()) {
         lines.add(new ConfigStore.Inner_DataUtil(0, 0, 0));
         return lines;
      }

      int paragraphStart = 0;

      while (paragraphStart < source.length()) {
         int newline = source.indexOf(10, paragraphStart);
         int paragraphEnd = newline >= 0 ? newline : source.length();
         if (paragraphStart == paragraphEnd) {
            lines.add(new ConfigStore.Inner_DataUtil(paragraphStart, newline + 1, paragraphStart));
         } else {
            int cursor = paragraphStart;

            while (cursor < paragraphEnd) {
               int end = init(source, cursor, paragraphEnd, available, width);
               int renderEnd = init(source, cursor, end);
               lines.add(new ConfigStore.Inner_DataUtil(cursor, end, renderEnd));
               cursor = end;
            }

            if (newline >= 0) {
               ConfigStore.Inner_DataUtil last = lines.remove(lines.size() - 1);
               lines.add(new ConfigStore.Inner_DataUtil(last.init(), newline + 1, last.read()));
            }
         }

         paragraphStart = newline >= 0 ? newline + 1 : source.length();
      }

      if (source.endsWith("\n")) {
         lines.add(new ConfigStore.Inner_DataUtil(source.length(), source.length(), source.length()));
      }

      return lines;
   }

   public static int init(String source, int start, int limit, int maxWidth, ConfigStore.Inner_StreamUtil width) {
      if (source != null && !source.isEmpty()) {
         int safeStart = init(source, Math.max(0, Math.min(start, source.length())));
         int safeLimit = init(source, Math.max(safeStart, Math.min(limit, source.length())));
         if (safeStart >= safeLimit) {
            return safeLimit;
         }

         int available = Math.max(1, maxWidth);
         int low = 1;
         int high = source.codePointCount(safeStart, safeLimit);
         int fittingCodePoints = 0;

         while (low <= high) {
            int middle = low + (high - low >>> 1);
            int middleEnd = source.offsetByCodePoints(safeStart, middle);
            if (width.width(safeStart, middleEnd) <= available) {
               fittingCodePoints = middle;
               low = middle + 1;
            } else {
               high = middle - 1;
            }
         }

         int fittingEnd = source.offsetByCodePoints(safeStart, fittingCodePoints);
         if (fittingEnd >= safeLimit) {
            return safeLimit;
         }

         if (fittingEnd == safeStart) {
            return source.offsetByCodePoints(safeStart, 1);
         }

         int end = fittingEnd;

         while (end > safeStart) {
            int index = source.offsetByCodePoints(end, -1);
            if (init(source.codePointAt(index))) {
               return end;
            }

            end = index;
         }

         return fittingEnd;
      } else {
         return 0;
      }
   }

   private static int init(String source, int start, int end) {
      int renderEnd;
      for (renderEnd = end; renderEnd > start; renderEnd--) {
         char ch = source.charAt(renderEnd - 1);
         if (ch != ' ' && ch != '\t' && ch != '\r' && ch != '\n') {
            break;
         }
      }

      return renderEnd;
   }

   private static int init(String source, int index) {
      return index > 0 && index < source.length() && Character.isHighSurrogate(source.charAt(index - 1)) && Character.isLowSurrogate(source.charAt(index))
         ? index - 1
         : index;
   }

   private static boolean init(int ch) {
      return Character.isWhitespace(ch) || ch == 44 || ch == 59 || ch == 58 || ch == 47 || ch == 41 || ch == 93 || ch == 125 || ch == 62;
   }

   public record Inner_DataUtil(int init, int load, int read) {
   }

   @FunctionalInterface
   public interface Inner_StreamUtil {
      int width(int var1, int var2);
   }
}
