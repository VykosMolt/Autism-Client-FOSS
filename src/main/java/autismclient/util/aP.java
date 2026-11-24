package autismclient.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.entity.player.Input;

public final class aP {
   private aP() {
   }

   public static boolean init(String mode) {
      return "Packet".equalsIgnoreCase(mode);
   }

   public static void init(Minecraft mc, String mode) {
      if (mc != null) {
         if (init(mode)) {
            load(mc, true);
         } else if (mc.options != null && mc.options.keyShift != null) {
            mc.options.keyShift.setDown(true);
         }
      }
   }

   public static void load(Minecraft mc, String mode) {
      if (mc != null) {
         if (init(mode)) {
            load(mc, false);
         }

         if (mc.options != null && mc.options.keyShift != null) {
            mc.options.keyShift.setDown(false);
         }
      }
   }

   public static void init(Minecraft mc, boolean sneak) {
      if (mc != null) {
         if (mc.options != null && mc.options.keyShift != null) {
            mc.options.keyShift.setDown(sneak);
         }

         if (mc.player != null) {
            mc.player.setShiftKeyDown(sneak);
         }

         load(mc, sneak);
      }
   }

   public static void load(Minecraft mc, boolean sneak) {
      if (!autismclient.modules.aD.read_Z() && mc != null && mc.options != null && mc.getConnection() != null) {
         Input input = new Input(
            mc.options.keyUp.isDown(),
            mc.options.keyDown.isDown(),
            mc.options.keyLeft.isDown(),
            mc.options.keyRight.isDown(),
            mc.options.keyJump.isDown(),
            sneak,
            mc.options.keySprint.isDown()
         );
         mc.getConnection().send(new ServerboundPlayerInputPacket(input));
      }
   }
}
