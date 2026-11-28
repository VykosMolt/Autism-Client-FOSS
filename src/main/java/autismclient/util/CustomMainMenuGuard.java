package autismclient.util;

public final class CustomMainMenuGuard {
   private CustomMainMenuGuard() {
   }

   public static boolean isEnabled() {
      if (AutismLiteVariant.enabled()) {
         return false;
      }

      AutismConfig config = AutismConfig.getGlobal();
      return config == null || config.customMainMenu;
   }

   public static boolean isSuppressed() {
      return autismclient.modules.aD.init_Z() || !isEnabled();
   }
}
