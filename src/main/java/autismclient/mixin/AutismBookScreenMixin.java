package autismclient.mixin;

import autismclient.compat263.Compat263;
import autismclient.gui.f.b.DataPipe;
import autismclient.util.AutismClientMessaging;
import autismclient.util.ScreenOverlayHost;
import autismclient.util.aH;
import autismclient.util.cA;
import autismclient.util.cp;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.LecternScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BookViewScreen.class)
public abstract class AutismBookScreenMixin extends Screen implements cp {
   @Unique
   private static final Minecraft MC = Minecraft.getInstance();
   @Unique
   private final ScreenOverlayHost autism$host = ScreenOverlayHost.forBook();

   protected AutismBookScreenMixin(Component title) {
      super(title);
   }

   @Inject(method = "init", at = @At("TAIL"))
   private void yang$init(CallbackInfo ci) {
      this.autism$host.onInit(this, this.font);
   }

   @Override
   public void removed() {
      this.autism$host.onBookRemoved();
      super.removed();
   }

   @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
   private void yang$mouseClicked(MouseButtonEvent click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
      if (this.autism$host.isActive()) {
         if ((Object)this instanceof LecternScreen) {
            double mouseX = cA.init_D_D(click.x());
            double mouseY = cA.init_D_D(click.y());

            for (DataPipe button : aH.init(MC, this.autism$host.queueEditorOverlay())) {
               if (button.init(mouseX, mouseY, Compat263.mouseEventGlfwButton(click))) {
                  cir.setReturnValue(true);
                  return;
               }
            }
         }

         if (this.autism$host.mouseClicked(click)) {
            cir.setReturnValue(true);
         }
      }
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

   @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
   private void yang$keyPressed(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
      if (this.autism$host.keyPressed(input)) {
         cir.setReturnValue(true);
      }
   }

   @Override
   public boolean charTyped(CharacterEvent input) {
      return this.autism$host.charTyped(input) || super.charTyped(input);
   }

   @Override
   public void autism$closeWithPacket() {
      this.autism$closeWithPacket(true);
   }

   @Override
   public void autism$closeWithPacket(boolean notify) {
      MC.gui.setScreen(null);
   }

   @Override
   public void autism$closeWithoutPacket() {
      this.autism$closeWithoutPacket(true);
   }

   @Override
   public void autism$closeWithoutPacket(boolean notify) {
      MC.gui.setScreen(null);
      if (notify) {
         AutismClientMessaging.sendPrefixed("Book screen closed locally.");
      }
   }

   @Override
   public void autism$desync() {
      this.autism$desync(true);
   }

   @Override
   public void autism$desync(boolean notify) {
      if (notify) {
         AutismClientMessaging.sendPrefixed("This book screen has no update packet to desync.");
      }
   }
}
