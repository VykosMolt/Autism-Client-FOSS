package autismclient.util;

public final class aT {
   private aT() {
   }

   public static boolean init() {
      if (AutismLiteVariant.enabled()) {
         return false;
      }

      AutismConfig config = AutismConfig.getGlobal();
      return config == null || config.customMainMenu;
   }

   public static boolean load() {
      return autismclient.modules.aD.init_Z() || !init();
   }
}
