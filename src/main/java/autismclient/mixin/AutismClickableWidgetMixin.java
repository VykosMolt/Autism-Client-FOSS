package autismclient.mixin;

import autismclient.util.OverlayManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractWidget.class)
public abstract class AutismClickableWidgetMixin {
   @Shadow
   protected boolean isHovered;

   @Inject(
      method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
      at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/AbstractWidget;isHovered:Z", shift = Shift.AFTER)
   )
   private void autism$suppressHoverWhenOverlayBlocks(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      if (OverlayManager.getInstance().isMouseOverCached(mouseX, mouseY)) {
         this.isHovered = false;
      }
   }
}
