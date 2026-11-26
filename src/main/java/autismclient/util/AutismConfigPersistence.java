package autismclient.util;

import autismclient.AutismClientAddon;

final class AutismConfigPersistence {
   private static final String init = "config:" + AutismConfig.configFile().getAbsolutePath();

   private AutismConfigPersistence() {
   }

   static void init(AutismConfig config) {
      DebouncedPersistenceScheduler.init(config);
   }

   static void load(AutismConfig source) {
      AutismConfig snapshot;
      try {
         snapshot = AutismConfigSnapshotter.load(source);
      } catch (Throwable t) {
         AutismClientAddon.LOG.error("Failed to capture Autism config", t);
         return;
      }

      read(snapshot);
      AutismConfig.onPersistenceSnapshot(snapshot);
   }

   static void read(AutismConfig snapshot) {
      DebouncedPersistenceScheduler.init(init, () -> {
         try {
            AutismConfig.writeToDisk(AutismConfig.toJson(snapshot));
         } catch (Throwable t) {
            AutismClientAddon.LOG.error("Failed to serialize Autism config", t);
         }
      });
   }

   static void init() {
      DebouncedPersistenceScheduler.init();
   }

   static void init(long timeoutMs) {
      DebouncedPersistenceScheduler.init(timeoutMs);
   }
}
