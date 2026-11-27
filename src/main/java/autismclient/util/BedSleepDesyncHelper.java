package autismclient.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Pose;

public final class BedSleepDesyncHelper {
   private static final double init_D = 25.0;
   private static volatile boolean init_Z;
   private static volatile long sleepingPos = -9223372036854775808L;

   private BedSleepDesyncHelper() {
   }

   public static void wakeUp() {
      if (!AutismLiteVariant.enabled() && !autismclient.e.RefQueue.init()) {
         AutismClientMessaging.sendPrefixed("§cAutism is locked: early access requires the donator role. Sign in to unlock.");
      } else {
         Minecraft mc = Minecraft.getInstance();
         LocalPlayer player = mc.player;
         if (player != null) {
            sleepingPos = player.getSleepingPos().map(BlockPos::asLong).orElse(-9223372036854775808L);
            init_Z = true;
            init(player);
            load(mc);
         }
      }
   }

   public static void load() {
      init_Z = false;
      sleepingPos = -9223372036854775808L;
   }

   public static boolean isClientAwake() {
      return init_Z;
   }

   public static void init(Minecraft mc) {
      if (init_Z) {
         LocalPlayer player = mc == null ? null : mc.player;
         if (player == null) {
            load();
         } else if (player.isSleeping()) {
            long current = player.getSleepingPos().map(BlockPos::asLong).orElse(-9223372036854775808L);
            if (current != sleepingPos) {
               load();
               return;
            }

            init(player);
            load(mc);
         } else if (sleepingPos != -9223372036854775808L && player.blockPosition().distSqr(BlockPos.of(sleepingPos)) > 25.0) {
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
