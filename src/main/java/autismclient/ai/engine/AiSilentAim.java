package autismclient.ai.engine;

import autismclient.util.SilentRotationManager;

public final class AiSilentAim {
   private static volatile boolean aimEnabled;

   private AiSilentAim() {
   }

   public static boolean active(boolean windingDown, boolean moduleEnabled, boolean scaffoldOwnsRotation) {
      return !scaffoldOwnsRotation && (windingDown || moduleEnabled && aimEnabled);
   }

   public static boolean init_Z() {
      return aimEnabled;
   }

   public static void setEnabled(boolean value) {
      aimEnabled = value;
   }

   public static void release() {
      aimEnabled = false;
      SilentRotationManager.releaseOwner("ai-assistant");
   }
}
