package autismclient.mixin;

import autismclient.gui.sdf.UiScissorStack;
import autismclient.gui.sdf.SubpixelScissorTracker;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderState.class)
public abstract class AutismGuiRenderStateResetMixin {
   @Inject(method = "reset", at = @At("HEAD"))
   private void autism$clearScissorMirror(CallbackInfo ci) {
      UiScissorStack.clear();
      SubpixelScissorTracker.clear();
      autismclient.gui.ui.UiAlphaStack.popTo(0);
   }
}
