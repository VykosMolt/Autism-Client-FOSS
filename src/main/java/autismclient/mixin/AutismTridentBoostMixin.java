package autismclient.mixin;

import autismclient.modules.TridentBoostModule;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TridentItem.class)
public abstract class AutismTridentBoostMixin {
   @WrapOperation(method = {"use", "releaseUsing"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isInWaterOrRain()Z"))
   private boolean autism$allowDryRiptide(Player player, Operation<Boolean> original) {
      return original.call(player) || TridentBoostModule.canBoost(player);
   }

   @Inject(method = "releaseUsing", at = @At("RETURN"))
   private void autism$noteDryRiptide(ItemStack stack, Level level, LivingEntity entity, int remaining, CallbackInfoReturnable<Boolean> cir) {
      if (cir.getReturnValueZ()
         && entity instanceof Player player
         && player.isAutoSpinAttack()
         && EnchantmentHelper.getTridentSpinAttackStrength(stack, player) > 0.0F
         && !player.isInWaterOrRain()) {
         TridentBoostModule.markBoosted(player);
      }
   }
}
