package autismclient.mixin;

import autismclient.AutismClientAddon;
import autismclient.util.macro.MacroFpsOverrideManager;
import java.util.concurrent.locks.LockSupport;
import net.minecraft.client.FramerateLimiter;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FramerateLimiter.class)
public abstract class AutismFramerateLimiterMixin {
   private static final long FREEZE_RECHECK_NANOS = 20000000L;

   @Inject(method = "limitDisplayFPS", at = @At("HEAD"), cancellable = true)
   private static void autism$freezeWhenZero(int framerateLimit, CallbackInfo ci) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft != null && minecraft.level != null && minecraft.gui.screen() == null && MacroFpsOverrideManager.load_Z()) {
         boolean logged = false;

         while (MacroFpsOverrideManager.load_Z() && Minecraft.getInstance().level != null && Minecraft.getInstance().gui.screen() == null) {
            if (!logged) {
               logged = true;
               AutismClientAddon.LOG.warn("[FPS] Render freeze engaged (macro FPS 0); auto-releases on screen open or failsafe expiry");
            }

            LockSupport.parkNanos(20000000L);
            if (Thread.interrupted()) {
               break;
            }
         }

         ci.cancel();
      }
   }
}
