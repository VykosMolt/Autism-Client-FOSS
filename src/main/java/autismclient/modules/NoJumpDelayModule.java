package autismclient.modules;

import net.minecraft.world.entity.LivingEntity;

public final class NoJumpDelayModule extends Module {
   public NoJumpDelayModule() {
      super("no-jump-delay", "NoJumpDelay", ae.load_ae, "Remove the cooldown between grounded jumps.");
   }

   public static boolean init(LivingEntity entity) {
      return entity == MC.player && entity.onGround() && !aD.read_Z() && al.init_Z_Str("no-jump-delay");
   }
}
