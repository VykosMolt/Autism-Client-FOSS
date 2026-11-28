package autismclient.modules;

import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;

final class KnockbackCounterStrafe {
   private Vec3 direction = Vec3.ZERO;
   private int ticksLeft;
   private boolean jump;

   void init(Vec3 motion, boolean jump, boolean counter, int ticks) {
      this.jump = jump;
      this.init(motion, counter, ticks);
   }

   void init(Vec3 motion, boolean counter, int ticks) {
      double speed = motion.horizontalDistance();
      this.direction = speed > 0.035 ? new Vec3(motion.x / speed, 0.0, motion.z / speed) : Vec3.ZERO;
      this.ticksLeft = counter && speed > 0.035 ? Math.clamp(ticks, 1, 6) : 0;
   }

   Input init(Input own, Vec3 motion, float yaw, boolean canJump) {
      boolean jump = this.jump && canJump;
      this.jump = false;
      if (this.ticksLeft <= 0) {
         return withJump(own, jump);
      } else {
         this.ticksLeft--;
         double away = motion.x * this.direction.x + motion.z * this.direction.z;
         if (!(away <= 0.035) && !(motion.horizontalDistanceSqr() < 0.001225)) {
            double radians = Math.toRadians(yaw);
            double forward = this.direction.x * Math.sin(radians) - this.direction.z * Math.cos(radians);
            double side = -this.direction.x * Math.cos(radians) - this.direction.z * Math.sin(radians);
            return new Input(forward > 0.25, forward < -0.25, side > 0.25, side < -0.25, own.jump() || jump, own.shift(), false);
         } else {
            this.ticksLeft = 0;
            return withJump(own, jump);
         }
      }
   }

   private static Input withJump(Input input, boolean jump) {
      return jump ? new Input(input.forward(), input.backward(), input.left(), input.right(), true, input.shift(), input.sprint()) : input;
   }

   void reset() {
      this.ticksLeft = 0;
      this.jump = false;
      this.direction = Vec3.ZERO;
   }

   int init_I() {
      return this.ticksLeft;
   }
}
