package autismclient.util;

import java.security.SecureRandom;

public final class CoordinateSpoofer {
   private static final SecureRandom RANDOM = new SecureRandom();
   private static final long REGION_SIZE = 1024L;
   private static final long SPOOF_RANGE = 20000000L;
   private static volatile boolean active;
   private static volatile CoordinateSpoofer.SpoofMode mode = CoordinateSpoofer.SpoofMode.OFFSET;
   private static volatile boolean spoofYEnabled = false;
   private static double offsetX;
   private static double offsetY;
   private static double offsetZ;
   private static double scaleX;
   private static double scaleZ;
   private static double scaleOriginX;
   private static double scaleOriginZ;
   private static double rotation;
   private static double rotateOriginX;
   private static double rotateOriginZ;
   private static long noiseSeed;
   private static double fixedX;
   private static double fixedY;
   private static double fixedZ;
   private static volatile double fakeAnchorX;
   private static volatile double fakeAnchorY;
   private static volatile double fakeAnchorZ;
   private static double realAnchorX;
   private static double realAnchorY;
   private static double realAnchorZ;
   private static volatile double deltaX;
   private static volatile double deltaY;
   private static volatile double deltaZ;

   private CoordinateSpoofer() {
   }

   public static boolean isActive() {
      return active && !autismclient.modules.aD.init_Z();
   }

   public static void init(CoordinateSpoofer.SpoofMode m, boolean spoofY) {
      mode = m == null ? CoordinateSpoofer.SpoofMode.OFFSET : m;
      spoofYEnabled = spoofY;
      reroll();
      active = true;
   }

   public static void disable() {
      active = false;
   }

   public static void init(CoordinateSpoofer.SpoofMode m) {
      if (m != null) {
         mode = m;
      }
   }

   public static void setSpoofY(boolean spoofY) {
      spoofYEnabled = spoofY;
   }

   public static void reroll() {
      offsetX = randomOffset();
      offsetY = randomBetween(-120, 200);
      offsetZ = randomOffset();
      scaleX = randomScale();
      scaleZ = randomScale();
      scaleOriginX = randomOffset();
      scaleOriginZ = randomOffset();
      rotation = RANDOM.nextDouble() * 3.141592653589793 * 2.0;
      rotateOriginX = randomOffset();
      rotateOriginZ = randomOffset();
      noiseSeed = RANDOM.nextLong();
      fixedX = randomOffset();
      fixedY = randomBetween(-50, 320);
      fixedZ = randomOffset();
   }

   public static void setCustomCoords(double cx, double cy, double cz) {
      fakeAnchorX = cx;
      fakeAnchorY = cy;
      fakeAnchorZ = cz;
      deltaX = fakeAnchorX - realAnchorX;
      deltaY = fakeAnchorY - realAnchorY;
      deltaZ = fakeAnchorZ - realAnchorZ;
   }

   public static void setRealAnchor(double realX, double realY, double realZ) {
      realAnchorX = realX;
      realAnchorY = realY;
      realAnchorZ = realZ;
      deltaX = fakeAnchorX - realAnchorX;
      deltaY = fakeAnchorY - realAnchorY;
      deltaZ = fakeAnchorZ - realAnchorZ;
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
            fx = (x - scaleOriginX) * scaleX + offsetX;
            fy = y + offsetY;
            fz = (z - scaleOriginZ) * scaleZ + offsetZ;
            break;
         case ROTATED:
            double dx = x - rotateOriginX;
            double dz = z - rotateOriginZ;
            double c = Math.cos(rotation);
            double s = Math.sin(rotation);
            fx = dx * c - dz * s + offsetX;
            fz = dx * s + dz * c + offsetZ;
            fy = y + offsetY;
            break;
         case SCRAMBLED:
            long rx = Math.floorDiv((long)Math.floor(x), 1024L);
            long ry = Math.floorDiv((long)Math.floor(y), 1024L);
            long rz = Math.floorDiv((long)Math.floor(z), 1024L);
            fx = x + hashNoise(noiseSeed, rx, rz, 0);
            fz = z + hashNoise(noiseSeed, rx, rz, 1);
            fy = y + hashNoise(noiseSeed ^ -7046029254386353131L, ry, 0L, 2);
            break;
         case FROZEN:
            fx = fixedX;
            fy = fixedY;
            fz = fixedZ;
            break;
         case CUSTOM:
            fx = x + deltaX;
            fy = y + deltaY;
            fz = z + deltaZ;
            break;
         default:
            fx = x + offsetX;
            fy = y + offsetY;
            fz = z + offsetZ;
      }

      if (!spoofYEnabled && mode != CoordinateSpoofer.SpoofMode.FROZEN) {
         fy = y;
      }

      return new double[]{fx, fy, fz};
   }

   private static double hashNoise(long key, long a, long b, int axis) {
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

   private static double randomBetween(int lo, int hi) {
      return lo + RANDOM.nextInt(hi - lo);
   }

   private static double randomScale() {
      double s = 0.5 + RANDOM.nextDouble() * 1.5;
      return RANDOM.nextBoolean() ? s : -s;
   }

   public enum SpoofMode {
      OFFSET,
      SCALED,
      ROTATED,
      SCRAMBLED,
      FROZEN,
      CUSTOM;
   }
}
