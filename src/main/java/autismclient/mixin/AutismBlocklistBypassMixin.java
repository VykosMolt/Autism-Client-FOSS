package autismclient.mixin;

import autismclient.util.AutismAddressCheckHelper;
import net.minecraft.client.multiplayer.resolver.AddressCheck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AddressCheck.class)
public interface AutismBlocklistBypassMixin {
   @Inject(method = "createFromService", at = @At("HEAD"), cancellable = true, require = 0)
   private static void autism$starveBlocklist(CallbackInfoReturnable<AddressCheck> cir) {
      cir.setReturnValue(AutismAddressCheckHelper.createPermissive());
   }
}
