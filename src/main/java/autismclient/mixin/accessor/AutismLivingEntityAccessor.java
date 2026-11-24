package autismclient.mixin.accessor;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface AutismLivingEntityAccessor {
   @Accessor("noJumpDelay")
   int autism$getNoJumpDelay();

   @Accessor("attackStrengthTicker")
   int autism$getAttackStrengthTicker();

   @Accessor("autoSpinAttackDmg")
   float autism$getAutoSpinAttackDmg();

   @Invoker("getDamageAfterArmorAbsorb")
   float autism$getDamageAfterArmorAbsorb(DamageSource var1, float var2);

   @Invoker("getDamageAfterMagicAbsorb")
   float autism$getDamageAfterMagicAbsorb(DamageSource var1, float var2);

   @Invoker("calculateFallDamage")
   int autism$calculateFallDamage(double var1, float var3);

   @Invoker("getJumpPower")
   float autism$getJumpPower();
}
