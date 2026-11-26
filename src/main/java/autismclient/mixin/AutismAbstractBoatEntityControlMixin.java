package autismclient.mixin;

import autismclient.modules.EntityControlModule;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractBoat.class)
public abstract class AutismAbstractBoatEntityControlMixin {
   @ModifyExpressionValue(
      method = "controlBoat",
      at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/vehicle/boat/AbstractBoat;inputLeft:Z", opcode = 180)
   )
   private boolean autism$lockLeftTurn(boolean original) {
      return EntityControlModule.init_Z_7() ? false : original;
   }

   @ModifyExpressionValue(
      method = "controlBoat",
      at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/vehicle/boat/AbstractBoat;inputRight:Z", opcode = 180)
   )
   private boolean autism$lockRightTurn(boolean original) {
      return EntityControlModule.init_Z_7() ? false : original;
   }
}
