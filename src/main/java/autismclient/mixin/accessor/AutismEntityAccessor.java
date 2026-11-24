package autismclient.mixin.accessor;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface AutismEntityAccessor {
   @Invoker("isInvulnerableToBase")
   boolean autism$isInvulnerableToBase(DamageSource var1);

   @Accessor("position")
   void autism$setPosition(Vec3 var1);

   @Invoker("getInputVector")
   static Vec3 autism$getInputVector(Vec3 relative, float motion, float facing) {
      throw new AssertionError();
   }
}
