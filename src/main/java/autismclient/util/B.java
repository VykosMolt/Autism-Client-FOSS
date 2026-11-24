package autismclient.util;

import autismclient.AutismClientAddon;

final class B {
   private static final String init = "config:" + AutismConfig.configFile().getAbsolutePath();

   private B() {
   }

   static void init(AutismConfig config) {
      dj.init(config);
   }

   static void load(AutismConfig source) {
      AutismConfig snapshot;
      try {
         snapshot = A.load(source);
      } catch (Throwable t) {
         AutismClientAddon.LOG.error("Failed to capture Autism config", t);
         return;
      }

      read(snapshot);
      AutismConfig.onPersistenceSnapshot(snapshot);
   }

   static void read(AutismConfig snapshot) {
      dj.init(init, () -> {
         try {
            AutismConfig.writeToDisk(AutismConfig.toJson(snapshot));
         } catch (Throwable t) {
            AutismClientAddon.LOG.error("Failed to serialize Autism config", t);
         }
      });
   }

   static void init() {
      dj.init();
   }

   static void init(long timeoutMs) {
      dj.init(timeoutMs);
   }
}
