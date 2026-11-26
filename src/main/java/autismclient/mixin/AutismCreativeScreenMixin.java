package autismclient.mixin;

import autismclient.compat263.Compat263;
import autismclient.modules.ModuleRuntimeManager;
import autismclient.util.OverlayManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class AutismCreativeScreenMixin {
   @Unique
   private static final ThreadLocal<Boolean> autism$inSafeRecall = ThreadLocal.withInitial(() -> Boolean.FALSE);

   @Shadow
   protected abstract void extractTabButton(GuiGraphicsExtractor var1, int var2, int var3, CreativeModeTab var4);

   @Inject(method = "checkTabHovering", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$blockCoveredTabHover(GuiGraphicsExtractor graphics, CreativeModeTab tab, int mouseX, int mouseY, CallbackInfoReturnable<Boolean> cir) {
      ModuleRuntimeManager module = ModuleRuntimeManager.init_PoolEntry();
      if (module != null && module.init_Z() && OverlayManager.getInstance().isMouseOverCached(mouseX, mouseY)) {
         cir.setReturnValue(false);
      }
   }

   @Inject(method = "extractTabButton", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$blockCoveredTabCursor(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CreativeModeTab tab, CallbackInfo ci) {
      if (!autism$inSafeRecall.get()) {
         ModuleRuntimeManager module = ModuleRuntimeManager.init_PoolEntry();
         if (module != null && module.init_Z() && OverlayManager.getInstance().isMouseOverCached(mouseX, mouseY)) {
            autism$inSafeRecall.set(Boolean.TRUE);

            try {
               this.extractTabButton(graphics, -10000, -10000, tab);
            } finally {
               autism$inSafeRecall.set(Boolean.FALSE);
            }

            ci.cancel();
         }
      }
   }

   @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$mouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
      ModuleRuntimeManager module = ModuleRuntimeManager.init_PoolEntry();
      if (module != null && module.init_Z() && OverlayManager.getInstance().mouseClicked(event.x(), event.y(), Compat263.mouseEventGlfwButton(event))) {
         cir.setReturnValue(true);
      }
   }

   @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$mouseReleased(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
      ModuleRuntimeManager module = ModuleRuntimeManager.init_PoolEntry();
      if (module != null && module.init_Z() && OverlayManager.getInstance().mouseReleased(event.x(), event.y(), Compat263.mouseEventGlfwButton(event))) {
         cir.setReturnValue(true);
      }
   }

   @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$mouseDragged(MouseButtonEvent event, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
      ModuleRuntimeManager module = ModuleRuntimeManager.init_PoolEntry();
      if (module != null && module.init_Z() && OverlayManager.getInstance().mouseDragged(event.x(), event.y(), Compat263.mouseEventGlfwButton(event), deltaX, deltaY)) {
         cir.setReturnValue(true);
      }
   }

   @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
      ModuleRuntimeManager module = ModuleRuntimeManager.init_PoolEntry();
      if (module != null && module.init_Z() && OverlayManager.getInstance().mouseScrolled(mouseX, mouseY, verticalAmount)) {
         cir.setReturnValue(true);
      }
   }

   @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$keyPressed(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
      ModuleRuntimeManager module = ModuleRuntimeManager.init_PoolEntry();
      if (module != null && module.init_Z() && OverlayManager.getInstance().keyPressed(Compat263.keyEventGlfwKey(input), input.keycode(), Compat263.keyEventGlfwModifiers(input))
         )
       {
         cir.setReturnValue(true);
      }
   }

   @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$charTyped(CharacterEvent input, CallbackInfoReturnable<Boolean> cir) {
      ModuleRuntimeManager module = ModuleRuntimeManager.init_PoolEntry();
      if (module != null && module.init_Z() && OverlayManager.getInstance().charTyped((char)input.codepoint(), 0)) {
         cir.setReturnValue(true);
      }
   }
}
