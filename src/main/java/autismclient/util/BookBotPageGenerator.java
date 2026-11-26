package autismclient.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.PrimitiveIterator.OfInt;

public final class BookBotPageGenerator {
   public static final int init_I = 100;
   public static final int load_I = 100;
   public static final int read_I = 1024;
   public static final String init_Str = "Ascii";
   public static final String load_Str = "Utf8";
   public static final String read_Str = "PaperMC";

   private BookBotPageGenerator() {
   }

   public static List<String> init(int requestedPages, int requestedCharsPerPage, String randomType, Random random) {
      String type = init(randomType);
      if ("PaperMC".equals(type)) {
         return init(random);
      }

      int pages = Math.max(1, Math.min(100, requestedPages));
      int charsPerPage = Math.max(1, Math.min(1024, requestedCharsPerPage));
      Random rng = random == null ? new Random() : random;
      OfInt chars = "Ascii".equals(type) ? rng.ints(33, 128).filter(BookBotPageGenerator::init).iterator() : rng.ints(33, 55296).filter(BookBotPageGenerator::init).iterator();
      List<String> out = new ArrayList<>(pages);

      for (int page = 0; page < pages; page++) {
         StringBuilder builder = new StringBuilder(charsPerPage);

         for (int i = 0; i < charsPerPage && chars.hasNext(); i++) {
            builder.appendCodePoint(chars.nextInt());
         }

         out.add(builder.toString());
      }

      return out;
   }

   public static String init(String value) {
      if (value == null) {
         return "Utf8";
      }

      String var10000;
      switch (value.trim().toLowerCase(Locale.ROOT)) {
         case "ascii":
            var10000 = "Ascii";
            break;
         case "papermc":
         case "paper":
            var10000 = "PaperMC";
            break;
         default:
            var10000 = "Utf8";
      }

      return var10000;
   }

   private static List<String> init(Random random) {
      Random rng = random == null ? new Random() : random;
      List<String> pages = new ArrayList<>(100);
      StringBuilder page = new StringBuilder(1024);
      OfInt oneByte = rng.ints(33, 128).filter(BookBotPageGenerator::init).iterator();
      OfInt twoBytes = rng.ints(128, 2048).filter(BookBotPageGenerator::init).iterator();
      OfInt threeBytes = rng.ints(2048, 55296).filter(BookBotPageGenerator::init).iterator();

      for (int pageIndex = 0; pageIndex < 100; pageIndex++) {
         if (pageIndex < 50) {
            page.appendCodePoint(threeBytes.nextInt());

            for (int i = 1; i < 1024; i++) {
               page.appendCodePoint(oneByte.nextInt());
            }
         } else if (pageIndex != 50) {
            for (int i = 0; i < 1024; i++) {
               page.appendCodePoint(threeBytes.nextInt());
            }
         } else {
            for (int i = 0; i < 110; i++) {
               page.appendCodePoint(threeBytes.nextInt());
            }

            page.appendCodePoint(twoBytes.nextInt());

            for (int i = 0; i < 913; i++) {
               page.appendCodePoint(oneByte.nextInt());
            }
         }

         pages.add(page.toString());
         page.setLength(0);
      }

      return pages;
   }

   public static int init(List<String> pages) {
      if (pages != null && !pages.isEmpty()) {
         int bytes = 0;

         for (String page : pages) {
            if (page != null && !page.isEmpty()) {
               for (int i = 0; i < page.length(); i++) {
                  char c = page.charAt(i);
                  if (c < 128) {
                     bytes++;
                  } else if (c < 2048) {
                     bytes += 2;
                  } else {
                     bytes += 3;
                  }
               }
            }
         }

         return bytes;
      } else {
         return 0;
      }
   }

   private static boolean init(int value) {
      return !Character.isWhitespace(value) && value != 13 && value != 10;
   }
}
