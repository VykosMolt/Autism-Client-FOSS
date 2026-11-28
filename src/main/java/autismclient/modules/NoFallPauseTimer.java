package autismclient.modules;

import net.minecraft.client.Minecraft;

public final class NoFallPauseTimer {
   private static final long init_J = 3000L;
   private static final int init_I = 4;
   private static volatile long expiresAt;
   private static volatile boolean active;
   private static int groundedTicks;

   private NoFallPauseTimer() {
   }

   public static void start() {
      expiresAt = System.currentTimeMillis() + 3000L;
      groundedTicks = 0;
      active = true;
   }

   public static void reset() {
      active = false;
      expiresAt = 0L;
      groundedTicks = 0;
   }

   public static void tick() {
      if (active) {
         Minecraft mc = Minecraft.getInstance();
         boolean grounded = mc.player != null && mc.player.onGround();
         groundedTicks = grounded ? groundedTicks + 1 : 0;
         if (System.currentTimeMillis() >= expiresAt || groundedTicks >= 4) {
            active = false;
         }
      }
   }

   public static boolean isActive() {
      return active;
   }
}
