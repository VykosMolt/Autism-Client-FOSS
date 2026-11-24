package autismclient.mixin.accessor;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FishingHook.class)
public interface AutismFishingHookAccessor {
   @Accessor("biting")
   boolean autism$isBiting();

   @Accessor("DATA_HOOKED_ENTITY")
   static EntityDataAccessor<Integer> autism$getHookedEntityData() {
      throw new AssertionError();
   }
}
