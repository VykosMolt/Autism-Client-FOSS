package autismclient.util;

public final class PacketCaptureState {
   public static final int init_I = 1;
   public static final int load_I = 2;
   public static final byte[] EMPTY_BYTES = new byte[0];
   private static final int read = 3;
   private static volatile long capturedState;
   private static volatile long load_J;
   private static final ThreadLocal<int[]> CAPTURE_DEPTH = new ThreadLocal<>();

   private PacketCaptureState() {
   }

   public static long getState() {
      return capturedState;
   }

   public static long effectiveState() {
      long current = capturedState;
      if (!isPayloadCapture(current)) {
         return current;
      }

      int[] depth = CAPTURE_DEPTH.get();
      return depth != null && depth[0] > 0 ? current & -3L : current;
   }

   public static void enterCapture() {
      int[] depth = CAPTURE_DEPTH.get();
      if (depth == null) {
         depth = new int[1];
         CAPTURE_DEPTH.set(depth);
      }

      depth[0]++;
   }

   public static void exitCapture() {
      int[] depth = CAPTURE_DEPTH.get();
      if (depth != null && --depth[0] <= 0) {
         CAPTURE_DEPTH.remove();
      }
   }

   public static void read() {
      CAPTURE_DEPTH.remove();
   }

   public static int captureFlags(long capturedState) {
      return (int)capturedState & 3;
   }

   public static boolean isLoggerCapture(long capturedState) {
      return (captureFlags(capturedState) & 1) != 0;
   }

   public static boolean isPayloadCapture(long capturedState) {
      return (captureFlags(capturedState) & 2) != 0;
   }

   public static boolean isLoggerCaptureActive() {
      return isLoggerCapture(capturedState);
   }

   public static boolean load_Z() {
      return isPayloadCapture(capturedState);
   }

   public static void write() {
      refresh(autismclient.modules.ModuleRuntimeManager.getInstance());
   }

   public static void expireIfDue(autismclient.modules.ModuleRuntimeManager module) {
      long deadline = load_J;
      if (deadline > 0L && System.currentTimeMillis() > deadline) {
         load_J = 0L;
         refresh(module);
      }
   }

   public static void refresh(autismclient.modules.ModuleRuntimeManager module) {
      int next = 0;
      if (module != null && !autismclient.modules.aD.read_Z()) {
         if (module.isLoggerCaptureEnabled()) {
            next |= 1;
         }

         if (module.needsAnyPayloadHook()) {
            next |= 2;
         }

         long deadline = module.getJoinProbeWindowUntilMs();
         load_J = deadline > System.currentTimeMillis() ? deadline : 0L;
      } else {
         load_J = 0L;
      }

      setCaptureMode(next);
   }

   public static void update() {
      load_J = 0L;
      setCaptureMode(0);
   }

   private static void setCaptureMode(int nextMode) {
      long previous = capturedState;
      if (captureFlags(previous) != nextMode) {
         synchronized (PacketCaptureState.class) {
            previous = capturedState;
            if (captureFlags(previous) == nextMode) {
               return;
            }

            long nextEpoch = (previous >>> 2) + 1L;
            capturedState = nextEpoch << 2 | nextMode & 3;
         }

         cf.init_V_J_Z(1024L, nextMode != 0);
      }
   }
}
