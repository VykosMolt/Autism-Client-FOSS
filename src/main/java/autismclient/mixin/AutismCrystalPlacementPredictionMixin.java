package autismclient.mixin;

import autismclient.modules.CrystalAuraModule;
import autismclient.util.AutismLiteVariant;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.EndCrystalItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EndCrystalItem.class)
public class AutismCrystalPlacementPredictionMixin {
   @ModifyExpressionValue(
      method = "useOn",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"
      )
   )
   private List<Entity> autism$ownedReplacement(List<Entity> occupants, UseOnContext context) {
      return !AutismLiteVariant.enabled() ? CrystalAuraModule.filterPlacementOccupants(context, occupants) : occupants;
   }
}
