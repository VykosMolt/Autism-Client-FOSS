package autismclient.util;

import java.security.SecureRandom;

public final class CoordinateSpoofer {
   private static final SecureRandom RANDOM = new SecureRandom();
   private static final long init_J = 1024L;
   private static final long load_J = 20000000L;
   private static volatile boolean active;
   private static volatile CoordinateSpoofer.Inner_DataUtil mode = CoordinateSpoofer.Inner_DataUtil.OFFSET;
   private static volatile boolean spoofYEnabled = false;
   private static double offsetX;
   private static double offsetY;
   private static double offsetZ;
   private static double write;
   private static double update;
   private static double sync;
   private static double flush;
   private static double close;
   private static double open;
   private static double poll;
   private static long noiseSeed;
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
      return active && !autismclient.modules.aD.init_Z();
   }

   public static void init(CoordinateSpoofer.Inner_DataUtil m, boolean spoofY) {
      mode = m == null ? CoordinateSpoofer.Inner_DataUtil.OFFSET : m;
      spoofYEnabled = spoofY;
      reroll();
      active = true;
   }

   public static void disable() {
      active = false;
   }

   public static void init(CoordinateSpoofer.Inner_DataUtil m) {
      if (m != null) {
         mode = m;
      }
   }

   public static void init(boolean spoofY) {
      spoofYEnabled = spoofY;
   }

   public static void reroll() {
      offsetX = randomOffset();
      offsetY = init(-120, 200);
      offsetZ = randomOffset();
      write = randomScale();
      update = randomScale();
      sync = randomOffset();
      flush = randomOffset();
      close = RANDOM.nextDouble() * 3.141592653589793 * 2.0;
      open = randomOffset();
      poll = randomOffset();
      noiseSeed = RANDOM.nextLong();
      push = randomOffset();
      pop = init(-50, 320);
      tick = randomOffset();
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
      if (!active) {
         return new double[]{x, y, z};
      }

      double fx;
      double fy;
      double fz;
      switch (mode) {
         case SCALED:
            fx = (x - sync) * write + offsetX;
            fy = y + offsetY;
            fz = (z - flush) * update + offsetZ;
            break;
         case ROTATED:
            double dx = x - open;
            double dz = z - poll;
            double c = Math.cos(close);
            double s = Math.sin(close);
            fx = dx * c - dz * s + offsetX;
            fz = dx * s + dz * c + offsetZ;
            fy = y + offsetY;
            break;
         case SCRAMBLED:
            long rx = Math.floorDiv((long)Math.floor(x), 1024L);
            long ry = Math.floorDiv((long)Math.floor(y), 1024L);
            long rz = Math.floorDiv((long)Math.floor(z), 1024L);
            fx = x + init(noiseSeed, rx, rz, 0);
            fz = z + init(noiseSeed, rx, rz, 1);
            fy = y + init(noiseSeed ^ -7046029254386353131L, ry, 0L, 2);
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
            fx = x + offsetX;
            fy = y + offsetY;
            fz = z + offsetZ;
      }

      if (!spoofYEnabled && mode != CoordinateSpoofer.Inner_DataUtil.FROZEN) {
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

   private static double randomOffset() {
      return RANDOM.nextDouble() * 2.0E7 - 1.0E7;
   }

   private static double init(int lo, int hi) {
      return lo + RANDOM.nextInt(hi - lo);
   }

   private static double randomScale() {
      double s = 0.5 + RANDOM.nextDouble() * 1.5;
      return RANDOM.nextBoolean() ? s : -s;
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
