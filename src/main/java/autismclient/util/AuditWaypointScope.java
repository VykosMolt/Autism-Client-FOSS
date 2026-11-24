package autismclient.util;

import java.util.Objects;
import net.minecraft.client.Minecraft;

public final class AuditWaypointScope {
   private static final String PREFIX = "@autism-waypoints/dimension/v1:";

   private AuditWaypointScope() {
   }

   public static String current(Minecraft var0) {
      String var1 = AutismWaypoints.init_Str_Minecraft(var0);
      return var0 != null && var0.level != null ? qualify(var1, var0.level.dimension().identifier().toString()) : var1;
   }

   public static String qualify(String var0, String var1) {
      Objects.requireNonNull(var0);
      return var1 != null && !var1.equals("minecraft:overworld")
         ? "@autism-waypoints/dimension/v1:" + var1.length() + ":" + var1 + ":" + var0.length() + ":" + var0
         : var0;
   }

   public static String display(String var0) {
      if (var0 != null && var0.startsWith("@autism-waypoints/dimension/v1:")) {
         try {
            int var1 = var0.indexOf(58, "@autism-waypoints/dimension/v1:".length()) + 1;
            int var2 = Integer.parseInt(var0.substring("@autism-waypoints/dimension/v1:".length(), var1 - 1));
            int var3 = Math.addExact(var1, var2);
            if (var2 >= 1 && var0.charAt(var3) == ':') {
               int var4 = var0.indexOf(58, var3 + 1) + 1;
               int var5 = Integer.parseInt(var0.substring(var3 + 1, var4 - 1));
               return var5 >= 0 && Math.addExact(var4, var5) == var0.length() ? var0.substring(var4) + " (" + var0.substring(var1, var3) + ")" : var0;
            } else {
               return var0;
            }
         } catch (IllegalArgumentException | IndexOutOfBoundsException | ArithmeticException var6) {
            return var0;
         }
      } else {
         return var0;
      }
   }

   public static boolean canAdd(Minecraft var0, String var1) {
      if (var0 != null && var0.player != null && !Objects.equals(current(var0), var1)) {
         AutismClientMessaging.sendPrefixed("World or dimension changed. Reopen Waypoints before adding a marker.");
         return false;
      } else {
         return true;
      }
   }
}
