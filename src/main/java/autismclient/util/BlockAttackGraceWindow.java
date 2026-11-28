package autismclient.util;

import java.lang.ref.WeakReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class BlockAttackGraceWindow {
   static final int init_I = 2;
   private static WeakReference<LocalPlayer> notedPlayer = new WeakReference<>(null);
   private static int notedTick = -2147483648;

   private BlockAttackGraceWindow() {
   }

   public static void init(LocalPlayer player) {
      if (player != null) {
         notedPlayer = new WeakReference<>(player);
         notedTick = player.tickCount;
      }
   }

   public static void init() {
      notedPlayer = new WeakReference<>(null);
      notedTick = -2147483648;
   }

   public static boolean init(Minecraft mc) {
      return mc != null && mc.player != null && notedPlayer.get() == mc.player ? withinGrace(mc.player.tickCount, notedTick) : false;
   }

   static boolean withinGrace(int tick, int noted) {
      if (noted == -2147483648) {
         return false;
      }

      int since = tick - noted;
      return since >= 0 && since <= 2;
   }
}
