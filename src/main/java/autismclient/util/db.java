package autismclient.util;

import java.util.List;

public class db {
   public static List<aM> init() {
      return z.init_List();
   }

   public static void init(aM packUtilMacro) {
      if (z.init(packUtilMacro)) {
         AutismClientMessaging.sendPrefixed("§aImported to Meteor: " + packUtilMacro.init_Str);
      } else {
         AutismClientMessaging.sendPrefixed("§cFailed to import to Meteor");
      }
   }

   public static boolean init(String macroName) {
      aM packUtilMacro = z.init_aM(macroName);
      if (packUtilMacro == null) {
         AutismClientMessaging.sendPrefixed("§cMeteor macro not found: " + macroName);
         return false;
      }

      aM imported = aN.init_aN().init_aM_aM_Str(packUtilMacro, packUtilMacro.init_Str);
      if (imported == null) {
         return false;
      }

      if (!imported.init_Str.equals(packUtilMacro.init_Str)) {
         AutismClientMessaging.sendPrefixed("§eImported Meteor macro as: " + imported.init_Str);
      }

      return true;
   }
}
