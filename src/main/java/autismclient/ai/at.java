package autismclient.ai;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public record at(List<String> types, Map<String, Integer> counts) {
   public at {
      types = List.copyOf(types);
      counts = Collections.unmodifiableMap(new LinkedHashMap<>(counts));
   }

   public static at parse(String text) {
      if (text != null && !text.isBlank()) {
         LinkedHashSet<String> types = new LinkedHashSet<>();
         LinkedHashMap<String, Integer> counts = new LinkedHashMap<>();

         for (String part : text.split(",", -1)) {
            String[] pair = part.split("=", -1);
            if (pair.length > 2) {
               throw new IllegalArgumentException("Use block=amount for a mining amount");
            }

            String type = pair[0].trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "_").replaceAll("^_+|_+$", "");
            if (type.isEmpty()) {
               throw new IllegalArgumentException("A mining block is missing between commas");
            }

            types.add(type);
            if (pair.length == 2) {
               String number = pair[1].trim().replaceAll("^_+|_+$", "");

               int amount;
               try {
                  if (!number.matches("[0-9]+")) {
                     throw new NumberFormatException();
                  }

                  amount = Integer.parseInt(number);
               } catch (NumberFormatException e) {
                  throw new IllegalArgumentException("Choose an amount from 1 to 640 for " + type);
               }

               if (amount < 1 || amount > 640) {
                  throw new IllegalArgumentException("Choose an amount from 1 to 640 for " + type);
               }

               Integer old = counts.putIfAbsent(type, amount);
               if (old != null && old != amount) {
                  throw new IllegalArgumentException("Conflicting mining amounts for " + type);
               }
            }
         }

         return new at(List.copyOf(types), counts);
      } else {
         throw new IllegalArgumentException("Choose at least one block to mine");
      }
   }

   public String spec() {
      return String.join(",", this.types);
   }

   public static boolean matches(String selectors, String id) {
      if (selectors != null && !selectors.isBlank() && id != null && !id.isBlank()) {
         String path = id.substring(id.indexOf(58) + 1);
         if (selectors.indexOf(44) < 0) {
            return matchesOne(selectors, id, path);
         }

         for (String type : selectors.split(",")) {
            type = type.trim();
            if (!type.isEmpty() && matchesOne(type, id, path)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static boolean matchesOne(String type, String id, String path) {
      return type.indexOf(58) >= 0 ? id.equals(type) : path.equals(type) || path.endsWith("_" + type);
   }
}
