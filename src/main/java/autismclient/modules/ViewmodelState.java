package autismclient.modules;

import autismclient.util.AutismLiteVariant;

public final class ViewmodelState {
   private static volatile boolean close_Z;
   static volatile boolean mainHand;
   static volatile float mainHandScale;
   static volatile float mainHandX;
   static volatile float mainHandY;
   static volatile float mainHandRotX;
   static volatile float mainHandRotY;
   static volatile float mainHandRotZ;
   static volatile boolean offHand;
   static volatile float offHandScale;
   static volatile float offHandX;
   static volatile float open;
   static volatile float poll;
   static volatile float push;
   static volatile float pop;
   static volatile int swingDuration = 6;
   static volatile int blockingAnimation;
   static volatile float tick = 0.1F;
   static volatile float run = 0.9F;
   static volatile boolean equipOffset = true;
   static volatile boolean ignoreBlocking = true;
   static volatile boolean ignorePlace = true;
   static volatile boolean ignoreAmount;
   static volatile boolean airWalker;

   private ViewmodelState() {
   }

   static void activate() {
      close_Z = true;
   }

   static void deactivate() {
      close_Z = false;
   }

   private static boolean open_Z() {
      return AutismLiteVariant.enabled() || autismclient.e.HashUtil.init();
   }

   public static boolean init_Z() {
      return close_Z && open_Z();
   }

   public static boolean load_Z() {
      return close_Z && open_Z() && mainHand;
   }

   public static float init_F() {
      return mainHandScale;
   }

   public static float load_F() {
      return mainHandX;
   }

   public static float read_F() {
      return mainHandY;
   }

   public static float write_F() {
      return mainHandRotX;
   }

   public static float update_F() {
      return mainHandRotY;
   }

   public static float sync_F() {
      return mainHandRotZ;
   }

   public static boolean read_Z() {
      return close_Z && open_Z() && offHand;
   }

   public static float flush_F() {
      return offHandScale;
   }

   public static float close_F() {
      return offHandX;
   }

   public static float open_F() {
      return open;
   }

   public static float poll() {
      return poll;
   }

   public static float push() {
      return push;
   }

   public static float pop() {
      return pop;
   }

   public static int init_I() {
      return swingDuration;
   }

   public static int load_I() {
      return blockingAnimation;
   }

   public static float tick() {
      return tick;
   }

   public static float run() {
      return run;
   }

   public static boolean write_Z() {
      return close_Z && open_Z() && equipOffset;
   }

   public static boolean update_Z() {
      return ignoreBlocking;
   }

   public static boolean sync_Z() {
      return ignorePlace;
   }

   public static boolean flush_Z() {
      return ignoreAmount;
   }

   public static boolean airWalkerActive() {
      return close_Z && open_Z() && airWalker;
   }
}
