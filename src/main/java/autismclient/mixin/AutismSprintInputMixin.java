package autismclient.mixin;

import autismclient.ai.a.AiMotionSafety;
import autismclient.modules.aI;
import autismclient.modules.ai;
import autismclient.modules.bg;
import autismclient.util.AutismLiteVariant;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(KeyboardInput.class)
public abstract class AutismSprintInputMixin {
   @ModifyExpressionValue(method = "tick", at = @At(value = "NEW", target = "(ZZZZZZZ)Lnet/minecraft/world/entity/player/Input;"))
   private Input autism$sprintInputRecord(Input original) {
      if (!AutismLiteVariant.enabled() && (bg.init_Z_36() || AiMotionSafety.putAt_Z())) {
         return new Input(original.forward(), original.backward(), original.left(), original.right(), original.jump(), original.shift(), false);
      } else if (original.forward() && !aI.close_Z_4()) {
         return !original.sprint() && ai.init(false, false)
            ? new Input(original.forward(), original.backward(), original.left(), original.right(), original.jump(), original.shift(), true)
            : original;
      } else {
         return original;
      }
   }
}
