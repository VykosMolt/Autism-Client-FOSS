package autismclient.mixin;

import autismclient.modules.ModuleRuntimeManager;
import autismclient.util.AutismSessionState;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ResultSlot.class)
public abstract class AutismResultSlotMixin {
   @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
   private void autism$xcarryMayPlace(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
      AutismSessionState shared = AutismSessionState.getInstance();
      ModuleRuntimeManager module = ModuleRuntimeManager.getInstance();
      if (shared.isXCarryForced() || module != null && module.isXCarryEnabled() && module.isXCarryUseCrafting()) {
         cir.setReturnValue(true);
      }
   }
}
