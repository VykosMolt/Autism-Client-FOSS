package autismclient.mixin;

import autismclient.gui.sdf.UiScissorStack;
import autismclient.gui.sdf.SubpixelScissorTracker;
import autismclient.util.cA;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
public abstract class AutismGuiScissorMirrorMixin {
   @Inject(method = "<init>", at = @At("TAIL"))
   private void autism$freshExtractor(CallbackInfo ci) {
      UiScissorStack.clear();
   }

   @Inject(method = "enableScissor", at = @At("HEAD"), cancellable = true)
   private void autism$exactPush(int x0, int y0, int x1, int y1, CallbackInfo ci) {
      if (cA.init_Z()) {
         GuiGraphicsExtractor self = (GuiGraphicsExtractor)(Object)this;
         if (SubpixelScissorTracker.init_Z_GuiGraphicsExtract(self.scissorStack, self.pose(), x0, y0, x1, y1)) {
            UiScissorStack.load(self.scissorStack.peek());
            ci.cancel();
         }
      }
   }

   @Inject(method = "enableScissor", at = @At("TAIL"))
   private void autism$mirrorPush(int x0, int y0, int x1, int y1, CallbackInfo ci) {
      UiScissorStack.load(((GuiGraphicsExtractor)(Object)this).scissorStack.peek());
   }

   @Inject(method = "disableScissor", at = @At("HEAD"))
   private void autism$mirrorPop(CallbackInfo ci) {
      UiScissorStack.pop();
   }
}
