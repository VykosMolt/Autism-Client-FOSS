package autismclient.util.h;

import java.lang.ref.WeakReference;

public final class BlockChangeAckTracker {
   private static WeakReference<Object> init_WeakReference = new WeakReference<>(null);
   private static int init_I = -1;

   private BlockChangeAckTracker() {
   }

   public static void init_V(Object level, int value) {
      if (init_WeakReference.get() != level) {
         init_WeakReference = new WeakReference<>(level);
         init_I = value;
      } else {
         init_I = Math.max(init_I, value);
      }
   }

   public static boolean init_Z(Object level, int value) {
      return value >= 0 && init_WeakReference.get() == level && init_I >= value;
   }
}
