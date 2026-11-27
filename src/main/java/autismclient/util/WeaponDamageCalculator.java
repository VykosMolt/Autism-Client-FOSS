package autismclient.util;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class WeaponDamageCalculator {
   private WeaponDamageCalculator() {
   }

   public static double init(Player player, ItemStack stack) {
      AttributeInstance live = player.getAttribute(Attributes.ATTACK_DAMAGE);
      if (live == null) {
         return 0.0;
      }

      AttributeInstance candidate = new AttributeInstance(Attributes.ATTACK_DAMAGE, ignored -> {});
      candidate.replaceFrom(live);
      player.getMainHandItem().forEachModifier(EquipmentSlot.MAINHAND, (type, modifier) -> {
         if (type.equals(Attributes.ATTACK_DAMAGE)) {
            candidate.removeModifier(modifier.id());
         }
      });
      stack.forEachModifier(EquipmentSlot.MAINHAND, (type, modifier) -> {
         if (type.equals(Attributes.ATTACK_DAMAGE)) {
            candidate.removeModifier(modifier.id());
            candidate.addTransientModifier(modifier);
         }
      });
      init(candidate, player.getActiveEffects());
      return candidate.getValue();
   }

   static void init(AttributeInstance candidate, Iterable<MobEffectInstance> effects) {
      for (Holder<MobEffect> effect : List.of(MobEffects.STRENGTH, MobEffects.WEAKNESS)) {
         effect.value().createModifiers(0, (attribute, modifier) -> {
            if (attribute.equals(Attributes.ATTACK_DAMAGE)) {
               candidate.removeModifier(modifier.id());
            }
         });
      }

      for (MobEffectInstance effect : effects) {
         effect.getEffect().value().createModifiers(effect.getAmplifier(), (attribute, modifier) -> {
            if (attribute.equals(Attributes.ATTACK_DAMAGE)) {
               candidate.removeModifier(modifier.id());
               candidate.addTransientModifier(modifier);
            }
         });
      }
   }

   public static HitResult init() {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      Vec3 eye = player.getEyePosition();
      Vec3 look = Vec3.directionFromRotation(player.getXRot(), player.getYRot());
      double blockRange = player.blockInteractionRange();
      double entityRange = player.entityInteractionRange();
      double range = Math.max(blockRange, entityRange);
      Vec3 end = eye.add(look.scale(range));
      BlockHitResult block = mc.level.clip(new ClipContext(eye, end, Block.OUTLINE, Fluid.NONE, player));
      double obstruction = block.getType() == Type.BLOCK ? eye.distanceToSqr(block.getLocation()) : range * range;
      EntityHitResult entity = ProjectileUtil.getEntityHitResult(
         player,
         eye,
         end,
         player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0),
         target -> !target.isSpectator() && target.isPickable(),
         Math.min(obstruction, entityRange * entityRange)
      );
      if (entity != null) {
         return entity;
      } else {
         return block.getType() == Type.BLOCK && obstruction <= blockRange * blockRange ? block : null;
      }
   }

   public static boolean init(Vec3 source, float power, ExplosionDamageCalculator.SimOptions options, double maximum) {
      Minecraft mc = Minecraft.getInstance();

      for (Player friend : mc.level.players()) {
         if (friend != mc.player
            && friend.isAlive()
            && !friend.isSpectator()
            && autismclient.modules.TeamsModule.init_Z_Entity_5(friend)
            && !(friend.distanceToSqr(source) > 4.0F * power * power)) {
            double damage = ExplosionDamageCalculator.calculateDamageCached(friend, source, power, options);
            if (!init_Z_D(damage, maximum, ExplosionDamageCalculator.effectiveHealth(friend))) {
               return false;
            }
         }
      }

      return true;
   }

   static boolean init_Z_D(double damage, double maximum, double health) {
      return Double.isFinite(damage) && damage <= maximum && (damage == 0.0 || damage < health);
   }

   public static boolean init(double target, double self, double minimumTarget, double maximumSelf, boolean efficient) {
      return Double.isFinite(target)
         && Double.isFinite(self)
         && target >= 0.0
         && self >= 0.0
         && Double.isFinite(minimumTarget)
         && Double.isFinite(maximumSelf)
         && target >= minimumTarget
         && self <= maximumSelf
         && (!efficient || target > self);
   }

   public static boolean init_Z_I(int local, int received, boolean offhandFuel) {
      return local >= 1 && local <= 4 && local == received && (!offhandFuel || received == 4);
   }
}
