package autismclient.mixin;

import autismclient.modules.aD;
import autismclient.util.cC;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SplashManager.class, priority = 2000)
public abstract class AutismSplashManagerMixin {
   @Inject(method = "prepare", at = @At("HEAD"), cancellable = true)
   private void autism$prepareOnlyVanillaSplashes(ResourceManager manager, ProfilerFiller profiler, CallbackInfoReturnable<List<Component>> cir) {
      if (aD.init_Z()) {
         cir.setReturnValue(cC.init_List(Minecraft.getInstance()));
      }
   }

   @Inject(method = "getSplash", at = @At("HEAD"), cancellable = true)
   private void autism$getOnlyVanillaSplash(CallbackInfoReturnable<SplashRenderer> cir) {
      if (aD.init_Z()) {
         cir.setReturnValue(cC.init_SplashRenderer(Minecraft.getInstance()));
      }
   }
}
