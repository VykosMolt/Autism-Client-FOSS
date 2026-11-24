package autismclient.util;

import org.jetbrains.annotations.Nullable;

public class T {
   private static volatile AutismFabricatorOverlay init = null;

   public static void init(@Nullable AutismFabricatorOverlay overlay) {
      init = overlay;
   }

   @Nullable
   public static AutismFabricatorOverlay init() {
      return init;
   }
}
