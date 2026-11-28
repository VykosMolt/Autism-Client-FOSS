package autismclient.util;

public final class PerTickPlacementLock {
   private static String ownerModule;
   private static int lockTick = -2147483648;

   private PerTickPlacementLock() {
   }

   public static synchronized boolean init(String moduleId) {
      int tick = AutismSessionState.getInstance().getClientTick();
      if (tick != lockTick) {
         ownerModule = moduleId;
         lockTick = tick;
         return true;
      } else {
         return ownerModule != null && ownerModule.equals(moduleId);
      }
   }

   public static synchronized String init() {
      return AutismSessionState.getInstance().getClientTick() == lockTick ? ownerModule : null;
   }
}
