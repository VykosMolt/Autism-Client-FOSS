package autismclient.util;

public final class PerTickPlacementLock {
   private static String init_Str;
   private static int init_I = -2147483648;

   private PerTickPlacementLock() {
   }

   public static synchronized boolean init(String moduleId) {
      int tick = AutismSessionState.init_ck().state_I();
      if (tick != init_I) {
         init_Str = moduleId;
         init_I = tick;
         return true;
      } else {
         return init_Str != null && init_Str.equals(moduleId);
      }
   }

   public static synchronized String init() {
      return AutismSessionState.init_ck().state_I() == init_I ? init_Str : null;
   }
}
