package autismclient.mixin.security;

import autismclient.protector.PacketComponentOriginTracker;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.ResolutionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ComponentUtils.class)
public class AutismComponentUtilsMixin {
   @Inject(
      method = "resolve(Lnet/minecraft/network/chat/ResolutionContext;Lnet/minecraft/network/chat/Component;I)Lnet/minecraft/network/chat/MutableComponent;",
      at = @At("RETURN")
   )
   private static void autism$propagateResolution(
      ResolutionContext context, Component component, int recursionDepth, CallbackInfoReturnable<MutableComponent> cir
   ) {
      PacketComponentOriginTracker.init(component, cir.getReturnValue());
   }
}
