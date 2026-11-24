package autismclient.util;

public final class bg {
   public static final int init_I = 1;
   public static final int load_I = 2;
   public static final byte[] init_BArr = new byte[0];
   private static final int read = 3;
   private static volatile long init_J;
   private static volatile long load_J;
   private static final ThreadLocal<int[]> init_ThreadLocal = new ThreadLocal<>();

   private bg() {
   }

   public static long init_J() {
      return init_J;
   }

   public static long load_J() {
      long current = init_J;
      if (!load(current)) {
         return current;
      }

      int[] depth = init_ThreadLocal.get();
      return depth != null && depth[0] > 0 ? current & -3L : current;
   }

   public static void init_V() {
      int[] depth = init_ThreadLocal.get();
      if (depth == null) {
         depth = new int[1];
         init_ThreadLocal.set(depth);
      }

      depth[0]++;
   }

   public static void load_V() {
      int[] depth = init_ThreadLocal.get();
      if (depth != null && --depth[0] <= 0) {
         init_ThreadLocal.remove();
      }
   }

   public static void read() {
      init_ThreadLocal.remove();
   }

   public static int init_I_J(long capturedState) {
      return (int)capturedState & 3;
   }

   public static boolean init_Z_J(long capturedState) {
      return (init_I_J(capturedState) & 1) != 0;
   }

   public static boolean load(long capturedState) {
      return (init_I_J(capturedState) & 2) != 0;
   }

   public static boolean init_Z() {
      return init_Z_J(init_J);
   }

   public static boolean load_Z() {
      return load(init_J);
   }

   public static void write() {
      load(autismclient.modules.PoolEntry.init_PoolEntry());
   }

   public static void init_V_PoolEntry(autismclient.modules.PoolEntry module) {
      long deadline = load_J;
      if (deadline > 0L && System.currentTimeMillis() > deadline) {
         load_J = 0L;
         load(module);
      }
   }

   public static void load(autismclient.modules.PoolEntry module) {
      int next = 0;
      if (module != null && !autismclient.modules.aD.read_Z()) {
         if (module.update_Z()) {
            next |= 1;
         }

         if (module.sync_Z()) {
            next |= 2;
         }

         long deadline = module.init_J();
         load_J = deadline > System.currentTimeMillis() ? deadline : 0L;
      } else {
         load_J = 0L;
      }

      init_V_I(next);
   }

   public static void update() {
      load_J = 0L;
      init_V_I(0);
   }

   private static void init_V_I(int nextMode) {
      long previous = init_J;
      if (init_I_J(previous) != nextMode) {
         synchronized (bg.class) {
            previous = init_J;
            if (init_I_J(previous) == nextMode) {
               return;
            }

            long nextEpoch = (previous >>> 2) + 1L;
            init_J = nextEpoch << 2 | nextMode & 3;
         }

         cf.init_V_J_Z(1024L, nextMode != 0);
      }
   }
}
