package autismclient.mixin.accessor;

import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractArrow.class)
public interface AutismDangerArrowAccessor {
   @Invoker("isInGround")
   boolean autism$inGround();
}
