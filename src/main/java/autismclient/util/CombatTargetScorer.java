package autismclient.util;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class CombatTargetScorer {
   private CombatTargetScorer() {
   }

   public static double init(LivingEntity target, Player player, RotationMath.Rotation look) {
      Vec3 eyes = player.getEyePosition();
      double angle = RotationMath.vectorAngleDegrees(look, RotationMath.lookAt(target.getBoundingBox().getCenter(), eyes));
      double distance = Math.sqrt(target.getBoundingBox().inflate(target.getPickRadius()).distanceToSqr(eyes));
      double score = angle / 40.0 + distance / 1.5;
      if (!(target instanceof Player)) {
         score += 6.0;
      }

      DamageSource source = player.getLastDamageSource();
      if (source != null && source.getEntity() == target) {
         score -= 8.0;
      }

      return score;
   }
}
