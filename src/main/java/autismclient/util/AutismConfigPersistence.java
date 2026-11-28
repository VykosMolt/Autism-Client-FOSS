package autismclient.util;

import autismclient.AutismClientAddon;

final class AutismConfigPersistence {
   private static final String SCHEDULE_KEY = "config:" + AutismConfig.configFile().getAbsolutePath();

   private AutismConfigPersistence() {
   }

   static void schedule(AutismConfig config) {
      DebouncedPersistenceScheduler.init(config);
   }

   static void persist(AutismConfig source) {
      AutismConfig snapshot;
      try {
         snapshot = AutismConfigSnapshotter.load(source);
      } catch (Throwable t) {
         AutismClientAddon.LOG.error("Failed to capture Autism config", t);
         return;
      }

      scheduleWrite(snapshot);
      AutismConfig.onPersistenceSnapshot(snapshot);
   }

   static void scheduleWrite(AutismConfig snapshot) {
      DebouncedPersistenceScheduler.init(SCHEDULE_KEY, () -> {
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
