package autismclient.util;

import java.security.SecureRandom;

public final class CoordinateSpoofer {
   private static final SecureRandom init_SecureRandom = new SecureRandom();
   private static final long init_J = 1024L;
   private static final long load_J = 20000000L;
   private static volatile boolean init_Z;
   private static volatile CoordinateSpoofer.Inner_DataUtil init_V_DataUtil = CoordinateSpoofer.Inner_DataUtil.OFFSET;
   private static volatile boolean load_Z = false;
   private static double init_D;
   private static double load_D;
   private static double read_D;
   private static double write;
   private static double update;
   private static double sync;
   private static double flush;
   private static double close;
   private static double open;
   private static double poll;
   private static long read_J;
   private static double push;
   private static double pop;
   private static double tick;
   private static volatile double run;
   private static volatile double get;
   private static volatile double set;
   private static double put;
   private static double add;
   private static double clear;
   private static volatile double reset;
   private static volatile double start;
   private static volatile double stop;

   private CoordinateSpoofer() {
   }

   public static boolean isActive() {
      return init_Z && !autismclient.modules.aD.init_Z();
   }

   public static void init(CoordinateSpoofer.Inner_DataUtil m, boolean spoofY) {
      init_V_DataUtil = m == null ? CoordinateSpoofer.Inner_DataUtil.OFFSET : m;
      load_Z = spoofY;
      load_V();
      init_Z = true;
   }

   public static void disable() {
      init_Z = false;
   }

   public static void init(CoordinateSpoofer.Inner_DataUtil m) {
      if (m != null) {
         init_V_DataUtil = m;
      }
   }

   public static void init(boolean spoofY) {
      load_Z = spoofY;
   }

   public static void load_V() {
      init_D = init_D();
      load_D = init(-120, 200);
      read_D = init_D();
      write = load_D();
      update = load_D();
      sync = init_D();
      flush = init_D();
      close = init_SecureRandom.nextDouble() * 3.141592653589793 * 2.0;
      open = init_D();
      poll = init_D();
      read_J = init_SecureRandom.nextLong();
      push = init_D();
      pop = init(-50, 320);
      tick = init_D();
   }

   public static void setCustomCoords(double cx, double cy, double cz) {
      run = cx;
      get = cy;
      set = cz;
      reset = run - put;
      start = get - add;
      stop = set - clear;
   }

   public static void load(double realX, double realY, double realZ) {
      put = realX;
      add = realY;
      clear = realZ;
      reset = run - put;
      start = get - add;
      stop = set - clear;
   }

   public static double[] spoof(double x, double y, double z) {
      if (!init_Z) {
         return new double[]{x, y, z};
      }

      double fx;
      double fy;
      double fz;
      switch (init_V_DataUtil) {
         case SCALED:
            fx = (x - sync) * write + init_D;
            fy = y + load_D;
            fz = (z - flush) * update + read_D;
            break;
         case ROTATED:
            double dx = x - open;
            double dz = z - poll;
            double c = Math.cos(close);
            double s = Math.sin(close);
            fx = dx * c - dz * s + init_D;
            fz = dx * s + dz * c + read_D;
            fy = y + load_D;
            break;
         case SCRAMBLED:
            long rx = Math.floorDiv((long)Math.floor(x), 1024L);
            long ry = Math.floorDiv((long)Math.floor(y), 1024L);
            long rz = Math.floorDiv((long)Math.floor(z), 1024L);
            fx = x + init(read_J, rx, rz, 0);
            fz = z + init(read_J, rx, rz, 1);
            fy = y + init(read_J ^ -7046029254386353131L, ry, 0L, 2);
            break;
         case FROZEN:
            fx = push;
            fy = pop;
            fz = tick;
            break;
         case CUSTOM:
            fx = x + reset;
            fy = y + start;
            fz = z + stop;
            break;
         default:
            fx = x + init_D;
            fy = y + load_D;
            fz = z + read_D;
      }

      if (!load_Z && init_V_DataUtil != CoordinateSpoofer.Inner_DataUtil.FROZEN) {
         fy = y;
      }

      return new double[]{fx, fy, fz};
   }

   private static double init(long key, long a, long b, int axis) {
      long h = key + -7046029254386353131L * (a + 1L);
      h ^= h >>> 29;
      h = h * -4658895280553007687L + b + 1L;
      h ^= h >>> 27;
      h = h * -7723592293110705685L + (axis + 1);
      h ^= h >>> 31;
      return Math.floorMod(h, 20000000L) - 1.0E7;
   }

   private static double init_D() {
      return init_SecureRandom.nextDouble() * 2.0E7 - 1.0E7;
   }

   private static double init(int lo, int hi) {
      return lo + init_SecureRandom.nextInt(hi - lo);
   }

   private static double load_D() {
      double s = 0.5 + init_SecureRandom.nextDouble() * 1.5;
      return init_SecureRandom.nextBoolean() ? s : -s;
   }

   public enum Inner_DataUtil {
      OFFSET,
      SCALED,
      ROTATED,
      SCRAMBLED,
      FROZEN,
      CUSTOM;
   }
}
