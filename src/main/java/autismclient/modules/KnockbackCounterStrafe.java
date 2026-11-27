package autismclient.modules;

import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;

final class KnockbackCounterStrafe {
   private Vec3 init_Vec3 = Vec3.ZERO;
   private int init_I;
   private boolean init_Z;

   void init(Vec3 motion, boolean jump, boolean counter, int ticks) {
      this.init_Z = jump;
      this.init(motion, counter, ticks);
   }

   void init(Vec3 motion, boolean counter, int ticks) {
      double speed = motion.horizontalDistance();
      this.init_Vec3 = speed > 0.035 ? new Vec3(motion.x / speed, 0.0, motion.z / speed) : Vec3.ZERO;
      this.init_I = counter && speed > 0.035 ? Math.clamp(ticks, 1, 6) : 0;
   }

   Input init(Input own, Vec3 motion, float yaw, boolean canJump) {
      boolean jump = this.init_Z && canJump;
      this.init_Z = false;
      if (this.init_I <= 0) {
         return init(own, jump);
      } else {
         this.init_I--;
         double away = motion.x * this.init_Vec3.x + motion.z * this.init_Vec3.z;
         if (!(away <= 0.035) && !(motion.horizontalDistanceSqr() < 0.001225)) {
            double radians = Math.toRadians(yaw);
            double forward = this.init_Vec3.x * Math.sin(radians) - this.init_Vec3.z * Math.cos(radians);
            double side = -this.init_Vec3.x * Math.cos(radians) - this.init_Vec3.z * Math.sin(radians);
            return new Input(forward > 0.25, forward < -0.25, side > 0.25, side < -0.25, own.jump() || jump, own.shift(), false);
         } else {
            this.init_I = 0;
            return init(own, jump);
         }
      }
   }

   private static Input init(Input input, boolean jump) {
      return jump ? new Input(input.forward(), input.backward(), input.left(), input.right(), true, input.shift(), input.sprint()) : input;
   }

   void reset() {
      this.init_I = 0;
      this.init_Z = false;
      this.init_Vec3 = Vec3.ZERO;
   }

   int init_I() {
      return this.init_I;
   }
}
