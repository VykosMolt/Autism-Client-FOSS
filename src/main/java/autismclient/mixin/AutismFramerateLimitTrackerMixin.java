package autismclient.mixin;

import autismclient.gui.screens.AutismModulesScreen;
import autismclient.gui.screens.AutismTitleScreen;
import autismclient.util.AutismLiteVariant;
import autismclient.util.macro.MacroFpsOverrideManager;
import com.mojang.blaze3d.platform.FramerateLimitTracker;
import com.mojang.blaze3d.platform.FramerateLimitTracker.FramerateThrottleReason;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FramerateLimitTracker.class)
public abstract class AutismFramerateLimitTrackerMixin {
   @Inject(method = "getFramerateLimit", at = @At("RETURN"), cancellable = true)
   private void autism$applyFpsAction(CallbackInfoReturnable<Integer> cir) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft != null && minecraft.level != null) {
         int override = MacroFpsOverrideManager.load_I();
         if (override >= 0) {
            cir.setReturnValue(Math.min(override, cir.getReturnValueI()));
            return;
         }
      }

      if (minecraft != null) {
         boolean ours = !AutismLiteVariant.enabled()
            && (minecraft.gui.screen() instanceof AutismTitleScreen || minecraft.gui.screen() instanceof AutismModulesScreen screen && screen.isTitleSetup());
         if (ours) {
            FramerateLimitTracker tracker = (FramerateLimitTracker)(Object)this;
            if (tracker.getThrottleReason() == FramerateThrottleReason.OUT_OF_LEVEL_MENU) {
               cir.setReturnValue(minecraft.options.framerateLimit().get());
            }
         }
      }
   }
}
