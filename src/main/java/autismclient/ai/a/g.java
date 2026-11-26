package autismclient.ai.a;

import autismclient.util.SilentRotationManager;

public final class g {
   private static volatile boolean init;

   private g() {
   }

   public static boolean init(boolean windingDown, boolean moduleEnabled, boolean scaffoldOwnsRotation) {
      return !scaffoldOwnsRotation && (windingDown || moduleEnabled && init);
   }

   public static boolean init_Z() {
      return init;
   }

   public static void init(boolean value) {
      init = value;
   }

   public static void init_V() {
      init = false;
      SilentRotationManager.init("ai-assistant");
   }
}
