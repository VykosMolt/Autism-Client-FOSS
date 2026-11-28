package autismclient.util;

final class ServerScanAttemptTracker {
   private static final int init_I = 4;
   private final long init_J;
   private final String init_Str;
   private boolean init_Z;

   ServerScanAttemptTracker(long generation, String serverAddress) {
      this.init_J = generation;
      this.init_Str = serverAddress == null ? "" : serverAddress;
   }

   boolean init(long expectedGeneration, String currentAddress) {
      return !this.init_Z && this.init_J == expectedGeneration && this.init_Str.equals(currentAddress == null ? "" : currentAddress);
   }

   boolean canRetry(int completedAttempts) {
      return !this.init_Z && completedAttempts + 1 < 4;
   }

   boolean finish() {
      if (this.init_Z) {
         return false;
      }

      this.init_Z = true;
      return true;
   }

   boolean hadFailures(int runtimeErrors, int failedProbes, int pendingProbes) {
      return runtimeErrors > 0 || failedProbes > 0 || pendingProbes > 0;
   }

   boolean isFinished() {
      return this.init_Z;
   }
}
