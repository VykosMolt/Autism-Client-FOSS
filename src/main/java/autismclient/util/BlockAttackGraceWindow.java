package autismclient.util;

import java.lang.ref.WeakReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class BlockAttackGraceWindow {
   static final int init_I = 2;
   private static WeakReference<LocalPlayer> init_WeakReference = new WeakReference<>(null);
   private static int load = -2147483648;

   private BlockAttackGraceWindow() {
   }

   public static void init(LocalPlayer player) {
      if (player != null) {
         init_WeakReference = new WeakReference<>(player);
         load = player.tickCount;
      }
   }

   public static void init() {
      init_WeakReference = new WeakReference<>(null);
      load = -2147483648;
   }

   public static boolean init(Minecraft mc) {
      return mc != null && mc.player != null && init_WeakReference.get() == mc.player ? init(mc.player.tickCount, load) : false;
   }

   static boolean init(int tick, int noted) {
      if (noted == -2147483648) {
         return false;
      }

      int since = tick - noted;
      return since >= 0 && since <= 2;
   }
}
