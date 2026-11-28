package autismclient.util;

import java.util.function.LongConsumer;

public final class ClickRateTracker {
   private static final long init_J = 1000000000L;
   private static final int HISTORY_SIZE = 48;
   private static final long[] init_JArr = new long[48];
   private static final long[] load_JArr = new long[48];
   private static int load_I;
   private static int rightIndex;

   private ClickRateTracker() {
   }

   public static void recordLeftClick() {
      init_JArr[load_I] = System.nanoTime();
      load_I = (load_I + 1) % 48;
   }

   public static void recordRightClick() {
      load_JArr[rightIndex] = System.nanoTime();
      rightIndex = (rightIndex + 1) % 48;
   }

   public static int leftCps() {
      return countLastSecond(init_JArr);
   }

   public static int rightCps() {
      return countLastSecond(load_JArr);
   }

   public static int totalCps() {
      return leftCps() + rightCps();
   }

   public static boolean init(long ms) {
      return clickedWithin(init_JArr, load_I, ms);
   }

   public static boolean recentUse(long ms) {
      return clickedWithin(load_JArr, rightIndex, ms);
   }

   public static void forEachClickAfter(long after, LongConsumer sink) {
      for (int i = 0; i < 48; i++) {
         long at = init_JArr[(load_I + i) % 48];
         if (at > after) {
            sink.accept(at);
         }
      }
   }

   private static boolean clickedWithin(long[] ring, int idx, long ms) {
      long newest = ring[(idx + 48 - 1) % 48];
      return newest != 0L && newest > System.nanoTime() - ms * 1000000L;
   }

   private static int countLastSecond(long[] ring) {
      long cutoff = System.nanoTime() - 1000000000L;
      int c = 0;

      for (int i = 0; i < 48; i++) {
         if (ring[i] > cutoff) {
            c++;
         }
      }

      return c;
   }
}
