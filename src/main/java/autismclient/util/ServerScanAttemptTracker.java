package autismclient.util;

final class ServerScanAttemptTracker {
   private static final int init_I = 4;
   private final long generation;
   private final String serverAddress;
   private boolean cancelled;

   ServerScanAttemptTracker(long generation, String serverAddress) {
      this.generation = generation;
      this.serverAddress = serverAddress == null ? "" : serverAddress;
   }

   boolean init(long expectedGeneration, String currentAddress) {
      return !this.cancelled && this.generation == expectedGeneration && this.serverAddress.equals(currentAddress == null ? "" : currentAddress);
   }

   boolean canRetry(int completedAttempts) {
      return !this.cancelled && completedAttempts + 1 < 4;
   }

   boolean finish() {
      if (this.cancelled) {
         return false;
      }

      this.cancelled = true;
      return true;
   }

   boolean hadFailures(int runtimeErrors, int failedProbes, int pendingProbes) {
      return runtimeErrors > 0 || failedProbes > 0 || pendingProbes > 0;
   }

   boolean isFinished() {
      return this.cancelled;
   }
}
