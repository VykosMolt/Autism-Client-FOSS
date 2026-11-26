package autismclient.modules;

import net.minecraft.client.Minecraft;

public final class NoFallPauseTimer {
   private static final long init_J = 3000L;
   private static final int init_I = 4;
   private static volatile long load_J;
   private static volatile boolean init_Z;
   private static int load_I;

   private NoFallPauseTimer() {
   }

   public static void init_V() {
      load_J = System.currentTimeMillis() + 3000L;
      load_I = 0;
      init_Z = true;
   }

   public static void load() {
      init_Z = false;
      load_J = 0L;
      load_I = 0;
   }

   public static void read() {
      if (init_Z) {
         Minecraft mc = Minecraft.getInstance();
         boolean grounded = mc.player != null && mc.player.onGround();
         load_I = grounded ? load_I + 1 : 0;
         if (System.currentTimeMillis() >= load_J || load_I >= 4) {
            init_Z = false;
         }
      }
   }

   public static boolean init_Z() {
      return init_Z;
   }
}
