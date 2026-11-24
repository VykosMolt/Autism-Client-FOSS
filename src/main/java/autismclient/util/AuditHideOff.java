package autismclient.util;

import autismclient.modules.Module;
import net.minecraft.client.Minecraft;

public final class AuditHideOff {
   private AuditHideOff() {
   }

   public static boolean disable() {
      AutismConfig var0 = AutismConfig.getGlobal();
      if (var0 != null && var0.modules != null) {
         AutismConfig.ModuleState var1 = var0.modules.get("hide");
         if (var1 != null && var1.enabled) {
            for (Module var3 : autismclient.modules.al.init_List_ae(autismclient.modules.ae.write)) {
               if ("hide".equals(var3.id())) {
                  var3.setEnabled(false);
                  AutismClientMessaging.sendPrefixed("Autism hide disabled");
                  Minecraft.getInstance().gui.setScreen(null);
                  return true;
               }
            }

            return false;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
