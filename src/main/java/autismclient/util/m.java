package autismclient.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Pose;

public final class m {
   private static final double init_D = 25.0;
   private static volatile boolean init_Z;
   private static volatile long init_J = -9223372036854775808L;

   private m() {
   }

   public static void init_V() {
      if (!AutismLiteVariant.enabled() && !autismclient.e.RefQueue.init()) {
         AutismClientMessaging.sendPrefixed("§cAutism is locked: early access requires the donator role. Sign in to unlock.");
      } else {
         Minecraft mc = Minecraft.getInstance();
         LocalPlayer player = mc.player;
         if (player != null) {
            init_J = player.getSleepingPos().map(BlockPos::asLong).orElse(-9223372036854775808L);
            init_Z = true;
            init(player);
            load(mc);
         }
      }
   }

   public static void load() {
      init_Z = false;
      init_J = -9223372036854775808L;
   }

   public static boolean init_Z() {
      return init_Z;
   }

   public static void init(Minecraft mc) {
      if (init_Z) {
         LocalPlayer player = mc == null ? null : mc.player;
         if (player == null) {
            load();
         } else if (player.isSleeping()) {
            long current = player.getSleepingPos().map(BlockPos::asLong).orElse(-9223372036854775808L);
            if (current != init_J) {
               load();
               return;
            }

            init(player);
            load(mc);
         } else if (init_J != -9223372036854775808L && player.blockPosition().distSqr(BlockPos.of(init_J)) > 25.0) {
            load();
         }
      }
   }

   private static void init(LocalPlayer player) {
      player.clearSleepingPos();
      player.setPose(Pose.STANDING);
   }

   private static void load(Minecraft mc) {
      if (mc.gui.screen() instanceof InBedChatScreen) {
         mc.gui.setScreen(null);
      }
   }
}
