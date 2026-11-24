package autismclient.mixin.security;

import autismclient.g.TaskTimer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentInitializers.SingleComponentInitializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SingleComponentInitializer.class)
public interface AutismDataComponentInitializerMixin {
   @WrapOperation(
      method = "lambda$asInitializer$0",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/core/component/DataComponentInitializers$SingleComponentInitializer;create(Lnet/minecraft/core/HolderLookup$Provider;)Ljava/lang/Object;"
      ),
      require = 0
   )
   private static Object autism$skipMissingRemoteComponent(SingleComponentInitializer<?> self, Provider context, Operation<Object> original) {
      try {
         return original.call(self, context);
      } catch (RuntimeException error) {
         if (!TaskTimer.init_Z(error)) {
            throw error;
         }

         TaskTimer.init_V(error);
         return null;
      }
   }
}
