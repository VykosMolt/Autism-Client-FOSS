package autismclient.ai;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;

final class k {
   private k() {
   }

   static boolean init_Z(Minecraft mc) {
      if (mc.player == null) {
         return false;
      }

      ItemStack shield = mc.player.getOffhandItem();
      return bK.init(shield, 8) && shield.get(DataComponents.BLOCKS_ATTACKS) != null && !mc.player.getCooldowns().isOnCooldown(shield);
   }

   static int init_I(Minecraft mc) {
      BlocksAttacks component = mc.player.getOffhandItem().get(DataComponents.BLOCKS_ATTACKS);
      if (component == null) {
         return 2147483647;
      }

      int elapsed = mc.player.isUsingItem() && mc.player.getUseItem() == mc.player.getOffhandItem() ? mc.player.getTicksUsingItem() : 0;
      return Math.max(0, component.blockDelayTicks() - elapsed);
   }

   static int load(Minecraft mc) {
      if (mc.player == null) {
         return 0;
      }

      BlocksAttacks component = mc.player.getOffhandItem().get(DataComponents.BLOCKS_ATTACKS);
      return component == null ? 0 : Math.max(0, component.blockDelayTicks());
   }

   static boolean init(Minecraft mc, double ticksUntilImpact) {
      if (!init_Z(mc)) {
         return false;
      }

      int remaining = init_I(mc);
      return remaining == 0 || ticksUntilImpact >= remaining + 2;
   }

   static boolean init(Minecraft mc, Entity projectile) {
      if (init_Z(mc) && projectile != null) {
         if (projectile instanceof AbstractArrow arrow && arrow.getPierceLevel() > 0) {
            return false;
         } else {
            String type = BuiltInRegistries.ENTITY_TYPE.getKey(projectile.getType()).getPath();
            boolean var10000;
            switch (type) {
               case "arrow":
               case "spectral_arrow":
               case "trident":
               case "small_fireball":
               case "fireball":
                  var10000 = true;
                  break;
               default:
                  var10000 = false;
            }

            boolean supported = var10000;
            if (!supported) {
               return false;
            }

            Entity owner = projectile instanceof Projectile p ? p.getOwner() : null;
            DamageSource source = type.equals("trident")
               ? mc.level.damageSources().trident(projectile, owner == null ? projectile : owner)
               : (
                  projectile instanceof AbstractArrow arrow
                     ? mc.level.damageSources().arrow(arrow, owner)
                     : (
                        projectile instanceof Fireball fireball
                           ? mc.level.damageSources().fireball(fireball, owner)
                           : mc.level.damageSources().trident(projectile, owner)
                     )
               );
            BlocksAttacks component = mc.player.getOffhandItem().get(DataComponents.BLOCKS_ATTACKS);
            return component.bypassedBy().isPresent() && component.bypassedBy().get().contains(source.typeHolder())
               ? false
               : component.resolveBlockedDamage(source, 1.0F, 0.0) >= 1.0F;
         }
      } else {
         return false;
      }
   }

   static boolean load(Minecraft mc, Entity target) {
      return init(mc, target, null);
   }

   static boolean init(Minecraft mc, Entity target, autismclient.ai.a.DataUtil aim) {
      return mc.player != null
         && target != null
         && target.isAlive()
         && !mc.player.cannotAttackWithItem(mc.player.getMainHandItem(), 0)
         && mc.player.isWithinAttackRange(mc.player.getMainHandItem(), target.getBoundingBox(), 0.0)
         && (aim == null ? autismclient.ai.a.DataUtil.init_Z_Entity(target) : aim.load_Z_Entity(target));
   }

   static double init_D(Minecraft mc) {
      return mc.player == null ? 0.0 : mc.player.getAttackRangeWith(mc.player.getMainHandItem()).effectiveMaxRange(mc.player);
   }
}
