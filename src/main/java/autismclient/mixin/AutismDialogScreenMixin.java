package autismclient.mixin;

import autismclient.util.ScreenOverlayHost;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.dialog.DialogScreen;
import net.minecraft.client.gui.screens.dialog.WaitingForResponseScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({DialogScreen.class, WaitingForResponseScreen.class})
public abstract class AutismDialogScreenMixin extends Screen {
   @Unique
   private final ScreenOverlayHost autism$host = ScreenOverlayHost.forDialog();

   protected AutismDialogScreenMixin(Component title) {
      super(title);
   }

   @Inject(method = "init", at = @At("TAIL"))
   private void autism$init(CallbackInfo ci) {
      this.autism$host.onInit(this, this.font);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
      return this.autism$host.mouseClicked(click) || super.mouseClicked(click, doubled);
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent click) {
      return this.autism$host.mouseReleased(click) || super.mouseReleased(click);
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
      return this.autism$host.mouseDragged(click, deltaX, deltaY) || super.mouseDragged(click, deltaX, deltaY);
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      return this.autism$host.mouseScrolled(mouseX, mouseY, verticalAmount) || super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   @Override
   public boolean keyPressed(KeyEvent input) {
      return this.autism$host.keyPressed(input) || super.keyPressed(input);
   }

   @Override
   public boolean charTyped(CharacterEvent input) {
      return this.autism$host.charTyped(input) || super.charTyped(input);
   }
}
