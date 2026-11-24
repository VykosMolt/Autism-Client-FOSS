package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.DoubleSetting;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

public final class e extends Module {
   private static e init;

   public e() {
      super("boat-fly", "BoatFly", ae.load_ae, "Lets ridden vehicles fly.");
      init = this;
      this.add(new BoolSetting("change-forward-speed", "Change Forward Speed", false).description("Use custom forward speed.").build());
      this.add(
         new DoubleSetting("forward-speed", "Forward Speed", 1.0, 0.05, 5.0, 0.05)
            .visibleWhen(() -> this.bool("change-forward-speed"))
            .description("Sets forward speed.")
            .build()
      );
      this.add(new DoubleSetting("upward-speed", "Upward Speed", 0.3, 0.0, 5.0, 0.05).description("Sets upward speed.").build());
   }

   static Vec3 init(Entity vehicle, MoverType type, Vec3 movement) {
      e module = init;
      if (module == null || !module.isEnabled() || aD.read_Z()) {
         return movement;
      }

      if (type == MoverType.SELF && init(vehicle)) {
         double motionX = movement.x;
         double motionY = 0.0;
         double motionZ = movement.z;
         if (MC.options.keyJump.isDown()) {
            motionY = module.decimal("upward-speed");
         } else if (MC.options.keySprint.isDown()) {
            motionY = movement.y;
         }

         if (MC.options.keyUp.isDown() && module.bool("change-forward-speed")) {
            double speed = module.decimal("forward-speed");
            float yawRadians = vehicle.getYRot() * 0.017453292F;
            motionX = Mth.sin(-yawRadians) * speed;
            motionZ = Mth.cos(yawRadians) * speed;
         }

         return new Vec3(motionX, motionY, motionZ);
      } else {
         return movement;
      }
   }

   private static boolean init(Entity vehicle) {
      return MC.player != null && vehicle != null && MC.player.getVehicle() == vehicle;
   }
}
