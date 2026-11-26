package autismclient.util;

public final class CustomMainMenuGuard {
   private CustomMainMenuGuard() {
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
