package autismclient.util;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class PlayerMotionEnvelope {
   private static final AABB INFINITE = new AABB(-1.0 / 0.0, -1.0 / 0.0, -1.0 / 0.0, 1.0 / 0.0, 1.0 / 0.0, 1.0 / 0.0);

   private PlayerMotionEnvelope() {
   }

   public static AABB nextTickBox(LocalPlayer player) {
      double speed = Math.max(player.getSpeed(), player.getAttributeValue(Attributes.MOVEMENT_SPEED));
      if (!player.isSprinting()) {
         speed *= 1.3;
      }

      double acceleration;
      if (player.onGround()) {
         float friction = player.level().getBlockState(player.getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction();
         acceleration = speed * (0.21600002F / (friction * friction * friction)) * 0.9800000190734863;
      } else {
         acceleration = player.isSprinting() ? 0.026 : 0.02;
      }

      if (player.isInWater() || player.isInLava() || player.onClimbable() || player.getAbilities().flying) {
         acceleration = Math.max(acceleration, speed);
      }

      return expand(player.getBoundingBox(), player.getDeltaMovement(), acceleration);
   }

   public static AABB expand(AABB body, Vec3 motion, double acceleration) {
      return Double.isFinite(acceleration) && !(acceleration < 0.0) && Double.isFinite(motion.x) && Double.isFinite(motion.y) && Double.isFinite(motion.z)
         ? body.move(motion).inflate(acceleration, 0.0, acceleration)
         : INFINITE;
   }
}
