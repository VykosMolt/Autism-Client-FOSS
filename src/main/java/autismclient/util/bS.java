package autismclient.util;

final class bS {
   private static final int init_I = 4;
   private final long init_J;
   private final String init_Str;
   private boolean init_Z;

   bS(long generation, String serverAddress) {
      this.init_J = generation;
      this.init_Str = serverAddress == null ? "" : serverAddress;
   }

   boolean init(long expectedGeneration, String currentAddress) {
      return !this.init_Z && this.init_J == expectedGeneration && this.init_Str.equals(currentAddress == null ? "" : currentAddress);
   }

   boolean init(int completedAttempts) {
      return !this.init_Z && completedAttempts + 1 < 4;
   }

   boolean init() {
      if (this.init_Z) {
         return false;
      }

      this.init_Z = true;
      return true;
   }

   boolean init(int runtimeErrors, int failedProbes, int pendingProbes) {
      return runtimeErrors > 0 || failedProbes > 0 || pendingProbes > 0;
   }

   boolean load() {
      return this.init_Z;
   }
}
