package autismclient.mixin;

import autismclient.ai.engine.AiFallGuard;
import autismclient.modules.AutoTotemModule;
import autismclient.modules.BedDefenderModule;
import autismclient.modules.AntiKnockbackModule;
import autismclient.modules.AutoArmorModule;
import autismclient.modules.SafeWalkModule;
import autismclient.modules.ScaffoldModule;
import autismclient.modules.WTappingModule;
import autismclient.modules.ClutchModule;
import autismclient.modules.DangerModule;
import autismclient.util.AutismLiteVariant;
import autismclient.util.SilentMovementCoordinator;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(KeyboardInput.class)
public abstract class AutismScaffoldInputMixin extends ClientInput {
   @ModifyExpressionValue(method = "tick", at = @At(value = "NEW", target = "(ZZZZZZZ)Lnet/minecraft/world/entity/player/Input;"))
   private Input autism$optionalScaffoldStabilization(Input original) {
      if (!AutismLiteVariant.enabled()) {
         original = WTappingModule.filterInput(original);
      }

      if (!AutismLiteVariant.enabled()) {
         original = AntiKnockbackModule.filterInput(original);
      }

      if (!AutismLiteVariant.enabled()) {
         original = ClutchModule.overrideInput(original);
      }

      Input scaffold = ScaffoldModule.overrideInput(this, original);
      Input aura = SilentMovementCoordinator.rewriteMovementInput(this, scaffold);
      if (!AutismLiteVariant.enabled()) {
         aura = DangerModule.overrideInput(aura);
      }

      Input bed = BedDefenderModule.overrideInput(this, aura);
      Input safe = SafeWalkModule.adjustInput(this, bed);
      if (!AutismLiteVariant.enabled()) {
         safe = AiFallGuard.filterInput(this, safe);
      }

      Input totem = AutoTotemModule.overrideInput(this, safe);
      return AutoArmorModule.overrideInput(this, totem);
   }
}
