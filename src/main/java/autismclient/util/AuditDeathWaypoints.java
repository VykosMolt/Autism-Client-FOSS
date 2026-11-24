package autismclient.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public final class AuditDeathWaypoints {
   public static int retention(JsonObject var0) {
      JsonElement var1 = var0.get("keep");
      if (var1 != null && var1.isJsonPrimitive() && var1.getAsJsonPrimitive().isNumber()) {
         try {
            int var2 = var1.getAsBigDecimal().intValueExact();
            if (var2 > 0) {
               return var2;
            }
         } catch (ArithmeticException | NumberFormatException var3) {
         }
      }

      throw new IllegalArgumentException("pruneDeaths requires keep to be a positive integer");
   }

   static String nextName(List<AutismWaypoints.Waypoint> var0, String var1) {
      String var2 = (var1 != null && !var1.isBlank() ? var1.trim() : "Waypoint") + " #";
      HashSet var3 = new HashSet();
      int var4 = 0;

      for (AutismWaypoints.Waypoint var6 : var0) {
         String var7 = var6.name();
         if (var7 != null && var7.regionMatches(true, 0, var2, 0, var2.length())) {
            try {
               int var8 = Integer.parseInt(var7.substring(var2.length()).trim());
               var3.add(var8);
               var4 = Math.max(var4, var8);
            } catch (NumberFormatException var9) {
            }
         }
      }

      if (var4 < 2147483647) {
         return var2 + (var4 + 1);
      }

      int var10 = 1;

      while (var3.contains(var10)) {
         var10++;
      }

      return var2 + var10;
   }

   static AutismWaypoints.Waypoint kw_record_AutismWaypoints_Wa(
      Map<String, List<AutismWaypoints.Waypoint>> var0, String var1, int var2, int var3, int var4, int var5, long var6, int var8
   ) {
      synchronized (var0) {
         List var10 = var0.computeIfAbsent(var1, var0x -> new ArrayList<>());
         ArrayList var11 = new ArrayList();

         for (AutismWaypoints.Waypoint var13 : (List<AutismWaypoints.Waypoint>)var10) {
            if (var13.death()) {
               long var14 = var6 - var13.createdMs();
               if (var13.x() == var2 && var13.y() == var3 && var13.z() == var4 && var6 >= var13.createdMs() && var14 >= 0L && var14 < 30000L) {
                  return null;
               }

               var11.add(var13);
            }
         }

         int var18 = var10.size() - 511;
         if (var8 >= 1) {
            var18 = Math.max(var18, var11.size() - (var8 - 1));
         }

         if (var18 > var11.size()) {
            return null;
         }

         AutismWaypoints.Waypoint var19 = new AutismWaypoints.Waypoint(nextName(var10, "Death"), var2, var3, var4, var5, var6, true);
         if (var18 > 0) {
            var11.sort(Comparator.comparingLong(AutismWaypoints.Waypoint::createdMs));

            for (AutismWaypoints.Waypoint var15 : (List<AutismWaypoints.Waypoint>)var11.subList(0, var18)) {
               var10.remove(var15);
            }
         }

         var10.add(var19);
         return var19;
      }
   }
}
