package autismclient.mixin;

import autismclient.modules.GoldenLeverModule;
import autismclient.modules.as;
import autismclient.util.MapTooltipPreview;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public class AutismItemStackMixin {
   // One hook, nothing allocated per call. The golden lever's own name is not censored, as before.
   @ModifyReturnValue(method = "getHoverName", at = @At("RETURN"))
   private Component autism$hoverName(Component original) {
      return GoldenLeverModule.init((ItemStack)(Object)this) ? GoldenLeverModule.init_Component() : as.init_Component(original);
   }

   @ModifyReturnValue(method = "getTooltipImage", at = @At("RETURN"))
   private Optional<TooltipComponent> autism$mapPreview(Optional<TooltipComponent> original) {
      return MapTooltipPreview.init((ItemStack)(Object)this, original);
   }
}
