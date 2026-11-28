package autismclient.modules;

import autismclient.util.AutismLiteVariant;

public final class ViewmodelState {
   private static volatile boolean active;
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
   static volatile float offHandY;
   static volatile float offHandRotX;
   static volatile float offHandRotY;
   static volatile float offHandRotZ;
   static volatile int swingDuration = 6;
   static volatile int blockingAnimation;
   static volatile float oneSevenY = 0.1F;
   static volatile float oneSevenSwingScale = 0.9F;
   static volatile boolean equipOffset = true;
   static volatile boolean ignoreBlocking = true;
   static volatile boolean ignorePlace = true;
   static volatile boolean ignoreAmount;
   static volatile boolean airWalker;

   private ViewmodelState() {
   }

   static void activate() {
      active = true;
   }

   static void deactivate() {
      active = false;
   }

   private static boolean isUnlocked() {
      return AutismLiteVariant.enabled() || autismclient.e.HashUtil.init();
   }

   public static boolean isActive() {
      return active && isUnlocked();
   }

   public static boolean isMainHandEnabled() {
      return active && isUnlocked() && mainHand;
   }

   public static float mainHandScale() {
      return mainHandScale;
   }

   public static float mainHandX() {
      return mainHandX;
   }

   public static float mainHandY() {
      return mainHandY;
   }

   public static float mainHandRotX() {
      return mainHandRotX;
   }

   public static float mainHandRotY() {
      return mainHandRotY;
   }

   public static float mainHandRotZ() {
      return mainHandRotZ;
   }

   public static boolean isOffHandEnabled() {
      return active && isUnlocked() && offHand;
   }

   public static float offHandScale() {
      return offHandScale;
   }

   public static float offHandX() {
      return offHandX;
   }

   public static float offHandY() {
      return offHandY;
   }

   public static float poll() {
      return offHandRotX;
   }

   public static float push() {
      return offHandRotY;
   }

   public static float pop() {
      return offHandRotZ;
   }

   public static int swingDuration() {
      return swingDuration;
   }

   public static int blockingAnimation() {
      return blockingAnimation;
   }

   public static float tick() {
      return oneSevenY;
   }

   public static float run() {
      return oneSevenSwingScale;
   }

   public static boolean isEquipOffsetEnabled() {
      return active && isUnlocked() && equipOffset;
   }

   public static boolean ignoreBlocking() {
      return ignoreBlocking;
   }

   public static boolean ignorePlace() {
      return ignorePlace;
   }

   public static boolean ignoreAmount() {
      return ignoreAmount;
   }

   public static boolean airWalkerActive() {
      return active && isUnlocked() && airWalker;
   }
}
