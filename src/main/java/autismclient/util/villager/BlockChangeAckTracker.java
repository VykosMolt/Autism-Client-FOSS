package autismclient.util.villager;

import java.lang.ref.WeakReference;

public final class BlockChangeAckTracker {
   private static WeakReference<Object> trackedLevel = new WeakReference<>(null);
   private static int highestAck = -1;

   private BlockChangeAckTracker() {
   }

   public static void acknowledge(Object level, int value) {
      if (trackedLevel.get() != level) {
         trackedLevel = new WeakReference<>(level);
         highestAck = value;
      } else {
         highestAck = Math.max(highestAck, value);
      }
   }

   public static boolean isAcknowledged(Object level, int value) {
      return value >= 0 && trackedLevel.get() == level && highestAck >= value;
   }
}
