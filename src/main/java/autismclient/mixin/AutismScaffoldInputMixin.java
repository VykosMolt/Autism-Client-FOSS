package autismclient.mixin;

import autismclient.ai.a.PacketUtil;
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
         original = WTappingModule.init(original);
      }

      if (!AutismLiteVariant.enabled()) {
         original = AntiKnockbackModule.init_Input_Input(original);
      }

      if (!AutismLiteVariant.enabled()) {
         original = ClutchModule.init_Input_Input_3(original);
      }

      Input scaffold = ScaffoldModule.init_Input_ClientInput_Input_4(this, original);
      Input aura = SilentMovementCoordinator.init(this, scaffold);
      if (!AutismLiteVariant.enabled()) {
         aura = DangerModule.init_Input_Input_4(aura);
      }

      Input bed = BedDefenderModule.init_Input_ClientInput_Input_2(this, aura);
      Input safe = SafeWalkModule.init(this, bed);
      if (!AutismLiteVariant.enabled()) {
         safe = PacketUtil.init(this, safe);
      }

      Input totem = AutoTotemModule.init_Input_ClientInput_Input(this, safe);
      return AutoArmorModule.init_Input_ClientInput_Input_3(this, totem);
   }
}
