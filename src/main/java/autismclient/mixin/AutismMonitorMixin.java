package autismclient.mixin;

import autismclient.compat263.CompatGLFW;
import com.mojang.blaze3d.platform.Monitor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Monitor.class)
public class AutismMonitorMixin {
   @Redirect(method = "queryMonitorName", at = @At(value = "INVOKE", target = "Lorg/lwjgl/sdl/SDLVideo;SDL_GetDisplayName(I)Ljava/lang/String;"), require = 0)
   private static String autism$safeMonitorName(int displayId) {
      try {
         String text = CompatGLFW.glfwGetMonitorName(displayId);
         if (text != null && !text.isEmpty()) {
            return text;
         }
      } catch (Throwable var2) {
      }

      return "unknown";
   }
}
