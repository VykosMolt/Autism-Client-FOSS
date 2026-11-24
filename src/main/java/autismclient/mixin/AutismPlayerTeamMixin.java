package autismclient.mixin;

import autismclient.modules.as;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerTeam.class)
public class AutismPlayerTeamMixin {
   @Inject(method = "getFormattedName", at = @At("RETURN"), cancellable = true)
   private void autism$censorTeamName(CallbackInfoReturnable<MutableComponent> cir) {
      if (as.init_Z_33()) {
         MutableComponent original = cir.getReturnValue();
         Component censored = as.load(original);
         if (censored != original) {
            cir.setReturnValue(censored.copy());
         }
      }
   }
}
