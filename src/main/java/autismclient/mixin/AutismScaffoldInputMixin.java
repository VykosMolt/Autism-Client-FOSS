package autismclient.mixin;

import autismclient.ai.a.PacketUtil;
import autismclient.modules.FieldCache;
import autismclient.modules.ListPool;
import autismclient.modules.StateCache;
import autismclient.modules.TableIndex;
import autismclient.modules.aH;
import autismclient.modules.aI;
import autismclient.modules.bg;
import autismclient.modules.j;
import autismclient.modules.z;
import autismclient.util.AutismLiteVariant;
import autismclient.util.cn;
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
         original = bg.init(original);
      }

      if (!AutismLiteVariant.enabled()) {
         original = StateCache.init_Input_Input(original);
      }

      if (!AutismLiteVariant.enabled()) {
         original = j.init_Input_Input_3(original);
      }

      Input scaffold = aI.init_Input_ClientInput_Input_4(this, original);
      Input aura = cn.init(this, scaffold);
      if (!AutismLiteVariant.enabled()) {
         aura = z.init_Input_Input_4(aura);
      }

      Input bed = ListPool.init_Input_ClientInput_Input_2(this, aura);
      Input safe = aH.init(this, bed);
      if (!AutismLiteVariant.enabled()) {
         safe = PacketUtil.init(this, safe);
      }

      Input totem = FieldCache.init_Input_ClientInput_Input(this, safe);
      return TableIndex.init_Input_ClientInput_Input_3(this, totem);
   }
}
