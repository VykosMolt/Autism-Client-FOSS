package autismclient.modules;

import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

public final class AirStuckModule extends Module {
   public AirStuckModule() {
      super("air-stuck", "AirStuck", ae.load_ae, "Freeze your position while allowing mouse look.");
   }

   @Override
   public void preMovementTick() {
      if (this.init_Z()) {
         MC.player.setDeltaMovement(Vec3.ZERO);
      }
   }

   @Override
   public Vec3 onPlayerMove(MoverType type, Vec3 movement) {
      if (!this.init_Z()) {
         return movement;
      }

      MC.player.setDeltaMovement(Vec3.ZERO);
      return Vec3.ZERO;
   }

   private boolean init_Z() {
      return this.isEnabled() && MC.player != null && MC.player.isAlive() && !MC.player.isPassenger() && !MC.player.isSpectator() && !aD.read_Z();
   }

   public static void init_V() {
      if (al.init_Module_Str("air-stuck") instanceof AirStuckModule module && module.isEnabled()) {
         module.disableWithToggleMessage("AirStuck disabled after a server position correction.");
      }
   }
}
