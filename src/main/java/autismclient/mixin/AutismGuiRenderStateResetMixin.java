package autismclient.mixin;

import autismclient.gui.e.UiScissorStack;
import autismclient.gui.e.SubpixelScissorTracker;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderState.class)
public abstract class AutismGuiRenderStateResetMixin {
   @Inject(method = "reset", at = @At("HEAD"))
   private void autism$clearScissorMirror(CallbackInfo ci) {
      UiScissorStack.load_V();
      SubpixelScissorTracker.init();
      autismclient.gui.f.UiAlphaStack.init_V_I(0);
   }
}
