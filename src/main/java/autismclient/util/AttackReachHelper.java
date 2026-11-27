package autismclient.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class AttackReachHelper {
   private AttackReachHelper() {
   }

   public static AttackRange attackRange(LivingEntity attacker) {
      return attacker.getAttackRangeWith(attacker.getMainHandItem());
   }

   public static double maxReach(LivingEntity attacker) {
      AttackRange range = attackRange(attacker);
      return range.effectiveMaxRange(attacker) + range.hitboxMargin();
   }

   public static double init(LivingEntity attacker, Vec3 look) {
      AttackRange range = attackRange(attacker);
      double forward = attacker.getMainHandItem().has(DataComponents.PIERCING_WEAPON) ? Math.max(0.0, attacker.getKnownMovement().dot(look)) : 0.0;
      return range.effectiveMaxRange(attacker) + forward;
   }

   public static boolean init(Vec3 eyes, AABB box, Vec3 ownStep, Vec3 targetStep, double reach) {
      return box.distanceToSqr(eyes) > reach * reach ? false : box.move(targetStep).distanceToSqr(eyes.add(ownStep)) > reach * reach;
   }
}
