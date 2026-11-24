package autismclient.mixin;

import autismclient.modules.PoolEntry;
import autismclient.util.ck;
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
      ck shared = ck.init_ck();
      PoolEntry module = PoolEntry.init_PoolEntry();
      if (shared.sync_Z() || module != null && module.get_Z() && module.size()) {
         cir.setReturnValue(true);
      }
   }
}
